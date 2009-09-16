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

package ch.o2it.weblounge.common.impl.site;

import ch.o2it.weblounge.common.impl.url.UrlSupport;
import ch.o2it.weblounge.common.impl.util.UploadedFile;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.page.Link;
import ch.o2it.weblounge.common.page.Page;
import ch.o2it.weblounge.common.page.PageManager;
import ch.o2it.weblounge.common.page.Script;
import ch.o2it.weblounge.common.renderer.Renderer;
import ch.o2it.weblounge.common.renderer.PageRendererConfiguation;
import ch.o2it.weblounge.common.request.WebloungeRequest;
import ch.o2it.weblounge.common.request.WebloungeResponse;
import ch.o2it.weblounge.common.security.SystemPermission;
import ch.o2it.weblounge.common.security.User;
import ch.o2it.weblounge.common.site.ActionConfiguration;
import ch.o2it.weblounge.common.site.Action;
import ch.o2it.weblounge.common.site.Module;
import ch.o2it.weblounge.common.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is the default implementation for a <code>ActionHandler</code>.
 * The implementations of the various <code>startXYZ</code> methods are
 * implemented such that they leave it to the target page to render the stuff.
 * <p>
 * Be aware of the fact that action handlers are pooled, so make sure to implement
 * the <code>cleanup</code> method to clear any state information from this
 * handler instance.
 *
 * @author Tobias Wunden <tobias.wunden@o2it.ch>
 * @version 1.0
 * @since Weblounge 2.0
 */

public abstract class AbstractActionHandler implements Action {

	/** Request field containing the required field names */
	public static final String FLD_REQUIRED = "required";
	
	/** The action configuration */
	protected ActionConfiguration config;
	
	/** The requested rendering method */
	protected String method;

	/** The parent site */
	protected Site site;

	/** The parent module */
	protected Module module;
	
	/** Map containing uploaded files */
	protected Map files;
	
	/** The number of includes */
	protected int includes = 0;
	
	/** The underlying page */
	protected Page page = null;
	
	/** The renderer used for this request */
	protected Renderer renderer = null;
	
	/** The current request object */
	protected WebloungeRequest request = null;
	
	/** The current response object */
	protected WebloungeResponse response = null;
	
	// Logging

	/** the class name, used for the loggin facility */
	private final static String className = AbstractActionHandler.class.getName();

	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(className);

	/**
	 * Returns the action configuration.
	 * 
	 * @return the configuration
	 */
	public ActionConfiguration getConfiguration() {
		return config;
	}
	
	/**
	 * Sets the action handler configuration, which will be available to sublasses via
	 * the instance member <code>config</code>.
	 * 
	 * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#init(ch.o2it.weblounge.common.site.api.module.ActionConfiguration)
	 */
	public void init(ActionConfiguration config) {
		this.config = config;
		for (Link l : config.getLinks())
			l.setModule(module);
		for (Script s : config.getScripts())
			s.setModule(module);
	}

	/**
	 * Returns the action identifier.
	 * 
	 * @return the action identifier
	 */
	public String getIdentifier() {
		return config.getIdentifier();
	}

	/**
	 * Sets the parent module.
	 * 
	 * @param module the parent module
	 */
	public void setModule(Module module) {
		this.module = module;
	}
	
	/**
	 * Returns the parent module.
	 * 
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * Returns the parent module with the given identifier.
	 * 
	 * @return the module
	 */
	public Module getModule(String module) {
		return this.module.getSite().getModule(module);
	}

	/**
	 * Sets the associated site if this is a site related renderer configuration.
	 * 
	 * @param site the associated site
	 */
	public void setSite(Site site) {
		this.site = site;
	}

	/**
	 * Returns the parent site.
	 * 
	 * @return the site
	 */
	public Site getSite() {
		return site;
	}

	/**
	 * Sets the page that is used to do the action's rendering.
	 * 
	 * @param page the page
	 */
	public void setPage(Page page) {
		this.page = page;
	}

	/**
	 * Returns the page that is used to do the action's rendering.
	 * 
	 * @return the target page
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * Sets the response message that will show up if the template decides to evaluate
	 * it, for example by using the <code>responsemessage</code> tag.
	 * 
	 * @param message the message
	 * @param request the weblounge request
	 * @param response the weblounge response
	 */
	protected void setResponseMessage(ResponseMessage message, WebloungeRequest request, WebloungeResponse response) {
		request.setAttribute(ResponseMessage.REQUEST_ATTRIBUTE, message);
	}
	
	/**
	 * Returns the absolute link pointing to this action.
	 * 
	 * @return the action's link
	 */
	public String getLink() {
		return UrlSupport.concat(new String[] {
				module.getSite().getLink(),
				config.getMountpoint()
		});
	}

	/**
	 * Returns the path relative to the server root, including the weblounge mount
	 * point.
	 * 
	 * @return the action's path
	 */
	public String getPath() {
		return UrlSupport.concat(new String[] {
				Env.getMountpoint(),
				config.getMountpoint()
		});
	}

	/**
	 * Returns the extension part of the requested url. For example, if an action is mounted
	 * to <code>/test</code> and the url is <code>/test/a</code> then this method
	 * will return <code>/a</code>. For the mountpoint itself, the method will return
	 * <code>/</code>.
	 * 
	 * @param request the request
	 * @return the path extension relative to the action's mountpoint
	 */
	public String getRequestedUrlExtension(WebloungeRequest request) {
		return request.getRequestedUrl().getPath().substring(config.getMountpoint().length());
	}
	
	/**
	 * Returns the extension part of the target url. For example, if an action is mounted
	 * to <code>/test</code> and the url is <code>/test/a</code> then this method
	 * will return <code>/a</code>. For the mountpoint itself, the method will return
	 * <code>/</code>.
	 * 
	 * @param request the request
	 * @return the path extension relative to the action's mountpoint
	 */
	public String getUrlExtension(WebloungeRequest request) {
		return request.getUrl().getPath().substring(config.getMountpoint().length());
	}

	/**
	 * Sets the method that is requested upon the next run.
	 * 
	 * @param method the rendering method
	 */
	protected void setMethod(String method) {
		this.method = method;
	}
	
	/**
	 * Returns the requested rendering method.
	 * 
	 * @return the rendering method
	 */
	protected String getMethod() {
		return method;
	}
	
	/**
	 * Configures the request to use the given renderer.
	 * 
	 * @param renderer the renderer identifier
	 * @param request the request
	 */
	protected void setRenderer(String renderer, WebloungeRequest request) {
		request.setAttribute(Renderer.TEMPLATE, renderer);
	}

	/**
	 * Returns <code>true</code> if <code>composer</code> equals the stage
	 * of the current renderer.
	 * 
	 * @param composer the composer to test
	 * @param request the request
	 * @return <code>true</code> if <code>composer</code> is the main stage
	 */
	protected boolean isStage(String composer, WebloungeRequest request) {
		if (composer == null)
			throw new IllegalArgumentException("Composer may not be null!");
		String rendererId = (String)request.getAttribute(Renderer.TEMPLATE);
		Renderer renderer = null;
		if (rendererId != null) {
			renderer = getSite().getRenderers().getRenderer(rendererId, request.getOutputMethod());
		} else {
			long version = request.getVersion();
			Site site = request.getSite();
			User user = request.getUser();
			String path = request.getUrl().getPath();
			Page page = PageManager.getPage(path, site, user, SystemPermission.READ, version);
			renderer = page.getRenderer(request.getOutputMethod());
		}
		if (renderer != null && renderer.getConfiguration() instanceof PageRendererConfiguation) {
			return composer.equalsIgnoreCase(((PageRendererConfiguation)renderer.getConfiguration()).getStage());
		}
		return composer.equalsIgnoreCase(PageRendererConfiguation.DEFAULT_STAGE);
	}
	
	/**
	 * Returns <code>true</code> if the given method is supported by the action.
	 * The method is used to lookup a rendering method for a given action id.
	 * 
	 * @param method the method name
	 * @return <code>true</code> if the action supports the rendering method
	 */
	public boolean provides(String method) {
		return config.provides(method);
	}

	/**
	 * Returns the supported action methods. The meaning of methods is
	 * the possible output format of an action. Therefore, the methods
	 * usually include <tt>html</tt>, <tt>pdf</tt> and so on.
	 * 
	 * @return the supported methods
	 */
	public String[] methods(){
		return config.methods();
	}
	
	/**
	 * @see ch.o2it.weblounge.common.site.Action.module.ActionHandler#configure(ch.o2it.weblounge.api.request.WebloungeRequest, ch.o2it.weblounge.api.request.WebloungeResponse, java.lang.String)
	 */
	public void configure(WebloungeRequest request, WebloungeResponse response, String method) {
		includes = 0;
		this.request = request;
		this.response = response;
		this.method = method;
		if (request instanceof MultipartRequestWrapper) {
			Iterator fi = ((MultipartRequestWrapper)request).files();
			while (fi.hasNext()) {
				UploadedFile f = (UploadedFile)fi.next();
				addFile(f.getFieldName(), f);
			}
		}
	}

	/**
	 * Adds a file to the list of uploaded files.
	 * 
	 * @param name the file name
	 * @param file the file
	 */
	private void addFile(String name, UploadedFile file) {
		if (files == null) {
			files = new HashMap<String, UploadedFile>();
		}
		Object o = files.get(name);
		if (o == null) {
			files.put(name, file);
		} else if (o instanceof List) {
			List<UploadedFile> f = (List<UploadedFile>)o;
			f.add(file);
		} else if (o instanceof UploadedFile) {
			List<UploadedFile> f = new ArrayList<UploadedFile>();
			f.add((UploadedFile)o);
			f.add(file);
			files.put(name, f);
		}
	}

	/**
	 * Returns an iteration of the files that have been uploaded in the current
	 * step. Note that this iterator may be empty if no files are present, since
	 * the files collection is cleared if the wizard moves on.
	 * <br>
	 * The iterator returns elements of type <code>UploadedFile</code>.
	 * 
	 * @return an iteration of uploaded files
	 */
	protected Iterator files() {
		if (files != null)
			return files.values().iterator();
		return (new ArrayList()).iterator();
	}

	/**
	 * Includes the given renderer with the request.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param renderer the renderer to include
	 * @param data is passed to the renderer
	 * @throws ActionHandlerException if the passed renderer is <code>null</code>
	 */
	protected void include(WebloungeRequest request, WebloungeResponse response, Renderer renderer, Object data) {
		if (renderer == null) {
			String msg = "The renderer passed to include in action '" + this + "' was <null>!";
			throw new ActionHandlerException(this, "html", new IllegalArgumentException(msg));
		}
	
		// Prepare caching
		Language language = request.getLanguage();
		User user = request.getUser();
		long validTime = renderer.getConfiguration().getValidTime();
		long recheckTime = renderer.getConfiguration().getRecheckTime();
		Cache cache = (Cache)SystemServices.getEnabled(Cache.ID);
		CacheTagSet rendererTagSet = new CacheTagSet();
		CacheHandle rendererCacheHdl = null;

		if (cache != null) {
			rendererTagSet.add("webl:site", request.getSite().getIdentifier());
			rendererTagSet.add("webl:url", request.getUrl().getPath());
			rendererTagSet.add("webl:url", request.getRequestedUrl().getPath());
			rendererTagSet.add("webl:language", language.getIdentifier());
			rendererTagSet.add("webl:user", user.getLogin());
			rendererTagSet.add("webl:module", getModule().getIdentifier());
			rendererTagSet.add("webl:action", getIdentifier());
			rendererTagSet.add("webl:include-position", includes);
			Enumeration<?> pe = request.getParameterNames();
			int parameterCount = 0;
			while (pe.hasMoreElements()) {
				parameterCount ++;
				String key = pe.nextElement().toString();
				String[] values = request.getParameterValues(key);
				for (String value : values) {
					rendererTagSet.add(key, value);
				}
			}
			rendererTagSet.add("webl:parameters", Integer.toString(parameterCount));
			rendererCacheHdl = cache.startResponsePart(rendererTagSet, response, validTime, recheckTime);
			if (rendererCacheHdl == null)
				return;
		}
		
		// Include renderer
		try {
			
			// Add additional cache tags
			if (renderer.getModule() != null)
				response.addTag("webl:module", renderer.getModule());
			response.addTag("webl:renderer", renderer.getIdentifier());

			// Render
			renderer.configure(request.getOutputMethod(), data);
			renderer.render(request, response);
			renderer.cleanup();

		} finally {
			Module m = renderer.getModule();
			if (m != null)
				m.returnRenderer(renderer);
			if (rendererCacheHdl != null) {
				cache.endResponsePart(rendererCacheHdl, response);
			}
		}
		includes ++;
	}

	/**
	 * Requests the renderer with the given id from the current module
	 * and Includes it in the request.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param renderer the renderer to include
	 * @throws ActionHandlerException if the passed renderer cannot be found.
	 */
	protected void include(WebloungeRequest request, WebloungeResponse response, String renderer) {
		include(request, response, getModule(), renderer, null);
	}

	/**
	 * Requests the renderer with the given id from the current module
	 * and Includes it in the request.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param renderer the renderer to include
	 * @param data is passed to the renderer
	 * @throws ActionHandlerException if the passed renderer cannot be found.
	 */
	protected void include(WebloungeRequest request, WebloungeResponse response, String renderer, Object data) {
		include(request, response, getModule(), renderer, data);
	}

	/**
	 * Requests the renderer with the given id from module <code>module</code>
	 * and Includes it in the request.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param module the module identifier
	 * @param renderer the renderer to include
	 * @param data is passed to the renderer
	 * @throws ActionHandlerException if the passed renderer cannot be found.
	 */
	protected void include(WebloungeRequest request, WebloungeResponse response, String module, String renderer, Object data) {
		if (module == null)
			throw new ActionHandlerException(this, "html", new IllegalArgumentException("Module is null!"));
		if (renderer == null)
			throw new ActionHandlerException(this, "html", new IllegalArgumentException("Renderer is null!"));
		Module m = getSite().getModule(module);
		if (m == null) {
			String msg = "Trying to include renderer from unknown module '" + module + "'";
			throw new ActionHandlerException(this, "html", new IllegalArgumentException(msg));
		}
		include(request, response, m, renderer, data);
	}

	/**
	 * Requests the renderer with the given id from module <code>module</code>
	 * and Includes it in the request.
	 * 
	 * @param request the request
	 * @param response the response
	 * @param module the module
	 * @param renderer the renderer to include
	 * @param data is passed to the renderer
	 * @throws ActionHandlerException if the passed renderer cannot be found.
	 */
	protected void include(WebloungeRequest request, WebloungeResponse response, Module module, String renderer, Object data) {
		if (module == null)
			throw new ActionHandlerException(this, "html", new IllegalArgumentException("Module is null!"));
		if (renderer == null)
			throw new ActionHandlerException(this, "html", new IllegalArgumentException("Renderer is null!"));
		Renderer r = module.getRenderer(renderer);
		if (r == null) {
			String msg = "Trying to include unknown renderer '" + renderer + "'";
			throw new ActionHandlerException(this, "html", new IllegalArgumentException(msg));
		}
		log_.debug("Including renderer " + renderer);
		include(request, response, r, data);
	}

	/**
	 * Returns <code>true</code> if <code>o</code> equals this action
	 * handler.
	 * 
	 * @param o the object to test for equality
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof AbstractActionHandler) {
			AbstractActionHandler h = (AbstractActionHandler)o;
			return this.config == h.config;
		}
		return false;
	}
	
	/**
	 * @see ch.o2it.weblounge.common.impl.core.util.pool.Lease#leased()
	 */
	public void leased() { }

	/**
	 * This method clears all member variables except for the action configuration, site
	 * and module which are only set once.
	 * 
	 * @see ch.o2it.weblounge.common.impl.core.util.pool.Lease#returned()
	 */
	public void returned() {
		cleanup();
		site = null;
		module = null;
		method = null;
		files = null;
		includes = 0;
		page = null;
		renderer = null;
		request = null;
		response = null;
	}

	/**
	 * @see ch.o2it.weblounge.common.impl.core.util.pool.Lease#dispose()
	 */
	public boolean dispose() {
		return false;
	}

	/**
	 * @see ch.o2it.weblounge.common.impl.core.util.pool.Lease#retired()
	 */
	public void retired() { }

	/**
	 * Returns a list of required fields, which are determined by evaluating the
	 * request parameter <code>{@link #FLD_REQUIRED}</code>
	 * and then searching the request for the fields mentioned there.
	 * 
	 * @param request the request
	 * @return a list with missing fields
	 */
	protected static List<String> checkMissingFields(WebloungeRequest request) {
		List<String> incomplete = new ArrayList<String>();
		
		String fldRequired = request.getParameter(FLD_REQUIRED);
		if (fldRequired == null || fldRequired.trim().length() == 0) {
			return null;
		}
	
		// Check existance of required fields
	
		String[] required = ConfigurationUtils.getMultiOptionValues(fldRequired);
		for (int i=0; i < required.length; i++) {
			String field = required[i];
			String value = request.getParameter(field);
			if (value == null || value.trim().length() == 0) {
				incomplete.add(field);
			}
		}

		if (incomplete.size() > 0) {
			request.setAttribute(FLD_REQUIRED, incomplete);
			return incomplete;
		}
		return null;
	}

	/**
	 * Returns <code>s</code> if <code>s</code> is not <code>null</code>. otherwise,
	 * the replacement is returned.
	 * 
	 * @param s the original text
	 * @param replacement the replacement
	 * @return <code>s</code> or <code>replacement</code>
	 */
	protected static String replaceNull(String s, String replacement) {
		return (s == null || s.trim().equals("")) ? replacement : s;
	}

	/**
	 * Returns <code>s</code> if <code>s</code> is not the empty string. otherwise,
	 * <code>null</code> is returned.
	 * 
	 * @param s the original text
	 * @return <code>s</code> or <code>null</code>
	 */
	protected static String emptyToNull(String s) {
		return (s != null && s.trim().equals("")) ? null : s;
	}

	/**
	 * Returns a string representation of this action, which consists of
	 * the action identifier and the configured method.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getModule().getIdentifier() + "/" + config.getIdentifier() + ((method != null) ? " (" + method + ")" : "");
	}

}