/*
 * @(#)AbstractResourceIteratorDelegate.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 09/04/2001
 */

package net.osm.hub.resource;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.Session.AbstractResourceIteratorOperations;
import org.omg.CosPersistentState.StorageObject;
import net.osm.hub.resource.LinkStorageIteratorDelegate;
import net.osm.list.Iterator;
import net.osm.list.LinkedList;

/**
 * Implementation of the AbstractResourceIterator interface.
 */

public class AbstractResourceIteratorDelegate extends LinkStorageIteratorDelegate implements AbstractResourceIteratorOperations
{

   /**
    * AbstractResourceIteratorDelegate constructor.
    * @param orb
    * @param iterator of a list of links
    */

    public AbstractResourceIteratorDelegate( ORB orb, Iterator iterator )
    {
        this( orb, iterator, null );
    }

   /**
    * Creation of a new AbstractResourceIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param iterator from the underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public AbstractResourceIteratorDelegate( ORB orb, Iterator iterator, TypeCode type )
    {
	  super( orb, iterator, type );
    }

   /**
    * Creation of a new AbstractResourceIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param list underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public AbstractResourceIteratorDelegate( ORB orb, LinkedList list, TypeCode type )
    {
	  super( orb, list, type );
    }
    
}
