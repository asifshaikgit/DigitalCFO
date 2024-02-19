jQuery.noConflict();
jQuery(document).ready(function(){
    // Shortcodes

        // Alert 
        jQuery('.close').click(function(){
            jQuery(this).parent().fadeOut("slow");
        });

        // ST Tabs 
        var st_tab = jQuery('.st-tabs');
        st_tab.find('ul li').click(function(){
            if(jQuery(this).hasClass('current')) return;
            var tab_id = jQuery(this).attr('tab-id');
            //alert(tab_id);
            var tab_title = jQuery(this).parents('ul.tab-title');
            var tab_content = tab_title.siblings('.tab-content-wrapper');

            tab_title .find('li.current').removeClass('current');
            jQuery(this).addClass('current');
            tab_content.find('div.active').removeClass('active').css('display','none');
            tab_content.find('[tab-id="' + tab_id + '"]').fadeIn().addClass('active');
            tab_content.find('#' + tab_id ).fadeIn().addClass('active');
        });

        st_tab.find('ul li').eq(0).click();

        // Accordion
        jQuery('.acc-title').toggleClass('acc-title-inactive');

        //Open accordion by default by class "acc-opened".
        jQuery('.acc-opened .acc-title').toggleClass('acc-title-active').toggleClass('acc-title-inactive');
        jQuery('.acc-opened .acc-content').slideDown().toggleClass('open-content');

        jQuery('.acc-title').click(function(){
            if(jQuery(this).is('.acc-title-inactive')){
                jQuery('.acc-title-active').toggleClass('acc-title-active').toggleClass('acc-title-inactive').next().slideToggle().toggleClass('open-content');
                jQuery(this).toggleClass('acc-title-active').toggleClass('acc-title-inactive');
                jQuery(this).next().slideToggle().toggleClass('open-content');
            } else {
                jQuery(this).toggleClass('acc-title-active').toggleClass('acc-title-inactive');
                jQuery(this).next().slideToggle().toggleClass('open-content');
            }
        });

        // Toggle
        jQuery('.toggle-title').click(function(){
            var toggle_tab = jQuery(this).parent();
            toggle_tab.find('> :last-child').stop(true, true).slideToggle();
            if (jQuery(this).hasClass('toggle_current'))
            {
                jQuery(this).removeClass('toggle_current');
            }
            else
            {
                jQuery(this).addClass('toggle_current');
            }
        });

    
}); // End of Jquery DOM ready

