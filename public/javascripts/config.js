'use strict';
var isGoogleLangLoaded = true;
var isPlbsDataReloadNeeded = true;
var USERST_SELECTED_ITEMS_MAP = {};
var USERST_SAVED_ITEMS_MAP = {};
//End global variable declaration.

//$(function() {
//    return init();
//});


$(function() {
	$("table :text").bind("paste", function(e) {
		if($(this).attr('id')!='searchTxnRefNumber' && $(this).attr('id')!='gstinput1' && $(this).attr('id')!='gstinput2' && $(this).attr('id')!='items' && $(this).attr('id')!='custName' && $(this).attr('id')!='vendName'){
			try {
				swal("Wrong operation!", "This operation is not allowed, please type the data.", "error");
				var valueifthis = $(this).val();
		 		$(this).val(valueifthis);
		 		e.preventDefault();
			} catch (e) {

			}
		}
	});
});

var init = function() {
    document.cookie.split('; ').forEach(function(cookieString) {
        var cookie;
        cookie = cookieString.split("=");
        if ((cookie.length === 2) && (cookie[0] === "authToken")) {
			console.log("authToken : " + cookie[1]);
            return window.authToken = cookie[1];
        }
    });
    if (window.authToken === undefined){
    	console.log("logout due to authToken");
        //return doLogout();
    }

};

//This will allow to call onload function on div
$(function(){
 $('div[onload]').trigger('onload');
});

function populateStaticData(){
	logDebug("Start populateStaticData");
	getVendorData();
	getBranchData();
	getProjectData();
	getCategoryData();
	logDebug("End populateStaticData");
}

$(document).ready(function(){
	$('#transactionTable').delegate('tbody tr', 'click', function () {
		expandRow(this) ;
	});

	$('#transactionTable').delegate('tbody tr input, button, select, textarea', 'click', function (evnt) {
		evnt.stopPropagation();
	});
});


$(document).ready(function(){
	$('#claimDetailsTable').delegate('tbody tr', 'click', function () {
		expandRow(this) ;
	});

	$('#claimDetailsTable').delegate('tbody tr input, button, select, textarea', 'click', function (evnt) {
		evnt.stopPropagation();
	});
});


function traversetopMenu(locHash,id){
	toggleTopMenu(locHash);
}

function toggleTopMenu(locHash){
	// alert("toggleTopMenu") ;
	 showdivandactiveleftmenu(locHash);
}

function showdivandactiveleftmenu(showdiv){
	//alert("showdivandactiveleftmenu") ;
	alwaysScrollTop();
	$(pushToTop).animate({ scrollTop: 0 }, "fast");
	$('.go-to-top').fadeOut();
	$(".dynmBnchBankActList").remove();
	$("#socpnreceiptdetail").find('option:first').prop("selected","selected");
	$("#rcpfccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rcafccpaymentdetail").find('option:first').prop("selected","selected");
	$("#rsaafvpaymentdetail").find('option:first').prop("selected","selected");
	$("#paymentDetails").find('option:first').prop("selected","selected");
	$("#userRoleSpecs").hide();
	$("#customize-account-dialog").hide();
	$("#newadmin").hide();
	$("#newadminButton").hide();
	$(".mainDiv").each(function() {
		var divId=$(this).attr('id');
		$(this).hide();
		$("#"+divId+""+"Id").attr('class',"");
	});
	$(""+showdiv+"").show();
	location.hash = showdiv;
	$(""+showdiv+""+"Id").attr('class',"active");
	$("b[id='duplicacyabel']").each(function() {
		$(this).html("");
	});
	$("tr[id*='dynBranchOfficer']").remove();
	$("tr[id*='dynBranchStatutory']").remove();
	$("tr[id*='dynBranchOperRem']").remove();
	$("tr[id*='dynBranchKeyDep']").remove();
	$("tr[id*='dynBranchInsurence']").remove();
	$("tr[id*='dynBranchBnkAct']").remove();
	$("tr[id*='dynBranchTax']").remove();
	var divarr=["#vendorSetup","#categorySetUp","#itemSetUp","#usersSetup","#branchSetup","#projectSetup","#permissionSetup","#billOfMatSetup","#taxSetup","#companyDetails","#claimSetup","#branchOfficerViewSetup","#myPayrollSetup","#companySetup","#uploadCompanyLogo"];
	$("div[id*='form-container']").each(function() {
		$(this).hide();
	});
	$('.notify-success').hide();
	$("#notificationMessage").html("");
	$("#projectenddate").attr("class","datepicker");
	$("#projectenddate").addClass('calendar');
	$("#uploadChartOfAccount").val("")
	$(".duplabel").html("");
	$(".klBranchSpecfTd").text("");
	$(".itemParentNameDiv").text("");
	$(".inventoryItemInStock").text("");
	$(".customerVendorExistingAdvance").text("");
	$(".resultantAdvance").text("");
	$(".resultantAdvance").text("");
	$("#bocaplunits").removeAttr("readonly");
	$("#bocpraunits").removeAttr("readonly");
	$(".budgetDisplay").text("");
	$(".actualbudgetDisplay").text("");
	$(".branchAvailablePettyCash").html("");
	$(".amountRangeLimitRule").text("");
	$(".searchedContent").html("");
	$(".searched-coadata-container").hide();
	$("#existingCOA").val("");
	$(".discountavailable").text("");
	$(".netAmountDescriptionDisplay").text("");
	var maximumYear=new Date().getFullYear()+30;
	$(".datepicker").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
			var elemName=$(this).attr('name');
			if(elemName=='projectenddate'){
				if($("#projectstartdate").val()==""){
					$(this).val("");
				}
				var d1 = new Date($("#projectenddate").val())
				var d2 = new Date($("#projectstartdate").val())
				if (d1 < d2) {
				   swal("Error","project end date cannot be less than project start date","error");
				   $(this).val("");
				   return true;
				}
			}
		        $(this).focus();
		    }
	});
	$(''+showdiv+' input[type="text"]').val("");
	$(".transactionDetailsTable").each(function(){
		$(this).hide();
	});
	$(''+showdiv+' select').find('option:first').prop("selected","selected");
	$(''+showdiv+' textarea').val("");
	$("#userRole option:selected").each(function () {
	       $(this).removeAttr('selected');
	});
	$(".multipleDropdown option:selected").each(function () {
	    $(this).removeAttr('selected');
	});
	$('.multipleDropdown').multiselect('rebuild');

	$(".multipleDropdownForSearch option:selected").each(function () {
	    $(this).removeAttr('selected');
	});
	$('.multipleDropdownForSearch').multiselect('rebuild');

	$(".multiBranch").each(function () {
		$(this).removeAttr('selected');
	});
	$('.multiBranch').multiselect('rebuild');
	$('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
	    $('input[name="vendoritemcheck"][value=\''+this.value+'\']').prop('checked', false);
	}).get();
	$('input[name="unitPrice"]').map(function () {
		if($(this).val().trim()!=""){
			$(this).val("0.0");
		}
	}).get();
	$("#vendordropdown").text("None Selected");
	$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");

	/*$('input[name="customeritemcheck"]:checkbox:checked').map(function () {
	    $('input[name="customeritemcheck"][value=\''+this.value+'\']').prop('checked', false);
	}).get();*/

	$('input[name="custDiscount"]').map(function () {
		if($(this).val().trim()!=""){
			$(this).val("0.0");
		}
	}).get();
	$("#customerdropdown").text("None Selected");
	$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
	if(showdiv=="#companyDetails"){
		getOrganizationDeatils();
	}
	if(showdiv=="#companySetup"){
		$("#companySetup").show();
		getCompanyOrgList();
	}
	if(showdiv=="#uploadCompanyLogo"){
		$("#uploadCompanyLogo").show();
	}
	if(showdiv=="#cashierCount"){
		displayCashierInformation();
	}
	if(showdiv=="#labourHiring"){
		$(".newlabourform-container").hide();
		$(".hiredLabourform-container").show();
		$(".submitHiringRequest").text("Submit Request");
		$(".submitHiringRequest").removeAttr("disabled");
	}
	if(showdiv=="#vendorSetup"){
		//$(".vendorFreeTextSearchButton").trigger('click');
		$("#vendorTableListDiv").show();
		//loadCoaExpenseItems();
	}
	if(showdiv=="#customerSetup"){
		//$(".customerFreeTextSearchButton").trigger('click');
		//loadCoaIncomeItems();
	}
	if(showdiv=="#usersSetup"){
		$("#travelCliam-container-close").trigger('click');
		$("#payrollsetup-container-close").trigger('click');
		/*searchUsers();
		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		ajaxCall('/claims/getAvailableTravelExpenseGroups', jsonData, '', '', '', '', 'availabelTravelExpenseGroups', '', true);
		*/
	}
	if(showdiv=="#branchSetup"){
		$(".newbranchform-container,.newwarehouseform-container,.viewwarehouseform-container").hide();
		$("#branchSetup").show();
		$('#branchesList').show();
	}else if(showdiv=="#itemSetUp"){
		showChartOFAccount();
	}else if(showdiv == "#billOfMatSetup"){
		$('#billOfMatSetup').show();
        getBillofMaterial();
	}

	$("div[class='mainDiv'], .btn-div-top").hide();
	//alert("End showdivandactiveleftmenu");
}

function formCancel1(){
	$("#payrollsetup-container").hide();
	alwaysScrollTop();
}

function leftmenuhideunhide(id,href){
	//alert("leftmenuhideunhide" );
	if(id=="systemconfigadminId1" && href=="#companyDetails"){
		getOrganizationDeatils();
		showdivandactiveleftmenu('#companyDetails');
		$("#companyDetails input:enabled:visible:first").focus();
	}
	if(id=="systemconfigadminId3"){
		if(href="#staticchangepassword"){
			$("#staticchangepassword input:enabled:visible:first").focus();
		}
	}
	if(id=="systemconfigadminId1" && href=="#pendingExpense"){
		showdivandactiveleftmenu('#pendingExpense');
	}
	if(id=="systemconfiglabourId"){
		showdivandactiveleftmenu('#labourHiring');
		$("#labourHiring input:enabled:visible:first").focus();
	}
	$('.notify-success').hide();
	toggleTopMenu(id);
}


var closeFaq=null;
$(document).ready(function(){//0n top menu click

	$('.faqclicks').click(function(){
		if(closeFaq==null||closeFaq=="") {
			closeFaq = window.open("/generalFAQ");
			closeFaq.onbeforeunload = function() {
					 closeFaq = null;
			}
		}
	});

	$('div.sidebar-nav-fixed a' ).click(function() {
		$("#configsetup .label-list").each(function() {
			if($(this).attr('id')!="orgLabel"){
				$(this).html("");
			}
		});

		//alert("div.sidebar-nav-fixed a");

		var divRef=this.href;
		var n=divRef.lastIndexOf("/");
		var str=divRef.substring(n+1, divRef.length);
		str="#"+str;
		if(str=="#pendingExpense"){
			 $("#pendingExpense").show();
			 $("#pendingExpenseId").attr("class","active");
			 $('#transactionPurpose:first').prop("selected","selected");
			 $('#payMentType:first').prop("selected","selected");
			 $('#specificsoption:first').prop("selected","selected");
			 $('#vendor option:first').prop("selected","selected");
			 $('#docuploadurl').val("");
			 $('#noofitems').val("");
			 $('#tamount').val("");
			 $('#remark').val("");
			 showdivandactiveleftmenu('#pendingExpense');
			 $("#transactionTable tbody").html("");
			 $("#transactionTable tbody").html(userTransactionListString);
			 $('#searchTransaction').hide();
			 getUserTransactions(0, PER_PAGE_TXN);
		}else if(str=="#companySetup"){
			showdivandactiveleftmenu('#companySetup');
		}else if(str=="#uploadCompanyLogo"){
			showdivandactiveleftmenu('#uploadCompanyLogo');
		}else if(str=="#companyDetails"){
			//getOrganizationDeatils(); sunil: not needed, get called from showdivandactiveleftmenu
			showdivandactiveleftmenu('#companyDetails');
		}else if(str=="#categorySetUp"){
			showdivandactiveleftmenu('#categorySetUp');
		}else if(str=="#vendorSetup"){
			showdivandactiveleftmenu('#vendorSetup');
            loadCoaExpenseItems();
		}else if(str=="#usersSetup"){
			showdivandactiveleftmenu('#usersSetup');
            getAllUsers();
			onUserScreenLoad();
		}else if(str=="#branchSetup"){
			showdivandactiveleftmenu('#branchSetup');
			//ajaxCall('/config/getbranchadministratordatas', '', '', '', 'GET', '', 'getbranchadministratordatas', '', true);
		}else if(str=="#projectSetup"){
			showdivandactiveleftmenu('#projectSetup');
		}else if(str=="#labourHiring"){
			var useremail=$("#hiddenuseremail").text();
			getLabourData(useremail);
			showdivandactiveleftmenu('#labourHiring');
		}else if(str=="#permissionSetup"){
			showdivandactiveleftmenu('#permissionSetup');
		}else if(str=="#itemSetUp"){

			showdivandactiveleftmenu('#itemSetUp');

			//$("#mainChartOfAccount").remove();


			/* $("#mainChartOfAccount").each(function() {
					$(this).remove();
			});


			$("#mainChartOfAccount li[id='1000000000000000000']").find("ul[id='mainChartOfAccount']").remove();
			$("#mainChartOfAccount li[id='1000000000000000000']").find("img[id='1000000000000000000']").attr('src',"/assets/images/new.v1370889834.png");
			$("#mainChartOfAccount li[id='1000000000000000000']").find("img[id='1000000000000000000']").attr('onclick',"javascript:getChildChartOfAccount(this)");
			$("#mainChartOfAccount li[id='2000000000000000000']").find("ul[id='mainChartOfAccount']").remove();
			$("#mainChartOfAccount li[id='2000000000000000000']").find("img[id='2000000000000000000']").attr('src',"/assets/images/new.v1370889834.png");
			$("#mainChartOfAccount li[id='2000000000000000000']").find("img[id='2000000000000000000']").attr('onclick',"javascript:getChildChartOfAccount(this)");
			$("#mainChartOfAccount li[id='3000000000000000000']").find("ul[id='mainChartOfAccount']").remove();
			$("#mainChartOfAccount li[id='3000000000000000000']").find("img[id='3000000000000000000']").attr('src',"/assets/images/new.v1370889834.png");
			$("#mainChartOfAccount li[id='3000000000000000000']").find("img[id='3000000000000000000']").attr('onclick',"javascript:getChildChartOfAccount(this)");
			$("#mainChartOfAccount li[id='4000000000000000000']").find("ul[id='mainChartOfAccount']").remove();
			$("#mainChartOfAccount li[id='4000000000000000000']").find("img[id='4000000000000000000']").attr('src',"/assets/images/new.v1370889834.png");
			$("#mainChartOfAccount li[id='4000000000000000000']").find("img[id='4000000000000000000']").attr('onclick',"javascript:getChildChartOfAccount(this)");
			*/
		}else if(str=="#budgetSetup"){
			showdivandactiveleftmenu('#budgetSetup');
			getBudgetDetails();
		}else if(str=="#customerSetup"){
			showdivandactiveleftmenu('#customerSetup');
            loadCoaIncomeItems();
		}else if(str=="#billOfMatSetup"){
			showdivandactiveleftmenu('#billOfMatSetup');
		}else if(str=="#taxSetup"){
			showdivandactiveleftmenu('#taxSetup');
            setPlaceOfSupplyType();
            var isCompositionSchemeApply = $("#isCompositionSchemeApply").val();
            if(isCompositionSchemeApply == 1) {
            	$('.taxsetupcls').hide();
            	$('#outputTaxTab').hide();
            	$('#inputTaxTab a').click();
            	$('#inputTaxDiv').show();
            }else {
            	$('#taxableItemTab').hide();
            }

		}else if(str=="#cashierCount"){
			showdivandactiveleftmenu('#cashierCount');
		}else if(str=="#claimSetup"){
			showdivandactiveleftmenu('#claimSetup');
			getClaimsTransactions(1000);

		}else if(str=="#myPayrollSetup"){
			showdivandactiveleftmenu('#myPayrollSetup');
			showPaySlipHistory();

		}else if(str=="#dashBoard"){
			showdivandactiveleftmenu('#dashBoard');
			$("#financialDataDashboard").show();
			$("#operationalDataDashboard").hide();
			var useremail=$("#hiddenuseremail").text();
		}else if(str=="#branchOfficerViewSetup"){
			showdivandactiveleftmenu('#branchOfficerViewSetup');
		}else if(str=="#branchOfficerViewSetup"){
			showdivandactiveleftmenu('#branchOfficerViewSetup');
		}else if(str=="#vendCustTransactions"){
			$("#vendCustTransactions").show();
			$("#transactionStatement").hide();
			$("#vendCustTransactionId").attr('class',"active");
			$("#transactionStatementId").attr('class',"");
		}else if(str=="#transactionStatement"){
			$("#transactionStatement").show();
			$("#vendCustTransactions").hide();
			$("#transactionStatementId").attr('class',"active");
			$("#vendCustTransactionId").attr('class',"");
			var accountOrganization=GetURLParameter('accountOrganization');
			ajaxCall('/vendorcustomer/branchProject/'+accountOrganization, '', '', '', 'GET', '', 'getVendorCustomerBPSuccess', '', false);
		}else if(str=="#supportCenter"){

			showdivandactiveleftmenu('#supportCenter');
			//getSupportCases();
			/*


			   $('#supportCenter').slideUp('slow',function(){
			    $('#supportMyCases').css({'width': '0', 'min-width': '0'});
			    $('#supportExtra').css({'top': '0'});
			    $('#supportHistory').hide();
			   });
			  }

			  if($('#createNewNote').is(':visible')){
			   $('#createNewNote').slideUp('slow', function(){
			    animateNotesTable(false);
			    $('#notesRemarks').hide();
			   });
				 getNotesCount();
			   */
		}
		alwaysScrollTop();
	});
});


function branchWiseCashExpenseReceivablesPayables(elem){
	var jsonData = {};
	var tabId=$(elem).attr('id');
	var useremail=$("#hiddenuseremail").text();
	jsonData.tabElement = tabId;
	jsonData.usermail = useremail;
	var url="/user/branchWiseApproverCashBankReceivablePayables";
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
			logDebug("start branchWiseCashExpenseReceivablesPayables");
			$("#staticcashbankreceivablepayablebranchwisebreakup h4").text("Branch Wise-Breakups");
			$("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").hide();
			if(tabId=="cashBalanceAllBranches"){
				$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		    	$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		    	$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		    	$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		    	if(data.branchWiseCashBankRecivablesPayablesData.length>0){
		    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchCashBreakupTable" style="margin-top: 0px; width:450px;">'+
		    		'<thead class="tablehead1" style="position:relative"><th>Branch Name</th><th>Cash Balance</th><th>Pettycash Balance</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			    	for(var i=0;i<data.branchWiseCashBankRecivablesPayablesData.length;i++){
						//console.log("=====" + data.branchWiseCashBankRecivablesPayablesData[i].cashBalance);
			    		var branchAmountArr=data.branchWiseCashBankRecivablesPayablesData[i].cashBalance.split(":");
			    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchCashBreakupTable'] tbody").append('<tr><td>'+branchAmountArr[0]+'</td><td>'+branchAmountArr[1]+'</td><td>'+branchAmountArr[2]+'</td><tr>');
			    	}
		    	}
			}else if(tabId=="bankBalanceAllBranches"){
				$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		    	$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		    	$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		    	$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		    	if(data.branchWiseCashBankRecivablesPayablesData.length>0){
		    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchBankCashBreakupTable" style="margin-top: 0px; width:450px;">'+
		    		'<thead class="tablehead1" style="position:relative"><th>Branch Name</th><th>Bank Balance</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			    	for(var i=0;i<data.branchWiseCashBankRecivablesPayablesData.length;i++){
			    		var branchAmountArr=data.branchWiseCashBankRecivablesPayablesData[i].bankBalance.split(":");
			    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchBankCashBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="bankwiseBalances" onclick="displayBankwiseBalances(this, \''+ branchAmountArr[2] +'\');">' + branchAmountArr[0] + '</a></td><td>'+branchAmountArr[1]+'</td><tr>');
			    	}
		    	}
			} else if(tabId=="accountsReceivablesAllBranches"){
				$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		    	$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		    	$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		    	$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		    	if(data.branchWiseCashBankRecivablesPayablesData.length>0){
		    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivablesBreakupTable" style="margin-top: 0px; width:450px;">'+
		    		'<thead class="tablehead1" style="position:relative"><th>Branch Name</th><th>Amount Receivables</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			    	for(var i=0;i<data.branchWiseCashBankRecivablesPayablesData.length;i++){

						var branchAmountArr=data.branchWiseCashBankRecivablesPayablesData[i].accountsReceivables.split(":");

			    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivablesBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="customerReceivables" onclick="displayCustVend(this, \''+ branchAmountArr[2] +'\');">'+branchAmountArr[0]+'</a></td><td>'+branchAmountArr[1]+'</td><tr>');
			    	}
		    	}
			} else if(tabId=="accountsPayablesAllBranches"){
				$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		    	$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		    	$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		    	$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		    	if(data.branchWiseCashBankRecivablesPayablesData.length>0){
		    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchPayablesBreakupTable" style="margin-top: 0px; width:450px;">'+
		    		'<thead class="tablehead1" style="position:relative"><th>Branch Name</th><th>Amount Payables</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			    	for(var i=0;i<data.branchWiseCashBankRecivablesPayablesData.length;i++){
			    		var branchAmountArr=data.branchWiseCashBankRecivablesPayablesData[i].accountsPayables.split(":");
			    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchPayablesBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="vendorPayables" onclick="displayCustVend(this, \''+ branchAmountArr[2] +'\');">'+branchAmountArr[0]+'</a></td><td>'+branchAmountArr[1]+'</td><tr>');
			    	}
		    	}
			}
			logDebug("end branchWiseCashExpenseReceivablesPayables");
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function displayBankwiseBalances(elem, branchID){
	logDebug("start displayBankwiseBalances");
	var jsonData = {};
	//var branchName=$(elem).text();
	var useremail=$("#hiddenuseremail").text();
	var modelFor=$(elem).attr('class');
	//jsonData.bnchName = branchName;
	jsonData.branchID = branchID ;
	jsonData.usermail = useremail;
	jsonData.txnModelFor=modelFor;
	ajaxCall('/user/branchCustomerVendorReceivablePayables', jsonData, '', '', '', '', 'displayBankwiseBalancesSuccess', '', true);
	logDebug("end displayBankwiseBalances");
}

function displayBankwiseBalancesSuccess(data){
	if(data.result){
		location.hash="#pendingExpense";
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		if(data.branchCustomerVendorReceivablePayablesData.length>0){
			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable" style="margin-top: 0px; width:450px;">'+
    		'<thead class="tablehead1" style="position:relative"><th>Bank Name</th><th>Balance</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			for(var i=0;i<data.branchCustomerVendorReceivablePayablesData.length;i++){
				$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#pendingExpense" class="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" onclick="showBranchWiseBreakUp(this);">'+data.branchCustomerVendorReceivablePayablesData[i].branchName+'</a>');
				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivableCustomerVendorBreakupTable'] tbody").append('<tr><td><div style="width:180px;">'+data.branchCustomerVendorReceivablePayablesData[i].bankName+'</div></td><td><div style="width:140px;">'+data.branchCustomerVendorReceivablePayablesData[i].bankBalance+'</div></td></tr>');
			}
		}
	}
}


function displayCustVend(elem, branchID){
	var jsonData = {};
	//var branchName=$(elem).text();
	var modelFor=$(elem).attr('class');
	//jsonData.bnchName = branchName;
    $("#staticcashbankreceivablepayablebranchwisebreakup #staticBranchID").val(branchID);
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.branchID = branchID;
	jsonData.txnModelFor=modelFor;
	ajaxCall('/user/branchCustomerVendorReceivablePayables', jsonData, '', '', '', '', 'displayCustVendSuccess', '', true);
}


function displayCustVendSuccess(data){
	logDebug("start displayCustVendSuccess");
	if(data.result){
		location.hash="#pendingExpense";
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		if(data.branchCustomerVendorReceivablePayablesData.length>0){
			//$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable" style="margin-top: 0px; width:450px;">'+
    		//'<thead class="tablehead1" style="position:relative"><th>Name</th><th>Balance</th><th></th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable" style="margin-top: 0px; width:450px;">'+
			'<thead class="tablehead1" style="position:relative"><th>More Info</th><th>Name</th><th>Balance</th><th>OVER 180 Days</th><th>UNDER 180 Days</th><tr></thead><tbody style="position:relative"></tbody></table></div>');

			for(var i=0;i<data.branchCustomerVendorReceivablePayablesData.length;i++){
				$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#" class="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" onclick="showBranchWiseBreakUp(this);">'+data.branchCustomerVendorReceivablePayablesData[i].branchName+'</a>');

				//$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivableCustomerVendorBreakupTable'] tbody").append('<tr><td><div style="width:180px;">'+data.branchCustomerVendorReceivablePayablesData[i].customerName+'</div></td>'+
                $("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivableCustomerVendorBreakupTable'] tbody").append('<tr><td><img src="/assets/images/moreinfo.png" class="pl-5 pr-5" height="30" width="30" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="OVER 180 Days" onClick="showCustCOAGraphs(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');"></td><td><div style="width:180px;">'+data.branchCustomerVendorReceivablePayablesData[i].customerName+'</div></td>'+

				'<td><div style="width:140px;">'+data.branchCustomerVendorReceivablePayablesData[i].netAmount+'</div></td>'+

				'<td><div style="width:140px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="OVER 180 Days" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].over180daysamount+'</a></div></td>'+

				'<td><div style="width:140px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="UNDER 180 Days" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under180daysamount+'</a></div></td><tr>');
			}
		}
	}
	logDebug("end displayCustVendSuccess");
}



function overUnderOneEightyDaysCustVendTransaction(elem, branchID){
	var buttonFor="";
	if($(elem).attr('title')=="OVER 180 Days"){
		buttonFor="over";
	}
	else if($(elem).attr('title')=="UNDER 180 Days"){
		buttonFor="under";
	}else {
		buttonFor=$(elem).attr('title');
	}
	var jsonData = {};
	jsonData.custVendId=$(elem).attr('id');
	jsonData.txnModelFor=$(elem).attr('name');
	jsonData.clickedButtonFor=buttonFor;
	jsonData.branchID=branchID;
	/*if(buttonFor=="over"){
		jsonData.txnModelFor="customerReceivables";
	}
	if(buttonFor=="under"){
		jsonData.txnModelFor="vendorPayables";
	}*/
	var useremail=$("#hiddenuseremail").text();
	jsonData.email = useremail;
	jsonData.branchName=$("#staticcashbankreceivablepayablebranchwisebreakup h4 a").text();
	ajaxCall('/user/overUnderOneEightyReceivablePayablesTxn', jsonData, '', '', '', '', 'overUnderOneEightyReceivablePayablesTxnSuccess', '', true);
}

/*function overUnderOneEightyReceivablePayablesTxnSuccess(data){
	logDebug("start overUnderOneEightyReceivablePayablesTxnSuccess");
	if(data.result){
		//location.hash="#pendingExpense";
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		if(data.overUnderOneEightyReceivablePayablesTxnData.length>0){
			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="overUnderOneEightyReceivablePayablesTxnDataTable" style="margin-top: 0px; width:450px;">'+
    		'<thead class="tablehead1" style="position:relative"><th>Txn. Ref. Number</th><th>Amount</th><th>Created By</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			for(var i=0;i<data.overUnderOneEightyReceivablePayablesTxnData.length;i++){
				if(data.overUnderOneEightyReceivablePayablesTxnData[i].clickedButtonFor=="over" || data.overUnderOneEightyReceivablePayablesTxnData[i].clickedButtonFor=="under"){
					$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#pendingExpense" name="'+data.overUnderOneEightyReceivablePayablesTxnData[i].branchName+'" class="'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnModelFor+'" onclick="showCustVend(this);">'+data.overUnderOneEightyReceivablePayablesTxnData[i].vendorName+'</a>: '+data.overUnderOneEightyReceivablePayablesTxnData[i].totalamount+'<button style="float: right; margin-right: 50px;" class="btn btn-submit" name="'+data.overUnderOneEightyReceivablePayablesTxnData[i].clickedButtonFor+'" id="'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnModelFor+'" onclick="downloadOverUnderOneEightyDayaTxnExcel(this);">Download '+data.overUnderOneEightyReceivablePayablesTxnData[i].clickedButtonFor+' 180 Days Transactions</button>');
				}else{
					$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#pendingExpense" name="'+data.overUnderOneEightyReceivablePayablesTxnData[i].branchName+'" class="'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnModelFor+'" onclick="showCustVend(this);">'+data.overUnderOneEightyReceivablePayablesTxnData[i].vendorName+'</a>: '+data.overUnderOneEightyReceivablePayablesTxnData[i].totalamount);
				}
				//$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='overUnderOneEightyReceivablePayablesTxnDataTable'] tbody").append('<tr><td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnRefNumber+'</td><td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].netAmount+'</td><td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].createdBy+'</td></tr>');
				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='overUnderOneEightyReceivablePayablesTxnDataTable'] tbody").append('<tr>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" id="'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnRefNumber+'" onclick="displayTransctionModal(this);">'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnRefNumber+'</a></div></td>'+
				'<td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].netAmount+'</td><td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].createdBy+'</td></tr>');
			}
		}
	}else{
		swal("No data found!", "No record found for last one month", "warning");
	}
	logDebug("end overUnderOneEightyReceivablePayablesTxnSuccess");
}*/

function overUnderOneEightyReceivablePayablesTxnSuccess(data){
	logDebug("start overUnderOneEightyReceivablePayablesTxnSuccess");
	if(data.result){
		//location.hash="#pendingExpense";
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		if(data.overUnderOneEightyReceivablePayablesTxnData.length>0){
			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="overUnderOneEightyReceivablePayablesTxnDataTable" style="margin-top: 0px; width:450px;">'+
    		'<thead class="tablehead1" style="position:relative"><th>Txn. Ref. Number</th><th>Invoice Amount</th><th>Received</th><th>Outstanding Amount</th><th>Created By</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			for(var i=0;i<data.overUnderOneEightyReceivablePayablesTxnData.length;i++){
				if(data.overUnderOneEightyReceivablePayablesTxnData[i].clickedButtonFor=="over" || data.overUnderOneEightyReceivablePayablesTxnData[i].clickedButtonFor=="under"){
					$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#pendingExpense" name="'+data.overUnderOneEightyReceivablePayablesTxnData[i].branchName+'" class="'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnModelFor+'" onclick="showCustVend(this);">'+data.overUnderOneEightyReceivablePayablesTxnData[i].vendorName+'</a>: '+data.overUnderOneEightyReceivablePayablesTxnData[i].totalamount+'<button style="float: right; margin-right: 50px;" class="btn btn-submit" name="'+data.overUnderOneEightyReceivablePayablesTxnData[i].clickedButtonFor+'" id="'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnModelFor+'" onclick="downloadOverUnderOneEightyDayaTxnExcel(this);">Download '+data.overUnderOneEightyReceivablePayablesTxnData[i].clickedButtonFor+' 180 Days Transactions</button>');
				}else{
					$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#pendingExpense" name="'+data.overUnderOneEightyReceivablePayablesTxnData[i].branchName+'" class="'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnModelFor+'" onclick="showCustVend(this);">'+data.overUnderOneEightyReceivablePayablesTxnData[i].vendorName+'</a>: '+data.overUnderOneEightyReceivablePayablesTxnData[i].totalamount);
				}
				//$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='overUnderOneEightyReceivablePayablesTxnDataTable'] tbody").append('<tr><td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnRefNumber+'</td><td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].netAmount+'</td><td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].createdBy+'</td></tr>');
				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='overUnderOneEightyReceivablePayablesTxnDataTable'] tbody").append('<tr>'+
				'<td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].txnRefNumber+'</td>'+'<td>'+'<div style="width:80px;"><a href="#pendingExpense" id="'+data.overUnderOneEightyReceivablePayablesTxnData[i].invoiceValueLinkedTxnIDs+'" onclick="displayTransctionModal(this);">'+data.overUnderOneEightyReceivablePayablesTxnData[i].invoiceValue+'</a></div>'+'</td>'+'<td>'+'<div style="width:80px;"><a href="#pendingExpense" id="'+data.overUnderOneEightyReceivablePayablesTxnData[i].receivedValueLinkedTxnIDs+'" onclick="displayTransctionModal(this);">'+data.overUnderOneEightyReceivablePayablesTxnData[i].receivedValue+'</a></div>'+'</td>'+'<td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].outstandingAmount+'</td><td>'+data.overUnderOneEightyReceivablePayablesTxnData[i].createdBy+'</td></tr>');
			}
		}
	}else{
		swal("No data found!", "No record found for last one month", "warning");
	}
	logDebug("end overUnderOneEightyReceivablePayablesTxnSuccess");
}


function showBranchWiseBreakUp(elem){
	var modelFor=$(elem).attr('class');
	$("#staticcashbankreceivablepayablebranchwisebreakup h4").text("Branch Wise-Breakups");
	if(modelFor=="customerReceivables"){
		$("#accountsReceivablesAllBranches").trigger('click');
	}
	if(modelFor=="vendorPayables"){
		$("#accountsPayablesAllBranches").trigger('click');
	}
	if(modelFor=="bankwiseBalances"){
		$("#bankBalanceAllBranches").trigger('click');
	}
}



/* Sunil
$(document).ready(function() {
	$(".staticcashbankreceivablepayablebranchwisebreakupclose").click(function() {
		location.hash=$(this).attr('href');
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('hide');
	});
});

$(document).ready(function() {
	$(".staticwapbranchwisebreakupclose").click(function() {
		location.hash=$(this).attr('href');
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticwapbranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticwapbranchwisebreakup").modal('hide');
	});
});
*/

function doLogout() {
	$.ajax({
		url: "/logout",
		type: "post",
		dataType:'json',
		headers: {
			"X-AUTH-TOKEN": window.authToken
		},
		success: function(data) {
		//	alert("close FAQ:::",closeFaq);
			if(closeFaq != null){
				closeFaq.close();
				closeFaq == null;
			}
			window.location.href="signout";

		},
		error: function (xhr, status, error) {
			window.location.href="signout";
		},
		complete: function(data) {
			deleteAllCookies();
			isPlbsDataReloadNeeded = true;
		}

	});
}

function deleteAllCookies() {
    var cookies = document.cookie.split(";");

    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i];
        var eqPos = cookie.indexOf("=");
        var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
    }
}

function doSellerLogout() {
    $.ajax({
		url: "/sllrlogout",
		type: "post",
		dataType: 'json',
		async: false,
		headers: {
			"X-AUTH-TOKEN": window.authToken
		},
		success: function(data){
			window.location.href="/signoutsllr";
			//window.location.pathname = "signoutsllr";
		},
		error: function (xhr, status, error) {
			window.location.href="/signoutsllr";
			//window.location.pathname = "signoutsllr";
		}
	});
}

function doVendorLogout() {
    $.ajax({
		url: "/vendorlogout",
		type: "post",
		dataType: 'json',
		async: false,
		headers: {
			"X-AUTH-TOKEN": window.authToken
		},
		success: function(data){
			window.location.href="/signoutvendor";
			//window.location.pathname = "signoutsllr";
		},
		error: function (xhr, status, error) {
			window.location.href="/signoutvendor";
			//window.location.pathname = "signoutsllr";
		}
	});
}

var selectTheme = function(themeid) {
	$("link[id='" + themeid + "']").removeAttr("disabled");
	if( themeid == 'green'){
        $("link[id='silver']").attr("disabled", "disabled");
        $("link[id='magenta']").attr("disabled", "disabled");
        $("link[id='blue']").attr("disabled", "disabled");
        $("link[id='darkBlue']").attr("disabled", "disabled");
		$("link[id='red']").attr("disabled", "disabled");
		$("link[id='silverblue']").attr("disabled", "disabled");
	}else if(themeid == 'magenta'){
        $("link[id='green']").attr("disabled", "disabled");
        $("link[id='silver']").attr("disabled", "disabled");
        $("link[id='blue']").attr("disabled", "disabled");
        $("link[id='darkBlue']").attr("disabled", "disabled");
		$("link[id='red']").attr("disabled", "disabled");
		$("link[id='silverblue']").attr("disabled", "disabled");
	}else if(themeid == 'silver'){
        $("link[id='green']").attr("disabled", "disabled");
        $("link[id='magenta']").attr("disabled", "disabled");
        $("link[id='blue']").attr("disabled", "disabled");
        $("link[id='darkBlue']").attr("disabled", "disabled");
		$("link[id='red']").attr("disabled", "disabled");
		$("link[id='silverblue']").attr("disabled", "disabled");
	}else if(themeid == 'blue'){
        $("link[id='green']").attr("disabled", "disabled");
        $("link[id='magenta']").attr("disabled", "disabled");
        $("link[id='silver']").attr("disabled", "disabled");
        $("link[id='darkBlue']").attr("disabled", "disabled");
		$("link[id='red']").attr("disabled", "disabled");
		$("link[id='silverblue']").attr("disabled", "disabled");
    }else if(themeid == 'darkBlue'){
        $("link[id='green']").attr("disabled", "disabled");
        $("link[id='magenta']").attr("disabled", "disabled");
        $("link[id='silver']").attr("disabled", "disabled");
        $("link[id='blue']").attr("disabled", "disabled");
		$("link[id='red']").attr("disabled", "disabled");
		$("link[id='silverblue']").attr("disabled", "disabled");
    }else if(themeid == 'red'){
		$("link[id='green']").attr("disabled", "disabled");
		$("link[id='magenta']").attr("disabled", "disabled");
		$("link[id='silver']").attr("disabled", "disabled");
		$("link[id='blue']").attr("disabled", "disabled");
		$("link[id='darkBlue']").attr("disabled", "disabled");
		$("link[id='silverblue']").attr("disabled", "disabled");
	}else if(themeid == 'silverblue'){
		$("link[id='green']").attr("disabled", "disabled");
		$("link[id='magenta']").attr("disabled", "disabled");
		$("link[id='silver']").attr("disabled", "disabled");
		$("link[id='blue']").attr("disabled", "disabled");
		$("link[id='darkBlue']").attr("disabled", "disabled");
		$("link[id='red']").attr("disabled", "disabled");
	}
}

var hideInstantViewElements = function(){
	let instantViewElementsHideId = $("#instantViewElementsHideId").val();
	let idosConfigParamValarr = instantViewElementsHideId.split(",");
	for (let i = 0; i < idosConfigParamValarr.length; i++) {
		$("#"+idosConfigParamValarr[i]).hide();
	}
}
