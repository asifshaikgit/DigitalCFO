
function getVendorSellerAnalyticsSuccess(data){
	if(data.result){
		$('#analySearch').html(data.searched);
		$('#analyContact').html(data.contacted);
		$('#analyConvert').html(data.converted);
		$('#analyConvertRate').html(data.convertRate);
	}
}

function sellerVendorItemsListWithPricings(){
	var jsonData = {};
	jsonData.vendSellerEmail=$("#hiddenvendcustemail").text();
	var url="/sellervendor/itemsPricings";
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		type:"text",
		async: false,
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
			$("#vendorAccountName").val(data.sellerVendorInfoData[0].vendorName);
			$("#vendorAccountEmail").val(data.sellerVendorInfoData[0].vendorEmail);
			var phNo1=data.sellerVendorInfoData[0].vendorPhoneNumber.substring(0,3);
			var phNo2=data.sellerVendorInfoData[0].vendorPhoneNumber.substring(3,6);
			var phNo3=data.sellerVendorInfoData[0].vendorPhoneNumber.substring(6,10);
			var countryCode=data.sellerVendorInfoData[0].vendorCountryCode;
			if(typeof phNo1!='undefined'){
				$("#vendorsellerphnumber1").val(phNo1);
			}
			if(typeof phNo2!='undefined'){
				$("#vendorsellerphnumber2").val(phNo2);
			}
			if(typeof phNo3!='undefined'){
				$("#vendorsellerphnumber3").val(phNo3);
			}
			$("select[name='vendorsellercountryPhnCode'] option").filter(function () {return $(this).val()==countryCode;}).prop("selected", "selected");
			$.unblockUI();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doSellerLogout(); }
		}
	});
}

function addSupplierEcommerce(elem){
	var jsonData = {};
	var parentTr=$(elem).parent().parent().attr('class');
	var supplierName=$("#vendorAccountName").val();
	var supplierEmail=$("#vendorAccountEmail").val();
	var supplierPhoneNumber=$("#vendorsellercountryPhnCode option:selected").val()+"-"+$("#vendorsellerphnumber1").val()+$("#vendorsellerphnumber2").val()+$("#vendorsellerphnumber3").val();
	var itemPricePrimId=$("."+parentTr+" input[name='idosRegVendSellerHiddenId']").val();
	var itemPriceName=$("."+parentTr+" input[name='supplierVendorItem']").val();
	var itemDescription=$("."+parentTr+" textarea[name='vendorselleritemdescription']").val();
	var itemAvailableLocations=$('.'+parentTr+' select[name="vendorsellerBranches"] option[value!="multiselect-all"]:selected').map(function () {
 		return this.value;
 	}).get().toString();
	var resellerPrice=$("."+parentTr+" input[name='vendorsellerretailerprice']").val();
	var wholesellerPrice=$("."+parentTr+" input[name='vendorsellerwholesellerprice']").val();
	var specialPrice=$("."+parentTr+" input[name='vendorsellerspecialofferedprice']").val();
	var specialPriceRequirements=$("."+parentTr+" textarea[name='vendorsellerspecialofferedpricerequirements']").val();
	if(itemPricePrimId!=""){
		jsonData.supplieritemPricePrimId=itemPricePrimId;
	}
	if(itemPriceName==""){
		swal("Error!","Please enter what you can supply.","error")
		return true;
	}
	if(itemAvailableLocations==""){
		swal("Invalid!","Please select to which locations you can supply.","error")
		return true;
	}
	jsonData.supplierRegName=supplierName;
	jsonData.supplierRegEmail=supplierEmail;
	jsonData.supplierRegPhoneNumber=supplierPhoneNumber;
	jsonData.supplieritemPriceName=itemPriceName;
	jsonData.supplieritemDescription=itemDescription;
	jsonData.supplieritemAvailableLocations=itemAvailableLocations;
	jsonData.supplierresellerPrice=resellerPrice;
	jsonData.supplierwholesellerPrice=wholesellerPrice;
	jsonData.supplierspecialPrice=specialPrice;
	jsonData.specialPriceRequirements=specialPriceRequirements;
	var url="/ecommerce/saveSupplierItemPricings";
	$.ajax({
		url: url,
		data: JSON.stringify(jsonData),
		type:"text",
		headers:{
			"X-AUTH-TOKEN": window.authToken
		},
		method:"POST",
		contentType:'application/json',
		success: function (data) {
			$("."+parentTr+" input[name='idosRegVendSellerHiddenId']").val("");
			$("."+parentTr+" input[name='supplierVendorItem']").val("");
			$("."+parentTr+" textarea[name='vendorselleritemdescription']").val("");
			$("."+parentTr+" select[name='vendorsellerBranches'] option:selected").each(function () {
				$(this).removeAttr('selected');
			});
			$("."+parentTr+" select[name='vendorsellerBranches']").multiselect('rebuild');
			$("."+parentTr+" input[name='vendorsellerretailerprice']").val("");
			$("."+parentTr+" input[name='vendorsellerwholesellerprice']").val("");
			$("."+parentTr+" input[name='vendorsellerspecialofferedprice']").val("");
			$("."+parentTr+" textarea[name='vendorsellerspecialofferedpricerequirements']").val("");
			var jsonData = {};
			jsonData.email = $('#hiddenvendcustemail').text();
			ajaxCall('/seller/getPricings', jsonData, '', true, '', '', 'getPricingsSuccess', '', true);
			swal("Success!","Successfully entered Supplier Item and their pricings.","success");
		},
		error: function (jqXHR, status, error) {
			if(jqXHR.status == 401){ doSellerLogout(); }
		}
	});
}



function returnToMainSellerLogin(){
	$("#sellerAccountLoginDiv input:enabled:visible:first").focus();
	$(".mainDiv").hide();
	$("#sellerAccountLoginDiv").show();
	window.location.href="/signUp#sellerAccountLoginDiv";
}


/*vendor supplier add location to the master database start*/
function addLocationToContainer(elem){
	swal("INFO!","addLocationToContainer","info");
	var multipleVendorSellerInput=$("#multipleVendorSellerInput").val();
	if(multipleVendorSellerInput!=""){
		var jsonData = {};
		jsonData.userEmail = $("#hiddenvendcustemail").text();
		jsonData.locationName=multipleVendorSellerInput;
		ajaxCall('/add/vendSupplierLocation', jsonData, '', '', '', '', 'vendSupplierLocationSuccess', '', false);
	}
}
function vendSupplierLocationSuccess(data){
	if(data.result){
		//add to the select optio and rebuild the multiselect container
		$("#vendCustTransactions select[id='vendorsellerBranches']").append('<option value="'+data.listedLocationData[0].listedLocation+'">'+data.listedLocationData[0].listedLocation+'</option>');
		$("#vendSellerAccounts select[id='vendorsellerBranches']").append('<option value="'+data.listedLocationData[0].listedLocation+'" selected="selected">'+data.listedLocationData[0].listedLocation+'</option>');
		$('.multipleVendorSeller').multiselect('rebuild');
		$("input[type='checkbox'][value="+data.listedLocationData[0].locationName+"]").attr('checked',true);
		var length=0;
		$("#vendSellerAccounts select[id='vendorsellerBranches'] option").each(function () {
			if($(this).val()!="" && $(this).attr('selected')=="selected"){
				length=length+1;
			}
		});
		var buttonText="";
		if (length == 0) {
			buttonText='None selected <b class="caret"></b>';
        }
        else if (length > 6) {
        	buttonText=length + ' selected  <b class="caret"></b>';
        }else{
        	buttonText=length + ' selected  <b class="caret"></b>';
        }
		$(".newSupplierVendorItemsRegister").find('button[class="multiselect dropdown-toggle btn"]').html(buttonText);
		$("#multipleVendorSellerInput").val("");
	}
}
/*vendor supplier add location to the master database end*/


/*Seller Table Starts*/
$(document).ready(function(){
	$('body').on('click', '.sellerEdit', function(){
		$(".multipleVendorSeller").each(function () {
			$(this).removeAttr('selected');
		});
		$(".multipleVendorSeller").multiselect('rebuild');
		var id=this.id,email = $('#hiddenvendcustemail').text();
		id=id.split('_');
		id=id[1];
		if(!isEmpty(email) && !isEmpty(id)){
			var jsonData = {};
			jsonData.email = email;
			jsonData.id = id;
			ajaxCall('/seller/getPriceDetails', jsonData, '', true, '', '', 'getPriceDetailsSuccess', '', false);
		}
	});
});
function getPriceDetailsSuccess(data) {
	if(data.result){
		$('#idosRegVendSellerHiddenId').val(data.id);
		$('#supplierVendorItem').val(data.item);
		$('#vendorselleritemdescription').val(data.description);
		$('#vendorsellerretailerprice').val(data.retailerUnitPrice);
		$('#vendorsellerwholesellerprice').val(data.wholesaleUnitPrice);
		$('#vendorsellerspecialofferedprice').val(data.specialUnitPrice);
		$('#vendorsellerspecialofferedpricerequirements').val(data.specialPriceRequirements);
		data=data.locations;
		if(!isEmpty(data) && data.length>0){
			for(var i=0;i<data.length;i++){
				$(".multipleVendorSeller").find('option[value="'+data[i].location+'"]').prop('selected', "selected");
			}
			var text=data.length+' selected';
			$(".multipleVendorSeller").multiselect('rebuild');
		}
	}
}
function getPricingsSuccess(data){
	if (data.result){
		data = data.pricings;
		if (!isEmpty(data) && data.length>0){
			var html='', locs;
			for (var i=0;i<data.length;i++){
				if (!isEmpty(data[i].locations) && data[i].locations.length>0){
					locs='<select id="itemSelect_'+data[i].id+'" style="width: 140px;">';
					for(var j=0;j<data[i].locations.length;j++){
						locs+='<option>'+data[i].locations[j].location+'</option>';
					}
					locs+='</select>';
				} else {
					locs = '';
				}
				$("#sellerItemsTable").find('tr[id="row_'+data[i].id+'"]').remove();
				html+='<tr id=row_'+data[i].id+'><td>'+data[i].item+'</td><td>'+data[i].description+'</td><td>'+locs+'</td>'
					+'<td>'+data[i].retailerUnitPrice+'</td><td>'+data[i].wholesaleUnitPrice+'</td><td>'+data[i].specialUnitPrice+'</td>'
					+'<td>'+data[i].specialPriceRequirements+'</td><td><span id="price_'+data[i].id+'" class="sellerEdit color-light-blue"><i class="fa fa-edit pr-5"></i>Edit</span></td></tr>';
			}
			$('#sellerItemsTable tbody').append(html);
		}
	}
}
/*Seller Table Ends*/


$(document).ready(function() {
	$(".uploadSupplierItems").click(function(){
		var supplieritems=$("#uploadsupplieritem").val();
		if(supplieritems==""){
			swal("Invalid!","please upload all the items supplied by you.","error");
			return true;
		}
		var ext = supplieritems.substring(supplieritems.lastIndexOf('.') + 1);
		if(ext != "xls" && ext != "xlsx"){
			swal("Invalid!","Only Excel files are allowed for supplier items upload","error");
			$("#uploadsupplieritem").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenvendcustemail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#mySupplierForm');
		var data = new FormData();
		jQuery.each($('#uploadsupplieritem')[0].files, function(i, file) {
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
				$("#uploadsupplieritem").val("");
				$.unblockUI();
			},
			error: function (xhr, status, error) {
				$.unblockUI();
				if(xhr.status == 401){ doSellerLogout(); }
			}
		});
	});
});
