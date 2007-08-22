/*
 * @(#)CompositeValue.java
 *
 * Copyright 2000 OSM S.A.R.L. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM S.A.R.L.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 29/07/2000
 */


package net.osm.discovery;

import net.osm.discovery.Key;
import net.osm.discovery.Feature;
import net.osm.discovery.Entry;



/**
Interface for any class containing a ChainBase instance.
*/

public interface CompositeValue 
{

   /**
    * Return the number of named values contained within this container.
    */

    public int size( );

   /**
    * Return the container of named values managed by this instance.
    */

    public ChainBase value( );

   /**
    * Return an instance of a named value.
    */

    public Entry lookup( String name ) throws KeywordNotFound;

   /**
    * Create a new Key as a child of this key.  
    */

    public KeyBase newKey( String name );

   /**
    * Create a new Feature as a child of this key using the supplied
    * name and value.  
    */

    public FeatureBase newFeature( String name, String value );

   /**
    * Add an existing key to this filter.
    */

    public void addKey( Key key );

   /**
    * Add an existing feature to this filter.
    */

    public void addFeature( Feature f );

} // CompositeValue
