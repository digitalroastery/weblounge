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

import ch.o2it.weblounge.common.content.DeclarativeHTMLHeadElement;
import ch.o2it.weblounge.common.content.HTMLHeadElement;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.content.Renderer;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.HTMLAction;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.jsp.JspException;

/**
 * This tag prints out various headers for the &lt;head&gt; section of an html
 * page.
 */
public class HTMLHeaderTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = -1813975272420106327L;

  /** Logging facility provided by log4j */
  private final static Logger log_ = LoggerFactory.getLogger(HTMLHeaderTag.class);

  /**
   * Does the tag processing.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    HTMLAction action = (HTMLAction) getRequest().getAttribute(WebloungeRequest.ACTION);
    
    // See what the action has to contribute
    try {
      if (action != null && action.startHeader(request, response) == HTMLAction.SKIP_HEADER) {
        return EVAL_BODY_INCLUDE;
      }
    } catch (Exception e) {
      log_.error("Error asking action '" + action + "' for headers", e);
    }

    // Start with the default processing
    Set<HTMLHeadElement> headElements = new HashSet<HTMLHeadElement>();

    // Pagelets links & scripts
    Page page = (Page) request.getAttribute(WebloungeRequest.PAGE);
    if (page != null) {
      Pagelet[] pagelets = page.getPagelets();
      Site site = request.getSite();
      for (Pagelet p : pagelets) {
        String moduleId = p.getModule();
        Module module = site.getModule(moduleId);
        if (module == null) {
          log_.warn("Unable to get renderer '" + p + "' for " + request.getUrl() + " since module '" + moduleId + "' is not installed");
          continue;
        }
        Renderer renderer = module.getRenderer(p.getIdentifier());
        if (renderer != null) {
          for (HTMLHeadElement header : renderer.getHTMLHeaders()) {
            if (header instanceof DeclarativeHTMLHeadElement)
              ((DeclarativeHTMLHeadElement)header).configure(request, site, module);
            headElements.add(header);
          }
        } else {
          log_.warn("Renderer '" + p + "' not found for " + request.getUrl() + "!");
          continue;
        }
      }
    }

    // Action links & scripts
    if (action != null) {
      for (HTMLHeadElement l : action.getHTMLHeaders())
        headElements.add(l);
    }

    // Write links & scripts to output
    try {
      for (HTMLHeadElement s : headElements) {
        pageContext.getOut().print(s.toXml());
      }
      pageContext.getOut().flush();
    } catch (IOException e) {
      throw new JspException();
    }

    return EVAL_BODY_INCLUDE;
  }

}
