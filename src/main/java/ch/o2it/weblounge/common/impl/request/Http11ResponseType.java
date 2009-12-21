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

package ch.o2it.weblounge.common.impl.request;

/**
 * Describes the required response type to maintain HTTP 1.1 compatibility.
 * 
 * @see Http11ProtocolHandler
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

  /**
   * Creates a new response type with the given modification time.
   * 
   * @param type
   *          the response type
   * @param modified
   *          the modification time
   */
  public Http11ResponseType(int type, long modified) {
    this(type, modified, -1L, null);
  }

  /**
   * Creates a new response type with the given modification and expiration
   * time.
   * 
   * @param type
   *          the response type
   * @param modified
   *          the modification time
   * @param expires
   *          the expiration time
   */
  public Http11ResponseType(int type, long modified, long expires) {
    this(type, modified, expires, null);
  }

  /**
   * Creates a new response type with the given modification and expiration time
   * and the indicated error message.
   * 
   * @param type
   *          the response type
   * @param modified
   *          the modification time
   * @param expires
   *          the expiration time
   * @param err
   *          the error message
   */
  public Http11ResponseType(int type, long modified, long expires, String err) {
    this.type = type;
    this.modified = modified;
    this.expires = expires;
    this.err = err;
  }

  /**
   * Returns the starting range for partial responses.
   * 
   * @return the starting range
   */
  public int getFrom() {
    return from;
  }

  /**
   * Returns the ending range for partial responses.
   * 
   * @return the ending range
   */
  public int getTo() {
    return to;
  }

  /**
   * Returns the response type.
   * 
   * @return the response type
   */
  public int getType() {
    return type;
  }

  /**
   * Returns the value of the <code>expires</code> header.
   * 
   * @return the expiration time
   */
  public long getExpires() {
    return expires;
  }

  /**
   * Returns the modification time.
   * 
   * @return the modification time
   */
  public long getModified() {
    return modified;
  }

  /**
   * Returns the creation time of the response.
   * 
   * @return the creation time
   */
  public long getTime() {
    return time;
  }

  /**
   * Returns the error message or <code>null</code> if there is none.
   * 
   * @return the error message
   */
  public String getErr() {
    return err;
  }

  /**
   * Returns the response length in bytes.
   * 
   * @return the response length
   */
  public long getSize() {
    return size;
  }

  /**
   * Returns <code>true</code> if the response contains headers only. This could
   * be the case if a client was asking for a page and the system would respond
   * with a <code>not modified</code> message, in which case no data would be
   * transmitted back to the client.
   * 
   * @return <code>true</code> if there are only headers in the response
   */
  public boolean isHeaderOnly() {
    return headerOnly;
  }

}