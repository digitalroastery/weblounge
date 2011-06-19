steal.plugins('jqueryui/sortable').then('pagelet').css('composer').then(function($) {

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
      this.id = this.element.attr('id');
      steal.dev.log('init composer ' + this.id);

      // init jQuery UI sortable plugin to support drag'n'drop of pagelets
      $(el).sortable({
        connectWith: this.options.connectWith,
        distance: 15
      });

      $(el).find('div.pagelet').editor_pagelet({
        composer: {
          id: this.id
        }
      });
    }
  });

});
