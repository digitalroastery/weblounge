steal.plugins('jquery',
		'jquery/controller/view',
		'jquery/view',
		'jquery/view/tmpl',
		'jquery/event/hover',
		'jqueryui/widget',
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
				width: 900,
				height: 800,
				buttons: {
					Abbrechen: function() {
						$(this).dialog('close');
					},
					Upload: $.proxy(function() {
						this._openTagDialog();
						this.element.dialog('destroy');
						this.destroy();
					},this)
				},
				close: $.proxy(function () {
					$.each(this.map, $.proxy(function(key, value) {
						this._deleteFile(value.resourceId);
					},this));
					
//					this.divScroll.smoothDivScroll('destroy');
					this.element.dialog('destroy');
					this.destroy();
				},this)
			});
			
			this.map = new Array();
			
			var uploader = new qq.FileUploader({
			    // pass the dom node (ex. $(selector)[0] for jQuery users)
			    element: document.getElementById('wbl-fileUploader'),
			    params: {language: this.options.language},
				// validation    
				// ex. ['jpg', 'jpeg', 'png', 'gif'] or []
				allowedExtensions: [],        
				// each file size limit in bytes
				// this option isn't supported in all browsers
				sizeLimit: 0, // max size   
				minSizeLimit: 0, // min size
				// onCancel: this._cancel,
				onComplete: $.proxy(function(id, fileName, response) {
					if($.isEmptyObject(response)) return;
					this.map.push({resourceId: response.url.substring(response.url.lastIndexOf('/') + 1), eTag: response.eTag});
					this._loadImage(response.url + '/content/' + this.options.language);
					this._updateUploadButton();
			    }, this),
			    // path to server-side upload script
			    action: '/system/weblounge/files/uploads'
			});
			
			this.divScroll = this.element.find('div#wbl-makeMeScrollableImage').smoothDivScroll({
			  	autoScroll: "onstart" ,
				autoScrollDirection: "left", 
				autoScrollStep: 1, 
				autoScrollInterval: 15,
				scrollableArea:	"div.wbl-scrollableImageArea",
				visibleHotSpots: "always"
		  	});
			
			this.uploadButton = this.element.parent().find(".ui-dialog-buttonpane button:contains('Upload')").button('disable');
	    },
	    
	    update: function(options) {
	    	this.options = options;
	    	this.element.dialog('open');
	    },
	    
	    _updateUploadButton: function() {
	    	if(this.map.length > 0) {
	    		this.uploadButton.button('enable');
	    	} else {
	    		this.uploadButton.button('disable');
	    	}
	    },
	    
	    _deleteFile: function(resourceId) {
	    	Editor.File.destroy({id: resourceId});
	    },
	    
	    _openTagDialog: function() {
	    	$('div#wbl-tagger').editor_tagger({map: this.map, language: this.options.language, runtime: this.options.runtime});
	    },
	    
        _loadImage: function(url) {
        	if (url) {
        		var divScroll = this.divScroll;
        		var image = new jsu.PreloadImage({
        			url: url,
        			containerId: "wbl-previewImageContainer",
        			onLoad: function(img_data) {
        				var element = image.getElement();
        				image.setSize(200, -1);
        				$(element).wrap('<div class="wbl-previewImage" />');
        				$(element).parent().append('<a class="wbl-removeButton" />');
        				$(element).parent().find('.wbl-removeButton').hide();
        				divScroll.smoothDivScroll('recalculateScrollableArea');
        				$(element).parent().hover(
							function () {
								$(this).find('.wbl-removeButton').show();
							},
							function () {
								$(this).find('.wbl-removeButton').hide();
							}
						);
        			}
                });
            }
        },
        
        "a.wbl-removeButton click": function(el, ev) {
        	var index = el.parent().index();
        	el.parent().remove();
        	this._deleteFile(this.map[index].resourceId);
        	this.map.splice(index, 1);
        	this._updateUploadButton();
        }
        
  	});
});
