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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.content.PublishingContext;
import ch.o2it.weblounge.common.impl.language.LocalizableObject;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.page.SearchResult;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityContext;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * Search result implementation.
 * 
 * TODO: Add language sensitivity
 */
public class SearchResultImpl extends LocalizableObject implements SearchResult {

  /** The title */
  protected String title = null;

  /** The preview */
  protected String preview = null;

  /** The hit location */
  protected PageURI uri = null;

  /** The content type */
  protected String contentType = "text/html";

  /** The renderer used to show the preview */
  protected Renderer previewRenderer = null;

  /** Source of the search result */
  protected Object source = null;

  /** Relevance of this search result with respect to the search terms */
  float relevance = 0;

  /** The security context of this search result */
  protected SecurityContext securityContext = null;

  /** The publishing context of this search result */
  protected PublishingContext publishingContext = null;

  /**
   * Creates a new search result with the given title and url. Mimetype and
   * renderer will be set to the default values.
   * 
   * @param title
   *          the result's title
   * @param uri
   *          the url to show the hit
   * @param source
   *          the source of this search result
   */
  public SearchResultImpl(String title, String preview, PageURI uri,
      Object source) {
    this.title = title;
    this.preview = preview;
    this.uri = uri;
    this.source = source;
  }

  /**
   * Returns the title for this search result.
   * 
   * @see ch.o2it.weblounge.api.content.SearchResult#getTitle()
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the search result's content type. By default, this will be
   * <code>text/html</code>.
   * 
   * @see ch.o2it.weblounge.api.content.SearchResult#getContentType()
   * @return the content type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * @see ch.o2it.weblounge.api.content.SearchResult#getUrl()
   */
  public PageURI getURI() {
    return uri;
  }

  /**
   * @see ch.o2it.weblounge.api.content.SearchResult#getPreview()
   */
  public String getPreview() {
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
   * @see ch.o2it.weblounge.api.content.SearchResult#getPreviewRenderer()
   */
  public Renderer getPreviewRenderer() {
    return previewRenderer;
  }

  /**
   * @see ch.o2it.weblounge.api.content.SearchResult#getRelevance()
   */
  public float getRelevance() {
    return relevance;
  }

  /**
   * Returns the search result's source.
   * 
   * @see ch.o2it.weblounge.api.content.SearchResult#getSource()
   */
  public Object getSource() {
    return source;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(SearchResult sr) {
    if (relevance < sr.getRelevance())
      return 1;
    else if (relevance > sr.getRelevance())
      return -1;
    else
      return getTitle().compareTo(sr.getTitle());
  }

  /**
   * Sets the security context for this search result.
   * 
   * @param ctxt
   *          the context
   */
  public void setSecurityContext(SecurityContext ctxt) {
    securityContext = ctxt;
  }

  /**
   * @see ch.o2it.weblounge.api.security.Secured#getSecurityContext()
   */
  public SecurityContext getSecurityContext() {
    return securityContext;
  }

  /**
   * @see ch.o2it.weblounge.api.security.Secured#check(ch.o2it.weblounge.api.security.Permission,
   *      ch.o2it.weblounge.common.user.api.security.User)
   */
  public boolean check(Permission p, Authority a) {
    return check(p, a);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.PermissionSet,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(PermissionSet p, Authority a) {
    return securityContext.check(p, a);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#checkOne(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    if (securityContext == null)
      return true;
    return securityContext.checkOne(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#checkAll(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    if (securityContext == null)
      return true;
    return securityContext.checkAll(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#permissions()
   */
  public Permission[] permissions() {
    if (securityContext == null)
      return new Permission[] {};
    return securityContext.permissions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (securityContext == null)
      return null;
    return securityContext.getOwner();
  }

  /**
   * @see ch.o2it.weblounge.api.security.Secured#addSecurityListener(ch.o2it.weblounge.api.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    if (securityContext != null)
      securityContext.addSecurityListener(listener);
  }

  /**
   * @see ch.o2it.weblounge.api.security.Secured#removeSecurityListener(ch.o2it.weblounge.api.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (securityContext != null)
      securityContext.removeSecurityListener(listener);
  }

  /**
   * Sets the publishing context for this search result.
   * 
   * @param ctxt
   *          the context
   */
  public void setPublishingContext(PublishingContext ctxt) {
    publishingContext = ctxt;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    if (publishingContext == null)
      return false;
    return publishingContext.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    if (publishingContext == null)
      return false;
    return publishingContext.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    if (publishingContext == null)
      return null;
    return publishingContext.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    if (publishingContext == null)
      return null;
    return publishingContext.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    if (publishingContext == null)
      return null;
    return publishingContext.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable, ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (o instanceof SearchResult) {
      SearchResult r = (SearchResult)o;
      if (relevance > r.getRelevance())
        return 1;
      else if (relevance < r.getRelevance())
        return -1;
      else {
        // TODO: Return newest entry?
        return 0;
      }
    }
    return 0;
  }

}