function getOperational(useremail){
	//alert(">>>>>29"); //sunil
	var jsonData = {};
	jsonData.usermail = useremail;
	var url="/organization/getOperationals";
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
			$("#auditorOperationalTable tbody").html("");
			for(var i=0;i<data.orgTaskManagerData.length;i++){
				$("#auditorOperationalTable").append('<tr id="alertActionId'+data.orgTaskManagerData[i].remainderActionid+'"><td>'+data.orgTaskManagerData[i].remainderActionTask+'</td><td>'+data.orgTaskManagerData[i].alertForAction+'</td><td>'+data.orgTaskManagerData[i].alertForInformation+'</td><td>'+data.orgTaskManagerData[i].remainderDueDated+'</td><td><div class="operremstatgreen">'+data.orgTaskManagerData[i].confirmationStatus+'</div></td></tr>');
				if(data.orgTaskManagerData[i].confirmationStatus=='completed'){
					$("#auditorOperationalTable").find('tr[id="alertActionId'+data.orgTaskManagerData[i].remainderActionid+'"] div[class="operremstatgreen"]').attr('class','operremstatgreen');
					$("#auditorOperationalTable").find('tr[id="alertActionId'+data.orgTaskManagerData[i].remainderActionid+'"] div[class="operremstatred"]').attr('class','operremstatgreen');
				}
				if(data.orgTaskManagerData[i].confirmationStatus=='PENDING'){
					$("#auditorOperationalTable").find('tr[id="alertActionId'+data.orgTaskManagerData[i].remainderActionid+'"] div[class="operremstatgreen"]').attr('class','operremstatred');
					$("#auditorOperationalTable").find('tr[id="alertActionId'+data.orgTaskManagerData[i].remainderActionid+'"] div[class="operremstatred"]').attr('class','operremstatred');
				}
			}
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}

var idosmonths=['January','February','March','April','May','June','July','August','September','October','November','December'];
function dateCheck(from,to,check) {
	var fDate,lDate,cDate;
	fDate = Date.parse(from);
	tDate = Date.parse(to);
	cDate = Date.parse(check);
	if(cDate >= fDate && cDate <= tDate) {
		return true;
	}
	return false;
}

function closeOperational(){
	$("#financialDataDashboard").slideDown('slow');
	$("#operationalDataDashboard").slideUp('slow');
	$("#operationalSearchDataDashboard").slideUp('slow');
}

$(document).ready(function(){
	$('.newOperationalButton').click(function(){
		var useremail=$("#hiddenuseremail").text();
		$("#financialDataDashboard").slideUp('slow');
		$("#operationalDataDashboard").slideDown('slow');
		$("#operationalSearchDataDashboard").slideUp('slow');
		$('.operationalCalendar').fullCalendar('render');
		var firstdate=$("tr[class='fc-week fc-first'] td:first").attr('data-date');
		var lastdate=$("tr[class='fc-week fc-last'] td:last").attr('data-date');
		getRealTimeAlertsInfo(useremail,firstdate,lastdate);
	});
});

function closeOperationalSearch(){
	$("#financialDataDashboard").slideUp('slow');
	$("#operationalDataDashboard").slideDown('slow');
	$("#operationalSearchDataDashboard").slideUp('slow');
}

$(document).ready(function(){
	$('.newOperationalSearchControlButton'). click(function(){
		$("#financialDataDashboard").slideUp('slow');
		$("#operationalDataDashboard").slideUp('slow');
		$("#operationalSearchDataDashboard").slideDown('slow');
		searchOperationalData.resetOperationalDropdowns();
		searchOperationalData.resetOperationalDataResult();
		var jsonData = {};
		jsonData.useremail=$("#hiddenuseremail").text();
		jsonData.type = 1;
		$.ajax({
			url: "/dashboard/operationalDataSearch",
			data:JSON.stringify(jsonData),
			type:"text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method:"POST",
			contentType:'application/json',
			success: function (data) {
				if (null != data || undefined != data) {
					searchOperationalData.createOrganizationDataTable(data);
				}
			},
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});


function getRealTimeAlertsInfo(useremail,firstdate,lastdate){
	//alert(">>>>>30"); //sunil
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.usermail = useremail;
	jsonData.monthFirstDate=firstdate;
	jsonData.monthLastDate=lastdate;
	var url="/organization/getRealTimeAlertsInfo";
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
			var curDate=new Date(),dateStr=new RegExp(curDate.getFullYear()+'-'+(curDate.getMonth()+1));
			var upcoming={},nexDate=new Date(curDate.getTime()+(30*24*60*60*1000));
			$("tr[class='fc-week fc-first'] td div[class='fc-day-content']").html("");
			$("tr[class='fc-week'] td div[class='fc-day-content']").html("");
			$("tr[class='fc-week fc-last'] td div[class='fc-day-content']").html("");
			for(var i=0;i<data.branchWisePremiseAlertLiveDatesData.length;i++){
				$("tr[class='fc-week fc-first'] td[data-date='"+data.branchWisePremiseAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWisePremiseAlertLiveDatesData[i].title+'"><div class="fc-color fc-premise-color"></div>'+data.branchWisePremiseAlertLiveDatesData[i].title+'</div>');
				$("tr[class='fc-week'] td[data-date='"+data.branchWisePremiseAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWisePremiseAlertLiveDatesData[i].title+'"><div class="fc-color fc-premise-color"></div>'+data.branchWisePremiseAlertLiveDatesData[i].title+'</div>');
				$("tr[class='fc-week fc-last'] td[data-date='"+data.branchWisePremiseAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWisePremiseAlertLiveDatesData[i].title+'"><div class="fc-color fc-premise-color"></div>'+data.branchWisePremiseAlertLiveDatesData[i].title+'</div>');
				if(dateCheck(curDate,nexDate,data.branchWisePremiseAlertLiveDatesData[i].start)){
					if(isEmpty(upcoming[data.branchWisePremiseAlertLiveDatesData[i].start])){
						upcoming[data.branchWisePremiseAlertLiveDatesData[i].start]=data.branchWisePremiseAlertLiveDatesData[i].title+'@_premise_@';
					}else{
						upcoming[data.branchWisePremiseAlertLiveDatesData[i].start]+=','+data.branchWisePremiseAlertLiveDatesData[i].title+'@_premise_@';
					}
				}
			}
			for(var i=0;i<data.branchWiseStatutoryAlertLiveDatesData.length;i++){
				$("tr[class='fc-week fc-first'] td[data-date='"+data.branchWiseStatutoryAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseStatutoryAlertLiveDatesData[i].title+'"><div class="fc-color fc-statutory-color"></div>'+data.branchWiseStatutoryAlertLiveDatesData[i].title+'</div>');
				$("tr[class='fc-week'] td[data-date='"+data.branchWiseStatutoryAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseStatutoryAlertLiveDatesData[i].title+'"><div class="fc-color fc-statutory-color"></div>'+data.branchWiseStatutoryAlertLiveDatesData[i].title+'</div>');
				$("tr[class='fc-week fc-last'] td[data-date='"+data.branchWiseStatutoryAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseStatutoryAlertLiveDatesData[i].title+'"><div class="fc-color fc-statutory-color"></div>'+data.branchWiseStatutoryAlertLiveDatesData[i].title+'</div>');
				if(dateCheck(curDate,nexDate,data.branchWiseStatutoryAlertLiveDatesData[i].start)){
					if(isEmpty(upcoming[data.branchWiseStatutoryAlertLiveDatesData[i].start])){
						upcoming[data.branchWiseStatutoryAlertLiveDatesData[i].start]=data.branchWiseStatutoryAlertLiveDatesData[i].title+'@_statutory_@';
					}else{
						upcoming[data.branchWiseStatutoryAlertLiveDatesData[i].start]+=','+data.branchWiseStatutoryAlertLiveDatesData[i].title+'@_statutory_@';
					}
				}
			}
			for(var i=0;i<data.branchWiseOperationalAlertLiveDatesData.length;i++){
				$("tr[class='fc-week fc-first'] td[data-date='"+data.branchWiseOperationalAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseOperationalAlertLiveDatesData[i].title+'"><div class="fc-color fc-operational-color"></div>'+data.branchWiseOperationalAlertLiveDatesData[i].title+'</div>');
				$("tr[class='fc-week'] td[data-date='"+data.branchWiseOperationalAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseOperationalAlertLiveDatesData[i].title+'"><div class="fc-color fc-operational-color"></div>'+data.branchWiseOperationalAlertLiveDatesData[i].title+'</div>');
				$("tr[class='fc-week fc-last'] td[data-date='"+data.branchWiseOperationalAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseOperationalAlertLiveDatesData[i].title+'"><div class="fc-color fc-operational-color"></div>'+data.branchWiseOperationalAlertLiveDatesData[i].title+'</div>');
				if(dateCheck(curDate,nexDate,data.branchWiseOperationalAlertLiveDatesData[i].start)){
					if(isEmpty(upcoming[data.branchWiseOperationalAlertLiveDatesData[i].start])){
						upcoming[data.branchWiseOperationalAlertLiveDatesData[i].start]=data.branchWiseOperationalAlertLiveDatesData[i].title+'@_operational_@';
					}else{
						upcoming[data.branchWiseOperationalAlertLiveDatesData[i].start]+=','+data.branchWiseOperationalAlertLiveDatesData[i].title+'@_operational_@';
					}
				}
			}
			for(var i=0;i<data.branchWiseInsurenceAlertLiveDatesData.length;i++){
				$("tr[class='fc-week fc-first'] td[data-date='"+data.branchWiseInsurenceAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseInsurenceAlertLiveDatesData[i].title+'"><div class="fc-color fc-insurance-color"></div>'+data.branchWiseInsurenceAlertLiveDatesData[i].title+'</div>');
				$("tr[class='fc-week'] td[data-date='"+data.branchWiseInsurenceAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseInsurenceAlertLiveDatesData[i].title+'"><div class="fc-color fc-insurance-color"></div>'+data.branchWiseInsurenceAlertLiveDatesData[i].title+'</div>');
				$("tr[class='fc-week fc-last'] td[data-date='"+data.branchWiseInsurenceAlertLiveDatesData[i].start+"'] div[class='fc-day-content']").append('<div class="fc-day-view" title="'+data.branchWiseInsurenceAlertLiveDatesData[i].title+'"><div class="fc-color fc-insurance-color"></div>'+data.branchWiseInsurenceAlertLiveDatesData[i].title+'</div>');
				if(dateCheck(curDate,nexDate,data.branchWiseInsurenceAlertLiveDatesData[i].start)){
					if(isEmpty(upcoming[data.branchWiseInsurenceAlertLiveDatesData[i].start])){
						upcoming[data.branchWiseInsurenceAlertLiveDatesData[i].start]=data.branchWiseInsurenceAlertLiveDatesData[i].title+'@_insurance_@';
					}else{
						upcoming[data.branchWiseInsurenceAlertLiveDatesData[i].start]+=','+data.branchWiseInsurenceAlertLiveDatesData[i].title+'@_insurance_@';
					}
				}
			}
			var keys=Object.keys(upcoming), disDate='',content='';
			keys.sort();
			if(keys.length >=3){
				for (var i=0;i<=3;i++){
					disDate=keys[i].split('-');
					$('.ue-'+i+' .ue-date').html(disDate[2]+'<br>'+idosmonths[disDate[1]-1]+', '+disDate[0]);
					content=upcoming[keys[i]].split(',');
					$('.ue-'+i+' .ue-content').empty();
					for(var j=0;j<content.length;j++){
						if(j>0 && j!=content.length-1){
							$('.ue-'+i+' .ue-content').append('<br>');
						}
						if(/premise/i.test(content[j])){
							$('.ue-'+i+' .ue-content').append('<div title="'+content[j].split('@_premise_@')[0]+'"><div class="fc-color fc-premise-color"></div>'+content[j].split('@_premise_@')[0]+'</div>');
						}else if(/statutory/i.test(content[j])){
							$('.ue-'+i+' .ue-content').append('<div title="'+content[j].split('@_statutory_@')[0]+'"><div class="fc-color fc-statutory-color"></div>'+content[j].split('@_statutory_@')[0]+'</div>');
						}else if(/operational/i.test(content[j])){
							$('.ue-'+i+' .ue-content').append('<div title="'+content[j].split('@_operational_@')[0]+'"><div class="fc-color fc-operational-color"></div>'+content[j].split('@_operational_@')[0]+'</div>');
						}else if(/insurance/i.test(content[j])){
							$('.ue-'+i+' .ue-content').append('<div title="'+content[j].split('@_insurance_@')[0]+'"><div class="fc-color fc-insurance-color"></div>'+content[j].split('@_insurance_@')[0]+'</div>');
						}
						$('.ue-'+i).show();
					}
					($('.upcoming-events').is(':visible'))?$('#upcomingLabel').show():$('#upcomingLabel').hide();
					$('.d-opera-reminder').hide();
					$('.operationalCalendar').show();
					$('.opera-list').removeClass('selected');
					$('#dOperaMonth').addClass('selected');
				}
			}
			$.unblockUI();
		},
		error: function (xhr, status, error) {
			if(xhr.status == 401){ doLogout(); }
		}
	});
}