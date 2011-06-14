steal.plugins(
	'jquery/controller',
	'jquery/controller/view',
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
    {
		defaults: {
			tabElement: null
		}
    },
    /* @prototype */
    {
     /**
     * Initialize a new MenuBar controller.
     */
        init: function(el) {
            $(el).html('//editor/menubar/views/menubar.tmpl', {});
            this.options.tabElement = $('#pagebrowser').editor_resourcebrowser({resourceType: 'pages'});
        },
        
        _toggleTab: function(el) {
        	this.options.tabElement.hide();
        	this.options.tabElement = el;
        	el.show();
        },
        
		".tab click": function(el, ev) {
			this.element.find('.tab.active').removeClass('active');
			el.addClass('active');
//			this.element.trigger('menubarmodechange', {mode: 'mymode'});
		},
		
		".tab.pages click": function(el, ev) {
			var tab = $('#pagebrowser');
			this._toggleTab(tab)
			tab.editor_resourcebrowser({resourceType: 'pages'});
		},
		
		".tab.media click": function(el, ev) {
			var tab = $('#mediabrowser');
			this._toggleTab(tab)
			tab.editor_resourcebrowser({resourceType: 'media'});
		},
		
		".tab.designer click": function(el, ev) {
			var tab = $('#designer');
			this._toggleTab(tab)
//			tab.editor_designerbrowser();
		},
		
		"li.settings click": function(el, ev) {
			steal.dev.log('settings')
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
			$('div#add-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		".editor_menubar span.profile-menu click": function() {
			$('.menu').hide();
			$('div#profile-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		".editor_menubar input focus": function(el, ev) {
			$('div#search-result').show();
		},
		
		".editor_menubar input blur": function() {
			$('div#search-result').hide();
		},
		
		/* move to new plugin "designer" */
		".pagelet hover": function() {
			$(this).addClass('hover');
		}
		
    });

});