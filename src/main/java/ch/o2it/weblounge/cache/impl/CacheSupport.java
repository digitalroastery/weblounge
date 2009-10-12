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

package ch.o2it.weblounge.cache.impl;

import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.url.WebUrl;

import java.util.Map;

/**
 * Helper class to deal with cache invalidation.
 * 
 * @author Tobias Wunden
 */
public class CacheSupport {
	
	/** Flag to check whether the cache is enabled */
	private static boolean isCacheEnabled = false;

	/** Flag to check whether the shared cache is enabled */
	private static boolean isSharedCacheEnabled = false;

	/** The cache service */
	private static CacheServiceImpl cache = null;
	
	/** The shared cache service */
	private static SharedCache sharedCache = null;

	/** CacheSupport must not be instantiated */
	private CacheSupport() { }

	/**
	 * Configures the cache support.
	 */
	static void configure() {
		cache = (CacheServiceImpl) SystemServices.getEnabled(CacheServiceImpl.ID);
		sharedCache = (SharedCache) SystemServices.getEnabled(SharedCache.ID);
		isCacheEnabled = cache != null && cache.isEnabled();
		isSharedCacheEnabled = sharedCache != null && sharedCache.isEnabled();
	}

	/**
	 * Removes the elements that are being matched by the given tag set from the
	 * cache.
	 * 
	 * @param tags
	 *            the set of tags identifying the cache elements to be removed
	 */
	public static void invalidate(CacheTagSet tags) {
		// If a shared cache is configured, invalidate shared
		if (isSharedCacheEnabled)
			sharedCache.invalidate(tags);
		
		// If the cache is active, invalidate locally
		if (isCacheEnabled)
			cache.invalidate(tags);
	}

	/**
	 * Removes the given url from the cache.
	 * 
	 * @param url
	 *            the url to remove
	 */
	public static void invalidate(WebUrl url) {
		invalidate(url, null, null);
	}

	/**
	 * Removes the given url from the cache. All values except
	 * <code>url</code> may be <code>null</code>.
	 * 
	 * @param url
	 *            the url to remove
	 * @param user
	 *            the user for whom the pages are being removed
	 */
	public static void invalidate(WebUrl url, User user) {
		invalidate(url, null, user);
	}

	/**
	 * Removes the given url from the cache. All values except
	 * <code>url</code> may be <code>null</code>. The more parameters
	 * you specify, the less pages will be removed from cache.
	 * 
	 * @param url
	 *            the url to remove
	 * @param language
	 *            the language variants to be removed
	 */
	public static void invalidate(WebUrl url, Language language) {
		invalidate(url, language, null);
	}

	/**
	 * Removes the given url from the cache. All values except
	 * <code>url</code> may be <code>null</code>.
	 * 
	 * @param url
	 *            the url to remove
	 * @param language
	 *            the language variants to be removed
	 * @param user
	 *            the user for whom the pages are being removed
	 */
	public static void invalidate(WebUrl url, Language language, User user) {
		assert url != null;
		CacheServiceImpl cache = (CacheServiceImpl) SystemServices.getEnabled(CacheServiceImpl.ID);
		if (cache == null)
			return;
		
		// Create tag set
		CacheTagSet tags = new CacheTagSet();
		tags.add("webl:url", url.getPath());
		tags.add("webl:site", url.getSite().getIdentifier());
		if (language != null)
			tags.add("webl:language", language.getIdentifier());
		if (user != null)
			tags.add("webl:user", user.getLogin());
		
		// Remove everything that matches
		cache.invalidate(tags);		
	}
	
	/**
	 * Removes the given action from the cache.
	 * 
	 * @param action
	 *            the action to remove
	 */
	public static void invalidate(ActionHandler action) {
		invalidate(action, null, null, null);
	}

	/**
	 * Removes the given action from the cache. All values except
	 * <code>action</code> may be <code>null</code>.
	 * 
	 * @param action
	 *            the action to remove
	 * @param params
	 *            the action parameters
	 */
	public static void invalidate(ActionHandler action, Map<String, String> params) {
		invalidate(action, null, null, params);
	}

	/**
	 * Removes the given action from the cache. All values except
	 * <code>action</code> may be <code>null</code>.
	 * 
	 * @param action
	 *            the action to remove
	 * @param user
	 *            the user for whom the pages are being removed
	 */
	public static void invalidate(ActionHandler action, User user) {
		invalidate(action, null, user, null);
	}

	/**
	 * Removes the given action from the cache. All values except
	 * <code>action</code> may be <code>null</code>.
	 * 
	 * @param action
	 *            the action to remove
	 * @param user
	 *            the user for whom the pages are being removed
	 * @param params
	 *            the action parameters
	 */
	public static void invalidate(ActionHandler action, User user, Map<String, String> params) {
		invalidate(action, null, user, params);
	}

	/**
	 * Removes the given action from the cache. All values except
	 * <code>action</code> may be <code>null</code>. The more parameters
	 * you specify, the less pages will be removed from cache.
	 * 
	 * @param action
	 *            the action to remove
	 * @param language
	 *            the language variants to be removed
	 */
	public static void invalidate(ActionHandler action, Language language) {
		invalidate(action, language, null, null);
	}

	/**
	 * Removes the given action from the cache. All values except
	 * <code>action</code> may be <code>null</code>.
	 * 
	 * @param action
	 *            the action to remove
	 * @param language
	 *            the language variants to be removed
	 * @param params
	 *            the action parameters
	 */
	public static void invalidate(ActionHandler action, Language language, Map<String, String> params) {
		invalidate(action, language, null, params);
	}

	/**
	 * Removes the given action from the cache. All values except
	 * <code>action</code> may be <code>null</code>.
	 * 
	 * @param action
	 *            the action to remove
	 * @param language
	 *            the language variants to be removed
	 * @param user
	 *            the user for whom the pages are being removed
	 */
	public static void invalidate(ActionHandler action, Language language, User user) {
		invalidate(action, language, user, null);
	}

	/**
	 * Removes the given action from the cache. All values except
	 * <code>action</code> may be <code>null</code>.
	 * 
	 * @param action
	 *            the action to remove
	 * @param language
	 *            the language variants to be removed
	 * @param user
	 *            the user for whom the pages are being removed
	 * @param params
	 *            the action parameters
	 */
	public static void invalidate(ActionHandler action, Language language, User user, Map<String, String> params) {
		assert action != null;
		CacheServiceImpl cache = (CacheServiceImpl) SystemServices.getEnabled(CacheServiceImpl.ID);
		if (cache == null)
			return;

		// Create tag set
		CacheTagSet tags = new CacheTagSet();
		tags.add("webl:action", action.getIdentifier());
		tags.add("webl:module", action.getModule().getIdentifier());
		tags.add("webl:site", action.getSite().getIdentifier());
		if (language != null)
			tags.add("webl:language", language.getIdentifier());
		if (user != null)
			tags.add("webl:user", user.getLogin());
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				tags.add(key, value);
			}
		}

		// Remove everything that matches
		cache.invalidate(tags);		
	}

	/**
	 * Invalidates the action specified by <code>module</code> and
	 * <code>action</code>.
	 * 
	 * @param module the action's module
	 * @param action the action identifier
	 */
	public static void invalidateAction(Module module, String action) {
		invalidateAction(module, action, null, null, null);
	}

	/**
	 * Invalidates the action specified by <code>module</code> and
	 * <code>action</code>.
	 * 
	 * @param module the action's module
	 * @param action the action identifier
	 * @param language the language
	 * @param user the user
	 * @param params the parameters
	 */
	public static void invalidateAction(Module module, String action, Language language, User user, Map<String, String> params) {
		CacheServiceImpl cache = (CacheServiceImpl) SystemServices.getEnabled(CacheServiceImpl.ID);
		if (cache == null)
			return;

		if (module == null)
			throw new IllegalArgumentException("Argument 'module' must not be null");
		if (action == null)
			throw new IllegalArgumentException("Argument 'action' must not be null");
		
		// Create tag set
		CacheTagSet tags = new CacheTagSet();
		tags.add("webl:action", action);
		tags.add("webl:module", module.getIdentifier());
		tags.add("webl:site", module.getSite().getIdentifier());
		if (language != null)
			tags.add("webl:language", language.getIdentifier());
		if (user != null)
			tags.add("webl:user", user.getLogin());
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				tags.add(key, value);
			}
		}

		// Remove everything that matches
		cache.invalidate(tags);		
	}

	/**
	 * Invalidates the renderer specified by <code>module</code> and
	 * <code>renderer</code>.
	 * 
	 * @param module the renderer's module
	 * @param renderer the renderer identifier
	 */
	public static void invalidateRenderer(Module module, String renderer) {
		invalidateRenderer(module, renderer, null, null, null);
	}

	/**
	 * Invalidates the renderer specified by <code>module</code> and
	 * <code>renderer</code>.
	 * 
	 * @param module the renderer's module
	 * @param renderer the renderer identifier
	 * @param language the language
	 * @param user the user
	 * @param params the parameters
	 */
	public static void invalidateRenderer(Module module, String renderer, Language language, User user, Map<String, String> params) {
		CacheServiceImpl cache = (CacheServiceImpl) SystemServices.getEnabled(CacheServiceImpl.ID);
		if (cache == null)
			return;

		if (module == null)
			throw new IllegalArgumentException("Argument 'module' must not be null");
		if (renderer == null)
			throw new IllegalArgumentException("Argument 'action' must not be null");
		
		// Create tag set
		CacheTagSet tags = new CacheTagSet();
		tags.add("webl:renderer", renderer);
		tags.add("webl:module", module.getIdentifier());
		tags.add("webl:site", module.getSite().getIdentifier());
		if (language != null)
			tags.add("webl:language", language.getIdentifier());
		if (user != null)
			tags.add("webl:user", user.getLogin());
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				tags.add(key, value);
			}
		}

		// Remove everything that matches
		cache.invalidate(tags);		
	}

}