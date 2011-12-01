steal.plugins('jquery/controller', 'jquery/event/hover', 'jquery/controller/view')
.then('resourceviewitem')
.then(function($) {

  Editor.Resourceviewitem.extend('Editor.Resourcescrollviewitem',
	{
		init: function(el) {
			$.Hover.delay = 0;
			this.element.find('img.wbl-trashPage').hide();
			this.element.find('img.wbl-editPage').hide();
			this.element.find('img.wbl-pagePreview').hide();
		},
		
		"click": function(el, ev) {
			var isMulti = (this.options.mode == 'editorMultiSelection');
			if(this.options.mode != 'normal' && !isMulti) {
				$('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
			}
			
			if(!$(ev.target).is('.wbl-trashPage, .wbl-editPage, .wbl-pagePreview, .wbl-pagePath')) {
				el.toggleClass('wbl-marked');
			}
			
			// Enable or disable delete button
			if($('div.wbl-scrollViewItem.wbl-marked').length > 0) {
				$('button.wbl-delete').button("enable");
			} else {
				$('button.wbl-delete').button("disable");
			}
		},
		
		"dblclick": function(el, ev) {
			switch(this.options.mode) {
			case 'normal':
				ev.stopPropagation();
				switch(this.options.resourceType) {
				case 'pages':
					this.element.find('a.wbl-pagePath').click();
					break;
				case 'media':
					this._showResource(el.parent());
				}
				break;
			case 'editorMultiSelection':
				$('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
				el.addClass('wbl-marked');
				$('button.wbl-editorSelectionOK').click();
				break;
			case 'editorSelection':
				$('button.wbl-editorSelectionOK').click();
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
		
		"img.wbl-pagePreview click": function(el, ev) {
			ev.stopPropagation();
			this._showResource(el.parent());
		},
		
		"hoverenter": function(ev, hover) {
			if(this.options.mode != 'normal') return;
			this.element.find('img.wbl-pagePreview').fadeIn('fast');
			this.element.find('img.wbl-trashPage').fadeIn('fast');
			this.element.find('img.wbl-editPage').fadeIn('fast');
	    },
	    
		"hoverleave": function(ev, hover) {
			this.element.find('img.wbl-pagePreview').fadeOut('fast');
			this.element.find('img.wbl-trashPage').fadeOut('fast');
			this.element.find('img.wbl-editPage').fadeOut('fast');
	    }
		
	});

});
