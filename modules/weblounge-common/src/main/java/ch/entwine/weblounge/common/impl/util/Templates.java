/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.impl.util;

import ch.entwine.weblounge.common.site.Site;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is able to parse text content and replace any weblounge formatting
 * codes by their xhtml equivalent.
 */
public final class Templates {

	/** Pattern to match tag definitions */
	static final Pattern tag = Pattern.compile(
			"<\\s*webl:(\\w+)\\s*(\\w+\\s*=\\s*\".*?\"\\s*)*>(.*?)<\\s*/webl:(\\1)\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE
		);

	/** Pattern to match encoded tag definitions */
	static final Pattern tagEncoded = Pattern.compile(
			"&lt;\\s*webl:(\\w+)\\s*(\\w+\\s*=\\s*&quot;.*?&quot;\\s*)*&gt;(.*?)&lt;\\s*/webl:(\\1)\\s*&gt;", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE
	);

	/** Pattern to match attribute definitions */
	static final Pattern attribute = Pattern.compile(
			"(\\w+)\\s*=\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE
	);

	/** Pattern to match encoded attribute definitions */
	static final Pattern attributeEncoded = Pattern.compile(
			"(\\w+)\\s*=\\s*&quot;(.*?)&quot;", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE
	);

	/**
   * This class is not intended to be instantiated.
   */
  private Templates() {
    // Nothing to be done here
  }
	
	/**
	 * Formats the given text by resolving any known codes like formatting,
	 * links etc.
	 * 
	 * @param text the text to format
	 * @param encoded <code>true</code> if the text is html encoded
	 * @param site the site
	 */
	public static String format(String text, boolean encoded, Site site) {
		StringBuffer result = new StringBuffer(text.length() + 1024);
		Matcher m = (encoded) ? tagEncoded.matcher(text) : tag.matcher(text);
		while (m.find()) {
			m.appendReplacement(result, markup(m.group(1), getAttributes(m.group(2), encoded), format(m.group(3), encoded, site), encoded));
		}
		m.appendTail(result);
		return result.toString();
	}

	/**
	 * Returns the body surrounded by the tag.
	 * 
	 * @param tag the tag name
	 * @param attributes the attributes
	 * @param body the link body
	 * @param encoded <code>true</code> if the body is encoded
	 * @return the tag
	 */
	private static String markup(String tag, Map<String, String> attributes, String body, boolean encoded) {
		if (tag == null)
			return body;

		StringBuffer b = new StringBuffer();
		tag = tag.toLowerCase();

		b.append("<");
		b.append(tag);
		for (Map.Entry<String, String> e : attributes.entrySet()) {
			b.append(" ");
			b.append(e.getKey());
			b.append("=");
			b.append("\"");
			b.append(e.getValue());
			b.append("\"");
		}
		b.append(">");
		b.append(body);
		b.append("</");
		b.append(tag);
		b.append(">");
		return b.toString();
	}

	/**
	 * Extracts the attributes from the given string <code>s</code> and
	 * puts them into a map.
	 * 
	 * @param text the string
	 * @param encoded <code>true</code> if the attributes are encoded
	 * @return the mapped attributes
	 */
	private static Map<String, String> getAttributes(String text, boolean encoded) {
		Map<String, String> attributes = new HashMap<String, String>();
		if (text != null) {
			Matcher m = (encoded) ? attributeEncoded.matcher(text) : attribute.matcher(text);
			while (m.find()) {
				attributes.put(m.group(1), m.group(2));
			}
		}
		return attributes;
	}

}