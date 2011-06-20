steal.plugins('jquery/controller', 'editor/menubar', 'editor/resourcebrowser', 'editor/composer', 'editor/designer').then(function($) {
		
	$.Controller('Editor.App',
	{
    	/**
    	 * Mode 0 = Designer
    	 * Mode 1 = Pages
    	 * Mode 2 = Media
    	 */
    	defaults: {
    		mode: 0
    	}
	},
	/* @prototype */
	{
		
		init: function(el) {
			this.menuBar = this.find('#menubar').editor_menubar();
            this.pagesTab = this.find('#pagebrowser');
            this.mediaTab = this.find('#mediabrowser');
            $('.composer').editor_composer();
            this._initTab();
        },
        
        _initTab: function() {
        	this.update();
        },
        
        update: function(options) {
        	if(options !== undefined) {
        		this.options.mode = options.mode;
        	}
        	switch (this.options.mode) {
	      	  case 0:
	      	  	this.pagesTab.hide();
	      	  	this.mediaTab.hide();
	      		break;
	      	  case 1:
	      		this.pagesTab.show();
	      	  	this.mediaTab.hide();
	      	  	break;
	      	  case 2:
	      		this.mediaTab.show();
	      	  	this.pagesTab.hide();
	      		break;
        	}
        },
        
        "a showDesigner": function(el, ev, pageId, url) {
        	this.update({mode: 0});
        	this.menuBar.editor_menubar({mode: 0});
//        	this.designerTab.editor_designer('show', pageId, url);
        },
        
        "a showPages": function(el, ev) {
        	this.update({mode: 1});
        	this.menuBar.editor_menubar({mode: 1});
        	this.pagesTab.editor_resourcebrowser({resourceType: 'pages'});
        },
        
        "a showMedia": function(el, ev) {
        	this.update({mode: 2});
        	this.menuBar.editor_menubar({mode: 2});
        	this.mediaTab.editor_resourcebrowser({resourceType: 'media'});
        }
        
	});
	
});