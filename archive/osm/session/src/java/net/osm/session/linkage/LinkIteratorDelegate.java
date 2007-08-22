/*
 * @(#)LinkIteratorDelegate.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 12/04/2001
 */

package net.osm.session.linkage;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.CORBA.ORB;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.Session.LinkIterator;
import org.omg.Session.LinkIteratorOperations;
import org.omg.Session.Link;
import org.omg.Session.LinkHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CosCollection.IteratorOperations;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosPersistentState.StorageObject;

import net.osm.list.AbstractIteratorDelegate;
import net.osm.list.NoEntry;
import net.osm.list.Iterator;
import net.osm.list.List;
import net.osm.list.LinkedList;

/**
 * Implementation of the LinkIterator interface.
 */

public class LinkIteratorDelegate extends AbstractIteratorDelegate implements LinkIteratorOperations
{

    protected ORB orb;

   /**
    * LinkIteratorDelegate constructor.
    * @param orb
    * @param iterator of a list of links
    */

    public LinkIteratorDelegate( ORB orb, Iterator iterator )
    {
        this( orb, iterator, null );
    }

   /**
    * Creation of a new LinkIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param iterator from the underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public LinkIteratorDelegate( ORB orb, Iterator iterator, TypeCode type )
    {
	  super( iterator, type );
        this.orb = orb;
    }

   /**
    * Creation of a new LinkIteratorDelegate.
    * @param orb current ORB used to create anys
    * @param list underlying persistent list
    * @param org.omg.CORBA.TypeCode type used as a filter (may be null)
    */
    public LinkIteratorDelegate( ORB orb, LinkedList list, TypeCode type )
    {
	  super( list, type );
        this.orb = orb;
    }

   /**
    * Retrieves the current element and returns it via the output parameter element.
    * The iterator must point to an element of the collection; otherwise, the exception
    * IteratorInvalid or IteratorInBetween is raised.
    * @return  true if an element was retrieved.
    */

    public boolean retrieve_element( AnyHolder element )
        throws IteratorInvalid, IteratorInBetween
    {
	  try
        {
	      Any any = ORB.init().create_any();
	      if( store != null )
            {
		    LinkStorage ls = (LinkStorage) store;
		    any.insert_Value( ls.link(), LinkHelper.type());
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
	      throw new CascadingRuntimeException("Unexpected exception while retrieving link.", e );
        }
    }

   /**
    * Returns true if the supplied LinkStorage object is 
    * equivilent with the supplied type (applies the test 
    * to the link contained within the storage object).
    */
    public boolean evaluate( StorageObject s, TypeCode t )
    {
	  if( type == null ) return true;
	  if( s == null ) throw new RuntimeException(
	    "LinkIterator. Null value supplied to link evaluation." );

	  try
        {
            Link link = ((LinkStorage)s).link();
	      String ID = getIDLIdentifier( link );
		
            ValueFactory factory = null;
		try
		{
                factory = ((org.omg.CORBA_2_3.ORB)orb).lookup_value_factory( ID );
		}
		catch( Exception fe )
		{
		    final String factoryError = 
			"unable to resolve a value factory for " 
			+ ID;
		    throw new RuntimeException( factoryError, fe );
		}
            return factory.getClass().isInstance( link );
        }
        catch( Exception e )
        {
	      final String error = "Unexpected exception while evaluating link.";
            throw new RuntimeException( error, e );
        }
    }

    private String getIDLIdentifier( Link link )
    {
	  try
        {
            return ((org.omg.CORBA.portable.Streamable)link)._type().id();
        }
        catch( Throwable e )
        {
	      throw new RuntimeException("LinkIterator. Unexpected exception while evaluating link.",e);
        }
    }
}
