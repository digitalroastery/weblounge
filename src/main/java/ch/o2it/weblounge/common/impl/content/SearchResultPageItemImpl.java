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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.Page;
import ch.o2it.weblounge.common.content.PageURI;
import ch.o2it.weblounge.common.content.SearchResultItem;
import ch.o2it.weblounge.common.impl.page.LazyPageImpl;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * Default implementation of a {@link SearchResultItem}.
 */
public class SearchResultPageItemImpl extends SearchResultItemImpl {

  /** The page xml */
  protected String pageXml = null;

  /** The page header xml */
  protected String headerXml = null;

  /** The page preview xml */
  protected String previewXml = null;

  /**
   * Creates a new search result item with the given uri. The
   * <code>source</code> is the object that created the item, usually, this will
   * be the site itself but it could very well be a module that added to a
   * search result.
   * 
   * @param site
   *          the site
   * @param id
   *          the document id
   * @param url
   *          the url to show the hit
   * @param relevance
   *          the score inside the search result
   * @param source
   *          the object that produced the result item
   */
  public SearchResultPageItemImpl(Site site, String id, WebUrl url,
      double relevance, Object source) {
    super(site, id, url, relevance, source);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResultItem#getPageURI()
   */
  public PageURI getPageURI() {
    return new PageURIImpl(site, url.getPath(), id);
  }

  /**
   * Sets the page xml.
   * 
   * @param xml
   *          the xml
   */
  public void setPageXml(String xml) {
    this.pageXml = xml;
  }

  /**
   * Sets the page header xml.
   * 
   * @param xml
   *          the xml
   */
  public void setPageHeaderXml(String xml) {
    this.headerXml = xml;
  }

  /**
   * Sets the page header xml.
   * 
   * @param xml
   *          the xml
   */
  public void setPagePreviewXml(String xml) {
    this.previewXml = xml;
  }

  /**
   * Returns the page object.
   * 
   * @return the page
   */
  public Page getPage() {
    if (page == null) {
      PageURI uri = new PageURIImpl(site, url.getPath(), id);
      page = new LazyPageImpl(uri, pageXml, headerXml, previewXml);
    }
    return page;
  }

}