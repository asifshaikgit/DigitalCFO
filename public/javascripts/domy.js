
// JavaScript Document
// ----------------------------------------------------------
// If you're not in IE (or IE version is less than 5) then:
//     ie === undefined
// If you're in IE (>5) then you can determine which version:
//     ie === 7; // IE7
// Thus, to detect IE:
//     if (ie) {}
// And to detect the version:
//     ie === 6 // IE6
//     ie> 7 // IE8, IE9 ...
//     ie <9 // Anything less than IE9
// ----------------------------------------------------------
var ie = (function(){
	var undef, v = 3, div = document.createElement('div');

    while (
        div.innerHTML = '<!--[if gt IE '+(++v)+']><i></i><![endif]-->',
        div.getElementsByTagName('i')[0]
    );

    return v> 4 ? v : undef;
}());
if (ie<10) {
		
		$("#p-nav").css('padding','5px');
		$(".red-right").css('margin-right','4px');		
		}

/* Custom events. Can be removed */