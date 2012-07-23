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

package ch.entwine.weblounge.test.site;

import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.site.HTMLActionSupport;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.ActionException;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.test.util.TestSiteUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Simple test action that is able to render a greeting on the site template.
 */
public class GreeterHTMLAction extends HTMLActionSupport {

  /** Name of the language parameter */
  public static final String LANGUAGE_PARAM = "language";

  /** Identifier of the included pagelet */
  public static final String PAGELET_ID = "include";

  /** Parameter name of code template */
  public static final String CODE_TEMPLATE = "code-template";

  /** The greetings */
  protected String greeting = null;

  /** The greeting's language */
  protected String language = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.site.HTMLActionSupport#configure(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse,
   *      ch.entwine.weblounge.common.request.RequestFlavor)
   */
  @Override
  public void configure(WebloungeRequest request, WebloungeResponse response,
      RequestFlavor flavor) throws ActionException {
    super.configure(request, response, flavor);

    // Load the greetings
    Map<String, String> allGreetings = TestSiteUtils.loadGreetings();
    try {
      language = getLanguage(request);
      greeting = allGreetings.get(language);
    } catch (IllegalStateException e) {
      throw new ActionException("Language parameter '" + LANGUAGE_PARAM + "' was not specified");
    }

    // Load the template
    String codeTemplate = StringUtils.trimToNull(request.getParameter(CODE_TEMPLATE));
    if (codeTemplate != null) {
      PageTemplate t = site.getTemplate(codeTemplate);
      if (t == null) {
        logger.warn("Template '{}' does not exist", codeTemplate);
      } else {
        logger.info("Setting template to '{}'", codeTemplate);
        setTemplate(t);
      }
    }

    use(module.getRenderer(PAGELET_ID));
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.impl.site.HTMLActionSupport#startStage(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse,
   *      ch.entwine.weblounge.common.content.page.Composer)
   */
  @Override
  public int startStage(WebloungeRequest request, WebloungeResponse response,
      Composer composer) throws ActionException {
    try {
      String htmlGreeting = StringEscapeUtils.escapeHtml(greeting);
      IOUtils.write("<h1>" + htmlGreeting + "</h1>", response.getWriter());

      // Include another pagelet
      include(request, response, PAGELET_ID, null);
      return HTMLAction.SKIP_COMPOSER;
    } catch (IOException e) {
      throw new ActionException("Unable to send json response", e);
    }
  }

  /**
   * Returns the language, which is either taken from the request parameter
   * <code>language</code> or from the language sent by the client browser.
   * The final fallback is <code>English</code>.
   * 
   * @param request the request
   * @return the language name
   */
  protected String getLanguage(WebloungeRequest request) {
    String language = RequestUtils.getParameter(request, LANGUAGE_PARAM);
    if (language == null) {
      Language en = LanguageUtils.getLanguage("en");
      language = request.getLanguage().getDescription(en).toLowerCase();
      if (language == null) {
        language = "english";
      }
    }
    return language;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.impl.site.HTMLActionSupport#passivate()
   */
  @Override
  public void passivate() {
    greeting = null;
    language = null;
    super.passivate();
  }

}
