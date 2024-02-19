function validateAdvancePayment(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var withholdingelem=$("#"+parentTr+" input[name='pcafcvTaxAdjusted']");
	var withholdingAmount="0.0";
	var enteredAmount=$("#"+parentTr+" input[class='customerAdvance']").val();
	/*if(advanceAdjustment!="" && enteredAmount!=""){
		enteredAmount=parseFloat(enteredAmount)+parseFloat(advanceAdjustment);
	}*/
	$("#"+parentTr+" input[class='txnGross']").val(enteredAmount); //hidden variable, needed when converting table data to array for TransactionItems
	var creditCustomer= $("#pcafcvVendors option:selected").val();
	var textUserTxnPurposeText=$("#whatYouWantToDo").find('option:selected').text();
	//var enteredAmount=$(elem).val();
	var tdsTaxValue = $("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").val();
     $("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").val("");
	var txnPurposeVal=$("#whatYouWantToDo").find('option:selected').val();
	if(enteredAmount!=""){
		if(parentTr.indexOf("pcafcv")!=-1 || parentTr.indexOf("transactionEntity")!=-1){
			var jsonData = {};
			jsonData.useremail=$("#hiddenuseremail").text();
			jsonData.txnSelectedVendorCustomer=$("#pcafcvVendors option:selected").val();  //$("#"+parentTr+" select[name='pcafcvVendors'] option:selected").val();
			jsonData.txnSpecificsId=$("#"+parentTr+" select[name='pcafcvItems'] option:selected").val();
			jsonData.txnGrossAmt=enteredAmount;
			jsonData.userTxnPurposeText=textUserTxnPurposeText;
			jsonData.txnPurposeValue = txnPurposeVal;
			var url="/transaction/calculateNetAmount";
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
				   if(data!=null && data.branchSpecificsTaxComponentPurchaseData.length>0){
					   for(var i=0;i<data.branchSpecificsTaxComponentPurchaseData.length;i++){
						   console.log("When TDS is more than monetory limit");
						   $("#pcafcVendorAdvancePayment").append('Rate: '+data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate+",");
						   $("#pcafcVendorAdvancePayment").append('<br/>Limit: '+data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxLimit+",");
						   $("#pcafcVendorAdvancePayment").append('<br/>Withholding Monetory Limit: '+data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit+",");
						   var withholdingAmount=data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxTotalAmount;
						   $("#pcafcVendorAdvancePayment").append('<br/>Withholding Tax: '+parseFloat(withholdingAmount)+'(-)');
						   $("#"+parentTr+" input[id='pcafcvTaxAdjusted']").val(parseFloat(withholdingAmount.toFixed(2)));
						   $("#"+parentTr+" input[id='pcafcvNetPaid']").val(parseFloat(enteredAmount-withholdingAmount).toFixed(2));
					   }
	                   //$("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val(data.branchSpecificsTaxComponentPurchaseData[i].leftOutTransactionIDList);
				   }else{
					   var netAmount = parseFloat(enteredAmount);
					   if(data.hasOwnProperty('modeOfTdsCompute')) {
						   if(data.modeOfTdsCompute !== 1 && tdsTaxValue !== "" && parseFloat(tdsTaxValue) > 0) {
								netAmount = netAmount - parseFloat(tdsTaxValue);
								$("#"+parentTr+" input[id='pcafcvTaxAdjusted']").val(parseFloat(tdsTaxValue));
							}else {
								 $("#"+parentTr+" input[id='pcafcvTaxAdjusted']").val(0);
							}
					   }

					   $("#"+parentTr+" input[id='pcafcvNetPaid']").val(parseFloat(netAmount).toFixed(2));

					   /*$("#"+parentTr+" input[id='pcafcvTaxAdjusted']").val(parseFloat(0).toFixed(2));
					   $("#"+parentTr+" input[id='pcafcvNetPaid']").val(parseFloat(enteredAmount).toFixed(2));*/
				   }
				   if(data.hasOwnProperty('modeOfTdsCompute')) {
						if(data.modeOfTdsCompute == 1) {
							$("#pcafcvTaxAdjusted").attr('readonly','readonly');
						}else {
							$("#pcafcvTaxAdjusted").removeAttr('readonly');
						}
					}
			   },
			   error: function (xhr, status, error) {
			   		if(xhr.status == 401){ doLogout(); }
			   },
			complete: function(data) {
				calculateNetAmountPaidByTDS();
			}
			});
		}
	}else{
		$("#pcafcVendorAdvancePayment").text("");
		$(".withholdingtaxcomponenetdiv").val("");
		$("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val("");
	}
}

function validatePayment(elem){
	var enteredAmount=$(elem).val();
	var parentTr=$(elem).parent().parent().attr('id');
	var withholdingelem=$("#"+parentTr+" input[name='rcpfccTaxAdjusted']");
	var advanceAdjustment=$("#"+parentTr+" input[class='creditadvanceadjustment']").val();
	var withholdingAmount="0.0";
	/*if(advanceAdjustment!="" && advanceAdjustment!= undefined && enteredAmount!=""){
		enteredAmount=parseFloat(enteredAmount)+parseFloat(advanceAdjustment);
	}*/
	$("#rcpfccTotalDiscountAllowed").val("");
	if(($(elem).val()!="" && !isNaN($(elem).val())) || ($(elem).attr('id') == 'mcpfcvdiscountReceived')){
		if(parentTr.substring(0,10)=="rcpfcctrid"){
			enteredAmount = 0.0;
			//var advanceAdjustment=$("#"+parentTr+" input[class='creditadvanceadjustment']").val();
			if(advanceAdjustment!="" && advanceAdjustment!= undefined && enteredAmount!=""){
				enteredAmount=parseFloat(enteredAmount)+parseFloat(advanceAdjustment);
			}

			if(typeof withholdingelem!='undefined'){
				var amountReceived=$("#"+parentTr+" input[name='rcpfccpaymentreceived']").val();
				var withholdingtaxval=$("#"+parentTr+" input[name='rcpfccTaxAdjusted']").val();
				var discountAllowed = $("#"+parentTr+" input[name='rcpfccdiscountAllowed']").val();
				//var totalDiscountAllowed = "0.0";
				if(amountReceived!=""){
					enteredAmount=parseFloat(enteredAmount)+parseFloat(amountReceived);
				}
				if(withholdingtaxval!=""){
					enteredAmount=parseFloat(enteredAmount)+parseFloat(withholdingtaxval);
				}
				if(discountAllowed != ""){
					enteredAmount=parseFloat(enteredAmount)+parseFloat(discountAllowed);
				}

				var netDiscountTotal=0,netPaymentReceivedTotal=0;netTaxWithheldTotal=0;
				$("#rcpfccSecondTableForMultiInvoice > tbody > tr").each(function() {
					var netDiscount = $(this).find("td input[id='rcpfccdiscountAllowed']").val();
					if(netDiscount!="" && typeof netDiscount!='undefined'){
						netDiscountTotal = netDiscountTotal + parseFloat(netDiscount);
					}
					var netPaymentReceived = $(this).find("td input[id='rcpfccpaymentreceived']").val();
					if(netPaymentReceived!="" && typeof netPaymentReceived!='undefined'){
						netPaymentReceivedTotal = netPaymentReceivedTotal + parseFloat(netPaymentReceived);
					}
					var netTaxWithheld = $(this).find("td input[id='rcpfccTaxAdjusted']").val();
					if(netTaxWithheld!="" && typeof netTaxWithheld!='undefined'){
						netTaxWithheldTotal = netTaxWithheldTotal + parseFloat(netTaxWithheld);
					}
				});
				$("#rcpfccTotalDiscountAllowed").val(netDiscountTotal);
				$("#rcpfcctotalpaymentreceived").val(netPaymentReceivedTotal);
				$("#rcpfccTotalTaxAdjusted").val(netTaxWithheldTotal);
			}
			var amountToBePaid = $("#"+parentTr+" td[class='vendcustoutstandings'] div:nth-child(5)").text();
			var amountToBePaidArr = amountToBePaid.substring(0,amountToBePaid.length).split(':');
			var finalAmount = amountToBePaidArr[1];
			/* Sunil: finalAmount is already after reducing sales return amount so no need below calculation.
			var salesreturn=$("#"+parentTr+" td[class='vendcustoutstandings'] div:nth-child(6)").text();
			var salesreturnArr=salesreturn.substring(0,salesreturn.length).split(':');
			var salesreturnValue=salesreturnArr[1];
			if(typeof salesreturnValue!='undefined'){
				finalAmount=parseFloat(finalAmount)-parseFloat(salesreturnValue);
			} */
			if(typeof finalAmount=='undefined'){
				$(elem).val("");
			}
			var due=0.0;
			if(parseFloat(enteredAmount)>parseFloat(finalAmount)){
				$(".vendorActPayment").html("");
				swal("Error!","Amount cannot be more than outstandings","error");
				$(elem).val("");
				$("#"+parentTr+" input[name='rcpfccTaxAdjusted']").val("");
				$("#"+parentTr+" input[name='rcpfccdiscountAllowed']").val("");
				$("#rcpfccTotalDiscountAllowed").val("");
				$("#rcpfcctotalpaymentreceived").val("");
				$("#rcpfccTotalTaxAdjusted").val("");
				return true;
			}
			if(finalAmount!="" && typeof finalAmount!='undefined'){
				due=parseFloat(finalAmount)-parseFloat(enteredAmount);
				$("#"+parentTr+" input[class='dueBalance']").val((due).toFixed(2));
			}else{
				$("#"+parentTr+" input[class='dueBalance']").val("");
			}
		}else if(parentTr.substring(0,10)=="mcpfcvtrid" || parentTr.indexOf("transactionEntity")!=-1){

			var jsonData = {};
			jsonData.useremail=$("#hiddenuseremail").text();
			jsonData.txnVendorId=$("#"+parentTr+" select[name='mcpfcvVendors'] option:selected").val();
			jsonData.pendingTxnEntityId=$("#"+parentTr+" select[name='mcpfcvpendingInvoices'] option:selected").val();
			/*var url="/transaction/vendorSupplierWithholdingData";
			$.ajax({
			   url: url,
			   data:JSON.stringify(jsonData),
			   type:"text",
			   method:"POST",
			   contentType:'application/json',
			   success: function (data) {
				   /*for(var i=0;i<data.branchSpecificsTaxComponentPurchaseData.length;i++){
					   if(parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit)>0){
						   if(data.branchSpecificsTaxComponentPurchaseData[i].totalVendNetForFinYearForWithholding!="" && data.branchSpecificsTaxComponentPurchaseData[i].totalVendNetForFinYearForWithholding!=null){
							   if(parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].totalVendNetForFinYearForWithholding)>parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit)){
								   withholdingAmount=(parseFloat(withholdingAmount)+((parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate)/parseFloat(100.0))*parseFloat(enteredAmount))).toFixed(2);
							   }
							   if(parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].totalVendNetForFinYearForWithholding)<parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit)){
								   var totalVendNetForFinYearForWithholdingWithCurrentEnteredAmount=parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].totalVendNetForFinYearForWithholding)+parseFloat(enteredAmount);
								   if(parseFloat(totalVendNetForFinYearForWithholdingWithCurrentEnteredAmount)>parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit)){
									   withholdingAmount=(parseFloat(withholdingAmount)+((parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate)/parseFloat(100.0))*parseFloat(enteredAmount))).toFixed(2);
								   }
								   if(parseFloat(enteredAmount)>parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxLimit)){
									   withholdingAmount=(parseFloat(withholdingAmount)+((parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate)/parseFloat(100.0))*parseFloat(enteredAmount))).toFixed(2);
								   }
							   }
						   }else{
							   var totalVendNetForFinYearForWithholdingWithCurrentEnteredAmount=parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].totalVendNetForFinYearForWithholding)+parseFloat(enteredAmount);
							   if(parseFloat(totalVendNetForFinYearForWithholdingWithCurrentEnteredAmount)>parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withHoldingMonetoryLimit)){
								   withholdingAmount=(parseFloat(withholdingAmount)+((parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate)/parseFloat(100.0))*parseFloat(enteredAmount))).toFixed(2);
							   }
						   }
					   }else{
						   withholdingAmount=(parseFloat(withholdingAmount)+((parseFloat(data.branchSpecificsTaxComponentPurchaseData[i].withholdingtaxRate)/parseFloat(100.0))*parseFloat(enteredAmount))).toFixed(2);
					   }
					   if(parseFloat(withholdingAmount)>0){
							var payToVendor=parseFloat(enteredAmount)-parseFloat(withholdingAmount);
							$(".vendorActPayment").html('Pay Vendor:'+parseFloat(payToVendor)+'<br/>,Remit withholding tax:'+parseFloat(withholdingAmount)+'');
						}
					    if(withholdingAmount=="0.0"){
							$(".vendorActPayment").html('Pay Vendor:'+parseFloat(enteredAmount)+'');
						}
				   }*/
					enteredAmount=0.0;
					if(advanceAdjustment!="" && advanceAdjustment!= undefined && enteredAmount!=""){
						enteredAmount=parseFloat(enteredAmount)+parseFloat(advanceAdjustment);
					}
					var amountReceived=$("#"+parentTr+" input[name='mcpfcvpaymentpaid']").val();
					if(amountReceived!=""){
						enteredAmount=parseFloat(enteredAmount)+parseFloat(amountReceived);
					}


				   var discount = $("#"+parentTr+" input[name='mcpfcvdiscountReceived']").val();
					if(discount != ""){
						enteredAmount=parseFloat(enteredAmount)+parseFloat(discount);
					}

					var netDiscountTotal=0,netPaymentPaidTotal=0;
					$("#mcpfcvSecondTableForMultiInvoice > tbody > tr").each(function() {
						var netDiscount = $(this).find("td input[id='mcpfcvdiscountReceived']").val();
						if(netDiscount!="" && typeof netDiscount!='undefined'){
							netDiscountTotal = netDiscountTotal + parseFloat(netDiscount);
						}
						var netPaymentPaid = $(this).find("td input[id='mcpfcvpaymentpaid']").val();
						if(netPaymentPaid!="" && typeof netPaymentPaid!='undefined'){
							netPaymentPaidTotal = netPaymentPaidTotal + parseFloat(netPaymentPaid);
						}
					});
					$("#mcpfcvtotalDiscount").val(netDiscountTotal);
					$("#mcpfcvtotalpaymentPaid").val(netPaymentPaidTotal);

					if(withholdingAmount=="0.0"){
						  $(".vendorActPayment").html('Pay Vendor:'+parseFloat(netPaymentPaidTotal)+'');
					   }

				   var amountToBePaid=$("#"+parentTr+" td[class='vendcustoutstandings'] div:nth-child(5)").text();
				   var amountToBePaidArr=amountToBePaid.substring(0,amountToBePaid.length).split(':');
				   var finalAmount=amountToBePaidArr[1];
				   /* Sunil: finalAount is already after reducing purchase return amount so no need below calculation.
				   var purchasereturn=$("#"+parentTr+" td[class='vendcustoutstandings'] div:nth-child(6)").text();
				   var purchasereturnArr=purchasereturn.substring(0,purchasereturn.length).split(':');
				   var purchasereturnValue=purchasereturnArr[1];
					if(typeof purchasereturnValue!='undefined'){
						finalAmount=parseFloat(finalAmount)-parseFloat(purchasereturnValue);
					}
					*/
					if(typeof finalAmount=='undefined'){
						$(elem).val("");
					}
					let due=0.0;
					if(parseFloat(enteredAmount)>parseFloat(finalAmount)){
						$(".vendorActPayment").html("");
						swal("Error!","Amount cannot be more than outstandings","error");
						$("#"+parentTr+" input[name='mcpfcvpaymentpaid']").val("");
						$("#"+parentTr+" input[name='mcpfcvdiscountReceived']").val("");
						$("#mcpfcvtotalDiscount").val("");
						$("#mcpfcvtotalpaymentPaid").val("");
						return true;
					}
					if(parseFloat(enteredAmount) <= parseFloat(finalAmount)){
						let txnInProgress = $("#"+parentTr+" div[id='mcpfcvtxninprogress']").text();
						if(txnInProgress != ""){
							let txnInProgressAmount=$("#"+parentTr+" div[id='mcpfcvtxninprogress']").text();
							let txnInProgressAmountArr=txnInProgressAmount.substring(0,txnInProgressAmount.length).split(':');
							let amtInProgress = txnInProgressAmountArr[1];

							let txnInProgressDiscountAmount = $("#"+parentTr+" div[id='mcpfcvtxnDiscountInprogress']").text();
							let txnInProgressDiscountAmountArr = txnInProgressDiscountAmount.substring(0,txnInProgressDiscountAmount.length).split(':');
							let discountAmtInProgress = txnInProgressDiscountAmountArr[1];
							if(discountAmtInProgress !== undefined){
								amtInProgress = parseFloat(discountAmtInProgress) + parseFloat(amtInProgress);
							}
							if(parseFloat(enteredAmount)>(parseFloat(finalAmount)-parseFloat(amtInProgress))){
								$(".vendorActPayment").html("");
								swal("Error!","Amount cannot be more than outstandings","error");
								$(elem).val("");
								$("#"+parentTr+" input[name='mcpfcvdiscountReceived']").val("");
								$("#mcpfcvtotalDiscount").val("");
								$("#mcpfcvtotalpaymentPaid").val("");
								return true;
							}
							if(finalAmount!="" && typeof finalAmount!='undefined'){
								due = (parseFloat(finalAmount)-parseFloat(amtInProgress)) - parseFloat(enteredAmount);
								$("#"+parentTr+" input[class='dueBalance']").val((due).toFixed(2));
							}else{
								$(".vendorActPayment").html("");
								$("#"+parentTr+" input[class='dueBalance']").val("");
							}
						}else{
							if(finalAmount!="" && typeof finalAmount!='undefined'){
								due=parseFloat(finalAmount)-parseFloat(enteredAmount);
								$("#"+parentTr+" input[class='dueBalance']").val((due).toFixed(2));
							}else{
								$(".vendorActPayment").html("");
								$("#"+parentTr+" input[class='dueBalance']").val("");
							}
						}
					}
			   /*},
			   error: function (xhr, status, error) {
			   }
			});*/
		}
	}else{
		$("#"+parentTr+" input[class='dueBalance']").val("");
		$(".vendorActPayment").text("");
	}
}

function populatecustvendspecifics(elem){
	 var parentTr = $(elem).closest('tr').attr('id');
	 $(".klBranchSpecfTd").text("");
	 $(".itemParentNameDiv").text("");
	 $(".inventoryItemInStock").text("");
	 $(".customerVendorExistingAdvance").text("");
	 $(".resultantAdvance").text("");

	$("#pcafcvAmountOfAdvanceReceived").val("");
	$("#pcafcVendorAdvancePayment").text("");
    $(".withholdingtaxcomponenetdiv").val("");

	$(".advancePurpose").val("");

	var text=$("#whatYouWantToDo").find('option:selected').text();
	var value=$("#whatYouWantToDo").find('option:selected').val();
	if(parentTr.indexOf("transactionEntity")!=-1){ //For EditTransaction
		text = $("#"+parentTr+" select[id='whatYouWantToDo'] option:selected").text();
		value = $("#"+parentTr+" select[id='whatYouWantToDo'] option:selected").val();
	}
	var custvendid=$("#"+parentTr+" .masterList option:selected").val();
	var txnForUnavailableCustomer=$("#rcafccWalkInCustomers").val();
	var txnBranchID=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var typeOfSupplyForItemType=$("#"+parentTr+" select[id='bocpraTypeOfSupply'] option:selected").val();
	parentTr = $("#" + parentTr).closest('div').attr('id');
	if(value== SELL_ON_CASH_COLLECT_PAYMENT_NOW || value == RECEIVE_ADVANCE_FROM_CUSTOMER){
        if(custvendid == "" || custvendid == null){
            txnForUnavailableCustomer = $("#"+parentTr+" input[name='unAvailableCustomer']").val();
            custvendid = "";
        }else{
            $("#"+parentTr+" input[name='unAvailableCustomer']").val("");
        }
	}else if(value==BUY_ON_CASH_PAY_RIGHT_AWAY || value==BUY_ON_CREDIT_PAY_LATER || value==BUY_ON_PETTY_CASH_ACCOUNT || value==PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER ){
		if(custvendid == "" || custvendid == null){
			txnForUnavailableCustomer = $("#"+parentTr+" input[name='unAvailableVendor']").val();
			custvendid = "";
		}
	}else if(value != SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
        $("#"+parentTr+" input[class='unavailable ui-autocomplete-input']").val("");
	}
	$("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val("");
	$("#"+parentTr+" .masterListItems").children().remove();
	$("#"+parentTr+" .masterListItems").append('<option value="">--Please Select--</option>');

	$("#"+parentTr+" .multipleItemsTable .txnItems").children().remove();
	$("#"+parentTr+" .multipleItemsTable .txnItems").append("<option value=''>--Please Select--</option>");
	resetMultiItemsTableLength(parentTr);
	resetMultiItemsTableFieldsData(elem);

	$("#"+parentTr+" div[class='netAmountDescriptionDisplay']").html("");
	$("#"+parentTr+" input[class='netAmountValTotal']").val("");
	if(txnForUnavailableCustomer==="" && custvendid!=="" && typeof custvendid !== 'undefined'){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.txnPurposeId=value;
		jsonData.txnPurposeText=text;
		jsonData.custVendEntityId=custvendid;
		jsonData.txnBranchID = txnBranchID;
		jsonData.typeOfSupplyForItemType=typeOfSupplyForItemType;
		var url="/transaction/getTxnItemsCustomerVendors";
		$.ajax({
			url: url,
			data:JSON.stringify(jsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			async : false,
		  	method:"POST",
			contentType:'application/json',
			success: function (data) {

				var customerVendorListTemp = "";
				for(var i=0;i<data.txnItemsCustomerVendorsData.length;i++){
					customerVendorListTemp += ('<option value="'+data.txnItemsCustomerVendorsData[i].id+'" category="'+data.txnItemsCustomerVendorsData[i].category+'" id="'+data.txnItemsCustomerVendorsData[i].iseditable+'" combsales="'+data.txnItemsCustomerVendorsData[i].isCombinationSales+'" isTdsSpecific="'+data.txnItemsCustomerVendorsData[i].isTdsSpecific+'">'+data.txnItemsCustomerVendorsData[i].name+'</option>');
				}
				if(value==SELL_ON_CASH_COLLECT_PAYMENT_NOW){
					$("#multipleItemsTablesoccpn > tbody > tr > td > .txnItems").append(customerVendorListTemp);
			    }else if(value== SELL_ON_CREDIT_COLLECT_PAYMENT_LATER){
			    	$("#multipleItemsTablesoccpl > tbody > tr > td > .txnItems").append(customerVendorListTemp);
			    }else if(value==PREPARE_QUOTATION){
			    	$("#multipleItemsTableQuotation > tbody > tr > td > .txnItems").append(customerVendorListTemp);
			    }else if(value==PROFORMA_INVOICE){
			    	$("#multipleItemsTableProforma > tbody > tr > td > .txnItems").append(customerVendorListTemp);
		    	}else if(value==BUY_ON_CASH_PAY_RIGHT_AWAY || value==BUY_ON_CREDIT_PAY_LATER || value==BUY_ON_PETTY_CASH_ACCOUNT){
		    		$("#multipleItemsTablebocpra > tbody > tr > td > .txnItems").append(customerVendorListTemp);
		    	}else if(value==BUY_ON_CREDIT_PAY_LATER){
		    		$("#multipleItemsTablebocapl > tbody > tr > td > .txnItems").append(customerVendorListTemp);
		    	}else if(value==BUY_ON_PETTY_CASH_ACCOUNT){
		    		$("#multipleItemsTablebptyca > tbody > tr > td > .txnItems").append(customerVendorListTemp);
		    	}else if(value==RECEIVE_ADVANCE_FROM_CUSTOMER){
		    		$("#multipleItemsTablercafcc > tbody > tr > td > .txnItems").append(customerVendorListTemp);
		    	}else if(value==PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER){
		    		$("#multipleItemsTablepcafcv > tbody > tr > td > .txnItems").append(customerVendorListTemp);
		    	}else if(value==PURCHASE_ORDER){
		    		$("#multipleItemsTablepurord > tbody > tr > td > .txnItems").append(customerVendorListTemp);
		    	}else if(value == CREDIT_NOTE_VENDOR || value == DEBIT_NOTE_VENDOR){
		    		$("#multipleItemsTablecdtdbv > tbody > tr > td > .txnItems").append(customerVendorListTemp);
		    	}else{
					if(text=="Sales returns"){
						$("#"+parentTr+" .masterListItems").append(customerVendorListTemp);
				  	}else if(text=="Pay advance to vendor or supplier" || text=="Purchase returns"){
						$("#"+parentTr+" .masterListItems").append(customerVendorListTemp);
					}else{
                        $("#"+parentTr+" table[class='multipleItemsTable'] > tbody > tr > td > .txnItems").append(customerVendorListTemp);
					}
			    }
			   initMultiItemsSelect2();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhttp.status == 500){
					swal("Error on fetching transaction items!", "Please retry, if problem persists contact support team", "error");
				}
			},
			complete: function(data) {
				setTimeout(function(){
	    		$.unblockUI(); },2000);
			}
		});
	}else if((value==SELL_ON_CASH_COLLECT_PAYMENT_NOW || value == RECEIVE_ADVANCE_FROM_CUSTOMER) && txnForUnavailableCustomer!="") {
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
        var selectedBranch=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.txnPurposeId=value;
		jsonData.txnPurposeText=text;
		jsonData.custVendEntityId=custvendid;
		jsonData.branchid = selectedBranch;
		var url="/transaction/getAllIncomeItems";
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
			  	if(value == SELL_ON_CASH_COLLECT_PAYMENT_NOW){
					 //("#multipleItemsTablesoccpn tr[id='0'] .txnItems").children().remove();
					 //("#multipleItemsTablesoccpn tr[id='0'] .txnItems").append('<option value="">Please Select</option>');
			    	  for(var i=0;i<data.txnItemsCustomerVendorsData.length;i++){
			    		 $("#multipleItemsTablesoccpn > tbody > tr > td > .txnItems").append('<option value="'+data.txnItemsCustomerVendorsData[i].id+'"category="'+data.txnItemsCustomerVendorsData[i].category+'" combsales="'+data.txnItemsCustomerVendorsData[i].isCombinationSales+'">'+data.txnItemsCustomerVendorsData[i].name+'</option>');
			    	  }
			  	}else if(value == RECEIVE_ADVANCE_FROM_CUSTOMER){
					//$("#multipleItemsTablercafcc .txnItems").children().remove();
					//$("#multipleItemsTablercafcc .txnItems").append('<option value="">--Please Select--</option>');
					for(var i=0;i<data.txnItemsCustomerVendorsData.length;i++){
						$("#multipleItemsTablercafcc > tbody > tr > td > .txnItems").append('<option value="'+data.txnItemsCustomerVendorsData[i].id+'" category="'+data.txnItemsCustomerVendorsData[i].category+'" id="'+data.txnItemsCustomerVendorsData[i].iseditable+'" combsales="'+data.txnItemsCustomerVendorsData[i].isCombinationSales+'" >'+data.txnItemsCustomerVendorsData[i].name+'</option>');
			    	}
			  	}else if(value == TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER){
                    //$("#multipleItemsTablercafcc .txnItems").children().remove();
                    //$("#multipleItemsTablercafcc .txnItems").append('<option value="">--Please Select--</option>');
                    for(var i=0;i<data.txnItemsCustomerVendorsData.length;i++){
                        $("#multipleItemsTabletifbtb > tbody > tr > td > .txnItems").append('<option value="'+data.txnItemsCustomerVendorsData[i].id+'" category="'+data.txnItemsCustomerVendorsData[i].category+'" id="'+data.txnItemsCustomerVendorsData[i].iseditable+'" combsales="'+data.txnItemsCustomerVendorsData[i].isCombinationSales+'" >'+data.txnItemsCustomerVendorsData[i].name+'</option>');
                    }
                }else{
					$("#"+parentTr+" .masterListItems").children().remove();
					$("#"+parentTr+" .masterListItems").append('<option value="">--Please Select--</option>');
					for(var i=0;i<data.txnItemsCustomerVendorsData.length;i++){
						$("#"+parentTr+" .masterListItems").append('<option value="'+data.txnItemsCustomerVendorsData[i].id+'">'+data.txnItemsCustomerVendorsData[i].name+'</option>');
					}
			  	}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}else if(xhttp.status == 500){
					swal("Error on fetching transaction items for visiting customer!", "Please retry, if problem persists contact support team", "error");
				}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
}

function getAdvanceTxnItemParent(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var value=$("#whatYouWantToDo").find('option:selected').val();
	$("#"+parentTr+" div[class='itemParentNameDiv']").text("");

	$("#"+parentTr+" div[class='resultantAdvance']").text("");
	$("#"+parentTr+" input[class='customerAdvance']").val("");
	$("#"+parentTr+" input[class='netPaid']").val("");
	var advanceTxnItem=$(elem).val();
	if(advanceTxnItem == "") {
		return false;
	}
	var custVendId="";
	var brachId="";
	var txnTypeOfSupply = "";
	var txnDestGstinCls= "";
	var destinGstinId = "";
	if(value == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER || value == RECEIVE_ADVANCE_FROM_CUSTOMER){
        var transTableTr = $("#"+parentTr).parent().parent().parent().parent().closest("div").attr('id');
        custVendId = $("#"+transTableTr+" .masterList option:selected").val();
		brachId = $("#"+transTableTr+" .txnBranches option:selected").val();
		txnTypeOfSupply = $("#"+transTableTr+" .txnTypeOfSupply option:selected").val();
		txnDestGstinCls = $("#"+transTableTr+" .txnDestGstinCls option:selected").val();
		destinGstinId = $("#"+transTableTr+" .txnDestGstinCls option:selected").attr('id');
	}else{
		custVendId = $("#"+parentTr+" .masterList option:selected").val();
		$("#"+parentTr+" div[class='customerVendorExistingAdvance']").text("");
		brachId =  $("#"+parentTr+" .txnBranches option:selected").val();
		txnTypeOfSupply =  $("#"+parentTr+" .txnTypeOfSupply option:selected").val();
		txnDestGstinCls =  $("#"+parentTr+" .txnDestGstinCls option:selected").val();
	}

	$(".inventoryItemInStock").text("");

    $("#"+parentTr+" .withholdingtaxcomponenetdiv").text("");
    $("#"+parentTr+" .withholdingtaxcomponenetdiv").val("");
	$("#"+parentTr+" input[class='txnLeftOutWithholdTransIDs']").val("");
	$("#"+parentTr+" .advancePurpose").val("");
	if(advanceTxnItem!=""){
		 var jsonData = {};
		 jsonData.useremail=$("#hiddenuseremail").text();
		 jsonData.txnCustVendId=custVendId;
		 jsonData.txnAdvanceItemId=advanceTxnItem;
		 jsonData.brachId=brachId;
		 jsonData.txnTypeOfSupply=txnTypeOfSupply;
		 jsonData.txnDestGstinCls=txnDestGstinCls;
		 jsonData.destinGstinId = destinGstinId;
		 var url="/transaction/getTxnItemParent";
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
				$("#"+parentTr+" div[class='itemParentNameDiv']").text(data.advanceTxnItemParentData[0].itemParentName);
                if(value != PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
                    $("#" + parentTr + " input[class='customerAdvance']").val(data.advanceTxnItemParentData[0].vendCustExistingAdvance);
                }
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}else if(xhr.status == 500){
					swal("Error on fetching existing advance detail!", "Please retry, if problem persists contact support team", "error");
				}
			},
			complete: function(data) {
				calculateNetAmountPaidByTDS();
			}
		});
	}
}

function showResultantAdvance(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var enteredAdvance=$(elem).val();
	var resultantAdvance = 0;
	if(enteredAdvance != ""){
		var availableAdvance=$("#"+parentTr+" div[class='customerVendorExistingAdvance']").text();
		if($.isNumeric(availableAdvance) && Number(availableAdvance) > 0) {
			resultantAdvance=parseFloat(enteredAdvance)+parseFloat(availableAdvance);
		}
		var resultantAdvance=parseFloat(enteredAdvance)+parseFloat(availableAdvance);
		$("#"+parentTr+" div[class='resultantAdvance']").text(parseFloat(resultantAdvance).toFixed(2));

	}
}

function showResultantAdvanceWithWithheldingTax(elem){
	var parentTr=$(elem).parent().parent().attr('id');
	var advanceReceived=$("#"+parentTr+" input[class='advanceReceived']").val();
	var withholdingelem=$("#"+parentTr+" input[class='withholdingtaxcomponenetCls']").val();
	var resultantAdvance=0;
	var availableAdvance=$("#"+parentTr+" input[class='customerAdvance']").val();

	if(availableAdvance!=""){
		resultantAdvance=resultantAdvance + parseFloat(availableAdvance);
	}
	if(advanceReceived!="" && typeof withholdingelem!='undefined'){
		resultantAdvance=resultantAdvance + parseFloat(advanceReceived);
	}
	if(withholdingelem!="" && typeof withholdingelem!='undefined'){
		resultantAdvance= resultantAdvance +parseFloat(withholdingelem);
	}
	$("#"+parentTr+" div[class='resultantAdvance']").text(resultantAdvance);
    $("#"+parentTr+" input[class='netAmountVal']").val(parseFloat(resultantAdvance).toFixed(2));
    // var netAmountTotal = calculateTotalNetForMultipleItemsTable(this, parentOfparentTr);
	let netAmtTotal=0.0;
	var parentOfparentTr = $("#"+parentTr).parent().parent().parent().parent().closest('div').attr('id');
	$("#" + parentOfparentTr + " .multipleItemsTable > tbody > tr").each(function() {
		let advAmt = $("#"+parentTr+" input[class='advanceReceived']").val();
		let withtax = $("#"+parentTr+" input[class='withholdingtaxcomponenetCls']").val();
		if(advAmt!="" && typeof advAmt!='undefined'){
			netAmtTotal = netAmtTotal + parseFloat(advAmt);
		}
		if(withtax!="" && typeof withtax!='undefined'){
			netAmtTotal = netAmtTotal + parseFloat(withtax);
		}
	});
    $("#"+parentOfparentTr + " input[class='netAmountValTotal']").val(Math.round(netAmtTotal).toFixed(2));
}

function getOutstandings(elem){
	//get even advance for the customerspecifics
	//var parentTr=$(elem).parent().parent().attr('id');
	var parentTr=$(elem).closest('tr').attr('id');
	//var parentOfParentTr=$('#transactionDetailsRfndAmntRcvdTable');
	//alert("parent of parent="+parentOfParentTr);
	$("#"+parentTr+" #rcpfcccustomeradvance").val("");
	$("#"+parentTr+" #mcpfcvvendoradvance").val("");
	$("#"+parentTr+" #rcpfccvendcustoutstandingsgross").text("");
	$("#"+parentTr+" #rcpfccvendcustoutstandingsnet").text("");
	$("#"+parentTr+" #rcpfccvendcustoutstandingsnetdescription").text("");
	$("#"+parentTr+" #rcpfccvendcustoutstandingspaid").text("");
	$("#"+parentTr+" #rcpfccvendcustoutstandingsnotpaid").text("");
	$("#"+parentTr+" #rcpfccvendcustoutstandingssalesreturn").text("");
	$("#"+parentTr+" #rcpfccdiscountAllowed").val("");
	$("#"+parentTr+" #rcpfccTaxAdjusted").val("");
	$("#"+parentTr+" #mcpfcvvendcustoutstandingsgross").text("");
	$("#"+parentTr+" #mcpfcvvendcustoutstandingsnet").text("");
	$("#"+parentTr+" #mcpfcvvendcustoutstandingsnetdescription").text("");
	$("#"+parentTr+" #mcpfcvvendcustoutstandingspaid").text("");
	$("#"+parentTr+" #mcpfcvvendcustoutstandingsnotpaid").text("");
	$("#"+parentTr+" #mcpfcvtxninprogress").text("");
	$("#"+parentTr+" #mcpfcvdiscountAllowed").val("");

	$("#"+parentTr+" .paymentreceivedmade").val("");
	$("#"+parentTr+" .dueBalance").val("")
	$("#"+parentTr+" .vendorActPayment").text("");
    $("#"+parentTr+" input[id='amntAvailForRefund']").val("");
	$("#"+parentTr+" input[id='tdsAvailForRefund']").val("")
    $("#"+parentTr+" input[id='rfndAmountOfReceived']").val("")
    $("#"+parentTr+" input[id='rfndTaxAdjusted']").val("")
	$("#"+parentTr+" input[id='rfndamntResultantAmnt']").val("")
	$("#"+parentTr+" input[id='rfndamntResultantTax']").val("")

	$("#mcpfcvvendcustoutstandingspurchasereturn").text("");
	/*var returnValue = validateSelectedItems(argument);
	if(returnValue === false){
		return false;
	}*/
	var multiItemsTable = $(elem).closest('table').attr('id');
	var parentTr = $(elem).closest('tr').attr('id');
   	var selectedItem = $(elem).val();
	var count=0;

	/*$("#" + multiItemsTable + " > tbody > tr").each(function() {
		var itemId = $(this).find("td .salesExpenseTxns option:selected").val();
		if(selectedItem == itemId){
			count++;
		}

	});

	if(count > 1){
		//$(elem).trigger("mousedown");
		$("#" + parentTr +" span[class='select2-selection__clear']").trigger("mousedown");
		return false;
	}*/
	var returnValue = validateSelectedItems(elem);
	if(returnValue === false){
		return false;
	}
	var whatYouWantToDo=$("#whatYouWantToDo").find('option:selected').text();
	var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
	if(parentTr.indexOf("transactionEntity")!=-1){ //For EditTransaction
		whatYouWantToDo = $("#"+parentTr+" select[id='whatYouWantToDo'] option:selected").text();
		whatYouWantToDoVal = $("#"+parentTr+" select[id='whatYouWantToDo'] option:selected").val();
	}
	var openingBalBillId = "";
	var txnBranchId = "";
	var txnInvoiceRefId= "";
	if(whatYouWantToDoVal==36){
		txnInvoiceRefId=$("#"+parentTr+" select[class='salesExpenseTxns'] option:selected").val();
	}else{
		txnInvoiceRefId=$("#"+parentTr+" select[class='pendingTxns'] option:selected").val();
		if($("#"+parentTr+" select[class='pendingTxns'] option:selected").attr('billId')) {
			openingBalBillId = $("#"+parentTr+" select[class='pendingTxns'] option:selected").attr('billId');
		}
		txnBranchId = $("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	}
	if(whatYouWantToDoVal == 5){
		$("#rcpfcccustomeradvanceType").attr('selectedIndex', 0);
	}else if(whatYouWantToDoVal == 7){
		$("#mcpfcvvendoradvanceType").attr('selectedIndex', 0);
	}
	if(txnInvoiceRefId == -1 ){//Opening balance credit transaction, then disable advance as we don't have specifics info
		if(whatYouWantToDoVal == 5){
			$("#rcpfcccustomeradvanceType").attr("disabled", "disabled");
			$("#rcpfcccustomeradvance").attr("disabled", "disabled");
			$("#rcpfcchowmuchfromadvance").attr("disabled", "disabled");
		}else if(whatYouWantToDoVal == 7){
			$("#mcpfcvvendoradvanceType").attr("disabled", "disabled");
			$("#mcpfcvvendoradvance").attr("disabled", "disabled");
			$("#mcpfcvhowmuchfromadvance").attr("disabled", "disabled");
		}
	}else{
		if(whatYouWantToDoVal == 5){
			$('#rcpfcccustomeradvanceType').removeAttr('disabled');
			$("#rcpfcccustomeradvance").removeAttr('disabled');
			$("#rcpfcchowmuchfromadvance").removeAttr('disabled');
		}else if(whatYouWantToDoVal == 7){
			$('#mcpfcvvendoradvanceType').removeAttr('disabled');
			$("#mcpfcvvendoradvance").removeAttr('disabled');
			$("#mcpfcvhowmuchfromadvance").removeAttr('disabled');
		}
	}
	if(txnInvoiceRefId!=""){
		var vendorCustomerID="";
		if(whatYouWantToDo=="Refund Amount Received Against Invoice"){
			 vendorCustomerID= $("#transactionDetailsRfndAmntRcvdTable .masterList option:selected").val();
			 txnBranchId= $("#transactionDetailsRfndAmntRcvdTable .txnBranches option:selected").val();
		} else if(whatYouWantToDo=="Pay vendor/supplier"){
			vendorCustomerID= $("#transactionDetailsMCPFCVTable .masterList option:selected").val();
			txnBranchId= $("#transactionDetailsMCPFCVTable .txnBranches option:selected").val();
		} else if(whatYouWantToDo == "Receive payment from customer"){
			vendorCustomerID= $("#transactionDetailsRCPFCCTable .masterList option:selected").val();
			txnBranchId= $("#transactionDetailsRCPFCCTable .txnBranches option:selected").val();
		} else{
		    vendorCustomerID = $("#"+parentTr+" .masterList option:selected").val();
		    txnBranchId = $("#"+parentTr+" .txnBranches option:selected").val();
		}
		if(vendorCustomerID == ""){
			swal("Invalid vendor/customer!", "Please provide vendor/customer.", "error");
			enableTransactionButtons();
			return false;
		}
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.txnPurposeId=whatYouWantToDoVal;
		jsonData.txnPurposeText=whatYouWantToDo;
		jsonData.txnEntityId=txnInvoiceRefId;
		jsonData.txnVendCust=vendorCustomerID;
		jsonData.openingBalBillId = openingBalBillId;
		jsonData.txnBranchId = txnBranchId;
		var url="/transaction/getinvoiceOutstandings";
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
			   if(whatYouWantToDo=="Receive payment from customer"){
				   if(data.invoiceOutstandingsData.length > 0) {
					   //$("#rcpfccvendcustoutstandingsgross").text('Invoice Value:'+data.invoiceOutstandingsData[0].gross);
						   $("#" + parentTr +" #rcpfccvendcustoutstandingsnet").text('Net Receivable:'+data.invoiceOutstandingsData[0].net);
						   //$("#rcpfccvendcustoutstandingsnetdescription").text('Net Receivable Result:'+data.invoiceOutstandingsData[0].netDesc);
						   $("#" + parentTr +" #rcpfccvendcustoutstandingspaid").text('Amount Received:'+data.invoiceOutstandingsData[0].amountPaid);
						   $("#" + parentTr +" #rcpfccvendcustoutstandingsnotpaid").text('Amount Receivable:'+data.invoiceOutstandingsData[0].amountNotPaid);
						   $("#" + parentTr +" #rcpfccvendcustoutstandingssalesreturn").text('Sales Return:'+data.invoiceOutstandingsData[0].returns);
						   $("#" + parentTr +" #rcpfcccustomeradvance").val(data.invoiceOutstandingsData[0].custAdvanceMoney);
				   }
			   } else if(whatYouWantToDo=="Pay vendor/supplier"){
				   //$("#"+parentTr+" div[id='mcpfcvvendcustoutstandingsgross']").text('Invoice Value:'+data.invoiceOutstandingsData[0].gross);
				   $("#"+parentTr+" div[id='mcpfcvvendcustoutstandingsnet']").text('Net Payable:'+data.invoiceOutstandingsData[0].net);
				  // $("#"+parentTr+" div[id='mcpfcvvendcustoutstandingsnetdescription']").text('Net Payable Result:'+data.invoiceOutstandingsData[0].netDesc);
				   $("#"+parentTr+" div[id='mcpfcvvendcustoutstandingspaid']").text('Amount Paid:'+data.invoiceOutstandingsData[0].amountPaid);
				   $("#"+parentTr+" div[id='mcpfcvvendcustoutstandingsnotpaid']").text('Amount Payable:'+data.invoiceOutstandingsData[0].amountNotPaid);
				   $("#"+parentTr+" div[id='mcpfcvvendcustoutstandingspurchasereturn']").text('Purchase Return:'+data.invoiceOutstandingsData[0].returns);
				   $("#"+parentTr+" input[id='mcpfcvvendoradvance']").val(data.invoiceOutstandingsData[0].custAdvanceMoney);
				   if(data.invoiceOutstandingsData[0].txnRefFlowInProgress!=null && data.invoiceOutstandingsData[0].txnRefFlowInProgress!=""){
					   $("#"+parentTr+" div[id='mcpfcvtxninprogress']").text(data.invoiceOutstandingsData[0].txnRefFlowInProgress);
				   }
				   if(data.invoiceOutstandingsData[0].txnRefDiscountInProgress !=null && data.invoiceOutstandingsData[0].txnRefDiscountInProgress!=""){
					   $("#"+parentTr+" div[id='mcpfcvtxnDiscountInprogress']").text(data.invoiceOutstandingsData[0].txnRefDiscountInProgress);
				   }
			   } else if(whatYouWantToDo=="Refund Amount Received Against Invoice"){
				   $("#" + parentTr +"  input[class='amtAvailForRefund']").val(data.invoiceOutstandingsData[0].netAmount);
					$("#" + parentTr +"  input[class='tdsAvailForRefund']").val(data.invoiceOutstandingsData[0].withholdingAmount);
			   }
		   	},
		   	error: function (xhr, status, error) {
		   		if(xhr.status == 401){ doLogout();
		   		}else if(xhr.status == 500){
    				swal("Error on fetching outstandings!", "Please retry, if problem persists contact support team", "error");
    			}
		   	},
		   	complete: function(data) {
				$.unblockUI();
		   	}
		});
	}
}

function getOpeningBalCustVendorAdvance(elem){
	var value=$(elem).val();
	var text=$("#whatYouWantToDo").find('option:selected').text();
	var whatYouWantToDoVal=$("#whatYouWantToDo").find('option:selected').val();
	var advanceOption="";
	var creditCustomer="";
	var incomeItem="";
	var parentTr = $(elem).closest('tr').attr('id');
	if(whatYouWantToDoVal == "5"){//receive payment from customer
		advanceOption=$("#"+parentTr+" select[id='rcpfcccustomeradvanceType'] option:selected").val(); //1=opening balance of advance from customer setup screen, 2=advance paid using Receive adv from customer screen
		creditCustomer=$("#"+parentTr+" select[id='rcpfccCustomers'] option:selected").val();
		incomeItem=$("#"+parentTr+" select[id='rcpfccpendingInvoices'] option:selected").val();
		if(advanceOption == ""){
			swal("Data not selected!","Adjustment Type is not selected.","error");
			return false;
		}
		if(creditCustomer == ""){
			swal("Data not selected!","Customer is not selected.","error");
			return false;
		}
		if(incomeItem == ""){
			swal("Data not selected!","Pending Invoice is not selected.","error");
			return false;
		}

	}else if(whatYouWantToDoVal == "7"){//pay vendor
		advanceOption=$("#"+parentTr+" select[id='mcpfcvvendoradvanceType'] option:selected").val();
		creditCustomer=$("#"+parentTr+" select[id='mcpfcvVendors'] option:selected").val();
		incomeItem=$("#"+parentTr+" select[id='mcpfcvpendingInvoices'] option:selected").val();
		if(advanceOption == ""){
			swal("Data not selected!","Adjustment Type is not selected.","error");
			return false;
		}
		if(creditCustomer == ""){
			swal("Data not selected!","Vendor is not selected.","error");
			return false;
		}
		if(incomeItem == ""){
			swal("Data not selected!","Pending Invoice is not selected.","error");
			return false;
		}
	}else if(whatYouWantToDoVal == "3"){//buy on cash
		advanceOption=$("#"+parentTr+" select[id='bocpravendoradvanceType'] option:selected").val();
		creditCustomer=$("#"+parentTr+" select[id='bocpraVendor'] option:selected").val();
		incomeItem=$("#"+parentTr+" select[id='bocpraItem'] option:selected").val();
		if(advanceOption == ""){
			swal("Data not selected!","Adjustment Type is not selected.","error");
			return false;
		}
		if(creditCustomer == ""){
			swal("Data not selected!","Vendor is not selected.","error");
			return false;
		}
		if(incomeItem == ""){
			swal("Data not selected!","Buy item is not selected.","error");
			return false;
		}
	}else if(whatYouWantToDoVal == "4"){//buy on credit
		advanceOption=$("#"+parentTr+" select[id='bocaplvendoradvanceType'] option:selected").val();
		creditCustomer=$("#"+parentTr+" select[id='bocaplVendor'] option:selected").val();
		incomeItem=$("#"+parentTr+" select[id='bocaplItem'] option:selected").val();
		if(advanceOption == ""){
			swal("Data not selected!","Adjustment Type is not selected.","error");
			return false;
		}
		if(creditCustomer == ""){
			swal("Data not selected!","Vendor is not selected.","error");
			return false;
		}
		if(incomeItem == ""){
			swal("Data not selected!","Buy item is not selected.","error");
			return false;
		}
	}

	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });

	var jsonData={};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	jsonData.txnPurposeVal=whatYouWantToDoVal;
	jsonData.creditCustomer = creditCustomer;
	jsonData.incomeItem=incomeItem;
	jsonData.advanceOption = advanceOption;
	var url = "/customer/getOpenignBalAdvCustomer";
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
			   if(whatYouWantToDoVal==5){ //receive adv from cust
				   $("#rcpfcccustomeradvance").val(data.custVendOpeningBalAdvData[0].custVendorAdvance);
			   }else if(whatYouWantToDoVal==7){ //pay vendor
				   $("#mcpfcvvendoradvance").val(data.custVendOpeningBalAdvData[0].custVendorAdvance);
			   }else if(whatYouWantToDoVal==3){ //buy on cash
				   $("#bocpravendoradvance").val(data.custVendOpeningBalAdvData[0].custVendorAdvance);
			   }else if(whatYouWantToDoVal==4){ //buy on credit
				   $("#bocaplvendoradvance").val(data.custVendOpeningBalAdvData[0].custVendorAdvance);
			   }
		   },
		   error: function (xhr, status, error) {
		   		if(xhr.status == 401){ doLogout(); }
		   },
			complete: function(data) {
				$.unblockUI();
			}
		});
}

var VENDOR_4USER_BRANCH_LIST;
var CUSTOMER_4USER_BRANCH_LIST;
//Sell on cash/credit, populate customers based on branch
function getCustomerListForBranch(elem){
	var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	$("#rcpfccvendcustoutstandingsgross").text("");
	$("#rcpfccvendcustoutstandingsnet").text("");
	$("#rcpfccvendcustoutstandingsnetdescription").text("");
	$("#rcpfccvendcustoutstandingspaid").text("");
	$("#rcpfccvendcustoutstandingsnotpaid").text("");
	$("#rcpfccCustomers").children().remove();
	$("#rcpfccCustomers").append('<option value="">--Please Select--</option>');
	$("#rcpfccpendingInvoices").children().remove();
	$("#rcpfccpendingInvoices").append('<option value="">--Please Select--</option>');
	var parentTr = $(elem).closest('tr').attr('id');
	var poType = $("#purchaseOrderCategoryId").find('option:selected').val();
	if(transactionPurposeId == CREATE_PURCHASE_ORDER){
		parentTr = 'crtprrtrid';
		if (poType == "req")
			parentTr = "poareq0";
		else if (poType == "bom")
			parentTr = "poabom0";
	}
	$("#mcpfcvvendcustoutstandingsgross").text("");
	$("#mcpfcvvendcustoutstandingsnet").text("");
	$("#mcpfcvvendcustoutstandingsnetdescription").text("");
	$("#mcpfcvvendcustoutstandingspaid").text("");
	$("#mcpfcvvendcustoutstandingsnotpaid").text("");
	$("#mcpfcvtxninprogress").text("");
	var txnPurposeBranchId=$(elem).val();
	var jsonData = {};

	var text=$("#whatYouWantToDo").find('option:selected').text();
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnPurposeId=transactionPurposeId;
	jsonData.txnPurposeBnchId=txnPurposeBranchId;
	if(txnPurposeBranchId==""){
		populateItemBasedOnWhatYouWantToDo(transactionPurposeId,text);
	}else if(txnPurposeBranchId!=""){
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var url="/customer/getCustomerListForBranch";
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
				$("#" + parentTr +" .masterList").children().remove();
				$("#" + parentTr +" .masterList").append('<option value="">--Please Select--</option>');
				var tempList = "";

                if(transactionPurposeId == BILL_OF_MATERIAL || transactionPurposeId == CREATE_PURCHASE_REQUISITION){
                    var vendorListTemp = "";
                    for (var i = 0; i < data.custListForBranch.length; i++) {
                        var parentDiv = $(elem).closest('div').attr('id');
                        var multiitemTr = $("#" + parentDiv + " .staticsellmultipleitems table tbody tr:last").attr('id');
                        if(data.custListForBranch[i].customerType == '2') {
                            tempList += ('<option value="' + data.custListForBranch[i].customerId + '">' + data.custListForBranch[i].customerName + '</option>');
                        }else{
                            vendorListTemp += ('<option value="' + data.custListForBranch[i].customerId + '">' + data.custListForBranch[i].customerName + '</option>');
                        }
                    }
                    $("#" + multiitemTr +" .masterList").append(vendorListTemp);
                    VENDOR_4USER_BRANCH_LIST = vendorListTemp;
                    CUSTOMER_4USER_BRANCH_LIST = tempList;
                    if(poType == "praso" && transactionPurposeId == CREATE_PURCHASE_REQUISITION){
						$("#" + parentTr +" .masterList").append(tempList);
					}
                }else if(transactionPurposeId == CREATE_PURCHASE_ORDER){
                    for (var i = 0; i < data.custListForBranch.length; i++) {
                        tempList += ('<option value="' + data.custListForBranch[i].customerId + '">' + data.custListForBranch[i].customerName + '</option>');
                    }
					VENDOR_4USER_BRANCH_LIST = tempList;
                }else {
                    for (var i = 0; i < data.custListForBranch.length; i++) {
                        tempList += ('<option value="' + data.custListForBranch[i].customerId + '">' + data.custListForBranch[i].customerName + '</option>');
                    }
                }
				$("#" + parentTr +" .masterList").append(tempList);
				custVendSelect2();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}else if(xhr.status == 500){
	    			swal("Error on fetching customer!", "Please retry, if problem persists contact support team", "error");
	    		}
			},
			complete: function(data) {
				$.unblockUI();
			}
	   	});
  	}
}

var getCustomerGstinList = function(elem){
	var isSingleUserDeploy = $("#isDeploymentSingleUser").html();
	var parentTr = $(elem).closest('tr').attr('id');
	var branchId=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var typeOfSupply=$("#"+parentTr+" select[class='txnTypeOfSupply'] option:selected").val();
	var custvendid=$("#"+parentTr+" .masterList option:selected").val();
	if(custvendid === "" || branchId ===""){
		return false;
	}
    var txnPurpose=$("#whatYouWantToDo").find('option:selected').val();
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnBranchId=branchId;
	jsonData.txnCustomerId=custvendid;
	jsonData.txnPurpose = txnPurpose;
	var url="/customer/customerlocations";
	$.ajax({
    	url: url,
    	data:JSON.stringify(jsonData),
    	type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async: true,
	    method:"POST",
	    contentType:'application/json',
	    success: function (data) {
			$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").children().remove();
			$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").append('<option value=""> Please Select </option>');
			if(typeof data.custGstinList == 'undefined'){
				return false;
			}
			var gstinListTemp = "";
			for(var i=0;i<data.custGstinList.length;i++){
				// gstinListTemp += ('<option  value="'+data.custGstinList[i].customerDetailId+'" id="'+data.custGstinList[i].gstin+'">'+data.custGstinList[i].custLocation+'</option>');
				gstinListTemp += ('<option  value="'+data.custGstinList[i].gstin+'" id="'+data.custGstinList[i].customerDetailId+'">'+data.custGstinList[i].custLocation+'</option>');

			}
			$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").append(gstinListTemp);
			// Single User
			if(isSingleUserDeploy == "true") {
			if(data.custGstinList.length == 1 && typeOfSupply != 3) {
				$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls'] option:last").attr("selected", "selected");
			 	$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").trigger("change");
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
    			swal("Error on fetching GSTIN for Customer!", "Please retry, if problem persists contact support team", "error");
    		}
		},
		complete: function(data) {
			$.unblockUI();
		}
   	});
}

var GLOBAL_GSTIN_LIST_FOR_VENDOR;
var getVendorGstinList = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
	var branchId=$("#"+parentTr+" select[class='txnBranches'] option:selected").val();
	var custvendid=$("#"+parentTr+" .masterList option:selected").val();
	if(custvendid === "" || branchId ===""){
		return false;
	}
	if(transPurposeId == CREATE_PURCHASE_ORDER){
		var poType = $("#purchaseOrderCategoryId").find('option:selected').val();
		if(poType == "npo")
			parentTr = "crtprr0";
		else if(poType == "req")
			parentTr = "poareq0";
		else if(poType == "bom")
			parentTr = "poabom0";
	}
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.txnBranchId=branchId;
	jsonData.txnVendorId=custvendid;
	var url="/vendor/vendorlocations";
	$.ajax({
    	url: url,
    	data:JSON.stringify(jsonData),
    	type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		async: true,
	    method:"POST",
	    contentType:'application/json',
	    success: function (data) {
	    	$(elem).attr('vendorbusinesstype',''+data.vendorBusinessType);
			$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").children().remove();
			$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").append('<option value=""> Please Select </option>');
			var gstinListTemp = "";
			for(var i=0;i<data.custGstinList.length;i++){
				gstinListTemp += ('<option  value="'+data.custGstinList[i].gstin+'" id="'+data.custGstinList[i].vendorDetailId+'">'+data.custGstinList[i].custLocation+'</option>');
			}
			$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").append(gstinListTemp);
            GLOBAL_GSTIN_LIST_FOR_VENDOR = gstinListTemp;
			// Single User
			if(data.custGstinList.length == 1) {
				$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls'] option:last").attr("selected", "selected");
			 	$("#" + parentTr +" select[class='placeOfSply txnDestGstinCls']").trigger("change");
			}

		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
    			swal("Error on fetching GSTIN for Vendor!", "Please retry, if problem persists contact support team", "error");
    		}
		},
		complete: function(data) {
			$.unblockUI();
		}
   	});
}


function showCustCOAGraphs(elem, branchID){
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
	var useremail=$("#hiddenuseremail").text();
	jsonData.email = useremail;
	ajaxCall('/customer/customerSalesMonthWiseItemsData', jsonData, '', '', '', '', 'showCustCOAGraphsSuccess', '', true);
}

function showCustCOAGraphsSuccess(data){
		$("div[class='modal-backdrop fade in']").remove();
		$("#staticcashbankreceivablepayablebranchwisebreakup").modal('hide');
		$('#dGraphPlotCustItems').unbind('jqplotDataClick').empty();
		var reportType = $("#staticcashbankreceivablepayablebranchwisebreakup").data('type');
		var label1 = "";
		var label2 = "";
		if(reportType == 'SALE') {
			$('#dGraphHeaderCustItems').html("ITEMWISE SALES DATA");
			label1 = "Credit sales";
			label2 = "Cash sales";
		}else if(reportType == 'PURCHASE') {
			$('#dGraphHeaderCustItems').html("ITEMWISE PURCHASE DATA");
			label1 = "Credit Purchases";
			label2 = "Cash Purchases";
		}else {
 			$('#dGraphHeaderCustItems').html("ITEMWISE SALES/PURCHASE DATA");
			label1 = "Credit Sales/Purchase";
			label2 = "Cash Sales/Purchase";

		}
		$("div[class='modal-backdrop fade in']").remove();
		$("#dGraphPlotCustItemsDiv").attr('data-toggle', 'modal');
	    $("#dGraphPlotCustItemsDiv").modal('show');
		var res=data.custMonthwiseItemsData;
		var resCash = data.custMonthwiseCashSalesItemsData;
		if(!isEmpty(res) && res.length > 0){
			var ticks=["Jan","Feb","Mar","Apr","May","June","July","Aug","Sept","Oct","Nov","Dec"];

			var titleSpecName="";
			setTimeout(function(){
				for(var i in res){
					if(!isEmpty(res[i])){
						var values=[];
						var cashSales=[];
						titleSpecName = res[i].specificsName;
						values.push(res[i].Jan);
						values.push(res[i].Feb);
						values.push(res[i].Mar);
						values.push(res[i].Apr);
						values.push(res[i].May);
						values.push(res[i].Jun);
						values.push(res[i].Jul);
						values.push(res[i].Aug);
						values.push(res[i].Sep);
						values.push(res[i].Oct);
						values.push(res[i].Nov);
						values.push(res[i].Dec);

						if(!isEmpty(resCash) && resCash.length > 0){
							for(var j=0;j<resCash.length;j++){
								if(titleSpecName == resCash[j].specificsName){
									cashSales.push(resCash[j].Jan);
									cashSales.push(resCash[j].Feb);
									cashSales.push(resCash[j].Mar);
									cashSales.push(resCash[j].Apr);
									cashSales.push(resCash[j].May);
									cashSales.push(resCash[j].Jun);
									cashSales.push(resCash[j].Jul);
									cashSales.push(resCash[j].Aug);
									cashSales.push(resCash[j].Sep);
									cashSales.push(resCash[j].Oct);
									cashSales.push(resCash[j].Nov);
									cashSales.push(resCash[j].Dec);
								}
							}
						}

				    	$('#dGraphPlotCustItems').append('<div id=graph'+i+' sytle="height:100px;"></div>');
				    	var plot1 = $.jqplot('graph'+i, [values,cashSales], {
				    		title: "Item " + titleSpecName,

				            animate: !$.jqplot.use_excanvas,
				            seriesDefaults:{
				                pointLabels: { show: true },
				                rendererOptions: {fillToZero: true, varyBarColor: true},
				            },
				            // Custom labels for the series are specified with the "label"
				            series:[
				                {label:label1},
				                {label:label2}
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
				                }
				            },
				            legend: {
					    	      show: true
					    	},
				            highlighter: { show: true }
				        });

				    	$('#graph'+i).bind('resize', function(event, ui) {
				    		var w = parseInt($(".jqplot-yaxis").width(), 10) + parseInt($("#graph"+i).width(), 10);
							var h = parseInt($(".jqplot-title").height(), 10) + parseInt($(".jqplot-xaxis").height(), 10) + parseInt($("#graph"+i).height(), 10);
							$("#graph"+i).width(w).height(h);
							plot.replot( { resetAxes: true });
				        });
				}
			}
		},2000);
	}else{
		swal("Error!","No data to plot graph!","error");
	}

}

function calculateNetAmountPaidByTDS() {
		var total = 0;
		var table= $("#staticmultipleitemspcafcv").find("table");
		table.find(".netPaid").each(function() {
			var netTotalOfTds = $(this).val();
			if(netTotalOfTds != "" && $.isNumeric(netTotalOfTds)) {
				total += Number(netTotalOfTds);
			}
		});
		$("#pcafnetAmountPaid").val(parseFloat(total));
}

var changeMultipleItemTableForPayAdvToVendor = function(transPurposeId,txnTypeOfSupply,parentId,elem,transactionTableTr) {

	var taxTD = '<select class="txnGstTaxRate" id="bocpraTaxRate" onChange="calculateNetAmount(this);"><option value="">Select</option></select>';
	var AmtTD = '<div id="taxCell"><div id="taxCell0"></div><div id="taxCell1"></div><div id="taxCell2"></div></div><input class="txnTaxTypes" placeholder="Taxes" type="hidden" name="bocpraInTaxDetails" id="bocpraInTaxDetails" readonly="readonly">';
	var cessTD = '<select class="txnCessRate" id="bocpraCessRate"  onChange="calculateNetAmount(this);"><option value=""> Select</option></select>';
	var cessAmtTD = '<input type="text" class="txnCessTaxAmt" id="bocpraCessAmt" readonly="readonly"/><input type="hidden" class="itemTaxAmount" name="bocpraTotaltaxamnt" id="bocpraTotaltaxamnt" readonly="readonly" placeholder="Tax Amount"/><input type="hidden" class="actualbudgetDisplayVal" id="bocpraactualbudgetDis" name="bocpraactualbudgetDis" value="0"/><input type="hidden" class="budgetDisplayVal" id="bocprabudgetDisplay" name="bocprabudgetDisplay" value="0"/><input type="hidden" class="amountRangeFromLimit" id="bocpraAmountRangeFromLimit" value="0"/><input type="hidden" class="amountRangeToLimit" id="bocpraAmountRangeToLimit" value="0"/>';
	if(transPurposeId == PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
		if(txnTypeOfSupply == "2" || txnTypeOfSupply == "3") {

			$("#"+parentId +" .multipleItemsTable").find('.rcmTaxRateThLast').removeClass('hidden');
			$("#"+parentId +" .multipleItemsTable").find('.rcmTaxAmountThLast').removeClass('hidden');
			//cess
			$("#"+parentId +" .multipleItemsTable").find('.rcmCessRateThLast').removeClass('hidden');
			$("#"+parentId +" .multipleItemsTable").find('.rcmCessAmountThLast').removeClass('hidden');
			$("#"+parentId).find('.multipleItemsTable tr').each(function(){
				 $(this).find('.taxRateTdLast').html('<input class="txnGstTaxRate" type="text" readonly id="bocpraTaxRate" onChange="calculateNetAmount(this);">');
				  $(this).find('.taxAmountTdLast').html(AmtTD);
				   $(this).find('.taxRateTdLast').removeClass('hidden');
				  $(this).find('.taxAmountTdLast').removeClass('hidden');

				  // cess
				 $(this).find('.cessRateTdLast').html('<input class="txnCessRate" id="bocpraCessRate" type="text" readonly onChange="calculateNetAmount(this);">');
				  $(this).find('.cessAmountTdLast').html(cessAmtTD);
				   $(this).find('.cessRateTdLast').removeClass('hidden');
				  $(this).find('.cessAmountTdLast').removeClass('hidden');
			});

		}else {
			$("#"+parentId +" .multipleItemsTable").find('.rcmTaxRateThLast').addClass('hidden');
			$("#"+parentId +" .multipleItemsTable").find('.rcmTaxAmountThLast').addClass('hidden');
			// cess
			$("#"+parentId +" .multipleItemsTable").find('.rcmCessRateThLast').addClass('hidden');
			$("#"+parentId +" .multipleItemsTable").find('.rcmCessAmountThLast').addClass('hidden');
			$("#"+parentId).find('.multipleItemsTable tr').each(function(){
				  $(this).find('.taxRateTdLast').html("");
				  $(this).find('.taxAmountTdLast').html("");
				   $(this).find('.taxRateTdLast').addClass('hidden');
				  $(this).find('.taxAmountTdLast').addClass('hidden');
				  // cess
				  $(this).find('.cessRateTdLast').html("");
				  $(this).find('.cessAmountTdLast').html("");
				   $(this).find('.cessRateTdLast').addClass('hidden');
				  $(this).find('.cessAmountTdLast').addClass('hidden');
			});
			//$("#"+parentId +" .multipleItemsTable tbody tr").remove();

		}
	}
	fetchBranchInTaxesCess($("#"+transactionTableTr+" select[class='txnBranches']"));

}

function getCustomerListForBranchAndTypeOfSuply(elem){
	var typeOfSupply=$(elem).val();
	if(typeOfSupply != "" && (typeOfSupply == 2 || typeOfSupply == 3)) {
			var parentTr = $(elem).closest('tr').attr('id');
			var txnPurposeBranchId=$("#" + parentTr +" select[class='txnBranches']").val();
			var typeOfSupply=$(elem).val();
			var jsonData = {};
			var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
			var text=$("#whatYouWantToDo").find('option:selected').text();
			jsonData.useremail=$("#hiddenuseremail").text();
			jsonData.txnPurposeId=transactionPurposeId;
			jsonData.txnPurposeBnchId=txnPurposeBranchId;
			jsonData.typeOfSupply=typeOfSupply;

			if(txnPurposeBranchId==""){
				swal("Invalid Data", "Please Select Branch First!", "error");
				return false;
			}
				$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
				var url="/customer/getCustomerListForBranchAndTypeOfSuply";
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
						$("#" + parentTr +" .masterList").children().remove();
						$("#" + parentTr +" .masterList").append('<option value="">--Please Select--</option>');
						var customerVendorListTemp = "";
						for(var i=0;i<data.custListForBranch.length;i++){
							customerVendorListTemp += ('<option value="'+data.custListForBranch[i].customerId+'">'+data.custListForBranch[i].customerName+'</option>');
						}
						$("#" + parentTr +" .masterList").append(customerVendorListTemp);
						custVendSelect2();
					},
					error: function (xhr, status, error) {
						if(xhr.status == 401){
							doLogout();
						}else if(xhr.status == 500){
			    			swal("Error on fetching customer!", "Please retry, if problem persists contact support team", "error");
			    		}
					},
					complete: function(data) {
						$.unblockUI();
					}
			   	});
	}
}

$(document).ready(function() {
	$(".addnewItemForMultiInvTransaction").click(function(){
		var transPurposeId=$("#whatYouWantToDo").find('option:selected').val();
		var mainTableID = $(this).closest('div').attr('id');
		var parentTr = $("#"+mainTableID +" .secondTableForMultiInvoice tbody tr").attr('id');
		transPurposeId = parseInt(transPurposeId);

		var txnPurposeText=$("#whatYouWantToDo").find('option:selected').text();
		var vendorcustomer=$("#"+mainTableID+" tbody tr .masterList option:selected").val();
		//var txnForUnavailableCustomer=$("#soccpnUnAvailableCustomer").val();
		/*var txnForUnavailableCustomer = "";
		if(transPurposeId === SELL_ON_CASH_COLLECT_PAYMENT_NOW || transPurposeId === RECEIVE_ADVANCE_FROM_CUSTOMER || transPurposeId === BUY_ON_CASH_PAY_RIGHT_AWAY || transPurposeId === BUY_ON_CREDIT_PAY_LATER || transPurposeId === BUY_ON_PETTY_CASH_ACCOUNT){
			txnForUnavailableCustomer=$("#"+parentTr+" input[class='unavailable ui-autocomplete-input']").val();
		}else{
			txnForUnavailableCustomer=$("#"+parentTr+" input[class='unavailable']").val();
		}*/
		if(vendorcustomer==""){
			swal("Incomplete transaction data!", "Before adding item, please choose the customer/vendor from dropdown", "error");
			return false;
		}
		var branchId=$("#"+mainTableID+" tbody tr select[class='txnBranches'] option:selected").val();
		var txnDate = $("#"+mainTableID).find(".txnBackDate").val();
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		if(vendorcustomer!="" && branchId!=""){
			var jsonData = {};
			jsonData.useremail=$("#hiddenuseremail").text();
			jsonData.txnPurposeId=transPurposeId;
			jsonData.txnPurposeText=txnPurposeText;
			jsonData.selectedCustVend=vendorcustomer;
			jsonData.txnBranchId=branchId;
			jsonData.txnDate = txnDate;
			var url="/transaction/getCustVendPendingInvoices";
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
					if(transPurposeId == 5){
						var transactionListTemp = "";
						for(var i=0;i<data.allCustomerVendorsPendingInvoicesData.length;i++){
							transactionListTemp+='<option value="'+data.allCustomerVendorsPendingInvoicesData[i].id+'" billId="'+data.allCustomerVendorsPendingInvoicesData[i].opId+'">'+data.allCustomerVendorsPendingInvoicesData[i].dateNetAmount+'</option>';
						}
	                    var currentTr = $("#rcpfccSecondTableForMultiInvoice tbody tr:last").attr('id');
	                    var length = currentTr.substring(10, currentTr.length);
	                    length = parseInt(length) + 1;

	                    var multiItemsTableTr = '<tr id="rcpfcctrid'+length+'"><td><select class="pendingTxns" name="rcpfccpendingInvoices" id="rcpfccpendingInvoices" onchange="getOutstandings(this);"><option value="">--Please Select--</option></select>';
	                    multiItemsTableTr+= '<br/>Knowledge Library<br/><div class="klBranchSpecfTd" style="max-height: 100px; overflow: auto; width:160px;"></div></td>';
	                    multiItemsTableTr+= '<td class="vendcustoutstandings"><div style="max-height: 150px; overflow: auto;"><div id="rcpfccvendcustoutstandingsgross" style="width:140px;"></div><div id="rcpfccvendcustoutstandingsnet" style="width:140px;"></div><div id="rcpfccvendcustoutstandingsnetdescription" style="width:140px;"></div><div id="rcpfccvendcustoutstandingspaid" style="width:140px;"></div><div id="rcpfccvendcustoutstandingsnotpaid" style="width:140px;"></div><div id="rcpfccvendcustoutstandingssalesreturn" style="width:140px;"></div></div></td>';
	                    multiItemsTableTr+= '<td class="actualPayment"><input type="text" placeholder="Amount Received" class="paymentreceivedmade" name="rcpfccpaymentreceived" id="rcpfccpaymentreceived" onkeypress="return onlyDotsAndNumbers(event)" onblur="validatePayment(this);" onkeyup="validatePayment(this);"></td>';
	                    multiItemsTableTr+= '<td><input type="text" class="txnTaxAdjustedCls" name="rcpfccTaxAdjusted" placeholder="Tax Withheld" id="rcpfccTaxAdjusted" onkeypress="return onlyDotsAndNumbers(event)" onblur="validatePayment(this);" onkeyup="validatePayment(this);" ></td>';
	                    multiItemsTableTr+= '<td><input type="text" class="discountAllowedCls" name="rcpfccdiscountAllowed" placeholder="Discount Allowed" id="rcpfccdiscountAllowed" onkeypress="return onlyDotsAndNumbers(event)" onblur="validatePayment(this);" onkeyup="validatePayment(this);"></td>';
	                    multiItemsTableTr+= '<td><input placeholder="Due balance" class="dueBalance" type="text" name="rcpfccduebalance" id="rcpfccduebalance" onkeypress="return onlyDotsAndNumbers(event)" readonly="readonly"></td>';
	                    multiItemsTableTr+= '<td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';
	                    $("#rcpfccSecondTableForMultiInvoice tbody").append(multiItemsTableTr);
				    	$("#rcpfccSecondTableForMultiInvoice > tbody > tr[id='rcpfcctrid"+length+"'] > td > .pendingTxns").append(transactionListTemp);
					} else if(transPurposeId == 7){
						var transactionListTemp = "";
						for(var i=0;i<data.allCustomerVendorsPendingInvoicesData.length;i++){
							transactionListTemp+='<option value="'+data.allCustomerVendorsPendingInvoicesData[i].id+'" billId="'+data.allCustomerVendorsPendingInvoicesData[i].opId+'">'+data.allCustomerVendorsPendingInvoicesData[i].dateNetAmount+'</option>';
						}
	                    var currentTr = $("#mcpfcvSecondTableForMultiInvoice tbody tr:last").attr('id');
	                    var length = currentTr.substring(10, currentTr.length);
	                    length = parseInt(length) + 1;

	                    var multiItemsTableTr = '<tr id="mcpfcvtrid'+length+'"><td><select class="pendingTxns" style="margin-bottom: 10px;" name="mcpfcvpendingInvoices" id="mcpfcvpendingInvoices" onchange="getOutstandings(this);"><option value="">--Please Select--</option></select><br/>';
	                    multiItemsTableTr+= '<br/>Knowledge Library<br/><div class="klBranchSpecfTd" style="max-height: 100px; overflow: auto; width:160px;"></div></td>';
	                    multiItemsTableTr+= '<td class="vendcustoutstandings"><div style="max-height: 155px;overflow: auto;"><div id="mcpfcvvendcustoutstandingsgross" style="width:160px;"></div><div id="mcpfcvvendcustoutstandingsnet" style="width:160px;"></div><div id="mcpfcvvendcustoutstandingsnetdescription" style="width:160px;"></div><div id="mcpfcvvendcustoutstandingspaid" style="width:160px;"></div><div id="mcpfcvvendcustoutstandingsnotpaid" style="width:160px;"></div><div id="mcpfcvvendcustoutstandingspurchasereturn" style="width:160px;"></div><div id="mcpfcvtxninprogress" style="width:160px;"></div></div></td>';
	                    multiItemsTableTr+= '<td class="actualPayment"><input type="text" placeholder="Amount You Wish To Pay" class="paymentmade" name="mcpfcvpaymentpaid" id="mcpfcvpaymentpaid" onkeypress="return onlyDotsAndNumbers(event)" onblur="validatePayment(this);" onkeyup="validatePayment(this);"></td>';
	                    multiItemsTableTr+= '<td><input type="text" class="discountReceivedCls" name="mcpfcvdiscountReceived" placeholder="Discount Received" id="mcpfcvdiscountReceived" onkeypress="return onlyDotsAndNumbers(event)" onblur="validatePayment(this);" onkeyup="validatePayment(this);"></td>';
	                    multiItemsTableTr+= '<td><input placeholder="Due balance" class="dueBalance" type="text" name="mcpfcvduebalance" id="mcpfcvduebalance" onkeypress="return onlyDotsAndNumbers(event)" readonly="readonly"></td>';
	                    multiItemsTableTr+= '<td><input class="removeTxnCheckBox" type="checkbox" /></td></tr>';
	                    $("#mcpfcvSecondTableForMultiInvoice tbody").append(multiItemsTableTr);
				    	$("#mcpfcvSecondTableForMultiInvoice > tbody > tr[id='mcpfcvtrid"+length+"'] > td > .pendingTxns").append(transactionListTemp);
					}
				},
				    error: function (xhr, status, error) {
				      	if(xhr.status == 401){ doLogout();
					      }else if(xhr.status == 500){
				    		swal("Error on fetching Pending Invoices!", "Please retry, if problem persists contact support team", "error");
				    	}
					},
					complete: function(data) {
						$.unblockUI();
					}
				});
		}
	});
});

function checkIsTdsConfigured(elem) {
	var parentTr=$(elem).parent().parent().attr('id');
	var istdsspecific = $("#"+parentTr+" .txnItems").find('option:selected').attr("istdsspecific");
	if(istdsspecific == "false") {
		$("#"+parentTr+" input[class='withholdingtaxcomponenetdiv']").val("");
		swal("Invalid Data", "Please Configure TDS setup for selected Item!", "error");
		return false;
	}
	return true;
}



