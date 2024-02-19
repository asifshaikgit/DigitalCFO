// *********** GSTR2 Changes *******************

function addAdditionalDetailsForGSTR2(){
	var orgGstinValue = $("#karvyJSONData select[id='branchList']").find("option:selected").val();
	if(orgGstinValue == ''){
		swal("Select GSTIN!","please select a GSTIN before processing.","error");
		return false;
	}
	 $("#karvyGSTR2JSONAdditionalData").attr('data-toggle', 'modal');
	 $("#karvyGSTR2JSONAdditionalData").modal('show');
}

function selectReturnPeriodForGSTR2(elem){
	var option=$(elem).val();
	if(option==1){
		$("#quarterCalenderForGSTR2").hide();
		$("#dateRangeCalenderForGSTR2").hide();
		$("#monthlyCalenderForGSTR2").show();
	}else if(option==2){
		$("#monthlyCalenderForGSTR2").hide();
		$("#dateRangeCalenderForGSTR2").hide();
		$("#quarterCalenderForGSTR2").show();
	}else if(option==3){
		$("#monthlyCalenderForGSTR2").hide();
		$("#quarterCalenderForGSTR2").hide();
		$("#dateRangeCalenderForGSTR2").show();
	}else {
		$("#monthlyCalenderForGSTR2").hide();
		$("#quarterCalenderForGSTR2").hide();
		$("#dateRangeCalenderForGSTR2").show();
	}
}

function resetGSTR2() {
	$("#karvyGSTR2JSONAdditionalData").find('input[type="text"]').val("");
	$("#karvyGSTR2JSONAdditionalData").find('select').find('option:first').prop("selected","selected");
}

function getGSTR2TableData(){
	
	
	var gstIn=$("select[id='branchList'] option:selected ").text();
	var intervalType=$("#karvyGSTR2JSONAdditionalData select[id='returnIntervalForGSTR2'] option:selected ").val();
   var jsonType=$("#karvyGSTR2JSONAdditionalData select[id='jsonType'] option:selected ").val();
   
   if(intervalType == ''){
		swal("Incomplete Details!","please select Interval Type before processing.","error");
		return false;
	}  
	
   if(jsonType == ''){
	 swal("Incomplete Details!","please select JSON Type before processing.","error");
		return false;
	}
	var txtDate1="";
	var txtDate2="";
	var txtDate3="";
	var txtDate4="";
	var gstr3bDate="'"
	if(intervalType == 1){
    	txtDate1 = $("#txtDateGSTRMonth").val(); 
    }
		
	if(intervalType == 2){
    	txtDate2 = $("#txtDateGSTR2").val(); 
    }
	
	if(intervalType ==3){
    	txtDate3 = $("#txtDateGSTR21").val(); 
    	txtDate4 = $("#txtDateGSTR22").val(); 
    }
	
    var jsonData = {};  
 	 jsonData.gstIn = gstIn; 
    jsonData.txtDate1 = txtDate1;
    jsonData.txtDate2 = txtDate2;
    jsonData.txtDate3 = txtDate3;
    jsonData.txtDate4 = txtDate4;
    jsonData.intervalType = intervalType;
    jsonData.jsonType = jsonType;
    jsonData.useremail=$("#hiddenuseremail").text(); 
	 var url="/config/downloadKarvyGSTR2JSONSFileForKarvy";
    downloadFile(url, "POST", jsonData, "Error on downloading JSON Data!");
}