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

package ch.entwine.weblounge.common.impl.content.page;

import ch.entwine.weblounge.common.content.PageSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.impl.content.ResourceSearchResultItemImpl;
import ch.entwine.weblounge.common.url.WebUrl;

/**
 * File specific implementation of a
 * {@link ch.entwine.weblounge.common.content.SearchResultItem}.
 */
public class PageSearchResultItemImpl extends ResourceSearchResultItemImpl implements PageSearchResultItem {

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
   * @param uri
   *          the page uri
   * @param url
   *          the url to show the hit
   * @param relevance
   *          the score inside the search result
   * @param source
   *          the object that produced the result item
   */
  public PageSearchResultItemImpl(ResourceURI uri, WebUrl url,
      double relevance, Object source) {
    super(uri, url, relevance, source);
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
   * Returns the xml that makes up the whole page.
   * 
   * @return the page xml
   */
  public String getPageXml() {
    return this.pageXml;
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
   * Returns the xml that makes up the header portion of the page.
   * 
   * @return the page header xml
   */
  public String getPageHeaderXml() {
    return this.headerXml;
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
    if (resource == null) {
      ResourceURI uri = new PageURIImpl(url.getSite(), url.getPath(), id);
      resource = new LazyPageImpl(uri, pageXml, headerXml, previewXml);
    }
    return (Page) resource;
  }

}