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

package ch.o2it.weblounge.taglib;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageTemplate;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.content.page.PageletRenderer;
import ch.o2it.weblounge.common.impl.content.page.ComposerImpl;
import ch.o2it.weblounge.common.impl.content.page.PageURIImpl;
import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.HTMLAction;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.taglib.content.ComposerTag;

import org.apache.commons.lang.StringUtils;
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

  /** The composer orientation */
  public enum Orientation {
    Horizontal, Vertical
  };

  /** The possible states that this tag can be in while rendering the composer */
  public enum RenderingState {
    Outside, InsideComposer, InsidePagelet
  };

  /** Css class name for vertical composer */
  public static final String CLASS_VCOMPOSER = "vcomposer";

  /** Css class name for horizontal composer */
  public static final String CLASS_HCOMPOSER = "hcomposer";

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

  /** The composer orientation */
  protected Orientation orientation = Orientation.Vertical;

  /** The composer title */
  protected String description = null;

  /** The underlying page */
  protected Page targetPage = null;

  /** The content providing page */
  protected Page contentProvider = null;

  /** True if the content is not coming from the target page directly */
  protected boolean contentIsInherited = false;

  /** The pagelets within this composer */
  protected Pagelet[] pagelets = null;

  /** The current rendering state */
  protected RenderingState renderingState = RenderingState.Outside;

  /** True if the tag tried to load the composer data */
  private boolean initialized = false;

  /** True to turn on debug comments to indicate start and end of elements */
  protected boolean debug = false;

  /* Request attributes */
  protected final Map<String, Object> attributes = new HashMap<String, Object>();

  /**
   * Sets the composer identifier.
   * 
   * @param value
   *          the composer identifier
   */
  @Override
  public void setName(String value) {
    id = value;
    name = value;
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
   * Sets the composer orientation. Currently, this feature is not implemented.
   * 
   * @param value
   *          the composer orientation
   */
  public void setOrientation(String value) {
    if (StringUtils.trimToNull(value) == null)
      throw new IllegalStateException("Orientation must not be null or empty");
    for (Orientation o : Orientation.values()) {
      if (o.toString().equalsIgnoreCase(value)) {
        orientation = o;
        return;
      }
    }
    throw new IllegalStateException("Unknown composer orientation: '" + value + "'");
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
   * Sets the composer title.
   * 
   * @see ch.o2it.weblounge.taglib.WebloungeTag#setTitle(java.lang.String)
   */
  public void setDescription(String value) {
    description = value;
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
   * @see #afterComposer(JspWriter)
   */
  protected void beforeComposer(JspWriter writer) throws IOException {
    StringBuffer buf = new StringBuffer("<div ");
    if (request.getVersion() == Resource.WORK && targetPage.isLocked()) {
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
   * @throws IOException
   *           if writing to the output fails
   * @see #beforeComposer(JspWriter)
   */
  protected void afterComposer(JspWriter writer) throws IOException {
    writer.println("</div>");
  }

  /**
   * Returns the attributes that need to be added to the composer. This default
   * implementation will return the list of default attributes as defined by the
   * superclass.
   * 
   * @return the attributes that should be added to the composer
   */
  protected Map<String, String> getComposerAttributes() {
    addCssClass(getOrientationClass(orientation));
    return getStandardAttributes();
  }

  /**
   * Returns the css class that should be added to the composer according to the
   * composer's orientation.
   * <p>
   * This default implementation will return
   * <ul>
   * <li><code>vcomposer</code> for vertical orientation</li>
   * <li><code>hcomposer</code> for horizontal orientation</li>
   * </ul>
   * </p>
   * Returning <code>null</code> will result in no class attribute to be added.
   * 
   * @param orientation
   *          the composer orientation
   * @return the css class name
   */
  protected String getOrientationClass(Orientation orientation) {
    switch (orientation) {
      case Horizontal:
        return CLASS_HCOMPOSER;
      case Vertical:
        return CLASS_VCOMPOSER;
    }
    return null;
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
   * 
   * @return <code>{@link #EVAL_PAGELET}</code> or
   *         <code>{@link #SKIP_PAGELET}</code>
   * @throws IOException
   *           if writing to the composer fails
   */
  protected int beforePagelet(Pagelet pagelet, int position, JspWriter writer)
      throws IOException {
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
   */
  protected void afterPagelet(Pagelet pagelet, int position, JspWriter writer)
      throws IOException {
  }

  /**
   * Checks if the current composer contains pagelets. If not, content is loaded
   * and returned from a parent page.
   * 
   * @return the page that provided the content
   * @throws ContentRepositoryException
   */
  private void loadContent(boolean inheritFromParent) throws SecurityException,
      ContentRepositoryException {

    try {
      WebUrl url = getRequest().getUrl();
      Site site = request.getSite();
      ContentRepository contentRepository = ContentRepositoryFactory.getRepository(site);
      if (contentRepository == null) {
        logger.warn("Content repository unavailable for site '{}'", site.getIdentifier());
        return;
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
          logger.warn("Composer '" + name + "' was unable to choose homepage as fallback: " + e.getMessage());
          return;
        } catch (ContentRepositoryException e) {
          logger.warn("Composer '" + name + "' was unable to choose homepage as fallback: " + e.getMessage());
          return;
        }
      }

      Page contentProvider = targetPage;
      Pagelet[] content = contentProvider.getPagelets(name);

      // If composer is empty and ghost content is enabled, go up the page
      // hierarchy and try to find content for this composer
      if (inheritFromParent) {
        String pageUrl = contentProvider.getURI().getPath();
        while (content.length == 0 && pageUrl.length() > 1) {
          if (pageUrl.endsWith("/") && !"/".equals(pageUrl))
            pageUrl = pageUrl.substring(0, pageUrl.length() - 1);
          int urlSeparator = pageUrl.lastIndexOf("/");
          if (urlSeparator < 0) {
            contentProvider = null;
            break;
          } else {
            pageUrl = pageUrl.substring(0, urlSeparator);
            if ("".equals(pageUrl))
              pageUrl = "/";
            ResourceURI pageURI = new PageURIImpl(site, pageUrl);
            try {
              contentProvider = (Page) contentRepository.get(pageURI);
              if (contentProvider != null) {
                if (!Page.TYPE.equals(contentProvider.getType())) {
                  logger.debug("Home page is not of type '{}'", Page.TYPE);
                  return;
                }
                content = contentProvider.getPagelets(name);
              }
            } catch (SecurityException e) {
              logger.debug("Prevented loading of protected content from inherited page {} for composer {}", pageURI, name);
            }
          }
        }
      }

      // If pagelets have been found, set them in the composer
      if (content != null && content.length > 0) {
        pagelets = content;
      } else {
        pagelets = new Pagelet[] {};
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
   */
  protected Page getTargetPage() {
    if (!initialized) {
      try {
        loadContent(contentInheritanceEnabled);
      } catch (Exception e) {
        logger.warn("Unable to load composer content: {}", e.getMessage());
      }
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
   * Returns the composer's pagelets. Note that the pagelets are only available
   * on or after the first call to {@link #beforeComposer(JspWriter)}.
   * 
   * @return the pagelets
   * @throws ContentRepositoryException
   *           if loading the content fails
   * @throws SecurityException
   *           if accessing the content is forbidden
   */
  protected Pagelet[] getContent() throws SecurityException,
      ContentRepositoryException {
    if (contentProvider == null)
      loadContent(contentInheritanceEnabled);
    if (pagelets == null)
      pagelets = new Pagelet[] {};
    return pagelets;
  }

  @Override
  public int doStartTag() throws JspException {
    Enumeration<?> e = request.getAttributeNames();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      attributes.put(key, request.getAttribute(key));
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
    logger.debug("Rendering composer " + name);

    Site site = request.getSite();
    WebUrl url = request.getUrl();
    ComposerImpl composer = new ComposerImpl(name);
    JspWriter writer = pageContext.getOut();

    Action action = (Action) request.getAttribute(WebloungeRequest.ACTION);
    CacheTagSet composerCacheTags = null;

    try {

      // Flush all input that has been written to the response so far.
      writer.flush();
      if (debug) {
        writer.println("<!-- start content of composer '" + name + "' -->");
        writer.flush();
      }

      try {

        // Check if this composer is already in the cache
        if (request.getVersion() == Resource.LIVE) {

          composerCacheTags = new CacheTagSet();
          long validTime = Times.MS_PER_DAY;
          long recheckTime = Times.MS_PER_HOUR;

          // Create tagset
          composerCacheTags.add(CacheTag.Url, url.getPath());
          composerCacheTags.add(CacheTag.Url, request.getRequestedUrl().getPath());
          composerCacheTags.add(CacheTag.Composer, name);
          composerCacheTags.add(CacheTag.Site, url.getSite().getIdentifier());
          composerCacheTags.add(CacheTag.Language, request.getLanguage().getIdentifier());
          composerCacheTags.add(CacheTag.User, request.getUser().getLogin());
          Enumeration<?> pe = request.getParameterNames();
          int parameterCount = 0;
          while (pe.hasMoreElements()) {
            parameterCount++;
            String key = pe.nextElement().toString();
            String[] values = request.getParameterValues(key);
            for (String value : values) {
              composerCacheTags.add(key, value);
            }
          }
          composerCacheTags.add(CacheTag.Parameters, Integer.toString(parameterCount));

          // Add action specific tags and adjust valid and recheck time
          if (action != null) {
            composerCacheTags.add(CacheTag.Module, action.getModule().getIdentifier());
            composerCacheTags.add(CacheTag.Action, action.getIdentifier());
            validTime = action.getValidTime();
            recheckTime = action.getRecheckTime();
          }

          // See if the composer is already in the cache. If not, a new response
          // part is started, that we can now add content for
          if (response.startResponsePart(composerCacheTags.getTags(), validTime, recheckTime)) {
            return EVAL_PAGE;
          }

          if (debug)
            writer.println("<!-- start content of composer '" + name + "' (cached) -->");
        } else {
          if (debug)
            writer.println("<!-- start content of composer '" + name + "' (generated) -->");
        }

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
            if (template != null && name.equalsIgnoreCase(template.getStage())) {
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
        if (composerCacheTags != null && contentProvider != null && !contentProvider.equals(targetPage)) {
          response.addTag(CacheTag.Url, contentProvider.getURI().getPath());
        }

        // Render the pagelets
        for (int i = 0; i < pagelets.length; i++) {
          Pagelet pagelet = pagelets[i];

          // Add pagelet and composer to the request
          request.setAttribute(WebloungeRequest.PAGELET, pagelet);
          request.setAttribute(WebloungeRequest.COMPOSER, composer);
          
          doPagelet(pagelet, i, writer);
        }

      } finally {

        // Syntactically close the composer
        if (renderingState.equals(RenderingState.InsideComposer)) {
          afterComposer(writer);
          renderingState = RenderingState.Outside;
          writer.flush();
        }

        // Cleanup request
        request.removeAttribute(WebloungeRequest.PAGELET);
        request.removeAttribute(WebloungeRequest.COMPOSER);

        // Close composer cache handle
        if (request.getVersion() == Resource.LIVE) {
          if (debug)
            writer.println("<!-- end generated content of composer '" + name + "' -->");
          response.endResponsePart();
        } else {
          if (debug)
            writer.println("<!-- end cached content of composer '" + name + "' -->");
        }
        writer.flush();
      }

    } catch (IOException e) {
      response.invalidate();
      logger.error("Unable to print to out", e);
      return EVAL_PAGE;
    } catch (Throwable t) {
      response.invalidate();
      String msg = "Exception when processing composer '" + name + "' on " + getRequest().getRequestedUrl();
      if (action != null)
        msg += " for action '" + action + "'";
      logger.error(msg, t);
      return EVAL_PAGE;
    } finally {
      reset();
    }
    return EVAL_PAGE;
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
  protected void doPagelet(Pagelet pagelet, int position, JspWriter writer)
      throws IOException {

    Site site = request.getSite();
    WebUrl url = request.getUrl();
    long version = request.getVersion();

    Action action = (Action) request.getAttribute(WebloungeRequest.ACTION);

    PageletRenderer renderer = null;

    try {
      CacheTagSet pageletCacheTags = new CacheTagSet();

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
      if (!(request.getVersion() == Resource.WORK) && !pagelet.isPublished()) {
        logger.debug("Skipping pagelet " + position + " in composer " + name + " since it is not yet published");
        return;
      }

      // Select the actual renderer by method and have it render the
      // request. Since renderers are beeing pooled by the bundle, we
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

      // Check to see if the pagelet is already part of the cache. If it
      // is, we may already
      // move to the next pagelet!

      // Create a new cache handle with the configured times
      long validTime = renderer.getValidTime();
      long recheckTime = renderer.getRecheckTime();
      if (request.getVersion() == Resource.LIVE) {

        // Create tagset
        pageletCacheTags.add(CacheTag.Url, url.getPath());
        pageletCacheTags.add(CacheTag.Url, request.getRequestedUrl().getPath());
        pageletCacheTags.add(CacheTag.Composer, name);
        pageletCacheTags.add(CacheTag.Position, Integer.toString(position));
        pageletCacheTags.add(CacheTag.Module, pagelet.getModule());
        pageletCacheTags.add(CacheTag.Renderer, pagelet.getIdentifier());
        pageletCacheTags.add(CacheTag.Language, request.getLanguage().getIdentifier());
        pageletCacheTags.add(CacheTag.User, request.getUser().getLogin());
        pageletCacheTags.add(CacheTag.Site, url.getSite().getIdentifier());
        Enumeration<?> pe = request.getParameterNames();
        int parameterCount = 0;
        while (pe.hasMoreElements()) {
          parameterCount++;
          String key = pe.nextElement().toString();
          String[] values = request.getParameterValues(key);
          for (String value : values) {
            pageletCacheTags.add(key, value);
          }
        }
        pageletCacheTags.add(CacheTag.Parameters, Integer.toString(parameterCount));

        // Add action specific tags
        if (action != null) {
          pageletCacheTags.add(CacheTag.Module, action.getModule().getIdentifier());
          pageletCacheTags.add(CacheTag.Action, action.getIdentifier());
        }

        if (response.startResponsePart(pageletCacheTags.getTags(), validTime, recheckTime)) {
          return;
        }

        // Add cache tag for content provider (in case of inheritance)
        if (contentProvider != null && !contentProvider.equals(targetPage)) {
          response.addTag(CacheTag.Url, contentProvider.getURI().getPath());
        }

        if (debug)
          writer.println("<!-- start cache content of pagelet " + position + " -->");
        writer.flush();
      }

      // Pass control to callback
      int beforePageletResult = beforePagelet(pagelet, position, writer);

      // Do we need to process this pagelet?
      if (beforePageletResult == SKIP_PAGELET)
        return;

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
        afterPagelet(pagelet, position, writer);
        renderingState = RenderingState.InsideComposer;
      }

      // Flush everything to the response
      writer.flush();

      // Finish cache response
      if (request.getVersion() == Resource.LIVE) {
        if (debug)
          writer.println("<!-- end cache content of pagelet " + position + " -->");
        writer.flush();
        response.endResponsePart();
      }

      // Restore action attributes that may have been overwritten by
      // pagelets
      for (String key : attributes.keySet()) {
        request.setAttribute(key, attributes.get(key));
      }
    }
  }

  /**
   * Resets the properties of this tag to default values.
   */
  protected void reset() {
    super.reset();
    attributes.clear();
    targetPage = null;
    contentProvider = null;
    pagelets = null;
    description = null;
    contentInheritanceEnabled = false;
    orientation = Orientation.Vertical;
    renderingState = RenderingState.Outside;
    initialized = false;
  }

}
