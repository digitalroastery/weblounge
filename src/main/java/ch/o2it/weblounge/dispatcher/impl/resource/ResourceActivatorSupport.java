/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.dispatcher.impl.resource;

import ch.o2it.weblounge.dispatcher.impl.http.HttpActivator;
import ch.o2it.weblounge.dispatcher.impl.http.WebXml;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public abstract class ResourceActivatorSupport extends HttpActivator implements BundleActivator {

  public void start(BundleContext context) throws Exception {
    start(context, webXml);
  }

  public void start(BundleContext context, WebXml webXml) throws Exception {

    // set default if not set by concrete class

    if (!webXml.containsContextParam(HttpActivator.BUNDLE_URI_NAMESPACE)) {
      webXml.addContextParam(HttpActivator.BUNDLE_URI_NAMESPACE, "/" + context.getBundle().getSymbolicName());
    }

    super.start(context, webXml);
  }
}
