steal.plugins(
'jquery/controller',
'jquery/controller/view',
'jquery/view',
'jquery/view/tmpl')
.views('//editor/menubar/views/menubar.tmpl').css('menubar', 'general').then(function($) {

    $.Controller("Editor.Menubar",

    /* @prototype */
    {
     /**
     * Initialize a new MenuBar controller.
     */
        init: function(el) {
            $(el).html('//editor/menubar/views/menubar.tmpl', {});
            
            // initiate buttons
            
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
		
		".editor_menubar input#search focus": function() {
			$('div.search-result').show();
		},
		
		".editor_menubar input#search blur": function() {
			$('div.search-result').hide();
		},
		
				
    });

});