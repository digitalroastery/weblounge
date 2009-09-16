/*
 * Weblounge: Web Content Management System Copyright (c) 2007 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.common.impl.url;

import ch.o2it.weblounge.common.url.Url;

import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * The <code>UrlTokenize</code> is a utility class that supports implementation
 * of the <code>Url</code> interface to provide the methods related to
 * <code>UrlComponents</code>.
 * 
 * @author Tobias Wunden
 * @version Jan 24, 2003
 */

public class UrlTokenizer implements Enumeration {

  /** the supporting string tokenizer */
  private StringTokenizer tok;

  /**
   * Creates a new <code>UrlTokenizer</code> which may be used to divide a
   * string representation of a url into its <code>UrlComponent</code> parts.
   * 
   * @param url
   *          the string representation of the url
   * @param separator
   *          the path separator
   */
  public UrlTokenizer(String url, String separator) {
    tok = new StringTokenizer(url, separator);
  }

  /**
   * Creates a new tokenizer from the given url.
   * 
   * @param url
   *          the tokenizer
   */
  public UrlTokenizer(Url url) {
    this(url.getPath(), Character.toString(url.getSeparator()));
  }

  /**
   * Returns true if the tokenizer has more url path elements to process.
   * 
   * @return <code>true</code> if there is a next components
   */
  public boolean hasMoreElements() {
    return tok.hasMoreTokens();
  }

  /**
   * Returns the next path element. Be sure to call <code>hasMoreElements()
	 * </code> before calling this
   * method, otherwise this method will return <code>null</code>.
   * 
   * @return the next url component or <code>null</code> if there is no next
   *         component
   * @see java.util.Enumeration#nextElement()
   */
  public Object nextElement() {
    return tok.nextElement();
  }

}