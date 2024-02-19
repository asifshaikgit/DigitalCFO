/*
 * Contact Form Jquery
 * Inspired by http://trevordavis.net/blog/wordpress-jquery-contact-form-without-a-plugin
*/

//jQuery(document).ready(function($){
$(document).ready(function(){
	$('#contactForm').find('input').attr('autocomplete', 'off');
	$('#demoForm').find('input').attr('autocomplete', 'off');
	$('#sendMessage').find('input').attr('autocomplete', 'off');
    $('#indexDemoForm').find('input').attr('autocomplete', 'off');
    $('#knowmoreform').find('input').attr('autocomplete', 'off');

	$('form#contactForm #contactButton').click(function(){
		submitContactMsg('contactForm', 1, '#contactButton');
	});

	$('form#demoForm #demoButton').click(function(){
		submitContactMsg('demoForm', 2, '#demoButton');
	});

	$('form#sendMessage #msgButton').click(function(){
		submitContactMsg('sendMessage', 3, '#msgButton');
	});

    $('form#indexDemoForm #indexDemoBtn').click(function(){
        submitContactMsg('indexDemoForm', 2, '#indexDemoBtn');
    });

    $('form#knowmoreform #knowmoreBtn').click(function(){
        submitContactMsg('knowmoreform', 1, '#knowmoreBtn');
    });
});

var submitContactMsg = function(formname, enquiryType, fromBtn){
	$('.error').remove();
	var hasError = false;
	var forgot_error = ""; var email_error = "";
	$('form#'+formname+' .requiredField').each(function() {
		if(jQuery.trim($(this).val()) == '') {
			var labelText = $(this).attr('placeholder');
			if($('.error')!==''){
			 	$(this).parent().before('<span class="error" style="color:red;">'+forgot_error+' '+labelText+'.</span>');
			}
			hasError = true;
		} else if($(this).hasClass('email')) {
			var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
			if(!emailReg.test(jQuery.trim($(this).val()))) {
				var labelText = $(this).attr('placeholder');
				if($('.error')!==''){
				  $(this).parent().before('<span class="error" style="color:red;">'+email_error+' '+labelText+'.</span>');
				}
				hasError = true;
			}
		}  else if($(this).hasClass('captcha')) {
			 var captcha = $(this).val();
		}
	});

	if(!hasError) {			
		var contactName=$("#"+formname + " input[id='contactName']").val();
		var email = $("#"+formname + " input[id='contactemail']").val();
		var enqPhone = $("#"+formname + " input[id='contactphone']").val();
		if(enqPhone == 'undefined'){
			enqPhone="";
		}
		var comments = $("#"+formname + " textarea[id='contactcomments']").val();
		var companyName = $("#"+formname + " input[id='contactcompany']").val();
		if(companyName == 'undefined'){
			companyName="";
		}
		if(contactName!=="" && email!=="" && enqPhone !== ""){
            $(fromBtn).attr("disabled", "disabled");
			var jsonData = {};
			jsonData.cName = contactName;
			jsonData.enqEmail = email;
			jsonData.enqPhone = enqPhone;
			jsonData.enqComments = comments;
			jsonData.enquiryType = enquiryType;
			jsonData.companyName = companyName;
			var url="/idos/enquiry";
			$.ajax({
				url : url,
				data : JSON.stringify(jsonData),
				type : "text",
				method : "POST",
				contentType : 'application/json',
				success : function(data) {						
					/* $('#contactForm #submitMsg').fadeIn('normal', function() {
						$('#contactForm #submitLoading').remove();
					});*/
					$('#'+formname).before('<span class="error" style="color: red;">'+data.message+'</span>');
					setTimeout(function(){
						$('.error').fadeOut('normal', function() {								
							$(this).remove();
						});
					}, 7000);
				},
				error : function(xhr, status, error) {						
					$('.error').fadeOut('normal', function() {
						$(this).remove();
					});
				},
				complete: function(data) {
					$('#'+formname).find('input[type=text], textarea').val('');
					$('#'+formname).find('input[type=email], textarea').val('');
					$('#'+formname).find('input[type=tel]').val('');
					setTimeout(function(){
						$('#'+formname +" :button").removeAttr("disabled");
                        $(fromBtn).removeAttr("disabled");
					}, 7000);
				}
			});
		}else{
            $('#'+formname+" :button").removeAttr("disabled");
            $(fromBtn).removeAttr("disabled");
		}
	}
	return false;
}

