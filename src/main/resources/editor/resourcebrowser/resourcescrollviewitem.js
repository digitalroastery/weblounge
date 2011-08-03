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
			ev.stopPropagation();
			switch(this.options.resourceType) {
			case 'pages':
				this.element.find('a.wbl-pagePath').click();
				break;
			case 'media':
				
				//TODO
//                window.open('/system/weblounge/files/' + $(el.parent).attr('id') + 'content/' + this.options.language, "popUp", "width=300,height=400,scrollbars=yes");
//                event.preventDefault();
//				
//				// Show image
//				//Grab the href, open it in a window and cancel the click action
//				$("a[href^='http']").click(function(){});
//				//Add target = blant to the external link
//				$("a[href^='http']").attr('target','_blank');
////				this.element.find('a.wbl-pagePath').click();
				break;
			}
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
