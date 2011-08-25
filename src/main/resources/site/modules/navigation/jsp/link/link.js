$.Controller("Editor.Link",
/* @static */
{
},

/* @prototype */
{
    /**
     * Initialize a new LinkEditor controller.
     */
    init: function(el) {
    	this.element.find('div.wbl-linkInternal, p.wbl-linkExternal').hide();
    	this.element.find('select#wbl-linkType').change();
    },
    
    "select#wbl-linkType change": function(el, ev) {
    	var type = $(el).val();
    	this.element.find('div.wbl-linkInternal, p.wbl-linkExternal').hide();
    	if(type == 'external') {
    		this.element.find('p.wbl-linkExternal').show();
    	} else if(type == 'internal') {
    		this.element.find('div.wbl-linkInternal').show();
    	}
    },
    
    "button#wbl-linkInternalButton click": function(el, ev) {
    	var input = this.element.find('input#wbl-linkInternal');
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', 'pages', false, function(selectedPage) {
    		if(selectedPage == null) return;
			input.val(selectedPage);
    	});
    },
    
    "input#wbl-linkExternal change": function(el, ev) {
    	alert('validate link');
    	// URL mit Linkchecker Rot Grün
    }
    
});

$('#wbl-pageleteditor div#wbl-linkEditor').editor_link();