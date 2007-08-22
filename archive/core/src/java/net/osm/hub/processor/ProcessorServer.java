/*
 * @(#)ProcessorServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.JarURLConnection;
import java.util.jar.Attributes;
import java.util.Hashtable;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.phoenix.BlockContext;
import org.apache.avalon.phoenix.Block;

import org.omg.CORBA_2_3.ORB;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.StorageObject;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.CollaborationFramework.ProcessorPOATie;
import org.omg.Session.AbstractResource;
import org.omg.Session.task_state;
import org.omg.CollaborationFramework.StateDescriptor;
import org.omg.CollaborationFramework.CoordinatedBy;
import org.omg.CollaborationFramework.ControlledBy;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.CollaborationSingleton;
import org.omg.Session.ProducedBy;
import org.omg.CommunityFramework.Criteria;

import net.osm.hub.gateway.Manager;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.FactoryService;
import net.osm.hub.gateway.DomainService;
import net.osm.hub.gateway.RandomService;
import net.osm.hub.gateway.ServantContext;
import net.osm.hub.pss.ProcessorStorage;
import net.osm.hub.pss.ProcessorStorageHome;
import net.osm.hub.resource.AbstractResourceServer;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;
import net.osm.realm.RealmSingleton;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.orb.ORBService;

/**
 * A <code>ProcessorServer</code> block that provides general pluggable appliacance
 * based process management.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class ProcessorServer extends AbstractResourceServer 
implements ProcessorService
{

    //=======================================================================
    // state
    //=======================================================================

    private ProcessorStorageHome home;
    
   /**
    * Table of appliance mappings where each key corresponds to a DPML criteria
    * label and each value corresponds to the appliance element name.  The table 
    * is used to lookup the appliance to use for a particular processor based on 
    * the processor criteria label.
    */
    private Hashtable mapping = new Hashtable();

   /**
    * Table of appliance classes keyed by the name of the appliance.
    */
    private Hashtable engines = new Hashtable();

   /**
    * Table of appliance profiles keyed by the name of the appliance.
    */
    private Hashtable profiles = new Hashtable();

   /**
    * The root application directory within which we can find the appliance 
    * directory.
    */
    private File base;
    private File appliancePath;

    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Set the block context.
    * @param context the block context
    * @exception ContextException if the block cannot be narrowed to BlockContext
    */
    public void contextualize( Context block ) throws ContextException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize [processor server]" );
	  super.contextualize( block );
	  base = ((BlockContext) block).getBaseDirectory();
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize complete [processor server]" );
    }

    //=======================================================================
    // Configurable
    //=======================================================================
    
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
		"configuration [processor server]" );
        super.configure( config );
        this.configuration = config;
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
	    "configuration complete [processor server]" );
    }
    
    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
	  super.initialize();
        if( getLogger().isDebugEnabled() ) getLogger().debug( 
	    "initialization [processor]" );

        home = (ProcessorStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/ProcessorStorageHomeBase:1.0" );

	  //
	  // build up a table of the DPML processor criteria elements and 
        // and the corresponding appliance mappings
        //

        if( getLogger().isDebugEnabled() ) getLogger().debug( 
	    "mapping DPML processor criteria" );
        Configuration[] children = configuration.getChild("dpml").getChildren();
 	  for( int i=0; i<children.length; i++ )
        {
 		Configuration c = children[i];
 		if( c.getName().equals( "processor" ) )
 		{
 		    String criteriaLabel = c.getAttribute("label");
 		    String applianceLabel = c.getAttribute("appliance");
                mapping.put( criteriaLabel, applianceLabel );
                if( getLogger().isDebugEnabled() ) getLogger().debug( 
	             "\tcriteria: " + criteriaLabel + ", appliance: " + applianceLabel );
 		}
        }

        //
        // make sure that all of the appliance declarations refer to 
        // classes that can be instantiated and build a table of the 
        // classes based on the appliance name
        //

        if( getLogger().isDebugEnabled() ) getLogger().debug( 
	    "mapping appliance entries" );
        Configuration[] mappings = configuration.getChild("extensions").getChildren("appliance");
 	  for( int i=0; i<mappings.length; i++ )
        {
 		Configuration profile = mappings[i];
		String name = profile.getAttribute("label");
		String implementation = profile.getAttribute("class");
		try
		{
		    Class appliance = Class.forName( implementation );
		    engines.put( name, appliance );
		    profiles.put( name, profile );
                if( getLogger().isDebugEnabled() ) getLogger().debug( 
	            "\tappliance: " + name + ", class: " + appliance.getName() );
		}
	      catch( Throwable e )
		{
		    final String error = "unable to resolve appliance with the label: ";
                if( getLogger().isWarnEnabled() ) getLogger().warn( error + name, e );
		    throw new Exception( error, e );
	      }
        }

        if( getLogger().isDebugEnabled() ) getLogger().debug( 
	    "initialization complete [processor server]" );
    }

   /**
    * Returns the storage home for processors.
    * @return ProcessorStorageHome the processor storage home
    */
    protected ProcessorStorageHome getProcessorStorageHome()
    {
        if( home == null ) throw new IllegalStateException(
	   "server has not been initalized");
        return home;
    }

   /**
    * Return the appliance instance.
    */
    protected Class getApplianceClass( File path, Configuration config )
    throws ConfigurationException
    {
	  try
	  {
		String filename = config.getAttribute( "file" );
            final File jar = new File( path, filename );
		final URL url = jar.toURL();
		URLClassLoader loader = new URLClassLoader( new URL[]{ url }, 
		  Thread.currentThread().getContextClassLoader());
		return loader.loadClass( getMainClassName( url ));
     	  }
	  catch( Exception e )
	  {
		File f = new File( path, config.getAttribute( "file", "" ));
		final String error = "failed to establish class for appliance: " 
		  + config.getName() + " file: " + f.toString();
            throw new ConfigurationException( error, e );
	  }
    }

    private String getMainClassName( URL url )
    throws IOException 
    {
        URL jarURL = new URL("jar:" + url.toString() + "!/");
	  JarURLConnection connection = (JarURLConnection)jarURL.openConnection();
	  Attributes attributes = connection.getMainAttributes();
	  String main = attributes.getValue(Attributes.Name.MAIN_CLASS);
	  if( main == null ) throw new RuntimeException("Missing main class declaration in " + url );
        return main;
    }

    //=======================================================================
    // FactoryService
    //=======================================================================
    
   /**
    * Creation of a new processor based on a supplied criteria.
    * @param name the name of the process
    * @param value the <code>ProcessorCriteria</code> defining 
    *   process constraints and/or parameters
    * @exception FactoryException if the criteria is incomplete
    */
    public AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "create" );
        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");
	  if( !( criteria instanceof ProcessorCriteria ) )
	  {
		final String error = "Invalid criteria, " + criteria.getClass();
	      throw new FactoryException( error );
        }
	  return createProcessor( name, (ProcessorCriteria) criteria );
    }

   /**
    * Creation of a new Processor object reference.
    * @param name the name of the processor
    * @param criteria the <code>ProcessorCriteria</code> defining 
    *   process constraints and/or parameters
    * @exception FactoryException
    */
    public Processor createProcessor( String name, ProcessorCriteria criteria ) 
    throws FactoryException
    {
        String applianceLabel = "";
        try
        {
            applianceLabel = getApplianceLabel( criteria.label );
		return createProcessor( name, criteria, applianceLabel );
        }
        catch( Exception e )
        {
            String error = "criteria declaration does not contain an appliance label";
            throw new FactoryException( error, e );
        }
    }

   /**
    * Creation of a new Processor object reference.
    * @param name the name of the processor
    * @param criteria the <code>ProcessorCriteria</code> defining 
    *   process constraints and/or parameters
    * @param label the label identifying the appliance to use to execute the process
    * @exception FactoryException
    */
    public Processor createProcessor( String name, ProcessorCriteria criteria, String label ) 
    throws FactoryException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "create" );
	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");
        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");
        if( label == null ) throw new NullPointerException(
          "null appliance label argument");

	  // create a storage object for this processor

        ProcessorCriteria processorCriteria = (ProcessorCriteria) criteria;
        ProcessorStorage store = null;
        try
        {
		StandardPrincipal principal = getCurrentPrincipal( );
            long t = TimeUtils.resolveTime( clock );
            store = home.create(
			principal,
			domain.getDomainShortPID(), 
			random.getRandom(), t, t, t, name, 
			new ProducedBy(),
			new StateDescriptor( task_state.notstarted ), 
			new CoordinatedBy(), 
			new ControlledBy(),
		      processorCriteria.label,
			processorCriteria.model, 
			processorCriteria.values,
			label
	      );
        }
        catch (Exception e)
        {
		String error = "Unexpected failure while creating processor storage.";
            throw new FactoryException( error, e );
        }
        
	  // create an object reference to return to the client

        try
        {
            return getProcessorReference( store.get_pid() );
        }
        catch (Exception e)
        {
		String error = "failed to allocate a new processor reference";
            throw new FactoryException( error, e );
        }
    }

   /**
    * Creation of a servant based on a supplied object identifier.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public Servant createServant( byte[] oid )
    {
        try
        {
	      ProcessorStorage store = (ProcessorStorage) home.get_catalog().find_by_pid( oid );
            return new ProcessorPOATie( 
              (ProcessorDelegate) super.processLifecycle( new ProcessorDelegate( ), oid ), poa );
        }
        catch( Exception e )
	  {
		final String error = "processor delegate instantiation failure.";
		throw new RuntimeException( error, e );
	  }
    }

   /**
    * Utility method that creates the default servant context object.
    * @param oid object identifier
    * @return Context the context object to be applied to the delegate
    */
    protected ServantContext getContext( StorageObject store )
    {
        if( store == null ) throw new NullPointerException(
		"Illegal null storage object supplied to getContext.");

        try
        {
	      final ProcessorStorage ps = (ProcessorStorage) store;
	      final String name = ps.appliance();
	      ServantContext parent = super.getContext( store );
		final Class appliance = (Class) engines.get( name );
	      final Configuration profile = (Configuration) profiles.get( name );
	      final Configuration policy = profile.getChild( "policy" );
	      final Configuration config = profile.getChild( "configuration" );
            return new ProcessorContext( parent, name, appliance, policy, config );
        }
        catch( Throwable e )
        {
            final String error = "Unable to create processor context.";
		throw new RuntimeException( error, e );
        }
    }

   /**
    * Returns a reference to a Processor given a persistent storage object identifier.
    * @param pid Processor persistent identifier
    * @return Processor corresponding to the PID
    * @exception NotFound if the supplied pid does not match a know Processor
    */
    public Processor getProcessorReference( byte[] pid )
    throws NotFound
    {
	  StorageObject store = (StorageObject) home.get_catalog().find_by_pid( pid );
        return getProcessorReference( store );
    }

    /**
    * Returns a reference to a Processor given a persistent storage object.
    * @param StorageObject Processor persistent object
    * @return Processor corresponding to the storage object PID
    */
    public Processor getProcessorReference( StorageObject store )
    {
        return ProcessorHelper.narrow( poa.create_reference_with_id
          (store.get_pid(), ProcessorHelper.id() ));
    }


    //=======================================================================
    // utilities
    //=======================================================================

    public String getApplianceLabel( String label ) throws Exception
    {
        Object path = mapping.get( label );
        if( path == null )
	  {
		String error = "appliance path for the criteria '" + label + "' not found";
            throw new Exception( error );
        }
	  return (String) path;
    }
}
