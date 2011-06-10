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

package ch.entwine.weblounge.test.site;

import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.ActionException;
import ch.entwine.weblounge.common.site.XMLAction;

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
   * @see ch.entwine.weblounge.test.site.GreeterActionSupport#startXMLResponse(ch.entwine.weblounge.common.request.WebloungeRequest, ch.entwine.weblounge.common.request.WebloungeResponse)
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
