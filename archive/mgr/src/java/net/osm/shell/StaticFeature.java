/*
 * @(#)Feature.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 *  
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.shell;

/**
 * A <code>Feature</code> is a name value painr container.
 */

public class StaticFeature implements Feature
{

    private String name;
    protected Object value;

    //========================================================================
    // Constructor
    //========================================================================
    
   /**
    * The <code>StaticFeature</code> class supports association of a named value pair.
    * @param name the name of the feature
    * @param value the value of the feature 
    */
    public StaticFeature( String name, Object value ) 
    {
        this.name = name;
        this.value = value;
    }

    //========================================================================
    // Feature
    //========================================================================

    public String getName()
    {
	  return this.name;
    }
    
    public Object getValue()
    {
	  return this.value;
    }
}
