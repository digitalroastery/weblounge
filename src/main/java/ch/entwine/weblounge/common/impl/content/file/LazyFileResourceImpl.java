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

package ch.entwine.weblounge.common.impl.content.file;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.file.FileContent;
import ch.entwine.weblounge.common.content.file.FileResource;
import ch.entwine.weblounge.common.content.page.Composer;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.page.PageContentListener;
import ch.entwine.weblounge.common.content.page.Pagelet;
import ch.entwine.weblounge.common.impl.content.page.ComposerImpl;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.content.page.PagePreviewReader;
import ch.entwine.weblounge.common.impl.content.page.PageReader;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.Localizable;
import ch.entwine.weblounge.common.security.Authority;
import ch.entwine.weblounge.common.security.Permission;
import ch.entwine.weblounge.common.security.PermissionSet;
import ch.entwine.weblounge.common.security.SecurityListener;
import ch.entwine.weblounge.common.security.User;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Set;

/**
 * Implementation of a lazy loading file.
 */
public class LazyFileResourceImpl implements FileResource {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LazyFileResourceImpl.class);

  /** The file content as a byte array containing the file's xml */
  protected String fileXml = null;

  /** The file content as a byte array containing the file header's xml */
  protected String headerXml = null;

  /** The file content as a byte array containing the file preview's xml */
  protected String previewXml = null;

  /** The backing file */
  protected PageImpl file = null;

  /** The preview composer */
  protected Composer previewComposer = null;

  /** True if the file header was loaded */
  protected boolean isHeaderLoaded = false;

  /** True if the file body was loaded */
  protected boolean isBodyLoaded = false;

  /** The file reader */
  protected WeakReference<PageReader> readerRef = null;

  /** The file uri */
  protected ResourceURI uri = null;

  /**
   * Creates a new lazy file.
   * 
   * @param uri
   *          the file uri
   * @param fileXml
   *          the full file xml
   * @param headerXml
   *          the head section's xml
   * @param previewXml
   *          the file preview's xml
   */
  public LazyFileResourceImpl(ResourceURI uri, String fileXml,
      String headerXml, String previewXml) {
    this.uri = uri;
    this.fileXml = fileXml;
    this.headerXml = headerXml;
    this.previewXml = previewXml;
  }

  /**
   * Loads the complete file.
   */
  protected void loadPage() {
    try {

      // Get a hold of the file reader
      PageReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new PageReader();
        // No need to keep the reference, since we're done after this
      }

      // Load the file
      file = reader.read(IOUtils.toInputStream(fileXml, "utf-8"), uri.getSite());
      isHeaderLoaded = true;
      isBodyLoaded = true;
      cleanupAfterLoading();
    } catch (Throwable e) {
      logger.error("Failed to lazy-load body of {}", uri);
      throw new IllegalStateException("Failed to lazy-load body of " + uri, e);
    }
  }

  /**
   * Loads the file header only.
   */
  protected void loadPageHeader() {
    try {

      // Get a hold of the file reader
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

      // Load the file header
      file = reader.readHeader(IOUtils.toInputStream(headerXml, "utf-8"), uri.getSite());
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
   * Loads the file body only.
   */
  protected void loadPageBody() {
    try {

      // Get a hold of the file reader
      PageReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new PageReader();
        readerRef = new WeakReference<PageReader>(reader);
      }

      // Load the file body
      file = reader.readBody(IOUtils.toInputStream(fileXml, "utf-8"), uri.getSite());
      isBodyLoaded = true;
      if (isHeaderLoaded && isBodyLoaded)
        cleanupAfterLoading();
      else if (headerXml != null)
        fileXml = null;

    } catch (Throwable e) {
      logger.error("Failed to lazy-load body of {}: {}", uri, e.getMessage());
      throw new IllegalStateException(e);
    }
  }

  /**
   * Loads the file preview only.
   */
  protected void loadPagePreview() {
    // If no separate preview data was given, then we need to load the whole
    // thing instead.
    if (previewXml == null) {
      loadPageBody();
      previewComposer = new ComposerImpl(PagePreviewReader.PREVIEW_COMPOSER_NAME, file.getPreview());
      return;
    }

    try {
      PagePreviewReader reader = new PagePreviewReader();
      previewComposer = reader.read(IOUtils.toInputStream(previewXml, "utf-8"), uri);
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
    fileXml = null;
    previewXml = null;
    previewComposer = null;
    if (readerRef != null)
      readerRef.clear();
  }

  /**
   * Returns <code>true</code> if the file header has been loaded already.
   * 
   * @return <code>true</code> if the file header has been loaded
   */
  public boolean isHeaderLoaded() {
    return isHeaderLoaded;
  }

  /**
   * Returns <code>true</code> if the file body has been loaded already.
   * 
   * @return <code>true</code> if the file body has been loaded
   */
  public boolean isBodyLoaded() {
    return isBodyLoaded;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#addPageContentListener(ch.entwine.weblounge.common.content.file.PageContentListener)
   */
  public void addPageContentListener(PageContentListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.addPageContentListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#addPagelet(ch.entwine.weblounge.common.content.file.Pagelet,
   *      java.lang.String)
   */
  public Pagelet addPagelet(Pagelet filelet, String composer) {
    if (!isBodyLoaded)
      loadPageBody();
    return file.addPagelet(filelet, composer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#addPagelet(ch.entwine.weblounge.common.content.file.Pagelet,
   *      java.lang.String, int)
   */
  public Pagelet addPagelet(Pagelet filelet, String composer, int index)
      throws IndexOutOfBoundsException {
    if (!isBodyLoaded)
      loadPageBody();
    return file.addPagelet(filelet, composer, index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.addSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addSeries(java.lang.String)
   */
  public void addSeries(String series) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.addSeries(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getComposers()
   */
  public Composer[] getComposers() {
    if (!isBodyLoaded)
      loadPageBody();
    return file.getComposers();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getStage()
   */
  public Composer getStage() {
    if (!isHeaderLoaded && isBodyLoaded)
      loadPageHeader();
    else if (!isBodyLoaded)
      loadPage();
    return file.getStage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getComposer(java.lang.String)
   */
  public Composer getComposer(String composerId) {
    if (!isBodyLoaded)
      loadPageBody();
    return file.getComposer(composerId);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getCoverage()
   */
  public String getCoverage() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getCoverage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getCoverage(ch.entwine.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getCoverage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getCoverage(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getCoverage(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getCoverage(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getDescription()
   */
  public String getDescription() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getDescription();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getDescription(ch.entwine.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getDescription(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getDescription(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getDescription(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getDescription(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getIdentifier()
   */
  public String getIdentifier() {
    return uri.getIdentifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getLayout()
   */
  public String getLayout() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getLayout();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getLockOwner()
   */
  public User getLockOwner() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getLockOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getPagelets()
   */
  public Pagelet[] getPagelets() {
    if (!isBodyLoaded)
      loadPageBody();
    return file.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getPagelets(java.lang.String)
   */
  public Pagelet[] getPagelets(String composer) {
    if (!isBodyLoaded)
      loadPageBody();
    return file.getPagelets(composer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getPagelets(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public Pagelet[] getPagelets(String composer, String module, String id) {
    if (!isBodyLoaded)
      loadPageBody();
    return file.getPagelets(composer, module, id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getPath()
   */
  public String getPath() {
    return uri.getPath();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getPreview()
   */
  public Pagelet[] getPreview() {
    if (previewComposer == null)
      loadPagePreview();
    return previewComposer.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getRights()
   */
  public String getRights() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getRights();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getRights(ch.entwine.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getRights(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getRights(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getRights(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getRights(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getSubjects()
   */
  public String[] getSubjects() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getSubjects();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getSeries()
   */
  public String[] getSeries() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getSeries();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getTemplate()
   */
  public String getTemplate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getTemplate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getTitle()
   */
  public String getTitle() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getTitle();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getTitle(ch.entwine.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getTitle(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getTitle(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getTitle(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getTitle(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getType()
   */
  public String getType() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getType();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getURI()
   */
  public ResourceURI getURI() {
    return uri;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getVersion()
   */
  public long getVersion() {
    return uri.getVersion();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#hasSubject(java.lang.String)
   */
  public boolean hasSubject(String subject) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.hasSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#hasSeries(java.lang.String)
   */
  public boolean hasSeries(String series) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.hasSeries(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#isIndexed()
   */
  public boolean isIndexed() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.isIndexed();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#isLocked()
   */
  public boolean isLocked() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.isLocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#isPromoted()
   */
  public boolean isPromoted() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.isPromoted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#removePageContentListener(ch.entwine.weblounge.common.content.file.PageContentListener)
   */
  public void removePageContentListener(PageContentListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.removePageContentListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#removePagelet(java.lang.String,
   *      int)
   */
  public Pagelet removePagelet(String composer, int index)
      throws IndexOutOfBoundsException {
    if (!isBodyLoaded)
      loadPageBody();
    return file.removePagelet(composer, index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.removeSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeSeries(java.lang.String)
   */
  public void removeSeries(String series) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.removeSeries(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setCoverage(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setCoverage(coverage, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setDescription(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setDescription(String description, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setDescription(description, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setIdentifier(java.lang.String)
   */
  public void setIdentifier(String identifier) {
    uri.setIdentifier(identifier);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setIndexed(index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setLayout(java.lang.String)
   */
  public void setLayout(String layout) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setLayout(layout);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#lock(ch.entwine.weblounge.common.security.User)
   */
  public void lock(User user) throws IllegalStateException {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.lock(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setCreated(User user, Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setModified(User user, Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setModified(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setPromoted(boolean)
   */
  public void setPromoted(boolean promoted) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setPromoted(promoted);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setPublished(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setRights(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setRights(String rights, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setRights(rights, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setDefaultTemplate(java.lang.String)
   */
  public void setTemplate(String template) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setTemplate(template);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setTitle(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setTitle(title, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setType(java.lang.String)
   */
  public void setType(String type) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setType(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#unlock()
   */
  public User unlock() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.unlock();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setVersion(long)
   */
  public void setVersion(long version) {
    uri.setVersion(version);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#toXml()
   */
  public String toXml() {
    if (!isBodyLoaded)
      loadPageBody();
    return file.toXml();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#compareTo(ch.entwine.weblounge.common.language.Localizable,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#supportsLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.toString(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.toString(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#addSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#allow(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public void allow(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#check(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public boolean check(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.check(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#check(ch.entwine.weblounge.common.security.PermissionSet,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public boolean check(PermissionSet permissions, Authority authority) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.check(permissions, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#checkAll(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority[])
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.checkAll(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#checkOne(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority[])
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.checkOne(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#deny(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public void deny(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.deny(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#permissions()
   */
  public Permission[] permissions() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return file.permissions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#removeSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#setOwner(ch.entwine.weblounge.common.security.User)
   */
  public void setOwner(User owner) {
    if (!isHeaderLoaded)
      loadPageHeader();
    file.setOwner(owner);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setPath(java.lang.String)
   */
  public void setPath(String path) {
    uri.setPath(path);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getContent(ch.entwine.weblounge.common.language.Language)
   */
  public FileContent getContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getOriginalContent()
   */
  public FileContent getOriginalContent() {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeContent(ch.entwine.weblounge.common.language.Language)
   */
  public FileContent removeContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contents()
   */
  public Set<FileContent> contents() {
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
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return uri.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addContent(ch.entwine.weblounge.common.content.ResourceContent)
   */
  public void addContent(FileContent content) {
    // TODO Auto-generated method stub
  }

}
