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
.views('//editor/massuploader/views/init.tmpl')
.css('massuploader', 'fileuploader')
.resources('jsupload/jsupload.nocache', 'fileuploader').then(function($) {
	
 	function jsuOnLoad() {
	 	steal.dev.log('test');
 	}

	$.Controller("Editor.Massuploader",	
	/* @static */
	{
		defaults: {
			max_file_size: '10mb',
			resize: {
				width: 1600,
				height: 1200,
				quality: 90
			}
	    }
  	},
  	/* @prototype */
  	{
		/**
		 * Initialize a new MassUploader controller.
		 */
		init: function(el) {
			$(el).html('//editor/massuploader/views/init.tmpl', {});
			this.element.dialog({
				modal: true,
				title: 'Medien upload',
				autoOpen: true,
				resizable: true,
				buttons: this.options.buttons,
				width: 900,
				height: 800,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					Upload: $.proxy(function () {
						this.element.dialog('close');
					},this)
				},
			});
			
			var uploader = new qq.FileUploader({
			    // pass the dom node (ex. $(selector)[0] for jQuery users)
			    element: document.getElementById('file-uploader'),
			    // path to server-side upload script
			    action: '/system/weblounge/files/uploads?path=/test/&languageId=fr'
			});
	    },
	    
	    deleteFile: function(upl_data) {
	    	// REST Delete Image
	    },
	    
	    openTagDialog: function(upl_data) {
	    	alert(upl_data.url);
	    },
	    
        // Method to show a picture using the class PreloadImage
        // The image is not shown until it has been sucessfully downloaded
        loadImage: function(upl_data) {
        	// REST CREATE Image update
        	if (upl_data && upl_data.url) {
        		var image = new jsu.PreloadImage({
        			url: upl_data.url,
        			containerId: "photos",
        			onLoad: function(img_data) {
        				image.setSize(100, -1);
        			}
                });
            }
        },
        
//		ready : function() {
////			this.options.resize = this.callback("resize");
////			this.options.close = this.callback("close");
////			this.element.dialog(this.options);
//		},
//		
//		resize : function(event, ui) {
//		},
//		
//		close : function(even, ui) {
//			alert("Dialog closed");
//		},
//		
//		destroy : function() {
//			this.element.dialog("destroy");
//		},
//		
//		"input change": function(el, ev) {
//			steal.dev.log('test');
//		},
        
	    "#drop-area dragleave": function(el, ev) {
			var target = ev.target;
			
			if (target && target === this.dropArea) {
				this.className = "";
			}
			ev.preventDefault();
			ev.stopPropagation();
	    },
	    
	    "#drop-area dragenter": function(el, ev) {
			this.className = "over";
			ev.preventDefault();
			ev.stopPropagation();
	    },
	    
	    "#drop-area dragover": function(el, ev) {
			ev.preventDefault();
			ev.stopPropagation();
	    },
	    
	    "input[type=file] change": function(el, ev, bla) {
	    	steal.dev.log('adsfadf');
	    },
	    
	    "#drop-area drop": function(el, ev) {
//	    	this.find("input[type=file]:first").val("test.jpg");
//	    	this.find("input[type=file]:first").trigger('change');
//	    	this.find("input[type=file]");
//			this.traverseFiles(ev.dataTransfer.files);
	    	this.className = "";
	    	ev.preventDefault();
	    	ev.stopPropagation();
	    }
  	});
});
