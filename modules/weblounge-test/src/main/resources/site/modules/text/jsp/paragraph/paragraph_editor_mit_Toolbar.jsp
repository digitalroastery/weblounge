<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<script type="text/javascript">
<!--
function insert(aTag, eTag) {
  var input = document.forms['formular'].elements['paragraph'];
  input.focus();
  /* für Internet Explorer */
  if(typeof document.selection != 'undefined') {
    /* Einfügen des Formatierungscodes */
    var range = document.selection.createRange();
    var insText = range.text;
    range.text = aTag + insText + eTag;
    /* Anpassen der Cursorposition */
    range = document.selection.createRange();
    if (insText.length == 0) {
      range.move('character', -eTag.length);
    } else {
      range.moveStart('character', aTag.length + insText.length + eTag.length);      
    }
    range.select();
  }
  /* für neuere auf Gecko basierende Browser */
  else if(typeof input.selectionStart != 'undefined')
  {
    /* Einfügen des Formatierungscodes */
    var start = input.selectionStart;
    var end = input.selectionEnd;
    var insText = input.value.substring(start, end);
    input.value = input.value.substr(0, start) + aTag + insText + eTag + input.value.substr(end);
    /* Anpassen der Cursorposition */
    var pos;
    if (insText.length == 0) {
      pos = start + aTag.length;
    } else {
      pos = start + aTag.length + insText.length + eTag.length;
    }
    input.selectionStart = pos;
    input.selectionEnd = pos;
  }
  /* für die übrigen Browser */
  else
  {
    /* Abfrage der Einfügeposition */
    var pos;
    var re = new RegExp('^[0-9]{0,3}$');
    while(!re.test(pos)) {
      pos = prompt("Einfügen an Position (0.." + input.value.length + "):", "0");
    }
    if(pos > input.value.length) {
      pos = input.value.length;
    }
    /* Einfügen des Formatierungscodes */
    var insText = prompt("Bitte geben Sie den zu formatierenden Text ein:");
    input.value = input.value.substr(0, pos) + aTag + insText + eTag + input.value.substr(pos);
  }
}
//-->
</script>

<div align="left">
	<form name="formular" action="">
		<p><webl:i18n key="module.text.subtitle.anchor"/><br />
			<wiz:input property="anchor" i18n="module.text.subtitle.anchor" type="text" /><br />
			<span class="editor-sample"><webl:i18n key="module.text.subtitle.anchor.sample"/></span>
		</p>	
		<p><webl:i18n key="module.text.paragraph.title"/><br />
			<wiz:input element="title" i18n="module.text.title.keyword" type="text" style="width:100%;" /><br />
			<span class="editor-sample"><webl:i18n key="module.text.paragraph.title.sample"/></span>
		</p>
		<p><a href="#" onclick="insert('<webl\:b>', '<\/webl\:b>')" title="Fett" alt="Fett"><img src="http://www.swissunihockey.ch/media/icons/icon_bold.png" alt="bold" /></a>&nbsp;<a href="#" onclick="insert('<webl\:i>', '<\/webl\:i>')" title="Kursiv" alt="Kursiv"><img src="http://www.swissunihockey.ch/media/icons/icon_italic.png" alt="italic" /></a></p>
		<p><webl:i18n key="module.text.paragraph.paragraph"/>&nbsp;<span class="required">*</span><br />
		   <wiz:textarea element="paragraph" i18n="module.text.paragraph.paragraph" required="true" wrap="soft" rows="12" cols="100" style="width:100%;height:320px;" /><br />
		   <span class="editor-sample"><webl:i18n key="module.text.paragraph.paragraph.sample"/></span>
		</p>
	</form>
</div>
