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

import ch.o2it.weblounge.common.impl.util.enumeration.Countries;
import ch.o2it.weblounge.taglib.WebloungeTag;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

public class CountryListTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = 1326786853708846890L;

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
    Properties countries = Countries.getCountries(getRequest().getLanguage());
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
        writer.print(getStandardAttributes());
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