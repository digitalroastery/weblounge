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

import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.taglib.WebloungeTag;

import javax.servlet.jsp.JspException;

/**
 * Tag to provide support for &lt;ifaction&gt; and &lt;ifnotaction&gt; tag
 * implementations.
 */
public abstract class ActionCheckTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -6049927247109426852L;

  /** String to match all modules/actions */
  public static final String MATCH_ALL = "*";

  /** The module identifier */
  protected String module = MATCH_ALL;

  /** The action identifier */
  protected String action = MATCH_ALL;

  /**
   * Sets the module identifier. Set an asterisk (*) to match any module
   * identifier.
   * 
   * @param value
   *          the module identifier
   */
  public final void setModule(String value) {
    module = value;
  }

  /**
   * Sets the action identifier. Set an asterisk (*) to match any action
   * identifier.
   * 
   * @param value
   *          the action identifier
   */
  public final void setAction(String value) {
    action = value;
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
    Action handler = (Action) request.getAttribute(WebloungeRequest.ACTION);
    stashAndSetAttribute(ActionCheckTagVariables.ACTION, handler);
    if (skip(handler)) {
      return SKIP_BODY;
    }
    return EVAL_BODY_INCLUDE;
  }

  /**
   * Overwritten to remove the action variable binding.
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
   */
  public int doEndTag() throws JspException {
    removeAndUnsstashAttribute(ActionCheckTagVariables.ACTION);
    return super.doEndTag();
  }

  /**
   * Does the cleanup by resetting the instance variables to their initial
   * values.
   */
  public void reset() {
    module = MATCH_ALL;
    action = MATCH_ALL;
  }

  /**
   * This method is used to define whether this tag skips on an available or a
   * non-available action handler.
   * 
   * @param handler
   *          the action handler to check
   * @return <code>true</code> to skip the tag body
   */
  protected abstract boolean skip(Action handler);

  /**
   * Returns <code>true</code> if <code>module</code> and <code>action</code>
   * matches the given action handler.
   * 
   * @param handler
   *          the action handler
   * @return <code>true</code> if the handler matches
   */
  protected boolean matches(Action handler) {
    assert handler != null;
    if (MATCH_ALL.equals(module) && MATCH_ALL.equals(action))
      return true;
    else if (handler.getModule().getIdentifier().equals(module) && MATCH_ALL.equals(action))
      return true;
    else if (handler.getIdentifier().equals(action) && MATCH_ALL.equals(module))
      return true;
    else if (handler.getModule().getIdentifier().equals(module) && handler.getIdentifier().equals(action))
      return true;
    return false;
  }

}
