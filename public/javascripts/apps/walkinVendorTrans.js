/************************************************************************************************
This file contains functions for Buy trans walk-in vendor
Created by Sunil K. Namdev on 03-10-2017.
*************************************************************************************************/

$(document).ready(function(){
	var cache = {};
	$('input[name="unAvailableVendor"]').autocomplete({
        delay: 100,
        autoFocus: true,
        cacheLength: 3,
        scroll: true,
        highlight: false,
		minLength: 3,
		source: function( request, response ) {
	        var term = request.term;
	        if ( term in cache ) {
				response(cache[term]);
				return;
	        }
	        var selectedItemsArray = [];
	        var jsonData = {};
			$.ajax({
				dataType: "json",
				type: 'text',
				data: JSON.stringify(jsonData),
				method: "GET",
				url: '/vendor/walkinvendor/'+term,
				headers:{
					"X-AUTH-TOKEN": window.authToken
			  	},
			  	contentType:'application/json',
				success: function(data) {
					cache[term] = data.cutomerList;
					response(data.cutomerList);
					$('input[name="unAvailableVendor"]').removeClass('ui-autocomplete-loading');
				},
				error: function(data) {
					$('input[name="unAvailableVendor"]').removeClass('ui-autocomplete-loading');  
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
	    	$("#"+parentTr+" select[class='walkinVendorType'] option:first").prop("selected","selected");
	    	$("#"+parentTr+" input[name='unAvailableVendor']").val(ui.item.label); 
	    	$("#"+parentTr+" input[name='txnWalkinPlcSplyText']").val(ui.item.value);
	    	$("#"+parentTr+" input[name='txnWalkinPlcSplyHid']").val(ui.item.idx); 
	    	showWalkinVendorDetail(this);
	    	$('input[name="unAvailableVendor"]').removeClass('ui-autocomplete-loading'); 
	    },
	    change: function( event, ui ) {
	    	showWalkinVendorDetail(this);
	    }
    });
});

var clearWalkinVendorDetail = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr+" input[name='txnWalkinPlcSplyText']").val('');
	$("#"+parentTr+" input[name='txnWalkinPlcSplyHid']").val(''); 
	$("#"+parentTr+" select[class='walkinCustType'] option:first").prop("selected","selected");
}

var showWalkinVendorDetail = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr +" input[name='txnWalkinPlcSplyText']").show();
	fetchBuyItemsForVisitingVendor(elem); 
	enableWalkinVendor(elem);
}

var showWalkinVendorModal = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var walkincustomer = $("#"+parentTr +" input[name='unAvailableVendor']").val();
	if(walkincustomer == ""){
		swal("walk-in vendor error!", "Please provide walk-in vendor name.", "error");
		$(elem).val('');
	}
	var walkinCustPlcSplyHid =$("#"+parentTr +" input[name='txnWalkinPlcSplyHid']").val();
	var customerType= $(elem).val();
	if(customerType == "1" || customerType == "2"){
		if(walkinCustPlcSplyHid == ""){
			$("#staticWalkinVendorModal").attr('data-toggle', 'modal');
			$("#staticWalkinVendorModal").modal('show');
			$("#staticWalkinVendorModal input[class='transTableTrIdCls']").val(parentTr);
			clearWalkinVendorModal(this);
		}
		$("#"+parentTr +" select[name='txnWalkinPlcSplySelect']").hide();
		$("#"+parentTr +" input[name='txnWalkinPlcSplyText']").show();
	}else if(customerType == "3" || customerType == "4"){
		$("#"+parentTr +" select[name='txnWalkinPlcSplySelect']").hide();
		$("#"+parentTr +" input[name='txnWalkinPlcSplyText']").show();
		var placeOfSplyText = $("#"+parentTr+" select[class='txnBranches']").children(":selected").text();
		var placeOfSplyValue = $("#"+parentTr+" select[class='txnBranches']").children(":selected").attr("id");
		$("#"+parentTr +" input[name='txnWalkinPlcSplyText']").val(placeOfSplyText+"-"+placeOfSplyValue);
		$("#"+parentTr +" input[name='txnWalkinPlcSplyHid']").val(placeOfSplyValue);
	}else if(customerType == "5" || customerType == "6"){
		$("#"+parentTr +" input[name='txnWalkinPlcSplyText']").hide();
		$("#"+parentTr +" select[name='txnWalkinPlcSplySelect']").show();
		$("#"+parentTr +" input[name='txnWalkinPlcSplyHid']").val("");
		$("#"+parentTr +" input[name='txnWalkinPlcSplyText']").val('');
	}else if(customerType == ""){
		$("#"+parentTr +" select[class='walkinCustType']").find('option:first').prop("selected","selected");
		$("#"+parentTr +" input[name='txnWalkinPlcSplyHid']").val("");
		$("#"+parentTr +" input[name='unAvailableCustomer']").val("");
		$("#"+parentTr +" input[name='txnWalkinPlcSplyText']").val('');
		$("#"+parentTr +" select[name='txnWalkinPlcSplySelect']").hide();
	}
	resetMultiItemsTableLength(parentTr);
	resetMultiItemsTableFieldsData(elem);
}


var clearRegisterVendorData = function(elem){
	var parentTr = $(elem).closest('div').attr('id');
	$("#"+parentTr +" table[class='multipleItemsTable'] tbody tr:last span[class='select2-selection__clear']").trigger("mousedown");
	$("#"+parentTr +" select[class='placeOfSply txnDestGstinCls']").find('option:first').prop("selected",true);
    $("#"+parentTr +" .masterList").find('option:first').prop("selected",true);
}

var clearWalkinVendorModal = function(elem){
	$("#staticWalkinVendorModal input[name='gstinPart1']").val('');
	$("#staticWalkinVendorModal input[name='gstinPart2']").val('');
	$("#staticWalkinVendorModal textarea[name='addressModal']").val('');
	$("#staticWalkinVendorModal select[name='countryModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinVendorModal select[name='stateModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinVendorModal input[name='locationModal']").val('');
	$("#staticWalkinVendorModal select[name='phoneCountryCodeModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinVendorModal input[name='phone1Modal']").val('');
	$("#staticWalkinVendorModal input[name='phone2Modal']").val('');
	$("#staticWalkinVendorModal input[name='phone3Modal']").val('');
	$("#staticWalkinVendorModal input[id='isGstinAddedInTransHid']").val('0');
}


$(document).ready(function(){
    $("#addWalkinVendModalBtn").click(function(){
		var gstinPart1 = $("#staticWalkinVendorModal input[name='gstinPart1']").val();
		var gstinPart2 = $("#staticWalkinVendorModal input[name='gstinPart2']").val();
		var gstinCode = gstinPart1 + gstinPart2;
		if((gstinCode.length > 1) && (gstinCode.length < 15 || gstinCode.length > 15)){
	        swal("Invalid GSTIN!","Invalid GSTIN! Please provide correct GSTIN","error");
	        return false;
	    }
		var addressModal = $("#staticWalkinVendorModal textarea[name='addressModal']").val();
		var countryModal = $("#staticWalkinVendorModal select[name='countryModal'] option:selected").val();
		var stateModal = $("#staticWalkinVendorModal select[name='stateModal'] option:selected").text();
		var stateCodeModal = $("#staticWalkinVendorModal select[name='stateModal'] option:selected").val();
		var locationModal = $("#staticWalkinVendorModal input[name='locationModal']").val();
		var phoneCountryCodeModal = $("#staticWalkinVendorModal select[name='phoneCountryCodeModal'] option:selected").val();
		var phone1Modal = $("#staticWalkinVendorModal input[name='phone1Modal']").val();
		var phone2Modal = $("#staticWalkinVendorModal input[name='phone2Modal']").val();
		var phone3Modal = $("#staticWalkinVendorModal input[name='phone3Modal']").val();
		
		if(addressModal == ""){
			swal("Invalid Data!","Please provide valid address.","error");
			return false;
		}
		if(stateModal == ""){
			swal("Invalid Data!","Please provide valid state.","error");
			return false;
		}else if(gstinPart1.length > 1 && stateCodeModal != gstinPart1){
	    	swal("Invalid Data!","GSTIN and State does not match, please provide valid state/GSTIN.","error");
	    	return false;
	    }
		if(locationModal == ""){
			swal("Invalid Data!","Please provide valid location.","error");
			return false;
		}
		$("#staticWalkinVendorModal input[id='vendorAddressHid']").val(addressModal);	
		var parentTr = $("#staticWalkinVendorModal input[class='transTableTrIdCls']").val();
		var placeOfSplyText = locationModal+"-"+gstinCode+"-"+stateModal;
		$("#"+parentTr +" input[class='placeOfSplyText']").val(placeOfSplyText);
		$("#"+parentTr+" input[class='placeOfSplyTextHid']").val(gstinCode);
		$("#staticWalkinVendorModal input[id='isGstinAddedInTransHid']").val(1);
		$("#closeWalkinVendModalBtn").click();
    });
});

var setWalkinVendorDetail = function(txnJsonData){
	var isGstinAddedInTransHid = $("#staticWalkinVendorModal input[id='isGstinAddedInTransHid']").val();
	if(isGstinAddedInTransHid != "1"){
		return true;
	}
	var gstinPart1 = $("#staticWalkinVendorModal input[name='gstinPart1']").val();
	var gstinPart2 = $("#staticWalkinVendorModal input[name='gstinPart2']").val();
	var gstinCode = gstinPart1 + gstinPart2;
	if((gstinCode.length > 1) && (gstinCode.length < 15 || gstinCode.length > 15)){
        swal("Invalid GSTIN!","Invalid GSTIN! Please provide correct GSTIN","error");
        return false;
    }
	var addressModal = $("#staticWalkinVendorModal input[id='vendorAddressHid']").val();
	var countryModal = $("#staticWalkinVendorModal select[name='countryModal'] option:selected").val();
	var stateModal = $("#staticWalkinVendorModal select[name='stateModal'] option:selected").text();
	var stateCode = $("#staticWalkinVendorModal select[name='stateModal'] option:selected").val();
	var locationModal = $("#staticWalkinVendorModal input[name='locationModal']").val();
	var phoneCountryCodeModal = $("#staticWalkinVendorModal select[name='phoneCountryCodeModal'] option:selected").val();
	var phone1Modal = $("#staticWalkinVendorModal input[name='phone1Modal']").val();
	var phone2Modal = $("#staticWalkinVendorModal input[name='phone2Modal']").val();
	var phone3Modal = $("#staticWalkinVendorModal input[name='phone3Modal']").val();
	
	if(addressModal == ""){
		swal("Invalid data!","Please provide valid vendor address.","error");
		$("#staticWalkinVendorModal").attr('data-toggle', 'modal');
		$("#staticWalkinVendorModal").modal('show');
		return false;
	}
	if(stateModal == ""){
		swal("Invalid data!","Please provide valid vendor state.","error");
		$("#staticWalkinVendorModal").attr('data-toggle', 'modal');
		$("#staticWalkinVendorModal").modal('show');
		return false;
	}else if(gstinPart1.length > 1 && stateCode != gstinPart1){
    	swal("Invalid data!","GSTIN and vendor State does not match, please provide valid vendor state/GSTIN.","error");
    	$("#staticWalkinVendorModal").attr('data-toggle', 'modal');
		$("#staticWalkinVendorModal").modal('show');
    	return false;
    }
	if(locationModal == ""){
		swal("Invalid data!","Please provide valid vendor location.","error");
		$("#staticWalkinVendorModal").attr('data-toggle', 'modal');
		$("#staticWalkinVendorModal").modal('show');
		return false;
	}
	if(txnJsonData.txnWalkinCustomerType == "1"){
		txnJsonData.businessIndividual ="2";
	}else if(txnJsonData.txnWalkinCustomerType == "2"){
		txnJsonData.businessIndividual ="1";  //business
	}
	txnJsonData.registeredOrUnReg = "1";
	txnJsonData.gstinCode = gstinCode;
	txnJsonData.futurePayAlwd = 1;
	txnJsonData.vendName = txnJsonData.txnforunavailablecustomer;
	txnJsonData.vendAddress = addressModal;
	txnJsonData.vendCountry = countryModal;
	txnJsonData.vendorState = stateModal;
	txnJsonData.vendorStateCode = stateCode
	txnJsonData.vendLocation = locationModal;
	txnJsonData.vendPhnCtryCode = phoneCountryCodeModal;
	txnJsonData.vendPhone = phone1Modal + phone2Modal + phone3Modal;
	txnJsonData.isGstinAddedInTransHid = isGstinAddedInTransHid;
	return true;
}

var disableWalkinVendor = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr+" #txnWalkinDiv").attr('disabled', 'disabled');
	clearWalkinVendorModal(this);
}

var enableWalkinVendor = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr+" #txnWalkinDiv").removeAttr('disabled');
}
