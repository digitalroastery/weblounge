steal.plugins('jquery', 'jquery/controller/view', 'jquery/view/tmpl')
.views('//editor/massuploader/views/file_queued.tmpl', '//editor/massuploader/views/file_edit.tmpl', '//editor/massuploader/views/init.tmpl')
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
			$(el).html('//editor/massuploader/views/init.tmpl', {});
			
			this.filesUpload = this.find("#files-upload");
			this.dropArea = this.find("#drop-area");
			this.fileList = this.find("#file-list");
	    },
	    
	    "#files-upload change": function(el) {
	    	this.traverseFiles(el[0].files);
	    },
	    
		traverseFiles: function(files) {
			if (typeof files !== "undefined") {
				for (var i=0, l=files.length; i<l; i++) {
					this.uploadFile(files[i]);
				}
			}
			else {
				this.fileList.innerHTML = "No support for the File API in this web browser";
			}	
		},
		
		ieReadFile: function(filename) {
		    try {
		        var fso  = new ActiveXObject("Scripting.FileSystemObject"); 
		        var fh = fso.OpenTextFile(filename, 1); 
		        var contents = fh.ReadAll(); 
		        fh.Close();
		        return contents;
		    }
		    catch (Exception) {
		        return "Cannot open file :(";
		    }
		},
	    
	    uploadFile: function(file) {
			var li = document.createElement("li"),
			div = document.createElement("div"),
			img,
			progressBarContainer = document.createElement("div"),
			progressBar = document.createElement("div"),
			reader,
			xhr,
			fileInfo;
			
			li.appendChild(div);
			
			progressBarContainer.className = "progress-bar-container";
			progressBar.className = "progress-bar";
			progressBarContainer.appendChild(progressBar);
			li.appendChild(progressBarContainer);
			
			/*
				If the file is an image and the web browser supports FileReader,
				present a preview in the file list
			*/
			if (typeof FileReader !== "undefined" && (/image/i).test(file.type)) {
				img = document.createElement("img");
				li.appendChild(img);
				reader = new FileReader();
				reader.onload = (function (theImg) {
					return function (evt) {
						theImg.src = evt.target.result;
					};
				}(img));
				reader.readAsDataURL(file);
			}
			
			File.create
			
			// Uploading - for Firefox, Google Chrome and Safari
			xhr = new XMLHttpRequest();
			
			// Update progress bar
			xhr.upload.addEventListener("progress", function (evt) {
				if (evt.lengthComputable) {
					progressBar.style.width = (evt.loaded / evt.total) * 100 + "%";
				}
				else {
					// No data to calculate on
				}
			}, false);
			
			// File uploaded
			xhr.addEventListener("load", function () {
				progressBarContainer.className += " uploaded";
				progressBar.innerHTML = "Uploaded!";
			}, false);
			
			xhr.open("post", "/system/weblounge/files/", true);
			
			// Set appropriate headers
			xhr.setRequestHeader("Content-Type", "multipart/form-data");
			xhr.setRequestHeader("X-File-Name", file.fileName);
			xhr.setRequestHeader("X-File-Size", file.fileSize);
			xhr.setRequestHeader("X-File-Type", file.type);
	
			// Send the file (doh)
			xhr.send(file);
			
			// Present file info and append it to the list of files
			fileInfo = "<div><strong>Name:</strong> " + file.name + "</div>";
			fileInfo += "<div><strong>Size:</strong> " + parseInt(file.size / 1024, 10) + " kb</div>";
			fileInfo += "<div><strong>Type:</strong> " + file.type + "</div>";
			div.innerHTML = fileInfo;
			
			this.fileList.appendChild(li);
	    }
  	});
});
