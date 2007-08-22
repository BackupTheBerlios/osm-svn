/*
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 26/03/2001
 */

package net.osm.properties;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
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

import org.apache.orb.ORBContext;
import org.apache.pss.Connector;
import org.apache.pss.Session;
import org.apache.pss.DefaultServantManager;
import org.apache.pss.StorageContext;
import org.apache.pss.ORB;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TypeCode;
import org.omg.CosPersistentState.NotFound;
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
import org.omg.CosPersistentState.StorageObject;
import org.omg.CosPersistentState.NotFound;

import org.omg.CosPropertyService.PropertySet;
import org.omg.CosPropertyService.PropertySetDef;
import org.omg.CosPropertyService.PropertySetDefHelper;
import org.omg.CosPropertyService.PropertySetDefPOATie;
import org.omg.CosPropertyService.PropertyModeType ;
import org.omg.CosPropertyService.PropertyDef;
import org.omg.CosPropertyService.PropertyHolder;
import org.omg.CosPropertyService.PropertiesHolder;
import org.omg.CosPropertyService.PropertiesIteratorHolder;

/**
 * <p>The <code>PropertySetDefServantManager</code> provides services supporting the 
 * creation of new <code>PropertySetDef</code> container hoilding an embedded
 * <code>PropertySetDef</code> storage object.</p>
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
 * <p>The <code>ServiceManager</code> value is handed by the super type during which
 * a PSS enabled ORB is resolved, enabling establishment of a PSS connector and 
 * session.</p>
 * <tr>
 * <td width="20%" valign="top">Configurable</td>
 * <td>
 * <p>The <code>Configuration</code> value is handed by the super type.  Configuration
 * arguments defined by default for the <code>PropertySetDefServantManager</code>
 * are detailed below:</p>
 * <pre>
 * 
 *   &lt;configuration>
 * 
 *     <font color="blue"><i>&lt;!--
 *     Service publication
 *     --&gt;</i></font>
 * 
 *     &lt;ior value="repository.ior"/&gt;
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
 *         &lt;storage psdl="PSDL:osm.net/properties/PropertyStorageBase:1.0"
 *           class="net.osm.properties.PropertyStorageBase" /&gt;
 *         &lt;home psdl="PSDL:osm.net/properties/PropertyStorageHomeBase:1.0"
 *           class="net.osm.properties.PropertyStorageHomeBase" /&gt;
 *         &lt;storage psdl="PSDL:osm.net/properties/PropertySetDefStorageBase:1.0"
 *           class="net.osm.properties.PropertySetDefStore" /&gt;
 *         &lt;storage psdl="PSDL:osm.net/properties/PropertySetDefStorageHomeBase:1.0"
 *           class="net.osm.properties.PropertySetDefStorageHomeBase" /&gt;
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
 * <p>Handles control to the supertype initialization following which the implementation
 * resolves the <code>ContainerStorageHome</code>. </p>
 * </td></tr>
 * 
 * <tr><td  valign="top">Executable</td>
 * <td>
 * <p>Utility phase used for validation of <code>PropertySetDef</code> creation,
 * <code>Property</code> attribition, modification and listing of the set.</p>
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
public class PropertySetDefServantManager extends DefaultServantManager 
implements PropertiesService, Initializable, Executable, Disposable
{

    //=======================================================================
    // state
    //=======================================================================

    private PropertySetDefStorageHome m_home;
    private POA m_poa;
    
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
	  super.initialize();
	  try
	  {
            if( getLogger().isDebugEnabled() ) getLogger().debug( "initialization" );
            m_home = (PropertySetDefStorageHome) super.getSession().find_storage_home( 
	        "PSDL:osm.net/properties/PropertySetDefStorageHomeBase:1.0" );
	  }
	  catch( Throwable throwable )
	  {
	      String error = "unable to complete supplier initialization due to a unexpected error";
		throw new Exception( error, throwable );
	  }
    }

    //=======================================================================
    // Executable
    //=======================================================================

   /**
    * Executes validation of property container creation, property attribution and listing.
    * The implementation demonstrates the creation of a new <code>PropertySerDef</code>,
    * the assiciation of three properties (email address, web url, and current date).
    * If the email and url properties are already defined they will be updated with 
    * the supplied property values.  The last update properly will always be updated 
    * with the current system time. 
    */
    public void execute() throws Exception
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug( "execution" );

        PropertySetDefStorage store = createPropertySetDefStorage();
        PropertySetDef props = getPropertySetDefReference( store );

        getLogger().info( "created new PropertySetDef" );

        try
        {

            Any email = ORB.init().create_any();
            Any web = ORB.init().create_any();
            email.insert_string("info@osm.net");
            web.insert_string("http://home.osm.net");

            props.define_property( "email", email );
            props.define_property( "web", web );
        }
        finally
        {
            Any update = ORB.init().create_any();
            update.insert_string("" + new java.util.Date().toString());
            props.define_property( "update", update );
        }

        getLogger().info( listProperties( props ) );

        store.destroy_object();
        getLogger().info( "delete PropertySetDef" );
        
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
    }

    //=======================================================================
    // DefaultServantManager
    //=======================================================================

   /**
    * Method by which derived types can override POA creation.
    * @return POA the portable object adapter
    */
    protected POA createPOA() throws Exception
    {
        m_poa = super.createPOA();
        return m_poa;
    }

   /**
    * Returns the name to be assigned to the POA on creation.
    * @return String the POA name
    */
    protected String getPoaName()
    {
        return "PROPERTY_CONTAINER";
    }

   /**
    * Returns a servant implmentation.
    * @return Servant a managable servant
    */
    protected Servant createServant( StorageContext context, Configuration config, ServiceManager manager ) throws Exception
    {
        PropertySetDefDelegate delegate = new PropertySetDefDelegate();
        Servant servant = new PropertySetDefPOATie( delegate );
        Logger log = getLogger().getChildLogger( "" + System.identityHashCode( delegate ) );
        LifecycleHelper.pipeline( delegate, log, context, config, manager );
        return servant;
    }

    //=======================================================================
    // PropertiesService
    //=======================================================================
    
   /**
    * Creation of a new <code>PropertySetDefStorage</code> object.
    * @return PropertySetDefStorage the property set def storage object
    * @exception PropertyException
    */
    public PropertySetDefStorage createPropertySetDefStorage() 
    throws PropertyException
    {
        return m_home.create( 
          new TypeCode[0], new PropertyDef[0], new byte[0][] );
    }

   /**
    * Return an existing <code>PropertySetDefStorage</code> object given 
    * a short PID.
    * @param pid the property set def short pid
    * @return PropertySetDefStorage the property set def storage object
    * @exception NotFound
    */
    public PropertySetDefStorage getPropertySetDefStorage( byte[] pid ) 
    throws NotFound
    {
        return (PropertySetDefStorage) m_home.find_by_short_pid( pid );
    }

   /**
    * Locate a new <code>PropertySetDef</code> based on a supplied short PID.
    * @param pid property set short pid value
    * @exception NotFound if the property set does not exist
    */
    public PropertySetDef getPropertySetDefReference( byte[] pid  ) 
    {
        return PropertySetDefHelper.narrow(
          m_poa.create_reference_with_id( pid, PropertySetDefHelper.id() ) );
    }
    
   /**
    * Create an object reference to a <code>PropertySetDef</code> using a 
    * supplied property set storage object.
    * @param store a protperty set def storage object
    */
    public PropertySetDef getPropertySetDefReference( PropertySetDefStorage store )
    {
        return getPropertySetDefReference( store.get_pid());
    }

    private String listProperties( PropertySet set )
    {
        PropertiesHolder holder = new PropertiesHolder();
	  PropertiesIteratorHolder iterator = new PropertiesIteratorHolder();
        set.get_all_properties( 0, holder, iterator );
	  PropertyHolder props = new PropertyHolder();
        StringBuffer buffer = new StringBuffer();
        buffer.append("Listing properties:");
	  while( iterator.value.next_one( props ) )
	  {
	      String name = props.value.property_name;
		String value = props.value.property_value.extract_string();
		buffer.append("\nproperty: " + name + ", " + value );
	  }
	  iterator.value.destroy();
        return buffer.toString();
    }
}
