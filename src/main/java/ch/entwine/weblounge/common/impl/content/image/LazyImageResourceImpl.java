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
  protected PageImpl image = null;

  /** The preview composer */
  protected Composer previewComposer = null;

  /** True if the image header was loaded */
  protected boolean isHeaderLoaded = false;

  /** True if the image body was loaded */
  protected boolean isBodyLoaded = false;

  /** The image reader */
  protected WeakReference<PageReader> readerRef = null;

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
      String headerXml,
      String previewXml) {
    this.uri = uri;
    this.imageXml = imageXml;
    this.headerXml = headerXml;
    this.previewXml = previewXml;
  }

  /**
   * Loads the complete image.
   */
  protected void loadPage() {
    try {

      // Get a hold of the image reader
      PageReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new PageReader();
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
  protected void loadPageHeader() {
    try {

      // Get a hold of the image reader
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
  protected void loadPageBody() {
    try {

      // Get a hold of the image reader
      PageReader reader = (readerRef != null) ? readerRef.get() : null;
      if (reader == null) {
        reader = new PageReader();
        readerRef = new WeakReference<PageReader>(reader);
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
   * Loads the image preview only.
   */
  protected void loadPagePreview() {
    // If no separate preview data was given, then we need to load the whole
    // thing instead.
    if (previewXml == null) {
      loadPageBody();
      previewComposer = new ComposerImpl(PagePreviewReader.PREVIEW_COMPOSER_NAME, image.getPreview());
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
    imageXml = null;
    previewXml = null;
    previewComposer = null;
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
   * @see ch.entwine.weblounge.common.content.image.Page#addPageContentListener(ch.entwine.weblounge.common.content.image.PageContentListener)
   */
  public void addPageContentListener(PageContentListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.addPageContentListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#addPagelet(ch.entwine.weblounge.common.content.image.Pagelet,
   *      java.lang.String)
   */
  public Pagelet addPagelet(Pagelet imagelet, String composer) {
    if (!isBodyLoaded)
      loadPageBody();
    return image.addPagelet(imagelet, composer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#addPagelet(ch.entwine.weblounge.common.content.image.Pagelet,
   *      java.lang.String, int)
   */
  public Pagelet addPagelet(Pagelet imagelet, String composer, int index)
      throws IndexOutOfBoundsException {
    if (!isBodyLoaded)
      loadPageBody();
    return image.addPagelet(imagelet, composer, index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#addSubject(java.lang.String)
   */
  public void addSubject(String subject) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.addSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getComposers()
   */
  public Composer[] getComposers() {
    if (!isBodyLoaded)
      loadPageBody();
    return image.getComposers();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getStage()
   */
  public Composer getStage() {
    if (!isHeaderLoaded && isBodyLoaded)
      loadPageHeader();
    else if (!isBodyLoaded)
      loadPage();
    return image.getStage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getComposer(java.lang.String)
   */
  public Composer getComposer(String composerId) {
    if (!isBodyLoaded)
      loadPageBody();
    return image.getComposer(composerId);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getCoverage()
   */
  public String getCoverage() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getCoverage();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getCoverage(ch.entwine.weblounge.common.language.Language)
   */
  public String getCoverage(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageHeader();
    return image.getCoverage(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getDescription()
   */
  public String getDescription() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getDescription();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getDescription(ch.entwine.weblounge.common.language.Language)
   */
  public String getDescription(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageHeader();
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
   * @see ch.entwine.weblounge.common.content.image.Page#getLayout()
   */
  public String getLayout() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getLayout();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getLockOwner()
   */
  public User getLockOwner() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getLockOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getPagelets()
   */
  public Pagelet[] getPagelets() {
    if (!isBodyLoaded)
      loadPageBody();
    return image.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getPagelets(java.lang.String)
   */
  public Pagelet[] getPagelets(String composer) {
    if (!isBodyLoaded)
      loadPageBody();
    return image.getPagelets(composer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getPagelets(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  public Pagelet[] getPagelets(String composer, String module, String id) {
    if (!isBodyLoaded)
      loadPageBody();
    return image.getPagelets(composer, module, id);
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
   * @see ch.entwine.weblounge.common.content.image.Page#getPreview()
   */
  public Pagelet[] getPreview() {
    if (previewComposer == null)
      loadPagePreview();
    return previewComposer.getPagelets();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getRights()
   */
  public String getRights() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getRights();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getRights(ch.entwine.weblounge.common.language.Language)
   */
  public String getRights(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageHeader();
    return image.getRights(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getSubjects()
   */
  public String[] getSubjects() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getSubjects();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getTemplate()
   */
  public String getTemplate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getTemplate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getTitle()
   */
  public String getTitle() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getTitle();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getTitle(ch.entwine.weblounge.common.language.Language)
   */
  public String getTitle(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageHeader();
    return image.getTitle(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getType()
   */
  public String getType() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getType();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#getURI()
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
      loadPageHeader();
    return image.hasSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#isIndexed()
   */
  public boolean isIndexed() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.isIndexed();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#isLocked()
   */
  public boolean isLocked() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.isLocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#isPromoted()
   */
  public boolean isPromoted() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.isPromoted();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#removePageContentListener(ch.entwine.weblounge.common.content.image.PageContentListener)
   */
  public void removePageContentListener(PageContentListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.removePageContentListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#removePagelet(java.lang.String,
   *      int)
   */
  public Pagelet removePagelet(String composer, int index)
      throws IndexOutOfBoundsException {
    if (!isBodyLoaded)
      loadPageBody();
    return image.removePagelet(composer, index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#removeSubject(java.lang.String)
   */
  public void removeSubject(String subject) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.removeSubject(subject);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setCoverage(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setCoverage(String coverage, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageHeader();
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
   * @see ch.entwine.weblounge.common.content.image.Page#setIndexed(boolean)
   */
  public void setIndexed(boolean index) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.setIndexed(index);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setLayout(java.lang.String)
   */
  public void setLayout(String layout) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.setLayout(layout);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#lock(ch.entwine.weblounge.common.security.User)
   */
  public void lock(User user) throws IllegalStateException {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageHeader();
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
      loadPageHeader();
    image.setModified(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setPromoted(boolean)
   */
  public void setPromoted(boolean promoted) {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageHeader();
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
      loadPageHeader();
    image.setRights(rights, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setDefaultTemplate(java.lang.String)
   */
  public void setTemplate(String template) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.setTemplate(template);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setTitle(java.lang.String,
   *      ch.entwine.weblounge.common.language.Language)
   */
  public void setTitle(String title, Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.setTitle(title, language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#setType(java.lang.String)
   */
  public void setType(String type) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.setType(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.image.Page#unlock()
   */
  public User unlock() {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageBody();
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
      loadPageHeader();
    return image.compareTo(o, l);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#languages()
   */
  public Set<Language> languages() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.languages();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#supportsLanguage(ch.entwine.weblounge.common.language.Language)
   */
  public boolean supportsLanguage(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.supportsLanguage(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#switchTo(ch.entwine.weblounge.common.language.Language)
   */
  public Language switchTo(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.switchTo(language);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.language.Localizable#toString(ch.entwine.weblounge.common.language.Language)
   */
  public String toString(Language language) {
    if (!isHeaderLoaded)
      loadPageHeader();
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
      loadPageHeader();
    return image.toString(language, force);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreationDate(java.util.Date)
   */
  public void setCreationDate(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.setCreationDate(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#setCreator(ch.entwine.weblounge.common.security.User)
   */
  public void setCreator(User user) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.setCreator(user);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getCreator();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModificationDate()
   */
  public Date getModificationDate() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getModificationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Modifiable#getModifier()
   */
  public User getModifier() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getModifier();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishFrom()
   */
  public Date getPublishFrom() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getPublishFrom();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublishTo()
   */
  public Date getPublishTo() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getPublishTo();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#getPublisher()
   */
  public User getPublisher() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getPublisher();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished()
   */
  public boolean isPublished() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.isPublished();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Publishable#isPublished(java.util.Date)
   */
  public boolean isPublished(Date date) {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.isPublished(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#addSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void addSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.addSecurityListener(listener);
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
    image.allow(permission, authority);
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
    return image.check(permission, authority);
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
    return image.check(permissions, authority);
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
    return image.checkAll(permission, authorities);
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
    return image.checkOne(permission, authorities);
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
    image.deny(permission, authority);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#getOwner()
   */
  public User getOwner() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.getOwner();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#permissions()
   */
  public Permission[] permissions() {
    if (!isHeaderLoaded)
      loadPageHeader();
    return image.permissions();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#removeSecurityListener(ch.entwine.weblounge.common.security.SecurityListener)
   */
  public void removeSecurityListener(SecurityListener listener) {
    if (!isHeaderLoaded)
      loadPageHeader();
    image.removeSecurityListener(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.security.Securable#setOwner(ch.entwine.weblounge.common.security.User)
   */
  public void setOwner(User owner) {
    if (!isHeaderLoaded)
      loadPageHeader();
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
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#getOriginalContent()
   */
  public ImageContent getOriginalContent() {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#removeContent(ch.entwine.weblounge.common.language.Language)
   */
  public ImageContent removeContent(Language language) {
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.Resource#contents()
   */
  public Set<ImageContent> contents() {
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
  public void addContent(ImageContent content) {
    // TODO Auto-generated method stub

  }

}
