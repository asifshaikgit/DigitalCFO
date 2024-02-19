
// Single User Deployment 
// It will work only for Single User


$(function(){
	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	if(isSingleUserDeploy == "true") {
		$(".submitForApproval").text("Submit for Accounting");
		$(".submitForApproval").attr("title","Submit for Accounting");
	}
	
});


function approvealForSingleUser(data) {

	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	if(isSingleUserDeploy == "true") {
					var jsonData = {};
					if(data.singleUserAccounting.length > 0) {
						jsonData.useremail = data.singleUserAccounting[0].useremail;
						jsonData.selectedApproverAction = data.singleUserAccounting[0].selectedApproverAction;
						jsonData.transactionPrimId = data.singleUserAccounting[0].transactionPrimId;
						jsonData.selectedAddApproverEmail = data.singleUserAccounting[0].selectedAddApproverEmail;
						jsonData.suppDoc = data.singleUserAccounting[0].suppDoc;
						jsonData.txnRmarks = data.singleUserAccounting[0].txnRemarks;
						jsonData.txnInvDate = data.singleUserAccounting[0].txnInvDate;
						jsonData.paymentDetails = data.singleUserAccounting[0].paymentDetails;
						jsonData.txnPaymentBank = data.singleUserAccounting[0].txnPaymentBank;
						jsonData.txnInstrumentNum = data.singleUserAccounting[0].txnInstrumentNum;
						jsonData.txnInstrumentDate = data.singleUserAccounting[0].txnInstrumentDate;
						jsonData.bankInf = data.singleUserAccounting[0].bankInf;
						if(txnPurposeVal == MAKE_PROVISION_JOURNAL_ENTRY || txnPurposeVal == JOURNAL_ENTRY){
							provisionapproverActionSingleUser(jsonData);
						}else {
							completeAccountingSingleUser(jsonData);
						}
						
					}
		}
}

function completeAccountingSingleUser(jsonData){
	disableTransactionButtons();
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var url="/transaction/approverAction";
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
            if (data) {
				if (data.errorMessage) {
					swal("Error on submit for accounting!", data.errorMessage, "error");
                    disableTransactionButtons();
                    return false;
				}
			}
			if(typeof data.tdsPayableSpecific!='undefined' && data.tdsPayableSpecific == 0){
	   			swal("COA: mapping missing!", "Chart of Account, TDS Payable mapping is not defined, please define and try.", "error");
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


function provisionapproverActionSingleUser(txnJsonData){
		disableTransactionButtons();
		var url="/transactionProvision/approverAction";
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
				if(data.resultantAmount < 0){
					swal("Insufficient balance!","Insufficient balance in the bank account. Use alternative payment mode or infuse funds into the bank account. Effective Bank Balance is: " + data.resultantAmount,"error");
				}
				if(data.resultantCash < 0){
					swal("Insufficient balance!","Insufficient balance in the cash account. Use alternative payment mode or infuse funds into the cash account. Effective cash balance is: " + data.resultantCash,"error");
				}

				if(data.resultantPettyCashAmount < 0){
					swal("Insufficient balance!","Insufficient balance in the petty cash account. Use alternative payment mode or infuse funds into petty cash account. Effective petty cash balance is: " + data.resultantPettyCashAmount,"error");
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			},
			complete: function(data) {
				$.unblockUI();
				enableTransactionButtons();
			}
		});
}


function singleUserMiscChanges(parentTR) {

	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	if(isSingleUserDeploy == "true") {
		var length = $("#"+parentTR+" .txnBranches option").length;
		if(length == 2) {
			 $("#"+parentTR+" .txnBranches option:last").attr("selected", "selected");
			 $("#"+parentTR+" .txnBranches").trigger("change");
			 var branch = $("#"+parentTR+" .txnBranches").val();
			 if(branch != "") {
			 	 $("#"+parentTR+" .txnTypeOfSupply").val("1");
			 	 $("#"+parentTR+" .txnTypeOfSupply").trigger("change");
			 }
		}
		$("#"+parentTR+" .txnTypeOfSupply").val("1");
		$("#"+parentTR+" .txnTypeOfSupply").trigger("change");
	}	
	singleUserWalkCustomerChanges(parentTR);
}


function singleUserWalkCustomerChanges(parentTR) {
	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	if(isSingleUserDeploy == "true") {
		if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == RECEIVE_ADVANCE_FROM_CUSTOMER){
			$("#"+parentTR+" .txnWalkinDivCls").show();
			$("#"+parentTR+" .isWalkinCustPara").hide();
			$("#"+parentTR+" .regCustomerDivCls").find('.para-tm5-bm0').hide();
			$("#"+parentTR+" .regCustomerDivCls").find('.placeOfSply').hide();
			$("#"+parentTR+" .txnWalkinDivCls").find('.walkinCustType').hide();
			$("#"+parentTR+" .txnWalkinDivCls").find('.para-tm5-bm0').hide();
			$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSply').hide();
		}
	}
	
}

function changeUIforSingleUsr(comp) {
	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	var selectedVal = $(comp).val();
	var unavailable = $(comp).closest("tr").find(".unavailable").val();
	var txnTypeOfSupply = $(comp).closest("tr").find(".txnTypeOfSupply").val();
	var parentTR = $(comp).closest("tr").attr("id");
	if(isSingleUserDeploy == "true") {
		if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == RECEIVE_ADVANCE_FROM_CUSTOMER){
			if(unavailable == "" && selectedVal != "") {
				$("#"+parentTR+" .txnWalkinDivCls").show();
				$("#"+parentTR+" .isWalkinCustPara").hide();
				$("#"+parentTR+" .regCustomerDivCls").find('.para-tm5-bm0').show();
				$("#"+parentTR+" .regCustomerDivCls").find('.placeOfSply').show();
				$("#"+parentTR+" .txnWalkinDivCls").find('.walkinCustType').hide();
				$("#"+parentTR+" .txnWalkinDivCls").find('.para-tm5-bm0').hide();
				$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSply').hide();
				$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSplyText').hide();
				$("#"+parentTR+" .isWalkinCustomerCls").prop('checked', false);
				if(txnTypeOfSupply != "3" || txnTypeOfSupply != "4" || txnTypeOfSupply != "5"){
					$("#"+parentTR+" .txnWalkinDivCls").hide();
					$("#"+parentTR+" .isWalkinCustPara").hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSply').show();
				}
			}else {
				if(unavailable != "") {
					swal("Incorrect Data !!", "Please Clear WalkinCustomer Field!", "error");
					return;
				}
				if(selectedVal == ""){
					$("#"+parentTR+" .regCustomerDivCls").find('.para-tm5-bm0').hide();
					$("#"+parentTR+" .regCustomerDivCls").find('.placeOfSply').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSplyText').hide();
					$("#"+parentTR+" .isWalkinCustomerCls").prop('checked', false);
				}
				if(selectedVal == "" && unavailable == ""){
					$("#"+parentTR+" .txnWalkinDivCls").show();
					$("#"+parentTR+" .isWalkinCustPara").hide();
					$("#"+parentTR+" .regCustomerDivCls").find('.para-tm5-bm0').hide();
					$("#"+parentTR+" .regCustomerDivCls").find('.placeOfSply').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.walkinCustType').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.para-tm5-bm0').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSply').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSplyText').hide();
					$("#"+parentTR+" .isWalkinCustomerCls").prop('checked', false);
				}
			}
		}
	}
}


function changeUIforSingleInputUsr(comp) {
	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	var selectedVal = $(comp).val();
	var regCust = $(comp).closest("tr").find(".masterList").val();
	var txnTypeOfSupply = $(comp).closest("tr").find(".txnTypeOfSupply").val();
	var parentTR = $(comp).closest("tr").attr("id");
	if(isSingleUserDeploy == "true") {
		if(txnPurposeVal == SELL_ON_CASH_COLLECT_PAYMENT_NOW || txnPurposeVal == RECEIVE_ADVANCE_FROM_CUSTOMER){
			if(regCust == "" && selectedVal != "") {
				$("#"+parentTR+" .txnWalkinDivCls").show();
				$("#"+parentTR+" .isWalkinCustPara").hide();
				$("#"+parentTR+" .regCustomerDivCls").find('.para-tm5-bm0').hide();
				$("#"+parentTR+" .regCustomerDivCls").find('.placeOfSply').hide();
				$("#"+parentTR+" .txnWalkinDivCls").find('.walkinCustType').show();
				$("#"+parentTR+" .txnWalkinDivCls").find('.para-tm5-bm0').show();
				$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSply').show();
				$("#"+parentTR+" .isWalkinCustomerCls").prop('checked', true);
				if(txnTypeOfSupply != "3" || txnTypeOfSupply != "4" || txnTypeOfSupply != "5"){
					$("#"+parentTR+" p[name='txnWalkinDivCls']").show();
				}else{
					$("#"+parentTR+" p[name='txnWalkinDivCls']").hide();
					$("#"+parentTR+" select[class='txnWithWithoutTaxCls']").val('');

				}
			}else {
				if(regCust != "") {
					swal("Incorrect Data !!", "Please Clear selected Customer!", "error");
				}
				if(selectedVal == ""){
					$("#"+parentTR+" .txnWalkinDivCls").find('.walkinCustType').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.para-tm5-bm0').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSply').hide();
					$("#"+parentTR+" .isWalkinCustomerCls").prop('checked', false);
				}
				if(selectedVal == "" && regCust == ""){
					$("#"+parentTR+" .txnWalkinDivCls").show();
					$("#"+parentTR+" .isWalkinCustPara").hide();
					$("#"+parentTR+" .regCustomerDivCls").find('.para-tm5-bm0').hide();
					$("#"+parentTR+" .regCustomerDivCls").find('.placeOfSply').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.walkinCustType').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.para-tm5-bm0').hide();
					$("#"+parentTR+" .txnWalkinDivCls").find('.placeOfSply').hide();
					$("#"+parentTR+" .isWalkinCustomerCls").prop('checked', false);
				}
				if(txnTypeOfSupply != "3" || txnTypeOfSupply != "4" || txnTypeOfSupply != "5"){
					$("#"+parentTR+" p[name='txnWalkinDivCls']").show();
				}else{
					$("#"+parentTR+" p[name='txnWalkinDivCls']").hide();
					$("#"+parentTR+" select[class='txnWithWithoutTaxCls']").val('');

				}
			}
			
		}
	}
}







