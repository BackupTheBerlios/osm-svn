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
import org.omg.CosPersistentState.NotFound;


/**
 * Iterator provides support for the iteration over elements of a List.
 */
public class IteratorImpl extends LocalObject
implements net.osm.list.Iterator
{

    private EntryStorageHome home;
    private boolean m_trace = false;
    private List list;
    
    private EntryStorage last;
    private EntryStorage next;

    public IteratorImpl( List list, EntryStorageHome home )
    {
        this.list = list;
        this.home = home;
        reset();
    }
    
   /**
    * Reset the iterator to the first entry in the list.
    */
    public void reset()
    {
        this.last = null;
        this.next = list.header();
    }

   /**
    * Returns true if the iterator can return another reference.
    * @return boolean true if a next entry is available
    */
    public synchronized boolean has_next()
    {
        return (next != null );
    }
    
   /**
    * Returns the next element without incrementing the iterator.
    * @return StorageObject the storage object following the current
    *   storage object that the iterator is pointing to.
    * @exception NoEntry if no further entries exist in the list
    */
    public synchronized StorageObject peek()
    throws NoEntry
    {
        if( next != null ) return next.target();
        throw new NoEntry();
    }

   /**
    * Returns the next StorageObject in the list and advances the iterator
    * one step foward in the iteration.
    * @return StorageObject the next storage object in the list
    */
    public synchronized StorageObject next()
    throws NoEntry
    {
        if( !has_next() ) throw new NoEntry();
        last = next;
        next = last.next();
        if( last.target() != null ) return last.target();
        throw new NoEntry();
    }
    
   /**
    * Removes the current entry referencing the last StorageObject returned from
    * the iteration.
    */
    
    public synchronized void remove()
    {
        if( last == null ) return;
        try
        {
            list.remove_entry( last );
        }
        catch( Throwable e )
        {
        }
        finally
        {
            last = null;
        }
    }
}
