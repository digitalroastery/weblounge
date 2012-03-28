/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.common.impl.util.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.net.URL;

/**
 * Error handler for schema validation that will log the errors to the console.
 */
public class ValidationErrorHandler implements ErrorHandler {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ValidationErrorHandler.class);

  /** The module descriptor */
  private String name = null;

  /** Number of errors */
  private int errors = 0;

  /** Number of warnings */
  private int warnings = 0;

  /**
   * Creates a new validator which will handle errors that occur while reading
   * the resource located at <code>resourceURL</code>.
   * 
   * @param resourceURL
   *          the module descriptor
   */
  public ValidationErrorHandler(URL resourceURL) {
    this.name = resourceURL.toExternalForm();
  }

  /**
   * Creates a new validator which will handle errors that occur while reading
   * the resource identified by <code>name</code>.
   * 
   * @param name
   *          the module descriptor
   */
  public ValidationErrorHandler(String name) {
    this.name = name;
  }

  /**
   * Returns <code>true</code> if there were any errors while parsing.
   * 
   * @return <code>true</code> if there were errors
   */
  public boolean hasErrors() {
    return errors > 0;
  }

  /**
   * Returns <code>true</code> if there were any warnings while parsing.
   * 
   * @return <code>true</code> if there were warnings
   */
  public boolean hasWarnings() {
    return warnings > 0;
  }

  /**
   * Resets the error handler.
   */
  public void reset() {
    errors = 0;
    warnings = 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  public void error(SAXParseException exception) throws SAXException {
    errors++;
    logger.error("Error parsing " + name + ": " + exception.getMessage());
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  public void fatalError(SAXParseException exception) throws SAXException {
    errors++;
    logger.error("Fatal error parsing " + name + ": " + exception.getMessage());
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  public void warning(SAXParseException exception) throws SAXException {
    warnings++;
    logger.error("Problem found while parsing " + name + ": " + exception.getMessage());
  }

}
