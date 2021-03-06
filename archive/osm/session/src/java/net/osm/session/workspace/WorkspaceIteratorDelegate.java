/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 09/04/2001
 */

package net.osm.session.workspace;

import org.omg.CORBA.ORB;
import org.omg.Session.WorkspaceIteratorOperations;
import net.osm.session.linkage.LinkStorageIteratorDelegate;
import net.osm.session.resource.AbstractResourceIteratorDelegate;
import net.osm.list.Iterator;

/**
 * Implementation of the WorkspaceIterator interface.
 */

public class WorkspaceIteratorDelegate extends AbstractResourceIteratorDelegate implements WorkspaceIteratorOperations
{

   /**
    * WorkspaceIteratorDelegate constructor.
    * @param orb
    * @param Iterator from the persistent list
    * @link the last LinksStorage instance returned from the iterator
    */

    public WorkspaceIteratorDelegate( Iterator iterator )
    {
        super( iterator );
    }
      
}
