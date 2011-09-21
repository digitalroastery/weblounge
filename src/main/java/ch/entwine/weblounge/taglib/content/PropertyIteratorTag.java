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

package ch.entwine.weblounge.taglib.content;

import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.request.WebloungeRequest;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;

/**
 * This tag iterates over a property.
 */
public class PropertyIteratorTag extends AbstractContentIteratorTag {

  /** The serial version id */
  private static final long serialVersionUID = -5705402493357299735L;

  /** The iteration index */
  protected int index = 0;

  /** The number of iterations */
  protected int iterations = -1;

  /** The minimum number of iterations */
  protected int minOccurs = -1;

  /** The maximum number of iterations */
  protected int maxOccurs = -1;

  /** The property name */
  protected String propertyName = null;

  /** The property values */
  protected List<PropertyValue> propertyValues = null;

  /**
   * Sets the regular expression that selects the properties to iterate over.
   * 
   * @param value
   *          the regular expression for the property name
   */
  public void setPropertys(String value) {
    propertyName = StringUtils.trim(value);
  }

  /**
   * Sets the minimum number of iterations.
   * 
   * @param value
   *          the minimum number of iterations
   */
  public void setMinOccurs(String value) {
    minOccurs = Integer.parseInt(value);
  }

  /**
   * Sets the maximum number of iterations.
   * 
   * @param value
   *          the maximum number of iterations
   */
  public void setMaxOccurs(String value) {
    maxOccurs = Integer.parseInt(value);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {

    // If pagelet is null, then this is the first iteration and the tag needs
    // to be initialized
    if (propertyValues == null) {
      Pagelet pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);

      // Do we have a pagelet?
      if (pagelet == null)
        return SKIP_BODY;

      // Initialize the tag
      propertyValues = new ArrayList<PropertyValue>();
      Pattern p = Pattern.compile(propertyName);
      for (String propertyName : pagelet.getPropertyNames()) {
        Matcher m = p.matcher(propertyName);
        if (m.matches()) {
          String[] values = pagelet.getMultiValueProperty(propertyName);
          if (values != null) {
            for (String value : values) {
              propertyValues.add(new PropertyValue(propertyName, value));
            }
          }
        }
      }

      setupPropertyData();
      Collections.sort(propertyValues);

      // Are there values to iterate over?
      if (iterations == 0)
        return SKIP_BODY;
    }

    // Get the first property value
    PropertyValue propertyValue = propertyValues.get(index);

    pageContext.setAttribute(PropertyIteratorTagVariables.ITERATIONS, new Integer(iterations));
    pageContext.setAttribute(PropertyIteratorTagVariables.INDEX, new Integer(index));
    pageContext.setAttribute(PropertyIteratorTagVariables.PROPERTY_NAME, propertyValue.getName());
    pageContext.setAttribute(PropertyIteratorTagVariables.PROPERTY_VALUE, propertyValue.getValue());

    return EVAL_BODY_INCLUDE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.content.AbstractContentIteratorTag#doAfterBody()
   */
  public int doAfterBody() {
    index++;
    if (index >= iterations)
      return SKIP_BODY;

    // Get the current property value
    PropertyValue propertyValue = propertyValues.get(index);

    pageContext.setAttribute(PropertyIteratorTagVariables.INDEX, new Integer(index));
    pageContext.setAttribute(PropertyIteratorTagVariables.PROPERTY_NAME, propertyValue.getName());
    pageContext.setAttribute(PropertyIteratorTagVariables.PROPERTY_VALUE, propertyValue.getValue());

    return EVAL_BODY_AGAIN;
  }

  /**
   * Selects the set of property values over which to iterate.
   */
  protected void setupPropertyData() {
    int cardinality = -1;
    index = 0;

    // Did we find the property?
    if (propertyValues != null) {
      cardinality = propertyValues.size();
      if (maxOccurs >= 0)
        iterations = Math.min(maxOccurs, cardinality);
      else
        iterations = cardinality;
    } else {
      iterations = 0;
      cardinality = 0;
    }

    // Are there enough values to iterate over?
    if (minOccurs >= 0 && minOccurs > cardinality)
      iterations = 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#doEndTag()
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(PropertyIteratorTagVariables.ITERATIONS);
    pageContext.removeAttribute(PropertyIteratorTagVariables.INDEX);
    pageContext.removeAttribute(PropertyIteratorTagVariables.PROPERTY_NAME);
    pageContext.removeAttribute(PropertyIteratorTagVariables.PROPERTY_VALUE);
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.content.AbstractContentIteratorTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    propertyValues = null;
    propertyName = null;
    index = 0;
    iterations = 0;
    minOccurs = -1;
    maxOccurs = -1;
  }

  /**
   * This class holds for every property value the corresponding property name.
   */
  private final static class PropertyValue implements Comparable<PropertyValue> {

    /** The property name */
    private String propertyName = null;

    /** The property value */
    private String propertyValue = null;

    /**
     * Creates a new property value - name mapping.
     * 
     * @param propertyName
     *          the property name
     * @param propertyValue
     *          the property value
     */
    public PropertyValue(String propertyName, String propertyValue) {
      this.propertyName = propertyName;
      this.propertyValue = propertyValue;
    }

    /**
     * Returns the property name.
     * 
     * @return the property name
     */
    String getName() {
      return propertyName;
    }

    /**
     * Returns the property value.
     * 
     * @return the property value
     */
    String getValue() {
      return propertyValue;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(PropertyValue v) {
      int comparison = propertyName.compareTo(v.propertyName);
      if (comparison != 0)
        return comparison;
      return propertyValue.compareTo(v.propertyValue);
    }

  }

}
