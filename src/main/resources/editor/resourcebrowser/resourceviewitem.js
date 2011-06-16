steal.plugins().then(function($) {

 /*
  * 
  */
  $.Controller('Editor.Resourceviewitem', {
    defaults: {
  		page: {}
    }
  },

  {

    init: function(el) {
		//TODO Liste mit SelektiertenPages
		// Löschfunktionen usw. hier drin
    },
    
    _selectPage: function() {
    	
    },
    
	"a.pagePath click": function(el, ev) {
		ev.preventDefault();
		el.trigger('showDesigner', [this.options.page.id, el.attr('href')]);
	}

  });

});
