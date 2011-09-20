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
    	var imageId = this.element.find('input#wbl-imageId').val();
    	if(imageId != '') {
    		this._setImage(imageId);
    	}
    },
    
    _setImage: function(imageId) {
    	this.element.find('img').error(function() {
    		$(this).attr('src', '/weblounge/editor/resourcebrowser/images/empty_thumbnail.png');
    	}).attr('src', '/system/weblounge/previews/' + imageId + '/locales/' + this.options.language + '/styles/editorpreview').show();
    },
    
    "button#wbl-imageFileButton click": function(el, ev) {
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'image', false, $.proxy(function(selectedMedia) {
    		if(selectedMedia == null) {
    			this.element.find('img').hide();
    			return;
    		}
    		var id = selectedMedia[0].value.id;
			var title = selectedMedia[0].getTitle(this.options.language);
			var desc = selectedMedia[0].getDescription(this.options.language);
    		var author = selectedMedia[0].value.head.created.user.name;
    		
			this.element.find('input#wbl-imageId').val(id);
    		this._setImage(id);
    		
	    	var inputTitle = this.element.find('input#wbl-imageTitle');
	    	var inputDesc = this.element.find('input#wbl-imageDesc');
	    	var inputPhotographer = this.element.find('input#wbl-imagePhotographer');
	    	
			if(title != '' && inputTitle.val() == '') inputTitle.val(title);
			if(desc != '' && inputDesc.val() == '') inputDesc.val(desc);
			if(author != '' && inputPhotographer.val() == '') inputPhotographer.val(author);
    	}, this));
    },
    
});