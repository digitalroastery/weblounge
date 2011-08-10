<%@ taglib uri="/WEB-INF/weblounge-content.tld" prefix="webl" %>

<ul>
	<li>
		<label for="company"><webl:i18n key="module.text.adress.company"/></label>
		<input name="element:company" i18n="module.text.adress.company" type="text" />
	</li>
	<li>
		<label for="anrede"><webl:i18n key="module.text.adress.salutation"/></label>
		<select name="property:anrede" description="anrede">	
			 <option value="Herr"><webl:i18n key="module.text.adress.Herr"/></option>
			 <option value="Frau"><webl:i18n key="module.text.adress.Frau"/></option>
		</select>
	</li>				
	<li>
		<label for="name"><webl:i18n key="module.text.adress.name"/></label>
		<input name="property:name" i18n="module.text.adress.name" type="text" />
	</li>
	<li>
		<label for="position"><webl:i18n key="module.text.adress.position"/></label>
		<input name="element:position" i18n="module.text.adress.position" type="text" />
	</li>
	<li>
		<label for="email"><webl:i18n key="module.text.adress.email"/></label>
		<input name="property:email" i18n="module.text.adress.email" type="text" />
	</li>
	<li>
		<label for="phone"><webl:i18n key="module.text.adress.phone"/></label>
		<input name="property:phone" i18n="module.text.adress.phone.number" type="text" />
	</li>
	<li>
		<label for="celular"><webl:i18n key="module.text.adress.celular"/></label>
		<input name="property:mobile" i18n="module.text.adress.celular.number" type="text" />
	</li>
</ul>

<ul>
	<li>
		<label for="adress"><webl:i18n key="module.text.adress.adress"/></label>
		<input name="element:adress" i18n="module.text.adress.adress" type="text" />
	</li>
	<li>
		<label for="pob"><webl:i18n key="module.text.adress.pob"/></label> 
		<input name="element:pob" i18n="module.text.adress.pob" type="text" />
	</li>
	<li>
		<label for="zip"><webl:i18n key="module.text.adress.zip"/></label> 
		<input name="property:zip" i18n="module.text.adress.zip" type="text" style="width:50px;" /> 
	</li>	
	<li>
		<label for="city"><webl:i18n key="module.text.adress.city"/></label>
		<input name="element:city" i18n="module.text.adress.city" type="text" />
	</li>
	<li>
		<label for="country"><webl:i18n key="module.text.adress.country"/></label>
		<select name="property:country" i18n="module.text.adress.country" value="CH">
				<option value="CH"><webl:i18n key="module.text.adress.country.CH"/></option>
				<option value="DE"><webl:i18n key="module.text.adress.country.DE"/></option>
				<option value="AT"><webl:i18n key="module.text.adress.country.AT"/></option>
				<option value="AU"><webl:i18n key="module.text.adress.country.AU"/></option>
				<option value="BE"><webl:i18n key="module.text.adress.country.BE"/></option>
				<option value="CA"><webl:i18n key="module.text.adress.country.CA"/></option>
				<option value="CZ"><webl:i18n key="module.text.adress.country.CZ"/></option>
				<option value="DK"><webl:i18n key="module.text.adress.country.DK"/></option>
				<option value="EE"><webl:i18n key="module.text.adress.country.EE"/></option>
				<option value="FI"><webl:i18n key="module.text.adress.country.FI"/></option>
				<option value="FR"><webl:i18n key="module.text.adress.country.FR"/></option>
				<option value="GE"><webl:i18n key="module.text.adress.country.GE"/></option>
				<option value="GB"><webl:i18n key="module.text.adress.country.GB"/></option>
				<option value="HU"><webl:i18n key="module.text.adress.country.HU"/></option>
				<option value="IT"><webl:i18n key="module.text.adress.country.IT"/></option>
				<option value="IS"><webl:i18n key="module.text.adress.country.IS"/></option>
				<option value="JP"><webl:i18n key="module.text.adress.country.JP"/></option>
				<option value="KR"><webl:i18n key="module.text.adress.country.KR"/></option>
				<option value="LV"><webl:i18n key="module.text.adress.country.LV"/></option>
				<option value="LI"><webl:i18n key="module.text.adress.country.LI"/></option>
				<option value="MY"><webl:i18n key="module.text.adress.country.MY"/></option>
				<option value="NL"><webl:i18n key="module.text.adress.country.NL"/></option>
				<option value="NO"><webl:i18n key="module.text.adress.country.NO"/></option>
				<option value="PK"><webl:i18n key="module.text.adress.country.PK"/></option>
				<option value="PL"><webl:i18n key="module.text.adress.country.PL"/></option>
				<option value="RU"><webl:i18n key="module.text.adress.country.RU"/></option>
				<option value="SG"><webl:i18n key="module.text.adress.country.SG"/></option>
				<option value="SK"><webl:i18n key="module.text.adress.country.SK"/></option>
				<option value="SI"><webl:i18n key="module.text.adress.country.SI"/></option>
				<option value="ES"><webl:i18n key="module.text.adress.country.ES"/></option>
				<option value="SE"><webl:i18n key="module.text.adress.country.SE"/></option>
				<option value="US"><webl:i18n key="module.text.adress.country.US"/></option>
		</select>		
	</li>
</ul>