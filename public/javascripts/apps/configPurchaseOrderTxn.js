var setPurchaseOrderTxnCategory = function () {
  $("#purchaseOrderCategoryId").children().remove();
  $("#purchaseOrderCategoryId").append('<option value="">Please Select</option>');
  $("#purchaseOrderCategoryId").append('<option value="npo">Normal Purchase order</option>');
  $("#purchaseOrderCategoryId").append('<option value="poareq">Purchase order against requisition</option>');
  //<!--<option value="bom">Purchase order against Bill of material</option> -->
  $("#purchaseOrderCategoryDivId").show();
  $("#purchaseOrderCategoryId").find('option:first').prop("selected", "selected");
  
}

var selectPurchaseOrderCategoryTxn = function (elem) { 
  $("#createPurchaseOrderTxnDiv").show();
  var orderType = $(elem).val();
  var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
  if(transPurposeId == CREATE_PURCHASE_ORDER){
      $("#poCreateBranchSelection").val("");
      $("#poCreateProjectSelect").val("");
      if (orderType == "npo") {
          $("#purReqTitlePOCreateForm").hide();
          $("#purReqDropdownPOCreateForm").hide();
      } else if(orderType == "poareq") {
          $("#purReqTitlePOCreateForm").show();
          $("#purReqDropdownPOCreateForm").show();
          var prDropDownHtml = '<option>Select Purchase Requisition</option>';
          $("#poCreatePurchaseRequisitionSelect").html(prDropDownHtml);
          
      }
  }
  setItemRowPurchaseOrder(true);
}

// this method is for fetching all created Purchase Requisition
// var createPurchaseRequisitionDropdown = function() {
//     console.log("Called purchase requisition list for dropdown");
//     var url = "/transactions/purchaseRequisitionList";
//     $.ajax({
//         url: url,
//         type: "text",
//         headers: {
//             "X-AUTH-TOKEN": window.authToken
//         },
//         method: "GET",
//         contentType: 'application/json',
//         success: function (data) {
//             if (data.result === true) {
//                 let dropdownValues = "";
//                 data.items.forEach(function(element){
//                     console.log(element.name);
//                     dropdownValues += '<option value="'+element['id']+'">'+element['purReqRefNo']+'</option>';  
//                 });
//                 localStorage.setItem('purchseRequisitionDropdown', dropdownValues);

//             }
//         }
//     })
// }
function convertNegativeToZero(inputField) {
  inputField.addEventListener('input', function() {
      var value = Number(inputField.value);

      if (value < 0) {
      inputField.value = '0';
      }
  });
}
var setItemRowPurchaseOrder = function (isDefaultRow = false) {
  var prItemRow = `<tr>
      <td><select><option value="">Select Item</option>${localStorage.getItem("bomComponentItemsDropDown")}</select></td>
      <td><input type="number" min="0" onkeyup="convertNegativeToZero(this)" style="margin-top:5px;"/></td>
      <td><input type="text" /></td>
      <td><select class="poCreateVendorDropDown" onchange="getPlacesForVendor(this)"><option value="">Select Vendor</option>${localStorage.getItem("vendorListDropDown")}</select></td>
      <td><select></select></td>
      <td><input type="text" /></td>
      <td><input type="text" class="deliveryDate"/></td>
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
      <td>${!isDefaultRow ? '<button style="cursor: pointer; float:right; margin-top:5px;" class="removeItemRowPurchaseOrder" title="remove item"><i class="fa fa-minus-circle fa-lg"></i></button>' : ''}</td>
  </tr>`;
  if(isDefaultRow) {
      $("#purchaseOrderCreateTableBody").html(prItemRow);   
  } else {
      $("#purchaseOrderCreateTableBody").append(prItemRow);   
  }
}

var readMulitItemDataPurchaseOrder = function(){
  var multipleItemsData = [];
  $("#purchaseOrderCreateTableBody > tr").each(function() {
      
          
          var txnExpenseItemId = $(this).children("td:eq(0)").children("select").val();
          var txnNoOfUnit = $(this).children("td:eq(1)").children("input").val();
          var txnUnitOfMeasure = $(this).children("td:eq(2)").children("input").val();
          var txnVendor = $(this).children("td:eq(3)").children("select").val();
          var txnPlaceOfSupply = $(this).children("td:eq(4)").children("select").val();
          var txnOem = $(this).children("td:eq(5)").children("input").val();
          var txnExpectedDate = $(this).children("td:eq(6)").children("input").val();
          var txnTypeOfItem = $(this).children("td:eq(7)").children("select").val();
          
          var jsonData = {};
          jsonData.itemId = txnExpenseItemId;
          jsonData.txnNoOfUnit = txnNoOfUnit;
          jsonData.txnMeasureName = txnUnitOfMeasure;
          jsonData.txnVendor = txnVendor;
          jsonData.txnPlaceOfSupply = txnPlaceOfSupply;
          jsonData.txnOem = txnOem;
          jsonData.txnExpDelDate = txnExpectedDate;
          jsonData.txnTypeOfMaterial = txnTypeOfItem;
          
          multipleItemsData.push(JSON.stringify(jsonData));
      
  });
  return multipleItemsData;
}

var validateIfPOItemRowsAreFilled = function(){
  var validity = true;
  $("#purchaseOrderCreateTableBody > tr").each(function() {
          var txnExpenseItemId = $(this).children("td:eq(0)").children("select").val();
          var txnNoOfUnit = $(this).children("td:eq(1)").children("input").val();
          var txnUnitOfMeasure = $(this).children("td:eq(2)").children("input").val();
          var txnVendor = $(this).children("td:eq(3)").children("select").val();
          var txnPlaceOfSupply = $(this).children("td:eq(4)").children("select").val();
          var txnOem = $(this).children("td:eq(5)").children("input").val();
          var txnExpectedDate = $(this).children("td:eq(6)").children("input").val();
          var txnTypeOfItem = $(this).children("td:eq(7)").children("select").val();
          
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

var populatePRDropdown = function() {
  var poCategory = $("#purchaseOrderCategoryId").val();
  var branchId = $("#poCreateBranchSelection").val();
  var projectId = $("#poCreateProjectSelect").val();
  if(projectId == "") {
      projectId = 0;
  }
  if(poCategory == "poareq") {
      var url = "/transactions/purchaseRequisitionListByBranchIdAndProjectId/"+branchId+"/"+projectId;
      $.ajax({
          url: url,
          type: "text",
          headers: {
              "X-AUTH-TOKEN": window.authToken
          },
          method: "GET",
          contentType: 'application/json',
          success: function (data) {
              let dropdownValues = '<option>Select Purchase Requisition</option>';
              if (data.result === true) {
                  data.items.forEach(function(element){
                      dropdownValues += '<option value="'+element['id']+'">'+element['purReqRefNo']+'</option>';  
                  });
                  
                  
              }
              $("#poCreatePurchaseRequisitionSelect").html(dropdownValues);
          }
      })

  }
}

var getPlacesForVendor = function(elem){
  
$(elem).parents("tr").children("td:eq(4)").children("select").html('<option value=""> Please Select </option>');
var branchId=$("#poCreateBranchSelection").val();
var vendid=$(elem).val();
if(branchId==="") {
      swal("Please select branch!", "Please retry, if problem persists contact support team", "error");
  }
  if(vendid === "" || branchId ===""){
  return false;
}

$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
var jsonData = {};
jsonData.useremail=$("#hiddenuseremail").text();
jsonData.txnBranchId=branchId;
jsonData.txnVendorId=vendid;
var url="/vendor/vendorlocations";
$.ajax({
    url: url,
    data:JSON.stringify(jsonData),
    type:"text",
  headers:{
    "X-AUTH-TOKEN": window.authToken
  },
  async: true,
    method:"POST",
    contentType:'application/json',
    success: function (data) {
      
    
    var gstinListTemp = "";
    for(var i=0;i<data.custGstinList.length;i++){
      gstinListTemp += ('<option  value="'+data.custGstinList[i].gstin+'" id="'+data.custGstinList[i].vendorDetailId+'">'+data.custGstinList[i].custLocation+'</option>');
    }
    $(elem).parents("tr").children("td:eq(4)").children("select").append(gstinListTemp);
          //GLOBAL_GSTIN_LIST_FOR_VENDOR = gstinListTemp;
    // Single User
    // if(data.custGstinList.length == 1) {
    // 	$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls'] option:last").attr("selected", "selected");
    //  	$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").trigger("change");
    // }

  },
  error: function (xhr, status, error) {
    if(xhr.status == 401){
      doLogout();
    }else if(xhr.status == 500){
        swal("Error on fetching GSTIN for Vendor!", "Please retry, if problem persists contact support team", "error");
      }
  },
  complete: function(data) {
    $.unblockUI();
  }
   });
}

$(document).on("change", ".poCreateVendorDropDown", function(){
  var selectedExpItem = $(this).parents("tr").children("td:eq(0)").children("select").val();
  
  if(selectedExpItem == "") {
      swal("Please select Item!", "Please retry, if problem persists contact support team", "error");
      $(this).find('option:first').prop("selected", "selected");
      return false;
  } else {
      var noDuplicate = true;
      var currentSelectionVal = $(this);
      var selectedRowExpenseItem =  $(this).parents("tr").children("td:eq(0)").children("select").val();
      var totalItemRows = $("#purchaseOrderCreateTableBody > tr").length;
      var i = 1;
      if(totalItemRows > 1) {
          $("#purchaseOrderCreateTableBody > tr").each(function() {
              if(i < totalItemRows) {
              
                  var selectedVendor = $(this).children("td:eq(3)").children("select").val();
                  var selectedExpenseItem = $(this).children("td:eq(0)").children("select").val();
                  if((selectedVendor == currentSelectionVal.val()) && (selectedExpenseItem == selectedRowExpenseItem)) {
                      noDuplicate =false;
                      
                      currentSelectionVal.find('option:first').prop("selected", "selected");
                      if (currentSelectionVal.data('select2')) {
                          currentSelectionVal.select2('destroy');
                      }
                      swal("Duplicate vendor and expense!", "There is already a row with same vendor and item, Please select different vendor or expense item", "error");   
                      return false;
                  }
                  i++;
              }
          });
      }
      if(noDuplicate) {
          $(this).select2({
              placeholder: "Select an item",
              allowClear: true,
              multiple: false
          });
      }
  }
});

//display list of POs when click on "view PO" link in transaction list
var listMultiPOItems = function(elem) {
  var parentTr = $(elem).closest('tr').attr('id');
var transactionEntityId=parentTr.substring(17, parentTr.length);
  var txnReferenceNo = $("#"+parentTr+" td:first p").text();
var jsonData = {};
var useremail=$("#hiddenuseremail").text();
jsonData.transactionEntityId= transactionEntityId;
jsonData.usermail = useremail;
jsonData.txnReferenceNo = txnReferenceNo;
var url="/transactionItems/getListOfMultipleItems";
$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
          $("#popUpShowPOInformation").attr('data-toggle', 'modal');
          $("#popUpShowPOInformation").modal('show');
      // $(".staticMutipleTransactionItemsclose").attr("href",location.hash);
        $("#popUpShowPOInformation div[class='modal-body']").html("");
        if(data.transactionItemdetailsData.length>0){
            if(txnReferenceNo.startsWith("PO") || txnReferenceNo.startsWith("PR")){
                      $("#popUpShowPOInformation .panel-title").text("Transaction Items for " + txnReferenceNo);
                      $("#popUpShowPOInformation div[class='modal-body']").html('<div class="datascrolltable" style="height: 100%; overflow: auto;"><table style="width:100%; float:left;"><thead><tr><th>Branch</th><th>Project</th><th>Purchase Req (if any)</th></tr><tr><td></td><td></td><td></td></tr></thead></table><table id="multipleSellItemsBreakupTable" style="margin-top: 5px; width:100%; float:left;">' +
                          '<thead class="tablehead1"><tr><th>ITEM</th><th>NO. OF UNITS</th><th>Vendor</th><th>Measure Name</th><th>OEM</th><th>TYPE OF MATERIAL</th><tr></thead><tbody></tbody></table></div>');
                          
                      for (var i = 0; i < data.transactionItemdetailsData.length; i++) {
                          $("#popUpShowPOInformation div[class='modal-body'] table[id='multipleSellItemsBreakupTable'] tbody").append('<tr id=' + i + '><td>' + data.transactionItemdetailsData[i].itemName + '</td><td>' + data.transactionItemdetailsData[i].noOfUnits + '</td><td>' + data.transactionItemdetailsData[i].vendor + '</td><td>' + data.transactionItemdetailsData[i].measureName + '</td><td>' + data.transactionItemdetailsData[i].oem + '</td><td>' + data.transactionItemdetailsData[i].typeOfMaterial + '</td></tr>');
                      }
                  }
        }
    },
    error: function (xhr, status, error) {
      if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
          swal("Error on fetching transaction items!", "Please retry, if problem persists contact support team", "error");
        }
    },
    complete: function(data) {
      $.unblockUI();
    }
  });
  
}

$(document).ready(function(){

  
  $("#addNewItemForPO").click(function(){
      setItemRowPurchaseOrder();
  });

  $("#purchaseOrderCreateTableBody").on("focus", ".deliveryDate", function(){
      $(this).datepicker({
          changeMonth : true,
          changeYear : true,
          dateFormat:  'MM d,yy',
          minDate: 0,
          yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+''
      });
  });

  $("#purchaseOrderCreateTableBody").on("click", ".removeItemRowPurchaseOrder", function() {
      if (confirm("This action will remove the component item!") == true) {
          $(this).parents("tr").remove();    
      }
      
  });

  $("#poCreatePurchaseRequisitionSelect").change(function() {
      if($(this).val() != "") {
          $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
          $.ajax({
              url: "/transactions/purchaseRequisitionItemsList/"+$(this).val(),
              headers:{
                  "X-AUTH-TOKEN": window.authToken
              },
              method:"GET",
              success: function (data){
                  console.log(data);
                  if(data.result == true){
                      
                      let poCreateTableBody = $("#purchaseOrderCreateTableBody");
                      poCreateTableBody.empty();
                      setItemRowPurchaseOrder(true);
                      for(var i=0; i<data.items.length; i++){
                          if(i > 0){
                              $("#addNewItemForPO").trigger("click");
                          }
                          poCreateTableBody.children("tr:eq("+i+")").children("td:eq(0)").children("select").children("option[value='"+data.items[i].expense_id+"']").prop("selected","selected");

                          poCreateTableBody.children("tr:eq("+i+")").children("td:eq(1)").children("input").val(data.items[i].no_of_unit);
                          poCreateTableBody.children("tr:eq("+i+")").children("td:eq(2)").children("input").val(data.items[i].measure_name);
                          poCreateTableBody.children("tr:eq("+i+")").children("td:eq(3)").children("select").children("option[value='"+data.items[i].vendor_id+"']").prop("selected","selected");
                          poCreateTableBody.children("tr:eq("+i+")").children("td:eq(5)").children("input").val(data.items[i].oem);
                          var formattedDate = $.datepicker.formatDate('M dd, yy', new Date(data.items[i].expected_date));
                          poCreateTableBody.children("tr:eq("+i+")").children("td:eq(6)").children("input").val(formattedDate);
                          poCreateTableBody.children("tr:eq("+i+")").children("td:eq(7)").children("select").children("option[value='"+data.items[i].type_of_material+"']").prop("selected","selected");
                      }
                      
                  }else{
                      $("#notificationMessage").html("Fetch of list if purchase requisition items has been failed.");
                  }
              },
              error: function (xhr, status, error){
                  if(xhr.status == 401){
                      doLogout();
                  }else if(xhr.status == 500){
                      swal("Error on fetching Purchase Requisition Items!", "Please retry, if problem persists contact support team", "error");
                  }
              },
              complete: function(data) {
              $.unblockUI();
              }
          });
      }
  });

  //purchase requisition submission
  $("#purchaseOrderSubmitForApproval").click(function() {
      if ($("#poCreateBranchSelection").val() == "") {
          swal("Invalid Branch!", "Please select valid Branch.", "error");
          return false;
      }
      if(!validateIfPOItemRowsAreFilled())
      {
          return false;
      }
      
      // fetch items row as object
      var txnForBranch = $("#poCreateBranchSelection").val();
      var txnForProject = $("#poCreateProjectSelect").val();
      var prTxnId = $("#poCreatePurchaseRequisitionSelect").val();

      var txnJsonData={};
      txnJsonData.txnEntityID = ""; //change this value dynamically if sending any existing transaction id
      txnJsonData.txnPurpose = $("#purchaseOrderCategoryId").find('option:selected').val(); //pur req category dropdown value
      txnJsonData.txnPurposeVal = $("#whatYouWantToDo").find('option:selected').val(); // integer value in transaction dropdown
      txnJsonData.txnForItem = readMulitItemDataPurchaseOrder();
      txnJsonData.txnForBranch = txnForBranch;
      txnJsonData.txnForProject = txnForProject;
      txnJsonData.txnRemarks = ""; 
      txnJsonData.supportingdoc = "";
      txnJsonData.prTxnId = prTxnId;
      txnJsonData.txnParentPurchaseOrder = "";
      
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
                  $("#purchaseOrderCategoryId").hide();
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
}); //ready functionends


// /*
//     Also used in CREATE_PURCHASE_REQUISITION
//  */
// var selectPurchaseOrderCategoryTxn = function (elem) {
//     var orderType = $(elem).val();
//     var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
//     var transPurposeTxt=$("#whatYouWantToDo").find('option:selected').text();
//     $("#purchaseRequisitionAgainstSalesOrderTxnDiv").hide();
//     $("#createPurchaseOrderTxnDiv").hide();
//     $("#purchaseRequisitionTxnDivId").hide();
//     $("#poAgainstBomTxnDiv").hide();
//     $("#poAgainstRequisitionTxnDiv").hide();
//     if(transPurposeId == CREATE_PURCHASE_ORDER) {
//         if (orderType == "npo") {
//             resetMultiItemsTableLength('createPurchaseOrderTxnDiv');
//             resetMainTransTableFields('crtprrtrid');
//             resetMultiItemsTableFieldsData('#crtprrtrid');
//             $("#createPurchaseOrderTxnDiv").show();
//             populateItemBasedOnWhatYouWantToDo(transPurposeId, transPurposeTxt);
//         } else if (orderType == "req") {
//             resetMultiItemsTableLength('poAgainstRequisitionTxnDiv');
//             resetMainTransTableFields('poareqtrid');
//             resetMultiItemsTableFieldsData('#poareqtrid');
//             $("#poAgainstRequisitionTxnDiv").show();
//             populateItemBasedOnWhatYouWantToDo(transPurposeId, transPurposeTxt);
//         } else if (orderType == "bom") {
//             resetMultiItemsTableLength('poAgainstBomTxnDiv');
//             resetMainTransTableFields('poabomtrid');
//             resetMultiItemsTableFieldsData('#poabomtrid');
//             $("#poAgainstBomTxnDiv").show();
//             populateItemBasedOnWhatYouWantToDo(transPurposeId, transPurposeTxt);
//         }
//     }
// }

// var onPurchaseOrderItemChange = function(elem){
//     var parentTr = $(elem).closest('tr').attr('id');
//     var txnitemSpecifics = $("#"+parentTr+" .txnItems option:selected").val();
//     if(txnitemSpecifics =="" || typeof txnitemSpecifics == 'undefined') {
//         return false;
//     }
//     var isvalid = validateSelectedItems(elem);
//     if(!isvalid){
//         return false;
//     }
//     getExpenseItemAvailableStock(elem);
//     showTransactionBranchKnowledgeLiabrary(elem);
//     getExpenseItemUnfulfilledUnits(elem);
// }

// var getExpenseItemUnfulfilledUnits = function(elem) {
//     var parentDiv = $(elem).closest('div').parent().attr('id');
//     var branchid = $("#" + parentDiv + " select[class='txnBranches'] option:selected").val();
//     var multiitemTr = $(elem).closest('tr').attr('id');
//     var txnitemSpecifics=$("#"+multiitemTr+" .txnItems").val();
//     var url = "/bom/unfulfilled/"+txnitemSpecifics+"/"+branchid;
//     $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
//     $.ajax({
//         url: url,
//         type: "text",
//         headers: {
//             "X-AUTH-TOKEN": window.authToken
//         },
//         method: "GET",
//         contentType: 'application/json',
//         success: function (data) {
//             if (data.status === 'success') {
//                 $("#"+multiitemTr + " input[class='txnOrderedUnit']").val(parseFloat(data.total).toFixed(2));
//                 var txnInStockUnit = $("#"+multiitemTr + " input[class='txnInStockUnit']").val();
//                 if(txnInStockUnit != "" && parseFloat(txnInStockUnit) > 0.0){
//                     var tmpTotal = parseFloat(txnInStockUnit) + parseFloat(data.total);
//                     $("#"+multiitemTr + " input[class='txnNetUnit']").val(parseFloat(tmpTotal).toFixed(2));
//                 }else{
//                     $("#"+multiitemTr + " input[class='txnNetUnit']").val(parseFloat(data.total).toFixed(2));
//                 }
//             }
//         },
//         error: function (xhr, status, error) {
//             if (xhr.status == 401) {
//                 doLogout();
//             }else if(xhttp.status == 500){
//                 swal("Error on fetching available unfulfilled units!", "Please retry, if problem persists contact support team", "error");
//             }
//         },
//         complete: function(data) {
//             $.unblockUI();
//         }
//     });
// }

// var calculateTotalItemPriceTxn = function(elem) {
//     var multiitemTr = $(elem).closest('tr').attr('id');
//     var txnNoOfUnit = $("#"+multiitemTr + " input[class='txnNoOfUnit']").val();
//     if(txnNoOfUnit == "" || parseFloat(txnPerUnitPrice) === 0){
//         return false;
//     }
//     var txnPerUnitPrice = $("#"+multiitemTr + " input[class='txnPerUnitPrice']").val();
//     if(txnPerUnitPrice == "" || parseFloat(txnPerUnitPrice) === 0){
//         return false;
//     }
//     var tmpTotal = parseFloat(txnNoOfUnit) * parseFloat(txnPerUnitPrice);
//     $("#"+multiitemTr + " input[class='txnTotalPrice']").val(parseFloat(tmpTotal).toFixed(2));
// }

//     var validateItemOnAdd = function(parentDiv){
//     var count = 0;
//     $("#" + parentDiv + " .multipleItemsTable > tbody > tr").each(function() {
//         var itemId = $(this).find("td .txnItems option:selected").val();
//         if(itemId == ""){
//             count++;
//         }
//     });
//     if(count > 0){
//         swal("Invalid previous item data!", "Please data for previous item is not valid", "error");
//         return false;
//     }else{
//         return true;
//     }
// }

// var addItemsPurchaseOrderTxn = function(mainTableID, mainTableTrId, expenseItems) {
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
//     if(purchaseOrderCategoryId == "npo"){
//         addItemsPurchaseOrderNormalTxn(mainTableID, expenseItems, parentDiv, branchid);
//     }else{
//         addItemsPurchaseOrderAgainstRequisitionTxn(mainTableID, expenseItems, parentDiv, branchid);
//     }
// }

// var addItemsPurchaseOrderNormalTxn = function(mainTableID, expenseItems, parentDiv, branchid){
//     /*var parentDiv = $("#"+mainTableID).closest('div').attr('id');
//     var branchid = $("#" + mainTableID + " select[class='txnBranches'] option:selected").val();
//     if(branchid == ""){
//         swal("Invalid Branch!", "Please select valid Branch.", "error");
//         return false;
//     }
//     var isValid = validateItemOnAdd(parentDiv);
//     if(!isValid){
//         return false;
//     }*/
//     var bomTr = [];
//     var i = 0;
//     var length = $("#" + parentDiv + " table[class='multipleItemsTable'] > tbody > tr").length;
//     var multiItemTr = 'crtprr'+length;
//     bomTr[i++] = '<tr id="'+multiItemTr+'">';
//     bomTr[i++] = '<td><select class="txnItems" id="crtprrItem" onChange="onPurchaseOrderItemChange(this);"><option value="">Please Select</option></select></td>';
//     bomTr[i++] = '<td><input class="txnInStockUnit" placeholder="Units in stock" type="text" id="crtprrInStockUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td style="display:none;"><input class="txnCommitedUnit" placeholder="Committed Units" type="text" id="crtprrCommitedUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td><input class="txnOrderedUnit" placeholder="Ordered Units" type="text"  id="crtprrOrderedUnit" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="addAvailableAndOrderedUnits(this);"></td>';
//     bomTr[i++] = '<td><input class="txnNetUnit" placeholder="Net Units" type="text" id="crtprrNetUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td><input class="txnNoOfUnit" type="text" id="crtprrNoOfUnit" onkeypress="return onlyDotsAndNumbers(event)" placeholder="Units(if any)" onkeyup="calculateTotalItemPriceTxn(this);"></td>';
//     bomTr[i++] = '<td><input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text"  id="crtprrPricePerUnits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateTotalItemPriceTxn(this);"/></td>';
//     bomTr[i++] = '<td><input class="txnTotalPrice" placeholder="Total Price" type="text" id="crtprrTotalPrice" readonly="readonly"></td>';
//     bomTr[i++] = '<td><select class="placeOfSply txnDestGstinCls" id="crtprrPlaceOfVend"><option value="">--Please Select--</option></select></td>';
//     bomTr[i++] = '<td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>';
//     $("#" + parentDiv + " table[class='multipleItemsTable'] > tbody").append(bomTr.join(''));
//     $("#" + multiItemTr + " .txnItems").children().remove();
//     $("#" + multiItemTr + " .txnItems").append('<option value="">Please Select</option>');
//     $("#" + multiItemTr + " .txnItems").append(expenseItems);
//     $("#" + multiItemTr + " select[class='placeOfSply txnDestGstinCls']").children().remove();
//     $("#" + multiItemTr + " select[class='placeOfSply txnDestGstinCls']").append('<option value="">Please Select</option>');
//     $("#" + multiItemTr + " select[class='placeOfSply txnDestGstinCls']").append(GLOBAL_GSTIN_LIST_FOR_VENDOR);
//     //custVendSelect2();
//     //initMultiItemsSelect2();
// }

// var addItemsPurchaseOrderAgainstRequisitionTxn = function(mainTableID, expenseItems, parentDiv, branchid){
//     var bomTr = [];
//     var i = 0;
//     var length = $("#" + parentDiv + " table[class='multipleItemsTable'] > tbody > tr").length;
//     var multiItemTr = 'poareq'+length;
//     bomTr[i++] = '<tr id="'+multiItemTr+'">';
//     bomTr[i++] = '<td><select class="txnItems" id="poareqItem" onChange="onPurchaseOrderItemChange(this);"><option value="">Please Select</option></select></td>';
//     bomTr[i++] = '<td><input class="txnInStockUnit" placeholder="Units in stock" type="text" id="poareqInStockUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td style="display:none;"><input class="txnCommitedUnit" placeholder="Committed Units" type="text" id="poareqCommitedUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td><input class="txnOrderedUnit" placeholder="Ordered Units" type="text"  id="poareqOrderedUnit" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="addAvailableAndOrderedUnits(this);"></td>';
//     bomTr[i++] = '<td><input class="txnNetUnit" placeholder="Net Units" type="text" id="poareqNetUnit" onkeypress="return onlyDotsAndNumbers(event);"></td>';
//     bomTr[i++] = '<td><select class="masterList" onchange="getVendorGstinList(this);" id="poareqVendor"><option value="">--Please Select--</option></select></td>';
//     bomTr[i++] = '<td><input class="txnNoOfUnit" type="text" id="poareqNoOfUnit" onkeypress="return onlyDotsAndNumbers(event)" placeholder="Units(if any)" onkeyup="calculateTotalItemPriceTxn(this);"></td>';
//     bomTr[i++] = '<td><input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text"  id="poareqPricePerUnits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateTotalItemPriceTxn(this);"/></td>';
//     bomTr[i++] = '<td><input class="txnTotalPrice" placeholder="Total Price" type="text" id="poareqTotalPrice" readonly="readonly"></td>';
//     bomTr[i++] = '<td><select class="placeOfSply txnDestGstinCls" id="poareqPlaceOfVend"><option value="">--Please Select--</option></select></td>';
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

//     //custVendSelect2();
//     //initMultiItemsSelect2();
// }

// var readMulitItemDataPurchaseOrder = function(multiItemTbl){
//     var multipleItemsData = [];
//     $("#"+multiItemTbl+ " > tbody > tr").each(function() {
//         var itemId = $(this).find("td .txnItems option:selected").val();
//         var txnInStockUnit = $(this).find("td input[class='txnInStockUnit']").val();
//         var txnCommitedUnit = $(this).find("td input[class='txnCommitedUnit']").val();
//         var txnOrderedUnit = $(this).find("td input[class='txnOrderedUnit']").val();
//         var txnNetUnit = $(this).find("td input[class='txnNetUnit']").val();
//         var txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
//         var txnPerUnitPrice = $(this).find("td input[class='txnPerUnitPrice']").val();
//         var txnTotalPrice = $(this).find("td input[class='txnTotalPrice']").val();
//         var txnDestGstin = $(this).find("td select[class='placeOfSply txnDestGstinCls'] option:selected").val();

//         var jsonData = {};
//         jsonData.itemId = itemId;
//         jsonData.txnInStockUnit = txnInStockUnit;
//         jsonData.txnCommitedUnit = txnCommitedUnit;
//         jsonData.txnOrderedUnit = txnOrderedUnit;
//         jsonData.txnNetUnit = txnNetUnit;
//         jsonData.txnNoOfUnit = txnNoOfUnit;
//         jsonData.txnPerUnitPrice = txnPerUnitPrice;
//         jsonData.txnTotalPrice = txnTotalPrice;
//         jsonData.txnDestGstin = txnDestGstin;
//         multipleItemsData.push(JSON.stringify(jsonData));
//     });
//     return multipleItemsData;
// }

// var readMulitItemDataPoAainstRequisition = function(multiItemTbl){
//     var multipleItemsData = [];
//     $("#"+multiItemTbl+ " > tbody > tr").each(function() {
//         var itemId = $(this).find("td .txnItems option:selected").val();
//         var txnInStockUnit = $(this).find("td input[class='txnInStockUnit']").val();
//         var txnCommitedUnit = $(this).find("td input[class='txnCommitedUnit']").val();
//         var txnOrderedUnit = $(this).find("td input[class='txnOrderedUnit']").val();
//         var txnNetUnit = $(this).find("td input[class='txnNetUnit']").val();
//         var txnVendor = $(this).find("td .masterList").val();
//         var txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
//         var txnPerUnitPrice = $(this).find("td input[class='txnPerUnitPrice']").val();
//         var txnTotalPrice = $(this).find("td input[class='txnTotalPrice']").val();
//         var txnDestGstin = $(this).find("td select[class='placeOfSply txnDestGstinCls'] option:selected").val();

//         var jsonData = {};
//         jsonData.itemId = itemId;
//         jsonData.txnInStockUnit = txnInStockUnit;
//         jsonData.txnCommitedUnit = txnCommitedUnit;
//         jsonData.txnOrderedUnit = txnOrderedUnit;
//         jsonData.txnNetUnit = txnNetUnit;
//         jsonData.txnNoOfUnit = txnNoOfUnit;
//         jsonData.txnVendor = txnVendor;
//         jsonData.txnPerUnitPrice = txnPerUnitPrice;
//         jsonData.txnTotalPrice = txnTotalPrice;
//         jsonData.txnDestGstin = txnDestGstin;
//         multipleItemsData.push(JSON.stringify(jsonData));
//     });
//     return multipleItemsData;
// }

// var submitForApprovalPoTxn = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
//     var poType = $("#purchaseOrderCategoryId").find('option:selected').val();
//     if(poType == "npo"){
//         submitForApprovalPoNormalTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
//     }else{
//         submitForApprovalPoAgainstRequisitionTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
//     }
// }

// function submitForApprovalPoNormalTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr){
//     var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
//     var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
//     var txnVendor = $("#"+parentTr+" .masterList option:selected").val();
//     var txnMasterItem = $("#"+parentTr+" .txnItems option:selected").val();
//     var txnPoReference =  $("#"+parentTr+" input[class='txnPoReference']").val();
//     var purchaseOrderCategoryId = $("#purchaseOrderCategoryId option:selected").val();
//     var txnForItem = readMulitItemDataPurchaseOrder('multipleItemsTablecrtprr');
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

//     if (txnMasterItem === "") {
//         swal("Incomplete data!", "select income item", "error");
//         enableTransactionButtons();
//         return false;
//     }

//     if(txnVendor == "") {
//         swal("Incomplete data!", "select Customer for Proceed", "error");
//         enableTransactionButtons();
//         return false;
//     }

//     var txnJsonData={};
//     txnJsonData.txnEntityID = "";
//     txnJsonData.txnPurpose = whatYouWantToDo;
//     txnJsonData.txnPurposeVal = whatYouWantToDoVal;
//     txnJsonData.txnForBranch = txnForBranch;
//     txnJsonData.txnForProject = txnForProject;
//     txnJsonData.txnMasterItem = txnMasterItem;
//     txnJsonData.txnPoReference = txnPoReference;
//     txnJsonData.purchaseOrderCategoryId = purchaseOrderCategoryId;
//     txnJsonData.txnForItem = txnForItem;
//     txnJsonData.txnVendor = txnVendor;
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

// function submitForApprovalPoAgainstRequisitionTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr){
//     var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
//     var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
//     var bomTxnId = $("#"+parentTr+" select[class='txnBomCls'] option:selected").val();
//     var purchaseOrderCategoryId = $("#purchaseOrderCategoryId option:selected").val();
//     var txnForItem = readMulitItemDataPoAainstRequisition('multipleItemsTablepoareq');
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
//     txnJsonData.txnForBranch = txnForBranch;
//     txnJsonData.txnForProject = txnForProject;
//     txnJsonData.bomTxnId = bomTxnId;
//     txnJsonData.purchaseOrderCategoryId = purchaseOrderCategoryId;
//     txnJsonData.txnForItem = txnForItem;
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


// var getPurchaseOrderDetail = function(elem) {
//     var value = $("#whatYouWantToDo").find('option:selected').val();
//     var parentTr = $(elem).parent().parent('tr:first').attr('id');
//     var transactionTableTr = $(elem).closest('div').attr('id');
//     var invoiceTxnEntityId = $(elem).val();
//     if (invoiceTxnEntityId == "" || invoiceTxnEntityId == null || typeof invoiceTxnEntityId == 'undefined') {
//         return false;
//     }
//     var vendorId = $("#"+transactionTableTr+" .masterList option:selected").val();
//     var branchId = $("#"+transactionTableTr+" select[class='txnBranches'] option:selected").val();
//     $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
//     var url = "/bom/getTxnDetail/"+invoiceTxnEntityId + "/" + branchId + "/" +vendorId+"/1";
//     $.ajax({
//         url: url,
//         type:"text",
//         headers:{
//             "X-AUTH-TOKEN": window.authToken
//         },
//         method:"GET",
//         contentType:'application/json',
//         success: function (data) {
//             if(data.status == 'success') {
//                 for (var i = 0; i < data.bomItemlist.length; i++) {
//                     if (i > 0) {
//                         $("#" + transactionTableTr + " button[class='addnewItemForTransaction']").click();
//                     }
//                     var length = $("#" + transactionTableTr + " tbody tr").length;
//                     //$("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnItemTableIdHid']").val(data.bomItemlist[i].expenseId);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").val(data.bomItemlist[i].expenseId);
//                     $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnItems").change();
//                     initMultiItemsSelect2();
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last .txnItems").prop('disabled', true);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnPerUnitPrice']").val(data.bomItemlist[i].pricePerUnit);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnPerUnitPriceHid']").val(data.bomItemlist[i].pricePerUnit);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']").val(data.bomItemlist[i].unfulfilledUnits);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnitHid']").val(data.bomItemlist[i].unfulfilledUnits);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='originalNoOfUnitsHid']").val(data.bomItemlist[i].noOfUnit);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='txnGross']").val(data.bomItemlist[i].totalPrice);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='invoiceValue']").val(data.bomItemlist[i].totalPrice);
//                     $("#" + transactionTableTr + " table[class='multipleItemsTable'] tbody tr:last input[class='netAmountVal']").val(data.bomItemlist[i].totalPrice);
//                     $("#" + transactionTableTr + " input[class='netAmountValTotal']").val(data.totalNet);
//                 }
//             }
//         },
//         error: function (xhr, status, error) {
//             if(xhr.status == 401){ doLogout();
//             }else if(xhr.status == 500){
//                 swal("Error on fetching transaction detail!", "Please retry, if problem persists contact support team", "error");
//             }
//         },
//         complete: function(data) {
//             $.unblockUI();
//         }
//     });
// }