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
        var language = localStorage["weblounge.editor.language"];
        if (!language) {
        	location.href = el.attr('href') + "?edit";
        }
        else {
        	location.href = el.attr('href') + language + "?edit";
        }
	}

  });

});
