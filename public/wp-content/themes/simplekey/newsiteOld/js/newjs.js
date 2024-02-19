<!-- Basic Date Range Picker -->
$(document).ready(function() {
  $('#date-range-picker').daterangepicker(null, function(start, end, label) {
    console.log(start.toISOString(), end.toISOString(), label);
  });
});

// Show Tables
$(document).ready(function() {
    $('#showtables').DataTable();

} );

//Order by Show Tables
$(document).ready(function() {
    var table = $('#example').DataTable({
        "columnDefs": [
            { "visible": false, "targets": 2 }
        ],
        "order": [[ 2, 'asc' ]],
        "displayLength": 25,
        "drawCallback": function ( settings ) {
            var api = this.api();
            var rows = api.rows( {page:'current'} ).nodes();
            var last=null;
 
            api.column(2, {page:'current'} ).data().each( function ( group, i ) {
                if ( last !== group ) {
                    $(rows).eq( i ).before(
                        '<tr class="group"><td colspan="5">'+group+'</td></tr>'
                    );
 
                    last = group;
                }
            } );
        }
    } );
 
    // Order by the grouping
    $('#showtables tbody').on( 'click', 'tr.group', function () {
        var currentOrder = table.order()[0];
        if ( currentOrder[0] === 2 && currentOrder[1] === 'asc' ) {
            table.order( [ 2, 'desc' ] ).draw();
        }
        else {
            table.order( [ 2, 'asc' ] ).draw();
        }
    } );
} );

//Create Transaction
   $('#createtransaction').change(function() {
        $('.showtable').hide();
        $('#' + $(this).val()).show();
 });

// Submit 
document.querySelector('.submit').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 
document.querySelector('.cancel').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};

// Submit 2
document.querySelector('.submit2').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 2
document.querySelector('.cancel2').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 3
document.querySelector('.submit3').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 3
document.querySelector('.cancel3').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 4
document.querySelector('.submit4').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 4
document.querySelector('.cancel4').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 5
document.querySelector('.submit5').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 5
document.querySelector('.cancel5').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 6
document.querySelector('.submit6').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 6
document.querySelector('.cancel6').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 7
document.querySelector('.submit7').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 7
document.querySelector('.cancel7').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 8
document.querySelector('.submit8').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 8
document.querySelector('.cancel8').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 9
document.querySelector('.submit9').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 9
document.querySelector('.cancel9').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 10
document.querySelector('.submit10').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 

// cancel 10
document.querySelector('.cancel10').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 11
document.querySelector('.submit11').onclick = function(){
swal("Searsh", "You clicked on Searsh!", "success")
 }; 

 // cancel 12
document.querySelector('.cancel12').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};

// Submit 13
document.querySelector('.submit13').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 
 // cancel 13
document.querySelector('.cancel13').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 14
document.querySelector('.submit14').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 
 // cancel 14
document.querySelector('.cancel14').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 15
document.querySelector('.submit15').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 
 // cancel 15
document.querySelector('.cancel15').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 16
document.querySelector('.submit16').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 
 // cancel 16
document.querySelector('.cancel16').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 17
document.querySelector('.submit17').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 
 // cancel 17
document.querySelector('.cancel17').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 18
document.querySelector('.submit18').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 
 // cancel 18
document.querySelector('.cancel18').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 19
document.querySelector('.submit19').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 
 // cancel 19
document.querySelector('.cancel19').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
// Submit 12
document.querySelector('.submit12').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 
  // cancel transaction
document.querySelector('.canceltr').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
  // cancel transaction 1
document.querySelector('.canceltr1').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
  // cancel transaction 2
document.querySelector('.canceltr2').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
  // cancel transaction 3
document.querySelector('.canceltr3').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};

  // cancel transaction 4
document.querySelector('.canceltr4').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
 // cancel transaction 5
document.querySelector('.canceltr5').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};
 // cancel transaction 6
document.querySelector('.canceltr6').onclick = function(){
swal({
title: "Are you sure?",
text: "You will not be able to recover this transaction!",
type: "warning",
showCancelButton: true,
confirmButtonColor: "#DD6B55",
confirmButtonText: "Yes, cancel it!",
cancelButtonText: "No, cancel please!",
closeOnConfirm: false,
closeOnCancel: false
},
function(isConfirm){
if (isConfirm) {
swal("Deleted!", "Your transaction has been deleted.", "success");
} else {
swal("Cancelled", "Your transaction is safe :)", "error");
}
});
};

// Submit 13
document.querySelector('.submitbtn').onclick = function(){
swal("Good job!", "You clicked submit!", "success")
 }; 


			//Función en caso de error
			var error = function(e) {
				console.log('¡No pude grabarte!', e);
			};

			//Función cuando todo tenga exito
			var exito = function(s) {
				var context = new webkitAudioContext(); //Conectamos con nuestra entrada de audio
				var flujo = context.createMediaStreamSource(s); //Obtenemos el flujo de datos desde la fuente
				recorder = new Recorder(flujo); //Todo el flujo de datos lo pasamos a nuestra libreria para procesarlo en esta instancia
				recorder.record(); //Ejecutamos la función para procesarlo
			}

			//Convertirmos el objeto en URL
			window.URL = window.URL || window.webkitURL;
			navigator.getUserMedia  = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia;

			var recorder; //Es nuestra variable para usar la libreria Recorder.js
			var audio = document.querySelector('audio'); //Seleccionamos la etiqueta audio para enviarte el audio y escucharla

			//Funcion para iniciar el grabado
			function grabar() {
				if (navigator.getUserMedia) { //Preguntamos si nuestro navegador es compatible con esta función que permite usar microfono o camara web
					navigator.getUserMedia({audio: true}, exito, error); //En caso de que si, habilitamos audio y se ejecutan las funciones, en caso de exito o error.
					document.querySelector('p').innerHTML = "Estamos grabando...";
				} else {
					console.log('¡Tu navegador no es compatible!, ¿No lo vas a acutalizar?'); //Si no es compatible, enviamos este mensaje.
				}
			}

			//Funcion para parar la grabación y escucharla
			function parar() {
				recorder.stop(); //Paramos la grabación
				recorder.exportWAV(function(s) { //Exportamos en formato WAV el audio 
					audio.src = window.URL.createObjectURL(s); //Y convertimos el valor devuelto en URL para pasarlo a nuestro reproductor.
				});
				document.querySelector('p').innerHTML = "Paramos la grabación y ahora escuchala...";
			}

			

