/*
 * @(#)LinkStorageIteratorDelegate.java
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

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosPersistentState.StorageObject;
import org.omg.Session.WorkspaceHelper;
import org.omg.CommunityFramework.CommunityHelper;
import net.osm.list.NoEntry;
import net.osm.list.Iterator;
import net.osm.list.LinkedList;
import net.osm.hub.pss.LinkStorage;
import net.osm.hub.gateway.AbstractIteratorDelegate;


/**
 * The LinkStorageIteratorDelegate is the base implementation class for all 
 * link based iterators supporting the CosCollection iterator interface.  Due 
 * to the "heavy-weight" nature of the CosCollection iterator specification the 
 * implementation declares a noimplement exception for some operations. 
 * This is temporary pending the specification of a "smart iterator" model 
 * that takes advantage of a combination of valuetypes and abstract 
 * interfaces, enabling behind the scenes management of a stack of references
 * maintained by an iterator valuetype. This approach will allow local iteration 
 * over a subset of a remote collection and maintainence of a local stack 
 * under a background thread.
 */

public class LinkStorageIteratorDelegate extends AbstractIteratorDelegate
{

   /**
    * LinkStorageIteratorDelegate constructor.
    * @param orb
    * @param iterator of a list of links
    */

    public LinkStorageIteratorDelegate( ORB orb, Iterator iterator )
    {
        this( orb, iterator, null );
    }

   /**
    * Creation of a new LinkStorageIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param iterator from the underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public LinkStorageIteratorDelegate( ORB orb, Iterator iterator, TypeCode type )
    {
	  super( orb, iterator, type );
    }

   /**
    * Creation of a new LinkStorageIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param list underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public LinkStorageIteratorDelegate( ORB orb, LinkedList list, TypeCode type )
    {
	  super( orb, list, type );
    }

    //=========================================================================
    // CosCollections::Iterator abstract method override
    //=========================================================================

   /**
    * Retrieves the current element and returns it via the output parameter element.
    * The iterator must point to an element of the collection; otherwise, the exception
    * IteratorInvalid or IteratorInBetween is raised.
    * @return  true if an element was retrieved.
    */
    public boolean retrieve_element(org.omg.CORBA.AnyHolder element)
    throws IteratorInvalid, IteratorInBetween
    {
	  try
	  {
	      Any any = orb.create_any();
	      if( store != null )
            {
		    LinkStorage ls = (LinkStorage) store;
		    any.insert_Object( ls.link().resource()._duplicate(), ls.link().resource().resourceKind() );
		    element.value = any;
		    return true;
            }
            else
	      {
	          element.value = any;
	          return false;
            }
        }
        catch( Exception e )
        {
	      throw new RuntimeException("Unexpected exception while retrieving element.", e );
        }
    }

   /**
    * Returns true if the supplied LinkStorage object is equivilent with 
    * the supplied type (this implementation applies the test to the 
    * resource contained within the link within the storage object).
    * @osm.warning type testing is absolut - does not respect IDL inhertance hierachy
    */
    public boolean evaluate( StorageObject s, TypeCode t )
    {
	  if( t == null ) return true;
	  try
	  {
		TypeCode source = ((LinkStorage)s).link().resource().resourceKind();
		if( t.equivalent( WorkspaceHelper.type() ) )
		{
		    if( source.equivalent( CommunityHelper.type() )) return true;
		}
		return t.equivalent( source );
	  }
	  catch( Exception e )
	  {
	      return false;
        }
    }
}
