<%--
 * swiss unihockey
 *
 * Termine / Schweizer Meister
 * Multitab
 *
 * @dependency  jQuery.tabs
 * @attributes  -
 * @parameters  -
 *
 * @version     weblounge 2.2
 * @author      Simon Betschmann
 * @link        http://www.swissunihockey.ch
 * @copyright   Copyright 2009, swiss unihockey
 *
--%>
<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>
<%@ page session="true" %>

<script type="text/javascript">
// <![CDATA[
  $(document).ready(function(){
    $(".termine").tabs();
    $(".termine > ul").removeClass("hideme");
  });
// ]]>
</script>

<div class="termine">
	<ul class="hideme">
		<li><a href="#termine_tab1"><span>Termine</span></a></li>
	    <li><a href="#termine_tab2"><span>Schweizer Meister</span></a></li>
	</ul>
	<div id="termine_tab1">
		<p><b>21. bis 23. August 2009</b><br/>Europacup-Qualifikationsturnier Herren, Biglen</p>
		<p><b>28. bis 30. August 2009</b><br/>Europacup-Qualifikationsturniere Damen, Winterthur</p>
		<p><b>11. bis 13. September 2009</b><br/>L&auml;nderspiele Damennationalteam, Polen</p>
		<p><b>11. bis 13. September 2009</b><br/>L&auml;nderspiele Herrennationalteam, Lettland</p>
		<p><b>14 September 2009</b><br/>swiss unihockey Pressekonferenz, Bern</p>
		<p><b>19 September 2009</b><br/>Saisonstart Swiss Mobiliar League</p>
	</div>
	<div id="termine_tab2">
		<h3>Die Cupsieger 2008/09</h3>
		<p>SMC Herren: Tigers Langnau<br/>SMC Damen: UHC Dietlikon<br/>Ligacup Damen: Jona-Uznach Flames<br/>Ligacup Herren: Berner Hurricanes</p>
		<h3>Die Schweizer Meister 2008/09</h3>
		<p>SML Herren: SV Wiler-Ersigen<br/>SML Damen: UHC Dietlikon<br/>KF Herren: Berner Hurricanes<br/>KF Damen: Flamatt-Sense</p>
	</div>
</div>