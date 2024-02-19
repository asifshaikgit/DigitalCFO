// var BRANCH_WISE_ADVANCE_BAL = null;
// var uniqueIdCounter = 1;
$(document).ready(function(){
	$('#customerBranchDropdownBtn').click(function(){
		var parentTr=$(this).closest('tr').attr('id');
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="openCustomerBranchDropdown-menu"
			$("#"+parentTr+" div[class='customerBranchDropdown-menu']").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="customerBranchDropdown-menu";
			$("#"+parentTr+" div[class='openCustomerBranchDropdown-menu']").attr('class',divdropdown);
		}
	});

	$('#vendorBranchDropdownBtn').click(function(){
		var parentTr=$(this).closest('tr').attr('id');
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="openCustomerBranchDropdown-menu"
			$("#"+parentTr+" div[class='customerBranchDropdown-menu']").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="customerBranchDropdown-menu";
			$("#"+parentTr+" div[class='openCustomerBranchDropdown-menu']").attr('class',divdropdown);
		}
	});


	$('#customerdropdown').click(function(){
		var classval=$(this).attr('class');
		if(classval=="multiselect dropdown-toggle btn"){
			var newclassval=classval+ " " +"open";
			$(this).attr('class',newclassval);
			var divdropdown="opencustomeritemdropdown-menu"
			$(".customeritemdropdown-menu").attr('class',divdropdown);
		}
		if(classval=="multiselect dropdown-toggle btn open"){
			var newclassval="multiselect dropdown-toggle btn";
			$(this).attr('class',newclassval);
			var divdropdown="customeritemdropdown-menu"
			$(".opencustomeritemdropdown-menu").attr('class',divdropdown);
		}
	});
});

var resetVendorCustomerBranch = function(elem){
	$("#" +elem+ " input[type='button']").html("None Selected &nbsp;&nbsp;<span class='caret'></span>");
	$("#" +elem+ " input[type='checkbox']").attr('checked',false);
}

var checkUncheckBranches = function(elem, dropdown){
	var dropdownID = $(dropdown).attr('id');
	var checked=$(elem).is(':checked');
	var elementName = $(elem).attr('id');
	if(checked==true){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			$('input[id="'+elementName+'"]').each(function () {
		        $(this).prop("checked" ,true);
			});
		}
		var check_box_values = $('input[id="'+elementName+'"]:checkbox:checked').map(function () {
		    return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Branches ";
			$("#"+dropdownID).text(text);
			$("#"+dropdownID).append("&nbsp;&nbsp;<b class='caret'></b>");
		}if(check_box_values==0){
			$("#"+dropdownID).text("None Selected");
			$("#"+dropdownID).append("&nbsp;&nbsp;<b class='caret'></b>");
		}
	} else if(checked==false){
		var parentTr = $(elem).closest('tr').attr('id');
		var amount = 0;
		var parentLi = $(elem).closest('li').attr('id');
		$("#"+parentLi + " input[class='openingBalance']").val('');
		$("#"+parentTr+" input[class='openingBalance']").map(function () {
			if($(this).val().trim() != ""){
				amount += parseFloat(this.value);
			}
		}).get();
		if(parseFloat(amount)> 0){
			$("#"+parentTr+" input[class='totalOpeningBalance']").val(parseFloat(amount).toFixed(2));
		}else{
			$("#"+parentTr+" input[class='totalOpeningBalance']").val('');
		}
		amount = 0;
		$("#"+parentLi + " input[class='openingBalanceAP']").val('');
		$("#"+parentTr+" input[class='openingBalanceAP']").map(function () {
			if($(this).val().trim() != ""){
				amount += parseFloat(this.value);
			}
		}).get();
		if(parseFloat(amount)> 0){
			$("#"+parentTr+" input[class='totalOpeningBalanceAdvPaid']").val(parseFloat(amount).toFixed(2));
		}else{
			$("#"+parentTr+" input[class='totalOpeningBalanceAdvPaid']").val('');
		}
	}
	var check_box_values = $('input[id="'+elementName+'"]:checkbox:checked').map(function () {
	    return this.value;
	}).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Branches ";
		$("#"+dropdownID).text(text);
		$("#"+dropdownID).append("&nbsp;&nbsp;<b class='caret'></b>");
	}if(check_box_values==0){
		$("#"+dropdownID).text("None Selected");
		$("#"+dropdownID).append("&nbsp;&nbsp;<b class='caret'></b>");
	}

}

var totalOpeningBalances= function(elem, branchid, onType){
	var parentTr = $(elem).closest('tr').attr('id');
	var amount = 0;
	var valueTmp  = $("#"+parentTr+" li[id='"+branchid+"'] input[class='openingBalance']").val();
	var valueTmp2 = $("#"+parentTr+" li[id='"+branchid+"'] input[class='openingBalanceAP']").val();

	$("#"+parentTr+" input[value='"+branchid+"']:checkbox").prop("checked" ,true);
	if(onType =='obap'){
		$("#"+parentTr+" input[class='openingBalanceAP']").map(function () {
			if($(this).val().trim()!=""){
				amount += parseFloat(this.value);
			}
		}).get();
		if(parseFloat(amount)> 0){
			$("#"+parentTr+" input[class='totalOpeningBalanceAdvPaid']").val(parseFloat(amount).toFixed(2));
		}else{
			$("#"+parentTr+" input[class='totalOpeningBalanceAdvPaid']").val('');
		}
	}else if(onType =='ob'){
		$("#"+parentTr+" input[class='openingBalance']").map(function () {
			if($(this).val().trim()!=""){
				amount += parseFloat(this.value);
			}
		}).get();
		if(parseFloat(amount)> 0){
			$("#"+parentTr+" input[class='totalOpeningBalance']").val(parseFloat(amount).toFixed(2));
		}else{
			$("#"+parentTr+" input[class='totalOpeningBalance']").val('');
		}
	}
	if(valueTmp == "" && valueTmp2 == ""){
		$("#"+parentTr+" input[value='"+branchid+"']:checkbox").prop("checked" ,false);
	}
	var btn = $("#"+parentTr+" button[name='branchDropdownBtn']");
	var chkBox = $("#"+parentTr+" input[value='"+branchid+"']:checkbox");
	checkUncheckBranches(chkBox, btn);
}

function downloadCustomerTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/downloadCustomerTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading Customer Template!");
}

function downloadOrganizationVendorTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/config/downloadOrgVendorTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading COA Template!");

}

function addVendor(elem){
	var vendorId = $("#vendorEntityHiddenId").val();
	var btnname=this.name;
    var branchArray = $('#vendorBranchList').find('#vendorBranchCheck:checked').map(function () {
        return this.value;
    }).get();
	var vendorBranches = branchArray.toString();
	if(vendorBranches == null || vendorBranches == ""){
		swal("Invalid Branch!","Please select at least a branch", "error");
		return false;
	}
	var branchOpeningBalance = "", branchOpeningBalanceAP = "";
    for (var i in branchArray) {
        branchOpeningBalance += $("#vendorBranchList li[id='" + branchArray[i] + "'] input[class='openingBalance']").val() +",";
        branchOpeningBalanceAP += $("#vendorBranchList li[id='" + branchArray[i] + "'] input[class='openingBalanceAP']").val() +",";
    }
    if(branchOpeningBalance.length > 0){
        branchOpeningBalance = branchOpeningBalance.substring(0, branchOpeningBalance.length);
    }
    if(branchOpeningBalanceAP.length > 0){
        branchOpeningBalanceAP = branchOpeningBalanceAP.substring(0, branchOpeningBalanceAP.length);
    }
	var vendorName = $("#vendName").val();
	if(vendorName == null || vendorName == "" || vendorName.length < 3){
		swal("Invalid Vendor Name!","Please provide valid Vendor name.", "error");
		return false;
	}
	var vendorEmail = $("#vendoremail").val();
	var gstCountryCode = $("#gstCountryCode").val();
	var gstinCode = ""; var vendBusinessIndividual =""; var registeredOrUnReg ="";
	if(gstCountryCode != "" && gstCountryCode != null){
		registeredOrUnReg = $("#vendorRegisteredOrUnReg option:selected").val();
		if(registeredOrUnReg == null || registeredOrUnReg == ""){
			swal("Invalid GST register type!","Please select either Yes/No.", "error");
		    return false;
		}else if(registeredOrUnReg == "1"){
			var stateGstinCode = $("#vendorDetailsTable input[id='gstinputVend']").val();
			var gstinsecondPart = $("#vendorDetailsTable input[id='gstinput2Vend']").val();
			gstinCode= stateGstinCode + gstinsecondPart;

			if(stateGstinCode !== "" && gstinsecondPart === ""){
	            swal("Invalid GSTIN!","Second part of GSTIN cannot be empty", "error");
	            return false;
	        }
			if(gstinCode < 15 || gstinCode.length > 15){
				swal("Invalid GSTIN!","Please provide correct GSTIN", "error");
				return false;
		    }
		}
		vendBusinessIndividual = $("#vendBusinessIndividual option:selected").val();
		if(vendBusinessIndividual == null || vendBusinessIndividual == ""){
			swal("Invalid GST Vendor type!","Please select a Vendor type.", "error");
		    return false;
		}
	}
	var vendorPhnCtryCode = $("#vendorPhnNocountryCode option:selected").val();
	var vendorPhone = $("#vendorphone1").val()+$("#vendorphone2").val()+$("#vendorphone3").val();
	var vendorAddress = $("#vendorAddress").val();
	if(vendorAddress == null || vendorAddress ==""){
		swal("Invalid Address!","Please provide valid Address.", "error");
	    return false;
	}
	var vendorCountry = $("#vendorcountry option:selected").val();
	var vendorState = $("#vendorState option:selected").text();
	var vendorStateCode = $("#vendorState option:selected").val();
	if(vendorStateCode == null || vendorStateCode ==""){
		swal("Invalid State!","Please provide valid State.", "error");
	    return false;
	}
	var vendorLocation = $("#location").val();
	if(vendorLocation == ""){
		swal("Invalid location!","Please provide valid location.", "error");
	    return false;
	}
	//var vendorAgreement = $("input[name='contractAgrrement']").val();
	  var vendorAgreement="";
		 $('select[id="contractAgreementUploads"] option').each(function () {

			if(vendorAgreement==""){
				vendorAgreement= this.value;
			}else{
				vendorAgreement+= ','+this.value;
			}
		});
	var purchaseOrder = $("input[name='purchaseOrder']").val();
	var futPayAlwd = $("#futurePayment option:selected").val();
	var vendorGroup = $("#vendorGroup option:selected").val();
	var daysOfCdt = $("#daysOfCredit").val();
	var openingBalance = $("#vendOpeningBalance").val();
	var openingBalanceAdvPaid = $("#vendOpeningBalanceAdvPaid").val();
	var validyFrom = $("#vendoeContractalidityFrom").val();
	var validyTo = $("#vendoeContractalidityTo").val();
	var vendorStatutoryName1 = $("#vendorStatutoryName1").val();
	var vendorStatutoryNumber1 = $("#vendorStatutoryNumber1").val();
	var vendorStatutoryName2 = $("#vendorStatutoryName2").val();
	var vendorStatutoryNumber2 = $("#vendorStatutoryNumber2").val();
	var vendorStatutoryName3 = $("#vendorStatutoryName3").val();
	var vendorStatutoryNumber3 = $("#vendorStatutoryNumber3").val();
	var vendorStatutoryName4 = $("#vendorStatutoryName4").val();
	var vendorStatutoryNumber4 = $("#vendorStatutoryNumber4").val();
	var panNo = $("#panNoVend").val();
	if(panNo == ""){
		swal("Invalid PAN No!","Please provide valid PAN No.", "error");
	    return false;
	}
	var natureOfVend = $("#natureOfVend").val();
	var gstinCheckedVal = $('#vendorGstinTbl tbody tr input[id="gstinEnableCheck"]').map(function () {
	    return this.checked;
	}).get();
	var gstinCheckedValues = "";
	var tableTrCount = $("#vendorGstinTbl tbody tr").length;
	for (var i = 0; i < tableTrCount; i++) {
		gstinCheckedValues += "," + $("#vendorGstinTbl tbody tr:eq("+i+") input[id='gstinEnableCheck']").is(':checked');
	}
    // $("a[id*='form-container-close']").attr("href",location.hash);
	$("#newVendorform-container-close").attr("href", location.hash);
    var specfUnitPrice="";
    var specfId="";
 /*   $('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
      if(this.value!=""){
    	  specfId=specfId+this.value+",";
    	  specfUnitPrice=specfUnitPrice+$("#unitPrice"+this.value+"").val()+",";
      }
    }).get();

    */

    //
     $('input[name="unitPrice"]').each(function () {
     	if($(this).closest("li").find("#checkboxid").prop("checked") == true){
     		var valueChecked = $(this).closest("li").find("#checkboxid").val();
     		 if(valueChecked != ""){
	    	  specfId=specfId+valueChecked+",";
	    	  specfUnitPrice=specfUnitPrice+this.value+",";
      		}
     	}else {
     		specfId=specfId+",";
	    	specfUnitPrice=specfUnitPrice+"0.0,";
     	}
    });

    //
    var check_box_values = $('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
        return this.value;
    }).get();
    if(specfUnitPrice==""){
      for(var i=0;i<check_box_values.length;i++){
        specfUnitPrice+="0.0"+",";
      }
    }

    var vendRcmApplicableDateItems="";
	   var vendRcmTaxRateForItems="";
	    var vendCessTaxRateForItems="";

    $('input[name="VendRcmApplicableDate"]').map(function () {
	    	  vendRcmApplicableDateItems=vendRcmApplicableDateItems+this.value+"|";
	 }).get();
	 $('input[name="rcmRateVendItem"]').map(function () {
	    	  vendRcmTaxRateForItems=vendRcmTaxRateForItems+this.value+",";
	 }).get();
	 $('input[name="cesRateVendItem"]').map(function () {
	    	  vendCessTaxRateForItems=vendCessTaxRateForItems+this.value+",";
	 }).get();

    var jsonData = {};
    if(vendorId!=""){
      jsonData.vendId= vendorId;
      }
    if(futPayAlwd==0 || futPayAlwd==2){
      jsonData.daysOfCredit= daysOfCdt;
    }

    // Billwise Opening Balance Vendor
    var billwiseOpeningBalance = getBillWiseOpBalDetails(elem);
	// var branchWiseAdvBalance = getBranchWiseAdvBalDetails(elem);

    jsonData.useremail  = $("#hiddenuseremail").text();
    jsonData.vendName = vendorName;
    jsonData.gstinCode = gstinCode;
	jsonData.businessIndividual =vendBusinessIndividual;
	jsonData.registeredOrUnReg = registeredOrUnReg;
    jsonData.vendUnitCost = specfUnitPrice;
    jsonData.vendEmail = vendorEmail;
    jsonData.vendPhnCtryCode = vendorPhnCtryCode;
    jsonData.vendPhone = vendorPhone;
    jsonData.futurePayAlwd = futPayAlwd;
    jsonData.vendAddress = vendorAddress;
    jsonData.vendCountry = vendorCountry;
    jsonData.vendorState = vendorState;
    jsonData.vendorStateCode  =  vendorStateCode;
    jsonData.vendLocation = vendorLocation;
    jsonData.vendContAgg = vendorAgreement;
    jsonData.vendPurOrd = purchaseOrder;
    jsonData.vendSelSpecf = specfId;
    jsonData.vendRcmApplicableDateItems=vendRcmApplicableDateItems;
	jsonData.vendRcmTaxRateForItems=vendRcmTaxRateForItems;
	jsonData.vendCessTaxRateForItems=vendCessTaxRateForItems;
    jsonData.vendSelGroup = vendorGroup;
    jsonData.vendorBnchs = vendorBranches;
    jsonData.branchOpeningBalance = branchOpeningBalance;
    jsonData.branchOpeningBalanceAP = branchOpeningBalanceAP;
    jsonData.validityFrom = validyFrom;
    jsonData.validityTo = validyTo;
    jsonData.openingBalance  =  openingBalance;
	jsonData.openingBalanceAdvPaid  =  openingBalanceAdvPaid;
    jsonData.vendStatutoryName1 = vendorStatutoryName1;
    jsonData.vendStatutoryNumber1 = vendorStatutoryNumber1;
    jsonData.vendStatutoryName2 = vendorStatutoryName2;
    jsonData.vendStatutoryNumber2 = vendorStatutoryNumber2;
    jsonData.vendStatutoryName3 = vendorStatutoryName3;
    jsonData.vendStatutoryNumber3 = vendorStatutoryNumber3;
    jsonData.vendStatutoryName4 = vendorStatutoryName4;
    jsonData.vendStatutoryNumber4 = vendorStatutoryNumber4;
    jsonData.btnName  =  btnname;
    jsonData.vendPanNo = panNo;
    jsonData.natureOfVend = natureOfVend;
    jsonData.billwiseOpeningBalance = billwiseOpeningBalance;
	// jsonData.branchWiseAdvBalance = branchWiseAdvBalance

    //Start multiple GSTIN code
	jsonData.vendorDetailIdListHid  =  $("#vendorDetailIdListHid").val();
	jsonData.gstinCodeHid  =  $("#vendorGstinCodeHid").val();
	jsonData.vendorAddressHid = $("#vendorAddressHid").val();
	jsonData.vendorcountryCodeHid = $("#vendorcountryCodeHid").val();
	jsonData.vendorstateHid = $("#vendorstateHid").val();
	jsonData.vendorStateCodeHid = $("#vendorStateCodeHid").val();
	jsonData.vendorlocationHid = $("#vendorlocationHid").val();
	jsonData.vendorPhnNocountryCodeHid = $("#vendorPhnNocountryCodeHid").val();
	jsonData.vendorphone1Hid = $("#vendorphone1Hid").val();
	jsonData.vendorphone2Hid = $("#vendorphone2Hid").val();
	jsonData.vendorphone3Hid = $("#vendorphone3Hid").val();
	jsonData.gstinCheckedValues = gstinCheckedValues;
	//End multiple GSTIN code

	jsonData.vendTdsData = getVendTdsData();

    var url = "/vendor/addVendor";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
		url : url,
		data : JSON.stringify(jsonData),
		type : "text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method : "POST",
		contentType : 'text/plain',
		success : function(data) {
			if(typeof data.message !=='undefined' && data.message != ""){
				swal("Error!", data.message, "error");
				return false;
			}else if(data.info != "vendorAdded"){
				swal("Error on save/update vendor!", data.info, "error");
			} else {
				if(data.role.includes("MASTER ADMIN") || data.canCreateCustomer == true || data.canActivateCustomer == true || data.canCreateVendor == true || data.canActivateVendor == true){
					var entityType=data.entityType;
					if(entityType=="vendorCustomer"){
						$("input[type='text']").val();
						$("textarea").val();
						$('select').find('option:first').prop("selected","selected");
						$(".multiBranch").each(function () {
							$(this).removeAttr('selected');
						});
						$('.multiBranch').multiselect('rebuild');
						var vendId=data.id;
						var vendName=data.name;
						var address=(data.address)!=null?data.address:"";
						var location=(data.location)!=null?data.location:"";
						var type=data.type;
						var email=(data.email)!=null?data.email:"";
						var phone=(data.phone)!=null?data.phone:"";
						if(type==1){
							$("#newVendorform-container-close").trigger('click');
							if(data.role.includes("MASTER ADMIN") || data.canCreateVendor == true || data.canActivateVendor == true){
								var vendorlisttable=$("#vendorTable");
								$("#searchVendor option[value="+data.id+"]").remove();
								$("#searchVendor").append('<option value="'+data.id+'">'+data.name+'</option>');
								var existingVendor=$("#vendorTable").find('tr[name="vendorEntity'+data.id+'"]').attr('name');
								var vendorTableTemp = "";
								if(typeof(existingVendor)!='undefined'){
									$("#vendorTable").find('tr[name="vendorEntity'+data.id+'"]').html("");
									vendorTableTemp += ('<td>'+data.name+'</td><td>'+location+'</td><td>'+email+'</td><td style="text-align:center;"><div class="grantAccess"></div></td><td>'+phone+'</td><td>'+address+'</td><td><button class="btn btn-submit"  onClick="showVendorEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td><td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 0)" id="entity-details'+data.id+'"><i class="fas fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td>');
	
									$("#vendorTable").find('tr[name="vendorEntity'+data.id+'"]').append(vendorTableTemp);
								}else if(typeof(existingVendor)=='undefined'){
									vendorTableTemp +=('<tr name="vendorEntity'+data.id+'"><td>'+data.name+'</td><td>'+location+'</td><td>'+email+'</td><td style="text-align:center;"><div class="grantAccess"></div></td><td>'+phone+'</td><td>'+address+'</td><td><button class="btn btn-submit"  onClick="showVendorEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td><td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 0)" id="entity-details'+data.id+'"><i class="fas fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td>');
									vendorTableTemp += ('</tr>');
									$("#vendorTable").append(vendorTableTemp);
								}
								if(data.grantAccess=="0"){
									$("tr[name='vendorEntity"+data.id+"'] div[class='grantAccess']").html('<a href="#vendorSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Grant Access"><i class="fa fa-check pr-5"></i></a>');
								}else if(data.grantAccess=="1"){
									$("tr[name='vendorEntity"+data.id+"'] div[class='grantAccess']").html('<a href="#vendorSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Remove Access"><i class="fa fa-times pr-5"></i></a>');
								}
								$('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
									$('input[name="vendoritemcheck"][value=\''+this.value+'\']').prop('checked', false);
								}).get();
								$('input[name="unitPrice"]').map(function () {
									if($(this).val().trim()!=""){
										$(this).val("0.0");
									}
								}).get();
								ALL_VENDORS_MAP[data.id] = '<option value="'+data.id+'">' +data.name + '</option>';
								$("#vendordropdown").text("None Selected");
								$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
								$("#notificationMessage").html("Vendor has been added/Updated successfully.");
							}
							
						}
					}
					$.unblockUI();
					$('.notify-success').show();
					alwaysScrollTop();
				}
				if(data.role.includes("CONTROLLER")){
					getDashboardFinancials();
				}
				clearBranchBillDetails();
				// clearBranchAdvDetails();
			}
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on save/update vendor!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			// $("#vendor-form-container").hide();
			$.unblockUI();
			clearBranchBillDetails();
			resetVendTdsScreen();
		}
   });
}
$(document).ready(function() {
    // Assuming "addVendBtn" is a button, adjust the selector accordingly
    $("#addVendBtn").click(function(elem) {
        // Call the addVendBtn function here
        addVendor(elem);
    });
});

function getVendorData(){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	var url="/config/getVendorData"
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
			$("#searchVendor").children().remove();
			$("#searchCustomer").children().remove();
			var searchVendor = "";
			var searchCustomer = "";
			for(var i=0; i<data.vendorData.length; i++){
	 	        if(data.vendorData[i].type=="Vendor"){
                    searchVendor += '<option value="'+data.vendorData[i].id+'">'+data.vendorData[i].name+'</option>';
	 	        }
	 	        if(data.vendorData[i].type=="Customer"){
                    searchCustomer += '<option value="'+data.vendorData[i].id+'">'+data.vendorData[i].name+'</option>';
	 	        }
	    	 }
            $('#searchVendor').append(searchVendor);
            $('#searchCustomer').append(searchCustomer);
            $('#searchVendor').multiselect('rebuild');
            $('#searchCustomer').multiselect('rebuild');
		},
		error: function(xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on fetching vendor/customer!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

$(document).ready(function() {
    $(".vendorFreeTextSearchButton, #vendorSetupId").click(function() {
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var freeTextSearchVendor=$("#vendorFreeTextSearch").val();
		var jsonData = {};
		jsonData.freeTextSearchVendorVal=freeTextSearchVendor;
		jsonData.usermail=$("#hiddenuseremail").text();
		$("#vendorTableListDiv").show();
		var url="/vendor/searchVendor"
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
				$("#vendorTable tbody").html("");
				var vendorTemp = "";
				for(var i=0;i<data.vendorListData.length;i++){
					vendorTemp += ('<tr name="vendorEntity'+data.vendorListData[i].id+'"><td>'+data.vendorListData[i].name+'</td><td>'+data.vendorListData[i].location+'</td><td>'+data.vendorListData[i].email+'</td>');
					if(data.vendorListData[i].grantAccess=="0"){
		    			vendorTemp +=('<td style="text-align:center;"><div class="grantAccess"><a href="#vendorSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Grant Access"><i class="fa fa-check pr-5"></i></a></div></td>');
		    		}else if(data.vendorListData[i].grantAccess=="1"){
		    			vendorTemp +=('<td style="text-align:center;"><div class="grantAccess"><a href="#vendorSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Remove Access"><i class="fa fa-times pr-5"></i></a></div></td>');
		    		}
		    		if(!(data.userRoles.indexOf("MASTER ADMIN") == -1)||data.canActivateVendor==1){
		    			vendorTemp += ('<td>'+data.vendorListData[i].phone+'</td><td>'+data.vendorListData[i].address+'</td><td><button class="btn btn-submit" onClick="showVendorEntityDetails(this)" id="show-entity-details'+data.vendorListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>');
					}else{
						vendorTemp += '<td>'+data.vendorListData[i].phone+'</td><td>'+data.vendorListData[i].address+'</td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"></div></div></td>';
					}
					if(data.vendorListData[i].presentStatus == 1){
						 vendorTemp += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 0)" id="entity-details'+data.vendorListData[i].id+'"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td></tr>');
					}else if(data.vendorListData[i].presentStatus == 0){
						 vendorTemp += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 1)"  id="entity-details'+data.vendorListData[i].id+'"><i class="fa fa-check-square-o fa-lg pr-5"></i>Activate</button></td>');
					}else{
						 vendorTemp += ('<td></td>');
					}
					vendorTemp += "</tr>";
				}
				$("#vendorTable").append(vendorTemp);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
				}else if(xhr.status == 500){
	    			swal("Error on fetching vendors!", "Please retry, if problem persists contact support team", "error");
	    		}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	});
});

$(document).ready(function() {
	$(".customerFreeTextSearchButton, #customerSetupId").click(function() {
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var freeTextSearchCustomer=$("#customerFreeTextSearch").val();
		var jsonData = {};
		jsonData.freeTextSearchCustomerVal=freeTextSearchCustomer;
		jsonData.usermail=$("#hiddenuseremail").text();
		var url="/customer/searchCustomer";
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
					var vendorTemp = "";
					$("#customerTable tbody").html("");
					for(var i=0;i<data.vendorListData.length;i++){
						vendorTemp += ('<tr name="customerEntity'+data.vendorListData[i].id+'"><td>'+data.vendorListData[i].name+'</td><td>'+data.vendorListData[i].location+'</td><td>'+data.vendorListData[i].email+'</td><td style="text-align:center;"><div class="grantAccess">');
						if(data.vendorListData[i].grantAccess=="0"){
							vendorTemp +=('<a href="#customerSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Grant Access"><i class="fa fa-check pr-5"></i></a>');
						}else if(data.vendorListData[i].grantAccess=="1"){
							vendorTemp +=('<a href="#customerSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Remove Access"><i class="fa fa-times pr-5"></i></a>');
						}
						/*if(data.userRole.indexOf("1") == -1){
							vendorTemp += ('</div></td><td>'+data.vendorListData[i].phone+'</td><td>'+data.vendorListData[i].address+'</td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"></div></div></td>');
						}else{
							vendorTemp += ('</div></td><td>'+data.vendorListData[i].phone+'</td><td>'+data.vendorListData[i].address+'</td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"><a href="#customerSetup" class="button small search-open search-open btn-idos-flat-white fs-16" onClick="showCustomerEntityDetails(this)" id="show-entity-details'+data.vendorListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</div></div></td>');
						}*/
						if(!(data.userRoles.indexOf("MASTER ADMIN") == -1)||data.canActivateCustomer==1){
							vendorTemp += ('</div></td><td>'+data.vendorListData[i].phone+'</td><td>'+data.vendorListData[i].address+'</td><td><button class="btn btn-submit" onClick="showCustomerEntityDetails(this)" id="show-entity-details'+data.vendorListData[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>');
						}else{
							vendorTemp += '</div></td><td>'+data.vendorListData[i].phone+'</td><td>'+data.vendorListData[i].address+'</td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"></div></div></td>';
						}
						//alert("present status="+data.vendorListData[i].presentStatus);
						if(data.vendorListData[i].presentStatus == 1){
							vendorTemp += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 0)" id="entity-details'+data.vendorListData[i].id+'"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td></tr>');
						}else if(data.vendorListData[i].presentStatus == 0){
							vendorTemp += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 1)" id="entity-details'+data.vendorListData[i].id+'"><i class="fa fa-check-square-o fa-lg pr-5"></i>Activate</button></td>');
						}else{
							vendorTemp += ('<td></td>');
						}
						vendorTemp += "</tr>";
					}
					$("#customerTable").append(vendorTemp);
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout();
					}else if(xhr.status == 500){
		    			swal("Error on fetching customers!", "Please retry, if problem persists contact support team", "error");
		    		}
				},
				complete: function(data) {
					$.unblockUI();
				}
			});
	});
});

var changeStatusCustomerVendor = function (elem, presentStatus){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var currentTd = $(elem).closest('td');
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(14, entityId.length);
	var jsonData = {};
	jsonData.origEntityId= origEntityId;
	jsonData.presentStatus = presentStatus;
	var url="config/updatevendorstatus";
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
			currentTd.html("");
			if(presentStatus == 1){
				currentTd.html('<button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 0)" id="entity-details'+origEntityId+'"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</button>');
			}else{
				currentTd.html('<button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 1)" id="entity-details'+origEntityId+'"><i class="fa fa-check-square-o fa-lg pr-5"></i>Activate</button>');
			}
		},
		error: function () {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on processing request!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

function showVendorEntityDetails(elem){
	clearVendorPopupHidden();
	populatevendorgroupdropdown();
	 loadCoaExpenseItems();
	$(".newVendorGroupform-container").hide();
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	var detailForm="newVendorform-container";
	clearVendorForm(detailForm);
	var jsonData = {};
	jsonData.entityPrimaryId= origEntityId;
	logDebug("Start showVendorEntityDetails");
	var url="/config/vendorDetails";
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
		for(var i=0;i<data.vendordetailsData.length;i++){
			/*
			$('input[name="vendoritemcheck"]:checkbox:checked').map(function () {
	  	    	$('input[name="vendoritemcheck"][value=\''+this.value+'\']').prop('checked', false);
			}).get();
			*/
			$('#vendor-form-container input[name="unitPrice"]').map(function () {
				$(this).val("0.0");
			}).get();
			$("#vendordropdown").text("None Selected");
	        var contAgg=data.vendordetailsData[i].vendorcntBnchAgg;
	        var purOrd=data.vendordetailsData[i].vendorPurOrd;
	        if(contAgg!=null && contAgg!=""){
	        	$('#vendor-form-container input[name="contractAgrrement"]').val(contAgg);
	        }
	        if(purOrd!=null && purOrd!=""){
	        	$('#vendor-form-container input[name="purchaseOrder"]').val(purOrd);
	        }

	        $("#futurePayment option[value='"+data.vendordetailsData[i].vendorFutPayAlwd+"']").prop("selected","selected");

			if(data.vendordetailsData[i].vendorGroup != ""){
				$("#vendorGroup option[value='"+data.vendordetailsData[i].vendorGroup+"']").prop("selected","selected");
			}
	        if(data.vendordetailsData[i].vendorFutPayAlwd==0 || data.vendordetailsData[i].vendorFutPayAlwd==2){
	        	$("#daysCreditLabel").show();
	        	$("#daysOfCredit").show();
	        	$("#daysOfCredit").val(data.vendordetailsData[i].daysOfCredit);
	        }else{
	        	$("#daysCreditLabel").hide();
	        	$("#daysOfCredit").hide();
	        	$("#daysOfCredit").val("");
	        }

	        $("#vendorDetailsTable input[id='gstinputVend']").val(data.vendordetailsData[i].gstinPart1);
			$("#vendorDetailsTable input[id='gstinput2Vend']").val(data.vendordetailsData[i].gstinPart2);
			$("#vendBusinessIndividual option[value='"+data.vendordetailsData[i].businessIndividual+"']").prop("selected","selected");
			$("#vendorRegisteredOrUnReg option[value='"+data.vendordetailsData[i].registeredOrUnReg+"']").prop("selected","selected");
			if($("#vendorRegisteredOrUnReg").val()==0){
				$("#gstinputVend").attr("disabled", true);
				$("#gstinput2Vend").attr("disabled", true);
			} else if($("#vendorRegisteredOrUnReg").val()==1){
				$("#gstinputVend").attr("disabled", false);
				$("#gstinput2Vend").attr("disabled", false);
			}

			$("#tdsVendHistoryList").html("");
          	if(data.vendordetailsData[i].tdsHistoryList != "") {
                 var tdsHistoryList = data.vendordetailsData[i].tdsHistoryList;
                 tdsHistoryList = tdsHistoryList.substring(0, tdsHistoryList.length - 1);
                 var tdsHistoryListArray=tdsHistoryList.split("|");
                 for (var j = 0; j < tdsHistoryListArray.length; j++) {
                  	$("#tdsVendHistoryList").append('<li><a href="#"><b>'+tdsHistoryListArray[j]+'</b></a></li>');
                  }
              }

	        $("#vendOpeningBalance").val(data.vendordetailsData[i].openingBalance);
	        $("#vendOpeningBalanceAdvPaid").val(data.vendordetailsData[i].openingBalanceAdvPaid);
			$('#vendor-form-container input[id="vendorEntityHiddenId"]').val(data.vendordetailsData[i].id);
			$('#vendor-form-container input[id="vendName"]').val(data.vendordetailsData[i].vendorName);
			$('#vendor-form-container input[id="vendoremail"]').val(data.vendordetailsData[i].vendorEmail);
			$('#vendor-form-container input[id="vendorphone1"]').val(data.vendordetailsData[i].vendorPhone.substring(0,3));
			$('#vendor-form-container input[id="vendorphone2"]').val(data.vendordetailsData[i].vendorPhone.substring(3,6));
			$('#vendor-form-container input[id="vendorphone3"]').val(data.vendordetailsData[i].vendorPhone.substring(6,10));
			$('#vendor-form-container textarea[id="vendorAddress"]').val(data.vendordetailsData[i].vendAddress);
			var vendCtry=data.vendordetailsData[i].vendorCountryCode;
			$('#vendor-form-container select[id="vendorcountry"] option').filter(function () {return $(this).val()==vendCtry;}).prop("selected", "selected");
			$('#vendor-form-container select[id="vendorState"] option').filter(function () {return $(this).text()==data.vendordetailsData[i].vendorState;}).prop("selected", "selected");
			var vendPhnCtryCode=data.vendordetailsData[i].vendPhnCtryCode;

			$("#vendor-form-container select[id='vendorPhnNocountryCode'] option").filter(function () {return $(this).val()==data.vendordetailsData[i].vendPhnCtryCode;}).prop("selected", "selected");
			$('#vendor-form-container input[id="location"]').val(data.vendordetailsData[i].vendorLocation);
			$('#vendor-form-container input[id="vendoeContractalidityFrom"]').val(data.vendordetailsData[i].validFrom);
			$('#vendor-form-container input[id="vendoeContractalidityTo"]').val(data.vendordetailsData[i].validTo);
			//$('#vendor-form-container input[id="docuploadurl"]').val(data.vendordetailsData[i].getContractPoDoc);
			fillSelectElementWithUploadedDocs(data.vendordetailsData[i].getContractPoDoc, 'vendortr', 'contractAgreementUpload');
			$('#vendor-form-container input[id="vendorStatutoryName1"]').val(data.vendordetailsData[i].vendorStatutoryName1);
			$('#vendor-form-container input[id="vendorStatutoryNumber1"]').val(data.vendordetailsData[i].vendorStatutoryNumber1);
			$('#vendor-form-container input[id="vendorStatutoryName2"]').val(data.vendordetailsData[i].vendorStatutoryName2);
			$('#vendor-form-container input[id="vendorStatutoryNumber2"]').val(data.vendordetailsData[i].vendorStatutoryNumber2);
			$('#vendor-form-container input[id="vendorStatutoryName3"]').val(data.vendordetailsData[i].vendorStatutoryName3);
			$('#vendor-form-container input[id="vendorStatutoryNumber3"]').val(data.vendordetailsData[i].vendorStatutoryNumber3);
			$('#vendor-form-container input[id="vendorStatutoryName4"]').val(data.vendordetailsData[i].vendorStatutoryName4);
			$('#vendor-form-container input[id="vendorStatutoryNumber4"]').val(data.vendordetailsData[i].vendorStatutoryNumber4);
				var specificationId=data.vendordetailsData[i].Specifications.split(',');
				var unitPrice="";
				var vendRcmRate="";
				var vendCessRate="";
				var vendRcmApplicableDate="";
				if(data.vendordetailsData[i].vendspecfunitPrice!=null){
					unitPrice=data.vendordetailsData[i].vendspecfunitPrice.split(',');
				}
				if(data.vendordetailsData[i].vendSpecfRcmRate!=null){
					vendRcmRate=data.vendordetailsData[i].vendSpecfRcmRate.split(',');
				}
				if(data.vendordetailsData[i].vendSpecfCessRate!=null){
					vendCessRate=data.vendordetailsData[i].vendSpecfCessRate.split(',');
				}
				if(data.vendordetailsData[i].vendSpecfApplicableDate!=null){
					vendRcmApplicableDate=data.vendordetailsData[i].vendSpecfApplicableDate.split('|');
				}
				if(specificationId!=""){
                    logDebug("mid showVendorEntityDetails");
                    var counter = 0;
					for(var j=0;j<specificationId.length;j++){
						 if(specificationId[j] != ""){
						 	if(unitPrice!=""){
								$("#unitPrice"+specificationId[j]).val(unitPrice[j]);
							}else{
								$("#unitPrice"+specificationId[j]).val("0.0");
							}
							var rcmTaxStatusForDate = false;
							var rcmCessStatusForDate = false;
							if(vendRcmRate[j] != ""){
								$("#rcmRateVendItem"+specificationId[j]).val(vendRcmRate[j]);
								$("#rcmRateVendItem"+specificationId[j]).attr("rcmtaxrate",vendRcmRate[j]);
								rcmCessStatusForDate = true;
							}else{
								$("#rcmRateVendItem"+specificationId[j]).val("");
								$("#rcmRateVendItem"+specificationId[j]).attr("rcmtaxrate","");
							}

							if(vendCessRate[j] != ""){
								$("#cesRateVendItem"+specificationId[j]).val(vendCessRate[j]);
								$("#cesRateVendItem"+specificationId[j]).attr("rcmcessrate",vendCessRate[j]);
								rcmTaxStatusForDate = true;
							}else{
								$("#cesRateVendItem"+specificationId[j]).val("");
									$("#cesRateVendItem"+specificationId[j]).attr("rcmcessrate",vendCessRate[j]);
							}

							if(vendRcmApplicableDate[j] != ""){
								$("#VendRcmApplicableDate"+specificationId[j]).val(vendRcmApplicableDate[j]);
							}else{
								$("#VendRcmApplicableDate"+specificationId[j]).val("");
							}


							$('input[id="checkboxid"][value='+specificationId[j]+']').prop("checked",true);
							counter = (counter+1);
							var text=counter+" "+"Items Selected";
							$("#vendordropdown").text(text);
							$("#vendordropdown").append("&nbsp;&nbsp;<b class='caret'></b>");

							if(rcmTaxStatusForDate == true && rcmCessStatusForDate == true) {
								$("#VendRcmApplicableDate"+specificationId[j]).prop("disabled", true);
							}
						}
					 }
                    logDebug("mid end showVendorEntityDetails");
				}
				$("#natureOfVend").val(data.vendordetailsData[i].natureOfVend);
				$("#panNoVend").val(data.vendordetailsData[i].vendPanNo);

				var vendorBranchesList=data.vendordetailsData[i].vendBranches.split(','); var isPresent=false;
				var openingBalance = data.vendordetailsData[i].branchOpeningBalance.split(',');
				var openingBalanceAP = data.vendordetailsData[i].branchopeningBalanceAP.split(',');
				for(var j=0;j<vendorBranchesList.length;j++){
					if(!isEmpty(vendorBranchesList[j])){
						$('#vendor-form-container #vendorBranchList').find("input[type='checkbox'][value='"+vendorBranchesList[j]+"']").prop("checked",true);
						if(openingBalance[j] == "" || openingBalance[j] == undefined){
							$('#vendor-form-container #vendorBranchList').find("li[id='"+vendorBranchesList[j]+"'] input[class='openingBalance']").val(openingBalance[j]);
						}
						else{
							$('#vendor-form-container #vendorBranchList').find("li[id='"+vendorBranchesList[j]+"'] input[class='openingBalance']").val(parseFloat(openingBalance[j]).toFixed(2));
						}
						if(openingBalance[j] == "" || openingBalance[j] == undefined){
							$('#vendor-form-container #vendorBranchList').find("li[id='"+vendorBranchesList[j]+"'] input[class='openingBalanceAP']").val(openingBalanceAP[j]);
						}
						else{
							$('#vendor-form-container #vendorBranchList').find("li[id='"+vendorBranchesList[j]+"'] input[class='openingBalanceAP']").val(parseFloat(openingBalanceAP[j]).toFixed(2));
						}
						isPresent=true;
					}
				}
				if(isPresent){
					$('#vendor-form-container #vendorBranchDropdownBtn').html(vendorBranchesList.length + ' Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
				}
				$('.notify-success').hide();
				$("."+detailForm+"").slideDown('slow');
				$.unblockUI();

			}

			var gstinTblTrData="";
			for(var i=0; i<data.vendGstinState.length; i++){
				var gstinCode = data.vendGstinState[i].gstinCode;
				var vendorState = data.vendGstinState[i].gstState;
				var vendorID = data.vendGstinState[i].vendorID;
				var vendorStatus = data.vendGstinState[i].vendorStatus;
				var vendorLocation = data.vendGstinState[i].vendorLocation;
				if(vendorStatus === 1){
					gstinTblTrData +=('<tr><td style="cursor:pointer" onClick="popupVendorGstinDetail(\''+vendorID+'\');">'+vendorState+'</td><td>'+gstinCode+'</td><td><input id="shipVendCity" type="hidden" value="'+vendorLocation+'"/><input id="vendorDetID" type="hidden" value="'+vendorID+'"/><input type="checkbox" id="gstinEnableCheck" checked="checked"/></td></tr>');
				}else{
					gstinTblTrData+=('<tr><td style="cursor:pointer" onClick="popupVendorGstinDetail(\''+vendorID+'\');">'+vendorState+'</td><td>'+gstinCode+'</td><td><input id="shipVendCity" type="hidden" value="'+vendorLocation+'"/><input id="vendorDetID" type="hidden" value="'+vendorID+'"/><input type="checkbox" id="gstinEnableCheck"/></td></tr>');
				}
			}
			$("#vendorGstinTbl tbody").append(gstinTblTrData);
			if(data){
				$("#vendorDetailIdHid").val(data.vendorDetailIdHid);
				$("#vendorDetailIdListHid").val(data.vendorDetailIdListHid);
				$("#vendorGstinCodeHid").val(data.vendorGstinCodeHid);
				$("#vendorAddressHid").val(data.vendorAddressHid);
				$("#vendorcountryCodeHid").val(data.vendorcountryCodeHid);
				$("#vendorstateHid").val(data.vendorstateHid);
				$("#vendorStateCodeHid").val(data.vendorStateCodeHid);
				$("#vendorlocationHid").val(data.vendorlocationHid);
				$("#vendorPhnNocountryCodeHid").val(data.vendorPhnNocountryCodeHid);
				$("#vendorphone1Hid").val(data.vendorphone1Hid);
				$("#vendorphone2Hid").val(data.vendorphone2Hid);
				$("#vendorphone3Hid").val(data.vendorphone3Hid);
			}
			// BRANCH_WISE_ADVANCE_BAL = data.branchWiseAdvBalData;
			// Set Billwise Opening Balance
			setBillWiseOpBalDetails(data.billwiseOpBalanceData,"VENDOR");
			// setBranchWiseAdvBalDetails(data.branchWiseAdvBalData, "VENDOR")
			setVendorTdsDetailsHidden(data.tdsItemDetails);
			alwaysScrollTop();
            logDebug("End showVendorEntityDetails");
        },
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
    			swal("Error on fetching vendor detail!", "Please retry, if problem persists contact support team", "error");
    		}
		},
		complete: function(data) {
			$.unblockUI();
            tdsSelectChangeOnItemsVend();
		}
	});
}

$(document).ready(function() {
	$(".addnewGSTINVend").click(function(){
		var isRegister = $('#vendorRegisteredOrUnReg').val();
		if(isRegister == 1) {
			$("#staticMutipleGSTINVendor").attr('data-toggle', 'modal');
			$("#staticMutipleGSTINVendor").modal('show');
			clearVendorGstinPopup();
		} else {
			swal("For unregistered GST vendors, you can't add a GSTIN.");
		}
		
	});
});

var clearVendorGstinPopup = function(){
	$("#vendorPopupDetailsTable input[name='gstinPart1']").val('');
	$("#vendorPopupDetailsTable input[name='gstinPart2']").val('');
	$("#vendorPopupDetailsTable textarea[name='addressPopup']").val('');
	$("#vendorPopupDetailsTable select[name='countryDropDown']").find('option:first').prop("selected","selected");
	$("#vendorPopupDetailsTable select[name='statePopup']").find('option:first').prop("selected","selected");
	$("#vendorPopupDetailsTable input[name='locationPopup']").val('');
	$("#vendorPopupDetailsTable select[name='phnNoCountryCodePopup']").find('option:first').prop("selected","selected");
	$("#vendorPopupDetailsTable input[name='phone1Popup']").val('');
	$("#vendorPopupDetailsTable input[name='phone2Popup']").val('');
	$("#vendorPopupDetailsTable input[name='phone3Popup']").val('');
}

$(document).ready(function() {
	$(".addPopupGstinVendorBtn").click(function(){
		var parentTr = $(this).parent().parent().children().children().closest('table').attr('id');
		//.closest('table').attr('id');
		var stateGstinCode = $("#" + parentTr + " input[name='gstinPart1']").val();
		var gstinsecondPart = $("#" + parentTr + " input[name='gstinPart2']").val();
		var gstLocation = $("#" + parentTr + " input[name='locationPopup']").val();
		var gstinCode = stateGstinCode + gstinsecondPart;


		if(stateGstinCode !== "" && gstinsecondPart === ""){
			swal("Invalid GSTIN! Second part of GSTIN cannot be empty");
            return false;
        }
		if(gstinCode < 15 || gstinCode.length > 15){
			swal("Invalid GSTIN! Please provide correct GSTIN");
			return false;
	    }
		if(gstLocation == "" || gstLocation == undefined) {
			swal("Please provide location! Location cannot be empty");
			return false;
		}

/*
		if((gstinCode.length > 1) && (gstinCode.length < 15 || gstinCode.length > 15)){
	        alert("Invalid GSTIN! Please provide correct GSTIN");
	        return false;
	    }
*/
	    var vendorAddress=$("#vendorAddressPopup").val();
	    if(vendorAddress == ""){
	    	swal("Invalid!","Please provide valid address","error");
	    	return false;
	    }
		var vendorCountry=$("#vendorcountryPopup option:selected").val();
		var vendorState=$("#vendorstatePopup option:selected").text();
		var vendorStateCode=$("#vendorstatePopup option:selected").val();
		if(vendorState == ""){
	    	swal("Invalid!","Please provide valid state","error");
	    	return false;
	    }

	    if(stateGstinCode.length > 1 && vendorStateCode != stateGstinCode){
	    	swal("Invalid!","GSTIN and State does not match, please provide valid state/GSTIN.","error");
	    	return false;
	    }

		var vendorLocation=$("#vendorLocationPopup").val();

		if(vendorLocation == ""){
	    	swal("Invalid!","Please provide valid location.","error");
	    	return false;
	    }
		var vendorPhnCtryCode=$("#vendorPhnNocountryCodePopup option:selected").val();
		var vendorPhone1=$("#vendorPhone1Popup").val();
		var vendorPhone2= $("#vendorPhone2Popup").val();
		var vendorPhone3= $("#vendorPhone3Popup").val();

		var vendorDetailIdListHid = $("#vendorDetailIdListHid").val();
		var gstinCodeHid = $("#vendorGstinCodeHid").val();
		var vendorlocationHid=$("#vendorlocationHid").val();

		if(gstinCode.length > 1 && gstinCodeHid.indexOf(gstinCode) !== -1){
			if(vendorlocationHid.indexOf(vendorLocation)!= -1){
				swal("Invalid!","GSTIN already added! Please add different GSTIN","error");
				return false;
			}
		}

		var vendorAddressHid = $("#vendorAddressHid").val();
		var vendorcountryCodeHid = $("#vendorcountryCodeHid").val();
		var vendorstateHid = $("#vendorstateHid").val();
		var vendorStateCodeHid = $("#vendorStateCodeHid").val();
		var vendorlocationHid = $("#vendorlocationHid").val();
		var vendorPhnNocountryCodeHid = $("#vendorPhnNocountryCodeHid").val();
		var vendorphone1Hid = $("#vendorphone1Hid").val();
		var vendorphone2Hid = $("#vendorphone2Hid").val();
		var vendorphone3Hid = $("#vendorphone3Hid").val();

		//Manali
		var updatedVendorIdHid = $("#updatedVendorIdHid").val();
		var detailIdListHid = $("#vendorDetailIdListHid").val().split('|');
		var idIndex = detailIdListHid.indexOf(updatedVendorIdHid);

		if(updatedVendorIdHid==''|| idIndex == 0 || idIndex == -1){
			var vendorId = $("#vendorDetailIdHid").val();
			if(vendorId == ""){
				vendorId = 0;  //dummy id
			}else if(vendorId < 1){
				vendorId = vendorId - 1; //dummy id
			}else{
				vendorId = parseInt(vendorId) + 1;
			}
			vendorDetailIdListHid = vendorDetailIdListHid + "|" + vendorId;
			gstinCodeHid = gstinCodeHid + "|" + gstinCode;
			vendorAddressHid = vendorAddressHid+ "|" + vendorAddress;
			vendorcountryCodeHid = vendorcountryCodeHid+ "|" + vendorCountry;
			vendorstateHid = vendorstateHid+ "|" + vendorState;
			vendorStateCodeHid = vendorStateCodeHid+ "|" + vendorStateCode;
			vendorlocationHid = vendorlocationHid+ "|" + vendorLocation;
			vendorPhnNocountryCodeHid = vendorPhnNocountryCodeHid+ "|" + vendorPhnCtryCode;
			vendorphone1Hid = vendorphone1Hid+ "|" + vendorPhone1;
			vendorphone2Hid = vendorphone2Hid+ "|" + vendorPhone2;
			vendorphone3Hid = vendorphone3Hid+ "|" + vendorPhone3;
			$("#vendorDetailIdHid").val(vendorId);
			$("#vendorDetailIdListHid").val(vendorDetailIdListHid);
			$("#vendorGstinCodeHid").val(gstinCodeHid);
			$("#vendorAddressHid").val(vendorAddressHid);
			$("#vendorcountryCodeHid").val(vendorcountryCodeHid);
			$("#vendorstateHid").val(vendorstateHid);
			$("#vendorStateCodeHid").val(vendorStateCodeHid);
			$("#vendorlocationHid").val(vendorlocationHid);
			$("#vendorPhnNocountryCodeHid").val(vendorPhnNocountryCodeHid);
			$("#vendorphone1Hid").val(vendorphone1Hid);
			$("#vendorphone2Hid").val(vendorphone2Hid);
			$("#vendorphone3Hid").val(vendorphone3Hid);

			$("#vendorGstinTbl tbody").append('<tr><td style="cursor:pointer" onClick="popupVendorGstinDetail(\''+vendorId+'\');">'+vendorState+'</td><td>'+gstinCode+'</td><td><input id="shipVendCity" type="hidden" value="'+vendorLocation+'"/><input id="vendorDetID" type="hidden" value=""/><input type="checkbox" id="gstinEnableCheck" checked="checked"/></td></tr>');
		}else{
			var detailIdHid = $("#vendorDetailIdHid").val().split('|');
			var gstinCodeHid = $("#vendorGstinCodeHid").val().split('|');
			var addressHid = $("#vendorAddressHid").val().split('|');
			var countryCodeHid = $("#vendorcountryCodeHid").val().split('|');
			var stateHid = $("#vendorstateHid").val().split('|');
			var stateCodeHid = $("#vendorStateCodeHid").val().split('|');
			var locationHid = $("#vendorlocationHid").val().split('|');
			var phnNocountryCodeHid = $("#vendorPhnNocountryCodeHid").val().split('|');
			var phone1Hid = $("#vendorphone1Hid").val().split('|');
			var phone2Hid = $("#vendorphone2Hid").val().split('|');
			var phone3Hid = $("#vendorphone3Hid").val().split('|');

			//detailIdHid[idIndex]=vendorId; No need to set this as we are not changing internal vendId
			gstinCodeHid[idIndex]=gstinCode;
			addressHid[idIndex]=vendorAddress;
			countryCodeHid[idIndex]=vendorCountry;
			stateHid[idIndex]=vendorState;
			stateCodeHid[idIndex]=vendorStateCode;
			locationHid[idIndex]=vendorLocation;
			phnNocountryCodeHid[idIndex]=vendorPhnCtryCode;
			phone1Hid[idIndex]=vendorPhone1;
			phone2Hid[idIndex]=vendorPhone2;
			phone3Hid[idIndex]=vendorPhone3;
			//var vendorlocationHid=locationHid.toString();

			//vendorDetailIdListHid = vendorDetailIdListHid + "|" + vendorId;
			vendorGstinCodeHid = gstinCodeHid.join('|');
			vendorAddressHid = addressHid.join('|');
			vendorcountryCodeHid = countryCodeHid.join('|');
			vendorstateHid = stateHid.join('|');
			vendorStateCodeHid = stateCodeHid.join('|');
			vendorlocationHid=locationHid.join('|');
			vendorPhnNocountryCodeHid = phnNocountryCodeHid.join('|');
			vendorphone1Hid = phone1Hid.join('|');
			vendorphone2Hid = phone2Hid.join('|');
			vendorphone3Hid = phone3Hid.join('|');

			//$("#vendorDetailIdHid").val(vendorId);
			$("#vendorDetailIdListHid").val(vendorDetailIdListHid);
			$("#vendorGstinCodeHid").val(vendorGstinCodeHid);
			$("#vendorAddressHid").val(vendorAddressHid);
			$("#vendorcountryCodeHid").val(vendorcountryCodeHid);
			$("#vendorstateHid").val(vendorstateHid);
			$("#vendorStateCodeHid").val(vendorStateCodeHid);
			$("#vendorlocationHid").val(vendorlocationHid);
			$("#vendorPhnNocountryCodeHid").val(vendorPhnNocountryCodeHid);
			$("#vendorphone1Hid").val(vendorphone1Hid);
			$("#vendorphone2Hid").val(vendorphone2Hid);
			$("#vendorphone3Hid").val(vendorphone3Hid);
		}
		$("#updatedVendorIdHid").val("");
		clearVendorGstinPopup();
		$("#staticMutipleGSTINVendor").hide();
		$(".modal .close").click();
	});
});

var popupVendorGstinDetail = function(vendorId){
	clearVendorGstinPopup();
	$("#updatedVendorIdHid").val(vendorId);
	var detailIdListHid = $("#vendorDetailIdListHid").val().split('|');
	var detailIdHid = $("#vendorDetailIdHid").val().split('|');
	var gstinCodeHid = $("#vendorGstinCodeHid").val().split('|');
	var addressHid = $("#vendorAddressHid").val().split('|');
	var countryCodeHid = $("#vendorcountryCodeHid").val().split('|');
	var stateHid = $("#vendorstateHid").val().split('|');
	var locationHid = $("#vendorlocationHid").val().split('|');
	var phnNocountryCodeHid = $("#vendorPhnNocountryCodeHid").val().split('|');
	var phone1Hid = $("#vendorphone1Hid").val().split('|');
	var phone2Hid = $("#vendorphone2Hid").val().split('|');
	var phone3Hid = $("#vendorphone3Hid").val().split('|');

	var idIndex = detailIdListHid.indexOf(vendorId);
	$("#staticMutipleGSTINVendor").attr('data-toggle', 'modal');
	$("#staticMutipleGSTINVendor").modal('show');
	var gstinCode = gstinCodeHid[idIndex];
	var gstinCode1 = gstinCode.substring(0,2);
	var gstinCode2 = gstinCode.substring(2,15);
	$("#vendorPopupDetailsTable input[name='gstinPart1']").val(gstinCode1);
	$("#vendorPopupDetailsTable input[name='gstinPart2']").val(gstinCode2);
	$("#vendorPopupDetailsTable textarea[name='addressPopup']").val(addressHid[idIndex]);
	$("#vendorPopupDetailsTable select[name='countryPopup']").find("option[value='"+countryCodeHid[idIndex]+"']").prop("selected", "selected");
	$("#vendorPopupDetailsTable select[name='statePopup'] option").filter(function(){ return $(this).text() == stateHid[idIndex]; }).prop('selected', true);
	$("#vendorPopupDetailsTable input[name='locationPopup']").val(locationHid[idIndex]);
	$("#vendorPopupDetailsTable select[name='phnNocountryCodePopup'] option").filter(function(){return $(this).html()==phnNocountryCodeHid[idIndex];}).prop('selected',true);
	$("#vendorPopupDetailsTable input[name='phone1Popup']").val(phone1Hid[idIndex]);
	$("#vendorPopupDetailsTable input[name='phone2Popup']").val(phone2Hid[idIndex]);
	$("#vendorPopupDetailsTable input[name='phone3Popup']").val(phone3Hid[idIndex]);

}

var clearVendorPopupHidden = function(){
	$("#vendorDetailIdHid").val('');
	$("#vendorDetailIdListHid").val('');
	$("#vendorGstinCodeHid").val('');
	$("#vendorAddressHid").val('');
	$("#vendorcountryCodeHid").val('');
	$("#vendorstateHid").val('');
	$("#vendorStateCodeHid").val();
	$("#vendorlocationHid").val('');
	$("#vendorPhnNocountryCodeHid").val('');
	$("#vendorphone1Hid").val('');
	$("#vendorphone2Hid").val('');
	$("#vendorphone3Hid").val('');
}

var clearVendorForm = function(detailForm){
	$('.'+detailForm+' input[type="hidden"]').val("");
	$('.'+detailForm+' input[type="text"]').val("");
	$('.'+detailForm+' textarea').val("");
	$('.'+detailForm+' input[type="password"]').val("");
	$('.'+detailForm+' select option:first').prop("selected", "selected");
	$('.'+detailForm+' select[class="countryPhnCode"]').each(function () {
		$(this).find('option:first').prop("selected", "selected");
    });
	$('.'+detailForm+' select[class="countryDropDown"]').each(function () {
		$(this).find('option:first').prop("selected", "selected");
    });
	$(".logo-upload-button").attr("href",location.hash);
	$("a[id*='form-container-close']").attr("href",location.hash);
    $('.'+detailForm+' select[id="contractAgreementUploads"]').children().remove();
    $('.'+detailForm+' select[id="contractAgreementUploads"]').append('<option value="">Select to view files</option>');
    $("#vendorRegisteredOrUnReg").find('option:first').prop("selected", "selected");
    $("#vendBusinessIndividual").find('option:first').prop("selected", "selected");
    $("#vendorState").find('option:first').prop("selected", "selected");
    $("#vendorGstinTbl tbody").html("");
	$("#branchAdvBalTable tbody").html("");
	// $("#branchOpBalTable tbody").html("");
    $('#vendor-form-container #vendorBranchDropdownBtn').html('None Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
	$('#daysCreditLabel').hide();
}
/*********************************************************************************************************
*********************************************Start Customer setup ****************************************
**********************************************************************************************************/
function showCustomerEntityDetails(elem){
	clearCustomerPopupHidden();
	populatecustomergroupdropdown();
	loadCoaIncomeItems();
	$(".newCustomerGroupform-container").hide();
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	var detailForm="newCustomerform-container";
	clearCustomerForm(detailForm);
	$(".logo-upload-button").attr("href",location.hash);
	$("a[id*='form-container-close']").attr("href",location.hash);
	var jsonData = {};
	jsonData.entityPrimaryId= origEntityId;
	var url="/config/customerDetails";
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
            logDebug("Start showCustomerEntityDetails");
			for(var i=0;i<data.vendordetailsData.length;i++){
				/*$('input[name="customeritemcheck"]:checkbox:checked').map(function () {
			  		$('input[name="customeritemcheck"][value=\''+this.value+'\']').prop('checked', false);
				}).get();*/
				$('#vendor-form-container input[name="custDiscount"]').map(function () {
					$(this).val("0.0");
				}).get();
				$("#vendordropdown").text("None Selected");
		        /*var purOrd=data.vendordetailsData[i].priceListDoc;
		        if(purOrd!=null && purOrd!=""){
		        	$('#customer-form-container input[name="custPriceList"]').val(purOrd);
		        }*/
				fillSelectElementWithUploadedDocs(data.vendordetailsData[i].priceListDoc, 'customertr', 'custPriceListUploads');
		        $("#custfutPayment option[value='"+data.vendordetailsData[i].vendorFutPayAlwd+"']").prop("selected","selected");

				if(data.vendordetailsData[i].vendorGroup != ""){
					$("#customerGroup option[value='"+data.vendordetailsData[i].vendorGroup+"']").prop("selected","selected");
				}

		        if(data.vendordetailsData[i].vendorFutPayAlwd==0 || data.vendordetailsData[i].vendorFutPayAlwd==2){
		        	$("#custdaysCreditLabel").show();
		        	$("#custdaysOfCredit").show();
		        	$("#custdaysOfCredit").val(data.vendordetailsData[i].daysOfCredit);
					$(".para-tm3-bm0").show();
					$("#custCreditLimit").show();
					$("#custTranExceedCredLim").show();
					$("#custOpeningBalance").show();
					$("#custOpeningBalanceAdvPaid").show();
		        }else{
		        	$("#custdaysCreditLabel").hide();
		        	$("#custdaysOfCredit").hide();
		        	$("#custdaysOfCredit").val("");
					$(".para-tm3-bm0").hide();
					$("#custCreditLimit").hide();
					$("#custTranExceedCredLim").hide();
					// $("#custOpeningBalance").hide();
					// $("#custOpeningBalanceAdvPaid").hide();
					$("#custOpeningBalance").show();
					$("#custOpeningBalanceAdvPaid").show();
		        }
		        $("#customerDetailsTable input[id='gstinputCust']").val(data.vendordetailsData[i].custGstinPart1);
		        $("#customerDetailsTable input[id='gstinput2Cust']").val(data.vendordetailsData[i].custGstinPart2);
		        $("#custBusinessIndividual option[value='"+data.vendordetailsData[i].custBusinessIndividual+"']").prop("selected","selected");
		        $("#custRegisteredOrUnReg option[value='"+data.vendordetailsData[i].custRegisteredOrUnReg+"']").prop("selected","selected");
		        $("#custTranExceedCredLim option[value='"+data.vendordetailsData[i].custTranExceedCredLim+"']").prop("selected","selected");
		        $("#custCreditLimit").val(data.vendordetailsData[i].custCreditLimit);
		        $("#custOpeningBalance").val(data.vendordetailsData[i].openingBalance);
		        $("#custOpeningBalanceAdvPaid").val(data.vendordetailsData[i].openingBalanceAdvPaid);

		        if(data.vendordetailsData[i].exculdeAdvCreLimCheck == 1){
		        	$('input[id="exculdeAdvCreLimCheck"]').prop("checked",true);
		        }else{
		        	$('input[id="exculdeAdvCreLimCheck"]').prop("checked",false);
		        }

		        $('#customer-form-container select[id="placeOfSupplyForOrg"]').val(data.vendordetailsData[i].placeOfSupplyType);

				$('#customer-form-container input[id="customerEntityHiddenId"]').val(data.vendordetailsData[i].id);
				$('#customer-form-container input[id="custName"]').val(data.vendordetailsData[i].vendorName);
				$('#customer-form-container input[id="custCode"]').val(data.vendordetailsData[i].code);
				$('#customer-form-container input[id="custemail"]').val(data.vendordetailsData[i].vendorEmail);
				$('#customer-form-container textarea[id="custdiscount"]').val(data.vendordetailsData[i].discount);
				var custCtry=data.vendordetailsData[i].vendorAddress;
				$('#customer-form-container select[id="customercountry"] option').filter(function () {return $(this).val()==custCtry;}).prop("selected", "selected");
				$('#customer-form-container select[id="customerState"] option').filter(function () {return $(this).text()==data.vendordetailsData[i].vendorState;}).prop("selected", "selected");
				var custPhnCtryCode=data.vendordetailsData[i].vendPhnCtryCode;

				$("#customer-form-container select[id='custPhnNocountryCode'] option").filter(function () {return $(this).val()==custPhnCtryCode;}).prop("selected", "selected");
				$('#customer-form-container input[id="custphone1"]').val(data.vendordetailsData[i].vendorPhone.substring(0,3));
				$('#customer-form-container input[id="custphone2"]').val(data.vendordetailsData[i].vendorPhone.substring(3,6));
				$('#customer-form-container input[id="custphone3"]').val(data.vendordetailsData[i].vendorPhone.substring(6,10));
				$('#customer-form-container textarea[id="customerAddress"]').val(data.vendordetailsData[i].vendAddress);
				//var vendPhnCtryCode=data.vendordetailsData[i].vendPhnCtryCode;
				$('#customer-form-container input[id="custlocation"]').val(data.vendordetailsData[i].vendorLocation);
				$('#customer-form-container input[id="customerStatutoryName1"]').val(data.vendordetailsData[i].customerStatutoryName1);
				$('#customer-form-container input[id="customerStatutoryNumber1"]').val(data.vendordetailsData[i].customerStatutoryNumber1);
				$('#customer-form-container input[id="customerStatutoryName2"]').val(data.vendordetailsData[i].customerStatutoryName2);
				$('#customer-form-container input[id="customerStatutoryNumber2"]').val(data.vendordetailsData[i].customerStatutoryNumber2);
				$('#customer-form-container input[id="customerStatutoryName3"]').val(data.vendordetailsData[i].customerStatutoryName3);
				$('#customer-form-container input[id="customerStatutoryNumber3"]').val(data.vendordetailsData[i].customerStatutoryNumber3);
				$('#customer-form-container input[id="customerStatutoryName4"]').val(data.vendordetailsData[i].customerStatutoryName4);
				$('#customer-form-container input[id="customerStatutoryNumber4"]').val(data.vendordetailsData[i].customerStatutoryNumber4);

				var specificationId=data.vendordetailsData[i].Specifications.split(',');
				var discPerc="";
				if(data.vendordetailsData[i].custspecfdisperc!=null){
					discPerc=data.vendordetailsData[i].custspecfdisperc.split(',');
				}
				if(specificationId!=""){
					for(var j=0;j<specificationId.length;j++){
						if(discPerc!=""){
							$("#custDiscount"+specificationId[j]).val(discPerc[j]);
						}else{
							$("#custDiscount"+specificationId[j]).val("0.0");
						}
						if(specificationId[j]!=""){
							$('input[id="checkboxid"][value='+specificationId[j]+']').prop("checked",true);
							var text=(j+1)+" "+"Items Selected";
						}
						$("#customerdropdown").text(text);
						$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
					}
				}
				var vendorBranchesList=data.vendordetailsData[i].vendBranches.split(','); var isPresent=false;
				var openingBalance = data.vendordetailsData[i].branchOpeningBalance.split(',');
				var openingBalanceAP = data.vendordetailsData[i].branchOpeningBalanceAP.split(',');
				for(var j=0;j<vendorBranchesList.length;j++){
					if(!isEmpty(vendorBranchesList[j])){
						const formattedValueBill = isNaN(Number(openingBalance[j])) ? '0.00' : Number(openingBalance[j]).toFixed(2);
						const formattedValueAdv = isNaN(Number(openingBalanceAP[j])) ? '0.00' : Number(openingBalanceAP[j]).toFixed(2);
						$('#customer-form-container #customerBranchList').find("input[type='checkbox'][value='"+vendorBranchesList[j]+"']").prop("checked",true);
						// if(openingBalance[j] == "" || openingBalance[j] == undefined){
							
						// } else{
						// 	$('#customer-form-container #customerBranchList').find("li[id='"+vendorBranchesList[j]+"'] input[class='openingBalance']").val(parseFloat(openingBalance[j]).toFixed(2));
						// }
						// if(openingBalanceAP[j] == "" || openingBalanceAP[j] == undefined){
							
						// } else{
						// 	$('#customer-form-container #customerBranchList').find("li[id='"+vendorBranchesList[j]+"'] input[class='openingBalanceAP']").val(parseFloat(openingBalanceAP[j]).toFixed(2));
						// }
						$('#customer-form-container #customerBranchList').find("li[id='"+vendorBranchesList[j]+"'] input[class='openingBalance']").val(formattedValueBill);
						$('#customer-form-container #customerBranchList').find("li[id='"+vendorBranchesList[j]+"'] input[class='openingBalanceAP']").val(formattedValueAdv);
						isPresent=true;
					}
				}
				if(isPresent){
					$('#customer-form-container #customerBranchDropdownBtn').html(vendorBranchesList.length + ' Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
				}
				if(data.vendordetailsData[i].entityType == 2){
					if(data.vendordetailsData[i].shippingSameAsBilling == '1'){
			        	$('input[id="isShippingAddressSame"]').prop("checked",true);
			        }else{
			        	$('input[id="isShippingAddressSame"]').prop("checked",false);
			        }
					$('#customer-form-container textarea[id="shipcustomerAddress"]').val(data.vendordetailsData[i].shippingAddress);
					var shippingcustCtry=data.vendordetailsData[i].shippingCountry;
					$('#customer-form-container select[id="shipcustomercountry"] option').filter(function () {return $(this).val()==shippingcustCtry;}).prop("selected", "selected");
					$('#customer-form-container select[id="shipCustomerState"] option').filter(function () {return $(this).text()==data.vendordetailsData[i].shippingState;}).prop("selected", "selected");
					$('#customer-form-container input[id="shipcustlocation"]').val(data.vendordetailsData[i].shippingLocation);
					var shippingcustPhnCtryCode=data.vendordetailsData[i].shippingvendPhnCtryCode;
					$("#customer-form-container select[id='shipcustPhnNocountryCode'] option").filter(function () {return $(this).val()==shippingcustPhnCtryCode;}).prop("selected", "selected");
					if(typeof data.vendordetailsData[i].shippingPhone !='undefined' &&  data.vendordetailsData[i].shippingPhone != ""){
						if(data.vendordetailsData[i].shippingPhone.length > 9){
							$('#customer-form-container input[id="shipcustphone1"]').val(data.vendordetailsData[i].shippingPhone.substring(0,3));
							$('#customer-form-container input[id="shipcustphone2"]').val(data.vendordetailsData[i].shippingPhone.substring(3,6));
							$('#customer-form-container input[id="shipcustphone3"]').val(data.vendordetailsData[i].shippingPhone.substring(6,10));
						}
					}
				}

				$('#customerBranch').multiselect('rebuild');
				$("."+detailForm+"").slideDown('slow');
				$('.notify-success').hide();
			}

			var customerGstinTblTrData="";
			for(var i=0; i<data.custGstinState.length; i++){
				var gstinCode = data.custGstinState[i].gstinCode;
				var shipcustState = data.custGstinState[i].gstState;
				var customerDetID = data.custGstinState[i].customerDetID;
				var customerStatus = data.custGstinState[i].customerStatus;
				var shippingLocation = data.custGstinState[i].shipingLocation
				if(customerStatus === 1){
					customerGstinTblTrData +=('<tr><td style="cursor:pointer" onClick="popupCustomerGstinDetail(\''+customerDetID+'\');">'+shipcustState+'</td><td>'+gstinCode+'<input id="shipCustCity" type="hidden" value="'+shippingLocation+'"/><input id="customerDetID" type="hidden" value="'+customerDetID+'"/></td><td><input type="checkbox" id="gstinEnableCheck" checked="checked"/></td></tr>');
				}else{
					customerGstinTblTrData+=('<tr><td style="cursor:pointer" onClick="popupCustomerGstinDetail(\''+customerDetID+'\');">'+shipcustState+'</td><td>'+gstinCode+'<input id="shipCustCity" type="hidden" value="'+shippingLocation+'"/><input id="customerDetID" type="hidden" value="'+customerDetID+'"/></td><td><input type="checkbox" id="gstinEnableCheck"/></td></tr>');
				}
			}
			$("#customerGstinTbl tbody").append(customerGstinTblTrData);
			if(data){
				$("#customerDetailIdHid").val(data.customerDetailIdHid);
				$("#customerDetailIdListHid").val(data.customerDetailIdListHid);
				$("#gstinCodeHid").val(data.gstinCodeHid);
				$("#customerAddressHid").val(data.customerAddressHid);
				$("#customercountryCodeHid").val(data.customercountryCodeHid);
				$("#custstateHid").val(data.custstateHid);
				$("#custstatecodeHid").val(data.custstatecodeHid);
				$("#custlocationHid").val(data.custlocationHid);
				$("#custPhnNocountryCodeHid").val(data.custPhnNocountryCodeHid);
				$("#custphone1Hid").val(data.custphone1Hid);
				$("#custphone2Hid").val(data.custphone2Hid);
				$("#custphone3Hid").val(data.custphone3Hid);
				$("#isShippingAddressSameHid").val(data.isShippingAddressSameHid);
				$("#shipcustomerAddressHid").val(data.shipcustomerAddressHid);
				$("#shipcustomerCountryCodeHid").val(data.shipcustomerCountryCodeHid);
				$("#shipstateHid").val(data.shipstateHid);
				$("#shipcustStateCodeHid").val(data.shipStateCodeHid);
				$("#shiptlocationHid").val(data.shiptlocationHid);
				$("#shipcustPhnNoCountryCodeHid").val(data.shipcustPhnNoCountryCodeHid);
				$("#shipcustPhone1Hid").val(data.shipcustPhone1Hid);
				$("#shipcustPhone2Hid").val(data.shipcustPhone2Hid);
				$("#shipcustPhone3Hid").val(data.shipcustPhone3Hid);
			}
			// Set Billwise Opening Balance
			// BRANCH_WISE_ADVANCE_BAL = data.branchWiseAdvBalData;
			setBillWiseOpBalDetails(data.billwiseOpBalanceData,"CUSTOMER");
			// setBranchWiseAdvBalDetails(data.branchWiseAdvBalData, "CUSTOMER")
            logDebug("End showCustomerEntityDetails");
			alwaysScrollTop();
        },
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout();
			}else if(xhr.status == 500){
    			swal("Error on fetching customer detail!", "Please retry, if problem persists contact support team", "error");
    		}
		},
		complete: function(data) {
			$.unblockUI();
           // loadCoaIncomeItems();
		}
	});
}

var enableShiipingAddress = function(){
	var isShippingAddressSame=$("#isShippingAddressSame").is(':checked');
	if(isShippingAddressSame == true){
		$("#shipcustPhnNocountryCode").attr("disabled", "disabled");
		$("#shipcustphone1").attr("disabled", "disabled");
		$("#shipcustphone2").attr("disabled", "disabled");
		$("#shipcustphone3").attr("disabled", "disabled");
		$("#shipcustomercountry").attr("disabled", "disabled");
		$("#shipcustomerAddress").attr("disabled", "disabled");
		$("#shipcustlocation").attr("disabled", "disabled");
	}else{
		$("#shipcustPhnNocountryCode").removeAttr("disabled");
		$("#shipcustphone1").removeAttr("disabled");
		$("#shipcustphone2").removeAttr("disabled");
		$("#shipcustphone3").removeAttr("disabled");
		$("#shipcustomercountry").removeAttr("disabled");
		$("#shipcustomerAddress").removeAttr("disabled");
		$("#shipcustlocation").removeAttr("disabled");
	}
}

function addCustomer(elem){
	var custId = $("#customerEntityHiddenId").val();
	var customerBranches = "";
    var branchArray = $('#customerBranchList').find('#custBranchCheck:checked').map(function () {
        return this.value;
    }).get();
    var customerBranches = branchArray.toString();
    if(customerBranches == null || customerBranches == ""){
        swal("Invalid Branch!","Please select at least a branch", "error");
        return false;
    }
    var branchOpeningBalance = "", branchOpeningBalanceAP = "";
    for (var i in branchArray) {
        branchOpeningBalance += $("#customerBranchList li[id='" + branchArray[i] + "'] input[class='openingBalance']").val() +",";
        branchOpeningBalanceAP += $("#customerBranchList li[id='" + branchArray[i] + "'] input[class='openingBalanceAP']").val() +",";
    }
    if(branchOpeningBalance.length > 0){
        branchOpeningBalance = branchOpeningBalance.substring(0, branchOpeningBalance.length);
    }
    if(branchOpeningBalanceAP.length > 0){
        branchOpeningBalanceAP = branchOpeningBalanceAP.substring(0, branchOpeningBalanceAP.length);
    }
	if(customerBranches == null || customerBranches == ""){
		swal("Invalid Branch!","Please select at least a branch", "error");
		return false;
	}
	var customerName=$("#custName").val();
	//var customerCode=$("#custCode").val();
	var customerEmail=$("#custemail").val();
	if(customerName == null || customerName == "" || customerName.length < 3){
		swal("Invalid Customer Name!","Please provide valid customer name.", "error");
		return false;
	}
	/*if(customerCode == null || customerCode == ""){
		swal("Invalid Customer Code!","Please provide valid customer code.", "error");
		return false;
	}*/
	var gstinCode = ""
	var custRegisteredOrUnReg=$("#custRegisteredOrUnReg option:selected").val();
	if(custRegisteredOrUnReg == null || custRegisteredOrUnReg == ""){
		swal("Invalid GST register type!","Please select either Yes/No.", "error");
	    return false;
	}
	if(custRegisteredOrUnReg == "1"){
		var stateGstinCode = $("#customerDetailsTable input[id='gstinputCust']").val();
		var gstinsecondPart = $("#customerDetailsTable input[id='gstinput2Cust']").val();
		gstinCode= stateGstinCode + gstinsecondPart;
		if(stateGstinCode !== "" && gstinsecondPart === ""){
            swal("Invalid GSTIN!","Second part of GSTIN cannot be empty", "error");
            return false;
        }
		if(gstinCode < 15 || gstinCode.length > 15){
	        swal("Invalid GSTIN!","Please provide correct GSTIN", "error");
	        return false;
	    }
	}
	var custBusinessIndividual=$("#custBusinessIndividual option:selected").val();
	if(custBusinessIndividual == null || custBusinessIndividual == ""){
		swal("Invalid GST Customer type!","Please select a Customer type.", "error");
	    return false;
	}
	var custPhnCtryCode=$("#custPhnNocountryCode option:selected").val();
	var custPhone=$("#custphone1").val()+$("#custphone2").val()+$("#custphone3").val();
	var custCountry=$("#customercountry option:selected").val();
	var custAddress=$("#customerAddress").val();
	if(custAddress == null || custAddress ==""){
		swal("Invalid Billing address!","Please provide valid billing address.", "error");
	    return false;
	}
	var custState=$("#customerState option:selected").text();
	var custStateCode=$("#customerState option:selected").val();
	if(custStateCode == null || custStateCode ==""){
		swal("Invalid Billing State!","Please provide valid billing State.", "error");
	    return false;
	}
	var custLocation=$("#custlocation").val();
	if(custLocation == null || custLocation ==""){
		swal("Invalid Billing location!","Please provide valid billing location.", "error");
	    return false;
	}
	var isShippingAddressSame=$("#isShippingAddressSame").is(':checked');
	var shipcustPhnCtryCode=$("#shipcustPhnNocountryCode option:selected").val();
	//var shipcustPhnCtryCode=$("#shipcustPhnNocountryCode option:selected").text();
	var shipcustPhone=$("#shipcustphone1").val()+$("#shipcustphone2").val()+$("#shipcustphone3").val();
	var shipcustCountry=$("#shipcustomercountry option:selected").val();
	var shipcustAddress=$("#shipcustomerAddress").val();
	var shipcustLocation=$("#shipcustlocation").val();
	var shipcustState=$("#shipCustomerState option:selected").text();
	var shipcustStateCode = $("#shipCustomerState option:selected").val();
	if(isShippingAddressSame){
		shipcustState = custState;
		shipcustLocation = custLocation;
		shipcustAddress = custAddress;
		shipcustStateCode = $("#customerState option:selected").val();
		shipcustPhnCtryCode=$("#custPhnNocountryCode option:selected").val();
		shipcustPhone=$("#custphone1").val()+$("#custphone2").val()+$("#custphone3").val();
	}

	if(isShippingAddressSame == false){
		if(shipcustAddress == null || shipcustAddress ==""){
			swal("Invalid Shpping address!","Please provide valid Shpping address.", "error");
		    return false;
		}
		if((shipcustStateCode == null || shipcustStateCode =="")){
			swal("Invalid GSTIN State!","Please provide valid shipping State.", "error");
	        return false;
		}
		if(shipcustLocation == null || shipcustLocation ==""){
			swal("Invalid Shpping location!","Please provide valid Shpping location.", "error");
		    return false;
		}
	}

	var placeOfSupplyForOrg=$("#placeOfSupplyForOrg option:selected").val();

	//var custPriceList=$("input[name='custPriceList']").val();
	var custDiscount=$("#custdiscount").val();
	var custfutPayAlwd=$("#custfutPayment option:selected").val();
	var customerGroup=$("#customerGroup option:selected").val();
	//var custContractDoc=$("input[name='custPriceList']").val();
	 var  custContractDoc="";
	 $('select[id="custPriceListUploads"] option').each(function () {
		if( custContractDoc==""){
			 custContractDoc= this.value;
		}else{
			 custContractDoc+= ','+this.value;
		}
	});
	var daysOfCdt=$("#custdaysOfCredit").val();
	var custTranExceedCredLim=$("#custTranExceedCredLim option:selected").val();
	var custCreditLimit = $("#custCreditLimit").val();
	var exculdeAdvCreLimCheck = $('input[name="exculdeAdvCreLimCheck"]:checkbox').prop('checked') ? 1 : 0;
	var custOpeningBalanceAdvPaid = $("#custOpeningBalanceAdvPaid").val();
	var custOpeningBalance = $("#custOpeningBalance").val();
    var customerStatutoryName1=$("#customerStatutoryName1").val();
    var customerStatutoryNumber1=$("#customerStatutoryNumber1").val();
    var customerStatutoryName2=$("#customerStatutoryName2").val();
    var customerStatutoryNumber2=$("#customerStatutoryNumber2").val();
    var customerStatutoryName3=$("#customerStatutoryName3").val();
    var customerStatutoryNumber3=$("#customerStatutoryNumber3").val();
    var customerStatutoryName4=$("#customerStatutoryName4").val();
    var customerStatutoryNumber4=$("#customerStatutoryNumber4").val();

	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var gstinCheckedVal = $('#customerGstinTbl tbody tr input[id="gstinEnableCheck"]').map(function () {
	    return this.checked;
	}).get();
	console.log(gstinCheckedVal);
	console.log(gstinCheckedVal.toString());
	var gstinCheckedValues = "";
	console.log(gstinCheckedValues);
	var tableTrCount = $("#customerGstinTbl tbody tr").length;
	for (var i = 0; i < tableTrCount; i++) {
		gstinCheckedValues += "," + $("#customerGstinTbl tbody tr:eq("+i+") input[id='gstinEnableCheck']").is(':checked');
	}
	console.log(gstinCheckedValues);
	var custDiscountPerc="";
	 var customerItems="";
	 $('input[name="customeritemcheck"]:checkbox:checked').map(function () {
		 if(this.value!=""){
			 customerItems=customerItems+this.value+",";
		 }
	 }).get();
	 $('input[name="custDiscount"]').map(function () {
	      if($(this).val().trim()!=""){
	    	  custDiscountPerc=custDiscountPerc+this.value+",";
	      }
	 }).get();
	 var check_box_values = $('input[name="customeritemcheck"]:checkbox:checked').map(function () {
	        return this.value;
	 }).get();

	 if(custDiscountPerc==""){
	      for(var i=0;i<check_box_values.length;i++){
	    	  custDiscountPerc+="0.0"+",";
	      }
	 $('input[name="VendRcmApplicableDate"]').map(function () {
	    	 var vendRcmApplicableDateItems=vendRcmApplicableDateItems+this.value+"";
	 }).get();
	 $('input[name="rcmRateVendItem"]').map(function () {
	    	 var vendRcmTaxRateForItems=vendRcmTaxRateForItems+this.value+",";
	 }).get();
	 $('input[name="cesRateVendItem"]').map(function () {
	    	 var vendCessTaxRateForItems=vendCessTaxRateForItems+this.value+",";
	 }).get();
	 }
	// $("a[id*='form-container-close']").attr("href",location.hash);
	$("#newCustomerform-container-close").attr("href", location.hash);
	var jsonData = {};
	if(custId!=""){
	   jsonData.customerId= custId;
	}
	if(custfutPayAlwd==0 || custfutPayAlwd==2){
	   jsonData.daysOfCredit= daysOfCdt;
	}

	 // Billwise Opening Balance
    var billwiseOpeningBalance = getBillWiseOpBalDetails(elem);
	// var branchWiseAdvBalance = getBranchWiseAdvBalDetails(elem);

	jsonData.useremail =$("#hiddenuseremail").text();
	jsonData.custName=customerName;
	//jsonData.custCode=customerCode;
	jsonData.custEmail=customerEmail;
	jsonData.gstinCode=gstinCode;
	jsonData.custBusinessIndividual =custBusinessIndividual;
	jsonData.custRegisteredOrUnReg = custRegisteredOrUnReg;
	jsonData.customerPhnCtryCode=custPhnCtryCode;
	jsonData.customerPhone=custPhone;
	jsonData.customerfutPayAlwd=custfutPayAlwd;
	jsonData.customerSelGroup=customerGroup;
	jsonData.customerContractDoc=custContractDoc;
	jsonData.customerCountry=custCountry;
	jsonData.customerAddress=custAddress;
	jsonData.customerLocation=custLocation;
	jsonData.customerState=custState;
	jsonData.customerStateCode=custStateCode;
	jsonData.customerDiscount=custDiscount;
	jsonData.customerItems=customerItems;
	jsonData.custItemsDiscount=custDiscountPerc;
	jsonData.custBranches=customerBranches;
    jsonData.branchOpeningBalance = branchOpeningBalance;
    jsonData.branchOpeningBalanceAP = branchOpeningBalanceAP;
	jsonData.custTranExceedCredLim=custTranExceedCredLim;
	jsonData.custCreditLimit=custCreditLimit;
	jsonData.exculdeAdvCreLimCheck=exculdeAdvCreLimCheck;
	jsonData.openingBalance = custOpeningBalance;
	jsonData.openingBalanceAdvPaid = custOpeningBalanceAdvPaid;
	jsonData.custStatutoryName1=customerStatutoryName1;
    jsonData.custStatutoryNumber1=customerStatutoryNumber1;
    jsonData.custStatutoryName2=customerStatutoryName2;
    jsonData.custStatutoryNumber2=customerStatutoryNumber2;
    jsonData.custStatutoryName3=customerStatutoryName3;
    jsonData.custStatutoryNumber3=customerStatutoryNumber3;
    jsonData.custStatutoryName4=customerStatutoryName4;
    jsonData.custStatutoryNumber4=customerStatutoryNumber4;
    jsonData.isShippingAddressSame = isShippingAddressSame;
    jsonData.shipcustPhnCtryCode = shipcustPhnCtryCode;
	jsonData.shipcustPhone = shipcustPhone;
	jsonData.shipcustCountry = shipcustCountry;
	jsonData.shipcustAddress = shipcustAddress;
	jsonData.shipcustLocation = shipcustLocation;
	jsonData.shipcustState=shipcustState;
	jsonData.shipcustStateCode=shipcustStateCode;
	jsonData.billwiseOpeningBalance = billwiseOpeningBalance;
	// jsonData.branchWiseAdvBalance = branchWiseAdvBalance
	jsonData.placeOfSupplyForOrg = placeOfSupplyForOrg;
	//Start multiple GSTIN code
	jsonData.customerDetailIdListHid = $("#customerDetailIdListHid").val();
	jsonData.gstinCodeHid = $("#gstinCodeHid").val();
	jsonData.customerAddressHid = $("#customerAddressHid").val();
	jsonData.customercountryCodeHid = $("#customercountryCodeHid").val();
	jsonData.custstateHid = $("#custstateHid").val();
	jsonData.custstatecodeHid = $("#custstatecodeHid").val();
	jsonData.custlocationHid = $("#custlocationHid").val();
	jsonData.custPhnNocountryCodeHid = $("#custPhnNocountryCodeHid").val();
	//jsonData.custPhnNocountryTextHid = $("#custPhnNocountryTextHid").val();
	jsonData.custphone1Hid = $("#custphone1Hid").val();
	jsonData.custphone2Hid = $("#custphone2Hid").val();
	jsonData.custphone3Hid = $("#custphone3Hid").val();
	jsonData.isShippingAddressSameHid = $("#isShippingAddressSameHid").val();
	jsonData.shipcustomerAddressHid = $("#shipcustomerAddressHid").val();
	jsonData.shipcustomerCountryCodeHid = $("#shipcustomerCountryCodeHid").val();
	jsonData.shipstateHid = $("#shipstateHid").val();
	jsonData.shipcustStateCodeHid = $("#shipcustStateCodeHid").val();
	jsonData.shiptlocationHid = $("#shiptlocationHid").val();
	jsonData.shipcustPhnNoCountryCodeHid = $("#shipcustPhnNoCountryCodeHid").val();
	//jsonData.shipcustPhnNoCountryTextHid = $("#shipcustPhnNoCountryTextHid").val();
	jsonData.shipcustPhone1Hid = $("#shipcustPhone1Hid").val();
	jsonData.shipcustPhone2Hid = $("#shipcustPhone2Hid").val();
	jsonData.shipcustPhone3Hid = $("#shipcustPhone3Hid").val();
	jsonData.gstinCheckedValues = gstinCheckedValues;
	//End multiple GSTIN code
	var url = "/customer/addCustomer";
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
			if(typeof data.message !=='undefined' && data.message != ""){
				swal("Error!", data.message, "error");
				return false;
			}else if(data.info != "vendorAdded"){
				swal("Error on save/update Customer!", data.info, "error");
			} else {
				if(data.role.includes("MASTER ADMIN") || data.canCreateCustomer == true || data.canActivateCustomer == true || data.canCreateVendor == true || data.canActivateVendor == true){
					var entityType=data.entityType;
					if(entityType=="vendorCustomer"){
						$("input[type='text']").val();
						$("textarea").val();
						$('select').find('option:first').prop("selected","selected");
						$(".multiBranch").each(function () {
							$(this).removeAttr('selected');
						});
						$('.multiBranch').multiselect('rebuild');
						var vendId=data.id;
						var vendName=data.name;
						var address=(data.address)!=null?data.address:"";
						var location=(data.location)!=null?data.location:"";
						var type=data.type;
						var email=(data.email)!=null?data.email:"";
						var phone=(data.phone)!=null?data.phone:"";
						if(type==2){
							$("#newCustomerform-container-close").trigger('click');
							if(data.role.includes("MASTER ADMIN") || data.canCreateCustomer == true || data.canActivateCustomer == true){
								$("#searchCustomer option[value="+data.id+"]").remove();
								$("#searchCustomer").append('<option value="'+data.id+'">'+data.name+'</option>');
								var existingCustomer=$("#customerTable").find('tr[name="customerEntity'+data.id+'"]').attr('name');
								var customerTableTemp = "";
								if(typeof(existingCustomer)!='undefined'){
									$("#customerTable").find('tr[name="customerEntity'+data.id+'"]').html("");
									customerTableTemp += ('<td>'+data.name+'</td><td>'+location+'</td><td>'+email+'</td><td style="text-align:center;"><div class="grantAccess"></div></td><td>'+phone+'</td><td>'+address+'</td><td><button class="btn btn-submit"  onClick="showCustomerEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>');
									if(data.presentStatus == 1){
										customerTableTemp += ('<td><button class="btn btn-submit"  onClick="changeStatusCustomerVendor(this, 0)" id="entity-details'+data.id+'"><i class="fas fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td></tr>');
									}else if(data.presentStatus == 0){
										customerTableTemp += ('<td><button class="btn btn-submit"  onClick="changeStatusCustomerVendor(this, 1)" id="entity-details'+data.id+'"><i class="fa fa-check-square-o fa-lg pr-5"></i>Activate</button></td>');
									}else{
										customerTableTemp += ('<td></td>');
									}
									$("#customerTable").find('tr[name="customerEntity'+data.id+'"]').append(customerTableTemp);
								}else if(typeof(existingCustomer)=='undefined'){
									customerTableTemp += ('<tr name="customerEntity'+data.id+'"><td>'+data.name+'</td><td>'+location+'</td><td>'+email+'</td><td style="text-align:center;"><div class="grantAccess""></div></td><td>'+phone+'</td><td>'+address+'</td><td><button class="btn btn-submit"  onClick="showCustomerEntityDetails(this)" id="show-entity-details'+data.id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td>');
									if(data.presentStatus == 1){
										customerTableTemp += ('<td><button class="btn btn-submit"  onClick="changeStatusCustomerVendor(this, 0)" id="entity-details'+data.id+'"><i class="fas fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td></tr>');
									}else if(data.presentStatus == 0){
										customerTableTemp += ('<td><button class="btn btn-submit" onClick="changeStatusCustomerVendor(this, 1)" id="entity-details'+data.id+'"><i class="fa fa-check-square-o fa-lg pr-5"></i>Activate</button></td>');
									}else{
										customerTableTemp += ('<td></td>');
									}
									customerTableTemp += '</tr>'
									$("#customerTable").append(customerTableTemp);
								}
								if(data.grantAccess=="0"){
									$("tr[name='customerEntity"+data.id+"'] div[class='grantAccess']").html('<a href="#customerSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Grant Access"><i class="fa fa-check pr-5"></i></a>');
								}else if(data.grantAccess=="1"){
									$("tr[name='customerEntity"+data.id+"'] div[class='grantAccess']").html('</i><a href="#customerSetup" name="grantaccess" id="grantaccess" onclick="grantRemoveVendorCustomerAccess(this);" title="Remove Access"><i class="fa fa-times pr-5"></a>');
								}
	
								$('input[name="customeritemcheck"]:checkbox:checked').map(function () {
									$('input[name="customeritemcheck"][value=\''+this.value+'\']').prop('checked', false);
								}).get();
								$('input[name="custDiscount"]').map(function () {
									if($(this).val().trim()!=""){
										$(this).val("0.0");
									}
								}).get();
								$("#customerdropdown").text("None Selected");
								$("#customerdropdown").append("&nbsp;&nbsp;<b class='caret'></b>");
								$("#notificationMessage").html("Customer has been added/Updated successfully.");
							}
						}
					}
					$.unblockUI();
					$('.notify-success').show();
					alwaysScrollTop();
				}
				if(data.role.includes("CONTROLLER")){
					getDashboardFinancials();
				}
				clearBranchBillDetails();
				clearBranchAdvDetails();
			}
	   	},
	   	error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
    			swal("Error on save/update customer!", "Please retry, if problem persists contact support team", "error");
    		}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

$(document).ready(function() {
    // Assuming "addCustBtn" is a button, adjust the selector accordingly
    $("#addCustBtn").click(function(elem) {
        // Call the addCustBtn function here
        addCustomer(elem);
    });
});

function populatecustomergroupdropdown(){
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/customer/listCustomerGroup";
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
			$("#customerGroup").children().remove();
			$("#customerGroup").append('<option value="">--Please Select--</option>');
			for(var i=0;i<data.customerGroupList.length;i++){
				$("#customerGroup").append('<option value="'+data.customerGroupList[i].id+'">'+data.customerGroupList[i].vendGroupName+'</option>');
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	})
}

function populatecustomergrouplist(){
	var jsonData={};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/customer/listCustomerGroup";
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
			$("#customerGroupDetailsListTable tbody").html("");
			for(var i=0;i<data.customerGroupList.length;i++){
				$("#customerGroupDetailsListTable").append('<tr id="customerGroupEntity'+data.customerGroupList[i].id+'" name="customerGroupEntity'+data.customerGroupList[i].id+'"><td>'+data.customerGroupList[i].vendGroupName+'</td><td><button class="btn btn-submit" onClick="showCustomerGroupEntityDetails(this)" id="show-entity-details'+data.customerGroupList[i].id+'"><i class="fa fa-edit fa-lg pr-5"></i>Edit</button></td></tr>');
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

$(document).ready(function() {
	$("#updateShipAddressBtn").click(function() {
		$("#messageModal").html("");
		var shipcustPhnCtryCodeVal=$("#shippingCustPhnNocountryCode option:selected").val();
		var shipcustPhnCtryCode=$("#shippingCustPhnNocountryCode option:selected").text();
		var shipcustPhone=shipcustPhnCtryCodeVal+"-"+$("#shippingCustphone1").val()+$("#shippingCustphone2").val()+$("#shippingCustphone3").val();
		var shipcustCountry=$("#shippingCustCountry option:selected").val();
		var shipcustAddress=$("#shippingCustAddress").val();
		var shipcustLocation=$("#shippingCustlocation").val();
		var entitycustid=$("#custEntityID").val();
		if(shipcustAddress == ""){
			$("#messageModal").append("Shipping address cannot be empty.");
			return false;
		}

		if(entitycustid == ""){
			$("#messageModal").append("You cannot store shipping detail for visiting customer. Input shipping address and proceed to generate invoice.");
			return false;
		}
	 	var jsonData={};
	 	jsonData.shipcustPhnCtryCode = shipcustPhnCtryCode;
		jsonData.shipcustPhone = shipcustPhone;
		jsonData.shipcustCountry = shipcustCountry;
		jsonData.shipcustAddress = shipcustAddress;
		jsonData.shipcustLocation = shipcustLocation;
		jsonData.entitycustid = entitycustid;
		var url = "/customer/updateshipping";
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
		   		$("#messageModal").append("Shipping address updated successfully.");
		   	},
		   	error: function (xhr, status, error) {
				if(xhr.status == 401){
					doLogout();
				}else if(xhr.status == 500){
	    			$("#messageModal").append("Error on updating shipping address!, Please retry, if problem persists contact support team");
	    		}
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	});
});

$(document).ready(function() {
	$(".vendcustloginButton").click(function(){
		var vendcustpassword=$("#vendcustpass").val();
		if(vendcustpassword==""){
			notifyLogin.show("Please provide password for your account.",true,true);
			return true;
		}
		var jsonData = {};
		jsonData.accountOrganization = GetURLParameter('accountOrganization');
		jsonData.accountEmail = GetURLParameter('accountEmail');
		jsonData.entityType = GetURLParameter('entityType');
		jsonData.accountPassword=vendcustpassword;
		var url="/account/loginVendorAccount";
		$.ajax({
			url: url,
			data:JSON.stringify(jsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data){
				if(data.wrongses == "false"){
					notifyLogin.show("Session expired, refresh the page and retry.",true,true);
					return false;
				}
				if(data.vendorCustomerLoginData[0].message=='failure'){
					notifyLogin.show("Vendor/customer email and password combination do not match.",true,true);
					return true;
				}else if(data.vendorCustomerLoginData[0].message=='inactive'){
					notifyLogin.show("Vendor/customer email is not activated. please contact your company master admin",true,true);
					return true;
				}else if(data.vendorCustomerLoginData[0].message=='nogrant'){
					notifyLogin.show("Vendor/customer email is not granted, please contact your company master admin.",true,true);
					return true;
				}else{
					window.location.href="/vendCustConfig?accountName="+data.vendorCustomerLoginData[0].accountName+"&accountEmail="+data.vendorCustomerLoginData[0].accountEmail+"&accountOrganization="+data.vendorCustomerLoginData[0].accountOrganization+"&entityType="+data.vendorCustomerLoginData[0].entityType+"&entityId="+data.vendorCustomerLoginData[0].id+"#vendCustAccount";
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doVendorLogout(); }
			}
		})
	});
});

function vendcustcancel(){
	$("#vendCustAccount").show();
	$("#logindiv").hide();
	$("#forgotlogindiv").hide();
	$("#vendCustForgotAccount").hide();
}

function grantRemoveVendorCustomerAccess(elem){
	//alert(">>>>>32"); //sunil
	var parentTrName=$(elem).parent().parent().parent().attr('name');
	var vendorCustomerEntityId="";
	if(parentTrName.indexOf('vendorEntity')!=-1){
		vendorCustomerEntityId=parentTrName.substring(12, parentTrName.length);
		//send ajax request for vendor access for the user in session organization
	}
	if(parentTrName.indexOf('customerEntity')!=-1){
		vendorCustomerEntityId=parentTrName.substring(14, parentTrName.length);
		//send ajax request for customer access for the user in session organization
	}
	var jsonData = {};
	var useremail=$("#hiddenuseremail").text();
	jsonData.usermail = useremail;
	jsonData.selectedCustVend=vendorCustomerEntityId;
	if ($(elem).find('i.fa').hasClass('fa-check')) {
		jsonData.elemText='Grant Access';
	} else if ($(elem).find('i.fa').hasClass('fa-times')) {
		jsonData.elemText='Remove Access';
	}
	var url="/vendor/grantRemoveVendorCustomerAccess";
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
			if(data.vendorCustActData[0].grantAccess=="1"){
				$(elem).find('i.fa').removeClass('fa-check').addClass('fa-times').attr('title', 'Remove Access');
			}
			if(data.vendorCustActData[0].grantAccess=="0"){
				$(elem).find('i.fa').removeClass('fa-times').addClass('fa-check').attr('title', 'Grant Access');
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){
				doLogout();
			}else if(xhr.status == 500){
	    		swal("Error on Granting/Revoking access to Vendor!", "Please retry, if problem persists contact support team", "error");
	    	}
		},
		complete: function(data) {
			$.unblockUI();
		}
	});
}

function populateVendorCustomerTransactions(accountEmail,accountOrganization,entityType){
	//alert(">>>>>33"); //sunil
	var jsonData = {};
	jsonData.vendcustaccountEmail = accountEmail;
	jsonData.vendcustaccountOrganization=accountOrganization;
	jsonData.vendcustentityType=entityType;
	var url="/vendorcustomer/transactionsList";
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
			 $(".currentOutstandings").text();
			 $(".currentOutstandings").text(data.totalOutstandingsData[0].currentOutstandings);
			 if(data.totalOutstandingsData[0].type==1){
				 $(".specialAdjustmentsOutstandings").text();
				 $(".specialAdjustmentsOutstandings").text("Special Adjustments Outstandings:" +data.totalOutstandingsData[0].outstandingVendorSpecialAdjustments);
			 }
			 for(var i=0;i<data.userTxnData.length;i++){
				 $("#vendCustTransactions table[id='transactionTable']").prepend('<tr id="transactionEntity'+data.userTxnData[i].id+'"><td><b>BRANCH:</b><br/><p style="color: blue;">'+data.userTxnData[i].branchName+'</p><br/>'+
				 '<b>PROJECT:</b><br/><p style="color: blue;">'+data.userTxnData[i].projectName+'</p></td><td><b>ITEM:</b><br/><p style="color: blue;">'+data.userTxnData[i].itemName+'</p></td><td><b>OUTSTANDINGS:</b><br/><div class="vendCustOutstandings" style="height: 130px;overflow: auto;"></div></p></td>'+
				 '<td><b>TRANSACTION DATE:</b><br/><p style="color: blue;">'+data.userTxnData[i].txnDate+'</p><div class="vendorInvDateLabelDiv"><b>'+data.userTxnData[i].invoiceDateLabel+'</b></div><div class="vendorInvDateDiv"><p style="color: blue;">'+data.userTxnData[i].invoiceDate+'</p></div></td><td>'+data.userTxnData[i].paymentMode+'</td><td><b>UNIT:</b><br/><p style="color: blue;">'+data.userTxnData[i].noOfUnit+'</p><br/>'+
				 '<b>PRICE/UNIT:</b><br/><p style="color: blue;">'+data.userTxnData[i].unitPrice+'</p><br/><b>GROSS:</b><br/><p style="color: blue;">'+data.userTxnData[i].grossAmount+'</p></td><td><b>NET AMOUNT:</b><br/><p style="color: blue;">'+data.userTxnData[i].netAmount+'</p><br/>'+
				 '<b>CALCULATION/DESCRIPTION:</b><br/><div class="netResultCalcDesc" style="height: 130px;overflow: auto;"></div><br/></td><td><div><b>TRANSACTION PURPOSE:</b><br/><p style="color: blue;">'+data.userTxnData[i].transactionPurpose+'</p></div></td>'+
				 '<td><div class="vendcustRemarks"></div></td>'+
				 '<td><div class="vendcustAcceptenceDiv"></div></td></tr>');
				 if(data.userTxnData[i].vendCustAcceptence=="1"){
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustAcceptenceDiv']").html("");
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustAcceptenceDiv']").html("<font color='green' size='4'><b>Accepted</b></p>");
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustRemarks']").html("");
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustRemarks']").html(data.userTxnData[i].vendCustRemarks);
				 }
				 if(data.userTxnData[i].vendCustAcceptence=="0"){
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustRemarks']").html("");
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustRemarks']").html('<input type="text" style="width: 140px;" id="vendcustuploadSuppDocs'+data.userTxnData[i].id+'" name="vendcustuploadSuppDocs'+data.userTxnData[i].id+'" readonly="readonly"><input type="button" id="vendcustuploadSuppDocs'+data.userTxnData[i].id+'" value="Upload" class="btn btn-primary btn-idos" onclick="uploadFile(this.id)"><br/>Remarks:<br/><textarea rows="1" name="vendcusttxnRemarks" id="vendcusttxnRemarks"></textarea>');
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustAcceptenceDiv']").html("");
					 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity"+data.userTxnData[i].id+"'] div[class='vendcustAcceptenceDiv']").html("<input type='button' value='Accept Transaction' id='acceptVendCustTransaction' class='btn btn-primary btn-idos' onclick='vendCustAcceptTransaction(this)'>");
				 }
				 if ('customer' === entityType.toLowerCase()) {
					 if(parseInt(data.userTxnData[i].transactionPurposeID) == RECEIVE_PAYMENT_FROM_CUSTOMER || parseInt(data.userTxnData[i].transactionPurposeID) ==RECEIVE_ADVANCE_FROM_CUSTOMER){
						 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity" + data.userTxnData[i].id + "'] div[class='vendcustAcceptenceDiv']").append('<form action="/exportReceiptPdf" class="'+data.userTxnData[i].transactionPurpose+'" name="receiptForm'+data.userTxnData[i].id+'" id="receiptForm'+data.userTxnData[i].id+'">'+
							 '<input type="button" value="Generate Voucher" id="generateReceipt" class="btn btn-submit btn-idos" onclick="generatePDFReceipt(this,'+data.userTxnData[i].transactionPurposeID+');"></form>');
					 }else {
						 $("#vendCustTransactions table[id='transactionTable'] tr[id='transactionEntity" + data.userTxnData[i].id + "'] div[class='vendcustAcceptenceDiv']").append('<div class="invoiceForm"><form action="/exportInvoicePdf" name="invoiceForm' + data.userTxnData[i].id + '" id="invoiceForm' + data.userTxnData[i].id + '"><input type="button" value="Generate Invoice" id="generateCustomerInvoiceBtn" email="' + data.userTxnData[i].email + '" class="btn btn-primary btn-idos" onclick="generateCustomerInvoice(this);"/></form></div>');
					 }
				 }
				 if(data.userTxnData[i].netAmtDesc!=null && data.userTxnData[i].netAmtDesc!=""){
					var individualNetDesc=data.userTxnData[i].netAmtDesc.substring(0,data.userTxnData[i].netAmtDesc.length).split(',');
					for(var m=0;m<individualNetDesc.length;m++){
					    var labelAndFigure=individualNetDesc[m].substring(0, individualNetDesc[m].length).split(':');
					    $('#vendCustTransactions table[id="transactionTable"] tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="netResultCalcDesc"]').append('<font color="FF00FF"><b>'+labelAndFigure[0]+'</b></p><br/>');
					    if(typeof labelAndFigure[1]!='undefined'){
					    	$('#vendCustTransactions table[id="transactionTable"] tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="netResultCalcDesc"]').append('<p style="color: blue;">'+labelAndFigure[1]+'</p><br/>');
					    }
					}
				 }
				 if(data.userTxnData[i].transactionVendCustOutstandings!=null && data.userTxnData[i].transactionVendCustOutstandings!=""){
					var individualtransactionVendCustOutstandings=data.userTxnData[i].transactionVendCustOutstandings.substring(0,data.userTxnData[i].transactionVendCustOutstandings.length).split(',');
					for(var m=0;m<individualtransactionVendCustOutstandings.length;m++){
						var labelAndFigure=individualtransactionVendCustOutstandings[m].substring(0, individualtransactionVendCustOutstandings[m].length).split(':');
						$('#vendCustTransactions table[id="transactionTable"] tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="vendCustOutstandings"]').append('<b>'+labelAndFigure[0]+'</b><br/>');
						if(typeof labelAndFigure[1]!='undefined'){
						   $('#vendCustTransactions table[id="transactionTable"] tr[id="transactionEntity'+data.userTxnData[i].id+'"] div[class="vendCustOutstandings"]').append('<p style="color: blue;">'+labelAndFigure[1]+'</p><br/>');
						}
					}
				}
			 }
			 if(data.userTxnData.length>0){
					var payMode=parseInt($("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(5)").css("width"));
					var status=parseInt($("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(8)").css("width"));
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(1)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(1)").css("width"));
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(2)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(2)").css("width"));
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(3)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(3)").css("width"));
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(4)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(4)").css("width"));
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(5)").css("width",payMode);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(6)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(6)").css("width"));
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(7)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(7)").css("width"));
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(8)").css("width",status);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(9)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(9)").css("width"));
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(10)").css("width",$("#vendCustTransactions table[id='transactionTable'] tbody tr td:nth-child(10)").css("width"));
				}else{
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(1)").css("width",118);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(2)").css("width",130);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(3)").css("width",110);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(4)").css("width",80);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(5)").css("width",60);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(6)").css("width",98);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(7)").css("width",187);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(8)").css("width",170);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(9)").css("width",145);
					$("#vendCustTransactions table[id='transactionTable'] thead tr th:nth-child(10)").css("width",214);
				}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

function vendCustAcceptTransaction(elem){
	//alert(">>>>>34"); //sunil
	var parentTr=$(elem).parent().parent().parent().attr('id');
	var transactionEntityId=parentTr.substring(17, parentTr.length);
	var vendorEmail=$("#hiddenvendcustemail").text();
	var vendUploadedDocUrl=$("#vendCustTransactions table[id='transactionTable'] tr[id='"+parentTr+"'] div[class='vendcustRemarks'] input[name*='vendcustuploadSuppDocs']").val();
	var vendorCustRemarks=$("#vendCustTransactions table[id='transactionTable'] tr[id='"+parentTr+"'] div[class='vendcustRemarks'] textarea[name='vendcusttxnRemarks']").val();
	var jsonData = {};
	jsonData.txnEntityId=transactionEntityId;
	jsonData.txnvendorEmail=vendorEmail;
	jsonData.txnvendUploadedDocUrl=vendUploadedDocUrl;
	jsonData.txnvendorCustRemarks=vendorCustRemarks;
	var url="/vendorcustomer/acceptTransaction";
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
			$("#vendCustTransactions table[id='transactionTable'] tr[id='"+parentTr+"'] div[class='vendcustAcceptenceDiv']").html("");
			$("#vendCustTransactions table[id='transactionTable'] tr[id='"+parentTr+"'] div[class='vendcustAcceptenceDiv']").html("<font color='green' size='4'><b>Accepted</b></p>");
			$("#vendCustTransactions table[id='transactionTable'] tr[id='"+parentTr+"'] div[class='vendcustRemarks']").html("");
			$("#vendCustTransactions table[id='transactionTable'] tr[id='"+parentTr+"'] div[class='vendcustRemarks']").html(vendorCustRemarks);
			var jsonData = {};
			jsonData.transactionPrimId = transactionEntityId;
			jsonData.txnvendorEmail=vendorEmail;
			jsonData.txnRmarks=vendorCustRemarks;
			jsonData.suppDoc=vendUploadedDocUrl;
			jsonData.selectedApproverAction="8";
			jsonData.txnReferenceNo = "txnReferenceNo";
			var url="/transaction/approverAction";
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
				},
				error: function(xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

$(document).ready(function() {
	$(".addnewGSTINCust").click(function(){
		var isRegister = $('#custRegisteredOrUnReg').val();
		if(isRegister == 1){
			$("#staticMutipleGSTINCustItems").attr('data-toggle', 'modal');
			$("#staticMutipleGSTINCustItems").modal('show');
			$(".staticMutipleGSTINCustItemsclose").attr("href",location.hash);
			clearCustomerGstinPopup();
			enbDisPopupShipAddress(false);
		} else {
			swal("For unregistered GST Customer, you can't add a GSTIN.");
		}
		
	});
});

$(document).ready(function() {
	$(".addPopupGSTINCustInfoBtn").click(function(){
		var stateGstinCode = $("#gstinputCustPopup").val();
		var gstinsecondPart = $("#gstinput2CustPopup").val();
		var gstinCode = stateGstinCode + gstinsecondPart;

		if(stateGstinCode !== "" && gstinsecondPart === ""){
			swal("Invalid!","Invalid GSTIN! Second part of GSTIN cannot be empty","error");
            return false;
        }
		if(gstinCode < 15 || gstinCode.length > 15){
			swal("Invalid!","Invalid GSTIN! Please provide correct GSTIN","error");
			return false;
	    }
/*		if((gstinCode.length > 1) && (gstinCode.length < 15 || gstinCode.length > 15)){
	        alert("Invalid GSTIN! Please provide correct GSTIN");
	        return false;
	    }
*/

	    var custAddress=$("#customerAddressPopup").val();
		var custCountry=$("#customercountryPopup option:selected").val();
		var custState=$("#customerstatePopup option:selected").text();
		var custStateCode=$("#customerstatePopup option:selected").val();
		var custLocation=$("#custlocationPopup").val();
		var custPhnCtryCode=$("#custPhnNocountryCodePopup option:selected").val();
		var custPhone1=$("#custphone1Popup").val();
		var custPhone2= $("#custphone2Popup").val();
		var custPhone3= $("#custphone3Popup").val();
		var shipcustCountry ="";    var shipcustAddress ="";  var shipcustState ="";
		var shipcustStateCode ="";  var shipcustLocation =""; var shipcustPhnCtryCodeVal ="";
		var shipcustPhone1 ="";     var shipcustPhone2 ="";   var shipcustPhone3 ="";

		var isShippingAddressSame=$("#isShippingAddressSamePopup").is(':checked');
		if(isShippingAddressSame){
			shipcustCountry = custCountry;
			shipcustAddress = custAddress;
			shipcustState = custState;
			shipcustStateCode = $("#customerstatePopup option:selected").val();
			shipcustLocation = custLocation;
			shipcustPhnCtryCodeVal = custPhnCtryCode;
			shipcustPhone1 = custPhone1;
			shipcustPhone2 = custPhone2;
			shipcustPhone3 = custPhone2;
		}else{
			shipcustCountry = $("#shipcustomercountryPopup option:selected").val();
			shipcustAddress = $("#shipcustomerAddressPopup").val();
			shipcustState = $("#shipstatePopup option:selected").text();
			shipcustStateCode = $("#shipstatePopup option:selected").val();
			shipcustLocation = $("#shipcustlocationPopup").val();
			shipcustPhnCtryCodeVal = $("#shipcustPhnNocountryCodePopup option:selected").val();
			shipcustPhone1 = $("#shipcustphone1Popup").val();
			shipcustPhone2 = $("#shipcustphone2Popup").val();
			shipcustPhone3 = $("#shipcustphone3Popup").val();
		}

		if(shipcustAddress == ""){
			swal("Error!","Please provide valid shipping address.","error");
			return false;
		}
		if(shipcustState == ""){
			swal("Error!","Please provide valid shipping state.","error");
			return false;
		}
		/*GST-95
			else if(stateGstinCode.length > 1 && shipcustStateCode != stateGstinCode){
	    	alert("GSTIN and shipping State does not match, please provide valid shipping state/GSTIN.");
	    	return false;
	    }*/
		if(shipcustLocation == ""){
			swal("Invalid!","Please provide valid shippping location.","error");
			return false;
		}

		var customerDetailIdListHid = $("#customerDetailIdListHid").val();
		var gstinCodeHid = $("#gstinCodeHid").val();
		var customerAddressHid = $("#customerAddressHid").val();
		var customercountryCodeHid = $("#customercountryCodeHid").val();
		var custstateHid = $("#custstateHid").val();
		var custstatecodeHid = $("#custstatecodeHid").val();
		var custlocationHid = $("#custlocationHid").val();
		var custPhnNocountryCodeHid = $("#custPhnNocountryCodeHid").val();
		//var custPhnNocountryTextHid = $("#custPhnNocountryTextHid").val();
		var custphone1Hid = $("#custphone1Hid").val();
		var custphone2Hid = $("#custphone2Hid").val();
		var custphone3Hid = $("#custphone3Hid").val();

		var isShippingAddressSameHid = $("#isShippingAddressSameHid").val();
		var shipcustomerAddressHid = $("#shipcustomerAddressHid").val();
		var shipcustomerCountryCodeHid = $("#shipcustomerCountryCodeHid").val();
		var shipstateHid = $("#shipstateHid").val();
		var shipcustStateCodeHid = $("#shipcustStateCodeHid").val();
		var shiptlocationHid = $("#shiptlocationHid").val();

		var shipcustPhnNoCountryCodeHid = $("#shipcustPhnNoCountryCodeHid").val();
		//var shipcustPhnNoCountryTextHid = $("#shipcustPhnNoCountryTextHid").val();
		var shipcustPhone1Hid = $("#shipcustPhone1Hid").val();
		var shipcustPhone2Hid = $("#shipcustPhone2Hid").val();
		var shipcustPhone3Hid = $("#shipcustPhone3Hid").val();

		//Manali
		var updatedCustomerIdHid = $("#updatedCustomerIdHid").val();
		var detailIdListHid = $("#customerDetailIdListHid").val().split('|');
		var idIndex = detailIdListHid.indexOf(updatedCustomerIdHid);

		if(updatedCustomerIdHid==''|| idIndex == 0 || idIndex == -1){
			var custId = $("#customerDetailIdHid").val();
			if(custId == ""){
				custId = 0;
			}else if(custId < 1){
				custId = custId - 1;
			}else{
				custId = parseInt(custId) + 1;
			}
			customerDetailIdListHid = customerDetailIdListHid + "|" + custId;
			gstinCodeHid = gstinCodeHid + "|" + gstinCode;
			customerAddressHid = customerAddressHid+ "|" + custAddress;
			customercountryCodeHid = customercountryCodeHid+ "|" + custCountry;
			custstateHid = custstateHid+ "|" + custState;
			custstatecodeHid = custstatecodeHid+ "|" + custStateCode;
			custlocationHid = custlocationHid+ "|" + custLocation;
			custPhnNocountryCodeHid = custPhnNocountryCodeHid+ "|" + custPhnCtryCode;
			//custPhnNocountryTextHid = custPhnNocountryTextHid+ "|" + custPhnCtryCodeText;
			custphone1Hid = custphone1Hid+ "|" + custPhone1;
			custphone2Hid = custphone2Hid+ "|" + custPhone2;
			custphone3Hid = custphone3Hid+ "|" + custPhone3;

			isShippingAddressSameHid = isShippingAddressSameHid+ "|" + isShippingAddressSame;
			shipcustomerAddressHid = shipcustomerAddressHid+ "|" + shipcustAddress;
			shipcustomerCountryCodeHid = shipcustomerCountryCodeHid+ "|" + shipcustCountry;
			shipstateHid = shipstateHid+ "|" + shipcustState;
			shipcustStateCodeHid = shipcustStateCodeHid+ "|" + shipcustStateCode;
			shiptlocationHid = shiptlocationHid+ "|" + shipcustLocation;
			//shipcustPhnNoCountryTextHid = shipcustPhnNoCountryTextHid + "|" + shipcustPhnCtryCodeText
			shipcustPhnNoCountryCodeHid = shipcustPhnNoCountryCodeHid+ "|" + shipcustPhnCtryCodeVal;
			shipcustPhone1Hid = shipcustPhone1Hid+ "|" +shipcustPhone1;
			shipcustPhone2Hid = shipcustPhone2Hid+ "|" +shipcustPhone2;
			shipcustPhone3Hid = shipcustPhone3Hid+ "|" +shipcustPhone3;

		}else{
			var customergstinCodeHidArr = $("#gstinCodeHid").val().split('|');
			var customerAddressHidArr = $("#customerAddressHid").val().split('|');
			var customercountryCodeHidArr = $("#customercountryCodeHid").val().split('|');
			var custstateHidArr = $("#custstateHid").val().split('|');
			var custstatecodeHidArr = $("#custstatecodeHid").val().split('|');
			var custlocationHidArr = $("#custlocationHid").val().split('|');
			var custPhnNocountryCodeHidArr = $("#custPhnNocountryCodeHid").val().split('|');
			var custphone1HidArr = $("#custphone1Hid").val().split('|');
			var custphone2HidArr = $("#custphone2Hid").val().split('|');
			var custphone3HidArr = $("#custphone3Hid").val().split('|');
			var isShippingAddressSameHidArr = $("#isShippingAddressSameHid").val().split('|');
			var shipcustomerAddressHidArr = $("#shipcustomerAddressHid").val().split('|');
			var shipcustomerCountryCodeHidArr = $("#shipcustomerCountryCodeHid").val().split('|');
			var shipstateHidArr = $("#shipstateHid").val().split('|');
			var shipstatecodeHidArr = $("#shipcustStateCodeHid").val().split('|');
			var shiptlocationHidArr = $("#shiptlocationHid").val().split('|');
			var shipcustPhnNoCountryCodeHidArr = $("#shipcustPhnNoCountryCodeHid").val().split('|');
			var shipcustPhone1HidArr = $("#shipcustPhone1Hid").val().split('|');
			var shipcustPhone2HidArr = $("#shipcustPhone2Hid").val().split('|');
			var shipcustPhone3HidArr = $("#shipcustPhone3Hid").val().split('|');

			customergstinCodeHidArr[idIndex]=gstinCode;
			customerAddressHidArr[idIndex]=custAddress;
			customercountryCodeHidArr[idIndex]=custCountry;
			custstateHidArr[idIndex]=custState;
			custstatecodeHidArr[idIndex]=custStateCode;
			custlocationHidArr[idIndex]=custLocation;
			custPhnNocountryCodeHidArr[idIndex]=custPhnCtryCode;
			custphone1HidArr[idIndex]=custPhone1;
			custphone2HidArr[idIndex]=custPhone2;
			custphone3HidArr[idIndex]=custPhone3;
			isShippingAddressSameHidArr[idIndex]=isShippingAddressSame;
			shipcustomerAddressHidArr[idIndex]=shipcustAddress;
			shipcustomerCountryCodeHidArr[idIndex]=shipcustCountry;
			shipstateHidArr[idIndex]=shipcustState;
			shipstatecodeHidArr[idIndex]=shipcustStateCode;
			shiptlocationHidArr[idIndex]=shipcustLocation;
			shipcustPhnNoCountryCodeHidArr[idIndex]=shipcustPhnCtryCodeVal;
			shipcustPhone1HidArr[idIndex]=shipcustPhone1;
			shipcustPhone2HidArr[idIndex]=shipcustPhone2;
			shipcustPhone3HidArr[idIndex]=shipcustPhone3;

			gstinCodeHid = customergstinCodeHidArr.join('|');
			customerAddressHid = customerAddressHidArr.join('|');
			customercountryCodeHid = customercountryCodeHidArr.join('|');
			custstateHid = custstateHidArr.join('|');
			custstatecodeHid = custstatecodeHidArr.join('|');
			custlocationHid = custlocationHidArr.join('|');
			custPhnNocountryCodeHid = custPhnNocountryCodeHidArr.join('|');
			custphone1Hid = custphone1HidArr.join('|');
			custphone2Hid = custphone2HidArr.join('|');
			custphone3Hid = custphone3HidArr.join('|');
			isShippingAddressSameHid = isShippingAddressSameHidArr.join('|');
			shipcustomerAddressHid = shipcustomerAddressHidArr.join('|');
			shipcustomerCountryCodeHid = shipcustomerCountryCodeHidArr.join('|');
			shipstateHid = shipstateHidArr.join('|');
			shipcustStateCodeHid = shipstatecodeHidArr.join('|');
			shiptlocationHid = shiptlocationHidArr.join('|');
			shipcustPhnNoCountryCodeHid = shipcustPhnNoCountryCodeHidArr.join('|');
			shipcustPhone1Hid = shipcustPhone1HidArr.join('|');
			shipcustPhone2Hid = shipcustPhone2HidArr.join('|');
			shipcustPhone3Hid = shipcustPhone3HidArr.join('|');
		}

		$("#customerDetailIdHid").val(custId);
		$("#customerDetailIdListHid").val(customerDetailIdListHid);
		$("#gstinCodeHid").val(gstinCodeHid);
		$("#customerAddressHid").val(customerAddressHid);
		$("#customercountryCodeHid").val(customercountryCodeHid);
		$("#custstateHid").val(custstateHid);
		$("#custstatecodeHid").val(custstatecodeHid);
		$("#custlocationHid").val(custlocationHid);
		$("#custPhnNocountryCodeHid").val(custPhnNocountryCodeHid);
		//$("#custPhnNocountryTextHid").val(custPhnNocountryTextHid);
		$("#custphone1Hid").val(custphone1Hid); $("#custphone2Hid").val(custphone2Hid); $("#custphone3Hid").val(custphone3Hid);

		$("#isShippingAddressSameHid").val(isShippingAddressSameHid);
		$("#shipcustomerAddressHid").val(shipcustomerAddressHid);
		$("#shipcustomerCountryCodeHid").val(shipcustomerCountryCodeHid);
		$("#shipstateHid").val(shipstateHid);
		$("#shipcustStateCodeHid").val(shipcustStateCodeHid);
		$("#shiptlocationHid").val(shiptlocationHid);
		//$("#shipcustPhnNoCountryTextHid").val(shipcustPhnNoCountryTextHid);
		$("#shipcustPhnNoCountryCodeHid").val(shipcustPhnNoCountryCodeHid);
		$("#shipcustPhone1Hid").val(shipcustPhone1Hid);  $("#shipcustPhone2Hid").val(shipcustPhone2Hid);   $("#shipcustPhone3Hid").val(shipcustPhone3Hid);

		if(updatedCustomerIdHid==''|| idIndex == 0 || idIndex == -1){//If new row in GSTIN list
			$("#customerGstinTbl tbody").append('<tr><td style="cursor:pointer" onClick="popupCustomerGstinDetail(\''+custId+'\');">'+shipcustState+'</td><td>'+gstinCode+'<input id="shipCustCity" type="hidden" value="'+shipcustLocation+'"/><input id="customerDetID" type="hidden" value="'+custId+'"/></td><td><input type="checkbox" id="gstinEnableCheck" checked="checked"/></td></tr>');
		}
		$("#updatedCustomerIdHid").val("");
		clearCustomerGstinPopup();
		$(".modal .close").click();
	});
});

var popupCustomerGstinDetail = function(customerId){
	clearCustomerGstinPopup();
	$("#updatedCustomerIdHid").val(customerId);
	var customerDetailIdListHid = $("#customerDetailIdListHid").val();
	var gstinCodeHid = $("#gstinCodeHid").val().split('|');
	var customerAddressHid = $("#customerAddressHid").val();
	var customercountryCodeHid = $("#customercountryCodeHid").val();
	var custstateHid = $("#custstateHid").val();
	var custlocationHid = $("#custlocationHid").val().split('|');
	var custPhnNocountryCodeHid = $("#custPhnNocountryCodeHid").val().split('|');
	//var custPhnNocountryTextHid = $("#custPhnNocountryTextHid").val().split('|');
	var custphone1Hid = $("#custphone1Hid").val().split('|');
	var custphone2Hid = $("#custphone2Hid").val().split('|');
	var custphone3Hid = $("#custphone3Hid").val().split('|');

	var isShippingAddressSameHid = $("#isShippingAddressSameHid").val().split('|');
	var shipcustomerAddressHid = $("#shipcustomerAddressHid").val().split('|');
	var shipcustomerCountryCodeHid = $("#shipcustomerCountryCodeHid").val().split('|');
	var shipstateHid = $("#shipstateHid").val().split('|');
	//var shipcustStateCodeHid = $("#shipcustStateCodeHid").val().split('|');
	var shiptlocationHid = $("#shiptlocationHid").val().split('|');
	var shipcustPhnNoCountryCodeHid = $("#shipcustPhnNoCountryCodeHid").val().split('|');
	//var shipcustPhnNoCountryTextHid = $("#shipcustPhnNoCountryTextHid").val().split('|');
	var shipcustPhone1Hid = $("#shipcustPhone1Hid").val().split('|');
	var shipcustPhone2Hid = $("#shipcustPhone2Hid").val().split('|');
	var shipcustPhone3Hid = $("#shipcustPhone3Hid").val().split('|');

	var customerDetailIdArray = customerDetailIdListHid.split('|');
	var customerIdIndex = customerDetailIdArray.indexOf(customerId);
	$("#staticMutipleGSTINCustItems").attr('data-toggle', 'modal');
	$("#staticMutipleGSTINCustItems").modal('show');
	$(".staticMutipleGSTINCustItemsclose").attr("href",location.hash);
	var gstinCode = gstinCodeHid[customerIdIndex];
	var gstinCode1 = gstinCode.substring(0,2);
	var gstinCode2 = gstinCode.substring(2,15);
	$("#customerPopupDetailsTable input[name='gstinPart1']").val(gstinCode1);
	$("#customerPopupDetailsTable input[name='gstinPart2']").val(gstinCode2);
	var customerAddressArray = customerAddressHid.split('|');
	$("#customerPopupDetailsTable textarea[name='addressPopup']").val(customerAddressArray[customerIdIndex]);
	var customercountryCodeArray = customercountryCodeHid.split('|');
	$("#customerPopupDetailsTable select[name='countryPopup']").find("option[value='"+customercountryCodeArray[customerIdIndex]+"']").prop("selected", "selected");
	var custstateArray = custstateHid.split('|');
	$("#customerPopupDetailsTable select[name='statePopup'] option").filter(function(){ return $(this).text() == custstateArray[customerIdIndex]; }).prop('selected', true);
	$("#customerPopupDetailsTable input[name='locationPopup']").val(custlocationHid[customerIdIndex]);
	$("#customerPopupDetailsTable select[name='phnNocountryCodePopup'] option").filter(function(){return $(this).html()==custPhnNocountryCodeHid[customerIdIndex];}).prop('selected',true);
	//$("#customerPopupDetailsTable select[name='phnNocountryCodePopup']").find("option[value='"+custPhnNocountryCodeHid[customerIdIndex]+"']").prop("selected", "selected");
	$("#customerPopupDetailsTable input[name='phone1Popup']").val(custphone1Hid[customerIdIndex]);
	$("#customerPopupDetailsTable input[name='phone2Popup']").val(custphone2Hid[customerIdIndex]);
	$("#customerPopupDetailsTable input[name='phone3Popup']").val(custphone3Hid[customerIdIndex]);
	if(isShippingAddressSameHid[customerIdIndex] === 'true'){
		$("#customerPopupDetailsTable input[name='isAddressSamePopup']").prop('checked', true);
		enbDisPopupShipAddress(true);
		return false;
	}
	$("#customerPopupDetailsTable textarea[name='shipAddressPopup']").val(shipcustomerAddressHid[customerIdIndex]);
	$("#customerPopupDetailsTable select[name='shipCountryPopup']").find("option[value='"+shipcustomerCountryCodeHid[customerIdIndex]+"']").prop("selected", "selected");
	$("#customerPopupDetailsTable select[name='shipStatePopup'] option").filter(function(){ return $(this).text() == shipstateHid[customerIdIndex]; }).prop('selected', true);
	$("#customerPopupDetailsTable input[name='shipLocationPopup']").val(shiptlocationHid[customerIdIndex]);
	$("#customerPopupDetailsTable select[name='shipPhnNocountryCodePopup']").find("option[value='"+shipcustPhnNoCountryCodeHid[customerIdIndex]+"']").prop("selected", "selected");
	$("#customerPopupDetailsTable input[name='shipphone1Popup']").val(shipcustPhone1Hid[customerIdIndex]);
	$("#customerPopupDetailsTable input[name='shipphone2Popup']").val(shipcustPhone2Hid[customerIdIndex]);
	$("#customerPopupDetailsTable input[name='shipphone3Popup']").val(shipcustPhone3Hid[customerIdIndex]);

}

var clearCustomerGstinPopup = function(){
	$("#gstinputCustPopup").val('');
	$("#gstinput2CustPopup").val('');
	$("#customerAddressPopup").val('');
	$("#customercountryPopup").find('option:first').prop("selected","selected");
	$("#customerstatePopup").find('option:first').prop("selected","selected");
	$("#custlocationPopup").val('');
	$("#custPhnNocountryCodePopup").find('option:first').prop("selected","selected");
	$("#custphone1Popup").val('');
	$("#custphone2Popup").val('');
	$("#custphone3Popup").val('');

	$("#isShippingAddressSamePopup").attr('checked', false);
	$("#shipcustomercountryPopup").find('option:first').prop("selected","selected");
	$("#shipcustomerAddressPopup").val('');
	$("#shipstatePopup").find('option:first').prop("selected","selected");
	$("#shipcustlocationPopup").val('');
	$("#shipcustPhnNocountryCodePopup").find('option:first').prop("selected","selected");
	$("#shipcustphone1Popup").val('');
	$("#shipcustphone2Popup").val('');
	$("#shipcustphone3Popup").val('');
}

$(document).ready(function(){
	$('input[name="isAddressSamePopup"]').click(function(){
		var isShippingAddressSame = $(this).is(':checked');
		enbDisPopupShipAddress(isShippingAddressSame);
	});

	$("#custRegisteredOrUnReg").change(function(){
		if($(this).val() == "1"){
			$("#gstinputCust").removeAttr("disabled");
			$("#gstinput2Cust").removeAttr("disabled");
			//$("#customerDetailsTable select[id='shipCustomerState']").attr("disabled", "disabled");
		}else{
			//$("#customerDetailsTable select[id='shipCustomerState']").removeAttr("disabled");
			$("#gstinputCust").val("");
			$("#gstinput2Cust").val("");
			$("#gstinputCust").attr("disabled", "disabled");
			$("#gstinput2Cust").attr("disabled", "disabled");
		}
	});

});

var enbDisPopupShipAddress = function(isShippingAddressSame){
	if(isShippingAddressSame == true){
		$("#customerPopupDetailsTable textarea[name='shipAddressPopup']").attr("disabled", "disabled");
		$("#customerPopupDetailsTable select[name='shipCountryPopup']").attr("disabled", "disabled");
		$("#customerPopupDetailsTable select[name='shipStatePopup']").attr("disabled", "disabled");
		$("#customerPopupDetailsTable input[name='shipLocationPopup']").attr("disabled", "disabled");
		$("#customerPopupDetailsTable select[name='shipPhnNocountryCodePopup']").attr("disabled", "disabled");
		$("#customerPopupDetailsTable input[name='shipphone1Popup']").attr("disabled", "disabled");
		$("#customerPopupDetailsTable input[name='shipphone2Popup']").attr("disabled", "disabled");
		$("#customerPopupDetailsTable input[name='shipphone3Popup']").attr("disabled", "disabled");
	}else{
		$("#customerPopupDetailsTable textarea[name='shipAddressPopup']").removeAttr("disabled");
		$("#customerPopupDetailsTable select[name='shipCountryPopup']").removeAttr("disabled");
		$("#customerPopupDetailsTable select[name='shipStatePopup']").removeAttr("disabled");
		$("#customerPopupDetailsTable input[name='shipLocationPopup']").removeAttr("disabled");
		$("#customerPopupDetailsTable select[name='shipPhnNocountryCodePopup']").removeAttr("disabled");
		$("#customerPopupDetailsTable input[name='shipphone1Popup']").removeAttr("disabled");
		$("#customerPopupDetailsTable input[name='shipphone2Popup']").removeAttr("disabled");
		$("#customerPopupDetailsTable input[name='shipphone3Popup']").removeAttr("disabled");
	}
}

// $(document).ready(function(){
// 	$('#close-branchAdvOpeningBalLabel').click(function(){
// 		$("#branchAdvBalTable tbody tr").each(function() {
// 			// Find the hidden input with class "advId" in the current row
// 			var advIdInput = $(this).find(".advId");
		
// 			// Check if advId value is empty
// 			if (advIdInput.val() === "") {
// 				// Remove the current row and its parent row
// 				$(this).remove();
// 			}
// 		});
// 	})
// });

var clearCustomerPopupHidden = function(){
	$("#customerDetailIdHid").val('');
	$("#customerDetailIdListHid").val('');
	$("#gstinCodeHid").val('');
	$("#customerAddressHid").val('');
	$("#customercountryCodeHid").val('');
	$("#custstateHid").val('');
	$("#custstatecodeHid").val('');
	$("#custlocationHid").val('');
	$("#custPhnNocountryCodeHid").val('');
	$("#custphone1Hid").val('');
	$("#custphone2Hid").val('');
	$("#custphone3Hid").val('');

	$("#isShippingAddressSameHid").val('');
	$("#shipcustomerAddressHid").val('');
	$("#shipcustomerCountryCodeHid").val('');
	$("#shipstateHid").val('');
	$("#shipcustStateCodeHid").val('');
	$("#shiptlocationHid").val('');
	$("#shipcustPhnNoCountryCodeHid").val('');
	$("#shipcustPhone1Hid").val('');
	$("#shipcustPhone2Hid").val('');
	$("#shipcustPhone3Hid").val('');

	// $("#branchAdvBalTable tbody").empty();
}

var clearCustomerForm = function(detailForm){
	$('.'+detailForm+' input[type="hidden"]').val("");
	$('.'+detailForm+' input[type="text"]').val("");
	$('.'+detailForm+' textarea').val("");
	$('.'+detailForm+' input[type="password"]').val("");
	$('.'+detailForm+' select option:first').prop("selected", "selected");
	$('.'+detailForm+' select[class="countryPhnCode"]').each(function () {
		$(this).find('option:first').prop("selected", "selected");
    });
	$('.'+detailForm+' select[class="countryDropDown"]').each(function () {
		$(this).find('option:first').prop("selected", "selected");
    });
	 $('.'+detailForm+' select[id="custPriceListUploads"]').children().remove();
	 $('.'+detailForm+' select[id="custPriceListUploads"]').append('<option value="">Select to view files</option>');
	$('.'+detailForm+' input[type="hidden"]').val("");
	$('.'+detailForm+' input[type="text"]').val("");
	$('.'+detailForm+' textarea').val("");
	$('.'+detailForm+' input[type="password"]').val("");
	$('.'+detailForm+' select option:first').prop("selected", "selected");
	$('.'+detailForm+' select[class="countryPhnCode"]').each(function () {
		$(this).find('option:first').prop("selected", "selected");
    });
	$('.'+detailForm+' select[class="countryDropDown"]').each(function () {
		$(this).find('option:first').prop("selected", "selected");
    });
    $("#custRegisteredOrUnReg").find('option:first').prop("selected", "selected");
    $("#custBusinessIndividual").find('option:first').prop("selected", "selected");
    $("#customerState").find('option:first').prop("selected", "selected");
    $("#shipCustomerState").find('option:first').prop("selected", "selected");
    $("#shipCustomerState").removeAttr("disabled");
    $("#customerGstinTbl tbody").html("");
    $('#customer-form-container #customerBranchDropdownBtn').html('None Selected&nbsp;<b class="caret" style="margin: 5px auto;"></b>');
	$(".para-tm3-bm01").show();
}


var checkUncheckAllVendCustBranches = function(elem, dropdownID) {
	//var dropdownID = $(dropdown).attr('id');
	var checked=$(elem).is(':checked');
	var checkvalue=$(elem).val();
	if(checked==true){
		$('#'+dropdownID+' li[class="customerVendorBranchCls"] input[type="checkbox"]').each(function () {
	        $(this).prop("checked" ,true);
		});
	}else if(checked==false){
		$('#'+dropdownID+' li[class="customerVendorBranchCls"] input[type="checkbox"]').each(function () {
	        $(this).prop("checked" ,false);
		});
	}

	var parentTr=$(elem).closest('tr').attr('id');
	var check_box_values = $('#'+dropdownID+' li[class="customerVendorBranchCls"] input[type="checkbox"]:checkbox:checked').map(function() {
	    return this.value;
	}).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Branches ";
		$("#"+parentTr+" button[name='branchDropdownBtn']").text(text);
		$("#"+parentTr+" button[name='branchDropdownBtn']").append("&nbsp;&nbsp;<b class='caret'></b>");
	}else if(length==0){
		$("#"+parentTr+" button[name='branchDropdownBtn']").text("None Selected");
		$("#"+parentTr+" button[name='branchDropdownBtn']").append("&nbsp;&nbsp;<b class='caret'></b>");
	}

	$('#'+dropdownID+' li[class="customerVendorBranchCls"] input[class="openingBalance"]').each(function () {
        var tmpValue = $(this).val();
        if(tmpValue == null || tmpValue ==""){
        	$(this).val(0.0);
        }else if(checked==false){
        	$(this).val('');
        }
	});

	$('#'+dropdownID+' li[class="customerVendorBranchCls"] input[class="openingBalanceAP"]').each(function () {
        var tmpValue = $(this).val();
        if(tmpValue == null || tmpValue ==""){
        	$(this).val(0.0);
        }else if(checked==false){
        	$(this).val('');
        }
	});
}

//************* START Bill Wise Opening Balance Inner Table*******************

$(function(){
	$("#addBranchOpBalButton").on("click",addBranchOpBalDetails);
	$("#removeBranchOpBalButton").on("click",removeBranchOpBalDetails);
});

$(function(){
	$("#addBranchAdvBalButton").on("click",addBranchAdvBalDetails);
	$("#removeBranchAdvBalButton").on("click",removeBranchAdvBalDetails);
});

function calculateOpeningBalGrandTotal() {
	var total = 0;
	$("#branchOpBalTable").find('.billAmount').each(function(){
		var amount = $(this).val();
		var delType = $.trim($(this).closest("tr").find(".delType").val());
		if(amount !== "" && delType !== "DEL") {
			total += parseFloat(amount);
		}
	});
	$("#grandTotalOpBal").html(total.toFixed(2));
}

function calculateOpeningAdvGrandTotal() {
	var total = 0;
	$("#branchAdvBalTable").find('.advAmount').each(function(){
		var amount = $(this).val();
		var delType = $.trim($(this).closest("tr").find(".delType").val());
		if(amount !== "" && delType !== "DEL") {
			total += parseFloat(amount);
		}
	});
	$("#grandTotalAdvBal").html(total.toFixed(2));
}

function saveBillWiseOpeningBal(modalOwner){
	$("#branchOpeningBalModal").find("#modalOwner").val(modalOwner);
	$("#branchOpeningBalModal").modal('show');
}

function saveAdvanceOpeningBal(modalOwner){
	$("#branchAdvOpeningBalModal").find("#modalOwner").val(modalOwner);
	$("#branchAdvOpeningBalModal").modal('show');
}

function addBranchOpBalDetails() {
	if(!validateBranchBillDetails()) {
		return false;
	}
	var modalOwner = $("#branchOpeningBalModal").find("#modalOwner").val();
	$("#grandTotalOpBal").html("0.0");
	let branchList = Object.values(ALL_BRANCH_OF_ORG_MAP);
	$("#branchOpBalTable > tbody").append(billwiseRowTemplate(branchList));
	$("#branchOpBalTable > tbody").find("table > tbody > tr:last").find('.billDate').datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
}

function addBranchAdvBalDetails() {
	if(!validateBranchAdvDetails()) {
		return false;
	}
	var modalOwner = $("#branchAdvOpeningBalModal").find("#modalOwner").val();
	$("#grandTotalAdvBal").html("0.0");
	// let branchList = Object.values(ALL_BRANCH_OF_ORG_MAP);
	var selectedOptions = [];
	if(modalOwner == "CUSTOMER") {
		$('#customerBranchList input[type="checkbox"]:checked').each(function() {
			if (!$(this).hasClass('vendCustSelectAllCls')){
				// Get the name associated with the checkbox
				var branchName = $(this).closest('li').find('.branchNameLabel').text();
				// Get the value of the checkbox
				var branchValue = $(this).val();
				// Create an option element and push it to the array
				var optionElement = '<option value="' + branchValue + '">' + branchName + '</option>';
				selectedOptions.push(optionElement);
			}
		});
	} else {
		$('#vendorBranchList input[type="checkbox"]:checked').each(function() {
			if (!$(this).hasClass('vendCustSelectAllCls')) {
				// Get the name associated with the checkbox
				var branchName = $(this).closest('li').find('.branchNameLabel').text();
				// Get the value of the checkbox
				var branchValue = $(this).val();
				// Create an option element and push it to the array
				var optionElement = '<option value="' + branchValue + '">' + branchName + '</option>';
				selectedOptions.push(optionElement);
			}
		
		});
	}
	
	$("#branchAdvBalTable > tbody").append(branchwiseAdvRowTemplate(selectedOptions));
	$("#branchAdvBalTable > tbody").find("table > tbody > tr:last").find('.receiptDate').datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
	$(".receiptDate").datepicker("option", "maxDate", new Date());
}

function billwiseRowTemplate(itemData) {
	var row = [];
	i=0;
		row[i++] = '<tr><td><select class="branchItem" name="branchItem" id="branchItem" onChange="billBranchChanged(this);"><option value="">--Please Select--</option>';
		row[i++] = itemData.join(" ");
		row[i++] = '</select></td><td colspan="7"><table class="table innerBranchBillTable" id="innerBranchBillTable"><tbody>';
		row[i++] = innerBillOPRowTemplate();
		row[i++] = '</tbody></table></td><td><input type="checkbox" class="removeCheckBox" style="height: 18px;width: 18px;margin: 10px;"></td></tr>';
	return row.join(" ");
}

function branchwiseAdvRowTemplate(itemData) {
	var row = [];
	var rowId = 'advRow_' + uniqueIdCounter 
	i=0;
		row[i++] = '<tr id='+ rowId +'><td style="width: 140px;"><select class="branchItem" name="branchItem" id="branchItem" onchange="advBranchChanged(this);"><option value="">--Please Select--</option>';
		row[i++] = itemData.join(" ");
		row[i++] = '</select></td><td><table class="table innerBranchAdvTable" id="innerBranchAdvTable"><tbody>';
		row[i++] = innerAdvOPRowTemplate(rowId);
		row[i++] = '</tbody></table></td><td><input type="checkbox" class="removeCheckBox" style="height: 18px;width: 18px;margin: 10px;"></td></tr>';
		uniqueIdCounter++;
	return row.join(" ");
}

function removeBranchOpBalDetails(comp) {
		$("#branchOpBalTable").find(".removeCheckBox:checkbox:checked").each(function(){
			var branch = $(this).closest('tr').find(".branchItem").val();
			if(branch != "") {
				var modalOwner = $("#branchOpeningBalModal").find("#modalOwner").val();
				if(modalOwner == "VENDOR") {
					$("#vendorBranchList").find("#"+branch).find(".openingBalance").val("0.0");
					$("#vendorBranchList").find("#"+branch).find(".openingBalance").removeAttr('readonly');
					$("#vendorBranchList").find("#"+branch).find(".openingBalance").blur();
				}if(modalOwner == "CUSTOMER") {
					$("#customerBranchList").find("#"+branch).find(".openingBalance").val("0.0");
					$("#customerBranchList").find("#"+branch).find(".openingBalance").removeAttr('readonly');
					$("#customerBranchList").find("#"+branch).find(".openingBalance").blur();
				}
			}
            $(this).closest('tr').remove();
        });

	calculateOpeningBalGrandTotal();
}

function removeBranchAdvBalDetails(comp) {
	$("#branchAdvBalTable").find(".removeCheckBox:checkbox:checked").each(function(){
		var branchRow = $(this).closest('tr');
		var branch = branchRow.find(".branchItem").val();
		if(branch != "") {
			var modalOwner = $("#branchAdvOpeningBalModal").find("#modalOwner").val();
			if(modalOwner == "VENDOR") {
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").val("0.0");
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").removeAttr('readonly');
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").blur();
			}if(modalOwner == "CUSTOMER") {
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").val("0.0");
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").removeAttr('readonly');
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").blur();
			}
		}
		// $(this).closest('tr').remove();
		// branchRow.remove();
		if(branchRow.find(".delType").val() == "EDIT") {
			branchRow.find(".delType").val("DEL");
			branchRow.hide();
		}else {
			branchRow.remove();
		}
	});

	calculateOpeningAdvGrandTotal();
}

function validateBranchBillDetails() {
	if($("#branchOpBalTable > tbody > tr").length > 0) {
		var lastTr = $("#branchOpBalTable > tbody > tr:last");
		var item = $.trim(lastTr.find(".branchItem").val());
		if(item == "") {
			swal("Incomplete detail!", "Please Select Branch First", "error");
			return false;
		}
//		var parentInnerTable = lastTr.find("table");
//		if(parentInnerTable.find("tbody tr").length > 0) {
//			var vendor = parentInnerTable.find("tbody tr:last").find('.masterList').val();
//			if(vendor == "") {
//				swal("Incomplete detail!", "Please Select Vendor", "error");
//				return false;
//			}
//		}
	}
	return true;
}

function validateBranchAdvDetails() {
	if($("#branchAdvBalTable > tbody > tr").length > 0) {
		var lastTr = $("#branchAdvBalTable > tbody > tr:last");
		var item = $.trim(lastTr.find(".branchItem").val());
		if(item == "") {
			swal("Incomplete detail!", "Please Select Branch First", "error");
			return false;
		}
//		var parentInnerTable = lastTr.find("table");
//		if(parentInnerTable.find("tbody tr").length > 0) {
//			var vendor = parentInnerTable.find("tbody tr:last").find('.masterList').val();
//			if(vendor == "") {
//				swal("Incomplete detail!", "Please Select Vendor", "error");
//				return false;
//			}
//		}
	}
	return true;
}

function saveBillwiseOpBalance() {
	$("#branchOpBalTable > tbody > tr").each(function(){
		var branch = $.trim($(this).find(".branchItem").val());
		var opBal = 0;
		$(this).find("table > tbody > tr").each(function(){
			var billAmt = $.trim($(this).find(".billAmount").val());
			var delType = $.trim($(this).find(".delType").val());
			if(billAmt !== "" && delType !== "DEL") {
				opBal += parseFloat(billAmt);
			}
		});
		var modalOwner = $("#branchOpeningBalModal").find("#modalOwner").val();
		if(parseFloat(opBal) > 0) {
			if(modalOwner == "VENDOR") {
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").val(opBal);
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").attr('readonly','readonly');
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").blur();
				if(!$("#vendorBranchList").find("#"+branch).find("input[type='checkbox']").prop('checked')) {
					$("#vendorBranchList").find("#"+branch).find("input[type='checkbox']").click();
				}

			}if(modalOwner == "CUSTOMER") {
				$("#customerBranchList").find("#"+branch).find(".openingBalance").val(opBal);
				$("#customerBranchList").find("#"+branch).find(".openingBalance").attr('readonly','readonly');
				$("#customerBranchList").find("#"+branch).find(".openingBalance").blur();
				if(!$("#customerBranchList").find("#"+branch).find("input[type='checkbox']").prop('checked')) {
					$("#customerBranchList").find("#"+branch).find("input[type='checkbox']").click();
				}
			}
		}else {
			if(modalOwner == "VENDOR") {
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").val("0.0");
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").removeAttr('readonly');
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").blur();
			}if(modalOwner == "CUSTOMER") {
				$("#customerBranchList").find("#"+branch).find(".openingBalance").val("0.0");
				$("#customerBranchList").find("#"+branch).find(".openingBalance").removeAttr('readonly');
				$("#customerBranchList").find("#"+branch).find(".openingBalance").blur();
			}
		}
	});
	$("#branchOpeningBalModal").modal('hide');
}

function saveBranchWiseAdvBalance() {
	var isValidData = true;
	$("#branchAdvBalTable > tbody > tr").each(function(){
		// var branch = $.trim($(this).find(".branchItem").val());
		var branchRow = $(this);
		var branch = $.trim(branchRow.find(".branchItem").val());
		var opBal = 0;
		branchRow.find("table > tbody > tr").each(function(){
			var receiptDate = $.trim($(this).find('.receiptDate').val());
			var receiptNumber = $.trim($(this).find('.receiptNumber').val());
			var typeOfSupply = $.trim($(this).find('.typeOfSupply').val());
			var placeOfSply = $.trim($(this).find('.advDestGstinCls').val());
			var item = $.trim($(this).find('.item').val());
			var advAmount = $.trim($(this).find('.advAmount').val());
			if(receiptDate == "" || receiptDate == undefined) {
				swal("Incomplete detail!", "Please Select Receipt Date", "error");
				isValidData = false;
			}
			if(receiptNumber == "" || receiptNumber == undefined) {
				swal("Incomplete detail!", "Please Fill Receipt Number", "error");
				isValidData = false;
			}
			if(typeOfSupply == "" || typeOfSupply == undefined) {
				swal("Incomplete detail!", "Please Select Type Of Supply", "error");
				isValidData = false;
			}
			if(placeOfSply == "" || placeOfSply == undefined) {
				swal("Incomplete detail!", "Please Select Place Of Supply", "error");
				isValidData = false;
			}
			if(item == "" || item == undefined) {
				swal("Incomplete detail!", "Please Select Item", "error");
				isValidData = false;
			}
			if(advAmount == "" || advAmount == undefined) {
				swal("Incomplete detail!", "Please Fill Advance Amount", "error");
				isValidData = false;
			}
			var delType = $.trim($(this).find(".delType").val());
			if(advAmount !== "" && delType !== "DEL") {
				opBal += parseFloat(advAmount);
			}
		});
		var modalOwner = $("#branchAdvOpeningBalModal").find("#modalOwner").val();
		if(parseFloat(opBal) > 0) {
			if(modalOwner == "VENDOR") {
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").val(opBal);
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").attr('readonly','readonly');
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").blur();
				if(!$("#vendorBranchList").find("#"+branch).find("input[type='checkbox']").prop('checked')) {
					$("#vendorBranchList").find("#"+branch).find("input[type='checkbox']").click();
				}

			}if(modalOwner == "CUSTOMER") {
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").val(opBal);
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").attr('readonly','readonly');
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").blur();
				if(!$("#customerBranchList").find("#"+branch).find("input[type='checkbox']").prop('checked')) {
					$("#customerBranchList").find("#"+branch).find("input[type='checkbox']").click();
				}
			}
		}else {
			if(modalOwner == "VENDOR") {
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").val("0.0");
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").removeAttr('readonly');
				$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").blur();
			}if(modalOwner == "CUSTOMER") {
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").val("0.0");
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").removeAttr('readonly');
				$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").blur();
			}
		}
	});
	if(isValidData){
		$("#branchAdvOpeningBalModal").modal('hide');
	}
}

function getBillWiseOpBalDetails(elem) {
	var jsonData = {};
	var tdsRowData = [];
	$("#branchOpBalTable > tbody > tr").each(function(){
		var rowData = {};
		var billData = [];
		var branch = $.trim($(this).find("#branchItem").val());
		$(this).find("table > tbody > tr").each(function(){
			var innerRowData = {};
			var id = $.trim($(this).find(".billId").val());
			var billDate = $.trim($(this).find(".billDate").val());
			var billNumber = $.trim($(this).find(".billNumber").val());
			var billAmount = $.trim($(this).find(".billAmount").val());
			var status = $.trim($(this).find(".delType").val());
			if(billDate !== "" && billNumber !== "" && billAmount !== "") {
				innerRowData.id = id;
				innerRowData.billDate = billDate;
				innerRowData.billNumber = billNumber;
				innerRowData.billAmount = billAmount;
				innerRowData.status = status;
				billData.push(innerRowData);
			}
		});
		rowData.branch = branch;
		rowData.billDetails = billData;
		tdsRowData.push(rowData);
	});

	return tdsRowData;

}

function getBranchWiseAdvBalDetails(elem) {
	var jsonData = {};
	var tdsRowData = [];
	$("#branchAdvBalTable > tbody > tr").each(function(){
		var rowData = {};
		var advData = [];
		var branch = $.trim($(this).find("#branchItem").val());
		var rowId = $(this).attr('id');
		$(this).find("table > tbody > tr").each(function(){
			var innerRowData = {};
			var id = $.trim($(this).find(".advId").val());
			var receiptDate = $.trim($(this).find(".receiptDate").val());
			var receiptNumber = $.trim($(this).find(".receiptNumber").val());
			var typeOfSupply = $.trim($(this).find(".typeOfSupply").val());
			var placeOfSply = $.trim($(this).find(".placeOfSply").val());
			var item = $.trim($(this).find(".item").val());
			var advAmount = $.trim($(this).find(".advAmount").val());
			var status = $.trim($(this).find(".delType").val());
			if(receiptDate !== "" && receiptNumber !== "" && advAmount !== "") {
				innerRowData.id = id;
				innerRowData.receiptDate = receiptDate;
				innerRowData.receiptNumber = receiptNumber;
				innerRowData.advAmount = advAmount;
				innerRowData.typeOfSupply = typeOfSupply ? typeOfSupply : "";
				innerRowData.placeOfSply = placeOfSply ? placeOfSply : "";
				innerRowData.item = item ? item : "";
				innerRowData.status = status;
				advData.push(innerRowData);
			}
		});
		rowData.branch = branch;
		rowData.advDetails = advData;
		rowData.rowId = rowId;
		tdsRowData.push(rowData);
	});
	return tdsRowData;
}

function setBillWiseOpBalDetails(billWiseOpBalData,modalOwner) {
	$("#branchOpBalTable > tbody").html("");
	$("#branchOpeningBalModal").find("#modalOwner").val(modalOwner);
	for(var i=0;i<billWiseOpBalData.length;i++) {
		var branch = billWiseOpBalData[i].branchId;
		var branchStatus = true;
		var innerBillDetails = billWiseOpBalData[i].billDetails;
		$("#addBranchOpBalButton").click();
		var selectedTr = $("#branchOpBalTable > tbody > tr:last");
		selectedTr.find("#branchItem").val(branch);
		var innerTable = selectedTr.find("#innerBranchBillTable");
		for(var j=0;j<innerBillDetails.length;j++) {
			var innerTr = innerTable.find('tbody > tr:last');
			innerTr.find(".billId").val(innerBillDetails[j].id);
			innerTr.find(".billDate").val(innerBillDetails[j].billDate);
			innerTr.find(".billNumber").val(innerBillDetails[j].billNo);
			if(innerBillDetails[j] == "" || innerBillDetails[j] == undefined){
				innerTr.find(".billAmount").val(innerBillDetails[j].billAmt);
			}
			else{
				innerTr.find(".billAmount").val(parseFloat(innerBillDetails[j].billAmt).toFixed(2));
			}
			innerTr.find(".isEditable").val(innerBillDetails[j].editStatus);
			innerTr.find(".delType").val("EDIT");

			if(innerBillDetails[j].editStatus == false){
				branchStatus = false;
				innerTr.find('input').attr('readonly','readonly');
				innerTr.find(".resetInnerBillOpRow").hide();
				innerTr.find(".removeInnerBillOpRow").hide();
			}
			if((j+1) < innerBillDetails.length) {
				innerTr.find(".addInnerBillOpRow").click();
			}
		}
		if(!branchStatus) {
			selectedTr.find("#branchItem").prop('disabled', 'disabled');
			if(modalOwner == "VENDOR") {
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").attr('readonly','readonly');
			}else {
				$("#customerBranchList").find("#"+branch).find(".openingBalance").attr('readonly','readonly');
			}

		}
	}
	calculateOpeningBalGrandTotal();
}

function setBranchWiseAdvBalDetails(branchWiseAdvBalData,modalOwner) {
	$("#branchAdvBalTable > tbody").html("");
	$("#branchAdvOpeningBalModal").find("#modalOwner").val(modalOwner);
	if(branchWiseAdvBalData && branchWiseAdvBalData.length > 0){
		uniqueIdCounter = 1
		for(var i=0;i<branchWiseAdvBalData.length;i++) {
			var branch = branchWiseAdvBalData[i].branchId;
			var branchStatus = true;
			var innerAdvDetails = branchWiseAdvBalData[i].advDetails;
			var grandTotal = 0;
			$("#addBranchAdvBalButton").click();
			var selectedTr = $("#branchAdvBalTable > tbody > tr:last");
			var trId = "advRow__" + uniqueIdCounter;
			selectedTr.attr("id", trId);
			selectedTr.find("#branchItem").val(branch);
			var innerTable = selectedTr.find("#innerBranchAdvTable");
			getCustomerPlaceOfSupply(selectedTr, "")
			getVendorCustomerItemList(selectedTr, "")
			for(var j=0;j<innerAdvDetails.length;j++) {
				var innerTr = innerTable.find('tbody > tr:last');
				innerTr.find(".advId").val(innerAdvDetails[j].id);
				innerTr.find(".receiptDate").val(innerAdvDetails[j].receiptDate);
				innerTr.find(".receiptDate").attr("disabled", true);
				innerTr.find(".receiptNumber").val(innerAdvDetails[j].receiptNumber);
				innerTr.find(".receiptNumber").attr('readonly','readonly');
				innerTr.find(".typeOfSupply").val(innerAdvDetails[j].typeOfSupply);
				innerTr.find(".typeOfSupply").attr("disabled", true);
				innerTr.find(".placeOfSply").val(innerAdvDetails[j].placeOfSply);
				innerTr.find(".placeOfSply").attr("disabled", true);
				innerTr.find(".item").val(innerAdvDetails[j].item);
				innerTr.find(".item").attr("disabled", true);
				if(innerAdvDetails[j] == "" || innerAdvDetails[j] == undefined){
					innerTr.find(".advAmount").val("");
				} else{
					grandTotal += innerAdvDetails[j].openingBalance
					innerTr.find(".advAmount").val(parseFloat(innerAdvDetails[j].openingBalance).toFixed(2));
				}
				innerTr.find(".receiptDate").attr('readonly','readonly');
				innerTr.find(".isEditable").val(false);
				innerTr.find(".delType").val("EDIT");
				branchStatus = false;
				innerTr.find('input').attr('readonly','readonly');
				innerTr.find(".resetInnerAdvOpRow").hide();
				innerTr.find(".removeInnerAdvOpRow").hide();
				if((j+1) < innerAdvDetails.length) {
					innerTr.find(".addInnerAdvOpRow").click();
				}
			}
			if(!branchStatus) {
				selectedTr.find("#branchItem").prop('disabled', 'disabled');
				selectedTr.find(".removeCheckBox").hide();
				if(modalOwner == "VENDOR") {
					$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").val(parseFloat(grandTotal).toFixed(2));
					$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").attr('readonly','readonly');
				}else {
					$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").val(parseFloat(grandTotal).toFixed(2));
					$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").attr('readonly','readonly');
				}
			}
			uniqueIdCounter++;
		}
	}
	calculateOpeningAdvGrandTotal()
}

var getCustomerPlaceOfSupply = function(elem, branchIdValue){
	var modalOwner = $("#branchAdvOpeningBalModal").find("#modalOwner").val();
	var custVendGstinList = [];
	var jsonData = {};
	var custVenLocation;
	var gstinputCustValue ;
	var gstinput2CustValue ;
	var customerState ;
	var selectedOption ;
	var selectedText;
	if(modalOwner == "CUSTOMER") {
		custVenLocation = $("#custlocation").val();
		gstinputCustValue = $("#gstinputCust").val();
		gstinput2CustValue = $("#gstinput2Cust").val();
		customerState = $("#customerState").val();
		selectedOption = $('#customerState option[value="' + customerState + '"]:selected');
		// Get the text content of the selected option
		selectedText = selectedOption.text();
		var isRegister = $('#custRegisteredOrUnReg').val();
		if(isRegister == 1){
			jsonData.gstin = gstinputCustValue+gstinput2CustValue;
		} else {
			jsonData.gstin = "0";
		}
		jsonData.gstin = gstinputCustValue+gstinput2CustValue;
		jsonData.customerDetailId = "";
		if(custVenLocation != null && custVenLocation != undefined && gstinputCustValue != null && gstinputCustValue != undefined && gstinput2CustValue != null && gstinput2CustValue != undefined){
			jsonData.custVenLocation= custVenLocation+'-'+gstinputCustValue+gstinput2CustValue+'-'+selectedText
			custVendGstinList.push(jsonData);
		}
		$("#customerGstinTbl tbody tr").each(function () {
			var rowData = {};
			// Extract the state value from the first column
			var state = $(this).find('td:eq(0)').text().trim();
			// Extract the GSTIN/UIN value from the second column
			var gstin = $(this).find('td:eq(1)').text().trim();
			// Extract the location value from the hidden input with id="shipCustCity"
			var location = $(this).find('td input#shipCustCity').val();
			var customerDetID = $(this).find('td input#customerDetID').val();
			rowData.gstin = gstin;
			rowData.customerDetailId = customerDetID ? customerDetID : "";
			rowData.custVenLocation = location+'-'+gstin+'-'+state;
			// Push the rowData object into the tableData array
			custVendGstinList.push(rowData);
		});
	} else {
		custVenLocation = $("#location").val();
		gstinputCustValue = $("#gstinputVend").val();
		gstinput2CustValue = $("#gstinput2Vend").val();
		customerState = $("#vendorState").val();
		selectedOption = $('#vendorState option[value="' + customerState + '"]:selected');
		// Get the text content of the selected option
		selectedText = selectedOption.text();
		var isRegister = $('#vendorRegisteredOrUnReg').val();
		if(isRegister == 1){
			jsonData.gstin = gstinputCustValue+gstinput2CustValue;
		} else {
			jsonData.gstin = "0";
		}
		jsonData.customerDetailId = "";
		if(custVenLocation != null && custVenLocation != undefined && gstinputCustValue != null && gstinputCustValue != undefined && gstinput2CustValue != null && gstinput2CustValue != undefined){
			jsonData.custVenLocation= custVenLocation+'-'+gstinputCustValue+gstinput2CustValue+'-'+selectedText
			custVendGstinList.push(jsonData);
		}
		$("#vendorGstinTbl tbody tr").each(function () {
			var rowData = {};
			// Extract the state value from the first column
			var state = $(this).find('td:eq(0)').text().trim();
			// Extract the GSTIN/UIN value from the second column
			var gstin = $(this).find('td:eq(1)').text().trim();
			// Extract the location value from the hidden input with id="shipCustCity"
			var location = $(this).find('td input#shipVendCity').val();
			var vendorDetID = $(this).find('td input#vendorDetID').val();
			rowData.gstin = gstin;
			rowData.customerDetailId = vendorDetID ? vendorDetID : "";
			rowData.custVenLocation = location+'-'+gstin+'-'+state;
			// Push the rowData object into the tableData array
			custVendGstinList.push(rowData);
		});
	}
	var trElement = "";
	if(branchIdValue == ""){
		var parentTr = $(elem).closest('tr').attr('id');
		branchId=$("#"+parentTr+" select[class='branchItem'] option:selected").val();
		trElement = $(elem).closest('tr');
	} else {
		branchId = branchIdValue
		trElement = $("#" + elem);
	}
	var selectElement = trElement.find("select.placeOfSply.advDestGstinCls");
	selectElement.children().remove();
	selectElement.append('<option value=""> Please Select </option>');
	var gstinListTemp = "";
	for(var i=0;i<custVendGstinList.length;i++){
		gstinListTemp += ('<option  value="'+custVendGstinList[i].gstin+'" id="'+custVendGstinList[i].customerDetailId+'">'+custVendGstinList[i].custVenLocation+'</option>');
	}
	selectElement.append(gstinListTemp);
	// Single User
	if (custVendGstinList.length == 1) {
		selectElement.find("option:last").attr("selected", "selected");
		selectElement.trigger("change");
	}
}

var getVendorCustomerItemList = function(elem, branchIdValue){
		var selectedOptions = [];
		var modalOwner = $("#branchAdvOpeningBalModal").find("#modalOwner").val();
		if(modalOwner == "CUSTOMER") {
			$('#customerItemList input[type="checkbox"]:checked').each(function() {
				// Get the name associated with the checkbox
				var itemName = $(this).closest('li').find('.itemLabelClass').text();
				// Get the value of the checkbox
				var itemValue = $(this).val();
				// Create an option element and push it to the array
				var optionElement = '<option value="' + itemValue + '">' + itemName + '</option>';
				selectedOptions.push(optionElement);
			});
		} else {
			$('#vendorItemList input[type="checkbox"]:checked').each(function() {
				// Get the name associated with the checkbox
				var itemName = $(this).closest('li').find('.itemLabelClass').text();
				// Get the value of the checkbox
				var itemValue = $(this).val();
				// Create an option element and push it to the array
				var optionElement = '<option value="' + itemValue + '">' + itemName + '</option>';
				selectedOptions.push(optionElement);
			});
		}
		if(branchIdValue == "") {
			var parentTr = $(elem).closest('tr').attr('id');
			branchId=$("#"+parentTr+" select[class='branchItem'] option:selected").val();
			trElement = $(elem).closest('tr');
		} else {
			branchId = branchIdValue;
			trElement = $("#" + elem);
		}
		var selectElement = trElement.find("select.item");
		selectElement.children().remove();
        selectElement.append('<option value=""> Please Select </option>');
		selectElement.append(selectedOptions);
}
//****************** END Bill Wise Opening Balance Table*****************************
// *************** START Bill Wise Opening Balance Inner TableTABLE DETAILS **********

function billBranchChanged(comp) {
	var branch = $(comp).val();
	var modalOwner = $("#branchOpeningBalModal").find("#modalOwner").val();
	var count = 0;
	$("#branchOpBalTable > tbody > tr").each(function(){
		var branchSelected = $.trim($(this).find(".branchItem").val());
		if(branch != "" &&  branchSelected != "") {
			if(branch == branchSelected) {
				count++;
			}
		}
	});
	if(branch !== "") {
		if(count == 1){
			if(modalOwner == "VENDOR") {
				var value = $("#vendorBranchList").find("#"+branch).find(".openingBalance").val();
				if(value !== "") {
					if(parseFloat(value) > 0) {
						swal("Invalid!","Please Clear Opening Balance for Selected Branch","error");
						$(comp).val("");
						return false;
					}

				}
			}if(modalOwner == "CUSTOMER") {
				var value = $("#customerBranchList").find("#"+branch).find(".openingBalance").val();
				$("#customerBranchList").find("#"+branch).find(".openingBalance").blur();
				if(value !== "") {
					if(parseFloat(value) > 0) {
						swal("Invalid!","Please Clear Opening Balance for Selected Branch","error");
						$(comp).val("");
						return false;
					}
				}
			}
			$(comp).closest('tr').find("table > tbody > tr:last").find(".addInnerBillOpRow").trigger("click");
			var count = $(comp).closest('tr').find("table > tbody > tr").length;
			if(count > 1){
				for (var i = 0; i < count-1; i++) {
					$(comp).closest('tr').find("table > tbody  tr:First").remove();
				}
			}
		}else {
			$(comp).val("");
			swal("Invalid!","Branch Already Selected","error");
			return false;
		}
	}
}
function advBranchChanged(comp) {
	var branch = $(comp).val();
	var modalOwner = $("#branchAdvOpeningBalModal").find("#modalOwner").val();
	var count = 0;
	$("#branchAdvBalTable > tbody > tr").each(function(){
		var branchSelected = $.trim($(this).find(".branchItem").val());
		if(branch != "" &&  branchSelected != "") {
			if(branch == branchSelected) {
				count++;
			}
		}
	});
	if(branch !== "") {
		if(count == 1){
			if(modalOwner == "VENDOR") {
				var value = $("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").val();
				if(value !== "") {
					if(parseFloat(value) > 0) {
						swal("Invalid!","Please Clear Opening Advance Balance for Selected Branch","error");
						$(comp).val("");
						return false;
					}

				}
			}if(modalOwner == "CUSTOMER") {
				var value = $("#customerBranchList").find("#"+branch).find(".openingBalanceAP").val();
				$("#customerBranchList").find("#"+branch).find(".openingBalance").blur();
				if(value !== "") {
					if(parseFloat(value) > 0) {
						swal("Invalid!","Please Clear Opening Advance Balance for Selected Branch","error");
						$(comp).val("");
						return false;
					}
				}
			}
			$(comp).closest('tr').find("table > tbody > tr:last").find(".addInnerAdvOpRow").trigger("click");
			var count = $(comp).closest('tr').find("table > tbody > tr").length;
			if(count > 1){
				for (var i = 0; i < count-1; i++) {
					$(comp).closest('tr').find("table > tbody  tr:First").remove();
				}
			}
		}else {
			$(comp).val("");
			swal("Invalid!","Branch Already Selected","error");
			return false;
		}
	}
}

function clearBranchBillDetails() {
	$("#branchOpBalTable > tbody > tr").each(function(){
		$(this).find('.removeCheckBox').prop("checked",true)
	});

	$("#branchOpBalTable > tbody > tr").each(function(){
		var branch = $(this).find(".branchItem").val();
		if(branch != "") {
			var modalOwner = $("#branchOpeningBalModal").find("#modalOwner").val();
			if(modalOwner == "VENDOR") {
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").val("0.0");
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").removeAttr('readonly');
				$("#vendorBranchList").find("#"+branch).find(".openingBalance").blur();
			}if(modalOwner == "CUSTOMER") {
				$("#customerBranchList").find("#"+branch).find(".openingBalance").val("0.0");
				$("#customerBranchList").find("#"+branch).find(".openingBalance").removeAttr('readonly');
				$("#customerBranchList").find("#"+branch).find(".openingBalance").blur();
			}
		}
        $(this).closest('tr').remove();
    });

	calculateOpeningBalGrandTotal();
}
function clearBranchAdvDetails() {
	$("#branchAdvBalTable > tbody > tr").each(function(){
		var advIdInput = $(this).find(".advId");
		if (advIdInput.val() === "") {
			// Remove the current row and its parent row
			$(this).find('.removeCheckBox').prop("checked",true)
		}
		
	});

	$("#branchAdvBalTable > tbody > tr").each(function(){
		var branch = $(this).find(".branchItem").val();
		var advIdInput = $(this).find(".advId");
		if (advIdInput.val() === "") {
			if(branch != "") {
				var modalOwner = $("#branchAdvOpeningBalModal").find("#modalOwner").val();
				if(modalOwner == "VENDOR") {
					$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").val("0.0");
					$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").removeAttr('readonly');
					$("#vendorBranchList").find("#"+branch).find(".openingBalanceAP").blur();
				}if(modalOwner == "CUSTOMER") {
					$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").val("0.0");
					$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").removeAttr('readonly');
					$("#customerBranchList").find("#"+branch).find(".openingBalanceAP").blur();
				}
			}
			$(this).closest('tr').remove();
		}
    });
	calculateOpeningAdvGrandTotal();
}

var addInnerBillOpRow = function(comp) {
	var parentInnerTable = $(comp).closest("table");
	var mainRow = parentInnerTable.closest("tr");
	var branchLedger = mainRow.find(".branchItem").val();
	if(branchLedger == "") {
		swal("Incomplete detail!", "Please Select Branch First", "error");
		return false;
	}
	if(parentInnerTable.find("tbody tr").length > 1) {
		var billDate = parentInnerTable.find("tbody tr:last").find('.billDate').val();
		var billNo = parentInnerTable.find("tbody tr:last").find('.billNumber').val();
		var billAmt = parentInnerTable.find("tbody tr:last").find('.billAmount').val();
		if(billDate == "") {
			swal("Incomplete detail!", "Please Select Bill Date", "error");
			return false;
		}
		if(billNo == "") {
			swal("Incomplete detail!", "Please Fill Bill Number", "error");
			return false;
		}
		if(billAmt == "") {
			swal("Incomplete detail!", "Please Fill Bill Total Amount", "error");
			return false;
		}
	}
	parentInnerTable.find("tbody").append(innerBillOPRowTemplate());
	parentInnerTable.find("tbody tr:last").find('.billDate').datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
	$(comp).hide();

}

var addInnerAdvOpRow = function(elem) {
	var parentInnerTable = $(elem).closest("table");
	var mainRow = parentInnerTable.closest("tr");
	var branchLedger = mainRow.find(".branchItem").val();
	var trId = mainRow.attr("id");
	if(branchLedger == "") {
		swal("Incomplete detail!", "Please Select Branch First", "error");
		return false;
	}
	if(parentInnerTable.find("tbody tr").length > 1) {
		var receiptDate = parentInnerTable.find("tbody tr:last").find('.receiptDate').val();
		var receiptNumber = parentInnerTable.find("tbody tr:last").find('.receiptNumber').val();
		var typeOfSupply = parentInnerTable.find("tbody tr:last").find('.typeOfSupply').val();
		var placeOfSply = parentInnerTable.find("tbody tr:last").find('.placeOfSply').val();
		var item = parentInnerTable.find("tbody tr:last").find('.item').val();
		var advAmount = parentInnerTable.find("tbody tr:last").find('.advAmount').val();

		if(receiptDate == "" || receiptDate == undefined) {
			swal("Incomplete detail!", "Please Select Receipt Date", "error");
			return false;
		}
		if(receiptNumber == "" || receiptNumber == undefined) {
			swal("Incomplete detail!", "Please Fill Receipt Number", "error");
			return false;
		}
		if(typeOfSupply == "" || typeOfSupply == undefined) {
			swal("Incomplete detail!", "Please Select Type Of Supply", "error");
			return false;
		}
		if(placeOfSply == "" || placeOfSply == undefined) {
			swal("Incomplete detail!", "Please Select Place Of Supply", "error");
			return false;
		}
		if(item == "" || item == undefined) {
			swal("Incomplete detail!", "Please Select Item", "error");
			return false;
		}
		if(advAmount == "" || advAmount == undefined) {
			swal("Incomplete detail!", "Please Fill Advance Amount", "error");
			return false;
		}
	}
	parentInnerTable.find("tbody").append(innerAdvOPRowTemplate('advRow_'+uniqueIdCounter));
	getCustomerPlaceOfSupply('advRow_'+uniqueIdCounter, branchLedger); 
	getVendorCustomerItemList('advRow_'+uniqueIdCounter, branchLedger);
	parentInnerTable.find("tbody tr:last").find('.receiptDate').datepicker({
		changeMonth : true,
		changeYear : true,
		dateFormat:  'MM d,yy',
		yearRange: ''+new Date().getFullYear()-100+':'+maximumYear+'',
		onSelect: function(x,y){
	        $(this).focus();
	    }
	});
	$(".receiptDate").datepicker("option", "maxDate", new Date());
	uniqueIdCounter++
	$(elem).hide();

}

function innerBillOPRowTemplate() {

	var innerTdsRow = [];
	var i=0;
	innerTdsRow[i++] = '<tr><td><input class="billDate" type="text" name="billDate" placeholder="Date"><input class="billId" type="hidden" /><input class="isEditable" type="hidden" value="true"/><input class="delType" type="hidden" value="ADD"/></td>';
	innerTdsRow[i++] = '<td><input class="billNumber" type="text" name="billNumber" placeholder="Bill Number" id="billNumber" onkeyup=""/></td>';
	innerTdsRow[i++] = '<td><input class="billAmount"  type="text" name="billAmount" placeholder="Bill Amount" id="billAmount" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateOpeningBalGrandTotal();"/></td>';
	innerTdsRow[i++] = '<td><button class="addInnerBillOpRow" title="add Row" onclick="addInnerBillOpRow(this);"><i class="fa fa-plus-circle fa-lg"></i></button>';
	innerTdsRow[i++] = '<button class="resetInnerBillOpRow" title="Reset Row" onclick="resetInnerBillOpRow(this);"><i class="fa fa-refresh fa-lg"></i></button>';
	innerTdsRow[i++] = '<button class="removeInnerBillOpRow" title="Remove Row" onclick="removeInnerBillOpRow(this);"><i class="fa fa-minus-circle fa-lg"></i></button><td></tr>';
	return innerTdsRow.join(" ");
}

function innerAdvOPRowTemplate(rowId) {
    var innerTdsRow = [];
    var i = 0;

    innerTdsRow[i++] = '<tr id='+ rowId +'>';
    innerTdsRow[i++] = '<td style="width: 140px;"><input class="receiptDate" type="text" name="receiptDate" placeholder="Date"><input class="advId" type="hidden" /><input class="isEditable" type="hidden" value="true"/><input class="delType" type="hidden" value="ADD"/></td>';
    innerTdsRow[i++] = '<td style="width: 140px;"><input class="receiptNumber" type="text" name="receiptNumber" placeholder="Receipt Number" id="receiptNumber" onkeyup=""/></td>';
    innerTdsRow[i++] = '<td style="width: 140px;">';
    innerTdsRow[i++] = '<select class="typeOfSupply" id="typeOfSupply"><option value="">--Please Select--</option><option value="1">Regular Supply</option><option value="2">Supply applicable for Reverse Charge</option><option value="3">This is an Export Supply</option><option value="4">This is supply to SEZ Unit or SEZ Developer</option><option value="5">This is deemed Export Supply</option><option value="6">Supply made through E-commerce Operator</option><option value="7">Bill of Supply</option></select>';
    innerTdsRow[i++] = '</td>';
    innerTdsRow[i++] = '<td style="width: 140px;">';
    innerTdsRow[i++] = '<select class="placeOfSply advDestGstinCls" name="advPlaceOfSply" id="advPlaceOfSply"><option value="">--Please Select--</option></select>';
    innerTdsRow[i++] = '</td>';
    innerTdsRow[i++] = '<td style="width: 140px;">';
    innerTdsRow[i++] = '<select class="item" name="item" id="item"><option value="">--Please Select--</option></select>';
    innerTdsRow[i++] = '</td style="width: 140px;">';
    innerTdsRow[i++] = '<td><input class="advAmount" type="text" name="advAmount" placeholder="Advance Amount" id="advAmount" onkeypress="return onlyDotsAndNumbers(event);" onkeyup="calculateOpeningAdvGrandTotal();"/></td>';

    innerTdsRow[i++] = '<td style="width: 115px;display: flex;justify-content: space-between;padding-top: 5px;border: none;">';
    innerTdsRow[i++] = '<button class="addInnerAdvOpRow" title="add Row" onclick="addInnerAdvOpRow(this);"><i class="fa fa-plus-circle fa-lg"></i></button>';addInnerAdvOpRow
    innerTdsRow[i++] = '<button class="resetInnerAdvOpRow" title="Reset Row" onclick="resetInnerAdvOpRow(this);"><i class="fa fa-refresh fa-lg"></i></button>';
    innerTdsRow[i++] = '<button class="removeInnerAdvOpRow" title="Remove Row" onclick="removeInnerAdvOpRow(this);"><i class="fa fa-minus-circle fa-lg"></i></button>';
    innerTdsRow[i++] = '</td></tr>';

    return innerTdsRow.join(" ");
}

var resetInnerBillOpRow = function(comp) {
	var parentTR = $(comp).closest("tr");
	parentTR.find(".billDate").val("");
	parentTR.find(".billAmount").val("");
	parentTR.find(".billNumber").val("");
}

var removeInnerBillOpRow = function(comp) {
	var parentInnerTable = $(comp).closest("table");
	var count = parentInnerTable.find("tbody tr").length;
	if(count > 1 ) {
		if($(comp).closest("tr").find(".delType").val() == "EDIT") {
			$(comp).closest("tr").find(".delType").val("DEL");
			$(comp).closest("tr").hide();
		}else {
			$(comp).closest("tr").remove();
		}
	}
	parentInnerTable.find("tbody tr:last").find("button[class='addInnerBillOpRow']").show();
	calculateOpeningBalGrandTotal();
}

var resetInnerAdvOpRow = function(comp) {
	var parentTR = $(comp).closest("tr");
	parentTR.find(".receiptDate").val("");
	parentTR.find(".receiptNumber").val("");
	parentTR.find(".typeOfSupply").val("");
	parentTR.find(".placeOfSply").val("");
	parentTR.find(".item").val("");
	parentTR.find(".advAmount").val("");
}

var removeInnerAdvOpRow = function(comp) {
	var parentInnerTable = $(comp).closest("table");
	var count = parentInnerTable.find("tbody tr").length;
	if(count > 1 ) {
		if($(comp).closest("tr").find(".delType").val() == "EDIT") {
			$(comp).closest("tr").find(".delType").val("DEL");
			$(comp).closest("tr").hide();
		}else {
			$(comp).closest("tr").remove();
		}
	}
	parentInnerTable.find("tbody tr:last").find("button[class='addInnerAdvOpRow']").show();
	calculateOpeningAdvGrandTotal();
}

//************** END Bill Wise Opening Balance Inner Table DETAILS *********
$(document).ready(function(){
	// $('#vendorRegisteredOrUnReg,#custRegisteredOrUnReg').change(function(){
	// 	var gstYesorNo = $(this).val();
	// 	var parentTr=$(this).closest('tr').attr('id');
	// 	if(gstYesorNo == 0){
	// 		$("#"+parentTr+" input[id='gstinputCust']").val("");
	// 		$("#"+parentTr+" input[id='gstinput2Cust']").val("");
	// 		$("#"+parentTr+" input[id='gstinputCust']").attr("disabled", true);
	// 		$("#"+parentTr+" input[id='gstinput2Cust']").attr("disabled", true);
	// 	}
	// 	else if(gstYesorNo == 1){
	// 		$("#"+parentTr+" input[id='gstinputCust']").attr("disabled", false);
	// 		$("#"+parentTr+" input[id='gstinput2Cust']").attr("disabled", false);
	// 	}
	// });
	$('#vendorRegisteredOrUnReg').change(function(){
		var gstYesorNo = $(this).val();
		var parentTr=$(this).closest('tr').attr('id');
		if(gstYesorNo == 0){
			$("#"+parentTr+" input[id='gstinputVend']").val("");
			$("#"+parentTr+" input[id='gstinput2Vend']").val("");
			$("#"+parentTr+" input[id='gstinputVend']").attr("disabled", true);
			$("#"+parentTr+" input[id='gstinput2Vend']").attr("disabled", true);
		}
		else if(gstYesorNo == 1){
			$("#"+parentTr+" input[id='gstinputVend']").attr("disabled", false);
			$("#"+parentTr+" input[id='gstinput2Vend']").attr("disabled", false);
		}
	});
	$('#custRegisteredOrUnReg').change(function(){
		var gstYesorNo = $(this).val();
		var parentTr=$(this).closest('tr').attr('id');
		if(gstYesorNo == 0){
			$("#"+parentTr+" input[id='gstinputCust']").val("");
			$("#"+parentTr+" input[id='gstinput2Cust']").val("");
			$("#"+parentTr+" input[id='gstinputCust']").attr("disabled", true);
			$("#"+parentTr+" input[id='gstinput2Cust']").attr("disabled", true);
		}
		else if(gstYesorNo == 1){
			$("#"+parentTr+" input[id='gstinputCust']").attr("disabled", false);
			$("#"+parentTr+" input[id='gstinput2Cust']").attr("disabled", false);
		}
	});

});
// $(document).ready(function(){
// 	$('#custRegisteredOrUnReg').change(function(){
// 		var gstYesorNo = $(this).val();
// 		var parentTr=$(this).closest('tr').attr('id');
// 		if(gstYesorNo == 0){
// 			$("#"+parentTr+" input[id='gstinput2Cust']").val("");
// 			$("#"+parentTr+" input[id='gstinputCust']").val("");
// 			$("#"+parentTr+" input[id='gstinputCust']").attr("disabled", true);
// 			$("#"+parentTr+" input[id='gstinput2Cust']").attr("disabled", true);
// 		}
// 		else if(gstYesorNo == 1){
// 			$("#"+parentTr+" input[id='gstinputCust']").attr("disabled", false);
// 			$("#"+parentTr+" input[id='gstinput2Cust']").attr("disabled", false);
// 		}
// 	});
// });
function onRcmTaxChange(comp) {
	var parent = $(comp).parents("li");
	var taxRate = parent.find(".rcmRateVendItem").val();
	var rcmtaxrate = parent.find(".rcmRateVendItem").attr("rcmtaxrate");
	var cessRate = parent.find(".cesRateVendItem").val();
	var rcmcessrate = parent.find(".cesRateVendItem").attr("rcmcessrate");

	 if(taxRate == "" && cessRate == "" ) {
		$(comp).closest("li").find(".VendRcmApplicableDate").attr("disabled", false);
	 }else {
	 	if((taxRate != "" && rcmtaxrate == taxRate) && (cessRate != "" && rcmcessrate == cessRate)) {
	 		$(comp).closest("li").find(".VendRcmApplicableDate").attr("disabled", true);
	 	}else{
	 		$(comp).closest("li").find(".VendRcmApplicableDate").attr("disabled", false);
	 	}
	 }
 }


function setVendorTdsDetailsHidden(tdsItemDetails) {
	for(var i=0;i<tdsItemDetails.length;i++) {
		var specificId = tdsItemDetails[i].specificId;
			$("#vendTdsItemList li[name='"+specificId+"']").find(".tdsWhType").val(tdsItemDetails[i].tdsWhType);
			$("#vendTdsItemList li[name='"+specificId+"']").find(".tdsTaxRate").val(tdsItemDetails[i].tdsTaxRate);
			$("#vendTdsItemList li[name='"+specificId+"']").find(".tdsTaxTransLimit").val(tdsItemDetails[i].tdsTaxTransLimit);
			$("#vendTdsItemList li[name='"+specificId+"']").find(".tdsTaxOverallLimitApply").val(tdsItemDetails[i].tdsTaxOverallLimitApply);
			$("#vendTdsItemList li[name='"+specificId+"']").find(".overallLimit").val(tdsItemDetails[i].overallLimit);
			$("#vendTdsItemList li[name='"+specificId+"']").find(".tdsFromDate").val(tdsItemDetails[i].tdsFromDate);
			$("#vendTdsItemList li[name='"+specificId+"']").find(".tdsToDate").val(tdsItemDetails[i].tdsToDate);
	}
}

$(document).ready(function() {
	$(".uploadVendor").click(function(){
		var chatofacturl=$("#uploadvendor").val();
		if(chatofacturl==""){
			swal("Invalid details!","please upload your company vendors","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "xls" && ext != "xlsx"){
			swal("Error!","Only Excel files are allowed for vendor upload","error");
			$("#uploadvendor").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#myVendorForm');
		var data = new FormData();
		jQuery.each($('#uploadvendor')[0].files, function(i, file) {
			data.append('file-'+i, file);
		});
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		$.ajax({
			method: "POST",
			url: form.attr('action'),
			data: data,
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			cache: false,
			contentType: false,
			processData: false,
			success: function(data) {
				$("#uploadvendor").val("");
				if(typeof data.message !=='undefined' && data.message != ""){
					swal("Error!", data.message, "error");
					return false;
				}else {
					let msgTmp = "";
					let timeOutRef = 1000;
					if(data.itemsNotFound.length > 0) {
						msgTmp = ("These items not found: " + data.itemsNotFound);
						timeOutRef = 30000;
					}
					if(data.branchNotFound.length > 0) {
						msgTmp += ("These Branches are not found: " + data.branchNotFound);
						timeOutRef = 30000;
					}
					$(".errorMessage").text(msgTmp);
					swal("Error!","Total records in xls: " + data.totalRowsInXls + ", Inserted in system: " + data.totalRowsInserted,"error");
					setTimeout(function() {
						location.reload(true);
					}, timeOutRef);
				}
				$.unblockUI();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});


$(document).ready(function() {
	$(".uploadCustomer").click(function(){
		var chatofacturl=$("#uploadcustomer").val();
		if(chatofacturl==""){
			swal("Incomplete details!","please upload your company customers","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "xls" && ext != "xlsx"){
			swal("Error!","Only Excel files are allowed for customers upload","error");
			$("#uploadcustomer").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#myCustomerForm');
		var data = new FormData();
		jQuery.each($('#uploadcustomer')[0].files, function(i, file) {
			data.append('file-'+i, file);
		});
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		$.ajax({
			method: "POST",
			url: form.attr('action'),
			data: data,
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			cache: false,
			contentType: false,
			processData: false,
			success: function(data) {
				$("#uploadcustomer").val("");
				if(typeof data.message !=='undefined' && data.message != ""){
					swal("Error!", data.message, "error");
					return false;
				}else {
					let msgTmp = "";
					let timeOutRef = 1000;
					if(data.itemsNotFound.length > 0) {
						msgTmp = ("These items not found: " + data.itemsNotFound);
						timeOutRef = 30000;
					}
					if(data.branchNotFound.length > 0) {
						msgTmp += ("These Branches are not found: " + data.branchNotFound);
						timeOutRef = 30000;
					}
					$(".errorMessage").text(msgTmp);
					swal("Error!","Total records in xls: " + data.totalRowsInXls + ", Inserted in system: " + data.totalRowsInserted,"error");
					setTimeout(function() {
						location.reload(true);
					}, timeOutRef);
				}
				$.unblockUI();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

