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

package ch.o2it.weblounge.test.site;

import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.impl.language.LanguageUtils;
import ch.o2it.weblounge.common.impl.request.RequestUtils;
import ch.o2it.weblounge.common.impl.site.HTMLActionSupport;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.HTMLAction;
import ch.o2it.weblounge.test.util.TestSiteUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Simple test action that is able to render a greeting on the site template.
 */
public class GreeterHTMLAction extends HTMLActionSupport {

  /** Name of the language parameter */
  public static final String LANGUAGE_PARAM = "language";

  /** The greetings */
  protected String greeting = null;

  /** The greeting's language */
  protected String language = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.site.HTMLActionSupport#configure(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse,
   *      ch.o2it.weblounge.common.request.RequestFlavor)
   */
  public void configure(WebloungeRequest request, WebloungeResponse response,
      RequestFlavor flavor) throws ActionException {
    super.configure(request, response, flavor);
    Map<String, String> allGreetings = TestSiteUtils.loadGreetings();
    try {
      language = getLanguage(request);
      greeting = allGreetings.get(language);
    } catch (IllegalStateException e) {
      throw new ActionException("Language parameter '" + LANGUAGE_PARAM + "' was not specified");
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.impl.site.HTMLActionSupport#startStage(ch.o2it.weblounge.common.request.WebloungeRequest,
   *      ch.o2it.weblounge.common.request.WebloungeResponse,
   *      ch.o2it.weblounge.common.content.page.Composer)
   */
  public int startStage(WebloungeRequest request, WebloungeResponse response,
      Composer composer) throws ActionException {
    try {
      response.setCharacterEncoding("UTF-8");

      String htmlGreeting = StringEscapeUtils.escapeHtml(greeting);
      IOUtils.write("<h1>" + htmlGreeting + "</h1>", response.getWriter());

      // Include another pagelet
      include(request, response, "include", null);
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

}
