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

public class Months {

	/** Hash map holding the months for various language */
	private static Map months_ = new HashMap();
	
	/**
	 * Returns the months in the given language or the english version if no
	 * localized version can be found.
	 * 
	 * @param language the requested language
	 * @return the resource bundle
	 */
	public static Properties getMonths(Language language) {
		Properties months = (Properties)months_.get(language);
		if (months != null) {
			return months;
		}			
		Locale locale = new Locale(language.getIdentifier(), "");
		String path = "/enumeration/months";
		months = new Properties();
		try {
			months.load(Months.class.getResourceAsStream(path + "_" + locale + ".properties"));
		} catch (IOException e) {
			try {
				months.load(Months.class.getResourceAsStream(path + ".properties"));
			} catch (IOException e1) {
				
			}
		}
		if (months != null) {
			months_.put(language, months);
			return months;
		}
		return months;
	}

	/**
	 * Returns the name of the month for the given number or the number
	 * itself, if the country name is not available in the given language.
	 * <p>
	 * Note that according to the <code>Calendar</code> class, January corresponds
	 * to <code>0</code>.
	 * 
	 * @param month the month
	 * @param language the requested language
	 * @param defaultValue the default value if the month is not found
	 * @return the country name
	 */
	public static String getName(int month, Language language, String defaultValue) {
		Properties p = getMonths(language);
		return (p != null) ? p.getProperty(Integer.toString(month)) : defaultValue;
	}

	/**
	 * Returns the name of the month for the given number or the number
	 * itself, if the country name is not available in the given language.
	 * <p>
	 * Note that according to the <code>Calendar</code> class, January corresponds
	 * to <code>0</code>.
	 * 
	 * @param month the month
	 * @param language the requested language
	 * @return the country name
	 */
	public static String getName(int month, Language language) {
		return getName(month, language, Integer.toString(month));
	}
	
}