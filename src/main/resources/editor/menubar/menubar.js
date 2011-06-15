steal.plugins(
	'jquery/controller',
	'jquery/controller/view',
	'jquery/controller/subscribe',
	'jquery/view',
	'jquery/view/tmpl',
	'jqueryui/button')
.views(
	'//editor/menubar/views/menubar.tmpl')
.css(
	'menubar',
	'css/blitzer/jquery-ui-1.8.11')
.then(function($) {

    $.Controller("Editor.Menubar",
    /* @prototype */
    {
     /**
     * Initialize a new MenuBar controller.
     */
        init: function(el) {
            $(el).html('//editor/menubar/views/menubar.tmpl', {});
            this.pagesTab = $('#pagebrowser');
            this.mediaTab = $('#mediabrowser');
            this.designerTab = $('#designer');
            this.toolbarMore = this.find('img.more').hide();
            this.toolbarEdit = this.find('span.editmode').hide();
            this.pageOptions = this.find('div#page_options').hide();
            this.tabElement = this.pagesTab.editor_resourcebrowser({resourceType: 'pages'});
        },
        
        _toggleTab: function(tab, el) {
			this.element.find('.tab.active').removeClass('active');
			el.addClass('active');
        	this.tabElement.hide();
        	this.tabElement = tab;
        	tab.show();
        },
        
        _showDesignerToolbar: function(show) {
			this.toolbarEdit.toggle(show);
			this.toolbarMore.toggle(show);
			this.pageOptions.toggle(show);
        },
        
    	"designer.open subscribe": function(called, pageId) {
			this._showDesignerToolbar(true);
			this.element.find('.tab.active').removeClass('active');
        	this.tabElement.hide();
        	this.tabElement = this.designerTab;
        	this.designerTab.show();
//			this.designerTab.editor_designerbrowser(pageId);
    	},
        
		".tab.designer click": function(el, ev) {
			this._showDesignerToolbar(true);
//			this.designerTab.editor_designerbrowser();
		},
		
		".tab.pages click": function(el, ev) {
			this._toggleTab(this.pagesTab, el);
			this._showDesignerToolbar(false);
			this.pagesTab.editor_resourcebrowser({resourceType: 'pages'});
		},
		
		".tab.media click": function(el, ev) {
			this._toggleTab(this.mediaTab, el);
			this._showDesignerToolbar(false);
			this.mediaTab.editor_resourcebrowser({resourceType: 'media'});
		},
		
		"li.settings click": function(el, ev) {
			steal.dev.log('settings')
			$('.menu').hide();
//			$('#editor').dialog( "option", "title", 'Einstellungen' ).dialog('open').load('user_preferences.html');
		},
		
		"li.news click": function(el, ev) {
			steal.dev.log('news')
		},
		
		"li.logout click": function(el, ev) {
			steal.dev.log('logout')
		},
		
		"li.new_page click": function(el, ev) {
			steal.dev.log('new_page')
		},
		
		"li.new_upload click": function(el, ev) {
			steal.dev.log('upload')
		},
		
		"li.new_note click": function(el, ev) {
			steal.dev.log('note')
		},
	
		// trigger menus
		".editor_menubar img.add click": function(el, ev) {
			$('.menu').hide();
			$('div#add-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		".editor_menubar img.more click": function(el, ev) {
			$('.menu').hide();
			$('div#more-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		".editor_menubar span.profile-menu click": function(el, ev) {
			$('.menu').hide();
			$('div#profile-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		"div#page_options click": function(el, ev) {
			el.toggle(function() {
				$(this).animate({"right": "0"}, "slow")
			}, function() {
				$(this).animate({"right": "-200px"}, "slow")
			});
		},
		
		".editor_menubar input focus": function(el, ev) {
			$('div#search-result').show();
		},
		
		".editor_menubar input blur": function() {
			$('div#search-result').hide();
		},
		
		".div.search-result p.footer click": function() {
//			$('#editor').dialog( "option", "title", 'Suchresultate' ).dialog('open')
			$('div.search-result').hide();
		},
		
		/* move to new plugin "designer" */
		".pagelet hover": function() {
			$(this).addClass('hover');
		},
		
		// trigger editmode
		"input#editmode click": function(el, ev) {
			if(el.is(':checked')) {
				steal.dev.log('editmode is enabled');
			} else {
				steal.dev.log('editmode is disabled');
				// Publish Dialog
//				$('#editor').dialog({title: 'Seite publizieren', buttons: {
//					Ja: function() {
//						$(this).dialog('close');
//						log('weiter');
//					},
//					Nein: function() {
//						$(this).dialog('close');
//						log('abbrechen');
//					}
//					
//				}} ).dialog('open').load('publish_page.html')
			}
		}
		
    });

});