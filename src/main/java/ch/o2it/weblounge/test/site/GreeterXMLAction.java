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

package ch.o2it.weblounge.test.site;

import ch.o2it.weblounge.common.request.RequestFlavor;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.site.ActionException;
import ch.o2it.weblounge.common.site.XMLAction;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Simple test action that is able to render a greeting on the site template.
 */
public class GreeterXMLAction extends GreeterHTMLAction implements XMLAction {

  /**
   * Creates an extension of the <code>GreeterAction</code> that can handle
   * <code>XML</code> requests.
   */
  public GreeterXMLAction() {
    clearFlavors();
    addFlavor(RequestFlavor.XML);
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.test.site.GreeterActionSupport#startXMLResponse(ch.o2it.weblounge.common.request.WebloungeRequest, ch.o2it.weblounge.common.request.WebloungeResponse)
   */
  public void startXML(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException {
    try {
      DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = docBuilder.newDocument();
      doc.setXmlStandalone(true);
      Element root = doc.createElement("greetings");
      doc.appendChild(root);
      Element greetingNode = doc.createElement("greeting");
      greetingNode.setAttribute("language", language);
      greetingNode.appendChild(doc.createTextNode(greeting));
      root.appendChild(greetingNode);
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      transformer.transform(new DOMSource(doc), new StreamResult(response.getWriter()));
    } catch (Throwable t) {
      throw new ActionException("Unable to create and send xml response", t);
    }
  }

}
