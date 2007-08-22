/*
 * @(#)List.java
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

import org.omg.CosPersistentState.StorageHomeBase;
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.NotFound;

import net.osm.util.Incrementor;

/**
 * List is a storage type used to hold a storage type instance
 * derived from EntryStorage under the header state field.  The
 * header refers to the first storage instance in the linked list.
 */

public class List
extends LinkedListBase
{
    
    private static final String PSS_HOME = "PSDL:osm.net/list/EntryStorageHomeBase:1.0";
    private static final Incrementor inc = Incrementor.create("ENTRY");
    private EntryStorageHome home;
    
   /**
    * Adds a new entry to the end of the linked list.
    */
    
    public synchronized void add( StorageObject target )
    {
	  if( target == null ) throw new RuntimeException(
		"Illegal attempt to add a null storage object to a list.");

        EntryStorage entry = home().create( inc.increment(), target.get_pid(), null, null );
        if( entry == null )
	  {
		throw new RuntimeException(
	        "The EntryStorageHome return a null EntryStorage!");
	  }

        EntryStorageRef entryRef = entry.reference();
        if( entryRef == null )
	  {
		throw new RuntimeException(
	        "The EntryStorageHome return a null EntryStorageRef!");
	  }
        
        EntryStorage header = header();
        
        if( header != null ) synchronized( header )
        {
            // We have a list with at least one entry. Adding an entry involves updating the
            // last entry to include the new entry as its next entry and update the header to
            // reference the new entry as the header's prev entry (last entry).
            
            EntryStorage last = header.prev();
            if( last == null )
	      {
		    throw new RuntimeException(
	            "List. The EntryStorage header return a null prev entry!");
	      }
		else synchronized( last )
            {
                header.prev( entryRef );
                entry.prev( last.reference() );
                last.next( entryRef );
                size( size() + 1 );
            }
        }
        else
        {
            
            // This is an empty list so put a reference to the new EntryStorage 
            // in the list header and set the header prev slot to the same value.

            header( entryRef );
            header().prev( entryRef );
            size( 1 );
        }
    }
    
   /**
    * Requests removal of an entry with a target corresponding to the supplied
    * storage object.  The NotFound exception will be thrown if the target to be removed
    * has already been removed by another thead.
    * @param target the storage object to add to the list
    * @exception NullPointerException if the supplied target argument is null
    * @exception NotFound if the supplied storage object is not contained within this list
    */
    public synchronized void remove( StorageObject target )
    throws NotFound
    {
	  if( target == null ) throw new NullPointerException(
		"Illegal attempt to remove a null target from a list.");
 
        byte[] TID = target.get_pid();
        Iterator iterator = new IteratorImpl( this, home() );
        boolean result = false;
        while( iterator.has_next() )
        {
            try
            {
                if( iterator.next().equals( target ))
                {
                    iterator.remove();
                    return;
                }
            }
            catch( NoEntry ne )
            {
                // ignore
                System.out.println("Iterator.next() return NoEntry in List.remove()");
            }
            catch( Throwable e )
            {
                final String error = "Internal error while removing a list entry.";
                throw new RuntimeException( error, e );
            }
        }
        throw new NotFound();
    }
    
   /**
    * Returns an iterator supporting iteration over members of the list.
    */
    public Iterator iterator()
    {
        return new IteratorImpl( this, home() );
    }
    
   /**
    * Removes an entry from the linked list.
    */
    
    protected synchronized void remove_entry( EntryStorage entry )
    throws NotFound
    {

	  if( entry == null ) throw new NullPointerException(
		"Illegal attempt to remove a null entry from a list.");

        synchronized( entry )
        {
            EntryStorage header = header();

		// If the header is null then this is an empty list so its 
		// impossible to remove anything from it.
		
            if( header == null ) throw new NotFound();

		// Otherwise, we have a list with at least one entry. Check 
	      // if we are removing the header entry or a entry is in the 
		// body of the list.

            synchronized( header )
            {
                if( header.equals( entry ) )
                {
                    
                    // Client is requesting the removal of the header
			  // which means that either the next entry becomes the 
	              // header, or, if no next entry, the header is set to null.
                    
                    EntryStorage next = header.next();
                    if( next != null ) synchronized( next )
                    {
                        // There is a valid next entry in the current header
                        // which needs to assigned as the new header.
                            
                        EntryStorage last = header.prev();
			      if( last == null )
				{
		                throw new RuntimeException(
	                        "Unexpected null prev entry in list header.");
				}
                        synchronized( last )
                        {
                            next.prev( last.reference() );
                            header( next.reference() );
                            header().prev( last.reference() );
                            size( size() - 1 );
                        }
                    }
                    else
                    {
			      // The next entry is null and the header is being removed 
	                  // which means that this list is now empty which also means 
				// that we need to set the header to a null value.

                        EntryStorageRef nullRef = null;
                        header( nullRef );
                        size( 0 );
                    }
                }
                else
                {
                    // Client is requesting the removal of an entry somewhere in the
                    // list beyond the header position.  This means that there is
                    // valid prev entry to be updated, and possibly a next entry unless
                    // the entry being removed is the last entry in which case we need
                    // update the header as well.
                    
                    EntryStorage prev = entry.prev();
                    synchronized( prev )
                    {
                        EntryStorage next = entry.next();
                        
                        if( next == null )
                        {

				    // client is removing the last entry
				    // in the list which means that the prev entry
				    // becomes the new last entry

                            EntryStorageRef nullRef = null;
                            prev.next( nullRef );
				    header.prev( prev.reference() );
                        }
                        else
                        {
				    // this is a classic middle of the list update
				    // where the prev entries next element becomes 
	                      // the current element next element and the 
				    // next element prev element becomes the current
				    // elements next element

                            synchronized( next )
                            {
                                prev.next( next.reference() );
                                next.prev( prev.reference() );
                            }
                        }
                        size( size() - 1 );
                    }
                }

                entry.destroy_object();

	          if(( size() == 0 ) && ( header() != null )) 
			throw new RuntimeException( "Non-null header in empty list." );
            }
        }
    }

   /**
    * Override read due to a bug in PSS - make sure that the storage home for 
    * EntryStorage is incarnated before invoking super.read.
    */
    public void read( org.omg.CORBA.portable.InputStream [] input )
    {
        home();
        super.read( input );
    }

    
   /**
    * Returns the entry storage home.
    * @return EntryStorageHome the storage home the EntryStorage instances
    */
    private EntryStorageHome home()
    {
        if( home == null )
        {
		try
		{
                home = (EntryStorageHome) get_storage_home().get_catalog().find_storage_home(
 		      "PSDL:osm.net/list/EntryStorageHomeBase:1.0" );
		}
		catch( Throwable e )
		{
		    String error = "Could not resolve the EntryStorageHome";
		    throw new RuntimeException( error, e );
		}
        }
        return home;
    }
}
