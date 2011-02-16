/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher.impl.handler;

import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchQuery.Order;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.content.SearchResultItem;
import ch.o2it.weblounge.common.content.SearchResultPageItem;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.repository.ContentRepository;
import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.dispatcher.RequestHandler;
import ch.o2it.weblounge.dispatcher.impl.DispatchUtils;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The feed request handler will answer requests that are looking for
 * <code>rss</code> or <code>atom</code> feeds for a certain site. The request
 * is expected to provide feed type and version as the first two parameters on
 * the request path, e. g.
 * 
 * <pre>
 *  http://localhost:8080/weblounge-feeds/atom/0.3
 * </pre>
 * 
 * <p>
 * The request implementation can handle a parameter that specifies one or more
 * subjects that may appear in the pages that make up the feed entries. Like
 * this, different feeds can be created.
 * 
 * <pre>
 *  http://localhost:8080/weblounge-feeds/atom/0.3?subject=a,b,c
 * </pre>
 * 
 * </p>
 */
public class FeedRequestHandlerImpl implements RequestHandler {

  /** The subjects parameter name */
  public static final String PARAM_SUBJECT = "subject";

  /** The limit parameter name */
  public static final String PARAM_LIMIT = "limit";

  /** Default value for the <code>limit</code> parameter */
  public static final int DEFAULT_LIMIT = 10;

  /** Alternate uri prefix */
  protected static final String URI_PREFIX = "/weblounge-feeds/";

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(FeedRequestHandlerImpl.class);
  
  /**
   * Handles the request for a feed of a certain type.
   * <p>
   * This method returns <code>true</code> if the handler is decided to handle
   * the request, <code>false</code> otherwise.
   * 
   * @param request
   *          the weblounge request
   * @param response
   *          the weblounge response
   */
  public boolean service(WebloungeRequest request, WebloungeResponse response) {

    Site site = request.getSite();
    String path = request.getRequestURI();
    String feedType = null;

    // Currently, we only support feeds mapped to our well-known uri
    if (!path.startsWith(URI_PREFIX) || !(path.length() > URI_PREFIX.length()))
      return false;

    // Check the request method. Only GET is supported right now.
    String requestMethod = request.getMethod();
    if (!"GET".equals(requestMethod)) {
      logger.debug("Feed request handler does not support {} requests", requestMethod);
      return false;
    }

    // Get hold of the content repository
    ContentRepository contentRepository = site.getContentRepository();
    if (contentRepository == null) {
      logger.warn("No content repository found for site '{}'", site);
      return false;
    }

    // TODO: Add check for if-modified-since
    
    // Check for feed type and version
    String feedURI = path.substring(URI_PREFIX.length());
    String[] feedURIParts = feedURI.split("/");
    if (feedURIParts.length == 0) {
      logger.debug("Feed request {} does not include feed type", path);
      return false;
    } else if (feedURIParts.length == 1) {
      logger.debug("Feed request {} does not include feed version", path);
      return false;
    }

    // Extract the subjects. The parameter may be specified multiple times
    // and add more than one subject by separating them using a comma.
    String[] subjectParameter = request.getParameterValues(PARAM_SUBJECT);
    List<String> subjects = new ArrayList<String>();
    if (subjectParameter != null) {
      for (String parameter : subjectParameter) {
        for (String subject : parameter.split(",")) {
          if (StringUtils.isNotBlank(subject))
            subjects.add(StringUtils.trim(subject));
        }
      }
    }
    
    // How many entries do we need?
    int limit = DEFAULT_LIMIT;
    String limitParameter = StringUtils.trimToNull(request.getParameter(PARAM_LIMIT));
    if (limitParameter != null) {
      try {
        limit = Integer.parseInt(limitParameter);
      } catch (Throwable t) {
        logger.debug("Non parseable number {} specified as limit", limitParameter);
        limit = DEFAULT_LIMIT;
      }
    }

    // User and language
    Language language = request.getLanguage();
    //User user = request.getUser();

    // Determine the feed type
    feedType = feedURIParts[0].toLowerCase() + "_" + feedURIParts[1];
    SyndFeed feed = new SyndFeedImpl();
    feed.setFeedType(feedType);
    feed.setLink(request.getRequestURL().toString());
    feed.setTitle(site.getName());
    feed.setLanguage(language.getDescription());

    // TODO: Add more feed metadata, ask site

    // Find the pages that will form the feed
    try {
      SearchQuery query = new SearchQueryImpl(site);
      query.withType(Page.TYPE);
      query.withLimit(limit);
      query.sortByModificationDate(Order.Descending);
      for (String subject : subjects) {
        query.withSubject(subject);
      }
      
      // Load the result and add feed entries
      SearchResult result = contentRepository.find(query);
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      for (SearchResultItem item : result.getItems()) {
        if (limit == 0)
          break;
        
        // Get the page
        SearchResultPageItem pageItem = (SearchResultPageItem)item;
        Page page = pageItem.getPage();
        page.switchTo(language);

        // Create the entry
        SyndEntry entry = new SyndEntryImpl();
        entry.setPublishedDate(page.getPublishFrom());
        entry.setLink(item.getUrl().getLink());
        entry.setAuthor(page.getCreator().getName());
        entry.setCategories(Arrays.asList(page.getSubjects()));
        entry.setTitle(item.getTitle());

        // TODO: Can the page be accessed?

        // Contents
        // TODO: Try to render the preview pagelets and write them to the feed
        //entry.setContents(Arrays.asList(item.getPreview().toString()));

        entries.add(entry);
        limit--;
      }
      
      feed.setEntries(entries);
    } catch (ContentRepositoryException e) {
      logger.error("Error loading articles for feeds from {}: {}", contentRepository, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    }

    // Set the response type
    if ("atom".equalsIgnoreCase("atom"))
      response.setContentType("application/atom+xml");
    else if ("rss".equalsIgnoreCase(feedType))
      response.setContentType("application/rss+xml");
    
    // Set the character encoding
    response.setCharacterEncoding(feed.getEncoding());
    

    // Write the feed back to the response
    try {

      SyndFeedOutput output = new SyndFeedOutput();
      Writer responseWriter = new OutputStreamWriter(response.getOutputStream());
      output.output(feed, responseWriter);
      response.getOutputStream().flush();
      return true;
    } catch (FeedException e) {
      logger.error("Error creating {} feed: {}", feedType, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    } catch (IOException e) {
      logger.error("Error sending {} feed to the client: {}", feedType, e.getMessage());
      DispatchUtils.sendInternalError(request, response);
      return true;
    }
  }

  /**
   * Adds the image as a content element to the feed entry.
   * 
   * @param entry
   *          the feed entry
   * @param imageUrl
   *          the image url
   * @return the image
   */
  protected Content setImage(String imageUrl) {
    StringBuffer buf = new StringBuffer("<div xmlns=\"http://www.w3.org/1999/xhtml\">");
    buf.append("<img src=\"");
    buf.append(imageUrl);
    buf.append("\" />");
    buf.append("</div>");
    Content image = new Content();
    image.setType("application/xhtml+xml");
    image.setValue(buf.toString());
    return image;
  }

  /**
   * @see ch.o2it.weblounge.dispatcher.api.request.RequestHandler#getName()
   */
  public String getName() {
    return "feed request handler";
  }

  /**
   * Returns a string representation of this request handler.
   * 
   * @return the handler name
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

}
