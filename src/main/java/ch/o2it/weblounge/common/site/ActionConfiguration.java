/**
 * ActionConfiguration.java
 *
 * Copyright 2003 by O2 IT Engineering
 * Zurich,  Switzerland (CH)
 * All rights reserved.
 * 
 * This software is confidential and proprietary information
 * ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into.
 */

package ch.o2it.weblounge.common.site;

import ch.o2it.weblounge.common.Customizable;

/**
 * The action configuration stores configuration information about an action handler
 * instance. The configuration data is beeing read from the <code>&lt;action&gt;</code>
 * section of the <code>module.xml</code>.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public interface ActionConfiguration extends Customizable {

	/** the default valid time is 60 minutes */
	long DEFAULT_VALID_TIME = 60L * 60L * 1000L;

	/** the default recheck time is 1 minute */
	long DEFAULT_RECHECK_TIME = 60L * 1000L;

	/**
	 * Returns the action identifier.
	 * 
	 * @return the identifier
	 */
	String getIdentifier();

	/**
	 * Returns the supported output methods. The meaning of methods is
	 * the possible output format of an action. The methods usually include
	 * <tt>html</tt>, <tt>pdf</tt> and so on.
	 * 
	 * @return the supported methods
	 */
	String[] methods();

	/**
	 * Returns <code>true</code> if the given method is supported by the action.
	 * The method is used to lookup a method for a given action id.
	 * 
	 * @param method the method name
	 * @return <code>true</code> if the action supports the output method
	 */
	boolean provides(String method);

	/**
	 * Returns the mountpoint used to call the action. The mountpoint is
	 * interpreted relative to the site root.
	 * 
	 * @return the action mountpoint
	 */
	String getMountpoint();
	
	/**
	 * Returns the mountpoint extension. The extension is either <code>null</code>,
	 * <code>/*</code> or <code>/**</code>, depending on whether to match only
	 * the mountpoint (e. g. <code>/news</code>), to match the mountpoint and any
	 * direct children (e. g. <code>/news/today</code>) or the mountpoint and any
	 * subsequent urls.
	 * 
	 * @return <code>true</code> if this action has an extended mountpoint
	 */
	String getExtension();
	
	/**
	 * Returns the class name in case of a custom action.
	 * 
	 * @return the class name
	 */
	String getClassName();

	/**
	 * Returns the url that is beeing targeted by default when executing
	 * this action. You may always overwrite this setting by passing a
	 * request parameter of name <tt>url</tt>.
	 * 
	 * @return the target url
	 */
	String getTargetUrl();
	
	/**
	 * Returns the amount of time in milliseconds that the output of this action
	 * will be considered valid. This value is used by the cache to throw away
	 * outdated content.
	 * 
	 * @return the valid time of the action output in milliseconds.
	 */
	long getValidTime();

	/**
	 * Returns the amount of time in milliseconds that the output of this action
	 * will be considered valid. After this time, clients will be advised to recheck
	 * whether the output they may have cached is still valid.
	 * 
	 * @return the recheck time of the action output in milliseconds.
	 */
	long getRecheckTime();
	
	/**
	 * Returns the &lt;link&gt; elements that have been defined for this action.
	 * 
	 * @return the links
	 */
	Include[] getLinks();
	
	/**
	 * Returns the &lt;script&gt; elements that have been defined for this action.
	 * 
	 * @return the scripts
	 */
	ScriptInclude[] getScripts();

}