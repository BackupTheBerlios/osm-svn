/*
 * @(#)CommunityServer.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.hub.community;

import java.io.File;
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
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
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
import org.omg.CommunityFramework.MembershipModel;
import org.omg.CommunityFramework.CommunityCriteria;
import org.omg.CommunityFramework.CommunityPOATie;
import org.omg.CommunityFramework.CommunityHelper;
import org.omg.CommunityFramework.Community;
import org.omg.CommunityFramework.RecruitmentStatus;
import org.omg.CommunityFramework.Criteria;
import org.omg.Session.AbstractResource;
import org.omg.Session.ProducedBy;
import org.omg.Session.User;
import org.omg.Session.Administers;
import org.omg.Session.AdministeredBy;
import net.osm.hub.gateway.FactoryException;
import net.osm.orb.ORBService;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.hub.gateway.FactoryService;
import net.osm.hub.gateway.DomainService;
import net.osm.hub.gateway.RandomService;
import net.osm.hub.gateway.ServantContext;
import net.osm.hub.resource.AbstractResourceServer;
import net.osm.hub.workspace.WorkspaceService;
import net.osm.hub.workspace.WorkspaceServer;
import net.osm.pss.PersistanceHandler;
import net.osm.hub.user.UserService;
import net.osm.hub.pss.CommunityStorage;
import net.osm.hub.pss.CommunityStorageHome;
import net.osm.hub.pss.LinkStorageHome;
import net.osm.hub.pss.UserStorage;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;
import net.osm.realm.StandardPrincipal;
import net.osm.dpml.DPML;
import net.osm.util.Incrementor;

/**
 * The <code>CommunityServer</code> block services supporting the 
 * creation of new <code>Community</code> instances.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class CommunityServer extends AbstractResourceServer 
implements CommunityService, CommunityAdministratorService
{

    //=======================================================================
    // state
    //=======================================================================

    private CommunityStorageHome home;
    private UserService userService;
    private Incrementor linkInc = Incrementor.create("LINK");
    private LinkStorageHome links;

    //=================================================================
    // Composable
    //=================================================================
    
    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
        if( getLogger().isDebugEnabled()  ) getLogger().debug( "composition [community server]" );
	  super.compose( manager );
	  try
	  {
		userService = (UserService) manager.lookup("USER");
	      super.manager.put( "USER", userService );
	      super.manager.put( "WORKSPACE", manager.lookup("WORKSPACE") );
	      super.manager.put( "FACTORY", manager.lookup("FACTORY") );
	  }
	  catch( Exception e )
	  {
		final String error = "manager failed to provide a service";
		throw new ComponentException( error, e);
	  }
        if( getLogger().isDebugEnabled()  ) getLogger().debug( "composition [community server] complete" );
    }


    //=======================================================================
    // Initializable
    //=======================================================================
    
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled()  ) getLogger().debug( "initialization [community server]" );
	  super.initialize();
        home = (CommunityStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/CommunityStorageHomeBase:1.0" );
        links = (LinkStorageHome) session.find_storage_home(
 		"PSDL:osm.net/hub/pss/LinkStorageHomeBase:1.0" );
        if( getLogger().isDebugEnabled()  ) getLogger().debug( "initialization complete [community server]" );
    }
        
    //=======================================================================
    // FactoryService
    //=======================================================================
    
   /**
    * Creation of a generic resource reference.
    * @param name the name of the generic resource
    * @param value the <code>CommunityCriteria</code> defining 
    *   resource constraints and/or parameters
    * @exception FactoryException if the criteria is incomplete
    */
    public AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException
    {
        if( getLogger().isDebugEnabled()  ) getLogger().debug( "create" );

	  if( !initialized() ) throw new IllegalStateException("server has not been initialized");
	  if( disposed() ) throw new IllegalStateException("server is in disposed state");
        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");
	  if( !( criteria instanceof CommunityCriteria ) )
	  {
		final String error = "Invalid criteria, " + criteria.getClass();
	      throw new FactoryException( error );
        }
	  return createCommunity( name, (CommunityCriteria) criteria );
    }

    //=======================================================================
    // CommunityService
    //=======================================================================

   /**
    * Creation of a new Community object reference bound to the principal user
    * as community administrator.
    * @param name the initial name to assign to the community
    * @return Community a new Community object reference
    */
    public Community createCommunity( String name, CommunityCriteria criteria ) 
    throws FactoryException
    {
        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");
        return createCommunity( name, criteria, null, null );
    }

   /**
    * Creation of a new Community object reference bound to a supplied administrator.
    * @param name the initial name to assign to the community
    * @param user the User to assign as community administrator
    * @param pricipal the Principal to assign as community owner
    * @return Community a new Community object reference
    */
    private Community createCommunity( String name, CommunityCriteria criteria, 
      User user, StandardPrincipal principal )
    throws FactoryException
    {
	  if( getLogger().isDebugEnabled()  ) getLogger().debug(
          "creating new community (admin mode = " 
          + (user != null ) + ")");

        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");

	  // default value

	  Any value = orb.create_any();
	  value.insert_string("");
	  boolean template = false;
	  boolean locked = false;

	  // get the user to whom we will assign task ownership

        if( user == null ) try
	  {
        	user = userService.locateUser();
	  }
	  catch( Throwable e )
	  {
		String error = "unable to resolve the principal user";
		throw new FactoryException( error, e );
	  }

        if( principal == null ) try
	  {
        	principal = getCurrentPrincipal( );
	  }
	  catch( Throwable e )
	  {
		String error = "unable to resolve the current principal";
		throw new FactoryException( error, e );
	  }

	  // create a storage object for the generic resource

        CommunityStorage store = null;
        try
        {
		MembershipModel model = (MembershipModel) criteria.model;
            long t = TimeUtils.resolveTime( clock );
            store = home.create(
			principal,
			domain.getDomainShortPID(), 
			random.getRandom(), t, t, t, name, 
			new ProducedBy(),
			model, 
		      RecruitmentStatus.OPEN_MEMBERSHIP 
	      );
            home.get_catalog().flush();
        }
        catch (Exception e)
        {
		String error = "Unexpected failure while creating community storage.";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }
        
	  // create an object reference to return to the client

	  Community community = null;
        try
        {
            community = getCommunityReference( store.get_pid() );
        }
        catch (Exception e)
        {
		String error = "failed to allocate a new reference";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }

        // establish relationships between user, task and processor

/*
        Administers administers = new Administers( community );

        AdministeredBy administeredBy = new AdministeredBy( user );
        try
        {
		community.bind( administeredBy );
        }
        catch (Exception e)
        {
		String error = "failed to bind AdministeredBy relationship on community";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		store.destroy_object();
            throw new FactoryException( error, e );   
        }
        try
        {
		user.bind( administers );
		if( getLogger().isDebugEnabled() ) getLogger().debug( 
		  "user/community administration established" );
        }
        catch (Exception e)
        {
		String error = "failed to establish Administers relationship on user.";
		if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
		store.destroy_object();
            throw new FactoryException( error, e );   
        }
        finally
        {
            return community;
        }
*/

	  if( getLogger().isDebugEnabled()  ) getLogger().debug(
          "new community: " + store.random() );

        return community;

    }

    //=======================================================================
    // CommunityAdministratorService
    //=======================================================================

   /**
    * Creation of a new Community object reference bound to a supplied administrator.
    * @param name the initial name to assign to the community
    * @param user the UserStorage to assign as community administrator
    * @param pricipal the Principal to assign as community owner
    * @return Community a new Community object reference
    */
    public CommunityStorage createCommunityStorage( String name, CommunityCriteria criteria, 
      UserStorage owner, StandardPrincipal principal )
    throws FactoryException
    {
	  if( getLogger().isDebugEnabled()  ) getLogger().debug(
          "creating new community storage (admin mode)");

        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");
        if( owner == null ) throw new NullPointerException(
          "null user storage argument");
        if( principal == null ) throw new NullPointerException(
          "null principal argument");

	  // default value

	  Any value = orb.create_any();
	  value.insert_string("");
	  boolean template = false;
	  boolean locked = false;

	  // create a storage object for the community

        CommunityStorage store = null;
        try
        {
		MembershipModel model = (MembershipModel) criteria.model;
            long t = TimeUtils.resolveTime( clock );
            store = home.create(
			principal,
			domain.getDomainShortPID(), 
			random.getRandom(), t, t, t, name, 
			new ProducedBy(),
			model, 
		      RecruitmentStatus.OPEN_MEMBERSHIP 
	      );
            AdministeredBy admin = new AdministeredBy( userService.getUserReference( owner.get_pid() ) );
            store.accessed_by().add( links.create( linkInc.increment(), admin ) );
            Administers administers = new Administers( getCommunityReference( store ));
            owner.accesses().add( links.create( linkInc.increment(), administers ) );	
		return store;
        }
        catch (Exception e)
        {
		String error = "community storage creation failure";
            if( getLogger().isErrorEnabled() ) getLogger().error( error, e );
            throw new FactoryException( error, e );
        }
    }

   /**
    * Returns a reference to a Community given a persistent storage object identifier.
    * @param pid Community  persistent identifier
    * @return Community  corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know Community 
    */
    public Community getCommunityReference( byte[] pid )
    throws NotFound
    {
        return getCommunityReference( (StorageObject) home.get_catalog().find_by_pid( pid ) );
    }

   /**
    * Returns a reference to a Community given a persistent storage object.
    * @param pid Community  persistent identifier
    * @return Community  corresponding to the PID
    * @exception NotFound if the supplied pid does not matach a know Community 
    */
    public Community getCommunityReference( StorageObject store )
    {
        return CommunityHelper.narrow(
          poa.create_reference_with_id
          (
            store.get_pid(), CommunityHelper.id()
          )
        );
    }

   /**
    * Creation of a servant based on a supplied delegate.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public Servant createServant( byte[] oid )
    {
        return new CommunityPOATie( 
	    (CommunityDelegate) super.processLifecycle( new CommunityDelegate(), oid ), poa );
    }
}
