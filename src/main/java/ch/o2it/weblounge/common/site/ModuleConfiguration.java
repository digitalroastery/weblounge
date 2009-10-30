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

package ch.o2it.weblounge.common.site;

import ch.o2it.weblounge.common.Customizable;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.renderer.Renderer;

import java.util.Collection;

/**
 * This interface defines the fields and methods for a module configuration. The
 * default implementation is;@link ch.o2it.weblounge.core.module.ModuleConfigurationImpl}.
 * 
 * @author Tobias Wunden
 * @version 1.0
 * @since Weblounge 2.0
 */
public interface ModuleConfiguration extends Customizable {

	/**
	 * Returns the module identifier, e. g. <tt>forum</tt>.
	 * 
	 * @return the module identifier
	 */
	String getIdentifier();

	/**
	 * Returns the module description, e. g. <tt>Forum</tt>.
	 * 
	 * @param l the language used to return the description
	 * @return the module description
	 */
	String getDescription(Language l);

	/**
	 * Returns <code>true</code> if the module is configured to be enabled.
	 * 
	 * @return <code>true</code> if the module is enabled
	 */
	boolean isEnabled();
	
	/**
	 * Returns the module's document base.
	 * 
	 * @return the module's document base
	 */
	String getPath();
	
	/**
	 * Returns the module load factor. The factor denotes the load that is expected
	 * to be put on the module.
	 * 
	 * @return the module load factor
	 */
	int getLoadFactor();

	/**
	 * Returns the renderers that are defined for the module.
	 * 
	 * @return the module renderer
	 */
	Collection<Renderer> getRenderers();

	/**
	 * Returns the action registry containing all registered actions.
	 * 
	 * @return the actions
	 */
	Collection<Action> getActions();

	/**
	 * Returns the image style registry containing all registered styles.
	 * 
	 * @return the image styles
	 */
	Collection<ImageStyle> getImageStyles();

	/**
	 * Returns the module jobs.
	 * 
	 * @return the module jobs
	 */
	Collection<Job> getJobs();

}