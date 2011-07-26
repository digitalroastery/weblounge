steal.plugins('jquery/controller', 'editor/menubar', 'editor/resourcebrowser', 'editor/composer')
.models('../../models/runtime', '../../models/page')
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
			if (!this._supportsLocalStorage()) {
				alert('Your browser does not support HTML5! Update your browser to the latest version.');
				return false;
			}
			
			Runtime.findOne({}, this.callback('_loadRuntime'));
			this._loadCurrentLanguage();
			this._loadValidateLanguage();
			
        	var path = location.pathname.substring(0, location.pathname.lastIndexOf('/') + 1);
        	Page.findOne({path: path}, this.callback('_setPage'));
        },
        
        _loadRuntime: function(runtime) {
        	this.runtime = runtime;
        	this.options.language = this.runtime.getDefaultLanguage();
        },
        
        _loadCurrentLanguage: function() {
        	var temp = location.pathname.substring(location.pathname.lastIndexOf('/') + 1, location.pathname.length);
        	var language = temp.length == 2 ? temp : '';
        	if(language == '') {
        		language = localStorage['weblounge.editor.' + this.runtime.getId() + '.language'];
        	}
        	if (language) { 
        		this.options.language = language;
        	}
        	localStorage['weblounge.editor.' + this.runtime.getId() + '.language'] = this.options.language;
        },
        
        _loadValidateLanguage: function() {
        	$.getScript(this.runtime.getRootPath() + '/editor/resources/localization/messages_' + this.options.language + '.js');
        },
        
        _supportsLocalStorage: function() {
        	try {
        		return 'localStorage' in window && window['localStorage'] !== null;
        	} catch (e) {
        		return false;
        	}
        },
        
        _initTab: function() {
        	this.update();
        },
        
        _setPage: function(page) {
        	$('.composer').editor_composer({page: page, language: this.options.language, runtime: this.runtime});
			this.menuBar = this.find('#wbl-menubar').editor_menubar({page: page, runtime: this.runtime, language: this.options.language});
            this.pagesTab = this.find('#wbl-pagebrowser');
            this.mediaTab = this.find('#wbl-mediabrowser');
            this._initTab();
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
            var language = localStorage['weblounge.editor.' + this.runtime.getId() + '.language'];
            if (!language) {
            	location.href = el.attr('href') + "?edit";
            }
            else {
            	location.href = el.attr('href') + language + "?edit";
            }
        },
        
        "a showPages": function(el, ev) {
        	this.update({mode: 1});
        	this.pagesTab.editor_resourcebrowser({resourceType: 'pages', language: this.options.language, runtime: this.runtime});
        },
        
        "a showMedia": function(el, ev) {
        	this.update({mode: 2});
        	this.mediaTab.editor_resourcebrowser({resourceType: 'media', language: this.options.language, runtime: this.runtime});
        },
        
        "span changeLanguage": function(el, ev, language) {
        	localStorage['weblounge.editor.' + this.runtime.getId() + '.language'] = language;
        	var path = location.pathname.substring(0, location.pathname.lastIndexOf('/') + 1);
        	location.href = path + language + "?edit";
        }
        
	});
	
});