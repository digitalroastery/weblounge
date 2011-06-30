steal.plugins('jquery/controller', 'editor/menubar', 'editor/massuploader', 'editor/resourcebrowser', 'editor/composer')
.models('../../models/site', '../../models/page')
.then(function($) {
		
	$.Controller('Editor.App',
	{
    	/**
    	 * Mode 0 = Designer
    	 * Mode 1 = Pages
    	 * Mode 2 = Media
    	 */
    	defaults: {
    		mode: 0,
    		language: 'de'
    	}
	},
	/* @prototype */
	{
		
		init: function(el) {
			if (!this._supportsLocaleStorage()) {
				alert('Your browser does not support HTML5! Update your browser to the latest version.');
				return false;
			}
			
			Site.findOne({}, this.callback('_loadSite'));
			this._loadCurrentLanguage();
			
			this.menuBar = this.find('#menubar').editor_menubar({site: this.site, language: this.options.language});
            this.pagesTab = this.find('#pagebrowser');
            this.mediaTab = this.find('#mediabrowser');
            this.massuploader = this.find('#massuploader');
            this._initTab();
            this._loadPage();
        },
        
        _loadSite: function(site) {
        	this.site = site;
        	this.options.language = this.site.getDefaultLanguage();
        },
        
        _loadCurrentLanguage: function() {
        	var language = location.pathname.substring(location.pathname.lastIndexOf('/') + 1, location.pathname.length);
        	if(language == '') {
        		language = localStorage['weblounge.editor.' + this.site.getId() + '.language'];
        	}
        	if (language) { 
        		this.options.language = language;
        	}
        	localStorage['weblounge.editor.' + this.site.getId() + '.language'] = this.options.language;
        },
        
        _supportsLocaleStorage: function() {
        	try {
        		return 'localStorage' in window && window['localStorage'] !== null;
        	} catch (e) {
        		return false;
        	}
        },
        
        _initTab: function() {
        	this.update();
        },
        
        _loadPage: function() {
        	var path = location.pathname.substring(0, location.pathname.lastIndexOf('/') + 1);
        	Page.findOne({path: path}, this.callback('_setPage'));
        },
        
        _setPage: function(page) {
        	this.page = page;
        	$('.composer').editor_composer({page: page, language: this.options.language});
        },
        
        update: function(options) {
        	if(options !== undefined) {
        		this.options.mode = options.mode;
        	}
        	this.menuBar.editor_menubar({mode: this.options.mode});
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
        },
        
        "a openDesigner": function(el, ev) {
            var language = localStorage['weblounge.editor.' + this.site.getId() + '.language'];
            if (!language) {
            	location.href = el.attr('href') + "?edit";
            }
            else {
            	location.href = el.attr('href') + language + "?edit";
            }
        },
        
        "a showPages": function(el, ev) {
        	this.update({mode: 1});
        	this.pagesTab.editor_resourcebrowser({resourceType: 'pages'});
        },
        
        "a showMedia": function(el, ev) {
        	this.update({mode: 2});
        	this.mediaTab.editor_resourcebrowser({resourceType: 'media'});
        },
        
        "li uploadMedia": function(el, ev) {
        	this.update({mode: 2});
      		this.mediaTab.hide();
      		this.pagesTab.hide();
      		this.massuploader.show();
      		this.massuploader.editor_massuploader();
        },
        
        "span changeLanguage": function(el, ev, language) {
        	localStorage['weblounge.editor.' + this.site.getId() + '.language'] = language;
        	var path = location.pathname.substring(0, location.pathname.lastIndexOf('/') + 1);
        	location.href = path + language + "?edit";
        }
        
	});
	
});