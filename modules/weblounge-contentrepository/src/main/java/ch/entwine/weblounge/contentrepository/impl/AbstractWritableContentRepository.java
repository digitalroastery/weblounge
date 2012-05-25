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

import ch.entwine.weblounge.cache.ResponseCacheTracker;
import ch.entwine.weblounge.common.content.PreviewGenerator;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.image.ImagePreviewGenerator;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.repository.ContentRepository;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryOperationListener;
import ch.entwine.weblounge.common.content.repository.DeleteContentOperation;
import ch.entwine.weblounge.common.content.repository.DeleteOperation;
import ch.entwine.weblounge.common.content.repository.LockOperation;
import ch.entwine.weblounge.common.content.repository.MoveOperation;
import ch.entwine.weblounge.common.content.repository.PutContentOperation;
import ch.entwine.weblounge.common.content.repository.PutOperation;
import ch.entwine.weblounge.common.content.repository.ReferentialIntegrityException;
import ch.entwine.weblounge.common.content.repository.UnlockOperation;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.request.CacheTagImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.ResponseCache;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;
import ch.entwine.weblounge.contentrepository.impl.operation.DeleteOperationImpl;
import ch.entwine.weblounge.contentrepository.impl.operation.PutOperationImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Abstract base implementation of a <code>WritableContentRepository</code>.
 */
public abstract class AbstractWritableContentRepository extends AbstractContentRepository implements WritableContentRepository {

  /** The logging facility */
  static final Logger logger = LoggerFactory.getLogger(AbstractWritableContentRepository.class);

  /** Holds pages while they are written to the index */
  protected OperationsScheduler operationsScheduler = new OperationsScheduler();

  /** The image style tracker */
  private ImageStyleTracker imageStyleTracker = null;

  /** The response cache tracker */
  private ResponseCacheTracker responseCacheTracker = null;

  /** The environment tracker */
  private EnvironmentTracker environmentTracker = null;

  /** True to create a homepage when an empty repository is started */
  protected boolean createHomepage = true;

  /** the preview generator creates PNG's by default */
  private static final String PREVIEW_FORMAT = "png";

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

    if (createHomepage) {
      createHomepage();
    }

    Bundle bundle = loadBundle(site);
    if (bundle != null) {
      imageStyleTracker = new ImageStyleTracker(bundle.getBundleContext());
      imageStyleTracker.open();
      responseCacheTracker = new ResponseCacheTracker(bundle.getBundleContext(), site.getIdentifier());
      responseCacheTracker.open();
      environmentTracker = new EnvironmentTracker(bundle.getBundleContext(), this);
      environmentTracker.open();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#disconnect()
   */
  @Override
  public void disconnect() throws ContentRepositoryException {
    super.disconnect();

    // Make sure the bundle is still active. If not, unregistering the trackers
    // below will throw an IllegalStateException
    Bundle bundle = loadBundle(site);
    if (bundle == null || bundle.getState() != Bundle.ACTIVE)
      return;

    // Close the image style tracker
    if (imageStyleTracker != null) {
      imageStyleTracker.close();
      imageStyleTracker = null;
    }

    // Close the cache tracker
    if (responseCacheTracker != null) {
      responseCacheTracker.close();
      responseCacheTracker = null;
    }

    // Close the environment tracker
    if (environmentTracker != null) {
      environmentTracker.close();
      environmentTracker = null;
    }
  }

  /**
   * Returns the site's response cache.
   * 
   * @return the cache
   */
  protected ResponseCache getCache() {
    if (responseCacheTracker == null)
      return null;
    return responseCacheTracker.getCache();
  }

  /**
   * Creates an empty homepage in the content repository if it doesn't exist
   * yet.
   * 
   * @throws IllegalStateException
   * @throws ContentRepositoryException
   */
  protected void createHomepage() throws IllegalStateException,
      ContentRepositoryException {
    // Make sure there is a home page
    ResourceURI homeURI = new ResourceURIImpl(Page.TYPE, site, "/");
    if (!existsInAnyVersion(homeURI)) {
      try {
        Page page = new PageImpl(homeURI);
        User siteAdmininstrator = new UserImpl(site.getAdministrator());
        page.setTemplate(site.getDefaultTemplate().getIdentifier());
        page.setCreated(siteAdmininstrator, new Date());
        page.setPublished(siteAdmininstrator, new Date(), null);
        put(page);
        logger.info("Created homepage for {}", site.getIdentifier());
      } catch (IOException e) {
        logger.warn("Error creating home page in empty site '{}': {}", site.getIdentifier(), e.getMessage());
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#get(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @Override
  public Resource<?> get(ResourceURI uri) throws ContentRepositoryException {
    // Check if resource is in temporary cache
    Resource<?> resource = null;
    synchronized (operationsScheduler) {
      resource = operationsScheduler.getCurrentResource(uri);
      if (resource != null)
        return resource;
    }

    // If not, have the super implementation get the content for us
    return super.get(uri);
  }

  public <T extends ResourceContent> Resource<T> lock(ResourceURI uri, User user)
      throws IllegalStateException, ContentRepositoryException, IOException {

    // Update all resources in memory
    Resource<?> resource = null;
    Date date = new Date();
    for (ResourceURI u : getVersions(uri)) {
      Resource<?> r = get(u);
      r.lock(user);
      r.setModified(user, date);
      operationsScheduler.enqueue(u, new PutOperationImpl(r));
      put(r, false);
      if (r.getVersion() == uri.getVersion())
        resource = r;
    }

    return returnedResource;

    Future<Resource<?>> futureResource = lock(uri, user, null);
    try {
      return futureResource.get();
    } catch (Throwable t) {
      if (t.getCause() instanceof ContentRepositoryException)
        throw (ContentRepositoryException) t.getCause();
      else if (t.getCause() instanceof IOException)
        throw (IOException) t.getCause();
      throw new ContentRepositoryException(t.getCause());
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#lockAsynchronously(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.security.User)
   */
  public synchronized <T extends ResourceContent> LockOperation<T> lockAsynchronously(
      final ResourceURI uri, final User user) throws IOException,
      ContentRepositoryException, IllegalStateException {

    // Execute the actual storing of the resource
    final Resource<?> returnedResource = resource;
    FutureTask<Resource<?>> task = new FutureTask<Resource<?>>(new Callable<Resource<?>>() {
      public Resource<?> call() throws Exception {
        for (Resource<?> r : resourcesToUpdate) {
          try {
            put(r, false);
          } catch (Throwable t) {
            throw new ExecutionException(t);
          }
        }
        return returnedResource;
      }
    });

    new Thread(task).start();
    return task;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#unlock(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.security.User)
   */
  public synchronized <T extends ResourceContent> Resource<T> unlock(
      ResourceURI uri, User user) throws ContentRepositoryException,
      IllegalStateException, IOException {
    try {
      Future<Resource<?>> futureResource = unlock(uri, user, null);
      return futureResource.get();
    } catch (Throwable t) {
      if (t.getCause() instanceof ContentRepositoryException)
        throw (ContentRepositoryException) t.getCause();
      else if (t.getCause() instanceof IOException)
        throw (IOException) t.getCause();
      throw new ContentRepositoryException(t.getCause());
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#unlockAsynchronously(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.security.User)
   */
  public synchronized <T extends ResourceContent> UnlockOperation<T> unlockAsynchronously(
      final ResourceURI uri, final User user) throws IOException,
      ContentRepositoryException {

    // Update all resources in memory
    final List<Resource<?>> resourcesToUpdate = new ArrayList<Resource<?>>();
    Resource<?> resource = null;
    Date date = new Date();
    for (ResourceURI u : getVersions(uri)) {
      Resource<?> r = get(u);
      r.unlock();
      r.setModified(user, date);
      resourcesToUpdate.add(r);
      if (r.getVersion() == uri.getVersion())
        resource = r;
    }

    // Execute the actual storing of the resource
    final Resource<?> returnedResource = resource;
    FutureTask<Resource<?>> task = new FutureTask<Resource<?>>(new Callable<Resource<?>>() {
      public Resource<?> call() throws Exception {
        for (Resource<?> r : resourcesToUpdate) {
          try {
            put(r, false);
          } catch (Throwable t) {
            throw new ExecutionException(t);
          }
        }
        return returnedResource;
      }
    });

    new Thread(task).start();
    return task;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#isLocked(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public boolean isLocked(ResourceURI uri) throws ContentRepositoryException {
    Resource<?> r = get(uri);
    return r.isLocked();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#delete(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public synchronized boolean delete(ResourceURI uri)
      throws ContentRepositoryException, IOException {
    return delete(uri, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#deleteAsynchronously(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public synchronized DeleteOperation deleteAsynchronously(final ResourceURI uri)
      throws ContentRepositoryException, IOException {
    FutureTask<Boolean> task = new FutureTask<Boolean>(new Callable<Boolean>() {
      public Boolean call() throws Exception {
        return delete(uri, false);
      }
    });
    new Thread(task).start();
    return task;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#delete(ch.entwine.weblounge.common.content.ResourceURI,
   *      boolean)
   */
  public synchronized boolean delete(ResourceURI uri, boolean allRevisions)
      throws ContentRepositoryException, IOException {

    // Let's wait for the outcome of this operation
    ContentRepositoryOperationListener listener = new NotifyingOperationListener();
    DeleteOperationImpl op = new DeleteOperationImpl(uri, allRevisions);
    op.addOperationListener(listener);
    synchronized (listener) {
      operationsScheduler.enqueue(uri, op);
      try {
        listener.wait();
      } catch (InterruptedException e) {
        logger.warn("Interrupted while waiting for the deletion of {}", uri);
        return false;
      }
    }
    return op.getResult();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#deleteAsynchronously(ch.entwine.weblounge.common.content.ResourceURI,
   *      boolean)
   */
  public DeleteOperation deleteAsynchronously(ResourceURI uri,
      boolean allRevisions) throws ContentRepositoryException, IOException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // See if the resource exists
    if (allRevisions && !index.existsInAnyVersion(uri) && operationsScheduler.containsVersionOf(uri)) {
      logger.warn("Resource '{}' not found in repository index", uri);
      return false;
    }

    // Make sure the resource is not being referenced elsewhere
    if (allRevisions || uri.getVersion() == Resource.LIVE) {
      SearchQuery searchByResource = new SearchQueryImpl(uri.getSite());
      searchByResource.withVersion(Resource.LIVE);
      searchByResource.withProperty("resourceid", uri.getIdentifier());
      if (index.find(searchByResource).getItems().length > 0) {
        logger.debug("Resource '{}' is still being referenced", uri);
        throw new ReferentialIntegrityException(uri.getIdentifier());
      }
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
      synchronized (operationsScheduler) {
        try {
          index.delete(new ResourceURIImpl(uri, revision));
        } finally {
          operationsScheduler.remove(new ResourceURIImpl(uri, revision));
        }
      }
    }

    // Delete previews
    deletePreviews(uri);

    // Make sure related stuff gets thrown out of the cache
    ResponseCache cache = getCache();
    if (cache != null && (allRevisions || uri.getVersion() == Resource.LIVE)) {
      cache.invalidate(new CacheTag[] { new CacheTagImpl(CacheTag.Resource, uri.getIdentifier()) }, true);
    }

    return true;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#move(ch.entwine.weblounge.common.content.ResourceURI,
   *      String, boolean)
   */
  public synchronized void move(ResourceURI uri, String targetPath,
      boolean moveChildren) throws IOException, ContentRepositoryException {
    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    String originalPathPrefix = uri.getPath();
    if (originalPathPrefix == null)
      throw new IllegalArgumentException("Cannot move resource with null path");
    if (StringUtils.isEmpty(targetPath))
      throw new IllegalArgumentException("Cannot move resource to empty path");
    if (!targetPath.startsWith("/"))
      throw new IllegalArgumentException("Cannot move resource to relative path '" + targetPath + "'");
    if (originalPathPrefix.equals(targetPath))
      return;

    // Locate the resources to move
    Set<ResourceURI> documentsToMove = new HashSet<ResourceURI>();
    documentsToMove.add(uri);

    // Also move children?
    if (moveChildren) {
      SearchQuery q = new SearchQueryImpl(site).withPreferredVersion(Resource.LIVE);
      q.withPathPrefix(originalPathPrefix);

      SearchResult result = index.find(q);
      if (result.getDocumentCount() == 0) {
        logger.warn("Trying to move non existing resource {}", uri);
        return;
      }

      // We need to check the prefix again, since the search query will also
      // match parts of the originalPathPrefix
      for (SearchResultItem searchResult : result.getItems()) {
        if (!(searchResult instanceof ResourceSearchResultItem))
          continue;
        ResourceSearchResultItem rsri = (ResourceSearchResultItem) searchResult;
        String resourcePath = rsri.getResourceURI().getPath();

        // Add the document if the paths match and it is not already contained
        // in our list (never mind path and version, just look at the id)
        if (resourcePath != null && resourcePath.startsWith(originalPathPrefix)) {
          boolean existing = false;
          for (ResourceURI u : documentsToMove) {
            if (u.getIdentifier().equals(rsri.getResourceURI().getIdentifier())) {
              existing = true;
              break;
            }
          }
          if (!existing)
            documentsToMove.add(rsri.getResourceURI());
        }
      }
    }

    // TODO: Include resources that are not part of the search index

    // Finally, move all resources
    for (ResourceURI u : documentsToMove) {
      String originalPath = u.getPath();
      String pathSuffix = originalPath.substring(originalPathPrefix.length());
      String newPath = null;

      // Is the original path just a prefix, or is it an exact match?
      if (StringUtils.isNotBlank(pathSuffix))
        newPath = UrlUtils.concat(targetPath, pathSuffix);
      else
        newPath = targetPath;

      // Move every version of the resource, since we want the path to be
      // in sync across resource versions
      for (long version : index.getRevisions(u)) {
        ResourceURI candidateURI = new ResourceURIImpl(u.getType(), site, null, u.getIdentifier(), version);

        // Load the resource, adjust the path and store it again
        Resource<?> r = get(candidateURI);

        // Store the updated resource
        r.getURI().setPath(newPath);
        storeResource(r);

        // Update the index
        r.getURI().setPath(originalPath);
        index.move(r.getURI(), newPath);

        // Create the preview images
        if (connected && !initializing)
          createPreviews(r);
      }
    }

    // Make sure related stuff gets thrown out of the cache
    ResponseCache cache = getCache();
    if (cache != null) {
      cache.invalidate(new CacheTag[] { new CacheTagImpl(CacheTag.Resource, uri.getIdentifier()) }, true);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#moveAsynchronously(ch.entwine.weblounge.common.content.ResourceURI,
   *      java.lang.String, boolean)
   */
  public synchronized MoveOperation moveAsynchronously(final ResourceURI uri,
      final String path, final boolean moveChildren)
      throws ContentRepositoryException, IOException {
    FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
      public Void call() throws Exception {
        move(uri, path, moveChildren);
        return null;
      }
    });
    new Thread(task).start();
    return task;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#put(ch.entwine.weblounge.common.content.Resource)
   */
  public synchronized <T extends ResourceContent> Resource<T> put(
      Resource<T> resource) throws ContentRepositoryException, IOException,
      IllegalStateException {
    ContentRepositoryOperationListener listener = new NotifyingOperationListener();
    synchronized (listener) {
      resource = put(resource, listener);
      try {
        listener.wait();
      } catch (InterruptedException e) {
        logger.warn("Interrupted while waiting on a put operation for {}", resource.getIdentifier());
      }
    }
    return put(resource, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#putAsynchronously(ch.entwine.weblounge.common.content.Resource)
   */
  public synchronized <T extends ResourceContent> PutOperation<T> putAsynchronously(
      final Resource<T> resource) throws ContentRepositoryException,
      IOException, IllegalStateException {
    final ResourceURI uri = resource.getURI();

    // Make sure others know about this update
    synchronized (operationsScheduler) {
      operationsScheduler.enqueue(uri, new PutOperationImpl(resource));
    }

    FutureTask<Resource<T>> task = new FutureTask<Resource<T>>(new Callable<Resource<T>>() {
      public Resource<T> call() throws Exception {
        Resource<T> returnVal = null;
        try {
          returnVal = put(resource, true);
        } finally {
          synchronized (operationsScheduler) {
            operationsScheduler.remove(uri);
          }
        }
        return returnVal;
      }
    });
    new Thread(task).start();
    return task;
  }

  /**
   * Updates the resource and optionally updates the resource's previews.
   * 
   * @param resource
   *          the resource
   * @param updatePreviews
   *          <code>true</code> to update the previews
   * @return the updated resource
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if adding fails due to a database error
   * @throws IllegalStateException
   *           if the resource does not exist and contains a non-empty content
   *           section.
   * @throws IllegalStateException
   *           if the resource exists but contains different resource content
   *           than what is specified in the updated document
   */
  protected <T extends ResourceContent> Resource<T> put(Resource<T> resource,
      boolean updatePreviews) throws ContentRepositoryException, IOException,
      IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    ResourceURI uri = resource.getURI();

    // Add entry to index
    if (!index.existsInAnyVersion(uri) || operationsScheduler.containsVersionOf(uri)) {
      if (resource.contents().size() > 0)
        throw new IllegalStateException("Cannot add content metadata without content");
      index.add(resource);
    }

    // The resource exists in some version
    else {
      logger.debug("Checking content section of existing {} {}", resource.getType(), resource);
      Resource<?> r = get(uri);

      // Does the resource exist in this version?
      if (r != null) {
        if (resource.contents().size() != r.contents().size())
          throw new IllegalStateException("Cannot modify content metadata without content");
        index.update(resource);
      }

      // We are about to add a new version of a resource
      else {
        if (resource.contents().size() > 0)
          throw new IllegalStateException("Cannot modify content metadata without content");
        index.add(resource);
      }
    }

    // Make sure related stuff gets thrown out of the cache
    ResponseCache cache = getCache();
    if (cache != null && updatePreviews && uri.getVersion() == Resource.LIVE) {
      List<CacheTag> tags = new ArrayList<CacheTag>();

      // resource id
      tags.add(new CacheTagImpl(CacheTag.Resource, uri.getIdentifier()));

      // subjects, so that resource lists get updated
      for (String subject : resource.getSubjects())
        tags.add(new CacheTagImpl(CacheTag.Subject, subject));

      cache.invalidate(tags.toArray(new CacheTagImpl[tags.size()]), true);
    }

    // Write the updated resource to disk
    storeResource(resource);

    // Create the preview images. Don't if the site is currently being created.
    if (updatePreviews && connected && !initializing)
      createPreviews(resource);

    return resource;
  }

  /**
   * This method makes sure that any asynchronous processing that is happening
   * to related resources (resources with the same id and potentially different
   * version) will be finished first.
   * 
   * @param uri
   *          the resource uri
   * @param differentVersionsOnly
   *          <code>true</code> to match only versions that are not equal to the
   *          one returned by <code>uri</code>.
   * @param addToCache
   *          <code>true</code> if <code>uri</code> should be added to the put
   *          cache
   */
  private void waitForRelatedProcessing(ResourceURI uri,
      boolean differentVersionsOnly, boolean addToCache) {
    boolean relatedResourceInPutCache = true;
    while (relatedResourceInPutCache) {
      relatedResourceInPutCache = false;
      synchronized (operationsScheduler) {
        for (ResourceURI u : operationsScheduler.keySet()) {
          if (u.getIdentifier().equals(uri.getIdentifier()) && u.getVersion() != uri.getVersion()) {
            relatedResourceInPutCache = true;
            break;
          }
        }
      }
      try {
        Thread.sleep(200);
      } catch (Throwable t) {
        // Just keep going
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#putContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      java.io.InputStream)
   */
  @SuppressWarnings("unchecked")
  public synchronized <T extends ResourceContent> Resource<T> putContent(
      ResourceURI uri, T content, InputStream is)
      throws ContentRepositoryException, IOException, IllegalStateException {

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
    if (connected && !initializing)
      createPreviews(resource);

    // Make sure related stuff gets thrown out of the cache
    ResponseCache cache = getCache();
    if (cache != null) {
      cache.invalidate(new CacheTag[] { new CacheTagImpl(CacheTag.Resource, uri.getIdentifier()) }, true);
    }

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#putContentAsynchronously(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      java.io.InputStream)
   */
  public synchronized <T extends ResourceContent> PutContentOperation<T> putContentAsynchronously(
      final ResourceURI uri, final T content, final InputStream is)
      throws ContentRepositoryException, IOException, IllegalStateException {
    FutureTask<Resource<T>> task = new FutureTask<Resource<T>>(new Callable<Resource<T>>() {
      public Resource<T> call() throws Exception {
        return putContent(uri, content, is);
      }
    });
    new Thread(task).start();
    return task;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#deleteContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent)
   */
  public synchronized <T extends ResourceContent> Resource<T> deleteContent(
      ResourceURI uri, T content) throws ContentRepositoryException,
      IOException, IllegalStateException {

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

    // Make sure related stuff gets thrown out of the cache
    ResponseCache cache = getCache();
    if (cache != null) {
      cache.invalidate(new CacheTag[] { new CacheTagImpl(CacheTag.Resource, uri.getIdentifier()) }, true);
    }

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#deleteContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      ch.entwine.weblounge.common.content.repository.ContentRepositoryOperationListener)
   */
  public synchronized <T extends ResourceContent> DeleteContentOperation<T> deleteContentAsynchronously(
      final ResourceURI uri, final T content)
      throws ContentRepositoryException, IOException, IllegalStateException {
    FutureTask<Resource<T>> task = new FutureTask<Resource<T>>(new Callable<Resource<T>>() {
      public Resource<T> call() throws Exception {
        return deleteContent(uri, content);
      }
    });
    new Thread(task).start();
    return task;
  };

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#getVersions(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @Override
  public ResourceURI[] getVersions(ResourceURI uri)
      throws ContentRepositoryException {
    Set<ResourceURI> uris = new HashSet<ResourceURI>(Arrays.asList(super.getVersions(uri)));
    synchronized (operationsScheduler) {
      for (ResourceURI u : operationsScheduler.keySet()) {
        if (u.getIdentifier().equals(uri.getIdentifier()) && operationsScheduler.getCurrentResource(uri) != null)
          uris.add(u);
      }
    }
    return uris.toArray(new ResourceURI[uris.size()]);
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
   * @param languages
   *          the languages to build the previews for
   */
  protected void createPreviews(final Resource<?> resource,
      Language... languages) {

    // Compile the full list of image styles
    final List<ImageStyle> styles = new ArrayList<ImageStyle>();
    if (imageStyleTracker != null)
      styles.addAll(imageStyleTracker.getImageStyles());
    for (Module m : getSite().getModules()) {
      styles.addAll(Arrays.asList(m.getImageStyles()));
    }

    // If no language has been specified, we create the preview for all
    // languages
    if (languages == null || languages.length == 0) {
      languages = resource.getURI().getSite().getLanguages();
    }

    // Create the previews
    for (Language language : languages) {
      PreviewGeneratorWorker previewWorker = new PreviewGeneratorWorker(this, resource, environment, language, styles, PREVIEW_FORMAT);
      Thread t = new Thread(previewWorker);
      t.start();
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
    if (imageStyleTracker != null)
      styles.addAll(imageStyleTracker.getImageStyles());
    for (Module m : getSite().getModules()) {
      styles.addAll(Arrays.asList(m.getImageStyles()));
    }

    for (ImageStyle style : styles) {
      File styledImage = null;

      // Create the path to a sample image
      if (language != null) {
        styledImage = ImageStyleUtils.getScaledFile(uri, "test." + PREVIEW_FORMAT, language, style);
      } else {
        styledImage = ImageStyleUtils.getScaledFile(uri, "test." + PREVIEW_FORMAT, LanguageUtils.getLanguage("en"), style);
        styledImage = styledImage.getParentFile();
      }

      // Remove the parent's directory, which will include the specified
      // previews
      File dir = styledImage.getParentFile();
      logger.debug("Deleting previews in {}", dir.getAbsolutePath());
      FileUtils.deleteQuietly(dir);
    }
  }

  /**
   * Worker implementation that creates a preview in a separate thread.
   */
  private static class PreviewGeneratorWorker implements Runnable {

    private ContentRepository contentRepository = null;
    private Resource<?> resource = null;
    private List<ImageStyle> styles = null;
    private Environment environment = null;
    private Language language = null;
    private String format = null;

    /**
     * Creates a new preview worker who will create the corresponding previews
     * for the given resource and style.
     * 
     * @param resource
     *          the resource
     * @param environment
     *          the current environment
     * @param language
     *          the language
     * @param styles
     *          the image styles
     */
    public PreviewGeneratorWorker(ContentRepository repository,
        Resource<?> resource, Environment environment, Language language,
        List<ImageStyle> styles, String format) {
      this.contentRepository = repository;
      this.resource = resource;
      this.environment = environment;
      this.language = language;
      this.styles = styles;
      this.format = format;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
      ResourceURI resourceURI = resource.getURI();
      String resourceType = resourceURI.getType();

      // Find the resource serializer
      ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
      if (serializer == null) {
        logger.warn("Unable to index resources of type '{}': no resource serializer found", resourceType);
        return;
      }

      // Does the serializer come with a preview generator?
      PreviewGenerator previewGenerator = serializer.getPreviewGenerator(resource);
      if (previewGenerator == null) {
        logger.debug("Resource type '{}' does not support previews", resourceType);
        return;
      }

      // Create the original preview image for every language
      ImageStyle original = new ImageStyleImpl("original");
      File file = createPreview(resource, original, language, previewGenerator, format);
      if (file == null || !file.exists() || file.length() == 0) {
        logger.debug("Preview generation for {} failed", resource);
        return;
      }

      // Create the scaled images
      String mimeType = "image/" + format;
      ResourceSerializer<?, ?> s = ResourceSerializerFactory.getSerializerByMimeType(mimeType);
      if (s == null) {
        logger.warn("No resource serializer is capable of dealing with resources of format '{}'", mimeType);
        return;
      } else if (!(s instanceof ImageResourceSerializer)) {
        logger.warn("Resource serializer lookup for format '{}' returned {}", format, s.getClass());
        return;
      }

      // Find us an image serializer
      ImageResourceSerializer irs = (ImageResourceSerializer) s;
      ImagePreviewGenerator imagePreviewGenerator = (ImagePreviewGenerator) irs.getPreviewGenerator(format);
      if (imagePreviewGenerator == null) {
        logger.warn("Image resource serializer {} does not provide support for '{}'", irs, format);
        return;
      }

      // Now scale the original preview according to the existing styles
      for (ImageStyle style : styles) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
          fis = new FileInputStream(file);
          File scaledFile = ImageStyleUtils.createScaledFile(resourceURI, file.getName(), language, style);
          fos = new FileOutputStream(scaledFile);
          imagePreviewGenerator.createPreview(file, environment, language, style, resourceType, fis, fos);
        } catch (Throwable t) {
          logger.error("Error scaling {}: {}", file, t.getMessage());
          continue;
        } finally {
          IOUtils.closeQuietly(fis);
          IOUtils.closeQuietly(fos);
        }
      }

    }

    /**
     * Creates the actual preview.
     * 
     * @param resource
     *          the resource
     * @param style
     *          the image style
     * @param language
     *          the language
     * @param previewGenerator
     *          the preview generator
     * @param the
     *          preview format
     * @return returns the preview file
     */
    private File createPreview(Resource<?> resource, ImageStyle style,
        Language language, PreviewGenerator previewGenerator, String format) {

      ResourceURI resourceURI = resource.getURI();
      String resourceType = resourceURI.getType();

      // Create the filename
      ResourceContent content = resource.getContent(language);
      String filename = content != null ? content.getFilename() : resource.getIdentifier();
      String suffix = previewGenerator.getSuffix(resource, language, style);
      filename = FilenameUtils.getBaseName(filename) + "." + suffix;

      // Initiate creation of previews
      InputStream resourceInputStream = null;
      InputStream contentRepositoryIs = null;
      FileOutputStream fos = null;
      File scaledResourceFile = null;

      try {
        scaledResourceFile = ImageStyleUtils.createScaledFile(resourceURI, filename.toString(), language, style);

        // Find the modification date
        long lastModified = ResourceUtils.getModificationDate(resource, language).getTime();

        // Create the file if it doesn't exist or if it is outdated
        if (!scaledResourceFile.isFile() || scaledResourceFile.lastModified() < lastModified) {
          contentRepositoryIs = contentRepository.getContent(resourceURI, language);

          // Is this local content?
          if (contentRepositoryIs == null && content != null && content.getExternalLocation() != null) {
            contentRepositoryIs = content.getExternalLocation().openStream();
          }

          fos = new FileOutputStream(scaledResourceFile);
          logger.debug("Creating preview of '{}' at {}", resource, scaledResourceFile);

          previewGenerator.createPreview(resource, environment, language, style, format, contentRepositoryIs, fos);
          if (scaledResourceFile.length() > 0) {
            scaledResourceFile.setLastModified(lastModified);
          } else {
            File f = scaledResourceFile;
            while (f != null && f.isDirectory() && f.listFiles().length == 0) {
              FileUtils.deleteQuietly(f);
              f = f.getParentFile();
            }
          }
        }

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

      } finally {
        IOUtils.closeQuietly(contentRepositoryIs);
        IOUtils.closeQuietly(fos);
      }

      return scaledResourceFile;
    }

  }

  /**
   * This class is used as a way to keep track of what has been added to the
   * repository but has not been flushed to disk.
   */
  public static final class OperationsScheduler extends HashMap<ResourceURI, List<ContentRepositoryOperation>> {

    /** Serial version uid */
    private static final long serialVersionUID = -2733584523568321320L;

    /** The operations counter */
    private Map<ResourceURI, List<ContentRepositoryOperation>> operationsPerResource = new HashMap<ResourceURI, List<ContentRepositoryOperation>>();

    /** The repository operations */
    private List<ContentRepositoryOperation<?>> repositoryOperations = new ArrayList<ContentRepositoryOperation<?>>();

    /**
     * Returns <code>true</code> if the cache contains the uri itself or a
     * different version of it.
     * 
     * @param uri
     *          the uri
     * @return <code>true</code> if it contains a version of this resource
     */
    public synchronized boolean containsVersionOf(ResourceURI uri) {
      for (ResourceURI u : keySet()) {
        if (u.getIdentifier().equals(uri.getIdentifier()))
          return true;
      }
      return false;
    }

    /**
     * Returns the current state of the resource, assuming that all currently
     * queued operations succeed.
     * 
     * @param uri
     *          the resource uri
     * @return the current resource
     */
    public synchronized Resource<?> getCurrentResource(ResourceURI uri) {
      List<ContentRepositoryOperation> operations = get(uri);

      // Walk through the list of operations from end to beginning
      for (int i = operations.size() - 1; i >= 0; i--) {
        ContentRepositoryOperation op = operations.get(i);
        if (op instanceof DeleteOperation)
          return null;
        Resource<?> resource = op.getResource();
        if (resource != null)
          return resource;
      }

      return null;
    }

    /**
     * TODO: Document me
     * 
     * @param operation
     */
    public synchronized void enqueue(
ContentRepositoryOperation<?> operation) {
      repositoryOperations.add(operation);
    }

    /**
     * Adds <code>operation</code> to the list of currently enqueued operations
     * for the given resource and returns the resulting list of operations.
     * 
     * @param uri
     *          the resource uri
     * @param operation
     *          the operation that needs to be executed
     * @return the resource
     */
    public synchronized List<ContentRepositoryOperation> enqueue(
        ResourceURI uri, ContentRepositoryOperation operation) {
      List<ContentRepositoryOperation> operations = get(uri);
      if (uri == null) {
        operations = new ArrayList<ContentRepositoryOperation>();
        put(uri, operations);
      }
      operations.add(operation);
      return operations;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.util.HashMap#clear()
     */
    @Override
    public synchronized void clear() {
      operationsPerResource.clear();
      super.clear();
    }

  }

}
