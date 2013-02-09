steal.plugins('jquery/view/tmpl', 'editor/app')
.css('editor', 'css/bootstrap', 'css/font-awesome.min', 'css/jquery-ui', 'css/token-input-facebook', 'css/token-input', 'css/validation', 'css/player/mediaelementplayer')
.resources('jquery.validate.min', 'jquery.cookie', 'jquery.ba-bbq.min', 'jquery.tools.min', 'mediaelement-and-player.min')
.then(function($) {

	$(document).ready(function() {
		// Add all links and form actions timestamp
		$("a, form").querystring({ _: new Date().getTime()});
		
		// Append the Weblounge Editor skeleton at the end of the page body
		$(document.body).prepend('//editor/views/app', {});
		
		// Start the Weblounge Editor App
		$('#weblounge-editor').editor_app();
		
		// Replace all body position absolute top + 45px
		// steal.dev.log('transition 45px');
		// $('body > *').each(function(index, elem) {
		// 	if($(elem).css('position') == 'absolute') {
		// 		var cssTop = $(elem).css('top');
		// 		if(cssTop == '') return;
		// 		var top = parseFloat(cssTop);
		// 		$(elem).css('top', top + 45 + 'px');
		// 	}
		// });
	});
	
});