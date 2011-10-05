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

package ch.entwine.weblounge.common.impl.content.movie;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.movie.MovieContent;
import ch.entwine.weblounge.common.content.movie.MovieResource;
import ch.entwine.weblounge.common.content.page.Page;
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
 * Implementation of a lazy loading audio visual.
 */
public class LazyMovieResourceImpl implements MovieResource {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LazyMovieResourceImpl.class);

  /** The content as a byte array containing the audio visual's xml */
  protected String audiovisualXml = null;

  /** The content as a byte array containing the audio visual header's xml */
  protected String headerXml = null;

  /** The content as a byte array containing the audio visual preview's xml */
  protected String previewXml = null;

  /** The backing audio visual object */
  protected MovieResource audioVisual = null;

  /** True if the audio visual header was loaded */
  protected boolean isHeaderLoaded = false;

  /** True if the audio visual body was loaded */
  protected boolean isBodyLoaded = false;

  /** The audio visual reader */
  protected WeakReference<MovieResourceReader> readerRef = null;

  /** The audio visual uri */
  protected ResourceURI uri = null;

  /**
   * Creates a new lazy audio visual object.
   * 
   * @param uri
   *          the audio visual uri
   * @param avXml
   *          the full audio visual xml
   * @param headerXml
   *          the head section's xml
   * @param previewXml
   *          the audio visual preview's xml
   */
  public LazyMovieResourceImpl(ResourceURI uri, String avXml,
      String headerXml, String previewXml) {
    this.uri = uri;
    this.audiovisualXml = avXml;
    this.headerXml = headerXml;
    this.previewXml = previewXml;
  }

  /**
   * Loads the complete audio visual.
   */
  protected void loadAudioVisual() {
    try {

      // Get a hold of the audio visual reader
      MovieResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new MovieResourceReader();
        // No need to keep the reference, since we're done after this
      }

      // Load the audio visual
      audioVisual = reader.read(IOUtils.toInputStream(audiovisualXml, "utf-8"), uri.getSite());
      isHeaderLoaded = true;
      isBodyLoaded = true;
      cleanupAfterLoading();
    } catch (Throwable e) {
      logger.error("Failed to lazy-load body of {}", uri);
      throw new IllegalStateException("Failed to lazy-load body of " + uri, e);
    }
  }

  /**
   * Loads the audio visual header only.
   */
  protected void loadAudioVisualHeader() {
    try {

      // Get a hold of the audio visual reader
      MovieResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new MovieResourceReader();
        readerRef = new WeakReference<MovieResourceReader>(reader);
      }

      // If no separate header was given, then we need to load the whole thing
      // instead.
      if (headerXml == null) {
        loadAudioVisual();
        return;
      }

      // Load the audio visual header
      audioVisual = reader.readHeader(IOUtils.toInputStream(headerXml, "utf-8"), uri.getSite());
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
   * Loads the audio visual body only.
   */
  protected void loadAudioVisualBody() {
    try {

      // Get a hold of the audio visual reader
      MovieResourceReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new MovieResourceReader();
        readerRef = new WeakReference<MovieResourceReader>(reader);
      }

      // Load the audio visual body
      audioVisual = reader.readBody(IOUtils.toInputStream(audiovisualXml, "utf-8"), uri.getSite());
      isBodyLoaded = true;
      if (isHeaderLoaded && isBodyLoaded)
        cleanupAfterLoading();
      else if (headerXml != null)
        audiovisualXml = null;

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
    audiovisualXml = null;
    previewXml = null;
    if (readerRef != null)
      readerRef.clear();
  }

  /**
   * Returns <code>true</code> if the audio visual header has been loaded
   * already.
   * 
   * @return <code>true</code> if the audio visual header has been loaded
   */
  public boolean isHeaderLoaded() {
    return isHeaderLoaded;
  }

  /**
   * Returns <code>true</code> if the audio visual body has been loaded already.
   * 
   * @return <code>true</code> if the audio visual body has been loaded
   */
  public boolean isBodyLoaded() {
    return isBodyLoaded;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.addSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage()
   */
  public String getCoverage() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getCoverage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage(ch.entwine.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getCoverage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getCoverage(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getCoverage(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getCoverage(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription()
   */
  public String getDescription() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getDescription();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription(ch.entwine.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getDescription(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getDescription(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getDescription(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getDescription(language, force);
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
   * @see ch.entwine.weblounge.common.content.Resource#getLockOwner()
   */
  public User getLockOwner() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getLockOwner();
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
   * @see ch.entwine.weblounge.common.content.Resource#getRights()
   */
  public String getRights() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getRights();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getRights(ch.entwine.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getRights(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getRights(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getRights(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getRights(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getSubjects()
   */
  public String[] getSubjects() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getSubjects();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getTitle()
   */
  public String getTitle() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getTitle();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getTitle(ch.entwine.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getTitle(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getTitle(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String getTitle(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getTitle(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getType()
   */
  public String getType() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getType();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getURI()
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
   * @see ch.entwine.weblounge.common.content.Resource#hasSubject(java.lang.String)
   */
  public boolean hasSubject(String subject) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.hasSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#isIndexed()
   */
  public boolean isIndexed() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.isIndexed();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#isLocked()
   */
  public boolean isLocked() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.isLocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#isPromoted()
   */
  public boolean isPromoted() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.isPromoted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.removeSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setCoverage(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setCoverage(coverage, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setDescription(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setDescription(String description, Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setDescription(description, language);
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
   * @see ch.entwine.weblounge.common.content.Resource#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setIndexed(index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#lock(ch.entwine.weblounge.common.security.User)
   */
  public void lock(User user) throws IllegalStateException {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.lock(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setCreated(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setCreated(User user, Date date) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setModified(ch.entwine.weblounge.common.security.User,
   *      java.util.Date)
   */
  public void setModified(User user, Date date) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setModified(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setPromoted(boolean)
   */
  public void setPromoted(boolean promoted) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setPromoted(promoted);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setPublished(ch.entwine.weblounge.common.security.User,
   *      java.util.Date, java.util.Date)
   */
  public void setPublished(User publisher, Date from, Date to) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setPublished(publisher, from, to);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setRights(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setRights(String rights, Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setRights(rights, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setTitle(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setTitle(title, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#setType(java.lang.String)
   */
  public void setType(String type) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setType(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#unlock()
   */
  public User unlock() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.unlock();
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
   * @see ch.entwine.weblounge.common.content.Resource#toXml()
   */
  public String toXml() {
    if (!isBodyLoaded)
      loadAudioVisualBody();
    return audioVisual.toXml();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#compareTo(ch.entwine.weblounge.common.language.Localizable,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public int compareTo(Localizable o, Language l) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#supportsLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.toString(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language,
   *      boolean)
   */
  public String toString(Language language, boolean force) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.toString(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#addSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.addSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#allow(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public void allow(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.allow(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#check(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public boolean check(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.check(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#check(ch.entwine.weblounge.common.security.PermissionSet,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public boolean check(PermissionSet permissions, Authority authority) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.check(permissions, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#checkAll(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority[])
   */
  public boolean checkAll(Permission permission, Authority[] authorities) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.checkAll(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#checkOne(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority[])
   */
  public boolean checkOne(Permission permission, Authority[] authorities) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.checkOne(permission, authorities);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#deny(ch.entwine.weblounge.common.security.Permission,
   *      ch.entwine.weblounge.common.security.Authority)
   */
  public void deny(Permission permission, Authority authority) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.deny(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#permissions()
   */
  public Permission[] permissions() {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    return audioVisual.permissions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#removeSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#setOwner(ch.entwine.weblounge.common.security.User)
   */
  public void setOwner(User owner) {
    if (!isHeaderLoaded)
      loadAudioVisualHeader();
    audioVisual.setOwner(owner);
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
  public MovieContent getContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getOriginalContent()
   */
  public MovieContent getOriginalContent() {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeContent(ch.entwine.weblounge.common.language.Language)
   */
  public MovieContent removeContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contents()
   */
  public Set<MovieContent> contents() {
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
  public void addContent(MovieContent content) {
    // TODO Auto-generated method stub

  }

}
