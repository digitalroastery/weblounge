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
				$.ajax({
					url: '/system/weblounge/pages/' + params.id
				});
			}
		},
		
		findAll: function(params, success, error) {
			steal.dev.log('try loading children...');
			$.ajax('/system/weblounge/pages/025d9ceb-62d4-4f1b-8797-f86bc47338cd/children', {
				success: function(xml) {
					unmarshalPage(xml, success);
				}
			});
		}

	},
	/* @Prototype */
	{});

});
