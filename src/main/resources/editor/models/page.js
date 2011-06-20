steal.then('jsonix')
.then(function($) {
	
	unmarshalPage = function(xml) {
		var unmarshaller = Editor.Jsonix.context().createUnmarshaller();
		return unmarshaller.unmarshalDocument(xml);
	};
	
	marshalPage = function(json) {
		var marshaller = Editor.Jsonix.context().createMarshaller();
		var xml = marshaller.marshalString(json);
		return xml;
	};
	
	$.Model('Page',
	/* @Static */
	{
		getFromId: function(params, success, error) {
			if ('id' in params) {
				$.ajax('/system/weblounge/pages/' + params.id, {
					success: function(xml) {
						var json = unmarshalPage(xml);
						success(json.value);
					}
				});
			}
		},
		
		getFromPath: function(params, success, error) {
			if ('path' in params) {
				$.ajax('/system/weblounge/pages?path=' + params.path, {
					success: function(xml) {
						var json = unmarshalPage(xml);
						success(json.value);
					}
				});
			}
		},
		
		findAll: function(params, success, error) {
			$.ajax('/system/weblounge/pages/4bb19980-8f98-4873-a813-000000000001/children', {
				success: function(xml) {
					var json = unmarshalPage(xml);
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
					data: marshalPage(attrs)
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
	{});

});
