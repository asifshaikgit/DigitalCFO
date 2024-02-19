var DUPLICATE_ITEMS_ALLOWED_FOR_TXN = 0;
$(document).ready(function() {
	initMultiItemsSelect2();
	initItemSelect2();
});

var initMultiItemsSelect2 = function(){
	$(".txnItems").select2({
		placeholder: "Select an item",
  		allowClear: true,
  		multiple: false
	});
}

var custVendSelect2 = function(){
	$(".masterList").select2({
		placeholder: "--Please Select--",
  		allowClear: true,
  		multiple: false
	});
}

var initItemSelect2 = function(){
	$(".masterListItems").select2({
		placeholder: "Select an item",
  		allowClear: true
	});
}

function enableTransactionButtons(){
	$(".btn-custom").removeAttr("disabled");
	$(".btn-customred").removeAttr("disabled");
	$("input.approverAction[type=button]").removeAttr("disabled");
	$("input.completeTxn[type=button]").removeAttr("disabled");
	$("input.submitForApproval[type=button]").removeAttr("disabled");
	$("input.submitForAccounting[type=button]").removeAttr("disabled");
	$("#approverAction").removeAttr("disabled");
	$(".approverAction btn btn-submit btn-center").removeAttr("disabled");
	$(".submitForApproval btn btn-submit btn-idos btn-custom").removeAttr("disabled");
	$(".btn btn-submit btn-center").removeAttr("disabled");
	$.unblockUI();
}

function disableTransactionButtons(){
	$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$("input.approverAction[type=button]").attr("disabled", "disabled");
	$("input.completeTxn[type=button]").attr("disabled", "disabled");
	$("input.submitForApproval[type=button]").attr("disabled", "disabled");
	$("input.submitForAccounting[type=button]").attr("disabled", "disabled");
	$("#approverAction").attr("disabled", "disabled");
	$(".approverAction btn btn-submit btn-center").attr("disabled", "disabled");
	$(".submitForApproval").attr("disabled", "disabled");
	$(".submitForApproval btn btn-submit btn-idos btn-custom").attr("disabled", "disabled");
	$(".submitForAccounting btn btn-submit btn-custom").attr("disabled", "disabled");
	$(".submitForAccounting").attr("disabled", "disabled");
	$(".btn btn-submit btn-center").attr("disabled", "disabled");

}

function populateSellTranData(argument) {
	clearMultiItemsCurrentTrData(argument);
	if(DUPLICATE_ITEMS_ALLOWED_FOR_TXN == 0){
		var returnValue = validateSelectedItems(argument);
		if(returnValue === false){
			return false;
		}
	}

	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	if(SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeVal || SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeVal){
		isCombinationSalesItemSelected(argument);
	}

	if(SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeVal || SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeVal){
		if (COMPANY_OWNER != "PWC") {
			isTransactionEditable(argument);
		}
	}

	if (GST_COUNTRY_CODE !== "" && GST_COUNTRY_CODE !== undefined && GST_COUNTRY_CODE !== null) {
		returnValue = validateGstItemsForCategory(argument);
		if (returnValue === false) {
			return false;
		}
	}
	if(TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER != txnPurposeVal) {
        returnValue = showTransactionBranchKnowledgeLiabrary(argument);
        if (returnValue === false) {
            return false;
        }
    }
	returnValue = getAdvanceDiscount(argument);
	if(returnValue === false){
		return false;
	}
	returnValue = calculateDiscountSell(argument);
	if(returnValue === false){
		return false;
	}
	returnValue = calculateGross(argument);
	if(returnValue === false){
		return false;
	}
	returnValue = barcodeFetch(argument);
	if(returnValue === false){
		return false;
	}
	returnValue = calculateNetAmountForSell(argument);
	if(returnValue === false){
		return false;
	}
}

function itemChosen(elem){
	if(event.keyCode == 13) {
	var barcode = $(elem).val();
	var itemTr = $(elem).closest('tr').attr('id');
	var txnType = itemTr.substring(0,6);
	var jsonData = {};
	jsonData.txnType=txnType;
	jsonData.barcode = barcode;
	var url="/transaction/barcodeItemFetch";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
	   	headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async: false,
	   	method:"POST",
	   	contentType:'application/json',
	   	success: function (data) {
	   		if(data.itemBarcodeId.length>0){
	   			var barcodeItemNo=data.itemBarcodeId[0].barcodeItemId;
	   			if(barcodeItemNo==null||barcodeItemNo==undefined||barcodeItemNo==""){
	   				if(txnType=="soccpn"){
	   				$("#multipleItemsTablesoccpn > tbody > tr[id='"+itemTr+"'] > td > .txnItems option").removeAttr("selected").trigger('change');
	   				$("#multipleItemsTablesoccpn > tbody > tr[id='"+itemTr+"'] > td > .txnNoOfUnit").val("").trigger('onkeyup');
	   				$("#multipleItemsTablesoccpn > tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val("");
	   				swal("Error!","Barcode doesn't exist with respect to transaction details. Please set it up in COA.","error");
	   				}
	   				if(txnType=="soccpl"){
		   				$("#multipleItemsTablesoccpl > tbody > tr[id='"+itemTr+"'] > td > .txnItems option").removeAttr("selected").trigger('change');
		   				$("#multipleItemsTablesoccpl > tbody > tr[id='"+itemTr+"'] > td > .txnNoOfUnit").val("").trigger('onkeyup');
		   				$("#multipleItemsTablesoccpl > tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val("");
		   				swal("Error!","Barcode doesn't exist with respect to transaction details. Please set it up in COA.","error");
		   				}
	   				if(txnType=="bocpra"){
		   				$("#multipleItemsTablebocpra > tbody > tr[id='"+itemTr+"'] > td > .txnItems option").removeAttr("selected").trigger('change');
		   				$("#multipleItemsTablebocpra > tbody > tr[id='"+itemTr+"'] > td > .txnNoOfUnit").val("").trigger('onkeyup');
		   				$("#multipleItemsTablebocpra > tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val("");
		   				swal("Error!","Barcode doesn't exist with respect to transaction details. Please set it up in COA.","error");
		   				}
	   			}
	   		}
	   		if(txnType=="soccpn"){
	   		$("#multipleItemsTablesoccpn > tbody > tr[id='"+itemTr+"'] > td > .txnItems option[value="+barcodeItemNo+"]").prop("selected","selected").trigger('change');
	   		$("#multipleItemsTablesoccpn > tbody > tr[id='"+itemTr+"'] > td > .txnNoOfUnit").val(1).trigger('onkeyup');
	   		}
	   		if(txnType=="soccpl"){
		   		$("#multipleItemsTablesoccpl > tbody > tr[id='"+itemTr+"'] > td > .txnItems option[value="+barcodeItemNo+"]").prop("selected","selected").trigger('change');
		   		$("#multipleItemsTablesoccpl > tbody > tr[id='"+itemTr+"'] > td > .txnNoOfUnit").val(1).trigger('onkeyup');
		   		}
	   		if(txnType=="bocpra"){
		   		$("#multipleItemsTablebocpra > tbody > tr[id='"+itemTr+"'] > td > .txnItems option[value="+barcodeItemNo+"]").prop("selected","selected").trigger('change');
		   		$("#multipleItemsTablebocpra > tbody > tr[id='"+itemTr+"'] > td > .txnNoOfUnit").val(1).trigger('onkeyup');
		   		}
	   		},
			error: function (xhr, status, error) {
		   		if(xhr.status == 401){ doLogout();
		   		}else if(xhr.status == 500){
		    		swal("Error on Fetching Data!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
}

var barcodeFetch = function (elem){
	var itemId = $(elem).val();
	var itemTr = $(elem).closest('tr').attr('id');
	var txnType = itemTr.substring(0,6);
	var jsonData = {};
	var returnValue = false;
	jsonData.itemId = itemId;
	var url="/transaction/barcodeFetch";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
	   	headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async: false,
	   	method:"POST",
	   	contentType:'application/json',
	   	success: function (data) {
	   		if(data.itemId[0].itemBarcode>0)
   			{
	   			if(txnType=="soccpn"){
	   		$("#multipleItemsTablesoccpn > tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val(data.itemId[0].itemBarcode);
	   			}
	   			if(txnType=="soccpl"){
	   		   		$("#multipleItemsTablesoccpl > tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val(data.itemId[0].itemBarcode);
	   		   			}
	   			if(txnType=="bocpra"){
	   		   		$("#multipleItemsTablebocpra> tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val(data.itemId[0].itemBarcode);
	   		   			}
	   		returnValue = true;
   			}
	   		else{
	   			$("#multipleItemsTablebocpra> tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val("");
	   			$("#multipleItemsTablesoccpn> tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val("");
	   			$("#multipleItemsTablesoccpl> tbody > tr[id='"+itemTr+"'] > td > .txnItemBarcode").val("");
	   		}
	   	},
		error: function (xhr, status, error) {
	   		if(xhr.status == 401){ doLogout();
	   		}else if(xhr.status == 500){
	    		swal("Error on Fetching Data!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
	return returnValue;
}

var isTransactionEditable = function (elem) {
    var multiItemsTable = $(elem).closest('table').attr('id');
    var multiItemsTableTr = $(elem).closest('tr').attr('id');
    var transDiv =  $("#"+multiItemsTable).parent().parent().closest("div").attr('id');
    var transTable = $("#"+transDiv).children('table').attr('id');
    var transTableTr =  $("#"+transTable +" >tbody >tr").attr('id');
	var isEditable = false;
	var trLen = $("#" + multiItemsTable + " > tbody > tr").length;
	$("#" + multiItemsTable + " > tbody > tr").each(function() {
		var isEdit = $(this).find("td .txnItems option:selected").attr('id');
		if(isEdit == 1){
			isEditable = true;
		}
	});

	if(isEditable){
		hideRecieptDetailTD(transDiv, transTable);
	}else{
		displayRecieptDetailTD(transDiv, transTable);
	}
}

var isCombinationSalesItemSelected = function (elem) {
	var multiItemsTable = $(elem).closest('table').attr('id');
	var multiItemsTableTr = $(elem).closest('tr').attr('id');
	var isCombSales = false;
	var combsales = $("#" + multiItemsTableTr + " > td > .txnItems option:selected").attr('combsales');
	if(combsales == 1){
		isCombSales = true;
	}
	var parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
	if(isCombSales){
		var incomeSpecificsId = $("#" + multiItemsTableTr + " > td > .txnItems option:selected").val();
		$("#"+parentOfparentTr+" div[class='combSalesItemDiv'] button[id='combSalesBtn']").remove();
		$("#"+parentOfparentTr+" div[class='combSalesItemDiv']").append('<button title="Combination Sales Item" style="margin-left:10px;height:20px; float:right;" name="combSalesBtn" id="combSalesBtn" type="button" class="combSalesBtn btn btn-submit btn-idos" onclick="showCominationSalesItems(this);">CombSales Items</button>');
		$("#" + multiItemsTableTr + " > td > #soccpnpriceperunits").prop('readonly', true);
	}else{
		//$("#" + transTable +" tbody tr  td:nth-child(2) button[id='combSalesBtn']").remove();
		$("#"+parentOfparentTr+" div[class='combSalesItemDiv'] button[id='combSalesBtn']").remove();
	}
}

function showCominationSalesItems(elem){
	var returnValue = true;
	//var parentTr = $(".multipleItemsTable > tbody > tr:last").attr('id'); //multipleItemsTablesoccpn
	var parentTr = $(elem).parent().parent().closest('div').attr('id');
	var sellOnCashTableTr = $("#"+parentTr+" table[class='table excelFormTable transaction-create'] > tbody > tr ").attr('id');
	var txnTypeOfSupply = $("#"+sellOnCashTableTr+" select[class='txnTypeOfSupply']").val();
	var txnBranch=$("#"+sellOnCashTableTr+" select[class='txnBranches']").val();
	var txnWithWithoutTax = $("#"+sellOnCashTableTr+" select[class='txnWithWithoutTaxCls']").val();
	var sourceGstinCode = $("#"+sellOnCashTableTr+" select[class='txnBranches']").children(":selected").attr("id");
	var destGstinCode = $("#"+sellOnCashTableTr+" select[class='placeOfSply txnDestGstinCls']").val();

	var soccpnLastTableTr = $("#"+parentTr+" div[class='staticsellmultipleitems'] table[class='multipleItemsTable'] > tbody > tr:last ").attr('id');
	var unitPrice = $("#"+soccpnLastTableTr+" input[class='txnPerUnitPrice']").val();
	var noOfUnits = $("#"+soccpnLastTableTr+" input[class='txnNoOfUnit']").val();
	var txnitemSpecifics=$("#"+soccpnLastTableTr+" .txnItems").val();

	if(txnBranch==""){
		swal("Incomplete transaction detail!", "Please select a Branch.", "error");
		returnValue=false;
	}
	if(txnitemSpecifics==""){
		swal("Incomplete transaction detail!", "Please select a Item for transaction.", "error");
		returnValue=false;
	}
	if(txnTypeOfSupply==""){
		swal("Incomplete transaction detail!", "Please select a Type of Supply.", "error");
		returnValue=false;
	}
	if((txnTypeOfSupply =="3" || txnTypeOfSupply =="4" || txnTypeOfSupply =="5") && txnWithWithoutTax==""){
		swal("Incomplete transaction detail!", "Please select With / Without IGST.", "error");
		returnValue=false;
	}
	if(unitPrice === "0" || unitPrice === "" || unitPrice === null || noOfUnits === "0" || noOfUnits === "" || noOfUnits === null){
		swal("Incomplete transaction detail!", "Please enter unit price & no of units.", "error");
		return false;
	}
	 if(returnValue === true){
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.txnBranchId=txnBranch;
		jsonData.txnSpecificsId=txnitemSpecifics;
		jsonData.noOfUnits=noOfUnits;
		jsonData.txnSourceGstinCode = sourceGstinCode;
		jsonData.txnDestGstinCode = destGstinCode;
		jsonData.txnTypeOfSupply = txnTypeOfSupply;
		jsonData.txnWithWithoutTax = txnWithWithoutTax;
		var url="/transaction/showTranCombSalesItemsWithTaxes";
		$.ajax({
			url         : url,
			data        : JSON.stringify(jsonData),
			type        : "text",
			headers:{
						"X-AUTH-TOKEN": window.authToken
			},
			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				$("#staticMutipleCombinationSalesItems").attr('data-toggle', 'modal');
		    	$("#staticMutipleCombinationSalesItems").modal('show');
		    	$(".staticMutipleCombinationSalesItemsclose").attr("href",location.hash);
		    	$("#staticMutipleCombinationSalesItems div[class='modal-body']").html("");
		    	if(data.combSalesListData.length>0){
		    		$("#staticMutipleCombinationSalesItems div[class='modal-body']").html('<div class="datascrolltable" style="height: 100%; overflow: auto;"><table id="multipleCombSalesItemsBreakupTable" class="table table-hover table-striped excelFormTable transaction-create" style="margin-top: 0px; width:100%;">'+
		    		'<thead class="tablehead1"><tr><th>ITEM</th><th>UNIT PRICE</th><th>NO. OF UNITS</th><th>GROSS AMOUNT</th><th>Tax1</th><th>Tax2</th><th>Tax3</th><th>Total</th><tr></thead><tbody></tbody></table></div>');
		    		for(var i=0;i<data.combSalesListData.length;i++){
		    			var grossAmt = data.combSalesListData[i].openBalUnits * data.combSalesListData[i].openingBalRate;
		    			var tax0 = data.combSalesListData[i].individualTax0;
		    			if(typeof tax0 =='undefined'){
		    				tax0=0;
		    			}
		    			var tax1 = data.combSalesListData[i].individualTax1;
		    			if(typeof tax1 =='undefined'){
		    				tax1=0;
		    			}
		    			var tax2 = data.combSalesListData[i].individualTax2;
		    			if(typeof tax2 =='undefined'){
		    				tax2=0;
		    			}
			    		$("#staticMutipleCombinationSalesItems div[class='modal-body'] table[id='multipleCombSalesItemsBreakupTable'] tbody").append('<tr id='+i+'><td style="width=100px;right-margin=5px;">'+data.combSalesListData[i].itemName+'</td><td>'+data.combSalesListData[i].openingBalRate+'</td><td>'+data.combSalesListData[i].openBalUnits+'</td><td>'+data.combSalesListData[i].grossAmt+'</td><td>'+tax0+'</td><td>'+tax1+'</td><td>'+tax2+'</td><td>'+data.combSalesListData[i].totalAmount+'</td></tr>');
			    	}
		    	}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}

var resetMultiItemsTableLength = function(parentTr){
	var initLen = 0;
	var tableTrCount = $("#"+parentTr).find(".multipleItemsTable > tbody > tr").length;
	for (var i = tableTrCount; i > initLen ; i--) {
		$("#"+parentTr).find(".multipleItemsTable > tbody > tr").eq(i).remove();
	}
}

var resetMultiItemsTableFieldsData = function(elem){
	var transactionTableTr = $(elem).closest('tr').attr('id');
	var value=$("#whatYouWantToDo").find('option:selected').val();
    transactionTableTr = $(elem).closest('table').closest('div').attr('id');
    $("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='taxNameList']").html('');
    $("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='taxNameList']").removeAttr('class');
    $("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='taxNameList']").empty();
	if(value == PREPARE_QUOTATION){
		var transactionTable = $(elem).closest('table').attr('id');
		$("#"+transactionTable+" table[name='multiItemsTblHead'] div[id='taxNameList']").html('');
		$("#"+transactionTable+" table[name='multiItemsTblHead'] div[id='taxNameList']").removeAttr('class');
		$("#"+transactionTable+" table[name='multiItemsTblHead'] div[id='taxNameList']").empty();
		$("#"+transactionTable+" table[name='multiItemsTblHead'] div[id='advAdjTaxNameList']").html('');
		$("#"+transactionTable+" table[name='multiItemsTblHead'] div[id='advAdjTaxNameList']").removeAttr('class');
		$("#"+transactionTable+" table[name='multiItemsTblHead'] div[id='advAdjTaxNameList']").empty();
	}
	var txnitemSpecifics= $("#"+transactionTableTr+" table[class='multipleItemsTable'] tbody tr:last .txnItems").val();
	if((txnitemSpecifics != "" && txnitemSpecifics != null) || (value == CREDIT_NOTE_CUSTOMER || value == CREDIT_NOTE_CUSTOMER || value == CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR)){
		$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last span[class='select2-selection__clear']").trigger("mousedown");
        $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last select[class='txnGstTaxRate']").children().remove();
        $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last select[class='txnCessRate']").children().remove();

        $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last select[class='txnRcmTaxItem']").children().remove();
        $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDutiesAndTaxes']").val("");
        $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDutiesAndTaxesHid']").val("");
        $("#" + transactionTableTr ).find(".rcmCompToshowGood").hide();
	}
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnPerUnitPrice']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDiscountPercent']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDiscountPercentHid']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDiscountAmount']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnGross']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnTaxTypes']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='customerAdvance']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='howMuchAdvance']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='advAvailForRefund']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='tdsAvailForRefund']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='advanceReceived']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[name='mkrfndadvResultantAdv']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[name='mkrfndadvResultantTax']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='taxAdjusted']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='netAmountVal']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnTaxAmount']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='taxRate']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='invoiceValue']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnTaxNameOnAdvAdjCls']").val("");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnTaxOnAdvAdjCls']").val("");

	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell0']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell1']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell2']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell3']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell4']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell0']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell1']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell2']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell3']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell4']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell0']").css('display: none;');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell1']").css('display: none;');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell2']").css('display: none;');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell3']").css('display: none;');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell4']").css('display: none;');

	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell0']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell1']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell2']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell3']").empty();
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell4']").empty();

	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell0']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell1']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell2']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell3']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='advAdjTaxCell4']").removeAttr('class');
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last select[class='txnGstTaxRate']").find('option:first').prop("selected","selected");
	$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last select[class='txnCessRate']").find('option:first').prop("selected","selected");
    $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnCessTaxAmt']").val("");
    $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnGstTaxRate']").val("");
}

var clearMultiItemsCurrentTrData = function(elem){
	var multipleItemsTableTr = $(elem).closest('tr').attr('id');
	//$("#" + multipleItemsTableTr +" span[class='select2-selection__clear']").trigger("mousedown");
	$("#" + multipleItemsTableTr +" input[class='txnPerUnitPrice']").val("");
		$("#" + multipleItemsTableTr +" input[class='txnPerUnitPriceTaxInclusice']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnNoOfUnit']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnDiscountPercent']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnDiscountPercentHid']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnDiscountAmount']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnGross']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnTaxTypes']").val("");
	$("#" + multipleItemsTableTr +" input[class='customerAdvance']").val("");
	$("#" + multipleItemsTableTr +" input[class='howMuchAdvance']").val("");
	$("#" + multipleItemsTableTr +" input[class='netAmountVal']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnTaxAmount']").val("");
	$("#" + multipleItemsTableTr +" input[class='taxRate']").val("");
	$("#" + multipleItemsTableTr +" input[class='invoiceValue']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnTaxNameOnAdvAdjCls']").val("");
	$("#" + multipleItemsTableTr +" input[class='txnTaxOnAdvAdjCls']").val("");

	$("#" + multipleItemsTableTr +" div[id='taxCell0']").html('');
	$("#" + multipleItemsTableTr +" div[id='taxCell1']").html('');
	$("#" + multipleItemsTableTr +" div[id='taxCell2']").html('');
	$("#" + multipleItemsTableTr +" div[id='taxCell3']").html('');
	$("#" + multipleItemsTableTr +" div[id='taxCell4']").html('');
	//$("table[class='multipleItemsTable'] thead tr div[id='taxNameList']").html('');
	$("#" + multipleItemsTableTr +" div[id='advAdjTaxCell0']").html('');
	$("#" + multipleItemsTableTr +" div[id='advAdjTaxCell1']").html('');
	$("#" + multipleItemsTableTr +" div[id='advAdjTaxCell2']").html('');
	$("#" + multipleItemsTableTr +" div[id='advAdjTaxCell3']").html('');
	$("#" + multipleItemsTableTr +" div[id='advAdjTaxCell4']").html('');
	//$("table[class='multipleItemsTable'] div[id='advAdjTaxNameList']").empty();
	$("#" + multipleItemsTableTr +" select[class='txnGstTaxRate']").find('option:first').prop("selected","selected");
	$("#" + multipleItemsTableTr +" select[class='txnCessRate']").find('option:first').prop("selected","selected");
	$("#" + multipleItemsTableTr +" input[class='txnCessTaxAmt']").val('');
	$("#" + multipleItemsTableTr +" input[class='itemTaxAmount']").val('');
	$("#" + multipleItemsTableTr +" input[class='withholdingtaxcomponenetdiv']").val('');
}

var resetMainTransTableFields = function(transactionTableTr){
	$("#" + transactionTableTr +" input[class='placeOfSplyText']").val("");
	$("#" + transactionTableTr +" input[class='placeOfSplyTextHid']").val("");
	$("#" + transactionTableTr +" input[class='unavailable']").val("");
	$("#" + transactionTableTr +" select[name='txnPlaceOfSply']").find('option:first').prop("selected","selected");
	$("#" + transactionTableTr +" select[name='txnWalkinPlcSplySelect']").find('option:first').prop("selected","selected");
	$("#" + transactionTableTr +" select[class='txnTypeOfSupply']").find('option:first').prop("selected","selected");
	$("#" + transactionTableTr +" select[class='placeOfSply txnDestGstinCls']").find('option:first').prop("selected","selected");
	$("#staticWalkinCustomerModal input[id='isGstinAddedInTransHid']").val(0);
	$("#staticWalkinVendorModal input[id='isGstinAddedInTransHid']").val(0);
	$("#" + transactionTableTr +" div[class='txnWalkinDivCls']").hide();
	$("#" + transactionTableTr +" div[class='regCustomerDivCls']").show();
	$("#" + transactionTableTr +" input[class='isWalkinCustomerCls']").prop('checked', false);
	$("#" + transactionTableTr +" p[class='isWalkinCustPara']").hide();
}

var displayRecieptDetailTD = function(transTableTr, transTable){
	$("#" + transTableTr + " button[class='submitForApproval btn btn-submit btn-custom']").hide();
	$("#" + transTableTr + " button[class='submitForAccounting btn btn-submit btn-custom']").show();
	/*if(transTable == "sellOnCashTable"){
		$("#" + transTable + "  tbody tr  td:nth-child(5)").show();
		$("#" + transTable + "  thead tr  th:nth-child(5)").show();
	}*/
}

var hideRecieptDetailTD = function(transTableTr, transTable){
	$("#" + transTableTr + " button[class='submitForApproval btn btn-submit btn-custom']").show();
	$("#" + transTableTr + " button[class='submitForAccounting btn btn-submit btn-custom']").hide();
	/*if(transTable == "sellOnCashTable"){
		$("#" + transTable + "  tbody tr  td:nth-child(5)").hide();
		$("#" + transTable + "  thead tr  th:nth-child(5)").hide();
	}*/
}

function whatYouWantToDoFun(elem){
	console.log("whatYouWantToDoFun");
	GST_COUNTRY_CODE = $("#gstCountryCode").val();
	enableTransactionButtons();
	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	DUPLICATE_ITEMS_ALLOWED_FOR_TXN= $("#duplicateItemsAllowed").html();
	$(".specAdjustReceivPayAmount").val("");
	$(".openWhatDoINeedToDo").show();
	$(".whatDoINeedToDoContent").html("");
	$(".whatDoINeedToDoContent").hide();
	var parentTr=$(elem).parent().parent().parent().parent().parent().parent().parent().parent().parent().attr('id'); //Changed by Sunil
	var parentTr2=$(elem).parent().parent().parent().parent().parent();
	$("div[id='moreSupportingDocDiv']").html("");
	$("#rcpfccvendcustoutstandingsgross").text("");
	$("#rcpfccvendcustoutstandingsnet").text("");
	$("#rcpfccvendcustoutstandingsnetdescription").text("");
	$("#rcpfccvendcustoutstandingspaid").text("");
	$("#rcpfccvendcustoutstandingsnotpaid").text("");
	$("#mcpfcvvendcustoutstandingsgross").text("");
	$("#mcpfcvvendcustoutstandingsnet").text("");
	$("#mcpfcvvendcustoutstandingsnetdescription").text("");
	$("#mcpfcvvendcustoutstandingspaid").text("");
	$("#rcpfccvendcustoutstandingssalesreturn").text("");
	$("#mcpfcvvendcustoutstandingspurchasereturn").text("");
	$("#mcpfcvvendcustoutstandingsnotpaid").text("");
	$("#mcpfcvtxninprogress").text("");
	$('#createExpense input[type="text"]').val("");
	$('#createExpense textarea').val("");
	$(".dynmBnchBankActList").remove();
	$("#socpnreceiptdetail").find('option:first').prop("selected","selected");
	$("#rcpfccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rcafccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rsaafvpaymentdetail").find('option:first').prop("selected","selected");
	$("#paymentDetails").find('option:first').prop("selected","selected");
	$(".klBranchSpecfTd").text("");
	$(".itemParentNameDiv").text("");
	$(".combSalesItemDiv").text("");
	$(".inventoryItemInStock").text("");
	$(".customerVendorExistingAdvance").text("");
	$(".resultantAdvance").text("");
	$("#bocaplunits").removeAttr("readonly");
	$("#bocpraunits").removeAttr("readonly");
	$(".discountavailable").text("");
	$("#procurementRequestRemarks").text("");
	$(".netAmountDescriptionDisplay").text("");
	$(".netAmountVal").val("");
	$(".budgetDisplay").text("");
	$(".actualbudgetDisplay").text("");
	$(".branchAvailablePettyCash").html("");
	//$(".inputtaxbuttondiv").html("");
	$(".inputtaxcomponentsdiv").html("");
	$(".vendorActPayment").text("");
	$(".withholdingtaxcomponentdiv").text("");
	$(".individualtaxdiv").text("");
	$(".individualtaxformuladiv").text("");
	$("#rcpfccvendcustoutstandingsgross").text("");
	$("#rcpfccvendcustoutstandingsnet").text("");
	$("#procurementRequestRemarks").text("");
	$("input[class='invValue']").val("");
	$("div[class='returnDetails']").html("");
	$(".balanceDueReturns").text("");
	$("div[class='maxReturnAmount']").html("");
	$("select[class='pendingTxns'] option:first").prop("selected","selected");
	$("#fromBranchBankAccounts").children().remove();
	$("#fromBranchBankAccounts").append('<option value="">--Please Select--</option>');
	$("#toBranchBankAccounts").children().remove();
	$("#toBranchBankAccounts").append('<option value="">--Please Select--</option>');
	$(".txnBranchBanks").children().remove();
	$(".txnBranchBanks").append('<option value="">--Please Select--</option>');
	$(".txnBranches").children().remove();
	$(".txnBranches").append('<option value="">--Please Select--</option>');
	$(".txnUploadSuppDocs").children().remove();
	$(".txnUploadSuppDocs").append('<option value="">Select a file</option>');
	$(".txnItems").children().remove();
	//$(".txnItems").append('<option value="">--Please Select--</option>');
	$(".klBranchCashierTd").text("");
	$(".txnFromBranchBankDetails").html("");
	$(".txnToBranchBankDetails").text("");
	$(".txnBranchBankDetails").html("");
    $("input[class='customerAdvance']").removeAttr("disabled");
    $("input[class='howMuchAdvance']").removeAttr("disabled");
	//$(".txnNoOfUnit").val("1");
	$("#mtefpetrid button[id='debitAccountHeadsdropdown']").html("None Selected &nbsp;&nbsp;<span class='caret'></span>");
	$("#mtefpetrid button[id='creditAccountHeadsdropdown']").html("None Selected &nbsp;&nbsp;<span class='caret'></span>");
	var value=$(elem).val();
	var text=$(elem).find('option:selected').text();
	$('.transactionDetailsTable:visible').slideUp();
	//$("#"+parentTr+" table[class='multipleItemsTable'] tbody tr:first").remove();
	$("#txnDocRefNo").val("");
	$("#isBackDatedTxn").val("false");
	$("#backDateDiv").hide();
    $("#purchaseOrderCategoryDivId").hide();
	$("#purchaseRequisitionCategoryDivId").hide();

	if(SELL_ON_CASH_COLLECT_PAYMENT_NOW == value){
		displayRecieptDetailTD("soccpntrid", "sellOnCashTable");
		resetMultiItemsTableLength('transactionDetailsSOCCPNTable');
		resetMainTransTableFields('soccpntrid');
		resetMultiItemsTableFieldsData('#soccpntrid');
		//$("#soccpntrid input[class='isWalkinCustomerCls']").prop('checked', false);
		//$(".txnWalkinDivCls").hide();
		//$(".regCustomerDivCls").show();
		$('#transWhatToDo span').html("In the first column select the branch where you are making the sale. Second column select the item being sold (System will display only those items, goods and services which are permitted to be sold at the branch selected in the first column. In case the item you wish to sell is not being displayed in the drop down list of the second column, then contact your Manager or administrator in your office to get the issue resolved). In the third column, select from dropdown list the customer to whom you wish to make this sale. The dropdown will only display names of those customers who are in your customer list and accounts receivables list which is entered into system by your Manager or Administrator in your office. If the name of the customer is not displayed in the dropdown list, enter the customer name in the field below. In case you select customer from dropdown list, then discount which is pre-approved will automatically be taken and applied for the sale. However if you have a new customer whose name is not in the dropdown list, then you can input the discount you wish to offer to this customer and system will compute and apply discount on this sale. In the fourth column (Knowledge Library) standing instructions (if any) given by your Manager will be displayed and you will need to read the instruction and select YES / NO with respect to whether you have followed the instruction or not. If you select NO, you can type the explanation in the last column (Input remarks). If you do not select and click on either YES / NO, you will not be able to complete processing this sale. In the fifth column, input the number of items beng sold (for sale of services system automatically selects 1). If the price is pre-approved and fixed by the Manager or by your office, system will automatically display and apply that price. If the price is not pre-approved or pre-fixed, then you can input the price per unit and then the gross sales amount will be automatically computed. In the next column \"Adjustments\" system will display advance (if any) received from the customer to whom you are making this sale and if you wish to adjust the sale price against this advance, you can do so either in part or in full. In the next column, just click on \"Display Amount\" and system will compute the net sale price after adjusting discount (if any), advance (if any) and also after automatically computing and applying sales taxes applicable for this sale. In the eighth column, select the the mode of payment received from the customer, if customer pays by cash then select cash, else select the appropriate mode from the dropdown list and also input the reference details like check number, wire transfer reference, etc in the \"Input Receipt Details\" field. In the last column input the remarks, notes and any other observation regarding this transaction for future reference and also upload supporting documents (if any) regarding this sale.  Lastly click on \"Submit for Accounting\" to complete processing this transaction.");
		$("#transactionDetailsSOCCPNTable").slideDown('slow');
		$("#transactionDetailsSOCCPNTable th[class='paymentDetailsLabel']").show();
		$("#"+parentTr+" td[class='paymentDetails']").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
		setBackDatedDateSelecter('transactionDetailsSOCCPNTable');
		singleUserMiscChanges("soccpntrid");
	} else if(SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == value){
		resetMultiItemsTableLength('transactionDetailsSOCCPLTable');
		resetMainTransTableFields('soccpltrid');
		resetMultiItemsTableFieldsData('#soccpltrid');
		$('#transWhatToDo span').html("In the first column select the branch where you are making the sale. Second column select the item being sold (system will display only those items, goods and services which are permitted to be sold at the branch selected in the first column. In case the item you wish to sell is not being displayed in the drop down list of the second column, then contact your Manager or system administrator in your office to get the issue resolved). In the third column, select from dropdown list the customer to whom you wish to make this sale. The dropdown will only display names of those customers who are in your customer list and accounts receivables list which is entered into system by your Manager or system Administrator in your office. system allows you to make credit sales only to registered customers (customers who are added to the customer list in system and you will not be able to make credit sales to walk-in customers). Once you select customer from dropdown list, then discount which is pre-approved will automatically be taken and applied for the sale. In the fourth column (Knowledge Library) standing instructions (if any) given by your Manager will be displayed and you will need to read the instruction and select YES / NO with respect to whether you have followed the instruction or not. If you select NO, you can type the explanation in the last column (Input remarks). If you do not select and click on either YES / NO, you will not be able to complete processing this sale. In the fifth column, input the number of items beng sold (for sale of services system automatically selects 1). If the price is pre-approved and fixed by the Manager or by your office, system will automatically display and apply that price. If the price is not pre-approved or pre-fixed, then you can input the price per unit and then the gross sales amount will be automatically computed. In the next column \"Adjustments\" system will display advance (if any) received from the customer to whom you are making this sale and if you wish to adjust the sale price against this advance, you can do so either in part or in full. In the next column, just click on \"Display Amount\" and system will compute the net sale price after adjusting discount (if any), advance (if any) and also after automatically computing and applying sales taxes applicable for this sale. In the last column input the remarks, notes and any other observation regarding this transaction for future reference and also upload supporting documents (if any) regarding this sale. Lastly click on \"Submit for Accounting\" to complete processing this transaction.");
		$("#transactionDetailsSOCCPLTable").slideDown('slow');
		$("#transactionDetailsSOCCPLTable th[class='paymentDetailsLabel']").hide();
		$("#"+parentTr+" td[class='paymentDetails']").hide();
		populateItemBasedOnWhatYouWantToDo(value,text);
		setBackDatedDateSelecter('transactionDetailsSOCCPLTable');
		singleUserMiscChanges("soccpltrid");
	} else if(BUY_ON_CASH_PAY_RIGHT_AWAY == value || value == BUY_ON_CREDIT_PAY_LATER || value == BUY_ON_PETTY_CASH_ACCOUNT){
		parentTr = "transactionDetailsBOCPRATable";
		resetMultiItemsTableLength('transactionDetailsBOCPRATable');
		resetMainTransTableFields('bocpratrid');
		resetMultiItemsTableFieldsData('#bocpratrid');
		$('#transWhatToDo span').html("Input/select the information in the columns below and click on \"Submit for Approval\". In the first cloumn, first field, select the branch at which you want to buy. If you wish to allocate the purchase to a specific project, then select the project to which you wish to allocate this expense, if you do not wish to allocate to any branch, then ignore this field. Second column, select from the dropdown list, the item you want to buy. Remember that system will display only those items, which you are permitted to buy on cash at this branch. If the item you want to buy is not listed in the dropdown, then you have to inform your Manager or system administrator in your organisation to add that item to the list of items you are permitted to buy at the branch. Once you select what you want to buy, system will display the monthly budget and the monetary limit set for you. You can buy only if the price is within the monetary limit set by your organisation. Next column, either select the vendor you wish to buy from or if the vendor you wish to buy from is not listed in your organisation vendor list, input the vendor name in the field below. In the next column ʺKnowledge Libraryʺ system will display if there is any standing instruction from your boss with respect to this transaction. You need to read the instruction and select ʺYesʺ if you have followed the rule or standing instruction. If for any reason you cant follow the standing instruction, select ʺNoʺ and continue with the transaction. In the next column, if you are buying any service, let the number of units field be 1. If you are buying any goods or tangible products, then input the number of items / units you are buying and the price per unit, system will automatically compute the gross price. If you do not have price per unit information, then input 1 in the first field and total purchase price in the ʺprice per unitʺ field. The next column allows you to adjust advance (if any) to registered vendor. If there is an advance paid to this vendor system will automatically display the advance and you can choose to adjust the advance in full or in part. In the next column, click on ʺDisplay Amountʺ and system will display the amount payable to the vendor for this purchase. In the last column, input remarks or observations, upload supporting documents, bills, quotations or other documents as instructed by your company and click on ʺSubmit for Approvalʺ. The transaction instantly gets transmitted to your Manager for approval and a status column for this transaction will display ʺRequire approvalʺ, once it is approved, you will see that that the status is displayed as ʺApprovedʺ and now you will have a column where you have to select if you are making the payment by cash, cheque, draft or credit card by selecting the option in the dropdown box. If you are making the payment by cheque or draft or wire transfer, select from which bank account you are making the payment and input the reference details in the field below. You can even make notes in the ʺTransaction notesʺ column. Finally click on ʺComplete accountingʺ to complete processing this transaction and system will automatically post the transaction in the ledgers. You can see the youtube video by clicking on this link to view how transaction is created - <a href=\"https://www.youtube.com/watch?v=sC1WXUBzUtw\" target=\"_blank\">Demo</a>");
		$("#transactionDetailsBOCPRATable").slideDown('slow');
		populateItemBasedOnWhatYouWantToDo(value,text);
		setBackDatedDateSelecter('transactionDetailsBOCPRATable');
		$("#"+parentTr+ " select[class='txnTypeOfSupply']").children().remove();
		if(value == BUY_ON_PETTY_CASH_ACCOUNT){
			$(".submitForAccounting").removeAttr("disabled");
		}
		if(value == BUY_ON_CASH_PAY_RIGHT_AWAY || value == BUY_ON_CREDIT_PAY_LATER){
			if((COMPANY_OWNER == COMPANY_PWC || isSingleUserDeploy == "true")  && value == BUY_ON_CASH_PAY_RIGHT_AWAY) {
				$("#transactionDetailsBOCPRATable th[id='bocprawPaymentDetailsLbl']").addClass('paymentDetailsLabel');
				$("#" + parentTr + " td[id='bocprawPaymentDetails']").addClass('paymentDetails');
			} else {
				$("#transactionDetailsBOCPRATable th[id='bocprawPaymentDetailsLbl']").removeClass('paymentDetailsLabel');
				$("#" + parentTr + " td[id='bocprawPaymentDetails']").removeClass('paymentDetails');
			}
			$("#bocprasubmitForApproval").show();
			$("#bocprasubmitForAccounting").hide();
			$("#"+parentTr+ " select[class='txnTypeOfSupply']").append('<option value="">--Please Select--</option><option value="1">Regular</option><option value="2">Supply on Reverse Charge - Unregistered Vendor</option><option value="3">Supply attracting tax on reverse charge - registered vendor</option><option value="4">Overseas / SEZ Import Goods - Supply</option><option value="5">Overseas / SEZ Import Services - Supply</option>');
		} else {
			$("#transactionDetailsBOCPRATable th[id='bocprawPaymentDetailsLbl']").removeClass('paymentDetailsLabel');
			$("#" + parentTr + " td[id='bocprawPaymentDetails']").removeClass('paymentDetails');
			$("#bocprasubmitForApproval").hide();
			$("#bocprasubmitForAccounting").show();
			$("#"+parentTr+ " select[class='txnTypeOfSupply']").append('<option value="">--Please Select--</option><option value="1">Regular</option><option value="2">Supply on Reverse Charge - Unregistered Vendor</option>');
			$("#transactionDetailsBOCPRATable tbody tr[id='bptycatrid'] td:nth-child(5) input[name='vendorInvoiceDate']").datepicker({
				changeMonth : true,
				changeYear : true,
				dateFormat:  'MM d,yy',
				yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
				onSelect: function(x,y){
			        $(this).focus();
			    }
			});
		}

		//populating the PO dropdown if buy on credit selected
		if(value == BUY_ON_CREDIT_PAY_LATER){ 
			$("#BuyTransactionPODropdown").show();
			$("#BuyTransactionPODropdownHeading").show();
		} else if(value == BUY_ON_CASH_PAY_RIGHT_AWAY) {
			$("#BuyTransactionPODropdown").hide();
			$("#BuyTransactionPODropdownHeading").hide();
		}
		singleUserMiscChanges("bocpratrid");
	} else if(text=="Buy on Petty Cash Account" || value=="11"){
		resetMultiItemsTableLength('bptycatrid');
		$('#transWhatToDo span').html("Use the option of \"Buy on Petty Cash Account\" when you wish to make a purchase without the need to submit the transaction for approval. Depending on which items are pre-approved for Petty Cash Purchase in your office, you can buy using\"Buy on Petty Cash Account\" and directly submit for accounting without the need to submit for approval. In column one select the branch where you are making the purchase and if you wish to allocate to a particular project of that branch, then select the project in the second field. Second column select what you wish to buy using petty cash (system displays only those items approved by your Manager for petty cash purchase). Third Column select the vendor you wish to buy from and if you are buying from your pre-approved vendor or a vendor who forms part of the vendor list created by your company in system, then such vendor will be displayed when you click on the dropdown list. If you are buying from a new vendor or a vendor who is not pre-approved in your company, then input that vendor name in the field below the dropdown list. In the fourth column (Knowledge Library) standing instructions (if any) given by your Manager will be displayed and you will need to read the instruction and select YES / NO with respect to whether you have followed the instruction or not. If you select NO, you can type the explanation in the last column (Input remarks). If you do not select and click on either YES / NO, you will not be able to complete processing this petty cash purchase. In the fifth column, input the number of items beng purchased (for purchase of services system automatically selects 1). In the next column \"Adjustments\" system will display advance (if any) paid to the vendor from whom you are making this purchase and if you wish to adjust the purchase price against this advance, you can do so either in part or in full. In the next column, just click on \"Display Amount\" and system will compute the net purchase price after adjusting advance (if any). In the column \"Payment details\" select the mode of payment for this petty cash purchse and in the field \"Input payment details\" provide the details of mode of payment if paid by check or wire transfer or credit card or debit card. In the last column input the remarks, notes and any other observation regarding this transaction for future reference and also upload supporting documents (if any) regarding this purchase. Lastly click on \"Submit for Accounting\" to complete processing this transaction.");
		$("#transactionDetailsBPTYCATable").slideDown('slow');
		$("#transactionDetailsBPTYCATable th[class='paymentDetailsLabel']").hide();
		$("#"+parentTr+" td[class='paymentDetails']").hide();

		$("#transactionDetailsBPTYCATable tbody tr[id='bptycatrid'] td:nth-child(5) input[name='vendorInvoiceDate']").datepicker({
			changeMonth : true,
			changeYear : true,
			dateFormat:  'MM d,yy',
			yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
			onSelect: function(x,y){
		        $(this).focus();
		    }
		});
		populateItemBasedOnWhatYouWantToDo(value,text);
		singleUserMiscChanges("bptycatrid");
	} else if(text=="Buy on credit & pay later" || value=="4"){
		resetMultiItemsTableLength('bocapltrid');
		$('#transWhatToDo span').html("Input / select the information in the columns below and click on \"Submit for Approval\". In the first cloumn, first field, select the branch at which you want to buy. If you wish to allocate the purchase to a specific project, then select the project to which you wish to allocate this expense, if you do not wish to allocate to any branch, then ignore this field. Second column, select from the dropdown list, the item you want to buy. Remember that system will display only those items, which you are permitted to buy on cash at this branch. If the item you want to buy is not listed in the dropdown, then you have to inform your Manager or system administrator in your organisation to add that item to the list of items you are permitted to buy at the branch. Once you select what you want to buy, system will display the monthly budget and the monetary limit set for you. You can buy only if the price is within the monetary limit set by your organisation. Next column, either select the vendor you wish to buy from. Please note that system permits credit purchases only from pre-approved vendors who are listed by your company in the vendor list and authorised as vendors for the selected branch. If you do not see the name of vendor in the dropdown list, inform your boss and system administrator in your organisation. In the next column \"Knowledge Library\" system will display if there is any standing instruction from your boss with respect to this transaction. You need to read the instruction and select \"Yes\" if you have followed the rule or standing instruction. If for any reason you cant follow the standing instruction, select \"No\" and continue with the transaction. In the next column, if you are buying any service, let the number of units field be 1. If you are buying any goods or tangible products, then input the number of items / units you are buying and system will automatically compute the price, if the price is pre-approved by your organisation. If the price does not automatically appear, then input the per unit price. If per unit price is not available, then input 1 in the number of units field and input total purchase price in price per unit field. The next column allows you to adjust advance (if any) to registered vendor. If there is an advance paid to this vendor system will automatically display the advance and you can choose to adjust the advance in full or in part. In the next column, click on \"Display Amount\" and system will display the amount payable to the vendor for this purchase. In the last column, input remarks or observations, upload supporting documents, bills, quotations or other documents as instructed by your company and click on \"Submit for Approval\". The transaction instantly gets transmitted to your Manager for approval and a status column for this transaction will display \"Require approval\", once it is approved, you will see that that the status is displayed as \"Approved\" and now you will have a column where you have to select if you are making the payment by cash, cheque, draft or credit card by selecting the option in the dropdown box. If you are making the payment by cheque or draft or wire transfer, select from which bank account you are making the payment and input the reference details in the field below. You can even make notes in the \"Transaction notes\" column. Finally click on \"Complete accounting\" to complete processing this transaction and system will automatically post the transaction in the ledgers. You can see the youtube video by clicking on this link to view how transaction is created - <a href=\"https://www.youtube.com/watch?v=sC1WXUBzUtw\" target=\"_blank\">Demo</a>");
		$("#transactionDetailsBOCAPLTable").slideDown('slow');
		$("#transactionDetailsBOCAPLTable th[class='paymentDetailsLabel']").hide();
		$("#"+parentTr+" td[class='paymentDetails']").hide();
		populateItemBasedOnWhatYouWantToDo(value,text);
		singleUserMiscChanges("bocapltrid");
	} else if(text=="Receive payment from customer" || value=="5"){
		resetMultiItemsTableLength('transactionDetailsRCPFCCTable');
		$('#transWhatToDo span').html("system has a unique process for recording payments from customers (Accounts receivables). system captures data in different columns to help avoid / reduce reconciliation effort on Accounts Receivabes. In the first column select the customer from whom you have received the payment. The drop down list will display the names of only those customers who owe money to your business. In the second column select the invoice against which you are receiving the payment. Once you select the customer from whom you are receiving the amount (in the first column), system will automaticallly display in the second column dropdown list, only those invoices on which payment is pending for the customer selected in column one.  If the amount you have received relates to an invoice of previous year and it is part of the opening balance, then select \"Opening Balance\" in the dropdown list. If you have received the payment towards two or more invoices, then you need to first adjust the amount for one invoice, submit the transaction for accounting and create another transaction to adjust the amount against the next invoice. You can adjust the amount against each invoice in full or in part and system will automatically compute and store information regarding balance due against each invoice. Third column displays information regarding how much is due against the invlice selected in column two and you dont need to enter anything in the third column. In the fourth column, system will display if any advance has been received from this customer and you can (if needed / optionally) discuss with your customer and adjust part or full amount of the advance. In the fifth column you must input how much money you have received from the customer for the invoice selected in column two (in case customer has paid for two or more invoices, then you need to talk to the customer or check documents received from the customer and adjust the specified amount for each invoice. Further, if the customer has deducted or withheld any tax from the payment, input the tax withheld in the second field of the fifth column.). In the sixth column, system will automatically display balance amount due (if any) after adjusting the payment received from customer. In the seventh column, select the mode of payment received from the customer and input the receipt details. In the last column upload any documents supporting this transaction and input remarks for future reference. Lastly click on \"Submit for Accounting\" to complete this transaction.");
		$("#transactionDetailsRCPFCCTable").slideDown('slow');
		$("#transactionDetailsRCPFCCTable th[class='paymentDetailsLabel']").show();
		$("#"+parentTr+" td[class='paymentDetails']").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
		//populatecreditcustomer(value,text,parentTr);
		setBackDatedDateSelecter('transactionDetailsRCPFCCTable');
		singleUserMiscChanges("rcpfcctrid");
	} else if(value== RECEIVE_ADVANCE_FROM_CUSTOMER){
        resetMultiItemsTableLength('transactionDetailsRCAFCCTable');
        resetMainTransTableFields('rcafcctrid');
        resetMultiItemsTableFieldsData('#rcafcctrid');
		$('#transWhatToDo span').html("Please select or input the details in the columns below, After that input the receipt details, input remarks, upload documents to support this transaction and finally click on the button 'Submit for accounting...' to complete processing this transaction. At anytime before you submit, click on button on the left side of this table to cancel the transaction.");
		$("#transactionDetailsRCAFCCTable").slideDown('slow');
		$("#transactionDetailsRCAFCCTable th[class='paymentDetailsLabel']").show();
		$("#"+parentTr+" td[class='paymentDetails']").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
		singleUserMiscChanges("rcafcctrid");
		//populatecreditcustomer(value,text,parentTr);
	} else if(value == PAY_VENDOR_SUPPLIER){
		$('#transWhatToDo span').html("Please select or input the details in the columns below, After that input the receipt details, input remarks, upload documents to support this transaction and finally click on the button 'Submit for approval...' to complete processing this transaction. At anytime before you submit, click on button on the left side of this table to cancel the transaction.");
		$("#"+parentTr+" .masterListItems").children().remove();
		$("#"+parentTr+" .masterListItems").append('<option value="">--Please Select--</option>');
		$("#transactionDetailsMCPFCVTable").slideDown('slow');
		if(COMPANY_OWNER == COMPANY_PWC || isSingleUserDeploy == "true") {
			$("#transactionDetailsMCPFCVTable th[class='paymentDetails']").show();
			$("#" + parentTr + " td[class='paymentDetails']").show();
		}else{
			$("#transactionDetailsMCPFCVTable th[class='paymentDetails']").hide();
			$("#" + parentTr + " td[class='paymentDetails']").hide();
		}
		populateItemBasedOnWhatYouWantToDo(value,text);
		//populatecreditcustomer(value,text,parentTr);
		setBackDatedDateSelecter('transactionDetailsMCPFCVTable');
		singleUserMiscChanges("mcpfcvtrid");
	} else if(value == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
        resetMultiItemsTableLength('transactionDetailsPCAFCVTable');
        resetMainTransTableFields('pcafcvtrid');
        resetMultiItemsTableFieldsData('#pcafcvtrid');
		$('#transWhatToDo span').html("Please select or input the details in the columns below, After that input the receipt details, input remarks, upload documents to support this transaction and finally click on the button 'Submit for approval...' to complete processing this transaction. At anytime before you submit, click on button on the left side of this table to cancel the transaction.");
		$("#transactionDetailsPCAFCVTable").slideDown('slow');
		if((COMPANY_OWNER == COMPANY_PWC || isSingleUserDeploy == "true")) {
			$("#transactionDetailsPCAFCVTable th[class='paymentDetails']").show();
			$("#"+parentTr+" td[class='paymentDetails']").show();
		}else{
			$("#transactionDetailsPCAFCVTable th[class='paymentDetails']").hide();
			$("#"+parentTr+" td[class='paymentDetails']").hide();
		}
		populateItemBasedOnWhatYouWantToDo(value,text);
		setBackDatedDateSelecter('transactionDetailsPCAFCVTable');
		singleUserMiscChanges("pcafcvtrid");
	} /*else if(text=="Receive special adjustments amount from vendors" || value=="9"){
		$('#transWhatToDo span').html("Please select or input the details in the columns below, After that input the receipt details, input remarks, upload documents to support this transaction and finally click on the button 'Submit for accounting...' to complete processing this transaction. At anytime before you submit, click on button on the left side of this table to cancel the transaction.");
		$("#"+parentTr+" .masterListItems").children().remove();
		$("#"+parentTr+" .masterListItems").append('<option value="">--Please Select--</option>');
		$("#transactionDetailsRSAAFVTable").slideDown('slow');
		$("#transactionDetailsRSAAFVTable th[class='paymentDetailsLabel']").show();
		$("#"+parentTr+" td[class='paymentDetails']").show();
		populateallowedspecialadjustmentvendorsprojects(value,text,parentTr);
	} else if(text=="Pay special adjustments amount to vendors" || value=="10"){
		$('#transWhatToDo span').html("Please select or input the details in the columns below, After that input the receipt details, input remarks, upload documents to support this transaction and finally click on the button 'Submit for approval...' to complete processing this transaction. At anytime before you submit, click on button on the left side of this table to cancel the transaction.");
		$("#"+parentTr+" .masterListItems").children().remove();
		$("#"+parentTr+" .masterListItems").append('<option value="">--Please Select--</option>');
		$("#transactionDetailsPSAATVTable").slideDown('slow');
		$("#transactionDetailsPSAATVTable th[class='paymentDetailsLabel']").hide();
		$("#"+parentTr+" td[class='paymentDetails']").hide();
		populateallowedspecialadjustmentvendorsprojects(value,text,parentTr);
	}*/
	else if(text=="Sales returns" || value=="12"){
		resetMultiItemsTableLength('srtfcctrid');
		$('#transWhatToDo span').html("Please select or input the details in the columns below, After that input the receipt details, input remarks, upload documents to support this transaction and finally click on the button 'Submit for approval...' to complete processing this transaction. At anytime before you submit, click on button on the left side of this table to cancel the transaction.");
		$("#"+parentTr+" .masterListItems").children().remove();
		$("#"+parentTr+" .masterListItems").append('<option value="">--Please Select--</option>');
		$("#"+parentTr+" select[class='salesExpenseTxns']").children().remove();
		$("#"+parentTr+" select[class='salesExpenseTxns']").append('<option value="">--Please Select--</option>');
		$("#transactionDetailsSRTFCCTable").slideDown('slow');
		$("#transactionDetailsSRTFCCTable th[class='paymentDetailsLabel']").hide();
		$("#"+parentTr+" td[class='paymentDetails']").hide();
		populateItemBasedOnWhatYouWantToDo(value,text);
		//populatecreditcustomer(value,text,parentTr);
		singleUserMiscChanges("srtfcctrid");
	} else if(text=="Purchase returns" || value=="13"){
		resetMultiItemsTableLength('prtfcvtrid');
		$('#transWhatToDo span').html("Please select or input the details in the columns below, After that input the receipt details, input remarks, upload documents to support this transaction and finally click on the button 'Submit for approval...' to complete processing this transaction. At anytime before you submit, click on button on the left side of this table to cancel the transaction.");
		$("#"+parentTr+" .masterListItems").children().remove();
		$("#"+parentTr+" .masterListItems").append('<option value="">--Please Select--</option>');
		$("#"+parentTr+" select[class='salesExpenseTxns']").children().remove();
		$("#"+parentTr+" select[class='salesExpenseTxns']").append('<option value="">--Please Select--</option>');
		$("#transactionDetailsPRTFCVTable").slideDown('slow');
		$("#transactionDetailsPRTFCVTable th[class='paymentDetailsLabel']").hide();
		$("#"+parentTr+" td[class='paymentDetails']").hide();
		populateItemBasedOnWhatYouWantToDo(value,text);
		//populatecreditcustomer(value,text,parentTr);
		singleUserMiscChanges("prtfcvtrid");
	} else if(text=="Transfer main cash to petty cash" || value=="14"){
		//$('#transWhatToDo span').html("Please select or input the details in the columns below, After that input the receipt details, input remarks, upload documents to support this transaction and finally click on the button 'Submit for approval...' to complete processing this transaction. At anytime before you submit, click on button on the left side of this table to cancel the transaction.");
		$('#transWhatToDo span').html("In column one select the branch where you wish to transfer cash from main cash to petty cash. In column two and three system will automatically display how much main cash and petty cash is currently available at that branch. In column four, input the amount you wish to transfer from main cash to petty cash. In column five input the purpose of this transfer. In column six and seven system will automatically display the resultant cash balance in main cash and petty cash after this proposed transfer. In the last column input remarks and also upload any document in support of this transaction and click on \"Submit for Approval\". Once you click on \"Submit for Approval\", the transaction status will be displayed as \"Require Approval\". Once the Manager approves this transfer, you will see the transaction status changing to \"Approved\" and then you will need to click on \"Complete Accounting\" to ensure that the transaction is posted to ledger and only then is the process complete.");
		$("#transactionDetailsTMTPCATable").slideDown('slow');
		$("#transactionDetailsTMTPCATable th[class='paymentDetailsLabel']").hide();
		$("#"+parentTr+" td[class='paymentDetails']").hide();
		populateItemBasedOnWhatYouWantToDo(value,text);
		singleUserMiscChanges("tmtpcatrid");
	} else if(text=="Make Provision/Journal Entry" || value=="20"){
		$("#backDateDiv").hide();
        resetMultiItemsTableLength('debitAccountHeadTd');
        resetMultiItemsTableLength('creditAccountHeadTd');
		$('#transWhatToDo span').html("Use this option to pass journal entries or to input opening balance for assets and liabilities. JOURNAL ENTRIES: To pass journal entries select the account to debit in column 2 and input the amount in column 3, then select the account to credit in column 4 and system will automatically take the amount to credit automatically. You can create one journal entry at a time and therefore debit & credit amount will have to be same. OPENING BALANCE: For creating opening balance of any asset (including customers / AR), select the asset (under debit column) and input the opening balance amount in column 3 and in column 4 select “Opening balance” which is a default item in dropdown list AND in the column “Purpose” it is mandatory to type Opening balance.");
		$("#transactionDetailsMTEFPETable").slideDown('slow');
		getBranchData();
		// singleUserMiscChanges(""); Not For GE
	} else if(text=="Journal Entry" || value=="21"){
		$("#transactionDetailsMTEFJETable").slideDown('slow');
		showChartofAccountsInProvisionalAndJournalEntry("mtefjetrid");
	} else if(text=="Withdraw Cash From Bank" || value=="22"){
		$('#transWhatToDo span').html("In column one select the branch where you wish to make withdrawal from the bank. In column two select the bank from which you wish to make cash withdrawal. system will automatically display in column three, the balance available in that bank account and in column four system will display if the Manager has given any standing instructions that must be followed for cash withdrawal from bank. In column five input the amount of cash that you wish to withdraw. In column six,  will automatically display balance remaining in the bank after this withdrawal. In column seven upload any document (if any) to support this transaction or any document as per Manager's instruction. Column eight input the remarks such as reason for withdrawal or proposed usage of the cash, tc and then click on \"Submit for Approval\". After the Manager approves the withdrawal, you can go ahead and withdraw cash and submit the transaction for accounting. The transaction will not be complete until you click on \"Complete Accounting\" after approval by Manager.");
		$("#transactionDetailsWCAFBNKTable").slideDown('slow');
		populateItemBasedOnWhatYouWantToDo(value,text);
		singleUserMiscChanges("wcabnktrid");
	} else if(text=="Deposit Cash In Bank" || value=="23"){
		$("#transactionDetailsDCAIBNKTable").slideDown('slow');
		populateItemBasedOnWhatYouWantToDo(value,text);
		singleUserMiscChanges("dcabnktrid");
	} else if(text=="Transfer Funds From One Bank To Another" || value=="24"){
		$('#transWhatToDo span').html("Use this option to transfer funds from one bank account (at a any branch) to another bank account (at another branch) . In column one select the branch from where you wish to transfer funds. In the second column select the bank account at that branch from which you wish to transfer funds (money).  will automatically display the balance details of the bank account from which you wish to transfer funds. In the fourth column select the branch to which you wish to transfer the funds. In the fifth column select the bank account at the branch to which you wish to transfer the funds. In column six  will display the balance available before the transfer. In the seventh column input the amount of funds you wish to transfer and  will automatically display the balance in the transferee bank account if this transfer is made. In the next column input the purpose of this transfer. In the last column input remarks and also upload documents in support of this transaction and then click on \"Submit for Approval\". Once you click on \"Submit for Approval\", the transaction status will be displayed as \"Require Approval\". Once the Manager approves this transfer, you will see the transaction status changing to \"Approved\" and then you will need to click on \"Complete Accounting\" to ensure that the transaction is posted to ledger and only then is the process complete.");
		$("#transactionDetailsTFFTBNKTable").slideDown('slow');
		populateItemBasedOnWhatYouWantToDo(value,text);
	} else if(value == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
        resetMultiItemsTableLength('transactionDetailsTIFBTBTable');
        resetMainTransTableFields('tifbtbtrid');
        resetMultiItemsTableFieldsData('#tifbtbtrid');
		$('#transWhatToDo span').html("This transaction does not have any financial impact and it is to be used to control and transfer physical units of inventory from one branch to another. In column one select the item of inventory you wish to transfer from one branch to another. In column two select the branch from which you wish to transfer the inventory.  will then automatically display how many units of that item are available in that branch and if any stock transfer is currently in progress. In column three input the number of units you wish to transfer from this branch. In column four selct the branch to which you wish to transfer the inventory.  will display the inventory in stock at that branch before inventory transfer. In column five system will automatically display the inventory at the two branches after the stock transfer is made. In column six upload documents in support of this transfer and in the last column input remarks including the reason for this stock transfer and then click on \"Submit for Approval\". Once you click on \"Submit for Approval\", the transaction status will be displayed as \"Require Approval\". Once the Manager approves this transfer, you will see the transaction status changing to \"Approved\" and then you will need to click on \"Complete Accounting\" to ensure that the transaction is posted to ledger and only then is the process complete.");
		$("#transactionDetailsTIFBTBTable").slideDown('slow');
		$(".inventoryTxnBranch").children().remove();
		$(".inventoryTxnBranch").append('<option value="">--Please Select--</option>');
		$(".availableStock").text("");
		$(".stockTransferInProgress").text("");
		$(".availableToStock").text("");
		$("#unitToTransferId").val("");
		$("#resultantStockId").val("");
		$("input[name='pcafcvuploadSuppDocs']").val("");
		$("#tifbtbRemarks").val("");
        //populateItemBasedOnWhatYouWantToDo(value,text);
		//populateInventoryItems(value,text,parentTr);
	} else if(text=="Inventory Opening Balance" || value=="26"){
		$('#transWhatToDo span').html("Use this option to input the number of items in opening balance of trading inventory. You can only input the number of items or units in opening inventory and not the value.");
		$("#transactionDetailsOBFINVTable").slideDown('slow');
		$(".inventoryTxnBranch").children().remove();
		$(".inventoryTxnBranch").append('<option value="">--Please Select--</option>');
		$("#obfinvAvailableStockId").val("");
		$(".stockTransferInProgress").text("");
		$(".openingStockInProgress").text("");
		populateInventoryItems(value,text,parentTr);

	}else if(value==PREPARE_QUOTATION){  // Make Quotation
        parentTr = "transactionQuotationTable";
        resetMultiItemsTableLength('transactionQuotationTable');
        resetMainTransTableFields('quotattrid');
        resetMultiItemsTableFieldsData('#quotattrid');
		populateItemBasedOnWhatYouWantToDo(value,text);
        $("#transactionQuotationTable").slideDown('slow');
        $("#transactionQuotationTable th[class='paymentDetailsLabel']").show();
        $("#"+parentTr+" td[class='paymentDetails']").show();
	}else if(value==PROFORMA_INVOICE){
		resetMultiItemsTableLength('profortrid');
		$("#transactionProformaTable").slideDown('slow');
		$("#transactionProformaTable th[class='paymentDetailsLabel']").show();
		$("#"+parentTr+" td[class='paymentDetails']").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
	}else if(value==PURCHASE_ORDER){
		resetMultiItemsTableLength('purordtrid');
		$("#transactionPurchaseOrderTable").slideDown('slow');
		$("#transactionPurchaseOrderTable th[class='paymentDetailsLabel']").show();
		$("#"+parentTr+" td[class='paymentDetails']").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
		singleUserMiscChanges("purordtrid");
	}else if(value== CREDIT_NOTE_CUSTOMER || value == DEBIT_NOTE_CUSTOMER){
		$('#transWhatToDo span').html('');
		resetMultiItemsTableLength('creditDebitTxnDiv');
		$("#creditDebitTxnDiv").slideDown('slow');
		$("#txnCrditDebitTable th[class='paymentDetailsLabel']").show();
		$("#"+parentTr+" td[class='paymentDetails']").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
		if(value== CREDIT_NOTE_CUSTOMER){
			$("#creditDebitTxnDiv .creditDebitType").children().remove();
			$("#creditDebitTxnDiv .creditDebitType").append('<option value="">--Select Option--</option><option value="1">Decrease in price</option><option value="2">Decrease in quantity</option>');
		}else{
			$("#creditDebitTxnDiv .creditDebitType").children().remove();
			$("#creditDebitTxnDiv .creditDebitType").append('<option value="">--Select Option--</option><option value="1">Increase in price</option><option value="2">Increase in quantity</option>');
		}
		//singleUserMiscChanges("cdtdbttrid");
	}else if(value== CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR){
		parentTr = "txnCrditDebitVendTable";
		resetMultiItemsTableLength('creditDebitVendTxnDiv');
		resetMainTransTableFields('cdtdbvtrid');
		resetMultiItemsTableFieldsData('#cdtdbvtrid');
		$('#transWhatToDo span').html('');
		$("#creditDebitVendTxnDiv").slideDown('slow');
		$("#txnCrditDebitVendTable th[class='paymentDetailsLabel']").show();
		$("#"+parentTr+" td[class='paymentDetails']").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
		$("#"+parentTr+ " select[class='txnTypeOfSupply']").children().remove();
		$("#txnCrditDebitVendTable th[class='paymentDetailsLabel']").hide();
		$("#"+parentTr+" td[class='paymentDetails']").hide();
		$("#"+parentTr+ " select[class='txnTypeOfSupply']").append('<option value="">--Please Select--</option><option value="1">Regular</option><option value="2">Supply on Reverse Charge - Unregistered Vendor</option><option value="3">Supply attracting tax on reverse charge - registered vendor</option><option value="4">Overseas / SEZ Import Goods - Supply</option><option value="5">Overseas / SEZ Import Services - Supply</option>');

		if(value== CREDIT_NOTE_VENDOR){
			$("#"+parentTr+" .creditDebitType").children().remove();
			$("#"+parentTr+" .creditDebitType").append('<option value="">--Select Option--</option><option value="1">Increase in price</option><option value="2">Increase in quantity</option>');
		}else{
			$("#"+parentTr+" .creditDebitType").children().remove();
			$("#"+parentTr+" .creditDebitType").append('<option value="">--Select Option--</option><option value="1">Decrease in price</option><option value="2">Decrease in quantity</option>');
		}
		//singleUserMiscChanges("cdtdbvtrid");
	}else if(PROCESS_PAYROLL == value){ //payroll processing
		$("#processPayroll").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
	}else if(REFUND_ADVANCE_RECEIVED == value){
        parentTr = "txnMkrfndTable";
        resetMultiItemsTableLength('transactionDetailsMkrfndTable');
        resetMainTransTableFields('mkrfndtrid');
        resetMultiItemsTableFieldsData('#mkrfndtrid');
        $("#transactionDetailsMkrfndTable").show();
        populateItemBasedOnWhatYouWantToDo(value,text);
        singleUserMiscChanges("mkrfndtrid");
	}else if(REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE == value){
		    resetMultiItemsTableLength('rfndamtrid');
	        resetMainTransTableFields('rfndamtrid');
	        resetMultiItemsTableFieldsData('#rfndamtrid');
	        $("#transactionDetailsRfndAmntRcvdTable").show();
	        populateItemBasedOnWhatYouWantToDo(value,text);
	        singleUserMiscChanges("rfndamtrid");
    }else if(CANCEL_INVOICE == value){
		resetMultiItemsTableLength('cancelInvoiceTxnDiv');
		resetMainTransTableFields('caninvtrid');
		resetMultiItemsTableFieldsData('#caninvtrid');
		$("#cancelInvoiceTxnDiv").show();
		populateItemBasedOnWhatYouWantToDo(value,text);
		if(isSingleUserDeploy == "true") {
			$("#caninvsubmitForAccounting").show();
			$("#caninvsubmitForApproval").hide();
		}else{
			$("#caninvsubmitForAccounting").hide();
			$("#caninvsubmitForApproval").show();
		}
	}else if(REVERSAL_OF_ITC == value) {
    	$("#txnRevOfITCTable > tbody").html("");
    	addRevOfITCRow();
        $("#transactionDetailsRevOfITCTable").show();
    }else if(BILL_OF_MATERIAL == value) {
        $("#backDateDiv").hide();
    	resetMultiItemsTableLength('billOfMaterialTxnDiv');
		resetMainTransTableFields('bomtrid');
		resetMultiItemsTableFieldsData('#bomtrid');
		$("#billOfMaterialTxnDiv").show();
        getAllDifferentUnitsfromCoa("#multipleItemsBomTxnTbl > tbody > tr > td > #bomTxnUnitOfMeasure");
		populateItemBasedOnWhatYouWantToDo(value,text);
    }else if(CREATE_PURCHASE_REQUISITION == value) {
		$("#backDateDiv").hide();
		setPurchaseRequisitionTxnCategory();
	}else if(CREATE_PURCHASE_ORDER == value) {
        $("#backDateDiv").hide();
		setPurchaseOrderTxnCategory();
    }else if(MATERIAL_ISSUE_NOTE == value) {
        $("#backDateDiv").hide();
        resetMultiItemsTableLength('materialIssueNoteTxnDiv');
        resetMainTransTableFields('matinttrid');
        resetMultiItemsTableFieldsData('#matinttrid');
        $("#materialIssueNoteTxnDiv").show();
        populateItemBasedOnWhatYouWantToDo(value, text);
    }else if(text=="----------Please Select----------"|| value=="" ){
		$(".openWhatDoINeedToDo").hide();
		$(".whatDoINeedToDoContent").html("");
		$(".whatDoINeedToDoContent").hide();
		$(".transactionDetailsTable").slideUp();
	}
}

$(document).ready(function(){
	$('.newExpenseButton').click(function(){
		$("#whatYouWantToDo").removeAttr("disabled");
		$(".transactionDetailsTable").hide();
		 $(".openWhatDoINeedToDo").hide();
		 $(".whatDoINeedToDoContent").html("");
		 $(".whatDoINeedToDoContent").hide();
		 $("#searchTransaction").slideUp('slow');
		 $("#searchTransaction input[type='text']").val("");
		 $('#searchTransaction select').find('option:first').prop("selected","selected");
		 $('#searchTransaction textarea').val("");
		 $("#createExpense").slideDown('slow');
		 $("#expenseFormCreate").show();
		 $("#expenseFormShow").hide();
		 $('#submitApprovalButton').show();
		 $("#pendingExpenseId").attr("class","active");
		 $("#createExpenses").attr("class","linklist");
		 $("#approvalSubmit").attr("class","linkcolor");
		 $('#transactionPurpose option:first').prop("selected","selected");
		 $('#payMentType option:first').prop("selected","selected");
		 $('#docuploadurl').val("");
		 $('#noofitems').val("");
		 $('#tamount').val("");
		 $('#hiddenId').val("");
		 $("#usercatcreate").hide();
		 $("#useritemcreate").hide();
		 $("#uservendorcreate").hide();
		 $('#specifics').children().remove();
	     $('#vendor').children().remove();
	     $('select[name="specifics"]').children().remove();
		 $('select[name="vendor"]').children().remove();
		 $('#vendorInvDateDiv').html("");
		 $("#txnDocRefNo").val("");
		 $("#backDateDiv").hide();
		 getSellReceiveData();
	});
});

/*
function getProcurementCountDetails(){
	//alert("getProcurementCountDetails") ;
	var jsonData = {};
	jsonData.usermail = $("#hiddenuseremail").text();
	var url="/transaction/getprocurementrequest";
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
	    method:"POST",
	    contentType:'application/json',
		success: function (data) {
			$("#procurementRequestsList").children().remove();
			$("#bocpratrid select[name='procurementRequestForCreator']").children().remove();
			$("#bocpratrid select[name='procurementRequestForCreator']").append('<option value="">--Please Select--</option>');
			$("#bocapltrid select[name='procurementRequestForCreator']").children().remove();
			$("#bocapltrid select[name='procurementRequestForCreator']").append('<option value="">--Please Select--</option>');
			if(data.procurementData.length>0){
				for(var i=0;i<data.procurementData.length;i++){
					$("#proccount").text("");
					$("#proccount").text(data.procurementData[i].count);
					$("#bocpratrid select[name='procurementRequestForCreator']").append('<option value="'+data.procurementData[i].procurementRequestId+'">' +data.procurementData[i].procurementDetails+ '</option>');
					$("#bocapltrid select[name='procurementRequestForCreator']").append('<option value="'+data.procurementData[i].procurementRequestId+'">' +data.procurementData[i].procurementDetails+ '</option>');
					$("#procurementRequestsList").append('<li>'+data.procurementData[i].procurementDetails+'</li>');
				}
			}else{
				$("#proccount").text("");
			}
		},
		error: function (xhr, status, error) {

		}
	});
} */

function getSellReceiveData(){
	$("#whatYouWantToDo").children().remove();
	var fullname =  $("#hiddenfullName").text();
	$("#whatYouWantToDo").append('<option value="">---- Hi ' + fullname + '! Please select what you want to do? ----</option>');
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/getsellreceicve";
	$.ajax({
		url: url,
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		data:JSON.stringify(jsonData),
		type:"text",
		method:"POST",
		async : false,
		contentType:'application/json',
		success: function (data) {
			if(data.userInSessionData[0].userinsession=="userinsession"){
			  for(var i=0;i<data.sellReceiveData.length;i++){
				  if(data.sellReceiveData[i].id != 29)
				$("#whatYouWantToDo").append('<option value="'+data.sellReceiveData[i].id+'">'+data.sellReceiveData[i].name+'');
			  }
			}else{
			  window.location.href="/logout";
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
		  		doLogout();
		  	}else if(xhr.status == 500){
				swal("Error on fetching Transaction purposes!", "Please retry, if problem persists contact support team", "error");
			}
      	}
	});
}

function cancelWhatDoINeedToDoContent(){
	$(".whatDoINeedToDoContent").hide();
}

function getCashBankReceivablePayable(){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	var url="/user/approverCashBankReceivablePayables";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
					"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		contentType : 'application/json',
		success     : function (data) {
			$(".cashBalanceAllBranchesDisplayText").text("");
			$(".bankBalanceAllBranchesDisplayText").text("");
			$(".accountsReceivablesAllBranchesDisplayText").text("");
			$(".accountsPayablesAllBranchesDisplayText").text("");
			$(".cashBalanceAllBranchesDisplayText").text("CASH: "+data.cashBankRecivablesPayablesData[0].cashBalance);
			$(".bankBalanceAllBranchesDisplayText").text("BANK: "+data.cashBankRecivablesPayablesData[0].bankBalance);
			$(".accountsReceivablesAllBranchesDisplayText").text("RECEIVABLES: "+data.cashBankRecivablesPayablesData[0].accountsReceivables);
			$(".accountsPayablesAllBranchesDisplayText").text("PAYABLES: "+data.cashBankRecivablesPayablesData[0].accountsPayables);
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on fetching cash/bank balance!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

function showRecPayablesOpeningBalAndCurrentYearTotalForTranTabs(elem){
	var jsonData = {};
	var tabId = $(elem).attr('id');
	var useremail=$("#hiddenuseremail").text();
	jsonData.tabElement = tabId;
	jsonData.usermail = useremail;
	jsonData.currDashboardToDate = $("#currDashboardToDate").val();
	var url="/dashboard/recPayablesOpeningBalAndCurrentYearTotal";
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
			$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
	    	$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
	    	if(tabId == "accountsReceivablesAllBranches") {
	    		$("#staticcashbankreceivablepayablebranchwisebreakup").data('type','SALE');
	    	}else if(tabId == "accountsPayablesAllBranches") {
	    		$("#staticcashbankreceivablepayablebranchwisebreakup").data('type','PURCHASE');
	    	}else {
	    		$("#staticcashbankreceivablepayablebranchwisebreakup").data('type','');
	    	}
	    	$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
	    	$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
	    	$("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").attr('class', '');
	    	$("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").addClass(tabId);
            $("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").show();
	    	$("#staticcashbankreceivablepayablebranchwisebreakup input[id='ageingDate']").val('');
	    	$("#staticcashbankreceivablepayablebranchwisebreakup h4").text("Total");
	    	if(data.recPayablesOpeningBalAndCurrentYearTotal.length>0){
	    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivablesBreakupTable" style="margin-top: 0px; width:450px;">'+
	    		'<thead class="tablehead1" style="position:relative"><th>Opening Balance</th><th>Current Year</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
	    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivablesBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="customerReceivablesOnDashboard" onclick="custVendOpeningBalanceBreakup(this, \''+ tabId +'\');">'+data.recPayablesOpeningBalAndCurrentYearTotal[0].openingBalance+'</a>	</td>'+
	    		'<td><div id='+tabId+'><a href="#pendingExpense" id='+tabId+' class="customerReceivablesOnDashboard" onclick="branchWiseCashExpenseReceivablesPayables(this);">'+data.recPayablesOpeningBalAndCurrentYearTotal[0].netRecievablePayablesCurrentYear+'</a></div></td></tr>');

	    	}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function wightedAverageForTransaction(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	var parentTr = $(elem).closest('tr').attr('id');    	//	parentTr=$(elem).parent().parent().parent().parent().attr('id'); Sunil

	var transactionEntityId=parentTr.substring(17, parentTr.length);
	var periodVal=$("#"+parentTr+" select[id='waperiod'] option:selected").val();
	var useremail=$("#hiddenuseremail").text();
	jsonData.transactionId=transactionEntityId;
	jsonData.period=periodVal;
	jsonData.usermail = useremail;
	var url="/transaction/wightedAverageForTransaction";
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
			$("#staticwapbranchwisebreakup").attr('data-toggle', 'modal');
	    	$("#staticwapbranchwisebreakup").modal('show');
	    	$(".staticwapbranchwisebreakupclose").attr("href",location.hash);
	    	$("#staticwapbranchwisebreakup div[class='modal-body']").html("");
	    	var selectedTxnConsolidatedWAP=data.selectedTransactionMinWeightedAveragePriceData[0].transactionBranchMinWeightedAverage;
	    	if(data.branchVendorWiseWeightedAveragePriceData.length>0){
	    		$("#staticwapbranchwisebreakup div[class='modal-body']").html('<button name="wightedAveragePriceAdvanceSearchButton'+selectedTxnConsolidatedWAP+'" id="wightedAveragePriceAdvanceSearchButton'+transactionEntityId+'" class="wightedAveragePriceAdvanceSearchButton btn btn-primary btn-idos m-bottom-10" title="Search Alternate Suppliers In Your Location" onclick="leastWapGlobalSearchInBranchLocation(this);"><i class="fa fa-search"></i>Search Alternate Suppliers In Your Location</button><div class="datascrolltable" style="height: 400px; overflow: auto;"><table class="table table-hover table-striped excelFormTable transaction-create" id="branchWAPBreakupTable" style="margin-top: 0px;">'+
	    		'<thead class="tablehead1"><th>Branch/Vendor Name</th><th>Email</th><th>Number</th><th>Gross Weighted Average Price</th><th>Net Weighted Average Price</th><tr></thead><tbody></tbody></table></div>');
		    	for(var i=0;i<data.branchVendorWiseWeightedAveragePriceData.length;i++){
		    		$("#staticwapbranchwisebreakup h4[class='itemName']").html("");
		    		$("#staticwapbranchwisebreakup h4[class='itemName']").html("WAP BreakUps:"+data.branchVendorWiseWeightedAveragePriceData[i].itemSpecificsName);
		    		$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody").append('<tr><td>'+data.branchVendorWiseWeightedAveragePriceData[i].branchVendorName+'</td><td>'+data.branchVendorWiseWeightedAveragePriceData[i].itemName+'</td><td>'+data.branchVendorWiseWeightedAveragePriceData[i].period+'</td><td>'+data.branchVendorWiseWeightedAveragePriceData[i].grossWeightedAverage+'</td><td>'+data.branchVendorWiseWeightedAveragePriceData[i].netWeightedAverage+'</td></tr>');
		    	}
	    	}
	    	$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody tr td:contains('"+data.branchVendorWiseMinMaxWeightedAveragePriceData[0].minGrossWeightedAverage+"')").css('color','green');
	    	$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody tr td:contains('"+data.branchVendorWiseMinMaxWeightedAveragePriceData[0].maxGrossWeightedAverage+"')").css('color','blue');
	    	$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody tr td:contains('"+data.branchVendorWiseMinMaxWeightedAveragePriceData[0].minNetWeightedAverage+"')").css('color','green');
	    	$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody tr td:contains('"+data.branchVendorWiseMinMaxWeightedAveragePriceData[0].maxNetWeightedAverage+"')").css('color','blue');
	    	$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody tr td:contains('"+data.branchVendorWiseMinMaxWeightedAveragePriceData[0].vendorminGrossWeightedAverage+"')").css('color','green');
	    	$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody tr td:contains('"+data.branchVendorWiseMinMaxWeightedAveragePriceData[0].vendormaxGrossWeightedAverage+"')").css('color','blue');
	    	$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody tr td:contains('"+data.branchVendorWiseMinMaxWeightedAveragePriceData[0].vendorminNetWeightedAverage+"')").css('color','green');
	    	$("#staticwapbranchwisebreakup div[class='modal-body'] table[id='branchWAPBreakupTable'] tbody tr td:contains('"+data.branchVendorWiseMinMaxWeightedAveragePriceData[0].vendormaxNetWeightedAverage+"')").css('color','blue');
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on fetching Transaction detail!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

function listAllBranchBankAccounts(elem){
	$(".dynmBnchBankActList").remove();
	var value=$(elem).val();
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();

	var parentTr = $(elem).closest('tr').attr('id');
	if(whatYouWantToDoVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW){
		var modeOption=$("#"+parentTr+" select[id='socpnreceiptdetail'] option:selected").val();
		if(modeOption=="2"){
			var selectedTransactionBranch=$("#"+parentTr+" select[id='soccpnTxnForBranches'] option:selected").val();
			if(!selectedTransactionBranch==""){
				$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
				//get all the bank accounts related to txn branch for this organization
				var jsonData={};
				var useremail=$("#hiddenuseremail").text();
				jsonData.usermail = useremail;
				jsonData.txnBranch=selectedTransactionBranch;
				jsonData.txnPurpose=whatYouWantToDoVal;
				var url="/branch/bankAccountsForPayment";
				$.ajax({
					url : url,
					data : JSON.stringify(jsonData),
					type : "text",
					headers:{
						"X-AUTH-TOKEN": window.authToken
					},
					method : "POST",
					contentType : 'application/json',
					success : function(data) {
						$(".dynmBnchBankActList").remove();
						$(elem).after('<div class="dynmBnchBankActList"><select name="availableBank" id="availableBank"><option value="">Select Bank</option></select></div>');
						if(data.availableBranchBankData.length>0){
							for(var i=0;i<data.availableBranchBankData.length;i++){
								$("#"+parentTr+" select[id='availableBank']").append('<option value="'+data.availableBranchBankData[i].bnchBankAccountsId+'">'+data.availableBranchBankData[i].bnchBankAccountsName+'</option>')
							}
							addBankInstrumentDetail(parentTr);

						}else{
							$(".dynmBnchBankActList").remove();
							swal("Bank account is not configured for the branch for which you want to process the transaction.");
							$(elem).find('option:first').prop("selected","selected");
						}
					},
					error: function(xhr, status, error) {
						if(xhr.status == 401){ doLogout();
						}else if(xhr.status == 500){
				    		swal("Error on fetching bank detail!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
					}
				});
			}else{
				swal("Incomplete transaction data!", "Please select the transaction branch for which you want to receive money from customer", "error");
				$(elem).find('option:first').prop("selected","selected");
				enableTransactionButtons();
			}
		}
	}else if(whatYouWantToDoVal == BUY_ON_CASH_PAY_RIGHT_AWAY || whatYouWantToDoVal == PAY_VENDOR_SUPPLIER || whatYouWantToDoVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
				var selectId = "";
				var branchId = "";
				if(whatYouWantToDoVal == BUY_ON_CASH_PAY_RIGHT_AWAY) {
					branchId = "bocpraTxnForBranches";
					selectId = "bocprawreceiptdetail";
				}else if(whatYouWantToDoVal == PAY_VENDOR_SUPPLIER){
					branchId = "mcpfcvTxnForBranches";
					selectId = "mcpfcvpaymentdetail";
				}else if(whatYouWantToDoVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
					branchId = "pcafcvTxnForBranches";
					selectId = "pcafcvpaymentdetail";
				}

				var modeOption=$("#"+parentTr+" select[id='"+selectId+"'] option:selected").val();
				if(modeOption=="2"){
					var selectedTransactionBranch=$("#"+parentTr+" select[id='"+branchId+"'] option:selected").val();
					if(!selectedTransactionBranch==""){
						$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
						//get all the bank accounts related to txn branch for this organization
						var jsonData={};
						var useremail=$("#hiddenuseremail").text();
						jsonData.usermail = useremail;
						jsonData.txnBranch=selectedTransactionBranch;
						jsonData.txnPurpose=whatYouWantToDoVal;
						var url="/branch/bankAccountsForPayment";
						$.ajax({
							url : url,
							data : JSON.stringify(jsonData),
							type : "text",
							headers:{
								"X-AUTH-TOKEN": window.authToken
							},
							method : "POST",
							contentType : 'application/json',
							success : function(data) {
								$(".dynmBnchBankActList").remove();
								$(elem).after('<div class="dynmBnchBankActList"><select name="availableBank" id="availableBank"><option value="">Select Bank</option></select></div>');
								if(data.availableBranchBankData.length>0){
									for(var i=0;i<data.availableBranchBankData.length;i++){
										$("#"+parentTr+" select[id='availableBank']").append('<option value="'+data.availableBranchBankData[i].bnchBankAccountsId+'">'+data.availableBranchBankData[i].bnchBankAccountsName+'</option>')
									}
									addBankInstrumentDetail(parentTr);
								}else{
									$(".dynmBnchBankActList").remove();
									swal("Bank account is not configured for the branch for which you want to process the transaction.");
									$(elem).find('option:first').prop("selected","selected");
								}
							},
							error: function(xhr, status, error) {
								if(xhr.status == 401){ doLogout();
								}else if(xhr.status == 500){
						    		swal("Error on fetching bank detail!", "Please retry, if problem persists contact support team", "error");
						    	}
							},
							complete: function(data) {
								$.unblockUI();
							}
						});
					}else{
						swal("Incomplete transaction data!", "Please select the transaction branch for which you want to receive money from customer", "error");
						$(elem).find('option:first').prop("selected","selected");
						enableTransactionButtons();
					}
				}
			} else if(whatYouWantToDoVal == RECEIVE_PAYMENT_FROM_CUSTOMER){ // Sunil if(text=="Receive payment from customer")
		var modeOption=$("#"+parentTr+" select[id='rcpfccpaymentdetail'] option:selected").val();
		if(modeOption=="2"){
			var selectedTransactionBranch=$("#"+parentTr+" select[id='rcpfccTxnForBranches'] option:selected").val();
			if(selectedTransactionBranch==""){
				swal("Incomplete transaction data!", 'You must select Branch to proceed this transactions.', 'warning');
				$("#"+parentTr+" select[id='rcpfccTxnForBranches']").focus();
				$(elem).find('option:first').prop("selected","selected");
				enableTransactionButtons();
				return false;
			}

			var txnInvoice=$("#rcpfccpendingInvoices option:selected").val();
			if(!txnInvoice==""){
				$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
				//get all the bank accounts related to txn branch to which invoice belongs to for this organization
				var jsonData={};
				var useremail=$("#hiddenuseremail").text();
				jsonData.usermail = useremail;
				jsonData.pendingInvoice=txnInvoice;
				jsonData.txnPurpose=whatYouWantToDoVal;
				jsonData.txnBranch=selectedTransactionBranch;
				var url="/branch/bankAccountsForPayment";
				$.ajax({
					url : url,
					data : JSON.stringify(jsonData),
					type : "text",
					headers:{
						"X-AUTH-TOKEN": window.authToken
					},
					method : "POST",
					contentType : 'application/json',
					success : function(data) {
						$(".dynmBnchBankActList").remove();
						$(elem).after('<div class="dynmBnchBankActList"><select name="availableBank" id="availableBank"><option value="">Select Bank</option></select></div>');
						if(data.availableBranchBankData.length>0){
							for(var i=0;i<data.availableBranchBankData.length;i++){
								$("#"+parentTr+" select[id='availableBank']").append('<option value="'+data.availableBranchBankData[i].bnchBankAccountsId+'">'+data.availableBranchBankData[i].bnchBankAccountsName+'</option>')
							}
							addBankInstrumentDetail(parentTr);
						}else{
							$(".dynmBnchBankActList").remove();
							swal("Bank account is not configured for the branch for which you want to process the transaction.");
							$(elem).find('option:first').prop("selected","selected");
							enableTransactionButtons();
						}
					},
					error: function(xhr, status, error) {
						if(xhr.status == 401){ doLogout();
						}else if(xhr.status == 500){
				    		swal("Error on fetching bank detail!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
						enableTransactionButtons();
					}
				});
			}else{
				swal("Incomplete transaction data!", "Please select pending invoice for customer from whom you want to receive money.", "error");
				$(elem).find('option:first').prop("selected","selected");
				enableTransactionButtons();
			}
		}

	} else if(whatYouWantToDoVal == RECEIVE_ADVANCE_FROM_CUSTOMER || whatYouWantToDoVal == REFUND_ADVANCE_RECEIVED || whatYouWantToDoVal == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE || whatYouWantToDoVal == PROCESS_PAYROLL||parentTr.includes('Payroll')==true){ // sunil if(text=="Receive advance from customer" ){
		var modeOption = "";
		if(whatYouWantToDoVal == RECEIVE_ADVANCE_FROM_CUSTOMER) {
			modeOption = $("#"+parentTr+" select[id='rcafccpaymentdetail'] option:selected").val();
		}else if(whatYouWantToDoVal == REFUND_ADVANCE_RECEIVED|| whatYouWantToDoVal == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
			modeOption = $("#"+parentTr+" select[id='mkrfndpaymentdetail'] option:selected").val();
		}else if(whatYouWantToDoVal == PROCESS_PAYROLL||parentTr.includes('Payroll')==true) {
			modeOption = $("#"+parentTr+" select[id='paryollreceiptdetail'] option:selected").val();

		}
		if(modeOption=="2"){
			var selectedTransactionBranch="";
			if(parentTr.includes('Payroll')==true){
				selectedTransactionBranch = $("#"+parentTr+" p[class='branchDetails'] ").attr('id');
			}
			else{
			selectedTransactionBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
			}
			if(selectedTransactionBranch==""){
				swal("Incomplete transaction data!", 'You must select a Branch to proceed with this transactions.', 'warning');
				$("#"+parentTr+" select[class='txnBranches']").focus();
				$(elem).find('option:first').prop("selected","selected");
				enableTransactionButtons();
				return false;
			}
			//get all the bank accounts related to main headquarter branch for this organization
			var txnCustomer = "";
			if(whatYouWantToDoVal == RECEIVE_ADVANCE_FROM_CUSTOMER) {
				txnCustomer = $("#rcafccCustomers option:selected").val();
			}else if(whatYouWantToDoVal == REFUND_ADVANCE_RECEIVED) {
				txnCustomer = $("#mkrfndtrid").find("#mkrfndCustomer option:selected").val();
			}else if(whatYouWantToDoVal == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
				txnCustomer = $("#rfndamtrid").find("#mkrfndCustomer option:selected").val();
			}
			if(!txnCustomer=="" || (whatYouWantToDoVal == PROCESS_PAYROLL || parentTr.includes('Payroll')==true)){
				$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
				var jsonData={};
				var useremail=$("#hiddenuseremail").text();
				jsonData.usermail = useremail;
				jsonData.txnPurpose="34";
				jsonData.txnBranch=selectedTransactionBranch;
				var url="/branch/bankAccountsForPayment";
				$.ajax({
					url : url,
					data : JSON.stringify(jsonData),
					type : "text",
					headers:{
						"X-AUTH-TOKEN": window.authToken
					},
					method : "POST",
					contentType : 'application/json',
					success : function(data) {
						$(".dynmBnchBankActList").remove();
						$(elem).after('<div class="dynmBnchBankActList"><select class="txnBranchBanks" name="availableBank" id="availableBank"><option value="">Select Bank</option></select></div>');
						if(data.availableBranchBankData.length>0){
							for(var i=0;i<data.availableBranchBankData.length;i++){
								$("#"+parentTr+" select[id='availableBank']").append('<option value="'+data.availableBranchBankData[i].bnchBankAccountsId+'">'+data.availableBranchBankData[i].bnchBankAccountsName+'</option>')
							}
							addBankInstrumentDetail(parentTr);
						}else{
							$(".dynmBnchBankActList").remove();
							swal("Bank account is not configured for the branch for which you want to process the transaction.");
							$(elem).find('option:first').prop("selected","selected");
							enableTransactionButtons();
						}
					},
					error: function(xhr, status, error) {
						if(xhr.status == 401){ doLogout();
						}else if(xhr.status == 500){
				    		swal("Error on fetching bank detail!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
						enableTransactionButtons();
					}
				});
			}else{
				swal("Incomplete transaction data!", "Please select customer from whom you want to receive advance money.", "error");
				$(elem).find('option:first').prop("selected","selected");
				enableTransactionButtons();
			}
		}
	} else if(whatYouWantToDoVal == RECEIVE_SPECIAL_ADJUSTMENTS_AMOUNT_FROM_VENDORS){
		//Sunil	if(text=="Receive special adjustments amount from vendors"){
		var modeOption=$("#"+parentTr+" select[id='rsaafvpaymentdetail'] option:selected").val();
		if(modeOption=="2"){
			//get all the bank accounts related to main headquarter branch for this organization
			var txnCustomer=$("#rsaafvVendors option:selected").val();
			if(!txnCustomer==""){
				$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
				var jsonData={};
				var useremail=$("#hiddenuseremail").text();
				jsonData.usermail = useremail;
				jsonData.txnPurpose=whatYouWantToDoVal;
				var url="/branch/bankAccountsForPayment";
				$.ajax({
					url : url,
					data : JSON.stringify(jsonData),
					type : "text",
					headers:{
						"X-AUTH-TOKEN": window.authToken
					},
					method : "POST",
					contentType : 'application/json',
					success : function(data) {
						$(".dynmBnchBankActList").remove();
						$(elem).after('<div class="dynmBnchBankActList"><select name="availableBank" id="availableBank"><option value="">Select Bank</option></select></div>');
						if(data.availableBranchBankData.length>0){
							for(var i=0;i<data.availableBranchBankData.length;i++){
								$("#"+parentTr+" select[id='availableBank']").append('<option value="'+data.availableBranchBankData[i].bnchBankAccountsId+'">'+data.availableBranchBankData[i].bnchBankAccountsName+'</option>')
							}
							addBankInstrumentDetail(parentTr);
						}else{
							$(".dynmBnchBankActList").remove();
							swal("Bank account is not configured for the branch for which you want to process the transaction.");
							$(elem).find('option:first').prop("selected","selected");
						}
					},
					error: function(xhr, status, error) {
						if(xhr.status == 401){ doLogout();
						}else if(xhr.status == 500){
				    		swal("Error on fetching bank detail!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
					}
				});
			}else{
				swal("Incomplete transaction data!", "Please select vendor from whom you want to receive special adjustment amount.", "error");
				$(elem).find('option:first').prop("selected","selected");
			}
		}
	}else if (typeof text=='undefined' || text==null || whatYouWantToDoVal == ""|| whatYouWantToDoVal == null){
		var modeOption=$("#"+parentTr+" select[id='paymentDetails'] option:selected").val();
		if(modeOption=="2"){
			var transactionEntityId=parentTr.substring(17, parentTr.length);
			if(!transactionEntityId==""){
				$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
				//get all the bank accounts related to txn entity txn branch if not txn branch exist consider headquarter branch for this organization
				var jsonData={};
				var useremail=$("#hiddenuseremail").text();
				jsonData.usermail = useremail;
				jsonData.txnEntityId=transactionEntityId;
				jsonData.txnPurpose=whatYouWantToDoVal;
				var url="/branch/bankAccountsForPayment";
				$.ajax({
					url : url,
					data : JSON.stringify(jsonData),
					type : "text",
					headers:{
						"X-AUTH-TOKEN": window.authToken
					},
					method : "POST",
					contentType : 'application/json',
					success : function(data) {
						$(".dynmBnchBankActList").remove();
						$(elem).after('<div class="dynmBnchBankActList"><select name="availableBank" id="availableBank"><option value="">Select Bank</option></select></div>');
						if(data.availableBranchBankData.length>0){
							for(var i=0;i<data.availableBranchBankData.length;i++){
								$("#"+parentTr+" select[id='availableBank']").append('<option value="'+data.availableBranchBankData[i].bnchBankAccountsId+'">'+data.availableBranchBankData[i].bnchBankAccountsName+'</option>')
							}
							addBankInstrumentDetail(parentTr);

						}else{
							$(".dynmBnchBankActList").remove();
							swal("Bank account is not configured for the branch for which you want to process the transaction.");
							$(elem).find('option:first').prop("selected","selected");
						}
					},
					error: function(xhr, status, error) {
						if(xhr.status == 401){ doLogout();
						}else if(xhr.status == 500){
				    		swal("Error on fetching bank detail!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
					}
				});
			}
		}
	}
}

function addBankInstrumentDetail(parentTr){
	$(".dynmBnchBankActList").append('</br><input placeholder="Instrument Number" type="text" class="txnInstrumentNoCls" name="txtInstrumentNumber" id="txtInstrumentNumber" maxlength="20" />');
	$(".dynmBnchBankActList").append('</br><input placeholder="Instrument Date" type="text" class="txnInstrumentDtCls" name="txtInstrumentDate" id="txtInstrumentDate" class="datepicker" />');

	$("#"+parentTr+" input[name='txtInstrumentDate']").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
			$(this).focus();
		}
	});
}

function completeAccounting(elem){
	disableTransactionButtons();
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var parentTr = $(elem).closest('tr').attr('id');   //var parentTr=$(elem).parent().parent().attr('id'); Sunil
	var txnReferenceNo = $("#"+parentTr+" td:first p").text();
	var selectedAction="4";
	var transactionEntityId=parentTr.substring(17, parentTr.length);
	var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var paymentOption=$("#"+parentTr+" select[id='paymentDetails'] option:selected").val();
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
	txnJsonData.txnReferenceNo = txnReferenceNo;

	if(READ_PAYMODE_ON_APPROVAL != IS_READ_PAYMODE_ON_APPROVAL){
		var returnVal = getReceiptPaymentDetails(txnJsonData, parentTr);
		if(returnVal === false){
			return false;
		}
	}
	var url="/transaction/approverAction";
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
            if (data) {
				if (data.errorMessage) {
					swal("Error on submit for accounting!", data.errorMessage, "error");
                    disableTransactionButtons();
                    return false;
				}
			}
			if(typeof data.message !=='undefined' && data.message != ""){
				swal("Error!", data.message, "error");
				return false;
			}
			if(typeof data.tdsPayableSpecific!='undefined' && data.tdsPayableSpecific == 0){
	   			swal("COA: mapping missing!", "Chart of Account, TDS Payable mapping is not defined, please define and try.", "error");
	   			return false;
	   		}
	   		if(typeof data.roundupMappingFound!='undefined' && !data.roundupMappingFound){
	   			swal("Round-up: mapping missing!", "Chart of Account, Round-up mapping is not defined, please define and try.", "error");
	   			return false;
	   		}

			if(data.isvalidstock == 0){
				var stockDetail = "";
				if(data.incomeStockData){
					for(var i = 0; i < data.incomeStockData.length; i++) {
						stockDetail += data.incomeStockData[i].sellItems + " = " + data.incomeStockData[i].stockAvailable + ", ";
					}
					var stringLen = parseInt(stockDetail.length)-2;
					stockDetail = stockDetail.substring(0, stringLen);
				}
				swal("Sell item stock is insufficient!", "Please verify the transaction Items stock availabilty before proceeding: " +stockDetail, "warning");
			}

			if(data.resultantAmount < 0){
				if(data.branchBankDetailEntered === false){
					swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
					disableTransactionButtons();
					return false;
				}else{
					swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
				}
			}
			if(data.resultantCash < 0){
				swal("Insufficient balance in the cash account!","Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash, "warning");
			}

			if(data.resultantPettyCashAmount < 0){
				swal("Insufficient balance in the petty cash account!", "Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount, "warning");
			}
			//getUserTransactions(2, 100); commented on 22dec2016
			viewTransactionData(data)
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on complete accounting!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
			enableTransactionButtons();
		}
	});
}

var editTransactionOnceAllowed = function(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var parentTr = $(elem).closest('tr').attr('id');
	var transactionEntityId=parentTr.substring(17, parentTr.length);
	var jsonData = {};
	jsonData.transactionEntityId = transactionEntityId;
	var url="/transaction/showTransactionDetails";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async 		: false,
		method      : "POST",
		contentType : 'application/json',
		success     : function (data) {
			$('.newExpenseButton').click();
			$("#whatYouWantToDo").val(data.transactiondetailsData[0].transactionPurposeVal).change();
			var transactionPurposeVal = data.transactiondetailsData[0].transactionPurposeVal;

			var transactionTableTr = ""; //transactionPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || transactionPurposeVal == BUY_ON_CREDIT_PAY_LATER ||
			if(transactionPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT){
				editTransactionOnceAllowedSingleItem(elem, data, transactionEntityId, transactionPurposeVal);
			}else if(transactionPurposeVal == PAY_VENDOR_SUPPLIER || transactionPurposeVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
				editTransactionPay(elem, transactionEntityId, data);
			}else{
				// Below is only for multiple items
				if(transactionPurposeVal == PREPARE_QUOTATION){
					transactionTableTr = "quotattrid";
				}else if (transactionPurposeVal == PROFORMA_INVOICE) {
					transactionTableTr = "profortrid";
				}else if(transactionPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW){
					transactionTableTr = "soccpntrid";
				}else if(transactionPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
					transactionTableTr = "soccpltrid";
				}else if(transactionPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY){
					transactionTableTr = "bocpratrid";
				}else if(transactionPurposeVal == BUY_ON_CREDIT_PAY_LATER){
					transactionTableTr = "bocapltrid";
				}
				$("#"+transactionTableTr).attr("name", transactionEntityId);
				$("#" + transactionTableTr +" select[class='txnBranches']").val(data.transactiondetailsData[0].branchId).change();
				$("#" + transactionTableTr +" select[class='txnForProjects']").val(data.transactiondetailsData[0].projectID).change();
				if(!isEmpty(data.transactiondetailsData[0].customerVendorId)){
					$("#" + transactionTableTr +" .masterList").val(data.transactiondetailsData[0].customerVendorId).change();
				}else{
					resetMultiItemsTableLength(transactionTableTr);
					//resetMultiItemsTableFieldsData(elem);
					if(transactionTableTr == "soccpntrid"){
						$("#"+transactionTableTr+" input[id='soccpnUnAvailableCustomer']").val(data.transactiondetailsData[0].customerVendorName).change();
					}else{
						$("#" + transactionTableTr +" input[class='unavailable']").val(data.transactiondetailsData[0].customerVendorName).change();
					}
				}
				for (var i = 0; i < data.transactionItemdetailsData.length; i++) {
					if(i > 0){
						$("#" + transactionTableTr +" span[class='addnewItemForTransaction']").click();
						var length=$("#" + transactionTableTr +" tbody tr").length;
					}

					var itemVale = data.transactionItemdetailsData[i].itemId;
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last .txnItems").val(itemVale);
					if(i > 0){
						initMultiItemsSelect2();
					}
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnPerUnitPrice']").val(data.transactionItemdetailsData[i].pricePerUnit);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnNoOfUnit']").val(data.transactionItemdetailsData[i].noOfUnits);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDiscountPercent']").val(data.transactionItemdetailsData[i].discountPer);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnDiscountAmount']").val(data.transactionItemdetailsData[i].discountAmt);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnGross']").val(data.transactionItemdetailsData[i].grossAmount);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnTaxTypes']").val(data.transactionItemdetailsData[i].taxDescription);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='txnTaxAmount']").val(data.transactionItemdetailsData[i].totalInputTax);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='withholdingtaxcomponenetdiv']").val(data.transactionItemdetailsData[i].withholdingAmount);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='customerAdvance']").val(data.transactionItemdetailsData[i].availableAdvance);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='howMuchAdvance']").val(data.transactionItemdetailsData[i].adjFromAdvance);
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last input[class='netAmountVal']").val(data.transactionItemdetailsData[i].netAmount);
				}
				$("#" + transactionTableTr +" div[class='netAmountDescriptionDisplay']").html(data.transactionItemdetailsData[data.transactionItemdetailsData.length-1].taxDescription);

				var itemTableLength = $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr").length;
				if(data.transactionItemdetailsData.length < itemTableLength && itemTableLength > 1){
					$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:first").remove();
				}

				$("#" + transactionTableTr +" input[class='txnPoReference']").val(data.transactiondetailsData[0].poReference);
				$("#" + transactionTableTr +" input[class='netAmountValTotal']").val(data.transactiondetailsData[0].netAmount);
				$("#" + transactionTableTr +" textarea[class='voiceRemarksClass']").val(data.transactiondetailsData[0].remarks);
				if(transactionPurposeVal == PREPARE_QUOTATION || transactionPurposeVal == PROFORMA_INVOICE){
					$("#" + transactionTableTr +" textarea[class='voiceRemarksClassPrivate']").val(data.transactiondetailsData[0].remarksPrivate);
				}
				var supportingDocsList = data.transactiondetailsData[0].supportingDocs.split(',');
				for(var j=0; j<supportingDocsList.length; j++){
					var supportDocUrl = supportingDocsList[j].split('#');
					if(j == 0){
						$("#" + transactionTableTr +" input[class='txnUploadSuppDocs']").val(supportDocUrl[1]);
					}else{
						$("#" + transactionTableTr +" input[name='addMoreSupportingDocuments']").click();
						$("#" + transactionTableTr +" div[id='moreSupportingDocDiv'] input:last").val(supportDocUrl[1]);
					}
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on fetching transtion detail to Edit", "Please contact support team", "error");
	    		enableTransactionButtons();
	    	}
		},
		complete: function(data) {
			$("#whatYouWantToDo").attr("disabled", "disabled");
			//setTimeout(function(){ $.unblockUI(); },4000);
		}
	});
	setTimeout(function(){ $.unblockUI(); },4000);
}

var editTransactionPay = function(elem, transactionEntityId, data){
	$(elem).closest("tr").find('td #editTxnOnceAllowed').attr("disabled", "disabled");
	var transactionPurposeVal = data.transactiondetailsData[0].transactionPurposeVal;
	if(transactionPurposeVal == PAY_VENDOR_SUPPLIER){
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(3)").empty();
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(3)").append('<br><select class="masterList" name="mcpfcvVendors" id="mcpfcvVendors" onchange="getInvoices(this);"><option value="">--Please Select--</option><option value="'+data.transactiondetailsData[0].customerVendorId+'">'+data.transactiondetailsData[0].customerVendorName+'</option></select>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(3)").append('<br><select class="pendingTxns" name="mcpfcvpendingInvoices" id="mcpfcvpendingInvoices" onchange="getOutstandings(this);"><option value="">--Please Select--</option></select>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(3)").append('<br><b>TRANSACTION PURPOSE:</b><br><select name="whatYouWantToDo" id="whatYouWantToDo" style="width: 200px;" onchange="javascript:whatYouWantToDo(this);" readonly="readonly"><option value="7">'+data.transactiondetailsData[0].transactionPurpose+'</option></select>');

		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").empty();
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").addClass('vendcustoutstandings');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").append('<div id="mcpfcvvendcustoutstandingsgross" style="width:160px;">Invoice Value:0</div>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").append('<div id="mcpfcvvendcustoutstandingsnet" style="width:160px;">Net Payable:0</div>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").append('<div id="mcpfcvvendcustoutstandingsnetdescription" style="width:160px;">Net Payable Result:<br></div>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").append('<div id="mcpfcvvendcustoutstandingspaid" style="width:160px;">Amount Paid:0</div>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").append('<div id="mcpfcvvendcustoutstandingsnotpaid" style="width:160px;">Amount Payable:0</div>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").append('<div id="mcpfcvvendcustoutstandingspurchasereturn" style="width:160px;">Purchase Return:0</div>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").append('<div id="mcpfcvtxninprogress" style="width:160px;"></div></div></td>');

        $('#transactionEntity'+transactionEntityId+' div[class="txnWorkflowRemarks"]').append('<p style="color: blue;"> EditTransaction allowed only once. This transaction was originally created on '+data.transactiondetailsData[0].transactionDate+'</p><br/>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(10)").append('<br/><input type="button" value="Resubmit For Approval" id="resubmitForApproval" class="btn btn-submit btn-idos" onclick="resubmitForApproval(this)" style="margin-top:10px;margin-left: 0px;">');

		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").empty();
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").append('<input type="text" placeholder="Advance available" style="margin-bottom: 10px;" class="customerAdvance" name="mcpfcvvendoradvance" id="mcpfcvvendoradvance" readonly="readonly">');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").append('<input class="creditadvanceadjustment" placeholder="Advance to adjust" type="text" name="mcpfcvhowmuchfromadvance" id="mcpfcvhowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="return whenAdvanceIsEmpty(this)">');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").append('<b>Amount you wish to Pay:</b><br><input type="text" placeholder="Amount" style="margin-bottom: 10px;" class="paymentreceivedmade" name="mcpfcvpaymentreceived" id="mcpfcvpaymentreceived" onkeypress="return onlyDotsAndNumbers(event)" onblur="validatePayment(this);" onkeyup="validatePayment(this);">');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").append('<b>Tax you must withhold:</b><br><div class="vendorActPayment">Pay Vendor:0</div>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").append('<b>Balance Due On This Invoice:</b><br><input type="text" placeholder="Amount" class="dueBalance" name="mcpfcvduebalance" id="mcpfcvduebalance" onkeypress="return onlyDotsAndNumbers(event)" readonly="readonly">');
	}else if(transactionPurposeVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(3)").empty();
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(3)").append('<br><select style="width: 160px;" class="masterList" name="pcafcvVendors" id="pcafcvVendors" onchange="populatecustvendspecifics(this);"><option value="">--Please Select--</option><option value="'+data.transactiondetailsData[0].customerVendorId+'">'+data.transactiondetailsData[0].customerVendorName+'</option></select>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(3)").append('<b>Select Item:</b><br><select style="width: 200px;" class="masterListItems" name="pcafcvItems" id="pcafcvItems" onchange="getAdvanceTxnItemParent(this);"><option value="">--Please Select--</option></select>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(3)").append('<br><b>TRANSACTION PURPOSE:</b><br><select name="whatYouWantToDo" id="whatYouWantToDo" style="width: 200px;" onchange="javascript:whatYouWantToDo(this);" readonly="readonly"><option value="8">'+data.transactiondetailsData[0].transactionPurpose+'</option></select>');

		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").empty();
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(6)").append('<br><b>PURPOSE OF ADVANCE:</b><br><textarea placeholder="Details" style="width: 80px;" rows="8" class="advancePurpose" name="pcafcvadvancepurpose" id="pcafcvadvancepurpose"></textarea>');

		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").empty();
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").append('<b>Existing Advance:</b><br><div class="customerVendorExistingAdvance" style="width:152px;"></div>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").append('<b>Amount of Advance Being Paid:</b><br><input placeholder="Advance Received" style="width: 180px;" type="text" name="pcafcvAmountOfAdvanceReceived" id="pcafcvAmountOfAdvanceReceived" onkeypress="return onlyDotsAndNumbers(event)" onblur="showResultantAdvance(this);">');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(7)").append('<b>Resultant Advance:</b><br><div class="resultantAdvance" style="width:142px;"></div>');

		$('#transactionEntity'+transactionEntityId+' div[class="txnWorkflowRemarks"]').append('<p style="color: blue;"> Edit Transaction allowed only once. This transaction was originally created on '+data.transactiondetailsData[0].transactionDate+'</p><br/>');
		$("#transactionTable tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(10)").append('<br/><input type="button" value="Resubmit For Approval" id="resubmitForApproval" class="btn btn-submit btn-idos" onclick="resubmitForApproval(this)" style="margin-top:10px;margin-left: 0px;">');
	}
}

function clarifyProvisionTransaction(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var selectedAction="6";
	var transactionEntityId=parentTr.substring(26, parentTr.length);
	var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	if(confirm("Are You Sure,You Made Necessary Clarification About Transaction!")){
		var selectedAddApproverVal="";var paymentBank="";
		var txnJsonData={};
		txnJsonData.useremail=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		var url="/transactionProvision/approverAction";
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
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
	    			swal("Error on complete accounting!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}else{
		$("#"+parentTr+" textarea[id='txnRemarks']").focus();
	}
}

function clarifyTransaction(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var selectedAction="6";
	var transactionEntityId=parentTr.substring(17, parentTr.length);
	var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var txnReferenceNo = $("#"+parentTr+" td:first p").text();
	if(confirm("Are You Sure,You Made Necessary Clarification About Transaction!")){
		var selectedAddApproverVal="";var paymentBank="";
		var txnJsonData={};
		txnJsonData.useremail=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		txnJsonData.txnReferenceNo = txnReferenceNo;
		var url="/transaction/approverAction";
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
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
	    			swal("Error on complete accounting!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}else{
		$("#"+parentTr+" textarea[id='txnRemarks']").focus();
	}
}

function pettyCashTransaction(elem){
		if ($(elem).val() == "3") {
      $(elem)
        .parents("td")
        .children("div")
        .children(".userForAdditionalApprovalClass")
        .css("display", "block");
    } else {
      $(elem)
        .parents("td")
        .children("div")
        .children(".userForAdditionalApprovalClass")
        .find("option:first")
        .prop("selected", "selected");
      $(elem)
        .parents("td")
        .children("div")
        .children(".userForAdditionalApprovalClass")
        .css("display", "none");
    }
	var parentTr=$(elem).parent().parent().attr('id');
	var transactionPurposeWithText=$("#"+parentTr+" td:nth-child(3)").text();
	var n=transactionPurposeWithText.lastIndexOf(":");
	var transactionPurpose=transactionPurposeWithText.substring(n+1, transactionPurposeWithText.length);
	if(transactionPurpose=="Buy on Petty Cash Account"){
		var transactionEntityId=parentTr.substring(17, parentTr.length);
		var txnJsonData={};
		txnJsonData.useremail=$("#hiddenuseremail").text();
		txnJsonData.transactionPrimId=transactionEntityId;
		var url="/transaction/proceedingPettyTxnApproval";
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
				var currentBranchPettyCashAmount=data.currentBranchPettyCashAccountData[0].currentBranchResultantPettyCashAmount;
				var currentTransactionNetAmount=data.currentBranchPettyCashAccountData[0].currentTransactionNetAmount;
				if(parseFloat(currentTransactionNetAmount)>parseFloat(currentBranchPettyCashAmount)){
					$(elem).find('option:first').prop("selected","selected");
					swal("Insufficient amount in the branch petty cash account!", "cannot proceed with the transaction.Available Amount=" +parseFloat(currentBranchPettyCashAmount), "error");
					return true;
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
	    			swal("Error on Submit for Approval!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}

		});
	}
}

function reimbursementApproverAction(parentTr){
	$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$(".approverAction").attr("disabled", "disabled");
	var selectedAction=$("#"+parentTr+" select[id='claimapproverActionList'] option:selected").val();
	var transactionEntityId=parentTr.substring(23, parentTr.length);

	var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var selectedAddApproverVal="";
	if(selectedAction==""){
		alert("Please choose your next action from the Approver action list");
		$(".btn-custom").removeAttr("disabled");
		$(".btn-customred").removeAttr("disabled");
		$(".approverAction").removeAttr("disabled");
		$("#completeTxn").removeAttr("disabled");
		return false;
	}
	if(selectedAction=="3"){
		if(selectedAddApproverVal==""){
			alert("Please choose the user to whom you want to send for additional approval");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return false;
		}else{
			var txnJsonData={};
			txnJsonData.email=$("#hiddenuseremail").text();
			txnJsonData.selectedApproverAction=selectedAction;
			txnJsonData.transactionPrimId=transactionEntityId;
			txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
			txnJsonData.suppDoc=supportingDoc;
			txnJsonData.txnRmarks=remarks;
			var url="/reimbursement/reimbursementApproverAction";
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
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		}
	}else{
		//send server for action to complete
		var txnJsonData={};
		txnJsonData.email=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		var url="/reimbursement/reimbursementApproverAction";
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
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}

/*$(document).ready(function(){
	$('.newDownloadCompanyButton'). click(function(){
		var jsonData = {};
		jsonData.usermail = $("#hiddenuseremail").text();
		var url="/transaction/getCompanyTemplate";
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
				var url=data.txncompanytemplate[0].templateurl;
				if(url!=null && url!=""){
					downloadfile(url);
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
		    		swal("Error on downloading company template!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	});
});
*/
$(document).ready(function(){
	$('.newDownloadCompanyButton'). click(function(){
	    $("#companyTemplateDiv").attr('data-toggle', 'modal');
		$("#companyTemplateDiv").modal('show');
		var jsonData = {};
		jsonData.usermail = $("#hiddenuseremail").text();
		var url="/transaction/getCompanyTemplate";
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
				var orgdocument=data.txncompanytemplate[0].templateurl;

				$('#companyTemplateDiv select[id="companyTemplateList"]').children().remove();
				$('#companyTemplateDiv select[id="companyTemplateList"]').append('<option value="">Select to view Files</option>');
				var fileURLWithUser=orgdocument.substring(0,orgdocument.length).split(',');
				for(var j=0;j<fileURLWithUser.length;j++){
						var fileURLWithoutUser=fileURLWithUser[j].substring(0, fileURLWithUser[j].length).split('#');

						if(fileURLWithoutUser.length < 2){
							if(fileURLWithoutUser[0] != "") {
								var fileName=getFileStat('', fileURLWithoutUser[0],"companyTemplateDiv","companyTemplateList");
							}
						}else{
							if(fileURLWithoutUser[0] != "" && fileURLWithoutUser[1] != "") {
								var fileName=getFileStat(fileURLWithoutUser[0],fileURLWithoutUser[1],"companyTemplateDiv","companyTemplateList");
							}
						}
					}

				/*if(url!=null && url!=""){
					downloadfile(url);
				}*/
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
		    		swal("Error on downloading company template!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	});
});

function showTransactionBranchKnowledgeLiabrary(elem) {
	let returnValue = true;
	//$(".dynmBnchBankActList").remove();
	/* not needed for PWC as submit for approval time user will set payment details
	$("#socpnreceiptdetail").find('option:first').prop("selected","selected");
	$("#rcpfccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rcafccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rsaafvpaymentdetail").find('option:first').prop("selected","selected");
	$("#paymentDetails").find('option:first').prop("selected","selected");
	*/
	//$('#createExpense input[type="text"]').val("");
	//$('#createExpense textarea').val("");
	$(".itemParentNameDiv").text("");
	$(".inventoryItemInStock").text("");
	$(".customerVendorExistingAdvance").text("");
	$(".resultantAdvance").text("");
	$(".discountavailable").text("");

	$(".budgetDisplay").text("");
	$(".actualbudgetDisplay").text("");
	$(".branchAvailablePettyCash").html("");
	$(".inputtaxbuttondiv").html("");
	$(".inputtaxcomponentsdiv").html("");
	$(".vendorActPayment").text("");
	$(".withholdingtaxcomponentdiv").text("");
	$(".individualtaxdiv").text("");
	$(".individualtaxformuladiv").text("");
	$("#bocaplunits").removeAttr("readonly");
	$("#bocpraunits").removeAttr("readonly");
	$(".amountRangeLimitRule").text("");
	$("select[name='procurementRequestForCreator'] option:first").prop("selected","selected");
	let text=$("#whatYouWantToDo").find('option:selected').text();
	let txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	let parentTr=$(elem).parent().parent('tr:first').attr('id');

	$("#"+parentTr+" .netAmountDescriptionDisplay").text("");
	if(parentTr.indexOf("transactionEntity")!=-1){
		text = $("#"+parentTr+" select[id='whatYouWantToDo'] option:selected").text();
	}else{
		if(elem.id=="bocpraTxnForBranches"){
			$("#"+parentTr+" .txnItems option:selected").val("");
		}
	}
	//var txnitemSpecifics=$("#"+parentTr+" .txnItems option:selected").val();
	//var txnitemSpecifics = $("#"+parentTr+" .txnItems").val();
	let txnitemSpecifics=$("#"+parentTr+" .txnItems option:selected").val();
	let txnDate=$("#"+parentTr).find(".txnBackDate").val();
	let txnBranch="";
	let parentOfparentTr="";
	if(txnPurposeVal == PROFORMA_INVOICE ) {
		parentOfparentTr = $(elem).parents().closest('tr').attr('id');
		txnBranch=$("#"+parentOfparentTr+" select[class='txnBranches'] option:selected").val();
	}else if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT || txnPurposeVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER || txnPurposeVal == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || txnPurposeVal == PREPARE_QUOTATION || txnPurposeVal == PURCHASE_ORDER || txnPurposeVal == BILL_OF_MATERIAL || txnPurposeVal == CREATE_PURCHASE_REQUISITION || txnPurposeVal == CREATE_PURCHASE_ORDER || txnPurposeVal == MATERIAL_ISSUE_NOTE || txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR ){
		parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
		txnBranch=$("#"+parentOfparentTr+" select[class='txnBranches'] option:selected").val();
	}else{
		txnBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
		$("#"+parentTr+" div[class='klBranchSpecfTd']").text("");
		//$(".masterList").children().remove();
		//$(".masterList").append('<option value="">--Please Select--</option>');
		//$(".txnPerUnitPrice").val("");
		//$(".txnFrieghtCharges").val("");
	}
	if(txnBranch!="" && txnitemSpecifics!=""){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		let jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.userTxnPurposeText=text;
		jsonData.txnBranchId=txnBranch;
		jsonData.txnSpecificsId=txnitemSpecifics;
		jsonData.txnPurposeValue = txnPurposeVal;
		jsonData.txnDate = txnDate;
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
			if(txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT) {
				if (data.gstRateSelected != "") {
					var gstRateArray = data.gstRateSelected.split(",");
					var gstData = '<option value="">Select</option>';
					$("#" + parentTr + " select[class='txnGstTaxRate']").html("");
					$("#" + parentTr + " select[class='txnGstTaxRate']").append(BRANCH_INGSTTAXES_LIST_GLOBAL);
					if (gstRateArray.length > 0) {
						for (var i = 0; i < gstRateArray.length; i++) {
							$("#" + parentTr + " .txnGstTaxRate option").each(function () {
								if (parseFloat($(this).text()) == parseFloat(gstRateArray[i])) {
									gstData += '<option value="' + $(this).val() + '">' + $(this).text() + '</option>';
								}
							});
						}
						$("#" + parentTr + " .txnGstTaxRate").html(gstData);
					}
				}

				if (data.cessRateSelected != "") {
					var cessRateArray = data.cessRateSelected.split(",");
					var cessData = '<option value="">Select</option>';
					$("#" + parentTr + " select[class='txnCessRate']").html("");
					$("#" + parentTr + " select[class='txnCessRate']").append(BRANCH_INCESSTAXES_LIST_GLOBAL);
					if (cessRateArray.length > 0) {
						for (var i = 0; i < cessRateArray.length; i++) {
							$("#" + parentTr + " .txnCessRate option").each(function () {
								if (parseFloat($(this).text()) == parseFloat(cessRateArray[i])) {
									cessData += '<option value="' + $(this).val() + '">' + $(this).text() + '</option>';
								}
							});
						}
						$("#" + parentTr + " .txnCessRate").html(cessData);
					}
				}
			}
			$("#"+parentTr+" div[class='klBranchSpecfTd']").text("");
			if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
	    	  		if(data.incomeStockData[0].inventory=="InventoryItem"){
						$(".inventoryItemInStock").text("In Stock: "+data.incomeStockData[0].stockAvailable);
						$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<b>Current Inventory: '+data.incomeStockData[0].stockAvailable+"</b><br/>");
					}else{
						$(".inventoryItemInStock").text("");
					}
					if(data.isItemTaxInclusive == 1) {
						$("#"+parentTr+" input[class='txnPerUnitPrice']").hide();
						$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").show();

						//$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").keyup();
					}else {
						$("#"+parentTr+" input[class='txnPerUnitPrice']").show();
						$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").hide();
					}
					if(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers!=""){
						for(var i=0;i<data.txnBranchSpecificsKLData.length;i++){
							var klcount=i+1;
							var itemUnitPrice = data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers;
							if(i==0){
						  		if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
							  		var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
							  		if(typeof followedkl=='undefined'){
								  		$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
							  		}
							  		$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
						  		}else if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
								  	var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
								  	if(typeof followedkl=='undefined'){
										$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
								 	}
								  	$("#"+parentTr+" div[class='klBranchSpecfTd']").append(''+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
							  	}
							}else{
						  		if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
			    				  	var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
			    				  	if(typeof followedkl=='undefined'){
			    						$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
			    				  	}
			    				  	$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
								}else if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
								  	var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
								  	if(typeof followedkl=='undefined'){
										$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
								  	}
								  	$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
								}
			    		  	}
							// $("#soccpnpriceperunits")
							$("#"+parentTr+" input[class='txnPerUnitPrice']").val(itemUnitPrice);
							if(data.isItemTaxInclusive == 1) {
								$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").val(itemUnitPrice);
								$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").keyup();
								$("#"+parentTr+" input[class='txnPerUnitPrice']").hide();
								$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").show();

							}else {
								$("#"+parentTr+" input[class='txnPerUnitPrice']").show();
								$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").hide();
							}
							//$("#"+parentTr+" input[id='soccpnnetamnt']").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							//$("#soccpnnetamntTotal").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							//$("#"+parentTr+" input[id='soccpngross']").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							$("#"+parentOfparentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);
			    	  	}
						/*  $("#soccpnCustomer").children().remove();
						$("#soccpnCustomer").append('<option value="">--Please Select--</option>');
						for(var i=0;i<data.txnBranchSpecificsCustomerData.length;i++){
							$("#soccpnCustomer").append('<option value="'+data.txnBranchSpecificsCustomerData[i].customerId+'">'+data.txnBranchSpecificsCustomerData[i].customerName+'</option>');
						}*/
	    		  	}
	    		  	//else{ confirmed by Venkat on 22nd May 2017
						//swal("This Item Is Currently Not Available For Sale!", "Please select another item", "error");
						//$(elem).find("option:first").prop("selected","selected");
						//$(elem).select2('destroy').val("").select2();
						//returnValue=false;
	    			//}
	    		}else if(txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
	    			if(data.isItemTaxInclusive == 1) {
						$("#"+parentTr+" input[class='txnPerUnitPrice']").css("display", "none");
						$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").css("display", "inherit");
						$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").keyup();
					}else {
						$("#"+parentTr+" input[class='txnPerUnitPrice']").css("display", "inherit");
						$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").css("display", "none");
					}
					if(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers!=""){
						for(var i=0;i<data.txnBranchSpecificsKLData.length;i++){
							var klcount=i+1;
							if(i==0){
								if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
									var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
									if(typeof followedkl=='undefined'){
										$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
									}
									$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
								}
								if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
									$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
								}
							}else{
								if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
									var followedkl=$("#"+parentOfparentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
									if(typeof followedkl=='undefined'){
										$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
									}
									$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
								}
								if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
									$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
								}
							}
							$("#"+parentTr+" input[id='soccplpriceperunits']").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							if(data.isItemTaxInclusive == 1) {
								$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
								$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").keyup();
								$("#"+parentTr+" input[class='txnPerUnitPrice']").css("display", "none");
								$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").css("display", "inherit");
							}else {
								$("#"+parentTr+" input[class='txnPerUnitPrice']").css("display", "inherit");
								$("#"+parentTr+" input[class='txnPerUnitPriceTaxInclusice']").css("display", "none");
							}
							//$("#"+parentTr+" input[id='soccplgross']").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							//$("#"+parentTr+" input[id='soccplnetamnt']").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							//$("#soccplnetamntTotal").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							$("#"+parentOfparentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);
						}

					}
						//else{
						//swal("This Item Is Currently Not Available For Sale!", "Please select another item", "error");
						//$(elem).find("option:first").prop("selected","selected");
						//$(elem).select2('destroy').val("").select2();
						//returnValue=false;
					//}
    			}else if(txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT){
    				var itemUnitPrice="";
    				if(data.incomeStockData[0].inventory=="BuyInventoryItem"){
						$(".inventoryItemInStock").text("In Stock: "+data.incomeStockData[0].stockAvailable);
						$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<b>Current Inventory: '+data.incomeStockData[0].stockAvailable+"</b><br/>");
					}else{
						$(".inventoryItemInStock").text("");
					}
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

			    		$("#"+parentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);
			    	}
		    		/*if(data.txnSpecificsInputTaxesData[0].isitemallowsinputtaxes=="1"){
		    			  $("#"+parentTr+" div[class='inputtaxbuttondiv']").append('<input type="button" class="btn btn-submit btn-idos input-small" name="inputtaxes" id="inputtaxes" value="Input Tax" style="margin-top:10px; width: 60px; padding: 5px 7px 5px 7px;" onclick="addTaxComponents(this);"><input type="text" class="txnInputTaxAmount" name="inputtaxesamt" id="inputtaxesamt" placeholder="Amount" style="float:right; margin-top:10px; width: 70px;" readonly="readonly">');
						   $("#noOfInputTaxesAdd").val(0);
		    		}*/


		    		$("#"+parentTr+" input[id='bocprapriceperunits']").val(itemUnitPrice);
					//$("#"+parentTr+" input[id='bocpranetamnt']").val(itemUnitPrice);
					//$("#bocpranetamntTotal").val(itemUnitPrice);
					//$("#"+parentTr+" input[id='bocpragross']").val(itemUnitPrice);
					$("#"+parentOfparentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);

    			  //$("#bocpraVendor").children().remove();
		    	  //$("#bocpraVendor").append('<option value="">--Please Select--</option>');
    			 /*( $("#"+parentTr+" select[id='bocpraVendor']").children().remove();
    			  $("#"+parentTr+" select[id='bocpraVendor']").append('<option value="">--Please Select--</option>');
		    	  for(var i=0;i<data.txnBranchSpecificsCustomerData.length;i++){
		    		  $("#bocpraVendor").append('<option value="'+data.txnBranchSpecificsCustomerData[i].customerId+'">'+data.txnBranchSpecificsCustomerData[i].customerName+'</option>');
		    		  $("#"+parentTr+" select[id='bocpraVendor']").append('<option value="'+data.txnBranchSpecificsCustomerData[i].customerId+'">'+data.txnBranchSpecificsCustomerData[i].customerName+'</option>');
		    	  }*/

					$("#"+parentOfparentTr+" div[class='actualbudgetDisplay']").append('Budget Allocated For This Month: '+data.txnBudgetData[0].monthBudgetForBranchTxnSpecific);
					$("#"+parentOfparentTr+" div[class='budgetDisplay']").append('Budget Available For This Month: '+data.txnBudgetData[0].monthBudgetAvailableForBranchTxnSpecific);
					$("#"+parentOfparentTr+" div[class='amountRangeLimitRule']").append('Transaction Creation Limit From: '+data.txnBudgetData[0].userTxnAmountFrom);
					$("#"+parentOfparentTr+" div[class='amountRangeLimitRule']").append(' To: '+data.txnBudgetData[0].userTxnAmountTo);

					//set hidden variables for item budget
					$("#"+parentTr+" input[id='bocpraactualbudgetDis']").val(data.txnBudgetData[0].monthBudgetForBranchTxnSpecific);
					$("#"+parentTr+" input[id='bocprabudgetDisplay']").val(data.txnBudgetData[0].monthBudgetAvailableForBranchTxnSpecific);
					$("#"+parentTr+" class[id='amountRangeFromLimit']").val(data.txnBudgetData[0].userTxnAmountFrom);
					$("#"+parentTr+" class[id='amountRangeToLimit']").val(data.txnBudgetData[0].userTxnAmountTo);
    	  		}
	    		else if(txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT){
	    			var itemUnitPrice =0;
    				if(data.txnBranchSpecificsKLData.length>0 && data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers!=""){
    					itemUnitPrice = data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers;
    				}
		    		for(var i=0;i<data.txnBranchSpecificsKLData.length;i++){
		    			var klcount=i+1;
			    		if(i==0){
			    			if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
			    				var followedkl=$("#"+parentOfparentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
			    				if(typeof followedkl=='undefined'){
			    					$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
			    				}
			    				$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
			    			}else if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
			    				$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
			    			}
			    		}else{
			    			if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
			    				var followedkl=$("#"+parentOfparentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
			    				if(typeof followedkl=='undefined'){
			    					  $("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
			    				}
			    				$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
			    			}
			    			if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
			    				  $("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
			    			}
			    		}
			    		$("#"+parentOfparentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);
			    	}

		    		$("#"+parentTr+" input[id='bptycapriceperunits']").val(itemUnitPrice);
					$("#"+parentTr+" input[id='bptycanetamnt']").val(itemUnitPrice);
					$("#bptycanetamntTotal").val(itemUnitPrice);
					$("#"+parentTr+" input[id='bptycagross']").val(itemUnitPrice);
					$("#"+parentOfparentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);

		    		/*if(data.txnSpecificsInputTaxesData[0].isitemallowsinputtaxes=="1"){
		    			  $("#"+parentOfparentTr+" div[class='inputtaxbuttondiv']").append('<input type="button" class="btn btn-submit btn-idos input-small" name="inputtaxes" id="inputtaxes" value="Input Tax" style="margin-top:10px; width: 60px; padding: 5px 7px 5px 7px;" onclick="addTaxComponents(this);"><input type="text" class="txnInputTaxAmount" name="inputtaxesamt" id="inputtaxesamt" placeholder="Amount" style="float:right; margin-top:10px; width: 70px;" readonly="readonly">');
						   $("#noOfInputTaxesAdd").val(0);
		    		}
					$("#bptycaVendor").children().remove();
					$("#bptycaVendor").append('<option value="">--Please Select--</option>');
					for(var i=0;i<data.txnBranchSpecificsCustomerData.length;i++){
						$("#bptycaVendor").append('<option value="'+data.txnBranchSpecificsCustomerData[i].customerId+'">'+data.txnBranchSpecificsCustomerData[i].customerName+'</option>');
					}*/
					for(var k=0;k<data.branchPettyCashTxnData.length;k++){
						if(data.branchPettyCashTxnData[k].approvalRequired=="0"){
							$(".branchAvailablePettyCash").html("Approval Needed:No");
							$(".branchAvailablePettyCash").append("<br/>Available Petty Cash="+data.branchPettyCashTxnData[k].resultantPettyCash);
						}
						if(data.branchPettyCashTxnData[k].approvalRequired=="1"){
							$(".branchAvailablePettyCash").html("Approval Needed:Yes");
							$(".branchAvailablePettyCash").append("<br/>Limit="+data.branchPettyCashTxnData[k].approvalAmountLimit);
							$(".branchAvailablePettyCash").append("<br/>Available Petty Cash="+data.branchPettyCashTxnData[k].resultantPettyCash);
						}
					}
					$("#"+parentOfparentTr+" div[class='actualbudgetDisplay']").append('Budget Allocated For This Month: '+data.txnBudgetData[0].monthBudgetForBranchTxnSpecific);
					$("#"+parentOfparentTr+" div[class='budgetDisplay']").append('Budget Available For This Month: '+data.txnBudgetData[0].monthBudgetAvailableForBranchTxnSpecific);
					$("#"+parentOfparentTr+" div[class='amountRangeLimitRule']").append('Transaction Creation Limit From: '+data.txnBudgetData[0].userTxnAmountFrom);
					$("#"+parentOfparentTr+" div[class='amountRangeLimitRule']").append(' To: '+data.txnBudgetData[0].userTxnAmountTo);

    	  		}else if(txnPurposeVal == BUY_ON_CREDIT_PAY_LATER){
    	  			var itemUnitPrice=0;
    	  			if(data.txnBranchSpecificsKLData.length>0 && data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers!=""){
    					itemUnitPrice = data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers;
    				}
    		  		for(var i=0;i<data.txnBranchSpecificsKLData.length;i++){
						var klcount=i+1;
						if(i==0){
							if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
								var followedkl=$("#"+parentOfparentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
								if(typeof followedkl=='undefined'){
									$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
								}
								$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
							}
							if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
								$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
							}
						}else{
							if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
								var followedkl=$("#"+parentOfparentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
								if(typeof followedkl=='undefined'){
									$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
								}
								$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
							}
							if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
								$("#"+parentOfparentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
							}
						}
						$("#"+parentOfparentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);
					}
					/*if(data.txnSpecificsInputTaxesData[0].isitemallowsinputtaxes=="1"){
						$("#"+parentTr+" div[class='inputtaxbuttondiv']").append('<input type="button" class="btn btn-submit btn-idos input-small" name="inputtaxes" id="inputtaxes" value="Input Tax" style="margin-top:10px; width: 60px; padding: 5px 7px 5px 7px;" onclick="addTaxComponents(this);">&nbsp;<input type="text" class="txnInputTaxAmount" name="inputtaxesamt" id="inputtaxesamt" placeholder="Amount" style="float:right; margin-top:10px; width: 70px;" readonly="readonly">');
						$("#noOfInputTaxesAdd").val(0);
					}
					if(parentTr.indexOf("transactionEntity")!=-1){ //For EditTransaction we are having bocpravendor as dropdown list irrespective of tranPurpose
						$("#"+parentTr+" select[id='bocpraVendor']").children().remove();
						$("#"+parentTr+" select[id='bocpraVendor']").append('<option value="">--Please Select--</option>');
						for(var i=0;i<data.txnBranchSpecificsCustomerData.length;i++){
							// $("#bocpraVendor").append('<option value="'+data.txnBranchSpecificsCustomerData[i].customerId+'">'+data.txnBranchSpecificsCustomerData[i].customerName+'</option>');
							$("#"+parentTr+" select[id='bocpraVendor']").append('<option value="'+data.txnBranchSpecificsCustomerData[i].customerId+'">'+data.txnBranchSpecificsCustomerData[i].customerName+'</option>');
						}
					}else{
						$("#bocaplVendor").children().remove();
						$("#bocaplVendor").append('<option value="">--Please Select--</option>');
						$("#"+parentTr+" div[class='inputtaxbuttondiv']")
						for(var i=0;i<data.txnBranchSpecificsCustomerData.length;i++){
							$("#bocaplVendor").append('<option value="'+data.txnBranchSpecificsCustomerData[i].customerId+'">'+data.txnBranchSpecificsCustomerData[i].customerName+'</option>');
						}
					}*/
					$("#"+parentTr+" input[id='bocaplpriceperunits']").val(itemUnitPrice);
					$("#"+parentTr+" input[id='bocaplnetamnt']").val(itemUnitPrice);
					$("#bocaplnetamntTotal").val(itemUnitPrice);
					$("#"+parentTr+" input[id='bocaplgross']").val(itemUnitPrice);
					$("#"+parentOfparentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);

					$("#"+parentOfparentTr+" div[class='actualbudgetDisplay']").append('Budget Allocated For This Month:<br/>'+data.txnBudgetData[0].monthBudgetForBranchTxnSpecific);
					$("#"+parentOfparentTr+" div[class='budgetDisplay']").append('Budget Available For This Month:<br/>'+data.txnBudgetData[0].monthBudgetAvailableForBranchTxnSpecific);
					$("#"+parentOfparentTr+" div[class='amountRangeLimitRule']").append('Transaction Creation Limit<br/> From:<br/>'+data.txnBudgetData[0].userTxnAmountFrom);
					$("#"+parentOfparentTr+" div[class='amountRangeLimitRule']").append('<br/>To:<br/>'+data.txnBudgetData[0].userTxnAmountTo);

					//set hidden variables for item budget
					$(".bocaplactualbudgetDis").val(data.txnBudgetData[0].monthBudgetForBranchTxnSpecific);
					$(".bocaplbudgetDisplay").val(data.txnBudgetData[0].monthBudgetAvailableForBranchTxnSpecific);
					$(".bocaplamountRangeLimit").val(data.txnBudgetData[0].userTxnAmountFrom+","+data.txnBudgetData[0].userTxnAmountTo);

					$("#"+parentTr+" input[id='bocaplactualbudgetDis']").val(data.txnBudgetData[0].monthBudgetForBranchTxnSpecific);
					$("#"+parentTr+" input[id='bocaplbudgetDisplay']").val(data.txnBudgetData[0].monthBudgetAvailableForBranchTxnSpecific);
					$("#"+parentTr+" input[id='bocaplamountRangeLimit']").val(data.txnBudgetData[0].userTxnAmountFrom+","+data.txnBudgetData[0].userTxnAmountTo);
				}else if(parseInt(txnPurposeVal)== PREPARE_QUOTATION || parseInt(txnPurposeVal)== PROFORMA_INVOICE || parseInt(txnPurposeVal)== PURCHASE_ORDER){
	    		  	if(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers!=""){
						for(var i=0;i<data.txnBranchSpecificsKLData.length;i++){
							var klcount=i+1;
							var itemUnitPrice = data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers;
							var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
						  	if(typeof followedkl=='undefined'){
								$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
						 	}
					  		if(data.txnBranchSpecificsKLData[i].klIsMandatory=="1"){
						  		$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+klcount+'<i class="icon-star"></i>'+data.txnBranchSpecificsKLData[i].klContent+'.');
					  		}else if(data.txnBranchSpecificsKLData[i].klIsMandatory=="0"){
							  	$("#"+parentTr+" div[class='klBranchSpecfTd']").append(''+klcount+''+data.txnBranchSpecificsKLData[i].klContent+'.');
						  	}
							$("#"+parentTr+" input[class='txnPerUnitPrice']").val(itemUnitPrice);
							//$("#quotationnetamntTotal").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							$("#"+parentTr+" input[class='txnGross']").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
							$("#"+parentTr+" input[class='netAmountValTotal']").val(data.txnBranchSpecificsKLData[0].specificsunitpriceforcustomers);
			    	  	}
						if(data.incomeStockData[0].inventory=="InventoryItem"){
							$(".inventoryItemInStock").text("In Stock: "+data.incomeStockData[0].stockAvailable);
						}else{
							$(".inventoryItemInStock").text("");
						}
	    		  	}
	    		  	//else{
					//	swal("This Item Is Currently Not Available For Quotation!", "Please select another item", "error");
						//$(elem).find("option:first").prop("selected","selected");
					//	$(elem).select2('destroy').val("").select2();
					//	returnValue=false;
	    			//}
	    		}else if(txnPurposeVal == BILL_OF_MATERIAL || txnPurposeVal == CREATE_PURCHASE_REQUISITION || txnPurposeVal == CREATE_PURCHASE_ORDER || txnPurposeVal == MATERIAL_ISSUE_NOTE){
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

						$("#"+parentTr+" div[class='itemParentNameDiv']").text(data.txnBranchSpecificsKLData[0].itemParentName);
					}
				}
    	  		//$("#"+parentTr+" input[class='txnNoOfUnit']").val("0");
	      	},
	      	error: function (xhr, status, error) {
	      		if(xhr.status == 401){ doLogout();
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
   return returnValue;
}

function populateProjectItemBasedOnTxnPurposeBranchSelection(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	$("#branchCashAvailable").val("");
	$("#branchPettyCashAvailable").val("");
	$("#transferPurpose").val("");
	$("#branchCashResultant").val("");
	$("#branchPettyCashResultant").val("");
	$(".budgetDisplay").text("");
	$(".actualbudgetDisplay").text("");
	$(".branchAvailablePettyCash").html("");
	$(".txnTypeOfSupply").val("");
	$(".amountRangeLimitRule").text("");
	$(".inputtaxbuttondiv").html("");
	$(".inputtaxcomponentsdiv").html("");
	$(".vendorActPayment").text("");
	$(".withholdingtaxcomponentdiv").text("");
	$(".individualtaxdiv").text("");
	//$("#staticmultipleitemspcafcv").find("table tbody").html("");
	$("#pcafnetAmountPaid").val("0");
	$(".netPaid").val("0");

	$("#"+parentTr+" select[name='pcafcvVendors']").change();
	$("select[name='procurementRequestForCreator'] option:first").prop("selected","selected");
	$("#"+parentTr+" select[class='placeOfSply txnDestGstinCls'] option:first").prop("selected","selected");
	$("#"+parentTr+" select[id='srtfccInvoices'] option:first").prop("selected","selected");
	var txnPurposeBranchId=$(elem).val();

	var jsonData = {};
	var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	var text=$("#whatYouWantToDo").find('option:selected').text();

	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnPurposeId=transactionPurposeId;
	jsonData.txnPurposeBnchId=txnPurposeBranchId;
	if(txnPurposeBranchId==""){
		populateItemBasedOnWhatYouWantToDo(transactionPurposeId,text);
	}else if(txnPurposeBranchId!=""){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var url="/transaction/getTxnPurposePjctItemOnBranch";
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
	    		var projectListTemp = "";
	    		if(transactionPurposeId != TRANSFER_MAIN_CASH_TO_PETTY_CASH){
	    			for(var i=0;i<data.allTxnProjectPurposeData.length;i++){
						projectListTemp += ('<option value="'+data.allTxnProjectPurposeData[i].id+'">'+data.allTxnProjectPurposeData[i].name+'</option>');
					}
				}
				if(transactionPurposeId== SELL_ON_CASH_COLLECT_PAYMENT_NOW){ //Sell on cash & collect payment now
					$("#allTxnProjectPurposeData").children().remove();
					$("#allTxnProjectPurposeData").append('<option value="">--Please Select--</option>');
					$("#allTxnProjectPurposeData").append(projectListTemp);
				}else if(transactionPurposeId== SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){//Sell on credit & collect payment later
					$("#soccplTxnForProjects").children().remove();
					$("#soccplTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#soccplTxnForProjects").append(projectListTemp);
				}else if(transactionPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transactionPurposeId == BUY_ON_CREDIT_PAY_LATER || transactionPurposeId == BUY_ON_PETTY_CASH_ACCOUNT || transactionPurposeId == SALES_RETURNS || transactionPurposeId == PURCHASE_RETURNS || transactionPurposeId == CREDIT_NOTE_VENDOR || transactionPurposeId == DEBIT_NOTE_VENDOR){
					$("#" + parentTr +" select[class='txnForProjects']").children().remove();
					$("#" + parentTr +" select[class='txnForProjects']").append('<option value="">--Please Select--</option>');
					$("#" + parentTr +" .txnItems").children().remove();
					$("#" + parentTr +" .txnItems").append('<option value="">--Please Select--</option>');
					$("#" + parentTr +" .masterList").children().remove();
					$("#" + parentTr +" .masterList").append('<option value="">--Please Select--</option>');
					$("#" + parentTr +" select[class='txnForProjects']").append(projectListTemp);
					if(transactionPurposeId == BUY_ON_PETTY_CASH_ACCOUNT) {
						 for(var i=0;i<data.branchCashPettyAccountData.length;i++){
			    			$("#" + parentTr +" select[class='txnBranches']").attr("branchPettyCashAvailable",data.branchCashPettyAccountData[i].resultantPettyCash);
		    		 	}
					}

		    	}else if(text=="Transfer main cash to petty cash"){
		    		 for(var i=0;i<data.branchCashPettyAccountData.length;i++){
		    			 $("#branchCashAvailable").val(data.branchCashPettyAccountData[i].resultantCash);
		    			 $("#branchPettyCashAvailable").val(data.branchCashPettyAccountData[i].resultantPettyCash);
		    		 }
					 $("#branchTransferrableAmount").val("");
					 $("#tmtpcaRemarks").val("");
		    	}else if(transactionPurposeId == PREPARE_QUOTATION){// quotation
					$("#quotationTxnForProjects").children().remove();
					$("#quotationTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#quotationTxnForProjects").append(projectListTemp);
				}else if(transactionPurposeId == PROFORMA_INVOICE){
					$("#proformaTxnForProjects").children().remove();
					$("#proformaTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#proformaTxnForProjects").append(projectListTemp);
				}else if(transactionPurposeId == CREDIT_NOTE_CUSTOMER || transactionPurposeId == DEBIT_NOTE_CUSTOMER){
					$("#cdtdbtTxnForProjects").children().remove();
					$("#cdtdbtTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#cdtdbtTxnForProjects").append(projectListTemp);
				}else if(transactionPurposeId == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
					$("#" + parentTr +" select[class='txnForProjects']").children().remove();
					$("#" + parentTr +" select[class='txnForProjects']").append('<option value="">--Please Select--</option>');
					$("#" + parentTr +" .txnItems").children().remove();
					$("#" + parentTr +" .txnItems").append('<option value="">--Please Select--</option>');
					$("#" + parentTr +" select[class='txnForProjects']").append(projectListTemp);
				}else if(transactionPurposeId == CANCEL_INVOICE){
					$("#caninvTxnForProjects").children().remove();
					$("#caninvTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#caninvTxnForProjects").append(projectListTemp);
				}else if(transactionPurposeId == BILL_OF_MATERIAL || transactionPurposeId == CREATE_PURCHASE_REQUISITION || transactionPurposeId == CREATE_PURCHASE_ORDER){
					$("#"+parentTr+" select[class='txnForProjects']").children().remove();
					$("#"+parentTr+" select[class='txnForProjects']").append('<option value="">--Please Select--</option>');
					$("#"+parentTr+" select[class='txnForProjects']").append(projectListTemp);
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
		    		swal("Error on transaction items!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				if(isSingleUserDeploy == "true" && (transactionPurposeId != CREDIT_NOTE_CUSTOMER || transactionPurposeId != DEBIT_NOTE_CUSTOMER || transactionPurposeId != CREDIT_NOTE_VENDOR || transactionPurposeId != DEBIT_NOTE_VENDOR)) {
					$("#"+parentTr+" select[class='txnTypeOfSupply']").val("1");
					$("#"+parentTr+" select[class='txnTypeOfSupply']").trigger("change");
				}
				$.unblockUI();
			}
		});
	}
}

function resubmitForApproval(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var whatYouWantToDo = $("#"+parentTr+" select[id='whatYouWantToDo'] option:selected").text();
	var whatYouWantToDoVal = $("#"+parentTr+" select[id='whatYouWantToDo'] option:selected").val();
	var withholdingTaxValues = $("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").val();
	var txnLeftOutWithholdTransIDs = $("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val();
/*
	if(whatYouWantToDo == "Buy on cash & pay right away" || whatYouWantToDo=="Buy on credit & pay later"){
		var txnForCustomer=$("#"+parentTr+" select[id='bocpraVendor']").val();  //$("#bocpraVendor option:selected").val()
		var txnNoOfUnits=$("#"+parentTr+" input[class='txnNoOfUnit']").val();
		var txnPricePerUnit=$("#"+parentTr+" input[class='txnPerUnitPrice']").val();
		var txnGross=$("#"+parentTr+" input[class='txnGross']").val();
		var txnRemarks=$("#"+parentTr+" div[class='txnWorkflowRemarks']").text();
		var txnNetAmount=$("#"+parentTr+" input[class='netAmountVal']").val();   //$("#soccpnnetamnt").val();
		var txnNetAmountDescription=$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").text();  //$("#netAmountLabel").text();
		var txnCustomerAdvanceIfAny=$("#"+parentTr+" input[class='customerAdvance']").val(); //$("#bocpravendoradvance").val();
		var txnCustomerAdvanceAdjustment=$("#"+parentTr+" input[class='txnHowMuchFromAdvance']").val(); //$("#bocprahowmuchfromadvance").val();
		var txnFrieghtCharges=$("#"+parentTr+" input[class='txnFrieghtCharges']").val();
		var txnInputTaxesNames="";
		var txnInputTaxesValues="";
		var inputTaxAmt=$("#"+parentTr+" input[name='inputtaxesamt']").attr('id');
		if(typeof inputTaxAmt!='undefined'){
			txnInputTaxesNames=$(".dynminputtaxcomponentsdiv").text();
			txnInputTaxesValues=$("#"+parentTr+" input[name='bocprainputtaxcomponenetamt']").map(function () {
		 		return this.value;
		 	}).get().toString();
		}
		var txnJsonData={};
		txnJsonData.txnPurpose=whatYouWantToDo;
		txnJsonData.txnPurposeVal=whatYouWantToDoVal;
		txnJsonData.transactionEntityId=parentTr.substring(17, parentTr.length);
		txnJsonData.txnForCustomer=txnForCustomer;
	    txnJsonData.txnnoofunits=txnNoOfUnits;
		txnJsonData.txnpriceperunit=txnPricePerUnit;
		txnJsonData.txnFrieghtCharges = txnFrieghtCharges;
		txnJsonData.txngross=txnGross;
		txnJsonData.txnRemarks=txnRemarks;
		txnJsonData.txnnetamount=txnNetAmount;
		txnJsonData.txnnetamountdescription=txnNetAmountDescription;
		txnJsonData.txnCustomerAdvanceIfAny=txnCustomerAdvanceIfAny;
		txnJsonData.txnCustomerAdvanceAdjustment=txnCustomerAdvanceAdjustment;
		txnJsonData.txnInputTaxesNames=txnInputTaxesNames;
		txnJsonData.txnInputTaxesValues=txnInputTaxesValues;
		txnJsonData.txnwithholdingTaxValues=withholdingTaxValues;
		txnJsonData.txnLeftOutWithholdTransIDs=txnLeftOutWithholdTransIDs;
		txnJsonData.useremail=$("#hiddenuseremail").text();
		var url="/transaction/resubmitForApproval";
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
				getUserTransactions(2, 100);		//will load user transactions, so new transaction is visible...tried appending only this transaction to existing transactionTable, but no all data available
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}*/
	if(whatYouWantToDo == "Pay vendor/supplier"){
		var creditVendor="";var txnInvoice="";var outstandings="";var txnVendorAdvanceIfAny="";var txnVendorAdvanceAdjustment;var paymentReceived="";
		var balanceOnThatInvoice="";var txnRemarks="";var actPayToVend="";
		creditVendor=$("#"+parentTr+" select[id='mcpfcvVendors'] option:selected").val();
		txnInvoice=$("#"+parentTr+" select[id='mcpfcvpendingInvoices'] option:selected").val();
		outstandings=$("#"+parentTr+" div[id='mcpfcvvendcustoutstandingsgross']").text()+","+$("#"+parentTr+" div[id='mcpfcvvendcustoutstandingsnet']").text()+","+$("#"+parentTr+" div[id='mcpfcvvendcustoutstandingsnetdescription']").text()+","+$("#"+parentTr+" div[id='mcpfcvvendcustoutstandingspaid']").text()+","+$("#"+parentTr+" div[id='mcpfcvvendcustoutstandingsnotpaid']").text()+","+$("#"+parentTr+" div[id='mcpfcvvendcustoutstandingspurchasereturn']").text();
		txnVendorAdvanceIfAny=$("#"+parentTr+" input[id='mcpfcvvendoradvance']").val();
		txnVendorAdvanceAdjustment=$("#"+parentTr+" input[id='mcpfcvhowmuchfromadvance']").val();
		paymentReceived=$("#"+parentTr+" input[id='mcpfcvpaymentreceived']").val();
		balanceOnThatInvoice=$("#"+parentTr+" input[id='mcpfcvduebalance']").val();
		txnRemarks=$("#"+parentTr+" div[class='txnWorkflowRemarks']").text();
		actPayToVend=$("#"+parentTr+" div[class='vendorActPayment']").text();
		if(balanceOnThatInvoice==""){
			swal("Incomplete transaction data!", "Please provide complete pay vendor/supplier details before submitting for approval", "error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}else{
			var proceed=false;

			var userCheckRule=checkForOnlyConfiguredApproverPVS(txnInvoice,paymentReceived,"payvendorsupplier");
			if(userCheckRule==true){
				proceed=true;
			}
			if(userCheckRule==false){
				proceed=false;
			}
			if(proceed){
				var txnJsonData={};
				txnJsonData.txnPurpose=whatYouWantToDo;
				txnJsonData.txnPurposeVal=whatYouWantToDoVal;
				txnJsonData.transactionEntityId=parentTr.substring(17, parentTr.length);
				txnJsonData.creditMCPFCVVendor=creditVendor;
				txnJsonData.txnMCPFCVInvoice=txnInvoice;
				txnJsonData.txnMCPFCVoutstandings=outstandings;
				txnJsonData.txnMCPFCVVendorAdvanceIfAny=txnVendorAdvanceIfAny;
				txnJsonData.txnMCPFCVVendorAdvanceAdjustment=txnVendorAdvanceAdjustment;
				txnJsonData.txnMCPFCVpaymentReceived=paymentReceived;
				txnJsonData.txnMCPFCVpaymentDue=balanceOnThatInvoice;
				txnJsonData.txnactPayToVend=actPayToVend;
				txnJsonData.txnremarks=txnRemarks;
				txnJsonData.useremail=$("#hiddenuseremail").text();
				var url="/transaction/resubmitForApproval";
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
						//getUserTransactions(0, 20);
					},
					error: function (xhr, status, error) {
						if(xhr.status == 401){ doLogout(); }
					}
				});
			}
		}
	}
	if(whatYouWantToDo=="Pay advance to vendor or supplier"){
		var expenseItem="";var creditVendor="";var purposeOfAdvance="";var amountOfAdvance="";
		var txnRemarks="";
		expenseItem=$("#"+parentTr+" select[id='pcafcvItems'] option:selected").val();
		creditVendor=$("#"+parentTr+" select[id='pcafcvVendors'] option:selected").val();
		purposeOfAdvance=$("#"+parentTr+" textarea[id='pcafcvadvancepurpose']").val();
		amountOfAdvance=$("#"+parentTr+" input[id='pcafcvAmountOfAdvanceReceived']").val();
		txnRemarks=$("#"+parentTr+" div[class='txnWorkflowRemarks']").text();

		if(expenseItem=="" || creditVendor=="" || amountOfAdvance==""){
			swal("Incomplete transaction data!", "Please provide complete Pay advance to vendor or supplier transaction details before submitting for approval.", "error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}else{
			var proceed=false;
			var userCheckRule=checkForOnlyConfiguredApproverPATVS(expenseItem,amountOfAdvance,"payadvancetovendorsupplier");
			if(userCheckRule==true){
				proceed=true;
			}
			if(userCheckRule==false){
				proceed=false;
			}
			if(proceed){
				var txnJsonData={};
				txnJsonData.txnPurpose=whatYouWantToDo;
				txnJsonData.txnPurposeVal=whatYouWantToDoVal;
				txnJsonData.transactionEntityId=parentTr.substring(17, parentTr.length);
				txnJsonData.txnPCAFCVExpenseItem=expenseItem;
				txnJsonData.txnPCAFCVCreditVendor=creditVendor;
				txnJsonData.txnPCAFCVPurposeOfAdvance=purposeOfAdvance;
				txnJsonData.txnPCAFCVAmountOfAdvance=amountOfAdvance;
				txnJsonData.txnremarks=txnRemarks;
				txnJsonData.txnwithholdingTaxValues=withholdingTaxValues;
				txnJsonData.txnLeftOutWithholdTransIDs=txnLeftOutWithholdTransIDs;
				txnJsonData.useremail=$("#hiddenuseremail").text();
				var url="/transaction/resubmitForApproval";
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
						cancel();
						//getUserTransactions(0, 20);
					},
					error: function (xhr, status, error) {
						if(xhr.status == 401){ doLogout(); }
					}
				});
			}
		}
	}

}

function givetxnProvisionRemarks(elem){
	var parentTr = $(elem).closest('tr').attr('id');

	var transactionEntityId=parentTr.substring(26, parentTr.length);
	var useremail=$("#hiddenuseremail").text();
	var transactionRmarks=$("#transactionTable tr[id='"+parentTr+"'] textarea[name='txnRemarks']").val();
	var jsonData = {};
	jsonData.transactionPrimId = transactionEntityId;
	jsonData.useremail = useremail;
	jsonData.txnRmarks=transactionRmarks;
	jsonData.suppDoc="";
	jsonData.selectedApproverAction="7";
	if(transactionRmarks!=""){
		var url="/transactionProvision/approverAction";
		$.ajax({
			url : url,
			data : JSON.stringify(jsonData),
			type : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method : "POST",
			contentType : 'application/json',
			success : function(data) {
			},
			error: function(xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
		    		swal("Error on adding  remarks!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
}

function givetxnRemarks(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var txnReferenceNo = $(elem).closest('tr').attr('txnref');
	var transactionEntityId=parentTr.substring(17, parentTr.length);
	var useremail=$("#hiddenuseremail").text();
	var transactionRmarks=$("#transactionTable tr[id='"+parentTr+"'] textarea[name='txnRemarks']").val();
	var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var jsonData = {};
	jsonData.transactionPrimId = transactionEntityId;
	jsonData.useremail = useremail;
	jsonData.txnRmarks=transactionRmarks;
	jsonData.suppDoc=supportingDoc;
	jsonData.selectedApproverAction="7";
	jsonData.txnReferenceNo = txnReferenceNo;
	if(transactionRmarks!=""){
	var url="/transaction/approverAction";
		$.ajax({
			url : url,
			data : JSON.stringify(jsonData),
			type : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method : "POST",
			contentType : 'application/json',
			success : function(data) {
				/*$('#transactionEntity'+transactionEntityId+' div[class="txnWorkflowRemarks"]').append('<h1auditor><b>'+useremail+"#"+transactionRmarks+'</b></h1auditor>#');
				$("#transactionTable tr[id='"+parentTr+"'] textarea[name='txnRemarks']").val("Enter Auditor/Other Remark");*/
				swal("Remark added", "Transaction Remark has been added successfully.", "success");
			},
			error: function(xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
	    			swal("Error on adding transaction remark!", "Please retry, if problem persists contact support team", "error");
	    		}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
}

//also used in proforma invoice

function calculateNetAmountForSell(elem){
	var returnValue = true;
	var parentTr = $(elem).closest('tr').attr('id');
	var parentOfparentTr = $("#"+parentTr).parent().parent().parent().parent().closest('div').attr('id');
	var parentTable = $(elem).parents().closest('table').attr('id');

	var unitPrice = $("#"+parentTr+" input[class='txnPerUnitPrice']").val();
	var noOfUnits = $("#"+parentTr+" input[class='txnNoOfUnit']").val();

	if(unitPrice === "0" || unitPrice === "" || unitPrice === null || noOfUnits === "0" || noOfUnits === "" || noOfUnits === null){
		return false;
	}
    $("#" + parentTr + " .netAmountDescriptionDisplay").text("");
	$(".withholdingtaxcomponenetdiv").val("");
	$("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val("");
	//var text=$("#whatYouWantToDo").find('option:selected').text();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	var txnUnavailable=$("#"+parentOfparentTr+" input[class='unavailable']").val();
	if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW){
		txnUnavailable=$("#"+parentOfparentTr+" input[id='soccpnUnAvailableCustomer']").val();
	}
	var txnAdjustmentAmount=$("#"+parentTr+" input[class='howMuchAdvance']").val();
	var txnBranch=$("#"+parentOfparentTr+" select[class='txnBranches']").val();
	var txnVendorCustomer=$("#"+parentOfparentTr+" .masterList").val();	//$(".masterList option:selected").val();
	var txnitemSpecifics=$("#"+parentTr+" .txnItems").val();
	if(txnPurposeVal == SALES_RETURNS){
		var orgNoOfUnits = $("#"+parentTr+" input[id='srtfccOriginalNoOfUnitsHid']").val();
		var noOfUnits = $("#"+parentTr+" input[id='srtfccunits']").val();
		if(noOfUnits!="" && parseInt(noOfUnits) > parseInt(orgNoOfUnits)){
			swal("Returning quantity cannot be greater than Original Units " +orgNoOfUnits);
			$("#"+parentTr+" input[id='srtfccunits']").val("");
			returnValue=false;
		}
		var txnGrossAmountTmp = $("#"+parentTr+" input[class='txnGross']").val();
	}else{
		var txnGrossAmountTmp = $("#"+parentTr+" input[class='txnGross']").val();
	}
	var proceedSalesTxn=false;
	if(txnBranch==""){
		swal("Incomplete transaction detail!", "Please select a Branch.", "error");
		returnValue=false;
	}
	var txnTypeOfSupply = $("#"+parentOfparentTr+" select[class='txnTypeOfSupply']").val();
	if(txnTypeOfSupply=="" && txnPurposeVal != TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
		swal("Incomplete transaction detail!", "Please select a Type of Supply.", "error");
		returnValue=false;
	}
	var txnWithWithoutTax = $("#"+parentOfparentTr+" select[class='txnWithWithoutTaxCls']").val();
	if((txnTypeOfSupply =="3" || txnTypeOfSupply =="4" || txnTypeOfSupply =="5") && txnWithWithoutTax==""){
		swal("Incomplete transaction detail!", "Please select With / Without IGST.", "error");
		returnValue=false;
	}
	if(txnVendorCustomer=="" && txnUnavailable==""){
		swal("Incomplete transaction detail!", "Please select customer or provide customer name.", "error");
		returnValue=false;
	}
	if(txnitemSpecifics==""){
		swal("Incomplete transaction detail!", "Please select a Item for transaction.", "error");
		returnValue=false;
	}
	if(txnGrossAmountTmp==""){
		//swal("Incomplete transaction detail!", "Please provide price/unit to calaculate Gross Amount.", "error");
		$("#"+parentTr+" input[class='netAmountVal']").val(0);
		$("#"+parentOfparentTr+" div[class='netAmountDescriptionDisplay']").html("");
		$("#"+parentOfparentTr+" input[class='netAmountValTotal']").val("");
		returnValue=false;
	}
	var sourceGstinCode = $("#"+parentOfparentTr+" select[class='txnBranches']").children(":selected").attr("id");
	var destGstinCode = $("#"+parentOfparentTr+" select[class='placeOfSply txnDestGstinCls']").val();
	var destCustDetailId = $("#"+parentOfparentTr+" select[class='placeOfSply txnDestGstinCls']").children(":selected").attr("id");
	if(txnVendorCustomer === "" && txnUnavailable !== ""){
		var txnWalkinCustomerType = $("#"+parentOfparentTr+" select[class='walkinCustType']").val();
		if(txnWalkinCustomerType == ""){
			swal("Invalid Type of Customer!", "Please select valid Type of customer.", "error");
			returnValue=false;
		}else if(txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2"){
			destGstinCode = $("#"+parentOfparentTr+" input[class='placeOfSplyTextHid']").val();
		}else if(txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4"){
			destGstinCode = sourceGstinCode;
		}else if(txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6"){
			destGstinCode = $("#"+parentOfparentTr+" select[name='txnWalkinPlcSplySelect']").val();
		}
	}
    if(txnPurposeVal == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
        destGstinCode = $("#"+parentOfparentTr+" select[class='txnBranchesTo']").children(":selected").attr("id");
    }
	$("#"+parentOfparentTr+" input[class='netAmountValTotal']").val("");
	if(returnValue === true){
		var txnDate = $("#"+parentOfparentTr).find(".txnBackDate").val();
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		//jsonData.userTxnPurposeText=text;
		jsonData.txnBranchId=txnBranch;
		jsonData.txnSpecificsId=txnitemSpecifics;
		jsonData.txnGrossAmt=txnGrossAmountTmp;
		jsonData.txnSelectedVendorCustomer=txnVendorCustomer;
		jsonData.txnPurposeValue = txnPurposeVal;
		jsonData.txnSourceGstinCode = sourceGstinCode;
		jsonData.txnDestGstinCode = destGstinCode;
		jsonData.txnDestCustDetailId = destCustDetailId;
		jsonData.txnTypeOfSupply = txnTypeOfSupply;
		jsonData.txnWithWithoutTax = txnWithWithoutTax;
		jsonData.noOfUnits = noOfUnits;
		jsonData.txnDate = txnDate;
		var url="/transaction/calculateNetAmount";
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
			   	if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER || txnPurposeVal == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER ){
                    if(txnPurposeVal == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").html("");
					}
				    var taxNameList = "";
				    var i=0;
				    var txnItemsDecription = ""; //var txnIndividualtaxdiv = ""; var txnIndividualtaxformuladiv = "";
		    		for(i=0;i<data.branchSpecificsTaxComponentData.length;i++){
                        txnItemsDecription = txnItemsDecription + data.branchSpecificsTaxComponentData[i].individualTax + ", ";
                        /*if(i==0){
		    				$("#"+parentTr+" div[class='individualtaxdiv']").text(data.branchSpecificsTaxComponentData[i].individualTax);
		    				$("#"+parentTr+" div[class='individualtaxformuladiv']").text(data.branchSpecificsTaxFormulaComponentData[i].individualTaxFormula);
		    			}else{
		    				var existingIndividualTaxComp=$("#"+parentTr+" div[class='individualtaxdiv']").text();
		    				var existingIndividualTaxFormulaComp=$("#"+parentTr+" div[class='individualtaxformuladiv']").text();
		    				$("#"+parentTr+" div[class='individualtaxdiv']").text(existingIndividualTaxComp+","+data.branchSpecificsTaxComponentData[i].individualTax);
		    				$("#"+parentTr+" div[class='individualtaxformuladiv']").text(existingIndividualTaxFormulaComp+","+data.branchSpecificsTaxFormulaComponentData[i].individualTaxFormula);
		    			}*/
		    			//taxNameList += '<div class="taxNameHead">'+data.branchSpecificsTaxComponentData[i].taxName+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
		    			var taxCellData = "";
		    			var elementPrefix = "";
		    			if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW){
                            elementPrefix = "soccpn";
                        }else if(txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
		    				elementPrefix = "soccpl";
                        }else if(txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER){
		    				elementPrefix = "cdtdbt";
						}else if(txnPurposeVal == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
                            elementPrefix = "tifbtb";
						}
						taxCellData = '<div class="txnTaxNameCls">'+data.branchSpecificsTaxComponentData[i].taxName + '</div><input type="text" class="taxRate" name="'+elementPrefix+'TaxRate" id="'+elementPrefix+'TaxRate" readonly placeholder="Tax Rate" value="'+data.branchSpecificsTaxComponentData[i].taxRate+'"/>';
							if(data.branchSpecificsTaxComponentData[i].taxName == "CESS") {
								taxCellData += '<input type="text" style="width:62px;" class="txnTaxAmount" name="'+elementPrefix+'taxamnt" id="'+elementPrefix+'taxamnt" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);"  placeholder="Tax Amount" value="'+data.branchSpecificsTaxComponentData[i].taxAmount+'"/>';
							}else {
							    taxCellData += '<input type="text" style="width:62px;" class="txnTaxAmount" name="'+elementPrefix+'taxamnt" id="'+elementPrefix+'taxamnt" readonly  placeholder="Tax Amount" value="'+data.branchSpecificsTaxComponentData[i].taxAmount+'"/>';
							}
						taxCellData+='<input type="hidden" class="txnTaxName" name="'+elementPrefix+'TaxName" id="'+elementPrefix+'TaxName" value="'+data.branchSpecificsTaxComponentData[i].taxName+'"/>';
						taxCellData+='<input type="hidden" class="txnTaxID" name="' + elementPrefix + 'TxnTaxID" id="' + elementPrefix + 'TxnTaxID" value="' + data.branchSpecificsTaxComponentData[i].taxid + '" formula="'+ data.branchSpecificsTaxComponentData[i].taxFormulaId + '"/>';

						$("#"+parentTr+ " div[id='taxCell"+i+"']").addClass('taxCellCls');
						$("#"+parentTr+ " div[id='taxCell"+i+"']").empty();
		    			$("#"+parentTr+ " div[id='taxCell"+i+"']").append(taxCellData);
		    		}

                    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append(txnItemsDecription);

		    		if(data.branchSpecificsTaxComponentData.length <= 0){
						$("#"+parentTr+ " div[id='taxCell0']").empty();
						$("#"+parentTr+ " div[id='taxCell1']").empty();
						$("#"+parentTr+ " div[id='taxCell2']").empty();
						$("#"+parentTr+ " div[id='taxCell3']").empty();
						$("#"+parentTr+ " div[id='taxCell0']").removeClass();
						$("#"+parentTr+ " div[id='taxCell1']").removeClass();
						$("#"+parentTr+ " div[id='taxCell2']").removeClass();
						$("#"+parentTr+ " div[id='taxCell3']").removeClass();
						$("#"+parentTr+" input[class='txnTaxTypes']").val('');
						$("#"+parentTr+ " input[class='itemTaxAmount']").val('');
                        }
		    		var taxTotalAmount=0;
		    		if(data.branchSpecificsTaxResultAmountData.length > 0){
		    			taxTotalAmount = parseFloat(data.branchSpecificsTaxResultAmountData[0].taxTotalAmount);
		    		}
                    var taxTypeAndRate = $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").text();
                    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").append(' Net Tax:' + (taxTotalAmount * 1).toFixed(2) + ",");


		    		$("#"+parentTr+ " input[class='itemTaxAmount']").val(taxTotalAmount);
		    		var gross=$("#"+parentTr+" input[class='txnGross']").data('txnGross');
	    			var invoiceValue=parseFloat(gross);
		    		if(taxTotalAmount!="" && gross != ""){
		    			invoiceValue=(invoiceValue+parseFloat(taxTotalAmount));
		    		}
	    			$("#"+parentTr+" input[class='invoiceValue']").val((invoiceValue*1).toFixed(2));
	    			var netAmount = invoiceValue;
                    if(typeof txnAdjustmentAmount != 'undefined' && txnAdjustmentAmount != null && txnAdjustmentAmount != "" && parseFloat(txnAdjustmentAmount) > 0){
                    	netAmount = (parseFloat(invoiceValue)-parseFloat(txnAdjustmentAmount));
                        $("#"+parentTr+" div[class='netAmountDescriptionDisplay']").append(' Adjustment:'+txnAdjustmentAmount);
                    }
		    		$("#"+parentTr+" input[class='txnTaxTypes']").val(taxTypeAndRate);
		    		$("#"+parentTr+" input[class='netAmountVal']").val((netAmount*1).toFixed(2));
		    		let netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
		    		$("#"+parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));
		    		var totalInvoiceValue = calInvoiceValueTotalForMultiItems(elem);
		    		$("#"+parentOfparentTr + " input[class='totalInvoiceValue']").val(Math.round(totalInvoiceValue).toFixed(2));
		    	}else if(txnPurposeVal == PROFORMA_INVOICE){
					calcNetAmtProforma(elem, data, parentTr);
				}else if(txnPurposeVal == SALES_RETURNS){
					calculateNetAmtForSalesRetTransaction(elem,data,parentTr)
				}
			},
			error: function (xhr, status, error) {
		   		if(xhr.status == 401){ doLogout();
		   		}else if(xhr.status == 500){
		    		swal("Error on calaculate net amount!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
	return returnValue;
}

//Transaction is allowed to edit once, calaculate net and tax based on new gross
function calculateNetAmountForEditedTransaction(elem, transactionPurpose, txnVendorCustomer, txnBranch, txnitemSpecifics, parentTr){
	$("#"+parentTr+" input[class='netAmountVal']").val("");
	$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").text("");
	$(".withholdingtaxcomponenetdiv").val("");
	$("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val("");
	$(".netAmountVal").val("");
	var txnGrossAmount=$("#"+parentTr+" input[class='txnGross']").val(); //get newly calculated gross amount
	var transactionEntityId=parentTr.substring(17, parentTr.length);
	if(transactionPurpose=="Buy on cash & pay right away" || transactionPurpose=="Buy on credit & pay later" || transactionPurpose=="Buy on Petty Cash Account"){
		var err=checkAmountRangeLimitForTransactionCreator(elem,txnGrossAmount,parentTr);
		if(err==true){
			return true;
		}
	}
	var txnAdjustmentAmount=$("#"+parentTr+" input[class='txnHowMuchFromAdvance']").val();
	var txnInputTaxAmt=$("#"+parentTr+" input[name='inputtaxesamt']").attr('id');
	var txnTotalInputTaxes=$("#"+parentTr+" input[name='inputtaxesamt']").val();
	var txnFrieghtCharges=$("#"+parentTr+" input[class='txnFrieghtCharges']").val();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.userTxnPurposeText=transactionPurpose;
	jsonData.txnBranchId=txnBranch;
	jsonData.txnSpecificsId=txnitemSpecifics;
	jsonData.txnGrossAmt=txnGrossAmount;
	jsonData.txnAdjustmentAmount = txnAdjustmentAmount;
	jsonData.txnTotalInputTaxes = txnTotalInputTaxes;
	jsonData.txnSelectedVendorCustomer=txnVendorCustomer;
	jsonData.txnPurposeValue = txnPurposeVal;
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var url="/transaction/calculateNetAmount"
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
			if(transactionPurpose=="Buy on cash & pay right away" || transactionPurpose=="Buy on credit & pay later"){
				$(".netAmountDescriptionDisplay").text("");
				if(txnAdjustmentAmount!=""){
					$(".netAmountDescriptionDisplay").append('<br/>Advance Adjustment: '+txnAdjustmentAmount+",");
				}
				if(typeof txnInputTaxAmt!='undefined' && txnTotalInputTaxes != ""){
					$(".netAmountDescriptionDisplay").append('<br/>Total Input Taxes: '+txnTotalInputTaxes+'(+)'+",");
				}

				if(txnFrieghtCharges != ""){
					$(".netAmountDescriptionDisplay").append('<br/>Frieght Charges: '+txnFrieghtCharges+'(+)'+",");
				}

				for(var i=0;i<data.branchSpecificsTaxComponentPurchaseData.length;i++){
					$(".netAmountDescriptionDisplay").append('<br/>Rate: '+data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate+",");
					$(".netAmountDescriptionDisplay").append('<br/>Limit: '+data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxLimit+",");
					$(".netAmountDescriptionDisplay").append('<br/>Withholding Monetory Limit: '+data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit+",");
					var withholdingAmount=data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxTotalAmount;
					$(".netAmountDescriptionDisplay").append('<br/>Withholding Tax: '+parseFloat(withholdingAmount)+'(-)');
					$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").val(withholdingAmount);
					$("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val(data.branchSpecificsTaxComponentPurchaseData[i].txnLeftOutWithholdTransIDs);
				}
				$("#"+parentTr+" input[class='netAmountVal']").val(data.txnNetAmount);
			}
	   },
	   error: function (xhr, status, error) {
		    if(xhr.status == 401){ doLogout(); }
	   },
		complete: function(data) {
			$.unblockUI();
		}
	});
}

function checkAmountRangeLimitForTransactionCreator(elem,grossAmount, parentTr){
	var budgetAvailable=$('#'+parentTr+' input[class="budgetDisplayVal"]').val();
	var budgetAmount=parseFloat(budgetAvailable);

	var userAmountLimitRangeWithText=$('#'+parentTr+' div[class="amountRangeLimitRule"]').text();
	var userAmountLimitRange=userAmountLimitRangeWithText.substring(0,userAmountLimitRangeWithText.length).split(':');
	var amountFrom=parseFloat(userAmountLimitRange[1]);
	var amountTo=parseFloat(userAmountLimitRange[2]);
	if(parseFloat(grossAmount)>amountFrom){
		if(amountTo>0){
			if(parseFloat(grossAmount)>amountTo){
				$(elem).val("");
				$(elem).keyup();
				swal("Limit exceeded!","You cannot create transaction greater than allowed limit which is "+amountTo, "error");
				return true;
			}
		}else{
			//alert("You cannot create transaction greater than you limit which is "+amountFrom);
			$(elem).val("");
			$(elem).keyup();
			swal("Transaction limit exceeded!","You cannot create transaction greater than allowed limit which is "+amountFrom, "error");
			return true;
		}
	}
	/*var approvalRequired=true;
	if(approvalRequired==true){
		//show submit for approval button
		$("#bocprasubmitForApproval").show();
		$("#bocprasubmitForAccounting").hide();
	}
	if(approvalRequired==false){
		//show submit for accounting button
		$("#bocprasubmitForAccounting").show();
	}*/

}

function showBranchBankDetails(elem){
	var bankPrimKey=$(elem).val();
	var bankContainerId=$(elem).attr('id');
	if(bankPrimKey!=""){
		var jsonData={};
		jsonData.email =$("#hiddenuseremail").text();
		jsonData.bankId =bankPrimKey;
		jsonData.bankSelectId=bankContainerId;
		ajaxCall('/data/branchBankDetails', jsonData, '', '', '', '', 'showBranchBankDetailsSuccess', '', false);
	}else{
		$(".txnFromBranchBankDetails").html("");
		$(".txnToBranchBankDetails").html("");
		$(".txnBranchBankDetails").html("");
	}
}

function showBranchBankDetailsSuccess(data){
	if(data.result){
		data=data.branchBankDetails;
		var bankSelectId=data[0].bankSelectId;
		if(bankSelectId!=""){
			if(bankSelectId=="fromBranchBankAccounts"){
				$(".txnFromBranchBankDetails").html("");
				for(var i=0;i<data.length;i++){
					$(".txnFromBranchBankDetails").append("<b>From Bank:</b><br/><b>"+data[i].bankName+"<b/>,<br/><b>Bank Number:</b><br/><b>"+data[i].bankNumber+"<b/>,<br/><b>Amount:</b><br/><b id='originalAmount'>"+data[i].bankAmount+"<b/><br/>");
				}
				$(".branchTypeAllowsNegBal").val(data[0].branchTypeAllowsNegBal);
			}else if(bankSelectId=="toBranchBankAccounts"){
				$(".txnToBranchBankDetails").text("");
				for(var i=0;i<data.length;i++){
					$(".txnToBranchBankDetails").append("<b>To Bank:</b><br/><b>"+data[i].bankName+"<b/>,<br/><b>Bank Number:</b><br/><b>"+data[i].bankNumber+"<b/>,<br/><b>Amount:</b><br/><b id='originalAmount'>"+data[i].bankAmount+"<b/><br/>");
				}
			}else{
				$(".txnBranchBankDetails").html("");
				$(".branchTypeAllowsNegBal").val(data[0].branchTypeAllowsNegBal);
				if( bankSelectId==="branchBankAccounts"){
					for(var i=0;i<data.length;i++){
						$(".txnBranchBankDetails").append("<b>To Bank:</b><br/><b>"+data[i].bankName+"<b/>,<br/><b>Bank Number:</b><br/><b>"+data[i].bankNumber+"<b/>,<br/><b>Amount:</b><br/><b id='originalAmount'>"+data[i].bankAmount+"<b/><br/>");
					}
				}else{
					for(var i=0;i<data.length;i++){
						$(".txnBranchBankDetails").append("<b>From Bank:</b><br/><b>"+data[i].bankName+"<b/>,<br/><b>Bank Number:</b><br/><b>"+data[i].bankNumber+"<b/>,<br/><b>Amount:</b><br/><b id='originalAmount'>"+data[i].bankAmount+"<b/><br/>");
					}
				}
			}
		}
	}
}

function submitForAccounting(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	disableTransactionButtons();
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	if(whatYouWantToDoVal==SELL_ON_CASH_COLLECT_PAYMENT_NOW || whatYouWantToDoVal==SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
		submitForAccountingSell(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal==CREDIT_NOTE_CUSTOMER || whatYouWantToDoVal== DEBIT_NOTE_CUSTOMER){
		submitForAccountingNote(whatYouWantToDo, whatYouWantToDoVal, parentTr); //can't find this function (asif)
	}else if(whatYouWantToDoVal == CANCEL_INVOICE) {
		submitForAccountingCancelInvoice(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal== RECEIVE_PAYMENT_FROM_CUSTOMER){
		var creditCustomer="";var txnInvoice=""; var openingBalBillId = ""; var outstandings="";var txnCustomerAdvanceIfAny="";var txnCustomerAdvanceAdjustment;var paymentReceived="";
		var balanceOnThatInvoice=0.0;var txnReceiptDetails="";var txnreceipttypebankdetails="";var txnRemarks="";var supportingDoc="";var withHoldingTaxReceipt="";var discountAllowed="";
		var klmandatoryfollowednotfollowed="";var receiptPaymentBank="";var customeradvanceType="";var txnForBranch="";var totalDiscountAllowed=0.0;
		txnForBranch=$("#rcpfccTxnForBranches option:selected").val();
		creditCustomer=$("#rcpfccCustomers option:selected").val();
		txnInvoice=$("#rcpfccpendingInvoices option:selected").val();
		if($("#rcpfccpendingInvoices option:selected").attr('billId')) {
			openingBalBillId = $("#rcpfccpendingInvoices option:selected").attr('billId');
		}
		var txnForItem = convertTableDataToArray("rcpfccSecondTableForMultiInvoice");
		outstandings=$("#rcpfccvendcustoutstandingsgross").text()+","+$("#rcpfccvendcustoutstandingsnet").text()+","+$("#rcpfccvendcustoutstandingsnetdescription").text()+","+$("#rcpfccvendcustoutstandingspaid").text()+","+$("#rcpfccvendcustoutstandingsnotpaid").text()+","+$("#rcpfccvendcustoutstandingssalesreturn").text();
		customeradvanceType=$("#rcpfcccustomeradvanceType option:selected").val();
		txnCustomerAdvanceIfAny=$("#rcpfcccustomeradvance").val();
		txnCustomerAdvanceAdjustment=$("#rcpfcchowmuchfromadvance").val();
		paymentReceived=$("#rcpfcctotalpaymentreceived").val();
		withHoldingTaxReceipt=$("#rcpfccTotalTaxAdjusted").val();
		discountAllowed=$("#rcpfccTotalDiscountAllowed").val();
		$("#rcpfccSecondTableForMultiInvoice > tbody > tr").each(function() {
			var dueBal = $(this).find("td #rcpfccduebalance").val();
			if(dueBal != "" && dueBal !=undefined)
				balanceOnThatInvoice = parseFloat(balanceOnThatInvoice) + parseFloat(dueBal);
		});

		totalDiscountAllowed = $("#rcpfccTotalDiscountAllowed").val();
		txnRemarks=$("#rcpfccRemarks").val();
		var supportingDocTmp = $("#"+parentTr+" select[name='rcpfccuploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		supportingDoc = supportingDocTmp.join(',');

		var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
				klmandatoryfollowednotfollowed="1";
			}
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
				klmandatoryfollowednotfollowed="0";
			}
		}
		var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			var klfollowednotfollowed=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").is(':checked');
			if(klfollowednotfollowed==false){
				swal("Mandatory rules not followed!", "Please read the madatory knowledge library before submitting transaction for accounting", "error");
				enableTransactionButtons();
				return true;
			}
		}
		var txnDate = $("#"+parentTr).find(".txnBackDate").val();
		if(balanceOnThatInvoice < 0.0){
			swal("Incomplete transaction data!", "Please provide complete Receive payment from customer transaction details before submitting for accounting", "error");
			enableTransactionButtons();
			return false;
		}else{
			var txnJsonData={};
			txnJsonData.txnPurpose=whatYouWantToDo;
			txnJsonData.txnPurposeVal=whatYouWantToDoVal;
			txnJsonData.txnforbranch=txnForBranch;
			txnJsonData.creditRCPFCCCustomer=creditCustomer;
			txnJsonData.txnRCPFCCInvoice=txnInvoice;
			txnJsonData.txnRCPFCCoutstandings=outstandings;
			txnJsonData.customeradvanceType=customeradvanceType;
			txnJsonData.txnRCPFCCCustomerAdvanceIfAny=txnCustomerAdvanceIfAny;
			txnJsonData.txnRCPFCCCustomerAdvanceAdjustment=txnCustomerAdvanceAdjustment;
			txnJsonData.txnRCPFCCpaymentReceived=paymentReceived;
			txnJsonData.txnRCPFCCDiscountAllowed = totalDiscountAllowed;
			txnJsonData.txnRCPFCCwithHoldingTaxReceipt=withHoldingTaxReceipt;
			txnJsonData.txnRCPFCCdiscountAllowed=discountAllowed;
			txnJsonData.txnRCPFCCpaymentDue=balanceOnThatInvoice;

			txnJsonData.openingBalBillId = openingBalBillId;
			txnJsonData.txnForItem = txnForItem;
			txnJsonData.txnDate = txnDate;

			var returnVal = getReceiptPaymentDetails(txnJsonData, parentTr);
			if(returnVal === false){
				return false;
			}

			txnJsonData.txnremarks=txnRemarks;
			txnJsonData.supportingdoc=supportingDoc;
			txnJsonData.klfollowednotfollowed=klmandatoryfollowednotfollowed;
			txnJsonData.useremail=$("#hiddenuseremail").text();
			var url="/transaction/submitForAccounting";
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
					if(typeof data.tdsReceivableSpecific!='undefined' && data.tdsReceivableSpecific == 0){
			   			swal("COA: mapping missing!", "Chart of Account, TDS receivable mapping is not defined, please define and try.", "error");
			   			disableTransactionButtons();
			   			return false;
		   			}
		   			if(typeof data.roundupMappingFound!='undefined' && !data.roundupMappingFound){
			   			swal("Round-up: mapping missing!", "Chart of Account, Round-up mapping is not defined, please define and try.", "error");
			   			disableTransactionButtons();
			   			return false;
			   		}
					if(typeof data.message !=='undefined' && data.message != ""){
						swal("Error!", data.message, "error");
						return false;
					}
					cancel();
					viewTransactionData(data); // to render the updated transaction recored
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout();
					}else if(xhr.status == 500){
						swal("Error on Submit For Accounting!", "Please retry, if problem persists contact support team", "error");
					}
				},
				complete: function(data) {
					$.unblockUI();
					enableTransactionButtons();
				}
			});
		}
	}else if(whatYouWantToDoVal==RECEIVE_ADVANCE_FROM_CUSTOMER){
		submitForAccountingCustRec(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal==REVERSAL_OF_ITC){
		submitForAccountingRevOfITC(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal == BUY_ON_PETTY_CASH_ACCOUNT){
		var checkTDS = checkForVendorTdsSetup("multipleItemsTablebocpra");
		if(checkTDS == false) {
			swal({
				  title: 'TDS setup required',
				  text: 'TDS / withhold setup not done for this vendor',
				  type: 'warning',
				  showCancelButton: true,
				  confirmButtonClass: "btn-success",
				  confirmButtonText: "Proceed",
				  cancelButtonText: "Cancel",
				  closeOnConfirm: false,
				  closeOnCancel: false
				},
				function(isConfirm) {
				  if (isConfirm) {
					  swal.close();
					  submitForApprovalBuyTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
				  } else {
					  swal.close()
					  enableTransactionButtons();
					  return true;
				  }
				});
			}else{
				submitForApprovalBuyTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
			}
	}
	$.unblockUI();
	enableTransactionButtons();
}

$(document).ready(function() {
	$('.submitForAccounting').click(function(){
		var parentTrId=$(this).attr('id');
		var parentTr=parentTrId.substring(0,6)+"trid";
		var whatYouWantToDo=$("#whatYouWantToDo").find('option:selected').text();
		var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
		
		submitForAccounting(whatYouWantToDo,whatYouWantToDoVal,parentTr);
	 });
});
$(document).ready(function() {
	$('.btn-approve').click(function(){
		var txnId=$(elem).attr('id');
		var actionText=$(elem).text();
		TransactionAction(actionText);
		
	});
});

function submitForApproval(whatYouWantToDo,whatYouWantToDoVal,parentTr){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	disableTransactionButtons();
	if(whatYouWantToDoVal == BUY_ON_CASH_PAY_RIGHT_AWAY || whatYouWantToDoVal == BUY_ON_CREDIT_PAY_LATER || whatYouWantToDoVal == BUY_ON_PETTY_CASH_ACCOUNT){
		var checkTDS = true;
		if(whatYouWantToDoVal == BUY_ON_CASH_PAY_RIGHT_AWAY) {
			checkTDS = checkForVendorTdsSetup("multipleItemsTablebocpra");
		}else if(whatYouWantToDoVal == BUY_ON_CREDIT_PAY_LATER) {
			checkTDS = checkForVendorTdsSetup("multipleItemsTablebocpra");
		}else if(whatYouWantToDoVal == BUY_ON_PETTY_CASH_ACCOUNT) {
			checkTDS = checkForVendorTdsSetup("multipleItemsTablebocpra");
		}
		if(checkTDS == false) {
			swal({
				title: 'TDS setup required',
				text: 'TDS / withhold setup not done for this vendor',
				type: 'warning',
				showCancelButton: true,
				confirmButtonClass: "btn-success",
				confirmButtonText: "Proceed",
				cancelButtonText: "Cancel",
				closeOnConfirm: false,
				closeOnCancel: false
			},
			function(isConfirm) {
				if (isConfirm) {
				  swal.close();
				  submitForApprovalBuyTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
				} else {
				  swal.close()
				  enableTransactionButtons();
				  return true;
				}
			});
		}else{
			submitForApprovalBuyTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
		}
	}else if(whatYouWantToDoVal == CREDIT_NOTE_CUSTOMER || whatYouWantToDoVal == DEBIT_NOTE_CUSTOMER){
		submitForApprovalNoteTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal == CREDIT_NOTE_VENDOR || whatYouWantToDoVal == DEBIT_NOTE_VENDOR){
		submitForApprovalVendNoteTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
    }else if(whatYouWantToDoVal == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
        submitForApprovalInvTransferTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal == PAY_VENDOR_SUPPLIER){
		var openingBalBillId = "";
		var balanceOnThatInvoice = 0.0;
		var txnForBranch = $("#mcpfcvTxnForBranches option:selected").val();
		var creditVendor = $("#mcpfcvVendors option:selected").val();
		var txnInvoice = $("#mcpfcvpendingInvoices option:selected").val();
		if($("#mcpfcvpendingInvoices option:selected").attr('billId')) {
			openingBalBillId  =  $("#mcpfcvpendingInvoices option:selected").attr('billId');
		}
		var txnForItem = readMultiItemDataForPayVendor("mcpfcvSecondTableForMultiInvoice");
		if(txnForItem.length <=0 || txnForItem.amountPaid == ""){
			return false;
		}
		var outstandings = $("#mcpfcvvendcustoutstandingsgross").text()+","+$("#mcpfcvvendcustoutstandingsnet").text()+","+$("#mcpfcvvendcustoutstandingsnetdescription").text()+","+$("#mcpfcvvendcustoutstandingspaid").text()+","+$("#mcpfcvvendcustoutstandingsnotpaid").text()+","+$("#mcpfcvvendcustoutstandingspurchasereturn").text();
		var vendorAdvanceType = $("#mcpfcvvendoradvanceType option:selected").val();
		var txnVendorAdvanceIfAny = $("#mcpfcvvendoradvance").val();
		var txnVendorAdvanceAdjustment = $("#mcpfcvhowmuchfromadvance").val();
		var paymentReceived = $("#mcpfcvtotalpaymentPaid").val();
		var totalDiscountReceived = $("#mcpfcvtotalDiscount").val();
		$("#mcpfcvSecondTableForMultiInvoice > tbody > tr").each(function() {
			var dueBal = $(this).find("td #mcpfcvduebalance").val();
			if(dueBal != "" && dueBal !=undefined)
				balanceOnThatInvoice = parseFloat(balanceOnThatInvoice) + parseFloat(dueBal);
		});
		var txnRemarks = $("#mcpfcvRemarks").val();
		var actPayToVend = $(".vendorActPayment").text();
		var supportingDocTmp = $("#"+parentTr+" select[name='mcpfcvuploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		var supportingDoc = supportingDocTmp.join(',');
		var followedkl = $("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		var klmandatoryfollowednotfollowed="";
		if(typeof followedkl!='undefined'){
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
				klmandatoryfollowednotfollowed="1";
			}
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
				klmandatoryfollowednotfollowed="0";
			}
		}
		var followedkl = $("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		if(typeof followedkl != 'undefined'){
			var klfollowednotfollowed = $("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").is(':checked');
			if(klfollowednotfollowed == false){
				swal("Mandatory rules not followed!", "Please read mandatory knowledge library before submitting transaction for approval.", "error");
				enableTransactionButtons();
				return true;
			}
		}
		var txnDate = $("#"+parentTr).find(".txnBackDate").val();
		if(balanceOnThatInvoice < 0.0){
			swal("Incomplete transaction data!", "Please provide complete pay vendor/supplier details before submitting for approval", "error");
			enableTransactionButtons();
			return true;
		}else{
			var proceed=false;
			if(supportingDoc==""){

				var documentUploadRequired = checkForDocumentUploadRulePVS(txnInvoice,paymentReceived);
				if(documentUploadRequired == true){
					if(confirm("This Transaction requires mandatory documents. Please upload the necessary document.")){
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
			var userCheckRule = checkForOnlyConfiguredApproverPVS(txnInvoice,paymentReceived,"payvendorsupplier");
			if(userCheckRule ==true){
				proceed=true;
			}
			if(userCheckRule==false){
				proceed=false;
			}
			// SingleUSer
			var txnReceiptPaymentBank = "";
			var txnInstrumentNum = "";
			var txnInstrumentDate = "";

			if(proceed){
				var txnJsonData={};
				txnJsonData.txnPurpose = whatYouWantToDo;
				txnJsonData.txnPurposeVal = whatYouWantToDoVal;
				txnJsonData.txnForBranch  =  txnForBranch;
				txnJsonData.creditMCPFCVVendor = creditVendor;
				txnJsonData.txnMCPFCVInvoice = txnInvoice;
				txnJsonData.txnMCPFCVoutstandings = outstandings;
				txnJsonData.vendorAdvanceType = vendorAdvanceType;
				txnJsonData.txnMCPFCVVendorAdvanceIfAny = txnVendorAdvanceIfAny;
				txnJsonData.txnMCPFCVVendorAdvanceAdjustment = txnVendorAdvanceAdjustment;
				txnJsonData.txnMCPFCVpaymentReceived = paymentReceived;
				txnJsonData.txnMCPFCVTotalDiscountReceived = totalDiscountReceived;
				txnJsonData.txnMCPFCVpaymentDue = balanceOnThatInvoice;
				txnJsonData.txnactPayToVend = actPayToVend;
				txnJsonData.txnremarks = txnRemarks;
				txnJsonData.supportingdoc = supportingDoc;
				txnJsonData.klfollowednotfollowed  =  klmandatoryfollowednotfollowed;
				txnJsonData.txndocumentUploadRequired = documentUploadRequired;
				txnJsonData.openingBalBillId = openingBalBillId;
				txnJsonData.txnForItem = txnForItem;
				txnJsonData.txnDate = txnDate;
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
				txnJsonData.useremail=$("#hiddenuseremail").text();
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
							return false;
						}
						approvealForSingleUser(data);	// Single User
						cancel();
						//getUserTransactions(2, 100);
						viewTransactionData(data); // to render the updated transaction recored
					},
					error: function (xhr, status, error) {
						if(xhr.status == 401){
							doLogout();
						}else if(xhr.status == 500){
				    		swal("Error on submit for approval!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
						enableTransactionButtons();
					}
				});
			}
		}
	}else if(whatYouWantToDoVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
		submitForApprovalVendRecAdv(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal ==  TRANSFER_MAIN_CASH_TO_PETTY_CASH){
		var txnForBranch="";var txnRemarks="";var supportingDoc="";
		var purposeOfTransfer="";var amountOfTransfer="";
		var resultantCash="";var resulatntPettyCash="";
		txnForBranch=$("#tmtpcaTxnForBranches option:selected").val();
		txnRemarks=$("#tmtpcaRemarks").val();
		var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		supportingDoc = supportingDocTmp.join(',');
		purposeOfTransfer=$("#transferPurpose").val();
		amountOfTransfer=$("#branchTransferrableAmount").val();
		resultantCash=$("#branchCashResultant").val();
		resulatntPettyCash=$("#branchPettyCashResultant").val();

		var txnReceiptDetails = $("#"+parentTr+" select[class='txnPaymodeCls'] option:selected").val();
		var txnReceiptTypeBankDetails = $("#"+parentTr+" textarea[class='txnReceptTextCls']").val();

		if(txnForBranch=="" || amountOfTransfer=="" || resultantCash=="" || resulatntPettyCash==""){
			swal("Incomplete transaction data!", "Please provide complete Transfer main cash to petty cash transaction details before submitting for approval", "error");
			enableTransactionButtons();
		}else{
			var proceed=false;
			var userCheckRule=checkForOnlyConfiguredApproverTMTPC(txnForBranch,amountOfTransfer,"maintopettycashtransfer");
			if(userCheckRule==true){
				proceed=true;
			}
			if(userCheckRule==false){
				proceed=false;
			}
			if(proceed){
				var txnJsonData={};
				txnJsonData.txnPurpose=whatYouWantToDo;
				txnJsonData.txnPurposeVal=whatYouWantToDoVal;
				txnJsonData.txnTMTPCABranch=txnForBranch;
				txnJsonData.txnTMTPCAPurposeOfTransfer=purposeOfTransfer;
				txnJsonData.txnTMTPCAAmountOfTransfer=amountOfTransfer;
				txnJsonData.txnReceiptTypeBankDetails = txnReceiptTypeBankDetails;
				txnJsonData.txnReceiptDetails = txnReceiptDetails;
				txnJsonData.txnremarks=txnRemarks;
				txnJsonData.supportingdoc=supportingDoc;
				txnJsonData.useremail=$("#hiddenuseremail").text();
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
							return false;
						}
						approvealForSingleUser(data);	// Single User
						cancel();
						//getUserTransactions(2, 100);
						viewTransactionData(data); // to render the updated transaction recored
					},
					error: function (xhr, status, error) {
						if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
				    		swal("Error on submit for approval!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
						enableTransactionButtons();
					}
				});
			}
		}
	} else if(whatYouWantToDo=="Pay special adjustments amount to vendors"){
		var creditVendor="";var txnForProject="";var paymentAmountToVendor="";
		var txnRemarks="";var supportingDoc="";var klmandatoryfollowednotfollowed="";
		creditVendor=$("#psaatvVendors option:selected").val();
		txnForProject=$("#psaatvTxnForProjects option:selected").val();
		paymentAmountToVendor=$("#psaatvReceivedAmount").val();
		txnRemarks=$("#psaatvRemarks").val();
		var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		supportingDoc = supportingDocTmp.join(',');
		var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
				klmandatoryfollowednotfollowed="1";
			}
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
				klmandatoryfollowednotfollowed="0";
			}
		}
		var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			var klfollowednotfollowed=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").is(':checked');
			if(klfollowednotfollowed==false){
				swal("Mandatory rules not followed!", "Please read the madatory knowledge library before submitting transaction for approval.", "error");
				enableTransactionButtons();
				return true;
			}
		}
		if(creditVendor=="" || paymentAmountToVendor==""){
			swal("Incomplete transaction data!", "Please provide complete Pay special adjustments amount to vendors transaction details before submitting for approval", "error");
			enableTransactionButtons();
			return true;
		}else{
			var proceed=false;
			var userCheckRule=checkForOnlyConfiguredApproverPSAATV(creditVendor,paymentAmountToVendor,"payspecialadjustmenttovendor");
			if(userCheckRule==true){
				proceed=true;
			}
			if(userCheckRule==false){
				proceed=false;
			}
			if(proceed){
				var txnJsonData={};
				txnJsonData.txnPurpose=whatYouWantToDo;
				txnJsonData.txnPurposeVal=whatYouWantToDoVal;
				txnJsonData.txnPSAATVCreditVendor=creditVendor;
				txnJsonData.txnPSAATVForProject=txnForProject;
				txnJsonData.txnPSAATVAmountPaid=paymentAmountToVendor;
				txnJsonData.txnremarks=txnRemarks;
				txnJsonData.supportingdoc=supportingDoc;
				txnJsonData.klfollowednotfollowed=klmandatoryfollowednotfollowed;
				txnJsonData.useremail=$("#hiddenuseremail").text();
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
							return false;
						}
						cancel();
						//getUserTransactions(2, 100);
						viewTransactionData(data); // to render the updated transaction recored
					},
					error: function (xhr, status, error) {
						if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
				    		swal("Error on submit for approval!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
						enableTransactionButtons();
					}

				});
			}
		}
	} else if(whatYouWantToDo=="Sales returns"){
		submitForApprovalSalesReturns(whatYouWantToDo,whatYouWantToDoVal,parentTr);
	} else if(whatYouWantToDo=="Purchase returns"){
		submitForApprovalPurchaseReturnTrans(whatYouWantToDo,whatYouWantToDoVal,parentTr);
	} else if(whatYouWantToDo=="Make Provision/Journal Entry" || whatYouWantToDo=="Journal Entry"){
		submitForApprovalProvisionJournal(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	} else if(whatYouWantToDo=="Withdraw Cash From Bank" || whatYouWantToDo=="Deposit Cash In Bank" || whatYouWantToDo=="Transfer Funds From One Bank To Another"){
		var bankBranch="";var branchBank="";var branchBankDetails="";var enteredAmount="";var resultAmount="";var toBankBranch="";var toBranchBank="";var purpose="";
		var txnRemarks="";var supportingDoc="";var toBranchBankDetails="";
		var instrumentDate=""; var instrumentNumber=""; /* var instrumentDate2=""; var instrumentNumber2=""; */

		if(whatYouWantToDo=="Withdraw Cash From Bank"){
			bankBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
			branchBank=$("#"+parentTr+" select[class='txnBranchBanks'] option:selected").val();
			branchBankDetails=$("#"+parentTr+" div[class='txnBranchBankDetails']").text();
			enteredAmount=$("#"+parentTr+" input[id='withdrawalAmount']").val();
			resultAmount=$("#"+parentTr+" input[id='leftAmountInBank']").val();
			txnRemarks=$("#wcafbnkRemarks").val();
			var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
				if($(this).val() != ""){
					return $(this).val();
				}
			}).get();
			supportingDoc = supportingDocTmp.join(',');
			instrumentNumber=$("#"+parentTr+" input[name='wcafbnkInstrumentNumber']").val();
			instrumentDate=$("#"+parentTr+" input[name='wcafbnkInstrumentDate']").val();
		} else if(whatYouWantToDo=="Deposit Cash In Bank"){
			bankBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
			branchBank=$("#"+parentTr+" select[class='txnBranchBanks'] option:selected").val();
			branchBankDetails=$("#"+parentTr+" div[class='txnBranchBankDetails']").text();
			enteredAmount=$("#"+parentTr+" input[id='depositAmount']").val();
			resultAmount=$("#"+parentTr+" input[id='totalAmountInBank']").val();
			txnRemarks=$("#dcaibnkRemarks").val();
			var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
				if($(this).val() != ""){
					return $(this).val();
				}
			}).get();
			supportingDoc = supportingDocTmp.join(',');
			instrumentNumber=$("#"+parentTr+" input[name='dcaibnkInstrumentNumber']").val();
			instrumentDate=$("#"+parentTr+" input[name='dcaibnkInstrumentDate']").val();
		} else if(whatYouWantToDo=="Transfer Funds From One Bank To Another"){
			bankBranch=$("#"+parentTr+" select[id='tfftbnkTxnFromBranches'] option:selected").val();
			branchBank=$("#"+parentTr+" select[id='fromBranchBankAccounts'] option:selected").val();
			branchBankDetails=$("#"+parentTr+" div[class='txnFromBranchBankDetails']").text();
			toBankBranch=$("#"+parentTr+" select[id='tfftbnkTxnToBranches'] option:selected").val();
			toBranchBank=$("#"+parentTr+" select[id='toBranchBankAccounts'] option:selected").val();
			toBranchBankDetails=$("#"+parentTr+" div[class='txnToBranchBankDetails']").text();
			enteredAmount=$("#"+parentTr+" input[id='transferAmount']").val();
			resultAmount=$("#"+parentTr+" input[id='totalAmtInBank']").val();
			purpose=$("#tfftbnkPurpose").val();
			txnRemarks=$("#dcaibnkRemarks").val();
			var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
				if($(this).val() != ""){
					return $(this).val();
				}
			}).get();
			supportingDoc = supportingDocTmp.join(',');
			instrumentNumber=$("#"+parentTr+" input[name='tfftbnkInstrumentNumber']").val();
			instrumentDate=$("#"+parentTr+" input[name='tfftbnkInstrumentDate']").val();
		}

		if(instrumentDate == ""){
			swal("Incomplete Transaction data!", "Instrument Date cannot be empty.", "error");
			$.unblockUI();
			enableTransactionButtons();
			return false;
		}
		/*instrumentNumber2=$("#"+parentTr+" input[name='tfftbnkInstrumentNumber2']").val();
		instrumentDate2=$("#"+parentTr+" input[name='tfftbnkInstrumentDate2']").val();
		if(instrumentDate2 == ""){
			alert("Instrument Date cannot be empty.");
			enableTransactionButtons();
			return false;
		}*/

		if(enteredAmount!="" && resultAmount!=""){
			var userCheckRule=checkForOnlyConfiguredApproverTMTPC(bankBranch,enteredAmount,"maintopettycashtransfer");
			if(userCheckRule==true){
				proceed=true;
			}
			if(userCheckRule==false){
				proceed=false;
			}
			if(proceed){
				var txnJsonData={};
				txnJsonData.txnPurpose=whatYouWantToDo;
				txnJsonData.txnPurposeVal=whatYouWantToDoVal;
				txnJsonData.txnbankBranch=bankBranch;
				txnJsonData.txnbranchBank=branchBank;
				txnJsonData.txnbranchBankDetails=branchBankDetails;
				txnJsonData.txntoBankBranch=toBankBranch;
				txnJsonData.txntoBranchBank=toBranchBank;
				txnJsonData.txntoBranchBankDetails=toBranchBankDetails;
				txnJsonData.txnenteredAmount=enteredAmount;
				txnJsonData.txnresultAmount=resultAmount;
				txnJsonData.txnpurpose=purpose;
				txnJsonData.txnremarks=txnRemarks;
				txnJsonData.supportingdoc=supportingDoc;
				txnJsonData.useremail=$("#hiddenuseremail").text();
				txnJsonData.txnInstrumentNum=instrumentNumber;
				txnJsonData.txnInstrumentDate=instrumentDate;
				/*txnJsonData.txnInstrumentNum2=instrumentNumber2;
				txnJsonData.txnInstrumentDate2=instrumentDate2; */
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
						approvealForSingleUser(data);	// Single User
						cancel();
						//getUserTransactions(2, 100);
						viewTransactionData(data); // to render the updated transaction recored
					},
					error: function (xhr, status, error) {
						if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
				    		swal("Error on submit for approval!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
						enableTransactionButtons();
					}

				});
			}
		}
	} else if(whatYouWantToDo=="Transfer Inventory Item From One Branch To Another"){
		var inventoryItem="";var inventoryItemBranch="";var availableStock="";var stockTransferInProgress="";
		var noOfUnitToTransfer="";var transferToBranch="";var availableStockToBranch="";var resultantStockInToBranch="";
		var txnRemarks="";var supportingDoc="";
		inventoryItem=$("#tifbtbItemsId option:selected").val();
		inventoryItemBranch=$("#inventoryTransferFromBranchId option:selected").val();
		availableStock=$(".availableStock").text();
		stockTransferInProgress=$(".stockTransferInProgress").text();
		noOfUnitToTransfer=$("#unitToTransferId").val();
		transferToBranch=$("#inventoryTransferToBranchId option:selected").val();
		availableStockToBranch=$(".availableToStock").text();
		resultantStockInToBranch=$("#resultantStockId").val();
		txnRemarks=$("#tifbtbRemarks").val();
		var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		supportingDoc = supportingDocTmp.join(',');
		if(resultantStockInToBranch==""){
			swal("Incomplete transaction data!", "Please Complete The Stock Transfer Details Completely Before Submit For Approval", "error");
			return true;
		}
		var proceed=false;
		var userCheckRule=checkForConfiguredApproverInventoryTransfer(inventoryItemBranch,inventoryItem,"inventorystocktransfer");
		if(userCheckRule==true){
			proceed=true;
		}
		if(userCheckRule==false){
			proceed=false;
		}
		if(proceed){
			var txnJsonData={};
			txnJsonData.txnPurpose=whatYouWantToDo;
			txnJsonData.txnPurposeVal=whatYouWantToDoVal;
			txnJsonData.invItem=inventoryItem;
			txnJsonData.invTransferFromBranch=inventoryItemBranch;
			txnJsonData.invFromBranchAvailableStock=availableStock;
			txnJsonData.invFromBranchStockTransferInprogress=stockTransferInProgress;
			txnJsonData.numberOfUnitToTransfer=noOfUnitToTransfer;
			txnJsonData.invTransferToBranch=transferToBranch;
			txnJsonData.invTransferToBranchStock=availableStockToBranch;
			txnJsonData.invResultantStock=resultantStockInToBranch;
			txnJsonData.txnremarks=txnRemarks;
			txnJsonData.supportingdoc=supportingDoc;
			txnJsonData.useremail=$("#hiddenuseremail").text();
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
					approvealForSingleUser(data);	// Single User
					cancel();
					//getUserTransactions(2, 100);
					viewTransactionData(data); // to render the updated transaction recored
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
			    		swal("Error on submit for approval!", "Please retry, if problem persists contact support team", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
					enableTransactionButtons();
				}
			});
		}
	} else if(whatYouWantToDo=="Inventory Opening Balance"){
		var inventoryItem="";var inventoryItemBranch="";var availableStock="";var noOfUnitOfOpeningStock="";
		var purpose="";var txnRemarks="";var supportingDoc="";
		inventoryItem=$("#openingBalanceInvItemNamesId option:selected").val();
		inventoryItemBranch=$("#invOpeningTxnForBranchesId option:selected").val();
		availableStock=$("#obfinvAvailableStockId").val();
		noOfUnitOfOpeningStock=$("#obfinvOpeningStockId").val();
		purpose=$("#obfinvPurposeId").val();
		txnRemarks=$("#obfinvRemarks").val();
		var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
			if($(this).val() != ""){
				return $(this).val();
			}
		}).get();
		supportingDoc = supportingDocTmp.join(',');
		if(noOfUnitOfOpeningStock=="" || purpose==""){
			swal("Incomplete transaction data!","Please provide complete transaction data before submiting for approval.", "error");
			return true;
		}
		var proceed=false;
		var userCheckRule=checkForConfiguredApproverInventoryTransfer(inventoryItemBranch,inventoryItem,"inventorystocktransfer");
		if(userCheckRule==true){
			proceed=true;
		}
		if(userCheckRule==false){
			proceed=false;
		}
		if(proceed){
			var txnJsonData={};
			txnJsonData.txnPurpose=whatYouWantToDo;
			txnJsonData.txnPurposeVal=whatYouWantToDoVal;
			txnJsonData.invItem=inventoryItem;
			txnJsonData.invBranchOpeningBal=inventoryItemBranch;
			txnJsonData.invAvailableStock=availableStock;
			txnJsonData.invNoOfUnitOfOpeningStock=noOfUnitOfOpeningStock;
			txnJsonData.invPurpose=purpose;
			txnJsonData.txnremarks=txnRemarks;
			txnJsonData.supportingdoc=supportingDoc;
			txnJsonData.useremail=$("#hiddenuseremail").text();
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
					if(data.validTransactionDate == 0){
						enableTransactionButtons();
						swal("Invalid transaction date!", "Transaction date is out of financial date range or Financial year date is not set.", "error");
						return false;
					}
					cancel();
					//getUserTransactions(2, 100); 22Dec2016
					viewTransactionData(data); // to render the updated transaction recored
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout();
					}else if(xhr.status == 500){
			    		swal("Error on submit for approval!", "Please retry, if problem persists contact support team", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
					enableTransactionButtons();
				}
			});
		}
	} else if(whatYouWantToDoVal==PREPARE_QUOTATION){
		submitForApprovalQuotation(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal==PROFORMA_INVOICE){
		submitForApprovalProforma(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal==SELL_ON_CASH_COLLECT_PAYMENT_NOW || whatYouWantToDoVal==SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
		submitForApprovalSell(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	} else if(whatYouWantToDoVal==PURCHASE_ORDER){
		submitForApprovalPurchaseOrder(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal== REFUND_ADVANCE_RECEIVED){
		submitForApprovalRefundAdvanceReceived(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	} else if(whatYouWantToDoVal== REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
		submitForApprovalRefundAmountReceivedAgainstInvoice(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal== BILL_OF_MATERIAL){
		submitForApprovalBillOfMaterialTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	//}else  if(whatYouWantToDoVal == CREATE_PURCHASE_ORDER){
	//submitForApprovalPoTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	//}else if(whatYouWantToDoVal == CREATE_PURCHASE_REQUISITION){
	//submitForApprovalPrTxn(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}else if(whatYouWantToDoVal == CANCEL_INVOICE) {
		submitForApprovalCancelInvoice(whatYouWantToDo, whatYouWantToDoVal, parentTr);
	}
    enableTransactionButtons();
}

$(document).ready(function() {
	$('.submitForApproval').click(function(){
		var parentTrId=$(this).attr('id');
		var parentTr=parentTrId.substring(0,6)+"trid";
		var whatYouWantToDo=$("#whatYouWantToDo").find('option:selected').text();
		var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
		if(whatYouWantToDo=="" || typeof whatYouWantToDo=='undefined'){
			whatYouWantToDo=$("#whatYouWantToDoProvJournal").find('option:selected').text();
		}
		if(whatYouWantToDoVal=="" || typeof whatYouWantToDoVal=='undefined'){
			whatYouWantToDoVal=$("#whatYouWantToDoProvJournal").find('option:selected').val();
		}
		submitForApproval(whatYouWantToDo,whatYouWantToDoVal,parentTr);
    });
});

function approverAction(parentTr){
	//alert(">>>>>16"); //sunil
	disableTransactionButtons();

	var txnReferenceNo = $("#"+parentTr+" td:first p").text();
	var selectedAction=$("#"+parentTr+" select[id='approverActionList'] option:selected").val();
	var transactionEntityId=parentTr.substring(17, parentTr.length);
	var supportingDocTmp = $("#"+parentTr+" select[name='txnViewListUpload'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var selectedAddApproverVal="";
	if(selectedAction==""){
		alert("Please choose your next action from the Approver action list");
		enableTransactionButtons();
		return true;
	}
	if(selectedAction=="3"){
		selectedAddApproverVal=$("#"+parentTr+" select[id='userAddApproval'] option:selected").val();
		if(selectedAddApproverVal==""){
			alert("Please choose the user to whom you want to send for additional approval");
			enableTransactionButtons();
			return true;
		}else{
			var txnReferenceNo = $("#"+parentTr+" td:first p").text();
			var txnJsonData={};
			txnJsonData.useremail=$("#hiddenuseremail").text();
			txnJsonData.selectedApproverAction=selectedAction;
			txnJsonData.transactionPrimId=transactionEntityId;
			txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
			txnJsonData.suppDoc=supportingDoc;
			txnJsonData.txnRmarks=remarks;
			txnJsonData.txnReferenceNo = txnReferenceNo;
			$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
			var url="/transaction/approverAction";
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
					$("tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(8)").empty();
					$("tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(8)").append('<div style="height: 130px;overflow: auto;"><div class="txnstatgreen">Require Additional Approval</div><b>APPROVER:</b><br/><p style="color: blue;">'+txnJsonData.useremail+'</p><br><b>Additional Approval Required By:</b><br>'+selectedAddApproverVal+'</div>');
					viewTransactionData(data)
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout();
					}else if(xhr.status == 500){
						swal("Error!", "Please retry, if problem persists contact support team", "error");
					}
				},
				complete: function(data) {
					$.unblockUI();
					enableTransactionButtons();
				}
			});
		}
	}else{
		//send server for action to complete
		var txnJsonData={};
		txnJsonData.useremail=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		txnJsonData.txnReferenceNo = txnReferenceNo;
		var url="/transaction/approverAction";
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
				if(typeof data.resultantAmount !=='undefined' && data.resultantAmount < 0){
					if(typeof data.branchBankDetailEntered !=='undefined' &&  data.branchBankDetailEntered === false){
						swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
						disableTransactionButtons();
						return false;
					}else{
						swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
					}
				}
				if(typeof data.resultantCash !=='undefined' && data.resultantCash < 0){
					swal("Insufficient balance in the cash account!","Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash, "warning");
				}

				if(typeof data.resultantPettyCashAmount !=='undefined' && data.resultantPettyCashAmount < 0){
					swal("Insufficient balance in the petty cash account!", "Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount, "warning");
				}
				//$("tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(8)").empty();
				if(selectedAction=="1"){ //When transaction is approved, update status column with staus as Approved and approver email
					//$("tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(8)").append('<div style="height: 130px;overflow: auto;"><div class="txnstatgreen">Approved</div><b>APPROVER:</b><br/><p style="color: blue;">'+txnJsonData.useremail+'</p></div>');

				}
				if(selectedAction=="2"){ //When transaction is rejected, update status column with staus as Rejected and approver email
					$("tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(8)").append('<div style="height: 130px;overflow: auto;"><div class="txnstatred">Rejected</div><b>APPROVER:</b><br/><p style="color: blue;">'+txnJsonData.useremail+'</p></div>');
				}
				if(selectedAction=="5"){ //When transaction Require Clarification, update status column with staus as Require Clarification and approver email
					$("tr[id='transactionEntity"+transactionEntityId+"'] td:nth-child(8)").append('<div style="height: 130px;overflow: auto;"><div class="txnstat">Require Clarification</div><b>APPROVER:</b><br/><p style="color: blue;">'+txnJsonData.useremail+'</p></div>');
				}
				viewTransactionData(data)
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
					swal("Error!", "Please retry, if problem persists contact support team", "error");
				}
			},
			complete: function(data) {
				$.unblockUI();
				enableTransactionButtons();
			}
		});
	}
	enableTransactionButtons();
}

$(document).ready(function(){
	$("input[class='txnGross']").change(function() {
		$("#"+parentTr+" input[class='howMuchAdvance']").val("");
	});
});

function calculateGross(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var parentOfparentTr = "";
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	if(txnPurposeVal== BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal== BUY_ON_CREDIT_PAY_LATER || txnPurposeVal== BUY_ON_PETTY_CASH_ACCOUNT){
		 parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
		var vendSelectedVal=$("#"+parentOfparentTr+" .masterList option:selected").val();
		if(txnPurposeVal== BUY_ON_CREDIT_PAY_LATER){
			if(vendSelectedVal==""){
				swal("Incomplete transaction data!","Please Choose Vendor for the Item Which You Want To Purchase On Credit.", "error");
				$(elem).val("");
				$("#"+parentTr+" input[class='txnGross']").val("");
				return false;
			}
		}else{
			var bocpraUnAvailableVendor=$(".unavailable ui-autocomplete-input").val();
			if(vendSelectedVal=="" && bocpraUnAvailableVendor==""){
				swal("Incomplete transaction data!", "Please Choose Vendor Or Provide Unregistered Vendor Name for the Item Which You Want To Purchase On Cash.", "error");
				$(elem).val("");
				$("#"+parentTr+" input[class='txnGross']").val("");
				return false;
			}
		}
	}
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var noOfUnitValue=$("#"+parentTr+" input[class='txnNoOfUnit']").val();
	var unitPriceValue=$("#"+parentTr+" input[class='txnPerUnitPrice']").val();
	//var frieghtCharges=$("#"+parentTr+" input[class='txnFrieghtCharges']").val();

	if(unitPriceValue!="" && noOfUnitValue!=""){
		var grossTotal = (noOfUnitValue*unitPriceValue);
		var increaseDecrease = "";
        if(txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER) {
            increaseDecrease = $("#creditDebitTxnDiv select[class='creditDebitType'] option:selected").val();
        }else  if(txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR) {
            increaseDecrease = $("#creditDebitVendTxnDiv select[class='creditDebitType'] option:selected").val();
        }
		if(txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_VENDOR){
        	if(increaseDecrease == "1"){
                var unitPriceValueOrg = $("#"+parentTr+" input[class='txnPerUnitPriceHid']").val();
                var revisedPrice = parseFloat(unitPriceValueOrg) - parseFloat(unitPriceValue);
                if(parseFloat(revisedPrice) > 0) {
                    grossTotal = parseFloat(noOfUnitValue)*parseFloat(revisedPrice);
                }else{
                    grossTotal = parseFloat(noOfUnitValue)*parseFloat(unitPriceValue);
                }
			}else if(increaseDecrease == "2"){
                var unitOrg = $("#"+parentTr+" input[class='txnNoOfUnitHid']").val();
                var revisedUnits = parseFloat(unitOrg) - parseFloat(noOfUnitValue);
                if(parseFloat(revisedUnits) > 0) {
                    grossTotal = parseFloat(unitPriceValue) * parseFloat(revisedUnits);
                }else{
                    grossTotal = parseFloat(unitPriceValue) * parseFloat(noOfUnitValue);
				}
            }
		}else if(txnPurposeVal == DEBIT_NOTE_CUSTOMER || txnPurposeVal == CREDIT_NOTE_VENDOR){
            if(increaseDecrease == "1"){
                var unitPriceValueOrg = $("#"+parentTr+" input[class='txnPerUnitPriceHid']").val();
                var revisedPrice = parseFloat(unitPriceValue) - parseFloat(unitPriceValueOrg);
                if(parseFloat(revisedPrice) > 0) {
                    grossTotal = parseFloat(noOfUnitValue)*parseFloat(revisedPrice);
                }else{
                    grossTotal = parseFloat(noOfUnitValue)*parseFloat(unitPriceValue);
                }
            }else if(increaseDecrease == "2"){
                var unitOrg = $("#"+parentTr+" input[class='txnNoOfUnitHid']").val();
                var revisedUnits = parseFloat(noOfUnitValue) - parseFloat(unitOrg);
                if(parseFloat(revisedUnits) > 0) {
                    grossTotal = parseFloat(unitPriceValue) * parseFloat(revisedUnits);
                }else{
                    grossTotal = parseFloat(unitPriceValue) * parseFloat(noOfUnitValue);
                }
            }
		}

		//if(parentTr.substring(0,4)=="item"){ //Sell on cash transaction items inner table
		//	parentTr = "soccpntrid";
		//}
		var discountAmount = $("#"+parentTr+" input[class='txnDiscountAmount']").val();
		if(typeof discountAmount !='undefined' && discountAmount != ""){
			grossTotal = parseFloat(grossTotal) - parseFloat(discountAmount);
		}

		if(txnPurposeVal== BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal== BUY_ON_CREDIT_PAY_LATER || txnPurposeVal== BUY_ON_PETTY_CASH_ACCOUNT){
				checkForSupportDocLimit(parentOfparentTr, parentTr, grossTotal);
		}
		$("#"+parentTr+" input[class='txnGross']").data('txnGross', ''+grossTotal);
		$("#"+parentTr+" input[class='txnGross']").val((grossTotal*1).toFixed(2));

	}else{
		$("#"+parentTr+" input[class='txnGross']").val("");
		$("#"+parentTr+" input[class='txnGross']").data('txnGross', "0");
		//$("#"+parentTr+" input[class='taxableVal']").val("");
	}
	//$(".netAmountVal").val("");
	//$(".withholdingtaxcomponenetdiv").val("");
	$(".txnLeftOutWithholdTransIDs").val("");
	$.unblockUI();
}

function getAdvanceDiscount(elem){
	var returnValue = true;
	$(".individualtaxdiv").text("");
	$(".individualtaxformuladiv").text("");
	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	//$(".customerAdvance").val("");
	$(".discountavailable").text("");
	//$(".netAmountVal").val("");
	$(".txnFrieghtCharges").val("");
	var parentTr = $(elem).closest('tr').attr('id');
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	if(parentTr.indexOf("transactionEntity")!=-1){
		text = $("#"+parentTr+" select[id='whatYouWantToDo'] option:selected").text();
	}
	var mainTableTr = "";
	var visitingCustomer =""; var vendorcustomer="";	var selectedBranch="";	var txnTypeOfSupply = ""; var destinGstin =""; var txnWithWithoutTax =""; var destinGstinId = "";
	if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR || txnPurposeVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER || txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER || txnPurposeVal == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || txnPurposeVal == PREPARE_QUOTATION) {
        mainTableTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
        vendorcustomer = $("#"+mainTableTr+" .masterList option:selected").val();
        if(vendorcustomer==""){
            visitingCustomer = $("#"+mainTableTr+" input[class='unavailable ui-autocomplete-input']").val();
            if(visitingCustomer==""){
                visitingCustomer = $("#"+mainTableTr+" input[class='unavailable']").val();
            }
        }
        txnTypeOfSupply = $("#"+mainTableTr+" select[class='txnTypeOfSupply']").val();
        if(txnTypeOfSupply=="" && txnPurposeVal != TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
            swal("Incomplete transaction detail!", "Please select Type of Supply", "error");
            returnValue=false;
        }
        destinGstin = $("#"+mainTableTr+" select[class='placeOfSply txnDestGstinCls']").val();
		destinGstinId = $("#"+mainTableTr+" select[class='placeOfSply txnDestGstinCls']").find(':selected').attr('id');
    }else{
        vendorcustomer =$(elem).val();
	}

    selectedBranch=$("#"+mainTableTr+" select[class='txnBranches'] option:selected").val();

	if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
		txnWithWithoutTax = $("#"+mainTableTr+" select[class='txnWithWithoutTaxCls']").val();
		if((txnTypeOfSupply =="3" || txnTypeOfSupply =="4" || txnTypeOfSupply =="5") && txnWithWithoutTax==""){
			swal("Incomplete transaction detail!", "Please select With / Without IGST.", "error");
			returnValue=false;
		}
		if(visitingCustomer !="" && txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW && GST_COUNTRY_CODE !== "" && GST_COUNTRY_CODE !== undefined){
			var txnWalkinCustomerType = $("#"+mainTableTr+" select[class='walkinCustType']").val();
			if(txnWalkinCustomerType == "" || txnWalkinCustomerType == null){
				swal("Invalid Type of Customer!", "Please select valid Type of customer.", "error");
				return false;
			}else if(txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2"){
				destinGstin = $("#"+mainTableTr+" input[class='placeOfSplyTextHid']").val();
			}else if(txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4"){
				var destinGstin = $("#"+mainTableTr+" select[class='txnBranches']").children(":selected").attr("id");
				if(destinGstin === null || destinGstin === ""){
					swal("Invalid Branch's GSTIN", "Please select valid Branch.", "error");
					return false;
				}
			}else if(txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6"){
				destinGstin = $("#"+mainTableTr+" select[name='txnWalkinPlcSplySelect']").val();
			}
			if(txnWalkinCustomerType == "5" && txnWalkinCustomerType == "6" && destinGstin.length  < 1){
				swal("Invalid Place of Supply", "Please provide valid Place of Supply.", "error");
				enableTransactionButtons();
				return false;
			}else if(txnWalkinCustomerType != "5" && txnWalkinCustomerType != "6" && destinGstin.length > 1 && destinGstin.length < 15){
				swal("Invalid Place of Supply", "Please provide valid Place of Supply.", "error");
				enableTransactionButtons();
				return false;
			}
		}else{
			destinGstin = $("#"+mainTableTr+" select[class='placeOfSply txnDestGstinCls']").val();
		}
	}

	var vendorcustomeritem=$("#"+parentTr+" .txnItems option:selected").val();
	if(vendorcustomer=="" && visitingCustomer ==""){
		swal("Missing customer/vendor!", "Customer/vendor is mandatory to proceed.", "error");
		returnValue = false;
	}
	var txnDate =  $("#"+mainTableTr).find(".txnBackDate").val();
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.userTxnPurposeText=text;
	jsonData.txnPurposeVal = txnPurposeVal;
	jsonData.txnVendCustId=vendorcustomer;
	jsonData.txnVendCustItemId=vendorcustomeritem;
	jsonData.txnvisitingCustomer=visitingCustomer;
	jsonData.txnBranchId=selectedBranch;
	jsonData.txnTypeOfSupply = txnTypeOfSupply;
	jsonData.txnPlaceOfSupply = destinGstin;
	jsonData.destinGstinId = destinGstinId;
	jsonData.txnWithWithoutTax = txnWithWithoutTax;
	jsonData.txnDate = txnDate;
	if((vendorcustomer!="" || visitingCustomer != "") && vendorcustomeritem!=""){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var url="/transaction/getAdvanceDiscount";
		$.ajax({
			url: url,
			data:JSON.stringify(jsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			async : false,
			method:"POST",
			contentType:'application/json',
			success: function (data) {
	      		if(data.customerAdvanceDiscountData && data.customerAdvanceDiscountData.length>0){
			    	 if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER || txnPurposeVal == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
			    		 $("#"+parentTr+" input[class='customerAdvance']").val(data.customerAdvanceDiscountData[0].custAdvanceMoney);
			    		if(isSingleUserDeploy == "true") {
			    			$("#"+parentTr+" input[class='txnDiscountPercent']").val("");
			    		}else {
			    			$("#"+parentTr+" input[class='txnDiscountPercent']").val(data.customerAdvanceDiscountData[0].custDiscountPerc);
			    		}
			    		 $("#"+parentTr+" input[class='txnDiscountPercentHid']").val(data.customerAdvanceDiscountData[0].custDiscountPerc);
			    		 $("#"+parentTr+" input[class='txnDiscountAmount']").val("");
			    	 }
	      		}else if(data.vendorAdvanceUnitPriceData.length>0){
	      			if(txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT || txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR){
			    		 $("#"+parentTr+" input[class='customerAdvance']").val(data.vendorAdvanceUnitPriceData[0].vendAdvanceMoney);
			    		 $("#"+parentTr+" input[class='txnPerUnitPrice']").val(data.vendorAdvanceUnitPriceData[0].vendUnitPrice);
                    } else if (txnPurposeVal == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
                        $("#" + parentTr + " div[class='customerVendorExistingAdvance']").text(data.vendorAdvanceUnitPriceData[0].vendAdvanceMoney);
			    	 }
	      		}
	  	    },
	      error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}else if(xhr.status == 500){
					swal("Error on fetching advance/discount!", "Please retry, if problem persists contact support team", "error");
				}
			},
			complete: function(data) {
				$.unblockUI();
			}
	   });
   }
   return returnValue;
}

/*function addTaxComponents(elem){
	var parentTr=$(elem).parent().parent().parent().attr('id');
	var txnBranchId="";
	var jsonData = {};
	var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var inputTaxesLength=$("#noOfInputTaxesAdd").val();
	if(inputTaxesLength == "7")	{
		swal("Limit exceeded!", "Maximum 7 taxes allowed to apply for a transaction.", "error");
		return false;
	}
	if( transactionPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transactionPurposeId == BUY_ON_CREDIT_PAY_LATER || transactionPurposeId == PURCHASE_RETURNS) {
		var parentOfparentTr = $(elem).parents().closest('tr').attr('id');
		txnBranchId=$("#"+parentOfparentTr+" select[class='txnBranches'] option:selected").val();
	}else{
		txnBranchId=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	}
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnPurposeId=transactionPurposeId;
	jsonData.txnBranchId=txnBranchId;
	if(txnBranchId!="" && inputTaxesLength == "0"){
		var url="/transaction/getBranchInputTaxList";
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
			success: function (data) {*/
				/*if(parentTr.indexOf("bocpra")!=-1){
					$("#staticBuyTaxes").attr('data-toggle', 'modal');
			    	$("#staticBuyTaxes").modal('show');
			    	$(".staticBuyTaxesclose").attr("href",location.hash);
			    	$("#staticBuyTaxes div[class='modal-body']").html("");
					$("#staticBuyTaxes div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="multipleBuyTaxesBreakupTable" style="margin-top: 0px; width:450px;">'+
		    		'<thead class="tablehead1" style="position:relative"><th>Tax Name</th><th>Tax Rate</th><th>Total Tax</th><th>Add New</th><tr></thead><tbody style="position:relative"></tbody></table></div>');

		    		$("#staticBuyTaxes div[class='modal-body'] table[id='multipleBuyTaxesBreakupTable'] tbody").append('<tr id=1><td><select id="bocprainputtaxcomponenet" class="inputtaxcomponenet" name="bocprainputtaxcomponenet" placeholder="Tax Name" style="width:87px; margin-top:5px;"><option value="">Select</option></select></td><td><input type="text" id="bocprainputtaxcomponenetamt" class="inputtaxcomponenetValue" name="bocprainputtaxcomponenetamt" placeholder="Value" style="width:49px;margin-top:5px;" onkeyup="totalInputBuyTaxAmount(this); calculateNetAmount(this);" onkeypress="return onlyDotsAndNumbers(event);"</td><td><input type="button" style="width: 75px;" id="addTaxRow" value="AddTax" class="btn btn-submit" onclick="addNewBuyTaxRow(this);"></td></tr>');
		    		for(var i=0;i<data.inputTaxList.length;i++){
			    		//$("#staticMutipleTransactionItems div[class='modal-body'] table[id='multipleSellItemsBreakupTable'] tbody").append('<tr id='+i+'><td>'+data.transactionItemdetailsData[i].itemName+'</td><td>'+data.transactionItemdetailsData[i].pricePerUnit+'</td><td>'+data.transactionItemdetailsData[i].noOfUnits+'</td><td>'+data.transactionItemdetailsData[i].discountPer+'</td><td>'+data.transactionItemdetailsData[i].discountAmt+'</td><td>'+data.transactionItemdetailsData[i].grossAmount+'</td><td>'+data.transactionItemdetailsData[i].taxDescription+'</td><td>'+data.transactionItemdetailsData[i].totalInputTax+'</td><td>'+data.transactionItemdetailsData[i].availableAdvance+'</td><td>'+data.transactionItemdetailsData[i].adjFromAdvance+'</td><td>'+data.transactionItemdetailsData[i].netAmount+'</td></tr>');
			    		$("#staticBuyTaxes div[class='modal-body'] table[id='multipleBuyTaxesBreakupTable'] tbody select[name='bocprainputtaxcomponenet']").append('<option value="'+data.inputTaxList[i].inputTaxID+'">'+data.inputTaxList[i].inputTaxName+'</option>');
			    	}
		    		$("#staticBuyTaxes div[class='modal-body'] table[id='multipleBuyTaxesBreakupTable'] tbody").append('<tr><td></td><td></td><td>Total Taxes:</td><td><input type="text" style="width:80px;" class="netTaxAmt" name="netTaxAmt" id="netTaxAmt" readonly="readonly" placeholder="Net Result"></td></tr>');

					$("#"+parentTr+" div[class='inputtaxcomponentsdiv']").append('<div class="dynminputtaxcomponentsdiv">');
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('<select id="bocprainputtaxcomponenet" class="inputtaxcomponenet" name="bocprainputtaxcomponenet" placeholder="Tax Name" style="width:87px; margin-top:5px;"><option value="">Select</option></select>');
					for(var i=0;i<data.inputTaxList.length;i++){
						$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv'] select[name='bocprainputtaxcomponenet']").append('<option value="'+data.inputTaxList[i].inputTaxID+'">'+data.inputTaxList[i].inputTaxName+'</option>');
					}
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('&nbsp;<input type="text" id="bocprainputtaxcomponenetamt" class="inputtaxcomponenetValue" name="bocprainputtaxcomponenetamt" placeholder="Value" style="width:49px;margin-top:5px;" onkeyup="totalInputTaxAmount(this); calculateNetAmount(this);" onkeypress="return onlyDotsAndNumbers(event);"></div>');
				}else *//*if(parentTr=="bocapltrid"){

					$("#"+parentTr+" div[class='inputtaxcomponentsdiv']").append('<div class="dynminputtaxcomponentsdiv">');
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('<select id="bocaplinputtaxcomponenet" name="bocaplinputtaxcomponenet" placeholder="Tax Name" style="width:87px; margin-top:5px;"><option value="">Select</option></select>');
					for(var i=0;i<data.inputTaxList.length;i++){
						$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv'] select[name='bocaplinputtaxcomponenet']").append('<option value="'+data.inputTaxList[i].inputTaxID+'">'+data.inputTaxList[i].inputTaxName+'</option>');
					}
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('&nbsp;<input type="text" id="bocaplinputtaxcomponenetamt" name="bocaplinputtaxcomponenetamt" placeholder="Value" style="width:49px;margin-top:5px;" onkeyup="totalInputTaxAmount(this); calculateNetAmount(this);" onkeypress="return onlyDotsAndNumbers(event);"></div>');

				} else if(parentTr=="bptycatrid"){

					$("#"+parentTr+" div[class='inputtaxcomponentsdiv']").append('<div class="dynminputtaxcomponentsdiv">');
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('<select id="bptycainputtaxcomponenet" name="bptycainputtaxcomponenet" placeholder="Tax Name" style="width:87px; margin-top:5px;"><option value="">Select</option></select>');
					for(var i=0;i<data.inputTaxList.length;i++){
						$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv'] select[name='bptycainputtaxcomponenet']").append('<option value="'+data.inputTaxList[i].inputTaxID+'">'+data.inputTaxList[i].inputTaxName+'</option>');
					}
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('&nbsp;<input type="text" id="bptycainputtaxcomponenetamt" name="bptycainputtaxcomponenetamt" placeholder="Value" style="width:49px;margin-top:5px;" onkeyup="totalInputTaxAmount(this); calculateNetAmount(this);" onkeypress="return onlyDotsAndNumbers(event);"></div>');
				}
				else if(parentTr=="prtfcvtrid"){
					$("#"+parentTr+" div[class='inputtaxcomponentsdiv']").append('<div class="dynminputtaxcomponentsdiv">');
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('<select id="prtfcvinputtaxcomponenet" name="prtfcvinputtaxcomponenet" placeholder="Tax Name" style="width:87px; margin-top:5px;"><option value="">Select</option></select>');
					for(var i=0;i<data.inputTaxList.length;i++){
						$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv'] select[name='prtfcvinputtaxcomponenet']").append('<option value="'+data.inputTaxList[i].inputTaxID+'">'+data.inputTaxList[i].inputTaxName+'</option>');
					}
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('&nbsp;<input type="text" id="prtfcvinputtaxcomponenetamt" name="prtfcvinputtaxcomponenetamt" placeholder="Value" style="width:49px;margin-top:5px;" onkeyup="totalInputTaxAmount(this); calculateNetAmount(this);" onkeypress="return onlyDotsAndNumbers(event);"></div>');

				}
				else if(parentTr.indexOf("transactionEntity") != -1){ //For Edit Transaction
					$("#"+parentTr+" div[class='inputtaxcomponentsdiv']").append('<div class="dynminputtaxcomponentsdiv">');
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('<select id="bocprainputtaxcomponenet" name="bocprainputtaxcomponenet" placeholder="Tax Name" style="width:87px; margin-top:5px;"><option value="">Select</option></select>');
					for(var i=0;i<data.inputTaxList.length;i++){
						$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv'] select[name='bocprainputtaxcomponenet']").append('<option value="'+data.inputTaxList[i].inputTaxID+'">'+data.inputTaxList[i].inputTaxName+'</option>');
					}
					$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv']").append('&nbsp;<input type="text" id="" name="bocprainputtaxcomponenetamt" placeholder="Value" style="width:49px;margin-top:5px;" onkeyup="totalInputTaxAmount(this); calculateNetAmount(this);" onkeypress="return onlyDotsAndNumbers(event);"></div>');
				}

				$("#noOfInputTaxesAdd").val(parseInt(inputTaxesLength) + 1);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
		    		swal("Error on fetching tax detail!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
	   });
	}else{
		$("#noOfInputTaxesAdd").val(parseInt(inputTaxesLength) + 1);
		var content=$("#"+parentTr+" div[class='inputtaxcomponentsdiv'] div[class='dynminputtaxcomponentsdiv']").html();
		$("#"+parentTr+" div[class='inputtaxcomponentsdiv']").append('<div class="dynminputtaxcomponentsdiv' + inputTaxesLength + '">');
		$("#"+parentTr+" div[class='dynminputtaxcomponentsdiv" + inputTaxesLength +"']").append(content + '</div>');
	}
}*/

/*function totalInputTaxAmount(elem){
	var parentTr=$(elem).parent().parent().parent().parent().attr('id');
	var parentDiv=$(elem).parent().attr('class');*/
	/*if(parentTr=="bocpratrid"){
	if(parentTr=="multipleBuyTaxesBreakupTable"){
		var taxTotal="0.0";
		//var taxName = $("#"+parentTr+" div[class='"+parentDiv+"'] select[name='bocprainputtaxcomponenet'] option:selected").text();
		var taxName = $("#"+parentTr+" select[name='bocprainputtaxcomponenet'] option:selected").text();
		if(taxName == "Select"){
			swal("Incomplete tax data!", "Please first choose TaxName from dropdown.", "error");
			$("#"+parentTr+" div[class='"+parentDiv+"'] input[name='bocprainputtaxcomponenetamt']").val("");
		}else{
			$("#"+parentTr+" input[name='bocprainputtaxcomponenetamt']").each(function(){
				if($(this).val()!=""){
					taxTotal=parseFloat(taxTotal)+parseFloat($(this).val());
				}
			});
			if(parseFloat(taxTotal)>0){
				$("#"+parentTr+" input[name='inputtaxesamt']").val(parseFloat(taxTotal));
			}else{
				$("#"+parentTr+" input[name='inputtaxesamt']").val("");
				$("#"+parentTr+" input[name='inputtaxesamt']").attr('placeholder',"Amount");
			}
		}
	}*/
	/*if(parentTr=="bocapltrid"){
		var taxTotal="0.0";
		var taxName = $("#"+parentTr+" div[class='"+parentDiv+"'] select[name='bocaplinputtaxcomponenet'] option:selected").text();
		if(taxName == "Select"){
			swal("Incomplete tax data!", "Please first choose TaxName from dropdown.", "error");
			$("#"+parentTr+" div[class='"+parentDiv+"'] input[name='bocaplinputtaxcomponenetamt']").val("");
		}else{
			$("#"+parentTr+" input[name='bocaplinputtaxcomponenetamt']").each(function(){
				if($(this).val()!=""){
					taxTotal=parseFloat(taxTotal)+parseFloat($(this).val());
				}
			});
			if(parseFloat(taxTotal)>0){
				$("#"+parentTr+" input[name='inputtaxesamt']").val(parseFloat(taxTotal));
			}else{
				$("#"+parentTr+" input[name='inputtaxesamt']").val("");
				$("#"+parentTr+" input[name='inputtaxesamt']").attr('placeholder',"Amount");
			}
		}
	}
	if(parentTr=="bptycatrid"){
		var taxTotal="0.0";
		$("#"+parentTr+" input[name='bptycainputtaxcomponenetamt']").each(function(){
			if($(this).val()!=""){
				if($("#"+parentDiv+" input[name='bptycainputtaxcomponenet']").val()==""){
					$(this).val("");
				}else{
					taxTotal=parseFloat(taxTotal)+parseFloat($(this).val());
				}
			}
		});
		if(parseFloat(taxTotal)>0){
			$("#"+parentTr+" input[name='inputtaxesamt']").val(parseFloat(taxTotal));
		}else{
			$("#"+parentTr+" input[name='inputtaxesamt']").val("");
			$("#"+parentTr+" input[name='inputtaxesamt']").attr('placeholder',"Amount");
		}
	}
	if(parentTr.indexOf("transactionEntity") != -1){
		var taxTotal="0.0";
		//var taxTotal = "#"+parentTr+" input[name='bocprainputtaxcomponenetamt']";
		$("#"+parentTr+" input[name='bocprainputtaxcomponenetamt']").each(function(){
			if($(this).val()!=""){
				if($("#"+parentDiv+" input[name='bocprainputtaxcomponenet']").val()==""){
					$(this).val("");
				}else{
					taxTotal=parseFloat(taxTotal)+parseFloat($(this).val());
				}
			}
		});
		if(parseFloat(taxTotal)>0){
			$("#"+parentTr+" input[name='inputtaxesamt']").val(parseFloat(taxTotal));
		}else{
			$("#"+parentTr+" input[name='inputtaxesamt']").val("");
			$("#"+parentTr+" input[name='inputtaxesamt']").attr('placeholder',"Amount");
		}
	}
}*/

function whenAdvanceIsEmpty(elem){
    var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
    var adjustmentClass = $(elem).attr('class');
    if(transactionPurposeId != SELL_ON_CASH_COLLECT_PAYMENT_NOW && transactionPurposeId != SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
        var returnValue = calculateDiscountSell(elem);
        if (returnValue === false) {
            return false;
        }
        returnValue = calculateGross(elem);
        if (returnValue === false) {
            return false;
        }
   	}

	var value=$(elem).val();
	var parentTr=$(elem).parent().parent('tr:first').attr('id');
	$("#"+parentTr+" input[class='paymentreceivedmade']").val("");
	$("#"+parentTr+" input[class='dueBalance']").val("");
	var masterListSelected=$("#"+parentTr+" input[class='customerAdvance']").val();
	if(masterListSelected==""){
		swal("No data found!", "There is no advance from the customer/vendor to be adjusted.", "error");
		$(elem).val("");
		//calculateNetAmount(elem);

		//return false;
	}
	if(parseFloat(value)>parseFloat(masterListSelected)){
		swal("Exceeding data limit!", "Advance adjustment cannot be more than advance available.", "error");
		$("#"+parentTr+" .netAmountDescriptionDisplay").text("");
		$(elem).val("");
		//calculateNetAmount(elem);

		//return false;
	}
	//logic to check for advance adjustment should not exceed with the gross value in case when advance in the customer/vendor account is more than the gross amount

	if(transactionPurposeId == SELL_ON_CASH_COLLECT_PAYMENT_NOW || transactionPurposeId== SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
		//var grossValue=$("#soccpngross").val();
		var txnGrossAmount=$("#"+parentTr+" input[class='invoiceValue']").val();
		var txnTaxAmount=$("#"+parentTr+" input[class='txnTaxAmount']").val();
		var netAmountValue=parseFloat(txnGrossAmount)+ parseFloat(txnTaxAmount);
		if(parseFloat(value)>parseFloat(netAmountValue)){
			swal("Exceeding data limit!", "Advance adjustment cannot be more than Invoice value.", "error");
			$(elem).val("");
			$("#"+parentTr+" input[id='netAmountVal']").val(txnGrossAmount);
			return false;
		}
	}

	if(parentTr=="bocapltrid"){
		var netAmountValue=$("#bocaplnetamnt").val();
		if(parseFloat(value)>parseFloat(netAmountValue)){
			swal("Exceeding data limit!", "Advance adjustment cannot be more than Net Amount.", "error");
			$(elem).val("");
			calculateNetAmount(elem);
			return false;
		}
	}
}

function validateCashPettyAccount(elem){
	var enteredAmount=$(elem).val();
	var resultantCashAccount=$("#branchCashAvailable").val();
	var pettyCashAvailable=$("#branchPettyCashAvailable").val();
	if(enteredAmount==""){
		$(elem).val("");
		$("#branchCashResultant").val("");
		$("#branchPettyCashResultant").val("");
		return true;
	}
	if(enteredAmount!=""){
		if(resultantCashAccount!=""){
			if(parseFloat(enteredAmount)>parseFloat(resultantCashAccount)){
				swal("Exceeding account limit!", "Cannot Transsfer Amount greater than amount available in branch cash account", "error");
				$(elem).val("");
				$("#branchCashResultant").val("");
				$("#branchPettyCashResultant").val("");
				return true;
			}
			$("#branchCashResultant").val((parseFloat(resultantCashAccount)-parseFloat(enteredAmount)).toFixed(2));
		}else{
			swal("Exceeding account limit!", "Cannot Transsfer Amount greater than amount available in branch cash account", "error");
			$(elem).val("");
			$("#branchCashResultant").val("");
			$("#branchPettyCashResultant").val("");
			return true;
		}
		if(pettyCashAvailable!=""){
			$("#branchPettyCashResultant").val((parseFloat(pettyCashAvailable)+parseFloat(enteredAmount)).toFixed(2));
		}else{
			$("#branchPettyCashResultant").val(parseFloat(enteredAmount).toFixed(2));
		}
	}
}


function populateItemBasedOnWhatYouWantToDo(transactionPurposeId, text){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		$(".budgetDisplay").text("");
		$(".actualbudgetDisplay").text("");
		$(".branchAvailablePettyCash").html("");
		$(".amountRangeLimitRule").text("");
		$(".inputtaxbuttondiv").html("");
		$(".inputtaxcomponentsdiv").html("");
		$(".vendorActPayment").text("")
		$(".withholdingtaxcomponentdiv").text("");
		$(".individualtaxdiv").text("");
		$(".individualtaxformuladiv").text("");
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.txnPurposeId=transactionPurposeId;
		var url="/transaction/getTransactionPurposeItems";
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
				if(text=="Sell on cash & collect payment now"){
					$("#soccpnTxnForBranches").children().remove();
					$("#allTxnProjectPurposeData").children().remove();
					$("#soccpnItem").children().remove();
					$("#soccpnTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#allTxnProjectPurposeData").append('<option value="">--Please Select--</option>');
					$("#soccpnCustomer").children().remove();
					$("#soccpnCustomer").append('<option value="">--Please Select--</option>');
					$("#soccpnItem").append('<option value="">--Please Select--</option>');
					let txnBranchesTmp = "";
					for(let i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#soccpnTxnForBranches").append(txnBranchesTmp);
				}else if(text=="Sell on credit & collect payment later"){
					$("#soccplTxnForBranches").children().remove();
					$("#soccplTxnForProjects").children().remove();
					$("#soccplItem").children().remove();
					$("#soccplTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#soccplTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#soccplCustomer").children().remove();
					$("#soccplCustomer").append('<option value="">--Please Select--</option>');
					$("#soccplItem").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#soccplTxnForBranches").append(txnBranchesTmp);
				}else if(text=="Buy on cash & pay right away" || text=="Buy on Petty Cash Account" || text=="Buy on credit & pay later"){
					$("#bocpraTxnForBranches").children().remove();
					$("#bocpraTxnForProjects").children().remove();
					$("#bocpraItem").children().remove();
					$("#bocpraTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#bocpraTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#bocpraVendor").children().remove();
					$("#bocpraVendor").append('<option value="">--Please Select--</option>');
					$("#bocpraItem").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#bocpraTxnForBranches").append(txnBranchesTmp);
				}else if(text=="Buy on Petty Cash Account"){
					$("#bptycaTxnForBranches").children().remove();
					$("#bptycaTxnForProjects").children().remove();
					$("#bptycaItem").children().remove();
					$("#bptycaTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#bptycaTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#bptycaVendor").children().remove();
					$("#bptycaVendor").append('<option value="">--Please Select--</option>');
					$("#bptycaItem").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#bptycaTxnForBranches").append(txnBranchesTmp);
				}else if(text=="Buy on credit & pay later"){
					$("#bocaplTxnForBranches").children().remove();
					$("#bocaplTxnForProjects").children().remove();
					$("#bocaplItem").children().remove();
					$("#bocaplTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#bocaplTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#bocaplVendor").children().remove();
					$("#bocaplVendor").append('<option value="">--Please Select--</option>');
					$("#bocaplItem").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#bocaplTxnForBranches").append(txnBranchesTmp);
				}else if(text=="Transfer main cash to petty cash"){
					$("#tmtpcaTxnForBranches").children().remove();
					$("#tmtpcaTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#branchCashAvailable").val("");
					$("#branchPettyCashAvailable").val("");
					$("#branchTransferrableAmount").val("");
					$("#transferPurpose").val("");
					$("#branchCashResultant").val("");
					$("#branchPettyCashResultant").val("");
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						$("#tmtpcaTxnForBranches").append('<option value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
				}else if(text=="Withdraw Cash From Bank" || transactionPurposeId=="22" || text=="Deposit Cash In Bank" || transactionPurposeId=="23" || text=="Transfer Funds From One Bank To Another" || transactionPurposeId=="24"){
					$("#wcafbnkTxnForBranches").children().remove();
					$("#dcaibnkTxnForBranches").children().remove();
					$("#tfftbnkTxnFromBranches").children().remove();
					$("#wcafbnkTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#dcaibnkTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#tfftbnkTxnFromBranches").append('<option value="">--Please Select--</option>');
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						$(".txnBranches").append('<option value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
				}else if(transactionPurposeId == RECEIVE_ADVANCE_FROM_CUSTOMER){
					$("#transactionDetailsRCAFCCTable .txnBranches").children().remove();
					$("#transactionDetailsRCAFCCTable .txnBranches").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#transactionDetailsRCAFCCTable .txnBranches").append(txnBranchesTmp);
				}else if(text=="Receive payment from customer" || transactionPurposeId=="5" || text=="Pay vendor/supplier" || transactionPurposeId=="7" || transactionPurposeId=="12" || transactionPurposeId=="13" || transactionPurposeId=="47" || text=="Receive Payment from Customer for Astute" || transactionPurposeId=="47" || text=="Pay Vendor/Supplier for Astute"){
					$("#rcpfccSecondTableForMultiInvoice").find("tr:gt(1)").remove();
					$("#rcpfccCustomers").children().remove();
					$("#rcpfccCustomers").append('<option value="">--Please Select--</option>');
					$("#rcpfccpendingInvoices").children().remove();
					$("#rcpfccpendingInvoices").append('<option value="">--Please Select--</option>');
					$("#mcpfcvSecondTableForMultiInvoice").find("tr:gt(1)").remove();
					$("#mcpfcvVendors").children().remove();
					$("#mcpfcvVendors").append('<option value="">--Please Select--</option>');
					$("#mcpfcvpendingInvoices").children().remove();
					$("#mcpfcvpendingInvoices").append('<option value="">--Please Select--</option>');
					$(".txnBranches").children().remove();
					if(text!="Receive Payment from Customer for Astute" && text!="Pay Vendor/Supplier for Astute")
						$(".txnBranches").append('<option value="">--Please Select--</option>');
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						$(".txnBranches").append('<option value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					if(text=="Receive Payment from Customer for Astute")
						$("#rcpfccAstuteTxnForBranches").multiselect('rebuild');
						if(text=="Pay Vendor/Supplier for Astute")
							$("#mcpfcvAstuteTxnForBranches").multiselect('rebuild');
				}else if(transactionPurposeId ==PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
					$(".txnForProjects").children().remove();
					$(".txnForProjects").append('<option value="">--Please Select--</option>');
					$("#transactionDetailsPCAFCVTable .txnBranches").children().remove();
					$("#transactionDetailsPCAFCVTable .txnBranches").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#transactionDetailsPCAFCVTable .txnBranches").append(txnBranchesTmp);
				}
				else if(transactionPurposeId==PREPARE_QUOTATION){  //quotation
					$("#quotationTxnForBranches").children().remove();
					$("#quotationTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#quotationTxnForProjects").children().remove();
					$("#quotationTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#quotationCustomer").children().remove();
					$("#quotationCustomer").append('<option value="">--Please Select--</option>');
					$("#quotationItem").children().remove();
					$("#quotationItem").append('<option value="">--Please Select--</option>');
					var branchesTemp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						branchesTemp += ('<option value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#quotationTxnForBranches").append(branchesTemp);
				}else if(transactionPurposeId==PURCHASE_ORDER){
					$("#purordTxnForBranches").children().remove();
					$("#purordTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#purordTxnForProjects").children().remove();
					$("#purordTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#purordVendor").children().remove();
					$("#purordVendor").append('<option value="">--Please Select--</option>');
					var branchesTemp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						branchesTemp += ('<option value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#purordTxnForBranches").append(branchesTemp);
				}else if(transactionPurposeId==PROFORMA_INVOICE){
					$("#proformaTxnForBranches").children().remove();
					$("#proformaTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#proformaTxnForProjects").children().remove();
					$("#proformaTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#proformaCustomer").children().remove();
					$("#proformaCustomer").append('<option value="">--Please Select--</option>');
					$("#proformaItem").children().remove();
					$("#proformaItem").append('<option value="">--Please Select--</option>');
					var branchesTemp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						branchesTemp += ('<option value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#proformaTxnForBranches").append(branchesTemp);
				}else if(transactionPurposeId==CREDIT_NOTE_CUSTOMER || transactionPurposeId == DEBIT_NOTE_CUSTOMER){
					$("#cdtdbtTxnForBranches").children().remove();
					$("#cdtdbtTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#cdtdbtTxnForProjects").children().remove();
					$("#cdtdbtTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#cdtdbtCustomer").children().remove();
					$("#cdtdbtCustomer").append('<option value="">--Please Select--</option>');
					$("#cdtdbtItem").children().remove();
					$("#cdtdbtItem").append('<option value="">--Please Select--</option>');
                    var txnBranchesTmp = "";
                    for(var i=0; i<data.allTxnBranchPurposeData.length; i++) {
                        txnBranchesTmp += ('<option id="' + data.allTxnBranchPurposeData[i].gstin + '" value="' + data.allTxnBranchPurposeData[i].id + '">' + data.allTxnBranchPurposeData[i].name + '</option>');
                    }
					$("#cdtdbtTxnForBranches").append(txnBranchesTmp);
				}else if(transactionPurposeId==CREDIT_NOTE_VENDOR || transactionPurposeId == DEBIT_NOTE_VENDOR){
					$("#cdtdbvTxnForBranches").children().remove();
					$("#cdtdbvTxnForProjects").children().remove();
					$("#cdtdbvItem").children().remove();
					$("#cdtdbvTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#cdtdbvTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#cdtdbvVendor").children().remove();
					$("#cdtdbvVendor").append('<option value="">--Please Select--</option>');
					$("#cdtdbvItem").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#cdtdbvTxnForBranches").append(txnBranchesTmp);
				}else if(transactionPurposeId==TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
                    $("#tifbtbFromBranch").children().remove();
                    $("#tifbtbFromBranch").append('<option value="">--Please Select--</option>');
                    $("#tifbtbToBranch").children().remove();
                    $("#tifbtbToBranch").append('<option value="">--Please Select--</option>');
                    $("#tifbtbItem").children().remove();
                    $("#tifbtbItem").append('<option value="">--Please Select--</option>');
                    var txnBranchesTmp = "";
                    for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
                        txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
                    }
                    var transferType = $("#tifbtbtrid select[class='inwardOutwardSelect'] option:selected").val();
                    if(transferType === ""){
                        swal("Incomplete transaction data!", "Please select "+whatYouWantToDo+" type", "error");
                        return false;
                    }else if(transferType == '1'){
                        $("#tifbtbFromBranch").append(txnBranchesTmp);
                        var tempArr = Object.values(ALL_BRANCH_OF_ORG_MAP);
                        var branchlist = tempArr.join('');
                        $("#tifbtbToBranch").append(branchlist);
					}else if(transferType == '2') {
                        var tempArr = Object.values(ALL_BRANCH_OF_ORG_MAP);
                        var branchlist = tempArr.join('');
                        $("#tifbtbFromBranch").append(branchlist);
                        $("#tifbtbToBranch").append(txnBranchesTmp);
                    }
				} else if(transactionPurposeId==PROCESS_PAYROLL){
					$("#payrollTxnForBranches").children().remove();
					$("#payrollTxnForBranches").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
						txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
					}
					$("#payrollTxnForBranches").append(txnBranchesTmp);

				}else if(transactionPurposeId==REFUND_ADVANCE_RECEIVED){
                    $("#mkrfndTxnForBranches").children().remove();
                    $("#mkrfndTxnForBranches").append('<option value="">--Please Select--</option>');
                    $("#mkrfndItems").children().remove();
                    $("#mkrfndItems").append('<option value="">--Please Select--</option>');
                    var txnBranchesTmp = "";
                    for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
                        txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
                    }
                    $("#mkrfndTxnForBranches").append(txnBranchesTmp);
                }else if(transactionPurposeId==REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
                    $("#rfndAmntRcvdTxnForBranches").children().remove();
                    $("#rfndAmntRcvdTxnForBranches").append('<option value="">--Please Select--</option>');
                    $("#rfndamntrcvdItems").children().remove();
                    $("#rfndamntrcvdItems").append('<option value="">--Please Select--</option>');
                    var txnBranchesTmp = "";
                    for(var i=0;i<data.allTxnBranchPurposeData.length;i++){
                        txnBranchesTmp += ('<option id="'+data.allTxnBranchPurposeData[i].gstin+'" value="'+data.allTxnBranchPurposeData[i].id+'">'+data.allTxnBranchPurposeData[i].name+'</option>');
                    }
                    $("#rfndAmntRcvdTxnForBranches").append(txnBranchesTmp);
                }else if(transactionPurposeId == CANCEL_INVOICE){
					$("#caninvTxnForBranches").children().remove();
					$("#caninvTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#caninvTxnForProjects").children().remove();
					$("#caninvTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#caninvCustomer").children().remove();
					$("#caninvCustomer").append('<option value="">--Please Select--</option>');
					$("#caninvItem").children().remove();
					$("#caninvItem").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0; i<data.allTxnBranchPurposeData.length; i++) {
						txnBranchesTmp += ('<option id="' + data.allTxnBranchPurposeData[i].gstin + '" value="' + data.allTxnBranchPurposeData[i].id + '">' + data.allTxnBranchPurposeData[i].name + '</option>');
					}
					$("#caninvTxnForBranches").append(txnBranchesTmp);
                }else if(transactionPurposeId == BILL_OF_MATERIAL){
					$("#bomTxnForBranches").children().remove();
					$("#bomTxnForBranches").append('<option value="">--Please Select--</option>');
					$("#bomTxnForProjects").children().remove();
					$("#bomTxnForProjects").append('<option value="">--Please Select--</option>');
					$("#bomIsEdit").val("");

					$("#txnbomSelect").children().remove();
					$("#txnbomSelect").append('<option value="">--Please Select--</option>');

					$("#bomItem").children().remove();
					$("#bomItem").append('<option value="">--Please Select--</option>');
					$("#bomRemark").val("");
					var txnBranchesTmp = "";
					for(var i=0; i<data.allTxnBranchPurposeData.length; i++) {
						txnBranchesTmp += ('<option id="' + data.allTxnBranchPurposeData[i].gstin + '" value="' + data.allTxnBranchPurposeData[i].id + '">' + data.allTxnBranchPurposeData[i].name + '</option>');
					}
					$("#bomTxnForBranches").append(txnBranchesTmp);
                }else if(transactionPurposeId == CREATE_PURCHASE_REQUISITION || transactionPurposeId == CREATE_PURCHASE_ORDER || transactionPurposeId == MATERIAL_ISSUE_NOTE) {
					var poType = $("#purchaseOrderCategoryId").find('option:selected').val();
					var mainTableTr = 'crtprrtrid';
                    if (transactionPurposeId == CREATE_PURCHASE_ORDER) {
                        if (poType == "req")
                            mainTableTr = "poareqtrid";
                        else if (poType == "bom")
                            mainTableTr = "poabomtrid";
                    }else if (transactionPurposeId == CREATE_PURCHASE_REQUISITION) {
						if (poType == "npr")
							mainTableTr = "purreqtrid";
						else if (poType == "praso")
							mainTableTr = "prqasotrid";
					} else if (transactionPurposeId == MATERIAL_ISSUE_NOTE){
                        mainTableTr = 'matinttrid';
                    }
					$("#"+mainTableTr+ " .txnBranches").children().remove();
                    $("#"+mainTableTr+ " .txnBranches").append('<option value="">--Please Select--</option>');
                    $("#"+mainTableTr+ " .txnForProjects").children().remove();
                    $("#"+mainTableTr+ " .txnForProjects").append('<option value="">--Please Select--</option>');
                    $("#"+mainTableTr+ " .masterList").children().remove();
                    $("#"+mainTableTr+ " .masterList").append('<option value="">--Please Select--</option>');
					$(".txnItems").children().remove();
					$(".txnItems").append('<option value="">--Please Select--</option>');
					var txnBranchesTmp = "";
					for(var i=0; i<data.allTxnBranchPurposeData.length; i++) {
						txnBranchesTmp += ('<option id="' + data.allTxnBranchPurposeData[i].gstin + '" value="' + data.allTxnBranchPurposeData[i].id + '">' + data.allTxnBranchPurposeData[i].name + '</option>');
					}
                    $("#"+mainTableTr+ " .txnBranches").append(txnBranchesTmp);
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
		    		swal("Error on transaction data!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
	   });
}

function getInvoices(elem){
	$("#rcpfccvendcustoutstandingsgross").text("");
	$("#rcpfccvendcustoutstandingsnet").text("");
	$("#rcpfccvendcustoutstandingsnetdescription").text("");
	$("#rcpfccvendcustoutstandingspaid").text("");
	$("#rcpfccvendcustoutstandingsnotpaid").text("");
	$("#rcpfccvendcustoutstandingssalesreturn").text("");
	$("#mcpfcvvendcustoutstandingsgross").text("");
	$("#mcpfcvvendcustoutstandingsnet").text("");
	$("#mcpfcvvendcustoutstandingsnetdescription").text("");
	$("#mcpfcvvendcustoutstandingspaid").text("");
	$("#mcpfcvvendcustoutstandingsnotpaid").text("");
	$("#mcpfcvtxninprogress").text("");
	$(".vendorActPayment").text("");
	$("#mcpfcvvendcustoutstandingspurchasereturn").text("");
	var parentTr=$(elem).parent().parent().attr('id');
	$("#"+parentTr+" div[class='klBranchSpecfTd']").text("");
	$("#"+parentTr+" input[class='paymentreceivedmade']").val("");
	$("#"+parentTr+" input[class='dueBalance']").val("");
	$(".secondTableForMultiInvoice .pendingTxns").children().remove();
	$(".secondTableForMultiInvoice .pendingTxns").append('<option value="">--Please Select--</option>');
	var selectedTransactionBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	if(selectedTransactionBranch==""){
		swal("Incomplete transaction data!", 'You must select a Branch to proceed this transactions.', 'warning');
		$("#"+parentTr+" select[class='txnBranches']").focus();
		$(elem).find('option:first').prop("selected","selected");
		enableTransactionButtons();
		return false;
	}
    var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	var selectedCustomerVendor=$(elem).find("option:selected").val();
	var txnDate=$("#"+parentTr).find(".txnBackDate").val();
	if(selectedCustomerVendor!=""){
		var jsonData = {};
		var useremail=$("#hiddenuseremail").text();
		jsonData.usermail = useremail;
		jsonData.selectedCustVend=selectedCustomerVendor;
		jsonData.txnBranchId = selectedTransactionBranch;
		jsonData.txnPurposeId = transPurposeId;
		jsonData.txnDate = txnDate;
		var url="/transaction/getCustVendPendingInvoices";
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
				$("#"+parentTr+" select[class='pendingTxns']").children().remove();
				parentTr = parentTr + '0';
				for(var i=0;i<data.allCustomerVendorsPendingInvoicesData.length;i++){
					$("#"+parentTr+" select[class='pendingTxns']").append('<option value="'+data.allCustomerVendorsPendingInvoicesData[i].id+'" billId="'+data.allCustomerVendorsPendingInvoicesData[i].opId + '" pendingTxn="' + data.allCustomerVendorsPendingInvoicesData[i].pendingPayVendors +'">'+data.allCustomerVendorsPendingInvoicesData[i].dateNetAmount+'</option>');
				}
				$("#"+parentTr+" div[class='klBranchSpecfTd']").text("");
				if(typeof data.allCustomerVendorsPendingInvoicesData[0].vendorCustomerGroupKl !='undefined' && data.allCustomerVendorsPendingInvoicesData[0].vendorCustomerGroupKl!=null && data.allCustomerVendorsPendingInvoicesData[0].vendorCustomerGroupKl!=""){
					$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<input type="radio" name="klfollowed" id="klfollowedyes" value="1"/>Yes &nbsp;&nbsp;<input type="radio" name="klfollowed" id="klfollowedno" value="0"/>No');
					$("#"+parentTr+" div[class='klBranchSpecfTd']").append('<br/>'+data.allCustomerVendorsPendingInvoicesData[0].vendorCustomerGroupKl+'.');
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
		    		swal("Error on fetching pending invoices!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
}

$(document).ready(function() {
	$(".removeItemForTransaction").click(function(){
	    var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
		var parentTr = 0;var trLen = 0;
		if(transactionPurposeId == RECEIVE_PAYMENT_FROM_CUSTOMER || transactionPurposeId == PAY_VENDOR_SUPPLIER || transactionPurposeId == REVERSAL_OF_ITC){
			parentTr = $(this).closest('div').attr('id');
			trLen =$("#"+parentTr+" tbody tr").length;
		} else {
			parentTr = $(this).closest('div').parent().attr('id');
			trLen =$("#"+parentTr+" table[class='multipleItemsTable'] tbody tr").length;
		}
		var selectedRows = $("#"+parentTr).find(".removeTxnCheckBox:checkbox:checked").length;
 		if(selectedRows == 0){
			swal("Select Items First!", "Please choose the items from List", "error");
			return false;
		}
		if(selectedRows > 0 && (parseInt(trLen) > 1 && selectedRows < trLen)) {
			$("#"+parentTr).find(".removeTxnCheckBox:checkbox:checked").each(function(){
				var selectedTr = $(this).closest('tr').attr('id');
				var itemNetamunt =  $("#"+selectedTr+" input[class='netAmountVal']").val();
				var totalNetAmount =  $("#"+parentTr+" input[class='netAmountValTotal']").val();
				if(itemNetamunt != "" && totalNetAmount != ""){
					totalNetAmount = parseFloat(totalNetAmount) - parseFloat(itemNetamunt);
					$("#"+parentTr+" input[class='netAmountValTotal']").val(Math.round(totalNetAmount).toFixed(2));
				}
				if(transactionPurposeId == SELL_ON_CASH_COLLECT_PAYMENT_NOW || transactionPurposeId == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || transactionPurposeId == CREDIT_NOTE_CUSTOMER || transactionPurposeId == DEBIT_NOTE_CUSTOMER){
					var invoiceValue = $("#"+selectedTr+" input[class='invoiceValue']").val();
					var totalInvoiceValue =  $("#"+parentTr+" input[class='totalInvoiceValue']").val();
					if(invoiceValue != "" && totalInvoiceValue != ""){
						totalInvoiceValue = parseFloat(totalInvoiceValue) - parseFloat(invoiceValue);
						$("#"+parentTr+" input[class='totalInvoiceValue']").val(totalInvoiceValue);
					}
				}
				$("#"+selectedTr).remove();
			});
		}else {
			swal("Item error!", "At least one item is needed to proceed transaction.", "error");
			return false;
		}
	});

	$(".addnewItemForTransaction").click(function(){
		var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
		var mainTableID = $(this).closest('div').parent().attr('id');
		var parentTr = $("#"+mainTableID +" tbody tr").attr('id');
		transPurposeId = parseInt(transPurposeId);

		var txnPurposeText=$("#whatYouWantToDo").find('option:selected').text();
		var vendorcustomer=$("#"+parentTr+" .masterList option:selected").val();

		//var txnForUnavailableCustomer=$("#soccpnUnAvailableCustomer").val();
		var txnForUnavailableCustomer = "";
		if(transPurposeId === SELL_ON_CASH_COLLECT_PAYMENT_NOW || transPurposeId === RECEIVE_ADVANCE_FROM_CUSTOMER || transPurposeId === BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId === BUY_ON_CREDIT_PAY_LATER || transPurposeId === BUY_ON_PETTY_CASH_ACCOUNT){
			txnForUnavailableCustomer=$("#"+parentTr+" input[class='unavailable ui-autocomplete-input']").val();
		}else{
			txnForUnavailableCustomer=$("#"+parentTr+" input[class='unavailable']").val();
		}
		if(vendorcustomer=="" && txnForUnavailableCustomer=="" && transPurposeId != TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
			swal("Incomplete transaction data!", "Before adding item, please choose the customer/vendor from dropdown", "error");
			return false;
		}
		var branchId=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();

		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		if((txnForUnavailableCustomer==="" || typeof txnForUnavailableCustomer === "undefined") && vendorcustomer!="" && branchId!="" && transPurposeId != TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER && transPurposeId != BILL_OF_MATERIAL && transPurposeId != CREATE_PURCHASE_REQUISITION){
			var jsonData = {};
			jsonData.useremail=$("#hiddenuseremail").text();
			jsonData.txnPurposeId=transPurposeId;
			jsonData.txnPurposeText=txnPurposeText;
			jsonData.custVendEntityId=vendorcustomer;
			jsonData.txnBranchID=branchId;
			var url="/transaction/getTxnItemsCustomerVendors";
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
					var customerVendorItemsListTemp = "";

					var transactionListTemp = "";
					if(transPurposeId==REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
						 for(var i=0;i<data.txnItemsCustomerVendorsData.length;i++){
							 transactionListTemp+=('<option value="'+data.txnItemsCustomerVendorsData[i].id+'">'+data.txnItemsCustomerVendorsData[i].name+'</option>');
				    	  }
					}
					else{
						for(var i=0;i<data.txnItemsCustomerVendorsData.length;i++){
							customerVendorItemsListTemp += ('<option value="'+data.txnItemsCustomerVendorsData[i].id+'" category="'+data.txnItemsCustomerVendorsData[i].category+'" id="'+data.txnItemsCustomerVendorsData[i].iseditable+'" combsales="'+data.txnItemsCustomerVendorsData[i].isCombinationSales+'" isTdsSpecific="'+data.txnItemsCustomerVendorsData[i].isTdsSpecific+'">'+data.txnItemsCustomerVendorsData[i].name+'</option>');
						}
					}
                    var currentTr = $("#" + mainTableID +" table[class='multipleItemsTable'] tbody tr:last").attr('id');
                    var length = currentTr.substring(6, currentTr.length);
                    length = parseInt(length) + 1;

					if(transPurposeId == SELL_ON_CASH_COLLECT_PAYMENT_NOW){
						//var length=$("#staticsellmultipleitemssoccpn tbody tr").length;
						var multiItemsTableTr = '<tr id="soccpn'+length+'"><td><select class="txnItems"  name="soccpnItem" id="soccpnItem" onChange="populateSellTranData(this);"><option value="">Please Select</option></select><input type="text" style="margin-top: 3px !important; display: block;" name="soccpnitemBarcode" id="soccpnitemBarcode" class="txnItemBarcode" onkeyup="itemChosen(this);" placeholder="Barcode"></td>';

                        multiItemsTableTr += '<td><div class="klBranchSpecfTd"></div></td>';
						multiItemsTableTr += '<td><input id="txnPerUnitPriceTaxInclusice" style="display:none;width: 60px !important;" placeholder="Price Per Unit" class="txnPerUnitPriceTaxInclusice" type="text" onkeypress="return onlyDotsAndNumbers(event)" onKeyup="calculateInclusiveTaxAndSubmit(this)" />';
						multiItemsTableTr += '<input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text" name="soccpnpriceperunits" id="soccpnpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="validateKnowledgeLib(this); calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);"></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit" type="text" name="soccpnunits" id="soccpnunits" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="validateKnowledgeLib(this); calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" onblur="checkStockAvailableForSellTran(this);"  placeholder="Units(if any)"></td>';
						multiItemsTableTr +='<td><input type="text" class="txnDiscountPercent" name="soccpnDiscountPercent" id="soccpnDiscountPercent" placeholder="%" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);">&#37;<input type="hidden" class="txnDiscountPercentHid" id="soccpndiscounttocust"/>';
						multiItemsTableTr += '<br><input type="text" class="txnDiscountAmount" name="soccpnDiscountAmt" id="soccpnDiscountAmt" readonly="readonly" placeholder="Amount"></td>';
						multiItemsTableTr += '<td><input class="txnGross" placeholder="Gross Amount" type="text" name="soccpngross" id="soccpngross" readonly="readonly"></td>';
						multiItemsTableTr += '<td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" name="soccpntaxtypes" id="soccpntaxtypes"/></td>';

						multiItemsTableTr += '<td><input type="text" class="invoiceValue" placeholder="Invoice Value" name="soccpnInvoiceValue" id="soccpnInvoiceValue" readonly="readonly"/></td>';

						multiItemsTableTr += '<td><input type="text" class="customerAdvance" placeholder="Advance Available" name="soccpncustomeradvance" id="soccpncustomeradvance" readonly="readonly"></td>';

						multiItemsTableTr += '<td><input type="text" class="howMuchAdvance" placeholder="Advance to Adjust?" name="soccpnhowmuchfromadvance" id="soccpnhowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="adjustAdvance(this);"/></td>';
						//multiItemsTableTr += '<td><input type="text" style="width:80px;" class="taxableVal" name="soccpnTaxableVal" id="soccpnTaxableVal" readonly="readonly" placeholder="Taxable Value"></td>'
						multiItemsTableTr += '<td><div id="advAdjTaxCell"><div id="advAdjTaxCell0"></div><div id="advAdjTaxCell1"></div><div id="advAdjTaxCell2"></div><div id="advAdjTaxCell3"></div><div id="advAdjTaxCell4"></div></div></td>';
						multiItemsTableTr += '<td><input type="text" class="netAmountVal" name="soccpnnetamnt" id="soccpnnetamnt" readonly="readonly" placeholder="Net Result"></td>';
                        multiItemsTableTr += '<td><div class="netAmountDescriptionDisplay" id="netAmountLabel"></div></td><td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';
						$("#staticsellmultipleitemssoccpn table[id='multipleItemsTablesoccpn'] tbody").append(multiItemsTableTr);
				    	$("#multipleItemsTablesoccpn > tbody > tr[id='soccpn"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);

					}else if(transPurposeId == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
						var length=$("#staticsellmultipleitemssoccpl tbody tr").length;
						var multiItemsTableTr = '<tr id="soccpl'+length+'"><td><select class="txnItems"  name="soccplItem" id="soccplItem" onChange="populateSellTranData(this);"><option value="">Please Select</option></select><input type="text" style="margin-top: 3px !important; display: block;" name="soccplitemBarcode" id="soccplitemBarcode" class="txnItemBarcode" onkeyup="itemChosen(this);" placeholder="Barcode"></td>';

                        multiItemsTableTr += '<td><div class="klBranchSpecfTd"></div></td>';
						multiItemsTableTr += '<td><input id="txnPerUnitPriceTaxInclusice" style="display:none;width: 60px !important;" placeholder="Price Per Unit" class="txnPerUnitPriceTaxInclusice" type="text" onkeypress="return onlyDotsAndNumbers(event)" onKeyup="calculateInclusiveTaxAndSubmit(this)" />';
						multiItemsTableTr += '<input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text" name="soccplpriceperunits" id="soccplpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="validateKnowledgeLib(this); calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);"></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit" type="text" name="soccplunits" id="soccplunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="validateKnowledgeLib(this); calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" onblur="checkStockAvailableForSellTran(this);" placeholder="Units(if any)"></td>';
						multiItemsTableTr += '<td><input type="text" class="txnDiscountPercent" name="soccplDiscountPercent" id="soccplDiscountPercent" placeholder="%" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);">&#37;<input type="hidden" class="txnDiscountPercentHid" id="soccpldiscounttocust"/>';
						multiItemsTableTr += '<br><input type="text" class="txnDiscountAmount" name="soccplDiscountAmt" id="soccplDiscountAmt" readonly="readonly" placeholder="Amount"></td>';
						multiItemsTableTr += '<td><input class="txnGross" placeholder="Gross Amount" type="text" name="soccplgross" id="soccplgross" readonly="readonly"></td>';
						multiItemsTableTr += '<td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" name="soccpltaxtypes" id="soccpltaxtypes"/></td>';

						multiItemsTableTr += '<td><input type="text" class="invoiceValue" placeholder="Invoice Value" name="soccplInvoiceValue" id="soccplInvoiceValue" readonly="readonly"/></td>';

						multiItemsTableTr += '<td><input type="text" class="customerAdvance" placeholder="Advance Available" name="soccplcustomeradvance" id="soccplcustomeradvance" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" class="howMuchAdvance" placeholder="Advance to Adjust?" name="soccplhowmuchfromadvance" id="soccplhowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="adjustAdvance(this);"></td>';

						multiItemsTableTr += '<td><div id="advAdjTaxCell"><div id="advAdjTaxCell0"></div><div id="advAdjTaxCell1"></div><div id="advAdjTaxCell2"></div><div id="advAdjTaxCell3"></div><div id="advAdjTaxCell4"></div></div></td>';
						multiItemsTableTr += '<td><input type="text" class="netAmountVal" name="soccplnetamnt" id="soccplnetamnt" readonly="readonly" placeholder="Net Result"></td>';
                        multiItemsTableTr += '<td><div class="netAmountDescriptionDisplay" id="netAmountLabel"></div></td><td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';
						$("#staticsellmultipleitemssoccpl table[id='multipleItemsTablesoccpl'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablesoccpl > tbody > tr[id='soccpl"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId == BUY_ON_CREDIT_PAY_LATER || transPurposeId == BUY_ON_PETTY_CASH_ACCOUNT ){
						$("#totalInputTaxes").val(0);
						var parentId = mainTableID;
						var txnTypeOfSupply = $("#"+parentId).find('.txnTypeOfSupply').val();

						var length=$("#staticsellmultipleitemsbocpra tbody tr").length;
						var multiItemsTableTr = '<tr id="bocpra'+length+'"><td><select class="txnItems"  name="bocpraItem" id="bocpraItem" onChange="populateBuyTranData(this);"><option value="">Please Select</option></select><input type="text" style="margin-top: 3px !important; display: block;" name="bocpraitemBarcode" id="bocpraitemBarcode" class="txnItemBarcode" onkeyup="itemChosen(this);" placeholder="Barcode"></td>';

						multiItemsTableTr += '<td><div class="klBranchSpecfTd" style="height: 50px; overflow: auto;"></div></td>';
						if((txnTypeOfSupply == "2" || txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5") && transPurposeId != BUY_ON_PETTY_CASH_ACCOUNT) {
							multiItemsTableTr += '<td class="rcmCompToshow"><select class="txnRcmTaxItem" onChange="validateSelectedItems(this);setRcmTaxAndCess(this);"><option value="">Please Select</option></select></td>';
							if(txnTypeOfSupply == "2" || txnTypeOfSupply == "3") {
								multiItemsTableTr +='<td class="rcmCompToshowGood hidden"><input type="text" class="txnDutiesAndTaxes" value="0" onkeypress="return onlyDotsAndNumbers(event);"  onkeyup="calculateNetAmount(this);"></td>';
							}else {
								multiItemsTableTr +='<td class="rcmCompToshowGood"><input type="text" class="txnDutiesAndTaxes" value="0" onkeypress="return onlyDotsAndNumbers(event);"  onkeyup="calculateNetAmount(this);"></td>';
							}

						}else {
							multiItemsTableTr += '<td class="rcmCompToshow hidden" ><select class="txnRcmTaxItem" onChange="validateSelectedItems(this);setRcmTaxAndCess(this);"><option value="">Please Select</option></select></td>';
							multiItemsTableTr +='<td class="rcmCompToshowGood hidden" ><input type="text" class="txnDutiesAndTaxes" value="0" onkeypress="return onlyDotsAndNumbers(event);" onChange=""></td>';
						}
						multiItemsTableTr += '<td><input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text" name="bocprapriceperunits" id="bocprapriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=" calculateGross(this); calculateNetAmount(this);"/></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit" type="text" name="bocpraunits" id="bocpraunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateGross(this); calculateNetAmount(this);" placeholder="Units(if any)"/></td>';
						multiItemsTableTr += '<td><input class="txnGross" placeholder="Gross Amount" type="text" name="bocpragross" id="bocpragross" readonly="readonly"></td>';
						if((txnTypeOfSupply == "2" || txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5") && transPurposeId != BUY_ON_PETTY_CASH_ACCOUNT) {
							multiItemsTableTr += '<td class="taxRateTdMiddle hidden" ></td>';
							multiItemsTableTr += '<td class="taxAmountTdMiddle hidden" ></td>';
							multiItemsTableTr += '<td class="cessRateTdMiddle hidden" ></td>';
							multiItemsTableTr += '<td class="cessAmountTdMiddle hidden" ></td>';
						}else {
							multiItemsTableTr +='<td class="taxRateTdMiddle"><select class="txnGstTaxRate" id="bocpraTaxRate" onChange="calculateNetAmount(this);"><option value="">Select</option></select></td>';
							multiItemsTableTr +='<td class="taxAmountTdMiddle"><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div></div><input class="txnTaxTypes" placeholder="Taxes" type="hidden" name="bocpraInTaxDetails" id="bocpraInTaxDetails" readonly="readonly"></td>';
							multiItemsTableTr += '<td class="cessRateTdMiddle" ><select class="txnCessRate" id="bocpraCessRate" onChange="calculateNetAmount(this);"><option value=""> Select</option></select></td>';
							multiItemsTableTr += '<td class="cessAmountTdMiddle"><input type="text" class="txnCessTaxAmt" id="bocpraCessAmt" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);"/><input type="hidden" class="itemTaxAmount" name="bocpraTotaltaxamnt" id="bocpraTotaltaxamnt" readonly="readonly" placeholder="Tax Amount"/></td>';
						}
						multiItemsTableTr += '<td><input type="text" class="withholdingtaxcomponenetdiv" name="bocprawithheldingtaxamnt" id="bocprawithheldingtaxamnt" onkeyup="calculateNetAmountOnTdsChange(this);" readonly="readonly" placeholder="WithholdingTax"></td>';
						multiItemsTableTr += '<td><input type="text" class="customerAdvance" placeholder="Advance Available" name="bocpravendoradvance" id="bocpravendoradvance" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" class="howMuchAdvance" placeholder="Advance to Adjust?" name="bocprahowmuchfromadvance" id="bocprahowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenAdvanceChangedForBuy(this);"></td>';
						multiItemsTableTr += '<td><input type="text" class="netAmountVal" name="bocpranetamnt" id="bocpranetamnt" readonly="readonly" placeholder="Net Amount"></td>';
						if((txnTypeOfSupply == "2" || txnTypeOfSupply == "3"  || txnTypeOfSupply == "4" || txnTypeOfSupply == "5") && transPurposeId != BUY_ON_PETTY_CASH_ACCOUNT) {
							multiItemsTableTr +='<td class="taxRateTdLast"><input class="txnGstTaxRate"  type="text" readonly id="bocpraTaxRate" onChange="calculateNetAmount(this);"></td>';
							multiItemsTableTr +='<td class="taxAmountTdLast"><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div></div><input class="txnTaxTypes" placeholder="Taxes" type="hidden" name="bocpraInTaxDetails" id="bocpraInTaxDetails" readonly="readonly"></td>';
							multiItemsTableTr += '<td class="cessRateTdLast"><input class="txnCessRate" type="text" readonly id="bocpraCessRate"  onChange="calculateNetAmount(this);"></td>';
							multiItemsTableTr += '<td class="cessAmountTdLast" ><input type="text" class="txnCessTaxAmt" id="bocpraCessAmt"  onChange=""/><input type="hidden" class="itemTaxAmount" name="bocpraTotaltaxamnt" id="bocpraTotaltaxamnt" readonly="readonly" placeholder="Tax Amount"/></td>';
							multiItemsTableTr += '<td><div class="netAmountDescriptionDisplay" id="bocpranetAmountLabel"></div></td>';
						}else {
							multiItemsTableTr += '<td><div class="netAmountDescriptionDisplay" id="bocpranetAmountLabel"></div></td><td class="taxRateTdLast hidden"></td>';
							multiItemsTableTr += '<td class="taxAmountTdLast hidden" ></td>';
							multiItemsTableTr += '<td class="cessRateTdLast hidden"></td>';
							multiItemsTableTr += '<td class="cessAmountTdLast hidden" ></td>';
						}
						multiItemsTableTr += '<td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';

						$("#staticsellmultipleitemsbocpra table[id='multipleItemsTablebocpra'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablebocpra > tbody > tr[id='bocpra"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
						fetchBranchInTaxesCess($("#"+parentTr+" select[class='txnBranches']"));
						if(transPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId == BUY_ON_CREDIT_PAY_LATER) {
							var isCompositionSchemeApply = $("#isCompositionSchemeApply").val();
						    if(isCompositionSchemeApply == 1) {
						    	$("#staticsellmultipleitemsbocpra table[id='multipleItemsTablebocpra'] tbody > tr:last").find('.txnGstTaxRate').prop('disabled', true);
						    	$("#staticsellmultipleitemsbocpra table[id='multipleItemsTablebocpra'] tbody > tr:last").find('.txnCessRate').prop('disabled', true);
						    }
						}
					}else if(transPurposeId == BUY_ON_CREDIT_PAY_LATER){
						$("#totalInputTaxes").val(0);
						var multiItemsTableTr = '<tr id="bocapl'+length+'"><td><select class="txnItems"  name="bocaplItem" id="bocaplItem" onChange="populateBuyTranData(this);"><option value="">Please Select</option></select></td>';
						multiItemsTableTr += '<td><input class="txnPerUnitPrice"  style="width:50px;" placeholder="Price Per Unit" type="text" name="bocaplpriceperunits" id="bocaplpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=" calculateGross(this); calculateNetAmount(this);"/></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit"  style="width:50px;" type="text" name="bocaplunits" id="bocaplunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateGross(this); calculateNetAmount(this);" placeholder="Units(if any)"/></td>';
						multiItemsTableTr += '<td><input class="txnGross" style="width:80px;" placeholder="Gross Amount" type="text" name="bocaplgross" id="bocaplgross" readonly="readonly"></td></td>';
						multiItemsTableTr += '<td><input type="button" class="btn btn-submit btn-idos input-small" name="inputtaxes" id="inputtaxes" value="Input Tax" style="margin-top:1px; width: 60px; padding: 5px 7px 5px 7px;" onclick="addBuyTaxComponentsPopup(this);"></td>';
						multiItemsTableTr += '<td><input class="txnTaxTypes" style="width:80px;" placeholder="Taxes" type="text" name="bocaplInTaxDetails" id="bocaplInTaxDetails" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:50px;"  class="txnTaxAmount" name="bocapltaxamnt" id="bocapltaxamnt" readonly="readonly" placeholder="Tax Amount"></td>';
						multiItemsTableTr += '<td><input class="withholdingtaxcomponenetdiv" style="width:50px;" placeholder="WithholdingTaxes" type="text" name="bocaplwithheldingtaxamnt" id="bocaplwithheldingtaxamnt" onkeyup="calculateNetAmountOnTdsChange(this);" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:52px;"  class="customerAdvance" placeholder="Advance Available" name="bocaplvendoradvance" id="bocaplvendoradvance" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:75px;"  class="howMuchAdvance" placeholder="Advance to Adjust?" name="bocaplhowmuchfromadvance" id="bocaplhowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="whenAdvanceIsEmpty(this); calculateNetAmountWhenAdvanceChangedForBuy(this);"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:80px;" class="netAmountVal" name="bocaplnetamnt" id="bocaplnetamnt" readonly="readonly" placeholder="Net Result"></td></tr>';
						$("#staticsellmultipleitemsbocapl table[id='multipleItemsTablebocapl'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablebocapl > tbody > tr[id='bocapl"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == BUY_ON_PETTY_CASH_ACCOUNT){
						$("#totalInputTaxes").val(0);
						var multiItemsTableTr = '<tr id="bptyca'+length+'"><td><select class="txnItems"  name="bptycaItem" id="bptycaItem" onChange="populateBuyTranData(this);"><option value="">Please Select</option></select></td>';
						multiItemsTableTr += '<td><input class="txnPerUnitPrice"  style="width:50px;" placeholder="Price Per Unit" type="text" name="bptycapriceperunits" id="bptycapriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=" calculateGross(this); calculateNetAmount(this);"/></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit"  style="width:50px;" type="text" name="bptycaunits" id="bptycaunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="checkStockAvailableForSellTran(this); calculateGross(this); calculateNetAmount(this);" placeholder="Units(if any)"/></td>';
						multiItemsTableTr += '<td><input class="txnGross" style="width:80px;" placeholder="Gross Amount" type="text" name="bptycagross" id="bptycagross" readonly="readonly"></td></td>';
						multiItemsTableTr += '<td><input type="button" class="btn btn-submit btn-idos input-small" name="inputtaxes" id="inputtaxes" value="Input Tax" style="margin-top:1px; width: 60px; padding: 5px 7px 5px 7px;" onclick="addBuyTaxComponentsPopup(this);"></td>';
						multiItemsTableTr += '<td><input class="txnTaxTypes" style="width:80px;" placeholder="Taxes" type="text" name="bptycaInTaxDetails" id="bptycaInTaxDetails" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:50px;"  class="txnTaxAmount" name="bptycataxamnt" id="bptycataxamnt" readonly="readonly" placeholder="Tax Amount"></td>';
						multiItemsTableTr += '<td><input class="withholdingtaxcomponenetdiv" style="width:50px;" placeholder="WithholdingTaxes" type="text" name="bptycawithheldingtaxamnt" id="bptycawithheldingtaxamnt" onkeyup="calculateNetAmountOnTdsChange(this);" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:52px;"  class="customerAdvance" placeholder="Advance Available" name="bptycavendoradvance" id="bptycavendoradvance" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:75px;"  class="howMuchAdvance" placeholder="Advance to Adjust?" name="bptycahowmuchfromadvance" id="bptycahowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="whenAdvanceIsEmpty(this); calculateNetAmountWhenAdvanceChangedForBuy(this);"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:80px;" class="netAmountVal" name="bptycanetamnt" id="bptycanetamnt" readonly="readonly" placeholder="Net Result"></td></tr>';
						$("#staticsellmultipleitemsbptyca table[id='multipleItemsTablebptyca'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablebptyca > tbody > tr[id='bptyca"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == SALES_RETURNS){
						var multiItemsTableTr = '<tr id="srtfcc'+length+'"><td><select class="txnItems"  name="srtfccItem" id="srtfccItem" readonly="readonly"><option value="">Please Select</option></select><input type="hidden" class="txnItemTableIdHid" id="srtfccTxnItemTableIdHid"/></td>';
						multiItemsTableTr += '<td><input class="txnPerUnitPrice"  style="width:50px;" placeholder="Price Per Unit" type="text" name="srtfccpriceperunits" id="srtfccpriceperunits" readonly="readonly" ></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit"  style="width:50px;" type="text" name="srtfccunits" id="srtfccunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateGross(this);" onblur="calculateNetAmountForSell(this);" placeholder="Units(if any)"><input type="hidden" class="originalNoOfUnitsHid" id="srtfccOriginalNoOfUnitsHid"/></td>';
						multiItemsTableTr += '<td><input type="text" style="width:60px;"  class="txnDiscountPercent" name="srtfccDiscountPercent" id="srtfccDiscountPercent" placeholder="Discount %" onkeypress="return onlyDotsAndNumbers(event);" readonly="readonly"><input type="hidden" class="txnDiscountPercentHid" id="srtfccdiscounttocust"/></td>';
						multiItemsTableTr += '<td><input type="text" style="width:60px;"  class="txnDiscountAmount" name="srtfccDiscountAmt" id="srtfccDiscountAmt" readonly="readonly" readonly="readonly" placeholder="Discount Amt"></td>';
						multiItemsTableTr += '<td><input class="txnGross" style="width:80px;" placeholder="Gross Amount" type="text" name="srtfccgross" id="srtfccgross" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:50px;"  class="txnTaxAmount" name="srtfcctaxamnt" id="srtfcctaxamnt" readonly="readonly" placeholder="Tax Amount"><input type="hidden" class="txnTaxTypes" name="srtfcctaxtypes" id="srtfcctaxtypes"/></td>';
						multiItemsTableTr += '<td><input type="text" style="width:52px;"  class="customerAdvance" placeholder="Advance Available" name="srtfcccustomeradvance" id="srtfcccustomeradvance" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:75px;"  class="howMuchAdvance" placeholder="Advance to Adjust?" name="srtfcchowmuchfromadvance" id="srtfcchowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmtForSalesRetWhenAdvChanged(this);"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:80px;" class="netAmountVal" name="srtfccnetamnt" id="srtfccnetamnt" readonly="readonly" placeholder="Net Result"></td></tr>';
						$("#staticsellmultipleitemssrtfcc table[id='multipleItemsTablesrtfcc'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablesrtfcc > tbody > tr[id='srtfcc"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == PURCHASE_RETURNS){
						$("#totalInputTaxes").val(0);
						var multiItemsTableTr = '<tr id="prtfcv'+length+'"><td><select class="txnItems"  name="prtfcvItem" id="prtfcvItem" readonly="readonly"><option value="">Please Select</option></select><input type="hidden" class="txnItemTableIdHid" id="prtfcvTxnItemTableIdHid"/></td>';
						multiItemsTableTr += '<td><input class="txnPerUnitPrice"  style="width:50px;" placeholder="Price Per Unit" type="text" name="prtfcvpriceperunits" id="prtfcvpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" readonly="readonly"/></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit"  style="width:50px;" type="text" name="prtfcvunits" id="prtfcvunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateGross(this);" onblur="calculateNetAmount(this);" placeholder="Units(if any)"/><input type="hidden" class="originalNoOfUnitsHid" id="prtfcvOriginalNoOfUnitsHid"/></td>';
						multiItemsTableTr += '<td><input class="txnGross" style="width:80px;" placeholder="Gross Amount" type="text" name="prtfcvgross" id="prtfcvgross" readonly="readonly"></td></td>';
						multiItemsTableTr += '<td><input type="button" class="btn btn-submit btn-idos input-small" name="inputtaxes" id="inputtaxes" value="Input Tax" style="margin-top:1px; width: 60px; padding: 5px 7px 5px 7px;" onclick="addBuyTaxComponentsPopup(this);"></td>';
						multiItemsTableTr += '<td><input class="txnTaxTypes" style="width:80px;" placeholder="Taxes" type="text" name="prtfcvInTaxDetails" id="prtfcvInTaxDetails" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:50px;"  class="txnTaxAmount" name="prtfcvtaxamnt" id="prtfcvtaxamnt" readonly="readonly" placeholder="Tax Amount"></td>';
						multiItemsTableTr += '<td><input class="withholdingtaxcomponenetdiv" style="width:50px;" placeholder="WithholdingTaxes" type="text" onkeyup="calculateNetAmountOnTdsChange(this);" name="prtfcvwithheldingtaxamnt" id="prtfcvwithheldingtaxamnt" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:52px;"  class="customerAdvance" placeholder="Advance Available" name="prtfcvvendoradvance" id="prtfcvvendoradvance" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:75px;"  class="howMuchAdvance" placeholder="Advance to Adjust?" name="prtfcvhowmuchfromadvance" id="prtfcvhowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="whenAdvanceIsEmpty(this); calculateNetAmountWhenAdvanceChangedForBuy(this);"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:80px;" class="netAmountVal" name="prtfcvnetamnt" id="prtfcvnetamnt" readonly="readonly" placeholder="Net Result"></td></tr>';
						$("#staticsellmultipleitemsprtfcv table[id='multipleItemsTableprtfcv'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTableprtfcv > tbody > tr[id='prtfcv"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == PREPARE_QUOTATION){
						$("#staticsellmultipleitemsquotation table[id='multipleItemsTableQuotation'] tbody").append('<tr id="quotat'+length+'"><td><select class="txnItems"  name="quotationItem" id="quotationItem" onChange="populateQuotationData(this);"><option value="">Please Select</option></select></td><td><input class="txnPerUnitPrice"  placeholder="Price Per Unit" type="text" name="quotationpriceperunits" id="quotationpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateGross(this); calcNetAmtQuotation(this);"></td><td><input class="txnNoOfUnit"  type="text" name="quotationunits" id="quotationunits" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="checkStockAvailableForSellTran(this); calculateGross(this); calcNetAmtQuotation(this);" placeholder="Units(if any)"></td><td><input class="txnGross" placeholder="Gross Amount" type="text" name="quotationgross" id="quotationgross" readonly="readonly"></td><td><input class="netAmountVal" placeholder="Net Amount" type="text" readonly="readonly"></td><td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>');

						$("#multipleItemsTableQuotation > tbody > tr[id='quotat"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == PROFORMA_INVOICE){
						$("#staticsellmultipleitemsproforma table[id='multipleItemsTableProforma'] tbody").append('<tr id="proforma'+length+'"><td><select class="txnItems"  name="proformaItem" id="proformaItem" onChange="populateSellTranData(this);"><option value="">Please Select</option></select></td><td><input class="txnPerUnitPrice"  placeholder="Price Per Unit" type="text" name="proformapriceperunits" id="proformapriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateGross(this); calculateNetAmountForSell(this);"></td><td><input class="txnNoOfUnit"  type="text" name="proformaunits" id="proformaunits" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="checkStockAvailableForSellTran(this); calculateGross(this); calculateNetAmountForSell(this);" placeholder="Units(if any)"></td><td><input class="txnGross" placeholder="Gross Amount" type="text" name="proformagross" id="proformagross" readonly="readonly"></td><td><input type="text" style="width:100px;" class="txnTaxTypes" name="proformataxtypes" id="proformataxtypes" readonly="readonly" placeholder="Tax Rate"></td><td><input type="text" style="width:50px;" class="txnTaxAmount" name="proformataxamnt" id="proformataxamnt" readonly="readonly" placeholder="Tax Amount"></td><td><input type="text" style="width:75px;" class="netAmountVal" name="proformanetamnt" id="proformanetamnt" readonly="readonly" placeholder="Net Result"></td></tr>');
						//$("#multipleItemsTableProforma > tbody > tr[id='proforma"+length+"'] > td > select[id='proformaItem']").children().remove();
						//$("#multipleItemsTableProforma > tbody > tr[id='proforma"+length+"'] > td > select[id='proformaItem']").append('<option value="">--Please Select--</option>');
						$("#multipleItemsTableProforma > tbody > tr[id='proforma"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == PURCHASE_ORDER){
						$("#staticsellmultipleitemspurord table[id='multipleItemsTablepurord'] tbody").append('<tr id="purord'+length+'"><td><select class="txnItems"  name="purordItem" id="purordItem" onChange="populateBuyTranData(this);"><option value="">Please Select</option></select></td><td><input class="txnPerUnitPrice"  placeholder="Price Per Unit" type="text" name="purordpriceperunits" id="purordpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateGross(this); calculateNetAmtForPurchaseOrder(this);"></td><td><input class="txnNoOfUnit"  type="text" name="purordunits" id="purordunits" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="calculateGross(this); calculateNetAmtForPurchaseOrder(this);" placeholder="Units(if any)"></td><td><input class="txnGross" placeholder="Gross Amount" type="text" name="purordgross" id="purordgross" readonly="readonly"></td><td><input type="text" class="netAmountVal" name="purordnetamnt" id="purordnetamnt" readonly="readonly" placeholder="Net Result"></td><td><div class="netAmountDescriptionDisplay"></div></td><td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>');
						$("#multipleItemsTablepurord > tbody > tr[id='purord"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == RECEIVE_ADVANCE_FROM_CUSTOMER){
                        var multiItemsTableTr = ('<tr id="rcafcc'+length+'"><td><select class="txnItems" name="rcafccItems" id="rcafccItems" onchange="validateGstItemsForCategory(this); getAdvanceTxnItemParent(this);"><option value="">--Please Select--</option></select></td><td><div class="itemParentNameDiv"></div></td><td><input type="text" class="customerAdvance" readonly="true"/></td><td><input class="advanceReceived" placeholder="Amount received" type="text" name="advanceReceived" id="rcafccAmountOfAdvanceReceived" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="getTaxComponentForReceive(this);" onblur="showResultantAdvanceWithWithheldingTax(this);"/></td><td><input type="text" class="withholdingtaxcomponenetCls" name="rcafccTaxAdjusted" placeholder="Tax Withheld" id="rcafccTaxAdjusted" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="getTaxComponentForReceive(this);" onblur="showResultantAdvanceWithWithheldingTax(this);"/><input class="txnGross" placeholder="Taxable Value" type="hidden" name="rcafccGross" id="rcafccGross"/></td><td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" name="rcafccTaxtypes" id="rcafccTaxtypes"/></td><td><input type="text" class="netAmountVal" readonly="true"/></td><td><input type="text" placeholder="Details" class="advancePurpose" name="rcafccadvancepurpose" id="rcafccadvancepurpose"/></td><td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>');

                        $("#staticmultipleitemsrcafcc table[id='multipleItemsTablercafcc'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablercafcc > tbody > tr[id='rcafcc"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
						var txnTypeOfSupply = $("#"+parentTr+" select[class='txnTypeOfSupply']").val();
						if(txnTypeOfSupply == '4' || txnTypeOfSupply == '5'){
							$("#staticmultipleitemspcafcv table[id='multipleItemsTablepcafcv'] tbody").append('<tr id="pcafcv'+length+'"><td><select class="txnItems" name="pcafcvItems" id="pcafcvItems" onchange="populateRecItemData(this); getAdvanceDiscount(this); getReserveChargesRCMData(this);"><option value="">--Please Select--</option></select></td><td><div class="itemParentNameDiv"></div></td><td><select class="txnRcmTaxItem" onChange=""><option value="">Please Select</option></select></td><td><div class="customerVendorExistingAdvance""></div></td><td><input class="customerAdvance" placeholder="Amount received" type="text" name="advanceReceived" id="pcafcvAmountOfAdvanceReceived" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="getTaxComponentForReceive(this);" onblur="showResultantAdvance(this); validateAdvancePayment(this);"/></td><td><input type="text" class="withholdingtaxcomponenetdiv" name="pcafcvTaxAdjusted" placeholder="Tax Withheld" id="pcafcvTaxAdjusted" onkeyup="checkIsTdsConfigured(this);calculateNetAmountOnTdsChange(this);" readonly="readonly"/><input class="txnGross" placeholder="Taxable Value" type="hidden" name="pcafcvGross" id="pcafcvGross"/></td><!-- <td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" name="pcafcvTaxtypes" id="pcafcvTaxtypes"/></td> --> <td> <input type="text" class="netPaid" name="pcafcvNetPaid" placeholder="Net Amount Paid" id="pcafcvNetPaid" readonly="readonly"/></td> <td><div class="resultantAdvance"></div></td><td><input type="text" placeholder="Details" class="advancePurpose" name="pcafcvadvancepurpose" id="pcafcvadvancepurpose" maxlength="256"/></td><td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>');
							$("#multipleItemsTablepcafcv > tbody > tr[id='pcafcv"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
						}
						else {
							$("#staticmultipleitemspcafcv table[id='multipleItemsTablepcafcv'] tbody").append('<tr id="pcafcv'+length+'"><td><select class="txnItems" name="pcafcvItems" id="pcafcvItems" onchange="validateSelectedItems(this); getAdvanceDiscount(this); getAdvanceTxnItemParent(this); getReserveChargesRCMData(this);"><option value="">--Please Select--</option></select></td><td><div class="itemParentNameDiv"></div></td><td><div class="customerVendorExistingAdvance""></div></td><td><input class="customerAdvance" placeholder="Amount received" type="text" name="advanceReceived" id="pcafcvAmountOfAdvanceReceived" onkeypress="return onlyDotsAndNumbers(event)" onblur="showResultantAdvance(this); validateAdvancePayment(this);"/></td><td><input type="text" class="withholdingtaxcomponenetdiv" name="pcafcvTaxAdjusted" placeholder="Tax Withheld" id="pcafcvTaxAdjusted" onkeyup="checkIsTdsConfigured(this);calculateNetAmountOnTdsChange(this);" readonly="readonly"/><input class="txnGross" placeholder="Taxable Value" type="hidden" name="pcafcvGross" id="pcafcvGross"/></td><!-- <td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" name="pcafcvTaxtypes" id="pcafcvTaxtypes"/></td> --> <td><input type="text" class="netPaid" name="pcafcvNetPaid" placeholder="Net Amount Paid" id="pcafcvNetPaid" readonly="readonly"/></td><td><div class="resultantAdvance"></div></td><td><input type="text" placeholder="Details" class="advancePurpose" name="pcafcvadvancepurpose" id="pcafcvadvancepurpose"/></td><td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>');
								$("#multipleItemsTablepcafcv > tbody > tr[id='pcafcv"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
							}
					}
					else if(transPurposeId == CREDIT_NOTE_CUSTOMER || transPurposeId == DEBIT_NOTE_CUSTOMER){
						var multiItemsTableTr = '<tr id="cdtdbt'+length+'"><td><select class="txnItems" id="cdtdbtItem" readonly="readonly"><option value="">Please Select</option></select></td>';

						multiItemsTableTr += '<td><input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text" id="cdtdbtPerUnitPrice"  onkeypress="return onlyDotsAndNumbers(event);" onblur="checkData4CreditDebitNote(this, \'txnPerUnitPriceHid\');" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);"/><input type="hidden" class="txnPerUnitPriceHid"/></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit" type="text" id="cdtdbtUnits" placeholder="Units(if any)" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" onblur="checkData4CreditDebitNote(this, \'txnNoOfUnitHid\'); checkStockAvailableForSellTran(this);" /><input type="hidden" class="txnNoOfUnitHid"/></td>';

						multiItemsTableTr += '<td><input type="text" class="txnDiscountPercent" id="cdtdbtDiscountPercent" placeholder="Discount %" onkeypress="return onlyDotsAndNumbers(event);" onblur="checkData4CreditDebitNote(this, \'txnDiscountPercentHid\'); " onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" readonly="readonly"/>&#37;<input type="hidden" class="txnDiscountPercentHid"/>';

						multiItemsTableTr += '<br><input type="text" class="txnDiscountAmount" id="cdtdbtDiscountAmt"  placeholder="Discount Amt" readonly="readonly"/></td>';

						multiItemsTableTr += '<td><input class="txnGross"  placeholder="Gross Amount" type="text" id="cdtdbtGross" readonly="readonly"></td>';

						multiItemsTableTr += '<td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" id="cdtdbtTaxtypes"/><input type="hidden" class="itemTaxAmount"/></td>';

						multiItemsTableTr += '<td><input type="text" class="invoiceValue" placeholder="Invoice Value" id="cdtdbtInvoiceValue" readonly="readonly"/></td>';

						/*multiItemsTableTr += '<td><input type="text" class="customerAdvance" placeholder="Advance Available" id="cdtdbtCustomeradvance" readonly="readonly"/></td>';
						multiItemsTableTr += '<td><input type="text" class="howMuchAdvance" placeholder="Advance to Adjust?" id="cdtdbtHowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onblur="checkData4CreditDebitNote(this, \'howMuchAdvanceHid\'); checkStockAvailableForSellTran(this);" onkeyup="adjustAdvance(this);" readonly="readonly"/><input type="hidden" class="howMuchAdvanceHid"/></td>';
						multiItemsTableTr += '<td><div id="advAdjTaxCell"><div id="advAdjTaxCell0"></div><div id="advAdjTaxCell1"></div><div id="advAdjTaxCell2"></div><div id="advAdjTaxCell3"></div><div id="advAdjTaxCell4"></div></div></td>';*/
						multiItemsTableTr += '<td><input type="text" class="netAmountVal" id="cdtdbtNetamnt" readonly="readonly" placeholder="Net Result"></td>';
						multiItemsTableTr += '<td><div class="netAmountDescriptionDisplay" id="netAmountLabel"></div></td><td><input type="checkbox" class="removeTxnCheckBox"/></td></tr>';
						$("#staticsellmultipleitemscdtdbt table[class='multipleItemsTable'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablecdtdbt > tbody > tr[id='cdtdbt"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
                        enableDisableNoteTxnField(transPurposeId);
					}else if(transPurposeId == CREDIT_NOTE_VENDOR || transPurposeId == DEBIT_NOTE_VENDOR){
						$("#totalInputTaxes").val(0);
						var length=$("#staticsellmultipleitemscdtdbv tbody tr").length;
						var multiItemsTableTr = '<tr id="cdtdbv'+length+'"><td><select class="txnItems"  name="cdtdbvItem" id="cdtdbvItem" onChange="populateBuyTranData(this);"><option value="">Please Select</option></select></td>';
						//multiItemsTableTr += '<td><div class="klBranchSpecfTd" style="height: 50px; overflow: auto;"></div></td>';
						multiItemsTableTr += '<td style="display:none;"><select class="txnRcmTaxItem" onChange=""><option value="">Please Select</option></select></td>';
						multiItemsTableTr += '<td><input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text" name="cdtdbvpriceperunits" id="cdtdbvpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onblur="checkData4CreditDebitNote(this, \'txnPerUnitPriceHid\');" onkeyup="calculateGross(this); calculateNetAmount(this);"/><input type="hidden" class="txnPerUnitPriceHid"/></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit" type="text" name="cdtdbvunits" id="cdtdbvunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateGross(this); calculateNetAmount(this);" onblur="checkData4CreditDebitNote(this, \'txnNoOfUnitHid\'); checkStockAvailableForSellTran(this);" placeholder="Units(if any)"/><input type="hidden" class="txnNoOfUnitHid"/></td>';
						multiItemsTableTr += '<td><input class="txnGross" placeholder="Gross Amount" type="text" name="cdtdbvgross" id="cdtdbvgross" readonly="readonly"></td>';
						multiItemsTableTr += '<td><select class="txnGstTaxRate" id="cdtdbvTaxRate" onChange="calculateNetAmount(this);"><option value="">Select</option></select></td>';
						multiItemsTableTr += '<td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div></div><input class="txnTaxTypes" placeholder="Taxes" type="hidden" name="cdtdbvInTaxDetails" id="cdtdbvInTaxDetails" readonly="readonly"><input type="hidden" class="inputTaxNames" id="cdtdbvinputTaxNames" name="cdtdbvinputTaxNames" value="0"/><input type="hidden" class="inputTaxValues" id="cdtdbvinputTaxValues" name="cdtdbvinputTaxValues" value="0"/></td>';
						multiItemsTableTr += '<td><select class="txnCessRate" id="cdtdbvCessRate"  onChange="calculateNetAmount(this);"><option value=""> Select</option></select></td>';
						multiItemsTableTr += '<td><input type="text" class="txnCessTaxAmt" id="cdtdbvCessAmt" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);"/><input type="hidden" class="itemTaxAmount" name="cdtdbvTotaltaxamnt" id="cdtdbvTotaltaxamnt" readonly="readonly" placeholder="Tax Amount"/></td>';
						multiItemsTableTr += '<td><input type="text" class="withholdingtaxcomponenetdiv" name="cdtdbvwithheldingtaxamnt" id="cdtdbvwithheldingtaxamnt" onkeyup="calculateNetAmountOnTdsChange(this);" readonly="readonly" placeholder="WithholdingTax"></td>';
						/*multiItemsTableTr += '<td><input type="text" class="customerAdvance" placeholder="Advance Available" name="cdtdbvvendoradvance" id="cdtdbvvendoradvance" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" class="howMuchAdvance" placeholder="Advance to Adjust?" name="cdtdbvhowmuchfromadvance" id="cdtdbvhowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenAdvanceChangedForBuy(this);" readonly="readonly"/></td>';*/
						multiItemsTableTr += '<td><input type="text" class="netAmountVal" name="cdtdbvnetamnt" id="cdtdbvnetamnt" readonly="readonly" placeholder="Net Amount"></td><td><div class="netAmountDescriptionDisplay" id="cdtdbvnetAmountLabel"></div></td>';
						multiItemsTableTr +='<td><input type="checkbox" class="removeTxnCheckBox"/></td></tr>';
						$("#staticsellmultipleitemscdtdbv table[id='multipleItemsTablecdtdbv'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablecdtdbv > tbody > tr[id='cdtdbv"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
						//fetchBranchInTaxesCess($("#"+parentTr+" select[class='txnBranches']"));
						$("#cdtdbv"+length+" select[class='txnGstTaxRate']").append(BRANCH_INGSTTAXES_LIST_GLOBAL);
						$("#cdtdbv"+length+" select[class='txnCessRate']").append(BRANCH_INCESSTAXES_LIST_GLOBAL);
					}else if(transPurposeId==REFUND_ADVANCE_RECEIVED){
						var multiItemsTableTr = '<tr id="mkrfnd'+length+'">';
						multiItemsTableTr += '<td><select class="txnItems" name="mkrfndItems" id="mkrfndItems" onchange="populateRecItemData(this);"><option value="">--Please Select--</option></select></td>';
                        multiItemsTableTr += '<td><input type="text" class="advAvailForRefund" name="mkrfndadvAvailForRefund" id="mkrfndadvAvailForRefund" readonly/></td>';
                        multiItemsTableTr += ' <td><input type="text" class="tdsAvailForRefund" name="mkrfndtdsAvailForRefund" id="mkrfndtdsAvailForRefund" readonly/></td>';
                        multiItemsTableTr += ' <td><input placeholder="Amount received" class="advanceReceived" type="text" name="advanceReceived" id="mkrfndAmountOfAdvanceReceived" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="calculateResultantRefundAdv(this);getTaxComponentForMakeRefund(this);" /></td>';
                        multiItemsTableTr += ' <td><input type="text" class="taxAdjusted" name="mkrfndTaxAdjusted" placeholder="Tax Withheld" id="mkrfndTaxAdjusted" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="calculateResultantRefundTDS(this);getTaxComponentForMakeRefund(this);" /></td>';
                        multiItemsTableTr += ' <td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div>';
                        multiItemsTableTr += ' <input type="hidden" class="txnTaxTypes" name="mkrfndTaxtypes" id="mkrfndTaxtypes"/></td>';
                        multiItemsTableTr += ' <td><input type="text" class="mkrfndadvResultantAdv" name="mkrfndadvResultantAdv" id="mkrfndadvResultantAdv" readonly/></td>';
                        multiItemsTableTr += ' <td><input type="text" class="mkrfndadvResultantTax"  name="mkrfndadvResultantTax" id="mkrfndadvResultantTax" readonly/></td>';
						multiItemsTableTr += '<td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>';
						$("#staticmultipleitemsmkrfnd table[id='multipleItemsTablemkrfnd'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablemkrfnd > tbody > tr[id='mkrfnd"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
						//fetchBranchInTaxesCess($("#"+parentTr+" select[class='txnBranches']"));
					} else if(transPurposeId==REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
						var multiItemsTableTr = '<tr id="rfndam'+length+'">';
						multiItemsTableTr += '<td><select class="salesExpenseTxns" name="rfndamntrcvdItems" id="rfndamntrcvdItems" onchange="getOutstandings(this);"><option value="">--Please Select--</option></select></td>';
                        multiItemsTableTr += '<td><input type="text" class="amtAvailForRefund" name="amntAvailForRefund" id="amntAvailForRefund" readonly/></td>';
                        multiItemsTableTr += '<td><input type="text" class="tdsAvailForRefund" name="tdsAvailForRefund" id="tdsAvailForRefund" readonly/></td>';
                        multiItemsTableTr += ' <td><input placeholder="Amount received" class="refundAmountReceived" type="text" name="refundAmountReceived" id="rfndAmountOfReceived"   onkeyup="calculateResultantRefundAmount(this);"/> </td>';
                        multiItemsTableTr += ' <td><input type="text" class="taxAdjusted" name="rfndTaxAdjusted" placeholder="Tax Withheld" id="rfndTaxAdjusted" onkeyup="calculateResultantRefundTDSForRefund(this);"/></td>';
                       // multiItemsTableTr += ' <td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div>';
                       // multiItemsTableTr += ' <input type="hidden" class="txnTaxTypes" name="mkrfndTaxtypes" id="mkrfndTaxtypes"/></td>';
                        multiItemsTableTr += ' <td><input type="text" class="rfndamntResultantAmnt" name="rfndamntResultantAmnt" id="rfndamntResultantAmnt" readonly/></td>';
                        multiItemsTableTr += ' <td><input type="text" class="rfndamntResultantTax" name="rfndamntResultantTax" id="rfndamntResultantTax" readonly/></td>';
						multiItemsTableTr += '<td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>';
						$("#staticmultipleitemsrfndamntrcvd table[id='multipleItemsTablerfndamntrcvd'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablerfndamntrcvd > tbody > tr[id='rfndam"+length+"'] > td > .salesExpenseTxns").append(transactionListTemp);
						//fetchBranchInTaxesCess($("#"+parentTr+" select[class='txnBranches']"));
					}else if(transPurposeId == CANCEL_INVOICE){
						addItemsInCancelInvoiceTxn(customerVendorItemsListTemp, transPurposeId);
					}else if(transPurposeId == CREATE_PURCHASE_ORDER){
						addItemsPurchaseOrderTxn(mainTableID, parentTr, customerVendorItemsListTemp);
					}
					initMultiItemsSelect2();
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
		    		swal("Error on fetching items!", "Please retry, if problem persists contact support team", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
				}

			});
		}else if((transPurposeId === SELL_ON_CASH_COLLECT_PAYMENT_NOW || transPurposeId === RECEIVE_ADVANCE_FROM_CUSTOMER) && txnForUnavailableCustomer != ""){
			var jsonData = {};
			jsonData.useremail=$("#hiddenuseremail").text();
			jsonData.txnPurposeId=transPurposeId;
			jsonData.txnPurposeText=txnPurposeText;
			jsonData.custVendEntityId=vendorcustomer;
			var url="/transaction/getAllIncomeItems";
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
		      		var customerVendorItemsListTemp = "";
					for(var i=0;i<data.txnItemsCustomerVendorsData.length;i++){
			    		customerVendorItemsListTemp += ('<option value="'+data.txnItemsCustomerVendorsData[i].id+'" category="'+data.txnItemsCustomerVendorsData[i].category+'" id="'+data.txnItemsCustomerVendorsData[i].iseditable+'" combsales="'+data.txnItemsCustomerVendorsData[i].isCombinationSales+'">'+data.txnItemsCustomerVendorsData[i].name+'</option>');
			    	}
                    var currentTr = $("#" + mainTableID +" table[class='multipleItemsTable'] tbody tr:last").attr('id');
                    var length = currentTr.substring(6, currentTr.length);
                    length = parseInt(length) + 1;
		    	  	if(transPurposeId === SELL_ON_CASH_COLLECT_PAYMENT_NOW){
						var multiItemsTableTr = '<tr id="soccpn'+length+'"><td><select class="txnItems"  name="soccpnItem" id="soccpnItem" onChange="populateSellTranData(this);"><option value="">Please Select</option></select></td>';
						multiItemsTableTr += '<td><div class="klBranchSpecfTd"></div></td>';
						//multiItemsTableTr += '<td><input class="txnPerUnitPrice"  style="width:50px;" placeholder="Price Per Unit" type="text" name="soccpnpriceperunits" id="soccpnpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="validateKnowledgeLib(this); calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);"></td>';
						multiItemsTableTr += '<td><input id="txnPerUnitPriceTaxInclusice" style="display:none;width: 60px !important;" placeholder="Price Per Unit" class="txnPerUnitPriceTaxInclusice" type="text" onkeypress="return onlyDotsAndNumbers(event)" onKeyup="calculateInclusiveTaxAndSubmit(this)" />';
						multiItemsTableTr += '<input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text" name="soccpnpriceperunits" id="soccpnpriceperunits" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="validateKnowledgeLib(this); calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);"></td>';
						multiItemsTableTr += '<td><input class="txnNoOfUnit" style="width:50px;" type="text" name="soccpnunits" id="soccpnunits" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="validateKnowledgeLib(this); calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" onblur="checkStockAvailableForSellTran(this);"  placeholder="Units(if any)"></td>';
						multiItemsTableTr +='<td><input type="text" style="width:60px;"  class="txnDiscountPercent" name="soccpnDiscountPercent" id="soccpnDiscountPercent" placeholder="Discount %" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);"><input type="hidden" class="txnDiscountPercentHid" id="soccpndiscounttocust"/></td>';
						multiItemsTableTr += '<td><input type="text" style="width:60px;"  class="txnDiscountAmount" name="soccpnDiscountAmt" id="soccpnDiscountAmt" readonly="readonly" placeholder="Discount Amt"></td>';
						multiItemsTableTr += '<td><input class="txnGross"  style="width:80px;" placeholder="Gross Amount" type="text" name="soccpngross" id="soccpngross" readonly="readonly"></td>';
						multiItemsTableTr += '<td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" name="soccpntaxtypes" id="soccpntaxtypes"/></td>';
						multiItemsTableTr += '<td><input type="text" style="width:80px;" class="invoiceValue" placeholder="Invoice Value" name="soccpnInvoiceValue" id="soccpnInvoiceValue" readonly="readonly"/></td>';
						multiItemsTableTr += '<td><input type="text" style="width:63px;"  class="customerAdvance" placeholder="Advance Available" name="soccpncustomeradvance" id="soccpncustomeradvance" readonly="readonly"></td>';
						multiItemsTableTr += '<td><input type="text" style="width:63px;" class="howMuchAdvance" placeholder="Advance to Adjust?" name="soccpnhowmuchfromadvance" id="soccpnhowmuchfromadvance" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="adjustAdvance(this);"/></td>';
						//multiItemsTableTr += '<td><input type="text" style="width:80px;" class="taxableVal" name="soccpnTaxableVal" id="soccpnTaxableVal" readonly="readonly" placeholder="Taxable Value"></td>'
						multiItemsTableTr += '<td><div id="advAdjTaxCell"><div id="advAdjTaxCell0"></div><div id="advAdjTaxCell1"></div><div id="advAdjTaxCell2"></div><div id="advAdjTaxCell3"></div><div id="advAdjTaxCell4"></div></div></td>';
						multiItemsTableTr += '<td><input type="text" style="width:80px;" class="netAmountVal" name="soccpnnetamnt" id="soccpnnetamnt" readonly="readonly" placeholder="Net Result"></td>';
						multiItemsTableTr += '<td><div class="netAmountDescriptionDisplay" id="netAmountLabel"></div></td><td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';
						$("#staticsellmultipleitemssoccpn table[id='multipleItemsTablesoccpn'] tbody").append(multiItemsTableTr);
						$("#multipleItemsTablesoccpn > tbody > tr[id='soccpn"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}else if(transPurposeId === RECEIVE_ADVANCE_FROM_CUSTOMER){
						$("#staticmultipleitemsrcafcc table[id='multipleItemsTablercafcc'] tbody").append('<tr id="rcafcc'+length+'"><td><select class="txnItems" name="rcafccItems" id="rcafccItems" onchange="validateGstItemsForCategory(this); getAdvanceTxnItemParent(this);"><option value="">--Please Select--</option></select></td><td><div class="itemParentNameDiv"></div></td><td><input type="text" class="customerAdvance" readonly="true"/></td><td><input class="advanceReceived" placeholder="Amount received" type="text" name="advanceReceived" id="rcafccAmountOfAdvanceReceived" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="getTaxComponentForReceive(this);" onblur="showResultantAdvanceWithWithheldingTax(this);"/></td><td><input type="text" class="withholdingtaxcomponenetCls" name="rcafccTaxAdjusted" placeholder="Tax Withheld" id="rcafccTaxAdjusted" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="getTaxComponentForReceive(this);" onblur="showResultantAdvanceWithWithheldingTax(this);"/><input class="txnGross" placeholder="Taxable Value" type="hidden" name="rcafccGross" id="rcafccGross"/></td><td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" name="rcafccTaxtypes" id="rcafccTaxtypes"/></td><td><input type="text" class="netAmountVal" readonly="true"/></td><td><input type="text" placeholder="Details" class="advancePurpose" name="rcafccadvancepurpose" id="rcafccadvancepurpose"/></td><td><input class="removeTxnCheckBox" type="checkbox"/></td></tr>');
						$("#multipleItemsTablercafcc > tbody > tr[id='rcafcc"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
					}
					initMultiItemsSelect2();
			   	},
			    error: function (xhr, status, error) {
			      	if(xhr.status == 401){ doLogout();
				      }else if(xhr.status == 500){
			    		swal("Error on fetching items!", "Please retry, if problem persists contact support team", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
				}
			   });
		}else if((transPurposeId==BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId==BUY_ON_CREDIT_PAY_LATER || transPurposeId==BUY_ON_PETTY_CASH_ACCOUNT) &&  txnForUnavailableCustomer!=""){
            var currentTr = $("#" + mainTableID +" table[class='multipleItemsTable'] tbody tr:last").attr('id');
            var lengthTr = currentTr.substring(6, currentTr.length);
            lengthTr = parseInt(lengthTr) + 1;

	 		visitingVendorAddMultiItem(transPurposeId, lengthTr);
	 		showWalkinVendorDetail($("#"+parentTr+" input[name='unAvailableVendor']"));
	 		fetchBranchInTaxesCess($("#"+parentTr+" select[class='txnBranches']"));
		}else if(transPurposeId == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
            addMoreTranferInventoryItems(mainTableID, parentTr);
        }else if(transPurposeId == BILL_OF_MATERIAL){
			addItemsInBillOdMaterialTxn(mainTableID, parentTr);
        }else if(transPurposeId == CREATE_PURCHASE_REQUISITION){
			addItemsPurchaseRequisitionTxn(mainTableID, parentTr);
		}
		$.unblockUI();
	});
});

var validateMultiItemsTransaction = function(tableName){
	var isvalid = true;
	$("#" + tableName + " > tbody > tr").each(function() {
		var itemId = $(this).find("td .txnItems option:selected").val();
		var grossAmt = $(this).find("td input[class='txnGross']").val();
		if(itemId != "" && typeof itemId!='undefined' && grossAmt != "" && typeof grossAmt!='undefined' ){
			var txnPerUnitPrice = $(this).find("td input[class='txnPerUnitPrice']").val();
			var txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
			var txnGross = $(this).find("td input[class='txnGross']").val();
			var netAmountVal = $(this).find("td input[class='netAmountVal']").val();
			if(txnPerUnitPrice == "" || parseFloat(txnPerUnitPrice) == 0.0 || txnNoOfUnit=="" || parseFloat(txnNoOfUnit) == 0.0 || txnGross=="" || parseFloat(txnGross) == 0.0 || netAmountVal==""){
				isvalid = false;
				return isvalid;
			}
		}else if( itemId == "" || grossAmt == ""){
            isvalid = false;
            return isvalid;
		}
	});
	return isvalid;
}

function convertTableDataToArray(tableName){
	var parentTable = $("#"+tableName).parents().closest('table').attr('id');
    var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
	var multipleItemsData = [];

	if(whatYouWantToDoVal==RECEIVE_PAYMENT_FROM_CUSTOMER){
		$("#" + tableName + " > tbody > tr").each(function() {
			var jsonData = {};
			var pendingTxn = "",amountReceived="",taxWH="",discAllowed="",dueBal="";
			pendingTxn = $(this).find("td .pendingTxns option:selected").val();
			amountReceived = $(this).find("td #rcpfccpaymentreceived").val();
			taxWH = $(this).find("td #rcpfccTaxAdjusted").val();
			discAllowed= $(this).find("td #rcpfccdiscountAllowed").val();
			dueBal= $(this).find("td #rcpfccduebalance").val();

			jsonData.pendingTxn = pendingTxn;
			jsonData.amountReceived = amountReceived;
			jsonData.taxWH = taxWH;
			jsonData.discAllowed = discAllowed;
			jsonData.dueBal = dueBal;
			multipleItemsData.push(JSON.stringify(jsonData));
		});
	}

	$("#" + tableName + " > tbody > tr").each(function() {
		var jsonData = {};
		var grossAmt = ""; var itemId = "";
		if(whatYouWantToDoVal==REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
			itemId = $(this).find("td .salesExpenseTxns option:selected").val();
		}else{
			itemId = $(this).find("td .txnItems option:selected").val();
		}
		var grossAmt = "";
		if(whatYouWantToDoVal == REFUND_ADVANCE_RECEIVED){
			grossAmt = $(this).find("td input[class='advanceReceived']").val();
		}
		else if(whatYouWantToDoVal==REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
			grossAmt = $(this).find("td input[class='refundAmountReceived']").val();
		}else {
			grossAmt = $(this).find("td input[class='txnGross']").val();
		}
		if(itemId != "" && typeof itemId!='undefined' && grossAmt != "" && typeof grossAmt!='undefined' ){
			jsonData.txnItemID = $(this).attr('name');
			jsonData.txnItems = itemId;
            var txnPerUnitPrice = $(this).find("td input[class='txnPerUnitPrice']").val();
            var txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
			if(whatYouWantToDoVal == CREDIT_NOTE_CUSTOMER || whatYouWantToDoVal == DEBIT_NOTE_VENDOR){
                var txnPerUnitPriceOrg = $(this).find("td input[class='txnPerUnitPriceHid']").val();
                if(parseFloat(txnPerUnitPriceOrg) > parseFloat(txnPerUnitPrice)) {
                    txnPerUnitPrice = parseFloat(txnPerUnitPriceOrg) - parseFloat(txnPerUnitPrice);
                }
                var txnNoOfUnitOrg = $(this).find("td input[class='txnNoOfUnitHid']").val();
                if(parseFloat(txnNoOfUnitOrg) > parseFloat(txnNoOfUnit)) {
                    txnNoOfUnit = parseFloat(txnNoOfUnitOrg) - parseFloat(txnNoOfUnit);
                }
            }else if(whatYouWantToDoVal == CREDIT_NOTE_VENDOR || whatYouWantToDoVal == DEBIT_NOTE_CUSTOMER) {
                var txnPerUnitPriceOrg = $(this).find("td input[class='txnPerUnitPriceHid']").val();
                if(parseFloat(txnPerUnitPriceOrg) < parseFloat(txnPerUnitPrice)) {
                    txnPerUnitPrice = parseFloat(txnPerUnitPrice) - parseFloat(txnPerUnitPriceOrg);
                }
                var txnNoOfUnitOrg = $(this).find("td input[class='txnNoOfUnitHid']").val();
                if(parseFloat(txnNoOfUnitOrg) < parseFloat(txnNoOfUnit)) {
                    txnNoOfUnit = parseFloat(txnNoOfUnit) - parseFloat(txnNoOfUnitOrg);
                }
            }
            if(whatYouWantToDoVal == REFUND_ADVANCE_RECEIVED){
			  	jsonData.advAvailForRefund = $(this).find("td input[class='advAvailForRefund']").val();
			  	jsonData.tdsAvailForRefund = $(this).find("td input[class='tdsAvailForRefund']").val();
			  	jsonData.advanceReceived = $(this).find("td input[class='advanceReceived']").val();
			  	jsonData.taxAdjusted = $(this).find("td input[name='mkrfndTaxAdjusted']").val();
			  	jsonData.ResultantAdv = $(this).find("td input[name='mkrfndadvResultantAdv']").val();
			  	jsonData.ResultantTax = $(this).find("td input[name='mkrfndadvResultantTax']").val();
			 }
            if(whatYouWantToDoVal == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
			  	jsonData.amtAvailForRefund = $(this).find("td input[class='amtAvailForRefund']").val();
			  	jsonData.tdsAvailForRefund = $(this).find("td input[class='tdsAvailForRefund']").val();
			  	jsonData.amountToRefund = $(this).find("td input[class='refundAmountReceived']").val();
			  	jsonData.taxToRefund = $(this).find("td input[name='rfndTaxAdjusted']").val();
			  	jsonData.resultantAmt = $(this).find("td input[name='rfndamntResultantAmnt']").val();
			  	jsonData.resultantTax = $(this).find("td input[name='rfndamntResultantTax']").val();

			 }
            var txnRcmTaxItemID = "";
            var txnDutiesAndTaxesAmount = "";
            if(whatYouWantToDoVal == BUY_ON_CASH_PAY_RIGHT_AWAY || whatYouWantToDoVal == BUY_ON_CREDIT_PAY_LATER){
            	txnRcmTaxItemID = $(this).find("td .txnRcmTaxItem option:selected").val();
				txnDutiesAndTaxesAmount = $(this).find("td .txnDutiesAndTaxes").val();
            }
            jsonData.txnPerUnitPrice = txnPerUnitPrice;
            jsonData.txnNoOfUnit = txnNoOfUnit;
			jsonData.txnGross = grossAmt;
			jsonData.txnTaxDesc = $(this).find("td input[class='txnTaxTypes']").val();
			var txnTaxAmount = $(this).find("td input[class='txnTaxAmount']").map(function() {
				return this.value;
			}).get().toString();
			jsonData.txnTaxAmount = txnTaxAmount;
			var txnTaxRate = $(this).find("td input[class='taxRate']").map(function() {
				return this.value;
			}).get().toString();
			jsonData.txnTaxRate = txnTaxRate;
			var txnTaxName = $(this).find("td input[class='txnTaxName']").map(function() {
				return this.value;
			}).get().toString();
			jsonData.txnTaxName = txnTaxName;
			var txnTaxID = $(this).find("td input[class='txnTaxID']").map(function() {
				return this.value;
			}).get().toString();
			var txnTaxFormulaId = $(this).find("td input[class='txnTaxID']").map(function() {
				return $(this).attr('formula');
			}).get().toString();
			// RCM
			var txnRcmTaxItemID = $(this).find("td .txnRcmTaxItem option:selected").val();
			var txnDutiesAndTaxesAmount = $(this).find("td input[class='txnDutiesAndTaxes']").map(function() {
				return this.value;
			}).get().toString();
			jsonData.txnTaxID = txnTaxID;
			jsonData.txnTaxFormulaId = txnTaxFormulaId;
			jsonData.txnRcmTaxItemID = txnRcmTaxItemID;
			jsonData.txnDutiesAndTaxesAmount = txnDutiesAndTaxesAmount;
			jsonData.txnInvoiceValue = $(this).find("td input[class='invoiceValue']").val();
			if(whatYouWantToDoVal == RECEIVE_ADVANCE_FROM_CUSTOMER){
                jsonData.withholdingAmount = $(this).find("td input[class='withholdingtaxcomponenetCls']").val();
                jsonData.customerAdvance = $(this).find("td input[class='advanceReceived']").val();
            }else{
                jsonData.withholdingAmount = $(this).find("td input[class='withholdingtaxcomponenetdiv']").val();
                jsonData.customerAdvance = $(this).find("td input[class='customerAdvance']").val();
            }
			jsonData.txnLeftOutWithholdTransIDs = $(this).find("td input[class='txnLeftOutWithholdTransIDs']").val();
			jsonData.howMuchAdvance = $(this).find("td input[class='howMuchAdvance']").val();
			var netAmount = $(this).find("td input[class='netAmountVal']").val();
			if(typeof netAmount == 'undefined'){
				netAmount = $(this).find("td div[class='resultantAdvance']").text();
			}
			jsonData.netAmountVal = netAmount;
			jsonData.txnAdvancePurpose = $(this).find("td input[class='advancePurpose']").val();
			jsonData.txnDiscountPercent = $(this).find("td input[class='txnDiscountPercent']").val();
			jsonData.txnDiscountAmt = $(this).find("td input[class='txnDiscountAmount']").val();
			var txnTaxOnAdvAdj = $(this).find("td input[class='txnTaxOnAdvAdjCls']").map(function() {
				return this.value;
			}).get().toString();
			var txnTaxNameOnAdvAdj = $(this).find("td input[class='txnTaxNameOnAdvAdjCls']").map(function() {
				return this.value;
			}).get().toString();
			jsonData.txnTaxOnAdvAdj = txnTaxOnAdvAdj;
			jsonData.txnTaxNameOnAdvAdj = txnTaxNameOnAdvAdj;
			jsonData.actualbudgetDisplayVal = $(this).find("td input[class='actualbudgetDisplayVal']").val();
			jsonData.budgetDisplayVal = $(this).find("td input[class='budgetDisplayVal']").val();
			var amountRangeFromLimit = $(this).find("td input[class='amountRangeFromLimit']").val();
			var amountRangeToLimit = $(this).find("td input[class='amountRangeToLimit']").val();
			if(typeof amountRangeFromLimit != 'undefined' && typeof amountRangeToLimit != 'undefined'){
				jsonData.amountRangeLimitRuleVal = amountRangeFromLimit +","+amountRangeToLimit;
			}else{
				jsonData.amountRangeLimitRuleVal ="";
			}
			jsonData.txnItemTableIdHid = $(this).find("td input[class='txnItemTableIdHid']").val();
			var followedkl = $(this).find("td div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
			var klmandatoryfollowednotfollowed = "0";
			if(typeof followedkl!='undefined'){
				if($(this).find("td div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
					klmandatoryfollowednotfollowed="1";
				}
				if($(this).find("td div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
					klmandatoryfollowednotfollowed="0";
				}
			}
			jsonData.klmandatoryfollowednotfollowed = klmandatoryfollowednotfollowed;
			multipleItemsData.push(JSON.stringify(jsonData));
		}
	});
	return multipleItemsData;
}

function calculateTotalNetForMultipleItemsTable(elem, parentOfparentTr){
	let netAmtTotal=0.0;
	$("#" + parentOfparentTr + " .multipleItemsTable > tbody > tr").each(function() {
		let netAmt = $(this).find("td input[class='netAmountVal']").val();
		if(netAmt!="" && typeof netAmt!='undefined'){
			netAmtTotal = netAmtTotal + parseFloat(netAmt);
		}
	});
	return netAmtTotal;
}

var getTransactionItemsList = function(tableName){
	var parentTable = $("#"+tableName).parents().closest('table').attr('id');
	var itemList="";
	$("#" + tableName + " > tbody > tr").each(function() {
		var itemId = $(this).find("td .txnItems option:selected").val();
		if(itemId != "" && typeof itemId!='undefined'){
			itemList += itemId +",";
		}
	});
	return itemList;
}

function calcuateTotalAmt4Element(parentTr, elemClass){
	var totalAmount=0;
	$("#"+parentTr+" .multipleItemsTable > tbody > tr").each(function() {
		var netAmt = $(this).find("td input[class='"+elemClass+"']").val();
		if(netAmt!="" && typeof netAmt!='undefined'){
			totalAmount = totalAmount + parseFloat(netAmt);
		}
	});
	return totalAmount;
}
function isKnowledgeLibFollowedInMultiItems(parentid){
	var klmandatoryfollowednotfollowed = "1";
	$("#" + parentid +" .multipleItemsTable > tbody > tr").each(function() {
		var followedkl = $(this).find("td div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			if($(this).find("td div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
				klmandatoryfollowednotfollowed="0";
			}
		}
	});
	return klmandatoryfollowednotfollowed;
}

function listMultiSellItems(elem){
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
				$("#staticMutipleTransactionItems").attr('data-toggle', 'modal');
		    	$("#staticMutipleTransactionItems").modal('show');
		    	$(".staticMutipleTransactionItemsclose").attr("href",location.hash);
		    	$("#staticMutipleTransactionItems div[class='modal-body']").html("");
		    	if(data.transactionItemdetailsData.length>0){
		    	    if(txnReferenceNo.startsWith("BOM")){
                        popupBomTransactionItems(txnReferenceNo, data);
                    } else if(txnReferenceNo.startsWith("PR")){
											$("#staticMutipleTransactionItems .panel-title").text("Transaction Items for " + txnReferenceNo);
    $("#staticMutipleTransactionItems div[class='modal-body']").html('<div class="datascrolltable" style="height: 100%; overflow: auto;"><table id="multipleSellItemsBreakupTable" class="table table-hover table-striped excelFormTable transaction-create table-bordered" style="margin-top: 0px; width:100%;">' +
        '<thead class="tablehead1"><tr><th>ITEM</th><th>UNIT PRICE</th><th>NO. OF UNITS</th><th>VENDOR</th><th>MEASURE NAME</th><th>OEM</th><th>TOTAL PRICE</th><th>AVAILABLE UNITS</th><th>COMMITTED UNITS</th><th>ORDERED UNITS</th><th>NET UNITS</th><th>FULFILLED UNITS</th><th>TYPE OF MATERIAL</th><th>DESTINATION GSTIN</th><th>IS FULFILLED</th><tr></thead><tbody></tbody></table></div>');
    var tableTr = "";
    for (var i = 0; i < data.transactionItemdetailsData.length; i++) {
        tableTr += '<tr id=' + i + '><td>' + data.transactionItemdetailsData[i].itemName + '</td><td>' + data.transactionItemdetailsData[i].pricePerUnit + '</td><td>' + data.transactionItemdetailsData[i].noOfUnits + '</td><td>' + data.transactionItemdetailsData[i].vendor + '</td><td>' + data.transactionItemdetailsData[i].measureName + '</td><td>' + data.transactionItemdetailsData[i].oem + '</td><td>' + data.transactionItemdetailsData[i].totalPrice + '</td><td>' + data.transactionItemdetailsData[i].availableUnits + '</td><td>' + data.transactionItemdetailsData[i].committedUnits + '</td><td>' + data.transactionItemdetailsData[i].orderedUnits + '</td><td>' + data.transactionItemdetailsData[i].netUnits + '</td><td>' + data.transactionItemdetailsData[i].fulfilledUnits + '</td><td>' + data.transactionItemdetailsData[i].typeOfMaterial + '</td><td>' + data.transactionItemdetailsData[i].destinationGstin + '</td><td>' + data.transactionItemdetailsData[i].isFulfilled + '</td></tr>';
    }
    $("#staticMutipleTransactionItems div[class='modal-body'] table[id='multipleSellItemsBreakupTable'] tbody").append(tableTr);

										}else {
                        $("#staticMutipleTransactionItems .panel-title").text("Transaction Items for " + txnReferenceNo);
                        $("#staticMutipleTransactionItems div[class='modal-body']").html('<div class="datascrolltable" style="height: 100%; overflow: auto;"><table id="multipleSellItemsBreakupTable" class="table table-hover table-striped excelFormTable transaction-create" style="margin-top: 0px; width:100%;">' +
                            '<thead class="tablehead1"><tr><th>ITEM</th><th>UNIT PRICE</th><th>NO. OF UNITS</th><th>DISCOUNT %</th><th>DISCOUNT AMOUNT</th><th>GROSS AMOUNT</th><th>TAX DESCRIPTION</th><th>TAX AMOUNT</th><th>WITHHOLDING TAX</th><th>ADVANCE</th><th>ADJUSTMENT</th><th>NET AMOUNT</th><tr></thead><tbody></tbody></table></div>');
                        for (var i = 0; i < data.transactionItemdetailsData.length; i++) {
                            $("#staticMutipleTransactionItems div[class='modal-body'] table[id='multipleSellItemsBreakupTable'] tbody").append('<tr id=' + i + '><td>' + data.transactionItemdetailsData[i].itemName + '</td><td>' + data.transactionItemdetailsData[i].pricePerUnit + '</td><td>' + data.transactionItemdetailsData[i].noOfUnits + '</td><td>' + data.transactionItemdetailsData[i].discountPer + '</td><td>' + data.transactionItemdetailsData[i].discountAmt + '</td><td>' + data.transactionItemdetailsData[i].grossAmount + '</td><td>' + data.transactionItemdetailsData[i].taxDescription + '</td><td>' + data.transactionItemdetailsData[i].totalInputTax + '</td><td>' + data.transactionItemdetailsData[i].withholdingAmount + '</td><td>' + data.transactionItemdetailsData[i].availableAdvance + '</td><td>' + data.transactionItemdetailsData[i].adjFromAdvance + '</td><td>' + data.transactionItemdetailsData[i].netAmount + '</td></tr>');
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

function listPayrollItems(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var transactionEntityId=parentTr.substring(18, parentTr.length);
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.transactionEntityId= transactionEntityId;
	$("#staticPayrollDetails").attr('data-toggle', 'modal');
	$("#staticPayrollDetails").modal('show');
	var url="/transactionItems/getPayrollItems";
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
			if(data.payrollEarnHeadsData.length > 0){
				$("#staticPayrollDetails div[class='modal-body']").html('<div class="datascrolltable" style="height: 100%; overflow: auto;"><table id="payrollMonthYear" class="table table-hover table-striped excelFormTable transaction-create" style="margin-top: 0px; margin-left: 0px !important;"><thead><th style="padding: 3px;">Payroll Month,Year</th></thead><tbody><tr></tr></tbody></table><table id="payrollItemsBreakupTable1" class="table table-hover table-striped excelFormTable transaction-create" style="margin-top: 5px !important; width:100%; float: left">'+
			    		'<thead class="tablehead1"><tr></tr></thead><tbody></tbody></table> <table id="payrollItemsBreakupTable2" class="table table-hover table-striped excelFormTable transaction-create" style="margin-top: 5px !important; width:100%; margin-left: 5px !important;">'+
			    		'<thead class="tablehead1"><tr></tr></thead><tbody></tbody></table>');
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] thead tr").append('<th style="padding: 5px; height:51px;">Name</th>');
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] thead tr").append('<th style="padding: 5px;">Eligible Days</th>');
				for(var i=0; i<data.payrollEarnHeadsData.length; i++){
					$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] thead tr").append('<th style="padding: 5px;">'+data.payrollEarnHeadsData[i].headName+'</th>');
				}
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] thead tr").append('<th style="padding: 5px;">Total Earnings</th>');

				if(data.payrollDeduHeadsData.length > 0){
					for(var i=0;i<data.payrollDeduHeadsData.length;i++){
						$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] thead tr").append('<th style="padding: 5px;">'+data.payrollDeduHeadsData[i].headName+'</th>');
					}
				}
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] thead tr").append('<th style="padding: 5px;height:51px;">Total Deductions</th>');
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] thead tr").append('<th style="padding: 5px;">Net Pay</th>');
			}

			if(data.payrollDetailsData.length>0){
				var  isMonthYear = 0;
			for(var i=0;i<data.payrollDetailsData.length;i++){
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody").append('<tr id='+i+'></tr>');
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody").append('<tr id='+i+'></tr>');
				//$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] thead tr").append(
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].empName+'</td>');
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].eligibleDays+'</td>');
				for(var j=0; j<data.payrollEarnHeadsData.length;j++){
					switch(j){
					case 0: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].income1+'</td>');
							break;
					case 1: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].income2+'</td>');
							break;
					case 2: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].income3+'</td>');
							break;
					case 3: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].income4+'</td>');
							break;
					case 4: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].income5+'</td>');
							break;
					case 5: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].income6+'</td>');
							break;
					case 6: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].income7+'</td>');
							break;
					}
				}
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable1'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].totalIncome+'</td>');
				for(var j=0; j<data.payrollDeduHeadsData.length;j++){
					switch(j){
					case 0: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].deduction1+'</td>');
							break;
					case 1: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].deduction2+'</td>');
							break;
					case 2: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].deduction3+'</td>');
							break;
					case 3: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].deduction4+'</td>');
							break;
					case 4: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].deduction5+'</td>')
							break;
					case 5: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].deduction6+'</td>');
							break;
					case 6: $("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].deduction7+'</td>');
							break;
						}
					}
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].totalDeduction+'</td>');
				$("#staticPayrollDetails div[class='modal-body'] table[id='payrollItemsBreakupTable2'] tbody tr[id="+i+"]").append('<td>'+data.payrollDetailsData[i].netPay+'</td>');
				if(isMonthYear==0){
					isMonthYear=1;
					$("#staticPayrollDetails div[class='modal-body'] table[id='payrollMonthYear'] tbody tr").append('<td>'+data.payrollDetailsData[i].payrollMonth+','+data.payrollDetailsData[i].payrollYear+'</td>');
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

function cancel(){
	$(".openWhatDoINeedToDo").hide();
	$(".dynmBnchBankActList").remove();
	$("#socpnreceiptdetail").find('option:first').prop("selected","selected");
	$("#rcpfccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rcafccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rsaafvpaymentdetail").find('option:first').prop("selected","selected");
	$("#paymentDetails").find('option:first').prop("selected","selected");
	$(".whatDoINeedToDoContent").html("");
	$(".whatDoINeedToDoContent").hide();
	$("#pendingExpenseId").attr("class","active");
	$('#createExpense select').find('option:first').prop("selected","selected");
	$('#createExpense input[type="text"]').val("");
	$('#createExpense textarea').val("");
	$(".klBranchSpecfTd").text("");
	$(".itemParentNameDiv").text("");
	$(".inventoryItemInStock").text("");
	$(".customerVendorExistingAdvance").text("");
	$(".resultantAdvance").text("");
	$(".budgetDisplay").text("");
	$(".inputtaxbuttondiv").html("");
	$(".inputtaxcomponentsdiv").html("");
	$(".vendorActPayment").text("");
	$(".withholdingtaxcomponentdiv").text("");
	$(".individualtaxdiv").text("");
	$(".individualtaxformuladiv").text("");
	$("#tmtpcaTxnForBranches").children().remove();
	$("#tmtpcaTxnForBranches").append('<option value="">--Please Select--</option>');
	$("#branchCashAvailable").val("");
	$("#branchPettyCashAvailable").val("");
	$("#transferPurpose").val("");
	$("#branchCashResultant").val("");
	$("#branchPettyCashResultant").val("");
	$("#bocaplunits").attr("readonly","");
	$("#bocpraunits").attr("readonly","");
	$(".actualbudgetDisplay").text("");
	$(".branchAvailablePettyCash").html("");
	$(".amountRangeLimitRule").text("");
	$(".discountavailable").text("");
	$("input[class='invValue']").val("");
	$("div[class='returnDetails']").html("");
	$(".balanceDueReturns").text("");
	$("div[class='maxReturnAmount']").html("");
	$(".netAmountDescriptionDisplay").text("");
	$("#procurementRequestRemarks").text("");
	$("#rcpfccvendcustoutstandingsgross").text("");
	$("#rcpfccvendcustoutstandingsnet").text("");
	$("#rcpfccvendcustoutstandingsnetdescription").text("");
	$("#rcpfccvendcustoutstandingspaid").text("");
	$("#rcpfccvendcustoutstandingsnotpaid").text("");
	$("#rcpfccvendcustoutstandingssalesreturn").text("");
	$("#mcpfcvvendcustoutstandingsgross").text("");
	$("#mcpfcvvendcustoutstandingsnet").text("");
	$("#mcpfcvvendcustoutstandingsnetdescription").text("");
	$("#mcpfcvvendcustoutstandingspaid").text("");
	$("#mcpfcvvendcustoutstandingsnotpaid").text("");
	$("#mcpfcvtxninprogress").text("");
	$("#mcpfcvvendcustoutstandingspurchasereturn").text("");
	$(".transactionDetailsTable").each(function(){
		$(this).hide();
	});
	$(".txnItems option:first").prop("selected","selected");
	$("#createExpense").slideUp('slow');
}


function addMoreSupportingDocuments(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var length=$("#"+parentTr+" div[id='moreSupportingDocDiv'] div[class='dynmmoreSupportingDocDiv']").length;
	if(parentTr=="bptycatrid"){
		$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="socpnuploadSuppDocs'+length+'" name="socpnuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="socpnuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
	}
}

$(document).ready(function(){
	$('input[name="addMoreSupportingDocuments"]').click(function(){
		var parentTr=$(this).parent().parent().attr('id');
		var length=$("#"+parentTr+" div[id='moreSupportingDocDiv'] div[class='dynmmoreSupportingDocDiv']").length;
		if(parentTr=="soccpntrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="socpnuploadSuppDocs'+length+'" name="socpnuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="socpnuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		}else if(parentTr=="soccpltrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="socpluploadSuppDocs'+length+'" name="socpluploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="socpluploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="bocpratrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="bocprauploadSuppDocs'+length+'" name="bocprauploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="bocprauploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="bocapltrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="bocapluploadSuppDocs'+length+'" name="bocapluploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="bocapluploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="rcpfcctrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="rcpfccuploadSuppDocs'+length+'" name="rcpfccuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="rcpfccuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="rcafcctrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="rcafccuploadSuppDocs'+length+'" name="rcafccuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="rcafccuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="mcpfcvtrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="mcpfcvuploadSuppDocs'+length+'" name="mcpfcvuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="mcpfcvuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="pcafcvtrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="pcafcvuploadSuppDocs'+length+'" name="pcafcvuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="pcafcvuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="srtfcctrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="srtfccuploadSuppDocs'+length+'" name="srtfccuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="srtfccuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="prtfcvtrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="prtfcvuploadSuppDocs'+length+'" name="prtfcvuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="prtfcvuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="claimRequestTravelAdvanceRow"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="claimRequestTravelAdvanceuploadSuppDocs'+length+'" name="claimRequestTravelAdvanceuploadSuppDocs'+length+'" readonly="readonly"><br/><input type="button" id="claimRequestTravelAdvanceuploadSuppDocs'+length+'" value="Upload" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)"></div>');
		} else if(parentTr=="quotattrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="quotationuploadSuppDocs'+length+'" name="quotationuploadSuppDocs'+length+'" readonly="readonly"><br/><button type="button" id="quotationuploadSuppDocs'+length+'" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)">Upload</<button></div>');
		} else if(parentTr=="profortrid"){
			$("#"+parentTr+" div[id='moreSupportingDocDiv']").append('<div class="dynmmoreSupportingDocDiv"><input type="text" id="proformauploadSuppDocs'+length+'" name="proformauploadSuppDocs'+length+'" readonly="readonly"><br/><button id="proformauploadSuppDocs'+length+'" class="btn btn-submit btn-idos" onclick="uploadFile(this.id)">Upload</<button></div>');
		}
	});
});


function leastWapGlobalSearchInBranchLocation(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var texttxnid=$(elem).attr('id');
	var transactionEntityId=texttxnid.substring(38, texttxnid.length);
	var textseltxnminwap=$(elem).attr('name');
	var transactionSelectedMinWAP=textseltxnminwap.substring(38, textseltxnminwap.length);
	if(transactionSelectedMinWAP!=""){
		var jsonData = {};
		var useremail=$("#hiddenuseremail").text();
		jsonData.transactionId=transactionEntityId;
		jsonData.minWAP=transactionSelectedMinWAP;
		jsonData.usermail = useremail;
		var url="/ecommerce/availableSupplierThanMinWAP";
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
				$(".staticwapbranchwisebreakupclose").trigger('click');
				$("#staticsupplierwap").attr('data-toggle', 'modal');
		    	$("#staticsupplierwap").modal('show');
		    	$(".staticsupplierwapclose").attr("href",location.hash);
		    	$("#staticsupplierwap div[class='modal-body']").html("");
		    	$("#staticsupplierwap h2[class='supplieritemName']").html("");
	    		$("#staticsupplierwap h2[class='supplieritemName']").append("<b>Available Suppliers In "+data.actualTransactionItemBranchWAPData[0].txnLocation+"</b>");
		    	$("#staticsupplierwap div[class='modal-body']").append('<div style="width: 100%;"><div class="actions button-set">Actual Transaction Minimum Weighted Average Price:</div></div><table class="table table-hover table-striped table-bordered excelFormTable transaction-create" id="supplierWAPActualTransactionTable" style="margin-top: 0px; width:600px;">'+
		    	'<thead class="tablehead1"><th>Item Name</th><th>Location</th><th>Minimum WAP Price In Location</th></thead><tbody><td class="minWAPitemName">'+data.actualTransactionItemBranchWAPData[0].itemName+'</td><td class="minWAPLocation">'+data.actualTransactionItemBranchWAPData[0].txnLocation+'</td><td>'+data.actualTransactionItemBranchWAPData[0].minWAP+'</td></tbody></table><br/>');
		    	$("#staticsupplierwap div[class='modal-body']").append('<div style="width: 100%;"><div class="actions button-set">Supplier list in '+data.actualTransactionItemBranchWAPData[0].txnLocation+'</div></div><div class="datascrolltable" STYLE=" height: 150px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-create" id="supplierItemPricingsTable" style="margin-top: 0px; width:600px;">'+
				'<thead class="tablehead1"><th>Supplier Name</th><th>Email</th><th>Number</th><th>Retailer Price</th><th>Wholeseller Price</th><th>Special Price</th><th>Contact Supplier</th></thead><tbody></tbody></table></div>');
		    	if(data.availableSuppliersLeassThanMinWAPData.length>0){
		    		for(var i=0;i<data.availableSuppliersLeassThanMinWAPData.length;i++){
		    			$("#staticsupplierwap div[class='modal-body'] table[id='supplierItemPricingsTable'] tbody").append('<tr class="'+data.availableSuppliersLeassThanMinWAPData[i].supplierEmail+'"><td>'+data.availableSuppliersLeassThanMinWAPData[i].supplierName+'</td><td>'+data.availableSuppliersLeassThanMinWAPData[i].supplierEmail+'</td><td>'+data.availableSuppliersLeassThanMinWAPData[i].supplierNumber+'</td><td>'+data.availableSuppliersLeassThanMinWAPData[i].supplierResellerPrice+'</td><td>'+data.availableSuppliersLeassThanMinWAPData[i].wholesellerPrice+'</td><td>'+data.availableSuppliersLeassThanMinWAPData[i].specialPrice+'</td><td><img align="middle" src="assets/images/vendorcontact.png" onclick="contactSupplierVendor(this)" style="width:40px;height;40px;margin-left:20px;"/></td></td></tr>');
		    		}
		    	}else{
		    		$("#staticsupplierwap div[class='modal-body']").append('Supplier Not Available');
		    	}
		    	$.unblockUI();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}else{
		alert("Weighted Average price data not vailable");
		return true;
	}
}

function generatePDFReceipt(elem, transPurposeID){
	var txnGstCountryCode = $("#gstCountryCode").val();
	if(txnGstCountryCode == "" || transPurposeID == RECEIVE_PAYMENT_FROM_CUSTOMER || transPurposeID == REFUND_ADVANCE_RECEIVED || transPurposeID ==REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE||transPurposeID== PAY_VENDOR_SUPPLIER || transPurposeID== PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var formName=$(elem).parent().attr('name');
		var transactionId=formName.substring(11, formName.length);
		var url=$("#"+formName+"").attr('action');
		var jsonData = {};
		var useremail=$("#hiddenuseremail").text();
		jsonData.email = useremail;
		jsonData.entityTxnId=transactionId;
		downloadFile(url, "POST", jsonData, "Error on receipt generation!");
	}else if(txnGstCountryCode !== "" && transPurposeID == RECEIVE_ADVANCE_FROM_CUSTOMER){
		showAdvReceiptPopup(elem);
	}
}

$(document).ready(function() {
	$(".downloadTransaction").click(function() {
		var chartOfAccountCategory=$("#searchCategory option:selected").val();

		var txnRefNumber=$("#searchTxnRefNumber").val();
		var chartOfAccountItem=$('#searchItems option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();

		var txnStatus1=$('#searchTxnStatus option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();
		var txnStatus = '\'' + txnStatus1.toString().split(',').join('\',\'') + '\'';
		var fromTxnDate=$("#searchFromDate").val();
		var toTxnDate=$("#searchToDate").val();
		var txnBranch=$('#searchBranch option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();
		var txnProject=$('#searchProject option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();
		var amountRangeLimtFrom=$("#amountFrom").val();
		var amountRangeLimtTo=$("#amountTo").val();
		var withwithoutdocument=$("#searchWithWithoutSuppDoc option:selected").val();
		var payMode=$('#txnSearchPayMode option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();
		var withwithoutremarks=$("#txnSearchRemarks option:selected").val();
		var txnException=$('#txnSearchException option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();
		var txnpurchaseVendor=$('#searchVendor option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();
		var txnsalesCustomer=$('#searchCustomer option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();
		var jsonData={};
			if(chartOfAccountCategory!=""){
				jsonData.searchCategory=chartOfAccountCategory;
			}
			if(txnRefNumber!=""){
				jsonData.searchTransactionRefNumber=txnRefNumber;
			}
			if(chartOfAccountItem!=""){
			jsonData.searchItems=chartOfAccountItem.toString();
			}
			if(txnStatus!=""){
				jsonData.searchTxnStatus=txnStatus.toString();
		}
		if(txnStatus1==""){
			jsonData.searchTxnStatus="";
		}
		if(fromTxnDate!=""){
			jsonData.searchTxnFromDate=fromTxnDate;
		}
		if(toTxnDate!=""){
			jsonData.searchTxnToDate=toTxnDate;
			}
			if(txnBranch!=""){
				jsonData.searchTxnBranch=txnBranch.toString();
			}
			if(txnProject!=""){
				jsonData.searchTxnProjects=txnProject.toString();
			}
			if(amountRangeLimtFrom!=""){
				jsonData.searchAmountRanseLimitFrom=amountRangeLimtFrom;
			}
			if(amountRangeLimtTo!=""){
				jsonData.searchAmountRanseLimitTo=amountRangeLimtTo;
			}
			if(withwithoutdocument!=""){
				jsonData.searchTxnWithWithoutDoc=withwithoutdocument;
			}
			if(payMode!=""){
				jsonData.searchTxnPyMode=payMode.toString();
			}
			if(withwithoutremarks!=""){
				jsonData.searchTxnWithWithoutRemarks=withwithoutremarks;
			}
			if(txnException!=""){
				jsonData.searchTxnException=txnException.toString();
			}
			if(txnpurchaseVendor!=""){
				jsonData.searchVendors=txnpurchaseVendor.toString();
			}
			if(txnsalesCustomer!=""){
				jsonData.searchCustomers=txnsalesCustomer.toString();
			}
		var txnUserType=$("#searchUserType option:selected").val();
		var txnQuestion1=$('#searchTxnQuestion option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			   }
		}).get();

		if(txnQuestion1!=""){
			jsonData.txnQuestion=txnQuestion1.toString();
		}
		if(txnUserType!=""){
			jsonData.txnUserType=txnUserType;
		}
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.txnDownloadAs=$("#downloadTransactionAs option:selected").val();
			var url="/transaction/downloadTransaction";
			downloadFile(url, "POST", jsonData, "Error on downloading transactions!");
	});
});

function downloadOverUnderOneEightyDayaTxnExcel(elem){

	//var buttonFor=$(elem).attr('class');
	var buttonFor=$(elem).attr('name');
	var txnModelFor=$(elem).attr('id');
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.downloadButtonFor=buttonFor;
	jsonData.downloadTxnModelFor=txnModelFor;
	jsonData.email = useremail;
	downloadFile('/user/downloadOverUnderOneEightyDayaTxnExcel', "POST", jsonData, "Error on downloading 180 days data!");
	//ajaxCall('/user/downloadOverUnderOneEightyDayaTxnExcel', jsonData, '', '', '', '', 'downloadOverUnderOneEightyDayaTxnExcelSuccess', '', true);
}

/*
function downloadOverUnderOneEightyDayaTxnExcelSuccess(data){
	if(data.result){
		var dt = new Date().toString();
    	var fileName=data.overUnderOneEightyReceivablePayablesTxnDataDownload[0].fileName;
    	var url='/assets/TransactionExcel/'+fileName+'?unique='+dt;
	    window.open(url);
	}
} */


function getklinvoices(elem){
	var parentTr=$(elem).closest('tr').attr('id');
	var transactionDiv = $(elem).parent().closest('div').attr('id');
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var value=$("#whatYouWantToDo").find('option:selected').val();
    var txnBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
    var txnToBranch = "";
      var refundType = "";
	var custvendid=$("#"+parentTr+" .masterList option:selected").val();

    if(value == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
        txnToBranch=$("#"+parentTr+" select[class='txnBranchesTo'] option:selected").val();
        if(txnToBranch === null || txnToBranch === ""){
            swal("Invalid to Branch!", "Please select branch to proceed with Transaction.", "error");
            return false;
        }
    }else if(value == REFUND_ADVANCE_RECEIVED || value == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
        txnToBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
        /*refundType = $("#"+parentTr+" select[class='mkrfndTxnTypeSelect'] option:selected").val();*/
        if(txnToBranch === ""){
            swal("Invalid to Branch!", "Please select branch to proceed with Transaction.", "error");
            return false;
        }
       /* if(refundType === ""){
            swal("Invalid Refund Type!", "Please select Refund Type to proceed with Transaction.", "error");
            return false;
        }*/
        if(custvendid === ""){
            swal("Invalid Customer !", "Please select Customer to proceed with Transaction.", "error");
            return false;
        }
        $("#"+parentTr+" select[class='txnTypeOfSupply'] option:first").prop("selected","selected");

    }else {
        if (typeof custvendid == 'undefined') {
            swal("Error", "Please contact support.", "error");
            return false;
        }
    }
	var fromDate = ""; var toDate = "";
	if(value == CREDIT_NOTE_CUSTOMER || value == DEBIT_NOTE_CUSTOMER || value == CANCEL_INVOICE || value == CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR){
        if(txnBranch === null || txnBranch === ""){
            swal("Invalid Branch!", "Please select a branch to proceed with Transaction.", "error");
            return false;
        }
		fromDate = $("#"+parentTr+" input[name='txnFromDate']").val();
		toDate = $("#"+parentTr+" input[name='txnToDate']").val();
		if(fromDate == "" || toDate == ""){
			swal("Error", "Provide valid From and To dates.", "error");
			$(elem).val("");
			return false;
		}
	}
	$("input[class='invValue']").val("");
	$("div[class='returnDetails']").html("");
	$(".balanceDueReturns").text("");
	$("div[class='maxReturnAmount']").html("");
	$("#"+parentTr+" select[class='salesExpenseTxns']").children().remove();
	$("#"+parentTr+" select[class='salesExpenseTxns']").append('<option value="">--Please Select--</option>');
	$(".klBranchSpecfTd").text("");
	$(".itemParentNameDiv").text("");
	$(".inventoryItemInStock").text("");
	$(".customerVendorExistingAdvance").text("");
	$(".resultantAdvance").text("");
	if(custvendid!="" ){ //&& incomeExpenseItem!=""
		 var jsonData = {};
		 jsonData.useremail=$("#hiddenuseremail").text();
		 jsonData.txnPurposeId=value;
		 jsonData.txnPurposeText=text;
		 jsonData.custVendEntityId=custvendid;
		 jsonData.fromDate = fromDate;
		 jsonData.toDate = toDate;
		 jsonData.txnBranch = txnBranch;
		 jsonData.txnToBranch = txnToBranch;
		 jsonData.refundType = refundType;
		 var url="/transaction/getklinvoices";
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
					$(".klBranchSpecfTd").html("");
					$(".itemParentNameDiv").text("");
					$(".inventoryItemInStock").text("");
					$(".customerVendorExistingAdvance").text("");
					$(".resultantAdvance").text("");
					var salesExpenseTxnsList = "";
					if(value == SELL_ON_CASH_COLLECT_PAYMENT_NOW || value == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
						for (var i = 0; i < data.txnSalesExpenseInvoicesData.length; i++) {
							salesExpenseTxnsList += ('<option value="' + data.txnSalesExpenseInvoicesData[i].id + '" txnRefNo="' + data.txnSalesExpenseInvoicesData[i].txnRefNo + '" incomeItemId="' + data.txnSalesExpenseInvoicesData[i].incomeId + '" incomeUnits="' + data.txnSalesExpenseInvoicesData[i].incomeUnits + '">' + data.txnSalesExpenseInvoicesData[i].name + '</option>');
						}
					} 
					// else if(value == REFUND_ADVANCE_RECEIVED) {
					// 	for (var i = 0; i < data.txnSalesExpenseInvoicesData.length; i++) {
					// 		salesExpenseTxnsList += ('<option value="' + data.txnSalesExpenseInvoicesData[i].id +  '" data-opening-adv-id="' + data.txnSalesExpenseInvoicesData[i].openingAdvId + '" txnRefNo="' + data.txnSalesExpenseInvoicesData[i].txnRefNo + '">' + data.txnSalesExpenseInvoicesData[i].name + '</option>');
					// 	}
					// } 
					else{
						for (var i = 0; i < data.txnSalesExpenseInvoicesData.length; i++) {
							salesExpenseTxnsList += ('<option value="' + data.txnSalesExpenseInvoicesData[i].id + '" txnRefNo="' + data.txnSalesExpenseInvoicesData[i].txnRefNo + '">' + data.txnSalesExpenseInvoicesData[i].name + '</option>');
						}
					}
					if(value == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
						 $("#"+transactionDiv+" table[class='multipleItemsTable'] tbody tr:last").find("select[class='salesExpenseTxns']").children().remove();
						 $("#"+transactionDiv+" table[class='multipleItemsTable'] tbody tr:last").find("select[class='salesExpenseTxns']").append('<option value="">----Please Select----</option>');
						$("#" + transactionDiv +" table[class='multipleItemsTable'] tbody tr:last").find("select[class='salesExpenseTxns']").append(salesExpenseTxnsList);
					}else{
						 $("#"+parentTr+" select[class='salesExpenseTxns']").children().remove();
						$("#"+parentTr+" select[class='salesExpenseTxns']").append('<option value="">----Please Select----</option>');
						$("#"+parentTr+" select[class='salesExpenseTxns']").append(salesExpenseTxnsList);

					}
					if(value == CREDIT_NOTE_CUSTOMER ||value == DEBIT_NOTE_CUSTOMER || value == CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR   ){
						$('.txnDestGstinCls').val("");
						$('.txnTypeOfSupply').val("");
					}
		      		
				},
		      error: function (xhr, status, error) {
			      	if(xhr.status == 401){ doLogout();
			      	}else if(xhttp.status == 500){
	    				swal("Error on fetching knowledge library!", "Please retry, if problem persists contact support team", "error");
	    			}
		      },
			   	complete: function(data) {
					$.unblockUI();
			   	}
		   });
		 }
}

var validateGstItemsForCategory = function(elem){
	if (GST_COUNTRY_CODE == "" || GST_COUNTRY_CODE === undefined || GST_COUNTRY_CODE == null) {
		return true;
	}
	var multiItemsTable = $(elem).closest('table').attr('id');
	var parentOfparentTr = $(elem).parents().closest('tr').attr('id');
	var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	if(transPurposeId == SELL_ON_CASH_COLLECT_PAYMENT_NOW || transPurposeId == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || transPurposeId == RECEIVE_ADVANCE_FROM_CUSTOMER || transPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId == BUY_ON_CREDIT_PAY_LATER || transPurposeId == BUY_ON_PETTY_CASH_ACCOUNT || transPurposeId == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER || transPurposeId == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE){
		parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
	}
	var sourceGstin = $("#"+parentOfparentTr+" select[class='txnBranches']").children(":selected").attr("id");
	if(typeof sourceGstin == 'undefined'){
		return false;
	}
	if(sourceGstin === null || sourceGstin === ""){
		swal("Invalid Branch's GSTIN", "Please select valid a Branch.", "error");
		enableTransactionButtons();
		return false;
	}
	var txnTypeOfSupply = $("#"+parentOfparentTr+" select[class='txnTypeOfSupply']").val();
	if((txnTypeOfSupply == "" || txnTypeOfSupply == null) && (transPurposeId != TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER && transPurposeId != REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE)){
		swal("Invalid Type of Supply!", "Please select valid Type of Supply.", "error");
		$("#"+multiItemsTable+" span[class='select2-selection__clear']").trigger("mousedown");
		return false;
	}
	var destGstinCode = "";
	var txnForUnavailableCustomer=$("#"+parentOfparentTr+" input[class='unavailable ui-autocomplete-input']").val();
	if(txnForUnavailableCustomer !== null && txnForUnavailableCustomer !== "" && typeof txnForUnavailableCustomer !== "undefined"){
		var txnWalkinCustomerType = $("#"+parentOfparentTr+" select[class='walkinCustType']").val();
		if(txnWalkinCustomerType == ""){
			swal("Invalid Type of Customer!", "Please select valid Type of customer.", "error");
			returnValue=false;
		}else if(txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2"){
			destGstinCode = $("#"+parentOfparentTr+" input[class='placeOfSplyTextHid']").val();
		}else if(txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4"){
			destGstinCode = sourceGstin;
		}else if(txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6"){
			destGstinCode = $("#"+parentOfparentTr+" select[name='txnWalkinPlcSplySelect']").val();
		}
	}else if(transPurposeId == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
        destGstinCode = $("#"+parentOfparentTr+" select[class='txnBranchesTo']").children(":selected").attr("id");
    }else{
		destGstinCode = $("#"+parentOfparentTr+" select[class='placeOfSply txnDestGstinCls']").val();
	}
	if(txnTypeOfSupply != "3" && destGstinCode == ""){
		swal("Invalid Place of Supply!", "Please provide valid place of supply.", "error");
		$("#"+multiItemsTable+" span[class='select2-selection__clear']").trigger("mousedown");
		return false;
	}
	if(transPurposeId == SELL_ON_CASH_COLLECT_PAYMENT_NOW || transPurposeId == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
		var parentTr = $(elem).closest('tr').attr('id');
		var itemCategory = $(elem).children(":selected").attr("category");
		if(txnTypeOfSupply == "7" && (itemCategory != "1" && itemCategory != "2" && itemCategory != "3")){
			$("#"+parentTr+" span[class='select2-selection__clear']").trigger("mousedown");
			swal("Invalid item category!", "Only GST Exempt/Nil Rated/ Non GST goods are allowed.", "error");
			return false;
		}
	}
	return true;
	/* This code is to select only same category items.
	var trLen = $("#" + multiItemsTable + " > tbody > tr").length;
	if(trLen === 1){
		$("#gstPreviousItemCategory").val(itemCategory);
		return true;
	}

	 var gstCountryCode = $("#gstCountryCode").val();
	if(gstCountryCode === "" || gstCountryCode === null){
		$("#gstPreviousItemCategory").val(itemCategory);
		return true;
	}
	var previousItemCat = $("#gstPreviousItemCategory").val();
	if(itemCategory === previousItemCat){
		$("#gstPreviousItemCategory").val(itemCategory);
		return true;
	}else{
		$("#"+parentTr+" span[class='select2-selection__clear']").trigger("mousedown");
		swal("Invalid item category!", "GST Item category is not same as previous selected item.", "error");
		return false;
	}*/
}

var resetBasedOnTypeOfSupply = function(elem){
	var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	var transactionTableTr = $(elem).closest('tr').attr('id');
	var txnBranchId=$("#"+transactionTableTr+" select[class='txnBranches'] option:selected").val();

	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	if(isSingleUserDeploy == "true") {
		if(txnBranchId == "" || typeof txnBranchId == "undefined"){
				return false;
		}
	}
	if(txnBranchId == "" || typeof txnBranchId == "undefined"){
		swal("Invalid Branch!", "Please select a valid branch.", "error");
		 $(elem).val("");
		return false;
	}
	var parentId = $(elem).closest('.transaction-create').closest('div').attr('id');
	var txnTypeOfSupply = $("#"+transactionTableTr+" select[class='txnTypeOfSupply']").val();
	if(transPurposeId == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
		if(txnTypeOfSupply == "4" || txnTypeOfSupply == "5"){

			$("#multipleItemsTablepcafcv th[name='reverserChargeItem']").show();
			$("#multipleItemsTablepcafcv select[class='txnRcmTaxItem']").on('change', getAdvanceTxnItemParent(elem));
			$("#multipleItemsTablepcafcv select[class='txnItems']").off('change');
			$("#multipleItemsTablepcafcv td[name='txnRcmTaxItem']").show();
			//$("#"+transactionTableTr+ " select[class='txnRcmTaxItem']").show();
		}
		if(txnTypeOfSupply == "1" || txnTypeOfSupply == "2" ||  txnTypeOfSupply == "3"){
			$("#multipleItemsTablepcafcv th[name='reverserChargeItem']").hide();
			$("#multipleItemsTablepcafcv td[name='txnRcmTaxItem']").hide();
			//$("#"+transactionTableTr+ " select[class='txnRcmTaxItem']").hide();
		}

        $("#multipleItemsTablepcafcv div[class='customerVendorExistingAdvance']").text('');
	}else if(transPurposeId != BUY_ON_CREDIT_PAY_LATER){
		var txnTypeOfSupply = $("#"+transactionTableTr+" select[class='txnTypeOfSupply']").val();

		if(isSingleUserDeploy == "true") {
			$("#"+transactionTableTr+" p[class='isWalkinCustPara']").hide();
			if(txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5"){
				$("#"+transactionTableTr+" div[class='txnWalkinDivCls']").hide();
			}else{
				$("#"+transactionTableTr+" div[class='txnWalkinDivCls']").show();
			}
		}else {
			if(txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5" || txnTypeOfSupply == "6"){
				$("#"+transactionTableTr+" p[class='isWalkinCustPara']").hide();
			}else{
				$("#"+transactionTableTr+" p[class='isWalkinCustPara']").show();
			}
		}

		if(txnTypeOfSupply == "3" || txnTypeOfSupply == "4" || txnTypeOfSupply == "5"){
			$("#"+transactionTableTr+" p[name='withWithoutTaxPara']").show();
		}else{
			$("#"+transactionTableTr+" p[name='withWithoutTaxPara']").hide();
			$("#"+transactionTableTr+" select[class='txnWithWithoutTaxCls']").val('');

		}
	}

	if(transPurposeId == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
		resetMultiItemsTableLength("staticmultipleitemspcafcv");
		resetMultiItemsTableFieldsData("staticmultipleitemspcafcv");
		$("#staticmultipleitemspcafcv table[class='multipleItemsTable'] tr").last().find(".txnItems").val("");
		$("#staticmultipleitemspcafcv table[class='multipleItemsTable'] tr").last().find(".txnItems").change();
		resetBasedOnWithWithoutTax("staticmultipleitemspcafcv");
	}else {
		resetMultiItemsTableLength(transactionTableTr);
		resetMultiItemsTableFieldsData(elem);
		resetBasedOnWithWithoutTax(elem);
	}


	changeMultipleItemTableForBuyTrans(transPurposeId,txnTypeOfSupply,parentId,elem,transactionTableTr);
}

var resetBasedOnWithWithoutTax = function(elem){
	var transactionTableTr = $(elem).closest('tr').attr('id');
	var txnTypeOfSupply = $("#"+transactionTableTr+" select[class='txnTypeOfSupply']").val();
	if(txnTypeOfSupply == "3"){
		//$("#"+transactionTableTr+" p[class='withWithoutTaxPara']").show();
		$("#"+transactionTableTr+" select[name='txnPlaceOfSply']").val("");
		$("#"+transactionTableTr+" select[name='txnPlaceOfSply']").attr("disabled", "disabled");
	}else{
		//$("#"+transactionTableTr+" p[class='withWithoutTaxPara']").hide();
		$("#"+transactionTableTr+" select[name='txnPlaceOfSply']").val("");
		$("#"+transactionTableTr+" select[name='txnPlaceOfSply']").removeAttr("disabled");
	}
	resetMultiItemsTableLength(transactionTableTr);
	resetMultiItemsTableFieldsData(elem);
}

var getTransactionsOnload = function(){
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	jsonData.perPage=50;
	jsonData.curPage=0;
	cancel();
	var url="/user/userTransactions";
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		contentType : 'application/json',
		success     : function (data) {
			var totalRecords=data.totalRecords;
			var sessionuser="";
			if(data.sessionuserTxnData){
				sessionuser=data.sessionuserTxnData[0].sessemail;
			}
			if(sessionuser!='null'){
				txnMsg=data.approval;
				txnMsg+='<br>'+data.approved;
				displayTransactionRecords(data);
				getCashBankReceivablePayable();
				setPagingDetail('transactionTable', 20, 'pagingTransactionNavPosition');
			}else if(sessionuser=='null'){
				window.location.href="/logout";
			}
        },
		error : function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on fetching transactions!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	})
}

var validateSelectedItems = function(elem){
	var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	var multiItemsTable = $(elem).closest('table').attr('id');
	var parentTr = $(elem).closest('tr').attr('id');

	var selectedItem = $(elem).val();
	var count=0;
	if(transPurposeId == BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId == BUY_ON_CREDIT_PAY_LATER) {
		selectedItem = $("#"+parentTr).find("td .txnItems option:selected").val();
		var selectedRcmItem = $("#"+parentTr).find("td .txnRcmTaxItem option:selected").val();
		var parentOfParent = $("#"+multiItemsTable).closest(".transactionDetailsTable").find("table tbody tr:first").attr('id');
		var typeOfSupply = $("#" + parentOfParent +" select[class='txnTypeOfSupply']").val();
		if(typeOfSupply == "4" || typeOfSupply == "5") {
			$("#" + multiItemsTable + " > tbody > tr").each(function() {
				var itemId = $(this).find("td .txnItems option:selected").val();
				var rcmItemId = $(this).find("td .txnRcmTaxItem option:selected").val();
				if(selectedItem == itemId && selectedRcmItem == rcmItemId){
						count++;
					}
				});
				if(count > 1){
					//$(elem).trigger("mousedown");
					$("#"+parentTr).find("td .txnRcmTaxItem option:first").prop("selected","selected");
					$("#" + parentTr +" span[class='select2-selection__clear']").trigger("mousedown");
					return false;
				}else{
					return true;
				}
		}else {
			$("#" + multiItemsTable + " > tbody > tr").each(function() {
				var itemId = $(this).find("td .txnItems option:selected").val();
				if(selectedItem == itemId){
					count++;
				}
			});
			$("#" + multiItemsTable + " > tbody > tr").each(function() {
				var itemId = $(this).find("td .salesExpenseTxns option:selected").val();
				if(selectedItem == itemId){
					count++;
				}
			});
				if(count > 1){
				//$(elem).trigger("mousedown");
					$(".pendingTxns").val("");
				$("#" + parentTr +" span[class='select2-selection__clear']").trigger("mousedown");
				return false;
			}else{
				return true;
			}
		}
	} else if(transPurposeId == RECEIVE_PAYMENT_FROM_CUSTOMER || transPurposeId == PAY_VENDOR_SUPPLIER){
		count=0;
		$("#" + multiItemsTable + " > tbody > tr").each(function() {
			var itemId = $(this).find("td .pendingTxns option:selected").val();
			if(selectedItem == itemId){
				count++;
			}
		});
		if(count > 1){
			$(elem).val("");
			return false;
		}else{
			return true;
		}
	}else {
		$("#" + multiItemsTable + " > tbody > tr").each(function() {
			var itemId = $(this).find("td .txnItems option:selected").val();
			if(selectedItem == itemId){
				count++;
			}

		});
		$("#" + multiItemsTable + " > tbody > tr").each(function() {
			var itemId = $(this).find("td .salesExpenseTxns option:selected").val();
			if(selectedItem == itemId){
				count++;
			}

		});
		if(count > 1){
			//$(elem).trigger("mousedown");
			$("#" + parentTr +" span[class='select2-selection__clear']").trigger("mousedown");
			return false;
		}else{
			return true;
		}
	}
}

var generateAgeingReport = function(elem, exportType, ispayable){
	var tabid = $("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").attr('class');
	var jsonData = {};
	var ageingDate = $("#ageingDate").val();
	if(ageingDate == ""){
		swal("Ageing date cannot be empty!", "You must select a date to generate ageing report.", "error");
		return false;
	}
	jsonData.exportType = exportType;
	jsonData.ageingDate = ageingDate;
	jsonData.tabid = tabid;
	var url="/report/ageing";
	downloadFile(url, "POST", jsonData, "Error on generate ageing report!");
}
function downloadTransactionTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/downloadTransactionTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading Transaction Template!");
}
function downloadSellOnCashTransactionTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/downloadSellOnCashTransactionTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading Transaction Template!");
}
function downloadTransactionRecieveFromCustomerTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/downloadRcvPayFromCustomerTransactionTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading Transaction Template!");
}

function validateRemovalTr(parentId) {
    var totalRow =  $("#"+parentId).find(".removeCheckBox").length;
    var selectedRows = $("#"+parentId).find(".removeCheckBox:checkbox:checked").length;
    if(selectedRows == 0){
        swal("Select Items First!", "Please choose the items from Table", "error");
        return false;
    }
    if(totalRow == 1) {
        swal("Last Item!", "At least one item needed otherwisw Remove Outer Row.", "error");
        return false;
    }
    if(selectedRows < totalRow) {
        return true;
    }
    return false;
}

function calculateGrandTotal(tableId) {
    let grandTotal = 0;
    $("#"+tableId).find('.netAmountValTotal').val("");
    $("#"+tableId).find(".netAmountVal").each(function(){
        let total = Number($.trim($(this).val()));
        if($.isNumeric(total)) {
            grandTotal += total;
        }
    });
	let amountDbl = Math.round(grandTotal * 100.00)/100.00;
    $("#"+tableId).find('.netAmountValTotal').val(amountDbl);
}

var bindSelect = function(className){
    $("."+className).select2({
        placeholder: "Select an item",
        allowClear: true,
        multiple: false
    });
}
function downloadBuyOnCreditTransactionTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/downloadBuyOnCreditTransactionTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading Transaction Template!");
}

function downloadBuyOnCashTransactionTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/downloadBuyOnCashTransactionTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading Transaction Template!");
}
function downloadTransactionPayVendorTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/downloadPayVendorTransactionTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading Transaction Template!");

}
// **************** Reports on TopBar **********
function changeReportPanel(elem){
	var selectedId = $(elem).find('a').data("id");
	if(selectedId != "") {
		if(selectedId == "1") {
			showCashBook(elem);
		}else if(selectedId == "2") {
			showBankBook(elem);
		}else if(selectedId == "3") {
			showTrialBalance(elem);
		}else if(selectedId == "4") {
			reportAllInventory(elem);
		}else if(selectedId == "5") {
			renderPLBSCOAMappings(elem);
		}else if(selectedId == "6") {
			renderProfitLoss(elem);
		}else if(selectedId == "7") {
			submitTransactionsToKarvy(elem);
		}else if(selectedId == "8") {
			getJSONOutputFromKarvy(elem);
		}else if(selectedId == "9") {
            getTransactionsDataInXlsx(elem);
        }else if(selectedId == "10") {
            $('button[id="cashBalanceAllBranches"]').show();
            $('button[id="bankBalanceAllBranches"]').hide();
            $('button[id="accountsReceivablesAllBranches"]').hide();
            $('button[id="accountsPayablesAllBranches"]').hide();
        }else if(selectedId == "11") {
            $('button[id="cashBalanceAllBranches"]').hide();
            $('button[id="bankBalanceAllBranches"]').show();
            $('button[id="accountsReceivablesAllBranches"]').hide();
            $('button[id="accountsPayablesAllBranches"]').hide();
        }else if(selectedId == "12") {
            $('button[id="cashBalanceAllBranches"]').hide();
            $('button[id="bankBalanceAllBranches"]').hide();
            $('button[id="accountsReceivablesAllBranches"]').show();
            $('button[id="accountsPayablesAllBranches"]').hide();
        }else if(selectedId == "13") {
            $('button[id="cashBalanceAllBranches"]').hide();
            $('button[id="bankBalanceAllBranches"]').hide();
            $('button[id="accountsReceivablesAllBranches"]').hide();
            $('button[id="accountsPayablesAllBranches"]').show();
        }else if(selectedId == "14") {
            getTDSReportInXlsx(elem);
        }
	}
}

function setRcmTaxAndCess(elem) {
	var parentTr = $(elem).closest('tr').attr('id');
	var rcmItem = $(elem).find("option:selected").val();
	if(rcmItem == "") {
		$("#"+parentTr+" .txnGstTaxRate").val("");
		$("#"+parentTr+" .txnCessRate").val("");
		return false;
	}
	var taxRate = $(elem).find("option:selected").attr("taxRate");
	var cessRate = $(elem).find("option:selected").attr("cessRate");
	var taxRateName = $(elem).find("option:selected").attr("taxRateName");
	var taxNameId = $(elem).find("option:selected").attr("taxNameId");
	var cessRateName = $(elem).find("option:selected").attr("cessRateName");
	var cessNameId = $(elem).find("option:selected").attr("cessNameId");

	if(taxRateName.length > 4){
        taxRateName = taxRateName.substring(0, 4);
	}
    if(cessRateName.length > 4){
        cessRateName = cessRateName.substring(0, 4);
    }

	if(Number(taxRate) > 0) {
		$("#"+parentTr+" .txnGstTaxRate").val(taxRate);
		$("#"+parentTr+" .txnGstTaxRate").data("taxName",taxRateName);
		$("#"+parentTr+" .txnGstTaxRate").data("taxId",taxNameId);
	}else{
		$("#"+parentTr+" .txnGstTaxRate").val('');
		$("#"+parentTr+" .txnGstTaxRate").data("taxName","");
		$("#"+parentTr+" .txnGstTaxRate").data("taxId","");
	}
	if(cessRate != "") {
		$("#"+parentTr+" .txnCessRate").val(cessRate);
		$("#"+parentTr+" .txnCessRate").data("taxName",cessRateName);
		$("#"+parentTr+" .txnCessRate").data("taxId",cessNameId);
	}else{
		$("#"+parentTr+" .txnCessRate").val('');
		$("#"+parentTr+" .txnCessRate").data("taxName","");
		$("#"+parentTr+" .txnCessRate").data("taxId","");
	}

	$("#"+parentTr+" .txnPerUnitPrice").trigger("keyup");
}

//Manali: For quarterly datepicker
// This adds the function 'afterShow' to the datepicker to show quarter 4 months only in calender for karvy GST JSON-GSTR4
$(function() {
	  $.datepicker._updateDatepicker_original = $.datepicker._updateDatepicker;
	  $.datepicker._updateDatepicker = function(inst) {
	    $.datepicker._updateDatepicker_original(inst);
	    var afterShow = this._get(inst, 'afterShow');
	    if (afterShow)
	      afterShow.apply((inst.input ? inst.input[0] : null)); // trigger custom callback
	  }
	});

$(function() {
  $('#txtDate3').datepicker({
    dateFormat: 'yy-mm-dd',
    changeMonth: true,
    changeYear: true,
    showButtonPanel: true,
    afterShow: function(dateText, inst) {
      var keepMonths = [];
      for (i = -1; i < 12; i += 3) {
        keepMonths.push(i);
      }
      $(".ui-datepicker-month option").each(function() {
        if ($.inArray(parseInt(this.value), keepMonths) < 0) {
          $(this).remove();
        }
      });
    },

	  onClose: function(dateText, inst) {
	      var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
	      var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
	      $(this).val($.datepicker.formatDate('MM yy', new Date(year, month, 1)));
	  }
  });

  $("#txtDate3").focus(function () {
      $(".ui-datepicker-calendar").hide();
      $("#ui-datepicker-div").position({
          my: "center top",
          at: "center bottom",
          of: $(this)
      });
  });
});
$(function() {
	  $('#txtDateGSTR3B3').datepicker({
	    dateFormat: 'yy-mm-dd',
	    changeMonth: true,
	    changeYear: true,
	    showButtonPanel: true,
	    afterShow: function(dateText, inst) {
	      var keepMonths = [];
	      for (i = -1; i < 12; i += 3) {
	        keepMonths.push(i);
	      }
	      $(".ui-datepicker-month option").each(function() {
	        if ($.inArray(parseInt(this.value), keepMonths) < 0) {
	          $(this).remove();
	        }
	      });
	    },

		  onClose: function(dateText, inst) {
		      var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
		      var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
		      $(this).val($.datepicker.formatDate('MM yy', new Date(year, month, 1)));
		  }
	  });

	  $("#txtDateGSTR3B3").focus(function () {
	      $(".ui-datepicker-calendar").hide();
	      $("#ui-datepicker-div").position({
	          my: "center top",
	          at: "center bottom",
	          of: $(this)
	      });
	  });
	});
$(function() {
	var maximumYr = new Date().getFullYear();
        $(".backdatedDatePicker").datepicker({
            changeMonth : true,
            changeYear : true,
            dateFormat:  'MM d,yy',
            yearRange: ''+new Date().getFullYear()-1+':'+maximumYr+'',
            onSelect: function(x,y){
                $(this).focus();
               verifyLastTransaction(this,x);
            }

        });
});
$(function() {
	var maximumYr = new Date().getFullYear();
        $(".backdatedSellOnCreditDatePicker").datepicker({

            changeMonth : true,
            changeYear : true,
            dateFormat:  'MM d,yy',
            yearRange: ''+new Date().getFullYear()-1+':'+maximumYr+'',
            onSelect: function(x,y){
                $(this).focus();
               verifyLastTransaction(this,x);
            }

        });
});


function validateTxnRefNo(elem) {
	var txnRefNo = $(elem).val();
	if(txnRefNo != "") {
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		if(txnRefNo.length > 16){
			alert("Document Ref. No. should not be more than 16 characters.");
			$(elem).focus();
		}
		jsonData.txnRefNo=txnRefNo;
		var url="/transaction/validateTxnRefNo";
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
					if(data.valid == false) {
						$(elem).val("");
						swal("Duplicate Entry!", "Same invoice number not allowed", "error");
					}
				},
			    error: function (xhr, status, error) {
					if(xhr.status == 401){
						doLogout();
					}else if(xhr.status == 500){
						swal("Error on fetching advance/discount!", "Please retry, if problem persists contact support team", "error");
					}
				},
				complete: function(data) {

				}
		});
	}
}

function checkForSupportDocLimit(parentOfparentTr, parentTr, grossTotal){
		var txnBranch=$("#"+parentOfparentTr+" select[class='txnBranches'] option:selected").val();
		var txnSpecific=$("#"+parentTr).find("td .txnItems option:selected").val();
		var uploadDoc=$("#"+parentOfparentTr+" input[name='bocprauploadSuppDocs']").val();
		if(txnSpecific != "" && txnBranch != "") {
			var jsonData = {};
			jsonData.useremail=$("#hiddenuseremail").text();
			jsonData.txnSpecific=txnSpecific;
			jsonData.txnBranch=txnBranch;
			jsonData.grossTotal=grossTotal;
			var url="/specifics/getSupportDocLimit";
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
						if(data.limitAmt != "") {
							var dataLimit = parseFloat(data.limitAmt);
							if(grossTotal > dataLimit) {
								$("#"+parentTr).attr("exceedLimit", "true");
							}else {
								$("#"+parentTr).attr("exceedLimit", "false");
							}
						}
					},
				    error: function (xhr, status, error) {
						if(xhr.status == 401){
							doLogout();
						}else if(xhr.status == 500){
							swal("Error on fetching advance/discount!", "Please retry, if problem persists contact support team", "error");
						}
					},
					complete: function(data) {

					}
			});
		}
}


function getTransactionList(){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	var url="/config/getsellreceicve"
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		contentType : 'application/json',
		success     : function (data) {
			$("#searchTxnQuestion").children().remove();
			var searchTransaction = "";
			for(var i=0; i<data.sellReceiveData.length; i++){
				searchTransaction += '<option value="'+data.sellReceiveData[i].id+'">'+data.sellReceiveData[i].name+'</option>';
	    	 }
            $('#searchTxnQuestion').append(searchTransaction);
            $('#searchTxnQuestion').multiselect('rebuild');

		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on fetching vendor/customer!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

function getTransactionStatuses(){
	$("#searchTxnStatus").children().remove();
	$("#searchTxnStatus").append('<option value="Require Approval">Require Approval</option>');
	$("#searchTxnStatus").append('<option value="Require Clarification">Require Clarification</option>');
	$("#searchTxnStatus").append('<option value="Clarified">Clarified</option>');
	$("#searchTxnStatus").append('<option value="Approved">Approved</option>');
	$("#searchTxnStatus").append('<option value="Require Additional Approval">Require Additional Approval</option>');
	$("#searchTxnStatus").append('<option value="Rejected">Rejected</option>');
	$("#searchTxnStatus").append('<option value="Accounted">Accounted</option>');
	$('#searchTxnStatus').multiselect('rebuild');
}

function getPayModes(){
	$("#txnSearchPayMode").children().remove();
	$("#txnSearchPayMode").append('<option value="1">CASH</option>');
	$("#txnSearchPayMode").append('<option value="2">BANK</option>');
	$('#txnSearchPayMode').multiselect('rebuild');
	}

function getTransactionExceptions(){
	$("#txnSearchException").children().remove();
	$("#txnSearchException").append('<option value="1">Transaction Exceeding Budget</option>');
	$("#txnSearchException").append('<option value="2">Knowledge Library Not Followed</option>');
	$("#txnSearchException").append('<option value="3">Transaction Exceeding Budget & Knowledge Library Not Followed</option>');
	$('#txnSearchException').multiselect('rebuild');
}

function getVendorName(){
console.log('getVendorName');
	var vLength=$('#vInput').val().length;
	var arr=[];
	inpText=$('#vInput').val();
	var jsonData={};
	jsonData.vName=inpText;
	var url="/vendor/searchVendorName";
	if(vLength<3){
		$('#vDiv').css('display','none');
	}
	if(vLength>=3){
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
				console.log('vLength is...'+vLength);
				if(vLength<3){
					data.vendorListData=[];
					$('#vDiv').css('display','none');
				}
				$('#vDiv').html('');
				$('#vDiv').css('display','block');

					data.vendorListData.forEach(function(element){
						console.log(element.name);
						$('#vDiv').append('<div id='+element.id+'>'+element.name+'</div>');
						arr.push(element.name);
				});
					$('#vDiv > div').click(function(){
						$('#vInput').val($(this).html());
						$('#vendIDDiv').val($(this).attr('id'));
						$('#vDiv > div').css('display','none');
					});
					$('#vDiv > div').mouseover(function(event){
						$(this).css('background-color','blue');
						$(this).css('color','white');
					});
					$('#vDiv > div').mouseout(function(event){
						$(this).css('background-color','white');
						$(this).css('color','black');
					});
				},

			error:function (data){
				swal('No Vendor found please re-enter correct Vendor name');
		}
	});
   }
}
function getCustomerName(){
	console.log('getCustomerName');
		var cLength=$('#cInput').val().length;
		var arr=[];
		inpText=$('#cInput').val();
		var jsonData={};
		jsonData.cName=inpText;
		var url="/customer/searchCustomerName";
		if(cLength<3){
			$('#cDiv').css('display','none');
		}
		if(cLength>=3){
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
					if(cLength<3){
						data.vendorListData=[];
						$('#cDiv').css('display','none');
					}
					$('#cDiv').html('');
					$('#cDiv').css('display','block');

						data.vendorListData.forEach(function(element){
							$('#cDiv').append('<div id='+element.id+'>'+element.name+'</div>');
					});
						$('#cDiv > div').click(function(){
							$('#cInput').val($(this).html());
							$('#custIDDiv').val($(this).attr('id'));
							$('#cDiv > div').css('display','none');
						});
						$('#cDiv > div').mouseover(function(event){
							$(this).css('background-color','blue');
							$(this).css('color','white');
						});
						$('#cDiv > div').mouseout(function(event){
							$(this).css('background-color','white');
							$(this).css('color','black');
						});
						},

				error:function (data){
					swal('No customer found please re-enter correct Customer name');
			}
		});
	}
	}

var getReceiptPaymentDetails = function(txnJsonData, parentTr){
	var txnPaymentMode = $("#" + parentTr + " select[class='txnPaymodeCls'] option:selected").val();
	var txnReceiptPaymentBank = "";
	var txnInstrumentNum = "";
	var txnInstrumentDate = "";
	var txnReceiptDescription = "";
	if(txnPaymentMode == ""){
		swal("Payment Method is not selected", "Please select Cash or Bank", "error");
		enableTransactionButtons();
		return false;
	}else if (txnPaymentMode == "2") {
		txnReceiptPaymentBank = $("#" + parentTr + " select[name='availableBank'] option:selected").val();
		if (typeof txnReceiptPaymentBank != 'undefined' && txnReceiptPaymentBank != "") {
			txnInstrumentNum = $("#" + parentTr + " input[class='txnInstrumentNoCls']").val();
			if(txnInstrumentNum == ""){
				swal("Incomplete transaction detail!", "Instrument Number cannot be empty.", "error");
				enableTransactionButtons();
				return false;
			}
			txnInstrumentDate = $("#" + parentTr + " input[class='txnInstrumentDtCls hasDatepicker']").val();
			if (txnInstrumentDate == "") {
				swal("Incomplete transaction detail!", "Instrument Date cannot be empty.", "error");
				enableTransactionButtons();
				return false;
			}
		}else{
			swal("Incomplete transaction detail!", "Select a bank to proceed.", "error");
			enableTransactionButtons();
			return false;
		}
		txnReceiptDescription = $("#" + parentTr + " textarea[class='txnReceptTextCls']").val();
	}else if(txnPaymentMode == "1"){
		txnReceiptDescription = $("#" + parentTr + " textarea[class='txnReceptTextCls']").val();
	}
	txnJsonData.txnReceiptDetails = txnPaymentMode;
    txnJsonData.txnReceiptPaymentBank = txnReceiptPaymentBank;
    txnJsonData.txnInstrumentNum = txnInstrumentNum;
    txnJsonData.txnInstrumentDate = txnInstrumentDate;
    txnJsonData.txnReceiptDescription = txnReceiptDescription;
}
/*function verifyLastTransaction(elem,x){

	var jsonData = {};
	var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	var transactionDate=x;
	var txnPurposeBranchId="";
	if(transactionPurposeId==1){
		txnPurposeBranchId=$("#soccpnTxnForBranches").find('option:selected').val();
	}
	if(transactionPurposeId==2){
		txnPurposeBranchId=$("#soccplTxnForBranches").find('option:selected').val();
	}
	var text=$("#whatYouWantToDo").find('option:selected').text();
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnPurposeId=transactionPurposeId;
	jsonData.txnPurposeBnchId=txnPurposeBranchId;
	jsonData.transactionDate=transactionDate;
	var url="/transaction/checkforlasttarnsactiondate";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
					"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		contentType : 'application/json',
		success     : function (data) {

		   if(data.value=="valid date"){

		   }else{
			   swal("CHANGE DATE","Selected date is less then last transaction date");
		   }
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
		  		doLogout();
		  	}else if(xhr.status == 500){
				swal("Error while confirmation of backdated transaction", "Please retry, if problem persists contact support team", "error");
			}
      	}
	});

}*/

// *********** Don't Add New code here .... File contain more than 7000 lines *************

