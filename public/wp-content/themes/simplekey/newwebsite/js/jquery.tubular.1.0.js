/* jQuery tubular plugin
* by Sean McCambridge
* http://www.seanmccambridge.com/tubular
* version: 1.0
* updated: October 1, 2012
* since 2010
* licensed under the MIT License
* Enjoy.
* 
* Thanks*/


var yttag = document.createElement('script');
yttag.src = "//www.youtube.com/iframe_api";
var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(yttag, firstScriptTag);

window.player;

function onYouTubeIframeAPIReady(){
   // console.debug('ready');
    jQuery(document).ready(function(){
       //  console.debug('ready');
       // jQuery(document).trigger('ytready');
        jQuery('#video-full-width-slider').tubular({videoId: window.full_video_data});
    });
}



;(function ($, window) {
    var defaults = {
        ratio: 16/9, 
        videoId: 'Yk9n48ZB8XQ',
        mute: false,
        repeat: true,
        width: $(window).width(),
        wrapperZIndex: 10,
        playButtonClass: 'tubular-play',
        pauseButtonClass: 'tubular-pause',
        muteButtonClass: 'tubular-mute',
        volumeUpClass: 'tubular-volume-up',
        volumeDownClass: 'tubular-volume-down',
        increaseVolumeBy: 10,
        start: 0
    };

    var tubular = function(node, options) { 
        var options = $.extend({}, defaults, options),
            $body = $('#video-full-width-slider'),
            $node = $(node);



        $('html,body').css({'width': '100%', 'height': '100%'});
        var h = $(window).height() ,
            hHeader = $('#header-sticky-wrapper').height();

        $('#video-full-width-slider, .video-element-wrapper').css({'height': h - hHeader});
        $('#video-full-width-slider .video-inner').hide();
        $node.css({position: 'relative', 'z-index': options.wrapperZIndex});

        function resize ()  {
            var width = $(window).width(),
                pWidth,
                height = $(window).height(),
                hHeader = $('#header-sticky-wrapper').height(),
                pHeight,
                $tubularPlayer = $('#tubular-player');

            $('#video-full-width-slider, .video-element-wrapper').css({'height': height - hHeader});

            if (width / options.ratio < height) {
                pWidth = Math.ceil(height * options.ratio);
                $tubularPlayer.width(pWidth).height(height).css({left: (width - pWidth) / 2, top: 0});
            } else {
                pHeight = Math.ceil(width / options.ratio);
                $tubularPlayer.width(width).height(pHeight).css({left: 0, top: (height - pHeight) / 2});
            }
        }

        var is_mobile=  /Android|webOS|iPhone|iPad|iPod|BlackBerry/i.test(navigator.userAgent);

        if(is_mobile  ){

            $('.video-fullwidth-content').css({
                'background': 'url(http://img.youtube.com/vi/'+options.videoId+'/maxresdefault.jpg) center center',
                'backgroundSize' : 'cover'
            });
            $('#video-full-width-slider .video-inner').show();
            $('#video-full-cover-loading, .video-full-controls').hide();

            return true;
        }




        var tubularContainer ='';
        if($('#tubular-container').length<=0){
             tubularContainer = '<div id="tubular-container" style="opacity:0;overflow: hidden; position: relative; z-index: 1; width: 100%; height: 100%"><div id="tubular-player" style="position: absolute"></div><div id="tubular-shield" style="width: 100%; height: 100%; z-index: 2; position: absolute; left: 0; top: 0;"></div>';
             $body.prepend(tubularContainer);
        }



        //window.player;
       // document.onYouTubeIframeAPIReady = function() {

        player = new YT.Player('tubular-player', {
            width: options.width,
            height: Math.ceil(options.width / options.ratio),
            videoId: options.videoId,
            playerVars: {
                controls: 0,
                showinfo: 0,
                modestbranding: 1,
                wmode: 'transparent'
            },
            events: {
                'onReady': function(e){


                    if (options.mute) e.target.mute();
                    e.target.seekTo(options.start);

                    player.playVideo();

                    if(player.getPlayerState() == 1){
                        $('#video-full-cover-loading').hide();
                        $('#video-full-width-slider .video-inner').show();
                        $('#video-full-cover-loading .loading-icon').remove();
                    }

                    resize();

                },
                'onStateChange': function(state){
                    if(state.data === 1){
                        $('#tubular-container,.control-handle').css({"opacity" : "1"});
                        $('#video-full-cover-loading').hide();
                        $('#video-full-width-slider .video-inner').show();
                        $('#video-full-cover-loading .loading-icon').remove();
                    }
                    if (state.data === 0 && options.repeat) {
                        player.seekTo(options.start);
                    }
                }
            }
        });

       // };



        function updateActive(clicked){

            $('.video-header-controls li').each(function(){

                if(!$(this).hasClass(options.muteButtonClass)){
                    $(this).removeClass('active');
                }
            });

             $('li.'+clicked).addClass('active');

         }

        $(window).on('resize.tubular', function() {
            resize();
        })

        $('body').on('click','.' + options.playButtonClass, function(e) {
            e.preventDefault();
            updateActive(options.playButtonClass);
            player.playVideo();
        }).on('click', '.' + options.pauseButtonClass, function(e) { 
            e.preventDefault();
            updateActive(options.pauseButtonClass);
                player.pauseVideo();
        }).on('click', '.' + options.muteButtonClass, function(e) {
            e.preventDefault();

            if(player.isMuted()){

                player.unMute();

                $('li.'+options.muteButtonClass).removeClass('active');

            }else{

                player.mute();

                $('li.'+options.muteButtonClass).addClass('active');

            }



        }).on('click', '.' + options.volumeDownClass, function(e) {
            e.preventDefault();
            var currentVolume = player.getVolume();
            if (currentVolume < options.increaseVolumeBy) currentVolume = options.increaseVolumeBy;
                player.setVolume(currentVolume - options.increaseVolumeBy);
        }).on('click', '.' + options.volumeUpClass, function(e) {
            e.preventDefault();
            if (player.isMuted()){
                player.unMute();
                if($('li.'+options.muteButtonClass).hasClass('active')){
                    $('li.'+options.muteButtonClass).removeClass('active')
                }
            }
            var currentVolume = player.getVolume();
            if (currentVolume > 100 - options.increaseVolumeBy) currentVolume = 100 - options.increaseVolumeBy;
                player.setVolume(currentVolume + options.increaseVolumeBy);
        })
    }


    $.fn.tubular = function (options) {
        return this.each(function () {
            if (!$.data(this, 'tubular_instantiated')) {
                $.data(this, 'tubular_instantiated', 
                tubular(this, options));
            }
        });
    }

})(jQuery, window);


