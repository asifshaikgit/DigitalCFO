$(document).ready(function () {
    $('#addExternalUserOTPButton').click(function () {
        $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        $('#addExternalUserDiv').show();
        $('#OTPCodeInput').val("");
        $("div[class='extUserNameDiv']").text("");
        $("div[class='extUserEmail']").text("");
        $("div[class='extUserPhNoDiv']").text("");
        $("div[class='extUserAddress']").text("");
        $("div[class='extUserStatusDiv1']").text("");

        $('#externalUserListDiv').hide();
        $.unblockUI();
    });

    $('#externalUsrListDivClose').click(function () {
        $('#externalUserListDiv').hide();
    });

    $('#addExternalUsrDivClose').click(function () {
        $('#addExternalUserDiv').hide();
    });

    $('#signUpExternalUser').click(function () {
        //function signUpExternalUser(){
        //("#signUpExternalUser").attr("disabled", "disabled");
        $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        var captchaLength = $("#recaptchaLength").val();
        var businessName = $("#businessname").val();
        var contactName = $("#contactName").val();
        var corEmailId = $("#corporateemail").val();
        var adminPass = $("#extusrpwd").val();
        var adminPassConfirm = $("#extusrpwd_confirm").val();
        var address = $("#address").val();
        var adminPhonenumber = $("#phonenumber").val();
        var adminWebsite = $("#website").val();
        var goodEmail = corEmailId.match(/\b(^(\S+@).+((\.com)|(\.net)|(\.edu)|(\.mil)|(\.gov)|(\.org)|(\.info)|(\.sex)|(\.biz)|(\.aero)|(\.coop)|(\.museum)|(\.name)|(\.pro)|(\.arpa)|(\.asia)|(\.cat)|(\.int)|(\.jobs)|(\.tel)|(\.travel)|(\.xxx)|(\..{2,2}))$)\b/gi);
        apos = corEmailId.indexOf("@");
        dotpos = corEmailId.lastIndexOf(".");
        lastpos = corEmailId.length - 1;
        var badEmail = (apos < 1 || dotpos - apos < 2 || lastpos - dotpos < 2);

        //var url_regexp = new RegExp("^http(s?)\:\/\/[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?$");
        var url_regexp = new RegExp("[0-9a-zA-Z]([-.\w]*[0-9a-zA-Z])*(:(0-9)*)*(\/?)([a-zA-Z0-9\-\.\?\,\'\/\\\+&amp;%\$#_]*)?$");
        if (businessName == "") {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            //notifyLogin.show('Provide company name.',true,true);
            $("#businessname").val("");
            $("#businessname").attr("placeholder", "Enter Business/Firm name").blur();
            $("#businessname").focus();
            return true;
        }

        if (address == "") {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            //notifyLogin.show('Provide company name.',true,true);
            $("#address").val("");
            $("#address").attr("placeholder", "Enter your address").blur();
            $("#address").focus();
            return true;
        }

        if (contactName == "") {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            //notifyLogin.show('Provide company name.',true,true);
            $("#contactName").val("");
            $("#contactName").attr("placeholder", "Enter your name").blur();
            $("#contactName").focus();
            return true;
        }

        if (corEmailId == "" || !goodEmail || badEmail) {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            //notifyLogin.show('Provide valid company/corporate Email Id.',true,true);
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            $("#corporateemail").val("");
            $("#corporateemail").attr("placeholder", "Enter company E-mail.").blur();
            $("#corporateemail").focus();
            return true;
        }
        if (adminPass == "") {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            //notifyLogin.show('Provide Password For the Account.',true,true);
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            $("#extusrpwd").attr("placeholder", "Enter password.").blur();
            $("#extusrpwd").focus();
            return true;
        }
        if (adminPassConfirm == "") {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            //notifyLogin.show('Provide Confirm Password For the Account.',true,true);
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            $("#extusrpwd_confirm").attr("placeholder", "Enter password again.").blur();
            $("#extusrpwd_confirm").focus();
            return true;
        }

        if (adminPass != adminPassConfirm) {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            //notifyLogin.show('Provide Password and Confirm Password same.',true,true);
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            $("#extusrpwd_confirm").val("");
            $("#extusrpwd_confirm").attr("placeholder", "Both passwords do not match.").blur();
            $("#extusrpwd_confirm").focus();
            return true;
        }

        var validationPassedNo = $(".validationPassedNo").val();
        if (validationPassedNo != "6") {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            notifyLogin.show('Provided Password does not match the rules.', true, true);
            $("#extusrpwd").focus();
            return true;
        }
        if (!verifyPassword(adminPass)) {
            $.unblockUI();
            return true;
        }
        if (isEmpty(adminPhonenumber) || adminPhonenumber.lenght < 6) {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            //notifyLogin.show('Provide company name.',true,true);
            $("#phonenumber").val("");
            $("#phonenumber").attr("placeholder", "Provide valid phone/mobile number.").blur();
            $("#phonenumber").focus();
            return true;
        }

        if ((adminWebsite != "") && (!url_regexp.test(adminWebsite))) {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            grecaptcha.reset(newUserWidgetId);
            $("#recaptchaLength").val(0);
            $("#website").val("");
            $("#website").attr("placeholder", "Enter valid website URL.").blur();
            //notifyLogin.show('Provide valid website URL.',true,true);
            $("#website").focus();
            return true;
        }

        if (validateCaptcha === true && (captchaLength == 0 || captchaLength == "")) {
            $.unblockUI();
            $('#signUpExternalUser').removeAttr("disabled");
            notifyLogin.show('Please select Captcha.', true, true);
            return false;
        }

        var jsonData = {};
        jsonData.companyName = businessName;
        jsonData.contactName = contactName;
        jsonData.companyEmailId = corEmailId;
        jsonData.adminPwd = adminPass;
        jsonData.adminPhonenumber = adminPhonenumber;
        jsonData.adminWebsite = adminWebsite;
        jsonData.address = address;
        var url = "/config/addExternalUser";
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
                $("#businessname").val("");
                $("#contactName").val("");
                $("#corporateemail").val("");
                $("#extusrpwd").val("");
                $("#extusrpwd_confirm").val("");
                $("#phonenumber").val("");
                $("#website").val("");
                $("#accountspanstat").html("Please log in to your account.");
                //$("#successregdiv").show();
                $("#successaccountmsg").html("");
                $("#successaccountmsg").append('Welcome!! You have been successfully registered. Please activate you account following activation link sent to your email.');
                //$("#forgotlogindiv").hide();
                notifyLogin.show('Welcome!! You have been successfully registered. Please activate you account following activation link sent to your email.', true, false);
                //$("#messageDisplay").html("");
                //$("#messageDisplay").append('Welcome to IDOS. Your organization has been successfully created. Please activate you account following activation link sent to your email.');
                alwaysScrollTop();
            },
            error: function (xhr, status, error) {
                $("#recaptchaLength").val(0);
                grecaptcha.reset(newUserWidgetId);
                notifyLogin.show(error, true, true);
            },
            complete: function (data) {
                $.unblockUI();
                setTimeout(function () {
                    $('#signUpExternalUser').removeAttr("disabled");
                }, 7000);
            }
        })
    });

    $('#externalUserAccessButton').click(function () {
        $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        $('#payrollsetup-container').hide();
        $('#addExternalUserDiv').hide();
        $('#externalUserListDiv').show();

        var extUserRow;
        var jsonData = {};
        jsonData.usermail = $("#hiddenuseremail").text();
        var url = "/externalUserList/allUsers"
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
                $("#externalUsersTable tbody").html("");
                var companyTemp = "";
                if (data.extUserListWithAccess.length == 0) {
                    swal("Warning","There are no registered external users","warning");
                    return false;
                }

                for (var i = 0; i < data.extUserListWithAccess.length; i++) {
                    if (data.extUserListWithAccess[i].status == "Deactivated" || data.extUserListWithAccess[i].status == "No Access") {
                        extUserRow += ('<tr id="' + data.extUserListWithAccess[i].extUserId + '"><td>' + data.extUserListWithAccess[i].extUserName + '</td><td>' + data.extUserListWithAccess[i].extUserEmail + '</td><td>' + data.extUserListWithAccess[i].extUserPhNo + '</td><td>' + data.extUserListWithAccess[i].extUserAddress + '</td><td class="activationStatus">' + data.extUserListWithAccess[i].status + '</td>');
                        extUserRow += ('<td><div class="search"><div class="provideAccessToExtUserBtn" style="display: block;"><a href="#" class="button small search-open btn-idos-flat-white fs-16" onClick="provideAccessToExtUser(this)" <i class="fa fa-tick pr-5"></i>Provide Access</a></div></div><div class="deactiveExtUserBtn" style="display: none;"><a href="#" class="button small search-open btn-idos-flat-white fs-16" onClick="deactivateExtUser(this)" <i class="fa fa-tick pr-5"></i>Deactivate User</a></div></div></td></tr>');
                    } else {
                        extUserRow += ('<tr id="' + data.extUserListWithAccess[i].extUserId + '"><td>' + data.extUserListWithAccess[i].extUserName + '</td><td>' + data.extUserListWithAccess[i].extUserEmail + '</td><td>' + data.extUserListWithAccess[i].extUserPhNo + '</td><td>' + data.extUserListWithAccess[i].extUserAddress + '</td><td class="activationStatus">' + data.extUserListWithAccess[i].status + '</td>');
                        extUserRow += ('<td><div class="search"><div class="provideAccessToExtUserBtn" style="display: none;"><a href="#" class="button small search-open btn-idos-flat-white fs-16" onClick="provideAccessToExtUser(this)" <i class="fa fa-tick pr-5"></i>Provide Access</a></div></div><div class="deactiveExtUserBtn" style="display: block;"><a href="#" class="button small search-open btn-idos-flat-white fs-16" onClick="deactivateExtUser(this)" <i class="fa fa-tick pr-5"></i>Deactivate User</a></div></div></td></tr>');

                    }


                }

                $("#externalUsersTable").append(extUserRow);
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on fetching External Users!", "Please retry, if problem persists contact IDOS support team", "error");
                }
            },
            complete: function (data) {
                $.unblockUI();
            }
        });
    });
});

function provideAccessToExtUser(elem) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    var trId = $(elem).closest('tr').attr('id');
    jsonData.usermail = $("#hiddenuseremail").text();
    var url = "/externalUser/provideAccess/" + trId;
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
            $("#companyTable tbody").html("");
            var companyTemp = "";
            if (data.provideAccessToExtUser[0].message == "Failure") {
                swal("Error!","Could not send the access code!!","error");
                return false;
            } else {
                swal("Error!","Sent the access code!!","error");
                $("#" + trId).find(".activationStatus").text("Access Code Sent");
                //$("#"+trId).find("td .activationStatus").text("Access Code Sent");
                $("#" + trId).find("td .deactiveExtUserBtn").show();
                $("#" + trId).find("td .provideAccessToExtUserBtn").hide();

            }
        }

        ,
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching vendors!", "Please retry, if problem persists contact IDOS support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function deactivateExtUser(elem) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    var trId = $(elem).closest('tr').attr('id');
    jsonData.usermail = $("#hiddenuseremail").text();
    var url = "/externalUser/deactivate/" + trId;
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
            $("#companyTable tbody").html("");
            var companyTemp = "";
            if (data.deactivateExtUser[0].message == "Failure") {
                swal("Error!","Error while deactivation!!","error");
                return false;
            } else {
                swal("Error!","Deactivated the user successfully","error");
                $("#" + trId).find(".activationStatus").text("Deactivated");
                $("#" + trId).find("td .deactiveExtUserBtn").hide();
                $("#" + trId).find("td .provideAccessToExtUserBtn").show();
            }
        }

        ,
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching vendors!", "Please retry, if problem persists contact IDOS support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

var openOrg = null;

function extUserLogInToOrg(username, pwd) {
    var jsonData = {};
    jsonData.userName = username;
    jsonData.loginpwd = pwd;
    var url = "/externalUser/loginToOrg";
    $.ajax({
        url: url,
        data: JSON.stringify(jsonData),
        dataType: 'json',
        contentType: 'application/json',
        async: true,
        method: "POST",
        success: function (data) {
            if (data.loginToOrgMessage[0].message == "failure") {
                swal("Error!","Failure during logging in...","error");
                return false;
            } else {
                if (openOrg == null) {
                    openOrg = window.open("/config?" + data.loggedin);
                    //window.location.href = "/config?" + data.loggedin;
                } else {
                    if (confirm('Do you want to logout from current organization?')) {
                        openOrg.close();
                        openOrg = null;
                        openOrg = window.open("/config");
                    }
                }

            }

            alwaysScrollTop();
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching External Users!", "Please retry, if problem persists contact IDOS support team", "error");
            }
        }
    });
}

function cancelAddExtUser() {
    //$('#orgDetailsRow').hide();
    $('#OTPCodeInput').val("");
    $("div[class='extUserNameDiv']").text("");
    $("div[class='extUserEmail']").text("");
    $("div[class='extUserPhNoDiv']").text("");
    $("div[class='extUserAddress']").text("");
    $("div[class='extUserStatusDiv1']").text("");
    $('#addExtUserBtn').hide();
    $('#addExtUserCancelBtn').hide();
    //$('#addExternalUserDiv').hide();
    //alwaysScrollTop();
}

function addExtUserToOrg(elem) {
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    jsonData.usermail = $("#hiddenuseremail").text();
    var OTPCode = $.trim($('#OTPCodeInput').val());
    jsonData.OTPCode = OTPCode;
    var url = "/externalUser/addExtUserToOrgViaOtp"

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
            if (data.extUserToBeAdded[0].message == "Failure") {

                swal("Error!","Error in adding external user","error");
                return false;
            } else {
                $("div[class='addExtUserDiv']").hide();
                $("div[class='extUserStatusDiv']").text(data.extUserToBeAdded[0].externalUserStatus);
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching vendors!", "Please retry, if problem persists contact IDOS support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function fetchExtUserDetails(elem) {
    var parentTable = $(elem).attr('id');
    var OTPCode = $.trim($(elem).val());
    if (OTPCode == "" || OTPCode.length != 6)
        return false;
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var jsonData = {};
    jsonData.usermail = $("#hiddenuseremail").text();
    jsonData.OTPCode = OTPCode;
    $("#externalUsersTable").show();
    var url = "/externalUser/showExtUserDetails"
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
            var extUsrTemp = "";
            $("#addExtUserBtn").hide();
            $("#addExtUserCancelBtn").hide();
            if (data.extUserToBeAdded[0].message == "Failure") {
                swal("Invalid data!","Please enter a valid OTP Code..!","error");
                $(elem).val("");
                $("div[class='extUserNameDiv']").text("");
                $("div[class='extUserEmail']").text("");
                $("div[class='extUserPhNoDiv']").text("");
                $("div[class='extUserAddress']").text("");
                $("div[class='extUserStatusDiv1']").text("");
                $("div[class='addExtUserDiv']").hide();
                $("#addExtUserBtn").hide();
                $("#addExtUserCancelBtn").hide();
                return false;
            } else {
                for (var i = 0; i < data.extUserToBeAdded.length; i++) {
                    $("div[class='extUserNameDiv']").text(data.extUserToBeAdded[0].name);
                    $("div[class='extUserEmail']").text(data.extUserToBeAdded[0].email);
                    $("div[class='extUserPhNoDiv']").text(data.extUserToBeAdded[0].registeredPhoneNumber);
                    $("div[class='extUserAddress']").text(data.extUserToBeAdded[0].registeredAddress);
                    $("div[class='extUserStatusDiv1']").text(data.extUserToBeAdded[0].externalUserStatus);
                    if (data.extUserToBeAdded[0].externalUserStatus == "No Access") {
                        $("div[class='addExtUserDiv']").show();
                        $("#addExtUserBtn").show();
                        $("#addExtUserCancelBtn").show();
                    }
                }
            }

            $(elem).append(extUsrTemp);
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching vendors!", "Please retry, if problem persists contact IDOS support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });

};

function doLogoutExtUser() {
    $.ajax({
        url: "/logoutExtUser",
        type: "post",
        dataType: 'json',
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        success: function (data) {
            window.location.href = "signout";

        },
        error: function (xhr, status, error) {
            window.location.href = "signout";
        },
        complete: function (data) {
            deleteAllCookies();
            isPlbsDataReloadNeeded = true;
        }

    });
}

/*Privacy setting for External User  */
function openPrivacySettingExtUser() {
    var jsonData = {};
    jsonData.email = $("#hiddenuseremail").text();
    jsonData.type = 'general';
    $('ul.nav').css('margin-bottom', '0');
    ajaxCall('/accountSetting/getUserDetails', jsonData, '', '', '', '', 'generalDetailsSuccess', '', true);
    var areaCodesLen = $('#settingPhoneCode').children().length;

    alwaysScrollTop();
    $('.tab-pane').fadeOut(600);
    $('#privaySettingDiv').fadeIn(600);
    $('.common-rightpanel').fadeOut();
    $('#generalSetting_content').fadeIn(1000);
    $('body').css('overflow', 'hidden');
    $('#systemconfigadminId1').find('.active').removeClass('active');
}
