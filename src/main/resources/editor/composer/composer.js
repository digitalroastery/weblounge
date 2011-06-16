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
      $(el).sortable({
        connectWith: this.options.connectWith,
        distance: 15
      });

      $(el).find('div.pagelet').editor_pagelet();

      $(el).bind('sortupdate', function(event, ui) {

      });
    }
  });

});
