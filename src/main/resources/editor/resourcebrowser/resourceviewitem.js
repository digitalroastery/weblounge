steal.plugins().then(function($) {

 /*
  * 
  */
  $.Controller('Editor.Resourceviewitem', {
    defaults: {
  		page: {}
    }
  },

  {

    init: function(el) {
    },
    
	"a.wbl-pagePath click": function(el, ev) {
		ev.preventDefault();
		if(this.options.mode == 'normal') {
			el.trigger('openDesigner');
		} else {
			// Unmark all Elements, mark current and click ok
			if(el.parents('.wbl-thumbnailView').length > 0) {
				el.parents('.wbl-thumbnailView').find('div.wbl-scrollViewItem.wbl-marked').removeClass('wbl-marked');
				el.parents('.wbl-scrollViewItem').addClass('wbl-marked');
			} else {
				el.parents('.wbl-listView').find('tr.wbl-pageEntry input:checked').removeAttr('checked');
				el.parent().find('input').attr('checked', 'checked');
			}
			$('button.wbl-editorSelectionOK').click();
		}
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
				this._loadOverlay($.proxy(function() {
					var url = '/system/weblounge/files/' + $(el).attr('id') + '/content/' + fileContent.language;
					var videoTag = this.element.find("div.wbl-previewOverlay video");
					
					if(fileContent.video == undefined || fileContent.video.resolution == undefined) {
						videoTag.css('max-width', $(window).width() * 0.8 + 'px');
						videoTag.css('max-height', $(window).height() * 0.8 + 'px');
					} else {
						var resolution = fileContent.video.resolution.split('x');
						var height = this._getScaledHeight(resolution[0], resolution[1]);
						var width = this._getScaledWidth(resolution[0], resolution[1]);
						
						videoTag.attr('width', width + 'px');
						videoTag.attr('height', height + 'px');
					}
					videoTag.html('<source src="' + url + '" type="' + fileContent.mimetype + '" />').show();
					
					player = new MediaElementPlayer(videoTag, {});
				}, this), function() {
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

	_openSettings: function(resourceItem) {
		switch(this.options.resourceType) {
		case 'pages':
			Page.findOne({id: resourceItem.attr('id')}, $.proxy(function(page) {
    			var locked = page.isLocked();
            	var userLocked = page.isLockedUser(this.options.runtime.getUserLogin());
            	if(locked && !userLocked) {
            		this.element.trigger('showErrorMessage', "Can't edite page settings " + page.getPath() + ": Page is locked by " + page.getLockOwner() + "!");
            		return;
            	} else if(locked && userLocked) {
            		$('#wbl-pageheadeditor').editor_pageheadeditor({
            			page: page,
            			language: this.options.language,
            			runtime: this.options.runtime,
            			success: $.proxy(function() {
            				this._update();
            			}, this)
            		});
            	} else {
		    		page.lock(this.options.runtime.getUserLogin(), $.proxy(function() {
		    			$('#wbl-pageheadeditor').editor_pageheadeditor({
		    				page: page, 
		    				language: this.options.language, 
		    				runtime: this.options.runtime,
		    				success: $.proxy(function() {
		    					page.unlock($.proxy(function(){
		    						this._update();
		    					}, this), $.proxy(function() {
		    						this.element.trigger('showErrorMessage', "Can't unlock page " + page.getPath() + "!");
		    					}, this));
		    				}, this)
		    			});
		    		}, this), $.proxy(function() {
		    			this.element.trigger('showErrorMessage', "Can't lock page " + page.getPath() + "!");
		    		}, this));
            	}
			}, this));
			break;
		case 'media':
			var map = new Array({resourceId: resourceItem.attr('id')});
			$('div#wbl-mediaeditor').editor_mediaeditor({
				map: map, 
				language: this.options.language,
				runtime: this.options.runtime,
				success: $.proxy(function() {
					this._update();
				}, this)
			});
			break;
		}
	},
	
	_update: function() {
		switch(this.options.resourceType) {
			case 'pages':
				Page.findOne({id: this.element.attr('id')}, $.proxy(function(page) {
					this._updateView(page.value);
				}, this));
				break;
			case 'media':
				Editor.File.findOne({id: this.element.attr('id')}, $.proxy(function(file) {
					file.value.type = file.name.key;
					this._updateView(file.value);
				}, this));
				break;
		}
	}

  });

});
