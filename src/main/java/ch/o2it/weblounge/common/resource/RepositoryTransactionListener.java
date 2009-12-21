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

package ch.o2it.weblounge.common.resource;

/**
 * A <code>RepositoryTransactionListener</code> can observe and react on all
 * kinds of repository transactions like moving and deletion of collections and
 * items, but also has the possibility to stop them by calling
 * <code>abort</code> on the transaction.
 */
public interface RepositoryTransactionListener {

  /**
   * This method is called if the page transaction has been started. Here the
   * listener should check if the intended operation is ok, otherwise call
   * <code>abort()</code> on the transaction.
   * 
   * @param transaction
   *          the transaction
   */
  void repositoryTransactionStarted(RepositoryTransaction transaction);

  /**
   * This method is called after all <code>PageTransactionListener</code>s have
   * agreed on committing the transaction. Here is the place to do whatever is
   * necessary to conform to the transaction.
   * 
   * @param transaction
   *          the transaction
   */
  void repositoryTransactionCompleted(RepositoryTransaction transaction);

  /**
   * This method is called if the page transaction has been aborted by a
   * <code>PageTransactionListener</code>.
   * 
   * @param transaction
   *          the transaction
   */
  void repositoryTransactionAborted(RepositoryTransaction transaction);

}