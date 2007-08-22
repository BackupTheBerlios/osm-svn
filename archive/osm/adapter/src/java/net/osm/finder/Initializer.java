/**
 * License: etc/LICENSE.TXT
 * Copyright: Copyright (C) The Apache Software Foundation. All rights reserved.
 * Copyright: OSM SARL 2001-2002, All Rights Reserved.
 */
package net.osm.finder;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.LocalObject;
import org.omg.PortableServer.POA;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.orb.POAContext;
import org.apache.orb.DefaultPOAContext;

import org.openorb.CORBA.LoggableLocalObject;

/**
 * Initializer for an embedded time server.
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class Initializer extends LoggableLocalObject
implements Configurable, Contextualizable, ORBInitializer, Disposable
{

    private Configuration m_config;
    private Context m_context;
    private DefaultFinder m_target;
    private boolean m_flag = false;

    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Method invoked by the Apache ORB initializer to declare the runtime context.
    * @param Context runtime application context
    */
    public void contextualize( Context context ) throws ContextException
    {
        m_context = context;
    }

    //=======================================================================
    // Configurable
    //=======================================================================
    
   /**
    * Method invoked by the ORB initializer to declare the static configuration.
    * @param Configuration application static configuration
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
          m_config = config;
    }

    //==========================================================================
    // ORBInitializer
    //==========================================================================

    /**
     * Method invoked by the ORB.  The implementation establish the POA and servant
     * and passes the ORBInitInfo to the target POA as a context parameter for inital 
     * reference registration.
     * @param info 
     */
    public void pre_init( ORBInitInfo info )
    {

        if( getLogger().isDebugEnabled() ) getLogger().debug("Initializer" );

        //
        // create the POA
        //
        
        try
        {
            POA root = (POA) info.resolve_initial_references("RootPOA");
            if( getLogger().isDebugEnabled() ) getLogger().debug("creating POA" );
            m_target = new DefaultFinder();
            m_target.enableLogging( getLogger().getChildLogger("provider") );
            m_target.contextualize( m_context );
            m_target.configure( m_config.getChild("provider") );
            DefaultServiceManager manager = new DefaultServiceManager();
            manager.put( POAContext.POA_KEY, new DefaultPOAContext( root, null ) );
            manager.makeReadOnly();
            m_target.service( manager );
            m_target.initialize();
            m_flag = true;
        }
        catch( Throwable e)
        {
            throw new CascadingRuntimeException( "Unable to instantiate embedded server.", e);
        }
    }
  
    /**
     * Post initalization of the interceptor invoked by the
     * ORB in which this interceptor is installed.
     * @param info  
     */
    public void post_init( ORBInitInfo info ) 
    {
        if( !m_flag ) return;

        try
        {
	      info.register_initial_reference( 
              "Finder", m_target._this_object() );
        }
        catch( Throwable e)
        {
            throw new CascadingRuntimeException( "Unable to register server.", e);
        }

        try
        {
            m_target.start();
        }
        catch( Throwable e)
        {
            throw new CascadingRuntimeException( "Unable to start embedded server.", e);
        }
    }

    //=======================================================================
    // Disposable
    //=======================================================================
    
   /**
    * Disposal will be invoked by the ORB initializer following shutdown of the 
    * ORB we have been initialized within.
    */
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("initializer disposal" );
        try
        {
            m_target.stop();
        }
        catch( Throwable e )
        {
            if( getLogger().isWarnEnabled() ) getLogger().warn(
              "ignoring exception while stopping the time provider", e );
        }
        finally
        {
            m_target.dispose();
            m_target = null;
            m_config = null;
            m_context = null;
        }
    }
}
