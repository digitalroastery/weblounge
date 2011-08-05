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
      this.element.addClass('wbl-nojQuery');
      
      // init jQuery UI sortable plugin to support drag'n'drop of pagelets
      $(el).sortable({
        connectWith: this.options.connectWith,
        distance: 15,
        placeholder: "wbl-pageletPlaceholder",
        items: 'div.pagelet',
        tolerance: 'pointer',
        cursor: 'move',
        cursorAt: { top: -8, left: -10 },
        revert: true,
        start: $.proxy(function(event, ui) {
        	this.element.find('img.wbl-iconEditing').remove();
        	this.element.find('img.wbl-iconRemove').remove();
        	
        	// add pageletData to draggable helper
        	var index = ui.item.attr('index');
        	var test = ui.item.next();
        	var pagelet = this.options.page.getPagelet(this.id, index);
        	var copyPagelet = jQuery.extend(true, {}, pagelet);
        	ui.helper.data('pagelet', copyPagelet);
        	this._disablePagelets();
        }, this),
        stop: $.proxy(function(event, ui) {
        	this._enablePagelets();
        }, this),
        update: $.proxy(function(event, ui) {
        	var page = this.options.page;
        	var composerId = this.id;
        	
        	// Create new composer when empty
        	if(this.element.hasClass('empty')) {
        		page.createComposer(composerId);
        	}
        	
        	var pagelets = jQuery.extend(true, [], page.getComposer(composerId).pagelets);
        	
        	// Insert new pagelet to composer
            if(ui.item.hasClass('wbl-draggable')) {
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
            	pagelets.splice(ui.item.index(), 0, newPagelet);
            	ui.item.after('<div class="pagelet editor_pagelet" >');
            	var pagelet = ui.item.next();
            	ui.item.remove();
        	} 
            else { // Iterate over Pagelets and insert to composer
        		pagelets = $.map(this.element.find('.pagelet'), function(elem, i) {
        			var oldIndex = $(elem).attr("index");
        			var pageletData = $(elem).data('pagelet');
        			if(pageletData != undefined) return pageletData;
        			return page.getPagelet(composerId, oldIndex);
        		});
        	}
            
            page.updateComposer(composerId, pagelets);
            this.update();
            
            // If pagelet is new open page editor dialog
            if(ui.item.hasClass('wbl-draggable')) {
            	Workbench.findOne({ id: page.value.id, composer: composerId, pagelet: pagelet.attr('index') }, $.proxy(function(pageletEditor) {
            		pagelet.editor_pagelet('_openPageEditor', pageletEditor, true);
            	}, this));
        	}
		}, this)
      });
      
      if(this.element.hasClass('empty')) {
    	  this.element.append('<a class="wbl-addPagelet" />');
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
    		this.element.append('<a class="wbl-addPagelet" />');
    		this.element.addClass('empty');
    		return;
    	}
		this.element.removeClass('empty');
		this.element.find('a.wbl-addPagelet').remove();
		
    	pagelets.editor_pagelet({
        composer: {
            id: this.id,
            page: this.options.page,
            language: this.options.language,
            runtime: this.options.runtime
          }
        });
    },
    
	disable: function() {
		$(this.element).removeClass('wbl-nojQuery');
		$(this.element).sortable('disable');
		$(this.element).find('div.pagelet').editor_pagelet('disable');
	},
	
	enable: function() {
		$(this.element).addClass('wbl-nojQuery');
		$(this.element).sortable('enable');
		$(this.element).find('div.pagelet').editor_pagelet('enable');
	},
    
    _enablePagelets: function() {
    	$('.composer').editor_composer('enable');
    },
    
    _disablePagelets: function() {
    	$('.composer').editor_composer('disable');
    },
    
    "a.wbl-addPagelet click": function(el, ev) {
    	$('#wbl-pageletcreator').editor_pageletcreator({language: this.options.language, runtime: this.options.runtime});
    }
    
  });

});
