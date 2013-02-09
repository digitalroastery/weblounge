steal.plugins('jquery/view/tmpl')
.css('login')
.then(function($){
	
	$(document).ready(function() {
	    // Append the Weblounge Editor skeleton at the end of the page body
		$(document.body).append('//login/views/login', {});
		
		// focus on the login-input on page startup
		$("#j_username").focus();
		
		// show error-message
		if (window.location.search.substring(1) == 'error') {
			$(document.body).find('.error').show();
		} 
	});

});