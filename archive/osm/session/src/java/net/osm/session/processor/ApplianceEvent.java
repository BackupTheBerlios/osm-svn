/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 24/06/2001
 */

package net.osm.session.processor;

import java.util.EventObject;

/**
 * The <code>ApplianceEvent</code> is an event raised by an appliance
 * as a result of a state change.
 */

public class ApplianceEvent extends EventObject
{

   //============================================================
    // state
    //============================================================

   /**
    * Mode.
    */
    private int m_state;

    //============================================================
    // constructor
    //============================================================

   /**
    * Creation of a new ApplianceEvent signally the change of state of the 
    * appliance.
    *
    * @param appliance the source appliance 
    * @param state the appliance state
    */
    public ApplianceEvent( Appliance source, int state ) 
    {
        super( source );
        m_state = state;
    }

    //============================================================
    // ContextEvent
    //============================================================

   /**
    * Returns the appliance state
    * @return int the appliance state
    */
    public int getState()
    {
        return m_state;
    }
}

