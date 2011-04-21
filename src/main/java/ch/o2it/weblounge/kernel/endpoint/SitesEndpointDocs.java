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

package ch.o2it.weblounge.kernel.endpoint;

import static ch.o2it.weblounge.common.impl.util.doc.Status.badRequest;
import static ch.o2it.weblounge.common.impl.util.doc.Status.notFound;
import static ch.o2it.weblounge.common.impl.util.doc.Status.ok;

import ch.o2it.weblounge.common.impl.util.doc.Endpoint;
import ch.o2it.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.o2it.weblounge.common.impl.util.doc.Format;
import ch.o2it.weblounge.common.impl.util.doc.Parameter;
import ch.o2it.weblounge.common.impl.util.doc.TestForm;

/**
 * Site endpoint documentation generator.
 */
public final class SitesEndpointDocs {

  /**
   * This class does not need to be instantiated.
   */
  private SitesEndpointDocs() {
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
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "sites");
    docs.setTitle("Weblounge Sites");

    // GET /
    Endpoint sitesEndpoint = new Endpoint("/", Method.GET, "getsites");
    sitesEndpoint.setDescription("Returns all sites");
    sitesEndpoint.addFormat(Format.xml());
    sitesEndpoint.addStatus(ok("the sites are returned as part of the response"));
    sitesEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, sitesEndpoint);

    // GET /{site}
    Endpoint siteEndpoint = new Endpoint("/{site}", Method.GET, "getsite");
    siteEndpoint.setDescription("Returns the site with the given id");
    siteEndpoint.addFormat(Format.xml());
    siteEndpoint.addStatus(ok("the site was found and is returned as part of the response"));
    siteEndpoint.addStatus(notFound("the site was not found"));
    siteEndpoint.addPathParameter(new Parameter("site", Parameter.Type.String, "The site identifier"));
    siteEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, siteEndpoint);

    // PUT /{site}
    Endpoint updateSiteEndpoint = new Endpoint("/{site}", Method.PUT, "updatesite");
    updateSiteEndpoint.setDescription("Updates the specified site");
    updateSiteEndpoint.addFormat(Format.xml());
    updateSiteEndpoint.addStatus(ok("the site was updated"));
    updateSiteEndpoint.addStatus(badRequest("the site identifier was not specified"));
    updateSiteEndpoint.addStatus(badRequest("the site status is malformed"));
    updateSiteEndpoint.addStatus(notFound("the site to update was not found"));
    updateSiteEndpoint.addPathParameter(new Parameter("site", Parameter.Type.String, "The site identifier"));
    updateSiteEndpoint.addOptionalParameter(new Parameter("status", Parameter.Type.Text, "The site status", "on"));
    updateSiteEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.WRITE, updateSiteEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
