$(document).ready(function(){
	alert("inside loginbutton click");
	$('.loginButton'). click(function(){
		alert("inside loginbutton function");
		var username=$("#loginuser").val();
		var password=$("#pass").val();
		if(username=="" || password==""){
			alert("Please provide Login Credentilas.")
			return true;
		}else{
			var jsonData = {};
			jsonData.userName = username;
			jsonData.loginpwd = password;
			var url="/login";
			$.ajax({
				url: url,
				data:JSON.stringify(jsonData),
				type:"text",
			    method:"POST",
			    contentType:'application/json',
				success: function (data) {
					if(data.logincredentials[0].message=="Failure"){
						alert(data.logincredentials[0].failurereason);
						$("#loginuser").val("");
						$("#pass").val("");
						return true;
					}else{
						 if (data.logincredentials[0].days == 0) {
							 window.location.href="/config";
//							 if(data.logincredentials[0].userrole.indexOf("MASTER ADMIN")!=-1 && data.logincredentials[0].trialOver == 1){
//								 window.location.href="/subscribe";
//							 }else{
//								 window.location.href="/config";
//							 }
						 } else {
	                         window.location.href="/passwordExpiry";
						 }
					}
					alwaysScrollTop();
				},
				error: function (xhr, status, error) {
					
				}
			});
		}
	});
	});