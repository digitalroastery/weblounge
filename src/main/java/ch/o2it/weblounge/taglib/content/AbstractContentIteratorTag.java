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
import ch.o2it.weblounge.taglib.WebloungeTag;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;

/**
 * This tags iterates over multivalue pagelet content elements. If there are
 * elements or properties defined, the iteration will only walk over those
 * elements and properties. Otherwhise, the iteration will respect every
 * embedded multivalue element or property.
 */
public abstract class AbstractContentIteratorTag extends WebloungeTag {

  private static final long serialVersionUID = 6777331743017567453L;

  /** The list of other elements to iterate over */
  protected List<String> elements = null;

  /** The list of properties to iterate over */
  protected List<String> properties = null;

  /** The iteration index */
  protected int index = -1;

  /** The minimum number of iterations */
  protected int minOccurs = -1;

  /** The maximum number of iterations */
  protected int maxOccurs = -1;

  /** The pagelet from the request */
  private Pagelet pagelet = null;

  /**
   * Creates a new content iterator tag.
   */
  public AbstractContentIteratorTag() {
    elements = new ArrayList<String>();
    properties = new ArrayList<String>();
    reset();
  }

  /**
   * Sets the list of elements to iterate. The elements must consist of a list
   * of strings, separated by either ",", ";" or " ".
   * 
   * @param value
   *          the elements
   */
  public void setElements(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",; ");
    while (tok.hasMoreTokens()) {
      elements.add(String.valueOf(tok.nextElement()));
    }
  }

  /**
   * Sets the list of properties to iterate. The properties must consist of a
   * list of strings, separated by either ",", ";" or " ".
   * 
   * @param value
   *          the properties
   */
  public void setProperties(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",; ");
    while (tok.hasMoreTokens()) {
      properties.add(String.valueOf(tok.nextElement()));
    }
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
   * Returns the index for multivalue elements or properties that is to be
   * returned.
   * 
   * @param element
   *          the content element name
   * @return the element's index
   */
  public int getIndex(String element) {
    if (elements.size() == 0 || elements.contains(element)) {
      Object[] e = pagelet.getMultiValueContent(element);
      if (e != null && e.length >= index - 1) {
        return index;
      }
    }
    if (properties.size() == 0 || properties.contains(element)) {
      Object[] p = pagelet.getMultiValueProperty(element);
      if (p != null && p.length >= index - 1) {
        return index;
      }
    }
    return 0;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);
    if (pagelet != null) {
      return EVAL_BODY_INCLUDE;
    } else {
      return SKIP_BODY;
    }
  }

  /**
   * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
   */
  public int doAfterBody() {
    index++;
    if (maxOccurs == -1 || index < maxOccurs) {
      return EVAL_BODY_AGAIN;
    } else {
      return SKIP_BODY;
    }
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    reset();
    return super.doEndTag();
  }

  /**
   * Method called when the tag is released to the pool.
   * 
   * @see javax.servlet.jsp.tagext.Tag#release()
   */
  public void release() {
    reset();
    super.release();
  }

  /**
   * Initializes and resets this tag instance.
   */
  protected void reset() {
    super.reset();
    elements.clear();
    properties.clear();
    index = 0;
    minOccurs = -1;
    maxOccurs = -1;
  }

}
