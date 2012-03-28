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

import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * Sends an html construct to the page containing all the warning, error and
 * info messages created during postback.
 */
public class MessageTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -6915653732681218856L;
  
  /** Logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(MessageTag.class);

  /**
   * Process the end tag for this instance.
   * 
   * @return either EVAL_PAGE or SKIP_PAGE
   */
  @SuppressWarnings("unchecked")
  public int doEndTag() throws JspException {
    List<String> errorList = (List<String>) request.getAttribute(HTMLAction.ERRORS);
    List<String> warningList = (List<String>) request.getAttribute(HTMLAction.WARNINGS);
    List<String> messageList = (List<String>) request.getAttribute(HTMLAction.INFOS);

    boolean hasErrors = errorList != null && errorList.size() > 0;
    boolean hasWarnings = warningList != null && warningList.size() > 0;
    boolean hasInfos = messageList != null && messageList.size() > 0;
    boolean hasMessage = hasErrors || hasWarnings || hasInfos;

    // Is there any message at all?
    if (hasMessage) {
      JspWriter writer = pageContext.getOut();
      StringBuffer buf = new StringBuffer();
      buf.append("<div");

      // Add tag attributes
      for (Map.Entry<String, String> attribute : getStandardAttributes().entrySet()) {
        buf.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
      }

      // Create the base id for the error, warning and info <ul> element
      String baseId = getId();
      if (baseId == null || baseId.equals(""))
        baseId = "messages";

      // Add errors
      if (hasErrors && errorList != null) {
        buf.append("<ul id=\"");
        buf.append(baseId);
        buf.append("-errors\">");
        for (String s : errorList) {
          buf.append("<li>");
          buf.append(s);
          buf.append("</li>");
        }
        buf.append("</ul>");
      }

      // Add warnings
      if (hasWarnings && warningList != null) {
        buf.append("<ul id=\"");
        buf.append(baseId);
        buf.append("-warnings\">");
        for (String s : warningList) {
          buf.append("<li>");
          buf.append(s);
          buf.append("</li>");
        }
        buf.append("</ul>");
      }

      // Add info messages
      if (hasInfos && messageList != null) {
        buf.append("<ul id=\"");
        buf.append(baseId);
        buf.append("-infos\">");
        for (String s : messageList) {
          buf.append("<li>");
          buf.append(s);
          buf.append("</li>");
        }
        buf.append("</ul>");
      }

      buf.append("</div>");

      try {
        writer.write(buf.toString());
      } catch (IOException e) {
        logger.warn("Unable to write error list to the page: " + e.getMessage());
      }
    }

    return super.doEndTag();
  }

}