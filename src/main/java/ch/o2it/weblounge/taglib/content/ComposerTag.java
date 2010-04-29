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

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageTemplate;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.content.PageletRenderer;
import ch.o2it.weblounge.common.impl.content.ComposerImpl;
import ch.o2it.weblounge.common.impl.content.PageURIImpl;
import ch.o2it.weblounge.common.impl.request.CacheTagSet;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.HTMLAction;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * <code>ComposerTag</code> implements the handler for <code>module</code> tags
 * embedded in a jsp file. The handler will request the specified module to
 * return some resources using the requested view.
 */
public class ComposerTag extends WebloungeTag {

  /** The serial version id */
  private static final long serialVersionUID = 1L;

  /** Horizontal orientation */
  public static final int ORIENTATION_HORIZONTAL = 0;

  /** Vertical orientation */
  public static final int ORIENTATION_VERTICAL = 1;

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

  /** the composer identifier */
  protected String composerName = null;

  /** true if the composer should not be editable */
  protected boolean isLocked = false;

  /** true to enable content inheritance */
  protected boolean ghostContentEnabled = false;

  /** the composer orientation */
  protected int orientation = ORIENTATION_VERTICAL;

  /** the composer title */
  protected String description = null;

  /** the underlying page */
  protected Page targetPage = null;

  /** the content providing page */
  protected Page contentProvider = null;

  /** the pagelets within this composer */
  protected Pagelet[] pagelets = null;

  /** True if a composer has been opened */
  boolean composerIsOpen = false;

  /** True if a pagelet has been opened */
  boolean pageletIsOpen = false;

  /* Request attributes */
  protected final Map<String, Object> attributes = new HashMap<String, Object>();

  // Logging

  /** the class name, used for the loggin facility */
  private final static String className = ComposerTag.class.getName();

  /** Logging facility provided by log4j */
  private final static Logger log_ = LoggerFactory.getLogger(className);

  /**
   * Sets the composer identifier.
   * 
   * @param value
   *          the composer identifier
   */
  @Override
  public void setName(String value) {
    composerName = value;
  }

  /**
   * Sets the composer orientation. Currently, this feature is not implemented.
   * 
   * @param value
   *          the composer orientation
   */
  public void setOrientation(String value) {
    if ("horizontal".equals(value.trim().toLowerCase())) {
      orientation = ORIENTATION_HORIZONTAL;
    } else if ("vertical".equals(value.trim().toLowerCase())) {
      orientation = ORIENTATION_VERTICAL;
    }
  }

  /**
   * Enables content inheritance for this composer.
   * 
   * @param value
   *          <code>true</code>, <code>yes</code> or <code>on</code> will enable
   *          the feature
   */
  public void setInherit(String value) {
    ghostContentEnabled = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
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
   * Sets the composer to a locked state, preventing editing at all.
   * 
   * @param value
   *          <code>true</code> to lock the composer
   */
  public void setLocked(String value) {
    isLocked = "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
  }

  /**
   * Callback that is executed before the composer ist being filled with
   * content. This is the right place to send any opening divs etc. to the page.
   */
  protected void beforeComposer(JspWriter writer) throws IOException {
    StringBuffer buf = new StringBuffer("<div ");
    if (request.getVersion() == Page.WORK && targetPage.isLocked()) {
      addCssClass(CLASS_LOCKED);
    }
    buf.append(getStandardAttributes());
    buf.append(">");
    writer.println(buf.toString());
  }

  /**
   * Callback that is executed after the composer has been filled with content.
   * This is the right place to close any previously opened divs.
   */
  protected void afterComposer(JspWriter writer) throws IOException {
    writer.println("</div>");
  }

  /**
   * Callback that is executed before the composer is being filled with content.
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
  protected Pagelet[] loadContent(boolean inheritFromParent) throws SecurityException, ContentRepositoryException {
    Page p = targetPage;
    Pagelet[] content = p.getPagelets(composerName);
    boolean originalContent = true;
    long version = request.getVersion();
    Site site = request.getSite();
    User user = request.getUser();
    boolean isLocked = targetPage.isLocked();
    boolean isLockedByCurrentUser = isLocked && user.equals(targetPage.getLockOwner());
    ContentRepository contentRepository = ContentRepositoryFactory.getRepository(site);

    // If composer is empty and ghost content is enabled, go up the page
    // hierarchy and try to find content for this composer
    if (inheritFromParent && !(version == Page.WORK && isLockedByCurrentUser)) {
      String pageUrl = p.getURI().getPath();
      while (content.length == 0 && pageUrl.length() > 1) {
        if (pageUrl.endsWith("/") && !"/".equals(pageUrl))
          pageUrl = pageUrl.substring(0, pageUrl.length() - 1);
        int urlSeparator = pageUrl.lastIndexOf("/");
        if (urlSeparator < 0) {
          p = null;
          break;
        } else {
          pageUrl = pageUrl.substring(0, urlSeparator);
          PageURI pageURI = new PageURIImpl(site, pageUrl);
          try {
            p = contentRepository.getPage(pageURI, user, SystemPermission.READ);
            if (p != null)
              content = p.getPagelets(composerName);
            originalContent = false;
          } catch (SecurityException e) {
            log_.debug("Tried to load protected content from inherited page {} for composer {}", pageURI, composerName);
          }
        }
      }
    }

    // Mark inherited composer and ghost content in locked work mode
    if (version == Page.WORK && isLockedByCurrentUser) {
      if (inheritFromParent)
        addCssClass(CLASS_INHERIT_CONTENT);
      if (!originalContent)
        addCssClass(CLASS_GHOST_CONTENT);
    }

    // If pagelets have been found, set them in the composer
    if (content != null && content.length > 0) {
      pagelets = content;
    } else {
      pagelets = new Pagelet[] {};
    }

    // Mark empty composers
    if (pagelets.length == 0) {
      addCssClass(CLASS_EMPTY);
    }

    contentProvider = p;
    return pagelets;
  }

  /**
   * Returns the page that actually delivers the content for this composer.
   * 
   * @return the content delivering page
   */
  protected Page getContentProvider() {
    return contentProvider;
  }

  /**
   * Returns the composer's pagelets. Note that the pagelets are only available
   * on or after the first call to {@link #beforeComposer(JspWriter)}.
   * 
   * @return the pagelets
   */
  protected Pagelet[] getPagelets() {
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
    log_.debug("Rendering composer " + composerName);
    
    Site site = request.getSite();
    User user = request.getUser();
    WebUrl url = request.getUrl();
    ComposerImpl composer = new ComposerImpl(composerName);
    JspWriter writer = pageContext.getOut();

    ContentRepository contentRepository = ContentRepositoryFactory.getRepository(site);
    targetPage = (Page)request.getAttribute(WebloungeRequest.PAGE);

    // If no page was specified, take homepage instead.
    if (targetPage == null) {
      PageURI homeURI = new PageURIImpl(site, "/");
      try {
        targetPage = contentRepository.getPage(homeURI, user, SystemPermission.READ);
        if (targetPage == null) {
          log_.warn("No page was found while processing composer on " + url);
          return EVAL_PAGE;
        }
      } catch (SecurityException e) {
        log_.warn("Composer '" + composerName + "' was unable to choose homepage as fallback: " + e.getMessage());
        return EVAL_PAGE;
      } catch (ContentRepositoryException e) {
        log_.warn("Composer '" + composerName + "' was unable to choose homepage as fallback: " + e.getMessage());
        return EVAL_PAGE;
      }
    }
    
    long version = request.getVersion();
    
    boolean isLocked = targetPage.isLocked();
    boolean isLockedByCurrentUser = isLocked && user.equals(targetPage.getLockOwner());

    Action action = (Action) request.getAttribute(WebloungeRequest.ACTION);
    CacheTagSet composerCacheTags = null;

    try {

      // Flush all input that has been written to the response so far.
      pageContext.getOut().flush();
      writer.println("<!-- start content of composer '" + composerName + "' -->");
      writer.flush();

      // Open the composer
      switch (orientation) {
        case ORIENTATION_HORIZONTAL:
          addCssClass(CLASS_HCOMPOSER);
          break;
        case ORIENTATION_VERTICAL:
          addCssClass(CLASS_VCOMPOSER);
          break;
      }

      try {

        // Set composer id
        request.setAttribute(WebloungeRequest.COMPOSER, composerName);

        // Check if this composer is already in the cache
        if (request.getVersion() == Page.LIVE) {

          composerCacheTags = new CacheTagSet();
          long validTime = Times.MS_PER_DAY;
          long recheckTime = Times.MS_PER_HOUR;

          // Create tagset
          composerCacheTags.add("webl:url", url.getPath());
          composerCacheTags.add("webl:url", request.getRequestedUrl().getPath());
          composerCacheTags.add("webl:composer", composerName);
          composerCacheTags.add("webl:site", url.getSite().getIdentifier());
          composerCacheTags.add("webl:language", request.getLanguage().getIdentifier());
          composerCacheTags.add("webl:user", request.getUser().getLogin());
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
          composerCacheTags.add("webl:parameters", Integer.toString(parameterCount));

          // Add action specific tags and adjust valid and recheck time
          if (action != null) {
            composerCacheTags.add("webl:module", action.getModule().getIdentifier());
            composerCacheTags.add("webl:action", action.getIdentifier());
            validTime = action.getValidTime();
            recheckTime = action.getRecheckTime();
          }

          // See if the composer is already in the cache. If not, a new response
          // part is started, that we can now add content for
          if (response.startResponsePart(composerCacheTags, validTime, recheckTime)) {
            return EVAL_PAGE;
          }

          writer.println("<!-- start content of composer '" + composerName + "' (cached) -->");
          writer.flush();
        } else {
          writer.println("<!-- start content of composer '" + composerName + "' (generated) -->");
          writer.flush();
        }

        // Start the composer
        beforeComposer(writer);
        composerIsOpen = true;
        writer.flush();

        // Check for action handler
        if (action != null && action instanceof HTMLAction) {
          HTMLAction htmlAction = (HTMLAction)action;
          log_.debug("Action handler found");

          if (targetPage != null) {
            String templateId = targetPage.getTemplate();
            PageTemplate template = site.getTemplate(templateId);
            if (template != null && composerName.equalsIgnoreCase(template.getStage())) {
              if (htmlAction.startStage(request, response, composer) == HTMLAction.SKIP_COMPOSER) {
                return EVAL_PAGE;
              }
            }
          }
          if (htmlAction.startComposer(request, response, composer) == HTMLAction.SKIP_COMPOSER) {
            return EVAL_PAGE;
          }
        }

        // Load the pagelets
        pagelets = loadContent(ghostContentEnabled);
        composer.setPagelets(pagelets);

        // Add cache tag for content provider (in case of inheritance)
        if (composerCacheTags != null && contentProvider != null && !contentProvider.equals(targetPage)) {
          response.addTag("webl:url", contentProvider.getURI().getPath());
        }

        // Initialize for pagelets processing
        CacheTagSet pageletCacheTags = null;

        // Add first handle
//        if (version == Page.WORK && isLockedByCurrentUser) {
//          request.setAttribute(WebloungeRequest.COMPOSER, composer);
//          PageletEditorTag editorTag = new PageletEditorTag();
//          editorTag.showPageletEditor(getRequest(), getResponse(), writer);
//          writer.flush();
//        }

        p: for (int i = 0; i < pagelets.length; i++) {
          PageletRenderer renderer = null;
          Pagelet pagelet = pagelets[i];
          try {
            pageletCacheTags = new CacheTagSet();

            // Add the pagelet to the request. Like this, actions may have
            // access to the
            // pagelet data as well.
            request.setAttribute(WebloungeRequest.PAGELET, pagelet);
            request.setAttribute(WebloungeRequest.COMPOSER, composerName);

            String moduleId = pagelet.getModule();
            String rendererId = pagelet.getIdentifier();

            // Check access rights
//            Permission p = SystemPermission.READ;
//            if (!pagelet.checkOne(p, user.getRoleClosure()) && !pagelet.check(p, user)) {
//              log_.debug("Skipping pagelet " + i + " in composer " + composer + " due to insufficient rights");
//              continue p;
//            }

            // Check publishing dates
            if (!(request.getVersion() == Page.WORK) && !pagelet.isPublished()) {
              log_.debug("Skipping pagelet " + i + " in composer " + composerName + " since it is not yet published");
              continue p;
            }

            // Select the actual renderer by method and have it render the
            // request. Since renderers are beeing pooled by the bundle, we
            // have to return it after the request has finished.

            Module m = site.getModule(moduleId);
            if (m == null) {
              log_.warn("Unable to load renderer '" + rendererId + "' for pagelet in composer '" + composer + "' on " + url + ": module '" + moduleId + "' not found!");
              continue p;
            }

            // Load renderer
            renderer = m.getRenderer(rendererId);
            if (renderer == null) {
              log_.warn("No suitable renderer '" + moduleId + "/" + rendererId + "' found to handle " + url);
              continue p;
            }

            // Check to see if the pagelet is already part of the cache. If it
            // is, we may already
            // move to the next pagelet!

            // Create a new cache handle with the configured times
            long validTime = renderer.getValidTime();
            long recheckTime = renderer.getRecheckTime();
            if (request.getVersion() == Page.LIVE) {

              // Create tagset
              pageletCacheTags.add("webl:url", url.getPath());
              pageletCacheTags.add("webl:url", request.getRequestedUrl().getPath());
              pageletCacheTags.add("webl:composer", composerName);
              pageletCacheTags.add("webl:renderer-position", i);
              pageletCacheTags.add("webl:module", pagelet.getModule());
              pageletCacheTags.add("webl:renderer", pagelet.getIdentifier());
              pageletCacheTags.add("webl:language", request.getLanguage().getIdentifier());
              pageletCacheTags.add("webl:user", request.getUser().getLogin());
              pageletCacheTags.add("webl:site", url.getSite().getIdentifier());
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
              pageletCacheTags.add("webl:parameters", Integer.toString(parameterCount));

              // Add action specific tags
              if (action != null) {
                pageletCacheTags.add("webl:module", action.getModule().getIdentifier());
                pageletCacheTags.add("webl:action", action.getIdentifier());
              }

              if (response.startResponsePart(pageletCacheTags, validTime, recheckTime)) {
                continue p;
              }

              // Add cache tag for content provider (in case of inheritance)
              if (contentProvider != null && !contentProvider.equals(targetPage)) {
                response.addTag("webl:url", contentProvider.getURI().getPath());
              }

              writer.println("<!-- start cache content of pagelet " + i + " -->");
              writer.flush();
            }

            // Pass control to callback
            int beforePageletResult = beforePagelet(pagelet, i, writer);
            pageletIsOpen = true;
            writer.flush();
            if (beforePageletResult == SKIP_PAGELET)
              continue p;

            // Check whether this request is being controlled by an action. If
            // so, we have to call the action on composer and pagelet start

            if (action != null && action instanceof HTMLAction) {
              HTMLAction htmlAction = (HTMLAction)action;
              try {
                if (htmlAction.startPagelet(request, response, pagelet) == HTMLAction.SKIP_PAGELET) {
                  continue p;
                }
              } catch (Exception e) {
                log_.warn("Exception while rendering pagelet through action " + action + " on " + url, e);
                response.invalidate();
              }
            }

            log_.debug("Rendering pagelet " + renderer);

            // Start editing support
//            if (version == Page.WORK && isLockedByCurrentUser) {
//              writer.println("<div class=\"pagelet\">");
//              writer.flush();
//            }

            // Render pagelet
            try {
              renderer.render(request, response);
              // if (orientation_ == ORIENTATION_VERTICAL) {
              // writer.println("<br class=\"weblounge\"/>");
              // }
              writer.flush();

            } catch (Throwable e) {
//              String params = RequestUtils.getParameters(request);
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

              if (version == Page.WORK && isLockedByCurrentUser) {
                // TODO: Read error message from labels
                writer.println("Error while rendering &quot;" + renderer + "&quot;<br />");
              }

              throw e;
            }

            // If user is not editing this page, then we are finished with
            // the current pagelet.
//            finally {
//              if (version == Page.WORK && isLockedByCurrentUser && request.getAttribute(PageletEditorTag.ID) == null) {
//                request.setAttribute(WebloungeRequest.PAGE, targetPage);
//                request.setAttribute(WebloungeRequest.PAGELET, pagelet);
//                request.setAttribute(WebloungeRequest.COMPOSER, composer);
//                PageletEditorTag editorTag = new PageletEditorTag();
//                editorTag.showPageletEditor(getRequest(), getResponse(), writer);
//              }
//            }

          } catch (Throwable t) {
            response.invalidate();
          } finally {

            // Syntactically close the pagelet
            if (pageletIsOpen) {
              afterPagelet(pagelet, i, writer);
              pageletIsOpen = false;
              writer.flush();
            }

            if (version == Page.WORK && isLockedByCurrentUser) {
              writer.println("</div>");
            }

            // Flush everything to the response
            writer.flush();

            // Remove temporary request attributes
//            request.removeAttribute(PageletEditorTag.ID);

            // Finish cache response
            if (request.getVersion() == Page.LIVE) {
              writer.println("<!-- end cache content of pagelet " + i + " -->");
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

      } finally {

        // Syntactically close the composer
        if (composerIsOpen) {
          afterComposer(writer);
          composerIsOpen = false;
          writer.flush();
        }

        // Cleanup request
        request.removeAttribute(WebloungeRequest.PAGELET);
        request.removeAttribute(WebloungeRequest.COMPOSER);

        // Close composer cache handle
        if (request.getVersion() == Page.LIVE) {
          writer.println("<!-- end generated content of composer '" + composerName + "' -->");
          writer.flush();
          response.endResponsePart();
        } else {
          writer.println("<!-- end cached content of composer '" + composerName + "' -->");
          writer.flush();
        }
      }

    } catch (IOException e) {
      response.invalidate();
      log_.error("Unable to print to out", e);
      return EVAL_PAGE;
    } catch (Throwable t) {
      response.invalidate();
      String msg = "Exception when processing composer '" + composerName + "' on " + getRequest().getRequestedUrl();
      if (action != null)
        msg += " for action '" + action + "'";
      log_.error(msg, t);
      return EVAL_PAGE;
    } finally {
      reset();
    }
    return EVAL_PAGE;
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
    composerName = null;
    description = null;
    ghostContentEnabled = false;
    orientation = ORIENTATION_VERTICAL;
    composerIsOpen = false;
    pageletIsOpen = false;
  }

}
