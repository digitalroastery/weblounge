<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>

<%@ page import="java.util.List" %>
<%@ page import="ch.entwine.weblounge.api.repository.ImageResource" %>
<%@ page import="ch.entwine.weblounge.api.repository.ImageStyle" %>

<webl:context define="site, language, toolkit">

<%
    List images = (List) request.getAttribute("images");
    String style = "simple"; //getParam(request, "style", "simple");
    String position = "top"; //getParam(request, "position", "top");
    String index = "center"; //getParam(request, "index", "center");
    int maxPageItems = 1; //getParam(request, "maxPageItems", 1);
    int maxIndexPages = 10; // getParam(request, "maxIndexPages", 10);
    ImageStyle imageStyle = site.getImageStyles().getStyle("gallery1");
    String gallery = request.getParameter("gallery");
%>

<pg:pager
    items="<%= images.size() %>"
    index="<%= index %>"
    maxPageItems="<%= maxPageItems %>"
    maxIndexPages="<%= maxIndexPages %>"
    isOffset="true"
    export="offset,currentPageNumber=pageNumber"
    scope="request">

	<%-- keep track of preference --%>
	  <pg:param name="gallery"/>
	  <pg:param name="layout"/>

	<div align="center">
	  <%
		int count = offset.intValue();
		ImageResource image = null;

		if (count < images.size()) {
			image = (ImageResource)images.get(count);
			String link3x3 = toolkit.linkAction("repository", "gallery") + "?gallery=" + gallery + "&layout=33" + "&pager.offset=" + (count / 9);
		%>
		<table cellspacing="10" border="0">
			<tr>
				<td>
					<pg:item>
						<% if (image.getWidth() >= imageStyle.getWidth() || image.getHeight() >= imageStyle.getHeight()) { %>
						<img src="<%= image.getLinkPath(imageStyle) %>" style="border:0;" alt="" /><br />
						<% } else { %>
						<img src="<%= image.getLinkPath() %>" style="border:0;" alt="" /><br />
						<% } %>
						<% if ((image.getDescription(language) != null) && !(image.getDescription(language)).equals("null")) { %>
							<span class="name"><%= image.getName(language) %></span><br/>
							<span class="description"><%= image.getDescription(language) %></span>
						<% } else { %>
							<span class="name"><%= image.getName(language) %></span>
						<% } %>
					</pg:item>
				</td>
			</tr>
		</table>
			<%
			}
	  %>

	<p class="galleryIndex">
	<pg:prev export="pageUrl">&nbsp;[<a href="<%= pageUrl %>"><webl:i18n name="module.repository.gallery.prev" /></a>]</pg:prev>
	<pg:pages>
		<%
			if (pageNumber.intValue() < 10) {
		%>&nbsp;<%
			}
			if (pageNumber == currentPageNumber) {
		%><b><%= pageNumber %></b><%
			} else {
		%><a href="<%= pageUrl %>"><%= pageNumber %></a><%
			}
		%>
	</pg:pages>
	<pg:next export="pageUrl">&nbsp;[<a href="<%= pageUrl %>"><webl:i18n name="module.repository.gallery.next" /></a>]</pg:next>
	</p>
	
	</div>

</pg:pager>
</webl:context>

<%!
private static final String getParam(ServletRequest request, String name,
    String defval)
{
    String param = request.getParameter(name);
    return (param != null ? param : defval);
}

private static final int getParam(ServletRequest request, String name,
    int defval)
{
    String param = request.getParameter(name);
    int value = defval;
    if (param != null) {
	try { value = Integer.parseInt(param); }
	catch (NumberFormatException ignore) { }
    }
    return value;
}
%>