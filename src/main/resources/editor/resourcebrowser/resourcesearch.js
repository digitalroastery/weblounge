steal.plugins('jquery/view/tmpl', 'jquery/event/key')
.views('//editor/resourcebrowser/views/resourcepagessearch.tmpl', '//editor/resourcebrowser/views/resourcemediasearch.tmpl')
.then(function($) {

	$.Controller('Editor.Resourcesearch', 
	{	
		init: function(el) {
			if(this.options.resourceType == 'pages') {
				$(el).html('//editor/resourcebrowser/views/resourcepagessearch.tmpl', {});
			}
			else if(this.options.resourceType == 'media') {
				$(el).html('//editor/resourcebrowser/views/resourcemediasearch.tmpl', {});
				
				this.map = new Array();
				
				var uploader = new qq.FileUploader({
				    // pass the dom node (ex. $(selector)[0] for jQuery users)
				    element: document.getElementById('media-file-uploader'),
				    params: {language: this.options.language},
					// validation    
					// ex. ['jpg', 'jpeg', 'png', 'gif'] or []
					allowedExtensions: [],        
					// each file size limit in bytes
					// this option isn't supported in all browsers
					sizeLimit: 0, // max size   
					minSizeLimit: 0, // min size
					onCancel: this._cancel,
					onComplete: $.proxy(function(id, fileName, response) {
						if($.isEmptyObject(response)) return;
						this.map[id] = response.url.substring(response.url.lastIndexOf('/') + 1);
						// TODO Tag Button
						if(!this.element.find('#tag_button').length) {
							this.element.find('.qq-upload-button').after('<button id="tag_button">Tag</button>');
						}
				    }, this),
				    // path to server-side upload script
				    action: '/system/weblounge/files/uploads'
				});
			}
			
			$("input#resourceSearch").keypress(function(ev){
				if(ev.key() == '\r') {
					ev.preventDefault();
					if(this.options.resourceType == 'pages') {
						$(this).trigger('searchPages', $(this).val());
					}
					else if(this.options.resourceType == 'media') {
						$(this).trigger('searchMedia', $(this).val());
					}
				}
			});
		},
		
	    _cancel: function(id, fileName) {
	    	// TODO ???
	    	steal.dev.log(id + fileName);
	    },
	    
	    "button#tag_button click": function(el, ev) {
	    	$('div#tagger').editor_tagger({map: this.map, language: this.options.language, runtime: this.options.runtime});
	    }
		
	});

});
