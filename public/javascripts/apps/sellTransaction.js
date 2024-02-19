var nowDate = new Date();
var todayDate = new Date(nowDate.getFullYear(), nowDate.getMonth(), nowDate.getDate(), 0, 0, 0, 0);

$(document).ready(function(){
	$("#datetimeofsupply").datetimepicker({
        format: 'DD-MMM-YYYY HH:mm:ss',
        useCurrent: true
        //minDate: todayDate
	});

	$(".popupdate").datetimepicker({
        format: 'DD-MMM-YYYY',
        useCurrent: true
	});
});

$('input[type="text"]').focus(function(){
    $(this).on("mouseup.a keyup.a", function(e){
        $(this).off("mouseup.a keyup.a").select();
    });
});

var submitForApprovalSell = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	var performaInvoice=false; var sourceGstin=""; var destinGstin="";
	var txnForUnavailableCustomer=$("#"+parentTr+" input[name='unAvailableCustomer']").val();
	var txnTypeOfSupply = ""; var txnWalkinCustomerType =""; var txnWithWithoutTax = "";
	if (GST_COUNTRY_CODE !== "" && GST_COUNTRY_CODE !== undefined && GST_COUNTRY_CODE !== null) {
		txnTypeOfSupply = $("#"+parentTr+" select[class='txnTypeOfSupply']").val();
		if(txnTypeOfSupply == "" || txnTypeOfSupply == null){
			swal("Invalid Type of Supply!", "Please select valid Type of Supply.", "error");
			return false;
		}
		sourceGstin = $("#"+parentTr+" select[class='txnBranches']").children(":selected").attr("id");
		if(sourceGstin === null || sourceGstin === ""){
			swal("Invalid Branch's GSTIN on submit for approval.", "Please select valid Branch.", "error");
			enableTransactionButtons();
			return false;
		}
		destinGstin = $("#"+parentTr+" select[class='placeOfSply txnDestGstinCls']").val();
		if(txnForUnavailableCustomer !== null && txnForUnavailableCustomer !== ""){
			txnWalkinCustomerType = $("#"+parentTr+" select[class='walkinCustType']").val();
			if(txnWalkinCustomerType == "" || txnWalkinCustomerType == null){
				swal("Invalid Type of Customer!", "Please select valid Type of customer.", "error");
				return false;
			}else if(txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2"){
				var txnWaklinGstinCode1 = $("#staticWalkinCustomerModal input[name='gstinPart1']").val();
				var txnWaklinGstinCode2 = $("#staticWalkinCustomerModal input[name='gstinPart2']").val();
				destinGstin = txnWaklinGstinCode1+txnWaklinGstinCode2;
			}else if(txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4"){
				destinGstin = sourceGstin;
			}else if(txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6"){
				destinGstin = $("#"+parentTr+" select[name='txnWalkinPlcSplySelect']").val();
			}

			if((destinGstin.length > 1 && destinGstin.length < 15) && (txnWalkinCustomerType != "5" || txnWalkinCustomerType != "6")){
				swal("Invalid Place of Supply", "Please provide valid Place of Supply.", "error");
				enableTransactionButtons();
				return false;
			}
		}

		if(txnTypeOfSupply != "3" && (destinGstin === null || destinGstin === "")){
			swal("Invalid Place of Supply!", "Please provide valid Place of Supply.", "error");
			enableTransactionButtons();
			return false;
		}
	}

	var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
	var txnForCustomer = $("#"+parentTr+" .masterList option:selected").val();
	var txnPoReference =  $("#"+parentTr+" input[class='txnPoReference']").val();
	var txnNetAmount = $("#"+parentTr+" input[class='netAmountValTotal']").val();
	var txnDocRefNo = $("#txnDocRefNo").val();
	var txnRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClass']").val();
    var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
        if($(this).val() != ""){
            return $(this).val();
        }
    }).get();
    var supportingDoc = supportingDocTmp.join(',');
	var txnCustomerDiscountAvailable; var txnForItem;
	if(whatYouWantToDoVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW){
		var isValid = validateMultiItemsTransaction("multipleItemsTablesoccpn");
		if(!isValid){
			swal("Incomplete transaction data!", "Please provide complete transaction details befor submitting for accounting", "error");
			enableTransactionButtons();
			return true;
		}
		txnForItem = convertTableDataToArray("multipleItemsTablesoccpn");
		txnCustomerDiscountAvailable = $("#soccpndiscountavailable").text();
	}else{
		var isValid = validateMultiItemsTransaction("multipleItemsTablesoccpl");
		if(!isValid){
			swal("Incomplete transaction data!", "Please provide complete transaction details befor submitting for accounting", "error");
			enableTransactionButtons();
			return true;
		}
		txnForItem = convertTableDataToArray("multipleItemsTablesoccpl");
		txnCustomerDiscountAvailable = $("#soccpldiscountavailable").text();
	}

	//var netAmountTotalWithDecimalValue = calcuateTotalNetForMultipleItemsTable(this);
	var txnTotalInvoiceValue = $("#"+parentTr+" input[class='totalInvoiceValue']").val();
	if((txnNetAmount==""  || parseFloat(txnNetAmount) == 0.0) && (txnTotalInvoiceValue == "" || parseFloat(txnTotalInvoiceValue) ==0.0)){
		swal("Incomplete Transaction detail!", "Please provide complete transaction details before submitting for approval.", "error");
		enableTransactionButtons();
		return true;
	}
	var klmandatoryfollowednotfollowed;
    var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
    if(typeof followedkl!='undefined'){
        if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
            klmandatoryfollowednotfollowed="1";
        }
        if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
            klmandatoryfollowednotfollowed="0";
        }
        var klfollowednotfollowed=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").is(':checked');
        if(klfollowednotfollowed==false){
            swal("Transaction knowledge library!", "Please follow mandatory knowledge library before submitting transaction for accounting", error);
            enableTransactionButtons();
            return false;
        }
    }
    txnWithWithoutTax = $("#"+parentTr+" select[class='txnWithWithoutTaxCls']").val();
	if((txnTypeOfSupply =="3" || txnTypeOfSupply =="4" || txnTypeOfSupply =="5") && txnWithWithoutTax==""){
		swal("Incomplete transaction detail!", "Please select With / Without IGST.", "error");
		enableTransactionButtons();
		returnValue=false;
    }
	var txnDate=$("#"+parentTr).find(".txnBackDate").val();
	var txnEntityID = $("#"+parentTr).attr("name");
	var txnJsonData={};
	txnJsonData.txnEntityID = txnEntityID;
	txnJsonData.txnPurpose=whatYouWantToDo;
	txnJsonData.txnPurposeVal=whatYouWantToDoVal;
	txnJsonData.txnforbranch=txnForBranch;
	txnJsonData.txnforproject=txnForProject;
	txnJsonData.txnforitem=txnForItem;
	txnJsonData.txnforcustomer=txnForCustomer;
	txnJsonData.performaInvoice=performaInvoice;
	txnJsonData.txnforunavailablecustomer=txnForUnavailableCustomer;
	txnJsonData.txnPoReference=txnPoReference;
	txnJsonData.txncustomerdiscountavailable=txnCustomerDiscountAvailable;
	//txnJsonData.netAmountTotalWithDecimalValue = netAmountTotalWithDecimalValue;
	txnJsonData.txnnetamount=txnNetAmount;
	txnJsonData.txnremarks=txnRemarks;
	txnJsonData.supportingdoc = supportingDoc;
	txnJsonData.useremail=$("#hiddenuseremail").text();
	txnJsonData.txnSourceGstin = sourceGstin;
	txnJsonData.txnDestinGstin = destinGstin;
	txnJsonData.txnTypeOfSupply = txnTypeOfSupply;
	txnJsonData.txnWithWithoutTax = txnWithWithoutTax;
    txnJsonData.klfollowednotfollowed = klmandatoryfollowednotfollowed;
    txnJsonData.txnDate = txnDate;
    txnJsonData.txnDocRefNo = txnDocRefNo;
    if(whatYouWantToDoVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
    	var returnVal = getReceiptPaymentDetails(txnJsonData, parentTr);
    	if(returnVal === false){
    		return false;
    	}
    }

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
	    		swal("Error on Submit For Approval!", "Please retry, if problem persists contact support team.", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
			enableTransactionButtons();
		}
	});
}

var submitForApprovalSalesReturns = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	var transactionInvoiceId=$("#srtfccInvoices option:selected").val(); //original sell on credit trans id
	var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
	var txnForCustomer = $("#"+parentTr+" .masterList option:selected").val();
	var txnNetAmount = $("#"+parentTr+" input[class='netAmountValTotal']").val();
	var txnRemarks =  $("#"+parentTr+" textarea[class='srtfccRemarks']").val();

	if(transactionInvoiceId=="" || txnNetAmount==""){
		swal("Incomplete tranction data!", "Please provide complete sales return transaction details before submitting for approval", "error");
		enableTransactionButtons();
		return true;
	}else{
		var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
		if(typeof followedkl!='undefined'){
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedyes']").is(':checked')==true){
				klmandatoryfollowednotfollowed="1";
			}
			if($("#"+parentTr+" div[class='klBranchSpecfTd'] input[id='klfollowedno']").is(':checked')==true){
				klmandatoryfollowednotfollowed="0";
			}
		}
		var userCheckRule=checkForOnlyConfiguredApproverPVS(transactionInvoiceId,txnNetAmount,"salesreturn");
		if(userCheckRule==true){
			proceed=true;
		}
		if(userCheckRule==false){
			proceed=false;
		}
		if(proceed){
			var txnCustomerDiscountAvailable; var txnForItem;
			txnForItem = convertTableDataToArray("multipleItemsTablesrtfcc");
            var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
                if($(this).val() != ""){
                    return $(this).val();
                }
            }).get();
            var supportingDoc = supportingDocTmp.join(',');

			//var netAmountTotalWithDecimalValue = calcuateTotalNetForMultipleItemsTable(this);
			var txnEntityID = $("#"+parentTr).attr("name");
			var txnJsonData={};
			txnJsonData.txnEntityID = txnEntityID;
			txnJsonData.transactionInvoiceId = transactionInvoiceId;
			txnJsonData.txnPurpose=whatYouWantToDo;
			txnJsonData.txnPurposeVal=whatYouWantToDoVal;
			txnJsonData.txnforbranch=txnForBranch;
			txnJsonData.txnforproject=txnForProject;
			txnJsonData.txnforitem=txnForItem;
			txnJsonData.txnforcustomer=txnForCustomer;
			//txnJsonData.netAmountTotalWithDecimalValue = netAmountTotalWithDecimalValue;
			txnJsonData.txnnetamount=txnNetAmount;
			txnJsonData.txnremarks=txnRemarks;
			txnJsonData.supportingdoc = supportingDoc;
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
					viewTransactionData(data); // to render the updated transaction recored
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout();
					}else if(xhr.status == 500){
			    		swal("Error on Submit For Approval!", "Please retry, if problem persists contact support team.", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
					enableTransactionButtons();
				}
			});
		}
	}
}

var submitForAccountingSell = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	var txnForBranch="";var txnForProject="";var txnForItem="";var txnForCustomer="";"";
	var txnNoOfUnits=""; var txnPricePerUnit="";var txnGross="";var txnCustomerAdvanceIfAny="";var txnCustomerAdvanceAdjustment="";
	var txnCustomerDiscountAvailable="";var txnNetAmount="";var txnNetAmountDescription="";var txnReceiptDetails="";
	var klmandatoryfollowednotfollowed="";var userTxnBudAmountLimit="";
	var txnReceiptTypeBankDetails="";var txnRemarks="";var performaInvoice=false;
	var individualTaxComponents="";var individualTaxFormulaComponents="";var receiptPaymentBank="";
	var txnFrieghtCharges=""; var txnPoReference=""; var sourceGstin = ""; var destinGstin ="";
	var txnDocRefNo = "";
	var txnForUnavailableCustomer=$("#"+parentTr+" input[name='unAvailableCustomer']").val();
	var txnTypeOfSupply = ""; var txnWalkinCustomerType =""; var txnWithWithoutTax = "";
	var txnJsonData={};
	var txnDate=$("#"+parentTr).find(".txnBackDate").val();

	if (GST_COUNTRY_CODE !== "" && GST_COUNTRY_CODE !== undefined && GST_COUNTRY_CODE !== null) {
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
			returnValue=false;
		}
		sourceGstin = $("#"+parentTr+" select[class='txnBranches']").children(":selected").attr("id");
		if(sourceGstin === null || sourceGstin === ""){
			swal("Invalid Branch's GSTIN on Submit for Accounting.", "Please select valid Branch.", "error");
			enableTransactionButtons();
			return false;
		}
		destinGstin = $("#"+parentTr+" select[class='placeOfSply txnDestGstinCls']").val();
		if(txnForUnavailableCustomer !== null && txnForUnavailableCustomer !== ""){
			itemItemsList = getTransactionItemsList("multipleItemsTablesoccpn");
			txnJsonData.customerItems = itemItemsList;  //user in case of walkin customer
			txnWalkinCustomerType = $("#"+parentTr+" select[class='walkinCustType']").val();
			if(txnWalkinCustomerType == "" || txnWalkinCustomerType == null){
				swal("Invalid Type of Customer!", "Please select valid Type of customer.", "error");
				enableTransactionButtons();
				return false;
			}else if(txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2"){
				destinGstin = $("#"+parentTr+" input[class='placeOfSplyTextHid']").val();
				var returnValue = setWalkinCustomerdetail(txnJsonData);
				if(!returnValue){
					enableTransactionButtons();
					return returnValue;
				}
			}else if(txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4"){
				destinGstin = sourceGstin;
				txnJsonData.customerfutPayAlwd = "1";
				txnJsonData.customerLocation = $("#soccpnTxnForBranches option:selected").val();
			}else if(txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6"){
				destinGstin = $("#"+parentTr+" select[name='txnWalkinPlcSplySelect']").val();
				txnJsonData.customerStateCode = $("#"+parentTr+" select[name='txnWalkinPlcSplySelect']").val();
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

		if(txnTypeOfSupply != "3" && (destinGstin === null || destinGstin === "")){
			swal("Invalid Place of Supply!", "Please provide valid Place of Supply.", "error");
			enableTransactionButtons();
			return false;
		}
	}
    var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
        if($(this).val() != ""){
            return $(this).val();
        }
    }).get();
    var supportingDoc = supportingDocTmp.join(',');
	var itemItemsList = "";
	if(whatYouWantToDoVal==SELL_ON_CASH_COLLECT_PAYMENT_NOW){
		//txnDate= $("#backdatedDatePicker").val();
		txnForBranch=$("#soccpnTxnForBranches option:selected").val();
		txnForProject=$("#allTxnProjectPurposeData option:selected").val();
		var isValid = validateMultiItemsTransaction("multipleItemsTablesoccpn");
		if(!isValid){
			swal("Incomplete transaction data!", "Please provide complete transaction details befor submitting for accounting", "error");
			enableTransactionButtons();
			return false;
		}
		txnForItem = convertTableDataToArray("multipleItemsTablesoccpn");
		txnForCustomer=$("#soccpnCustomer option:selected").val();
		txnPoReference=$("#soccpn_po_reference").val();
		txnCustomerDiscountAvailable=$("#soccpndiscountavailable").text();
		txnNetAmount=$("#soccpnnetamntTotal").val();
//		txnReceiptDetails=$("#socpnreceiptdetail option:selected").val();
//		getReceiptPaymentDetails(txnJsonData, "socpnreceiptdetail", txnReceiptDetails);
		txnReceiptTypeBankDetails=$("#receiptBranchBankAccount").val();
		txnRemarks=$("#socpnRemarks").val();
		txnDocRefNo = $("#txnDocRefNo").val();
	}else if(whatYouWantToDoVal==SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
		//txnDate= $("#backdatedSellOnCreditDatePicker").val();
		txnForBranch=$("#soccplTxnForBranches option:selected").val();
		txnTypeOfSupply = $("#soccplTypeOfSupply option:selected").val();
		var txnPlaceOfSupply =$("#soccplPlaceOfSply option:selected").val();
		txnForProject=$("#soccplTxnForProjects option:selected").val();
		var isValid = validateMultiItemsTransaction("multipleItemsTablesoccpl");
		if(!isValid){
			swal("Incomplete transaction data!", "Please provide complete transaction details befor submitting for accounting", "error");
			enableTransactionButtons();
			return true;
		}
		txnForItem = convertTableDataToArray("multipleItemsTablesoccpl");
		txnForCustomer=$("#soccplCustomer option:selected").val();
		if ($('#soccplPerformaInv').is(":checked"))	{
			performaInvoice = true;
		}
		txnPoReference=$("#soccpl_po_reference").val();
		txnCustomerDiscountAvailable=$("#soccpldiscountavailable").text();
		txnNetAmount=$("#soccplnetamntTotal").val();
		txnNetAmountDescription=$("#socplnetAmountLabel").text();
		var creditExceeded = checkIfCustomerCreditLimitExceeded(txnForCustomer,txnNetAmount,txnForItem,txnForBranch,txnTypeOfSupply,txnPlaceOfSupply);
		if(creditExceeded === false){
			swal("Sorry, customer's credit limit has been exceeded, you can sell to this customer only on cash.", "");
			return false;
		}
		//txnReceiptDetails=$("#socplreceiptdetail option:selected").val();
		//txnReceiptTypeBankDetails=$("#socplreceiptBranchBankAccount").val();
		txnRemarks=$("#socplRemarks").val();
		txnDocRefNo = $("#txnDocRefNo").val();
	}
	var txnBomTxnRef = $("#"+parentTr+" select[class='salesExpenseTxns'] option:selected").attr('txnRefNo');
    var parentDiv = $("#"+parentTr).closest('div').attr('id');
    var klfollowednotfollowed = isKnowledgeLibFollowedInMultiItems(parentDiv);
	//var netAmountTotalWithDecimalValue = calcuateTotalNetForMultipleItemsTable(this);
	var txnTotalInvoiceValue = $("#"+parentTr+" input[class='totalInvoiceValue']").val();
	if((txnNetAmount==""  || parseFloat(txnNetAmount) == 0.0) && (txnTotalInvoiceValue == "" || parseFloat(txnTotalInvoiceValue) ==0.0)){
		swal("Error on Submit for accounting!", "Please provide complete transaction details before submitting for accounting", "error");
		enableTransactionButtons();
		return true;
	}
	var txnEntityID = $("#"+parentTr).attr("name");
	txnJsonData.txnEntityID = txnEntityID;
	txnJsonData.txnPurpose=whatYouWantToDo;
	txnJsonData.txnPurposeVal=whatYouWantToDoVal;
	txnJsonData.txnforbranch=txnForBranch;
	txnJsonData.txnforproject=txnForProject;
	txnJsonData.txnforitem=txnForItem;
	txnJsonData.txnforcustomer=txnForCustomer;
	txnJsonData.performaInvoice=performaInvoice;
	txnJsonData.txnforunavailablecustomer=txnForUnavailableCustomer;
	txnJsonData.txnPoReference=txnPoReference;
	txnJsonData.txnBomTxnRef=txnBomTxnRef;
	txnJsonData.txncustomerdiscountavailable=txnCustomerDiscountAvailable;
	//txnJsonData.netAmountTotalWithDecimalValue = netAmountTotalWithDecimalValue;
	txnJsonData.txnnetamount=txnNetAmount;
//	txnJsonData.txnreceiptdetails=txnReceiptDetails;
	txnJsonData.txnSourceGstin = sourceGstin;
	txnJsonData.txnDestinGstin = destinGstin;
	txnJsonData.txnTypeOfSupply = txnTypeOfSupply;
	txnJsonData.txnWithWithoutTax = txnWithWithoutTax;
	txnJsonData.txnWalkinCustomerType = txnWalkinCustomerType;
	txnJsonData.txnDate=txnDate;
	var returnVal = getReceiptPaymentDetails(txnJsonData, parentTr);
	if(returnVal === false){
		return false;
   	}
	txnJsonData.txnremarks=txnRemarks;
	txnJsonData.supportingdoc = supportingDoc;
    txnJsonData.klfollowednotfollowed = klfollowednotfollowed;
    txnJsonData.txnDocRefNo=txnDocRefNo;
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
			if(whatYouWantToDoVal==SELL_ON_CASH_COLLECT_PAYMENT_NOW && txnReceiptDetails==2){
				if(data.resultantAmount < 0){
					if(data.branchBankDetailEntered === false){
						swal("Insufficient balance in the bank account!", "Use alternative payment mode or infuse funds into the bank account. Current bank balance is:"+data.resultantAmount, "error");
						disableTransactionButtons();
						return false;
					}else{
						swal("Bank Balance is in negative!", "This bank account type allows -ve balance so transaction is successful. Note current bank balance is:"+data.resultantAmount, "warning");
					}
				}
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

function checkStockAvailableForSellTran(elem){
	if(txnPurposeVal == CREDIT_NOTE_CUSTOMER) {
		 return false;
	}
	var parentTr = $(elem).closest('tr').attr('id');
	var parentTableId =$(elem).closest('table').attr('id');
	var inputQty=$("#"+parentTr+" input[class='txnNoOfUnit']").val();
	var txnitemSpecifics=$("#"+parentTr+" .txnItems").val();
	var isBackDated = $("#isBackDatedTxn").val();
	var txnDate = $("#"+parentTr).find(".txnBackDate").val();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	var txnForItem="";
	if(txnPurposeVal==SELL_ON_CASH_COLLECT_PAYMENT_NOW){
		txnForItem = convertTableDataToArrayForStockCheck("multipleItemsTablesoccpn");
	}else if(txnPurposeVal==SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
		txnForItem = convertTableDataToArrayForStockCheck("multipleItemsTablesoccpl");
	}else if(txnPurposeVal==TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
        txnForItem = convertTableDataToArrayForStockCheck("multipleItemsTabletifbtb");
	}else if(txnPurposeVal == DEBIT_NOTE_CUSTOMER){
        txnForItem = convertTableDataToArrayForStockCheck("multipleItemsTablecdtdbt");
        var oldItemQty=$("#"+parentTr+" input[class='txnNoOfUnitHid']").val();

    }
	if(txnPurposeVal == BUY_ON_CASH_PAY_RIGHT_AWAY || txnPurposeVal == BUY_ON_CREDIT_PAY_LATER || txnPurposeVal == BUY_ON_PETTY_CASH_ACCOUNT || txnPurposeVal==SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal==SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == DEBIT_NOTE_CUSTOMER
		|| txnPurposeVal == CREATE_PURCHASE_REQUISITION || txnPurposeVal == CREATE_PURCHASE_ORDER || txnPurposeVal == MATERIAL_ISSUE_NOTE){
		parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
	}else{
		parentOfparentTr = $(elem).parents().closest('tr').attr('id');
	}
	var txnBranch=$("#"+parentOfparentTr+" select[class='txnBranches']").val();
	var jsonData={};
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.incomeSpecificsId=txnitemSpecifics;
	jsonData.branchId=txnBranch;
	jsonData.inputQty=inputQty;
	jsonData.txnForItem=txnForItem;
	jsonData.txnDate = txnDate;
	jsonData.isBackDated = isBackDated;
	//var url="/specifics/branchIncomeAvailableStock";
	var url="/specifics/branchSellStockAvailableCombSales";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		contentType : 'application/json',
		async: false,
		success     : function (data) {
			if(data.result){
				for(var i=0; i<data.incomeStockData.length;i++){
					if(data.incomeStockData[i].isStockAvailable=="false"){
						if(txnPurposeVal == DEBIT_NOTE_CUSTOMER) {
							var reqUnits = parseFloat(inputQty) - parseFloat(oldItemQty);
							var availableUnits = parseFloat(data.incomeStockData[i].stockAvailable);
							if(reqUnits > availableUnits) {
								swal("Invalid Inventory!", "You Are Selling An Inventory Item.The Stock Available Of this item is " +(availableUnits + parseFloat(oldItemQty))+ ". Item is out of stock. You can create the stock for this item in order to sell for this much quantity.", "warning");
								$("#"+parentTr+" input[class='txnNoOfUnit']").val("0");
							}
						}else if(txnPurposeVal == CREATE_PURCHASE_REQUISITION){
							$("#"+parentTr+" input[class='txnInStockUnit']").val(data.incomeStockData[i].stockAvailable);
						}else {
						swal("Invalid Inventory!", "You Are Selling An Inventory Item.The Stock Available Of this item is " +data.incomeStockData[i].stockAvailable+ ". Item is out of stock. You can create the stock for this item in order to sell for this much quantity.", "warning");
						$("#"+parentTr+" input[class='txnNoOfUnit']").val("0");
					}

					}
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
				swal("Error on fetching item stock!", "Please retry, if problem persists contact support team", "error");
			}
		},
		complete: function(data) {
			$.unblockUI();
		}

	});
}

function convertTableDataToArrayForStockCheck(tableName){
	var parentTable = $("#"+tableName).parents().closest('table').attr('id');
	var multipleItemsData = [];
	$("#" + tableName + " > tbody > tr").each(function() {
		var jsonData = {};
		var itemId = $(this).find("td .txnItems option:selected").val();
		if(itemId != "" && typeof itemId!='undefined' ){
			jsonData.txnItemID = $(this).attr('name');
			jsonData.txnItems = itemId;
			jsonData.txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
			jsonData.txnItemTableIdHid = $(this).find("td input[class='txnItemTableIdHid']").val();
			multipleItemsData.push(JSON.stringify(jsonData));
		}
	});
	return multipleItemsData;
}

function calculateNetAmountWhenAdvanceChanged(elem){
	var parentTr = $(elem).closest('tr').attr('id');
    var parentOfparentTr = $(elem).parents().closest('tr').attr('id');
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
    if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
        parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
	}
	if(txnPurposeVal==SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
        var netAmtLabelVal = $("#"+parentTr+" div[class='netAmountDescriptionDisplay']").text();
        $("#"+parentTr+" div[class='netAmountDescriptionDisplay']").text('');
        var adjustmentAmount=$("#"+parentTr+" input[class='howMuchAdvance']").val();
		if(adjustmentAmount ==""){
			adjustmentAmount = 0;
		}
		var invoiceValue=$("#"+parentTr+" input[class='invoiceValue']").val();
		var str_array = netAmtLabelVal.split(',');
		for(var i = 0; i < str_array.length; i++) {
			if(str_array[i].indexOf("Adjustment:") <= -1 && str_array[i] != ""){
                $("#"+parentTr+" div[class='netAmountDescriptionDisplay']").append(str_array[i] + ",<br/>");
			}
		}
		var netAmount=0;
		if(invoiceValue!=""){
			netAmount = parseFloat(invoiceValue);
		}
		if(adjustmentAmount!=""){
            netAmount=(parseFloat(netAmount)-parseFloat(adjustmentAmount));
            $("#"+parentTr+" div[class='netAmountDescriptionDisplay']").append('Adjustment:'+adjustmentAmount);
		}
		$("#"+parentTr+" input[class='netAmountVal']").val((netAmount*1).toFixed(2));
		var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
		//$("#soccpnnetamntTotal").val(Math.round(netAmountTotal).toFixed(2));
        $("#"+parentOfparentTr+" input[class='netAmountValTotal']").val(Math.round(netAmountTotal).toFixed(2));

	}else if(txnPurposeVal == DEBIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER){
        var adjustmentAmount=$("#"+parentTr+" input[class='howMuchAdvance']").val();
        if(adjustmentAmount==""){
            adjustmentAmount = 0;
        }
        var netAmtLabelVal = $("#"+parentOfparentTr+" div[class='netAmountDescriptionDisplay']").text();
        $("#"+parentOfparentTr+" div[class='netAmountDescriptionDisplay']").text("");
        var discount = $("#"+parentTr+" input[class='txnDiscountPercentHid']").val();
        var gross = $("#"+parentTr+" input[class='txnGross']").val();
        var invoiceValue=$("#"+parentTr+" input[class='invoiceValue']").val();
        var str_array = netAmtLabelVal.split(',');
        for(var i = 0; i < str_array.length; i++) {
            if(str_array[i].indexOf("Adjustment:") <= -1 && str_array[i] != ""){
                $("#"+parentOfparentTr+" div[class='netAmountDescriptionDisplay']").append(str_array[i] + ",<br/>");
            }
        }
        var netAmount=0;
        if(invoiceValue!=""){
            netAmount = parseFloat(invoiceValue);
        }
        if(adjustmentAmount!=""){
            netAmount=(parseFloat(netAmount)-parseFloat(adjustmentAmount));
            $("#"+parentOfparentTr+" div[class='netAmountDescriptionDisplay']").append('Adjustment:'+adjustmentAmount);
        }
        $("#"+parentTr+" input[class='netAmountVal']").val((netAmount*1).toFixed(2));
        var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
        $("#"+parentOfparentTr+" input[class='netAmountValTotal']").val(Math.round(netAmountTotal));
    }
	return true;
}

/* Discount Percentage on the configured amount for the walkin customer starts*/
function placeDiscountIntoDiscoutBTag(elem){
	var value=$(elem).val();
	if(value!=""){
		var branchPrimaryKeyId=$("#soccpnTxnForBranches option:selected").val();
		var specificsPrimaryKeyId=$("#soccpnItem option:selected").val();
		if(branchPrimaryKeyId!="" && specificsPrimaryKeyId!=""){
			var registeredCustomerValue=$("#soccpnCustomer option:selected").val();
			var unregisteredWalkinCustomer=$("#soccpnUnAvailableCustomer").val();
			if(registeredCustomerValue!="" && value!=""){
				swal("Error!","Sorry You Do not Have Authority To Give Discount For the Item You Sell To Registered Customer.","error");
				$(elem).val("");
				return true;
			}else{
				var json={};
				json.email=$("#hiddenuseremail").text();
				json.bnchPrimaryKeyId=branchPrimaryKeyId;
				json.specfPrimaryKeyId=specificsPrimaryKeyId;
				json.enteredDiscountValue=value;
				ajaxCall('/sell/checkMaxDiscountForWalkinCust', json, '', '', '', '', 'getMaxDiscForWalkinCustSuccess', '', false);
			}
		}else{
			swal("Error!","Please Select Branch and Item For Which You Want To Give Discount For Customer.","error");
			$(elem).val("");
			return true;
		}
	}
}

function getMaxDiscForWalkinCustSuccess(data){
	if(data.result){
		$("#soccpndiscountavailable").text("");
		var value=$(".unavailablecustomerdiscount").val();
		$("#soccpndiscountavailable").text(value);
	}else{
		swal("Error!","Sorry,Entered discount percentage for the Walkin customer in this branch is greater than the configured Discount Percentage by the administrator "+data.maxWalikinCustomerDiscountData[0].discountPercentage+" %.So You Are Not Authorized To Give Discount.Please Do Contact Your Administrator.","error");
		$(".unavailablecustomerdiscount").val("");
		$("#soccpndiscountavailable").text("");
		return true;
	}
}
/* Discount Percentage on the configured amount for the walkin customer ends*/

/*not in use*/
function whenMasterListDontHaveData(elem){
	var parentTr=$(elem).parent().parent('tr:first').attr('id');
	var masterListSelected=$("#"+parentTr+" .masterList option:selected").val();
	if(masterListSelected!=""){
		swal("Error!","Please Choose from master list or provide manual entry","error");
		$(elem).val("");
		return true;
	}
}

var calculateDiscountSell = function(elem){
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	var parentTr = $(elem).closest('tr').attr('id');
	var noOfUnitValue=$("#"+parentTr+" input[class='txnNoOfUnit']").val();
	var unitPriceValue=$("#"+parentTr+" input[class='txnPerUnitPrice']").val();
	var discount = $("#"+parentTr+" input[class='txnDiscountPercent']").val();
	var orgDiscount = $("#"+parentTr+" input[class='txnDiscountPercentHid']").val();
	if(parseFloat(discount) > parseFloat(orgDiscount)){
		swal("Invalid data!", "Max applicable discount: " + orgDiscount +" %", "error");
		$("#"+parentTr+" input[class='txnDiscountPercent']").val(orgDiscount);
		return false;
	}
	if(parseFloat(discount) < 0 || parseFloat(discount) > 100){
		swal("Invalid data!", "Wrong discount value!", "error");
		$("#"+parentTr+" input[class='txnDiscountPercent']").val(orgDiscount);
		return false;
	}
	if(noOfUnitValue!="" && unitPriceValue!="" && discount != ""){
		var totalPrice = parseFloat(noOfUnitValue)*parseFloat(unitPriceValue);
		var discountAmount=((discount/100.0)*totalPrice).toFixed(2);
		var increaseDecrease = "";
        if(txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER) {
            increaseDecrease = $("#creditDebitTxnDiv select[class='creditDebitType'] option:selected").val();
        }
		//var mainTableTr =$(elem).parent().parent().parent().parent().closest('tr').attr('id');
		$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").append('Discount('+discount+'%):'+discountAmount + ', ');
		if(txnPurposeVal == CREDIT_NOTE_CUSTOMER){
        	if(increaseDecrease == "1"){
                var unitPriceValueOrg = $("#"+parentTr+" input[class='txnPerUnitPriceHid']").val();
                var revisedPrice = parseFloat(unitPriceValueOrg) - parseFloat(unitPriceValue);
                if(parseFloat(revisedPrice) > 0) {
                    totalPrice = parseFloat(noOfUnitValue)*parseFloat(revisedPrice);
                }else{
                    totalPrice = parseFloat(noOfUnitValue)*parseFloat(unitPriceValue);
                }
			}else if(increaseDecrease == "2"){
                var unitOrg = $("#"+parentTr+" input[class='txnNoOfUnitHid']").val();
                var revisedUnits = parseFloat(unitOrg) - parseFloat(noOfUnitValue);
                if(parseFloat(revisedUnits) > 0) {
                    totalPrice = parseFloat(unitPriceValue) * parseFloat(revisedUnits);
                }else{
                    totalPrice = parseFloat(unitPriceValue) * parseFloat(noOfUnitValue);
				}
            }
		}else if(txnPurposeVal == DEBIT_NOTE_CUSTOMER){
            if(increaseDecrease == "1"){
                var unitPriceValueOrg = $("#"+parentTr+" input[class='txnPerUnitPriceHid']").val();
                var revisedPrice = parseFloat(unitPriceValue) - parseFloat(unitPriceValueOrg);
                if(parseFloat(revisedPrice) > 0) {
                    totalPrice = parseFloat(noOfUnitValue)*parseFloat(revisedPrice);
                }else{
                    totalPrice = parseFloat(noOfUnitValue)*parseFloat(unitPriceValue);
                }
            }else if(increaseDecrease == "2"){
                var unitOrg = $("#"+parentTr+" input[class='txnNoOfUnitHid']").val();
                var revisedUnits = parseFloat(noOfUnitValue) - parseFloat(unitOrg);
                if(parseFloat(revisedUnits) > 0) {
                    totalPrice = parseFloat(unitPriceValue) * parseFloat(revisedUnits);
                }else{
                    totalPrice = parseFloat(unitPriceValue) * parseFloat(noOfUnitValue);
                }
            }
		}
		discountAmount=((discount/100.0)*totalPrice).toFixed(2);
		$("#"+parentTr+" input[class='txnDiscountAmount']").val(discountAmount);
	}else{
		$("#"+parentTr+" input[class='txnDiscountAmount']").val(0);
	}
}

//not in use
var generatePDFInvoiceOld = function(elem){
	$("#messageModal").html("");
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var formName=$(elem).attr('id');
	var exportType=formName.substring(0, 4);
	var transactionId=formName.substring(4, formName.length);
	var url="/customer/getshipaddress/"+transactionId;
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
			$("#staticSellTransInvoice").attr('data-toggle', 'modal');
	    	$("#staticSellTransInvoice").modal('show');
	    	$(".staticSellTransInvoiceclose").attr("href",location.hash);
	    	//$("#staticSellTransInvoice div[class='modal-body']").html("");
	    	if(data){
	    		$('#staticSellTransInvoice input[id="txnEntityID"]').val(formName);
				$('#staticSellTransInvoice input[id="custEntityID"]').val(data.custid);
				$('#staticSellTransInvoice textarea[id="shippingCustAddress"]').val(data.shippingAddress);
				var shippingcustCtry=data.shipingCountry;
				$('#staticSellTransInvoice select[id="shippingCustCountry"] option').filter(function () {return $(this).val()==shippingcustCtry;}).prop("selected", "selected");
				$('#staticSellTransInvoice input[id="shippingCustlocation"]').val(data.shippingLocation);
				var shippingcustPhnCtryCode=data.shippingvendPhnCtryCode;
				$("#staticSellTransInvoice select[id='shippingCustPhnNocountryCode'] option").filter(function () {return $(this).html()==shippingcustPhnCtryCode;}).prop("selected", "selected");
				if(typeof data.shippingPhone != 'undefined' && data.shippingPhone.length >= 10){
					$('#staticSellTransInvoice input[id="shippingCustphone1"]').val(data.shippingPhone.substring(0,3));
					$('#staticSellTransInvoice input[id="shippingCustphone2"]').val(data.shippingPhone.substring(3,6));
					$('#staticSellTransInvoice input[id="shippingCustphone3"]').val(data.shippingPhone.substring(6,10));
				}
	    	}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on fetching shipping address!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}


var generatePDFInvoice = function(elem){
	$("#messageModal").html("");
	var parentTr = $(elem).closest('tr').attr('id');
	var formName=$(elem).attr('id');
	var exportType=formName.substring(0, 4);
	var transactionId=formName.substring(4, formName.length);
	if(exportType == "popu"){
		$("#staticSellTransInvoice button[id='generateSellInvoiceBtn']").hide();
		$("#staticSellTransInvoice button[id='savePopTxnDetailBtn']").show();
        transactionId = $("#" + parentTr + " select[class='salesExpenseTxns'] option:selected").val();
	}else{
		$("#staticSellTransInvoice button[id='savePopTxnDetailBtn']").hide();
		$("#staticSellTransInvoice button[id='generateSellInvoiceBtn']").show();
	}
	$("#staticSellTransInvoice").attr('data-toggle', 'modal');
	$("#staticSellTransInvoice").modal('show');
	$(".staticSellTransInvoiceclose").attr("href",location.hash);
	$('#staticSellTransInvoice input[id="txnEntityID"]').val(formName);
    getShippingAddress4Customer(elem, transactionId);
    getAdditionalDetails(elem, transactionId);
}

var getAdditionalDetails = function(elem, entityTxnId){
	var isCompositionSchemeApply = $("#isCompositionSchemeApply").val();

	if(entityTxnId == "" || entityTxnId == null || typeof entityTxnId == 'undefined'){
        swal("Invalid Transaction id!", "Please contact support.", "error");
        return false;
    }
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.entityTxnId = entityTxnId;
    var url="/addtionalDetails";
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
        	$('#staticSellTransInvoice input[id="datetimeofsupplyTxt"]').val(data.dateTimeofSupply);
        	$('#staticSellTransInvoice input[id="invoiceTranportationMode"]').val(data.transMode);
        	$('#staticSellTransInvoice input[id="invoiceVehicleDetail"]').val(data.vehicleDetails);
        	$('#staticSellTransInvoice input[id="dateofgoodsremoveTxt"]').val(data.dateRemovalOfGoods);
        	$('#staticSellTransInvoice input[id="numgoodsremove"]').val(data.appNoGoodsRem);
        	$('#staticSellTransInvoice input[id="ecomGstin1"]').val(data.gstIneCommOp1);
        	$('#staticSellTransInvoice input[id="ecomGstin2"]').val(data.gstIneCommOp2);
        	$("#dest_countries span[class='bfh-selectbox-option']").text(data.countryName);
        	$(".bfh-currencies span[class='bfh-selectbox-option']").text(data.currCode);
        	$('#staticSellTransInvoice input[id="currencyRate"]').val(data.convRate);
        	$('#staticSellTransInvoice input[id="portCode"]').val(data.portCode);
        	$('#staticSellTransInvoice input[id="reasonForReturn"]').val(data.reasonForReturn);
        	$('#staticSellTransInvoice textarea[id="invoiceTerms"]').val(data.terms);
        	 if(isCompositionSchemeApply == 1) {
        		 $('#invoiceHeading').find('option[value="2"]').prop('selected', true);
        		 $('#invoiceHeading').prop('disabled', true);
        	 }else {
        		 $('#invoiceHeading').find('option[value="'+data.invoiceHeading+'"]').prop('selected', true);
        	 }
        		$("#digitalSignatureText").val(data.digitalSignatureContent);
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
            	doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching shipping address!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            	$.unblockUI();
        	}
    	});
    	return true;
}

var getShippingAddress4Customer = function(elem, entityTxnId){
    if(entityTxnId == "" || entityTxnId == null || typeof entityTxnId == 'undefined'){
        swal("Invalid Transaction id!", "Please contact support.", "error");
        return false;
    }
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var jsonData = {};
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.entityTxnId = entityTxnId;
    var url="/shippingAddress";
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
        		$('#staticSellTransInvoice textarea[id="shipingAddress"]').val("");
                $("#staticSellTransInvoice input[id='shipingLocation']").val("");
                $("#shipping_states span[class='bfh-selectbox-option']").text("");
            if(data){
                $('#staticSellTransInvoice textarea[id="shipingAddress"]').val(data.shippingAddress);
                $("#staticSellTransInvoice input[id='shipingLocation']").val(data.shippingLocation);
                $("#shipping_states span[class='bfh-selectbox-option']").text(data.shippingState);
            }
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
            	doLogout();
            }else if(xhr.status == 500){
                swal("Error on fetching shipping address!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
    return true;
}

$(document).ready(function() {
	$("#generateSellInvoiceBtn").click(function() {
		 var shipingAddress = $.trim($("#staticSellTransInvoice textarea[id='shipingAddress']").val());
		 var shipingLocation = $.trim($("#staticSellTransInvoice input[id='shipingLocation']").val());
   		 var shipingCountry = $.trim($("#shipping_countries span[class='bfh-selectbox-option']").text());
   		 var shipingState = $.trim($("#shipping_states span[class='bfh-selectbox-option']").text());


   		 if(shipingCountry == "") {
   		 	swal("Incomplete invoice detail!", "Please select Country Ship/Dispatch To.", "error");
   		 	return false;
   		 }

   		 if(shipingState == "") {
   		 	swal("Incomplete invoice detail!", "Please select State Ship/Dispatch To.", "error");
   		 	return false;
   		 }

   		 if(shipingLocation == "") {
   		 	swal("Incomplete invoice detail!", "Please Enter Location Ship/Dispatch To.", "error");
   		 	return false;
   		 }

   		 if(shipingAddress == "") {
   		 	swal("Incomplete invoice detail!", "Please Enter Shipping Address.", "error");
   		 	return false;
   		 }

		$("#messageModal").html("");
		var formName=$('#staticSellTransInvoice input[id="txnEntityID"]').val();
		var exportType=formName.substring(0, 4);
		var transactionId=formName.substring(4, formName.length);
		var entitycustid=$("#custEntityID").val();
		var useremail=$("#hiddenuseremail").text();
		var jsonData = {};
		setStaticSellTransInvoiceData(jsonData);
		jsonData.usermail = useremail;
		jsonData.entityTxnId=transactionId;
		jsonData.exportType = exportType;
		var url="/exportInvoicePdf";
		downloadFile(url, "POST", jsonData, "Error on invoice generation!");
		$("#closeSellInvoiceBtn").click();
		 $("#staticSellTransInvoice input[id='shipingLocation']").val("");
    	 $("#shipping_states span[class='bfh-selectbox-option']").text("");

	});

	$("#savePopTxnDetailBtn").click(function(){
		$("#closeSellInvoiceBtn").click();
	});
});

var setStaticSellTransInvoiceData = function(jsonData){
	var datetimeOfShipping = $("#staticSellTransInvoice input[id='datetimeofsupplyTxt']").val();
	var transportMode = $("#staticSellTransInvoice input[id='invoiceTranportationMode']").val();
	var invoiceVehicleDetail = $("#staticSellTransInvoice input[id='invoiceVehicleDetail']").val();
	var invoiceTerms = $("#staticSellTransInvoice textarea[id='invoiceTerms']").val();
	var dateofgoodsremove = $("#staticSellTransInvoice input[id='dateofgoodsremoveTxt']").val();
	var numgoodsremove = $("#staticSellTransInvoice input[id='numgoodsremove']").val();
	var ecomGstin1 = $("#staticSellTransInvoice input[id='ecomGstin1']").val();
	var ecomGstin2 = $("#staticSellTransInvoice input[id='ecomGstin2']").val();
	var destCountry = $("#dest_countries span[class='bfh-selectbox-option']").text();
	var destCurrencyCode = $("#staticSellTransInvoice input[id='destCurrencyCode']").val();
	var currencyRate = $("#staticSellTransInvoice input[id='currencyRate']").val();
    var shipingAddress = $("#staticSellTransInvoice textarea[id='shipingAddress']").val();
    var shipingLocation = $("#staticSellTransInvoice input[id='shipingLocation']").val();
    var shipingCountry = $("#shipping_countries span[class='bfh-selectbox-option']").text();
    var shipingState = $("#shipping_states span[class='bfh-selectbox-option']").text();
	var portCode = $("#staticSellTransInvoice input[id='portCode']").val();
	var invoiceHeading = $("#invoiceHeading").find('option:selected').val();
	var digiSignContent = $("#digitalSignatureText").val();

	jsonData.datetimeOfShipping = datetimeOfShipping;
	jsonData.transportMode = transportMode;
	jsonData.invoiceVehicleDetail = invoiceVehicleDetail;
	jsonData.invoiceTerms = invoiceTerms;
	jsonData.dateofgoodsremove = dateofgoodsremove;
	jsonData.numgoodsremove = numgoodsremove;
	jsonData.ecomGstin1 = ecomGstin1;
	jsonData.ecomGstin2 = ecomGstin2;
	jsonData.destCountry = destCountry;
	jsonData.destCurrencyCode = destCurrencyCode;
	jsonData.currencyRate = currencyRate;
	jsonData.shipingAddress = shipingAddress;
	jsonData.shipingLocation = shipingLocation;
	jsonData.shipingCountry = shipingCountry;
	jsonData.shipingState = shipingState;
	jsonData.portCode = portCode;
	jsonData.invoiceHeading = invoiceHeading;
	jsonData.digiSignContent = digiSignContent;
}

function calculateNetAmtForSalesRetTransaction(elem,data,parentTr){
	var gross=$("#"+parentTr+" input[id='srtfccgross']").val();
	var discountAmt=$("#"+parentTr+" input[id='srtfccDiscountAmt']").val();
	var taxTotalAmount=$("#"+parentTr+" input[id='srtfcctaxamnt']").val();
	var adjustmentAmount=$("#"+parentTr+" input[id='srtfcchowmuchfromadvance']").val();
	$("#srtfccnetAmountLabel").text("");
	for(var i=0;i<data.branchSpecificsTaxComponentData.length;i++){
	   if(i==0){
			$("#srtfccnetAmountLabel").append('<br/>'+data.branchSpecificsTaxComponentData[i].individualTax+",");
			$("#"+parentTr+" div[class='individualtaxdiv']").text(data.branchSpecificsTaxComponentData[i].individualTax);
			$("#"+parentTr+" div[class='individualtaxformuladiv']").text(data.branchSpecificsTaxFormulaComponentData[i].individualTaxFormula);
		}else{
			$("#srtfccnetAmountLabel").append('<br/>'+data.branchSpecificsTaxComponentData[i].individualTax+",");
			var existingIndividualTaxComp=$("#"+parentTr+" div[class='individualtaxdiv']").text();
			var existingIndividualTaxFormulaComp=$("#"+parentTr+" div[class='individualtaxformuladiv']").text();
			$("#"+parentTr+" div[class='individualtaxdiv']").text(existingIndividualTaxComp+","+data.branchSpecificsTaxComponentData[i].individualTax);
			$("#"+parentTr+" div[class='individualtaxformuladiv']").text(existingIndividualTaxFormulaComp+","+data.branchSpecificsTaxFormulaComponentData[i].individualTaxFormula);
		}
	}
    var taxTotalAmount=parseFloat(data.branchSpecificsTaxResultAmountData[0].taxTotalAmount);
    var taxTypeAndRate = $("#srtfccnetAmountLabel").text();
	var netAmount=parseFloat(gross);
	if(adjustmentAmount!="" && gross!=""){
		netAmount=(parseFloat(netAmount)-parseFloat(adjustmentAmount));
	}
	if(taxTotalAmount!=""){
		netAmount=(parseFloat(netAmount)+parseFloat(taxTotalAmount));
	}
	$("#"+parentTr+" input[id='srtfcctaxtypes']").val(taxTypeAndRate);
	$("#"+parentTr+" input[id='srtfcctaxamnt']").val((taxTotalAmount*1).toFixed(2));
	$("#"+parentTr+" input[id='srtfccnetamnt']").val((netAmount*1).toFixed(2));
	let netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	$("#srtfccnetamntTotal").val(Math.round(netAmountTotal).toFixed(2));
}

function calculateNetAmtForSalesRetWhenAdvChanged(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var gross=$("#"+parentTr+" input[id='srtfccgross']").val();
	var discountAmt=$("#"+parentTr+" input[id='srtfccDiscountAmt']").val();
	var taxTotalAmount=$("#"+parentTr+" input[id='srtfcctaxamnt']").val();
	var custAdvAdjusted=$("#"+parentTr+" input[id='srtfcccustomeradvance']").val();
	var adjustmentAmountReturned=$("#"+parentTr+" input[id='srtfcchowmuchfromadvance']").val();

	if(parseFloat(adjustmentAmountReturned)>parseFloat(custAdvAdjusted)){
		swal("Exceeding data limit!", "Advance adjustment cannot be more than advance value.", "error");
		$(elem).val("");
		return true;
	}
	var netAmount=parseFloat(gross);
	if(adjustmentAmountReturned!="" && gross!=""){
		netAmount=(parseFloat(netAmount)-parseFloat(adjustmentAmountReturned));
	}
	if(taxTotalAmount!=""){
		netAmount=(parseFloat(netAmount)+parseFloat(taxTotalAmount));
	}
	$("#"+parentTr+" input[id='srtfccnetamnt']").val((netAmount*1).toFixed(2));
	let parentOfparentTr = $("#"+parentTr).closest('div').attr('id');
	let netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	$("#srtfccnetamntTotal").val(Math.round(netAmountTotal).toFixed(2));
}


var selectGstTaxes = function(elem){
	var destinationGstin = $(elem).val();
	var parentTr = $(elem).closest('tr').attr('id');
	var sourceGstin = $("#"+parentTr+" select[class='txnBranches']").children(":selected").attr("id");
	if(destinationGstin === sourceGstin){

	}
	//var tmpsss=$(elem).children(":selected").attr("id");
	swal("INFO!",sourceGstin,"info");
}


var typingTimer;
var doneTypingInterval = 5000;
var adjustAdvance = function(elem){
    var parentTr = $(elem).closest('tr').attr('id');
    var txnAdjustmentAmount=$("#"+parentTr+" input[class='howMuchAdvance']").val();
    if(txnAdjustmentAmount == ""){
		txnAdjustmentAmount = 0;
    }
	clearTimeout(typingTimer);
    var returnVal = whenAdvanceIsEmpty(elem);
	if(returnVal === false){
		return false;
	}
	returnVal = calculateNetAmountWhenAdvanceChanged(elem);
	if(returnVal === false){
		return false;
	}
	returnVal = getAdvanceAdjustmentTaxSell(elem);
	if(returnVal === false){
		return false;
	}

    //typingTimer = setTimeout(calculateNetAmountForSell(elem), doneTypingInterval);
}

var getAdvanceAdjustmentTaxSell = function(elem){
	var returnValue = true;
	var parentTr = $(elem).closest('tr').attr('id');
	var parentOfparentTr = $(elem).parents().closest('tr').attr('id');
    var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
    if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
        parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
    }
	var parentTable = $(elem).parents().closest('table').attr('id');

	var txnUnavailable=$("#"+parentOfparentTr+" input[name='unAvailableCustomer']").val();
	var txnAdjustmentAmount=$("#"+parentTr+" input[class='howMuchAdvance']").val();
	if(txnAdjustmentAmount == ""){
		txnAdjustmentAmount = 0;
	}
	var txnBranch=$("#"+parentOfparentTr+" select[class='txnBranches']").val();
	var txnVendorCustomer=$("#"+parentOfparentTr+" .masterList").val();
	var txnitemSpecifics=$("#"+parentTr+" .txnItems").val();
	var txnInvoiceValue = $("#"+parentTr+" input[class='invoiceValue']").val();
	if(txnBranch==""){
		swal("Incomplete transaction detail!", "Please select a Branch.", "error");
		returnValue=false;
	}
	var txnTypeOfSupply = $("#"+parentOfparentTr+" select[class='txnTypeOfSupply']").val();
	if(txnTypeOfSupply == "" || txnTypeOfSupply == null){
		swal("Invalid Type of Supply!", "Please select valid Type of Supply.", "error");
		$("#"+parentTr+" span[class='select2-selection__clear']").trigger("mousedown");
		return false;
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
	if(txnInvoiceValue==""){
		//swal("Incomplete transaction detail!", "Please provide price/unit to calaculate Gross Amount.", "error");
		$("#"+parentTr+" input[class='netAmountVal']").val(0);
		$("#"+parentOfparentTr+" div[class='netAmountDescriptionDisplay']").html("");
		$("#"+parentOfparentTr+" input[class='netAmountValTotal']").val("");
		returnValue=false;
	}
	var sourceGstin = $("#"+parentOfparentTr+" select[class='txnBranches']").children(":selected").attr("id");
	var destGstinCode = "";
	var destCustDetailId = $("#"+parentOfparentTr+" select[class='placeOfSply txnDestGstinCls']").children(":selected").attr("id");

	if(txnUnavailable !== null && txnUnavailable !== "" && typeof txnUnavailable !== "undefined"){
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
	}else{
		destGstinCode = $("#"+parentOfparentTr+" select[class='placeOfSply txnDestGstinCls']").val();
	}

	if(txnTypeOfSupply != "3" && destGstinCode == ""){
		swal("Invalid Place of Supply!", "Please provide valid place of supply.", "error");
		$("#"+parentTr+" span[class='select2-selection__clear']").trigger("mousedown");
		return false;
	}
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnBranchId=txnBranch;
	jsonData.txnSpecificsId=txnitemSpecifics;
	jsonData.txnAdjustmentAmount=txnAdjustmentAmount;
	jsonData.txnSelectedVendorCustomer=txnVendorCustomer;
	jsonData.txnPurposeValue = txnPurposeVal;
	jsonData.txnSourceGstinCode = sourceGstin;
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
	   		if(data.advAdjTaxData){
	   			var advAdjTaxNameList = "";
		    	for(var i=0;i<data.advAdjTaxData.length;i++){
		    		//advAdjTaxNameList += '<div class="taxNameHead">'+data.advAdjTaxData[i].taxName+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
	    			var taxCellData = '<div class="txnTaxNameCls">'+data.advAdjTaxData[i].taxName+'</div>';
	    			if(data.advAdjTaxData[i].taxName == "CESS") {
	    			 taxCellData +='<input type="text" class="txnTaxOnAdvAdjCls" id="taxOnAdvAdj" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);" placeholder="Tax Amount" value="'+data.advAdjTaxData[i].taxAmount+'"/>';
	    			}else {
	    			 taxCellData +='<input type="text" class="txnTaxOnAdvAdjCls" id="taxOnAdvAdj" readonly="readonly" placeholder="Tax Amount" value="'+data.advAdjTaxData[i].taxAmount+'"/>';
	    			}
	    			taxCellData += '<input type="hidden" class="txnTaxNameOnAdvAdjCls" id="taxNameOnAdvAdj" value="'+data.advAdjTaxData[i].taxName+'"/>';
					$("#"+parentTr+ " div[id='advAdjTaxCell"+i+"']").addClass('advAdjTaxCellCls');
					$("#"+parentTr+ " div[id='advAdjTaxCell"+i+"']").empty();
	    			$("#"+parentTr+ " div[id='advAdjTaxCell"+i+"']").append(taxCellData);
		    	}
		    	//$("#"+parentTr+ " div[id='advAdjTaxCell']").addClass('div'+i+'-wd');
                /*if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                    $("#" + parentTable + " div[id='advAdjTaxNameList']").addClass('div' + i + '-wd');
                    $("#" + parentTable + " div[id='advAdjTaxNameList']").empty();
                    $("#" + parentTable + " div[id='advAdjTaxNameList']").append(advAdjTaxNameList);
                }else{
                    $("#" + parentTable + " table[id='multipleItemsTblHead'] div[id='advAdjTaxNameList']").addClass('div' + i + '-wd');
                    $("#" + parentTable + " table[id='multipleItemsTblHead'] div[id='advAdjTaxNameList']").empty();
                    $("#" + parentTable + " table[id='multipleItemsTblHead'] div[id='advAdjTaxNameList']").append(advAdjTaxNameList);
				}*/
	   		}
	   	},
	   	error: function (xhr, status, error) {
	   		if(xhr.status == 401){ doLogout();
	   		}else if(xhr.status == 500){
	    		swal("Error on calculating tax on advance adjustment!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
	return returnValue;
}


function calInvoiceValueTotalForMultiItems(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var parentOfparentTr = $(elem).parents().closest('tr').attr('id');
    var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
    if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER ){
        parentOfparentTr = $(elem).parent().parent().parent().parent().parent().parent().closest('div').attr('id');
    }
	var totalInvoice=0;
	$("#" +parentOfparentTr+" .multipleItemsTable > tbody > tr").each(function() {
		var invoiceValue = $(this).find("td input[class='invoiceValue']").val();
		if(invoiceValue!="" && typeof invoiceValue!='undefined'){
			totalInvoice = totalInvoice + parseFloat(invoiceValue);
		}
	});
	return totalInvoice;
}

var checkIfCustomerCreditLimitExceeded = function(customerId,netAmt,txnForItem,txnForBranch,txnTypeOfSupply,txnPlaceOfSupply){
	var creditExceeded=true;
	var jsonData={};
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.customerId=customerId;
	jsonData.netAmt=netAmt;
	jsonData.txnForItem=txnForItem;
	jsonData.txnForBranch=txnForBranch;
	jsonData.txnTypeOfSupply=txnTypeOfSupply;
	jsonData.txnPlaceOfSupply=txnPlaceOfSupply;
	var url="/customer/checkIfCustomerCreditLimitExceeded";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		contentType : 'application/json',
		async: false,
		success     : function (data) {
			if(data.custCreditLimitInfo.length>0){
				if(data.custCreditLimitInfo[0].creditLimitExceeded == "true"){
					if(data.custCreditLimitInfo[0].processOrStopTransaction == 0){//process
						$("#sellOnCreditTable tr[id='soccpltrid'] textarea[id='socplRemarks']").val("Customer has crossed the credit limit.");
						swal("Customer has crossed the credit limit.","INFO");
						creditExceeded= true;
					}else{ //stop transction as credit limit exceeded
						disableTransactionButtons();
						creditExceeded= false;
					}
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
				swal("Error on fetching item stock!", "Please retry, if problem persists contact support team", "error");
			}
		}
	});
	return creditExceeded;
}

$(document).ready(function(){
    $('.bfh-countries').bfhcountries({country:'US', blank:false});
    $('.bfh-currencies').bfhcurrencies({country:'UDS', blank:false});
});


function calculateInclusiveTaxAndSubmit(elem) {
	var returnValue = true;
	var parentTr = $(elem).closest('tr').attr('id');
	var parentOfparentTr = $("#"+parentTr).parent().parent().parent().parent().closest('div').attr('id');
	var parentTable = $(elem).parents().closest('table').attr('id');
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	var txnUnavailable=$("#"+parentOfparentTr+" input[name='unAvailableCustomer']").val();
	var txnAdjustmentAmount=$(elem).val();
	$("#"+parentTr).find(".txnPerUnitPrice").hide();
	if(txnAdjustmentAmount == "" || txnAdjustmentAmount =="0"){
		return false;
	}
	var txnBranch=$("#"+parentOfparentTr+" select[class='txnBranches']").val();
	var txnVendorCustomer=$("#"+parentOfparentTr+" .masterList").val();
	var txnitemSpecifics=$("#"+parentTr+" .txnItems").val();
	var txnInvoiceValue = $("#"+parentTr+" input[class='invoiceValue']").val();
	if(txnBranch==""){
		swal("Incomplete transaction detail!", "Please select a Branch.", "error");
		returnValue=false;
	}
	var txnTypeOfSupply = $("#"+parentOfparentTr+" select[class='txnTypeOfSupply']").val();
	if(txnTypeOfSupply == "" || txnTypeOfSupply == null){
		swal("Invalid Type of Supply!", "Please select valid Type of Supply.", "error");
		$("#"+parentTr+" span[class='select2-selection__clear']").trigger("mousedown");
		return false;
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

	var sourceGstin = $("#"+parentOfparentTr+" select[class='txnBranches']").children(":selected").attr("id");
	var destGstinCode = "";
	var destCustDetailId = $("#"+parentOfparentTr+" select[class='placeOfSply txnDestGstinCls']").children(":selected").attr("id");

	if(txnUnavailable !== null && txnUnavailable !== "" && typeof txnUnavailable !== "undefined"){
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
	}else{
		destGstinCode = $("#"+parentOfparentTr+" select[class='placeOfSply txnDestGstinCls']").val();
	}

	if(txnTypeOfSupply != "3" && destGstinCode == ""){
		swal("Invalid Place of Supply!", "Please provide valid place of supply.", "error");
		$("#"+parentTr+" span[class='select2-selection__clear']").trigger("mousedown");
		return false;
	}
	var txnDate=$("#"+parentOfparentTr).find(".txnBackDate").val();
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnBranchId=txnBranch;
	jsonData.txnSpecificsId=txnitemSpecifics;
	jsonData.txnAdjustmentAmount=txnAdjustmentAmount;
	jsonData.txnSelectedVendorCustomer=txnVendorCustomer;
	jsonData.txnPurposeValue = txnPurposeVal;
	jsonData.txnSourceGstinCode = sourceGstin;
	jsonData.txnDestGstinCode = destGstinCode;
	jsonData.txnDestCustDetailId = destCustDetailId;
	jsonData.txnTypeOfSupply = txnTypeOfSupply;
	jsonData.txnWithWithoutTax = txnWithWithoutTax;
	jsonData.txnDate = txnDate;

		var url="/transaction/calculatePriceForInclusive";

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
			   var temp = $("#"+parentTr+" input[class='txnPerUnitPrice']");
			   temp.val(data.price);
			   temp.css('display','none');
			},
			error: function (xhr, status, error) {
		   		if(xhr.status == 401){ doLogout();
		   		}else if(xhr.status == 500){
		    		swal("Error on calculate inclusive net amount!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
			$("#"+parentTr+" input[class='txnPerUnitPrice']").hide();
			$("#"+parentTr+" input[class='txnPerUnitPrice']").keyup();
				$.unblockUI();
			}
		});
	 return returnValue;
}

var validateKnowledgeLib = function(elem){
    var parentTr = $(elem).closest('tr').attr('id');
    var followedkl=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").attr('id');
    if(typeof followedkl!='undefined'){
        var klfollowednotfollowed=$("#"+parentTr+" div[class='klBranchSpecfTd'] input[name='klfollowed']").is(':checked');
        if(klfollowednotfollowed==false){
            swal("Transaction knowledge library!", "Please follow mandatory knowledge library before proceeding.", "error");
            $(elem).val('');
            enableTransactionButtons();
            return false;
        }
    }
}


function checkForCompositionScheme(elem) {
	var isCompositionSchemeApply = $("#isCompositionSchemeApply").val();
    if(isCompositionSchemeApply == 1) {
    	var parentTr = $(elem).closest('tr').attr('id');
    	var txnForUnavailableCustomer=$("#"+parentTr+" input[name='unAvailableCustomer']").val();
    	var sourceGstin=$("#"+parentTr+" select[class='txnBranches'] option:selected").attr("id");
    	var destinGstin=$("#"+parentTr+" .txnDestGstinCls").val();

    	if(txnForUnavailableCustomer !== null && txnForUnavailableCustomer !== ""){
			//user in case of walkin customer
			var txnWalkinCustomerType = $("#"+parentTr+" select[class='walkinCustType']").val();
			if(txnWalkinCustomerType == "1" || txnWalkinCustomerType == "2"){
				destinGstin = $("#"+parentTr+" input[class='placeOfSplyTextHid']").val();
			}else if(txnWalkinCustomerType == "3" || txnWalkinCustomerType == "4"){
				destinGstin = sourceGstin;
			}else if(txnWalkinCustomerType == "5" || txnWalkinCustomerType == "6"){
				destinGstin = $("#"+parentTr+" select[name='txnWalkinPlcSplySelect']").val();
			}
		}

    	checkGstinForCompositionScheme(parentTr,sourceGstin,destinGstin,elem);

    }

}


function checkGstinForCompositionScheme(parentTR,sourceGstin,destinGstin,elem) {
	if(typeof sourceGstin != 'undefined' && sourceGstin != "" && destinGstin != "" && sourceGstin.length >= 2 && destinGstin.length >= 2) {
		var branchCode = sourceGstin.substring(0, 2);
		var custGSTCode = destinGstin.substring(0, 2);
		if(branchCode != custGSTCode) {
			$(elem).val("");
			swal("Invalid Place Of Supply!", "User registered under composition scheme, Not eligible for Interstate sales", "error");
    	}
	}
}

function txnChangesForCompositionScheme(elem) {
	var isCompositionSchemeApply = $("#isCompositionSchemeApply").val();
    if(isCompositionSchemeApply == 1) {
    	var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
    	if(transactionPurposeId != "" && (transactionPurposeId == SELL_ON_CASH_COLLECT_PAYMENT_NOW || transactionPurposeId == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER || transactionPurposeId == RECEIVE_ADVANCE_FROM_CUSTOMER)) {
    		$(elem).closest("tr").find('.txnTypeOfSupply').val("1");
    		$(elem).closest("tr").find('.txnTypeOfSupply').prop('disabled', true);
    	}
    }else {
    	$(elem).closest("tr").find('.txnTypeOfSupply').val("");
		$(elem).closest("tr").find('.txnTypeOfSupply').prop('disabled', false);
    }
}
