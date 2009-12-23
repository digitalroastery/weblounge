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

package ch.o2it.weblounge.cache.impl.filter;

import ch.o2it.weblounge.cache.StreamFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

/**
 * This stream writer will analyze the output stream and report problems
 * (warnings and errors) related to the html standard to the console.
 */
public class TidyReport implements StreamFilter {

  /** Tidy thread */
  private TidyThread tt = null;

  /** The logger */
  protected static Logger log = LoggerFactory.getLogger(TidyReport.class.getName());

  /** Name of the output stream */
  private String name = null;

  /**
   * @see ch.o2it.weblounge.api.request.StreamFilter#filter(java.lang.StringBuffer,
   *      java.lang.String)
   */
  public StringBuffer filter(StringBuffer buffer, String contentType) {
    if (tt == null)
      tt = new TidyThread(name);
    try {
      tt.los.write(buffer.toString().getBytes());
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    return buffer;
  }

  /**
   * @see ch.o2it.weblounge.api.request.StreamFilter#flush()
   */
  public StringBuffer flush() {
    return null;
  }

  /**
   * @see ch.o2it.weblounge.api.request.StreamFilter#close()
   */
  public void close() {
    if (tt == null)
      return;
    try {
      tt.los.flush();
      tt.los.close();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
    try {
      tt.join();
    } catch (InterruptedException e) {
      log.error(e.getMessage(), e);
    }
    int err = tt.getErrors();
    int wrn = tt.getWarnings();
    if (err > 0 || wrn > 0) {
      log.warn("Filter report for {}: document contains {} warning(s) and {} error(s)", new Object[] {name, wrn, err});
      log.info(tt.wr.toString());
    } else
      log.info("Filter report for {}: document clean", name);
    tt = null;
    name = null;
  }

  /**
   * Tidy worker thread implementation.
   */
  private static class TidyThread extends Thread {

    protected PipedOutputStream los;
    private PipedInputStream tis;
    private Tidy tidy;
    protected CharArrayWriter wr;

    private static int id = 0;

    /**
     * Creates a new thread that will parse the output stream for html related
     * structure problems.
     * 
     * @param name
     */
    public TidyThread(String name) {
      super("Tidy Thread " + (++id));
      setDaemon(true);
      try {
        tis = new PipedInputStream();
        los = new PipedOutputStream(tis);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
      tidy = new Tidy();
      wr = new CharArrayWriter(500);
      tidy.setErrout(new PrintWriter(wr));
      if (name != null)
        tidy.setInputStreamName(name);
      start();
    }

    /**
     * Executes parsing.
     * 
     * {@inheritDoc}
     * 
     * @see java.lang.Thread#run()
     */
    public void run() {
      if (tidy != null)
        tidy.parse(tis, null);
    }

    /**
     * Returns the number of parser errors.
     * 
     * @return the number of errors
     */
    public int getErrors() {
      return tidy == null ? 0 : tidy.getParseErrors();
    }

    /**
     * Returns the number of parser warnings.
     * 
     * @return the number of warnings
     */
    public int getWarnings() {
      return tidy == null ? 0 : tidy.getParseWarnings();
    }

  }

}