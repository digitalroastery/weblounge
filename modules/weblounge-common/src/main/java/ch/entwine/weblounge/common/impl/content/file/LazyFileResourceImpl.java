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
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.Localizable;
import ch.entwine.weblounge.common.security.Action;
import ch.entwine.weblounge.common.security.Authority;
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
  protected FileResource file = null;

  /** The preview composer */
  protected Composer previewComposer = null;

  /** True if the file header was loaded */
  protected boolean isHeaderLoaded = false;

  /** True if the file body was loaded */
  protected boolean isBodyLoaded = false;

  /** The file reader */
  protected WeakReference<FileResourceReader> readerRef = null;

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
      FileResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new FileResourceReader();
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
  protected void loadFileHeader() {
    try {

      // Get a hold of the file reader
      FileResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new FileResourceReader();
        readerRef = new WeakReference<FileResourceReader>(reader);
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
  protected void loadFileBody() {
    try {

      // Get a hold of the file reader
      FileResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new FileResourceReader();
        readerRef = new WeakReference<FileResourceReader>(reader);
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
   * @see ch.entwine.weblounge.common.content.file.Page#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.addSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addSeries(java.lang.String)
   */
  public void addSeries(String series) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.addSeries(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getCoverage()
   */
  public String getCoverage() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getCoverage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getCoverage(ch.entwine.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileHeader();
    return file.getCoverage(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getDescription()
   */
  public String getDescription() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getDescription();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getDescription(ch.entwine.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileHeader();
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
   * @see ch.entwine.weblounge.common.content.file.Page#getLockOwner()
   */
  public User getLockOwner() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getLockOwner();
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
   * @see ch.entwine.weblounge.common.content.file.Page#getRights()
   */
  public String getRights() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getRights();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getRights(ch.entwine.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileHeader();
    return file.getRights(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getSubjects()
   */
  public String[] getSubjects() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getSubjects();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getSeries()
   */
  public String[] getSeries() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getSeries();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getTitle()
   */
  public String getTitle() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getTitle();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getTitle(ch.entwine.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileHeader();
    return file.getTitle(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getType()
   */
  public String getType() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getType();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#getResourceURI()
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
      loadFileHeader();
    return file.hasSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#hasSeries(java.lang.String)
   */
  public boolean hasSeries(String series) {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.hasSeries(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#isLocked()
   */
  public boolean isLocked() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.isLocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#isPromoted()
   */
  public boolean isPromoted() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.isPromoted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.removeSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeSeries(java.lang.String)
   */
  public void removeSeries(String series) {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileHeader();
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
      loadFileHeader();
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
   * @see ch.entwine.weblounge.common.content.file.Page#lock(ch.entwine.weblounge.common.security.User)
   */
  public void lock(User user) throws IllegalStateException {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileHeader();
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
      loadFileHeader();
    file.setModified(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setPromoted(boolean)
   */
  public void setPromoted(boolean promoted) {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileHeader();
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
      loadFileHeader();
    file.setRights(rights, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setTitle(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.setTitle(title, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#setType(java.lang.String)
   */
  public void setType(String type) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.setType(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.file.Page#unlock()
   */
  public User unlock() {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileBody();
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
      loadFileHeader();
    return file.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contentLanguages()
   */
  public Set<Language> contentLanguages() {
    if (!isBodyLoaded)
      loadFileBody();
    return file.contentLanguages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#supportsLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#supportsContentLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsContentLanguage(Language language) {
    if (!isBodyLoaded)
      loadFileBody();
    return file.supportsContentLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    if (!isHeaderLoaded)
      loadFileHeader();
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
      loadFileHeader();
    return file.toString(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getModificationDate();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.Modifiable#getLastModified()
   */
  @Override
  public Date getLastModified() {
    Date date = getModificationDate();
    if (date != null)
      return date;
    date = getPublishFrom();
    if (date != null)
      return date;
    return getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getModifier();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.content.Modifiable#getLastModifier()
   */
  @Override
  public User getLastModifier() {
    User user = getModifier();
    if (user != null)
      return user;
    user = getPublisher();
    if (user != null)
      return user;
    return getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#addSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#setAllowDenyOrder(ch.entwine.weblounge.common.security.Securable.Order)
   */
  @Override
  public void setAllowDenyOrder(Order order) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.setAllowDenyOrder(order);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAllowDenyOrder()
   */
  public Order getAllowDenyOrder() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getAllowDenyOrder();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#allow(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public void allow(Action action, Authority authority) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.allow(action, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isAllowed(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isAllowed(Action action, Authority authority) {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.isAllowed(action, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#deny(ch.entwine.weblounge.common.security.Action,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public void deny(Action action, Authority authority) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.deny(action, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDenied(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isDenied(Action action, Authority authority) {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.isDenied(action, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#actions()
   */
  public Action[] actions() {
    if (!isHeaderLoaded)
      loadFileHeader();
    return file.actions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#removeSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadFileHeader();
    file.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#setOwner(ch.entwine.weblounge.common.security.User)
   */
  public void setOwner(User owner) {
    if (!isHeaderLoaded)
      loadFileHeader();
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
    if (!isBodyLoaded)
      loadPage();
    return file.getContent(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getOriginalContent()
   */
  public FileContent getOriginalContent() {
    if (!isBodyLoaded)
      loadPage();
    return file.getOriginalContent();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addContent(ch.entwine.weblounge.common.content.ResourceContent)
   */
  public void addContent(FileContent content) {
    if (!isBodyLoaded)
      loadPage();
    file.addContent(content);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeContent(ch.entwine.weblounge.common.language.Language)
   */
  public FileContent removeContent(Language language) {
    if (!isBodyLoaded)
      loadPage();
    return file.removeContent(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contents()
   */
  public Set<FileContent> contents() {
    if (!isBodyLoaded)
      loadPage();
    return file.contents();
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

}
