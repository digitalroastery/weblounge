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

import ch.entwine.weblounge.common.editor.EditingState;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.jsp.JspException;

/**
 * This tag prints out a placeholder for the HTML header tags, that will later
 * on be replaced by the response once all action handlers and pagelets have had
 * the chance to contribute their HTML head elements.
 */
public class HTMLHeaderTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -1813975272420106327L;

  /** Logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(HTMLHeaderTag.class);

  /**
   * Does the tag processing.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  @Override
  public int doEndTag() throws JspException {
    HTMLAction action = (HTMLAction) getRequest().getAttribute(WebloungeRequest.ACTION);

    // See what the action has to contribute
    boolean skipHeaders = false;
    try {
      pageContext.getOut().flush();
      if (action != null && action.startHeader(request, response) == HTMLAction.SKIP_HEADER) {
        pageContext.getOut().flush();
        skipHeaders = true;
        return EVAL_PAGE;
      }
    } catch (Exception e) {
      logger.error("Error asking action '" + action + "' for headers", e);
    }

    // Write links & scripts to output
    try {
      pageContext.getOut().flush();

      // Write the marker to the response.
      if (!skipHeaders)
        pageContext.getOut().println(WebloungeResponse.HTML_HEADER_MARKER);

      if (request.getParameter(EditingState.WORKBENCH_PREVIEW_PARAM) != null) {
        pageContext.getOut().print("<script>$(document).ready(function() { $('form').submit(function(event) { event.preventDefault(); }); $('a').click(function(event) { event.preventDefault(); }); });</script>");
      }

      pageContext.getOut().flush();
    } catch (IOException e) {
      throw new JspException();
    }

    return super.doEndTag();
  }

}
