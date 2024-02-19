function toggleGstApplyTo(elem){
	var trId=$(elem).parent().parent('tr:first').attr('id');
	$("#gstTaxRuleDetailsTable  tr[id='"+trId+"'] select[id='taxApplyTo'] option:first").prop("selected","selected");
	$("#gstTaxRuleDetailsTable  tr[id='"+trId+"'] input[id='taxFormula']").val("");
	$("#gstTaxRuleDetailsTable  tr[id='"+trId+"'] input[id='taxInvoiceValue']").val("");
	$("#gstTaxRuleDetailsTable  tr[id='"+trId+"'] input[id='taxName']").val("");
	//$("#"+trId+" input[id='rowCheckBox']").prop('checked',false);
}

var createGstTaxRule = function(elem,branchId,specificsId, taxCategory){
	/*if(taxCategory === 1){
		swal("Please configure Input taxes from branch setup.");
		return false;
	} */
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
			$("#taxRuleDetailsTable tbody").html("");
			var gstTaxRate = data.rate;
			if(data){
			/*	if(data.itemCategory == ""){
					swal("Error insuffient data!", "Type of Supply cannot not be empty, please define from COA.", "error");
					return false;
				}
				if(data.hsnSacCode == ""){
					swal("Error insuffient data!", "HSN/SAC cannot not be empty, please define from COA.", "error");
					return false;
				} */
				if(data.rate == "" || data.rate == null){
					swal("Error insuffient data!", "Tax rate cannot not be empty, please define from COA.", "error");
					return false;
				}
				$("#gstTaxRuleDetailsTable input[id='itemCategory'").val(data.itemCategory);
				$("#gstTaxRuleDetailsTable input[id='itemHsnSac'").val(data.hsnSacCode);
				$("#gstTaxRuleDetailsTable input[id='itemTaxRate'").val(data.rate);
				$("#gstTaxRuleDetailsTable input[id='itemCessTaxRate'").val(data.cessRate);
				$("#gstTaxRuleDetailsTable #itemNameLbl").empty();
				$("#gstTaxRuleDetailsTable #itemNameLbl").append(data.specificsname);
				$("#gstTaxRuleDetailsTable tr[id='1'] input[id='itemgstrate'").val(data.sgstRate);
				$("#gstTaxRuleDetailsTable tr[id='2'] input[id='itemgstrate'").val(data.cgstRate);
				$("#gstTaxRuleDetailsTable tr[id='3'] input[id='itemgstrate'").val(data.igstRate);
				$("#gstTaxRuleDetailsTable tr[id='4'] input[id='itemgstrate'").val(data.cessRate);

				if(data.allowAddNew === 1){
					$(".newTaxRuleform-container").slideDown('slow');
				}
				if(data.applicableDate != "") {
					$("#applicableDate").datepicker('destroy');
					$("#applicableDate").datepicker({
						changeMonth : true,
						changeYear : true,
						dateFormat:  'M d,yy',
						onSelect: function(x,y){
					        $(this).focus();
					    }
					});
					$("#applicableDate").datepicker("option", "minDate", data.applicableDate);
				}
				var tableTr = "<tbody>";
				$("#taxRuleListTable tbody").empty();
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
				$("#taxRuleListTable").append(tableTr);
				if(data.itemTaxList.length > 0){
					$("#gsttaxList-form-container").slideDown('slow');
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
//	$(".newTaxRuleform-container").slideDown('slow');
}

var validateGstTaxRate = function(elem, taxName){
	var curTaxRate = $(elem).val();
	var itemTaxRate = $("#itemTaxRate").val();
	if(taxName === "SGST" || taxName === "CGST"){
		curTaxRate = $("#gstTaxRuleDetailsTable tr[id=1] input[id='itemgstrate']").val();
		var cgstTaxRate = $("#gstTaxRuleDetailsTable tr[id=2] input[id='itemgstrate']").val();
		var tempTaxRate = parseFloat(curTaxRate) + parseFloat(cgstTaxRate);
		if(parseFloat(tempTaxRate) > parseFloat(itemTaxRate)){
			swal("Invalid Tax Rate!", "SGST + CGST cannot be more than " + itemTaxRate, "error");
			$(elem).val('');
			return false;
		}
	}else if(taxName === "IGST"){
		if(parseFloat(curTaxRate) > parseFloat(itemTaxRate)){
			swal("Invalid Tax Rate!", "IGST cannot be more than " + itemTaxRate, "error");
			$(elem).val('');
			return false;
		}
	}
}

function validateGstTaxationRule(elem){
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
		
		var taxRate =  $("#gstTaxRuleDetailsTable tr[id='"+trId+"'] select[id='itemCessrate']").val();
		var addDeduct= $("#gstTaxRuleDetailsTable tr[id='"+trId+"'] select[id='taxAddDeduct']").val();
		var applyTo =  $("#gstTaxRuleDetailsTable tr[id='"+trId+"'] select[id='taxApplyTo']").val();

		if(taxRate!="" && addDeduct!="" && applyTo!=""){
			var formula = curRow + "=" + applyTo + "*(Rate/100)";
			var invoiceValue="GV";
			if(addDeduct==1){
				invoiceValue+='+';
			}else if(addDeduct==0){
				invoiceValue+='-';
			}
			invoiceValue= curIVRow + "=" + invoiceValue + totalTaxes;
			$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxFormula']").val(formula);
			$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxInvoiceValue']").val(invoiceValue);''
			
			if(trId==="1"){
				$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('SGST');
			}else if(trId==="2"){
				$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('CGST');
			}else if(trId==="3"){
				$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('IGST');
			}else if(trId==="4"){
				$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('CESS');
			}
		}else{
			$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxFormula']").val("");
			$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxInvoiceValue']").val("");
			if(trId==="1"){
				$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('');
			}else if(trId==="2"){
				c$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('');
			}else if(trId==="3"){
				$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('');
			}else if(trId==="4"){
				$("#gstTaxRuleDetailsTable tr[id='"+trId+"'] input[id='taxName']").val('');
			}
		}
	}
}


$(document).ready(function(){
	$('.applyGstTax').click(function(){
		var taxNames= $('input[id="taxName"]').map(function () {
		    return this.value;
		}).get();
		//var applicableDate = $("#applicableDate").val();
		if((taxNames.indexOf('SGST') !== -1 && taxNames.indexOf('CGST') === -1 ) || (taxNames.indexOf('SGST') === -1 && taxNames.indexOf('CGST') !== -1 )){
			swal("Error on Tax setup!", "SGST and CGST should be configured together.", "error");
			return false;
		}
	/*	if(applicableDate == "") {
			swal("Error on Tax setup!", "Select Tax Applicable Date.", "error");
			return false;
		}
	*/
		var taxRates = $('input[id="itemgstrate"]').map(function () {
		    return this.value;
		}).get();
		var add_deduct_values= $('select[id="taxAddDeduct"] option:selected').map(function () {
		    return this.value;
		}).get();
		var rate_applied_to= $('select[id="taxApplyTo"] option:selected').map(function () {
		    return this.value;
		}).get();
		var tax_formula= $('input[id="taxFormula"]').map(function () {
		    return this.value;
		}).get();
		var invoice_value= $('input[id="taxInvoiceValue"]').map(function () {
		    return this.value;
		}).get();
		var itemCategory = $("#gstTaxRuleDetailsTable input[id='itemCategory'").val();
		var hsnSacCode = $("#gstTaxRuleDetailsTable input[id='itemHsnSac'").val();
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
	//	jsonData.applicableDate = applicableDate;
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
					$("#notificationMessage").html("Successfully Applied Tax Rules for the branch income item.");
					$(".newTaxRuleform-container").slideUp('slow');
					$("a[id='newTaxRuleform-container-close']").attr("href",location.hash);
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


function validateGstTaxFormula(){
	var taxFormulas="";var taxRates="";
	var addDeducts="";var applyTos="";
	$("input[id='taxFormula']").each(function(){
		if($(this).val()!=""){
			taxFormulas+=$(this).val()+",";
			taxRates+=$(this).parent().parent().find('input[id="itemgstrate"]').val()+",";
			addDeducts+=$(this).parent().parent().find('select[id="taxAddDeduct"] option:selected').val()+",";
			applyTos+=$(this).parent().parent().find('select[id="taxApplyTo"] option:selected').text()+",";
		}
	})
	if(taxFormulas!="" && taxRates!="" && addDeduct!="" && applyTos!=""){
		var jsonData = {};
		var taxFormula=taxFormulas.substring(0, taxFormulas.length-1);
		var taxRate=taxRates.substring(0, taxRates.length-1);
		var addDeduct=addDeducts.substring(0, addDeducts.length-1);
		var applyTo=applyTos.substring(0, applyTos.length-1);
		var avalue="100";
		var useremail=$("#hiddenuseremail").text();
		jsonData.usermail = useremail;
		jsonData.taxFormulas=taxFormula;
		jsonData.taxRates=taxRate;
		jsonData.addDeducts=addDeduct;
		jsonData.applyTos=applyTo;
		jsonData.aValue=avalue;
		var url="/config/taxFormulaValidation";
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
				var output = '';var dataNode=data.taxFormulaOutcomeData;
				for (var key in dataNode) {
					var newoutput= JSON.stringify(dataNode[key]).split(':');
					output+=newoutput[0].replace("{","")+"="+newoutput[1].replace("}","")+",";
				}
				swal("INFO",output.substring(0,output.length-1).replace(/"/g, ''),"INFO");
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	}else{
		swal("Error!","Tax Formula is not configured.Please configure.","error");
		return true;
	}
}



/*$(document).ready(function() {
	  $("#searchGSTDesc").click(function () {
	    alert("Hello!");
	  });
	});
*/
function searchGSTItemBasedOnDesc(){	
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	jsonData.gstTypeOfSupply=$("#GSTtypeOfSupply option:selected").val();
	jsonData.itemDesc=$("#GSTDesc").val();
	var url="/GSTController/searchGSTItemBasedOnDesc";
	$.ajax({
		url         : url,
		data        : JSON.stringify(jsonData),
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		type        : "text",
		method      : "POST",
		contentType : 'application/json',
		success     : function (data) {
			$("#chartOfAccountTable tr td:nth-child(4) div[id='GSTItemsList']").show();
			//$("#chartOfAccountTable tr td:nth-child(4) div[id='GSTItemsList'] select[id='GSTItems']").children().remove();
			$("#chartOfAccountTable tr td:nth-child(4) div[id='GSTItemsList'] select[id='GSTItems']").remove();
			var gstItemsListTemp = "";
			for(var i=0;i<data.gstItemsData.length;i++){
				gstItemsListTemp += ('<option value="'+data.gstItemsData[i].code+','+data.gstItemsData[i].rate+'" id="'+data.gstItemsData[i].code+'">'+data.gstItemsData[i].label+'</option>');
	    	 }
			$("#chartOfAccountTable tr td:nth-child(4) div[id='GSTItemsList']").append('<select name="GSTItems" id="GSTItems" onchange="getSelectedHSNCodeAndTaxRate(this);"><option value="0">Please Select..</option></select>');
			$("#chartOfAccountTable tr td:nth-child(4) div[id='GSTItemsList'] select[id='GSTItems']").append(gstItemsListTemp);
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function getSelectedHSNCodeAndTaxRate(elem){
	var input1=$('select[name="GSTItems"]').map(function () {
		return this.value;
	   }).get();
	var fields = input1.toString().split(',');
	var gstCode = fields[0];
	var gstTaxRate = fields[1];
	$('#GSTCode').val(gstCode);
	$('#GSTTaxRate').val(gstTaxRate);
		
}

$(document).ready(function(){
	$(".mainBranchExpChartOfAccount").treeview({
		animated: "fast",
		collapsed: true,
		unique: true,
		toggle: function() {
		}
	});
});

$(document).ready(function(){
	$('body').on('click','.taxsetuptabCls',function(){
		var tabClass = $(this).parent().attr("class");
		if(tabClass == "active"){
			return false;
		}
		var tab = $(this).parent().attr("id");
		//alert(clickedTab);
		//var tab=$(this).attr('data-tab');
		$('.taxsetupcls').hide();
		if('inputTaxTab'==tab){
			$("#outputTaxTab").removeAttr("class", "active");
			$("#rcmTaxTab").removeAttr("class", "active");
			$("#inputTaxTab").attr("class", "active");
			$("#tdsTaxTab").removeAttr("class", "active");
			$("#taxableItemTab").removeAttr("class", "active");
			resetInputTaxScreen();
			$('#inputTaxDiv').fadeIn('normal',function(){
			});
		}else if('rcmTaxTab'==tab){
			$("#rcmTaxTab").attr("class", "active");
			$("#inputTaxTab").removeAttr("class", "active");
			$("#outputTaxTab").removeAttr("class", "active");
			$("#tdsTaxTab").removeAttr("class", "active");
			$("#taxableItemTab").removeAttr("class", "active");
			$('#rcmTaxDiv').fadeIn('normal',function(){
				//showNewRcmTax(this);
			});
		}else if('outputTaxTab'===tab){
            $("#taxSetup select[id='placeOfSupplyType']").val(ORG_PLACE_OF_SUPPLY_TYPE);
			alwaysScrollTop();
			$("#outputTaxTab").attr("class", "active");
			$("#rcmTaxTab").removeAttr("class", "active");
			$("#tdsTaxTab").removeAttr("class", "active");
			$("#inputTaxTab").removeAttr("class", "active");
			$("#taxableItemTab").removeAttr("class", "active");
			$('#outputTaxDiv').fadeIn('normal',function(){
				//$('.taxsetuptabCls').attr('data-tab','inputTaxTab');
			});
		}else if('tdsTaxTab'===tab){
            $("#tdsTaxTab").attr("class", "active");
			$("#inputTaxTab").removeAttr("class", "active");
			$("#outputTaxTab").removeAttr("class", "active");
			$("#rcmTaxTab").removeAttr("class", "active");
			$("#taxableItemTab").removeAttr("class", "active");
			$("#fromToDateDiv").find('label').hide();
			$("#fromToDateDiv").find('input').hide();
			$('#tdsTaxDiv').fadeIn('normal',function(){
				displayTdsBasicDetails();
			});
		}else if('taxableItemTab'===tab){
            $("#taxableItemTab").attr("class", "active");
			$("#inputTaxTab").removeAttr("class", "active");
			$("#outputTaxTab").removeAttr("class", "active");
			$("#rcmTaxTab").removeAttr("class", "active");
			$("#tdsTaxTab").removeAttr("class", "active");
			$("#fromToDateDiv").find('label').hide();
			$("#fromToDateDiv").find('input').hide();
			$('#taxableItemDiv').fadeIn('normal',function(){
				displayTaxableItemsDetails();
			});
		}
	});
});


var showNewInputTaxDiv = function(elem){
	var value = $(elem).val();
	resetTaxTableLength('inputTaxTable');
	if(value == ""){
		$("#newinputtaxdiv").hide();	
	}else{
		$("#newinputtaxdiv").show();
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var url = '/tax/branchInputTaxes/'+value;
		$.ajax({
			url         : url,
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method      : "GET",
			contentType : 'application/json',
			success     : function (data) {
				var taxList = "";
				for(var i=0; i<data.itemTaxList.length; i++){
					taxList += '<tr id="'+data.itemTaxList[i].id+'"><td>'+data.itemTaxList[i].taxname+'</td><td>'+data.itemTaxList[i].rate+'</td></tr>';
		    	}
		    	$("#InputTaxHistory tbody").html('');
		    	$("#InputTaxHistory tbody").append(taxList);
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
	}
}

$(document).ready(function() {
	$("#addMoreInputTaxes").click(function(){
		var length = $("#inputTaxTable tbody tr").length;
		var newTrId = 'inputtaxtr'+length;
		var trdata = '<tr id="inputtaxtr'+length+'"><td></td><td><select multiple="multiple" name="inputTaxBranchMulti" id="inputTaxBranchMulti"></select></td><td style="min-width:100px;"><div class="ui-widget"><select id="inputGstTaxRate" class="combobox"><option value="">Select/Input</option><option value="0">0</option><option value="5">5</option><option value="12">12</option><option value="18">18</option><option value="28">28</option></select></div></td><td colspan="2"><input type="text" id="inputCessTaxRate" onkeypress="return onlyDotsAndNumbersWithMultipleIDs(event, this)"></td></tr>';
		$("#inputTaxTable tbody").append(trdata);
		$("#" + newTrId + " #inputGstTaxRate").combobox();
        let tempArr = Object.values(ALL_BRANCH_OF_ORG_MAP);
        let branchlist = tempArr.join('');
		$("#" + newTrId + " #inputTaxBranchMulti").append(branchlist);
		$('#'+newTrId+' select[id="inputTaxBranchMulti"]').multiselect({
			buttonWidth: '250px',
	        maxHeight:   150,
	        includeSelectAllOption: true,
	        enableFiltering :true,
	        enableCaseInsensitiveFiltering: true
		});
	});
	
	$("#removeInputTaxes").click(function(){
		var tableTrCount = $("#inputTaxTable tbody tr").length;
		if(tableTrCount > 1){
			$("#inputTaxTable tbody tr:last").remove();
		}
	});
});
var resetTaxTableLength = function(table){
	var initLen = 1;
	var tableTrCount = $("#"+table+" tbody tr").length;
	for (var i = tableTrCount; i > initLen ; i--) {
		$("#"+table+" tr:eq("+i+")").remove();
	}
	$("#"+table+" tbody tr").find(':input[type=text]').val('');
}

$(function() {
    $.widget( "custom.combobox", {
      	_create: function(){
        	this.wrapper = $( "<span>" ).addClass( "custom-combobox").insertAfter( this.element );
 	        this.element.hide();
    	    this._createAutocomplete();
        	this._createShowAllButton();
      	},
 
      _createAutocomplete: function() {
        var selected = this.element.children( ":selected" ),
          value = selected.val() ? selected.text() : "";
 
        this.input = $( "<input>" )
          .appendTo( this.wrapper )
          .val( value )
          .attr( "title", "" )
          .addClass( "custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left" )
          .autocomplete({
            delay: 0,
            minLength: 0,
            source: $.proxy( this, "_source" )
          })
          .tooltip({
            classes: {
              "ui-tooltip": "ui-state-highlight"
            }
          });
 
        this._on( this.input, {
          autocompleteselect: function( event, ui ) {
            ui.item.option.selected = true;
            this._trigger( "select", event, {
              item: ui.item.option
            });
          },
 
          autocompletechange: "_removeIfInvalid"
        });
      },
 
      _createShowAllButton: function() {
        var input = this.input,
          wasOpen = false;
 
        $( "<a>" )
          .attr( "tabIndex", -1 )
          .attr( "title", "Show All Items" )
          .tooltip()
          .appendTo( this.wrapper )
          .button({
            icons: {
              primary: "ui-icon-triangle-1-s"
            },
            text: false
          })
          .removeClass( "ui-corner-all" )
          .addClass( "custom-combobox-toggle ui-corner-right" )
          .on( "mousedown", function() {
            wasOpen = input.autocomplete( "widget" ).is( ":visible" );
          })
          .on( "click", function() {
            input.trigger( "focus" );
 
            // Close if already visible
            if ( wasOpen ) {
              return;
            }
 
            // Pass empty string as value to search for, displaying all results
            input.autocomplete( "search", "" );
          });
      },
 
      _source: function( request, response ) {
        var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
        response( this.element.children( "option" ).map(function() {
          var text = $( this ).text();
          if ( this.value && ( !request.term || matcher.test(text) ) )
            return {
              label: text,
              value: text,
              option: this
            };
        }) );
      },
 
      _removeIfInvalid: function( event, ui ) {
 
        // Selected an item, nothing to do
        if ( ui.item ) {
          return;
        }
 
        // Search for a match (case-insensitive)
        var value = this.input.val(),
          valueLowerCase = value.toLowerCase(),
          valid = false;
        this.element.children( "option" ).each(function() {
          if ( $( this ).text().toLowerCase() === valueLowerCase ) {
            this.selected = valid = true;
            return false;
          }
        });
 
        // Found a match, nothing to do
        if ( valid ) {
          return;
        }
 
        // Remove invalid value
       // this.input
         // .val( "" )
          //.attr( "title", value + " didn't match any item" )
         // .tooltip( "open" );
        //this.element.val( "" );
        this._delay(function() {
          this.input.tooltip( "close" ).attr( "title", "" );
        }, 2500 );
        this.input.autocomplete( "instance" ).term = "";
      },
 
      _destroy: function() {
        this.wrapper.remove();
        this.element.show();
      }
    });
 
    $("#inputtaxtr .combobox" ).combobox();
  } );

var saveInputTaxes = function(){
	var isValid = true;
	var multipleTaxData = [];
	var jsonData = {};
	$("#inputTaxTable > tbody > tr").each(function(){
		var selectedBranches = $(this).find('select[id="inputTaxBranchMulti"] option:selected').map(function () {
			if(this.value!="multiselect-all"){
				return this.value;
			}
		}).get();
		
		jsonData.branches=selectedBranches.toString();
		var gstTaxRates = $(this).find("td input[class='custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left ui-autocomplete-input']").val();
		if(gstTaxRates != "" && parseFloat(gstTaxRates) > 100){
			swal("Invalid GST Tax Rate!", "GST tax Rate cannot be more than 100%.", "error");
			isValid = false;
			return false;
		}
		var cessTaxRates = $(this).find("td input[id='inputCessTaxRate']").val();
		if(cessTaxRates != "" && parseFloat(cessTaxRates) > 100){
			swal("Invalid CESS Rate!", "CESS Rate cannot be more than 100%.", "error");
			isValid = false;
			return false;
		}

		if(cessTaxRates == "" && gstTaxRates == ""){
			swal("Invalid Rates!", "GST and CESS, both cannot be empty.", "error");
			isValid = false;
			return false;
		}
		jsonData.gstTaxRates = gstTaxRates;
		jsonData.cessTaxRates = cessTaxRates;
		multipleTaxData.push(JSON.stringify(jsonData));
	});
	if(jsonData.branches == ""){
		swal("Invalid Branch!", "Branch is not selected.", "error");
		return false;
	}

	if(!isValid){
		return false;
	}
	var txnJsonData = {};
	txnJsonData.inputTaxData = multipleTaxData;
	var url = '/tax/saveinputtaxes';
	$.ajax({
		url         : url,
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		data        : JSON.stringify(txnJsonData),
		contentType : 'application/json',
		success     : function (data) {
			resetInputTaxScreen();
		},
		error : function (xhr, status, error) {
			if(xhr.status == 401){ 
				doLogout(); 
			}else if(xhr.status == 500){
	    		swal("Error on storing input taxes!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

var resetInputTaxScreen = function(){
	resetTaxTableLength('inputTaxTable');
	$("#newinputtaxdiv").hide();
	$('#inputTaxBranch').find('option:first').prop("selected","selected");
	$("#inputtaxtr select[id='inputTaxBranchMulti'] option:selected").removeAttr("selected");
	$("#inputtaxtr select[id='inputTaxBranchMulti']").multiselect('refresh');
}

/* Function already present in configTransVendorCust.js
function getCustomerListForBranch(elem){
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
	$("#mcpfcvvendcustoutstandingsgross").text("");
	$("#mcpfcvvendcustoutstandingsnet").text("");
	$("#mcpfcvvendcustoutstandingsnetdescription").text("");
	$("#mcpfcvvendcustoutstandingspaid").text("");
	$("#mcpfcvvendcustoutstandingsnotpaid").text("");
	$("#mcpfcvtxninprogress").text("");
	var txnPurposeBranchId=$(elem).val();
	var jsonData = {};
	var transactionPurposeId=$("#whatYouWantToDo").find('option:selected').val();
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
}*/

/**************RCM Changes******************/
var rcmDescripCache = {}; var rcmExpenseItemsCache = {}; //Global param

$(document).ready(function(){
	$("#rcmDescription").autocomplete({
        delay: 100,
        autoFocus: true,
        cacheLength: 10,
        scroll: true,
        highlight: false,
		minLength: 3,
		source: function( request, response ) {
	        var term = request.term;
	        if ( term in rcmDescripCache ) {
				response(rcmDescripCache[term]);
				return;
	        }
	        var selectedItemsArray = [];
	        var jsonData = {};
	        jsonData.gstTypeOfSupply=$("#rcmTaxDiv select[id='rcmTypeOfSupply'] option:selected").val();
			jsonData.itemDesc=$("#rcmDescription").val();
			$.ajax({
				dataType: "json",
				type: 'text',
				data: JSON.stringify(jsonData),
				method: "POST",
				url: '/GSTController/searchGSTItemBasedOnDesc',
				headers:{
					"X-AUTH-TOKEN": window.authToken
			  	},
			  	contentType:'application/json',
				success: function(data) {
					rcmDescripCache[term] = data.gstItemsData;
					response(data.gstItemsData);
					$('#rcmDescription').removeClass('ui-autocomplete-loading');
				},
				error: function(data) {
					$('#rcmDescription').removeClass('ui-autocomplete-loading');  
				}
			});
    	},
    	open: function() {
    	},
	    close: function(){ 
		},
	    focus: function(event,ui) {
	    },
	    select: function(event, ui) {
	    	event.preventDefault();
	    	var parentTr = $(this).closest('tr').attr('id');
	    	$("#"+parentTr+" input[id='rcmDescription']").val(ui.item.label); 
	    	$("#"+parentTr+" input[id='hsnSacCode']").val(ui.item.code);
	    	$("#"+parentTr+" input[id='rcmGstTaxRate']").val(ui.item.rate);
	    },
	    change: function( event, ui ) {
	    }
    });


	$("#rcmItem").autocomplete({
        delay: 100,
        autoFocus: true,
        cacheLength: 10,
        scroll: true,
        highlight: false,
		minLength: 3,
		source: function( request, response ) {
	        var term = request.term;
	        if ( term in rcmExpenseItemsCache ) {
				response(rcmExpenseItemsCache[term]);
				return;
	        }
	        var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex(request.term, ""), "i");
	        var filteredArray = $.grep(expenseItemDataGlobal, function(value){
	        	return matcher.test( value.label || value.value || value );
	            //return matcher.test(item);
	        });
	        response(filteredArray);
	      	rcmExpenseItemsCache[term] = filteredArray;
			$('#rcmItem').removeClass('ui-autocomplete-loading');
    	},
    	open: function() {
    	},
	    close: function(){ 
		},
	    focus: function(event,ui) {
	    },
	    select: function(event, ui) {
	    	event.preventDefault();
	    	var parentTr = $(this).closest('tr').attr('id');
	    	$("#"+parentTr+" input[id='rcmItem']").val(ui.item.label); 
	    	$("#"+parentTr+" input[id='rcmItemId']").val(ui.item.id);
	    },
	    change: function( event, ui ) {
	    }
    });
});



var resetRcmTaxDetail = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var supplyType = $("#"+parentTr+" select[id='rcmTypeOfSupply'] option:selected").val();  
	if(supplyType == ""){
		swal("Invalid Goods/Server!", "Please select Goods/Services.", "error");
		return false;
	}
}

$(document).ready(function() {
	$("#addMoreRcmTaxes").click(function(){
		var length = $("#rcmTaxTable tbody tr").length;
		var newTrId = 'rcmtaxtr'+length;
		var trdata = '<tr id="rcmtaxtr'+length+'"><td><select id="rcmTaxBranch"><option value="">Select a Branch</option></select></td><td><select id="rcmTaxVendor" onChange="getCustomerListForBranch(this);"><option value="">--Please Select--</option></select></td><td><select id="rcmTypeOfSupply"><option value="">Please Select</option><option value="1">Goods</option><option value="2">Services</option></select></td><td><input type="text" style="width: 200px;" id="rcmDescription" maxlength="256" onkeyup="resetRcmTaxDetail();"></td><td><input type="text" id="hsnSacCode" style="width: 100px;"></td><td><input type="text" style="width: 60px;" id="rcmGstTaxRate" onkeypress="return onlyDotsAndNumbers(event);"/></td><td><input type="text" style="width: 60px;" id="rcmCessTaxRate" onkeypress="return onlyDotsAndNumbers(event);"></td><td><input type="text" id="rcmItem" style="width: 200px;"></td><input type="hidden" id="rcmItemId"></tr>';
		$("#rcmTaxTable tbody").append(trdata);
        let branchlistArray = Object.values(ALL_BRANCH_OF_ORG_MAP);
        let branchlist = branchlistArray.join('');
		$("#"+ newTrId + " #rcmTaxBranch").append(branchlist);
        let vendorlistArray = Object.values(ALL_VENDORS_MAP);
        let vendorList = vendorlistArray.join('');
        $("#"+ newTrId + " select[id='rcmTaxVendor']").append(vendorList);
		$("#rcmtaxtr" + length + " #rcmDescription").autocomplete({
	        delay: 100,
	        autoFocus: true,
	        cacheLength: 10,
	        scroll: true,
	        highlight: false,
			minLength: 3,
			source: function( request, response ) {
		        var term = request.term;
		        if ( term in rcmDescripCache ) {
					response(rcmDescripCache[term]);
					return;
		        }
		        var selectedItemsArray = [];
		        var jsonData = {};
		        jsonData.gstTypeOfSupply=$("#rcmTaxDiv select[id='rcmTypeOfSupply'] option:selected").val();
				jsonData.itemDesc=$("#rcmDescription").val();
				$.ajax({
					dataType: "json",
					type: 'text',
					data: JSON.stringify(jsonData),
					method: "POST",
					url: '/GSTController/searchGSTItemBasedOnDesc',
					headers:{
						"X-AUTH-TOKEN": window.authToken
				  	},
				  	contentType:'application/json',
					success: function(data) {
						rcmDescripCache[term] = data.gstItemsData;
						response(data.gstItemsData);
						$('#rcmDescription').removeClass('ui-autocomplete-loading');
										
						//response( $.map( data, function(item) {
						// your operation on data
						//}));
					},
					error: function(data) {
						$('#rcmDescription').removeClass('ui-autocomplete-loading');  
					}
				});
		       /* $.getJSON( "/customer/walkincustomer", request, function( data, status, xhr ) {
					cache[term] = data;
					response(data.cutomerList);
		        });*/
	    	},
	    	open: function() {
	    	},
		    close: function(){ 
			},
		    focus: function(event,ui) {
		    },
		    select: function(event, ui) {
		    	event.preventDefault();
		    	var parentTr = $(this).closest('tr').attr('id');
		    	$("#"+parentTr+" input[id='rcmDescription']").val(ui.item.label); 
		    	$("#"+parentTr+" input[id='hsnSacCode']").val(ui.item.code);
		    	$("#"+parentTr+" input[id='inputGstTaxRate']").val(ui.item.rate);
		    	//showWalkinCustomerDetail(this);
		    },
		    change: function( event, ui ) {
		    	//showWalkinCustomerDetail(this);
		    }
    	});


		$("#rcmtaxtr" + length + " #rcmItem").autocomplete({
	        delay: 100,
	        autoFocus: true,
	        cacheLength: 10,
	        scroll: true,
	        highlight: false,
			minLength: 3,
			source: function( request, response ) {
		        var term = request.term;
		        if ( term in rcmExpenseItemsCache ) {
					response(rcmExpenseItemsCache[term]);
					return;
		        }
		      	var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex(request.term, ""), "i");
		        var filteredArray = $.grep(expenseItemDataGlobal, function(value){
		        	return matcher.test( value.label || value.value || value );
		        });
		        response(filteredArray);
		      	rcmExpenseItemsCache[term] = filteredArray;
				$(this).removeClass('ui-autocomplete-loading');
	    	},
	    	open: function() {
	    	},
		    close: function(){ 
			},
		    focus: function(event,ui) {
		    },
		    select: function(event, ui) {
		    	event.preventDefault();
		    	var parentTr = $(this).closest('tr').attr('id');
		    	$("#"+parentTr+" input[id='rcmItem']").val(ui.item.label); 
		    	$("#"+parentTr+" input[id='rcmItemId']").val(ui.item.id);
		    },
		    change: function( event, ui ) {
		    }
    	});
	});
	
	$("#removeRcmTaxes").click(function(){
		var tableTrCount = $("#rcmTaxTable tbody tr").length;
		if(tableTrCount > 1){
			$("#rcmTaxTable tbody tr:last").remove();
		}
	});
});

var showRcmTaxWindow=function(){
	$.ajax({
		url: '/data/getcoaexpenseitems',
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method: "GET",
		success: function(data) {
			expenseItemDataGlobal = data.coaItemData;
		},
		error: function(xhr, errorMessage, error){
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
                swal("Error on fetching expense items!", "Please retry, if problem persists contact support team", "error");
            }
		}
	});
	
	$("#rcmTaxTable tbody").html("");
	$("#addMoreRcmTaxes").click();
	$('#newRcmTaxDiv').show();
}

var saveRcmTaxes = function(){
	var multipleTaxData = [];
	var jsonData = {};
	var flag = true;
	$("#rcmTaxTable > tbody > tr").each(function(){
		var rcmTaxBranch = $(this).find('select[id="rcmTaxBranch"] option:selected').val();
		if(rcmTaxBranch == ""){
			swal("Invalid Branch!", "Select a Branch.", "error");
			flag = false;
			return false;
		}
		var rcmTaxVendor = $(this).find('select[id="rcmTaxVendor"] option:selected').val();
		if(rcmTaxVendor == ""){
			swal("Invalid vendor!", "Select a vendor.", "error");
			flag = false;
			return false;
		}
		var rcmTypeOfSupply = $(this).find('select[id="rcmTypeOfSupply"] option:selected').val();
		if(rcmTypeOfSupply == ""){
			swal("Invalid vendor!", "Select a vendor.", "error");
			flag = false;
			return false;
		}
		var rcmDescription = $(this).find('input[id="rcmDescription"]').val();
		if(rcmTypeOfSupply == ""){
			swal("Invalid HSN Description!", "Provide HSN Description.", "error");
			flag = false;
			return false;
		}
		var hsnSacCode = $(this).find('input[id="hsnSacCode"]').val();
		if(hsnSacCode == ""){
			swal("Invalid HSN/SAC!", "Provide HSN/SAC.", "error");
			flag = false;
			return false;
		}
		var gstTaxRates = $(this).find("input[id='rcmGstTaxRate']").val();
		if(gstTaxRates == ""){
			swal("Invalid GST Tax Rate!", "GST tax Rate cannot be empty.", "error");
			flag = false;
			return false;
		}else if(parseFloat(gstTaxRates) > 100){
			swal("Invalid GST Tax Rate!", "GST tax Rate cannot be more than 100%.", "error");
			flag = false;
			return false;
		}
		var cessTaxRates = $(this).find("input[id='rcmCessTaxRate']").val();
		if(cessTaxRates == ""){
			cessTaxRates = "";
		}else if(parseFloat(cessTaxRates) > 100){
			swal("Invalid CESS Rate!", "CESS Rate cannot be more than 100%.", "error");
			flag = false;
			return false;
		}
		var rcmItemId = $(this).find('input[id="rcmItemId"]').val();
		if(rcmItemId == ""){
			$(this).find('input[id="rcmItem"]').val("");
			swal("Invalid expense item!", "Provide expense item.", "error");
			
			flag = false;
			return false;
		}
		var rcmItemName = $(this).find('input[id="rcmItem"]').val();
		if(rcmItemName == ""){
		    $(this).find('input[id="rcmItemId"]').val("");
			swal("Invalid expense item!", "Provide expense item.", "error");
			
			flag = false;
			return false;
		}
		
		jsonData.branch=rcmTaxBranch;
		jsonData.vendor=rcmTaxVendor;
		jsonData.typeOfSupply=rcmTypeOfSupply;
		jsonData.description = rcmDescription
		jsonData.hsnSacCode = hsnSacCode;
		jsonData.gstTaxRates = gstTaxRates;
		jsonData.cessTaxRates = cessTaxRates;
		jsonData.itemId = rcmItemId;
		multipleTaxData.push(JSON.stringify(jsonData));
	});
	if(flag) {
	var txnJsonData = {};
	txnJsonData.rcmTaxData = multipleTaxData;
	var url = '/tax/savercmtaxes';
	$.ajax({
		url         : url,
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		data        : JSON.stringify(txnJsonData),
		contentType : 'application/json',
		success     : function (data) {
			resetRcmTaxScreen();
			$("#rcmTaxTab").removeClass("active");
			$("#rcmTaxTab").find(".taxsetuptabCls").click();
		},
		error : function (xhr, status, error) {
			if(xhr.status == 401){ 
				doLogout(); 
			}else if(xhr.status == 500){
	    		swal("Error on storing input taxes!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$("#rcmTaxTable > tbody > tr").each(function(){
				$(this).find('input[id="rcmItemId"]').val("");
				$(this).find('input[id="rcmItem"]').val("");
			});
			$.unblockUI();
		}
	});
	}
}

var resetRcmTaxScreen = function(){
	$("#rcmTaxTable tbody").html("");
	$("#addMoreRcmTaxes").click();
	$("#newRcmTaxDiv").hide();
	$('#rcmTaxBranch').find('option:first').prop("selected","selected");
	$('#rcmTaxVendor').find('option:first').prop("selected","selected");
	$('#rcmTypeOfSupply').find('option:first').prop("selected","selected");
}

var showNewRcmTax = function(elem){
	var value = $(elem).val();
	resetTaxTableLength('rcmTaxTable');
	
	$("#newRcmTaxDiv").hide();	
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var url = '/tax/showrcmtaxes';
	$.ajax({
		url         : url,
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "GET",
		contentType : 'application/json',
		success     : function (data) {
			var taxList = "";
			for(var i=0; i<data.itemTaxList.length; i++){
				taxList += '<tr id="'+data.itemTaxList[i].id+'"><td>'+data.itemTaxList[i].date+'</td><td>'+data.itemTaxList[i].branch+'</td><td>'+data.itemTaxList[i].vendor+'</td><td>'+data.itemTaxList[i].supplyType+'</td><td>'+data.itemTaxList[i].desc+'</td><td>'+data.itemTaxList[i].hsnsac+'</td><td>'+data.itemTaxList[i].taxname+'</td><td>'+data.itemTaxList[i].rate+'</td><td>'+data.itemTaxList[i].itemName+'</td></tr>';
				//<td><button id="editRcmTax" onclick="editRcmTax(this);" class="btn btn-submit" title="Edit">Edit</button><input type="hidden" class="branchSpecificsTaxFormulaId" value="'+data.itemTaxList[i].branchSpecificsTaxFormulaId+'" /></td>
	    	}
	    	$("#rcmTaxTableHistory tbody").html('');
	    	$("#rcmTaxTableHistory tbody").append(taxList);
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
	
}

function split( val ) {
  return val.split( /,\s*/ );
}
function extractLast( term ) {
  return split( term ).pop();
}

function editRcmTax(elem) {
	var rcmId = $.trim($(elem).closest('tr').attr("id"));
	var rcmTaxFormulaId = $.trim($(elem).closest('tr').find(".branchSpecificsTaxFormulaId").val());
	
	if(rcmId != "" && rcmTaxFormulaId != "") {
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var txnJsonData = {};
	txnJsonData.rcmId = rcmId;
	xnJsonData.rcmTaxFormulaId = rcmTaxFormulaId;
	
	var url = '/tax/getrcmtaxes';
	$.ajax({
		url         : url,
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method      : "POST",
		data        : JSON.stringify(txnJsonData),
		contentType : 'application/json',
		success     : function (data) {
			
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
	$("#rcmTaxTab").find(".taxsetuptabCls").click();
	$("#rcmTaxTable tbody").append(trdata);
	}
}

$(function(){
	$("#applicableDate").datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
});
