/*
 * @(#)IteratorImpl.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/03/2001
 */

package net.osm.list;

import org.omg.CORBA.LocalObject;
import org.omg.CosPersistentState.StorageHomeBase;
import org.omg.CosPersistentState.StorageObject;


/**
 * Iterator is a simple iterator of a storage type
 * instances maintained under a linked list.
 */

public class IteratorImpl extends LocalObject
implements net.osm.list.Iterator
{
    
    private EntryStorage last;
    private StorageObject target;
    private EntryStorageHome home;
    private List list;
    
    public IteratorImpl( List list, EntryStorageHome home )
    {
        this.list = list;
        this.home = home;
    }
    

   /**
    * Reset the iterator to the first entry in the list.
    */

    public void reset()
    {
        this.last = null;
        this.target = null;
    }

   /**
    * Returns the last element that the iterator is referencing.
    */

    public StorageObject element()
    {
	  if( last != null ) return last.target();
        return null;
    }

   /**
    * Returns true if the iterator can return another reference.
    */
    
    public synchronized boolean has_next()
    {
        synchronized( list )
        {
            if( null == last )
            {
                // this is the first call to has_next
                return ( list.header() != null );
            }
            else
            {
                return (last.next() != null);
            }
        }
    }
    
   /**
    * Returns the next element without incrementing the iterator.
    */
    
    public synchronized StorageObject peek()
    throws NoEntry
    {
	  if( !has_next() ) throw new NoEntry();
        synchronized( list )
        {
            if( null == last )
            {
                return ( list.header().target());
            }
            else
            {
                return (last.next().target());
            }
        }
    }

   /**
    * Returns the next StorageObject in the list.
    */
    
    public synchronized StorageObject next()
    throws NoEntry
    {
        synchronized( list )
        {
            if( null == last )
            {
                last = list.header();
            }
            else
            {
                last = last.next();
            }
            if( last != null ) return last.target();
            throw new NoEntry();
        }
    }
    
   /**
    * Removes the current entry referencing the last StorageObject returned from
    * the iteration.
    */
    
    public synchronized void remove()
    throws NoEntry
    {
        synchronized( list )
        {
            if( null == last ) throw new NoEntry();

		synchronized( last )
		{
                if( last.equals( list.header() ) )
                {
                    try
                    {
                        list.remove_entry( last );
                        last = null;
                    } 
			  catch (Throwable e)
                    {
                        throw new RuntimeException("Failed to remove current iterator head entry.", e );
                    }
                }
                else
                {
                    try
                    {
                        EntryStorage prev = last.prev();
                        list.remove_entry( last );
                        last = prev;
                    } 
                    catch (Throwable e)
                    {
                        throw new RuntimeException("Failed to remove current iterator entry.", e );
                    }
                }
		}
        }
    }
}
