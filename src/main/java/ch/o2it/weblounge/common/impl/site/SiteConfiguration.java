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

import ch.o2it.weblounge.common.ConfigurationException;
import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.impl.language.LanguageSupport;
import ch.o2it.weblounge.common.impl.language.LocalizableContent;
import ch.o2it.weblounge.common.impl.security.WebloungeAdmin;
import ch.o2it.weblounge.common.impl.security.jaas.AdminLoginModule;
import ch.o2it.weblounge.common.impl.util.Arguments;
import ch.o2it.weblounge.common.impl.util.ServletConfiguration;
import ch.o2it.weblounge.common.impl.util.ServletMapping;
import ch.o2it.weblounge.common.impl.util.classloader.SiteClassLoader;
import ch.o2it.weblounge.common.impl.util.config.XMLConfigurator;
import ch.o2it.weblounge.common.impl.util.xml.XMLUtilities;
import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.request.RequestHandler;
import ch.o2it.weblounge.common.security.AuthenticationModule;
import ch.o2it.weblounge.common.service.Service;
import ch.o2it.weblounge.common.site.ImageStyle;
import ch.o2it.weblounge.common.site.ImageStyleRegistry;
import ch.o2it.weblounge.common.site.SiteAdmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.spi.ServiceRegistry;
import javax.xml.xpath.XPath;

/**
 * <code>SiteConfiguration</code> represents the contents of the
 * <code>&lt;site&gt;</code> node from a site configuration file
 * <code>site.xml</code>.
 * <p>
 * A site configuration looks like this:
 * <p>
 * <pre>
 * 	&lt;site id="www"&gt;
 * 		&lt;enable&gt;true&lt;/enable&gt;
 * 		&lt;description&gt;Main site&lt;/description&gt;
 * 		.
 * 		.
 * 		.
 * 	&lt;/site&gt;
 * </pre>
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */

public class SiteConfiguration extends XMLConfigurator implements Customizable {

	/** The default lodafactor */
	public static final int DEFAULT_LOADFACTOR = 1;
	
	/** The default history size */
	public static final int DEFAULT_HISTORYSIZE = 1;
	
	/** Site identifier */
	String identifier;
	
	/** special class loader for module classes */
	SiteClassLoader classLoader;

	/** Site description */
	LocalizableContent description;

	/** True if the site is enabled */
	boolean isEnabled;
	
	/** True if this is the system default site */
	boolean isDefault;

	/** The site class implementation to load */
	String siteClass;

	/** Server names that match to this site */
	List urls;
	
	/** Main url mountpoints */
	List mountpoints;
	
	/** The JAAS authentication modules */
	List authenticationModules;
	
	/** The site languages */
	LanguageRegistry languages;
	
	/** The renderer registry */
	RendererRegistry renderers;
	
	/** The service registry */
	ServiceRegistry services;
	
	/** the request handler registry */
	RequestHandlerRegistry handlers;
	
	/** The image style registry */
	ImageStyleRegistry imagestyles;
	
	/** Site cron jobs */
	List<SiteJob> jobs;
	
	/** Site load factor */
	int loadfactor = DEFAULT_LOADFACTOR;
	
	/** Number of versions to keep */
	int historysize = DEFAULT_HISTORYSIZE;
	
	/** True to use the page cache */
	boolean usePageCache = true;

	/** the servlet request handler */
	ServletRequestHandler servletHandler = null;
	
	/** the site administrator */
	SiteAdmin admin = null;
	
	// Logging

	/** the class name, used for the loggin facility */
	private final static String loggerClass = SiteConfiguration.class.getName();

	/** Logging facility */
	private final static Logger log_ = LoggerFactory.getLogger(loggerClass);
	
	/**
	 * Creates a new site configuration.
	 * 
	 * @param file the configuration file
	 */
	public SiteConfiguration(File file) { 
		super(file, "Site configuration");
		String basePath = getFile().getParent();
		if (!basePath.endsWith(File.separator))
			basePath += File.separatorChar;
		classLoader = new SiteClassLoader(basePath);
		urls = new ArrayList();
		authenticationModules = new ArrayList();
		handlers = new RequestHandlerRegistry();
		imagestyles = new ImageStyleRegistryImpl();
		mountpoints = new ArrayList();
		renderers = new RendererRegistry();
		services = new ServiceRegistry();
		jobs = new ArrayList<SiteJob>();
	}

	/**
	 * Reads the site configuration from the given xml configuration node.
	 * 
	 * @param config the configuration node
	 * @throws ConfigurationException if there are errors in the configuration
	 */
	public void configure(Document config) throws ConfigurationException {
		Arguments.checkNull(config, "config");
		XPath path = XMLUtilities.getXPath();
		try {
			readMainSettings(path, XPathHelper.select(path, config, "/site"));
			readOptions(path, XPathHelper.select(path, config, "/site"));
			readAdmin(path, XPathHelper.select(path, config, "/site/admin"));
			readUrls(path, XPathHelper.select(path, config, "/site/urls"));
			readAuthenticationModules(path, XPathHelper.select(path, config, "/site/authentication"));
			readMountpoints(path, XPathHelper.select(path, config, "/site/urls"));
			readLanguages(path, XPathHelper.select(path, config, "/site/languages"));
			readPerformanceSettings(path, XPathHelper.select(path, config, "/site/performance"));
			readLayouts(path, XPathHelper.select(path, config, "/site/layouts"));
			readRenderers(path, XPathHelper.select(path, config, "/site/renderers"));
			readImagestyles(path, XPathHelper.select(path, config, "/site/imagestyles"));
			readJobs(path, XPathHelper.select(path, config, "/site/jobs"));
			readServices(path, XPathHelper.select(path, config, "/site/services"));
			readHandlers(path, XPathHelper.select(path, config, "/site/handlers"));
			readServlets(path, XPathHelper.select(path, config, "/site"));
			setName("Site '" + identifier + "' configuration watchdog");
		} catch (ConfigurationException e) {
			throw e;
		} catch (Exception e) {
			log_.error("Error when reading site configuration: " + e.getMessage(), e);
			throw new ConfigurationException("Error when reading site configuration!", e);
		}
	}

	/**
	 * Returns the site identifier, e. g. <tt>wfc2004</tt>.
	 * 
	 * @return the site identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the site description, e. g. <tt>World Championships 2004</tt>.
	 * 
	 * @param l the language used to return the description
	 * @return the site description
	 */
	public String getDescription(Language l) {
		return description.toString(l);
	}

	/**
	 * Returns <code>true</code> if <code>o</code> is a <code>SiteConfiguration
	 * </code> and maches this configuration in every aspect.
	 * 
	 * @return <code>true</code> if o is equal to this configuration
	 */
	public boolean equals(Object o) {
		if (o instanceof SiteConfiguration) {
			SiteConfiguration s = (SiteConfiguration)o;
			return (
				identifier.equals(s.identifier) &&
				description.equals(s.description) &&
				isEnabled == s.isEnabled
			);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	/**
	 * Reads the main site settings like identifier, name and description
	 * from the site configuration.
	 * 
	 * @param config site configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readMainSettings(XPath path, Node config) throws ConfigurationException {
		identifier = XPathHelper.valueOf(path, config, "@id");
		isDefault = "true".equals(XPathHelper.valueOf(path, config, "@default"));
		siteClass = XPathHelper.valueOf(path, config, "class");
		if (siteClass == null)
			siteClass = "ch.o2it.weblounge.core.site.SiteImpl";
		description = new LocalizableContent();
		LanguageSupport.addDescriptions(path, config, null, description);
		isEnabled = "true".equals(XPathHelper.valueOf(path, config, "enable", false).toLowerCase());
	}

	/**
	 * Reads the site's contact information.
	 * 
	 * @param config contact configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readAdmin(XPath path, Node config) {
		if (config != null) {
			String login = XPathHelper.valueOf(path, config, "login");
			if (login.equals(WebloungeAdmin.getInstance().getLogin())) {
				throw new ConfigurationException("Site administrator login '" + login + "' is not allowed. Login is taken by system administrator");
			}
			String password = XPathHelper.valueOf(path, config, "password");
			String email = XPathHelper.valueOf(path, config, "email");
			String firstname = XPathHelper.valueOf(path, config, "firstname");
			String lastname = XPathHelper.valueOf(path, config, "lastname");
			
			if (login == null || password == null) {
				throw new ConfigurationException("Site administrator definition missing!");
			}
			
			admin = new SiteAdmin(login, password, email);
			admin.setFirstName(firstname);
			admin.setLastName(lastname);
		} else {
			throw new ConfigurationException("Site administrator definition missing!");
		}
	}

	/**
	 * Reads the urls that lead to this site from the configuration.
	 * 
	 * @param config urls configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readUrls(XPath path, Node config) {
		NodeList urlNodes = XPathHelper.selectList(path, config, "url");
		for (int i=0; i < urlNodes.getLength(); i++) {
			Node node = urlNodes.item(i);
			String url = node.getFirstChild().getNodeValue();
			urls.add(url);
			log_.debug("Found site url " + url);
		}
	}

	/**
	 * Reads the JAAS authentication module definitions.
	 * 
	 * @param config authentication configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readAuthenticationModules(XPath path, Node config) {
		if (config == null) {
			log_.debug("No authentication modules found");
			return;
		}
		NodeList moduleNodes = XPathHelper.selectList(path, config, "module");
		for (int i=0; i < moduleNodes.getLength(); i++) {
			Node node = moduleNodes.item(i);
			try {
				AuthenticationModule module = new AuthenticationModule(path, node, classLoader);
				authenticationModules.add(module);
				log_.debug("Login module " + module.getClass() + " registered");
			} catch (Exception e) {
				String msg = "Error reading authentication module: " + e.getMessage();
				log_.warn(msg);
			}
		}		
		// By default, add authentication for superuser login
		authenticationModules.add(new AuthenticationModule(AdminLoginModule.class.getName(), "sufficient", classLoader));
	}

	/**
	 * Reads the mount points (main urls) from the configuration.
	 * 
	 * @param config urls configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readMountpoints(XPath path, Node config) {
		if (config == null) {
			log_.debug("No mountpoint definitions found");
			return;
		}
		NodeList mountpointNodes = XPathHelper.selectList(path, config, "mount");
		for (int i=0; i < mountpointNodes.getLength(); i++) {
			Node node = mountpointNodes.item(i);
			String url = node.getAttributes().getNamedItem("url").getNodeValue();
			String partition = node.getAttributes().getNamedItem("partition").getNodeValue();
			Mountpoint mp = new MountpointImpl(url, partition);
			mountpoints.add(mp);
			log_.debug("Partition '" + mp.getPartition() + "' mounted to url '" + mp.getUrl() + "'");
		}
	}
	
	/**
	 * Reads the site languages from the site configuration and checks whether
	 * the languages have been defined in the system settings.
	 * 
	 * @param config languages configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readLanguages(XPath path, Node config) throws ConfigurationException {
		if (config == null) {
			throw new ConfigurationException("A site must at least have one language!");
		}

		String allLanguages = XPathHelper.valueOf(path, config, "all");
		String defaultLanguage = XPathHelper.valueOf(path, config, "default");
		LanguageRegistry systemLanguages = (LanguageRegistry)SystemRegistries.get(LanguageRegistry.ID);

		// Default language

		Language l = systemLanguages.getLanguage(defaultLanguage);
		if (l != null) {
			languages = new LanguageRegistry(l);
		} else {
			throw new ConfigurationException("The default language " + defaultLanguage + " is not a system language");
		}

		// Site languages

		StringTokenizer tok = new StringTokenizer(allLanguages);
		while (tok.hasMoreTokens()) {
			String id = tok.nextToken();
			l = systemLanguages.getLanguage(id);
			if (l != null) {
				languages.put(id, l);
				log_.debug("Added language " + l);
			} else {
				throw new ConfigurationException("Language " + id + " is not a system language");
			}
		}
	}

	/**
	 * Reads the performance settings from the site configuration.
	 * 
	 * @param config performance configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readPerformanceSettings(XPath path, Node config) throws ConfigurationException {
		if (config == null) {
			log_.debug("No performance settings found");
			return;
		}

		// Load factor
		String factor = XPathHelper.valueOf(path, config, "loadfactor");
		try {
			loadfactor = Integer.parseInt(factor);
			if (loadfactor < 1) {
				String msg = "Error in site configuration: Loadfactor must be >= 1!";
				log_.error(msg);
				throw new ConfigurationException(msg);
			}
		} catch (NumberFormatException e) {
			String msg = "Error in site configuration: Loadfactor must be a number >= 1!";
			log_.error(msg);
			throw new ConfigurationException(msg);
		}

		// History size
		String size = XPathHelper.valueOf(path, config, "history");
		if (size != null) {
			try {
				historysize = Integer.parseInt(size);
				if (historysize < 0) {
					String msg = "Error in site configuration: History size must be >= 0!";
					log_.error(msg);
					throw new ConfigurationException(msg);
				}
			} catch (NumberFormatException e) {
				String msg = "Error in site configuration: Historysize must be a number >= 0!";
				log_.error(msg);
				throw new ConfigurationException(msg);
			}
		} else {
			historysize = DEFAULT_HISTORYSIZE;
		}
		
		// Page cache
		usePageCache = !"false".equalsIgnoreCase(XPathHelper.valueOf(path, config, "pagecache", false));
	}
	
	/**
	 * Reads the urls that lead to this site from the configuration.
	 * 
	 * @param config urls configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readLayouts(XPath path, Node config) throws ConfigurationException {
		if (config == null) {
			log_.debug("No layout definitions found");
			return;
		}
	}
	
	/**
	 * Reads the renderer definitions.
	 * 
	 * @param config renderers configuration node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readRenderers(XPath path, Node config) throws ConfigurationException {
		if (config == null) {
			log_.debug("No renderer definitions found");
			return;
		}
		RendererBundle defaultRenderer = null;
		NodeList rendererNodes = XPathHelper.selectList(path, config, "renderer");
		for (int i=0; i < rendererNodes.getLength(); i++) {
			Node node = rendererNodes.item(i);
			String id = null;
			RendererBundle bundle = null;
			try {
				id = XPathHelper.valueOf(path, node, "@id");
				log_.debug("Reading renderer bundle '" + id + "'");
				RendererBundleConfiguration bundleConfig = new RendererBundleConfiguration(id, getFile());
				bundleConfig.read(path, node);
				bundle = new RendererBundle(id);
				bundle.init(bundleConfig);
				LanguageSupport.addDescriptions(path, node, languages.getDefaultLanguage(), bundle);
				if (!bundle.supportsLanguage(languages.getDefaultLanguage()))
					throw new ConfigurationException("Default language description is missing for renderer bundle '" + id + "'");
	
				// Handle default renderer
				if (defaultRenderer == null || bundleConfig.isDefault()) {
					defaultRenderer = bundle;
				}
				
				// Read jsp renderer definitions
				NodeList jspRenderers = XPathHelper.selectList(path, node, "jsp");
				for (int j=0; j < jspRenderers.getLength(); j++) {
					Node jspNode = jspRenderers.item(j);
					TemplateConfigurationImpl rendererConfig = new TemplateConfigurationImpl(bundleConfig);
					rendererConfig.init(path, jspNode);
					bundle.define(JSPRenderer.class, rendererConfig);
				}
	
				// Read xsl renderer definitions
				NodeList xslRenderers = XPathHelper.selectList(path, node, "xsl");
				for (int j=0; j < xslRenderers.getLength(); j++) {
					Node xslNode = xslRenderers.item(j);
					TemplateConfigurationImpl rendererConfig = new TemplateConfigurationImpl(bundleConfig);
					rendererConfig.init(path, xslNode);
					bundle.define(XSLRenderer.class, rendererConfig);
				}
	
				// Read custom renderer definitions
				NodeList customRenderers = XPathHelper.selectList(path, node, "custom");
				for (int j=0; j < customRenderers.getLength(); j++) {
					Node customNode = customRenderers.item(j);
					TemplateConfigurationImpl rendererConfig = new TemplateConfigurationImpl(bundleConfig);
					rendererConfig.init(path, customNode);
					try {
						Class clazz = classLoader.loadClass(rendererConfig.getClassName());
						bundle.define(clazz, rendererConfig);
					} catch (ClassNotFoundException e) {
						log_.error("Unable to load custom renderer, since class " + rendererConfig.getClassName() + " was not found!", e);
					}
				}
				this.renderers.put(id, bundle);
			} catch (ConfigurationException e) {
				log_.warn("Error when reading renderer bundle '" + id + "'!", e);
			}
		}
		
		// Check if there is at least one renderer defined
		if (defaultRenderer == null) {
			String msg = "Site '" + identifier + "' does not define a default renderer!";
			log_.warn(msg);
			throw new ConfigurationException(msg);
		}
		this.renderers.setDefault(defaultRenderer);
		log_.info("Renderer '" + defaultRenderer + "' is the default renderer");
	}

	/**
	 * Configures the job settings.
	 * 
	 * @param config the jobs node
	 * @param path the XPath object used to parse the configuration
	 */	
	private void readJobs(XPath path, Node config) {
		if (config == null) {
			log_.debug("No job definitions found");
			return;
		}
		log_.debug("Configuring jobs");
		NodeList jobNodes = XPathHelper.selectList(path, config, "job");
		for (int i=0; i < jobNodes.getLength(); i++) {
			Node jobNode = jobNodes.item(i);
			SiteJob job = null;
			
			// See if job is enabled
			String enabled = XPathHelper.valueOf(path, jobNode, "@enabled");
			if (enabled != null && !"true".equals(enabled))
				continue;

			String className = XPathHelper.valueOf(path, jobNode, "class");
			try {
				job = (SiteJob) classLoader.loadClass(className).newInstance();
				job.init(path, jobNode);
				jobs.add(job);
			} catch (ConfigurationException e) {
				log_.debug("Error configuring service!", e.getReason());
				log_.error("Error configuring cronjob '" + job.getName() + "' of site '" + identifier + "': " + e.getMessage());
			} catch (InstantiationException e) {
				log_.error("Error instantiating cronjob '" + className + "' of site '" + identifier + "': " + e.getMessage());
			} catch (IllegalAccessException e) {
				log_.error("Access error instantiating cronjob '" + className + "' of site '" + identifier + "': " + e.getMessage());
			} catch (ClassNotFoundException e) {
				log_.error("Class '" + className + "' for cronjob of site '" + identifier + "' not found: " + e.getMessage());
			} catch (NoClassDefFoundError e) {
				log_.error("Required class '" + className + "' for cronjob of site '" + identifier + "' not found: " + e.getMessage());
			} catch (Exception e) {
				log_.debug("Error configuring job!", e);
				log_.error("Error configuring job '" + ((job != null) ? job.getIdentifier() : "?") + "' of site '" + identifier + "': " + e.getMessage());
			}
		}
		log_.debug("Jobs configured");
	}

	/**
	 * Configures the service settings, such as the preprocessor.
	 * 
	 * @param config the services node
	 * @param path the XPath object used to parse the configuration
	 */	
	private void readServices(XPath path, Node config) {
		if (config == null) {
			log_.debug("No service definitions found");
			return;
		}
		log_.debug("Configuring services");
		NodeList servicesNodes = XPathHelper.selectList(path, config, "service");
		for (int i=0; i < servicesNodes.getLength(); i++) {
			Node serviceNode = servicesNodes.item(i);
			ServiceConfigurationImpl serviceConfig = null;
			try {
				serviceConfig = new ServiceConfigurationImpl();
				serviceConfig.init(path, serviceNode);
				Service s = ServiceManager.loadAndConfigure(serviceConfig, classLoader);
				services.put(serviceConfig.getIdentifier(), s);
			} catch (ConfigurationException e) {
				log_.debug("Error configuring service!", e.getReason());
				log_.error("Error configuring service '" + serviceConfig.getIdentifier() + "' of site '" + identifier + "': " + e.getMessage());
			}
		}
		log_.debug("Services configured");
	}

	/**
	 * Configures the image styles.
	 * 
	 * @param config the services node
	 * @param path the XPath object used to parse the configuration
	 */	
	private void readImagestyles(XPath path, Node config) {
		if (config == null) {
			log_.debug("No image styles found");
			return;
		}
		log_.debug("Configuring imagestyles");
		NodeList styleNodes = XPathHelper.selectList(path, config, "imagestyle");
		for (int i=0; i < styleNodes.getLength(); i++) {
			Node styleNode = styleNodes.item(i);
			String id = XPathHelper.valueOf(path, styleNode, "@id");
			String composeableAttribute = XPathHelper.valueOf(path, styleNode, "@composeable");
			boolean composeable = composeableAttribute == null || "true".equalsIgnoreCase(composeableAttribute);
			try {
				String mode = XPathHelper.valueOf(path, styleNode, "scalingmode");
				String width =  XPathHelper.valueOf(path, styleNode, "width");
				String height =  XPathHelper.valueOf(path, styleNode, "height");
				int m = ImageStyle.SCALE_NONE;
				int h = -1;
				int w = -1;
				if (mode != null && !mode.equals("none")) {
					w = Integer.parseInt(width);
					h = Integer.parseInt(height);
					if ((mode != null) && mode.equals("fit")) {
						m = ImageStyle.SCALE_TO_FIT;
					} else if ((mode != null) && mode.equals("fill")) {
						m = ImageStyle.SCALE_TO_FILL;
					} else {
						String msg = "Found unknown scalingmode for imagestyle '" + id + "': " + mode;
						log_.warn(msg);
						throw new ConfigurationException(msg);
					}
				}
				
				ImageStyleImpl style = new ImageStyleImpl(id, w, h, composeable);
				style.setScalingMode(m);
				LanguageSupport.addDescriptions(path, styleNode, languages.getDefaultLanguage(), style);
				imagestyles.addStyle(style);
			} catch (ConfigurationException e) {
				String msg = "Configuration error when reading imagestyle '" + id + "': ";
				log_.debug(msg, e.getReason());
				log_.warn(msg + e.getReason().getMessage());
			} catch (Exception e) {
				String msg = "Error reading imagestyle '" + id + "': ";
				log_.debug(msg, e);
				log_.warn(msg + e.getMessage());
			}
		}
		log_.debug("Imagestyles configured");
	}

	/**
	 * Configures the request handlers.
	 * 
	 * @param node the handlers node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readHandlers(XPath path, Node node) {
		log_.debug("Configuring request handlers");
		if (node == null) {
			log_.debug("No request handler definitions found");
			return;
		}
		NodeList handlerNodes = XPathHelper.selectList(path, node, "handler");
		for (int i=0; i < handlerNodes.getLength(); i++) {
			Node handlerNode = handlerNodes.item(i);
			RequestHandlerConfigurationImpl config = null;
			try {
				config = new RequestHandlerConfigurationImpl();
				config.init(path, handlerNode);
				RequestHandler h = RequestHandlerManager.loadAndConfigure(config, classLoader);
				handlers.put(config.getIdentifier(), h);
			} catch (ConfigurationException e) {
				log_.debug("Error configuring handler!", e.getReason());
				log_.error("Error configuring handler '" + config.getIdentifier() + "': " + e.getMessage());
			}
		}
		log_.debug("Request handlers configured");
	}
	
	/**
	 * Configures the servlet mapping for this site.
	 * 
	 * @param node the site node
	 * @param path the XPath object used to parse the configuration
	 */
	private void readServlets(XPath path, Node node) {
		if (node == null) {
			log_.debug("No servlet definitions found");
			return;
		}
		log_.debug("Configuring servlet mappings");

		// Define the handler in any case, since modules may rely on its presence
		servletHandler = new ServletRequestHandler();
		handlers.put(ServletRequestHandler.ID, servletHandler);

		NodeList servletNodes = XPathHelper.selectList(path, node, "servlets/servlet");
		for (int i=0; i < servletNodes.getLength(); i++) {
			Node servlet = servletNodes.item(i);
			try {
				servletHandler.addServlet(new ServletConfiguration(path, servlet), classLoader);
			} catch (ConfigurationException e) {
				log_.error("Error reading servlet configuration in module '" + this + "'");
			}
		}
		NodeList mappingNodes= XPathHelper.selectList(path, node, "servlet-mappings/servlet-mapping");
		for (int i=0; i < mappingNodes.getLength(); i++) {
			Node mapping = mappingNodes.item(i);
			try {
				servletHandler.addServletMapping(new ServletMapping(path, mapping));
			} catch (ConfigurationException e) {
				log_.error("Error reading servlet configuration in module '" + this + "'");
			}
		}
	}

}