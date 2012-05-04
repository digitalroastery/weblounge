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

package ch.entwine.weblounge.taglib.security;

import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.taglib.WebloungeTag;

import javax.servlet.jsp.JspException;

/**
 * This tag checks if the user specified by the parameters is defined in the
 * user registration database.
 */
public abstract class UserCheckTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = 6402453515629077507L;

  /** The user identifier */
  protected String user = null;

  /**
   * Sets the user.
   * 
   * @param user
   *          the user login
   */
  public final void setUser(String user) {
    this.user = user;
  }

  /**
   * This method is called if the start of a <code>&lt;ufuser&gt;</code> tag is
   * found. The method evaluates whether the given service is currently enabled.
   * If so, it returns <code>EVAL_BODY_INCLUDE</code> which will trigger tag
   * body evaluation, <code>SKIP_BODY</code> otherwise.
   * 
   * @return the corresponding <code>int</code> to skip or include the tag body
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();
    User user = request.getUser();
    if (skip(user)) {
      return SKIP_BODY;
    } else if (user != null) {
      stashAndSetAttribute(UserCheckTagVariables.USER, user);
    }
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  public void reset() {
    user = null;
  }

  /**
   * This method is used to define whether this tag skips on an available or a
   * non-available user.
   * 
   * @param user
   *          the user to check
   * @return <code>true</code> to skip the tag body
   */
  protected abstract boolean skip(User user);

}
