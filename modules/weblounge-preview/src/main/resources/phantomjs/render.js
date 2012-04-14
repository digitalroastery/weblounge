var page = require('webpage').create();
page.viewportSize = { width: ${viewport.width}, height: ${viewport.height} };
page.open('${page.url}', function (status) {
    if (status !== 'success') {
        console.log('Unable to access the network!');
    } else {
        page.evaluate(function () {
        	${prepare.js}
        });
        page.render('${out.file}');
    }
    phantom.exit();
});
