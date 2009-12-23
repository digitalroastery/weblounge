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

package ch.o2it.weblounge.common.impl.util.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Provides various utility routines for xml handling.
 */
public class XMLUtilities {

  /** the DOM parser factory */
  private static final DocumentBuilderFactory docBuilderFactory_;

  /** initialize the DOM parser factory */
  static {
    docBuilderFactory_ = DocumentBuilderFactory.newInstance();
    docBuilderFactory_.setNamespaceAware(true);
  }

  /** the SAX parser factory */
  private static final SAXParserFactory saxParserFactory_;

  /** initialize the SAX parser factory */
  static {
    saxParserFactory_ = SAXParserFactory.newInstance();
    saxParserFactory_.setNamespaceAware(true);
  }

  /** the transformer factory */
  private static final TransformerFactory transformerFactory_;

  /** initialize the transformer factory */
  static {
    // try to use a compiling transformer
    String key = "javax.xml.transform.TransformerFactory";
    String ovalue = "org.apache.xalan.processor.TransformerFactoryImpl";
    String nvalue = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
    if (ovalue.equals(System.getProperty(key)))
      System.setProperty(key, nvalue);
    transformerFactory_ = TransformerFactory.newInstance();
  }

  /** the xpath factory */
  private static final XPathFactory xpathFactory_;

  /** initialize the XPath factory */
  static {
    xpathFactory_ = XPathFactory.newInstance();
  }

  /**
   * Constructor for class XMLUtilities.
   */
  private XMLUtilities() { /* no instances */
  }

  /**
   * Returns <code>true</code> if this installation features a dom parser for
   * level <code>level</code>. The level usually is provided as a version string
   * like <tt>2.0</tt>.
   * 
   * @param level
   *          the dom level to support
   * @return <code>true</code> if a corresponding dom level parser could be
   *         found
   */
  public static boolean isDomLevelSupported(String level) {
    try {
      return getDocumentBuilder().getDOMImplementation().hasFeature("Core", level);
    } catch (ParserConfigurationException e) {
      return false;
    }
  }

  /**
   * Returns a new DOM parser.
   * 
   * @return a new DOM parser
   * @throws ParserConfigurationException
   *           if no parser has been registered
   */
  public static DocumentBuilder getDocumentBuilder()
      throws ParserConfigurationException {
    synchronized (docBuilderFactory_) {
      return docBuilderFactory_.newDocumentBuilder();
    }
  }

  public static void parse(InputStream is, DefaultHandler handler)
      throws SAXException, IOException, ParserConfigurationException {
    getSAXParser().parse(is, handler);
  }

  /**
   * Returns a new SAX parser.
   * 
   * @return a new SAX parser.
   * @throws ParserConfigurationException
   *           if no parser has been registered
   * @throws SAXException
   */
  public static SAXParser getSAXParser() throws ParserConfigurationException,
      SAXException {
    synchronized (saxParserFactory_) {
      return saxParserFactory_.newSAXParser();
    }
  }

  /**
   * Returns a new identity transformer.
   * 
   * @return a new transformer
   * @throws TransformerConfigurationException
   *           if no transformer has been registered
   */
  public static Transformer getTransformer()
      throws TransformerConfigurationException {
    synchronized (transformerFactory_) {
      return transformerFactory_.newTransformer();
    }
  }

  /**
   * Returns a new transformer for the given source.
   * 
   * @param source
   *          the source for the transformer
   * @return a new transformer
   * @throws TransformerConfigurationException
   *           if no transformer has been registered
   */
  public static Transformer getTransformer(Source source)
      throws TransformerConfigurationException {
    synchronized (transformerFactory_) {
      return transformerFactory_.newTransformer(source);
    }
  }

  /**
   * Returns a new transformer template for the given source (precompiled
   * version of the source).
   * 
   * @param source
   *          the source for the template
   * @return a new transformer template
   * @throws TransformerConfigurationException
   *           if no transformer has been registered
   */
  public static Templates getTemplates(Source source)
      throws TransformerConfigurationException {
    synchronized (transformerFactory_) {
      return transformerFactory_.newTemplates(source);
    }
  }

  /**
   * Returns a new XPath.
   * 
   * @return a new XPath
   */
  public static XPath getXPath() {
    synchronized (xpathFactory_) {
      return xpathFactory_.newXPath();
    }
  }

}