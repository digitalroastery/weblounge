var page = require('webpage').create();

// Check the number of arguments
if (phantom.args.length < 2 || phantom.args.length > 3) {
 console.log('Usage: render.js <some URL> <output file>');
 phantom.exit(1);
}

// Extract the commandline arguments
var address = phantom.args[0];
var file = phantom.args[1];

// Open the page and render the content
page.open(address, function (status) {
  if (status !== 'success') {
    phantom.exit(1);
  } else {
	 page.evaluate(function () {
		${prepare.js} 
	 });
     page.render(file);
  }
  phantom.exit(0);
});