steal.plugins('jquery/controller', 'editor/menubar').then(function($){
		
	$.Controller('Editor.App',
	/* @prototype */
	{
		
		init: function(el) {
			
			this.mode = 1;
      	
steal.dev.log('Starting the menubar widget.');
			this.find('div.menubar').editor_menubar();
            
        },
		
	})
	
});