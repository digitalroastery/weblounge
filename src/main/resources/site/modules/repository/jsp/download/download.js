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
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'media', false, function(selectedMedia) {
    		if(selectedMedia == null) return;
			input.val(selectedMedia[0].value.id);
    	});
    },
    
});