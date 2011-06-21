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
        items: 'div.pagelet',
        update: $.proxy(function() {
        	var page = this.options.page;
        	var composerId = this.id;
        	
            var newComposer = $.map(this.element.find('.pagelet'), function(elem, i) {
            	var oldIndex = $(elem).attr("index");
            	return page.getPagelet(composerId, oldIndex);
            });
            
            this.options.page.updateComposer(this.id, newComposer);
            
		}, this)
      });
      
      $(el).find('div.pagelet').editor_pagelet({
        composer: {
          id: this.id,
          page: this.options.page
        }
      });

      $(el).bind('sortchange', function(event, ui) {
        steal.dev.log('position of pagelet changed');
      });

      $(el).bind('sortremove', function(event, ui) {
        steal.dev.log('pagelet removed to new composer');
      });

      $(el).bind('sortreceive', function(event, ui) {
        steal.dev.log('pagelet moved to new composer');
      });
    },
    
  });

});
