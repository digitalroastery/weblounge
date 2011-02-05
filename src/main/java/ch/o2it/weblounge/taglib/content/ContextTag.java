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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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

  /** Map of existing key-value pairs that are replaced */
  private Map<String, Object> existingVariables = new HashMap<String, Object>();

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
        if (variables.getAction() != null)
          define(variables.getAction(), request.getAttribute(WebloungeRequest.ACTION));
        if (variables.getComposer() != null)
          define(variables.getComposer(), request.getAttribute(WebloungeRequest.COMPOSER));
        if (variables.getLanguage() != null)
          define(variables.getLanguage(), request.getLanguage());
        if (variables.getPage() != null)
          define(variables.getPage(), request.getAttribute(WebloungeRequest.PAGE));
        if (variables.getPagelet() != null)
          define(variables.getPagelet(), request.getAttribute(WebloungeRequest.PAGELET));
        if (variables.getRepository() != null)
          define(variables.getRepository(), request.getSite().getContentRepository());
        if (variables.getSite() != null)
          define(variables.getSite(), request.getSite());
        if (variables.getUri() != null)
          define(variables.getUri(), uri);
        if (variables.getUrl() != null)
          define(variables.getUrl(), request.getRequestedUrl());
        if (variables.getUser() != null)
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
      if (variables.getAction() != null)
        pageContext.removeAttribute(variables.getAction());
      if (variables.getComposer() != null)
        pageContext.removeAttribute(variables.getComposer());
      if (variables.getLanguage() != null)
        pageContext.removeAttribute(variables.getLanguage());
      if (variables.getPage() != null)
        pageContext.removeAttribute(variables.getPage());
      if (variables.getPagelet() != null)
        pageContext.removeAttribute(variables.getPagelet());
      if (variables.getSite() != null)
        pageContext.removeAttribute(variables.getSite());
      if (variables.getUri() != null)
        pageContext.removeAttribute(variables.getUri());
      if (variables.getUrl() != null)
        pageContext.removeAttribute(variables.getUrl());
      if (variables.getUser() != null)
        pageContext.removeAttribute(variables.getUser());
    } else {
      pageContext.removeAttribute(ContextTagVariables.ACTION);
      pageContext.removeAttribute(ContextTagVariables.LANGUAGE);
      pageContext.removeAttribute(ContextTagVariables.SITE);
      pageContext.removeAttribute(ContextTagVariables.URL);
      pageContext.removeAttribute(ContextTagVariables.USER);
    }

    // Restore former values
    for (Map.Entry<String, Object> entry : existingVariables.entrySet()) {
      pageContext.setAttribute(entry.getKey(), entry.getValue());
    }

    return super.doEndTag();
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

    // If there is a value already, keep it for later reference
    if (existingValue != null) {
      existingVariables.put(key, existingValue);
      String existingType = existingValue.getClass().getName();
      logger.debug("Temporarily replacing context item '" + key + "' of type " + existingType + " with new value");
    }

    // Store the new value
    pageContext.setAttribute(key, value);
    logger.debug("Defining context item '{}': {}", key, value != null ? value : "null");
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    definitions = null;
    existingVariables.clear();
    uri = null;
    variables = null;
  }

}
