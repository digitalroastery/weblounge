<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ page import="java.util.HashSet"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="ch.entwine.weblounge.common.content.image.ImageStyle" %>
<%@ page import="ch.entwine.weblounge.common.site.Module" %>

<webl:context define="site, language">
<div id="wbl-galleryEditor">
	<p>
	    <label for="wbl-galleryTitle">Titel:</label> <input id="wbl-galleryTitle" type="text" name="element:gallerytitle" />
	    <label for="wbl-galleryDesc">Beschreibung:</label> <input id="wbl-galleryDesc" type="text" name="element:description" /><br />
	    <input id="wbl-galleryFiles" type="text" name="property:resourceid" readonly="readonly" /><br /><br />
		<button id="wbl-galleryFilesButton" type="button">Select Files</button>
	</p>
	<div id="Standard" title="Standard" selected="true" style="margin: 10px;">
      	<p class="editor-title" style="float:left; width:50%; clear:left; padding-bottom:15px">
			<webl:i18n key="module.repository.image.style"/><br />
			<select name="property:style" description="Style" required="true">
				<%
					HashSet<ImageStyle> set = new HashSet<ImageStyle>();
				    for (Module m : site.getModules()) {
				      ImageStyle[] styles = m.getImageStyles();
				      for (ImageStyle style : styles) {
				        if(style.isComposeable()) set.add(style);
				      }
				    }
				
				    for(ImageStyle style : set) {
		     	%>
					<option value="<%= style.getIdentifier() %>"><%= style.getName() %></option>
		 		<%
				    }
		       	%>
			</select>
		</p>
	
		<p class="editor-title" style="float:left; width:50%">
		    <webl:i18n key="module.repository.image.alignment"/><br />
			<select name="property:alignment" required="true">
				 <option value="center"><webl:i18n key="module.repository.image.center"/></option>
				 <option value="right"><webl:i18n key="module.repository.image.right"/></option>
				 <option value="left"><webl:i18n key="module.repository.image.left"/></option>
				 <option value="right_floated"><webl:i18n key="module.repository.image.right_floated"/></option>
				 <option value="left_floated"><webl:i18n key="module.repository.image.left_floated"/></option>
			</select>
		</p>
		<p class="editor-title" style="clear:left">
	   		<webl:i18n key="module.repository.image.description"/><br />
	       	<input name="element:description" i18n="module.repository.image.description" type="text" style="width:100%" /><br />
	       	<span class="editor-sample"><webl:i18n key="module.repository.image.description.sample"/></span>
	    </p>
		<p class="editor-title">
			<webl:i18n key="module.repository.image.photographer"/><br />
			<input name="property:photographer" i18n="module.repository.image.photographer" type="text" style="width:100%" /><br />
			<span class="editor-sample"><webl:i18n key="module.repository.image.photographer.sample"/></span>
		</p>
		<p>
			<input name="property:enlarge" i18n="module.repository.image.enlarge" type="checkbox" default="true" />&nbsp;<webl:i18n key="module.repository.image.enlarge"/>
		</p>	
	</div>
	<div id="Erweitert" title="Erweitert" style="margin: 10px; display: none;" >
	<p>
		<input name="property:border" i18n="module.repository.image.border" type="checkbox" default="true" />&nbsp;<webl:i18n key="module.repository.image.border"/><br />
		<input name="property:description_preview" i18n="module.repository.image.description_preview" type="checkbox" />&nbsp;<webl:i18n key="module.repository.image.description_preview"/><br />
		<input name="property:group" i18n="module.repository.image.slideshow" type="checkbox" />&nbsp;<webl:i18n key="module.repository.image.slideshow"/>
	</p>
    <p class="editor-title">
	   	<webl:i18n key="module.repository.image.link"/><br />
	    <input name="property:link" i18n="module.repository.image.alt" type="text" style="width:100%" /><br />
	    <span class="editor-sample"><webl:i18n key="module.repository.image.link.sample"/></span>
    </p>
	</div>
</div>
</webl:context>