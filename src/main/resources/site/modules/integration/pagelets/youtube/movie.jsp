<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<webl:property define="movie, border, color1, color2">
	<div class="youtube">
		<object width="560" height="315">
			<param name="movie" value="http://www.youtube.com/v/<%= movie %>&amp;rel=0&amp;color1=<%= color1 %>&color2=<%= color2 %>&amp;border=<%= border %>"></param>
			<param name="wmode" value="transparent"></param>
			<embed src="http://www.youtube.com/v/<%= movie %>&amp;rel=0&amp;color1=<%= color1 %>&amp;color2=<%= color2 %>&amp;border=<%= border %>" type="application/x-shockwave-flash" wmode="transparent" width="560" height="315"></embed>
		</object>
	</div>
</webl:property>