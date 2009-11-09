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

package ch.o2it.weblounge.common.impl.page;

import ch.o2it.weblounge.common.page.PageURI;
import ch.o2it.weblounge.common.page.PageletLocation;
import ch.o2it.weblounge.common.site.Site;

/**
 * This class describes a pagelet's position with respect to its url, composer
 * and position within this composer.
 */
public class PageletLocationImpl implements PageletLocation {

	/** The pagelet's url */
	private PageURI uri_;
	
	/** The pagelet's composer */
	private String composer_;
	
	/** The pagelet's position within the specified composer */
	private int position_;
	
	/**
	 * Creates a new <code>PageletLocationImpl</code> with the given url,
	 * comoser and position.
	 *
	 *@param uri the pagelet's url
	 *@param composer the pagelet's composer
	 *@param position the position within the composer
	 */
  public PageletLocationImpl(PageURI uri, String composer, int position) {
		uri_ = uri;
		composer_ = composer;
		position_ = position;
	}
	
  /**
   * {@inheritDoc}
   * @see ch.o2it.weblounge.common.page.PageletLocation#getSite()
   */
	public Site getSite() {
		return uri_.getSite();
	}

	/**
	 * {@inheritDoc}
	 * @see ch.o2it.weblounge.common.page.PageletLocation#getURI()
	 */
	public PageURI getURI() {
		return uri_;
	}

	/**
	 * {@inheritDoc}
	 * @see ch.o2it.weblounge.common.page.PageletLocation#getComposer()
	 */
	public String getComposer() {
		return composer_;
	}

	/**
	 * {@inheritDoc}
	 * @see ch.o2it.weblounge.common.page.PageletLocation#getPosition()
	 */
	public int getPosition() {
		return position_;
	}
	
	void setURI(PageURI uri) {
		uri_ = uri;
	}
	
	void setComposer(String composer) {
		composer_ = composer;
	}
	
	void setPosition(int position) {
		position_ = position;
	}
	
	/**
	 * Returns the location's hash code.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (uri_ != null && composer_ != null)
			return uri_.hashCode() | (composer_.hashCode() >> 8) | (position_ >> 8);
		else
			return super.hashCode();
	}

	/**
	 * Returns <code>true</code> if <code>o</code> equals this pagelet
	 * location.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof PageletLocation) {
			PageletLocation l = (PageletLocation)o;
			if (l.getURI() != null && l.getComposer() != null)
				return l.getURI().equals(uri_) && l.getComposer().equals(composer_) && l.getPosition() == position_;
		}
		return false;
	}

  /**
   * {@inheritDoc}
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(PageletLocation l) {
    if (!uri_.equals(l.getURI()) || !composer_.equals(l.getComposer()))
      return 0;
    return Integer.valueOf(position_).compareTo(Integer.valueOf(l.getPosition()));
  }
	
}