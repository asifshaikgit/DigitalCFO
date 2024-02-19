function getTDSReportInXlsx(elem){
    $("#showTDSReportData").attr('data-toggle', 'modal');
    $("#showTDSReportData").modal('show');
    $('#TDSFromDate').datepicker();
    $('#TDSToDate').datepicker();    
}

function downloadTDSReport(elem){
    var fromDate=$("#TDSFromDate").val();
    var toDate=$("#TDSToDate").val();
    var buttonId=$(elem).attr('id');
    if(fromDate == ""){
    	swal("Date range not selected", "Please select From date for the Report", "error");
    	return false;
    }
    if(toDate == ""){
       	swal("Date range not selected", "Please select To date for the Report", "error");
       	return false;
   	}
    if(Date.parse(toDate) <= Date.parse(fromDate)){
    	swal("Wrong To Date selected", "Please select To date after From date for the Report", "error");
    	return false;
    }
    var jsonData = {};
    if(buttonId=="downloadTDSReportPDF")
	{
    	jsonData.reportType="pdf";
	}
    else
    	{
    	jsonData.reportType="xlsx";
    	}
    jsonData.fromDate=fromDate;
    jsonData.toDate=toDate;
    jsonData.useremail=$("#hiddenuseremail").text();
    var url="/transaction/downloadTDSReportFile";
    downloadFile(url, "POST", jsonData, "Error on downloading Transaction Data!");
}
