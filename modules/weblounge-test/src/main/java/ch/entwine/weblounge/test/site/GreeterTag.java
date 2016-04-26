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

package ch.entwine.weblounge.test.site;

import ch.entwine.weblounge.test.util.TestSiteUtils;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Sample tag implementation.
 */
public class GreeterTag extends TagSupport {

  /** Serial version uid */
  private static final long serialVersionUID = 633023845095031930L;

  /** The language */
  private String language = null;

  /**
   * Sets the language.
   * 
   * @param language
   *          the language
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {
    try {
      Map<String, String> greetings = TestSiteUtils.loadGreetings();
      String greeting = null;
      if (language != null) {
        greeting = greetings.get(language);
        if (greeting == null)
          greeting = "Excuse me?";
      } else {
        String[] languages = greetings.keySet().toArray(new String[greetings.size()]);
        language = languages[(int)Math.random() * languages.length];
        greeting = greetings.get(language);
      }
      String encodedGreeting = StringEscapeUtils.escapeHtml4(greeting);
      pageContext.getOut().print("<div id=\"greeting\">");
      pageContext.getOut().print(encodedGreeting);
      pageContext.getOut().println("</div>");
      pageContext.getOut().flush();
    } catch (IOException ioe) {
      throw new JspException("IOException while writing to client" + ioe.getMessage());
    }
    return SKIP_BODY;
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
   */
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

}
