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

import ch.o2it.weblounge.common.request.WebloungeResponse;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Utility class used to facilitate in creating <code>HTTP 1.1</code> responses
 * and header analysis.
 * 
 * TODO: use set instead of array
 */
public class Http11Utils implements Http11Constants {

  /** the request methods supported by this handler */
  public static final String DEFAULT_METHODS[] = {
      METHOD_GET,
      METHOD_HEAD,
      METHOD_POST };

  /** the default allow string */
  public static final String DEFAULT_ALLOW = buildAllowString(DEFAULT_METHODS);

  /**
   * Constructs an HTTP "Allow" response header for the allowed request methods.
   * 
   * @param methods
   *          the allowed methods
   * @return an "Allow" response header
   */
  private static String buildAllowString(String methods[]) {
    StringBuffer b = new StringBuffer(METHOD_OPTIONS);
    for (int i = 0; i < methods.length; i++) {
      b.append(',');
      b.append(methods[i]);
    }
    return b.toString();
  }

  /**
   * Checks the request against the default request methods that all <code>
	 * RequestHandlers</code>
   * must handle. These are:
   * <ul>
   * <li>GET
   * <li>HEAD
   * <li>POST
   * </ul>
   * The OPTIONS request method is handled transparently. <br>
   * If the check succeeds, the caller can assume that the request methods is
   * one of the default methods and should be handled accordingly. <br>
   * If the check fails, the request is either an OPTIONS request or is not
   * allowed. But is has already been handled and the caller doesn't need to do
   * any further request handling.
   * 
   * @param method
   *          the request method to check against and handle
   * @param response
   *          the response of the request
   * @return <code>true</code>if the check succeeded and the caller can handle
   *         the default methods, <code>false</code> if the check failed and the
   *         request has already been handled.
   */
  public static boolean checkDefaultMethods(String method,
      HttpServletResponse response) {
    // handle OPTIONS requests
    if (METHOD_OPTIONS.equals(method)) {
      response.setHeader(HEADER_ALLOW, DEFAULT_ALLOW);
      response.setContentLength(0);
      return false;
    }

    // check whether the methods is allowed
    for (int i = 0; i < DEFAULT_METHODS.length; i++)
      if (DEFAULT_METHODS[i].equals(method))
        return true;

    // invalid request method
    response.setHeader(HEADER_ALLOW, DEFAULT_ALLOW);
    try {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    } catch (IOException e) { /* ignore */
    }
    return false;
  }

  /**
   * Checks the request against a custom set of supported methods.
   * 
   * @see Http11Utils#checkDefaultMethods(String, WebloungeResponse)
   * @param method
   *          the request method to check against and handle
   * @param response
   *          the response of the request
   * @param methods
   * @return <code>true</code>if the check succeeded and the caller can handle
   *         the default methods, <code>false</code> if the check failed and the
   *         request has already been handled.
   */
  public static boolean checkMethods(String method,
      HttpServletResponse response, String methods[]) {
    // handle OPTIONS requests
    if (METHOD_OPTIONS.equals(method)) {
      response.setHeader(HEADER_ALLOW, buildAllowString(methods));
      response.setContentLength(0);
      return false;
    }

    // check whether the methods is allowed
    for (int i = 0; i < methods.length; i++)
      if (methods[i].equals(method))
        return true;

    // invalid request method
    response.setHeader(HEADER_ALLOW, buildAllowString(methods));
    try {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    } catch (IOException e) { /* ignore */
    }
    return false;

  }

  /**
   * Prepares a response for handling a HEAD request using a GET handler. <br>
   * NOTE: This method should not be used in conjunction with the cache, since
   * the cache can do a much better job in handling HEAD requests than just
   * discarding all its output! <br>
   * NOTE: This method should not be used with the Http11ProtocolHandler, since
   * it already handles HEAD requests correctly and much more efficiently!
   * 
   * @param resp
   *          the original response
   */
  public static void startHeadResponse(WebloungeResponse resp) {
    WebloungeResponseImpl r = (WebloungeResponseImpl) resp;
    r.setResponse(new DiscardBodyResponse((HttpServletResponse) r.getResponse()));
  }

  /**
   * Finishes a HEAD response that was started with <code>startHeadResponse()
	 * <code>.
   * 
   * @param resp
   *          the original response
   */
  public static void endHeadResponse(WebloungeResponse resp) {
    DiscardBodyResponse b = (DiscardBodyResponse) ((WebloungeResponseImpl) resp).getResponse();
    b.finishOutput();
  }

  /**
   * Calculates an ETag for a given modification time.
   * 
   * @param modified
   *          the modification time in milliseconds since the epoch
   * @return String the ETag
   */
  public static String calcETag(long modified) {
    return "\"WL-" + Long.toHexString(modified) + "\"";
  }

  /**
   * A servlet response that make sure no HTTP response body is sent back to the
   * client.
   */
  private static class DiscardBodyResponse extends HttpServletResponseWrapper {
    /** the servlet output stream that discards all output */
    private NullOutputStream os = new NullOutputStream();

    /** the servlet writer that discards all output */
    private PrintWriter pw;

    /**
     * Creates a new <code>DiscardBodyResponse</code>.
     * 
     * @param resp
     *          the original response
     */
    protected DiscardBodyResponse(HttpServletResponse resp) {
      super(resp);
    }

    /**
     * Ends a response and makes sure, the content length is set correctly.
     */
    protected void finishOutput() {
      getResponse().setContentLength(os.getBytesWritten());
    }

    /**
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
      return os;
    }

    /**
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter() throws IOException {
      if (pw == null)
        pw = new PrintWriter(new OutputStreamWriter(os, getResponse().getCharacterEncoding()));
      return pw;
    }
  }

  /**
   * Output stream that discards all output and only counts the number of bytes
   * that have been written.
   */
  static class NullOutputStream extends ServletOutputStream {

    /** the number of bytes that have been written to this stream */
    private int bytesWritten = 0;

    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
      bytesWritten += len;
    }

    /**
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
      bytesWritten++;
    }

    /**
     * Returns the number of bytes written to this stream.
     * 
     * @return the number of bytes written to this stream
     */
    protected int getBytesWritten() {
      return bytesWritten;
    }

  }
}
