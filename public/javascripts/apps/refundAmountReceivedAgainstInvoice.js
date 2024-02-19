var calculateResultantRefundAmount = function (elem) {
    let parentTr = $(elem).closest("tr").attr("id");
    let refundAmt = $.trim($(elem).val());
    if (refundAmt == "") {
        return false;
    }
    let availableRefundAmt = $.trim($("#" + parentTr + " input[name='amntAvailForRefund']").val());
    if (refundAmt != "" && availableRefundAmt != "") {
        if ($.isNumeric(refundAmt) && $.isNumeric(availableRefundAmt)) {
            if (Number(refundAmt) > Number(availableRefundAmt)) {
                $(elem).val("");
                $("#" + parentTr + " input[name='rfndamntResultantAmnt']").val("");
                swal("Advance Refund!", "Refund amount Must be Less than or Equal to Advance available", "error");
                return false;
            }
            let amt = Number(availableRefundAmt) - Number(refundAmt);
            $("#" + parentTr + " input[name='rfndamntResultantAmnt']").val(parseFloat(amt).toFixed(2));
        }
    } else {
        $(elem).val("");
        swal("Refund Amount!", "Refund amount can't be Zero or Empty", "error");
    }
}

var calculateResultantRefundTDSForRefund = function (elem) {
    let parentTr = $(elem).closest("tr").attr("id");
    let refundTDS = $.trim($(elem).val());
    //alert("refund tds="+refundTDS);
    if (refundTDS == "") {
        return false;
    }
    let availableRefundTDS = $.trim($("#" + parentTr + " input[name='tdsAvailForRefund']").val());
    if (refundTDS != "" && availableRefundTDS != "") {
        if ($.isNumeric(refundTDS) && $.isNumeric(availableRefundTDS)) {
            if (Number(refundTDS) > Number(availableRefundTDS)) {
                $(elem).val("");
                $("#" + parentTr + " input[name='rfndamntResultantTax]").val("");
                swal("Witholding Tax (TDS)!", "Witholding Tax (TDS) Must be Less than or Equal to Witholding Tax (TDS) Available for Reversal", "error");
                return false;
            }

            let amt = Number(availableRefundTDS) - Number(refundTDS);
            $("#" + parentTr + " input[name='rfndamntResultantTax']").val(amt);
        }
    } else {
        $(elem).val("");
        swal("Witholding Tax (TDS)!", "Witholding Tax (TDS) can't be Zero or Empty", "error");
    }
}

var submitForApprovalRefundAmountReceivedAgainstInvoice = function (whatYouWantToDo, whatYouWantToDoVal, parentTr) {
    let transactionDiv = $("#" + parentTr).closest('div').attr('id');
    let sourceGstin = "";
    let destinGstin = "";
    let txnTypeOfSupply = "";
    let txnForBranch = $("#" + parentTr + " select[class='txnBranches'] option:selected").val();
    let txnForCustomer = $("#" + parentTr + " .masterList option:selected").val();
    let txnRemarks = $("#" + parentTr + " textarea[name='mkrfndRemarks']").val();
    let txnRemarksPrivate = $("#" + parentTr + "  textarea[name='rfndRemarksPrivate']").val();
    let txnInvoice = $("#" + transactionDiv + " table[class='multipleItemsTable'] tbody tr:last").find("select[class='salesExpenseTxns'] option:selected").val();
    let txnForItem = convertTableDataToArray("multipleItemsTablerfndamntrcvd");
    let supportingDocTmp = $("#" + parentTr + " select[class='txnUploadSuppDocs'] option").map(function () {
        if ($(this).val() != "") {
            return $(this).val();
        }
    }).get();
    let supportingDoc = supportingDocTmp.join(',');
    let totalRefund = 0;
    let txnNetTDS = 0;
    $("#multipleItemsTablerfndamntrcvd tbody tr").each(function () {
        let selectedTr = $(this).attr('id');
        let itemNetAmt = $("#" + selectedTr + " input[class='refundAmountReceived']").val();
        let itemNetTax = $("#" + selectedTr + " input[class='taxAdjusted']").val();

        if (itemNetAmt != "") {
            totalRefund += parseFloat(itemNetAmt);
            if (itemNetTax != "") {
                txnNetTDS += parseFloat(itemNetTax);
            }
        }
    });

    let txnNetAmount = totalRefund;
    let netAmountTotalWithDecimalValue = totalRefund;
    if (txnNetAmount <= 0.0) {
        swal("Incomplete Transction detail!", "Please provide complete transaction details befor submitting for approval.", "error");
        enableTransactionButtons();
        return true;
    }

    let txnEntityID = $("#" + parentTr).attr("name");
    let txnJsonData = {};
    txnJsonData.txnPurpose = whatYouWantToDo;
    txnJsonData.txnPurposeVal = whatYouWantToDoVal;
    txnJsonData.txnforbranch = txnForBranch;
    txnJsonData.txnforitem = txnForItem;
    txnJsonData.txnforcustomer = txnForCustomer;
    txnJsonData.txnInvoice = txnInvoice;
    txnJsonData.txnnetAmount = txnNetAmount;
    txnJsonData.txnnetTDS = txnNetTDS;
    txnJsonData.txnremarks = txnRemarks;
    txnJsonData.txnRemarksPrivate = txnRemarksPrivate;
    txnJsonData.txnEntityID = txnEntityID;
    txnJsonData.supportingdoc = supportingDoc;
    txnJsonData.useremail = $("#hiddenuseremail").text();
    txnJsonData.txnSourceGstin = sourceGstin;
    txnJsonData.txnDestinGstin = destinGstin;
    txnJsonData.txnTypeOfSupply = txnTypeOfSupply;
    txnJsonData.netAmountTotalWithDecimalValue = netAmountTotalWithDecimalValue;
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
                enableTransactionButtons();
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
                swal("Error on Submit For Approval!", "Please retry, if problem persists contact support team.", "error");
            }
        },
        complete: function (data) {
            enableTransactionButtons();
            getTransactionsOnload();
            $.unblockUI();
        }
    });
}
