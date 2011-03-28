steal.plugins('jquery/view/tmpl', 'editor/menubar', 'editor/massuploader')
.then(function($) {

$(document).ready(function() {
	
	// Append the Weblor skeleton at the end of the page body
	$(document.body).append('//editor/views/app', {});
	
	$('#weblor .menubar').editor_menubar();
	
	var uploader = $('#weblor .massuploader').editor_massuploader();
	
	$('#weblor .menubar').bind('startuploader', function() {
		uploader.myshow();
	});
	
	
});


});
