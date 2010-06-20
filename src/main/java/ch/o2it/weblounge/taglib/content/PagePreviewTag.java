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

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageTemplate;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.content.Renderer;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * <code>PagePreviewTag</code> implements the handler for <code>module</code>
 * tags embedded in a jsp file. The handler will request the specified module to
 * return some resources using the requested view.
 */
public class PagePreviewTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = 1313539327041640005L;

  /** Logging facility provided by log4j */
  private final static Logger log_ = LoggerFactory.getLogger(PagePreviewTag.class);

  /** Preview stop */
  private static enum Marker {
    None, Preview, Marker
  };

  /** The page */
  private String pageId = null;

  /** The stop marker */
  private Marker stopMarker = Marker.None;

  /**
   * Sets the page identifier.
   * 
   * @param value
   *          the page identifier
   */
  public void setPageId(String value) {
    pageId = value;
  }

  /**
   * Sets the end method.
   * 
   * @param value
   *          the stop value
   */
  public void setEnd(String value) {
    if ("lead".equalsIgnoreCase(value))
      stopMarker = Marker.Preview;
    else if ("marker".equalsIgnoreCase(value))
      stopMarker = Marker.Marker;
    else
      stopMarker = Marker.None;
  }

  /**
   * Process the end tag for this instance.
   * 
   * @return either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspException {
    JspWriter writer = pageContext.getOut();
    String stage = null;

    // Store old page, composer and pagelet for later reference
    Object oldPage = request.getAttribute(WebloungeRequest.PAGE);
    Object oldComposer = request.getAttribute(WebloungeRequest.COMPOSER);
    Object oldPagelet = request.getAttribute(WebloungeRequest.PAGELET);

    Site site = request.getSite();
    User user = request.getUser();
    ContentRepository contentRepository = null;
    
    try {

      // Flush all input that has been written to the response so far.
      pageContext.getOut().flush();

      // Load the page
      contentRepository = ContentRepositoryFactory.getRepository(site);
      PageURI pageURI = PageURIImpl.fromId(site, pageId);
      Page page = contentRepository.getPage(pageURI, user, SystemPermission.READ);
      if (page == null) {
        log_.error("No data available for page '" + pageURI + "'");
        return EVAL_PAGE;
      }

      WebUrl url = new WebUrlImpl(site, page.getURI().getPath());
      PageTemplate template = site.getTemplate(page.getTemplate());
      if (template == null) {
        log_.error("No template found for page '" + pageURI + "'");
        return EVAL_PAGE;
      }
      stage = template.getStage();

      // Add included page url to tags
      response.addTag("webl:url", url);

      // Read pagelets
      Pagelet[] pagelets = page.getPagelets(stage);
      switch (stopMarker) {
        case Preview:
          pagelets = page.getPreview();
          break;
        case None:
          pagelets = page.getPagelets(stage);
      }

      // Render the pagelets
      p: for (int i = 0; i < pagelets.length; i++) {

        Renderer renderer = null;
        Pagelet pagelet = pagelets[i];
        try {
          request.setAttribute(WebloungeRequest.PAGE, page);
          request.setAttribute(WebloungeRequest.PAGELET, pagelet);
          request.setAttribute(WebloungeRequest.COMPOSER, stage);

          String moduleId = pagelet.getModule();
          String rendererId = pagelet.getIdentifier();

          // Check access rights
          // TODO: Check access
//          Permission p = SystemPermission.READ;
//          if (!pagelet.checkOne(p, user.getRoleClosure()) && !pagelet.check(p, user)) {
//            log_.debug("Skipping pagelet " + i + " in composer " + composer_ + " due to insufficient rights");
//            continue p;
//          }

          // Check publishing dates
          if (!(request.getVersion() == Page.WORK) && !pagelet.isPublished()) {
            log_.debug("Skipping pagelet " + i + " in composer " + stage + " since it is not yet published");
            continue p;
          }

          // Select the renderer's module
          Module m = site.getModule(moduleId);
          if (m == null) {
            log_.warn("Unable to load renderer '" + rendererId + "' for " + url + ": module '" + moduleId + "' not found!");
            continue p;
          }

          // Load renderer
          renderer = m.getRenderer(rendererId);
          if (renderer == null) {
            log_.warn("No suitable renderer '" + moduleId + "/" + rendererId + "' found to render on " + url);
            continue p;
          }

          // Render pagelet
          try {
            renderer.render(request, response);
            writer.flush();
          } catch (Throwable e) {
//            String params = RequestSupport.getParameters(request);
            String msg = "Error rendering " + renderer + " on " + url + "'";
            String reason = "";
            Throwable o = e.getCause();
            if (o != null) {
              reason = o.getMessage();
              msg += ": " + reason;
              log_.error(msg, o);
            } else {
              log_.error(msg, e);
            }
          }
        } catch (Throwable t) {
          String msg = "Exception when processing pagelet '" + pagelets[i].getURI() + "'";
          log_.error(msg + ":" + t.getMessage());
          log_.warn(msg, t);
        }
      }
    } catch (IOException e) {
      log_.error("Unable to print to out", e);
      return EVAL_PAGE;
    } catch (Throwable t) {
      String msg = "Exception when processing composer '" + stage + "'";
      log_.error(msg + ":" + t.getMessage());
      log_.warn(msg, t);
      return EVAL_PAGE;
    } finally {
      request.setAttribute(WebloungeRequest.PAGE, oldPage);
      request.setAttribute(WebloungeRequest.COMPOSER, oldComposer);
      request.setAttribute(WebloungeRequest.PAGELET, oldPagelet);
    }
    return EVAL_PAGE;
  }

  /**
   * This method is called when this tag instance is released and put back into
   * the pool.
   * 
   * @see javax.servlet.jsp.tagext.Tag#release()
   */
  public void release() {
    super.release();
    pageId = null;
    stopMarker = Marker.None;
  }

}
