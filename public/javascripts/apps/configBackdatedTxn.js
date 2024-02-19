
function setBackDatedDateSelecter(parentId) {
		var backDatedDays = $("#backDatedDays").val();
		$("#txnDocRefNo").val("");
		$("#txnDocRefNoDiv").hide();
		$(".txnBackDate").datepicker('destroy');
		$("#"+parentId).find(".txnBackDate").datepicker({
			changeMonth : true,
			changeYear : true,
			dateFormat:  'M d,yy',
			onSelect: function(x,y){
		        $(this).focus();
		        validateBackDate(this);
		    }
		});
		var newdate = new Date();
		if(backDatedDays != "" && parseInt(backDatedDays) > 0) {
			newdate.setDate(newdate.getDate() - parseInt(backDatedDays));
		}
		$("#"+parentId).find(".txnBackDate").datepicker("option", "minDate", newdate);
		$("#"+parentId).find(".txnBackDate").datepicker("option", "maxDate",  new Date());
		$("#"+parentId).find(".txnBackDate").datepicker().datepicker("setDate", new Date());
		$("#"+parentId).find("txnBackDate").show();
}

function validateBackDate(elem) {
	var val = $(elem).val();
	var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	if(val != "" && transactionPurposeId !="") {
		var backDate = new Date(val);
		var today = new Date();
		 if (today > backDate) {
			if(transactionPurposeId == SELL_ON_CASH_COLLECT_PAYMENT_NOW || transactionPurposeId == SELL_ON_CREDIT_COLLECT_PAYMENT_LATER ||
				transactionPurposeId == RECEIVE_ADVANCE_FROM_CUSTOMER || transactionPurposeId == RECEIVE_PAYMENT_FROM_CUSTOMER ||
				transactionPurposeId == REFUND_ADVANCE_RECEIVED || transactionPurposeId == REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE ||
				transactionPurposeId == CREDIT_NOTE_CUSTOMER || transactionPurposeId == DEBIT_NOTE_CUSTOMER || transactionPurposeId == TRANSFER_FUNDS_FROM_ONE_BANK_TO_ANOTHER) {
				$("#txnDocRefNo").val("");
				$("#isBackDatedTxn").val("true");
				$("#txnDocRefNoDiv").css("display","inline");
				$("#backDateDiv").show();
			}else {
				 $("#txnDocRefNo").val("");
				 $("#isBackDatedTxn").val("false");
				 $("#txnDocRefNoDiv").hide();
			 }
		 }else {
			 $("#txnDocRefNo").val("");
			 $("#isBackDatedTxn").val("false");
			  $("#txnDocRefNoDiv").hide();
		 }
	}else {
		 $("#txnDocRefNo").val("");
		 $("#isBackDatedTxn").val("false");
		  $("#txnDocRefNoDiv").hide();
	}
}          