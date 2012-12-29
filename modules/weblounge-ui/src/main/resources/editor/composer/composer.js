steal.plugins('jquery/controller',
		'jquery/event/hover',
		'jqueryui/sortable',
		'jqueryui/mouse')
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
      this.element.addClass('wbl-nojQuery');  // class draws a border round all composers
      
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
        	this.element.find('i.wbl').remove(); // remove all editing-icons
        	this._disablePagelets();
        	if(ui.item.hasClass('wbl-draggable')) return;
        	
        	// add pageletData to draggable helper
        	var index = ui.item.attr('index');
        	var test = ui.item.next();
        	var pagelet = this.options.page.getPagelet(this.id, index);
        	var copyPagelet = jQuery.extend(true, {}, pagelet);
        	ui.helper.data('pagelet', copyPagelet);
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
            	var newPagelet = {
            		id: ui.item.attr('id'),
            		module: ui.item.attr('module'),
            		locale: new Array(),
            		properties: {property: {}},
            		created: {
	    				user: {
	    					id: this.options.runtime.getUserLogin(),
	    					name: this.options.runtime.getUserName(),
	    					realm: this.options.runtime.getId()
	    				},
	    				date: new Date()
            		}
            	};
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
            	Workbench.getPageletEditor({ id: page.value.id, language: this.options.language, composer: composerId, pagelet: pagelet.attr('index') }, $.proxy(function(pageletEditor) {
            		if(!$(pageletEditor).find('editor:first').length) {
            			pagelet.addClass('wbl-noEditor');
            		}
            		pagelet.editor_pagelet('_openPageEditor', pageletEditor, true);
            	}, this));
        	}
		}, this)
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
    
    update: function() {
    	var pagelets = this.element.find('div.pagelet');
    	if(!pagelets.length) {
    		this.element.addClass('empty');
			return;
    	}
		this.element.removeClass('empty');
		
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
        // fired for each composer on the page
		$(this.element).removeClass('wbl-nojQuery');
		$(this.element).sortable('disable');
		$(this.element).find('div.pagelet').editor_pagelet('disable').css('min-height', '');
	},
	
	enable: function() {
        // fired for each composer on the page
		$(this.element).addClass('wbl-nojQuery');
		$(this.element).sortable('enable');
		$(this.element).find('div.pagelet').editor_pagelet('enable').css('min-height', '35px');
	},
	
	hideGhostComposer: function() {
		$('div.#' + this.id + '-ghost').hide();
		$(this.element).show();
	},
	
	handleGhostComposer: function() {
		if($(this.element).find('div.pagelet').length > 0) {
			$('div.#' + this.id + '-ghost').hide();
		} else {
			$('div.#' + this.id + '-ghost').show();
			$(this.element).hide();
		}
	},
    
    _enablePagelets: function() {
    	$('.composer:not(.locked)').editor_composer('enable');
    },
    
    _disablePagelets: function() {
    	$('.composer:not(.locked)').editor_composer('disable');
    },

	'hoverenter': function(el, ev) {
		if(!$(this.element).hasClass('wbl-nojQuery')) return;
		$(this.element).addClass('wbl-composerBorder');
    },
    
	'hoverleave': function(el, ev) {
		if(!$(this.element).hasClass('wbl-nojQuery')) return;
    	if($(this.element).hasClass('empty')) return;
    	$(this.element).removeClass('wbl-composerBorder');
    }
    
  });

});
