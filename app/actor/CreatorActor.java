package actor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.idos.util.IdosConstants;

import actor.AdminActor.NotificationMessage;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import play.libs.Akka;
import akka.actor.*;
import play.libs.Json;

public class CreatorActor extends AbstractActor {

	public static Map<String, ActorRef> expenseregistrered = new HashMap<String, ActorRef>();

	public static ActorRef actor;

	public static Props props(ActorRef out) {
		return Props.create(CreatorActor.class, out);
	}

	// Add this overloaded method with a default parameter
	public static Props props() {
		return Props.create(CreatorActor.class, ActorRef.noSender());
	}

	public CreatorActor(ActorRef out) {
		actor = out;
		System.out.println("Actor(Creater) Contructor ============" + actor);
	}

	public static void register(final String email, ActorRef out) {
		try {
			ActorRef value = expenseregistrered.get(email);
			if (value != null) {
				// log.log(Level.INFO, "client email is removed as key if found and again registered on as
				// key");
				expenseregistrered.remove(email);
				value.tell("close", ActorRef.noSender()); // Close the existing WebSocket connection
			}
			if (actor != null)
				actor.tell(new LoginMessage(email, out).toString(), ActorRef.noSender());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public static void unregister(String email) throws Exception {
		System.out.println("Actor(Creater) (unreg)============" + actor);
		LogoutMessage logOut = new LogoutMessage();
		logOut.setEmail(email);
		actor.tell(logOut.getEmail(), ActorRef.noSender());
	}

	public static void add(final Long id, final String expenseCreator, final String expenseItem,
			final String expenseVendor, final Integer expenseQty, final Double totalAmount,
			final String expenseActionDate, final String expenseStatus, final String expenseDoc,
			final String txnRemarks) {
		actor.tell(new AddExpenseMessage(id, expenseCreator, expenseItem, expenseVendor, expenseQty, totalAmount,
				expenseActionDate, expenseStatus, expenseDoc, txnRemarks).toString(), ActorRef.noSender());
	}

	public static void action(final Long id, final String expenseStatus, final String nextApprover,
			final String fileUrl, final String txnRemarks) {
		actor.tell(new ActionMessage(id, expenseStatus, nextApprover, fileUrl, txnRemarks).toString(),
				ActorRef.noSender());
	}

	public static void addTxn(final Long id, final String branchName, final String projectName, final String itemName,
			final String itemParentName, final String customerVendorName, final String transactionPurpose,
			final String txnDate, final String invoiceDateLabel, final String invoiceDate, final String paymentMode,
			final Double noOfUnits, final Double perUnitPrice, final Double grossAmt, final Double netAmount,
			final String netAmtDesc, final String outstandings, final String status, final String createdBy,
			final String approverLabel, final String approverEmail, final String txnDocument, final String txnRemarks,
			final String debitCredit, final Map<String, ActorRef> orgexpenseregistrered,
			final String txnSpecialStatus, final Double frieghtCharges, final String poReference,
			final String txnInstrumentNumber, final String txnInstrumentDate, final Long transactionPurposeID,
			final String serialNumber, final String txnRefNo, final Integer typeOfSupply) {
		actor.tell(
				new AddTransactionMessage(id, branchName, projectName, itemName, itemParentName, customerVendorName,
						transactionPurpose, txnDate, invoiceDateLabel, invoiceDate, paymentMode, noOfUnits,
						perUnitPrice,
						grossAmt, netAmount, netAmtDesc, outstandings, status, createdBy, approverLabel,
						approverEmail,
						txnDocument, txnRemarks, debitCredit, orgexpenseregistrered, txnSpecialStatus,
						frieghtCharges,
						poReference, txnInstrumentNumber, txnInstrumentDate, transactionPurposeID, serialNumber,
						txnRefNo,
						typeOfSupply).toString(),
				ActorRef.noSender());
	}

	public static void addActionTxn(final Long id, final String branchName, final String projectName,
			final String itemName, final String itemParentName, final String budgetAllocated,
			final String budgetAllocatedAmt, final String budgetAvailable, final String budgetAvailableAmt,
			final String customerVendorName, final String transactionPurpose, final String txnDate,
			final String invoiceDateLabel, final String invoiceDate, final String paymentMode, final Double noOfUnits,
			final Double perUnitPrice, final Double grossAmt, final Double netAmount, final String netAmtDesc,
			final String outstandings, final String status, final String createdBy, final String approverLabel,
			final String approverEmail, final String txnDocument, final String txnRemarks, final String debitCredit,
			final String approverEmails, final String additionalApprovalEmails, final String selectedAdditionalApproval,
			final Map<String, ActorRef> orgexpenseregistrered, final String txnSpecialStatus,
			final Double frieghtCharges, final String poReference, final String txnInstrumentNumber,
			final String txnInstrumentDate, final Long transactionPurposeID, final String txnRemarksPrivate,
			final String serialNumber, int txnIdentifier, String txnRefNo, final Long branchId,
			final Double totalDeductions, final int workingDays, final int typeOfSupply) {
		actor.tell(new AddTransactionMessage(id, branchName, projectName, itemName, itemParentName, budgetAllocated,
				budgetAllocatedAmt, budgetAvailable, budgetAvailableAmt, customerVendorName,
				transactionPurpose,
				txnDate, invoiceDateLabel, invoiceDate, paymentMode, noOfUnits, perUnitPrice, grossAmt,
				netAmount,
				netAmtDesc, outstandings, status, createdBy, approverLabel, approverEmail, txnDocument,
				txnRemarks,
				debitCredit, approverEmails, additionalApprovalEmails, selectedAdditionalApproval,
				orgexpenseregistrered, txnSpecialStatus, frieghtCharges, poReference, txnInstrumentNumber,
				txnInstrumentDate, transactionPurposeID, txnRemarksPrivate, serialNumber, txnIdentifier,
				txnRefNo,
				branchId, totalDeductions, workingDays, typeOfSupply).toString(), ActorRef.noSender());
	}

	public static void requestHiring(final Long id, final String projectNumber, final String projectTitle,
			final String requester, final String requestType, final String position, final String status,
			final String remarks, final String document, final String approverEmailList) {
		actor.tell(
				new RequestHiringMessage(id, projectNumber, projectTitle, requester, requestType, position, status,
						remarks, document, approverEmailList).toString(),
				ActorRef.noSender());
	}

	public static void addProcurementRequest(final String procurementStatus) {
		actor.tell(new AddProcurementMessage(procurementStatus).toString(), ActorRef.noSender());
	}

	public static void actionHiring(final Long id, final String status, final String remarks, final String document,
			final String requester, final String approverEmailList) {
		actor.tell(new ActionHiring(id, status, remarks, document, requester, approverEmailList).toString(),
				ActorRef.noSender());
	}

	public static void addClaimTxn(final Long id, final String branchName, final String projectName,
			final String txnQuestionName, final String txnOrgnName, final String claimtravelType,
			final String claimnoOfPlacesToVisit, final String claimplacesSelectedOrEntered,
			final String claimtypeOfCity, final String claimappropriateDiatance, final String claimtotalDays,
			final String claimtravelDetailedConfDescription, final Double claimexistingAdvance,
			final String claimuserAdvanveEligibility, final Double claimadjustedAdvance,
			final Double claimenteredAdvance,
			final Double claimtotalAdvance, final String claimpurposeOfVisit, final String claimtxnRemarks,
			final String claimsupportingDoc, final String debitCredit, final String claimTxnStatus,
			final String approverEmails, final String additionalApprovalEmails,
			final String selectedAdditionalApproval, final String creatorLabel, final String createdBy,
			final String transactionDate, final String approverLabel, final String approvedBy,
			final String accountantEmails, final String accountedLabel, final String accountedBy,
			final Map<String, ActorRef> orgclaimregistrered, final String txnSpecialStatus,
			final String paymentMode, final String instrumentNumber, final String instrumentDate,
			final String userEmail, String claimTxnRefNo) {
		actor.tell(new AddClaimTransactionMessage(id, branchName, projectName, txnQuestionName, txnOrgnName,
				claimtravelType, claimnoOfPlacesToVisit, claimplacesSelectedOrEntered, claimtypeOfCity,
				claimappropriateDiatance, claimtotalDays,
				claimtravelDetailedConfDescription,
				claimexistingAdvance, claimuserAdvanveEligibility, claimadjustedAdvance,
				claimenteredAdvance,
				claimtotalAdvance, claimpurposeOfVisit,
				claimtxnRemarks, claimsupportingDoc, debitCredit, claimTxnStatus, approverEmails,
				additionalApprovalEmails, selectedAdditionalApproval, creatorLabel,
				createdBy, transactionDate, approverLabel, approvedBy, accountantEmails, accountedLabel,
				accountedBy,
				orgclaimregistrered,
				txnSpecialStatus, paymentMode, instrumentNumber, instrumentDate, userEmail, claimTxnRefNo)
				.toString(),
				ActorRef.noSender());
	}

	public static void addClaimSettlementTxn(final Long id, final String branchName, final String projectName,
			final String txnQuestionName, final String txnOrgnName, final String travelType,
			final String noOfPlacesToVisit, final String placesSelectedOrEntered, final String typeOfCity,
			final String appropriateDiatance, final String totalDays, final String travelDetailedConfDescription,
			final String existingClaimsCurrentSettlementDetails, final String claimuserAdvanveEligibility,
			final String userExpenditureOnThisTxn, final Double netSettlementAmount, final Double dueSettlementAmount,
			final Double requiredSettlement, final Double returnSettlement, final String purposeOfVisit,
			final String claimdebitCredit, final String claimTxnStatus, final String creatorLabel,
			final String createdBy, final String accountedLabel, final String accountedBy, final String transactionDate,
			final String paymentMode, final String accountantEmails, final String claimTxnRemarks,
			final String supportingDoc, final Map<String, ActorRef> orgclaimregistrered,
			final String instrumentNumber, final String instrumentDate, final String userEmail,
			final Double amountReturnInCaseOfDueToCompany, String claimTxnRefNo) {
		actor.tell(
				new AddClaimTransactionSettlementMessage(id, branchName, projectName, txnQuestionName, txnOrgnName,
						travelType, noOfPlacesToVisit, placesSelectedOrEntered, typeOfCity, appropriateDiatance,
						totalDays,
						travelDetailedConfDescription, existingClaimsCurrentSettlementDetails,
						claimuserAdvanveEligibility,
						userExpenditureOnThisTxn, netSettlementAmount, dueSettlementAmount, requiredSettlement,
						returnSettlement, purposeOfVisit, claimdebitCredit, claimTxnStatus, creatorLabel, createdBy,
						accountedLabel, accountedBy, transactionDate, paymentMode, accountantEmails,
						claimTxnRemarks,
						supportingDoc, orgclaimregistrered, instrumentNumber, instrumentDate, userEmail,
						amountReturnInCaseOfDueToCompany, claimTxnRefNo).toString(),
				ActorRef.noSender());
	}

	public static void addAdvanceExpenseTxn(final Long id, final String branchName, final String projectName,
			final String txnQuestionName, final String txnOrgnName, final String claimtravelType,
			final String claimnoOfPlacesToVisit, final String claimplacesSelectedOrEntered,
			final String claimtypeOfCity, final String claimappropriateDiatance, final String claimtotalDays,
			final String claimtravelDetailedConfDescription, final Double claimexistingAdvance,
			final String claimuserAdvanveEligibility, final Double claimadjustedAdvance,
			final Double claimenteredAdvance,
			final Double claimtotalAdvance, final String claimpurposeOfVisit, final String claimtxnRemarks,
			final String claimsupportingDoc, final String debitCredit, final String claimTxnStatus,
			final String approverEmails, final String additionalApprovalEmails,
			final String selectedAdditionalApproval, final String creatorLabel, final String createdBy,
			final String transactionDate, final String approverLabel, final String approvedBy,
			final String accountantEmails, final String accountedLabel, final String accountedBy,
			final Map<String, ActorRef> orgclaimregistrered, final String txnSpecialStatus,
			final String paymentMode,
			final Double expenseAdvanceTotalAdvanceAmount, final String expenseAdvancepurposeOfExpenseAdvance,
			final String itemName, final String itemParticularName, final String parentSpecificName,
			final Double dueFromCompany, final Double dueToCompany, final Double amountReturnInCaseOfDueToCompany,
			final String instrumentNumber, final String instrumentDate, final String userEmail, String claimTxnRefNo) {
		actor.tell(new AddClaimTransactionMessage(id, branchName,
				projectName, txnQuestionName, txnOrgnName,
				claimtravelType, claimnoOfPlacesToVisit, claimplacesSelectedOrEntered, claimtypeOfCity,
				claimappropriateDiatance, claimtotalDays, claimtravelDetailedConfDescription, claimexistingAdvance,
				claimuserAdvanveEligibility, claimadjustedAdvance, claimenteredAdvance, claimtotalAdvance,
				claimpurposeOfVisit, claimtxnRemarks, claimsupportingDoc, debitCredit, claimTxnStatus,
				approverEmails,
				additionalApprovalEmails, selectedAdditionalApproval, creatorLabel, createdBy, transactionDate,
				approverLabel, approvedBy, accountantEmails, accountedLabel, accountedBy, orgclaimregistrered,
				txnSpecialStatus, paymentMode, expenseAdvanceTotalAdvanceAmount,
				expenseAdvancepurposeOfExpenseAdvance,
				itemName, itemParticularName, parentSpecificName, dueFromCompany, dueToCompany,
				amountReturnInCaseOfDueToCompany, instrumentNumber, instrumentDate, userEmail, claimTxnRefNo)
				.toString(),
				ActorRef.noSender());
	}

	public static void chat(final String from, final String to, final String message, final String name) {
		actor.tell(new ChatMessage(from, to, message, name).toString(), ActorRef.noSender());
	}

	public static void online(final boolean result, final ArrayNode node) {
		actor.tell(new OnlineUsersMessage(result, node).toString(), ActorRef.noSender());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(LoginMessage.class, registration -> {
					expenseregistrered.put(registration.email, registration.channel);
				})
				.match(AddExpenseMessage.class, add -> {
					Object[] keyArray = expenseregistrered.keySet().toArray();
					for (ActorRef channel : expenseregistrered.values()) {
						ObjectNode event = Json.newObject();
						event.put("txnType", "sellExpenseTxn");
						event.put("id", add.id);
						event.put("expenseCreator", add.expenseCreator);
						event.put("expenseItem", add.expenseItem);
						event.put("expenseVendor", add.expenseVendor);
						event.put("expenseQty", add.expenseQty);
						event.put("totalAmount", add.totalAmount);
						event.put("expenseActionDate", add.expenseActionDate);
						event.put("expenseStatus", add.expenseStatus);
						event.put("expenseDoc", add.expenseDoc);
						event.put("expenseTxnRemarks", add.txnRemarks);
						event.put("workflowAction", "addExpense");
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(AddTransactionMessage.class, add -> {
					for (ActorRef channel : add.orgexpenseregistrered.values()) {
						// log.log(Level.INFO, channel);
						ObjectNode event = Json.newObject();
						if (!add.transactionPurpose.equals("Make Provision/Journal Entry")) {
							event.put("txnType", "sellExpenseTxn");
						} else if (add.transactionPurpose.equals("Make Provision/Journal Entry")) {
							event.put("txnType", "expenseAssetsLiabilitiesProvisionTxn");
						} else if (add.transactionPurpose.equalsIgnoreCase("process payroll")) {
							event.put("txnType", "processPayroll");
						}

						event.put("id", add.id);
						event.put("branchName", add.branchName);
						event.put("projectName", add.projectName);
						event.put("itemName", add.itemName);
						event.put("itemParentName", add.itemParentName);
						event.put("budgetAllocated", add.budgetAllocated);
						event.put("budgetAllocatedAmt", add.budgetAllocatedAmt);
						event.put("budgetAvailable", add.budgetAvailable);
						event.put("budgetAvailableAmt", add.budgetAvailableAmt);
						event.put("customerVendorName", add.customerVendorName);
						event.put("transactionPurpose", add.transactionPurpose);
						event.put("txnDate", add.txnDate);
						event.put("invoiceDateLabel", add.invoiceDateLabel);
						event.put("invoiceDate", add.invoiceDate);
						event.put("paymentMode", add.paymentMode);
						event.put("noOfUnit", add.noOfUnit);
						try {
							if (add.unitPrice != null) {
								event.put("unitPrice", IdosConstants.decimalFormat.format(add.unitPrice));
							} else {
								event.put("unitPrice", "");
							}
							if (add.grossAmount != null) {
								event.put("grossAmount", IdosConstants.decimalFormat.format(add.grossAmount));
							} else {
								event.put("grossAmount", "");
							}
							if (add.netAmount != null) {
								event.put("netAmount", IdosConstants.decimalFormat.format(add.netAmount));
							} else {
								event.put("netAmount", "");
							}
						} catch (NumberFormatException e) {
							System.out.println("Error : " + e.getMessage());
						}
						event.put("netAmtDesc", add.netAmtDesc);
						event.put("outstandings", add.outStandings);
						event.put("status", add.status);
						event.put("createdBy", add.createdBy);
						event.put("approverLabel", add.approverLabel);
						event.put("approverEmail", add.approverEmail);
						event.put("txnDocument", add.txnDocument);
						event.put("txnRemarks", add.txnRemarks);
						event.put("debitCredit", add.debitCredit);
						event.put("approverEmails", add.approverEmails);
						event.put("additionalApprovarUsers", add.additionalApprovalEmails);
						event.put("selectedAdditionalApproval", add.selectedAdditionalApproval);
						event.put("txnSpecialStatus", add.txnSpecialStatus);
						event.put("frieghtCharges", add.frieghtCharges);
						event.put("poReference", add.poReference);
						event.put("instrumentNumber", add.instrumentNumber);
						event.put("instrumentDate", add.instrumentDate);
						event.put("transactionPurposeID", add.transactionPurposeID);
						event.put("remarksPrivate", add.remarksPrivate);
						event.put("invoiceNumber", add.invoiceNumber);
						event.put("txnRefNo", add.txnRefNo);
						event.put("branchId", add.branchId);
						event.put("totalDeductions", add.totalDeductions);
						event.put("workingDays", add.workingsDays);
						event.put("typeOfSupply", add.typeOfSupply);
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(AddClaimTransactionMessage.class, add -> {
					for (ActorRef channel : add.orgclaimregistrered.values()) {
						ObjectNode event = Json.newObject();
						if (add.txnQuestionName.equals("Request For Travel Advance")) {
							event.put("txnType", "claimTxn");
						}
						if (add.txnQuestionName.equals("Request Advance For Expense")) {
							event.put("txnType", "expenseAdvanceTxn");
						}
						if (add.txnQuestionName.equals("Settle Advance For Expense")) {
							event.put("txnType", "expAdvanceSettlementTxn");
						}
						if (add.txnQuestionName.equals("Request For Expense Reimbursement")) {
							event.put("txnType", "expReimbursementTxn");
						}
						if (add.txnQuestionName.equals("Settle Travel Advance")) {
							event.put("txnType", "claimSettlementTxn");
						}
						event.put("id", add.id);
						event.put("branchName", add.branchName);
						event.put("projectName", add.projectName);
						event.put("txnQuestionName", add.txnQuestionName);
						event.put("txnOrgnName", add.txnOrgnName);
						event.put("claimtravelType", add.claimtravelType);
						event.put("claimnoOfPlacesToVisit", add.claimnoOfPlacesToVisit);
						event.put("claimplacesSelectedOrEntered", add.claimplacesSelectedOrEntered);
						event.put("claimtypeOfCity", add.claimtypeOfCity);
						event.put("claimappropriateDiatance", add.claimappropriateDiatance);
						event.put("claimtotalDays", add.claimtotalDays);
						event.put("claimtravelDetailedConfDescription", add.claimtravelDetailedConfDescription);
						event.put("claimexistingAdvance", IdosConstants.decimalFormat.format(add.claimexistingAdvance));
						event.put("claimuserAdvanveEligibility", add.claimuserAdvanveEligibility);
						try {
							event.put("claimexistingAdvance",
									IdosConstants.decimalFormat.format(add.claimexistingAdvance));
							event.put("claimadjustedAdvance",
									IdosConstants.decimalFormat.format(add.claimadjustedAdvance));
							event.put("claimenteredAdvance",
									IdosConstants.decimalFormat.format(add.claimenteredAdvance));
							event.put("claimtotalAdvance", IdosConstants.decimalFormat.format(add.claimtotalAdvance));
						} catch (NumberFormatException e) {
							System.out.println("Error : " + e.getMessage());
						}

						event.put("claimpurposeOfVisit", add.claimpurposeOfVisit);
						event.put("claimtxnRemarks", add.claimtxnRemarks);
						event.put("claimsupportingDoc", add.claimsupportingDoc);
						event.put("debitCredit", add.debitCredit);
						event.put("claimTxnStatus", add.claimTxnStatus);
						event.put("approverEmails", add.approverEmails);
						event.put("additionalApprovarUsers", add.additionalApprovalEmails);
						event.put("selectedAdditionalApproval", add.selectedAdditionalApproval);
						event.put("creatorLabel", add.creatorLabel);
						event.put("createdBy", add.createdBy);
						event.put("transactionDate", add.transactionDate);
						event.put("approverLabel", add.approverLabel);
						event.put("approvedBy", add.approvedBy);
						event.put("accountantEmails", add.accountantEmails);
						event.put("accountedLabel", add.accountedLabel);
						event.put("accountedBy", add.accountedBy);
						event.put("txnSpecialStatus", add.txnSpecialStatus);
						event.put("paymentMode", add.paymentMode);
						event.put("expenseAdvanceTotalAdvanceAmount", add.expenseAdvanceTotalAdvanceAmount);
						event.put("expenseAdvancepurposeOfExpenseAdvance", add.expenseAdvancepurposeOfExpenseAdvance);
						event.put("itemName", add.itemName);
						event.put("itemParticularName", add.itemParticularName);
						event.put("parentSpecificName", add.parentSpecificName);
						event.put("parentSpecificName", add.parentSpecificName);
						event.put("parentSpecificName", add.parentSpecificName);
						event.put("parentSpecificName", add.parentSpecificName);
						event.put("dueFromCompany", add.dueFromCompany);
						event.put("dueToCompany", add.dueToCompany);
						event.put("amountReturnInCaseOfDueToCompany", add.amountReturnInCaseOfDueToCompany);
						event.put("instrumentNumber", add.instrumentNumber);
						event.put("instrumentDate", add.instrumentDate);
						event.put("claimTxnRefNo", add.claimTxnRefNo);
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(AddClaimTransactionSettlementMessage.class, add -> {
					for (ActorRef channel : add.orgclaimregistrered.values()) {
						ObjectNode event = Json.newObject();
						event.put("txnType", "claimSettlementTxn");
						event.put("id", add.id);
						event.put("branchName", add.branchName);
						event.put("projectName", add.projectName);
						event.put("txnQuestionName", add.txnQuestionName);
						event.put("txnOrgnName", add.txnOrgnName);
						event.put("claimtravelType", add.travelType);
						event.put("claimnoOfPlacesToVisit", add.noOfPlacesToVisit);
						event.put("claimplacesSelectedOrEntered", add.placesSelectedOrEntered);
						event.put("claimtypeOfCity", add.typeOfCity);
						event.put("claimappropriateDiatance", add.appropriateDiatance);
						event.put("claimtotalDays", add.totalDays);
						event.put("travelDetailedConfDescription", add.travelDetailedConfDescription);
						event.put("existingClaimsCurrentSettlementDetails", add.existingClaimsCurrentSettlementDetails);
						event.put("claimuserAdvanveEligibility", add.claimuserAdvanveEligibility);
						event.put("userExpenditureOnThisTxn", add.userExpenditureOnThisTxn);
						try {
							event.put("netSettlementAmount",
									IdosConstants.decimalFormat.format(add.netSettlementAmount));
							event.put("dueSettlementAmount",
									IdosConstants.decimalFormat.format(add.dueSettlementAmount));
							event.put("requiredSettlement", IdosConstants.decimalFormat.format(add.requiredSettlement));
							event.put("returnSettlement", IdosConstants.decimalFormat.format(add.returnSettlement));
						} catch (Exception e) {
							System.out.println("Error : " + e.getMessage());
						}
						event.put("purposeOfVisit", add.purposeOfVisit);
						event.put("claimdebitCredit", add.claimdebitCredit);
						event.put("claimTxnStatus", add.claimTxnStatus);
						event.put("creatorLabel", add.creatorLabel);
						event.put("createdBy", add.createdBy);
						event.put("accountedLabel", add.accountedLabel);
						event.put("accountedBy", add.accountedBy);
						event.put("transactionDate", add.transactionDate);
						event.put("paymentMode", add.paymentMode);
						event.put("claimtxnRemarks", add.claimtxnRemarks);
						event.put("claimsupportingDoc", add.claimsupportingDoc);
						event.put("accountantEmails", add.accountantEmails);
						event.put("instrumentNumber", add.instrumentNumber);
						event.put("instrumentDate", add.instrumentDate);
						event.put("useremail", add.userEmail);
						event.put("amountReturnInCaseOfDueToCompany", add.amountReturnInCaseOfDueToCompany);
						event.put("claimTxnRefNo", add.claimTxnRefNo);
						// log.log(Level.FINE, "End " + event);
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(AddProcurementMessage.class, add -> {
					Object[] keyArray = expenseregistrered.keySet().toArray();
					for (ActorRef channel : expenseregistrered.values()) {
						ObjectNode event = Json.newObject();
						event.put("txnType", "sellExpenseTxn");
						event.put("procurementStatus", add.procurementStatus);
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(ActionMessage.class, action -> {
					Object[] keyArray = expenseregistrered.keySet().toArray();
					for (ActorRef channel : expenseregistrered.values()) {
						ObjectNode event = Json.newObject();
						event.put("txnType", "sellExpenseTxn");
						event.put("id", action.id);
						event.put("status", action.status);
						event.put("nextApprover", action.nextApprover);
						event.put("fileUrl", action.fileUrl);
						event.put("expenseTxnRemarks", action.txnRemarks);
						event.put("workflowAction", "actionExpense");
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(RequestHiringMessage.class, requestHiring -> {
					for (ActorRef channel : expenseregistrered.values()) {
						ObjectNode event = Json.newObject();
						event.put("txnType", "sellExpenseTxn");
						event.put("id", requestHiring.id);
						event.put("projectNumber", requestHiring.projectNumber);
						event.put("projectTitle", requestHiring.projectTitle);
						event.put("requester", requestHiring.requester);
						event.put("requetType", requestHiring.requestType);
						event.put("position", requestHiring.position);
						event.put("status", requestHiring.status);
						event.put("workflowAction", "requestHiring");
						event.put("remarks", requestHiring.remarks);
						event.put("document", requestHiring.document);
						event.put("approverEmailList", requestHiring.approverEmailList);
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(ActionHiring.class, actionHiring -> {
					for (ActorRef channel : expenseregistrered.values()) {
						ObjectNode event = Json.newObject();
						event.put("txnType", "sellExpenseTxn");
						event.put("id", actionHiring.id);
						event.put("status", actionHiring.status);
						event.put("workflowAction", "actionHiring");
						event.put("remarks", actionHiring.remarks);
						event.put("document", actionHiring.document);
						event.put("requester", actionHiring.requester);
						event.put("approverEmailList", actionHiring.approverEmailList);
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(LogoutMessage.class, logout -> {
					System.out.println(logout);
					expenseregistrered.remove(logout.email);
				})
				.match(ChatMessage.class, chat -> {
					ObjectNode event = Json.newObject();
					event.put("type", "chat");
					event.put("from", chat.from);
					event.put("to", chat.to);
					event.put("message", chat.message);
					event.put("name", chat.name);
					ActorRef channel = expenseregistrered.get(chat.to);
					if (null != channel) {
						channel.tell(Json.toJson(event), getSelf());
					}
				})
				.match(OnlineUsersMessage.class, online -> {
					for (ActorRef channel : expenseregistrered.values()) {
						ObjectNode event = Json.newObject();
						event.put("txnType", "onlineUsers");
						event.put("result", online.result);
						event.put("users", online.node);
						channel.tell(Json.toJson(event), getSelf());
					}
				}).build();

	}

	public static class LoginMessage {
		public String email;
		public ActorRef channel;

		public LoginMessage(String email, ActorRef channel) {
			this.email = email;
			this.channel = channel;
		}

		@Override
		public String toString() {
			return email;
		}

	}

	public static class LogoutMessage {
		public String email;

		public LogoutMessage() {
		}

		public LogoutMessage(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		@Override
		public String toString() {
			return "LogoutMessage [email=" + email + "]";
		}

	}

	public static class ActionMessage {
		public Long id;
		public String status;
		public String nextApprover;
		public String fileUrl;
		public String txnRemarks;

		public ActionMessage(Long id, String status, String nextApprover, String fileUrl, String txnRemarks) {
			this.id = id;
			this.status = status;
			this.nextApprover = nextApprover;
			this.fileUrl = fileUrl;
			this.txnRemarks = txnRemarks;
		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			event.put("txnType", "sellExpenseTxn");
			event.put("id", this.id);
			event.put("status", this.status);
			event.put("nextApprover", this.nextApprover);
			event.put("fileUrl", this.fileUrl);
			event.put("expenseTxnRemarks", this.txnRemarks);
			event.put("workflowAction", "actionExpense");
			return event.toString();
		}

	}

	public static class AddTransactionMessage {
		public Long id;
		public String branchName;
		public String projectName;
		public String itemName;
		public String itemParentName;
		public String budgetAllocated;
		public String budgetAllocatedAmt;
		public String budgetAvailable;
		public String budgetAvailableAmt;
		public String customerVendorName;
		public String transactionPurpose;
		public String txnDate;
		public String invoiceDateLabel;
		public String invoiceDate;
		public String paymentMode;
		public Double noOfUnit;
		public Double unitPrice;
		public Double grossAmount;
		public Double netAmount;
		public String netAmtDesc;
		public String outStandings;
		public String status;
		public String createdBy;
		public String approverLabel;
		public String approverEmail;
		public String txnDocument;
		public String txnRemarks;
		public String debitCredit;
		public String approverEmails;
		public String additionalApprovalEmails;
		public String selectedAdditionalApproval;
		@JsonIgnore
		public Map<String, ActorRef> orgexpenseregistrered;
		public String txnSpecialStatus;
		public Double frieghtCharges;
		public String poReference;
		public String instrumentNumber;
		public String instrumentDate;
		public Long transactionPurposeID;
		public String remarksPrivate;
		public String invoiceNumber;
		public Integer txnIdentifier;
		public String txnRefNo;
		public Long branchId;
		public Double totalDeductions;
		public Integer workingsDays;
		public Integer typeOfSupply;

		public AddTransactionMessage(Long id, String branchName, String projectName, String itemName,
				String itemParentName, String customerVendorName, String transactionPurpose, String txnDate,
				String invoiceDateLabel, String invoiceDate, String paymentMode, Double noOfUnit, Double unitPrice,
				Double grossAmount, Double netAmount, String netAmtDesc, String outStandings, String status,
				String createdBy, String approverLabel, String approverEmail, String txnDocument, String txnRemarks,
				String debitCredit, Map<String, ActorRef> orgexpenseregistrered, String txnSpecialStatus,
				Double frieghtCharges, String poReference, String instrumentNumber, String instrumentDate,
				Long transactionPurposeID, final String serialNumber, final String txnRefNo, Integer typeOfSupply) {

			this.id = id;
			this.branchName = branchName;
			this.projectName = projectName;
			this.itemName = itemName;
			this.itemParentName = itemParentName;
			this.customerVendorName = customerVendorName;
			this.transactionPurpose = transactionPurpose;
			this.txnDate = txnDate;
			this.invoiceDateLabel = invoiceDateLabel;
			this.invoiceDate = invoiceDate;
			this.paymentMode = paymentMode;
			this.noOfUnit = (noOfUnit == null ? 0 : noOfUnit);
			this.unitPrice = (unitPrice == null ? 0 : unitPrice);
			this.grossAmount = grossAmount;
			this.netAmount = netAmount;
			this.netAmtDesc = netAmtDesc;
			this.outStandings = outStandings;
			this.status = status;
			this.createdBy = createdBy;
			this.approverLabel = approverLabel;
			this.approverEmail = approverEmail;
			this.txnDocument = txnDocument;
			this.txnRemarks = txnRemarks;
			this.debitCredit = debitCredit;
			this.orgexpenseregistrered = orgexpenseregistrered;
			this.txnSpecialStatus = txnSpecialStatus;
			this.frieghtCharges = frieghtCharges;
			this.poReference = poReference;
			this.instrumentNumber = instrumentNumber;
			this.instrumentDate = instrumentDate;
			this.transactionPurposeID = transactionPurposeID;
			this.invoiceNumber = serialNumber;
			this.txnRefNo = txnRefNo;
			this.typeOfSupply = typeOfSupply;

		}

		public AddTransactionMessage(Long id, String branchName, String projectName, String itemName,
				String itemParentName, String budgetAllocated, String budgetAllocatedAmt, String budgetAvailable,
				String budgetAvailableAmt, String customerVendorName, String transactionPurpose, String txnDate,
				String invoiceDateLabel, String invoiceDate, String paymentMode, Double noOfUnit, Double unitPrice,
				Double grossAmount, Double netAmount, String netAmtDesc, String outStandings, String status,
				String createdBy, String approverLabel, String approverEmail, String txnDocument, String txnRemarks,
				String debitCredit, String approverEmails, String additionalApprovalEmails,
				String selectedAdditionalApproval, Map<String, ActorRef> orgexpenseregistrered,
				String txnSpecialStatus, Double frieghtCharges, String poReference, String instrumentNumber,
				String instrumentDate, Long transactionPurposeID, String remarksPrivate, String serialNumber,
				int txnIdentifier, String txnRefNo, Long branchId, Double totalDeductions, Integer workingDays,
				Integer typeOfSupply) {
			this.id = id;
			this.branchName = branchName;
			this.projectName = projectName;
			this.itemName = itemName;
			this.itemParentName = itemParentName;
			this.budgetAllocated = budgetAllocated;
			this.budgetAllocatedAmt = budgetAllocatedAmt;
			this.budgetAvailable = budgetAvailable;
			this.budgetAvailableAmt = budgetAvailableAmt;
			this.customerVendorName = customerVendorName;
			this.transactionPurpose = transactionPurpose;
			this.txnDate = txnDate;
			this.invoiceDateLabel = invoiceDateLabel;
			this.invoiceDate = invoiceDate;
			this.paymentMode = paymentMode;
			this.noOfUnit = noOfUnit;
			this.unitPrice = unitPrice;
			this.grossAmount = grossAmount;
			this.netAmount = netAmount;
			this.netAmtDesc = netAmtDesc;
			this.outStandings = outStandings;
			this.status = status;
			this.createdBy = createdBy;
			this.approverLabel = approverLabel;
			this.approverEmail = approverEmail;
			this.txnDocument = txnDocument;
			this.txnRemarks = txnRemarks;
			this.debitCredit = debitCredit;
			this.approverEmails = approverEmails;
			this.additionalApprovalEmails = additionalApprovalEmails;
			this.selectedAdditionalApproval = selectedAdditionalApproval;
			this.orgexpenseregistrered = orgexpenseregistrered;
			this.txnSpecialStatus = txnSpecialStatus;
			this.frieghtCharges = frieghtCharges;
			this.poReference = poReference;
			this.instrumentNumber = instrumentNumber;
			this.instrumentDate = instrumentDate;
			this.transactionPurposeID = transactionPurposeID;
			this.remarksPrivate = remarksPrivate;
			this.invoiceNumber = serialNumber;
			this.txnIdentifier = txnIdentifier;
			this.txnRefNo = txnRefNo;
			this.branchId = branchId;
			this.totalDeductions = totalDeductions;
			this.workingsDays = workingDays;
			this.typeOfSupply = typeOfSupply;

		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			if (!this.transactionPurpose.equals("Make Provision/Journal Entry")) {
				event.put("txnType", "sellExpenseTxn");
			} else if (this.transactionPurpose.equals("Make Provision/Journal Entry")) {
				event.put("txnType", "expenseAssetsLiabilitiesProvisionTxn");
			} else if (this.transactionPurpose.equalsIgnoreCase("process payroll")) {
				event.put("txnType", "processPayroll");
			}

			event.put("id", this.id);
			event.put("branchName", this.branchName);
			event.put("projectName", this.projectName);
			event.put("itemName", this.itemName);
			event.put("itemParentName", this.itemParentName);
			event.put("budgetAllocated", this.budgetAllocated);
			event.put("budgetAllocatedAmt", this.budgetAllocatedAmt);
			event.put("budgetAvailable", this.budgetAvailable);
			event.put("budgetAvailableAmt", this.budgetAvailableAmt);
			event.put("customerVendorName", this.customerVendorName);
			event.put("transactionPurpose", this.transactionPurpose);
			event.put("txnDate", this.txnDate);
			event.put("invoiceDateLabel", this.invoiceDateLabel);
			event.put("invoiceDate", this.invoiceDate);
			event.put("paymentMode", this.paymentMode);
			event.put("noOfUnit", this.noOfUnit);
			try {
				if (this.unitPrice != null) {
					event.put("unitPrice", IdosConstants.decimalFormat.format(this.unitPrice));
				} else {
					event.put("unitPrice", "");
				}
				if (this.grossAmount != null) {
					event.put("grossAmount", IdosConstants.decimalFormat.format(this.grossAmount));
				} else {
					event.put("grossAmount", "");
				}
				if (this.netAmount != null) {
					event.put("netAmount", IdosConstants.decimalFormat.format(this.netAmount));
				} else {
					event.put("netAmount", "");
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.out.println("Error : " + e.getMessage());
			}
			event.put("netAmtDesc", this.netAmtDesc);
			event.put("outstandings", this.outStandings);
			event.put("status", this.status);
			event.put("createdBy", this.createdBy);
			event.put("approverLabel", this.approverLabel);
			event.put("approverEmail", this.approverEmail);
			event.put("txnDocument", this.txnDocument);
			event.put("txnRemarks", this.txnRemarks);
			event.put("debitCredit", this.debitCredit);
			event.put("approverEmails", this.approverEmails);
			event.put("additionalApprovarUsers", this.additionalApprovalEmails);
			event.put("selectedAdditionalApproval", this.selectedAdditionalApproval);
			event.put("txnSpecialStatus", this.txnSpecialStatus);
			event.put("frieghtCharges", this.frieghtCharges);
			event.put("poReference", this.poReference);
			event.put("instrumentNumber", this.instrumentNumber);
			event.put("instrumentDate", this.instrumentDate);
			event.put("transactionPurposeID", this.transactionPurposeID);
			event.put("remarksPrivate", this.remarksPrivate);
			event.put("invoiceNumber", this.invoiceNumber);
			event.put("txnRefNo", this.txnRefNo);
			event.put("branchId", this.branchId);
			event.put("totalDeductions", this.totalDeductions);
			event.put("workingDays", this.workingsDays);
			event.put("typeOfSupply", this.typeOfSupply);
			return event.toString();
		}
	}

	public static class AddProcurementMessage {
		public String procurementStatus;

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			event.put("txnType", "sellExpenseTxn");
			event.put("procurementStatus", this.procurementStatus);
			return event.toString();
		}

		public AddProcurementMessage(String procurementStatus) {
			this.procurementStatus = procurementStatus;
		}
	}

	public static class AddExpenseMessage {

		public Long id;

		public String expenseCreator;

		public String expenseItem;

		public String expenseVendor;

		public Integer expenseQty;

		public Double totalAmount;

		public String expenseActionDate;

		public String expenseStatus;

		public String expenseDoc;

		public String txnRemarks;

		public AddExpenseMessage(Long id, String expenseCreator, String expenseItem, String expenseVendor,
				Integer expenseQty, Double totalAmount, String expenseActionDate, String expenseStatus,
				String expenseDoc, String txnRemarks) {
			this.id = id;
			this.expenseCreator = expenseCreator;
			this.expenseItem = expenseItem;
			this.expenseVendor = expenseVendor;
			this.expenseQty = expenseQty;
			this.totalAmount = totalAmount;
			this.expenseActionDate = expenseActionDate;
			this.expenseStatus = expenseStatus;
			this.expenseDoc = expenseDoc;
			this.txnRemarks = txnRemarks;
		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			event.put("txnType", "sellExpenseTxn");
			event.put("id", this.id);
			event.put("expenseCreator", this.expenseCreator);
			event.put("expenseItem", this.expenseItem);
			event.put("expenseVendor", this.expenseVendor);
			event.put("expenseQty", this.expenseQty);
			event.put("totalAmount", this.totalAmount);
			event.put("expenseActionDate", this.expenseActionDate);
			event.put("expenseStatus", this.expenseStatus);
			event.put("expenseDoc", this.expenseDoc);
			event.put("expenseTxnRemarks", this.txnRemarks);
			event.put("workflowAction", "addExpense");
			return event.toString();
		}

	}

	public static class RequestHiringMessage {
		public Long id;
		public String projectNumber;
		public String projectTitle;
		public String requester;
		public String requestType;
		public String position;
		public String status;
		public String remarks;
		public String document;
		public String approverEmailList;

		public RequestHiringMessage(Long id, String projectNumber, String projectTitle, String requester,
				String requestType, String position, String status, String remarks, String document,
				String approverEmailList) {
			this.id = id;
			this.projectNumber = projectNumber;
			this.projectTitle = projectTitle;
			this.requester = requester;
			this.approverEmailList = approverEmailList;
			this.requestType = requestType;
			this.position = position;
			this.status = status;
			this.remarks = remarks;
			this.document = document;
		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			event.put("txnType", "sellExpenseTxn");
			event.put("id", this.id);
			event.put("projectNumber", this.projectNumber);
			event.put("projectTitle", this.projectTitle);
			event.put("requester", this.requester);
			event.put("requetType", this.requestType);
			event.put("position", this.position);
			event.put("status", this.status);
			event.put("workflowAction", "requestHiring");
			event.put("remarks", this.remarks);
			event.put("document", this.document);
			event.put("approverEmailList", this.approverEmailList);
			return event.toString();
		}

	}

	public static class ActionHiring {
		public Long id;
		public String status;
		public String remarks;
		public String document;
		public String requester;
		public String approverEmailList;

		public ActionHiring(Long id, String status, String remarks, String document, String requester,
				String approverEmailList) {
			this.id = id;
			this.status = status;
			this.remarks = remarks;
			this.document = document;
			this.requester = requester;
			this.approverEmailList = approverEmailList;
		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			event.put("txnType", "sellExpenseTxn");
			event.put("id", this.id);
			event.put("status", this.status);
			event.put("workflowAction", "actionHiring");
			event.put("remarks", this.remarks);
			event.put("document", this.document);
			event.put("requester", this.requester);
			event.put("approverEmailList", this.approverEmailList);
			return event.toString();
		}

	}

	public static class AddClaimTransactionMessage {
		public Long id;
		public String branchName;
		public String projectName;
		public String txnQuestionName;
		public String txnOrgnName;
		public String claimtravelType;
		public String claimnoOfPlacesToVisit;
		public String claimplacesSelectedOrEntered;
		public String claimtypeOfCity;
		public String claimappropriateDiatance;
		public String claimtotalDays;
		public String claimtravelDetailedConfDescription;
		public String existingClaimsCurrentSettlementDetails;
		public Double claimexistingAdvance;
		public String claimuserAdvanveEligibility;
		public String userExpenditureOnThisTxn;
		public Double claimadjustedAdvance;
		public Double claimenteredAdvance;
		public Double claimtotalAdvance;
		public Double netSettlementAmount;
		public Double dueSettlementAmount;
		public Double requiredSettlement;
		public Double returnSettlement;
		public String claimpurposeOfVisit;
		public String claimtxnRemarks;
		public String claimsupportingDoc;
		public String debitCredit;
		public String claimTxnStatus;
		public String approverEmails;
		public String additionalApprovalEmails;
		public String selectedAdditionalApproval;
		public String creatorLabel;
		public String createdBy;
		public String transactionDate;
		public String approverLabel;
		public String approvedBy;
		public String accountantEmails;
		public String accountedLabel;
		public String accountedBy;
		@JsonIgnore
		public Map<String, ActorRef> orgclaimregistrered;
		public String txnSpecialStatus;
		public String paymentMode;
		public Double expenseAdvanceTotalAdvanceAmount;
		public String expenseAdvancepurposeOfExpenseAdvance;
		public String itemName;
		public String itemParticularName;
		public String parentSpecificName;
		public Double dueFromCompany;
		public Double dueToCompany;
		public Double amountReturnInCaseOfDueToCompany;
		public String instrumentNumber;
		public String instrumentDate;
		public String useremail;
		public String claimTxnRefNo;

		public AddClaimTransactionMessage(Long id, String branchName, String projectName, String txnQuestionName,
				String txnOrgnName, String claimtravelType,
				String claimnoOfPlacesToVisit, String claimplacesSelectedOrEntered, String claimtypeOfCity,
				String claimappropriateDiatance, String claimtotalDays, String claimtravelDetailedConfDescription,
				Double claimexistingAdvance,
				String claimuserAdvanveEligibility, Double claimadjustedAdvance, Double claimenteredAdvance,
				Double claimtotalAdvance,
				String claimpurposeOfVisit, String claimtxnRemarks, String claimsupportingDoc, String debitCredit,
				String claimTxnStatus,
				String approverEmails, String additionalApprovalEmails, String selectedAdditionalApproval,
				String creatorLabel,
				String createdBy, String transactionDate, String approverLabel, String approvedBy,
				String accountantEmails, String accountedLabel,
				String accountedBy, Map<String, ActorRef> orgclaimregistrered,
				String txnSpecialStatus, String paymentMode, String instrumentNumber, String instrumentDate,
				String userEmail, String claimTxnRefNo) {
			this.id = id;
			this.branchName = branchName;
			this.projectName = projectName;
			this.txnQuestionName = txnQuestionName;
			this.txnOrgnName = txnOrgnName;
			this.claimtravelType = claimtravelType;
			this.claimnoOfPlacesToVisit = claimnoOfPlacesToVisit;
			this.claimplacesSelectedOrEntered = claimplacesSelectedOrEntered;
			this.claimtypeOfCity = claimtypeOfCity;
			this.claimappropriateDiatance = claimappropriateDiatance;
			this.claimtotalDays = claimtotalDays;
			this.claimtravelDetailedConfDescription = claimtravelDetailedConfDescription;
			this.claimexistingAdvance = claimexistingAdvance;
			this.claimadjustedAdvance = claimadjustedAdvance;
			this.claimuserAdvanveEligibility = claimuserAdvanveEligibility;
			this.claimenteredAdvance = claimenteredAdvance;
			this.claimtotalAdvance = claimtotalAdvance;
			this.claimpurposeOfVisit = claimpurposeOfVisit;
			this.claimtxnRemarks = claimtxnRemarks;
			this.claimsupportingDoc = claimsupportingDoc;
			this.debitCredit = debitCredit;
			this.claimTxnStatus = claimTxnStatus;
			this.approverEmails = approverEmails;
			this.additionalApprovalEmails = additionalApprovalEmails;
			this.selectedAdditionalApproval = selectedAdditionalApproval;
			this.creatorLabel = creatorLabel;
			this.createdBy = createdBy;
			this.transactionDate = transactionDate;
			this.approverLabel = approverLabel;
			this.approvedBy = approvedBy;
			this.accountantEmails = accountantEmails;
			this.accountedLabel = accountedLabel;
			this.accountedBy = accountedBy;
			this.orgclaimregistrered = orgclaimregistrered;
			this.txnSpecialStatus = txnSpecialStatus;
			this.paymentMode = paymentMode;
			this.instrumentNumber = instrumentNumber;
			this.instrumentDate = instrumentDate;
			this.useremail = userEmail;
			this.claimTxnRefNo = claimTxnRefNo;
		}

		public AddClaimTransactionMessage(Long id, String branchName, String projectName, String txnQuestionName,
				String txnOrgnName, String claimtravelType, String claimnoOfPlacesToVisit,
				String claimplacesSelectedOrEntered, String claimtypeOfCity, String claimappropriateDiatance,
				String claimtotalDays, String claimtravelDetailedConfDescription, Double claimexistingAdvance,
				String claimuserAdvanveEligibility, Double claimadjustedAdvance, Double claimenteredAdvance,
				Double claimtotalAdvance, String claimpurposeOfVisit, String claimtxnRemarks,
				String claimsupportingDoc,
				String debitCredit, String claimTxnStatus, String approverEmails, String additionalApprovalEmails,
				String selectedAdditionalApproval, String creatorLabel, String createdBy, String transactionDate,
				String approverLabel, String approvedBy, String accountantEmails, String accountedLabel,
				String accountedBy, Map<String, ActorRef> orgclaimregistrered,
				String txnSpecialStatus,
				String paymentMode, Double expenseAdvanceTotalAdvanceAmount,
				String expenseAdvancepurposeOfExpenseAdvance, String itemName, String itemParticularName,
				String parentSpecificName, Double dueFromCompany, Double dueToCompany,
				Double amountReturnInCaseOfDueToCompany, String instrumentNumber, String instrumentDate,
				String userEmail, String claimTxnRefNo) {
			this.id = id;
			this.branchName = branchName;
			this.projectName = projectName;
			this.txnQuestionName = txnQuestionName;
			this.txnOrgnName = txnOrgnName;
			this.claimtravelType = claimtravelType;
			this.claimnoOfPlacesToVisit = claimnoOfPlacesToVisit;
			this.claimplacesSelectedOrEntered = claimplacesSelectedOrEntered;
			this.claimtypeOfCity = claimtypeOfCity;
			this.claimappropriateDiatance = claimappropriateDiatance;
			this.claimtotalDays = claimtotalDays;
			this.claimtravelDetailedConfDescription = claimtravelDetailedConfDescription;
			this.claimexistingAdvance = claimexistingAdvance;
			this.claimadjustedAdvance = claimadjustedAdvance;
			this.claimuserAdvanveEligibility = claimuserAdvanveEligibility;
			this.claimenteredAdvance = claimenteredAdvance;
			this.claimtotalAdvance = claimtotalAdvance;
			this.claimpurposeOfVisit = claimpurposeOfVisit;
			this.claimtxnRemarks = claimtxnRemarks;
			this.claimsupportingDoc = claimsupportingDoc;
			this.debitCredit = debitCredit;
			this.claimTxnStatus = claimTxnStatus;
			this.approverEmails = approverEmails;
			this.additionalApprovalEmails = additionalApprovalEmails;
			this.selectedAdditionalApproval = selectedAdditionalApproval;
			this.creatorLabel = creatorLabel;
			this.createdBy = createdBy;
			this.transactionDate = transactionDate;
			this.approverLabel = approverLabel;
			this.approvedBy = approvedBy;
			this.accountantEmails = accountantEmails;
			this.accountedLabel = accountedLabel;
			this.accountedBy = accountedBy;
			this.orgclaimregistrered = orgclaimregistrered;
			this.txnSpecialStatus = txnSpecialStatus;
			this.paymentMode = paymentMode;
			this.expenseAdvanceTotalAdvanceAmount = expenseAdvanceTotalAdvanceAmount;
			this.expenseAdvancepurposeOfExpenseAdvance = expenseAdvancepurposeOfExpenseAdvance;
			this.itemName = itemName;
			this.itemParticularName = itemParticularName;
			this.parentSpecificName = parentSpecificName;
			this.dueFromCompany = dueFromCompany;
			this.dueToCompany = dueToCompany;
			this.amountReturnInCaseOfDueToCompany = amountReturnInCaseOfDueToCompany;
			this.instrumentDate = instrumentDate;
			this.instrumentNumber = instrumentNumber;
			this.useremail = userEmail;
			this.claimTxnRefNo = claimTxnRefNo;
		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			if (this.txnQuestionName.equals("Request For Travel Advance")) {
				event.put("txnType", "claimTxn");
			}
			if (this.txnQuestionName.equals("Request Advance For Expense")) {
				event.put("txnType", "expenseAdvanceTxn");
			}
			if (this.txnQuestionName.equals("Settle Advance For Expense")) {
				event.put("txnType", "expAdvanceSettlementTxn");
			}
			if (this.txnQuestionName.equals("Request For Expense Reimbursement")) {
				event.put("txnType", "expReimbursementTxn");
			}
			if (this.txnQuestionName.equals("Settle Travel Advance")) {
				event.put("txnType", "claimSettlementTxn");
			}
			event.put("id", this.id);
			event.put("branchName", this.branchName);
			event.put("projectName", this.projectName);
			event.put("txnQuestionName", this.txnQuestionName);
			event.put("txnOrgnName", this.txnOrgnName);
			event.put("claimtravelType", this.claimtravelType);
			event.put("claimnoOfPlacesToVisit", this.claimnoOfPlacesToVisit);
			event.put("claimplacesSelectedOrEntered", this.claimplacesSelectedOrEntered);
			event.put("claimtypeOfCity", this.claimtypeOfCity);
			event.put("claimappropriateDiatance", this.claimappropriateDiatance);
			event.put("claimtotalDays", this.claimtotalDays);
			event.put("claimtravelDetailedConfDescription", this.claimtravelDetailedConfDescription);
			event.put("claimexistingAdvance", IdosConstants.decimalFormat.format(this.claimexistingAdvance));
			event.put("claimuserAdvanveEligibility", this.claimuserAdvanveEligibility);
			try {
				event.put("claimexistingAdvance",
						IdosConstants.decimalFormat.format(this.claimexistingAdvance));
				event.put("claimadjustedAdvance",
						IdosConstants.decimalFormat.format(this.claimadjustedAdvance));
				event.put("claimenteredAdvance",
						IdosConstants.decimalFormat.format(this.claimenteredAdvance));
				event.put("claimtotalAdvance", IdosConstants.decimalFormat.format(this.claimtotalAdvance));
			} catch (NumberFormatException e) {
				System.out.println("Error : " + e.getMessage());
			}

			event.put("claimpurposeOfVisit", this.claimpurposeOfVisit);
			event.put("claimtxnRemarks", this.claimtxnRemarks);
			event.put("claimsupportingDoc", this.claimsupportingDoc);
			event.put("debitCredit", this.debitCredit);
			event.put("claimTxnStatus", this.claimTxnStatus);
			event.put("approverEmails", this.approverEmails);
			event.put("additionalApprovarUsers", this.additionalApprovalEmails);
			event.put("selectedAdditionalApproval", this.selectedAdditionalApproval);
			event.put("creatorLabel", this.creatorLabel);
			event.put("createdBy", this.createdBy);
			event.put("transactionDate", this.transactionDate);
			event.put("approverLabel", this.approverLabel);
			event.put("approvedBy", this.approvedBy);
			event.put("accountantEmails", this.accountantEmails);
			event.put("accountedLabel", this.accountedLabel);
			event.put("accountedBy", this.accountedBy);
			event.put("txnSpecialStatus", this.txnSpecialStatus);
			event.put("paymentMode", this.paymentMode);
			event.put("expenseAdvanceTotalAdvanceAmount", this.expenseAdvanceTotalAdvanceAmount);
			event.put("expenseAdvancepurposeOfExpenseAdvance", this.expenseAdvancepurposeOfExpenseAdvance);
			event.put("itemName", this.itemName);
			event.put("itemParticularName", this.itemParticularName);
			event.put("parentSpecificName", this.parentSpecificName);
			event.put("parentSpecificName", this.parentSpecificName);
			event.put("parentSpecificName", this.parentSpecificName);
			event.put("parentSpecificName", this.parentSpecificName);
			event.put("dueFromCompany", this.dueFromCompany);
			event.put("dueToCompany", this.dueToCompany);
			event.put("amountReturnInCaseOfDueToCompany", this.amountReturnInCaseOfDueToCompany);
			event.put("instrumentNumber", this.instrumentNumber);
			event.put("instrumentDate", this.instrumentDate);
			event.put("claimTxnRefNo", this.claimTxnRefNo);
			return event.toString();
		}

	}

	public static class AddClaimTransactionSettlementMessage {
		public Long id;
		public String branchName;
		public String projectName;
		public String txnQuestionName;
		public String txnOrgnName;
		public String travelType;
		public String noOfPlacesToVisit;
		public String placesSelectedOrEntered;
		public String typeOfCity;
		public String appropriateDiatance;
		public String totalDays;
		public String travelDetailedConfDescription;
		public String existingClaimsCurrentSettlementDetails;
		public String claimuserAdvanveEligibility;
		public String userExpenditureOnThisTxn;
		public Double netSettlementAmount;
		public Double dueSettlementAmount;
		public Double requiredSettlement;
		public Double returnSettlement;
		public Double amountReturnInCaseOfDueToCompany;
		public String purposeOfVisit;
		public String claimtxnRemarks;
		public String claimsupportingDoc;
		public String claimdebitCredit;
		public String claimTxnStatus;
		public String creatorLabel;
		public String createdBy;
		public String accountedLabel;
		public String accountedBy;
		public String transactionDate;
		public String paymentMode;
		public String accountantEmails;
		public String instrumentNumber;
		public String instrumentDate;
		public String userEmail;
		public String claimTxnRefNo;
		@JsonIgnore
		public Map<String, ActorRef> orgclaimregistrered;

		public AddClaimTransactionSettlementMessage(Long id, String branchName, String projectName,
				String txnQuestionName, String txnOrgnName, String travelType, String noOfPlacesToVisit,
				String placesSelectedOrEntered, String typeOfCity, String appropriateDiatance, String totalDays,
				String travelDetailedConfDescription, String existingClaimsCurrentSettlementDetails,
				String claimuserAdvanveEligibility, String userExpenditureOnThisTxn, Double netSettlementAmount,
				Double dueSettlementAmount, Double requiredSettlement, Double returnSettlement,
				String purposeOfVisit,
				String claimdebitCredit, String claimTxnStatus, String creatorLabel, String createdBy,
				String accountedLabel, String accountedBy, String transactionDate, String paymentMode,
				String accountantEmails, String claimtxnRemarks, String claimsupportingDoc,
				Map<String, ActorRef> orgclaimregistrered, String instrumentNumber,
				String instrumentDate, String userEmail, Double amountReturnInCaseOfDueToCompany,
				String claimTxnRefNo) {
			this.id = id;
			this.branchName = branchName;
			this.projectName = projectName;
			this.txnQuestionName = txnQuestionName;
			this.txnOrgnName = txnOrgnName;
			this.travelType = travelType;
			this.noOfPlacesToVisit = noOfPlacesToVisit;
			this.placesSelectedOrEntered = placesSelectedOrEntered;
			this.typeOfCity = typeOfCity;
			this.appropriateDiatance = appropriateDiatance;
			this.totalDays = totalDays;
			this.travelDetailedConfDescription = travelDetailedConfDescription;
			this.existingClaimsCurrentSettlementDetails = existingClaimsCurrentSettlementDetails;
			this.claimuserAdvanveEligibility = claimuserAdvanveEligibility;
			this.userExpenditureOnThisTxn = userExpenditureOnThisTxn;
			this.netSettlementAmount = netSettlementAmount;
			this.dueSettlementAmount = dueSettlementAmount;
			this.requiredSettlement = requiredSettlement;
			this.returnSettlement = returnSettlement;
			this.purposeOfVisit = purposeOfVisit;
			this.claimdebitCredit = claimdebitCredit;
			this.claimTxnStatus = claimTxnStatus;
			this.creatorLabel = creatorLabel;
			this.createdBy = createdBy;
			this.accountedLabel = accountedLabel;
			this.accountedBy = accountedBy;
			this.transactionDate = transactionDate;
			this.paymentMode = paymentMode;
			this.instrumentDate = instrumentDate;
			this.instrumentNumber = instrumentNumber;
			this.accountantEmails = accountantEmails;
			this.claimtxnRemarks = claimtxnRemarks;
			this.claimsupportingDoc = claimsupportingDoc;
			this.orgclaimregistrered = orgclaimregistrered;
			this.instrumentNumber = instrumentNumber;
			this.instrumentDate = instrumentDate;
			this.userEmail = userEmail;
			this.amountReturnInCaseOfDueToCompany = amountReturnInCaseOfDueToCompany;
			this.claimTxnRefNo = claimTxnRefNo;
		}

		@Override
		public String toString() {
			return "AddClaimTransactionSettlementMessage [id=" + id + ", branchName=" + branchName + ", projectName="
					+ projectName + ", txnQuestionName=" + txnQuestionName + ", txnOrgnName=" + txnOrgnName
					+ ", travelType=" + travelType + ", noOfPlacesToVisit=" + noOfPlacesToVisit
					+ ", placesSelectedOrEntered=" + placesSelectedOrEntered + ", typeOfCity=" + typeOfCity
					+ ", appropriateDiatance=" + appropriateDiatance + ", totalDays=" + totalDays
					+ ", travelDetailedConfDescription=" + travelDetailedConfDescription
					+ ", existingClaimsCurrentSettlementDetails=" + existingClaimsCurrentSettlementDetails
					+ ", claimuserAdvanveEligibility=" + claimuserAdvanveEligibility + ", userExpenditureOnThisTxn="
					+ userExpenditureOnThisTxn + ", netSettlementAmount=" + netSettlementAmount
					+ ", dueSettlementAmount=" + dueSettlementAmount + ", requiredSettlement=" + requiredSettlement
					+ ", returnSettlement=" + returnSettlement + ", amountReturnInCaseOfDueToCompany="
					+ amountReturnInCaseOfDueToCompany + ", purposeOfVisit=" + purposeOfVisit + ", claimtxnRemarks="
					+ claimtxnRemarks + ", claimsupportingDoc=" + claimsupportingDoc + ", claimdebitCredit="
					+ claimdebitCredit + ", claimTxnStatus=" + claimTxnStatus + ", creatorLabel=" + creatorLabel
					+ ", createdBy=" + createdBy + ", accountedLabel=" + accountedLabel + ", accountedBy=" + accountedBy
					+ ", transactionDate=" + transactionDate + ", paymentMode=" + paymentMode + ", accountantEmails="
					+ accountantEmails + ", instrumentNumber=" + instrumentNumber + ", instrumentDate=" + instrumentDate
					+ ", userEmail=" + userEmail + ", claimTxnRefNo=" + claimTxnRefNo + ", orgclaimregistrered="
					+ orgclaimregistrered + "]";
		}

	}

	public static class ChatMessage {
		public String from;
		public String to;
		public String message;
		public String name;

		public ChatMessage(final String from, final String to, final String message, final String name) {
			this.from = from;
			this.to = to;
			this.message = message;
			this.name = name;
		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			event.put("type", "chat");
			event.put("from", this.from);
			event.put("to", this.to);
			event.put("message", this.message);
			event.put("name", this.name);
			return event.toString();
		}

	}

	public static class OnlineUsersMessage {
		public boolean result;
		public ArrayNode node;

		public OnlineUsersMessage(final boolean result, final ArrayNode node) {
			this.result = result;
			this.node = node;
		}

		@Override
		public String toString() {
			ObjectNode event = Json.newObject();
			event.put("txnType", "onlineUsers");
			event.put("result", this.result);
			event.put("users", this.node);
			return event.toString();
		}

	}
}
