/*
 * @(#)PKIServer.java
 *
 * Copyright 2001 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 05/01/2002
 */

package net.osm.pki.process;

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
import org.omg.Session.AbstractResource;
import org.omg.Session.ProducedBy;
import org.omg.Session.task_state;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.GenericCriteria;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CollaborationFramework.ProcessorPOATie;
import org.omg.CollaborationFramework.StateDescriptor;
import org.omg.CollaborationFramework.CoordinatedBy;
import org.omg.CollaborationFramework.ControlledBy;
import org.omg.PKIAuthority.UnsupportedEncodingException;
import org.omg.PKIAuthority.MalformedDataException;

import net.osm.dpml.DPML;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.FactoryService;
import net.osm.hub.gateway.DomainService;
import net.osm.hub.gateway.RandomService;
import net.osm.hub.generic.GenericResourceService;
import net.osm.hub.pss.ProcessorStorage;
import net.osm.hub.pss.ProcessorStorageHome;
import net.osm.hub.processor.ProcessorServer;
import net.osm.orb.ORBService;
import net.osm.pki.pkcs.PKCS10;
import net.osm.pki.pkcs.PKCSSingleton;
import net.osm.pki.base.PKISingleton;
import net.osm.pki.authority.RegistrationAuthoritySingleton;
import net.osm.pki.authority.RegistrationAuthorityService;
import net.osm.pss.PSSConnectorService;
import net.osm.pss.PSSSessionService;
import net.osm.pss.PersistanceHandler;
import net.osm.realm.StandardPrincipal;
import net.osm.realm.StandardPrincipal;
import net.osm.time.TimeService;
import net.osm.time.TimeUtils;

/**
 * The <code>PKIServer</code> block provides services supporting the 
 * establishment and execution of digital identity certification, extension,
 * and certificate revocation processes.
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */

public class PKIServer extends ProcessorServer 
implements PKIService
{

    //=======================================================================
    // state
    //=======================================================================

    private GenericResourceService generic;

    private ORB orb;

    //=======================================================================
    // Composable
    //=======================================================================
    
   /**
    * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
    * The <code>Composable</code> implementation should use the specified
    * <code>ComponentManager</code> to acquire the components it needs for
    * execution.  This implementation adds a RegistrationAuthority and 
    * a GenericResourceService to the set of managed services that will 
    * be provided to delegates managed by this server.  These services are 
    * detailed in the following table:
    * <p>
    * <table border="1" cellpadding="3" cellspacing="0" width="100%">
    * <tr bgcolor="#ccccff">
    * <td width="20%"><b><code>Service</code></b></td>
    * <td><b><code>Description</code></b></td></tr>
    * <tr><td><code>AUTHORITY</code></td>
    * <td>A Registration Authority Service through which a certification 
    *   requesty can be issued.</td></tr>
    * <tr><td><code>GENERIC</code></td>
    * <td>A GenericResource Service through which new generic resources can be 
    * created and assigned as process outputs.</td></tr>
    * </table>
    *
    * @param manager The <code>ComponentManager</code> which this
    *                <code>Composer</code> uses.
    */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
	  super.compose( manager );
	  super.manager.put( "AUTHORITY", manager.lookup("AUTHORITY") );
	  super.manager.put( "GENERIC", manager.lookup("GENERIC") );
        generic = ((GenericResourceService)manager.lookup("GENERIC"));
        orb = ((ORBService)manager.lookup("ORB")).getOrb();
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initalization of the PKI related valuetypes.
    */
    public void initialize()
    throws Exception
    {
	  super.initialize();
        PKISingleton.init( orb );
        PKCSSingleton.init( orb );
	  RegistrationAuthoritySingleton.init( orb );
    }
        
    //=======================================================================
    // FactoryService
    //=======================================================================
    
   /**
    * The <code>PKIServer</code> provides factory services supporting the 
    * creation of PKI business processes and the related input and output 
    * resources.  The complete set of criteria supported by this server 
    * is detailed in the following table:
    *
    * <p>
    * <table border="1" cellpadding="3" cellspacing="0" width="100%">
    * <tr bgcolor="#ccccff">
    * <td width="20%"><b><code>Type</code></b></td>
    * <td><b><code>Label</code></b></td>
    * <td><b><code>Description</code></b></td></tr>
    *
    * <tr><td><code>GenericCriteria</code></td>
    * <td><code>net.osm.pki.certification.request</code></td>
    * <td>Criteria describing a GenericResource constrained to hold
    * a PKCS10 certification request.</td></tr>
    *
    * <tr><td><code>GenericCriteria</code></td>
    * <td><code>net.osm.pki.certification.response</code></td>
    * <td>Criteria describing a GenericResource constrained to hold
    * a PKCS7 certification response.</td></tr>
    *
    * <tr><td><code>ProcessorCriteria</code></td>
    * <td><code>net.osm.pki.certification</code></td>
    * <td>Processor criteria describing a digital identity certification 
    *  process that takes a PKCS10 resource as an input and produces
    *  a PKCS7 certification response as an output.  The process may 
    *  automonously suspend and issue request via messages to the 
    *  owner of the coordinating task if additional interaction is 
    *  required to complete the certification process.</td></tr>
    *
    * </table>
    * 
    * @param name the name of resource to be created
    * @param criteria the <code>Criteria</code> defining 
    *   resource creation constraints and/or parameters
    * @exception FactoryException if the criteria is incomplete
    *   or not supported by this server.
    */
    public AbstractResource create( String name, Criteria criteria ) 
    throws FactoryException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "create" );
        if( criteria == null ) throw new NullPointerException(
          "null criteria argument");

        if( criteria instanceof ProcessorCriteria )
	  {
	      return super.createProcessor( name, (ProcessorCriteria) criteria );
	  }
	  else if( criteria instanceof GenericCriteria )
	  {
            return generic.createGenericResource( name, (GenericCriteria) criteria );
	  }
	  else
	  {
		final String error = "Invalid criteria, " + criteria.getClass();
	      throw new FactoryException( error );
	  }
    }

   /**
    * Creation of a servant based on a supplied delegate.  The <code>CertificationServer</code>
    * overrides the supertype to associate a <code>CertificationDelegate</code> under a 
    * processor POA.
    * @param oid the persistent object identifier
    * @return Servant the servant
    */
    public Servant createServant( byte[] oid )
    {
	  //
	  // implementation needs to be updated to select the appropriate servant 
	  // based on the storage object type that the oid refers to - currently
	  // we are only implementing the certification process so its not an immediate
        // issue
        //

        return new ProcessorPOATie( 
	    (CertificationDelegate) 
             super.processLifecycle( new CertificationDelegate(), oid ), poa );
    }

    //=================================================================
    // Disposable 
    //=================================================================

   /**
    * Clean up state members.
    */ 
    public synchronized void dispose()
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug("dispose");
	  generic = null;
	  orb = null;
	  super.dispose();
    }
}
