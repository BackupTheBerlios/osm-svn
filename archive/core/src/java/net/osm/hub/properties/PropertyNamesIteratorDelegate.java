/*
 * @(#)PropertyNamesIteratorDelegate.java
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

import java.util.Vector;

import org.omg.CORBA.StringHolder;
import org.omg.CosPropertyService.PropertyNamesIteratorPOA;
import org.omg.CosPropertyService.PropertyNamesHolder;

/**
 * The PropertyNamesIterator interface allows a client to iterate through the names using
 * the next_one or next_n operations.
 * A PropertySet maintains a set of name-value pairs accessible through several operations, 
 * some of which returning a names iterator that is implementated by this class. The 
 * get_all_property_names operation of PropertySet returns a sequence of names 
 * (PropertyNames) and if there are additional names, the get_all_property_names operation 
 * returns an object (this class) supporting the PropertyNamesIterator interface with the 
 * additional names.
 */

public class PropertyNamesIteratorDelegate extends PropertyNamesIteratorPOA
{
	Vector vector;
	int index=0;
	
	/**
	 * Constructor
	 */
	public PropertyNamesIteratorDelegate(  Vector tab )
	{
		this.vector = new Vector();
		this.vector = (Vector)tab.clone();
		
	}

	/**
	 * The reset operation resets the position in an iterator to the first property name, if one
	 * exists.
	 */
	public void reset()
	{
		index=0;
	}

	/**
	 * The next_one operation returns true if an item exists at the current position in the
	 * iterator with an output parameter of a property name. A return of false signifies no
	 * more items in the iterator.
	 */
	public boolean next_one( StringHolder property_name )
	{
		property_name.value = new String();
		if( index == this.vector.size() )
			return false;
		
		property_name.value= (String) this.vector.elementAt(index++);
		return true;
	}

	/**
	 * The next_n operation returns true if an item exists at the current position in the iterator
	 * and the how_many parameter was set greater than zero. The output is a PropertyNames
	 * sequence with at most the how_many number of names. A return of false signifies no
	 * more items in the iterator.
	 */
	public boolean next_n(int how_many, PropertyNamesHolder property_names)
	{		
		StringHolder tmpHolder = new StringHolder();
		
		property_names.value = new String[ how_many ];
	
		for ( int i=0;i< how_many && next_one(tmpHolder)==true ;i++ )
		{
			property_names.value[i] = tmpHolder.value;
		}
		
		if ( index ==this.vector.size() )
			return false;
		
		return true;		
	}

	/**
	 * The destroy operation destroys the iterator.
	 */ 	
	public void destroy()
	{
		this.vector=null;
		
		try
		{
			_poa().deactivate_object( _object_id() );
		}
		catch ( Exception ex )
		{ 
			ex.printStackTrace();	
		}
	}
}
