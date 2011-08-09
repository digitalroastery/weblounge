steal.plugins(
'jquery/view/tmpl', 
'editor/app')
.css('editor', 'css/jquery-ui', 'css/token-input-facebook', 'css/token-input', 'css/validation')
.resources('jquery.validate.min')
.then(function($) {

  $(document).ready(function() {
	
    // Append the Weblounge Editor skeleton at the end of the page body
    $(document.body).append('//editor/views/app', {});

    // Start the Weblounge Editor App
    $('#weblounge-editor').editor_app();

  });

});