function getBudgetTemplate(){
	var jsonData = {};
	jsonData.useremail=$("#hiddenuseremail").text();
	var url="/budget/budgetTemplate";
	downloadFile(url, "POST", jsonData, "Error on downloading Budget!");
	/*$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
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
    	var dt = new Date().toString();
    	var fileName=data.budgetFileCred[0].fileName;
    	var url='assets/BudgetExcel/'+fileName+'?unique='+dt;
    	window.open(url);
      },
      error: function (xhr, status, error) {
      	if(xhr.status == 401){ doLogout(); }
      },
		complete: function(data) {
			$.unblockUI();
		}
   });*/
}


function getBudgetDetails(){
	var json = {};
	json.email = '@email';
	ajaxCall('/budget/getDetails', json, '', '', '', '', 'getBudgetDetailsSuccess', '', false);
}

/*Budget table Starts*/
function getBudgetDetailsSuccess(data) {
	if (data.result && data.size > 0) {
		data = data.details;
		if (!isEmpty(data) && data.length > 0) {
			budgetTable.prepareTable(data);
		}
	} else {
		swal("Error!",data.message,"error");
	}
}
var budgetTable = {
		getHeader : function(){
			return '<thead><tr>'
				+ '<td >Name</td><td >Jan</td><td >Feb</td><td >Mar</td><td >Apr</td><td >May</td><td >Jun</td><td >Jul</td><td >Aug</td><td >Sep</td><td >Oct</td><td >Nov</td><td >Dec</td><td >Total</td>'
				+ '</tr></thead>';
		},
		getEmptyInputRow : function(){
			return '<tr class="emptyInputRowClass" style="display:none"><td><button id="saveBudgetId" class="saveBudget btn btn-primary btn-idos"'
			+ 'title="Save Budget">Save Budget</button><button id="cancelBudgetId" class="cancelBudget btn btn-primary btn-idos"'
			+ 'title="Cancel Budget">Cancel Budget</button></td><td><input class="inputBudgetJan budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetFeb budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetMar budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetApr budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetMay budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetJune budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetJuly budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetAug budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetSep budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetOct budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetNov budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td>'
			+ '<td><input class="inputBudgetDec budgetCreteInput" value="0.0" onkeypress="return onlyDotsAndNumbers(event)"></td></tr>'
		},
		getRow : function(data){
			return '<tr>'
					+ '<td title="' + data.name + '">' + data.name + '</td>'
					+ '<td><span title="' + data.january +'"><i class="fa fa-arrow-up pr-5"></i>' + data.january +'</span><br/><span title="' + data.januaryDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.januaryDeducted +'</span></td>'
					+ '<td><span title="' + data.february +'"><i class="fa fa-arrow-up pr-5"></i>' + data.february +'</span><br/><span title="' + data.februaryDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.februaryDeducted +'</span></td>'
					+ '<td><span title="' + data.march +'"><i class="fa fa-arrow-up pr-5"></i>' + data.march +'</span><br/><span title="' + data.marchDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.marchDeducted +'</span></td>'
					+ '<td><span title="' + data.april +'"><i class="fa fa-arrow-up pr-5"></i>' + data.april +'</span><br/><span title="' + data.aprilDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.aprilDeducted +'</span></td>'
					+ '<td><span title="' + data.may +'"><i class="fa fa-arrow-up pr-5"></i>' + data.may +'</span><br/><span title="' + data.mayDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.mayDeducted +'</span></td>'
					+ '<td><span title="' + data.june +'"><i class="fa fa-arrow-up pr-5"></i>' + data.june +'</span><br/><span title="' + data.juneDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.juneDeducted +'</span></td>'
					+ '<td><span title="' + data.july +'"><i class="fa fa-arrow-up pr-5"></i>' + data.july +'</span><br/><span title="' + data.julyDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.julyDeducted +'</span></td>'
					+ '<td><span title="' + data.august +'"><i class="fa fa-arrow-up pr-5"></i>' + data.august +'</span><br/><span title="' + data.augustDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.augustDeducted +'</span></td>'
					+ '<td><span title="' + data.september +'"><i class="fa fa-arrow-up pr-5"></i>' + data.september +'</span><br/><span title="' + data.septemberDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.septemberDeducted +'</span></td>'
					+ '<td><span title="' + data.october +'"><i class="fa fa-arrow-up pr-5"></i>' + data.october +'</span><br/><span title="' + data.octoberDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.octoberDeducted +'</span></td>'
					+ '<td><span title="' + data.november +'"><i class="fa fa-arrow-up pr-5"></i>' + data.november +'</span><br/><span title="' + data.novemberDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.novemberDeducted +'</span></td>'
					+ '<td><span title="' + data.december +'"><i class="fa fa-arrow-up pr-5"></i>' + data.december +'</span><br/><span title="' + data.decemberDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.decemberDeducted +'</span></td>'
					+ '<td><span title="' + data.total +'"><i class="fa fa-arrow-up pr-5"></i>' + data.total +'</span><br/><span title="' + data.totalDeducted +'"><i class="fa fa-arrow-down pr-5"></i>' + data.totalDeducted +'</span></td>'
					+ '</tr>';
		},
		prepareTable : function(data){
			var table = '', tab = '', tData;
			for (var i=0;i<data.length;i++){
				tab += '<li class="btn-idos" onclick="budgetTabChange(this);"><a href="#budgetTab_' + i + '" data-toggle="tab">' + data[i].name +'</a></li>';
				table += '<div id="budgetTab_' + i + '"  class="tab-pane fade"><table class="table table-hover excelFormTable" id="budgetTable" style="margin: 0;">' + this.getHeader()
					+ '<tbody>' + this.getRow(data[i]);
				tData = data[i].specifics;
				if (!isEmpty(tData) && tData.length > 0){
					for (var j=0;j<tData.length;j++){
						table += this.getRow(tData[j]);
					}
				}
				table += '</tbody></table></div>';
			}
			$('#budgetTab').html(tab);
			$('#budgetTab').find('li:first').addClass('active');
			//$('#budgetTab').find('li:first').attr('style', 'background-color: #c0c0c0 !important');
			$('#budgetTable').html(table);
			$('#budgetTable').find('div:first').addClass('in').addClass('active');
		}
	};
/*Budget table Ends*/



$(document).ready(function() {
	$(".uploadBudget").click(function(){
		var chatofacturl=$("#uploadbudget").val();
		if(chatofacturl==""){
			swal("Invalid!","please upload budget for your company.","error");
			return true;
		}
		var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
		if(ext != "xls" && ext != "xlsx"){
			swal("Invalid!","Only Excel files are allowed for budget upload","error");
			$("#uploadbudget").val("");
			return true;
		}
		else{
			var jsonData={};
			var useremail=$("#hiddenuseremail").text();
			jsonData.usermail = useremail;
		}
		var form=$('#myBudgetForm');
		var data = new FormData();
		jQuery.each($('#uploadbudget')[0].files, function(i, file) {
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
				$("#uploadbudget").val("");
				getBudgetDetails();
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			},
			complete: function(data) {
				$.unblockUI();
			}
		});
	});
});


function budgetTabChange(e){
	  //e.preventDefault();
	  //$(e).tab('show');
	  //$('#budgetTab').find(e).addClass('active');
	  //$(e).css("background-color: red;");
	  //	$("#budgetTab").find("li").each(function(){
	//		$(this).attr('style', 'background-color: #e6e6e6; !important');
	//	});
	  //$(e).attr('style', 'background-color: #c0c0c0 !important');

}
