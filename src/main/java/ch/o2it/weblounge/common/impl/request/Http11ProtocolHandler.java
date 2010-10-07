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

import ch.o2it.weblounge.common.Times;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>Http11ProtocolHandler</code> analyzes HTTP 1.1 request headers and
 * decides what response to generate. It includes support for the following
 * features as defined in RFC2616:
 * <ul>
 * <li>external cache control using Last-Modified, ETag and Expires headers
 * <li>support for conditional requests using If-Modified-Since, If-None-Match
 * If-Unmodified-Since and If-Match headers
 * <li>support for partial requests using Rage and If-Range headers
 * <li>generates the following replies based on the request headers:
 * <ul>
 * <li>200 OK replies
 * <li>206 Partial Content
 * <li>304 Not Modified
 * <li>405 Method Not Allowed
 * <li>412 Precondition Failed
 * <li>416 Requested Range Not Satisfiable
 * <li>500 Internal Server Error
 * </ul>
 * </ul>
 * 
 * @see ftp://ftp.rfc-editor.org/in-notes/rfc2616.txt
 */
public class Http11ProtocolHandler implements Times, Http11Constants {

  /** Logging facility */
  private static final Logger log = LoggerFactory.getLogger(Http11ProtocolHandler.class);

  /**
   * This response type indicates a "501 Internal Server Error" response
   * required
   **/
  public static final int RESPONSE_INTERNAL_SERVER_ERROR = 0;

  /** This response type indicates a "200 OK" response required */
  public static final int RESPONSE_OK = 1;

  /** This response type indicates a "206 Partial Content" response required */
  public static final int RESPONSE_PARTIAL_CONTENT = 2;

  /** This response type indicates a "304 Not Modified" response required */
  public static final int RESPONSE_NOT_MODIFIED = 3;

  /**
   * This response type indicates a "412 Precondition Failed" response required
   **/
  public static final int RESPONSE_PRECONDITION_FAILED = 4;

  /**
   * This response type indicates a "416 Requested Range Not Satisfiable"
   * response required
   **/
  public static final int RESPONSE_REQUESTED_RANGE_NOT_SATISFIABLE = 5;

  /**
   * This response type indicates a "405 Method Not Allowed" response required
   */
  public static final int RESPONSE_METHOD_NOT_ALLOWED = 6;

  /** unknown response, just in case... */
  public static final int RESPONSE_UNKNOWN = 7;

  /** statistics constant for the number of analyzed requests */
  public static final int STATS_ANALYZED = 0;

  /** statistics constant for the number of response headers generated */
  public static final int STATS_HEADER_GENERATED = 1;

  /** statistics constant for the number of response bodies generated */
  public static final int STATS_BODY_GENERATED = 2;

  /** statistics constant for the numer of bytes written */
  public static final int STATS_BYTES_WRITTEN = 3;

  /** the number of errors while writing the response */
  public static final int STATS_ERRORS = 4;

  /** calculated statisical values */
  public static final int STATS_BYTES_PER_RESPONSE = 20;

  /** the number of general statistics counters */
  protected static final int STATS_NOF_COUNTERS = 5;

  /** the maximum number of response counters */
  protected static final int STATS_NOF_RESPONSE = 8;

  /** the size if the temporary buffer */
  private static final int BUFFER_SIZE = 8 * 1024;

  /** protocol handler statistics */
  protected static long stats[] = new long[STATS_NOF_COUNTERS];

  /** per response code header statistics */
  protected static long headerStats[] = new long[STATS_NOF_RESPONSE];

  /** per response code body statistics */
  protected static long bodyStats[] = new long[STATS_NOF_RESPONSE];

  /** holds a temporary buffer for data copying */
  private static final ThreadLocal<byte[]> buffer = new ThreadLocal<byte[]>();

  /**
   * Method isError.
   * 
   * @param type
   * @return <code>true</code> if the responsetype is an error
   */
  public static final boolean isError(Http11ResponseType type) {
    return type.type != RESPONSE_OK && type.type != RESPONSE_PARTIAL_CONTENT;
  }

  /**
   * Method analyzeRequest.
   * 
   * @param req
   * @param modified
   * @param expires
   * @param size
   * @return Http11ResponseType
   */
  public static Http11ResponseType analyzeRequest(HttpServletRequest req,
      long modified, long expires, long size) {

    /* adjust the statistics */
    ++stats[STATS_ANALYZED];

    /* the response type */
    Http11ResponseType type = new Http11ResponseType(RESPONSE_INTERNAL_SERVER_ERROR, modified, expires);
    type.size = size;

    /* calculate the etag */
    String eTag = Http11Utils.calcETag(modified);

    /* decode the conditional headers */
    long ifModifiedSince = -1;
    try {
      ifModifiedSince = req.getDateHeader(HEADER_IF_MODIFIED_SINCE);
    } catch (IllegalArgumentException e) {
      log.debug("Client provided malformed '{}' header: {}", HEADER_IF_MODIFIED_SINCE, req.getDateHeader(HEADER_IF_MODIFIED_SINCE));
    }
    String ifNoneMatch = req.getHeader(HEADER_IF_NONE_MATCH);

    long ifUnmodifiedSince = -1;
    try {
      ifUnmodifiedSince = req.getDateHeader(HEADER_IF_UNMODIFIED_SINCE);
    } catch (IllegalArgumentException e) {
      log.debug("Client provided malformed '{}' header: {}", HEADER_IF_UNMODIFIED_SINCE, req.getDateHeader(HEADER_IF_UNMODIFIED_SINCE));
    }
    
    String ifMatch = req.getHeader(HEADER_IF_MATCH);
    String method = req.getMethod();
    type.headerOnly = method.equals(METHOD_HEAD);
    boolean reqGetHead = method.equals(METHOD_GET) || type.headerOnly;
    boolean ifNoneMatchMatch = matchETag(eTag, ifNoneMatch);

    /* method */
    if (!reqGetHead && !method.equals(METHOD_POST)) {
      type.type = RESPONSE_METHOD_NOT_ALLOWED;
      return type;
    }

    /* check e-tag */
    if (ifNoneMatch != null && ifNoneMatchMatch && reqGetHead) {
      type.type = RESPONSE_NOT_MODIFIED;
      return type;
    }
    
    /* check not modified */
    if (ifNoneMatch == null && ifModifiedSince != -1 && modified < ifModifiedSince + MS_PER_SECOND) {
      type.type = RESPONSE_NOT_MODIFIED;
      return type;
    }
    
    /* precondition check failed */
    if (ifNoneMatch != null && ifNoneMatchMatch && !reqGetHead) {
      log.error("412 PCF: Method={}, If-None-Match={}, match={}", new Object[] {
          req.getMethod(),
          ifNoneMatch,
          ifNoneMatchMatch });
      log.info("If-None-Match header only supported in GET or HEAD requests.");
      type.type = RESPONSE_PRECONDITION_FAILED;
      type.err = "If-None-Match header only supported in GET or HEAD requests.";
      return type;
    }
    if (ifUnmodifiedSince != -1 && modified > ifUnmodifiedSince) {
      log.error("412 PCF: modified={} > ifUnmodifiedSince={}", modified, ifUnmodifiedSince);
      log.info("If-Unmodified-Since precondition check failed.");
      type.type = RESPONSE_PRECONDITION_FAILED;
      type.err = "If-Unmodified-Since precondition check failed.";
      return type;
    }
    if (ifMatch != null && !matchETag(eTag, ifMatch)) {
      log.error("412 PCF: !matchETag({}, {})", eTag, ifMatch);
      log.info("If-match precondition check failed.");
      type.type = RESPONSE_PRECONDITION_FAILED;
      type.err = "If-match precondition check failed.";
      return type;
    }

    /* decode the range headers */
    if (size >= 0) {
      // PENDING: handle ranges
    }

    /* return the result */
    type.type = RESPONSE_OK;
    return type;
  }

  /**
   * Method matchETag.
   * 
   * @param eTag
   * @param eTagList
   * @return boolean
   */
  protected static boolean matchETag(String eTag, String eTagList) {
    if (eTagList == null || eTag == null)
      return false;
    String s = null;
    StringTokenizer t = new StringTokenizer(eTagList, ",");
    while (t.hasMoreTokens()) {
      s = t.nextToken().trim();
      if ("*".equals(s) || s.equals(eTag))
        return true;
    }
    return false;
  }

  /**
   * Method generateResponse.
   * 
   * @param resp
   * @param type
   * @param buf
   * @return boolean
   * @throws IOException
   *           if generating the response fails
   */
  public static boolean generateResponse(HttpServletResponse resp,
      Http11ResponseType type, byte[] buf) throws IOException {

    return generateResponse(resp, type, new ByteArrayInputStream(buf));
  }

  /**
   * Method generateResponse.
   * 
   * @param resp
   * @param type
   * @param is
   * @return boolean
   * @throws IOException
   *           if generating the response fails
   */
  public static boolean generateResponse(HttpServletResponse resp,
      Http11ResponseType type, InputStream is) throws IOException {

    /* first generate the response headers */
    generateHeaders(resp, type);

    /* adjust the statistics */
    ++stats[STATS_BODY_GENERATED];
    incResponseStats(type.type, bodyStats);

    /* generate the response body */
    try {
      if (resp.isCommitted())
        log.warn("Response is already committed!");
      switch (type.type) {
        case RESPONSE_OK:
          if (!type.isHeaderOnly() && is != null) {
            resp.setBufferSize(BUFFER_SIZE);
            OutputStream os = null;
            try {
              os = resp.getOutputStream();
              IOUtils.copy(is, os);
            } catch (SocketException e) {
              log.debug("Request canceled by client");
            } finally {
              IOUtils.closeQuietly(os);
            }
          }
          break;

        case RESPONSE_PARTIAL_CONTENT:
          if (type.from < 0 || type.to < 0 || type.from > type.to || type.to > type.size) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid partial content parameters");
            log.warn("Invalid partial content parameters");
          } else if (!type.isHeaderOnly() && is != null) {
            resp.setBufferSize(BUFFER_SIZE);
            OutputStream os = resp.getOutputStream();
            if (is.skip(type.from) != type.from) {
              resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Premature end of input stream");
              log.warn("Premature end of input stream");
              break;
            }
            try {

              /* get the temporary buffer for this thread */
              byte tmp[] = buffer.get();
              if (tmp == null) {
                tmp = new byte[BUFFER_SIZE];
                buffer.set(tmp);
              }

              int read = type.to - type.from;
              int copy = read;
              int write = 0;

              while (copy > 0 && (read = is.read(tmp)) >= 0) {
                write = (copy -= read > 0 ? read : read + copy);
                os.write(tmp, 0, write);
                stats[STATS_BYTES_WRITTEN] += write;
              }
              if (copy > 0) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Premature end of input stream");
                log.warn("Premature end of input stream");
                break;
              }
              os.flush();
              os.close();
            } catch (SocketException e) {
              log.debug("Request cancelled by client");
            }
          }
          break;

        case RESPONSE_NOT_MODIFIED:
          /* NOTE: we MUST NOT return any content (RFC 2616)!!! */
          break;

        case RESPONSE_PRECONDITION_FAILED:
          if (type.err == null)
            resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
          else
            resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, type.err);
          break;

        case RESPONSE_REQUESTED_RANGE_NOT_SATISFIABLE:
          if (type.err == null)
            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
          else
            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, type.err);
          break;

        case RESPONSE_METHOD_NOT_ALLOWED:
          if (type.err == null)
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
          else
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, type.err);
          break;

        case RESPONSE_INTERNAL_SERVER_ERROR:
        default:
          if (type.err == null)
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          else
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, type.err);
      }
    } catch (IOException e) {
      if (e instanceof EOFException) {
        log.debug("Request canceled by client");
        return true;
      }
      ++stats[STATS_ERRORS];
      String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      Throwable cause = e.getCause() != null ? e.getCause() : e;
      log.warn("I/O exception while sending response: {}", message, cause);
      throw e;
    }

    return true;
  }

  /**
   * Method generateHeaders.
   * 
   * @param resp
   * @param type
   */
  public static void generateHeaders(HttpServletResponse resp,
      Http11ResponseType type) {

    /* generate headers only once! */
    if (type.headers)
      return;
    type.headers = true;

    /* adjust the statistics */
    ++stats[STATS_HEADER_GENERATED];
    incResponseStats(type.type, headerStats);

    /* set the date header */
    resp.setDateHeader(HEADER_DATE, type.time);

    /* check expires */
    if (type.expires > type.time + MS_PER_YEAR) {
      type.expires = type.time + MS_PER_YEAR;
      log.warn("Expiration date too far in the future. Adjusting.");
    }

    /* set the standard headers and status code */
    switch (type.type) {
      case RESPONSE_PARTIAL_CONTENT:
        if (type.expires > type.time)
          resp.setDateHeader(HEADER_EXPIRES, type.expires);
        if (type.modified > 0) {
          resp.setHeader(HEADER_ETAG, Http11Utils.calcETag(type.modified));
          resp.setDateHeader(HEADER_LAST_MODIFIED, type.modified);
        }
        if (type.size < 0 || type.from < 0 || type.to < 0 || type.from > type.to || type.to > type.size) {
          resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          break;
        }
        resp.setContentLength((int) type.size);
        resp.setHeader(HEADER_CONTENT_RANGE, "bytes " + type.from + "-" + type.to + "/" + type.size);
        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        break;

      case RESPONSE_OK:
        if (type.expires > type.time)
          resp.setDateHeader(HEADER_EXPIRES, type.expires);
        if (type.modified > 0) {
          resp.setHeader(HEADER_ETAG, Http11Utils.calcETag(type.modified));
          resp.setDateHeader(HEADER_LAST_MODIFIED, type.modified);
        }
        if (type.size >= 0)
          resp.setContentLength((int) type.size);
        resp.setStatus(HttpServletResponse.SC_OK);
        break;

      case RESPONSE_NOT_MODIFIED:
        if (type.expires > type.time)
          resp.setDateHeader(HEADER_EXPIRES, type.expires);
        if (type.modified > 0)
          resp.setHeader(HEADER_ETAG, Http11Utils.calcETag(type.modified));
        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        break;

      case RESPONSE_METHOD_NOT_ALLOWED:
        resp.setHeader(HEADER_ALLOW, "GET, POST, HEAD");
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        break;

      case RESPONSE_PRECONDITION_FAILED:
        if (type.expires > type.time)
          resp.setDateHeader(HEADER_EXPIRES, type.expires);
        if (type.modified > 0) {
          resp.setHeader(HEADER_ETAG, Http11Utils.calcETag(type.modified));
          resp.setDateHeader(HEADER_LAST_MODIFIED, type.modified);
        }
        resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
        break;

      case RESPONSE_REQUESTED_RANGE_NOT_SATISFIABLE:
        if (type.expires > type.time)
          resp.setDateHeader(HEADER_EXPIRES, type.expires);
        if (type.modified > 0) {
          resp.setHeader(HEADER_ETAG, Http11Utils.calcETag(type.modified));
          resp.setDateHeader(HEADER_LAST_MODIFIED, type.modified);
        }
        if (type.size >= 0)
          resp.setHeader(HEADER_CONTENT_RANGE, "*/" + type.size);
        break;

      case RESPONSE_INTERNAL_SERVER_ERROR:
      default:
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Method incResponseStatistics.
   * 
   * @param type
   * @param stats
   */
  protected static void incResponseStats(int type, long stats[]) {
    if (type < 0 || type >= stats.length)
      ++stats[RESPONSE_UNKNOWN];
    ++stats[type];
  }

  /**
   * Method getStatistics.
   * 
   * @param value
   * @return
   */
  public static long getStatistics(int value) {
    if (value >= 0 && value < stats.length)
      return stats[value];
    switch (value) {
      case STATS_BYTES_PER_RESPONSE:
        return (stats[STATS_BODY_GENERATED] > 0) ? stats[STATS_BYTES_WRITTEN] / stats[STATS_BODY_GENERATED] : 0;
      default:
        return -1;
    }
  }

  /**
   * Method getHeaderStatistics.
   * 
   * @param value
   * @return
   */
  public static long getHeaderStatistics(int value) {
    return getResponseStats(value, headerStats);
  }

  /**
   * Method getBodyStatistics.
   * 
   * @param value
   * @return
   */
  public static long getBodyStatistics(int value) {
    return getResponseStats(value, bodyStats);
  }

  /**
   * Method getRealStats.
   * 
   * @param value
   * @param values
   * @return
   */
  protected static long getResponseStats(int value, long values[]) {
    if (value < 0 || value >= values.length)
      return -1;
    return values[value];
  }

  /**
   * Resets the statistical values.
   */
  public static void resetStatistics() {
    stats = new long[STATS_NOF_COUNTERS];
    headerStats = new long[STATS_NOF_RESPONSE];
    bodyStats = new long[STATS_NOF_RESPONSE];
  }
}
