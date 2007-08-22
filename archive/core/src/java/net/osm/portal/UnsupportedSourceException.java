/*
 * @(#)UnsupportedSourceException.java
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

/**
 * @author  Stephen McConnell
 * @version 1.01 29/05/1999
 */

public class UnsupportedSourceException extends Exception {

    public UnsupportedSourceException( String message) {
	super(message);
    }

    public UnsupportedSourceException( ) {
	super( );
    }

}
