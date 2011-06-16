steal.plugins('jquery/controller', 'editor/menubar', 'editor/resourcebrowser', 'editor/composer', 'editor/designer').then(function($) {
		
	$.Controller('Editor.App',
	/* @prototype */
	{
		
		init: function(el) {
			
//			this.mode = 1;
			//TODO GLOBALER ABFANGPUNKT FÜR TRIGGER ANSTATT Ajax.Open.hub
 
			this.menuBar = this.find('header:first').editor_menubar();
			//this.find('#designer').editor_resourcebrowser({resources: pages});		
			//this.find('#mediabrowser').editor_resourcebrowser({resources: pages});            
			// 
			
			$('.composer').editor_composer();
        },
        
        "a openDesigner": function(el, ev, param1, param2) {
        	steal.dev.log('param1' + param1);
        	this.menuBar.editor_menubar("_openDesigner", param1, param2);
        }
		
	});
	
});