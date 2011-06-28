steal.plugins('jquery', 'jquery/controller/view', 'jquery/view/tmpl')
.views('//editor/massuploader/views/file_queued.tmpl', '//editor/massuploader/views/file_edit.tmpl')
.css('massuploader')
.resources('plupload.js', 'plupload.html5.js').then(function($) {

	$.Controller("Editor.Massuploader",	
	/* @static */
	{
		defaults: {
			max_file_size: '10mb',
			resize: {
				width: 1600,
				height: 1200,
				quality: 90
			}
	    }
  	},
  	/* @prototype */
  	{
		/**
		 * Initialize a new MassUploader controller.
		 */
		init: function(el) {
			$(el).html(this.view());
	
		    var uploader = new plupload.Uploader({
		    	runtimes: 'html5',
		        browse_button: 'filebrowser',
		        container: 'massuploader',
		        max_file_size: this.options.max_file_size,
		        url: '/system/weblounge/workbench/files',
		        resize: this.options.resize
		    });
	
		    uploader.init();
	
		    uploader.bind('FilesAdded', function(up, files) {
		    	$.each(files, function(i, file) {
		    		$(el).find('#queue').append('//editor/massuploader/views/file_queued.tmpl', file);
		    	});
		    	uploader.start();
		    });
	
		    uploader.bind('UploadProgress', function(up, file) {
		    	$(el).find('#' + file.id + ' span').html(file.percent);
		    });
	
			uploader.bind('UploadFile', function(up, file) {
				
			});
			
			uploader.bind('FileUploaded', function(up, file, res) {
				$('#'+file.id).html('//editor/massuploader/views/file_edit.tmpl', $.parseJSON(res.response));
			});
			
			uploader.bind('Error', function(up, error) {
				$('#'+error.file.id).addClass('error');
			});
			
			uploader.bind('UploadComplete', function(up, files) {
				up.refresh();
			});
	    }
  	});
});
