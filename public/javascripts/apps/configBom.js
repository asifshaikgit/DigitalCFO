var VENDORS_LIST_4_BRANCH_MAP = {}; // stores vendors for a specific branch
var UNITS_NAMES_LIST_4_COA_MAP = {}; // holds units name

var initBomSetup = function(){
    resetMultiBomTableLength('billOfMatSetupTbl');
    let tempArr = Object.values(ALL_BRANCH_OF_ORG_MAP);
    let branchlist = tempArr.join('');
    $("#bomBranchId").children().remove();
    $("#bomBranchId").append('<option value="">Please Select</option>');
    $("#bomBranchId").append(branchlist);
    $('#bomSetup-form-container').show();

    $(".bomNoOfUnitCls").val('');
    $(".bomOemCls").val('');
    $(".bomTomCls option:selected").val('');
    getAllDifferentUnitsfromCoa("#billOfMatSetupTbl > tbody > tr > td > #bomUnitOfMeasureId");
    $("#saveUpdateBomSetupBtn").removeAttr("disabled");
}

var resetMultiBomTableLength = function(elem){
    var initLen = 1;
    var tableTrCount = $("#"+elem+" tbody tr").length;
    for (var i = tableTrCount; i > initLen ; i--) {
        $("#"+elem+" tr:eq("+i+")").remove();
    }
}

var getCoaItemsListForBom = function(elem){
    $("#bomIncomeItemId").children().remove();
    $("#bomIncomeItemId").append("<option value=''>--Please Select--</option>");

    $("#billOfMatSetupTbl > tbody > tr > td > .txnItems").children().remove();
    $("#billOfMatSetupTbl > tbody > tr > td > .txnItems").append("<option value=''>--Please Select--</option>");
   /* if(COA_INCOME_4USER_BRANCH_LIST != null && COA_INCOME_4USER_BRANCH_LIST != ""){
        $("#bomIncomeItemId").append(COA_INCOME_4USER_BRANCH_LIST);
    }
    if(COA_EXPENS_4USER_BRANCH_LIST != null && COA_EXPENS_4USER_BRANCH_LIST != ""){
        $("#billOfMatSetupTbl > tbody > tr:last > td > .txnItems").append(COA_EXPENS_4USER_BRANCH_LIST);
    }*/
    var selectedBranch=$("#bomBranchId option:selected").val();
    if (selectedBranch === "") {
        swal("Incomplete data!", "Before proceeding, select a branch", "error");
        return false;
    }

    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.txnPurposeId="-1";
    jsonData.branchid = selectedBranch;
    jsonData.itemType = "1100";
    var url="/txn/coaUserBranchList";
    $.ajax({
        url: url,
        data:JSON.stringify(jsonData),
        type:"text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method:"POST",
        contentType:'application/json',
        success: function (data) {
            var coaIncomeList = "";
            for(var i=0;i<data.incomeCOAData.length;i++){
                coaIncomeList += ('<option value="' + data.incomeCOAData[i].id + '" category="' + data.incomeCOAData[i].category + '" id="' + data.incomeCOAData[i].iseditable + '" combsales="' + data.incomeCOAData[i].isCombinationSales + '" >' + data.incomeCOAData[i].name + '</option>');
            }
            $("#bomIncomeItemId").append(coaIncomeList);
            COA_INCOME_4USER_BRANCH_LIST = coaIncomeList;
            coaIncomeList = "";
            for(var i=0;i<data.expenseCOAData.length;i++){
                coaIncomeList += ('<option value="' + data.expenseCOAData[i].id + '" category="' + data.expenseCOAData[i].category + '" id="' + data.expenseCOAData[i].iseditable + '" combsales="' + data.expenseCOAData[i].isCombinationSales + '" >' + data.expenseCOAData[i].name + '</option>');
            }
            $("#billOfMatSetupTbl > tbody > tr > td > .txnItems").append(coaIncomeList);
            COA_EXPENS_4USER_BRANCH_LIST = coaIncomeList;
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
                doLogout();
            }else if(xhttp.status == 500){
                swal("Error on fetching items!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var getVendorsListForBranch = function(elem){
    var selectedBranch=$(elem).val();
    if (selectedBranch === "") {
        swal("Incomplete data!", "Before proceeding, select a branch", "error");
        return false;
    }
    var parentTr = $(elem).closest('tr').attr('id');
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.txnPurposeId=BUY_ON_CASH_PAY_RIGHT_AWAY;
    jsonData.txnPurposeBnchId=selectedBranch;

    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var url="/customer/getCustomerListForBranch";
    $.ajax({
        url: url,
        data:JSON.stringify(jsonData),
        type:"text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method:"POST",
        contentType:'application/json',
        success: function (data) {
            $("#billOfMatSetupTbl > tbody > tr > td > #bomVendorId").children().remove();
            $("#billOfMatSetupTbl > tbody > tr > td > #bomVendorId").append('<option value="">Please Select</option>');
            var customerVendorListTemp = "";
            for(var i=0;i<data.custListForBranch.length;i++){
                customerVendorListTemp += ('<option value="'+data.custListForBranch[i].customerId+'">'+data.custListForBranch[i].customerName+'</option>');
                VENDORS_LIST_4_BRANCH_MAP[data.custListForBranch[i].customerId] = ('<option value="'+data.custListForBranch[i].customerId+'">'+data.custListForBranch[i].customerName+'</option>');
            }
            $("#" + parentTr +" .masterList").append(customerVendorListTemp);
            $("#billOfMatSetupTbl > tbody > tr:last > td > #bomVendorId").append(customerVendorListTemp);
            custVendSelect2();
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching vendors!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var ORGANIZATON_COA_UNITS;
var getAllDifferentUnitsfromCoa = function(elemObj){
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var url="/data/getcoaunits";
    $.ajax({
        url: url,
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"get",
        success: function (data) {
            $(elemObj).children().remove();
            $(elemObj).append('<option value="">Please Select</option>');
            var tempList = "";
            for(var i=0;i<data.coaUnitsList.length;i++){
                tempList += ('<option value="'+data.coaUnitsList[i].unitName+'">'+data.coaUnitsList[i].unitName+'</option>');
                UNITS_NAMES_LIST_4_COA_MAP[data.coaUnitsList[i].unitName] = ('<option value="'+data.coaUnitsList[i].unitName+'">'+data.coaUnitsList[i].unitName+'</option>');
            }
            $(elemObj).append(tempList);
            ORGANIZATON_COA_UNITS = tempList;
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching measure of units!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var getKnowledgeLiabrary = function(elem) {
    var parentTr=$(elem).closest('tr').attr('id');
    var txnitemSpecifics=$("#"+parentTr+" .txnItems option:selected").val();
    if(txnitemSpecifics =="" || typeof txnitemSpecifics == 'undefined') {
        //swal("Incomplete data!", "Before proceeding, select a expense item", "error");
        return false;
    }
    var isvalid = validateSelectedItems(elem);
    if(!isvalid){
        return false;
    }

    var selectedBranch=$("#bomBranchId option:selected").val();
    if (selectedBranch === "") {
        swal("Incomplete data!", "Before proceeding, select a branch", "error");
        return false;
    }
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.userTxnPurposeText="Buy on cash & pay right away";
    jsonData.txnBranchId=selectedBranch;
    jsonData.txnSpecificsId=txnitemSpecifics;
    jsonData.txnPurposeValue = BUY_ON_CASH_PAY_RIGHT_AWAY;
    var url="/transaction/getTxnBranchSpecificsKL";
    $.ajax({
        url: url,
        data:JSON.stringify(jsonData),
        type:"text",
        async: false,
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"POST",
        contentType:'application/json',
        success: function (data) {
            $("#"+parentTr+" div[class='klBranchSpecfTd']").text("");
            var itemUnitPrice="";
            if(data.txnBranchSpecificsKLData.length>0 && data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers!=""){
                itemUnitPrice = data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers;
            }
            for(var i=0;i<data.txnBranchSpecificsKLData.length;i++){
                var klcount=i+1;
                if(i==0){
                    if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
                        var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
                        if(typeof followedkl=='undefined'){
                            $("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
                        }
                        $("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'&nbsp;<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
                    }else if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
                        $("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'&nbsp;'+data.txnBranchSpecificsKLData[i].klContent+'.');
                    }
                }else{
                    if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
                        var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
                        if(typeof followedkl=='undefined'){
                            $("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
                        }
                        $("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'&nbsp;<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
                    }
                    if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
                        $("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'&nbsp;'+data.txnBranchSpecificsKLData[i].klContent+'.');
                    }
                }
                //$("#"+parentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);
            }
            /*
            $("#"+parentOfparentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);
            $("#"+parentOfparentTr+" div[class='actualbudgetDisplay']").append('Budget Allocated For This Month: '+data.txnBudgetData[0].monthBudgetForBranchTxnSpecific);
            $("#"+parentOfparentTr+" div[class='budgetDisplay']").append('Budget Available For This Month: '+data.txnBudgetData[0].monthBudgetAvailableForBranchTxnSpecific);
            $("#"+parentOfparentTr+" div[class='amountRangeLimitRule']").append('Transaction Creation Limit From: '+data.txnBudgetData[0].userTxnAmountFrom);
            $("#"+parentOfparentTr+" div[class='amountRangeLimitRule']").append(' To: '+data.txnBudgetData[0].userTxnAmountTo);

            $("#"+parentTr+" input[id='bocpraactualbudgetDis']").val(data.txnBudgetData[0].monthBudgetForBranchTxnSpecific);
            $("#"+parentTr+" input[id='bocprabudgetDisplay']").val(data.txnBudgetData[0].monthBudgetAvailableForBranchTxnSpecific);
            $("#"+parentTr+" class[id='amountRangeFromLimit']").val(data.txnBudgetData[0].userTxnAmountFrom);
            $("#"+parentTr+" class[id='amountRangeToLimit']").val(data.txnBudgetData[0].userTxnAmountTo); */
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching knowledge library!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}

$(document).ready(function () {
    $("#addBomRowBtn").click(function () {
        var currentTr = $("#billOfMatSetupTbl tbody tr:last").attr('id');
        var length = currentTr.substring(5, currentTr.length);
        length = parseInt(length) + 1;
        var newTr = "bomtr" + length;
        var multiItemsTableTr = '<tr id="'+newTr+'"><td><select class="txnItems" id="bomExpenseItemId" onChange="getKnowledgeLiabrary(this);"></select></td>';
        multiItemsTableTr += '<td><select class="bomUnitOfMeasureCls" id="bomUnitOfMeasureId"></select></td>';
        multiItemsTableTr += '<td><input type="text" class="bomNoOfUnitCls" id="bomNumberOfUnitId" onkeypress="return onlyDotsAndNumbers(event);"/></td>';
        multiItemsTableTr += '<td><select class="masterList" id="bomVendorId"></select></td><td><input type="text" class="bomOemCls" id="bomOemId"/></td>';
        multiItemsTableTr += '<td><select class="bomTomCls" id="bomTomId"><option value="">Please Select</option><option value="1">Perishable</option><option value="2">Non Perishable</option><option value="3">Combustible</option><option value="4">Easily Available</option><option value="5">Rare Commodity</option><option value="6">Long Delivery Period</option><option value="7">High Value</option><option value="8">Low Value</option><option value="9">Difficult To Transport</option><option value="10">Imported</option></select></td>';
        multiItemsTableTr += '<td><div class="klBranchSpecfTd" style="height: 50px; overflow: auto;"></div></td>';
        multiItemsTableTr += '<td><input class="bomRemoveCheckBox" type="checkbox"/><input type="hidden" class="bomDetailCls"/></td></tr>';
        $("#billOfMatSetupTbl tbody").append(multiItemsTableTr);
        $("#" + newTr + " .txnItems").children().remove();
        $("#" + newTr + " .txnItems").append("<option value=''>Please Select</option>");
        if(COA_EXPENS_4USER_BRANCH_LIST != null && COA_EXPENS_4USER_BRANCH_LIST != ""){
            $("#" + newTr + " .txnItems").append(COA_EXPENS_4USER_BRANCH_LIST);
            initMultiItemsSelect2();
        }
        var tempArr = Object.values(VENDORS_LIST_4_BRANCH_MAP);
        var customerVendorListTemp = tempArr.join('');
        $("#" + newTr + " #bomVendorId").children().remove();
        $("#" + newTr + " #bomVendorId").append("<option value=''>Please Select</option>");
        $("#" + newTr + " #bomVendorId").append(customerVendorListTemp);

        $("#" + newTr + " #bomUnitOfMeasureId").children().remove();
        $("#" + newTr + " #bomUnitOfMeasureId").append("<option value=''>Please Select</option>");
        tempArr = Object.values(UNITS_NAMES_LIST_4_COA_MAP);
        $("#" + newTr + " #bomUnitOfMeasureId").append(tempArr.join(''));
        custVendSelect2();
    });

    $("#removeBomRowBtn").click(function(){
        var trLen = $("#billOfMatSetupTbl tbody tr").length;
        var selectedRows = $("#billOfMatSetupTbl").find(".bomRemoveCheckBox:checkbox:checked").length;
        if(selectedRows == 0){
            swal("Select a row!", "Please choose the items from List", "error");
            return false;
        }
        if(parseInt(selectedRows) > 0 && (parseInt(trLen) > 1 && parseInt(selectedRows) < parseInt(trLen))) {
            $("#billOfMatSetupTbl").find(".bomRemoveCheckBox:checkbox:checked").each(function(){
                var selectedTr = $(this).closest('tr').attr('id');
                $("#"+selectedTr).remove();
            });
        }else {
            swal("Configuration error!", "At least one item is needed to proceed.", "error");
            return false;
        }
    });
});

var saveUpdateBillOfMaterial = function(){
    var branchId=$("#bomBranchId option:selected").val();
    if (branchId === "") {
        swal("Incomplete data!", "Before proceeding, select a branch", "error");
        return false;
    }
    var billOFMaterialId = $("#billOFMaterialId").val();
    var projectId=$("#bomProjectId option:selected").val();
    var incomeItemId=$("#bomIncomeItemId option:selected").val();
    var bomDataArray = readMulitItemTableDataToArray("billOfMatSetupTbl");
    $("#saveUpdateBomSetupBtn").attr("disabled", "disabled");
    var jsonData = {};
    jsonData.billOFMaterialId = billOFMaterialId;
    jsonData.branchId = branchId;
    jsonData.projectId = projectId;
    jsonData.incomeItemId = incomeItemId;
    jsonData.dataArray = bomDataArray;
    $.ajax({
        url: "/bom/saveupdate",
        data:JSON.stringify(jsonData),
        type:"text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"POST",
        contentType:'application/json',
        success: function (data){
            if(data.status == 'added') {
                $("#notificationMessage").html("Bill of Material has been added successfully.");
                $('.notify-success').show();
                $("#createBomClose").trigger('click');
                $(".btn-div-top").hide();
                appendNewBillOfMaterial(data.bomlist);
            }else  if(data.status == 'updated') {
                $("#notificationMessage").html("Bill of Material has been Updated successfully.");
                $('.notify-success').show();
                $("#createBomClose").trigger('click');
                $(".btn-div-top").hide();
            }else{
                $("#notificationMessage").html("Bill of Material has been added/Updated failed.");
                $('.notify-success').show();
            }
        },
        error: function (xhr, status, error){
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on save/update Bill of Material!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
            $("#saveUpdateBomSetupBtn").removeAttr("disabled");
        }
    });
}

var readMulitItemTableDataToArray = function(tableId){
    var multipleItemsData = [];
    $("#" + tableId + " > tbody > tr").each(function() {
        var itemId = $(this).find("td .txnItems option:selected").val();
        var bomUnitOfMeasure = $(this).find("td select[class='bomUnitOfMeasureCls'] option:selected").val();
        var bomNoOfUnit = $(this).find("td input[class='bomNoOfUnitCls']").val();
        var bomVendor = $(this).find("td .masterList option:selected").val();
        var bomOem = $(this).find("td input[class='bomOemCls']").val();
        var bomTom = $(this).find("td select[class='bomTomCls'] option:selected").val();
        var bomDetailId = $(this).find("td input[class='bomDetailCls']").val();
        var jsonData = {};
        jsonData.bomItem = itemId;
        jsonData.bomUnitOfMeasure = bomUnitOfMeasure;
        jsonData.bomNoOfUnit = bomNoOfUnit;
        jsonData.bomVendor = bomVendor;
        jsonData.bomOem = bomOem;
        jsonData.bomTom = bomTom;
        jsonData.bomDetailId = bomDetailId;
        var followedkl = $(this).find("td div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
        var bomKnowledgeFollowed = "0";
        if(typeof followedkl!='undefined'){
            if($(this).find("td div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
                bomKnowledgeFollowed="1";
            }
            if($(this).find("td div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
                bomKnowledgeFollowed="0";
            }
        }
        jsonData.bomKnowledgeFollowed = bomKnowledgeFollowed;
        multipleItemsData.push(JSON.stringify(jsonData));
    });
    return multipleItemsData;
}

var appendNewBillOfMaterial = function(bomlist){
    var rowData = '<tr id="'+bomlist["entityId"]+'"><td>';
    rowData += bomlist.branch;
    rowData += '</td><td>';
    rowData += bomlist.income;
    rowData += '</td><td>';
    rowData += bomlist.project;
    rowData += '</td><td><button style="margin-top: 0px; height: 25px; padding: 3px 20px;" class="btn btn-submit" onclick="editBillOfMaterial(\''+bomlist.entityId+'\');"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td></tr>';
    $("#bomSetupTbl > tbody").prepend(rowData);
}

var getBillofMaterial = function() {
    $.ajax({
        url: "/bom/getbyorg",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"GET",
        success: function (data){
            $("#bomSetupTbl > tbody").html('');
            if(data.status == 'success'){
                for(var i=0; i<data.bomlist.length; i++){
                    var rowData = '<tr id="' + data.bomlist[i].entityId + '"><td>';
                    rowData += data.bomlist[i].branch;
                    rowData += '</td><td>';
                    rowData += data.bomlist[i].income;
                    rowData += '</td><td class="snglUsrDply">';
                    rowData += data.bomlist[i].project;
                    rowData += '</td><td><button style="margin-top: 0px; height: 25px; padding: 3px 20px;" class="btn btn-submit" onclick="editBillOfMaterial(\''+data.bomlist[i].entityId+'\');"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td></tr>';
                    $("#bomSetupTbl > tbody").append(rowData);
                }
                setPagingDetail('bomSetupTbl', 20, 'pagingBomSetupNavPosition');
            }else{
                $("#notificationMessage").html("Fetch of Bill of Material has been failed.");
            }
        },
        error: function (xhr, status, error){
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetch Bill of Material!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var editBillOfMaterial = function (entityID) {
    initBomSetup();
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        url: "/bom/getdetail/"+entityID,
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"GET",
        success: function (data){
            if(data.status == 'success'){
                $("#bomIncomeItemDiv #bomBranchId option[value='"+data.branchid+"']").prop("selected","selected").trigger('change');
                $("#bomIncomeItemDiv #bomProjectId option[value='"+data.projectid+"']").prop("selected","selected");
                $("#bomIncomeItemDiv .txnItems option[value='"+data.incomeid+"']").prop("selected","selected");
                $("#billOFMaterialId").val(data.bomid);
                for(var i=0; i<data.bomlist.length; i++){
                    if(i > 0){
                        $("#addBomRowBtn").trigger('click');
                    }
                    var parentTr = 'bomtr'+i;
                    $("#"+parentTr + " .bomDetailCls").val(data.bomlist[i].entityId);
                    $("#"+parentTr + " .txnItems option[value='"+data.bomlist[i].expenseId+"']").prop("selected","selected").trigger('change');
                    //initMultiItemsSelect2();
                    $("#"+parentTr + " .bomUnitOfMeasureCls option[value='"+data.bomlist[i].unitOfMeasure+"']").prop("selected","selected");
                    $("#"+parentTr + " .bomNoOfUnitCls").val(data.bomlist[i].noOfUnit);
                    $("#"+parentTr + " .masterList option[value='"+data.bomlist[i].vendorid+"']").prop("selected","selected");
                    $("#"+parentTr + " .bomOemCls").val(data.bomlist[i].oem);
                    $("#"+parentTr + " .bomTomCls option[value='"+data.bomlist[i].tom+"']").prop("selected","selected");
                    if(data.bomlist[i].knowledge === "1") {
                        $("#" + parentTr + " #klfollowedyes").prop("checked", true);
                    }else if(data.bomlist[i].knowledge === "0"){
                        $("#" + parentTr + " #klfollowedno").prop("checked", true);
                    }
                }
                custVendSelect2();
            }else{
                $("#notificationMessage").html("Fetch of Bill of Material has been failed.");
            }
        },
        error: function (xhr, status, error){
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching Bill of Material details!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

// ****************** Below used in BOM Transaction **********************

var getBomIncomeItemsByBranch = function(elem) {
    var mainTableTr=$(elem).closest('tr').attr('id');
    var txnForBranch = $("#"+mainTableTr+" select[class='txnBranches'] option:selected").val();
    $("#"+mainTableTr + " .txnItems").children().remove();
    $("#"+mainTableTr + " .txnItems").append("<option value=''>--Please Select--</option>");
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.branchId = txnForBranch;
    var url="/bom/incomeitemsbybranch";
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
            if(data.incomeItems.length > 0) {
                var tmpList = "";
                for(var i=0;i<data.incomeItems.length;i++){
                    tmpList += ('<option bomid="' + data.incomeItems[i].bomid + '" value="' + data.incomeItems[i].id + '" >' + data.incomeItems[i].name + '</option>');
                }
                $("#"+mainTableTr+ " .txnItems").append(tmpList);
            }
        },
        error: function (xhr, status, error) {
            if(xhr.status === 401){
                doLogout();
            }else if(xhr.status === 500){
                swal("Error on fetching items!", "Please contact support team or retry.", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var getBomDetailForIncomeItem = function(elem){
    var mainTableTr=$(elem).closest('tr').attr('id');
    var parentDiv = $("#"+mainTableTr).closest('div').attr('id');
    var isCreate = $("#"+mainTableTr+" .bomIsEdit").val();
    if(isCreate =! "1"){
        return false;
    }
    var branchId = $(elem).closest('tr').find('.txnBranches').val();
    if (branchId === "") {
        swal("Incomplete data!", "select Branch First", "error");
        return false;
    }
    var masterItem = $(elem).closest('tr').find('.txnItems').val();
    if (masterItem === ""){
        swal("Incomplete data!", "select Master/Income item", "error");
        return false;
    }
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var url="bom/getbyincome/"+branchId+"/"+masterItem;
    $.ajax({
        url: url,
        type:"text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        async: false,
        method:"GET",
        contentType:'application/json',
        success: function (data) {
            if(data.status == 'success'){
                $("#"+mainTableTr+" .txnItems option[value='"+data.incomeId+"']").prop("selected","selected").trigger('change');
                $("#"+mainTableTr+" .masterList option[value='"+data.customerId+"']").prop("selected","selected");
                $("#"+mainTableTr+" .voiceRemarksClass").val(data.remark);
                $("#"+mainTableTr+" input[class='txnUploadSuppDocs']").val(data.documents);
                $("#"+mainTableTr+" input[class='txnNoOfUnit']").val("");
                for(var i=0; i<data.bomItemlist.length; i++){
                    if(i > 0){
                        //$("#" +parentDiv+ " .addnewItemForTransaction").trigger('click');
                        addItemsInBillOfMaterialTxnBasedOnBom();
                    }
                    var parentTr = 'bomtxnitm'+i;
                    //$("#"+parentTr + " .txnItems").val(data.bomItemlist[i].expenseId);
                    $("#"+parentTr + " .txnItems option[value='"+data.bomItemlist[i].expenseId+"']").prop("selected","selected");
                    $("#"+parentTr + " .txnUnitOfMeasure option[value='"+data.bomItemlist[i].unitOfMeasure+"']").prop("selected","selected");
                    $("#"+parentTr + " .txnNoOfbomUnit").val(data.bomItemlist[i].noOfUnit);
                    $("#"+parentTr + " .txnNoOfbomUnitHidden").val(data.bomItemlist[i].noOfUnit);
                    $("#"+parentTr + " .masterList option[value='"+data.bomItemlist[i].vendorid+"']").prop("selected","selected");
                    $("#"+parentTr  + " .txnOme").val(data.bomItemlist[i].oem);
                   $("#"+parentTr + " .txnTypeOfMaterial option[value='"+data.bomItemlist[i].tom+"']").prop("selected","selected");
                    if(data.bomItemlist[i].knowledge === "1") {
                        $("#" + parentTr + " #klfollowedyes").prop("checked", true);
                    }else if(data.bomItemlist[i].knowledge === "0"){
                        $("#" + parentTr + " #klfollowedno").prop("checked", true);
                    }
                    $("#"+parentTr  + " .txnInStockUnit").val(data.bomItemlist[i].availableStock);
                    custVendSelect2();
                    initMultiItemsSelect2();
                }
            }else{
                $("#notificationMessage").html("Fetch of Bill of Material detail has been failed.");
            }
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching Bill of Material for Master item!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}
// ****************** BOM Transaction Changes End**********************
