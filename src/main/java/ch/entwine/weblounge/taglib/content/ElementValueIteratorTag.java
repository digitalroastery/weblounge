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

  /** The element's cardinality */
  private int cardinality = -1;

  /** The minimum number of iterations */
  protected int minOccurs = -1;

  /** The maximum number of iterations */
  protected int maxOccurs = -1;

  /** The pagelet from the request */
  protected Pagelet pagelet = null;

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
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {

    // If pagelet is null, then this is the first iteration and the tag needs
    // to be initialized
    if (pagelet == null) {
      pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);

      // Do we have a pagelet?
      if (pagelet == null)
        return SKIP_BODY;

      // Initialize the tag
      setupElementData();

      // Are there elements to iterate over?
      if (iterations == 0)
        return SKIP_BODY;
    }

    pageContext.setAttribute(ElementIteratorTagVariables.ELEMENT_COUNT, new Integer(iterations));
    pageContext.setAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));

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
    pageContext.setAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));
    return EVAL_BODY_AGAIN;
  }

  /**
   * Selects the set of element values over which to iterate.
   */
  protected void setupElementData() {
    index = 0;
    Object[] e = pagelet.getMultiValueContent(elementName, request.getLanguage());

    // Did we find any elements?
    if (e != null) {
      cardinality = e.length;
      if (maxOccurs >= 0)
        iterations = Math.min(maxOccurs, cardinality);
      else
        iterations = cardinality;
    } else {
      iterations = 0;
      cardinality = 0;
    }

    // Are there enough elements to iterate over?
    if (minOccurs >= 0 && minOccurs > cardinality)
      iterations = 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#doEndTag()
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(ElementIteratorTagVariables.ELEMENT_COUNT);
    pageContext.removeAttribute(ElementIteratorTagVariables.INDEX);
    return super.doEndTag();
  }

  /**
   * Returns the index for the element.
   * 
   * @param element
   *          the content element name
   * @return the element's index
   */
  public int getIndex(String element) {
    Object[] e = pagelet.getMultiValueContent(element);
    if (e != null && e.length >= index - 1) {
      return index;
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.content.AbstractContentIteratorTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    pagelet = null;
    elementName = null;
    index = 0;
    cardinality = -1;
    minOccurs = -1;
    maxOccurs = -1;
  }

}
