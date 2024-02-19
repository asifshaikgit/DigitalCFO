var isExpCoaLoaded = false;
var isIncomeCoaLoaded = false;
var expenseItemDataGlobal = '';
var ORG_PLACE_OF_SUPPLY_TYPE;
const COMPANY_PWC = "PWC";
const IS_READ_PAYMODE_ON_APPROVAL = 1;
var COMPANY_OWNER = "";
var READ_PAYMODE_ON_APPROVAL;
var readOrgConfigParams = function(){
	COMPANY_OWNER = $("#companyOwner").val();
	READ_PAYMODE_ON_APPROVAL = $("#readPaymodeOnApproval").val();
}

$(document).ready(function(){
	$('.completecompanyDetails'). click(function(){
		let uploadDestinationId = $("#uploadDestinationId").val();
		if (COMPANY_OWNER == "PWC") {
			uploadDestinationId = 1;
		}else if(uploadDestinationId == ""){
			uploadDestinationId = 0;
		}
		let orgId      = $("#hiddenOrgId").val();
		let compName=    $("#companyName").val();
		let corporateEmail=$("#corporateemail").val();
		let regAddr    = $("#registeredaddress").val();
		let countryCode=$("#orgregPhnNocountryCode").val();
		let ctryCodeText=$("#orgregPhnNocountryCode").find("option:selected").text();
		let regPhNo    = countryCode+"-"+$("#regPhoneNumber1").val()+$("#regPhoneNumber2").val()+$("#regPhoneNumber3").val();
		let regWebUrl  = $("#weburl").val();
		let GSTApplicable = $("#GSTApplicable").find("option:selected").val();
		let GSTApplicableText = $("#GSTApplicable").find("option:selected").text();
		let finStYr    = $("#fincstartyear").val();
		let finEndYr   = $("#fincendyear").val();
		if(finStYr == ""){
			swal("Error in data field!","Please enter financial year start date","error");
			return false;
		}

		if(finEndYr == ""){
			swal("Error in data field!","Please enter financial year end date","error");
			return false;
		}

		if(GSTApplicable == ""){
			swal("Error!","Please choose YES/No for India GST legislation Applicable.","error");
			return false;
		}else{
			swal("Warning!","IMPORTANT: You have chosen India GST legislation Applicable as " + GSTApplicableText + ". To change it in future you need to create new company.","error");
		}

		let auditedAccountsUploads="";
		$('select[id="auditedAccountsUploads"] option').each(function () {
			if(auditedAccountsUploads==""){
				auditedAccountsUploads= this.value;
			}else{
				auditedAccountsUploads+= ','+this.value;
			}
		});

		let taxReturnsUploads="";
		$('select[id="taxReturnsUploads"] option').each(function () {
			if(taxReturnsUploads==""){
				taxReturnsUploads=this.value;
			}else{
				taxReturnsUploads+=','+this.value;
			}
		});

		let organizationChartUploads="";
		$('select[id="organizationChartUploads"] option').each(function () {
			if(organizationChartUploads==""){
				organizationChartUploads=this.value;
			}else{
				organizationChartUploads+=','+this.value;

			}
		});

		let accountingManualUploads="";
		$('select[id="accountingManualUploads"] option').each(function () {
			if(accountingManualUploads==""){
				accountingManualUploads=this.value;
			}else{
				accountingManualUploads+=','+this.value;
			}
		});

		let listOfStatergiesUploads="";
		$('select[id="listOfStatergiesUploads"] option').each(function () {
			if(listOfStatergiesUploads==""){
				listOfStatergiesUploads=this.value;
			}else{
				listOfStatergiesUploads+=','+this.value;
			}
		});

		let templatesForCompanyUploads="";
		$('select[id="templatesForCompanyUploads"] option').each(function () {
			if(templatesForCompanyUploads==""){
				templatesForCompanyUploads=this.value;
			}else{
				templatesForCompanyUploads+=','+this.value;
			}
		});
		let country    =$("#country").val();
		let currency   =$("#currency").val();
		/*  let prevYrAcc  = $('input[name$="prevYrAcc"]').val();
          let prevYrTaxRtrn  = $('input[name$="prevYrTaxRtn"]').val();
          let orgChart   = $('input[name$="orgChart"]').val();
          let accMan     = $('input[name$="accMan"]').val();
          let signList=$('input[name$="signatoriesList"]').val();
          let companyTemplates=$('input[name$="companytemplates"]').val();*/
		let compLogo=$('input[id="uploadLogo"]').val();
		let itemLisingAllowed="";let compLogoBase64encodeData="";
		let isCompositeScheme="";

		if($("input[name='productListing']").prop("checked")){
			itemLisingAllowed="1";
		}else{
			itemLisingAllowed="0";
		}
		if($("input[name='isCompositionScheme']").prop("checked")){
			isCompositeScheme="1";
		}else{
			isCompositeScheme="0";
		}
		if(compName==""){
			swal("Invalid Data!","Please Fill in Company Name","error");
			return true;
		}
		let limitForBackDatedTxn = $("#backDatedTxnLimit").val();
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		let jsonData = {};
		let useremail=$("#hiddenuseremail").text();
		jsonData.usermail = useremail;
		jsonData.orgId     = orgId;
		jsonData.companyName = compName;
		jsonData.corpEmail    = corporateEmail;
		jsonData.addr      = regAddr;
		jsonData.regphnoccode=ctryCodeText;
		jsonData.phno      = regPhNo;
		jsonData.weburl    = regWebUrl;
		jsonData.GSTApplicable    = GSTApplicable;
		jsonData.orgcountry=country;
		jsonData.orgCurrency=currency;
		jsonData.finstyr   = finStYr;
		jsonData.finendyr  = finEndYr;
		/*jsonData.prevyracc = prevYrAcc;
        jsonData.prevyrtaxrtrn = prevYrTaxRtrn;
        jsonData.accman    = accMan;
        jsonData.orgchart=orgChart;
        jsonData.signatoryList= signList;
        jsonData.compTemplates  = companyTemplates;*/
		jsonData.companyLogo=compLogo;
		jsonData.companyProductListings=itemLisingAllowed;
		jsonData.isCompositionScheme = isCompositeScheme;
		jsonData.auditedAccountsUploads=auditedAccountsUploads;
		jsonData.taxReturnsUploads=taxReturnsUploads;
		jsonData.organizationChartUploads=organizationChartUploads;
		jsonData.accountingManualUploads=accountingManualUploads;
		jsonData.listOfStatergiesUploads=listOfStatergiesUploads;
		jsonData.templatesForCompanyUploads=templatesForCompanyUploads;
		jsonData.limitForBackDatedTxn = limitForBackDatedTxn;
		jsonData.uploadDestinationId = uploadDestinationId;
		/*if(compLogo!=null && compLogo!=""){
             inkblob = { url: compLogo};
            // filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
             filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
             filepicker.read(inkblob, {base64encode: true},
                 function(imgdata){
                    compLogoBase64encodeData+='data:image/png;base64,'+imgdata;
                     jsonData.orgLogoEncodedData=imgdata;
                     let url="/config/addOrganizationDetails";
                    $.ajax({
                        url         : url,
                        dataType    : 'json',
                        data        : JSON.stringify(jsonData),
                        method      : "POST",
                        contentType: 'application/json;',
                        success     : function (data) {
                            $("#notificationMessage").html("Successfully created organization settings.");
                            $('.notify-success').show();
                            $(".orglogo img").attr('src','data:image/png;base64,'+imgdata);
                            getOrganizationDeatils();
                            alwaysScrollTop();
                            $.unblockUI();
                        },
                        error : function (xhr, status, error) {
                        }
                    });
                 },function(fperror) {
                 }
             );
        }else{*/
		let url="/config/addOrganizationDetails";
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
				if(data){
					$("#gstCountryCode").val(data.gstcountrycode);
				}
				$("#notificationMessage").html("Successfully created organization settings.");
				$('.notify-success').show();
				//getOrganizationDeatils();
				alwaysScrollTop();
				$.unblockUI();
				window.location.reload();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
		//}
	});
});

var loadCoaExpenseItems = function(){
    if(isExpCoaLoaded){
        return;
    }
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$.ajax({
		url: '/data/getcoaexpenseitems',
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method: "GET",
		success: function(data) {
			isExpCoaLoaded = true;
			let vendoritemulsel= '<li id="vendoritemlist">&nbsp;&nbsp;&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="checkboxid" name="vendoritemcheck" value="" onClick="checkUncheck(this)">&nbsp;&nbsp;&nbsp;Select All</li>';
			let vendorTDSitemulsel= '<li id="vendTdsitemlist">&nbsp;&nbsp;&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="checkboxid" name="vendTdscheck" value="" onClick="checkUncheckTds(this)">&nbsp;&nbsp;&nbsp;Select All</li>';
			for (let i=0;i<data.coaItemData.length; i++) {
				if(data.coaItemData[i].name != null && data.coaItemData[i].name.length > 25){
					vendoritemulsel +=('<li id="vendoritemlist" name="'+data.coaItemData[i].id+'" isTdsSpecific="'+data.coaItemData[i].isTdsSpecific+'">&nbsp;<input style="margin-bottom:5px;float: left;margin-left: 4px;" type="checkbox" id="checkboxid" name="vendoritemcheck" value="'+data.coaItemData[i].id+'" onClick="checkUncheck(this)">&nbsp;&nbsp;&nbsp;<input class="span1" type="text" style="width:100px;height:20px;float: left;margin-left: 10px;" id="unitPrice'+data.coaItemData[i].id+'" name="unitPrice" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="toggleCheck(this)"><span class="itemLabelClass">'+data.coaItemData[i].name+'</span><b style="float:right;"><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onRcmTaxChange(this);"  class="cesRateVendItem" id="cesRateVendItem'+data.coaItemData[i].id+'" name="cesRateVendItem" rcmcessrate="" placeholder="Cess Rate" onkeyup="rcmToggleCheck(this);"><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onRcmTaxChange(this);" rcmtaxrate="" class="rcmRateVendItem" name="rcmRateVendItem" placeholder="RCM Tax Rate" id="rcmRateVendItem'+data.coaItemData[i].id+'" onkeyup="rcmToggleCheck(this);"><input type="text" readonly class="VendRcmApplicableDate" onkeypress="return onlyDateAllow(event);" placeholder="Applicable Date" name="VendRcmApplicableDate" id="VendRcmApplicableDate'+data.coaItemData[i].id+'"></b></li>');
				}else {
					vendoritemulsel +=('<li id="vendoritemlist" name="'+data.coaItemData[i].id+'" isTdsSpecific="'+data.coaItemData[i].isTdsSpecific+'">&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="checkboxid" name="vendoritemcheck" value="'+data.coaItemData[i].id+'" onClick="checkUncheck(this)">&nbsp;&nbsp;&nbsp;<input class="span1" type="text" style="width:100px;height:20px;" id="unitPrice'+data.coaItemData[i].id+'" name="unitPrice" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="toggleCheck(this)">&nbsp;&nbsp;&nbsp;<span class="itemLabelClass">'+data.coaItemData[i].name+'</span>&nbsp;&nbsp;&nbsp;<b style="float:right;"><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onRcmTaxChange(this);"  class="cesRateVendItem" id="cesRateVendItem'+data.coaItemData[i].id+'" name="cesRateVendItem" rcmcessrate="" placeholder="Cess Rate" onkeyup="rcmToggleCheck(this);"><input type="text" onkeypress="return onlyDotsAndNumbers(event);" onchange="onRcmTaxChange(this);" rcmtaxrate="" class="rcmRateVendItem" name="rcmRateVendItem" placeholder="RCM Tax Rate" id="rcmRateVendItem'+data.coaItemData[i].id+'" onkeyup="rcmToggleCheck(this);"><input type="text" readonly class="VendRcmApplicableDate" onkeypress="return onlyDateAllow(event);" placeholder="Applicable Date" name="VendRcmApplicableDate" id="VendRcmApplicableDate'+data.coaItemData[i].id+'"></b></li>');
				}
				if(data.coaItemData[i].isTdsSpecific){
					vendorTDSitemulsel +=('<li id="vendTdsitemlist" style="padding-top:4px;padding-bottom:4px;" name="'+data.coaItemData[i].id+'">&nbsp;<input style="margin-bottom:5px;display:inline-block;" type="checkbox" id="checkboxid" name="vendTdscheck" value="'+data.coaItemData[i].id+'" onClick="checkUncheckTds(this)">&nbsp;&nbsp;&nbsp;<span class="itemLabelClass">'+data.coaItemData[i].name+'</span>&nbsp;&nbsp;&nbsp;<div style="float: right;display:inline-block;"><button type="button" name="vendTdsSetupButton" class="btn btn-submit" style="height: 20px;float: right;padding-top: 0px;margin: 0px;display:inline-block;padding-bottom: 0px;" onclick="showVendTDSSetup(this,\''+data.coaItemData[i].id+'\',\''+data.coaItemData[i].name+'\');">TDS Setup</button> <input type="hidden" name="tdsSpecificId" class="tdsSpecificId"><input type="hidden" id="tdsWhType" name="tdsWhType" class="tdsWhType" value="'+data.coaItemData[i].tdsItemDetails[0].tdsWhType+'" ><input type="hidden" id="tdsTaxRate" name="tdsTaxRate" class="tdsTaxRate" value="'+data.coaItemData[i].tdsItemDetails[0].tdsTaxRate+'" ><input type="hidden" id="tdsTaxTransLimit" name="tdsTaxTransLimit" class="tdsTaxTransLimit" value="'+data.coaItemData[i].tdsItemDetails[0].tdsTaxTransLimit+'" ><input type="hidden" id="tdsTaxOverallLimitApply" name="tdsTaxOverallLimitApply" class="tdsTaxOverallLimitApply" value="'+data.coaItemData[i].tdsItemDetails[0].tdsTaxOverallLimitApply+'"><input type="hidden" id="overallLimit" name="overallLimit" class="overallLimit" value="'+data.coaItemData[i].tdsItemDetails[0].overallLimit+'"><input type="hidden" id="tdsFromDate" name="tdsFromDate" class="tdsFromDate" value="'+data.coaItemData[i].tdsItemDetails[0].tdsFromDate+'"><input type="hidden" id="tdsToDate" name="tdsToDate" class="tdsToDate" value="'+data.coaItemData[i].tdsItemDetails[0].tdsToDate +'"><input type="hidden" id="tdsExpenceAmount" name="tdsExpenceAmount" class="tdsExpenceAmount"><input type="hidden" id="tdsAlreadyEffected" name="tdsAlreadyEffected" class="tdsAlreadyEffected"><input type="hidden" id="tdsUptoDate" name="tdsUptoDate" class="tdsUptoDate"><input type="hidden" id="tdsSupportingDoc" name="tdsSupportingDoc" class="tdsSupportingDoc"></div></li>');
				}
			}
            $("#vendorItemList").children().remove();
			$("#vendorItemList").append(vendoritemulsel);
			$("#vendTdsItemList").children().remove();
			$("#vendTdsItemList").append(vendorTDSitemulsel);
			//$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
			$(".VendRcmApplicableDate").datepicker({
						changeMonth : true,
						changeYear : true,
						dateFormat:  'MM d,yy',
						yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
						onSelect: function(x,y){
					        $(this).focus();
					    }
					});

		},
		error: function(xhr, errorMessage, error){
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
                swal("Error on fetching expense items!", "Please retry, if problem persists contact support team", "error");
            }
		},
		complete: function(data) {
            $.unblockUI();
        }
	});
}

var loadCoaIncomeItems = function(){
	if(isIncomeCoaLoaded){
		return;
	}
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$.ajax({
		url: '/data/getcoaincomeitems',
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method: "GET",
		success: function(data) {
            isIncomeCoaLoaded = true;
			let customeritemulsel= ['<li id="customeritemlist">&nbsp;&nbsp;&nbsp;<input style="margin-bottom:5px;" type="checkbox" id="checkboxid" name="customeritemcheck" value="" onClick="custcheckUncheck(this)">&nbsp;&nbsp;&nbsp;Select All</li>'];
			let cnt = 1;
			for (let i=0; i<data.coaItemData.length; i++) {
				if(data.coaItemData[i].name.length > 40) {
					customeritemulsel[cnt++] = '<li id="customeritemlist"   style="margin-top: 10px;margin-bottom: -10px;" name="';
				}else {
					customeritemulsel[cnt++] = '<li id="customeritemlist" style="margin-top: 10px;" name="';
				}
                customeritemulsel[cnt++] = data.coaItemData[i].id;
                customeritemulsel[cnt++] ='">&nbsp;<input style="margin-bottom:5px;float: left;margin-left: 3px;" class="custSinUsrCheck" type="checkbox" id="checkboxid"';
				customeritemulsel[cnt++] = 'name="customeritemcheck" value="';
				customeritemulsel[cnt++] =  data.coaItemData[i].id;
				customeritemulsel[cnt++] = '" onClick="custcheckUncheck(this)">&nbsp;&nbsp;&nbsp;<input class="span1 custDiscSinUsr" type="text" id="custDiscount';
				customeritemulsel[cnt++] =  data.coaItemData[i].id;
				customeritemulsel[cnt++] = '" name="custDiscount" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="custtoggleCheck(this)" style="padding-right: 26px;width: 50px;margin-left: 5px;float: left;"><span  style="position: relative;top: 8px;float: left;" >%</span>&nbsp;<span class="itemLabelClass">';
				customeritemulsel[cnt++] = data.coaItemData[i].name;
				customeritemulsel[cnt++] = '</span></li>';
			}
            $("#customerItemList").children().remove();
			$("#customerItemList").append(customeritemulsel.join(''));
			$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
		},
		error: function(xhr, errorMessage, error){
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
                swal("Error on fetching income items!", "Please retry, if problem persists contact support team", "error");
            }
		},
		complete: function(data) {
            $.unblockUI();
        }
	});
}


function getCountryCurrencyData(){
	$.ajax({
		url: '/config/getcurrcountry',
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		Async: false,
		method: "GET",
		success: function(data) {
			logDebug("Start getCountryCurrencyData");
			let currencysel="";
			let countrysel="";
			let countryPhnCodesel="";
			for (let i=0;i<data.currencyAndCountryData.length; ++i) {
				currencysel += ('<option value="'+data.currencyAndCountryData[i].id+'">'+data.currencyAndCountryData[i].currency+'</option>');
				countrysel += ('<option value="'+data.currencyAndCountryData[i].id+'">' +data.currencyAndCountryData[i].name + '</option>');
			}
			for(let i=0;i<data.phoneCodeData.length;++i){
				countryPhnCodesel += ('<option value="'+data.phoneCodeData[i].id+'">' +data.phoneCodeData[i].name + '</option>');
			}
			$('.currencyDropDown').append(currencysel);
			$(".countryDropDown").append(countrysel);
			$(".countryPhnCode").append(countryPhnCodesel);
			logDebug("End getCountryCurrencyData");
		},
		error: function(xhr, errorMessage, error){
			if(xhr.status == 401){
				doLogout();
			}
		}

	});
}

var ALL_BRANCH_OF_ORG_MAP = {};
var VENDOR_BRANCH_MAP = {};
var CUSTOMER_BRANCH_MAP ={};
var COA_INCOME_BRANCHES_MAP = {};
var COA_EXPENSE_BRANCHES_MAP = {};
var COA_ASSET_LIAB_BRANCHES_MAP = {};
var ALL_VENDORS_MAP = {};
function getOrgConfigData(){
	$.ajax({
		url: '/config/getorgdatas',
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		Async: true,
		method: "GET",
		success: function(data) {
			logDebug("Start getOrgConfigData");
            $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
			//getOrganizationDeatils(); // Sunil; Not needed get called from showdivandactiveleftmenu
			$("#myTd1").html("");
			$("#myTd2").html("");
			$("#myTd3").html("");
			$("input[name='bnchName']").attr('onfocus','getExistedData(this)');
			$("input[name='items']").attr('onfocus','getExistedData(this)');
			$("input[name='projectname']").attr('onfocus','getExistedData(this)');
			$("#myTd1").append('<textarea name="jobDescription" id="jobDescription" class="text-area-2x m-bottom-10" placeholder="Job Description"></textarea>'+
	    	      '<br/><select name="positionRequiresApproval" id="positionRequiresApproval">'+
	    	      '<option value="">--Please Select--</option>'+
	    	      '<option value="1">Yes</option>'+
	              '<option value="2">No</option></select>');
			$("#myTd2").append('<input type="text" name="placeOfAdvertisement" id="placeOfAdvertisement" placeholder="Place Of Advertisement" class="m-bottom-10">'+
   		  		'<br/><input type="text" name="hiringBudget" id="hiringBudget" onkeypress="return onlyDotsAndNumbers(event)" placeholder="Budget" class="money">');
			$("#myTd3").append('<input type="text" id="empAggreementDoc" name="empAggreementDoc" readonly="readonly">'+
             	'<span id="empAggreementDoc" class="btn-idos-flat-white btn-upload m-top-10" onclick="uploadFile(this.id,this)"><i class="fa fa-upload pr-5"></i>Upload</span>');
			//let userlisttable=$("#usersTable");
			//let branchlisttable=$("#branchTable");

			$("#branchTable tbody").html("");
			$("#vendorTable tbody").html("");
			$("#customerTable tbody").html("");
			$("#projectTable tbody").html("");
			$("#usersTable tbody").html("");
			$("#taxTable tbody").html("");
			let taxTable=$("#taxTable");

			$("#orgLabel").html('');

			let bnchsel=$("#bnchLabel");
			let itemcatsel=$("#itemCategory");

			$("#userRole").children().remove();

			$("#transactionCreationForProject").children().remove();
			$("#transactionApprovalForProject").children().remove();
			$("#bomProjectId").children().remove();
			$(".multiBranch").children().remove();
            ORG_PLACE_OF_SUPPLY_TYPE = data.placeOfSupplyType;
			$("#taxSetup select[id='placeOfSupplyType']").val(data.placeOfSupplyType);
			let orgsel= "";
			for (let i=0;i<data.organizationData.length; ++i) {
				let orgId=data.organizationData[i].id;
				let orgName=data.organizationData[i].name;
				let corporateEmail=data.organizationData[i].corporateEmail;
				orgsel += ('<li id="org'+orgId+'" class="orgclass"><a id="org'+orgId+'" title="Organization Name" href="#branchSetup" class="filter-item color-label labelstyle-fc2929 selected"><input style="display:none" type="checkbox" id="'+orgId+'" value="'+orgId+'"><span class="color"></span><span>'+orgName+'</span></a></li>');
				$("#companyName").val(orgName);
				$("#hiddenOrgId").val(orgId);
				$("#corporateemail").val(corporateEmail);
			}
			$("#orgLabel").append(orgsel);
			let projectLabourProficiencyList = "";
			for(let i=0;i<data.lanuageListData.length;++i){
				projectLabourProficiencyList += ('<li class="lanProf" id="'+data.lanuageListData[i].name+'">&nbsp;&nbsp;<input type="hidden" id="langProfId" value=""/><input type="checkbox" class="langProfEnable" id="langProf_0" value="' + data.lanuageListData[i].name + '" style="margin: auto;" onclick="langCheckUncheck(this);"/>&nbsp;&nbsp;<span class="lanProfHead" style="display: inline-block; width: 85px;">' + data.lanuageListData[i].name + '</span>&nbsp;&nbsp;<span class="langProfCheckbox"><input style="width:110px;" class="langProfValues1" name="langProfValues1" id="langProfValues1" /></span>&nbsp;&nbsp;<span class="langProfCheckbox"><input style="width:110px;" class="langProfValues2" name="langProfValues2" id="langProfValues2" /></span>&nbsp;&nbsp;<span class="langProfCheckbox"><input style="width:110px;" class="langProfValues3" name="langProfValues3" id="langProfValues3" /></span></li>');
			}
			$('#projectLabourProficiencyList').append(projectLabourProficiencyList);
			let bnkActTypeSel= "";
			for(let i=0;i<data.bnkactTypean.length;++i){
				bnkActTypeSel += ('<option value="'+data.bnkactTypean[i].id+'">' +data.bnkactTypean[i].name + '</option>');
			}
			$("#bnkActType").append(bnkActTypeSel);
			let userrolesel="";
			for (let i=0;i<data.userroleData.length; ++i) {
				if(data.userroleData[i].id!="8"){
					userrolesel += ('<option value="'+data.userroleData[i].id+'">' +data.userroleData[i].name + '</option>');
				}
			}
			$("#userRole").append(userrolesel);

			let incomeExpenseItems = ""; let expenseItems = ""; let expenseTaxTable = ""; let branchTrList = "";
			for(let i=0;i<data.branchListData.length;i++){
				let countryName="";
				if(data.branchListData[i].country!=""){
					countryName = $("#branchCountry").find('option[value='+data.branchListData[i].country+']').text();
				}
				incomeExpenseItems += ('<li class="branchEntity'+data.branchListData[i].id+'"><div class="chartOfAccountContainer"><img id="branchEntity'+data.branchListData[i].id+'" src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><font class="color-grey"><b id="branchEntity'+data.branchListData[i].id+'" style="margin-left:2px;">'+data.branchListData[i].name+'</b></font></div>');

				incomeExpenseItems += ('<ul id="mainBranchIncomeChartOfAccount" class="treeview-black mainBranchIncomeChartOfAccount">');
				incomeExpenseItems += ('<li id="branchEntity'+data.branchListData[i].id+'"><div class="chartOfAccountContainer"><img id="branchEntity'+data.branchListData[i].id+'" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="javascript:getChildIncomesChartOfAccount(this,\''+data.gstCountryCode+'\');"></img><font class="color-grey"><b style="margin-left:2px;">Incomes</b></font></div></li></ul></li>');
            
				expenseItems += ('<li class="branchEntityRcm'+data.branchListData[i].id+'"><div class="chartOfAccountContainer"><img id="branchEntityRcm'+data.branchListData[i].id+'" src="/assets/images/minus.png" style="margin-top:-2px;" id="getNodeChild"></img><font class="color-grey"><b id="branchEntityRcm'+data.branchListData[i].id+'" style="margin-left:2px;">'+data.branchListData[i].name+'</b></font></div>');
				expenseItems += ('<ul id="mainBranchExpenceChartOfAccount" class="treeview-black mainBranchExpenceChartOfAccount">');
				expenseItems += ('<li id="branchEntityRcm'+data.branchListData[i].id+'"><div class="chartOfAccountContainer"><img id="branchEntityRcm'+data.branchListData[i].id+'" src="/assets/images/new.v1370889834.png" style="margin-top:-2px;" id="getNodeChild" onclick="javascript:getChildRcmChartOfAccount(this,\''+data.gstCountryCode+'\');"></img><font class="color-grey"><b style="margin-left:2px;">Expence</b></font></div></li></ul></li>');

				branchTrList  += '<tr name="branchEntity'+data.branchListData[i].id+'"><td>'+data.branchListData[i].name+'</td><td>'+data.branchListData[i].branchgstin+'</td><td>'+countryName+'</td><td>'+(data.branchListData[i].location==null?"":data.branchListData[i].location)+'</td><td>'+(data.branchListData[i].phone==null?"":data.branchListData[i].phone)+'</td><td><button href="#branchSetup" class="btn btn-submit" onClick="showBranchEntityDetails(this);" id="show-entity-details'+data.branchListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>';
				if(data.branchListData[i].actionText == "Deactivate") {
					branchTrList += '<td><button href="#branchSetup" class="btn btn-submit" onClick="deactivateBranchEntityDetails(this);" id="deactivate-entity-details' + data.branchListData[i].id + '"><i class="far fa-trash-alt fa-lg pr-5"></i>' + data.branchListData[i].actionText + '</button></td></tr>';
				}else{
					branchTrList += '<td><button href="#branchSetup" class="btn btn-submit" onClick="deactivateBranchEntityDetails(this);" id="deactivate-entity-details' + data.branchListData[i].id + '"><i class="far fa-check-square fa-lg pr-5"></i>' + data.branchListData[i].actionText + '</button></td></tr>';
				}
			}
			$("#branchTable tbody").append(branchTrList);
			$("#mainBranchIncomeChartOfAccount").append(incomeExpenseItems);
			$("#mainBranchRCMChartOfAccount").append(expenseItems);
			let vendorlisttable	= "";
			let customerlisttable= "";
			for(let i=0;i<data.vendorListData.length;i++){
				if(data.vendorListData[i].type=="Vendor"){
					vendorlisttable += '<tr name="vendorEntity'+data.vendorListData[i].id+'"><td>'+data.vendorListData[i].name+'</td><td>'+data.vendorListData[i].location+'</td><td>'+data.vendorListData[i].email+'<div class="grantAccess" style="float:right;">';

					if(data.vendorListData[i].grantAccess=="0"){
						vendorlisttable += ('<a href="#vendorSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Grant Access"><i class="fa fa-check pr-5"></i></a>');
					}else if(data.vendorListData[i].grantAccess=="1"){
						vendorlisttable += ('<a href="#vendorSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Remove Access"><i class="fa fa-times pr-5"></i></a>');
					}
					vendorlisttable += '</div></td><td>'+data.vendorListData[i].phone+'</td><td>'+data.vendorListData[i].address+'</td><td><a href="#vendorSetup" class="btn btn-submit" onClick="showVendorEntityDetails(this)" id="show-entity-details'+data.vendorListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</a></td>';

					if(data.vendorListData[i].presentStatus == 1){
						vendorlisttable += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 0)"  id="entity-details'+data.vendorListData[i].id+'"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td></tr>');
					}else if(data.vendorListData[i].presentStatus == 0){
						vendorlisttable += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 1)"  id="entity-details'+data.vendorListData[i].id+'"><i class="far fa-check-square fa-lg pr-5"></i>Activate</button></td></tr>');

					}else{
						vendorlisttable += ('<td></td></tr>');
					}
                    ALL_VENDORS_MAP[data.vendorListData[i].id] = '<option value="'+data.vendorListData[i].id+'">' +data.vendorListData[i].name + '</option>';
				}else if(data.vendorListData[i].type=="Customer"){
					customerlisttable += '<tr name="customerEntity'+data.vendorListData[i].id+'"><td>'+data.vendorListData[i].name+'</td><td>'+data.vendorListData[i].location+'</td><td>'+data.vendorListData[i].email+'<div class="grantAccess" style="float:right;">';

					if(data.vendorListData[i].grantAccess=="0"){
						customerlisttable += ('<a href="#customerSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Grant Access"><i class="fa fa-check pr-5"></i></a>');
					}else if(data.vendorListData[i].grantAccess=="1"){
						customerlisttable += ('<a href="#customerSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Remove Access"><i class="fa fa-times pr-5"></i></a>');
					}
					customerlisttable += '</div></td><td>'+data.vendorListData[i].phone+'</td><td>'+data.vendorListData[i].address+'</td><td><button href="#customerSetup" class="btn btn-submit" onClick="showCustomerEntityDetails(this)" id="show-entity-details'+data.vendorListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>';
					if(data.vendorListData[i].presentStatus == 1){
						customerlisttable += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 0)"  id="entity-details'+data.vendorListData[i].id+'"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td></tr>');
					}else if(data.vendorListData[i].presentStatus == 0){
						customerlisttable += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 1)" id="entity-details'+data.vendorListData[i].id+'"><i class="far fa-check-square fa-lg pr-5"></i>Activate</button></td></tr>');
					}else{
						customerlisttable += ('<td></td></tr>');
					}
				}
			}
			$("#vendorTable").append(vendorlisttable);
			$("#customerTable").append(customerlisttable);
			let vendorlistArray = Object.values(ALL_VENDORS_MAP);
			let vendorList = vendorlistArray.join('');
            $("#rcmtaxtr select[id='rcmTaxVendor']").append(vendorList);
			localStorage.setItem("vendorListDropDown", vendorList);
			let projectlisttable= "";  let txnRuleForProject = "";
			for(let i=0;i<data.projectListData.length;i++){
				projectlisttable += ('<tr name="projectEntity'+data.projectListData[i].id+'"><td>'+data.projectListData[i].name+'</td><td>'+data.projectListData[i].number+'</td><td>'+data.projectListData[i].startDate+'</td><td>'+data.projectListData[i].endDate+'</td><td>'+data.projectListData[i].location+'</td><td><button href="#projectSetup" class="btn btn-submit" onClick="showProjectEntityDetails(this)" id="show-entity-details'+data.projectListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td><td><button href="#projectSetup" class="btn btn-submit" onClick="deactivateProjectEntityDetails(this)" id="deactivate-entity-details'+data.projectListData[i].id+'"><i class="far fa-trash-alt fa-lg pr-5"></i>'+data.projectListData[i].actionText+'</button></td></tr>');
                txnRuleForProject += ('<option value="'+data.projectListData[i].id+'">' +data.projectListData[i].name + '</option>');
			}
            $("#projectTable").append(projectlisttable);
            $("#transactionApprovalForProject").append(txnRuleForProject);
            $("#transactionCreationForProject").append(txnRuleForProject);
			$("#prCreateProjectSelect").append(txnRuleForProject);
			$("#poCreateProjectSelect").append(txnRuleForProject);
			$("#bomProjectId").append('<option value="">Please Select</option>' + txnRuleForProject);

			for (let i=0;i<data.userbranchData.length; ++i) {
				if(data.userbranchData[i].isHeadQuarters==1){
					$("#bnchName").val(data.userbranchData[i].name);
					$("#branchEntityHiddenId").val(data.userbranchData[i].id);
				}
				ALL_BRANCH_OF_ORG_MAP[data.userbranchData[i].id] = '<option id="'+data.userbranchData[i].gstin+'" value="'+data.userbranchData[i].id+'">' +data.userbranchData[i].name + '</option>';

				$("#prCreateBranchSelection").append('<option value="'+data.userbranchData[i].id+'">' +data.userbranchData[i].name + '</option>');
				$("#poCreateBranchSelection").append('<option value="'+data.userbranchData[i].id+'">' +data.userbranchData[i].name + '</option>');
				$(".docuploadrulecustomdropdownBranchList").append('<li id="docuploadrulecustomdropdownBranchlist">&nbsp;&nbsp;<input type="checkbox" name="customdoccheckBranch" id="customdoccheckBranch" value="'+data.userbranchData[i].id+'" onclick="customdoccheckUncheck(this);"/>&nbsp;&nbsp;<input type="text" class="input-small" name="monetoryLimit" id="monetoryLimit'+data.userbranchData[i].id+'" value="0.0" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="customdoctoggleCheck(this)">&nbsp;&nbsp;'+data.userbranchData[i].name+'</li>');
				$(".multiBranch").each(function() {
					let elemId=this.name;
					if(data.userbranchData.length==1){
					  $(this).append('<option value="'+data.userbranchData[i].id+'" selected="selected">' +data.userbranchData[i].name + '</option>');
					}else{
					  $(this).append('<option value="'+data.userbranchData[i].id+'">' +data.userbranchData[i].name + '</option>');
					}
				});
                COA_INCOME_BRANCHES_MAP[data.userbranchData[i].id] = ('<li class="customerVendorBranchCls" id="'+data.userbranchData[i].id+'"><input type="checkbox" id="coaBranchCheck" class="itemBranch2Class-cb" value="'+data.userbranchData[i].id+'">'+data.userbranchData[i].name+'<b style="float:right;"><input type="text" id="incBranchItemOB" class="openingBalance" placeholder="Opening Balance" onkeypress="return onlyDotsAndNumbersandMinus(event, this);" onblur="validateCoaBranchData(this, '+data.userbranchData[i].id+', 1);"><input type="text" name="incomeItemDiscount" id="itemBranch2Class-cb'+data.userbranchData[i].id+'" class="txtBox80" onkeypress="return onlyDotsAndNumbersandMinus(event, this);" onblur="validateCoaBranchData(this, '+data.userbranchData[i].id+', 1);" placeholder="Discount %"></b></li>');

                COA_EXPENSE_BRANCHES_MAP[data.userbranchData[i].id] = ('<li class="customerVendorBranchCls" id="'+data.userbranchData[i].id+'"><input type="checkbox" id="coaBranchCheck" class="itemBranch2Class-cb" value="'+data.userbranchData[i].id+'">'+data.userbranchData[i].name+'<b style="float:right;"><input type="text" id="expBranchItemOB" class="openingBalance" placeholder="Opening Balance" onkeypress="return onlyDotsAndNumbersandMinus(event, this)" onblur="validateCoaBranchData(this, '+data.userbranchData[i].id+', 2);"><input type="text" id="noOfUnits" placeholder="No of Units" onkeypress="return onlyDotsAndNumbersandMinus(event, this)" onblur="validateCoaBranchData(this, '+data.userbranchData[i].id+',2);" class="txtBox80"><input type="text" id="inventoryRate" placeholder="Rate" onkeypress="return onlyDotsAndNumbersandMinus(event, this)" onblur="validateCoaBranchData(this, '+data.userbranchData[i].id+', 2);" class="txtBox80"><input type="text" id="inventoryValue" placeholder="Inventory Value" onkeypress="return onlyDotsAndNumbersandMinus(event, this);" onblur="validateCoaBranchData(this, '+data.userbranchData[i].id+', 2);" readonly></b></li>');

                COA_ASSET_LIAB_BRANCHES_MAP[data.userbranchData[i].id] = ('<li class="customerVendorBranchCls" id="'+data.userbranchData[i].id+'"><input type="checkbox" id="coaBranchCheck" class="itemBranch2Class-cb" value="'+data.userbranchData[i].id+'">'+data.userbranchData[i].name+'<b style="float:right;"><input type="text" id="astlibBranchItemOB" class="openingBalance" placeholder="Opening Balance" onkeypress="return onlyDotsAndNumbersandMinus(event, this);" onblur="validateCoaBranchData(this, '+data.userbranchData[i].id+', 34);"></b></li>');

				CUSTOMER_BRANCH_MAP[data.userbranchData[i].id] = '<li class="customerVendorBranchCls" id="'+data.userbranchData[i].id+'" ><input type="checkbox" id="custBranchCheck" onclick="checkUncheckBranches(this,customerBranchDropdownBtn);" value="'+data.userbranchData[i].id+'"><span class="branchNameLabel">'+data.userbranchData[i].name+'</span><b style="float:right;"><input onkeypress="return onlyDotsAndNumbersandMinus(event, this);" type="text" id="custOpenBalance" class="openingBalance" placeholder="Opening balance" onblur="totalOpeningBalances(this,'+data.userbranchData[i].id+', \'ob\');"><input onkeypress="return onlyDotsAndNumbersandMinus(event, this);" type="text" id="custOpenBalanceAdvPaid" class="openingBalanceAP" placeholder="Advance Paid Opening Balance" onblur="totalOpeningBalances(this,'+data.userbranchData[i].id+', \'obap\');"></b></li>';

                VENDOR_BRANCH_MAP[data.userbranchData[i].id] = '<li class="customerVendorBranchCls" id="'+data.userbranchData[i].id+'"><input type="checkbox" id="vendorBranchCheck" onclick="checkUncheckBranches(this,vendorBranchDropdownBtn);" value="'+data.userbranchData[i].id+'"><span class="branchNameLabel">'+data.userbranchData[i].name+'</span><b style="float:right;"><input onkeypress="return onlyDotsAndNumbersandMinus(event, this);" type="text" id="vendorOpenBalance" class="openingBalance" placeholder="Opening balance" onblur="totalOpeningBalances(this,'+data.userbranchData[i].id+', \'ob\');"><input onkeypress="return onlyDotsAndNumbersandMinus(event, this);" type="text" id="vendorOpenBalanceAdvPaid" class="openingBalanceAP" placeholder="Advance Paid Opening Balance" onblur="totalOpeningBalances(this,'+data.userbranchData[i].id+', \'obap\');"></b></li>';
			}
            refreshBranchOnSaveOrUpdate('load', 'load', 'load');
			//getAllChartOfAccount();	     //Sunil now will call from whne user click on usersetup
		},
		error: function(xhr, errorMessage, error){
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
                swal("Error on fetching Organisation data!", "Please retry, if problem persists contact support team", "error");
            }
		},
        complete: function(data) {
            $.unblockUI();
            logDebug("End getOrgConfigData");
        }
	});
	logDebug("getOrgConfigData2");
}

function getOrganizationDeatils(){
	$.ajax({
		url: '/config/getorg',
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method: "GET",
		success: function(data) {
			logDebug("getOrganizationDeatils");
			 $(".orgnKeyOff").find("#dynmKeyOfficialDiv").html("");
			 $("#orgDynmStatutory").html("");
			 for (let i=0;i<data.organizationData.length; i++) {
				 if(data.organizationData[i].name!=null && data.organizationData[i].name!=""){
					 $("#companyName").val(data.organizationData[i].name);
				 }
				 if(data.organizationData[i].regPhnCtryCode!=null && data.organizationData[i].regPhnCtryCode!=""){
					 $("#orgregPhnNocountryCode option").filter(function () {return $(this).html()==data.organizationData[i].regPhnCtryCode;}).prop("selected", "selected");
				 }

				 if(data.organizationData[i].regPhn!=null && data.organizationData[i].regPhn!=""){
					 $("#regPhoneNumber1").val(data.organizationData[i].regPhn.substring(0,3));
					 $("#regPhoneNumber2").val(data.organizationData[i].regPhn.substring(3,6));
					 $("#regPhoneNumber3").val(data.organizationData[i].regPhn.substring(6,10));
				 }
				 if(data.organizationData[i].country!=null && data.organizationData[i].country!=""){
					 $("#country option").filter(function () {return $(this).val()==data.organizationData[i].country;}).prop("selected", "selected");
				 }
				 if(data.organizationData[i].currency!=null && data.organizationData[i].currency!=""){
					 $("#currency option").filter(function () {return $(this).val()==data.organizationData[i].currency;}).prop("selected", "selected");
				 }
				 if(data.organizationData[i].corporateEmail!=null && data.organizationData[i].corporateEmail!=""){
					 $("#corporateemail").val(data.organizationData[i].corporateEmail);
				 }
				 if(data.organizationData[i].webUrl!=null && data.organizationData[i].webUrl!=""){
					 $("#weburl").val(data.organizationData[i].webUrl);
				 }
				 if(data.organizationData[i].GSTApplicable!=null && data.organizationData[i].GSTApplicable!=""){
					 $("#GSTApplicable option").filter(function () {return $(this).val()==data.organizationData[i].GSTApplicable;}).prop("selected", "selected");
				 }
				 if(data.organizationData[i].regAdd!=null && data.organizationData[i].regAdd!=""){
					 $("#registeredaddress").val(data.organizationData[i].regAdd);
				 }
				 if(data.organizationData[i].finStDate!=null && data.organizationData[i].finStDate!=""){
					 $("#fincstartyear").val(data.organizationData[i].finStDate);
				 }
				 if(data.organizationData[i].finEndDate!=null && data.organizationData[i].finEndDate!=""){
					 $("#fincendyear").val(data.organizationData[i].finEndDate);
				 }

				 if(data.organizationData[i].prevAuditReport!=null && data.organizationData[i].prevAuditReport!=""){
					 fillSelectElementWithUploadedDocs(data.organizationData[i].prevAuditReport, 'organizationFinDetails', 'auditedAccountsUpload');
				 }
				 if(data.organizationData[i].prevYrTaxRtrn!=null && data.organizationData[i].prevYrTaxRtrn!=""){
					 fillSelectElementWithUploadedDocs(data.organizationData[i].prevYrTaxRtrn, 'organizationFinDetails', 'taxReturnsUpload');
				 }
				 if(data.organizationData[i].orgnChart!=null && data.organizationData[i].orgnChart!=""){
					 fillSelectElementWithUploadedDocs(data.organizationData[i].orgnChart, 'organizationFinDetails', 'organizationChartUpload');
				 }
				 if(data.organizationData[i].acctManual!=null && data.organizationData[i].acctManual!=""){
					 fillSelectElementWithUploadedDocs(data.organizationData[i].acctManual, 'organizationFinDetails', 'accountingManualUpload');
				 }
				 if(data.organizationData[i].signatoryList!=null && data.organizationData[i].signatoryList!=""){
					 fillSelectElementWithUploadedDocs(data.organizationData[i].signatoryList, 'organizationFinDetails', 'listOfStatergiesUpload');
				 }
				 if(data.organizationData[i].companyTemplates!=null && data.organizationData[i].companyTemplates!=""){
					 fillSelectElementWithUploadedDocs(data.organizationData[i].companyTemplates, 'organizationFinDetails', 'templatesForCompanyUpload');
				 }

				/* if(data.organizationData[i].prevAuditReport!=null && data.organizationData[i].prevAuditReport!=""){
					 $("input[name$='prevYrAcc']").val(data.organizationData[i].prevAuditReport);
				 }
				 if(data.organizationData[i].prevYrTaxRtrn!=null && data.organizationData[i].prevYrTaxRtrn!=""){
					 $("input[name$='prevYrTaxRtn']").val(data.organizationData[i].prevYrTaxRtrn);
				 }
				 if(data.organizationData[i].orgnChart!=null && data.organizationData[i].orgnChart!=""){
					 $("input[name$='orgChart']").val(data.organizationData[i].orgnChart);
				 }
				 if(data.organizationData[i].acctManual!=null && data.organizationData[i].acctManual!=""){
					 $("input[name$='accMan']").val(data.organizationData[i].acctManual);
				 }
				 if(data.organizationData[i].signatoryList!=null && data.organizationData[i].signatoryList!=""){
					 $("input[name$='signatoriesList']").val(data.organizationData[i].signatoryList);
				 }
				 if(data.organizationData[i].companyTemplates!=null && data.organizationData[i].companyTemplates!=""){
					 $("input[name$='companytemplates']").val(data.organizationData[i].companyTemplates);
				 }*/
				 if(data.organizationData[i].corporateInformation!=null && data.organizationData[i].corporateInformation!=""){
					 $("input[name$='corpInfo']").val(data.organizationData[i].corporateInformation);
				 }
				 if(data.organizationData[i].companyLogo!=null && data.organizationData[i].companyLogo!=""){
					 $("input[name$='orgLogoUploads']").val(data.organizationData[i].companyLogo);
				 }
				 if(data.organizationData[i].companyProductListings!=null && data.organizationData[i].companyProductListings!=""){
					 if(data.organizationData[i].companyProductListings=="1"){
						 $("input[name='productListing']").prop("checked","checked");
					 }
					 if(data.organizationData[i].companyProductListings=="0"){
						 $("input[name='productListing']").prop("checked","");
					 }
				 }
				  if(data.organizationData[i].isCompositionScheme!=null && data.organizationData[i].isCompositionScheme!=""){
					 if(data.organizationData[i].isCompositionScheme=="1"){
						 $("input[name='isCompositionScheme']").prop("checked","checked");
					 }
					 if(data.organizationData[i].isCompositionScheme=="0"){
						 $("input[name='isCompositionScheme']").prop("checked","");
					 }
				 }
				  if(data.organizationData[i].limitForBackDatedTxn !=null && data.organizationData[i].limitForBackDatedTxn!=""){
					  $("#backDatedTxnLimit").val(data.organizationData[i].limitForBackDatedTxn);
					 }
			 }
			logDebug("end getOrganizationDeatils");
		},
		error: function(xhr, status, error){
			if(xhr.status == 401){ doLogout(); }
		}
	 });
}

function getOrgData() {
	let dt = $('#allOrgTable').DataTable({
		"responsive":true,
		"processing":true,
		"columns": [
		{ "data": "orgDate" },
		{ "data": "orgName" },
		{ "data": "orgPersonName" },
		{ "data": "orgContactNo" },
		{ "data": "orgContactEmail" },
		{ "data": "orgRegisteredDays" },
		{ "data": "orgUsersCount" },
		{ "data": "orgSource" }
		],
		"order": [[0, 'desc']]
	});
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$.ajax({
		url: "/organization/getallorg",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		contentType:'application/json',
		success: function (data){
			dt.rows.add(data.data).draw();
		},
		error: function(xhr, errorMessage, error){
			if(xhr.status == 401){
				swal("Unauthorized access!", "You are unauthorized, please login and try.", "error");
				setTimeout(function(){ doLogout(); }, 3000);
			}
		}
	});
	$.unblockUI();
}

var uploadOrgLogo = function(){
	let fileUrl = $("#uploadorglogofile").val();
	if(fileUrl==""){
		swal("File is not selected!", "Please upload your company logo.", "error");
		return true;
	}
	//let ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
	let jsonData={};
	let useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	let form=$('#myForm');
	let data = new FormData();
	jQuery.each($('#uploadorglogofile')[0].files, function(i, file) {
		data.append('file-'+i, file);
		if (!file.type.match('image.*')) {
			swal("File is not image!", "Please select valid company logo.", "error");
			return true;
		}

		if (file.size > 10240) {
			swal("Too big logo!", "Please select upto 10KB size company logo.", "error");
			return true;
		}

	});

	let orgId = $("#hiddenOrgId").val();
	if(orgId === ""){
		swal("Organization error!", "Please save first Organization detail then try to upload company logo.", "error");
		return true;
	}
    let compName=    $("#companyName").val();
	jsonData.orgId     = orgId;
	let url = "/orgnization/uploadlogo/"+orgId;
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	$.ajax({
		method: "POST",
		url: url,
		data: data,
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		cache: false,
		contentType: false,
		processData: false,
		success: function(data) {

			if(data.status === "failed"){
				swal("Error on logo upload!", "Please retry to upload company logo again and if problem persists, contact support.", "error");
			}else{
				swal("Company logo upload!", "Company logo uploaded", "success");
			}
			$("#uploadorglogotxt").val("");
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

var savePlaceOfSupplyType = function(elem){
    let jsonData={};
    let useremail=$("#hiddenuseremail").text();
    jsonData.usermail = useremail;
    jsonData.placeOfSupplyType = $(elem).val();
    let url = "/orgnization/savePosType";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            swal("Success!", "Successfully saved/Updated Place of Supply Type preference.", "success");
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
}

var setPlaceOfSupplyType = function(){
    $("#taxSetup select[id='placeOfSupplyType']").val(ORG_PLACE_OF_SUPPLY_TYPE);
}


function validateBackDatedDays(elem) {
	let days = $(elem).val();
	if(days != "") {
		let startDate = $('#fincstartyear').val();
		if(startDate != "") {
			let today = new Date();
			let year = today.getFullYear()-2;
			let day_start = new Date(startDate +" "+year);

			let total_days = (today - day_start) / (1000 * 60 * 60 * 24);
			if(parseFloat(total_days) < 0){
				day_start = new Date(startDate +" "+(parseInt(year)-1));
				total_days = (today - day_start) / (1000 * 60 * 60 * 24);
			}
			if(parseFloat(days) > parseFloat(total_days)) {
				$(elem).val("0");
				swal("Warning","Days Exceeds Financial Year Limit, Days Value Must be less than "+Math.round(total_days)+" days.","error");
			}
		}else {
			swal("Error in Financial year!","Select Financial Year Details First","error");
		}
	}
}

function getCompanyOrgList() {
	var jsonData = {};
	var cmpId = $("#hiddenCompanyId").val();
	jsonData.companyId = cmpId;
	var url = "/config/getCompanyOrgList";
	$.ajax({
		url: url,
		data: JSON.stringify(jsonData),
		type: "text",
		headers: {
			"X-AUTH-TOKEN": window.authToken
		},
		method: "POST",
		contentType: 'application/json',
		success: function (data) {
			$("#companyOrgTable tbody").html("");
			let companyOrgTrList = "";
			for (let i = 0; i < data.companyOrgList.length; i++) {
				companyOrgTrList += '<tr><td>' + data.companyOrgList[i].companyOrgName + '</td><td>' + data.companyOrgList[i].companyOrgPerName + '</td><td>' + data.companyOrgList[i].companyOrgEmail + '</td><td>' + data.companyOrgList[i].companyOrgPhoneNo + '</td><td>' + data.companyOrgList[i].companyOrgWebsite + '</td></tr>';
			}
			$("#companyOrgTable tbody").append(companyOrgTrList);
		},
		error: function (xhr, status, error) {
			if (xhr.status == 401) {
				doLogout();
			}
		}
	});
}

