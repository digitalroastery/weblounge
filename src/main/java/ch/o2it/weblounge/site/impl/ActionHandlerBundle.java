/*
 * Weblounge: Web Content Management System Copyright (c) 2009 The Weblounge
 * Team http://weblounge.o2it.ch
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.site.impl;

import ch.o2it.weblounge.common.Lease;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.url.WebUrlImpl;
import ch.o2it.weblounge.common.impl.util.pool.LeaseFactory;
import ch.o2it.weblounge.common.impl.util.pool.Pool;
import ch.o2it.weblounge.common.impl.util.pool.PoolListener;
import ch.o2it.weblounge.common.site.ActionConfiguration;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;
import ch.o2it.weblounge.common.site.SiteListener;
import ch.o2it.weblounge.common.url.WebUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The action bundle contains at least one but maybe more
 * concrete actions, supporting multiple output methods.
 *
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class ActionHandlerBundle extends LocalizableContent implements PoolListener, SiteListener {

	/** ActionHandler identifier */
	private String identifier_;
	
	/** The supported output methods */
	private List methods_;
	
	/** The action configurations */
	private Map configurations_;
	
	/** Method - Class relation */
	private Map classes_;
	
	/** The action pools */
	private Map pools_;
		
	/** The loadfactor */
	private int loadfactor_;
	
	/** The bundle configuration */
	private ActionBundleConfiguration config_;
	
	/** The defining module */
	Module module_;
	
	/** The active actions */
	//private Map activeActions_;
	
	// Logging

	/** the class name, used for the logging facility */
	protected final static String loggerClass = ActionHandlerBundle.class.getName();

	/** Logging facility */
	protected final static Logger log_ = LoggerFactory.getLogger(loggerClass);

	/**
	 * Creates a new action bundle with the given identifier and the loadfactor
	 * that specifies the load that is being expected on the action.
	 * 
	 * @param identifier the bundle identifier
	 * @param loadfactor the load factor
	 */
	public ActionHandlerBundle(String identifier, int loadfactor) {
		identifier_ = identifier;
		methods_ = new ArrayList();
		classes_ = new HashMap();
		configurations_ = new HashMap();
		pools_ = new HashMap();
		loadfactor_ = loadfactor;
	}

	/**
	 * Returns the action identifier.
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier_;
	}

	/**
	 * Returns the defining site.
	 * 
	 * @return the site
	 */
	public Site getSite() {
		return module_.getSite();
	}

	/**
	 * Sets the associated module.
	 * 
	 * @param module the module
	 */
	public void setModule(Module module) {
		module_ = module;
		module_.getSite().addSiteListener(this);
		Iterator rp = pools_.values().iterator();
		while (rp.hasNext()) {
			Pool pool = (Pool)rp.next();
			pool.setup();
		}
	}
	
	/**
	 * Returns the defining module.
	 * 
	 * @return the module
	 */
	public Module getModule() {
		return module_;
	}

	/**
	 * This method is called to initialize the action bundle with the associated
	 * configuration object.
	 * 
	 * @param config the bundle configuration
	 */
	public void init(ActionBundleConfiguration config) {
		config_ = config;
		Iterator ci = config.configurations.entrySet().iterator();
		while (ci.hasNext()) {
			Map.Entry entry = (Map.Entry)ci.next();
			define((Class)entry.getKey(), (ActionConfiguration)entry.getValue());
		}		
	}
	
	/**
	 * Returns the bundle configuration.
	 * 
	 * @return the bundle configuration
	 */
	public ActionBundleConfiguration getConfiguration() {
		return config_;
	}

	/**
	 * Adds the action definition to this action bundle. The <code>
	 * action</code> argument is used to instantiate actions on
	 * demand, the <code>config</code> is then used to configure the
	 * newly created actions.
	 * 
	 * @param actionClass the action class
	 * @param config the action configuration
	 */
	public void define(Class actionClass, ActionConfiguration config) {
		log_.debug("Defining action '" + config.getIdentifier() + "' as a " + actionClass);
		configurations_.put(actionClass, config);
		String methods[] = config.methods();
		for (int i=0; i < methods.length; i ++) {
			log_.debug("ActionHandler supports method " + methods[i]);
			classes_.put(methods[i], actionClass);
			Pool pool = (Pool)pools_.get(methods[i]);
			if (pool == null) {
				String id = config.getIdentifier() + "/" + methods[i];
				LeaseFactory factory = new ActionFactory(methods[i]);
				pool = new Pool(id, factory, loadfactor_, loadfactor_);
				pool.setStep(loadfactor_);
				pool.addPoolListener(this);
				pools_.put(methods[i], pool);
				log_.debug("Created pool for '" + identifier_ + "/" + methods[i] +"'");
			}
		}
	}

	/**
	 * Returns the supported output methods. The meaning of methods is
	 * the possible output format of a action. Therefore, the methods
	 * usually include <tt>html</tt>, <tt>pdf</tt> and so on.
	 * 
	 * @return the supported methods
	 */
	public String[] methods() {
		String[] result = new String[methods_.size()];
		return (String[])methods_.toArray(result);
	}

	/**
	 * Returns <code>true</code> if the given method is supported by the action.
	 * The method is used to lookup an output method for a given action id.
	 * 
	 * @param method the method name
	 * @return <code>true</code> if the action supports the output method
	 */
	public boolean provides(String method) {
		return methods_.contains(method);
	}

	/**
	 * Returns an instance of the action, supporting the requested
	 * rendering method or <code>null</code> if no such action
	 * could be instantiated.
	 * <p>
	 * Make sure to return the action after the rendering request
	 * has been accomplished by using <code>returnAction</code>
	 * to support action pooling.
	 * 
	 * @param method the requested rendering method
	 * @return the action or <code>null</code>
	 * @see #returnAction(Action)
	 */
	public Action getAction(String method) {
		Pool actions = (Pool)pools_.get(method);
		if (actions != null) {
			Action handler = (Action)actions.getLease();
			handler.setSite(module_.getSite());
			handler.setModule(module_);
			return handler;
		}
		log_.debug("No pool found for '" + identifier_ + "/" + method + "'");
		return null;
	}
	
	/**
	 * Returns the mountpoint extension. The extension is either <code>null</code>,
	 * <code>/*</code> or <code>/**</code>, depending on whether to match only
	 * the mountpoint (e. g. <code>/news</code>), to match the mountpoint and any
	 * direct children (e. g. <code>/news/today</code>) or the mountpoint and any
	 * subsequent urls.
	 * 
	 * @return the mountpoint extension
	 */
	public String getExtension() {
		return config_.extension;
	}

	/**
	 * Returns the url that is being targeted by default when executing
	 * this action. You may always overwrite this setting by passing a
	 * request parameter of name <tt>url</tt>.
	 * 
	 * @return the target url
	 */
	public WebUrl getTargetUrl() {
		if (config_.target != null) {
			return new WebUrlImpl(module_.getSite(), config_.target);
		}
		return null;
	}
	
	/**
	 * Returns a action that has previously been leased from the
	 * local rendering pool.
	 * 
	 * @param action the returned action
	 * @see #getAction(java.lang.String)
	 */
	public void returnAction(Action action) {
		log_.debug("ActionHandler '" + action + "' returned.");
		String[] methods = action.methods();
		for (int i=0; i < methods.length; i++) {
			Pool pool = (Pool)pools_.get(methods[i]);
			if (pool != null) {
				pool.returnLease(action);
			} else {
				log_.warn("Pool for '" + identifier_ + "/" + methods[i] +"' has disappeared!");
			}
		}
	}
	
	/**
	 * Creates a action, suitable for the required rendering method.
	 * If no such action could be created, then this method returns
	 * <code>null</code>.
	 * 
	 * @param method the requested method
	 * @return the new action
	 */
	protected Action createAction(String method) {
		Class actionClass = (Class)classes_.get(method);
		if (actionClass == null) return null;
		try {
			Action r = (Action)actionClass.newInstance();
			r.setSite(module_.getSite());
			r.setModule(module_);
			r.init((ActionConfiguration)configurations_.get(actionClass));
			return r;
		} catch (InstantiationException e) {
			log_.error("Error instantiating action handler of type '" + actionClass + "' for action '" + this + "'");
		} catch (IllegalAccessException e) {
			log_.error("Access violation instantiating action handler of type '" + actionClass + "' for action '" + this + "'");
		}
		return null;
	}
	
	/**
	 * Returns the action class for the requested method.
	 * 
	 * @param method the requested rendering method
	 * @return the action
	 */
	protected Class getClassForMethod(String method) {
		return (Class)classes_.get(method);
	}
	
	/**
	 * Returns the configuration for the specified action class.
	 * 
	 * @param action the action class
	 * @return the action configuration
	 */
	protected ActionConfiguration getConfiguration(Class action) {
		return (ActionConfiguration)configurations_.get(action);
	}
	
	/**
	 * This method is called whenever the pool size has changed. It notifies
	 * the listener about the amount as well as the current pool size.
	 * 
	 * @param amount the increase (positive) or decrease (negative)
	 * @param size the current pool size
	 */
	public void poolSizeChanged(Pool pool, int amount, int size) {
		if (amount > 0) {
			log_.info("Poolsize for '" + pool + "' increased to " + size);
		} else {
			log_.info("Poolsize for '" + pool + "' decreased to " + size);
		}
	}
	
	/**
	 * @see ch.o2it.weblounge.api.site.SiteListener#shutdown(Site)
	 */
	public void shutdown(Site site) {
		Iterator pools = pools_.values().iterator();
		while (pools.hasNext()) {
			((Pool)pools.next()).close();
		}
	}
	
	/**
	 * Returns the string representation of this action bundle.
	 * 
	 * @return the action identifier
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return module_ + "/" + identifier_;
	}

	/**
	 * The <code>ActionFactory</code> will serve as a action factory for
	 * actions on a per method base.
	 * 
	 * @author Tobias Wunden
	 * @version 1.0
	 * @since Weblounge 2.0
	 */
	private class ActionFactory implements LeaseFactory {

		/** The requested action method */
		private String method_;
		
		/**
		 * Creates a new action factory that will produce 
		 * 
		 * @param method the action method
		 */
		public ActionFactory(String method) {
			method_ = method;
		}

		/**
		 * Factory method to create a new lease.
		 * 
		 * @return the new lease
		 */
		public Lease createLease() {
			Class actionClass = null;
			try {
				actionClass = getClassForMethod(method_);
				Action a = (Action)actionClass.newInstance();
				a.setSite(module_.getSite());
				a.setModule(module_);
				a.init(getConfiguration(actionClass));
				return a;
			} catch (InstantiationException e) {
				log_.error("Error instantiating action handler of type '" + actionClass + "' for action '" + ActionHandlerBundle.this + "'");
			} catch (IllegalAccessException e) {
				log_.error("Access violation instantiating action handler of type '" + actionClass + "' for action '" + ActionHandlerBundle.this + "'");
			}
			return null;
		}
		
		/**
		 * Method to cleanly get rid of an unusable lease.
		 *
		 *@param lease the lease to dispose
		 */
		public void disposeLease(Lease lease) {}
		
	}

}