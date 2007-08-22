/*
 * @(#)SystemMessageIteratorDelegate.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 03/06/2001
 */

package net.osm.hub.resource;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInvalidReason;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosPersistentState.StorageObject;
import org.omg.Session.SystemMessage;
import org.omg.Session.SystemMessageBase;
import org.omg.Session.SystemMessageHelper;
import org.omg.Session.SystemMessageIteratorOperations;
import net.osm.hub.gateway.AbstractIteratorDelegate;
import net.osm.hub.pss.MessageStorage;
import net.osm.list.NoEntry;
import net.osm.list.Iterator;
import net.osm.list.List;
import net.osm.list.LinkedList;

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

    public SystemMessageIteratorDelegate( ORB orb, Iterator iterator )
    {
        this( orb, iterator, null );
    }

   /**
    * Creation of a new SystemMessageIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param iterator from the underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public SystemMessageIteratorDelegate( ORB orb, Iterator iterator, TypeCode type )
    {
	  super( orb, iterator, type );
    }

   /**
    * Creation of a new SystemMessageIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param list underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public SystemMessageIteratorDelegate( ORB orb, LinkedList list, TypeCode type )
    {
	  super( orb, list, type );
    }

   /**
    * Retrieves the current element and returns it via the output parameter element.
    * The iterator must point to an element of the collection; otherwise, the exception
    * IteratorInvalid or IteratorInBetween is raised.
    * @return  true if an element was retrieved.
    */

    public boolean retrieve_element(org.omg.CORBA.AnyHolder element)
    throws IteratorInvalid, IteratorInBetween
    {
	  if( element == null ) throw new RuntimeException( "null value supplied to retrieve_element");
	  try
        {
	      Any any = orb.create_any();
	      if( store != null )
            {
		    MessageStorage ms = (MessageStorage) store;
		    SystemMessage message = ms.message();
		    SystemMessageHelper.insert( any, message );
		    element.value = any;
		    return true;
            }
            else
	      {
	          element.value = any;
	          return false;
            }
        }
        catch(Throwable e)
        {
		String error = "unexpected exception in retrieve_element";
		System.err.println( error );
            e.printStackTrace();
		throw new IteratorInvalid( error, IteratorInvalidReason.is_invalid );
        }
    }

   /**
    * Returns true if the supplied LinkStorage object is equivilent with 
    * the supplied type.
    */

    public boolean evaluate( StorageObject s, TypeCode t )
    {
	  return true;
    }

}

