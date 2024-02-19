function populateBuyTranData(argument) {
    clearMultiItemsCurrentTrData(argument);
    $(".actualbudgetDisplay").text("");
    $(".budgetDisplay").text("");
    $(".amountRangeLimitRule").text("");

	if(DUPLICATE_ITEMS_ALLOWED_FOR_TXN == 0){
		var returnValue = validateSelectedItems(argument);
		if (returnValue === false) {
		    return false;
		}
    }
	if (GST_COUNTRY_CODE !== "" && GST_COUNTRY_CODE !== undefined && GST_COUNTRY_CODE !== null) {
        returnValue = validateGstItemsForCategory(argument);
        if (returnValue === false) {
            return false;
        }
    }

    returnValue = showTransactionBranchKnowledgeLiabrary(argument);
    if (returnValue === false) {
        return false;
    }
    returnValue = getAdvanceDiscount(argument);
    if (returnValue === false) {
        return false;
    }
    returnValue = calculateGross(argument);
    if (returnValue === false) {
        return false;
    }

    returnValue = getReserveChargesRCMData(argument);
    if (returnValue === false) {
        return false;
    }
    calculateNetAmount(argument);
    txnBuyItemChangesForCompositionScheme(argument);

    returnValue = barcodeFetch(argument);
    if (returnValue === false) {
        return false;
    }
}

var BRANCH_INGSTTAXES_LIST_GLOBAL = "";
var BRANCH_INCESSTAXES_LIST_GLOBAL = "";
var fetchBranchInTaxesCess = function (elem) {
    var parentOfParentTr = $(elem).closest('div').attr('id');
    //var txnBranchId=$(elem).val();
    var txnBranchId = $("#" + parentOfParentTr + " select[class='txnBranches'] option:selected").val();
    if (txnBranchId == "" || typeof txnBranchId == "undefined") {
        swal("Invalid Branch!", "Please select a valid branch.", "error");
        $(elem).val("");
        return false;
    }
    var jsonData = {};
    var transPurposeId = $("#whatYouWantToDo").find('option:selected').val();
    jsonData.useremail = $("#hiddenuseremail").text();
    jsonData.txnBranch = txnBranchId;
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var url = "/tax/getintaxes/" + txnBranchId;
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        async: true,
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            if (transPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId == BUY_ON_CREDIT_PAY_LATER || transPurposeId == BUY_ON_PETTY_CASH_ACCOUNT || transPurposeId == CREDIT_NOTE_VENDOR || transPurposeId == DEBIT_NOTE_VENDOR) {
                var parentTr = $("#" + parentOfParentTr + " table[class='multipleItemsTable'] > tbody > tr:last").attr('id');
                $("#" + parentTr + " select[class='txnGstTaxRate']").children().remove();
                $("#" + parentTr + " select[class='txnGstTaxRate']").append('<option value="">Select</option>');
                $("#" + parentTr + " select[class='txnCessRate']").children().remove();
                $("#" + parentTr + " select[class='txnCessRate']").append('<option value="">Select</option>');
                var intaxList = "";
                var cessTaxList = "";
                for (var i = 0; i < data.inTaxRateList.length; i++) {
                    if (data.inTaxRateList[i].taxtype == "12") {
                        intaxList += '<option value="' + data.inTaxRateList[i].id + '">' + data.inTaxRateList[i].rate + '</option>';
                    } else if (data.inTaxRateList[i].taxtype == "13") {
                        cessTaxList += '<option value="' + data.inTaxRateList[i].id + '">' + data.inTaxRateList[i].rate + '</option>';
                    }
                }
                $("#" + parentTr + " select[class='txnGstTaxRate']").append(intaxList);
                $("#" + parentTr + " select[class='txnCessRate']").append(cessTaxList);
                BRANCH_INGSTTAXES_LIST_GLOBAL = intaxList;
                BRANCH_INCESSTAXES_LIST_GLOBAL = cessTaxList;
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching taxes!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function calculateNetAmount(elem) {
    var parentTr = $(elem).closest('tr').attr('id');
    var txnTotalInputTaxes = $("#totalInputTaxes").val();
    $("#" + parentTr + " input[name='inputtaxesamt']").val(txnTotalInputTaxes);
    let parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
    var txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val();
    if (GST_COUNTRY_CODE == "") {
        calculateBuyNetAmount(elem, parentTr, parentOfparentTr);
        calculateNetAmountWhenTaxAmtChangedForBuy(elem);
        let netAmountTotal = calculateTotalNetForMultipleItemsTable(elem, parentOfparentTr);
        $("#" + parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));
    } else if (GST_COUNTRY_CODE !== "" && (txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR)) {
        //parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
        calculateBuyGstTaxNetAmount(elem, parentTr, parentOfparentTr);
    }
}

var calculateBuyGstTaxNetAmount = function (elem, parentTr, parentOfparentTr) {
    var returnValue = true;
    var txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val();
    var txnBranch = $("#" + parentOfparentTr + " select[class='txnBranches'] option:selected").val();
    var txnitemSpecifics = $("#" + parentTr + " .txnItems").val();
    var txnGrossAmount = $("#" + parentTr + " input[class='txnGross']").val();
    if (txnGrossAmount == "" || txnGrossAmount == "0") {
        //swal("Incomplete transaction detail!", "Please provide price/unit to calculate Gross Amount.", "error");
        $("#" + parentTr + " input[class='netAmountVal']").val(0);
        $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").html("");
        $("#" + parentOfparentTr + " input[class='netAmountValTotal']").val("");
        returnValue = false;
    }
    var txnUnavailable = $("#" + parentOfparentTr + " input[class='unavailable ui-autocomplete-input']").val();
    var txnVendorCustomer = $("#" + parentOfparentTr + " .masterList option:selected").val();
    var txnGstTaxID = "";
    var txnDutiesAndTaxes = "0";
    if (txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR) {
        txnGstTaxID = $("#" + parentTr + " input[id='cdtdbvTxnTaxID']").val();
    } else {
        txnGstTaxID = $("#" + parentTr + " select[class='txnGstTaxRate'] option:selected").val();
    }
    var txnGstTaxRate = $("#" + parentTr + " select[class='txnGstTaxRate'] option:selected").text();
    var txnCessID = $("#" + parentTr + " select[class='txnCessRate'] option:selected").val();
    var txnAdjustmentAmount = $("#" + parentTr + " input[class='howMuchAdvance']").val();
    var txnTypeOfSupply = $("#" + parentOfparentTr + " select[class='txnTypeOfSupply'] option:selected").val();
    var withholdingtaxInput = $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val();
    if (txnGstTaxID == "") {
        txnGstTaxRate = "";
        $("#" + parentTr + " .txnTaxAmount").val('');
        $("#" + parentTr + " .txnTaxName").val('');
        $("#" + parentTr + " .txnTaxID").val('');
        $("#" + parentTr + " .taxRate").val('');
    }

    if(txnCessID == ""){
        $("#" + parentTr + " .txnCessTaxAmt").val('');
    }

    if (txnBranch == "") {
        swal("Incomplete transaction detail!", "Please select a Branch.", "error");
        returnValue = false;
    }
    if (txnTypeOfSupply == "") {
        swal("Incomplete transaction detail!", "Please select a Type of Supply.", "error");
        returnValue = false;
    }
    if (txnVendorCustomer == "" && txnUnavailable == "") {
        swal("Incomplete transaction detail!", "Please select customer or provide customer name.", "error");
        returnValue = false;
    }
    if (txnitemSpecifics == "") {
        swal("Incomplete transaction detail!", "Please select a Item for transaction.", "error");
        returnValue = false;
    }

    if ((txnGrossAmount != "" || txnGrossAmount != "0") && (withholdingtaxInput != "" || withholdingtaxInput != "0")) {
        if (parseFloat(withholdingtaxInput) > parseFloat(txnGrossAmount)) {
            $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val("");
            swal("Invalid TDS Amount!", "TDS Amount must be less than Gross Amount", "error");
            returnValue = false;
        }
    }
    if (txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT) {
        if (txnTypeOfSupply == "2" || txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5") {
            calculateBuyRCMTaxNetAmount(elem, parentTr, parentOfparentTr, txnGrossAmount, txnAdjustmentAmount, txnTypeOfSupply, txnVendorCustomer);
            return true;
        } else if (txnTypeOfSupply !== "1") {
            txnDutiesAndTaxes = $("#" + parentTr + " input[class='txnDutiesAndTaxes']").val();
            if (txnGrossAmount !== "") {
                txnGrossAmount = parseFloat(txnGrossAmount) + parseFloat(txnDutiesAndTaxes);
            }
        }
    } else if (txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR) {
        if (txnTypeOfSupply !== "1") {
            var newTxnDutiesAndTaxes = $("#" + parentTr + " input[class='txnDutiesAndTaxes']").val();
            var oldTxnDutiesAndTaxes = $("#" + parentTr + " input[class='txnDutiesAndTaxesHid']").val();

            if (oldTxnDutiesAndTaxes != "" && newTxnDutiesAndTaxes != "" && typeof newTxnDutiesAndTaxes != 'undefined' && typeof oldTxnDutiesAndTaxes != 'undefined') {
                if (parseFloat(oldtxnDutiesAndTaxes) > parseFloat(newTxnDutiesAndTaxes)) {
                    txnDutiesAndTaxes = parseFloat(oldTxnDutiesAndTaxes) - parseFloat(newTxnDutiesAndTaxes);
                } else {
                    txnDutiesAndTaxes = parseFloat(newTxnDutiesAndTaxes) - parseFloat(oldTxnDutiesAndTaxes);
                }
            }
            if (parseFloat(txnGrossAmount) > 0) {
                txnGrossAmount = parseFloat(txnGrossAmount) + parseFloat(txnDutiesAndTaxes);
            }
        }
    }
    var sourceGstinCode = $("#" + parentOfparentTr + " select[class='txnBranches']").children(":selected").attr("id");
    var destGstinCode = $("#" + parentOfparentTr + " select[class='placeOfSply txnDestGstinCls']").val();
    var destVendorDetailId = $("#" + parentOfparentTr + " select[class='placeOfSply txnDestGstinCls']").children(":selected").attr("id");
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
    if (returnValue) {
        var followedkl = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
        if (typeof followedkl != 'undefined') {
            var klfollowednotfollowed = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").is(':checked');
            if (klfollowednotfollowed == false) {
                swal("Transaction knowledge library!", "Please follow mandatory knowledge library before proceeding.", "error");
                $(elem).val('');
                returnValue = false;
            }
        }
    }
    var err = checkAmountRangeLimitForTransactionCreator(elem, txnGrossAmount, parentOfparentTr);
    if (err == true) {
        returnValue = false;
    }
    var txnDate = $("#" + parentOfparentTr).find(".txnBackDate").val();
    if (returnValue === true) {
        var jsonData = {};
        jsonData.useremail = $("#hiddenuseremail").text();
        jsonData.txnBranchId = txnBranch;
        jsonData.txnSpecificsId = txnitemSpecifics;
        jsonData.txnGrossAmt = txnGrossAmount;
        jsonData.txnPurposeValue = txnPurposeVal;
        jsonData.txnAdjustmentAmount = txnAdjustmentAmount;
        jsonData.txnGstTaxID = txnGstTaxID;
        jsonData.txnCessID = txnCessID;
        jsonData.txnGstTaxRate = txnGstTaxRate;
        jsonData.txnSelectedVendorCustomer = txnVendorCustomer;
        jsonData.txnSourceGstinCode = sourceGstinCode;
        jsonData.txnDestGstinCode = destGstinCode;
        jsonData.txnDestVendorDetailId = destVendorDetailId;
        jsonData.txnTypeOfSupply = txnTypeOfSupply;
        jsonData.txnDate = txnDate;
        var url = "/transaction/gettaxandnetamt";
        $.ajax({
            url: url,
            data: JSON.stringify(jsonData),
            type: "text",
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            method: "POST",
            contentType: 'application/json',
            success: function (data) {
                if (typeof data.tdsPayableSpecific != 'undefined' && data.tdsPayableSpecific == 0) {
                    swal("COA: mapping missing", "Chart of Account, TDS Payable mapping is not defined, please define and try.", "error");
                    $(elem).val('');
                    initMultiItemsSelect2();
                    disableTransactionButtons();
                    returnValue = false;
                }
                if (data.taxCalculateStatusFalse == false) {
                    $("#" + parentTr + " select[class='txnGstTaxRate']").attr('disabled', 'disabled');
                    $("#" + parentTr + " select[class='txnCessRate']").attr('disabled', 'disabled');
                } else {
                    $("#" + parentTr + " select[class='txnGstTaxRate']").removeAttr('disabled');
                    $("#" + parentTr + " select[class='txnCessRate']").removeAttr('disabled');
                }
                $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").text("");
                var withholdingAmount = 0;
                $("#" + parentTr).attr("withholdingtaxRate", "");
                $("#" + parentTr).attr("tdstxnlimit", "");
                for (var i = 0; i < data.branchTdsDetail.length; i++) {
                    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Rate: ' + data.branchTdsDetail[i].withholdingtaxRate + ", ");
                    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Limit: ' + data.branchTdsDetail[i].withholdingtaxLimit + ", ");
                    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Withholding Monetory Limit: ' + data.branchTdsDetail[i].withHoldingMonetoryLimit + ",");
                    withholdingAmount = data.branchTdsDetail[i].withholdingtaxTotalAmount;
                    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Withholding Tax: ' + parseFloat(withholdingAmount) + '(-), ');
                    $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val((withholdingAmount * 1).toFixed(2));
                    $("#" + parentTr + " input[class='txnLeftOutWithholdTransIDs']").val(data.branchTdsDetail[i].leftOutTransactionIDList);
                    $("#" + parentTr).attr("withholdingtaxRate", data.branchTdsDetail[i].withholdingRate);
                    $("#" + parentTr).attr("tdstxnlimit", data.branchTdsDetail[i].withholdingtaxLimit);
                }
                if (data.hasOwnProperty('modeOfTdsCompute') || txnVendorCustomer === "") {
                    $("#" + parentTr).attr("modeOfTDSCompute", data.modeOfTdsCompute);
                    $("#" + parentTr).attr("specficIsTDSSpec", data.specficIsTDSSpec);

                    if (data.modeOfTdsCompute == 2 || txnVendorCustomer == "") {
                        $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").removeAttr('readonly');
                    } else {
                        //$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").attr('readonly','readonly');
                    }
                }
                $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").removeAttr('readonly');
                var taxTotalAmount = parseFloat(data.taxTotalAmount);
                var netAmount = parseFloat(data.txnNetAmount);
                var taxNameList = "";
                var countTaxes = 0;
                var taxTypeAndRate = "";
                $("#" + parentTr + " div[class='buyTaxCellCls']").empty();
                if (txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR) {
                    for (var i = 0; i < data.branchTaxDetail.length; i++) {
                        if (data.branchTaxDetail[i].taxName == "CESS") {
                            $("#" + parentTr + " input[class='txnCessTaxAmt']").val(data.branchTaxDetail[i].taxAmount);
                            var taxCellData = '<input type="hidden" class="taxRate" name="cdtdbvTaxRate" id="cdtdbvTaxRate" readonly="readonly" value="' + data.branchTaxDetail[i].taxRate + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxAmount" name="txnTaxAmount" id="txnTaxAmount" readonly="readonly" value="' + data.branchTaxDetail[i].taxAmount + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxName" name="cdtdbvTaxName" id="cdtdbvTaxName" value="' + data.branchTaxDetail[i].taxName + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxID" name="cdtdbvTxnTaxID" id="cdtdbvTxnTaxID" value="' + data.branchTaxDetail[i].taxid + '"/>';
                            $("#" + parentTr + " div[id='taxCell" + i + "']").empty();
                            $("#" + parentTr + " div[id='taxCell" + i + "']").append(taxCellData);

                        } else {
                            countTaxes = countTaxes + 1;
                            taxNameList += '<div class="taxNameHead">' + data.branchTaxDetail[i].taxName + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
                            var taxCellData = '<input type="hidden" class="taxRate" name="cdtdbvTaxRate" id="cdtdbvTaxRate" readonly="readonly" value="' + data.branchTaxDetail[i].taxRate + '"/>';
                            taxCellData += '<input type="text" style="width:62px;" class="txnTaxAmount" name="txnTaxAmount" id="txnTaxAmount" value="' + data.branchTaxDetail[i].taxAmount + '" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);"/>';
                            taxCellData += '<input type="hidden" class="txnTaxName" name="cdtdbvTaxName" id="cdtdbvTaxName" value="' + data.branchTaxDetail[i].taxName + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxID" name="cdtdbvTxnTaxID" id="cdtdbvTxnTaxID" value="' + data.branchTaxDetail[i].taxid + '"/>';
                            $("#" + parentTr + " div[id='taxCell" + i + "']").addClass('buyTaxCellCls');
                            $("#" + parentTr + " div[id='taxCell" + i + "']").empty();
                            $("#" + parentTr + " div[id='taxCell" + i + "']").append(taxCellData);
                            $("#" + parentTr + " div[id='taxCell']").removeAttr('class', 'divbuy-w' + i + '00');
                        }

                        taxTypeAndRate += (data.branchTaxDetail[i].individualTax) + ",";
                        $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").removeAttr('class', 'divbuy-w' + i + '00');
                    }
                } else {
                    for (var i = 0; i < data.branchTaxDetail.length; i++) {
                        if (data.branchTaxDetail[i].taxName == "CESS") {
                            $("#" + parentTr + " input[class='txnCessTaxAmt']").val(data.branchTaxDetail[i].taxAmount);
                            var taxCellData = '<input type="hidden" class="taxRate" name="bocpraTaxRate" id="bocpraTaxRate" readonly="readonly" value="' + data.branchTaxDetail[i].taxRate + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxAmount" name="bocprataxamnt" id="bocprataxamnt" readonly="readonly" value="' + data.branchTaxDetail[i].taxAmount + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxName" name="bocpraTaxName" id="bocpraTaxName" value="' + data.branchTaxDetail[i].taxName + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxID" name="bocpraTxnTaxID" id="bocpraTxnTaxID" value="' + data.branchTaxDetail[i].taxid + '"/>';
                            $("#" + parentTr + " div[id='taxCell" + i + "']").empty();
                            $("#" + parentTr + " div[id='taxCell" + i + "']").append(taxCellData);

                        } else {
                            countTaxes = countTaxes + 1;
                            taxNameList += '<div class="taxNameHead">' + data.branchTaxDetail[i].taxName + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
                            var taxCellData = '<input type="hidden" class="taxRate" name="bocpraTaxRate" id="bocpraTaxRate" readonly="readonly" value="' + data.branchTaxDetail[i].taxRate + '"/>';
                            taxCellData += '<input type="text" style="width:62px;" class="txnTaxAmount" name="bocprataxamnt" id="bocprataxamnt' + countTaxes + '" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);" value="' + data.branchTaxDetail[i].taxAmount + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxName" name="bocpraTaxName" id="bocpraTaxName" value="' + data.branchTaxDetail[i].taxName + '"/>';
                            taxCellData += '<input type="hidden" class="txnTaxID" name="bocpraTxnTaxID" id="bocpraTxnTaxID" value="' + data.branchTaxDetail[i].taxid + '"/>';
                            $("#" + parentTr + " div[id='taxCell" + i + "']").addClass('buyTaxCellCls');
                            $("#" + parentTr + " div[id='taxCell" + i + "']").empty();
                            $("#" + parentTr + " div[id='taxCell" + i + "']").append(taxCellData);
                            $("#" + parentTr + " div[id='taxCell']").removeAttr('class', 'divbuy-w' + i + '00');
                        }

                        taxTypeAndRate += (data.branchTaxDetail[i].individualTax) + ",";
                        $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").removeAttr('class', 'divbuy-w' + i + '00');
                    }
                }


                $("#" + parentTr + " div[id='taxCell']").addClass('divbuy-w' + countTaxes + '00');
                var taxNameHeadDivCount = $("#" + parentOfparentTr + ' .taxNameHead').length;
                if (data.branchTaxDetail.length > parseInt(taxNameHeadDivCount)) {
                    $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").addClass('divbuy-w' + countTaxes + '00');
                    $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").empty();
                    $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").append(taxNameList);
                }
                $("#" + parentTr + " input[class='txnTaxTypes']").val(taxTypeAndRate);

                var netAmount = parseFloat(data.txnNetAmount);
                if (data.hasOwnProperty('modeOfTdsCompute') || txnVendorCustomer === "") {
                    if ((data.modeOfTdsCompute !== 1 || txnVendorCustomer === "") && withholdingtaxInput !== "" && parseFloat(withholdingtaxInput) > 0) {
                        var tdsAmt = parseFloat(withholdingtaxInput);
                        if (netAmount > tdsAmt) {
                            netAmount = netAmount - tdsAmt;
                        } else {
                            $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val("");
                            swal("Invalid TDS Amount!", "TDS Amount must be less than Gross Amount", "error");
                            return;
                        }
                    }
                }
                var taxTotalAmount = parseFloat(data.taxTotalAmount);

                if (txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR) {
                    if (txnDutiesAndTaxes > 0) {
                        if ((txnTypeOfSupply == "2" || txnTypeOfSupply == "3") && (txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal ==  BUY_ON_PETTY_CASH_ACCOUNT)) {
                            netAmount = netAmount - parseFloat(txnDutiesAndTaxes);
                        }
                        if (txnTypeOfSupply !== "1" && (txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR)) {
                            netAmount = netAmount - parseFloat(txnDutiesAndTaxes);
                        }
                    }
                }

                /*if(txnAdjustmentAmount!="" && typeof txnAdjustmentAmount!='undefined'){
	    			$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").append(' Adjustment: '+txnAdjustmentAmount);
				}*/
                if (taxTotalAmount != "" && typeof taxTotalAmount != 'undefined') { //add input taxes to net
                    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append(taxTypeAndRate);
                    $("#" + parentTr + " input[class='itemTaxAmount']").val(taxTotalAmount);
                }
                $("#" + parentTr + " input[class='netAmountVal']").val((netAmount * 1).toFixed(2));
                var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
                $("#" + parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on Net amount calculation!", "Please contact support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
            }
        });
    }
    return returnValue;
}

function calculateNetAmountWhenTaxAmtChangedForBuy(elem) {
    var cessEditFlag = true;
    var txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val();
    var parentTr = $(elem).closest('tr').attr('id');
    var parentOfparentTr = $(elem).closest('.transactionDetailsTable').find(".transaction-create > tbody > tr:first").attr("id");
    var changedTaxVal = $(elem).val();
    var grossAmount = $("#" + parentTr + " input[class='txnGross']").val();
    var sumOfTaxes = 0.0;
    if ($(elem).hasClass("txnCessTaxAmt")) {
        var flag = true;
        $("#" + parentTr + " input[class='txnTaxAmount']").each(function () {
            var taxName = $(this).closest('div').find(".txnTaxName").val();
            if (taxName == "CESS") {
                $(this).val($(elem).val());
            }
        });
        cessEditFlag = false;
    }
    if (txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER) {
        var increaseDecrease = $("#" + parentOfparentTr + " select[class='creditDebitType'] option:selected").val();
        if (increaseDecrease == "") {
            swal("Incomplete transaction data!", "Please select " + whatYouWantToDo + " type", "error");
            $("#" + parentOfparentTr + " select[class='salesExpenseTxns']").change();
            return false;
        }
        var isValid = validateCreditDedbitNote("multipleItemsTablecdtdbt", increaseDecrease);
        if (!isValid) {
            swal("Incomplete transaction data!", "Please change data before submitting for approval", "error");
            $("#" + parentOfparentTr + " select[class='salesExpenseTxns']").change();
            return false;
        }
    }else if (txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR ||
        txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER || txnPurposeVal == REFUND_ADVANCE_RECEIVED) {
        if ($("#" + parentTr + " select[class='txnGstTaxRate'] option:selected").text() != null || $("#" + parentTr + " select[class='txnGstTaxRate'] option:selected").text() != "") {
            $("#" + parentTr + " input[class='txnTaxAmount']").each(function () {
                /*var taxName = $(this).closest('div').find(".txnTaxName").val();
					if(taxName == "CESS") {
						if(cessEditFlag) {
							if($(this).val()!=undefined && $(this).val()!="")
								sumOfTaxes += parseFloat($(this).val());
						}
					}else {*/
                if ($(this).val() != undefined && $(this).val() != "")
                    sumOfTaxes += parseFloat($(this).val());
                //	}

            });
        }
    }

    /*	if(txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR ||
	    txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER || txnPurposeVal == REFUND_ADVANCE_RECEIVED){
		if($("#"+parentTr+" input[class='txnCessTaxAmt']").val() != undefined || $("#"+parentTr+" input[class='txnCessTaxAmt']").val() != null){
			if(!(isNaN(parseFloat($("#"+parentTr+" input[class='txnCessTaxAmt']").val())))){
				sumOfTaxes+=parseFloat($("#"+parentTr+" input[class='txnCessTaxAmt']").val());
			}
		}
	}
*/
    if (txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
        $("#" + parentTr + " input[class='txnTaxOnAdvAdjCls']").each(function () {
            if ($(this).val() != undefined && $(this).val() != "")
                sumOfTaxes += parseFloat($(this).val());
        });
    }

    var grossWithTax = (parseFloat(grossAmount) + parseFloat(sumOfTaxes)).toFixed(2);
    $("#" + parentTr + " input[class='netAmountVal']").val(grossWithTax);
    let netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
    $("#" + parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));
    if (txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER) {
        $("#" + parentTr + " input[class='invoiceValue']").val(grossWithTax);
        $("#" + parentOfparentTr + " input[class='totalInvoiceValue']").val(netAmountTotal);
    }
    // if(txnPurposeVal == REFUND_ADVANCE_RECEIVED) {
    // 		$("#"+parentTr+" input[class='mkrfndadvResultantAdv']").val(grossWithTax);
    // }
    if (sumOfTaxes != "" && typeof sumOfTaxes != 'undefined') { //add input taxes to net
        $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").html("");
        $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Total Taxes: ' + sumOfTaxes);
        $("#" + parentTr + " input[class='itemTaxAmount']").val(sumOfTaxes);
    }
    //change
    setTaxDescriptionOnTaxChange(elem);
}

function setTaxDescriptionOnTaxChange(elem) {
    var parentTr = $(elem).closest('tr').attr('id');
    var taxName = "";
    if ($(elem).hasClass("txnCessTaxAmt")) {
        taxName = "CESS";
    } else {
        taxName = $(elem).closest('div').find(".txnTaxName").val();
    }

    var value = $(elem).val();
    var taxTypeAndRate = $("#" + parentTr + " input[class='txnTaxTypes']").val();
    var taxTypeAndRateNew = "";
    var taxTypeAndRateArray = taxTypeAndRate.split(',');
    for (var j = 0; j < taxTypeAndRateArray.length; j++) {
        if (taxName != "" && taxTypeAndRateArray[j] != "") {
            if (taxTypeAndRateArray[j].startsWith(taxName)) {
                var taxAray = taxTypeAndRateArray[j].split(':');
                if (taxAray.length > 1) {
                    var newTax = "";
                    newTax += taxAray[0] + ":";
                    newTax += value;
                    taxTypeAndRateArray[j] = newTax;
                }
            }
        }
    }
    for (var j = 0; j < taxTypeAndRateArray.length; j++) {
        if (taxTypeAndRateArray[j] != "") {
            taxTypeAndRateNew += taxTypeAndRateArray[j] + ",";
        }
    }
    $("#" + parentTr + " input[class='txnTaxTypes']").val(taxTypeAndRateNew);
    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").html("");
    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append(taxTypeAndRateNew);
}

function calculateNetAmountWhenAdvanceChangedForBuy(elem) {

    clearTimeout(typingTimer);
    var returnVal = whenAdvanceIsEmpty(elem);
    if (returnVal === false) {
        return false;
    }

    var parentTr = $(elem).closest('tr').attr('id');
    var parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
    let adjustmentAmount = $("#" + parentTr + " input[class='howMuchAdvance']").val();

    var txnVendorCustomer = $("#" + parentOfparentTr + " .masterList").val();
    var customerAdvance = $("#" + parentTr + " input[class='customerAdvance']").val();
    if (customerAdvance != "" && adjustmentAmount != "" && parseFloat(adjustmentAmount) > parseFloat(customerAdvance)) {
        $("#" + parentTr + " input[class='howMuchAdvance']").val('');
        swal("Exceeding data limit!", "Advance adjustment cannot be more than advance available.", "error");
        adjustmentAmount = "";
        //return false; Due to these stmt, it is not recalculating net amt
    }

    if (txnVendorCustomer == "" && adjustmentAmount != "" && parseFloat(adjustmentAmount) > 0) {
        $("#" + parentTr + " input[class='howMuchAdvance']").val('');
        swal("Invalid advance adjustment!", "advance adjustment is not applicable for walk-in vendor.", "error");
        adjustmentAmount = "";

    }
    var txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val();

    var gross = $("#" + parentTr + " input[class='txnGross']").val();
    var netAmount = parseFloat(gross);

    var itemTaxAmount = $("#" + parentTr + " input[class='itemTaxAmount']").val();
    var txnTypeOfSupply = $("#" + parentOfparentTr + " select[class='txnTypeOfSupply']").val();
    if (itemTaxAmount != "") {
        if (txnTypeOfSupply == "" || txnTypeOfSupply == 1) {
        	netAmount = (parseFloat(netAmount) + parseFloat(itemTaxAmount));
        }
    }
    var whTax = $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val();
    if (whTax != "") {
        netAmount = (parseFloat(netAmount) - parseFloat(whTax));
    }

    if (parseFloat(adjustmentAmount) > parseFloat(netAmount)) {
        $("#" + parentTr + " input[class='howMuchAdvance']").val('');
        swal("Exceeding data limit!", "Advance adjustment cannot be more than net amount.", "error");
        adjustmentAmount = "";
        //return false; Due to these stmt, it is not recalculating net amt
    } else if (gross != "" && adjustmentAmount != "") {
        netAmount = netAmount - parseFloat(adjustmentAmount);
    }

    if (netAmount < 0) {
        $("#" + parentTr + " input[class='howMuchAdvance']").val('');
        swal("Invalid advance adjustment!", "advance adjustment cannot be more than net amount.", "error");
        netAmount = parseFloat(gross);
    }
    $("#" + parentTr + " input[class='netAmountVal']").val((netAmount * 1).toFixed(2));
    let netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
    $("#" + parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));
    /*if(adjustmentAmount!="" && typeof adjustmentAmount!='undefined'){
		$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").append(' Adjustment: '+adjustmentAmount);
	}*/
}

var submitForApprovalBuyTrans = function (whatYouWantToDo, whatYouWantToDoVal, parentTr) {
    var txnForBranch = "";
    var txnForProject = "";
    var txnForItem = "";
    var txnForCustomer = "";
    var txnForUnavailableCustomer = "";
    var txnNoOfUnits = "";
    var txnPricePerUnit = "";
    var txnGross = "";
    var txnCustomerAdvanceIfAny = "";
    var txnCustomerAdvanceAdjustment = "";
    var txnCustomerDiscountAvailable = "";
    var txnNetAmount = "";
    var txnNetAmountDescription = "";
    var txnReceiptDetails = "";
    var klmandatoryfollowednotfollowed = "";
    var userTxnAmountLimit = "";
    var userTxnBudget = "";
    var userTxnActBud = "";
    var txnReceiptTypeBankDetails = "";
    var txnRemarks = "";
    var supportingDoc = "";
    var inputTaxNames = "";
    var inputTaxValues = "";
    var procurementRequest = "";
    var procurementRequestRemarks = "";
    var withholdingTaxValues = "";
    var txnFrieghtCharges = "";
    var txnLeftOutWithholdTransIDs = "";
    var vendorAdvanceType = "";
    var netAmountTotalWithDecimalValue = "";
    var txnPOInvoiceTxnRef = "";
    var txnPlaceOfSupply = "";
    let parentOfparentTr = $("#"+parentTr).closest('div').attr('id');
    if (whatYouWantToDoVal == BUY_ON_CASH_PAY_RIGHT_AWAY) {
        txnForBranch = $("#" + parentTr + " select[class='txnBranches'] option:selected").val();
        txnForProject = $("#" + parentTr + " select[class='txnForProjects'] option:selected").val();
        txnForCustomer = $("#" + parentTr + " .masterList option:selected").val();
        txnPOInvoice = $("#" + parentTr + " select[id='bocpraInvoices'] option:selected").val();//purchaseOrder transaction id in Transaciton table
        txnPOInvoiceTxnRef = $("#" + parentTr + " select[class='salesExpenseTxns'] option:selected").text();
        txnPlaceOfSupply = $("#" + parentTr + " select[id='bocpraPlaceOfVend'] option:selected").attr("id"); //VendorDetail Id in VendorDetail table
        txnForUnavailableCustomer = $("#" + parentTr + " input[class='unavailable']").val();
        txnNetAmount = $("#" + parentTr + " input[class='netAmountValTotal']").val();
        var isValid = validateMultiItemsTransaction("multipleItemsTablebocpra");
        if (!isValid) {
            swal("Incomplete transaction data!", "Please provide complete transaction details befor submitting for accounting", "error");
            enableTransactionButtons();
            return true;
        }
        txnForItem = convertTableDataToArray("multipleItemsTablebocpra");
        netAmountTotalWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
        if (txnNetAmount == "") {
            swal("Incomplete Transction detail!", "Please provide complete transaction details befor submitting for approval.", "error");
            enableTransactionButtons();
            return true;
        }

        //txnForBranch=$("#bocpraTxnForBranches option:selected").val();
        //txnForProject=$("#bocpraTxnForProjects option:selected").val();
        //txnForItem=$("#bocpraItem option:selected").val();
        //userTxnBudget=$("#"+parentTr+" div[class='budgetDisplay']").text();
        //userTxnActBud=$("#"+parentTr+" div[class='actualbudgetDisplay']").text()
        //userTxnAmountLimit=$("#"+parentTr+" div[class='amountRangeLimitRule']").text();
        //txnForCustomer=$("#bocpraVendor option:selected").val();
        //txnForUnavailableCustomer=$("#bocpraUnAvailableVendor").val();
        //txnNoOfUnits=$("#bocpraunits").val();
        //txnPricePerUnit=$("#bocprapriceperunits").val();
        //txnFrieghtCharges=$("#bocpraFrieghtCharges").val();
        //txnGross=$("#bocpragross").val();
        //vendorAdvanceType=$("#bocpravendoradvanceType option:selected").val();
        /*txnCustomerAdvanceIfAny=$("#bocpravendoradvance").val();
		txnCustomerAdvanceAdjustment=$("#bocprahowmuchfromadvance").val();
		txnNetAmount=$("#bocpranetamnt").val();
		txnNetAmountDescription=$("#bocpranetAmountLabel").text();
		txnReceiptDetails=$("#bocprapaymentdetail option:selected").val();
		txnReceiptTypeBankDetails=$("#"+parentTr+" textarea[id='paymentBranchBankAccount']").val();*/
        txnRemarks = $("#bocpraRemarks").val();
        var supportingDocTmp = $("#" + parentTr + " select[class='txnUploadSuppDocs'] option").map(function () {
            if ($(this).val() != "") {
                return $(this).val();
            }
        }).get();
        supportingDoc = supportingDocTmp.join(',');
        //procurementRequest=$("#"+parentTr+" select[name='procurementRequestForCreator'] option:selected").val();
        procurementRequestRemarks = $("#" + parentTr + " div[name='procurementRequestRemarks']").text();
        var followedkl = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
        if (typeof followedkl != 'undefined') {
            if ($("#" + parentTr + " div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked') == true) {
                klmandatoryfollowednotfollowed = "1";
            }
            if ($("#" + parentTr + " div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked') == true) {
                klmandatoryfollowednotfollowed = "0";
            }
        }
        //var inputTaxAmt=$("#bocpratrid input[name='inputtaxesamt']").attr('id');

        /*var inputTaxAmt=$("#inputtaxes").val();
		if(typeof inputTaxAmt!='undefined'){
			inputTaxNames=$('select[name="bocprainputtaxcomponenet"] option:selected').map(function () {
				return this.text;
			}).get();
			inputTaxValues=$('input[name="bocprainputtaxcomponenetamt"]').map(function () {
		 		return this.value;
		 	}).get().toString();
		}

		var inputTaxIdList="";
		for (var i=0;i<inputTaxNames.length;i++){
			if(inputTaxNames[i]!=""){
				inputTaxIdList+=inputTaxNames[i]+",";
			}else{
				inputTaxIdList+=" "+",";
			}
		}
		inputTaxNames=inputTaxIdList;*/
        //withholdingTaxValues=$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").val();
        txnLeftOutWithholdTransIDs = $("#" + parentTr + " input[class='txnLeftOutWithholdTransIDs']").val();
    } else if (whatYouWantToDoVal == BUY_ON_CREDIT_PAY_LATER) {
        txnForBranch = $("#" + parentTr + " select[class='txnBranches'] option:selected").val();
        txnForProject = $("#" + parentTr + " select[class='txnForProjects'] option:selected").val();
        txnForCustomer = $("#" + parentTr + " .masterList option:selected").val();
        txnPOInvoice = $("#" + parentTr + " select[id='bocaplInvoices'] option:selected").val();//purchaseOrder transaction id in Transaciton table
        txnPOInvoiceTxnRef = $("#" + parentTr + " select[class='salesExpenseTxns'] option:selected").text();
        //txnPlaceOfSupply=$("#"+parentTr+" select[id='bocpraPlaceOfVend'] option:selected").attr("id"); //VendorDetail Id in VendorDetail table
        txnForUnavailableCustomer = $("#" + parentTr + " input[class='unavailable']").val();
        txnNetAmount = $("#" + parentTr + " input[class='netAmountValTotal']").val();
        var isValid = validateMultiItemsTransaction("multipleItemsTablebocapl");
        if (!isValid) {
            swal("Incomplete transaction data!", "Please provide complete transaction details befor submitting for accounting", "error");
            enableTransactionButtons();
            return true;
        }
        txnForItem = convertTableDataToArray("multipleItemsTablebocapl");
        netAmountTotalWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
        if (txnNetAmount == "") {
            swal("Incomplete Transction detail!", "Please provide complete transaction details befor submitting for approval.", "error");
            enableTransactionButtons();
            return true;
        }

        /*txnForBranch=$("#bocaplTxnForBranches option:selected").val();
		txnForProject=$("#bocaplTxnForProjects option:selected").val();
		txnForItem=$("#bocaplItem option:selected").val();
		userTxnBudget=$("#"+parentTr+" div[class='budgetDisplay']").text();
		userTxnActBud=$("#"+parentTr+" div[class='actualbudgetDisplay']").text()
		userTxnAmountLimit=$("#"+parentTr+" div[class='amountRangeLimitRule']").text();
		txnForCustomer=$("#bocaplVendor option:selected").val();
		txnForUnavailableCustomer=$("#bocaplUnAvailableVendor").val();
		txnNoOfUnits=$("#bocaplunits").val();
		txnPricePerUnit=$("#bocaplpriceperunits").val();
		txnFrieghtCharges=$("#bocaplFrieghtCharges").val();
		txnGross=$("#bocaplgross").val();
		vendorAdvanceType=$("#bocaplvendoradvanceType option:selected").val();
		txnCustomerAdvanceIfAny=$("#bocaplvendoradvance").val();
		txnCustomerAdvanceAdjustment=$("#bocaplhowmuchfromadvance").val();
		txnNetAmount=$("#bocaplnetamnt").val();
		txnNetAmountDescription=$("#bocaplnetAmountLabel").text();*/
        txnRemarks = $("#bocaplRemarks").val();
        var supportingDocTmp = $("#" + parentTr + " select[class='txnUploadSuppDocs'] option").map(function () {
            if ($(this).val() != "") {
                return $(this).val();
            }
        }).get();
        supportingDoc = supportingDocTmp.join(',');
        //procurementRequest=$("#"+parentTr+" select[name='procurementRequestForCreator'] option:selected").val();
        procurementRequestRemarks = $("#" + parentTr + " div[name='procurementRequestRemarks']").text();
        var followedkl = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
        if (typeof followedkl != 'undefined') {
            if ($("#" + parentTr + " div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked') == true) {
                klmandatoryfollowednotfollowed = "1";
            }
            if ($("#" + parentTr + " div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked') == true) {
                klmandatoryfollowednotfollowed = "0";
            }
        }
        /*var inputTaxAmt=$("#bocapltrid input[name='inputtaxesamt']").attr('id');
		if(typeof inputTaxAmt!='undefined'){
			inputTaxNames=$('select[name="bocaplinputtaxcomponenet"] option:selected').map(function () {
				return this.text;
			}).get();

			inputTaxValues=$('input[name="bocaplinputtaxcomponenetamt"]').map(function () {
		 		return this.value;
		 	}).get().toString();
		}
		var inputTaxIdList="";
		for (var i=0;i<inputTaxNames.length;i++){
			if(inputTaxNames[i]!=""){
				inputTaxIdList+=inputTaxNames[i]+",";
			}else{
				inputTaxIdList+=" "+",";
			}
		}
		inputTaxNames=inputTaxIdList;*/
        //withholdingTaxValues=$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").val();
        txnLeftOutWithholdTransIDs = $("#" + parentTr + " input[class='txnLeftOutWithholdTransIDs']").val();
    }/*else if(whatYouWantToDoVal== BUY_ON_PETTY_CASH_ACCOUNT){
		txnForBranch=$("#bptycaTxnForBranches option:selected").val();
		txnForProject=$("#bptycaTxnForProjects option:selected").val();
		txnForItem=$("#bptycaItem option:selected").val();
		userTxnBudget=$("#"+parentTr+" div[class='budgetDisplay']").text();
		userTxnActBud=$("#"+parentTr+" div[class='actualbudgetDisplay']").text()
		userTxnAmountLimit=$("#"+parentTr+" div[class='amountRangeLimitRule']").text();
		txnForCustomer=$("#bptycaVendor option:selected").val();
		txnForUnavailableCustomer=$("#bptycaUnAvailableVendor").val();
		txnNoOfUnits=$("#bptycaunits").val();
		txnPricePerUnit=$("#bptycapriceperunits").val();
		txnFrieghtCharges=$("#bptycaFrieghtCharges").val();
		txnGross=$("#bptycagross").val();
		txnCustomerAdvanceIfAny=$("#bptycavendoradvance").val();
		txnCustomerAdvanceAdjustment=$("#bptycahowmuchfromadvance").val();
		txnNetAmount=$("#bptycanetamnt").val();
		txnNetAmountDescription=$("#bptycanetAmountLabel").text();
		txnReceiptDetails=$("#bptycapaymentdetail option:selected").val();
		txnReceiptTypeBankDetails=$("#"+parentTr+" textarea[id='paymentBranchBankAccount']").val();
		txnRemarks=$("#bptycaRemarks").val();
		supportingDoc=$('select[name*="bptycauploadSuppDocs"]').map(function () {
	 		return this.value;
	 	}).get().toString();
		procurementRequest=$("#"+parentTr+" select[name='procurementRequestForCreator'] option:selected").val();
		procurementRequestRemarks=$("#"+parentTr+" div[name='procurementRequestRemarks']").text();
		var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
				klmandatoryfollowednotfollowed="1";
			}
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
				klmandatoryfollowednotfollowed="0";
			}
		}
		var inputTaxAmt=$("#bptycatrid input[name='inputtaxesamt']").attr('id');
		if(typeof inputTaxAmt!='undefined'){
			inputTaxNames=$('select[name="bptycainputtaxcomponenet"] option:selected').map(function () {
				return this.text;
			}).get();

			inputTaxValues=$('input[name="bptycainputtaxcomponenetamt"]').map(function () {
		 		return this.value;
		 	}).get().toString();
		}

		var inputTaxIdList="";
		for (var i=0;i<inputTaxNames.length;i++){
			if(inputTaxNames[i]!=""){
				inputTaxIdList+=inputTaxNames[i]+",";
			}else{
				inputTaxIdList+=" "+",";
			}
		}
		inputTaxNames=inputTaxIdList;
		withholdingTaxValues=$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").val();
		txnLeftOutWithholdTransIDs=$("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val();
	}*/

    var followedkl = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
    if (typeof followedkl != 'undefined') {
        var klfollowednotfollowed = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").is(':checked');
        if (klfollowednotfollowed == false) {
            swal("Transaction knowledge library!", "Please follow mandatory knowledge library before submitting transaction for accounting", "error");
            enableTransactionButtons();
            return true;
        }
    }

    if (txnNetAmount == "") {
        swal("Incomplete Transaction detail!", "Please provide complete transaction details befor submitting for approval", "error");
        enableTransactionButtons();
        return true;
    } else {
        var proceed = true;
        var documentUploadRequired = false;
        /*if(supportingDoc==""){
			var documentUploadRequired=checkForDocumentUploadRule(txnForItem,txnForBranch,txnNetAmount);
			if(documentUploadRequired==true){
				if(confirm("This Transaction requires mandatory documents upload.Please upload the necessary document.")){
					proceed=true;
				}else{
					enableTransactionButtons();
				}
			}
			if(documentUploadRequired==false){
				proceed=true;
			}
		}else{
			proceed=true;
		}
		var userCheckRule=checkForOnlyConfiguredApprover(txnForItem,txnForBranch,txnNetAmount,"transaction");
		if(userCheckRule==true){
			proceed=true;
		}
		if(userCheckRule==false){
			proceed=false;
		}*/
        var txnDate = $("#" + parentTr).find(".txnBackDate").val();
        if (proceed) {
            var txnEntityID = $("#" + parentTr).attr("name");
            var txnJsonData = {};
            txnJsonData.txnEntityID = txnEntityID;
            txnJsonData.txnPurpose = whatYouWantToDo;
            txnJsonData.txnPurposeVal = whatYouWantToDoVal;
            txnJsonData.txnforbranch = txnForBranch;
            txnJsonData.txnforproject = txnForProject;
            txnJsonData.txnPOInvoice = txnPOInvoice == "Select Purchase Order" ? "": txnPOInvoice;
            txnJsonData.txnPOInvoiceTxnRef = txnPOInvoiceTxnRef;
            txnJsonData.txnPlaceOfSupply = txnPlaceOfSupply;
            txnJsonData.txnforitem = txnForItem;
            //txnJsonData.txnactualbudget=userTxnActBud;
            //txnJsonData.txnbudget=userTxnBudget;
            //txnJsonData.userTxnAmtLimit=userTxnAmountLimit;
            txnJsonData.txnforcustomer = txnForCustomer;
            txnJsonData.txnforunavailablecustomer = txnForUnavailableCustomer;
            txnJsonData.txnnoofunits = txnNoOfUnits;
            txnJsonData.txnpriceperunit = txnPricePerUnit;
            txnJsonData.txnFrieghtCharges = txnFrieghtCharges;
            txnJsonData.txngross = txnGross;
            txnJsonData.vendorAdvanceType = vendorAdvanceType;
            txnJsonData.txncustomeradvanceifany = txnCustomerAdvanceIfAny;
            txnJsonData.txncustomeradvanceadjustment = txnCustomerAdvanceAdjustment;
            txnJsonData.txncustomerdiscountavailable = txnCustomerDiscountAvailable;
            txnJsonData.txnnetamount = txnNetAmount;
            txnJsonData.netAmountTotalWithDecimalValue = netAmountTotalWithDecimalValue;
            txnJsonData.txnnetamountdescription = txnNetAmountDescription;
            txnJsonData.txnreceiptdetails = txnReceiptDetails;
            txnJsonData.txnreceipttypebankdetails = txnReceiptTypeBankDetails;
            txnJsonData.txnremarks = txnRemarks;
            txnJsonData.supportingdoc = supportingDoc;
            txnJsonData.klfollowednotfollowed = klmandatoryfollowednotfollowed;
            /*if(procurementRequest!=""){
				txnJsonData.txnprocreq=procurementRequest;
			}*/
            if (procurementRequestRemarks != "") {
                txnJsonData.txnprocrem = procurementRequestRemarks;
            }
            txnJsonData.txnInputTaxesNames = inputTaxNames;
            txnJsonData.txnInputTaxesValues = inputTaxValues;
            txnJsonData.txnwithholdingTaxValues = withholdingTaxValues;
            txnJsonData.txnLeftOutWithholdTransIDs = txnLeftOutWithholdTransIDs;
            txnJsonData.txndocumentUploadRequired = documentUploadRequired;
            txnJsonData.useremail = $("#hiddenuseremail").text();
            txnJsonData.txnDate = txnDate;
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
                    cancel();							//this will remove create transaction div
                    //getUserTransactions(0, 20);		//will load user transactions, so new transaction is visible...tried appending only this transaction to existing transactionTable, but no all data available
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    } else if (xhr.status == 500) {
                        swal("Error on Submit For Approval!", "Please contact support team or retry.", "error");
                    }
                },
                complete: function (data) {
                    $.unblockUI();
                    enableTransactionButtons();
                }
            });
        }
    }
}

var submitForApprovalPurchaseReturnTrans = function (whatYouWantToDo, whatYouWantToDoVal, parentTr) {
    var txnForBranch = "";
    var txnForProject = "";
    var txnForItem = "";
    var txnForCustomer = "";
    var txnNetAmount = "";
    var txnNetAmountDescription = "";
    var txnReceiptDetails = "";
    var klmandatoryfollowednotfollowed = "";
    var txnReceiptTypeBankDetails = "";
    var txnRemarks = "";
    var supportingDoc = "";
    var txnLeftOutWithholdTransIDs = "";
    var vendorAdvanceType = "";
    var netAmountTotalWithDecimalValue = "";
    let parentOfparentTr = $("#"+parentTr).closest('div').attr('id');
    var transactionInvoiceId = $("#prtfcvInvoices option:selected").val(); //original buy on credit trans id
    txnForBranch = $("#" + parentTr + " select[class='txnBranches'] option:selected").val();
    txnForProject = $("#" + parentTr + " select[class='txnForProjects'] option:selected").val();
    txnForCustomer = $("#" + parentTr + " .masterList option:selected").val();
    txnNetAmount = $("#" + parentTr + " input[class='netAmountValTotal']").val();

    txnForItem = convertTableDataToArray("multipleItemsTableprtfcv");
    netAmountTotalWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
    if (txnNetAmount == "") {
        swal("Incomplete Transction detail!", "Please provide complete transaction details befor submitting for approval.", "error");
        enableTransactionButtons();
        return true;
    }
    txnRemarks = $("#prtfcvRemarks").val();
    var supportingDocTmp = $("#" + parentTr + " select[class='txnUploadSuppDocs'] option").map(function () {
        if ($(this).val() != "") {
            return $(this).val();
        }
    }).get();
    supportingDoc = supportingDocTmp.join(',');

    var followedkl = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
    if (typeof followedkl != 'undefined') {
        if ($("#" + parentTr + " div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked') == true) {
            klmandatoryfollowednotfollowed = "1";
        }
        if ($("#" + parentTr + " div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked') == true) {
            klmandatoryfollowednotfollowed = "0";
        }
    }
    txnLeftOutWithholdTransIDs = $("#" + parentTr + " input[class='txnLeftOutWithholdTransIDs']").val();
    var followedkl = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
    if (typeof followedkl != 'undefined') {
        var klfollowednotfollowed = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").is(':checked');
        if (klfollowednotfollowed == false) {
            swal("Mandatory rules not followed!", "Please read the mandatory knowledge library before submitting transaction for approval.", "error");
            enableTransactionButtons();
            return true;
        }
    }
    if (transactionInvoiceId == "" || txnNetAmount == "") {
        swal("Incomplete transaction data!", "Please provide complete sales return transaction details before submitting for approval", "error");
        enableTransactionButtons();
        return true;
    } else {
        var followedkl = $("#" + parentTr + " div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
        if (typeof followedkl != 'undefined') {
            if ($("#" + parentTr + " div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked') == true) {
                klmandatoryfollowednotfollowed = "1";
            }
            if ($("#" + parentTr + " div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked') == true) {
                klmandatoryfollowednotfollowed = "0";
            }
        }
        var userCheckRule = checkForOnlyConfiguredApproverPVS(transactionInvoiceId, txnNetAmount, "purchasereturn");
        if (userCheckRule == true) {
            proceed = true;
        }
        if (userCheckRule == false) {
            proceed = false;
        }
        if (proceed) {
            var txnEntityID = $("#" + parentTr).attr("name");
            var txnJsonData = {};
            txnJsonData.txnEntityID = txnEntityID;
            txnJsonData.transactionInvoiceId = transactionInvoiceId;
            txnJsonData.txnPurpose = whatYouWantToDo;
            txnJsonData.txnPurposeVal = whatYouWantToDoVal;
            txnJsonData.txnforbranch = txnForBranch;
            txnJsonData.txnforproject = txnForProject;
            txnJsonData.txnforitem = txnForItem;
            txnJsonData.txnforcustomer = txnForCustomer;
            txnJsonData.txnnetamount = txnNetAmount;
            txnJsonData.netAmountTotalWithDecimalValue = netAmountTotalWithDecimalValue;
            txnJsonData.txnnetamountdescription = txnNetAmountDescription;
            txnJsonData.txnreceiptdetails = txnReceiptDetails;
            txnJsonData.txnreceipttypebankdetails = txnReceiptTypeBankDetails;
            txnJsonData.txnremarks = txnRemarks;
            txnJsonData.supportingdoc = supportingDoc;
            txnJsonData.klfollowednotfollowed = klmandatoryfollowednotfollowed;
            txnJsonData.txnLeftOutWithholdTransIDs = txnLeftOutWithholdTransIDs;
            txnJsonData.useremail = $("#hiddenuseremail").text();
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
                    cancel();							//this will remove create transaction div
                    //getUserTransactions(2, 100);		//will load user transactions, so new transaction is visible...tried appending only this transaction to existing transactionTable, but no all data available
                    viewTransactionData(data); // to render the updated transaction recored
                },
                error: function (xhr, status, error) {
                    if(typeof data.message !=='undefined' && data.message != ""){
                        swal("Error!", data.message, "error");
                        return false;
                    }
                    if (xhr.status == 401) {
                        doLogout();
                    } else if (xhr.status == 500) {
                        swal("Error on Submit For Approval!", "Please contact support team or retry.", "error");
                    }
                },
                complete: function (data) {
                    $.unblockUI();
                    enableTransactionButtons();
                }
            });
        }
    }
}

function calculateBuyNetAmount(elem, parentTr, parentOfparentTr) {
    var returnValue = true;
    $(".netAmountDescriptionDisplay").text("");
    var text = $("#whatYouWantToDo").find('option:selected').text();
    var txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val();
    var txnAdjustmentAmount = $("#" + parentTr + " input[class='howMuchAdvance']").val();
    var txnitemSpecifics = $("#" + parentTr + " .txnItems").val();
    var txnTotalInputTaxes = $("#" + parentTr + " input[class='txnTaxAmount']").val();
    var txnInputTaxesDetails = $("#" + parentTr + " input[class='txnTaxTypes']").val();
    var txnUnavailable = $("#" + parentOfparentTr + " input[name='unAvailableVendor']").val();
    var txnVendorCustomer = $("#" + parentOfparentTr + " .masterList option:selected").val();
    var txnBranch = $("#" + parentOfparentTr + " select[class='txnBranches'] option:selected").val();
    var txnGrossAmount = $("#" + parentTr + " input[class='txnGross']").val();
    if (txnPurposeVal == PURCHASE_RETURNS) {
        var orgNoOfUnits = $("#" + parentTr + " input[id='prtfcvOriginalNoOfUnitsHid']").val();
        var noOfUnits = $("#" + parentTr + " input[id='prtfcvunits']").val();
        if (noOfUnits != "" && parseInt(noOfUnits) > parseInt(orgNoOfUnits)) {
            swal("Returning quantity cannot be greater than Original Units " + orgNoOfUnits,"error");
            $("#" + parentTr + " input[id='prtfcvunits']").val("");
            return false;
        }
    }
    //var txnFrieghtCharges=$("#"+parentTr+" input[class='txnFrieghtCharges']").val();
    var proceedSalesTxn = false;
    if ((txnitemSpecifics == "" || txnitemSpecifics == null) || (txnBranch == "") || (txnVendorCustomer == "" && txnUnavailable == "") || (txnGrossAmount == "")) {
        //swal("Incomplete Transaction detail!", "Please provide transaction details before System calculates net amount.", "error");
        returnValue = false;
    } else {
        if (text == "Buy on cash & pay right away" || text == "Buy on credit & pay later" || text == "Buy on Petty Cash Account" || txnPurposeVal == PURCHASE_RETURNS) {
            var err = checkAmountRangeLimitForTransactionCreator(elem, txnGrossAmount, parentTr);
            if (err == true) {
                returnValue = false;
            }

            var jsonData = {};
            jsonData.useremail = $("#hiddenuseremail").text();
            jsonData.userTxnPurposeText = text;
            jsonData.txnBranchId = txnBranch;
            jsonData.txnSpecificsId = txnitemSpecifics;
            jsonData.txnGrossAmt = txnGrossAmount;
            jsonData.txnAdjustmentAmount = txnAdjustmentAmount;
            jsonData.txnTotalInputTaxes = txnTotalInputTaxes;
            jsonData.txnSelectedVendorCustomer = txnVendorCustomer;
            jsonData.txnPurposeValue = txnPurposeVal;
            var url = "/transaction/calculateNetAmount";
            $.ajax({
                url: url,
                data: JSON.stringify(jsonData),
                type: "text",
                headers: {
                    "X-AUTH-TOKEN": window.authToken
                },
                method: "POST",
                contentType: 'application/json',
                success: function (data) {
                    if (typeof data.tdsPayableSpecific != 'undefined' && data.tdsPayableSpecific == 0) {
                        swal("COA: mapping missing", "Chart of Account, TDS Payable mapping is not defined, please define and try.", "error");
                        $(elem).val('');
                        initMultiItemsSelect2();
                        disableTransactionButtons();
                        returnValue = false;
                    }
                    if (text == "Buy on cash & pay right away") {
                        $("#bocpranetAmountLabel").text("");
                        if (txnAdjustmentAmount != "") {
                            $("#bocpranetAmountLabel").append('<br/>Advance Adjustment: ' + txnAdjustmentAmount + ",");
                        }
                        var withholdingAmount = 0;
                        for (var i = 0; i < data.branchSpecificsTaxComponentPurchaseData.length; i++) {
                            $("#bocpranetAmountLabel").append('<br/>Rate: ' + data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate + ",");
                            $("#bocpranetAmountLabel").append('<br/>Limit: ' + data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxLimit + ",");
                            $("#bocpranetAmountLabel").append('<br/>Withholding Monetory Limit: ' + data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit + ",");
                            withholdingAmount = data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxTotalAmount;
                            $("#bocpranetAmountLabel").append('<br/>Withholding Tax: ' + parseFloat(withholdingAmount) + '(-)');
                            $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val(withholdingAmount);
                            $("#" + parentTr + " input[class='txnLeftOutWithholdTransIDs']").val(data.branchSpecificsTaxComponentPurchaseData[i].leftOutTransactionIDList);
                        }
                        //var taxTotalAmount=parseFloat(data.branchSpecificsTaxResultAmountData[0].taxTotalAmount);
                        var taxTypeAndRate = $("#netAmountLabel").text();
                        var adjustmentAmount = $("#" + parentTr + " input[id='bocprahowmuchfromadvance']").val();
                        var gross = $("#" + parentTr + " input[id='bocpragross']").val();
                        var netAmount = parseFloat(gross);
                        if (gross != "" && adjustmentAmount != "" && typeof adjustmentAmount != 'undefined') {
                            netAmount = netAmount - parseFloat(adjustmentAmount);
                            $("#netAmountLabel").append('<br/>Adjustment:' + adjustmentAmount + ",");
                        }
                        if (txnTotalInputTaxes != "" && typeof txnTotalInputTaxes != 'undefined') { //add input taxes to net
                            $("#bocpranetAmountLabel").append('<br/>Total Input Taxes: ' + txnTotalInputTaxes + '(+)' + ",");
                            netAmount = (parseFloat(netAmount) + parseFloat(txnTotalInputTaxes));
                            $("#netAmountLabel").append('<br/>Net Tax:' + (txnTotalInputTaxes * 1).toFixed(2) + ",");
                        }
                        netAmount = (parseFloat(netAmount) - parseFloat(withholdingAmount)); //deduct withholding taxes from net
                        $("#" + parentTr + " input[id='bocpranetamnt']").val((netAmount * 1).toFixed(2));
                        var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
                        $("#bocpranetamntTotal").val(Math.round(netAmountTotal).toFixed(2));
                    } else if (text == "Buy on credit & pay later") {
                        $("#bocaplnetAmountLabel").text("");
                        if (txnAdjustmentAmount != "") {
                            $("#bocaplnetAmountLabel").append('<br/>Advance Adjustment: ' + txnAdjustmentAmount + ",");
                        }
                        /*if(typeof txnInputTaxAmt!='undefined' && txnTotalInputTaxes != ""){
						$("#bocaplnetAmountLabel").append('<br/>Total Input Taxes: '+txnTotalInputTaxes+'(+)'+",");
					}
					if(txnFrieghtCharges != ""){
						$("#bocaplnetAmountLabel").append('<br/>Frieght Charges: '+txnFrieghtCharges+'(+)'+",");
					}*/
                        var withholdingAmount = 0;
                        for (var i = 0; i < data.branchSpecificsTaxComponentPurchaseData.length; i++) {
                            $("#bocaplnetAmountLabel").append('<br/>Rate: ' + data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate + ",");
                            $("#bocaplnetAmountLabel").append('<br/>Limit: ' + data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxLimit + ",");
                            $("#bocaplnetAmountLabel").append('<br/>Withholding Monetory Limit: ' + data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit + ",");
                            withholdingAmount = data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxTotalAmount;
                            $("#bocaplnetAmountLabel").append('<br/>Withholding Tax: ' + parseFloat(withholdingAmount) + '(-)');
                            $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val(withholdingAmount);
                            $("#" + parentTr + " input[class='txnLeftOutWithholdTransIDs']").val(data.branchSpecificsTaxComponentPurchaseData[i].leftOutTransactionIDList);
                        }
                        var taxTypeAndRate = $("#netAmountLabel").text();
                        var adjustmentAmount = $("#" + parentTr + " input[id='bptycahowmuchfromadvance']").val();
                        var gross = $("#" + parentTr + " input[id='bocaplgross']").val();
                        var netAmount = parseFloat(gross);
                        if (gross != "" && adjustmentAmount != "" && typeof adjustmentAmount != 'undefined') {
                            netAmount = netAmount - parseFloat(adjustmentAmount);
                            $("#netAmountLabel").append('<br/>Adjustment:' + adjustmentAmount + ",");
                        }
                        if (txnTotalInputTaxes != "" && typeof txnTotalInputTaxes != 'undefined') {
                            $("#bocaplnetAmountLabel").append('<br/>Total Input Taxes: ' + txnTotalInputTaxes + '(+)' + ",");
                            netAmount = (parseFloat(netAmount) + parseFloat(txnTotalInputTaxes));
                            $("#netAmountLabel").append('<br/>Net Tax:' + (txnTotalInputTaxes * 1).toFixed(2) + ",");
                        }
                        netAmount = (parseFloat(netAmount) - parseFloat(withholdingAmount)); //deduct withholding taxes from net
                        $("#" + parentTr + " input[id='bocaplnetamnt']").val((netAmount * 1).toFixed(2));
                        var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
                        $("#bocaplnetamntTotal").val(Math.round(netAmountTotal).toFixed(2));
                    } else if (text == "Buy on Petty Cash Account") {
                        $("#bptycanetAmountLabel").text("");
                        if (txnAdjustmentAmount != "") {
                            $("#bptycanetAmountLabel").append('<br/>Advance Adjustment: ' + txnAdjustmentAmount + ",");
                        }
                        var withholdingAmount = 0;
                        for (var i = 0; i < data.branchSpecificsTaxComponentPurchaseData.length; i++) {
                            $("#bptycanetAmountLabel").append('<br/>Rate: ' + data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate + ",");
                            $("#bptycanetAmountLabel").append('<br/>Limit: ' + data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxLimit + ",");
                            $("#bptycanetAmountLabel").append('<br/>Withholding Monetory Limit: ' + data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit + ",");
                            withholdingAmount = data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxTotalAmount;
                            $("#bptycanetAmountLabel").append('<br/>Withholding Tax: ' + parseFloat(withholdingAmount) + '(-)');
                            $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val(withholdingAmount);
                            $("#" + parentTr + " input[class='txnLeftOutWithholdTransIDs']").val(data.branchSpecificsTaxComponentPurchaseData[i].leftOutTransactionIDList);
                        }
                        var taxTypeAndRate = $("#netAmountLabel").text();
                        var adjustmentAmount = $("#" + parentTr + " input[id='bptycahowmuchfromadvance']").val();
                        var gross = $("#" + parentTr + " input[id='bptycagross']").val();
                        var netAmount = parseFloat(gross);
                        if (gross != "" && adjustmentAmount != "" && typeof adjustmentAmount != 'undefined') {
                            netAmount = netAmount - parseFloat(adjustmentAmount);
                            $("#netAmountLabel").append('<br/>Adjustment:' + adjustmentAmount + ",");
                        }
                        if (txnTotalInputTaxes != "" && typeof txnTotalInputTaxes != 'undefined') {
                            $("#bptycanetAmountLabel").append('<br/>Total Input Taxes: ' + txnTotalInputTaxes + '(+)' + ",");
                            netAmount = (parseFloat(netAmount) + parseFloat(txnTotalInputTaxes));
                            $("#netAmountLabel").append('<br/>Net Tax:' + (txnTotalInputTaxes * 1).toFixed(2) + ",");
                        }
                        netAmount = (parseFloat(netAmount) - parseFloat(withholdingAmount)); //deduct withholding taxes from net
                        $("#" + parentTr + " input[id='bptycanetamnt']").val((netAmount * 1).toFixed(2));
                        var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
                        $("#bptycanetamntTotal").val(Math.round(netAmountTotal).toFixed(2));

                        var pettyTxnRule = $("#" + parentTr + " div[class='branchAvailablePettyCash']").attr('class');
                        if (typeof pettyTxnRule != 'undefined') {
                            var pettyTxnRuleVal = $("#" + parentTr + " div[class='branchAvailablePettyCash']").text();
                            if (pettyTxnRuleVal.indexOf("No") != -1) {
                                $("#" + parentTr + " td[class='paymentDetails']").show();
                                $("#bptycasubmitForAccounting").show();
                                $("#bptycasubmitForApproval").hide();
                                $("#transactionDetailsBPTYCATable th[class='paymentDetailsLabel']").show();
                                var lastIndexOfAvailablePettyCash = pettyTxnRuleVal.lastIndexOf("=");
                                var availablePettyCashAmount = pettyTxnRuleVal.substring(lastIndexOfAvailablePettyCash + 1, pettyTxnRuleVal.length);
                                if (parseFloat(netAmount) > parseFloat(availablePettyCashAmount)) {
                                    $(".netAmountVal").val("");
                                    $(".netAmountDescriptionDisplay").text("");
                                    swal("Insufficient petty cash!", "Shortage in Available Petty Cash Amount, cannot Proceed with the Petty Cash.", "error")
                                    returnValue = false;
                                }
                            }
                            if (pettyTxnRuleVal.indexOf("Yes") != -1) {
                                var limitwithtext = pettyTxnRuleVal.split('=');
                                var limitAmount = parseFloat(limitwithtext[1]);
                                if (parseFloat(netAmount) >= limitAmount) {
                                    $("#" + parentTr + " td[class='paymentDetails']").hide();
                                    $("#bptycasubmitForAccounting").hide();
                                    $("#bptycasubmitForApproval").show();
                                    $("#transactionDetailsBPTYCATable th[class='paymentDetailsLabel']").hide();
                                } else {
                                    $("#" + parentTr + " td[class='paymentDetails']").show();
                                    $("#bptycasubmitForAccounting").show();
                                    $("#bptycasubmitForApproval").hide();
                                    $("#transactionDetailsBPTYCATable th[class='paymentDetailsLabel']").show();
                                    var lastIndexOfAvailablePettyCash = pettyTxnRuleVal.lastIndexOf("=");
                                    var availablePettyCashAmount = pettyTxnRuleVal.substring(lastIndexOfAvailablePettyCash + 1, pettyTxnRuleVal.length);
                                    if (parseFloat(netAmount) > parseFloat(availablePettyCashAmount)) {
                                        $(".netAmountVal").val("");
                                        $(".netAmountDescriptionDisplay").text("");
                                        swal("Insufficient petty cash!", "Shortage in Available Petty Cash Amount, cannot Proceed with the Petty Cash.", "error")
                                        returnValue = false;
                                    }
                                }
                            }
                        }
                        if (typeof pettyTxnRule == 'undefined') {
                            $("#" + parentTr + " td[class='paymentDetails']").hide();
                            $("#bptycasubmitForAccounting").show();
                            $("#bptycasubmitForApproval").hide();
                            $("#" + parentTr + " td[class='paymentDetails']").hide();
                            $("#transactionDetailsBPTYCATable th[class='paymentDetailsLabel']").hide();
                        }
                    } else if (txnPurposeVal == PURCHASE_RETURNS) {
                        $("#prtfcvnetAmountLabel").text("");
                        if (txnAdjustmentAmount != "") {
                            $("#prtfcvnetAmountLabel").append('<br/>Advance Adjustment: ' + txnAdjustmentAmount + ",");
                        }
                        var withholdingAmount = 0;
                        for (var i = 0; i < data.branchSpecificsTaxComponentPurchaseData.length; i++) {
                            $("#prtfcvnetAmountLabel").append('<br/>Rate: ' + data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate + ",");
                            $("#prtfcvnetAmountLabel").append('<br/>Limit: ' + data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxLimit + ",");
                            $("#prtfcvnetAmountLabel").append('<br/>Withholding Monetory Limit: ' + data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit + ",");
                            withholdingAmount = data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxTotalAmount;
                            $("#prtfcvnetAmountLabel").append('<br/>Withholding Tax: ' + parseFloat(withholdingAmount) + '(-)');
                            $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val(withholdingAmount);
                            $("#" + parentTr + " input[class='txnLeftOutWithholdTransIDs']").val(data.branchSpecificsTaxComponentPurchaseData[i].leftOutTransactionIDList);
                        }
                        var taxTypeAndRate = $("#netAmountLabel").text();
                        var adjustmentAmount = $("#" + parentTr + " input[id='bptycahowmuchfromadvance']").val();
                        var gross = $("#" + parentTr + " input[id='prtfcvgross']").val();
                        var netAmount = parseFloat(gross);
                        if (gross != "" && adjustmentAmount != "" && typeof adjustmentAmount != 'undefined') {
                            netAmount = netAmount - parseFloat(adjustmentAmount);
                            $("#prtfcvnetAmountLabel").append('<br/>Adjustment:' + adjustmentAmount + ",");
                        }
                        if (txnTotalInputTaxes != "" && typeof txnTotalInputTaxes != 'undefined') {
                            $("#prtfcvnetAmountLabel").append('<br/>Total Input Taxes: ' + txnTotalInputTaxes + '(+)' + ",");
                            netAmount = (parseFloat(netAmount) + parseFloat(txnTotalInputTaxes));
                            $("#prtfcvnetAmountLabel").append('<br/>Net Tax:' + (txnTotalInputTaxes * 1).toFixed(2) + ",");
                        }
                        netAmount = (parseFloat(netAmount) - parseFloat(withholdingAmount)); //deduct withholding taxes from net
                        $("#" + parentTr + " input[id='prtfcvnetamnt']").val((netAmount * 1).toFixed(2));
                        var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
                        $("#prtfcvnetamntTotal").val(Math.round(netAmountTotal).toFixed(2));
                    }
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    } else if (xhr.status == 500) {
                        swal("Error on Net amount calculation!", "Please contact support team", "error");
                    }
                },
                complete: function (data) {
                    $.unblockUI();
                }
            });
        }
    }
    return returnValue;
}

/** also used on Buy on petty */
var submitForApprovalBuyTxn = function (whatYouWantToDo, whatYouWantToDoVal, parentTr) {
    let isSingleUserDeploy = $("#isDeploymentSingleUser").html();
    let sourceGstin = "";
    let destinGstin = "";
    let txnTypeOfSupply = "";
    let txnWalkinCustomerType = "";
    let parentOfparentTr = $("#"+parentTr).closest('div').attr('id');
    let txnForUnavailableCustomer = $("#" + parentTr + " input[class='unavailable ui-autocomplete-input']").val();
    if (GST_COUNTRY_CODE !== "" && GST_COUNTRY_CODE !== undefined && GST_COUNTRY_CODE !== null) {
        txnTypeOfSupply = $("#" + parentTr + " select[class='txnTypeOfSupply']").val();
        if (txnTypeOfSupply == "" || txnTypeOfSupply == null) {
            swal("Invalid Type of Supply!", "Please select valid Type of Supply.", "error");
            enableTransactionButtons();
            return false;
        }
        sourceGstin = $("#" + parentTr + " select[class='txnBranches']").children(":selected").attr("id");
        if (sourceGstin === null || sourceGstin === "") {
            swal("Invalid Branch's GSTIN", "Please select valid Branch.", "error");
            enableTransactionButtons();
            return false;
        }
        destinGstin = $("#" + parentTr + " select[class='placeOfSply txnDestGstinCls']").val();
        if (txnForUnavailableCustomer !== null && txnForUnavailableCustomer !== "") {
            txnWalkinCustomerType = $("#" + parentTr + " select[class='walkinCustType']").val();
            if (txnWalkinCustomerType == "" || txnWalkinCustomerType == null) {
                swal("Invalid Type of Customer!", "Please select valid Type of customer.", "error");
                enableTransactionButtons();
                return false;
            } else if (txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2") {
                destinGstin = $("#" + parentTr + " input[class='placeOfSplyTextHid']").val();
            } else if (txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4") {
                destinGstin = sourceGstin;
            } else if (txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6") {
                destinGstin = $("#" + parentTr + " select[name='txnWalkinPlcSplySelect']").val();
            }

            if (txnWalkinCustomerType == "5" && txnWalkinCustomerType == "6" && destinGstin.length < 1) {
                swal("Invalid Place of Supply", "Please provide valid Place of Supply.", "error");
                enableTransactionButtons();
                return false;
            } else if (txnWalkinCustomerType != "5" && txnWalkinCustomerType != "6" && destinGstin.length > 1 && destinGstin.length < 15) {
                swal("Invalid Place of Supply", "Please provide valid Place of Supply.", "error");
                enableTransactionButtons();
                return false;
            }
        }
        if (destinGstin === null || destinGstin === "") {
            swal("Invalid Place of Supply!", "Please provide valid Place of Supply.", "error");
            enableTransactionButtons();
            return false;
        }
    }
    if (showExceedLimitPopup(parentTr)) {
        enableTransactionButtons();
        return false;
    }

    var txnDate = $("#" + parentTr).find(".txnBackDate").val();
    var txnForBranch = $("#" + parentTr + " select[class='txnBranches'] option:selected").val();
    var txnForProject = $("#" + parentTr + " select[class='txnForProjects'] option:selected").val();
    var txnForCustomer = $("#" + parentTr + " .masterList option:selected").val();
    var txnPOInvoice = $("#" + parentTr + " select[id='bocpraInvoices'] option:selected").val();//purchaseOrder transaction id in Transaciton table
    var txnPOInvoiceTxnRef = $("#" + parentTr + " select[class='salesExpenseTxns'] option:selected").attr('txnRefNo');
    var txnTotalNetAmount = $("#" + parentTr + " input[class='netAmountValTotal']").val();
    var isValid = validateMultiItemsTransaction("multipleItemsTablebocpra");
    if (!isValid) {
        swal("Incomplete transaction data!", "Please provide complete transaction details before submitting for approval", "error");
        enableTransactionButtons();
        return false;
    }
    var txnForItem = convertTableDataToArray("multipleItemsTablebocpra");
    var totalTxnNetAmtWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
    var parentDiv = $("#" + parentTr).closest('div').attr('id');
    var totalWithholdTaxAmt = calcuateTotalAmt4Element(parentDiv, 'withholdingtaxcomponenetdiv');
    var totalTxnTaxAmt = calcuateTotalAmt4Element(parentDiv, 'itemTaxAmount');
    var totalTxnGrossAmt = calcuateTotalAmt4Element(parentDiv, 'txnGross');
    var klfollowednotfollowed = isKnowledgeLibFollowedInMultiItems(parentDiv);
    if (txnTotalNetAmount == "") {
        swal("Incomplete Transaction detail!", "Please provide complete transaction details before submitting for approval.", "error");
        enableTransactionButtons();
        return false;
    }
    var txnRemarks = $("#" + parentTr + " textarea[class='voiceRemarksClass']").val();
    var supportingDocTmp = $("#" + parentTr + " select[class='txnUploadSuppDocs'] option").map(function () {
        if ($(this).val() != "") {
            return $(this).val();
        }
    }).get();
    supportingDoc = supportingDocTmp.join(',');
    var procurementRequestRemarks = $("#" + parentTr + " div[id='procurementRequestRemarks']").text();
    var txnEntityID = $("#" + parentTr).attr("name");
    var txnJsonData = {};
    txnJsonData.txnEntityID = txnEntityID;
    txnJsonData.txnPurpose = whatYouWantToDo;
    txnJsonData.txnPurposeVal = whatYouWantToDoVal;
    txnJsonData.txnForBranch = txnForBranch;
    txnJsonData.txnForProject = txnForProject;
    txnJsonData.txnPOInvoice = txnPOInvoice == "Select Purchase Order" ? "": txnPOInvoice;;
    txnJsonData.txnPOInvoiceTxnRef = txnPOInvoiceTxnRef;
    txnJsonData.txnForItem = txnForItem;
    txnJsonData.txnForCustomer = txnForCustomer;
    txnJsonData.txnForUnavailableCustomer = txnForUnavailableCustomer;
    txnJsonData.txnTotalNetAmount = txnTotalNetAmount;
    txnJsonData.totalTxnNetAmtWithDecimalValue = totalTxnNetAmtWithDecimalValue;
    txnJsonData.totalWithholdTaxAmt = totalWithholdTaxAmt;
    txnJsonData.totalTxnTaxAmt = totalTxnTaxAmt;
    txnJsonData.totalTxnGrossAmt = totalTxnGrossAmt;
    txnJsonData.klfollowednotfollowed = klfollowednotfollowed;
    txnJsonData.txnRemarks = txnRemarks;
    txnJsonData.txnprocrem = procurementRequestRemarks;
    txnJsonData.supportingdoc = supportingDoc;
    txnJsonData.txnDocumentUploadRequired = "false";
    txnJsonData.txnSourceGstin = sourceGstin;
    txnJsonData.txnDestinGstin = destinGstin;
    txnJsonData.txnTypeOfSupply = txnTypeOfSupply;
    txnJsonData.txnWalkinCustomerType = txnWalkinCustomerType;
    txnJsonData.txnDate = txnDate;
    if (txnForUnavailableCustomer !== null && txnForUnavailableCustomer !== "" && (txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2")) {
        var returnValue = setWalkinVendorDetail(txnJsonData);
        if (!returnValue) {
            enableTransactionButtons();
            return returnValue;
        }
    }
    txnJsonData.useremail = $("#hiddenuseremail").text();
    // SingleUSer
	if(READ_PAYMODE_ON_APPROVAL == IS_READ_PAYMODE_ON_APPROVAL){
		if(whatYouWantToDoVal == BUY_ON_CASH_PAY_RIGHT_AWAY) {
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
	} else {
    	txnJsonData.txnReceiptDetails = "";
        txnJsonData.txnReceiptPaymentBank = "";
        txnJsonData.txnInstrumentNum = "";
        txnJsonData.txnInstrumentDate = "";
        txnJsonData.txnReceiptDescription = "";
    }
    var url = "/transaction/submitForApproval";
    if (whatYouWantToDoVal == BUY_ON_PETTY_CASH_ACCOUNT) {
        var pettyCashAvailable = $("#" + parentTr + " select[class='txnBranches']").attr("branchPettyCashAvailable");
        var txnNetAmount = $("#" + parentTr + " input[class='netAmountValTotal']").val();
        if (parseFloat(txnNetAmount) > parseFloat(pettyCashAvailable)) {
            swal("Insufficient balance in the Petty Cash account!", "Please infuse funds into the Petty cash account. Current bank balance is:" + pettyCashAvailable, "error");
            enableTransactionButtons();
            return false;
        }
        txnReceiptTypeBankDetails = $("#" + parentTr + " textarea[id='paymentBranchBankAccount']").val();
        var txnVendorInvoiceDate = $("#" + parentTr + " input[id='vendorInvoiceDate']").val();
        txnJsonData.txnReceiptDetails = "3"; //Petty Cash
        txnJsonData.txnReceiptTypeBankDetails = txnReceiptTypeBankDetails;
        txnJsonData.txnInvDate = txnVendorInvoiceDate
        url = "/transaction/submitForAccounting";
    }
    setBuyTxnAdditionalDetail(txnJsonData);
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
            }
            if (typeof data.roundupMappingFound != 'undefined' && !data.roundupMappingFound) {
                swal("Round-up: mapping missing!", "Chart of Account, Round-up mapping is not defined, please define and try.", "error");
                disableTransactionButtons();
                return false;
            }
            if (whatYouWantToDoVal != BUY_ON_PETTY_CASH_ACCOUNT) {
                approvealForSingleUser(data);	// Single User
            }
            
            cancel();//this will remove create transaction div
            //getUserTransactions(2, 100);		//will load user transactions, so new transaction is visible...tried appending only this transaction to existing transactionTable, but no all data available
            enableTransactionButtons();
            viewTransactionData(data); // to render the updated transaction recored
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                if (whatYouWantToDoVal == BUY_ON_PETTY_CASH_ACCOUNT) {
                    swal("Error on Submit For accounting!", "Please contact support team or retry.", "error");
                } else {
                    swal("Error on Submit For Approval!", "Please contact support team or retry.", "error");
                }
            }
        },
        complete: function (data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}

var editTransactionOnceAllowedSingleItem = function (elem, data, transactionEntityId, transactionPurposeVal) {
    var transactionTableTr = "";
    if (transactionPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY) {
        transactionTableTr = "bocpratrid";
    } else if (transactionPurposeVal == BUY_ON_CREDIT_PAY_LATER) {
        transactionTableTr = "bocapltrid";
    } else if (transactionPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT) {
        transactionTableTr = "bptycatrid";
    }
    $("#" + transactionTableTr).attr("name", transactionEntityId);
    $("#" + transactionTableTr + " select[class='txnBranches']").val(data.transactiondetailsData[0].branchId).change();
    $("#" + transactionTableTr + " select[class='txnForProjects']").val(data.transactiondetailsData[0].projectID).change();
    $("#" + transactionTableTr + " .txnItems").val(data.transactiondetailsData[0].itemId).change();
    $("#" + transactionTableTr + " .masterList").val(data.transactiondetailsData[0].customerVendorId).change();
    $("#" + transactionTableTr + " input[class='txnPerUnitPrice']").val(data.transactiondetailsData[0].pricePerUnit);
    $("#" + transactionTableTr + " input[class='txnNoOfUnit']").val(data.transactiondetailsData[0].noOfUnits);
    $("#" + transactionTableTr + " input[class='txnFrieghtCharges']").val(data.transactiondetailsData[0].frieghtCharges);
    $("#" + transactionTableTr + " input[class='txnGross']").val(data.transactiondetailsData[0].grossAmount);
    $("#" + transactionTableTr + " select[class='txnVendorAdvanceType']").val(data.transactiondetailsData[0].advanceType).change();
    $("#" + transactionTableTr + " input[class='howMuchAdvance']").val(data.transactiondetailsData[0].adjustmentFromAdvance);
    $("#" + transactionTableTr + " input[class='txnInputTaxAmount']").val(data.transactiondetailsData[0].totalInputTax);

    if (data.transactiondetailsData[0].taxName1 != "") {
        populateInputTaxTransData(transactionTableTr, 1, data.transactiondetailsData[0].taxName1, data.transactiondetailsData[0].taxValue1);
    }

    if (data.transactiondetailsData[0].taxName2 != "") {
        populateInputTaxTransData(transactionTableTr, 2, data.transactiondetailsData[0].taxName2, data.transactiondetailsData[0].taxValue2);
    }

    if (data.transactiondetailsData[0].taxName3 != "") {
        populateInputTaxTransData(transactionTableTr, 3, data.transactiondetailsData[0].taxName3, data.transactiondetailsData[0].taxValue3);
    }

    if (data.transactiondetailsData[0].taxName4 != "") {
        populateInputTaxTransData(transactionTableTr, 4, data.transactiondetailsData[0].taxName4, data.transactiondetailsData[0].taxValue4);
    }

    if (data.transactiondetailsData[0].taxName5 != "") {
        populateInputTaxTransData(transactionTableTr, 5, data.transactiondetailsData[0].taxName5, data.transactiondetailsData[0].taxValue5);
    }
    if (data.transactiondetailsData[0].taxName6 != "") {
        populateInputTaxTransData(transactionTableTr, 6, data.transactiondetailsData[0].taxName6, data.transactiondetailsData[0].taxValue6);
    }
    if (data.transactiondetailsData[0].taxName7 != "") {
        populateInputTaxTransData(transactionTableTr, 7, data.transactiondetailsData[0].taxName7, data.transactiondetailsData[0].taxValue7);
    }


    $("#" + transactionTableTr + " input[class='netAmountVal']").val(data.transactiondetailsData[0].netAmount);
    $("#" + transactionTableTr + " div[class='netAmountDescriptionDisplay']").text(data.transactiondetailsData[0].netAmountResultDescription);
    $("#" + transactionTableTr + " input[class='withholdingtaxcomponenetdiv']").val(data.transactiondetailsData[0].withholdingTax);
    $("#" + transactionTableTr + " textarea[class='voiceRemarksClass']").val(data.transactiondetailsData[0].remarks);
    //TODO Sunil
    var supportingDocsList = data.transactiondetailsData[0].supportingDocs.split(',');
    for (var j = 0; j < supportingDocsList.length; j++) {
        var supportDocUrl = supportingDocsList[j].split('#');
        if (j == 0) {
            $("#" + transactionTableTr + " input[class='txnUploadSuppDocs']").val(supportDocUrl[1]);
        } else {
            $("#" + transactionTableTr + " input[name='addMoreSupportingDocuments']").click();
            $("#" + transactionTableTr + " div[id='moreSupportingDocDiv'] input:last").val(supportDocUrl[1]);
        }
    }
}

var populateInputTaxTransData = function (transactionTableTr, inputTaxCounter, taxName, taxValue) {
    var divcounter = ""
    if (inputTaxCounter > 1) {
        divcounter = inputTaxCounter - 1;
    }
    $("#" + transactionTableTr + " input[class='txnNoOfInputTaxesAdd']").val(inputTaxCounter);
    $("#" + transactionTableTr + " input[id='inputtaxes']").click();
    var valofText = $("#" + transactionTableTr + " div[class='inputtaxcomponentsdiv'] div[class='dynminputtaxcomponentsdiv" + divcounter + "'] select[class='inputtaxcomponenet'] option").filter(function () {
        return this.text == taxName
    }).val();
    $("#" + transactionTableTr + " div[class='inputtaxcomponentsdiv'] div[class='dynminputtaxcomponentsdiv" + divcounter + "'] select[class='inputtaxcomponenet']").val(valofText);
    $("#" + transactionTableTr + " div[class='inputtaxcomponentsdiv'] div[class='dynminputtaxcomponentsdiv" + divcounter + "'] input[class='inputtaxcomponenetValue']").val(taxValue);
}

var fetchBuyItemsForVisitingVendor = function (elem) {
    var parentTr = $(elem).closest('tr').attr('id');
    $("#branchCashAvailable").val("");
    $("#branchPettyCashAvailable").val("");
    $("#transferPurpose").val("");
    $("#branchCashResultant").val("");
    $("#branchPettyCashResultant").val("");
    $(".budgetDisplay").text("");
    $(".actualbudgetDisplay").text("");
    $(".branchAvailablePettyCash").html("");
    $(".amountRangeLimitRule").text("");
    $(".inputtaxbuttondiv").html("");
    $(".inputtaxcomponentsdiv").html("");
    $(".vendorActPayment").text("");
    $(".withholdingtaxcomponentdiv").text("");
    $("individualtaxdiv").text("");
    $("select[name='procurementRequestForCreator'] option:first").prop("selected", "selected");
    var txnPurposeBranchId = $("#" + parentTr + " select[class='txnBranches']").val();
    if (txnPurposeBranchId == "" || typeof txnPurposeBranchId == 'undefined') {
        swal("Invalid Branch!", "Please select valid Branch", "error");
        return false;
    }
    var jsonData = {};
    var transactionPurposeId = $("#whatYouWantToDo").find('option:selected').val();
    //var text=$("#whatYouWantToDo").find('option:selected').text();
    jsonData.useremail = $("#hiddenuseremail").text();
    jsonData.txnPurposeId = transactionPurposeId;
    jsonData.txnPurposeBnchId = txnPurposeBranchId;
    jsonData.isVisitingVendor = true;

    if (txnPurposeBranchId == "") {
        populateItemBasedOnWhatYouWantToDo(transactionPurposeId, text);
    } else if (txnPurposeBranchId != "") {
        $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        var url = "/transaction/fetchItemsForBranchProject";
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
                $("#" + parentTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").children().remove();
                $("#" + parentTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").append("<option value=''>--Please Select--</option>");
                var txnItemsList = ""
                if (transactionPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transactionPurposeId == BUY_ON_CREDIT_PAY_LATER || transactionPurposeId == BUY_ON_PETTY_CASH_ACCOUNT) {
                    parentTr = $(elem).parent().parent().parent().parent().parent().closest('div').attr('id');
                    $("#" + parentTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").children().remove();
                    $("#" + parentTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").append("<option value=''>--Please Select--</option>");
                    for (var i = 0; i < data.allTxnPurposeItemsData.length; i++) {
                        if (data.allTxnPurposeItemsData[i].accountCode == "2000000000000000000") {
                            txnItemsList = txnItemsList + ('<option value="' + data.allTxnPurposeItemsData[i].id + '">' + data.allTxnPurposeItemsData[i].name + '</option>');
                        }
                    }
                    $("#" + parentTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").append(txnItemsList)
                }
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on fetching Item for visiting Vendor!", "Please retry, if problem persists contact support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
                enableTransactionButtons();
            }
        });
    }
}

var visitingVendorAddMultiItem = function (transactionPurposeId, length) {
    if (transactionPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transactionPurposeId == BUY_ON_CREDIT_PAY_LATER || transactionPurposeId == BUY_ON_PETTY_CASH_ACCOUNT) {
        $("#totalInputTaxes").val(0);
        var multiItemsTableTr = '<tr id="bocpra' + length + '"><td><select class="txnItems"  name="bocpraItem" id="bocpraItem" onChange="populateBuyTranData(this);"><option value="">Please Select</option></select></td>';
        multiItemsTableTr += '<td><div class="klBranchSpecfTd" style="height: 50px; overflow: auto;"></div></td>';
        multiItemsTableTr += '<td style="display:none;"><select class="txnRcmTaxItem" onChange="validateSelectedItems(this);setRcmTaxAndCess(this);"><option value="">Please Select</option></select></td>';
        multiItemsTableTr += '<td><input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text" name="bocprapriceperunits" id="bocprapriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=" calculateGross(this); calculateNetAmount(this);"/></td>';
        multiItemsTableTr += '<td><input class="txnNoOfUnit" type="text" name="bocpraunits" id="bocpraunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="checkStockAvailableForSellTran(this); calculateGross(this); calculateNetAmount(this);" placeholder="Units(if any)"/></td>';
        multiItemsTableTr += '<td><input class="txnGross" placeholder="Gross Amount" type="text" name="bocpragross" id="bocpragross" readonly="readonly"></td>';
        multiItemsTableTr += '<td><select class="txnGstTaxRate" id="bocpraTaxRate" onChange="calculateNetAmount(this);"><option value="">Select</option></select></td>';
        multiItemsTableTr += '<td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div></div><input class="txnTaxTypes" placeholder="Taxes" type="hidden" name="bocpraInTaxDetails" id="bocpraInTaxDetails" readonly="readonly"><input type="hidden" class="inputTaxNames" id="bocprainputTaxNames" name="bocprainputTaxNames" value="0"/><input type="hidden" class="inputTaxValues" id="bocprainputTaxValues" name="bocprainputTaxValues" value="0"/></td>';
        multiItemsTableTr += '<td><select class="txnCessRate" id="bocpraCessRate"  onChange="calculateNetAmount(this);"><option value=""> Select</option></select></td>';
        multiItemsTableTr += '<td><input type="text" class="txnCessTaxAmt" id="bocpraCessAmt" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);"/><input type="hidden" class="itemTaxAmount" name="bocpraTotaltaxamnt" id="bocpraTotaltaxamnt" readonly="readonly" placeholder="Tax Amount"/></td>';
        multiItemsTableTr += '<td><input type="text" class="withholdingtaxcomponenetdiv" onkeyup="calculateNetAmountOnTdsChange(this);" name="bocprawithheldingtaxamnt" id="bocprawithheldingtaxamnt" readonly="readonly" placeholder="WithholdingTax"></td>';
        multiItemsTableTr += '<td><input type="text" class="customerAdvance" placeholder="Advance Available" name="bocpravendoradvance" id="bocpravendoradvance" readonly="readonly"></td>';
        multiItemsTableTr += '<td><input type="text" class="howMuchAdvance" placeholder="Advance to Adjust?" name="bocprahowmuchfromadvance" id="bocprahowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenAdvanceChangedForBuy(this);"></td>';
        multiItemsTableTr += '<td><input type="text" class="netAmountVal" name="bocpranetamnt" id="bocpranetamnt" readonly="readonly" placeholder="Net Amount"></td><td><div class="netAmountDescriptionDisplay" id="bocpranetAmountLabel"></div></td></tr>';

        $("#staticsellmultipleitemsbocpra table[id='multipleItemsTablebocpra'] tbody").append(multiItemsTableTr);
        //$("#multipleItemsTablebocpra > tbody > tr[id='bocpra"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
        //$("#bocpraUnAvailableVendor").change();

    } else if (transactionPurposeId == BUY_ON_CREDIT_PAY_LATER) {
        $("#totalInputTaxes").val(0);
        var multiItemsTableTr = '<tr id="bocapl' + length + '"><td><select class="txnItems"  name="bocaplItem" id="bocaplItem" onChange="populateBuyTranData(this);"><option value="">Please Select</option></select></td>';
        multiItemsTableTr += '<td><input class="txnPerUnitPrice"  style="width:50px;" placeholder="Price Per Unit" type="text" name="bocaplpriceperunits" id="bocaplpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=" calculateGross(this); calculateNetAmount(this);"/></td>';
        multiItemsTableTr += '<td><input class="txnNoOfUnit"  style="width:50px;" type="text" name="bocaplunits" id="bocaplunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="checkStockAvailableForSellTran(this); calculateGross(this); calculateNetAmount(this);" placeholder="Units(if any)"/></td>';
        multiItemsTableTr += '<td><input class="txnGross" style="width:80px;" placeholder="Gross Amount" type="text" name="bocaplgross" id="bocaplgross" readonly="readonly"></td></td>';
        multiItemsTableTr += '<td><input type="button" class="btn btn-submit btn-idos input-small" name="inputtaxes" id="inputtaxes" value="Input Tax" style="margin-top:1px; width: 60px; padding: 5px 7px 5px 7px;" onclick="addBuyTaxComponentsPopup(this);"></td>';
        multiItemsTableTr += '<td><input class="txnTaxTypes" style="width:80px;" placeholder="Taxes" type="text" name="bocaplInTaxDetails" id="bocaplInTaxDetails" readonly="readonly"></td>';
        multiItemsTableTr += '<td><input type="text" style="width:50px;"  class="txnTaxAmount" name="bocapltaxamnt" id="bocapltaxamnt" readonly="readonly" placeholder="Tax Amount"></td>';
        multiItemsTableTr += '<td><input class="withholdingtaxcomponenetdiv" onkeyup="calculateNetAmount(this);" style="width:50px;" placeholder="WithholdingTaxes" type="text" name="bocaplwithheldingtaxamnt" id="bocaplwithheldingtaxamnt" readonly="readonly"></td>';
        multiItemsTableTr += '<td><input type="text" style="width:52px;"  class="customerAdvance" placeholder="Advance Available" name="bocaplvendoradvance" id="bocaplvendoradvance" readonly="readonly"></td>';
        multiItemsTableTr += '<td><input type="text" style="width:75px;"  class="howMuchAdvance" placeholder="Advance to Adjust?" name="bocaplhowmuchfromadvance" id="bocaplhowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="whenAdvanceIsEmpty(this); calculateNetAmountWhenAdvanceChangedForBuy(this);"></td>';
        multiItemsTableTr += '<td><input type="text" style="width:80px;" class="netAmountVal" name="bocaplnetamnt" id="bocaplnetamnt" readonly="readonly" placeholder="Net Result"></td></tr>';
        $("#staticsellmultipleitemsbocapl table[id='multipleItemsTablebocapl'] tbody").append(multiItemsTableTr);
        //$("#multipleItemsTablebocapl > tbody > tr[id='bocapl"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
    } else if (transactionPurposeId == BUY_ON_PETTY_CASH_ACCOUNT) {
        $("#totalInputTaxes").val(0);
        var multiItemsTableTr = '<tr id="bptyca' + length + '"><td><select class="txnItems"  name="bptycaItem" id="bptycaItem" onChange="populateBuyTranData(this);"><option value="">Please Select</option></select></td>';
        multiItemsTableTr += '<td><input class="txnPerUnitPrice"  style="width:50px;" placeholder="Price Per Unit" type="text" name="bptycapriceperunits" id="bptycapriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=" calculateGross(this); calculateNetAmount(this);"/></td>';
        multiItemsTableTr += '<td><input class="txnNoOfUnit"  style="width:50px;" type="text" name="bptycaunits" id="bptycaunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="checkStockAvailableForSellTran(this); calculateGross(this); calculateNetAmount(this);" placeholder="Units(if any)"/></td>';
        multiItemsTableTr += '<td><input class="txnGross" style="width:80px;" placeholder="Gross Amount" type="text" name="bptycagross" id="bptycagross" readonly="readonly"></td></td>';
        multiItemsTableTr += '<td><input type="button" class="btn btn-submit btn-idos input-small" name="inputtaxes" id="inputtaxes" value="Input Tax" style="margin-top:1px; width: 60px; padding: 5px 7px 5px 7px;" onclick="addBuyTaxComponentsPopup(this);"></td>';
        multiItemsTableTr += '<td><input class="txnTaxTypes" style="width:80px;" placeholder="Taxes" type="text" name="bptycaInTaxDetails" id="bptycaInTaxDetails" readonly="readonly"></td>';
        multiItemsTableTr += '<td><input type="text" style="width:50px;"  class="txnTaxAmount" name="bptycataxamnt" id="bptycataxamnt" readonly="readonly" placeholder="Tax Amount"></td>';
        multiItemsTableTr += '<td><input class="withholdingtaxcomponenetdiv" style="width:50px;" placeholder="WithholdingTaxes" onkeyup="calculateNetAmount(this);" type="text" name="bptycawithheldingtaxamnt" id="bptycawithheldingtaxamnt" readonly="readonly"></td>';
        multiItemsTableTr += '<td><input type="text" style="width:52px;"  class="customerAdvance" placeholder="Advance Available" name="bptycavendoradvance" id="bptycavendoradvance" readonly="readonly"></td>';
        multiItemsTableTr += '<td><input type="text" style="width:75px;"  class="howMuchAdvance" placeholder="Advance to Adjust?" name="bptycahowmuchfromadvance" id="bptycahowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="whenAdvanceIsEmpty(this); calculateNetAmountWhenAdvanceChangedForBuy(this);"></td>';
        multiItemsTableTr += '<td><input type="text" style="width:80px;" class="netAmountVal" name="bptycanetamnt" id="bptycanetamnt" readonly="readonly" placeholder="Net Result"></td></tr>';
        //$("#staticsellmultipleitemsbptyca table[id='multipleItemsTablebptyca'] tbody").append(multiItemsTableTr);
        $("#multipleItemsTablebptyca > tbody > tr[id='bptyca" + length + "'] > td > .txnItems").append(customerVendorItemsListTemp);
    }
    initMultiItemsSelect2();
}

function generatePDFBuySelfInvoice(elem) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var formName = $(elem).attr('id');
    var exportType = formName.substring(0, 4);
    var transactionId = formName.substring(4, formName.length);
    var jsonData = {};
    var useremail = $("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    jsonData.entityTxnId = transactionId;
    jsonData.exportType = exportType;
    var url = "/exportInvoicePdf";
    downloadFile(url, "POST", jsonData, "Error on Buy self invoice generation!");
    /*$.ajax({
	   url: url,
	   data:JSON.stringify(jsonData),
	   type:"text",
	   headers:{
			"X-AUTH-TOKEN": window.authToken
		},
	   method:"POST",
	   contentType:'application/json',
	   success: function (data) {
		   var dt = new Date().toString();
		   var fileName=data.invoiceFileName[0].fileName;
		   var url='assets/report/'+fileName+'?unique='+dt;
	       var childwindow=window.open(url);

	   },
	   error: function (xhr, status, error) {
	   		if(xhr.status == 401){
	   			doLogout();
	   		}else if(xhr.status == 500){
	    		swal("Error on invoice generation!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});*/
}

/*************************************
 ************Need to delete below once GST buy is done
 ***************************************************/

function addBuyTaxComponentsPopup(elem) {
    var parentTr1 = $(elem).parent().parent().parent().attr('id');
    var parentTr = $(elem).closest('tr').attr('id');
    var txnBranchId = "";
    var jsonData = {};
    var transactionPurposeId = $("#whatYouWantToDo").find('option:selected').val();
    var text = $("#whatYouWantToDo").find('option:selected').text();
    var inputTaxesLength = $("#noOfInputTaxesAdd").val();
    var inputTaxNamesArr = "";
    if (inputTaxesLength == "7") {
        swal("Limit exceeded!", "Maximum 7 taxes allowed to apply for a transaction.", "error");
        return false;
    }
    if (transactionPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transactionPurposeId == BUY_ON_CREDIT_PAY_LATER || transactionPurposeId == BUY_ON_PETTY_CASH_ACCOUNT || transactionPurposeId == PURCHASE_RETURNS) {
        var parentOfparentTr = $(elem).parents().closest('tr').attr('id');
        txnBranchId = $("#" + parentOfparentTr + " select[class='txnBranches'] option:selected").val();
    } else {
        txnBranchId = $("#" + parentTr + " select[class='txnBranches'] option:selected").val();
    }
    var txnItemTableIdHid = $("#" + parentTr + " input[class='txnItemTableIdHid']").val();
    jsonData.useremail = $("#hiddenuseremail").text();
    jsonData.txnPurposeId = transactionPurposeId;
    jsonData.txnBranchId = txnBranchId;
    if (transactionPurposeId == PURCHASE_RETURNS && typeof txnItemTableIdHid != 'undefined') {
        jsonData.txnItemTableIdHid = txnItemTableIdHid;
    } else {
        jsonData.txnItemTableIdHid = null;
    }
    if (txnBranchId != "") {
        var url = "/transaction/getBranchInputTaxList";
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
                //if(parentTr.indexOf("bocpra")!=-1){
                $("#staticBuyTaxes").attr('data-toggle', 'modal');
                $("#staticBuyTaxes").modal('show');
                $(".staticBuyTaxesclose").attr("href", location.hash);
                $("#staticBuyTaxes div[class='modal-body']").html("");
                $("#staticBuyTaxes div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="multipleBuyTaxesBreakupTable" style="margin-top: 0px; width:450px;">' +
                    '<thead class="tablehead1" style="position:relative"><th>Tax Name</th><th>Tax Value</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
                for (var i = 0; i < data.inputTaxList.length; i++) {
                    $("#staticBuyTaxes div[class='modal-body'] table[id='multipleBuyTaxesBreakupTable'] tbody").append('<tr id=' + data.inputTaxList[i].inputTaxID + '><td><input class="taxName" style="width:80px;" type="text" name="bocpraTaxName" id="bocpraTaxName" readonly="readonly" value=' + data.inputTaxList[i].inputTaxName + '></td><td><input type="text" id="bocprainputtaxcomponenetamt" class="inputtaxcomponenetValue" name="bocprainputtaxcomponenetamt" style="width:49px;margin-top:5px;" value=' + data.inputTaxList[i].inputTaxValue + ' onkeyup="totalInputBuyTaxAmount(this,' + parentTr + ');" onkeypress="return onlyDotsAndNumbers(event);"></td></tr>');
                    inputTaxNamesArr = inputTaxNamesArr + "," + data.inputTaxList[i].inputTaxName;
                }
                $(".inputTaxNames").val(inputTaxNamesArr);
                $("#staticBuyTaxes div[class='modal-body'] table[id='multipleBuyTaxesBreakupTable'] tbody").append('<tr><td>Total Taxes:</td><td><input type="text" style="width:80px;" class="netTaxAmt" name="netTaxAmt" id="netTaxAmt" readonly="readonly" placeholder="Net Result"></td></tr>');
                $("#staticBuyTaxes div[class='modal-body'] table[id='multipleBuyTaxesBreakupTable'] tbody").append('<tr><td></td><td><input type="button" style="width:60px;" id="submitInputTax" name="submitInputTax" class="btn btn-submit" value="Submit" onclick="submitInputTax(this,\'' + parentTr + '\',\'' + parentOfparentTr + '\');"></td></tr>');
                //}
                $("#noOfInputTaxesAdd").val(parseInt(inputTaxesLength) + 1);
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on fetching tax detail!", "Please retry, if problem persists contact support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
            }
        });
    }/*else{
		$("#noOfInputTaxesAdd").val(parseInt(inputTaxesLength) + 1);
		var content=$("#"+parentTr+" div[class='inputtaxcomponentsdiv'] div[class='dynminputtaxcomponentsdiv']").html();
		$("#"+parentTr+" div[class='inputtaxcomponentsdiv']").append('<div class="dynminputtaxcomponentsdiv' + inputTaxesLength + '">');
		$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv" + inputTaxesLength +"']").append(content + '</div>');
	}*/
}

function submitInputTax(elem, parentTr, parentOfparentTr) {
    $("#staticBuyTaxes").modal('hide');
    var inputTaxNames = $(".inputTaxNames").val();
    var inputTaxNamesArr = inputTaxNames.split(",");
    var inputTaxValues = $(".inputTaxValues").val();
    var inputTaxValuesArr = inputTaxValues.split(",");
    var inputTaxNameValue = "";
    for (var i = 0; i < inputTaxValuesArr.length; i++) {
        if (inputTaxValuesArr[i] != "" && inputTaxValuesArr[i] != 0) {
            inputTaxNameValue = inputTaxNameValue + inputTaxNamesArr[i] + "=" + inputTaxValuesArr[i] + ",";
        }
    }
    $("#" + parentTr + " input[class='txnTaxTypes']").val(inputTaxNameValue);
    var txnTotalInputTaxes = $(".totalInputTaxes").val();
    $("#" + parentTr + " input[class='txnTaxAmount']").val(txnTotalInputTaxes);
    calculateBuyNetAmount(this, parentTr, parentOfparentTr);
}

function totalInputBuyTaxAmount(elem, mainparentTr) {
    var parentTr = $(elem).parent().parent().parent().parent().attr('id');
    var inputTaxValuesArr = "";
    if (parentTr == "multipleBuyTaxesBreakupTable") {
        var taxTotal = "0.0";
        var taxNames = "";
        var taxName = $("#" + parentTr + " select[name='bocprainputtaxcomponenet'] option:selected").text();
        if (taxName == "Select") {
            swal("Incomplete tax data!", "Please first choose TaxName from dropdown.", "error");
            $("#" + parentTr + " div[class='" + parentDiv + "'] input[name='bocprainputtaxcomponenetamt']").val("");
        } else {
            $("#" + parentTr + " input[name='bocprainputtaxcomponenetamt']").each(function () {
                if ($(this).val() != "") {
                    taxTotal = parseFloat(taxTotal) + parseFloat($(this).val());
                }
                inputTaxValuesArr = inputTaxValuesArr + "," + $(this).val();
            });
            $(".inputTaxValues").val(inputTaxValuesArr);
            if (parseFloat(taxTotal) > 0) {
                $("#" + parentTr + " input[name='netTaxAmt']").val(parseFloat(taxTotal));
                $(".totalInputTaxes").val(parseFloat(taxTotal));
            } else {
                $("#" + parentTr + " input[name='inputtaxesamt']").val("");
                $("#" + parentTr + " input[name='inputtaxesamt']").attr('placeholder', "Amount");
            }
        }
    }
}

//*****************************************************************************/

var popupBuyTxnAddDetailModal = function (elem) {
    var parentTr = $(elem).closest('tr').attr('id');
    var txnTypeOfSupply = $("#" + parentTr + " select[class='txnTypeOfSupply'] option:selected").val();
    var whatYouWantToDoVal = $("#whatYouWantToDo").find('option:selected').val();
    if (whatYouWantToDoVal == CREDIT_NOTE_VENDOR || whatYouWantToDoVal == DEBIT_NOTE_VENDOR) {
        $(".hideResoneForReturn").removeClass('hidden');
    } else {
        $(".hideResoneForReturn").addClass('hidden');
    }

    if (txnTypeOfSupply == "") {
        swal("Incomplete transaction detail!", "Please select a Type of Supply.", "error");
        return false;
    } else if (txnTypeOfSupply == "2" || txnTypeOfSupply == "3") {
        $("#staticBuyTxnAddDetail .currencyTr").hide();
    } else {
        $("#staticBuyTxnAddDetail .currencyTr").show();
    }

    $("#staticBuyTxnAddDetail").attr('data-toggle', 'modal');
    $("#staticBuyTxnAddDetail").modal('show');
}

var closeBuyTxnAddDetailModal = function (elem) {
    $("#closeBuyTxnAddDetailBtn").click();
}

var setBuyTxnAdditionalDetail = function (jsonData) {
    var invRefDate = $("#staticBuyTxnAddDetail input[id='buyTxnRefDateTxt']").val();
    var invRefNumber = $("#staticBuyTxnAddDetail input[id='buyTxnRefNumber']").val();
    var grnRefDate = $("#staticBuyTxnAddDetail input[id='buyTxnGrnDateTxt']").val();
    var grnRefNumber = $("#staticBuyTxnAddDetail input[id='buyTxnGrnRefNumber']").val();
    var impRefDate = $("#staticBuyTxnAddDetail input[id='buyTxnImpRefDateTxt']").val();
    var impRefNumber = $("#staticBuyTxnAddDetail input[id='buyTxnImpRefNumber']").val();
    var destCountry = $("#staticBuyTxnAddDetail div[class='bfh-selectbox bfh-countries'] span[class='bfh-selectbox-option']").text();
    var destCurrencyCode = $("#staticBuyTxnAddDetail input[id='destCurrencyCode']").val();
    var amount = $("#staticBuyTxnAddDetail input[id='buyTxnImpAmount']").val();
    var portCode = $("#staticBuyTxnAddDetail input[id='portCode']").val();
    var remarksAddDetailsBuy = $("#staticBuyTxnAddDetail textarea[id='remarksAddDetailsBuy']").val();
    var reasonForReturn = $("#staticBuyTxnAddDetail select[id='reasonForReturn']").val();
    jsonData.destCountry = destCountry;
    jsonData.destCurrencyCode = destCurrencyCode;
    jsonData.amount = amount;
    jsonData.datetimeOfShipping = invRefDate;
    jsonData.invRefNumber = invRefNumber;
    jsonData.grnRefDate = grnRefDate;
    jsonData.grnRefNumber = grnRefNumber;
    jsonData.impRefDate = impRefDate;
    jsonData.impRefNumber = impRefNumber;
    jsonData.portCode = portCode;
    jsonData.reasonForReturn = reasonForReturn;
    jsonData.remarksAddDetailsBuy = remarksAddDetailsBuy;
}

function getReserveChargesRCMData(elem) {
    var returnValue = true;
    var txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val();
    if (txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT) {
        var parentTr = $(elem).closest('tr').attr('id');

        var text = $("#whatYouWantToDo").find('option:selected').text();
        if (parentTr.indexOf("transactionEntity") != -1) {
            text = $("#" + parentTr + " select[id='whatYouWantToDo'] option:selected").text();
        }
        mainTableTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
        var vendorcustomer = $("#" + mainTableTr + " .masterList option:selected").val();

        var visitingCustomer = "";
        if (vendorcustomer == "") {
            visitingCustomer = $("#" + mainTableTr + " input[name='unAvailableVendor']").val();
        }
        var selectedBranch = $("#" + mainTableTr + " select[class='txnBranches'] option:selected").val();

        var txnTypeOfSupply = $("#" + mainTableTr + " select[class='txnTypeOfSupply'] option:selected").val();

        var itemId = $("#" + parentTr + " .txnItems option:selected").val();
        if (itemId == "") {
            return false;
        }
        if (selectedBranch == "") {
            swal("Missing Branch!", "	Branch Selection is mandatory to proceed.", "error");
            returnValue = false;
        }
        if (txnTypeOfSupply == "") {
            swal("Missing Type Of Supply!", "Type Of Supply is mandatory to proceed.", "error");
            returnValue = false;
        }
        if (vendorcustomer == "" && visitingCustomer == "") {
            swal("Missing customer/vendor!", "Customer/vendor is mandatory to proceed.", "error");
            returnValue = false;
        }

        if (txnTypeOfSupply == "2" || txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5") {
            var txnDate = $("#" + mainTableTr).find(".txnBackDate").val();
            var jsonData = {};
            jsonData.useremail = $("#hiddenuseremail").text();
            jsonData.userTxnPurposeText = text;
            jsonData.txnPurposeVal = txnPurposeVal;
            jsonData.txnVendCustId = vendorcustomer;
            jsonData.itemId = itemId;
            jsonData.txnvisitingCustomer = visitingCustomer;
            jsonData.txnBranchId = selectedBranch;
            jsonData.txnTypeOfSupply = txnTypeOfSupply;
            jsonData.txnDate = txnDate;
            $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
            var url = "/transaction/rcmItems";
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
                    if (txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR) {
                        if (typeof data.tdsPayableSpecific != 'undefined' && data.tdsPayableSpecific == 0) {
                            swal("COA: mapping missing", "Chart of Account, TDS Payable mapping is not defined, please define and try.", "error");
                            $(elem).val('');
                            initMultiItemsSelect2();
                            disableTransactionButtons();
                            returnValue = false;
                        }
                        $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").text("");
                        $("#" + parentTr).attr("withholdingtaxRate", "");
                        $("#" + parentTr).attr("tdstxnlimit", "");
                        $("#" + parentTr).attr("modeOfTDSCompute", data.modeOfTdsCompute);
                        $("#" + parentTr).attr("specficIsTDSSpec", data.specficIsTDSSpec);
                        var withholdingAmount = 0;
                        for (var i = 0; i < data.branchTdsDetail.length; i++) {
                            $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Rate: ' + data.branchTdsDetail[i].withholdingtaxRate + " %, ");
                            $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Limit: ' + data.branchTdsDetail[i].withholdingtaxLimit + ", ");
                            $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Withholding Monetory Limit: ' + data.branchTdsDetail[i].withHoldingMonetoryLimit + ",");
                            $("#" + parentTr).attr("withholdingtaxRate", data.branchTdsDetail[i].withholdingRate);
                            $("#" + parentTr).attr("tdstxnlimit", data.branchTdsDetail[i].withholdingtaxLimit);
                        }
                    }

                    $("#" + parentTr + " .txnRcmTaxItem").html("");
                    var reverseChargeItems = ['<option value="" selected>Please Select</option>'];
                    var j = 1;
                    if (data.reverseChargeData.length > 0) {
                        for (var i = 0; i < data.reverseChargeData.length; i++) {
                            reverseChargeItems[j++] = '<option value="';
                            reverseChargeItems[j++] = data.reverseChargeData[i].id;
                            reverseChargeItems[j++] = '" taxRate="';
                            reverseChargeItems[j++] = data.reverseChargeData[i].taxRate;
                            reverseChargeItems[j++] = '" cessRate="';
                            reverseChargeItems[j++] = data.reverseChargeData[i].cessRate;
                            reverseChargeItems[j++] = '" taxRateName="';
                            reverseChargeItems[j++] = data.reverseChargeData[i].taxRateName;
                            reverseChargeItems[j++] = '" taxNameId="';
                            reverseChargeItems[j++] = data.reverseChargeData[i].taxNameId;
                            reverseChargeItems[j++] = '" cessRateName="';
                            reverseChargeItems[j++] = data.reverseChargeData[i].cessRateName;
                            reverseChargeItems[j++] = '" cessNameId="'
                            reverseChargeItems[j++] = data.reverseChargeData[i].cessNameId;
                            reverseChargeItems[j++] = '" >';
                            reverseChargeItems[j++] = data.reverseChargeData[i].description;
                            reverseChargeItems[j++] = '</option>';
                        }
                        $("#" + parentTr + " .txnRcmTaxItem").html(reverseChargeItems.join(""));
                    } else {
                        $("#" + parentTr + " .txnRcmTaxItem").html(reverseChargeItems.join(""));
                    }
                    initMultiItemsSelect2();
                },
                error: function (xhr, status, error) {
                    if (xhr.status == 401) {
                        doLogout();
                    } else if (xhr.status == 500) {
                        swal("Error on fetching advance/discount!", "Please retry, if problem persists contact support team", "error");
                    }
                },
                complete: function (data) {
                    $.unblockUI();
                }
            });

        }
    }

    return returnValue;
}


var changeMultipleItemTableForBuyTrans = function (transPurposeId, txnTypeOfSupply, parentId, elem, transactionTableTr) {

    var taxTD = '<select class="txnGstTaxRate" id="bocpraTaxRate" onChange="calculateNetAmount(this);"><option value="">Select</option></select>';
    var AmtTD = '<div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div></div><input class="txnTaxTypes" placeholder="Taxes" type="hidden" name="bocpraInTaxDetails" id="bocpraInTaxDetails" readonly="readonly">';
    var cessTD = '<select class="txnCessRate" id="bocpraCessRate"  onChange="calculateNetAmount(this);"><option value=""> Select</option></select>';
    var cessAmtTD = '<input type="text" class="txnCessTaxAmt" id="bocpraCessAmt" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);"/><input type="hidden" class="itemTaxAmount" name="bocpraTotaltaxamnt" id="bocpraTotaltaxamnt" readonly="readonly" placeholder="Tax Amount"/><input type="hidden" class="actualbudgetDisplayVal" id="bocpraactualbudgetDis" name="bocpraactualbudgetDis" value="0"/><input type="hidden" class="budgetDisplayVal" id="bocprabudgetDisplay" name="bocprabudgetDisplay" value="0"/><input type="hidden" class="amountRangeFromLimit" id="bocpraAmountRangeFromLimit" value="0"/><input type="hidden" class="amountRangeToLimit" id="bocpraAmountRangeToLimit" value="0"/>';
    if (transPurposeId != BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId != BUY_ON_CREDIT_PAY_LATER) {
        if (txnTypeOfSupply == "2" || txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5") {

            $("#" + parentId + " .multipleItemsTable").find('.rcmCompToshowGood').removeClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmCompToshow').removeClass('hidden');
            if (txnTypeOfSupply == "2" || txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5") {
                $("#" + parentId + " .multipleItemsTable").find('.rcmTaxRateThLast').removeClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmTaxAmountThLast').removeClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmTaxRateThMiddle').addClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmTaxAmountThMiddle').addClass('hidden');
                //cess
                $("#" + parentId + " .multipleItemsTable").find('.rcmCessRateThLast').removeClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmCessAmountThLast').removeClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmCessRateThMiddle').addClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmCessAmountThMiddle').addClass('hidden');
                if (txnTypeOfSupply == "2" || txnTypeOfSupply == "3") {
                    $("#" + parentId + " .multipleItemsTable").find('.rcmCompToshowGood').addClass('hidden');
                }
                $("#" + parentId).find('.multipleItemsTable tr').each(function () {

                    $(this).find('.taxAmountTdMiddle').html("");
                    $(this).find('.taxRateTdMiddle').html("");
                    $(this).find('.taxAmountTdMiddle').addClass('hidden');
                    $(this).find('.taxRateTdMiddle').addClass('hidden');
                    $(this).find('.taxRateTdLast').html('<input class="txnGstTaxRate" type="text" readonly id="bocpraTaxRate" onChange="calculateNetAmount(this);">');
                    $(this).find('.taxAmountTdLast').html(AmtTD);
                    $(this).find('.taxRateTdLast').removeClass('hidden');
                    $(this).find('.taxAmountTdLast').removeClass('hidden');

                    // cess
                    $(this).find('.cessAmountTdMiddle').html("");
                    $(this).find('.cessRateTdMiddle').html("");
                    $(this).find('.cessAmountTdMiddle').addClass('hidden');
                    $(this).find('.cessRateTdMiddle').addClass('hidden');
                    $(this).find('.cessRateTdLast').html('<input class="txnCessRate" id="bocpraCessRate" type="text" readonly onChange="calculateNetAmount(this);">');
                    $(this).find('.cessAmountTdLast').html(cessAmtTD);
                    $(this).find('.cessRateTdLast').removeClass('hidden');
                    $(this).find('.cessAmountTdLast').removeClass('hidden');
                });
            } else {
                $("#" + parentId + " .multipleItemsTable").find('.rcmTaxRateThLast').addClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmTaxAmountThLast').addClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmTaxRateThMiddle').removeClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmTaxAmountThMiddle').removeClass('hidden');
                // cess
                $("#" + parentId + " .multipleItemsTable").find('.rcmCessRateThLast').addClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmCessAmountThLast').addClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmCessRateThMiddle').removeClass('hidden');
                $("#" + parentId + " .multipleItemsTable").find('.rcmCessAmountThMiddle').removeClass('hidden');
                $("#" + parentId).find('.multipleItemsTable tr').each(function () {
                    $(this).find('.taxAmountTdMiddle').removeClass('hidden');
                    $(this).find('.taxRateTdMiddle').removeClass('hidden');
                    $(this).find('.taxRateTdMiddle').html(taxTD);
                    $(this).find('.taxAmountTdMiddle').html(AmtTD);
                    $(this).find('.taxRateTdLast').html("");
                    $(this).find('.taxAmountTdLast').html("");
                    $(this).find('.taxRateTdLast').addClass('hidden');
                    $(this).find('.taxAmountTdLast').addClass('hidden');
                    // cess
                    $(this).find('.cessAmountTdMiddle').removeClass('hidden');
                    $(this).find('.cessRateTdMiddle').removeClass('hidden');
                    $(this).find('.cessRateTdMiddle').html(cessTD);
                    $(this).find('.cessAmountTdMiddle').html(cessAmtTD);
                    $(this).find('.cessRateTdLast').html("");
                    $(this).find('.cessAmountTdLast').html("");
                    $(this).find('.cessRateTdLast').addClass('hidden');
                    $(this).find('.cessAmountTdLast').addClass('hidden');
                });
            }

        } else {
            $("#" + parentId + " .multipleItemsTable").find('.rcmCompToshowGood').addClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmCompToshow').addClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmTaxRateThLast').addClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmTaxAmountThLast').addClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmTaxRateThMiddle').removeClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmTaxAmountThMiddle').removeClass('hidden');
            // cess
            $("#" + parentId + " .multipleItemsTable").find('.rcmCessRateThLast').addClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmCessAmountThLast').addClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmCessRateThMiddle').removeClass('hidden');
            $("#" + parentId + " .multipleItemsTable").find('.rcmCessAmountThMiddle').removeClass('hidden');
            $("#" + parentId).find('.multipleItemsTable tr').each(function () {
                $(this).find('.taxAmountTdMiddle').removeClass('hidden');
                $(this).find('.taxRateTdMiddle').removeClass('hidden');
                $(this).find('.taxRateTdMiddle').html(taxTD);
                $(this).find('.taxAmountTdMiddle').html(AmtTD);
                $(this).find('.taxRateTdLast').html("");
                $(this).find('.taxAmountTdLast').html("");
                $(this).find('.taxRateTdLast').addClass('hidden');
                $(this).find('.taxAmountTdLast').addClass('hidden');
                // cess
                $(this).find('.cessAmountTdMiddle').removeClass('hidden');
                $(this).find('.cessRateTdMiddle').removeClass('hidden');
                $(this).find('.cessRateTdMiddle').html(cessTD);
                $(this).find('.cessAmountTdMiddle').html(cessAmtTD);
                $(this).find('.cessRateTdLast').html("");
                $(this).find('.cessAmountTdLast').html("");
                $(this).find('.cessRateTdLast').addClass('hidden');
                $(this).find('.cessAmountTdLast').addClass('hidden');
            });
            //$("#"+parentId +" .multipleItemsTable tbody tr").remove();

        }
    }
    fetchBranchInTaxesCess($("#" + transactionTableTr + " select[class='txnBranches']"));
}

var calculateBuyRCMTaxNetAmount = function (elem, parentTr, parentOfparentTr, txnGrossAmount, txnAdjustmentAmount, txnTypeOfSupply, txnVendorCustomer) {
    var netAmount = "";
    var withholdingAmount = 0;
    var txnGstTaxRate = $("#" + parentTr + " input[class='txnGstTaxRate']").val();
    var txnCessRate = $("#" + parentTr + " input[class='txnCessRate']").val();
    var withholdingtaxRate = $("#" + parentTr).attr("withholdingtaxRate");
    var modeOfTDSCompute = $("#" + parentTr).attr("modeOfTDSCompute");
    var tdsTaxLimit = $("#" + parentTr).attr("tdstxnlimit");

    var typeOfSupply = $("#" + parentOfparentTr + " select[class='txnTypeOfSupply']").val();
    var sourceGst = $("#" + parentOfparentTr + " select[class='txnBranches'] option:selected").attr('id');
    var destinationGst = $("#" + parentOfparentTr + " .txnDestGstinCls").val();

    if (txnGrossAmount != "") {
        txnGrossAmount = Number(txnGrossAmount);
        if (txnAdjustmentAmount != "") {
            txnAdjustmentAmount = Number(txnAdjustmentAmount);
            netAmount = txnGrossAmount - txnAdjustmentAmount;
        } else {
            netAmount = txnGrossAmount;
            txnAdjustmentAmount = 0;
        }
        if (modeOfTDSCompute == 1) {
            if (tdsTaxLimit != "" && Number(txnGrossAmount) > Number(tdsTaxLimit)) {
                if (withholdingtaxRate != "" && Number(withholdingtaxRate) > 0) {
                    withholdingAmount = (withholdingtaxRate / 100.0) * (txnGrossAmount);
                }
                if (withholdingAmount != "") {
                    netAmount = netAmount - withholdingAmount;
                }
                $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append('Withholding Tax: ' + parseFloat(withholdingAmount) + '(-), ');
            }

            //$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").attr('readonly','readonly');
            $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val(withholdingAmount);
        } else if (modeOfTDSCompute == 2 || txnVendorCustomer == "") {
            //$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").removeAttr('readonly');
        } else {
            //$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").attr('readonly','readonly');
        }
        $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").removeAttr('readonly'); // Any mode required Editabale

        // Tax
        var index = 0;
        $("#" + parentTr + " input[class='netAmountVal']").val(Math.round(netAmount).toFixed(2));
        let netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
        $("#" + parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));
        var source = "";
        var destination = "";
        if (sourceGst != "" && destinationGst != "" && sourceGst != "undefined" && destinationGst != "undefined") {
            source = sourceGst.substring(0, 2);
            destination = destinationGst.substring(0, 2);
        }
        if (source != "" && destination != "" && source == destination && (typeOfSupply == 2 || typeOfSupply == 3)) {
            var rcmSelected = $("#" + parentTr + " select[class='txnRcmTaxItem'] option:selected").val();
            var taxIgstId = $("#" + parentTr + " select[class='txnRcmTaxItem'] option:selected").attr('taxnameid');
            if (rcmSelected == "" || typeof rcmSelected === "undefined" || taxIgstId == "" || typeof taxIgstId === "undefined") {
                $("#" + parentTr + " div[id='taxCell" + index + "']").empty();
            } else {
                var jsonData = {};
                var transPurposeId = $("#whatYouWantToDo").find('option:selected').val();
                jsonData.useremail = $("#hiddenuseremail").text();
                jsonData.taxIgstId = taxIgstId;
                $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
                var url = "/transaction/getReverseChargeTaxforTypeOfSupply";
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
                        if (data.status == true) {
                            $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").empty();
                            $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").addClass('divbuy-w200');
                            $("#" + parentTr + " div[id='taxCell']").addClass('divbuy-w200');
                            if (data.sgstData.length > 0) {
                                var sgstRate = Number(data.sgstData[0].taxRateSgst);
                                var txnDutiesAndTaxesAmt = $("#" + parentTr + " input[class='txnDutiesAndTaxes']").val();
                                var taxAmt = (txnGrossAmount * sgstRate) / 100;
                                var taxCellData = '<input type="hidden" class="taxRate" name="bocpraTaxRate" id="bocpraTaxRate" readonly="readonly" value="' + sgstRate + '"/>';
                                taxCellData += '<input type="text" style="width:62px;" class="txnTaxAmount" name="bocprataxamnt" id="bocprataxamnt" readonly="readonly" value="' + taxAmt + '"/>';
                                taxCellData += '<input type="hidden" class="txnTaxName" name="bocpraTaxName" id="bocpraTaxName" value="' + data.sgstData[0].taxRateNameSgst + '"/>';
                                taxCellData += '<input type="hidden" class="txnTaxID" name="bocpraTxnTaxID" id="bocpraTxnTaxID" value="' + data.sgstData[0].taxNameIdSgst + '"/>';
                                $("#" + parentTr + " div[id='taxCell" + index + "']").addClass('buyTaxCellCls');
                                $("#" + parentTr + " div[id='taxCell" + index + "']").empty();
                                $("#" + parentTr + " div[id='taxCell" + index + "']").append(taxCellData);
                                $("#" + parentTr + " div[id='taxCell" + index + "']").removeAttr('class', 'divbuy-w000');
                                $("#" + parentTr + " div[id='taxCell" + index + "']").css("display", "inline");
                                $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").append('<div class="taxNameHead">SGST&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>');
                                index++;
                            } else {
                                $("#" + parentTr + " div[id='taxCell" + index + "']").empty();

                            }
                            if (data.cgstData.length > 0) {
                                var cgstRate = Number(data.cgstData[0].taxRateCgst);
                                var taxAmt = (txnGrossAmount * cgstRate) / 100;
                                var taxCellData = '<input type="hidden" class="taxRate" name="bocpraTaxRate" id="bocpraTaxRate" readonly="readonly" value="' + cgstRate + '"/>';
                                taxCellData += '<input type="text" style="width:62px;" class="txnTaxAmount" name="bocprataxamnt" id="bocprataxamnt" readonly="readonly" value="' + taxAmt + '"/>';
                                taxCellData += '<input type="hidden" class="txnTaxName" name="bocpraTaxName" id="bocpraTaxName" value="' + data.cgstData[0].taxRateNameCgst + '"/>';
                                taxCellData += '<input type="hidden" class="txnTaxID" name="bocpraTxnTaxID" id="bocpraTxnTaxID" value="' + data.cgstData[0].taxNameIdCgst + '"/>';
                                $("#" + parentTr + " div[id='taxCell" + index + "']").addClass('buyTaxCellCls');
                                $("#" + parentTr + " div[id='taxCell" + index + "']").empty();
                                $("#" + parentTr + " div[id='taxCell" + index + "']").append(taxCellData);
                                $("#" + parentTr + " div[id='taxCell" + index + "']").removeAttr('class', 'divbuy-w000');
                                $("#" + parentTr + " div[id='taxCell" + index + "']").css("display", "inline");
                                $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").append('<div class="taxNameHead">CGST&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>');
                                index++;
                            } else {
                                $("#" + parentTr + " div[id='taxCell" + index + "']").empty();

                            }
                        }

                    },
                    error: function (xhr, status, error) {
                        if (xhr.status == 401) {
                            doLogout();
                        } else if (xhr.status == 500) {
                            swal("Error on fetching taxes!", "Please retry, if problem persists contact support team", "error");
                        }
                    },
                    complete: function (data) {
                        $.unblockUI();
                    }
                });

            }

        } else {
            if (txnGstTaxRate != "") {
                txnGstTaxRate = Number(txnGstTaxRate);
                var txnDutiesAndTaxesAmt = $("#" + parentTr + " input[class='txnDutiesAndTaxes']").val();
                if (txnDutiesAndTaxesAmt != "") {
                    txnDutiesAndTaxesAmt = Number(txnDutiesAndTaxesAmt);
                } else {
                    txnDutiesAndTaxesAmt = 0;
                }
                var taxAmt = ((txnGrossAmount + txnDutiesAndTaxesAmt) * txnGstTaxRate) / 100;
                var taxCellData = '<input type="hidden" class="taxRate" name="bocpraTaxRate" id="bocpraTaxRate" readonly="readonly" value="' + txnGstTaxRate + '"/>';
                taxCellData += '<input type="text" style="width:62px;" class="txnTaxAmount" name="bocprataxamnt" id="bocprataxamnt" readonly="readonly" value="' + taxAmt + '"/>';
                taxCellData += '<input type="hidden" class="txnTaxName" name="bocpraTaxName" id="bocpraTaxName" value="' + $("#" + parentTr + " input[class='txnGstTaxRate']").data('taxName') + '"/>';
                taxCellData += '<input type="hidden" class="txnTaxID" name="bocpraTxnTaxID" id="bocpraTxnTaxID" value="' + $("#" + parentTr + " input[class='txnGstTaxRate']").data('taxId') + '"/>';
                $("#" + parentTr + " div[id='taxCell" + index + "']").addClass('buyTaxCellCls');
                $("#" + parentTr + " div[id='taxCell" + index + "']").empty();
                $("#" + parentTr + " div[id='taxCell" + index + "']").append(taxCellData);
                $("#" + parentTr + " div[id='taxCell" + index + "']").removeAttr('class', 'divbuy-w000');
                $("#" + parentTr + " div[id='taxCell" + index + "']").show();

                $("#" + parentTr + " div[id='taxCell']").addClass('divbuy-w100');
                $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").addClass('divbuy-w100');
                $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").empty();
                $("#" + parentOfparentTr + " table[id='multipleItemsTablebocpra'] div[id='taxNameList']").append('<div class="taxNameHead">IGST&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>');
                index++;
            } else {
                $("#" + parentTr + " div[id='taxCell" + index + "']").empty();
            }
        }

    }
    if (txnCessRate != "") {
        txnCessRate = Number(txnCessRate);
        var txnDutiesAndTaxesAmt = $("#" + parentTr + " input[class='txnDutiesAndTaxes']").val();
        if (txnDutiesAndTaxesAmt != "") {
            txnDutiesAndTaxesAmt = Number(txnDutiesAndTaxesAmt);
        } else {
            txnDutiesAndTaxesAmt = 0;
        }
        var cessAmt = ((txnGrossAmount + txnDutiesAndTaxesAmt) * txnCessRate) / 100;

        $("#" + parentTr + " input[class='txnCessTaxAmt']").val(cessAmt);
        var taxCellData = '<input type="hidden" class="taxRate" name="bocpraTaxRate" id="bocpraTaxRate" readonly="readonly" value="' + txnCessRate + '"/>';
        taxCellData += '<input type="hidden" class="txnTaxAmount" name="bocprataxamnt" id="bocprataxamnt" readonly="readonly" value="' + cessAmt + '"/>';
        taxCellData += '<input type="hidden" class="txnTaxName" name="bocpraTaxName" id="bocpraTaxName" value="' + $("#" + parentTr + " input[class='txnCessRate']").data("taxName") + '"/>';
        taxCellData += '<input type="hidden" class="txnTaxID" name="bocpraTxnTaxID" id="bocpraTxnTaxID" value="' + $("#" + parentTr + " input[class='txnCessRate']").data("taxId") + '"/>';
        $("#" + parentTr + " div[id='taxCell" + index + "']").empty();
        $("#" + parentTr + " div[id='taxCell" + index + "']").append(taxCellData);
        index++;

    } else {
        $("#" + parentTr + " input[class='txnCessTaxAmt']").val("");
    }

}

function showExceedLimitPopup(parentTr) {
    var isLimitExceeds = false;
    var uploadDoc = $("#" + parentTr + " input[name='bocprauploadSuppDocs']").val();
    var parentOfParent = $("#" + parentTr).closest(".transactionDetailsTable").attr('id');
    $("#" + parentOfParent).find(".multipleItemsTable tbody tr").each(function () {
        var exceedStatus = $(this).attr("exceedLimit");
        if (exceedStatus == "true") {
            isLimitExceeds = true;
            return false;
        }
    });
    if (isLimitExceeds) {
        if (uploadDoc == "" || uploadDoc == 'undefined') {
            swal("Exceeds Monetory Limit for Branch!", "Please upload Supporting documents for complete transaction.", "error");
            return true;
        }
    }
    return false;
}

function grnReportDownload(elem) {
    var elemId = $(elem).attr('id');
    var exportType = elemId.substring(0, 4);
    var transactionId = elemId.substring(4, elemId.length);
    var jsonData = {};
    var useremail = $("#hiddenuseremail").text();
    jsonData.email = useremail;
    jsonData.exportType = exportType;
    jsonData.entityTxnId = transactionId;
    var url = "/exportInvoicePdf";
    downloadFile(url, "POST", jsonData, "Error on GRN generation!");
}

function checkForVendorTdsSetup(tableName) {
    var parentTable = $("#" + tableName).parents().closest('table').attr('id');
    var whatYouWantToDoVal = $("#whatYouWantToDo").find('option:selected').val();
    var status = true;
    var itemsData = [];
    $("#" + tableName + " > tbody > tr").each(function () {
        var modeOfTDSCompute = $(this).attr("modeOfTDSCompute");
        var specficIsTDSSpec = $(this).attr("specficIsTDSSpec");
        if (modeOfTDSCompute == "" && specficIsTDSSpec == "true") {
            status = false;
            var val = $(this).find("td .txnItems option:selected").val();
            itemsData.push(val);
        }
    });
    return status;
}

function txnBuyItemChangesForCompositionScheme(elem, transactionPurposeId) {
    var parentOfparentTr = $(elem).parents().closest('.transactionDetailsTable').attr('id');
    var vendorcustomerType = $("#" + parentOfparentTr + " .masterList").attr('vendorbusinesstype');
    if (typeof vendorcustomerType != 'undefined' && vendorcustomerType == 3) {
        var transactionPurposeId = $("#whatYouWantToDo").find('option:selected').val();
        if (transactionPurposeId != "" && (transactionPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transactionPurposeId == BUY_ON_CREDIT_PAY_LATER || transactionPurposeId == BUY_ON_PETTY_CASH_ACCOUNT)) {
            $(elem).closest("tr").find('.txnGstTaxRate').prop('disabled', true);
            $(elem).closest("tr").find('.txnCessRate').prop('disabled', true);
        }
    } else {
        $(elem).closest("tr").find('.txnGstTaxRate').prop('disabled', false);
        $(elem).closest("tr").find('.txnCessRate').prop('disabled', false);
    }
}

function calculateNetAmountOnTdsChange(elem) {

    var parentTr = $(elem).closest('tr').attr('id');
    var parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
    var gross = $("#" + parentTr + " input[class='txnGross']").val();
    var netAmount = 0;
    if (gross != "") {
        netAmount = parseFloat(gross);
    }
    var whTax = $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val();
    if ((gross != "" || gross != "0") && (whTax != "" || whTax != "0")) {
        if (parseFloat(whTax) > parseFloat(gross)) {
            $("#" + parentTr + " input[class='withholdingtaxcomponenetdiv']").val("");
            swal("Invalid TDS Amount!", "TDS Amount must be less than Gross Amount", "error");
            whTax = 0;
        }
    }
    var txnTypeOfSupply = $("#" + parentOfparentTr + " select[class='txnTypeOfSupply']").val();
    if(typeof txnTypeOfSupply != "undefined" && txnTypeOfSupply == "1") {
    	$("#" + parentTr).find(".txnTaxAmount").each(function () {
	        var itemTaxAmount = $(this).val();
	        if (itemTaxAmount != "") {
	            netAmount = (parseFloat(netAmount) + parseFloat(itemTaxAmount));
	        }
   		 });
    }


    if (typeof whTax != "undefined" && whTax != "") {
        netAmount = (parseFloat(netAmount) - parseFloat(whTax));
    }
    var adjustmentAmount = $("#" + parentTr + " input[class='howMuchAdvance']").val();
    if (typeof adjustmentAmount != "undefined" && gross != "" && adjustmentAmount != "") {
        netAmount = netAmount - parseFloat(adjustmentAmount);
    }

    $("#" + parentTr + " input[class='netAmountVal']").val((netAmount * 1).toFixed(2));
    let netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
    $("#" + parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));

}

function popupBuyTxnInvoiceModal(elem) {
    //console.log('Button clicked!!');
    var buttonId = $(elem).attr('id');
    //console.log('id is '+buttonId);
    $("#reportPopUp").attr('data-toggle', 'modal');
    $("#reportPopUp").modal('show');
    $(".reportPopUp").attr("href", location.hash);
    $("#reportPopUp div[class='modal-body']").html("");
    $("#reportPopUp div[class='modal-body']").append('<select id="selfInvoiceOrGRN"><option value="0">--Please Select--</option><option value="1">Self Invoice</option><option value="2">GRN</option></select>');
    $("#reportPopUp div[class='modal-body']").append('<input type="submit" style="float:right;" id="PDF' + buttonId + '" data-dismiss="modal" onclick="generateBuyTxnInvoice(this);" value="PDF" class="btn btn-primary"><input type="submit" style="float:right;" id="XLSX' + buttonId + '" data-dismiss="modal" onclick="generateBuyTxnInvoice(this);" value="XLSX" class="btn btn-primary">');
}

function generateBuyTxnInvoice(elem) {
    var txnId;
    var exportType;
    var option;
    var useremail = $("#hiddenuseremail").text();
    option = $(elem).siblings('select').val();
    var format = $(elem).attr('id');
    if (option == '0') {
        $('input[type=submit]').removeAttr('data-dismiss');
        swal('Invalid!','Please enter valid option','error');
    } else {
        if (format.startsWith('P')) {
            exportType = format.substring(0, 3);
            txnId = format.substring(3);
        } else {
            exportType = format.substring(0, 4);
            txnId = format.substring(4);
        }
        var jsonData = {
            email: useremail,
            exportType: exportType,
            entityTxnId: txnId,
            optionValue: option
        };
        var url = "/exportInvoicePdf";
        downloadFile(url, "POST", jsonData, "Error on GRN generation!");
    }
}

function populatePODropdownInBuyOnCreditForm() {
  ///transactions/purchaseOrderListByBranchIdAndProjectId/:branchId/:projectId
  var transactionTypeDropdown = $("#whatYouWantToDo").val();
  if (transactionTypeDropdown == BUY_ON_CREDIT_PAY_LATER) {
    var branchId = $("#bocpraTxnForBranches").val();
    var projectId = $("#bocpraTxnForProjects").val();
    if (projectId == "") {
      projectId = 0;
    }

    var url =
      "/transactions/purchaseOrderListByBranchIdAndProjectId/" +
      branchId +
      "/" +
      projectId;
    $.ajax({
      url: url,
      type: "text",
      headers: {
        "X-AUTH-TOKEN": window.authToken,
      },
      method: "GET",
      contentType: "application/json",
      success: function (data) {
        let dropdownValues = "<option>Select Purchase Order</option>";
        if (data.result === true) {
          data.items.forEach(function (element) {
            dropdownValues +=
              '<option value="' +
              element["id"] +
              '">' +
              element["purOrdRefNo"] +
              "</option>";
          });
        }
        $("#bocpraInvoices").html(dropdownValues);
      },
    });
  }
}
