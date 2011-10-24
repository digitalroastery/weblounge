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
			var beforeLoad = null;
			var scrollItem = this.element;
			switch(this.options.resourceType) {
			case 'pages':
				var pageURL = this.element.find('a.wbl-pagePath').attr('href');
				if(this.options.page.version == 'work') {
					pageURL += 'work.html';
				}
				pageURL += '?preview&_=' + new Date().getTime();
				this._loadOverlay(function() {
					var iframeTag = this.getOverlay().find('iframe');
					iframeTag.css('width', $(window).width() * 0.8 + 'px');
					iframeTag.css('height', $(window).height() * 0.8 + 'px');
					iframeTag.attr('src', pageURL).show();
				}, function() {});
				break;
			case 'media':
				var fileResource = new Editor.File({value: this.options.page});
				var fileContent = fileResource.getContent(this.options.language);
				switch(this.options.page.type) {
				case 'file':
					var url = '/system/weblounge/files/' + $(el).attr('id') + '/content/' + fileContent.language;
	                window.open(url, "popUp", "width=800,height=600,scrollbars=yes");
					break;
				case 'image':
					this._loadOverlay($.proxy(function() {
						var url = '/system/weblounge/files/' + $(el).attr('id') + '/content/' + fileContent.language;
						var imgTag = this.element.find("div.wbl-previewOverlay img.wbl-overlayPreviewImage");
						var height = this._getScaledHeight(fileContent.width, fileContent.height);
						var width = this._getScaledWidth(fileContent.width, fileContent.height);
						imgTag.attr('src', url).show();
						imgTag.attr('width', width + 'px');
						imgTag.attr('height', height + 'px');
					}, this), function() {});
					break;
				case 'movie':
					var player = null;
					this._loadOverlay(function() {
						var url = '/system/weblounge/files/' + $(el).attr('id') + '/content/' + fileContent.language;
						var videoTag = this.getOverlay().find('video');
						videoTag.css('max-width', $(window).width() * 0.8 + 'px');
						videoTag.css('max-height', $(window).height() * 0.8 + 'px');
						videoTag.html('<source src="' + url + '" type="' + fileContent.mimetype + '" />').show();
						
						player = new MediaElementPlayer(videoTag, {});
					}, function() {
						if(player != null) {
							player.pause();
						}
					});
					break;
				}
				break;
			}

		},
		
		_getScaledHeight: function(width, height) {
			var maxWidth = $(window).width() * 0.8;
			var maxHeight = $(window).height() * 0.8;
			
			var scaleX = maxWidth / width;
			var scaleY = maxHeight / height;
			var scale = Math.min(scaleX, scaleY);
			
			if(scale > 1.0) {
				return height;
			} else {
				return height * scale;
			}
		},
		
		_getScaledWidth: function(width, height) {
			var maxWidth = $(window).width() * 0.8;
			var maxHeight = $(window).height() * 0.8;
			
			var scaleX = maxWidth / width;
			var scaleY = maxHeight / height;
			var scale = Math.min(scaleX, scaleY);
			
			if(scale > 1.0) {
				return width;
			} else {
				return width * scale;
			}
		},
		
		_loadOverlay: function(beforeLoad, beforeClose) {
			this.element.find("div.wbl-previewOverlay").overlay({
				top: 60,
				load: true,
				onBeforeLoad: beforeLoad,
				onBeforeClose: beforeClose
			}).load();
		},
		
		"click": function(el, ev) {
			var isMulti = (this.options.mode == 'editorMultiSelection');
			if(this.options.mode != 'normal' && !isMulti) {
				$('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
			}
			if($(ev.target).is('.wbl-showPage, .wbl-trashPage, .wbl-editPage, .wbl-pagePreview, .wbl-pagePath')) return;
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
			if(this.options.resourceType == 'pages') {
				this.element.find('img.wbl-pagePreview').show();
			}
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
