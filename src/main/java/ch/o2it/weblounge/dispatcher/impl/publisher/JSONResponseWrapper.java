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

package ch.o2it.weblounge.dispatcher.impl.publisher;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Servlet response wrapper that will convert the response from <code>xml</code>
 * to <code>json</code>.
 */
public class JSONResponseWrapper extends HttpServletResponseWrapper {

  /** Byte array used to buffer the servlet output */
  private ByteArrayServletOutputStream servletOutputStream = null;

  /** Print writer */
  private PrintWriter printWriter = null;

  /** The character encoding */
  protected String encoding = null;

  /**
   * Creates a new response wrapper that will convert the response from
   * <code>xml</code> to <code>json</code>.
   * 
   * @param response
   *          the response to wrap
   */
  public JSONResponseWrapper(HttpServletResponse response) {
    super(response);
    response.setContentType("text/x-json");
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponseWrapper#setContentLength(int)
   */
  @Override
  public void setContentLength(int len) {
    // ignore
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponseWrapper#setContentType(java.lang.String)
   */
  @Override
  public void setContentType(String type) {
    // ignore
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponseWrapper#getOutputStream()
   */
  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (printWriter != null)
      throw new IllegalStateException("Servlet already accessed print writer");

    if (servletOutputStream == null) {
      servletOutputStream = new ByteArrayServletOutputStream();
    }
    return servletOutputStream;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponseWrapper#getWriter()
   */
  @Override
  public PrintWriter getWriter() throws IOException {
    if (printWriter == null && servletOutputStream != null)
      throw new IllegalStateException("Servlet already accessed output stream");

    if (printWriter == null) {
      servletOutputStream = new ByteArrayServletOutputStream();
      encoding = getResponse().getCharacterEncoding();
      Writer writer = new OutputStreamWriter(servletOutputStream, encoding);
      printWriter = new PrintWriter(writer);
    }
    return printWriter;
  }

  /**
   * Finishes the response by converting the current content of the buffered
   * servlet output stream to json and writing it to the original response.
   * 
   * @throws IOException
   *           if writing the response fails
   * @throws JSONException
   *           if the conversion to json failed
   */
  public void finishResponse() throws IOException, JSONException {
    if (servletOutputStream != null) {
      if (printWriter != null) {
        printWriter.flush();
      }

      // Convert the current xml content to json
      Writer responseWriter = getResponse().getWriter();
      String xml = new String(servletOutputStream.getBytes(), encoding);
      JSONObject json = XML.toJSONObject(xml);

      // Write json back to the response
      responseWriter.write(json.toString());
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponseWrapper#resetBuffer()
   */
  @Override
  public void resetBuffer() {
    servletOutputStream = null;
    printWriter = null;
    super.resetBuffer();
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.servlet.ServletResponseWrapper#reset()
   */
  @Override
  public void reset() {
    servletOutputStream = null;
    printWriter = null;
    super.reset();
  }

}