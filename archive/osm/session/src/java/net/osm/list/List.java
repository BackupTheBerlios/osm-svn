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

import org.apache.pss.util.Incrementor;

/**
 * List is a storage type that represent a set of linked EntryStorage 
 * instances.  A list contains a reference to e <code>header<code>
 * entry which consitutes the root entry in the list.  Entries contain
 * references to the next and previous entry.  Client can access list
 * content using a list iterator.
 */
public class List
extends LinkedListBase
{
    
    private static final String PSS_HOME = "PSDL:osm.net/list/EntryStorageHomeBase:1.0";

    private EntryStorageHome home;

    
   /**
    * Adds a new entry to the end of the linked list.
    * @param target the storage object to add to the end of the list
    */
    public synchronized void add( StorageObject target )
    {
	  if( target == null ) throw new RuntimeException(
		"Illegal attempt to add a null storage object to a list.");

        EntryStorage entry = home().create( target.get_pid(), null, null, false );

        EntryStorage header = header();
        if( header != null ) synchronized( header )
        {
            // We have a list with at least one entry. Adding an entry involves updating the
            // last entry to include the new entry as its next entry and update the header to
            // reference the new entry as the header's prev entry (last entry).

            if( size() == 1 )
            {
                header.prev( entry.reference() );
                header.next( entry.reference() );
                entry.prev( header.reference() );
                size( 2 );
            }
            else
            {
                EntryStorage last = header.prev();
                if( last != null ) synchronized( last )
                {
                    header.prev( entry.reference() );
                    entry.prev( last.reference() );
                    last.next( entry.reference() );
                    size( size() + 1 );
                }
                else
	          {
                    entry.destroy_object();
		        throw new RuntimeException(
	                "EntryStorage header return a null prev entry in list of size = " + size()
                      + "\n" + this 
                    );
                }
	      }
        }
        else
        {
            
            // This is an empty list so put a reference to the new EntryStorage 
            // in the list header and set the header prev slot to the same value.

            entry.prev( entry.reference() );
            header( entry.reference() );
            size( 1 );
        }
    }
    
   /**
    * Requests removal of an entry with a target corresponding to the supplied
    * storage object. 
    * @param target the storage object to add to the list
    * @exception NullPointerException if the supplied target argument is null
    * @exception NotFound if the supplied storage object is not contained within this list
    */
    public synchronized void remove( StorageObject target )
    {
	  if( target == null ) throw new NullPointerException(
		"Illegal attempt to remove a null target from a list.");
 
        Iterator iterator = iterator();
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
                // ignore this case
            }
            catch( Throwable e )
            {
                final String error = "Internal error while removing a list entry.";
                throw new RuntimeException( error, e );
            }
        }
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
    protected synchronized void remove_entry( final EntryStorage entry )
    throws NotFound
    {
	  if( entry == null ) throw new NullPointerException(
		"Illegal attempt to remove a null entry from a list.");

        EntryStorage header = header();
        if( header == null ) throw new NotFound();

        synchronized( entry )
        {

		// We have a list with at least one entry. Check 
	      // if we are removing the header entry or an entry in the 
		// body of the list.

            synchronized( header )
            {
                if( header.equals( entry ) )
                {
                    
                    // Client is requesting the removal of the header
			  // which means that either the next entry becomes the 
	              // header, or, if no next entry, the header is set to null.

                    if( size() == 1 )
                    {
                        header( (EntryStorageRef) null );
                        size( 0 );
                    }
                    else
                    {
                        EntryStorage next = header.next();
                        if( next != null ) synchronized( next )
                        {
                            // There is a valid next entry in the current header
                            // which needs to assigned as the new header.
                            
                            EntryStorage last = header.prev();
                            if( last != null ) synchronized( last )
                            {
                                next.prev( last.reference() );
                                header( next.reference() );
                                size( size() - 1 );
                            }
			          else
				    {                            
		                    throw new RuntimeException(
	                            "Unexpected null prev entry in list header. \n" + this );
				    }
                        }
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
                        if( next != null ) synchronized( next )
                        {
                            prev.next( next.reference() );
                            next.prev( prev.reference() );
                        }
                        else
                        {
                            prev.next( (EntryStorageRef) null );
				    header.prev( prev.reference() );
                        }
                        size( size() - 1 );
                    }
                }
                entry.destroy_object();
            }
        }
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
                home = (EntryStorageHome) 
                  get_storage_home().get_catalog().find_storage_home( PSS_HOME );
		}
		catch( Throwable e )
		{
		    String error = "Could not resolve the EntryStorageHome";
		    throw new RuntimeException( error, e );
		}
        }
        return home;
    }

    public String toString()
    {
        String s = "LIST: " + size() + " entries.";
        if( header() != null ) s = s + "\n" + header().toString();
        return s;
    }
}
