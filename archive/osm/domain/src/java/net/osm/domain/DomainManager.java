/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.domain;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;

import org.omg.CORBA.Any;
import org.apache.orb.ORBContext;
import org.apache.pss.ORB;
import org.apache.pss.Connector;
import org.apache.pss.ConnectorContext;
import org.apache.pss.Session;
import org.apache.pss.StorageContext;

import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.NotFound;
import org.omg.CosPersistentState.ConnectorHelper;

import org.omg.NamingAuthority.RegistrationAuthority;
import org.omg.NamingAuthority.AuthorityId;


/**
 * <p>The <code>DomainManager</code> provides services supporting the 
 * creation, lokup and removal of <code>DomainStorage</code> entries
 * that capture multiple schema names - including DCE, DNS, ISO and IDL.</p>
 *
 * <p><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#ccccff">
 * <td colspan="2"><font size="+2"><b>Lifecycle</b></font></td>
 * <tr><td width="20%"><b>Phase</b></td><td><b>Description</b></td></tr>
 * <tr>
 * <td width="20%" valign="top">Contextualizable</td>
 * <td>
 * The <code>Context</code> value is handed by the super type.
 * </td></tr>
 * 
 * <td width="20%" valign="top">Serviceable</td>
 * <td>
 * <p>The <code>ServiceManager</code> is used to resolve a PSS enabled ORB.</p>
 * <tr>
 * <td width="20%" valign="top">Configurable</td>
 * <td>
 * <p>The <code>Configuration</code> value is used to establish the PSDL storage
 * type to implementation class mappings and configuration of the persistent 
 * connector.  The default configuration is shown below.</p>
 * <pre>
 * 
 *   &lt;configuration>
 * 
 *     <font color="blue"><i>&lt;!--
 *     The PSS configuration.
 *     --&gt;</i></font>
 * 
 *     &lt;pss&gt;
 * 
 *       &lt;connector value="file" /&gt;
 * 
 *       &lt;session&gt;
 *         &lt;parameter name="PSS.File.DataStore.Directory" value="pss" /&gt;
 *         &lt;parameter name="PSS.File.DataStore.Name" value="properties" /&gt;
 *       &lt;/session&gt;
 * 
 *       &lt;persistence&gt;
 *         &lt;storage psdl="PSDL:osm.net/domain/DomainStorageBase:1.0"
 *           class="net.osm.domain.DomainStore" /&gt;
 *         &lt;home psdl="PSDL:osm.net/domain/DomainStorageHomeBase:1.0"
 *           class="net.osm.domain.DomainStorageHomeBase" /&gt;
 *       &lt;/persistence&gt;
 * 
 *     &lt;/pss&gt;
 * 
 *   &lt;/configuration&gt;
 * </pre>
 * </td></tr>
 * 
 * <tr><td  valign="top">Initalizable</td>
 * <td>
 * <p>Handles the establishment of the PSS connection, session and reference 
 *   to the <code>DomainStorageHome</code>.</p>
 * </td></tr>
 * 
 * <tr><td  valign="top">Executable</td>
 * <td>
 * <p>Utility phase used for validation of <code>AuthorityId</code> creation.</p>
 * </td></tr>
 * 
 * <tr><td valign="top">Disposable</td>
 * <td>
 * <p>Cleanup and disposal of state members.</p>
 * </td></tr>
 *
 * </table>
 * </P>
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
public class DomainManager extends AbstractLogEnabled
implements DomainService, Configurable, Contextualizable, Serviceable, Initializable, Executable, Disposable
{

    //=======================================================================
    // state
    //=======================================================================

   /**
    * The default domain identifity derived from the configuration.
    */
    private ConnectorContext m_connector_context;

   /**
    * DomainStorage home.
    */
    private DomainStorageHome m_home;

   /**
    * Application context provided by the container.
    */
    private Context m_context;

   /**
    * Configuration provided by the container.
    */
    private Configuration m_config;

   /**
    * Manager provided by the container - provides a PSS configured ORB.
    */
    private ServiceManager m_manager;

   /**
    * Internal reference to the ORB provided by the manager against which
    * valuetype factories are declared.
    */
    private ORB m_orb;

   /**
    * Reference to the PSS Connector.
    */
    private Connector m_connector;

   /**
    * Reference to the PSS Session.
    */
    private Session m_session;

   /**
    * The default domain identifity derived from the configuration.
    */
    private byte[] m_default_domain;

    //=================================================================
    // Contextualizable
    //=================================================================

   /**
    * Set the application context.
    * @param context the application context
    */
    public void contextualize( Context context ) throws ContextException
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "contextualize" );
	  m_context = context;
    }

    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Used by a container to supply the static component configuration.
    * @param config the static configuration
    */    
    public void configure( final Configuration config )
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "configuration" );
        m_config = config;
    }
    
    //=================================================================
    // Serviceable
    //=================================================================
    
    /**
     * Pass the <code>ServiceManager</code> to the <code>Serviceable</code>.
     * The <code>Serviceable</code> implementation should use the supplied
     * <code>ServiceManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ServiceManager</code> which this
     *                <code>Serviceable</code> uses.
     */
    public void service( ServiceManager manager )
    throws ServiceException
    {
	  if( getLogger().isDebugEnabled() ) getLogger().debug( "compose" );
        m_manager = manager;
    }

    //=======================================================================
    // Initializable
    //=======================================================================
    
   /**
    * Initialization of the manager during which the container storage home is 
    * resolved following supertype initialization.
    * @exception Exception if an error occurs during initalization
    */
    public void initialize()
    throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization" );

        if( m_config == null ) throw new NullPointerException(
           "Manager has not been configured.");

        try
        {
            m_orb = (ORB) m_manager.lookup("orb");
            m_connector = m_orb.getConnector();

            Configuration pss = m_config.getChild("pss");
            m_connector.register( pss.getChild("persistence") );
            m_session = m_connector.createBasicSession( pss.getChild("session") );
            m_home = (DomainStorageHome) m_session.find_storage_home(
 		    "PSDL:osm.net/domain/DomainStorageHomeBase:1.0" );
        }
        catch( Throwable e )
        {
            getLogger().error( "intialization failure", e );
            throw new DomainException( "DomainManager initization failed.", e);
        }
    }

    //=======================================================================
    // Executable
    //=======================================================================

   /**
    * Executes validation of property container creation, property attribution and listing.
    * The implementation demonstrates the creation of a new <codePPropertySerDef</code>,
    * the assiciation of three properties (email address, web url, and current date).
    * If the email and url properties are already defined they will be updated with 
    * the supplied property values.  The last update properly will always be updated 
    * with the current system time. 
    */
    public void execute() throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "execution" );
     
        int scheme = RegistrationAuthority.DNS.value();
        String name = "home.osm.net";

        try
        {
            locateDomainEntry( scheme, name );
            getLogger().info( "Located existing DNS entry: " + name );
        }
        catch( NotFound nf )
        {
            createDomainEntry( scheme, name );
            getLogger().info( "Created new domain entry: " + name );
        }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

   /**
    * Disposal of the servant manager and cleanup of state members.
    */
    public void dispose() 
    {
        getLogger().debug("dispose");
        m_session.flush();
        m_manager.release( m_orb );
        m_orb = null;
        m_connector = null;
        m_home = null;
        m_context = null;
        m_config = null;
        m_manager = null;
        m_session = null;
        m_default_domain = null;
    }

    //=======================================================================
    // DomainService
    //=======================================================================

   /**
    * Return the default <code>AuthorityId</code> the the domain manager 
    * is managing.
    * @return byte[] default domain identify
    */
    public byte[] getDefaultDomain()
    {
        if( m_default_domain != null ) return m_default_domain;
        Configuration domain = m_config.getChild("domain");
        String scheme = domain.getChild("authority").getValue("DNS");
        String address = domain.getChild("address").getValue("localhost");
        AuthorityId id = resolveDomainEntry( schemeStringToInt( scheme ), address );
        return m_default_domain = resolveDomainPID( id );        
    }

   /**
    * Creation of a new <code>AuthorityId</code> structure under a 
    * persistent store.
    * @param int the naming authority scheme
    * @param entity the domain name
    * @exception DomainException
    */
    public AuthorityId createDomainEntry( int scheme, String name ) 
    throws DomainException
    {
        try
        {
            DomainStorage store = m_home.find_by_domain_key( scheme, name );
            throw new DomainException("Entry already exists: " + name );
        }
        catch( NotFound nf )
        {
            return m_home.create( scheme, name ).authority_id();
        }
    }

   /**
    * Locate an existing <code>AuthorityId</code> based on a supplied 
    * scheme and name.
    * @param int the naming authority scheme
    * @param entity the domain name
    * @exception DomainException
    */
    public AuthorityId locateDomainEntry( int scheme, String name ) 
    throws NotFound
    {
        return m_home.find_by_domain_key( scheme, name ).authority_id();
    }

   /**
    * Resolve a domain and return a corresponding <code>AuthorityId</code> 
    * based on a supplied scheme and name.  If a domain does not exist in the
    * domain store, a new domain will be created.
    * @param int the naming authority scheme
    * @param entity the domain name
    */
    public AuthorityId resolveDomainEntry( int scheme, String name )
    {
        try
        {
            return m_home.find_by_domain_key( scheme, name ).authority_id();
        }
        catch( NotFound nf )
        {
            return m_home.create( scheme, name ).authority_id();
        }
    }

   /**
    * Get the short PID of a <code>DomainStorage</code> based on a supplied
    * scheme and name.
    * @param int the naming authority scheme
    * @param entity the domain name
    * @return byte[] short PID
    */
    public byte[] resolveDomainPID( int scheme, String name )
    {
        try
        {
            return m_home.find_by_domain_key( scheme, name ).get_short_pid();
        }
        catch( NotFound nf )
        {
            return m_home.create( scheme, name ).get_short_pid();
        }
    }

   /**
    * Get the short PID of a <code>DomainStorage</code> based on a supplied
    * <code>AuthorityID,<code>.
    * @param id authority naming structure
    * @return byte[] short PID
    */
    public byte[] resolveDomainPID( AuthorityId id )
    {
        return resolveDomainPID( id.authority.value(), id.naming_entity );
    }


   /**
    * Returns an AuthorityID matching a supplied short PID.
    * @param pid short persitent object identifier
    * @return AuthorityId the matching authority ID structure
    */
    public AuthorityId authorityFromPID( byte[] pid )
    {
        try
        {
            return ((DomainStore)m_home.find_by_short_pid( pid )).authority_id();
        }
        catch( NotFound nf )
        {
            throw new DomainRuntimeException(
             "Suppied PID does not match a known domain storage entry.", nf );
        }
    }

   /**
    * Converts a string such as DNS, ISO, IDL, DCS, or OTHER to 
    * the <code>NamingAuthority</code> equivalent.
    * @param scheme the string form of a sheme name
    * @return int the int form of the scheme name
    */
    public static int schemeStringToInt( String scheme )
    { 
         if( scheme.equals("DNS"))
         {
             return RegistrationAuthority.DNS.value();
         }
         else if( scheme.equalsIgnoreCase("ISO"))
         {
             return RegistrationAuthority.ISO.value();
         }
         else if( scheme.equals("DNS"))
         {
             return RegistrationAuthority.DNS.value();
         }
         else if( scheme.equals("IDL"))
         {
             return RegistrationAuthority.IDL.value();
         }
         else if( scheme.equals("DCE"))
         {
             return RegistrationAuthority.DCE.value();
         }
         else
         {
             return RegistrationAuthority.OTHER.value();
         }
    }
}
