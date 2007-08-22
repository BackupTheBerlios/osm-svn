
package net.osm.session;

import java.util.Iterator;
import java.net.URL;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.orb.ORB;
import org.apache.orb.corbaloc.Handler;
import org.apache.orb.util.IOR;
import org.apache.time.TimeUtils;

import org.omg.Session.AbstractResource;
import org.omg.Session.Workspace;
import org.omg.Session.WorkspaceHelper;
import org.omg.Session.Desktop;
import org.omg.Session.DesktopHelper;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.WorkspaceIterator;
import org.omg.Session.Task;
import org.omg.Session.TaskHelper;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.TaskIterator;
import org.omg.Session.TasksHolder;

import net.osm.adapter.Adapter;
import net.osm.adapter.ServiceAdapter;
import net.osm.finder.Finder;
import net.osm.finder.FinderAdapter;
import net.osm.finder.FinderHelper;
import net.osm.chooser.Chooser;
import net.osm.chooser.ChooserAdapter;
import net.osm.chooser.ChooserHelper;
import net.osm.factory.FactoryAdapter;
import net.osm.factory.Argument;
import net.osm.session.Home;
import net.osm.session.HomeAdapter;
import net.osm.session.HomeHelper;
import net.osm.session.UnknownPrincipal;
import net.osm.session.SessionSingleton;
import net.osm.session.resource.AbstractResourceAdapter;
import net.osm.session.user.User;
import net.osm.session.user.UserHelper;
import net.osm.session.user.UserAdapter;
import net.osm.session.user.PrincipalAdapter;
import net.osm.session.workspace.WorkspaceAdapter;
import net.osm.session.desktop.DesktopAdapter;
import net.osm.session.processor.ProcessorAdapter;
import net.osm.session.task.TaskAdapter;
import net.osm.session.util.CollectionIterator;
import net.osm.realm.PrincipalManagerBase;
import net.osm.realm.StandardPrincipalBase;
import net.osm.realm.RealmSingleton;
import net.osm.vault.Vault;

/**
 */
public class Demonstration extends AbstractLogEnabled
implements LogEnabled, Configurable, Serviceable, Initializable, Executable, Disposable
{

    private Vault m_vault;
    private Configuration m_config;
    private ServiceManager m_manager;
    private HomeAdapter m_home;
    private PrincipalAdapter m_principal;
    private ORB m_orb;
    private URL m_base;

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

    public void service( ServiceManager manager ) throws ServiceException
    {
	  getLogger().debug("service");
        m_manager = manager;
        m_vault = (Vault) m_manager.lookup("vault");
        m_orb = (ORB) m_manager.lookup("orb");
        RealmSingleton.init( m_orb );
        SessionSingleton.init( m_orb );
    }

    //================================================================
    // Initializable
    //================================================================
    
   /**
    */
    public void initialize()
    throws Exception
    {

        //
        // setup the local principal identity
        //

        getLogger().debug("initialization");
        m_vault.login();
        try
        {
	      PrincipalManagerBase manager = (PrincipalManagerBase)
			m_orb.resolve_initial_references("PRINCIPAL");
		manager.setLocalPrincipal( new StandardPrincipalBase( m_vault.getCertificatePath() ) );
        }
        catch (Exception e)
        {
            String error = "principal establishment error";
            throw new SessionException( error, e );
        }

        //
        // start the client ORB
        //

        m_orb.start();

        //
        // get the gateway
        //

        m_base = new URL( null, "corbaloc::home.osm.net:2056/session", new Handler( m_orb ));
        getLogger().debug("connecting to: " + m_base );
        m_home = (HomeAdapter) 
          HomeHelper.narrow( (org.omg.CORBA.Object) m_base.getContent() ).get_adapter();
        m_principal = (PrincipalAdapter) m_home.resolve_user( true );

        //
        // execute the demo
        //

        getLogger().debug("ready");
        try
        {
            execute();
        }
        catch( Throwable e )
        {
            getLogger().error( "testing error", e );
        }

    }

    //================================================================
    // Executable
    //================================================================

   /**
    * Perform session related tests.
    */
    public void execute()
    throws Exception
    {
        getLogger().debug("execution");

        //
        // log the gateway description and available services
        //

        //getLogger().info( "Gateway\n" + m_home.toString() + "\n" );

        try
        {
            System.out.println( "\nTEST 1" );
            String path = "?manager=user&id=" + m_principal.getIdentity();
            URL url = new URL( m_base, path, new Handler( m_orb ));
            System.out.println( "URL: " + url );

            Object content = url.getContent();
            System.out.println( "CLASS: " + content.getClass().getName() );
            System.out.println( "VALUE: " + content );
        }
        catch( Throwable e )
        {
            getLogger().warn("test 1 failure", e );
        }

        try
        {
            System.out.println( "\nTEST 2" );
            String path = "/session#workspace";
            URL url = new URL( m_base, path, new Handler( m_orb ));
            System.out.println( "URL: " + url );

            Object content = url.getContent();
            System.out.println( "CLASS: " + content.getClass().getName() );
            System.out.println( "VALUE: " + content );
        }
        catch( Throwable e )
        {
            getLogger().warn("test 2 failure", e );
        }

        try
        {
            System.out.println( "\nTEST 3" );
            String path = "/session/desktop#" + m_principal.getDesktop().getIdentity();
            URL url = new URL( m_base, path, new Handler( m_orb ));
            System.out.println( "URL: " + url );

            Object content = url.getContent();
            System.out.println( "CLASS: " + content.getClass().getName() );
            System.out.println( "VALUE: " + content );
        }
        catch( Throwable e )
        {
            getLogger().warn("test 3 failure", e );
        }
    }

    //================================================================
    // Disposable
    //================================================================

   /**
    * Dispose of the value.
    */
    public void dispose()
    {
        if( getLogger().isDebugEnabled() ) getLogger().debug("disposal");
        try
        {
            m_orb.stop();
        }
        catch( Throwable e )
        {
            // ignore
        }
        try
        {
            m_orb.dispose();
            m_manager.release( m_vault );
            m_manager = null;
            m_vault = null;
        }
        catch( Throwable e )
        {
            // ignore
        }
    }

    //================================================================
    // utilities
    //================================================================

   /**
    * Utility that recursively scans services contained by a chooser.  If 
    * the service exposed by the chooser is itseld a chooser, then recursive 
    * lookup in invoked against that service, otherwise the service is listed
    * normally.
    */
    private void recursiveLookup( ChooserAdapter chooser ) throws Exception
    {

        getLogger().debug("Listing: " + chooser.getName() + " services" );

        String[] services = chooser.getNames();
        for( int i=0; i<services.length; i++ )
        {
            String name = (String) services[i];
            try
            {
                Adapter service =  chooser.lookup( name );
                if( service instanceof ServiceAdapter )
                {
                    getLogger().info( ((ServiceAdapter)service).getName() 
                      + "\n" + service.toString() + "\n" );

                    if( service instanceof ChooserAdapter )
                    {
                        recursiveLookup( (ChooserAdapter) service );
                    }
                }
                else
                {
                    getLogger().info( "adapter:\n" + service.toString() + "\n" );
                }
            }
            catch( Throwable ce )
            {
                final String error = 
                  "Lookup error from service: " + name;
                throw new SessionException( error, ce );
            }
        }
    }
}
