steal.plugins('jqueryui/sortable').then('pagelet').models('../../models/page').css('composer').then(function($) {

  $.Controller("Editor.Composer",
  /* @static */
  {
    defaults: {
      connectWith: ".composer"
    }
  },

  /* @prototype */
  {
    /**
     * Initialize a new Composer controller.
     */
    init: function(el) {
      // get this composers id
      Page.getFromPath({path: location.pathname}, this.callback('_setPage'));
      
      this.id = this.element.attr('id');
      steal.dev.log('init composer ' + this.id);

      // init jQuery UI sortable plugin to support drag'n'drop of pagelets
      $(el).sortable({
        connectWith: this.options.connectWith,
        distance: 15,
        update: $.proxy(function () {
        	Page.update({id:this.page.id}, this.page, {});
		},this)
      });
      
      $(el).find('div.pagelet').editor_pagelet({
        composer: {
          id: this.id
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
    
    _setPage: function(page) {
    	this.page = page;
    }
    
  });

});
