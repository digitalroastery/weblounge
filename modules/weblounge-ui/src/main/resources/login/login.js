steal
.plugins('jquery/view/tmpl')
.css('login')
.then(function($){
	
	$(document).ready(function() {
		// Append the Weblounge Editor skeleton at the end of the page body
		$(document.body).append('//login/views/login', {});
		
		// Store the username & password in localStorage
		// =============================================

		var field_username = $('#username'),
			field_password = $('#password'),
			field_remember = $('#remember');

		// retrieve the stored username & password
		username = window.localStorage.getItem('username');
		password = window.localStorage.getItem('password');
	 
		// if a username & password were saved from previous session
		// set the values of the username & password field to that
		// tick off the checkbox 
		if (username) {
			field_username.val(username);
			field_password.val(password);
			field_remember.prop('checked', true);
			$('#submit').focus();
		}
	 
		// if username & password weren't saved then
		// set username field value to blank and focus on it
		// and make sure the checkbox is unchecked
		else {
			field_username.val('').focus();
			field_remember.prop('checked', false);
		}
	 
		// when form is submitted check the checkbox
		// if it's checked then save the username using jStorage
		// if not then delete whatever saved username exists
		$('#submit').click(function(e){
			if (field_remember.prop('checked')) {
				window.localStorage.setItem('username', field_username.val());
				window.localStorage.setItem('password', field_password.val());
			}
			else {
				window.localStorage.removeItem('username');
				window.localStorage.removeItem('password');
			}
		});
		
		// show error-message
		if (window.location.search.substring(1) == 'error') {
			$(document.body).find('.error').show();
		} 
	});

});