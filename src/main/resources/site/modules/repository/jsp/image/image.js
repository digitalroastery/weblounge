$.Controller("Editor.Image",
/* @static */
{
},

/* @prototype */
{
    /**
     * Initialize a new Image Editor controller.
     */
    init: function(el) {
    	var imageId = this.element.find('input#wbl-imageId');
    	var previewImage = this.element.find('img');
    	if(imageId.val() != '') {
    		previewImage.attr('src', '/system/weblounge/previews/' + imageId.val() + '/locales/' + this.options.language + '/styles/editorpreview').show();
    	}
    },
    
    "button#wbl-imageFileButton click": function(el, ev) {
    	var input = this.element.find('input#wbl-imageId');
    	var language = this.options.language;
    	var previewImage = this.element.find('img');
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'images', false, function(selectedMedia) {
    		if(selectedMedia == null) {
    			previewImage.hide();
    			return;
    		}
    		var id = selectedMedia[0].value.id;
    		input.val(id);
    		previewImage.attr('src', '/system/weblounge/previews/' + id + '/locales/' + language + '/styles/editorpreview').show();
    	});
    },
    
});