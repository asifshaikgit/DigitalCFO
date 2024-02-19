var FILE_SIZE = 5242880;
var UPLOAD_TYPE_BLOB = 1;
var uploadFileBlob = function(elem) {
    var parentTr = $(elem).closest('tr').attr('id');
    var txnRefrenceNo = "";
    if(parentTr.toLowerCase().indexOf("transaction") != -1){
        txnRefrenceNo = $(elem).closest('tr').attr('txnref');
    }
    var elementName = elem.id;
    elementName = elementName.replace('Btn', '');
    if (typeof $("#"+parentTr+" select[name='" + elementName + "']") !== 'undefined') {
        if($("#"+parentTr+" input[name='" + elementName + "']").val() != "" && typeof $("#"+parentTr+" input[name='" + elementName + "']").val() != 'undefined'){
            swal("Limit exceeded", "Number of uploads can not exceed the limit: 1", "error");
            return false;
        }
        var uploadCount = $("#"+parentTr+" select[name='" + elementName + "']").children('option').length;
        var noOfUploadsAllowed = $("#noOfUploadsAllowed").val();
        if(noOfUploadsAllowed == "" || typeof numberOfDocUploadsAllowed == 'undefined'){
            noOfUploadsAllowed = 5;
        }
        if (uploadCount > parseInt(noOfUploadsAllowed)) {
            swal("Limit exceeded", "Number of uploads can not exceed the limit: " + noOfUploadsAllowed, "error");
            return false;
        }
    }
    var uploadDestinationId = $("#uploadDestinationId").val();
    if (uploadDestinationId == 1) {
        $("#popupUploadDocumentDivId").attr('data-toggle', 'modal');
        $("#popupUploadDocumentDivId").modal('show');
        //$("popupUploadDocumentDivId").attr("href",location.hash);
        $("#popupUploadDocumentDivId #wrongActCred").html('');
        $("#popupUploadDocumentDivId input[type='file']").val(null);
        document.getElementById("uploadBlobFormId").reset();
        $("#popupUploadDocumentDivId #elementId").val(elementName);
        $("#popupUploadDocumentDivId #elementParentId").val(parentTr);
        $("#popupUploadDocumentDivId #txnRefrenceNoId").val(txnRefrenceNo);
    }else{
        uploadFileOnPicker(elem, elementName, parentTr, txnRefrenceNo);
    }
}

var scanAndUpload = function(){
    //var filesList = $("#uploadBlobId").val();
    var filesList = document.getElementById("uploadBlobId").files[0];
    var file;
    var message = "";
    if (filesList) {
        if (filesList.length == 0) {
            message = "Select one or more files.";
        } else {
            file = filesList;
        }
    } else {
        if (filesList == "") {
            message += "Select one or more files.";
        } else {
            message += "The files property is not supported by your browser!";
            message  += "<br>The path of the selected file: " + filesList.value; // If the browser does not support the files property, it will return the path of the selected file instead.
        }
    }

    var extention = file.name.substr(file.name.lastIndexOf('.') + 1).toLowerCase();
    if (extention == 'cmd' || extention == 'bat' || extention == 'sh' || extention == 'exe' || extention == 'dll'){
        $("#popupUploadDocumentDivId #wrongActCred").text('Invalid file format, Please select a document or image.');
        return;
    }
    var id =  $("#popupUploadDocumentDivId #elementId").val();
    if(id=="orgLogoUploads"){
        var mimeType = file.type.substr(0, file.type.indexOf('/')).toLowerCase();
        if (mimeType != 'image') {
            $("#popupUploadDocumentDivId #wrongActCred").text('Invalid image type.');
            return;
        }
        if (file.size > 1048576) {
            $("#popupUploadDocumentDivId #wrongActCred").text('Invalid logo image size, should be less than 1MB.');
            return;
        }
    }else if(file.size > FILE_SIZE){
        $("#popupUploadDocumentDivId #wrongActCred").text('Invalid file size, should not be more than 5MB.');
        return;
    }
    var retValue = scanAndUploadInBlob(file, file.size);
}

var scanAndUploadInBlob = function(fileUrl, fileSize){
    var returnValue = 200;
    //var fileUrl = $("#fsp-fileUpload").val();
    if(fileUrl==""){
        swal("File is not selected!", "Select a valid file.", "error");
        return true;
    }
    var parentTr = $("#popupUploadDocumentDivId #elementParentId").val();
    var txnRefrenceNo =  $("#popupUploadDocumentDivId #txnRefrenceNoId").val();
    var selectElemName = $("#popupUploadDocumentDivId #elementId").val();
    var data = new FormData();
    data.append("fileupload-1", fileUrl);
    var url = "/files/scan";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        method: "POST",
        url: url,
        data: data,
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        cache: false,
        contentType: false,
        processData: false,
        async:true,
        success: function(data) {
            $("#popupUploadDocumentDivId #wrongActCred").html("");
            if(data.statusCode === 202){
                $("#popupUploadDocumentDivId #wrongActCred").text("File contains virus!");
            }else if(data.statusCode === 201){
                $("#popupUploadDocumentDivId #wrongActCred").text("fail to scan File!");
            }else if(data.statusCode === 200){
                $("#popupUploadDocumentDivId #wrongActCred").text("Uploaded!");
                if(typeof parentTr !== 'undefined'){
                    var inputElementName = $("#" + parentTr + " select[name='"+selectElemName+"'] option:selected").val();
                    if(typeof inputElementName == 'undefined') {
                        $("#" + parentTr + " input[name='" + selectElemName + "']").val(data.fileUri);
                    }else{
                        $("#" + parentTr + " select[name='" + selectElemName + "']").append("<option value='" + data.fileUri + "'>" + data.fileName + "</option>");
                        $("#" + parentTr + " select[name='" + selectElemName + "']").find("option[value='" + data.fileUri + "']").prop("selected", "selected");
                    }
                }else{
                    var inputElementName = $("select[name='"+selectElemName+"'] option:selected").val();
                    if(typeof inputElementName == 'undefined') {
                        $("input[name='" + selectElemName + "']").val(data.fileUri);
                    }else{
                        $("select[name='" + selectElemName + "']").append("<option value='" + data.fileUri + "'>" + data.fileName + "</option>");
                        $("select[name='" + selectElemName + "']").find("option[value='" + data.fileUri + "']").prop("selected", "selected");
                    }
                }
                $('#popupUploadDocumentDivId').modal('toggle');
                insertIntoIdosFileUploadLogs(parentTr, selectElemName, data.fileName, fileSize, data.fileUri);
                if(selectElemName == "txnViewListUpload"){
                    var jsonData={};
                    jsonData.email = $("#hiddenuseremail").text();
                    jsonData.modelId = parentTr;
                    jsonData.txnRefrenceNo = txnRefrenceNo;
                    jsonData.docUrl = data.fileUri;
                    jsonData.fileName = data.fileName;
                    ajaxCall('/files/uploadTxn', jsonData, '', '', '', '', 'fileUploadBlobTxnSuccess', '', false);
                }
            }else{
                $("#popupUploadDocumentDivId #wrongActCred").text(data.statusMsg);
            }
            return returnValue = data.statusCode;
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){
                doLogout();
            }else if(xhr.status == 500){
                $("#popupUploadDocumentDivId #wrongActCred").text("Please contact support team.");
            }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
    return returnValue;
}

var fileUploadBlobTxnSuccess = function(data){
    if(data.result){
    }
}

var fillSelectElementWithUploadedDocs = function (txndocument, mainTblTrId, selectElemName) {
    if(txndocument !="" && txndocument != null){
        $("#" + mainTblTrId + " select[name='"+selectElemName+"']").children().remove();
        $("#" + mainTblTrId + " select[name='"+selectElemName+"']").append('<option value="">Select a file</option>');
        var fileURLWithUser = txndocument.substring(0,txndocument.length).split(',');
        for(var j=0; j<fileURLWithUser.length; j++){
            var fileURLWithoutUser=fileURLWithUser[j].substring(0, fileURLWithUser[j].length).split('#');
            if(fileURLWithoutUser.length < 2){
                if(fileURLWithoutUser[0] != "") {
                    var fileName = getFileStat('', fileURLWithoutUser[0], mainTblTrId, selectElemName);
                }
            }else if(fileURLWithoutUser[0] != "" && fileURLWithoutUser[1] != "") {
                var fileName = getFileStat(fileURLWithoutUser[0], fileURLWithoutUser[1], mainTblTrId, selectElemName);
            }
        }
    } else {
        $("#" + mainTblTrId + " select[name='"+selectElemName+"']").children().remove();
        $("#" + mainTblTrId + " select[name='"+selectElemName+"']").append('<option value="">Select a file</option>');
    }
}

var deleteUploadedFile = function(elem){
    var parentTr = $(elem).closest('tr').attr('id');
    var elementName =elem.id;
    elementName = elementName.replace('DelBtn', '');
    var fileUrl = "";

    if(typeof $("input[name='"+elementName+"']").val() != 'undefined'){
        if($("#"+parentTr+" input[name='"+elementName+"']").val() != ''){
            fileUrl = $("#"+parentTr+" input[name='"+elementName+"']").val();
            $("#"+parentTr+" input[name='"+elementName+"']").val('');
        }
    } else {
        var uploadCount = $("#" + parentTr + " select[name='"+elementName+"']").children('option').length;
        if(typeof uploadCount === 'undefined'){
                fileUrl = $("select[name='"+elementName+"'] option:selected").val();
                uploadCount = $("select[name='"+elementName+"']").children('option').length;
                if(uploadCount > 0 && fileUrl != "" && fileUrl != 'undefined'){
                    $("select[name='"+elementName+"']").find("option:selected").remove();
                }
        }else{
            fileUrl = $("#" + parentTr + " select[name='"+elementName+"'] option:selected").val();
            if(uploadCount > 0 && fileUrl != "" && fileUrl != 'undefined'){
                $("#" + parentTr + " select[name='"+elementName+"']").find("option:selected").remove();
            }else{
                $("select[name='"+elementName+"']").find("option:selected").remove();
            }
        }
    }
    var fileUrlTmp = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
    var uploadDestinationId = $("#uploadDestinationId").val();
    if(uploadDestinationId == 0){
      /* // remove from API
        if(fileUrlTmp != "") {
        	//var security = policySignatureOptions.security;
            filepicker.remove(fileUrlTmp).then((response) => {
                console.log(response);
            }).catch((err) => {
                console.log(err);
            });
        }
       */
       var jsonData = getParametersForFile(parentTr, elementName, fileUrlTmp, -1, fileUrl);
       ajaxCall('/files/delete', jsonData, '', '', 'POST', '', 'fileUploadBlobTxnSuccess', '', true);
    }else if(elementName == "txnViewListUpload"){
        var jsonData = getParametersForFile(parentTr, elementName, fileUrlTmp, -1, fileUrl);
        ajaxCall('/files/delete', jsonData, '', '', 'POST', '', 'fileUploadBlobTxnSuccess', '', true);
    }
}

var downloadFileBlob = function (elem){
    let elementName = elem.id;
    elementName = elementName.replace('DownloadBtn', '');
    let parentTr = $(elem).closest('tr').attr('id');
    let url = $("#" + parentTr + " select[name='" + elementName + "'] option:selected").val();
    if(typeof url == 'undefined' || url == "" || url == null){
        url = $("select[name='" + elementName + "'] option:selected").val();
    }
    if(typeof url == 'undefined' || url == "" || url == null){
       return false;
    }else{
        window.open(url);
    }
}

var getParametersForFile = function(parentTr, selectElemName, fileFullName, fileSize, uploadUrl){
    var transactionRefNo = "";
    var uploadDestinationId = $("#uploadDestinationId").val();

    if(fileSize == -1){  // case of delete
        transactionRefNo =  $("#"+parentTr).attr('txnref');
    }else if(uploadDestinationId == UPLOAD_TYPE_BLOB){
        parentTr = $("#popupUploadDocumentDivId #elementParentId").val();
        transactionRefNo =  $("#popupUploadDocumentDivId #txnRefrenceNoId").val();
        selectElemName = $("#popupUploadDocumentDivId #elementId").val();
	}

    var orgId = $("#hiddenOrgId").val();
    var branchId = $("#branchEntityHiddenId").val();
    var custId = $("#customerEntityHiddenId").val();
    var vendorId = $("#vendorEntityHiddenId").val();
    var jsonData = {};
    jsonData.email=$("#hiddenuseremail").text();
    jsonData.uploadDestinationId = $("#uploadDestinationId").val();
    jsonData.fileName = fileFullName;
    jsonData.fileSize = fileSize;
    jsonData.fileUrl = uploadUrl;
    jsonData.uploadModuleElemName = selectElemName;
    jsonData.uploadModuleElemParent = parentTr;
    if(parentTr.toUpperCase().indexOf(ORG_MODULE_TYPE) != -1){
        jsonData.uploadModule = ORG_MODULE_TYPE;
    } else if (parentTr.toUpperCase().indexOf(BRANCH_MODULE_TYPE_FOR_INDEX) != -1){
        jsonData.uploadModule = BRANCH_MODULE_TYPE;
        var branchId = $("#branchEntityHiddenId").val();
    } else if (parentTr.toUpperCase().indexOf(CUSTOMER_MODULE_TYPE) != -1){
        jsonData.uploadModule = CUSTOMER_MODULE_TYPE;
        var custId = $("#customerEntityHiddenId").val();
    } else if (parentTr.toUpperCase().indexOf(VENDOR_MODULE_TYPE) != -1){
        jsonData.uploadModule = VENDOR_MODULE_TYPE;
        var vendorId = $("#vendorEntityHiddenId").val();
    } else if (transactionRefNo != null && transactionRefNo != '' && typeof transactionRefNo != 'undefined'){
        jsonData.referenceId = parentTr.substring(parentTr.lastIndexOf('y')+1, parentTr.length);
        if(transactionRefNo.indexOf(BOM_TXN_TYPE) != -1){
            jsonData.uploadModule = BOM_TXN_TYPE;
        } else if(transactionRefNo.indexOf(CLAIM_TXN_TYPE_FOR_INDEX) != -1){
            jsonData.uploadModule = CLAIM_TXN_TYPE;
        } else if(transactionRefNo.indexOf(PJE_TXN_TYPE_FOR_INDEX) != -1){
            jsonData.uploadModule = PJE_TXN_TYPE;
        } else if(transactionRefNo.indexOf(MAIN_TXN_TYPE) != -1){
            jsonData.uploadModule = MAIN_TXN_TYPE;
        }
    }
    return jsonData;
}
/**************************************File picker **************************************************************/

var scanfile = function(fileUrl){
    var returnValue = 200;
    //var fileUrl = $("#fsp-fileUpload").val();
    if(fileUrl==""){
        swal("File is not selected!", "Select a valid file.", "error");
        return true;
    }
    var data = new FormData();
    var fileTmp = $('#fsp-fileUpload')
    if(fileUrl.source==='local_file_system'){
        jQuery.each($('#fsp-fileUpload')[0].files, function(i, file) {
            data.append('file-'+i, file);
        });
    }else{
        //data.append('file-0', fileUrl);
        return returnValue;
    }

    var url = "/files/scan";
    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        method: "POST",
        url: url,
        data: data,
        headers:{
            "X-AUTH-TOKEN": window.authToken
        },
        cache: false,
        contentType: false,
        processData: false,
        async:false,
        success: function(data) {
            if(data.status === "failed" || data.statusCode != "200"){
                returnValue = 201;
            }
            if(data.isclean === "false"){
                returnValue = 202;
                //throw new Error("File contqain virus!");
            }
            return returnValue;
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){ doLogout(); }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
    return returnValue;
}

var uploadFileOnPicker = function(elem, elementName, parentTr, txnRefrenceNo){
    filepicker.pick({
        storeTo: { location: 's3' },
        fromSources:["local_file_system","url","imagesearch","facebook","instagram","googledrive","dropbox","evernote","flickr","box","github","gmail","picasa","onedrive","clouddrive","webcam","video","audio","customsource"],
        accept:["image/*","video/*","audio/*",".pdf",".doc",".docx",".docm","text/plain",".xlsx",".xls",".rtf", ".odt"],
        onFileSelected: function(file){
            var extention = file.filename.substr(file.filename.lastIndexOf('.') + 1).toLowerCase();
            if (extention == 'cmd' || extention == 'bat' || extention == 'sh' || extention == 'exe' || extention == 'dll'){
                throw new Error('Please select a document or image.');
            }
            if(id=="uploadLogo"){
                var mimeType = file.mimetype.substr(0, file.mimetype.indexOf('/')).toLowerCase();
                if (mimeType != 'image') {
                    throw new Error('Invalid image type.');
                }
                if (file.size > 1048576) {
                    throw new Error('Invalid logo image size, should be less than 1MB.');
                }
            }
            var retValue = scanfile(file);
            if(retValue === 202){
                throw new Error("File contains virus!");
            }else if(retValue === 201){
                throw new Error("fail to scan File!");
            }
        }
    }).then(function(response) {
        var uploadUrl = response.filesUploaded[0].url;
        var fileFileFullName = response.filesUploaded[0].filename;
        var fileSize = response.filesUploaded[0].size;
        $('input[name="'+elementName+'"]').val(uploadUrl);
        var intSize=parseInt(fileSize);
        if(elementName === "uploadLogo"){
            if(intSize> (1*1024*1024)){ //less than 1MB
                swal("Invalid!","Please Upload Your Organization Logo of size less than 1MB.","error");
                $('input[name="'+id+'"]').val("");
                return false;
            }else{
                insertIntoIdosFileUploadLogs(parentTr, elementName, fileFileFullName, fileSize, uploadUrl);
            }
        }else{
            insertIntoIdosFileUploadLogs(parentTr, elementName, fileFileFullName, fileSize, uploadUrl);
        }
        if(elementName === "txnViewListUpload"){
            var jsonData={};
            jsonData.email=$("#hiddenuseremail").text();
            jsonData.modelId=parentTr;
            jsonData.txnRefrenceNo = txnRefrenceNo;
            jsonData.docUrl=uploadUrl;
            jsonData.fileName=fileFileFullName;
            ajaxCall('/files/uploadTxn', jsonData, '', '', '', '', 'fileUploadTxnSuccess', '', false);
        }else{
            $("select[name='"+elementName+"']").append("<option value='"+uploadUrl+"'>"+fileFileFullName+"</option>");
        }
    });
}


function fileUploadTxnSuccess(data){
    if(data.result){
        var parentTr=data.resultTxn[0].parentTr;
        var fileUrl=data.resultTxn[0].fileUrl;
        var fileName=data.resultTxn[0].fileName;
        $("#"+parentTr+" select[class='auditorAccountantSelect']").append("<option value='"+fileUrl+"'>"+fileName+"</option>");
    }
}

function insertIntoIdosFileUploadLogs(parentTr, elementName, fileFileFullName, fileSize, uploadUrl){
    var jsonData = getParametersForFile(parentTr, elementName, fileFileFullName, fileSize, uploadUrl);
    ajaxCall('/files/insertFileLogs', jsonData, '', '', '', '', 'insertIntoIdosFileUploadLogsSuccess', '', true);
}

function insertIntoIdosFileUploadLogsSuccess(data){
    if(data.result){
    }else{
        swal("Error!","Error In Uploading File. Server Connection Lost.","error");
        return true;
    }
}

function getFileStat(fileuser, fileUrl, parentElemId, selectElemId){
    var uploadDestinationId = $("#uploadDestinationId").val();
    var fileName = "";
    if(uploadDestinationId == "1") {
        var fileUrlTmp = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        fileName = fileUrlTmp;
    }else{
        fileName = getFileInfo(fileUrl+"/metadata");
    }
    appendtotxndoc(fileuser, fileUrl, fileName, parentElemId, selectElemId);
    return fileName;
}

var getFileInfo = function(fileUrl){
    var returnValue = "";
    if(fileUrl==""){
        swal("File is not selected!", "Select a valid file.", "error");
        return returnValue;
    }

    $.blockUI({ message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />' });
    $.ajax({
        method: "GET",
        url: fileUrl,
        async: false,
        success: function(data) {
            returnValue = data.filename;
        },
        error: function (xhr, status, error) {
            if(xhr.status == 401){ doLogout(); }
        },
        complete: function(data) {
            $.unblockUI();
        }
    });
    return returnValue;
}

function appendtotxndoc(fileuser, fileUrl, fileName, parentElemId, selectElemId){
    var newFileName=fileuser+"#"+fileName;
    $("#" +parentElemId + " select[name='"+selectElemId+"']").append('<option value='+fileUrl+'>'+newFileName+'</option>');
}

function deleteUncomittedFiles(){
    var jsonData = {};
    jsonData.email=$("#hiddenuseremail").text();
    //ajaxCall('/files/deleteUncommittedFiles', jsonData, '', '', '', '', 'deleteUncomittedFilesSuccess', '', true);
}

function deleteUncomittedFilesSuccess(data){
    if(data.result){
    }
}

/*
function getFile(elem){
    var url=$(elem).val();

    if(url!=""){
        var appendedUrl="?dl=true";
        var fullUrl=url+appendedUrl;
        window.open(fullUrl);
    }
}
function retrieveFile(fileuser, fileUrl, transTrId, selectid){
    var fileName="";
    var fileUrlTmp = fileUrl.substring(fileUrl.lastIndexOf('/')+1);
    console.log(fileUrlTmp);
    filepicker.retrieve(fileUrlTmp, { metadata: true }).then(function(response){
        console.log(response.filename);
        fileName=response.filename;
        appendToDocList(fileuser, fileUrl, fileName, transTrId, selectid);
    });
    return fileName;
}
function appendToDocList(fileuser, fileUrl, fileName, transTrId, selectid ){
    var newFileName="";
    if(fileuser == ""){
        newFileName = fileName;
    }else{
        newFileName=fileuser+"#"+fileName;
    }
    $("#"+transTrId+" select[id='"+selectid+"']").append('<option value='+fileUrl+'>'+newFileName+'</option>');
}


function uploadTableRowFile(id,elem){
    //filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
    filepicker.setKey('AgCDcsM5HTIuhRtbbBe4Iz');
    var trId=$(elem).parent().parent("tr:first").attr('id');
    var fileurl="";
    filepicker.pickAndStore({multiple:"true"},{location:"S3"},function(fpfiles){
        jQuery.each(fpfiles, function() {
            fileurl+=this.url+",";
        });
        var uploadUrl=fileurl.substring(0, fileurl.length-1);
        $('#'+trId+' input[name="'+id+'"]').val(uploadUrl);
    });
}


function getFileStatForOrg(fileuser,fileUrl,id){
    var fileName="";
    var fileUrlTmp = fileUrl.substring(fileUrl.lastIndexOf('/')+1);
    filepicker.retrieve(fileUrlTmp, { metadata: true }).then(function(response){
        fileName=JSON.stringify(response.filename);
        appendtoOrgdoc(fileuser, fileUrl, fileName, id);
    });
    return fileName;
}
function appendtoOrgdoc(fileuser,fileUrl,fileName,id){
    var newFileName=fileName;
    $('select[id='+id+']').append('<option value='+fileUrl+'>'+newFileName+'</option>');
}

function downloadfinfile(id){
	var url = document.getElementById(id).value;
	if(url == "" || url == null){
		return false;
	}
	//alert("Down url-- "+url);

	if(typeof url != 'undefined'){
		window.open(url);
	}else {
		url = $("#docuploadurl[name='"+id+"']").val();
		window.open(url);
	}
}

var uploadFileMultipleWithClass = function(id){
	//var filepicker = filestack.init('A7ucPpqRuR46F7OVE8CHJz');
	filepicker.pick({
		storeTo: { location: 's3' },
		fromSources:["local_file_system","url","imagesearch","facebook","instagram","googledrive","dropbox","evernote","flickr","box","github","gmail","picasa","onedrive","clouddrive","webcam","video","audio","customsource"],
		accept:["image/*","video/*","audio/*",".pdf",".doc",".docx",".docm","text/plain",".xlsx",".xls",".rtf", ".odt"],
		onFileSelected: function(file){
			var extention = file.filename.substr(file.filename.lastIndexOf('.') + 1).toLowerCase();
			if (extention == 'cmd' || extention == 'bat' || extention == 'sh' || extention == 'exe' || extention == 'dll'){
				throw new Error('Please select a document or image.');
			}
			if(id=="uploadLogo"){
				var mimeType = file.mimetype.substr(0, file.mimetype.indexOf('/')).toLowerCase();
				if (mimeType != 'image') {
					throw new Error('Invalid image type.');
				}
				if (file.size > 1048576) {
					throw new Error('Invalid logo image size, should be less than 1MB.');
				}
			}
			var retValue = scanfile(file);
			if(retValue === 202){
				throw new Error("File contains virus!");
			}else if(retValue === 201){
				throw new Error("fail to scan File!");
			}

		}
    }).then(function(response) {
		//handleFilestack(response);
		var $elem=$("#"+id+""); var selectClass="";
		var parentTr = $($elem).closest('tr').attr('id');
		var uploadUrl = response.filesUploaded[0].url;
		document.getElementById(id).value=uploadUrl;
		var fileFileFullName = response.filesUploaded[0].filename;
		var fileSize = response.filesUploaded[0].size;
		if(typeof parentTr != 'undefined'){
			$('#'+parentTr+' input').each(function() {
				if($(this).hasClass(""+id+"")) {
					$(this).val(uploadUrl);
				}
			});
		}else{
			$('input').each(function() {
				if($(this).hasClass(""+id+"")) {
					$(this).val(uploadUrl);
				}
			});
		}
		var intSize=parseInt(fileSize);
		if(id=="uploadLogo"){
			if(intSize> (1*1024*1024)){ //less than 1MB
				alert("Please Upload Your Organization Logo of size less than 1MB.");
				$('input[name="'+id+'"]').val("");
				return false;
			}else{
				inserIntoIdosFileUploadLogs(fileFileFullName,fileSize,uploadUrl);
			}
		}else{
			inserIntoIdosFileUploadLogs(fileFileFullName,fileSize,uploadUrl);
		}
		if(typeof parentTr!=undefined){
			selectClass=$("#"+parentTr+" select[class='auditorAccountantSelect']").attr('class');
			if(selectClass!=undefined && selectClass!=""){
				if(selectClass=="auditorAccountantSelect"){
					$('#'+parentTr+' input[name="'+id+'"]').val("");
					//insert into the document fiels and return the file name and populate the drop down in transaction claim table
					var jsonData={};
					jsonData.email=$("#hiddenuseremail").text();
					jsonData.modelId=parentTr;
					jsonData.docUrl=uploadUrl;
					jsonData.fileName=fileFileFullName;
					ajaxCall('/files/uploadTxn', jsonData, '', '', '', '', 'fileUploadTxnSuccess', '', false);
				}
			}
		}
	});
}
*/
