/**
 * 
 */
(function($) {
	$.calculatorVersion = '1.0';
	var containers = new Array();
	var calc, focused;
	var defaults = {
		width : 250,
		height : 300,
		title : 'Calculator',
		show : false,
		draggable : true
	};
	$.fn.draggable = function(opt) {
        opt = $.extend({handle:"",cursor:"default"}, opt);
        if(opt.handle === "") {
            var $el = this;
        } else {
            var $el = this.find(opt.handle);
        }
        return $el.css('cursor', opt.cursor).on("mousedown", function(e) {
            if(opt.handle === "") {
                var $drag = $(this).addClass('draggable');
            } else {
                var $drag = $(this).addClass('active-handle').parent().addClass('draggable');
            }
            var z_idx = $drag.css('z-index'),
                drg_h = $drag.outerHeight(),
                drg_w = $drag.outerWidth(),
                pos_y = $drag.offset().top + drg_h - e.pageY,
                pos_x = $drag.offset().left + drg_w - e.pageX;
            $drag.css('z-index', 1000).parents().on("mousemove", function(e) {
                $('.draggable').offset({
                    top:e.pageY + pos_y - drg_h,
                    left:e.pageX + pos_x - drg_w
                }).on("mouseup", function() {
                    $(this).removeClass('draggable').css('z-index', z_idx);
                });
            });
            e.preventDefault(); // disable selection
        }).on("mouseup", function() {
            if(opt.handle === "") {
                $(this).removeClass('draggable');
            } else {
                $(this).removeClass('active-handle').parent().removeClass('draggable');
            }
        });
    }
	$.fn.calculator = function(cal) {
		var dn = {
			_init : function() {
				/*this._width = cal.width || defaults.width;
				this._height = cal.height || defaults.height;*/
				this._title = cal.title || defaults.title;
				this._show = cal.show || defaults.show;
				this._draggable = cal.draggable || defaults.draggable;
				this._prepareCalculator();
			},
			_prepareCalculator : function() {
				if (undefined === calc || $(calc).length === 0) {
					var html = '';
					html = '<div class="dn-calc" id="dnCalculator" title="This widget is draggable anywhere on the screen.">'
							+ '<div id="calcHead" class="calc-head"><div class="calc-title">'
							+ this._title
							+ '</div><div class="close-calc"></div></div>'
							+ '<div id="calcBody" class="calc-body">'
//							+ '<div class="calc-menu"><span>File</span><span>Help</span></div>'
							+ '<div class="calc-screen"><div id="calcTotal"></div><div id="dnCalculations"></div></div>'
							+ '<div class="calc-keys">'
							+ '<div class="calc-keys-row"><input type="checkbox" id="dnInput" style="display: none;"/>'
							+ '<button value="clear">C</button><button value="back" class="calc-button-2x calc-back"></button><button value="%">%</button>'
							+ '</div>'
							+ '<div class="calc-keys-row">'
							+ '<button value="7">7</button><button value="8">8</button><button value="9">9</button><button value="/">/</button>'
							+ '</div>'
							+ '<div class="calc-keys-row">'
							+ '<button value="4">4</button><button value="5">5</button><button value="6">6</button><button value="*">*</button>'
							+ '</div>'
							+ '<div class="calc-keys-row">'
							+ '<button value="1">1</button><button value="2">2</button><button value="3">3</button><button value="-">-</button>'
							+ '</div>'
							+ '<div class="calc-keys-row">'
							+ '<button value="0">0</button><button value=".">.</button><button value="=">=</button><button value="+">+</button>'
							+ '</div></div></div></div>';
					$('body').append(html);
					calc = $('body').find('div#dnCalculator');
					if (this._show) {
						$.showCalculator();
					}
					if (this._draggable) {
						$(calc).draggable();
					}
					/*if (this._width > 250) {
						$(calc).width(this._width);
					}
					if (this._height > 300) {
						$(calc).height(this._height);
					}*/
				}
			}
		};
		if ($(this).length > 0) {
			$(this).each(function() {
				containers.push($(this));
				$(this).on('click', function() {
					$.showCalculator();
				});
			});
			dn._init();
		} else {
			swal('Empty field','Please specify a binding element.','error');
		}
	}
	$.showCalculator = function() {
		calc = $('body').find('div#dnCalculator');
		if ($(calc).length === 0) {
			var item, d = 0;
			while (d !== containers.length) {
				item = containers[d];
				item = $('body').find(item);
				if (item.length === 1) {
					$(item).calculator(defaults);
					break;
				}
				d++;
			}
		}
		if (!$(calc).is(':visible')) {
			$(calc).fadeIn('normal', function() {
				$('#dnInput').prop('checked', true);
				focused = $(':focus');
			});
		}
	}
	$.hideCalculator = function() {
		if ($(calc).is(':visible')) {
			$(calc).fadeOut('normal', function() {
				$('#dnInput').prop('checked', false);
				$(focused).focus();
				focused = '';
			});
		}
	}
	$.isCalculatorVisible = function() {
		if ($(calc).is(':visible')) {
			return true;
		}
		return false;
	}
	$.fn.isCalculatorBound = function() {
		var res = false;
		try {
			($._data($(this)[0], "events")) ? res = true : res = false;
		} catch (msg) {
			res = false;
		}
		return res;
	}
	$('body').on('click', '.calc-keys button', function() {
		var val = this.value;
		functions.calculateVal(val);
	});
	$('body').on('click', 'div.close-calc', function() {
		$.hideCalculator();
	});
	var functions = {
		number : '',
		operation : '',
		_result : '',
		calculateVal : function(val) {
			switch (val) {
			case '+':
			case '-':
			case '*':
			case '/':
				functions.appendLower(val);
				functions.calculate();
				functions.operation = val;
				break;
			case '%':
				functions.calculate();
				functions.operation = val;
				functions.calculate();
				break;
			case '=':
				functions.calculate();
				functions.equals();
				break;
			case 'clear':
				functions.appendLower(val);
				functions.clear();
				break;
			case 'back':
				functions.back();
				break;
			default:
				functions.appendLower(val);
				if ('.' === val && functions.isDotAppended()) {
					val = '';
				}
				functions.number += val;
				break;
			}
		},
		isDotAppended : function(){
			var val = this.number.split('.');
			if (val.length > 1) {
				return true;
			}
			return false;
		},
		equals : function() {
			this.operation = '';
			$('#dnCalculations').empty();
			$('#calcTotal').html(this._result);
		},
		clear : function() {
			this._result = '';
			this._reset();
			$('#dnCalculations, #calcTotal').empty();
		},
		back : function() {
			var val = $('#dnCalculations').html();
			var last = val.substring(val.length - 1, val.length);
			if (isNaN(last)) {
				this.operation = '';
			} else {
				this.number = this.number.substring(0, this.number.length - 1);
			}
			val = val.substring(0, val.length - 1);
			$('#dnCalculations').empty();
			this.appendLower(val);
		},
		_checkLastAppendedOperator : function(val){
			var htm = $('#dnCalculations').html();
			var last = htm.substring(htm.length - 1, htm.length);
			if (isNaN(last) && '.' !== last){
				htm = htm.substring(0, htm.length - 1);
				$('#dnCalculations').html(htm);
			}
		},
		calculate : function() {
			if ('+' === this.operation) {
				this._result = parseFloat(this._result) + (('' !== this.number) ? parseFloat(this.number) : 0);
				this._reset();
			} else if ('-' === this.operation) {
				this._result = parseFloat(this._result) - (('' !== this.number) ? parseFloat(this.number) : 0);
				this._reset();
			} else if ('*' === this.operation) {
				this._result = parseFloat(this._result) * (('' !== this.number) ? parseFloat(this.number) : 0);
				this._reset();
			} else if ('/' === this.operation) {
				if (0 == this.number) {
					swal('Error!','Cannot divide by zero.','error');
					this._reset();
					this._result = '';
					$('#dnCalculations, #calcTotal').empty();
				} else {
					this._result = parseFloat(this._result) / (('' !== this.number) ? parseFloat(this.number) : 1);
					this._reset();
				}
			} else if ('%' === this.operation) {
				this._result = parseFloat(this._result) / 100;
				this._reset();
				this.equals();
			} else {
				this._result = (('' !== this.number) ? parseFloat(this.number) : parseFloat(this._result));
				this._reset();
			}
			$('#calcTotal').html(this._result);
		},
		_reset : function() {
			this.number = '';
			this.operation = '';
		},
		_isOperator : function(val){
			if ('+' === val || '-' === val || '*' === val || '/' === val || '%' === val){
				return true;
			}
			return false;
		},
		appendLower : function(val) {
			if ($('#dnCalculations').html().length === 0 && ('' === val || this._isOperator(val))) {
				$('#dnCalculations').append(this._result);
			}
			if ('.' === val && this.isDotAppended()) {
				val = '';
			} else if (this._isOperator(val)) {
				this._checkLastAppendedOperator(val);
			}
			$('#dnCalculations').append(val);
		},
		getInputFromKeyboard : function(keyPressed) {
			switch (keyPressed) {
				case 48 : return '0';
				case 49 : return '1';
				case 50 : return '2';
				case 51 : return '3';
				case 52 : return '4';
				case 53 : return '5';
				case 54 : return '6';
				case 55 : return '7';
				case 56 : return '8';
				case 57 : return '9';
				case 46 : return '.';
				case 43 : return '+';
				case 45 : return '-';
				case 42 : return '*';
				case 47 : return '/';
				case 37 : return '%';
				case 13	:
				case 61 : return '=';
				case 67 :
				case 99 : return 'clear';
				case 8	: return 'back';
			}
		}
	}
	$(document).ready(function() {
		$('body').on('click', function(event) {
			var target = $(event.target);    
		    if (target.parents('div#dnCalculator').length) {
		        $('#dnInput').prop('checked', true);
		        $('#dnCalculator').css('opacity', '1.0');
		    } else {
		    	focused = $(':focus');
		    	$('#dnInput').prop('checked', false);
		    	$('#dnCalculator').css('opacity', '0.5');
		    }
		});
		$('body').on('keypress', function(event) {
			if ($.isCalculatorVisible() && $('#dnInput').is(':checked')) {
				focused = $(':focus');
				$(focused).blur();
				var key = (event.keyCode ? event.keyCode : event.which);
				if (27 !== key) {
					key = functions.getInputFromKeyboard(key);
					if (undefined != key) {
						functions.calculateVal(key);
					}
				} else {
					$.hideCalculator();
				}
			}
		});
		$('body').on('keydown', function(event) {
			var key = (event.keyCode ? event.keyCode : event.which);
			if ($.isCalculatorVisible() && $('#dnInput').is(':checked')) {
				if (8 === key) {
					var clazz = event.target.className, id = event.target.id;
					key = functions.getInputFromKeyboard(key);
					if (undefined != key) {
						functions.calculateVal(key);
					}
					event.preventDefault();
				} else if (27 === key) {
					$.hideCalculator();
				}
			}
		});
	});
}(jQuery));