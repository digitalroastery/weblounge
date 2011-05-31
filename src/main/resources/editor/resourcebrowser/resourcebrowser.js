steal.plugins(
	'jquery/controller/view', 
	'jquery/view/tmpl')
.views('//editor/resourcebrowser/views/init.tmpl')
.css('resourcebrowser')
.then('resourcescrollview', 'resourcelistview')
.then(function($) {

  $.Controller('Editor.Resourcebrowser', 
	{
		defaults: {
			resources: {},
			resourceType: 'pages',
			view: 'scrollview'
		}
	},
	
	{
		
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/init.tmpl', {});
			
			this.element.find('div.resourceview').editor_resourcescrollview({resources: this.options.resources});
		}
		
	});

});
