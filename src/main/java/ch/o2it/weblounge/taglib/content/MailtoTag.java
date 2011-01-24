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

import ch.o2it.weblounge.common.user.WebloungeUser;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * This tag prints out a link to the specified action.
 */
public class MailtoTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -4045932330101731516L;

  /** Logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(MailtoTag.class);

  /** The login */
  private String login = null;

  /** The tag body */
  private String body = null;

  /**
   * Sets the user login.
   * 
   * @param login
   *          the user's login
   */
  public void setUser(String login) {
    this.login = login;
  }

  /**
   * Process the start tag for this instance
   * 
   * @return either a EVAL_BODY_INCLUDE or a SKIP_BODY
   */
  public int doStartTag() throws JspException {
    return EVAL_BODY_BUFFERED;
  }

  /**
   * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
   */
  public int doAfterBody() {
    if (getBodyContent() != null) {
      body = getBodyContent().getString();
    }
    return SKIP_BODY;
  }

  /**
   * This method is called after the action tag body has been evaluated. Like
   * this, the action parameters had the chance to tell us about parameters.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    try {
      StringBuffer a = new StringBuffer("<a ");
      WebloungeUser user = request.getSite().getUser(login);
      String email = user.getEmail();
      if (email != null) {
        a.append("href=\"mailto:" + email + "\">");
      }
      if (body != null) {
        a.append(body.trim());
      } else {
        a.append(user.getName());
      }
      if (email != null) {
        a.append("</a>");
      }
      JspWriter out = pageContext.getOut();
      out.println(a.toString());
    } catch (IOException e) {
      logger.warn("Error when writing mailto tag: " + e.getMessage());
    }
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  public void reset() {
    login = null;
  }

}
