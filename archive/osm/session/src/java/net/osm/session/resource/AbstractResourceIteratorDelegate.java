/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 09/04/2001
 */

package net.osm.session.resource;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.Session.AbstractResourceIteratorOperations;
import org.omg.CosPersistentState.StorageObject;

import net.osm.list.Iterator;
import net.osm.list.LinkedList;
import net.osm.session.linkage.LinkStorageIteratorDelegate;

/**
 * Implementation of the AbstractResourceIterator interface.
 */
public class AbstractResourceIteratorDelegate extends LinkStorageIteratorDelegate implements AbstractResourceIteratorOperations
{

   /**
    * AbstractResourceIteratorDelegate constructor.
    * @param iterator of a list of links
    */
    public AbstractResourceIteratorDelegate( Iterator iterator )
    {
        this( iterator, null );
    }

   /**
    * Creation of a new AbstractResourceIteratorDelegate.
    * @param iterator from the underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public AbstractResourceIteratorDelegate( Iterator iterator, TypeCode type )
    {
	  super( iterator, type );
    }

   /**
    * Creation of a new AbstractResourceIteratorDelegate.
    * @param list underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public AbstractResourceIteratorDelegate( LinkedList list, TypeCode type )
    {
	  super( list, type );
    }

   /**
    * Returns true if the supplied LinkStorage object is equivilent with 
    * the supplied type (this implementation applies the test to the 
    * resource contained within the link within the storage object).
    * @osm.warning type testing is absolute - does not respect IDL inhertance hierachy
    */
    public boolean evaluate( StorageObject s, TypeCode t )
    {
	  return true;
    }

}
