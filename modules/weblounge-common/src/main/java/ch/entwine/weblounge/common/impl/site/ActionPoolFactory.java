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

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.page.HTMLHeadElement;
import ch.entwine.weblounge.common.impl.content.page.PageURIImpl;
import ch.entwine.weblounge.common.site.Action;
import ch.entwine.weblounge.common.site.Environment;
import ch.entwine.weblounge.common.site.HTMLAction;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The action pool factory will create action objects according to the action
 * configuration that is passed in at construction time.
 */
public final class ActionPoolFactory extends BasePoolableObjectFactory<Action> {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(ActionPoolFactory.class);

  /** The action blueprint */
  protected Action blueprint = null;
  
  /**
   * Creates a new action pool factory that will create action objects for the
   * given site according to the configuration.
   * 
   * @param sample
   *          the action configuration
   */
  public ActionPoolFactory(Action sample) {
    this.blueprint = sample;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BasePoolableObjectFactory#makeObject()
   */
  @Override
  public Action makeObject() throws Exception {
    logger.debug("Creating new action '{}'", blueprint.getIdentifier());
    
    Action action = blueprint.getClass().newInstance();
    
    // Module
    action.setModule(blueprint.getModule());
    
    // Site
    action.setSite(blueprint.getSite());

    // Identifier
    action.setIdentifier(blueprint.getIdentifier());

    // Path
    action.setPath(blueprint.getPath());

    // Includes
    for (HTMLHeadElement header : blueprint.getHTMLHeaders()) {
      action.addHTMLHeader(header);
    }

    // Options
    for (Map.Entry<String, Map<Environment, List<String>>> option : blueprint.getOptions().entrySet()) {
      for (Environment environment : option.getValue().keySet()) {
        List<String> values = option.getValue().get(environment);
        for (String value : values) {
          action.setOption(option.getKey(), value, environment);
        }
      }
    }
    
    // Recheck time
    action.setClientRevalidationTime(blueprint.getClientRevalidationTime());

    // Valid time
    action.setCacheExpirationTime(blueprint.getCacheExpirationTime());

    // Are we looking at an html action?
    if (blueprint instanceof HTMLAction) {
      HTMLAction htmlBlueprint = (HTMLAction)blueprint;
      HTMLAction htmlAction = (HTMLAction)action;

      // Page URI
      if (htmlBlueprint.getPageURI() != null) {
        ResourceURI uri = htmlBlueprint.getPageURI();
        htmlAction.setPageURI(new PageURIImpl(uri.getSite(), uri.getPath()));
      }

      // Default page template
      htmlAction.setDefaultTemplate(htmlBlueprint.getDefaultTemplate());
    }
    
    // Name
    action.setName(blueprint.getName());

    return action;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BasePoolableObjectFactory#destroyObject(java.lang.Object)
   */
  @Override
  public void destroyObject(Action action) throws Exception {
    logger.debug("Destroying action '{}'", action.getIdentifier());
    try {
      action.passivate();
    } catch (Throwable t) {
      logger.error("Error destroying action: {}", t.getMessage(), t);
    }
    super.destroyObject(action);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BasePoolableObjectFactory#activateObject(java.lang.Object)
   */
  @Override
  public void activateObject(Action action) throws Exception {
    logger.debug("Activating action '{}'", action.getIdentifier());
    try {
      action.activate();
    } catch (Throwable t) {
      logger.error("Error destroying action: {}", t.getMessage(), t);
    }
    super.activateObject(action);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BasePoolableObjectFactory#passivateObject(java.lang.Object)
   */
  @Override
  public void passivateObject(Action action) throws Exception {
    logger.debug("Passivating action '{}'", action.getIdentifier());
    try {
      action.passivate();
    } catch (Throwable t) {
      logger.error("Error destroying action: {}", t.getMessage(), t);
    }
    super.passivateObject(action);
  }
  
  /**
   * {@inheritDoc}
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "action pool factory [" + blueprint + "]";
  }

}
