steal.plugins('jquery/controller','jqueryui/sortable')
.models('../../models/page')
.css('composer')
.then('pagelet')
.then(function($) {

  $.Controller("Editor.Composer",
  /* @static */
  {
    defaults: {
      connectWith: ".composer",
      page: {}
    }
  },

  /* @prototype */
  {
    /**
     * Initialize a new Composer controller.
     */
    init: function(el) {
      this.id = this.element.attr('id');
      
      // init jQuery UI sortable plugin to support drag'n'drop of pagelets
      $(el).sortable({
        connectWith: this.options.connectWith,
        distance: 15,
        placeholder: "pagelet-placeholder",
        items: 'div.pagelet',
        tolerance: 'pointer',
        cursor: 'move',
        cursorAt: { top: -8, left: -10 },
        revert: true,
        update: $.proxy(function(event, ui) {
        	if(ui.sender != null) return;
        	var page = this.options.page;
        	var composerId = this.id;
        	
        	var newComposer = [];
        	
        	if(this.element.hasClass('empty')) {
        		this.options.page.createComposer(this.id);
        	}
        	else {
        		newComposer = $.map(this.element.find('.pagelet'), function(elem, i) {
        			var oldIndex = $(elem).attr("index");
        			return page.getPagelet(composerId, oldIndex);
        		});
        	}
        	
            if(ui.item.hasClass('draggable')) {
            	var newPagelet = {};
            	newPagelet.id = ui.item.attr('id');
            	newPagelet.module = ui.item.attr('module')
            	newPagelet.locale = new Array();
            	newPagelet.properties = {};
            	newPagelet.properties.property = {};
            	newPagelet.created = {};
            	newPagelet.created.user = {};
            	newPagelet.created.user.id = this.options.runtime.getUserLogin();
            	newPagelet.created.user.name = this.options.runtime.getUserName();
            	newPagelet.created.user.realm = this.options.runtime.getId();
            	newPagelet.created.date = new Date();
            	newComposer.splice(ui.item.index(), 0, newPagelet);
            	ui.item.after('<div class="pagelet editor_pagelet" >');
            	var pagelet = ui.item.next();
            	ui.item.remove();
        	}
            
            this.options.page.updateComposer(this.id, newComposer);
            this.update();
            
            // Index false
            if(ui.item.hasClass('draggable')) {
            	Workbench.findOne({ id: this.options.page.value.id, composer: this.id, pagelet: pagelet.attr('index') }, $.proxy(function(pageletEditor) {
            		pagelet.editor_pagelet('_openPageEditor', pageletEditor, true);
            	}, this));
        	}
		}, this)
      });
      
      if(this.element.hasClass('empty')) {
    	  this.element.append('<a class="add-pagelet" />');
    	  return;
      }
      
      $(el).find('div.pagelet').editor_pagelet({
        composer: {
          id: this.id,
          page: this.options.page,
          language: this.options.language,
          runtime: this.options.runtime
        }
      });

    },
    
    update: function() {
    	var pagelets = this.element.find('div.pagelet');
    	if(!pagelets.length) {
    		this.element.append('<a class="add-pagelet" />');
    		this.element.addClass('empty');
    		return;
    	}
		this.element.removeClass('empty');
		this.element.find('a.add-pagelet').remove();
		
    	pagelets.editor_pagelet({
        composer: {
            id: this.id,
            page: this.options.page,
            language: this.options.language,
            runtime: this.options.runtime
          }
        });
    },
    
    "a.add-pagelet click": function(el, ev) {
    	$('#pageletcreator').editor_pageletcreator({language: this.options.language, runtime: this.options.runtime});
    }
    
  });

});
