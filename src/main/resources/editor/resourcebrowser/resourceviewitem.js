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
    
	"a.wbl-pagePath click": function(el, ev) {
		ev.preventDefault();
		el.trigger('openDesigner');
	},
	
	_openSettings: function(resourceId) {
		switch(this.options.resourceType) {
		case 'pages':
			Page.findOne({id: resourceId}, $.proxy(function(page) {
				$('#wbl-pageheadeditor').editor_pageheadeditor({page: page, language: this.options.language, runtime: this.options.runtime});
			}, this));
			break;
		case 'media':
			var map = new Array({resourceId: resourceId});
			$('div#wbl-tagger').editor_tagger({map: map, language: this.options.language, runtime: this.options.runtime});
			break;
		}
	}

  });

});
