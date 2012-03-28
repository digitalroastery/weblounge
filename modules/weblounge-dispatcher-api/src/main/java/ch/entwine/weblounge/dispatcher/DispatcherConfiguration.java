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

package ch.entwine.weblounge.dispatcher;

/**
 * This interface defines configuration values for the dispatcher.
 */
public interface DispatcherConfiguration {

  /** Property name for the web application context root (mountpoint) */
  String WEBAPP_CONTEXT_ROOT = "weblounge.http.CONTEXT_ROOT";
  
  /** Property name for the bundles (sites) root path element */
  String BUNDLE_CONTEXT_ROOT_URI = "weblounge.http.BUNDLE_CONTEXT_ROOT_URI";
  
  /**
   * Property name for the bundle context root path (including the webapp
   * context root)
   */
  String BUNDLE_CONTEXT_ROOT = "weblounge.http.BUNDLE_CONTEXT_ROOT";
  
  /**
   * Property name for the bundle root path (including the webapp context root
   * and the bundle context root)
   */
  String BUNDLE_ROOT = "weblounge.http.BUNDLE_ROOT";
  
  /** Property name for the bundle name */
  String BUNDLE_NAME = "weblounge.http.BUNDLE_NAME";
  
  /** Property name for the bundle (site) path element */
  String BUNDLE_URI = "weblounge.http.BUNDLE_URI";
  
  /** Property name for the entry path into the bundle resources */
  String BUNDLE_ENTRY = "weblounge.http.BUNDLE_ENTRY";
  
  /** Semicolon delimited list of welcome files */
  String WELCOME_FILES = "weblounge.http.WELCOME_FILES";

}
