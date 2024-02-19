var expenseSelectArray = ["costOfMaterialsConsumedCOASelect","purchasesOfStockInTradeCOASelect","cIIOfFGWIPAndStockInTradeCOASelect","employeeBenefitsExpenseFinanceCostsCOASelect","financeCostsCOASelect","depreciationAndAmortizationExpenseCOASelect","otherExpensesCOASelect","exceptionalItemsCOASelect","extraordinaryItemsCOASelect","currentTaxCOASelect","deferredTaxCOASelect"];
var liabilitiesSelectArray = ["shareCapitalCOASelect", "reservesAndSurplusCOASelect", "moneyReceivedAgainstShareWarrantsCOASelect", "shareApplicationMoneyPendingAllotmentCOASelect", "longTermBorrowingsCOASelect", "deferredTaxLiabilitiesNetCOASelect", "otherLongTermLiabilitiesCOASelect", "longTermProvisionsCOASelect", "shortTermBorrowingsCOASelect", "tradePayablesCOASelect", "otherCurrentLiabilitiesCOASelect", "shortTermProvisionsCOASelect"];
var assetsSelectArray = ["tangibleAssetsCOASelect", "intangibleAssetsCOASelect", "capitalWorkInProgressCOASelect", "intangibleAssetsUnderDevelopmentCOASelect", "nonCurrentInvestmentsCOASelect", "deferredTaxAssetsNetCOASelect", "longTermLoansAndAdvancesCOASelect", "otherNonCurrentAssetsCOASelect", "currentInvestmentsCOASelect", "inventoriesCOASelect", "tradeReceivablesCOASelect" , "cashAndCashEquivalentsCOASelect", "shortTermLoansAndAdvancesCOASelect", "otherCurrentAssetsCOASelect"];
var $incomeOptionsCache = "";
$(document).ready(function(){
	$('.multipleCheckSelect').multiselect({
	    includeSelectAllOption: true,
	    enableFiltering :true,
	    enableCaseInsensitiveFiltering: true,
	    buttonWidth: '220px',
   		 maxHeight:   300,
	    /*buttonText: function(options) {
			if (options.length == 0) {
	            return 'None selected <b class="caret"></b>';
	        }else if (options.length > 6) {
	            return options.length + ' selected  <b class="caret"></b>';
	        }else{
	            var selected = '';
				options.each(function(){
					selected += $(this).text() + ', ';
				});
	            return options.length + ' selected  <b class="caret"></b>';
	      	}
	    },*/
	    onSelectAll: function() {
            var elemId = this.$select.context.id;
            if(elemId=="revenueFromOperationsSelect"){
    	  		$("#incomecoaitems").children().remove();
	  			$("#otherIncomeCOASelect option:not(:selected)").attr("disabled", "disabled");
			  	$('#otherIncomeCOASelect').multiselect('rebuild');
			}else if(elemId=="otherIncomeCOASelect"){
		  		$("#incomecoaitems").children().remove();
		  		$("#revenueFromOperationsSelect option:not(:selected)").attr("disabled", "disabled");
				$('#revenueFromOperationsSelect').multiselect('rebuild');
			}else if(expenseSelectArray.indexOf(elemId) !== -1){
				$("#expensecoaitems").children().remove();
				for(var i=0; i < expenseSelectArray.length; i++){
					var tempID = expenseSelectArray[i];
					if(tempID !== elemId){
				  		$("#"+tempID+" option:not(:selected)").attr("disabled", "disabled");
						$('#'+tempID).multiselect('rebuild');
					}
				}
			}else if(liabilitiesSelectArray.indexOf(elemId) !== -1){
				$("#liabilitiescoaitems").children().remove();
				for(var i=0; i < liabilitiesSelectArray.length; i++){
					var tempID = liabilitiesSelectArray[i];
					if(tempID !== elemId){
				  		$("#"+tempID+" option:not(:selected)").attr("disabled", "disabled");
						$('#'+tempID).multiselect('rebuild');
					}
				}
			}else if(assetsSelectArray.indexOf(elemId) !== -1){
				$("#assetsscoaitems").children().remove();
				for(var i=0; i < assetsSelectArray.length; i++){
					var tempID = assetsSelectArray[i];
					if(tempID !== elemId){
				  		$("#"+tempID+" option:not(:selected)").attr("disabled", "disabled");
						$('#'+tempID).multiselect('rebuild');
					}
				}
			}
        },
        onDeselectAll: function() {
            var elemId = this.$select.context.id;
            var $deselectOptions = $('#'+elemId+' option:enabled').clone();
            if(elemId=="revenueFromOperationsSelect"){
		  		$('#incomecoaitems').append($deselectOptions);
		  		$("#otherIncomeCOASelect").children().removeAttr("disabled");
				$('#otherIncomeCOASelect').multiselect('rebuild');
			}else if(elemId=="otherIncomeCOASelect"){
				$('#incomecoaitems').append($deselectOptions);
		  		$("#revenueFromOperationsSelect").children().removeAttr("disabled");
				$('#revenueFromOperationsSelect').multiselect('rebuild');
			}else if(expenseSelectArray.indexOf(elemId) !== -1){
				$('#expensecoaitems').append($deselectOptions);
				for(var i=0; i < expenseSelectArray.length; i++){
					var tempID = expenseSelectArray[i];
					if(tempID !== elemId){
				  		$("#"+tempID).children().removeAttr("disabled");
						$('#'+tempID).multiselect('rebuild');
					}
				}
			}else if(liabilitiesSelectArray.indexOf(elemId) !== -1){
				$('#liabilitiescoaitems').append($deselectOptions);
				for(var i=0; i < liabilitiesSelectArray.length; i++){
					var tempID = liabilitiesSelectArray[i];
					if(tempID !== elemId){
				  		$("#"+tempID).children().removeAttr("disabled");
						$('#'+tempID).multiselect('rebuild');
					}
				}
			}else if(assetsSelectArray.indexOf(elemId) !== -1){
				$('#assetsscoaitems').append($deselectOptions);
				for(var i=0; i < assetsSelectArray.length; i++){
					var tempID = assetsSelectArray[i];
					if(tempID !== elemId){
				  		$("#"+tempID).children().removeAttr("disabled");
						$('#'+tempID).multiselect('rebuild');
					}
				}
			}
        },
	    onChange: function(element, checked) {
			var elemId=$(element).context.offsetParent.id;
			var elemValue = $(element).val();
		  	var elemText = $(element).text();
			if(elemId=="revenueFromOperationsSelect"){
			  	if(checked == true && elemValue != "multiselect-all") {
			  		$("#incomecoaitems option[value='"+ elemValue + "']").remove();
			  		$("#otherIncomeCOASelect option[value='"+ elemValue + "']").attr("disabled", "disabled");
			  	}else if(checked == false && elemValue != "multiselect-all") {
				 	$("#incomecoaitems").append('<option value="'+elemValue+'">' +elemText+ '</option>');
				 	$("#otherIncomeCOASelect option[value='"+ elemValue + "']").removeAttr("disabled");
			  	} 
			  	$('#otherIncomeCOASelect').multiselect('rebuild');
			}else if(elemId=="otherIncomeCOASelect"){
			  	if(checked == true && elemValue != "multiselect-all") {
			  		$("#incomecoaitems option[value='"+ elemValue + "']").remove();
			  		$("#revenueFromOperationsSelect option[value='"+ elemValue + "']").attr("disabled", "disabled");
			  	}else if(checked == false && elemValue != "multiselect-all") {
				 	$("#incomecoaitems").append('<option value="'+elemValue+'">' +elemText+ '</option>');
				 	$("#revenueFromOperationsSelect option[value='"+ elemValue + "']").removeAttr("disabled");
			  	}
			  	$('#revenueFromOperationsSelect').multiselect('rebuild');
			}else if(expenseSelectArray.indexOf(elemId) !== -1){
			  	if(checked == true && elemValue != "multiselect-all") {
			  		$("#expensecoaitems option[value='"+ elemValue + "']").remove();
			  		for(var i=0; i < expenseSelectArray.length; i++){
			  			var tempSelectID = expenseSelectArray[i];
						if(tempSelectID !== elemId){
			  				$("#"+expenseSelectArray[i]+" option[value='"+ elemValue + "']").attr("disabled", "disabled");
			  				$("#"+ expenseSelectArray[i]).multiselect('rebuild');
			  			}
			  		}
			  	}else if(checked == false && elemValue != "multiselect-all") {
				 	$("#expensecoaitems").append('<option value="'+elemValue+'">' +elemText+ '</option>');
				 	for(var i=0; i < expenseSelectArray.length; i++){
			  			var tempSelectID = expenseSelectArray[i];
						if(tempSelectID !== elemId){
			  				$("#"+ expenseSelectArray[i] +" option[value='"+ elemValue + "']").removeAttr("disabled");
			  				$("#"+ expenseSelectArray[i]).multiselect('rebuild');
			  			}
			  		}
			  	}
			}else if(liabilitiesSelectArray.indexOf(elemId) !== -1){
			  	if(checked == true && elemValue != "multiselect-all") {
			  		$("#liabilitiescoaitems option[value='"+ elemValue + "']").remove();
			  		for(var i=0; i < liabilitiesSelectArray.length; i++){
			  			var tempSelectID = liabilitiesSelectArray[i];
			  			if(tempSelectID !== elemId){
			  				$("#"+liabilitiesSelectArray[i]+" option[value='"+ elemValue + "']").attr("disabled", "disabled");
			  				$("#"+ liabilitiesSelectArray[i]).multiselect('rebuild');
			  			}
			  		}
			  	}else if(checked == false && elemValue != "multiselect-all") {
				 	$("#liabilitiescoaitems").append('<option value="'+elemValue+'">' +elemText+ '</option>');
				 	for(var i=0; i < liabilitiesSelectArray.length; i++){
			  			var tempSelectID = liabilitiesSelectArray[i];
			  			if(tempSelectID !== elemId){
			  				$("#"+ liabilitiesSelectArray[i] + " option[value='"+ elemValue + "']").removeAttr("disabled");
			  				$("#"+ liabilitiesSelectArray[i]).multiselect('rebuild');
			  			}
			  		}
			  	}
			}else if(assetsSelectArray.indexOf(elemId) !== -1){
			  	if(checked == true && elemValue != "multiselect-all") {
			  		$("#assetsscoaitems option[value='"+ elemValue + "']").remove();
			  		for(var i=0; i < assetsSelectArray.length; i++){
			  			var tempSelectID = assetsSelectArray[i];
			  			if(tempSelectID !== elemId){
			  				$("#"+assetsSelectArray[i]+" option[value='"+ elemValue + "']").attr("disabled", "disabled");
			  				$("#"+ assetsSelectArray[i]).multiselect('rebuild');
			  			}
			  		}
			  	}else if(checked == false && elemValue != "multiselect-all") {
				 	$("#assetsscoaitems").append('<option value="'+elemValue+'">' +elemText+ '</option>');
				 	for(var i=0; i < assetsSelectArray.length; i++){
			  			var tempSelectID = assetsSelectArray[i];
			  			if(tempSelectID !== elemId){
			  				$("#"+ assetsSelectArray[i] + " option[value='"+ elemValue + "']").removeAttr("disabled");
			  				$("#"+ assetsSelectArray[i]).multiselect('rebuild');
			  			}
			  		}
			  	}
			}
			$.unblockUI();
	    }
	});
});

//Renders the Mapping - COA page.
function renderPLBSCOAMappings(elem){
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
	$('#profitloss').hide();
	$('#balanceSheet').hide();
	$('#plbsCoaMapping').show();
	if(isPlbsDataReloadNeeded){
		/*$(".multipleCheckSelect option:selected").each(function () {
		    $(this).removeAttr('selected');
		});
		$('.multipleCheckSelect').multiselect('rebuild');*/
		resetSelectOptions();
	    fillCOAItemsForTheUser();
		isPlbsDataReloadNeeded = true;
	}
}

//Function to fetch all COA items and display in the multiselect.
function fillCOAItemsForTheUser(){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	$.ajax({
		url         : "/data/coaPlbsMap",
		data        : JSON.stringify(jsonData),
		type        : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "GET",
		contentType : 'application/json',
		success     : function (data) {
			populateSavedData(data);
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

var populateSavedData = function(coaData){
	
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/plbscoa/fetch";
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
			var revenueFromOperationsList = ""; var index = -1;
			var otherIncomeList = "";
			var mainItemsList = ""; var mainItemsListTemp = "";
			var selectedItemsArray = [];
			var allItemsArray = [];
			$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
			for(var i=0;i<coaData.incomeCOAData.length;i++){
				if(i < data.revenueFromOperations.length){
					index = coaData.incomeCOAData.map(function(d) { return d['id']; }).indexOf(data.revenueFromOperations[i]);
					if(parseInt(index) !== -1){
						revenueFromOperationsList += ('<option value="'+coaData.incomeCOAData[index].id+'" selected>' +coaData.incomeCOAData[index].name+ '</option>');
						otherIncomeList += ('<option value="'+coaData.incomeCOAData[index].id+'" disabled>' +coaData.incomeCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.incomeCOAData[index].id);
					}
				}
				if(i < data.otherIncome.length){
					index = coaData.incomeCOAData.map(function(d) { return d['id']; }).indexOf(data.otherIncome[i]);
					if(parseInt(index) !== -1){
						otherIncomeList += ('<option value="'+coaData.incomeCOAData[index].id+'" selected>' +coaData.incomeCOAData[index].name+ '</option>');
						revenueFromOperationsList += ('<option value="'+coaData.incomeCOAData[index].id+'" disabled>' +coaData.incomeCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.incomeCOAData[index].id);
					}
				}
				allItemsArray[i] = coaData.incomeCOAData[i].id;
				index = -1;
			}
			var myArray = allItemsArray.filter( function( el ) {
			  return selectedItemsArray.indexOf( el ) < 0;
			} );
		
			for (var i=0; i <myArray.length; i++) {
				index = coaData.incomeCOAData.map(function(d) { return d['id']; }).indexOf(myArray[i]);
				if(index != -1){
					mainItemsList += ('<option value="'+coaData.incomeCOAData[index].id+'">' +coaData.incomeCOAData[index].name+ '</option>');
				}
			}
			$("#revenueFromOperationsSelect").append(revenueFromOperationsList + mainItemsList);
			$("#otherIncomeCOASelect").append(otherIncomeList + mainItemsList);
			$("#incomecoaitems").append(mainItemsList);
			$incomeOptionsCache = $("#incomecoaitems > option").clone();

			/********************************Start Expense*********************************************/
			mainItemsList = ""; index = 0;
			while(selectedItemsArray.length > 0) {
			   selectedItemsArray.pop();
			}
			selectedItemsArray.length=0;
			while(allItemsArray.length > 0) {
			   allItemsArray.pop();
			}
			allItemsArray.length=0;
			
			var costOfMaterialsConsumedList = ""; var purchasesOfStockInTradeList = ""; var cIIOfFGWIPAndStockInTradeList = "";
			var empBenefitsExpenseFinCostsList = ""; var financeCostsList = ""; var depreciationAmortizationList="";
			var otherExpensesList = ""; var exceptionalItemsList = ""; var extraordinaryItemsList=""; var currentTaxList =""; var deferredTaxList = ""

			for(var i=0;i<coaData.expenseCOAData.length;i++){
				if(i < data.costOfMaterialsConsumed.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.costOfMaterialsConsumed[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.purchasesOfStockInTrade.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.purchasesOfStockInTrade[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.cIIOfFGWIPAndStockInTrade.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.cIIOfFGWIPAndStockInTrade[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.empBenefitsExpenseFinCosts.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.empBenefitsExpenseFinCosts[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.financeCosts.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.financeCosts[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.depreciationAmortization.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.depreciationAmortization[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.otherExpenses.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.otherExpenses[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.exceptionalItems.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.exceptionalItems[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.extraordinaryItems.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.extraordinaryItems[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.currentTax.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.currentTax[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				if(i < data.deferredTax.length){
					index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(data.deferredTax[i]);
					if(index != -1){
						costOfMaterialsConsumedList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						purchasesOfStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						cIIOfFGWIPAndStockInTradeList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						empBenefitsExpenseFinCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						financeCostsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						depreciationAmortizationList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						otherExpensesList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						exceptionalItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						extraordinaryItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						currentTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" disabled>' +coaData.expenseCOAData[index].name+ '</option>');
						deferredTaxList += ('<option value="'+coaData.expenseCOAData[index].id+'" selected>' +coaData.expenseCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.expenseCOAData[index].id);
					}
				}
				
				allItemsArray[i] = coaData.expenseCOAData[i].id;
				index = -1;
			}
			var myArray = allItemsArray.filter( function( el ) {
			  return selectedItemsArray.indexOf( el ) < 0;
			} );
		
			for (var i=0; i <myArray.length; i++) {
				index = coaData.expenseCOAData.map(function(d) { return d['id']; }).indexOf(myArray[i]);
				if(index != -1){
					mainItemsList += ('<option value="'+coaData.expenseCOAData[index].id+'">' +coaData.expenseCOAData[index].name+ '</option>');
				}
			}
			$("#costOfMaterialsConsumedCOASelect").append(costOfMaterialsConsumedList + mainItemsList);
			$("#purchasesOfStockInTradeCOASelect").append(purchasesOfStockInTradeList + mainItemsList);
			$("#cIIOfFGWIPAndStockInTradeCOASelect").append(cIIOfFGWIPAndStockInTradeList + mainItemsList);
			$("#employeeBenefitsExpenseFinanceCostsCOASelect").append(empBenefitsExpenseFinCostsList + mainItemsList);
			$('#financeCostsCOASelect').append(financeCostsList + mainItemsList);
			$('#depreciationAndAmortizationExpenseCOASelect').append(depreciationAmortizationList+ mainItemsList);
			$('#otherExpensesCOASelect').append(otherExpensesList+ mainItemsList);
			$('#exceptionalItemsCOASelect').append(exceptionalItemsList+ mainItemsList);
			$('#extraordinaryItemsCOASelect').append(extraordinaryItemsList+ mainItemsList);
			$('#currentTaxCOASelect').append(currentTaxList+ mainItemsList);
			$('#deferredTaxCOASelect').append(deferredTaxList+ mainItemsList);
			$("#expensecoaitems").append(mainItemsList);
						
			/********************************Start liabilities*********************************************/
			mainItemsList = ""; index = -1;
			while(selectedItemsArray.length > 0) {
			   selectedItemsArray.pop();
			}
			selectedItemsArray.length=0;
			while(allItemsArray.length > 0) {
			   allItemsArray.pop();
			}
			allItemsArray.length=0;
			var shareCapitalList = ""; var reservesAndSurplusList = ""; var moneyRecAgainstShareWarrantsList = "";
			var shareApplMoneyPendingAllotmentList = ""; var longTermBorrowingsList = ""; var deferredTaxLiabilitiesList="";
			var otherLongTermLiabilitiesList = ""; var longTermProvisionsList = ""; var shortTermBorrowingsList=""; var tradePayablesList =""; 
			var otherCurrentLiabilitiesList = ""; var shortTermProvisionsList=""; 
			for(var i=0;i<coaData.liabilitiesCOAData.length;i++){
				if(i < data.shareCapital.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.shareCapital[i]);
					if(index != -1){
						//console.log("=1= " + coaData.liabilitiesCOAData[index].id + "  =  " + data.shareCapital[i]);
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.reservesAndSurplus.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.reservesAndSurplus[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.moneyRecAgainstShareWarrants.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.moneyRecAgainstShareWarrants[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.shareApplMoneyPendingAllotment.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.shareApplMoneyPendingAllotment[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.longTermBorrowings.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.longTermBorrowings[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.deferredTaxLiabilities.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.deferredTaxLiabilities[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.otherLongTermLiabilities.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.otherLongTermLiabilities[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.longTermProvisions.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.longTermProvisions[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.shortTermBorrowings.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.shortTermBorrowings[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.tradePayables.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.tradePayables[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.otherCurrentLiabilities.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.otherCurrentLiabilities[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				if(i < data.shortTermProvisions.length){
					index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(data.shortTermProvisions[i]);
					if(index != -1){
						shareCapitalList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						reservesAndSurplusList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						moneyRecAgainstShareWarrantsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shareApplMoneyPendingAllotmentList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						deferredTaxLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherLongTermLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						longTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermBorrowingsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						tradePayablesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						otherCurrentLiabilitiesList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" disabled>' +coaData.liabilitiesCOAData[index].name+ '</option>');
						shortTermProvisionsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'" selected>' +coaData.liabilitiesCOAData[index].name+ '</option>');

						selectedItemsArray.push(coaData.liabilitiesCOAData[index].id);
					}
				}
				allItemsArray[i] = coaData.liabilitiesCOAData[i].id;
				index = -1;
			}
			var myArray = allItemsArray.filter( function( el ) {
				return selectedItemsArray.indexOf( el ) < 0;
			} );
		
			for (var i=0; i <myArray.length; i++) {
				index = coaData.liabilitiesCOAData.map(function(d) { return d['id']; }).indexOf(myArray[i]);
				if(index != -1){
					mainItemsList += ('<option value="'+coaData.liabilitiesCOAData[index].id+'">' +coaData.liabilitiesCOAData[index].name+ '</option>');
				}
			}
			$("#shareCapitalCOASelect").append(shareCapitalList + mainItemsList);
			$("#reservesAndSurplusCOASelect").append(reservesAndSurplusList + mainItemsList);
			$("#moneyReceivedAgainstShareWarrantsCOASelect").append(moneyRecAgainstShareWarrantsList + mainItemsList);
			$("#shareApplicationMoneyPendingAllotmentCOASelect").append(shareApplMoneyPendingAllotmentList + mainItemsList);
			$('#longTermBorrowingsCOASelect').append(longTermBorrowingsList + mainItemsList);
			$('#deferredTaxLiabilitiesNetCOASelect').append(deferredTaxLiabilitiesList+ mainItemsList);
			$('#otherLongTermLiabilitiesCOASelect').append(otherLongTermLiabilitiesList+ mainItemsList);
			$('#longTermProvisionsCOASelect').append(longTermProvisionsList+ mainItemsList);
			$('#shortTermBorrowingsCOASelect').append(shortTermBorrowingsList+ mainItemsList);
			$('#tradePayablesCOASelect').append(tradePayablesList+ mainItemsList);
			$('#otherCurrentLiabilitiesCOASelect').append(otherCurrentLiabilitiesList+ mainItemsList);
			$('#shortTermProvisionsCOASelect').append(shortTermProvisionsList+ mainItemsList);
			$("#liabilitiescoaitems").append(mainItemsList);

			/****************************************Start Assets*****************************************/
			mainItemsList = ""; index = 0;
			while(selectedItemsArray.length > 0) {
			   selectedItemsArray.pop();
			}
			selectedItemsArray.length=0;
			while(allItemsArray.length > 0) {
			   allItemsArray.pop();
			}
			allItemsArray.length=0;
			var tangibleAssetsList = ""; var intangibleAssetsList = ""; var capitalWorkInProgressList = ""; var intangibleAssetsUnderDevList = "";
			var nonCurrentInvestmentsList = ""; var deferredTaxAssetsList = ""; var longTermLoansAndAdvancesList = ""; var otherNonCurrentAssetsList = "";
			var currentInvestmentsList = ""; var inventoriesList = ""; var tradeReceivablesList = ""; var cashAndCashEquivalentsList = "";
			var shortTermLoansAndAdvancesList = ""; var otherCurrentAssetsList = "";
			
			for(var i=0;i<coaData.assetsCOAData.length;i++){
				if(i < data.tangibleAssets.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.tangibleAssets[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.intangibleAssets.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.intangibleAssets[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.capitalWorkInProgress.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.capitalWorkInProgress[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.intangibleAssetsUnderDev.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.intangibleAssetsUnderDev[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.nonCurrentInvestments.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.nonCurrentInvestments[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.deferredTaxAssets.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.deferredTaxAssets[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.longTermLoansAndAdvances.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.longTermLoansAndAdvances[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.otherNonCurrentAssets.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.otherNonCurrentAssets[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.currentInvestments.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.currentInvestments[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.inventories.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.inventories[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.tradeReceivables.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.tradeReceivables[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.cashAndCashEquivalents.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.cashAndCashEquivalents[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.shortTermLoansAndAdvances.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.shortTermLoansAndAdvances[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				if(i < data.otherCurrentAssets.length){
					index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(data.otherCurrentAssets[i]);
					if(index != -1){
						tangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						capitalWorkInProgressList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						intangibleAssetsUnderDevList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						nonCurrentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						deferredTaxAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						longTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherNonCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						currentInvestmentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						inventoriesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						tradeReceivablesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						cashAndCashEquivalentsList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						shortTermLoansAndAdvancesList += ('<option value="'+coaData.assetsCOAData[index].id+'" disabled>' +coaData.assetsCOAData[index].name+ '</option>');
						otherCurrentAssetsList += ('<option value="'+coaData.assetsCOAData[index].id+'" selected>' +coaData.assetsCOAData[index].name+ '</option>');
						selectedItemsArray.push(coaData.assetsCOAData[index].id);
					}
				}
				allItemsArray[i] = coaData.assetsCOAData[i].id;
				index = -1;
			}
			var myArray = allItemsArray.filter( function( el ) {
			  	return selectedItemsArray.indexOf( el ) < 0;
			});
		
			for (var i=0; i <myArray.length; i++) {
				index = coaData.assetsCOAData.map(function(d) { return d['id']; }).indexOf(myArray[i]);
				if(index != -1){
					mainItemsList += ('<option value="'+coaData.assetsCOAData[index].id+'">' +coaData.assetsCOAData[index].name+ '</option>');
				}
			}

			$("#tangibleAssetsCOASelect").append(tangibleAssetsList + mainItemsList);
			$("#intangibleAssetsCOASelect").append(intangibleAssetsList + mainItemsList);
			$("#capitalWorkInProgressCOASelect").append(capitalWorkInProgressList + mainItemsList);
			$("#intangibleAssetsUnderDevelopmentCOASelect").append(intangibleAssetsUnderDevList + mainItemsList);
			$("#nonCurrentInvestmentsCOASelect").append(nonCurrentInvestmentsList + mainItemsList);
			$("#deferredTaxAssetsNetCOASelect").append(deferredTaxAssetsList + mainItemsList);
			$("#longTermLoansAndAdvancesCOASelect").append(longTermLoansAndAdvancesList + mainItemsList);
			$("#otherNonCurrentAssetsCOASelect").append(otherNonCurrentAssetsList + mainItemsList);
			$("#currentInvestmentsCOASelect").append(currentInvestmentsList + mainItemsList);
			$("#inventoriesCOASelect").append(inventoriesList + mainItemsList);
			$("#tradeReceivablesCOASelect").append(tradeReceivablesList + mainItemsList);
			$("#cashAndCashEquivalentsCOASelect").append(cashAndCashEquivalentsList + mainItemsList);
			$("#shortTermLoansAndAdvancesCOASelect").append(shortTermLoansAndAdvancesList + mainItemsList);
			$("#otherCurrentAssetsCOASelect").append(otherCurrentAssetsList + mainItemsList);
			$("#assetsscoaitems").append(mainItemsList);
			$('.multipleCheckSelect').multiselect('rebuild');
			while(selectedItemsArray.length > 0) {
			   selectedItemsArray.pop();
			}
			$.unblockUI();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		},
		complete: function(data) {
			//$.unblockUI();
		}
	});
	
}

function savePLBSCOAAssociation(plBsHead, selElem){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
	var coaIds = [];
	$('#' + selElem+ ' :selected').each(function(i, selected){
		coaIds[i] = $(selected).val();
	});

	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.plBsHead = plBsHead;
	jsonData.coaIds = coaIds.join();
	var url="/plbscoa/map";
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
			swal("Profit & Loss and Balance Sheet COA Mapping", "Saved", "success");
			//alert("PL BS COA Mappings saved!");
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		},
		complete: function(data) {
			$.unblockUI();
		}
	});



}//End of function - savePLBSCOAAssociation

var resetSelectOptions = function(){
	$("#revenueFromOperationsSelect").children().remove();
	$("#otherIncomeCOASelect").children().remove();
	$("#incomecoaitems").children().remove();
	$("#costOfMaterialsConsumedCOASelect").children().remove();
	$("#purchasesOfStockInTradeCOASelect").children().remove();
	$("#cIIOfFGWIPAndStockInTradeCOASelect").children().remove();
	$("#employeeBenefitsExpenseFinanceCostsCOASelect").children().remove();
	$("#financeCostsCOASelect").children().remove();
	$("#depreciationAndAmortizationExpenseCOASelect").children().remove();
	$("#otherExpensesCOASelect").children().remove();
	$("#exceptionalItemsCOASelect").children().remove();
	$("#extraordinaryItemsCOASelect").children().remove();
	$("#currentTaxCOASelect").children().remove();
	$("#deferredTaxCOASelect").children().remove();
	$("#profitLossFromDiscontinuingOperationsCOASelect").children().remove();
	$("#taxExpenseOfDiscontinuingOperationsCOASelect").children().remove();
	$("#expensecoaitems").children().remove();
	$("#shareCapitalCOASelect").children().remove();
	$("#reservesAndSurplusCOASelect").children().remove();
	$("#moneyReceivedAgainstShareWarrantsCOASelect").children().remove();
	$("#shareApplicationMoneyPendingAllotmentCOASelect").children().remove();
	$("#longTermBorrowingsCOASelect").children().remove();
	$("#deferredTaxLiabilitiesNetCOASelect").children().remove();
	$("#otherLongTermLiabilitiesCOASelect").children().remove();
	$("#longTermProvisionsCOASelect").children().remove();
	$("#shortTermBorrowingsCOASelect").children().remove();
	$("#tradePayablesCOASelect").children().remove();
	$("#otherCurrentLiabilitiesCOASelect").children().remove();
	$("#shortTermProvisionsCOASelect").children().remove();
	$("#liabilitiescoaitems").children().remove();
	
	$("#tangibleAssetsCOASelect").children().remove();
	$("#intangibleAssetsCOASelect").children().remove();
	$("#capitalWorkInProgressCOASelect").children().remove();
	$("#intangibleAssetsUnderDevelopmentCOASelect").children().remove();
	$("#nonCurrentInvestmentsCOASelect").children().remove();
	$("#deferredTaxAssetsNetCOASelect").children().remove();
	$("#longTermLoansAndAdvancesCOASelect").children().remove();
	$("#otherNonCurrentAssetsCOASelect").children().remove();
	$("#currentInvestmentsCOASelect").children().remove();
	$("#inventoriesCOASelect").children().remove();
	$("#tradeReceivablesCOASelect").children().remove();
	$("#cashAndCashEquivalentsCOASelect").children().remove();
	$("#shortTermLoansAndAdvancesCOASelect").children().remove();
	$("#otherCurrentAssetsCOASelect").children().remove();
	$("#assetsscoaitems").children().remove();
}