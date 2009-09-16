/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.repository;

/**
 * This exception is thrown if a repository transaction is being aborted by one
 * of the consulted parties.
 */
public final class RepositoryTransactionException extends RuntimeException {

  /** The serial version UID */
  private static final long serialVersionUID = -6246592869189259056L;

  /** The repository transaction */
  private RepositoryTransaction transaction_;

  /** The instance causing this exception */
  private RepositoryTransactionListener originator_;

  /**
   * Creates a new repository transaction exception.
   * 
   * @param transaction
   *          the repository transaction
   */
  public RepositoryTransactionException(RepositoryTransaction transaction,
      RepositoryTransactionListener listener, String reason) {
    super(reason);
    transaction_ = transaction;
    originator_ = listener;
  }

  /**
   * Returns the repository transaction.
   * 
   * @return the transaction
   */
  public RepositoryTransaction getTransaction() {
    return transaction_;
  }

  /**
   * Returns the repository listener that caused this exception.
   * 
   * @return the originator
   */
  public RepositoryTransactionListener getOriginator() {
    return originator_;
  }

}