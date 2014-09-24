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

package ch.entwine.weblounge.common.impl.content.image;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.image.ImageContent;
import ch.entwine.weblounge.common.content.image.ImageResource;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.language.Localizable;
import ch.entwine.weblounge.common.security.AccessRule;
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
import java.util.SortedSet;

/**
 * Implementation of a lazy loading image.
 */
public class LazyImageResourceImpl implements ImageResource {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LazyImageResourceImpl.class);

  /** The image content as a byte array containing the image's xml */
  protected String imageXml = null;

  /** The image content as a byte array containing the image header's xml */
  protected String headerXml = null;

  /** The image content as a byte array containing the image preview's xml */
  protected String previewXml = null;

  /** The backing image */
  protected ImageResource image = null;

  /** True if the image header was loaded */
  protected boolean isHeaderLoaded = false;

  /** True if the image body was loaded */
  protected boolean isBodyLoaded = false;

  /** The image reader */
  protected WeakReference<ImageResourceReader> readerRef = null;

  /** The image uri */
  protected ResourceURI uri = null;

  /**
   * Creates a new lazy image.
   * 
   * @param uri
   *          the image uri
   * @param imageXml
   *          the full image xml
   * @param headerXml
   *          the head section's xml
   * @param previewXml
   *          the image preview's xml
   */
  public LazyImageResourceImpl(ResourceURI uri, String imageXml,
      String headerXml, String previewXml) {
    this.uri = uri;
    this.imageXml = imageXml;
    this.headerXml = headerXml;
    this.previewXml = previewXml;
  }

  /**
   * Loads the complete image.
   */
  protected void loadImage() {
    try {

      // Get a hold of the image reader
      ImageResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new ImageResourceReader();
        // No need to keep the reference, since we're done after this
      }

      // Load the image
      image = reader.read(IOUtils.toInputStream(imageXml, "utf-8"), uri.getSite());
      isHeaderLoaded = true;
      isBodyLoaded = true;
      cleanupAfterLoading();
    } catch (Throwable e) {
      logger.error("Failed to lazy-load body of {}", uri);
      throw new IllegalStateException("Failed to lazy-load body of " + uri, e);
    }
  }

  /**
   * Loads the image header only.
   */
  protected void loadImageHeader() {
    try {

      // Get a hold of the image reader
      ImageResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new ImageResourceReader();
        readerRef = new WeakReference<ImageResourceReader>(reader);
      }

      // If no separate header was given, then we need to load the whole thing
      // instead.
      if (headerXml == null) {
        loadImage();
        return;
      }

      // Load the image header
      image = reader.readHeader(IOUtils.toInputStream(headerXml, "utf-8"), uri.getSite());
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
   * Loads the image body only.
   */
  protected void loadImageBody() {
    try {

      // Get a hold of the image reader
      ImageResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new ImageResourceReader();
        readerRef = new WeakReference<ImageResourceReader>(reader);
      }

      // Load the image body
      image = reader.readBody(IOUtils.toInputStream(imageXml, "utf-8"), uri.getSite());
      isBodyLoaded = true;
      if (isHeaderLoaded && isBodyLoaded)
        cleanupAfterLoading();
      else if (headerXml != null)
        imageXml = null;

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
    imageXml = null;
    previewXml = null;
    if (readerRef != null)
      readerRef.clear();
  }

  /**
   * Returns <code>true</code> if the image header has been loaded already.
   * 
   * @return <code>true</code> if the image header has been loaded
   */
  public boolean isHeaderLoaded() {
    return isHeaderLoaded;
  }

  /**
   * Returns <code>true</code> if the image body has been loaded already.
   * 
   * @return <code>true</code> if the image body has been loaded
   */
  public boolean isBodyLoaded() {
    return isBodyLoaded;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.addSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addSeries(java.lang.String)
   */
  public void addSeries(String series) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.addSeries(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getCoverage()
   */
  public String getCoverage() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getCoverage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getCoverage(ch.entwine.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getCoverage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getCoverage(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getCoverage(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getCoverage(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getDescription()
   */
  public String getDescription() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getDescription();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getDescription(ch.entwine.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getDescription(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getDescription(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getDescription(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getDescription(language, force);
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
   * @see ch.entwine.weblounge.common.content.image.Page#getLockOwner()
   */
  public User getLockOwner() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getLockOwner();
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
   * @see ch.entwine.weblounge.common.content.image.Page#getRights()
   */
  public String getRights() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getRights();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getRights(ch.entwine.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getRights(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getRights(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getRights(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getRights(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getSubjects()
   */
  public String[] getSubjects() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getSubjects();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getSeries()
   */
  public String[] getSeries() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getSeries();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getTitle()
   */
  public String getTitle() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getTitle();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getTitle(ch.entwine.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getTitle(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getTitle(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getTitle(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getTitle(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getType()
   */
  public String getType() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getType();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getResourceURI()
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
   * @see ch.entwine.weblounge.common.content.image.Page#hasSubject(java.lang.String)
   */
  public boolean hasSubject(String subject) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.hasSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#hasSeries(java.lang.String)
   */
  public boolean hasSeries(String series) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.hasSubject(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#isLocked()
   */
  public boolean isLocked() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.isLocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#isPromoted()
   */
  public boolean isPromoted() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.isPromoted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.removeSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeSeries(java.lang.String)
   */
  public void removeSeries(String series) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.removeSeries(series);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setCoverage(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setCoverage(coverage, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setDescription(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setDescription(String description, Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setDescription(description, language);
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
   * @see ch.entwine.weblounge.common.content.image.Page#lock(ch.entwine.weblounge.common.security.User)
   */
  public void lock(User user) throws IllegalStateException {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.lock(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setCreated(User user, Date date) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setModified(User user, Date date) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setModified(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setPromoted(boolean)
   */
  public void setPromoted(boolean promoted) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setPromoted(promoted);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setPublished(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setRights(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setRights(String rights, Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setRights(rights, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setTitle(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setTitle(title, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setType(java.lang.String)
   */
  public void setType(String type) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setType(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#unlock()
   */
  public User unlock() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.unlock();
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
   * @see ch.entwine.weblounge.common.content.image.Page#toXml()
   */
  public String toXml() {
    if (!isBodyLoaded)
      loadImageBody();
    return image.toXml();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#compareTo(ch.entwine.weblounge.common.language.Localizable,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contentLanguages()
   */
  public Set<Language> contentLanguages() {
    if (!isBodyLoaded)
      loadImageBody();
    return image.contentLanguages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#supportsLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#supportsContentLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsContentLanguage(Language language) {
    if (!isBodyLoaded)
      loadImageBody();
    return image.supportsContentLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.toString(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.toString(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getModificationDate();
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
      loadImageHeader();
    return image.getModifier();
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
      loadImageHeader();
    return image.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#addSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDefaultAccess()
   */
  @Override
  public boolean isDefaultAccess() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.isDefaultAccess();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#setAllowDenyOrder(ch.entwine.weblounge.common.security.Securable.Order)
   */
  @Override
  public void setAllowDenyOrder(Order order) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setAllowDenyOrder(order);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAllowDenyOrder()
   */
  public Order getAllowDenyOrder() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getAllowDenyOrder();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#addAccessRule(ch.entwine.weblounge.common.security.AccessRule)
   */
  @Override
  public void addAccessRule(AccessRule rule) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.addAccessRule(rule);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#getAccessRules()
   */
  @Override
  public SortedSet<AccessRule> getAccessRules() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getAccessRules();
  }
  
  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isAllowed(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isAllowed(Action action, Authority authority) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.isAllowed(action, authority);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.entwine.weblounge.common.security.Securable#isDenied(ch.entwine.weblounge.common.security.Action, ch.entwine.weblounge.common.security.Authority)
   */
  public boolean isDenied(Action action, Authority authority) {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.isDenied(action, authority);
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getActions()
   */
  public Action[] getActions() {
    if (!isHeaderLoaded)
      loadImageHeader();
    return image.getActions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#removeSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#setOwner(ch.entwine.weblounge.common.security.User)
   */
  public void setOwner(User owner) {
    if (!isHeaderLoaded)
      loadImageHeader();
    image.setOwner(owner);
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
  public ImageContent getContent(Language language) {
    if (!isBodyLoaded)
      loadImage();
    return image.getContent(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getOriginalContent()
   */
  public ImageContent getOriginalContent() {
    if (!isBodyLoaded)
      loadImage();
    return image.getOriginalContent();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addContent(ch.entwine.weblounge.common.content.ResourceContent)
   */
  public void addContent(ImageContent content) {
    if (!isBodyLoaded)
      loadImage();
    image.addContent(content);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeContent(ch.entwine.weblounge.common.language.Language)
   */
  public ImageContent removeContent(Language language) {
    if (!isBodyLoaded)
      loadImage();
    return image.removeContent(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contents()
   */
  public Set<ImageContent> contents() {
    if (!isBodyLoaded)
      loadImage();
    return image.contents();
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
