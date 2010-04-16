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

import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.taglib.ParseException;
import ch.o2it.weblounge.taglib.TagVariableDefinitionParser;
import ch.o2it.weblounge.taglib.WebloungeTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * The <code>ContextTag</code> can be used to setup the context for a template
 * or a module. The context sets the following variables:
 * <ul>
 * <li><code>uri</code> - the web applications uri</li>
 * <li><code>url</code> - the url information</li>
 * <li><code>action</code> - the action handler</li>
 * <li><code>site</code> - the site associated with the url
 * <li><code>language</code> - the current language</li>
 * <li><code>user</code> - the current user</li>
 * <li><code>page</code> - the current page</li>
 * <li><code>pagelet</code> - the current pagelet</li>
 * <li><code>composer</code> - the current composer</li>
 * </ul>
 */
public class ContextTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = 1L;

  /** The tag variable definitions */
  private String definitions = null;

  /** The parsed variable definitions */
  private ContextTagVariables variables = null;

  /** The application uri */
  // TODO: Implement a better way
  private static String uri = "/";

  /**
   * Sets the tag variable definitions. This tag supports the following
   * variables:
   * <ul>
   * <li><code>uri</code> - the web applications uri</li>
   * <li><code>url</code> - the url information</li>
   * <li><code>action</code> - the action handler</li>
   * <li><code>site</code> - the site associated with the url
   * <li><code>language</code> - the current language</li>
   * <li><code>user</code> - the current user</li>
   * <li><code>page</code> - the current page</li>
   * <li><code>pagelet</code> - the current pagelet</li>
   * <li><code>composer</code> - the current composer</li>
   * <li><code>history</code> - the history</li>
   * <li><code>toolkit</code> - the template support</li>
   * <li><code>location</code> - the pagelet location</li>
   * <li><code>wizard</code> - the wizard handler</li>
   * </ul>
   * 
   * @param value
   *          the variable definitions
   * @throws JspException
   *           if the definition cannot be parsed
   */
  public final void setDefine(String value) throws JspException {
    if (definitions == null || !definitions.equals(value)) {
      try {
        variables = new ContextTagVariables(TagVariableDefinitionParser.parse(value));
      } catch (ParseException ex) {
        throw new JspTagException(ex.getMessage());
      }
    }
    definitions = value;
  }

  /**
   * Process the start tag for this instance by setting up either the defined
   * variables or the following set of default variables:
   * <ul>
   * <li><code>uri</code> - the web applications uri</li>
   * <li><code>url</code> - the url information</li>
   * <li><code>action</code> - the action handler</li>
   * <li><code>site</code> - the site associated with the url
   * <li><code>language</code> - the current language</li>
   * <li><code>user</code> - the current user</li>
   * <li><code>toolkit</code> - the jsp toolkit</li>
   * </ul>
   * 
   * @return either a EVAL_BODY_INCLUDE or a SKIP_BODY
   */
  public int doStartTag() throws JspException {
    // Tag variable export
    if (variables != null) {
      define(variables.getUri(), uri);
      define(variables.getSite(), request.getSite());
      define(variables.getUrl(), request.getUrl());
      define(variables.getAction(), request.getAttribute(WebloungeRequest.ACTION));
      define(variables.getLanguage(), request.getLanguage());
      define(variables.getUser(), request.getUser());
      define(variables.getPage(), request.getAttribute(WebloungeRequest.PAGE));
      define(variables.getPagelet(), request.getAttribute(WebloungeRequest.PAGELET));
      define(variables.getComposer(), request.getAttribute(WebloungeRequest.COMPOSER));
    } else {
      define(ContextTagVariables.URI, uri);
      define(ContextTagVariables.URL, request.getUrl());
      define(ContextTagVariables.ACTION, request.getAttribute(WebloungeRequest.ACTION));
      define(ContextTagVariables.SITE, request.getSite());
      define(ContextTagVariables.LANGUAGE, request.getLanguage());
      define(ContextTagVariables.USER, request.getUser());
    }
    return EVAL_BODY_INCLUDE;
  }

  /**
   * When this method is called, all variable definitions are revoked.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    if (variables != null) {
      if (variables.getUri() != null)
        pageContext.removeAttribute(variables.getUri());
      if (variables.getSite() != null)
        pageContext.removeAttribute(variables.getSite());
      if (variables.getUrl() != null)
        pageContext.removeAttribute(variables.getUrl());
      if (variables.getLanguage() != null)
        pageContext.removeAttribute(variables.getLanguage());
      if (variables.getUser() != null)
        pageContext.removeAttribute(variables.getUser());
      if (variables.getPage() != null)
        pageContext.removeAttribute(variables.getPage());
      if (variables.getPagelet() != null)
        pageContext.removeAttribute(variables.getPagelet());
      if (variables.getComposer() != null)
        pageContext.removeAttribute(variables.getComposer());
    } else {
      pageContext.removeAttribute(ContextTagVariables.URI);
      pageContext.removeAttribute(ContextTagVariables.SITE);
      pageContext.removeAttribute(ContextTagVariables.URL);
      pageContext.removeAttribute(ContextTagVariables.LANGUAGE);
      pageContext.removeAttribute(ContextTagVariables.USER);
    }
    return EVAL_PAGE;
  }

  /**
   * Defines <code>value</code> under the specified key as an attribute in the
   * page context.
   * 
   * @param key
   *          the attribute name
   * @param value
   *          the attribute value
   */
  private void define(String key, Object value) {
    if (key != null && pageContext.getAttribute(key) == null)
      pageContext.setAttribute(key, value);
  }

}
