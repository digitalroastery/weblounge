steal.plugins('jquery/controller', 'editor/menubar', 'editor/resourcebrowser', 'editor/composer', 'editor/designer').then(function($) {
		
	$.Controller('Editor.App',
	/* @prototype */
	{
		
		init: function(el) {
			this.menuBar = this.find('header:first').editor_menubar();
			this.designerTab = this.find('#designer').editor_designer();
            this.pagesTab = this.find('#pagebrowser').editor_resourcebrowser({resourceType: 'pages'});
            this.mediaTab = this.find('#mediabrowser').editor_resourcebrowser({resourceType: 'media'});
            this.tabElement = this.pagesTab;
            this.designerTab.hide();
            this.mediaTab.hide();
			$('.composer').editor_composer();
        },
        
        _toggleTab: function(tab) {
        	this.tabElement.hide();
        	this.tabElement = tab;
        	tab.show();
        },
        
        "a showDesigner": function(el, ev, pageId, url) {
        	this._toggleTab(this.designerTab);
        	this.designerTab.editor_designer('show', pageId, url);
        },
        
        "a showPages": function(el, ev) {
        	this._toggleTab(this.pagesTab);
//        	this.pagesTab.editor_resourcebrowser('mach öbis');
        },
        
        "a showMedia": function(el, ev) {
        	this._toggleTab(this.mediaTab);
//        	this.mediaTab.editor_resourcebrowser('mach öbis');
        }
		
	});
	
});