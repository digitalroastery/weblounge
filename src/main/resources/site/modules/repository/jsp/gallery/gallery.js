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
    	
    },
    
    "button#wbl-galleryFilesButton click": function(el, ev) {
    	var input = this.element.find('input#wbl-galleryFiles');
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', 'media', true, function(selectedMedia) {
    		if(selectedMedia == null) return;
			input.val(selectedMedia);
    	});
    },
    
});

$('#wbl-pageleteditor div#wbl-galleryEditor').editor_gallery();