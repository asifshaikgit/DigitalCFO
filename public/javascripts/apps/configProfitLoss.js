var isProfitAndLossGenerated = false;
//Renders the Profit loss report page.
function renderProfitLoss(elem){
    var mouldesRights = $("#usermoduleshidden").val();
    showHideModuleTabs(mouldesRights);
	$("#pendingExpense").hide();
	$("#trialBalance").hide();
	$("#cashAndBank").hide();
	$("#bankBookDiv").hide();
	$("#cashBookDiv").hide();
	$('#periodicInventory').hide();
	$('#reportInventory').hide();
	$('#reportAllInventory').hide();
	$('#balanceSheet').hide();
	$('#plbsCoaMapping').hide();
	$('#profitloss').show();
	
}

var displayInventory = function(){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/profitLoss/displayinventory";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "GET",
		contentType : 'application/json',
		success     : function (data) {
			if(data){
				if(data.row1 != undefined){
					$("#openRawMaterial").val(data.row1[0].openBalCr);
					$("#closingRawMaterial").val(data.row1[0].closeBalCr);
					$("#prevopenRawMaterial").val(data.row1[0].openBalPr);
					$("#prevclsingRawMaterial").val(data.row1[0].closeBalPr);
				}
				if(data.row2 != undefined){
					$("#openconsumables").val(data.row2[0].openBalCr);
					$("#closingconsumables").val(data.row2[0].closeBalCr);
					$("#prevopenconsumables").val(data.row2[0].openBalPr);
					$("#prevclosingconsumables").val(data.row2[0].closeBalPr);
				}
				if(data.row3 != undefined){
					$("#openfinishedGoods").val(data.row3[0].openBalCr);
					$("#closingfinishedGoods").val(data.row3[0].closeBalCr);
					$("#prevopenfinishedGoods").val(data.row3[0].openBalPr);
					$("#prevclosingfinishedGoods").val(data.row3[0].closeBalPr);
				}
				if(data.row4 != undefined){
					$("#openworkInProgress").val(data.row4[0].openBalCr);
					$("#closingworkInProgress").val(data.row4[0].closeBalCr);
					$("#prevopenworkInProgress").val(data.row4[0].openBalPr);
					$("#prevclosingworkInProgress").val(data.row4[0].closeBalPr);
				}
				if(data.row5 != undefined){
					$("#openstockInTrade").val(data.row5[0].openBalCr);
					$("#closingstockInTrade").val(data.row5[0].closeBalCr);
					$("#prevopenstockInTrade").val(data.row5[0].openBalPr);
					$("#prevclosingstockInTrade").val(data.row5[0].closeBalPr);
				}
			}
			var openTotal = 0;
			$(".openingBalCr").each(function() {
				openTotal+=parseFloat($(this).val());
			});
			$("#openTotal").val(openTotal.toFixed(2));
			openTotal = 0;
			$(".closingBalCr").each(function() {
				openTotal+=parseFloat($(this).val());
			});
			$("#closingTotal").val(openTotal.toFixed(2));
			openTotal = 0;
			$(".openingBalPr").each(function() {
				openTotal+=parseFloat($(this).val());
			});
			$("#prevopenTotal").val(openTotal.toFixed(2));
			openTotal = 0;
			$(".closingBalPr").each(function() {
				openTotal+=parseFloat($(this).val());
			});
			$("#prevclosingTotal").val(openTotal.toFixed(2));
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ 
				doLogout(); 
			}else if(xhr.status == 500){
	    		swal("Error on fetching Inventory!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

$(document).ready(function(){
	$('#generatePLbtn').click(function(){
		var currPLFromDate = $("#currPLFromDate").val();
		if(currPLFromDate == ""){
			swal("Current period reporting 'From Date' cannot be empty.", "Error", "error");
			//swal({ html:true, title:'<i>Error</i>', text:'Current period reporting <b>from date</b> cannot be empty.'});
			return false;
		}
		var currPLToDate = $("#currPLToDate").val();
		if(currPLToDate == ""){
			swal("Current period reporting 'To date' cannot be empty.", "Error", "error");
			return false;
		}
		if(new Date(currPLFromDate).getTime() > new Date(currPLToDate).getTime()){
			swal("Current period reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
			return false;
		}
		var prevPLFromDate = $("#prevPLFromDate").val();
		var prevPLToDate = $("#prevPLToDate").val();
		if(prevPLFromDate != "" && prevPLToDate == ""){
			swal("Previous reporting 'To Date' cannot be empty.", "Error", "error");
			return false;
		}
		if(new Date(prevPLFromDate).getTime() > new Date(prevPLToDate).getTime()){
			swal("Previous reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
			return false;
		}

		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		jsonData.currPLFromDate = currPLFromDate;
		jsonData.currPLToDate = currPLToDate;
		jsonData.prevPLFromDate = prevPLFromDate;
		jsonData.prevPLToDate = prevPLToDate;
		ajaxCall('/profitLoss/display', jsonData, '', 'POST', '', '', 'renderProfitLossSuccess', '', true);
	});


	$('.inventoryInputcls').keyup(function(event){
		onlyDotsAndNumbersandMinus(event,this);
		calculatInventoryTotal(this);
	});
	$('.inventoryInputcls').blur(function(event){
		onlyDotsAndNumbersandMinus(event,this);
		calculatInventoryTotal(this);
	});

	
	$('#saveupdateinventorybtn').click(function(){
		var openRawMaterial = $("#openRawMaterial").val();
		var closingRawMaterial = $("#closingRawMaterial").val();
		var prevopenRawMaterial = $("#prevopenRawMaterial").val();
		var prevclsingRawMaterial = $("#prevclsingRawMaterial").val();

		var openconsumables = $("#openconsumables").val();
		var closingconsumables = $("#closingconsumables").val();
		var prevopenconsumables = $("#prevopenconsumables").val();
		var prevclosingconsumables = $("#prevclosingconsumables").val();

		var openfinishedGoods = $("#openfinishedGoods").val();
		var closingfinishedGoods = $("#closingfinishedGoods").val();
		var prevopenfinishedGoods = $("#prevopenfinishedGoods").val();
		var prevclosingfinishedGoods = $("#prevclosingfinishedGoods").val();

		var openworkInProgress = $("#openworkInProgress").val();
		var closingworkInProgress = $("#closingworkInProgress").val();
		var prevopenworkInProgress = $("#prevopenworkInProgress").val();
		var prevclosingworkInProgress = $("#prevclosingworkInProgress").val();

		var openstockInTrade = $("#openstockInTrade").val();
		var closingstockInTrade = $("#closingstockInTrade").val();
		var prevopenstockInTrade = $("#prevopenstockInTrade").val();
		var prevclosingstockInTrade = $("#prevclosingstockInTrade").val();

		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		jsonData.openRawMaterial = openRawMaterial;
		jsonData.closingRawMaterial = closingRawMaterial;
		jsonData.prevopenRawMaterial = prevopenRawMaterial;
		jsonData.prevclsingRawMaterial = prevclsingRawMaterial;

		jsonData.openconsumables = openconsumables;
		jsonData.closingconsumables = closingconsumables;
		jsonData.prevopenconsumables = prevopenconsumables;
		jsonData.prevclosingconsumables = prevclosingconsumables;

		jsonData.openfinishedGoods = openfinishedGoods;
		jsonData.closingfinishedGoods = closingfinishedGoods;
		jsonData.prevopenfinishedGoods = prevopenfinishedGoods;
		jsonData.prevclosingfinishedGoods = prevclosingfinishedGoods;

		jsonData.openworkInProgress = openworkInProgress;
		jsonData.closingworkInProgress = closingworkInProgress;
		jsonData.prevopenworkInProgress = prevopenworkInProgress;
		jsonData.prevclosingworkInProgress = prevclosingworkInProgress;

		jsonData.openstockInTrade = openstockInTrade;
		jsonData.closingstockInTrade = closingstockInTrade;
		jsonData.prevopenstockInTrade = prevopenstockInTrade;
		jsonData.prevclosingstockInTrade = prevclosingstockInTrade;
		ajaxCall('/profitLoss/saveupdateinventory', jsonData, '', 'POST', '', '', 'saveUpdateInventory', '', true);
	});
});

var calculatInventoryTotal = function(elem){
	var openTotal = 0;
	var className = $(elem).attr('class');
	if(className == "inventoryInputcls openingBalCr"){
		$(".openingBalCr").each(function() {
			openTotal+=parseFloat($(this).val());
		});
		$("#openTotal").val(openTotal.toFixed(2));
	}else if(className == "inventoryInputcls closingBalCr"){
		$(".closingBalCr").each(function() {
			openTotal+=parseFloat($(this).val());
		});
		$("#closingTotal").val(openTotal.toFixed(2));
	}else if(className == "inventoryInputcls openingBalPr"){
		$(".openingBalPr").each(function() {
			openTotal+=parseFloat($(this).val());
		});
		$("#prevopenTotal").val(openTotal.toFixed(2));
	}else if(className == "inventoryInputcls closingBalPr"){
		$(".closingBalPr").each(function() {
			openTotal+=parseFloat($(this).val());
		});
		$("#prevclosingTotal").val(openTotal.toFixed(2));
	}
}


var saveUpdateInventory = function(data){
	$('#generateplreportmodal').modal('hide');
	if(!isEmpty(data)){
		if (data.issuccess == 1) {
			swal({
			    title: "Saved successfully!",
			    text: "Inventory detail",
			    type: "success"
			},
			function() {
			    $('#generateplreportmodal').modal('show');
			});
		}else{
			swal({
			    title: "Failed to save!",
			    text: "Inventory detail",
			    type: "error"
			},
			function() {
			    $('#generateplreportmodal').modal('show');
			});
		}
	}else{
		swal({
			    title: "Failed to save!",
			    text: "Inventory detail",
			    type: "error"
			},
			function() {
			    $('#generateplreportmodal').modal('show');
			});
	}
}

//Success function once profit loss fetch is successful.
function renderProfitLossSuccess(data){
	if(data.result){
		$("#revenueFrmOpers").html(data.ProfitLossBean.revenueFrmOpers.toFixed(2));
		$("#otherIncome").html(data.ProfitLossBean.otherIncome.toFixed(2));
		$("#totRevenue").html(data.ProfitLossBean.totRevenue.toFixed(2));
		$("#costOfmatConsumed").html(data.ProfitLossBean.costOfmatConsumed.toFixed(2));
		$("#purchasesOfStockinTrade").html(data.ProfitLossBean.purchasesOfStockinTrade.toFixed(2));
		$("#chnFinGud").html(data.ProfitLossBean.chnFinGud.toFixed(2));
		$("#empBenExp").html(data.ProfitLossBean.empBenExp.toFixed(2));
		$("#financeCost").html(data.ProfitLossBean.financeCostsExp.toFixed(2));
		$("#deprecAmtExp").html(data.ProfitLossBean.deprecAmtExp.toFixed(2));
		$("#othExp").html(data.ProfitLossBean.othExp.toFixed(2));
		$("#totExp").html(data.ProfitLossBean.totExp.toFixed(2));
		$("#profitExItmTx").html(data.ProfitLossBean.profitExItmTx.toFixed(2));
		$("#expItems").html(data.ProfitLossBean.expItems.toFixed(2));
		$("#profExtItmTx").html(data.ProfitLossBean.profExtItmTx.toFixed(2));
		$("#extrItms").html(data.ProfitLossBean.extrItms.toFixed(2));
		$("#profBefrTx").html(data.ProfitLossBean.profBefrTx.toFixed(2));
		$("#curTx").html(data.ProfitLossBean.curTx.toFixed(2));
		$("#defTx").html(data.ProfitLossBean.defTx.toFixed(2));
		/*$("#profContOprn").html(data.ProfitLossBean.profContOprn.toFixed(2));
		$("#profDisContOprn").html(data.ProfitLossBean.profDisContOprn.toFixed(2));
		$("#txExpDisContOprn").html(data.ProfitLossBean.txExpDisContOprn.toFixed(2));
		$("#profDisContOprnAftTx").html(data.ProfitLossBean.profDisContOprnAftTx.toFixed(2));
		*/
		$("#profForPeriod").html(data.ProfitLossBean.profForPeriod.toFixed(2));
		//$("#earnEqtShrBasic").html(data.ProfitLossBean.earnEqtShrBasic.toFixed(2));
		//$("#earnEqtShrDiluted").html(data.ProfitLossBean.earnEqtShrDiluted.toFixed(2));

		//Previous period.
		var prevPLFromDate = $("#prevPLFromDate").val();
		if(prevPLFromDate != ""){
			$("#revenueFrmOpersPrvRpt").html(data.ProfitLossBean.revenueFrmOpersPrvRpt.toFixed(2));
			$("#otherIncomePrvRpt").html(data.ProfitLossBean.otherIncomePrvRpt.toFixed(2));
			$("#totRevenuePrvRpt").html(data.ProfitLossBean.totRevenuePrvRpt.toFixed(2));
			$("#costOfmatConsumedPrvRpt").html(data.ProfitLossBean.costOfmatConsumedPrvRpt.toFixed(2));
			$("#purchasesOfStockinTradePrvRpt").html(data.ProfitLossBean.purchasesOfStockinTradePrvRpt.toFixed(2));
			$("#chnFinGudPrvRpt").html(data.ProfitLossBean.chnFinGudPrvRpt.toFixed(2));
			$("#empBenExpPrvRpt").html(data.ProfitLossBean.empBenExpPrvRpt.toFixed(2));
			$("#financeCostPrvRpt").html(data.ProfitLossBean.financeCostsExpPrvRpt.toFixed(2));
			$("#deprecAmtExpPrvRpt").html(data.ProfitLossBean.deprecAmtExpPrvRpt.toFixed(2));
			$("#othExpPrvRpt").html(data.ProfitLossBean.othExpPrvRpt.toFixed(2));
			$("#totExpPrvRpt").html(data.ProfitLossBean.totExpPrvRpt.toFixed(2));
			$("#profitExItmTxPrvRpt").html(data.ProfitLossBean.profitExItmTxPrvRpt.toFixed(2));
			$("#expItemsPrvRpt").html(data.ProfitLossBean.expItemsPrvRpt.toFixed(2));
			$("#profExtItmTxPrvRpt").html(data.ProfitLossBean.profExtItmTxPrvRpt.toFixed(2));
			$("#extrItmsPrvRpt").html(data.ProfitLossBean.extrItmsPrvRpt.toFixed(2));
			$("#profBefrTxPrvRpt").html(data.ProfitLossBean.profBefrTxPrvRpt.toFixed(2));
			$("#curTxPrvRpt").html(data.ProfitLossBean.curTxPrvRpt.toFixed(2));
			$("#defTxPrvRpt").html(data.ProfitLossBean.defTxPrvRpt.toFixed(2));
			/*$("#profContOprnPrvRpt").html(data.ProfitLossBean.profContOprnPrvRpt.toFixed(2));
			$("#profDisContOprnPrvRpt").html(data.ProfitLossBean.profDisContOprnPrvRpt.toFixed(2));
			$("#txExpDisContOprnPrvRpt").html(data.ProfitLossBean.txExpDisContOprnPrvRpt.toFixed(2));
			$("#profDisContOprnAftTxPrvRpt").html(data.ProfitLossBean.profDisContOprnAftTxPrvRpt.toFixed(2)); */
			$("#profForPeriodPrvRpt").html(data.ProfitLossBean.profForPeriodPrvRpt.toFixed(2));
			/*$("#earnEqtShrBasicPrvRpt").html(data.ProfitLossBean.earnEqtShrBasicPrvRpt.toFixed(2));
			$("#earnEqtShrDilutedPrvRpt").html(data.ProfitLossBean.earnEqtShrDilutedPrvRpt.toFixed(2));*/
		}
		isProfitAndLossGenerated = true;
	}
}

//Renders the Balance Sheet page.
function renderBalanceSheet(elem){
	if(isProfitAndLossGenerated === false){
		swal("invalid action!", "Must generate Profile & Loss before proceding for Balance Sheet generation.", "error");
		return false;
	}
	var currPLFromDate = $("#currPLFromDate").val();
	if(currPLFromDate == ""){
		swal("Current period reporting 'From Date' cannot be empty.", "Error", "error");
		//swal({ html:true, title:'<i>Error</i>', text:'Current period reporting <b>from date</b> cannot be empty.'});
		return false;
	}
	var currPLToDate = $("#currPLToDate").val();
	if(currPLToDate == ""){
		swal("Current period reporting 'To date' cannot be empty.", "Error", "error");
		return false;
	}
	if(new Date(currPLFromDate).getTime() > new Date(currPLToDate).getTime()){
		swal("Current period reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
		return false;
	}
	var prevPLFromDate = $("#prevPLFromDate").val();
	var prevPLToDate = $("#prevPLToDate").val();
	if(prevPLFromDate != "" && prevPLToDate == ""){
		swal("Previous reporting 'To Date' cannot be empty.", "Error", "error");
		return false;
	}
	if(new Date(prevPLFromDate).getTime() > new Date(prevPLToDate).getTime()){
		swal("Previous reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
		return false;
	}

	var jsonData = {};
	jsonData.userEmail = $("#hiddenuseremail").text();
	jsonData.currPLFromDate = currPLFromDate;
	jsonData.currPLToDate = currPLToDate;
	jsonData.prevPLFromDate = prevPLFromDate;
	jsonData.prevPLToDate = prevPLToDate;
	jsonData.profForPeriod = $("#profForPeriod").html();
	jsonData.profForPeriodPrvRpt = $("#profForPeriodPrvRpt").html();
	ajaxCall('/balanceSheet/display', jsonData, '', '', '', '', 'renderBalanceSheetSuccess', '', true);
	$("#pendingExpense").hide();
	$("#trialBalance").hide();
	$("#cashAndBank").hide();
	$('#periodicInventory').hide();
	$('#reportInventory').hide();
	$('#reportAllInventory').hide();
	$('#profitloss').hide();
	$('#plbsCoaMapping').hide();
	$('#balanceSheet').show();
}

//Success function once the Balance sheet data fetch is successful.
function renderBalanceSheetSuccess(data){
	$("#shareCapital").html(data.BalanceSheetBean.shareCapital.toFixed(2));
	$("#reservesAndSurplus").html(data.BalanceSheetBean.reservesAndSurplus.toFixed(2));
	$("#profitLossForPeriod").html(data.BalanceSheetBean.profitLossForPeriod.toFixed(2));
	$("#profitLossForPeriodPlusReservesSurplus").html(data.BalanceSheetBean.profitLossForPeriodPlusReservesSurplus.toFixed(2));
	$("#moneyReceivedAgainstShareWarrants").html(data.BalanceSheetBean.moneyReceivedAgainstShareWarrants.toFixed(2));
	
	$("#shareholderFundsTotal").html(data.BalanceSheetBean.shareholderFundsTotal.toFixed(2));

	$("#shareApplicationMoneyPendingAllotment").html(data.BalanceSheetBean.shareApplicationMoneyPendingAllotment.toFixed(2));
	$("#longTermBorrowings").html(data.BalanceSheetBean.longTermBorrowings.toFixed(2));
	$("#deferredTaxLiabilitiesNet").html(data.BalanceSheetBean.deferredTaxLiabilitiesNet.toFixed(2));
	$("#otherLongTermLiabilities").html(data.BalanceSheetBean.otherLongTermLiabilities.toFixed(2));
	$("#longTermProvisions").html(data.BalanceSheetBean.longTermProvisions.toFixed(2));
	
	$("#nonCurrentLiabilitiesTotal").html(data.BalanceSheetBean.nonCurrentLiabilitiesTotal.toFixed(2));

	$("#shortTermBorrowings").html(data.BalanceSheetBean.shortTermBorrowings.toFixed(2));
	$("#tradePayables").html(data.BalanceSheetBean.tradePayables.toFixed(2));
	$("#otherCurrentLiabilities").html(data.BalanceSheetBean.otherCurrentLiabilities.toFixed(2));
	$("#shortTermProvisions").html(data.BalanceSheetBean.shortTermProvisions.toFixed(2));
	
	$("#currentLiabilitiesTotal").html(data.BalanceSheetBean.currentLiabilitiesTotal.toFixed(2));
	
	$("#liabilityTotal").html(data.BalanceSheetBean.liabilityTotal.toFixed(2));

	$("#tangibleAssets").html(data.BalanceSheetBean.tangibleAssets.toFixed(2));
	$("#intangibleAssets").html(data.BalanceSheetBean.intangibleAssets.toFixed(2));
	$("#capitalWorkInProgress").html(data.BalanceSheetBean.capitalWorkInProgress.toFixed(2));
	$("#intangibleAssetsUnderDevelopment").html(data.BalanceSheetBean.intangibleAssetsUnderDevelopment.toFixed(2));
	$("#nonCurrentInvestments").html(data.BalanceSheetBean.nonCurrentInvestments.toFixed(2));
	$("#deferredTaxAssetsNet").html(data.BalanceSheetBean.deferredTaxAssetsNet.toFixed(2));
	$("#longTermLoansAndAdvances").html(data.BalanceSheetBean.longTermLoansAndAdvances.toFixed(2));
	$("#otherNonCurrentAssets").html(data.BalanceSheetBean.otherNonCurrentAssets.toFixed(2));
	
	$("#nonCurrentAssetsTotal").html(data.BalanceSheetBean.nonCurrentAssetsTotal.toFixed(2));

	$("#currentInvestments").html(data.BalanceSheetBean.currentInvestments.toFixed(2));
	$("#inventories").html(data.BalanceSheetBean.inventories.toFixed(2));
	$("#tradeReceivables").html(data.BalanceSheetBean.tradeReceivables.toFixed(2));
	$("#cashAndCashEquivalents").html(data.BalanceSheetBean.cashAndCashEquivalents.toFixed(2));
	$("#shortTermLoansAndAdvances").html(data.BalanceSheetBean.shortTermLoansAndAdvances.toFixed(2));
	$("#otherCurrentAssets").html(data.BalanceSheetBean.otherCurrentAssets.toFixed(2));
	
	$("#currentAssetsTotal").html(data.BalanceSheetBean.currentAssetsTotal.toFixed(2));
	
	$("#assetTotal").html(data.BalanceSheetBean.assetTotal.toFixed(2));

	var prevPLFromDate = $("#prevPLFromDate").val();
	if(prevPLFromDate != ""){
		$("#shareCapitalPrvRpt").html(data.BalanceSheetBean.shareCapitalPrvRpt.toFixed(2));
		$("#reservesAndSurplusPrvRpt").html(data.BalanceSheetBean.reservesAndSurplusPrvRpt.toFixed(2));
		$("#profitLossForPeriodPrvRpt").html(data.BalanceSheetBean.profitLossForPeriodPrvRpt.toFixed(2));
		$("#profitLossForPeriodPlusReservesSurplusPrvRpt").html(data.BalanceSheetBean.profitLossForPeriodPlusReservesSurplusPrvRpt.toFixed(2));
		$("#moneyReceivedAgainstShareWarrantsPrvRpt").html(data.BalanceSheetBean.moneyReceivedAgainstShareWarrantsPrvRpt.toFixed(2));
		
		$("#shareholderFundsTotalPrvRpt").html(data.BalanceSheetBean.shareholderFundsTotalPrvRpt.toFixed(2));

		$("#shareApplicationMoneyPendingAllotmentPrvRpt").html(data.BalanceSheetBean.shareApplicationMoneyPendingAllotmentPrvRpt.toFixed(2));
		$("#longTermBorrowingsPrvRpt").html(data.BalanceSheetBean.longTermBorrowingsPrvRpt.toFixed(2));
		$("#deferredTaxLiabilitiesNetPrvRpt").html(data.BalanceSheetBean.deferredTaxLiabilitiesNetPrvRpt.toFixed(2));
		$("#otherLongTermLiabilitiesPrvRpt").html(data.BalanceSheetBean.otherLongTermLiabilitiesPrvRpt.toFixed(2));
		$("#longTermProvisionsPrvRpt").html(data.BalanceSheetBean.longTermProvisionsPrvRpt.toFixed(2));
		
		$("#nonCurrentLiabilitiesTotalPrvRpt").html(data.BalanceSheetBean.nonCurrentLiabilitiesTotalPrvRpt.toFixed(2));

		$("#shortTermBorrowingsPrvRpt").html(data.BalanceSheetBean.shortTermBorrowingsPrvRpt.toFixed(2));
		$("#tradePayablesPrvRpt").html(data.BalanceSheetBean.tradePayablesPrvRpt.toFixed(2));
		$("#otherCurrentLiabilitiesPrvRpt").html(data.BalanceSheetBean.otherCurrentLiabilitiesPrvRpt.toFixed(2));
		$("#shortTermProvisionsPrvRpt").html(data.BalanceSheetBean.shortTermProvisionsPrvRpt.toFixed(2));
		
		$("#currentLiabilitiesTotalPrvRpt").html(data.BalanceSheetBean.currentLiabilitiesTotalPrvRpt.toFixed(2));
		
		$("#liabilityTotalPrvRpt").html(data.BalanceSheetBean.liabilityTotalPrvRpt.toFixed(2));

		$("#tangibleAssetsPrvRpt").html(data.BalanceSheetBean.tangibleAssetsPrvRpt.toFixed(2));
		$("#intangibleAssetsPrvRpt").html(data.BalanceSheetBean.intangibleAssetsPrvRpt.toFixed(2));
		$("#capitalWorkInProgressPrvRpt").html(data.BalanceSheetBean.capitalWorkInProgressPrvRpt.toFixed(2));
		$("#intangibleAssetsUnderDevelopmentPrvRpt").html(data.BalanceSheetBean.intangibleAssetsUnderDevelopmentPrvRpt.toFixed(2));
		$("#nonCurrentInvestmentsPrvRpt").html(data.BalanceSheetBean.nonCurrentInvestmentsPrvRpt.toFixed(2));
		$("#deferredTaxAssetsNetPrvRpt").html(data.BalanceSheetBean.deferredTaxAssetsNetPrvRpt.toFixed(2));
		$("#longTermLoansAndAdvancesPrvRpt").html(data.BalanceSheetBean.longTermLoansAndAdvancesPrvRpt.toFixed(2));
		$("#otherNonCurrentAssetsPrvRpt").html(data.BalanceSheetBean.otherNonCurrentAssetsPrvRpt.toFixed(2));
		
		$("#nonCurrentAssetsTotalPrvRpt").html(data.BalanceSheetBean.nonCurrentAssetsTotalPrvRpt.toFixed(2));

		$("#currentInvestmentsPrvRpt").html(data.BalanceSheetBean.currentInvestmentsPrvRpt.toFixed(2));
		$("#inventoriesPrvRpt").html(data.BalanceSheetBean.inventoriesPrvRpt.toFixed(2));
		$("#tradeReceivablesPrvRpt").html(data.BalanceSheetBean.tradeReceivablesPrvRpt.toFixed(2));
		$("#cashAndCashEquivalentsPrvRpt").html(data.BalanceSheetBean.cashAndCashEquivalentsPrvRpt.toFixed(2));
		$("#shortTermLoansAndAdvancesPrvRpt").html(data.BalanceSheetBean.shortTermLoansAndAdvancesPrvRpt.toFixed(2));
		$("#otherCurrentAssetsPrvRpt").html(data.BalanceSheetBean.otherCurrentAssetsPrvRpt.toFixed(2));
		
		$("#currentAssetsTotalPrvRpt").html(data.BalanceSheetBean.currentAssetsTotalPrvRpt.toFixed(2));

		$("#assetTotalPrvRpt").html(data.BalanceSheetBean.assetTotalPrvRpt.toFixed(2));
	}
	//Show the message if there is difference between assets and liabilities value.
	if(data.isCurrentDiff === true || data.isPreviousDiff == true){
		$("#diffAssetLiability").show();
	}else{
		$("#diffAssetLiability").hide();
	}
}


var exportPnLReport = function(exportType){
	var currPLFromDate = $("#currPLFromDate").val();
	if(currPLFromDate == ""){
		swal("Current period reporting 'From Date' cannot be empty.", "Error", "error");
		return false;
	}
	var currPLToDate = $("#currPLToDate").val();
	if(currPLToDate == ""){
		swal("Current period reporting 'To date' cannot be empty.", "Error", "error");
		return false;
	}
	if(new Date(currPLFromDate).getTime() > new Date(currPLToDate).getTime()){
		swal("Current period reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
		return false;
	}
	var prevPLFromDate = $("#prevPLFromDate").val();
	var prevPLToDate = $("#prevPLToDate").val();
	if(prevPLFromDate != "" && prevPLToDate == ""){
		swal("Previous reporting 'To Date' cannot be empty.", "Error", "error");
		return false;
	}
	if(new Date(prevPLFromDate).getTime() > new Date(prevPLToDate).getTime()){
		swal("Previous reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
		return false;
	}

	var jsonData = {};
	jsonData.userEmail = $("#hiddenuseremail").text();
	jsonData.currPLFromDate = currPLFromDate;
	jsonData.currPLToDate = currPLToDate;
	jsonData.prevPLFromDate = prevPLFromDate;
	jsonData.prevPLToDate = prevPLToDate;
	jsonData.exportType = exportType;
	downloadFile('/profitLoss/export', "POST", jsonData, "Error on downloading profile and loss!");
}

var exportBSReport = function(exportType){
	if(isProfitAndLossGenerated === false){
		swal("invalid action!", "Must generate Profile & Loss before proceding for Balance Sheet generation.", "error");
		return false;
	}
	var currPLFromDate = $("#currPLFromDate").val();
	if(currPLFromDate == ""){
		swal("Current period reporting 'From Date' cannot be empty.", "Error", "error");
		//swal({ html:true, title:'<i>Error</i>', text:'Current period reporting <b>from date</b> cannot be empty.'});
		return false;
	}
	var currPLToDate = $("#currPLToDate").val();
	if(currPLToDate == ""){
		swal("Current period reporting 'To date' cannot be empty.", "Error", "error");
		return false;
	}
	if(new Date(currPLFromDate).getTime() > new Date(currPLToDate).getTime()){
		swal("Current period reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
		return false;
	}
	var prevPLFromDate = $("#prevPLFromDate").val();
	var prevPLToDate = $("#prevPLToDate").val();
	if(prevPLFromDate != "" && prevPLToDate == ""){
		swal("Previous reporting 'To Date' cannot be empty.", "Error", "error");
		return false;
	}
	if(new Date(prevPLFromDate).getTime() > new Date(prevPLToDate).getTime()){
		swal("Previous reporting 'From Date' cannot be greater than 'To Date'.", "Error", "error");
		return false;
	}

	var jsonData = {};
	jsonData.userEmail = $("#hiddenuseremail").text();
	jsonData.currPLFromDate = currPLFromDate;
	jsonData.currPLToDate = currPLToDate;
	jsonData.prevPLFromDate = prevPLFromDate;
	jsonData.prevPLToDate = prevPLToDate;
	jsonData.profForPeriod = $("#profForPeriod").html();
	jsonData.profForPeriodPrvRpt = $("#profForPeriodPrvRpt").html();
	jsonData.exportType = exportType;
	downloadFile('/balanceSheet/export', "POST", jsonData, "Error on downloading balance sheet!");
}
