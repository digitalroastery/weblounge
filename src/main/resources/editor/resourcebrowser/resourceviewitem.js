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
    },
    
	"a.pagePath click": function(el, ev) {
		ev.preventDefault();
		el.trigger('openDesigner');
	}

  });

});
