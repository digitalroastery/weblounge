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
    
    _selectPage: function() {
    	
    },
    
	"a.pagePath click": function(el, ev) {
		// Open Designer from clicked PageUrl
		ev.preventDefault();
		el.trigger('showDesigner', [this.options.page.id, el.attr('href')]);
	}

  });

});
