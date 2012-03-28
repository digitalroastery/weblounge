steal.plugins('jquery/view/tmpl')
.css('login')
.then(function($){
	
	$(document).ready(function() {
	    // Append the Weblounge Editor skeleton at the end of the page body
		$(document.body).append('//login/views/login', {});
		
		
		var test = $(document.body).find('input[name="username"]').focus();
		if (window.location.search.substring(1) == 'error') {
			$(document.body).find('.error').show();
		} 
	});

});
	 