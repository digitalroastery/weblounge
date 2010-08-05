/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.taglib.content;

import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.request.WebloungeRequest;

import javax.servlet.jsp.JspException;

/**
 * This tag iteratos over a property.
 */
public class PropertyIteratorTag extends AbstractContentIteratorTag {

  /** Serial version uid */
  private static final long serialVersionUID = -3408702889049578384L;

  /** The element to iterate over */
  private String property = null;

  /** The propery's cardinality */
  private int cardinality = -1;

  /** The pagelet in question */
  private Pagelet pagelet = null;

  /**
   * Sets the property to iterate over.
   * 
   *@param value
   *          the property name
   */
  public void setProperty(String value) {
    property = value;
    properties.add(value);
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();
    if (pagelet == null) {
      if (property != null && !property.trim().equals("")) {
        pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);
        if (pagelet == null) {
          return SKIP_BODY;
        }
        pageContext.setAttribute(PropertyIteratorTagVariables.PROPERTY_COUNT, new Integer(cardinality));
        pageContext.setAttribute(PropertyIteratorTagVariables.INDEX, new Integer(index));
        Object[] e = pagelet.getMultiValueContent(property);
        if (e != null) {
          cardinality = e.length;
        }
        if (cardinality > 0 || minOccurs > 0) {
          return EVAL_BODY_INCLUDE;
        }

      }
      return SKIP_BODY;
    }
    return EVAL_BODY_INCLUDE;
  }

  /**
   * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
   */
  public int doAfterBody() {
    if (super.doAfterBody() == EVAL_BODY_AGAIN) {
      if (index <= cardinality - 1 || index < minOccurs) {
        pageContext.setAttribute(PropertyIteratorTagVariables.INDEX, new Integer(index));
        return EVAL_BODY_AGAIN;
      }
    }
    return SKIP_BODY;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    property = null;
    cardinality = 0;
    pagelet = null;
    pageContext.removeAttribute(PropertyIteratorTagVariables.PROPERTY_COUNT);
    pageContext.removeAttribute(PropertyIteratorTagVariables.INDEX);
    return super.doEndTag();
  }

}
