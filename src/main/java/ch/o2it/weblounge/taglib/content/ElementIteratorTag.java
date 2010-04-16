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

import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.request.WebloungeRequest;

import javax.servlet.jsp.JspException;

/**
 * This tag iterates over an element.
 */
public class ElementIteratorTag extends AbstractContentIteratorTag {

  /** The serial version id */
  private static final long serialVersionUID = -5705402493357299735L;

  /** The pagelet in question */
  private Pagelet pagelet = null;

  /** The element's cardinality */
  private int cardinality = -1;

  /**
   * Sets the element to iterate over.
   * 
   * @param value
   *          the element name
   */
  public void setElement(String value) {
    elements.add(value);
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();
    if (pagelet == null) {
      if (elements.size() > 0) {
        pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);
        if (pagelet == null) {
          return SKIP_BODY;
        }
        pageContext.setAttribute(ElementIteratorTagVariables.ELEMENT_COUNT, new Integer(cardinality));
        pageContext.setAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));
        Object[] e = pagelet.getMultiValueContent(elements.get(0), request.getLanguage());
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
        pageContext.setAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));
        return EVAL_BODY_AGAIN;
      }
    }
    return SKIP_BODY;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    cardinality = 0;
    pagelet = null;
    pageContext.removeAttribute(ElementIteratorTagVariables.ELEMENT_COUNT);
    pageContext.removeAttribute(ElementIteratorTagVariables.INDEX);
    return super.doEndTag();
  }

}
