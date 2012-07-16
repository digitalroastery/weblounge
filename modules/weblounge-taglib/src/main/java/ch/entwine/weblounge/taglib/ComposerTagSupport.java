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

package ch.entwine.weblounge.taglib;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryUnavailableException;
import ch.entwine.weblounge.common.impl.content.page.ComposerImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.request.CacheTagImpl;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.taglib.content.ComposerTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * Utility class to support tags that implement content areas.
 */
public class ComposerTagSupport extends WebloungeTag {

  /** Serial version uid */
  private static final long serialVersionUID = 3879878738066602501L;

  /** Logging facility provided by log4j */
  private static final Logger logger = LoggerFactory.getLogger(ComposerTag.class);

  /** The possible states that this tag can be in while rendering the composer */
  public enum RenderingState {
    Outside, InsideComposer, InsidePagelet
  };

  /** CSS class name for composer */
  public static final String CLASS_COMPOSER = "composer";

  /** Css class name for a locked composer */
  public static final String CLASS_LOCKED = "locked";

  /** Css class name for a composer with inheritance enabled */
  public static final String CLASS_INHERIT_CONTENT = "inherit";

  /** Css class name for a locked composer */
  public static final String CLASS_GHOST_CONTENT = "ghost";

  /** Css class name for an empty composer */
  public static final String CLASS_EMPTY = "empty";

  /** Return code indicating that the pagelet should be evaluated */
  public static final int EVAL_PAGELET = 0;

  /** Return code indicating that the pagelet should be skipped */
  public static final int SKIP_PAGELET = 1;

  /** True to enable content inheritance */
  protected boolean contentInheritanceEnabled = false;

  /** The underlying page */
  protected Page targetPage = null;

  /** The content providing page */
  protected Page contentProvider = null;

  /** The ghost content providing page */
  protected Page ghostContentProvider = null;

  /** True if the content is not coming from the target page directly */
  protected boolean contentIsInherited = false;

  /** The pagelets within this composer */
  protected Pagelet[] pagelets = null;

  /** The ghost pagelets within this composer */
  protected Pagelet[] ghostPaglets;

  /** The current rendering state */
  protected RenderingState renderingState = RenderingState.Outside;

  /** True if the tag tried to load the composer data */
  private boolean initialized = false;

  /** True to turn on debug comments to indicate start and end of elements */
  protected boolean debug = false;

  /* Request attributes */
  protected final Map<String, Object> attributes = new HashMap<String, Object>();

  @Override
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Switches debug output to indicate the beginning and the end of composers
   * and pagelets on and off.
   * 
   * @param debug
   *          <code>true</code> to turn debug output on
   */
  public void setDebug(String debug) {
    this.debug = ConfigurationUtils.isTrue(debug);
  }

  /**
   * Enables content inheritance for this composer.
   * 
   * @param value
   *          <code>true</code>, <code>yes</code> or <code>on</code> will enable
   *          the feature
   */
  public void setInherit(String value) {
    setInherit(ConfigurationUtils.isTrue(value));
  }

  /**
   * Enables content inheritance for this composer.
   * 
   * @param inherit
   *          <code>true</code> to enable content inheritance
   */
  public void setInherit(boolean inherit) {
    contentInheritanceEnabled = inherit;
  }

  /**
   * Callback that is executed before the composer is being filled with content.
   * <p>
   * This default implementation will open a <code>&lt;div&gt;</code> containing
   * all the attributes returned by {@link #getStandardAttributes()} and {@link
   * getComposerAttributes()}.
   * 
   * @param writer
   *          the jsp output writer
   * @throws IOException
   *           if writing to the output fails
   * @throws ContentRepositoryException
   *           if reading content from the repository fails
   * @throws ContentRepositoryUnavailableException
   *           if the content repository is offline
   * @see #afterComposer(JspWriter)
   */
  protected void beforeComposer(JspWriter writer) throws IOException,
  ContentRepositoryException, ContentRepositoryUnavailableException {
    StringBuffer buf = new StringBuffer("<div ");
    addCssClass(CLASS_COMPOSER);
    if (request.getVersion() == Resource.WORK && getTargetPage().isLocked()) {
      addCssClass(CLASS_LOCKED);
    }

    // Add tag attributes
    for (Map.Entry<String, String> attribute : getStandardAttributes().entrySet()) {
      buf.append(" ").append(attribute.getKey()).append("=\"").append(attribute.getValue()).append("\"");
    }

    buf.append(">");
    writer.println(buf.toString());
  }

  /**
   * Callback that is executed after the composer has been filled with content.
   * <p>
   * This default implementation will close the <code>&lt;div&gt;</code> that
   * was opened during the call to {@link #beforeComposer(JspWriter)}.
   * 
   * @param writer
   *          the jsp output writer
   * @param composer
   *          the composer
   * @throws IOException
   *           if writing to the output fails
   * @throws ContentRepositoryException
   *           if reading content from the repository fails
   * @throws ContentRepositoryUnavailableException
   *           if the content repository is offline
   * @see #beforeComposer(JspWriter)
   */
  protected void afterComposer(JspWriter writer, Composer composer)
      throws IOException, ContentRepositoryException,
      ContentRepositoryUnavailableException {
    writer.println("</div>");

    if (ghostPaglets.length > 0 && RequestUtils.isEditingState(request)) {
      writer.print("<div id=\"" + id + "-ghost\">");

      // Render the ghost pagelets
      for (int i = 0; i < ghostPaglets.length; i++) {

        Pagelet pagelet = ghostPaglets[i];

        // Add pagelet and composer to the request
        request.setAttribute(WebloungeRequest.COMPOSER, composer);
        request.setAttribute(WebloungeRequest.PAGELET, pagelet);

        doPagelet(pagelet, i, writer, true);
      }

      writer.println("</div>");
    }

  }

  /**
   * Returns the attributes that need to be added to the composer. This default
   * implementation will return the list of default attributes as defined by the
   * superclass.
   * 
   * @return the attributes that should be added to the composer
   */
  protected Map<String, String> getComposerAttributes() {
    return getStandardAttributes();
  }

  /**
   * Returns the css class that should be added to the composer in case where
   * there is no content in the composer.
   * <p>
   * This default implementation will return <code>empty</code>.
   * 
   * @return the css class for empty composers
   */
  protected String getEmptyComposerClass() {
    return CLASS_EMPTY;
  }

  /**
   * Callback that is executed before the composer is being filled with a piece
   * of content (pagelet).
   * 
   * @param pagelet
   *          the current pagelet
   * @param position
   *          the pagelet's position inside the composer
   * @param writer
   *          the writer
   * @param isGhostContent
   *          true if ghost content
   * 
   * @return <code>{@link #EVAL_PAGELET}</code> or
   *         <code>{@link #SKIP_PAGELET}</code>
   * @throws IOException
   *           if writing to the composer fails
   * @throws ContentRepositoryException
   *           if reading content from the repository fails
   * @throws ContentRepositoryUnavailableException
   *           if the content repository is offline
   */
  protected int beforePagelet(Pagelet pagelet, int position, JspWriter writer,
      boolean isGhostContent) throws IOException, ContentRepositoryException,
      ContentRepositoryUnavailableException {
    return EVAL_PAGELET;
  }

  /**
   * Callback that is executed before the composer ist being filled with
   * content.
   * 
   * @param pagelet
   *          the current pagelet
   * @param position
   *          the pagelet's position inside the composer
   * @param writer
   *          the writer
   * @throws IOException
   *           if writing to the composer fails
   * @throws ContentRepositoryException
   *           if reading content from the repository fails
   * @throws ContentRepositoryUnavailableException
   *           if the content repository is offline
   */
  protected void afterPagelet(Pagelet pagelet, int position, JspWriter writer)
      throws IOException, ContentRepositoryException,
      ContentRepositoryUnavailableException {
  }

  /**
   * Checks if the current composer contains pagelets. If not, content is loaded
   * and returned from a parent page.
   * 
   * @return the page that provided the content
   * @throws SecurityException
   *           if access to the content is denied
   * @throws ContentRepositoryException
   *           if reading content from the repository fails
   * @throws ContentRepositoryUnavailableException
   *           if the content repository is offline
   */
  private void loadContent(boolean inheritFromParent) throws SecurityException,
  ContentRepositoryException, ContentRepositoryUnavailableException {

    try {
      WebUrl url = getRequest().getUrl();
      Site site = request.getSite();
      ContentRepository contentRepository = site.getContentRepository();
      if (contentRepository == null) {
        logger.debug("Content repository unavailable for site '{}'", site.getIdentifier());
        throw new ContentRepositoryUnavailableException("Repository is offline");
      }

      targetPage = (Page) getRequest().getAttribute(WebloungeRequest.PAGE);

      // If no page was specified, take homepage instead.
      if (targetPage == null) {
        ResourceURI homeURI = new PageURIImpl(site, "/");
        try {
          targetPage = (Page) contentRepository.get(homeURI);
          if (targetPage == null) {
            logger.warn("No page was found while processing composer on " + url);
            return;
          }
        } catch (SecurityException e) {
          logger.warn("Composer '" + id + "' was unable to choose homepage as fallback: " + e.getMessage());
          return;
        } catch (ContentRepositoryException e) {
          logger.warn("Composer '" + id + "' was unable to choose homepage as fallback: " + e.getMessage());
          return;
        }
      }

      if (contentProvider == null)
        contentProvider = targetPage;

      Pagelet[] content = contentProvider.getPagelets(id);
      Pagelet[] ghostContent = new Pagelet[0];

      // If composer is empty and ghost content is enabled, go up the page
      // hierarchy and try to find content for this composer
      Page contentPage = contentProvider;
      if (inheritFromParent) {
        String pageUrl = contentPage.getURI().getPath();
        while (ghostContent.length == 0 && pageUrl.length() > 1) {
          if (pageUrl.endsWith("/") && !"/".equals(pageUrl))
            pageUrl = pageUrl.substring(0, pageUrl.length() - 1);
          int urlSeparator = pageUrl.lastIndexOf("/");
          if (urlSeparator < 0) {
            contentPage = null;
            break;
          } else {
            pageUrl = pageUrl.substring(0, urlSeparator);
            if ("".equals(pageUrl))
              pageUrl = "/";
            ResourceURI pageURI = new PageURIImpl(site, pageUrl);
            try {
              contentPage = (Page) contentRepository.get(pageURI);
            } catch (SecurityException e) {
              logger.debug("Prevented loading of protected content from inherited page {} for composer {}", pageURI, id);
            }

            // Did we find anything? If not, keep looking...
            if (contentPage == null) {
              logger.debug("Ancestor page {} could not be loaded", pageUrl);
              continue;
            }

            // If potential ghost content is available, keep it
            if (!contentPage.equals(targetPage)) {
              ghostContentProvider = contentPage;
              ghostContent = contentPage.getPagelets(id);
            }

          }
        }
      }

      // Add the ghost content provider to the set of cache tags
      if (ghostContentProvider != null) {
        response.addTag(new CacheTagImpl(CacheTag.Resource, ghostContentProvider.getURI().getIdentifier()));
      }

      // If pagelets have been found, set them in the composer
      ghostPaglets = ghostContent;
      pagelets = content;
      boolean isEditing = RequestUtils.isEditingState(request);

      // Mark empty composers while editing the current page
      if (isEditing && content.length == 0) {
        addCssClass(getEmptyComposerClass());
      }

      // Keep a record
    } finally {
      initialized = true;
    }
  }

  /**
   * Returns the page that is used to render the content.
   * <p>
   * If content inheritance is switched off, this will also be the page that
   * provides the content. Otherwise, the content may as well come from a
   * different page, as returned by {@link #getContentProvider()}.
   * 
   * @return the target page
   * @throws ContentRepositoryUnavailableException
   *           if the content repository is offline
   * @throws ContentRepositoryException
   *           if reading from the content repository fails
   */
  protected Page getTargetPage() throws ContentRepositoryUnavailableException,
  ContentRepositoryException {
    if (!initialized) {
      loadContent(contentInheritanceEnabled);
    }
    return targetPage;
  }

  /**
   * Returns the page that actually delivers the content for this composer.
   * 
   * @return the content delivering page
   */
  protected Page getContentProvider() {
    if (!initialized) {
      try {
        loadContent(contentInheritanceEnabled);
      } catch (Exception e) {
        logger.warn("Unable to load composer content: {}", e.getMessage());
      }
    }
    return contentProvider;
  }

  /**
   * Returns the page that actually delivers the ghost content for this
   * composer.
   * 
   * @return the ghost content delivering page
   */
  protected Page getGhostContentProvider() {
    if (!initialized) {
      try {
        loadContent(contentInheritanceEnabled);
      } catch (Exception e) {
        logger.warn("Unable to load composer ghost content: {}", e.getMessage());
      }
    }
    return ghostContentProvider;
  }

  /**
   * Returns the composer's pagelets. Note that the pagelets are only available
   * on or after the first call to {@link #beforeComposer(JspWriter)}.
   * 
   * @return the pagelets
   * @throws ContentRepositoryException
   *           if loading the content fails
   * @throws ContentRepositoryUnavailableException
   *           if the content repository is offline
   * @throws SecurityException
   *           if accessing the content is forbidden
   */
  protected Pagelet[] getContent() throws SecurityException,
  ContentRepositoryException, ContentRepositoryUnavailableException {
    if (contentProvider == null)
      loadContent(contentInheritanceEnabled);
    if (pagelets == null)
      pagelets = new Pagelet[] {};
    return pagelets;
  }

  /**
   * Returns the composer's ghost pagelets. Note that the pagelets are only
   * available on or after the first call to {@link #beforeComposer(JspWriter)}.
   * 
   * @return the pagelets
   * @throws ContentRepositoryException
   *           if loading the content fails
   * @throws ContentRepositoryUnavailableException
   *           if the content repository is offline
   * @throws SecurityException
   *           if accessing the content is forbidden
   */
  protected Pagelet[] getGhostContent() throws SecurityException,
  ContentRepositoryException, ContentRepositoryUnavailableException {
    if (ghostContentProvider == null)
      loadContent(contentInheritanceEnabled);
    if (ghostContentProvider == null)
      ghostPaglets = new Pagelet[] {};
    return ghostPaglets;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {
    Enumeration<?> names = request.getAttributeNames();
    while (names.hasMoreElements()) {
      String key = (String) names.nextElement();
      attributes.put(key, request.getAttribute(key));
    }

    // Initiate loading the page content
    try {
      loadContent(contentInheritanceEnabled);
    } catch (ContentRepositoryUnavailableException e) {
      logger.warn("Content repository '{}' unavailable while processing jsp", request.getSite().getIdentifier());
    } catch (ContentRepositoryException e) {
      logger.warn("Error accessing content repository '{}': {}", request.getSite().getIdentifier(), e.getMessage());
    }

    return EVAL_BODY_INCLUDE;
  }

  /**
   * Process the end tag for this instance.
   * 
   * @return either EVAL_PAGE or SKIP_PAGE
   */
  @Override
  public int doEndTag() throws JspException {
    logger.debug("Rendering composer " + id);

    Site site = request.getSite();
    ComposerImpl composer = new ComposerImpl(id);
    JspWriter writer = pageContext.getOut();

    Action action = (Action) request.getAttribute(WebloungeRequest.ACTION);

    try {

      // Flush all input that has been written to the response so far.
      writer.flush();
      try {

        // Add tags for this composer
        response.addTag(CacheTag.Composer, id);

        // Flush all output so far
        writer.flush();

        // Start the composer
        beforeComposer(writer);
        renderingState = RenderingState.InsideComposer;
        writer.flush();

        // Load the pagelets
        composer.setPagelets(getContent());

        // Set composer
        request.setAttribute(WebloungeRequest.COMPOSER, composer);

        // Check for action handler
        if (action != null && action instanceof HTMLAction) {
          HTMLAction htmlAction = (HTMLAction) action;
          logger.debug("Action handler found");

          if (targetPage != null) {
            String templateId = targetPage.getTemplate();
            PageTemplate template = site.getTemplate(templateId);
            if (template != null && id.equalsIgnoreCase(template.getStage())) {
              template.setEnvironment(request.getEnvironment());
              if (htmlAction.startStage(request, response, composer) == HTMLAction.SKIP_COMPOSER) {
                return EVAL_PAGE;
              }
            }
          }
          if (htmlAction.startComposer(request, response, composer) == HTMLAction.SKIP_COMPOSER) {
            return EVAL_PAGE;
          }
        }

        // Add cache tag for content provider (in case of inheritance)
        if (contentProvider != null && !contentProvider.equals(targetPage)) {
          response.addTag(CacheTag.Url, contentProvider.getURI().getPath());
        }

        // Render the pagelets
        for (int i = 0; i < pagelets.length; i++) {
          Pagelet pagelet = pagelets[i];

          // Add pagelet and composer to the request
          request.setAttribute(WebloungeRequest.PAGELET, pagelet);
          request.setAttribute(WebloungeRequest.COMPOSER, composer);

          doPagelet(pagelet, i, writer, false);
        }

        // If just ghost pagelets render them
        if (pagelets.length == 0 && !RequestUtils.isEditingState(request)) {
          // Render the ghost pagelets
          for (int i = 0; i < ghostPaglets.length; i++) {
            Pagelet pagelet = ghostPaglets[i];

            // Add pagelet and composer to the request
            request.setAttribute(WebloungeRequest.PAGELET, pagelet);
            request.setAttribute(WebloungeRequest.COMPOSER, composer);

            doPagelet(pagelet, i, writer, false);
          }
        }

      } finally {

        // Syntactically close the composer
        if (renderingState.equals(RenderingState.InsideComposer)) {
          afterComposer(writer, composer);
          renderingState = RenderingState.Outside;
        }

        // Cleanup request
        request.removeAttribute(WebloungeRequest.PAGELET);
        request.removeAttribute(WebloungeRequest.COMPOSER);

        writer.flush();
      }

    } catch (IOException e) {
      response.invalidate();
      logger.error("Unable to print to out", e);
      return EVAL_PAGE;
    } catch (ContentRepositoryUnavailableException e) {
      response.invalidate();
      return EVAL_PAGE;
    } catch (Throwable t) {
      response.invalidate();
      String msg = "Exception when processing composer '" + id + "' on " + getRequest().getRequestedUrl();
      if (action != null)
        msg += " for action '" + action + "'";
      logger.error(msg, t);
      return EVAL_PAGE;
    }

    return super.doEndTag();
  }

  /**
   * Writes the pagelet to the jsp page.
   * 
   * @param pagelet
   *          the pagelet to write
   * @param position
   *          the pagelet's position inside the composer
   * @param writer
   *          the jsp writer
   * @throws IOException
   *           if writing to the jsp fails
   */
  protected void doPagelet(Pagelet pagelet, int position, JspWriter writer,
      boolean isGhostContent) throws IOException {

    Site site = request.getSite();
    WebUrl url = request.getUrl();
    long version = request.getVersion();

    Action action = (Action) request.getAttribute(WebloungeRequest.ACTION);

    PageletRenderer renderer = null;

    try {

      String moduleId = pagelet.getModule();
      String rendererId = pagelet.getIdentifier();

      // Check access rights
      // Permission p = SystemPermission.READ;
      // if (!pagelet.checkOne(p, user.getRoleClosure()) &&
      // !pagelet.check(p, user)) {
      // logger.debug("Skipping pagelet " + i + " in composer " + composer
      // + " due to insufficient rights");
      // continue p;
      // }

      // Check publishing dates
      // TODO: Fix this. pagelet.isPublished() currently returns false,
      // as both from and to dates are null (see PublishingCtx)
      // if (!(request.getVersion() == Resource.WORK) && !pagelet.isPublished())
      // {
      // logger.debug("Skipping pagelet " + position + " in composer " + id +
      // " since it is not yet published");
      // return;
      // }

      // Select the actual renderer by method and have it render the
      // request. Since renderers are being pooled by the bundle, we
      // have to return it after the request has finished.

      Module m = site.getModule(moduleId);
      if (m == null) {
        logger.warn("Unable to render '{}' on {}://{}: module '{}' not installed", new Object[] {
            rendererId,
            site,
            request.getRequestedUrl(),
            moduleId });
        return;
      }

      // Load renderer
      renderer = m.getRenderer(rendererId);
      if (renderer == null) {
        logger.warn("No suitable renderer '" + moduleId + "/" + rendererId + "' found to handle " + url);
        return;
      }

      // Flush all data that has been created previously
      writer.flush();

      response.addTag(CacheTag.Position, Integer.toString(position));
      response.addTag(CacheTag.Module, pagelet.getModule());
      response.addTag(CacheTag.Renderer, pagelet.getIdentifier());

      response.setClientRevalidationTime(renderer.getRecheckTime());
      response.setCacheExpirationTime(renderer.getValidTime());

      // Pass control to callback
      int beforePageletResult = beforePagelet(pagelet, position, writer, isGhostContent);

      // Do we need to process this pagelet?
      if (beforePageletResult == SKIP_PAGELET) {
        // At least close pagelet properly before returning
        try {
          afterPagelet(pagelet, position, writer);
        } catch (ContentRepositoryException e) {
          logger.warn("Failed to close pagelet: {}", e.getMessage());
          response.invalidate();
        } catch (ContentRepositoryUnavailableException e) {
          logger.warn("Failed to close pagelet due to missing content repository");
          response.invalidate();
        }

        return;
      }

      renderingState = RenderingState.InsidePagelet;
      writer.flush();

      // Check whether this request is being controlled by an action. If
      // so, we have to call the action on composer and pagelet start

      if (action != null && action instanceof HTMLAction) {
        HTMLAction htmlAction = (HTMLAction) action;
        try {
          if (htmlAction.startPagelet(request, response, pagelet) == HTMLAction.SKIP_PAGELET) {
            return;
          }
        } catch (Exception e) {
          logger.warn("Exception while rendering pagelet through action " + action + " on " + url, e);
          response.invalidate();
        }
      }

      logger.debug("Rendering pagelet " + renderer);

      // Render pagelet
      try {
        renderer.render(request, response);
        // if (orientation_ == ORIENTATION_VERTICAL) {
        // writer.println("<br class=\"weblounge\"/>");
        // }
        writer.flush();

      } catch (Throwable e) {
        // String params = RequestUtils.getParameters(request);
        String msg = "Error rendering " + renderer + " on " + url + "'";
        String reason = "";
        Throwable o = e.getCause();
        if (o != null) {
          reason = o.getMessage();
          msg += ": " + reason;
          logger.error(msg, o);
        } else {
          logger.error(msg, e);
        }

        if (version == Resource.WORK) {
          // TODO: Read error message from labels
          writer.println("Error while rendering &quot;" + renderer + "&quot;<br />");
        }

        throw e;
      }

    } catch (Throwable t) {
      response.invalidate();
    } finally {

      // Syntactically close the pagelet
      if (renderingState.equals(RenderingState.InsidePagelet)) {
        try {
          afterPagelet(pagelet, position, writer);
        } catch (ContentRepositoryException e) {
          logger.warn("Failed to close pagelet: {}", e.getMessage());
          response.invalidate();
        } catch (ContentRepositoryUnavailableException e) {
          logger.warn("Failed to close pagelet due to missing content repository");
          response.invalidate();
        }
        renderingState = RenderingState.InsideComposer;
      }

      // Flush everything to the response
      writer.flush();

      // Restore action attributes that may have been overwritten by
      // pagelets
      for (String key : attributes.keySet()) {
        request.setAttribute(key, attributes.get(key));
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    attributes.clear();
    contentInheritanceEnabled = false;
    contentIsInherited = false;
    contentProvider = null;
    ghostContentProvider = null;
    debug = false;
    initialized = false;
    pagelets = null;
    ghostPaglets = null;
    renderingState = RenderingState.Outside;
    targetPage = null;
  }

}
