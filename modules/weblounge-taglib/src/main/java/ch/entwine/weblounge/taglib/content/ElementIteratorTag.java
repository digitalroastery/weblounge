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
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;

/**
 * This tag iterates over the values of multiple elements.
 */
public class ElementIteratorTag extends WebloungeTag {

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

  /** The element name */
  protected String elementName = null;

  /** The element values */
  protected List<ElementValue> elementValues = null;

  /**
   * Sets the regular expression that selects the elements to iterate over.
   * 
   * @param value
   *          the regular expression for the element name
   */
  public void setElements(String value) {
    elementName = StringUtils.trim(value);
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
    if (elementValues == null) {
      Pagelet pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);

      // Do we have a pagelet?
      if (pagelet == null)
        return SKIP_BODY;

      // Initialize the tag
      elementValues = new ArrayList<ElementValue>();
      Pattern p = Pattern.compile(elementName);
      for (String elementName : pagelet.getContentNames(request.getLanguage())) {
        Matcher m = p.matcher(elementName);
        if (m.matches()) {
          String[] values = pagelet.getMultiValueContent(elementName, request.getLanguage());
          if (values != null) {
            for (String value : values) {
              elementValues.add(new ElementValue(elementName, value));
            }
          }
        }
      }

      setupElementData();

      // Are there values to iterate over?
      if (iterations == 0)
        return SKIP_BODY;
    }

    // Get the first element value
    ElementValue elementValue = elementValues.get(index);

    pageContext.setAttribute(ElementIteratorTagVariables.ITERATIONS, new Integer(iterations));
    pageContext.setAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));
    pageContext.setAttribute(ElementIteratorTagVariables.ELEMENT_NAME, elementValue.getName());
    pageContext.setAttribute(ElementIteratorTagVariables.ELEMENT_VALUE, elementValue.getValue());

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

    // Get the current element value
    ElementValue elementValue = elementValues.get(index);

    pageContext.setAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));
    pageContext.setAttribute(ElementIteratorTagVariables.ELEMENT_NAME, elementValue.getName());
    pageContext.setAttribute(ElementIteratorTagVariables.ELEMENT_VALUE, elementValue.getValue());

    return EVAL_BODY_AGAIN;
  }

  /**
   * Selects the set of element values over which to iterate.
   */
  protected void setupElementData() {
    int cardinality = -1;
    index = 0;

    // Did we find the element?
    if (elementValues != null) {
      cardinality = elementValues.size();
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
    pageContext.removeAttribute(ElementIteratorTagVariables.ITERATIONS);
    pageContext.removeAttribute(ElementIteratorTagVariables.INDEX);
    pageContext.removeAttribute(ElementIteratorTagVariables.ELEMENT_NAME);
    pageContext.removeAttribute(ElementIteratorTagVariables.ELEMENT_VALUE);
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
    elementValues = null;
    elementName = null;
    index = 0;
    iterations = 0;
    minOccurs = -1;
    maxOccurs = -1;
  }

  /**
   * This class holds for every element value the corresponding element name.
   */
  private final static class ElementValue implements Comparable<ElementValue> {

    /** The element name */
    private String elementName = null;

    /** The element value */
    private String elementValue = null;

    /**
     * Creates a new element value - name mapping.
     * 
     * @param elementName
     *          the element name
     * @param elementValue
     *          the element value
     */
    public ElementValue(String elementName, String elementValue) {
      this.elementName = elementName;
      this.elementValue = elementValue;
    }

    /**
     * Returns the element name.
     * 
     * @return the element name
     */
    String getName() {
      return elementName;
    }

    /**
     * Returns the element value.
     * 
     * @return the element value
     */
    String getValue() {
      return elementValue;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ElementValue v) {
      int comparison = elementName.compareTo(v.elementName);
      if (comparison != 0)
        return comparison;
      return elementValue.compareTo(v.elementValue);
    }

  }

}
