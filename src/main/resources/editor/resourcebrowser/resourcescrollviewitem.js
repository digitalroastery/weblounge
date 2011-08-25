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
			var isMulti = false;
			if(this.options.mode == 'editorSelection' && !isMulti) {
				$('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
			}
			if($(ev.target).is('.wbl-showPage, .wbl-trashPage, .wbl-editPage, .wbl-pagePath')) return;
			el.toggleClass('wbl-marked');
		},
		
		"dblclick": function(el, ev) {
			if(this.options.mode == 'editorSelection') {
				$('button.wbl-editorSelectionOK').click();
			} else {
				this.element.find('img.wbl-showPage').click();
			}
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
			el.trigger('deleteResource', el.parent());
		},
		
		"img.wbl-editPage click": function(el, ev) {
			ev.stopPropagation();
			this._openSettings(el.parent());
		},
		
		'hoverenter': function(ev, hover) {
			if(this.options.mode == 'editorSelection') return;
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
