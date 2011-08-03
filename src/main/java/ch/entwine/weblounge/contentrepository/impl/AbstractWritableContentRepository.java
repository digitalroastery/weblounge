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

package ch.entwine.weblounge.contentrepository.impl;

import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.url.PathUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Abstract base implementation of a <code>WritableContentRepository</code>.
 */
public abstract class AbstractWritableContentRepository extends AbstractContentRepository implements WritableContentRepository {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(AbstractWritableContentRepository.class);

  /** The image style tracker */
  private ImageStyleTracker imageStyleTracker = null;

  /**
   * Creates a new instance of the content repository.
   * 
   * @param type
   *          the repository type
   */
  public AbstractWritableContentRepository(String type) {
    super(type);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#connect(ch.entwine.weblounge.common.site.Site)
   */
  @Override
  public void connect(Site site) throws ContentRepositoryException {
    super.connect(site);
    Bundle bundle = loadBundle(site);
    imageStyleTracker = new ImageStyleTracker(bundle.getBundleContext());
    imageStyleTracker.open();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#disconnect()
   */
  @Override
  public void disconnect() throws ContentRepositoryException {
    super.disconnect();
    imageStyleTracker.close();
    imageStyleTracker = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#delete(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public boolean delete(ResourceURI uri) throws ContentRepositoryException,
      IOException {
    return delete(uri, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#delete(ch.entwine.weblounge.common.content.ResourceURI,
   *      boolean)
   */
  public boolean delete(ResourceURI uri, boolean allRevisions)
      throws ContentRepositoryException, IOException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // See if the resource exists
    if (allRevisions && !index.existsInAnyVersion(uri) || !index.exists(uri)) {
      logger.warn("Resource '{}' not found in repository index", uri);
      return false;
    }

    // Make sure the resource is not being referenced elsewhere
    // TODO: Make this it's own index
    SearchQuery searchByResource = new SearchQueryImpl(uri.getSite());
    searchByResource.withProperty("resourceid", uri.getIdentifier());
    if (index.find(searchByResource).getItems().length > 0) {
      logger.warn("Resource '{}' is still being referenced", uri);
      return false;
    }

    // Get the revisions to delete
    long[] revisions = new long[] { uri.getVersion() };
    if (allRevisions) {
      if (uri.getVersion() != Resource.LIVE)
        uri = new ResourceURIImpl(uri, Resource.LIVE);
      revisions = index.getRevisions(uri);
    }

    // Delete resources
    deleteResource(uri, revisions);

    // Delete the index entries
    for (long revision : revisions) {
      index.delete(new ResourceURIImpl(uri, revision));
    }

    // Delete previews
    deletePreviews(uri);

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#move(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceURI)
   */
  public void move(ResourceURI uri, ResourceURI target) throws IOException,
      ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    index.move(uri, target.getPath());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#put(ch.entwine.weblounge.common.content.Resource)
   */
  public <T extends ResourceContent> Resource<T> put(Resource<T> resource)
      throws ContentRepositoryException, IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Add entry to index
    if (!index.existsInAnyVersion(resource.getURI())) {
      if (resource.contents().size() > 0)
        throw new IllegalStateException("Cannot add content metadata without content");
      index.add(resource);
    }

    // The resource exists in some version
    else {
      logger.debug("Checking content section of existing {} {}", resource.getType(), resource);
      Resource<?> r = get(resource.getURI());

      // Does the resource exist in this version?
      if (r != null) {
        if (resource.contents().size() != r.contents().size())
          throw new IllegalStateException("Cannot modify content metadata without content");
        for (ResourceContent c : resource.contents()) {
          if (!c.equals(r.getContent(c.getLanguage())))
            throw new IllegalStateException("Cannot modify content metadata without content");
        }
        index.update(resource);
      }

      // We are about to add a new version of a resource
      else {
        if (resource.contents().size() > 0)
          throw new IllegalStateException("Cannot modify content metadata without content");
        index.add(resource);
      }
    }

    // Write the updated resource to disk
    storeResource(resource);

    // Create the preview images
    createPreviews(resource);

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#putContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      java.io.InputStream)
   */
  @SuppressWarnings("unchecked")
  public <T extends ResourceContent> Resource<T> putContent(ResourceURI uri,
      T content, InputStream is) throws ContentRepositoryException,
      IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Make sure the resource exists
    if (!index.exists(uri))
      throw new IllegalStateException("Cannot add content to missing resource " + uri);
    Resource<T> resource = null;
    try {
      resource = (Resource<T>) get(uri);
      if (resource == null) {
        throw new IllegalStateException("Resource " + uri + " not found");
      }
    } catch (ClassCastException e) {
      logger.error("Trying to add content of type {} to incompatible resource", content.getClass());
      throw new IllegalStateException(e);
    }

    // Store the content and add entry to index
    try {
      resource.addContent(content);
    } catch (ClassCastException e) {
      logger.error("Trying to add content of type {} to incompatible resource", content.getClass());
      throw new IllegalStateException(e);
    }

    storeResourceContent(uri, content, is);
    storeResource(resource);
    index.update(resource);

    // Create the preview images
    createPreviews(resource);

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#deleteContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent)
   */
  @SuppressWarnings("unchecked")
  public <T extends ResourceContent> Resource<T> deleteContent(ResourceURI uri,
      T content) throws ContentRepositoryException, IOException,
      IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Make sure the resource exists
    if (!index.exists(uri))
      throw new IllegalStateException("Cannot remove content from missing resource " + uri);
    Resource<T> resource = null;
    try {
      resource = (Resource<T>) get(uri);
      if (resource == null) {
        throw new IllegalStateException("Resource " + uri + " not found");
      }
    } catch (ClassCastException e) {
      logger.error("Trying to remove content of type {} from incompatible resource", content.getClass());
      throw new IllegalStateException(e);
    }

    // Store the content and add entry to index
    resource.removeContent(content.getLanguage());
    deleteResourceContent(uri, content);
    storeResource(resource);
    index.update(resource);

    // Delete previews
    deletePreviews(uri, content.getLanguage());

    return resource;
  }

  /**
   * Writes a new resource to the repository storage.
   * 
   * @param resource
   *          the resource content
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  protected abstract <T extends ResourceContent, R extends Resource<T>> R storeResource(
      R resource) throws IOException;

  /**
   * Writes the resource content to the repository storage.
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @param is
   *          the input stream
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  protected abstract <T extends ResourceContent> T storeResourceContent(
      ResourceURI uri, T content, InputStream is) throws IOException;

  /**
   * Deletes the indicated revisions of resource <code>uri</code> from the
   * repository. The concrete implementation is responsible for making the
   * deletion of multiple revisions safe, i. e. transactional.
   * 
   * @param uri
   *          the resource uri
   * @param revisions
   *          the revisions to remove
   */
  protected abstract void deleteResource(ResourceURI uri, long[] revisions)
      throws IOException;

  /**
   * Deletes the resource content from the repository storage.
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  protected abstract <T extends ResourceContent> void deleteResourceContent(
      ResourceURI uri, T content) throws IOException;

  /**
   * Creates the previews for this resource in all languages and for all known
   * image styles.
   * 
   * @param resource
   *          the resource
   */
  protected void createPreviews(Resource<?> resource) {
    ResourceURI resourceURI = resource.getURI();
    String resourceType = resourceURI.getType();

    // Find the resource serializer
    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
    if (serializer == null) {
      logger.warn("Unable to index resources of type '{}': no resource serializer found", resourceType);
      return;
    }

    // Does the serializer come with a preview generator?
    PreviewGenerator previewGenerator = serializer.getPreviewGenerator();
    if (previewGenerator == null) {
      logger.debug("Resource type '{}' does not support previews", resourceType);
      return;
    }

    // Compile the full list of image styles
    List<ImageStyle> styles = new ArrayList<ImageStyle>();
    styles.addAll(imageStyleTracker.getImageStyles());
    for (Module m : getSite().getModules()) {
      styles.addAll(Arrays.asList(m.getImageStyles()));
    }

    // Create the previews for all languages.
    for (Language language : resource.languages()) {
      ResourceContent resourceContent = resource.getContent(language);

      logger.info("Creating {} previews for {} {}", new Object[] {
          language.getDescription(),
          resourceType,
          resource });

      for (ImageStyle style : styles) {

        // Create the target file name
        StringBuilder filename = new StringBuilder();
        String basename = null;

        if (resourceContent != null)
          basename = FilenameUtils.getBaseName(resourceContent.getFilename());
        else
          basename = resource.getIdentifier();
        filename.append(basename);
        filename.append("-").append(language.getIdentifier());

        String suffix = previewGenerator.getSuffix(resource, language, style);
        if (StringUtils.isNotBlank(suffix)) {
          filename.append(".").append(suffix);
        }

        // Create the preview
        File previewFile = createPreview(resource, style, language, filename.toString(), previewGenerator);

        if (previewFile != null)
          logger.debug("Created {} preview '{}' for {}", new Object[] {
              language.getDescription(),
              style.getIdentifier(),
              resource });
        else
          logger.debug("Preview generation '{}' for {} failed", new Object[] {
              style.getIdentifier(),
              resource });
      }

      logger.debug("All {} previews created for {} {}", new Object[] {
          language.getDescription(),
          resourceType,
          resource });
    }
  }

  /**
   * Creates a preview from the given resource and returns the preview's file or
   * <code>null</code> if the preview could not be created.
   * 
   * @param resource
   *          the resource
   * @param style
   *          the image style
   * @param language
   *          the language
   * @param filename
   *          the original filename
   * @param previewGenerator
   *          the preview generator
   * @return the preview file
   */
  private File createPreview(Resource<?> resource, ImageStyle style,
      Language language, String filename, PreviewGenerator previewGenerator) {
    ResourceURI resourceURI = resource.getURI();
    String resourceType = resourceURI.getType();

    InputStream resourceInputStream = null;
    InputStream contentRepositoryIs = null;
    FileOutputStream fos = null;
    File scaledResourceFile = null;

    try {
      scaledResourceFile = ImageStyleUtils.createScaledFile(resourceURI, filename.toString(), language, style);

      // Find the modification date
      Date modificationDate = resource.getModificationDate();
      if (modificationDate == null)
        modificationDate = resource.getCreationDate();
      long lastModified = modificationDate.getTime();

      // Create the file if it doesn't exist or if it is outdated
      if (!scaledResourceFile.isFile() || scaledResourceFile.lastModified() < lastModified) {
        contentRepositoryIs = getContent(resourceURI, language);
        fos = new FileOutputStream(scaledResourceFile);
        logger.debug("Creating scaled image '{}' at {}", resource, scaledResourceFile);
        previewGenerator.createPreview(resource, language, style, contentRepositoryIs, fos);
        scaledResourceFile.setLastModified(lastModified);
      }

      return scaledResourceFile;
    } catch (ContentRepositoryException e) {
      logger.error("Error loading {} {} '{}' from {}: {}", new Object[] {
          language,
          resourceType,
          resource,
          this,
          e.getMessage() });
      logger.error(e.getMessage(), e);
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

      return null;
    } catch (IOException e) {
      logger.warn("Error creating preview for {} '{}': {}", new Object[] {
          resourceType,
          resourceURI,
          e.getMessage() });
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

      return null;
    } catch (Throwable t) {
      logger.warn("Error creating preview for {} '{}': {}", new Object[] {
          resourceType,
          resourceURI,
          t.getMessage() });
      IOUtils.closeQuietly(resourceInputStream);

      File f = scaledResourceFile;
      while (f != null && f.isDirectory() && f.listFiles().length == 0) {
        FileUtils.deleteQuietly(f);
        f = f.getParentFile();
      }

      return null;
    } finally {
      IOUtils.closeQuietly(contentRepositoryIs);
      IOUtils.closeQuietly(fos);
    }
  }

  /**
   * Deletes the previews for this resource in all languages and for all known
   * image styles.
   * 
   * @param uri
   *          the resource uri
   */
  protected void deletePreviews(ResourceURI uri) {
    deletePreviews(uri, null);
  }

  /**
   * Deletes the previews for this resource in the given languages and for all
   * known image styles.
   * 
   * @param uri
   *          the resource uri
   * @param language
   *          the language
   */
  protected void deletePreviews(ResourceURI uri, Language language) {
    // Compile the full list of image styles
    List<ImageStyle> styles = new ArrayList<ImageStyle>();
    styles.addAll(imageStyleTracker.getImageStyles());
    for (Module m : getSite().getModules()) {
      styles.addAll(Arrays.asList(m.getImageStyles()));
    }

    for (ImageStyle style : styles) {
      File dir = new File(PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", site.getIdentifier(), "images", style.getIdentifier(), uri.getIdentifier()));
      if (language != null)
        dir = new File(dir, language.getIdentifier());
      FileUtils.deleteQuietly(dir);
    }
  }

}
