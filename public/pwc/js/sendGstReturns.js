/**
 * PWC: Manali Mungikar
 * GST Returns: downloads flat files
 */


function getTransactionsDataInXlsx(elem){
    $("#showTransactionData").attr('data-toggle', 'modal');
    $("#showTransactionData").modal('show');
    $('#searchFromTranDownloadDate').datepicker();
    $('#searchToTranDownloadDate').datepicker();    
}

function downloadTransactionData(){
    var fromDate=$("#searchFromTranDownloadDate").val();
    var toDate=$("#searchToTranDownloadDate").val();
    var exportType = $("#exportTypeForGSTReturn").val();
    var jsonData = {};
    jsonData.fromDate=fromDate;
    jsonData.toDate=toDate;
    jsonData.exportType=exportType;
    jsonData.useremail=$("#hiddenuseremail").text();
    var url="/config/downloadTransactionDataFile";
    downloadFile(url, "POST", jsonData, "Error on downloading Transaction Data!");
}
 
function downloadBuyTransactionData(){
	var fromDate=$("#searchFromTranDownloadDate").val();
    var toDate=$("#searchToTranDownloadDate").val();
    var exportType = $("#exportTypeForGSTReturn").val();
    var jsonData = {};
    jsonData.fromDate=fromDate;
    jsonData.toDate=toDate;
    jsonData.exportType=exportType;
    jsonData.useremail=$("#hiddenuseremail").text();
    var url="/config/downloadBuyTransactionDataFile";
    downloadFile(url, "POST", jsonData, "Error on downloading Transaction Data!");
}

function callPWCGBIUrl(elem){
    var jsonData = {};    
    var useremail=$("#hiddenuseremail").text();    
    jsonData.usermail = useremail;
    var url="/config/callPWCGBIUrl";
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
        	//window.open(data.pwcurl); // to open link in NEW tab.
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){ doLogout(); }
        }
    });
}


/** Added by Puja Lohia 22nd Feb'18**/
function newAdminPWC(domId){
	if(domId=="add-new-admin"){
		$("div[id='newadmin']").show();
		$("div[id='newadminButton']").show();
		document.getElementById("plusminus").className = "fa fa-minus-circle fa-lg";
		
	}
	if(domId=="adminClose"){
		$("div[id='newadmin']").hide();
		$("div[id='newadminButton']").hide();
		document.getElementById("plusminus").className = "fa fa-plus-circle fa-lg";
	}
}


