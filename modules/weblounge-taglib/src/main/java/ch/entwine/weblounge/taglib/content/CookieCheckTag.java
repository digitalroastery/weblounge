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

import ch.entwine.weblounge.taglib.WebloungeTag;

import javax.servlet.http.Cookie;
import javax.servlet.jsp.JspException;

/**
 * Tag to provide support for &lt;ifcookie&gt; and &lt;ifnotcookie&gt; tag
 * implementations.
 */
public abstract class CookieCheckTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -197740339008123305L;

  /** The cookie identifier */
  private String name = null;

  /**
   * Sets the cookie name.
   * 
   * @param value
   *          the element name
   */
  public final void setName(String value) {
    this.name = value;
  }

  /**
   * This method is called if the start of a <code>&lt;ifcookie&gt;</code> tag
   * is found.
   * 
   * @return the corresponding <code>int</code> to skip or include the tag body
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();
    Cookie cookie = null;
    Cookie[] cookies = request.getCookies();
    for (int i = 0; i < cookies.length; i++) {
      if (name.equals(cookies[i].getName())) {
        cookie = cookies[i];
        break;
      }
    }
    if (skip(cookie)) {
      return SKIP_BODY;
    }
    stashAndSetAttribute("cookie", cookie);
    return EVAL_BODY_INCLUDE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
   */
  @Override
  public int doEndTag() throws JspException {
    removeAndUnstashAttributes();
    return super.doEndTag();
  }

  /**
   * Does the cleanup by resetting the instance variables to their initial
   * values.
   */
  public void reset() {
    name = null;
  }

  /**
   * This method is used to define wether this tag skips on an available or a
   * non-available cookie.
   * 
   * @param cookie
   *          the cookie to check
   * @return <code>true</code> to skip the tag body
   */
  protected abstract boolean skip(Cookie cookie);

}
