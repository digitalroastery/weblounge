steal.plugins(	
	'jquery/controller',
	'jquery/controller/view',
	'jquery/controller/subscribe',
	'jquery/view',
	'jquery/view/tmpl')
.views('//editor/designer/views/init.tmpl')
.css('designer').then(function($) {
	
	$.Controller("Editor.Designer",
	/* @prototype */
	{
		init: function(el, url) {
//			$('html').css('background', '#FFFFFF');
			$(el).load(url);
//			$(el).html('//editor/designer/views/init.tmpl', {});
			steal.dev.log('test designer: ' + url);
		}
	});
	
});