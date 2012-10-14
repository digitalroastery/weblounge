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
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.page.ComposerImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

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
  private static final Logger logger = LoggerFactory.getLogger(PagePreviewTag.class);

  /** Preview stop */
  private static enum Marker {

    /** The whole page content is being shown */
    None,

    /** Only elements of the given type are being shown */
    Elements,

    /**
     * All elements between the elements specified as start and end marker are
     * shown. If either one of these markers are not present, elements will
     * start from the top of the page or range until the end of the page,
     * respectively.
     */
    Marker,

    /**
     * All elements defined as preview elements in <code>module.xml</code> are
     * shown
     */
    Pagepreview
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
  private Marker stopMarker = Marker.Pagepreview;

  /** The lead elements */
  private List<String> previewElements = null;

  /** The start marker element */
  private String startOfPreviewElement = null;

  /** The end marker element */
  private String endOfPreviewElement = null;

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
   * Creates a new page preview tag.
   */
  public PagePreviewTag() {
    previewElements = new ArrayList<String>();
  }

  /**
   * Sets the page identifier.
   * 
   * @param value
   *          the page identifier
   */
  public void setPageid(String value) {
    if (StringUtils.isBlank(value))
      return;
    pageId = value;
  }

  /**
   * Defines the lead elements. The elements need to be passed in as comma
   * separated strings, e. g.
   * 
   * <pre>
   * text/title, repository/image
   * </pre>
   * 
   * @param value
   *          the lead elements
   */
  public void setElements(String value) {
    if (StringUtils.isBlank(value))
      return;
    StringTokenizer tok = new StringTokenizer(value, ",;");
    while (tok.hasMoreTokens()) {
      String element = tok.nextToken().trim();
      String[] parts = element.split("/");
      if (parts.length != 2)
        throw new IllegalArgumentException("Preview elements '" + value + "' are malformed. Required is 'module1/pagelet1, module2/pagelet2, ...");
      previewElements.add(element);
    }
  }

  /**
   * Sets the preview type. The value needs to be one of
   * <ul>
   * <li><code>None</code> - All elements of the page's stage composer are
   * included</li>
   * <li><code>Elements</code> - Only elements of the given types are included</li>
   * <li><code>Endmarker</code> - All elements up until the appearance of the
   * stop marker are included</li>
   * <li><code>PagePreview - The page preview elements are included</code></li>
   * </ul>
   */
  public void setType(String value) {
    if (StringUtils.isNotBlank(value))
      stopMarker = Marker.valueOf(StringUtils.capitalize(value.toLowerCase()));
  }

  /**
   * Defines the element that marks the beginning of the page preview. The
   * element needs to be passed in the form
   * <code>&lt;module&gt;/&lt;pagelet&gt;</code>.
   * <p>
   * Note that this property is only relevant if the preview type is set to
   * {@link Marker#Marker}. If the start marker is not set, all elements
   * starting at the top of the page will be used.
   * 
   * <pre>
   * text / preview - start
   * </pre>
   * 
   * @param value
   *          the start element
   */
  public void setStart(String value) {
    if (StringUtils.isBlank(value))
      return;
    String[] parts = value.split("/");
    if (parts.length != 2)
      throw new IllegalArgumentException("Preview start element '" + value + "' is malformed. Expecting '<module>/<pagelet>'");
    startOfPreviewElement = value;
  }

  /**
   * Defines the element that marks the end of the page preview. The element
   * needs to be passed in the form <code>&lt;module&gt;/&lt;pagelet&gt;</code>
   * 
   * <pre>
   * text / preview - end
   * </pre>
   * 
   * <p>
   * Note that this property is only relevant if the preview type is set to
   * {@link Marker#Marker}. If the start marker is not set, all elements
   * starting at the top of the page will be used.
   * 
   * @param value
   *          the end element
   */
  public void setEnd(String value) {
    if (StringUtils.isBlank(value))
      return;
    String[] parts = value.split("/");
    if (parts.length != 2)
      throw new IllegalArgumentException("Preview end element '" + value + "' is malformed. Expecting '<module>/<pagelet>'");
    endOfPreviewElement = value;
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
    if (StringUtils.isBlank(render))
      return;
    this.render = Boolean.parseBoolean(render);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {

    // Don't do work if not needed (which is the case during precompilation)
    if (RequestUtils.isPrecompileRequest(request))
      return SKIP_BODY;

    Site site = request.getSite();

    if (pageId == null) {
      PageListTag pageListTag = (PageListTag) findAncestorWithClass(this, PageListTag.class);
      if (pageListTag != null) {
        page = pageListTag.getPage();
        pagePreview = pageListTag.getPagePreview();
        pageUrl = pageListTag.getPageUrl();
      } else if (RequestUtils.isMockRequest(request)) {
        // Skip body, if this is a mock-request
        return SKIP_BODY;
      } else {
        throw new JspException("Page preview tag needs either a page id or embedding inside a PageListTag");
      }
    } else {
      ContentRepository contentRepository = null;
      contentRepository = site.getContentRepository();
      if (contentRepository == null) {
        logger.debug("Content repository is offline");
        response.invalidate();
        return SKIP_BODY;
      }

      ResourceURI pageURI = new PageURIImpl(site, null, pageId);

      try {
        page = (Page) contentRepository.get(pageURI);
        if (page == null) {
          logger.error("No data available for page {}", pageURI);
          return EVAL_PAGE;
        }
      } catch (SecurityException e) {
        throw new JspException("Security exception while trying to load " + pageUrl, e);
      } catch (ContentRepositoryException e) {
        throw new JspException("Exception while trying to load " + pageUrl, e);
      }

      pageUrl = new WebUrlImpl(site, page.getURI().getPath());
      response.addTag(CacheTag.Url, pageUrl.getPath());
      response.addTag(CacheTag.Resource, page.getURI().getIdentifier());
    }

    PageTemplate template = site.getTemplate(page.getTemplate());
    if (template == null) {
      logger.error("No page template found for {}", page);
      return EVAL_PAGE;
    }
    template.setEnvironment(request.getEnvironment());

    String stage = template.getStage();
    if (stage == null) {
      logger.error("No stage defined for page template '{}'", template);
      return EVAL_PAGE;
    }

    // Read pagelets
    List<Pagelet> pagelets = new ArrayList<Pagelet>();
    switch (stopMarker) {
      case None:
        pagelets.addAll(Arrays.asList(page.getPagelets(stage)));
        break;
      case Elements:
        if (previewElements == null || previewElements.size() == 0)
          throw new IllegalStateException("No preview elements set");
        for (Pagelet p : page.getPagelets(stage)) {
          if (previewElements.contains(p.toString())) {
            pagelets.add(p);
          }
        }
        break;
      case Marker:
        if (StringUtils.isBlank(endOfPreviewElement))
          throw new IllegalStateException("No stop marker element set");
        for (Pagelet p : page.getPagelets(stage)) {
          if (p.toString().equals(startOfPreviewElement)) {
            pagelets.clear();
            continue;
          } else if (p.toString().equals(endOfPreviewElement)) {
            break;
          }
          pagelets.add(p);
        }
        break;
      case Pagepreview:
        pagelets.addAll(Arrays.asList(page.getPreview()));
        break;
      default:
        throw new IllegalStateException("Don't know how to handle stop marker '" + stopMarker + "'");
    }

    if (pagelets.size() == 0)
      return SKIP_BODY;

    pagePreview = new ComposerImpl("preview", pagelets);

    // Store old page, composer and pagelet for later reference
    oldPage = request.getAttribute(WebloungeRequest.PAGE);
    oldComposer = request.getAttribute(WebloungeRequest.COMPOSER);
    oldPagelet = request.getAttribute(WebloungeRequest.PAGELET);

    // Define the current values in the page context
    request.setAttribute(PagePreviewTagVariables.URL, pageUrl);
    request.setAttribute(PagePreviewTagVariables.PAGE, page);
    request.setAttribute(PagePreviewTagVariables.PREVIEW, pagePreview);

    stashAttribute(PagePreviewTagVariables.PAGELET);
    stashAndSetAttribute(PagePreviewTagVariables.URL, pageUrl);
    stashAndSetAttribute(PagePreviewTagVariables.PREVIEW, pagePreview);

    // Add included page url to tags
    response.addTag(CacheTag.Url, pageUrl.getLink());

    // Handle the first pagelet, we'll do the others
    pageletIndex = 0;
    handlePagelet(pageletIndex);

    return EVAL_BODY_INCLUDE;
  }

  /**
   * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
   */
  @Override
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
  @Override
  public int doEndTag() throws JspException {

    // If there is no page body, the doAfterBody() method will not be executed.
    // Therefore, make sure we do it here.
    if (pagePreview != null && pageletIndex < pagePreview.size()) {
      while (doAfterBody() == EVAL_BODY_AGAIN) {
        ;
      }
    }

    request.setAttribute(WebloungeRequest.PAGE, oldPage);
    request.setAttribute(WebloungeRequest.COMPOSER, oldComposer);
    request.setAttribute(WebloungeRequest.PAGELET, oldPagelet);

    removeAndUnstashAttributes();
    return super.doEndTag();
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
        request.setAttribute(WebloungeRequest.COMPOSER, pagePreview);

        pageContext.setAttribute(PagePreviewTagVariables.PAGELET, pagelet);

        if (render) {

          String moduleId = pagelet.getModule();
          String rendererId = pagelet.getIdentifier();

          // Check access rights
          // TODO: Check access
          // Permission p = SystemPermission.READ;
          // if (!pagelet.checkOne(p, user.getRoleClosure()) &&
          // !pagelet.check(p, user)) {
          // logger.debug("Skipping pagelet " + i + " in composer " + composer_
          // +
          // " due to insufficient rights");
          // continue p;
          // }

          // Check publishing dates
          // TODO: Fix this. pagelet.isPublished() currently returns false,
          // as both from and to dates are null (see PublishingCtx)
          // if (!(request.getVersion() == Resource.WORK) &&
          // !pagelet.isPublished()) {
          // logger.debug("Skipping pagelet " + index + " in composer " + stage
          // + " since it is not yet published");
          // return false;
          // }

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

        // Add cache tags
        response.addTag(CacheTag.Module, pagelet.getModule());
        response.addTag(CacheTag.Renderer, pagelet.getIdentifier());

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

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    page = null;
    pageId = null;
    pageletIndex = 0;
    pagePreview = null;
    pageUrl = null;
    previewElements.clear();
    stopMarker = Marker.None;
    render = true;
    startOfPreviewElement = null;
    endOfPreviewElement = null;
    oldPage = null;
    oldComposer = null;
    oldPagelet = null;
  }

}
