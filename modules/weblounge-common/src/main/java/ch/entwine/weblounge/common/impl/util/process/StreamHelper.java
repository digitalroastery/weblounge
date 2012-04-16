/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.impl.util.process;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Helper class to handle Runtime.exec() output.
 */
public final class StreamHelper extends Thread {

  /** The input stream */
  private InputStream inputStream;

  /** The output stream */
  private OutputStream outputStream;

  /** The content buffer */
  protected StringBuffer contentBuffer = null;

  /** the output writer */
  protected PrintWriter writer = null;

  /** Append messages to this logger */
  protected Logger logger = null;

  /** True to keep reading the streams */
  protected boolean keepReading = true;

  /**
   * Creates a new stream helper and immediately starts capturing output from
   * the given stream.
   * 
   * @param inputStream
   *          the input stream
   */
  public StreamHelper(InputStream inputStream) {
    this(inputStream, null, null, null);
  }

  /**
   * Creates a new stream helper and immediately starts capturing output from
   * the given stream. Output will be captured to the given buffer.
   * 
   * @param inputStream
   *          the input stream to read from
   * @param contentBuffer
   *          the buffer to write the captured output to
   */
  public StreamHelper(InputStream inputStream, StringBuffer contentBuffer) {
    this(inputStream, null, null, contentBuffer);
  }

  /**
   * Creates a new stream helper and immediately starts capturing output from
   * the given stream. Output will be captured to the given buffer.
   * 
   * @param inputStream
   *          the input stream to read from
   * @param logger
   *          the logger to append to
   * @param contentBuffer
   *          the buffer to write the captured output to
   */
  public StreamHelper(InputStream inputStream, Logger logger,
      StringBuffer contentBuffer) {
    this(inputStream, null, logger, contentBuffer);
  }

  /**
   * Creates a new stream helper and immediately starts capturing output from
   * the given stream. Output will be captured to the given buffer and also
   * redirected to the provided output stream.
   * 
   * @param inputStream
   *          the input stream to read from
   * @param redirect
   *          a stream to also redirect the captured output to
   * @param contentBuffer
   *          the buffer to write the captured output to
   */
  public StreamHelper(InputStream inputStream, OutputStream redirect,
      StringBuffer contentBuffer) {
    this(inputStream, redirect, null, contentBuffer);
  }

  /**
   * Creates a new stream helper and immediately starts capturing output from
   * the given stream. Output will be captured to the given buffer and also
   * redirected to the provided output stream.
   * 
   * @param inputStream
   *          the input stream to read from
   * @param redirect
   *          a stream to also redirect the captured output to
   * @param logger
   *          the logger to append to
   * @param contentBuffer
   *          the buffer to write the captured output to
   */
  public StreamHelper(InputStream inputStream, OutputStream redirect,
      Logger logger, StringBuffer contentBuffer) {
    this.inputStream = inputStream;
    this.outputStream = redirect;
    this.logger = logger;
    this.contentBuffer = contentBuffer;
    start();
  }

  /**
   * Tells the stream helper to stop reading and exit from the main loop.
   */
  public void stopReading() {
    keepReading = false;
  }

  /**
   * Thread run
   */
  @Override
  public void run() {
    BufferedReader reader = null;
    InputStreamReader isreader = null;
    try {
      if (outputStream != null) {
        writer = new PrintWriter(outputStream);
      }
      isreader = new InputStreamReader(inputStream);
      reader = new BufferedReader(isreader);
      if (reader.ready()) {
        String line = reader.readLine();
        while (keepReading && reader.ready() && line != null) {
          append(line);
          log(line);
          line = reader.readLine();
        }
        if (writer != null)
          writer.flush();
      }
    } catch (IOException ioe) {
      logger.error("Error reading process stream: {}", ioe.getMessage(), ioe);
    } finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(isreader);
      IOUtils.closeQuietly(writer);
    }
  }

  /**
   * This method will write any output from the stream to the the content buffer
   * and the logger.
   * 
   * @param output
   *          the stream output
   */
  protected void append(String output) {
    // Process stream redirects
    if (writer != null) {
      writer.println(output);
    }

    // Fill the content buffer, if one has been assigned
    if (contentBuffer != null) {
      contentBuffer.append(output.trim());
      contentBuffer.append('\n');
    }

    // Append output to logger?
  }

  /**
   * If a logger has been specified, the output is written to the logger using
   * the defined log level.
   * 
   * @param output
   *          the stream output
   */
  protected void log(String output) {
    if (logger != null) {
      logger.info(output);
    }
  }

}
