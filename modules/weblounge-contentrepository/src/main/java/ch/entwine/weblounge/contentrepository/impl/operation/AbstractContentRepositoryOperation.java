/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.contentrepository.impl.operation;

import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryOperationListener;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the basis for an asynchronously executed content
 * repository operation.
 */
public abstract class AbstractContentRepositoryOperation<T extends Object> implements ContentRepositoryOperation<T> {

  /** A list of listeners that are interested in the operation's outcome */
  protected List<ContentRepositoryOperationListener> listeners = null;

  /** The content repository */
  protected WritableContentRepository repository = null;

  /** The cause of potential errors */
  protected Throwable error = null;

  /** The result */
  protected T result = null;

  /** flag to indicate running operations */
  boolean isRunning = true;

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#addOperationListener(ch.entwine.weblounge.common.content.repository.ContentRepositoryOperationListener)
   */
  public void addOperationListener(ContentRepositoryOperationListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<ContentRepositoryOperationListener>(5);
    }
    listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#removeOperationListener(ch.entwine.weblounge.common.content.repository.ContentRepositoryOperationListener)
   */
  public void removeOperationListener(
      ContentRepositoryOperationListener listener) {
    if (listeners == null)
      return;
    listeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#execute(ch.entwine.weblounge.common.content.repository.WritableContentRepository)
   */
  public final T execute(WritableContentRepository repository)
      throws ContentRepositoryException, IOException {
    if (repository == null)
      throw new IllegalArgumentException("Repository must not be null");
    this.repository = repository;
    try {
      isRunning = true;
      result = run(null);
      isRunning = false;
      fireOperationSucceeded();
      return result;
    } catch (ContentRepositoryException e) {
      error = e;
      isRunning = false;
      fireOperationFailed(e);
      throw e;
    } catch (IOException e) {
      error = e;
      isRunning = false;
      fireOperationFailed(e);
      throw e;
    } catch (Throwable t) {
      isRunning = false;
      error = t;
      fireOperationFailed(t);
      throw new ContentRepositoryException(t);
    }
  }

  /**
   * Returns the content repository.
   * 
   * @return the content repository
   */
  protected WritableContentRepository getContentRepository() {
    return repository;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#getResult()
   */
  public T getResult() throws IllegalStateException {
    if (isRunning)
      throw new IllegalStateException("Operation is still running");
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#getError()
   */
  public Throwable getError() throws IllegalStateException {
    if (isRunning)
      throw new IllegalStateException("Operation is still running");
    return error;
  }

  /**
   * Executes the operation and returns the corresponding result.
   * 
   * @param repository
   *          the content repository
   * @return the operation result
   * @throws ContentRepositoryException
   *           if the operation fails
   * @throws IOException
   *           if the operation fails due to read/write failures
   */
  protected abstract T run(WritableContentRepository repository)
      throws ContentRepositoryException, IOException;

  /**
   * Informs the registered listeners about success or failure of this content
   * repository operation.
   */
  protected void fireOperationSucceeded() {
    if (listeners == null)
      return;
    for (ContentRepositoryOperationListener listener : listeners) {
      listener.executionSucceeded(this);
    }
  }

  /**
   * Informs the registered listeners about success or failure of this content
   * repository operation.
   * 
   * @param t
   *          the cause of failure
   */
  protected void fireOperationFailed(Throwable t) {
    if (listeners == null)
      return;
    for (ContentRepositoryOperationListener listener : listeners) {
      listener.executionFailed(this, t);
    }
  }

}
