
$(document).ready(function() {
	$('#setupPayrollForOrg-container').on('click', function() {			
			$('#expenseClaim-container, #travelClaim-container, #user-form-container, #claimGroup-container').slideUp(400);
			$('#payrollsetup-container').slideDown(600);			
			alwaysScrollTop();
			var jsonData = {};
			jsonData.userEmail = $("#hiddenuseremail").text();			
			var url="/payroll/showPayrollSetupItems";
			$.ajax({
				url         : url,
				data 		: JSON.stringify(jsonData),
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				method      : "POST",
				contentType : 'application/json',
				success     : function (data) {	
					//Populate Earning items
					//$("#staticPayrollEarningListDiv table[id='payrollEarningListTable'] tbody tr:last").remove();
					$("#staticPayrollEarningListDiv table[id='payrollEarningListTable'] > tbody").html("");
					var length=$("#staticPayrollEarningListDiv table[id='payrollEarningListTable'] tbody tr").length;
					if(length == 0){
						var multiItemsTableTr = '<tr id="pEarning'+length+'"><td><input type="text" class="earningItem" placeholder="Earning Head" name="earningItem" id="earningItem"></td><td><input class="fixedDedu" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed earning?</p></td></tr>';
						$("#staticPayrollEarningListDiv table[id='payrollEarningListTable'] tbody").append(multiItemsTableTr);
					}
					for(var i=0; i<data.allPayrollEarningsItemsData.length; i++){
						var multiItemsTableTr = '<tr id="pEarning'+length+'"><td><input type="text" style="width:100%;" class="earningItem" placeholder="Earning Head" name="earningItem" id="earningItem" value="'+data.allPayrollEarningsItemsData[i].name+'"></td><td><input id="fixedEarn'+data.allPayrollEarningsItemsData[i].id+'" class="fixedEarn" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed earning?</p></td></tr>';
						$("#staticPayrollEarningListDiv table[id='payrollEarningListTable'] tbody").append(multiItemsTableTr);
						if(data.allPayrollEarningsItemsData[i].isFixed==1){
							$('#fixedEarn'+data.allPayrollEarningsItemsData[i].id+'').prop('checked',true);
						}
						else{
							$('#fixedEarn'+data.allPayrollEarningsItemsData[i].id+'').prop('checked',false);
						}
				}
					//Deduction items					
					$("#staticPayrollDeductionListDiv table[id='payrollDeductionListTable'] tbody").html("");
					if(data.allPayrollDeductionsItemsData.length==0){
						var multiItemsTableTr = '<tr id="pDeduction0"><td><input type="text" class="deductionItem" placeholder="Deduction Head" name="deductionItem" id="deductionItem" value="Tax Deducted At Source" readonly="readonly"></td><td><input class="fixedDedu" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed deduction?</p></td></tr>';
						multiItemsTableTr += '<tr id="pDeduction0"><td><input type="text" class="deductionItem" placeholder="Deduction Head" name="deductionItem" id="deductionItem" value="Professional Tax" readonly="readonly"></td><td><input class="fixedDedu" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed deduction?</p></td></tr>';
						multiItemsTableTr += '<tr id="pDeduction0"><td><input type="text" class="deductionItem" placeholder="Deduction Head" name="deductionItem" id="deductionItem" value="Provident Fund" readonly="readonly"></td><td><input class="fixedDedu" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed deduction?</p></td></tr>';
						multiItemsTableTr += '<tr id="pDeduction0"><td><input type="text" class="deductionItem" placeholder="Deduction Head" name="deductionItem" id="deductionItem" value="Employee State Insurance" readonly="readonly"></td><td><input class="fixedDedu" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed deduction?</p></td></tr>';
						$("#staticPayrollDeductionListDiv table[id='payrollDeductionListTable'] tbody").append(multiItemsTableTr);
						
					}
					else{
					for(var i=0; i<data.allPayrollDeductionsItemsData.length; i++){
						var length=$("#staticPayrollDeductionListDiv tbody tr").length;
						if(length<4){
						var multiItemsTableTr = '<tr id="pDeduction'+length+'"><td><input type="text" class="deductionItem" placeholder="Deduction Head" name="deductionItem" id="deductionItem" value="'+data.allPayrollDeductionsItemsData[i].name+'" readonly="readonly"></td><td><input id="fixedDedu'+data.allPayrollDeductionsItemsData[i].id+'" class="fixedDedu" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed deduction?</p></td></tr>';	
						}
						else{
						var multiItemsTableTr = '<tr id="pDeduction'+length+'"><td><input type="text" class="deductionItem" placeholder="Deduction Head" name="deductionItem" id="deductionItem" value="'+data.allPayrollDeductionsItemsData[i].name+'"></td><td><input id="fixedDedu'+data.allPayrollDeductionsItemsData[i].id+'" class="fixedDedu" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed deduction?</p></td></tr>';
						}
						$("#staticPayrollDeductionListDiv table[id='payrollDeductionListTable'] tbody").append(multiItemsTableTr);
						if(data.allPayrollDeductionsItemsData[i].isFixed==1){
							$('#fixedDedu'+data.allPayrollDeductionsItemsData[i].id+'').prop('checked',true);
						}
						else{
							$('#fixedDedu'+data.allPayrollDeductionsItemsData[i].id+'').prop('checked',false);
						}
					}
				}
				},
				error : function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); 
					}else if(xhr.status == 500){
			    		swal("Error on fetching input taxes!", "Please retry, if problem persists contact support team", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
				}
			});
	});
});



$(document).ready(function() {
	$(".addnewEarningItemForPayroll").click(function(){		
		var length=$("#staticPayrollEarningListDiv tbody tr").length;
		if(length>=7){
			swal("Error!","The total number of Payroll Earning Items should not exceed 7.","error");
			return false;
		}
		var multiItemsTableTr = '<tr id="pEarning'+length+'"><td><input type="text" STYLE="width:100%;" class="earningItem" placeholder="Earning Head" name="earningItem" id="earningItem" /></td><td><input class="fixedEarn" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed earning?</p></td></tr>';
		$("#staticPayrollEarningListDiv table[id='payrollEarningListTable'] tbody").append(multiItemsTableTr);
	});

	$(".deleteEarningItemFromPayroll").click(function(){
		var length=$("#staticPayrollEarningListDiv tbody tr").length;
		if(length==1){
			swal("Incomplete details!","Payroll Earning Items are mandatory","error");
			return false;
		}
		$("#staticPayrollEarningListDiv table[id='payrollEarningListTable'] tbody tr:last").remove();
	});
	
	$(".addnewDeductionItemForPayroll").click(function(){		
		var length=$("#staticPayrollDeductionListDiv tbody tr").length;
		if(length>=7){
			swal("Invalid details!","The total number of Payroll Deduction Items should not exceed 7.","error");
			return false;
		}
		var multiItemsTableTr = '<tr id="pDeduction'+length+'"><td><input type="text" class="deductionItem" placeholder="Deduction Head" name="deductionItem" id="deductionItem" /></td><td><input class="fixedDedu" style="width: 50%;align: left; margin-left: -47%; !important" type="checkbox"><p style="margin-left: 18px; margin-top:-20px;!important">Is this fixed deduction?</p></td></tr>';
		$("#staticPayrollDeductionListDiv table[id='payrollDeductionListTable'] tbody").append(multiItemsTableTr);
	});

	$(".deleteDeductionItemFromPayroll").click(function(){
		var length=$("#staticPayrollDeductionListDiv tbody tr").length;
		if(length<=4){
			swal("Invalid details!","Top 4 Payroll Deduction Items cannot be deleted.","error");
			return false;
		}
		$("#staticPayrollDeductionListDiv table[id='payrollDeductionListTable'] tbody tr:last").remove();
	});
	
	$(".addPayrollItemBtn").click(function(){
		var useremail=$("#hiddenuseremail").text();
		var multipleEarningItemsData = [];
		var count=0;
		$("#payrollEarningListTable > tbody > tr").each(function() {
			count++;
			/*if(count>7){
				alert("The total number of payroll earning items should not exceed 7.");
				return false;
			}*/
			var jsonData = {};				
			var earningItem = $(this).find("td input[class='earningItem']").val();
			var ifEarnFixed;
			var isFixedEarn;
				if($(this).find("td input[class='fixedEarn']").prop('checked')==true){
					isFixedEarn="1";
				}
				else{
					isFixedEarn="0";
				}
			if(earningItem != "" && typeof earningItem!='undefined'){					
				jsonData.earningItem = earningItem;	
				if(isFixedEarn != "" && typeof isFixedEarn!='undefined'){
					jsonData.isFixedEarn=isFixedEarn;
				}
				multipleEarningItemsData.push(JSON.stringify(jsonData));
			}
		});	
		/*if(count==0){
			alert("Earning List cannot be empty");
			return false;
		}*/
		count=0;
		var multipleDeductionsItemsData = [];
		$("#payrollDeductionListTable > tbody > tr").each(function() {
			count++;
			/*if(count>7){
				alert("The total number of payroll deduction items should not exceed 7.");
				return false;
			}*/
			var jsonData = {};				
			var deductionItem = $(this).find("td input[class='deductionItem']").val();
			var isFixedDedu;
			if($(this).find("td input[class='fixedDedu']").prop('checked')==true){
				isFixedDedu="1";
			}
			else{
				isFixedDedu="0";
			}
			if(deductionItem != "" && typeof deductionItem!='undefined'){					
				if(isFixedDedu != "" && typeof isFixedDedu!='undefined'){
					jsonData.isFixedDedu=isFixedDedu;
				}
				jsonData.deductionItem = deductionItem;
				multipleDeductionsItemsData.push(JSON.stringify(jsonData));
			}
		});		
		var jsonData = {};
		jsonData.multipleEarningItemsData = multipleEarningItemsData;
		jsonData.useremail = useremail;
		jsonData.multipleDeductionsItemsData=multipleDeductionsItemsData;
		
		if(multipleEarningItemsData!=""){
			var url="/payroll/addPayrollSetupItems";
			$.ajax({
				url : url,
				data : JSON.stringify(jsonData),
				type : "text",
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				method : "POST",
				contentType : 'application/json',
				success : function(data) {
					swal("Success!","Payroll setup successful for the organisation!","success");
					alwaysScrollTop();
					getAllUsers();
				},
				error: function(xhr, status, error) {
					if(xhr.status == 401){ doLogout();
					}else if(xhr.status == 500){
			    		swal("Error on adding  payroll!", "Please retry, if problem persists contact support team", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
				}
			});
		}		
	});
	
	$("#submitForPayroll").click(function(elem){
		var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
		var parentTr = "payrollTrid"; //$(elem).closest('tr').attr('id');
		var useremail=$("#hiddenuseremail").text();
		var payrollMonth = $("#payrollMonth").val();
		
		var branchId= $("#payrollTxnForBranches option:selected").val();
		var payDays = $("#payDays").val();
		if(branchId=="" || typeof branchId=='undefined'){
			swal("Incomplete payroll data!", "Please select branch from dropdown!", "error");			
			return true;
		}
		if(payrollMonth=="" || typeof payrollMonth=='undefined'){
			swal("Incomplete payroll data!", "Please select payroll month and year!", "error");			
			return true;
		}
		if(payDays=="" || typeof payDays=='undefined'){
			swal("Incomplete payroll data!", "Please provide no. of pay days in this month!", "error");			
			return true;
		}
		var jsonData = {};
		var payrollItems = convertPayrollTableDataToArray();
		var txnReceiptDetails=$("#paryollreceiptdetail option:selected").val();
		var txnReceiptTypeBankDetails=$("div[id='receiptBankDetails'] #receiptBranchBankAccount").val();
		if(txnReceiptDetails=="2"){
			receiptPaymentBank=$("#availableBank option:selected").val();
			if(typeof receiptPaymentBank!='undefined'){
				jsonData.txnreceiptPaymentBank=receiptPaymentBank;
				jsonData.txnInstrumentNum=$("#"+parentTr+" input[name='txtInstrumentNumber']").val();
				var instrumentDate=$("tr[id='payrollTrid'] input[name=txtInstrumentDate]").val();
				if(instrumentDate == "" || typeof instrumentDate=='undefined'){
					swal("Incomplete transaction detail!", "Instrument Date cannot be empty.", "error");
					enableTransactionButtons();
					return false;
				}
				jsonData.txnInstrumentDate=instrumentDate;
		    }
		}
		var totalEarning=" ",totalDeduction="",totalNetPay;
		var noOfEarnCols = $("#payrollEarningTable > tbody > tr:first > td").length - 3;
		var noOfDedCols = $("#payrollDeductionTable > tbody > tr:first > td").length - 2;
		for(var i=0; i<noOfEarnCols;i++){
			totalEarning += ($("#totalEarnHead"+i+"").val()+",");
		}
		jsonData.totalEarning = totalEarning;
		for(var i=0; i<noOfDedCols;i++){
			if(!isEmpty($("#totalDedHead"+i+"").val())) {
                totalDeduction += ($("#totalDedHead" + i + "").val() + ",");
            }
		}
		jsonData.totalDeduction = totalDeduction;
		jsonData.totalTotalEarnings = $("#userMontEarningActualTotalHead").val();
		jsonData.totalTotalDeductions = $("#userMontDeductionActualTotalHead").val();
		jsonData.totalNetPay = $("#netPayTotalHead").val();
		jsonData.txnPurpose=whatYouWantToDoVal;
		jsonData.useremail = useremail;
		jsonData.payrollItems=payrollItems;
		jsonData.payrollMonth = payrollMonth;
		jsonData.branchId = branchId;
		jsonData.payDays = payDays;
		jsonData.txnReceiptDetails = txnReceiptDetails;
		jsonData.txnReceiptTypeBankDetails = txnReceiptTypeBankDetails;
			
		if(payrollItems!=""){
			var url="/payroll/addPayrollMonthlyPaySlips";
			$.ajax({
				url : url,
				data : JSON.stringify(jsonData),
				type : "text",
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				method : "POST",
				contentType : 'application/json',
				success : function(data) {
					swal("Success!","Payroll Submitted!","success");
					cancel();
				},
				error: function(xhr, status, error) {
					if(xhr.status == 401){ doLogout();
					}else if(xhr.status == 500){
			    		swal("Error on submit payroll!", "Please retry, if problem persists contact support team", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
				}
			});
		}		
	});
});

function paySlipPopUp(){
    var parentTr=$(this).closest('div').attr('id');
    var parentOfparentTr = $(this).parents().closest('tr').attr('id');
    var useremail=$("#hiddenuseremail").text();    
    var jsonData = {};
    //var branchId = $("#bnchHidden").val();
    //var specfId = $("#specfHidden").val();
    jsonData.usermail = useremail;
    //jsonData.branchId = branchId;
    //var url ="/config/getBranchIncomesCoa";
    var url = "/config/allUsers";
    $.ajax({
        url : url,
        data : JSON.stringify(jsonData),
        type : "text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method : "POST",
        contentType : 'application/json',
        success : function(data) {
            $("#popUpGeneratePaySlip").attr('data-toggle', 'modal');
            $("#popUpGeneratePaySlip").modal('show');
            $(".popUpGeneratePaySlip").attr("href",location.hash);
            $("#popUpGeneratePaySlip div[class='modal-body']").html("");   
            $("#popUpGeneratePaySlip div[class='modal-body']").append('<div class="itemsSelect"><ul id="userListPayslip" style="list-style-type: none; ">');
            $("#popUpGeneratePaySlip div[class='modal-body']").find("ul[id='userListPayslip']").append('<li style="text-align: left;">Select Month <input type="text" id="payrollMonthPopUp" class="date-picker"> ');
            $('#payrollMonthPopUp').datepicker({
		        dateFormat: 'MM yy',
		        changeMonth: true,
		        changeYear: true,
		        showButtonPanel: true,

		        onClose: function(dateText, inst) {
		            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
		            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
		            $(this).val($.datepicker.formatDate('MM yy', new Date(year, month, 1)));
		        }
		    });
            $('#payrollMonthPopUp').focus(function () {
		        $(".ui-datepicker-calendar").hide();
		        $("#ui-datepicker-div").position({
		            my: "center top",
		            at: "center bottom",
		            of: $(this)
		        });
		    });
            $("#popUpGeneratePaySlip div[class='modal-body']").find("ul[id='userListPayslip']").append('<li style="text-align: left;"><input id="SelectAll" value="Select All" type="checkbox" onchange="selectAllUsersForGeneratePayslip();">Select All</b></li>');
            if(data.userListData.length>0){        
                
                for(var i=0;i<data.userListData.length;i++){    
                    
                        $("#popUpGeneratePaySlip div[class='modal-body']").find("ul[id='userListPayslip']").append('<li style="text-align: left;"><input id="paySlipUserSelection" value="'+data.userListData[i].id+'" type="checkbox">'+data.userListData[i].fullName+'</b></li>');
                    
                    
                }                
            }                
            $("#popUpGeneratePaySlip div[class='modal-body'] ").append('</ul><input type="button" style="width:60px;" id="selectUserPayslip" name="selectUserPayslip" class="btn btn-submit" value="Submit" onclick="generatePaySlipSubmit(this,\''+parentTr+'\',\''+parentOfparentTr+'\');"></div>');        
        },
        error : function() {

        }
    });
}

function payrollApproverAction(parentTr){
	$(".btn-custom").attr("disabled", "disabled");
	$(".btn-customred").attr("disabled", "disabled");
	$(".approverAction").attr("disabled", "disabled");
	$("#completeTxn").attr("disabled", "disabled");
	
	var selectedAction=$("#"+parentTr+" select[id='approverActionList'] option:selected").val();
	var transactionEntityId=parentTr.substring(18, parentTr.length);
	var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
	var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
	var selectedAddApproverVal="";
	
	if(selectedAction==""){
		swal("Invalid details!","Please choose your next action from the Approver action list","error");
		$(".btn-custom").removeAttr("disabled");
		$(".btn-customred").removeAttr("disabled");
		$(".approverAction").removeAttr("disabled");
		$("#completeTxn").removeAttr("disabled");
		return true;
	}
	
	if(selectedAction=="3"){
		selectedAddApproverVal=$("#"+parentTr+" select[id='userAddApproval'] option:selected").val();
		if(selectedAddApproverVal==""){
			swal("Invalid details!","Please choose the user to whom you want to send for additional approval","error");
			$(".btn-custom").removeAttr("disabled");
			$(".btn-customred").removeAttr("disabled");
			$(".approverAction").removeAttr("disabled");
			$("#completeTxn").removeAttr("disabled");
			return true;
		}else{
			var txnJsonData={};
			txnJsonData.useremail=$("#hiddenuseremail").text();
			txnJsonData.selectedApproverAction=selectedAction;
			txnJsonData.transactionPrimId=transactionEntityId;
			txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
			txnJsonData.suppDoc=supportingDoc;
			txnJsonData.txnRmarks=remarks;
			var url="/transactionPayroll/approverAction";
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
				}
			});
		}
	}
	else{
		//send server for action to complete
		var txnJsonData={};
		txnJsonData.useremail=$("#hiddenuseremail").text();
		txnJsonData.selectedApproverAction=selectedAction;
		txnJsonData.transactionPrimId=transactionEntityId;
		txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
		txnJsonData.suppDoc=supportingDoc;
		txnJsonData.txnRmarks=remarks;
		var url="/transactionPayroll/approverAction";
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
			}
		});
	}
}

function completePayrollAccounting(elem){
    disableTransactionButtons();
    var parentTr = $(elem).closest('tr').attr('id');
    var selectedAction="4";
    var transactionEntityId=parentTr.substring(18, parentTr.length);
    var supportingDoc=$("#"+parentTr+" input[name='actionFileUpload']").val();
    var remarks=$("#"+parentTr+" textarea[id='txnRemarks']").val();
    var paymentOption=$("#"+parentTr+" select[id='paryollreceiptdetail'] option:selected").val();
    var bankDetails=$("#"+parentTr+" textarea[id='bankDetails']").val();
    var transactionInvoiceDate=$("#"+parentTr+" input[name='vendorInvoiceDate']").val();
    var selectedTransactionBranch = $("#"+parentTr+" p[class='branchDetails'] ").attr('id');
    var selectedAddApproverVal="";var paymentBank="";
    var txnJsonData={};
    txnJsonData.useremail=$("#hiddenuseremail").text();
    txnJsonData.selectedApproverAction=selectedAction;
    txnJsonData.transactionPrimId=transactionEntityId;
    txnJsonData.selectedAddApproverEmail=selectedAddApproverVal;
    txnJsonData.selectedTransactionBranch=selectedTransactionBranch;
    txnJsonData.suppDoc=supportingDoc;
    txnJsonData.txnRmarks=remarks;
    txnJsonData.txnInvDate=transactionInvoiceDate;
    txnJsonData.paymentDetails=paymentOption;
    if(paymentOption=="2"){
        paymentBank=$("#availableBank option:selected").val();
        txnInstrumentNum = $("#"+parentTr+" input[id='txtInstrumentNumber']").val();
        txnInstrumentDate = $("#"+parentTr+" input[id='txtInstrumentDate']").val();
        if(typeof paymentBank!='undefined'){
            txnJsonData.txnPaymentBank=paymentBank;
            txnJsonData.txnInstrumentNum = txnInstrumentNum;
            txnJsonData.txnInstrumentDate = txnInstrumentDate;
        }
    }
    //txnJsonData.bankInf=bankDetails;
    var url="/transactionPayroll/approverAction";
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
        	if(data.cashBankBal.length > 0){
        	if(data.cashBankBal[0].bankBalCheck == 1){
            	swal("Insufficient balance in the bank account!","Use alternative payment mode or infuse funds into the bank account","warning");
                   }
        	if(data.cashBankBal[0].cashBalCheck == 1){
        		swal("Insufficient balance in the cash account!","Use alternative payment mode or infuse funds into the cash account","warning");
                }
            }
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){ doLogout();
            }else if(xhr.status == 500){
                swal("Error on Complete Accounting!", "Please retry, if persists contact support team", "error");
            }

        },
        complete: function(data) {
            $.unblockUI();
            enableTransactionButtons();
        }
    });
}


function selectAllUsersForGeneratePayslip(){
	$('#userListPayslip li').each(function() {
		var item = $(this);
		$(this).find('input[type="checkbox"]').prop('checked','checked');
		});		
	}

function generatePaySlipSubmit(elem,parentTr,parentOfparentTr){    
    //$("#popUpGeneratePaySlip").modal('hide');    
    var allVals = [];
    $('.itemsSelect :checked').each(function() {
        allVals.push($(this).val());
    });
    if(allVals[0] == "Select All"){
        allVals.shift();
    }       
    for(var i=0;i<allVals.length;i++){
    	 var jsonData={};       
    	 if($("#payrollMonthPopUp").val()==null||$("#payrollMonthPopUp").val()==""||$("#payrollMonthPopUp").val()==undefined){
    		 swal("Incomplete details!","please choose month for payslip!","error");
    		 return false;
    	 }
    	 jsonData.period=$("#payrollMonthPopUp").val();   
    	 jsonData.userEmail=$("#hiddenuseremail").text();
    	 jsonData.payslipusers=allVals[i];    
    	 var url="/payroll/generatePayslip";    
    	 downloadFile(url, "POST", jsonData, "Error on payslip generation!");
    }
}


function payrollCustomDropDownToggle(elem){
	var userPk=$("#userEntityHiddenId").val();
	if(userPk=="" || typeof userPk=='undefined' || userPk==null){
		swal("Incomplete details!","You can configure payroll for existing USER only!","error");
		return false;
	}	
	var classval=$(elem).attr('class');
	var menuId=$(elem).attr('id');
	if(classval=="multiselect dropdown-toggle btn"){
		var newclassval=classval+ " " +"open";
		$(elem).attr('class',newclassval);
		if(menuId=="userEarningdropdown"){
			var divdropdown="openuserEarningdropdown-menu"
			$(".userEarningdropdown-menu").attr('class',divdropdown);
		}else if(menuId=="userDeductionsdropdown"){
			var divdropdown="openuserDeductionsdropdown-menu"
			$(".userDeductionsdropdown-menu").attr('class',divdropdown);
		}				
	}else if(classval=="multiselect dropdown-toggle btn open"){
		var menuId=$(elem).attr('id');
		var newclassval="multiselect dropdown-toggle btn";
		$(elem).attr('class',newclassval);
		if(menuId=="userEarningdropdown"){
			var divdropdown="userEarningdropdown-menu"
			$(".openuserEarningdropdown-menu").attr('class',divdropdown);
		}else if(menuId=="userDeductionsdropdown"){
			var divdropdown="userDeductionsdropdown-menu"
			$(".openuserDeductionsdropdown-menu").attr('class',divdropdown);
		}
	}
}

function calculateMonthlyEarningIncome(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var elemId = $(elem).attr('id');
	var payrollSeupId = elemId.substring(13);
	var earningAnnual=$("#"+parentTr+" input[id='"+elemId+"']").val();	
	var earningMonthly = earningAnnual/12;
	earningMonthly = parseFloat(Math.round(earningMonthly * 100) / 100).toFixed(2);
	$("#"+parentTr+" input[id='earningMonthly"+payrollSeupId+"']").val(earningMonthly);
}

function calculateMonthlyDeductionIncome(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var elemId = $(elem).attr('id');
	var payrollSeupId = elemId.substring(15);
	var earningAnnual=$("#"+parentTr+" input[id='"+elemId+"']").val();	
	var earningMonthly = earningAnnual/12;
	earningMonthly = parseFloat(Math.round(earningMonthly * 100) / 100).toFixed(2);
	$("#"+parentTr+" input[id='deductionMonthly"+payrollSeupId+"']").val(earningMonthly);
}

var saveUserEarningsPayrollRules = function(elem){			
	var annualIncome="";
	var monthlyIncome="";		
	var userPk=$("#userEntityHiddenId").val();
	if(userPk=="" || typeof userPk=='undefined' || userPk==null){
		swal("Error!","You can configure payroll for existing USER only!","error");
		return false;
	}	
	logDebug("Start saveUserEarningsPayrollRules");
	var earningcheckboxes = "";	
	earningcheckboxes = $('#newuserPayrollEarningsExcelFormTable tr[id="payrollSetUpRuleTr"] ul[id="earningULList"] input[name="checkCOA"]:checkbox:checked').map(function(){
		var value = this.value;
		if(value!=""){			
			annualIncome+=$("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='earningULList'] input[id='earningAnnual"+value+"']").val()+",";
			monthlyIncome+=$("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='earningULList'] input[id='earningMonthly"+value+"']").val()+",";
			return value;
		}
	}).get();	
	if(earningcheckboxes !== "" && earningcheckboxes !== null){
		var userInfo={};
		userInfo.userEmail=$("#hiddenuseremail").text();
		userInfo.earningsCheckboxList=earningcheckboxes.toString();		
		userInfo.annualIncome = annualIncome.substring(0, annualIncome.length-1);
		userInfo.monthlyIncome = monthlyIncome.substring(0, monthlyIncome.length-1);
		userInfo.userHiddenPrimKey=$("#userEntityHiddenId").val();	
		userInfo.userName=$("#userName").val();
		userInfo.payrollType = 1; //Earnings
		var url="/payroll/saveUserPayrollItems";
		$.ajax({
			url         : url,
			data        : JSON.stringify(userInfo),
			async		: false,
			type        : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				swal("Success!","User earning data saved!","success");
			},
			error: function (xhr, status, error){
				if(xhr.status == 401){
					doLogout();
				}else if(xhr.status == 500){
					swal("Error on uodate rule!", "Please retry, if problem persists contact support team", "error");
	    		}	    		
			},
			complete: function(data) {				
			}
		});
	}
}


var saveUserDeductionsPayrollRules = function(elem){			
	var annualIncome="";
	var monthlyIncome="";	
	logDebug("Start saveUserDeductionsPayrollRules");
	var earningcheckboxes = "";	
	earningcheckboxes = $('#newuserPayrollEarningsExcelFormTable tr[id="payrollSetUpRuleTr"] ul[id="deductionULList"] input[name="checkCOA"]:checkbox:checked').map(function(){
		var value = this.value;
		if(value!=""){			
			annualIncome+=$("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='deductionULList'] input[id='deductionAnnual"+value+"']").val()+",";
			monthlyIncome+=$("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='deductionULList'] input[id='deductionMonthly"+value+"']").val()+",";
			return value;
		}
	}).get();	
	if(earningcheckboxes !== "" && earningcheckboxes !== null){
		var userInfo={};
		userInfo.userEmail=$("#hiddenuseremail").text();
		userInfo.earningsCheckboxList=earningcheckboxes.toString();		
		userInfo.annualIncome = annualIncome.substring(0, annualIncome.length-1);
		userInfo.monthlyIncome = monthlyIncome.substring(0, monthlyIncome.length-1);
		userInfo.userHiddenPrimKey=$("#userEntityHiddenId").val();	
		userInfo.userName=$("#userName").val();
		userInfo.payrollType = 2; //Deductions
		var url="/payroll/saveUserPayrollItems";
		$.ajax({
			url         : url,
			data        : JSON.stringify(userInfo),
			async		: false,
			type        : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				swal("Success!","User deductions data saved!","success");
			},
			error: function (xhr, status, error){
				if(xhr.status == 401){
					doLogout();
				}else if(xhr.status == 500){
					swal("Error on uodate rule!", "Please retry, if problem persists contact support team", "error");
	    		}	    		
			},
			complete: function(data) {				
			}
		});
	}
}

var random=0;
function populateTransactionPayrollTable(elem){
	var jsonData = {};    
    var useremail=$("#hiddenuseremail").text();
    var branchId=$(elem).val();
    if(branchId == "" || typeof branchId == 'undefined'){
    	return;
	}
    jsonData.userEmail = useremail;
    jsonData.branchId = branchId;
    var url="/payroll/getTransactionPayrollData";
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
        	$("table[id='payrollEarningTable'] > tbody").html("");
        	$("table[id='payrollDeductionTable'] > tbody").html("");        	
        	$("table[id='payrollEarningTable'] > thead > tr[id='headNames']").html("");
        	$("table[id='payrollDeductionTable'] > thead > tr[id='headNamesDed']").html("");
        	$("#headTypes").html("");
        	$("#headTypesDed").html(""); 
        	var earningListSize = data.allPayrollEarningHeadersList.length +3;
        	var deductionListSize = data.allPayrollDeductionHeadersList.length+2;
        	$("#headTypes").append('<th  colspan='+earningListSize+'> EARNINGS</th>');
        	$("#headTypesDed").append('<th  colspan='+deductionListSize+'> DEDUCTIONS</th>');
        	$("#headNames").append('<th>User Name</th><th>Eligible Days</th>');
	        for(var i=0; i<data.allPayrollEarningHeadersList.length; i++){
	        	$("#headNames").append('<th id='+data.allPayrollEarningHeadersList[i].id+' data-isChecked='+data.allPayrollEarningHeadersList[i].isFixed+' class="earnHeadName">'+data.allPayrollEarningHeadersList[i].name+'</th>');
	        }
	        $("#headNames").append('<th id="totalUserIncome">Total Earnings');
	       for(var i=0; i<data.allPayrollDeductionHeadersList.length; i++){
	        	$("#headNamesDed").append('<th id='+data.allPayrollDeductionHeadersList[i].id+' data-isChecked='+data.allPayrollDeductionHeadersList[i].isFixed+' class="deduHeadName">'+data.allPayrollDeductionHeadersList[i].name +'</th>');
	        }
	       $("#headNamesDed").append('<th id="totalUserDed">Total Deductions');
	       $("#headNamesDed").append('<th id="userNetPay">Net Pay');
	       
	       
	       for(var i=0; i<data.allUserPayrollEarningItemsData.length; i++){
	    	   var monthlyEarningList=data.allUserPayrollEarningItemsData[i].monthlyInc.split(',');
		       	//var length=$("#transactionDetailsTable table[id='payrollEarningTable'] tbody tr").length;
				var multiItemsTableTr = '<tr id="earn'+data.allUserPayrollEarningItemsData[i].userId+'"><td>'+data.allUserPayrollEarningItemsData[i].userName+'</td><td></td>';
				var monthEarningTotal =0;
				for(var j=0;j<monthlyEarningList.length;j++){
					//multiItemsTableTr += '<td id='+data.allPayrollEarningHeadersList[i].id+'><input class="monthlyInc"  style="width:50px;" type="text"  id="userMontEarning'+j+'" value='+monthlyEarningList[j]+' readonly="readonly"></td>';
					multiItemsTableTr += '<td><input class="monthlyInc"  style="width:50px;" type="text"  id="userMontEarning'+j+'" value='+monthlyEarningList[j]+' readonly="readonly"></td>';
					//alert("data.allPayrollEarningHeadersList[i].id:::"+data.allPayrollEarningHeadersList[i].id);
					monthEarningTotal = monthEarningTotal + parseFloat(monthlyEarningList[j]);
				}				
				multiItemsTableTr += '<td><input class="monthlyIncTotal"  style="width:50px;" type="text"  id="userMontEarningTotal" value='+monthEarningTotal+' readonly="readonly"></td></tr>';
				$("table[id='payrollEarningTable'] tbody").append(multiItemsTableTr);
				
				var multiItemsTableTr1 = '<tr id="actEarnTotalhead"><td>Totals</td><td></td>';
				var multiItemsTableTr = '<tr class="earningTableTr" id="actEarn'+data.allUserPayrollEarningItemsData[i].userId+'"><td></td><td><input class="eligibleDays"  style="width:30px;" type="text"  id="eligibleDays'+j+'" onkeyup="calculateAcutualEarning(this);" onchange="calculateTotalEarnHeads(this);"></td>';
				
				for(var j=0;j<monthlyEarningList.length;j++){
					multiItemsTableTr += '<td id='+data.allPayrollEarningHeadersList[j].id+'><input class="monthlyIncActual"  style="width:50px;" onchange="calcOnIncChange(this);" type="text"  id="userActualMontEarning'+j+'"></td>';
					multiItemsTableTr1 += '<td id="totalEarnHeadTd'+j+'"><input class="monthlyIncActual"  style="width:50px;" type="text"  id="totalEarnHead'+j+'" ></td>';
					
				}				
				multiItemsTableTr += '<td><input class="monthlyIncActualTotal"  style="width:50px;" type="text"  id="userMontEarningActualTotal" readonly="readonly"></td></tr>';
				$("table[id='payrollEarningTable'] tbody").append(multiItemsTableTr);
				multiItemsTableTr1 += '<td><input style="width:50px;" type="text"  id="userMontEarningActualTotalHead" readonly="readonly"></td></tr>';
	       }
	       $("table[id='payrollEarningTable'] tbody").append(multiItemsTableTr1);
	       
	       for(var i=0; i<data.allUserPayrollDeductionsItemsData.length; i++){
	    	   var monthlyDeduList=data.allUserPayrollDeductionsItemsData[i].monthlyInc.split(',');		       
				var multiItemsTableTr = '<tr id="dedu'+data.allUserPayrollDeductionsItemsData[i].userId+'">';
				var monthDedTotal =0;
				for(var j=0;j<monthlyDeduList.length;j++){
					//multiItemsTableTr += '<td id='+data.allPayrollDeductionHeadersList[i].id+'><input class="monthlyDed"  style="width:50px;" type="text"  id="userMontDeduction'+j+'" value='+monthlyDeduList[j]+'></td>';
					multiItemsTableTr += '<td><input class="monthlyDed"  style="width:50px;" type="text"  id="userMontDeduction'+j+'" value='+monthlyDeduList[j]+'></td>';
					monthDedTotal = monthDedTotal + parseFloat(monthlyDeduList[j]);
				}				
				multiItemsTableTr += '<td><input class="monthlyDedTotal"  style="width:50px;" type="text"  id="userMontDeductionTotal" value='+monthDedTotal+'></td><td><input class="netPay"  style="width:50px;" type="text"  id="netPay" value='+data.allPayrollNetPayList[i].netPay+'></td></tr>';
				$("table[id='payrollDeductionTable'] tbody").append(multiItemsTableTr);
				
				var multiItemsTableTr1 = '<tr id="actDedTotalhead">';
				var multiItemsTableTr = '<tr class="deductionTableTr" id="actDed'+data.allUserPayrollDeductionsItemsData[i].userId+'">';
				for(var k=0;k<monthlyDeduList.length;k++){		
					multiItemsTableTr += '<td id='+data.allPayrollDeductionHeadersList[k].id+'><input class="monthlyDedActual"  style="width:50px;" type="text"  id="userMontActualDeduction'+k+'" onchange="calcOnDedChange(this);" value=0></td>';
					multiItemsTableTr1 += '<td id="totalDedHeadTd'+k+'"><input class="monthlyIncActual"  style="width:50px;" type="text"  id="totalDedHead'+k+'" ></td>';
				}
				//multiItemsTableTr += '<td><input class="monthlyDedActualTotal"  style="width:50px;" type="text"  id="userMontDeductionActualTotal'+i+'" value=0></td><td><input class="netPayActual"  style="width:50px;" type="text"  id="netPayActual'+i+'"></td></tr>';
				multiItemsTableTr += '<td><input class="monthlyDedActualTotal"  style="width:50px;" type="text"  id="userMontDeductionActualTotal" value=0></td><td><input class="netPayActual"  style="width:50px;" type="text"  id="netPayActual'+i+'"></td></tr>';
				$("table[id='payrollDeductionTable'] tbody").append(multiItemsTableTr);
				multiItemsTableTr1 += '<td><input style="width:50px;" type="text"  id="userMontDeductionActualTotalHead" readonly="readonly"></td><td><input style="width:50px;" type="text"  id="netPayTotalHead" readonly="readonly"></td></tr>';
	       }
	       $("table[id='payrollDeductionTable'] tbody").append(multiItemsTableTr1);
        },
        error : function (xhr, status, error) {
             if(xhr.status == 401){ 
            	 doLogout();             
            }else if(xhr.status == 500){
                swal("Error on fetching transactions!", "Please retry, if problem persists contact support team", "error");
            }
        },
		complete: function(data) {				
		}
      });
}

var totalHead=[]; 
function calculateAcutualEarning(elem){
	
	var payDays = $("#payDays").val();
	if(typeof payDays=='undefined' || payDays==null || payDays=="0" || payDays==""){
		swal("Invalid Details!","Insert Paydays for this Month!","error");
		return false;
	}
	var parentTr = $(elem).closest('tr').attr('id');  //actEarn3635	
	var userId = parentTr.substring(7);
	var dparentTr = "actDed"+userId;//actDed3635
	var mParentTr = "earn"+userId;//earn3635
	var nParentTr = "dedu"+userId;//dedu3635
	var tHeadTr="actEarnTotalhead";
	//var unitPrice = $("#"+parentTr+" input[class='txnPerUnitPrice']").val();
	var actualDays=$(elem).val();
	var totalActualInc=0;
	var totalActualDed=0;
	var prevAMEEarn=0;
	var noOfEarnCols = $("#payrollEarningTable > tbody > tr:first > td").length - 3;
	var noOfDedCols = $("#payrollDeductionTable > tbody > tr:first > td").length - 2;
	var noOfRows = ($("#payrollEarningTable> tbody > tr").length-1)/2;
	var isFixedEarn=[],isFixedDedu=[]; var i=0;
	$("#payrollEarningTable thead tr:last").find(".earnHeadName").each(function(){
		isFixedEarn[i]=$(this).data('ischecked');
		i++;
	});
	i=0;
	$("#payrollDeductionTable thead tr:last").find(".deduHeadName").each(function(){
		isFixedDedu[i]=$(this).data('ischecked');
		i++;
	});
	
	for(var i=0;i<noOfEarnCols;i++){
		var monthlyInc = $("#"+mParentTr+" input[id='userMontEarning"+i+"']").val();	
		$("#"+parentTr+" input[id='userActualMontEarning"+i+"']").val(123);
		//$("#"+tHeadTr+" input[id='totalEarnHead"+i+"']").val(0);
		if(monthlyInc != "" && typeof monthlyInc!='undefined'){
			var dailyInc = monthlyInc/payDays;
			var actualInc = dailyInc * actualDays;
			monthlyInc = parseFloat(monthlyInc);
			if(isFixedEarn[i]==1){
				$("#"+parentTr+" input[id='userActualMontEarning"+i+"']").val(monthlyInc.toFixed(2));
				totalActualInc = totalActualInc + monthlyInc;
				
			}
			else{
			$("#"+parentTr+" input[id='userActualMontEarning"+i+"']").val(actualInc.toFixed(2));
			totalActualInc = totalActualInc + actualInc;
			}
			
		}
	}
	for(var i=0;i<noOfDedCols;i++){
		var monthlyDed = $("#"+nParentTr+" input[id='userMontDeduction"+i+"']").val();
		if(monthlyDed != "" && typeof monthlyDed!='undefined'){
			var dailyDed = monthlyDed/payDays;
			var actualDed = dailyDed * actualDays;
			monthlyDed = parseFloat(monthlyDed);
			if(isFixedDedu[i]==1){
				$("#"+dparentTr+" input[id='userMontActualDeduction"+i+"']").val(monthlyDed.toFixed(2));
				totalActualDed = totalActualDed + monthlyDed;
			}
			else{
			$("#"+dparentTr+" input[id='userMontActualDeduction"+i+"']").val(actualDed.toFixed(2));			
			totalActualDed = totalActualDed + actualDed;
			}
		}
	}
	$("#"+parentTr+" input[id='userMontEarningActualTotal']").val(totalActualInc.toFixed(2));
	$("#"+dparentTr+" input[class='monthlyDedActualTotal']").val(totalActualDed.toFixed(2));
	$("#"+dparentTr+" input[class='netPayActual']").val((totalActualInc-totalActualDed).toFixed(2));
}

function calculateTotalEarnHeads(elem){
	var total=[];
	var totalEH=[];
	var total1=[];
	var totalEH1=[];
	var totalTotalHead=0;
	var totalTotalHeadEH=0;
	var netPay=0; 
	var totalNetPay=0;
	var j=0;
	var parentTr = $(elem).closest('tr').attr('id');  //actEarn3635	
	var userId = parentTr.substring(7);
	var dparentTr = "actDed"+userId;//actDed3635
	var mParentTr = "earn"+userId;//earn3635
	var nParentTr = "dedu"+userId;//dedu3635
	var tHeadTr="actEarnTotalhead";
	var tDedTr="actDedTotalhead";
	var noOfEarnCols = $("#payrollEarningTable > tbody > tr:first > td").length - 3;
	var noOfDedCols = $("#payrollDeductionTable > tbody > tr:first > td").length - 2;
	var noOfRows = ($("#payrollDeductionTable> tbody > tr").length-1)/2;
	
	for(i=0;i<noOfEarnCols;i++){
		total[i]=totalEH[i]=0;
	}

	$("#payrollEarningTable tbody").find(".earningTableTr").each(function(){
		for(i=0;i<noOfEarnCols;i++){
			if($(this).find("#userActualMontEarning"+i+"").val()==null||$(this).find("#userActualMontEarning"+i+"").val()==" "||$(this).find("#userActualMontEarning"+i+"").val()==undefined||isNaN(parseFloat($(this).find("#userActualMontEarning"+i+"").val()))){
				total[i]=0;
			}
			else{total[i]=parseFloat($(this).find("#userActualMontEarning"+i+"").val());}
			totalEH[i]=parseFloat(totalEH[i])+parseFloat(total[i]);
			$("#"+tHeadTr+" input[id='totalEarnHead"+i+"']").val(parseFloat(totalEH[i]).toFixed(2));
		}
	});
	
	$("#payrollEarningTable tbody").find(".earningTableTr").each(function(){
		
			if($(this).find("#userMontEarningActualTotal").val()==null||$(this).find("#userMontEarningActualTotal").val()==" "||$(this).find("#userMontEarningActualTotal").val()==undefined||isNaN(parseFloat($(this).find("#userMontEarningActualTotal").val()))){
				totalTotalHead=0;
			}
			else{totalTotalHead=parseFloat($(this).find("#userMontEarningActualTotal").val());}
			totalTotalHeadEH=parseFloat(totalTotalHeadEH)+parseFloat(totalTotalHead);
			$("#"+tHeadTr+" input[id='userMontEarningActualTotalHead']").val(parseFloat(totalTotalHeadEH).toFixed(2));
		
	});
	var count=0;
	for(i=0;i<noOfDedCols;i++){
		total[i]=totalEH[i]=0;
	}
	totalTotalHead=totalTotalHeadEH=0;
	$("#payrollDeductionTable tbody").find(".deductionTableTr").each(function(){
		for(var i=0;i<noOfDedCols;i++){
			if($(this).find("#userMontActualDeduction"+i+"").val()==null||$(this).find("#userMontActualDeduction"+i+"").val()==" "||$(this).find("#userMontActualDeduction"+i+"").val()==undefined||isNaN(parseFloat($(this).find("#userMontActualDeduction"+i+"").val()))){
				total[i]=0;
			}
			else{total[i]=parseFloat($(this).find("#userMontActualDeduction"+i+"").val());}
			totalEH[i]=parseFloat(totalEH[i])+parseFloat(total[i]);
			$("#"+tDedTr+" input[id='totalDedHead"+i+"']").val(parseFloat(totalEH[i]).toFixed(2));
			//$("#"+tDedTr+" input[id='totalDedHead"+i+"']").val(parseFloat(totalEH1[i]).toFixed(2));
		}
	});
	
	$("#payrollDeductionTable tbody").find(".deductionTableTr").each(function(){
		
			if($(this).find("#userMontDeductionActualTotal").val()==null||$(this).find("#userMontDeductionActualTotal").val()==" "||$(this).find("#userMontDeductionActualTotal").val()==undefined||isNaN(parseFloat($(this).find("#userMontDeductionActualTotal").val()))){
				totalTotalHead=0;
			}
			else{totalTotalHead=parseFloat($(this).find("#userMontDeductionActualTotal").val());}
			totalTotalHeadEH=parseFloat(totalTotalHeadEH)+parseFloat(totalTotalHead);
			$("#"+tDedTr+" input[id='userMontDeductionActualTotalHead']").val(parseFloat(totalTotalHeadEH).toFixed(2));
		
			for(var j=0;j<noOfRows;j++){
				if($(this).find("#netPayActual"+j+"").val()==null||$(this).find("#netPayActual"+j+"").val()==" "||$(this).find("#netPayActual"+j+"").val()==undefined||isNaN(parseFloat($(this).find("#netPayActual"+j+"").val()))){
					netPay=0;
				}
				else{
					netPay=parseFloat($(this).find("#netPayActual"+j+"").val());
				}
				totalNetPay=parseFloat(totalNetPay)+parseFloat(netPay);
				$("#"+tDedTr+" input[id='netPayTotalHead']").val(parseFloat(totalNetPay).toFixed(2));
			}
	});
	var jsonData = {};			
	var url="/payroll/getCashBalance";
	$.ajax({
		url         : url,
		data 		: JSON.stringify(jsonData),
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "GET",
		contentType : 'application/json',
		success     : function (data) {	
			if(totalNetPay>data.cashBalanceData[0].cashBalance){
				swal("Incomplete details!","Cannot process Payroll due to insufficient Cash Balance.","error");
				cancel();
			}
		}
		,
		error : function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); 
			}else if(xhr.status == 500){
	    		swal("Error on fetching input taxes!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

function convertPayrollTableDataToArray(){    
	var multipleItemsData = [];
	var noOfEarnCols = $("#payrollEarningTable > tbody > tr:first > td").length - 3;
	var noOfDedCols = $("#payrollDeductionTable > tbody > tr:first > td").length - 2;
	var ct =0;
	//var payrollMonth = $("#payrollMonth").val();
	//var branchId= $("#payrollTxnForBranches option:selected").val();
	//var payDays = $("#payDays").val();
	$("#payrollEarningTable > tbody > tr[class='earningTableTr']").each(function() {
		var jsonData = {};
		var eligibleDays = $(this).find("td input[class='eligibleDays']").val();
		if(eligibleDays != "" && typeof eligibleDays!='undefined' ){
			var earTrId =  $(this).closest('tr').attr('id'); //actEarn3635
			var userId = earTrId.substring(7);
		 	jsonData.userId = userId;
		 	var deduTrId = "actDed"+userId;	//actDed3636		 	
			jsonData.eligibleDays = eligibleDays;			

			var userId =  $(this).closest('tr').attr('id'); //actEarn3635	
		 	jsonData.userId = userId.substring(7);
		 	jsonData.payrollMonth = payrollMonth;
			jsonData.eligibleDays = eligibleDays;			
			jsonData.payDays = payDays;
			jsonData.earningsData = [];
			jsonData.earningPayrollTypes = []; //maintaining the order of payroll types in asc order when showing data on screen and inserting into payslip table..so not using this right now
			jsonData.deductionsData = [];
			jsonData.deductionsPayrollTypes = [];
			jsonData.earnHeadIds = [];
			jsonData.deduHeadIds = [];
			$('#payrollEarningTable thead tr:last th').each(function() {
				var earnHeadId = $(this).attr('id');
				if(earnHeadId != undefined){
					jsonData.earnHeadIds.push(earnHeadId);
				}
				});
			
			$('#payrollDeductionTable thead tr:last th').each(function() {
				var deduHeadId = $(this).attr('id');
				if(deduHeadId != undefined){
					jsonData.deduHeadIds.push(deduHeadId);
				}
				});
			for(var i=0;i<noOfEarnCols;i++){
				var earnings = $("#payrollEarningTable > tbody > tr[id="+userId+"] input[id='userActualMontEarning"+i+"']").val();
				var payrollType = $("#payrollEarningTable input[id='userActualMontEarning"+i+"']").closest('td').attr('id');
				jsonData.earningsData.push(earnings);
				jsonData.earningPayrollTypes.push(payrollType);
			}
			jsonData.monthlyIncActualTotal = $(this).find("td input[class='monthlyIncActualTotal']").val();
			for(var i=0;i<noOfDedCols;i++){
				var deductions = $("#payrollDeductionTable > tbody > tr[id="+deduTrId+"] input[id='userMontActualDeduction"+i+"']").val();
				var payrollType = $("#payrollDeductionTable input[id='userMontActualDeduction"+i+"']").closest('td').attr('id');
				if(!isEmpty(deductions)) {
                    jsonData.deductionsData.push(deductions);
                }else{
                    jsonData.deductionsData.push(0);
				}
                if(!isEmpty(payrollType)) {
                    jsonData.deductionsPayrollTypes.push(payrollType);
                }else{
                    jsonData.deductionsPayrollTypes.push(0);
				}
			}
			//jsonData.monthlyDedActualTotal = $("#payrollDeductionTable input[id='userMontDeductionActualTotal"+ct+"']").val();
			jsonData.monthlyDedActualTotal = $("#payrollDeductionTable > tbody > tr[id="+deduTrId+"] input[id='userMontDeductionActualTotal']").val();
			jsonData.netPayActual = $("#payrollDeductionTable input[id='netPayActual"+ct+"']").val();
            multipleItemsData.push(JSON.stringify(jsonData));
		}
		ct ++ ;
	});
	return multipleItemsData;
}

function calcOnIncChange(elem){
	var elemId = $(elem).closest('td').attr('id');
	var pFTotalEarnHead = ($(elem).attr('id')).substring(21,22);
	var parentTr = $(elem).closest('tr').attr('id');
	var transactionEntityId=parentTr.substring(7, parentTr.length);
	var dedTr = 'actDed'+ transactionEntityId;
	var i=0, totalInc = 0.0, totalDed = 0.0, totalEarnHead = 0.0;
	$("#payrollEarningTable > tbody > tr[id="+parentTr+"] > td").each(function() {
		var income = $("#payrollEarningTable tbody tr[id="+parentTr+"] input[id='userActualMontEarning"+i+"']").val();
		if(income != undefined){
			totalInc += parseFloat(income); 
		}
		i++;
	});
	$("#payrollEarningTable > tbody > tr[id="+parentTr+"] > td > input[id='userMontEarningActualTotal']").val(totalInc);
	totalDed = $("#payrollDeductionTable > tbody > tr[id="+dedTr+"] > td > input[id='userMontDeductionActualTotal']").val();
	$("#payrollDeductionTable > tbody > tr[id="+dedTr+"] > td > input[class='netPayActual']").val(parseFloat(totalInc)-parseFloat(totalDed));
	$("#payrollEarningTable > tbody > tr").each(function() {
		var trId = $(this).attr('id');
		var earnHeadVal = $("#"+trId+" > td[id="+elemId+"] > input").val();
		if(earnHeadVal != undefined && earnHeadVal != ""){
		totalEarnHead += parseFloat(earnHeadVal); 
		}
	});
	$("#payrollEarningTable tbody tr:last input[id='totalEarnHead"+pFTotalEarnHead+"']").val(totalEarnHead.toFixed(2));
}

function calcOnDedChange(elem){
	var elemId = $(elem).closest('td').attr('id');
	var pFTotalDeduHead = ($(elem).attr('id')).substring(23,24);
	var parentTr = $(elem).closest('tr').attr('id');
	var transactionEntityId=parentTr.substring(6, parentTr.length);
	var earnTr = 'actEarn'+ transactionEntityId;
	//alert(dedTr);
	var i=0, totalInc = 0.0, totalDed = 0.0, totalDeduHead = 0.0;
	$("#payrollDeductionTable > tbody > tr[id="+parentTr+"] > td").each(function() {
		var deduction = $("#payrollDeductionTable tbody tr[id="+parentTr+"] input[id='userMontActualDeduction"+i+"']").val();
		if(deduction != undefined){
			totalDed += parseFloat(deduction); 
		}
		i++;
	});
	$("#payrollDeductionTable > tbody > tr[id="+parentTr+"] > td > input[id='userMontDeductionActualTotal']").val(totalDed);
	totalInc = $("#payrollEarningTable > tbody > tr[id="+earnTr+"] > td > input[id='userMontEarningActualTotal']").val();
	$("#payrollDeductionTable > tbody > tr[id="+parentTr+"] > td > input[class='netPayActual']").val(parseFloat(totalInc)-parseFloat(totalDed));
	$("#payrollDeductionTable > tbody > tr").each(function() {
		var trId = $(this).attr('id');
		var deduHeadVal = $("#"+trId+" > td[id="+elemId+"] > input").val();
		if(deduHeadVal != undefined && deduHeadVal != ""){
			totalDeduHead += parseFloat(deduHeadVal); 
		}
	});
	$("#payrollDeductionTable tbody tr:last input[id='totalDedHead"+pFTotalDeduHead+"']").val(totalDeduHead.toFixed(2));
}

$(function(){
	$("#payslipDate").datepicker({
		dateFormat: 'MM,yy',
        changeMonth: true,
        changeYear: true,
        showButtonPanel: true,

        onClose: function(dateText, inst) {
            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
            $(this).val($.datepicker.formatDate('MM,yy', new Date(year, month, 1)));
        }
	});
	$('#payslipDate').focus(function () {
        $(".ui-datepicker-calendar").hide();
        $("#ui-datepicker-div").position({
            my: "center top",
            at: "center bottom",
            of: $(this)
        });
    });
	
	$("#searchPayslipButton").on('click',function(){
		var month = $("#payslipDate").val();
		if(month == "") {
			swal("Invalid Month!", "Please Select Month First", "warning");
		}
		var jsonData = {};
		var useremail=$("#hiddenuseremail").text();  
	    jsonData.usermail = useremail;
	    jsonData.month = month;
	    var url = "/payroll/payslipForMonth"; // MAP THIS URL TO YOUR BACKEND FUNCTION FOR PAYSLIP HISTORY
	    $.ajax({
	        url : url,
	        data : JSON.stringify(jsonData),
	        type : "text",
	        headers:{
	            "X-AUTH-TOKEN": window.authToken
	        },
	        method : "POST",
	        contentType : 'application/json',
	        success : function(data) {
	        	$("#payslipHistoryTable > tbody").html("");
	        	if(data.month != "") {
	            	var j = 0;
	            	var row=[];
	            	row[j++] = "<tr id='myPayroll"+data.userId+"'><td id='month' value="+data.month+">"+data.month;
	            	row[j++] = "</td><td>"+data.grossPay;
	            	row[j++] = "</td><td>"+data.netPay;
	            	row[j++] = '</td><td><span class="btn-idos-flat-white btn-download m-top-10" style="margin-left: 5px;" id="downloadPayslipES" onclick="generatePayslipEngtScn(this);"><a href="#"><i class="fa fa-download pr-5"></i>Download</a></span></td></tr>';
	            	$("#payslipHistoryTable > tbody").append(row.join(" "));
	        	}
	        },
	        error: function (xhr, status, error) {
		   		if(xhr.status == 401){ doLogout();
		   		}else if(xhr.status == 500){
		    		swal("Error on Fetching Payslip History!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
	    });
	});
});

function showPaySlipHistory() {
	
	//var useremail=$("#hiddenuseremail").text();    
    //var jsonData = {};
    //jsonData.usermail = useremail;
    var url = "/payroll/payslipHistory"; // MAP THIS URL TO YOUR BACKEND FUNCTION FOR PAYSLIP HISTORY
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        url : url,
      //  data : JSON.stringify(jsonData),
      //  type : "text",
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        method : "GET",
      //  contentType : 'application/json',
        success : function(data) {
            $("#payslipHistoryTable > tbody").html("");
            for(var i=0; i<data.payslipHistry.length; i++){
            	var j = 0;
            	var row=[];
            	row[j++] = '<tr id="myPayroll'+data.payslipHistry[i].userId+'"><td id="month" value="'+data.payslipHistry[i].month+'">'+data.payslipHistry[i].month;
            	row[j++] = '</td><td>'+data.payslipHistry[i].grossPay;
            	row[j++] = '</td><td>'+data.payslipHistry[i].netPay;
            	row[j++] = '</td><td><span class="btn-idos-flat-white btn-download m-top-10" style="margin-left: 5px;" id="downloadPayslipES" onclick="generatePayslipEngtScn(this);"><a href="#"><i class="fa fa-download pr-5"></i>Download</a></span></td></tr>';
            	$("#payslipHistoryTable > tbody").append(row.join(" "));
            }
        },
        error: function (xhr, status, error) {
	   		if(xhr.status == 401){ doLogout();
	   		}else if(xhr.status == 500){
	    		swal("Error on Fetching Payslip History!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
    });
}

function generatePayslipEngtScn(elem){
	var jsonData = {};
	var parentTr = $(elem).closest('tr').attr('id');
	//var period = $("#payslipHistoryTable  tr[id="+parentTr+"]  td[id='month']").attr('value');
	var period = $(elem).closest('tr').find('td[id=month]').attr('value');
	period = period.replace(","," ");
	jsonData.period=period;   
	 jsonData.userEmail=$("#hiddenuseremail").text();
	 jsonData.payslipusers = parentTr.substring(9,parentTr.length);
	 var url="/payroll/generatePayslip";    
	 downloadFile(url, "POST", jsonData, "Error on payslip generation!");
}
