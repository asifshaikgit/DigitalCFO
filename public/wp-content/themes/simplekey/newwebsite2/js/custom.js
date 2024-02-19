$(window).scroll(function() {
  if ($(document).scrollTop() > 50) {
    $('.navbar-nav').addClass("scroll_nav");
  } else {
    $('.navbar-nav').removeClass("scroll_nav");
  }
});

$(document).ready(function() {

  $("#main_slider").owlCarousel({
  autoPlay: 3000,
  items : 1,
  itemsDesktop : [1199,1],
  itemsDesktopSmall : [979,1],
  itemsTablet: [768,1],
  itemsMobile : [479,1],
  singleItem : false,
  pagination : false,
  navigation:true,
  navigationText: [
      "<img src='../assets/wp-content/themes/simplekey/newwebsite2/images/arrow_l.png'>",
      "<img src='../assets/wp-content/themes/simplekey/newwebsite2/images/arrow_r.png'>"
      ],
  });

});
$(document).ready(function() {

  $("#client_slider").owlCarousel({
  autoPlay: 3000,
  items : 3,
  itemsDesktop : [1199,3],
  itemsDesktopSmall : [979,2],
  itemsTablet: [768,2],
  itemsMobile : [479,1],
  singleItem : false,
  pagination : false,
  navigation:true,
  navigationText: [
       "<img src='../assets/wp-content/themes/simplekey/newwebsite2/images/arrow_l.png'>",
      "<img src='../assets/wp-content/themes/simplekey/newwebsite2/images/arrow_r.png'>"
      ],
  });

});

