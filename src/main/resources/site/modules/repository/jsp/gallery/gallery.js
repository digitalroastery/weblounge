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
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'media', true, function(selectedMedia) {
    		if(selectedMedia == null) return;
    		var ids = new Array();
    		$.each(selectedMedia, function(index, elem) {
    			var id = elem.value.id;
    			ids.push(id);
    		});
    		input.val(ids);
    	});
    },
    
});

$('#wbl-pageleteditor div#wbl-galleryEditor').editor_gallery();