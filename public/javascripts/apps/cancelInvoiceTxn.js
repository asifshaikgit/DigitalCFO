var addItemsInCancelInvoiceTxn = function(customerVendorItemsListTemp, transPurposeId){
	var multiItemsTableTr = '<tr id="caninv'+length+'"><td><select class="txnItems" id="caninvItem" readonly="readonly"><option value="">Please Select</option></select></td>';

	multiItemsTableTr += '<td><input class="txnPerUnitPrice" placeholder="Price Per Unit" type="text" id="caninvPerUnitPrice"  onkeypress="return onlyDotsAndNumbers(event);" onblur="checkData4CreditDebitNote(this, \'txnPerUnitPriceHid\');" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);"/><input type="hidden" class="txnPerUnitPriceHid"/></td>';
	multiItemsTableTr += '<td><input class="txnNoOfUnit" type="text" id="caninvUnits" placeholder="Units(if any)" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" onblur="checkData4CreditDebitNote(this, \'txnNoOfUnitHid\'); checkStockAvailableForSellTran(this);" /><input type="hidden" class="txnNoOfUnitHid"/></td>';

	multiItemsTableTr += '<td><input type="text" class="txnDiscountPercent" id="caninvDiscountPercent" placeholder="Discount %" onkeypress="return onlyDotsAndNumbers(event);" onblur="checkData4CreditDebitNote(this, \'txnDiscountPercentHid\'); " onkeyup="calculateDiscountSell(this); calculateGross(this); calculateNetAmountForSell(this);" readonly="readonly"/>&#37;<input type="hidden" class="txnDiscountPercentHid"/>';

	multiItemsTableTr += '<br><input type="text" class="txnDiscountAmount" id="caninvDiscountAmt"  placeholder="Discount Amt" readonly="readonly"/></td>';

	multiItemsTableTr += '<td><input class="txnGross"  placeholder="Gross Amount" type="text" id="caninvGross" readonly="readonly"></td>';

	multiItemsTableTr += '<td><div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div><div id="taxCell3"></div><div id="taxCell4"></div></div><input type="hidden" class="txnTaxTypes" id="caninvTaxtypes"/><input type="hidden" class="itemTaxAmount"/></td>';

	multiItemsTableTr += '<td><input type="text" class="invoiceValue" placeholder="Invoice Value" id="caninvInvoiceValue" readonly="readonly"/></td>';

	multiItemsTableTr += '<td><input type="text" class="netAmountVal" id="caninvNetamnt" readonly="readonly" placeholder="Net Result"></td>';
	multiItemsTableTr += '<td><div class="netAmountDescriptionDisplay" id="netAmountLabel"></div></td><td><input style="display:none;" type="checkbox" class="removeTxnCheckBox"/></td></tr>';
	$("#staticsellmultipleitemscaninv table[class='multipleItemsTable'] tbody").append(multiItemsTableTr);
	$("#multipleItemsTablecaninv > tbody > tr[id='caninv"+length+"'] > td > .txnItems").append(customerVendorItemsListTemp);
	//enableDisableNoteTxnField(transPurposeId);
}
var setCancelInvoiceTaxDetail = function(data, value, i, transactionTableTr){
	if(data.txnItemData[i].taxData){
		for(var j=0; j<data.txnItemData[i].taxData.length; j++){
			var taxCellData = '<div class="txnTaxNameCls">'+data.txnItemData[i].taxData[j].taxName+'</div><input type="text" class="taxRate" readonly="readonly" placeholder="Tax Rate" value="'+data.txnItemData[i].taxData[j].taxRate+'"/>'+
				'<input type="text" class="txnTaxAmount" readonly="readonly" placeholder="Tax Amount" value="'+data.txnItemData[i].taxData[j].taxAmount+'"/>';
			taxCellData+='<input type="hidden" class="txnTaxName" value="'+data.txnItemData[i].taxData[j].taxName+'"/>';
			taxCellData+='<input type="hidden" class="txnTaxID" value="'+data.txnItemData[i].taxData[j].taxid+'"/>';

			$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell"+j+"']").addClass('taxCellCls');
			$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell"+j+"']").empty();
			$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell"+j+"']").append(taxCellData);
		}
	}

	if(data.txnItemData[i].advAdjTaxData){
		for(var j=0; j<data.txnItemData[i].advAdjTaxData.length; j++){
			var taxCellData = '<div class="txnTaxNameCls">'+data.txnItemData[i].advAdjTaxData[j].taxName+'</div><input type="text" class="txnTaxOnAdvAdjCls" id="taxOnAdvAdj" readonly="readonly" placeholder="Tax Amount" value="'+data.txnItemData[i].advAdjTaxData[j].taxAmount+'"/>';
			taxCellData += '<input type="hidden" class="txnTaxNameOnAdvAdjCls" id="taxNameOnAdvAdj" value="'+data.txnItemData[i].advAdjTaxData[j].taxName+'"/>';
			$("#"+transactionTableTr+ " div[id='advAdjTaxCell"+j+"']").addClass('advAdjTaxCellCls');
			$("#"+transactionTableTr+ " div[id='advAdjTaxCell"+j+"']").empty();
			$("#"+transactionTableTr+ " div[id='advAdjTaxCell"+j+"']").append(taxCellData);
		}
		$("#"+transactionTableTr+ " div[id='advAdjTaxCell']").addClass('div'+j+'-wd');
	}
}


var submitForAccountingCancelInvoice = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	//let parentOfparentTr = "staticsellmultipleitemscaninv";
	var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
	var txnForCustomer = $("#"+parentTr+" .masterList option:selected").val();
	var txnTotalNetAmount = $("#"+parentTr+" input[class='netAmountValTotal']").val();
	let parentOfparentTr = $("#"+parentTr).closest('div').attr('id');
	isValid = validateMultiItemsCreditDebit("multipleItemsTablecaninv");
	if(!isValid){
		swal("Incomplete transaction data!", "Please provide complete transaction details before submitting for cancellation", "error");
		enableTransactionButtons();
		return false;
	}
	var txnForItem = convertTableDataToArray("multipleItemsTablecaninv");
	var totalTxnNetAmtWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	var totalTxnTaxAmt = calcuateTotalAmt4Element(parentOfparentTr,'itemTaxAmount');
	var totalTxnGrossAmt = calcuateTotalAmt4Element(parentOfparentTr,'txnGross');
	var txnRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClass']").val();
	var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var netAmountTotalWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	var txnTotalInvoiceValue = $("#"+parentTr+" input[class='totalInvoiceValue']").val();
	if((txnTotalNetAmount==""  || parseFloat(txnTotalNetAmount) == 0.0) && (txnTotalInvoiceValue == "" || parseFloat(txnTotalInvoiceValue) ==0.0)){
		swal("Error on Submit for cancellation!", "Please provide complete transaction details before submitting for cancellation", "error");
		enableTransactionButtons();
		return true;
	}
	var txnNetAmountDescription=$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").text();
	var txnSaleEntityID = $("#"+parentTr+" select[class='salesExpenseTxns']").find('option:selected').val();
	var txnEntityID = $("#"+parentTr).attr("name");
	var txnJsonData={};
	setStaticSellTransInvoiceData(txnJsonData); //to set popup details
	txnJsonData.txnEntityID = txnEntityID;
	txnJsonData.txnSaleEntityID = txnSaleEntityID;
	txnJsonData.txnPurpose=whatYouWantToDo;
	txnJsonData.txnPurposeVal=whatYouWantToDoVal;
	txnJsonData.txnForBranch=txnForBranch;
	txnJsonData.txnForProject=txnForProject;
	txnJsonData.txnForItem=txnForItem;
	txnJsonData.txnForCustomer=txnForCustomer;
	txnJsonData.txnTotalNetAmount=txnTotalNetAmount;
	txnJsonData.totalTxnNetAmtWithDecimalValue = totalTxnNetAmtWithDecimalValue;
	txnJsonData.totalTxnTaxAmt = totalTxnTaxAmt;
	txnJsonData.totalTxnGrossAmt = totalTxnGrossAmt;
	txnJsonData.txnNetAmountDescription=txnNetAmountDescription;
	txnJsonData.txnRemarks=txnRemarks;
	//txnJsonData.txnprocrem=procurementRequestRemarks;
	txnJsonData.supportingdoc=supportingDoc;
	txnJsonData.txnDocumentUploadRequired="false";
	txnJsonData.useremail=$("#hiddenuseremail").text();
	var url="/transaction/submitForAccounting";
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
			if(typeof data.roundupMappingFound!='undefined' && !data.roundupMappingFound){
	   			swal("Round-up: mapping missing!", "", "error");
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
				swal("Error on Submit For Cancellation!", "Please retry, if problem persists contact support team", "error");
			}
		},
		complete: function(data) {
			$.unblockUI();
			enableTransactionButtons();
		}
	});
}

var submitForApprovalCancelInvoice = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	//var parentOfparentTr = "staticsellmultipleitemscaninv";
	var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
	var txnForCustomer = $("#"+parentTr+" .masterList option:selected").val();
	var txnTotalNetAmount = $("#"+parentTr+" input[class='netAmountValTotal']").val();
	let parentOfparentTr = $("#"+parentTr).closest('div').attr('id');
	isValid = validateMultiItemsCreditDebit("multipleItemsTablecaninv");
	if(!isValid){
		swal("Incomplete transaction data!", "Please provide complete transaction details before submitting for cancellation", "error");
		enableTransactionButtons();
		return false;
	}
	var txnForItem = convertTableDataToArray("multipleItemsTablecaninv");
	var totalTxnNetAmtWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	var totalTxnTaxAmt = calcuateTotalAmt4Element(parentOfparentTr,'itemTaxAmount');
	var totalTxnGrossAmt = calcuateTotalAmt4Element(parentOfparentTr,'txnGross');
	var txnRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClass']").val();
	var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var netAmountTotalWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	var txnTotalInvoiceValue = $("#"+parentTr+" input[class='totalInvoiceValue']").val();
	if((txnTotalNetAmount==""  || parseFloat(txnTotalNetAmount) == 0.0) && (txnTotalInvoiceValue == "" || parseFloat(txnTotalInvoiceValue) ==0.0)){
		swal("Error on Submit for cancellation!", "Please provide complete transaction details before submitting for cancellation", "error");
		enableTransactionButtons();
		return true;
	}
	var txnNetAmountDescription=$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").text();
	var txnSaleEntityID = $("#"+parentTr+" select[class='salesExpenseTxns']").find('option:selected').val();
	var txnEntityID = $("#"+parentTr).attr("name");
	var txnJsonData={};
	setStaticSellTransInvoiceData(txnJsonData); //to set popup details
	txnJsonData.txnEntityID = txnEntityID;
	txnJsonData.txnSaleEntityID = txnSaleEntityID;
	txnJsonData.txnPurpose=whatYouWantToDo;
	txnJsonData.txnPurposeVal=whatYouWantToDoVal;
	txnJsonData.txnForBranch=txnForBranch;
	txnJsonData.txnForProject=txnForProject;
	txnJsonData.txnForItem=txnForItem;
	txnJsonData.txnForCustomer=txnForCustomer;
	txnJsonData.txnTotalNetAmount=txnTotalNetAmount;
	txnJsonData.totalTxnNetAmtWithDecimalValue = totalTxnNetAmtWithDecimalValue;
	txnJsonData.totalTxnTaxAmt = totalTxnTaxAmt;
	txnJsonData.totalTxnGrossAmt = totalTxnGrossAmt;
	txnJsonData.txnNetAmountDescription=txnNetAmountDescription;
	txnJsonData.txnRemarks=txnRemarks;
	//txnJsonData.txnprocrem=procurementRequestRemarks;
	txnJsonData.supportingdoc=supportingDoc;
	txnJsonData.txnDocumentUploadRequired="false";
	txnJsonData.useremail=$("#hiddenuseremail").text();
	var url="/transaction/submitForApproval";
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
				return false;
			}
			cancel();
			viewTransactionData(data); // to render the updated transaction recored
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
				swal("Error on Submit For Cancellation!", "Please retry, if problem persists contact support team", "error");
			}
		},
		complete: function(data) {
			enableTransactionButtons();
		}
	});
}
