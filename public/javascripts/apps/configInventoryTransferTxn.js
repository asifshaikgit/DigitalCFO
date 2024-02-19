$(function () {
    $("#dateofsupply").datetimepicker({
        format: 'DD-MMM-YYYY',
        useCurrent: true
        //minDate: todayDate
    });

    $("#popupInvTransferAddDetailBtn").on('click', function () {
        $("#popupinvTransferAddDetailBtn").data('toggle', 'modal');
        $("#staticSellTransInvoice").modal('show');
    });

    $("#removeItemInInventoryTransfer").on('click', function () {
        var parentId = $(this).closest('table').attr('id');

        if (validateRemovalTr(parentId)) {
            $("#" + parentId).find(".removeCheckBox:checkbox:checked").each(function () {
                $(this).closest('tr').remove();
            });
            calculateGrandTotal(parentId);
        }
    });
});

var COA_INCOME_4USER_BRANCH_LIST;
var COA_EXPENS_4USER_BRANCH_LIST;
function getCoaItemsList(elem){
    var parentTable = $(elem).closest('table').attr('id');
    var parentTr = $("#"+parentTable).closest('div').attr('id');
    $(".klBranchSpecfTd").text("");
    $(".itemParentNameDiv").text("");
    $(".inventoryItemInStock").text("");
    $(".customerVendorExistingAdvance").text("");
    $(".resultantAdvance").text("");

    var inwardOutward = $("#" + parentTr + " select[class='inwardOutwardSelect'] option:selected").val();
    if (inwardOutward === "") {
        swal("Incomplete transaction data!", "Before proceeding, please choose Inward/Outward", "error");
        return false;
    }

    var value=$("#whatYouWantToDo").find('option:selected').val();
    resetMultiItemsTableLength(parentTr);
    resetMultiItemsTableFieldsData(elem);
    $("#"+parentTr+" .multipleItemsTable .txnItems").children().remove();
    $("#"+parentTr+" .multipleItemsTable .txnItems").append("<option value=''>--Please Select--</option>");
    $("#"+parentTr+" div[class='netAmountDescriptionDisplay']").html("");
    $("#"+parentTr+" input[class='netAmountValTotal']").val("");


    var selectedBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
    if (selectedBranch === "") {
        swal("Incomplete transaction data!", "Before proceeding, select a from branch", "error");
        return false;
    }
    var txnBranchesTo=$("#"+parentTr+" select[class='txnBranchesTo'] option:selected").val();
    if (txnBranchesTo === "") {
        swal("Incomplete transaction data!", "Before proceeding, select a from branch", "error");
        return false;
    }
    validateFromToBranch(elem);
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.txnPurposeId=value;
    jsonData.branchid = selectedBranch;
    if(inwardOutward === "1") {
        jsonData.itemType = "1000";
    }else{
        jsonData.itemType = "0100";
    }
    var url="/txn/coaUserBranchList";
    $.ajax({
        url: url,
        data:JSON.stringify(jsonData),
        type:"text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"POST",
        contentType:'application/json',
        success: function (data) {
            var coaIncomeList = "";
            if(inwardOutward === "1"){
                for(var i=0;i<data.incomeCOAData.length;i++){
                    if(data.incomeCOAData[i].isinventory == 1) {
                        coaIncomeList += ('<option value="' + data.incomeCOAData[i].id + '" category="' + data.incomeCOAData[i].category + '" id="' + data.incomeCOAData[i].iseditable + '" combsales="' + data.incomeCOAData[i].isCombinationSales + '" >' + data.incomeCOAData[i].name + '</option>');
                    }
                }
                $("#multipleItemsTabletifbtb > tbody > tr > td > .txnItems").append(coaIncomeList);
                COA_INCOME_4USER_BRANCH_LIST = coaIncomeList;
            }else if(inwardOutward === "2"){
                for(var i=0;i<data.expenseCOAData.length;i++){
                    if(data.expenseCOAData[i].isinventory == 1) {
                        coaIncomeList += ('<option value="' + data.expenseCOAData[i].id + '" category="' + data.expenseCOAData[i].category + '" id="' + data.expenseCOAData[i].iseditable + '" combsales="' + data.expenseCOAData[i].isCombinationSales + '" >' + data.expenseCOAData[i].name + '</option>');
                    }
                }
                $("#multipleItemsTabletifbtb > tbody > tr > td > .txnItems").append(coaIncomeList);
                COA_EXPENS_4USER_BRANCH_LIST = coaIncomeList;
            }
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
                doLogout();
            }else if(xhttp.status == 500){
                swal("Error on fetching transaction items!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var addMoreTranferInventoryItems = function (parentTable, parentTr) {
    var transPurposeId = $("#whatYouWantToDo").find('option:selected').val();
    transPurposeId = parseInt(transPurposeId);
    var fromBranch = $("#" + parentTable + " select[class='txnBranches'] option:selected").val();
    var inwardOutward = $("#" + parentTable + " select[class='inwardOutwardSelect'] option:selected").val();
    if (inwardOutward === "") {
        swal("Incomplete transaction data!", "Before adding item, please choose Values from Inward/Outward", "error");
        return false;
    }
    if (fromBranch === "") {
        swal("Incomplete transaction data!", "Before adding item, please choose Branch from dropdown", "error");
        return false;
    }
    //var trLen =$("#" + parentTable).find(".multipleItemsTable > tbody > tr").length;
    var currentTr = $("#" + parentTable +" table[class='multipleItemsTable'] tbody tr:last").attr('id');
    var trLen = currentTr.substring(6, currentTr.length);
    trLen = parseInt(trLen) + 1;
    var row = [];
    var j = 0;
    row[j++] = '<tr id="tifbtb'+trLen+'"><td><select class="txnItems" id="tifbtbItem" onChange="populateSellTranData(this);"><option value="">Please Select</option>';
    row[j++] = '</select></td>';
    row[j++] = '<td><input type="text" class="txnPerUnitPrice" id="tifbtbPerUnitPrice" placeholder="Price Per Unit" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);"></td>';
    row[j++] = '<td><input class="txnNoOfUnit" type="text" id="tifbtbunits" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" onblur="checkStockAvailableForSellTran(this);" placeholder="Units to Transfer"/></td>';
    if(inwardOutward === "1") {
        row[j++] = '<td><input type="text" class="txnDiscountPercent" name="tifbtbDiscountPercent" id="tifbtbDiscountPercent" placeholder="Discount %" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" /><input type="hidden" class="txnDiscountPercentHid" id="tifbtbdiscounttocust"/></td>';
        row[j++] = '<td><input type="text" class="txnDiscountAmount" name="tifbtbDiscountAmt" id="tifbtbDiscountAmt" readonly="readonly" placeholder="Discount Amt"/></td>';
    }
    row[j++] = '<td><input class="txnGross" placeholder="Gross Amount" type="text" id="tifbtbGross" readonly="readonly"></td>';
    row[j++] = '<td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div>';
    row[j++] = '<input type="hidden" class="txnTaxTypes" id="tifbtbTaxtypes"/><input type="hidden" class="itemTaxAmount"/></td>';
    row[j++] = '<td style="display:none;"><input type="text" class="invoiceValue" placeholder="Invoice Value" name="tifbtbInvoiceValue" id="tifbtbInvoiceValue" readonly="readonly"/></td>';
    row[j++] = '<td><input type="text" class="netAmountVal" name="tifbtbnetamnt" id="tifbtbnetamnt" readonly="readonly" placeholder="Net Result"></td>';
    row[j++] = '<td><div class="netAmountDescriptionDisplay"></div></td><td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>';
    $("#" + parentTable).find(".multipleItemsTable tbody").append(row.join(""));
    if(inwardOutward === "1") {
        $("#" + parentTable).find(".multipleItemsTable > tbody > tr > td > .txnItems").append(COA_INCOME_4USER_BRANCH_LIST);
    }else{
        $("#" + parentTable).find(".multipleItemsTable > tbody > tr > td > .txnItems").append(COA_EXPENS_4USER_BRANCH_LIST);
        $("#" + parentTable +" table[class='multipleItemsTable'] tbody tr:last").find("input[class='removeTxnCheckBox']").hide();
    }
    bindSelect('txnItems');
}

var validateFromToBranch = function (elem) {
    var parentTr = $(elem).closest('tr');
    var fromBranch = parentTr.find(".txnBranches").val();
    var toBranch = parentTr.find(".txnBranchesTo").val();
    if(toBranch === "" || fromBranch === ""){
        return false;
    }
    if(fromBranch === toBranch){
        $(elem).val('')
        swal("Invalid from/to branch!", "from/to branches cannot be same.", "error");
        return false;
    }
    getklinvoices(elem);
}

var submitForApprovalInvTransferTxn = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
    var transferType = $("#"+parentTr+" select[class='inwardOutwardSelect'] option:selected").val();
    if(transferType === ""){
        swal("Incomplete transaction data!", "Please select "+whatYouWantToDo+" type", "error");
        enableTransactionButtons();
        return false;
    }
    var txnFromBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
    if(txnFromBranch === ""){
        swal("Incomplete transaction data!", "Please select from branch", "error");
        enableTransactionButtons();
        return false;
    }
    var txnToBranch = $("#"+parentTr+" select[class='txnBranchesTo'] option:selected").val();
    if(txnToBranch === ""){
        swal("Incomplete transaction data!", "Please select to branch", "error");
        enableTransactionButtons();
        return false;
    }
    var sourceGstin = $("#"+parentTr+" select[class='txnBranches']").children(":selected").attr("id");
    var destinGstin = $("#"+parentTr+" select[class='txnBranchesTo']").children(":selected").attr("id");
    var txnTotalNetAmount = $("#"+parentTr+" input[class='netAmountValTotal']").val();
    let parentOfparentTr = $("#"+parentTr).closest('div').attr('id');
    var isValid = validateMultiItemsTransaction("multipleItemsTabletifbtb");
    if(!isValid){
        swal("Incomplete transaction data!", "Please provide complete transaction details before submitting for approval", "error");
        enableTransactionButtons();
        return false;
    }
    var txnForItem = convertTableDataToArray("multipleItemsTabletifbtb");
    var totalTxnNetAmtWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
    var totalTxnTaxAmt = calcuateTotalAmt4Element(parentTr,'itemTaxAmount');
    var totalTxnGrossAmt = calcuateTotalAmt4Element(parentTr,'txnGross');
    var txnRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClass']").val();
    var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
        if($(this).val() != ""){
            return $(this).val();
        }
    }).get();
    var supportingDoc = supportingDocTmp.join(',');
    var netAmountTotalWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
    var txnTotalInvoiceValue = $("#"+parentTr+" input[class='totalInvoiceValue']").val();
    if((txnTotalNetAmount===""  || parseFloat(txnTotalNetAmount) === 0.0) && (txnTotalInvoiceValue === "" || parseFloat(txnTotalInvoiceValue) ===0.0)){
        swal("Error on Submit for approval!", "Please provide complete transaction details before submitting for approval", "error");
        enableTransactionButtons();
        return true;
    }
    var txnNetAmountDescription=$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").text();
    var txnSaleEntityID = $("#"+parentTr+" select[class='salesExpenseTxns']").find('option:selected').val();
    var txnEntityID = $("#"+parentTr).attr("name");
    var txnJsonData={};
    //setStaticSellTransInvoiceData(txnJsonData); //to set popup details
    txnJsonData.txnEntityID = txnEntityID;
    txnJsonData.txnSaleEntityID = txnSaleEntityID;
    txnJsonData.txnTransferType = transferType;
    txnJsonData.txnPurpose=whatYouWantToDo;
    txnJsonData.txnPurposeVal=whatYouWantToDoVal;
    txnJsonData.txnFromBranch=txnFromBranch;
    txnJsonData.txnToBranch=txnToBranch;
    txnJsonData.txnSourceGstin = sourceGstin;
    txnJsonData.txnDestinGstin = destinGstin;
    txnJsonData.txnForItem=txnForItem;
    txnJsonData.txnTotalNetAmount=txnTotalNetAmount;
    txnJsonData.totalTxnNetAmtWithDecimalValue = totalTxnNetAmtWithDecimalValue;
    txnJsonData.totalTxnTaxAmt = totalTxnTaxAmt;
    txnJsonData.totalTxnGrossAmt = totalTxnGrossAmt;
    txnJsonData.txnNetAmountDescription=txnNetAmountDescription;
    txnJsonData.txnRemarks=txnRemarks;
    txnJsonData.txnprocrem=procurementRequestRemarks;
    txnJsonData.supportingdoc=supportingDoc;
    txnJsonData.txnDocumentUploadRequired="false";
    txnJsonData.useremail=$("#hiddenuseremail").text();
    var url="/transaction/submitForApproval";
    setBuyTxnAdditionalDetail(txnJsonData);
    $.ajax({
        url: url,
        data:JSON.stringify(txnJsonData),
        type:"text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"POST",
        contentType:'application/json',
        success: function (data) {
            if(typeof data.message !=='undefined' && data.message != "" && data.message){
                swal("Error!", data.message, "error");
                swal(data.message, "Change price/quantity as per need and retry.", "warning");
                return false;
            }else{
            	approvealForSingleUser(data);	// Single User
                cancel();
                viewTransactionData(data); // to render the updated transaction recored
            }
        },
        error: function (xhr, status, error) {
            if(xhr.status === 401){ doLogout();
            }else if(xhr.status === 500){
                swal("Error on Submit For Approval!", "Please contact support team or retry.", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}
/***********************************************************************************************************/
var onInventoryTransferChange = function(elem){
    var text=$("#whatYouWantToDo").find('option:selected').text();
    var value=$("#whatYouWantToDo").find('option:selected').val();
    populateItemBasedOnWhatYouWantToDo(value, text);
    var parentTr = $(elem).closest('tr');
    var mainTableID = $(elem).closest('table').attr('id');
    var parentTrID = $(elem).closest('tr').attr('id');
    var transactionDiv = $("#"+ mainTableID).closest('div').attr('id');
    var transferType = $(elem).val();
    resetMultiItemsTableLength('transactionDetailsTIFBTBTable');
    resetMainTransTableFields('tifbtbtrid');
    resetMultiItemsTableFieldsData('#tifbtbtrid');
    if(transferType === '2') {
        $("#"+mainTableID+"> thead> tr> th:nth-child(3)").show();
        $("#"+mainTableID+"> tbody> tr> td:nth-child(3)").show();
        $("#"+mainTableID+"> thead> tr> th:nth-child(4)").show();
        $("#"+mainTableID+"> tbody> tr> td:nth-child(4)").show();
        parentTr.find("select[class='txnTypeOfSupply']").children().remove();
        parentTr.find("select[class='txnTypeOfSupply']").append('<option value="" selected>--Please Select--</option><option value="1">Regular</option><option value="2">Supply on Reverse Charge - Unregistered Vendor</option><option value="3">Supply attracting tax on reverse charge - registered vendor</option><option value="4">Overseas / SEZ Import Goods - Supply</option><option value="5">Overseas / SEZ Import Services - Supply</option>');

        $("#"+transactionDiv+" table[class='multipleItemsTable'] thead tr th:nth-child(4)").hide();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] thead tr th:nth-child(5)").hide();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] > tbody > tr > td:nth-child(4)").hide();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] > tbody > tr > td:nth-child(5)").hide();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] > tbody > tr > td:nth-last-child(1)").find("input[class='removeTxnCheckBox']").hide();
        $("#"+transactionDiv+" .addnewItemForTransaction").hide();
        $("#"+transactionDiv+" .removeItemInInventoryTransfer").hide();
        var childTableTr = $("#" + transactionDiv +" table[class='multipleItemsTable'] tbody tr:last");
        $("#"+mainTableID+" #popupBuyTxnAddDetailBtn").show();
        enableDisableTrElements(childTableTr, false);
    }else{
        $("#"+mainTableID+" #popupBuyTxnAddDetailBtn").hide();
        var childTableTr = $("#" + transactionDiv +" table[class='multipleItemsTable'] tbody tr:last");
        enableDisableTrElements(childTableTr, true);
        $("#"+mainTableID+"> thead> tr> th:nth-child(3)").hide();
        $("#"+mainTableID+"> tbody> tr> td:nth-child(3)").hide();
        $("#"+mainTableID+"> thead> tr> th:nth-child(4)").hide();
        $("#"+mainTableID+"> tbody> tr> td:nth-child(4)").hide();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] thead tr th:nth-child(4)").show();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] thead tr th:nth-child(5)").show();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] tbody tr td:nth-child(4)").show();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] tbody tr td:nth-child(5)").show();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] > tbody > tr > td:nth-last-child(1)").show();
        $("#"+transactionDiv+" table[class='multipleItemsTable'] > tbody > tr > td:nth-last-child(2)").find("div[class='netAmountDescriptionDisplay']").text('');
        $("#"+transactionDiv).find("input[class='netAmountValTotal']").val('');
        $("#"+transactionDiv+" .addnewItemForTransaction").show();
        $("#"+transactionDiv+" .removeItemInInventoryTransfer").show();
    }
}

var enableDisableTrElements = function(tableTr, isEnable){
    if(!isEnable) {
        $(tableTr).each(function () {
            $(this).find('td').each(function () {
                $(this).find('input,select,textarea').attr("disabled", "disabled");
            });
        });
    }else{
        $(tableTr).each(function () {
            $(this).find('td').each(function () {
                $(this).find('input,select,textarea').removeAttr("disabled");
            });
        });
    }
}

