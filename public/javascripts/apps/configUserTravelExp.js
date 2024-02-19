var onUserScreenLoad = function (){
	//alwaysScrollTop();
	var jsonData = {};
	jsonData.userEmail = $("#hiddenuseremail").text();
	jsonData.type = "distances";
	ajaxCall('/claims/getTravelStaticData', jsonData, '', '', '', '', 'travelDistancesSuccess', '', true);
	jsonData.type = "accomodation";
	ajaxCall('/claims/getTravelStaticData', jsonData, '', '', '', '', 'travelAccomodationSuccess', '', true);
	ajaxCall('/claims/getAvailableExpenseClaimItems', jsonData, '', '', '', '', 'availabelExpenseItemsSuccess', '', true);
	ajaxCall('/claims/getAvailableTravelExpenseGroups', jsonData, '', '', '', '', 'availabelTravelExpenseGroups', '', true);
}

function travelDistancesSuccess(data) {	
	if (data.result) {
		data = data.array;
		if (data.length > 0) {
			$('#travelClaimHeadMilesKms').empty();
			for (var i = 0; i < data.length; i++) {
				$('#travelClaimHeadMilesKms').append('<th>' + data[i].mileKm + '</th>');
			}
			var jsonData = {};
			jsonData.userEmail = $("#hiddenuseremail").text();
			jsonData.type = "travel";
			//ajaxCall('/claims/getTravelStaticData', jsonData, '', '', '', '', 'modesOfTravelSuccess', '', true);
			var url="/claims/getTravelStaticData";
			$.ajax({
				url : url,
				data : JSON.stringify(jsonData),
				type : "text",
				headers:{
					"X-AUTH-TOKEN": window.authToken
				},
				method : "POST",
				async: false,
				contentType : 'application/json',
				success : function(data) {
					if (data.result) {
						data = data.array;
						if (data.length > 0) {
							$('#travelClaimBodyMilesKms').html("");
							var headerLength = $('#travelClaimHeadMilesKms').children().length;
							var bodyStr = '', left = "0px";
							for (var i = 0; i < headerLength; i++) {
								var k=i+1;
								var headerChildText=$('#travelClaimHeadMilesKms th:nth-child('+k+')').text();
								if (i === 8) {
									left = "-102px";
								} else if (i === 9) {
									left = "-202px";
								} else if (i === 10) {
									left = "-302px";
								} else if (i === 11) {
									left = "-402px";
								} else if (i === 12) {
									left = "-502px";
								}
								bodyStr += '<td><div class="btn-group claim-btn-group">'
										+ '<button id="travelClaim_' + i + '" class="multiselect dropdown-toggle btn" style="width: 130px;" onclick="customClaimsDropDownToggle(\'travelClaim_' + i + '_menuid\')"> None Selected &#8711;</button>'
										+ '<div id="travelClaim_' + i + '_menuid" class="travelClaim-menuid" style="left: ' + left + '">'
										+ '<div id="travelClaim_' + i + '_SearchDiv"><input type="text" style="width: 550px; margin: 5px;" class="search-image travelClaimSearch" name="travelClaimSearch" id="travelClaim_' + i + '_Search" placeholder="Search item" onkeyup="prependMatchingItemFirstInCustomContainer(this);"></div>'
										+ '<ul class="claim-list travelClaim_' + i + '_List travelclaimList" id="travelClaim_' + i + '_List">'
										
										+ '<li id="travelClaim_Header" class="travelClaimList">&nbsp;<span></span><span class="claimsLabel"><b>'+headerChildText+'</b></span><span class="travelClaimListHeader" style="margin-left: 10px;">Max. One Way Fare</span>&nbsp;&nbsp;<span class="travelClaimListHeader" style="margin-left: 30px;">Max. Return Fare</span></li>'

										+ '<li id="travelClaim_' + i + '_List" class="travelClaimList" title="Select All">&nbsp;<input type="checkbox" class="travelClaimDistance" name="travelClaimDistance" id="travelClaim_' + i + '" value="" onclick="claimCheckUncheck(this);"/><span class="claimsLabel">Select All</span><input type="text" style="width:140px;" name="travelClaimOneWay" id="travelClaim_' + i + '_OneWay" onkeyup="fillAllInputBasedOnSelectAllinputValue(this)" onkeypress="return onlyDotsAndNumbers(event)"/>&nbsp;&nbsp;<input type="text" style="width:140px;" name="travelClaimReturn" id="travelClaim_' + i + '_Return" onkeyup="fillAllInputBasedOnSelectAllinputValue(this);" onkeypress="return onlyDotsAndNumbers(event);"/></li>';
								for (var j = 0; j < data.length; j++) {
									bodyStr += '<li id="travelClaim_' + i + '_' + j + '_List" class="travelClaimList" title="' + data[j].name + '">&nbsp;<input type="checkbox" class="travelClaimDistance" name="travelClaimDistance" id="travelClaim_' + i + '_' + j + '" value="' + data[j].id + '" onclick="claimCheckUncheck(this);"/><span class="claimsLabel">' + data[j].name + '</span><input type="text" style="width:140px;" name="travelClaimOneWay" id="travelClaim_' + i + '_' + j + '_OneWay" value="0.0" onkeyup="claimtoggleCheck(this)" onkeypress="return onlyDotsAndNumbers(event)"/>&nbsp;&nbsp;<input type="text" style="width:140px;" name="travelClaimReturn" id="travelClaim_' + i + '_' + j + '_Return" value="0.0" onkeyup="claimtoggleCheck(this)" onkeypress="return onlyDotsAndNumbers(event)"/></li>';
								}
								bodyStr += '</ul></div></div></td>';
							}
							$('#travelClaimBodyMilesKms').append('<tr>' + bodyStr + '</tr>');
							$('#travelClaimBodyMilesKms input[name=travelClaimOneWay]').attr('placeholder', 'Max. One Way Fare');
							$('#travelClaimBodyMilesKms input[name=travelClaimReturn]').attr('placeholder', 'Max. Return Fare');
						}
					}
				},
				error: function(xhr, status, error) {
					if(xhr.status == 401){ doLogout(); 
					}else if(xhr.status == 500){
			    		swal("Error on fetching travel distances!", "Please retry, if problem persists contact support team", "error");
			    	}
				},
				complete: function(data) {
					$.unblockUI();
				}
			});
		}
	}
}

function travelAccomodationSuccess(data) {
	if (data.result) {
		data = data.array;
		if (data.length > 0) {
			var length = $('.travelClaim-max-daily-table tbody tr').children().length;
			for (var j = 0; j < length; j++) {
				var strHtml = '<li id="travelBoardingLodging_Header" class="travelClaimList">&nbsp;&nbsp;<span></span><span class="claimsLabel"><b>Boarding & Loadging Type</b></span><span class="travelBoardingListHeader">Max. room cost per night</span>&nbsp;&nbsp;<span class="travelBoardingListHeader">Max. food cost per day</span></li>';
				for (var i = 0; i < data.length; i++) {
					strHtml += '<li id="travelBoardingLodging_' + i + '_' + j + '_List" class="travelClaimList" title="' + data[i].name + '"><input type="checkbox" class="travelBoardingLodging" name="travelBoardingLodging' + '_' + j + '" id="travelBoarding_' + i  + '_' + j + '" value="'+data[i].id+'" onclick="claimCheckUncheck(this);"/><span class="claimsLabel">' + data[i].name + '</span><input type="text" name="travelBoarding_room" id="travelBoarding_food_' + i + '_' + j + '" value="0.0" onkeyup="claimtoggleCheck(this)" onkeypress="return onlyDotsAndNumbers(event)"/><input type="text" class="travelBoarding_food" name="travelBoarding_food" id="travelBoarding_food_' + i  + '_' + j + '" value="0.0" onkeyup="claimtoggleCheck(this)" onkeypress="return onlyDotsAndNumbers(event)"/></li>';
				}
				$('.travelClaim-max-daily-table tbody tr #travelBoardingLodgingList_' + j).html("");
				$('.travelClaim-max-daily-table tbody tr #travelBoardingLodgingList_' + j).append(strHtml);
			}
		}
	}
}


/*Claims Starts*/
$(document).ready(function() {
	$('#newTravelClaimConfigurationform-container').on('click', function() {
		alwaysScrollTop();
		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		jsonData.type = "distances";
		ajaxCall('/claims/getTravelStaticData', jsonData, '', '', '', '', 'travelDistancesSuccess', '', true);
		jsonData.type = "accomodation";
		ajaxCall('/claims/getTravelStaticData', jsonData, '', '', '', '', 'travelAccomodationSuccess', '', true);
		$('#claimGroup-container, #expenseClaim-container, #user-form-container, #payrollsetup-container').slideUp(400);
		$('#travelClaim-container').slideDown(600);
	});

	$('#newExpenseClaimConfigurationform-container').on('click', function() {
		alwaysScrollTop();
		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		ajaxCall('/claims/getAvailableExpenseClaimItems', jsonData, '', '', '', '', 'availabelExpenseItemsSuccess', '', true);
		$('#claimGroup-container, #travelClaim-container, #user-form-container, #payrollsetup-container').slideUp(400);
		$('#expenseClaim-container').slideDown(600);
	});

	$('.claim-container-close, #cancelTravelGroup, #cancelExpenseGroup').on('click', function() {
		alwaysScrollTop();
		$('.claim-container:visible').slideUp(600);
	});

	$('#showTravelExpenseGroupform-container').on('click', function() {
		$('#expenseClaim-container, #travelClaim-container, #user-form-container, #payrollsetup-container').slideUp(400);
		$('#claimGroup-container').slideDown(600);
		alwaysScrollTop();
		var jsonData = {};
		jsonData.userEmail = $("#hiddenuseremail").text();
		ajaxCall('/claims/getAvailableTravelExpenseGroups', jsonData, '', '', '', '', 'availabelTravelExpenseGroups', '', true);
	});
});

function availabelTravelExpenseGroups(data){
	if(data.result){
		var tdata=data.travelarray;
		if(typeof data!='undefined'){
			if(tdata.length>0){
				$(".travelclaim-group-container tbody").html("");
				$("#userTravelEligibility").children().remove();
				$("#userTravelEligibility").append('<option value="">---Please Select---</option>');
				for(var i=0;i<tdata.length;i++){
					$("#userTravelEligibility").append('<option value="'+tdata[i].id+'">'+tdata[i].name+'</option>');
					$(".travelclaim-group-container tbody").append('<tr name="travelGroupEntity'+tdata[i].id+'"><td>'+tdata[i].name+'</td><td><div class="search"><div id="search-launch" style="display: block;"><button href="#usersSetup" class="btn btn-submit" onClick="showTravelGroupEntityDetails(this)" id="show-entity-details'+tdata[i].id+'"><i class="fa fa-edit fa-lg pr-3"></i>Edit</button></div></div></td></tr>');
			}
		}
		var edata=data.expensearray;
		if(typeof data!='undefined'){
			if(edata.length>0){
				$(".expenseclaim-group-container tbody").html("");
				$("#userExpenseEligibility").children().remove();
				$("#userExpenseEligibility").append('<option value="">---Please Select---</option>');
				for(var i=0;i<edata.length;i++){
					$("#userExpenseEligibility").append('<option value="'+edata[i].id+'">'+edata[i].name+'</option>');
					$(".expenseclaim-group-container tbody").append('<tr name="expenseGroupEntity'+edata[i].id+'"><td>'+edata[i].name+'</td><td><div class="search"><div id="search-launch" style="display: block;"><button href="#usersSetup" class="btn btn-submit" onClick="showExpenseGroupEntityDetails(this)" id="show-entity-details'+edata[i].id+'"><i class="fa fa-edit fa-lg pr-3"></i>Edit</a></div></div></td></tr>');
				}
				}
			}
		}
	}
}

function availabelExpenseItemsSuccess(data) {
	if (data.result) {
		data = data.array;
		if (data.length > 0) {
			$('#expenseClaimList').empty();
			$('#expenseClaimList').append('<li id="expenseClaims_Header" class="expenseClaimsClass">&nbsp;&nbsp;<span class="claimsLabel"></span><span class="expenseClaimsHeader">Max. permitted advance</span>&nbsp;&nbsp;<span class="expenseClaimsHeader">Monthly monetary limit for reimbursement</span></li>');
			$('#expenseClaimList').append('<li id="expenseClaims_List" class="expenseClaimsClass" title="Select All">&nbsp;&nbsp;<input type="checkbox" class="expenseClaims" name="expenseClaims" id="expenseClaim_selectAll" value="" onclick="claimCheckUncheck(this);"/><span class="claimsLabel">Select All</span><input type="text" name="expenseClaim_maxAdvance" id="expenseClaim_selectAll" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="fillAllInputBasedOnSelectAllinputValue(this)"/>&nbsp;&nbsp;<input type="text" name="expenseClaim_monthlyMoney" id="expenseClaim_monthlyMoney_selectAll" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="fillAllInputBasedOnSelectAllinputValue(this)"/></li>');
			for (var i = 0; i < data.length; i++) {
				$('#expenseClaimList').append('<li id="expenseClaims_' + i + '_List" class="expenseClaimsClass" title="' + data[i].name + '">&nbsp;&nbsp;<input type="checkbox" class="expenseClaims" name="expenseClaims" id="expenseClaims' + i + '" value="' + data[i].id + '" onclick="claimCheckUncheck(this);"/><span class="claimsLabel">' + data[i].name + '</span><input type="text" name="expenseClaim_maxAdvance" id="expenseClaim_' + i + '" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="claimtoggleCheck(this)"/>&nbsp;&nbsp;<input type="text" name="expenseClaim_monthlyMoney" id="expenseClaim_' + i + '" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="claimtoggleCheck(this)"/></li>');
			}
			$('#expenseClaimList input[name=expenseClaim_maxAdvance]').attr('placeholder', 'Max. Permitted Advance');
			$('#expenseClaimList input[name=expenseClaim_monthlyMoney]').attr('placeholder', 'Monthly Monetary Limit For Reimbursement');
		}
	}
}


/*
function modesOfTravelSuccess(data) {
	if (data.result) {
		data = data.array;
		if (data.length > 0) {
			$('#travelClaimBodyMilesKms').html("");
			var headerLength = $('#travelClaimHeadMilesKms').children().length;
			var bodyStr = '', left = "0px";
			for (var i = 0; i < headerLength; i++) {
				var k=i+1;
				var headerChildText=$('#travelClaimHeadMilesKms th:nth-child('+k+')').text();
				if (i === 8) {
					left = "-102px";
				} else if (i === 9) {
					left = "-202px";
				} else if (i === 10) {
					left = "-302px";
				} else if (i === 11) {
					left = "-402px";
				} else if (i === 12) {
					left = "-502px";
				}
				bodyStr += '<td><div class="btn-group claim-btn-group">'
						+ '<button id="travelClaim_' + i + '" class="multiselect dropdown-toggle btn" style="width: 130px;" onclick="customClaimsDropDownToggle(\'travelClaim_' + i + '_menuid\')"> None Selected &#8711;</button>'
						+ '<div id="travelClaim_' + i + '_menuid" class="travelClaim-menuid" style="left: ' + left + '">'
						+ '<div id="travelClaim_' + i + '_SearchDiv"><input type="text" style="width: 550px; margin: 5px;" class="search-image travelClaimSearch" name="travelClaimSearch" id="travelClaim_' + i + '_Search" placeholder="Search item" onkeyup="prependMatchingItemFirstInCustomContainer(this);"></div>'
						+ '<ul class="claim-list travelClaim_' + i + '_List travelclaimList" id="travelClaim_' + i + '_List">'
						
						+ '<li id="travelClaim_Header" class="travelClaimList">&nbsp;<span></span><span class="claimsLabel"><b>'+headerChildText+'</b></span><span class="travelClaimListHeader" style="margin-left: 10px;">Max. One Way Fare</span>&nbsp;&nbsp;<span class="travelClaimListHeader" style="margin-left: 30px;">Max. Return Fare</span></li>'

						+ '<li id="travelClaim_' + i + '_List" class="travelClaimList" title="Select All">&nbsp;<input type="checkbox" class="travelClaimDistance" name="travelClaimDistance" id="travelClaim_' + i + '" value="" onclick="claimCheckUncheck(this);"/><span class="claimsLabel">Select All</span><input type="text" style="width:140px;" name="travelClaimOneWay" id="travelClaim_' + i + '_OneWay" onkeyup="fillAllInputBasedOnSelectAllinputValue(this)" onkeypress="return onlyDotsAndNumbers(event)"/>&nbsp;&nbsp;<input type="text" style="width:140px;" name="travelClaimReturn" id="travelClaim_' + i + '_Return" onkeyup="fillAllInputBasedOnSelectAllinputValue(this);" onkeypress="return onlyDotsAndNumbers(event);"/></li>';
				for (var j = 0; j < data.length; j++) {
					bodyStr += '<li id="travelClaim_' + i + '_' + j + '_List" class="travelClaimList" title="' + data[j].name + '">&nbsp;<input type="checkbox" class="travelClaimDistance" name="travelClaimDistance" id="travelClaim_' + i + '_' + j + '" value="' + data[j].id + '" onclick="claimCheckUncheck(this);"/><span class="claimsLabel">' + data[j].name + '</span><input type="text" style="width:140px;" name="travelClaimOneWay" id="travelClaim_' + i + '_' + j + '_OneWay" value="0.0" onkeyup="claimtoggleCheck(this)" onkeypress="return onlyDotsAndNumbers(event)"/>&nbsp;&nbsp;<input type="text" style="width:140px;" name="travelClaimReturn" id="travelClaim_' + i + '_' + j + '_Return" value="0.0" onkeyup="claimtoggleCheck(this)" onkeypress="return onlyDotsAndNumbers(event)"/></li>';
				}
				bodyStr += '</ul></div></div></td>';
			}
			$('#travelClaimBodyMilesKms').append('<tr>' + bodyStr + '</tr>');
			$('#travelClaimBodyMilesKms input[name=travelClaimOneWay]').attr('placeholder', 'Max. One Way Fare');
			$('#travelClaimBodyMilesKms input[name=travelClaimReturn]').attr('placeholder', 'Max. Return Fare');
		}
	}
}*/

function customClaimsDropDownToggle(openId){
	
	openId = $.trim(openId);
	if($('#' + openId).hasClass('opentravelClaim-menuid')) {
		$("#" + openId).removeClass('opentravelClaim-menuid');
	} else {
		$('.travelClaim-menuid').removeClass('opentravelClaim-menuid');
		$("#" + openId).addClass('opentravelClaim-menuid');
	} 
}

/*Claims Ends*/
/* Travel Claim Add/Update Start*/
$(document).ready(function(){
	$("#addUpdateTravelGroup").on('click', function() {
		var travelGroup={};
		var travelGroupName=$("#travelClaimGroupName").val();
		if(travelGroupName==""){
			swal("Error!","Please provide travel group name","error");
			$("#travelClaimGroupName").focus();
			return true;
		}
		travelGroup.tGroupName=travelGroupName;
		travelGroup.tGroupEntityHiddenId=$("#travelGroupEntityHidden").val();
		travelGroup.useremail = $("#hiddenuseremail").text();
		//boarding and lodging json data start
		//Country capital BnL
		var maxRoomCostPerNightCtryCap="";
		var maxFoodCostPerDayCtryCap="";
		var boardingAndLodgingTypeCtryCap=$("#travelBoardingLodgingList_0 input[name='travelBoardingLodging_0']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightCtryCap+=$("#travelBoardingLodgingList_0  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayCtryCap+=$("#travelBoardingLodgingList_0  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeCtryCap=boardingAndLodgingTypeCtryCap.toString();
		travelGroup.tGroupMaxRoomCostPerNightCtryCap=maxRoomCostPerNightCtryCap;
		travelGroup.tGroupMaxFoodCostPerDayCtryCap=maxFoodCostPerDayCtryCap;
		//Country capital BnL end
		//State Capital BnL start
		var maxRoomCostPerNightStateCap="";
		var maxFoodCostPerDayStateCap="";
		var boardingAndLodgingTypeStateCap=$("#travelBoardingLodgingList_1 input[name='travelBoardingLodging_1']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightStateCap+=$("#travelBoardingLodgingList_1  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayStateCap+=$("#travelBoardingLodgingList_1  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeStateCap=boardingAndLodgingTypeStateCap.toString();
		travelGroup.tGroupMaxRoomCostPerNightStateCap=maxRoomCostPerNightStateCap;
		travelGroup.tGroupMaxFoodCostPerDayStateCap=maxFoodCostPerDayStateCap;
		//State Capital BnL End
		//Metro City BnL start
		var maxRoomCostPerNightMetroCity="";
		var maxFoodCostPerDayMetroCity="";
		var boardingAndLodgingTypeMetroCity=$("#travelBoardingLodgingList_2 input[name='travelBoardingLodging_2']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightMetroCity+=$("#travelBoardingLodgingList_2  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayMetroCity+=$("#travelBoardingLodgingList_2  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeMetroCity=boardingAndLodgingTypeMetroCity.toString();
		travelGroup.tGroupMaxRoomCostPerNightMetroCity=maxRoomCostPerNightMetroCity;
		travelGroup.tGroupMaxFoodCostPerDayMetroCity=maxFoodCostPerDayMetroCity;
		//Metro City BnL End
		//Other Cities BnL start
		var maxRoomCostPerNightOtherCity="";
		var maxFoodCostPerDayOtherCity="";
		var boardingAndLodgingTypeOtherCity=$("#travelBoardingLodgingList_3 input[name='travelBoardingLodging_3']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightOtherCity+=$("#travelBoardingLodgingList_3  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayOtherCity+=$("#travelBoardingLodgingList_3  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeOtherCity=boardingAndLodgingTypeOtherCity.toString();
		travelGroup.tGroupMaxRoomCostPerNightOtherCity=maxRoomCostPerNightOtherCity;
		travelGroup.tGroupMaxFoodCostPerDayOtherCity=maxFoodCostPerDayOtherCity;
		//Other Cities BnL End
		//Town BnL start
		var maxRoomCostPerNightTown="";
		var maxFoodCostPerDayTown="";
		var boardingAndLodgingTypeTown=$("#travelBoardingLodgingList_4 input[name='travelBoardingLodging_4']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightTown+=$("#travelBoardingLodgingList_4  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayTown+=$("#travelBoardingLodgingList_4  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeTown=boardingAndLodgingTypeTown.toString();
		travelGroup.tGroupMaxRoomCostPerNightTown=maxRoomCostPerNightTown;
		travelGroup.tGroupMaxFoodCostPerDayTown=maxFoodCostPerDayTown;
		//Town BnL End
		//Country BnL start
		var maxRoomCostPerNightCountry="";
		var maxFoodCostPerDayCountry="";
		var boardingAndLodgingTypeCountry=$("#travelBoardingLodgingList_5 input[name='travelBoardingLodging_5']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightCountry+=$("#travelBoardingLodgingList_5  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayCountry+=$("#travelBoardingLodgingList_5  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeCountry=boardingAndLodgingTypeCountry.toString();
		travelGroup.tGroupMaxRoomCostPerNightCountry=maxRoomCostPerNightCountry;
		travelGroup.tGroupMaxFoodCostPerDayCountry=maxFoodCostPerDayCountry;
		//Country BnL End
		//Municipality BnL start
		var maxRoomCostPerNightMunicipality="";
		var maxFoodCostPerDayMunicipality="";
		var boardingAndLodgingTypeMunicipality=$("#travelBoardingLodgingList_6 input[name='travelBoardingLodging_6']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightMunicipality+=$("#travelBoardingLodgingList_6  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayMunicipality+=$("#travelBoardingLodgingList_6  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeMunicipality=boardingAndLodgingTypeMunicipality.toString();
		travelGroup.tGroupMaxRoomCostPerNightMunicipality=maxRoomCostPerNightMunicipality;
		travelGroup.tGroupMaxFoodCostPerDayMunicipality=maxFoodCostPerDayMunicipality;
		//Municipality BnL End
		//Village BnL start
		var maxRoomCostPerNightVillage="";
		var maxFoodCostPerDayVillage="";
		var boardingAndLodgingTypeVillage=$("#travelBoardingLodgingList_7 input[name='travelBoardingLodging_7']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightVillage+=$("#travelBoardingLodgingList_7  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayVillage+=$("#travelBoardingLodgingList_7  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeVillage=boardingAndLodgingTypeVillage.toString();
		travelGroup.tGroupMaxRoomCostPerNightVillage=maxRoomCostPerNightVillage;
		travelGroup.tGroupMaxFoodCostPerDayVillage=maxFoodCostPerDayVillage;
		//Village BnL End
		//Remote Location BnL start
		var maxRoomCostPerNightRemoteLoc="";
		var maxFoodCostPerDayRemoteLoc="";
		var boardingAndLodgingTypeRemoteLoc=$("#travelBoardingLodgingList_8 input[name='travelBoardingLodging_8']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightRemoteLoc+=$("#travelBoardingLodgingList_8  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayRemoteLoc+=$("#travelBoardingLodgingList_8  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeRemoteLoc=boardingAndLodgingTypeRemoteLoc.toString();
		travelGroup.tGroupMaxRoomCostPerNightRemoteLoc=maxRoomCostPerNightRemoteLoc;
		travelGroup.tGroupMaxFoodCostPerDayRemoteLoc=maxFoodCostPerDayRemoteLoc;
		//Remote Location BnL End
		//20 Miles Away From City Or Town BnL start
		var maxRoomCostPerNight20Miles="";
		var maxFoodCostPerDay20Miles="";
		var boardingAndLodgingType20Miles=$("#travelBoardingLodgingList_9 input[name='travelBoardingLodging_9']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNight20Miles+=$("#travelBoardingLodgingList_9  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDay20Miles+=$("#travelBoardingLodgingList_9  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingType20Miles=boardingAndLodgingType20Miles.toString();
		travelGroup.tGroupMaxRoomCostPerNight20Miles=maxRoomCostPerNight20Miles;
		travelGroup.tGroupMaxFoodCostPerDay20Miles=maxFoodCostPerDay20Miles;
		//20 Miles Away From City Or Town BnL End
		//Hill Station BnL start
		var maxRoomCostPerNightHillStation="";
		var maxFoodCostPerDayHillStation="";
		var boardingAndLodgingTypeHillStation=$("#travelBoardingLodgingList_10 input[name='travelBoardingLodging_10']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightHillStation+=$("#travelBoardingLodgingList_10  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayHillStation+=$("#travelBoardingLodgingList_10  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeHillStation=boardingAndLodgingTypeHillStation.toString();
		travelGroup.tGroupMaxRoomCostPerNightHillStation=maxRoomCostPerNightHillStation;
		travelGroup.tGroupMaxFoodCostPerDayHillStation=maxFoodCostPerDayHillStation;
		//Hill Station BnL End
		//Resort BnL start
		var maxRoomCostPerNightResort="";
		var maxFoodCostPerDayResort="";
		var boardingAndLodgingTypeResort=$("#travelBoardingLodgingList_11 input[name='travelBoardingLodging_11']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightResort+=$("#travelBoardingLodgingList_11  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayResort+=$("#travelBoardingLodgingList_11  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeResort=boardingAndLodgingTypeResort.toString();
		travelGroup.tGroupMaxRoomCostPerNightResort=maxRoomCostPerNightResort;
		travelGroup.tGroupMaxFoodCostPerDayResort=maxFoodCostPerDayResort;
		//Resort BnL End
		//Place Of Conflict Or Warzone BnL start
		var maxRoomCostPerNightConflictWar="";
		var maxFoodCostPerDayConflictWar="";
		var boardingAndLodgingTypeConflictWar=$("#travelBoardingLodgingList_12 input[name='travelBoardingLodging_12']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				maxRoomCostPerNightConflictWar+=$("#travelBoardingLodgingList_12  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				maxFoodCostPerDayConflictWar+=$("#travelBoardingLodgingList_12  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupBoardingLodgingTypeConflictWar=boardingAndLodgingTypeConflictWar.toString();
		travelGroup.tGroupMaxRoomCostPerNightConflictWar=maxRoomCostPerNightConflictWar;
		travelGroup.tGroupMaxFoodCostPerDayConflictWar=maxFoodCostPerDayConflictWar;
		//Place Of Conflict Or Warzone BnL End
		//boarding and lodging json data end
		//less than 100 kms travel mode distance miles json data start
		var lessThanHundredMaxOneWayFare="";
		var lessThanHundredMaxReturnFare="";
		var lessThanHundredModesOfTravel=$("#travelClaim_0_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				lessThanHundredMaxOneWayFare+=$("#travelClaim_0_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				lessThanHundredMaxReturnFare+=$("#travelClaim_0_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGrouplessThanHundredModesOfTravel=lessThanHundredModesOfTravel.toString();
		travelGroup.tGrouplessThanHundredMaxOneWayFare=lessThanHundredMaxOneWayFare;
		travelGroup.tGrouplessThanHundredMaxReturnFare=lessThanHundredMaxReturnFare;
		//less than 100 kms travel mode distance miles json data end
		//100-250 kms travel mode distance miles json data start
		var hundredToTwoFiftyMaxOneWayFare="";
		var hundredToTwoFiftyMaxReturnFare="";
		var hundredToTwoFiftyModesOfTravel=$("#travelClaim_1_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				hundredToTwoFiftyMaxOneWayFare+=$("#travelClaim_1_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				hundredToTwoFiftyMaxReturnFare+=$("#travelClaim_1_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGrouphundredToTwoFiftyModesOfTravel=hundredToTwoFiftyModesOfTravel.toString();
		travelGroup.tGrouphundredToTwoFiftyMaxOneWayFare=hundredToTwoFiftyMaxOneWayFare;
		travelGroup.tGrouphundredToTwoFiftyMaxReturnFare=hundredToTwoFiftyMaxReturnFare;
		//100-250 kms travel mode distance miles json data end
		//250-500 kms travel mode distance miles json data start
		var twoFiftyToFiveHundredMaxOneWayFare="";
		var twoFiftyToFiveHundredMaxReturnFare="";
		var twoFiftyToFiveHundredModesOfTravel=$("#travelClaim_2_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				twoFiftyToFiveHundredMaxOneWayFare+=$("#travelClaim_2_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				twoFiftyToFiveHundredMaxReturnFare+=$("#travelClaim_2_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGrouptwoFiftyToFiveHundredModesOfTravel=twoFiftyToFiveHundredModesOfTravel.toString();
		travelGroup.tGrouptwoFiftyToFiveHundredMaxOneWayFare=twoFiftyToFiveHundredMaxOneWayFare;
		travelGroup.tGrouptwoFiftyToFiveHundredMaxReturnFare=twoFiftyToFiveHundredMaxReturnFare;
		//250-500 kms travel mode distance miles json data end
		//500-1000 kms travel mode distance miles json data start
		var fiveHundredToThousandMaxOneWayFare="";
		var fiveHundredToThousandMaxReturnFare="";
		var fiveHundredToThousandModesOfTravel=$("#travelClaim_3_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				fiveHundredToThousandMaxOneWayFare+=$("#travelClaim_3_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				fiveHundredToThousandMaxReturnFare+=$("#travelClaim_3_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupfiveHundredToThousandModesOfTravel=fiveHundredToThousandModesOfTravel.toString();
		travelGroup.tGroupfiveHundredToThousandMaxOneWayFare=fiveHundredToThousandMaxOneWayFare;
		travelGroup.tGroupfiveHundredToThousandMaxReturnFare=fiveHundredToThousandMaxReturnFare;
		//500-1000 kms travel mode distance miles json data end
		//1000-1500 kms travel mode distance miles json data start
		var thousandToThousandFiveHundredMaxOneWayFare="";
		var thousandToThousandFiveHundredMaxReturnFare="";
		var thousandToThousandFiveHundredModesOfTravel=$("#travelClaim_4_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				thousandToThousandFiveHundredMaxOneWayFare+=$("#travelClaim_4_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				thousandToThousandFiveHundredMaxReturnFare+=$("#travelClaim_4_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupthousandToThousandFiveHundredModesOfTravel=thousandToThousandFiveHundredModesOfTravel.toString();
		travelGroup.tGroupthousandToThousandFiveHundredMaxOneWayFare=thousandToThousandFiveHundredMaxOneWayFare;
		travelGroup.tGroupthousandToThousandFiveHundredMaxReturnFare=thousandToThousandFiveHundredMaxReturnFare;
		//1000-1500 kms travel mode distance miles json data end
		//1500-2000 kms travel mode distance miles json data start
		var thousandFiveHundredToTwoThousandMaxOneWayFare="";
		var thousandFiveHundredToTwoThousandMaxReturnFare="";
		var thousandFiveHundredToTwoThousandModesOfTravel=$("#travelClaim_5_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				thousandFiveHundredToTwoThousandMaxOneWayFare+=$("#travelClaim_5_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				thousandFiveHundredToTwoThousandMaxReturnFare+=$("#travelClaim_5_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupthousandFiveHundredToTwoThousandModesOfTravel=thousandFiveHundredToTwoThousandModesOfTravel.toString();
		travelGroup.tGroupthousandFiveHundredToTwoThousandMaxOneWayFare=thousandFiveHundredToTwoThousandMaxOneWayFare;
		travelGroup.tGroupthousandFiveHundredToTwoThousandMaxReturnFare=thousandFiveHundredToTwoThousandMaxReturnFare;
		//1500-2000 kms travel mode distance miles json data end
		//2000-2500 kms travel mode distance miles json data start
		var twoThousandToTwoThousandFiveHundredMaxOneWayFare="";
		var twoThousandToTwoThousandFiveHundredMaxReturnFare="";
		var twoThousandToTwoThousandFiveHundredModesOfTravel=$("#travelClaim_6_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				twoThousandToTwoThousandFiveHundredMaxOneWayFare+=$("#travelClaim_6_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				twoThousandToTwoThousandFiveHundredMaxReturnFare+=$("#travelClaim_6_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel=twoThousandToTwoThousandFiveHundredModesOfTravel.toString();
		travelGroup.tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare=twoThousandToTwoThousandFiveHundredMaxOneWayFare;
		travelGroup.tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare=twoThousandToTwoThousandFiveHundredMaxReturnFare;
		//2000-2500 kms travel mode distance miles json data end
		//2500-3000 kms travel mode distance miles json data start
		var twoThousandFiveHundredToThreeThousandMaxOneWayFare="";
		var twoThousandFiveHundredToThreeThousandMaxReturnFare="";
		var twoThousandFiveHundredToThreeThousandModesOfTravel=$("#travelClaim_7_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				twoThousandFiveHundredToThreeThousandMaxOneWayFare+=$("#travelClaim_7_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				twoThousandFiveHundredToThreeThousandMaxReturnFare+=$("#travelClaim_7_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel=twoThousandFiveHundredToThreeThousandModesOfTravel.toString();
		travelGroup.tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare=twoThousandFiveHundredToThreeThousandMaxOneWayFare;
		travelGroup.tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare=twoThousandFiveHundredToThreeThousandMaxReturnFare;
		//2500-3000 kms travel mode distance miles json data end
		//3000-4000 kms travel mode distance miles json data start
		var threeThousandToFourThousandMaxOneWayFare="";
		var threeThousandToFourThousandMaxReturnFare="";
		var threeThousandToFourThousandModesOfTravel=$("#travelClaim_8_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				threeThousandToFourThousandMaxOneWayFare+=$("#travelClaim_8_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				threeThousandToFourThousandMaxReturnFare+=$("#travelClaim_8_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupthreeThousandToFourThousandModesOfTravel=threeThousandToFourThousandModesOfTravel.toString();
		travelGroup.tGroupthreeThousandToFourThousandMaxOneWayFare=threeThousandToFourThousandMaxOneWayFare;
		travelGroup.tGroupthreeThousandToFourThousandMaxReturnFare=threeThousandToFourThousandMaxReturnFare;
		//3000-4000 kms travel mode distance miles json data end
		//4000-5000 kms travel mode distance miles json data start
		var fourToFiveThousandMaxOneWayFare="";
		var fourToFiveThousandMaxReturnFare="";
		var fourToFiveThousandModesOfTravel=$("#travelClaim_9_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				fourToFiveThousandMaxOneWayFare+=$("#travelClaim_9_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				fourToFiveThousandMaxReturnFare+=$("#travelClaim_9_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupfourToFiveThousandModesOfTravel=fourToFiveThousandModesOfTravel.toString();
		travelGroup.tGroupfourToFiveThousandMaxOneWayFare=fourToFiveThousandMaxOneWayFare;
		travelGroup.tGroupfourToFiveThousandMaxReturnFare=fourToFiveThousandMaxReturnFare;
		//4000-5000 kms travel mode distance miles json data end
		//5000-6000 kms travel mode distance miles json data start
		var fiveToSixThousandMaxOneWayFare="";
		var fiveToSixThousandMaxReturnFare="";
		var fiveToSixThousandModesOfTravel=$("#travelClaim_10_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				fiveToSixThousandMaxOneWayFare+=$("#travelClaim_10_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				fiveToSixThousandMaxReturnFare+=$("#travelClaim_10_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupfiveToSixThousandModesOfTravel=fiveToSixThousandModesOfTravel.toString();
		travelGroup.tGroupfiveToSixThousandMaxOneWayFare=fiveToSixThousandMaxOneWayFare;
		travelGroup.tGroupfiveToSixThousandMaxReturnFare=fiveToSixThousandMaxReturnFare;
		//5000-6000 kms travel mode distance miles json data end
		//6000-7000 kms travel mode distance miles json data start
		var sixToSevenThousandMaxOneWayFare="";
		var sixToSevenThousandMxReturnFare="";
		var sixToSevenThousandModesOfTravel=$("#travelClaim_11_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				sixToSevenThousandMaxOneWayFare+=$("#travelClaim_11_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				sixToSevenThousandMxReturnFare+=$("#travelClaim_11_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupsixToSevenThousandModesOfTravel=sixToSevenThousandModesOfTravel.toString();
		travelGroup.tGroupsixToSevenThousandMaxOneWayFare=sixToSevenThousandMaxOneWayFare;
		travelGroup.tGroupsixToSevenThousandMxReturnFare=sixToSevenThousandMxReturnFare;
		//6000-7000 kms travel mode distance miles json data end
		//Above 7000 kms travel mode distance miles json data start
		var aboveSevenThousandMaxOneWayFare="";
		var aboveSevenThousandMaxReturnFare="";
		var aboveSevenThousandModesOfTravel=$("#travelClaim_12_List input[name='travelClaimDistance']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				aboveSevenThousandMaxOneWayFare+=$("#travelClaim_12_List  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				aboveSevenThousandMaxReturnFare+=$("#travelClaim_12_List  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		travelGroup.tGroupaboveSevenThousandModesOfTravel=aboveSevenThousandModesOfTravel.toString();
		travelGroup.tGroupaboveSevenThousandMaxOneWayFare=aboveSevenThousandMaxOneWayFare;
		travelGroup.tGroupaboveSevenThousandMaxReturnFare=aboveSevenThousandMaxReturnFare;
		//Above 7000 kms travel mode distance miles json data end
		//Max Daily Limit for other expenses json data start
		travelGroup.tGroupCtryCapital=$("#travelMaxCountryCapital").val();
		travelGroup.tGroupStateCapital=$("#travelMaxStateCapital").val();
		travelGroup.tGrouptravelMaxMetroCity=$("#travelMaxMetroCity").val();
		travelGroup.tGrouptravelMaxOtherCity=$("#travelMaxOtherCity").val();
		travelGroup.tGrouptravelMaxTown=$("#travelMaxTown").val();
		travelGroup.tGrouptravelMaxCountry=$("#travelMaxCountry").val();
		travelGroup.tGrouptravelMaxMunicipality=$("#travelMaxMunicipality").val();
		travelGroup.tGrouptravelMaxVillage=$("#travelMaxVillage").val();
		travelGroup.tGrouptravelMaxRemoteLoc=$("#travelMaxRemoteLoc").val();
		travelGroup.tGrouptravelMaxAwayCityTown=$("#travelMaxAwayCityTown").val();
		travelGroup.tGrouptravelMaxHillStation=$("#travelMaxHillStation").val();
		travelGroup.tGrouptravelMaxresort=$("#travelMaxresort").val();
		travelGroup.tGrouptravelMaxWarZone=$("#travelMaxWarZone").val();
		//Max Daily Limit for other expenses json data end
		//Daily per diam for other expenses json data start
		travelGroup.tGroupfixedCountryCapital=$("#fixedCountryCapital").val();
		travelGroup.tGroupfixedStateCapital=$("#fixedStateCapital").val();
		travelGroup.tGroupfixedMetroCity=$("#fixedMetroCity").val();
		travelGroup.tGrouptravelfixedOtherCity=$("#fixedOtherCity").val();
		travelGroup.tGroupfixedTown=$("#fixedTown").val();
		travelGroup.tGroupfixedCountry=$("#fixedCountry").val();
		travelGroup.tGroupfixedMunicipality=$("#fixedMunicipality").val();
		travelGroup.tGroupfixedVillage=$("#fixedVillage").val();
		travelGroup.tGroupfixedRemoteLoc=$("#fixedRemoteLoc").val();
		travelGroup.tGroupfixedAwayCityTown=$("#fixedAwayCityTown").val();
		travelGroup.tGroupfixedHillStation=$("#fixedHillStation").val();
		travelGroup.tGroupfixedresort=$("#fixedresort").val();
		travelGroup.tGroupfixedWarZone=$("#fixedWarZone").val();
		//Daily per diam for other expenses json data end
		var travelGroupKlRemarks=$("textarea[name='travelKlremarks']").map(function(){
			return this.value;
		}).get().toString();
		var travelGroupKlMandatory=$("select[name='travelKlMandatory']").map(function(){
			return this.value;
		}).get().toString();
		travelGroup.tGroupKlRemarks=travelGroupKlRemarks;
		travelGroup.tGroupKlMandatory=travelGroupKlMandatory;
		ajaxCall('/claims/createTravelGroup', travelGroup, '', '', '', '', 'travelGroupSucess', '', true);
	});
});

function travelGroupSucess(){
	$("#notificationMessage").html("Travel Groups has been added/Updated successfully.");
	$('#showTravelExpenseGroupform-container').trigger('click');
}

function showTravelGroupEntityDetails(elem){
	$("#newTravelClaimConfigurationform-container").trigger('click');
	$("#travelClaimGroupName").val("");
	$("#travelGroupEntityHidden").val("");
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	var jsonData = {};
	jsonData.userEmail = $("#hiddenuseremail").text();
	jsonData.tGroupEntityId=origEntityId;
	ajaxCall('/claims/showTravelGroup', jsonData, '', '', '', '', 'travelGroupEditSucess', '', true);
}

function travelGroupEditSucess(data){
	if(data.result){
		var data=data.travelGroupEntityDetails;
		for(var i=0;i<data.length;i++){
			var travelGroupEntityId=data[i].tGroupId;
			var travelGroupTravelGroupName=data[i].tGroupGroupName;
			$("#travelGroupEntityHidden").val(travelGroupEntityId);
			$("#travelClaimGroupName").val(travelGroupTravelGroupName);
			var tGroupBoardingLodgingTypeCtryCap=data[i].tGroupBoardingLodgingTypeCtryCap.split(",");
			var tGroupMaxRoomPerDayCtryCap=data[i].tGroupMaxRoomCostPerNightCtryCap.split(",");
			var tGroupMaxFoodCostPerDay=data[i].tGroupMaxFoodCostPerDayCtryCap.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeCtryCap.length;j++){
				var parentLi=$("#travelBoardingLodgingList_0 input[name='travelBoardingLodging_0'][value='"+tGroupBoardingLodgingTypeCtryCap[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_0 li[id='"+parentLi+"'] input[name='travelBoardingLodging_0'][value='"+tGroupBoardingLodgingTypeCtryCap[j]+"']").prop("checked",true);
				$("#travelBoardingLodgingList_0 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomPerDayCtryCap[j]);
				$("#travelBoardingLodgingList_0 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDay[j]);
				if(tGroupBoardingLodgingTypeCtryCap[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_0").html("");
					$("#travelBoardingLodgingDropDown_0").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeStateCap=data[i].tGroupBoardingLodgingTypeStateCap.split(",");
			var tGroupMaxRoomCostPerNightStateCap=data[i].tGroupMaxRoomCostPerNightStateCap.split(",");
			var tGroupMaxFoodCostPerDayStateCap=data[i].tGroupMaxFoodCostPerDayStateCap.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeStateCap.length;j++){
				var parentLi=$("#travelBoardingLodgingList_1 input[name='travelBoardingLodging_1'][value='"+tGroupBoardingLodgingTypeStateCap[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_1 li[id='"+parentLi+"'] input[name='travelBoardingLodging_1'][value='"+tGroupBoardingLodgingTypeStateCap[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_1 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightStateCap[j]);
				$("#travelBoardingLodgingList_1 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayStateCap[j]);
				if(tGroupBoardingLodgingTypeStateCap[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_1").html("");
					$("#travelBoardingLodgingDropDown_1").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeMetroCity=data[i].tGroupBoardingLodgingTypeMetroCity.split(",");
			var tGroupMaxRoomCostPerNightMetroCity=data[i].tGroupMaxRoomCostPerNightMetroCity.split(",");
			var tGroupMaxFoodCostPerDayMetroCity=data[i].tGroupMaxFoodCostPerDayMetroCity.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeMetroCity.length;j++){
				var parentLi=$("#travelBoardingLodgingList_2 input[name='travelBoardingLodging_2'][value='"+tGroupBoardingLodgingTypeMetroCity[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_2 li[id='"+parentLi+"'] input[name='travelBoardingLodging_2'][value='"+tGroupBoardingLodgingTypeMetroCity[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_2 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightMetroCity[j]);
				$("#travelBoardingLodgingList_2 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayMetroCity[j]);
				if(tGroupBoardingLodgingTypeMetroCity[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_2").html("");
					$("#travelBoardingLodgingDropDown_2").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeOtherCity=data[i].tGroupBoardingLodgingTypeOtherCity.split(",");
			var tGroupMaxRoomCostPerNightOtherCity=data[i].tGroupMaxRoomCostPerNightOtherCity.split(",");
			var tGroupMaxFoodCostPerDayOtherCity=data[i].tGroupMaxFoodCostPerDayOtherCity.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeOtherCity.length;j++){
				var parentLi=$("#travelBoardingLodgingList_3 input[name='travelBoardingLodging_3'][value='"+tGroupBoardingLodgingTypeOtherCity[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_3 li[id='"+parentLi+"'] input[name='travelBoardingLodging_3'][value='"+tGroupBoardingLodgingTypeOtherCity[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_3 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightOtherCity[j]);
				$("#travelBoardingLodgingList_3 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayOtherCity[j]);
				if(tGroupBoardingLodgingTypeOtherCity[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_3").html("");
					$("#travelBoardingLodgingDropDown_3").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeTown=data[i].tGroupBoardingLodgingTypeTown.split(",");
			var tGroupMaxRoomCostPerNightTown=data[i].tGroupMaxRoomCostPerNightTown.split(",");
			var tGroupMaxFoodCostPerDayTown=data[i].tGroupMaxFoodCostPerDayTown.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeTown.length;j++){
				var parentLi=$("#travelBoardingLodgingList_4 input[name='travelBoardingLodging_4'][value='"+tGroupBoardingLodgingTypeTown[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_4 li[id='"+parentLi+"'] input[name='travelBoardingLodging_4'][value='"+tGroupBoardingLodgingTypeTown[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_4 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightTown[j]);
				$("#travelBoardingLodgingList_4 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayTown[j]);
				if(tGroupBoardingLodgingTypeTown[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_4").html("");
					$("#travelBoardingLodgingDropDown_4").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeCountry=data[i].tGroupBoardingLodgingTypeCountry.split(",");
			var tGroupMaxRoomCostPerNightCountry=data[i].tGroupMaxRoomCostPerNightCountry.split(",");
			var tGroupMaxFoodCostPerDayCountry=data[i].tGroupMaxFoodCostPerDayCountry.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeCountry.length;j++){
				var parentLi=$("#travelBoardingLodgingList_5 input[name='travelBoardingLodging_5'][value='"+tGroupBoardingLodgingTypeCountry[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_5 li[id='"+parentLi+"'] input[name='travelBoardingLodging_5'][value='"+tGroupBoardingLodgingTypeCountry[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_5 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightCountry[j]);
				$("#travelBoardingLodgingList_5 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayCountry[j]);
				if(tGroupBoardingLodgingTypeCountry[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_5").html("");
					$("#travelBoardingLodgingDropDown_5").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeMunicipality=data[i].tGroupBoardingLodgingTypeMunicipality.split(",");
			var tGroupMaxRoomCostPerNightMunicipality=data[i].tGroupMaxRoomCostPerNightMunicipality.split(",");
			var tGroupMaxFoodCostPerDayMunicipality=data[i].tGroupMaxFoodCostPerDayMunicipality.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeMunicipality.length;j++){
				var parentLi=$("#travelBoardingLodgingList_6 input[name='travelBoardingLodging_6'][value='"+tGroupBoardingLodgingTypeMunicipality[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_6 li[id='"+parentLi+"'] input[name='travelBoardingLodging_6'][value='"+tGroupBoardingLodgingTypeMunicipality[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_6 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightMunicipality[j]);
				$("#travelBoardingLodgingList_6 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayMunicipality[j]);
				if(tGroupBoardingLodgingTypeMunicipality[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_6").html("");
					$("#travelBoardingLodgingDropDown_6").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeVillage=data[i].tGroupBoardingLodgingTypeVillage.split(",");
			var tGroupMaxRoomCostPerNightVillage=data[i].tGroupMaxRoomCostPerNightVillage.split(",");
			var tGroupMaxFoodCostPerDayVillage=data[i].tGroupMaxFoodCostPerDayVillage.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeVillage.length;j++){
				var parentLi=$("#travelBoardingLodgingList_7 input[name='travelBoardingLodging_7'][value='"+tGroupBoardingLodgingTypeVillage[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_7 li[id='"+parentLi+"'] input[name='travelBoardingLodging_7'][value='"+tGroupBoardingLodgingTypeVillage[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_7 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightVillage[j]);
				$("#travelBoardingLodgingList_7 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayVillage[j]);
				if(tGroupBoardingLodgingTypeVillage[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_7").html("");
					$("#travelBoardingLodgingDropDown_7").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeRemoteLoc=data[i].tGroupBoardingLodgingTypeRemoteLoc.split(",");
			var tGroupMaxRoomCostPerNightRemoteLoc=data[i].tGroupMaxRoomCostPerNightRemoteLoc.split(",");
			var tGroupMaxFoodCostPerDayRemoteLoc=data[i].tGroupMaxFoodCostPerDayRemoteLoc.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeRemoteLoc.length;j++){
				var parentLi=$("#travelBoardingLodgingList_8 input[name='travelBoardingLodging_8'][value='"+tGroupBoardingLodgingTypeRemoteLoc[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_8 li[id='"+parentLi+"'] input[name='travelBoardingLodging_8'][value='"+tGroupBoardingLodgingTypeRemoteLoc[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_8 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightRemoteLoc[j]);
				$("#travelBoardingLodgingList_8 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayRemoteLoc[j]);
				if(tGroupBoardingLodgingTypeRemoteLoc[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_8").html("");
					$("#travelBoardingLodgingDropDown_8").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingType20Miles=data[i].tGroupBoardingLodgingType20Miles.split(",");
			var tGroupMaxRoomCostPerNight20Miles=data[i].tGroupMaxRoomCostPerNight20Miles.split(",");
			var tGroupMaxFoodCostPerDay20Miles=data[i].tGroupMaxFoodCostPerDay20Miles.split(",");
			for(var j=0;j<tGroupBoardingLodgingType20Miles.length;j++){
				var parentLi=$("#travelBoardingLodgingList_9 input[name='travelBoardingLodging_9'][value='"+tGroupBoardingLodgingType20Miles[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_9 li[id='"+parentLi+"'] input[name='travelBoardingLodging_9'][value='"+tGroupBoardingLodgingType20Miles[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_9 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNight20Miles[j]);
				$("#travelBoardingLodgingList_9 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDay20Miles[j]);
				if(tGroupBoardingLodgingType20Miles[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_9").html("");
					$("#travelBoardingLodgingDropDown_9").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeHillStation=data[i].tGroupBoardingLodgingTypeHillStation.split(",");
			var tGroupMaxRoomCostPerNightHillStation=data[i].tGroupMaxRoomCostPerNightHillStation.split(",");
			var tGroupMaxFoodCostPerDayHillStation=data[i].tGroupMaxFoodCostPerDayHillStation.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeHillStation.length;j++){
				var parentLi=$("#travelBoardingLodgingList_10 input[name='travelBoardingLodging_10'][value='"+tGroupBoardingLodgingTypeHillStation[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_10 li[id='"+parentLi+"'] input[name='travelBoardingLodging_10'][value='"+tGroupBoardingLodgingTypeHillStation[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_10 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightHillStation[j]);
				$("#travelBoardingLodgingList_10 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayHillStation[j]);
				if(tGroupBoardingLodgingTypeHillStation[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_10").html("");
					$("#travelBoardingLodgingDropDown_10").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeResort=data[i].tGroupBoardingLodgingTypeResort.split(",");
			var tGroupMaxRoomCostPerNightResort=data[i].tGroupMaxRoomCostPerNightResort.split(",");
			var tGroupMaxFoodCostPerDayResort=data[i].tGroupMaxFoodCostPerDayResort.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeResort.length;j++){
				var parentLi=$("#travelBoardingLodgingList_11 input[name='travelBoardingLodging_11'][value='"+tGroupBoardingLodgingTypeResort[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_11 li[id='"+parentLi+"'] input[name='travelBoardingLodging_11'][value='"+tGroupBoardingLodgingTypeResort[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_11 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightResort[j]);
				$("#travelBoardingLodgingList_11 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayResort[j]);
				if(tGroupBoardingLodgingTypeResort[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_11").html("");
					$("#travelBoardingLodgingDropDown_11").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupBoardingLodgingTypeConflictWar=data[i].tGroupBoardingLodgingTypeConflictWar.split(",");
			var tGroupMaxRoomCostPerNightConflictWar=data[i].tGroupMaxRoomCostPerNightConflictWar.split(",");
			var tGroupMaxFoodCostPerDayConflictWar=data[i].tGroupMaxFoodCostPerDayConflictWar.split(",");
			for(var j=0;j<tGroupBoardingLodgingTypeConflictWar.length;j++){
				var parentLi=$("#travelBoardingLodgingList_12 input[name='travelBoardingLodging_12'][value='"+tGroupBoardingLodgingTypeConflictWar[j]+"']").parent().attr('id');
				$("#travelBoardingLodgingList_12 li[id='"+parentLi+"'] input[name='travelBoardingLodging_12'][value='"+tGroupBoardingLodgingTypeConflictWar[j]+"']").prop("checked","checked");
				$("#travelBoardingLodgingList_12 li[id='"+parentLi+"'] input[name='travelBoarding_room']").val(tGroupMaxRoomCostPerNightConflictWar[j]);
				$("#travelBoardingLodgingList_12 li[id='"+parentLi+"'] input[name='travelBoarding_food']").val(tGroupMaxFoodCostPerDayConflictWar[j]);
				if(tGroupBoardingLodgingTypeConflictWar[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelBoardingLodgingDropDown_12").html("");
					$("#travelBoardingLodgingDropDown_12").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGrouplessThanHundredModesOfTravel=data[i].tGrouplessThanHundredModesOfTravel.split(",");
			var tGrouplessThanHundredMaxOneWayFare=data[i].tGrouplessThanHundredMaxOneWayFare.split(",");
			var tGrouplessThanHundredMaxReturnFare=data[i].tGrouplessThanHundredMaxReturnFare.split(",");
			for(var j=0;j<tGrouplessThanHundredModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_0_List input[name='travelClaimDistance'][value='"+tGrouplessThanHundredModesOfTravel[j]+"']").parent().attr('id');
				
				$("#travelClaim_0_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGrouplessThanHundredMaxOneWayFare[j]);
				$("#travelClaim_0_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGrouplessThanHundredMaxReturnFare[j]);
				if(tGrouplessThanHundredModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_0").html("");
					$("#travelClaim_0").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_0_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGrouplessThanHundredModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGrouphundredToTwoFiftyModesOfTravel=data[i].tGrouphundredToTwoFiftyModesOfTravel.split(",");
			var tGrouphundredToTwoFiftyMaxOneWayFare=data[i].tGrouphundredToTwoFiftyMaxOneWayFare.split(",");
			var tGrouphundredToTwoFiftyMaxReturnFare=data[i].tGrouphundredToTwoFiftyMaxReturnFare.split(",");
			for(var j=0;j<tGrouphundredToTwoFiftyModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_1_List input[name='travelClaimDistance'][value='"+tGrouphundredToTwoFiftyModesOfTravel[j]+"']").parent().attr('id');
				
				$("#travelClaim_1_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGrouphundredToTwoFiftyMaxOneWayFare[j]);
				$("#travelClaim_1_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGrouphundredToTwoFiftyMaxReturnFare[j]);
				if(tGrouphundredToTwoFiftyModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_1").html("");
					$("#travelClaim_1").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_1_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGrouphundredToTwoFiftyModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGrouptwoFiftyToFiveHundredModesOfTravel=data[i].tGrouptwoFiftyToFiveHundredModesOfTravel.split(",");
			var tGrouptwoFiftyToFiveHundredMaxOneWayFare=data[i].tGrouptwoFiftyToFiveHundredMaxOneWayFare.split(",");
			var tGrouptwoFiftyToFiveHundredMaxReturnFare=data[i].tGrouptwoFiftyToFiveHundredMaxReturnFare.split(",");
			for(var j=0;j<tGrouptwoFiftyToFiveHundredModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_2_List input[name='travelClaimDistance'][value='"+tGrouptwoFiftyToFiveHundredModesOfTravel[j]+"']").parent().attr('id');
				
				$("#travelClaim_2_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGrouptwoFiftyToFiveHundredMaxOneWayFare[j]);
				$("#travelClaim_2_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGrouptwoFiftyToFiveHundredMaxReturnFare[j]);
				if(tGrouptwoFiftyToFiveHundredModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_2").html("");
					$("#travelClaim_2").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_2_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGrouptwoFiftyToFiveHundredModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGroupfiveHundredToThousandModesOfTravel=data[i].tGroupfiveHundredToThousandModesOfTravel.split(",");
			var tGroupfiveHundredToThousandMaxOneWayFare=data[i].tGroupfiveHundredToThousandMaxOneWayFare.split(",");
			var tGroupfiveHundredToThousandMaxReturnFare=data[i].tGroupfiveHundredToThousandMaxReturnFare.split(",");
			for(var j=0;j<tGroupfiveHundredToThousandModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_3_List input[name='travelClaimDistance'][value='"+tGroupfiveHundredToThousandModesOfTravel[j]+"']").parent().attr('id');
				
				$("#travelClaim_3_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGroupfiveHundredToThousandMaxOneWayFare[j]);
				$("#travelClaim_3_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGroupfiveHundredToThousandMaxReturnFare[j]);
				if(tGroupfiveHundredToThousandModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_3").html("");
					$("#travelClaim_3").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_3_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGroupfiveHundredToThousandModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGroupthousandToThousandFiveHundredModesOfTravel=data[i].tGroupthousandToThousandFiveHundredModesOfTravel.split(",");
			var tGroupthousandToThousandFiveHundredMaxOneWayFare=data[i].tGroupthousandToThousandFiveHundredMaxOneWayFare.split(",");
			var tGroupthousandToThousandFiveHundredMaxReturnFare=data[i].tGroupthousandToThousandFiveHundredMaxReturnFare.split(",");
			for(var j=0;j<tGroupthousandToThousandFiveHundredModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_4_List input[name='travelClaimDistance'][value='"+tGroupthousandToThousandFiveHundredModesOfTravel[j]+"']").parent().attr('id');
				$("#travelClaim_4_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGroupthousandToThousandFiveHundredMaxOneWayFare[j]);
				$("#travelClaim_4_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGroupthousandToThousandFiveHundredMaxReturnFare[j]);
				if(tGroupthousandToThousandFiveHundredModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_4").html("");
					$("#travelClaim_4").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_4_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGroupthousandToThousandFiveHundredModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGroupthousandFiveHundredToTwoThousandModesOfTravel=data[i].tGroupthousandFiveHundredToTwoThousandModesOfTravel.split(",");
			var tGroupthousandFiveHundredToTwoThousandMaxOneWayFare=data[i].tGroupthousandFiveHundredToTwoThousandMaxOneWayFare.split(",");
			var tGroupthousandFiveHundredToTwoThousandMaxReturnFare=data[i].tGroupthousandFiveHundredToTwoThousandMaxReturnFare.split(",");
			for(var j=0;j<tGroupthousandFiveHundredToTwoThousandModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_5_List input[name='travelClaimDistance'][value='"+tGroupthousandFiveHundredToTwoThousandModesOfTravel[j]+"']").parent().attr('id');
			
				$("#travelClaim_5_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGroupthousandFiveHundredToTwoThousandMaxOneWayFare[j]);
				$("#travelClaim_5_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGroupthousandFiveHundredToTwoThousandMaxReturnFare[j]);
				if(tGroupthousandFiveHundredToTwoThousandModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_5").html("");
					$("#travelClaim_5").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
						$("#travelClaim_5_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGroupthousandFiveHundredToTwoThousandModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel=data[i].tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel.split(",");
			var tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare=data[i].tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare.split(",");
			var tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare=data[i].tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare.split(",");
			for(var j=0;j<tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_6_List input[name='travelClaimDistance'][value='"+tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel[j]+"']").parent().attr('id');
				
				$("#travelClaim_6_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare[j]);
				$("#travelClaim_6_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare[j]);
				if(tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_6").html("");
					$("#travelClaim_6").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_6_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel=data[i].tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel.split(",");
			var tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare=data[i].tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare.split(",");
			var tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare=data[i].tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare.split(",");
			for(var j=0;j<tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_7_List input[name='travelClaimDistance'][value='"+tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel[j]+"']").parent().attr('id');
				
				$("#travelClaim_7_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare[j]);
				$("#travelClaim_7_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare[j]);
				if(tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_7").html("");
					$("#travelClaim_7").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_7_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGroupthreeThousandToFourThousandModesOfTravel=data[i].tGroupthreeThousandToFourThousandModesOfTravel.split(",");
			var tGroupthreeThousandToFourThousandMaxOneWayFare=data[i].tGroupthreeThousandToFourThousandMaxOneWayFare.split(",");
			var tGroupthreeThousandToFourThousandMaxReturnFare=data[i].tGroupthreeThousandToFourThousandMaxReturnFare.split(",");
			for(var j=0;j<tGroupthreeThousandToFourThousandModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_8_List input[name='travelClaimDistance'][value='"+tGroupthreeThousandToFourThousandModesOfTravel[j]+"']").parent().attr('id');
				$("#travelClaim_8_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGroupthreeThousandToFourThousandMaxOneWayFare[j]);
				$("#travelClaim_8_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGroupthreeThousandToFourThousandMaxReturnFare[j]);
				if(tGroupthreeThousandToFourThousandModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_8").html("");
					$("#travelClaim_8").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_8_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGroupthreeThousandToFourThousandModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGroupfourToFiveThousandModesOfTravel=data[i].tGroupfourToFiveThousandModesOfTravel.split(",");
			var tGroupfourToFiveThousandMaxOneWayFare=data[i].tGroupfourToFiveThousandMaxOneWayFare.split(",");
			var tGroupfourToFiveThousandMaxReturnFare=data[i].tGroupfourToFiveThousandMaxReturnFare.split(",");
			for(var j=0;j<tGroupfourToFiveThousandModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_9_List input[name='travelClaimDistance'][value='"+tGroupfourToFiveThousandModesOfTravel[j]+"']").parent().attr('id');
				$("#travelClaim_9_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGroupfourToFiveThousandMaxOneWayFare[j]);
				$("#travelClaim_9_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGroupfourToFiveThousandMaxReturnFare[j]);
				if(tGroupfourToFiveThousandModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_9").html("");
					$("#travelClaim_9").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_9_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGroupfourToFiveThousandModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGroupfiveToSixThousandModesOfTravel=data[i].tGroupfiveToSixThousandModesOfTravel.split(",");
			var tGroupfiveToSixThousandMaxOneWayFare=data[i].tGroupfiveToSixThousandMaxOneWayFare.split(",");
			var tGroupfiveToSixThousandMaxReturnFare=data[i].tGroupfiveToSixThousandMaxReturnFare.split(",");
			for(var j=0;j<tGroupfiveToSixThousandModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_10_List input[name='travelClaimDistance'][value='"+tGroupfiveToSixThousandModesOfTravel[j]+"']").parent().attr('id');
				$("#travelClaim_10_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGroupfiveToSixThousandMaxOneWayFare[j]);
				$("#travelClaim_10_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGroupfiveToSixThousandMaxReturnFare[j]);
				if(tGroupfiveToSixThousandModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_10").html("");
					$("#travelClaim_10").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
					$("#travelClaim_10_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGroupfiveToSixThousandModesOfTravel[j]+"']").prop("checked","checked");
				}
			}
			var tGroupsixToSevenThousandModesOfTravel=data[i].tGroupsixToSevenThousandModesOfTravel.split(",");
			var tGroupsixToSevenThousandMaxOneWayFare=data[i].tGroupsixToSevenThousandMaxOneWayFare.split(",");
			var tGroupsixToSevenThousandMxReturnFare=data[i].tGroupsixToSevenThousandMxReturnFare.split(",");
			for(var j=0;j<tGroupsixToSevenThousandModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_11_List input[name='travelClaimDistance'][value='"+tGroupsixToSevenThousandModesOfTravel[j]+"']").parent().attr('id');
				$("#travelClaim_11_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGroupsixToSevenThousandModesOfTravel[j]+"']").prop("checked","checked");
				$("#travelClaim_11_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGroupsixToSevenThousandMaxOneWayFare[j]);
				$("#travelClaim_11_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGroupsixToSevenThousandMxReturnFare[j]);
				if(tGroupsixToSevenThousandModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_11").html("");
					$("#travelClaim_11").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			var tGroupaboveSevenThousandModesOfTravel=data[i].tGroupaboveSevenThousandModesOfTravel.split(",");
			var tGroupaboveSevenThousandMaxOneWayFare=data[i].tGroupaboveSevenThousandMaxOneWayFare.split(",");
			var tGroupaboveSevenThousandMaxReturnFare=data[i].tGroupaboveSevenThousandMaxReturnFare.split(",");
			for(var j=0;j<tGroupaboveSevenThousandModesOfTravel.length;j++){
				var parentLi=$("#travelClaim_12_List input[name='travelClaimDistance'][value='"+tGroupaboveSevenThousandModesOfTravel[j]+"']").parent().attr('id');
				$("#travelClaim_12_List li[id='"+parentLi+"'] input[name='travelClaimDistance'][value='"+tGroupaboveSevenThousandModesOfTravel[j]+"']").prop("checked","checked");
				$("#travelClaim_12_List li[id='"+parentLi+"'] input[name='travelClaimOneWay']").val(tGroupaboveSevenThousandMaxOneWayFare[j]);
				$("#travelClaim_12_List li[id='"+parentLi+"'] input[name='travelClaimReturn']").val(tGroupaboveSevenThousandMaxReturnFare[j]);
				if(tGroupaboveSevenThousandModesOfTravel[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#travelClaim_12").html("");
					$("#travelClaim_12").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
			$("#travelMaxCountryCapital").val(data[i].tGroupCtryCapital);
			$("#travelMaxStateCapital").val(data[i].tGroupStateCapital);
			$("#travelMaxMetroCity").val(data[i].tGrouptravelMaxMetroCity);
			$("#travelMaxOtherCity").val(data[i].tGrouptravelMaxOtherCity);
			$("#travelMaxTown").val(data[i].tGrouptravelMaxTown);
			$("#travelMaxCountry").val(data[i].tGrouptravelMaxCountry);
			$("#travelMaxMunicipality").val(data[i].tGrouptravelMaxMunicipality);
			$("#travelMaxVillage").val(data[i].tGrouptravelMaxVillage);
			$("#travelMaxRemoteLoc").val(data[i].tGrouptravelMaxRemoteLoc);
			$("#travelMaxAwayCityTown").val(data[i].tGrouptravelMaxAwayCityTown);
			$("#travelMaxHillStation").val(data[i].tGrouptravelMaxHillStation);
			$("#travelMaxresort").val(data[i].tGrouptravelMaxresort);
			$("#travelMaxWarZone").val(data[i].tGrouptravelMaxWarZone);
			$("#fixedCountryCapital").val(data[i].tGroupCtryCapitalDIAM);
			$("#fixedStateCapital").val(data[i].tGroupStateCapitalDIAM);
			$("#fixedMetroCity").val(data[i].tGrouptravelMetroCityDIAM);
			$("#fixedOtherCity").val(data[i].tGrouptravelOtherCityDIAM);
			$("#fixedTown").val(data[i].tGrouptravelTownDIAM);
			$("#fixedCountry").val(data[i].tGrouptravelCountryDIAM);
			$("#fixedMunicipality").val(data[i].tGrouptravelMunicipalityDIAM);
			$("#fixedVillage").val(data[i].tGrouptravelVillageDIAM);
			$("#fixedRemoteLoc").val(data[i].tGrouptravelRemoteLocDIAM);
			$("#fixedAwayCityTown").val(data[i].tGrouptravelAwayCityTownDIAM);
			$("#fixedHillStation").val(data[i].tGrouptravelHillStationDIAM);
			$("#fixedresort").val(data[i].tGrouptravelresortDIAM);
			$("#fixedWarZone").val(data[i].tGrouptravelWarZoneDIAM);
			if(typeof data[i].travelGroupKlRemarks != 'undefined'){
				var travelGroupKlRemarks=data[i].travelGroupKlRemarks.split(",");
				for(var k=0;k<travelGroupKlRemarks.length;k++){
					var j=k+1;
					$("#travelKlRemarks"+j+"").val(travelGroupKlRemarks[k]);
				}
			}
			if(typeof data[i].travelGroupKlMandatory != 'undefined'){
				var travelGroupKlMandatory=data[i].travelGroupKlMandatory.split(",");
				for(var k=0;k<travelGroupKlMandatory.length;k++){
					var j=k+1;
					$("#travelKlMandatory"+j+" option[value='"+travelGroupKlMandatory[k]+"']").prop("selected","selected");
				}
			}
		}
	}
}
/* Travel Claim Add/Update ends*/
/* Expense Claim Add/Update start*/
$(document).ready(function(){
	$("#addUpdateExpenseGroup").on('click', function() {
		var expenseGroup={};
		var expenseGroupName=$("#expenseClaimGroupName").val();
		if(expenseGroupName==""){
			$("#expenseClaimGroupName").focus();
			swal("Invalid data error!","Please Provide Expense Group Name","error");
			return true;
		}
		expenseGroup.eGroupExpenseGroupName=expenseGroupName;
		expenseGroup.eGroupEntityHiddenId=$("#expenseGroupEntityHidden").val();
		expenseGroup.useremail = $("#hiddenuseremail").text();
		var expenseItemMaximumPermittedAdvance="";
		var expenseItemMonthlyMonetoryLimitForReimbursement="";
		var expenseItems=$("#expenseClaimList input[name='expenseClaims']:checkbox:checked").map(function () {
			var value=this.value;
			if(value!=""){
				var parentLi=$(this).closest("li").attr('id');
				expenseItemMaximumPermittedAdvance+=$("#expenseClaimList  li[id="+parentLi+"] input[type='text']:nth-child(3)").val()+",";
				expenseItemMonthlyMonetoryLimitForReimbursement+=$("#expenseClaimList  li[id="+parentLi+"] input[type='text']:nth-child(4)").val()+",";
				return value;
			}
		}).get();
		expenseGroup.eGroupexpenseItems=expenseItems.toString();
		expenseGroup.eGroupexpenseItemMaximumPermittedAdvance=expenseItemMaximumPermittedAdvance;
		expenseGroup.eGroupexpenseItemMonthlyMonetoryLimitForReimbursement=expenseItemMonthlyMonetoryLimitForReimbursement;
		ajaxCall('/claims/createExpenseGroup', expenseGroup, '', '', '', '', 'expenseGroupSucess', '', true);
	});
});

function expenseGroupSucess(){
	$("#notificationMessage").html("Expense Groups has been added/Updated successfully.");
	$('#showTravelExpenseGroupform-container').trigger('click');
}

function showExpenseGroupEntityDetails(elem){
	$("#newExpenseClaimConfigurationform-container").trigger('click');
	$("#expenseGroupEntityHidden").val("");
	$("#expenseClaimGroupName").val("");
	var entityId=$(elem).attr('id');
	var origEntityId=entityId.substring(19, entityId.length);
	var jsonData = {};
	jsonData.userEmail = $("#hiddenuseremail").text();
	jsonData.eGroupEntityId=origEntityId;
	ajaxCall('/claims/showExpenseGroup', jsonData, '', '', '', '', 'expenseGroupEditSucess', '', true);
}

function expenseGroupEditSucess(data){
	if(data.result){
		var data=data.expenseGroupEntityDetails;
		for(var i=0;i<data.length;i++){
			var expenseGroupEntityId=data[i].eGroupId;
			var expenseGroupGroupName=data[i].eGroupGroupName;
			$("#expenseGroupEntityHidden").val(expenseGroupEntityId);
			$("#expenseClaimGroupName").val(expenseGroupGroupName);
			var eGroupexpenseItems=data[i].eGroupexpenseItems.split(",");
			var eGroupexpenseItemMaximumPermittedAdvance=data[i].eGroupexpenseItemMaximumPermittedAdvance.split(",");
			var eGroupexpenseItemMonthlyMonetoryLimitForReimbursement=data[i].eGroupexpenseItemMonthlyMonetoryLimitForReimbursement.split(",");
			for(var j=0;j<eGroupexpenseItems.length;j++){
				var parentLi=$("#expenseClaimList input[name='expenseClaims'][value='"+eGroupexpenseItems[j]+"']").parent().attr('id');
				$("#expenseClaimList li[id='"+parentLi+"'] input[name='expenseClaims'][value='"+eGroupexpenseItems[j]+"']").prop("checked","checked");
				$("#expenseClaimList li[id='"+parentLi+"'] input[name='expenseClaim_maxAdvance']").val(eGroupexpenseItemMaximumPermittedAdvance[j]);
				$("#expenseClaimList li[id='"+parentLi+"'] input[name='expenseClaim_monthlyMoney']").val(eGroupexpenseItemMonthlyMonetoryLimitForReimbursement[j]);
				if(eGroupexpenseItems[j]!=""){
					var text=(j+1)+" "+"Items Selected";
					$("#expenseClaimDropDown").html("");
					$("#expenseClaimDropDown").html(""+text+"<b>&nbsp;&nbsp;&#8711;</b>");
				}
			}
		}
	}
}
/* Expense Claim Add/Update ends*/


// select all functionality for custom dropdown with select all and custom input fields
function claimCheckUncheck(elem){
	var checked=$(elem).is(':checked');
	var parentUl=$(elem).closest("ul").attr('id');
	var parentLi=$(elem).closest("li").attr('id');
	var parentCustomContainerDiv=$(elem).parent().parent().parent().attr('id');
	var multiselectbutton=$(elem).parent().parent().parent().parent().find("button").attr('id');
	if(checked==true){
		var firstInputValue=$("#"+parentUl+" li:nth-child(2) input[type='text']:nth-child(3)").val();
		var secondInputValue=$("#"+parentUl+" li:nth-child(2) input[type='text']:nth-child(4)").val();
		var checkvalue=$(elem).val();

		if(checkvalue==""){
			$("#"+parentUl+" input[type='checkbox']").each(function () {
		        $(this).prop("checked" ,true);
			});
			if(firstInputValue!=""){
				$('#'+parentUl+' li input[type="text"]:nth-child(3)').each(function () {
					$(this).val(firstInputValue);
				});
			}else{
				$('#'+parentUl+' li input[type="text"]:nth-child(3)').each(function () {
					if($(this).val()=="0.0" || $(this).val()==""){
						$(this).val("0.0");
					}
				});
			}
			if(secondInputValue!=""){
				$('#'+parentUl+' li input[type="text"]:nth-child(4)').each(function () {
					$(this).val(secondInputValue);
				});
			}else{
				$('#'+parentUl+' li input[type="text"]:nth-child(4)').each(function () {
					if($(this).val()=="0.0" || $(this).val()==""){
						$(this).val("0.0");
					}
				});
			}
		}
		var check_box_values = $("#"+parentUl+" input[type='checkbox']:checked").map(function () {
			return this.value;
		}).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#"+multiselectbutton+"").text(text);
			$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}if(check_box_values==0){
			$("#"+multiselectbutton+"").text("None Selected");
			$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}
		var checkbox_box_values = $("#"+parentUl+" input[type='checkbox']:checked").map(function () {
			return this.value;
		}).get();
		if(checkbox_box_values!="" && checkbox_box_values!="undefined"){
			var length=checkbox_box_values.length;
			if(length>0){
				var text=length+" "+"Items Selected";
				$("#"+multiselectbutton+"").text(text);
				$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
			}if(checkbox_box_values==0){
				$("#"+multiselectbutton+"").text("None Selected");
				$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
			}
		}
	}else if(checked==false){
		var checkvalue=$(elem).val();
		if(checkvalue==""){
			var msg="";
			//if(parentCustomContainerDiv=="travelClaim_0_menuid"){
			if(parentCustomContainerDiv.startsWith('travelClaim_')){
				msg="Do you want to remove already configured distance in miles/kms and maximum one way and return fare for the same.";
			}
			if(parentCustomContainerDiv=="travelBoardingLodgingDropDown-menuid"){
				msg="Do you want to remove already configured accomodation type and maximum room cost per night and maximum food cost per day.";
			}
			if(parentCustomContainerDiv=="expenseClaimDropDown-menuid"){
				msg="Do you want to remove already configured expense items and maximum permitted advance and maximum monthly monetory limit for reimbursement.";
			}
			if(confirm(msg)){
				$("#"+parentUl+" input[type='checkbox']").each(function () {
			        $(this).prop("checked" ,false);
				});
				$('#'+parentUl+' li input[type="text"]:nth-child(3)').each(function () {
					$(this).val("0.0");
				});
				$('#'+parentUl+' li input[type="text"]:nth-child(4)').each(function () {
					$(this).val("0.0");
				});
				$("#"+parentUl+" li:nth-child(2) input[type='text']:nth-child(3)").val("");
				$("#"+parentUl+" li:nth-child(2) input[type='text']:nth-child(4)").val("");
			}
		}else{
			$("#"+parentUl+" input[type='checkbox'][value='']").prop("checked" ,false);
			$("#"+parentUl+" li[id="+parentLi+"] input[type='text']:nth-child(3)").val("0.0");
			$("#"+parentUl+" li[id="+parentLi+"] input[type='text']:nth-child(4)").val("0.0");
		}
		var check_box_values = $("#"+parentUl+" input[type='checkbox']:checked").map(function () {
			return this.value;
		   }).get();
		var length=check_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#"+multiselectbutton+"").text(text);
			$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}if(check_box_values==0){
			$("#"+multiselectbutton+"").text("None Selected");
			$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}
		var checkbox_box_values = $("#"+parentUl+" input[type='checkbox']:checked").map(function () {
			return this.value;
		}).get();
		if(checkbox_box_values!="" && checkbox_box_values!="undefined"){
			var length=checkbox_box_values.length;
			if(length>0){
				var text=length+" "+"Items Selected";
				$("#"+multiselectbutton+"").text(text);
				$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
			}if(checkbox_box_values==0){
				$("#"+multiselectbutton+"").text("None Selected");
				$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
			}
		}
	}
}

function claimtoggleCheck(elem){
	var parentUl=$(elem).closest("ul").attr('id');
	var parentCustomContainerDiv=$(elem).parent().parent().parent().attr('id');
	var parentLi=$(elem).closest("li").attr('id');
	var multiselectbutton=$(elem).parent().parent().parent().parent().find("button").attr('id');
	var inputValue=$(elem).val();
	if(inputValue==""){
		$("#"+parentUl+" li[id="+parentLi+"] input[type='checkbox']:nth-child(1)").prop('checked', false);
		$("#"+parentUl+" li[id="+parentLi+"] input[type='checkbox']:nth-child(1)").prop('checked', false);
	}else{
		$("#"+parentUl+" li[id="+parentLi+"] input[type='checkbox']:nth-child(1)").prop('checked', true);
		$("#"+parentUl+" li[id="+parentLi+"] input[type='checkbox']:nth-child(1)").prop('checked', true);
	}
	var check_box_values = $("#"+parentUl+" input[type='checkbox']:checked").map(function () {
		return this.value;
	   }).get();
	var length=check_box_values.length;
	if(length>0){
		var text=length+" "+"Items Selected";
		$("#"+multiselectbutton+"").text(text);
		$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
	}if(check_box_values==0){
		$("#"+multiselectbutton+"").text("None Selected");
		$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
	}
	var checkbox_box_values = $("#"+parentUl+" input[type='checkbox']:checked").map(function () {
		return this.value;
	}).get();
	if(checkbox_box_values!="" && checkbox_box_values!="undefined"){
		var length=checkbox_box_values.length;
		if(length>0){
			var text=length+" "+"Items Selected";
			$("#"+multiselectbutton+"").text(text);
			$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}if(checkbox_box_values==0){
			$("#"+multiselectbutton+"").text("None Selected");
			$("#"+multiselectbutton+"").append("<b>&nbsp;&nbsp;&#8711;</b>");
		}
	}
}
