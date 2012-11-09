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

import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/**
 * This tag prints out a link to the specified action.
 */
public class ActionTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = 2609596492681590569L;

  /** Logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(ActionTag.class);

  /** the module identifier */
  private Module module = null;

  /** action handler bundle configuration */
  private Action action = null;

  /** the action identifier */
  private String actionId = null;

  /** the target url */
  private String target = null;

  /** the parameters */
  private Map<String, String> parameters = null;

  /** tag body */
  private String body = null;

  /**
   * Creates a new action tag.
   */
  public ActionTag() {
    parameters = new HashMap<String, String>();
  }

  /**
   * Sets the module identifier. This method throws an exception if no module
   * with the given identifier can be found.
   * 
   * @param module
   *          The module to set.
   */
  public void setModule(String module) throws JspTagException {
    Site site = request.getSite();
    this.module = site.getModule(module);
    if (this.module == null) {
      String msg = "Module '" + module + "' not found!";
      throw new JspTagException(msg);
    }
  }

  /**
   * Sets the action identifier.
   * 
   * @param action
   *          the action identifier
   */
  public void setAction(String action) {
    actionId = action;
  }

  /**
   * Sets the action target url. If ommitted, the target url configured in the
   * action definition is chosen.
   * 
   * @param url
   *          the target url
   */
  public void setTarget(String url) {
    target = url;
  }

  /**
   * Adds a parameter to the list of parameters.
   * 
   * @param name
   *          the parameter name
   * @param value
   *          the parameter value
   */
  public void addParameter(String name, String value) {
    if (value == null)
      value = "";
    parameters.put(name, value);
  }

  /**
   * Process the start tag for this instance
   * 
   * @return either a EVAL_BODY_INCLUDE or a SKIP_BODY
   */
  public int doStartTag() throws JspException {
    if (RequestUtils.isPrecompileRequest(request))
      return SKIP_BODY;
    
    action = module.getAction(actionId);
    if (action == null) {
      logger.warn("Action handler '" + actionId + "' not found for module '" + module + "' and site '" + request.getSite() + "'");
      return SKIP_BODY;
    }
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
      if (action == null)
        return EVAL_PAGE;
      
      // Add cache tags
      response.addTag(CacheTag.Module, action.getModule().getIdentifier());
      response.addTag(CacheTag.Action, action.getIdentifier());

      StringBuffer a = new StringBuffer("<a ");
      if (getId() != null)
        a.append("id=\"" + getId() + "\"");

      a.append("href=\"");
      a.append(StringEscapeUtils.escapeXml(action.getPath()));

      // Add target url if present
      StringBuffer params = new StringBuffer("");

      // Add other parameters
      Iterator<String> pi = parameters.keySet().iterator();
      while (pi.hasNext()) {
        String param = pi.next();
        String value = URLEncoder.encode(parameters.get(param), "utf-8");
        params.append((params.length() == 0 ? "?" : "&") + param + "=" + value);
      }

      a.append(StringEscapeUtils.escapeXml(params.toString()));
      a.append("\"");

      if (target != null) {
        a.append(" target=\"");
        a.append(StringEscapeUtils.escapeXml(target));
        a.append("\"");
      }

      a.append(">");

      if (body != null) {
        a.append(body.trim());
      }
      a.append("</a>");
      JspWriter out = pageContext.getOut();
      out.print(a.toString());

    } catch (IOException e) {
      logger.warn("Error when writing action tag: " + e.getMessage());
    }

    return super.doEndTag();
  }

  /**
	 */
  public void reset() {
    module = null;
    actionId = null;
    action = null;
    parameters.clear();
  }

}
