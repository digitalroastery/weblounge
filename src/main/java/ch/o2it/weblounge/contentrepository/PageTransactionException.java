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

package ch.o2it.weblounge.contentrepository;

/**
 * This exception is thrown if a page transaction is being aborted by one of
 * the consulted parties.
 */
public final class PageTransactionException extends RuntimeException {

  /** The serial version UID */
  private static final long serialVersionUID = -6246592869189259056L;

  /** The page transaction */
  private PageTransaction transaction = null;

  /** The instance causing this exception */
  private PageTransactionListener originator = null;

  /** The error code */
  private String reason = null;

  /**
   * Creates a new page transaction exception.
   * 
   * @param transaction
   *          the page transaction
   */
  public PageTransactionException(PageTransaction transaction, PageTransactionListener listener, String reason) {
    this.transaction = transaction;
    this.originator = listener;
    this.reason = reason;
  }

  /**
   * Returns the page transaction.
   * 
   * @return the transaction
   */
  public PageTransaction getTransaction() {
    return transaction;
  }

  /**
   * Returns the page listener that caused this exception.
   * 
   * @return the originator
   */
  public PageTransactionListener getOriginator() {
    return originator;
  }

  /**
   * Returns the reason leading to this exception. Ideally, this is an i18n key
   * in order to provide multilingual error messages.
   * 
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

}