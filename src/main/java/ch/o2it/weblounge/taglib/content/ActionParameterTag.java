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

import ch.o2it.weblounge.taglib.WebloungeTag;

import javax.servlet.jsp.JspException;

/**
 * Parameter tag which must be nested inside an action tag.
 */
public class ActionParameterTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = 6518301782108444957L;

  /** parameter name */
  private String name = null;

  /** parameter value */
  private String value = null;

  /**
   * Constructor for class ActionParameterTag.
   */
  public ActionParameterTag() {
  }

  /**
   * Name of the property to be set. <b>Note:</b> Setting the property will have
   * no effect if either an unknown or emtpy property name was given or if the
   * user does not satisfy the role requirements set up via the option
   * <code>role</code> attribute.
   * 
   * @param name
   *          the property name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The parameter value. <b>Note:</b> Setting the property will have no effect
   * if either an unknown or empty property name was given or if the user does
   * not satisfy the role requirements set up via the option <code>role</code>
   * attribute.
   * 
   * @param value
   *          the property value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Process the end tag for this instance. Call the enclosing <code>
	* ModuleTag</code>
   * and tell it to configure the module, but make sure that the role
   * requirements (if set up via the optional attribute <code>role</code>) are
   * met.
   * 
   * @return either EVAL_PAGE or SKIP_PAGE
   */
  public int doStartTag() throws JspException {
    ActionTag action = (ActionTag) findAncestorWithClass(this, ActionTag.class);
    if (action != null) {
      if (!name.equals("")) {
        action.addParameter(name, value);
      }
    }
    return SKIP_BODY;
  }

}
