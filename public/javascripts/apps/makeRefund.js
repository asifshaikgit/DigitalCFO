var calculateResultantRefundAdv = function (elem) {
    var parentTr = $(elem).closest("tr").attr("id");
    var refundAdv = $.trim($(elem).val());
    if (refundAdv == "") {
        return false;
    }
    var availableRefundAdv = $.trim($("#" + parentTr + " input[name='mkrfndadvAvailForRefund']").val());
    if (refundAdv != "" && availableRefundAdv != "") {
        if ($.isNumeric(refundAdv) && $.isNumeric(availableRefundAdv)) {
            if (Number(refundAdv) > Number(availableRefundAdv)) {
                $(elem).val("");
                $("#" + parentTr + " input[name='mkrfndadvResultantAdv']").val("");
                swal("Advance Refund!", "Refund amount Must be Less than or Equal to Advance available", "error");
                return false;
            }
            var amt = Number(availableRefundAdv) - Number(refundAdv);
            $("#" + parentTr + " input[name='mkrfndadvResultantAdv']").val(amt.toFixed(2));
        }
    } else {
        $(elem).val("");
        swal("Refund Amount!", "Refund amount can't be Zero or Empty", "error");
    }
}

var calculateResultantRefundTDS = function (elem) {
    var parentTr = $(elem).closest("tr").attr("id");
    var refundTDS = $.trim($(elem).val());
    if (refundTDS == "") {
        return false;
    }
    var availableRefundTDS = $.trim($("#" + parentTr + " input[name='mkrfndtdsAvailForRefund']").val());
    if (refundTDS != "" && availableRefundTDS != "") {
        if ($.isNumeric(refundTDS) && $.isNumeric(availableRefundTDS)) {
            if (Number(refundTDS) > Number(availableRefundTDS)) {
                $(elem).val("");
                $("#" + parentTr + " input[name='mkrfndadvResultantTax']").val("");
                swal("Witholding Tax (TDS)!", "Witholding Tax (TDS) Must be Less than or Equal to Witholding Tax (TDS) Available for Reversal", "error");
                return false;
            }

            var amt = Number(availableRefundTDS) - Number(refundTDS);
            $("#" + parentTr + " input[name='mkrfndadvResultantTax']").val(amt);
        }
    } else {
        $(elem).val("");
        swal("Witholding Tax (TDS)!", "Witholding Tax (TDS) can't be Zero or Empty", "error");
    }
}


var setMakeRefundTaxDetail = function (data, value, i, transactionTableHeadTr, transactionTableTr) {
    if (data.txnItemData[i].taxData) {
        var taxNameList = "";
        for (var j = 0; j < data.txnItemData[i].taxData.length; j++) {
            taxNameList += '<div class="taxNameHead">' + data.txnItemData[i].taxData[j].taxName + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
            var taxCellData = "";

            taxCellData = '<input type="text" style="width:31px;" class="taxRate"  name="mkrfndTaxRate" id="mkrfndTaxRate" readonly="readonly" placeholder="Tax Rate" value="' + data.txnItemData[i].taxData[j].taxRate + '"/>' +
                '<input type="text" style="width:62px;" class="txnTaxAmount"  name="mkrfndTaxamnt" id="mkrfndTaxamnt" readonly="readonly" placeholder="Tax Amount" value=""/>';
            taxCellData += '<input type="hidden" class="txnTaxName"  name="mkrfndTaxName" id="mkrfndTaxName" value="' + data.txnItemData[i].taxData[j].taxName + '"/>';
            taxCellData += '<input type="hidden" class="txnTaxID" name="mkrfndTaxID" id="mkrfndTaxID"  value="' + data.txnItemData[i].taxData[j].taxid + '"/>';

            $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last div[id='taxCell" + j + "']").addClass('taxCellCls');
            $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last div[id='taxCell" + j + "']").empty();
            $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last div[id='taxCell" + j + "']").append(taxCellData);
        }

        $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last div[id='taxCell']").addClass('div-w' + j + '00');
        var taxNameHeadDivCount = $('.taxNameHead').length;
        if (data.txnItemData[i].taxData.length > parseInt(taxNameHeadDivCount)) {
            $("#" + transactionTableHeadTr + " table[id='multipleItemsTblHead'] div[id='taxNameList']").addClass('div-w' + j + '00');
            $("#" + transactionTableHeadTr + " table[id='multipleItemsTblHead'] div[id='taxNameList']").empty();
            $("#" + transactionTableHeadTr + " table[id='multipleItemsTblHead'] div[id='taxNameList']").append(taxNameList);
        }
    }

    if (data.txnItemData[i].advAdjTaxData) {
        var advAdjTaxNameList = "";
        for (var j = 0; j < data.txnItemData[i].advAdjTaxData.length; j++) {
            advAdjTaxNameList += '<div class="taxNameHead">' + data.txnItemData[i].advAdjTaxData[j].taxName + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
            var taxCellData = '<input type="text" class="txnTaxOnAdvAdjCls" id="taxOnAdvAdj" readonly="readonly" placeholder="Tax Amount" value="' + data.txnItemData[i].advAdjTaxData[j].taxAmount + '"/>';
            taxCellData += '<input type="hidden" class="txnTaxNameOnAdvAdjCls" id="taxNameOnAdvAdj" value="' + data.txnItemData[i].advAdjTaxData[j].taxName + '"/>';
            $("#" + transactionTableTr + " div[id='advAdjTaxCell" + j + "']").addClass('advAdjTaxCellCls');
            $("#" + transactionTableTr + " div[id='advAdjTaxCell" + j + "']").empty();
            $("#" + transactionTableTr + " div[id='advAdjTaxCell" + j + "']").append(taxCellData);
        }
        $("#" + transactionTableTr + " div[id='advAdjTaxCell']").addClass('div' + j + '-wd');
        $("#" + transactionTableHeadTr + " table[id='multipleItemsTblHead'] div[id='advAdjTaxNameList']").addClass('div' + j + '-wd');
        $("#" + transactionTableHeadTr + " table[id='multipleItemsTblHead'] div[id='advAdjTaxNameList']").empty();
        $("#" + transactionTableHeadTr + " table[id='multipleItemsTblHead'] div[id='advAdjTaxNameList']").append(advAdjTaxNameList);
    }
}


var getTaxComponentForMakeRefund = function (elem) {
    var returnValue = true;
    var parentTr = $(elem).closest('tr').attr('id');
    var parentOfparentTr = $("#" + parentTr).parent().parent().parent().parent().closest('div').attr('id');
    var parentTable = $(elem).parents().closest('table').attr('id');
    var text = $("#whatYouWantToDo").find('option:selected').text();
    var txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val();
    var txnBranch = $("#" + parentOfparentTr + " select[class='txnBranches']").val();
    if (txnBranch == "") {
        swal("Incomplete transaction detail!", "Please select a Branch.", "error");
        returnValue = false;
    }

    var txnTypeOfSupply = $("#" + parentOfparentTr + " select[class='txnTypeOfSupply']").val();
    if (txnTypeOfSupply == "") {
        swal("Incomplete transaction detail!", "Please select a Type of Supply.", "error");
        returnValue = false;
    }
    var txnWithWithoutTax = $("#" + parentOfparentTr + " select[class='txnWithWithoutTaxCls']").val();
    if ((txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5") && txnWithWithoutTax == "") {
        swal("Incomplete transaction detail!", "Please select With / Without IGST.", "error");
        returnValue = false;
    }
    var txnUnavailable = $("#" + parentOfparentTr + " input[name='unAvailableCustomer']").val();
    var txnVendorCustomer = $("#" + parentOfparentTr + " .masterList").val();
    if (txnVendorCustomer == "" && txnUnavailable == "") {
        swal("Incomplete transaction detail!", "Please select customer or provide customer name.", "error");
        returnValue = false;
    }
    var txnitemSpecifics = $("#" + parentTr + " .txnItems").val();
    if (txnitemSpecifics == "") {
        swal("Incomplete transaction detail!", "Please select a Item for transaction.", "error");
        returnValue = false;
    }
    var taxableValue = 0;
    var taxWithHeld = $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val();
    if (taxWithHeld != "" && taxWithHeld != "0") {
        taxableValue = parseFloat(taxWithHeld);
    }
    var sourceGstinCode = $("#" + parentOfparentTr + " select[class='txnBranches']").children(":selected").attr("id");
    var destGstinCode = $("#" + parentOfparentTr + " select[class='placeOfSply txnDestGstinCls']").val();
    var destCustDetailId = $("#" + parentOfparentTr + " select[class='placeOfSply txnDestGstinCls']").children(":selected").attr("id");
    if (txnVendorCustomer === "" && txnUnavailable !== "") {
        var txnWalkinCustomerType = $("#" + parentOfparentTr + " select[class='walkinCustType']").val();
        if (txnWalkinCustomerType == "") {
            swal("Invalid Type of Customer!", "Please select valid Type of customer.", "error");
            returnValue = false;
        } else if (txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2") {
            destGstinCode = $("#" + parentOfparentTr + " input[class='placeOfSplyTextHid']").val();
        } else if (txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4") {
            destGstinCode = sourceGstinCode;
        } else if (txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6") {
            destGstinCode = $("#" + parentOfparentTr + " select[name='txnWalkinPlcSplySelect']").val();
        }
    }
    var refundAdvance = $("#" + parentTr + " input[class='advanceReceived']").val();
    if (refundAdvance == "") {
        $("#" + parentTr + " div[class='taxCellCls']").empty();
        $("#" + parentTable + " table[id='multipleItemsTblHead'] div[id='taxNameList']").empty();
        $("#" + parentTr + " input[name='mkrfndadvResultantAdv']").val("");
        returnValue = false;
    }
    taxableValue = parseFloat(refundAdvance);
    var taxReceived = $("#" + parentTr + " input[class='taxAdjusted']").val();
    if (taxReceived != "") {
        taxableValue = parseFloat(taxableValue) + parseFloat(taxReceived);
    }
    $("#" + parentTr + " input[class='txnGross']").val(taxableValue);
    $("#" + parentOfparentTr + " input[class='netAmountValTotal']").val("");
    if (returnValue === true) {
        $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        var jsonData = {};
        var txnDate = $("#" + parentTr).find(".txnBackDate").val();
        jsonData.useremail = $("#hiddenuseremail").text();
        jsonData.userTxnPurposeText = text;
        jsonData.txnBranchId = txnBranch;
        jsonData.txnSpecificsId = txnitemSpecifics;
        jsonData.txnAdjustmentAmount = taxableValue;
        jsonData.txnWithheld = taxWithHeld;
        jsonData.txnSelectedVendorCustomer = txnVendorCustomer;
        jsonData.txnPurposeValue = txnPurposeVal;
        jsonData.txnSourceGstinCode = sourceGstinCode;
        jsonData.txnDestGstinCode = destGstinCode;
        jsonData.txnDestCustDetailId = destCustDetailId;
        jsonData.txnTypeOfSupply = txnTypeOfSupply;
        jsonData.txnWithWithoutTax = txnWithWithoutTax;
        jsonData.txnDate = txnDate;
        var url = "/transaction/getAdvAdjTax";
        $.ajax({
            url: url,
            data: JSON.stringify(jsonData),
            type: "text",
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            async: false,
            method: "POST",
            contentType: 'application/json',
            success: function (data) {
                if (txnPurposeVal == REFUND_ADVANCE_RECEIVED) {
                    var taxNameList = "";
                    var taxTypeAndRate = "";
                    for (var i = 0; i < data.advAdjTaxData.length; i++) {
                        taxNameList += '<div class="taxNameHead">' + data.advAdjTaxData[i].taxName + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
                        var taxCellData = '<input type="text" style="width:31px;" class="taxRate" name="mkrfndTaxRate" id="mkrfndTaxRate" readonly="readonly" placeholder="Tax Rate" value="' + data.advAdjTaxData[i].taxRate + '"/>';
                        taxCellData += '<input type="text" style="width:62px;" class="txnTaxAmount" name="mkrfndTaxamnt" id="mkrfndTaxamnt" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);" placeholder="Tax Amount" value="' + data.advAdjTaxData[i].taxAmount + '"/>';
                        taxCellData += '<input type="hidden" class="txnTaxName" name="mkrfndTaxName" id="mkrfndTaxName" value="' + data.advAdjTaxData[i].taxName + '"/>';
                        taxCellData += '<input type="hidden" class="txnTaxID" name="mkrfndTxnTaxID" id="mkrfndTxnTaxID" value="' + data.advAdjTaxData[i].taxid + '"/>';
                        $("#" + parentTr + " div[id='taxCell" + i + "']").addClass('taxCellCls');
                        $("#" + parentTr + " div[id='taxCell" + i + "']").empty();
                        $("#" + parentTr + " div[id='taxCell" + i + "']").append(taxCellData);
                        if (i == 0) {
                            taxTypeAndRate = (data.advAdjTaxData[i].individualTax) + ",";
                        } else {
                            taxTypeAndRate += (data.advAdjTaxData[i].individualTax) + ",";
                        }
                    }
                    $("#" + parentTr + " input[class='txnTaxTypes']").val(taxTypeAndRate);
                    $("#" + parentTr + " div[id='taxCell']").addClass('div-w' + i + '00');
                    $("#" + parentTable + " div[id='taxNameList']").addClass('div-w' + i + '00');
                    $("#" + parentTable + " div[id='taxNameList']").empty();
                    $("#" + parentTable + " div[id='taxNameList']").append(taxNameList);
                }
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on fetching Tax Component!", "Please retry, if problem persists contact support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
            }
        });
    }
    $.unblockUI();
    return returnValue;
}

function validateRefundAdvanceAmount() {
    var valid = false;
    var totalRefund = 0;
    var selectedRefund = 0;
    $("#staticmultipleitemsmkrfnd tbody tr").each(function () {
        var selectedTr = $(this).attr('id');
        var itemNetAdv = $("#" + selectedTr + " input[class='advanceReceived']").val();
        if (itemNetAdv != "") {
            totalRefund += parseFloat(itemNetAdv);
        }
    });
    $("#mkrfndtrid tr .removeTxnCheckBox:checkbox:checked").each(function () {
        var selectedTr = $(this).closest("tr").attr('id');
        var itemNetAdv = $("#" + selectedTr + " input[class='advanceReceived']").val();
        if (itemNetAdv != "") {
            selectedRefund += parseFloat(itemNetAdv);
        }
    });
    var diff = totalRefund - selectedRefund;
    if (diff != 0 && diff > 0) {
        valid = true;
    }

    return valid;
}

var submitForApprovalRefundAdvanceReceived = function (whatYouWantToDo, whatYouWantToDoVal, parentTr) {
    var sourceGstin = "";
    var destinGstin = "";
    // var openingBalAdvId = $("#openingBalAdvId").val();
    var txnGstCountryCode = $("#gstCountryCode").val();
    var txnTypeOfSupply = "";
    var txnWalkinCustomerType = "";
    if (txnGstCountryCode !== "" || typeof txnGstCountryCode != 'undefined' || txnGstCountryCode !== null) {
        txnTypeOfSupply = $("#" + parentTr + " select[class='txnTypeOfSupply']").val();
        if (txnTypeOfSupply == "" || txnTypeOfSupply == null) {
            swal("Invalid Type of Supply!", "Please select valid Type of Supply.", "error");
            return false;
        }
        sourceGstin = $("#" + parentTr + " select[class='txnBranches']").children(":selected").attr("id");
        if (sourceGstin === null || sourceGstin === "") {
            swal("Invalid Branch's GSTIN", "Please select valid Branch.", "error");
            enableTransactionButtons();
            return false;
        }
        destinGstin = $("#" + parentTr + " select[class='placeOfSply txnDestGstinCls']").val();

        if (txnTypeOfSupply != 3) {
            if (destinGstin === null || destinGstin === "") {
                swal("Invalid Place of Supply!", "Please provide valid Place of Supply.", "error");
                enableTransactionButtons();
                return false;
            }
        }
    }

    var txnForBranch = $("#" + parentTr + " select[class='txnBranches'] option:selected").val();
    var txnForCustomer = $("#" + parentTr + " .masterList option:selected").val();
    var txnRemarks = $("#" + parentTr + " textarea[name='mkrfndRemarks']").val();

    var txnRemarksPrivate = $("#" + parentTr + "  textarea[name='mkrfndRemarksPrivate']").val();
    var txnInvoice = $("#" + parentTr + " select[class='salesExpenseTxns'] option:selected").val();
    var txnWithWithoutTax = $("#" + parentTr + " select[class='txnWithWithoutTaxCls'] option:selected").val();
    var txnCustomerDiscountAvailable;
    var txnForItem;
    txnForItem = convertTableDataToArray("multipleItemsTablemkrfnd");
    var supportingDocTmp = $("#" + parentTr + " select[class='txnUploadSuppDocs'] option").map(function () {
        if ($(this).val() != "") {
            return $(this).val();
        }
    }).get();
    var supportingDoc = supportingDocTmp.join(',');
    //var txnpaymentdetails = $("#"+parentTr+" select[id='mkrfndpaymentdetail'] option:selected").val();
    //var txnReceiptTypeBankDetails= $("#"+parentTr+" textarea[name='paymentBranchBankAccount']").val();
    var totalRefund = 0;

    var txnNetTDS = 0;
    $("#staticmultipleitemsmkrfnd tbody tr").each(function () {
        var selectedTr = $(this).attr('id');
        var itemNetAdv = $("#" + selectedTr + " input[class='advanceReceived']").val();
        var itemNetTax = $("#" + selectedTr + " input[class='taxAdjusted']").val();
        if (itemNetAdv != "") {
            totalRefund += parseFloat(itemNetAdv);
            if (itemNetAdv != "") {
                txnNetTDS += parseFloat(itemNetTax);
            }
        }
    });
    var txnNetAmount = totalRefund;
    var netAmountTotalWithDecimalValue = totalRefund;
    if (txnNetAmount <= 0.0) {
        swal("Incomplete Transction detail!", "Please provide complete transaction details befor submitting for approval.", "error");
        enableTransactionButtons();
        return true;
    }
    // SingleUSer
   
    var txnEntityID = $("#" + parentTr).attr("name");
    var txnJsonData = {};
    txnJsonData.txnPurpose = whatYouWantToDo;
    txnJsonData.txnPurposeVal = whatYouWantToDoVal;
    txnJsonData.txnforbranch = txnForBranch;
    txnJsonData.txnforitem = txnForItem;
    txnJsonData.txnforcustomer = txnForCustomer;
    txnJsonData.txnInvoice = txnInvoice;
    txnJsonData.txnnetAdvance = txnNetAmount;
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
    txnJsonData.txnWithWithoutTax = txnWithWithoutTax;
    // txnJsonData.openingBalAdvId = openingBalAdvId;
	if(READ_PAYMODE_ON_APPROVAL == IS_READ_PAYMODE_ON_APPROVAL){
	    var returnVal = getReceiptPaymentDetails(txnJsonData, parentTr);
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
    var url = "/transaction/submitForApproval";
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
        }
    });
}
