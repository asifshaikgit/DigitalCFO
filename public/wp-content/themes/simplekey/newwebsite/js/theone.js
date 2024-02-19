var TheOneClass = function() {
    "use strict";
    var FS = {"animation":"slide","pauseOnHover":"true","controlNav":"false","directionNav":"true","animationDuration":"500","slideshowSpeed":"5000","pauseOnAction":"false","nextText" : '<i class="icon-chevron-right"></i>',"prevText" : '<i class="icon-chevron-left"></i>'};
    var self = this;

    this.menuPrimary = function() {
        //ddsmoothmenu for primary navigation
        ddsmoothmenu.init({
            mainmenuid: "primary-nav-id", //menu DIV id
            orientation: 'h', //Horizontal or vertical menu: Set to "h" or "v"
            classname: 'primary-nav slideMenu', //class added to menu's outer DIV
            contentsource: "markup" //"markup" or ["container_id", "path_to_menu_file"]
        });

        // Primary Navigation for mobile.
        var primary_nav_mobile_button = jQuery('#primary-nav-mobile');
        var primary_nav_cloned;
        var primary_nav = jQuery('#primary-nav-id > ul');

        primary_nav.clone().attr('id','primary-nav-mobile-id').removeClass().appendTo( primary_nav_mobile_button );
        primary_nav_cloned = primary_nav_mobile_button.find('> ul');

        jQuery('#primary-nav-mobile-a').click(function() {

            var mwidth =  jQuery(window).width();
            if(mwidth>340){
                mwidth = 340;
            }else{
                mwidth = mwidth - 60;
            }

            if (jQuery(this).hasClass('primary-nav-close')) {
                jQuery(this).removeClass('primary-nav-close').addClass('primary-nav-opened');
                //primary_nav_cloned.slideDown(400);
/*
                jQuery('#mobile-menu-wrapper').css({ 'width':  +mwidth+'px', left:'-'+mwidth+'px'  });
                jQuery('body').css({'overflow':'hidden'});
                jQuery('.theone-wrapper').css({'position': 'absolute'});

                jQuery('#mobile-menu-wrapper').animate({'left': '0px'},{
                    queue: false, duration: 400 ,
                    complete: function(){}
                });

                jQuery('.theone-wrapper').animate({'left': mwidth+'px'},{
                    queue: false, duration: 400 ,
                    complete: function(){}
                });
*/

            } else {
                jQuery(this).removeClass('primary-nav-opened').addClass('primary-nav-close');
                //primary_nav_cloned.slideUp(400);
/*
                jQuery('#mobile-menu-wrapper').animate({'left': '-'+mwidth+'px'},{
                    queue: false, duration: 400 ,
                    complete: function(){
                        jQuery('#mobile-menu-wrapper').css({ 'width':  '0px', left:'0px'  });
                        jQuery('body').css({'overflow':''});
                        jQuery('.theone-wrapper').css({'position': ''});
                    }
                });

                jQuery('.theone-wrapper').animate({'left': '0px'},{
                    queue: false, duration: 400,
                    complete: function(){
                    }
                });
*/
            }

            return false;
        });

        primary_nav_mobile_button.find('a').click(function(event){
            event.stopPropagation();
        });
    };



    this.scrollPrimaryItem = function() {
        /*Smooth Scroll to section*/
        var h = jQuery('#wpadminbar').height() + jQuery('#header-sticky-wrapper').height();
        jQuery.localScroll({
            target:'html, body',
            duration:1000,
            offset: {left: 0, top: - h}
        });

        jQuery(window).scroll(function() {

            var currentNode = null;
            jQuery('.page-area').each(function(){
                var currentId = jQuery(this).attr('id');

                if(jQuery('#'+currentId).length>0 ) {
                    if(jQuery(window).scrollTop() >= jQuery('#'+currentId).offset().top - h-10) {
                        currentNode = currentId;
                    }
                }

            });
            //console.log('#'+currentNode);
            jQuery('#primary-nav-inner li').removeClass('st-current-item').find('a[href$="#'+currentNode+'"]').parent().addClass('st-current-item');
        });

        jQuery(window).load(function(){
            var urlCurrent = location.hash;

            if (jQuery(urlCurrent).length>0 ) {
                smoothScroll(urlCurrent);
            }
        });

        function smoothScroll(urlhash) {
            jQuery("html, body").animate({
                scrollTop: (jQuery(urlhash).offset().top - h) + "px"
            }, {
                duration: 1800,
                easing: "swing"
            });
            return false;
        }

        // other scroll to
        jQuery('.local-scroll').live('click',function(){
            var url =  jQuery(this).attr('href');
            if(url.indexOf('://')<0 && url.indexOf('#')>=0){ // is
                smoothScroll(url);
            }
        });


        jQuery('#primary-nav-mobile-id li a').click(function(event){
            event.preventDefault();

            jQuery(this).parents('#primary-nav-mobile-a').removeClass('primary-nav-opened').addClass('primary-nav-close');


            var full_url = this.href,
                parts = full_url.split("#");

            if (parts.length >=2 && parts[1] != undefined) {
                var trgt = parts[1];
                if (jQuery("#" + trgt).length > 0) {
                    var goPosition = jQuery("#" + trgt).offset().top - h;

                    jQuery("html, body").animate({
                        scrollTop: goPosition
                    }, {
                        duration: 1800,
                        easing: "swing"
                    });
                    return false;
                }

                window.location.replace(full_url);

            }
            else {
                window.location.replace(full_url);
            }
        });
    };

    this.fullScreenSlider = function() {
        /* Full Screen slider =========================*/
        if ( typeof (window.full_slider_data) !== 'undefined' && typeof (jQuery.supersized) !== 'undefined') {

            jQuery('#supersized-loader').append('<div class="loading-icon"></div>');
            jQuery('#supersized-loader').show();

            self.loadingIcon('#supersized-loader .loading-icon');
            jQuery.supersized.themeVars.image_path = window.full_slider_settings.path;

            jQuery.supersized({
                //Functionality
                slideshow : window.full_slider_settings.slideshow, //Slideshow on/off
                autoplay : window.full_slider_settings.autoplay, //Slideshow starts playing automatically
                start_slide : 1, //Start slide (0 is random)
                random : 0, //Randomize slide order (Ignores start slide)
                slide_interval : window.full_slider_settings.slide_interval, //Length between transitions
                transition : 1, //0-None, 1-Fade, 2-Slide Top, 3-Slide Right, 4-Slide Bottom, 5-Slide Left, 6-Carousel Right, 7-Carousel Left
                transition_speed : window.full_slider_settings.transition_speed, //Speed of transition
                new_window : 1, //Image links open in new window/tab
                pause_hover : 0, //Pause slideshow on hover
                keyboard_nav : 1, //Keyboard navigation on/off
                performance : 1, //0-Normal, 1-Hybrid speed/quality, 2-Optimizes image quality, 3-Optimizes transition speed // (Only works for Firefox/IE, not Webkit)
                image_protect : 1, //Disables image dragging and right click with Javascript
                image_path : window.full_slider_settings.path, //Default image path

                //Size & Position
                min_width : 0, //Min width allowed (in pixels)
                min_height : 400, //Min height allowed (in pixels)
                vertical_center : 0, //Vertically center background
                horizontal_center : 1, //Horizontally center background
                fit_always: 0, //// Image will never exceed browser width or height (Ignores min. dimensions)
                fit_portrait : 0, //Portrait images will not exceed browser height
                fit_landscape : 0, //Landscape images will not exceed browser width

                //Components
                navigation : 1, //Slideshow controls on/off
                thumbnail_navigation : 0, //Thumbnail navigation
                slide_counter : 1, //Display slide numbers
                slide_captions : 1, //Slide caption (Pull from "title" in slides array)
                slides : window.full_slider_data

            });

        } else {// end check slider data
            jQuery('#supersized, #supersized-loader, .supersized-action').css({
                'display' : 'none'
            });
        }
    };

    // create loading icon with js effect
    this.loadingIcon = function(selector){
        var ld;

        if(typeof(selector)==='string'){
            ld = jQuery(selector);
        }else{
            ld = selector;
        }

        if(typeof(ld)==='undefined'){
            return;
        }

        var playAnimate;

        var loadingAnimate = {} ;

        if(ld.find(".shape1").length<=0){
            ld.append(jQuery('<div class="shape1 shape"></div>'));
        }
        if(ld.find(".shape2").length<=0){
            ld.append(jQuery('<div class="shape2 shape"></div>'));
        }

        loadingAnimate.shape1 =  jQuery(".shape1",ld);
        loadingAnimate.shape2 =  jQuery(".shape2",ld);


        var wwidth = ld.width();
        var bluewidth = loadingAnimate.shape1.width();

        loadingAnimate.shape1.css("left", (wwidth/2) - bluewidth);

        var bluepos =  loadingAnimate.shape1.position();

        var movex = loadingAnimate.shape1.width() + 4;
        loadingAnimate.shape2.css("left", bluepos.left + movex);


        loadingAnimate.moveleft = function($el){
            $el.animate({
                left: '+='+movex
            }, 800, function() {
                $el.css("z-index", "998");
            });
        };

        loadingAnimate.moveright = function($el){
            $el.animate({
                left: '-='+movex
            }, 800, function() {
                $el.css("z-index", "999");
            });
        };

        loadingAnimate.playAnimation = function(){
            loadingAnimate.moveleft(loadingAnimate.shape1);
            loadingAnimate.moveright(loadingAnimate.shape1);
            loadingAnimate.moveright(loadingAnimate.shape2);
            loadingAnimate.moveleft(loadingAnimate.shape2);
        };

        loadingAnimate.playAnimation();
        playAnimate = setInterval(loadingAnimate.playAnimation, 800);


    };

    this.stParallax = function() {
        var isMobile = {
            Android: function() {
                return navigator.userAgent.match(/Android/i);
            },
            BlackBerry: function() {
                return navigator.userAgent.match(/BlackBerry/i);
            },
            iOS: function() {
                return navigator.userAgent.match(/iPhone|iPad|iPod/i);
            },
            Opera: function() {
                return navigator.userAgent.match(/Opera Mini/i);
            },
            Windows: function() {
                return navigator.userAgent.match(/IEMobile/i);
            },
            any: function() {
                return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS() || isMobile.Opera() || isMobile.Windows());
            }
        };

        var testMobile = isMobile.any();

        // alert(testMobile);

        jQuery('.parallax').each(function() {
            var $this = jQuery(this);
            var bg = $this.find('.separator-bg');
            
            jQuery(bg).css('backgroundImage', 'url(' + $this.data('bg') + ')');

            if (testMobile == null) {
                jQuery(bg).addClass('not-mobile');
                jQuery(bg).removeClass('is-mobile');
                jQuery(bg).parallax('50%', 0.4);
            }
            else {
                //jQuery(bg).css('backgroundAttachment', 'inherit');
                jQuery(bg).removeClass('not-mobile');
                jQuery(bg).addClass('is-mobile');

            }

        });

    };

    this.portFolioFilter = function () {
        jQuery(window).bind("hashchange", function(){
            var url = jQuery.url(); //parse the current Pgae Url
            var hashOptions = url.fparam('filter');

            if (typeof(hashOptions) !== 'undefined') {
                jQuery(".cpt-filters a").removeClass("selected");
                if(jQuery(".cpt-filters a[href='#filter="+hashOptions+"']").length){
                    jQuery(".cpt-filters a[href='#filter="+hashOptions+"']").addClass("selected");
                }
                else{
                    jQuery(".cpt-filters li:first a").addClass("selected");
                }

                jQuery(".isotope").isotope({filter: hashOptions});
            }
        }).trigger("hashchange");
    };

    this.contactForm = function() {
        var error_report;
        jQuery("#contact_submit").bind("click",function(){
            // Hide notice message when submit
            jQuery("#contact_form .notice_ok").hide();
            jQuery("#contact_form .notice_error").hide();
            error_report = false;

            jQuery("#contact_form input, #contact_form textarea").each(function(i){

                var form_element          = jQuery(this);
                var form_element_value    = jQuery(this).attr("value");
                var form_element_id       = jQuery(this).attr("id");
                //var form_element_class    = jQuery(this).attr("class");
                var form_element_required = jQuery(this).hasClass("required");

                // Check email validation
                if(form_element_id === "contact_email"){
                    form_element.removeClass("error valid");
                    if(!form_element_value.match(/^\w[\w|\.|\-]+@\w[\w|\.|\-]+\.[a-zA-Z]{2,4}$/)){
                        form_element.addClass("error");
                        error_report = true;
                    } else {
                        form_element.addClass("valid");
                    }
                }

                // Check input required validation
                if(form_element_required && (form_element_id !== "contact_email" || form_element_id !== "contact_message")){
                    form_element.removeClass("error valid");
                    if(form_element_value === ""){
                        form_element.addClass("error");
                        error_report = true;
                    } else {
                        form_element.addClass("valid");
                    }
                }

                if(jQuery("#contact_form input, #contact_form textarea").length === i+1){
                    if(error_report === false){
                        jQuery("#contact_form .loading").show();

                        var $string = "ajax=true";
                        jQuery("#contact_form input, #contact_form textarea").each(function(){
                            var $form_element_name     = jQuery(this).attr("name");
                            var $form_element_value    = encodeURIComponent(jQuery(this).attr("value"));
                            $string = $string + "&" + $form_element_name + "=" + $form_element_value;
                        });

                        jQuery.ajax({
                            type: "POST",
                            url: "./page-contact-ajax.php",
                            data:$string,
                            success: function(response){
                                jQuery("#contact_form .loading").hide();
                                if(response === 'success'){
                                    jQuery("#contact_form .notice_ok").show();
                                    jQuery("#contact_form .field_submit").hide();
                                } else {
                                    jQuery("#contact_form .notice_error").show();
                                    jQuery("#contact_form .field_submit").hide();
                                }
                            }
                        });
                    }
                }


            });

            return false;
        });
    };

    this.gallerLightbox = function(parent) {
        if ( typeof (parent) !== 'undefined') {
            jQuery('.st-lightbox', parent).magnificPopup({
                type : 'image',
                gallery : {
                    enabled : true
                }
            });
        } else {
            jQuery('.st-lightbox').magnificPopup({
                type : 'image',
                gallery : {
                    enabled : true
                }
            });
        }
    };

    this.imageLightbox = function() {
        jQuery('.st-image-lightbox').magnificPopup({
            type : 'image',
            closeOnContentClick: true,
            image: {
                verticalFit: true
            }
        });
    };

    this.stShareThis = function() {
        // share tooltip
        jQuery('.entry-share').click(function(){
            var $this = jQuery(this);
            var tw= $this.outerWidth(true);
            var th = $this.outerHeight(true);

            var tooltip =  jQuery('.tooltip',$this);
            var w =  tooltip.outerWidth(true);

            tooltip.css({'bottom': (th+20)+'px', 'left': -(w/2 - tw/2) +'px' }).fadeIn(300);
            return false;
        });

        /* When click our side of share tooltip */
        jQuery(document).click(function(e) {
            var container = jQuery(".entry-share");
            if (container.has(e.target).length === 0) {
                jQuery(".entry-share .tooltip").fadeOut(300);
            }
        });
    };

    this.fullWidthStyle = function() {
        jQuery('.servcices-fp .four').each(function(){
            var w = jQuery(this).width();
            var itemW = jQuery(this).find('.icon').width();
            var mg = (w-itemW)/2;
            jQuery(this).find('.icon').css({'margin': '15px '+ mg + 'px'});

        });
    };

    //Call all function in one
    this.init = function() {
        var nice = jQuery("html").niceScroll({
            cursorcolor:"#424242",
            scrollspeed:70,
            horizrailenabled:false,
            autohidemode:true ,
            cursorwidth: 4,
            cursorminheight: 32,
            cursorborder: '1px solid #424242',
            cursorborderradius: '5px 5px 5px 5px'
        });

        if (jQuery('#wpadminbar').length>0) {
            jQuery('#header-sticky-wrapper').css('top', jQuery('#wpadminbar').height());
            jQuery(window).resize(function(){
                jQuery('#header-sticky-wrapper').css('top', jQuery('#wpadminbar').height());
            });
        }
        if (typeof (window.full_video_data) !== 'undefined') {

            if (jQuery('#video-full-cover-loading .loading-icon').length>0) {
                this.loadingIcon('#video-full-cover-loading .loading-icon');
            }
        }


        this.fullScreenSlider();
        this.menuPrimary();
        this.portFolioFilter();
        this.stShareThis();
        this.contactForm();
        this.scrollPrimaryItem();


        //this.stParallax();
        setTimeout(function(){
            self.stParallax();
        },100);

        jQuery(window).resize(function(){
            self.stParallax();
        });


        this.contactForm();
        this.fullWidthStyle();

        // Fitvideos
        jQuery(".page-wrapper").fitVids();

        // FlexSlider
        jQuery('.flexslider').each(function(){
            jQuery(this).flexslider(FS);
        });
        this.gallerLightbox('ul.slides');
        this.imageLightbox('.st-image-lightbox');

    };
}; //End Class TheOne

jQuery(document).ready(function() {
    "use strict";
    var theme = new TheOneClass();
    theme.init();
});
