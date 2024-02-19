function closeFinancial(){
	$("#financialDataDashboard").slideUp('slow');
	$("#operationalDataDashboard").slideDown('slow');
	$("#operationalSearchDataDashboard").slideUp('slow');
}

$(document).ready(function(){
	$('.newFinancialsButton'). click(function(){
		$("#financialDataDashboard").slideDown('slow');
		$("#operationalDataDashboard").slideUp('slow');
		$("#operationalSearchDataDashboard").slideUp('slow');
	});
});




/*New Dashboard Starts*/

$(document).ready(function() {
	$('li #dashBoardId').click(function() {
		var usermail = $("#hiddenuseremail").text();
		getDashboardFinancials();
	});
});

function getDashboardFinancials(){
	var startTime=new Date().getTime();
	console.log(startTime);
	var jsonData = {};
	jsonData.userEmail = $("#hiddenuseremail").text();
	jsonData.currDashboardFromDate = $("#currDashboardFromDate").val();
	jsonData.currDashboardToDate = $("#currDashboardToDate").val();;
	jsonData.prevDashboardFromDate = $("#prevDashboardFromDate").val();
	jsonData.prevDashboardToDate = $("#prevDashboardToDate").val();
	jsonData.userEmail = $("#hiddenuseremail").text();
	ajaxCall('/dashboard/getFinancials', jsonData, '', '', 'POST', '', 'getDashboardFinancialsSuccess', '', true);
	var endTime=new Date().getTime();
	console.log(endTime);
}

/*though names are lastFourteen, it is actually for 30days. Similarly though week is used in names like previousWeekcashIncome, it is actully previous and next month data*/
function getDashboardFinancialsSuccess(data){
	if(!isEmpty(data)){
		var res=data.dashBoardData;
		res=res[0];
		var cashExpense=(''==res.cashExpense)?0.00:res.cashExpense,
		pCashExpense=(''==res.previousWeekcashExpense)?0.00:res.previousWeekcashExpense,
		cashIncome=(''==res.cashIncome)?0.00:res.cashIncome,
		pCashIncome=(''==res.previousWeekcashIncome)?0.00:res.previousWeekcashIncome,
		creditExpense=(''==res.creditExpense)?0.00:res.creditExpense,
		pCreditExpense=(''==res.previousWeekcreditExpense)?0.00:res.previousWeekcreditExpense,
		creditIncome=(''==res.creditIncome)?0.00:res.creditIncome,
		pCreditIncome=(''==res.previousWekcreditIncome)?0.00:res.previousWekcreditIncome,
		budgetAvail=(''==res.expBudgetAvail)?0.00:res.expBudgetAvail,
		pBudgetAvail=(''==res.previousWeekexpBudgetAvail)?0.00:res.previousWeekexpBudgetAvail,
		quotationAvail=(''==res.quotationAvail)?0.00:res.quotationAvail,
		pQuotationAvail=(''==res.previousWeekQuotationAvail)?0.00:res.previousWeekQuotationAvail,
		pay=(''==res.netPayableThisWeek)?0.00:res.netPayableThisWeek,
		pPay=(''==res.netPayablePreviousWeek)?0.00:res.netPayablePreviousWeek,
		recieve=(''==res.netRecievableThisWeek)?0.00:res.netRecievableThisWeek,
		pRecieve=(''==res.netRecievablePreviousWeek)?0.00:res.netRecievablePreviousWeek,
		intraGstTurnover=(''==res.intraGstTurnover)?0.00:res.intraGstTurnover,
		interGstTurnover=(''==res.interGstTurnover)?0.00:res.interGstTurnover,
		nonGstTurnover=(''==res.nonGstTurnover)?0.00:res.nonGstTurnover,
		exportTurnover=(''==res.exportTurnover)?0.00:res.exportTurnover,
		totalTurnover=(''==res.totalTurnover)?0.00:res.totalTurnover;

		var isCompositionSchemeOrg=res.isCompositionSchemeOrg;
		displayDashboardValues(cashExpense,pCashExpense,'dCashExpense');
		displayDashboardValues(cashIncome,pCashIncome,'dCashIncome');
		displayDashboardValues(creditExpense,pCreditExpense,'dCreditExpense');
		displayDashboardValues(creditIncome,pCreditIncome,'dCreditIncome');
		displayDashboardValues(budgetAvail,pBudgetAvail,'dExpenseBudget');
		displayDashboardValues(quotationAvail, pQuotationAvail, 'quotationDiv');
		displayDashboardValues(pay,pPay,'dTotalPay');
		displayDashboardValues(recieve,pRecieve,'dTotalRecieve');
		$('#dOtherInfo #bnchWithHighestExpense').html(res.maxExpenseBranch).attr('title',res.maxExpenseBranch);
		$('#dOtherInfo #bnchWithHighestIncome').html(res.maxIncomeBranch).attr('title',res.maxIncomeBranch);
		// Turnover Panel
			$("#turnoverTable").find(".interStateAmt").html("");
			$("#turnoverTable").find(".intraStateAmt").html("");
			$("#turnoverTable").find(".nonGstAmt").html("");
			$("#turnoverTable").find(".exportAmt").html("");
			$("#turnoverTable").find(".totalAmt").html("");
			
			$("#turnoverTable").find(".interStateTax").html("");
			$("#turnoverTable").find(".intraStateTax").html("");
			$("#turnoverTable").find(".nonGstTax").html("");
			$("#turnoverTable").find(".exportTax").html("");
			$("#turnoverTable").find(".totalTax").html("");
			
			$("#turnoverTable").find(".turnoverTaxRate").val("");
			
			
		if(isCompositionSchemeOrg == "true") {
			$("#turnoverTable").find(".interStateAmt").html(interGstTurnover);
			$("#turnoverTable").find(".intraStateAmt").html(intraGstTurnover);
			$("#turnoverTable").find(".nonGstAmt").html(nonGstTurnover);
			$("#turnoverTable").find(".exportAmt").html(exportTurnover);
			$("#turnoverTable").find(".totalAmt").html(totalTurnover);
			
			$("#turnoverInfo").hide();
		}else {
			$("#turnoverInfo").hide();
		}
		for(var i=0;i<data.lastForteenDaysCustomerData.length;i++){
			$("select[class='addedCustomersDropdown']").append('<option>'+data.lastForteenDaysCustomerData[i].lastForteenDaysCustomers+'</option>');
			$("select[class='addedCustomersBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysCustomerData[i].lastForteenDaysCustomers+'/Branches">');
			var custBnch=data.lastForteenDaysCustomerData[i].lastForteenDaysCustomersBranches.split(",");
			for(var j=0;j<custBnch.length;j++){
				$("select[class='addedCustomersBranchesDropdown']").append('<option>'+custBnch[j]+'</option>');
			}
			$("select[class='addedCustomersBranchesDropdown']").append('</optgroup>');
			$("select[class='addedCustomersItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysCustomerData[i].lastForteenDaysCustomers+'/Items">');
			var custItems=data.lastForteenDaysCustomerData[i].lastForteenDaysCustomersItems.split(",");
			for(var k=0;k<custItems.length;k++){
				$("select[class='addedCustomersItemsDropdown']").append('<option>'+custItems[k]+'</option>');
			}
			$("select[class='addedCustomersItemsDropdown']").append('</optgroup>');
		}
		for(var i=0;i<data.lastForteenDaysVendorsData.length;i++){
			$("select[class='addedVendorsDropdown']").append('<option>'+data.lastForteenDaysVendorsData[i].lastForteenDaysVendors+'</option>');
			$("select[class='addedVendorsBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysVendorsData[i].lastForteenDaysVendors+'/Branches">');
			var vendBnch=data.lastForteenDaysVendorsData[i].lastForteenDaysVendorBranches.split(",");
			for(var j=0;j<vendBnch.length;j++){
				$("select[class='addedVendorsBranchesDropdown']").append('<option>'+vendBnch[j]+'</option>');
			}
			$("select[class='addedVendorsBranchesDropdown']").append('</optgroup>');
			$("select[class='addedVendorsItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysVendorsData[i].lastForteenDaysVendors+'/Items">');
			var vendItems=data.lastForteenDaysVendorsData[i].lastForteenDaysVendorItems.split(",");
			for(var k=0;k<vendItems.length;k++){
				$("select[class='addedVendorsItemsDropdown']").append('<option>'+vendItems[k]+'</option>');
			}
			$("select[class='addedVendorsItemsDropdown']").append('</optgroup>');
		}
		for(var i=0;i<data.lastForteenDaysUsersData.length;i++){
			$("select[class='addedUsersDropdown']").append('<option>'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'</option>');
			$("select[class='addedUsersBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Branches">');
			$("select[class='addedUsersBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Transaction Creation Right In Branches">');
			var creationRightBnchs=data.lastForteenDaysUsersData[i].lastForteenDaysUsersCreationRightForBranches.split(",");
			for(var j=0;j<creationRightBnchs.length;j++){
				$("select[class='addedUsersBranchesDropdown']").append('<option>'+creationRightBnchs[j]+'</option>');
			}
			$("select[class='addedUsersBranchesDropdown']").append('</optgroup>');
			$("select[class='addedUsersBranchesDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Transaction Approval Right In Branches">');
			var approverRightBnchs=data.lastForteenDaysUsersData[i].lastForteenDaysUsersApprovalRightForBranches.split(",");
			for(var k=0;k<approverRightBnchs.length;k++){
				$("select[class='addedUsersBranchesDropdown']").append('<option>'+approverRightBnchs[k]+'</option>');
			}
			$("select[class='addedUsersBranchesDropdown']").append('</optgroup>');
			$("select[class='addedUsersBranchesDropdown']").append('</optgroup>');
			$("select[class='addedUsersRolesDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Roles">');
			var userRoles=data.lastForteenDaysUsersData[i].lastForteenDaysUsersRoles.split(",");
			for(var l=0;l<userRoles.length;l++){
				$("select[class='addedUsersRolesDropdown']").append('<option>'+userRoles[l]+'</option>');
			}
			$("select[class='addedUsersRolesDropdown']").append('</optgroup>');
			$("select[class='addedUsersItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Items">');
			$("select[class='addedUsersItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Transaction Creation Right For Items">');
			var creationRightForItems=data.lastForteenDaysUsersData[i].lastForteenDaysUsersCreationRightForItems.split(",");
			for(var m=0;m<creationRightForItems.length;m++){
				$("select[class='addedUsersItemsDropdown']").append('<option>'+creationRightForItems[m]+'</option>');
			}
			$("select[class='addedUsersItemsDropdown']").append('</optgroup>');
			$("select[class='addedUsersItemsDropdown']").append('<optgroup label="'+data.lastForteenDaysUsersData[i].lastForteenDaysUsers+'/Transaction Approval Right For Items">');
			var approverRightForItems=data.lastForteenDaysUsersData[i].lastForteenDaysUsersApprovalRightForItems.split(",");
			for(var n=0;n<approverRightForItems.length;n++){
				$("select[class='addedUsersItemsDropdown']").append('<option>'+approverRightForItems[n]+'</option>');
			}
			$("select[class='addedUsersItemsDropdown']").append('</optgroup>');
			$("select[class='addedUsersItemsDropdown']").append('</optgroup>');
		}
		$("#auditorFinancialTable tr[class='auditorCreditIncome'] div[id='addedTxnPendingApproval']").prepend(data.dashBoardData[0].forteenBackDatePendingApproval);
		for(var z=0;z<data.lastForteenDaysPendingApprovalData.length;z++){
			$("select[class='addedTxnPendingApprovalDropdown']").append('<option>'+data.lastForteenDaysPendingApprovalData[z].forteenBackDatePendingApprovalTxnRef+'</option>');
		}
		$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<optgroup label="Transaction Exceeding Budget">');
		for(q=0;q<data.lastForteenDaysTxnExceedingBudgetAWHData.length;q++){
			$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<optgroup id="txnSpecificsBudgetRuleNotFollowed" classname="'+data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNoAHSpecificsId+'" label="'+data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNoAH+'">');
			var ahwTxnExceedingBudgedRefNumber=data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNo.split(',');
			for(var u=0;u<ahwTxnExceedingBudgedRefNumber.length;u++){
				$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<option>'+ahwTxnExceedingBudgedRefNumber[u]+'</option>');
			}
			$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('</optgroup>');
			$("select[class='txnExceedingBudgetKlNotFollowedGroupDropdown']").append('<option value="'+data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNoAHSpecificsId+'">'+data.lastForteenDaysTxnExceedingBudgetAWHData[q].txnExceedingBudgetBranchRefNoAH+'</option>');
		}
		$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('</optgroup>');
		$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<optgroup label="Rules Not Followed">');
		for(var q1=0;q1<data.lastForteenDaysTxnKlNotFollwedAWHData.length;q1++){
			$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<optgroup id="txnSpecificsBudgetRuleNotFollowed" classname="'+data.lastForteenDaysTxnKlNotFollwedAWHData[q1].txnKlNotFollowedBranchRefNoAHSpecificsId+'" label="'+data.lastForteenDaysTxnKlNotFollwedAWHData[q1].txnKlNotFollowedBranchRefNoAH+'">');
			var ahwTxnKlNotFollwedRefNumber=data.lastForteenDaysTxnKlNotFollwedAWHData[q1].txnKlNotFollowedBranchRefNo.split(',');
			for(var v=0;v<ahwTxnKlNotFollwedRefNumber.length;v++){
				$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('<option>'+ahwTxnKlNotFollwedRefNumber[v]+'</option>');
			}
			$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('</optgroup>');
		}
		$("select[class='txnExceedingBudgetKlNotFollowedDropdown']").append('</optgroup>');
		$("select[class='addedBranchesDropdown']").html('<option value="">New Branches</option>');
		for(var i=0;i<data.lastForteenDaysBranchData.length;i++){
			$("select[class='addedBranchesDropdown']").append('<option>'+data.lastForteenDaysBranchData[i].lastForteenDaysBranch+'</option>');
		}
	}
}

function displayDashboardValues(week, pWeek, id){
	var res=0.00,arrow='';
	if(!isEmpty(week) && !isEmpty(pWeek)){
		if((parseFloat(week)>parseFloat(pWeek)) || (parseFloat(week)<0 && parseFloat(pWeek)==0)){
			res=(week-pWeek)*100/week;
			arrow='<div class="variance-arrow arrow-green"></div>';
			$('#'+id).find('.d-variance .value').removeClass('red').addClass('green');
			$('#'+id).find('.d-variance').removeClass('d-variance-red').addClass('d-variance-green');
		}else if(parseFloat(week)<parseFloat(pWeek) && parseFloat(pWeek)!=0){
			res=(week-pWeek)*100/pWeek;
			arrow='<div class="variance-arrow arrow-red"></div>';
			$('#'+id).find('.d-variance .value').removeClass('green').addClass('red');
			$('#'+id).find('.d-variance').removeClass('d-variance-green').addClass('d-variance-red');
		}else{
			$('#'+id).find('.d-variance .value').removeClass('green').removeClass('red');
			$('#'+id).find('.d-variance').removeClass('d-variance-red').removeClass('d-variance-green');
		}
	}
	$('#'+id).find('.d-this-week .value').html(week);
	$('#'+id).find('.d-previous-week .value').html(pWeek);
	res=parseFloat(res.toString().replace('-',''));
	$('#'+id).find('.d-variance .indication').html(arrow);
	$('#'+id).find('.d-variance .value').html(res.toFixed(2).replace()+'%');
}

$(document).ready(function(){
	$('.opera-list').on('click',function(){
		var val=$(this).attr('data-id');
		if(!$(this).hasClass('selected')){
			$('.d-opera-reminder').slideUp('slow');
			if(2==val){
				$('#auditorOperationalTableDiv').slideDown('slow');
				getOperational($("#hiddenuseremail").text());
			}else{
				$('.operationalCalendar').slideDown('slow');
				var firstdate=$("tr[class='fc-week fc-first'] td:first").attr('data-date'),
				lastdate=$("tr[class='fc-week fc-last'] td:last").attr('data-date');
				getRealTimeAlertsInfo($("#hiddenuseremail").text(),firstdate,lastdate);
			}
			$('.opera-list').removeClass('selected');
			$(this).addClass('selected');
		}
	});

	$('.fc-text-arrow-next').on('click',function(){
			var firstdate=$("tr[class='fc-week fc-first'] td:first").attr('data-date'),
			lastdate=$("tr[class='fc-week fc-last'] td:last").attr('data-date');
			var firstDateNm, lastDateNm;
			var lastDateSplit = lastdate.split('-');
			var d1 = new Date(lastDateSplit[0],lastDateSplit[1]-1,lastDateSplit[2]);
			if(lastDateSplit[2]<7){
				d1.setDate(d1.getDate()-5);
				firstDateNm = (d1.toISOString()).substring(0,10);
				d1.setDate(d1.getDate()+41);
				lastDateNm=(d1.toISOString()).substring(0,10);
			}
			else if(lastDateSplit[2]>7){
				d1.setDate(d1.getDate()-12);
				firstDateNm = (d1.toISOString()).substring(0,10);
				d1.setDate(d1.getDate()+41);
				lastDateNm=(d1.toISOString()).substring(0,10);
			}
			//if(lastDateSplit[2]==7){
			else{
				d1.setDate(d1.getDate()-6);
				firstDateNm = (d1.toISOString()).substring(0,10);
				d1.setDate(d1.getDate()+42);
				lastDateNm=(d1.toISOString()).substring(0,10);
			}
			getRealTimeAlertsInfo($("#hiddenuseremail").text(),firstDateNm,lastDateNm);
		});
	
	$('.fc-text-arrow-prev').on('click',function(){
		var firstdate=$("tr[class='fc-week fc-first'] td:first").attr('data-date'),
		lastdate=$("tr[class='fc-week fc-last'] td:last").attr('data-date');
		var firstDateSplit = firstdate.split('-');
		var firstDateNm, lastDateNm;
		var d1 = new Date(firstDateSplit[0],firstDateSplit[1]-1,firstDateSplit[2]);
		
		if(firstDateSplit[2]==30||firstDateSplit[2]==31||firstDateSplit[2]==1){
			d1.setDate(d1.getDate()+7);
			lastDateNm=(d1.toISOString()).substring(0,10);
			d1.setDate(d1.getDate()-41);
			firstDateNm = (d1.toISOString()).substring(0,10);
		}
		else{
			d1.setDate(d1.getDate()+14);
			lastDateNm=(d1.toISOString()).substring(0,10);
			d1.setDate(d1.getDate()-42);
			firstDateNm = (d1.toISOString()).substring(0,10);
		}
		getRealTimeAlertsInfo($("#hiddenuseremail").text(),firstDateNm,lastDateNm);
	});
	$('.d-week').on('click',function(){
		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		jsonData.currDashboardFromDate = $("#currDashboardFromDate").val();
		jsonData.currDashboardToDate = $("#currDashboardToDate").val();;
		jsonData.prevDashboardFromDate = $("#prevDashboardFromDate").val();
		jsonData.prevDashboardToDate = $("#prevDashboardToDate").val();
		var dashboard=$(this).attr('data-dashboard'), graph=$(this).attr('data-graph'), val=$(this).find('.value').text();
		if(dashboard != 6 && dashboard !=7){
			console.log(val);
			if(!isEmpty(dashboard) && !isEmpty(graph) && (0!=val && '0.00%'!=val)){
				jsonData.dType = dashboard;
				jsonData.gType = graph;
				//var url='/dashboard/getGraph/'+dashboard+'/'+graph;
				ajaxCall('/dashboard/getGraph', jsonData, '', '', 'POST', '', 'plotGraphSuccess', '', true);
			}
		}else{
			if(graph == 1){
				showRecPayablesOpeningBalAndCurrentYearTotal(this);
			}else if(graph ==2){
				branchWiseReceivablePayablesGraph(this);
			}
			//branchWiseApproverCashBankReceivablePayables(this);
		}
	});
});

function plotGraphSuccess(data){
	(data.result)?graph.dashboard(data):swal("Error!",data.message,"error");
}

var graph={
	dashboard:function(data){
		$("div[class='modal-backdrop fade in']").remove();
		var res=data.data;
		if(!isEmpty(res) && res.length > 0){
			var values=[],ticks=[], ticksid=[], type=data.type,dashboardfor=data.dashboardfor,dashboardforbranch=data.dashboardforbranch;
			for(var i in res){
				if(!isEmpty(res[i])){
					values.push(res[i].amount);
					ticks.push(res[i].branch);
					ticksid.push(res[i].branchid);
				}
			}
			$('#dGraphPlot').unbind('jqplotDataClick').empty();
			$('#dGraphHeader').html(data.graphName);
			$("div[class='modal-backdrop fade in']").remove();
			$("#dGraphPlotDiv").attr('data-toggle', 'modal');
		    $("#dGraphPlotDiv").modal('show');
		    setTimeout(function(){
		    	var plot1 = $.jqplot('dGraphPlot', [values], {
		    		seriesColors: ['#f28671','#d993ad','#58b790','#e5a642','#7c85f4',"#958c12","#c5b47f","#EAA228","#579575", "#839557",
		    	                      "#958c12","#953579","#4b5de4","#d8b83f","#ff5800","#0085cc",'#85802b','#00749F','#73C774','#C7754C','#17BDB8'],
		            // Only animate if we're not using excanvas (not in IE 7 or IE 8)..
		            animate: !$.jqplot.use_excanvas,
		            seriesDefaults:{
		                renderer:$.jqplot.BarRenderer,
		                pointLabels: { show: true },
		                rendererOptions: {fillToZero: true, varyBarColor: true},
		            },
		            axesDefaults: {
		                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
		                tickOptions: {
		                  fontFamily: 'Helvetica',
		                  fontSize: '10pt',
		                  angle: -30,
		                  textColor :'#B4283C',
		                  formatString : "%#.2f"
		                }
		            },
		            axes: {
		                xaxis: {
		                    renderer: $.jqplot.CategoryAxisRenderer,
		                    ticks: ticks,
			                tickOptions: {
			                	showGridline: false
			                }
		                }
		            },
		            highlighter: { show: true }
		        });
				if(dashboardfor=="branch" || dashboardforbranch=="branch" || dashboardfor=="project"){
				    $('#dGraphPlot').bind('jqplotDataClick',function (ev, seriesIndex, pointIndex, data) {
			    		var additionalParameter="";
			    		if(dashboardfor=="branch" && dashboardforbranch=="branch"){
			    			additionalParameter="branch";//look for branch entity
			    		}
			    		if(dashboardfor=="project" && dashboardforbranch=="project"){
			    			additionalParameter="project";//look for project entity
			    		}
			    		if((dashboardfor=="project" && dashboardforbranch=="branch") || (dashboardfor=="branch" && dashboardforbranch=="project")){
			    			additionalParameter="projectbranch";//look for branch entity
			    		}
				    	var bnchName=ticks[pointIndex];
				    	var branchid = ticksid[pointIndex];
			    	    var amountValue=data.toString().split(",");
					    //$("#dGraphPlotDiv").modal('hide');
					    if(type == "thisWeekProformaInvoice" || type == "thisWeekquotationInvoice" || type == "previousWeekProformaInvoice" || type == "previousWeekquotationInvoice"){
					    	//displayCustWiseProformaInvoice(bnchName, amountValue, type, additionalParameter);
					    	displayQuotationProformaBranchBy(bnchName, branchid, amountValue, type, additionalParameter);
					    }else{
				    	//send individual data toserver get chart of account wise breakups and fill in the data modal
					    	dispalyAndPopulateModal(bnchName, branchid,amountValue,type,additionalParameter);
					    }
					});
				}
		    	$('#dGraphPlot').bind('resize', function(event, ui) {
		    		var w = parseInt($(".jqplot-yaxis").width(), 10) + parseInt($("#dGraphPlot").width(), 10);
					var h = parseInt($(".jqplot-title").height(), 10) + parseInt($(".jqplot-xaxis").height(), 10) + parseInt($("#dGraphPlot").height(), 10);
					$("#dGraphPlot").width(w).height(h);
					plot.replot( { resetAxes: true });
		        });
			    /*var w = parseInt($(".jqplot-yaxis").width(), 10) + parseInt($("#dGraphPlot").width(), 10);
				var h = parseInt($(".jqplot-title").height(), 10) + parseInt($(".jqplot-xaxis").height(), 10) + parseInt($("#dGraphPlot").height(), 10);
				$("#dGraphPlot").width(w).height(h);
				plot.replot( { resetAxes: true }); */

		    },2000);
		}else{
			swal('Error!','Not sufficient data to plot the graph.','error');
		}
	}
};


function showRecPayablesOpeningBalAndCurrentYearTotal(elem){
	var jsonData = {};
	var tabId = $(elem).closest('div').prop('id');
	var useremail=$("#hiddenuseremail").text();
	jsonData.tabElement = tabId;
	jsonData.usermail = useremail;
	jsonData.currDashboardToDate = $("#currDashboardToDate").val();
	var url="/dashboard/recPayablesOpeningBalAndCurrentYearTotal";
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
			$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
	    	$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
	    	$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
	    	$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
	    	$("#staticcashbankreceivablepayablebranchwisebreakup h4").text("Total");
	    	$("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").hide();
	    	if(data.recPayablesOpeningBalAndCurrentYearTotal.length>0){
	    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivablesBreakupTable" style="margin-top: 0px; width:450px;">'+
	    		'<thead class="tablehead1" style="position:relative"><th>Opening Balance</th><th>Current Year</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
	    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivablesBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="customerReceivablesOnDashboard" onclick="custVendOpeningBalanceBreakup(this, \''+ tabId +'\');">'+data.recPayablesOpeningBalAndCurrentYearTotal[0].openingBalance+'</a>	</td>'+
	    		'<td><div id='+tabId+'><a href="#pendingExpense" class="customerReceivablesOnDashboard" onclick="branchWiseReceivablePayablesGraph(this);">'+data.recPayablesOpeningBalAndCurrentYearTotal[0].netRecievablePayablesCurrentYear+'</a></div></td></tr>');
	    	}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});

}

function branchWiseReceivablePayablesGraph(elem){
	var jsonData = {};
	var tabId = $(elem).closest('div').prop('id');
	var useremail=$("#hiddenuseremail").text();
	jsonData.tabElement = tabId;
	jsonData.usermail = useremail;
	jsonData.currDashboardToDate = $("#currDashboardToDate").val();
	var url="/dashboard/branchWiseReceivablePayablesGraphData";
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
			$("#staticcashbankreceivablepayablebranchwisebreakup").modal('hide');
			$("div[class='modal-backdrop fade in']").remove();
			var res=data.branchWiseRecPayGraphData;
			if(!isEmpty(res) && res.length > 0){
				$('#dGraphPlot').unbind('jqplotDataClick').empty();
				$("#dGraphPlotDiv").attr('data-toggle', 'modal');
			    $("#dGraphPlotDiv").modal('show');
			    if(tabId == "accountsPayablesAllBranches"){
			    	$('#dGraphHeader').html("Account Payables");
			    }else if(tabId == "payableOverduesAllBranches"){
			    	$('#dGraphHeader').html("Payables Overdues");
			    }else if(tabId == "receivableOverduesAllBranches"){
			    	$('#dGraphHeader').html("Receivables Overdues");
			    }else if(tabId == "accountsReceivablesAllBranches"){
			    	//$('#dGraphHeader span').html("Account Receivables");
			    	$('#dGraphHeader').html("Account Receivables");
			    }
			    setTimeout(function(){

			    	 //var s1 = [0, 6, 7, 10];
			    	//  var s2 = [2000, 5, 3, 4];
			    	// var s3 = [14000, 9, 3, 8];
			    	 var s1=[],s2=[],s3=[],s4=[],s5=[],ticks=[],branchIds=[];
						for(var i in res){
							if(!isEmpty(res[i])){
								s1.push(res[i].amt0to30days);
								s2.push(res[i].amt31to60days);
								s3.push(res[i].amt61to90days);
								s4.push(res[i].amt91to180days);
								s5.push(res[i].amtOver180days);
								ticks.push(res[i].branchName);
								branchIds.push(res[i].branchId);
							}
						}
			    	  var plot1 = $.jqplot('dGraphPlot', [s1, s2, s3, s4, s5], {
			    	    // Tell the plot to stack the bars.
			    	    stackSeries: true,
			    	    captureRightClick: true,
			    	    seriesDefaults:{
			    	      renderer:$.jqplot.BarRenderer,
			    	      pointLabels: { show: true },
			    	      rendererOptions: {
			    	          // Put a 30 pixel margin between bars.
			    	          barMargin: 30,
			    	          // Highlight bars when mouse button pressed.
			    	          // Disables default highlighting on mouse over.
			    	          highlightMouseDown: true
			    	      }
			    	    },
			    	 // Custom labels for the series are specified with the "label"
			            // option on the series option.  Here a series option object
			            // is specified for each series.
			            series:[
			                {label:'0-30days'},
			                {label:'31-60days'},
			                {label:'61-90days'},
			                {label:'91-180days'},
			                {label:'over 180days'}
			            ],
			            axesDefaults: {
			                tickRenderer: $.jqplot.CanvasAxisTickRenderer ,
			                tickOptions: {
			                  fontFamily: 'Helvetica',
			                  fontSize: '10pt',
			                  angle: -30,
			                  textColor :'#B4283C'
			                }
			            },
			    	    axes: {
			    	      xaxis: {
			    	          renderer: $.jqplot.CategoryAxisRenderer,
			    	          ticks: ticks,
			    	          tickOptions: {
				                	showGridline: false
				                }
			    	      },
			    	      yaxis: {
			    	        // Don't pad out the bottom of the data range.  By default,
			    	        // axes scaled as if data extended 10% above and below the
			    	        // actual range to prevent data points right on grid boundaries.
			    	        // Don't want to do that here.
			    	        padMin: 0
			    	      }
			    	    },
			    	    legend: {
			    	      show: true,
			    	      location: 'e',
			    	      placement: 'outside'
			    	    }
			    	  });

			    	  $('#dGraphPlot').bind('jqplotDataClick',function (ev, seriesIndex, pointIndex, data) {
					    	var bnchName=ticks[pointIndex];
					    	var bnchId = branchIds[pointIndex];
				    	    var amountValue=data.toString().split(",");
						    $("#dGraphPlotDiv").modal('hide');
						    var series='under0to30';
						    var label='Under0to30 days'
						    if(seriesIndex==1){
						    	series= 'under31to60';
						    	label='Under31to60 days';
						    }else if(seriesIndex==2){
						    	series= 'under61to90';
						    	label='Under61to90 days';
						    }else if(seriesIndex==3){
						    	series= 'under91to180';
						    	label='Under91to180 days';
						    }else if(seriesIndex==4){
						    	series= 'over';
						    	label='Over180 days';
						    }
						    displayBranchAndPeriodWiseCustVend(tabId, bnchId, series);
						});

				    	$('#dGraphPlot').bind('resize', function(event, ui) {
				    		var w = parseInt($(".jqplot-yaxis").width(), 10) + parseInt($("#dGraphPlot").width(), 10);
							var h = parseInt($(".jqplot-title").height(), 10) + parseInt($(".jqplot-xaxis").height(), 10) + parseInt($("#dGraphPlot").height(), 10);
							$("#dGraphPlot").width(w).height(h);
							plot1.replot( { resetAxes: true });
							$("#dGraphPlot").width(w).height(h);
							$("#dGraphPlot").parent().parent().parent().width(800).height(h);
				    		//console.log(plot1);
				            //plot1.replot( { resetAxes: true } );
				        });
			    },2000);
			}else{
				swal('Error!','Not sufficient data to plot the graph.','error');
			}

		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}
function verifyDashboardDateRangeCorrect(){
	var currDashboardFromDate = $("#currDashboardFromDate").val();
	if(currDashboardFromDate == ""){
		swal("Current period reporting 'From Date' cannot be empty.", "Error", "error");
		//swal({ html:true, title:'<i>Error</i>', text:'Current period reporting <b>from date</b> cannot be empty.'});
		return false;
	}
	var currDashboardToDate = $("#currDashboardToDate").val();
	if(currDashboardToDate == ""){
		swal("Current period reporting 'To date' cannot be empty.", "Error", "error");
		return false;
	}
	if(new Date(currDashboardFromDate).getTime() > new Date(currDashboardToDate).getTime()){
		swal("Current period reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
		return false;
	}
	var prevDashboardFromDate = $("#prevDashboardFromDate").val();
	var prevDashboardToDate = $("#prevDashboardToDate").val();
	if(prevDashboardFromDate != "" && prevDashboardToDate == ""){
		swal("Previous reporting 'To Date' cannot be empty.", "Error", "error");
		return false;
	}
	if(new Date(prevDashboardFromDate).getTime() > new Date(prevDashboardToDate).getTime()){
		swal("Previous reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
		return false;
	}
}

$(document).ready(function(){
	$('#generateDashboardbtn').click(function(){
		verifyDashboardDateRangeCorrect();
		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		jsonData.currDashboardFromDate = $("#currDashboardFromDate").val();
		jsonData.currDashboardToDate = $("#currDashboardToDate").val();;
		jsonData.prevDashboardFromDate = $("#prevDashboardFromDate").val();
		jsonData.prevDashboardToDate = $("#prevDashboardToDate").val();
		var tab=$('.dashboard-title').attr('data-tab');
		if('dashboardProject'==tab){
			ajaxCall('/dashboard/getProjectFinancials', jsonData, '', '', 'POST', '', 'getProjectFinancialsSuccess', '', false);
		}else{
			ajaxCall('/dashboard/getFinancials', jsonData, '', '', 'POST', '', 'getDashboardFinancialsSuccess', '', true);
		}
	});
});
function branchWiseApproverCashBankReceivablePayables(elem){
    	var jsonData = {};
    	//var tabId = $(this).closest('div').prop('id');
    	var tabId = $(elem).closest('div').prop('id');
    	//var tabId="accountsReceivablesAllBranches";
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
    			$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		    	$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		    	$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		    	$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		    	$("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").hide();
		    	$("#staticcashbankreceivablepayablebranchwisebreakup h4").text("Branch Wise-Breakups");
		    	if(data.branchWiseCashBankRecivablesPayablesData.length>0){
		    		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivablesBreakupTable" style="margin-top: 0px; width:450px;">'+
		    		'<thead class="tablehead1" style="position:relative"><th>Branch Name</th><th>Amount Receivables</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			    	for(var i=0;i<data.branchWiseCashBankRecivablesPayablesData.length;i++){
			    		var branchAmountArr="";var classModelFor="";
			    		if(tabId=="accountsReceivablesAllBranches"){
			    			branchAmountArr=data.branchWiseCashBankRecivablesPayablesData[i].accountsReceivables.split(":");
			    			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivablesBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="customerReceivablesOnDashboard" onclick="displayCustVendOnDashboard(this, \''+ branchAmountArr[2] +'\');">'+branchAmountArr[0]+'</a></td><td>'+branchAmountArr[1]+'</td><tr>');
			    		}else if(tabId=="accountsPayablesAllBranches"){
			    			branchAmountArr=data.branchWiseCashBankRecivablesPayablesData[i].accountsPayables.split(":");
			    			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivablesBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="vendorPayablesOnDashboard" onclick="displayCustVendOnDashboard(this, \''+ branchAmountArr[2] +'\');">'+branchAmountArr[0]+'</a></td><td>'+branchAmountArr[1]+'</td><tr>');
			    		}else if(tabId=="receivableOverduesAllBranches"){
			    			branchAmountArr=data.branchWiseCashBankRecivablesPayablesData[i].accountsReceivablesOverdues.split(":");
			    			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivablesBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="customerReceivablesOnDashboard" onclick="displayCustVendOnOverduesDashboard(this, \''+ branchAmountArr[2] +'\');">'+branchAmountArr[0]+'</a></td><td>'+branchAmountArr[1]+'</td><tr>');
			    		}else if(tabId=="payableOverduesAllBranches"){
			    			branchAmountArr=data.branchWiseCashBankRecivablesPayablesData[i].accountsPayablesOverdues.split(":");
			    			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivablesBreakupTable'] tbody").append('<tr><td><a href="#pendingExpense" class="vendorPayablesOnDashboard" onclick="displayCustVendOnOverduesDashboard(this, \''+ branchAmountArr[2] +'\');">'+branchAmountArr[0]+'</a></td><td>'+branchAmountArr[1]+'</td><tr>');
			    		}

			    	}
		    	}
    		},
    		error: function (xhr, status, error) {
    			if(xhr.status == 401){ doLogout(); }
    		}
    	});
}



function displayBranchAndPeriodWiseCustVend(tabId, branchId, series){
	var jsonData = {};
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.tabId=tabId;
	jsonData.branchId = branchId;
	jsonData.series=series;
	jsonData.currDashboardToDate = $("#currDashboardToDate").val();
	ajaxCall('/dashboard/displayBarnchAndPeriodWiseCustVend', jsonData, '', '', '', '', 'displayBarnchAndPeriodWiseCustVendSuccess', '', true);
}


function displayBarnchAndPeriodWiseCustVendSuccess(data){
	//location.hash="#pendingExpense";
	$("div[class='modal-backdrop fade in']").remove();
	if(data.result){
		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		$("#staticcashbankreceivablepayablebranchwisebreakup h4").text("Customer/Vendorwise Breakups");
		if(data.branchAndPeriodWiseCustBreakup.length>0){
			if(data.branchAndPeriodInfo[0].txnModelFor == "vendorPayables"){
				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable" style="margin-top: 0px; width:450px;">'+
	    		'<thead class="tablehead1" style="position:relative"><th>Vendor Name</th><th>Amount</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			}else{
				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable" style="margin-top: 0px; width:450px;">'+
	    		'<thead class="tablehead1" style="position:relative"><th>Customer Name</th><th>Amount</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			}
			$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('Branch: '+data.branchAndPeriodInfo[0].branchName+ '     Period: '+ data.branchAndPeriodInfo[0].periodLabel);
			for(var i=0;i<data.branchAndPeriodWiseCustBreakup.length;i++){
				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivableCustomerVendorBreakupTable'] tbody").append('<tr><td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchAndPeriodWiseCustBreakup[i].customerId+'" name="'+data.branchAndPeriodInfo[0].txnModelFor+'" title="'+data.branchAndPeriodInfo[0].period+'" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchAndPeriodInfo[0].branchId+'\');">'+data.branchAndPeriodWiseCustBreakup[i].customerName+'</a></div></td>'+
				'<td><div style="width:80px;">'+data.branchAndPeriodWiseCustBreakup[i].amount+'</div></td><tr>');

			}
		}
	}
}




$(document).ready(function(){
	$('#aggregateDataCompareInput,#aggregateDataCompareFirstLocation,#aggregateDataCompareSecondLocation').on('blur',function(){
	  $('#dashboardAutoSuggestId,#dashboardLocationFirstAutoSuggest,#dashboardLocationSecondAutoSuggest').empty();
	});
});

/*New Dashboard Ends*/

/*Dashboard Project Starts*/
$(document).ready(function(){
	$('body').on('click','.dashboard-title',function(){
		var tabClass = $(this).parent().attr("class");
		if(tabClass == "active"){
			return false;
		}
		var tab=$(this).attr('data-tab');
		$('.dash-tab').hide();
		if('dashboardProject'==tab){	//If project dashboard visible then hide it and show branch dashboard
			$('#dashboardBranch').fadeIn('normal',function(){
				//$('.dashboard-title').html('Financials-Branch').attr('data-tab','dashboardBranch');
				$('.dashboard-title').attr('data-tab','dashboardBranch');
			});

			$("#financialProject").removeAttr("class", "active");
			$("#financialBranch").attr("class", "active");
		}else {
			$("#financialProject").attr("class", "active");
			$("#financialBranch").removeAttr("class", "active");
			$('#dashboardProject').fadeIn('normal',function(){
				//$('.dashboard-title').html('Financials-Project').attr('data-tab','dashboardProject');
				$('.dashboard-title').attr('data-tab','dashboardProject');
				var jsonData = {};
				jsonData.userEmail = $("#hiddenuseremail").text();
				jsonData.currDashboardFromDate = $("#currDashboardFromDate").val();
				jsonData.currDashboardToDate = $("#currDashboardToDate").val();;
				jsonData.prevDashboardFromDate = $("#prevDashboardFromDate").val();
				jsonData.prevDashboardToDate = $("#prevDashboardToDate").val();
				jsonData.email =$("#hiddenuseremail").text();
				ajaxCall('/dashboard/getProjectFinancials', jsonData, '', '', 'POST', '', 'getProjectFinancialsSuccess', '', false);
			});
		}
	});

	$('.d-prj-week').on('click',function(){
		var dashboard=$(this).attr('data-dashboard'),graph=$(this).attr('data-graph'),val=$(this).find('.value').text();
		console.log(dashboard);
		console.log(graph);
		console.log(val);
		if(!isEmpty(dashboard) && !isEmpty(graph) && (0!=val && '0.00%'!=val)){
			var jsonData = {};
			jsonData.dType = dashboard;
			jsonData.gType = graph;
			jsonData.userEmail = $("#hiddenuseremail").text();
			jsonData.currDashboardFromDate = $("#currDashboardFromDate").val();
			jsonData.currDashboardToDate = $("#currDashboardToDate").val();;
			jsonData.prevDashboardFromDate = $("#prevDashboardFromDate").val();
			jsonData.prevDashboardToDate = $("#prevDashboardToDate").val();
			//var url='/dashboard/getProjectGraph/'+dashboard+'/'+graph;
			ajaxCall('/dashboard/getProjectGraph', jsonData, '', '', 'POST', '', 'plotGraphSuccess', '', false);
		}
	});
});

function getProjectFinancialsSuccess(data){
	if(!isEmpty(data)){
		var res=data.projectDashBoardData;
		res=res[0];
		var projectCashExpense=(''==res.projectCashExpense)?0.00:res.projectCashExpense,
		pProjectCashExpense=(''==res.previousWeekProjectCashExpense)?0.00:res.previousWeekProjectCashExpense,
		projectCashIncome=(''==res.projectCashIncome)?0.00:res.projectCashIncome,
		pProjectCashIncome=(''==res.previousWeekProjectCashIncome)?0.00:res.previousWeekProjectCashIncome,
		projectCreditExpense=(''==res.projectCreditExpense)?0.00:res.projectCreditExpense,
		pProjectCreditExpense=(''==res.previousWeekProjectCreditExpense)?0.00:res.previousWeekProjectCreditExpense,
		projectCreditIncome=(''==res.projectCreditIncome)?0.00:res.projectCreditIncome,
		pProjectCreditIncome=(''==res.previousWeekProjectCreditIncome)?0.00:res.previousWeekProjectCreditIncome,
		projectBudgetAvail=(''==res.projectExpBudgetAvail)?0.00:res.projectExpBudgetAvail,
		pProjectBudgetAvail=(''==res.previousWeekProjectExpBudgetAvail)?0.00:res.previousWeekProjectExpBudgetAvail,
		projectPay=(''==res.projectNetPayableThisWeek)?0.00:res.projectNetPayableThisWeek,
		pProjectPay=(''==res.netProjectPayablePreviousWeek)?0.00:res.netProjectPayablePreviousWeek,
		projectRecieve=(''==res.projectNetRecievableThisWeek)?0.00:res.projectNetRecievableThisWeek,
		pProjectRecieve=(''==res.netProjectRecievablePreviousWeek)?0.00:res.netProjectRecievablePreviousWeek;
		displayDashboardValues(projectCashExpense,pProjectCashExpense,'dProjectCashExpense');
		displayDashboardValues(projectCashIncome,pProjectCashIncome,'dProjectCashIncome');
		displayDashboardValues(projectCreditExpense,pProjectCreditExpense,'dProjectCreditExpense');
		displayDashboardValues(projectCreditIncome,pProjectCreditIncome,'dProjectCreditIncome');
		displayDashboardValues(projectBudgetAvail,pProjectBudgetAvail,'dProjectExpenseBudget');
		displayDashboardValues(projectPay,pProjectPay,'dProjectTotalPay');
		displayDashboardValues(projectRecieve,pProjectRecieve,'dProjectTotalRecieve');
		$('#dOtherInfo #projectWithHighestExpense').html(res.maxExpenseProject).attr('title',res.maxExpenseProject);
		$('#dOtherInfo #projectWithHighestIncome').html(res.maxIncomeProject).attr('title',res.maxIncomeProject);
	}
}
/*Dashboard Project Ends*/



function showAdvanceModal(id){
	 var jsonData = {};
	 var useremail=$("#hiddenuseremail").text();
	 jsonData.usermail = useremail;
	 jsonData.clickSourceString=id;
	 var url="/dashboard/customerVendorAdvanceBI";
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
			$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
			if(data.dashboardCustomerVendorAdvanceData.length>0){
				$("#" + id).attr('data-toggle', 'modal');
			    $("#" + id).modal('show');
			    $("#" + id + " a[id='fancybox-close']").attr("href",location.hash);
				$("#customerAdvancePending table[id='advancePendingTable'] tbody").html("");
				$("#vendorAdvancePending table[id='advancePendingTable'] tbody").html("");
				$("#dashboardAutoSuggestId").hide();
				$("#dashboardLocationFirstAutoSuggest").hide();
				$("#dashboardLocationSecondAutoSuggest").hide();
				for(var i=0;i<data.dashboardCustomerVendorAdvanceData.length;i++){
					$("#" + id+" table[id='advancePendingTable'] tbody").append('<tr><td><b>'+data.dashboardCustomerVendorAdvanceData[i].name+'</b></td><td><b>'+data.dashboardCustomerVendorAdvanceData[i].totalAdvanceCollected+'</b></td><td><b>'+data.dashboardCustomerVendorAdvanceData[i].totalAdvanceAdjusted+'</b></td><td><b class="pendingAmount">'+data.dashboardCustomerVendorAdvanceData[i].totalAdvancePending+'</b><span style="float:right;" id="'+data.dashboardCustomerVendorAdvanceData[i].id+'" name="'+data.dashboardCustomerVendorAdvanceData[i].totalAdvancePending+'" class="customervendor-advance-breakups income-expense-last-3" onclick="showAdvanceTransactionModal(this);"></span></td></tr>');
				}
			}else{
				swal("Error!","Data Not Available","error");
			}
			$.unblockUI();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function showModal(id) {
	if ('' !== id || null !== id || undefined !== id) {
		var showModal="";
		if(id=="expenseLast3"){
			var text=$("#bnchWithHighestExpense").text();
			if(text!=""){
				showModal="true";
			}
		}
		if(id=="incomeLast3"){
			var text=$("#bnchWithHighestIncome").text();
			if(text!=""){
				showModal="true";
			}
		}
		if(id=="projectexpenseLast3"){
			var text=$("#projectWithHighestExpense").text();
			if(text!=""){
				showModal="true";
			}
		}
		if(id=="projectincomeLast3"){
			var text=$("#projectWithHighestIncome").text();
			if(text!=""){
				showModal="true";
			}
		}
		if(showModal!=""){
			var jsonData = {};
			 var useremail=$("#hiddenuseremail").text();
			 jsonData.usermail = useremail;
			 jsonData.clickSourceString=id;
			 var url="/dashboard/highestExpenseIncomeBI";
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
					$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
					$("#" + id).attr('data-toggle', 'modal');
				    $("#" + id).modal('show');
				   // $("#" + id + " a[id='fancybox-close']").attr("href",location.hash);
				    $("#dashboardAutoSuggestId").hide();
					$("#dashboardLocationFirstAutoSuggest").hide();
					$("#dashboardLocationSecondAutoSuggest").hide();
					if(data.dashboardHighestExpenseIncomeData.length>0){
						var s1=[];
						$("#incomeLast3Table tbody").html("");
						$("#expenseLast3Table tbody").html("");
						$("#highestIncomeExpensePieChart").remove();
						$("#highestBranchName").text("");
						for(var i=0;i<data.dashboardHighestExpenseIncomeData.length;i++){
							$("#" + id+" table[id='expenseLast3Table'] tbody").append('<tr><td>'+data.dashboardHighestExpenseIncomeData[i].itemName+'</td><td>'+data.dashboardHighestExpenseIncomeData[i].itemAmount+'</td></tr>');
							$("#" + id+" table[id='incomeLast3Table'] tbody").append('<tr><td>'+data.dashboardHighestExpenseIncomeData[i].itemName+'</td><td>'+data.dashboardHighestExpenseIncomeData[i].itemAmount+'</td></tr>');
							$("#" + id+" span[id='highestBranchName']").text(data.dashboardHighestExpenseIncomeData[i].branchName);
							var addeds1=[''+data.dashboardHighestExpenseIncomeData[i].itemName+'',parseFloat(data.dashboardHighestExpenseIncomeData[i].itemAmount)];
							s1.push(addeds1);
						}
						$("#" + id+" div[class='modal-body']").append('<div id="highestIncomeExpensePieChart"></div>');
				    	setTimeout(function(){
					    	var plot8 = $.jqplot('highestIncomeExpensePieChart', [s1], {
					            grid: {
					                drawBorder: false,
					                drawGridlines: false,
					                background: '#ffffff',
					                shadow: false
					            },
					            axesDefaults: {
					            },
					            seriesDefaults: {
					                renderer: $.jqplot.PieRenderer,
					                rendererOptions: { showDataLabels: true }
					            },
					            legend: {
					                show: true,
					               // rendererOptions: { numberRows: 1 },
					                location: 'e'
					            }
					        });
				    	},2000);
					}else{
						swal("Error!","Data Not Available","error");
					}
			    	$.unblockUI();
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			 });
		}else{
			swal("Error!","Data Not Available","error");
			return true;
		}
	}
}

function viewTransactionDetails(elem){
	var elemId=$(elem).attr('id');
	var n = elemId.indexOf("TXN"); //for receivable/payables/overdue drilldown elemId will be tranRefNo
	var selectedOptionText="";
	if(n == 0){
		selectedOptionText = elemId;
	}else{
		selectedOptionText=$(elem).find('option:selected').text(); //for transaction exceeding budget etc it will be from option
	}
	if(selectedOptionText!="Transaction Pending Approval"){
		var jsonData = {};
		var useremail=$("#hiddenuseremail").text();
		jsonData.usermail = useremail;
		jsonData.selecteTxnRefNumber=selectedOptionText;
		var url="/dashboard/showPendingTxnDetails";
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
				data = data.individualTxnData[0];
				if (undefined !== data || null !== data || '' !== data) {
					$(elem).find('option:first').prop("selected","selected");
				    $("#dashboardAutoSuggestId").hide();
					$("#dashboardLocationFirstAutoSuggest").hide();
					$("#dashboardLocationSecondAutoSuggest").hide();
					$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
					var image = '', status = '', selectDocument = '<option value="">--Please Select--</option>';
					var netAmount="";
					if(data.txnSpecialStatus=="Transaction Exceeding Budget & Rules Not Followed"){
				    	image = '<img src="assets/images/blue.png"></img>';
				    } else if(data.txnSpecialStatus=="Transaction Exceeding Budget"){
				    	image = '<img src="assets/images/red.png"></img>';
				    } else if(data.txnSpecialStatus=="Rules Not Followed"){
				    	image = '<img src="assets/images/green.png"></img>';
				    }
				    if(data.status!="" && data.status=="Rejected"){
				    	status = '<div class="txnstatred">' + data.status + '</div>';
				    } else if(data.status!="" && data.status=="Accounted"){
				    	status = '<div class="txnstat">' + data.status + '</div>';
				    } else if(data.status!="" && (data.status=="Approved" || data.status=="Require Approval" || data.status=="Require Additional Approval")){
				    	status = '<div class="txnstatgreen">' + data.status + '</div>';
				    }
				    if ('' !== data.txnDocument && undefined !== data.txnDocument && null !== data.txnDocument) {
				    	var txndocument=data.txnDocument;
				    	var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
				    	for(var j=0;j<fileURLWithUser.length;j++){
				    		var fileURLWithoutUser=fileURLWithUser[j].substring(0, fileURLWithUser[j].length).split('#');
				    		var inkblob = { url: fileURLWithoutUser[1]};
				    		var fileName=getFileStat(fileURLWithoutUser[0],fileURLWithoutUser[1],data.id);
				    		selectDocument += '<option value="' + fileURLWithoutUser + '">' + fileName + '</option>';
				    	}
				    }
				    if(data.netAmtDesc!=null && data.netAmtDesc!=""){
				          var individualNetDesc=data.netAmtDesc.substring(0,data.netAmtDesc.length).split(',');
				          for(var m=0;m<individualNetDesc.length;m++){
				           var labelAndFigure=individualNetDesc[m].substring(0, individualNetDesc[m].length).split(':');
				           netAmount += '<b>'+labelAndFigure[0]+'</b><br/>';
				           if(typeof labelAndFigure[1]!='undefined'){
				            netAmount += '<p style="color: blue;">'+labelAndFigure[1]+'</p><br/>';
				           }
				          }
				    }
					var writeToTable = '<td><b>BRANCH:</b><br/><span>' + data.branchName + '</span><br/><b>PROJECT:</b><br/><span>' + data.projectName+ '</span><br/><b>CREATOR:</b><br/><span>' + data.createdBy + '</span></td>'
					+ '<td><b>ITEM:</b><br/><span>' + data.itemName + '</span><b>IMMEDIATE PARENT:</b><span>' + data.itemParentName + '</span></td>'
					+ '<td><b>NAME:</b><br/><span>' + data.customerVendorName + '</span><br/><b>TRANSACTION PURPOSE:</b><br/><span>' + data.transactionPurpose + '</span></td>'
					+ '<td><b>CREATED DATE:</b><br/><span>' + data.txnDate + '</span></td><td>' + data.paymentMode + '</span></td>'
					+ '<td><b>UNITS:</b><br/><span>' + data.noOfUnit + '</span><br/><b>PRICE/UNIT:</b><br/><span>' + data.unitPrice + '</span><br/><b>GROSS:</b><br/><span>' + data.grossAmount + '</span></td>'
					+ '<td><b>NET AMOUNT:</b><br/><span>' + data.netAmount + '</span><br/><b>CALCULATION/DESCRIPTION:</b><br/>' + netAmount + '</span></td>'
					+ '<td>' + status + '<br/><div class="approvedBy" style="display:none">ApprovedBy:<br/><span>'+data.approverEmail+'</span></div><br/><div class="budgetAvailableDuringTransaction" style="display:none">Budget Available:<br/><span>'+data.budgetAvailableAmt+'</span></div><br/><div class="budgetExceededByWhenTransactionApproved" style="display:none">Transaction Exceeded By:<br/><span>'+data.budgetExceededBy+'</span></div></td><td><select id="transPenApproveDashboard">' + selectDocument +'</select></td><td><span>' + data.txnRemarks + '</span></td>';
					$('#transPenApproveDashboardTable').find('#transPenApproveDashboardRow').html(writeToTable);
					$('#transPenApproveDashboardTable').find('#transPenApproveDashboardRow span').css('color', 'blue');
					$("#transPenApproveDashboard").attr('data-toggle', 'modal');
				    $("#transPenApproveDashboard").modal('show');
				    if(elemId=="txnExceedingBudgetKlNotFollowedDropdown"){
				    	$('#transPenApproveDashboardTable').find('#transPenApproveDashboardRow div[class="approvedBy"]').show();
				    	$('#transPenApproveDashboardTable').find('#transPenApproveDashboardRow div[class="budgetAvailableDuringTransaction"]').show();
				    	$('#transPenApproveDashboardTable').find('#transPenApproveDashboardRow div[class="budgetExceededByWhenTransactionApproved"]').show();
				    }
				    $("#transPenApproveDashboard span[class='pendingTxnRefNumber']").text("");
				    $("#transPenApproveDashboard span[class='pendingTxnRefNumber']").text(selectedOptionText);
				    $("#transPenApproveDashboard a[id='fancybox-close']").attr("href",location.hash);
			    	$.unblockUI();
				} else {
					idosalert.show('Data unavailable at this moment. Please try again later.');
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	}
}


/*Transaction Exceeding Budget Group Details Starts*/
$(document).ready(function() {
	$('body').on('change', '#txnExceedingBudgetKlNotFollowedGroupDropdown', function() {
		var specId = this.value;
		if (!isEmpty(specId)) {
			var useremail = $("#hiddenuseremail").text();
			if (!isEmpty(useremail) && !isEmpty(specId)) {
				var jsonData = {};
				jsonData.email = useremail;
				jsonData.specId = specId;
				ajaxCall('/dashboard/getExceedingBudgetDetails', jsonData, '', '', '', '', 'transExceedDetailsSuccess', '', true)
			}
		}
	});
});

function transExceedDetailsSuccess(response) {
	if (response.result) {
		response = response.resultArray;
		if (response.length > 0) {
			var data = '';
			var selectedOptionText=$('#txnExceedingBudgetKlNotFollowedGroupDropdown').find('option:selected').text();
			var writeToTable = '';
			for (var i = 0; i < response.length; i++) {
				data = response[i];
				if (!isEmpty(data)) {
					$('#txnExceedingBudgetKlNotFollowedGroupDropdown').find('option:first').prop("selected","selected");
				    $("#dashboardAutoSuggestId").hide();
					$("#dashboardLocationFirstAutoSuggest").hide();
					$("#dashboardLocationSecondAutoSuggest").hide();
					var image = '', status = '', selectDocument = '<option value="">--Please Select--</option>';
					var netAmount="";
					if(data.txnSpecialStatus=="Transaction Exceeding Budget & Rules Not Followed"){
				    	image = '<img src="assets/images/blue.png"></img>';
				    } else if(data.txnSpecialStatus=="Transaction Exceeding Budget"){
				    	image = '<img src="assets/images/red.png"></img>';
				    } else if(data.txnSpecialStatus=="Rules Not Followed"){
				    	image = '<img src="assets/images/green.png"></img>';
				    }
				    if(data.status!="" && data.status=="Rejected"){
				    	status = '<div class="txnstatred">' + data.status + '</div>';
				    } else if(data.status!="" && data.status=="Accounted"){
				    	status = '<div class="txnstat">' + data.status + '</div>';
				    } else if(data.status!="" && (data.status=="Approved" || data.status=="Require Approval" || data.status=="Require Additional Approval")){
				    	status = '<div class="txnstatgreen">' + data.status + '</div>';
				    }
				    if ('' !== data.txnDocument && undefined !== data.txnDocument && null !== data.txnDocument) {
				    	var txndocument=data.txnDocument;
				    	var fileURLWithUser=txndocument.substring(0,txndocument.length).split(',');
				    	for(var j=0;j<fileURLWithUser.length;j++){
				    		var fileURLWithoutUser=fileURLWithUser[j].substring(0, fileURLWithUser[j].length).split('#');
				    		var inkblob = { url: fileURLWithoutUser[1]};
				    		var fileName=getFileStat(fileURLWithoutUser[0],fileURLWithoutUser[1],data.userTxnData[i].id);
				    		selectDocument += '<option value="' + fileURLWithoutUser + '">' + fileName + '</option>';
				    	}
				    }
				    if(data.netAmtDesc!=null && data.netAmtDesc!=""){
				          var individualNetDesc=data.netAmtDesc.substring(0,data.netAmtDesc.length).split(',');
				          for(var m=0;m<individualNetDesc.length;m++){
				           var labelAndFigure=individualNetDesc[m].substring(0, individualNetDesc[m].length).split(':');
				           netAmount += '<b>'+labelAndFigure[0]+'</b><br/>';
				           if(typeof labelAndFigure[1]!='undefined'){
				            netAmount += '<p style="color: blue;">'+labelAndFigure[1]+'</p><br/>';
				           }
				          }
				    }
				    writeToTable += '<tr><td><b>BRANCH:</b><br/><span>' + data.branchName + '</span><br/><b>PROJECT:</b><br/><span>' + data.projectName+ '</span><br/><b>CREATOR:</b><br/><span>' + data.createdBy + '</span></td>'
					+ '<td>' + image + '<br/><b>ITEM:</b><br/><span>' + data.itemName + '</span><br/><b>IMMEDIATE PARENT:</b><br/><span>' + data.itemParentName + '</span></td>'
					+ '<td><b>NAME:</b><br/><span>' + data.customerVendorName + '</span><br/><b>TRANSACTION PURPOSE:</b><br/><span>' + data.transactionPurpose + '</span></td>'
					+ '<td><b>CREATED DATE:</b><br/><span>' + data.txnDate + '</span></td><td>' + data.paymentMode + '</span></td>'
					+ '<td><b>UNITS:</b><br/><span>' + data.noOfUnit + '</span><br/><b>PRICE/UNIT:</b><br/><span>' + data.unitPrice + '</span><br/><b>GROSS:</b><br/><span>' + data.grossAmount + '</span></td>'
					+ '<td><b>NET AMOUNT:</b><br/><span>' + data.netAmount + '</span><br/><b>CALCULATION/DESCRIPTION:</b><br/>' + netAmount + '</span></td>'
					+ '<td>' + status + '<br/><div class="approvedBy">ApprovedBy:<br/><span>'+data.approverEmail+'</span></div><br/><div class="budgetAvailableDuringTransaction">Budget Available:<br/><span>'+data.budgetAvailableAmt+'</span></div><br/><div class="budgetExceededByWhenTransactionApproved">Transaction Exceeded By:<br/><span>'+data.budgetExceededBy+'</span></div></td><td><select id="transPenApproveDashboard">' + selectDocument +'</select></td><td><span>' + data.txnRemarks + '</span></td></tr>';
				}
			}
			$('#transExceedGroupDashboard tbody').html(writeToTable);
			$('#transExceedGroupDashboard tr span').css('color', 'blue');
			$("#transExceedGroupDashboard span[class='pendingTxnRefNumber']").text("");
		    $("#transExceedGroupDashboard span[class='pendingTxnRefNumber']").text(selectedOptionText);
		    $("#transExceedGroupDashboard a[id='fancybox-close']").attr("href",location.hash);
		    if (response.length > 1) {
		    	$('#transExceedGroupDashboard').css('top', '7%');
		    } else {
		    	$('#transExceedGroupDashboard').css('top', '15%');
		    }
			$("#transExceedGroupDashboard").attr('data-toggle', 'modal');
		    $("#transExceedGroupDashboard").modal('show');
		}
	}
}
/*Transaction Exceeding Budget Group Details Ends*/




function autoSuggestDashboardAvailableItems(elem){

	//alert("autoSuggestDashboardAvailableItems");

	var jsonData = {};
	//var parentTr=$(elem).parent().parent().attr('class');
	//alert(parentTr);

	//$("."+parentTr+" div[class='dashboardAutoSuggest']").html('');

	$("#dashboardAutoSuggestId").html('<div id="childtext" onClick="fillDashboardText(1);" class="dashboardchildtext">helllo</div>');

	//$("#aggregateDataCompareInput").

	var value=$(elem).val();
	if(value!=""){
		var fillElem=$(elem);
		jsonData.enteredValue = value;
		var url="/ecommerce/populateAllPossibleItems";
		$.ajax({
			url: url,
			data: JSON.stringify(jsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data) {
				$('#dashboardLocationFirstAutoSuggest,#dashboardLocationSecondAutoSuggest').empty();
				for(var i=0;i<data.ecommerceAvailableItemData.length;i++){
					$("#dashboardAutoSuggestId").append('<div id="childtext'+i+'" onClick="fillDashboardText('+i+');" class="dashboardchildtext">'+data.ecommerceAvailableItemData[i].itemName+'</div>');
				}
			},
			error: function (jqXHR, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	}
}

$('#dashboardAutoSuggestId').delegate('div', 'click', function (evnt) {
		evnt.stopPropagation();
});

function fillDashboardText(datas){
	var textvalue=$("#childtext"+datas+"").text();
	swal(textvalue);
	$("input[name='aggregateDataCompareInput']").val(textvalue);
	$("div[class='dashboardAutoSuggest']").html('');
}

function dispalyAndPopulateModal(bnchName, branchId, amountValue, requirements, additionalParameter){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.branchName=bnchName;
	jsonData.branchId = branchId;
	jsonData.amtValue=amountValue;
	jsonData.displayReq=requirements;
	jsonData.addOnParameter=additionalParameter;
	jsonData.currDashboardFromDate = $("#currDashboardFromDate").val();
	jsonData.currDashboardToDate = $("#currDashboardToDate").val();
	jsonData.prevDashboardFromDate = $("#prevDashboardFromDate").val();
	jsonData.prevDashboardToDate = $("#prevDashboardToDate").val();
	var url="/dashboard/chartOfAccountBreakUps";
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
    	  $("#staticdashboardindividualbreakup").attr('data-toggle', 'modal');
    	  $("#staticdashboardindividualbreakup").modal('show');
    	  $(".staticdashboardindividualbreakupclose").attr("href",location.hash);
    	  $("#staticdashboardindividualbreakup div[class='modal-body']").html("");
    	  if(data.coaBreakUpsData.length>0){
    		  $("#staticdashboardindividualbreakup div[class='modal-body']").html('<div class="datascrolltable" style=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-create" id="coaBreakupTable" style="margin-top: 0px;">'+
    		  '<thead class="tablehead1"><th>Account Heads</th><th>This Month Amount</th><th>Previous Month Amount</th><th>Variance</th><tr></thead><tbody></tbody></table></div>');
	    	  $("#staticdashboardindividualbreakup div[class='modal-dialog']").css("width", "70%");
	    	  $("#staticdashboardindividualbreakup h4").html('Chart Of Account Wise-Breakups');
	    	  for(var i=0;i<data.coaBreakUpsData.length;i++){
	    		  $("#staticdashboardindividualbreakup div[class='modal-body'] table[id='coaBreakupTable'] tbody").append('<tr><td>'+data.coaBreakUpsData[i].accountHeadName+'</td><td>'+data.coaBreakUpsData[i].thisWeekAmount+'</td><td>'+data.coaBreakUpsData[i].previousWeekAmount+'</td><td>'+data.coaBreakUpsData[i].variance+'</td></tr>');
	    	  }
    	  }
      },
      error: function (xhr, status, error) {
      	if(xhr.status == 401){ doLogout(); }
      }
   });
}


function custVendOpeningBalanceBreakup(elem,tabElement){
		var jsonData = {};
		jsonData.tabElement = tabElement;
		var url = "/dashboard/custVendOpeningBalanceBreakup";
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
			  $("#staticcashbankreceivablepayablebranchwisebreakup").modal('hide');
			  $("#staticdashboardindividualbreakup").attr('data-toggle', 'modal');
	    	  $("#staticdashboardindividualbreakup").modal('show');
	    	  $(".staticdashboardindividualbreakupclose").attr("href",location.hash);
	    	  $("#staticdashboardindividualbreakup div[class='modal-body']").html("");
	    	  if(tabElement == "accountsPayablesAllBranches"){
	    		  $("#staticdashboardindividualbreakup h4").text("Vendorwise Opening Balances");
	    	  }else{
	    		  $("#staticdashboardindividualbreakup h4").text("Customerwise Opening Balances");
			  }
	    	  if(data.recPayablesOpeningBalBreakup.length>0){
	    		  if(tabElement == "accountsPayablesAllBranches"){
	    			  $("#staticdashboardindividualbreakup div[class='modal-body']").html('<div class="datascrolltable" style=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-create" id="coaBreakupTable" style="margin-top: 0px;">'+
		    		  '<thead class="tablehead1"><th>Vendor Name</th><th>Opening Balance Pending</th><tr></thead><tbody></tbody></table></div>');
		    	  }else{
		    		  $("#staticdashboardindividualbreakup div[class='modal-body']").html('<div class="datascrolltable" style=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-create" id="coaBreakupTable" style="margin-top: 0px;">'+
		    		  '<thead class="tablehead1"><th>Customer Name</th><th>Opening Balance Pending</th><tr></thead><tbody></tbody></table></div>');
		    	  }
	    		  $("#staticdashboardindividualbreakup div[class='modal-dialog']").css("width", "70%");
	    		  $("#staticdashboardindividualbreakup h4").html('Chart Of Account Wise-Breakups');
		    	  for(var i=0;i<data.recPayablesOpeningBalBreakup.length;i++){
		    		  $("#staticdashboardindividualbreakup div[class='modal-body'] table[id='coaBreakupTable'] tbody").append('<tr><td>'+data.recPayablesOpeningBalBreakup[i].customerName+'</td><td>'+data.recPayablesOpeningBalBreakup[i].openingBal+'</td></tr>');
		    	  }
	    	  }
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
}

function displayCustVendOnDashboard(elem, branchID){
	var jsonData = {};
	var modelFor=$(elem).attr('class');
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.branchID = branchID;
	jsonData.txnModelFor=modelFor;
	ajaxCall('/user/branchCustomerVendorReceivablePayables', jsonData, '', '', '', '', 'displayCustVendSuccessOnDashboard', '', true);
}


function displayCustVendSuccessOnDashboard(data){
	if(data.result){
		location.hash="#pendingExpense";
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		$("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").hide();
		if(data.branchCustomerVendorReceivablePayablesData.length>0){
			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable" style="margin-top: 0px; width:450px;">'+
    		'<thead class="tablehead1" style="position:relative"><th>Name</th><th>Opening Balance</th><th>0-30 Days</th><th>31-60 Days</th><th>61-90 Days</th><th>91-180 Days</th><th>OVER 180 Days</th><th>Total</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			for(var i=0;i<data.branchCustomerVendorReceivablePayablesData.length;i++){
				$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#" class="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" onclick="showBranchWiseBreakUp(this);">'+data.branchCustomerVendorReceivablePayablesData[i].branchName+'</a>');

				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivableCustomerVendorBreakupTable'] tbody").append('<tr><td><div style="width:180px;">'+data.branchCustomerVendorReceivablePayablesData[i].customerName+'</div></td>'+

				'<td><div style="width:80px;">'+data.branchCustomerVendorReceivablePayablesData[i].openingBalance+'</div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="under0to30" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under0to30daysamount+'</a></div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="under31to60" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under31to60daysamount+'</a></div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="under61to90" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under61to90daysamount+'</a></div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="under91to180" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under91to180daysamount+'</a></div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="OVER 180 Days" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].over180daysamount+'</a></div></td>'+
				'<td><div style="width:80px;">'+data.branchCustomerVendorReceivablePayablesData[i].netAmount+'</div></td><tr>');

			}
		}
	}
}

function displayCustVendOnOverduesDashboard(elem, branchID){
	var jsonData = {};
	var modelFor=$(elem).attr('class');
	jsonData.email=$("#hiddenuseremail").text();
	jsonData.branchID = branchID;
	jsonData.txnModelFor=modelFor;
	ajaxCall('/user/branchCustomerVendorReceivablePayables', jsonData, '', '', '', '', 'displayCustVendSuccessOverduesOnDashboard', '', true);
}


function displayCustVendSuccessOverduesOnDashboard(data){
	if(data.result){
		location.hash="#pendingExpense";
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticcashbankreceivablepayablebranchwisebreakup").attr('data-toggle', 'modal');
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('show');
		$(".staticcashbankreceivablepayablebranchwisebreakupclose").attr("href",location.hash);
		$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html("");
		$("#staticcashbankreceivablepayablebranchwisebreakup div[id='ageingDiv']").hide();
		if(data.branchCustomerVendorReceivablePayablesData.length>0){
			$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body']").html('<div class="datascrolltable" STYLE=" height: 300px; overflow: auto;"><table class="table table-hover table-striped table-bordered excelFormTable transaction-table" id="branchReceivableCustomerVendorBreakupTable" style="margin-top: 0px; width:450px;">'+
    		'<thead class="tablehead1" style="position:relative"><th>Name</th><th>Opening Balance</th><th>0-30 Days</th><th>31-60 Days</th><th>61-90 Days</th><th>91-180 Days</th><th>OVER 180 Days</th><th>Total</th><tr></thead><tbody style="position:relative"></tbody></table></div>');
			for(var i=0;i<data.branchCustomerVendorReceivablePayablesData.length;i++){
				$("#staticcashbankreceivablepayablebranchwisebreakup h4").html('<a href="#" class="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" onclick="showBranchWiseBreakUp(this);">'+data.branchCustomerVendorReceivablePayablesData[i].branchName+'</a>');

				$("#staticcashbankreceivablepayablebranchwisebreakup div[class='modal-body'] table[id='branchReceivableCustomerVendorBreakupTable'] tbody").append('<tr><td><div style="width:180px;">'+data.branchCustomerVendorReceivablePayablesData[i].customerName+'</div></td>'+

				'<td><div style="width:80px;">'+data.branchCustomerVendorReceivablePayablesData[i].openingBalance+'</div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="under0to30" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under0to30daysoverdueamount+'</a></div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="under31to60" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under31to60daysoverdueamount+'</a></div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="under61to90" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under61to90daysoverdueamount+'</a></div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="under91to180" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].under91to180daysoverdueamount+'</a></div></td>'+
				'<td><div style="width:80px;"><a href="#pendingExpense" class="customerIndividualReceivables" id="'+data.branchCustomerVendorReceivablePayablesData[i].id+'" name="'+data.branchCustomerVendorReceivablePayablesData[i].txnModelFor+'" title="OVER 180 Days" onclick="overUnderOneEightyDaysCustVendTransaction(this, \''+data.branchCustomerVendorReceivablePayablesData[i].branchID+'\');">'+data.branchCustomerVendorReceivablePayablesData[i].over180daysoverdueamount+'</a></div></td>'+
				'<td><div style="width:80px;">'+data.branchCustomerVendorReceivablePayablesData[i].overdueAmount+'</div></td><tr>');

			}
		}
	}
}
// Turnover Panel Data


function calculateTaxForTurnover(elem){
		var parentId = $(elem).closest('table').attr('id');
		
		var taxRate =$.trim($(elem).val());
		setTaxRateTurnover(parentId,taxRate,"interStateAmt","interStateTax");
		setTaxRateTurnover(parentId,taxRate,"intraStateAmt","intraStateTax");
		setTaxRateTurnover(parentId,taxRate,"nonGstAmt","nonGstTax");
		setTaxRateTurnover(parentId,taxRate,"exportAmt","exportTax");
		setTaxRateTurnover(parentId,taxRate,"totalAmt","totalTax");
}


function setTaxRateTurnover(parentId,taxRate,amtClassName,taxClassName){
		if(taxRate == "" || !$.isNumeric(taxRate)) {
			$("#"+parentId).find("."+taxClassName).html("");
			return
		}
		var amount = $.trim($("#"+parentId).find("."+amtClassName).html());
		if(amount != "" && $.isNumeric(amount)) {
			taxRate = Number(taxRate);
			amount = Number(amount);
			var tax = (amount * taxRate) / 100;
			$("#"+parentId).find("."+taxClassName).html(tax.toFixed(2));
		}else{
			$("#"+parentId).find("."+taxClassName).html("")
		}
	
}




