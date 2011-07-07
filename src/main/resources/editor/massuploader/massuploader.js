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
.models('../../models/file')
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
					Upload: $.proxy(function() {
						this._openTagDialog();
						this.element.dialog('close');
					},this)
				},
				close: $.proxy(function () {
//					this.divScroll.smoothDivScroll('destroy');
					this.element.dialog('destroy');
					this.destroy();
				},this)
			});
			
			this.map = new Array();
			
			var uploader = new qq.FileUploader({
			    // pass the dom node (ex. $(selector)[0] for jQuery users)
			    element: document.getElementById('file-uploader'),
			    // TODO MIME-TYPE ANPASSEN
			    params: {language: this.options.language, mimeType: 'image/png'},
				// validation    
				// ex. ['jpg', 'jpeg', 'png', 'gif'] or []
				allowedExtensions: [],        
				// each file size limit in bytes
				// this option isn't supported in all browsers
				sizeLimit: 0, // max size   
				minSizeLimit: 0, // min size
				onCancel: this._cancel,
				onComplete: $.proxy(function(id, fileName, response) {
					this.map[id] = response.url.substring(response.url.lastIndexOf('/') + 1);
					this._loadImage(response.url + '/content/' + this.options.language, id);
			    }, this),
			    // path to server-side upload script
			    action: '/system/weblounge/files/uploads',
			});
			
			this.divScroll = this.element.find('div#makeMeScrollableImage').smoothDivScroll({
			  	autoScroll: "onstart" ,
				autoScrollDirection: "backandforth", 
				autoScrollStep: 1, 
				scrollableArea:	"div.scrollableImageArea",
				autoScrollInterval: 15,
				visibleHotSpots: "always"
		  	});
	    },
	    
	    update: function(options) {
	    	this.options = options;
	    	this.element.dialog('open');
	    },
	    
	    _cancel: function(id, fileName) {
	    	steal.dev.log(id + fileName);
	    },
	    
	    _deleteFile: function(resourceId) {
	    	// Endpoint BUG
//	    	Editor.File.destroy({id: resourceId});
	    },
	    
	    _openTagDialog: function() {
	    	$('<div></div>').editor_tagger({map: this.map, language: this.options.language});
	    },
	    
        _loadImage: function(url, id) {
        	if (url) {
        		var divScroll = this.divScroll;
        		var image = new jsu.PreloadImage({
        			url: url,
        			containerId: "previewImageContainer",
        			onLoad: function(img_data) {
        				var element = image.getElement();
        				$(element).wrap('<div id="' + id + '" class="previewImage" />');
        				image.setSize(200, -1);
        				divScroll.smoothDivScroll('recalculateScrollableArea');
        			    $(element).bind('hoverinit', function(el, hover){
        			    	hover.leave(500); //wait until the mouse leaves for 1/2 a second
        			    	$(element).parent().append('<a class="removeButton" />');
        			    }).bind('hoverleave', function(){
        			    	if($(element).next()[0].tagName == "A") {
        			    		$(element).next().remove();
        			    	}
        			    })
        			}
                });
            }
        },
        
        "a.removeButton click": function(el, ev) {
        	var id = el.parent().attr('id');
        	el.parent().remove();
        	this._deleteFile(this.map[id]);
        	delete this.map[id];
        }
        
  	});
});
