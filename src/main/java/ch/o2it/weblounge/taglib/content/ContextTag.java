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
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.taglib.ParseException;
import ch.o2it.weblounge.taglib.TagVariableDefinitionParser;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * The <code>ContextTag</code> can be used to setup the context for a jsp
 * template or pagelet.
 * <p>
 * The context implementation supports the following variables:
 * <ul>
 * <li><code>action</code> - the action handler</li>
 * <li><code>composer</code> - the current composer</li>
 * <li><code>language</code> - the current language</li>
 * <li><code>page</code> - the current page</li>
 * <li><code>pagelet</code> - the current pagelet</li>
 * <li><code>repository</code> - the site's content repository</li>
 * <li><code>site</code> - the site associated with the url
 * <li><code>uri</code> - the web applications uri</li>
 * <li><code>url</code> - the url information</li>
 * <li><code>user</code> - the current user</li>
 * </ul>
 */
public class ContextTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = 1L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ContextTag.class);

  /** The tag variable definitions */
  private String definitions = null;

  /** The parsed variable definitions */
  private ContextTagVariables variables = null;

  /** The application uri */
  // TODO: Implement a better way
  private static String uri = "/";

  /**
   * Defines the variables that should be set inside the context.
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
   * <li><code>action</code> - the action handler</li>
   * <li><code>language</code> - the current language</li>
   * <li><code>site</code> - the site associated with the url
   * <li><code>url</code> - the url information</li>
   * <li><code>user</code> - the current user</li>
   * </ul>
   * 
   * @return either a EVAL_BODY_INCLUDE or a SKIP_BODY
   */
  public int doStartTag() throws JspException {
    try {
      if (variables != null) {
        define(variables.getAction(), request.getAttribute(WebloungeRequest.ACTION));
        define(variables.getComposer(), request.getAttribute(WebloungeRequest.COMPOSER));
        define(variables.getLanguage(), request.getLanguage());
        define(variables.getPage(), request.getAttribute(WebloungeRequest.PAGE));
        define(variables.getPagelet(), request.getAttribute(WebloungeRequest.PAGELET));
        define(variables.getRepository(), getRepository(request.getSite()));
        define(variables.getSite(), request.getSite());
        define(variables.getUri(), uri);
        define(variables.getUrl(), request.getRequestedUrl());
        define(variables.getUser(), request.getUser());
      } else {
        define(ContextTagVariables.ACTION, request.getAttribute(WebloungeRequest.ACTION));
        define(ContextTagVariables.LANGUAGE, request.getLanguage());
        define(ContextTagVariables.SITE, request.getSite());
        define(ContextTagVariables.URL, request.getRequestedUrl());
        define(ContextTagVariables.USER, request.getUser());
      }
    } catch (IllegalStateException e) {
      logger.error("Error creating context: {}", e.getMessage());
      return SKIP_BODY;
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
      pageContext.removeAttribute(ContextTagVariables.ACTION);
      pageContext.removeAttribute(ContextTagVariables.LANGUAGE);
      pageContext.removeAttribute(ContextTagVariables.SITE);
      pageContext.removeAttribute(ContextTagVariables.URI);
      pageContext.removeAttribute(ContextTagVariables.URL);
      pageContext.removeAttribute(ContextTagVariables.USER);
    }
    return EVAL_PAGE;
  }

  /**
   * Returns the content repository that is associated with this site.
   * 
   * @param site
   *          the site
   * @return the site's repository
   */
  protected ContentRepository getRepository(Site site) {
    return ContentRepositoryFactory.getRepository(site);
  }

  /**
   * Defines <code>value</code> under the specified key as an attribute in the
   * page context. This method throws an <code>InvalidStateException</code> if a
   * different object is already defined in the request under the same key.
   * <p>
   * If <code>key</code> is null, this method returns silently.
   * 
   * @param key
   *          the attribute name
   * @param value
   *          the attribute value
   * @throws IllegalStateException
   *           if the value is <code>null</code> of if the variable is already
   *           defined in the page context
   */
  private void define(String key, Object value) throws IllegalStateException {
    if (key == null)
      return;
    Object existingValue = pageContext.getAttribute(key);
    String existingType = (existingValue != null) ? existingValue.getClass().getName() : "null";
    if (existingValue == null && value == null || existingValue != null && !existingValue.equals(value))
      throw new IllegalStateException("Context item '" + key + "' is already defined as " + existingType);
    pageContext.setAttribute(key, value);
    logger.debug("Defining context item '{}': {}", key, value != null ? value : "null");
  }

}
