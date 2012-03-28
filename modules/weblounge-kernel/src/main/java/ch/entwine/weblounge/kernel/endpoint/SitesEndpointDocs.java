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

    // GET /{site}/modules
    Endpoint modulesEndpoint = new Endpoint("/{site}/modules", Method.GET, "getmodules");
    modulesEndpoint.setDescription("Returns the modules of the site with the given id");
    modulesEndpoint.addFormat(Format.xml());
    modulesEndpoint.addStatus(ok("the site was found and its modules are returned as part of the response"));
    modulesEndpoint.addStatus(notFound("the site was not found"));
    modulesEndpoint.addPathParameter(new Parameter("site", Parameter.Type.String, "The site identifier"));
    modulesEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, modulesEndpoint);
    
    // GET /{site}/modules/{module}
    Endpoint moduleEndpoint = new Endpoint("/{site}/modules/{module}", Method.GET, "getmodule");
    moduleEndpoint.setDescription("Returns the module with the given id");
    moduleEndpoint.addFormat(Format.xml());
    moduleEndpoint.addStatus(ok("the module was found and is returned as part of the response"));
    moduleEndpoint.addStatus(notFound("either the site or the module was not found"));
    moduleEndpoint.addPathParameter(new Parameter("site", Parameter.Type.String, "The site identifier"));
    moduleEndpoint.addPathParameter(new Parameter("module", Parameter.Type.String, "The module identifier"));
    moduleEndpoint.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, moduleEndpoint);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
