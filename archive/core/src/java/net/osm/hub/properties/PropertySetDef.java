/*
 * @(#)PropertySetDef.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 03/04/2001
 */

package net.osm.hub.properties;

import java.util.List;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CosPropertyService.PropertySetDefPOA;
import org.omg.CosPropertyService.PropertyDef;
import org.omg.CosPropertyService.ConstraintNotSupported;
import org.omg.CosPropertyService.MultipleExceptions;
import org.omg.CosPropertyService.ConflictingProperty;
import org.omg.CosPropertyService.UnsupportedTypeCode;
import org.omg.CosPropertyService.UnsupportedProperty;
import org.omg.CosPropertyService.UnsupportedMode;
import org.omg.CosPropertyService.ReadOnlyProperty;
import org.omg.CosPropertyService.InvalidPropertyName;
import org.omg.CosPropertyService.PropertyNotFound;
import org.omg.CosPropertyService.FixedProperty;
import org.omg.CosPropertyService.PropertyException;
import org.omg.CosPropertyService.ExceptionReason;
import org.omg.CosPropertyService.Property;
import org.omg.CosPropertyService.PropertyMode;
import org.omg.CosPropertyService.PropertyModeType;
import org.omg.CosPropertyService.PropertyModesHolder;
import org.omg.CosPropertyService.PropertyTypesHolder;
import org.omg.CosPropertyService.PropertyNamesHolder;
import org.omg.CosPropertyService.PropertiesIterator;
import org.omg.CosPropertyService.PropertiesIteratorHolder;
import org.omg.CosPropertyService.PropertiesHolder;
import org.omg.CosPropertyService.PropertyNamesIteratorHolder;
import org.omg.CosPropertyService.PropertyNamesIterator;
import org.omg.CosPropertyService.PropertyDefsHolder;
import org.omg.CosPropertyService.PropertyDefHolder;
import org.omg.CosPersistentState.NotFound;

import net.osm.properties.pss.PropertySetDefStorage;
import net.osm.properties.pss.PropertySetDefStorageBase;
import net.osm.properties.pss.PropertyStorage;
import net.osm.properties.pss.PropertyStorageHome;

/**
 * PropertySetDef is the PSS implementation of the storage object 
 * for the persistant management of properties exposed by the 
 * OMG CosPropertySetDef interface. 
 */
public class PropertySetDef extends PropertySetDefStorageBase
{

    private PropertyStorageHome home;

        
    //============================================
    // Constructors
    //============================================

    public PropertySetDef( )
    {
    }
    
    //============================================
    // PropertySetDefStorage implementation
    //============================================

   /**
    * Return a PropertyDef given the supplied name.
    */
    public PropertyDef get( byte[] scope, String name ) throws NotFound
    {
	  PropertyStorage p = home().find_by_property_key( scope, name );
	  return new PropertyDef( p.name(), p.value(), p.mode() );
    }

   /**
    * Adds a PropertyStorage instance to the set of properties
    * managed by this PropertSetDefStorage instance.
    */
    public void put( byte[] scope, String name, Any value, PropertyModeType mode)
    {
	  int n = size();

        PropertyStorage p = null;
        try
        {
	      p = home().create( scope, name, value, mode );
        }
        catch( NotFound nf )
        {
            throw new RuntimeException("Unable to resolve storage home.", nf );
        }

	  byte[][] array = new byte[ n + 1 ][];
	  for( int i=0; i < n; i++ )
	  {
		array[i] = values()[i];
	  }
	  array[n] = p.get_short_pid();
	  values( array );
    }

   /**
    * Returns the size of the property set.
    */
    public int size()
    {
        return values().length;
    }

   /**
    * Returns the sequence of property names.
    */
    public String[] names()
    {
	  String[] names = new String[ size() ];
	  try
        {
	      for( int i=0; i< size(); i++ ) 
            {
		    names[i] = ((PropertyStorage)home().find_by_short_pid( values()[i] )).name();
		}
	      return names;
	  }
        catch( Exception e )
	  {
		String s = "unexpected exception while resolving a property name";
		throw new RuntimeException( s, e );
        }
    }

   /**
    * Returns true if the property set contains the supplied name.
    */
    public boolean available( byte[] scope, String name )
    {
	  try
	  {
	      get( scope, name );
	      return true;
	  }
	  catch( Exception e )
	  {
	      return false;
	  }
    }

   /**
    * Deletes a named property.
    */
    public void delete( byte[] scope, String name) throws NotFound
    {
        // locate the named property storage, get its ID,
        // remove the id from the list of property IDs and
        // and destroy the property storage object

	  PropertyStorage p = home().find_by_property_key( scope, name );

	  int n = size();
	  byte[][] array = new byte[ n - 1 ][];
	  byte[] pid = p.get_short_pid();
	  int j = 0;
	  for( int i=0; i < n; i++ )
	  {
	      if( values()[i] != pid ) 
		{
		    array[j] = values()[i];
		    j++;
		}
	  }
	  values( array );
	  p.destroy_object();
    }

    //============================================
    // Utilities
    //============================================

   /**
    * Returns the PropertySet storage home.
    */
    
    private PropertyStorageHome home() throws NotFound
    {
        if( home == null )
        {
            home = (PropertyStorageHome) get_storage_home().get_catalog().find_storage_home(
 		      "PSDL:osm.net/properties/pss/PropertyStorageHomeBase:1.0" );
		if( home == null ) throw new NotFound();
        }
        return home;
    }

    public void destroy_object()
    {
        home = null;
        super.destroy_object();
    }

}
