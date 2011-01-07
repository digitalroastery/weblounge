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

package ch.o2it.weblounge.workbench.endpoint;

import static ch.o2it.weblounge.common.impl.util.doc.Status.notFound;
import static ch.o2it.weblounge.common.impl.util.doc.Status.ok;
import static ch.o2it.weblounge.common.impl.util.doc.Status.serviceUnavailable;

import ch.o2it.weblounge.common.impl.util.doc.Endpoint;
import ch.o2it.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.o2it.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.o2it.weblounge.common.impl.util.doc.Format;
import ch.o2it.weblounge.common.impl.util.doc.Parameter;
import ch.o2it.weblounge.common.impl.util.doc.TestForm;

/**
 * Workbench endpoint documentation generator.
 */
public final class WorkbenchEndpointDocs {

  /**
   * Creates the documentation.
   * 
   * @param endpointUrl
   *          the endpoint address
   * @return the endpoint documentation
   */
  public static String createDocumentation(String endpointUrl) {
    EndpointDocumentation docs = new EndpointDocumentation(endpointUrl, "workbench");
    docs.setTitle("Weblounge Workbench");

    // GET /edit/{page}/{composer}/{pageletindex}
    Endpoint getImageMetadata = new Endpoint("/edit/{page}/{composer}/{pageletindex}", Method.GET, "getpageleteditor");
    getImageMetadata.setDescription("Returns the editor for the given pagelet");
    getImageMetadata.addFormat(new Format("xml", null, null));
    getImageMetadata.addStatus(ok("the pagelet was found and it's editing information is returned"));
    getImageMetadata.addStatus(notFound("the page, the composer or the pagelet were not found"));
    getImageMetadata.addStatus(serviceUnavailable("the site is temporarily offline"));
    getImageMetadata.addPathParameter(new Parameter("page", Parameter.Type.String, "The page uri"));
    getImageMetadata.addPathParameter(new Parameter("composer", Parameter.Type.String, "The composer identifier"));
    getImageMetadata.addPathParameter(new Parameter("pageletindex", Parameter.Type.String, "The pagelet's index within the composer (0 based)"));
    getImageMetadata.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getImageMetadata);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
