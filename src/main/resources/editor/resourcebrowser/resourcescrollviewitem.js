steal.plugins('jquery/controller', 'jquery/event/hover', 'jquery/controller/view')
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcescrollviewitem',
	{
		init: function(el) {
			$.Hover.delay = 0;
			this.element.find('img.wbl-showPage').hide();
			this.element.find('img.wbl-trashPage').hide();
		},
		
		"click": function(el, ev) {
			el.toggleClass('wbl-marked');
		},
		
		"img.wbl-showPage click": function(el, ev) {
			// TODO if Media hover not show oder so ???
			// and remove Link
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
