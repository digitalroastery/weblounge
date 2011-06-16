steal.then(function($) {

  $.Controller("Editor.Pagelet",
  /* @static */
  {
    
  },

  /* @prototype */
  {
    /**
     * Initialize a new Pagelet controller.
     */
    init: function(el) {
	
    },

	'hoverenter': 	function() {
      //log('pagelet hover in');
      this.element.append('<div class="icon_editing"></div>');
    },

	'hoverleave': 	function() {
      // log('pagelet hover out');
      this.element.find('div.icon_editing').remove();
    },

	'div.icon_editing click': function() {
		steal.dev.log('editing icon clicked.');
	}

  });

});
