var resetCreditDebitTxn = function(){
    var transPurposeId= parseInt($("#whatYouWantToDo").find('option:selected').val());
    if(transPurposeId == CREDIT_NOTE_CUSTOMER || transPurposeId == DEBIT_NOTE_CUSTOMER){
        resetMultiItemsTableLength('creditDebitTxnDiv');
        resetMainTransTableFields('cdtdbttrid');
        resetMultiItemsTableFieldsData('#cdtdbttrid');
        $("#creditDebitTxnDiv select[class='txnBranches']").val('');
        $("#creditDebitTxnDiv .masterList").val('');
        $("#creditDebitTxnDiv select[class='salesExpenseTxns']").children().remove();
        $("#creditDebitTxnDiv select[class='salesExpenseTxns']").append('<option value="">--Please Select--</option>');
    }else if(transPurposeId == CREDIT_NOTE_VENDOR || transPurposeId == DEBIT_NOTE_VENDOR){
        resetMultiItemsTableLength('creditDebitVendTxnDiv');
        resetMainTransTableFields('cdtdbvtrid');
        resetMultiItemsTableFieldsData('#cdtdbvtrid');
        $("#creditDebitVendTxnDiv select[class='txnBranches']").val('');
        $("#creditDebitVendTxnDiv .masterList").val('');
        $("#creditDebitVendTxnDiv select[class='salesExpenseTxns']").children().remove();
        $("#creditDebitVendTxnDiv select[class='salesExpenseTxns']").append('<option value="">--Please Select--</option>');
    }
}

var checkCreditDebitType = function(elem){
    var transPurposeId= parseInt($("#whatYouWantToDo").find('option:selected').val());
    var whatYouWantToDo = $("#whatYouWantToDo").find('option:selected').text();
    var increaseDecrease = "";
    if(transPurposeId === CREDIT_NOTE_CUSTOMER || transPurposeId === DEBIT_NOTE_CUSTOMER) {
        increaseDecrease = $("#creditDebitTxnDiv select[class='creditDebitType'] option:selected").val();
    }else  if(transPurposeId === CREDIT_NOTE_VENDOR || transPurposeId === DEBIT_NOTE_VENDOR) {
        increaseDecrease = $("#creditDebitVendTxnDiv select[class='creditDebitType'] option:selected").val();
	}
    if (increaseDecrease === "") {
        swal("Incomplete transaction data!", "Please select " + whatYouWantToDo + " type", "error");
        $(elem).val('');
        return false;
    }
    increaseDecrease = parseInt(increaseDecrease);
    if(increaseDecrease === 1 ){
        $("#creditDebitTxnDiv .txnPerUnitPrice").removeAttr("disabled");
        $("#creditDebitTxnDiv .txnNoOfUnit").attr("disabled", "disabled");
        $("#creditDebitVendTxnDiv .txnPerUnitPrice").removeAttr("disabled");
        $("#creditDebitVendTxnDiv .txnNoOfUnit").attr("disabled", "disabled");
	}else if(increaseDecrease === 2){
        $("#creditDebitTxnDiv .txnPerUnitPrice").attr("disabled", "disabled");
        $("#creditDebitTxnDiv .txnNoOfUnit").removeAttr("disabled");
        $("#creditDebitVendTxnDiv .txnPerUnitPrice").attr("disabled", "disabled");
        $("#creditDebitVendTxnDiv .txnNoOfUnit").removeAttr("disabled");
	}
}

var enableDisableNoteTxnField = function(txnPurposeVal){
    var increaseDecrease = "";
    var parentTr = "";
    if(txnPurposeVal == CREDIT_NOTE_CUSTOMER || txnPurposeVal == DEBIT_NOTE_CUSTOMER) {
        increaseDecrease = $("#creditDebitTxnDiv select[class='creditDebitType'] option:selected").val();
        parentTr = "creditDebitTxnDiv";
    }else  if(txnPurposeVal == CREDIT_NOTE_VENDOR || txnPurposeVal == DEBIT_NOTE_VENDOR) {
        increaseDecrease = $("#creditDebitVendTxnDiv select[class='creditDebitType'] option:selected").val();
        parentTr = "creditDebitVendTxnDiv";
    }
    if(increaseDecrease === "1"){
        $("#"+parentTr+" input[class='txnPerUnitPrice']").removeAttr("disabled");
        $("#"+parentTr+" input[class='txnNoOfUnit']").attr("disabled", "disabled");
    }else if(increaseDecrease === "2"){
        $("#"+parentTr+" input[class='txnNoOfUnit']").removeAttr("disabled");
        $("#"+parentTr+" input[class='txnPerUnitPrice']").attr("disabled", "disabled");
    }else{
        $("#"+parentTr+" input[class='txnNoOfUnit']").removeAttr("disabled");
        $("#"+parentTr+" input[class='txnPerUnitPrice']").removeAttr("disabled");
	}
    $("#"+parentTr+" input[class='customerAdvance']").attr("disabled", "disabled");
    $("#"+parentTr+" input[class='howMuchAdvance']").attr("disabled", "disabled");
}

var checkData4CreditDebitNote = function(elem, hidElem){
	var parentTr = $(elem).closest('tr').attr('id');
	var newData = $(elem).val();
	var originalData = $("#"+parentTr+" input[class='"+hidElem+"']").val();
	var increaseDecrease = $("#cdtdbttrid select[class='creditDebitType'] option:selected").val();
	if(typeof increaseDecrease === 'undefined' || increaseDecrease === ""){
        increaseDecrease = $("#cdtdbvtrid select[class='creditDebitType'] option:selected").val();
	}
	increaseDecrease = parseInt(increaseDecrease);
	var transPurposeId= parseInt($("#whatYouWantToDo").find('option:selected').val());
	if(transPurposeId === DEBIT_NOTE_CUSTOMER && increaseDecrease === 1){
        if(parseFloat(newData) < parseFloat(originalData) || parseFloat(newData) < 0){
            $(elem).val('');
        }
    }else if(transPurposeId === DEBIT_NOTE_CUSTOMER && increaseDecrease === 2){
        if(parseFloat(newData) < parseFloat(originalData) || parseFloat(newData) < 0){
            $(elem).val('');
        }
    }else if(transPurposeId === CREDIT_NOTE_CUSTOMER && increaseDecrease === 1){
        if(parseFloat(newData) > parseFloat(originalData) || parseFloat(newData) < 0){
            $(elem).val('');
        }
    }else if(transPurposeId === CREDIT_NOTE_CUSTOMER && increaseDecrease === 2){
        if(parseFloat(newData) > parseFloat(originalData) || parseFloat(newData) < 0){
            $(elem).val('');
        }
    }else if(transPurposeId === CREDIT_NOTE_VENDOR && increaseDecrease === 1){
        if(parseFloat(newData) < parseFloat(originalData) || parseFloat(newData) < 0){
            $(elem).val('');
        }
    }else if(transPurposeId === CREDIT_NOTE_VENDOR && increaseDecrease === 2){
        if(parseFloat(newData) < parseFloat(originalData) || parseFloat(newData) < 0){
            $(elem).val('');
        }
    }else if(transPurposeId === DEBIT_NOTE_VENDOR && increaseDecrease === 1){
        if(parseFloat(newData) > parseFloat(originalData) || parseFloat(newData) < 0){
            $(elem).val('');
        }
    }else if(transPurposeId === DEBIT_NOTE_VENDOR && increaseDecrease === 2){
        if(parseFloat(newData) > parseFloat(originalData) || parseFloat(newData) < 0){
            $(elem).val('');
        }
    }
}

var setCreditDebitCustomerTaxDetail = function(data, value, i, transactionTableTr){
	if(data.txnItemData[i].taxData){
		//var	taxNameList = "";
		for(var j=0; j<data.txnItemData[i].taxData.length; j++){
			//taxNameList += '<div class="taxNameHead">'+data.txnItemData[i].taxData[j].taxName+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
			var taxCellData = "";

			taxCellData = '<div class="txnTaxNameCls">'+data.txnItemData[i].taxData[j].taxName+'</div><input type="text" class="taxRate" readonly="readonly" placeholder="Tax Rate" value="'+data.txnItemData[i].taxData[j].taxRate+'"/>'+
			'<input type="text" class="txnTaxAmount" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);" placeholder="Tax Amount" value="'+data.txnItemData[i].taxData[j].taxAmount+'"/>';
			taxCellData+='<input type="hidden" class="txnTaxName" value="'+data.txnItemData[i].taxData[j].taxName+'"/>';
			taxCellData+='<input type="hidden" class="txnTaxID" value="'+data.txnItemData[i].taxData[j].taxid+'"/>';

			$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell"+j+"']").addClass('taxCellCls');
			$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell"+j+"']").empty();
			$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell"+j+"']").append(taxCellData);
		}

		//$("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last div[id='taxCell']").addClass('div-w'+j+'00');
		/*var taxNameHeadDivCount = $("#" + "transactionTableTr div[class='taxNameHead']").length;
		if(data.txnItemData[i].taxData.length > parseInt(taxNameHeadDivCount)){
    		$("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='taxNameList']").addClass('div-w'+j+'00');
    		$("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='taxNameList']").empty();
    		$("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='taxNameList']").append(taxNameList);
		}*/
	}

	if(data.txnItemData[i].advAdjTaxData){
		//var advAdjTaxNameList = "";
    	for(var j=0; j<data.txnItemData[i].advAdjTaxData.length; j++){
    		//advAdjTaxNameList += '<div class="taxNameHead">'+data.txnItemData[i].advAdjTaxData[j].taxName+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
			var taxCellData = '<div class="txnTaxNameCls">'+data.txnItemData[i].advAdjTaxData[j].taxName+'</div><input type="text" class="txnTaxOnAdvAdjCls" id="taxOnAdvAdj" readonly="readonly" placeholder="Tax Amount" value="'+data.txnItemData[i].advAdjTaxData[j].taxAmount+'"/>';
			taxCellData += '<input type="hidden" class="txnTaxNameOnAdvAdjCls" id="taxNameOnAdvAdj" value="'+data.txnItemData[i].advAdjTaxData[j].taxName+'"/>';
			$("#"+transactionTableTr+ " div[id='advAdjTaxCell"+j+"']").addClass('advAdjTaxCellCls');
			$("#"+transactionTableTr+ " div[id='advAdjTaxCell"+j+"']").empty();
			$("#"+transactionTableTr+ " div[id='advAdjTaxCell"+j+"']").append(taxCellData);
    	}
    	$("#"+transactionTableTr+ " div[id='advAdjTaxCell']").addClass('div'+j+'-wd');
    	/*$("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='advAdjTaxNameList']").addClass('div'+j+'-wd');
		$("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='advAdjTaxNameList']").empty();
		$("#"+transactionTableTr+" table[class='multipleItemsTable'] div[id='advAdjTaxNameList']").append(advAdjTaxNameList);*/
	}
}

var setCreditDebitVendorTaxDetail = function(data, value, i, transactionTableTr){
    var parentTr = $("#" + transactionTableTr +" table[class='multipleItemsTable'] tbody tr:last").attr('id');
    $("#"+parentTr+ " select[class='txnGstTaxRate']").removeAttr("disabled");
    $("#"+parentTr+ " select[class='txnCessRate']").removeAttr("disabled");

    $("#"+parentTr+ " select[class='txnCessRate']").find('option:first').prop("selected","selected");
    if(data.txnItemData[i].taxData){
        var	taxNameList = ""; var countTaxes =0;  var taxTypeAndRate = "";
        for(var j=0; j<data.txnItemData[i].taxData.length; j++){
            $("#"+parentTr+ " select[class='txnCessRate']").children().remove();
            if(data.txnItemData[i].taxData[j].taxName == "CESS"){
                var cessRate = data.txnItemData[i].taxData[j].taxRate;
                $("#"+parentTr+ " select[class='txnCessRate']").append('<option value="'+data.txnItemData[i].taxData[j].taxid+'">'+cessRate+'</option>');
                $("#"+parentTr+ " select[class='txnCessRate'] option:contains("+cessRate+")").attr("selected",true) ;
                $("#"+parentTr+ " input[class='txnCessTaxAmt']").val(data.txnItemData[i].taxData[j].taxAmount);
                var taxCellData = '<input type="hidden" class="taxRate" id="cdtdbvTaxRate" readonly="readonly" value="'+cessRate+'"/>';
                taxCellData+='<input type="hidden" class="txnTaxAmount" id="cdtdbvtaxamnt" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);" value="'+data.txnItemData[i].taxData[j].taxAmount+'"/>';
                taxCellData+='<input type="hidden" class="txnTaxName" id="cdtdbvTaxName" value="'+data.txnItemData[i].taxData[j].taxName+'"/>';
                taxCellData+='<input type="hidden" class="txnTaxID" id="cdtdbvTxnTaxID" value="'+data.txnItemData[i].taxData[j].taxid+'"/>';
                $("#"+parentTr+ " div[id='taxCell"+j+"']").empty();
                $("#"+parentTr+ " div[id='taxCell"+j+"']").append(taxCellData);
            }else{
                countTaxes = countTaxes + 1;
                taxNameList += '<div class="taxNameHead">'+data.txnItemData[i].taxData[j].taxName.substring(0, 4)+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
                var taxCellData = '<input type="hidden" class="taxRate" name="cdtdbvTaxRate" id="cdtdbvTaxRate" readonly="readonly" value="'+data.txnItemData[i].taxData[j].taxRate+'"/>';
                taxCellData+='<input type="text" style="width:62px;" class="txnTaxAmount" name="cdtdbvtaxamnt" id="cdtdbvtaxamnt" readonly="readonly" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateNetAmountWhenTaxAmtChangedForBuy(this);" value="'+data.txnItemData[i].taxData[j].taxAmount+'"/>';
                taxCellData+='<input type="hidden" class="txnTaxName" name="cdtdbvTaxName" id="cdtdbvTaxName" value="'+data.txnItemData[i].taxData[j].taxName+'"/>';
                taxCellData+='<input type="hidden" class="txnTaxID" name="cdtdbvTxnTaxID" id="cdtdbvTxnTaxID" value="'+data.txnItemData[i].taxData[j].taxid+'"/>';
                $("#"+parentTr+ " div[id='taxCell"+j+"']").addClass('buyTaxCellCls');
                $("#"+parentTr+ " div[id='taxCell"+j+"']").empty();
                $("#"+parentTr+ " div[id='taxCell"+j+"']").append(taxCellData);
                $("#"+parentTr+ " div[id='taxCell']").removeAttr('class', 'divbuy-w'+j+'00');
            }

            //taxTypeAndRate += (data.branchTaxDetail[i].individualTax) + "," ;
            $("#" + transactionTableTr +" table[class='multipleItemsTable'] div[id='taxNameList']").removeAttr('class', 'divbuy-w'+j+'00');
        }

        $("#"+parentTr+ " div[id='taxCell']").addClass('divbuy-w'+countTaxes+'00');
        var taxNameHeadDivCount = $("#"+transactionTableTr + ' .taxNameHead').length;
        if(data.txnItemData[i].taxData.length > parseInt(taxNameHeadDivCount)){
            $("#" + transactionTableTr +" table[class='multipleItemsTable'] div[id='taxNameList']").addClass('divbuy-w'+countTaxes+'00');
            $("#" + transactionTableTr +" table[class='multipleItemsTable'] div[id='taxNameList']").empty();
            $("#" + transactionTableTr +" table[class='multipleItemsTable'] div[id='taxNameList']").append(taxNameList);
        }

        var length = $("#"+parentTr+ " select[class='txnGstTaxRate']").children('option').length;
        $("#"+parentTr+ " select[class='txnGstTaxRate']").children().remove();
        if(parseInt(data.txnItemData[i].sgstCgstRate) !== 0){
            var gstTaxRate = parseInt(data.txnItemData[i].sgstCgstRate)+'';
            //$("#"+parentTr+ " select[class='txnGstTaxRate'] option:contains("+gstTaxRate+")").prop("selected","selected");
            /*$("#"+parentTr+ " select[class='txnGstTaxRate'] option").map(function () {
                if ($(this).text() == gstTaxRate){
                    console.log($(this).text());
                    return this;
                }
            }).prop("selected","selected");*/
            $("#"+parentTr+ " select[class='txnGstTaxRate']").append('<option value="'+gstTaxRate+'">'+gstTaxRate+'</option>');
            $("#"+parentTr+ " select[class='txnGstTaxRate'] option:contains("+gstTaxRate+")").attr("selected",true) ;

        }else{
            $("#"+parentTr+ " select[class='txnGstTaxRate']").find('option:first').prop("selected","selected");
        }
        $("#"+parentTr+ " select[class='txnGstTaxRate']").attr("disabled", "disabled");
        $("#"+parentTr+ " select[class='txnCessRate']").attr("disabled", "disabled");
    }
}

var submitForApprovalNoteTxn = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	var parentOfparentTr = "staticsellmultipleitemscdtdbt";
	var increaseDecrease = $("#" + parentTr +" select[class='creditDebitType'] option:selected").val();
	if(increaseDecrease == ""){
		swal("Incomplete transaction data!", "Please select "+whatYouWantToDo+" type", "error");
		enableTransactionButtons();
		return false;
	}
    var isValid = validateCreditDedbitNote("multipleItemsTablecdtdbt", increaseDecrease);
    if(!isValid){
        swal("Incomplete transaction data!", "Please change data before submitting for approval", "error");
        enableTransactionButtons();
        return false;
    }
	var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
	var txnForCustomer = $("#"+parentTr+" .masterList option:selected").val();
	var txnTotalNetAmount = $("#"+parentTr+" input[class='netAmountValTotal']").val();
	isValid = validateMultiItemsCreditDebit("multipleItemsTablecdtdbt");
	if(!isValid){
		swal("Incomplete transaction data!", "Please provide complete transaction details before submitting for approval", "error");
		enableTransactionButtons();
		return false;
	}
	var txnForItem = convertTableDataToArray("multipleItemsTablecdtdbt");
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
		swal("Error on Submit for approval!", "Please provide complete transaction details before submitting for approval", "error");
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
	txnJsonData.txnIncreaseDecrease = increaseDecrease;
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
			if(data.message){
				swal(data.message, "Change price/quantity as per need and retry.", "warning");
			}else{
				approvealForSingleUser(data);// Single User
				cancel(); //this will remove create transaction div
				//getUserTransactions(2, 100);
				viewTransactionData(data)
			}
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
}

var setAdditionalBuyTxnDetails= function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var txnTypeOfSupply = $("#"+parentTr+" select[class='txnTypeOfSupply'] option:selected").val();
	var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
	if(whatYouWantToDoVal == CREDIT_NOTE_VENDOR || whatYouWantToDoVal == DEBIT_NOTE_VENDOR) {
    	$(".hideResoneForReturn").removeClass('hidden');
    }else {
    	$(".hideResoneForReturn").addClass('hidden');
    }
	if(txnTypeOfSupply==""){
		swal("Incomplete transaction detail!", "Please select a Type of Supply.", "error");
		return false;
	}else if(txnTypeOfSupply == "2" || txnTypeOfSupply == "3" ){
		$("#staticBuyTxnAddDetail .currencyTr").hide();
	}else{
		$("#staticBuyTxnAddDetail .currencyTr").show();
	}

	$("#staticBuyTxnAddDetail").attr('data-toggle', 'modal');
	$("#staticBuyTxnAddDetail").modal('show');
}

var submitForApprovalVendNoteTxn = function(whatYouWantToDo, whatYouWantToDoVal, parentTr){
	var increaseDecrease = $("#"+parentTr+" select[class='creditDebitType'] option:selected").val();
	if(increaseDecrease == ""){
		swal("Incomplete transaction data!", "Please select "+whatYouWantToDo+" type", "error");
		enableTransactionButtons();
		return false;
	}
    var isValid = validateCreditDedbitNote("multipleItemsTablecdtdbv", increaseDecrease);
    if(!isValid){
        swal("Incomplete transaction data!", "Please change data before submitting for approval", "error");
        enableTransactionButtons();
        return false;
    }
	var txnForBranch = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var txnForProject = $("#"+parentTr+" select[class='txnForProjects'] option:selected").val();
	var txnForCustomer = $("#"+parentTr+" .masterList option:selected").val();
	var txnTotalNetAmount = $("#"+parentTr+" input[class='netAmountValTotal']").val();
	isValid = validateMultiItemsCreditDebit("multipleItemsTablecdtdbv");
	if(!isValid){
		swal("Incomplete transaction data!", "Please provide complete transaction details before submitting for approval", "error");
		enableTransactionButtons();
		return false;
	}
	var txnForItem = convertTableDataToArray("multipleItemsTablecdtdbv");
	var parentDiv = $("#"+parentTr).closest('div').attr('id');
	var totalTxnNetAmtWithDecimalValue = calculateTotalNetForMultipleItemsTable(this, parentDiv);
	var totalWithholdTaxAmt = calcuateTotalAmt4Element(parentDiv, 'withholdingtaxcomponenetdiv');
	var totalTxnTaxAmt = calcuateTotalAmt4Element(parentDiv,'itemTaxAmount');
	var totalTxnGrossAmt = calcuateTotalAmt4Element(parentDiv,'txnGross');
	var klfollowednotfollowed = isKnowledgeLibFollowedInMultiItems(parentDiv);

	if(txnTotalNetAmount==""){
		swal("Incomplete Transction detail!", "Please provide complete transaction details before submitting for approval.", "error");
		enableTransactionButtons();
		return false;
	}
	var txnRemarks =  $("#"+parentTr+" textarea[class='voiceRemarksClass']").val();
	var supportingDocTmp = $("#"+parentTr+" select[class='txnUploadSuppDocs'] option").map(function () {
		if($(this).val() != ""){
			return $(this).val();
		}
	}).get();
	var supportingDoc = supportingDocTmp.join(',');
	var txnBuyEntityID = $("#"+parentTr+" select[class='salesExpenseTxns']").find('option:selected').val();
	var txnEntityID = $("#"+parentTr).attr("name");
	var txnJsonData={};
	txnJsonData.txnBuyEntityID = txnBuyEntityID;
	txnJsonData.txnEntityID = txnEntityID;
	txnJsonData.txnPurpose=whatYouWantToDo;
	txnJsonData.txnPurposeVal=whatYouWantToDoVal;
    txnJsonData.txnIncreaseDecrease = increaseDecrease;
	txnJsonData.txnForBranch=txnForBranch;
	txnJsonData.txnForProject=txnForProject;
	txnJsonData.txnForItem=txnForItem;
	txnJsonData.txnForCustomer=txnForCustomer;
	txnJsonData.txnTotalNetAmount=txnTotalNetAmount;
	txnJsonData.totalTxnNetAmtWithDecimalValue = totalTxnNetAmtWithDecimalValue;
	txnJsonData.totalWithholdTaxAmt = totalWithholdTaxAmt;
	txnJsonData.totalTxnTaxAmt = totalTxnTaxAmt;
	txnJsonData.totalTxnGrossAmt = totalTxnGrossAmt;
	txnJsonData.klfollowednotfollowed = klfollowednotfollowed;
	txnJsonData.txnRemarks=txnRemarks;
	txnJsonData.supportingdoc=supportingDoc;
	txnJsonData.txnDocumentUploadRequired="false";
	txnJsonData.useremail=$("#hiddenuseremail").text();
	var url="/transaction/submitForApproval";
	setBuyTxnAdditionalDetail(txnJsonData);
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
			if(data.message){
				swal(data.message, "Change price/quantity as per need and retry.", "warning");
			}else{
				approvealForSingleUser(data);	// Single User
				cancel();
				//getUserTransactions(2, 100);
				viewTransactionData(data); // to render the updated transaction recored
			}
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
}


var generateCustCdtDbtNote = function(elem){
	$("#messageModal").html("");
	var formName=$(elem).attr('id');
	var exportType=formName.substring(0, 4);
	var transactionId=formName.substring(4, formName.length);
	var useremail=$("#hiddenuseremail").text();
	var jsonData = {};
	jsonData.usermail = useremail;
	jsonData.entityTxnId=transactionId;
	jsonData.exportType = exportType;
	var url="/exportInvoicePdf";
	downloadFile(url, "POST", jsonData, "Error on invoice generation!");
}

var validateTransctionNote = function(tableName){
	var parentTable = $("#"+tableName).parents().closest('table').attr('id');
	var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
	var isvalid = true;
	$("#" + tableName + " > tbody > tr").each(function() {
		var itemId = $(this).find("td .txnItems option:selected").val();
		if(itemId !== "" && typeof itemId!=='undefined'){
			var itemName = $(this).attr('name');
			var txnPerUnitPrice = $(this).find("td input[class='txnPerUnitPrice']").val();
			var txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
            var txnPerUnitPriceOrg = $(this).find("td input[class='txnPerUnitPriceHid']").val();
            var txnNoOfUnitOrg = $(this).find("td input[class='txnNoOfUnitHid']").val();
			if(whatYouWantToDoVal === CREDIT_NOTE_CUSTOMER || whatYouWantToDoVal === DEBIT_NOTE_VENDOR){
				if(parseFloat(txnPerUnitPriceOrg) > parseFloat(txnPerUnitPrice)) {
					txnPerUnitPrice = parseFloat(txnPerUnitPriceOrg) - parseFloat(txnPerUnitPrice);
				}else{
					isvalid = fasle;
				}
				if(parseFloat(txnNoOfUnitOrg) > parseFloat(txnNoOfUnit)) {
					txnNoOfUnit = parseFloat(txnNoOfUnitOrg) - parseFloat(txnNoOfUnit);
				}else{
                    isvalid = fasle;
                }
			}else if(whatYouWantToDoVal === CREDIT_NOTE_VENDOR || whatYouWantToDoVal === DEBIT_NOTE_CUSTOMER) {
				if(parseFloat(txnPerUnitPriceOrg) < parseFloat(txnPerUnitPrice)) {
					txnPerUnitPrice = parseFloat(txnPerUnitPriceOrg) - parseFloat(txnPerUnitPrice);
					txnPerUnitPrice += parseFloat(txnPerUnitPrice);
				}else{
                    isvalid = fasle;
                }
				if(parseFloat(txnNoOfUnitOrg) < parseFloat(txnNoOfUnit)) {
					txnNoOfUnit = parseFloat(txnNoOfUnitOrg) - parseFloat(txnNoOfUnit);
					txnNoOfUnit += parseFloat(txnNoOfUnit);
				}else{
                    isvalid = fasle;
                }
			}
		}else{
			isvalid = false;
		}
	});
	return isvalid;
}

var validateCreditDedbitNote = function(tableName, increaseDecrease){
    var isValid = false;
    var iCounter = 0;
    var jCounter = 0;
    $("#" + tableName + " > tbody > tr").each(function() {
        jCounter++;
        var jsonData = {};
        var itemId = $(this).find("td .txnItems option:selected").val();
        var grossAmt = $(this).find("td input[class='txnGross']").val();
        if(increaseDecrease == "1"){
            var txnPerUnitPrice = $(this).find("td input[class='txnPerUnitPrice']").val();
			var txnPerUnitPriceOrg = $(this).find("td input[class='txnPerUnitPriceHid']").val();
			if(parseFloat(txnPerUnitPrice) !== parseFloat(txnPerUnitPriceOrg)){
                iCounter++;
			}
        }else if(increaseDecrease == "2"){
            var txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
            var txnNoOfUnitOrg = $(this).find("td input[class='txnNoOfUnitHid']").val();
            if(parseFloat(txnNoOfUnit) !== parseFloat(txnNoOfUnitOrg)){
                iCounter++;
            }
		}
    });
    if(iCounter !== 0 && iCounter === jCounter){
        isValid = true;
	}
    return isValid;
}

var validateMultiItemsCreditDebit = function(tableName){
    var isvalid = true;
    $("#" + tableName + " > tbody > tr").each(function() {
        var itemId = $(this).find("td .txnItems option:selected").val();
        var grossAmt = $(this).find("td input[class='txnGross']").val();
        if(itemId != "" && typeof itemId!='undefined' && grossAmt != "" && typeof grossAmt!='undefined' ){
            var txnPerUnitPrice = $(this).find("td input[class='txnPerUnitPrice']").val();
            var txnNoOfUnit = $(this).find("td input[class='txnNoOfUnit']").val();
            var txnGross = $(this).find("td input[class='txnGross']").val();
            var netAmountVal = $(this).find("td input[class='netAmountVal']").val();
            if(txnPerUnitPrice == "" ||  txnNoOfUnit=="" || txnGross=="" || parseFloat(txnGross) == 0.0 || netAmountVal==""){
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
