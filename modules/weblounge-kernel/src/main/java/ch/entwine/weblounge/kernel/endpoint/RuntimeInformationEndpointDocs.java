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

package ch.entwine.weblounge.kernel.endpoint;

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
 * Runtime information endpoint documentation generator.
 */
public final class RuntimeInformationEndpointDocs {

  /**
   * This class does not need to be instantiated.
   */
  private RuntimeInformationEndpointDocs() {
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
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "runtimeinfo");
    docs.setTitle("Weblounge Runtime Information");

    // GET /
    Endpoint sitesEndpoint = new Endpoint("/", Method.GET, "getinfo");
    sitesEndpoint.setDescription("Returns all runtime information");
    sitesEndpoint.addFormat(Format.xml());
    sitesEndpoint.addStatus(ok("the runtime information is returned as part of the response"));
    sitesEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, sitesEndpoint);

    // GET /{component}
    Endpoint siteEndpoint = new Endpoint("/{component}", Method.GET, "getcomponent");
    siteEndpoint.setDescription("Returns the specified part of the runtime information");
    siteEndpoint.addFormat(Format.xml());
    siteEndpoint.addStatus(ok("the component was found and the corresponding runtime information is returned as part of the response"));
    siteEndpoint.addStatus(notFound("the component was not found"));
    siteEndpoint.addPathParameter(new Parameter("component", Parameter.Type.String, "The runtime information component"));
    siteEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, siteEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
