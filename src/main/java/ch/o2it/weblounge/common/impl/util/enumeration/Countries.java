/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
 *  http://weblounge.o2it.ch
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.util.enumeration;

import ch.o2it.weblounge.common.language.Language;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class Countries {

	/** Hash map holding the countries for various language */
	private static Map countries_ = new HashMap();
	
	/**
	 * Returns the countries in the given language or the english version if no
	 * localized version can be found.
	 * 
	 * @param language the requested language
	 * @return the resource bundle
	 */
	public static Properties getCountries(Language language) {
		Properties countries = (Properties)countries_.get(language);
		if (countries != null) {
			return countries;
		}			
		Locale locale = new Locale(language.getIdentifier(), "");
		String path = "/enumeration/countries";
		countries = new Properties();
		try {
			countries.load(Countries.class.getResourceAsStream(path + "_" + locale + ".properties"));
		} catch (IOException e) {
			try {
				countries.load(Countries.class.getResourceAsStream(path + ".properties"));
			} catch (IOException e1) {
				
			}
		}
		if (countries != null) {
			countries_.put(language, countries);
			return countries;
		}
		return countries;
	}

	/**
	 * Returns the name of the country for the given <code>ISO</code> code or the default
	 * value, if the country name is not available in the given language.
	 * 
	 * @param code the iso country code
	 * @param language the requested language
	 * @param defaultValue the default value if the country is not found
	 * @return the country name
	 */
	public static String getName(String code, Language language, String defaultValue) {
		Properties p = getCountries(language);
		return (p != null && !"-".equals(code)) ? p.getProperty(code) : defaultValue;
	}

	/**
	 * Returns the name of the country for the given <code>ISO</code> code or the code
	 * itself, if the country name is not available in the given language.
	 * 
	 * @param code the iso country code
	 * @param language the requested language
	 * @return the country name
	 */
	public static String getName(String code, Language language) {
		return getName(code, language, code);
	}
	
}