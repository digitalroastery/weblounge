steal.plugins('editor/resourcebrowser/resourceview').views('//editor/resourcebrowser/resourcescrollview/views/init.tmpl').then(function($) {

  Editor.Resourceview.extend('Editor.Resourcescrollview', 
	{
		
		init: function(el) {
			$(el).html('//editor/resourcebrowser/resourcescrollview/views/init.tmpl', {})
		}
		
	});

});
