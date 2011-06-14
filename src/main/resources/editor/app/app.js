steal.plugins('jquery/controller', 'editor/menubar', 'editor/resourcebrowser').then(function($) {
		
	$.Controller('Editor.App',
	/* @prototype */
	{
		
		init: function(el) {
			
//			this.mode = 1;
 
			this.find('header:first').editor_menubar();
			//this.find('#designer').editor_resourcebrowser({resources: pages});		
			//this.find('#mediabrowser').editor_resourcebrowser({resources: pages});            
        }
		
	});
	
});