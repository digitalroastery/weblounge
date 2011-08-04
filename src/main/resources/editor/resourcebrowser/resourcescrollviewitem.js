steal.plugins('jquery/controller', 'jquery/event/hover', 'jquery/controller/view')
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcescrollviewitem',
	{
		init: function(el) {
			$.Hover.delay = 0;
			this.element.find('img.wbl-showPage').hide();
			this.element.find('img.wbl-trashPage').hide();
			this.element.find('img.wbl-editPage').hide();
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
				var url = '/system/weblounge/files/' + $(el.parent()).attr('id') + '/content/' + this.options.language;
                window.open(url, "popUp", "width=800,height=600,scrollbars=yes");
                ev.preventDefault();
				break;
			}
		},
		
		"img.wbl-trashPage click": function(el, ev) {
			ev.stopPropagation();
			el.trigger('deletePage');
		},
		
		"img.wbl-editPage click": function(el, ev) {
			ev.stopPropagation();
			this._openSettings(el.parent().attr('id'));
		},
		
		'hoverenter': function(ev, hover) {
			this.element.find('img.wbl-showPage').show();
			this.element.find('img.wbl-trashPage').show();
			this.element.find('img.wbl-editPage').show();
	    },
	    
		'hoverleave': function(ev, hover) {
			this.element.find('img.wbl-showPage').hide();
			this.element.find('img.wbl-trashPage').hide();
			this.element.find('img.wbl-editPage').hide();
	    }
		
	});

});
