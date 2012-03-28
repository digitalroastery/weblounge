/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.ui.runtime;

import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.common.security.User;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Exposes this bundle's runtime information, which mainly consists of the http
 * context path.
 */
public class UIRuntimeInformation implements RuntimeInformationProvider {
  
  /** Name of the context path bundle header */
  public static final String CTX_PATH_HEADER = "Http-Context";

  /** The bundle context */
  protected String contextPath = null;

  /**
   * Reads the ui's context path from the bundle headers.
   * 
   * @param ctx
   *          the context path
   */
  void activate(ComponentContext ctx) {
    BundleContext bundleContext = ctx.getBundleContext();
    contextPath = (String)bundleContext.getBundle().getHeaders().get(CTX_PATH_HEADER);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider#getComponentId()
   */
  public String getComponentId() {
    return "ui";
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.kernel.runtime.RuntimeInformationProvider#getRuntimeInformation(ch.entwine.weblounge.common.site.Site,
   *      ch.entwine.weblounge.common.security.User,
   *      ch.entwine.weblounge.common.language.Language, Environment)
   */
  public String getRuntimeInformation(Site site, User user, Language language, Environment environment) {
    if (contextPath == null)
      return null;
    StringBuilder buf = new StringBuilder();
    buf.append("<path>").append(contextPath).append("</path>");
    return buf.toString();
  }

}
