/**
 * Manali: Created for bulk Transction upload/download using xls 
 */

var bulkTransactionUpload = function(elem){
	$("#messageModal").html("");
	$("#staticBulkTransUpload").attr('data-toggle', 'modal');
	$("#staticBulkTransUpload").modal('show');
	$(".staticBulkTransUploadclose").attr("href",location.hash);
}


$(document).ready(function() {
	$(".uploadrecpayButton").click(function(){
		var chatofacturl=$("#uploadrecpay").val();
		if(chatofacturl==""){
			swal("Incomplete details!","please upload your company transactions in csv format","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "csv"){
			swal("Error!","Only CSV files are allowed for transaction upload","error");
			$("#uploadrecpay").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#myRecPayForm');
		var data = new FormData();
		jQuery.each($('#uploadrecpay')[0].files, function(i, file) {
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
				$("#uploadrecpay").val("");
				//alert("Total records in xls: " +data.totalRowsInXls + ", Inserted in IDOS: " + data.totalRowsInserted);
				$.unblockUI();
				location.reload(true);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

$(document).ready(function() {
	$(".uploadselltranButton").click(function(){
		var chatofacturl=$("#uploadselltran").val();
		if(chatofacturl==""){
			swal("Incomplete details!","please upload your company transactions in csv format","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "csv"){
			swal("Error!","Only CSV files are allowed for transaction upload","error");
			$("#uploadrecpay").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#mySellTranUploadForm');
		var data = new FormData();
		jQuery.each($('#uploadselltran')[0].files, function(i, file) {
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
				$("#uploadselltran").val("");
				//alert("Total records in xls: " +data.totalRowsInXls + ", Inserted in IDOS: " + data.totalRowsInserted);
				$.unblockUI();
				location.reload(true);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

$(document).ready(function() {
	$(".uploadsellOnCashtranButton").click(function(){
		var chatofacturl=$("#uploadsellOnCashtran").val();
		if(chatofacturl==""){
			swal("Incomplete details!","please upload your company transactions in csv format","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "csv"){
			swal("Error!","Only CSV files are allowed for transaction upload","error");
			$("#uploadrecpay").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#mySellOnCashTranUploadForm');
		var data = new FormData();
		jQuery.each($('#uploadsellOnCashtran')[0].files, function(i, file) {
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
				$("#uploadsellOnCashtran").val("");
				//alert("Total records in xls: " +data.totalRowsInXls + ", Inserted in IDOS: " + data.totalRowsInserted);
				$.unblockUI();
				location.reload(true);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

$(document).ready(function() {
	$(".uploadbuyoncredittranButton").click(function(){
		var chatofacturl=$("#uploadbuyoncredittran").val();
		if(chatofacturl==""){
			swal("Incomplete Details!","please upload your company transactions in csv format","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "csv"){
			swal("Error!","Only CSV files are allowed for transaction upload","error");
			$("#uploadrecpay").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#myBuyOnCreditTranUploadForm');
		var data = new FormData();
		jQuery.each($('#uploadbuyoncredittran')[0].files, function(i, file) {
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
				$("#uploadbuyoncredittran").val("");
				//alert("Total records in xls: " +data.totalRowsInXls + ", Inserted in IDOS: " + data.totalRowsInserted);
				$.unblockUI();
				location.reload(true);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

$(document).ready(function() {
	$(".uploadbuyoncashtranButton").click(function(){
		var chatofacturl=$("#uploadbuyoncashtran").val();
		if(chatofacturl==""){
			swal("Incomplete Details!","please upload your company transactions in csv format","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "csv"){
			swal("Error!","Only CSV files are allowed for transaction upload","error");
			$("#uploadbuyoncashtran").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#myBuyOnCashTranUploadForm');
		var data = new FormData();
		jQuery.each($('#uploadbuyoncashtran')[0].files, function(i, file) {
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
				$("#uploadbuyoncashtran").val("");
				//alert("Total records in xls: " +data.totalRowsInXls + ", Inserted in IDOS: " + data.totalRowsInserted);
				$.unblockUI();
				location.reload(true);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});
$(document).ready(function() {
	$(".uploadpayvendButton").click(function(){
		var chatofacturl=$("#uploadpayvend").val();

		if(chatofacturl==""){
			swal("Incomplete Details!","please upload your company transactions in csv format2","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "csv"){
			swal("Error!","Only CSV files are allowed for transaction upload","error");
			$("#uploadpayvend").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#myPayVendForm');
		var data = new FormData();
		jQuery.each($('#uploadpayvend')[0].files, function(i, file) {
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
				$("#uploadpayvend").val("");
				//alert("Total records in xls: " +data.totalRowsInXls + ", Inserted in IDOS: " + data.totalRowsInserted);
				$.unblockUI();
				location.reload(true);
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});
