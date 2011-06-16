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
		init: function(el) {
			$(el).html('//editor/designer/views/init.tmpl', {});
		},
		
		show: function(pageId, url) {
			steal.dev.log('pageId: ' + pageId)
			this.element.load(url);
		}
		
	});
	
});