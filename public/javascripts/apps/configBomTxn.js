var resetMainTxnTable = function (elem) {
    var elemValue = $(elem).val();
    if(elemValue == "") {
        var mainTable = $(elem).closest('tr').attr('id');
        $("#" + mainTable + " select[class='bomIsEdit']").find('option:first').prop("selected", "selected");
        $("#" + mainTable + " select[class='txnBomCls']").find('option:first').prop("selected", "selected");
        $("#" + mainTable + " span[class='select2-selection__clear']").trigger("mousedown");
        $("#" + mainTable + " .voiceRemarksClass").val('');
        $("#" + mainTable + " input[class='txnUploadSuppDocs']").val('');
    }
}
var resetMultiItemsTableData = function(multiItemTable){
    var txnitemSpecifics= $("#" + multiItemTable + "  tbody tr:last .txnItems").val();
    if((txnitemSpecifics != "" && txnitemSpecifics != null)){
        $("#" + multiItemTable +"  tbody tr:last span[class='select2-selection__clear']").trigger("mousedown");
    }
    $("#" + multiItemTable +"  tbody tr:last select[class='txnUnitOfMeasure']").find('option:first').prop("selected","selected");
    $("#" + multiItemTable +"  tbody tr:last input[class='txnNoOfbomUnit']").val("");

    $("#" + multiItemTable +"  tbody tr:last input[class='txnOme']").val("");
    $("#" + multiItemTable +"  tbody tr:last select[class='txnTypeOfMaterial']").find('option:first').prop("selected","selected");
    $("#" + multiItemTable +"  tbody tr:last div[class='klBranchSpecfTd']").text("");
    $("#" + multiItemTable +"  tbody tr:last input[class='txnInStockUnit']").val("");
}

var clearMultiItemsCurrentRowData = function(elem){
    var multipleItemsTableTr = $(elem).closest('tr').attr('id');
    //$("#" + multipleItemsTableTr +" span[class='select2-selection__clear']").trigger("mousedown");
    $("#" + multipleItemsTableTr +" select[class='txnUnitOfMeasure']").find('option:first').prop("selected","selected");
    $("#" + multipleItemsTableTr +" input[class='txnNoOfbomUnit']").val("");
    $("#" + multipleItemsTableTr +" input[class='txnOme']").val("");
    $("#" + multipleItemsTableTr +" select[class='txnTypeOfMaterial']").find('option:first').prop("selected","selected");
    $("#" + multipleItemsTableTr +" div[class='klBranchSpecfTd']").text("");
    $("#" + multipleItemsTableTr +" input[class='txnInStockUnit']").val('');
}

var getCoaItems4BomTxnList = function(elem){
    var parentDiv = $(elem).closest('div').attr('id');
    var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
    var multiitemTr = $("#" + parentDiv + " .staticsellmultipleitems table tbody tr:last").attr('id');

    $("#"+multiitemTr + " .txnItems").children().remove();
    $("#"+multiitemTr + " .txnItems").append("<option value=''>--Please Select--</option>");
    /* if(COA_INCOME_4USER_BRANCH_LIST != null && COA_INCOME_4USER_BRANCH_LIST != ""){
         $("#bomIncomeItemId").append(COA_INCOME_4USER_BRANCH_LIST);
     }
     if(COA_EXPENS_4USER_BRANCH_LIST != null && COA_EXPENS_4USER_BRANCH_LIST != ""){
         $("#billOfMatSetupTbl > tbody > tr:last > td > .txnItems").append(COA_EXPENS_4USER_BRANCH_LIST);
     }*/
    var selectedBranch= $(elem).val();
    if (selectedBranch === "") {
        swal("Incomplete data!", "Before proceeding, select a branch", "error");
        return false;
    }
    var  mainTableTr = "";
    var itemType = "0100";
    if (transactionPurposeId == CREATE_PURCHASE_ORDER) {
        mainTableTr = 'crtprrtrid';
        var poType = $("#purchaseOrderCategoryId").find('option:selected').val();
        if (poType == "req"){
            mainTableTr = "poareqtrid";
        }else if (poType == "bom") {
            mainTableTr = "poabomtrid";
            itemType = "1100";
        }
    }

    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.txnPurposeId=transactionPurposeId;
    jsonData.branchid = selectedBranch;
    jsonData.itemType = itemType;
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
            var tmpList = "";
            if(itemType == "1100") {
                for (var i = 0; i < data.incomeCOAData.length; i++) {
                    tmpList += ('<option value="' + data.incomeCOAData[i].id + '" category="' + data.incomeCOAData[i].category + '" id="' + data.incomeCOAData[i].iseditable + '" combsales="' + data.incomeCOAData[i].isCombinationSales + '" >' + data.incomeCOAData[i].name + '</option>');
                }
                $("#"+mainTableTr+ " .txnItems").append(tmpList);
                COA_INCOME_4USER_BRANCH_LIST = tmpList;
                tmpList = "";
            }
            for(var i=0;i<data.expenseCOAData.length;i++){
                tmpList += ('<option value="' + data.expenseCOAData[i].id + '" category="' + data.expenseCOAData[i].category + '" id="' + data.expenseCOAData[i].iseditable + '" combsales="' + data.expenseCOAData[i].isCombinationSales + '" >' + data.expenseCOAData[i].name + '</option>');
            }
            $("#"+multiitemTr + " .txnItems").append(tmpList);
            COA_EXPENS_4USER_BRANCH_LIST = tmpList;
            initMultiItemsSelect2();
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

var updateBomExpenseUnitBasedOnIncomeUnits = function(elem){
    var incomeUnits = $(elem).val();
    if(incomeUnits == "" || parseFloat(incomeUnits) < 0)
        return false;
    var parentDiv = $(elem).closest('div').attr('id');
    $("#"+parentDiv+" .staticsellmultipleitems table > tbody > tr").each(function() {
        var bomNoOfUnit = $(this).find("td input[class='txnNoOfbomUnitHidden']").val();
        if(bomNoOfUnit != "" && parseFloat(bomNoOfUnit) > 0){
            var tmpUnits = parseFloat(bomNoOfUnit) * parseFloat(incomeUnits);
            $(this).find("td input[class='txnNoOfbomUnit']").val(tmpUnits);
        }
    });
}

var updateBomExpenseUnit = function(elem){
    var parentDiv = $(elem).closest('div').parent().attr('id');
    var expenseUnits = $(elem).val();
    if(expenseUnits == "" || parseFloat(expenseUnits) < 0) {
        return false;
    }
    var incomeUnits = $("#"+parentDiv+" input[class='txnNoOfUnit']").val();
    if(incomeUnits == "" || parseFloat(incomeUnits) < 0)
        return false;
    var tmpUnits = parseFloat(expenseUnits) * parseFloat(incomeUnits);

    var itemTableTr=$(elem).closest('tr').attr('id');
    $("#"+itemTableTr + " input[class='txnNoOfbomUnitHidden']").val(expenseUnits);
    $(elem).val(tmpUnits);
}

var getPendingBomTxn4Branch = function(elem) {
    var mainTableTr=$(elem).closest('tr').attr('id');
    var txnPurposeId = $("#whatYouWantToDo").find('option:selected').val();
    $("#"+mainTableTr+ " .txnBomCls").children().remove();

    if(txnPurposeId == BILL_OF_MATERIAL) {
        var value = $(elem).val();
        if (value != "2") {
            $("#" + mainTableTr + " .txnBomCls").hide();
            $(".txnBomTdCls").hide();
            return false;
        }
        $("#"+mainTableTr+ " .txnBomCls").append('<option value="">---select BOM---</option>');
    }else{
        $("#"+mainTableTr+ " .txnBomCls").append('<option value="">---select Requisition---</option>');
    }
    var txnForBranch = $("#"+mainTableTr+" select[class='txnBranches'] option:selected").val();
    if(txnForBranch == ""){
        swal("Incomplete transaction detail!", "Please select a Branch for transaction.", "error");
        return false;
    }

    var txnFromDate = $("#"+mainTableTr+" input[name='txnFromDate']").val();
    var txnToDate = $("#"+mainTableTr+" input[name='txnToDate']").val();
    var txnPurposeId=$("#whatYouWantToDo").find('option:selected').val();

    var bomList = [];
    var j=1;

    // Ajax Call for Get BOM
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.txnForBranch = txnForBranch;
    jsonData.txnFromDate = txnFromDate;
    jsonData.txnToDate = txnToDate;
    jsonData.txnPurposeId = txnPurposeId;
    var url="/bom/notaccountedbomtxnlist";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
            for(var i=0; i<data.bomList.length; i++){
                bomList[j++] = '<option value="';
                bomList[j++] = data.bomList[i].id + '">';
                bomList[j++] = data.bomList[i].name;
                bomList[j++] = '</option>';
            }
            $("#"+mainTableTr+ " .txnBomCls").append(bomList.join(''));
            $("#"+mainTableTr+ " .txnBomCls").show();
            $(".txnBomTdCls").show();
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){ doLogout();
            }else if(xhr.status == 500){
                swal("Error on data fetching!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var onBillOfMaterialTxnItemChange = function(elem){
    var parentTr = $(elem).closest('tr').attr('id');
    var txnitemSpecifics = $("#"+parentTr+" .txnItems option:selected").val();
    if(txnitemSpecifics =="" || typeof txnitemSpecifics == 'undefined') {
        return false;
    }
    var isvalid = validateSelectedItems(elem);
    if(!isvalid){
        return false;
    }
    getExpenseItemAvailableStock(elem);
    showTransactionBranchKnowledgeLiabrary(elem);
}

var getBillOfMaterialTxnDetail = function(elem) {
    var bomTxnId = $(elem).val();
    var mainTableTr=$(elem).closest('tr').attr('id');
    if(bomTxnId == "") {
        //$("#"+mainTableTr+" .txnItems").find('option:first').prop("selected","selected").trigger('change');
        //$("#"+mainTableTr+" .masterList").find('option:first').prop("selected","selected").trigger('change');

        return false;
    }

    var parentDiv = $(elem).closest('div').attr('id');
    var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
    var multiitemTr = $("#" + parentDiv + " .staticsellmultipleitems table tbody tr:last").attr('id');

    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        url: "/bom/getTxnDetail/"+bomTxnId+"/0/0/0",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"GET",
        success: function (data){
            if(data.status == 'success'){
                //$("#"+mainTableTr+" #bomTxnForBranches option[value='"+data.branchId+"']").prop("selected","selected").trigger('change');
                //$("#"+mainTableTr+" #bomTxnForProjects option[value='"+data.projectId+"']").prop("selected","selected");
                if(transactionPurposeId == BILL_OF_MATERIAL) {
                    $("#" + mainTableTr + " .txnItems option[value='" + data.incomeId + "']").prop("selected", "selected");
                }else{
                    $("#" + mainTableTr + " .txnItems option[value='" + data.incomeId + "']").prop("selected", "selected").trigger('change');
                }
                $("#"+mainTableTr+" .masterList option[value='"+data.customerId+"']").prop("selected","selected").trigger('change');
                $("#"+mainTableTr+" .voiceRemarksClass").val(data.remark);
                $("#"+mainTableTr+" input[class='txnUploadSuppDocs']").val(data.documents);
                if(transactionPurposeId == BILL_OF_MATERIAL) {
                    var multiItemTrKey = 'bomtxnitm';
                    populateBomTxnItemsRow(data.bomItemlist, multiItemTrKey, parentDiv);
                }else if(transactionPurposeId == CREATE_PURCHASE_REQUISITION){
                    var multiItemTrKey = 'prqaso';
                    populatePurchaseReqisitionAsoTxnItemsRow(data.bomItemlist, multiItemTrKey, parentDiv);
                }else if(transactionPurposeId == CREATE_PURCHASE_ORDER){
                    var multiItemTrKey = 'poareq';
                    populatePurchaseReqisitionAsoTxnItemsRow(data.bomItemlist, multiItemTrKey, parentDiv);
                }

            }else{
                $("#notificationMessage").html("Fetch of Bill of Material has been failed.");
            }
        },
        error: function (xhr, status, error){
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching Bill of Material transaction details!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var populateBomTxnItemsRow = function (bomItemlist, multiItemTrKey, parentDiv) {
    for(var i=0; i<bomItemlist.length; i++){
        if(i > 0){
            //$("#" +parentDiv+ " .addnewItemForTransaction").trigger('click');
            addItemsInBillOfMaterialTxnBasedOnBom();
        }
        var parentTr = multiItemTrKey+i;
        //$("#"+parentTr + " .txnItems").val(data.bomItemlist[i].expenseId);
        $("#"+parentTr + " .txnItems option[value='"+bomItemlist[i].expenseId+"']").prop("selected","selected").trigger('change');
        $("#"+parentTr + " .txnUnitOfMeasure option[value='"+bomItemlist[i].unitOfMeasure+"']").prop("selected","selected");
        $("#"+parentTr + " .txnNoOfbomUnit").val(bomItemlist[i].noOfUnit);
        $("#"+parentTr + " .txnNoOfbomUnitHidden").val(bomItemlist[i].noOfUnit);
        $("#"+parentTr + " .masterList option[value='"+bomItemlist[i].vendorid+"']").prop("selected","selected");
        $("#"+parentTr + " .txnOme").val(bomItemlist[i].oem);
        $("#"+parentTr + " .txnTypeOfMaterial option[value='"+bomItemlist[i].tom+"']").prop("selected","selected");
        if(bomItemlist[i].knowledge === "1") {
            $("#" + parentTr + " #klfollowedyes").prop("checked", true);
        }else if(bomItemlist[i].knowledge === "0"){
            $("#" + parentTr + " #klfollowedno").prop("checked", true);
        }
        custVendSelect2();
    }
}

var completeActionBomTxn = function(elem){
    disableTransactionButtons();
    var parentTr = $(elem).closest('tr').attr('id');
    var selectedAction="4";
    var transactionEntityId=parentTr.substring(26, parentTr.length);
    var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
        if($(this).val() != ""){
            return $(this).val();
        }
    }).get();
    var supportingDoc = supportingDocTmp.join(',');
    var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
    var paymentOption=$("#"+parentTr+" select[id='paymentDetails'] option:selected").val();
    var bankDetails=$("#"+parentTr+" textarea[id='bankDetails']").val();
    var transactionInvoiceDate=$("#"+parentTr+" input[name='vendorInvoiceDate']").val();
    var selectedAddApproverVal="";var paymentBank="";
    var txnJsonData={};
    txnJsonData.useremail=$("#hiddenuseremail").text();
    txnJsonData.selectedApproverAction=selectedAction;
    txnJsonData.transactionPrimId=transactionEntityId;
    txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
    txnJsonData.suppDoc=supportingDoc;
    txnJsonData.txnRmarks=remarks;
    txnJsonData.txnInvDate=transactionInvoiceDate;
    txnJsonData.paymentDetails=paymentOption;
    if(paymentOption=="2"){
        paymentBank=$("#availableBank option:selected").val();
        if(typeof paymentBank!='undefined'){
            txnJsonData.txnPaymentBank=paymentBank;
        }
    }
    txnJsonData.bankInf=bankDetails;
    var url="/bom/approverAction";
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
            swal("Insufficient balance in the petty cash account!", "Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount, "warning");
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){ doLogout();
            }else if(xhr.status == 500){
                swal("Error on Complete Accounting!", "Please retry, if persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}

function expenceItemSelectedBom(elem) {
    var parentTr=$(elem).closest('tr').attr('id');
    var item = $(elem).val();
    var parentTR = $(elem).closest('tr');
    if (item === "") {
        swal("Incomplete data!", "select expence item", "error");
        return false;
    }

    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.bomItemId = item;
    var url="/bom/bomDetailsByExpence";
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
            if(data.bomItems.length > 0) {
                parentTR.find(".txnUnitOfMeasure").val(data.bomItems[0].measureName);
                parentTR.find(".txnNoOfbomUnit").val(data.bomItems[0].noOfUnits);
                parentTR.find(".txnOme").val(data.bomItems[0].oem);
                parentTR.find(".txnTypeOfMaterial").val(data.bomItems[0].typeOfMaterial);
                var str = '<option value="'+data.bomItems[0].vendorId+'">'+data.bomItems[0].vendorName+'</option>';
                parentTR.find(".masterList").append(str);
                parentTR.find(".masterList").val(data.bomItems[0].vendorId);

                if(data.bomItems[0].klIsMandatory != ""){
                    var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
                    if(typeof followedkl=='undefined'){
                        $("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
                    }

                    if(data.bomItems[0].klIsMandatory == "1") {
                        $("#" + parentTr + " #klfollowedyes").prop("checked", true);
                    }else if(data.bomItems[0].klIsMandatory == "0"){
                        $("#" + parentTr + " #klfollowedno").prop("checked", true);
                    }
                }
            }
        },
        error: function (xhr, status, error) {
            if(xhr.status === 401){ doLogout();
            }else if(xhr.status === 500){
                swal("Error on fetching items!", "Please contact support team or retry.", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var getExpenseItemAvailableStock = function(elem) {
    var txnTableTr = $(elem).parent().parent('tr:first').attr('id');
    var parentDiv = $(elem).closest('div').parent().attr('id');
    var txnBranch = $("#"+parentDiv+" select[class='txnBranches'] option:selected").val();
    var multiitemTr = $(elem).closest('tr').attr('id');
    var txnitemSpecifics=$("#"+multiitemTr+" .txnItems").val();
    var jsonData = {};
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.expenseSpecificsId = txnitemSpecifics;
    jsonData.txnBranch = txnBranch;
    var url = "/specifics/buyInventoryStockAvailable";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
            $("#"+multiitemTr + " .txnInStockUnit").val('');
            if (data.result) {
                $("#"+multiitemTr + " .txnInStockUnit").val(parseFloat(data.expInventoryStockData[0].stockAvailable).toFixed(2)).trigger('onkeyup');
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }else if(xhttp.status == 500){
                swal("Error on fetching available inventory!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}


/*
var getExpenseItemAvailableStock = function(elem) {
    var multiitemTr = $(elem).closest('tr').attr('id');
    var txnitemSpecifics=$("#"+multiitemTr+" .txnItems").val();
    var jsonData = {};
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.expenseSpecificsId = txnitemSpecifics;
    var url = "/specifics/buyInventoryStockAvailable";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
            $("#"+multiitemTr + " .txnInStockUnit").val('');
            if (data.result) {
                $("#"+multiitemTr + " .txnInStockUnit").val(parseFloat(data.expInventoryStockData[0].stockAvailable).toFixed(2));
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }else if(xhttp.status == 500){
                swal("Error on fetching available inventory!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}*/

var addItemsInBillOdMaterialTxn = function(mainTableID, parentTr){
    var bomTr = [];
    var itemList = [];
    var i = 0, k = 0;
    var length = $("#multipleItemsBomTxnTbl > tbody > tr").length;
    var parentTr = 'bomtxnitm'+length;
    bomTr[i++] = '<tr id="'+parentTr+'">';
    bomTr[i++] = '<td><input type="hidden" class="bomItemTxnIdCls" value=""/><select class="txnItems" id="bomTxnExpItems" onChange="onBillOfMaterialTxnItemChange(this);"></select></td>';
    bomTr[i++] = '<td><select class="txnUnitOfMeasure" id="bomTxnUnitOfMeasure"></select></td>';
    bomTr[i++] = '<td><input class="txnNoOfbomUnit" type="text" id="bomTxnNoOfUnit" onkeypress="return onlyDotsAndNumbers(event);" /></td>';
    bomTr[i++] = '<td><select class="masterList" id="bomTxnVendor"><option value="">--Please Select--</option></select></td>';
    bomTr[i++] = '<td><input class="txnOme" type="text" id="bomTxnOme"/></td>';
    bomTr[i++] = '<td><select class="txnTypeOfMaterial" id="bomTxnTypeOfMaterial"><option value="">Please Select</option><option value="1">Perishable</option><option value="2">Non Perishable</option><option value="3">Combustible</option><option value="4">Easily Available</option><option value="5">Rare Commodity</option><option value="6">Long Delivery Period</option><option value="7">High Value</option><option value="8">Low Value</option><option value="9">Difficult To Transport</option><option value="10">Imported</option></select></td>';
    bomTr[i++] = '<td><div class="klBranchSpecfTd" style="height: 50px; overflow: auto;"></div></td>';
    bomTr[i++] = '<td><input class="txnInStockUnit" type="text" id="bomTxnInventoryStatus"/></td>';
    bomTr[i++] = '<td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';
    $("#multipleItemsBomTxnTbl > tbody").append(bomTr.join(''));
    $("#" + parentTr + " .txnUnitOfMeasure").children().remove();
    $("#" + parentTr + " .txnUnitOfMeasure").append('<option value="">Please Select</option>');
    $("#" + parentTr + " .txnUnitOfMeasure").append(ORGANIZATON_COA_UNITS);
    $("#" + parentTr + " .txnItems").children().remove();
    $("#" + parentTr + " .txnItems").append('<option value="">Please Select</option>');
    $("#" + parentTr + " .txnItems").append(COA_EXPENS_4USER_BRANCH_LIST);
    $("#" + parentTr + " .masterList").children().remove();
    $("#" + parentTr + " .masterList").append('<option value="">Please Select</option>');
    $("#" + parentTr + " .masterList").append(VENDOR_4USER_BRANCH_LIST);
    custVendSelect2();
    initMultiItemsSelect2();
}

var addItemsInBillOfMaterialTxnBasedOnBom = function(){
    var bomTr = [];
    var itemList = [];
    var i = 0, k = 0;
    var length = $("#multipleItemsBomTxnTbl > tbody > tr").length;
    var parentTr = 'bomtxnitm'+length;
    bomTr[i++] = '<tr id="'+parentTr+'">';
    bomTr[i++] = '<td><input type="hidden" class="bomItemTxnIdCls" value=""/><select class="txnItems" id="bomTxnExpItems" onChange="onBillOfMaterialTxnItemChange(this);"></select></td>';
    bomTr[i++] = '<td><select class="txnUnitOfMeasure" id="bomTxnUnitOfMeasure"></select></td>';
    bomTr[i++] = '<td><input class="txnNoOfbomUnit" type="text" id="bomTxnNoOfUnit" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="updateBomExpenseUnit(this);"/><input class="txnNoOfbomUnitHidden" type="hidden" id="bomTxnNoOfUnitHidden"></td>';
    bomTr[i++] = '<td><select class="masterList" id="bomTxnVendor"><option value="">--Please Select--</option></select></td>';
    bomTr[i++] = '<td><input class="txnOme" type="text" id="bomTxnOme"/></td>';
    bomTr[i++] = '<td><select class="txnTypeOfMaterial" id="bomTxnTypeOfMaterial"><option value="">Please Select</option><option value="1">Perishable</option><option value="2">Non Perishable</option><option value="3">Combustible</option><option value="4">Easily Available</option><option value="5">Rare Commodity</option><option value="6">Long Delivery Period</option><option value="7">High Value</option><option value="8">Low Value</option><option value="9">Difficult To Transport</option><option value="10">Imported</option></select></td>';
    bomTr[i++] = '<td><div class="klBranchSpecfTd" style="height: 50px; overflow: auto;"></div></td>';
    bomTr[i++] = '<td><input class="txnInStockUnit" type="text" id="bomTxnInventoryStatus"/></td>';
    bomTr[i++] = '<td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';
    $("#multipleItemsBomTxnTbl > tbody").append(bomTr.join(''));
    $("#" + parentTr + " .txnUnitOfMeasure").children().remove();
    $("#" + parentTr + " .txnUnitOfMeasure").append('<option value="">Please Select</option>');
    $("#" + parentTr + " .txnUnitOfMeasure").append(ORGANIZATON_COA_UNITS);
    $("#" + parentTr + " .txnItems").children().remove();
    $("#" + parentTr + " .txnItems").append('<option value="">Please Select</option>');
    $("#" + parentTr + " .txnItems").append(COA_EXPENS_4USER_BRANCH_LIST);
    $("#" + parentTr + " .masterList").children().remove();
    $("#" + parentTr + " .masterList").append('<option value="">Please Select</option>');
    $("#" + parentTr + " .masterList").append(VENDOR_4USER_BRANCH_LIST);
    custVendSelect2();
    initMultiItemsSelect2();
}

var readMulitItemTableBomTxnData = function(multiItemTbl){
    var multipleItemsData = [];
    $("#"+multiItemTbl+ " > tbody > tr").each(function() {
        var bomItemTxnId = $(this).find("td input[class='bomItemTxnIdCls']").val();
        var itemId = $(this).find("td .txnItems option:selected").val();
        var bomUnitOfMeasure = $(this).find("td select[class='txnUnitOfMeasure'] option:selected").val();
        var bomNoOfUnit = $(this).find("td input[class='txnNoOfbomUnit']").val();
        var bomVendor = $(this).find("td .masterList option:selected").val();
        var bomOem = $(this).find("td input[class='txnOme']").val();
        var bomTom = $(this).find("td select[class='txnTypeOfMaterial'] option:selected").val();
        var bomInStockUnit = $(this).find("td input[class='txnInStockUnit']").val();
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
        var jsonData = {};
        jsonData.bomItemTxnId = bomItemTxnId;
        jsonData.itemId = itemId;
        jsonData.bomUnitOfMeasure = bomUnitOfMeasure;
        jsonData.bomNoOfUnit = bomNoOfUnit;
        jsonData.bomVendor = bomVendor;
        jsonData.bomOem = bomOem;
        jsonData.bomTom = bomTom;
        jsonData.bomInStockUnit = bomInStockUnit;
        jsonData.bomKnowledgeFollowed = bomKnowledgeFollowed;
        multipleItemsData.push(JSON.stringify(jsonData));
    });
    return multipleItemsData;
}

function submitForApprovalBillOfMaterialTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr){
    var parentTr = "bomtxntrid";
    var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
    var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
    var txnForCustomer = $("#"+parentTr+" .masterList option:selected").val();
    var txnMasterItem = $("#"+parentTr+" .txnItems option:selected").val();
    var txnNoOfUnit =  $("#"+parentTr+" input[class='txnNoOfUnit']").val();
    var bomEdit = $("#"+parentTr+" select[class='bomIsEdit'] option:selected").val();
    var bomId = "";
    if(bomEdit == 2) {
        bomId = $("#"+parentTr+" select[class='txnbomSelect'] option:selected").val();
    }
    var txnForItem = readMulitItemTableBomTxnData('multipleItemsBomTxnTbl');
    var txnRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClass']").val();
    var txnPrivateRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClassPrivate']").val();
    var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
        if($(this).val() != ""){
            return $(this).val();
        }
    }).get();
    var supportingDoc = supportingDocTmp.join(',');
    if (txnForBranch === "") {
        swal("Incomplete data!", "select Branch First", "error");
        enableTransactionButtons();
        return false;
    }

    if (txnMasterItem === "") {
        swal("Incomplete data!", "select income item", "error");
        enableTransactionButtons();
        return false;
    }

    if(txnForCustomer == "") {
        swal("Incomplete data!", "select Customer for Proceed", "error");
        enableTransactionButtons();
        return false;
    }

    var txnJsonData={};
    txnJsonData.txnEntityID = bomId;
    txnJsonData.txnPurpose = whatYouWantToDo;
    txnJsonData.txnPurposeVal = whatYouWantToDoVal;
    txnJsonData.txnForBranch = txnForBranch;
    txnJsonData.txnForProject = txnForProject;
    txnJsonData.txnMasterItem = txnMasterItem;
    txnJsonData.txnNoOfUnit = txnNoOfUnit;
    txnJsonData.bomEdit = bomEdit;
    txnJsonData.bomId = bomId;
    txnJsonData.txnForItem = txnForItem;
    txnJsonData.txnForCustomer = txnForCustomer;
    txnJsonData.txnRemarks = txnRemarks;
    txnJsonData.txnPrivateRemarks = txnPrivateRemarks
    txnJsonData.supportingdoc = supportingDoc;
    txnJsonData.txnDocumentUploadRequired = "false";
    txnJsonData.useremail = $("#hiddenuseremail").text();
    var url="/transaction/submitForApproval";

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
            if(typeof data.message !=='undefined' && data.message != ""){
                swal("Error!", data.message, "error");
                enableTransactionButtons();
                return false;
            }
            cancel();
            viewTransactionData(data); // to render the updated transaction recored
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){ doLogout();
            }else if(xhr.status == 500){
                swal("Error on Submit For Approval!", "Please contact support team or retry.", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}

function createPODocDownload(elem){
	var elemId = $(elem).attr('id');
	var exportType=elemId.substring(0, 4);
	var transactionId=elemId.substring(4, elemId.length);
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.email = useremail;
	jsonData.exportType = exportType;
	jsonData.entityTxnId=transactionId;
	
	var url="/exportInvoicePdfForBom";
	downloadFile(url, "POST", jsonData, "Error on GRN generation!");
}

var popupBomTransactionItems = function(txnReferenceNo, data) {
    $("#staticMutipleTransactionItems .panel-title").text("Bill of Material Items for " + txnReferenceNo);
    $("#staticMutipleTransactionItems div[class='modal-body']").html('<div class="datascrolltable" style="height: 100%; overflow: auto;"><table id="multipleSellItemsBreakupTable" class="table table-hover table-striped excelFormTable transaction-create table-bordered" style="margin-top: 0px; width:100%;">' +
        '<thead class="tablehead1"><tr><th>ITEM</th><th>UNIT PRICE</th><th>NO. OF UNITS</th><th>VENDOR</th><th>MEASURE NAME</th><th>OEM</th><th>TOTAL PRICE</th><th>AVAILABLE UNITS</th><th>COMMITTED UNITS</th><th>ORDERED UNITS</th><th>NET UNITS</th><th>FULFILLED UNITS</th><th>TYPE OF MATERIAL</th><th>DESTINATION GSTIN</th><th>IS FULFILLED</th><tr></thead><tbody></tbody></table></div>');
    var tableTr = "";
    for (var i = 0; i < data.transactionItemdetailsData.length; i++) {
        tableTr += '<tr id=' + i + '><td>' + data.transactionItemdetailsData[i].itemName + '</td><td>' + data.transactionItemdetailsData[i].pricePerUnit + '</td><td>' + data.transactionItemdetailsData[i].noOfUnits + '</td><td>' + data.transactionItemdetailsData[i].vendor + '</td><td>' + data.transactionItemdetailsData[i].measureName + '</td><td>' + data.transactionItemdetailsData[i].oem + '</td><td>' + data.transactionItemdetailsData[i].totalPrice + '</td><td>' + data.transactionItemdetailsData[i].availableUnits + '</td><td>' + data.transactionItemdetailsData[i].committedUnits + '</td><td>' + data.transactionItemdetailsData[i].orderedUnits + '</td><td>' + data.transactionItemdetailsData[i].netUnits + '</td><td>' + data.transactionItemdetailsData[i].fulfilledUnits + '</td><td>' + data.transactionItemdetailsData[i].typeOfMaterial + '</td><td>' + data.transactionItemdetailsData[i].destinationGstin + '</td><td>' + data.transactionItemdetailsData[i].isFulfilled + '</td></tr>';
    }
    $("#staticMutipleTransactionItems div[class='modal-body'] table[id='multipleSellItemsBreakupTable'] tbody").append(tableTr);
}

var getSalesOrderOrBomTxnDetail = function(elem) {
    var parentTr = $(elem).parent().parent('tr:first').attr('id');
    var mainTxnTr = $(elem).closest('div').attr('id');
    resetMultiItemsTableLength(mainTxnTr);
    resetMultiItemsTableFieldsData(elem);
    var invoiceTxnEntityId = $(elem).val();
    if (invoiceTxnEntityId == "" || invoiceTxnEntityId == null || typeof invoiceTxnEntityId == 'undefined') {
        return false;
    }
    var incomeId =  $("#"+mainTxnTr+" select[class='salesExpenseTxns'] option:selected").attr('incomeitemid');
    var incomeUnits = $("#"+mainTxnTr+" select[class='salesExpenseTxns'] option:selected").attr('incomeunits');
    /*var isLastTrUsed = $("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").val();
    if (isLastTrUsed != "") {
        $("#" + mainTxnTr + " button[class='addnewItemForTransaction']").click();
    }*/
    $("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").val(incomeId);
    $("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").change();
    initMultiItemsSelect2();
    $("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").prop('disabled', true);
    $("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']").val(incomeUnits);
    //$("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']").trigger('keyup');
    $("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnitHid']").val(incomeUnits);
    $("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last input[class='originalNoOfUnitsHid']").val(incomeUnits);
    calculateDiscountSell($("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']"));
    calculateGross($("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']"));
    calculateNetAmountForSell($("#" + mainTxnTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']"));
}