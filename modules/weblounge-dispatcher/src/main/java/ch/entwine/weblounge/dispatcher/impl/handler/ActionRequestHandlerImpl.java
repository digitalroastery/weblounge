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

package ch.entwine.weblounge.dispatcher.impl.handler;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.page.ComposerImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.request.CacheTagSet;
import ch.entwine.weblounge.common.impl.request.Http11Constants;
import ch.entwine.weblounge.common.impl.request.Http11Utils;
import ch.entwine.weblounge.common.impl.site.ActionPool;
import ch.entwine.weblounge.common.impl.url.UrlMatcherImpl;
import ch.entwine.weblounge.common.impl.url.WebUrlImpl;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.ResponseCache;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.ActionException;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.common.site.JSONAction;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.site.XMLAction;
import ch.entwine.weblounge.common.url.UrlMatcher;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.dispatcher.ActionRequestHandler;
import ch.entwine.weblounge.dispatcher.impl.DispatchUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

/**
 * This handler can be used to register {@link Action}s. If a request matches
 * the url space of registered action, it will handle the request by forwarding
 * it to the action.
 */
public final class ActionRequestHandlerImpl implements ActionRequestHandler {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ActionRequestHandlerImpl.class);

  /** The registered actions */
  private Map<UrlMatcher, ActionPool> actions = null;

  /** Known urls */
  private Map<String, ActionPool> urlCache = null;

  /**
   * Creates a new action request handler.
   */
  public ActionRequestHandlerImpl() {
    actions = new HashMap<UrlMatcher, ActionPool>();
    urlCache = new HashMap<String, ActionPool>();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.ActionRequestHandler#register(ch.entwine.weblounge.common.site.Action)
   */
  public void register(Action action) {
    if (action == null)
      throw new IllegalArgumentException("Action configuration cannot be null");

    // Create a url matcher
    UrlMatcher matcher = new UrlMatcherImpl(action);
    ActionPool pool = new ActionPool(action);
    StringBuffer registration = new StringBuffer(new WebUrlImpl(action.getSite(), action.getPath()).normalize());

    // Register the action
    synchronized (actions) {
      actions.put(matcher, pool);
    }

    // Cache the action urls
    StringBuffer flavors = new StringBuffer();
    synchronized (urlCache) {
      for (RequestFlavor flavor : action.getFlavors()) {
        WebUrl actionUrl = new WebUrlImpl(action.getSite(), action.getPath(), Resource.LIVE, flavor);
        String normalizedUrl = actionUrl.normalize(false, false, false, true);
        urlCache.put(normalizedUrl, pool);
        if (flavors.length() > 0)
          flavors.append(",");
        flavors.append(flavor.toString().toLowerCase());
        logger.trace("Caching action '{}' for url {}", action, normalizedUrl);
      }
    }

    logger.debug("Action '{}' ({}) registered for site://{}", new Object[] {
        action,
        flavors.toString(),
        registration.toString() });
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.ActionRequestHandler#unregister(ch.entwine.weblounge.common.site.Action)
   */
  public boolean unregister(Action action) {
    ActionPool pool = null;

    // Remove the pool from the actions registry
    synchronized (actions) {
      UrlMatcher matcher = new UrlMatcherImpl(action);
      pool = actions.remove(matcher);
      if (pool == null) {
        logger.warn("Tried to unregister unknown action '{}'", action);
        return false;
      }
    }

    // Remove entries from the url cache
    synchronized (urlCache) {
      Iterator<Entry<String, ActionPool>> cacheIterator = urlCache.entrySet().iterator();
      while (cacheIterator.hasNext()) {
        ActionPool candidate = cacheIterator.next().getValue();
        if (candidate.equals(pool)) {
          logger.trace("Removing '{}' from action url cache", action);
          cacheIterator.remove();
        }
      }
    }

    logger.debug("Unregistering action '{}' from {}", action, new WebUrlImpl(action.getSite(), action.getPath()).normalize());
    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#service(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {
    WebUrl url = request.getUrl();
    RequestFlavor flavor = request.getFlavor();
    Mode processingMode = Mode.Default;

    // Try to get hold of an action pool
    ActionPool pool = null;
    pool = getActionForUrl(url, flavor);
    if (pool == null) {
      logger.debug("No action found to handle {}", url);
      return false;
    }

    // Match! Let's try to get an actual action from that pool
    Action action = null;
    try {
      action = (Action) pool.borrowObject();
    } catch (Throwable t) {
      logger.error("Error getting action from action pool", t);
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Make sure the action is returned to the pool no matter what
    try {

      // Check the request method. We won't handle just everything
      String requestMethod = request.getMethod();
      if (!Http11Utils.checkDefaultMethods(requestMethod, response)) {
        logger.debug("Actions are not supposed to handle {} requests", requestMethod);
        DispatchUtils.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, request, response);
        return true;
      }

      // Check for explicit no cache instructions
      boolean noCache = request.getParameter(ResponseCache.NOCACHE_PARAM) != null;

      // Check if the page is already part of the cache. If so, our task is
      // already done!
      if (!noCache && request.getVersion() == Resource.LIVE) {
        long validTime = action.getValidTime();
        long recheckTime = action.getRecheckTime();

        // Create the set of tags that identify the request output
        CacheTagSet cacheTags = createCacheTags(request, action);

        // Check if the page is already part of the cache
        if (response.startResponse(cacheTags.getTags(), validTime, recheckTime)) {
          logger.debug("Action answered request for {} from cache", request.getUrl());
          return true;
        }

        processingMode = Mode.Cached;
      } else if (Http11Constants.METHOD_HEAD.equals(request.getMethod())) {
        // handle HEAD requests
        Http11Utils.startHeadResponse(response);
        processingMode = Mode.Head;
      } else if (request.getVersion() == Resource.WORK) {
        response.setCacheExpirationTime(0);
      }

      logger.debug("Action {} will handle {}", action, url);

      // Call the service method depending on the flavor
      switch (flavor) {
        case HTML:
          if (!action.supportsFlavor(flavor) && !(action instanceof HTMLAction))
            return false;
          serveHTML(action, request, response);
          break;
        case XML:
          if (!action.supportsFlavor(flavor) && !(action instanceof XMLAction))
            return false;
          serveXML(action, request, response);
          break;
        case JSON:
          if (!action.supportsFlavor(flavor) && !(action instanceof JSONAction))
            return false;
          serveJSON(action, request, response);
          break;
        default:
          if (action.supportsFlavor(RequestFlavor.HTML) || action instanceof HTMLAction)
            serveHTML(action, request, response);
          else if (action.supportsFlavor(RequestFlavor.XML) || action instanceof XMLAction)
            serveXML(action, request, response);
          else if (action.supportsFlavor(RequestFlavor.JSON) || action instanceof JSONAction)
            serveJSON(action, request, response);
          else {
            logger.warn("Unable to serve {}: flavor mismatch");
            DispatchUtils.sendError(HttpServletResponse.SC_NOT_FOUND, request, response);
          }
      }

    } finally {

      // Finish cache handling
      switch (processingMode) {
        case Cached:
          response.endResponse();
          break;
        case Head:
          Http11Utils.endHeadResponse(response);
          break;
        default:
          break;
      }

      // Return the action handler to the pool
      try {
        pool.returnObject(action);
      } catch (Throwable t) {
        logger.error("Error returning action {} to pool: {}", new Object[] {
            action,
            t.getMessage(),
            t });
      }
    }

    return true;
  }

  /**
   * This method has the action serve the <code>HTML</code> flavor.
   * 
   * @param action
   *          the action
   * @param request
   *          the http request
   * @param response
   *          the http response
   */
  private void serveHTML(Action action, WebloungeRequest request,
      WebloungeResponse response) {

    WebUrl url = request.getUrl();
    Site site = request.getSite();

    // Load the target page used to render the action
    Page page = null;
    try {
      page = getTargetPage(action, request);
      request.setAttribute(WebloungeRequest.PAGE, page);
      // TODO: Check access rights with action configuration
    } catch (ContentRepositoryException e) {
      logger.error("Error loading target page for action {} at {}", action, url);
      DispatchUtils.sendInternalError(request, response);
      return;
    }

    // Get hold of the page template
    PageTemplate template = null;
    try {
      template = getTargetTemplate(action, page, request);
      if (template == null)
        template = site.getDefaultTemplate();
    } catch (IllegalStateException e) {
      logger.warn(e.getMessage());
      DispatchUtils.sendInternalError(request, response);
    }

    // Finally, let's get some work done!
    try {
      request.setAttribute(WebloungeRequest.ACTION, action);

      // Prepare the action by storing initial values for page and template
      if (action instanceof HTMLAction) {
        ((HTMLAction) action).setPage(page);
        ((HTMLAction) action).setTemplate(template);
      }

      // Have the action validate the request
      response.setContentType("text/html");
      action.configure(request, response, RequestFlavor.HTML);

      // Store values that may have been updated by the action during
      // configure()
      if (action instanceof HTMLAction) {
        request.setAttribute(WebloungeRequest.PAGE, ((HTMLAction) action).getPage());
        request.setAttribute(WebloungeRequest.TEMPLATE, ((HTMLAction) action).getTemplate());
      } else {
        request.setAttribute(WebloungeRequest.PAGE, page);
        request.setAttribute(WebloungeRequest.TEMPLATE, template);
      }

      // Have the content delivered
      if (action.startResponse(request, response) == Action.EVAL_REQUEST) {
        if (page != null) {
          logger.trace("Rendering action '{}' on page {}", action, page);
          PageRequestHandlerImpl.getInstance().service(request, response);
        } else {
          logger.trace("Rendering action '{}' on ad-hoc page", action);
          response.getWriter().println("<!DOCTYPE HTML>");
          response.getWriter().println("<html>\n\t<head>");
          if (action instanceof HTMLAction) {
            ((HTMLAction) action).startHeader(request, response);
          }
          response.getWriter().println("\t</head>\n\t<body>");
          if (action instanceof HTMLAction) {
            Composer c = new ComposerImpl("stage");
            ((HTMLAction) action).startStage(request, response, c);
          }
          response.getWriter().print("\n\t</body>\n</html>");
        }
      }
    } catch (EOFException e) {
      logger.debug("Error writing action '{}' back to client: connection closed by client", url);
    } catch (IOException e) {
      logger.error("Error writing action output to client: {}", e.getMessage());
    } catch (ActionException e) {
      logger.error("Error processing action '{}' for {}: {}", new Object[] {
          action,
          request.getUrl(),
          e.getMessage() });
      DispatchUtils.sendError(e.getStatusCode(), request, response);
    } catch (Throwable e) {
      logger.error("Error processing action '{}' for {}", action, request.getUrl());
      logger.error(e.getMessage(), e);
      DispatchUtils.sendInternalError(request, response);
    } finally {
      request.removeAttribute(WebloungeRequest.ACTION);
      request.removeAttribute(WebloungeRequest.PAGE);
    }
  }

  /**
   * This method has the action serve the <code>XML</code> flavor.
   * 
   * @param action
   *          the action
   * @param request
   *          the http request
   * @param response
   *          the http response
   */
  private void serveXML(Action action, WebloungeRequest request,
      WebloungeResponse response) {
    try {
      response.setContentType("text/xml");
      action.configure(request, response, RequestFlavor.XML);
      if (action.startResponse(request, response) == Action.EVAL_REQUEST) {
        if (action instanceof XMLAction) {
          ((XMLAction) action).startXML(request, response);
        }
      }
    } catch (EOFException e) {
      logger.debug("Error writing action '{}' back to client: connection closed by client", request.getUrl());
    } catch (IOException e) {
      logger.debug("Error writing action output to client: {}", e.getMessage());
    } catch (ActionException e) {
      logger.error("Error processing action '{}' for {}: {}", new Object[] {
          action,
          request.getUrl(),
          e.getMessage() });
      DispatchUtils.sendError(e.getStatusCode(), request, response);
    } catch (Throwable e) {
      logger.error("Error processing action '{}' for {}", action, request.getUrl());
      logger.error(e.getMessage(), e);
      DispatchUtils.sendInternalError(request, response);
    }
  }

  /**
   * This method has the action serve the <code>XML</code> flavor.
   * 
   * @param action
   *          the action
   * @param request
   *          the http request
   * @param response
   *          the http response
   */
  private void serveJSON(Action action, WebloungeRequest request,
      WebloungeResponse response) {
    try {
      response.setContentType("text/json");
      action.configure(request, response, RequestFlavor.JSON);
      if (action.startResponse(request, response) == Action.EVAL_REQUEST) {
        if (action instanceof JSONAction) {
          ((JSONAction) action).startJSON(request, response);
        }
      }
    } catch (EOFException e) {
      logger.debug("Error writing action '{}' back to client: connection closed by client", request.getUrl());
    } catch (IOException e) {
      logger.debug("Error writing action output to client: {}", e.getMessage());
    } catch (ActionException e) {
      logger.error("Error processing action '{}' for {}: {}", new Object[] {
          action,
          request.getUrl(),
          e.getMessage() });
      DispatchUtils.sendError(e.getStatusCode(), request, response);
    } catch (Throwable e) {
      logger.error("Error processing action '{}' for {}", action, request.getUrl());
      logger.error(e.getMessage(), e);
      DispatchUtils.sendInternalError(request, response);
    }
  }

  /**
   * Returns the primary set of cache tags for the given request and action.
   * 
   * @param request
   *          the request
   * @param action
   *          the action
   * @return the cache tags
   */
  protected CacheTagSet createCacheTags(WebloungeRequest request, Action action) {
    CacheTagSet cacheTags = new CacheTagSet();
    cacheTags.add(CacheTag.Url, request.getUrl().getPath());
    cacheTags.add(CacheTag.Url, request.getRequestedUrl().getPath());
    cacheTags.add(CacheTag.Language, request.getLanguage().getIdentifier());
    cacheTags.add(CacheTag.User, request.getUser().getLogin());
    cacheTags.add(CacheTag.Module, action.getModule().getIdentifier());
    cacheTags.add(CacheTag.Action, action.getIdentifier());
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
    cacheTags.add(CacheTag.Parameters, Integer.toString(parameterCount));
    return cacheTags;
  }

  /**
   * Returns the template that will be used to handle this request. If a
   * template was specified in the request but cannot be found or used for some
   * reason, an {@link IllegalStateException} is thrown.
   * 
   * @param action
   *          the action
   * @param page
   *          the page
   * @param request
   *          the request
   * 
   * @return the template
   * @throws IllegalStateException
   *           if the template cannot be found
   */
  protected PageTemplate getTargetTemplate(Action action, Page page,
      WebloungeRequest request) throws IllegalStateException {

    Site site = request.getSite();
    PageTemplate template = null;
    String templateId = null;

    // Does the request specify an ad-hoc template?
    if (request.getAttribute(WebloungeRequest.TEMPLATE) != null) {
      templateId = (String) request.getAttribute(WebloungeRequest.TEMPLATE);
      template = site.getTemplate(templateId);
      if (template == null)
        throw new IllegalStateException("Page template '" + templateId + "' specified by request attribute was not found");
    }

    // Does the request specify an ad-hoc template?
    if (template == null && request.getParameter(HTMLAction.TARGET_TEMPLATE) != null) {
      templateId = request.getParameter(HTMLAction.TARGET_TEMPLATE);
      template = site.getTemplate(templateId);
      if (template == null)
        throw new IllegalStateException("Page template '" + templateId + "' specified by request parameter was not found");
    }

    // See if the action handler specifies a template
    if (template == null && action instanceof HTMLAction) {
      HTMLAction htmlAction = (HTMLAction) action;
      template = htmlAction.getTemplate();
      if (template == null)
        logger.debug("Action '{}' did not define a page template", action.getIdentifier());
    }

    // See if the action handler specifies a default template
    if (template == null && action instanceof HTMLAction) {
      HTMLAction htmlAction = (HTMLAction) action;
      template = htmlAction.getDefaultTemplate();
      if (template == null)
        logger.debug("Action '{}' did not define a default page template", action.getIdentifier());
    }

    // By default, the page will have to deliver on the template
    if (template == null && page != null) {
      template = site.getTemplate(page.getTemplate());
      if (template == null)
        throw new IllegalStateException("Page template '" + templateId + "' for page '" + page + "' was not found");
    }

    // Did we end up finding a template?
    if (template == null)
      return null;

    template.setEnvironment(request.getEnvironment());
    return template;
  }

  /**
   * Tries to determine the target page for the action result. The
   * <code>target-page</code> request parameter will be considered as well as
   * the action configuration. In any case, the site's homepage will be the
   * fallback.
   * <p>
   * Should a target page be configured either through the request or through
   * action configuration, and should that url not be present, this method will
   * return <code>null</code>.
   * 
   * @param action
   *          the action
   * @param request
   *          the weblounge request
   * @return the target page
   * @throws ContentRepositoryException
   *           if the target page cannot be loaded
   */
  protected Page getTargetPage(Action action, WebloungeRequest request)
      throws ContentRepositoryException {

    ResourceURI target = null;
    Page page = null;
    Site site = request.getSite();
    boolean targetForced = false;

    // Check if a target-page parameter was passed
    String targetPage = request.getParameter(HTMLAction.TARGET_PAGE);
    if (targetPage != null) {
      targetForced = true;
      try {
        String decocedTargetUrl = null;
        String encoding = request.getCharacterEncoding();
        if (encoding == null)
          encoding = "utf-8";
        decocedTargetUrl = URLDecoder.decode(targetPage, encoding);
        target = new PageURIImpl(site, decocedTargetUrl);
      } catch (UnsupportedEncodingException e) {
        logger.warn("Error while decoding target url {}: {}", targetPage, e.getMessage());
        target = new PageURIImpl(site, "/");
      }
    }

    // Check the action configuration
    else if (action instanceof HTMLAction) {
      HTMLAction htmlAction = (HTMLAction) action;
      if (htmlAction.getPageURI() != null) {
        target = htmlAction.getPageURI();
        targetForced = true;
      }
    }

    // Nothing found, let's choose the site's homepage
    if (target == null) {
      target = new PageURIImpl(site, "/");
    }

    // We are about to render the action output in the composers of the target
    // page. This is why we have to make sure that this target page exists,
    // otherwise the user will get a 404.
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("Content repository not available to read target page for action '{}'", action, target);
      return null;
    }

    // Does the page exist?
    page = (Page) contentRepository.get(target);
    if (page == null) {
      if (targetForced) {
        logger.warn("Output of action '{}' is configured to render on non existing page {}", action, target);
        return null;
      }

      // Fall back to site homepage
      target = new PageURIImpl(site, "/");
      page = (Page) contentRepository.get(target);
      if (page == null) {
        logger.debug("Site {} has no homepage as fallback to render actions", site);
        return null;
      }
    }

    return page;
  }

  /**
   * Returns the action that is registered to serve the given url or
   * <code>null</code> if no such handler exists.
   * 
   * @param url
   *          the url
   * @param flavor
   *          the request flavor
   * @return the handler
   */
  private ActionPool getActionForUrl(WebUrl url, RequestFlavor flavor) {
    String normalizedUrl = url.normalize(false, false, false, true);

    // Try to use the url cache
    ActionPool actionPool = urlCache.get(normalizedUrl);
    if (actionPool != null)
      return actionPool;

    // Nothing is in the cache, let's see if this is simply the first time
    // that this action is being called
    int maxMatchLength = 0;
    for (Entry<UrlMatcher, ActionPool> entry : actions.entrySet()) {
      UrlMatcher matcher = entry.getKey();
      if (matcher.matches(url, flavor)) {
        ActionPool pool = entry.getValue();
        int matchLength = matcher.getMountpoint().length();
        if (matchLength > maxMatchLength) {
          maxMatchLength = matchLength;
          actionPool = pool;
        }
      }
    }

    // Still nothing?
    if (actionPool == null) {
      logger.debug("No action registered to handle {}", url);
      return null;
    }

    // Register the url for future reference
    urlCache.put(normalizedUrl, actionPool);

    // Get an action worker and return it
    // TODO: Instantiate the action

    return actionPool;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getName()
   */
  public String getName() {
    return "action request handler";
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getPriority()
   */
  public int getPriority() {
    return 0;
  }

}