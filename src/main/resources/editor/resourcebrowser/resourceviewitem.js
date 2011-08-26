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
		if(this.options.mode != 'normal') return;
		el.trigger('openDesigner');
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
			$('div#wbl-tagger').editor_tagger({
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
