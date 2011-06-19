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
		this.index = this.element.index();
		steal.dev.log('init pagelet with index ' + this.index);
    },

	'hoverenter': 	function(ev, hover) {
      this.element.append('<div class="icon_editing"></div>');
    },

	'hoverleave': 	function(ev, hover) {
      this.element.find('div.icon_editing').remove();
    },

	'div.icon_editing click': function(ev) {
		steal.dev.log('editing pagelet with index ' + this.index + ' in composer ' + this.options.composer.id);
	}

  });

});
