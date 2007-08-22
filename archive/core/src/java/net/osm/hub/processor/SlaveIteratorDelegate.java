/*
 * @(#)ProcessorIteratorDelegate.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 09/04/2001
 */

package net.osm.hub.processor;

import org.omg.CORBA.ORB;
import org.omg.CollaborationFramework.SlaveIteratorOperations;
import org.omg.CosPersistentState.StorageObject;
import net.osm.hub.resource.LinkStorageIteratorDelegate;
import net.osm.list.Iterator;

/**
 * Implementation of the SlaveIterator interface.
 */

public class SlaveIteratorDelegate extends LinkStorageIteratorDelegate implements SlaveIteratorOperations
{

   /**
    * SlaveIteratorDelegate constructor.
    * @param orb
    * @param iterator of a list of links
    */

    public SlaveIteratorDelegate( ORB orb, Iterator iterator )
    {
        super( orb, iterator );
    }
      
}
