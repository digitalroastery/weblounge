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

package ch.entwine.weblounge.taglib.util;

import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.taglib.WebloungeTag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

public class CountryListTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = 1326786853708846890L;

  /** Hash map holding the countries for various language */
  private static Map<Language, Properties> countries_ = new HashMap<Language, Properties>();

  /** The selected country */
  protected String selected = null;

  /**
   * Sets the selected country.
   * 
   * @param value
   *          the selected country
   */
  public void setSelected(String value) {
    if (value != null)
      selected = value.toLowerCase();
  }

  /**
   * Prints out the country list.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    Properties countries = getCountries(getRequest().getLanguage());
    JspWriter writer;
    try {
      writer = pageContext.getOut();
      writer.flush();
      if (countries != null) {
        TreeSet<Country> set = new TreeSet<Country>();
        Iterator<Object> pi = countries.keySet().iterator();
        while (pi.hasNext()) {
          String key = (String) pi.next();
          String name = countries.getProperty(key);
          set.add(new Country(key, name));
        }
        writer.print("<select");

        // Add tag attributes
        StringBuffer buf = new StringBuffer();
        for (Map.Entry<String, String> attribute : getStandardAttributes().entrySet()) {
          buf.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
        }
        writer.print(buf.toString());

        writer.println(">");

        Iterator<Country> ci = set.iterator();
        while (ci.hasNext()) {
          Country c = ci.next();
          StringBuffer option = new StringBuffer();
          option.append("<option value=\"");
          option.append(c.code);
          option.append("\"");
          if (c.code.toLowerCase().equals(selected)) {
            option.append(" selected ");
          }
          option.append(">");
          option.append(c.name);
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
   * Returns the countries in the given language or the English version if no
   * localized version can be found.
   * 
   * @param language the requested language
   * @return the resource bundle
   */
  private Properties getCountries(Language language) {
    Properties countries = countries_.get(language);
    if (countries != null) {
      return countries;
    }     
    Locale locale = new Locale(language.getIdentifier(), "");
    String path = "/countries/countries";
    countries = new Properties();
    try {
      countries.load(CountryListTag.class.getResourceAsStream(path + "_" + locale + ".properties"));
    } catch (IOException e) {
      try {
        countries.load(CountryListTag.class.getResourceAsStream(path + ".properties"));
      } catch (IOException e1) {
        
      }
    }
    countries_.put(language, countries);
    return countries;
  }

  /**
   * Inner type used to sort the countries.
   */
  private class Country implements Comparable<Country> {

    /** The country code */
    String code = null;

    /** The country name */
    String name = null;

    /**
     * Creates a new country with the given country code and name.
     * 
     * @param code
     *          the country code
     * @param name
     *          the name
     */
    Country(String code, String name) {
      this.code = code;
      this.name = name;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Country o) {
      return name.compareTo(o.name);
    }
  }

}