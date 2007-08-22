/*
 * @(#)Entry.java
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

import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;


public class Entry extends EntryStorageBase
{
        
   /**
    * Returns the primary storage obhect that this entry refers to.
    * @return StorageObject the primary storage object
    */
    public StorageObject target()
    {
        try
        {
            return (StorageObject) get_storage_home().get_catalog().find_by_pid( target_pid() );
        }
        catch( NotFound nf )
        {
            return null;
        }
    }
    
    /**
     * Get the ref for an entry.
     * @return EntryStorageRef a reference to the entry
     */
    public EntryStorageRef reference( )
    {
        return new EntryStorageBaseRef( get_short_pid(), get_storage_home());
    }
    
}