/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.site.impl.handler;

import static ch.o2it.weblounge.common.impl.request.CacheTagImpl.*;

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.impl.request.Http11Utils;
import ch.o2it.weblounge.common.impl.request.RequestSupport;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.request.CacheTag;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Renderer;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteLogger;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.contentrepository.PageManager;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.site.impl.ActionHandlerBundle;
import ch.o2it.weblounge.site.impl.ActionRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * This handler is registered with the site dispatcher. If a request is targeted
 * to a registered action, it will answer <code>true</code> to the
 * <code>match</code> call and handle the request by forwarding it to the
 * appropriate action handler.
 */
public final class ActionRequestHandler implements RequestHandler {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ActionRequestHandler.class);

  /** The registered actions */
  private Map<String, Action> actions = null;

  /**
   * Creates a new action request handler.
   */
  public ActionRequestHandler() {
    actions = new HashMap<String, Action>();
  }

  /**
   * Registers the handler with this request handler.
   * 
   * @param handler
   *          the action handler
   */
  public void addHandler(ActionHandlerBundle handler) {
    actions.put(handler.getMountpoint(), handler);
    log_.debug("Action handler '{}' registered for {}", handler, handler.getConfiguration().getMountpoint());
  }

  /**
   * Removes <code>handler</code> from the list of registered handlers.
   * 
   * @param handler
   *          the action handler
   */
  public void removeHandler(ActionHandlerBundle handler) {
    actions.remove(handler.getIdentifier());
  }

  /**
   * Returns the action handler that is registered to serve the given url or
   * <code>null</code> if no such handler exists.
   * 
   * @param url
   *          the url
   * @return the handler
   */
  public Action getHandlerForUrl(WebUrl url) {
    return actions.getByUrl(url.getPath(), "html");
  }

  /**
   * Handles the request for a simple url available somewhere in the system. The
   * handler sets the response type and then starts processing.
   * <p>
   * This method should return <code>true</code> if the handler has decided to
   * handle the request, <code>false</code> otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   * @return <code>true</code> if this handler processed the request
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {
    Action action = actions.getByUrl(request.getUrl().getPath(), "html");
    if (action == null) {
      log_.debug("Action handler {}, denies to handle {}", this, request.getUrl());
      return false;
    }

    log_.debug("Action handler {} agrees to handle {}", this, request.getUrl());
    try {
      // check the request method
      String requestMethod = request.getMethod();
      if (!Http11Utils.checkDefaultMethods(requestMethod, response))
        return true;

      // Check if url may be accessed by user otherwise send
      // Access Denied
      // TODO: Check access rights with action handler configuration

      // Add request to history
      // SessionSupport.getHistory(request).addEntry(url);

      WebUrl url = request.getUrl();
      User user = request.getUser();
      Language language = request.getLanguage();
      Cache cache = (Cache) ServiceManager.getEnabledSystemService(Cache.ID);
      CacheHandle cacheHdl = null;
      boolean cacheActions = cache != null && !"false".equals(cache.getConfiguration().getOption("actions")) && request.getVersion() == Page.LIVE;

      // Check the request. If it is a common POST request then just go on
      // like usual. In case of multipart/form-data content wrap the request

      String content = request.getContentType();
      if ((content != null) && content.startsWith("multipart/form-data")) {
        cache = null; // Don't bother the cache
        MultipartRequestWrapper w = new MultipartRequestWrapper(request);
        log_.debug("Wrapping multipart/form-data request");
        if (w.getState() != HttpServletResponse.SC_ACCEPTED) {
          try {
            response.sendError(w.getState());
            return true;
          } catch (IOException e) {
            log_.error("Error when sending {} back to client: {}", w.getState(), e.getMessage());
          }
        }
        request = w;
      }

      Page page = getTargetPage(action, request);
      if (page == null) {
        try {
          log_.error("No page available to serve action {}", action);
          response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
          return true;
        } catch (IOException e) {
          log_.error("Error when sending {} back to client: {}", HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
        }
      }

      // check whether the cache can complete the request

      if (cacheActions) {
        CacheTagSet cacheTags = new CacheTagSet();
        long validTime = action.getConfiguration().getValidTime();
        long recheckTime = action.getConfiguration().getRecheckTime();

        // Create the set of tags that identify the action
        cacheTags.add(CacheTag.Url, url.getPath());
        cacheTags.add(CacheTag.Url, request.getRequestedUrl().getPath());
        cacheTags.add(CacheTag.Url, url.getPath());
        cacheTags.add(CacheTag.Language, language.getIdentifier());
        cacheTags.add(CacheTag.User, user.getLogin());
        cacheTags.add(CacheTag.Module, action.getModule().getIdentifier());
        cacheTags.add(CacheTag.Action, action.getIdentifier());
        cacheTags.add(CacheTag.Site, url.getSite().getIdentifier());
        Enumeration<?> pe = request.getParameterNames();
        int parameterCount = 0;
        while (pe.hasMoreElements()) {
          parameterCount++;
          String key = pe.nextElement().toString();
          String[] values = request.getParameterValues(key);
          for (String value : values) {
            cacheTags.add(key, value);
          }
        }
        cacheTags.add("webl:parameters", Integer.toString(parameterCount));

        // If the cache contains a valid copy of this content, return it
        cacheHdl = cache.startResponse(cacheTags, request, response, validTime, recheckTime);
        if (cacheHdl == null) {
          // the cache could complete the request on its own.
          // nothing more to do...
          return true;
        }
      } else if (METHOD_HEAD.equals(requestMethod)) {
        Http11Utils.startHeadResponse(response);
      }

      // Select the actual renderer by method and have it render the
      // request. Since renderers are being pooled by the bundle, we
      // have to return it after the request has finished.

      String method = request.getFlavor();
      try {
        if (action.provides(method)) {
          log_.info("Handling action request through {}", action);
          request.setAttribute(Action.ID, action);

          request.setAttribute(Page.ID, page);
          if (action instanceof AbstractActionHandler) {
            ((AbstractActionHandler) action).setPage(page);
          }

          // Determine renderer for this action
          String rendererId = (String) request.getAttribute(Renderer.TEMPLATE);
          Renderer renderer = null;
          Site site = request.getSite();
          if (rendererId != null) {
            renderer = site.getTemplates().getRenderer(rendererId, request.getFlavor());
          } else {
            renderer = page.getRenderer(request.getFlavor());
          }
          if (renderer == null) {
            String params = RequestSupport.getParameters(request);
            String msg = (rendererId != null) ? "Template '" + rendererId + "' was not found " : "No template was found ";
            msg += "to render url '" + url + "' " + params;
            site.getLogger().warn(msg);
            WebloungeDispatcher.sendInternalError(msg, request, response);
            return true;
          }

          // Add action as tag
          response.addTag("webl:module", action.getModule().getIdentifier());
          response.addTag("webl:action", action.getIdentifier());

          // Add underlying page
          response.addTag("webl:template", rendererId);
          response.addTag("webl:pagetype", page.getType());
          for (String keyword : page.getKeywords()) {
            response.addTag("webl:keyword", keyword);
          }

          try {
            action.configure(request, response, method);
            if (action.startPage(request, response) == Action.EVAL_PAGE) {
              RequestSupport.setTargetUrl(page.getUrl(), request);
              PageRequestHandler.getInstance().service(request, response);
            }
          } catch (Exception e) {
            String msg = "Error executing action " + action + ": " + e.getMessage();
            sitelogger_.warn(msg);
            WebloungeDispatcher.sendInternalError(msg, request, response);
            return true;
          } finally {
            request.removeAttribute(Action.ID);
            action.cleanup();
          }
        } else {
          response.invalidate();
          String msg = "Method '" + method + "' not supported by action '" + action + "'";
          sitelogger_.warn(msg);
          WebloungeDispatcher.sendInternalError(msg, request, response);
          return true;
        }
        if (cache == null && METHOD_HEAD.equals(requestMethod)) {
          Http11Utils.endHeadResponse(response);
        }
      } catch (Exception e) {
        String params = RequestSupport.getParameters(request);
        String msg = "Error while handling action " + action + " " + params + ": " + e.getMessage();
        sitelogger_.error(msg, e);
        response.invalidate();
      } finally {
        if (cacheActions) {
          if (cacheHdl != null && !cache.endResponse(response)) {
            String params = RequestSupport.getParameters(request);
            sitelogger_.warn("Error caching response for action " + action + " " + params + " at " + request.getUrl());
          }
        }
      }
    } finally {
      actions.returnHandler(action);
    }
    return true;
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getIdentifier()
   */
  public String getIdentifier() {
    return "action";
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "action handler";
  }

  /**
   * Try to determine the target url for the action result. The default is the
   * current page, but first the target-url request parameter will be
   * considered, and secondly the config parameter
   * 
   * @param request
   *          the weblounge request
   * @return the target page
   */
  private Page getTargetPage(Action action, WebloungeRequest request) {
    WebUrl target = null;
    Page page = null;
    User user = request.getUser();
    boolean targetForced = false;
    if (request.getParameter(Action.TARGET) != null) {
      Site site = request.getSite();
      String targetUrl = request.getParameter(Action.TARGET);
      targetForced = true;
      try {
        String decocedTargetUrl;
        String encoding = request.getCharacterEncoding();
        if (encoding == null)
          encoding = "utf-8";
        decocedTargetUrl = URLDecoder.decode(targetUrl, encoding);
        target = new WebUrlImpl(site, decocedTargetUrl);
      } catch (UnsupportedEncodingException e) {
        log_.warn("Error while decoding target url {}: {}", targetUrl, e.getMessage());
        target = request.getPreviousUrl().getLatestMove();
      }
    } else if (action.getConfiguration().getTargetUrl() != null) {
      target = new WebUrlImpl(request.getSite(), action.getConfiguration().getTargetUrl());
      targetForced = true;
    } else if (request.getPreviousUrl().size() > 0) {
      target = request.getPreviousUrl().getLatestMove();
    } else {
      target = request.getSite().getNavigation().getHomeUrl();
    }

    // We are about to render the action output in the composer of the target
    // page. This is why we have to make sure that this target page exists,
    // otherwhise
    // the user will get a 404.
    page = PageManager.getPage(target, user, SystemPermission.READ, request.getVersion());
    if (page == null) {
      WebUrl oldTarget = target;
      target = request.getSite().getNavigation().getHomeUrl();
      page = PageManager.getPage(target, user, SystemPermission.READ, request.getVersion());
      if (targetForced) {
        sitelogger_.warn("Output of action '" + action + "' is configured to go to non existing page " + oldTarget);
      }
    }

    // Inform request about new url
    if (targetForced) {
      RequestSupport.setTargetUrl(target, request);
    }

    return page;
  }

}