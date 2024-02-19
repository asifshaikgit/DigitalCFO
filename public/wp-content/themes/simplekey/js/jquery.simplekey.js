/**
 * Created by ThemeVan.
 * SimpleKey Jquery functions.
 */
jQuery(function ($) { 
  /*Loading HomePage*/
  if(isLoad==1){
	$('body').css('display','none');
    $('body').jpreLoader({
		loaderVPos: '50%'
	});
  } 
});
 
jQuery(document).ready(function($){
	alert("inside jquery.simplekey.js");
  function initPrimaryNavi(){
	   if($(window).width() >= 640) {
		   /*Fix the primary navi when scrolling*/
		   $("#primary-menu").sticky({topSpacing:0});
	   }
	   
       /*Sub menu*/
	   $("ul.sf-menu").superfish({
	       pathLevels:    4 ,
		   delay:         100,
		   autoArrows:    false
	   });
	   
	   /*Mobile menu*/
	   $('#mobileMenu').html($('#primary-menu-container').html());
	   $('#mobileMenu').mobileMenu({
				defaultText: 'Navigate to...',
				className: 'select-menu',
				subMenuDash: '&nbsp;&nbsp;'
	   });
	   $(".select-menu").each(function(){  
			$(this).wrap('<div class="css3-selectbox">');
		});
   }
   
   $('#primary-menu-container li').each(function() {
			var i=1;
			if($(this).hasClass('none')) {
			  $(this).remove();
			}
   });
   
   initPrimaryNavi();
   
   /*Init Portfolio block*/
   $('.overlay').hide();
   function initPortfolioBlocks(){
	   $('.portfolio-item').fadeIn();
       var MobileDetect = navigator.userAgent.match(/(iPhone|iPod|iPad|Android|BlackBerry)/);
	   if(MobileDetect) {
	      $(window).load(function(){
	           $('.portfolio-item').fadeIn();
	      });
	      $('.home .portfolio-item').add('.page .portfolio-item').add('.archive .portfolio-item').click(function(){
	           var permalink=$(this).find('.info').attr('href');
	           $(this).attr('href',permalink);
	           location.href=permalink;
	      });
	   }else{
		   /*Show Portfolios detail*/
		   function portfolioHoverIn(){
			   $(this).children('.overlay').fadeIn(200);
			   $(this).children('.tools').fadeIn(200);
		   }
		   /*Hide Portfolios detail*/
		   function portfolioHoverOut(){
			   $(this).children('.overlay').fadeOut();
			   $(this).children('.tools').fadeOut();
		   }
	       $('.portfolio-item').hoverIntent({
				 sensitivity: 2,
				 interval: 20,
				 over: portfolioHoverIn,
				 timeout: 0,
				 out: portfolioHoverOut
		   });
		   $('.portfolio-item a.overlay').click(function(){
		       $(this).fadeOut();
			   $(this).next('.tools').fadeOut();
		   });
	   }
	   
	   function portfolio_isotope(){
		   
	   	//init isotope
		$('.portfolios').isotope({ 
			  itemSelector: '.portfolio-item',
			  animationEngine: 'best-available',
			  filter: "*"
	     });
		 
		 if($(window).width() <= 1024 && isResponsive==1) {
				//Set Portfolio Height on Mobile
				var portfolioWidth=$('.portfolio-item').width();
				$('.portfolio-item').css('height',portfolioWidth+'px');
		 }
	   }
	   $(window).load(function(){portfolio_isotope();});
	   $(window).resize(function(){portfolio_isotope();});
	   
	   $('#filter a').click(function(){
		  var selector = $(this).attr('data-filter');
		  $('.portfolios').isotope({ 
		    filter: selector
		  });
		  $(this).parent().attr('class','filter_current');
		  $(this).parent().siblings().removeAttr('class');
		  return false;
		});
		
		//Ajax load content
		if(!MobileDetect) {
		  $('.portfolio-item a.ajax').click(function(){
			 var url=$(this).parent().attr('data-url');
			 if(url!==''){
				if(isNiceScroll==1){
				   if($(window).width() >= 640) {
					$("body").getNiceScroll().hide();
				   }
				};
				portfolioTop = $(this).parent().offset().top;
				$("#ajax-load").slideDown();
				ajaxload('#ajax-content',url,'#portfolio-single');
				//Load effects
				$('.flexslider').flexslider();
			 }
		  });
		}
			$("#ajax-load #close").click(function(){
				$('html,body').animate({scrollTop:portfolioTop-100},'slow');
				$("#ajax-load").slideUp();
				$('#ajax-content').html('');
				if(isNiceScroll==1){
				 if($(window).width() >= 640) {
					$("body").getNiceScroll().show();
				 }
				}else{
					$("body").css('overflow','auto');
				};
			});


		
   }initPortfolioBlocks();
   
   /*Init Team block*/
   function initTeamBlocks(){
	   /*Show Portfolio's detail*/
	   function TeamHoverIn(){
		   $(this).children('.overlay').fadeIn();
	   }
	   /*Hide Portfolio's detail*/
	   function TeamHoverOut(){
		   $(this).children('.overlay').fadeOut();
	   }
	   $('.member .avatar').hoverIntent({
			 sensitivity: 2,
			 interval: 100,
			 over: TeamHoverIn,
			 timeout: 0,
			 out: TeamHoverOut
	   })
   }initTeamBlocks();
  
   /*Parallax Effect*/
   $('.parallax').each(function(){
		 if($(window).width()>768){
		   $(this).parallax("50%", 0.5);
		 }else{
		    $(this).css("background-attachment%", "scroll");
		 }
   });

   

   function initPageScroll(){
	   /*Smooth Scroll to section*/
	   $.localScroll({
		target:'body',
		duration:1000,
		queue:true,
		hash:true,
		easing:'easeInOutExpo',
		offset: {left: 0, top: -65}
	   });
	   
	//Detecting page scroll and set the navigation link active status
	if($('body').hasClass('home')){
		$(window).scroll(function() {
	
			var currentNode = null;
			$('.page-area').each(function(){
				var currentId = $(this).attr('id');	
				if($(window).scrollTop() >= $('#'+currentId).offset().top - 79)
				{
					currentNode = currentId;
				}
			});
			$('#primary-menu li').removeClass('current-menu-item').find('a[href="#'+currentNode+'"]').parent().addClass('current-menu-item');
		});
	}
	   
	   /*Smooth scroll event*/
	   if(isNiceScroll==1){
		 if($(window).width() >= 640) {
			$("body").niceScroll({
			   cursorcolor:"#000",
			   scrollspeed:70,
			   horizrailenabled:false,
			   autohidemode:true,
			   cursorwidth:10
			});
	     }	
	   }
   }initPageScroll();
   
   /*Top slider*/
   $('#featured').flexslider({
	   slideshowSpeed: slidePlayingSpeed,
	   animationSpeed: slideTransitionSpeed,
	   pauseOnHover: true,
	   video: true,
	   keyboard: true,
	   multipleKeyboard: true
   });
   
   /*Flex slider*/
   $('.flexslider').flexslider({
	   slideshow:true,
	   video: true,
	   keyboard: true,
	   smoothHeight:true,
       multipleKeyboard: true
   });

   /*Display the slider background on mobile & tablet*/
   $(window).load(function() {
	  if(isResponsive==1){
	   if($(window).width() <= 1024 && $(window).width() >= 768) {
		 replaceSliderBg('data-ipad');
	   }
	   if($(window).width() <= 640) {
		 replaceSliderBg('data-mobile');
	   }
	  }
   });
   
   function replaceSliderBg(data){
     $('.slide_bg').each(function() {
         var newSrc=$(this).children('img').attr(data);
	     if(newSrc!==''){
           $(this).children('img').attr('src',newSrc);
	     }
     });
   }
   
   /*Lightbox*/
   $('a.lightbox').colorbox({
	  maxWidth:"98%"
   });
   $('.attachment a').colorbox({
	  maxWidth:"98%",
   });
   $('.gallery-icon a').colorbox({
	  maxWidth:"98%",
	  onComplete:function(){
	     $('body').css('overflow','auto');
	  }
   });
   $(".iframe_window").colorbox({iframe:true, width:"98%", height:"98%"});
   
   /*Lazyload*/
   if (navigator.platform == "iPad") return;
   $("img").lazyload({
       effect:"fadeIn",
       placeholder: pixel
   });
   
   /*Placeholder for IE*/
   $("input, textarea").placeholder();
   
   /*Display back to top button*/
	$(window).scroll(function(){
	  if($(document).scrollTop()==0){
		  $('#backtoTop').hide();
	  }else{
	      $('#backtoTop').show();
	  }
	});
	/*Back to Top*/
	$('#backtoTop').click(function(){
		$('body').animate({scrollTop:0},'slow');
		return false;
	});
	
	function resizeCenterWrapper(){
	  if($(window).width()<=640){
	    $('.centerWrapper').css('width','100%');
	  }
	}
	$(window).load(function(){resizeCenterWrapper();});
	$(window).resize(function(){resizeCenterWrapper();});

   
   /*Ajax load*/
   function ajaxload(id,url,object) { 
	$(id).addClass("loader"); 
	$.ajax({ 
		type: "get", 
		url: url, 
		cache: false, 
		error: function() {(id).html('Loading error!');}, 
		success: function(data) { 
			$(id).removeClass("loader"); 
            $("body").css({"overflow":"hidden"});
			$("#ajax-load").css({"overflow":"auto"});
			$content=$(data).find(object).html();
			$(id).append($content);
			//Load effects
			$('.flexslider').flexslider();
			$('.attachment a').colorbox({ maxWidth:"98%",onComplete:function(){ $('body').css('overflow','auto'); }});
            $('#ajax-content .gallery-icon a').colorbox({ maxWidth:"98%",onComplete:function(){ $('body').css('overflow','auto'); }});
            $('#ajax-content a.lightbox').colorbox({ maxWidth:"98%",onComplete:function(){ $('body').css('overflow','auto'); }});
		}
	}); 
   }
   
   $('#mc-embedded-subscribe-form').removeAttr('action').removeAttr('target');
   $('#mc-embedded-subscribe-form').find('input').attr('autocomplete', 'off');
   
   $("#mc-embedded-subscribe-form").submit(function(){
	   var email=jQuery.trim($("#mce-EMAIL").val());
	   if(email!=""){
		   var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
			if(!emailReg.test(email)) {
				$('#mc-embedded-subscribe-form').after('<span class="error" style="color: red;">Please enter a valid email id.</span>');
				$("#mce-EMAIL").val('').focus();
				setTimeout(function(){
					$('.error').fadeOut('normal', function() {
						$(this).remove();
					});
				 }, 5000);
			} else {
				var jsonData = {};
				   jsonData.email = email;
				   var url="/idos/subscriberNewsBlog";
				   $.ajax({
					 url : url,
					 data : JSON.stringify(jsonData),
					 type : "text",
					 method : "POST",
					 contentType : 'application/json',
					 success : function(data) {
						 var style=' style="color: white;"';
						 if (!data.result) {
							 style=' style="color: red;"';
						 }
						 $('#mc-embedded-subscribe-form').after('<span class="error"' + style + '>' + data.message + '</span>');
						 $("#mce-EMAIL").val('');
						 setTimeout(function(){
							$('.error').fadeOut('normal', function() {
								$(this).remove();
							});
						 }, 5000);
					 },
					 error : function(xhr, status, error) {
					 }
				   });
			}
	   } else {
		   $('#mc-embedded-subscribe-form').after('<span class="error" style="color: red;">Please enter a valid email id.</span>');
		   $("#mce-EMAIL").focus();
			setTimeout(function(){
				$('.error').fadeOut('normal', function() {
					$(this).remove();
				});
			 }, 5000);
	   }
	   return false;
   });
   
   $(".built-in-btn").click(function(){
	   var author=$("#author").val();
	   var email=$("#email").val();
	   var url=$("#url").val();
	   var comment=$("#comment").val();
	   if(author!="" && email!=""){
		   var jsonData = {};
		   jsonData.blogAuthor = author;
		   jsonData.blogEmail = email;
		   jsonData.blogUrl = url;
		   jsonData.blogComment = comment;
		   var url="/idos/blogReply";
		   $.ajax({
			 url : url,
			 data : JSON.stringify(jsonData),
			 type : "text",
			 method : "POST",
			 contentType : 'application/json',
			 success : function(data) {
			 },
			 error : function(xhr, status, error) {
			 }
		   });
	   }
   });
   
   $('#generateApi').on('click', function(){
	   alert("Inside generate api on click function");
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
	   alert("inside key generate function");
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
   $("#channelPartnerForm").submit(function(){
	   var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;
	   var email=jQuery.trim($("#emailCP").val()),name=jQuery.trim($('#contactNameCP').val()),
	   sub=jQuery.trim($('#contactSubjectCP').val()),com=jQuery.trim($('#commentsCP').val());
	   $('span.error').html('');
	   if(name==""){
		   $('#channelPartnerForm').before('<span class="error" style="color: red;">Please enter a name.</span>');
		   $("#contactNameCP").focus();
			setTimeout(function(){
				$('.error').fadeOut('normal', function() {
					$(this).remove();
				});
			 }, 5000);
	   }else if(email==""){
		   $('#channelPartnerForm').before('<span class="error" style="color: red;">Please enter a valid email id.</span>');
		   $("#emailCP").focus();
			setTimeout(function(){
				$('.error').fadeOut('normal', function() {
					$(this).remove();
				});
			 }, 5000);
	   }else if(!emailReg.test(email)){
		   $('#channelPartnerForm').before('<span class="error" style="color: red;">Please enter a valid email id.</span>');
		   $("#emailCP").focus();
			setTimeout(function(){
				$('.error').fadeOut('normal', function() {
					$(this).remove();
				});
			 }, 5000);
	   }else if(sub==""){
		   $('#channelPartnerForm').before('<span class="error" style="color: red;">Please enter a subject.</span>');
		   $("#contactSubjectCP").focus();
			setTimeout(function(){
				$('.error').fadeOut('normal', function() {
					$(this).remove();
				});
			 }, 5000);
	   }else if(com==""){
		   $('#channelPartnerForm').before('<span class="error" style="color: red;">Please enter the message.</span>');
		   $("#commentsCP").focus();
			setTimeout(function(){
				$('.error').fadeOut('normal', function() {
					$(this).remove();
				});
			 }, 5000);
	   }else{
		   var json = {};
		   json.email = email;
		   json.sub=sub;
		   json.msg=com;
		   json.name=name;
		   json.phone=$('#phoneCP').val();
		   var url="/index/channelPartner";
		   $.ajax({
			 url : url,
			 data : JSON.stringify(json),
			 type : "text",
			 method : "POST",
			 contentType : 'application/json',
			 success : function(data) {
				 var style=' style="color: white;"';
				 if (!data.result) {
					 style=' style="color: red;"';
				 }
				 $('#channelPartnerForm').before('<span class="error"' + style + '>' + data.message + '</span>');
				 setTimeout(function(){
					$('.error').fadeOut('normal', function() {
						$(this).remove();
					});
				 }, 5000);
			 },
			 error : function(xhr, status, error) {
			 },
			 complete : function(){
				 $('#channelPartnerForm').find('input, textarea').val('');
			 }
		   });
	   }
	   return false;
   });
});