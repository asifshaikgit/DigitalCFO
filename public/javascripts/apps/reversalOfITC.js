function addRevOfITCRow(){
	if(!validateRow()) {
		return false;
	}
	var branchData = ['<option value="">Please Select</option>'];
	var i=1;
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	var url="/user/getBranchData";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		async: false,
		contentType : 'application/json',
		success     : function (data) {
			for(var j=0;j<data.branchData.length;j++){
				branchData[i++] = '<option value="'+data.branchData[j].id+'" >';
				branchData[i++] = data.branchData[j].name;
				branchData[i++] = '</option>';
			}
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
    var revOfITCRow = [];
     i = 0;
    revOfITCRow[i++] = '<tr id="revOfITCid"><td><select class="txnBranches" name="reversalBranches" id="reversalBranches" onChange="setItemwiseGSTLedger(this);">';
   // revOfITCRow[i++] = '<tr id="revOfITCid"><td><select class="txnBranches" name="reversalBranches" id="reversalBranches" onChange="setBranchWiseExpenceItem(this);">';
    revOfITCRow[i++] =  branchData.join('');
    revOfITCRow[i++] = '</select></td>';
   // revOfITCRow[i++] = '<td><select class="txnItem" name="reversalProduct" id="reversalProduct" onChange="setItemwiseGSTLedger(this);"><option value="">Please Select</option></select></td>';
    revOfITCRow[i++] = '<td><select class="reversalLedger" style="width:300px;" name="reversalLedger" id="reversalLedger" onChange=""><option value="">Please Select</option></select></td>';
    revOfITCRow[i++] = '<td><input class="txnPerUnitPrice" placeholder="" type="text" name="amountToBeReversal" id="amountToBeReversal" onkeypress="return onlyDotsAndNumbers(event);" onkeyup=""/></td>';
    revOfITCRow[i++] = '<td><select class="reasonForReversal" name="reasonForReversal" id="reasonForReversal" onChange="">';
    revOfITCRow[i++] = '<option value="">Please Select</option><option value="1">Amount in terms of Rule 37(2)</option><option value="2">Amount in terms of Rule 42(1)(m)</option><option value="3">Amount in terms of Rule 42(2)(a)</option>';
    revOfITCRow[i++] = 	'<option value="4">Amount in terms of Rule 42(2)(b)</option><option value="5">Amount in terms of rule 43(1)(h)</option><option value="6">On account of amount paid subsequent to Reversal of ITC</option><option value="7">Any other Reversal(+)/Reclaim(-)</option></select>';
    revOfITCRow[i++] = '</td><td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';
    $("#txnRevOfITCTable > tbody").append(revOfITCRow.join(''));
}

var fetchRevOfITCTableData = function() {
    var multipleItemsData = [];
    $("#txnRevOfITCTable > tbody > tr").each(function() {
      	 var json = {};
    		 json.branchId = $.trim($(this).find("td .txnBranches option:selected").val());
    		// json.itemId = $.trim($(this).find("td .txnItem option:selected").val());
    		 json.taxLedger = $.trim($(this).find("td .reversalLedger option:selected").val());
    		 json.reasonForReversal = $.trim($(this).find("td .reasonForReversal option:selected").val());
    		 json.revarsalAmount = $.trim($(this).find("td input[class='txnPerUnitPrice']").val());
         multipleItemsData.push(JSON.stringify(json));
   });
   return multipleItemsData;
}

function clearRowForITC(comp) {
	$(comp).closest('tr').find(".txnBranches").val("");
	// $(comp).closest('tr').find(".txnItem").val("");
	// $(comp).closest('tr').find(".txnItem").html("");
	$(comp).closest('tr').find(".reversalLedger").val("");
	$(comp).closest('tr').find(".reversalLedger").html("");
	$(comp).closest('tr').find(".txnPerUnitPrice").val("");
	$(comp).closest('tr').find(".reasonForReversal").val("");
}

function setBranchWiseExpenceItem(comp){
	var branchId = $(comp).val();
	if(branchId == "") {
		clearRowForITC(comp);
		return false;
	}
	$(comp).closest('tr').find(".txnItem").html("");
	var expenceItem = ['<option value="">Please Select</option>'];
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.branchId = branchId;
	jsonData.usermail = useremail;
	var i=1;
	var url="/data/getcoaexpenceitemsbranchwise";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		async: false,
		contentType : 'application/json',
		success     : function (data) {
			for(var j=0;j<data.expenceItemData.length;j++){
				expenceItem[i++] = '<option value="'+data.expenceItemData[j].id+'" >';
				expenceItem[i++] = data.expenceItemData[j].name;
				expenceItem[i++] = '</option>';
			}
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
	$(comp).closest('tr').find(".txnItem").append(expenceItem.join(''));
}

function validateRow() {
	var branch = $("#txnRevOfITCTable > tbody > tr:last").find(".txnBranches").val();
	// var item = $("#txnRevOfITCTable > tbody > tr:last").find(".txnItem").val();
	var ledger = $("#txnRevOfITCTable > tbody > tr:last").find(".reversalLedger").val();
	var price = $("#txnRevOfITCTable > tbody > tr:last").find(".txnPerUnitPrice").val();
	var reason = $("#txnRevOfITCTable > tbody > tr:last").find(".reasonForReversal").val();
	// if(branch == "" || item == "" || ledger == "" || price == "" || reason == "") {
	if(branch == "" || ledger == "" || price == "" || reason == "") {
		swal("Error!","Fill Last row ","error");
		return false
	}

	return true;
}


function setItemwiseGSTLedger(comp){
	var itemId = $(comp).val();
	if(itemId == "") {
		return false;
	}
	var branchId = $(comp).closest('tr').find(".txnBranches").val();
	$(comp).closest('tr').find(".reversalLedger").html("");
	var gstLedger = ['<option value="">Please Select</option>'];
	var i=1;
	var jsonData={};
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.branchId = branchId;
	var url="/claims/getclaimgstdata";
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
			for(var j=0; j < data.cgstRateList.length;j++) {
				gstLedger[i++] = '<option value="'+data.cgstRateList[j].id+'" rate="'+data.cgstRateList[j].rate+'">';
				gstLedger[i++] = data.cgstRateList[j].name;
				gstLedger[i++] = '</option>';
			}
			for(var j=0; j < data.sgstRateList.length;j++) {
				gstLedger[i++] = '<option value="'+data.sgstRateList[j].id+'" rate="'+data.sgstRateList[j].rate+'">';
				gstLedger[i++] = data.sgstRateList[j].name;
				gstLedger[i++] = '</option>';
			}
			for(var j=0; j < data.igstRateList.length;j++) {
				gstLedger[i++] = '<option value="'+data.igstRateList[j].id+'" rate="'+data.igstRateList[j].rate+'">';
				gstLedger[i++] = data.igstRateList[j].name;
				gstLedger[i++] = '</option>';
			}
			for(var j=0; j < data.cessRateList.length;j++) {
				gstLedger[i++] = '<option value="'+data.cessRateList[j].id+'" rate="'+data.cessRateList[j].rate+'">';
				gstLedger[i++] = data.cessRateList[j].name;
				gstLedger[i++] = '</option>';
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});

	$(comp).closest('tr').find(".reversalLedger").append(gstLedger.join(''));
}


var submitForAccountingRevOfITC = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	if(!validateRow()) {
		return false;
	}
	var txnJsonData={};
	var txnForBranch = $("#txnRevOfITCTable > tbody > tr:first").find("td .txnBranches option:selected").val();
	txnJsonData.txnPurpose = whatYouWantToDo;
	txnJsonData.txnPurposeVal = whatYouWantToDoVal;
	txnJsonData.txnforbranch = txnForBranch;
	txnJsonData.itemListData = fetchRevOfITCTableData();
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


