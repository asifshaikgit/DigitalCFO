/**
 * 
 */

$(document).ready(function() {
	$('#support').on('click', function() {
		$('#overlay, #supportModal').fadeIn();
	});
	
	$('#channelPartner').on('click', function() {
		$('#overlay, #channelPartnerModal').fadeIn();
	});
	
	$('.supportCancel').on('click', function() {
		$('#overlay, .extra-modal').fadeOut();
	});
	
	$('#supportSend').on('click', function() {
		var email = $('#supportEmail').val();
		var sub = $('#supportSubject').val();
		var msg = $('#supportMessage').val();
		if ("" !== email && "" !== sub && "" !== msg) {
			var jsonData = {};
			jsonData.email = email;
			jsonData.sub = sub;
			jsonData.msg = msg;
			$.ajax({
				url: '/index/support',
				data:JSON.stringify(jsonData),
				type:"text",
				async: true,
				method:"POST",
				contentType:'application/json',
				success: function (data) {	
					$('#overlay, .extra-modal').fadeOut();
				},
				error: function (xhr, status, error) {	
					$('#overlay, .extra-modal').fadeOut();
				}
			 });
		} else {
			if("" === email) {
				swal('Incomplete Details!','Email required.','error');
			} else if("" === sub) {
				swal('Incomplete Details!','Subject required.','error');
			} else if("" === msg) {
				swal('Incomplete Details!','Message required.','error');
			}
		}
	});
	
	$('#channelPartnerSend').on('click', function() {
		var name = $('#cpName').val();
		var phone = $('#cpPhone').val();
		var email = $('#cpEmail').val();
		var sub = $('#cpSubject').val();
		var query = $('#cpQuery').val();
		if ("" !== email && "" !== sub && "" !== name) {
			var jsonData = {};
			jsonData.email = email;
			jsonData.sub = sub;
			jsonData.msg = query;
			jsonData.name = name;
			jsonData.phone = phone;
			$.ajax({
				url: '/index/channelPartner',
				data:JSON.stringify(jsonData),
				type:"text",
				async: true,
				method:"POST",
				contentType:'application/json',
				success: function (data) {	
					$('#overlay, .extra-modal').fadeOut();
				},
				error: function (xhr, status, error) {
					$('#overlay, .extra-modal').fadeOut();
				}
			 });
		} else {
			 if("" === name) {
				swal('Incomplete Details!','Name required.','error');
			} else if("" === email) {
				swal('Incomplete Details!','Email required.','error');
			} else if("" === sub) {
				swal('Incomplete Details!','Subject required.','error');
			} else if("" === msg) {
				swal('Incomplete Details!','Message required.','error');
			}
		}
	});
});