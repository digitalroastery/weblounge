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
import ch.o2it.weblounge.common.content.Renderer;
import ch.o2it.weblounge.common.content.SearchResultItem;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default implementation of a {@link SearchResultItem}.
 */
public class SearchResultItemImpl implements SearchResultItem {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(SearchResultImpl.class);
  
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

  /** The page */
  protected Page page = null;

  /** Source of the search result */
  protected Object source = null;

  /** Score within the search result */
  protected double score = 0.0d;

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

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.SearchResultItem#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();

    buf.append("<result ");
    buf.append("relevance=\"").append(getRelevance()).append("\"");
    buf.append(">");

    // Query and execution time
    buf.append("<id>").append(getId()).append("</id>");
    buf.append("<url>").append(getUrl()).append("</url>");
    if (getTitle() != null)
      buf.append("<title>").append(getTitle()).append("</title>");
    if (getSource() != null)
      buf.append("<source>").append(getSource()).append("</source>");
    if (getPreviewRenderer() != null)
      buf.append("<renderer>").append(getPreviewRenderer().toXml()).append("</renderer>");
    if (getPreview() != null) {
      Object preview = getPreview();
      try {
        Class<?> previewClass = preview.getClass();
        Method toXmlMethod = previewClass.getMethod("toXml", new Class<?>[] {});
        if (toXmlMethod != null) {
          Object xml = toXmlMethod.invoke(preview, new Object[] {});
          if (xml != null) {
            buf.append("<preview>").append(xml).append("</preview>");
          }
        } else {
          buf.append("<preview>").append(preview.toString()).append("</preview>");
        }
      } catch (NoSuchMethodException e) {
        buf.append("<preview>").append(preview.toString()).append("</preview>");
      } catch (IllegalArgumentException e) {
        logger.error("Parameter error while trying to invoke toXml() on " + preview, e);
      } catch (IllegalAccessException e) {
        logger.error("Access denied while trying to invoke toXml() on " + preview, e);
      } catch (InvocationTargetException e) {
        logger.error("Error trying to invoke toXml() on " + preview, e);
      }
    }
    
    buf.append("</result>");
    return buf.toString();
  }

}