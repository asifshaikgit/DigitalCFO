/************************************************************************************************
This file contains functions for Sell trans walk-in customer
Created by Sunil K. Namdev on 18-07-2017.
*************************************************************************************************/

$(document).ready(function(){
	var cache = {};
	$('input[name="unAvailableCustomer"]').autocomplete({
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
				url: '/customer/walkincustomer/'+term,
				headers:{
					"X-AUTH-TOKEN": window.authToken
			  	},
			  	contentType:'application/json',
				success: function(data) {
					//for(var i=0;i<data.cutomerList.length;i++){
						//cache[data.cutomerList[i].customerName] = data.cutomerList[i].customerName;
					//	selectedItemsArray.push(data.cutomerList[i].customerName);
					//}
					cache[term] = data.cutomerList;
					response(data.cutomerList);
					$('input[name="unAvailableCustomer"]').removeClass('ui-autocomplete-loading');

										

					//response( $.map( data, function(item) {
					// your operation on data
					//}));
				},
				error: function(data) {
					$('input[name="unAvailableCustomer"]').removeClass('ui-autocomplete-loading');
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
	    	$("#"+parentTr+" select[class='walkinCustType'] option:first").prop("selected","selected");
	    	$("#"+parentTr+" input[name='unAvailableCustomer']").val(ui.item.label); 
	    	$("#"+parentTr+" input[name='txnWalkinPlcSplyText']").val(ui.item.value);
	    	$("#"+parentTr+" input[name='txnWalkinPlcSplyHid']").val(ui.item.idx);
	    	$("#"+parentTr+" input[name='txnWalkinPlcSplySelect']").val(ui.item.idx);
	    	showWalkinCustomerDetail(this);
	    },
	    change: function( event, ui ) {
	    	showWalkinCustomerDetail(this);
	    }
    });
});

var changeOnWalkinVendCust = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var checkBoxChecked=$(elem).is(':checked');
	if(checkBoxChecked === true){
		clearRegisterCustomerData(elem);
		clearMainTransTableFields(elem);
		$("#"+parentTr +" div[class='regCustomerDivCls']").hide();
		$("#"+parentTr +" div[class='txnWalkinDivCls']").show();
	}else{
		$("#"+parentTr +" div[class='txnWalkinDivCls']").hide();
		$("#"+parentTr +" div[class='regCustomerDivCls']").show();
		$("#closeWalkinCustModalBtn").click();
		$("#closeWalkinVendModalBtn").click();
		clearWalkinCustomerModal(elem);
		clearWalkinVendorModal(elem);
	}
	resetMultiItemsTableLength(parentTr);
	resetMultiItemsTableFieldsData(elem);
}

var clearMainTransTableFields = function(elem){
	var transactionTableTr = $(elem).closest('tr').attr('id');
	$("#" + transactionTableTr +" input[class='placeOfSplyText']").val("");
	$("#" + transactionTableTr +" input[class='placeOfSplyTextHid']").val("");
	$("#" + transactionTableTr +" input[class='unavailable']").val("");
	$("#" + transactionTableTr +" select[name='txnWalkinPlcSplySelect']").find('option:first').prop("selected","selected");
	$("#staticWalkinCustomerModal input[id='isGstinAddedInTransHid']").val(0);
	$("#staticWalkinVendorModal input[id='isGstinAddedInTransHid']").val(0);
}


var clearWalkinCustomerDetail = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr+" input[name='txnWalkinPlcSplyText']").val('');
	$("#"+parentTr+" input[name='txnWalkinPlcSplyHid']").val(''); 
	$("#"+parentTr+" select[class='walkinCustType'] option:first").prop("selected","selected");
}

var showWalkinCustomerDetail = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr +" input[name='txnWalkinPlcSplyText']").show();
	populatecustvendspecifics(elem); 
	enableWalkinCustomer(elem);
}


var showWalkinCustomerModal = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	var walkincustomer = $("#"+parentTr +" input[name='unAvailableCustomer']").val();
	if(walkincustomer == ""){
		swal("walk-in customer error!", "Please provide walk-in customer name.", "error");
		$(elem).val('');
	}
	var walkinCustPlcSplyHid =$("#"+parentTr +" input[name='txnWalkinPlcSplyHid']").val();
	var customerType= $(elem).val();
	if(customerType == "1" || customerType == "2"){
		if(walkinCustPlcSplyHid == ""){
			$("#staticWalkinCustomerModal").attr('data-toggle', 'modal');
			$("#staticWalkinCustomerModal").modal('show');
			$("#staticWalkinCustomerModal input[class='transTableTrIdCls']").val(parentTr);
			clearWalkinCustomerModal(this);
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
		checkForCompositionScheme(elem);
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


var clearRegisterCustomerData = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr +" table[class='multipleItemsTable'] tbody tr:last span[class='select2-selection__clear']").trigger("mousedown");
	$("#"+parentTr +" select[class='placeOfSply txnDestGstinCls']").find('option:first').prop("selected",true);
    $("#"+parentTr +" .masterList").find('option:first').prop("selected",true);
}

var clearWalkinCustomerModal = function(elem){
	$("#staticWalkinCustomerModal input[name='gstinPart1']").val('');
	$("#staticWalkinCustomerModal input[name='gstinPart2']").val('');
	$("#staticWalkinCustomerModal textarea[name='addressModal']").val('');
	$("#staticWalkinCustomerModal select[name='countryModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinCustomerModal select[name='stateModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinCustomerModal input[name='locationModal']").val('');
	$("#staticWalkinCustomerModal select[name='phoneCountryCodeModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinCustomerModal input[name='customerPhone1Modal']").val('');
	$("#staticWalkinCustomerModal input[name='customerPhone2Modal']").val('');
	$("#staticWalkinCustomerModal input[name='customerPhone3Modal']").val('');

	$("#staticWalkinCustomerModal input[name='isAddressSameModal']").attr('checked', false);
	$("#staticWalkinCustomerModal textarea[name='shipAddressModal']").val('');
	$("#staticWalkinCustomerModal select[name='shipCountryModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinCustomerModal select[name='shipStateModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinCustomerModal input[name='shipLocationModal']").val('');
	$("#staticWalkinCustomerModal select[name='shipPhnNocountryCodeModal']").find('option:first').prop("selected","selected");
	$("#staticWalkinCustomerModal input[name='shipPhone1Modal']").val('');
	$("#staticWalkinCustomerModal input[name='shipPhone2Modal']").val('');
	$("#staticWalkinCustomerModal input[name='shipPhone3Modal']").val('');
	$("#staticWalkinCustomerModal input[id='isGstinAddedInTransHid']").val('0');
}


$(document).ready(function(){
    $("#addWalkinCustModalBtn").click(function(){
		var gstinPart1 = $("#staticWalkinCustomerModal input[name='gstinPart1']").val();
		var gstinPart2 = $("#staticWalkinCustomerModal input[name='gstinPart2']").val();
		var gstinCode = gstinPart1 + gstinPart2;
		if((gstinCode.length > 1) && (gstinCode.length < 15 || gstinCode.length > 15)){
	        swal("Invalid GSTIN!","Invalid GSTIN! Please provide correct GSTIN","error");
	        return false;
	    }
		var addressModal = $("#staticWalkinCustomerModal textarea[name='addressModal']").val();
		var countryModal = $("#staticWalkinCustomerModal select[name='countryModal'] option:selected").val();
		var stateModal = $("#staticWalkinCustomerModal select[name='stateModal'] option:selected").text();
		var locationModal = $("#staticWalkinCustomerModal input[name='locationModal']").val();
		var phoneCountryCodeModal = $("#staticWalkinCustomerModal select[name='phoneCountryCodeModal'] option:selected").val();
		var customerPhone1Modal = $("#staticWalkinCustomerModal input[name='customerPhone1Modal']").val();
		var customerPhone2Modal = $("#staticWalkinCustomerModal input[name='customerPhone2Modal']").val();
		var customerPhone3Modal = $("#staticWalkinCustomerModal input[name='customerPhone3Modal']").val();
		
		var shipAddressModal = $("#staticWalkinCustomerModal textarea[name='shipAddressModal']").val();
		var shipCountryModal = $("#staticWalkinCustomerModal select[name='shipCountryModal'] option:selected").val();
		var shipStateModal = $("#staticWalkinCustomerModal select[name='shipStateModal'] option:selected").text();
		var shipcustStateCode = $("#staticWalkinCustomerModal select[name='shipStateModal'] option:selected").val();
		var shipLocationModal = $("#staticWalkinCustomerModal input[name='shipLocationModal']").val();
		var shipPhnNocountryCodeModal = $("#staticWalkinCustomerModal select[name='shipPhnNocountryCodeModal'] option:selected").val();
		var shipPhone1Modal = $("#staticWalkinCustomerModal input[name='shipPhone1Modal']").val();
		var shipPhone2Modal = $("#staticWalkinCustomerModal input[name='shipPhone2Modal']").val();
		var shipPhone3Modal = $("#staticWalkinCustomerModal input[name='shipPhone3Modal']").val();
		var isAddressSameModal = $("#staticWalkinCustomerModal input[name='isAddressSameModal']").is(':checked');

		$("#staticWalkinCustomerModal input[id='customerAddressHid']").val(addressModal);
		$("#staticWalkinCustomerModal input[id='shipCustAddressHid']").val(shipAddressModal);

		if(isAddressSameModal){
			shipAddressModal = addressModal;
			shipStateModal = stateModal;
			shipcustStateCode = $("#staticWalkinCustomerModal select[name='stateModal'] option:selected").val();
			shipLocationModal = locationModal;
		}
		if(shipAddressModal == ""){
			swal("Incomplete Details!","Please provide valid shipping address.","error");
			return false;
		}
		if(shipStateModal == ""){
			swal("Incomplete Details!","Please provide valid shipping state.","error");
			return false;
		}else if(gstinPart1.length > 1 && shipcustStateCode != gstinPart1){
	    	swal("Invalid details!","GSTIN and shipping State does not match, please provide valid shipping state/GSTIN.","error");
	    	return false;
	    }
		if(shipLocationModal == ""){
			swal("Invalid details!","Please provide valid shippping location.","error");
			return false;
		}
	
		var parentTr = $("#staticWalkinCustomerModal input[class='transTableTrIdCls']").val();
		var placeOfSplyText = shipLocationModal+"-"+gstinCode+"-"+shipStateModal;
		$("#"+parentTr +" input[class='placeOfSplyText']").val(placeOfSplyText);
		$("#"+parentTr+" input[class='placeOfSplyTextHid']").val(gstinCode);
		$("#staticWalkinCustomerModal input[id='isGstinAddedInTransHid']").val(1);
		$("#closeWalkinCustModalBtn").click();
    });
});

var setWalkinCustomerdetail = function(txnJsonData){
	var isGstinAddedInTransHid = $("#staticWalkinCustomerModal input[id='isGstinAddedInTransHid']").val();
	if(isGstinAddedInTransHid != "1"){
		return true;
	}
	var gstinPart1 = $("#staticWalkinCustomerModal input[name='gstinPart1']").val();
	var gstinPart2 = $("#staticWalkinCustomerModal input[name='gstinPart2']").val();
	var gstinCode = gstinPart1 + gstinPart2;
	if((gstinCode.length > 1) && (gstinCode.length < 15 || gstinCode.length > 15)){
        swal("Incomplete Details!","Invalid GSTIN! Please provide correct GSTIN","error");
        return false;
    }
	var addressModal = $("#staticWalkinCustomerModal input[id='customerAddressHid']").val();
	var countryModal = $("#staticWalkinCustomerModal select[name='countryModal'] option:selected").val();
	var stateModal = $("#staticWalkinCustomerModal select[name='stateModal'] option:selected").text();
	var locationModal = $("#staticWalkinCustomerModal input[name='locationModal']").val();
	var phoneCountryCodeModal = $("#staticWalkinCustomerModal select[name='phoneCountryCodeModal'] option:selected").val();
	var customerPhone1Modal = $("#staticWalkinCustomerModal input[name='phone1Modal']").val();
	var customerPhone2Modal = $("#staticWalkinCustomerModal input[name='phone2Modal']").val();
	var customerPhone3Modal = $("#staticWalkinCustomerModal input[name='phone3Modal']").val();
	
	var shipAddressModal = $("#staticWalkinCustomerModal input[id='shipCustAddressHid']").val();
	var shipCountryModal = $("#staticWalkinCustomerModal select[name='shipCountryModal'] option:selected").val();
	var shipStateModal = $("#staticWalkinCustomerModal select[name='shipStateModal'] option:selected").text();
	var shipcustStateCode = $("#staticWalkinCustomerModal select[name='shipStateModal'] option:selected").val();
	var shipLocationModal = $("#staticWalkinCustomerModal input[name='shipLocationModal']").val();
	var shipPhnNocountryCodeModal = $("#staticWalkinCustomerModal select[name='shipPhnNocountryCodeModal'] option:selected").val();
	var shipPhone1Modal = $("#staticWalkinCustomerModal input[name='shipPhone1Modal']").val();
	var shipPhone2Modal = $("#staticWalkinCustomerModal input[name='shipPhone2Modal']").val();
	var shipPhone3Modal = $("#staticWalkinCustomerModal input[name='shipPhone3Modal']").val();
	var isAddressSameModal = $("#staticWalkinCustomerModal input[name='isAddressSameModal']").is(':checked');
	if(isAddressSameModal){
		shipAddressModal = addressModal;
		shipStateModal = stateModal;
		shipcustStateCode = $("#staticWalkinCustomerModal select[name='stateModal'] option:selected").val();
		shipLocationModal = locationModal;
	}
	if(shipAddressModal == ""){
		swal("Invalid data!","Please provide valid shipping address.","error");
		return false;
	}
	if(shipStateModal == ""){
		swal("Invalid data!","Please provide valid shipping state.","error");
		return false;
	}else if(gstinPart1.length > 1 && shipcustStateCode != gstinPart1){
    	swal("Invalid data!","GSTIN and shipping State does not match, please provide valid shipping state/GSTIN.","error");
    	return false;
    }
	if(shipLocationModal == ""){
		swal("Invalid data!","Please provide valid shippping location.","error");
		return false;
	}
	
	txnJsonData.gstinCode = gstinCode;
	txnJsonData.customerfutPayAlwd = 1;
	txnJsonData.custName = txnJsonData.txnforunavailablecustomer;
	txnJsonData.customerAddress = addressModal;
	txnJsonData.customerCountry = countryModal;
	txnJsonData.customerState = stateModal;
	txnJsonData.customerLocation = locationModal;
	txnJsonData.customerPhnCtryCode = phoneCountryCodeModal;
	txnJsonData.customerPhone = customerPhone1Modal + customerPhone2Modal + customerPhone3Modal;
	
	txnJsonData.isShippingAddressSame = isAddressSameModal 
	txnJsonData.shipcustAddress = shipAddressModal;
	txnJsonData.shipcustCountry = shipCountryModal;
	txnJsonData.shipcustState = shipStateModal;
	txnJsonData.shipcustStateCode = shipcustStateCode
	txnJsonData.shipcustLocation = shipLocationModal;
	txnJsonData.shipcustPhnCtryCode = shipPhnNocountryCodeModal;
	txnJsonData.shipcustPhone = shipPhone1Modal + shipPhone2Modal + shipPhone3Modal;
	txnJsonData.isGstinAddedInTransHid = isGstinAddedInTransHid;
	return true;
}

var disableWalkinCustomer = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr+" #txnWalkinDiv").attr('disabled', 'disabled');
	clearWalkinCustomerModal(this);
}

var enableWalkinCustomer = function(elem){
	var parentTr = $(elem).closest('tr').attr('id');
	$("#"+parentTr+" #txnWalkinDiv").removeAttr('disabled');
}
