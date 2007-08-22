
/*
 * @(#)Entry.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */

package net.osm.portal;

import net.osm.discovery.Entry;


/**
 * KeywordNotFound is an exception raises when an attempt is made to locate an 
 * object using a tag unknown.
 * 
 * @see net.osm.portal.FeatureBase
 * @see net.osm.portal.KeyBase
 * @see net.osm.portal.ChainBase
 */

public class KeywordNotFound extends Exception {

    public String keyword;
    public Entry source;

    public KeywordNotFound( Entry source, String keyword, String message) {
	super(message);
	this.keyword = keyword;
	this.source = source;
    }

    public KeywordNotFound( Entry source, String keyword ) {
	this( source, keyword, "Keyword '" + keyword + "' not found." );
    }

    public KeywordNotFound( String message) {
	this( null, null, "Keyword not found." );
    }

    public KeywordNotFound( ) {
	super( );
    }

}


