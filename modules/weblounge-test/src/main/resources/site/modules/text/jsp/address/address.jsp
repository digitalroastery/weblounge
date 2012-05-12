<%@ taglib uri="http://entwinemedia.com/weblounge/3.0/content" prefix="webl" %>

<webl:element define="adress, pob, company, position">
<webl:property define="name">
<webl:context define="site, language">
	<div class="adress vcard">
		<p class="person">
			<webl:property define="anrede">
			<% if(!("".equals(company))) { %>
				<b><span class="org"><webl:element name="company"/></span></b><br/>
				<% if(!("".equals(name))) { %><%= site.getI18n().getAsHTML("module.text.adress." + anrede , language) %><% } %>	
			<% } else { %>		
				<% if(!("".equals(name))) { %><%= site.getI18n().getAsHTML("module.text.adress." + anrede , language) %><% } %>		
			<% } %>
			</webl:property>
			<webl:ifproperty name="email">
				<a class="email" href="mailto:<%= email %>">
			</webl:ifproperty>
			
			<webl:ifproperty name="name"><span class="fn"><webl:property name="name" /></span><br/></webl:ifproperty>
			
			<webl:ifproperty name="email">
				</a>
			</webl:ifproperty>
			
			<% if (("".equals(name)) && !("".equals(position))) { %>
			<webl:ifproperty name="email">
				<a class="email" href="mailto:<%= email %>">
			</webl:ifproperty>
			<span class="role"><webl:element name="position" /></span>
			<webl:ifproperty name="email">
				</a>
			</webl:ifproperty>
			<% } else { %>
			<span class="role"><webl:element name="position" /></span>
			<% } %>
		</p>
		<p class="adress adr">
			<webl:ifelement name="adress">
				<span class="street-address"><webl:element name="adress"/></span><br />
			</webl:ifelement>
			
			<webl:ifelement name="pob">
				<webl:element name="pob"/><br />
			</webl:ifelement>

			<% if(!("".equals(adress)) || !("".equals(pob))) { %>
				<webl:ifproperty name="zip">
					<span class="country-name"><webl:property name="country" /></span>-<span class="postal-code"><webl:property name="zip" /></span>&nbsp;<span class="locality"><webl:element name="city" /></span><br />
				</webl:ifproperty>	
			<% } %>
		</p>

		<webl:ifproperty name="phone">
			<div class="tel">
				<p class="phone"><span class="value"><webl:property name="phone" /></span></p>
			</div>
		</webl:ifproperty>
		<webl:ifproperty name="mobile">
			<div class="tel">
				<abbr class="type" title="Mobil"></abbr>
				<p class="phone"><span class="value"><webl:property name="mobile" /></span></p>
			</div>
		</webl:ifproperty>
	</div>
</webl:context>
</webl:property>
</webl:element>