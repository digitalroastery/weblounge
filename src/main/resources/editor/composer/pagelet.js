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

	'hoverenter': 	function(ev, hover) {
      //log('pagelet hover in');
      this.element.append('<div class="icon_editing"></div>');
    },

	'hoverleave': 	function(ev, hover) {
      // log('pagelet hover out');
      this.element.find('div.icon_editing').remove();
    },

	'div.icon_editing click': function(ev) {
		steal.dev.log('editing icon clicked.');
	}

  });

});
