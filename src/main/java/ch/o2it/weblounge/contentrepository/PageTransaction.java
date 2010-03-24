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

import ch.o2it.weblounge.common.content.PageURI;

/**
 * This interface defines fields and methods for a page transaction.
 */
public interface PageTransaction {

  /**
   * Causes this transaction to be stopped and rolled back.
   * 
   * <code>PageTransactionListener</code>s can call this method to cancel an
   * ongoing transaction.
   * 
   * @param listener
   *          the aborting <code>PageTransactionListener</code>
   * @param reason
   *          ideally, this is the i18n - key to the error message, but a string
   *          will also do
   */
  void abort(PageTransactionListener listener, String reason);

  /**
   * Returns the transaction subject.
   * 
   * @return uri of the page that is affected by this transaction
   */
  PageURI getURI();

}