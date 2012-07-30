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

import ch.entwine.weblounge.common.content.page.HTMLHeadElement;
import ch.entwine.weblounge.common.content.page.HTMLInclude;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.page.Script;
import ch.entwine.weblounge.common.editor.EditingState;
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
import java.util.ArrayList;

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

  /** A list for link and script head elements */
  private ArrayList<HTMLHeadElement> headElements = new ArrayList<HTMLHeadElement>();

  /** A flag if an script head element need jquery */
  private boolean needsJQuery = false;

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
    Site site = request.getSite();
    Page page = (Page) request.getAttribute(WebloungeRequest.PAGE);

    if (page != null) {
      // Page template links & scripts
      PageTemplate template = site.getTemplate(page.getTemplate());
      template.setEnvironment(request.getEnvironment());
      for (HTMLHeadElement header : template.getHTMLHeaders()) {
        addHeadElement(header, site, null);
      }

      // Pagelets links & scripts
      for (Pagelet p : page.getPagelets()) {
        String moduleId = p.getModule();
        Module module = site.getModule(moduleId);
        if (module == null) {
          logger.debug("Unable to load includes for renderer '{}': module '{}' not installed", new Object[] {
              p.getIdentifier(),
              site,
              request.getRequestedUrl(),
              moduleId });
          continue;
        }
        PageletRenderer renderer = module.getRenderer(p.getIdentifier());
        if (renderer == null) {
          logger.warn("Renderer '" + p + "' not found for " + request.getUrl() + "!");
          continue;
        }

        renderer.setEnvironment(request.getEnvironment());

        for (HTMLHeadElement header : renderer.getHTMLHeaders()) {
          addHeadElement(header, site, module);
        }
      }
    }

    // Action links & scripts
    if (action != null) {
      Module module = action.getModule();
      for (HTMLHeadElement header : action.getHTMLHeaders()) {
        addHeadElement(header, site, module);
      }
    }

    // Write links & scripts to output
    try {
      pageContext.getOut().flush();

      if (request.getParameter(EditingState.WORKBENCH_PREVIEW_PARAM) != null) {
        needsJQuery = true;
      }

      // Write first jQuery script to output
      if (!RequestUtils.isEditingState(request) && needsJQuery) {
        StringBuffer linkToJQuery = new StringBuffer("/weblounge-shared/scripts/jquery/");
        linkToJQuery.append(WebloungeSharedResources.JQUERY_VERSION);
        linkToJQuery.append("/jquery.min.js");
        pageContext.getOut().print("<script src=\"" + linkToJQuery + "\" type=\"text/javascript\"></script>");

        StringBuffer linkToJQueryTools = new StringBuffer("/weblounge-shared/scripts/jquery-tools/");
        linkToJQueryTools.append(WebloungeSharedResources.JQUERY_TOOLS_VERSION);
        linkToJQueryTools.append("/jquery.tools.min.js");
        pageContext.getOut().print("<script src=\"" + linkToJQueryTools + "\" type=\"text/javascript\"></script>");
      } else if (RequestUtils.isEditingState(request)) {
        pageContext.getOut().print("<meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\" />");
      }

      for (HTMLHeadElement s : headElements) {
        pageContext.getOut().println(s.toXml());
      }

      if (request.getParameter(EditingState.WORKBENCH_PREVIEW_PARAM) != null) {
        pageContext.getOut().print("<script>$(document).ready(function() { $('form').submit(function(event) { event.preventDefault(); }); $('a').click(function(event) { event.preventDefault(); }); });</script>");
      }

      pageContext.getOut().flush();
    } catch (IOException e) {
      throw new JspException();
    }

    return super.doEndTag();
  }

  /**
   * Adds a head element to the list if it isn't only used by the editor.
   * 
   * @param headElement
   *          the head element to include
   * @param site
   *          the site
   * @param module
   *          the module
   */
  private void addHeadElement(HTMLHeadElement headElement, Site site,
      Module module) {
    if (headElement.getUse().equals(HTMLInclude.Use.Editor))
      return;
    if (headElement instanceof Script)
      if (((Script) headElement).getJQuery() != null)
        needsJQuery = true;
    if (!headElements.contains(headElement))
      headElements.add(headElement);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#release()
   */
  @Override
  public void release() {
    super.release();
    headElements = new ArrayList<HTMLHeadElement>();
    needsJQuery = false;
  }

}
