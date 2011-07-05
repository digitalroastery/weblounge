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
.resources('fileuploader', 'jsupload/jsupload.nocache')
.then('tagger')
.then(function($) {
	
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
						this._openTagDialog();
						this.element.dialog('close');
					},this)
				},
			});
			
			var uploader = new qq.FileUploader({
			    // pass the dom node (ex. $(selector)[0] for jQuery users)
			    element: document.getElementById('file-uploader'),
			    params: {languageId: this.options.language},
				// validation    
				// ex. ['jpg', 'jpeg', 'png', 'gif'] or []
				allowedExtensions: [],        
				// each file size limit in bytes
				// this option isn't supported in all browsers
				sizeLimit: 0, // max size   
				minSizeLimit: 0, // min size
				onCancel: this._cancel,
				onComplete: $.proxy(function(id, fileName, response) {
					this._loadImage(response.url + '/content/' + this.options.language);
			    }, this),
			    // path to server-side upload script
			    action: '/system/weblounge/files/uploads',
			});
	    },
	    
	    update: function(options) {
	    	this.options = options;
	    	this.element.dialog('open');
	    },
	    
	    _cancel: function(id, fileName) {
	    	steal.dev.log(id + fileName);
	    },
	    
	    _deleteFile: function(upl_data) {
	    	// REST Delete Image
	    },
	    
	    _openTagDialog: function() {
	    	$('<div></div>').editor_tagger({});
	    },
	    
        _loadImage: function(url) {
        	if (url) {
        		var image = new jsu.PreloadImage({
        			url: url,
        			containerId: "qq-file-preview",
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
        
  	});
});
