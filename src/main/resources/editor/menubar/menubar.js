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
        },

		".tab click": function(el, ev) {
			this.element.find('.tab.active').removeClass('active');
			el.addClass('active');
			this.element.trigger('menubarmodechange', {mode: 'mymode'});
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