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

import static ch.entwine.weblounge.common.content.ResourceUtils.equalsByIdOrPath;

import ch.entwine.weblounge.cache.ResponseCacheTracker;
import ch.entwine.weblounge.common.content.MalformedResourceURIException;
import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceReader;
import ch.entwine.weblounge.common.content.ResourceSearchResultItem;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.SearchQuery;
import ch.entwine.weblounge.common.content.SearchResult;
import ch.entwine.weblounge.common.content.SearchResultItem;
import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.content.page.Page;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryResourceOperation;
import ch.entwine.weblounge.common.content.repository.DeleteContentOperation;
import ch.entwine.weblounge.common.content.repository.DeleteOperation;
import ch.entwine.weblounge.common.content.repository.IndexOperation;
import ch.entwine.weblounge.common.content.repository.LockOperation;
import ch.entwine.weblounge.common.content.repository.MoveOperation;
import ch.entwine.weblounge.common.content.repository.PutContentOperation;
import ch.entwine.weblounge.common.content.repository.PutOperation;
import ch.entwine.weblounge.common.content.repository.ReferentialIntegrityException;
import ch.entwine.weblounge.common.content.repository.UnlockOperation;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.common.impl.content.ResourceURIImpl;
import ch.entwine.weblounge.common.impl.content.SearchQueryImpl;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleUtils;
import ch.entwine.weblounge.common.impl.content.page.PageImpl;
import ch.entwine.weblounge.common.impl.language.LanguageUtils;
import ch.entwine.weblounge.common.impl.request.CacheTagImpl;
import ch.entwine.weblounge.common.impl.security.UserImpl;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.request.CacheTag;
import ch.entwine.weblounge.common.request.ResponseCache;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Module;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.common.url.PathUtils;
import ch.entwine.weblounge.common.url.UrlUtils;
import ch.entwine.weblounge.contentrepository.ResourceSerializer;
import ch.entwine.weblounge.contentrepository.ResourceSerializerFactory;
import ch.entwine.weblounge.contentrepository.VersionedContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.fs.FileSystemContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;
import ch.entwine.weblounge.contentrepository.impl.operation.CurrentOperation;
import ch.entwine.weblounge.contentrepository.impl.operation.DeleteContentOperationImpl;
import ch.entwine.weblounge.contentrepository.impl.operation.DeleteOperationImpl;
import ch.entwine.weblounge.contentrepository.impl.operation.IndexOperationImpl;
import ch.entwine.weblounge.contentrepository.impl.operation.LockOperationImpl;
import ch.entwine.weblounge.contentrepository.impl.operation.MoveOperationImpl;
import ch.entwine.weblounge.contentrepository.impl.operation.PutContentOperationImpl;
import ch.entwine.weblounge.contentrepository.impl.operation.PutOperationImpl;
import ch.entwine.weblounge.contentrepository.impl.operation.UnlockOperationImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
import java.util.UUID;

/**
 * Abstract base implementation of a <code>WritableContentRepository</code>.
 */
public abstract class AbstractWritableContentRepository extends AbstractContentRepository implements WritableContentRepository {

  /** The logging facility */
  static final Logger logger = LoggerFactory.getLogger(AbstractWritableContentRepository.class);

  /** Name of the index path element right below the repository root */
  public static final String INDEX_PATH = "index";

  /** Holds pages while they are written to the index */
  protected OperationProcessor processor = null;

  /** The image style tracker */
  private ImageStyleTracker imageStyleTracker = null;

  /** The response cache tracker */
  private ResponseCacheTracker responseCacheTracker = null;

  /** The environment tracker */
  private EnvironmentTracker environmentTracker = null;

  /** True to create a homepage when an empty repository is started */
  protected boolean createHomepage = true;

  /** The repository storage root directory */
  protected File repositoryRoot = null;

  /** The repository root directory */
  protected File repositorySiteRoot = null;

  /** The root directory for the temporary bundle index */
  protected File idxRootDir = null;

  /** Flag to indicate off-site indexing */
  protected boolean indexingOffsite = false;

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
    processor = new OperationProcessor(this);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#connect(ch.entwine.weblounge.common.site.Site)
   */
  @Override
  public void connect(Site site) throws ContentRepositoryException {
    repositorySiteRoot = new File(repositoryRoot, site.getIdentifier());
    logger.debug("Content repository root is located at {}", repositorySiteRoot);

    // Make sure we can create a temporary index
    idxRootDir = new File(repositorySiteRoot, INDEX_PATH);
    try {
      FileUtils.forceMkdir(idxRootDir);
    } catch (IOException e) {
      throw new ContentRepositoryException("Unable to create site index at " + idxRootDir, e);
    }

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

    // Finalize running operations
    processor.stop();

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
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#exists(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @Override
  public boolean exists(ResourceURI uri) throws ContentRepositoryException {
    for (ResourceURI u : getVersions(uri)) {
      if (u.equals(uri))
        return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#existsInAnyVersion(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @Override
  public boolean existsInAnyVersion(ResourceURI uri)
      throws ContentRepositoryException {
    return getVersions(uri).length > 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#getVersions(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @Override
  public ResourceURI[] getVersions(ResourceURI uri)
      throws ContentRepositoryException {
    Set<ResourceURI> uris = new HashSet<ResourceURI>();
    uris.addAll(Arrays.asList(super.getVersions(uri)));

    // Iterate over the resources that are currently being processed
    synchronized (processor) {
      for (ContentRepositoryOperation<?> op : processor.getOperations()) {

        // Is this a resource operation?
        if (!(op instanceof ContentRepositoryResourceOperation<?>))
          continue;

        // Apply the changes to the original resource
        ContentRepositoryResourceOperation<?> resourceOp = (ContentRepositoryResourceOperation<?>) op;

        // Is the resource about to be deleted?
        ResourceURI opURI = resourceOp.getResourceURI();
        if (op instanceof DeleteOperation && equalsByIdOrPath(uri, opURI)) {
          DeleteOperation deleteOp = (DeleteOperation) op;
          List<ResourceURI> deleteCandidates = new ArrayList<ResourceURI>();
          for (ResourceURI u : uris) {
            if (deleteOp.allVersions() || u.getVersion() == opURI.getVersion()) {
              deleteCandidates.add(u);
            }
          }
          uris.removeAll(deleteCandidates);
        }

        // Is the resource simply being updated?
        if (op instanceof PutOperation && equalsByIdOrPath(uri, opURI)) {
          uris.add(opURI);
        }

      }
    }

    return uris.toArray(new ResourceURI[uris.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#get(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @SuppressWarnings("unchecked")
  @Override
  public <R extends Resource<?>> R get(ResourceURI uri)
      throws ContentRepositoryException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Check if resource is in temporary cache and wait until it's clear which
    // version is the latest one
    Resource<?> resource = super.get(uri);

    // Iterate over the resources that are currently being processed
    synchronized (processor) {
      for (ContentRepositoryOperation<?> op : processor.getOperations()) {

        // Is this a resource operation?
        if (!(op instanceof ContentRepositoryResourceOperation<?>))
          continue;

        // Apply the changes to the original resource
        ContentRepositoryResourceOperation<?> resourceOp = (ContentRepositoryResourceOperation<?>) op;
        resource = resourceOp.apply(uri, resource);
      }
    }

    // If we found a resource, let's return it
    return (R) resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#lock(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.security.User)
   */
  public Resource<?> lock(ResourceURI uri, User user)
      throws IllegalStateException, ContentRepositoryException, IOException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Is this a new request or a scheduled asynchronous execution?
    if (!(CurrentOperation.get() instanceof LockOperation)) {
      return lockAsynchronously(uri, user).get();
    }

    // Check if resource is in temporary cache already by another operation
    if (processor.isProcessing(uri)) {
      logger.debug("Resource '{}' is being processed, locking anyway", uri);
    }

    // Update all resources in memory
    Resource<?> resource = null;
    ContentRepositoryOperation<?> lockOperation = CurrentOperation.get();
    for (ResourceURI u : getVersions(uri)) {
      Resource<?> r = get(u);
      r.lock(user);
      PutOperation putOp = new PutOperationImpl(r, false);
      try {
        CurrentOperation.set(putOp);
        put(r, false);
      } finally {
        CurrentOperation.set(lockOperation);
      }
      if (r.getVersion() == uri.getVersion())
        resource = r;
    }

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#lockAsynchronously(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.security.User)
   */
  public LockOperation lockAsynchronously(final ResourceURI uri, final User user)
      throws IOException, ContentRepositoryException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    LockOperation lockOperation = new LockOperationImpl(uri, user);
    processor.enqueue(lockOperation);
    return lockOperation;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#unlock(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.security.User)
   */
  public Resource<?> unlock(ResourceURI uri, User user)
      throws ContentRepositoryException, IllegalStateException, IOException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Is this a new request or a scheduled asynchronous execution?
    if (!(CurrentOperation.get() instanceof UnlockOperation)) {
      return unlockAsynchronously(uri, user).get();
    }

    // Check if resource is in temporary cache already by another operation
    if (processor.isProcessing(uri)) {
      logger.debug("Resource '{}' is being processed, unlocking anyway", uri);
    }

    // Update all resources in memory
    Resource<?> resource = null;
    ContentRepositoryOperation<?> unlockOperation = CurrentOperation.get();
    for (ResourceURI u : getVersions(uri)) {
      Resource<?> r = get(u);
      r.unlock();
      PutOperation putOp = new PutOperationImpl(r, false);
      try {
        CurrentOperation.set(putOp);
        put(r, false);
      } finally {
        CurrentOperation.set(unlockOperation);
      }
      if (r.getVersion() == uri.getVersion())
        resource = r;
    }

    return resource;

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#unlockAsynchronously(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.security.User)
   */
  public UnlockOperation unlockAsynchronously(final ResourceURI uri,
      final User user) throws IOException, ContentRepositoryException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Create an asynchronous operation representation and return it
    UnlockOperation lockOperation = new UnlockOperationImpl(uri, user);
    processor.enqueue(lockOperation);
    return lockOperation;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#isLocked(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public boolean isLocked(ResourceURI uri) throws ContentRepositoryException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    Resource<?> r = get(uri);

    // Check if resource is in temporary cache already by another operation
    if (processor.isProcessing(uri)) {
      logger.debug("Resource '{}' is being processed, return lock status anyway", uri);
    }

    return r.isLocked();
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
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#deleteAsynchronously(ch.entwine.weblounge.common.content.ResourceURI)
   */
  public DeleteOperation deleteAsynchronously(final ResourceURI uri)
      throws ContentRepositoryException, IOException {
    return deleteAsynchronously(uri, false);
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

    // Is this a new request or a scheduled asynchronous execution?
    if (!(CurrentOperation.get() instanceof DeleteOperation)) {
      return deleteAsynchronously(uri, allRevisions).get();
    }

    // Check if resource is in temporary cache already by another operation
    if (processor.isProcessing(uri)) {
      logger.debug("Resource '{}' is being processed, removing anyway", uri);
    }

    // See if the resource exists
    if (allRevisions && !index.existsInAnyVersion(uri) && processor.isProcessingVersionOf(uri)) {
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
      index.delete(new ResourceURIImpl(uri, revision));
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
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#deleteAsynchronously(ch.entwine.weblounge.common.content.ResourceURI,
   *      boolean)
   */
  public DeleteOperation deleteAsynchronously(ResourceURI uri,
      boolean allRevisions) throws ContentRepositoryException, IOException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Create an asynchronous operation representation and return it
    DeleteOperation deleteOperation = new DeleteOperationImpl(uri, allRevisions);
    processor.enqueue(deleteOperation);
    return deleteOperation;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#move(ch.entwine.weblounge.common.content.ResourceURI,
   *      String, boolean)
   */
  public void move(ResourceURI uri, String targetPath, boolean moveChildren)
      throws IOException, ContentRepositoryException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Is this a new request or a scheduled asynchronous execution?
    if (!(CurrentOperation.get() instanceof MoveOperation)) {
      moveAsynchronously(uri, targetPath, moveChildren).get();
      return;
    }

    // Check if resource is in temporary cache already by another operation
    if (processor.isProcessing(uri)) {
      logger.debug("Resource '{}' is being processed, moving anyway", uri);
    }

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
  public MoveOperation moveAsynchronously(final ResourceURI uri,
      final String path, final boolean moveChildren)
          throws ContentRepositoryException, IOException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Create an asynchronous operation representation and return it
    MoveOperation moveOperation = new MoveOperationImpl(uri, path, moveChildren);
    processor.enqueue(moveOperation);
    return moveOperation;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#put(ch.entwine.weblounge.common.content.Resource)
   */
  public Resource<?> put(Resource<?> resource)
      throws ContentRepositoryException, IOException, IllegalStateException {

    return put(resource, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#putAsynchronously(ch.entwine.weblounge.common.content.Resource)
   */
  public PutOperation putAsynchronously(Resource<?> resource)
      throws ContentRepositoryException, IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Create an asynchronous operation representation and return it
    PutOperation putOperation = new PutOperationImpl(resource, true);
    processor.enqueue(putOperation);
    return putOperation;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#putAsynchronously(ch.entwine.weblounge.common.content.Resource)
   */
  public PutOperation putAsynchronously(Resource<?> resource,
      boolean updatePreviews) throws ContentRepositoryException, IOException,
      IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Create an asynchronous operation representation and return it
    PutOperation putOperation = new PutOperationImpl(resource, updatePreviews);
    processor.enqueue(putOperation);
    return putOperation;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#put(ch.entwine.weblounge.common.content.Resource,
   *      boolean)
   */
  public Resource<?> put(Resource<?> resource, boolean updatePreviews)
      throws ContentRepositoryException, IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Is this a new request or a scheduled asynchronous execution?
    if (!(CurrentOperation.get() instanceof PutOperation)) {
      return putAsynchronously(resource, updatePreviews).get();
    }

    // Check if resource is in temporary cache already by another operation
    if (processor.isProcessing(resource.getURI())) {
      logger.debug("Resource '{}' is being processed, putting anyway", resource.getURI());
    }

    ResourceURI uri = resource.getURI();

    // Add entry to index
    if (!index.existsInAnyVersion(uri)) {
      if (resource.contents().size() > 0)
        throw new IllegalStateException("Cannot add content metadata withouC content");
      index.add(resource);
    }

    // The resource exists in some version
    else if (index.exists(uri)) {
      index.update(resource);
    }

    // We are about to add a new version of a resource
    else {
      if (resource.contents().size() > 0)
        throw new IllegalStateException("Cannot modify content metadata withouC content");
      index.add(resource);
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
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#putContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      java.io.InputStream)
   */
  public Resource<?> putContent(ResourceURI uri, ResourceContent content,
      InputStream is) throws ContentRepositoryException, IOException,
      IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Is this a new request or a scheduled asynchronous execution?
    if (!(CurrentOperation.get() instanceof PutContentOperation)) {
      return putContentAsynchronously(uri, content, is).get();
    }

    // Check if resource is in temporary cache already by another operation
    if (processor.isProcessing(uri)) {
      logger.debug("Resource '{}' is being processed, adding content anyway", uri);
    }

    // Make sure the resource exists
    if (!index.exists(uri))
      throw new IllegalStateException("Cannot add content to missing resource " + uri);
    Resource<ResourceContent> resource = null;
    try {
      resource = get(uri);
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
  public PutContentOperation putContentAsynchronously(final ResourceURI uri,
      final ResourceContent content, final InputStream is)
          throws ContentRepositoryException, IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Create an asynchronous operation representation and return it
    PutContentOperation putOperation = new PutContentOperationImpl(uri, content, is);
    processor.enqueue(putOperation);
    return putOperation;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#deleteContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent)
   */
  public Resource<?> deleteContent(ResourceURI uri, ResourceContent content)
      throws ContentRepositoryException, IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Is this a new request or a scheduled asynchronous execution?
    if (!(CurrentOperation.get() instanceof DeleteContentOperation)) {
      return deleteContentAsynchronously(uri, content).get();
    }

    // Check if resource is in temporary cache already by another operation
    if (processor.isProcessing(uri)) {
      logger.debug("Resource '{}' is being processed, removing content anyway", uri);
    }

    // Make sure the resource exists
    if (!index.exists(uri))
      throw new IllegalStateException("Cannot remove content from missing resource " + uri);
    Resource<?> resource = null;
    try {
      resource = get(uri);
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
  public DeleteContentOperation deleteContentAsynchronously(
      final ResourceURI uri, final ResourceContent content)
          throws ContentRepositoryException, IOException, IllegalStateException {

    if (!isStarted())
      throw new IllegalStateException("Content repository is not connected");

    // Create an asynchronous operation representation and return it
    DeleteContentOperation deleteContentOperation = new DeleteContentOperationImpl(uri, content);
    processor.enqueue(deleteContentOperation);
    return deleteContentOperation;
  };

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#index()
   */
  public void index() throws ContentRepositoryException {

    if (indexing || indexingOffsite) {
      logger.warn("Ignoring additional index request for {}", this);
      return;
    }

    readOnly = true;
    logger.info("Switching site '{}' to read only mode", site);

    String newIdxRootDirName = idxRootDir.getName() + "-new-" + UUID.randomUUID().toString();
    File newIdxRootDir = new File(idxRootDir.getParentFile(), newIdxRootDirName);
    FileSystemContentRepositoryIndex newIndex = null;

    // Create the new index
    try {
      logger.info("Creating new index at {}", newIdxRootDir);
      FileUtils.forceMkdir(newIdxRootDir);
      newIndex = new FileSystemContentRepositoryIndex(newIdxRootDir);
      indexingOffsite = true;
      rebuildIndex(newIndex);
    } catch (IOException e) {
      indexingOffsite = false;
      try {
        FileUtils.forceDelete(newIdxRootDir);
      } catch (IOException e1) {
        logger.error("Error removing incomplete new index at {}: {}", newIdxRootDir, e.getMessage());
      }
      throw new ContentRepositoryException("Error creating index " + site.getIdentifier(), e);
    } finally {
      try {
        if (newIndex != null)
          newIndex.close();
      } catch (IOException e) {
        throw new ContentRepositoryException("Error closing new index " + site.getIdentifier(), e);
      }
    }

    String oldIdxRootDirName = idxRootDir.getName() + "-old-" + UUID.randomUUID().toString();
    File oldIdxRootDir = new File(idxRootDir.getParentFile(), oldIdxRootDirName);

    try {
      indexing = true;
      index.close();
      logger.info("Moving new index to place {}", idxRootDir);
      FileUtils.moveDirectory(idxRootDir, oldIdxRootDir);
      FileUtils.moveDirectory(newIdxRootDir, idxRootDir);
      index = new FileSystemContentRepositoryIndex(idxRootDir);
      logger.info("Removing old index at {}", oldIdxRootDir);
      FileUtils.forceDelete(oldIdxRootDir);
    } catch (IOException e) {
      throw new ContentRepositoryException("Error clearing index " + site.getIdentifier(), e);
    } finally {
      indexing = false;
      indexingOffsite = false;
      logger.info("Switching site '{}' back to write mode", site);
      readOnly = false;
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#indexAsynchronously()
   */
  public IndexOperation indexAsynchronously() throws ContentRepositoryException {
    IndexOperation op = new IndexOperationImpl();
    processor.enqueue(op);
    return op;
  }

  /**
   * Creates a new content repository index at the given location as specified
   * by <code>idx</code>.
   * 
   * @param idx
   *          the index
   * @throws ContentRepositoryException
   *           if indexing fails
   */
  private void buildIndex(ContentRepositoryIndex idx)
      throws ContentRepositoryException {
    boolean oldReadOnly = readOnly;
    readOnly = true;
    indexing = true;

    if (!oldReadOnly)
      logger.info("Switching site '{}' to read only mode", site.getIdentifier());

    rebuildIndex(idx);

    indexing = false;
    if (!oldReadOnly)
      logger.info("Switching site '{}' back to write mode", site.getIdentifier());
    readOnly = oldReadOnly;
  }

  /**
   * Creates a new content repository index at the given location as specified
   * by <code>idx</code>.
   * 
   * @param idx
   *          the index
   * @throws ContentRepositoryException
   *           if indexing fails
   */
  private void rebuildIndex(ContentRepositoryIndex idx)
      throws ContentRepositoryException {
    boolean success = true;

    try {
      // Clear the current index, which might be null if the site has not been
      // started yet.
      if (idx == null)
        idx = loadIndex(idxRootDir);

      logger.info("Creating site index '{}'...", site.getIdentifier());
      long time = System.currentTimeMillis();
      long resourceCount = 0;

      // Index each and every known resource type
      Set<ResourceSerializer<?, ?>> serializers = ResourceSerializerFactory.getSerializers();
      if (serializers == null) {
        logger.warn("Unable to index {} while no resource serializers are registered", this);
        return;
      }
      for (ResourceSerializer<?, ?> serializer : serializers) {
        long added = index(idx, serializer.getType());
        if (added > 0)
          logger.info("Added {} {}s to index", added, serializer.getType().toLowerCase());
        resourceCount += added;
      }

      if (resourceCount > 0) {
        time = System.currentTimeMillis() - time;
        logger.info("Site index populated in {} ms", ConfigurationUtils.toHumanReadableDuration(time));
        logger.info("{} resources added to index", resourceCount);
      }
    } catch (IOException e) {
      success = false;
      throw new ContentRepositoryException("Error while writing to index", e);
    } catch (MalformedResourceURIException e) {
      success = false;
      throw new ContentRepositoryException("Error while reading resource uri for index", e);
    } finally {
      if (!success) {
        try {
          idx.clear();
        } catch (IOException e) {
          logger.error("Error while trying to cleanup after failed indexing operation", e);
        }
      }
    }
  }

  /**
   * This method indexes a certain type of resources and expects the resources
   * to be located in a sub directory of the site directory named
   * <tt>&lt;resourceType&gt;s<tt>.
   * 
   * @param idx
   *          the content repository index
   * @param resourceType
   *          the resource type
   * @return the number of resources that were indexed
   * @throws IOException
   *           if accessing a file fails
   */
  protected long index(ContentRepositoryIndex idx, String resourceType)
      throws IOException {

    // Temporary path for rebuilt site
    String resourceDirectory = resourceType + "s";
    String homePath = UrlUtils.concat(repositorySiteRoot.getAbsolutePath(), resourceDirectory);
    File resourcesRootDirectory = new File(homePath);
    FileUtils.forceMkdir(resourcesRootDirectory);
    if (resourcesRootDirectory.list().length == 0) {
      logger.debug("No {}s found to index", resourceType);
      return 0;
    }

    logger.info("Populating site index '{}' with {}s...", site, resourceType);

    ResourceSerializer<?, ?> serializer = ResourceSerializerFactory.getSerializerByType(resourceType);
    if (serializer == null) {
      logger.warn("Unable to index resources of type '{}': no resource serializer found", resourceType);
      return 0;
    }

    // Clear previews directory
    logger.info("Removing cached preview images");
    File previewsDir = new File(PathUtils.concat(System.getProperty("java.io.tmpdir"), "sites", site.getIdentifier(), "images"));
    FileUtils.deleteQuietly(previewsDir);

    long resourceCount = 0;
    long resourceVersionCount = 0;

    // Ask for all existing resources of the current type and index them
    List<ResourceURI> uris = listResources(resourceType);
    for (ResourceURI uri : uris) {
      try {
        Resource<?> resource = null;
        ResourceReader<?, ?> reader = serializer.getReader();
        InputStream is = null;

        // Read the resource
        try {
          is = loadResource(uri);
          resource = reader.read(is, site);
          if (resource == null) {
            logger.warn("Unkown error loading '{}'", uri);
            continue;
          }
        } catch (Throwable t) {
          logger.error("Error loading '{}': {}", uri, t.getMessage());
          continue;
        } finally {
          IOUtils.closeQuietly(is);
        }

        idx.add(resource);
        resourceVersionCount++;

      } catch (Throwable t) {
        logger.error("Error indexing {} {}: {}", new Object[] {
            resourceType,
            uri,
            t.getMessage() });
      }
    }

    // Log the work
    if (resourceCount > 0) {
      logger.info("{} {}s and {} revisions added to index", new Object[] {
          resourceCount,
          resourceType,
          resourceVersionCount - resourceCount });
    }

    return resourceCount;
  }

  /**
   * Returns a list of resources of the given type that are available in the
   * content repository.
   * 
   * @param resourceType
   *          the resource type
   * @return the resource URIs
   * @throws IOException
   *           if loading the list of resources failed
   */
  protected abstract List<ResourceURI> listResources(String resourceType)
      throws IOException;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#loadIndex()
   */
  @Override
  protected ContentRepositoryIndex loadIndex() throws IOException,
  ContentRepositoryException {
    logger.debug("Trying to load site index from {}", idxRootDir);
    return loadIndex(idxRootDir);
  }

  /**
   * Loads the index from a given directory on the filesystem.
   * 
   * @param idxRoot
   *          the root directory
   * @return the content repository
   * @throws IOException
   *           if reading from the filesystem fails
   * @throws ContentRepositoryException
   *           if creating the content repository index fails
   */
  protected ContentRepositoryIndex loadIndex(File idxRoot) throws IOException,
  ContentRepositoryException {

    ContentRepositoryIndex idx = null;

    logger.debug("Trying to load site index from {}", idxRoot);

    // Is this a new index?
    boolean created = !idxRoot.exists() || idxRoot.list().length == 0;
    FileUtils.forceMkdir(idxRoot);

    // Add content if there is any
    idx = new FileSystemContentRepositoryIndex(idxRoot);

    // Create the idx if there is nothing in place so far
    if (idx.getResourceCount() <= 0) {
      buildIndex(idx);
    }

    // Make sure the version matches the implementation
    else if (idx.getIndexVersion() < VersionedContentRepositoryIndex.INDEX_VERSION) {
      logger.info("Index needs to be updated, triggering reindex");
      buildIndex(idx);
    } else if (idx.getIndexVersion() != VersionedContentRepositoryIndex.INDEX_VERSION) {
      logger.warn("Index needs to be downgraded, triggering reindex");
      buildIndex(idx);
    }

    // Is there an existing idx?
    if (created) {
      logger.info("Created site idx at {}", idxRoot);
    } else {
      long resourceCount = idx.getResourceCount();
      long resourceVersionCount = idx.getRevisionCount();
      logger.info("Loaded site idx with {} resources and {} revisions from {}", new Object[] {
          resourceCount,
          resourceVersionCount - resourceCount,
          idxRoot });
    }

    return idx;
  }

  /**
   * Writes a new resource to the repository storage.
   * 
   * @param resource
   *          the resource content
   * @throws IOException
   *           if the resource can't be written to the storage
   */
  protected abstract <C extends ResourceContent, R extends Resource<C>> R storeResource(
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
  protected abstract <C extends ResourceContent, R extends Resource<C>> C storeResourceContent(
      ResourceURI uri, C content, InputStream is) throws IOException;

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
  protected abstract <C extends ResourceContent, R extends Resource<C>> void deleteResourceContent(
      ResourceURI uri, C content) throws IOException;

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
      t.setPriority(Thread.MIN_PRIORITY);
      t.setDaemon(true);
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
   * This class is used as a way to keep track of what has been added to the
   * repository but has not been flushed to disk.
   */
  public final class OperationProcessor {

    /** The operations counter */
    private final Map<ResourceURI, List<ContentRepositoryOperation<?>>> operationsPerResource = new HashMap<ResourceURI, List<ContentRepositoryOperation<?>>>();

    /** The repository operations */
    protected List<ContentRepositoryOperation<?>> operations = new ArrayList<ContentRepositoryOperation<?>>();

    /** The worker thread */
    private Thread processorWorker = null;

    /** Running flag */
    protected boolean keepRunning = true;

    /**
     * Creates a new operation processor.
     */
    public OperationProcessor(final WritableContentRepository repository) {
      final OperationProcessor monitor = this;
      processorWorker = new Thread(new Runnable() {
        public void run() {
          while (keepRunning) {
            List<ContentRepositoryOperation<?>> opList = new ArrayList<ContentRepositoryOperation<?>>(operations);
            for (ContentRepositoryOperation<?> op : opList) {
              try {
                CurrentOperation.set(op);
                op.execute(repository);
              } catch (Throwable t) {
                logger.debug("Error while executing {}: {}", op, t.getMessage());
                // This will be dealt with by the operation itself
              } finally {
                CurrentOperation.remove();

                // Remove the operation form the operations list
                synchronized (operations) {
                  operations.remove(op);
                  operations.notifyAll();
                }

                // Tell everyone that we are down 1
                synchronized (monitor) {
                  monitor.notifyAll();
                }
              }
            }

            // Is there more work to be done? If not, wait for more
            synchronized (operations) {
              while (keepRunning && operations.size() == 0) {
                try {
                  operations.wait();
                } catch (InterruptedException e) {
                  logger.debug("Interrupted while waiting for more work");
                }
              }
            }
          }
        }
      });
      processorWorker.start();
    }

    /**
     * Returns <code>true</code> if the cache contains the uri itself or a
     * different version of it.
     * <p>
     * Note that this method is not considering operations returned by
     * {@link CurrentOperation#get()}.
     * 
     * @param uri
     *          the uri
     * @return <code>true</code> if it contains a version of this resource
     */
    public boolean isProcessingVersionOf(ResourceURI uri) {
      synchronized (operations) {
        for (ResourceURI u : operationsPerResource.keySet()) {
          if (u.getIdentifier().equals(uri.getIdentifier())) {
            ContentRepositoryOperation<?> currentOp = CurrentOperation.get();
            if (currentOp instanceof ContentRepositoryResourceOperation<?>) {
              ResourceURI currentURI = ((ContentRepositoryResourceOperation<?>) currentOp).getResourceURI();
              if (!u.equals(currentURI))
                return true;
            }
          }
        }
      }
      return false;
    }

    /**
     * Returns <code>true</code> if the scheduler is processing work related to
     * the given resource and the version indicated by the uri.
     * 
     * @param uri
     *          the uri
     * @return <code>true</code> if the resource is being processed
     */
    public boolean isProcessing(ResourceURI uri) {
      synchronized (operations) {
        for (ResourceURI u : operationsPerResource.keySet()) {
          if (u.getIdentifier().equals(uri.getIdentifier()))
            return true;
        }
      }
      return false;
    }

    /**
     * Returns the list of currently scheduled content repository operation.
     * 
     * @return the content repository operations
     */
    public List<ContentRepositoryOperation<?>> getOperations() {
      return new ArrayList<ContentRepositoryOperation<?>>(operations);
    }

    /**
     * Adds the given operation to the list of resources that need to be
     * processed-
     * 
     * @param operation
     *          the operation
     */
    public void enqueue(ContentRepositoryOperation<?> operation) {
      operations.add(operation);
      synchronized (operations) {
        operations.notifyAll();
      }
    }

    /**
     * Stops the scheduler.
     */
    public void stop() {
      keepRunning = false;
      operationsPerResource.clear();
      synchronized (operations) {
        operations.clear();
        operations.notifyAll();
      }
    }

  }

}
