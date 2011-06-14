steal.plugins(
'jquery/view/tmpl', 
'editor/app')
.css(
'style',
'token-input-facebook.css',
'token-input')
.then(function($) {

  $(document).ready(function() {
	
    // Append the Weblounge Editor skeleton at the end of the page body
    $(document.body).append('//editor/views/app', {});

    // Start the Weblounge Editor App
    $('#container').editor_app();

  });

});
