
jQuery(document).ready(function($){	
$('#generateApi').on('click', function(){	   
	   if (parseFloat($('.generate-api').css('right')) < 0) {
		   resetApiForm();
		   $('.generate-api').animate({right: '0px'}, 800);
	   } else {
		   $('.generate-api').animate({right: '-236px'}, 800);
		   resetApiForm();
	   }
   });
   $('body').on('click', function(e){
	   if (parseFloat($('.generate-api').css('right')) == 0 && $(e.target).attr('id') != 'generateApiDetails'
		   	&& !$(e.target).hasClass('key-details') && $(e.target).attr('id') != 'keyGenerate' && !$(e.target).hasClass('keyCol')) {
		   resetApiForm();
		   $('.generate-api').animate({right: '-236px'}, 800);
	   }
   });
   $('input.key-details, textarea.key-details').on('focus', function(){
	   if (this.value === this.title) {this.value = '';}
   });
   $('input.key-details, textarea.key-details').on('blur', function(){
	   if (this.value === this.title || '' === this.value) {this.value = this.title;}
   });
   var time = 0;
   $('#keyGenerate').on('click', function(){	   
	  var org = $.trim($('#keyOrganization').val());
	  var pName = $.trim($('#keyProductName').val());
	  var email = $.trim($('#keyEmail').val());
	  $('#keyDisplay').fadeOut('fast', function(){
		  $('#keyDisplay #keyMsg').html('');
		  $('#keyDisplay #keyShow').html('');
	  });
	  clearTimeout(time);
	  if ('' === org || 'Organization' === org){
		  $('#errorDisplay').html('Enter Organization Name').fadeIn();
		  $("#keyOrganization").val('').focus();
	  } else if ('' === pName || 'Product Name' === pName){
		  $('#errorDisplay').html('Enter Product Name').fadeIn();
		  $("#keyProductName").val('').focus();
	  } else if ('' === email || 'Email' === email){
		  $('#errorDisplay').html('Enter Email ID').fadeIn();
		  $("#keyEmail").val('').focus();
	  } else {
		  var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
		  if(!emailReg.test(email)) {
			  $('#errorDisplay').html('Enter Valid Email ID').fadeIn();
			  $("#keyEmail").val('').focus();
		  } else {
			  var jsonData = {};
			  jsonData.org = org;
			  jsonData.email = email;
			  jsonData.pName = pName;
			  jsonData.url = $.trim($('#keyCompanyUrl').val());;
			  jsonData.phone = $.trim($('#keyContact').val());;
			  jsonData.note = $.trim($('#keyNote').val());;
			  $.ajax({
				 url : '/application/generateKey',
				 data : JSON.stringify(jsonData),
				 type : "text",
				 method : "POST",
				 contentType : 'application/json',
				 success : function(data) {
					 if(data.result){
						 $('#errorDisplay').fadeOut('normal', function() {
							$(this).empty();
						 });
						 $('#keyDisplay #keyMsg').html(data.message1);
						 $('#keyDisplay #keyShow').html(data.message2);
						 $('#keyDisplay').fadeIn();
						 resetApiForm();
					 }else{
						 clearTimeout(time);
						 $('#errorDisplay').html(data.message).fadeIn();
						 if(undefined!==data.field){
							 $('#'+data.field).focus().val('');
						 }
						 time = setTimeout(function(){
							$('#errorDisplay').fadeOut('normal', function() {
								$(this).empty();
							});
						 }, 5000);
					 }
				 },
				 error : function(xhr, status, error) {
				 }
			  });
		  }
	  }
	  time = setTimeout(function(){
		$('#errorDisplay').fadeOut('normal', function() {
			$(this).empty();
		});
	  }, 5000);
   });
   function resetApiForm(){
		$('#keyOrganization').val('Organization');
		$('#keyProductName').val('Product Name');
		$('#keyEmail').val('Email');
		$('#keyCompanyUrl').val('Company URL');
		$('#keyContact').val('Contact Number');
		$('#keyNote').val('Note');
		$('#errorDisplay').empty();
	}
   $('.rest-show').on('click', function(){
	  var val=$(this).attr('data-id');
	  if(!$('#'+val).is(':visible')){
		  $('.rest-show').removeClass('rest-show-active');
		  $(this).addClass('rest-show-active');
		  $('.rest-options').slideUp();
		  $('#'+val).slideDown();
		  setTimeout(function(){
			  var url=$('.rest-options:visible').find('.url').html();
			  if (undefined != url && '' != url && null != url) {
				  $('span.url-display').html(url);
			  }
		  }, 700);
	  }
   });
   $("ul.tabs li").click(function(e) {	   
	   if(!$(this).hasClass('active')){
		   $("ul.tabs li").removeClass("active"); //Remove any "active" class
		    $(this).addClass("active"); //Add "active" class to selected tab
		    $(".tab_content").hide(); //Hide all tab content
		    var activeTab = $(this).find("a").attr("href"); //Find the href attribute value to identify the active tab + content
		    $(activeTab).fadeIn(); //Fade in the active ID content
		    $('.tab_container').getNiceScroll().resize();
		    return false;
	   }
   });
   $('.alt-generate-key').on('click',function(){
	   $('#generateApi').trigger('click');
   });
   
});