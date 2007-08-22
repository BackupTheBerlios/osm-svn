/*
 * @(#)CertificationRequestServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.generic;

import java.io.File;
import java.util.Hashtable;
import java.io.Serializable;

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
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
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
import org.omg.CommunityFramework.GenericCriteria;
import org.omg.CommunityFramework.GenericResourcePOATie;
import org.omg.CommunityFramework.GenericResourceHelper;
import org.omg.CommunityFramework.GenericResource;
import org.omg.Session.AbstractResource;
import org.omg.Session.ProducedBy;
import org.omg.CommunityFramework.Criteria;

import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.FactoryService;
import net.osm.hub.gateway.DomainService;
import net.osm.hub.gateway.RandomService;
import net.osm.hub.resource.AbstractResourceServer;
import net.osm.hub.pss.GenericStorage;
import net.osm.hub.pss.GenericStorageHome;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;
import net.osm.dpml.DPML;
import net.osm.orb.ORBService;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.pss.PersistanceHandler;
import net.osm.realm.StandardPrincipal;

/**
 * The <code>GenericResourceServer</code> block services supporting the 
 * creation of new <code>GenericResource</code> instances.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class GenericResourceServer extends AbstractResourceServer 
implements GenericResourceService
{

    //=======================================================================
    // state
    //=======================================================================

    private GenericStorageHome home;
    
    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization" );
	  super.initialize();
        home = (GenericStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/GenericStorageHomeBase:1.0" );
    }
        
    //=======================================================================
    // FactoryService
    //=======================================================================
    
   /**
    * Creation of a generic resource reference.
    * @param name the name of the generic resource
    * @param value the <code>GenericCriteria</code> defining 
    *   resource constraints and/or parameters
    * @exception FactoryException if the criteria is incomplete
    */
    public AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "create" );

        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");
	  if( !( criteria instanceof GenericCriteria ) )
	  {
		final String error = "Invalid criteria, " + criteria.getClass();
	      throw new FactoryException( error );
        }
        return createGenericResource( name, (GenericCriteria) criteria );
    }

   /**
    * Creation of a GenericResource object reference based on a supplied name 
    * and criteria
    * @param name a initial name
    * @param criteria factory criteria
    * @return GenericResource a GenericResource object reference
    * @exception FactoryException
    */
    public GenericResource createGenericResource( String name, GenericCriteria criteria ) 
    throws FactoryException
    {

	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");
        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");

	  // default value

	  String identifier = criteria.identifier;
	  boolean template = false;
	  boolean locked = false;
	  Serializable value = null;

        ValueFactory factory = orb.lookup_value_factory( identifier );
        if( factory instanceof Serializable )
	  {
	      value = (Serializable) factory;
        }
        else 
	  {
	      throw new FactoryException(
              "could not resolve a value factory for: " + identifier );
        }

	  // create a storage object for the generic resource

        try
        {
		final StandardPrincipal principal = getCurrentPrincipal( );
            final long t = TimeUtils.resolveTime( clock );
		final byte[] dspid = domain.getDomainShortPID();
		final int r = random.getRandom();
            final GenericStorage store = home.create(
			principal,
			dspid,
			r, t, t, t, name, 
			new ProducedBy(),
			identifier,
			locked, 
			template,
			value
	      );
		return getGenericResourceReference( store.get_pid() );
        }
        catch (Throwable e)
        {
		String error = "Unexpected failure while creating generic storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }
    }

   /**
    * Returns a reference to a GenericResource given a persistent storage object.
    * @param pid generic resource persistant storage object identifier
    * @return GenericResource corresponding to the storage object pid
    * @exception NotFound if the supplied pid does not matach a know generic resource
    */
    public GenericResource getGenericResourceReference( byte[] pid )
    throws NotFound
    {
        try
	  {
            StorageObject store = (StorageObject) home.get_catalog().find_by_pid( pid );
            return GenericResourceHelper.narrow(
              poa.create_reference_with_id
              (
                store.get_pid(), GenericResourceHelper.id()
              )
            );
	  }
	  catch( NotFound e )
	  {
	      throw new NotFound();
	  }
	  catch( Throwable e )
	  {
		String error = "unexpected exception while resolving reference";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
	      throw new RuntimeException( error, e );
	  }
    }

   /**
    * Creation of a servant based on a supplied delegate.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public Servant createServant( byte[] oid )
    {
        return new GenericResourcePOATie( 
	    (GenericResourceDelegate) super.processLifecycle( 
		new GenericResourceDelegate(), oid ), poa );
    }
}
