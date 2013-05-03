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

import static ch.entwine.weblounge.common.request.RequestFlavor.ANY;
import static ch.entwine.weblounge.common.request.RequestFlavor.HTML;

import ch.entwine.weblounge.common.content.Renderer;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceMetadata;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageTemplate;
import ch.entwine.weblounge.common.content.page.PageletRenderer;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.impl.content.page.PageletImpl;
import ch.entwine.weblounge.common.impl.request.CacheTagSet;
import ch.entwine.weblounge.common.impl.request.RequestUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.ResourceSerializer;
import ch.entwine.weblounge.common.repository.ResourceSerializerService;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.HTMLAction;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.WebUrl;
import ch.entwine.weblounge.dispatcher.RequestHandler;
import ch.entwine.weblounge.dispatcher.impl.DispatchUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

/**
 * This handler is answering search requests.
 */
public final class SearchRequestHandlerImpl implements RequestHandler {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SearchRequestHandlerImpl.class);

  /** Alternate uri prefix */
  public static final String URI_PREFIX = "/weblounge-search/";

  /** Query parameter name */
  public static final String PARAM_QUERY = "query";

  /** Page limit parameter name */
  public static final String PARAM_LIMIT = "limit";

  /** Page offset parameter name */
  public static final String PARAM_OFFSET = "offset";

  /** Generic page data */
  public static final String PREVIEW_DATA_KEY = "data";

  /** Key to store the search result in the request */
  public static final String SEARCH_RESULT = "webl-searchresult";

  /** The resource serializer */
  private ResourceSerializerService serializerService = null;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#service(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {
    Site site = request.getSite();
    WebUrl url = request.getUrl();
    RequestFlavor flavor = request.getFlavor();
    String path = url.getPath();

    if (flavor == null || flavor.equals(ANY))
      flavor = RequestFlavor.HTML;

    // Is this request intended for the search handler?
    if (!path.startsWith(URI_PREFIX)) {
      logger.debug("Skipping request for {}, request path does not start with {}", URI_PREFIX);
      return false;
    }

    // Check the request flavor
    if (!HTML.equals(flavor)) {
      logger.debug("Skipping request for {}, flavor {} is not supported", path, request.getFlavor());
      return false;
    }

    // Check the request method. Only GET is supported right now.
    String requestMethod = request.getMethod().toUpperCase();
    if ("OPTIONS".equals(requestMethod)) {
      String verbs = "OPTIONS,GET";
      logger.trace("Answering options request to {} with {}", url, verbs);
      response.setHeader("Allow", verbs);
      response.setContentLength(0);
      return true;
    } else if (!"GET".equals(requestMethod)) {
      logger.debug("Search request handler does not support {} requests", requestMethod);
      DispatchUtils.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, request, response);
      return true;
    }

    int limit = 0;
    int offset = 0;
    String queryString = null;

    // Read the parameters
    try {
      limit = RequestUtils.getIntegerParameterWithDefault(request, PARAM_LIMIT, 0);
      offset = RequestUtils.getIntegerParameterWithDefault(request, PARAM_OFFSET, 0);
      queryString = RequestUtils.getRequiredParameter(request, PARAM_QUERY);
    } catch (IllegalStateException e) {
      logger.debug("Search request handler processing failed: {}", e.getMessage());
      DispatchUtils.sendBadRequest(request, response);
      return true;
    }

    // Load the content repository
    ContentRepository repository = site.getContentRepository();
    if (repository == null) {
      DispatchUtils.sendServiceUnavailable(request, response);
      return true;
    }

    // Create the search expression and the query
    SearchQuery q = new SearchQueryImpl(site);
    q.withFulltext(true, queryString);
    q.withVersion(Resource.LIVE);
    q.withRececyPriority();
    q.withOffset(offset);
    q.withLimit(limit);
    q.withTypes(Page.TYPE, MovieResource.TYPE);

    // Return the result
    SearchResult result = null;
    try {
      result = repository.find(q);
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to access the content repository", e);
      throw new WebApplicationException(e);
    }

    // Load the target page used to render the search result
    Page page = null;
    try {
      page = getTargetPage(request);
      request.setAttribute(WebloungeRequest.PAGE, page);
    } catch (ContentRepositoryException e) {
      logger.error("Error loading target page at {}", url);
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Get hold of the page template
    PageTemplate template = null;
    try {
      template = getTargetTemplate(page, request);
      if (template == null)
        template = site.getDefaultTemplate();
    } catch (IllegalStateException e) {
      logger.warn(e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Identify the stage composer and remove any existing content
    String stage = template.getStage();
    logger.trace("Removing existing pagelets from composer '{}'", stage);
    while (page.getComposer(stage) != null && page.getComposer(stage).size() > 0) {
      page.removePagelet(stage, 0);
    }

    // Add the search result to the main composer
    logger.trace("Adding search result to composer '{}'", stage);
    for (SearchResultItem item : result.getItems()) {

      Renderer renderer = item.getPreviewRenderer();

      // Is this search result coming from the search index or from a module?
      if (!(item instanceof ResourceSearchResultItem)) {
        renderer = item.getPreviewRenderer();
        if (!(renderer instanceof PageletRenderer)) {
          logger.warn("Skipping search result '{}' since it's preview renderer is not a pagelet", item);
          continue;
        }
        PageletImpl pagelet = new PageletImpl((PageletRenderer) renderer);
        pagelet.setContent(item.getContent());
        page.addPagelet(pagelet, stage);
        continue;
      }

      // The search result item seems to be coming from the search index

      // Convert the search result item into a resource search result item
      ResourceSearchResultItem resourceItem = (ResourceSearchResultItem) item;
      ResourceURI uri = resourceItem.getResourceURI();

      ResourceSerializer<?, ?> serializer = serializerService.getSerializerByType(uri.getType());
      if (serializer == null) {
        logger.debug("Skipping search result since it's type ({}) is unknown", uri.getType());
        continue;
      }

      // Load the resource
      Resource<?> resource = serializer.toResource(site, resourceItem.getMetadata());

      // Get the renderer and make sure it's a pagelet renderer. First check
      // the item itself, there may already be a renderer attached. If not,
      // use the serializer to get the appropriate renderer
      renderer = item.getPreviewRenderer();
      if (renderer == null) {
        renderer = serializer.getSearchResultRenderer(resource);
        if (renderer == null) {
          logger.warn("Skipping search result since a renderer can't be determined");
          continue;
        }
      }

      // Create the pagelet
      PageletRenderer pageletRenderer = (PageletRenderer) renderer;
      PageletImpl pagelet = new PageletImpl(pageletRenderer);
      pagelet.setContent(resource);

      // Add the pagelet's data
      for (ResourceMetadata<?> metadata : resourceItem.getMetadata()) {
        String key = metadata.getName();
        if (metadata.isLocalized()) {
          for (Entry<Language, ?> localizedMetadata : metadata.getLocalizedValues().entrySet()) {
            Language language = localizedMetadata.getKey();
            List<Object> values = (List<Object>) localizedMetadata.getValue();
            for (Object value : values) {
              pagelet.setContent(key, value.toString(), language);
            }
          }
        } else {
          for (Object value : metadata.getValues()) {
            pagelet.addProperty(key, value.toString());
          }
        }
      }

      // TODO: Set modified etc.

      // Store the pagelet in the page
      page.addPagelet(pagelet, stage);
    }

    // Search results are not being cached
    // TODO: Implement caching strategy
    response.setCacheExpirationTime(0);
    response.setClientRevalidationTime(0);
    response.setModificationDate(new Date());

    // Finally, let's get some work done!
    try {
      request.setAttribute(WebloungeRequest.PAGE, page);
      request.setAttribute(WebloungeRequest.TEMPLATE, template);
      request.setAttribute(WebloungeRequest.SEARCH, result);

      response.setContentType("text/html");
      logger.trace("Rendering search result on page {}", page);
      PageRequestHandlerImpl.getInstance().service(request, response);
    } catch (Throwable e) {
      logger.error("Error processing search result for {}", request.getUrl());
      logger.error(e.getMessage(), e);
      DispatchUtils.sendInternalError(request, response);
    } finally {
      request.removeAttribute(WebloungeRequest.PAGE);
    }

    return true;
  }

  /**
   * Returns the primary set of cache tags for the given request.
   * 
   * @param request
   *          the request
   * @return the cache tags
   */
  protected CacheTagSet createCacheTags(WebloungeRequest request) {
    CacheTagSet cacheTags = new CacheTagSet();
    cacheTags.add(CacheTag.Url, request.getUrl().getPath());
    cacheTags.add(CacheTag.Url, request.getRequestedUrl().getPath());
    cacheTags.add(CacheTag.Language, request.getLanguage().getIdentifier());
    cacheTags.add(CacheTag.User, request.getUser().getLogin());
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
   * @param page
   *          the page
   * @param request
   *          the request
   * 
   * @return the template
   * @throws IllegalStateException
   *           if the template cannot be found
   */
  protected PageTemplate getTargetTemplate(Page page, WebloungeRequest request)
      throws IllegalStateException {

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

    // Does the request specify a target template?
    if (template == null && request.getParameter(HTMLAction.TARGET_TEMPLATE) != null) {
      templateId = request.getParameter(HTMLAction.TARGET_TEMPLATE);
      template = site.getTemplate(templateId);
      if (template == null)
        throw new IllegalStateException("Page template '" + templateId + "' specified by request parameter was not found");
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
   * Tries to determine the target page for the search result. The
   * <code>target-page</code> request parameter will be considered, and in any
   * case, the site's homepage will be the fallback.
   * <p>
   * Should a target page be configured through the request and should that url
   * not be present, this method will return <code>null</code>.
   * 
   * @param request
   *          the weblounge request
   * @return the target page
   * @throws ContentRepositoryException
   *           if the target page cannot be loaded
   */
  protected Page getTargetPage(WebloungeRequest request)
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

    // Nothing found, let's choose the site's homepage
    if (target == null) {
      target = new PageURIImpl(site, "/");
    }

    // We are about to render the action output in the composers of the target
    // page. This is why we have to make sure that this target page exists,
    // otherwise the user will get a 404.
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("Content repository not available to read target page {}", target);
      return null;
    }

    // Does the page exist?
    page = (Page) contentRepository.get(target);
    if (page == null) {
      if (targetForced) {
        logger.warn("Output of search request is configured to render on non existing page {}", target);
        return null;
      }

      // Fall back to site homepage
      target = new PageURIImpl(site, "/");
      page = (Page) contentRepository.get(target);
      if (page == null) {
        logger.debug("Site {} has no homepage as fallback to render search result", site);
        return null;
      }
    }

    return page;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getPriority()
   */
  public int getPriority() {
    return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.dispatcher.RequestHandler#getName()
   */
  public String getName() {
    return "search request handler";
  }

  /**
   * OSGi callback that is setting the resource serializer.
   * 
   * @param serializer
   *          the resource serializer service
   */
  void setResourceSerializer(ResourceSerializerService serializer) {
    this.serializerService = serializer;
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

}