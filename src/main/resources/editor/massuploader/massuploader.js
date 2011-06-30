steal.plugins('jquery', 'jquery/controller/view', 'jquery/view/tmpl')
.views('//editor/massuploader/views/init.tmpl')
.css('massuploader', 'fileuploader')
.resources('jsupload/jsupload.nocache', 'fileuploader').then(function($) {
	
 	function jsuOnLoad() {
	 	steal.dev.log('test');
 	}

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
			$(el).html('//editor/massuploader/views/init.tmpl', {});
			
			var uploader = new qq.FileUploader({
			    // pass the dom node (ex. $(selector)[0] for jQuery users)
			    element: document.getElementById('file-uploader'),
			    // path to server-side upload script
			    action: '/system/weblounge/files/uploads?path=/test/&languageId=fr'
			});
			
//	        new jsu.Upload({
//	        	multiple: true,  // specify whether the uploader has a multiple behavior
//	        	chooser: "browser",  // Choose file button type, options are: "browser", "button", "label", "anchor".
//	        	type: "chismes",  // Type of progress bar, valid options are "basic", "chismes" or "incubator"
//	        	maxFiles: 0,  // Only used if multiple=true. The maximum number of files which the user can send to the server. 0 means unlimited. Only successful uploads are counted.
//		   
//	        	uploadBrowse: "", // ????
//	            auto: true, // ???
//	            
//	        	onStart: this.openTagDialog,  // Javascript method called when the upload process starts
//	        	onChange: null,  // Javascript method called when the user selects a file
//	        	onFinish: this.loadImage,  // Javascript method called when the upload process finishes
//	        	onCancel: this.deleteFile,  // Javascript method called when the upload file is canceled, removed from the queue or from the server
//	        	onStatus: null,  // Javascript method called when the upload file's status changes
//		   
//	        	containerId: "uploader",  // Id of the element where the widget will be inserted
//	        	action: "servlet.gupld",  // Servlet path, it has to be in the same domain, because cross-domain is not supported
//	        	validExtensions: null,  // List of valid extensions, the extensions has to be separated by comma or spaces
//	        	
//	        	regional: {     // hash with the set of key/values to internationalize the widget
//	        		uploaderActiveUpload: "There is already an active upload, try later.", 
//	        		uploaderAlreadyDone: "This file was already uploaded.", 
//	        		uploaderInvalidExtension: "Invalid file.\nOnly these types are allowed:\n", 
//	        		uploaderTimeout: "Timeout sending the file:\n perhups your browser does not send files correctly,\n your session has expired,\n or there was a server error.\nPlease try again.", 
//	        		uploaderServerError: "Invalid server response. Have you configured correctly your application in the server side?", 
//	        		uploaderServerUnavailable: "Unable to contact with the server: ", 
//	        		uploaderSend: "Send", 
//	        		uploadLabelCancel: null, 
//	        		uploadStatusCanceling: "Canceling", 
//	        		uploadStatusCanceled: "Canceled", 
//	        		uploadStatusError: "Error", 
//	        		uploadStatusInProgress: "Sending...", 
//	        		uploadStatusQueued: "Queued", 
//	        		uploadStatusSubmitting: "Submiting form...", 
//	        		uploadStatusSuccess: "Done", 
//	        		uploadStatusDeleted: "Deleted", 
//	        		uploadBrowse: "Select a file ...", 
//	        		progressPercentMsg: "{0}%", // Set the message used to format the progress in percent units. 
//	        		progressSecondsMsg: "Time remaining: {0} Seconds", // Set the message used to format the time remaining text below the progress bar in seconds.
//	        		progressMinutesMsg: "Time remaining: {0} Minutes", // Set the message used to format the time remaining text below the progress bar in minutes
//	        		progressHoursMsg: "Time remaining: {0} Hours" // Set the message used to format the time remaining text below the progress bar in hours
//        		}
//	        });
	        
//			this.uploadDialog = $('<div></div>')
//			.load('/weblounge/editor/massuploader/views/upload-dialog.html')
//			.dialog({
//				modal: true,
//				title: 'Medien upload',
//				autoOpen: true,
//				resizable: true,
//				buttons: {
//					Abbrechen: function() {
//						$(this).dialog('close');
//					},
//					OK: $.proxy(function () {
//						this.deleteDialog.dialog('close');
//					},this)
//				},
//			});
	    },
	    
	    deleteFile: function(upl_data) {
	    	// REST Delete Image
	    },
	    
	    openTagDialog: function(upl_data) {
	    	alert(upl_data.url);
	    },
	    
        // Method to show a picture using the class PreloadImage
        // The image is not shown until it has been sucessfully downloaded
        loadImage: function(upl_data) {
        	// REST CREATE Image update
        	if (upl_data && upl_data.url) {
        		var image = new jsu.PreloadImage({
        			url: upl_data.url,
        			containerId: "photos",
        			onLoad: function(img_data) {
        				image.setSize(100, -1);
        			}
                });
            }
        },
        
	    "#drop-area dragleave": function(el, ev) {
			var target = ev.target;
			
			if (target && target === this.dropArea) {
				this.className = "";
			}
			ev.preventDefault();
			ev.stopPropagation();
	    },
	    
	    "#drop-area dragenter": function(el, ev) {
			this.className = "over";
			ev.preventDefault();
			ev.stopPropagation();
	    },
	    
	    "#drop-area dragover": function(el, ev) {
			ev.preventDefault();
			ev.stopPropagation();
	    },
	    
	    "input[type=file] change": function(el, ev, bla) {
	    	steal.dev.log('adsfadf');
	    },
	    
	    "#drop-area drop": function(el, ev) {
//	    	this.find("input[type=file]:first").val("test.jpg");
//	    	this.find("input[type=file]:first").trigger('change');
//	    	this.find("input[type=file]");
//			this.traverseFiles(ev.dataTransfer.files);
	    	this.className = "";
	    	ev.preventDefault();
	    	ev.stopPropagation();
	    }
  	});
});
