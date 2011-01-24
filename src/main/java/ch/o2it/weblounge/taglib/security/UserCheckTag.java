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

package ch.o2it.weblounge.taglib.security;

import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.taglib.WebloungeTag;

import javax.servlet.jsp.JspException;

/**
 * This tag checks if the user specified by the parameters is defined in the
 * user registration database.
 */
public abstract class UserCheckTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = 6402453515629077507L;

  /** The user identifier */
  private String user = null;

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
   * This method is called if the start of a <code>&lt;ifservice&gt;</code> tag
   * is found. The method evaluates whether the given service is currently
   * enabled. If so, it returns <code>EVAL_BODY_INCLUDE</code> which will
   * trigger tag body evaluation, <code>SKIP_BODY</code> otherwise.
   * 
   * @return the corresponding <code>int</code> to skip or include the tag body
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();
    Site site = getRequest().getSite();
    User user = site.getUser(this.user);
    if (skip(user)) {
      return SKIP_BODY;
    } else if (user != null) {
      pageContext.setAttribute(UserCheckTagVariables.USER, user);
    }
    return EVAL_BODY_INCLUDE;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.taglib.WebloungeTag#reset()
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
