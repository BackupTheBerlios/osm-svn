/*
 * @(#)List.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 6/02/2001
 */

package net.osm.util;

import org.apache.avalon.framework.activity.Disposable;

/**
 * List enhances the classic java.util.List with support for 
 * the association of ListListeners that are notified of additions 
 * and removals to/from the list.
 */

public interface List extends java.util.List, ListHandler, Disposable
{

   /**
    * Adds a <code>ListListener</code>.
    */
    public void addListListener( ListListener listener );

   /**
    * Removes a <code>ListListener</code>.
    */
    public void removeListListener( ListListener listener );

}
