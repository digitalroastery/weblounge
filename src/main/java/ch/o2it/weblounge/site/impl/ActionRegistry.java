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

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.site.Action;

import com.sun.servicetag.Registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This registry keeps track of action bundles defined for a given site and
 * module and provides them through its <code>get</code> method.
 */
public class ActionRegistry extends Registry {

  /** Registry identifier */
  public static final String ID = "actions";

  /** The url mappings */
  private Map mappings_;

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ActionRegistry.class.getName());

  /**
   * Creates a new <code>ActionRegistry</code>.
   */
  public ActionRegistry() {
    super(ID);
    mappings_ = new HashMap();
  }

  /**
   * Returns the action handler bundle that has been registered for the given
   * url or <code>null</code> if no such registration exists.
   * 
   * @param url
   *          the url
   * @param the
   *          rendering method
   * @return the bundle
   */
  public Action getByUrl(String url, String method) {
    Action handler = getByUrl(url, true, method);
    if (handler == null) {
      return getByUrl(url, false, method);
    }
    return handler;
  }

  /**
   * Returns the action handler bundle that has been registered for the given
   * url or <code>null</code> if no such registration exists.
   * 
   * @param url
   *          the url
   * @param exactMatch
   *          <code>true</code> to only return exact matches
   * @param the
   *          rendering method
   * @return the bundle
   */
  public Action getByUrl(String url, boolean exactMatch, String method) {
    ActionHandlerBundle bundle = getByUrl(url, exactMatch);
    return (bundle != null) ? bundle.getAction(method) : null;
  }

  /**
   * Returns the action handler that has been mapped to the specified url.
   * 
   * @param url
   *          the url
   * @param exactMatch
   *          <code>true</code> to give an exact match
   * @return the handler bundle
   */
  private ActionHandlerBundle getByUrl(String url, boolean exactMatch) {
    assert url != null;
    Iterator urls = mappings_.keySet().iterator();
    while (urls.hasNext()) {
      String mp = (String) urls.next();
      if (url.equals(mp) || url.equals(mp + "/") || url.startsWith(mp + "?") || (!exactMatch && url.startsWith(mp + "/"))) {
        ActionHandlerBundle bundle = (ActionHandlerBundle) mappings_.get(mp);
        String ext = bundle.getExtension();
        if (ext == null) {
          return bundle;
        } else if (ext != null && ext.equals("/*") && UrlSupport.isPrefix(mp, url)) {
          return bundle;
        } else if (ext != null && ext.equals("/**") && UrlSupport.isExtendedPrefix(mp, url)) {
          return bundle;
        }
      }
    }
    return null;
  }

  /**
   * Returns the configuration of the renderer bundle.
   * 
   * @param id
   *          the renderer identifier
   * @return the bundle configuration
   */
  public ActionBundleConfiguration getActionHandlerBundleConfigurationl(
      String id) {
    ActionHandlerBundle bundle = (ActionHandlerBundle) get(id);
    return (bundle != null) ? bundle.getConfiguration() : null;
  }

  /**
   * Returns the action handler to the registry.
   * 
   * @param handler
   *          the handler
   */
  public void returnHandler(Action handler) {
    String mp = handler.getConfiguration().getMountpoint();
    ActionHandlerBundle bundle = getByUrl(mp, true);
    if (bundle != null) {
      bundle.returnAction(handler);
    }
  }

  /**
   * Puts the action bundle with the given identifier into the registry.
   * 
   * @param key
   *          the action identifier
   * @param o
   *          the action bundle
   * @see ch.o2it.weblounge.core.util.registry.Registry#put(java.lang.Object,
   *      java.lang.Object)
   */
  public Object put(Object key, Object o) {
    assert o instanceof ActionHandlerBundle;
    ActionHandlerBundle bundle = (ActionHandlerBundle) o;
    if (bundle.getConfiguration().getMountpoint() == null) {
      log_.warn("Unable to register action '" + bundle + "' since mountpoint is null!");
      return null;
    }
    mappings_.put(((ActionHandlerBundle) o).getConfiguration().getMountpoint(), o);
    return super.put(key, o);
  }

  /**
   * Removes the action bundle with the given identifier from the registry.
   * 
   * @param key
   *          the action identifier
   * @see ch.o2it.weblounge.core.util.registry.Registry#remove(java.lang.Object)
   */
  public Object remove(Object key) {
    ActionHandlerBundle bundle = (ActionHandlerBundle) super.remove(key);
    if (bundle != null) {
      mappings_.remove(bundle.getConfiguration().getMountpoint());
      return bundle;
    }
    return null;
  }

}