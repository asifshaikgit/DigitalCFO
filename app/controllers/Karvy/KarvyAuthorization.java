package controllers.Karvy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.ClaimTransaction;
import model.CustomerDetail;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.Users;
import model.Vendor;
import model.VendorDetail;
import model.karvy.GSTFiling;
import model.karvy.KarvyGSTR1SalesAdvance;
import model.karvy.KarvyGSTR1SalesInvoice;
import model.karvy.KarvyGSTR2PurchaseInvoice;
import model.karvy.KarvyGSTSignUPdata;
import model.karvy.KarvyPurchaseInvoice;
import model.karvy.KarvySalesAdvance;
import model.karvy.KarvySalesInvoice;
import model.karvy.KarvySignUp;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;

import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Http.MultipartFormData;
import service.gstdatafiling.GstDataFilingService;
import service.gstdatafiling.GstDataFilingServiceImpl;
import views.html.errorPage;
import javax.inject.Inject;
import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IDOSException;
import com.idos.util.KARVY.AESShaEncryptionKARVY;
import com.idos.util.PWC.AESShaEncryptionPWC;
import play.mvc.Http;
import controllers.StaticController;
import flexjson.JSONSerializer;
import org.json.JSONArray;
import play.mvc.Http.Request;
import play.Application;

public class KarvyAuthorization extends StaticController {
	public static Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private static Request request;
	// private Http.Session session = request.session();

	@Inject
	public KarvyAuthorization(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	GenericDAO genericDao = new GenericJpaDAO();
	GstDataFilingService fileGSTdata = new GstDataFilingServiceImpl();

	public void saveGSTFilingData(Users user, Transaction transaction, EntityManager entityManager)
			throws IDOSException {
		fileGSTdata.saveGSTFilingData(user, entityManager, transaction);
	}

	@Transactional
	public Result submitTransactionsToKarvy(Request request) {
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = null;
		try {
			// JsonNode json =request.body().asJson();
			user = getUserInfo(request);
			entityTransaction.begin();
			// sell transaction data
			StringBuilder newsbquery = new StringBuilder(
					"select obj from GSTFiling obj WHERE obj.transactionPurpose.id in (1,2) and obj.gstFilingStatus=0 and obj.presentStatus=1 and obj.organizationId.id="
							+ user.getOrganization().getId());

			List<GSTFiling> gstFilingData = genericDAO.executeSimpleQuery(newsbquery.toString(), entityManager);
			log.log(Level.INFO, "gst data size=" + gstFilingData.size());
			int totalSell = 0;
			for (GSTFiling gstData : gstFilingData) {
				log.log(Level.INFO, "inside transaction data=" + gstData.getTransactionPurpose());
				Transaction transaction = gstData.getTransactionId();
				int msgInserted = sendSellTranDataToKarvy(transaction, entityManager);

				if (msgInserted != 0) {
					gstData.setGstFilingStatus(1); // success
				} else {
					gstData.setGstFilingStatus(2); // error
				}
				genericDAO.saveOrUpdate(gstData, user, entityManager);
				// totalSell=totalSell+msgInserted;
				totalSell = totalSell + msgInserted;
			}
			results.put("totalSell", totalSell);
			// buy transaction data
			newsbquery = new StringBuilder(
					"select obj from GSTFiling obj WHERE obj.transactionPurpose.id in (3,4) and obj.gstFilingStatus=0 and obj.presentStatus=1 and obj.organizationId.id="
							+ user.getOrganization().getId());
			gstFilingData = genericDAO.executeSimpleQuery(newsbquery.toString(), entityManager);
			int totalBuy = 0;
			for (GSTFiling gstData : gstFilingData) {
				Transaction transaction = gstData.getTransactionId();
				int msgInserted = sendBuyTranDataToKarvy(transaction, entityManager);
				if (msgInserted != 0) {
					gstData.setGstFilingStatus(1); // success
				} else {
					gstData.setGstFilingStatus(2); // error
				}
				genericDAO.saveOrUpdate(gstData, user, entityManager);
				totalBuy = totalBuy + msgInserted;
			}
			results.put("totalBuy", totalBuy);
			// receive adv from cust
			// buy transaction data
			newsbquery = new StringBuilder(
					"select obj from GSTFiling obj WHERE obj.transactionPurpose.id =6  and obj.gstFilingStatus=0 and obj.presentStatus=1 and obj.organizationId.id="
							+ user.getOrganization().getId());
			gstFilingData = genericDAO.executeSimpleQuery(newsbquery.toString(), entityManager);
			int totalRecAdvFromCust = 0;
			for (GSTFiling gstData : gstFilingData) {
				Transaction transaction = gstData.getTransactionId();
				int msgInserted = sendReceiveAdvFromCustTranDataToKarvy(transaction, entityManager, request);
				if (msgInserted != 0) {
					gstData.setGstFilingStatus(1); // success
				} else {
					gstData.setGstFilingStatus(2); // error
				}
				genericDAO.saveOrUpdate(gstData, user, entityManager);
				totalRecAdvFromCust = totalRecAdvFromCust + msgInserted;
			}
			results.put("totalRecAdvFromCust", totalRecAdvFromCust);
			entityTransaction.commit();
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	public static Integer sendSellTranDataToKarvy(Transaction transaction, EntityManager entityManager) {
		ArrayList<KarvySalesInvoice> propertiesArray = new ArrayList();
		int msgInserted = 0;
		try {
			Integer typeOfSupply = transaction.getTypeOfSupply();
			Vendor customer = Vendor.findById(transaction.getTransactionVendorCustomer().getId());
			// get customer
			Map criterias = new HashMap();
			CustomerDetail customerDetail = null;
			if (typeOfSupply == 3) {
				criterias.put("customer.id", customer.getId());
				criterias.put("presentStatus", 1);
				List<CustomerDetail> customerDetailList = genericDAO.findByCriteria(CustomerDetail.class, criterias,
						entityManager);
				customerDetail = customerDetailList.get(0);
			} else {
				String customerGSTIN = transaction.getDestinationGstin();
				customerDetail = CustomerDetail.findByCustomerGSTNID(entityManager, customer.getId(), customerGSTIN);
			}
			Branch branch = transaction.getTransactionBranch();
			criterias.clear();
			criterias.put("transaction.id", transaction.getId());
			criterias.put("presentStatus", 1);
			List<TransactionItems> tranItemsList = genericDAO.findByCriteria(TransactionItems.class, criterias,
					entityManager);
			if (tranItemsList != null && tranItemsList.size() > 0) {
				for (TransactionItems tranItem : tranItemsList) {
					KarvySalesInvoice salesData = new KarvySalesInvoice();
					log.log(Level.INFO, "get the transaction item details");
					salesData.setProcessgstin(branch.getGstin());
					salesData.setBranchcode(branch.getId().toString());
					salesData.setCustomername(customer.getName());
					if (customerDetail.getGstin() != null) {
						String customerGSTIN = customerDetail.getGstin();
						salesData.setCustomergstin(customerGSTIN);
						String pos = customerGSTIN.substring(0, 2);
						salesData.setPos(pos);
					}
					if (branch.getGstin() != null) {
						String branchGSTIN = branch.getGstin();
						String stateCode = branchGSTIN.substring(0, 2);
						salesData.setStatecode(stateCode);
						// salesData.setStatecode(customerDetail.getShippingStateCode());
					}

					/*
					 * if(customerDetail.get(0).getBillingStateCode()!=null){
					 * salesData.setPos(customerDetail.get(0).getBillingStateCode()); //bill to
					 * address state code
					 * }
					 */
					SimpleDateFormat reportdf = new SimpleDateFormat("dd/MM/yyyy");
					salesData.setInvoicedate(reportdf.format(transaction.getTransactionDate()));

					Specifics tranItemSpec = tranItem.getTransactionSpecifics();
					if (tranItemSpec.getGstItemCode() != null) {
						// salesData.setHsnsac(tranItemSpec.getGstItemCode());
						salesData.setHsnsac("8541"); // dummy hsn as they have master data and our hsn not matching, so
														// this is workaround suggested by karvy
					}
					salesData.setProductid(tranItemSpec.getId().toString());
					salesData.setInvoiceno(tranItem.getId().toString());
					salesData.setInvoicevalue(tranItem.getInvoiceValue());
					salesData.setReversecharge("0");

					salesData.setTaxablevalue(tranItem.getGrossAmount());
					salesData.setAdvancedocumentdate(reportdf.format(transaction.getTransactionDate()));

					salesData.setQuantity(tranItem.getNoOfUnits());
					if (tranItemSpec.getIncomeUnitsMeasure() != null) {
						// salesData.setUqc(tranItemSpec.getIncomeUnitsMeasure());
						salesData.setUqc("Pcs"); // it is as per their approved document UQC_CODE, so either need that
													// UQC_CODE mapping we have to maintain in future
					}
					salesData.setDescription(tranItemSpec.getName());
					log.log(Level.INFO, "data added");
					// set taxes
					for (int i = 1; i < 6; i++) {
						String txnTaxName = "";
						Double taxRate = 0.0;
						Double taxAmt = 0.0;
						switch (i) {
							case 1:
								txnTaxName = tranItem.getTaxName1();
								taxRate = tranItem.getTaxRate1();
								taxAmt = tranItem.getTaxValue1();
								break;
							case 2:
								txnTaxName = tranItem.getTaxName2();
								taxRate = tranItem.getTaxRate2();
								taxAmt = tranItem.getTaxValue2();
								break;
							case 3:
								txnTaxName = tranItem.getTaxName3();
								taxRate = tranItem.getTaxRate3();
								taxAmt = tranItem.getTaxValue3();
								break;
							case 4:
								txnTaxName = tranItem.getTaxName4();
								taxRate = tranItem.getTaxRate4();
								taxAmt = tranItem.getTaxValue4();
								break;
							case 5:
								txnTaxName = tranItem.getTaxName5();
								taxRate = tranItem.getTaxRate5();
								taxAmt = tranItem.getTaxValue5();
								break;
						}
						if (txnTaxName != null && txnTaxName != "") {
							if (txnTaxName.indexOf("SGST") != -1) {
								salesData.setSgstrate(taxRate);
								salesData.setSgstamount(taxAmt);
							}
							if (txnTaxName.indexOf("CGST") != -1) {
								salesData.setCgstrate(taxRate);
								salesData.setCgstamount(taxAmt);
							}
							if (txnTaxName.indexOf("IGST") != -1) {
								salesData.setIgstrate(taxRate);
								salesData.setIgstamount(taxAmt);
							}
							if (txnTaxName.indexOf("UTGST") != -1) {
								salesData.setUtgstrate(taxRate);
								salesData.setUtgstamount(taxAmt);
							}
							if (txnTaxName.indexOf("CESS") != -1) {
								salesData.setCessamount(taxAmt);
							}
						}
					}
					propertiesArray.add(salesData);
				}
			}
			KarvyGSTR1SalesInvoice poa = new KarvyGSTR1SalesInvoice();
			poa.setSalesinvoicelist(propertiesArray);
			msgInserted = callKarvyAPI(poa, request);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msgInserted;
	}

	public static Integer callKarvyAPI(KarvyGSTR1SalesInvoice poa, Request request) {
		int msgInserted = 0;
		try {
			JSONSerializer serializer = new JSONSerializer();
			String strBodyJSON = serializer.exclude(new String[] { "*.class" })
					.include(new String[] { "salesinvoicelist" }).serialize(poa);

			log.log(Level.INFO, "json=" + strBodyJSON);
			Users usrinfo = null;
			usrinfo = getUserInfo(request);
			URL url = new URL("http://api.karvygst.com/Save/GSTR1SalesInvoices"); // "http://rapi.karvygst.com/v0.3/Save/GSTR1SalesInvoice");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("clientid", "KarvyGst");
			con.setRequestProperty("clientsecret", "KarvyGst@123");
			// con.setRequestProperty("emailid", "suraj@iDOSindia.onmicrosoft.com");

			String encryptedString = usrinfo.getPasswordForKarvy();
			String decryptedString = AESShaEncryptionKARVY.Decrypt(encryptedString);
			con.setRequestProperty("emailid", usrinfo.getEmail());
			con.setRequestProperty("password", decryptedString);
			con.setRequestProperty("accept", "application/json");
			con.setRequestProperty("type", "Postman");

			OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
			os.write(strBodyJSON);
			os.flush();
			os.close();

			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == 200) {
				BufferedReader buffer = new BufferedReader(
						new java.io.InputStreamReader(con.getInputStream(), "utf-8"));

				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}

				log.log(Level.FINE, "SB output : " + sb);
				buffer.close();
				JSONObject jObject = new JSONObject(sb.toString());
				String statusCode = jObject.getString("status_cd");
				/*
				 * SB output : {
				 * "status_cd": "1",
				 * "message": "0 Invoices Inserted",
				 * "transid": "1044",
				 * "total": "1 Invoices",
				 * "sucess": "0 Invoices",
				 * "failure": "1 Invoices"
				 * }
				 */
				if (statusCode.equals("1")) {
					String transid = jObject.getString("transid");
					String message = jObject.getString("message");
					String total = jObject.getString("total");
					String failure = jObject.getString("failure");
					String msgInsertedStr = message.substring(0, 1);
					try {
						msgInserted = Integer.parseInt(msgInsertedStr);
						// msgInserted=Integer.parseInt(failure);
						// is an integer!
					} catch (Exception e) {
					}
					if (message.contains("2 Invoices Inserted")) {
					}
				} else {
					// { "status_cd": "0", "error_cd": "Err1001","message": "unable to process your
					// request"}

					log.log(Level.FINE, "ERROR " + con.getResponseMessage());
				}
			} else {
				log.log(Level.FINE, "ERROR " + con.getResponseMessage());
			}
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgInserted;
	}

	// Receive advance from customer
	public static Integer sendReceiveAdvFromCustTranDataToKarvy(Transaction transaction, EntityManager entityManager,
			Request request) {
		ArrayList<KarvySalesAdvance> propertiesArray = new ArrayList();
		int msgInserted = 0;
		try {
			Integer typeOfSupply = transaction.getTypeOfSupply();
			Vendor customer = Vendor.findById(transaction.getTransactionVendorCustomer().getId());
			// get customer
			Map criterias = new HashMap();
			CustomerDetail customerDetail = null;
			if (typeOfSupply == 3) {
				criterias.put("customer.id", customer.getId());
				criterias.put("presentStatus", 1);
				List<CustomerDetail> customerDetailList = genericDAO.findByCriteria(CustomerDetail.class, criterias,
						entityManager);
				customerDetail = customerDetailList.get(0);
			} else {
				String customerGSTIN = transaction.getDestinationGstin();
				customerDetail = CustomerDetail.findByCustomerGSTNID(entityManager, customer.getId(), customerGSTIN);
			}
			Branch branch = transaction.getTransactionBranch();
			criterias.clear();
			criterias.put("transaction.id", transaction.getId());
			criterias.put("presentStatus", 1);
			List<TransactionItems> tranItemsList = genericDAO.findByCriteria(TransactionItems.class, criterias,
					entityManager);
			if (tranItemsList != null && tranItemsList.size() > 0) {
				for (TransactionItems tranItem : tranItemsList) {
					KarvySalesAdvance salesData = new KarvySalesAdvance();
					salesData.setProcessgstin(branch.getGstin());
					salesData.setBranchcode(branch.getId().toString());
					salesData.setReceiverName(customer.getName());
					/*
					 * if(customerDetail.getGstin()!=null){
					 * salesData.setGstin(customerDetail.getGstin());
					 * }
					 */
					if (customerDetail.getGstin() != null) {
						String customerGSTIN = customerDetail.getGstin();
						salesData.setGstin(customerDetail.getGstin());
						String pos = customerGSTIN.substring(0, 2);
						salesData.setPos(pos);
					}
					if (branch.getGstin() != null) {
						String branchGSTIN = branch.getGstin();
						String stateCode = branchGSTIN.substring(0, 2);
						salesData.setStatecode(stateCode);
					}
					/*
					 * if(customerDetail.getShippingStateCode()!=null){
					 * salesData.setStatecode(customerDetail.getShippingStateCode());
					 * salesData.setPos(customerDetail.getShippingStateCode());
					 * }
					 */

					SimpleDateFormat reportdf = new SimpleDateFormat("dd/MM/yyyy");

					Specifics tranItemSpec = tranItem.getTransactionSpecifics();
					if (tranItemSpec.getGstItemCode() != null) {
						// salesData.setHsnsac(tranItemSpec.getGstItemCode());
						salesData.setHsnsac("8541");
					}
					salesData.setDocumentdate(reportdf.format(transaction.getTransactionDate()));
					salesData.setDocumentno(transaction.getInvoiceNumber());
					salesData.setDescription("Receive Advance From Cust");
					salesData.setAmount(transaction.getNetAmount());
					// set taxes
					for (int i = 1; i < 6; i++) {
						String txnTaxName = "";
						Double taxRate = 0.0;
						Double taxAmt = 0.0;
						switch (i) {
							case 1:
								txnTaxName = tranItem.getTaxName1();
								taxRate = tranItem.getTaxRate1();
								taxAmt = tranItem.getTaxValue1();
								break;
							case 2:
								txnTaxName = tranItem.getTaxName2();
								taxRate = tranItem.getTaxRate2();
								taxAmt = tranItem.getTaxValue2();
								break;
							case 3:
								txnTaxName = tranItem.getTaxName3();
								taxRate = tranItem.getTaxRate3();
								taxAmt = tranItem.getTaxValue3();
								break;
							case 4:
								txnTaxName = tranItem.getTaxName4();
								taxRate = tranItem.getTaxRate4();
								taxAmt = tranItem.getTaxValue4();
								break;
							case 5:
								txnTaxName = tranItem.getTaxName5();
								taxRate = tranItem.getTaxRate5();
								taxAmt = tranItem.getTaxValue5();
								break;
						}
						if (txnTaxName != null && txnTaxName != "") {
							if (txnTaxName.indexOf("SGST") != -1) {
								salesData.setSgstrate(taxRate);
								salesData.setSgstamount(taxAmt);
							}
							if (txnTaxName.indexOf("CGST") != -1) {
								salesData.setCgstrate(taxRate);
								salesData.setCgstamount(taxAmt);
							}
							if (txnTaxName.indexOf("IGST") != -1) {
								salesData.setIgstrate(taxRate);
								salesData.setIgstamount(taxAmt);
							}
							if (txnTaxName.indexOf("UTGST") != -1) {
								salesData.setUtgstrate(taxRate);
								salesData.setUtgstamount(taxAmt);
							}
							if (txnTaxName.indexOf("CESS") != -1) {
								// salesData.setCessrate(taxRate);
								salesData.setCessamount(taxAmt);
							}
						}
					}
					propertiesArray.add(salesData);
				}
			}
			KarvyGSTR1SalesAdvance poa = new KarvyGSTR1SalesAdvance();
			poa.setSalestaxinvlist(propertiesArray);
			msgInserted = callKarvySalesAdvanceAPI(poa, request);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msgInserted;
	}

	public static Integer callKarvySalesAdvanceAPI(KarvyGSTR1SalesAdvance poa, Request request) {
		int msgInserted = 0;
		try {
			JSONSerializer serializer = new JSONSerializer();
			String strBodyJSON = serializer.exclude(new String[] { "*.class" })
					.include(new String[] { "salestaxinvlist" }).serialize(poa);

			Users usrinfo = null;
			usrinfo = getUserInfo(request);
			// URL url = new URL("http://rapi.karvygst.com/v0.3/Save/GSTR1SalesAdvance");
			URL url = new URL("http://api.karvygst.com/Save/GSTR1SalesAdvance");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("clientid", "KarvyGst");
			con.setRequestProperty("clientsecret", "KarvyGst@123");
			// con.setRequestProperty("emailid", "suraj@iDOSindia.onmicrosoft.com");
			// con.setRequestProperty("password", "Suraj@1");
			String encryptedString = usrinfo.getPasswordForKarvy();
			String decryptedString = AESShaEncryptionKARVY.Decrypt(encryptedString);
			con.setRequestProperty("emailid", usrinfo.getEmail());
			con.setRequestProperty("password", decryptedString);
			con.setRequestProperty("accept", "application/json");

			msgInserted = callKarvyToSendData(strBodyJSON, con);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msgInserted;
	}

	// Receive advance from customer
	public static Integer sendBuyTranDataToKarvy(Transaction transaction, EntityManager entityManager) {
		ArrayList<KarvyPurchaseInvoice> propertiesArray = new ArrayList();
		int msgInserted = 0;
		try {
			Integer typeOfSupply = transaction.getTypeOfSupply();
			Vendor vendor = Vendor.findById(transaction.getTransactionVendorCustomer().getId());
			// get customer
			Map criterias = new HashMap();
			VendorDetail vendorDetail = null;
			if (typeOfSupply == 3) {
				criterias.put("customer.id", vendor.getId());
				criterias.put("presentStatus", 1);
				List<VendorDetail> vendorDetailList = genericDAO.findByCriteria(VendorDetail.class, criterias,
						entityManager);
				vendorDetail = vendorDetailList.get(0);
			} else {
				String vendorGSTIN = transaction.getDestinationGstin();
				vendorDetail = VendorDetail.findByVendorGSTNID(entityManager, vendor.getId(), vendorGSTIN);
			}
			Branch branch = transaction.getTransactionBranch();
			criterias.clear();
			criterias.put("transaction.id", transaction.getId());
			criterias.put("presentStatus", 1);
			List<TransactionItems> tranItemsList = genericDAO.findByCriteria(TransactionItems.class, criterias,
					entityManager);
			if (tranItemsList != null && tranItemsList.size() > 0) {
				for (TransactionItems tranItem : tranItemsList) {
					KarvyPurchaseInvoice salesData = new KarvyPurchaseInvoice();
					salesData.setProcessgstin(branch.getGstin());
					salesData.setBranchcode(branch.getId().toString());
					salesData.setSupplierName(vendor.getName());
					if (vendorDetail.getGstin() != null) {
						salesData.setSupplierGSTIN(vendorDetail.getGstin());
					}

					if (vendorDetail.getStateCode() != null) {
						salesData.setPos(vendorDetail.getStateCode());
					}

					SimpleDateFormat reportdf = new SimpleDateFormat("dd/MM/yyyy");

					Specifics tranItemSpec = tranItem.getTransactionSpecifics();
					if (tranItemSpec.getGstItemCode() != null) {
						salesData.setHsnsac(tranItemSpec.getGstItemCode());
					}
					salesData.setDescription(tranItemSpec.getName()); // item name
					salesData.setInvoiceNo(tranItem.getId().toString());
					salesData.setInvoiceDate(reportdf.format(transaction.getTransactionDate()));
					salesData.setTotalInvoiceValue(transaction.getNetAmount());
					salesData.setTaxableValue(tranItem.getGrossAmount());
					salesData.setTotalTaxableValue(tranItem.getGrossAmount());
					salesData.setQuantity(tranItem.getNoOfUnits());
					if (tranItemSpec.getIncomeUnitsMeasure() != null) {
						// salesData.setUqc(tranItemSpec.getIncomeUnitsMeasure());
						salesData.setUqc("Pcs");
					}
					// set taxes
					for (int i = 1; i < 6; i++) {
						String txnTaxName = "";
						Double taxRate = 0.0;
						Double taxAmt = 0.0;
						switch (i) {
							case 1:
								txnTaxName = tranItem.getTaxName1();
								taxRate = tranItem.getTaxRate1();
								taxAmt = tranItem.getTaxValue1();
								break;
							case 2:
								txnTaxName = tranItem.getTaxName2();
								taxRate = tranItem.getTaxRate2();
								taxAmt = tranItem.getTaxValue2();
								break;
							case 3:
								txnTaxName = tranItem.getTaxName3();
								taxRate = tranItem.getTaxRate3();
								taxAmt = tranItem.getTaxValue3();
								break;
							case 4:
								txnTaxName = tranItem.getTaxName4();
								taxRate = tranItem.getTaxRate4();
								taxAmt = tranItem.getTaxValue4();
								break;
							case 5:
								txnTaxName = tranItem.getTaxName5();
								taxRate = tranItem.getTaxRate5();
								taxAmt = tranItem.getTaxValue5();
								break;
						}
						if (txnTaxName != null && txnTaxName != "") {
							if (txnTaxName.indexOf("SGST") != -1) {
								salesData.setSgstRate(taxRate);
								salesData.setSgstAmount(taxAmt);
							}
							if (txnTaxName.indexOf("CGST") != -1) {
								salesData.setCgstRate(taxRate);
								salesData.setCgstAmount(taxAmt);
							}
							if (txnTaxName.indexOf("IGST") != -1) {
								salesData.setIgstRate(taxRate);
								salesData.setIgstAmount(taxAmt);
							}
							if (txnTaxName.indexOf("UTGST") != -1) {
								salesData.setUtgstRate(taxRate);
								salesData.setUtgstAmount(taxAmt);
							}
							if (txnTaxName.indexOf("CESS") != -1) {
								salesData.setCessRate(taxRate);
								salesData.setCessamount(taxAmt);
							}
						}
					}
					propertiesArray.add(salesData);
				}
			}
			KarvyGSTR2PurchaseInvoice poa = new KarvyGSTR2PurchaseInvoice();
			poa.setPurchaseinvoicelist(propertiesArray);
			msgInserted = callKarvyPurchaseInvoiceAPI(poa, request);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msgInserted;
	}

	public static Integer callKarvyPurchaseInvoiceAPI(KarvyGSTR2PurchaseInvoice poa, Request request) {
		int msgInserted = 0;
		try {
			JSONSerializer serializer = new JSONSerializer();
			String strBodyJSON = serializer.exclude(new String[] { "*.class" })
					.include(new String[] { "purchaseinvoicelist" }).serialize(poa);

			Users usrinfo = null;
			usrinfo = getUserInfo(request);
			URL url = new URL("http://api.karvygst.com/Save/GSTR2PurchaseInvoice");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("clientid", "KarvyGst");
			con.setRequestProperty("clientsecret", "KarvyGst@123");
			// con.setRequestProperty("emailid", "suraj@iDOSindia.onmicrosoft.com");
			// con.setRequestProperty("password", "Suraj@1");

			String encryptedString = usrinfo.getPasswordForKarvy();
			String decryptedString = AESShaEncryptionKARVY.Decrypt(encryptedString);
			con.setRequestProperty("emailid", usrinfo.getEmail());
			con.setRequestProperty("password", decryptedString);
			con.setRequestProperty("accept", "application/json");

			msgInserted = callKarvyToSendData(strBodyJSON, con);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msgInserted;
	}

	public static Integer callKarvyToSendData(String strBodyJSON, HttpURLConnection con) {
		int msgInserted = 0;
		try {
			OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
			os.write(strBodyJSON);
			os.flush();
			os.close();

			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == 200) {
				BufferedReader buffer = new BufferedReader(
						new java.io.InputStreamReader(con.getInputStream(), "utf-8"));

				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}

				log.log(Level.FINE, "SB output : " + sb);
				buffer.close();
				JSONObject jObject = new JSONObject(sb.toString());
				String statusCode = jObject.getString("status_cd");
				if (statusCode.equals("1")) {
					String transid = jObject.getString("transid");
					String message = jObject.getString("message");
					String msgInsertedStr = message.substring(0, 1);
					try {
						msgInserted = Integer.parseInt(msgInsertedStr);
						// is an integer!
					} catch (Exception e) {
					}
					if (message.contains("2 Invoices Inserted")) {

					}
				} else {
					// { "status_cd": "0", "error_cd": "Err1001","message": "unable to process your
					// request"}

					log.log(Level.FINE, "ERROR " + con.getResponseMessage());
				}
			} else {

				log.log(Level.FINE, "ERROR " + con.getResponseMessage());
			}
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgInserted;
	}

	@Transactional
	public static Integer signupForKarvyGST(ObjectNode karvyRow) {
		log.log(Level.INFO, "inside karvy to get the user detail");
		ArrayList<KarvySignUp> propertiesArray = new ArrayList<KarvySignUp>();
		int msgInserted = 0;
		try {

			String email = karvyRow.get("EmailID").asText();
			String password = karvyRow.get("Password").asText();
			String mobileNo = karvyRow.get("MobileNo").asText();
			KarvySignUp signupData = new KarvySignUp();
			signupData.setEmailID(email);
			signupData.setPassword(password);
			signupData.setMobileNo(mobileNo);
			signupData.setDomainID("Tax payer");
			propertiesArray.add(signupData);

			KarvyGSTSignUPdata poa = new KarvyGSTSignUPdata();
			poa.setSignuplist(propertiesArray);
			msgInserted = callKarvySignUpAPI(poa);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msgInserted;
	}

	public static Integer callKarvySignUpAPI(KarvyGSTSignUPdata signupData) {
		int msgInserted = 0;
		try {
			JSONSerializer serializer = new JSONSerializer();
			// String strBodyJSON = serializer.exclude(new String[] { "*.class"
			// }).include(new String[]{ "signuplist"}).serialize(signupData);
			String strBodyJSON = serializer.include(new String[] { "signuplist" }).serialize(signupData);
			URL url = new URL("http://api.karvygst.com/Save/Signup");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("clientid", "KarvyGst");
			con.setRequestProperty("clientsecret", "KarvyGst@123");
			con.setRequestProperty("emailid", "idos@gmail.com");
			con.setRequestProperty("password", "Idos@1");
			con.setRequestProperty("accept", "application/json");
			msgInserted = callKarvyToSendSignUpData(strBodyJSON, con);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return msgInserted;
	}

	public static Integer callKarvyToSendSignUpData(String strBodyJSON, HttpURLConnection con) {
		int msgInserted = 0;
		try {
			log.log(Level.INFO, "string json=" + strBodyJSON);
			OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
			os.write(strBodyJSON);
			os.flush();
			os.close();

			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == 200) {
				BufferedReader buffer = new BufferedReader(
						new java.io.InputStreamReader(con.getInputStream(), "utf-8"));
				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}
				/*
				 * [ { "status_cd": "0", "message": "Email Already Exists", "username":
				 * "manali.mungikar@gmail.com", "businessid": "", "aspid": "" } ]
				 */
				log.log(Level.FINE, "SB output : " + sb);
				buffer.close();
				JSONArray jArray1 = new JSONArray(sb.toString());
				JSONObject jObject = jArray1.getJSONObject(0);

				String statusCode = jObject.getString("status_cd");
				if (statusCode.equals("1")) {
					String transid = jObject.getString("businessid");
					String message = jObject.getString("message");
					String msgInsertedStr = message.substring(0, 1);
					try {
						msgInserted = Integer.parseInt(msgInsertedStr);
					} catch (Exception e) {
					}
					if (message.contains("Registered Sucessfully")) {
					}
				}
			} else {
				log.log(Level.FINE, "ERROR " + con.getResponseMessage());
			}
			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgInserted;
	}

	/* Added by Puja Lohia 27th Feb 2018 */

	@Transactional
	public Result callKARVYUrl(Request request) {
		log.log(Level.FINE, ">>>> Start in callKarvyurl()");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();

		Users userinfo = null;
		String userEmail = null;
		try {
			userinfo = getUserInfo(request);
			if (userinfo == null) {
				return unauthorized();
			}

			URL url = new URL("http://api2.karvygst.com/GetData/GetToken");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("clientid", "KarvyGst");
			con.setRequestProperty("clientsecret", "KarvyGst@123");
			String encryptedString = userinfo.getPasswordForKarvy();
			String decryptedString = AESShaEncryptionKARVY.Decrypt(encryptedString);
			con.setRequestProperty("emailid", userinfo.getEmail());
			con.setRequestProperty("password", decryptedString);

			String sessionKey = callKarvyToGenerateSessionKey(con);
			String karvyurl = "https://dev.karvygst.com/Registration/LoginByToken?Token=" + sessionKey;
			log.log(Level.FINE, karvyurl);
			result.put("karvyurl", karvyurl);
			return Results.ok(result);

		} catch (Exception ex) {
			log.log(Level.SEVERE, userEmail, ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, userEmail, null,
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	/* Added by Puja Lohia 27th Feb 2018 */
	public static String callKarvyToGenerateSessionKey(HttpURLConnection con) {

		String sessionKey = null;

		try {
			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == 200) {
				BufferedReader buffer = new BufferedReader(
						new java.io.InputStreamReader(con.getInputStream(), "utf-8"));

				String line = null;
				while ((line = buffer.readLine()) != null) {
					sb.append(line + "\n");
				}
				log.log(Level.FINE, "SB output : " + sb);
				buffer.close();

				JSONObject jsonObj = new JSONObject(sb.toString());
				JSONArray arrJson = jsonObj.getJSONArray("table");
				sessionKey = arrJson.getJSONObject(0).getString("sessionKey");
			}
		} catch (Exception e) {
		}
		return sessionKey;
	}

	public void saveGSTFilingDataForClaimTransaction(Users user, ClaimTransaction claimTransaction,
			EntityManager entityManager) throws IDOSException {
		fileGSTdata.saveGSTFilingDataForClaimTransaction(user, entityManager, claimTransaction);

	}

}
