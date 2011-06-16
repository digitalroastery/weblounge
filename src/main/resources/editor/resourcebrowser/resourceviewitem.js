steal.plugins().then(function($) {

 /*
  * 
  */
  $.Controller('Editor.Resourceviewitem', {
    defaults: {
      resource: {}
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
		el.trigger('openDesigner', [this.pageId, el.attr('href')]);
	}

  });

});
