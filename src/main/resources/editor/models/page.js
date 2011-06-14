steal.then('jsonix')
.then(function($) {
	
	unmarshalPage = function(data, success) {
		steal.dev.log('Unmarshalling page');
		var unmarshaller = Editor.Jsonix.context().createUnmarshaller();
		var json = unmarshaller.unmarshalDocument(data);
		success(json.value.page); 
	};
	
	$.Model('Page',
	/* @Static */
	{
		findOne: function(params, success, error) {
			if ('id' in params) {
				$.ajax('/system/weblounge/pages/', {
					success: function(xml) {
						unmarshalPage(xml, success);
					}
				});
			}
		},
		
		findAll: function(params, success, error) {
			steal.dev.log('try loading children...');
			$.ajax('/system/weblounge/pages/4bb19980-8f98-4873-a813-000000000001/children', {
				success: function(xml) {
					unmarshalPage(xml, success);
				}
			});
		},
		
		destroy: function(params, attrs, success, error){
			if ('id' in params) {
				$.ajax({
					url: '/system/weblounge/pages/' + params.id,
					type: 'put',
					dataType: 'xml',
					data: attrs
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
		
		update: function(params, success, error){
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
