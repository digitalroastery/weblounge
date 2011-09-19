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
    	
    	var pageId = this.element.find('input#wbl-linkInternal');
    	var previewImage = this.element.find('img');
    	if(pageId.val() != '') {
    		previewImage.attr('src', '/system/weblounge/previews/' + pageId.val() + '/locales/' + this.options.language + '/styles/editorpreview').show();
    	}
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
    	var language = this.options.language;
    	var previewImage = this.element.find('img');
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'pages', false, function(selectedPage) {
    		if(selectedPage == null) {
    			previewImage.hide();
    			return;
    		}
    		var id = selectedPage[0].value.id;
			input.val(id);
			previewImage.attr('src', '/system/weblounge/previews/' + id + '/locales/' + language + '/styles/editorpreview').show();
    	});
    },
    
    "input#wbl-linkExternal change": function(el, ev) {
    	// URL mit Linkchecker Rot Grün
    }
    
});