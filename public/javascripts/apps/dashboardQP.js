
var displayQuotationProformaBranchBy = function(bnchName, branchId, amountValue, requirements, additionalParameter){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.currDashboardFromDate = $("#currDashboardFromDate").val();
	jsonData.currDashboardToDate = $("#currDashboardToDate").val();;
	jsonData.prevDashboardFromDate = $("#prevDashboardFromDate").val();
	jsonData.prevDashboardToDate = $("#prevDashboardToDate").val();
	jsonData.branchName=bnchName;
	jsonData.branchId = branchId;
	jsonData.amtValue=amountValue;
	jsonData.displayReq=requirements;
	jsonData.addOnParameter=additionalParameter;
	var url="/dashboard/quotationProformabybranch";
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
	    	//location.hash="#pendingExpense";
	  		$("div[class='modal-backdrop fade in']").remove();
	  		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
	  		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
	  		$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
	  		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
	  		$("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").hide();
	  		if(data.quotationProformaData.length>0){
	  			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable">'+
	      		'<thead class="tablehead1" style="position:relative"><th>User Name</th><th>Project Name</th><th>Amount</th><th>Percent</th><tr></thead><tbody></tbody></table></div>');

	      		$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#" class="'+data.quotationProformaData[0].txnModelFor+'">'+bnchName+'</a>');
	      		var quoProDatatemp ="";
	  			for(var i=0;i<data.quotationProformaData.length;i++){
	   				quoProDatatemp += '<tr><td>'+data.quotationProformaData[i].userName+'</td><td>'+data.quotationProformaData[i].projectName+'</td>';
	  				quoProDatatemp += '<td style="cursor:pointer; color:blue;" onClick="fetchItemsListQuotationProforma('+data.quotationProformaData[i].branchID+','+data.quotationProformaData[i].userID+','+data.quotationProformaData[i].projectID+',\''+requirements+'\',\''+data.quotationProformaData[i].userName+'\',\''+data.quotationProformaData[i].projectName+'\');">'+data.quotationProformaData[i].netAmount+'</td><td>'+data.quotationProformaData[i].percentAmount+'</td></tr>';
	  			}
	  			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivableCustomerVendorBreakupTable'] tbody").append(quoProDatatemp);
	  		}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
	    		swal("Error on fetching branch wise data!", "Please retry, if proplem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}

	});
}

var fetchItemsListQuotationProforma = function(branchID, userID, projectID, displayReq, userName, projectName){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.currDashboardFromDate = $("#currDashboardFromDate").val();
	jsonData.currDashboardToDate = $("#currDashboardToDate").val();;
	jsonData.prevDashboardFromDate = $("#prevDashboardFromDate").val();
	jsonData.prevDashboardToDate = $("#prevDashboardToDate").val();
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.branchID=branchID;
	jsonData.userID=userID;
	jsonData.projectID=projectID;
	jsonData.displayReq=displayReq;
	var url="/dashboard/quotationProformaItems";
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
			$("div[class='modal-backdrop fade in']").remove();
	  		$("#staticdashboardindividualbreakup").attr('data-toggle', 'modal');
	  		$("#staticdashboardindividualbreakup").modal('show');
	  		$(".staticdashboardindividualbreakupclose").attr("href",location.hash);
	  		$("#staticdashboardindividualbreakup div[class='modal-body']").html("");
	  		if(!isEmpty(data) && !isEmpty(data.quotationProformaData) && data.quotationProformaData.length>0){
	  			$("#staticdashboardindividualbreakup div[class='modal-body']").html('<div class="datascrolltable"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="quotationProformaItemsTable">'+
	      		'<thead class="tablehead1" style="position:relative"><th>Item Name</th><th>Amount</th><tr></thead><tbody></tbody></table></div>');

	      		$("#staticdashboardindividualbreakup h4").html('Branch Wise Items for User: ' + userName + ' Project: ' + projectName);
	      		var quoProDatatemp ="";
	  			for(var i=0;i<data.quotationProformaData.length;i++){
	  				var transSpecName = data.quotationProformaData[i].specificname;
	  				quoProDatatemp += '<tr><td>'+transSpecName+'</td>';
	   				quoProDatatemp += '<td style="cursor:pointer; color:blue;" onClick="fetchTransactionsForItems('+branchID+','+userID+','+projectID+','+data.quotationProformaData[i].specificid+',\''+transSpecName+'\',\''+displayReq+'\')">'+data.quotationProformaData[i].netamounttotal+'</td></tr>';
	  			}
	  			$("#staticdashboardindividualbreakup div[class='modal-body'] table[id='quotationProformaItemsTable'] tbody").append(quoProDatatemp);
	  		}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
	    		swal("Error on fetching items and net amount!", "Please retry, if proplem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

var fetchTransactionsForItems = function(branchID, userID, projectID, specificid, specificName, displayReq){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.branchID=branchID;
	jsonData.userID=userID;
	jsonData.projectID=projectID;
	jsonData.specificid=specificid;
	jsonData.displayReq=displayReq;
	var url="/dashboard/transactionsforitm";
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
			$("div[class='modal-backdrop fade in']").remove();
	  		$("#dashboardTransactionItem").attr('data-toggle', 'modal');
	  		$("#dashboardTransactionItem").modal('show');
	  		$(".dashboardTransactionItemclose").attr("href",location.hash);
	  		$("#dashboardTransactionItem div[class='modal-body']").html("");
	  		if(!isEmpty(data) && !isEmpty(data.transactionData) && data.transactionData.length>0){
	  			$("#dashboardTransactionItem div[class='modal-body']").html('<div class="datascrolltable"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="transactionItemTable">'+
	      		'<thead class="tablehead1" style="position:relative"><th>Transaction Ref.</th><th>Amount</th><tr></thead><tbody></tbody></table></div>');

	      		$("#dashboardTransactionItem h4").html("Transactions for: " + specificName);
	      		var quoProDatatemp ="";
	  			for(var i=0;i<data.transactionData.length;i++){
	  				quoProDatatemp += '<tr><td style="cursor:pointer; color:blue;" id="'+data.transactionData[i].transid+'" onClick="displayTransctionModal(this)">'+data.transactionData[i].transid+'</td>';
	   				quoProDatatemp += '<td>'+data.transactionData[i].netamounttotal+'</td></tr>';
	  			}
	  			$("#dashboardTransactionItem div[class='modal-body'] table[id='transactionItemTable'] tbody").append(quoProDatatemp);
	  		}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }else if(xhr.status == 500){
	    		swal("Error on fetching item's Transactions!", "Please retry, if proplem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

/*Not in use
function displayCustWiseProformaInvoice(bnchName, amountValue, requirements, additionalParameter){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.branchName=bnchName;
	jsonData.amtValue=amountValue;
	jsonData.displayReq=requirements;
	jsonData.addOnParameter=additionalParameter;
	var url="/dashboard/customerwiserProformaInvoice";
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
			location.hash="#pendingExpense";
			$("div[class='modal-backdrop fade in']").remove();
			$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
			$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
			$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
			if(data.customerWiseProformaInvoicesValues.length>0){
				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable" style="margin-top: 0px; width:450px;">'+
				'<thead class="tablehead1" style="position:relative"><th>Customer Name</th><th>Amount</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
				for(var i=0;i<data.customerWiseProformaInvoicesValues.length;i++){
					$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#" class="'+data.customerWiseProformaInvoicesValues[i].txnModelFor+'" onclick="showBranchWiseBreakUp(this);">'+data.customerWiseProformaInvoicesValues[i].branchName+'</a>');

					$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivableCustomerVendorBreakupTable'] tbody").append('<tr><td><div style="width:180px;">'+data.customerWiseProformaInvoicesValues[i].customerName+'</div></td>'+

					'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.customerWiseProformaInvoicesValues[i].id+'" name="'+data.customerWiseProformaInvoicesValues[i].txnModelFor+'" title="under0to30" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.customerWiseProformaInvoicesValues[i].branchID+'\');">'+data.customerWiseProformaInvoicesValues[i].netAmount+'</a></div></td></tr>');
				}
			}
      	},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}*/