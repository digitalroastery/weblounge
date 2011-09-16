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

import ch.entwine.weblounge.common.content.Renderer;
import ch.entwine.weblounge.common.content.page.DeclarativeHTMLHeadElement;
import ch.entwine.weblounge.common.content.page.HTMLHeadElement;
import ch.entwine.weblounge.common.content.page.HTMLInclude;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.Script;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.kernel.shared.WebloungeSharedResources;
import ch.entwine.weblounge.taglib.WebloungeTag;

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
  private static final Logger logger = LoggerFactory.getLogger(HTMLHeaderTag.class);

  /**
   * Does the tag processing.
   * 
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    HTMLAction action = (HTMLAction) getRequest().getAttribute(WebloungeRequest.ACTION);
    
    // See what the action has to contribute
    try {
      pageContext.getOut().flush();
      if (action != null && action.startHeader(request, response) == HTMLAction.SKIP_HEADER) {
        pageContext.getOut().flush();
        return EVAL_PAGE;
      }
    } catch (Exception e) {
      logger.error("Error asking action '" + action + "' for headers", e);
    }

    // Start with the default processing
    Set<HTMLHeadElement> headElements = new HashSet<HTMLHeadElement>();
    Site site = request.getSite();

    boolean needsJQuery = false;
    // Pagelets links & scripts
    Page page = (Page) request.getAttribute(WebloungeRequest.PAGE);
    if (page != null) {
      Pagelet[] pagelets = page.getPagelets();
      for (Pagelet p : pagelets) {
        String moduleId = p.getModule();
        Module module = site.getModule(moduleId);
        if (module == null) {
          logger.debug("Unable to load includes for renderer '{}': module '{}' not installed", new Object[] { p.getIdentifier(), site, request.getRequestedUrl(), moduleId });
          continue;
        }
        Renderer renderer = module.getRenderer(p.getIdentifier());
        if (renderer != null) {
          // TODO if use == renderer include to html as script
          for (HTMLHeadElement header : renderer.getHTMLHeaders()) {
            if (header instanceof DeclarativeHTMLHeadElement)
              ((DeclarativeHTMLHeadElement)header).configure(request, site, module);
            if(header.getUse().equals(HTMLInclude.Use.Editor)) continue;
            if (header instanceof Script)
              if(((Script)header).getJQuery() != null) needsJQuery = true;
            headElements.add(header);
          }
        } else {
          logger.warn("Renderer '" + p + "' not found for " + request.getUrl() + "!");
          continue;
        }
      }
    }
    
    // Action links & scripts
    if (action != null) {
      Module module = action.getModule();
      for (HTMLHeadElement l : action.getHTMLHeaders()) {
        if (l instanceof DeclarativeHTMLHeadElement)
          ((DeclarativeHTMLHeadElement)l).configure(request, site, module);
        headElements.add(l);
      }
    }
    
    // Write links & scripts to output
    try {
      pageContext.getOut().flush();
      // Write first jQuery script to output
      if(!RequestUtils.isEditingState(request) && needsJQuery) {
        StringBuffer linkToJQuery = new StringBuffer("/weblounge-shared/scripts/jquery/");
        linkToJQuery.append(WebloungeSharedResources.JQUERY_VERSION);
        linkToJQuery.append("/jquery.min.js");
        pageContext.getOut().print("<script src=\"" + linkToJQuery + "\" type=\"text/javascript\"></script>");
      }
      for (HTMLHeadElement s : headElements) {
        pageContext.getOut().println(s.toXml());
      }
      pageContext.getOut().flush();
    } catch (IOException e) {
      throw new JspException();
    }

    return super.doEndTag();
  }
  
}
