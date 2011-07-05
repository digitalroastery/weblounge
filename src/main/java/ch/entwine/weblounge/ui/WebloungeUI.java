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

package ch.entwine.weblounge.ui;

import ch.entwine.weblounge.common.content.image.ImageStyle;
import ch.entwine.weblounge.common.impl.content.image.ImageStyleImpl;
import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;
import ch.entwine.weblounge.common.impl.util.xml.XPathNamespaceContext;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Support class for the Weblounge user interface.
 */
public final class WebloungeUI {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WebloungeUI.class);
  
  /** File path and name */
  private static final String STYLES_DEFINITION_FILE = "/imagestyles.xml";

  /** The registered image styles */
  private List<ServiceRegistration> styles = new ArrayList<ServiceRegistration>();

  /**
   * OSGi callback for component startup.
   * 
   * @param ctx
   *          the component context
   */
  void activate(ComponentContext ctx) throws Exception {
    BundleContext bundleCtx = ctx.getBundleContext();
    registerImageStyles(bundleCtx);
  }

  /**
   * OSGi callback for component shutdown.
   * 
   * @param ctx
   *          the component context
   */
  void deactivate(ComponentContext ctx) throws Exception {
    BundleContext bundleCtx = ctx.getBundleContext();
    unregisterImageStyles(bundleCtx);
  }

  /**
   * Registers the image styles read from
   * <code>src/main/resources/imagestyles/</code> in the service registry.
   * 
   * @param ctx
   *          the bundle context
   * @throws IOException
   *           if reading the image style definitions fails
   * @throws SAXException
   *           if setting up the sax parser fails
   * @throws ParserConfigurationException
   *           if configuring the parser fails
   */
  private void registerImageStyles(BundleContext ctx) throws SAXException,
      IOException, ParserConfigurationException {

    logger.info("Registering weblounge ui imagestyles");

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(new XPathNamespaceContext(true));
    
    // Load the style definitions from disk
    URL stylesDefinition = this.getClass().getResource(STYLES_DEFINITION_FILE);
    Document doc = docBuilder.parse(stylesDefinition.openStream());

    // Register the styles as services
    NodeList imagestyleNodes = XPathHelper.selectList(doc, "imagestyles/imagestyle", xpath);
    for (int i = 0; i < imagestyleNodes.getLength(); i++) {
      ImageStyle style = ImageStyleImpl.fromXml(imagestyleNodes.item(i), xpath);
      ServiceRegistration service = ctx.registerService(ImageStyle.class.getName(), style, null);
      styles.add(service);
      logger.debug("Registering image style '{}'", style);
    }

  }

  /**
   * Removes the registered image styles from the service registry.
   * 
   * @param ctx
   *          the bundle context
   */
  private void unregisterImageStyles(BundleContext ctx) {
    logger.info("Unregistering weblounge ui imagestyles");
    for (ServiceRegistration service : styles) {
      ImageStyle style = (ImageStyle) ctx.getService(service.getReference());
      logger.debug("Unregistering image style '{}'", style);
      service.unregister();
    }
  }

}
