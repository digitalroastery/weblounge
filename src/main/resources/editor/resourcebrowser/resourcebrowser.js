steal.plugins(
	'jquery/controller/view', 
	'jquery/view/tmpl',
	'editor/resourcebrowser/resourcescrollview')
.views('//editor/resourcebrowser/views/init.tmpl')
.then(function($) {

  $.Controller('Editor.Resourcebrowser', 
	{
		defaults: {
			view: 'scrollview'
		}
	},
	
	{
		
		init: function(el) {
			$(el).html('//editor/resourcebrowser/views/init.tmpl', {});
			steal.dev.log('demo');
			
			this.element.find('div.resourceview').editor_resourcescrollview();
		},
		
		changeView: function() {
			
		},
		
		loadRecent: function() {
			
		},
		
		loadFavorites: function() {
			
		},
		
		loadPending: function() {
			
		},
		
		loadAll: function() {
			
		}
		
	});

});
