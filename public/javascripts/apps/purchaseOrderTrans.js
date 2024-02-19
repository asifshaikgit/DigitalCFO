var submitForApprovalPurchaseOrder = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	var txnNetAmount=$("#purordnetamntTotal").val();
	if(txnNetAmount==""){
		swal("Incomplete transaction data!", "Please provide complete transaction details", "error");
		enableTransactionButtons();
		return true;
	}

	var txnGrossAmountTotal = 0;
	$("#"+parentTr+" input[name='purordgross']").each(function(){
		if($(this).val()!=""){
			txnGrossAmountTotal += parseFloat($(this).val());
		}
	});

	var txnUnitsTotal = 0;
	$("#"+parentTr+" input[name='purordunits']").each(function(){
		if($(this).val()!=""){
			txnUnitsTotal += parseFloat($(this).val());
		}
	});

	var txnEntityID = $("#"+parentTr).attr("name");
	var txnForBranch=$("#purordTxnForBranches option:selected").val();
	var txnForProject=$("#purordTxnForProjects option:selected").val();
	//var destinGstin = $("#"+parentTr+" select[class='placeOfSply txnDestGstinCls']").attr("id");
	var destinGstin =$("#purordPlaceOfVend option:selected").attr("id");
	var txnForItem = convertTableDataToArray("multipleItemsTablepurord");
	var txnForCustomer=$("#purordVendor option:selected").val();
	var txnPoReference=$("#purord_reference").val();
	var txnRemarks=$("#purordRemarks").val();
	var txnRemarksPrivate=$("#purordRemarksPrivate").val();
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
	txnJsonData.txnDestinGstin = destinGstin;
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
	    		swal("Error on Submit For Approval!", "Please retry, if proplem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
			enableTransactionButtons();
		}
	});
}

function calculateNetAmtForPurchaseOrder(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var parentOfparentTr = $("#"+parentTr).parent().parent().parent().parent().closest('div').attr('id');
	var gross=$("#"+parentTr+" input[id='purordgross']").val();
	if(gross == ""){
		return false;
	}
	var netAmount = parseFloat(gross);
	$("#"+parentTr+" input[id='purordnetamnt']").val((netAmount*1).toFixed(2));
	var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	$("#"+parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));
    $("#" + parentTr + " div[class='netAmountDescriptionDisplay']").html("");
}

function generatePDFPurchaseOrder(elem){
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
	downloadFile(url, "POST", jsonData, "Error on purchase order invoice generation!");
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
