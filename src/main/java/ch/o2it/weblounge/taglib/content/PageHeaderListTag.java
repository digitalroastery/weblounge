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

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.Pagelet;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.user.User;
import ch.o2it.weblounge.taglib.WebloungeTag;

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

  /** The list of keywords */
  private List<String> keywords = null;

  /** The list of page types */
  private List<String> types = null;

  /** The number of page headers to return */
  private int count = 0;

  /** The page headers */
  private Pagelet[] headers = null;

  /** The iteration index */
  private int index = -1;

  /** The pagelet from the request */
  private Pagelet pagelet = null;

  /** List of required headlines */
  private Map<String, String> requireHeadlines = null;

  /** List of loaded headlines */
  private Map<String, String> loadHeadlines = null;

  /**
   * Creates a new page header list tag.
   */
  public PageHeaderListTag() {
    requireHeadlines = new HashMap<String, String>();
    loadHeadlines = new HashMap<String, String>();
    keywords = new ArrayList<String>();
    types = new ArrayList<String>();
    reset();
  }

  /**
   * Sets the number of page headers to load. If this attribute is omitted,
   * then a value of 10 is assumed.
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
    StringTokenizer tok = new StringTokenizer(value, ",; ");
    while (tok.hasMoreTokens()) {
      keywords.add(tok.nextToken().trim());
    }
  }

  /**
   * Sets the list of page types to look up. The keywords must consist of a list
   * of strings, separated by either ",", ";" or " ".
   * 
   * @param value
   *          the types
   */
  public void setTypes(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",; ");
    while (tok.hasMoreTokens()) {
      types.add(tok.nextToken().trim());
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
   * Indicates the headlines that are to be loaded.
   * 
   * @param value
   *          the headlines
   */
  public void setLoadheadlines(String value) {
    StringTokenizer tok = new StringTokenizer(value, ",;");
    while (tok.hasMoreTokens()) {
      String headline = tok.nextToken().trim();
      String[] parts = headline.split("/");
      if (parts.length != 2)
        throw new IllegalArgumentException("Loaded headlines '" + value + "' are malformed. Required is 'module1/pagelet1, module2/pagelet2, ...");
      loadHeadlines.put(parts[0], parts[1]);
    }
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doStartTag()
   */
  public int doStartTag() throws JspException {
    index = 0;
    return (loadNextHeadline()) ? EVAL_BODY_INCLUDE : SKIP_BODY;
  }

  /**
   * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
   */
  public int doAfterBody() throws JspException {
    index++;
    return (loadNextHeadline()) ? EVAL_BODY_AGAIN : SKIP_BODY;
  }

  /**
   * @see javax.servlet.jsp.tagext.Tag#doEndTag()
   */
  public int doEndTag() throws JspException {
    pageContext.removeAttribute(PageHeaderTagVariables.HEADER);
    pageContext.removeAttribute(PageHeaderTagVariables.HEADLINES);
    request.setAttribute(WebloungeRequest.PAGELET, pagelet);
    reset();
    return super.doEndTag();
  }

  /**
   * Loads the next headline, puts it into the request and returns
   * <code>true</code> if a suitable headline was found, false otherwise.
   * 
   * @return <code>true</code> if a headline was found
   */
  private boolean loadNextHeadline() {
    Site site = request.getSite();
    User user = request.getUser();

    // Check if headers have already been loaded
    if (headers == null) {
      headers = null;
      Permission p = SystemPermission.READ;
      long v = Page.LIVE;

      String[] pageTypes = types.toArray(new String[types.size()]);
      String[] pageKeywords = keywords.toArray(new String[keywords.size()]);
      headers = PageHeaderManager.find(pageTypes, pageKeywords, site, user, p, v, count);
    }

    // Look for the next header
    boolean found = false;
    while (index < headers.length && !found) {
      found = true;
      for (Map.Entry<String, String> h : requireHeadlines.entrySet()) {
        if (found && headers[index].getHeadline(h.getKey(), h.getValue(), user) == null) {
          found = false;
          index++;
          break;
        }
      }
    }

    // Set the headline in the request
    if (found) {
      Pagelet[] headlines = headers[index].getHeadlines();
      pageContext.setAttribute(PageHeaderTagVariables.HEADER, headers[index]);
      pageContext.setAttribute(PageHeaderTagVariables.HEADLINES, headlines);
      pagelet = (Pagelet) request.getAttribute(WebloungeRequest.PAGELET);
      request.setAttribute(WebloungeRequest.PAGE, headlines[0]);
      request.setAttribute(PageHeader.HEADLINES, headlines);
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
    headers = null;
    types.clear();
    keywords.clear();
    requireHeadlines.clear();
    loadHeadlines.clear();
    index = 0;
    count = 10;
  }

}
