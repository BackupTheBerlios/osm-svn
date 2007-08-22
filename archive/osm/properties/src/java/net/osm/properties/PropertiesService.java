/*
 * @(#)PropertiesService.java
 *
 * Copyright 2002 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 16/03/2002
 */

package net.osm.properties;

import org.omg.CosPropertyService.PropertySetDef;
import org.apache.avalon.framework.component.Component;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.NotFound;

/**
 * The <code>PropertiesService</code> is curently an empty service 
 * interface declaration until we know what we need.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public interface PropertiesService extends Component
{

    public static final String PROPERTY_SERVICE_KEY = "properties";
 
   /**
    * Creation of a new <code>PropertySetDefStorage</code> object.
    * @return PropertySetDefStorage the property set def storage object
    * @exception PropertyException
    */
    public PropertySetDefStorage createPropertySetDefStorage() 
    throws PropertyException;

   /**
    * Return an existing <code>PropertySetDefStorage</code> object given 
    * a short PID.
    * @param pid the property set def short pid
    * @return PropertySetDefStorage the property set def storage object
    * @exception NotFound
    */
    public PropertySetDefStorage getPropertySetDefStorage( byte[] pid ) 
    throws NotFound;

   /**
    * Locate a new <code>PropertySetDef</code> based on a supplied short PID.
    * @param pid property set short pid value
    * @exception NotFound if the property set does not exist
    */
    public PropertySetDef getPropertySetDefReference( byte[] pid  );

   /**
    * Create an object reference to a <code>PropertySetDef</code> using a 
    * supplied property set storage object.
    * @param store a protperty set def storage object
    */
    public PropertySetDef getPropertySetDefReference( PropertySetDefStorage store );

}
