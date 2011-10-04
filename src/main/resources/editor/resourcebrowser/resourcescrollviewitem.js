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
			this.element.find('img.wbl-pagePreview').hide();
		},
		
		_showResource: function(el) {
			var language = this.options.language;
			var overlay = this.element.find("div.wbl-previewOverlay");
			switch(this.options.resourceType) {
			case 'pages':
				overlay.overlay({
					top: 50,
					left: 50,
					load: true,
					onBeforeLoad: function() {
						var pageURL = this.element.find('a.wbl-pagePath').attr('href');
						this.getOverlay().append('<iframe src="/weblounge-pages/' + $(el).attr('id') + '/' + language + '" width="800" height="600" seamless></iframe>');
					}
				}).load();
				break;
			case 'media':
				switch(this.options.page.type) {
				case 'file':
					var url = '/system/weblounge/files/' + $(el).attr('id') + '/content/' + language;
	                window.open(url, "popUp", "width=800,height=600,scrollbars=yes");
					break;
				case 'image':
					overlay.overlay({
						top: 50,
						load: true,
						onBeforeLoad: function() {
							var url = '/system/weblounge/files/' + $(el).attr('id') + '/content/' + language;
							this.getOverlay().find('.wbl-overlayPreviewImage').attr('src', url).show();
						}
					}).load();
					break;
				case 'video':
					// TODO Stop video when close
					overlay.overlay({
						top: 50,
						load: true,
						onBeforeLoad: function() {
							var url = '/system/weblounge/files/' + $(el).attr('id') + '/content/' + language;
							var videoTag = '<video src="' + url + '" width="320" height="240" preload controls></video>';
							this.getOverlay().find('.wbl-overlayPreviewContent').html(videoTag).show();
						}
					}).load();
					break;
				}
				break;
			}
		},
		
		"click": function(el, ev) {
			var isMulti = (this.options.mode == 'editorMultiSelection');
			if(this.options.mode != 'normal' && !isMulti) {
				$('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
			}
			if($(ev.target).is('.wbl-showPage, .wbl-trashPage, .wbl-editPage, .wbl-pagePath')) return;
			el.toggleClass('wbl-marked');
		},
		
		"dblclick": function(el, ev) {
			switch(this.options.mode) {
			case 'normal':
				this.element.find('img.wbl-showPage').click();
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
		
		"img.wbl-showPage click": function(el, ev) {
			ev.stopPropagation();
			switch(this.options.resourceType) {
			case 'pages':
				this.element.find('a.wbl-pagePath').click();
				break;
			case 'media':
				this._showResource(el.parent());
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
		
		'hoverenter': function(ev, hover) {
			if(this.options.mode != 'normal') return;
//			if(this.options.resourceType == 'pages') {
//				this.element.find('img.wbl-pagePreview').show();
//			}
			this.element.find('img.wbl-showPage').show();
			this.element.find('img.wbl-trashPage').show();
			this.element.find('img.wbl-editPage').show();
	    },
	    
		'hoverleave': function(ev, hover) {
			this.element.find('img.wbl-pagePreview').hide();
			this.element.find('img.wbl-showPage').hide();
			this.element.find('img.wbl-trashPage').hide();
			this.element.find('img.wbl-editPage').hide();
	    }
		
	});

});
