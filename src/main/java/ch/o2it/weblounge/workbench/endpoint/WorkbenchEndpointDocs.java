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

  private static final Format XML = new Format("xml", null, null);

  private static final Format JSON = new Format("json", null, "http://www.json.org/");

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

    // GET /page/{page}/head
    Endpoint getPageHead = new Endpoint("/page/{page}/head", Method.GET, "getpagehead");
    getPageHead.setDescription("Returns the head for the given page");
    getPageHead.addFormat(JSON);
    getPageHead.addStatus(ok("the page head was found and it's returned"));
    getPageHead.addStatus(notFound("the page head was not found"));
    getPageHead.addStatus(serviceUnavailable("the site is temporarily offline"));
    getPageHead.addPathParameter(new Parameter("page", Parameter.Type.String, "The page uri"));
    getPageHead.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getPageHead);

    // GET /edit/{page}/{composer}/{pageletindex}
    Endpoint getPageletEditor = new Endpoint("/edit/{page}/{composer}/{pageletindex}", Method.GET, "getpageleteditor");
    getPageletEditor.setDescription("Returns the editor for the given pagelet");
    getPageletEditor.addFormat(XML);
    getPageletEditor.addFormat(JSON);
    getPageletEditor.addStatus(ok("the pagelet was found and it's editing information is returned"));
    getPageletEditor.addStatus(notFound("the page, the composer or the pagelet were not found"));
    getPageletEditor.addStatus(serviceUnavailable("the site is temporarily offline"));
    getPageletEditor.addPathParameter(new Parameter("page", Parameter.Type.String, "The page uri"));
    getPageletEditor.addPathParameter(new Parameter("composer", Parameter.Type.String, "The composer identifier"));
    getPageletEditor.addPathParameter(new Parameter("pageletindex", Parameter.Type.String, "The pagelet's index within the composer (0 based)"));
    getPageletEditor.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getPageletEditor);
    
    // GET /edit/{page}/{composer}/{pageletindex}
    Endpoint getLanguages = new Endpoint("/languages}", Method.GET, "getlanguages");
    getLanguages.setDescription("Returns the languages of the site");
    getLanguages.addFormat(JSON);
    getLanguages.addStatus(ok("the site was found and it's languages are returned"));
    //getLanguages.addStatus(notFound("the page, the composer or the pagelet were not found"));
    //getLanguages.addStatus(serviceUnavailable("the site is temporarily offline"));
    getLanguages.setTestForm(new TestForm());
    docs.addEndpoint(Endpoint.Type.READ, getLanguages);

    // GET /suggest/subjects/{hint}
    Endpoint suggestSubjects = new Endpoint("/suggest/subjects/{hint}", Method.GET, "suggestsubjects");
    suggestSubjects.setDescription("Returns suggestions for subjects based on the given hint");
    suggestSubjects.addFormat(XML);
    suggestSubjects.addFormat(JSON);
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
