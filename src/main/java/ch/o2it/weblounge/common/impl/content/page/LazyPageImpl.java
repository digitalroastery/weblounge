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

package ch.o2it.weblounge.common.impl.content.page;

import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.content.ResourceURI;
import ch.o2it.weblounge.common.content.page.Composer;
import ch.o2it.weblounge.common.content.page.Page;
import ch.o2it.weblounge.common.content.page.PageContentListener;
import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.Localizable;
import ch.o2it.weblounge.common.security.Authority;
import ch.o2it.weblounge.common.security.Permission;
import ch.o2it.weblounge.common.security.PermissionSet;
import ch.o2it.weblounge.common.security.SecurityListener;
import ch.o2it.weblounge.common.user.User;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Set;

/**
 * Implementation of a lazy loading page.
 */
public class LazyPageImpl implements Page {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LazyPageImpl.class);

  /** The page content as a byte array containing the page's xml */
  protected String pageXml = null;

  /** The page content as a byte array containing the page header's xml */
  protected String headerXml = null;

  /** The page content as a byte array containing the page preview's xml */
  protected String previewXml = null;

  /** The backing page */
  protected PageImpl page = null;

  /** The preview composer */
  protected Composer previewComposer = null;

  /** True if the page header was loaded */
  protected boolean isHeaderLoaded = false;

  /** True if the page body was loaded */
  protected boolean isBodyLoaded = false;

  /** The page reader */
  protected WeakReference<PageReader> readerRef = null;

  /** The page uri */
  protected ResourceURI uri = null;

  /**
   * Creates a new lazy page.
   * 
   * @param uri
   *          the page uri
   * @param pageXml
   *          the full page xml
   * @param headerXml
   *          the head section's xml
   * @param previewXml
   *          the page preview's xml
   */
  public LazyPageImpl(ResourceURI uri, String pageXml, String headerXml,
      String previewXml) {
    this.uri = uri;
    this.pageXml = pageXml;
    this.headerXml = headerXml;
    this.previewXml = previewXml;
  }

  /**
   * Loads the complete page.
   */
  protected void loadPage() {
    try {

      // Get a hold of the page reader
      PageReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new PageReader();
        // No need to keep the reference, since we're done after this
      }

      // Load the page
      page = reader.read(IOUtils.toInputStream(pageXml), uri.getSite());
      isHeaderLoaded = true;
      isBodyLoaded = true;
      cleanupAfterLoading();
    } catch (Throwable e) {
      logger.error("Failed to lazy-load body of {}", uri);
      throw new IllegalStateException(e);
    }
  }

  /**
   * Loads the page header only.
   */
  protected void loadPageHeader() {
    try {

      // Get a hold of the page reader
      PageReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new PageReader();
        readerRef = new WeakReference<PageReader>(reader);
      }

      // If no separate header was given, then we need to load the whole thing
      // instead.
      if (headerXml == null) {
        loadPage();
        return;
      }

      // Load the page header
      page = reader.readHeader(IOUtils.toInputStream(headerXml), uri.getSite());
      isHeaderLoaded = true;
      if (isHeaderLoaded && isBodyLoaded)
        cleanupAfterLoading();
      else
        headerXml = null;

    } catch (Throwable e) {
      logger.error("Failed to lazy-load header of {}", uri);
      throw new IllegalStateException(e);
    }
  }

  /**
   * Loads the page body only.
   */
  protected void loadPageBody() {
    try {

      // Get a hold of the page reader
      PageReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new PageReader();
        readerRef = new WeakReference<PageReader>(reader);
      }

      // Load the page body
      page = reader.readBody(IOUtils.toInputStream(pageXml), uri.getSite());
      isBodyLoaded = true;
      if (isHeaderLoaded && isBodyLoaded)
        cleanupAfterLoading();
      else if (headerXml != null)
        pageXml = null;

    } catch (Throwable e) {
      logger.error("Failed to lazy-load body of {}", uri);
      throw new IllegalStateException(e);
    }
  }

  /**
   * Loads the page preview only.
   */
  protected void loadPagePreview() {
    // If no separate preview data was given, then we need to load the whole
    // thing instead.
    if (previewXml == null) {
      loadPageBody();
      return;
    }

    try {
      PagePreviewReader reader = new PagePreviewReader();
      previewComposer = reader.read(IOUtils.toInputStream(previewXml, "UTF-8"), uri);
      previewXml = null;
    } catch (Throwable e) {
      logger.error("Failed to lazy-load preview of {}", uri);
      throw new IllegalStateException(e);
    }
  }

  /**
   * Removes all data that was being held for lazy loading purposes.
   */
  protected void cleanupAfterLoading() {
    headerXml = null;
    pageXml = null;
    previewXml = null;
    previewComposer = null;
    readerRef.clear();
  }

  /**
   * Returns <code>true</code> if the page header has been loaded already.
   * 
   * @return <code>true</code> if the page header has been loaded
   */
  public boolean isHeaderLoaded() {
    return isHeaderLoaded;
  }

  /**
   * Returns <code>true</code> if the page body has been loaded already.
   * 
   * @return <code>true</code> if the page body has been loaded
   */
  public boolean isBodyLoaded() {
    return isBodyLoaded;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#addPageContentListener(ch.o2it.weblounge.common.content.page.PageContentListener)
   */
  public void addPageContentListener(PageContentListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.addPageContentListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#addPagelet(ch.o2it.weblounge.common.content.page.Pagelet,
   *      java.lang.String)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer) {
    if (!isBodyLoaded)
      loadPageBody();
    return page.addPagelet(pagelet, composer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#addPagelet(ch.o2it.weblounge.common.content.page.Pagelet,
   *      java.lang.String, int)
   */
  public Pagelet addPagelet(Pagelet pagelet, String composer, int index)
      throws IndexOutOfBoundsException {
    if (!isBodyLoaded)
      loadPageBody();
    return page.addPagelet(pagelet, composer, index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.addSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getComposers()
   */
  public Composer[] getComposers() {
    if (!isBodyLoaded)
      loadPageBody();
    return page.getComposers();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.page.Page#getComposer(java.lang.String)
   */
  public Composer getComposer(String composerId) {
    if (!isBodyLoaded)
      loadPageBody();
    return page.getComposer(composerId);
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getCoverage()
   */
  public String getCoverage() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getCoverage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getCoverage(ch.o2it.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getCoverage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getCoverage(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getCoverage(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getCoverage(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getDescription()
   */
  public String getDescription() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getDescription();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getDescription(ch.o2it.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getDescription(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getDescription(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getDescription(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getDescription(language, force);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#getIdentifier()
   */
  public String getIdentifier() {
    return uri.getId();
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getLayout()
   */
  public String getLayout() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getLayout();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getLockOwner()
   */
  public User getLockOwner() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getLockOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getPagelets()
   */
  public Pagelet[] getPagelets() {
    if (!isBodyLoaded)
      loadPageBody();
    return page.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getPagelets(java.lang.String)
   */
  public Pagelet[] getPagelets(String composer) {
    if (!isBodyLoaded)
      loadPageBody();
    return page.getPagelets(composer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getPagelets(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public Pagelet[] getPagelets(String composer, String module, String id) {
    if (!isBodyLoaded)
      loadPageBody();
    return page.getPagelets(composer, module, id);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#getPath()
   */
  public String getPath() {
    return uri.getPath();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getPreview()
   */
  public Pagelet[] getPreview() {
    if (previewComposer != null)
      return previewComposer.getPagelets();
    if (!isBodyLoaded)
      loadPagePreview();
    return page.getPreview();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getRights()
   */
  public String getRights() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getRights();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getRights(ch.o2it.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getRights(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getRights(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getRights(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getRights(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getSubjects()
   */
  public String[] getSubjects() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getSubjects();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getTemplate()
   */
  public String getTemplate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getTemplate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getTitle()
   */
  public String getTitle() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getTitle();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getTitle(ch.o2it.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getTitle(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getTitle(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String getTitle(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getTitle(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getType()
   */
  public String getType() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getType();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#getURI()
   */
  public ResourceURI getURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#getVersion()
   */
  public long getVersion() {
    return uri.getVersion();
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#hasSubject(java.lang.String)
   */
  public boolean hasSubject(String subject) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.hasSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#isIndexed()
   */
  public boolean isIndexed() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.isIndexed();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#isLocked()
   */
  public boolean isLocked() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.isLocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#isPromoted()
   */
  public boolean isPromoted() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.isPromoted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#removePageContentListener(ch.o2it.weblounge.common.content.page.PageContentListener)
   */
  public void removePageContentListener(PageContentListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.removePageContentListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#removePagelet(java.lang.String,
   *      int)
   */
  public Pagelet removePagelet(String composer, int index)
      throws IndexOutOfBoundsException {
    if (!isBodyLoaded)
      loadPageBody();
    return page.removePagelet(composer, index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.removeSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setCoverage(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setCoverage(coverage, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setDescription(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setDescription(String description, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setDescription(description, language);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    uri.setIdentifier(identifier);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setIndexed(index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setLayout(java.lang.String)
   */
  public void setLayout(String layout) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setLayout(layout);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setLocked(ch.o2it.weblounge.common.user.User)
   */
  public void setLocked(User user) throws IllegalStateException {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setLocked(user);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#setCreated(ch.o2it.weblounge.common.user.User, java.util.Date)
   */
  public void setCreated(User user, Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setModified(ch.o2it.weblounge.common.user.User,
   *      java.util.Date)
   */
  public void setModified(User user, Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setModified(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setPromoted(boolean)
   */
  public void setPromoted(boolean promoted) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setPromoted(promoted);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setPublished(ch.o2it.weblounge.common.user.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setRights(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setRights(String rights, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setRights(rights, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setTemplate(java.lang.String)
   */
  public void setTemplate(String template) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setTemplate(template);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setTitle(java.lang.String,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setTitle(title, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setType(java.lang.String)
   */
  public void setType(String type) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setType(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#setUnlocked()
   */
  public User setUnlocked() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.setUnlocked();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#setVersion(long)
   */
  public void setVersion(long version) {
    uri.setVersion(version);
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.page.Page#toXml()
   */
  public String toXml() {
    if (!isBodyLoaded)
      loadPageBody();
    return page.toXml();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#compareTo(ch.o2it.weblounge.common.language.Localizable,
   *      ch.o2it.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#supportsLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#switchTo(ch.o2it.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.toString(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.language.Localizable#toString(ch.o2it.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.toString(language, force);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setCreationDate(date);
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Creatable#setCreator(ch.o2it.weblounge.common.user.User)
   */
  public void setCreator(User user) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setCreator(user);
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#addSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#allow(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public void allow(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.check(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#check(ch.o2it.weblounge.common.security.PermissionSet,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public boolean check(PermissionSet permissions, Authority authority) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.check(permissions, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#checkAll(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.checkAll(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#checkOne(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority[])
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.checkOne(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#deny(ch.o2it.weblounge.common.security.Permission,
   *      ch.o2it.weblounge.common.security.Authority)
   */
  public void deny(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.deny(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#permissions()
   */
  public Permission[] permissions() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return page.permissions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#removeSecurityListener(ch.o2it.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.security.Securable#setOwner(ch.o2it.weblounge.common.user.User)
   */
  public void setOwner(User owner) {
    if (!isHeaderLoaded)
      loadPageHeader();
    page.setOwner(owner);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#setPath(java.lang.String)
   */
  public void setPath(String path) {
    uri.setPath(path);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#getContent(ch.o2it.weblounge.common.language.Language)
   */
  public ResourceContent getContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#getOriginalContent()
   */
  public ResourceContent getOriginalContent() {
    return null;
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#removeContent(ch.o2it.weblounge.common.language.Language)
   */
  public ResourceContent removeContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#contents()
   */
  public Set<ResourceContent> contents() {
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Page) {
      return uri.equals(((Page) obj).getURI());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.content.Resource#addContent(ch.o2it.weblounge.common.content.ResourceContent)
   */
  public void addContent(ResourceContent content) {
    return;
  }
  
}
