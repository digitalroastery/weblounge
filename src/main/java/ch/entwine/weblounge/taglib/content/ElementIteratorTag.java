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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;

/**
 * This tag iterates over an element.
 */
public class ElementIteratorTag extends AbstractContentIteratorTag {

  /** The serial version id */
  private static final long serialVersionUID = -5705402493357299735L;

  /** The element's cardinality */
  private int cardinality = -1;
  
  private String regex = null;

  /**
   * Sets the element to iterate over.
   * 
   * @param value
   *          the element name
   */
  public void setElement(String value) {
    if (regex != null)
      throw new IllegalStateException("Cannot specify both 'regex' and 'element' attribute");
    elements.add(value);
  }
  
  public void setRegex(String regex) {
    if (elements.size() > 0)
      throw new IllegalStateException("Cannot specify both 'regex' and 'element' attribute");
    this.regex = regex;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();

    if(cardinality == -1 && pagelet != null) {
      // Have elements been specified explicitly?
      if (elements.size() > 0) {
        Object[] e = pagelet.getMultiValueContent(elements.get(0), request.getLanguage());
        if (e != null) {
          cardinality = e.length;
        }      
      }
      
      // Try to select elements by regular expression
      else if (regex != null) {
        Pattern p = Pattern.compile(regex);
        for (String elementName : pagelet.getContentNames(request.getLanguage())) {
          Matcher m = p.matcher(elementName);
          if (m.matches()) {
            elements.add(elementName);
          }
        }
        cardinality = elements.size();
        index = 0;
      }
    }
    
    // Did we find elements to iterate over?
    if (cardinality <= 0 && !(index < minOccurs)) {
      return SKIP_BODY;
    }

    pageContext.setAttribute(ElementIteratorTagVariables.ELEMENT_COUNT, new Integer(cardinality));
    pageContext.setAttribute(ElementIteratorTagVariables.INDEX, new Integer(index));

    return EVAL_BODY_INCLUDE;
  }

  /**
   * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
   */
  public int doAfterBody() {
    if (super.doAfterBody() == EVAL_BODY_AGAIN) {
      if (index < cardinality || index < minOccurs) {
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
    pageContext.removeAttribute(ElementIteratorTagVariables.ELEMENT_COUNT);
    pageContext.removeAttribute(ElementIteratorTagVariables.INDEX);
    return super.doEndTag();
  }
  
  @Override
  protected void reset() {
    super.reset();
    cardinality = -1;
    regex = null;
  }

}
