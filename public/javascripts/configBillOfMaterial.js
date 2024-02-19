
$(document).on("focusin", ".masterItemDropDown", function(){
	$(this).select2({
			placeholder: "Select an item",
				allowClear: true,
				multiple: false
	});
});
$(document).ready(function() {

	$(".masterItemDropDown").select2({
		placeholder: "Select an item",
		allowClear: true,
		multiple: false
	});  
	// fetching chart of accounts and saving component items in localstorage
	let jsonData = {};
	jsonData.usermail = $("#hiddenuseremail").text();
	let url = "/config/showChartOFAccount";
	$.ajax({
			url: url,
			data: JSON.stringify(jsonData),
			type: "text",
			headers: {
					"X-AUTH-TOKEN": window.authToken
			},
			method: "GET",
			contentType: 'application/json',
			success: function (data) {
					let incomesAccountCode = "";
					let expensesAccountCode = "";
					//checking the accountCode for incomes 
					for (var i = 0; i < data.partData.length; i++) {
							if(data.partData[i].name == "Expenses") {
									expensesAccountCode = data.partData[i].accountCode;
							}
					}
					
					//retreiving items whose topLevelAccountCode are of expenses
					let componentItemsDropDown = "";
					for(let i = 0; i < data.itemData.length; i++) {
							if(data.itemData[i].topLevelAccountcode == expensesAccountCode) {
									componentItemsDropDown += '<option value="'+data.itemData[i].id+'">' + data.itemData[i].name + '</option>';
							}
					}
					localStorage.setItem("bomComponentItemsDropDown", componentItemsDropDown);
					
			}
	});
	createDefaultComponentRowInBomForm();
	function createDefaultComponentRowInBomForm() {
			let componentDropdown = '<tr><td><select class="masterItemDropDown"><option value="">Select items</option>'+localStorage.getItem('bomComponentItemsDropDown')+'</select></td><td><input type="number"  min="0"/></td><td><input type="hidden" /></td></tr>';
			$(".bomCreateRight").children("table").children("tbody").html(componentDropdown);
	}
	$("#showBomArea").click(function(){
			$(".bomContainer").slideToggle();
			getAllBOMs();
			$(".bomCreateFormCont").hide();
	});
	$("#openBomForm").click(function(){
			resetBomCreateForm();
			$(".bomCreateFormCont").slideDown();
			$(".masterItemDropDown").select2({
				placeholder: "Select an item",
				allowClear: true,
				multiple: false
			}); 
	});
	$("#addnewItemForBom").click(function(){
		console.log("================");
			let componentDropdown = '<tr><td><select class="masterItemDropDown"><option value="" onchange="checkDuplicateRowInBom()">Select items</option>'+localStorage.getItem('bomComponentItemsDropDown')+'</select></td><td><input type="number" onchange="checkDuplicateRowInBom()"  min="0"/></td><td><input type="hidden" /><button style="cursor: pointer; float:right;" class="removeCurrentBomRow" title="remove item"><i class="fa fa-minus-circle fa-lg"></i></button></td></tr>';
			console.log("componentDropdown = ",componentDropdown);
			$("#bomItemRowsContainer").append(componentDropdown);
			$(".masterItemDropDown").select2({
				placeholder: "Select an item",
				allowClear: true,
				multiple: false
			}); 
			//let newHeight = parseInt($(".bomCreateFormCont").height()) + parseInt(55);
			//$(".bomCreateFormCont").css("height", newHeight + "px");
	});

	$("#bomItemRowsContainer").on("click", ".removeCurrentBomRow", function() {
			if (confirm("This action will remove the component item!") == true) {
					$(this).parents("tr").remove();    
			}
			
	});


	
	
	$("#createBOM").click(function() {
			if(!checkDuplicateRowInBom()) {
					
			
					$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
					let postData ={};
					let bomId = $("#bomMainId").val();
					let bomName = $("#bomName").val();
					postData.bomId = bomId;
					postData.bomName = bomName;
					let bomItemsData = new Array();
					$("#bomItemRowsContainer > tr").each(function(){
							let itemData = {};
							itemData.bomItem = $(this).children("td:eq(0)").children("select").val(); 
							itemData.bomNoOfUnit = $(this).children("td:eq(1)").children("input").val();
							itemData.bomDetailId = $(this).children("td:eq(2)").children("input").val();
							bomItemsData.push(itemData); 
					});
					postData.dataArray = bomItemsData;
					console.log(postData);
					var url = "/bom/saveorupdate";
							
							$.ajax({
									url: url,
									data: JSON.stringify(postData),
									type: "text",
									headers: {
											"X-AUTH-TOKEN": window.authToken
									},
									method: "POST",
									contentType: 'application/json',
									
									error: function (xhr, status, error) {
											if (xhr.status == 401) {
													//doLogout();
											} else if (xhr.status == 500) {
													swal("Error on Save/Update BOM!", "Please retry, if problem persists contact support team", "error");
											}
									},
									complete: function (data) {
											$.unblockUI();
											resetBomCreateForm();
											$(".bomCreateFormCont").hide();
											getAllBOMs();
											
									}
							});
					} else {
							swal("Error on Save/Update BOM!", "Duplicate item with no of unit found", "error");
					}

			});  
			
			function resetBomCreateForm() {
					$("#bomName").val("");
					$("#bomMainId").val("");
					createDefaultComponentRowInBomForm();            
			}

			function getAllBOMs() {
					$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
					$.ajax({
							url: "/bom/getbyorganization",
							headers:{
									"X-AUTH-TOKEN": window.authToken
							},
							method:"GET",
							success: function (data){
									
									
									console.log(data);
									if(data.status == 'success'){
											
											
											$("#bomListUl").html('');
											for(var i=0; i<data.bomlist.length; i++){
													var rowData = '<li><div style="width:auto; min-width:300px; float:left">'+data.bomlist[i].bomName+ '-' +data.bomlist[i].entityId+'</div> <button class="btn btn-primary" style="float:right; margin: -5px 10px 10px 0px;" onclick="editBOM(\''+data.bomlist[i].entityId+'\');">Edit BOM</button></li>';
													$("#bomListUl").append(rowData);
											}
									}else{
											$("#notificationMessage").html("Fetch of Bill of Material has been failed.");
									}
							},
							error: function (xhr, status, error){
									if(xhr.status == 401){
											doLogout();
									}else if(xhr.status == 500){
											swal("Error on fetch Bill of Material!", "Please retry, if problem persists contact support team", "error");
									}
							},
							complete: function(data) {
									$.unblockUI();
							}
					});
					//return false;
			}

			$("#bomFormCancel").click(function() {
					$(".bomCreateFormCont").slideUp();
					createDefaultComponentRowInBomForm();
			});

});

function editBOM(entityID) {
	$(".bomCreateFormCont").show()
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	
	$.ajax({
			url: "/bom/getdetails/"+entityID,
			headers:{
					"X-AUTH-TOKEN": window.authToken
			},
			method:"GET",
			success: function (data){
					console.log(data);
					if(data.status == 'success'){
							$("#bomMainId").val(data.bomId);
							$("#bomName").val(data.bomName);
							
							let bomTableBody = $("#bomItemRowsContainer");
							for(var j=0; j<bomTableBody.children("tr").length; j++) {
									if(j>0) {
											bomTableBody.children("tr:eq("+j+")").remove();
									}
							}
							for(var i=0; i<data.bomlist.length; i++){
									if(i > 0){
											$("#addnewItemForBom").trigger("click");
									}
									bomTableBody.children("tr:eq("+i+")").children("td:eq(0)").children("select").children("option[value='"+data.bomlist[i].expenseId+"']").prop("selected","selected");

									bomTableBody.children("tr:eq("+i+")").children("td:eq(1)").children("input").val(data.bomlist[i].noOfUnit);
									bomTableBody.children("tr:eq("+i+")").children("td:eq(2)").children("input").val(data.bomlist[i].entityId);
							}
							
					}else{
							$("#notificationMessage").html("Fetch of Bill of Material has been failed.");
					}
			},
			error: function (xhr, status, error){
					if(xhr.status == 401){
							doLogout();
					}else if(xhr.status == 500){
							swal("Error on fetching Bill of Material details!", "Please retry, if problem persists contact support team", "error");
					}
			},
			complete: function(data) {
				 $.unblockUI();
			}
	});
}

function checkDuplicateRowInBom() {
	//e.preventDefault(); // prevent the form from submitting
	
	var rows = $("#bomItemRowsContainer tr"); // get all rows in the table body
	var duplicates = false; // flag for duplicate rows
	
	rows.each(function() {
			var currentRow = $(this);
			var currentSelect = currentRow.find("select").val();
			var currentText = currentRow.find("input[type='number']").val();
			
			// loop through other rows and compare values
			rows.not(currentRow).each(function() {
					var otherRow = $(this);
					var otherSelect = otherRow.find("select").val();
					var otherText = otherRow.find("input[type='number']").val();
					
					// check for duplicate values
					if (currentSelect === otherSelect && currentText === otherText) {
							duplicates = true;
							return false; // exit the loop early if a duplicate is found
					}
			});
			
			if (duplicates) {
					swal("Error on Save/Update BOM!", "Duplicate item with no of unit found", "error");
					
					return false; // exit the loop early if a duplicate is found
			}
	});
	return duplicates;
	// if (!duplicates) {
	//     // submit the form if no duplicates were found
	//     this.submit();
	// }
}
