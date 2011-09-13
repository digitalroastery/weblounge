$.Controller("Editor.Author",
/* @static */
{
},

/* @prototype */
{
    /**
     * Initialize a new AuthorEditor controller.
     */
    init: function(el) {
    	var year = this.element.find('input[name="property:year"]');
    	var login = this.element.find('input[name="property:login"]');
    	var name = this.element.find('input[name="property:name"]');
    	
    	this.element.find('input[name="property:email"]').change(function() {
    		if($(this).val() == '') {
    			name.removeClass('required');
    		} else {
    			name.addClass('required');
    		}
    	});
    	
    	if(year.val() == "") {
    		year.val(new Date().getFullYear());
    	}
		if(login.val() == "") {
			login.val(this.options.runtime.getUserLogin());
		}
		if(name.val() == "") {
			name.val(this.options.runtime.getUserName());
		}
    }
    
});