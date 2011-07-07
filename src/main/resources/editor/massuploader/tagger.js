steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl',
		'jqueryui/core',
		'jqueryui/widget', 
		'jqueryui/position', 
		'jqueryui/dialog',
		'jqueryui/draggable',
		'jqueryui/resizable',
		'jqueryui/mouse')
.views('//editor/massuploader/views/tagger.tmpl')
.then(function($) {
	
	$.Controller("Editor.Tagger",	
	/* @static */
	{
  	},
  	/* @prototype */
  	{
		/**
		 * Initialize a new MassUploader controller.
		 */
		init: function(el) {
			// TODO Beim Upload der Bilder denk er es ist ein File weil kein mimetype gesetzt wird!!!
			$(el).html('//editor/massuploader/views/tagger.tmpl', {map : this.options.map, language: this.options.language});
			this.element.dialog({
				modal: true,
				title: 'Metadaten eingeben',
				autoOpen: true,
				resizable: true,
				buttons: this.options.buttons,
				width: 900,
				height: 800,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					Fertig: $.proxy(function () {
						this.element.dialog('close');
					},this)
				},
			});
	    },
	    
	    "div#metadata input[name=title] click": function(el, ev) {
	    	steal.dev.log('title');
	    },
	    
	    "div#metadata input[name=description] click": function(el, ev) {
	    	steal.dev.log('description');
	    },
	    
	    "div#metadata input[name=tags] click": function(el, ev) {
	    	steal.dev.log('tags');
	    },
	    
	    "div#metadata input[name=author] click": function(el, ev) {
	    	steal.dev.log('author');
	    },
	    
  	});
});
