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

package ch.entwine.weblounge.taglib.resource;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchQuery.Order;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;
import ch.entwine.weblounge.common.repository.ContentRepository;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.taglib.WebloungeTag;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;

public class ResourceIteratorTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = 3626449066999007138L;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ResourceIteratorTag.class);

  /** The subjects */
  private List<String> resourceSubjects = null;

  /** The series */
  private List<String> resourceSeries = null;

  /** The types to include */
  private List<String> includeTypes = null;

  /** The types to exclude */
  private List<String> excludeTypes = null;

  /** The resource id */
  private List<String> resourceId = null;

  /** The minimum creation start date */
  private Date creatorStartDate = null;

  /** The maximum creation start date */
  private Date creatorEndDate = null;

  /** The iteration index */
  protected int index = 0;

  /** The number of iterations */
  protected long iterations = -1;

  /** The search result */
  private SearchResult searchResult = null;

  /** The content repository */
  private ContentRepository repository = null;

  /** The result order */
  private Order order = null;

  /** The maximum number of resources to return */
  private int limit = 10;

  /** Index of the first document to return */
  private int offset = 0;

  /**
   * Sets the subjects for the search.
   * 
   * @param subjects
   *          the subjects to search
   */
  public void setSubjects(String subjects) {
    if (resourceSubjects == null)
      resourceSubjects = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(subjects, ",;");
    while (st.hasMoreTokens()) {
      resourceSubjects.add(st.nextToken());
    }
  }

  /**
   * Sets the resource identifier for the search.
   * 
   * @param id
   *          the resource identifier to search
   */
  public void setUuid(String id) {
    if (StringUtils.isBlank(id))
      return;
    if (resourceId == null)
      resourceId = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(id, ",;");
    while (st.hasMoreTokens()) {
      resourceId.add(st.nextToken());
    }
  }

  /**
   * Sets the series for the search.
   * 
   * @param series
   *          the series to search
   */
  public void setSeries(String series) {
    if (resourceSeries == null)
      resourceSeries = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(series, ",;");
    while (st.hasMoreTokens()) {
      resourceSeries.add(st.nextToken());
    }
  }

  /**
   * Sets the types to search for.
   * 
   * @param types
   *          the types
   */
  public void setIncludetypes(String types) {
    if (includeTypes == null)
      includeTypes = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(types, ",;");
    while (st.hasMoreTokens()) {
      includeTypes.add(st.nextToken());
    }
  }

  /**
   * Sets the types to exclude in the serach.
   * 
   * @param types
   *          the types to exlude
   */
  public void setExcludetypes(String types) {
    if (excludeTypes == null)
      excludeTypes = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(types, ",;");
    while (st.hasMoreTokens()) {
      excludeTypes.add(st.nextToken());
    }
  }

  /**
   * Set the minimum creation date to search from.
   * <p>
   * If the start date is omitted but the end date is given, the earliest date
   * possible <code>new Date(0)</code> is assumed.
   * 
   * @param startDate
   *          the creator date
   */
  public void setStartdate(String startDate) {
    try {
      creatorStartDate = WebloungeDateFormat.parseStatic(startDate);
    } catch (ParseException e) {
      logger.debug("Unable to parse start date '{}'", startDate);
    }
  }

  /**
   * Set the maximum creation date to search to.
   * <p>
   * If the end date is omitted but the start date is given, the current date
   * possible <code>new Date()</code> is assumed.
   * 
   * @param endDate
   *          the creator date
   */
  public void setEnddate(String endDate) {
    try {
      creatorEndDate = WebloungeDateFormat.parseStatic(endDate);
    } catch (ParseException e) {
      logger.debug("Unable to parse end date '{}'", endDate);
    }
  }

  /**
   * Set the sort order for the search
   * 
   * @param order
   *          the sort order
   */
  public void setOrder(String order) {
    try {
      this.order = Order.valueOf(order);
    } catch (Exception e) {
      logger.debug("Unable to parse order '{}'", order);
    }
  }

  /**
   * Sets the maximum number of resources to iterate over.
   * 
   * @param limit
   *          the maximum number of resources
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  /**
   * Sets the index of the first document to return.
   * 
   * @param offset
   *          index of the first document to return
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {
    Site site = request.getSite();

    repository = site.getContentRepository();
    if (repository == null) {
      logger.debug("Unable to load content repository for site '{}'", site);
      response.invalidate();
      return SKIP_BODY;
    }

    // First time search resources
    if (searchResult == null) {
      SearchQuery q = new SearchQueryImpl(site);
      if (includeTypes != null)
        q.withTypes(includeTypes.toArray(new String[includeTypes.size()]));

      if (excludeTypes != null)
        q.withoutTypes(excludeTypes.toArray(new String[excludeTypes.size()]));

      if (order != null)
        q.sortByCreationDate(order);

      if (resourceId != null) {
        for (String id : resourceId)
          q.withIdentifier(id);
      } else {
        if (resourceSubjects != null) {
          for (String subject : resourceSubjects) {
            q.withSubject(subject);
          }
        }
        if (resourceSeries != null) {
          for (String series : resourceSeries) {
            q.withSeries(series);
          }
        }
        if (creatorStartDate != null || creatorEndDate != null) {
          if (creatorStartDate == null)
            creatorStartDate = new Date(0);
          if (creatorEndDate == null)
            creatorEndDate = new Date();
          q.withCreationDateBetween(creatorStartDate);
          q.and(creatorEndDate);
        }
      }

      q.withLimit(limit);
      q.withOffset(offset);

      try {
        searchResult = repository.find(q);
      } catch (ContentRepositoryException e) {
        logger.error("Error searching for resources with given subjects.");
        return SKIP_BODY;
      }
      index = 0;
      iterations = searchResult.getDocumentCount();
    }

    if (iterations < 1)
      return SKIP_BODY;

    stashAndSetAttribute(ResourceIteratorTagExtraInfo.INDEX, index);
    stashAndSetAttribute(ResourceIteratorTagExtraInfo.ITERATIONS, iterations);

    ResourceSearchResultItem searchResultItem = (ResourceSearchResultItem) searchResult.getItems()[index];
    return setResource(searchResultItem, EVAL_BODY_INCLUDE);
  }

  /**
   * Get the search result item from the repository and set it to the page
   * context.
   * 
   * @param searchResultItem
   *          the search result item
   * @param resultCode
   *          the success code
   * @return the <code>resultCode</code> if success else <code>SKIP_BODY</code>
   */
  private int setResource(ResourceSearchResultItem searchResultItem,
      int resultCode) {
    ResourceURI uri = searchResultItem.getResourceURI();

    // Try to load the resource from the content repository
    try {
      if (!repository.exists(uri)) {
        logger.warn("Non existing resource {} requested on {}", uri, request.getUrl());
        return SKIP_BODY;
      }
    } catch (ContentRepositoryException e) {
      logger.error("Error trying to look up resource {} from {}", searchResultItem.getId(), repository);
      return SKIP_BODY;
    }

    Resource<?> resource = null;
    ResourceContent resourceContent = null;

    try {
      resource = repository.get(uri);
      resource.switchTo(request.getLanguage());
      resourceContent = resource.getContent(request.getLanguage());
      if (resourceContent == null)
        resourceContent = resource.getOriginalContent();
    } catch (ContentRepositoryException e) {
      logger.warn("Error trying to load resource " + uri + ": " + e.getMessage(), e);
      return SKIP_BODY;
    }

    // TODO: Check the permissions

    // Store the resource and the resource content in the request
    stashAndSetAttribute(ResourceIteratorTagExtraInfo.RESOURCE, resource);
    stashAndSetAttribute(ResourceIteratorTagExtraInfo.RESOURCE_CONTENT, resourceContent);

    // Add cache tags to the response
    response.addTag(CacheTag.Resource, resource.getURI().getIdentifier());
    response.addTag(CacheTag.Url, resource.getURI().getPath());

    return resultCode;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
   */
  @Override
  public int doAfterBody() throws JspException {
    index++;
    if (index >= iterations)
      return SKIP_BODY;

    pageContext.setAttribute(ResourceIteratorTagExtraInfo.INDEX, index);

    ResourceSearchResultItem searchResultItem = (ResourceSearchResultItem) searchResult.getItems()[index];

    return setResource(searchResultItem, EVAL_BODY_AGAIN);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#doEndTag()
   */
  @Override
  public int doEndTag() throws JspException {
    removeAndUnstashAttributes();
    return super.doEndTag();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.taglib.WebloungeTag#reset()
   */
  @Override
  protected void reset() {
    super.reset();
    index = 0;
    iterations = -1;
    searchResult = null;
    resourceSubjects = null;
    repository = null;
    creatorStartDate = null;
    creatorEndDate = null;
    resourceId = null;
    order = null;
    resourceSeries = null;
    excludeTypes = null;
    includeTypes = null;
    limit = 10;
    offset = 0;
  }

}
