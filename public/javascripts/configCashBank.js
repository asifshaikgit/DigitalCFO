$(document).ready(function(){
	$('#cashPettyBookSelection').on('change',function(){
		$("#selectedBookType").html("");
    	$("#selectedBookType").html($('#cashPettyBookSelection option:selected').text());
		$("#cashAndBankTable tbody").html("");
		$("#selectBranchCashBookId").val("");
		$("#selectBranchCashBankBookId option:first").prop("selected",'selected');
		$("#selectBankCashBankBookId option:first").prop("selected",'selected');
		$("#cashNBankBookFromDate").val("");
		$("#cashNBankBookToDate").val("");
		var noOfOptions = $('#cashBookDiv select[id="selectBranchCashBookId"] option').length;
		if(parseInt(noOfOptions) == 2){
			$('#cashBookDiv select[class="bnchCashnBankTrialBalance"] option:last').prop('selected', true);
		}
		else{
			$('#cashBookDiv select[class="bnchCashnBankTrialBalance"] option:first').prop('selected',true);
		}
		
	});
});

$(document).ready(function(){
	$(".searchCashBooks").on('click', function(){
		var cashNBrach=$("#selectBranchCashBookId option:selected").val();
		//var cashNBank=$("#selectBankCashBankBookId option:selected").val();
		var fromDate=$("#cashNBankBookFromDate").val();
		var toDate=$("#cashNBankBookToDate").val();
		if(cashNBrach==""){
			swal("Error!","Please Select The Branch For Which You Want Cash And Bank Statement","error");
			return true;
		}
		if(fromDate=="" || typeof fromDate=='undefined' || toDate =="" || typeof toDate=='undefined'){
			swal("Error!","Please Choose Appropriate Date Range For Cash And Bank Book Statement","error");
			return true;
		}
		if(cashNBrach!="" &&  typeof cashNBrach!='undefined' && fromDate!="" && toDate!=""){
			cashNBankBranch=cashNBrach;
			//cashNBankBank=cashNBank;
			cashNBankFromDate=fromDate;
			cashNBankToDate=toDate;
			var jsonData = {};
			jsonData.email =$("#hiddenuseremail").text();
			jsonData.bnchCashNBank=cashNBrach;
			//jsonData.bnkCashNBank=cashNBank;
			jsonData.fmDate=fromDate;
			jsonData.tDate=toDate;
			jsonData.bookType=$('#cashPettyBookSelection option:selected').val();
			ajaxCall('/cashnbank/display', jsonData, '', '', '', '', 'displayCashBookSuccess', '', true);
		}
	});
});


$(document).ready(function(){
	$(".searchBankBooks").on('click', function(){
		customMethod8();
		$("#bankStatBalance").hide();
		$("#bankReconciliation").hide();
		$("#genBankReconciliation").hide();
		$("#brsResult").html("");
		$("#bankBookTransTable tr td:nth-child(10)").hide();
		$("#bankBookTransTable tr th:nth-child(10)").hide();
		var cashNBrach=$("#selectBranchCashBankBookId option:selected").val();
		var cashNBank=$("#selectBankCashBankBookId option:selected").val();
		var fromDate=$("#bankBookFromDate").val();
		var toDate=$("#bankBookToDate").val();
		if(cashNBrach==""){
			swal("Error in data field!!","Please select a branch for which you want bank book statement.","error");
			return true;
		}

		if(cashNBank==""){
			swal("Error in data field!!","Please select a bank for which you want bank book statement.","error");
			return true;
		}

		if(fromDate=="" || typeof fromDate=='undefined' || toDate =="" || typeof toDate=='undefined'){
			swal("Error in data field!!","Please choose appropriate date range for bank book statement","error");
			return true;
		}
		if(cashNBrach!="" &&  typeof cashNBrach!='undefined' && fromDate!="" && toDate!=""){
			cashNBankBranch=cashNBrach;
			cashNBankBank=cashNBank;
			cashNBankFromDate=fromDate;
			cashNBankToDate=toDate;
			var jsonData = {};
			jsonData.email =$("#hiddenuseremail").text();
			jsonData.bnchCashNBank=cashNBrach;
			jsonData.bnkCashNBank=cashNBank;
			jsonData.fmDate=fromDate;
			jsonData.tDate=toDate;
			jsonData.bookType=2;
			ajaxCall('/cashnbank/display', jsonData, '', '', '', '', 'displayBankBookSuccess', '', true);
			$("#bankReconciliation").show();
		}
	});
});


function exportBook(bookType, exportType){
	var bookTypeTmp = $('#cashPettyBookSelection option:selected').val();
	var bookBranch = ""; var bookBank = ""; var fromDate =""; var toDate = "";
	var bookName = "Cash";
	if(bookType == "1"){
		if(bookTypeTmp == "3"){
			bookType="3"; //petty Cash
			bookName = "Pettycash";
		}

		bookBranch=$("#selectBranchCashBookId option:selected").val();
		fromDate=$("#cashNBankBookFromDate").val();
		toDate=$("#cashNBankBookToDate").val();
		if(bookBranch==""){
			swal("Error in data field!!","Please Select The Branch For Which You Want Cash And Bank Statement","error");
			return true;
		}
		if(fromDate=="" || typeof fromDate=='undefined' || toDate =="" || typeof toDate=='undefined'){
			swal("Error in data field!!","Please Choose Appropriate Date Range For Cash And Bank Book Statement","error");
			return true;
		}
	}else if(bookType == "2"){
		bookName = "Bank";
		bookBranch = $("#selectBranchCashBankBookId option:selected").val();
		bookBank=$("#selectBankCashBankBookId option:selected").val();
		fromDate=$("#bankBookFromDate").val();
		toDate=$("#bankBookToDate").val();
		if(bookBranch==""){
			swal("Error in data field!!","Please select a branch for which you want bank book statement.","error");
			return true;
		}

		if(bookBank==""){
			swal("Error in data field!!","Please select a bank for which you want bank book statement.","error");
			return true;
		}
		if(fromDate=="" || typeof fromDate=='undefined' || toDate =="" || typeof toDate=='undefined'){
			swal("Error in data field!!","Please choose appropriate date range for bank book statement","error");
			return true;
		}
	}

	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.bnchCashNBank=bookBranch;
	jsonData.bnkCashNBank=bookBank;
	jsonData.fmDate=fromDate;
	jsonData.tDate=toDate;
	jsonData.bookType=bookType;
	jsonData.exporttype=exportType;

	downloadFile('/cashnbank/export', "POST", jsonData, "Error on downloading " + bookName + " book!");
	//ajaxCall('/cashnbank/export', jsonData, '', '', '', '', 'exportSuccess', '', true);
}

function exportSuccess(data){
	if(data.result){
		window.open(data.fileName);
	}
}

function showCashBook(elem){
    var mouldesRights = $("#usermoduleshidden").val();
    showHideModuleTabs(mouldesRights);
	getBranchData();
	$("#pendingExpense").hide();
	$("#bankBookDiv").hide();
	$("#cashBookDiv").show();
	$("#trialBalance").hide();
	$('#periodicInventory').hide();
	$("#reportAllInventoryItems").hide();
	$("#reportAllInventory").hide();
	$('#reportInventory').hide();
	$("#plbsCoaMapping").hide();
	$("#profitloss").hide();
	$("#balanceSheet").hide();
	$(".bnchCashnBankTrialBalance option:first").prop('selected','selected');
	$(".bankCashnBankTrialBalance option:first").prop('selected','selected');
	$("#cashNBankBookFromDate").val("");
	$("#cashNBankBookToDate").val("");

	var cashNBrach=$("#selectBranchCashBookId option:selected").val();
	//var cashNBank=$("#selectBankCashBankBookId option:selected").val();
	var fromDate=$("#cashNBankBookFromDate").val();
	var toDate=$("#cashNBankBookToDate").val();
	cashNBankBranch=cashNBrach;
	//cashNBankBank=cashNBank;
	cashNBankFromDate=fromDate;
	cashNBankToDate=toDate;
	/*Sunil: On load cash book should not display
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.bnchCashNBank=cashNBrach;
	jsonData.fmDate=fromDate;
	jsonData.tDate=toDate;
	jsonData.bookType=$('#cashPettyBookSelection option:selected').val();
	ajaxCall('/cashnbank/display', jsonData, '', '', '', '', 'displayCashBookSuccess', '', true); */
}

function displayCashBookSuccess(data){
	$("#cashAndBankTable tbody").html("");
	if(data.result){
		for(var i=0;i<data.cashNBankData.length;i++){
			$("#cashAndBankTable tbody").append('<tr id="cashNBookDataTable'+i+'"><td>'+data.cashNBankData[i].createdDate+'</td><td>'+data.cashNBankData[i].transPurpose+'</td><td>'+data.cashNBankData[i].transLedger+'</td><td><a class="'+data.cashNBankData[i].txnLookUp+'" href="#pendingExpense" onclick="showCashNBankModal(this,\''+data.cashNBankData[i].txnReference+'\');">'+data.cashNBankData[i].incomeExpense+'</a></td><td>'+data.cashNBankData[i].debit+'</td><td>'+data.cashNBankData[i].credit+'</td><td>'+data.cashNBankData[i].balance+'</td></tr>');
		}
		/*
		$("#selectBranchCashBankBookId option:first").prop("selected",'selected');
		$("#selectBankCashBankBookId option:first").prop("selected",'selected');
		$("#cashNBankBookFromDate").val("");
		$("#cashNBankBookToDate").val("");
		*/
	}
}



function showBankBook(elem){
    var mouldesRights = $("#usermoduleshidden").val();
    showHideModuleTabs(mouldesRights);
	resetBankReconciliation(elem);
	getBranchData();
	$("#pendingExpense").hide();
	$("#cashBookDiv").hide();
	$("#bankBookDiv").show();
	$("#trialBalance").hide();
	$("#plbsCoaMapping").hide();
	$("#profitloss").hide();
	$("#balanceSheet").hide();
	$('#periodicInventory').hide();
	$("#reportAllInventoryItems").hide();
	$("#reportAllInventory").hide();
	$('#reportInventory').hide();
	$(".bnchCashnBankTrialBalance option:first").prop('selected','selected');
	$(".bankCashnBankTrialBalance option:first").prop('selected','selected');
	$("#cashNBankBookFromDate").val("");
	$("#cashNBankBookToDate").val("");

	var cashNBrach=$("#selectBranchCashBankBookId option:selected").val();
	var cashNBank=$("#selectBankCashBankBookId option:selected").val();
	var fromDate=$("#cashNBankBookFromDate").val();
	var toDate=$("#cashNBankBookToDate").val();
	cashNBankBranch=cashNBrach;
	cashNBankBank=cashNBank;
	cashNBankFromDate=fromDate;
	cashNBankToDate=toDate;
	$("#bankBookTransTable tbody tr").remove();
	/*Sunil: On load bank book should not display
	var jsonData = {};
	jsonData.email =$("#hiddenuseremail").text();
	jsonData.bnchCashNBank=cashNBrach;
	jsonData.bnkCashNBank=cashNBank;
	jsonData.fmDate=fromDate;
	jsonData.tDate=toDate;
	jsonData.bookType=2;
	ajaxCall('/cashnbank/display', jsonData, '', '', '', '', 'displayBankBookSuccess', '', true); */
}



function displayBankBookSuccess(data){
	$("#bankBookTransTable tbody").html("");
	if(data.result){
		for(var i=0;i<data.bankBookData.length;i++){
			if(i == 0){
				$("#bankBookTransTable tbody").append('<tr id="bankBookDataTable'+i+'"><td>'+data.bankBookData[i].createdDate+'</td><td>'+data.bankBookData[i].transPurpose+'</td><td>'+data.bankBookData[i].transLedger+'</td><td><a id="transDetail'+i+'" class="'+data.bankBookData[i].txnLookUp+'" href="#pendingExpense" onclick="showCashNBankModal(this,\''+data.bankBookData[i].txnReference+'\');">'+data.bankBookData[i].incomeExpense+'</a></td><td>'+data.bankBookData[i].debit+'</td><td>'+data.bankBookData[i].credit+'</td><td>'+data.bankBookData[i].balance+'</td><td>'+data.bankBookData[i].instrumentNumber+'</td><td>'+data.bankBookData[i].instrumentDate+'</td><td style="display:none;"></td></tr>');
			}else{
				$("#bankBookTransTable tbody").append('<tr id="bankBookDataTable'+i+'"><td>'+data.bankBookData[i].createdDate+'</td><td>'+data.bankBookData[i].transPurpose+'</td><td>'+data.bankBookData[i].transLedger+'</td><td><a class="'+data.bankBookData[i].txnLookUp+'" href="#pendingExpense" onclick="showCashNBankModal(this,\''+data.bankBookData[i].txnReference+'\');">'+data.bankBookData[i].incomeExpense+'</a></td><td>'+data.bankBookData[i].debit+'</td><td>'+data.bankBookData[i].credit+'</td><td>'+data.bankBookData[i].balance+'</td><td>'+data.bankBookData[i].instrumentNumber+'</td><td>'+data.bankBookData[i].instrumentDate+'</td><td style="display:none;"><input class="manualBankBookDatePicker" type="text" id="manualBankDate'+i+'" name="manualBankDate'+i+'" onblur="validateBankDate('+i+')" value="'+ data.bankBookData[i].brsBankDate+'"/><input type="hidden" id="bankBookRowData'+i+'" value="'+data.bankBookData[i].txnRefNumber+'"/></td></tr>');
			}
		}

		$(".manualBankBookDatePicker").datepicker({
			changeMonth : true,
			changeYear : true,
			dateFormat:  'MM d,yy',
			yearRange: ''+new Date().getFullYear()-1+':'+new Date().getFullYear()+'',
			onSelect: function(x,y){
				$(this).focus();
			}
		});

		//$("#selectBranchCashBankBookId option:first").prop("selected",'selected');
		//$("#selectBankCashBankBookId option:first").prop("selected",'selected');
		//$("#bankBookFromDate").val("");
		//$("#bankBookToDate").val("");
	}
}

function showCashNBankModal(elem, txnid){
	var text=$(elem).html();
	//var id=text.substring(text.indexOf('(')+1, text.indexOf(')'));
	var type=$(elem).attr('class');
	if(isEmpty(txnid)){
		swal('Data not specified!','Transaction ID not specified.','error');
	}else if(isEmpty(type)){
		swal('Data not specified!','Transaction type not specified.','error');
	}else{
		var url='/cashnbank/getData/'+txnid+'/'+type;
		ajaxCall(url, '', '', '', 'GET', '', 'displayCashNBankModal', '', true);
	}
}

function displayCashNBankModal(data){
	if(data.result){
		if(1===data.type){
			var html=transaction.txnData(data), table=$('#cashBankTransactionModal tbody');
			$(table).html(html);
			//if(data.transactionPurpose=="Sell on credit & collect payment later" || data.transactionPurpose=="Buy on credit & pay later" || data.transactionPurpose=="Pay special adjustments amount to vendors" || data.transactionPurpose=="Withdraw Cash From Bank" || data.transactionPurpose=="Deposit Cash In Bank" || data.transactionPurpose=="Transfer Funds From One Bank To Another" || data.userTxnData[i].transactionPurpose=="Transfer Inventory Item From One Branch To Another" || data.userTxnData[i].transactionPurpose=="Inventory Opening Balance"){
			if(data.transactionPurpose=="Sell on credit & collect payment later" || data.transactionPurpose=="Buy on credit & pay later"
			|| data.transactionPurpose=="Pay special adjustments amount to vendors" || data.transactionPurpose=="Withdraw Cash From Bank"
			|| data.transactionPurpose=="Deposit Cash In Bank" || data.transactionPurpose=="Transfer Funds From One Bank To Another"
			|| data.transactionPurpose=="Transfer Inventory Item From One Branch To Another" || data.transactionPurpose=="Inventory Opening Balance"
			|| data.transactionPurpose=="Prepare Quotation" || data.transactionPurpose=="Prepare Proforma Invoice" || data.txnPurposeID == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
				$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"]').hide();
			}else{
				$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"]').show();
				if(data.transactionPurpose=="Buy on Petty Cash Account" || data.transactionPurpose=="Transfer main cash to petty cash"){
					$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"] select[name="paymentDetails"] option[value="2"]').remove();
					$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="payementDiv"] select[name="paymentDetails"] option[value="1"]').prop("selected","selected");
    			}
			}
			if(data.netAmtDesc!=null && data.netAmtDesc!=""){
		    	 var individualNetDesc=data.netAmtDesc.substring(0,data.netAmtDesc.length).split(',');
		    	 for(var m=0;m<individualNetDesc.length;m++){
		    		 var labelAndFigure=individualNetDesc[m].substring(0, individualNetDesc[m].length).split(':');
						$(table).find($('#transactionEntity'+data.id+' div[class="netResultCalcDesc"]')).append('<font color="FF00FF"><b>'+labelAndFigure[0]+'</b></p><br/>');
		    		 if(typeof labelAndFigure[1]!='undefined'){
						$(table).find($('#transactionEntity'+data.id+' div[class="netResultCalcDesc"]')).append('<p style="color: blue;">'+labelAndFigure[1]+'</p><br/>');
		    		 }
		    	 }
		    }
		    if(data.txnRemarks!=null && data.txnRemarks!=""){
			   	var individualRemarks=data.txnRemarks.substring(0,data.txnRemarks.length).split(',');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$(table).find($('#transactionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]')).append('<font color="FF00FF"><b>'+emailAndRemarks[0]+'</b></p>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		if(emailAndRemarks[0].indexOf("Auditor")!=-1){
			    			$(table).find($('#transactionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]')).append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
			    		}else {
			    			$(table).find($('#transactionEntity'+data.userTxnData[i].id+' div[class="txnWorkflowRemarks"]')).append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
			    		}
			    	}
			    }
			}
		    if(data.roles.indexOf("APPROVER")!=-1 || data.roles.indexOf("ACCOUNTANT")!=-1 || data.roles.indexOf("AUDITOR")!=-1){
		    	//data.transactionPurpose=="Sell on cash & collect payment now" ||  data.transactionPurpose=="Sell on credit & collect payment later" ||
		    	//not needed for sell as bug raised IDOSWORK-14
		    	if(data.transactionPurpose=="Buy on cash & pay right away" || data.transactionPurpose=="Buy on credit & pay later" || data.transactionPurpose=="Buy on Petty Cash Account"){
		    		$(table).find($('#transactionEntity'+data.id+' div[class="outstandings"]')).show();
		    	}
		    }
		    if(data.txnDocument!="" && data.txnDocument!=null){
	    		var txndocument=data.txnDocument;
				var transTrID = 'transactionEntity'+data.id;
				fillSelectElementWithUploadedDocs(txndocument, transTrID, 'fileDownload');
		    }
		    if(data.txnSpecialStatus=="Transaction Exceeding Budget & Rules Not Followed"){
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
		    	//$(table).find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/blue.png"></img>');
				$(table).find('tr[id="transactionEntity'+data.id+'"] td:nth-child(2)').css("background-color", "#fdc893");
		    }else if(data.txnSpecialStatus=="Transaction Exceeding Budget"){
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
		    	//$(table).find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/red.png"></img>');
				$(table).find('tr[id="transactionEntity'+data.id+'"] td:nth-child(2)').css("background-color", "#fd9393");
		    }else if(data.txnSpecialStatus=="Rules Not Followed"){
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
		    	//$(table).find('tr[id="transactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/green.png"></img>');
				$(table).find('tr[id="transactionEntity'+data.id+'"] td:nth-child(2)').css("background-color", "#ffff63");
		    }else if(data.status!="" && data.status=="Rejected"){
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
		    }else if(data.status!="" && data.status=="Accounted"){
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstat');
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
		    }else if(data.status!="" && (data.status=="Approved" || data.status=="Require Approval" || data.status=="Require Additional Approval")){
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
		    	$(table).find('tr[id="transactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
		    }

		    if(data.transactionPurpose=="Sell on credit & collect payment later" || data.transactionPurpose=="Sell on cash & collect payment now"
				|| data.transactionPurpose=="Prepare Quotation" || data.transactionPurpose=="Prepare Proforma Invoice"){
		    	$("#transactionEntity"+data.id+" p[class='txnItemDesc']").html("");
				$("#transactionEntity"+data.id+" p[class='txnItemDesc']").append('<input type="button" value="Items List" id="multiSellItemsList" class="btn btn-submit btn-idos" onclick="listMultiSellItems(this);">');
		    }

		    $("#cashBankTransactionModal #txnRefNumber").html(data.transactionRefNumber);
		    $("#cashBankTransactionModal").attr('data-toggle', 'modal');
	    	$("#cashBankTransactionModal").modal('show');
//	    	$(".staticdashboardaggregatedataclose").attr("href",location.hash);
		}else if(3===data.type){
			var html=transaction.txnData(data),table=$('#cashBankTransactionModal tbody');
			$(table).html(html);
			if(data.additionalapproverEmails!=null && data.additionalapproverEmails!=""){
		    	var additionalApprovarUsersList=data.additionalapproverEmails.substring(0,data.additionalapproverEmails.length).split(',');
		    	$("tr[id='transactionProvisionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="">--Please Select--</option>');
		    	for(var j=0;j<additionalApprovarUsersList.length;j++){
		    		$("tr[id='transactionProvisionEntity"+data.id+"'] select[id='userAddApproval']").append('<option value="'+additionalApprovarUsersList[j]+'">'+additionalApprovarUsersList[j]+'</option>');
		    	}
		    }

			/*if(data.itemName!=null && data.itemName!=""){
				var individualitemName=data.itemName.substring(0, data.itemName.length).split('|');
				if(individualitemName.length > 0){
					$('#transactionProvisionEntity'+data.id+' div[class="provisionItemName"]').append('<b style="color: #102E55;">Debit: </b>');
					$('#transactionProvisionEntity'+data.id+' div[class="provisionItemName"]').append('<p style="color: blue;">'+individualitemName[0]+'</p>');
					$('#transactionProvisionEntity'+data.id+' div[class="provisionItemName"]').append('<b style="color: #102E55;">Credit: </b>');
					if(typeof individualitemName[1]!='undefined'){
						$('#transactionProvisionEntity'+data.id+' div[class="provisionItemName"]').append('<p style="color: blue;">'+individualitemName[1]+'</p>');
					}
				}
			}*/
			$('#transactionProvisionEntity'+data.id+' div[class="provisionItemName"]').append('<p class="txnItemDesc"><a onclick="getProvisionJournalTransactionDetails(this);">Items List</a></p><br>');

		    if(data.txnRemarks!=null && data.txnRemarks!=""){
			   	var individualRemarks=data.txnRemarks.substring(0,data.txnRemarks.length).split(',');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$(table).find($('#transactionProvisionEntity'+data.id+' div[class="txnWorkflowRemarks"]')).append('<font color="FF00FF"><b>'+emailAndRemarks[0]+'</b></p>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		if(emailAndRemarks[0].indexOf("Auditor")!=-1){
			    			$(table).find($('#transactionProvisionEntity'+data.id+' div[class="txnWorkflowRemarks"]')).append('<h1auditor>'+emailAndRemarks[1]+'</h1auditor><br/>');
			    		}else{
			    			$(table).find($('#transactionProvisionEntity'+data.id+' div[class="txnWorkflowRemarks"]')).append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
			    		}
			    	}
			    }
			}
		    if(data.txnDocument!="" && data.txnDocument!=null){
	    		
    			var rowTxnId=data.id;
    			$(table).find($("tr[id='transactionProvisionEntity"+data.id+"'] select[id='fileDownload']")).children().remove();
    			$(table).find($("tr[id='transactionProvisionEntity"+data.id+"'] select[id='fileDownload']")).append('<option value="">--Please Select--</option>');
			    /*var txndocument=data.txnDocument;
			    var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
			    for(var j=0;j<fileURLWithUser.length;j++){
			    	var fileURLWithoutUser=fileURLWithUser[j].substring(0, fileURLWithUser[j].length).split('#');
			    	var transTrID = 'transactionProvisionEntity'+data.id;
					retrieveFile(fileURLWithoutUser[0], fileURLWithoutUser[1], transTrID, 'fileDownload');
			    }*/

			    if(data.txnDocument!="" && data.txnDocument!=null){
	    			var txndocument=data.txnDocument;
					var transTrID = 'transactionEntity'+data.id;
					fillSelectElementWithUploadedDocs(txndocument, transTrID, 'fileDownload');
		    	}
    		}
		    if(data.status!="" && data.status=="Rejected"){
		    	$(table).find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
		    	$(table).find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
		    	$(table).find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
		    }else if(data.status!="" && data.status=="Accounted"){
		    	$(table).find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstat');
		    	$(table).find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
		    	$(table).find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
		    }else if(data.status!="" && (data.status=="Approved" || data.status=="Require Approval" || data.status=="Require Additional Approval")){
		    	$(table).find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
		    	$(table).find('tr[id="transactionProvisionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
		    	$(table).find('tr[id="transatransactionProvisionEntityctionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
		    }
		    $("#cashBankTransactionModal #txnRefNumber").html(data.transactionRefNumber);
		    $("#cashBankTransactionModal").attr('data-toggle', 'modal');
	    	$("#cashBankTransactionModal").modal('show');
	    	//$(".staticdashboardaggregatedataclose").attr("href",location.hash);
		}else if(2===data.type){
			var html=transaction.claimTxnData(data),table=$('#cashBankClaimTransactionModal tbody');
			$(table).html(html);
			if(data.txnPurposeId=='15' || data.txnPurposeId=='17' || data.txnPurposeId=='18' || data.txnPurposeId=='19'){
				if(data.claimtravelDetailedConfDescription!=null && data.claimtravelDetailedConfDescription!=""){
				   	var individualclaimtravelDetailedConfDescription=data.claimtravelDetailedConfDescription.substring(0,data.claimtravelDetailedConfDescription.length).split('#');
				   	for(var m=0;m<individualclaimtravelDetailedConfDescription.length;m++){
				    	if(m>0){
				    		var labelAndValue=individualclaimtravelDetailedConfDescription[m].substring(0, individualclaimtravelDetailedConfDescription[m].length).split(':');
				    		$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]')).append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
				    			$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]')).append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			    //logic for separation transaction advance eligibility separately into advance eligibility label and advance eligibility figure
			    if(data.claimuserAdvanveEligibility!=null && data.claimuserAdvanveEligibility!=""){
				   	var individualclaimuserAdvanveEligibility=data.claimuserAdvanveEligibility.substring(0,data.claimuserAdvanveEligibility.length).split('#');
				   	for(var m=0;m<individualclaimuserAdvanveEligibility.length;m++){
				   		if(m>0){
				    		var labelAndValue=individualclaimuserAdvanveEligibility[m].substring(0, individualclaimuserAdvanveEligibility[m].length).split(':');
				    		$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]')).append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
				    			$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]')).append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			    if(data.txnPurposeId=='15' || data.txnPurposeId=='17' || data.txnPurposeId=='19'){
				    if(data.txnSpecialStatus=="Rules Not Followed"){
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').html("");
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] b[class="txnSpecialStatus"]').append('<img src="assets/images/green.png"></img>');
				    }else if(data.claimTxnStatus!="" && data.claimTxnStatus=="Rejected"){
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatred');
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatred');
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatred');
				    }else if(data.claimTxnStatus!="" && data.claimTxnStatus=="Accounted"){
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstat');
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstat');
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstat');
				    }else if(data.claimTxnStatus!="" && (data.claimTxnStatus=="Approved" || data.claimTxnStatus=="Require Approval" || data.claimTxnStatus=="Require Additional Approval") || data.claimTxnStatus=="Require Accounting"){
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstat"]').attr('class','txnstatgreen');
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatgreen"]').attr('class','txnstatgreen');
				    	$(table).find('tr[id="claimsTransactionEntity'+data.id+'"] div[class="txnstatred"]').attr('class','txnstatgreen');
				    }
				    if(data.additionalApprovarUsers!=null){
					    if(typeof data.additionalApprovarUsers!='undefined'){
					    	var additionalApprovarUsersList=data.additionalApprovarUsers.substring(0,data.additionalApprovarUsers.length).split(',');
					    	$(table).find($("tr[id='claimsTransactionEntity"+data.id+"'] select[id='userAddApproval']")).append('<option value="">--Please Select--</option>');
					    	for(var k=0;k<additionalApprovarUsersList.length;k++){
					    		$(table).find($("tr[id='claimsTransactionEntity"+data.id+"'] select[id='userAddApproval']")).append('<option value="'+additionalApprovarUsersList[k]+'">'+additionalApprovarUsersList[k]+'</option>');
					    	}
					    }
					}
			    }
			}else if(data.txnPurposeId=='16'){
				if(data.existingClaimsCurrentSettlementDetails!=null && data.existingClaimsCurrentSettlementDetails!=""){
				   	var individualexistingClaimsCurrentSettlementDetails=data.existingClaimsCurrentSettlementDetails.substring(0,data.existingClaimsCurrentSettlementDetails.length).split('#');
				   	for(var m=0;m<individualexistingClaimsCurrentSettlementDetails.length;m++){
				    	if(m>0){
				    		var labelAndValue=individualexistingClaimsCurrentSettlementDetails[m].substring(0, individualexistingClaimsCurrentSettlementDetails[m].length).split(':');
				    		$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]')).append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
				    			$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimtravelDetailedConfDescriptionDiv"]')).append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			    //logic for separation of user expenditure on the travel claim transaction separatly into label and figure
			    if(data.userExpenditureOnThisTxn!=null && data.userExpenditureOnThisTxn!=""){
				   	var individualuserExpenditureOnThisTxn=data.userExpenditureOnThisTxn.substring(0,data.userExpenditureOnThisTxn.length).split('#');
				   	for(var m=0;m<individualuserExpenditureOnThisTxn.length;m++){
				   		if(m>0){
				    		var labelAndValue=individualuserExpenditureOnThisTxn[m].substring(0, individualuserExpenditureOnThisTxn[m].length).split(':');
				    		$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]')).append('<font color="black"><b>'+labelAndValue[0]+'</b></p>:<br/>');
				    		if(typeof labelAndValue[1]!='undefined'){
				    			$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimuserAdvanveEligibilityDiv"]')).append('<p style="color: blue;">'+labelAndValue[1]+'</p><br/>');
					    	}
				    	}
				    }
				}
			}
			//logic for separation transaction remarks separately into useremail and remarks made by them
		    if(data.claimtxnRemarks!=null && data.claimtxnRemarks!=""){
			   	var individualRemarks=data.claimtxnRemarks.substring(0,data.claimtxnRemarks.length).split(',');
			   	for(var m=0;m<individualRemarks.length;m++){
			    	var emailAndRemarks=individualRemarks[m].substring(0, individualRemarks[m].length).split('#');
			    	$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]')).append('<font color="#FFOFF"><b>'+emailAndRemarks[0]+'</b></p>#');
			    	if(typeof emailAndRemarks[1]!='undefined'){
			    		$(table).find($('#claimsTransactionEntity'+data.id+' div[class="claimtxnWorkflowRemarks"]')).append('<p style="color: blue;">'+emailAndRemarks[1]+'</p><br/>');
			    	}
			    }
			}
		  //logic for separation transaction remarks separately into useremail and documents uploaded by them
		    if(data.claimsupportingDoc!="" && data.claimsupportingDoc!=null){
		    	$(table).find($("tr[id='claimsTransactionEntity"+data.id+"'] select[id='claimfileDownload']")).children().remove();
		    	$(table).find($("tr[id='claimsTransactionEntity"+data.id+"'] select[id='claimfileDownload']")).append('<option value="">--Please Select--</option>');
		    	/*var txndocument=data.claimsupportingDoc;
		    	var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
		    	for(var k=0;k<fileURLWithUser.length;k++){
		    		var fileURLWithoutUser=fileURLWithUser[k].substring(0, fileURLWithUser[k].length).split('#');
		    		var transTrID = 'claimsTransactionEntity'+data.id;
	    		    retrieveFile(fileURLWithoutUser[0], fileURLWithoutUser[1], transTrID, 'claimfileDownload');
		    	}*/

		    	if(data.txnDocument!="" && data.txnDocument!=null){
	    			var txndocument=data.txnDocument;
					var transTrID = 'transactionEntity'+data.id;
					fillSelectElementWithUploadedDocs(txndocument, transTrID, 'fileDownload');
		    	}
	    	}
		    $("#cashBankClaimTransactionModal #claimTxnRefNumber").html(data.transactionRefNumber);
			$("#cashBankClaimTransactionModal").attr('data-toggle', 'modal');
	    	$("#cashBankClaimTransactionModal").modal('show');
		}
	}else{
		swal("Error!",data.message,"error");
	}
}

var transaction={
	claimTxnData:function(data){
		var html='<tr id="claimsTransactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/><b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.txnQuestionName+'</p><br/><b>'+data.creatorLabel+'</b><br/><p style="color: blue;">'+data.createdBy+'</p></td>'+
		'<td><b class="txnSpecialStatus"></b><br/><b>Transaction Purpose:</b><br/><p style="color: blue;">'+data.txnQuestionName+'</p><br/><b>Travel Type:</b><br/><p style="color: blue;">'+data.claimtravelType+'</p>'+
		'<br/><b>Number Of Places Visited:</b><br/><p style="color: blue;">'+data.claimnoOfPlacesToVisit+'</p><br/><b>Places Visited:</b><br/><p style="color: blue;">'+data.claimplacesSelectedOrEntered+'</p>'+
		'</td><td><div class="claimTravelDetConf" style="height:223px;overflow:auto;"><b>Type Of City:</b><br/><p style="color: blue;">'+data.claimtypeOfCity+'</p><br/><b>Approximate Distance:</b><br/><p style="color: blue;">'+data.claimappropriateDiatance+'</p><br/><b>Total Days(Excluding Days Of Travel):</b><br/><p style="color: blue;">'+data.claimtotalDays+'</p><div class="claimtravelDetailedConfDescriptionDiv"></div></div></td>'+
		'<td><p style="color: blue;">'+data.transactionDate+'</p></td><td><div class="advanceDetConf" style="height:220px;overflow:auto;"><b>Existing Advance:</b><br/><p style="color: blue;">'+data.claimexistingAdvance+'</p><div class="claimuserAdvanveEligibilityDiv"></div><b>Adjusted Advance:</b><p style="color: blue;">'+data.claimadjustedAdvance+'</p><br/><b>Entered Advance:</b><br/><p style="color: blue;">'+data.claimenteredAdvance+'</p><br/><b>Total Advance:</b><br/><p style="color: blue;">'+data.claimtotalAdvance+'</p>'+
		'</div></td><td><b>Purpose Of Visit:</b><br/><p style="color: blue;">'+data.claimpurposeOfVisit+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</p></td><td><div class="txnstat">'+data.claimTxnStatus+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approvedBy+'</p><br/><b>'+data.accountedLabel+'</b><br/><p style="color: blue;">'+data.accountedBy+'</p></td><td><select name="claimfileDownload" id="claimfileDownload" onchange="getFile(this);"><option value="">-Please Select-</option></select></td>'+
		'<td><div class="claimtxnWorkflowRemarks" style="height: 185px;overflow: auto;"></div></td></td></tr>';
		return html;
	},
	txnData:function(data){
		var html='';
		if(data.transactionPurpose!="Make Provision/Journal Entry"){
			var loggedUser=$("#hiddenuseremail").text();
			if(data.createdBy==loggedUser){
				if(data.status=="Approved"){
					html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
					'<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><br/><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
					'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td style="color: blue;">'+data.paymentMode+'</td><td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
					'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;"></div><div class="outstandings" style="margin-top: 40px;display:none;"></div></td><td><div class="txnstat">'+data.status+'</div>'+
					'<div class="payementDiv"></div><br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></td>'+
					'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
					'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
				}else{
					if(data.status=='Require Clarification'){
						html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><br/><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td style="color: blue;">'+data.paymentMode+'</td>'+
						'<td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td><td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/>'+
						'<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"></div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div>'+
						'<br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div><div class="invoiceForm"></div></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></div></td></tr>';
					}else{
						html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><br/><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td>'+
						'<td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td><td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/>'+
						'<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"></div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div><div class="invoiceForm"></div></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></td></tr>';
					}
				}
			}
			if(data.approverEmails!=null && data.approverEmails!=""){//for approver can be same user or not
				if(data.approverEmails.indexOf(data.useremail)!=-1){
					//check for user mail existence in the approver usermail list sent by server
					//based on transaction status row data is displayed
					if(data.status=="Require Approval" || data.status=='Clarified'){
						html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p><br/><b>BUDGET:</b><br/><b>'+data.budgetAllocated+'</b><br/><p style="color: blue;">'+data.budgetAllocatedAmt+'</p><br/><b>'+data.budgetAvailable+'</b><br/><p style="color: blue;">'+data.budgetAvailableAmt+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td>'+data.paymentMode+'</td><td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 125px;overflow: auto;"></div><div class="outstandings" style="margin-top: 50px;display:none;"></div></td><td><div class="txnstat">'+data.status+'</div>'+
						'<br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
					}else{
						 html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						 '<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><br/><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						 '<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td  style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td>'+
						 '<td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td><td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/>'+
						 '<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"></div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div></td>'+
						 '<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						 '<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></td></tr>';
					}
				}
			}
			if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
				if(data.selectedAdditionalApproval==data.useremail){
					if(data.status=='Require Additional Approval'){
						html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><br/><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p><br/><b>'+data.budgetAllocated+'</b><br/><p style="color: blue;">'+data.budgetAllocatedAmt+'</p><br/><b>'+data.budgetAvailable+'</b><br/><p style="color: blue;">'+data.budgetAvailableAmt+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td>'+data.paymentMode+'</td><td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 125px;overflow: auto;"></div><div class="outstandings" style="margin-top: 50px;display:none;"></div></td><td><div class="txnstat">'+data.status+'</div>'+
						'<br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
					}else{
						html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><br/><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td  style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td>'+
						'<td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td><td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/>'+
						'<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"></div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></td></tr>';
					}
				}
			}else{
				var roles=data.roles;
				if(roles.indexOf("ACCOUNTANT")!=-1 || roles.indexOf("AUDITOR")!=-1 || roles.indexOf("CONTROLLER")!=-1){
					var approverEmailVal=false;var selectedAdditionalApproval=false;
					 if(data.approverEmails!=null && data.approverEmails!=""){
						 if(data.approverEmails.indexOf(data.useremail)!=-1){
							 approverEmailVal=true;
						 }
					 }
					 if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
							if(data.selectedAdditionalApproval==data.useremail){
								selectedAdditionalApproval=true;
							}
					 }
					 if(data.createdBy!=loggedUser && approverEmailVal==false && selectedAdditionalApproval==false){
						html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><br/><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td  style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td>'+
						'<td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td><td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/>'+
						'<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"></div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div><div class="invoiceForm"></div></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></td></tr>';
					 }
				}else{
					html='<tr id="transactionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
					'<b>PROJECT:</b><br/><p style="color: blue;">'+data.projectName+'</p><br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><b class="txnSpecialStatus"></b><br/><p class="txnItemDesc">'+data.itemName+'</p><br/><b>IMMEDIATE PARENT:</b><br/><p style="color: blue;">'+data.itemParentName+'</p></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
					'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><div class="vendorInvDateDiv"><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></div></td><td  style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td>'+
					'<td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td><td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/>'+
					'<b>CALCULATION/DESCRIPTION:</b></br><div class="netResultCalcDesc" style="height: 100px;overflow: auto;"></div><div class="outstandings" style="display:none;"></div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div></td>'+
					'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
					'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></td></tr>';
				}
			}

		}
		if(data.transactionPurpose=="Make Provision/Journal Entry"){
			var loggedUser=$("#hiddenuseremail").text();
			if(data.createdBy==loggedUser){
				if(data.status=='Approved'){
					html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
					'<br/><b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td><td><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
					'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td>'+data.paymentMode+'</td><td><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
					'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div><br/></td><td><div class="txnstat">'+data.status+'</div>'+
					'<b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></td>'+
					'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
					'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
				}else{
					if(data.status=='Require Clarification'){
						html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td><td><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td style="color: blue;">'+data.paymentMode+'</td><td><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div><br/></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div>'+
						'<br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p><div class="invoiceForm"></div></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
					}else{
						html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td><td><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td><td><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div><br/></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div><div class="invoiceForm"></div></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
					}
				}
			}
			//for approver can be same user or not
			if(data.approverEmails!=null && data.approverEmails!=""){
				if(data.approverEmails.indexOf(data.useremail)!=-1){
					if(data.status=='Require Approval' || data.status=='Clarified'){
						//check for user mail existence in the approver usermail list sent by server
						//based on transaction status row data is displayed
						html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td>'+
						'<td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td>'+data.paymentMode+'</td><td><b>UNITS:</b><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></td><td><div class="txnstat">'+data.status+'</div>'+
						'<br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 220px;overflow: auto;"></div></td></tr>';
					}else{
						html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td><td><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td><td><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
					}
				}
			}
			if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
				if(data.selectedAdditionalApproval==data.useremail){
					if(data.status=='Require Additional Approval'){
						html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td><td><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td>'+data.paymentMode+'</td><td><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></td><td><div class="txnstat">'+data.status+'</div>'+
						'<br/><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 220px;overflow: auto;"></div></td></tr>';
					}else{
						html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						'<b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td><td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div></td>'+
						'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
					}
				}
			}else{
				var roles=data.roles;
				if(roles.indexOf("ACCOUNTANT")!=-1 || roles.indexOf("AUDITOR")!=-1 || roles.indexOf("CONTROLLER")!=-1){
					var approverEmailVal=false;var selectedAdditionalApproval=false;
					 if(data.approverEmails!=null && data.approverEmails!=""){
						 if(data.approverEmails.indexOf(data.useremail)!=-1){
							 approverEmailVal=true;
						 }
					 }
					 if(data.selectedAdditionalApproval!=null && data.selectedAdditionalApproval!=""){
							if(data.selectedAdditionalApproval==data.useremail){
								selectedAdditionalApproval=true;
							}
					 }
					 if(data.createdBy!=loggedUser && approverEmailVal==false && selectedAdditionalApproval==false){
						 html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
						 '<b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
						 '<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td><td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
						 '<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div></td>'+
						 '<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
						 '<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
					 }
				}else{
					html='<tr id="transactionProvisionEntity'+data.id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.branchName+'</p><br/>'+
					'<b>CREATOR:</b><br/><p style="color: blue;">'+data.createdBy+'</p></td><td><div class="provisionItemName"></div></td><td><b>NAME</b><br/><p style="color: blue;">'+data.customerVendorName+'</p><br/><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.transactionPurpose+'</p></td>'+
					'<td><b>CREATED DATE:</b><br/><p style="color: blue;">'+data.txnDate+'</p><br/><b>'+data.invoiceDateLabel+'</b><br/><p style="color: blue;">'+data.invoiceDate+'</p></td><td style="color: blue;">'+data.paymentMode+'<br>'+data.instrumentNumber+'<br>'+data.instrumentDate+'</td><td><b>UNITS:</b><br/><p style="color: blue;">'+data.noOfUnit+'</p><br/><b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.grossAmount+'</p></td>'+
					'<td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.netAmount+'</p><br/><b>PROVISION PURPOSE:</b></br><div class="netResultCalcDesc" style="height: 130px;overflow: auto;">'+data.netAmtDesc+'</div></td><td><div style="height: 180px;overflow: auto;"><div class="txnstat">'+data.status+'</div><b>'+data.approverLabel+'</b><br/><p style="color: blue;">'+data.approverEmail+'</p></div></td>'+
					'<td><select name="fileDownload" id="fileDownload" onchange="getFile(this);"><option value="">--Please Select--</option></select></td>'+
					'<td><div class="txnWorkflowRemarks" style="height: 150px;overflow: auto;"></div></td></tr>';
				}
			}
		}
		return html;
	}
};

$(document).ready(function(){
	$('.cashBankModalClose').on('click',function(){
		$(".modal:visible").attr('data-toggle', 'modal');
    	$(".modal:visible").modal('hide');
	});

	$('.bankReconciliation').on('click',function(){
		$("#bankBookTransTable tr td:nth-child(10)").show();
		$("#bankBookTransTable tr th:nth-child(10)").show();
		$("#bankStatBalance").show();
		$("#bankReconciliation").show();
		$("#genBankReconciliation").show();
	});

	$('.genBankReconciliation').on('click',function(){
		var cashNBrach=$("#selectBranchCashBankBookId option:selected").val();
		var cashNBank=$("#selectBankCashBankBookId option:selected").val();
		var fromDate=$("#bankBookFromDate").val();
		var toDate=$("#bankBookToDate").val();
		if(cashNBrach==""){
			swal("Error in data field!!","Please select a branch for which you want bank book statement.","error");
			return true;
		}

		if(cashNBank==""){
			swal("Error in data field!!","Please select a bank for which you want bank book statement.","error");
			return true;
		}


		if(fromDate=="" || typeof fromDate=='undefined' || toDate =="" || typeof toDate=='undefined'){
			swal("Error in data field!!","Please choose appropriate date range for bank book statement","error");
			return true;
		}

		var bankStatBalance=$("#bankStatBalance").val();
		if(bankStatBalance==""){
			swal("Error in data field!!","Please enter bank statement balance.","error");
			return true;
		}
		$("#brsResult").html("");
		var numberOfrows = $("#bankBookTransTable tr").length;
		var manualBankDate = ""; var creditVal=""; var debitVal=""; transactionList="";

	/*	$('#bankBookTransTable tbody tr td:nth-child(1) a').each(function() {
			transactionList += transactionList+$(this).html() + "|";
		});*/

		var table=$("#bankBookTransTable tbody");
		for(var i=1; i<numberOfrows-1; i++){
			transactionList +=  $(table).find('tr[id="bankBookDataTable'+i+'"] td:nth-child(1) a').html() + "|";
			manualBankDate= manualBankDate + $("#manualBankDate"+i).val() + "|";
			creditVal=creditVal + $(table).find('tr[id="bankBookDataTable'+i+'"] td:nth-child(5)').html() + "|";
			debitVal=debitVal + $(table).find('tr[id="bankBookDataTable'+i+'"] td:nth-child(6)').html() + "|";
		}
		i--;
		var balance=$(table).find('tr[id="bankBookDataTable'+i+'"] td:nth-child(7)').html();

		var tmpManualDate = manualBankDate.replace(/[|]/g,'');
		if(tmpManualDate == ""){
			swal("Error in data field!!","Bank date is not set for any transactions.","error");
			return true;
		}
		var jsonData = {};
		jsonData.manualBankDate = manualBankDate.substring(0, manualBankDate.length-1);
		jsonData.creditVal = creditVal.substring(0, creditVal.length-1);
		jsonData.debitVal = debitVal.substring(0, debitVal.length-1);
		jsonData.balance = balance;
		jsonData.toDate = toDate;
		jsonData.transactionList = transactionList.substring(0, transactionList.length-1);
		var url="/cashnbank/bankReconciliation";
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
				$("#brsResult").append('<b>Credit amount not reflected in bank: </b><a href="#pendingExpense" onclick="displayCreditDebitModal(1);">'+data.creditTotalforBRS +'</a>');
				$("#brsResult").append('<br><b>Debit amount not reflected in bank: </b><a href="#pendingExpense" onclick="displayCreditDebitModal(2);">'+data.debitTotalforBRS+'</a>');
				$("#brsResult").append("<br><b>Balance as per Bank: </b>" + data.derivedBalance);
				$("#brsResult").append('<button id="resetBankReconciliation" onclick="resetBankReconciliation(this);" style="margin-right:5px; float: right;" class="btn btn-submit btn-idos" title="Reset Bank Reconciliation"><i class="fa fa-refresh" aria-hidden="true"></i>Reset</button>');
				$("#brsResult").append('<button id="saveBankDate" onclick="saveBankDate();" style="margin-right:5px; float: right;" class="btn btn-submit" title="Save/Update Bank Dates"><i class="fa fa-floppy-o pr-5"></i>Save Bank Dates</button>');

				for(var j=0; j < data.creditBRSData.length; j++){
					$("#bankBookCreditTable tbody").append('<tr><td>'+data.creditBRSData[j].transactionDetail+'</td><td>'+data.creditBRSData[j].creditAmount+'</td></tr>');
				}
				for(var j=0; j < data.debitBRSData.length; j++){
					$("#bankBookDebitTable tbody").append('<tr><td>'+data.debitBRSData[j].transactionDetail+'</td><td>'+data.debitBRSData[j].debitAmount+'</td></tr>');
				}

				if(data.derivedBalance != bankStatBalance){
					swal("Error!","There is difference In bank balance as per statement, Please pass necessary journal entries and Reconcile","error");
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

function displayCreditDebitModal(creditOrDebit){
	if(creditOrDebit == 1){
		$("#bankBookCreditModal").modal('show');
	}else if(creditOrDebit == 2){
		$("#bankBookDebitModal").modal('show');
	}
}


function saveBankDate(){
	var numberOfrows = $("#bankBookTransTable tr").length;
	var transactionRefList = ""; var manualBankDate="";
	var table=$("#bankBookTransTable tbody");
	for(var i=1; i<numberOfrows-1; i++){
		transactionRefList = transactionRefList + $("#bankBookRowData"+i).val() + "|";
		manualBankDate= manualBankDate + $("#manualBankDate"+i).val() + "|";
	}
	var jsonData = {};
	jsonData.transactionRefList = transactionRefList.substring(0, transactionRefList.length-1);
	jsonData.manualBankDate = manualBankDate.substring(0, manualBankDate.length-1);
	var url="/cashnbank/saveTransactionBankDates";
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
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}


function validateBankDate(rowNum){
	var table=$("#bankBookTransTable tbody");
	var instrumentDate=$(table).find('tr[id="bankBookDataTable'+rowNum+'"] input[id="manualBankDate'+rowNum+'"]').val();
	var transactionDate=$(table).find('tr[id="bankBookDataTable'+rowNum+'"] td:nth-child(2)').html();
	var bankDate= $("#manualBankDate"+rowNum).val();
	var jsonData = {};
	jsonData.manualBankDate = bankDate;
	jsonData.instrumentDate = instrumentDate;
	jsonData.transactionDate = transactionDate;
	var url="/cashnbank/validateBankDate";
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
			if(data.isValidBankDate == "false" || data.isValidBankDate == ""){
				swal("Error in data field!!","Bank Date for the transaction is invalid","error");
				$("#manualBankDate"+rowNum).val("");
				$("#manualBankDate"+rowNum).focus();
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}



function resetBankReconciliation(element){
	var elementID = $(element).attr('id');

	$("#bankBookTransTable tr td:nth-child(10)").hide();
	$("#bankBookTransTable tr th:nth-child(10)").hide();
	$("#bankStatBalance").hide();
	$("#bankReconciliation").hide();
	$("#genBankReconciliation").hide();
	$("#brsResult").html("");
	$("#bankBookFromDate").val("");
	$("#bankBookToDate").val("");
	if(elementID == "selectBranchCashBankBookId"){
		$("#selectBankCashBankBookId option:first").prop("selected",'selected');
	}else if(elementID == "selectBankCashBankBookId"){

	}else{
		$("#selectBranchCashBankBookId option:first").prop("selected",'selected');
		$("#selectBankCashBankBookId option:first").prop("selected",'selected');

	}
}

function resetCashReconciliation(element){
	$("#cashNBankBookFromDate").val("");
	$("#cashNBankBookToDate").val("");
}


