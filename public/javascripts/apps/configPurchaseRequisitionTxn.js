//setting up the category selection for purchase requisition, normal or with BOM
var setPurchaseRequisitionTxnCategory = function () {
    $("#purchaseRequisitionCategoryId").children().remove();
    $("#purchaseRequisitionCategoryId").append('<option value="">Please Select</option>');
    $("#purchaseRequisitionCategoryId").append('<option value="npr">Normal Purchase Requisition</option>');
    $("#purchaseRequisitionCategoryId").append('<option value="prabom">Purchase Requisition Against Bill Of Material</option>');
    //<!--<option value="bom">Purchase order against Bill of material</option> -->
    $("#purchaseRequisitionCategoryDivId").show();
    $("#purchaseRequisitionCategoryDivId").find('option:first').prop("selected", "selected");
    //populating the bom dropdown when the Purchase Requisition type drodown selected
    populateBOMDrodown();
}

// var addItemsPurchaseRequisitionTxn = function(mainTableID, mainTableTrId) {
//     var parentDiv = $("#" + mainTableID).closest('div').attr('id');
//     var branchid = $("#" + mainTableTrId + " select[class='txnBranches'] option:selected").val();
//     if (branchid == "") {
//         swal("Invalid Branch!", "Please select valid Branch.", "error");
//         return false;
//     }
//     var isValid = validateItemOnAdd(parentDiv);
//     if (!isValid) {
//         return false;
//     }
//     var purchaseOrderCategoryId = $("#purchaseOrderCategoryId option:selected").val();

//     if(purchaseOrderCategoryId == "npr"){
//         addItemsPurchaseRequisitionNormalTxn(mainTableID, mainTableTrId, parentDiv);
//     }else{
//         addItemsPurchaseRequisitionAsoTxn(mainTableID, mainTableTrId, parentDiv);
//     }
// }

// var addItemsPurchaseRequisitionNormalTxn = function(mainTableID, mainTableTrId, parentDiv){
//     var bomTr = [];
//     var i = 0;
//     var length = $("#" + parentDiv + " table[class='multipleItemsTable'] > tbody > tr").length;
//     var multiItemTr = 'purreq'+length;
//     bomTr[i++] = '<tr id="'+multiItemTr+'">';
//     bomTr[i++] = '<td><select class="txnItems" id="purreqItem" onChange="onPurchaseOrderItemChange(this);"><option value="">Please Select</option></select></td>';
//     bomTr[i++] = '<td><input class="txnInStockUnit" placeholder="Units in stock" type="text" id="purreqInStockUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td style="display:none;"><input class="txnCommitedUnit" placeholder="Committed Units" type="text" id="purreqCommitedUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td><input class="txnOrderedUnit" placeholder="Ordered Units" type="text"  id="purreqOrderedUnit" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="addAvailableAndOrderedUnits(this);"></td>';
//     bomTr[i++] = '<td><input class="txnNetUnit" placeholder="Net Units" type="text" id="purreqNetUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td><input class="txnNoOfUnit" type="text" id="purreqNoOfUnit" onkeypress="return onlyDotsAndNumbers(event)" placeholder="Units(if any)"></td>';
//     bomTr[i++] = '<td><select class="masterList" id="purreqVendor"><option value="">--Please Select--</option></select></td>';
//     bomTr[i++] = '<td><input type="text" name="purreqExpDelDate" class="purreqExpDelDateCls"></td>';
//     bomTr[i++] = '<td><div class="klBranchSpecfTd"></div></td>';
//     bomTr[i++] = '<td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>';
//     $("#" + parentDiv + " table[class='multipleItemsTable'] > tbody").append(bomTr.join(''));
//     $("#" + multiItemTr + " .txnItems").children().remove();
//     $("#" + multiItemTr + " .txnItems").append('<option value="">Please Select</option>');
//     $("#" + multiItemTr + " .txnItems").append(COA_EXPENS_4USER_BRANCH_LIST);
//     $("#" + multiItemTr + " select[class='masterList']").children().remove();
//     $("#" + multiItemTr + " select[class='masterList']").append('<option value="">Please Select</option>');
//     $("#" + multiItemTr + " select[class='masterList']").append(VENDOR_4USER_BRANCH_LIST);
//     custVendSelect2();
//     initMultiItemsSelect2();
//     $("#" + multiItemTr + " input[class='purreqExpDelDateCls']").datepicker({
//         changeMonth : true,
//         changeYear : true,
//         dateFormat:  'MM d,yy',
//         yearRange: ''+new Date().getFullYear()+':'+maximumYear+'',
//         onSelect: function(x,y){
//             $(this).focus();
//         }
//     });
// }


// var addAvailableAndOrderedUnits = function(elem){
//     var parentTr = $(elem).closest('tr').attr('id');
//     var txnInStockUnit = $("#" + parentTr + " input[class='txnInStockUnit']").val();
//     var txnOrderedUnit = $("#" + parentTr + " input[class='txnOrderedUnit']").val();
//     if(txnInStockUnit == "")
//         return;
//     if(txnOrderedUnit == "")
//         return;
//     var txnNetUnit = parseFloat(txnInStockUnit) + parseFloat(txnOrderedUnit);
//     $("#" + parentTr + " input[class='txnNetUnit']").val(parseFloat(txnNetUnit).toFixed(2));
// }

// var readMulitItemDataPurchaseRequisition = function(multiItemTbl){
//     var multipleItemsData = [];
//     $("#"+multiItemTbl+ " > tbody > tr").each(function() {
//         var itemId = $(this).find("td .txnItems option:selected").val();
//         var txnInStockUnit = $(this).find("td input[class='txnInStockUnit']").val();
//         var txnCommitedUnit = $(this).find("td input[class='txnCommitedUnit']").val();
//         var txnOrderedUnit = $(this).find("td input[class='txnOrderedUnit']").val();
//         var txnNetUnit = $(this).find("td input[class='txnNetUnit']").val();
//         var txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
//         var txnVendor = $(this).find("td .masterList").val();
//         var txnExpDelDate = $(this).find("td input[name='purreqExpDelDate']").val();
//         var followedkl = $(this).find("td div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
//         var txnKnowledgeFollowed = "0";
//         if(typeof followedkl!='undefined'){
//             if($(this).find("td div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
//                 txnKnowledgeFollowed="1";
//             }
//             if($(this).find("td div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
//                 txnKnowledgeFollowed="0";
//             }
//         }
//         var jsonData = {};
//         jsonData.itemId = itemId;
//         jsonData.txnInStockUnit = txnInStockUnit;
//         jsonData.txnCommitedUnit = txnCommitedUnit;
//         jsonData.txnOrderedUnit = txnOrderedUnit;
//         jsonData.txnNetUnit = txnNetUnit;
//         jsonData.txnNoOfUnit = txnNoOfUnit;
//         jsonData.txnVendor = txnVendor;
//         jsonData.txnExpDelDate = txnExpDelDate;
//         jsonData.txnKnowledgeFollowed = txnKnowledgeFollowed;
//         multipleItemsData.push(JSON.stringify(jsonData));
//     });
//     return multipleItemsData;
// }

// var submitForApprovalPrTxn = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
//     var poType = $("#purchaseOrderCategoryId").find('option:selected').val();
//     if(poType == "npr"){
//         approvePurchaseRequisitionTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
//     }else{
//         approvePurchaseRequisitionAsoTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
//     }
// }

// var approvePurchaseRequisitionTxn = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
//     var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
//     var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
//     //var txnVendor = $("#"+parentTr+" .masterList option:selected").val();
//     var txnPoReference =  $("#"+parentTr+" input[class='txnPoReference']").val();
//     var purchaseOrderCategoryId = $("#purchaseOrderCategoryId option:selected").val();
//     var txnForItem = readMulitItemDataPurchaseRequisition('multipleItemsTablepurreq');
//     var txnRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClass']").val();
//     var txnPrivateRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClassPrivate']").val();
//     var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
//         if($(this).val() != ""){
//             return $(this).val();
//         }
//     }).get();
//     var supportingDoc = supportingDocTmp.join(',');

//     if (txnForBranch === "") {
//         swal("Incomplete data!", "select Branch First", "error");
//         enableTransactionButtons();
//         return false;
//     }

//     var txnJsonData={};
//     txnJsonData.txnEntityID = "";
//     txnJsonData.txnPurpose = whatYouWantToDo;
//     txnJsonData.txnPurposeVal = whatYouWantToDoVal;
//     txnJsonData.txnForItem = txnForItem;
//     txnJsonData.txnForBranch = txnForBranch;
//     txnJsonData.txnForProject = txnForProject;
//     txnJsonData.txnPoReference = txnPoReference;
//     txnJsonData.purchaseOrderCategoryId = purchaseOrderCategoryId;
//     txnJsonData.txnRemarks = txnRemarks;
//     txnJsonData.txnPrivateRemarks = txnPrivateRemarks;
//     txnJsonData.supportingdoc = supportingDoc;
//     txnJsonData.txnDocumentUploadRequired = "false";
//     txnJsonData.useremail = $("#hiddenuseremail").text();
//     var url="/transaction/submitForApproval";
//     $.ajax({
//         url: url,
//         data:JSON.stringify(txnJsonData),
//         type:"text",
//         headers:{
//             "X-AUTH-TOKEN": window.authToken
//         },
//         method:"POST",
//         contentType:'application/json',
//         success: function (data) {
//             if(typeof data.message !=='undefined' && data.message != ""){
//                 swal("Error!", data.message, "error");
//                 enableTransactionButtons();
//                 return false;
//             }
//             cancel();
//         },
//         error: function (xhr, status, error) {
//             if(xhr.status == 401){ doLogout();
//             }else if(xhr.status == 500){
//                 swal("Error on Submit For Approval!", "Please contact support team or retry.", "error");
//             }
//         },
//         complete: function(data) {
//             $.unblockUI();
//             enableTransactionButtons();
//         }
//     });
// }

// var populatePurchaseReqisitionAsoTxnItemsRow = function (bomItemlist, multiItemTrKey, parentDiv) {
//     for(var i=0; i<bomItemlist.length; i++){
//         if(i > 0){
//             $("#" +parentDiv+ " .addnewItemForTransaction").trigger('click');
//         }
//         var parentTr = multiItemTrKey+i;
//         $("#"+parentTr + " .txnItems option[value='"+bomItemlist[i].expenseId+"']").prop("selected","selected").trigger('change');
//         $("#"+parentTr + " input[class='txnOrderedUnit']").val(parseFloat(bomItemlist[i].unfulfilledUnits).toFixed(2)).trigger('onkeyup');
//         $("#"+parentTr + " .masterList option[value='"+bomItemlist[i].vendorid+"']").prop("selected","selected").trigger('change');
//         /*if(bomItemlist[i].netUnits !== "") {
//             $("#" + parentTr + " input[class='txnNoOfUnit']").val(parseFloat(bomItemlist[i].netUnits).toFixed(2)).trigger('onkeyup');
//         }
//         $("#" + parentTr + " input[class='txnNoOfUnit']").val('');
//         if(bomItemlist[i].pricePerUnit !== "") {
//             $("#" + parentTr + " input[class='txnPerUnitPrice']").val(parseFloat(bomItemlist[i].pricePerUnit).toFixed(2)).trigger('onkeyup');
//         }
//         if(bomItemlist[i].totalPrice !== "") {
//             $("#" + parentTr + " input[class='txnTotalPrice']").val(parseFloat(bomItemlist[i].totalPrice).toFixed(2)).trigger('onkeyup');
//         }*/
//     }
// }

// var addItemsPurchaseRequisitionAsoTxn = function(mainTableID, mainTableTrId, parentDiv){
//     var bomTr = [];
//     var i = 0;
//     var length = $("#" + parentDiv + " table[class='multipleItemsTable'] > tbody > tr").length;
//     var multiItemTr = 'prqaso'+length;
//     bomTr[i++] = '<tr id="'+multiItemTr+'">';
//     bomTr[i++] = '<td><select class="txnItems" id="purreqItem" onChange="onPurchaseOrderItemChange(this);"><option value="">Please Select</option></select></td>';
//     bomTr[i++] = '<td><input class="txnInStockUnit" placeholder="Units in stock" type="text" id="purreqInStockUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td style="display:none;"><input class="txnCommitedUnit" placeholder="Committed Units" type="text" id="purreqCommitedUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td><input class="txnOrderedUnit" placeholder="Ordered Units" type="text"  id="purreqOrderedUnit" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="addAvailableAndOrderedUnits(this);"></td>';
//     bomTr[i++] = '<td><input class="txnNetUnit" placeholder="Net Units" type="text" id="purreqNetUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td><select class="masterList" id="purreqVendor" onchange="getVendorGstinList(this);"><option value="">--Please Select--</option></select></td>';
//     bomTr[i++] = '<td><input class="txnNoOfUnit" type="text" id="NoOfUnit" onkeypress="return onlyDotsAndNumbers(event)" placeholder="Units(if any)" onkeyup="calculateTotalItemPriceBomPurchaseReqTxn(this);"></td>';
//     bomTr[i++] = '<td><input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text"  id="PricePerUnits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateTotalItemPriceBomPurchaseReqTxn(this);"/></td>';
//     bomTr[i++] = '<td><input class="txnTotalPrice" placeholder="Total Price" type="text" id="TotalPrice" readonly="readonly"></td>';
//     bomTr[i++] = '<td><select class="placeOfSply txnDestGstinCls" id="PlaceOfVend"><option value="">--Please Select--</option></select></td>';
//     bomTr[i++] = '<td>&nbsp;<input class="txnConfimPoCheckBox" type="checkbox"/></td>';
//     bomTr[i++] = '<td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>';
//     $("#" + parentDiv + " table[class='multipleItemsTable'] > tbody").append(bomTr.join(''));
//     $("#" + multiItemTr + " .txnItems").children().remove();
//     $("#" + multiItemTr + " .txnItems").append('<option value="">Please Select</option>');
//     $("#" + multiItemTr + " .txnItems").append(COA_EXPENS_4USER_BRANCH_LIST);
//     $("#" + multiItemTr + " select[class='masterList']").children().remove();
//     $("#" + multiItemTr + " select[class='masterList']").append('<option value="">Please Select</option>');
//     $("#" + multiItemTr + " select[class='masterList']").append(VENDOR_4USER_BRANCH_LIST);

//     $("#" + multiItemTr + " select[class='placeOfSply txnDestGstinCls']").children().remove();
//     $("#" + multiItemTr + " select[class='placeOfSply txnDestGstinCls']").append('<option value="">Please Select</option>');
//     $("#" + multiItemTr + " select[class='placeOfSply txnDestGstinCls']").append(GLOBAL_GSTIN_LIST_FOR_VENDOR);
//     custVendSelect2();
//     initMultiItemsSelect2();
// }


var readMulitItemDataPurchaseRequisition = function(){
    var multipleItemsData = [];
    $("#purchaseRequisitionCreateTableBody > tr").each(function() {
        
            
            var txnExpenseItemId = $(this).children("td:eq(0)").children("select").val();
            var txnNoOfUnit = $(this).children("td:eq(1)").children("input").val();
            var txnUnitOfMeasure = $(this).children("td:eq(2)").children("input").val();
            var txnVendor = $(this).children("td:eq(3)").children("select").val();
            var txnOem = $(this).children("td:eq(4)").children("input").val();
            var txnExpectedDate = $(this).children("td:eq(5)").children("input").val();
            var txnTypeOfItem = $(this).children("td:eq(6)").children("select").val();
            
            var jsonData = {};
            jsonData.itemId = txnExpenseItemId;
            jsonData.txnNoOfUnit = txnNoOfUnit;
            jsonData.txnMeasureName = txnUnitOfMeasure;
            jsonData.txnVendor = txnVendor;
            jsonData.txnOem = txnOem;
            jsonData.txnExpDelDate = txnExpectedDate;
            jsonData.txnTypeOfMaterial = txnTypeOfItem;
            
            multipleItemsData.push(JSON.stringify(jsonData));
        
    });
    return multipleItemsData;
}

var validateIfPRItemRowsAreFilled = function(){
    var validity = true;
    $("#purchaseRequisitionCreateTableBody > tr").each(function() {
        var txnExpenseItemId = $(this).children("td:eq(0)").children("select").val();
        var txnNoOfUnit = $(this).children("td:eq(1)").children("input").val();
        var txnUnitOfMeasure = $(this).children("td:eq(2)").children("input").val();
        var txnVendor = $(this).children("td:eq(3)").children("select").val();
        var txnOem = $(this).children("td:eq(4)").children("input").val();
        var txnExpectedDate = $(this).children("td:eq(5)").children("input").val();
        var txnTypeOfItem = $(this).children("td:eq(6)").children("select").val();
            
            if(txnNoOfUnit == "") {
                swal("Missing No of Unit!", "Please check if 'No Of Unit' field get missed in any item row.", "error");
                validity = false;
                return false;
            }
            if(txnUnitOfMeasure == "") {
                swal("Missing Unit of Measure!", "Please check if 'Unit of Measure' field get missed in any item row.", "error");
                validity = false;
                return false;
            }
            if(txnVendor == "") {
                swal("Missing Vendor!", "Please check if 'Vendor' field get missed in any item row.", "error");
                validity = false;
                return false;
            }
            if(txnExpectedDate == "") {
                swal("Missing Expected date!", "Please check if 'Expected date' field get missed in any item row.", "error");
                validity = false;
                return false;
            }
            if(txnTypeOfItem == "") {
                swal("Missing Type of item!", "Please check if 'Type of item' field get missed in any item row.", "error");
                validity = false;
                return false;
            }
            
        
    });
    return validity;
}

// var approvePurchaseRequisitionAsoTxn = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
//     var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
//     var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
//     var bomTxnId = $("#"+parentTr+" select[class='txnBomCls'] option:selected").val();
//     var txnCustomer = $("#"+parentTr+" .masterList option:selected").val();
//     var txnMasterItem = $("#"+parentTr+" .txnItems option:selected").val();
//     var txnPoReference =  $("#"+parentTr+" input[class='txnPoReference']").val();
//     var purchaseOrderCategoryId = $("#purchaseOrderCategoryId option:selected").val();
//     var txnForItem = readMulitItemDataPurchaseRequisitionAso('multipleItemsTableprqaso');
//     if(txnForItem.length <= 0){
//         swal("Incomplete data!", "Confirm PO is not selected for any item.", "error");
//         enableTransactionButtons();
//         return false;
//     }
//     var txnRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClass']").val();
//     var txnPrivateRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClassPrivate']").val();
//     var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
//         if($(this).val() != ""){
//             return $(this).val();
//         }
//     }).get();
//     var supportingDoc = supportingDocTmp.join(',');
//     if (txnForBranch === "") {
//         swal("Incomplete data!", "select Branch First", "error");
//         enableTransactionButtons();
//         return false;
//     }

//     if (bomTxnId === "") {
//         swal("Incomplete data!", "select Bill of Material transaction", "error");
//         enableTransactionButtons();
//         return false;
//     }

//     var txnJsonData={};
//     txnJsonData.txnEntityID = "";
//     txnJsonData.txnPurpose = whatYouWantToDo;
//     txnJsonData.txnPurposeVal = whatYouWantToDoVal;
//     txnJsonData.txnForItem = txnForItem;
//     txnJsonData.txnForBranch = txnForBranch;
//     txnJsonData.txnForProject = txnForProject;
//     txnJsonData.txnCustomer = txnCustomer;
//     txnJsonData.bomTxnId = bomTxnId;
//     txnJsonData.txnMasterItem = txnMasterItem;
//     txnJsonData.txnPoReference = txnPoReference;

//     txnJsonData.purchaseOrderCategoryId = purchaseOrderCategoryId;
//     txnJsonData.txnRemarks = txnRemarks;
//     txnJsonData.txnPrivateRemarks = txnPrivateRemarks;
//     txnJsonData.supportingdoc = supportingDoc;
//     txnJsonData.txnDocumentUploadRequired = "false";
//     txnJsonData.useremail = $("#hiddenuseremail").text();
//     var url="/transaction/submitForApproval";
//     $.ajax({
//         url: url,
//         data:JSON.stringify(txnJsonData),
//         type:"text",
//         headers:{
//             "X-AUTH-TOKEN": window.authToken
//         },
//         method:"POST",
//         contentType:'application/json',
//         success: function (data) {
//             if(typeof data.message !=='undefined' && data.message != ""){
//                 swal("Error!", data.message, "error");
//                 enableTransactionButtons();
//                 return false;
//             }
//             cancel();
//         },
//         error: function (xhr, status, error) {
//             if(xhr.status == 401){ doLogout();
//             }else if(xhr.status == 500){
//                 swal("Error on Submit For Approval!", "Please contact support team or retry.", "error");
//             }
//         },
//         complete: function(data) {
//             $.unblockUI();
//             enableTransactionButtons();
//         }
//     });
// }

var calculateTotalItemPriceBomPurchaseReqTxn = function(elem) {
    var multiitemTr = $(elem).closest('tr').attr('id');
    $("#"+multiitemTr + " input[class='txnTotalPrice']").val(parseFloat(0.0).toFixed(2));
}

// Functions for Purchase Requisition created by Harish Kumar

$(document).ready(function(){

    
    $("#addNewItemForPR").click(function(){
        setItemRowPurchaseReq();
    });    
    
    $("#purchaseRequisitionCreateTableBody").on("click", ".removeItemRowPurchaseReq", function() {
        if (confirm("This action will remove the component item!") == true) {
            $(this).parents("tr").remove();    
        }
        
    });

    $("#prCreateBomSelect").change(function() {
        if($(this).val() != "") {
            $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
            $.ajax({
                url: "/bom/getdetails/"+$(this).val(),
                headers:{
                    "X-AUTH-TOKEN": window.authToken
                },
                method:"GET",
                success: function (data){
                    console.log(data);
                    if(data.status == 'success'){
                        
                        let bomTableBody = $("#purchaseRequisitionCreateTableBody");
                        bomTableBody.empty();
                        setItemRowPurchaseReq(true);
                        for(var i=0; i<data.bomlist.length; i++){
                            if(i > 0){
                                $("#addNewItemForPR").trigger("click");
                            }
                            bomTableBody.children("tr:eq("+i+")").children("td:eq(0)").children("select").children("option[value='"+data.bomlist[i].expenseId+"']").prop("selected","selected");

                            bomTableBody.children("tr:eq("+i+")").children("td:eq(1)").children("input").val(data.bomlist[i].noOfUnit);
                        }
                        
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
    });

    //purchase requisition submission
    $("#purReqSubmitForApproval").click(function() {
        if ($("#prCreateBranchSelection").val() == "") {
            swal("Invalid Branch!", "Please select valid Branch.", "error");
            return false;
        }
        if(!validateIfPRItemRowsAreFilled()) {
            return false;
        }
        
        // fetch items row as object
        var txnForBranch = $("#prCreateBranchSelection").val();
        var txnForProject = $("#prCreateProjectSelect").val();
        var bomTxnId = $("#prCreateBomSelect").val();

        var txnJsonData={};
        txnJsonData.txnEntityID = ""; //change this value dynamically if sending any existing transaction id
        txnJsonData.txnPurpose = $("#purchaseRequisitionCategoryId").find('option:selected').val(); //pur req category dropdown value
        txnJsonData.txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val(); // integer value in transaction dropdown
        txnJsonData.txnForItem = readMulitItemDataPurchaseRequisition();
        txnJsonData.txnForBranch = txnForBranch;
        txnJsonData.txnForProject = txnForProject;
        txnJsonData.txnRemarks = ""; 
        txnJsonData.supportingdoc = "";
        txnJsonData.bomTxnId = bomTxnId;
        //txnJsonData.txnMasterItem = txnMasterItem;
        //txnJsonData.txnPoReference = txnPoReference;

        //txnJsonData.purchaseOrderCategoryId = purchaseOrderCategoryId;
        //txnJsonData.txnPrivateRemarks = txnPrivateRemarks;
        
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
                    $("#purchaseRequisitionCategoryId").hide();
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


    });
}); //ready function ends

var setItemRowPurchaseReq = function (isDefaultRow = false) {
    var prItemRow = `<tr>
        <td><select><option value="">Select Item</option>${localStorage.getItem("bomComponentItemsDropDown")}</select></td>
        <td><input type="number" style="margin-top:5px;"/></td>
        <td><input type="text" /></td>
        <td><select><option value="">Select Vendor</option>${localStorage.getItem("vendorListDropDown")}</select></td>
        <td><input type="text" /></td>
        <td><input type="text" class="datepicker"/></td>
        <td>
        <select>
			  <option value="" selected="">Select Item Type</option>
			  <option value="Perishable">Perishable</option>
			  <option value="Non Perishable">Non Perishable</option>
			  <option value="Combustible">Combustible</option>
			  <option value="Easily Available">Easily Available</option>
			  <option value="Rare Commodity">Rare Commodity</option>
			  <option value="long Delivery Period">long Delivery Period</option>
			  <option value="High Value">High Value</option>
			  <option value="Low Value">Low Value</option>
			  <option value="Difficult to Transport">Difficult to Transport</option>
			  <option value="Imported">Imported</option>
		</select>
        </td>
        <td>${!isDefaultRow ? '<button style="cursor: pointer; float:right; margin-top:5px;" class="removeItemRowPurchaseReq" title="remove item"><i class="fa fa-minus-circle fa-lg"></i></button>' : ''}</td>
    </tr>`;
    if(isDefaultRow) {
        $("#purchaseRequisitionCreateTableBody").html(prItemRow);   
    } else {
        $("#purchaseRequisitionCreateTableBody").append(prItemRow);   
    }
}

$(document).on("focusin", ".datepicker", function(){
    //applying datepicker on all dynamically created input date field
    $(this).datepicker({
        changeMonth : true,
        changeYear : true,
        dateFormat:  'MM d,yy',
        beforeShow: function(input) {
            setTimeout(function() {
                $(input).attr("readonly", true);
            }, 1);
        },
        onClose: function(input) {
            setTimeout(function() {
                $(input).attr("readonly", false);
            }, 1);
        },
        //yearRange: ''+new Date().getFullYear()+':'+maximumYear+'',
        onSelect: function(x,y){
            $(this).focus();
        }
    }).datepicker("show");
});


function populateBOMDrodown() {
    $.ajax({
        url: "/bom/getbyorganization",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method:"GET",
        success: function (data){
            if(data.status == 'success'){
                let bomDropdownOptions = "";
                for(var i=0; i<data.bomlist.length; i++){
                    bomDropdownOptions += '<option value="'+data.bomlist[i].entityId+'">'+data.bomlist[i].bomName+'-' +data.bomlist[i].entityId+'</option>';
                }
                $("#prCreateBomSelect").empty();
                $("#prCreateBomSelect").append('<option value="">Select BOM</option>');
                $("#prCreateBomSelect").append(bomDropdownOptions);
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
        }
    });
    //return false;
}

var selectPurchaseRequisitionCategoryTxn = function (elem) {
    var orderType = $(elem).val();
    var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
    //var transPurposeTxt=$("#whatYouWantToDo").find('option:selected').text();
    //$("#purchaseRequisitionAgainstSalesOrderTxnDiv").hide();
    //$("#createPurchaseOrderTxnDiv").hide();
    //$("#purchaseRequisitionTxnDivId").hide();
    //$("#poAgainstBomTxnDiv").hide();
    //$("#poAgainstRequisitionTxnDiv").hide();
    if(transPurposeId == CREATE_PURCHASE_REQUISITION){
        if (orderType == "npr") {
            $("#purchaseRequisitionTxnDivId").show();
            $("#prCreateBomSelect").hide();
            $("#prCreateBomHead").hide();
            setItemRowPurchaseReq(true);           
        } else if(orderType == "prabom") {
            $("#purchaseRequisitionTxnDivId").show();
            $("#prCreateBomSelect").show();
            $("#prCreateBomHead").show();
            setItemRowPurchaseReq(true);
        }
    }
}

