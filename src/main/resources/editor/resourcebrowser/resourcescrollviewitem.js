steal.plugins('jquery/controller', 'jquery/event/hover', 'jquery/controller/view')
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcescrollviewitem',
	{
		init: function(el) {
			$.Hover.delay = 0;
			this.element.find('img.wbl-showPage').hide();
			this.element.find('img.wbl-trashPage').hide();
			
			// Load default image if error during preview loading
            this.element.find('img.wbl-pageThumbnail').bind('error', function(event) {
            	$(this).attr('src', '/weblounge/editor/resourcebrowser/images/empty_thumbnail.png');
            });
		},
		
		"click": function(el, ev) {
			el.toggleClass('wbl-marked');
		},
		
		"img.wbl-showPage click": function(el, ev) {
			ev.stopPropagation();
			this.element.find('a.wbl-pagePath').click();
		},
		
		"img.wbl-trashPage click": function(el, ev) {
			ev.stopPropagation();
			el.trigger('deletePage');
		},
		
		'hoverenter': function(ev, hover) {
			this.element.find('img.wbl-showPage').show();
			this.element.find('img.wbl-trashPage').show();
	    },
	    
		'hoverleave': function(ev, hover) {
			this.element.find('img.wbl-showPage').hide();
			this.element.find('img.wbl-trashPage').hide();
	    }
		
	});

});
