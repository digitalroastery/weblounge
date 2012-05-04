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

import javax.servlet.jsp.JspException;

/**
 * This tag iterates over multiple values of an element.
 */
public class ElementValueIteratorTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = -5705402493357299735L;

  /** The element name */
  private String elementName = null;

  /** The iteration index */
  protected int index = 0;

  /** The number of iterations */
  protected int iterations = -1;

  /** The minimum number of iterations */
  protected int minOccurs = -1;

  /** The maximum number of iterations */
  protected int maxOccurs = -1;

  /** The element values */
  protected String[] elementValues = null;

  /**
   * Sets the element to iterate over.
   * 
   * @param value
   *          the element name
   */
  public void setElement(String value) {
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
      elementValues = pagelet.getMultiValueContent(elementName, request.getLanguage());
      setupElementData();

      // Are there values to iterate over?
      if (iterations == 0)
        return SKIP_BODY;
    }

    // Get the first element value
    String propertyValue = elementValues[index];

    stashAndSetAttribute(ElementIteratorTagVariables.ITERATIONS, new Integer(iterations));
    stashAndSetAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));
    stashAndSetAttribute(ElementIteratorTagVariables.ELEMENT_NAME, elementName);
    stashAndSetAttribute(ElementIteratorTagVariables.ELEMENT_VALUE, propertyValue);

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
    String elementValue = elementValues[index];

    pageContext.setAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));
    pageContext.setAttribute(ElementIteratorTagVariables.ELEMENT_VALUE, elementValue);

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
      cardinality = elementValues.length;
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
    removeAndUnstashAttributes();
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

}
