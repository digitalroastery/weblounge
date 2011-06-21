steal.then('jsonix')
.then(function($) {
	
	$.Model('Page',
	/* @Static */
	{
		findOne: function(params, success, error) {
			
			if ('path' in params) {
				$.ajax('/system/weblounge/pages?path=' + params.path, {
					success: this.callback(['parseXML','wrap',success]),
				});
			} 
			else if ('id' in params) {
				$.ajax('/system/weblounge/pages/' + params.id, {
					success: this.callback(['parseXML','wrap',success]),
				});
			}
		},
		
		parseXML: function(xml) {
			var unmarshaller = Editor.Jsonix.context().createUnmarshaller();
			return unmarshaller.unmarshalDocument(xml);
		},
		
		parseJSON: function(json) {
			var marshaller = Editor.Jsonix.context().createMarshaller();
			return marshaller.marshalString(json);
		},
		
		findAll: function(params, success, error) {
			$.ajax('/system/weblounge/pages/4bb19980-8f98-4873-a813-000000000001/children', {
				success: function(xml) {
					var json = Page.parseXML(xml);
					success(json.value.page);
				}
			});
		},
		
		update: function(params, attrs, success, error){
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.id,
					type: 'put',
					dataType: 'xml',
					data: {content : Page.parseJSON(attrs)}
				});
			}	
		},
		
		create: function(params, attrs, success, error){
			if ('path' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.path,
					type: 'post',
					dataType: 'xml',
					data: attrs
				});
			}	
		},
		
		destroy: function(params, success, error){
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.id,
					type: 'delete'
				});
			}
		}

	},
	/* @Prototype */
	{
	    getComposer: function(id) {
	    	var composer;
	    	$.each(this.value.body.composers, function(i, comp) {
	    		if(comp.id == id) {
	    			composer = comp;
	    			return false;
	    		}
    		});
	    	return composer;
	    },
	    
	    getComposerIndex: function(id) {
	    	var index = -1;
	    	$.each(this.value.body.composers, function(i, composer) {
	    		if(composer.id == id) {
	    			index = i;
	    			return false;
	    		};
    		});
	    	return index;
	    },
	    
	    getPagelet: function(composerId, index) {
	    	var composer = this.getComposer(composerId);
	    	return composer.pagelets[index];
	    },
	    
	    updateComposer: function(composerId, newComposer) {
	    	var index = this.getComposerIndex(composerId);
	    	var composers = this.value.body.composers;
	    	
	    	$.each(newComposer, function(i, pagelet) {
	    		composers[index].pagelets.splice(i, 1, pagelet);
    		});
	    	Page.update({id:this.value.id}, this);
	    }
	    
	});

});
