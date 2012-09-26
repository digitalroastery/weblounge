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

package ch.entwine.weblounge.kernel.fop;

import static ch.entwine.weblounge.common.impl.util.doc.Status.badRequest;
import static ch.entwine.weblounge.common.impl.util.doc.Status.notFound;
import static ch.entwine.weblounge.common.impl.util.doc.Status.ok;

import ch.entwine.weblounge.common.impl.util.doc.Endpoint;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.entwine.weblounge.common.impl.util.doc.Format;
import ch.entwine.weblounge.common.impl.util.doc.Parameter;
import ch.entwine.weblounge.common.impl.util.doc.TestForm;

/**
 * Fop endpoint documentation generator.
 */
public final class FopEndpointDocs {

  /**
   * This class does not need to be instantiated.
   */
  private FopEndpointDocs() {
    // Nothing to do
  }

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "fop");
    docs.setTitle("Weblounge Formatting Object Processor Endpoint");

    // POST /pdf
    Endpoint createPDFEndpoint = new Endpoint("/pdf", Method.POST, "createpdf");
    createPDFEndpoint.setDescription("Creates a PDF document");
    createPDFEndpoint.addFormat(new Format("pdf", "Portable Document Format", "http://en.wikipedia.org/wiki/Portable_Document_Format"));
    createPDFEndpoint.addStatus(ok("the document was created"));
    createPDFEndpoint.addStatus(badRequest("the xml document url is not a regular url"));
    createPDFEndpoint.addStatus(badRequest("the xsl document url is not a regular url"));
    createPDFEndpoint.addStatus(notFound("the xml document could not be accessed at the given address"));
    createPDFEndpoint.addStatus(notFound("the xsl document could not be accessed at the given address"));
    createPDFEndpoint.addRequiredParameter(new Parameter("xml", Parameter.Type.String, "URL to the xml document"));
    createPDFEndpoint.addRequiredParameter(new Parameter("xsl", Parameter.Type.String, "URL to the xsl document"));
    createPDFEndpoint.addOptionalParameter(new Parameter("parameters", Parameter.Type.String, "Parameters, formatted as 'a=b;c=d'"));
    createPDFEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, createPDFEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
