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
import ch.entwine.weblounge.common.content.repository.ReferentialIntegrityException;
import ch.entwine.weblounge.common.content.repository.WritableContentRepository;
import ch.entwine.weblounge.contentrepository.impl.NotifyingOperationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class implements the basis for an asynchronously executed content
 * repository operation.
 */
public abstract class AbstractContentRepositoryOperation<T> implements ContentRepositoryOperation<T> {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(AbstractContentRepositoryOperation.class);

  /** The operation identifier provider */
  private static AtomicLong operationIdProvider = new AtomicLong();

  /** The operation identifier */
  private long operationId = -1;

  /** A list of listeners that are interested in the operation's outcome */
  protected List<ContentRepositoryOperationListener> listeners = null;

  /** The content repository */
  protected WritableContentRepository repository = null;

  /** The cause of potential errors */
  protected Throwable error = null;

  /** The result */
  protected T result = null;

  /** flag to indicate running operations */
  private boolean isRunning = true;

  /** The operation listener */
  private final ContentRepositoryOperationListener internalListener;

  /**
   * Creates a new content repository operation.
   */
  protected AbstractContentRepositoryOperation() {
    internalListener = new NotifyingOperationListener();
    listeners = new ArrayList<ContentRepositoryOperationListener>();
    listeners.add(internalListener);
    operationId = operationIdProvider.getAndIncrement();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#getIdentifier()
   */
  public long getIdentifier() {
    return operationId;
  }

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
      throws ContentRepositoryException, IOException,
      ReferentialIntegrityException, IllegalStateException {
    if (repository == null)
      throw new IllegalArgumentException("Repository must not be null");
    this.repository = repository;
    try {
      isRunning = true;
      CurrentOperation.set(this);
      result = run(repository);
      synchronized (internalListener) {
        isRunning = false;
        fireOperationSucceeded();
      }
      return result;
    } catch (ReferentialIntegrityException e) {
      error = e;
      synchronized (internalListener) {
        isRunning = false;
        fireOperationFailed(e);
      }
      throw e;
    } catch (ContentRepositoryException e) {
      error = e;
      synchronized (internalListener) {
        isRunning = false;
        fireOperationFailed(e);
      }
      throw e;
    } catch (IOException e) {
      error = e;
      synchronized (internalListener) {
        isRunning = false;
        fireOperationFailed(e);
      }
      throw e;
    } catch (IllegalStateException e) {
      error = e;
      synchronized (internalListener) {
        isRunning = false;
        fireOperationFailed(e);
      }
      throw e;
    } catch (Throwable t) {
      synchronized (internalListener) {
        isRunning = false;
        error = t;
        fireOperationFailed(t);
      }
      throw new ContentRepositoryException(t);
    } finally {
      CurrentOperation.remove();
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
   * @see ch.entwine.weblounge.common.content.repository.ContentRepositoryOperation#get()
   */
  public T get() throws ContentRepositoryException, IOException {
    synchronized (internalListener) {
      if (isRunning) {
        try {
          internalListener.wait();
        } catch (InterruptedException e) {
          logger.warn("Interrupted while waiting for the operation result");
        }
      }
    }
    if (error != null) {
      if (error instanceof ContentRepositoryException)
        throw (ContentRepositoryException) error;
      else if (error instanceof IOException)
        throw (IOException) error;
      else
        throw new ContentRepositoryException(error);
    }
    return result;
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
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ContentRepositoryOperation))
      return false;
    return ((ContentRepositoryOperation<?>) o).getIdentifier() == operationId;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Long.valueOf(operationId).hashCode();
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
      throws ContentRepositoryException, IOException, IllegalStateException,
      ReferentialIntegrityException;

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
