package com.idos.dao;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Calendar;

import com.idos.util.FileUtil;
import com.idos.util.IdosConstants;
import com.idos.util.NumberToWordsInt;

import com.typesafe.config.Config;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.DigitalSignatureBranchWise;
import model.IDOSCountry;
import model.InvoiceReportModel;
import model.Organization;
import model.ReceiptAdvanceModal;
import model.Transaction;
import model.TransactionInvoice;
import model.TransactionItems;
import model.Users;

public class ReceiptDAOImpl implements ReceiptDAO {
	private static SimpleDateFormat idosdf1 = new SimpleDateFormat("MMM dd,yyyy");

	public String resolveFile(String fileName) {
		fileName = fileName.replace("\\", "/");
		fileName = fileName.replaceAll("\\u0020", "%20");
		URI uri;
		String strPath = null;
		try {
			uri = new URI(fileName);

			strPath = uri.getPath();
		} catch (URISyntaxException e) {
			log.log(Level.SEVERE, "Error", e);
		}
		return strPath;
	}

	@Override
	public ReceiptAdvanceModal generateReceipt(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception {
		Transaction transaction = null;
		ReceiptAdvanceModal receiptAdvanceModal = new ReceiptAdvanceModal();
		String transactionId = json.findValue("entityTxnId") != null ? json.findValue("entityTxnId").asText() : null;
		if (transactionId != null) {
			transaction = Transaction.findById(Long.parseLong(transactionId));
			if (transaction != null) {
				if (transaction.getTransactionBranchOrganization() != null) {
					String currency = null;
					String absoluteLogoPath = null;
					String companyLogo = FileUtil.getCompanyLogo(transaction.getTransactionBranchOrganization());
					if (companyLogo != null && !"".equals(companyLogo)) {
						receiptAdvanceModal.setCompanyLogo(companyLogo);
					}

					if (transaction.getTransactionBranchOrganization().getName() != null) {
						receiptAdvanceModal.setCompanyName(transaction.getTransactionBranchOrganization().getName());
					}
					if (transaction.getTransactionVendorCustomer() != null) {
						receiptAdvanceModal.setReceivedFrom(transaction.getTransactionVendorCustomer().getName() + "("
								+ transaction.getTransactionVendorCustomer().getEmail());
					}
					if (transaction.getTransactionBranchOrganization().getRegisteredAddress() != null) {
						receiptAdvanceModal
								.setCompanyAddress(
										transaction.getTransactionBranchOrganization().getRegisteredAddress());
					}
					if (transaction.getNetAmount() != null) {
						receiptAdvanceModal
								.setAmountReceived(IdosConstants.decimalFormat.format(transaction.getNetAmount()));
					}
					if (transaction.getTransactionPurpose().getTransactionPurpose()
							.equals("Receive payment from customer")) {
						if (transaction.getPaidInvoiceRefNumber() != null) {
							receiptAdvanceModal.setRefNumber(transaction.getPaidInvoiceRefNumber());
						}
					} else if (transaction.getTransactionPurpose().getTransactionPurpose()
							.equals("Receive advance from customer")) {
						receiptAdvanceModal.setRefNumber("Advance");
					}
					receiptAdvanceModal.setInvoiceNumber(transaction.getInvoiceNumber());
					if (transaction.getRemarks() != null) {
						receiptAdvanceModal.setRemarks(transaction.getRemarks());
					}
					if (transaction.getCreatedBy() != null) {
						receiptAdvanceModal.setReceiptCreatedBy(transaction.getCreatedBy().getFullName() + "("
								+ transaction.getCreatedBy().getEmail() + ")");
					}
					if (transaction.getTransactionBranch() != null) {
						if (transaction.getTransactionBranch().getCurrency() != null) {
							IDOSCountry country = IDOSCountry
									.findById(Long.getLong(transaction.getTransactionBranch().getCurrency()));
							if (country != null) {
								receiptAdvanceModal.setCurrency(country.getCurrencyCode());
							}
						}
					}
				}
			}
		}
		return receiptAdvanceModal;
	}

	@Override
	public Map<String, Object> generateGstInvoiceData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception {
		Map<String, Object> map = new HashMap<>();
		Transaction transaction = null;
		ReceiptAdvanceModal receiptAdvanceModal = new ReceiptAdvanceModal();
		String transactionId = json.findValue("entityTxnId") != null ? json.findValue("entityTxnId").asText() : null;
		if (transactionId != null) {
			transaction = Transaction.findById(Long.parseLong(transactionId));
			if (transaction != null) {
				if (transaction.getTransactionBranchOrganization() != null) {
					String currency = null;
					String absoluteLogoPath = null;
					String companyLogo = FileUtil.getCompanyLogo(transaction.getTransactionBranchOrganization());
					if (companyLogo != null && !"".equals(companyLogo)) {
						map.put("companyLogo", companyLogo);
					}

					if (transaction.getTransactionBranchOrganization().getName() != null) {
						map.put("companyName", transaction.getTransactionBranchOrganization().getName());
					} else {
						map.put("companyName", "");
					}

					if (transaction.getTransactionBranch() != null) {
						if (transaction.getTransactionBranch().getAddress() != null) {
							map.put("companyAddress", transaction.getTransactionBranch().getAddress());
						} else {
							map.put("companyAddress", "");
						}

						if (transaction.getTransactionBranch().getGstin() != null) {
							map.put("branchGSTIn", transaction.getTransactionBranch().getGstin());
						} else {
							map.put("branchGSTIn", "");
						}
					}
					if (transaction.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
						if (transaction.getPaidInvoiceRefNumber() != null) {
							List<Transaction> findByTxnReference = Transaction.findByTxnReference(entityManager,
									user.getOrganization().getId(), transaction.getPaidInvoiceRefNumber());
							if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
								Transaction transactionRef = findByTxnReference.get(0);
								if (transactionRef != null && transactionRef.getInvoiceNumber() != null) {
									map.put("invoiceRef", transactionRef.getInvoiceNumber());
								}
							}
						}
						if (transaction.getTransactionBranch() != null) {
							DigitalSignatureBranchWise digitalSignData = DigitalSignatureBranchWise.findByOrgAndBranch(
									entityManager, user.getOrganization().getId(),
									transaction.getTransactionBranch().getId());
							if (digitalSignData != null && digitalSignData.getPersonName() != null) {
								map.put("digitalSignContentRecPayFrmCus", digitalSignData.getPersonName());
							}
						}
					} else if (transaction.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER) {
						if (transaction.getPaidInvoiceRefNumber() != null) {
							List<Transaction> findByTxnReference = Transaction.findByTxnReference(entityManager,
									user.getOrganization().getId(), transaction.getPaidInvoiceRefNumber());
							if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
								Transaction transactionRef = findByTxnReference.get(0);
								TransactionInvoice invoiceLog = TransactionInvoice.findByTransactionID(entityManager,
										user.getOrganization().getId(), transactionRef.getId());
								if (invoiceLog.getInvoiceNumber() != null)
									map.put("invoiceRef", invoiceLog.getInvRefNumber());
								else
									map.put("invoiceRef", "");
							}
						}
					} else {
						map.put("invoiceRef", "");
					}

					// Receipt Serial Number
					if (transaction.getInvoiceNumber() != null)
						map.put("receiptSerialNumber", transaction.getInvoiceNumber());
				} else {
					map.put("receiptSerialNumber", "");
				}

				// Date

				if (transaction.getTransactionDate() != null) {
					map.put("receiptDate", IdosConstants.IDOSDF.format(transaction.getTransactionDate()));
				} else {
					map.put("receiptDate", "");
				}
				if (transaction.getTransactionVendorCustomer() != null) {
					if (transaction.getTransactionVendorCustomer().getName() != null) {
						map.put("customerName", transaction.getTransactionVendorCustomer().getName());
					} else {
						map.put("customerName", "");
					}

					if (transaction.getTransactionVendorCustomer().getGstin() != null) {
						map.put("customerGSTIn", transaction.getTransactionVendorCustomer().getGstin());
					} else {
						map.put("customerGSTIn", "");
					}

					if (transaction.getTransactionVendorCustomer().getAddress() != null) {
						map.put("customerAddress", transaction.getTransactionVendorCustomer().getAddress());
					} else {
						map.put("customerAddress", "");
					}

					if (transaction.getTransactionVendorCustomer().getGstin() != null
							&& transaction.getTransactionVendorCustomer().getGstin().length() > 1) {
						String state = IdosConstants.STATE_CODE_MAPPING
								.get(transaction.getTransactionVendorCustomer().getGstin().substring(0, 2));
						map.put("placeOfSuppy", state);
						map.put("destinationStateCode",
								transaction.getTransactionVendorCustomer().getGstin().subSequence(0, 2));
					} else {
						map.put("destinationStateCode", "");
					}

					StringBuilder contactAndEmail = new StringBuilder();
					if (transaction.getTransactionVendorCustomer().getPhone() != null) {
						contactAndEmail.append(transaction.getTransactionVendorCustomer().getPhone());
					}

					if (transaction.getTransactionVendorCustomer().getEmail() != null) {
						if (contactAndEmail.length() > 0) {
							contactAndEmail.append(", ");
						}
						contactAndEmail.append(transaction.getTransactionVendorCustomer().getEmail());
					}
					map.put("customerContactAndEmail", contactAndEmail.toString());
				}

				if (transaction.getNetAmount() != null) {
					map.put("netAmount", IdosConstants.decimalFormat.format(transaction.getNetAmount()));
				} else {
					map.put("netAmount", "");
				}
				// IN WORDS
				if (transaction.getNetAmount() != null) {
					map.put("totalAmtInWords",
							NumberToWordsInt.convert(transaction.getNetAmount().longValue()) + " Only.");
				} else {
					map.put("totalAmtInWords", "");
				}
				if (transaction.getTransactionPurpose() != null
						&& transaction.getTransactionPurpose().getId() != null) {
					if (transaction.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
						map.put("fromOrTo", "From");
						map.put("receiptHeading", "Receipt Voucher");
					} else if (transaction.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER) {
						map.put("fromOrTo", "To");
						map.put("receiptHeading", "Payment Voucher");
					} else if (transaction.getTransactionPurpose()
							.getId() == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
						map.put("fromOrTo", "To");
						map.put("receiptHeading", "Advance Payment Voucher");
					}
				}

				if (transaction.getTransactionPurpose() != null
						&& (transaction.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER
								|| transaction.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER)) {
					if (transaction.getReceiptDetailsType() != null) {
						if (transaction.getReceiptDetailsType() == 1) {
							map.put("payMode", "CASH");
						} else if (transaction.getReceiptDetailsType() == 2) {
							map.put("payMode", "BANK");
							if (transaction.getInstrumentDate() != null) {
								map.put("instDate", transaction.getInstrumentDate());
							}
							if (transaction.getInstrumentNumber() != null) {
								map.put("instNo", transaction.getInstrumentNumber());
							}
						}
					}
				}
			}
		}

		return map;
	}

	@Override
	public ReceiptAdvanceModal generateReceiptContent(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception {
		Transaction transaction = null;
		ReceiptAdvanceModal receiptAdvanceModal = new ReceiptAdvanceModal();
		String transactionId = json.findValue("entityTxnId") != null ? json.findValue("entityTxnId").asText() : null;
		if (transactionId != null) {
			transaction = Transaction.findById(Long.parseLong(transactionId));
			receiptAdvanceModal.setSrNo("1");
			if (transaction.getTransactionPurpose() != null) {
				if (transaction.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
					receiptAdvanceModal.setDescription("Receipt");
					receiptAdvanceModal.setReceivedOrPaid("Received By");
				} else if (transaction.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER) {
					receiptAdvanceModal.setDescription("Payment");
					receiptAdvanceModal.setReceivedOrPaid("Paid By");
				} else if (transaction.getTransactionPurpose()
						.getId() == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
					receiptAdvanceModal.setDescription("Advance Payment");
					receiptAdvanceModal.setReceivedOrPaid("Paid By");
				}
			}
			if (transaction.getReceiptDetailsType() != null) {
				if (transaction.getReceiptDetailsType() == 1) {
					receiptAdvanceModal.setType("CASH");
					receiptAdvanceModal.setInstrumentDate("");
					receiptAdvanceModal.setInstrumentNo("");

				} else if (transaction.getReceiptDetailsType() == 2) {
					receiptAdvanceModal.setType("BANK");
					if (transaction.getInstrumentDate() != null) {
						receiptAdvanceModal.setInstrumentDate(transaction.getInstrumentDate());
					} else {
						receiptAdvanceModal.setInstrumentDate("");
					}
					if (transaction.getInstrumentNumber() != null) {
						receiptAdvanceModal.setInstrumentNo(transaction.getInstrumentNumber());
					} else {
						receiptAdvanceModal.setInstrumentNo("");
					}
				}
			} else {
				receiptAdvanceModal.setType("");
				receiptAdvanceModal.setInstrumentDate("");
				receiptAdvanceModal.setInstrumentNo("");
			}
			receiptAdvanceModal.setReceivedBy(user.getFullName());
			if (transaction.getNetAmount() != null) {
				receiptAdvanceModal.setAmount(IdosConstants.decimalFormat.format(transaction.getNetAmount()));
			}
		}
		return receiptAdvanceModal;
	}

	@Override
	public List<ReceiptAdvanceModal> generateRefundPaymentReceiptData(Organization org, Transaction transaction,
			EntityManager entityManager) {
		List<ReceiptAdvanceModal> receiptAdvanceModalList = new ArrayList<ReceiptAdvanceModal>();
		Map<String, Object> criterias = new HashMap<String, Object>();
		if (transaction != null) {
			int counter = 1;
			double totalNetAmt = 0;
			criterias.put("transaction.id", transaction.getId());
			criterias.put("presentStatus", 1);
			List<TransactionItems> listTransactionItems = genericDao.findByCriteria(TransactionItems.class, criterias,
					entityManager);
			if (listTransactionItems != null && listTransactionItems.size() > 0) {
				for (TransactionItems txnItemrow : listTransactionItems) {
					ReceiptAdvanceModal receiptAdvanceModal = new ReceiptAdvanceModal();
					receiptAdvanceModal.setSrNo(String.valueOf(counter));
					counter++;
					receiptAdvanceModal.setDescription("Receipt");
					totalNetAmt = totalNetAmt + txnItemrow.getNetAmountReturned();
					receiptAdvanceModal
							.setAmount(IdosConstants.decimalFormat.format(txnItemrow.getNetAmountReturned()));
					if (transaction.getReceiptDetailsType() != null) {
						if (transaction.getReceiptDetailsType() == 1) {
							receiptAdvanceModal.setType("CASH");
							receiptAdvanceModal.setInstrumentDate("");
							receiptAdvanceModal.setInstrumentNo("");

						} else if (transaction.getReceiptDetailsType() == 2) {
							receiptAdvanceModal.setType("BANK");
							if (transaction.getInstrumentDate() != null) {
								receiptAdvanceModal.setInstrumentDate(transaction.getInstrumentDate());
							} else {
								receiptAdvanceModal.setInstrumentDate("");
							}
							if (transaction.getInstrumentNumber() != null) {
								receiptAdvanceModal.setInstrumentNo(transaction.getInstrumentNumber());
							} else {
								receiptAdvanceModal.setInstrumentNo("");
							}
						}
					} else {
						receiptAdvanceModal.setType("");
						receiptAdvanceModal.setInstrumentDate("");
						receiptAdvanceModal.setInstrumentNo("");
					}

					if (txnItemrow.getTransactionRefNumber() != null) {
						// get corresponding REceive payment from customer transaction, for which this
						// refund is issued
						List<Transaction> findByTxnReference = Transaction.findByTxnReference(entityManager,
								transaction.getTransactionBranchOrganization().getId(),
								txnItemrow.getTransactionRefNumber());
						if (findByTxnReference != null && !findByTxnReference.isEmpty()) {
							Transaction receivePaymentOriginalTran = findByTxnReference.get(0);
							if (receivePaymentOriginalTran != null) {
								String invoiceNumber = receivePaymentOriginalTran.getInvoiceNumber();
								Date invDate = receivePaymentOriginalTran.getTransactionDate();
								receiptAdvanceModal.setInvoiceNumber(invoiceNumber);
								if (invDate != null) {
									receiptAdvanceModal.setInvoiceDate(idosdf1.format(invDate));
								}
							}
						}
					}
					// receiptAdvanceModal.setTotalNetAmount(IdosConstants.decimalFormat.format(totalNetAmt));
					receiptAdvanceModalList.add(receiptAdvanceModal);
				}
			}
			if (!receiptAdvanceModalList.isEmpty()) {
				ReceiptAdvanceModal recAdvModel = receiptAdvanceModalList.get(receiptAdvanceModalList.size() - 1);
				recAdvModel.setTotalNetAmount(IdosConstants.decimalFormat.format(totalNetAmt));
				recAdvModel.setTotalAmtInWords(
						NumberToWordsInt.convert(transaction.getNetAmount().longValue()) + " Only.");
			}

		}
		return receiptAdvanceModalList;
	}

	public List<ReceiptAdvanceModal> generateMultiInvoiceData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) {
		String txnItemsQuery = "select obj from TransactionItems obj where obj.organization.id =?1 and obj.transaction.id=?2 and obj.presentStatus=1";

		List<TransactionItems> txnItemsList = null;
		Transaction transaction = null;
		List<ReceiptAdvanceModal> receiptAdvanceModalList = new ArrayList<ReceiptAdvanceModal>();
		String transactionId = json.findValue("entityTxnId") != null ? json.findValue("entityTxnId").asText() : null;

		if (transactionId != null) {
			ArrayList<Object> inparam1 = new ArrayList<Object>(3);
			inparam1.add(user.getOrganization().getId());
			inparam1.add(Long.parseLong(transactionId));
			txnItemsList = genericDao.queryWithParamsName(txnItemsQuery.toString(), entityManager, inparam1);
			Integer counter = 1;
			transaction = Transaction.findById(Long.parseLong(transactionId));

			for (TransactionItems item : txnItemsList) {
				ReceiptAdvanceModal receiptAdvanceModal = new ReceiptAdvanceModal();
				receiptAdvanceModal.setSrNo(counter.toString());
				if (item.getTransactionRefNumber().equals("-1"))
					receiptAdvanceModal.setInvoiceNumber("");
				else
					receiptAdvanceModal.setInvoiceNumber(item.getTransactionRefNumber());
				receiptAdvanceModal.setReceivedBy(user.getFullName());
				if (item.getNetAmount() != null)
					receiptAdvanceModal.setAmount(IdosConstants.decimalFormat.format(item.getNetAmount()).toString());
				if (item.getDiscountAmount() != null)
					receiptAdvanceModal.setDiscountAllowedOrReceived(
							IdosConstants.decimalFormat.format(item.getDiscountAmount()).toString());
				counter++;
				receiptAdvanceModalList.add(receiptAdvanceModal);
				if (transaction.getNetAmount() != null) {
					receiptAdvanceModal.setTotalNetAmount(
							IdosConstants.decimalFormat.format(transaction.getNetAmount()).toString());
					receiptAdvanceModal.setTotalAmtInWords(
							NumberToWordsInt.convert(transaction.getNetAmount().longValue()) + " Only.");
				}
			}
		}
		return receiptAdvanceModalList;
	}
}
