/************************************************************************************************
This file contains functions for Receive payment from customer and Receive advance from customer
Created by Sunil K. Namdev on 14-07-2017.
*************************************************************************************************/

function populateRecItemData(argument) {
	clearMultiItemsCurrentTrData(argument);
	if(DUPLICATE_ITEMS_ALLOWED_FOR_TXN == 0){
	var returnValue = validateSelectedItems(argument);
	if(returnValue === false){
		return false;
		}
	}
	returnValue= validateGstItemsForCategory(argument);
	if(returnValue === false){
		return false;
	}
	getAdvanceTxnItemParent(argument);
}

var getTaxComponentForReceive = function(elem){
	var returnValue = true;
	var parentTr = $(elem).closest('tr').attr('id');
	var parentOfparentTr = $("#"+parentTr).parent().parent().parent().parent().closest('div').attr('id');
	var parentTable = $(elem).parents().closest('table').attr('id');
	var text= $("#whatYouWantToDo").find('option:selected').text();
	var txnPurposeVal= $("#whatYouWantToDo").find('option:selected').val();
	var txnBranch= $("#"+parentOfparentTr+" select[class='txnBranches']").val();
	if(txnBranch==""){
		swal("Incomplete transaction detail!", "Please select a Branch.", "error");
		returnValue=false;
	}

	var txnTypeOfSupply = $("#"+parentOfparentTr+" select[class='txnTypeOfSupply']").val();
	if(txnTypeOfSupply==""){
		swal("Incomplete transaction detail!", "Please select a Type of Supply.", "error");
		returnValue=false;
	}
	var txnWithWithoutTax = $("#"+parentOfparentTr+" select[class='txnWithWithoutTaxCls']").val();
	if((txnTypeOfSupply =="3" || txnTypeOfSupply =="4" || txnTypeOfSupply =="5") && txnWithWithoutTax==""){
		swal("Incomplete transaction detail!", "Please select With / Without IGST.", "error");
		returnValue=false;
	}
	var txnUnavailable= $("#"+parentOfparentTr+" input[name='unAvailableCustomer']").val();
	var txnVendorCustomer= $("#"+parentOfparentTr+" .masterList").val();
	if(txnVendorCustomer=="" && txnUnavailable==""){
		swal("Incomplete transaction detail!", "Please select customer or provide customer name.", "error");
		returnValue=false;
	}
	var txnitemSpecifics= $("#"+parentTr+" .txnItems").val();
	if(txnitemSpecifics==""){
		swal("Incomplete transaction detail!", "Please select a Item for transaction.", "error");
		returnValue=false;
	}
	var taxableValue = 0;
	var taxWithHeld = 0;
	if(txnPurposeVal == RECEIVE_ADVANCE_FROM_CUSTOMER){
        taxWithHeld = $("#"+parentTr+" input[class='withholdingtaxcomponenetCls']").val();
        if(taxWithHeld != "" && taxWithHeld != "0"){
            taxableValue = parseFloat(taxWithHeld);
        }
	}else{
        taxWithHeld = $("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").text();
        if(taxWithHeld != "" && taxWithHeld != "0"){
            taxableValue = parseFloat(taxWithHeld);
        }
	}

	var advanceReceived = $("#"+parentTr+" input[name='advanceReceived']").val();
	if(advanceReceived == "" || advanceReceived == "0"){
		returnValue=false;
	}
	taxableValue = parseFloat(taxableValue) + parseFloat(advanceReceived);
	$("#"+parentTr+" input[class='txnGross']").val(taxableValue);
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

	$("#"+parentOfparentTr+" input[class='netAmountValTotal']").val("");
	if(returnValue === true){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var jsonData = {};
		jsonData.useremail= $("#hiddenuseremail").text();
		jsonData.userTxnPurposeText=text;
		jsonData.txnBranchId=txnBranch;
		jsonData.txnSpecificsId=txnitemSpecifics;
		jsonData.txnAdjustmentAmount=taxableValue;
		jsonData.txnWithheld=taxWithHeld;
		jsonData.txnSelectedVendorCustomer=txnVendorCustomer;
		jsonData.txnPurposeValue = txnPurposeVal;
		jsonData.txnSourceGstinCode = sourceGstinCode;
		jsonData.txnDestGstinCode = destGstinCode;
		jsonData.txnDestCustDetailId = destCustDetailId;
		jsonData.txnTypeOfSupply = txnTypeOfSupply;
		jsonData.txnWithWithoutTax = txnWithWithoutTax;
		var url="/transaction/getAdvAdjTax";
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
			   	if(txnPurposeVal == RECEIVE_ADVANCE_FROM_CUSTOMER){
				    //var taxNameList = "";
				    var taxTypeAndRate ="";
		    		for(var i=0;i<data.advAdjTaxData.length;i++){
		    			//taxNameList += '<div class="taxNameHead">'+data.advAdjTaxData[i].taxName+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
		    			var taxCellData = data.advAdjTaxData[i].taxName + '<input type="text" class="taxRate" name="rcafccTaxRate" id="rcafccTaxRate" readonly="readonly" placeholder="Tax Rate" value="'+data.advAdjTaxData[i].taxRate+'"/>';
		    			taxCellData+='<input type="text" class="txnTaxAmount" name="rcafccTaxamnt" readonly="readonly" id="rcafccTaxamnt" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);" placeholder="Tax Amount" value="'+data.advAdjTaxData[i].taxAmount+'"/>';
						taxCellData+='<input type="hidden" class="txnTaxName" name="rcafccTaxName" id="rcafccTaxName" value="'+data.advAdjTaxData[i].taxName+'"/>';
						taxCellData+='<input type="hidden" class="txnTaxID" name="rcafccTxnTaxID" id="rcafccTxnTaxID" value="'+data.advAdjTaxData[i].taxid+'"/>';
						$("#"+parentTr+ " div[id='taxCell"+i+"']").addClass('taxCellCls');
						$("#"+parentTr+ " div[id='taxCell"+i+"']").empty();
		    			$("#"+parentTr+ " div[id='taxCell"+i+"']").append(taxCellData);
		    			if(i == 0){
		    				taxTypeAndRate = (data.advAdjTaxData[i].individualTax) + "," ;
		    			}else{
		    				taxTypeAndRate += (data.advAdjTaxData[i].individualTax) + "," ;
		    			}
		    		}
		    		$("#"+parentTr+" input[class='txnTaxTypes']").val(taxTypeAndRate);
		    		$("#"+parentTr+ " div[id='taxCell']").addClass('div-w'+i+'00');
		    		/*$("#"+parentTable+" div[id='taxNameList']").addClass('div-w'+i+'00');
		    		$("#"+parentTable+" div[id='taxNameList']").empty();
		    		$("#"+parentTable+" div[id='taxNameList']").append(taxNameList);*/

                    var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
                    $("#"+parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));
		    	}
			},
			error: function (xhr, status, error) {
		   		if(xhr.status == 401){ doLogout();
		   		}else if(xhr.status == 500){
		    		swal("Error on fetching Tax Component!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
	$.unblockUI();
	return returnValue;
}

function submitForAccountingCustRec(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	let txnGstCountryCode = $("#gstCountryCode").val();
	let txnForUnavailableCustomer = $("#"+parentTr+" input[name='unAvailableCustomer']").val();
	let txnTypeOfSupply = ""; let txnWalkinCustomerType =""; let txnWithWithoutTax ="";
	let itemItemsList = ""; let customerLocation = "";
	let sourceGstin = "";
	let destinGstin = "";
	let txnJsonData={};
	if(txnGstCountryCode !== "" && typeof txnGstCountryCode !='undefined' && txnGstCountryCode !== null){
		txnTypeOfSupply = $("#"+parentTr+" select[class='txnTypeOfSupply']").val();
		if(txnTypeOfSupply == "" || txnTypeOfSupply == null){
			swal("Invalid Type of Supply!", "Please select valid Type of Supply.", "error");
			enableTransactionButtons();
			return false;
		}
		txnWithWithoutTax = $("#"+parentTr+" select[class='txnWithWithoutTaxCls']").val();
		if((txnTypeOfSupply =="3" || txnTypeOfSupply =="4" || txnTypeOfSupply =="5") && txnWithWithoutTax==""){
			swal("Incomplete transaction detail!", "Please select With / Without IGST.", "error");
			enableTransactionButtons();
			return false;
		}
		sourceGstin = $("#"+parentTr+" select[class='txnBranches']").children(":selected").attr("id");
		if(sourceGstin === null || sourceGstin === ""){
			swal("Invalid Branch's GSTIN", "Please select valid Branch.", "error");
			enableTransactionButtons();
			return false;
		}
		destinGstin = $("#"+parentTr+" select[class='placeOfSply txnDestGstinCls']").val();
		if(txnForUnavailableCustomer !== null && txnForUnavailableCustomer !== ""){
			itemItemsList = getTransactionItemsList("multipleItemsTablercafcc");
			txnJsonData.customerItems = itemItemsList;  //user in case of walkin customer
			txnWalkinCustomerType = $("#"+parentTr+" select[class='walkinCustType']").val();
			if(txnWalkinCustomerType == "" || txnWalkinCustomerType == null){
				swal("Invalid Type of Customer!", "Please select valid Type of customer.", "error");
				enableTransactionButtons();
				return false;
			}else if(txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2"){
				var returnValue = setWalkinCustomerdetail(txnJsonData);
				if(!returnValue){
					enableTransactionButtons();
					return returnValue;
				}
				destinGstin = $("#"+parentTr+" input[class='placeOfSplyTextHid']").val();
			}else if(txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4"){
				destinGstin = sourceGstin;
				txnJsonData.customerfutPayAlwd = "1";
				txnJsonData.customerLocation = $("#"+parentTr+" select[class='txnBranches'] option:selected").text();
			}else if(txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6"){
				destinGstin = $("#"+parentTr+" select[name='txnWalkinPlcSplySelect']").val();
				txnJsonData.customerfutPayAlwd = "1";
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
		}

		if(txnTypeOfSupply != "3" && destinGstin == ""){
			swal("Invalid Place of Supply!", "Please provide valid Place of Supply.", "error");
			enableTransactionButtons();
			return false;
		}
	}

	let isValid = validateMultiItemsTranRec("multipleItemsTablercafcc");
	if(!isValid){
		swal("Incomplete transaction data!", "Please provide complete transaction details befor submitting for accounting", "error");
		enableTransactionButtons();
		return false;
	}
	let txnForBranch = $("#rcafccTxnForBranches option:selected").val();
	let txnForItem = convertTableDataToArray("multipleItemsTablercafcc");
	let creditCustomer= $("#rcafccCustomers option:selected").val();
	txnForUnavailableCustomer= $("#rcafccWalkInCustomers").val();
	let purposeOfAdvance= $("#rcafccadvancepurpose").val();
	let withHoldingTaxReceipt = $("#rcafccTaxAdjusted").val();
	let txnRemarks= $("#rcafccRemarks").val();
	let supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	let supportingDoc = supportingDocTmp.join(',');
	if((creditCustomer=="" && txnForUnavailableCustomer=="")){
		swal("Incomplete transaction data!", "Please provide complete transaction details before submitting for accounting", "error");
		enableTransactionButtons();
		return true;
	}
	txnJsonData.txnPurpose = whatYouWantToDo;
	txnJsonData.txnPurposeVal = whatYouWantToDoVal;
	txnJsonData.txnforbranch = txnForBranch;
	txnJsonData.txnforitem = txnForItem;
	txnJsonData.txnRCAFCCCreditCustomer = creditCustomer;
	txnJsonData.txnforunavailablecustomer = txnForUnavailableCustomer;
	txnJsonData.txnRCAFCCPurposeOfAdvance = purposeOfAdvance;
	txnJsonData.txnRCAFCCWithHoldingTaxReceipt = withHoldingTaxReceipt;
	let returnVal = getReceiptPaymentDetails(txnJsonData, parentTr);
	if(returnVal === false){
		return false;
	}
	txnJsonData.txnremarks = txnRemarks;
	txnJsonData.supportingdoc = supportingDoc;
	txnJsonData.useremail = $("#hiddenuseremail").text();
	txnJsonData.txnSourceGstin = sourceGstin;
	txnJsonData.txnDestinGstin = destinGstin;
	txnJsonData.txnTypeOfSupply = txnTypeOfSupply;
	txnJsonData.txnWithWithoutTax = txnWithWithoutTax;
	txnJsonData.txnWalkinCustomerType = txnWalkinCustomerType;
	let url="/transaction/submitForAccounting";
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
			if(typeof data.recAdvFromCustCOAType != 'undefined' && data.recAdvFromCustCOAType == 0){
	   			swal("COA: mapping missing!", "Please make necessary mapping in Chart of accounts - Is this where you classify advance received from customers/debtors", "error");
	   			disableTransactionButtons();
	   			return false;
   			}
			if(typeof data.tdsReceivableSpecific != 'undefined' && data.tdsReceivableSpecific == 0){
	   			swal("COA: mapping missing!", "Chart of Account, TDS receivable mapping is not defined, please define and try.", "error");
	   			disableTransactionButtons();
	   			return false;
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
			if(typeof data.message !=='undefined' && data.message != ""){
				swal("Error!", data.message, "error");
				return false;
			}
			cancel();
			viewTransactionData(data); // to render the updated transaction recored
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			} else if(xhr.status == 500){
				swal("Error on Submit For Accounting!", "Please retry, if problem persists contact support team", "error");
			}
		},
		complete: function(data) {
			$.unblockUI();
			enableTransactionButtons();
		}
	});
}

var validateMultiItemsTranRec = function(tableName){
	var isvalid = true;
	$("#" + tableName + " > tbody > tr").each(function() {
		var itemId = $(this).find("td .txnItems option:selected").val();
		var grossAmt = $(this).find("td input[class='txnGross']").val();
		var advanceReceived = $(this).find("td input[name='advanceReceived']").val();
		var resultantAdvance = $(this).find("td input[class='netAmountVal']").val();
		if(itemId != "" && typeof itemId!='undefined' && grossAmt != "" && typeof grossAmt!='undefined' ){
			var txnGross = $(this).find("td input[class='txnGross']").val();
			if(txnGross=="" || parseFloat(txnGross) == 0.0 || advanceReceived=="" || parseFloat(advanceReceived) == 0.0 || resultantAdvance =="" || parseFloat(resultantAdvance) == 0.0){
				isvalid = false;
				return isvalid;
			}
		}
	});
	return isvalid;
}

var showAdvReceiptPopup = function(elem){
/*	$("#messageModal").html("");
	var formName=$(elem).attr('id');
	var exportType=formName.substring(0, 4);
	var transactionId=formName.substring(4, formName.length);
*/
	var formName=$(elem).parent().attr('name');
	var transactionId=formName.substring(11, formName.length);

	$("#staticAdvReceiptPopup").attr('data-toggle', 'modal');
	$("#staticAdvReceiptPopup").modal('show');
	$('#staticAdvReceiptPopup input[id="txnEntityID"]').val(formName);
}

$(document).ready(function() {
	$("#generateReceiptBtn").click(function() {
		$("#messageModal").html("");
		var formName=$('#staticAdvReceiptPopup input[id="txnEntityID"]').val();
		var exportType= "pdf";
		var transactionId=formName.substring(11, formName.length);
		var entitycustid=$("#custEntityID").val();
		var invoiceTerms = $("#staticAdvReceiptPopup textarea[id='invoiceTerms']").val();
		var dateofgoodsremove = $("#staticAdvReceiptPopup input[id='datetimeofsupplyTxt']").val();
		var ecomGstin1 = $("#staticAdvReceiptPopup input[id='ecomGstin1']").val();
		var ecomGstin2 = $("#staticAdvReceiptPopup input[id='ecomGstin2']").val();
		var destCountry = $("#staticAdvReceiptPopup div[class='bfh-selectbox bfh-countries'] span[class='bfh-selectbox-option']").text();
		var destCurrencyCode = $("#staticAdvReceiptPopup input[id='destCurrencyCode']").val();
		var currencyRate = $("#staticAdvReceiptPopup input[id='currencyRate']").val();
		var referenceNumber = $("#staticAdvReceiptPopup input[id='referenceNo']").val();
		var digitalSignatureContent = $("#digitalSignatureTextReceiveAdv").val();
		var useremail=$("#hiddenuseremail").text();
		var jsonData = {};
		jsonData.email = useremail;
		jsonData.entityTxnId=transactionId;
		jsonData.datetimeOfShipping = "";
		jsonData.transportMode = "";
		jsonData.invoiceVehicleDetail = "";
		jsonData.invoiceTerms = invoiceTerms;
		jsonData.dateofgoodsremove = dateofgoodsremove;
		jsonData.numgoodsremove = referenceNumber;
		jsonData.ecomGstin1 = ecomGstin1;
		jsonData.ecomGstin2 = ecomGstin2;
		jsonData.destCountry = destCountry;
		jsonData.destCurrencyCode = destCurrencyCode;
		jsonData.currencyRate = currencyRate;
		jsonData.exportType = exportType;
		jsonData.digitalSignatureContent = digitalSignatureContent;
		var url="/exportReceiptPdf";
		downloadFile(url, "POST", jsonData, "Error on generating receipt!");
		$("#closeReceiptBtn").click();
	});
});

