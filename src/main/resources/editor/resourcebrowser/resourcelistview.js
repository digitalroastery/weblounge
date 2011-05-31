steal.plugins()
.views('//editor/resourcebrowser/views/resourcelistview.tmpl')
.then('resourceview')
.then(function($) {

  Editor.Resourceview.extend('Editor.Resourcelistview', 
	{
		
		init: function(el) {
			
		}
		
	});

});
