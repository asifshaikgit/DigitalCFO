var populateQuotationData = function (argument) {
	showTransactionBranchKnowledgeLiabrary(argument);
	//getAdvanceDiscount(argument);
	calculateGross(argument);
	calcNetAmtQuotation(argument);
}

var calcNetAmtQuotation = function (elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	var parentOfparentTr = $("#"+parentTr).parent().parent().parent().parent().closest('div').attr('id');
	var txnBranch=$("#"+parentOfparentTr+" select[class='txnBranches']").val();
	var txnVendorCustomer=$("#"+parentOfparentTr+" .masterList").val();	//$(".masterList option:selected").val();
	var txnitemSpecifics=$("#"+parentTr+" .txnItems option:selected").val();
	var txnGrossAmountTmp=$("#"+parentTr+" input[class='txnGross']").val();
	var proceedSalesTxn=false;
	if(txnBranch==""){
		swal("Incomplete transaction detail!", "Please select a Branch.", "error");
		return false;
	}else if(txnVendorCustomer==""){
		swal("Incomplete transaction detail!", "Please select customer.", "error");
		return false;
	}else if(txnitemSpecifics==""){
		swal("Incomplete transaction detail!", "Please select a Item for transaction.", "error");
		return false;
	}/*else if(txnGrossAmountTmp==""){
		swal("Incomplete transaction detail!", "Please provide price/unit to calaculate Gross Amount.", "error");
		return false;
	}*/
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$("#netAmountLabel").html("");
	var gross=$("#"+parentTr+" input[class='txnGross']").val();
	var netAmount=parseFloat(gross);
	if(netAmount == ""){
		return false;
	}
    $("#"+parentTr+" input[class='netAmountVal']").val(parseFloat(netAmount).toFixed(2));
	var netAmtTotal=0;
	$("#multipleItemsTableQuotation > tbody > tr").each(function() {
		var grossAmt = $(this).find("td input[class='txnGross']").val();
		if(grossAmt!="" && typeof grossAmt!='undefined'){
			netAmtTotal = netAmtTotal + parseFloat(grossAmt);
		}
	});

	$("#quotationnetamntTotal").val(Math.round(netAmtTotal));
	$.unblockUI();

}

var submitForApprovalQuotation = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	var txnNetAmount=$("#quotationnetamntTotal").val();
	if(txnNetAmount==""){
		swal("Incomplete transaction data!", "Please provide complete transaction details", "error");
		enableTransactionButtons();
		return true;
	}

	var txnGrossAmountTotal = 0;
	$("#"+parentTr+" input[name='quotationgross']").each(function(){
		if($(this).val()!=""){
			txnGrossAmountTotal += parseFloat($(this).val());
		}
	});

	var txnUnitsTotal = 0;
	$("#"+parentTr+" input[name='quotationunits']").each(function(){
		if($(this).val()!=""){
			txnUnitsTotal += parseFloat($(this).val());
		}
	});

	var txnEntityID = $("#"+parentTr).attr("name");
	var txnForBranch=$("#quotationTxnForBranches option:selected").val();
	var txnForProject=$("#quotationTxnForProjects option:selected").val();
	var txnForItem = convertTableDataToArray("multipleItemsTableQuotation");
	var txnForCustomer=$("#quotationCustomer option:selected").val();
	var txnPoReference=$("#quotation_reference").val();
	var txnRemarks=$("#quotationRemarks").val();
	var txnRemarksPrivate=$("#quotationRemarksPrivate").val();
	var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');

	var txnJsonData={};
	txnJsonData.txnEntityID = txnEntityID;
	txnJsonData.txnPurpose=whatYouWantToDo;
	txnJsonData.txnPurposeVal=whatYouWantToDoVal;
	txnJsonData.txnforbranch=txnForBranch;
	txnJsonData.txnforproject=txnForProject;
	txnJsonData.txnforitem=txnForItem;
	txnJsonData.txnforcustomer=txnForCustomer;
	txnJsonData.txnPoReference=txnPoReference;
	txnJsonData.txnnetamount=txnNetAmount;
	txnJsonData.txnremarks=txnRemarks;
	txnJsonData.txnRemarksPrivate=txnRemarksPrivate;
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
			cancel();
			viewTransactionData(data); // to render the updated transaction recored
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on Submit For Approval!", "Please retry, if proplem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
			enableTransactionButtons();
		}
	});
}

var calcNetAmtProforma = function (elem, data, parentTr){
	var transTableTr = $("#"+parentTr).parent().closest('tr').attr('id');
	$("#"+transTableTr+" div[class='netAmountDescriptionDisplay']").html("");
	for(var i=0;i<data.branchSpecificsTaxComponentData.length;i++){
	   if(i==0){
			$("#"+transTableTr+" div[class='netAmountDescriptionDisplay']").append(data.branchSpecificsTaxComponentData[i].individualTax+",");
			$("#"+parentTr+" div[class='individualtaxdiv']").text(data.branchSpecificsTaxComponentData[i].individualTax);
			$("#"+parentTr+" div[class='individualtaxformuladiv']").text(data.branchSpecificsTaxFormulaComponentData[i].individualTaxFormula);
		}else{
			$("#"+transTableTr+" div[class='netAmountDescriptionDisplay']").append('<br/>'+data.branchSpecificsTaxComponentData[i].individualTax+",");
			var existingIndividualTaxComp=$("#"+parentTr+" div[class='individualtaxdiv']").text();
			var existingIndividualTaxFormulaComp=$("#"+parentTr+" div[class='individualtaxformuladiv']").text();
			$("#"+parentTr+" div[class='individualtaxdiv']").text(existingIndividualTaxComp+","+data.branchSpecificsTaxComponentData[i].individualTax);
			$("#"+parentTr+" div[class='individualtaxformuladiv']").text(existingIndividualTaxFormulaComp+","+data.branchSpecificsTaxFormulaComponentData[i].individualTaxFormula);
		}
	}

	var taxTotalAmount=parseFloat(data.branchSpecificsTaxResultAmountData[0].taxTotalAmount);
	var taxTypeAndRate = $("#"+transTableTr+" div[class='netAmountDescriptionDisplay']").text();
	$("#"+transTableTr+" div[class='netAmountDescriptionDisplay']").append('<br/>Net Tax:'+(taxTotalAmount*1).toFixed(2)+",");
	//var adjustmentAmount=$("#"+parentTr+" input[id='soccplhowmuchfromadvance']").val();
	//var discount=$("#soccpldiscountavailable").text();
	var gross=$("#"+parentTr+" input[class='txnGross']").val();
	var netAmount=parseFloat(gross);

	/*
	if(adjustmentAmount!="" && gross!=""){
		netAmount=(parseFloat(netAmount)-parseFloat(adjustmentAmount));
		$("#socplnetAmountLabel").append('<br/>Adjustment:'+adjustmentAmount+",");
	}

	if(discount!="" && gross!=""){
		var discountAmount=((discount/100.0)*gross).toFixed(2);
		netAmount=(parseFloat(netAmount)-parseFloat(discountAmount));
		$("#socplnetAmountLabel").append('<br/>Discount:'+discountAmount);
	} */

	if(taxTotalAmount!=""){
		netAmount=(parseFloat(netAmount)+parseFloat(taxTotalAmount));
	}
	$("#"+parentTr+" input[class='txnTaxTypes']").val(taxTypeAndRate);
	$("#"+parentTr+" input[class='txnTaxAmount']").val((taxTotalAmount*1).toFixed(2));
	$("#"+parentTr+" input[class='netAmountVal']").val((netAmount*1).toFixed(2));
	let parentOfparentTr = $("#"+parentTr).closest('div').attr('id');
	var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	$("#"+transTableTr+" input[class='netAmountValTotal']").val(Math.round(netAmountTotal));
}

var submitForApprovalProforma = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	var txnNetAmount=$("#proformanetamntTotal").val();
	if(txnNetAmount==""){
		swal("Incomplete transaction data!", "Please provide complete transaction details", "error");
		enableTransactionButtons();
		return true;
	}

	var txnGrossAmountTotal = 0;
	$("#"+parentTr+" input[name='proformagross']").each(function(){
		if($(this).val()!=""){
			txnGrossAmountTotal += parseFloat($(this).val());
		}
	});

	var txnUnitsTotal = 0;
	$("#"+parentTr+" input[name='proformaunits']").each(function(){
		if($(this).val()!=""){
			txnUnitsTotal += parseFloat($(this).val());
		}
	});

	var txnEntityID = $("#"+parentTr).attr("name");
	var txnForBranch=$("#proformaTxnForBranches option:selected").val();
	var txnForProject=$("#proformaTxnForProjects option:selected").val();
	var txnForItem = convertTableDataToArray("multipleItemsTableProforma");
	var txnForCustomer=$("#proformaCustomer option:selected").val();
	var txnPoReference=$("#proforma_reference").val();
	var txnRemarks=$("#proformaRemarks").val();
	var txnRemarksPrivate=$("#proformaRemarksPrivate").val();
	var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var txnJsonData={};
	txnJsonData.txnEntityID = txnEntityID;
	txnJsonData.txnPurpose=whatYouWantToDo;
	txnJsonData.txnPurposeVal=whatYouWantToDoVal;
	txnJsonData.txnforbranch=txnForBranch;
	txnJsonData.txnforproject=txnForProject;
	txnJsonData.txnforitem=txnForItem;
	txnJsonData.txnforcustomer=txnForCustomer;
	txnJsonData.txnPoReference=txnPoReference;
	txnJsonData.txnnetamount=txnNetAmount;
	txnJsonData.txnremarks=txnRemarks;
	txnJsonData.txnRemarksPrivate=txnRemarksPrivate;
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
			cancel();
			viewTransactionData(data); // to render the updated transaction recored
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on Submit For Approval!", "Please retry, if proplem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
			enableTransactionButtons();
		}
	});
}

function generatePDFQuotProf(elem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var formName=$(elem).attr('id');
	var exportType=formName.substring(0, 4);
	var transactionId=formName.substring(4, formName.length);
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	jsonData.entityTxnId=transactionId;
	jsonData.exportType = exportType;
	var url="/exportInvoicePdf";
	downloadFile(url, "POST", jsonData, "Error on Quotation or Proforma invoice generation!");
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
