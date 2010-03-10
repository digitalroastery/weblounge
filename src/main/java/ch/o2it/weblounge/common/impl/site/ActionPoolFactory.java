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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.impl.page.PageURIImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.PageInclude;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.ActionConfiguration;
import ch.o2it.weblounge.common.site.Site;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The action pool factory will create action objects according to the action
 * configuration that is passed in at construction time.
 */
public class ActionPoolFactory extends BasePoolableObjectFactory {

  /** Logging facility */
  private final static Logger log_ = LoggerFactory.getLogger(ActionPoolFactory.class);

  /** The action configuration */
  protected ActionConfiguration configuration = null;
  
  /** The site */
  protected Site site = null;

  /**
   * Creates a new action pool factory that will create action objects for the
   * given site according to the configuration.
   * 
   * @param configuration
   *          the action configuration
   * @param site
   *          the site
   */
  public ActionPoolFactory(ActionConfiguration configuration, Site site) {
    this.configuration = configuration;
    this.site = site;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BasePoolableObjectFactory#makeObject()
   */
  @Override
  public Object makeObject() throws Exception {
    log_.debug("Creating new action '{}'", configuration.getIdentifier());
    Class<? extends Action> actionClass = configuration.getActionClass();
    Action action = actionClass.newInstance();
    action.setIdentifier(configuration.getIdentifier());
    for (PageInclude include : configuration.getIncludes())
      action.addInclude(include);
    action.setPath(configuration.getMountpoint());
    for (Map.Entry<String, List<String>> option : configuration.getOptions().entrySet())
      for (String value : option.getValue())
        action.setOption(option.getKey(), value);
    action.setRecheckTime(configuration.getRecheckTime());
    if (configuration.getPageURI() != null)
      action.setPageURI(new PageURIImpl(site, configuration.getPageURI()));
    action.setTemplate(site.getTemplate(configuration.getTemplate()));
    action.setValidTime(configuration.getValidTime());
    for (Language l : configuration.getName().languages()) {
      action.setName(configuration.getName().toString(l), l);
    }
    return action;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BasePoolableObjectFactory#destroyObject(java.lang.Object)
   */
  @Override
  public void destroyObject(Object obj) throws Exception {
    Action action = (Action) obj;
    log_.debug("Destroying action '{}'", action.getIdentifier());
    try {
      action.cleanup();
    } catch (Throwable t) {
      log_.error("Error destroying action: {}", t.getMessage(), t);
    }
    super.destroyObject(obj);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BasePoolableObjectFactory#activateObject(java.lang.Object)
   */
  @Override
  public void activateObject(Object obj) throws Exception {
    Action action = (Action) obj;
    log_.debug("Activating action '{}'", action.getIdentifier());
    super.activateObject(obj);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.commons.pool.BasePoolableObjectFactory#passivateObject(java.lang.Object)
   */
  @Override
  public void passivateObject(Object obj) throws Exception {
    Action action = (Action) obj;
    log_.debug("Passivating action '{}'", action.getIdentifier());
    try {
      action.cleanup();
    } catch (Throwable t) {
      log_.error("Error destroying action: {}", t.getMessage(), t);
    }
    super.passivateObject(obj);
  }

}
