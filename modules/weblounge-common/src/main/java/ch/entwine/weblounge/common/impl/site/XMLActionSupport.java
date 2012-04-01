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

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.request.RequestFlavor;
import ch.entwine.weblounge.common.request.WebloungeRequest;
import ch.entwine.weblounge.common.request.WebloungeResponse;
import ch.entwine.weblounge.common.site.ActionException;
import ch.entwine.weblounge.common.site.XMLAction;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * This class is the default implementation for an <code>XMLAction</code>. The
 * implementations of the <code>startXML()</code> is empty, so nothing is
 * returned as the <code>XML</code> body of the response. Therefore, subclasses
 * need to overwrite this method in order to return meaningful content.
 * <p>
 * <b>Note:</b> Be aware of the fact that actions are pooled, so make sure to
 * implement the <code>activate()</code> and <code>passivate()</code> method
 * accordingly and include the respective super implementations.
 */
public class XMLActionSupport extends ActionSupport implements XMLAction {

  /** The transformer factory */
  protected final TransformerFactory transformerFactory = TransformerFactory.newInstance();

  /**
   * Creates a new action implementation that directly supports the generation
   * of <code>XML</code> data.
   */
  public XMLActionSupport() {
    addFlavor(RequestFlavor.XML);
  }

  /**
   * This implementation always returns
   * {@link ch.entwine.weblounge.common.site.Action#EVAL_REQUEST} and simply sets
   * the content type on the response to <code>application/xml;charset=utf-8</code>.
   * <p>
   * This means that subclasses should either overwrite this method to specify a
   * different encoding or make sure that everything that is written to the
   * response is encoded to <code>utf-8</code>.
   * 
   * @param request
   *          the servlet request
   * @param response
   *          the servlet response
   * @return {@link ch.entwine.weblounge.common.site.Action#EVAL_REQUEST}
   */
  @Override
  public int startResponse(WebloungeRequest request, WebloungeResponse response)
      throws ActionException {
    String characterEncoding = response.getCharacterEncoding();
    if (StringUtils.isNotBlank(characterEncoding))
      response.setContentType("application/xml;charset=" + characterEncoding.toLowerCase());
    else
      response.setContentType("application/xml");
    return EVAL_REQUEST;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.site.XMLAction#startXML(ch.entwine.weblounge.common.request.WebloungeRequest,
   *      ch.entwine.weblounge.common.request.WebloungeResponse)
   */
  public void startXML(WebloungeRequest request, WebloungeResponse response)
      throws IOException, ActionException {
    return;
  }

  /**
   * Sends the document to the client, using <code>transformer</code> to
   * transform the content and write it to the output stream.
   * 
   * @param doc
   *          the xml document
   * @param transformer
   *          the xml transformer
   * @throws ActionException
   *           if transforming and sending the document fails
   * @see #returnXML(org.w3c.dom.Document)
   */
  protected void returnXML(Element doc, Transformer transformer)
      throws ActionException {
    try {
      transformer.transform(new DOMSource(doc), new StreamResult(response.getWriter()));
    } catch (TransformerException e) {
      throw new ActionException("Unable to create xml response", e);
    } catch (IOException e) {
      throw new ActionException("Unable to send xml response", e);
    }
  }

  /**
   * Sends the document to the client. This method creates and uses a
   * {@link Transformer} to transform the content and write it to the output
   * stream.
   * <p>
   * If your implementation relies on specific transformer capabilities or
   * configurations, you may want to use the protected
   * <code>TransformerFactory</code> to create the transformer and then call
   * {@link #returnXML(org.w3c.dom.Document, Transformer)} instead of this
   * method.
   * 
   * @param doc
   *          the xml document
   * @param transformer
   *          the xml transformer
   * @throws ActionException
   *           if transforming and sending the document fails
   * @see #returnXML(org.w3c.dom.Document, Transformer)
   */
  protected void returnXML(Element doc) throws ActionException {
    try {
      Transformer transformer = transformerFactory.newTransformer();
      returnXML(doc, transformer);
    } catch (Throwable t) {
      throw new ActionException("Unable to create an xml transformer", t);
    }
  }

}
