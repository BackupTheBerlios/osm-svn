/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 03/06/2001
 */

package net.osm.session.message;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInvalidReason;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosPersistentState.StorageObject;

import net.osm.list.AbstractIteratorDelegate;
import net.osm.list.NoEntry;
import net.osm.list.Iterator;
import net.osm.list.LinkedList;
import net.osm.list.List;

/**
 * Implementation of the SystemMessageIterator interface.
 */
public class SystemMessageIteratorDelegate extends AbstractIteratorDelegate implements SystemMessageIteratorOperations
{

   /**
    * SystemMessageIteratorDelegate constructor.
    * @param orb
    * @param iterator of a list of links
    */

    public SystemMessageIteratorDelegate( Iterator iterator )
    {
        this( iterator, null );
    }

   /**
    * Creation of a new SystemMessageIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param iterator from the underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public SystemMessageIteratorDelegate( Iterator iterator, TypeCode type )
    {
	  super( iterator, type );
    }

   /**
    * Creation of a new SystemMessageIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param list underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public SystemMessageIteratorDelegate( LinkedList list, TypeCode type )
    {
	  super( list, type );
    }

   /**
    * Retrieves the current element and returns it via the output parameter element.
    * The iterator must point to an element of the collection; otherwise, the exception
    * IteratorInvalid or IteratorInBetween is raised.
    * @return  true if an element was retrieved.
    * @exception IteratorInvalid
    * @exception IteratorInBetween
    */
    public boolean retrieve_element(org.omg.CORBA.AnyHolder element)
    throws IteratorInvalid, IteratorInBetween
    {
	  try
        {
	      Any any = ORB.init().create_any();
	      if( store != null )
            {
		    MessageStorage ms = (MessageStorage) store;
		    SystemMessageHelper.insert( any, ms.system_message() );
		    element.value = any;
		    return true;
            }
            else
	      {
	          element.value = any;
	          return false;
            }
        }
        catch( Throwable e)
        {
	      throw new RuntimeException("Unexpected exception while retrieving element.", e );
        }
    }

   /**
    * Returns true if the supplied StorageObject object is equivilent with 
    * the supplied type. Current implemenmtation is empty (always returns true).
    */
    public boolean evaluate( StorageObject s, TypeCode t )
    {
	  return true;
    }

}

