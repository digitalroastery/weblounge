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

package ch.entwine.weblounge.common.content.repository;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.security.User;

import java.io.IOException;
import java.io.InputStream;

/**
 * This type of repository implements methods to write contents to it.
 */
public interface WritableContentRepository extends ContentRepository {

  /**
   * Puts the resource to the specified location. Depending on whether the
   * resource identified by <code>uri</code> already exists, the method either
   * creates a new resource or updates the existing one.
   * <p>
   * The returned resource contains the same data than the one passed in as the
   * <code>resource</code> argument but with an updated uri.
   * <p>
   * <b>Note:</b> do not modify the resource content using this method. Use
   * {@link #putContent(Resource, InputStream)} instead.
   * 
   * @param uri
   *          the resource uri
   * @param resource
   *          the resource
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
   * @return the updated resource
   */
  Resource<?> put(Resource<?> resource) throws ContentRepositoryException,
  IOException, IllegalStateException;

  /**
   * Puts the resource to the specified location. Depending on whether the
   * resource identified by <code>uri</code> already exists, the method either
   * creates a new resource or updates the existing one.
   * <p>
   * The returned resource contains the same data than the one passed in as the
   * <code>resource</code> argument but with an updated uri.
   * <p>
   * <b>Note:</b> do not modify the resource content using this method. Use
   * {@link #putContent(Resource, InputStream)} instead.
   * 
   * @param uri
   *          the resource uri
   * @param resource
   *          the resource
   * @param updatePreviews
   *          <code>true</code> to update previews
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
   * @return the updated resource
   */
  Resource<?> put(Resource<?> resource, boolean updatePreviews)
      throws ContentRepositoryException, IOException, IllegalStateException;

  /**
   * Puts the resource to the specified location and passes the result to the
   * operation once the process succeeds or fails.
   * <p>
   * Depending on whether the resource identified by <code>uri</code> already
   * exists, the method either creates a new resource or updates the existing
   * one.
   * <p>
   * The returned resource contains the same data than the one passed in as the
   * <code>resource</code> argument but with an updated uri.
   * <p>
   * <b>Note:</b> do not modify the resource content using this method. Use
   * {@link #putContent(Resource, InputStream)} instead.
   * 
   * @param uri
   *          the resource uri
   * @param resource
   *          the resource
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
   * @return the updated resource
   */
  PutOperation putAsynchronously(Resource<?> resource)
      throws ContentRepositoryException, IOException, IllegalStateException;

  /**
   * Puts the resource to the specified location and passes the result to the
   * operation once the process succeeds or fails.
   * <p>
   * Depending on whether the resource identified by <code>uri</code> already
   * exists, the method either creates a new resource or updates the existing
   * one.
   * <p>
   * The returned resource contains the same data than the one passed in as the
   * <code>resource</code> argument but with an updated uri.
   * <p>
   * <b>Note:</b> do not modify the resource content using this method. Use
   * {@link #putContent(Resource, InputStream)} instead.
   * 
   * @param uri
   *          the resource uri
   * @param resource
   *          the resource
   * @param updatePreviews
   *          <code>true</code> to update previews
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
   * @return the updated resource
   */
  PutOperation putAsynchronously(Resource<?> resource, boolean updatePreviews)
      throws ContentRepositoryException, IOException, IllegalStateException;

  /**
   * Adds the content to the specified resource.
   * <p>
   * The returned resource contains the same data than the one passed in as the
   * <code>resource</code> argument but with an updated uri.
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if adding fails due to a database error
   * @throws IllegalStateException
   *           if the parent resource does not exist or is of an incompatible
   *           type
   * @return the updated resource
   */
  Resource<?> putContent(ResourceURI uri, ResourceContent content,
      InputStream is) throws ContentRepositoryException, IOException,
      IllegalStateException;

  /**
   * Adds the content to the specified resource and passes the result to the
   * operation once the process succeeds or fails.
   * <p>
   * The returned resource contains the same data than the one passed in as the
   * <code>resource</code> argument but with an updated uri.
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if adding fails due to a database error
   * @throws IllegalStateException
   *           if the parent resource does not exist or is of an incompatible
   *           type
   * @return the updated resource
   */
  PutContentOperation putContentAsynchronously(ResourceURI uri,
      ResourceContent content, InputStream is)
          throws ContentRepositoryException, IOException, IllegalStateException;

  /**
   * Deletes the resource content.
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @throws ContentRepositoryException
   *           if removing the content repository fails
   * @throws IOException
   *           if removal fails due to a database error
   * @throws IllegalStateException
   *           if the parent resource does not exist
   * @return the updated resource
   */
  Resource<?> deleteContent(ResourceURI uri, ResourceContent content)
      throws ContentRepositoryException, IOException, IllegalStateException;

  /**
   * Removes the content from the specified resource and passes the result to
   * the operation once the process succeeds or fails.
   * <p>
   * 
   * @param uri
   *          the resource uri
   * @param content
   *          the resource content
   * @throws ContentRepositoryException
   *           if removing the content repository fails
   * @throws IOException
   *           if removal fails due to a database error
   * @throws IllegalStateException
   *           if the parent resource does not exist
   * @return the updated resource
   */
  DeleteContentOperation deleteContentAsynchronously(ResourceURI uri,
      ResourceContent content) throws ContentRepositoryException, IOException,
      IllegalStateException;

  /**
   * This method moves the given resource to the new uri.
   * 
   * @param uri
   *          uri of the resource to move
   * @param path
   *          the target path
   * @param moveChildren
   *          <code>true</code> to also move children of this resource
   * @throws IllegalArgumentException
   *           if <code>path</code> is <code>null</code>, empty or relative
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if moving fails due to a database error
   */
  void move(ResourceURI uri, String path, boolean moveChildren)
      throws ContentRepositoryException, IOException;

  /**
   * Moves the resource and optionally its children to the specified path and
   * passes the result to the operation once the process succeeds or fails.
   * 
   * @param uri
   *          uri of the resource to move
   * @param path
   *          the target path
   * @param moveChildren
   *          <code>true</code> to also move children of this resource
   * @throws IllegalArgumentException
   *           if <code>path</code> is <code>null</code>, empty or relative
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if moving fails due to a database error
   */
  MoveOperation moveAsynchronously(ResourceURI uri, String path,
      boolean moveChildren) throws ContentRepositoryException, IOException;

  /**
   * This method removes the given resource in the specified version from the
   * repository.
   * 
   * @param uri
   *          uri of the resource to remove
   * @return <code>true</code> if the resource could be removed
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if removal fails due to a database error
   */
  boolean delete(ResourceURI uri) throws ContentRepositoryException,
  IOException;

  /**
   * Removes the resource in the given version from the content repository and
   * passes the result to the operation once the process succeeds or fails.
   * <p>
   * 
   * @param uri
   *          uri of the resource to remove
   * @return <code>true</code> if the resource could be removed
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if removal fails due to a database error
   * @throws ReferentialIntegrityException
   *           if the resource is still being linked to
   */
  DeleteOperation deleteAsynchronously(ResourceURI uri)
      throws ContentRepositoryException, IOException;

  /**
   * Removes the given resource in the specified version from the database.
   * 
   * @param uri
   *          uri of the resource to remove
   * @param version
   *          the version to remove
   * @param allRevisions
   *          <code>true</code> to remove all revisions
   * @return <code>true</code> if the resource could be removed
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if removal fails due to a database error
   * @throws ReferentialIntegrityException
   *           if the resource is still being linked to
   */
  boolean delete(ResourceURI uri, boolean allRevisions)
      throws ContentRepositoryException, IOException,
      ReferentialIntegrityException;

  /**
   * Removes either all or just the specified version of the resource from the
   * content repository and passes the result to the operation once the process
   * succeeds or fails.
   * <p>
   * 
   * @param uri
   *          uri of the resource to remove
   * @param version
   *          the version to remove
   * @param allRevisions
   *          <code>true</code> to remove all revisions
   * @return <code>true</code> if the resource could be removed
   * @throws ContentRepositoryException
   *           if updating the content repository fails
   * @throws IOException
   *           if removal fails due to a database error
   */
  DeleteOperation deleteAsynchronously(ResourceURI uri, boolean allRevisions)
      throws ContentRepositoryException, IOException;

  /**
   * Triggers a re-index of the repository's search index.
   * <p>
   * Depending on the implementation of the search index, the repository might
   * be locked during this operation.
   * 
   * @throws ContentRepositoryException
   *           if the index operation fails
   */
  void index() throws ContentRepositoryException;

  /**
   * Triggers a re-index of the repository's search index and and passes the
   * result to the operation once the process succeeds or fails
   * 
   * <p>
   * Depending on the implementation of the search index, the repository might
   * be locked during this operation.
   * 
   * @throws ContentRepositoryException
   *           if the index operation fails
   */
  IndexOperation indexAsynchronously() throws ContentRepositoryException;

  /**
   * Returns <code>true</code> if the resource identified by the given uri is
   * locked. A resource is locked if an editor is currently editing the resource
   * and therefore holding the lock.
   * 
   * @param uri
   *          the resource uri
   * @return <code>true</code> if the resource is locked
   * @throws ContentRepositoryException
   *           if the resource can't be accessed
   */
  boolean isLocked(ResourceURI uri) throws ContentRepositoryException;

  /**
   * Locks the resource for editing by <code>user</code>. This method will throw
   * an <code>IllegalStateException</code> if the resource is already locked by
   * a different user.
   * 
   * @param uri
   *          the resource uri
   * @param user
   *          the user locking the resource
   * @return the locked resource
   * @throws IllegalStateException
   *           if the resource is already locked by a different user
   * @throws IOException
   *           if locking fails due to a database error
   * @throws ContentRepositoryException
   *           if the resource can't be accessed
   */
  Resource<?> lock(ResourceURI uri, User user) throws IOException,
  ContentRepositoryException, IllegalStateException;

  /**
   * Locks the resource for editing by <code>user</code>. This method will throw
   * an <code>IllegalStateException</code> if the resource is already locked by
   * a different user and pass the result on to the operation once the process
   * succeeds or fails.
   * 
   * @param uri
   *          the resource uri
   * @param user
   *          the user locking the resource
   * @return the locked resource
   * @throws IllegalStateException
   *           if the resource is already locked by a different user
   * @throws IOException
   *           if locking fails due to a database error
   * @throws ContentRepositoryException
   *           if the resource can't be accessed
   */
  LockOperation lockAsynchronously(ResourceURI uri, User user)
      throws IOException, ContentRepositoryException, IllegalStateException;

  /**
   * Removes the editing lock from the resource and returns the user if the
   * resource was locked prior to this call, <code>null</code> otherwise.
   * 
   * @param uri
   *          the resource uri
   * @return the unlocked resource
   * @throws IOException
   *           if unlocking fails due to a database error
   * @throws ContentRepositoryException
   *           if the resource can't be accessed
   */
  Resource<?> unlock(ResourceURI uri, User user) throws IOException,
  ContentRepositoryException;

  /**
   * Removes the editing lock from the resource and returns the user if the
   * resource was locked prior to this call, <code>null</code> otherwise and
   * passes the result on to the operation once the process succeeds or fails.
   * 
   * @param uri
   *          the resource uri
   * @return the unlocked resource
   * @throws IOException
   *           if unlocking fails due to a database error
   * @throws ContentRepositoryException
   *           if the resource can't be accessed
   */
  UnlockOperation unlockAsynchronously(ResourceURI uri, User user)
      throws IOException, ContentRepositoryException;

}
