// search Transaction only

$(document).ready(function(){
$('.multipleDropdownForSearch').multiselect({
    maxHeight: 110,
    enableFiltering :true,
    includeSelectAllOption: true,
    numberDisplayed: 0,
    enableCaseInsensitiveFiltering: true,
    onChange: function(element, checked) {
      var elemId=$(element).context.id;
      if(elemId=="userRole"){
    	  var elemValue=$(element).val();
    	  if(checked == true) {
    		  /*
    		   * Existing problem:
    		   * 1. When Auditor is selected all other roles are disabled which is correct.
    		   * 2. So ideally it should show 1 selected which is Auditor but it shows 3 selected etc which is previous selection too which is wrong.
    		   * 3. For userRole selectAll option should be disabled.
    		   */
   	    	 if(elemValue==7){ //If Auditor role selected then disable all other roles
   	    		  $('input:checkbox[value=3]').prop("checked",false); //even if creator is selected, uncheck that
   	    		  $('input:checkbox[value=4]').prop("checked",false);
   	    		  $('input:checkbox[value=5]').prop("checked",false);
   	    		  $('input:checkbox[value=6]').prop("checked",false);
   	    		  $('input:checkbox[value=3]').prop("disabled",true); //Creator option disabled
   	    		  $('input:checkbox[value=4]').prop("disabled",true); //Approver
   	    		  $('input:checkbox[value=5]').prop("disabled",true); //Accountant
   	    		  $('input:checkbox[value=6]').prop("disabled",true); //Controller

   	    		 // $('.multipleDropdown').multiselect('rebuild');
   	    		//$('input:checkbox[value=3]').remove();
   	    		//$("#userRole").find('option[value="4"]').remove();
   	    	 }
   	      }
    	  else if(checked == false) {
	    	  if(elemValue==1 || elemValue==8 || elemValue==9 || elemValue==12){
	    		  $('input:checkbox[value='+elemValue+']').prop("checked",true);
	    		  $("select[name='userRole'] option").filter(function () {return $(this).val()==elemValue;}).prop("selected", "selected");
	    	  }
	    	  else if(elemValue==7){ //if auditor role deslected, enable all other roles
  	    		  $('input:checkbox[value=3]').prop("disabled",false);
  	    		  $('input:checkbox[value=4]').prop("disabled",false);
  	    		  $('input:checkbox[value=5]').prop("disabled",false);
  	    		  $('input:checkbox[value=6]').prop("disabled",false);
  	    	  }
	      }
      }
    }
  });
});



