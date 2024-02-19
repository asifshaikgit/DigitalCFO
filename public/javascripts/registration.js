var validateCaptcha = true;
function sellerforgot(){
	location.hash = "#sellerforgotlogindiv";
	$('#sellerAccountLoginDiv').hide();
	$('#sellerAccountRegDiv').hide();
	$('#logindiv').hide();
	$('#sellerforgotlogindiv').show();
}

function sellerLoginPage(){
    location.hash = "#sellerAccountLoginDiv";
    $('#sellerAccountLoginDiv').hide();
    $('#sellerAccountRegDiv').hide();
    $('#logindiv').hide();
    $('#sellerforgotlogindiv').hide();
    $('#sellerAccountLoginDiv').show();
}

function forgot(){
	$("#successregdiv").hide();
	$("#logindetailemail").val("");
	location.hash = "#forgotlogindiv";
	$("#successaccountmsg").html("");
	$("#accountspanstat").html("");
	$("#accountspanstat").html("Reset Your Login Deatils.");
	$("#companyname").val("");
	$("#companybnchname").val("");
	$("#corporateemail").val("");
	$("#orgpwd").val("");
	$("#dupchecklabel").html("");
	$("#forgotlogindiv").show();
	$("#logindiv").hide();
	$("#signUpDiv").hide();
	$("#newSignUp").hide();
	$('#sellerforgotlogindiv').hide();
	$('#sellerAccountLoginDiv').hide();
	$('#sellerAccountRegDiv').hide();
	$("#leftboxtd").attr("valign","bottom");
}

function vendcustforgot(){
	var email = GetURLParameter('accountEmail');
	$("#vendCustresetemail").val(email);
	$("#successregdiv").hide();
	$("#logindetailemail").val("");
	location.hash = "#forgotlogindiv";
	$("#successaccountmsg").html("");
	$("#accountspanstat").html("");
	$("#accountspanstat").html("Reset Your Login Deatils.");
	$("#companyname").val("");
	$("#companybnchname").val("");
	$("#corporateemail").val("");
	$("#orgpwd").val("");
	$("#dupchecklabel").html("");
	$("#forgotlogindiv").hide();
	$("#logindiv").hide();
	$("#signUpDiv").hide();
	$("#newSignUp").hide();
	$("#leftboxtd").attr("valign","bottom");
	$("#vendCustAccount").hide();
	$("#vendCustForgotAccount").show();
}

/*New Login Starts*/
$(document).ready(function(){
	$('.loginButtonSh').on('click',function(){
		window.location.href="/signIn#logindiv";
		$('.login-form').hide().val('');
		$('#logindiv').show();
		$("#logindiv #loginuser").focus();
	});
	$('.signUpOrganizationSh').on('click',function(){
		window.location.href="/signUp#signUpDiv";
	});
/*
	$('.sellerAccountRegisterSh').on('click',function(){
		window.location.href="/signIn#sellerAccountRegDiv";
	});
	$('.sellerAccountLoginSh').on('click',function(){
		window.location.href="/signUp#sellerAccountLoginDiv";
		$('.login-form').hide().val('');
		$('#sellerAccountLoginDiv').show();
		$("#sellerAccountLoginDiv #sellerAccountUser").focus();
	}); */
});

var notifyLogin={
	time:0,
	show:function(msg,shouldHide,isError){
		if(!isEmpty(msg)){
			clearTimeout(notifyLogin.time);
			$('#messageDisplay').html(msg);
			(!isEmpty(isError) && isError)?$('#messageDisplay').css('color','#B4283C'):$('#messageDisplay').css('color','#000000');
			if($('#messageDisplay').css('right') != 0) {
				$('#messageDisplay').show({'right':'40px'},1500);
			}
			if(!isEmpty(shouldHide) && shouldHide){
				notifyLogin.time=window.setTimeout(function(){
					notifyLogin.hide();
				},10000);
			}
		}
	},
	hide:function(){

		$('#messageDisplay').hide({'right':'0'},1500,function(){
			var locHash=window.location.hash;
			$('#messageDisplay').empty().css('color','#000000');
			if('#resetLoginCred'==locHash) {
				window.location.href="/signIn#logindiv";
			}
		});
	}
};

function verifyPassword(str) {
    //var patt = new RegExp("^.*(?=.{8,})(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])[a-zA-Z0-9@#$%^&+=]*$");
    var patt = new RegExp("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$");

    var res = patt.test(str);
    if(!res){
    	$('.pswd_info').show();
    }
    return res;
}

//create Company account and master admin  user
$(document).ready(function(){

		$('#countrySelectButton').on('click',function(){

			$("#countrySelectButton").attr("disabled", "disabled");
			$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
			var captchaLength = $("#recaptchaLength").val();
			var countryName=$("#countryName").val();
			if(countryName == ""){
				$.unblockUI();
				$('#countrySelectButton').removeAttr("disabled");
				grecaptcha.reset(countrySelectWidgetId);
				$("#recaptchaLength").val(0);
				//notifyLogin.show('Provide company name.',true,true);

				$("#countryName").focus();
				return true;
			}

			if(validateCaptcha === true && (captchaLength==0 || captchaLength == "")){
				$.unblockUI();
				$('#countrySelectButton').removeAttr("disabled");
				notifyLogin.show('Please select Captcha.',true,true);
				return false;
			}
			if(countryName == "INDIA") {
				window.location.assign("/signIn#signUpDiv");
			}else {
				window.location.assign("/signIn#demoPageCountryWise");
			}
		});

		$('#demoCountryWiseSubmit').on('click',function(){

			$("#demoCountryWiseSubmit").attr("disabled", "disabled");
			$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
			var captchaLength = $("#recaptchaLength").val();

			var demofullName = $("#demofullName").val();
			var demoContactemail = $("#demoContactemail").val();
			var demoContactphone = $("#demoContactphone").val();
			var demoContactcompany = $("#demoContactcompany").val();
			var demoContactcomments = $("#demoContactcomments").val();


			if(demofullName == ""){
				$.unblockUI();
				$('#demoCountryWiseSubmit').removeAttr("disabled");
				grecaptcha.reset(demoPageWidgetId);
				$("#recaptchaLength").val(0);
				$("#demofullName").val("");
				$("#demofullName").attr("placeholder", "Enter Your Full Name").blur();
				$("#demofullName").focus();
				return true;
			}

			var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
			if(!emailReg.test(demoContactemail)) {
				$.unblockUI();
				$('#demoCountryWiseSubmit').removeAttr("disabled");
				grecaptcha.reset(demoPageWidgetId);
				$("#recaptchaLength").val(0);
				$("#demoContactemail").val("");
				$("#demoContactemail").attr("placeholder", "Enter Valid E-mail.").blur();
				$("#demoContactemail").focus();
				return true;
			}

			if(demoContactphone == ""){
				$.unblockUI();
				$('#demoCountryWiseSubmit').removeAttr("disabled");
				grecaptcha.reset(demoPageWidgetId);
				$("#recaptchaLength").val(0);
				$("#demoContactphone").val("");
				$("#demoContactphone").attr("placeholder", "Please provide your Phone Number").blur();
				$("#demoContactphone").focus();
				return true;
			}

			if(demoContactcompany == ""){
				$.unblockUI();
				$('#demoCountryWiseSubmit').removeAttr("disabled");
				grecaptcha.reset(demoPageWidgetId);
				$("#recaptchaLength").val(0);
				$("#demoContactcompany").val("");
				$("#demoContactcompany").attr("placeholder", "Please provide your Company Name").blur();
				$("#demoContactcompany").focus();
				return true;
			}

			if(validateCaptcha === true && (captchaLength==0 || captchaLength == "")){
				$.unblockUI();
				$('#demoCountryWiseSubmit').removeAttr("disabled");
				notifyLogin.show('Please select Captcha.',true,true);
				return false;
			}

			var jsonData = {};
			jsonData.cName = demofullName;
			jsonData.enqEmail = demoContactemail;
			jsonData.enqPhone = demoContactphone;
			jsonData.enqComments = demoContactcomments;
			jsonData.enquiryType = 2;
			jsonData.companyName = demoContactcompany;
			var url="/idos/enquiry";
			$.ajax({
				url : url,
				data : JSON.stringify(jsonData),
				type : "text",
				method : "POST",
				contentType : 'application/json',
				success : function(data) {
					$("#demofullName").val("");
					$("#demoContactemail").val("");
					$("#demoContactphone").val("");
					$("#demoContactcomments").val("");
					$("#demoContactcompany").val("");
					swal({
						  title: "Thank you for your interest.",
						   text: "We will connect with you and Help you get started with taking advantage of system for your Organization.",
						    type: "success"
						  },
						  function(){
						    window.location.href = '/';
							}
						);
				},
				error : function(xhr, status, error) {
					$("#recaptchaLength").val(0);
					grecaptcha.reset(demoPageWidgetId);
					notifyLogin.show(error,true,true);
				},
				complete: function(data) {
					$.unblockUI();
					setTimeout(function(){
						$('#demoCountryWiseSubmit').removeAttr("disabled");
					}, 7000);
				}
			});

		});

	// Added by Suresh Kumar P
	// Super admin company registeration
	$('#signUpCompany').click(function () {
		$("#signUpCompany").attr("disabled", "disabled");
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });

		var compName = $("#saCompanyName").val();
		var contactName = $("#saContactName").val();
		var corEmailId = $("#saCorporateEmail").val();
		var adminPass = $("#saOrgPwd").val();
		var adminPassConfirm = $("#saOrgPwdConfirm").val();
		var adminPhonenumber = $("#saPhoneNumber").val();
		var goodEmail = corEmailId.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
		apos = corEmailId.indexOf("@"); dotpos = corEmailId.lastIndexOf("."); lastpos = corEmailId.length - 1;
		var badEmail = (apos < 1 || dotpos - apos < 2 || lastpos - dotpos < 2);

		if (compName == "") {
			$.unblockUI();
			$('#signUpCompany').removeAttr("disabled");
			$("#saCompanyName").val("");
			$("#saCompanyName").attr("placeholder", "Enter company/business name").blur();
			$("#saCompanyName").focus();
			return true;
		}
		if (contactName == "") {
			$.unblockUI();
			$('#signUpCompany').removeAttr("disabled");
			$("#saContactName").val("");
			$("#saContactName").attr("placeholder", "Enter your name").blur();
			$("#saContactName").focus();
			return true;
		}
		if (corEmailId == "" || !goodEmail || badEmail) {
			$.unblockUI();
			$('#signUpCompany').removeAttr("disabled");
			$("#saCorporateEmail").val("");
			$("#saCorporateEmail").attr("placeholder", "Enter company E-mail.").blur();
			$("#saCorporateEmail").focus();
			return true;
		}
		if (adminPass == "") {
			$.unblockUI();
			$('#signUpCompany').removeAttr("disabled");
			$("#saOrgPwd").attr("placeholder", "Enter password.").blur();
			$("#saOrgPwd").focus();
			return true;
		}
		if (adminPassConfirm == "") {
			$.unblockUI();
			$('#signUpCompany').removeAttr("disabled");
			$("#saOrgPwdConfirm").attr("placeholder", "Enter password again.").blur();
			$("#saOrgPwdConfirm").focus();
			return true;
		}
		if (adminPass != adminPassConfirm) {
			$.unblockUI();
			$('#signUpCompany').removeAttr("disabled");
			$("#saOrgPwdConfirm").val("");
			$("#saOrgPwdConfirm").attr("placeholder", "Both passwords do not match.").blur();
			$("#saOrgPwdConfirm").focus();
			return true;
		}
		var validationPassedNo = $(".validationPassedNo").val();
		if (validationPassedNo != "6") {
			$.unblockUI();
			$('#signUpCompany').removeAttr("disabled");
			notifyLogin.show('Provided Password does not match the rules.', true, true);
			$("#saOrgPwd").focus();
			return true;
		}
		if (!verifyPassword(adminPass)) {
			$.unblockUI();
			return true;
		}
		if (isEmpty(adminPhonenumber) || adminPhonenumber.lenght < 6) {
			$.unblockUI();
			$('#signUpCompany').removeAttr("disabled");
			$("#saPhoneNumber").val("");
			$("#saPhoneNumber").attr("placeholder", "Provide valid phone/mobile number.").blur();
			$("#saPhoneNumber").focus();
			return true;
		}

		var jsonData = {};
		jsonData.companyName = compName;
		jsonData.contactName = contactName;
		jsonData.companyEmailId = corEmailId;
		jsonData.adminPwd = adminPass;
		jsonData.adminPhonenumber = adminPhonenumber;
		jsonData.registrationSource = "";
		jsonData.adminWebsite = "";
		jsonData.userMode = 0;
		jsonData.companyId = 0;
		var url = "/config/addOrganization";

		$.ajax({
			url: url,
			data: JSON.stringify(jsonData),
			type: "text",
			headers: {
				"X-AUTH-TOKEN": window.authToken
			},
			method: "POST",
			contentType: 'application/json',
			success: function (data) {
				$("#saCompanyName").val("");
				$("#saContactName").val("");
				$("#saCorporateEmail").val("");
				$("#saOrgPwd").val("");
				$("#saOrgPwdConfirm").val("");
				$("#saPhoneNumber").val("");
				$("#accountspanstat").html("Please log in to your account.");
				$("#successaccountmsg").html("");
				$("#successaccountmsg").append('Welcome, Your Company has been successfully created. Please activate you account following activation link sent to your email.');
				notifyLogin.show('Welcome! Your Company has been successfully created. Please activate you account following activation link sent to your email.', true, false);
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				notifyLogin.show(error, true, true);
			},
			complete: function (data) {
				$.unblockUI();
				setTimeout(function () {
					$('#signUpCompany').removeAttr("disabled");
				}, 7000);
			}
		});
	});

	$('#addCompanyOrganization').click(function () {
		$("#addCompanyOrganization").attr("disabled", "disabled");
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var companyId = $("#hiddenOrgId").val();
		var compName = $("#orgCompanyName").val();
		var contactName = $("#orgContactName").val();
		var corEmailId = $("#orgCorporateEmail").val();
		var adminPass = $("#orgPassword").val();
		var adminPassConfirm = $("#orgPwdConfirm").val();
		var adminPhonenumber = $("#orgPhoneNumber").val();
		var adminWebsite = $("#orgWebsite").val();
		var goodEmail = corEmailId.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
		apos = corEmailId.indexOf("@"); dotpos = corEmailId.lastIndexOf("."); lastpos = corEmailId.length - 1;
		var badEmail = (apos < 1 || dotpos - apos < 2 || lastpos - dotpos < 2);
		var url_regexp = new RegExp("[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?$");

		if (compName == "") {
			$.unblockUI();
			swal("Invalid data field!","Please Fill in Company Name","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgCompanyName").focus();
			return true;
		}
		if (contactName == "") {
			$.unblockUI();
			swal("Invalid data field!","Please Fill in Your Name","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgContactName").focus();
			return true;
		}
		if (corEmailId == "" || !goodEmail || badEmail) {
			$.unblockUI();
			swal("Invalid data field!","Please Fill in valid Company Email","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgCorporateEmail").focus();
			return true;
		}
		if (adminPass == "") {
			$.unblockUI();
			swal("Invalid data field!","Please Fill in Password","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgPassword").focus();
			return true;
		}
		if (adminPassConfirm == "") {
			$.unblockUI();
			swal("Invalid data field!","Please Fill in confirm password","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgPwdConfirm").focus();
			return true;
		}
		if (adminPass != adminPassConfirm) {
			$.unblockUI();
			swal("Invalid data field!","Both passwords do not match.","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgPwdConfirm").focus();
			return true;
		}
		var validationPassedNo = $(".validationPassedNo").val();
		if (validationPassedNo != "6") {
			$.unblockUI();
			swal("Invalid data field!","Password does not match the rules.","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgPassword").focus();
			return true;
		}
		if (!verifyPassword(adminPass)) {
			$.unblockUI();
			return true;
		}
		if (isEmpty(adminPhonenumber) || adminPhonenumber.lenght < 6) {
			$.unblockUI();
			swal("Invalid data field!","Provide valid phone/mobile number.","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgPhoneNumber").focus();
			return true;
		}
		if ((adminWebsite != "") && (!url_regexp.test(adminWebsite))) {
			$.unblockUI();
			swal("Invalid data field!","Enter valid website URL.","error");
			$('#addCompanyOrganization').removeAttr("disabled");
			$("#orgWebsite").focus();
			return true;
		}

		var jsonData = {};
		jsonData.companyName = compName;
		jsonData.contactName = contactName;
		jsonData.companyEmailId = corEmailId;
		jsonData.adminPwd = adminPass;
		jsonData.adminPhonenumber = adminPhonenumber;
		jsonData.registrationSource = "";
		jsonData.adminWebsite = adminWebsite;
		jsonData.userMode = 0;
		jsonData.companyId = companyId;
		var url = "/config/addOrganization";

		$.ajax({
			url: url,
			data: JSON.stringify(jsonData),
			type: "text",
			headers: {
				"X-AUTH-TOKEN": window.authToken
			},
			method: "POST",
			contentType: 'application/json',
			success: function (data) {
				$("#orgCompanyName").val("");
				$("#orgContactName").val("");
				$("#orgCorporateEmail").val("");
				$("#orgPassword").val("");
				$("#orgPwdConfirm").val("");
				$("#orgPhoneNumber").val("");
				$("#orgWebsite").val("");

				swal("Success!", "Welcome! Your organization has been successfully created. Please activate you account following activation link sent to your email", "success");

				//$("#companyOrgTable tbody").html("");
				let companyOrgTrList = "";
				companyOrgTrList += '<tr><td>' + data.companyOrgName + '</td><td>' + data.companyOrgPerName + '</td><td>' + data.companyOrgEmail + '</td><td>' + data.companyOrgPhoneNo + '</td><td>' + data.companyOrgWebsite + '</td></tr>';
				$("#companyOrgTable tbody").append(companyOrgTrList);
			},
			error: function (xhr, status, error) {
				notifyLogin.show(error, true, true);
			},
			complete: function (data) {
				$.unblockUI();
				setTimeout(function () {
					$('#addCompanyOrganization').removeAttr("disabled");
				}, 7000);
			}
		});
	});

	$('#signUpOrganization').click(function(){
		$("#signUpOrganization").attr("disabled", "disabled");
		$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var captchaLength = $("#recaptchaLength").val();
		var compName=$("#companyname").val();
		var contactName=$("#contactName").val();
		var corEmailId=$("#corporateemail").val();
		var adminPass=$("#orgpwd").val();
		var adminPassConfirm=$("#orgpwd_confirm").val();
		var adminPhonenumber=$("#phonenumber").val();
		var registrationSource=$("#registrationSource").val();
		var adminWebsite=$("#website").val();
		var userMode=$("#userMode").val();
		var goodEmail = corEmailId.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
		apos=corEmailId.indexOf("@");dotpos = corEmailId.lastIndexOf(".");lastpos=corEmailId.length-1;
		var badEmail = (apos<1 || dotpos-apos<2 || lastpos-dotpos<2);

		//var url_regexp = new RegExp("^http(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?$");
		var url_regexp = new RegExp("[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?$");
		if(compName==""){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			//notifyLogin.show('Provide company name.',true,true);
			$("#companyname").val("");
			$("#companyname").attr("placeholder", "Enter company/business name").blur();
			$("#companyname").focus();
			return true;
		}

		if(contactName==""){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			//notifyLogin.show('Provide company name.',true,true);
			$("#contactName").val("");
			$("#contactName").attr("placeholder", "Enter your name").blur();
			$("#contactName").focus();
			return true;
		}

		if(corEmailId=="" || !goodEmail || badEmail){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			//notifyLogin.show('Provide valid company/corporate Email Id.',true,true);
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			$("#corporateemail").val("");
			$("#corporateemail").attr("placeholder", "Enter company E-mail.").blur();
			$("#corporateemail").focus();
			return true;
		}
		if(adminPass==""){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			//notifyLogin.show('Provide Password For the Account.',true,true);
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			$("#orgpwd").attr("placeholder", "Enter password.").blur();
			$("#orgpwd").focus();
			return true;
		}
		if(adminPassConfirm==""){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			//notifyLogin.show('Provide Confirm Password For the Account.',true,true);
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			$("#orgpwd_confirm").attr("placeholder", "Enter password again.").blur();
			$("#orgpwd_confirm").focus();
			return true;
		}

		if(adminPass != adminPassConfirm){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			//notifyLogin.show('Provide Password and Confirm Password same.',true,true);
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			$("#orgpwd_confirm").val("");
			$("#orgpwd_confirm").attr("placeholder", "Both passwords do not match.").blur();
			$("#orgpwd_confirm").focus();
			return true;
		}

		var validationPassedNo=$(".validationPassedNo").val();
		if(validationPassedNo != "6"){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			notifyLogin.show('Provided Password does not match the rules.',true,true);
			$("#orgpwd").focus();
			return true;
		}
		if(!verifyPassword(adminPass)){
			$.unblockUI();
			return true;
		}
		if(isEmpty(adminPhonenumber) || adminPhonenumber.lenght < 6){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			//notifyLogin.show('Provide company name.',true,true);
			$("#phonenumber").val("");
			$("#phonenumber").attr("placeholder", "Provide valid phone/mobile number.").blur();
			$("#phonenumber").focus();
			return true;
		}

		if(registrationSource.lenght > 0 && registrationSource.lenght < 3){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			//notifyLogin.show('Provide company name.',true,true);
			$("#registrationSource").val("");
			$("#registrationSource").attr("placeholder", "Enter how you know about us.").blur();
			$("#registrationSource").focus();
			return true;
		}


		if ((adminWebsite !="") && (!url_regexp.test(adminWebsite))) {
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			grecaptcha.reset(newUserWidgetId);
			$("#recaptchaLength").val(0);
			$("#website").val("");
			$("#website").attr("placeholder", "Enter valid website URL.").blur();
			//notifyLogin.show('Provide valid website URL.',true,true);
			$("#website").focus();
			return true;
        }
		if(validateCaptcha === true && (captchaLength==0 || captchaLength == "")){
			$.unblockUI();
			$('#signUpOrganization').removeAttr("disabled");
			notifyLogin.show('Please select Captcha.',true,true);
			return false;
		}
		var jsonData = {};
		jsonData.companyName = compName;
		jsonData.contactName = contactName;
		jsonData.companyEmailId = corEmailId;
		jsonData.adminPwd = adminPass;
		jsonData.adminPhonenumber = adminPhonenumber;
		jsonData.registrationSource = registrationSource;
		jsonData.adminWebsite = adminWebsite;
		jsonData.userMode = userMode;
		jsonData.companyId = null;

		if(userMode == 1) {
			swal({
				  title: "Are you sure?",
				  text:  "Your will continue with Singleuser Mode otherwise change it to Multiuser mode",
				  type: "warning",
				  showCancelButton: true,
				  confirmButtonClass: "btn-danger",
				  confirmButtonText: "Procced",
				  closeOnConfirm: true
				},
				function(isConfirm){
					if (!isConfirm) {
						$.unblockUI();
						$('#signUpOrganization').removeAttr("disabled");
						return false;
					 } else {
						 addOrganizationModule(jsonData,newUserWidgetId);
					 }
				});
		}else {
			addOrganizationModule(jsonData,newUserWidgetId);
		}
	});
});

function addOrganizationModule(jsonData,newUserWidgetId) {
		var url="/config/addOrganization";
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
				$("#companyname").val("");
				$("#contactName").val("");
				$("#registrationSource").val("");
				$("#corporateemail").val("");
				$("#orgpwd").val("");
				$("#orgpwd_confirm").val("");
				$("#phonenumber").val("");
				$("#website").val("");
				$("#accountspanstat").html("Please log in to your account.");
				//$("#successregdiv").show();
				$("#successaccountmsg").html("");
				$("#successaccountmsg").append('Welcome, Your organization has been successfully created. Please activate you account following activation link sent to your email.');
				//$("#forgotlogindiv").hide();
				notifyLogin.show('Welcome! Your organization has been successfully created. Please activate you account following activation link sent to your email.',true,false);
				//$("#messageDisplay").html("");
				//$("#messageDisplay").append('Welcome, Your organization has been successfully created. Please activate you account following activation link sent to your email.');
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				$("#recaptchaLength").val(0);
				grecaptcha.reset(newUserWidgetId);
				notifyLogin.show(error,true,true);
			},
			complete: function(data) {
				$.unblockUI();
				setTimeout(function(){
					$('#signUpOrganization').removeAttr("disabled");
				}, 7000);
			}
	});
}

$(document).ready(function(){
	$( '.submitActivationButton' ).click(function() {
		var enteredUserMail=$("#enterregemail").val();
		var captchaLength=$("#recaptchaLength").val();
		if(enteredUserMail==""){
			grecaptcha.reset(actLinkWidgetId);
			$("#recaptchaLength").val(0);
			notifyLogin.show("Please Enter the email to which account activation link has to be sent.",true,true);
			return true;
		}else if(enteredUserMail!=""){
			if(emailValidation(enteredUserMail)){
				grecaptcha.reset(actLinkWidgetId);
				$("#recaptchaLength").val(0);
				notifyLogin.show("Please enter valid email address.",true,true);
				$("#enterregemail").val("");
				return true;
			}
		}
		if(validateCaptcha === true && (captchaLength==0 || captchaLength=="")){
			notifyLogin.show('Please select Captcha.',true,true);
			return false;
		}

		var jsonData = {};
		jsonData.useremail=enteredUserMail;
		var url="/resend/accountActivationLink";
		$.ajax({
			url         : url,
			data        : JSON.stringify(jsonData),
			type        : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},

			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				grecaptcha.reset(actLinkWidgetId);
				$("#recaptchaLength").val(0);
				notifyLogin.show(data.resendactivationresponsedata[0].result,true,true);
				$("#enterregemail").val("");
			},
			error : function (xhr, status, error) {
				grecaptcha.reset(actLinkWidgetId);
				$("#recaptchaLength").val(0);
			}
		})
	});
});


var verifyCode = function(){

	var url="/verification";
	var jsonData = {};
	//jsonData.securitycode=$("#securitycode").val();
	jsonData.securitycode='12345';
	$.ajax({
		url: url,
		data:JSON.stringify(jsonData),
		dataType: 'json',
		contentType: 'application/json',
		async: true,
		method:"POST",
		success: function (data) {
			if(data.message == "false"){
				notifyLogin.show("Invalid securitycode!",true,true);
				return false;
			}
			else{
				window.location.href=data.url+"?"+data.loggedin;
			}
			alwaysScrollTop();
		}
	});

}

var resendMail = function() {
	console.log("Resend mail");
	var url="/resendsecuritycode";
	$.ajax({
		url: url,
		data:JSON.stringify(null),
		dataType: 'json',
		contentType: 'application/json',
		async: true,
		method:"POST",
		success: function (data) {
			notifyLogin.show("Verification code is sent again!",true,true);
			alwaysScrollTop();
		}
	});
}
var userSignIn = function(elem){
		if(elem.id != 'loginButton'){
			return false;
		}

		var username=$("#loginuser").val();
		var password=$("#pass").val();
		var captchaLength = $("#recaptchaLength").val();
		if(username==""){
			grecaptcha.reset(loginWidgetId);
			$("#recaptchaLength").val(0);
			$("#loginuser").val("");
			$("#loginuser").attr("placeholder", "Please provide user name.").blur();
			return false;
		}else if(password==""){
			grecaptcha.reset(loginWidgetId);
			$("#recaptchaLength").val(0);
			$("#pass").val("");
			$("#pass").attr("placeholder", "Please provide login password.").blur();
			return false;
		}/* else if(validateCaptcha === true && (captchaLength==0 || captchaLength == "")){
			notifyLogin.show('Please select Captcha.',true,true);
			return false;} */
		else{
			var publicKeyID = $("#publicKeyID").text();
			var encrypt = new JSEncrypt();
			encrypt.setPublicKey(publicKeyID);
			var encryptedPassword = encrypt.encrypt(password);
			var jsonData = {};
			jsonData.userName = username;
			jsonData.loginpwd = encryptedPassword;

			var url="/login";
			$.ajax({
				url: url,
				data:JSON.stringify(jsonData),
				dataType: 'json',
      			contentType: 'application/json',
				async: true,
			    method:"POST",
				success: function (data) {
				if(data.wrongses == "false"){
						notifyLogin.show("Session expired.",true,true);
						doLogout();
						return false;
					}
					if(data.logincredentials[0].message=="Failure"){
						//alert(data.logincredentials[0].failurereason);
						notifyLogin.show(data.logincredentials[0].failurereason,true,true);
						$("#loginuser").val("");
						$("#pass").val("");
						grecaptcha.reset(loginWidgetId);
						$("#recaptchaLength").val(0);
						return false;
					}else{
                        window.location.href=data.url+"?"+data.loggedin;
					}
					alwaysScrollTop();
				},
				error: function (xhr, status, error) {
					grecaptcha.reset(loginWidgetId);
					$("#recaptchaLength").val(0);
				}

			});
		}
}

$(document).ready(function(){
	$('#requestresetlink').click(function(){
		var emailid=$("#logindetailemail").val();
		var goodEmail = emailid.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
		apos=emailid.indexOf("@");dotpos = emailid.lastIndexOf(".");lastpos=emailid.length-1;
		var badEmail    = (apos<1 || dotpos-apos<2 || lastpos-dotpos<2);
		var captchaLength = $("#recaptchaLength").val();

		if(emailid=="" || !goodEmail || badEmail){
			grecaptcha.reset(forgotpwdWidgetId);
			$("#recaptchaLength").val(0);
			//notifyLogin.show('Enter valid Email Id.',true,true);
			$("#logindetailemail").val("") ;
			$("#logindetailemail").attr("placeholder", "Enter valid Email Id.").blur();
			return false;
		}else if(validateCaptcha === true && (captchaLength==0 || captchaLength == "")){
			notifyLogin.show('Please select Captcha.',true,true);
			return false;
		}else{
			var jsonData = {};
			jsonData.emailId = emailid;
			var url="/forgotlogininfo";
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
					if(data.logininfocredential[0].message=="Failure"){
						$("#logindetailemail").val("");
						notifyLogin.show(data.logininfocredential[0].failurereason,true,true);
						return false;
					}else{
						$("#logindetailemail").val("");
						notifyLogin.show(data.logininfocredential[0].message,true,false);
						return false;
					}
					alwaysScrollTop();
					$("#recaptchaLength").val(0);
					grecaptcha.reset(forgotpwdWidgetId);

				},
				error: function (xhr, status, error) {
					$("#recaptchaLength").val(0);
					grecaptcha.reset(forgotpwdWidgetId);
				}
			});
		}
	});
});


var captchaCallback= function(recaptchaResponse) {
	$("#recaptchaLength").val(recaptchaResponse.length);
	if(recaptchaResponse.length == 0){
		notifyLogin.show("You can't leave Captcha Code empty.",true,true);
		document.getElementById('captcha').innerText="You can't leave Captcha Code empty";
		return false;
	}

	var jsonData = {};
	jsonData.recaptchaResponse = recaptchaResponse;
	var url="/verify";
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
			if(data.verifiedValue == "true"){
				// $('.recaptchaButton').removeAttr('disabled');
			}else{
				grecaptcha.reset();
			}
		},
		error: function (xhr, status, error) {
			console.log(error);
		}
	});
};

function get_action(form) {
	swal("Error!","action","error");
	var v = grecaptcha.getResponse();

	v = $("#g-recaptcha-response").val();

	if(v.length == 0){
		document.getElementById('captcha').innerText="You can't leave Captcha Code empty";
		return false;
	}
	if(v.length != 0) {
		document.getElementById('captcha').innerText="Captcha completed";
		return true;
	}
}

 var verifyCallback = function(response) {
	swal("Error!",response,"error");
  };

var loginWidgetId; var newUserWidgetId; var forgotpwdWidgetId; var actLinkWidgetId;
var sellerLoginWidgetId; var newSellerWidgetId; var frgtpwdSellerWidgetId;
var countrySelectWidgetId;
var demoPageWidgetId;

var onloadCallback = function() {
	// Renders the HTML element with id 'example1' as a reCAPTCHA widget.
	// The id of the reCAPTCHA widget is assigned to 'widgetId1'.

	if($("#actLinkRecaptcha").length > 0){
		actLinkWidgetId = grecaptcha.render('actLinkRecaptcha', {
	    'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
	   'callback' : captchaCallback
		});

	}else{
		loginWidgetId = grecaptcha.render('loginRecaptcha', {
		  'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
		  'theme' : 'light',
		  'callback' : captchaCallback,
		  'data-type': 'audio'
		});

		newUserWidgetId = grecaptcha.render('newUserRecaptcha', {
		  'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
		   'callback' : captchaCallback
		});

		forgotpwdWidgetId = grecaptcha.render(document.getElementById('forgotpwdRecaptcha'), {
		  'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
		   'callback' : captchaCallback
		});


		sellerLoginWidgetId = grecaptcha.render(document.getElementById('sellerLoginRecaptcha'), {
		  'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
		   'callback' : captchaCallback
		});

		newSellerWidgetId = grecaptcha.render(document.getElementById('newSellerRecaptcha'), {
		  'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
		   'callback' : captchaCallback
		});

		frgtpwdSellerWidgetId = grecaptcha.render(document.getElementById('frgtpwdSellerRecaptcha'), {
		  'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
		   'callback' : captchaCallback
		});

		countrySelectWidgetId = grecaptcha.render(document.getElementById('countrySelectRecaptcha'), {
		  'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
		   'callback' : captchaCallback
		});

		demoPageWidgetId = grecaptcha.render(document.getElementById('demoPageRecaptcha'), {
		  'sitekey' : '6LfUCncjAAAAAOgoN1wDFW1493uxvCg_6Hwp-YKA',
		   'callback' : captchaCallback
		});
	}
};

$(document).ready(function() {
	$('input[type=password]').keyup(function() {
		if(this.id == "oldPassword" || this.id == "settingCurPwd"){
			return false;
		}
		var pswd = $(this).val();
		var validationPassedNo=0;

		if ( pswd.length < 8 ) {
			$('.lengthPwd').removeClass('valid').addClass('invalid');
		} else {
			$('.lengthPwd').removeClass('invalid').addClass('valid');
			validationPassedNo++;
		}

		if ( pswd.match(/[A-z]/) ) {
			$('.letterPwd').removeClass('invalid').addClass('valid');
			validationPassedNo++;
		} else {
			$('.letterPwd').removeClass('valid').addClass('invalid');
		}

		if ( pswd.match(/[a-z]/) ) {
			$('.smallPwd').removeClass('invalid').addClass('valid');
			validationPassedNo++;
		} else {
			$('.smallPwd').removeClass('valid').addClass('invalid');
		}

		if ( pswd.match(/[A-Z]/) ) {
			$('.capitalPwd').removeClass('invalid').addClass('valid');
			validationPassedNo++;
		} else {
			$('.capitalPwd').removeClass('valid').addClass('invalid');
		}

		if ( pswd.match(/\d/) ) {
			$('.numberPwd').removeClass('invalid').addClass('valid');
			validationPassedNo++;
		} else {
			$('.numberPwd').removeClass('valid').addClass('invalid');
		}

		if ( pswd.match(/[!@#$%^&*()_]/) ) {
			$('.specialcharPwd').removeClass('invalid').addClass('valid');
			validationPassedNo++;
		} else {
			$('.specialcharPwd').removeClass('valid').addClass('invalid');
		}

		$(".validationPassedNo").val(validationPassedNo);

	}).focus(function() {
		if(this.id == "oldPassword" || this.id == "settingCurPwd"){
			return false;
		}
		$('.pswd_info').show();
	}).blur(function() {
		if(this.id == "oldPassword" || this.id == "settingCurPwd"){
			return false;
		}
		$('.pswd_info').hide();
	});
});



$(document).ready(function(){
	$('.sellerAccountLoginButton'). click(function(){
		var username=$("#sellerAccountUser").val();
		var password=$("#sellerAccountPass").val();
		var captchaLength = $("#recaptchaLength").val();
		if(username==""){
			grecaptcha.reset(sellerLoginWidgetId);
			$("#recaptchaLength").val(0);
			notifyLogin.show('Please enter valid login email.',true,true);
			$('#sellerAccountUser').focus();
			return true;
		}else if(password==""){
			grecaptcha.reset(sellerLoginWidgetId);
			$("#recaptchaLength").val(0);
			notifyLogin.show('Please enter valid password.',true,true);
			$('#sellerAccountPass').focus();
			return true;
		}else if(validateCaptcha === true && (captchaLength==0 || captchaLength == "")){
			notifyLogin.show('Please select Captcha.',true,true);
			return false;
		}
		var publicKeyID = $("#publicKeyID").text();
		var encrypt = new JSEncrypt();
		encrypt.setPublicKey(publicKeyID);
		var encryptedPassword = encrypt.encrypt(password);
		var jsonData = {};
		jsonData.userName = username;
		jsonData.loginpwd = encryptedPassword;
		var url="/sellerlogin";
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
				if(data.wrongses == "false"){
					notifyLogin.show("Session expired, refresh the page and retry.",true,true);
					return false;
				}
				if(data.logincredentials[0].message=="success"){
					window.location.href="/seller";
				}else if(data.logincredentials[0].message=="Failure"){
					alwaysScrollTop();
					notifyLogin.show(data.logincredentials[0].failurereason,true,true);
					$("#sellerAccountUser").val("");
					$("#sellerAccountPass").val("");
					grecaptcha.reset(sellerLoginWidgetId);
					$("#recaptchaLength").val(0);
				}
			},
			error: function (xhr, status, error) {

				grecaptcha.reset(sellerLoginWidgetId);
				$("#recaptchaLength").val(0);
			}
		});

	});
});


function sellerRegistration() {
	var accountName = $.trim($('#sellerAccountname').val());
	var email = $.trim($('#sellerAccountemail').val());
	var pass = $.trim($('#sellerAccountpwd').val());
	var goodEmail = email.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
    var apos=email.indexOf("@"), dotpos = email.lastIndexOf("."), lastpos=email.length-1;
    var badEmail    = (apos<1 || dotpos-apos<2 || lastpos-dotpos<2);

	if(isEmpty(accountName)){
		grecaptcha.reset(newSellerWidgetId);
		$("#recaptchaLength").val(0);
		notifyLogin.show('Please provide your company name.',true,true);
		$('#sellerAccountname').focus();
		return false;
	}else if('' == email || !goodEmail || badEmail){
		grecaptcha.reset(newSellerWidgetId);
		$("#recaptchaLength").val(0);
		notifyLogin.show('Please provide a valid email id.',true,true);
		$('#sellerAccountemail').focus();
		return false;
	}else if(isEmpty(pass)){
		grecaptcha.reset(newSellerWidgetId);
		$("#recaptchaLength").val(0);
		notifyLogin.show('Please provide a password.',true,true);
		$('#sellerAccountpwd').focus();
		return false;
	}

	var validationPassedNo=$(".validationPassedNo").val();
	if(validationPassedNo != "6"){
		grecaptcha.reset(newSellerWidgetId);
		$("#recaptchaLength").val(0);
		notifyLogin.show('Provided Password does not match the rules.',true,true);
		$("#sellerAccountpwd").focus();
		return true;
	}


	var captchaLength = $("#recaptchaLength").val();
	if(validateCaptcha === true && (captchaLength==0 || captchaLength == "")){
		notifyLogin.show('Please select Captcha.',true,true);
		return false;
	}

	if ('' !== accountName && ('' !== email && goodEmail && !badEmail) && '' !== pass) {
		var jsonData = {};
		jsonData.accountName = accountName;
		jsonData.email = email;
		jsonData.password = pass;
		ajaxCall('/sellerSignUp', jsonData, '', '', '', '', 'sellerSignUpSuccess', '', true);
	}
}

$(document).ready(function(){
	$('#sellerrequestresetlink').click(function(){
		var emailid=$("#sellerlogindetailemail").val();
		var goodEmail = emailid.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
		apos=emailid.indexOf("@");dotpos = emailid.lastIndexOf(".");lastpos=emailid.length-1;
		var badEmail    = (apos<1 || dotpos-apos<2 || lastpos-dotpos<2);
		if(emailid=="" || !goodEmail || badEmail){
			grecaptcha.reset(frgtpwdSellerWidgetId);
			$("#recaptchaLength").val(0);
			notifyLogin.show("Please enter valid email id",true,true);
			return true;
		}

		var captchaLength = $("#recaptchaLength").val();
		if(validateCaptcha === true && (captchaLength==0 || captchaLength == "")){
			notifyLogin.show('Please select Captcha.',true,true);
			return false;
		}

		var jsonData = {};
		jsonData.emailId = emailid;
		var url="/sellerforgotlogininfo";
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
				grecaptcha.reset(frgtpwdSellerWidgetId);
				$("#recaptchaLength").val(0);
				if(data.logininfocredential[0].message=="Failure"){

					$("#sellerforgotsuccessregdiv").show();
					$("#sellerforgotsuccessaccountmsg").html("");
					$("#sellerforgotsuccessaccountmsg").append(''+data.logininfocredential[0].failurereason+'');
					notifyLogin.show(data.logininfocredential[0].failurereason,true,true);
					$("#sellerlogindetailemail").val("");
					return true;
				}else{
					$("#sellerforgotsuccessregdiv").show();
					$("#sellerlogindetailemail").val("");
					$("#sellerforgotsuccessaccountmsg").html("");
					$("#sellerforgotsuccessaccountmsg").append(''+data.logininfocredential[0].message+'');
					notifyLogin.show(data.logininfocredential[0].message,true,false);
				}

			},
			error: function (xhr, status, error) {
				grecaptcha.reset(frgtpwdSellerWidgetId);
				$("#recaptchaLength").val(0);
			}
		});

	});
});


$(document).ready(function() {
    $('#allOrgTable').DataTable();
});


/*Password Expiry Starts*/
$(document).ready(function(){
	$('#submitPasswordExpiry').click(function(){
		var email = $('#userEmail').val();
		if ('' === email || undefined === email || null === email) {
			email = $("#hiddenuseremail").text();
		}
		var oldPwd = $('#oldPassword').val();
		var newPwd = $('#newPassword').val();
		var confirmPwd = $('#newConfirmPassword').val();
		if ('' !== email || undefined !== email || null !== email) {
			if ('' !== oldPwd || undefined !== oldPwd || null !== oldPwd) {
				if (newPwd !== confirmPwd) {
					idosalert.show('Passwords do not match.');
				} else if (oldPwd === newPwd) {
					idosalert.show('Old and New Password cannot be same.');
				} else {
					if(!verifyPassword(newPwd)){
						return false;
					}
					var jsonData = {};
					jsonData.usermail = email;
					jsonData.oldPwd = oldPwd;
					jsonData.newPwd = newPwd;
					jsonData.confirmPwd = confirmPwd;
					ajaxCall('/passwordExpiry/reset', jsonData, '', '', '', '', 'passwordExpirySuccess', '', true);
				}
			} else {
				idosalert.show('Old Password Empty.');
			}
		} else {
			idosalert.show('Email Empty.');
		}
	});
});

function passwordExpirySuccess(data) {
	data = data.toLowerCase();
	if ('wrongpassword' === data) {
		idosalert.show('Please enter your old password correctly.');
	} else if ('noequal' === data) {
		idosalert.show('Passwords does not match.');
	} else if ('success' === data) {
		doLogout();
		window.location.href = '/pwdexpsuccess';
	} else if ('passwordpresent' === data) {
		idosalert.show('Password already used.');
	} else {
		idosalert.show('User not found.');
	}
}
/*Password Expiry Ends*/


$(document).ready(function() {
	$(".resetlogincred").click(function(){
		var email=$("#resetemail").val();
		var newPassword=$("#resetnewpass").val();
		var confirmPassword=$("#resetconfirmpass").val();
		if(newPassword==""){
			swal("Invalid data field!","Enter New Account Password","error");
		    return true;
	    }
	   	if(newPassword.length<8){
			swal("Invalid data field!","Password cannot be less than length 8.","error");
		    return true;
	    }
	   	if(confirmPassword==""){
			swal("Invalid data field!","Please Confirm The New Password For The Account","error");
		    return true;
	   	}
	   	if(newPassword!=confirmPassword){
			swal("Invalid data field!","Mismatch During Confirm Password","error");
		    return true;
	   	}
		var jsonData = {};
		jsonData.resetemail= email;
		jsonData.resetpassword  = newPassword;
		jsonData.update = true;
		if($('#securityAnswerDiv').is(':visible')) {
			var answer = $('#resetSecurityAnswer').val();
			if ('' === answer) {
				swal('Invalid data field!','Please Provide the Security Answer.','error');
				$('#resetSecurityAnswer').focus();
				return true;
			} else {
				jsonData.questionId = $('#resetSecurityquestionId').val();
				jsonData.question = $('#resetSecurityquestion').val();
				jsonData.answer = answer;
				jsonData.update = false;
			}
		}
		var url="/user/resetPassword";
		$.ajax({
			url         : url,
			data        : JSON.stringify(jsonData),
			type        : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				$("#resetnewpass").val("");
				$("#resetconfirmpass").val("");
				notifyLogin.show(data.resetinfocredential[0].message,true,!data.resetinfocredential[0].result);
				if(data.resetinfocredential[0].result == "true") {
                    window.location.href = "forgotpwdresetsuccess";
                }
	        },
			error: function (xhr, status, error) {
				swal("Error on rest password!", "Please retry, if problem persists contact support team", "error");
			}
		})
	});
});

$(document).ready(function() {
	$(".sellerresetlogincred").click(function(){
		var email=$("#sellerresetemail").val();
		var newPassword=$("#sellerresetnewpass").val();
		var confirmPassword=$("#sellerresetconfirmpass").val();
		if(newPassword==""){
			swal("Invalid data field!","Enter New Account Password","error");
		    return true;
	    }
	   	if(newPassword.length<8){
			swal("Invalid data field!","Password cannot be less than length 8.","error");
		    return true;
	    }
	   	if(confirmPassword==""){
			swal("Invalid data field!","Please Confirm The New Password For The Account","error");
		    return true;
	   	}
	   	if(newPassword!=confirmPassword){
			swal("Invalid data field!","Mismatch During Confirm Password","error");
		    return true;
	   	}
		var jsonData = {};
		jsonData.resetemail= email;
		jsonData.resetpassword  = newPassword;
		var url="/seller/resetPassword";
		$.ajax({
			url         : url,
			data        : JSON.stringify(jsonData),
			type        : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				$("#sellerresetnewpass").val("");
				$("#sellerresetconfirmpass").val("");
				$("#sellerresetsuccessregdiv").show();
				$("#resetsuccessaccountmsg").html("");
				$("#sellerresetsuccessaccountmsg").append(''+data.resetinfocredential[0].message+'');
				alwaysScrollTop();
	        },
			error: function (xhr, status, error) {

			}
		})
	});
});


/*Privacy Setting Starts*/
function openPrivacySetting() {
	var jsonData = {};
	jsonData.email = $("#hiddenuseremail").text();
	jsonData.type = 'general';
	$('ul.nav').css('margin-bottom', '0');
	ajaxCall('/accountSetting/getUserDetails', jsonData, '', '', '', '', 'generalDetailsSuccess', '', true);
	var areaCodesLen = $('#settingPhoneCode').children().length;
	if (areaCodesLen <= 1) {
		ajaxCall('/feedback/getAreaCodes', '', '', '', '', '', 'populateAreaCodes', '', false);
	}
	alwaysScrollTop();
	$('.tab-pane').fadeOut(600);
	$('#privaySettingDiv').fadeIn(600);
	$('.common-rightpanel').fadeOut();
	$('#generalSetting_content').fadeIn(1000);
	$('body').css('overflow', 'hidden');
	$('#systemconfigadminId1').find('.active').removeClass('active');
}

function populateAreaCodes(data) {
	data = data.result;
	$('#settingPhoneCode').html('').append('<option value="">--Please Select--</option>');
	$(data).each(function(i) {
		$('#settingPhoneCode').append('<option value="' + data[i].areaCode + '">' + data[i].countryCode + '</option>');
	});
}

function generalDetailsSuccess(data) {
	data = data.result;
	if (data.length > 0) {
		var dayStr = '';
		data = data[0];
		if (data.lastLoginDays == 0) {
			dayStr = ' (Today.)';
		} else if (data.lastLoginDays == 1) {
			dayStr = ' (Yesterday.)';
		} else {
			dayStr = ' (' + data.lastLoginDays + ' days ago.)';
		}
		$('#settingLastLogin').html(data.lastLogin + dayStr);
		if (data.lastPwdChangeDays == 0) {
			dayStr = ' (Today.)';
		} else if (data.lastPwdChangeDays == 1) {
			dayStr = ' (Yesterday.)';
		} else {
			dayStr = ' (' + data.lastPwdChangeDays + ' days ago.)';
		}
		$('#settingPwdChange').html(data.lastPwdChange + dayStr);
		$('#settingUserId').val(data.id);
		$('#settingUserEmail').val(data.email);
	}
}

function userDetailsSuccess(data) {
	data = data.result;
	if (data.length > 0) {
		data = data[0];
		if ('' !== data.id && undefined !== data.id && null !== data.id) {
			$('#settingUserId').val(data.id);
			$('#settingUserEmail').val(data.email);
			$('#settingDob').val(data.dob);
			$('#settingAddress').val(data.address);
			$('#settingBloodGroup').val(data.bloodGroup);
			$('#settingIdProof').val(data.idProof);
			$('#settingPhoneCode').val(data.phoneCode);
			$('#settingPhone1').val(data.phone.substring(0, 3));
			$('#settingPhone2').val(data.phone.substring(3, 6));
			$('#settingPhone3').val(data.phone.substring(6, 10));
			var question = data.question;
			if ('' !== question && undefined !== question && null !== question) {
				$('#profileSettingSecurity').html('<div class="column-rightpanel" style="width: 400px;" id="profileSettingQuestion">' + question + '</div>'
						+ '<input type="hidden" id="profileSettingQuestionId" value="' + data.questionId + '" />'
						+ '<input type="password" id="profileSettingAnswer" class="columnvalue-rightpanel" value="" style="width: 272px;" />');
			} else {
				$('#profileSettingSecurity').html('<span id="profileSecuritySpan">Answer a security question in Security tab to add more security to your profile.</span>');
			}
		} else {
			settingNotify('Problem in fetching information. Please try after sometime.', 'notify-error-small');
		}
	}
}

function successUpdateUser(data) {
	var jsonData = {};
	jsonData.userId = $('#settingUserId').val();
	jsonData.userEmail = $('#settingUserEmail').val();
	ajaxCall('/accountSetting/getUserSecurityQuestions', jsonData, '', '', '', '', 'securityQuestionSuccess', '', true);
	if (undefined !== data) {
		if ('success' === data.result.toLowerCase()) {
			data = data.message;
			if ('' !== data || undefined !== data || null !== data) {
				settingNotify(data, 'notify-success-small');
			} else {
				settingNotify('Profile updation successful.', 'notify-success-small');
			}
		} else if ('failure' === data.result.toLowerCase()) {
			data = data.message;
			if ('' !== data || undefined !== data || null !== data) {
				settingNotify(data, 'notify-error-small');
			} else {
				settingNotify('Profile updation unsuccessful.', 'notify-error-small');
			}
		}
	}
}

function passwordDetailsSuccess(data) {
	var question = data.question;
	if ('' !== question && undefined !== question && null !== question) {
		$('#passwordSettingSecurity').html('<div class="column-rightpanel" style="width: 400px;" id="passwordSettingQuestion">' + question + '</div>'
				+ '<input type="hidden" id="passwordSettingQuestionId" value="' + data.questionId + '" />'
				+ '<input type="password" id="passwordSettingAnswer" class="columnvalue-rightpanel" style="width: 250px;" value="" />');
	} else {
		$('#passwordSettingSecurity').html('<span id="passwordSecuritySpan">Answer a security question in Security tab to add more security to your profile.</span>');
	}
}

function securityQuestionSuccess(data) {
	data = data.security;
	if (data.length > 0) {
		var answerField = '';
		$('#securitySetting_table').html('');
		$(data).each(function(i) {
			if ('' !== this.question && undefined !== this.question && null !== this.question) {
				if ('' !== this.answer && undefined !== this.answer && null !== this.answer) {
					answerField = '<input type="password" id="securityAnswer_' + i + '" value="' + this.answer + '" class="columnvalue-rightpanel" style="width: 400px;"/>';
				} else {
					answerField = '<input type="text" id="securityAnswer_' + i + '" value="" class="columnvalue-rightpanel" style="width: 400px;"/>';
				}
				$('#securitySetting_table').append('<div class="row-rightpanel securityQuestions">'
		 				+ '<div class="column-rightpanel" id="securityQuestion_' + i + '" style="width: 400px;">' + this.question + '</div>' + answerField
						+ '</div>');
			}
		});
		if ($('#securitySetting_content #securitySetting_table').children().length > 0) {
			$('#securitySetting_table').append('<div class="row-rightpanel btn-rightpanel" style="border-bottom: 0;">'
						+ '<div class="columnvalue-rightpanel" style="height: 208px; padding-left: 400px;">'
						+ '	<button id="settingSecurityAnswers" class="btn btn-submit btn-idos" title="Update Profile">Update Answers</button>'
						+ '</div></div>');
		}
	} else {
		settingNotify('Problem in fetching information. Please try after sometime.', 'notify-error-small');
	}
}

function resetPageSecurity(data) {
	var question = data.question;
	if ('' !== question && undefined !== question && null !== question) {
		$('#resetSecurityAnswer').attr('placeholder', question.toUpperCase());
		$('#resetSecurityquestion').val(question);
		$('#securityquestion').html("");
		$('#securityquestion').append(question);
		$('#resetSecurityquestionId').val(data.questionId);
		$('#securityAnswerDiv').show();
	} else {
		$('#resetSecurityAnswer').attr('placeholder', '');
		$('#resetSecurityquestionId').val('');
		$('#securityAnswerDiv').hide();
		$('#securityquestion').html("");
	}
}

function userPasswordReset(data) {
	if (data.message) {
		$("#resetLoginCred input:enabled:visible:first").focus();
		$("#resetLoginCred input:enabled:visible:first").attr('placeholder','');
		$('.signin').fadeIn(1000);
		$(data.locHash).show();
		$("#logindiv").hide();
		$("#accountspanstat").html("Reset Your Account.");
		$("#leftboxtd").attr("valign","middle");
	  	$("#resetemail").val(data.email);
	  	resetPageSecurity(data);
	} else {
		swal("Link expired!", "Reset password link is expired, please regenerate again.", "error");
	}
}

$(document).ready(function() {
	$('body').on('click', '#settingSecurityAnswers', function() {
		var length = $('#securitySetting_table').children('.securityQuestions').length;
		var answer = '';
		var jsonData = {};
		jsonData.userId = $('#settingUserId').val();
		jsonData.userEmail = $('#settingUserEmail').val();
		jsonData.length = length;
		for (var i = 0; i < length; i++) {
			answer = $('#securityAnswer_' + i).val();
			jsonData['question_' + i] = $('#securityQuestion_' + i).text();
			jsonData['answer_' + i] = answer;
		}
		ajaxCall('/accountSetting/saveUserSecurityAnswers', jsonData, '', '', '', '', 'successUpdateUser', '', false);
	});

	$('#settingSaveProfile').on('click', function() {
		var id = $('#settingUserId').val();
		var email = $('#settingUserEmail').val();
		var answer = $('#profileSettingAnswer').val();
		var question = $('#profileSettingQuestion').text();
		var questionId = $('#profileSettingQuestionId').val();
		var jsonData = {};
		if ('' === answer) {
			settingNotify('Provide the Security Answer.', 'notify-error-small');
			return true;
		} else if ('' !== $('#profileSecuritySpan').text()) {
			jsonData.update = true;
		} else if ('' === $('#profileSecuritySpan').text()) {
			jsonData.update = false;
		}
		if (undefined !== answer) {
			jsonData.answer = answer;
		}
		if ('' !== question) {
			jsonData.question = question;
		}
		if (undefined !== questionId) {
			jsonData.questionId = questionId;
		}
		if (('' !== id || null !== id || undefined != id) && ('' !== email || null !== email || undefined != email)) {
			jsonData.userId = id;
			jsonData.email = email;
			jsonData.dob = $('#settingDob').val();
			jsonData.address = $('#settingAddress').val();
			jsonData.bloodGroup = $('#settingBloodGroup').val();
			jsonData.idProof = $('#settingIdProof').val();
			jsonData.phoneCodeText = $('#settingPhoneCode option:selected').text();
			jsonData.phoneCodeValue = $('#settingPhoneCode').val();
			jsonData.phone = $('#settingPhone1').val() + $('#settingPhone2').val() + $('#settingPhone3').val();
			ajaxCall('/accountSetting/updateUserProfile', jsonData, '', '', '', '', 'successUpdateUser', '', false);
		}
	});


	$('ul.nav a[data-toggle="tab"]').on('click', function() {
		$('#privaySettingDiv,#chatMessagesList').slideUp();
		$('body').css('overflow', 'auto');
		alwaysScrollTop();
	});

	$('.common-image-leftpanel').on('click', function() {
		var jsonData = {};
		jsonData.email = $("#hiddenuseremail").text();
		if ('generalSetting' === this.id) {
			jsonData.type = 'general';
			ajaxCall('/accountSetting/getUserDetails', jsonData, '', '', '', '', 'generalDetailsSuccess', '', true);
		} else if ('profileSetting' === this.id) {
			jsonData.type = 'profile';
			ajaxCall('/accountSetting/getUserDetails', jsonData, '', '', '', '', 'userDetailsSuccess', '', true);
		} else if ('securitySetting' === this.id) {
			$('#securitySetting_content #securitySetting_table').html('');
			jsonData.userId = $('#settingUserId').val();
			jsonData.userEmail = $('#settingUserEmail').val();
			ajaxCall('/accountSetting/getUserSecurityQuestions', jsonData, '', '', '', '', 'securityQuestionSuccess', '', true);
		} else if ('passwordSetting' === this.id) {
			jsonData.userId = $('#settingUserId').val();
			jsonData.userEmail = $('#settingUserEmail').val();
			ajaxCall('/accountSetting/getUserRandomQuestion', jsonData, '', '', '', '', 'passwordDetailsSuccess', '', true);
		}
		$('.common-rightpanel:visible').hide();
		$('#' + this.id + '_content').fadeIn(500);
	});

	$('.settingClear').on('click', function() {
		$('.common-rightpanel:visible').find('input[type="text"]').val('');
		$('.common-rightpanel:visible').find('input[type="password"]').val('');
		$('.common-rightpanel:visible').find('select option:first').prop('selected', 'selected');
	});

	$('#settingResetPwd').on('click', function() {
		var actCred=$("#settingPwdEmail").text();
		var oldPassword=$("#settingCurPwd").val();
		var newPassword=$("#settingNewPwd").val();
		var confirmPassword=$("#settingConfirmPwd").val();
		var answer = $('#passwordSettingAnswer').val();
		var question = $('#passwordSettingQuestion').text();
		var questionId = $('#passwordSettingQuestionId').val();
		var jsonData = {};
		if ('' === answer) {
			settingNotify('Provide the Security Answer.', 'notify-error-small');
			return true;
		} else if(actCred==""){
			swal("Invalid data field!","Please Provide Account Username Or Email Id","error");
		    return true;
	    } else if(oldPassword==""){
			swal("Invalid data field!","Please Provide Password For The Account","error");
		    return true;
	    } else if(newPassword==""){
			swal("Invalid data field!","Enter New Account Password","error");
		    return true;
	    } else if(confirmPassword==""){
			swal("Invalid data field!","Please Confirm The New Password For The Account","error");
		    return true;
	   	} else if(newPassword!=confirmPassword){
			swal("Invalid data field!","Mismatch During Confirm Password","error");
		    return true;
	   	}
	   	if(!verifyPassword(newPassword)){
			return false;
		}
		if ('' !== $('#passwordSecuritySpan').text()) {
			jsonData.update = true;
		} else if ('' === $('#passwordSecuritySpan').text()) {
			jsonData.update = false;
		}
		if (undefined !== answer) {
			jsonData.answer = answer;
		}
		if (undefined !== question) {
			jsonData.question = question;
		}
		if (undefined !== questionId) {
			jsonData.questionId = questionId;
		}
	   	jsonData.userId = $('#settingUserId').val();
		jsonData.accounttCred= actCred;
		jsonData.oldPwd= oldPassword;
		jsonData.newPwd  = newPassword;
		var url="/user/ChangePassword";
		$.ajax({
			url         : url,
			data        : JSON.stringify(jsonData),
			type        : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				var pwdchangedStat=data.userChangedPwdData[0].pwdchanged;
				if(pwdchangedStat=="Password Changed Succesfully."){
					$('#passwordSetting_content input[type="password"]').val('');
					settingNotify(pwdchangedStat, 'notify-success-small');
				} else if(pwdchangedStat=="Not Able to Find Account with Provided Account Credential."){
					$('#passwordSetting_content #settingCurPwd').focus().val('');
					settingNotify(pwdchangedStat, 'notify-error-small');
				} else if(pwdchangedStat=="Password Provided has been Already Used in the Past History. Please try a Unique Password."){
					$('#passwordSetting_content #settingNewPwd').focus().val('');
					$('#passwordSetting_content #settingConfirmPwd').val('');
					settingNotify(pwdchangedStat, 'notify-error-small');
				} else if(pwdchangedStat=="Security Answer Provided is not as per the records. Please Correct you Answer."){
					$('#passwordSetting_content #passwordSettingAnswer').focus().val('');
					settingNotify(pwdchangedStat, 'notify-error-small');
				}
				alwaysScrollTop();
	        },
			error: function (xhr, status, error) {
				closeSettingNotification();
				if(xhr.status == 401){ doLogout(); }
			}
		});
	});
});

function settingNotify(msg, className) {
	if ('' === className || null === className || undefined === className) {
		className = 'notify-success-small';
	}
	$('#settingMessage').addClass(className).html(msg).animate({'height': '30px', 'padding-top': '8px'}, 400);
	closeSettingNotification();
}

function closeSettingNotification() {
	setTimeout(function(){
		$('#settingMessage').css('class', '').html('').animate({'height': '0', 'padding-top': '0'}, 400);
	}, 6000);
}
/*Privacy Setting Ends*/

$(document).ready(function(){
	$('.subsloginButton'). click(function(){
		var username=$("#subsloginuser").val();
		var password=$("#subspass").val();
		if(username=="" || password==""){
			swal("Invalid data field!","Please provide Login Credentilas.","error")
			return true;
		}else{
			var jsonData = {};
			jsonData.userName = username;
			jsonData.loginpwd = password;
			var url="/subslogin";
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
					if(data.logincredentials[0].message=="Failure"){
						swal("Error!",data.logincredentials[0].failurereason,"error");
						$("#subsloginuser").val("");
						$("#subspass").val("");
						return true;
					}else{
						window.location.href="/selectsubscription";
					}
					alwaysScrollTop();
				},
				error: function (xhr, status, error) {

				}
			});
		}
	});
});

function checkemail(elem){
	var locHash=window.location.hash;
	if(locHash != "#signUpDiv")
		return false;
	var emailid=$(elem).val();

	onFocusClickEmail=$(elem);
	if(enteredUserEmail==emailid){
	   enteredUserEmail="";
	   return;
	}
	if(enteredUserEmail!=emailid){
		if(emailid.length>1){
			var jsonData = {};
			jsonData.email = emailid;
			var url="/users/checkemail";
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
					$("#dupemaillabel").html("");
					if(data.userExistData[0].dupusrmessage!='Email Available.'){
						$(elem).val("");
						$(elem).focus();
						if($(elem).attr('id')=='corporateemail' && $(elem).hasClass('input')){
							notifyLogin.show('Email Already registered with system.',true,true);
						}else{
							swal("Error!","Email Already registered with system.","error");
						}
						return;
					}
				},
				error: function (xhr, status, error) {
					if(xhr.status == 401){ doLogout(); }
				}
			});
		}
	}
}


$(document).ready(function() {
	$("#changeuserpwd").click(function(){
		var email=$("#resetemail").val();
		var newPassword=$("#resetnewpass").val();
		var confirmPassword=$("#resetconfirmpass").val();
		var confirmPassword=$("#resetconfirmpass").val();
		if(newPassword==""){
			swal("Invalid data field!","Enter New Password","error");
	   	    $("#resetnewpass").focus();
		    return true;
	    }
	   	if(newPassword.length<8){
			swal("Invalid data field!","Password cannot be less than length 8.","error");
	   	    $("#resetnewpass").focus();
		    return true;
	    }
	   	if(confirmPassword==""){
			swal("Invalid data field!","Please Confirm The New Password For Account","error");
	       $("#resetconfirmpass").focus();
		    return true;
	   	}
	   	if(newPassword!=confirmPassword){
			swal("Invalid data field!","Mismatch During Confirm Password","error");
	   		 $("#resetnewpass").focus();
		    return true;
	   	}
	   	var activateuser=1;
	   	if($("#activatecheck").is(':checked')==true){
	   		activateuser = 1;
	   	}else{
	   		activateuser = 0;
	   	}
	   	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var jsonData = {};
		jsonData.resetemail= email;
		jsonData.resetpassword  = newPassword;
		jsonData.activatation = activateuser;
		var url="/user/resetaccount";
		$.ajax({
			url         : url,
			data        : JSON.stringify(jsonData),
			type        : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				$("#resetnewpass").val("");
				$("#resetconfirmpass").val("");
				//notifyLogin.show(data.resetinfocredential[0].message,true,!data.resetinfocredential[0].result);

				$('.form-area').before('<span class="error" style="color: red;">'+data.resetinfocredential[0].message+'</span>');
					setTimeout(function(){
						$('.error').fadeOut('normal', function() {
							$(this).remove();
						});
					}, 12000);

				alwaysScrollTop();

	        },
			error: function (xhr, status, error) {
				if(xhr.status == 401){ doLogout();
					swal("No more logged in!", "Please login and try again", "error");
				}else if(xhr.status == 500){
					swal("Error on account reset!", "Please retry, if problem persists contact support team", "error");
				}
			},
			complete: function(data) {
				$.unblockUI();
			}
		})
	});
});

function getPWCUsers(pwcEmailId) {
	//alert("pwcEmailId : "+pwcEmailId);
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	var jsonData = {};
	jsonData.userEmail = pwcEmailId;
	var url = "/getPWCUsers";
	$.ajax({
		url: url,
		data: JSON.stringify(jsonData),
		type: "text",
		method: "POST",
		contentType: 'application/json',
		success: function (data) {
			$("#usersTable1 tbody").html("");
			//var htmlTmp = '<option value="">--Please Select--</option>';
			var userList = "";
			for (var i = 0; i < data.userListData.length; i++) {
				userList += '<tr name="userEntity' + data.userListData[i].id + '"><td><a style="cursor: pointer" onclick="pwcUserSignIn(\''+data.userListData[i].userEmail+'\');">' + data.userListData[i].userEmail + '</a></td></tr>';
				//userList += '<tr name="userEntity' + data.userListData[i].id + '"><td>' + data.userListData[i].usersId + '</td><td onclick="pwcUserSignIn();">' + data.userListData[i].userEmail + '</td><td>' + data.userListData[i].pwcEmail + '</td></tr>';
				//htmlTmp += '<option value="' + data.userListData[i].id + '">' + data.userListData[i].usersId + ' (' + data.userListData[i].userEmail + ')</option>';
			}
			$("#usersTable1").append(userList);
			//$('.userAvailable').append(htmlTmp); // append() should be replace with html(); this requires showUserEntityDetails() to be invoked after getAllUsers()
		},
		error: function (xhr, status, error) {
			if (xhr.status == 401) {
				doLogout();
			}
		},
		complete: function (data) {
			document.getElementById("loadingIdos").style.display = "none";
			$.unblockUI();
		}
	});
}

function pwcUserSignIn(tabulateEmailID){
	//alert("tabulateEmailID : "+tabulateEmailID);
	var username = tabulateEmailID;
	if(username==""){
		return false;
	} else {
		var publicKeyID = $("#publicKeyID").text();
		var encrypt = new JSEncrypt();
		encrypt.setPublicKey(publicKeyID);
		var jsonData = {};
		jsonData.userName = username;
		var url="/ssoLogin";

		$.ajax({
			url: url,
			data:JSON.stringify(jsonData),
			dataType: 'json',
			contentType: 'application/json',
			async: true,
			method:"POST",
			success: function (data) {
				if(data.wrongses == "false"){
					notifyLogin.show("Session expired.",true,true);
					doLogout();
					return false;
				}
				if(data.logincredentials[0].message=="Failure"){
					notifyLogin.show(data.logincredentials[0].failurereason,true,true);
					return false;
				}else{
					window.location.href=data.url+"?"+data.loggedin;
				}
				alwaysScrollTop();
			},
			error: function (xhr, status, error) {
				if (xhr.status == 401) {
					doLogout();
				}
			}
		});
	}
}