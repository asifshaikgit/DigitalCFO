
$(function(){
	$("#rcmApplicableDate").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
});

function getChildRcmChartOfAccount(elem, gstCountryCode){
	var listDisplayableLi=$(elem).parent().parent('li:first').attr('id');
	var origEntityId=listDisplayableLi.substring(15, listDisplayableLi.length);	
	$(elem).attr('src',"/assets/images/minus.png");
	$(elem).attr('onclick',"javascript:removeChildExpenceRcmChartOfAccount(this,'"+gstCountryCode+"')");
	var useremail=$("#hiddenuseremail").text();
	var jsonData = {};
	jsonData.usermail = useremail;
	jsonData.branchId = origEntityId;
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$.ajax({
		url : '/data/getcoaExpenceitemsWithTaxRules',
		data : JSON.stringify(jsonData),
		type : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method : "POST",
		contentType : 'application/json',
		success : function(data) {
			if(data.expenceItemData.length>0){
				$("li[id='"+listDisplayableLi+"']").append('<ul id="mainBranchExpenceChartOfAccount" class="treeview-black mainBranchExpenceChartOfAccount"></ul>');
				var expenceList = "";
				for(var i=0;i<data.expenceItemData.length;i++){
					if(data.expenceItemData[i].isTaxSetup=="Yes"){						
						expenceList += ('<li><div class="chartOfAccountContainer"><p class="color-grey"><img src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><b style="margin-left:2px;color:red;">'+data.expenceItemData[i].name+'</b><button style="float:right" id="newTaxRuleform-container" class="newEntityCreateButton btn btn-submit btn-idos" title="Create/Update Tax Rule"');
					}else{
						expenceList += ('<li><div class="chartOfAccountContainer"><p class="color-grey"><img src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><b style="margin-left:2px;">'+data.expenceItemData[i].name+'</b><button style="float:right" id="newTaxRuleform-container" class="newEntityCreateButton btn btn-submit btn-idos" title="Create/Update Tax Rule"');
					}
					if(gstCountryCode !== ""){
						expenceList += (' onclick="createRcmTaxRule(this,'+origEntityId+','+data.expenceItemData[i].id+',3);"><i class="fa fa-plus pr-5"></i>Add/Update RCM Tax Rule</button></p></li>');
					}else{
						expenceList += (' onclick="createTaxRule(this,'+origEntityId+','+data.expenceItemData[i].id+');"><i class="fa fa-plus pr-5"></i>Add/Update RCM Tax Rule</button></p></li>');
					}
					
				}
				
				$("li[id='"+listDisplayableLi+"']").find("ul[id='mainBranchExpenceChartOfAccount']").append(expenceList);
			}
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){ 
				doLogout(); 
			}else if(xhr.status == 500){
	    		swal("Error on fetching Income Items!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

function removeChildExpenceRcmChartOfAccount(elem, gstCountryCode){
	var listDisplayableLi=$(elem).parent().parent('li:first').attr('id');
	$("li[id='"+listDisplayableLi+"']").find("ul[id='mainBranchExpenceChartOfAccount']").remove();
	$(elem).attr('src',"/assets/images/new.v1370889834.png");
	$(elem).attr('onclick',"javascript:getChildRcmChartOfAccount(this,'"+gstCountryCode+"')");
}

var createRcmTaxRule = function(elem,branchId,specificsId, taxCategory){
	var useremail=$("#hiddenuseremail").text();
	var jsonData = {};
	jsonData.usermail = useremail;
	jsonData.branchPrimaryId = branchId;
	jsonData.specificsPrimId=specificsId;
	jsonData.taxCategory = taxCategory;
	var url = "/tax/itemTaxDetail";
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
			$("#itemDisplay").text("");
			$("#bnchHidden").val(branchId);
			$("#specfHidden").val(specificsId);
			$("#taxCategoryHidden").val(taxCategory);
			$("#rcmRuleDetailsTable tbody").html("");
			var gstTaxRate = data.rate;
			if(data){
				if(data.rate == "" || data.rate == null){
					swal("Error insuffient data!", "Tax rate can not be empty, please define from COA.", "error");
					return false;
				}
				$("#rcmTaxRuleDetailsTable input[id='itemCategory'").val(data.itemCategory);
				$("#rcmTaxRuleDetailsTable input[id='itemHsnSac'").val(data.hsnSacCode);
				$("#rcmTaxRuleDetailsTable input[id='itemTaxRate'").val(data.rate);
				$("#rcmTaxRuleDetailsTable input[id='itemCessTaxRate'").val(data.cessRate);
				$("#rcmTaxRuleDetailsTable #itemNameLbl").empty();
				$("#rcmTaxRuleDetailsTable #itemNameLbl").append(data.specificsname);
				$("#rcmTaxRuleDetailsTable tr[id='1'] input[id='itemgstrate'").val(data.sgstRate);
				$("#rcmTaxRuleDetailsTable tr[id='2'] input[id='itemgstrate'").val(data.cgstRate);
				$("#rcmTaxRuleDetailsTable tr[id='3'] input[id='itemgstrate'").val(data.igstRate);
				$("#rcmTaxRuleDetailsTable tr[id='4'] input[id='itemgstrate'").val(data.cessRate);

				if(data.allowAddNew === 1){
					$(".newRcmTaxRuleform-container").slideDown('slow');
				}
				if(data.rcmApplicableDate != "") {
					$("#rcmApplicableDate").datepicker('destroy');
					$("#rcmApplicableDate").datepicker({
						changeMonth : true,
						changeYear : true,
						dateFormat:  'M d,yy',
						onSelect: function(x,y){
					        $(this).focus();
					    }
					});
					$("#rcmApplicableDate").datepicker("option", "minDate", data.rcmApplicableDate);
				}
				var tableTr = "<tbody>";
				$("#rcmRuleListTable tbody").empty();
				for(var i=0; i < data.itemTaxList.length; i++){
					if(typeof data.itemTaxList[i].gstItemCategory !== 'undefined'){
						tableTr += ("<tr><td>"+data.itemTaxList[i].itemName+"</td>");
						tableTr += ("<td>"+data.itemTaxList[i].gstItemCategory+"</td>");
						tableTr += ("<td>"+data.itemTaxList[i].gstItemCode+"</td>");
						tableTr += ("<td>"+data.itemTaxList[i].gstTaxRate+"</td>");
					}else{
						tableTr += ("<tr><td></td><td></td><td></td><td></td>");
					}
					for(var j=0; j < data.itemTaxList[i].branchTaxList.length; j++){
						tableTr += ("<td>"+data.itemTaxList[i].branchTaxList[j].taxName+"</td>");
						tableTr += ("<td>"+data.itemTaxList[i].branchTaxList[j].date+"</td>");
					}
				}
				tableTr += ("</tr></tbody>");
				$("#rcmRuleListTable").append(tableTr);
				if(data.itemTaxList.length > 0){
					$("#rcmtaxList-form-container").slideDown('slow');
				}
			}
			$("a[id='newTaxRuleform-container-close']").attr("href",location.hash);
		},
		error: function (xhr, status, error) {
	  		if(xhr.status == 401){ 
	  			doLogout(); 
	  		}else if(xhr.status == 500){
	    		swal("Error on fetching Tax detail!", "Please retry, if problem persists contact support team.", "error");
	    	}
      	},
		complete: function(data) {
			alwaysScrollTop();
			$.unblockUI();
		}
	});
}


function validateRcmTaxationRule(elem){
	var trId=$(elem).parent().parent('tr:first').attr('id');
	if(typeof(trId)!='undefined'){
		var curRow="";var curIVRow="";var totalTaxes="";
		if(trId==="1"){
			curRow="sgst";curIVRow="IV1";totalTaxes="(sgst)"
		}else if(trId==="2"){
			curRow="cgst";curIVRow="IV2";totalTaxes="(sgst+cgst)"
		}else if(trId==="3"){
			curRow="igst";curIVRow="IV3";totalTaxes="(sgst+cgst+igst)"
		}else if(trId==="4"){
			curRow="cess";curIVRow="IV4";totalTaxes="(sgst+cgst+igst+cess)"
		}
		
		var taxRate =  $("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] select[id='itemCessrate']").val();
		var addDeduct= $("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] select[id='taxAddDeduct']").val();
		var applyTo =  $("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] select[id='taxApplyTo']").val();

		if(taxRate!="" && addDeduct!="" && applyTo!=""){
			var formula = curRow + "=" + applyTo + "*(Rate/100)";
			var invoiceValue="GV";
			if(addDeduct==1){
				invoiceValue+='+';
			}else if(addDeduct==0){
				invoiceValue+='-';
			}
			invoiceValue= curIVRow + "=" + invoiceValue + totalTaxes;
			$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxFormula']").val(formula);
			$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxInvoiceValue']").val(invoiceValue);''
			
			if(trId==="1"){
				$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('SGST');
			}else if(trId==="2"){
				$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('CGST');
			}else if(trId==="3"){
				$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('IGST');
			}else if(trId==="4"){
				$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('CESS');
			}
		}else{
			$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxFormula']").val("");
			$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxInvoiceValue']").val("");
			if(trId==="1"){
				$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('');
			}else if(trId==="2"){
				$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('');
			}else if(trId==="3"){
				$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('');
			}else if(trId==="4"){
				$("#rcmTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('');
			}
		}
	}
}

$(document).ready(function(){
	$('.applyRcmTax').click(function(){
		var taxNames= $('#rcmTaxRuleDetailsTable input[id="taxName"]').map(function () {
		    return this.value;
		}).get();
		var rcmApplicableDate = $("#rcmApplicableDate").val();
		if((taxNames.indexOf('SGST') !== -1 && taxNames.indexOf('CGST') === -1 ) || (taxNames.indexOf('SGST') === -1 && taxNames.indexOf('CGST') !== -1 )){
			swal("Error on Tax setup!", "SGST and CGST should be configured together.", "error");
			return false;
		}
		if(rcmApplicableDate == "") {
			swal("Error on Tax setup!", "Select Tax Applicable Date.", "error");
			return false;
		}

		var taxRates = $('#rcmTaxRuleDetailsTable input[id="itemgstrate"]').map(function () {
		    return this.value;
		}).get();
		var add_deduct_values= $('#rcmTaxRuleDetailsTable select[id="taxAddDeduct"] option:selected').map(function () {
		    return this.value;
		}).get();
		var rate_applied_to= $('#rcmTaxRuleDetailsTable select[id="taxApplyTo"] option:selected').map(function () {
		    return this.value;
		}).get();
		var tax_formula= $('#rcmTaxRuleDetailsTable input[id="taxFormula"]').map(function () {
		    return this.value;
		}).get();
		var invoice_value= $('#rcmTaxRuleDetailsTable input[id="taxInvoiceValue"]').map(function () {
		    return this.value;
		}).get();
		var itemCategory = $("#rcmTaxRuleDetailsTable input[id='itemCategory'").val();
		var hsnSacCode = $("#rcmTaxRuleDetailsTable input[id='itemHsnSac'").val();
		var itemTaxRate = $("#gstTaxRuleDetailsTable input[id='itemTaxRate'").val();

		var addsDeducts="";var appliedTos="";var formulas="";var invoiceValues=""; var taxNameList=""; var taxRateList ="";
		for(var i=0;i<taxNames.length;i++){
			taxNameList+=taxNames[i]+",";
			taxRateList+=taxRates[i]+",";
			addsDeducts+=add_deduct_values[i]+",";
			appliedTos+=rate_applied_to[i]+",";
			formulas+=tax_formula[i]+",";
			invoiceValues+=invoice_value[i]+",";
		}
		var jsonData={};
		jsonData.taxNames=taxNameList.substring(0, taxNameList.length-1);
		jsonData.taxRates=taxRateList.substring(0, taxRateList.length-1);
		jsonData.applyRulesToMultiItemsList=$("#multiItemsListHidden").val(); 
		jsonData.addsDeducts=addsDeducts.substring(0, addsDeducts.length-1);
		jsonData.appliedTos=appliedTos.substring(0, appliedTos.length-1);
		jsonData.formulas=formulas.substring(0, formulas.length-1);
		jsonData.invoiceValues=invoiceValues.substring(0, invoiceValues.length-1);
		jsonData.branchId=$("#bnchHidden").val();
		jsonData.specificsId=$("#specfHidden").val();
		jsonData.taxCategory=$("#taxCategoryHidden").val();
		jsonData.usermail = $("#hiddenuseremail").text();
		jsonData.itemCategory = itemCategory;
		jsonData.itemGstCode  = hsnSacCode;
		jsonData.itemGstRate  = itemTaxRate;
		jsonData.applicableDate = rcmApplicableDate;
		var url = "/tax/saveUpdateItemsTax";
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
				if(data.rescode != "0"){
					swal("Error on save/update tax!", data.message, "error");
				}else{
					$('.notify-success').show();
					$("#notificationMessage").html("Successfully Applied Rcm Tax Rules for the branch expence item.");
					$(".newRcmTaxRuleform-container").slideUp('slow');
					$("a[id='newRcmTaxRuleform-container-close']").attr("href",location.hash);
				}
			},
		   	error: function (xhr, status, error) {
	    		if(xhr.status == 401){ 
	    			doLogout(); 
			    }else if(xhr.status == 500){
		    		swal("Error on save/update output tax!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$("#multiItemsListHidden").val("");
				$.unblockUI();
			}
		});
	});
});

function applyTaxRulesToMultipleRcmItems(){
	var parentTr=$(this).closest('div').attr('id');
	var parentOfparentTr = $(this).parents().closest('tr').attr('id');
	var useremail=$("#hiddenuseremail").text();	
	var jsonData = {};
	var branchId = $("#bnchHidden").val();
	var specfId = $("#specfHidden").val();
	jsonData.usermail = useremail;
	jsonData.branchId = branchId;
	//var url ="/config/getBranchIncomesCoa";
	var url = "/data/getcoaExpenceitemsWithTaxRules";
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
			$("#staticTaxesForMultiItems").attr('data-toggle', 'modal');
	    	$("#staticTaxesForMultiItems").modal('show');
	    	$(".staticTaxesForMultiItemsclose").attr("href",location.hash);
	    	$("#staticTaxesForMultiItems div[class='modal-body']").html("");			
			$("#staticTaxesForMultiItems div[class='modal-body']").append('<div class="itemsSelect"><ul id="multiItemsTaxesList" style="list-style-type: none; ">');
			$("#staticTaxesForMultiItems div[class='modal-body']").find("ul[id='multiItemsTaxesList']").append('<li style="text-align: left;"><input id="SelectAll" value="Select All" type="checkbox" onchange="selectAllIncomeItemsForTaxSetup();">Select All</b></li>');
			if(data.expenceItemData.length>0){		
				
				for(var i=0;i<data.expenceItemData.length;i++){	
					if(data.expenceItemData[i].isTaxSetup == "No"){
						$("#staticTaxesForMultiItems div[class='modal-body']").find("ul[id='multiItemsTaxesList']").append('<li style="text-align: left;"><input id="'+data.expenceItemData[i].id+'" value="'+data.expenceItemData[i].id+'" type="checkbox">'+data.expenceItemData[i].name+'</b></li>');
						if(specfId == data.expenceItemData[i].id ){
							$("#staticTaxesForMultiItems div[class='modal-body'] input[id='"+data.expenceItemData[i].id+"']").prop("checked","checked");
						}
					}
					
				}				
			}    			
    		$("#staticTaxesForMultiItems div[class='modal-body'] ").append('</ul><input type="button" style="width:60px;" id="submitInputTax" name="submitInputTax" class="btn btn-submit" value="Submit" onclick="submitMultipleItemsTaxRules(this,\''+parentTr+'\',\''+parentOfparentTr+'\');"></div>');		
		},
		error : function() {

		}
	});
}

