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
 * A <code>Feature</code> is a name value pair container.
 */
public interface Feature
{

   /**
    * Returns the name of the featue.
    * @return String the feature name
    */
    public String getName();
    
   /**
    * Returns the feature value.
    * @return Object the feature value
    */
    public Object getValue();

}
