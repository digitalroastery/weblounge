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
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.util.WebloungeDateFormat;
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

  /** The resource id */
  private String resourceId = null;

  /** The minimum creation start date */
  private Date creatorStartDate = null;

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
   *          the resource identifier to serach
   */
  public void setUuid(String id) {
    resourceId = id;
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
   * Set the minimum creation date to search from
   * 
   * @param startDate
   *          the creator date
   */
  public void setStartdate(String startDate) {
    try {
      creatorStartDate = WebloungeDateFormat.parseStatic(startDate);
    } catch (ParseException e) {
      logger.debug("Unable to parse date '{}'", startDate);
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

    // First time serach resources
    if (searchResult == null) {
      SearchQuery q = new SearchQueryImpl(site);
      q.withVersion(Resource.LIVE);

      if (order != null)
        q.sortByCreationDate(order);

      if (StringUtils.isNotBlank(resourceId)) {
        q.withIdentifier(resourceId);
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
        if (creatorStartDate != null)
          q.withCreationDateBetween(creatorStartDate);
      }

      try {
        searchResult = repository.find(q);
      } catch (ContentRepositoryException e) {
        logger.error("Error searching for resources with given subjects.");
        return SKIP_BODY;
      }
      index = 0;
      iterations = searchResult.getHitCount();
    }

    if (iterations < 1)
      return SKIP_BODY;

    pageContext.setAttribute(ResourceIteratorTagExtraInfo.INDEX, index);
    pageContext.setAttribute(ResourceIteratorTagExtraInfo.ITERATIONS, iterations);

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
    pageContext.setAttribute(ResourceIteratorTagExtraInfo.RESOURCE, resource);
    pageContext.setAttribute(ResourceIteratorTagExtraInfo.RESOURCE_CONTENT, resourceContent);

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
    pageContext.removeAttribute(ResourceIteratorTagExtraInfo.INDEX);
    pageContext.removeAttribute(ResourceIteratorTagExtraInfo.ITERATIONS);
    pageContext.removeAttribute(ResourceIteratorTagExtraInfo.RESOURCE);
    pageContext.removeAttribute(ResourceIteratorTagExtraInfo.RESOURCE_CONTENT);
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
    resourceId = null;
    order = null;
    resourceSeries = null;
  }

}
