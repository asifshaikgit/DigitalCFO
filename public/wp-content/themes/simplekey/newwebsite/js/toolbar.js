if(window.top != window.self) {
    window.open(window.self.location.href, '_top');
}


function tb_setCookie(c_name,value,exdays)
{
    var exdate=new Date();
    exdate.setDate(exdate.getDate() + exdays);
    var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
    document.cookie=c_name + "=" + c_value;
}

function tb_getCookie(c_name)
{
    var c_value = document.cookie;
    var c_start = c_value.indexOf(" " + c_name + "=");
    if (c_start == -1)
      {
      c_start = c_value.indexOf(c_name + "=");
      }
    if (c_start == -1)
      {
      c_value = null;
      }
    else
      {
      c_start = c_value.indexOf("=", c_start) + 1;
      var c_end = c_value.indexOf(";", c_start);
      if (c_end == -1)
      {
    c_end = c_value.length;
    }
    c_value = unescape(c_value.substring(c_start,c_end));
    }
    return c_value;
}


jQuery(document).ready(function(){
    
     var is_admin_bar = jQuery('body').hasClass('admin-bar') ?  true : false;
     jQuery('body').attr('default-mgt',jQuery('body').css('margin-top'));         
    
    /* WWhen click our side of tool bar*/
     jQuery(document).click(function (e)
        {
            var container = jQuery(".st-toolbar");
            var that  = jQuery(this).find('.tb-settings-btn');
            if (container.has(e.target).length === 0)
            {
               // container.hide();
               
               
               jQuery('.st-toolbar .tb-dropdow .tb-dropdow-items').slideUp();
        
                jQuery('body').animate({marginTop: jQuery('body').attr('default-mgt')}, 200);
                 if(is_admin_bar){
                     jQuery('.admin-bar .st-toolbar').animate({top:'-42px'}, 200);
                 }else{
                     jQuery('.st-toolbar').animate({top:'-88px'}, 200);
                 }
        
                that.removeClass('is_open');
               
               
            }
        });
    
     // hide toolbar when load
     jQuery('.tb-settings-btn').click(function(){
         var that = jQuery(this);
         
        
         
         if(that.hasClass('is_open')){
            // hide all dropdown
            jQuery('.st-toolbar .tb-dropdow .tb-dropdow-items').slideUp();
            
            jQuery('body').animate({marginTop:  jQuery('body').attr('default-mgt') }, 200);
             if(is_admin_bar){
                 jQuery('.admin-bar .st-toolbar').animate({top:'-62px'}, 200);
             }else{
                 jQuery('.st-toolbar').animate({top:'-88px'}, 200);
             }
    
            that.removeClass('is_open');
            
         }else{
            //jQuery('body').animate({marginTop:'88px'}, 200);
             if(is_admin_bar){
                 jQuery('.admin-bar .st-toolbar').animate({top:'28px'}, 200);
             }else{
                 jQuery('.st-toolbar').animate({top:'0px'}, 200);
             }
             that.addClass('is_open');
         }
        
     });
    

     jQuery( '.tb-theme-list' ).tooltip({
        items: ".tb-t-item",
       // track: true,
        position: {
                my: "left+20",
                at: "right-5",
                collision: "fit",
                using: function( position, feedback ) {
                    jQuery( this ).css( position );
                    jQuery( "<div>" )
                        .addClass( "tb-tooltip-content" )
                        .addClass( feedback.vertical )
                        .addClass( feedback.horizontal )
                        .appendTo( this );
                }
            },
         content: function() {
            var element = jQuery( this );
                 var text = element.find('.tb-tooltip').html();
                return text;
            }
        });
     // hover select themes
     jQuery('.st-toolbar .tb-dropdow .tb-current-item').click(function(){
          var  p = jQuery(this).parents('.tb-dropdow');
          jQuery('.st-toolbar .tb-dropdow').not(p).each(function(){ // hide other dropdown
                jQuery(this).find('.tb-dropdow-items').slideUp();
          });
          jQuery('.tb-dropdow-items',p).slideToggle(); 
          return false;
     }) ;
     
     
     // when click to change style
     jQuery('.st-toolbar .tb-styles .tb-dropdow-items .css').click(function(){
        if(jQuery('#st_toolbar_switcher').length){
              var css_link = jQuery(this).find('.tb-s-n').attr('link');
              var text = jQuery(this).find('.tb-s-n').text();
              jQuery(this).parents('.tb-styles').find('.tb-current-item .tb-t-n').text(text);
              jQuery('#st_toolbar_switcher').attr('href',css_link);
              tb_setCookie('st_toolbar_c_style',jQuery(this).find('.tb-s-n').attr('key'),1);   
        } 
     }) ;
     
     
     //when page load 
     var current_style = tb_getCookie('st_toolbar_c_style');
     if(typeof(current_style)!='undefined'){
         if(jQuery('.st-toolbar .tb-styles .tb-dropdow-items .'+current_style).length){
                var t = jQuery('.st-toolbar .tb-styles .tb-dropdow-items .'+current_style);
                var text = jQuery('.tb-s-n',t).text();
                var  css_link =  jQuery('.tb-s-n',t).attr('link');
                jQuery('.st-toolbar .tb-styles .tb-current-item .tb-t-n').text(text);
               jQuery('#st_toolbar_switcher').attr('href',css_link);
         }
     }
     

        
    
});