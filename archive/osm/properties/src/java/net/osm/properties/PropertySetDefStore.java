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

package net.osm.properties;

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

import net.osm.properties.PropertySetDefStorage;
import net.osm.properties.PropertySetDefStorageBase;
import net.osm.properties.PropertyStorage;
import net.osm.properties.PropertyStorageHome;

/**
 * PropertySetDef is the PSS implementation of the storage object 
 * for the persistant management of properties exposed by the 
 * OMG CosPropertySetDef interface. 
 */
public class PropertySetDefStore extends PropertySetDefStorageBase
{

    private PropertyStorageHome m_home;

    //============================================
    // PropertySetDefStorage implementation
    //============================================

   /**
    * Return a PropertyDef given the supplied name.
    */
    public PropertyDef get( String name ) throws NotFound
    {
	  PropertyStorage p = home().find_by_property_key( get_short_pid(), name );
	  return new PropertyDef( p.name(), p.value(), p.mode() );
    }

   /**
    * Adds a PropertyStorage instance to the set of properties
    * managed by this PropertSetDefStorage instance.
    * @param scope the short PID of the storage object containing the 
    *   new <code>PropertySetDef</code>
    * @param name the name of the new property
    * @param value the property value
    * @param mode the property mode 
    */
    public void put( String name, Any value, PropertyModeType mode)
    {
        PropertyStorage p = null;
        try
        {
            p = home().find_by_property_key( get_short_pid(), name );
            p.value( value );
            return;
        }
        catch( NotFound e )
        {
            try
            {
	          p = home().create( get_pid(), name, value, mode );
                values( add( values(), p.get_short_pid() ));
            }
            catch( NotFound nf )
            {
                throw new RuntimeException("Unable to create property.", nf );
            }
        }
    }

   /**
    * Returns the size of the property set.
    * @return in the size of the property set
    */
    public int size()
    {
        return values().length;
    }

   /**
    * Returns the sequence of property names.
    * @return String[] a sequence of property names
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
		throw new RuntimeException( s );
        }
    }

   /**
    * Returns true if the property set contains the supplied name.
    * @param scope the container property set storage objet identifer
    * @param name the property name
    * @return boolean TRUE if the supplied property name is available 
    *   with the supplied container
    */
    public boolean available( String name )
    {
	  try
	  {
	      get( name );
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
    public void delete( String name ) throws NotFound
    {
        // locate the named property storage, get its ID,
        // remove the id from the list of property IDs and
        // and destroy the property storage object

	  PropertyStorage p = home().find_by_property_key( get_short_pid(), name );
        values( remove( values(), p.get_short_pid() ) );
	  p.destroy_object();
    }

    //============================================
    // Utilities
    //============================================

    private byte[][] add( byte[][] array, byte[] value )
    {
        byte[][] result = new byte[ array.length + 1 ][];
        for( int i=0; i<array.length; i++ ) 
        {
            result[i] = array[i];
        }
        result[ array.length ] = value;
        return result;
    }

    private byte[][] remove( byte[][] array, byte[] value )
    {
        byte[][] result = new byte[ array.length - 1 ][];
        int j=0;
        for( int i=0; i<array.length; i++ ) 
        {
            try
            {
                if( !equivalent( array[i], value ) ) 
                {
                    result[j] = array[i];
                }
                j++;
            }
            catch( ArrayIndexOutOfBoundsException oobe )
            {
                System.out.println("entry does not exist");
                return array;
            }
        }
        return result;
    }

    private boolean equivalent( byte[] first, byte[] second )
    {
        if( first.length != second.length ) return false;
        for( int i=0; i<first.length; i++ )
        {
            if( first[i] != second[i] ) return false;
        }
        return true;
    }

   /**
    * Returns the PropertySet storage home.
    */
    
    private PropertyStorageHome home() throws NotFound
    {
        if( m_home != null ) return m_home;
        m_home = (PropertyStorageHome) get_storage_home().get_catalog().find_storage_home( 
          "PSDL:osm.net/properties/PropertyStorageHomeBase:1.0" );
        if( m_home == null ) throw new NotFound();
        return m_home;
    }

}
