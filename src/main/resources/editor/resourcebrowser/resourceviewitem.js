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
	
	_openSettings: function(resourceItem) {
		switch(this.options.resourceType) {
		case 'pages':
			Page.findOne({id: resourceItem.attr('id')}, $.proxy(function(page) {
    			var locked = page.isLocked();
            	var userLocked = page.isLockedUser(this.options.runtime.getUserLogin());
            	if(locked && !userLocked) {
            		this.element.trigger('showErrorMessage', "Can't edite page settings " + page.getPath() + ": Page is locked!");
            		return;
            	} else if(locked && userLocked) {
            		$('#wbl-pageheadeditor').editor_pageheadeditor({page: page, language: this.options.language, runtime: this.options.runtime});
            	} else {
		    		page.lock(this.options.runtime.getUserLogin(), $.proxy(function() {
		    			var editor = $('#wbl-pageheadeditor').editor_pageheadeditor({page: page, language: this.options.language, runtime: this.options.runtime});
		    			editor.one("closeeditor", $.proxy(function(event, ui) {
							page.unlock(function(){}, $.proxy(function() {
								this.element.trigger('showErrorMessage', "Can't unlock page " + page.getPath() + "!");
							}, this));
	    				}, this));
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
				success: function() {
					resourceItem.parents('#wbl-mainContainer').trigger('updateLastMedia');
				}
			});
			break;
		}
	}

  });

});
