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
    	
    	var pageId = this.element.find('input#wbl-linkInternal').val();
    	if(pageId != '') {
    		this._setImage(pageId);
    	}
    },
    
    _setImage: function(pageId) {
    	this.element.find('img').error(function() {
    		$(this).attr('src', '/weblounge/editor/resourcebrowser/images/empty_thumbnail.png');
    	}).attr('src', '/system/weblounge/previews/' + pageId + '/locales/' + this.options.language + '/styles/editorpreview').show();
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
    	$('div#wbl-menubar').editor_menubar('_editorSelectionMode', $('#wbl-pageleteditor'), 'pages', false, $.proxy(function(selectedPage) {
    		if(selectedPage == null) {
    			this.element.find('img').hide();
    			return;
    		}
    		var id = selectedPage[0].value.id;
			var title = selectedPage[0].getTitle(this.options.language);
			var desc = selectedPage[0].getDescription(this.options.language);
			
    		this.element.find('input#wbl-linkInternal').val(id);
			this._setImage(id);
			
	    	var inputTitle = this.element.find('input#wbl-linkTitle');
	    	var inputDesc = this.element.find('input#wbl-linkDesc');
			if(title != '' && inputTitle.val() == '') inputTitle.val(title);
			if(desc != '' && inputDesc.val() == '') inputDesc.val(desc);
    	}, this));
    },
    
    "input#wbl-linkExternal change": function(el, ev) {
    	// URL mit Linkchecker Rot Grün
    }
    
});