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

    /* @prototype */
    {
     /**
     * Initialize a new MenuBar controller.
     */
        init: function(el) {
            $(el).html('//editor/menubar/views/menubar.tmpl', {});
            // initiate buttons
            $('nav.weblounge div.view').buttonset();
            $('nav.weblounge div.filter').buttonset();
            $('nav.weblounge button.list').button({
            	icons: {primary: "icon-list"},
            	text: false
            }).click(function() { 
            		steal.dev.log('switch to list view');
            		$('.center.icons').show();
            		$('div.listview').show();
            		$('div.treeview').hide();
            		$('div.thumbnailview').hide();
            });
            $('button.tree').button({
            	icons: {primary: "icon-tree"},
            	disabled: true,
            	text: false
            }).click(function() { 
            		steal.dev.log('switch to tree view') 
            		$('div.treeview').show();
            		$('div.listview').hide();
            		$('div.thumbnailview').hide();
            });
            $('nav.weblounge button.thumbnails').button({
            	disabled: false,
            	icons: {primary: "icon-thumbnails"},
            	text: false }).click(function() { 
            		steal.dev.log('switch to thumbnail view');
            		$('.center.icons').hide();
            		$('div.treeview').hide();
            		$('div.listview').hide();
            		$('div.thumbnailview').show();
            });
                
        },

		changeMode: function() {},

		".tab click": function(el, ev) {
			this.element.find('.tab.active').removeClass('active');
			el.addClass('active');
			this.element.trigger('menubarmodechange', {mode: 'mymode'});
		},

		".uploader click": function(el, ev) {
			$(this.element).trigger('startuploader');
		},
		
		// trigger menus
		".editor_menubar img.add click": function(el, ev) {
			$('div#add-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		".editor_menubar span.profile-menu click": function() {
			$('.menu').hide();
			$('div#profile-menu').show().hover(function() { }, function() {$(this).hide();});
		},
		
		".editor_menubar input focusin": function(el, ev) {
			$('div#search-result').show();
		},
		
		".editor_menubar input blur": function() {
			$('div#search-result').hide();
		},
		
		/* move to new plugin "designer" */
		".pagelet hover": function() {
			$(this).addClass('hover');
		},
		
		
    });

});