/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 23/03/2001
 */

package net.osm.session.hello;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import net.osm.session.message.SystemMessage;
import net.osm.session.message.MessageClassification;

import net.osm.session.processor.Appliance;
import net.osm.session.processor.AbstractAppliance;

/**
 * The <code>HelloAppliance</code> is an example of a process
 * implementation. 
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class HelloAppliance extends AbstractAppliance
implements Configurable
{
   /**
    * Configuration supplied by the appliance container.
    */
    private Configuration m_config;

   /**
    * Message based on supplied context.
    */
    private String m_location = "Unknown location";

   /**
    * The number of steps to execute.
    */
    private int m_count = 10;

   /**
    * Internal counter of the progress of the process.
    */
    private int m_countdown;

   /**
    * The amount of time to sleep between execution intervals.
    */
    private int m_delay;


    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Configuration of the process based on a supplied Configuration arguments.
    *
    * @param config Configuration argument supplied by the process factory.
    * @exception ConfigurationException if the supplied configuration is incomplete or badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        getLogger().debug( "configuration" );
        m_config = config;
        m_count = m_config.getChild( "count" ).getAttributeAsInteger( "value", m_count );
        m_delay = m_config.getChild( "pause" ).getAttributeAsInteger( "value", m_delay );
	  m_countdown = m_count;
    }

   /**
    * Initate the thread - the implementation simply executes a countdown, printing out
    * values until it reaches 0.
    */
    public void run()
    {

        getLogger().info( "message: " + m_config.getChild( "message" ).getValue("null") );

        //
        // if the current state is terminated or completed that return
        // otherwise enter into a execute/sleep loop
        //

        while( ( m_state != Appliance.TERMINATED ) && ( m_state != Appliance.COMPLETED ))
        {
            switch( m_state )
            {
              case Appliance.IDLE:
                switch( m_action )
                {
                  case CONTINUE:
                    break;
                  case START:
                    setApplianceState( Appliance.RUNNING );
                    break;
                  case SUSPEND:
                    setApplianceState( Appliance.SUSPENDED );
                    break;
                  case RESUME:
                    setApplianceState( Appliance.RUNNING );
                    break;
                  case STOP:
                    setApplianceState( Appliance.TERMINATED );
                    break;
                }
                break;
              case Appliance.RUNNING:
                switch( m_action )
                {
                  case CONTINUE:
                    if( m_count > 0 )
                    {
                        getLogger().debug("count: " + m_count-- );
                    }
                    else
                    {
                        setApplianceState( Appliance.COMPLETED );
                    }
                    break;
                  case START:
                    break;
                  case SUSPEND:
                    setApplianceState( Appliance.SUSPENDED );
                    break;
                  case RESUME:
                    break;
                  case STOP:
                    setApplianceState( Appliance.TERMINATED );
                    break;
                }
                break;
              case Appliance.SUSPENDED:
                switch( m_action )
                {
                  case CONTINUE:
                    break;
                  case START:
                    setApplianceState( Appliance.RUNNING );
                    break;
                  case SUSPEND:
                    break;
                  case RESUME:
                    setApplianceState( Appliance.RUNNING );
                    break;
                  case STOP:
                    setApplianceState( Appliance.TERMINATED );
                    break;
                }
                break;
              case Appliance.TERMINATED:
                break;
              case Appliance.COMPLETED:
                break;
            }
            try
            {
                Thread.currentThread().sleep( m_delay );
            }
            catch( Throwable wakeup )
            {
            }
        }
    }
}
