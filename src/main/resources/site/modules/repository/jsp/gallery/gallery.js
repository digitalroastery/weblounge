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
    	var previewImage = this.element.find('img');
    	if(imageIds != '') {
    		previewImage.attr('src', '/system/weblounge/previews/' + imageIds.split(',')[0] + '/locales/' + this.options.language + '/styles/editorpreview').show();
    	}
    },
    
    "button#wbl-galleryFilesButton click": function(el, ev) {
    	var input = this.element.find('input#wbl-galleryFiles');
    	var language = this.options.language;
    	var previewImage = this.element.find('img');
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'images', true, function(selectedMedia) {
    		if(selectedMedia == null) {
    			previewImage.hide();
    			return;
    		}
    		var ids = new Array();
    		$.each(selectedMedia, function(index, elem) {
    			var id = elem.value.id;
    			ids.push(id);
    		});
    		input.val(ids);
    		previewImage.attr('src', '/system/weblounge/previews/' + ids[0] + '/locales/' + language + '/styles/editorpreview').show();
    	});
    },
    
});