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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The <code>ModuleManager</code> is responsible for loading and properly
 * configuring the available modules, reloading and stopping them.
 * <p>
 * Since it is possible that additional modules are added or removed while the
 * system is up and running, the module manager keeps watching the
 * <code>modules</code> directory for changes.
 */
public final class ModuleManager {

  /** Name of the modules directory */
  public final static String MODULE_DIR = "module";

  /** Watches the loaded modules for changes */
  private ModuleLoader loader_ = null;

  /** Container for module configurations */
  private static Map sharedConfigurations_ = new HashMap();

  /** Container for module configurations */
  private Map configurations_ = new HashMap();

  /** The associated site */
  private Site site_;

  /** Logging facility */
  final static Logger log_ = LoggerFactory.getLogger(ModuleManager.class.getName());

  /**
   * Since this module only provides static methods, the constructor is private.
   */
  public ModuleManager(Site site) {
    site_ = site;
  }

  /**
   * Initializes the <code>ModuleManager</code>.
   */
  public void init() {
    waitForSharedModules();
    addSharedModules();
    log_.info("Loading site modules");
    String modulesPath = site_.getPhysicalPath(MODULE_DIR);
    File dir = new File(modulesPath);
    File moduleFolder = new File(site_.getPhysicalPath("/") + ModuleManager.MODULE_DIR);
    if (moduleFolder.exists() && moduleFolder.isDirectory() && moduleFolder.canRead()) {
      log_.info("Looking for site modules in '" + dir + "'");
      loader_ = new ModuleLoader(dir, site_, this);
      loader_.init();
    }
  }

  /**
   * This method is called to shut down the module manager.
   * 
   * @param site
   *          the site that is shut down
   */
  public void destroy() {
    // Nothing to do
  }

  /**
   * Configures the module that is associated with the given module
   * configuration.
   * 
   * @param config
   *          the module configuration
   */
  Module configureModule(ModuleConfigurationImpl config)
      throws ConfigurationException {
    ModuleRegistry modules = site_.getModules();
    try {
      site_.getLogger().debug("Configuring module at " + config.getFile());
      if (!config.shared) {
        Module existing = (Module) modules.get(config.identifier);

        // If there is a module installed under the same identifier,
        // replace it
        if (existing != null && existing.isShared()) {
          String baseid = config.getBaseModuleIdentifier();
          if (baseid != null) {
            Module base = (Module) modules.get(baseid);
            if (base == null) {
              String msg = "Module '" + config.identifier + "' tries to shadow non existing base module '" + baseid;
              throw new ConfigurationException(msg);
            } else if (base != null && !base.isShared()) {
              String msg = "Module '" + config.identifier + "' tries to shadow site module '" + baseid + "' instead of a shared module";
              throw new ConfigurationException(msg);
            }
            modules.remove(config.identifier);
            log_.info("Shared module '" + config.identifier + "' is shadowed by site specific module");
          } else {
            modules.remove(config.identifier);
            log_.info("Site module '" + config.identifier + "' hides shared module");
          }
        } else if (existing != null) {
          String msg = "Two or more modules named '" + config.identifier + "' detected";
          site_.getLogger().error(msg);
          throw new ConfigurationException(msg);
        }
      }

      // Try to instantiate module class
      Module module = null;
      Class clazz = null;
      try {
        if (config.moduleClass != null) {
          clazz = config.classLoader.loadClass(config.moduleClass);
        } else {
          clazz = ModuleImpl.class;
        }
        module = (Module) clazz.newInstance();
      } catch (ClassNotFoundException e) {
        site_.getLogger().error("Module class '" + config.moduleClass + "' was not found!");
        return null;
      } catch (NoClassDefFoundError e) {
        String msg = "Class '" + e.getMessage() + "' not found which is required by module class '" + config.moduleClass + "' of module '" + config.identifier + "'";
        log_.debug(msg, e);
        site_.getLogger().error(msg);
        return null;
      } catch (InstantiationException e) {
        String msg = "Module class '" + config.moduleClass + "' of module '" + config.identifier + "' cound not be instantiated!";
        log_.debug(msg, e);
        site_.getLogger().error(msg);
        return null;
      } catch (IllegalAccessException e) {
        String msg = "Module class '" + config.moduleClass + "' of module '" + config.identifier + "' cound not be accessed!";
        log_.debug(msg, e);
        site_.getLogger().error(msg);
        return null;
      } catch (Exception e) {
        String msg = "Error instantiating module class '" + config.moduleClass + " for module '" + config.identifier + "'";
        log_.debug(msg, e);
        site_.getLogger().error(msg);
        return null;
      }

      // Check if module extends a shared module
      String baseModuleIdentifier = config.getBaseModuleIdentifier();
      if (baseModuleIdentifier != null) {

        // It is not allowed to shadow a module and extend it at the same time
        if (baseModuleIdentifier.equals(config.identifier)) {
          String msg = "Cannot extend module '" + config.identifier + "' and take its name at the same time!";
          throw new ConfigurationException(msg);
        }

        // See if the module to be extended can be found
        Module baseModule = (Module) modules.get(baseModuleIdentifier);
        if (baseModule == null) {
          String msg = "Base module '" + baseModuleIdentifier + "' of module '" + config.identifier + "' not found!";
          throw new ConfigurationException(msg);
        }
        module.setBaseModule(baseModule);
      }

      // Configure the module
      module.configure(config, site_);
      modules.put(module.getIdentifier(), module);
      if (!module.isShared()) {
        configurations_.put(module, config);
      }
      site_.getLogger().debug("Module '" + config.identifier + "' configured");
      return module;
    } catch (ConfigurationException e) {
      String path = config.getFile().getAbsolutePath();
      String msg = "Configuring module " + path + " failed: ";
      msg += (e.getReason() != null) ? e.getReason().getMessage() : e.getMessage();
      site_.getLogger().warn(msg);
      log_.debug(msg, (e.getReason() != null) ? e.getReason() : e);
      throw e;
    } catch (Exception e) {
      String path = config.getFile().getAbsolutePath();
      String msg = "Configuring module " + path + " failed: ";
      msg += e.getMessage();
      site_.getLogger().error(msg, e);
      throw new ConfigurationException(msg, e);
    }
  }

  /**
   * Starts all active modules for the given site.
   * 
   * @param site
   *          the associated site
   */
  public void startAllModules() {
    Iterator modules = site_.getModules().values().iterator();
    while (modules.hasNext()) {
      startModule((Module) modules.next());
    }
  }

  /**
   * Stops all active modules for the given site.
   * 
   * @param site
   *          the associated site
   */
  public void stopAllModules() {
    Iterator modules = site_.getModules().values().iterator();
    while (modules.hasNext()) {
      stopModule((Module) modules.next());
    }
  }

  /**
   * Starts the module.
   * 
   * @param module
   *          the module to start
   */
  public void startModule(Module module) {
    if (module.isEnabled()) {
      site_.getLogger().debug("Bringing up module '" + module.getIdentifier() + "' (" + module.getLoadFactor() + ")");
      module.start();

      // Start jobs
      Iterator ji = module.getJobs().values().iterator();
      while (ji.hasNext()) {
        ModuleJob job = (ModuleJob) ji.next();
        job.setModule(module);
        Daemon.getInstance().addJob(job);
      }

      site_.moduleStarted(module);
      // TODO: Activate module watchdog
    }
  }

  /**
   * Stops the module by calling its <code>shutdown</code> method.
   * 
   * @param module
   *          the module to shut down
   */
  public void stopModule(Module module) {
    site_.getLogger().debug("Shutting down module '" + module.getIdentifier() + "'");

    // Stop jobs
    Iterator ji = module.getJobs().values().iterator();
    while (ji.hasNext()) {
      ModuleJob job = (ModuleJob) ji.next();
      job.setModule(module);
      Daemon.getInstance().removeJob(job);
    }

    // Stop the module and tell listeners
    try {
      module.stop();
      site_.moduleStopped(module);
    } catch (Throwable t) {
      log_.error("Error stopping module " + module + ": " + t.getMessage(), t);
    }
  }

  /**
   * Restarts the module by calling its <code>restart</code> method.
   * 
   * @param module
   *          the module to restart
   */
  public void reloadModule(Module module) {
    site_.getLogger().debug("Restarting module '" + module.getIdentifier() + "'");
    module.restart();
  }

  /**
   * Waits until all shared modules have been loaded.
   */
  private void waitForSharedModules() {
    synchronized (sharedLoader_) {
      while (sharedLoader_.isLoading()) {
        try {
          sharedLoader_.wait();
        } catch (InterruptedException e) {
        }
      }
    }
  }

  /**
   * Adds all shared modules to the current site.
   */
  private void addSharedModules() {
    Iterator ci = sharedConfigurations_.values().iterator();
    while (ci.hasNext()) {
      ModuleConfigurationImpl config = (ModuleConfigurationImpl) ci.next();
      try {
        configureModule(config);
      } catch (ConfigurationException e) {
        site_.getLogger().error("Error while adding shared module '" + config + "'");
      }
    }
  }

}