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

import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.content.SearchQuery;
import ch.o2it.weblounge.common.content.SearchResult;
import ch.o2it.weblounge.common.content.SearchResultItem;
import ch.o2it.weblounge.common.impl.content.SearchQueryImpl;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;
import ch.o2it.weblounge.contentrepository.ContentRepository;
import ch.o2it.weblounge.contentrepository.ContentRepositoryException;
import ch.o2it.weblounge.contentrepository.ContentRepositoryFactory;
import ch.o2it.weblounge.taglib.WebloungeTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspException;

/**
 * This tag is used to gather a list of pages satisfying certain criteria such
 * as the page type, search keywords etc.
 */

public class PageHeaderListTag extends WebloungeTag {

  /** Serial version UID */
  private static final long serialVersionUID = -1825541321489778143L;

  /** Logging facility */
  private final static Logger logger = LoggerFactory.getLogger(PageHeaderTag.class.getName());

  /** The list of keywords */
  private List<String> subjects = null;

  /** The number of page headers to return */
  private int count = 0;

  /** The page headers */
  private SearchResult pages = null;

  /** The iteration index */
  private int index = -1;

  /** The pagelet from the request */
  private Pagelet pagelet = null;

  /** List of required headlines */
  private Map<String, String> requireHeadlines = null;

  /**
   * Creates a new page header list tag.
   */
  public PageHeaderListTag() {
    requireHeadlines = new HashMap<String, String>();
    subjects = new ArrayList<String>();
    reset();
  }

  /**
   * Sets the number of page headers to load. If this attribute is omitted, then
   * all headers are returned.
   * 
   * @param count
   *          the number of page headers
   */
  public void setCount(String count) {
    try {
      this.count = Integer.parseInt(count);
    } catch (NumberFormatException e) {
      this.count = Integer.MAX_VALUE;
    }
  }

  /**
   * Sets the list of page keywords to look up. The keywords must consist of a
   * list of strings, separated by either ",", ";" or " ".
   * 
   * @param value
   *          the keywords
   */
  public void setKeywords(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",;");
    while (tok.hasMoreTokens()) {
      subjects.add(tok.nextToken().trim());
    }
  }

  /**
   * Indicates the required headlines.
   * 
   * @param value
   *          the headlines
   */
  public void setRequireheadlines(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",;");
    while (tok.hasMoreTokens()) {
      String headline = tok.nextToken().trim();
      String[] parts = headline.split("/");
      if (parts.length != 2)
        throw new IllegalArgumentException("Required headlines '" + value + "' are malformed. Required is 'module1/pagelet1, module2/pagelet2, ...");
      requireHeadlines.put(parts[0], parts[1]);
    }
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    index = 0;
    pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);
    try {
      return (loadNextHeadline()) ? EVAL_BODY_INCLUDE : SKIP_BODY;
    } catch (ContentRepositoryException e) {
      throw new JspException(e);
    }
  }

  /**
   * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
   */
  public int doAfterBody() throws JspException {
    index++;
    try {
      return (loadNextHeadline()) ? EVAL_BODY_AGAIN : SKIP_BODY;
    } catch (ContentRepositoryException e) {
      throw new JspException(e);
    }
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(PageHeaderListTagVariables.URL);
    pageContext.removeAttribute(PageHeaderListTagVariables.PREVIEW);
    request.setAttribute(WebloungeRequest.PAGELET, pagelet);
    reset();
    return super.doEndTag();
  }

  /**
   * Loads the next headline, puts it into the request and returns
   * <code>true</code> if a suitable headline was found, false otherwise.
   * 
   * @return <code>true</code> if a headline was found
   * @throws ContentRepositoryException
   *           if loading the pages fails
   */
  private boolean loadNextHeadline() throws ContentRepositoryException {
    Site site = request.getSite();

    // Check if headers have already been loaded
    if (pages == null) {
      ContentRepository repository = ContentRepositoryFactory.getRepository(site);
      if (repository == null) {
        logger.warn("Unable to load content repository for site '{}'", site);
        return false;
      }

      // Load the pages
      SearchQuery query = new SearchQueryImpl(site);
      for (String subject : subjects)
        query.withSubject(subject);
      pages = repository.findPages(query);
    }

    // Look for the next header
    boolean found = false;
    SearchResultItem item = null;
    while (index < pages.getItems().length && !found) {
      found = true;
      item = pages.getItems()[index];
      for (Map.Entry<String, String> h : requireHeadlines.entrySet()) {
        // TODO: Add test for headline element availability
        if (found && item.getPreview() == null) {
          found = false;
          index++;
          break;
        }
      }
    }

    // Set the headline in the request
    if (found) {
      WebUrl url = item.getUrl();
      // TODO: How do we get the PREVIEW_XML out of the search result?
      // Pagelet[] preview = item.getPreview();
      Pagelet[] preview = new Pagelet[] {};
      pageContext.setAttribute(PageHeaderListTagVariables.URL, url);
      pageContext.setAttribute(PageHeaderListTagVariables.PREVIEW, preview);
    }

    return found;
  }

  /**
   * Method called when the tag is released to the pool.
   * 
   * @see javax.servlet.jsp.tagext.Tag#release()
   */
  public void release() {
    reset();
    super.release();
  }

  /**
   * Initializes and resets this tag instance.
   */
  protected void reset() {
    super.reset();
    pages = null;
    subjects.clear();
    requireHeadlines.clear();
    index = 0;
    count = 10;
  }

}