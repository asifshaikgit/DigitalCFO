$(function(){
 
    $("#userAgreeCheck").on('change', function(){
    	if($(this).prop("checked") == true){
    		$("#userDisagreeCheck").prop("checked", false);
    		$("#submitTermsAndConditions").prop('disabled',false);
    	}else {
    		if($("#userDisagreeCheck").prop("checked") == false){
    			$("#submitTermsAndConditions").prop('disabled',true);
    		}
    	}
    });
    
    $("#userDisagreeCheck").on('change', function(){
    	if($(this).prop("checked") == true){
    		$("#userAgreeCheck").prop("checked", false);
    		$("#submitTermsAndConditions").prop('disabled',false);
    	}else {
    		if($("#userAgreeCheck").prop("checked") == false){
    			$("#submitTermsAndConditions").prop('disabled',true);
    		}
    	}
    });
    
    
    $("#submitTermsAndConditions").on('click', function(){
    	var agreeOrDisagree;
    	if($(userAgreeCheck).prop("checked") == true) {
    		agreeOrDisagree = 1;
    	}
    	if($(userDisagreeCheck).prop("checked") == true) {
    		agreeOrDisagree = 0;
    	}
    	if($(userAgreeCheck).prop("checked") == true || $(userDisagreeCheck).prop("checked") == true){
    		var jsonData = {};
			var useremail=$("#hiddenuseremail").text();
			jsonData.useremail = useremail;
			jsonData.agreeOrDisagree = agreeOrDisagree;
			var url="/agreeTermsAndConditions";
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
					window.location.href=data.url;
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		}
	});

});