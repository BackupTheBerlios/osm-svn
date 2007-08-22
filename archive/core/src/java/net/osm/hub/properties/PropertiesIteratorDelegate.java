/*
 * @(#)PropertiesIteratorDelegate.java
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

import org.omg.CosPropertyService.PropertiesIteratorPOA;
import org.omg.CosPropertyService.PropertyHolder;
import org.omg.CosPropertyService.PropertiesHolder;
import org.omg.CosPropertyService.Property;

/**
 * The PropertiesIteratorDelegate implements the PropertiesIterator interface and 
 * allows a client to iterate through the name-value pairs using the next_one or 
 * next_n operations.
 * A PropertySet maintains a set of name-value pairs. The get_all_properties
 * operation of the PropertySet interface returns a sequence of Property structures
 * (Properties). If there are additional properties, the get_all_properties operation
 * returns an object supporting the PropertiesIterator interface with the additional
 * properties.
 */

public class PropertiesIteratorDelegate extends PropertiesIteratorPOA
{

	Vector vector;
	int index = 0;
	
	public PropertiesIteratorDelegate(  Vector tab )
	{
		this.vector = new Vector();
		this.vector = (Vector)tab.clone();
	}
	
	/**
	 * The reset operation resets the position in an iterator to the first
	 * property, if one exists.
	 */
	public void reset()
	{
		index = 0;
	}
	
	/**
	 * The next_one operation returns true if an item exists at the current position in the
	 * iterator with an output parameter of a property. A return of false signifies no more
	 * items in the iterator.
	 */ 
	public boolean next_one(PropertyHolder holder)
	{
		holder.value = new Property("", _orb().create_any());
		if( index == this.vector.size() ) return false;
		holder.value= ( Property )this.vector.elementAt(index++);
		return true;
	}

	/**
	 * The next_n operation returns true if an item exists at the current position in the iterator
	 * and the how_many parameter was set greater than zero. The output is a properties
	 * sequence with at most the how_many number of properties. A return of false signifies
	 * no more items in the iterator.
	 */
	public boolean next_n(int how_many, PropertiesHolder nproperties)
	{
		PropertyHolder tmpHolder = new   PropertyHolder();
		nproperties.value = new Property[ how_many ];
		for ( int i=0; i< how_many && next_one(tmpHolder)==true ;i++ )
		{
			nproperties.value[i] = tmpHolder.value;
		}
		if ( index ==this.vector.size() ) return false;
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
		catch ( java.lang.Exception ex )
		{ 
			ex.printStackTrace();	
		}
	}
}
