/**
 *
 */
var onlineUsers=false,historyMonth=0;
var scroll={
	_settings:{
		horizontalGutter: 30,
		verticalGutter: -16,
//		arrowScrollOnHover: true,
		animateScroll: true,
		enableKeyboardNavigation: true,
		hideFocus: true,
		contentWidth: '0px'
	},
	getJscrollpaneApiId:function(id){
		return this.getJscrollpaneApiEle($('#'+id));
	},
	getJscrollpaneApiClass:function(clazz){
		return this.getJscrollpaneApiEle($('.'+clazz));
	},
	getJscrollpaneApiEle:function(ele){
		var $el=$(ele).jScrollPane(this._settings);
		return $el.data('jsp');
	},
	initializeId:function(id){
		this.initializeEle($('#'+id));
	},
	initializeClass:function(clazz){
		this.initializeEle($('.'+clazz));
	},
	initializeEle:function(ele){
		var $el=$(ele).jScrollPane(this._settings);
		var api=$el.data('jsp');
		if(!isEmpty(api)){api.reinitialise(this._settings);}
		// the extension functions and options
		extensionPlugin={
			extPluginOpts:{
				// speed for the fadeOut animation
				mouseLeaveFadeSpeed	: 500,
				// scrollbar fades out after hovertimeout_t milliseconds
				hovertimeout_t		: 1000,
				// if set to false, the scrollbar will be shown on mouseenter and hidden on mouseleave
				// if set to true, the same will happen, but the scrollbar will be also hidden on mouseenter after "hovertimeout_t" ms
				// also, it will be shown when we start to scroll and hidden when stopping
				useTimeout			: true,
				// the extension only applies for devices with width > deviceWidth
				deviceWidth			: 980
			},
			hovertimeout	: null, // timeout to hide the scrollbar
			isScrollbarHover: false,// true if the mouse is over the scrollbar
			elementtimeout	: null,	// avoids showing the scrollbar when moving from inside the element to outside, passing over the scrollbar
			isScrolling		: false,// true if scrolling
			addHoverFunc	: function() {
				// run only if the window has a width bigger than deviceWidth
				if( $(window).width() <= this.extPluginOpts.deviceWidth ) return false;
				var instance= this;
				// functions to show / hide the scrollbar
				$.fn.jspmouseenter 	= $.fn.show;
				$.fn.jspmouseleave 	= $.fn.fadeOut;
				// hide the jScrollPane vertical bar
				var $vBar= this.getContentPane().siblings('.jspVerticalBar').hide();
				/*
				 * mouseenter / mouseleave events on the main element
				 * also scrollstart / scrollstop - @James Padolsey : http://james.padolsey.com/javascript/special-scroll-events-for-jquery/
				 */
				$el.bind('mouseenter.jsp',function() {
					// show the scrollbar
					$vBar.stop( true, true ).jspmouseenter();
					if( !instance.extPluginOpts.useTimeout ) return false;
					// hide the scrollbar after hovertimeout_t ms
					clearTimeout( instance.hovertimeout );
					instance.hovertimeout 	= setTimeout(function() {
						// if scrolling at the moment don't hide it
						if( !instance.isScrolling )
							$vBar.stop( true, true ).jspmouseleave( instance.extPluginOpts.mouseLeaveFadeSpeed || 0 );
					}, instance.extPluginOpts.hovertimeout_t );
				}).bind('mouseleave.jsp',function() {
					// hide the scrollbar
					if( !instance.extPluginOpts.useTimeout )
						$vBar.stop( true, true ).jspmouseleave( instance.extPluginOpts.mouseLeaveFadeSpeed || 0 );
					else {
					clearTimeout( instance.elementtimeout );
					if( !instance.isScrolling )
							$vBar.stop( true, true ).jspmouseleave( instance.extPluginOpts.mouseLeaveFadeSpeed || 0 );
					}
				});
				if( this.extPluginOpts.useTimeout ) {
					$el.bind('scrollstart.jsp', function() {
						// when scrolling show the scrollbar
					clearTimeout( instance.hovertimeout );
					instance.isScrolling	= true;
					$vBar.stop( true, true ).jspmouseenter();
				}).bind('scrollstop.jsp', function() {
						// when stop scrolling hide the scrollbar (if not hovering it at the moment)
					clearTimeout( instance.hovertimeout );
					instance.isScrolling	= false;
					instance.hovertimeout 	= setTimeout(function() {
						if( !instance.isScrollbarHover )
								$vBar.stop( true, true ).jspmouseleave( instance.extPluginOpts.mouseLeaveFadeSpeed || 0 );
						}, instance.extPluginOpts.hovertimeout_t );
				});
				// wrap the scrollbar
				// we need this to be able to add the mouseenter / mouseleave events to the scrollbar
				var $vBarWrapper	= $('<div/>').css({
					position	: 'absolute',
					left		: $vBar.css('left'),
					top			: $vBar.css('top'),
					right		: $vBar.css('right'),
					bottom		: $vBar.css('bottom'),
					width		: $vBar.width(),
					height		: $vBar.height()
				}).bind('mouseenter.jsp',function() {
					clearTimeout( instance.hovertimeout );
					clearTimeout( instance.elementtimeout );
					instance.isScrollbarHover	= true;
						// show the scrollbar after 100 ms.
						// avoids showing the scrollbar when moving from inside the element to outside, passing over the scrollbar
					instance.elementtimeout	= setTimeout(function() {
						$vBar.stop( true, true ).jspmouseenter();
					}, 100 );

				}).bind('mouseleave.jsp',function() {
						// hide the scrollbar after hovertimeout_t
					clearTimeout( instance.hovertimeout );
					instance.isScrollbarHover	= false;
					instance.hovertimeout = setTimeout(function() {
							// if scrolling at the moment don't hide it
						if( !instance.isScrolling )
								$vBar.stop( true, true ).jspmouseleave( instance.extPluginOpts.mouseLeaveFadeSpeed || 0 );
						}, instance.extPluginOpts.hovertimeout_t );
				});
				$vBar.wrap( $vBarWrapper );
			}
			}
		},
		// extend the jScollPane by merging
		$.extend(true,api,extensionPlugin);
		if(!isEmpty(api)){api.addHoverFunc();}
	},
	scrollToY:function(destY,time,ele){
		var element = $(ele).jScrollPane(this._settings);
		var api = element.data('jsp');
		if(!isEmpty(api)){api.scrollToY(destY,time);}
	}
};
$(document).ready(function(){
	$('.ol-chat-list').on('mouseenter',function(){
		if(!$(this).hasClass('fa-spin')){$(this).addClass('fa-spin');}
	}).on('mouseleave',function(){
		if($(this).hasClass('fa-spin')){$(this).removeClass('fa-spin');}
	}).on('click',function(){
		ajaxCall('/chat/online', '', '', '', 'GET', '', 'chatAvailableSuccess', '', false);
	});
	$('body').on('click','.ol-chat>span',function(event){
		if(!$(event.target).hasClass('ol-chat-list') && !$(event.target).hasClass('ol-add-chat')){
			var $this=$(this),par=$($this).parent(),
			ele=($(par).find('.ol-chat-window').length==0)?$($this).next():$(par).find('.ol-chat-window');
			if($(ele).is(':visible')){
				$(ele).slideUp('slow',function(){
					$($this).removeClass('bb');
					if(/showOnline/i.test($(ele).attr('id'))){
						$('#onlineUsers').slideUp('slow',function(){
							$('#chatIcon').slideDown();
						});
					}
				});
			}else{
				$($this).addClass('bb');
				$(ele).slideDown('slow',function(){
					if(/showOnline/i.test($(ele).attr('id'))){
						scroll.initializeEle(ele);
					}else{
						scroll.initializeClass('ol-history');
					}
				});
			}
		}
	});
	$('body').on('click','#chatIcon',function(){
		if(!$('#onlineUsers').is(':visible')){
			$(this).slideUp('normal',function(){
				$('#onlineUsers').slideDown('normal',function(){
					$('#onlineUsers>span').trigger('click');
				});
			});
		}
	});
	$('body').on('click','.online',function(){
		var to=$(this).attr('data-email'),name=$(this).attr('data-name'),from=$("#hiddenuseremail").text();
		if(!isEmpty(to) && !isEmpty(name) && !isEmpty(from)){
			chat.user(name,to,from);
		}
	});
	$('body').on('click','.ol-close-chat',function(){
		var id=$(this).attr('data-id');
		$('body').find('#'+id).slideUp('normal',function(){
			$(this).remove();
		});
	});
	$('body').on('click','.ol-chat-send',function(){
		var from=$(this).attr('data-from'),to=$(this).attr('data-to'),id=$(this).attr('data-id');
		if(undefined!=from && ''!=from && null!=from && undefined!=to && ''!=to && null!=to && undefined!=id && ''!=id && null!=id){
			var url=$(this).attr('data-content');
			var content=(isEmpty(url))?$.trim($('#'+id).find('.ol-content .ol-text').val()):url;
			if(undefined!=content && ''!=content && null!=content){
				var json={};
				json.from=to;
				json.to=from;
				json.message=content;
				ajaxCall('/chat/sendMessage', json, '', '', 'POST', '', 'sendMessageSuccess', '', false);
				var name=$('#hiddenuseremail').next('b').text();
				chat.appendToMessage(id,name,content);
				$(this).removeAttr('data-content');
			}
		}
	});
	$('body').on('click','.ol-attach',function(){
		var id=$(this).attr('data-id');
		if(!isEmpty(id)){
			var ele=$('#'+id);
			chat.uploadFile(ele);
		}
	});
	$('body').on('click','.ol-add-chat,.ol-close-new',function(){
		var id=$(this).attr('data-id');
		if(!isEmpty(id)){
			var ele=$('#'+id).find('.ol-add-user');
			if(!isEmpty(ele)){
				$(ele).find('.ol-new-user').val('');
				if($(this).hasClass('ol-add-chat') && !$(ele).is(':visible')){
					$(ele).fadeIn('slow');
				}else if($(this).hasClass('ol-close-new') || $(ele).is(':visible')){
					$(ele).fadeOut('slow');
				}
			}
		}
	});
	$('body').on('click','.ol-add-new',function(){
		var id=$(this).attr('data-id');
		if(!isEmpty(id)){
			var ele=$('#'+id);
			if(!isEmpty(ele)){
				var val=$.trim($(ele).find('.ol-new-user').val());
				if(!isEmpty(val)){
					val=val.split('-');
					$(ele).find('.ol-name').append(','+$.trim(val[0]));
					var to=$(ele).find('.ol-chat-send').attr('data-from');
					to+=','+$.trim(val[1]);
					$(ele).find('.ol-chat-send').attr('data-from',to);
					$(ele).attr('data-email',to);
					$(ele).find('.ol-new-user').val('');
				}
			}
		}
	});
	$('#chatMessages').on('click',function(){
		var api=scroll.getJscrollpaneApiEle($('#chatOverview ul.leftpanel-subdiv'));
		if(!isEmpty(api)){api.destroy();}
		$('#chatsHistory,#chatOverview ul.leftpanel-subdiv').empty();
		alwaysScrollTop();
		$('body').css('overflow', 'hidden');
		if(!$('#chatLoading').is(':visible')){
			$('#chatList').hide();
			$('#chatLoading').show();
		}
		historyMonth=1;
		ajaxCall('/chat/history/'+historyMonth, '', '', '', 'GET', '', 'chatHistorySuccess', '', false);
		$('.tab-pane,#privaySettingDiv,.common-rightpanel').hide();
		$('#chatMessagesList').fadeIn(600);
		$('#systemconfigadminId1').find('.active').removeClass('active');
	});
	$('body').on('click','#chatOverview .user-chat-overview',function(){
		var chat=$(this).attr('data-chat');
		if(!isEmpty(chat)){
			var ele=$('#chatsHistory').find('ul[data-msg="'+chat+'"]');
			if(!$(ele).is(':visible')){
				$('#chatOverview').find('.user-chat-overview').removeClass('user-chat-overview-sel');
				$(this).addClass('user-chat-overview-sel');
				$('#chatsHistory').find('ul').hide();
				$(ele).fadeIn(500);
				scroll.initializeEle(ele);
			}
		}
	});
	$(window).resize(function(){
		scroll.initializeEle($('#chatOverview ul.leftpanel-subdiv'));
		scroll.initializeEle($('#chatsHistory').find('ul:visible'));
	});
});
function chatHistorySuccess(data){
	if(data.result){
		var res=data.datas,msgs='';
		if(!isEmpty(res) && res.length>0){
			var i=0,html='',user=$("#hiddenuseremail").text();
			for(len=res.length;i<len;i++){
				html+='<li class="user-chat-overview" data-chat="'+res[i].email+'-'+res[i].chatDate+'">'
					+'<span class="user-chat-name" title="'+res[i].email+'">'+res[i].email
					+'</span><span class="user-chat-time" title="'+res[i].chatDate+'">'+res[i].chatDate+'</span></li>';
				msgs=res[i].messages;
				if(!isEmpty(msgs) && msgs.length>0){
					var j=0,chatClass='',subHtml='<ul style="display:none;" data-msg="'+res[i].email+'-'+res[i].chatDate+'">';
					for(msgLen=msgs.length;j<msgLen;j++){
						chatClass=(user==msgs[j].email)?'msg-you':'msg-other';
						subHtml+='<li class="msg '+chatClass+'">'
								+'<span class="msg-chat">'+msgs[j].chat+'</span>'
								+'<span class="msg-time" title="'+msgs[j].email+'">'+msgs[j].email+'</span></li>';
					}
					subHtml+='</ul>';
					$('#chatsHistory').append(subHtml);
				}
			}
			if(historyMonth<=1 && $('#chatOverview').find('ul.leftpanel-subdiv li').children().length==0){
				$('#chatOverview').find('ul.leftpanel-subdiv').html(html);
				$('#chatOverview').find('ul.leftpanel-subdiv li:first').addClass('user-chat-overview-sel');
			}else{
				$('#chatOverview').find('ul.leftpanel-subdiv').append(html);
			}
			$('#chatsHistory').find('ul:first').show();
		}
	}else{

	}
	if($('#chatLoading').is(':visible')){
		$('#chatLoading').hide();
		$('#chatList').show();
	}
	scroll.initializeEle($('#chatOverview ul.leftpanel-subdiv'));
	scroll.initializeEle($('#chatsHistory').find('ul:first'));
	$('#chatOverview ul.leftpanel-subdiv').unbind('jsp-scroll-y');
	$('#chatOverview ul.leftpanel-subdiv').bind('jsp-scroll-y',function(event, scrollPositionY, isAtTop, isAtBottom){
		if(isAtBottom){
			historyMonth++;
			ajaxCall('/chat/history/'+historyMonth, '', '', '', 'GET', '', 'chatHistorySuccess', '', false);
		}
	});
}
function sendMessageSuccess(data){
//	console.log(data);
	if(data.result){

	}
}
function chatAvailableSuccess(data){
	var html='';
	if(data.result){
		var res=data.users,user=$("#hiddenuseremail").text(),count=0,i=0;
		for(len=res.length;i<len;i++){
			if(user!=res[i].email){
				html+='<li class="online" data-email="'+res[i].email+'" data-name="'+res[i].name+'">'
					+'<i class="fa fa-circle ol-indication"></i><div class="ol-user">'+res[i].name+'</div></li>';
					count++;
			}
		}
		$('#chatIcon').attr('title', 'Online Users - '+count);
		$('#onlineCount').html(count);
		if(count>0){$('#showOnline').css('min-height','20px');}else{$('#showOnline').css('min-height','0')}
	}else{
		$('#chatIcon').attr('title', 'Problem in connecting with the chat.');
	}
	$('#showOnline #availableUsers').html(html);
	scroll.initializeId('showOnline');
}
var chat={
	displayMessage:function(data){
		var from=data.from,from,to=data.to,msg=data.message;
		if(!isEmpty(from) && !isEmpty(to) && !isEmpty(msg)){
			var id=this._getChatWindowId(from),/*isExists=this._chatExists(id)*/isExists=this._chatExists(id,from),
			name=(isEmpty(data.name))?from:data.name;
			var head=chat.getChatWindowHead(from);
			if(isExists){
				id=isExists;
				var ele=$('#'+id+'-chat');
				$(ele).find('.ol-name').html(head);
				$(ele).find('.ol-chat-send').attr('data-from',from);
				$(ele).attr('data-email',from);
				this._highlightChatWindow(id);
			}else{
				this._openChatWindow(id,head,from,to);
			}
//			this._appendFromMessage(id+'-chat','',msg);
			this._appendFromMessage(id+'-chat',name,msg);
			playSound();
		}
	},
	getChatWindowHead:function(from){
		var arr=[],i=0,ele,names='';
		if(from.indexOf(',')>0){arr=from.split(',');}else{arr[0]=from;}
		for(len=arr.length;i<len;i++){
			ele=$('#availableUsers').find('li[data-email="'+arr[i]+'"]');
			if(!isEmpty(ele)){names+=$(ele).attr('data-name')+',';}
		}
		return names.substring(0,names.length-1);;
	},
	_clearMessage:function(id){
		$('#'+id).find('.ol-content .ol-text').val('');
	},
	appendToMessage:function(id,time,msg){
		var $id=$('#'+id);
		$($id).find('.ol-history ul').append('<li class="ol-con ol-chat-you"><span class="ol-chat-time" title="'+time+'">'+time+'</span><span class="ol-chat-con">'+msg+'</span></li>');
		scroll.initializeClass('ol-history');
		scroll.scrollToY($($id)[0].scrollHeight,1000,$('.ol-history'));
		this._clearMessage(id);
	},
	_appendFromMessage:function(id,time,msg){
		var $id=$('#'+id);
		$($id).find('.ol-history ul').append('<li class="ol-con ol-chat-other"><span class="ol-chat-time" title="'+time+'">'+time+'</span><span class="ol-chat-con">'+msg+'</span></li>');
		scroll.initializeClass('ol-history');
		scroll.scrollToY($($id)[0].scrollHeight,1000,$('.ol-history'));

		this._clearMessage(id);
	},
	_getAppendMessage:function(isTo,time,msg){
		var html='';
		if(isEmpty(isTo)){
			html='<li class="ol-con ol-chat-you"><span class="ol-chat-time">23:10</span><span class="ol-chat-con">qwe</span></li>';
		}else{
			html='<li class="ol-con ol-chat-other"><span class="ol-chat-time">23:10</span><span class="ol-chat-con">asdadsjknasdkjnjjkasbndjknasjkdnkjasndkjansjkdnlakjsd</span></li>'
		}
	},
	user:function(name,fromEmail,toEmail){
		var id=this._getChatWindowId(fromEmail),/*isExists=this._chatExists(id)*/isExists=this._chatExists(id,fromEmail);
		if(isExists){
			this._highlightChatWindow(id);
		}else{
			this._openChatWindow(id,name,fromEmail,toEmail);
		}
	},
	_highlightChatWindow:function(id){
		var $id=$('#'+id+'-chat'),count=0;
		var int=setInterval(function(){
			if($id.length>0 && count<10){
				if($($id).hasClass('ol-chat-highlight')){
					$($id).removeClass('ol-chat-highlight');
					$($id).find('.ol-head').removeClass('ol-head-highlight');
					$($id).find('.ol-content').removeClass('ol-content-highlight');
				}else{
					$($id).addClass('ol-chat-highlight');
					$($id).find('.ol-head').addClass('ol-head-highlight');
					$($id).find('.ol-content').addClass('ol-content-highlight');
				}
				count++;
			}else{
				clearInterval(int);
			}
		},500);
	},
	_openChatWindow:function(id,name,fromEmail,toEmail){
		var html='<div class="ol-chat" style="display:none;" id="'+id+'-chat" data-email="'+fromEmail+'">'
				+'<span class="ol-head bb"><span class="ol-name" title="'+name+'">'+name+'</span><i class="fa fa-close ol-close-chat" data-id="'+id+'-chat" title="Close the chat window."></i><i class="fa fa-plus ol-add-chat" data-id="'+id+'-chat" title="Add users to this chat."></i></span>'
				+'<div class="ol-add-user"><input type="text" class="ol-new-user"><i class="fa fa-arrow-right ol-add-new" data-id="'+id+'-chat" title="Add the user."></i><i class="fa fa-close ol-close-new" data-id="'+id+'-chat" title="Close."></i></div>'
				+'<div class="ol-chat-window">'
				+'<div class="ol-history"><ul></ul></div>'
				+'<div class="ol-content">'
				+'<input type="text" class="ol-text">'
				+'<div class="ol-send-options"><i class=" fa fa-file ol-attach" title="Attach a file." data-id="'+id+'-chat"></i>'
				+'<br><i class="fa fa-angle-double-right ol-chat-send" title="Send the chat." data-from="'+fromEmail+'" data-to="'+toEmail+'" data-id="'+id+'-chat" data-name="'+name+'"></i><div>'
				+'</div></div></div>';
		$('body').find('.ol-chat:last').after(html);
		var right=$('body').find('.ol-chat:last').prev('.ol-chat').css('right');
		if(undefined==right){
			right='172px';
		}
		right=parseFloat(right.replace('px',''))+185;
		$('#'+id+'-chat').css('right',right).slideDown();
		scroll.initializeClass('ol-history');
		this._onlineUsersAutoComplete($('#'+id+'-chat'));
	},
	_onlineUsersAutoComplete:function(ele){
		$(ele).find(".ol-new-user").autocomplete({
			source: function(request,response){
				var val=$.trim(request.term);
				if(!isEmpty(val) && val.length>=2){
					$.ajax({
					  url: '/chat/searchUser/'+val+'/'+$(ele).attr('data-email'),
					  success: function(data) {
						  if(data.result){response(data.users);}
					  }
					});
				}
			  },
			  minLength: 2,
			  open: function(){
				$(this).removeClass("ui-corner-all").addClass("ui-corner-top");
			  },
			  close: function(){
				$(this).removeClass("ui-corner-top").addClass("ui-corner-all");
			  }/*,
		      select: function(event,ui){
		          log( ui.item ?
		            "Selected: " + ui.item.value + " aka " + ui.item.id :
		            "Nothing selected, input was " + this.value );
		      }*/
			});
	},
	_chatExists:function(id,from){
//		var res=($('#'+id+'-chat').length==0)?false:true;
		var res;
		if(from.indexOf(',')>0){
			var arr=from.split(','),i=0;
			while(i<arr.length){
				var id=this._getChatWindowId($.trim(arr[i]));
				if($('#'+id+'-chat').length>0){
					res=id;
					break;
				}
				i++;
			}
		}else{
			res=($('#'+id+'-chat').length==0)?false:id;
		}
		return res;
	},
	_getChatWindowId:function(fromEmail){
		var id=fromEmail.substring(0,fromEmail.indexOf('@'));
		if(/^[a-zA-Z0-9- ]*$/.test(id) == false) {
		    id=id.replace(/[^a-zA-Z0-9 ]/g,'-');
		}
		return id;
	},
	uploadFile:function(ele){
		//filepicker.setKey('A6zVM3VmDQhqeTq6sF209z');
		//filepicker.setKey('A7ucPpqRuR46F7OVE8CHJz');
		/*var fileurl="";var fileSize="";var fileFileFullName="";
		filepicker.pickAndStore({},{location:"S3"},function(fpfile){
			fpfile=fpfile[0];
			$(ele).find('.ol-content .ol-text').val(fpfile.filename);
			$(ele).find('.ol-chat-send').attr('data-content','<a href="'+fpfile.url+'" target="_blank">'+fpfile.filename+'</a>')
			inserIntoIdosFileUploadLogs(fpfile.filename,fpfile.size,fpfile.url);
		});*/
	}
};