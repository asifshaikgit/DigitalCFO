var CREATOR_CHANGE = false;
var APPROVER_CHANGE = false;
var AUDITOR_CHANGE = false;
var CREATOR_INCOME_CHANGE = false;
var CREATOR_EXPENSE_CHANGE = false;
var CREATOR_ASSETS_CHANGE = false;
var CREATOR_LIABILITIES_CHANGE = false;
var APPROVER_INCOME_CHANGE = false;
var APPROVER_EXPENSE_CHANGE = false;
var APPROVER_ASSETS_CHANGE = false;
var APPROVER_LIABILITIES_CHANGE = false;
var AUDITOR_INCOME_CHANGE = false;
var AUDITOR_EXPENSE_CHANGE = false;
var AUDITOR_ASSETS_CHANGE = false;
var AUDITOR_LIABILITIES_CHANGE = false;

//Used from config.scala

function getAllUsers() {
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var url = "/config/allUsers";
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
            $("#usersTable tbody").html("");
            var htmlTmp = '<option value="">--Please Select--</option>';
            var userList = "";
            for (var i = 0; i < data.userListData.length; i++) {
                userList += '<tr name="userEntity' + data.userListData[i].id + '"><td>' + data.userListData[i].fullName + '</td><td>' + data.userListData[i].userEmail + '</td><td>' + data.userListData[i].userBranch + '</td><td>' + data.userListData[i].userRole + '</td><td class="snglUsrDply"><button href="#usersSetup" class="btn btn-submit" onClick="showUserEntityDetails(this)" id="show-entity-details' + data.userListData[i].id + '"><i class="fa fa-edit fa-lg pr-3"></i>Edit</button></td><td class="snglUsrDply"><button href="#usersSetup" class="btn btn-submit" onClick="deactivateUserEntityDetails(this)" id="deactivate-entity-details' + data.userListData[i].id + '"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td></tr>';
                htmlTmp += '<option value="' + data.userListData[i].id + '">' + data.userListData[i].fullName + ' (' + data.userListData[i].userEmail + ')</option>';
            }
            if (data.extUserListData.length != 0) {
                for (var i = 0; i < data.extUserListData.length; i++) {
                    userList += '<tr name="userEntity' + data.extUserListData[i].id + '" style="color:red;"><td>' + data.extUserListData[i].fullName + '</td><td>' + data.extUserListData[i].userEmail + '</td><td>' + data.extUserListData[i].userBranch + '</td><td>' + data.extUserListData[i].userRole + '</td><td><div class="search"><div id="search-launch" style="display: block;"><a href="#usersSetup" class="btn btn-submit" onClick="showUserEntityDetails(this)" id="show-entity-details' + data.extUserListData[i].id + '"><i class="fa fa-edit fa-lg pr-3"></i>Edit</a></div></div></td><td><div class="search"><div id="search-launch" style="display: block;"><a href="#usersSetup" class="btn btn-submit" onClick="deactivateUserEntityDetails(this)" id="deactivate-entity-details' + data.extUserListData[i].id + '"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</a></div></div></td></tr>';
                    htmlTmp += '<option value="' + data.extUserListData[i].id + '">' + data.extUserListData[i].fullName + ' (' + data.extUserListData[i].userEmail + ')</option>';
                }
            }

            $("#usersTable").append(userList);
            $('.userAvailable').append(htmlTmp); // append() should be replace with html(); this requires showUserEntityDetails() to be invoked after getAllUsers()
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function searchUsers() {
    //alert("users/searchUsers");
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    var freeTextSearchUsers = $("#usersFreeTextSearch").val();
    var jsonData = {};
    jsonData.freeTextSearchUsersVal = freeTextSearchUsers;
    jsonData.usermail = $("#hiddenuseremail").text();
    var url = "/users/searchUsers";
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
            $("#usersTable tbody").html("");
            // for (var i = 0; i < data.userListData.length; i++) {
            //     $("#usersTable").append('<tr name="userEntity' + data.userListData[i].id + '"><td>' + data.userListData[i].fullName + '</td><td>' + data.userListData[i].userEmail + '</td><td>' + data.userListData[i].userBranch + '</td><td>' + data.userListData[i].userRole + '</td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"><a href="#usersSetup" class="button small search-open search-open btn-idos-flat-white fs-16" onClick="showUserEntityDetails(this)" id="show-entity-details' + data.userListData[i].id + '"><i class="fa fa-edit fa-lg pr-3"></i>Edit</div></div></td><td><div class="search btn-align-right edit-right"><div id="search-launch" style="display: block;"><a href="#usersSetup" class="button small search-open search-open btn-idos-flat-white fs-16" onClick="deactivateUserEntityDetails(this)" id="deactivate-entity-details' + data.userListData[i].id + '"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</a></div></div></td></tr>');
            // }
            var htmlTmp = '<option value="">--Please Select--</option>';
            var userList = "";
            for (var i = 0; i < data.userListData.length; i++) {
                userList += '<tr name="userEntity' + data.userListData[i].id + '"><td>' + data.userListData[i].fullName + '</td><td>' + data.userListData[i].userEmail + '</td><td>' + data.userListData[i].userBranch + '</td><td>' + data.userListData[i].userRole + '</td><td class="snglUsrDply"><button href="#usersSetup" class="btn btn-submit" onClick="showUserEntityDetails(this)" id="show-entity-details' + data.userListData[i].id + '"><i class="fa fa-edit fa-lg pr-3"></i>Edit</button></td><td class="snglUsrDply"><button href="#usersSetup" class="btn btn-submit" onClick="deactivateUserEntityDetails(this)" id="deactivate-entity-details' + data.userListData[i].id + '"><i class="far fa-trash-alt fa-lg pr-5"></i>Deactivate</button></td></tr>';
                htmlTmp += '<option value="' + data.userListData[i].id + '">' + data.userListData[i].fullName + ' (' + data.userListData[i].userEmail + ')</option>';
            }
            $("#usersTable").append(userList);
            $('.userAvailable').append(htmlTmp);
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching users!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function showUserEntityDetails(elem) {
    $("#txnIncoacreatordropdown").html("None Selected<b>&nbsp;&nbsp;&#8711;</b>");
    $("#txnIncoaapproverdropdown").html("None Selected<b>&nbsp;&nbsp;&#8711;</b>");

    $("#userRoleSpecs").hide();
    $(".duplabel").html("");
    enteredUserEmail = "";
    onFocusClickEmail = "";
    var entityId = $(elem).attr('id');
    var origEntityId = "";
    if (entityId.indexOf("show-entity-details") != -1) {
        origEntityId = entityId.substring(19, entityId.length);
    }
    if (entityId.indexOf("show-roletocoa-details") != -1) {
        origEntityId = entityId.substring(22, entityId.length);
    }
    $("#limitallfrom").each(function () {
        $(this).val("");
    });
    $("#limitallto").each(function () {
        $(this).val("");
    });
    var detailForm = "newUserform-container";
    $('.' + detailForm + ' input[type="hidden"]').val("");
    $('.' + detailForm + ' input[type="text"]').val("");
    $('.' + detailForm + ' textarea').val("");
    $('.' + detailForm + ' input[type="password"]').val("");
    $('.' + detailForm + ' select option:first').prop("selected", "selected");
    $('.' + detailForm + ' select[class="countryPhnCode"]').each(function () {
        $(this).find('option:first').prop("selected", "selected");
    });
    $('.' + detailForm + ' select[class="countryDropDown"]').each(function () {
        $(this).find('option:first').prop("selected", "selected");
    });
    $(".logo-upload-button").attr("href", location.hash);
    $("a[id*='form-container-close']").attr("href", location.hash);
    $("#idosModuleList option:selected").each(function () {
        $(this).removeAttr('selected');
    });
    var jsonData = {};
    jsonData.entityPrimaryId = origEntityId;
    var url = "/user/userDetails";
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
            $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
            logDebug("start showUserEntityDetails");
            $("#userRole option[value='1']").remove();
            $("#userRole option[value='2']").remove();
            $("#userRole option[value='8']").remove();
            $("#userRole option[value='9']").remove();
            $("#userRole option[value='12']").remove();
            $("#userRole").val(null).trigger('change');
            for (var i = 0; i < data.userdetailsData.length; i++) {
                $("#transactionCreationInBranch option:selected").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#transactionCreationInBranch').multiselect('rebuild');
                $("#transactionApprovalInBranch option:selected").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#transactionApprovalInBranch').multiselect('rebuild');
                $("#transactionAuditorInBranch option:selected").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#transactionAuditorInBranch').multiselect('rebuild');
                $("#transactionCreationForProject option:selected").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#transactionCreationForProject').multiselect('rebuild');
                $("#transactionApprovalForProject option:selected").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#transactionApprovalForProject').multiselect('rebuild');
                /*$('input[name="checkCOA"]:checkbox:checked').map(function () {
                      $('input[name="checkCOA"][value="'+this.value+'"]').prop('checked', false);
                }).get();
                $('input[name="coaAmountLimit"]').map(function () {
                    $(this).val("0.0");
                }).get();
                $('input[name="coaAmountLimitTo"]').map(function () {
                    $(this).val("0.0");
                }).get(); */
                $("#userRole option:selected").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#userRole').multiselect('rebuild');
                $('#userTxnQuestion').multiselect({
                    buttonWidth: '150px',
                    maxHeight: 150,
                    includeSelectAllOption: true,
                    enableFiltering: true,
                    onChange: function (element, checked) {
                    }
                });
                $("#userTxnQuestion option:selected").each(function () {
                    $(this).removeAttr('selected');
                });
                $('#userTxnQuestion').multiselect('rebuild');
                $("#userEntityHiddenId").val(data.userdetailsData[i].id);
                let userRolesIds = data.userdetailsData[i].userRoles;
                let orguserRolesIds = userRolesIds.substring(0, userRolesIds.length - 1).split(',');
                $("#userName").val(data.userdetailsData[i].fullName);
                $("#email").val(data.userdetailsData[i].emailId);
                $("#usraddress").val(data.userdetailsData[i].address);
                $("#usrdob").val(data.userdetailsData[i].dob);
                let phnCtryCode = data.userdetailsData[i].userPhnCtryCode;
                $("#user-form-container").find("select[name='userPhnNocountryCode'] option").filter(function () {
                    return $(this).html() == phnCtryCode;
                }).prop("selected", "selected");
                $("#usrmobile1").val(data.userdetailsData[i].mobile.substring(0, 3));
                $("#usrmobile2").val(data.userdetailsData[i].mobile.substring(3, 6));
                $("#usrmobile3").val(data.userdetailsData[i].mobile.substring(6, 10));
                $('input[name="usrphoto"]').val(data.userdetailsData[i].photograph);
                $('input[id="uploadIdProof"]').val(data.userdetailsData[i].idproof);
                $("#birthDate").val(data.userdetailsData[i].dob);
                /*$("select[name='proc']").children().remove();
                $("select[name='proc']").append('<option value="0">No</option><option value="1">Yes</option>');
                var allowedProcurementRequest=data.userdetailsData[i].procurementRequest;
                $("select[name='proc'] option").filter(function () {return $(this).val()==allowedProcurementRequest;}).prop("selected", "selected");*/
                $("#bloodgroup").val(data.userdetailsData[i].bloodGroup);
                $("#userBranch").find("option[value='" + data.userdetailsData[i].userBranch + "']").prop("selected", "selected");
                for (let j = 0; j < orguserRolesIds.length; j++) {
                    let userRoles = "";
                    if (orguserRolesIds[j] == 1) {
                        userRoles += ('<option value="' + orguserRolesIds[j] + '">MASTER ADMIN</option>');
                        $("#userBranch").find("option[value='" + data.userdetailsData[i].headQuarterBranch + "']").prop("selected", "selected");
                    } else if (orguserRolesIds[j] == 9) {
                        userRoles += ('<option value="' + orguserRolesIds[j] + '">BRANCH ADMIN</option>');
                        $("#userBranch").find("option[value='" + data.userdetailsData[i].branchAdministratorBranch + "']").prop("selected", "selected");
                    } else if (orguserRolesIds[j] == 12) {
                        userRoles += ('<option value="' + orguserRolesIds[j] + '">OFFICERS</option>');
                        $("#userBranch").find("option[value='" + data.userdetailsData[i].branchOfficersBranch + "']").prop("selected", "selected");
                    } else if (orguserRolesIds[j] == 8) {
                        let existingOption = $("#userRole option[value='" + orguserRolesIds[j] + "']").prop('value');
                        if (typeof (existingOption) == 'undefined') {
                            userRoles += ('<option value="' + orguserRolesIds[j] + '">CASHIER</option>');
                            $("#userBranch").find("option[value='" + data.userdetailsData[i].cashierBranch + "']").prop("selected", "selected");
                        }
                    }
                    $("#userRole").append(userRoles);
                    $("#userRole").find("option[value='" + orguserRolesIds[j] + "']").prop("selected", "selected");
                    $("#userRole").find("option[value='1']").attr('disabled', 'disabled').hide();
                }

                //$("#userRole").find("option[value='1']").style.display='none';
                let userTxnPurpIds = data.userdetailsData[i].userTxnQuestions;
                let orguserTxnQuestionsIds = userTxnPurpIds.substring(0, userTxnPurpIds.length - 1).split(',');
                for (let j = 0; j < orguserTxnQuestionsIds.length; j++) {
                    $("#userTxnQuestion").find("option[value='" + orguserTxnQuestionsIds[j] + "']").prop("selected", "selected");
                }
                var creationRigthInBranchId = data.userdetailsData[i].txnCreationInBranches.split(',');
                for (var j = 0; j < creationRigthInBranchId.length; j++) {
                    if (creationRigthInBranchId[j] != "") {
                        $('select[id="transactionCreationInBranch"]').find("option[value='" + creationRigthInBranchId[j] + "']").prop("selected", "selected");
                    }
                }
                var creationRigthInProjectId = data.userdetailsData[i].txnCreationForProjects.split(',');
                for (var j = 0; j < creationRigthInProjectId.length; j++) {
                    if (creationRigthInProjectId[j] != "") {
                        $('select[id="transactionCreationForProject"]').find("option[value='" + creationRigthInProjectId[j] + "']").prop("selected", "selected");
                    }
                }
                var approverRigthInBranchId = data.userdetailsData[i].txnApprovalInBranches.split(',');
                for (var j = 0; j < approverRigthInBranchId.length; j++) {
                    if (approverRigthInBranchId[j] != "") {
                        $('select[id="transactionApprovalInBranch"]').find("option[value='" + approverRigthInBranchId[j] + "']").prop("selected", "selected");
                    }
                }
                var approverRigthInProjectId = data.userdetailsData[i].txnApprovalInProjects.split(',');
                for (var j = 0; j < approverRigthInProjectId.length; j++) {
                    if (approverRigthInProjectId[j] != "") {
                        $('select[id="transactionApprovalForProject"]').find("option[value='" + approverRigthInProjectId[j] + "']").prop("selected", "selected");
                    }
                }
                logDebug("Start 2.0");


                logDebug("End  2");
                //auditor rights
                var auditorRigthInBranchId = data.userdetailsData[i].txnAuditorInBranches.split(',');
                for (var j = 0; j < auditorRigthInBranchId.length; j++) {
                    if (auditorRigthInBranchId[j] != "") {
                        $('select[id="transactionAuditorInBranch"]').find("option[value='" + auditorRigthInBranchId[j] + "']").prop("selected", "selected");
                    }
                }
                logDebug("End  2.1");
                getEarningsListForOrg();
                getDeductionsListForOrg();
                $('#user-form-container select[id="userTravelTransactionPurpose"] option').each(function () {
                    $(this).removeAttr('selected');
                });
                logDebug("End  2.3");
                $('#user-form-container select[id="userExpenseTransactionPurpose"] option').each(function () {
                    $(this).removeAttr('selected');
                });
                logDebug("Start 3");
                var userTravelEligibility = data.userdetailsData[i].userTravelEligibility;
                $("#user-form-container").find("select[name='userTravelEligibility'] option").filter(function () {
                    return $(this).val() == userTravelEligibility;
                }).prop("selected", "selected");
                var userTravelTransactionPurpose = data.userdetailsData[i].userTravelTransactionPurpose.split(',');
                for (var j = 0; j < userTravelTransactionPurpose.length; j++) {
                    if (userTravelTransactionPurpose[j] != "") {
                        $("#user-form-container").find("select[name='userTravelTransactionPurpose']").find("option[value='" + userTravelTransactionPurpose[j] + "']").prop("selected", "selected");
                    }
                }

                var userExpenseEligibility = data.userdetailsData[i].userExpenseEligibility;
                $("#user-form-container").find("select[name='userExpenseEligibility'] option").filter(function () {
                    return $(this).val() == userExpenseEligibility;
                }).prop("selected", "selected");
                var userExpenseTransactionPurpose = data.userdetailsData[i].userExpenseTransactionPurpose.split(',');
                for (var j = 0; j < userExpenseTransactionPurpose.length; j++) {
                    if (userExpenseTransactionPurpose[j] != "") {
                        $("#user-form-container").find("select[name='userExpenseTransactionPurpose']").find("option[value='" + userExpenseTransactionPurpose[j] + "']").prop("selected", "selected");
                    }
                }
                logDebug("End  3");

                $("#user-form-container").find("select[name='userTravelTransactionPurpose']").multiselect('rebuild');
                $("#user-form-container").find("select[name='userExpenseTransactionPurpose']").multiselect('rebuild');
                $('#transactionCreationInBranch').multiselect('rebuild');
                $('#transactionCreationForProject').multiselect('rebuild');
                $('#transactionApprovalInBranch').multiselect('rebuild');
                $('#transactionApprovalForProject').multiselect('rebuild');
                $('#transactionAuditorInBranch').multiselect('rebuild');
                $('.multipleDropdown').multiselect('rebuild');
                $('#userTxnQuestion').multiselect('rebuild');
                logDebug("End  4");
                //$('#userStatus').val(data.userdetailsData[i].status);
                //$('#userEthnicity').val(data.userdetailsData[i].ethnicity);
                $('#userHireDate').val(data.userdetailsData[i].hireDate);
                $('#userConfirmDate').val(data.userdetailsData[i].confirmDate);
                $('#userNoticeDate').val(data.userdetailsData[i].noticeDate);
                $('#userReleaseDate').val(data.userdetailsData[i].releaseDate);
                $('#userManager').val(data.userdetailsData[i].mgr);
                $('#userHRManager').val(data.userdetailsData[i].hrMgr);
                $('#userEmploymentType').val(data.userdetailsData[i].empType);
                $('#userSource').val(data.userdetailsData[i].source);
                $('#userPanNumber').val(data.userdetailsData[i].pan);
                $('#userPassportNumber').val(data.userdetailsData[i].passport);
                $('#userDesignation').val(data.userdetailsData[i].designation);
                $('#userDepartment').val(data.userdetailsData[i].department);
                $('#userEmergencyEmail').val(data.userdetailsData[i].userEmergencyEmail);
                $('#userEmergencyName').val(data.userdetailsData[i].userEmergencyName);
                var ePhone = data.userdetailsData[i].userEmergencyPhone;
                ePhone = ePhone.split('-');
                $('#userEmergencyPhnNocountryCode').val(ePhone[0]);
                if (!isEmpty(ePhone[1])) {
                    var res = ePhone[1].substring(0, 3);
                    $('#usrEmergencymobile1').val(res);
                    res = ePhone[1].substring(3, 6);
                    $('#usrEmergencymobile2').val(res);
                    res = ePhone[1].substring(6, 10);
                    $('#usrEmergencymobile3').val(res);
                }
                logDebug("End  5");
                $('#customerCreator').prop('checked', data.userdetailsData[i].customerCreator);
                $('#customerActivator').prop('checked', data.userdetailsData[i].customerActivator);
                $('#vendorCreator').prop('checked', data.userdetailsData[i].vendorCreator);
                $('#vendorActivator').prop('checked', data.userdetailsData[i].vendorActivator);
                var moduleRights = data.userdetailsData[i].moduleRights.split(',');
                for (var j = 0; j < 14; j++) {
                    if (moduleRights[j] != "" && moduleRights[j] == "1") {
                        var digitValue = parseInt(moduleRights[j]);
                        digitValue = (digitValue * (j + 1));
                        $('select[id="idosModuleList"]').find("option[value='" + digitValue + "']").prop("selected", "selected");
                    }
                }
                $('select[id="idosModuleList"]').multiselect('rebuild');
                $("." + detailForm + "").slideDown('slow');
                logDebug("End  6");
                ;
            }
            logDebug("end showUserEntityDetails");
            alwaysScrollTop();
            $.unblockUI();
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching user detail!", "Please retry, if problem persists contact support team", "error");
            }

        },
        complete: function (data) {
            CREATOR_CHANGE = false;
            APPROVER_CHANGE = false;
            AUDITOR_CHANGE = false;
            CREATOR_INCOME_CHANGE = false;
            CREATOR_EXPENSE_CHANGE = false;
            CREATOR_ASSETS_CHANGE = false;
            CREATOR_LIABILITIES_CHANGE = false;
            APPROVER_INCOME_CHANGE = false;
            APPROVER_EXPENSE_CHANGE = false;
            APPROVER_ASSETS_CHANGE = false;
            APPROVER_LIABILITIES_CHANGE = false;
            AUDITOR_INCOME_CHANGE = false;
            AUDITOR_EXPENSE_CHANGE = false;
            AUDITOR_ASSETS_CHANGE = false;
            AUDITOR_LIABILITIES_CHANGE = false;

            $.unblockUI();
        }
    });
}

$(document).ready(function () {
    $('#addUserBtn').click(function () {
        checkEditEmail(onFocusClickEmail);
        if (checkEditEmailResult || checkEditEmailResult == undefined) {
            onFocusClickEmail = "";
            checkEditEmailResult = "";
            if ($("#userName").val() == "") {
                swal("Invalid data!","Please enter username","error");
                return true;
            }
            if ($("#email").val() == "") {
                swal("Invalid data!","Please enter user emailId","error");
                return true;
            }
            if ($("#userBranch option:selected").val() == "") {
                swal("Invalid data!","Please choose a branch for the user you want to create.","error");
                return true;
            }
            var userInfo = {};
            userInfo.userHiddenPrimKey = $("#userEntityHiddenId").val();
            userInfo.userName = $("#userName").val();
            userInfo.userEmail = $("#email").val();
            userInfo.branch = $("#userBranch option:selected").val();
            logDebug("Start add");
            var userctryCodeVal = $("#userPhnNocountryCode option:selected").val();
            userInfo.userctryCodeText = $("#userPhnNocountryCode option:selected").text();
            userInfo.userNumber = userctryCodeVal + "-" + $("#usrmobile1").val() + $("#usrmobile2").val() + $("#usrmobile3").val();
            userInfo.userAddress = $("#usraddress").val();
            var roles = $('select[name="userRole"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get();
            if (roles.toString() == "") {
                swal("Invalid data!","Please choose one or many role for the user","error");
                return true;
            }
            $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
            var userTxnPurpose = $('select[name="userTxnQuestion"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get();
            logDebug("Start 2");
            userInfo.userRoles = roles.toString();
            userInfo.userTransactionQuestions = userTxnPurpose.toString();
            var creationrightInBnchs = $('select[name="transactionCreationInBranch"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get();
            userInfo.creationrightsInBranches = creationrightInBnchs.toString();
            logDebug("Start 3");
            var creationrightInPjcts = $('select[name="transactionCreationForProject"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get();
            userInfo.creationrightsInProjects = creationrightInPjcts.toString();

            var approvalrightInBnchs = $('select[name="transactionApprovalInBranch"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get();
            userInfo.approvalrightsInBranches = approvalrightInBnchs.toString();
            var approvalrightInPjcts = $('select[name="transactionApprovalForProject"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get();
            userInfo.approvalrightsInProjects = approvalrightInPjcts.toString();

            var auditorrightInBnchs = $('select[name="transactionAuditorInBranch"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get();
            userInfo.auditorrightsInBranches = auditorrightInBnchs.toString();

            userInfo.useremail = $("#hiddenuseremail").text();
            var userTravelTransactionPurpose = $('select[name="userTravelTransactionPurpose"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get().toString();
            userInfo.userTravelEligibility = $('select[name="userTravelEligibility"] option:selected').val();
            userInfo.userTravelTransPurpose = userTravelTransactionPurpose;
            var userExpenseTransactionPurpose = $('select[name="userExpenseTransactionPurpose"] option:selected').map(function () {
                if (this.value != "multiselect-all") {
                    return this.value;
                }
            }).get().toString();
            userInfo.userExpenseEligibility = $('select[name="userExpenseEligibility"] option:selected').val();
            userInfo.userExpenseTransPurpose = userExpenseTransactionPurpose;
            userInfo.hireDate = $('#userHireDate').val();
            userInfo.confirmDate = $('#userConfirmDate').val();
            userInfo.noticeDate = $('#userNoticeDate').val();
            userInfo.releaseDate = $('#userReleaseDate').val();
            var mgr = $('#userManager').val(), hrMgr = $('#userHRManager').val();
            userInfo.manager = mgr;
            userInfo.hrManager = hrMgr;
            userInfo.empType = $('#userEmploymentType').val();
            userInfo.source = $('#userSource').val();
            userInfo.pan = $('#userPanNumber').val();
            userInfo.passport = $('#userPassportNumber').val();
            userInfo.designation = $('#userDesignation').val();
            userInfo.department = $('#userDepartment').val();
            userInfo.userEmergencyName = $('#userEmergencyName').val();
            userInfo.userEmergencyEmail = $('#userEmergencyEmail').val();
            var userSetupIsSearchClicked = $("#userSetupIsSearchClicked").val();
            if (userSetupIsSearchClicked == "") {
                userSetupIsSearchClicked = 0;
            }
            userInfo.userSetupIsSearchClicked = userSetupIsSearchClicked;
            var ePhone = $('#userEmergencyPhnNocountryCode').val() + '-' + $('#usrEmergencymobile1').val() + $('#usrEmergencymobile2').val() + $('#usrEmergencymobile3').val();
            userInfo.userEmergencyPhone = ePhone;
            var customerCreator = 0;
            if ($("#customerCreator").is(':checked') == true) {
                customerCreator = 1;
            }
            var vednorCreator = 0;
            if ($("#vendorCreator").is(':checked') == true) {
                vednorCreator = 1;
            }
            var customerActivator = 0;
            if ($("#customerActivator").is(':checked') == true) {
                customerActivator = 1;
            }
            var vednorActivator = 0;
            if ($("#vendorActivator").is(':checked') == true) {
                vednorActivator = 1;
            }
            userInfo.customerCreator = customerCreator;
            userInfo.vednorCreator = vednorCreator;
            userInfo.customerActivator = customerActivator;
            userInfo.vednorActivator = vednorActivator;
            userInfo.CREATOR_CHANGE = CREATOR_CHANGE;
            userInfo.APPROVER_CHANGE = APPROVER_CHANGE;
            userInfo.AUDITOR_CHANGE = AUDITOR_CHANGE;

            userInfo.CREATOR_INCOME_CHANGE = CREATOR_INCOME_CHANGE;
            userInfo.CREATOR_EXPENSE_CHANGE = CREATOR_EXPENSE_CHANGE;
            userInfo.CREATOR_ASSETS_CHANGE = CREATOR_ASSETS_CHANGE;
            userInfo.CREATOR_LIABILITIES_CHANGE = CREATOR_LIABILITIES_CHANGE;

            userInfo.APPROVER_INCOME_CHANGE = APPROVER_INCOME_CHANGE;
            userInfo.APPROVER_EXPENSE_CHANGE = APPROVER_EXPENSE_CHANGE;
            userInfo.APPROVER_ASSETS_CHANGE = APPROVER_ASSETS_CHANGE;
            userInfo.APPROVER_LIABILITIES_CHANGE = APPROVER_LIABILITIES_CHANGE;

            userInfo.AUDITOR_INCOME_CHANGE = AUDITOR_INCOME_CHANGE;
            userInfo.AUDITOR_EXPENSE_CHANGE = AUDITOR_EXPENSE_CHANGE;
            userInfo.AUDITOR_ASSETS_CHANGE = AUDITOR_ASSETS_CHANGE;
            userInfo.AUDITOR_LIABILITIES_CHANGE = AUDITOR_LIABILITIES_CHANGE;
            var moduleAccess = "";
            $('select[id="idosModuleList"] option').each(function () {
                var selVale = $(this).is(':selected');
                if (selVale === true) {
                    moduleAccess += "1,";
                } else {
                    moduleAccess += "0,";
                }
            });
            userInfo.moduleAccess = moduleAccess.toString();
            var makeCall = false;
            if (!isEmpty(mgr) && !isEmpty(hrMgr)) {
                if (mgr != hrMgr) {
                    makeCall = true;
                }
            } else {
                makeCall = true;
            }
            if (makeCall) {
                var url = "/config/CreateUser";
                $.ajax({
                    url: url,
                    data: JSON.stringify(userInfo),
                    type: "text",
                    headers: {
                        "X-AUTH-TOKEN": window.authToken
                    },
                    method: "POST",
                    contentType: 'application/json',
                    success: function (data) {
                        CREATOR_CHANGE = false;
                        APPROVER_CHANGE = false;
                        AUDITOR_CHANGE = false;
                        INCOME_CHANGE = false;
                        EXPENSE_CHANGE = false;
                        ASSETS_CHANGE = false;
                        LIABILITIES_CHANGE = false;
                        var exisusertr = $("#usersTable tr[name='userEntity" + data.userListData[0].id + "']").attr('name');
                        if (typeof (exisusertr) != 'undefined') {
                            $("#usersTable tr[name='userEntity" + data.userListData[0].id + "']").html("");
                            $("#usersTable tr[name='userEntity" + data.userListData[0].id + "']").append('<td>' + data.userListData[0].fullName + '</td><td>' + data.userListData[0].userEmail + '</td><td>' + data.userListData[0].userBranch + '</td><td>' + data.userListData[0].userRole + '</td><td><div class="search"><div id="search-launch" style="display: block;"> <a href="#usersSetup" class="button small search-open" onClick="showUserEntityDetails(this)" id="show-entity-details' + data.userListData[0].id + '"><span class="statictext">Edit</span></a></div></div></td><td><div class="search"><div id="search-launch" style="display: block;"> <a href="#usersSetup" class="button small search-open" onClick="deactivateUserEntityDetails(this)" id="deactivate-entity-details' + data.userListData[0].id + '"><span class="statictext">Deactivate</span></a></div></div></td>');
                        } else {
                            $("#usersTable").append('<tr name="userEntity' + data.userListData[0].id + '"><td>' + data.userListData[0].fullName + '</td><td>' + data.userListData[0].userEmail + '</td><td>' + data.userListData[0].userBranch + '</td><td>' + data.userListData[0].userRole + '</td><td><div class="search"><div id="search-launch" style="display: block;"> <a href="#usersSetup" class="button small search-open" onClick="showUserEntityDetails(this)" id="show-entity-details' + data.userListData[0].id + '"><span class="statictext">Edit</span></a></div></div></td><td><div class="search"><div id="search-launch" style="display: block;"> <a href="#usersSetup" class="button small search-open" onClick="deactivateUserEntityDetails(this)" id="deactivate-entity-details' + data.userListData[0].id + '"><span class="statictext">Deactivate</span></a></div></div></td></tr>');
                        }
                        $("#notificationMessage").html("User has been added/Updated successfully.");
                        $("a[id*='form-container-close']").trigger('click');
                        $('.notify-success').show();
                        $("#usermoduleshidden").val(data.userListData[0].moduleAccess);
                        $("#user-form-container").hide();
                        showHideModuleTabs(data.userListData[0].moduleAccess);
                        alwaysScrollTop();
                        getAllUsers();
                        
                    },
                    error: function (xhr, status, error) {
                        if (xhr.status == 401) {
                            doLogout();
                        } else if (xhr.status == 500) {
                            swal("Error on uodate/save user!", "Please retry, if problem persists contact support team", "error");
                        }
                    },
                    complete: function (data) {
                        $("#userSetupIsSearchClicked").val(0);
                        CREATOR_CHANGE = false;
                        APPROVER_CHANGE = false;
                        AUDITOR_CHANGE = false;
                        INCOME_CHANGE = false;
                        EXPENSE_CHANGE = false;
                        ASSETS_CHANGE = false;
                        LIABILITIES_CHANGE = false;
                        $.unblockUI();
                    }
                });
            } else {
                CREATOR_CHANGE = false;
                APPROVER_CHANGE = false;
                AUDITOR_CHANGE = false;
                INCOME_CHANGE = false;
                EXPENSE_CHANGE = false;
                ASSETS_CHANGE = false;
                LIABILITIES_CHANGE = false;
                $("#userSetupIsSearchClicked").val(0);
                $.unblockUI();
                if (mgr == hrMgr) {
                    swal("Invalid data!","Supervisor and HR Manager cannot be the same person.","error");
                }
            }
            
        }
    });
});

var moduleTabs = ["cashBalanceAllBranches", "bankBalanceAllBranches", "accountsReceivablesAllBranches", "accountsPayablesAllBranches", "cashAndBankBooksAllBranches", "bankBooksAllBranches", "trialBalanceallBranches", "reportAllInventoryItems", "plbscoamapping", "reportProfitLoss", "downloadJson", "tdsReport", "popUPPS", "employeeClaimsButton"];
var showHideModuleTabs = function (rights) {
    for (var i = 0; i < 14; i++) {
        if (rights.charAt(i) === "0") {
            $('button[id*="' + moduleTabs[i] + '"]').hide();
            $('li[id*="' + moduleTabs[i] + '"]').hide();
        } else {
            $('button[id*="' + moduleTabs[i] + '"]').show();
            $('li[id*="' + moduleTabs[i] + '"]').show();
        }
    }
    $('button[id="cashBalanceAllBranches"]').hide();
    $('button[id="bankBalanceAllBranches"]').hide();
    $('button[id="accountsReceivablesAllBranches"]').hide();
    $('button[id="accountsPayablesAllBranches"]').hide();
}

/*
var saveUserTxnCoaRulesOld = function(headType, ulid, ruleType){
	$.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
	if(ruleType == 1){
		saveUserTxnCoaRules2(1, 'txnInCoaCreatorList', ruleType);
		saveUserTxnCoaRules2(2, 'txnExCoaCreatorList', ruleType);
		saveUserTxnCoaRules2(3, 'txnAsCoaCreatorList', ruleType);
		saveUserTxnCoaRules2(4, 'txnLiCoaCreatorList', ruleType);
	}else if(ruleType == 2){
		saveUserTxnCoaRules2(1, 'txnInCoaApproverList', ruleType);
		saveUserTxnCoaRules2(2, 'txnExCoaApproverList', ruleType);
		saveUserTxnCoaRules2(3, 'txnAsCoaApproverList', ruleType);
		saveUserTxnCoaRules2(4, 'txnLiCoaApproverList', ruleType);
	}else if(ruleType == 3){
		saveUserTxnCoaRules2(1, 'transactionInCoaAuditorList', ruleType);
		saveUserTxnCoaRules2(2, 'transactionExCoaAuditorList', ruleType);
		saveUserTxnCoaRules2(3, 'transactionAsCoaAuditorList', ruleType);
		saveUserTxnCoaRules2(4, 'transactionLiCoaAuditorList', ruleType);
	}
	$.unblockUI();
}

var saveUserTxnCoaRules = function(headType, ulid, ruleType){
	if($("#userName").val()==""){
		alert("Please enter username");
		return true;
	}
	if($("#email").val()==""){
		alert("Please enter user emailId");
		return true;
	}
	var component = $('.txnUserCoaRulesButton');
	$('.txnUserCoaRulesButton').attr('disabled', 'disabled');
	var parentTable = $("#"+ulid).closest('table').attr('id');
	var coaAmountLimit="";
	var coaAmountCriteria="";
	logDebug("Start create");
	var rightForCoa = "";
	if(ruleType == 3){
		rightForCoa = $("#" + parentTable+ " #" + ulid).val();
		if(rightForCoa !== null){
			rightForCoa = rightForCoa.toString();
		}
		AUDITOR_CHANGE = true;
		if(headType == 1){
			AUDITOR_INCOME_CHANGE = true;
		}else if(headType == 2){
			AUDITOR_EXPENSE_CHANGE = true;
		}else if(headType == 3){
			AUDITOR_ASSETS_CHANGE = true;
		}else if(headType == 4){
			AUDITOR_LIABILITIES_CHANGE = true;
		}
	}else{
		rightForCoa = $('#'+parentTable+' tr[id="userTransactionRow"] ul[id='+ulid+'] input[name="checkCOA"]:checkbox:checked').map(function(){
			var value = this.value;
			if(value!=""){
				coaAmountCriteria+=$("#"+parentTable+" tr[id='userTransactionRow'] ul[id='"+ulid+"'] input[id='coaAmountLimitTo"+value+"']").val()+",";
				coaAmountLimit+=$("#"+parentTable+" tr[id='userTransactionRow'] ul[id='"+ulid+"'] input[id='coaAmountLimit"+value+"']").val()+",";
				return value;
			}
		}).get();
		rightForCoa = rightForCoa.toString();
		if(ruleType == 1){
			CREATOR_CHANGE = true;
			if(headType == 1){
				CREATOR_INCOME_CHANGE = true;
			}else if(headType == 2){
				CREATOR_EXPENSE_CHANGE = true;
			}else if(headType == 3){
				CREATOR_ASSETS_CHANGE = true;
			}else if(headType == 4){
				CREATOR_LIABILITIES_CHANGE = true;
			}
		}else if(ruleType == 2){
			APPROVER_CHANGE = true;
			if(headType == 1){
				APPROVER_INCOME_CHANGE = true;
			}else if(headType == 2){
				APPROVER_EXPENSE_CHANGE = true;
			}else if(headType == 3){
				APPROVER_ASSETS_CHANGE = true;
			}else if(headType == 4){
				APPROVER_LIABILITIES_CHANGE = true;
			}
		}
	}
	logDebug("End create");
	if(rightForCoa !== "" && rightForCoa !== null){
        $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
		var userInfo={};
		userInfo.userEmail=$("#email").val();
		userInfo.rightForCOA=rightForCoa;
		userInfo.coaAmountLimit = coaAmountLimit.substring(0, coaAmountLimit.length-1);
		userInfo.coaAmountCriteria = coaAmountCriteria.substring(0, coaAmountCriteria.length-1);
		userInfo.headType = headType;
		userInfo.ruleType = ruleType;
		var url="/user/txnrule";
		$.ajax({
			url         : url,
			data        : JSON.stringify(userInfo),
			type        : "text",
			headers:{
				"X-AUTH-TOKEN": window.authToken
			},
			method      : "POST",
			contentType : 'application/json',
			success     : function (data) {
				$('.txnUserCoaRulesButton').removeAttr("disabled");
			},
			error: function (xhr, status, error){
				if(xhr.status == 401){
					doLogout();
				}else if(xhr.status == 500){
					swal("Error on updating transaction rule!", "Please retry, if problem persists contact support team", "error");
	    		}
	    		$('.txnUserCoaRulesButton').removeAttr("disabled");
			},
			complete: function(data) {
				$('.txnUserCoaRulesButton').removeAttr("disabled");
				$.unblockUI();

			}
		});
	}
}
*/

function userSetupCOAItemSearch(comp) {
    var filter;
    filter = $(comp).val().toUpperCase();
    $(comp).parents('.btn-group').find('li').each(function () {
        if ($(this).find('span').length) {
            var spanText = $(this).find('span').html();
            if (spanText.toUpperCase().indexOf(filter) > -1) {
                $(this).removeClass('hidden');
            } else {
                $(this).addClass('hidden');
            }
        }
    });
}

var getTxnCoaItems = function (particular, ruleType) {
    var selectedEmail = $("#email").val();
    if (selectedEmail == "") {
        swal("error!", "Please provide valid user's emailId", "error");
        return false;
    }
    var branchElement = "";
    var nameRule = "";
    if (ruleType == "1") {
        $("#transactionRuleDiv div[class='panel-title']").text("Transaction Creation Items");
        branchElement = "transactionCreationInBranch";
        nameRule = "Creator";
    } else if (ruleType == "2") {
        $("#transactionRuleDiv div[class='panel-title']").text("Transaction Approver Items");
        branchElement = 'transactionApprovalInBranch';
        nameRule = "Approver";
    } else if (ruleType == "3") {
        $("#transactionRuleDiv div[class='panel-title']").text("Transaction Auditor Items");
        branchElement = "transactionAuditorInBranch";
        nameRule = "Auditor";
    }
    var selectedBranch = $('select[id=' + branchElement + '] option:selected').map(function () {
        if (this.value != "multiselect-all") {
            return this.value;
        }
    }).get();
    var branchLen = Object.keys(selectedBranch).length;
    if (branchLen < 1) {
        swal("Error!", "Please select at least a " + nameRule + " branch to proceed.", "error");
        return false;
    } else {
        $("#transactionRuleDiv").attr('data-toggle', 'modal');
        $("#transactionRuleDiv").modal('show');
    }

    USERST_SELECTED_ITEMS_MAP = {};
    $("#userParticularType").val(particular);
    $("#userTxnRuleType").val(ruleType);
    var jsonData = {};
    jsonData.useremail = $("#email").val();
    var url = "";
    $("#transactionRuleDiv #txnInCoaCreatorList #checkCOA").attr('checked', false);
    $("#transactionRuleDiv #txnInCoaCreatorList").find("input").removeAttr("disabled");
    if (particular === 1) {
        $("#transactionRuleDiv #displayRecordLimit").val('10');
        url = "/get/userincomes/" + ruleType + "/" + selectedEmail;
        $("#expenseTab").removeAttr("class", "active");
        $("#assetTab").removeAttr("class", "active");
        $("#liabilitiesTab").removeAttr("class", "active");
        $("#incomeTab").attr("class", "active");
    } else if (particular === 2) {
        url = "/get/userexpenses/" + ruleType + "/" + selectedEmail;
    } else if (particular === 3) {
        url = "/get/userassets/" + ruleType + "/" + selectedEmail;
    } else if (particular === 4) {
        url = "/get/userliabilities/" + ruleType + "/" + selectedEmail;
    }
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    $.ajax({
        url: url,
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        type: "text",
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            if (typeof data.error != 'undefined' && data.error != "") {
                swal("Error!", data.err, "error");
            } else {
                fillUserTxnItemsRecords(data);
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on fetching COA items for User setup!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

var COA_USER_SETUP_ITEMS_CACHE = [];
//var COA_USER_SETUP_ITEMS_MAP = {};
//var COA_USER_SETUP_ITEMS_NAME_MAP = {};
var fillUserTxnItemsRecords = function (data) {
    if (typeof data == 'undefined' || typeof data.coaItemData == 'undefined') {
        swal("not found!", "Chart of account items not found for this particular.", "error");
        return false;
    }
    USERST_SAVED_ITEMS_MAP = {};
    var ruleType = $("#userTxnRuleType").val();
    var coaItemsList = [];
    var itr = 0;
    $("#transactionRuleDiv #pagingTxnItemsNavPosition").html('');
    $("#transactionRuleDiv table[id='txnInCoaCreatorList'] tbody").html("");
    var coaItemRow = "";
    for (var i = 0; i < data.coaItemData.length; i++) {
        coaItemRow += '<tr class="userTxnCoalist" id="';
        coaItemRow += data.coaItemData[i].id;
        coaItemRow += '" headType="';
        coaItemRow += data.coaItemData[i].headType;
        coaItemRow += '"><td><input type="checkbox" name="checkCOA" id="checkCOA" value=';
        coaItemRow += data.coaItemData[i].id;
        coaItemRow += ' onClick="onCoaItemUserSetupClick(this);" ';
        coaItemRow += data.coaItemData[i].isChecked;
        coaItemRow += '/></td>';
        coaItemRow += '<td><input type="text" class="input-small" name="coaAmountLimit" id="coaAmountLimit';
        coaItemRow += data.coaItemData[i].id;
        coaItemRow += '" value="';
        coaItemRow += data.coaItemData[i].fromAmount;
        coaItemRow += '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event); onCoaItemUserSetupClick(this);" /></td><td><input type="text" class="input-small" name="coaAmountLimitTo" id="coaAmountLimitTo';
        coaItemRow += data.coaItemData[i].id;
        coaItemRow += '" value="';
        coaItemRow += data.coaItemData[i].toAmount;
        coaItemRow += '" onkeypress="return onlyDotsAndNumbers(event)" onblur="onlyDotsAndNumbers(event); onCoaItemUserSetupClick(this);" /></td>';
        coaItemRow += '<td><span class="userCoaNameDisplay">';
        coaItemRow += data.coaItemData[i].name;
        coaItemRow += '</span></td>/tr>';
        coaItemsList[itr++] = coaItemRow;
        if (data.coaItemData[i].isChecked == "checked") {
            USERST_SAVED_ITEMS_MAP[data.coaItemData[i].id] = data.coaItemData[i].fromAmount + "-" + data.coaItemData[i].toAmount;
        }
        //COA_USER_SETUP_ITEMS_MAP[data.coaItemData[i].id] = coaItemRow;
        //COA_USER_SETUP_ITEMS_NAME_MAP[data.coaItemData[i].name] = coaItemRow;
        coaItemRow = "";
    }
    COA_USER_SETUP_ITEMS_CACHE = coaItemsList;
    $("#transactionRuleDiv table[id='txnInCoaCreatorList'] tbody").append(coaItemsList.join(''));
    setPagingDetail('txnInCoaCreatorList', 10, 'pagingTxnItemsNavPosition');

    if (ruleType == "3") {
        $("#transactionRuleDiv table[id='userSetupTxnCoaTbl'] tr  th:nth-child(2)").hide();
        $("#transactionRuleDiv table[id='txnInCoaCreatorList'] tr  td:nth-child(2)").hide();
        $("#transactionRuleDiv table[id='userSetupTxnCoaTbl'] tr  th:nth-child(3)").hide();
        $("#transactionRuleDiv table[id='txnInCoaCreatorList'] tr  td:nth-child(3)").hide();
    }
}

var changeItemsDisplayLimit = function (elem) {
    var limit = $(elem).find('option:selected').val();
    limit = parseInt(limit);
    setPagingDetail('txnInCoaCreatorList', limit, 'pagingTxnItemsNavPosition');
}

var onSelectDeselectAllCoaItems = function (elem, targetTbl) {
    //var checked=$(elem).is(':checked');
    var parentTr = $(elem).closest("tr").attr('id');
    var checked = $("#" + parentTr).find("input[type='checkbox']").is(':checked');
    if (checked == true) {
        var checkvalue = $("#" + parentTr).find("input[type='checkbox']").val();
        if (checkvalue == "") {
            $("#" + targetTbl + " tbody").find("input").attr("disabled", "disabled");
            $("#userSelectedAllItems").val('1');
        }
    } else {
        $("#" + targetTbl).find("input").removeAttr("disabled");
    }
    var itemid = parentTr;
    USERST_SELECTED_ITEMS_MAP = {};
    if (checked == true) {
        var coaFromLimit = $('#' + parentTr + ' input[name="coaAmountLimit"]').val();
        var coaFromLimitTo = $('#' + parentTr + ' input[name="coaAmountLimitTo"]').val();
        USERST_SELECTED_ITEMS_MAP[itemid] = coaFromLimit + "-" + coaFromLimitTo;
        //USERST_SELECTED_ITEMS_MAP[itemid] = coaFromLimit +"-"+ coaFromLimitTo ;
        $("#userSelectedAllItems").val('1');
    } else if (checked == false) {
        $("#userSelectedAllItems").val('0');
    }
}

var changeUserTheme = function (elem) {
    var value = $(elem).attr('value');
    var userInfo = {};
    userInfo.userEmail = $("#email").val();
    userInfo.theme = value;
    var url = "/user/savetheme";
    $.ajax({
        url: url,
        data: JSON.stringify(userInfo),
        type: "text",
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        method: "POST",
        contentType: 'application/json',
        success: function (data) {
            $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
            if (value == 1) {
                selectTheme('green');
            } else if (value == 2) {
                selectTheme('magenta');
            } else if (value == 3) {
                selectTheme('silver');
            } else if (value == 4) {
                selectTheme('blue');
            } else if (value == 5) {
                selectTheme('darkBlue');
            } else if (value == 6) {
                selectTheme('red');
            } else if (value == 7) {
                selectTheme('silverblue');
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on changing theme!", "Please retry, if problem persists contact support team", "error");
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });

}

var saveUserTxnRules = function () {
    var headType = $("#userParticularType").val();
    var ruleType = $("#userTxnRuleType").val();
    if ($("#userName").val() == "") {
        swal("Error!", "invalid username", "error");
        return true;
    }
    if ($("#email").val() == "") {
        swal("Error!", "invalid email address", "error");
        return true;
    }

    var coaAmountCriteria = "";
    if (ruleType == 1) {
        CREATOR_CHANGE = true;
        if (headType == 1) {
            CREATOR_INCOME_CHANGE = true;
        } else if (headType == 2) {
            CREATOR_EXPENSE_CHANGE = true;
        } else if (headType == 3) {
            CREATOR_ASSETS_CHANGE = true;
        } else if (headType == 4) {
            CREATOR_LIABILITIES_CHANGE = true;
        }
    } else if (ruleType == 2) {
        APPROVER_CHANGE = true;
        if (headType == 1) {
            APPROVER_INCOME_CHANGE = true;
        } else if (headType == 2) {
            APPROVER_EXPENSE_CHANGE = true;
        } else if (headType == 3) {
            APPROVER_ASSETS_CHANGE = true;
        } else if (headType == 4) {
            APPROVER_LIABILITIES_CHANGE = true;
        }
    } else if (ruleType == 3) {
        AUDITOR_CHANGE = true;
        if (headType == 1) {
            AUDITOR_INCOME_CHANGE = true;
        } else if (headType == 2) {
            AUDITOR_EXPENSE_CHANGE = true;
        } else if (headType == 3) {
            AUDITOR_ASSETS_CHANGE = true;
        } else if (headType == 4) {
            AUDITOR_LIABILITIES_CHANGE = true;
        }
    }

    var rightForCOAList = "";
    var coaAmountLimitList = "";
    var selectedLen = Object.keys(USERST_SELECTED_ITEMS_MAP).length;
    if (selectedLen > 0) {
        var rightForCoa = Object.keys(USERST_SELECTED_ITEMS_MAP);
        var coaAmountLimit = Object.values(USERST_SELECTED_ITEMS_MAP);
        rightForCOAList = rightForCoa.toString();
        coaAmountLimitList = coaAmountLimit.toString();
    }
    if (rightForCOAList !== "allcoaitems0") {
        var savedLen = Object.keys(USERST_SAVED_ITEMS_MAP).length;
        if (savedLen > 0) {
            var savedCoaAmountLimit = Object.values(USERST_SAVED_ITEMS_MAP);
            var savedRightForCoa = Object.keys(USERST_SAVED_ITEMS_MAP);
            if (selectedLen > 0) {
                rightForCOAList += "," + savedRightForCoa.toString();
                coaAmountLimitList += "," + savedCoaAmountLimit.toString();
            } else {
                rightForCOAList = savedRightForCoa.toString();
                coaAmountLimitList = savedCoaAmountLimit.toString();
            }
        }
    }

    if (rightForCOAList !== "" && rightForCOAList !== null) {
        $('#txnUserCoaRulesBtn').attr('disabled', 'disabled');
        $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
        var userInfo = {};
        userInfo.userEmail = $("#email").val();
        userInfo.rightForCOA = rightForCOAList;
        userInfo.coaAmountLimit = coaAmountLimitList;
        userInfo.coaAmountCriteria = coaAmountCriteria;
        userInfo.headType = headType;
        userInfo.ruleType = ruleType;
        var url = "/user/txnrule";
        $.ajax({
            url: url,
            data: JSON.stringify(userInfo),
            type: "text",
            headers: {
                "X-AUTH-TOKEN": window.authToken
            },
            method: "POST",
            contentType: 'application/json',
            success: function (data) {
                USERST_SELECTED_ITEMS_MAP = {};
                swal("Save/Update!", "Rules are successfully saved/updated.", "success");
                //$("#txnUserCoaRulesCloseBtn").trigger('click');
            },
            error: function (xhr, status, error) {
                if (xhr.status == 401) {
                    doLogout();
                } else if (xhr.status == 500) {
                    swal("Error on updating transaction rule!", "Please retry, if problem persists contact support team", "error");
                }
            },
            complete: function (data) {
                $('#txnUserCoaRulesBtn').removeAttr("disabled");
                $.unblockUI();
            }
        });
    }
}

$(document).ready(function () {
    $('body').on('click', '.transactionRule', function () {
        var tabClass = $(this).parent().attr("class");
        if (tabClass == "active") {
            return false;
        }
        var selectedItemsLen = Object.keys(USERST_SELECTED_ITEMS_MAP).length;
        if (parseInt(selectedItemsLen) > 0) {
            var answer = confirm("Some items changed and selected, if you proceed change will be lost! Want to proceed?");
            if (answer == false) {
                return false;
            } else {
                $("#userSelectedAllItems").val('0');
                USERST_SELECTED_ITEMS_MAP = {};
            }
        }
        var ruleType = $("#userTxnRuleType").val();
        var tab = $(this).parent().attr("id");
        $("#transactionRuleDiv #txnInCoaCreatorList #checkCOA").attr('checked', false);
        $("#transactionRuleDiv #txnInCoaCreatorList").find("input").removeAttr("disabled");
        $("#transactionRuleDiv #displayRecordLimit").val('10');
        $("#userSetupIsSearchClicked").val(0);
        if ('incomeTab' == tab) {
            $("#expenseTab").removeAttr("class", "active");
            $("#assetTab").removeAttr("class", "active");
            $("#liabilitiesTab").removeAttr("class", "active");
            $("#incomeTab").attr("class", "active");

            $('#incomeTabDiv').fadeIn('normal', function () {
            });
            getTxnCoaItems(1, ruleType);
        } else if ('expenseTab' == tab) {
            $("#incomeTab").removeAttr("class", "active");
            $("#assetTab").removeAttr("class", "active");
            $("#liabilitiesTab").removeAttr("class", "active");
            $("#expenseTab").attr("class", "active");
            $('#expenseTab').fadeIn('normal', function () {
            });
            getTxnCoaItems(2, ruleType);
        } else if ('assetTab' == tab) {
            $("#incomeTab").removeAttr("class", "active");
            $("#expenseTab").removeAttr("class", "active");
            $("#liabilitiesTab").removeAttr("class", "active");
            $("#assetTab").attr("class", "active");
            $('#assetTab').fadeIn('normal', function () {
            });
            getTxnCoaItems(3, ruleType);
        } else if ('liabilitiesTab' === tab) {
            alwaysScrollTop();
            $("#incomeTab").removeAttr("class", "active");
            $("#expenseTab").removeAttr("class", "active");
            $("#assetTab").removeAttr("class", "active");
            $("#liabilitiesTab").attr("class", "active");
            $('#liabilitiesTab').fadeIn('normal', function () {
            });
            getTxnCoaItems(4, ruleType);
        }
    });

    $('body').on('click', '#resetAllItems', function () {
        $("#userSetupIsSearchClicked").val(0);
        $("#transactionRuleDiv table[id='txnInCoaCreatorList'] tbody").html("");
        $("#transactionRuleDiv table[id='txnInCoaCreatorList'] tbody").append(COA_USER_SETUP_ITEMS_CACHE.join(''));
        setPagingDetail('txnInCoaCreatorList', 10, 'pagingTxnItemsNavPosition');
    });

});

var onCoaItemUserSetupClick = function (elem) {
    var parentTr = $(elem).closest("tr").attr('id');
    var itemid = parentTr;
    var coaFromLimit = $('#' + parentTr + ' input[name="coaAmountLimit"]').val();
    var coaFromLimitTo = $('#' + parentTr + ' input[name="coaAmountLimitTo"]').val();
    if (coaFromLimit == "") {
        $('#' + parentTr + ' input[name="coaAmountLimit"]').val(0.0);
        coaFromLimit = 0;
    }
    if (coaFromLimitTo != "" && parseFloat(coaFromLimit) > parseFloat(coaFromLimitTo)) {
        swal("error!", "To Amount must be greater than or equal to From Amount", "error");
        return false;
    }
    var checked = $("#" + parentTr).find("input[type='checkbox']").is(':checked');
    if (checked == true) {
        USERST_SELECTED_ITEMS_MAP[itemid] = coaFromLimit + "-" + coaFromLimitTo;
    } else if (checked == false) {
        if (itemid in USERST_SELECTED_ITEMS_MAP) {
            delete USERST_SELECTED_ITEMS_MAP[itemid];
            $('#' + parentTr + ' input[name="coaAmountLimit"]').val(0.0);
            $('#' + parentTr + ' input[name="coaAmountLimitTo"]').val(0.0);
        }
        if (itemid in USERST_SAVED_ITEMS_MAP) {
            delete USERST_SAVED_ITEMS_MAP[itemid];
            $('#' + parentTr + ' input[name="coaAmountLimit"]').val(0.0);
            $('#' + parentTr + ' input[name="coaAmountLimitTo"]').val(0.0);
        }
    }
}

var userSetupCoaSearch = function (elemid) {
    var ruleType = $("#userTxnRuleType").val();
    var content = $("#" + elemid).val();
    if (content == null || content.length < 3) {
        return false;
    }
    var particular = $("#transactionRuleDiv #userParticularType").val();
    var url = "/search/useritems/" + particular + "/" + ruleType + "/" + content + "/" + $("#email").val();
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    $.ajax({
        url: url,
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        type: "text",
        method: "GET",
        contentType: 'application/json',
        success: function (data) {
            $("#userSetupIsSearchClicked").val(1);
            fillUserTxnItemsRecords(data);
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            } else if (xhr.status == 500) {
                swal("Error on searching COA items for user setup!", "Please retry, if problem persists contact support team", "error");
            }
            $("#userSetupIsSearchClicked").val(0);
        },
        complete: function (data) {
            $.unblockUI();
        }
    });

}

function getEarningsListForOrg() {
    var jsonData = {};
    jsonData.userEmail = $("#hiddenuseremail").text();
    jsonData.userHiddenPrimKey = $("#userEntityHiddenId").val();
    jsonData.userName = $("#userName").val();
    var userPk = $("#userEntityHiddenId").val();
    if (userPk == "" || typeof userPk == 'undefined' || userPk == null) {
        swal("Error!","You can configure payroll for existing USER only!","error");
        return false;
    }
    var url = "/payroll/getUserEarningsData";
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
            $("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='earningULList']").html("");
            $("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='earningULList']").append('<li id="usrEarningslist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="earningItemName" id="earningItemName" value=""  onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="earningAnnual" id="earningAnnual" value="0.0" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="earningMonthly" id="earningMonthly" value="0.0" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="transactiontoggleCheck(this)"/>Select All</li>');
            var userList = "";
            for (var i = 0; i < data.allUserEarningsItemsData.length; i++) {
                userList += '<li id="usrEarningslist">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA"' + i + ' value="' + data.allUserEarningsItemsData[i].id + '" checked onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="earningItemName" id="earningItemName' + data.allUserEarningsItemsData[i].id + '" value="' + data.allUserEarningsItemsData[i].name + '"  onkeyup="transactiontoggleCheck(this)" readonly="readonly"/>&nbsp;&nbsp;<input type="text" class="input-small" name="earningAnnual" id="earningAnnual' + data.allUserEarningsItemsData[i].id + '" value=' + data.allUserEarningsItemsData[i].annualInc + ' onkeypress="return onlyDotsAndNumbers(event)" onkeyup="transactiontoggleCheck(this);calculateMonthlyEarningIncome(this);"/>&nbsp;&nbsp;<input type="text" class="input-small" name="earningMonthly" id="earningMonthly' + data.allUserEarningsItemsData[i].id + '" value="' + data.allUserEarningsItemsData[i].monthlyInc + '" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="transactiontoggleCheck(this)"/></li>';
            }
            $("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='earningULList']").append(userList);
            var totalChecked = data.allUserEarningsItemsData.length;
            if (parseInt(totalChecked) > 0) {
                $("#userEarningdropdown").html("");
                $("#userEarningdropdown").html(totalChecked + " Items Selected" + "<b>&nbsp;&nbsp;&#8711;</b>");
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}

function getDeductionsListForOrg() {
    var jsonData = {};
    jsonData.userEmail = $("#hiddenuseremail").text();
    jsonData.userHiddenPrimKey = $("#userEntityHiddenId").val();
    jsonData.userName = $("#userName").val();
    var userPk = $("#userEntityHiddenId").val();
    if (userPk == "" || typeof userPk == 'undefined' || userPk == null) {
        swal("Error!","You can configure payroll for existing USER only!","error");
        return false;
    }
    var url = "/payroll/getUserDeductionData";
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
            $("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='deductionULList']").html("");
            $("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='deductionULList']").append('<li id="usrDeductionsList">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="" onClick="transactioncheckUncheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="deductionItemName" id="deductionItemName" value=""  onkeyup="transactiontoggleCheck(this)"/>&nbsp;&nbsp;<input type="text" class="input-small" name="deductionAnnual" id="deductionAnnual" value="0.0" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="transactiontoggleCheck(this);calculateMonthlyIncome(this);"/>&nbsp;&nbsp;<input type="text" class="input-small" name="deductionMonthly" id="deductionMonthly" value="0.0" onkeypress="return onlyDotsAndNumbers(event)" onkeyup="transactiontoggleCheck(this)"/>Select All</li>');
            var userList = "";
            for (var i = 0; i < data.allUserDeductionsItemsData.length; i++) {
                userList += '<li id="usrDeductionsList">&nbsp;&nbsp;<input type="checkbox" name="checkCOA" id="checkCOA" value="' + data.allUserDeductionsItemsData[i].id + '" onClick="transactioncheckUncheck(this)"  checked/>&nbsp;&nbsp;<input type="text" class="input-small" name="deductionItemName" id="deductionItemName' + data.allUserDeductionsItemsData[i].id + '" value="' + data.allUserDeductionsItemsData[i].name + '"  onkeyup="transactiontoggleCheck(this)" readonly="readonly"/>&nbsp;&nbsp;<input type="text" class="input-small" name="deductionAnnual" id="deductionAnnual' + data.allUserDeductionsItemsData[i].id + '" value=' + data.allUserDeductionsItemsData[i].annualInc + ' onkeypress="return onlyDotsAndNumbers(event)" onkeyup="transactiontoggleCheck(this);calculateMonthlyDeductionIncome(this);"/>&nbsp;&nbsp;<input type="text" class="input-small" name="deductionMonthly" id="deductionMonthly' + data.allUserDeductionsItemsData[i].id + '" value=' + data.allUserDeductionsItemsData[i].monthlyInc + ' onkeypress="return onlyDotsAndNumbers(event)" onkeyup="transactiontoggleCheck(this)"/></li>';
            }
            $("#newuserPayrollEarningsExcelFormTable tr[id='payrollSetUpRuleTr'] ul[id='deductionULList']").append(userList);
            var totalChecked = data.allUserDeductionsItemsData.length;
            if (parseInt(totalChecked) > 0) {
                $("#userDeductionsdropdown").html("");
                $("#userDeductionsdropdown").html(totalChecked + " Items Selected" + "<b>&nbsp;&nbsp;&#8711;</b>");
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        },
        complete: function (data) {
            $.unblockUI();
        }
    });
}
