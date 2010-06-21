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
import ch.o2it.weblounge.common.content.Renderer;
import ch.o2it.weblounge.common.content.SearchResultItem;
import ch.o2it.weblounge.common.impl.page.LazyPageImpl;
import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

/**
 * Default implementation of a {@link SearchResultItem}.
 */
public class SearchResultItemImpl implements SearchResultItem {

  /** THe associated site */
  protected Site site = null;

  /** The page id */
  protected String id = null;

  /** The title */
  protected String title = null;

  /** The preview data */
  protected Object preview = null;

  /** The hit location */
  protected WebUrl url = null;

  /** The renderer used to show the preview */
  protected Renderer previewRenderer = null;

  /** The page xml */
  protected String pageXml = null;

  /** The page */
  protected Page page = null;

  /** Source of the search result */
  protected Object source = null;

  /** Score within the search result */
  protected double score = 0.0d;

  /**
   * Creates a new search result with the given uri. The <code>source</code> is
   * the object that created the item, usually, this will be the site itself but
   * it could very well be a module that added to a search result.
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
  public SearchResultItemImpl(Site site, String id, WebUrl url,
      double relevance, Object source) {
    this.site = site;
    this.id = id;
    this.url = url;
    this.source = source;
    this.score = relevance;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResultItem#getId()
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the search result's title, which is used in place of a missing preview
   * renderer.
   * 
   * @param title
   *          the result item's title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Returns the title for this search result.
   * 
   * @see ch.o2it.weblounge.common.content.SearchResultItem#getTitle()
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the url that points to the location of the search result.
   * 
   * @param url
   *          the target url
   */
  public void setUrl(WebUrl url) {
    if (url == null)
      throw new IllegalArgumentException("The url must not be null");
    this.url = url;
  }

  /**
   * @see ch.o2it.weblounge.common.content.SearchResultItem#getUrl()
   */
  public WebUrl getUrl() {
    return url;
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
   * Sets the result item's preview data.
   * 
   * @param preview
   *          the preview
   */
  public void setPreview(Object preview) {
    this.preview = preview;
  }

  /**
   * @see ch.o2it.weblounge.common.content.SearchResultItem#getPreview()
   */
  public Object getPreview() {
    return preview;
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
   * Returns the page object.
   * 
   * @return the page
   */
  public Page getPage() {
    if (page == null) {
      PageURI uri = new PageURIImpl(site, url.getPath(), id);
      page = new LazyPageImpl(uri, pageXml.getBytes());
    }
    return page;
  }

  /**
   * Sets the preview renderer.
   * 
   * @param r
   *          the renderer
   */
  public void setPreviewRenderer(Renderer r) {
    previewRenderer = r;
  }

  /**
   * @see ch.o2it.weblounge.common.content.SearchResultItem#getPreviewRenderer()
   */
  public Renderer getPreviewRenderer() {
    return previewRenderer;
  }

  /**
   * @see ch.o2it.weblounge.common.content.SearchResultItem#getRelevance()
   */
  public double getRelevance() {
    return score;
  }

  /**
   * Returns the search result's source.
   * 
   * @see ch.o2it.weblounge.common.content.SearchResultItem#getSource()
   */
  public Object getSource() {
    return source;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(SearchResultItem sr) {
    if (score < sr.getRelevance())
      return 1;
    else if (score > sr.getRelevance())
      return -1;
    else if (getTitle() != null && sr.getTitle() != null)
      return getTitle().compareTo(sr.getTitle());
    else
      return 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Double.toString(score).hashCode();
  }

}