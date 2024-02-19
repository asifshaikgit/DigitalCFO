var openDataUpload = function () {
    $("#dataUploadDiv").show();
    $("#pendingExpense").hide();
}


function uploadTransactionData() {

    var chatofacturl = $("#uploadTransactionId").val();
    if (chatofacturl == "") {
        swal("Incomplete details!","please Select Template","error");
        return true;
    }
    var ext = chatofacturl.substring(chatofacturl.lastIndexOf('.') + 1);
    if (ext != "csv" && ext != "csv") {
        swal("Error!","Only Excel files are allowed for Branch upload","error");
        $("#uploadTransactionId").val("");
        return true;
    } else {
        var jsonData = {};
        var useremail = $("#hiddenuseremail").text();
        jsonData.usermail = useremail;
    }
    var form = $('#myUplodsForm');
    var data = new FormData();
    jQuery.each($('#uploadTransactionId')[0].files, function (i, file) {
        data.append('file-' + i, file);
    });
    $.blockUI({message: '<img align="middle" src="assets/images/loader.gif" height="80px" width="80px" />'});
    $.ajax({
        method: "POST",
        url: "/uploads/CSVUploads",
        data: data,
        headers: {
            "X-AUTH-TOKEN": window.authToken
        },
        cache: false,
        contentType: false,
        processData: false,
        success: function (data) {
            $("#uploadTransactionId").val("");
            $.unblockUI();
            swal("success!","Uploaded Successfully","success");
            for (var i = 0; i < data.successUploading.length; i++) {
                $("#uploadsDiv div[id='msgDiv']").append("<b>Total " + data.successUploading[i].totalRowsInserted + " records inserted.</b>");
            }
        },
        error: function (xhr, status, error) {
            if (xhr.status == 401) {
                doLogout();
            }
        }
    });
}


function downloadTxnTemplate() {
    var jsonData = {};
    jsonData.useremail = $("#hiddenuseremail").text();
    var url = "/config/downloadTxnTemplate";
    downloadFile(url, "POST", jsonData, "Error on downloading Transaction Template!");
}
