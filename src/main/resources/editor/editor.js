steal.plugins('jquery/view/tmpl', 'editor/menubar', 'editor/massuploader')
.css('editor')
.then(function($) {

$(document).ready(function() {
	
	// Append the Weblor skeleton at the end of the page body
	$(document.body).append('//editor/views/app', {});
	
	$('#weblor .menubar').editor_menubar();
	
	
});


});
