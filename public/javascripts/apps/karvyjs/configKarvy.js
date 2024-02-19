/**
 * @Author: Manali Mungikar
 */

/*vendor supplier add location to the master database start*/
function submitTransactionsToKarvy(elem){
    var mouldesRights = $("#usermoduleshidden").val();
    showHideModuleTabs(mouldesRights);
	swal("INFO","submitTransactionsToKarvy","info");
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
    $("#karvyJSONData").attr('data-toggle', 'modal');
    $("#karvyJSONData").modal('show');
    $('#txtDate').datepicker();
   // $('#searchToTranDownloadDate').datepicker();    
}

$(document).ready(function() {
	$('.datepicker-year').datepicker({
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
function downloadGSTR1JSONData(){
    var txtDate=$("#txtDate").val();   
    var jsonData = {};   
    jsonData.txtDate=txtDate;
    jsonData.type="GSTR1";
    jsonData.useremail=$("#hiddenuseremail").text();
    var url="/config/downloadKarvyGSTR1JSONSFile";
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
