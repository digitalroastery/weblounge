steal.plugins('jquery/controller',
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
.then(function($) {   

	$.Controller('Editor.Dialog', {       
		/* @Static */
	    defaults:{
	    	title: "A dialog"
	    }
	}, /* @Prototype */
	{
		init: function(el) {
			this.element.dialog({
				modal: true,
				title: this.options.title,
				autoOpen: true,
				resizable: true,
				buttons: this.options.buttons,
				width: this.options.width,
				height: this.options.height,
			});
		},
	
		ready : function() {
//			this.options.resize = this.callback("resize");
//			this.options.close = this.callback("close");
//			this.element.dialog(this.options);
		},
		
		resize : function(event, ui) {
		},
		
		close : function(even, ui) {
			alert("Dialog closed");
		},
		
		destroy : function() {
			this.element.dialog("destroy");
		},
		
		"input change": function(el, ev) {
			steal.dev.log('test');
		},
		
	});
});