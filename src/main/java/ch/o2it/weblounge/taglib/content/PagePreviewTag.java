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

import ch.o2it.weblounge.common.content.Renderer;
import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.impl.content.page.ComposerImpl;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * <code>PagePreviewTag</code> renders parts or all of the contents of the stage
 * composer of a page.
 * <p>
 * It can be used in either of two scenarios: Standalone, simply given the
 * <code>pageURI</code> or embedded in a <code>PageListTag</code> where it will
 * render the contents of the preview composer as set by the enclosing tag.
 */
public class PagePreviewTag extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = 1313539327041640005L;

  /** Logging facility provided by log4j */
  private final static Logger logger = LoggerFactory.getLogger(PagePreviewTag.class);

  /** Preview stop */
  private static enum Marker {
    None, Lead, Marker
  };

  /** The page */
  private String pageId = null;

  /** The page (used when embedded inside PageListTag) */
  private Page page = null;

  /** The stage composer (used when embedded inside PageListTag) */
  private Composer pagePreview = null;

  /** The page url (used when embedded inside PageListTag) */
  private WebUrl pageUrl = null;

  /** The stop marker */
  private Marker stopMarker = Marker.None;

  /** The lead element */
  private String leadElement = "text/title";

  /** True to have the tag render the contents */
  private boolean render = true;

  /** The current pagelet index */
  private int pageletIndex = 0;

  /** The original page found in the request */
  private Object oldPage = null;

  /** The original composer found in the request */
  private Object oldComposer = null;

  /** The original pagelet found in the request */
  private Object oldPagelet = null;

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
   * Defines the lead element. The input needs to be formatted as
   * <code>&lt;module&gt;/&lt;pagelet&gt;</code> and defaults to
   * <code>text/title</code>.
   * 
   * @param element
   *          the lead element
   */
  public void setLead(String element) {
    if (element.split("/").length != 2)
      throw new IllegalArgumentException("The lead element definition must be formatted as '<module>/<pagelet>'");
    leadElement = element;
  }

  /**
   * Sets the end method.
   * 
   * @param value
   *          the stop value
   */
  public void setEnd(String value) {
    if ("lead".equalsIgnoreCase(value))
      stopMarker = Marker.Lead;
    else if ("marker".equalsIgnoreCase(value))
      stopMarker = Marker.Marker;
    else
      stopMarker = Marker.None;
  }

  /**
   * Specifies whether the tag should render the contents rather than just
   * setting page, composer and pagelet in the page context. The default is
   * <code>true</code>.
   * 
   * @param render
   *          <code>true</code> to have the tag render the content
   */
  public void setRender(String render) {
    this.render = Boolean.parseBoolean(render);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {
    Site site = request.getSite();
    User user = request.getUser();

    if (pageId == null) {
      PageListTag pageListTag = (PageListTag) findAncestorWithClass(this, PageListTag.class);
      if (pageListTag != null) {
        page = pageListTag.getPage();
        pagePreview = pageListTag.getPagePreview();
        pageUrl = pageListTag.getPageUrl();
      } else {
        throw new IllegalStateException("Page preview tag needs either a page id or embedding inside a PageListTag");
      }
    } else {
      ContentRepository contentRepository = null;
      contentRepository = ContentRepositoryFactory.getRepository(site);
      ResourceURI pageURI = new PageURIImpl(site, null, pageId);

      try {
        page = (Page)contentRepository.get(pageURI, user, SystemPermission.READ);
        if (page == null) {
          logger.error("No data available for page '" + pageURI + "'");
          return EVAL_PAGE;
        }
      } catch (SecurityException e) {
        throw new JspException("Security exception while trying to load " + pageUrl, e);
      } catch (ContentRepositoryException e) {
        throw new JspException("Exception while trying to load " + pageUrl, e);
      }

      pageUrl = new WebUrlImpl(site, page.getURI().getPath());
      PageTemplate template = site.getTemplate(page.getTemplate());
      if (template == null) {
        logger.error("No template found for page '" + pageURI + "'");
        return EVAL_PAGE;
      }

      String stage = template.getStage();
      if (stage == null) {
        logger.error("No stage defined for template '" + template + "'");
        return EVAL_PAGE;
      }

      // Read pagelets
      Pagelet[] pagelets = null;
      switch (stopMarker) {
        case Lead:
          for (Pagelet p : page.getPreview()) {
            String[] s = leadElement.split("/");
            if (p.getModule().equals(s[0]) && p.getIdentifier().equals(s[1])) {
              pagelets = new Pagelet[] { p };
              break;
            }
          }
          if (pagelets == null)
            pagelets = new Pagelet[] {};
          break;
        case Marker:
          pagelets = page.getPreview();
          break;
        default:
          pagelets = page.getPagelets(stage);
      }

      pagePreview = new ComposerImpl("stage", pagelets);
    }

    // Store old page, composer and pagelet for later reference
    oldPage = request.getAttribute(WebloungeRequest.PAGE);
    oldComposer = request.getAttribute(WebloungeRequest.COMPOSER);
    oldPagelet = request.getAttribute(WebloungeRequest.PAGELET);

    // Define the current values in the page context
    request.setAttribute(PagePreviewTagVariables.URL, pageUrl);
    request.setAttribute(PagePreviewTagVariables.PAGE, page);
    request.setAttribute(PagePreviewTagVariables.PREVIEW, pagePreview);

    pageContext.setAttribute(PagePreviewTagVariables.URL, pageUrl);
    pageContext.setAttribute(PagePreviewTagVariables.PREVIEW, pagePreview);

    // Add included page url to tags
    response.addTag("webl:url", pageUrl);

    pageletIndex = 0;
    handlePagelet(pageletIndex);

    return EVAL_BODY_INCLUDE;
  }

  /**
   * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
   */
  public int doAfterBody() throws JspException {
    pageletIndex++;
    
    // Look for the next pagelet (some might be skipped)
    while (pageletIndex < pagePreview.getPagelets().length && !handlePagelet(pageletIndex)) {
      pageletIndex++;
    }
    
    // If we found one, let's execute the body. Otherwise, we skip
    if (pageletIndex <= pagePreview.getPagelets().length) {
      return EVAL_BODY_AGAIN;
    } else {
      return SKIP_BODY;
    }
  }

  /**
   * Process the end tag for this instance.
   * 
   * @return either EVAL_PAGE or SKIP_PAGE
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(PagePreviewTagVariables.URL);
    pageContext.removeAttribute(PagePreviewTagVariables.PREVIEW);
    pageContext.removeAttribute(PagePreviewTagVariables.PAGELET);

    request.setAttribute(WebloungeRequest.PAGE, oldPage);
    request.setAttribute(WebloungeRequest.COMPOSER, oldComposer);
    request.setAttribute(WebloungeRequest.PAGELET, oldPagelet);
    
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
    pageletIndex = 0;
    leadElement = "text/title";
    render = true;
  }

  /**
   * Loads and optionally renders the current pagelet.
   * 
   * @param index
   *          the pagelet's index
   * @return <code>true</code> if the pagelet could be handled
   */
  public boolean handlePagelet(int index) {
    JspWriter writer = pageContext.getOut();

    Site site = request.getSite();
    String stage = pagePreview.getIdentifier();

    try {

      // Flush all input that has been written to the response so far.
      pageContext.getOut().flush();

      // Render the pagelet
      Renderer renderer = null;
      Pagelet pagelet = pagePreview.getPagelets()[index];
      try {
        request.setAttribute(WebloungeRequest.PAGE, page);
        request.setAttribute(WebloungeRequest.PAGELET, pagelet);
        request.setAttribute(WebloungeRequest.COMPOSER, pagePreview.getIdentifier());

        pageContext.setAttribute(PagePreviewTagVariables.PAGELET, pagelet);

        if (render) {

          String moduleId = pagelet.getModule();
          String rendererId = pagelet.getIdentifier();

          // Check access rights
          // TODO: Check access
          // Permission p = SystemPermission.READ;
          // if (!pagelet.checkOne(p, user.getRoleClosure()) &&
          // !pagelet.check(p, user)) {
          // logger.debug("Skipping pagelet " + i + " in composer " + composer_ +
          // " due to insufficient rights");
          // continue p;
          // }

          // Check publishing dates
          if (!(request.getVersion() == Resource.WORK) && !pagelet.isPublished()) {
            logger.debug("Skipping pagelet " + index + " in composer " + stage + " since it is not yet published");
            return false;
          }

          // Select the renderer's module
          Module m = site.getModule(moduleId);
          if (m == null) {
            logger.warn("Unable to load renderer '" + rendererId + "' for " + pageUrl + ": module '" + moduleId + "' not found!");
            return false;
          }

          // Load renderer
          renderer = m.getRenderer(rendererId);
          if (renderer == null) {
            logger.warn("No suitable renderer '" + moduleId + "/" + rendererId + "' found to render on " + pageUrl);
            return false;
          }

          // Render pagelet
          try {
            renderer.render(request, response);
            writer.flush();
          } catch (Throwable e) {
            // String params = RequestSupport.getParameters(request);
            String msg = "Error rendering " + renderer + " on " + pageUrl + "'";
            String reason = "";
            Throwable o = e.getCause();
            if (o != null) {
              reason = o.getMessage();
              msg += ": " + reason;
              logger.error(msg, o);
            } else {
              logger.error(msg, e);
            }
          }

        } // render?

      } catch (Throwable t) {
        String msg = "Exception when processing pagelet '" + pagelet.getURI() + "'";
        logger.error(msg + ":" + t.getMessage());
        logger.warn(msg, t);
      }
    } catch (IOException e) {
      logger.error("Unable to print to out", e);
      return false;
    } catch (Throwable t) {
      String msg = "Exception when processing composer '" + stage + "'";
      logger.error(msg + ":" + t.getMessage());
      logger.warn(msg, t);
      return false;
    } finally {
      request.setAttribute(WebloungeRequest.PAGE, oldPage);
      request.setAttribute(WebloungeRequest.COMPOSER, oldComposer);
      request.setAttribute(WebloungeRequest.PAGELET, oldPagelet);
    }

    return true;
  }

}
