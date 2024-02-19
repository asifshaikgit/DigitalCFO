$(document).ready(function(){
  $('.approveButton'). click(function(){
      var value = $('#inputValue').val();
      var jsonToSend = {};
      jsonToSend.name = value;
      var url_string = '/getData'
      var outputData = JSON.stringify(jsonToSend);
      $.ajax({
    	url:url_string,
        data:outputData,
        type:"text",
        method:"POST",
        contentType:'application/json',
        success: function(data) {
          console.log(data);
        },
        error: function() {
          swal("Error!","Error!","error");
        }
      });
    });
  });