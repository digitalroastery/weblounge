$.Controller("Editor.Gallery",
/* @static */
{
},

/* @prototype */
{
    /**
     * Initialize a new Gallery Editor controller.
     */
    init: function(el) {
    	var imageIds = this.element.find('input#wbl-galleryFiles').val();
    	if(imageIds != '') {
    		this._setImage(imageIds.split(',')[0]);
    	}
    },
    
    _setImage: function(imageId) {
    	this.element.find('img').error(function() {
    		$(this).attr('src', '/weblounge/editor/resourcebrowser/images/empty_thumbnail.png');
    	}).attr('src', '/system/weblounge/previews/' + imageId + '/locales/' + this.options.language + '/styles/editorpreview').show();
    },
    
    "button#wbl-galleryFilesButton click": function(el, ev) {
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'image', true, $.proxy(function(selectedMedia) {
    		if(selectedMedia == null) {
    			this.element.find('img').hide();
    			return;
    		}
    		var ids = new Array();
    		$.each(selectedMedia, function(index, elem) {
    			var id = elem.value.id;
    			ids.push(id);
    		});
    		
    		this.element.find('input#wbl-galleryFiles').val(ids);
    		this._setImage(ids[0]);
    	}, this));
    },
    
});