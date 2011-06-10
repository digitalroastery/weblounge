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

import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * This tag is used to create a from action to a specific weblounge action.
 */
public class FormTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = 5004974746165447409L;
  
  /** The logger */
  private static Logger logger = LoggerFactory.getLogger(FormTag.class);

  /** Form method, POST, PUT etc */
  private String method = "post";

  /** The module identifier of the action to link */
  private Module module = null;

  /** The action identifier to link */
  private String actionId = null;

  /** The accepted content types */
  private String accept = null;

  /** The character set */
  private String acceptCharset = null;

  /** The form encoding */
  private String enctype = null;

  /** The form target */
  private String target = null;

  /**
   * Sets the module identifier. This method throws an exception if no module
   * with the given identifier can be found.
   * 
   * @param moduleId
   *          The module to set.
   */
  public void setModule(String moduleId) throws JspTagException {
    Site site = request.getSite();
    module = site.getModule(moduleId);
    if (module == null) {
      String msg = "Module '" + module + "' not found!";
      throw new JspTagException(msg);
    }
  }

  /**
   * @param action
   *          The action to set.
   */
  public void setAction(String action) {
    actionId = action;
  }

  /**
   * @param method
   *          The method to set.
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Sets the target. Target specifies the link target, which is one of
   * <ul>
   * <li>blank_</li>
   * <li>parent_</li>
   * <li>self_</li>
   * <li>top_</li>
   * </ul>
   * 
   * @param target
   *          the target
   */
  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Sets the <code>accept</code> attribute.
   * 
   * @param accept
   *          the accepted encoding types
   */
  public void setAccept(String accept) {
    this.accept = accept;
  }

  /**
   * Sets the <code>accept-charset</code> attribute.
   * 
   * @param charset
   *          the accepted character sets
   */
  public void setAcceptcharset(String charset) {
    this.acceptCharset = charset;
  }

  /**
   * Sets the <code>enctype</code> attribute.
   * 
   * @param enctype
   *          the form encoding
   */
  public void setEnctype(String enctype) {
    this.enctype = enctype;
  }

  /**
   * The start tag.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    super.doStartTag();
    StringBuffer a = new StringBuffer("<form ");
    if (method != null)
      a.append("method=\"" + method + "\" ");
    if (accept != null)
      a.append("accept=\"" + accept + "\" ");
    if (acceptCharset != null)
      a.append("accept-charset=\"" + acceptCharset + "\" ");
    if (enctype != null)
      a.append("enctype=\"" + enctype + "\" ");
    if (target != null)
      a.append("target=\"" + target + "\" ");

    // Add tag attributes
    for (Map.Entry<String, String> attribute : getStandardAttributes().entrySet()) {
      a.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
    }

    Action action = module.getAction(actionId);
    if (action == null) {
      String msg = "Action '" + actionId + "' could not be found!";
      throw new JspException(msg);
    }

    a.append(" action=\"" + action.getPath() + "\">");

    try {
      pageContext.getOut().write(a.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }

    return EVAL_BODY_INCLUDE;
  }

  /**
   * The end tag.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    try {
      pageContext.getOut().write("</form>");
      reset();
    } catch (IOException e) {
      logger.warn("Error writing form tag to page", e);
    }
    return super.doEndTag();
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#release()
   */
  public void reset() {
    method = "post";
    module = null;
    actionId = null;
    accept = null;
    acceptCharset = null;
    target = null;
    enctype = null;
  }

}
