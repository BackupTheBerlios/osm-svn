
package net.osm.hub.gateway;

import java.io.File;
import java.util.Random;
import java.util.Hashtable;
import java.util.Vector;
import java.security.Principal;
import javax.security.auth.x500.X500Principal;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.Logger;

import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CORBA_2_3.ORB;
import org.omg.CosTime.TimeUnavailable;
import org.omg.CosTime.TimeService;
import org.omg.CosLifeCycle.NVP;
import org.omg.CosLifeCycle.NoFactory;
import org.omg.CosLifeCycle.InvalidCriteria;
import org.omg.CosLifeCycle.CannotMeetCriteria;
import org.omg.CosNaming.NameComponent;
import org.omg.CosPersistentState.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.IsPartOf;
import org.omg.Session.ProducedBy;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.User;
import org.omg.Session.UserHelper;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.GenericCriteria;
import org.omg.CommunityFramework.CommunityCriteria;
import org.omg.CommunityFramework.Problem;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CommunityFramework.UserCriteria;
import org.omg.CommunityFramework.MessageCriteria;

import net.osm.dpml.DPML;
import net.osm.list.List;
import net.osm.list.LinkedList;
import net.osm.list.LinkedListBase;
import net.osm.hub.pss.*;
import net.osm.hub.home.*;
import net.osm.hub.home.ResourceFactory;
import net.osm.hub.home.ResourceFactoryHelper;
import net.osm.hub.home.ResourceFactoryPOA;
import net.osm.hub.gateway.FactoryException;
import net.osm.hub.gateway.GatewayRuntimeException;
import net.osm.hub.user.UserService;
import net.osm.orb.ORBService;
import net.osm.realm.PrincipalManager;
import net.osm.realm.PrincipalManagerHelper;
import net.osm.realm.StandardPrincipal;
import net.osm.util.Incrementor;
import net.osm.util.XMLFilenameFilter;
import net.osm.util.X500Helper;
import net.osm.util.ExceptionHelper;

/**
* ResourceFactory is a general utility exposable by FactoryFinder 
* interfaces on Session::Workspace and Session::User interfaces.  
* ResourceFactory creates new instances of AbstractResource and derived 
* types based on a supplied name and Criteria. 
*/

public class ResourceFactoryServant extends ResourceFactoryPOA 
implements LogEnabled, Composable, Configurable, Initializable, Registry 
{
    
    //=======================================================================
    // state
    //=======================================================================

   /**
    * Logger assigned to this instance.
    */
    protected Logger log;

   /**
    * CORBA object reference to this factory.
    */
    protected ResourceFactory factory;

   /**
    * Directory containing factory DPML files.
    */
    protected File dpml;

   /**
    * Configuration profile supplied to the factory during the configuration stage.
    */
    protected Configuration configuration;

   /**
    * Hashtable containing the criteria published by this factory.
    */
    protected Hashtable criteria = new Hashtable();

   /**
    * The pricipal identity manager from which the current principal can be established.
    */
    private PrincipalManager manager;

    private Hashtable registry = new Hashtable();

    private Hashtable repository = new Hashtable();

    private boolean initialized = false;

    private ORB orb;


    //=======================================================================
    // Loggable
    //=======================================================================

   /**
    * Sets the logging channel.
    * @param logger the logging channel
    */
    public void enableLogging( final Logger logger )
    {
        if( logger == null ) throw new NullPointerException("null logger argument");
        log = logger;
    }

   /**
    * Returns the logging channel.
    * @return Logger the logging channel
    * @exception IllegalStateException if the logging channel has not been set
    */
    public Logger getLogger()
    {
        if( log == null ) throw new IllegalStateException("logging has not been enabled");
        return log;
    }

    //=================================================================
    // Composable
    //=================================================================
    
    /**
     * The compose operation handles the aggregation of dependent services:
     * @param manager The <code>ComponentManager</code> which this
     *                <code>Composer</code> uses.
     */
    public void compose( ComponentManager manager )
    throws ComponentException
    {
	  factory = ((ResourceFactoryService) manager.lookup("FACTORY")).getResourceFactory();
	  orb = ((ORBService) manager.lookup("ORB")).getOrb();
    }

    //=======================================================================
    // Configurable
    //=======================================================================

   /**
    * Handles static server configuration.
    */
    public void configure( Configuration config ) throws ConfigurationException
    {
	  this.configuration = config;
    }

    //=================================================================
    // Initializable
    //=================================================================
    
   /**
    * Initialization is invoked by the gateway following configuration and 
    * contextualization phases.
    */
    public void initialize()
    throws Exception
    {
        initialized = true;
    }

    //============================================================================
    // Registry
    //============================================================================

   /**
    * Register a criteria and the supporting factory.
    * @param criteria the <code>Criteria</code> supported by a server
    * @param source the factory supporting resource creation for the criteria
    *   identified by the label
    */
    public void register( Criteria criteria, FactoryService source )
    {
        registry.put( criteria.label, source );
        repository.put( criteria.label, criteria );
    }

    //============================================================================
    // ResourceFactory
    //============================================================================

   /**
    * Returns a Criteria instance based on a supplied criteria label.  
    * @exception CriteriaNotFound if the supplied label is unknown by the factory
    */
    public Criteria criterion( String label ) throws CriteriaNotFound
    {
        Object c = repository.get( label );
	  if( c != null ) return (Criteria) c;
        throw new CriteriaNotFound( label );
    }

    //============================================================================
    // org.omg.CommunityFramework.ResourceFactory
    //============================================================================

   /**
    * Returns a list of the Criteria instances supported by this 
    * factory implementation.
    */
    public Criteria[] supporting()
    {
        return (Criteria[]) repository.values().toArray( new Criteria[0] );
    }

   /**
    * Creation of a new instance of AbstractResource based on a supplied
    * name and criteria argument.
    *
    * @param name the name to be assigned to the created resource
    * @param criteria an instance of criteria defining the creation requirements
    * @osm.warning collaboration, vote and engagement not implemented
    */
    public AbstractResource create( String name, Criteria criteria ) throws ResourceFactoryProblem
    {

        FactoryService source = (FactoryService) registry.get( criteria.label );
        if( source != null ) try
        {
            return source.create( name, criteria );
        }
        catch( Throwable e )
        {
	      final String error = "failed to create the requested resource";
		if( getLogger().isWarnEnabled() ) getLogger().warn( error, e );
		throw new ResourceFactoryProblem( 
		  factory, 
		  new Problem( getClass().getName(), error, e )
		);
        }
	  else
	  {
	      final String error = "cannot match criteria '" + criteria.label + "' with a factory";
		if( getLogger().isWarnEnabled() ) getLogger().warn( error );
		Problem problem = new Problem( error );
		throw new ResourceFactoryProblem( factory, problem );		
	  }
    }

   /**
    * Returns a client principal invoking the operation.
    * @return StandardPrincipal the client principal
    */
    private StandardPrincipal getPrincipal() throws Exception
    {
        if( manager == null ) manager = PrincipalManagerHelper.narrow( 
			orb.resolve_initial_references( "PrincipalManager" ) );
	  return manager.getPrincipal();
    }
}
