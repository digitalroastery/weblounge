$.Controller("Editor.Download",
/* @static */
{
},

/* @prototype */
{
    /**
     * Initialize a new DownloadEditor controller.
     */
    init: function(el) {
    	var resourceId = this.element.find('input#wbl-downloadFile');
    	var previewImage = this.element.find('img');
    	if(resourceId.val() != '') {
    		previewImage.attr('src', '/system/weblounge/previews/' + resourceId.val() + '/locales/' + this.options.language + '/styles/editorpreview').show();
    	}
    },
    
    "button#wbl-downloadFileButton click": function(el, ev) {
    	var input = this.element.find('input#wbl-downloadFile');
    	var language = this.options.language;
    	var previewImage = this.element.find('img');
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'media', false, function(selectedMedia) {
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