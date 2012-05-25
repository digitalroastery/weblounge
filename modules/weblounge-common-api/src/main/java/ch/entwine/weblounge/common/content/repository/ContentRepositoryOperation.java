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

package ch.entwine.weblounge.common.content.repository;

import java.io.IOException;

/**
 * A resource operation describes what is performed on a resource in the content
 * repository.
 */
public interface ContentRepositoryOperation<T extends Object> {

  /**
   * Executes the operation and returns the corresponding result.
   * 
   * @param repository
   *          the content repository
   * @return the result
   * @throws ContentRepositoryException
   *           if the operation fails
   * @throws IOException
   *           if the operation fails due to read/write failures
   */
  T execute(WritableContentRepository repository)
      throws ContentRepositoryException, IOException;

  /**
   * Returns the operation result or <code>null</code> if there was no result
   * due to an error.
   * <p>
   * This method will throw an <code>IllegalStateException</code> if the
   * operation execution has not terminated yet. Users of this api should
   * register a {@link ContentRepositoryOperationListener} to get notified about
   * execution success or failure.
   * 
   * @return the result
   * @throws IllegalStateException
   *           if the operation is still running
   */
  T getResult() throws IllegalStateException;

  /**
   * Returns the cause of possible errors or <code>null</code> if no error has
   * occurred.
   * <p>
   * This method will throw an <code>IllegalStateException</code> if the
   * operation execution has not terminated yet. Users of this api should
   * register a {@link ContentRepositoryOperationListener} to get notified about
   * execution success or failure.
   * 
   * @return the error
   * @throws IllegalStateException
   *           if the operation is still running
   */
  Throwable getError() throws IllegalStateException;

  /**
   * Adds <code>listener</code> to the list of listeners that are interested in
   * hearing about this operation's result.
   * 
   * @param listener
   *          the listener
   */
  void addOperationListener(ContentRepositoryOperationListener listener);

  /**
   * Removes <code>listener</code> from the list of listeners that are
   * interested in hearing about this operation's outcome.
   * 
   * @param listener
   *          the listener
   */
  void removeOperationListener(ContentRepositoryOperationListener listener);

}
