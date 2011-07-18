steal.plugins(
'jquery/view/tmpl', 
'editor/app')
.css('editor', 'css/jquery-ui-1.8.11', 'css/token-input-facebook', 'css/token-input')
.resources('jquery.validate.min')
.then(function($) {

  $(document).ready(function() {
	
    // Append the Weblounge Editor skeleton at the end of the page body
    $(document.body).append('//editor/views/app', {});

    // Start the Weblounge Editor App
    $('#weblounge-editor').editor_app();

  });

});