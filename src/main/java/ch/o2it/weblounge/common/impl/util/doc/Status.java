/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.util.doc;

/**
 * Represents a possible status result for an endpoint with a code and an
 * optional description of that status code.
 */
public class Status {

  /** The status code */
  private int code = -1;

  /** The status name */
  private String name = null;

  /** Status description */
  private String description = null;

  /**
   * Creates a new response status.
   * 
   * @param code
   *          the status code
   * @param description
   *          the status description
   */
  public Status(int code, String description) {
    if (code < 100 || code > 1100) {
      throw new IllegalArgumentException("code " + code + " is outside of the valid range: 100-1100");
    }
    this.code = code;
    this.name = toString(code);
    this.description = description;
  }

  /**
   * Creates a return status <code>200</code> with the given optional
   * description.
   * 
   * @param description
   *          detailed meaning of this response status code
   * @return the status
   */
  public static Status OK(String description) {
    return new Status(200, description);
  }

  /**
   * Creates a return status <code>204</code> with the given optional
   * description.
   * 
   * @param description
   *          detailed meaning of this response status code
   * @return the status
   */
  public static Status NO_CONTENT(String description) {
    return new Status(204, description);
  }

  /**
   * Creates a return status <code>400</code> with the given optional
   * description.
   * 
   * @param description
   *          detailed meaning of this response status code
   * @return the status
   */
  public static Status BAD_REQUEST(String description) {
    return new Status(400, description);
  }

  /**
   * Creates a return status <code>401</code> with the given optional
   * description.
   * 
   * @param description
   *          detailed meaning of this response status code
   * @return the status
   */
  public static Status UNAUTHORIZED(String description) {
    return new Status(401, description);
  }

  /**
   * Creates a return status <code>403</code> with the given optional
   * description.
   * 
   * @param description
   *          detailed meaning of this response status code
   * @return the status
   */
  public static Status FORBIDDEN(String description) {
    return new Status(403, description);
  }

  /**
   * Creates a return status <code>404</code> with the given optional
   * description.
   * 
   * @param description
   *          detailed meaning of this response status code
   * @return the status
   */
  public static Status NOT_FOUND(String description) {
    return new Status(404, description);
  }

  /**
   * Creates a return status <code>500</code> with the given optional
   * description.
   * 
   * @param description
   *          detailed meaning of this response status code
   * @return the status
   */
  public static Status ERROR(String description) {
    return new Status(500, description);
  }

  /**
   * Creates a return status <code>503</code> with the given optional
   * description.
   * 
   * @param description
   *          detailed meaning of this response status code
   * @return the status
   */
  public static Status SERVICE_UNAVAILABLE(String description) {
    return new Status(503, description);
  }

  /**
   * Allows overriding of the name which is set from the code
   * 
   * @param name
   *          the name to display with the code
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the status code.
   * 
   * @return the code
   */
  public int getCode() {
    return code;
  }

  /**
   * Returns the status name.
   * 
   * @return the status name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the status description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * This will resolve a human readable name for all known http status codes,
   * taken from <a
   * href="http://en.wikipedia.org/wiki/List_of_HTTP_status_codes">
   * wikipedia</a>.
   * 
   * @param code
   *          the status code
   * @return the name OR UNKNOWN if none found
   * @throws IllegalArgumentException
   *           if the code is outside the valid range
   */
  private static String toString(int code) {
    // list from http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
    String result = "UNKNOWN";
    switch (code) {
      // 1xx Informational
      case 100:
        result = "Continue";
        break;
      case 101:
        result = "Switching Protocols";
        break;
      case 102:
        result = "Processing";
        break;
      // 2xx Success
      case 200:
        result = "OK";
        break;
      case 201:
        result = "Created";
        break;
      case 202:
        result = "Accepted";
        break;
      case 203:
        result = "Non-Authoritative Information";
        break;
      case 204:
        result = "No Content";
        break;
      case 205:
        result = "Reset Content";
        break;
      case 206:
        result = "Partial Content";
        break;
      case 207:
        result = "Multi-Status";
        break;
      // 3xx Redirection
      case 300:
        result = "Multiple Choices";
        break;
      case 301:
        result = "Moved Permanently";
        break;
      case 302:
        result = "Found";
        break;
      case 303:
        result = "See Other";
        break;
      case 304:
        result = "Not Modified";
        break;
      case 305:
        result = "Use Proxy";
        break;
      case 306:
        result = "Switch Proxy";
        break;
      case 307:
        result = "Temporary Redirect";
        break;
      // 4xx Client Error
      case 400:
        result = "Bad Request";
        break;
      case 401:
        result = "Unauthorized";
        break;
      case 402:
        result = "Payment Required";
        break;
      case 403:
        result = "Forbidden";
        break;
      case 404:
        result = "Not Found";
        break;
      case 405:
        result = "Method Not Allowed";
        break;
      case 406:
        result = "Not Acceptable";
        break;
      case 407:
        result = "Proxy Authentication Required";
        break;
      case 408:
        result = "Request Timeout";
        break;
      case 409:
        result = "Conflict";
        break;
      case 410:
        result = "Gone";
        break;
      case 411:
        result = "Length Required";
        break;
      case 412:
        result = "Precondition Failed";
        break;
      case 413:
        result = "Request Entity Too Large";
        break;
      case 414:
        result = "Request URI Too Long";
        break;
      case 415:
        result = "Unsupported Media Type";
        break;
      case 416:
        result = "Requested Range Not Satisfiable";
        break;
      case 417:
        result = "Expectation Failed";
        break;
      case 418:
        result = "I'm a teapot";
        break;
      case 422:
        result = "Unprocessable Entity";
        break;
      case 423:
        result = "Locked";
        break;
      case 424:
        result = "Failed Dependency";
        break;
      case 425:
        result = "Unordered Collection";
        break;
      case 426:
        result = "Upgrade Required";
        break;
      case 449:
        result = "Retry With";
        break;
      case 450:
        result = "Blocked by Windows Parental Controls";
        break;
      // 5xx Server Error
      case 500:
        result = "Internal Server Error";
        break;
      case 501:
        result = "Not Implemented";
        break;
      case 502:
        result = "Bad Gateway";
        break;
      case 503:
        result = "Service Unavailable";
        break;
      case 504:
        result = "Gateway Timeout";
        break;
      case 505:
        result = "Version Not Supported";
        break;
      case 506:
        result = "Variant Also Negotiates";
        break;
      case 507:
        result = "Insufficient Storage";
        break;
      case 509:
        result = "Bandwidth Limit Exceeded";
        break;
      case 510:
        result = "Not Extended";
        break;
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name + " (" + code + ")";
  }

}
