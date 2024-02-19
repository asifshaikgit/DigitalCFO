/**
 * 
 */

$(document).ready(function(){
	$('#taxableIncomeItemSelect').multiselect({
	    includeSelectAllOption: true,
	    enableFiltering :true,
	    enableCaseInsensitiveFiltering: true,
	    buttonWidth: '220px',
   		 maxHeight:   300,
	    onSelectAll: function() {
            var elemId = this.$select.context.id;
    	  	$("#taxableIncomeItems").children().remove();
        },
        onDeselectAll: function() {
            var elemId = this.$select.context.id;
            var $deselectOptions = $('#'+elemId+' option:enabled').clone();
		  		$('#taxableIncomeItems').append($deselectOptions);
        },
	    onChange: function(element, checked) {
			var elemId=$(element).context.offsetParent.id;
			var elemValue = $(element).val();
		  	var elemText = $(element).text();
			if(checked == true && elemValue != "multiselect-all") {
			  		$("#taxableIncomeItems option[value='"+ elemValue + "']").remove();
			 }else {
				 $("#taxableIncomeItems").append('<option value="'+elemValue+'">' +elemText+ '</option>');
			 }
	    }
	});
	
	var isCompositionSchemeApply = $("#isCompositionSchemeApply").val();
    if(isCompositionSchemeApply == 1) {
    	$('.taxsetupcls').hide();
    	$('#outputTaxTab').hide();
    	$('#inputTaxTab a').click();
    }else {
    	$('#taxableItemTab').hide();
    }
});

function displayTaxableItemsDetails() {
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	$.ajax({
		url: '/data/getcoaincomeitems',
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method: "GET",
		success: function(data) {
			
			var j = 0; var k=0;
			var optionSelectedData = [];
			var optionData = [];
			for (var i=0; i<data.coaItemData.length; i++) {
				if(data.coaItemData[i].taxableForComposition == 1){
					optionSelectedData[j++] = '<option value="'+data.coaItemData[i].id+'" selected>' +data.coaItemData[i].name+ '</option>';
				}else {
					optionData[k++] = '<option value="'+data.coaItemData[i].id+'">' +data.coaItemData[i].name+ '</option>';
				}
			}
            $("#taxableIncomeItemSelect").children().remove();
            $("#taxableIncomeItemSelect").append(optionSelectedData.join(''));
			$("#taxableIncomeItemSelect").append(optionData.join(''));
			$("#taxableIncomeItems").children().remove();
			$("#taxableIncomeItems").append(optionData.join(''));
			$('#taxableIncomeItemSelect').multiselect('rebuild');
		},
		error: function(xhr, errorMessage, error){
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
                swal("Error on fetching income items!", "Please retry, if problem persists contact support team", "error");
            }
		},
		complete: function(data) {
        }
	});
}

function saveTaxableItems() {
	
	var coaSelectedIds = [];
	var coaDeselectedIds = [];
	$('#taxableIncomeItemSelect :selected').each(function(i, selected){
		coaSelectedIds[i] = $(selected).val();
	});
	$('#taxableIncomeItems option').each(function(i, selected){
		coaDeselectedIds[i] = $(selected).attr("value");
	});
	if(coaSelectedIds.length > 0) {
		var jsonData={};
		jsonData.usermail=$("#hiddenuseremail").text();
		jsonData.coaSelectedIds = coaSelectedIds.join();
		jsonData.coaDeselectedIds = coaDeselectedIds.join();
		var url="/tax/taxableItemSave";
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
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
				swal("Taxable Items", "Saved", "success"); 
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	}
	
}
