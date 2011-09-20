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
    	var resourceId = this.element.find('input#wbl-downloadFile').val();
    	if(resourceId != '') {
    		this._setImage(resourceId);
    	}
    },
    
    _setImage: function(resourceId) {
    	this.element.find('img').error(function() {
    		$(this).attr('src', '/weblounge/editor/resourcebrowser/images/empty_thumbnail.png');
    	}).attr('src', '/system/weblounge/previews/' + resourceId + '/locales/' + this.options.language + '/styles/editorpreview').show();
    },
    
    "button#wbl-downloadFileButton click": function(el, ev) {
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'media', false, $.proxy(function(selectedMedia) {
    		if(selectedMedia == null) {
    			this.element.find('img').hide();
    			return;
    		}
    		var id = selectedMedia[0].value.id;
			var title = selectedMedia[0].getTitle(this.options.language);
			var desc = selectedMedia[0].getDescription(this.options.language);
    		
    		this.element.find('input#wbl-downloadFile').val(id);
    		this._setImage(id);
    		
	    	var inputTitle = this.element.find('input#wbl-downloadTitle');
	    	var inputDesc = this.element.find('input#wbl-downloadDesc');
			if(title != '' && inputTitle.val() == '') inputTitle.val(title);
			if(desc != '' && inputDesc.val() == '') inputDesc.val(desc);
    	}, this));
    },
    
});