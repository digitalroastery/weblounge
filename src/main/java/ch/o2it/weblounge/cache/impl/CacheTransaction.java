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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.cache.StreamFilter;
import ch.o2it.weblounge.common.request.CacheHandle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents an active cache transaction.
 */
class CacheTransaction {

	/**
	 * Creates a new CacheTransaction.
	 * @param hnd
	 * @param req
	 * @param resp
	 * @param filter
	 */
	CacheTransaction(
		CacheHandle hnd,
		HttpServletRequest req,
		HttpServletResponse resp,
		StreamFilter filter) {
		this.req = req;
		this.hnd = hnd;
		this.resp = resp;
		this.filter = filter;
	}

	/** the main cache handle for this transaction */
	CacheHandle hnd;

	/** the request that initiated this transaction */
	HttpServletRequest req;

	/** the response that accepts the result of this transaction */
	HttpServletResponse resp;

	/** the associated cache output stream */
	CacheOutputStream os = new CacheOutputStream();
	
	/** the cached response meta info */
	CachedResponseMetaInfo meta = new CachedResponseMetaInfo();

	/** the output filter */
	StreamFilter filter;
	
	/** true if the transaction has been invalidated */
	boolean invalidated = false;
}