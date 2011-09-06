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

package ch.entwine.weblounge.workbench.endpoint;

import static ch.entwine.weblounge.common.impl.util.doc.Status.notFound;
import static ch.entwine.weblounge.common.impl.util.doc.Status.ok;
import static ch.entwine.weblounge.common.impl.util.doc.Status.serviceUnavailable;

import ch.entwine.weblounge.common.impl.util.doc.Endpoint;
import ch.entwine.weblounge.common.impl.util.doc.Endpoint.Method;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentation;
import ch.entwine.weblounge.common.impl.util.doc.EndpointDocumentationGenerator;
import ch.entwine.weblounge.common.impl.util.doc.Format;
import ch.entwine.weblounge.common.impl.util.doc.Parameter;
import ch.entwine.weblounge.common.impl.util.doc.TestForm;

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
    Endpoint getPageletEditor = new Endpoint("/edit/{page}/{composer}/{pageletindex}", Method.GET, "getpageleteditor");
    getPageletEditor.setDescription("Returns the editor for the given pagelet");
    getPageletEditor.addFormat(new Format("xml", null, null));
    getPageletEditor.addStatus(ok("the pagelet was found and it's editing information is returned"));
    getPageletEditor.addStatus(notFound("the page, the composer or the pagelet were not found"));
    getPageletEditor.addStatus(serviceUnavailable("the site is temporarily offline"));
    getPageletEditor.addPathParameter(new Parameter("page", Parameter.Type.String, "The page uri"));
    getPageletEditor.addPathParameter(new Parameter("composer", Parameter.Type.String, "The composer identifier"));
    getPageletEditor.addPathParameter(new Parameter("pageletindex", Parameter.Type.String, "The pagelet's index within the composer (0 based)"));
    getPageletEditor.addOptionalParameter(new Parameter("language", Parameter.Type.String, "The language id"));
    getPageletEditor.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getPageletEditor);
    
    Endpoint getRenderer = new Endpoint("/renderer/{page}/{composer}/{pageletindex}", Method.GET, "getrenderer");
    getRenderer.setDescription("Returns the renderer for the given pagelet");
    getRenderer.addFormat(new Format("html", null, null));
    getRenderer.addStatus(ok("the pagelet was found and it's renderer is returned"));
    getRenderer.addStatus(notFound("the page, the composer, the pagelet or the renderer were not found"));
    getRenderer.addStatus(serviceUnavailable("the site is temporarily offline"));
    getRenderer.addPathParameter(new Parameter("page", Parameter.Type.String, "The page uri"));
    getRenderer.addPathParameter(new Parameter("composer", Parameter.Type.String, "The composer identifier"));
    getRenderer.addPathParameter(new Parameter("pageletindex", Parameter.Type.String, "The pagelet's index within the composer (0 based)"));
    getRenderer.addOptionalParameter(new Parameter("language", Parameter.Type.String, "The language id"));
    getRenderer.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getRenderer);

    // GET /suggest/subjects/{hint}
    Endpoint suggestSubjects = new Endpoint("/suggest/subjects/{hint}", Method.GET, "suggestsubjects");
    suggestSubjects.setDescription("Returns suggestions for subjects based on the given hint");
    suggestSubjects.addFormat(new Format("xml", null, null));
    suggestSubjects.addStatus(ok("suggestions based on the hint are returned"));
    suggestSubjects.addStatus(serviceUnavailable("the site is temporarily offline"));
    suggestSubjects.addPathParameter(new Parameter("hint", Parameter.Type.String, "The hint on which suggestions are based"));
    suggestSubjects.addOptionalParameter(new Parameter("limit", Parameter.Type.String, "The maximum number of suggestions"));
    suggestSubjects.addOptionalParameter(new Parameter("highlight", Parameter.Type.String, "The tag name used to highlight matches"));
    suggestSubjects.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, suggestSubjects);

    return EndpointDocumentationGenerator.generate(docs);
  }

}
