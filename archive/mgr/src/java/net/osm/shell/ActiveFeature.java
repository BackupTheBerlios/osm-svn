/*
 * @(#)ActiveFeature.java
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

import java.lang.reflect.InvocationTargetException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.reflect.Method;

/**
 * A <code>ActiveFeature</code> is a name value pair container that 
 * exposed the name of a property event that can be used to monitor the 
 * feature value.
 */

public class ActiveFeature implements Feature
{

    //=========================================================================
    // state
    //=========================================================================

   /**
    * The entity holding the feature.
    */
    private Entity entity;

   /**
    * Name of the feature.
    */
    private String name;

   /**
    * The name of the property to monitor.
    */
    String property;

   /**
    * The name of the operation to invoke.
    */
    String operation;

   /**
    * The method to invoke.
    */
    Method method;

    //========================================================================
    // Constructor
    //========================================================================
    
   /**
    * The <code>ActiveFeature</code> class supports association of a 
    * named value pair and exposed a property event name that can be used 
    * to monitor feature value changes.
    *
    * @param entity the entity holding the feature
    * @param name the name of the feature
    * @param operation the feature accessor
    * @param property the name of the property change event associated with the feature
    */
    public ActiveFeature( Entity entity, String name, String operation, String property )
    {
	  this.name = name;
        this.entity = entity;
        this.operation = operation;
        this.property = property;
        try
	  {
            method = entity.getClass().getMethod( operation, new Class[0] );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException("ActiveFeature, Argument error.", e );
	  }
    }

    //========================================================================
    // Feature
    //========================================================================

    public String getName()
    {
	  return this.name;
    }
    
   /**
    * Invokes a method on an object based on a supplied target object and a keyword.  The 
    * implementation prepends the keyword with the 'get' string, and capatilizes the first
    * character of the keyword (as per the Java Beans convention).
    */
    public Object getValue()
    {
        try
	  {
            return method.invoke( entity, new Object[0] );
	  }
	  catch( Exception e )
	  {
		throw new RuntimeException("ActiveFeature, unexpected invocation problem.", e );
	  }
    }

    //==========================================================
    // ActiveFeature
    //==========================================================

   /**
    * Return the property name for this feature.
    */
    public String getPropertyName( )
    {
        return property;
    }
}
