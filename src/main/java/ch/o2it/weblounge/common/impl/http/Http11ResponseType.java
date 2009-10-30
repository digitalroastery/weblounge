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

package ch.o2it.weblounge.common.impl.http;

/**
 * Describes the required response type to maintain HTTP 1.1 compatibility.
 * 
 * @see ch.ch.o2it.weblounge.common.util.http.Http11ProtocolHandler
 */
public class Http11ResponseType {

	protected int type = Http11ProtocolHandler.RESPONSE_INTERNAL_SERVER_ERROR;
	protected int from = -1;
	protected int to = -1;
	protected long size = -1;
	protected long modified = -1L;
	protected long expires = -1L;
	protected String err = null;
	protected long time = System.currentTimeMillis();
	protected boolean headers = false;
	protected boolean headerOnly = false;

	public Http11ResponseType(int type, long modified) {
		this(type, modified, -1L, null);
	}

	public Http11ResponseType(int type, long modified, long expires) {
		this(type, modified, expires, null);
	}

	public Http11ResponseType(
		int type,
		long modified,
		long expires,
		String err) {
		this.type = type;
		this.modified = modified;
		this.expires = expires;
		this.err = err;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public int getType() {
		return type;
	}

	public long getExpires() {
		return expires;
	}

	public long getModified() {
		return modified;
	}

	public long getTime() {
		return time;
	}

	public String getErr() {
		return err;
	}

	public long getSize() {
		return size;
	}
	
	public boolean isHeaderOnly() {
		return headerOnly;
	}

}