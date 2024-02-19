var submitForApprovalVendRecAdv = function (whatYouWantToDo, whatYouWantToDoVal, parentTr) {
    let txnGstCountryCode = $("#gstCountryCode").val();
    let txnTypeOfSupply = "";
    let txnWalkinCustomerType = "";
    let txnWithWithoutTax = "";
    let itemItemsList = "";
    let customerLocation = "";
    let txnRcmTaxItem = "";
    let netAmountPaid = "";
    let sourceGstin = ""
    let destinGstin = "";
    let txnJsonData = {};
    if (txnGstCountryCode !== "" && typeof txnGstCountryCode != 'undefined' && txnGstCountryCode !== null) {
        txnTypeOfSupply = $("#" + parentTr + " select[class='txnTypeOfSupply']").val();
        if (txnTypeOfSupply == "" || txnTypeOfSupply == null) {
            swal("Invalid Type of Supply!", "Please select valid Type of Supply.", "error");
            enableTransactionButtons();
            return false;
        }
        txnWithWithoutTax = $("#" + parentTr + " select[class='txnWithWithoutTaxCls']").val();

        //if((txnTypeOfSupply =="3" || txnTypeOfSupply =="4" || txnTypeOfSupply =="5") && txnWithWithoutTax==""){
        //swal("Incomplete transaction detail!", "Please select With / Without IGST.", "error");
        //enableTransactionButtons();
        //	return false;
        //}

        if (txnTypeOfSupply == "4" || txnTypeOfSupply == "5") {
            txnRcmTaxItem = $("#multipleItemsTablepcafcv" + " select[class='txnRcmTaxItem']").children(":selected").val();
        }

        sourceGstin = $("#" + parentTr + " select[class='txnBranches']").children(":selected").attr("id");
        if (sourceGstin === null || sourceGstin === "") {
            swal("Invalid Branch's GSTIN on submit for approval", "Please select valid Branch.", "error");
            enableTransactionButtons();
            return false;
        }
        destinGstin = $("#" + parentTr + " select[class='placeOfSply txnDestGstinCls']").val();
        if (txnTypeOfSupply != "3" && destinGstin == "") {
            swal("Invalid Place of Supply!", "Please provide valid Place of Supply.", "error");
            enableTransactionButtons();
            return false;
        }
    }

    let isValid = validateMultiItemsTranRec("multipleItemsTablepcafcv");
    if (!isValid) {
        swal("Incomplete transaction data!", "Please provide complete transaction details befor submitting for arroval", "error");
        enableTransactionButtons();
        return false;
    }
    let txnForBranch = $("#pcafcvTxnForBranches option:selected").val();
    let txnForItem = convertTableDataToArray("multipleItemsTablepcafcv");
    let totalTDSAmt = calcuateTotalAmt4Element('transactionDetailsPCAFCVTable', 'withholdingtaxcomponenetdiv');
    netAmountPaid = $("#pcafnetAmountPaid").val();
    let creditCustomer = $("#pcafcvVendors option:selected").val();
    //var purposeOfAdvance= $("#rcafccadvancepurpose").val();
    //var withHoldingTaxReceipt = $("#pcafcvTaxAdjusted").val();
    let txnRemarks = $("#pcafcvRemarks").val();
    let supportingDocTmp = $("#" + parentTr + " select[class='txnUploadSuppDocs'] option").map(function () {
        if ($(this).val() != "") {
            return $(this).val();
        }
    }).get();
    let supportingDoc = supportingDocTmp.join(',');
    if ((creditCustomer == "")) {
        swal("Incomplete transaction data!", "Please provide complete transaction details before submitting for approval", "error");
        enableTransactionButtons();
        return true;
    }
    // SingleUSer
    let txnreceiptPaymentBank = "";
    let txnInstrumentNum = "";
    let txnInstrumentDate = "";
    let receiptPaymentBank = "";
    let txnReceiptTypeBankDetails = "";
    if(READ_PAYMODE_ON_APPROVAL == IS_READ_PAYMODE_ON_APPROVAL){
        let returnVal = getReceiptPaymentDetails(txnJsonData, parentTr);
		if(returnVal === false){
			return false;
		}
    } else {
    	txnJsonData.txnReceiptDetails = "";
        txnJsonData.txnReceiptPaymentBank = "";
        txnJsonData.txnInstrumentNum = "";
        txnJsonData.txnInstrumentDate = "";
        txnJsonData.txnReceiptDescription = "";
    }
    txnReceiptTypeBankDetails = $("#" + parentTr + " textarea[id='paymentBranchBankAccount']").val();
    let txnDate = $("#" + parentTr).find(".txnBackDate").val();
    txnJsonData.txnPurpose = whatYouWantToDo;
    txnJsonData.txnPurposeVal = whatYouWantToDoVal;
    txnJsonData.txnforbranch = txnForBranch;
    txnJsonData.txnforitem = txnForItem;
    txnJsonData.txnRCAFCCCreditCustomer = creditCustomer;
    //txnJsonData.txnRCAFCCPurposeOfAdvance = purposeOfAdvance;
    //txnJsonData.txnRCAFCCWithHoldingTaxReceipt = withHoldingTaxReceipt;
    txnJsonData.txnremarks = txnRemarks;
    txnJsonData.supportingdoc = supportingDoc;
    txnJsonData.useremail = $("#hiddenuseremail").text();
    txnJsonData.txnSourceGstin = sourceGstin;
    txnJsonData.txnDestinGstin = destinGstin;
    txnJsonData.txnTypeOfSupply = txnTypeOfSupply;
    txnJsonData.txnRcmTaxItem = txnRcmTaxItem;
    txnJsonData.netAmountPaid = netAmountPaid;
    txnJsonData.totalTDSAmt = totalTDSAmt;
    /*txnJsonData.txnreceiptPaymentBank = txnreceiptPaymentBank;
    txnJsonData.txnInstrumentNum = txnInstrumentNum;
    txnJsonData.txnInstrumentDate = txnInstrumentDate;
    txnJsonData.txnReceiptPaymentBank = receiptPaymentBank;
    txnJsonData.txnReceiptDetails = txnReceiptDetails;*/
    txnJsonData.txnReceiptTypeBankDetails = txnReceiptTypeBankDetails;

    txnJsonData.txnDate = txnDate;
    //txnJsonData.txnWithWithoutTax = txnWithWithoutTax;
    let url = "/transaction/submitForApproval";
    $.ajax({
        url: url,
        data: JSON.stringify(txnJsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            if(typeof data.message !=='undefined' && data.message != ""){
                swal("Error!", data.message, "error");
                return false;
            }
            if (data.recAdvFromVendCOAType == 0) {
                swal("COA: mapping missing!", "Please make necessary mapping in Chart of accounts - Is this where you classify advance paid to vendors/creditors", "error");
                disableTransactionButtons();
                return false;
            }
            approvealForSingleUser(data);	// Single User
            cancel();
            viewTransactionData(data); // to render the updated transaction recored
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on Submit For Approval!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}

var readMultiItemDataForPayVendor = function(tableName) {
    let parentTable = $("#"+tableName).parents().closest('table').attr('id');
    let whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
    let multipleItemsData = [];
    if (whatYouWantToDoVal == PAY_VENDOR_SUPPLIER) {
        $("#" + tableName + " > tbody > tr").each(function () {
            let jsonData = {};
            let pendingTxn = "", amountReceived = "", taxWH = "", discAllowed = "", dueBal = "";
            pendingTxn = $(this).find("td .pendingTxns option:selected").val();
            if(pendingTxn == ""){
                swal("Mandatory data is not provided!", "Select a transaction.", "error");
                enableTransactionButtons();
                return false;
            }
            amountPaid = $(this).find("td #mcpfcvpaymentpaid").val();
            if(amountPaid == ""){
                swal("Mandatory data is not provided!", "Provide Amount to pay.", "error");
                enableTransactionButtons();
                return false;
            }
            discReceived = $(this).find("td #mcpfcvdiscountReceived").val();
            dueBal = $(this).find("td #mcpfcvduebalance").val();
            jsonData.pendingTxn = pendingTxn;
            jsonData.amountPaid = amountPaid;
            jsonData.discReceived = discReceived;
            jsonData.dueBal = dueBal;
            multipleItemsData.push(jsonData);
        });
    }
    if(multipleItemsData.length <= 0){
        swal("Mandatory data is not provided!", "Please provide complete data for transaction to approve.", "error");
        enableTransactionButtons();
    }
    return multipleItemsData;
}
