steal.plugins('jquery/controller', 'editor/menubar', 'editor/resourcebrowser', 'editor/pageheadeditor', 'editor/composer')
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
			if(!this._checkRequirements()) return;
			Runtime.findOne({}, this.callback('_setRuntime'));
			this._loadCurrentLanguage();
			this._loadValidateLanguage();
			
        	Page.findOne({path: window.currentPagePath}, this.callback('_initViews'));
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
        
        _checkRequirements: function() {
        	var success = true;
			if (!this._supportsLocalStorage()) {
				alert('Your browser does not support HTML5! Update your browser to the latest version.');
				success = false;
			}
			if (!navigator.cookieEnabled) { 
				alert('Your browser has disabled cookies! Enable your browser cookies.');
				success = false;
			}
			return success;
        },
        
        _initViews: function(page) {
        	if(page.isWorkVersion()) this._initComposer(page);
			this.menuBar = this.find('#wbl-menubar').editor_menubar({page: page, runtime: this.runtime, language: this.options.language});
            this.pagesTab = this.find('#wbl-pagebrowser');
            this.mediaTab = this.find('#wbl-mediabrowser');
            this.update();
        },
        
        _initComposer: function(page) {
        	$('.composer').editor_composer({page: page, language: this.options.language, runtime: this.runtime});
        },
        
        _setRuntime: function(runtime) {
        	this.runtime = runtime;
        	this.options.language = this.runtime.getDefaultLanguage();
        },
        
        _loadCurrentLanguage: function() {
        	var language = window.currentLanguage;
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
        
        "a showDesigner": function(el, ev, pageId, url) {
        	this.update({mode: 0});
        },
        
        "a openDesigner": function(el, ev) {
            var language = localStorage['weblounge.editor.' + this.runtime.getId() + '.language'];
            if (!language) {
            	location.href = el.attr('href') + "?edit&_=" + new Date().getTime();
            }
            else {
            	location.href = el.attr('href') + language + "?edit&_=" + new Date().getTime();
            }
        },
        
        "a showPages": function(el, ev) {
        	if(this.options.mode == 1) {
        		this.pagesTab.editor_resourcebrowser(true);
        	} else {
        		this.update({mode: 1});
        		this.pagesTab.editor_resourcebrowser({resourceType: 'pages', language: this.options.language, runtime: this.runtime});
        	}
        },
        
        "a showMedia": function(el, ev) {
        	if(this.options.mode == 2) {
        		this.mediaTab.editor_resourcebrowser(true);
        	} else {
        		this.update({mode: 2});
        		this.mediaTab.editor_resourcebrowser({resourceType: 'media', language: this.options.language, runtime: this.runtime});
        	}
        },
        
        "div#wbl-mainContainer updateLastMedia": function() {
        	this.mediaTab.editor_resourcebrowser('_updateLast');
        },
        
        "span changeLanguage": function(el, ev, language) {
        	localStorage['weblounge.editor.' + this.runtime.getId() + '.language'] = language;
        	location.href = window.currentPagePath + language + "?edit&_=" + new Date().getTime();
        }
        
	});
	
});