$(document).ready(function(){
	$(".mainBranchIncomeChartOfAccount").treeview({
		animated: "fast",
		collapsed: true,
		unique: true,
		toggle: function() {
		}
	});

	$(".confirmBtn").confirm({
	    text: "Are you sure you want to reset all taxes? <br> Reset will erase ALL taxes set-up for this income item.<br>Please click on OK to continue ",
	    title: "Confirmation required",
	    confirm: function(button) {
	        resetAllTaxes();
	    },
	    cancel: function(button) {
	        // nothing to do
	    },
	    confirmButton: "Ok",
	    cancelButton: "Cancel",
	    post: true,
	    confirmButtonClass: "btn-danger",
	    cancelButtonClass: "btn-default",
	    dialogClass: "modal-dialog " // Bootstrap classes for large modal
	});
});


var resetAllTaxes = function(){
	var length=$("#gstTaxRuleDetailsTable tbody tr").length;
	for(var i=1;i<=length;i++){
		
		$("tr[id='"+i+"'] input[id='taxFormula']").val("");
		$("tr[id='"+i+"'] input[id='taxInvoiceValue']").val("");
		$("tr[id='"+i+"'] select[id='taxApplyTo'] option:first").prop("selected","selected");
		$("tr[id='"+i+"'] select[class='taxNamerate']").each(function(){
			$(this).find('option:first').prop("selected","selected");
		});
		$("tr[id='"+i+"'] select[id='taxAddDeduct'] option:first").prop("selected","selected");
		$("#"+i+" input[id='rowCheckBox']").prop('checked',false);
		$("#"+i+" input[id='rowCheckBox']").removeAttr("disabled");
		//$("#taxRuleDetailsTable tbody tr[id="+i+"] input[id='branchIncomeCoaHidPrimKey']").val("");
	}
}

function toggleApplyTo(elem){
	var trId=$(elem).parent().parent('tr:first').attr('id');
	$("tr[id='"+trId+"'] select[id='applyTo'] option:first").prop("selected","selected");
	$("tr[id='"+trId+"'] input[id='formula']").val("");
	$("#"+trId+" input[id='rowCheckBox']").prop('checked',false);
}

function toggleTaxNameRate(elem){
	var trId=$(elem).parent().parent('tr:first').attr('id');
	var value=$(elem).val();
	$("tr[id='"+trId+"'] select[class='namerate']").each(function(){
		$(this).find('option[value="'+value+'"]').prop("selected","selected");
	});
	$("tr[id='"+trId+"'] select[id='applyTo'] option:first").prop("selected","selected");
	$("tr[id='"+trId+"'] input[id='formula']").val("");
	$("#"+trId+" input[id='rowCheckBox']").prop('checked',false);
}

function validateTaxationRule(elem){
	var trId=$(elem).parent().parent('tr:first').attr('id');
	var prevTr=trId-1;
	var element=$("tr[id='"+prevTr+"']").attr('id');
	if(typeof(element)!='undefined'){
		console.log($("#"+prevTr+" input[id='rowCheckBox']").is(':checked'));
		if(!($("#"+prevTr+" input[id='rowCheckBox']").is(':checked'))){
			$(elem).find('option:first').prop("selected","selected");
		}else{
			var curRow="";var curIVRow="";var totalTaxes="";
			if(trId=="1"){
				//curRow="B";prevRow="A";
				curRow="Tax1";curIVRow="IV1";totalTaxes="(Tax1)"
			}
			if(trId=="2"){
				//curRow="C";prevRow="B";
				curRow="Tax2";curIVRow="IV2";totalTaxes="(Tax1+Tax2)"
			}
			if(trId=="3"){
				//curRow="D";prevRow="C";
				curRow="Tax3";curIVRow="IV3";totalTaxes="(Tax1+Tax2+Tax3)"
			}
			if(trId=="4"){
				//curRow="E";prevRow="D";
				curRow="Tax4";curIVRow="IV4";totalTaxes="(Tax1+Tax2+Tax3+Tax4)"
			}
			if(trId=="5"){
				//curRow="F";prevRow="E";
				curRow="Tax5";curIVRow="IV5";totalTaxes="(Tax1+Tax2+Tax3+Tax4+Tax5)"
			}
			/*if(trId=="6"){
				curRow="G";prevRow="F";
			}*/
			var taxType=$("tr[id='"+trId+"'] select[id='taxType']").val();
			var taxRate=$("tr[id='"+trId+"'] select[id='taxRate']").val();
			var addDeduct=$("tr[id='"+trId+"'] select[id='addDeduct']").val();
			var applyTo=$("tr[id='"+trId+"'] select[id='applyTo']").val();
			if(taxType!="" && taxRate!="" && addDeduct!="" && applyTo!=""){
				/*var formula=prevRow;
				if(addDeduct==1){
					formula+='+';
				}
				if(addDeduct==0){
					formula+='-';
				}
				formula+="[(R/100)*"+applyTo+"]";
				var invoiceValue= GV + prevRow;
				var R="";
				*/
				//formula=curRow+"="+formula;

				var formula = curRow + "=" + applyTo + "*(Rate/100)";
				var invoiceValue="GV";
				if(addDeduct==1){
					invoiceValue+='+';
				}
				if(addDeduct==0){
					invoiceValue+='-';
				}
				invoiceValue= curIVRow + "=" + invoiceValue + totalTaxes;
				$("tr[id='"+trId+"'] input[id='formula']").val(formula);
				$("tr[id='"+trId+"'] input[id='invoiceValue']").val(invoiceValue);
				$("#"+trId+" input[id='rowCheckBox']").prop('checked',true);
			}else{
				$("tr[id='"+trId+"'] input[id='formula']").val("");
				$("tr[id='"+trId+"'] input[id='invoiceValue']").val("");
				$("#"+trId+" input[id='rowCheckBox']").prop('checked',false);
			}
		}
	}else{
		var curRow="";var curIVRow="";var totalTaxes="";
		if(trId=="1"){
			//curRow="B";prevRow="A";
			curRow="Tax1";curIVRow="IV1";totalTaxes="(Tax1)"
		}
		if(trId=="2"){
			//curRow="C";prevRow="B";
			curRow="Tax2";curIVRow="IV2";totalTaxes="(Tax1+Tax2)"
		}
		if(trId=="3"){
			//curRow="D";prevRow="C";
			curRow="Tax3";curIVRow="IV3";totalTaxes="(Tax1+Tax2+Tax3)"
		}
		if(trId=="4"){
			//curRow="E";prevRow="D";
			curRow="Tax4";curIVRow="IV4";totalTaxes="(Tax1+Tax2+Tax3+Tax4)"
		}
		if(trId=="5"){
			curRow="F";prevRow="E";
			curRow="Tax5";curIVRow="IV5";totalTaxes="(Tax1+Tax2+Tax3+Tax4+Tax5)"
		}
		/*if(trId=="6"){
			curRow="G";prevRow="F";
		}*/
		var taxType=$("tr[id='"+trId+"'] select[id='taxType']").val();
		var taxRate=$("tr[id='"+trId+"'] select[id='taxRate']").val();
		var addDeduct=$("tr[id='"+trId+"'] select[id='addDeduct']").val();
		var applyTo=$("tr[id='"+trId+"'] select[id='applyTo']").val();

		if(taxType!="" && taxRate!="" && addDeduct!="" && applyTo!=""){
			/*var formula=prevRow;
			if(addDeduct==1){
				formula+='+';
			}
			if(addDeduct==0){
				formula+='-';
			}
			formula+="[(R/100)*"+applyTo+"]";
			var R="";
			*/
			var formula = curRow + "=" + applyTo + "*(Rate/100)";
			var invoiceValue="GV";
			if(addDeduct==1){
				invoiceValue+='+';
			}
			if(addDeduct==0){
				invoiceValue+='-';
			}
			//formula=curRow+"="+formula;
			invoiceValue= curIVRow + "=" + invoiceValue + totalTaxes;
			$("tr[id='"+trId+"'] input[id='formula']").val(formula);
			$("tr[id='"+trId+"'] input[id='invoiceValue']").val(invoiceValue);
			$("#"+trId+" input[id='rowCheckBox']").prop('checked',true);
		}else{
			$("tr[id='"+trId+"'] input[id='formula']").val("");
			$("tr[id='"+trId+"'] input[id='invoiceValue']").val("");
			$("#"+trId+" input[id='rowCheckBox']").prop('checked',false);
		}
	}
}

function getChildIncomesChartOfAccount(elem, gstCountryCode){
	var listDisplayableLi=$(elem).parent().parent('li:first').attr('id');
	var origEntityId=listDisplayableLi.substring(12, listDisplayableLi.length);	
	$(elem).attr('src',"/assets/images/minus.png");
	$(elem).attr('onclick',"javascript:removeChildIncomesChartOfAccount(this,'"+gstCountryCode+"')");
	var useremail=$("#hiddenuseremail").text();
	var jsonData = {};
	jsonData.usermail = useremail;
	jsonData.branchId = origEntityId;
	//var url = "/config/getBranchIncomesCoa";
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$.ajax({
		url : '/data/getcoaincomeitemsWithTaxRules',
		data : JSON.stringify(jsonData),
		type : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method : "POST",
		contentType : 'application/json',
		success : function(data) {
			if(data.incomeItemData.length>0){
				$("li[id='"+listDisplayableLi+"']").append('<ul id="mainBranchIncomeChartOfAccount" class="treeview-black mainBranchIncomeChartOfAccount"></ul>');
				var incomeList = "";
				for(var i=0;i<data.incomeItemData.length;i++){
					if(data.incomeItemData[i].isTaxSetup=="Yes"){						
						incomeList += ('<li><div class="chartOfAccountContainer"><p class="color-grey"><img src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><b style="margin-left:2px;color:red;">'+data.incomeItemData[i].name+'</b><button style="float:right" id="newTaxRuleform-container" class="newEntityCreateButton btn btn-submit btn-idos" title="Create/Update Tax Rule"');
					}else{
						incomeList += ('<li><div class="chartOfAccountContainer"><p class="color-grey"><img src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><b style="margin-left:2px;">'+data.incomeItemData[i].name+'</b><button style="float:right" id="newTaxRuleform-container" class="newEntityCreateButton btn btn-submit btn-idos" title="Create/Update Tax Rule"');
					}
					if(gstCountryCode !== ""){
						incomeList += (' onclick="createGstTaxRule(this,'+origEntityId+','+data.incomeItemData[i].id+',2);"><i class="fa fa-plus pr-5"></i>Add/Update GST Tax Rule</button></p></li>');
					}else{
						incomeList += (' onclick="createTaxRule(this,'+origEntityId+','+data.incomeItemData[i].id+');"><i class="fa fa-plus pr-5"></i>Add/Update Tax Rule</button></p></li>');
					}
					
				}
				
				$("li[id='"+listDisplayableLi+"']").find("ul[id='mainBranchIncomeChartOfAccount']").append(incomeList);
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

function removeChildIncomesChartOfAccount(elem, gstCountryCode){
	var listDisplayableLi=$(elem).parent().parent('li:first').attr('id');
	$("li[id='"+listDisplayableLi+"']").find("ul[id='mainBranchIncomeChartOfAccount']").remove();
	$(elem).attr('src',"/assets/images/new.v1370889834.png");
	$(elem).attr('onclick',"javascript:getChildIncomesChartOfAccount(this,'"+gstCountryCode+"')");
}

function removeChildExpensesChartOfAccount(elem, gstCountryCode){
	var listDisplayableLi=$(elem).parent().parent('li:first').attr('id');
	$("li[id='"+listDisplayableLi+"']").find("ul[id='mainBranchIncomeChartOfAccount']").remove();
	$(elem).attr('src',"/assets/images/new.v1370889834.png");
	$(elem).attr('onclick',"javascript:getChildExpensesChartOfAccount(this,'"+gstCountryCode+"')");
}

function getChildExpensesChartOfAccount(elem, gstCountryCode){
	var listDisplayableLi=$(elem).parent().parent('li:first').attr('id');
	var origEntityId=listDisplayableLi.substring(12, listDisplayableLi.length);
	$(elem).attr('src',"/assets/images/minus.png");
	$(elem).attr('onclick',"javascript:removeChildExpensesChartOfAccount(this,'"+gstCountryCode+"')");
	var useremail=$("#hiddenuseremail").text();
	var jsonData = {};
	jsonData.usermail = useremail;
	jsonData.branchId = origEntityId;
	//var url = "/config/getBranchIncomesCoa";
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$.ajax({
		url : '/data/getcoaexpenseitems',
		data : JSON.stringify(jsonData),
		type : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method : "GET",
		contentType : 'application/json',
		success : function(data) {
			if(data.coaItemData.length>0){
				$("li[id='"+listDisplayableLi+"']").append('<ul id="mainBranchIncomeChartOfAccount" class="treeview-black mainBranchIncomeChartOfAccount"></ul>');
				var expenseList = "";
				for(var i=0;i<data.coaItemData.length;i++){
					expenseList += ('<li><div class="chartOfAccountContainer"><p class="color-grey"><img src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><b style="margin-left:2px;">'+data.coaItemData[i].name+'</b><button style="float:right" id="newTaxRuleform-container" class="newEntityCreateButton btn btn-submit btn-idos" title="Create/Update Tax Rule"');
					if(gstCountryCode !== ""){
						expenseList += (' onclick="createGstTaxRule(this,'+origEntityId+','+data.coaItemData[i].id+',1);"><i class="fa fa-plus pr-5"></i>Add/Update GST Tax Rule</button></p></li>');
					}else{
						expenseList += (' onclick="createTaxRule(this,'+origEntityId+','+data.coaItemData[i].id+');"><i class="fa fa-plus pr-5"></i>Add/Update Tax Rule</button></p></li>');
					}
				}
				$("li[id='"+listDisplayableLi+"']").find("ul[id='mainBranchIncomeChartOfAccount']").append(expenseList);
			}
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){
				doLogout(); 
			}else if(xhr.status == 500){
	    		swal("Error on fetching Expense Items!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}


function createTaxRule(elem,branchId,specificsId){
	var useremail=$("#hiddenuseremail").text();
	var jsonData = {};
	jsonData.usermail = useremail;
	jsonData.branchPrimaryId = branchId;
	jsonData.specificsPrimId=specificsId;
	var url = "/tax/getBranchTaxes";
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
			$("#taxRuleDetailsTable tbody").html("");
			if(data.branchTaxes.length>0){
				$("#itemDisplay").text(data.branchTaxes[0].specificsname);
				for(var i=0;i<data.branchTaxes.length;i++){
					var j=i+1;
					$("#taxRuleDetailsTable tbody").append('<tr id="'+j+'"><td><input type="hidden" name="branchIncomeCoaHidPrimKey" id="branchIncomeCoaHidPrimKey" value=""><input type="checkbox" value="Tax'+j+'" name="rowCheckBox" id="rowCheckBox" onchange="changeTaxFormulaStatus(this);">Tax'+j+'</input></td><td><select class="namerate" name="taxType" id="taxType" onchange="toggleTaxNameRate(this);">'+
					'<option value="">--Please Select--</option></select><td><select class="namerate" name="taxRate" id="taxRate" onchange="toggleTaxNameRate(this)"><option value="">--Please Select--</option></select></td>'+
					'<td><select name="addDeduct" id="addDeduct" onchange="toggleApplyTo(this);"><option value="">--Please Select--</option><option value="1">Add</option><option value="0">Deduct</option></select></td>'+
					'<td><select name="applyTo" id="applyTo" onchange="javascript:validateTaxationRule(this);"><option value="">--Please Select--</option></select></td><td><input type="text" name="formula" id="formula" readonly="readonly"></td><td><input type="text" name="invoiceValue" id="invoiceValue" readonly="readonly"></td></tr>');
					for(var k=0;k<data.branchTaxes.length;k++){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='taxType']").append('<option value="'+data.branchTaxes[k].id+'">'+data.branchTaxes[k].name+'</option>');
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='taxRate']").append('<option value="'+data.branchTaxes[k].id+'">'+data.branchTaxes[k].rate+'</option>');
					}
					if(j==1){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						//$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option>');
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option>');
					}else if(j==2){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						//$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option><option value="B">B</option>');
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option><option value="Tax1">Tax1</option><option value="IV1">IV1</option>');
					} else if(j==3){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option><option value="Tax1">Tax1</option><option value="Tax2">Tax2</option><option value="IV2">IV2</option>');
						//$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option><option value="B">B</option><option value="C">C</option>');
					} else if(j==4){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option><option value="Tax1">Tax1</option><option value="Tax2">Tax2</option><option value="Tax3">Tax3</option><option value="IV3">IV3</option>');
					} else if(j==5){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option><option value="Tax1">Tax1</option><option value="Tax2">Tax2</option><option value="Tax3">Tax3</option><option value="Tax4">Tax4</option><option value="IV4">IV4</option>');
					}
					/*if(j==4){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option><option value="B">B</option><option value="C">C</option><option value="D">D</option>');
					}
					if(j==5){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option><option value="B">B</option><option value="C">C</option><option value="D">D</option><option value="E">E</option>');
					}
					if(j==6){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option><option value="B">B</option><option value="C">C</option><option value="D">D</option><option value="E">E</option><option value="F">F</option>');
					}
					if(j==7){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option><option value="B">B</option><option value="C">C</option><option value="D">D</option><option value="E">E</option><option value="F">F</option><option value="G">G</option>');
					}*/
				}
				for(var i=0;i<data.branchSpecfTaxesFormula.length;i++){
					var j=i+1;
					$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='branchIncomeCoaHidPrimKey']").val(data.branchSpecfTaxesFormula[i].id);
					if(data.branchSpecfTaxesFormula[i].status === 1){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='rowCheckBox']").prop("checked","checked");
						$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='rowCheckBox']").prop("disabled",true);
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='taxType'] option").filter(function () {return $(this).val()==data.branchSpecfTaxesFormula[i].branchTaxId;}).prop("selected", "selected");
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='taxRate'] option").filter(function () {return $(this).val()==data.branchSpecfTaxesFormula[i].branchTaxId;}).prop("selected", "selected");
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='addDeduct'] option").filter(function () {return $(this).val()==data.branchSpecfTaxesFormula[i].addDeduct;}).prop("selected", "selected");
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option").filter(function () {return $(this).val()==data.branchSpecfTaxesFormula[i].appliedTo;}).prop("selected", "selected");
						$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='formula']").val(data.branchSpecfTaxesFormula[i].formula);
						$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='invoiceValue']").val(data.branchSpecfTaxesFormula[i].invoiceValue);
					}
				}
			}
			$(".newTaxRuleform-container").slideDown('slow');
			$("a[id='newTaxRuleform-container-close']").attr("href",location.hash);
		},
		error : function() {

		}
	});
}

function selectAllIncomeItemsForTaxSetup(){
	$('#multiItemsTaxesList li').each(function() {
		var item = $(this);
		$(this).find('input[type="checkbox"]').prop('checked','checked');
		});		
	}

function applyTaxRulesToMultipleItems(){
	var parentTr=$(this).closest('div').attr('id');
	var parentOfparentTr = $(this).parents().closest('tr').attr('id');
	var useremail=$("#hiddenuseremail").text();	
	var jsonData = {};
	jsonData.usermail = useremail;
	//var url ="/config/getBranchIncomesCoa";
	var url = "/data/getcoaincomeitemsWithTaxRules";
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
			if(data.incomeItemData.length>0){		
				
				for(var i=0;i<data.incomeItemData.length;i++){	
					if(data.incomeItemData[i].isTaxSetup == "No"){
						$("#staticTaxesForMultiItems div[class='modal-body']").find("ul[id='multiItemsTaxesList']").append('<li style="text-align: left;"><input id="'+data.incomeItemData[i].id+'" value="'+data.incomeItemData[i].id+'" type="checkbox">'+data.incomeItemData[i].name+'</b></li>');
					}
					
				}				
			}    			
    		$("#staticTaxesForMultiItems div[class='modal-body'] ").append('</ul><input type="button" style="width:60px;" id="submitInputTax" name="submitInputTax" class="btn btn-submit" value="Submit" onclick="submitMultipleItemsTaxRules(this,\''+parentTr+'\',\''+parentOfparentTr+'\');"></div>');		
		},
		error : function() {

		}
	});
}

function submitMultipleItemsTaxRules(elem,parentTr,parentOfparentTr){	
	$("#staticTaxesForMultiItems").modal('hide');	
	var allVals = [];
	$('#'+parentOfparentTr+' .itemsSelect :checked').each(function() {
		allVals.push($(this).val());
	  });
	if(allVals[0] == "Select All")
		allVals.shift();
	$("#multiItemsListHidden").val(allVals);	 
}
/*function createTaxRule(elem,branchId,specificsId){
	var useremail=$("#hiddenuseremail").text();
	var jsonData = {};
	jsonData.usermail = useremail;
	jsonData.branchPrimaryId = branchId;
	jsonData.specificsPrimId=specificsId;
	var url = "/tax/getBranchTaxes";
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
			$("#taxRuleDetailsTable tbody").html("");
			if(data.branchTaxes.length>0){
				$("#itemDisplay").text(data.branchTaxes[0].specificsname);
				for(var i=0;i<data.branchTaxes.length;i++){
					var j=i+1;
					$("#taxRuleDetailsTable tbody").append('<tr id="'+j+'"><td><input type="hidden" name="branchIncomeCoaHidPrimKey" id="branchIncomeCoaHidPrimKey" value=""><input type="checkbox" value="Tax'+j+'" name="rowCheckBox" id="rowCheckBox" >Tax'+j+'</input></td><td><select class="namerate" name="taxType" id="taxType" onchange="toggleTaxNameRate(this);">'+
					'<option value="">--Please Select--</option></select><td><select class="namerate" name="taxRate" id="taxRate" onchange="toggleTaxNameRate(this)"><option value="">--Please Select--</option></select></td>'+
					'<td><select name="addDeduct" id="addDeduct" onchange="toggleApplyTo(this);"><option value="">--Please Select--</option><option value="1">Add</option><option value="0">Deduct</option></select></td>'+
					'<td><select name="applyTo" id="applyTo" onchange="javascript:validateTaxationRule(this);"><option value="">--Please Select--</option></select></td><td><input type="text" name="formula" id="formula" readonly="readonly"></td><td><input type="text" name="invoiceValue" id="invoiceValue" readonly="readonly"></td></tr>');
					for(k=0;k<data.branchTaxes.length;k++){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='taxType']").append('<option value="'+data.branchTaxes[k].id+'">'+data.branchTaxes[k].name+'</option>');
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='taxRate']").append('<option value="'+data.branchTaxes[k].id+'">'+data.branchTaxes[k].rate+'</option>');
					}
					if(j==1){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						//$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option>');
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option>');
					}
					if(j==2){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						//$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option><option value="B">B</option>');
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option><option value="Tax1">Tax1</option><option value="IV1">IV1</option>');
					}
					if(j==3){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option><option value="Tax1">Tax1</option><option value="Tax2">Tax2</option><option value="IV2">IV2</option>');
						//$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="A">A</option><option value="B">B</option><option value="C">C</option>');
					}
					if(j==4){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option><option value="Tax1">Tax1</option><option value="Tax2">Tax2</option><option value="Tax3">Tax3</option><option value="IV3">IV3</option>');
					}
					if(j==5){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option[value!='']").remove();
						$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo']").append('<option value="GV">Gross Value(GV)</option><option value="Tax1">Tax1</option><option value="Tax2">Tax2</option><option value="Tax3">Tax3</option><option value="Tax4">Tax4</option><option value="IV4">IV4</option>');
					}
					
				}
				for(var i=0;i<data.branchSpecfTaxesFormula.length;i++){
					var j=i+1;
					$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='branchIncomeCoaHidPrimKey']").val(data.branchSpecfTaxesFormula[i].id);
					if(data.branchSpecfTaxesFormula[i].status == "1"){
						$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='rowCheckBox']").prop("checked","checked");
					}
					$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='rowCheckBox']").attr("class", data.branchSpecfTaxesFormula[i].id); 
					$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='taxType'] option").filter(function () {return $(this).val()==data.branchSpecfTaxesFormula[i].branchTaxId;}).prop("selected", "selected");
					$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='taxRate'] option").filter(function () {return $(this).val()==data.branchSpecfTaxesFormula[i].branchTaxId;}).prop("selected", "selected");
					$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='addDeduct'] option").filter(function () {return $(this).val()==data.branchSpecfTaxesFormula[i].addDeduct;}).prop("selected", "selected");
					$("#taxRuleDetailsTable tbody tr[id="+j+"] select[id='applyTo'] option").filter(function () {return $(this).val()==data.branchSpecfTaxesFormula[i].appliedTo;}).prop("selected", "selected");
					$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='formula']").val(data.branchSpecfTaxesFormula[i].formula);
					$("#taxRuleDetailsTable tbody tr[id="+j+"] input[id='invoiceValue']").val(data.branchSpecfTaxesFormula[i].invoiceValue);
				}
			}
			$(".newTaxRuleform-container").slideDown('slow');
			$("a[id='newTaxRuleform-container-close']").attr("href",location.hash);
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}
*/
$(document).ready(function(){
	$('.applyTax').click(function(){
		var branch_tax_formula_hiddenKeys= $('input[name="branchIncomeCoaHidPrimKey"]').map(function () {
		    return this.value;
		}).get();
		var check_box_values = $('input[name="rowCheckBox"]:checkbox').map(function () {
		    return this.value;
		}).get();

		/*
		var check_box_checked = $('input[name="rowCheckBox"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();

		if(check_box_checked.length==0){
			swal("No Tax selected!","Please apply tax rules for the income item.", "error");
			return true;
		}*/
		//console.log(">>" + check_box_values);

		var tax_type_values= $('select[name="taxType"] option:selected').map(function () {
		    return this.value;
		}).get();
		var add_deduct_values= $('select[name="addDeduct"] option:selected').map(function () {
		    return this.value;
		}).get();
		var rate_applied_to= $('select[name="applyTo"] option:selected').map(function () {
		    return this.value;
		}).get();
		var tax_formula= $('input[name="formula"]').map(function () {
		    return this.value;
		}).get();
		var invoice_value= $('input[name="invoiceValue"]').map(function () {
		    return this.value;
		}).get();
		var bnchTaxFmHid="";var taxTypes="";var addsDeducts="";var appliedTos="";var formulas="";var invoiceValues="";
		for(var i=0;i<branch_tax_formula_hiddenKeys.length;i++){
			bnchTaxFmHid+=branch_tax_formula_hiddenKeys[i]+",";
	    }

		for(var i=0;i<check_box_values.length;i++){
			taxTypes+=tax_type_values[i]+",";
			addsDeducts+=add_deduct_values[i]+",";
			appliedTos+=rate_applied_to[i]+",";
			formulas+=tax_formula[i]+",";
			invoiceValues+=invoice_value[i]+",";
		}
		var jsonData={};
		jsonData.bnchTaxFmHid=bnchTaxFmHid.substring(0, bnchTaxFmHid.length-1);
		jsonData.applyRulesToMultiItemsList=$("#multiItemsListHidden").val();//set multiple items list to apply same tax rules
		jsonData.taxTypes=taxTypes.substring(0, taxTypes.length-1);
		jsonData.addsDeducts=addsDeducts.substring(0, addsDeducts.length-1);
		jsonData.appliedTos=appliedTos.substring(0, appliedTos.length-1);
		jsonData.formulas=formulas.substring(0, formulas.length-1);
		jsonData.invoiceValues=invoiceValues.substring(0, invoiceValues.length-1);
		jsonData.branchId=$("#bnchHidden").val();
		jsonData.specificsId=$("#specfHidden").val();
		jsonData.usermail = $("#hiddenuseremail").text();
		var url = "/tax/addBranchSpecificTaxRules";
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
			   $('.notify-success').show();
			   $("#notificationMessage").html("Successfully Applied Tax Rules for the branch income item.");
			   $(".newTaxRuleform-container").slideUp('slow');
			   $("a[id='newTaxRuleform-container-close']").attr("href",location.hash);
			},
		   	error: function (xhr, status, error) {
	    		if(xhr.status == 401){ doLogout(); 
			    }else if(xhr.status == 500){
		    		swal("Error on save/update tax!", "Please retry, if problem persists contact support team", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	});
});


/*
$(document).ready(function(){
	$('.applyTax').click(function(){
		var branch_tax_formula_hiddenKeys= $('input[name="branchIncomeCoaHidPrimKey"]').map(function () {
		    return this.value;
		}).get();
		var check_box_values = $('input[id="rowCheckBox"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		
		var checked_taxId = $('input[id="rowCheckBox"]:checkbox:checked').map(function () {
		    return $(this).attr("class");
		}).get();

		var cheked_ids = "";

		var unchecked_taxId = $('input[id="rowCheckBox"]:checkbox').map(function () {
			if($(this).is(':checked')) {
				cheked_ids +="," + $(this).attr("class"); 
		    }else{
		    	return $(this).attr("class");
		    }
		}).get();

		var rate_applied_to= $('select[name="applyTo"] option:selected').map(function () {
			if(this.value != ""){
				return this.value;
			}
		}).get();

		if(checked_taxId.length != rate_applied_to.length){
			alert("Please select apply tax.");
			return true;
		}
		
		var tax_type_values= $('select[name="taxType"] option:selected').map(function () {
		    return this.value;
		}).get();
		var add_deduct_values= $('select[name="addDeduct"] option:selected').map(function () {
		    return this.value;
		}).get();
		var tax_formula= $('input[name="formula"]').map(function () {
		    return this.value;
		}).get();
		var invoice_value= $('input[name="invoiceValue"]').map(function () {
		    return this.value;
		}).get();
		var bnchTaxFmHid="";var taxTypes="";var addsDeducts="";var appliedTos="";var formulas="";var invoiceValues=""; var checkedTaxIDList="";
		for(var i=0;i<branch_tax_formula_hiddenKeys.length;i++){
			bnchTaxFmHid+=branch_tax_formula_hiddenKeys[i]+",";
	    }
		for(var i=0;i<check_box_values.length;i++){
			taxTypes+=tax_type_values[i]+",";
			addsDeducts+=add_deduct_values[i]+",";
			appliedTos+=rate_applied_to[i]+",";
			formulas+=tax_formula[i]+",";
			invoiceValues+=invoice_value[i]+",";
			//checkedTaxIDList += checked_taxId[i] +",";
		}
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var jsonData={};
		jsonData.bnchTaxFmHid=bnchTaxFmHid.substring(0, bnchTaxFmHid.length-1);
		jsonData.checkedTaxIDList = checked_taxId;
		jsonData.uncheckedTaxIDList = unchecked_taxId;
		jsonData.taxTypes=taxTypes.substring(0, taxTypes.length-1);
		jsonData.addsDeducts=addsDeducts.substring(0, addsDeducts.length-1);
		jsonData.appliedTos=appliedTos.substring(0, appliedTos.length-1);
		jsonData.formulas=formulas.substring(0, formulas.length-1);
		jsonData.invoiceValues=invoiceValues.substring(0, invoiceValues.length-1);
		jsonData.branchId=$("#bnchHidden").val();
		jsonData.specificsId=$("#specfHidden").val();
		jsonData.usermail = $("#hiddenuseremail").text();
		var url = "/tax/addBranchSpecificTaxRules";
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
			   $('.notify-success').show();
	           $("#notificationMessage").html("Successfully Applied Tax Rules for the branch income item.");
			   $(".newTaxRuleform-container").slideUp('slow');
			   $("a[id='newTaxRuleform-container-close']").attr("href",location.hash);
		   },
		   error : function(xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
		   },
			complete: function(data) {
				$.unblockUI();
			}
		});
	});
});

*/

var changeTaxFormulaStatus = function(elem){
		var parentTr = $(elem).parent().parent().attr('id');
		if ($(this).is(':checked')) {
			console.log('Checked');
		} else {
			$("tr[id='"+parentTr+"'] select[class='namerate']").each(function(){
				$(this).find('option:first').prop("selected","selected");
				//$("tr[id='"+parentTr+"'] select[class='namerate'] option:first").prop("selected","selected");
			});
			$("tr[id='"+parentTr+"'] select[id='addDeduct'] option:first").prop("selected","selected");
			$("tr[id='"+parentTr+"'] select[id='applyTo'] option:first").prop("selected","selected");
			$("tr[id='"+parentTr+"'] input[id='formula']").val("");
			$("tr[id='"+parentTr+"'] input[id='invoiceValue']").val("");
		}
}

function validateTaxFormula(){
	var taxFormulas="";var taxRates="";
	var addDeducts="";var applyTos="";
	$("input[name='formula']").each(function(){
		if($(this).val()!=""){
			taxFormulas+=$(this).val()+",";
			taxRates+=$(this).parent().parent().find('select[name="taxRate"] option:selected').text()+",";
			addDeducts+=$(this).parent().parent().find('select[name="addDeduct"] option:selected').val()+",";
			applyTos+=$(this).parent().parent().find('select[name="applyTo"] option:selected').text()+",";
		}
	})
	if(taxFormulas!="" && taxRates!="" && addDeduct!="" && applyTo!=""){
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
				swal("INFO",output.substring(0,output.length-1).replace(/"/g, ''),"info");
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		 });
	}else{
		swal("Invalid!","Tax Formula is not configured.Please configure.","warning");
		return true;
	}
}
