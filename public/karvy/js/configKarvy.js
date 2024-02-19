/*----------------------Sunil's Code Start ------------------*/

//var transactionTypeList = '<li class="transactioTypeListCls"><input style="margin-bottom:5px;" type="checkbox" class="selectTransactionCls" value="" onClick="checkUncheckAllTransactionTypes(this,\'transactionTypeList\');">&nbsp;&nbsp;&nbsp;Select All</li>';
var downloadGSTR3BJSONData = function(){
	var orgGstin = $("#karvyJSONData select[id='branchList']").find("option:selected").text();
	var orgGstinValue = $("#karvyJSONData select[id='branchList']").find("option:selected").val();
	if(orgGstinValue === ''){
		swal("Invalid!","please select a GSTIN before processing.","error");
		return false;
	}

	/*var gstr3bDate = $("#karvyJSONData input[id='txtDate2']").val();
	if(gstr3bDate === ''){
		alert("please provide GSTR3B date before processing.");
		return false;
	}
	var length = gstr3bDate.indexOf(' ');
	var gstrMonth = gstr3bDate.substring(0, length);
	var gstrYear = gstr3bDate.substring(length+1);
	*/
	$("#karvyGSTR3BJSONData").attr('data-toggle', 'modal');
    $("#karvyGSTR3BJSONData").modal('show');
    $("#karvyGSTR3BJSONData input[id='orgGstin']").val(orgGstin);
   // $("#karvyGSTR3BJSONData input[id='gstrMonth']").val(gstrMonth);
	//$("#karvyGSTR3BJSONData input[id='gstrYear']").val(gstrYear);
	$("#karvyGSTR3BJSONData input[id='gstrOrgName']").val($("#companyName").val());
}
function getGSTR3BTABLEDATA(){
	
	
	var gstIn=$("select[id='branchList'] option:selected ").text();
	var intervalType=$("#karvyGSTR3BJSONAdditionalData select[id='returnIntervalForGSTR3B'] option:selected ").val();
	/*$("#OTS input[id='totalTaxableA']").val("");
	 $("#OTS input[id='integratedTaxA']").val("");
     $("#OTS input[id='centralTaxA']").val("");
     $("#OTS input[id='stateTaxA']").val("");
     $("#OTS input[id='cessTaxA']").val("");
     
     
     $("#OTSZR input[id='totalTaxableA']").val("");
     $("#OTSZR input[id='integratedTaxA']").val("");
     $("#OTSZR input[id='cessTaxA']").val("");
     
     $("#ISRC input[id='integratedTax']").val("");
     $("#ISRC input[id='centralTax']").val("");
     $("#ISRC input[id='stateTax']").val("");
     $("#ISRC input[id='cessTax']").val("");
     
     $("#ISLRC input[id='totalTaxableA']").val("");
     $("#ISLRC input[id='integratedTaxA']").val("");
     $("#ISLRC input[id='centralTaxA']").val("");
     $("#ISLRC input[id='stateTaxA']").val("");
     $("#ISLRC input[id='cessTaxA']").val("");
     
     $("#NGOS input[id='totalTaxableA']").val("");
     
     $("#TOIR input[id='totalTaxable']").val("");
     $("#TOIR input[id='integratedTax']").val("");
     $("#TOIR input[id='centralTax']").val("");
     $("#TOIR input[id='stateTax']").val("");
     $("#TOIR input[id='cessTax']").val("");
     
     $("#IMPG input[id='integratedTax']").val("");
     
     $("#IMPG input[id='cessTax']").val("");
     
     $("#IMPS input[id='integratedTax']").val("");
     $("#IMPS input[id='cessTax']").val("");
    
     $("#RUL2 input[id='integratedTaxD']").val("");
     $("#RUL2 input[id='centralTaxD']").val("");
     $("#RUL2 input[id='stateTaxD']").val("");
     $("#RUL2 input[id='cessTaxD']").val("");
   
     $("#GST input[id='integratedTaxA']").val("");
     $("#GST input[id='cessTaxA']").val("");
     $("#NONGST input[id='integratedTaxA']").val("");
     $("#NONGST input[id='cessTaxA']").val("");
     $("#TEX input[id='integratedTaxTotal']").val("");
     $("#TEX input[id='cessTaxTotal']").val("");
     
     $("#OTH1 input[id='integratedTax']").val("");
     $("#OTH1 input[id='centralTax']").val("");
     $("#OTH1 input[id='stateTax']").val("");
     $("#OTH1 input[id='cessTax']").val("");
     $("#NETITC input[id='integratedTaxC']").val("");
     $("#NETITC input[id='centralTaxC']").val("");
     $("#NETITC input[id='stateTaxC']").val("");
     $("#NETITC input[id='cessTaxC']").val("");
     */
	$("#karvyGSTR3BJSONData").find('input[type="text"]').val("");
	$("#karvyGSTR3BJSONData").find('select').find('option:first').prop("selected","selected");

     
	var txtDate1="";
	var txtDate2="";
	var txtDate3="";
	var txtDate4="";
	var gstr3bDate="'"
	if(intervalType==1){
    	var txtDate1=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B1']").val(); 
    	gstr3bDate=txtDate1;
    }
	
	if(intervalType==2){
    	var txtDate2=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B3']").val();
    	gstr3bDate=txtDate2
    }
	
	if(intervalType==3){
    	var txtDate3=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B2']").val();
    	var txtDate4=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B4']").val();
    	gstr3bDate=txtDate3;
    }
	
    var jsonData = {};   
    jsonData.txtDate1=txtDate1;
    jsonData.txtDate2=txtDate2;
    jsonData.txtDate3=txtDate3;
    jsonData.txtDate4=txtDate4;
    jsonData.intervalType=intervalType;
    jsonData.useremail=$("#hiddenuseremail").text(); 
    jsonData.gstIn=gstIn;
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    var url="/config/getGSTR3BDataForKarvy";
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
        var orgGstin = $("#karvyJSONData select[id='branchList']").find("option:selected").text();
     	var orgGstinValue = $("#karvyJSONData select[id='branchList']").find("option:selected").val();
     	if(orgGstinValue === ''){
			swal("Invalid!","please select a GSTIN before processing.","error");
     		return false;
     	}

     	/*var gstr3bDate = $("#karvyJSONData input[id='txtDate2']").val();
     	if(gstr3bDate === ''){
     		alert("please provide GSTR3B date before processing.");
     		return false;
     	}
     	var length = gstr3bDate.indexOf(' ');
     	var gstrMonth = gstr3bDate.substring(0, length);
     	var gstrYear = gstr3bDate.substring(length+1);
     	*/
     	var grossAmount; var sgst; var cgst; var igst;var cess;
     	var grossAmount1; var sgst1; var cgst1; var igst1;var cess1;
     	var grossAmount2; var sgst2; var cgst2; var igst2;var cess2;
     	var grossAmount3; var sgst3; var cgst3; var igst3;var cess3;
     	var grossAmount4; var sgst4; var cgst4; var igst4;var cess4;
     	
     	var grossAmount5; var sgst5; var cgst5; var igst5;var cess5;
     	var grossAmount6; var sgst6; var cgst6; var igst6;var cess6;
     	var grossAmount7; var sgst7; var cgst7; var igst7;var cess7;
     	var grossAmount8; var sgst8; var cgst8; var igst8;var cess8;
     	var grossAmount9; var sgst9; var cgst9; var igst9;var cess9;
    	var totalGross1; var totalCgst1; var totalSgst1; var totalIgst1;var totalCess1;
    	var sgstFor4243; var cgstFor4243; var igstFor4243;var cessFor4243;
    	var sgstForOther; var cgstForOther; var igstForOther;var cessForOther;
     	
    	var totalGross2; var totalCgst2; var totalSgst2; var totalIgst2;var totalCess2;
     	
     	$("#karvyGSTR3BJSONData").attr('data-toggle', 'modal');
        $("#karvyGSTR3BJSONData").modal('show');
        $("#karvyGSTR3BJSONData input[id='orgGstin']").val(orgGstin);
        var length1=gstr3bDate.length;
        var length = gstr3bDate.indexOf(' ');
    	var gstrMonth = gstr3bDate.substring(0, length);
    	var gstrYear = gstr3bDate.substring(length1-4,length1);
    	//var gstrYear = gstr3bDate.substring(length+1);
    	$("#karvyGSTR3BJSONData input[id='gstrMonth']").val(gstrMonth);
    	$("#karvyGSTR3BJSONData input[id='gstrYear']").val(gstrYear);
        // $("#karvyGSTR3BJSONData input[id='gstrMonth']").val(gstrMonth);
     	//$("#karvyGSTR3BJSONData input[id='gstrYear']").val(gstrYear);
     	$("#karvyGSTR3BJSONData input[id='gstrOrgName']").val($("#companyName").val());
     	$("#karvyGSTR3BJSONData input[id='gstrOrgName']").val($("#companyName").val());
        for(var i=0;i<data.outwardTaxableSupplies.length;i++){
          grossAmount=data.outwardTaxableSupplies[i].grossAmount;
           sgst=data.outwardTaxableSupplies[i].sgst;
           cgst=data.outwardTaxableSupplies[i].cgst;
           igst=data.outwardTaxableSupplies[i].igst;
           cess=data.outwardTaxableSupplies[i].cess;
         }
       /* $("#karvyGSTR3BJSONData input[id='totalTaxableA']").val(grossAmount);
        $("#karvyGSTR3BJSONData input[id='integratedTaxA']").val(sgst);
        $("#karvyGSTR3BJSONData input[id='centralTaxA']").val(cgst);
        $("#karvyGSTR3BJSONData input[id='stateTaxA']").val(igst);
        $("#karvyGSTR3BJSONData input[id='cessTaxA']").val(cess);*/
        
        $("#OTS input[id='totalTaxableA']").val(grossAmount.toFixed(2));
        $("#OTS input[id='integratedTaxA']").val(igst.toFixed(2));
        $("#OTS input[id='centralTaxA']").val(cgst.toFixed(2));
        $("#OTS input[id='stateTaxA']").val(sgst.toFixed(2));
        $("#OTS input[id='cessTaxA']").val(cess.toFixed(2));
        
        for(var i=0;i<data.outwardTaxableSuppliesZeroRated.length;i++){
            grossAmount1=data.outwardTaxableSuppliesZeroRated[i].grossAmount;
            igst1=data.outwardTaxableSuppliesZeroRated[i].igst;
            cess1=data.outwardTaxableSuppliesZeroRated[i].cess;
         }
       
        $("#OTSZR input[id='totalTaxableA']").val(grossAmount1.toFixed(2));
        $("#OTSZR input[id='integratedTaxA']").val(igst1.toFixed(2));
        $("#OTSZR input[id='cessTaxA']").val(cess1.toFixed(2));
     	//$("#karvyGSTR3BJSONData input[id='gstrOrgName']").val($(""))
        
        for(var i=0;i<data.otherOutwardTaxableSuppliesZeroRated.length;i++){
            grossAmount2=data.otherOutwardTaxableSuppliesZeroRated[i].grossAmount;
           
         }
       
        $("#OOTS input[id='totalTaxableA']").val(grossAmount2.toFixed(2));
        
        
        for(var i=0;i<data.inwardSupplies.length;i++){
            grossAmount3=data.inwardSupplies[i].grossAmount;
            sgst3=data.inwardSupplies[i].sgst;
            cgst3=data.inwardSupplies[i].cgst;
           igst3=data.inwardSupplies[i].igst;
            cess3=data.inwardSupplies[i].cess;
         }
       
        $("#ISLRC input[id='totalTaxableA']").val(grossAmount3.toFixed(2));
        $("#ISLRC input[id='integratedTaxA']").val(igst3.toFixed(2));
        $("#ISLRC input[id='centralTaxA']").val(cgst3.toFixed(2));
        $("#ISLRC input[id='stateTaxA']").val(sgst3.toFixed(2));
        $("#ISLRC input[id='cessTaxA']").val(cess3.toFixed(2));
       
        for(var i=0;i<data.nonGstOutwardSupplies.length;i++){
           grossAmount4=data.nonGstOutwardSupplies[i].grossAmount;
           totalGross1=data.nonGstOutwardSupplies[i].totalGross1;
           totalCgst1=data.nonGstOutwardSupplies[i].totalcgst1;
           totalIgst1=data.nonGstOutwardSupplies[i].totaligst1;
           totalSgst1=data.nonGstOutwardSupplies[i].totalsgst1;
           totalCess1=data.nonGstOutwardSupplies[i].totalCess1;
          
         }
       
        $("#NGOS input[id='totalTaxableA']").val(grossAmount4.toFixed(2));
        
        $("#TOIR input[id='totalTaxable']").val(totalGross1.toFixed(2));
        $("#TOIR input[id='integratedTax']").val(totalIgst1.toFixed(2));
        $("#TOIR input[id='centralTax']").val(totalCgst1.toFixed(2));
        $("#TOIR input[id='stateTax']").val(totalSgst1.toFixed(2));
        $("#TOIR input[id='cessTax']").val(totalCess1.toFixed(2));
        
        for(var i=0;i<data.itcImportOfGoodsList.length;i++){
           
          igst5=data.itcImportOfGoodsList[i].igst;
          cess5=data.itcImportOfGoodsList[i].cess;
         }
       
        $("#IMPG input[id='integratedTax']").val(igst5.toFixed(2));
       
        $("#IMPG input[id='cessTax']").val(cess5.toFixed(2));
        
        
        for(var i=0;i<data.itcImportOfServiceList.length;i++){
           
          igst6=data.itcImportOfServiceList[i].igst;
           cess6=data.itcImportOfServiceList[i].cess;
         }
       
        $("#IMPS input[id='integratedTax']").val(igst6.toFixed(2));
        $("#IMPS input[id='cessTax']").val(cess6.toFixed(2));
       
        for(var i=0;i<data.itcInwardSuppliesList.length;i++){
            
            sgst7=data.itcInwardSuppliesList[i].sgst;
           cgst7=data.itcInwardSuppliesList[i].cgst;
           igst7=data.itcInwardSuppliesList[i].igst;
            cess7=data.itcInwardSuppliesList[i].cess;
         }
       
        $("#ISRC input[id='integratedTax']").val(igst7.toFixed(2));
        $("#ISRC input[id='centralTax']").val(cgst7.toFixed(2));
        $("#ISRC input[id='stateTax']").val(sgst7.toFixed(2));
        $("#ISRC input[id='cessTax']").val(cess7.toFixed(2));
        
        for(var i=0;i<data.itcinwardAllSuppliesList.length;i++){
            
            sgst8=data.itcinwardAllSuppliesList[i].sgst;
            cgst8=data.itcinwardAllSuppliesList[i].cgst;
           igst8=data.itcinwardAllSuppliesList[i].igst;
            cess8=data.itcinwardAllSuppliesList[i].cess;
            totalCgst2=data.itcinwardAllSuppliesList[i].totalcgst2;
            totalIgst2=data.itcinwardAllSuppliesList[i].totaligst2;
            totalSgst2=data.itcinwardAllSuppliesList[i].totalsgst2;
            totalCess2=data.itcinwardAllSuppliesList[i].totalCess2;
         }
       
        $("#OTH1 input[id='integratedTax']").val(igst8.toFixed(2));
        $("#OTH1 input[id='centralTax']").val(cgst8.toFixed(2));
        $("#OTH1 input[id='stateTax']").val(sgst8.toFixed(2));
        $("#OTH1 input[id='cessTax']").val(cess8.toFixed(2));
        $("#NETITC input[id='integratedTaxC']").val(totalIgst2.toFixed(2));
        $("#NETITC input[id='centralTaxC']").val(totalCgst2.toFixed(2));
        $("#NETITC input[id='stateTaxC']").val(totalSgst2.toFixed(2));
        $("#NETITC input[id='cessTaxC']").val(totalCess2.toFixed(2));
        
        $("#NETAB input[id='integratedTaxTotalA']").val(totalIgst2.toFixed(2));
        $("#NETAB input[id='centralTaxTotalA']").val(totalCgst2.toFixed(2));
        $("#NETAB input[id='stateTaxTotalA']").val(totalSgst2.toFixed(2));
        $("#NETAB input[id='cessTaxTotalA']").val(totalCess2.toFixed(2));
        
        $("#NETAB input[id='integratedTaxTotalB']").val(0);
        $("#NETAB input[id='centralTaxTotalB']").val(0);
        $("#NETAB input[id='stateTaxTotalB']").val(0);
        $("#NETAB input[id='cessTaxTotalB']").val(0);
        for(var i=0;i<data.allOtherITCList.length;i++){
            
           sgst9=data.allOtherITCList[i].sgst;
            cgst9=data.allOtherITCList[i].cgst;
           igst9=data.allOtherITCList[i].igst;
            cess9=data.allOtherITCList[i].cess;
         }
       
        $("#RUL2 input[id='integratedTaxD']").val(igst9.toFixed(2));
        $("#RUL2 input[id='centralTaxD']").val(cgst9.toFixed(2));
        $("#RUL2 input[id='stateTaxD']").val(sgst9.toFixed(2));
        $("#RUL2 input[id='cessTaxD']").val(cess9.toFixed(2));
        
        for(var i=0;i<data.allOtherITCList.length;i++){
            
        	sgstFor4243=data.reversalOfITCList[i].SGSTFor4243;
        	cgstFor4243=data.reversalOfITCList[i].CGSTFor4243;
        	igstFor4243=data.reversalOfITCList[i].IGSTFor4243;
        	cessFor4243=data.reversalOfITCList[i].CESSFor4243;
        	sgstForOther=data.reversalOfITCList[i].SGSTForOther;
        	cgstForOther=data.reversalOfITCList[i].CGSTForOther;
        	igstForOther=data.reversalOfITCList[i].IGSTForOther;
        	cessForOther=data.reversalOfITCList[i].CESSForOther;
          }
        
        $("#RUL1 input[id='integratedTax']").val(igstFor4243.toFixed(2));
        $("#RUL1 input[id='centralTax']").val(cgstFor4243.toFixed(2));
        $("#RUL1 input[id='stateTax']").val(sgstFor4243.toFixed(2));
        $("#RUL1 input[id='cessTax']").val(cessFor4243.toFixed(2));
        
        $("#OTH2 input[id='integratedTax']").val(igstForOther.toFixed(2));
        $("#OTH2 input[id='centralTax']").val(cgstForOther.toFixed(2));
        $("#OTH2 input[id='stateTax']").val(sgstForOther.toFixed(2));
        $("#OTH2 input[id='cessTax']").val(cessForOther.toFixed(2));
        
        
        var exepmtGSTINTERVal;
     	var exepmtGSTINTRAVal;
     	var exemptNONGSTINTERVal;
     	var exepmtNONGSTINTRAVal;
     	var totalINTERExempt;
     	var totalINTRAExempt;
        for(var i=0;i<data.exemptGSTSupplyData.length;i++){
        	
         	exepmtGSTINTERVal=data.exemptGSTSupplyData[i].interStateGSTSupplies;
         	exepmtGSTINTRAVal=data.exemptGSTSupplyData[i].intraStateGSTSupplies;
         	exemptNONGSTINTERVal=data.exemptGSTSupplyData[i].interStateNONGSTSupplies;
         	exepmtNONGSTINTRAVal=data.exemptGSTSupplyData[i].intraStateNONGSTSupplies;
         	totalINTERExempt=data.exemptGSTSupplyData[i].totalExemptInter;
         	totalINTRAExempt=data.exemptGSTSupplyData[i].totalExemptIntra;
         }
        
			
        $("#GST input[id='integratedTaxA']").val(exepmtGSTINTERVal.toFixed(2));
        $("#GST input[id='cessTaxA']").val(exepmtGSTINTRAVal.toFixed(2));
        $("#NONGST input[id='integratedTaxA']").val(exemptNONGSTINTERVal.toFixed(2));
        $("#NONGST input[id='cessTaxA']").val(exepmtNONGSTINTRAVal.toFixed(2));
        $("#TEX input[id='integratedTaxTotal']").val(totalINTERExempt.toFixed(2));
        $("#TEX input[id='cessTaxTotal']").val(totalINTRAExempt.toFixed(2));
        
        if(data.unregisteredSupplyData.length>=1){
        	$("#karvyGSTR3BTbl52 tbody").html("");
        	for(var i=0;i<data.unregisteredSupplyData.length;i++){ 
        		var multiItemsTableTr = '<tr id="gstr3b'+i+'">';
        		multiItemsTableTr += '<td><select id="karvyState" name="field0" class="input-medium bfh-states" data-country="IN" onChange="calculateGSTR3BTableValues(this);"></select></td>';
        		multiItemsTableTr += '<td><input type="text" id="field1" class="field1" onkeyup="calculateGstr3b(this, \'field1Total\');"/></td>';
        		multiItemsTableTr += '<td><input type="text" id="field2" class="field2" onkeyup="calculateGstr3b(this, \'field2Total\');"/></td>';
        		multiItemsTableTr += '<td><input type="text" id="field3" class="field3" onkeyup="calculateGstr3b(this, \'field3Total\');"/></td>';
        		multiItemsTableTr += '<td><input type="text" id="field4" class="field4" onkeyup="calculateGstr3b(this, \'field4Total\');"/></td>';
        		multiItemsTableTr += '<td><input type="text" id="field5" class="field5" onkeyup="calculateGstr3b(this, \'field5Total\');"/></td>';
        		multiItemsTableTr += '<td><input type="text" id="field6" class="field6" onkeyup="calculateGstr3b(this, \'field6Total\');"/></td></tr>';
        		
        		$("#karvyGSTR3BTbl52 tbody").append(multiItemsTableTr);
        		$('.bfh-states').bfhstates({country:'IN', blank:false});
        		$("#karvyGSTR3BTbl52 tbody tr[id=gstr3b"+i+"] select[id='karvyState']").val(data.unregisteredSupplyData[i].destinationStateCode);
        		$("#karvyGSTR3BTbl52 tbody tr[id=gstr3b"+i+"] input[id='field1']").val(data.unregisteredSupplyData[i].grossAmount);
        		$("#karvyGSTR3BTbl52 tbody tr[id=gstr3b"+i+"] input[id='field2']").val(data.unregisteredSupplyData[i].igst);
        		$("#karvyGSTR3BTbl52 tbody tr[id=gstr3b"+i+"] input[id='field3']").val(data.unregisteredSupplyData[i].grossAmount1);
        		$("#karvyGSTR3BTbl52 tbody tr[id=gstr3b"+i+"] input[id='field4']").val(data.unregisteredSupplyData[i].igst1);
        		$("#karvyGSTR3BTbl52 tbody tr[id=gstr3b"+i+"] input[id='field5']").val(data.unregisteredSupplyData[i].grossAmount2);
        		$("#karvyGSTR3BTbl52 tbody tr[id=gstr3b"+i+"] input[id='field6']").val(data.unregisteredSupplyData[i].igst2);
        		calculateGstr3bForLastTable('field1','field1','field1Total');
                calculateGstr3bForLastTable('field2','field2','field2Total');
                calculateGstr3bForLastTable('field3','field3','field3Total');
                calculateGstr3bForLastTable('field4','field4','field4Total');
                calculateGstr3bForLastTable('field5','field5','field5Total');
                calculateGstr3bForLastTable('field6','field6','field6Total');
        		}
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

function calculateGSTR3BTableValues(elem){
	var value=$(elem).val();
	if(value==""){
		cancelAndClearGSTR3BDivContents();
	}else{
		
		calculateGSTR3BValuesAndAppend(elem);
	}
}
function calculateGSTR3BValuesAndAppend(elem){
	var stateCode= $(elem).val();
	var trid=$(elem).parent().parent().attr('id');
	
	//var stateCode = $(elem).find("td select[name='field0'] option:selected").val();
	
	
	var gstIn=$("select[id='branchList'] option:selected ").text();
	var intervalType=$("#karvyGSTR3BJSONAdditionalData select[id='returnIntervalForGSTR3B'] option:selected ").val();
	
	var txtDate1="";
	var txtDate2="";
	var txtDate3="";
	var txtDate4="";
	var gstr3bDate="'"
	if(intervalType==1){
    	txtDate1=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B1']").val(); 
    }
	if(intervalType==2){
    	txtDate2=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B3']").val();
    }
	
	if(intervalType==3){
    	txtDate3=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B2']").val();
    	txtDate4=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B4']").val();
    	
    }
	
	var jsonData = {};
	jsonData.txtDate1=txtDate1;
    jsonData.txtDate2=txtDate2;
    jsonData.txtDate3=txtDate3;
    jsonData.txtDate4=txtDate4;
    jsonData.intervalType=intervalType;
    jsonData.useremail=$("#hiddenuseremail").text(); 
    jsonData.gstIn=gstIn;
    jsonData.stateCode=stateCode;
    var url="/config/getStatewiseDataForKarvy";
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
        	 for(var i=0;i<data.unregisteredSupplyData.length;i++){
        		 var grossAmount=0;var grossAmount1=0;var grossAmount2=0;
                 var igst=0;var igst1=0;var igst2=0;
                 grossAmount=data.unregisteredSupplyData[i].grossAmount;
                 grossAmount1=data.unregisteredSupplyData[i].grossAmount1;
                 grossAmount2=data.unregisteredSupplyData[i].grossAmount2;
                 igst=data.unregisteredSupplyData[i].igst;
                 igst1=data.unregisteredSupplyData[i].igst1;
                 igst2=data.unregisteredSupplyData[i].igst2;
                 $("#"+trid).find("td input[id='field1']").val(grossAmount.toFixed(2));
                 $("#"+trid).find("td input[id='field2']").val(igst.toFixed(2));
                 $("#"+trid).find("td input[id='field3']").val(grossAmount1.toFixed(2));
                 $("#"+trid).find("td input[id='field4']").val(igst1.toFixed(2));
                 $("#"+trid).find("td input[id='field5']").val(grossAmount2.toFixed(2));
                 $("#"+trid).find("td input[id='field6']").val(igst2.toFixed(2));
                 calculateGstr3bForLastTable('field1','field1','field1Total');
                 calculateGstr3bForLastTable('field2','field2','field2Total');
                 calculateGstr3bForLastTable('field3','field3','field3Total');
                 calculateGstr3bForLastTable('field4','field4','field4Total');
                 calculateGstr3bForLastTable('field5','field5','field5Total');
                 calculateGstr3bForLastTable('field6','field6','field6Total');
                 
                              }
        },
        error: function (xhr, status, error) {
           if(xhr.status == 401){ doLogout(); }
        }
     });
}
function cancelAndClearGSTR3BDivContents(){
	
}
var addNewKarvyGstr = function(){
	var length=$("#karvyGSTR3BTbl52 tbody tr").length;
	var multiItemsTableTr = '<tr id="gstr3b'+length+'">';
	multiItemsTableTr += '<td><select id="karvyState" name="field0" class="input-medium bfh-states" data-country="IN" onChange="calculateGSTR3BTableValues(this);"></select></td>';
	multiItemsTableTr += '<td><input type="text" id="field1" class="field1" onkeyup="calculateGstr3b(this, \'field1Total\');"/></td>';
	multiItemsTableTr += '<td><input type="text" id="field2" class="field2" onkeyup="calculateGstr3b(this, \'field2Total\');"/></td>';
	multiItemsTableTr += '<td><input type="text" id="field3" class="field3" onkeyup="calculateGstr3b(this, \'field3Total\');"/></td>';
	multiItemsTableTr += '<td><input type="text" id="field4" class="field4" onkeyup="calculateGstr3b(this, \'field4Total\');"/></td>';
	multiItemsTableTr += '<td><input type="text" id="field5" class="field5" onkeyup="calculateGstr3b(this, \'field5Total\');"/></td>';
	multiItemsTableTr += '<td><input type="text" id="field6" class="field6" onkeyup="calculateGstr3b(this, \'field6Total\');"/></td></tr>';

	$("#karvyGSTR3BTbl52 tbody").append(multiItemsTableTr);
	$('.bfh-states').bfhstates({country:'IN', blank:false});
}

function clearIfClaimBranchEmpty(elem){
	var value=$(elem).val();
	if(value==""){
		cancelAndClearDivContents();
	}
}
var removeNewKarvyGstr = function(){
	var length=$("#karvyGSTR3BTbl52 tbody tr").length;
	if(parseInt(length) > 1){
		$("#karvyGSTR3BTbl52 tbody tr:last").remove();
		 calculateGstr3bForLastTable('field1','field1','field1Total');
         calculateGstr3bForLastTable('field2','field2','field2Total');
         calculateGstr3bForLastTable('field3','field3','field3Total');
         calculateGstr3bForLastTable('field4','field4','field4Total');
         calculateGstr3bForLastTable('field5','field5','field5Total');
         calculateGstr3bForLastTable('field6','field6','field6Total');
	}
}
var calculateGstr3bForLastTable = function(fId,fClass, totalField) {
	var tableId = $("#"+fId).closest('table').attr('id');
    var grandTotal = 0;
    var fieldid = fId;
    var fieldClass = fClass;
    $("#"+tableId).find('#' + totalField).val("");
    $("#"+tableId).find("."+fieldClass).each(function(){
        var total = Number($.trim($(this).val()));
        if($.isNumeric(total)) {
            grandTotal += total;
        }
    });
    $("#"+tableId).find('#' + totalField).val(grandTotal.toFixed(2));
}
var calculateGstr3b = function(elem, totalField) {
	var tableId = $(elem).closest('table').attr('id');
    var grandTotal = 0;
    var fieldid = $(elem).attr('id');
    var fieldClass = $(elem).attr('class');
    $("#"+tableId).find('#' + totalField).val("");
    $("#"+tableId).find("."+fieldClass).each(function(){
        var total = Number($.trim($(this).val()));
        if($.isNumeric(total)) {
            grandTotal += total;
        }
    });
    $("#"+tableId).find('#' + totalField).val(grandTotal.toFixed(2));

    if(tableId === 'karvyGSTR3BTblItc'){
    	var totalA = $("#"+fieldid+"TotalA").val();
    	var totalB = $("#"+fieldid+"TotalB").val();
    	var resultNew=0.00;
    	if($.isNumeric(totalA) && $.isNumeric(totalB)) {
    		resultNew =  parseFloat(totalA) - parseFloat(totalB);
    	}else if ($.isNumeric(totalA)){
    		resultNew = totalA;
    	}else if ($.isNumeric(totalB)){
    		resultNew = totalB;
    	}
    	$("#"+fieldid+"C").val(resultNew);
    }
}

var generateGstr3BJson = function(){
	//var txtDate=$("#txtDate2").val();
	var intervalType=$("#karvyGSTR3BJSONAdditionalData select[id='returnIntervalForGSTR3B'] option:selected ").val();
	var txtDate="";
	var txtDate1="";
	var txtDate2="";
	var txtDate3="";
	var txtDate4="";
	var gstr3bDate="'"
	if(intervalType==1){
    	var txtDate1=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B1']").val(); 
    	txtDate=txtDate1;
    }
	if(intervalType==2){
    	var txtDate2=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B3']").val();
    	txtDate=txtDate2
    }
	if(intervalType==3){
    	var txtDate3=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B2']").val();
    	var txtDate4=$("#karvyGSTR3BJSONAdditionalData input[id='txtDateGSTR3B4']").val();
    	txtDate=txtDate3;
    }
	var table31 = read31DataForGstr3b('karvyGSTR3BTbl');
	var table4 = read31DataForGstr3b('karvyGSTR3BTblItc');
	var table5 = read31DataForGstr3b('karvyGSTR3BTbl5');
	var table51 = read31DataForGstr3b('karvyGSTR3BTbl51');
	var table52 = read31DataForGstr3b('karvyGSTR3BTbl52');

    var jsonData = {};   
    jsonData.txtDate=txtDate;
    jsonData.intervalType=intervalType;
    jsonData.table31 = table31;
    jsonData.table4 = table4;
    jsonData.table5 = table5;
    jsonData.table51 = table51;
    jsonData.table52 = table52;
    jsonData.gstin=$("select[id='branchList'] option:selected ").text();
    jsonData.useremail=$("#hiddenuseremail").text();
   
    var url="/data/dlGstr3b";
    downloadFile(url, "POST", jsonData, "Error on downloading JSON Data!");
}

var read31DataForGstr3b= function(tableName){
	var multipleItemsData = [];
	$("#" + tableName + " > tbody > tr").each(function() {
		var jsonData = {};
		if(tableName == 'karvyGSTR3BTbl'){
			var totalTaxable = $(this).find("td input[class='totalTaxableCls']").val();	
			if(totalTaxable !== '' && typeof totalTaxable != 'undefined'){
				jsonData.totalTaxable = totalTaxable;
				jsonData.integratedTax = $(this).find("td input[class='integratedTaxCls']").val();
				jsonData.centralTax = $(this).find("td input[class='centralTaxCls']").val();
				jsonData.stateTax = $(this).find("td input[class='stateTaxCls']").val();
				jsonData.cessTax = $(this).find("td input[class='cessTaxCls']").val();
				multipleItemsData.push(JSON.stringify(jsonData));
			}
		}else if(tableName == 'karvyGSTR3BTblItc'){
			var integratedTax = $(this).find("td input[name='integratedTax']").val();	
			if(integratedTax !== '' && typeof integratedTax != 'undefined'){
				var trId = $(this).attr('id');
				if(trId === '' || trId === null || typeof trId === 'undefined'){
					trId = '';
				}
				jsonData.trid = trId;
				jsonData.integratedTax = integratedTax;
				jsonData.centralTax = $(this).find("td input[name='centralTax']").val();
				jsonData.stateTax = $(this).find("td input[name='stateTax']").val();
				jsonData.cessTax = $(this).find("td input[name='cessTax']").val();
				multipleItemsData.push(JSON.stringify(jsonData));
			}
		}else if(tableName == 'karvyGSTR3BTbl5'){
			var integratedTax = $(this).find("td input[class='integratedTaxCls']").val();	
			if(integratedTax !== '' && typeof integratedTax != 'undefined'){
				var trId = $(this).attr('id');
				if(trId === '' || trId === null || typeof trId === 'undefined'){
					trId = '';
				}
				jsonData.trid = trId;
				jsonData.integratedTax = integratedTax;
				jsonData.cessTax = $(this).find("td input[class='cessTaxCls']").val();
				multipleItemsData.push(JSON.stringify(jsonData));
			}
		}else if(tableName == 'karvyGSTR3BTbl51'){
			var integratedTax = $(this).find("td input[class='integratedTaxCls']").val();	
			if(integratedTax !== '' && typeof integratedTax != 'undefined'){
				jsonData.integratedTax = integratedTax;
				jsonData.centralTax = $(this).find("td input[class='centralTaxCls']").val();
				jsonData.stateTax = $(this).find("td input[class='stateTaxCls']").val();
				jsonData.cessTax = $(this).find("td input[class='cessTaxCls']").val();
				multipleItemsData.push(JSON.stringify(jsonData));
			}
		}else if(tableName == 'karvyGSTR3BTbl52'){
			var field1 = $(this).find("td input[class='field1']").val();	
			if(field1 !== '' && typeof field1 != 'undefined'){
				var stateCode = $(this).find("td select[name='field0'] option:selected").val();
				jsonData.stateCode = stateCode;
				jsonData.field1 = field1;
				jsonData.field2 = $(this).find("td input[class='field2']").val();
				jsonData.field3 = $(this).find("td input[class='field3']").val();
				jsonData.field4 = $(this).find("td input[class='field4']").val();
				jsonData.field5 = $(this).find("td input[class='field5']").val();
				jsonData.field6 = $(this).find("td input[class='field6']").val();
				multipleItemsData.push(JSON.stringify(jsonData));
			}
		}
	});
	return multipleItemsData;
}


/*----------------------Sunil's Code End ------------------*/
/**
 * @Author: Manali Mungikar
 */

/*vendor supplier add location to the master database start*/
function submitTransactionsToKarvy(elem){
    var mouldesRights = $("#usermoduleshidden").val();
   
    showHideModuleTabs(mouldesRights);
	swal("INFO!","submitTransactionsToKarvy","info");
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.userEmail = $("#hiddenvendcustemail").text();	
	var url="/send/submitTransactionsToKarvy";
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
			swal("Success!","Data sent to karvy for Sell transactions = " + data.totalSell,"error");
			swal("Success!","Data sent to karvy for Buy transactions = " + data.totalBuy,"error");
			swal("Success!","Data sent for Receive Advance From Customer transactions = " + data.totalRecAdvFromCust,"error");
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on Submitting data to Karvy!", "Please retry, if problem persists contact support team.", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();			
		}
	});		
}

//karvy GST JSON download: calling karvy get API to get JSON 
function getJSONOutputFromKarvy(elem){
	var mouldesRights = $("#usermoduleshidden").val();
	
	showHideModuleTabs(mouldesRights);
    $("#karvyJSONData").attr('data-toggle', 'modal');
    $("#karvyJSONData").modal('show');
    $("#branchList") .children().remove();
    $("#txtDate1").val();
    $('#txtDate2').val();
    $('#txtDate3').val();
    $('#txtDate1').datepicker();
    $('#txtDate2').datepicker();
    $('#txtDate3').datepicker();
    
    var jsonData = {};
	jsonData.userEmail = $("#hiddenuseremail").text();	
	var url="/config/getbranchlist";
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method : "POST",
		contentType:'application/json',
		success: function (data) {
			$("#branchList").append('<option value="">please select GSTIN</option>');
			for(var i=0;i<data.branchlist.length;i++){
				$("#branchList").append('<option value="'+data.branchlist[i].bnchId+'">'+data.branchlist[i].bnchGST+'</option>');
				
			}
			var noOfOptions = $("#branchList option").length;
			if(parseInt(noOfOptions) == 2){
			    $('#branchList option:eq(1)').attr("selected", true);
			}
			else{
				$('#branchList option:first').attr("selected", true);
			}
			if(data.composition.composition == 1){
				$('div[class="gstr4Div"]').show();
				$('div[class="gstr1Div"]').hide();
				$('div[class="gstr3BDiv"]').hide();
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on getting branches!", "Please retry, if problem persists contact support team.", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();			
		}
	});		
}

/*$(document).ready(function() {
	$('#karvyGSTR3BJSONData input').blur(function(){
		var value = $(this).val();
    	$(this).val(value.toFixed(2));
    	alert(value);
    });
	$('.date-picker').datepicker({
	     changeMonth: true,
	     changeYear: true,
	     dateFormat: 'MM yy',
	       
	     onClose: function() {
	        var iMonth = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
	        var iYear = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
	        $(this).datepicker('setDate', new Date(iYear, iMonth, 1));
	        $(this).datepicker('widget').removeClass('hide-current hide-calendar');
	     },
	     
	     beforeShow: function() {
	    	 $(this).datepicker("widget").addClass('hide-current hide-calendar');	 
	       if ((selDate = $(this).val()).length > 0) 
	       {
	          iYear = selDate.substring(selDate.length - 4, selDate.length);
	          iMonth = jQuery.inArray(selDate.substring(0, selDate.length - 5), $(this).datepicker('option', 'monthNames'));
	          $(this).datepicker('option', 'defaultDate', new Date(iYear, iMonth, 1));
	           $(this).datepicker('setDate', new Date(iYear, iMonth, 1));
	       }
	    }
	  });
	});
*/
var maximumYear=new Date().getFullYear()+30;
$(document).ready(function() {
	/*$(".date-picker").datepicker({
		 changeMonth: true,
            changeYear: true,
            showButtonPanel: true,
            dateFormat: 'MM yy',
         
		//yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
			$(this).focus();
		}
          
	});*/
	/* $(".date-picker").datepicker({
	        dateFormat: 'MM yy',
	        changeMonth: true,
	        changeYear: true,
	        showButtonPanel: true,

	        onClose: function(dateText, inst) {
	            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
	            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
	            $(this).val($.datepicker.formatDate('MM yy', new Date(year, month, 1)));
	        }
	    });

	    $(".date-picker").focus(function () {
	        $(".ui-datepicker-calendar").hide();
	        $("#ui-datepicker-div").position({
	            my: "center top",
	            at: "center bottom",
	            of: $(this)
	        });
	    });
	
	*/
	 $(".date-picker").datepicker({
			dateFormat: 'MM yy',
			changeMonth: true,
			changeYear: true,
			showButtonPanel: true,
			yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
			onSelect: function(x,y){
				$(this).focus();
			},
			onClose: function(dateText, inst) {
				var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
				var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
				 $(this).datepicker('setDate', new Date(year, month, 1));
			}
				 
			     /*   $(this).datepicker('widget').removeClass('hide-current hide-calendar');
				$(this).val($.datepicker.formatDate('MM yy', new Date(year, month, 1)));*/
			
		});

		$(".date-picker").focus(function () {
			$(".ui-datepicker-calendar").hide();
			$("#ui-datepicker-div").position({
				my: "center top",
				at: "center bottom",
				of: $(this)
			});
		});

});
function downloadGSTR1JSONData(){
    var txtDate=$("#txtDate").val();   
    var jsonData = {};   
    jsonData.txtDate=txtDate;
    jsonData.type="GSTR1";
    jsonData.useremail=$("#hiddenuseremail").text();
    var url="/config/downloadKarvyGSTR1JSONSFile";
    downloadFile(url, "POST", jsonData, "Error on downloading JSON Data!");
}
function downloadGSTR1JSONDataForKarvy(){
	 var typeOfTransaction="";
	 var  typeOfOtherTransaction="";
	 typeOfTransaction=$("#karvyGSTR1JSONData select[id='transactionTypeDropDown']").find("option:selected").val();
	 typeOfOtherTransaction=$("#karvyGSTR1JSONData select[id='transactionOtherTypeDropDown']").find("option:selected").val();
	 var gstIn=$("select[id='branchList'] option:selected ").text();
		if(gstIn === ''){
			swal("Incomplete details!","please select a GSTIN before processing.","error");
			return false;
		}

		var intervalType=$("select[id='returnInterval'] option:selected ").val();
		if(intervalType==""){
			swal("Incomplete details!","please select the interval type","error");
			return false;
		}
		var txtDate1="";
		var txtDate2="";
		var txtDate3="";
		var txtDate4="";
		if(intervalType==1){
	    	var txtDate1=$("#txtDate1").val(); 
	    	if(txtDate1===''){
	    		swal("Incomplete details!","please select date","error");
	    		return false;
	    	}
	    }if(intervalType==2){
	    	var txtDate2=$("#txtDate3").val();
	    	if(txtDate2===''){
	    		swal("Incomplete details!","please select date","error");
	    		return false;
	    	}
	    }if(intervalType==3){
	    	var txtDate3=$("#txtDate2").val();
	    	var txtDate4=$("#txtDate4").val();
	    	if(txtDate3===''){
	    		swal("Incomplete details!","please select from date","error");
	    		return false;
	    	}
	    	if(txtDate4===''){
	    		swal("Incomplete details!","please select to date","error");
	    		return false;
	    	}
	    }
	  
	 if(typeof typeOfTransaction!='undefined'){
		 
		 downloadGSTR1JSONDataForTransactionKarvy(); 
		 
	 }
	 if(typeof typeOfOtherTransaction!='undefined') {
		
		 downloadGSTR1JSONDataForOtherTransactionKarvy();
		 
	 }
}
function downloadGSTR1JSONDataForTransactionKarvy(){
	var gstIn=$("select[id='branchList'] option:selected ").text();
	

	var intervalType=$("select[id='returnInterval'] option:selected ").val();
	
	var txtDate1="";
	var txtDate2="";
	var txtDate3="";
	var txtDate4="";
	if(intervalType==1){
    	var txtDate1=$("#txtDate1").val(); 
    	
    }if(intervalType==2){
    	var txtDate2=$("#txtDate3").val();
    	
    }if(intervalType==3){
    	var txtDate3=$("#txtDate2").val();
    	var txtDate4=$("#txtDate4").val();
    	
    }
  
    var selectedValues = [];    
    $("#transactionTypeDropDown :selected").each(function(){
        selectedValues.push($(this).val()); 
    });
   
    var jsonData = {};   
    jsonData.txtDate1=txtDate1;
    jsonData.txtDate2=txtDate2;
    jsonData.txtDate3=txtDate3;
    jsonData.txtDate4=txtDate4;
    jsonData.type="GSTR1";
    
    jsonData.intervalType=intervalType;
    
    jsonData.selectedValues=selectedValues;
    jsonData.useremail=$("#hiddenuseremail").text(); 
    jsonData.gstIn=gstIn;
    var url="/config/downloadKarvyGSTR1JSONSFileForKarvy";
    downloadFile(url, "POST", jsonData, "Error on downloading JSON Data!");
}
function downloadGSTR1JSONDataForOtherTransactionKarvy(){
	var intervalType=$("select[id='returnInterval'] option:selected ").val();
	var txtDate1="";
	var txtDate2="";
	var txtDate3="";
	var txtDate4="";
	if(intervalType==1){
    	var txtDate1=$("#txtDate1").val(); 
    }if(intervalType==2){
    	var txtDate2=$("#txtDate3").val();
    	
    }if(intervalType==3){
    	var txtDate3=$("#txtDate2").val();
    	var txtDate4=$("#txtDate4").val();
    }
   
    var selectedValues = [];    
    
    $("#transactionOtherTypeDropDown :selected").each(function(){
        selectedValues.push($(this).val()); 
    });
  
    var jsonData = {};   
    jsonData.txtDate1=txtDate1;
    jsonData.txtDate2=txtDate2;
    jsonData.txtDate3=txtDate3;
    jsonData.txtDate4=txtDate4;
    jsonData.type="GSTR1";
    jsonData.intervalType=intervalType;
    
    jsonData.selectedValues=selectedValues;
    jsonData.useremail=$("#hiddenuseremail").text(); 
    jsonData.gstIn=$("select[id='branchList'] option:selected ").text();
    var url="/config/downloadKarvyGSTR1JSONSFileForOtherTransactionsKarvy";
    downloadFile(url, "POST", jsonData, "Error on downloading JSON Data!");
}
function downloadGSTR4JSONData(elem){
	var orgGstin = $("#karvyJSONData select[id='branchList']").find("option:selected").text();
	var orgGstinValue = $("#karvyJSONData select[id='branchList']").find("option:selected").val();
	if(orgGstinValue === ''){
		swal("Incomplete details!","Please select a GSTIN before processing.","error");
		return false;
	}

	//var txtDate = $("#karvyJSONData input[id='txtDate3']").val();
	var txtDate = $("#gstr4MonthCalender").val();
	if(txtDate === ''){
		swal("Incomplete details!","Please provide GSTR4 date before processing.","error");
		return false;
	}
	//var txtDate=$("#txtDate3").val();
	
    var jsonData = {};   
    jsonData.txtDate=txtDate;
    jsonData.type="GSTR4";
    jsonData.useremail=$("#hiddenuseremail").text();
    jsonData.gstIn=$("select[id='branchList'] option:selected ").text();
    jsonData.taxRate=$("#karvyGSTR4JSONData select[id='inputGstTaxRate'] option:selected ").val();
    jsonData.turnOver=$("#turnOver").val();
    jsonData.cgst=$("#cgstRate").val();
    jsonData.sgst=$("#sgstRate").val();
    var url="/config/downloadKarvyGSTR4JSONSFileForKarvy";
    downloadFile(url, "POST", jsonData, "Error on downloading JSON Data!");
}

function downloadGSTR2JSONData(){
	var txtDate=$("#txtDate").val();   
	
    var jsonData = {};  
    
    jsonData.txtDate=txtDate;
    jsonData.type="GSTR3B";
    jsonData.useremail=$("#hiddenuseremail").text();
   
    var url="/config/downloadKarvyGSTR1JSONSFile";
    downloadFile(url, "POST", jsonData, "Error on downloading JSON Data!");
}
/* Added by Puja Lohia 27th Feb 2018*/
function callKARVYUrl(){
	var jsonData = {};    
    var useremail=$("#hiddenuseremail").text();
     jsonData.usermail = useremail;
    var url="/config/callKARVYUrl";
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
        	//window.location.href=data.pwcurl;
        	window.open(data.karvyurl); // to open link in NEW tab.
        	
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
            	doLogout(); 
            	}
        }
    });
}

function addAdditionalDetailsForGSTR4(){
	var orgGstinValue = $("#karvyJSONData select[id='branchList']").find("option:selected").val();
	if(orgGstinValue === ''){
		swal("Incomplete details!","please select a GSTIN before processing.","error");
		return false;
	}
	 $("#karvyGSTR4JSONData").attr('data-toggle', 'modal');
	 $("#karvyGSTR4JSONData").modal('show');
	 $("#turnOver").val("");
	 $("#cgstRate").val("");
	 $("#sgstRate").val("");
}
function addAdditionalDetailsForGSTR1(){
	var orgGstinValue = $("#karvyJSONData select[id='branchList']").find("option:selected").val();
	if(orgGstinValue === ''){
		swal("Incomplete details!","please select a GSTIN before processing.","error");
		return false;
	}
	 $("#karvyGSTR1JSONData").attr('data-toggle', 'modal');
	 $("#karvyGSTR1JSONData").modal('show');
	 //transactionTypeList += '<li class="transactioTypeListCls" id="1"><input type="checkbox" id="vendorBranchCheck" onclick="" value="1"></li>';

	 $("#vendorBranchList").append(vendorBranchList);
	 $('#customerBranchList').append(customerBranchList);
	 $('.multiBranch').multiselect('rebuild');
	 $('.multipleDropdown').multiselect('rebuild');
}
function addAdditionalDetailsForGSTR3B(){
	var orgGstinValue = $("#karvyJSONData select[id='branchList']").find("option:selected").val();
	if(orgGstinValue === ''){
		swal("Incomplete details!","please select a GSTIN before processing.","error");
		return false;
	}
	 $("#karvyGSTR3BJSONAdditionalData").attr('data-toggle', 'modal');
	 $("#karvyGSTR3BJSONAdditionalData").modal('show');
	 //transactionTypeList += '<li class="transactioTypeListCls" id="1"><input type="checkbox" id="vendorBranchCheck" onclick="" value="1"></li>';

	/* $("#vendorBranchList").append(vendorBranchList);
	 $('#customerBranchList').append(customerBranchList);*/
	 $('.multiBranch').multiselect('rebuild');
	 $('.multipleDropdown').multiselect('rebuild');
	
}
function calculateTurnOver(elem){
	  //  var txtDate=$("#txtDate3").val();  
	    var txtDate=$("#gstr4MonthCalender").val();  
	    var taxRate = $(elem).val();
	    if(txtDate == "") {
	    	swal("Incomplete details!","Please select Month First","error");
	    	 $(" input[name='turnover']").val("");
			 $(" #cgstRate").val("");
			 $(" #sgstRate").val("");
			 return false;
	    }
	    var jsonData = {};   
	    jsonData.txtDate=txtDate;
	    jsonData.type="GSTR4";
	    jsonData.useremail=$("#hiddenuseremail").text();	
	    jsonData.taxRate=taxRate;
	    jsonData.gstIn=$("select[id='branchList'] option:selected ").text();
	   // jsonData.taxRate=$("select[id='inputGstTaxRate'] option:selected ").text();
	    var url="/config/getturnover";
		$.ajax({
			url: url,
			data:JSON.stringify(jsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method : "POST",
			contentType:'application/json',
			success: function (data) {
				 $(" input[name='turnover']").val(data.dashBoardData[0].intraGstTurnover);
				 $(" #cgstRate").val(data.dashBoardData[0].cgst);
				 $(" #sgstRate").val(data.dashBoardData[0].sgst);
				
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
		    		swal("Error on getting branches!", "Please retry, if problem persists contact support team.", "error");
		    	}
			},
			complete: function(data) {
				$.unblockUI();			
			}
		});		
}
/*var maximumYear=new Date().getFullYear()+30;
$(function() {
	$(".date-picker").datepicker({
		 changeMonth: true,
            changeYear: true,
            showButtonPanel: true,
            dateFormat: 'MM yy',
		
            onChangeMonthYear: function(year, month, obj) {
                removeDisabledMonths();
            }
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
			$(this).focus();
		}
	});
});
var removeDisabledMonths = function() {
    setTimeout(function() {

        var monthsToDisable = [0,1,3,4,6,7,9,10];

        $.each(monthsToDisable, function(k, month) {
            var i = $('#ui-datepicker-div select.ui-datepicker-month').find('option[value="'+month+'"]').remove();
        });

    }, 100);
};
*/
$(document).ready(function(){
	$('#transactionTypeDropDown').multiselect();
	$('#transactionOtherTypeDropDown').multiselect();
	
});
$('#select_all').click( function() {
    $('#transactionTypeDropDown option').each(function(){
        $(this).attr('selected', 'selected');
    });
});
function selectReturnPeriod(elem){
	//var option=$("#karvyGSTR1JSONData select[id='returnInterval'] option:selected ").val();
	var tdId = $(elem).closest('td').attr('id');
	var option=$(elem).val();
	if(option==1){
		$("#quarterCalender").hide();
		$("#dateRangeCalender").hide();
		$("#monthlyCalender").show();
	}else if(option==2){
		$("#monthlyCalender").hide();
		$("#dateRangeCalender").hide();
		$("#quarterCalender").show();
	}else if(option==3){
		$("#monthlyCalender").hide();
		$("#quarterCalender").hide();
		$("#dateRangeCalender").show();
		
	}
}
function selectReturnPeriodForGSTR3B(elem){
	//var option=$("#karvyGSTR1JSONData select[id='returnInterval'] option:selected ").val();
	var tdId = $(elem).closest('td').attr('id');
	var option=$("#"+tdId+" select[id='returnIntervalForGSTR3B'] option:selected ").val();
	
	if(option==1){
		$("#quarterCalenderForGSTR3B").hide();
		$("#dateRangeCalenderForGSTR3B").hide();
		$("#monthlyCalenderForGSTR3B").show();
	}else if(option==2){
		$("#monthlyCalenderForGSTR3B").hide();
		$("#dateRangeCalenderForGSTR3B").hide();
		$("#quarterCalenderForGSTR3B").show();
	}else if(option==3){
		$("#monthlyCalenderForGSTR3B").hide();
		$("#quarterCalenderForGSTR3B").hide();
		$("#dateRangeCalenderForGSTR3B").show();
		
	}
}

$(document).ready(function(){
	$("select[name='transactionTypeDropDown']").multiselect({
			        buttonWidth: '150px',
			        maxHeight:   150,
			        includeSelectAllOption: true,
			        enableFiltering :true,
			       
			        onChange: function(element, checked) {
			        }
		});
});
/*$('#selectAll').on('click', function() {
    if (this.checked == true)
        $('#transactionTypeDropDown').find('option[id="checkboxRow"]').prop('checked', true);
    else
        $('#userTable').find('input[name="checkboxRow"]').prop('checked', false);
});*/
