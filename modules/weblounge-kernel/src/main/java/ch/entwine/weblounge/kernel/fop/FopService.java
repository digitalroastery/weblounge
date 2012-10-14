/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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
package ch.entwine.weblounge.kernel.fop;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.Document;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

/**
 * This service provides easy access to FO transformations.
 */
public class FopService {

  /** XSL-FO namespace URI */
  public static final String foNS = "http://www.w3.org/1999/XSL/Format";

  /** FOP factory */
  private FopFactory fopFactory = FopFactory.newInstance();

  /**
   * Creates a PDF document from the given XLS FO DOM representation using the
   * default FOP factory and FOP user agent and writes the resulting document to
   * the the output stream.
   * 
   * @param xml
   *          the XML document
   * @param xsl
   *          the XSL transformation document
   * @param params
   *          parameter for the XSL transformation
   * @param pdf
   *          the output stream
   */
  public void xml2pdf(Document xml, Document xsl, String[][] params,
      OutputStream pdf) throws TransformerConfigurationException,
      TransformerException, FOPException, IOException {

    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdf);
    xml2pdf(xml, xsl, params, fop, pdf);
  }

  /**
   * Creates a PDF document from the given XSL FO DOM representation using the
   * provided FOP factory and user agent and writes the resulting document to
   * the the output stream.
   * 
   * @param xml
   *          the xml document
   * @param xsl
   *          the XSL transformation document
   * @param params
   *          parameter for the XSL transformation
   * @param fop
   *          the FOP processor
   * @param pdf
   *          the output stream
   */
  public void xml2pdf(Document xml, Document xsl, String[][] params, Fop fop,
      OutputStream pdf) throws TransformerConfigurationException,
      TransformerException, FOPException, IOException {

    // Setup output
    OutputStream pdfOut = new BufferedOutputStream(pdf);

    try {
      // Setup xsl transformer
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer(new DOMSource(xsl));

      // Set the parameter values in the stylesheet
      if (params != null) {
        for (String[] p : params) {
          transformer.setParameter(p[0], p[1]);
        }
      }

      // Setup input for XSLT transformation
      Source src = new DOMSource(xml);

      // Resulting SAX events (the generated FO) must be piped through to FOP
      Result res = new SAXResult(fop.getDefaultHandler());

      // Start XSLT transformation and FOP processing
      transformer.transform(src, res);

    } finally {
      pdfOut.close();
    }

  }

  /**
   * Creates a PDF document from the given XSL FO DOM representation using the
   * provided FOP factory and user agent and writes the resulting document to
   * the the output stream.
   * 
   * @param xml
   *          the xml document
   * @param xsl
   *          the XSL transformation document
   * @param params
   *          parameter for the XSL transformation
   * @param fop
   *          the FOP processor
   * @param foUserAgent
   *          the FOP user agent
   * @param pdf
   *          the output stream
   */
  public void xml2fo(Document xml, Source xsl, String[][] params,
      OutputStream fopOut) throws TransformerConfigurationException,
      TransformerException, FOPException, IOException {

    try {
      // Setup xsl transformer
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer(xsl);

      // Set the parameter values in the stylesheet
      if (params != null) {
        for (String[] p : params) {
          transformer.setParameter(p[0], p[1]);
        }
      }

      // Setup input for XSLT transformation
      Source src = new DOMSource(xml);

      // Resulting SAX events (the generated FO) must be piped through to FOP
      Result res = new StreamResult(fopOut);

      // Start XSLT transformation and FOP processing
      transformer.transform(src, res);
    } finally {
      fopOut.close();
    }
  }

}
