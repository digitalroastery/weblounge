steal.plugins('jquery/controller', 'editor/menubar', 'editor/resourcebrowser').models('../../models/page').then(function($) {
		
	$.Controller('Editor.App',
	/* @prototype */
	{
		
		init: function(el) {
			
			this.mode = 1;
 
			this.find('#menubar').editor_menubar();
			Page.findAll({}, function(pages) {
				$('#pagebrowser').editor_resourcebrowser({resources: pages});
			});
			
            
        }
		
	});
	
});