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
    	
    },
    
    "button#wbl-downloadFileButton click": function(el, ev) {
    	var input = this.element.find('input#wbl-downloadFile');
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', 'media', false, function(selectedMedia) {
    		if(selectedMedia == null) return;
			input.val(selectedMedia);
    	});
    },
    
});

$('#wbl-pageleteditor div#wbl-downloadEditor').editor_download();