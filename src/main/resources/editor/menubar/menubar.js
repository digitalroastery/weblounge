steal.plugins(
'jquery/controller',
'jquery/controller/view',
'jquery/view',
'jquery/view/tmpl')
.views('//editor/menubar/views/menubar.tmpl').css('menubar').then(function($) {

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
		
		// Beispiel
		// ========
		//"a.pages click": function() {
		//	this.halloWelt();
		//	console.log('Konsolenmeldung');
		//},
		//
		//halloWelt: function() {
		//  alert('Jetzt bin ich Hallo Welt!');
		//}	
				
    });

});