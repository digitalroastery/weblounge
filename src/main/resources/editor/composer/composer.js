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
        	
            var newComposer = $.map(this.element.find('.pagelet'), function(elem, i) {
            	var oldIndex = $(elem).attr("index");
            	return page.getPagelet(composerId, oldIndex);
            });
            if(ui.item.attr('class').indexOf("draggable") != -1) {
            	var newPagelet = {};
            	newPagelet.id = ui.item.attr('id');
            	newPagelet.module = ui.item.attr('module')
            	newComposer.splice(ui.item.index(), 0, newPagelet);
        	}
            
            this.options.page.updateComposer(this.id, newComposer);
            this.update(this.options);
            
            if(ui.item.attr('class').indexOf("draggable") != -1) {
            	Workbench.findOne({ id: this.options.page.value.id, composer: this.id, pagelet: ui.item.index() }, $.proxy(function(pageletEditor) {
            		var renderer = $(pageletEditor).find('renderer')[0].firstChild.nodeValue.trim();
            		ui.item.after('<div class="pagelet editor_pagelet" index="' + ui.item.index() + '">');
            		var newPagelet = ui.item.next().html(renderer);
            		ui.item.remove();
            		this.update(this.options);
            		// TODO Simulate Click event for opening Dialog
            		newPagelet.find('img.icon_editing').trigger('click');
            	}, this));
        	}
		}, this),
      });
      
      $(el).find('div.pagelet').editor_pagelet({
        composer: {
          id: this.id,
          page: this.options.page,
          language: this.options.language,
          runtime: this.options.runtime
        }
      });

    },
    
    update: function(options) {
    	if(options === undefined) return;
    	this.options = options;
    	this.element.find('div.pagelet').editor_pagelet({
        composer: {
            id: this.id,
            page: this.options.page,
            language: this.options.language,
            runtime: this.options.runtime
          }
        });
    }
    
  });

});
