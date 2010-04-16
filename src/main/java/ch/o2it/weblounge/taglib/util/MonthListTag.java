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

package ch.o2it.weblounge.taglib.util;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.taglib.WebloungeTag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

public class MonthListTag extends WebloungeTag {

	/** Serial version uid */
  private static final long serialVersionUID = 519583223753192813L;
  
  /** Hash map holding the months for various language */
  private Map<Language, Properties> months_ = new HashMap<Language, Properties>();

  /**  The selected month */
	protected String selected = null;
	
	/**
	 * Sets the selected month.
	 * 
	 * @param value the selected month
	 */
	public void setSelected(String value) {
		if (value != null)
			selected = value.toLowerCase();
	}
	
	/**
	 * Prints out the month list.
	 * 
	 * @see javax.servlet.jsp.tagext.Tag#doStartTag()
	 */
	public int doStartTag() throws JspException {
		Properties months = getMonths(getRequest().getLanguage());
		JspWriter writer;
		try {
			writer = pageContext.getOut();
			writer.flush();
			if (months != null) {
				TreeSet<Integer> set = new TreeSet<Integer>();
				Iterator<Object> pi = months.keySet().iterator();
				while (pi.hasNext()) {
					set.add(new Integer(Integer.parseInt((String)pi.next())));
				}
				writer.print("<select");
				writer.print(getStandardAttributes());
				writer.println(">");
				
				Iterator<Integer> mi = set.iterator();
				while (pi.hasNext()) {
					Integer key = mi.next();
					String month = (String)months.get(key.toString());
					StringBuffer option = new StringBuffer();
					option.append("<option value=\"");
					option.append(key);
					option.append("\"");
					if (key.toString().equals(selected)) {
						option.append(" selected ");
					}
					option.append(">");
					option.append(month);
					option.append("</option>");
					writer.println(option.toString());
				}
				writer.println("</select>");
				writer.flush();
			}
		} catch (IOException e) {
		  throw new JspException(e);
		}
		return EVAL_PAGE;
	}
  
  /**
   * Returns the months in the given language or the english version if no
   * localized version can be found.
   * 
   * @param language the requested language
   * @return the resource bundle
   */
  private Properties getMonths(Language language) {
    Properties months = months_.get(language);
    if (months != null) {
      return months;
    }     
    Locale locale = new Locale(language.getIdentifier(), "");
    String path = "/enumeration/months";
    months = new Properties();
    try {
      months.load(MonthListTag.class.getResourceAsStream(path + "_" + locale + ".properties"));
    } catch (IOException e) {
      try {
        months.load(MonthListTag.class.getResourceAsStream(path + ".properties"));
      } catch (IOException e1) {
        
      }
    }
    months_.put(language, months);
    return months;
  }

}
