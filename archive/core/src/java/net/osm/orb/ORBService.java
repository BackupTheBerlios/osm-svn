/*
 * @(#)ORBService.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 12/03/2001
 */


package net.osm.orb;

import org.apache.avalon.framework.component.Component;

import org.omg.CORBA_2_3.ORB;

/**
 * The ORBService is an interface facilitating access to the runtime ORB.  The
 * ORB instance exposed by the <code>getOrb</code> operation is an ORB supporting
 * the CORBA 2.3 portability specification.
 * @author Stephen McConnell <mcconnell@osm.net>
 */
public interface ORBService extends Component
{

   /**
    * Returns the current ORB.
    * @return ORB a portable CORBA Object Request Broker (ORB)
    */
    public ORB getOrb( );

}



