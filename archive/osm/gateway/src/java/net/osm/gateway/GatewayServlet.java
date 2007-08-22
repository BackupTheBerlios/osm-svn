/*
 */

package net.osm.gateway;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.excalibur.configuration.CascadingConfiguration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AvalonFormatter;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.output.io.FileTarget;
import org.apache.orb.util.LifecycleHelper;
import org.apache.orb.ORB;
import org.apache.orb.corbaloc.Handler;

import net.osm.adapter.Adapter;
import net.osm.adapter.ServiceAdapter;
import net.osm.finder.ObjectNotFound;
import net.osm.finder.InvalidPath;
import net.osm.session.Home;
import net.osm.session.HomeAdapter;
import net.osm.session.HomeHelper;
import net.osm.realm.PrincipalManagerBase;
import net.osm.realm.StandardPrincipalBase;
import net.osm.realm.RealmSingleton;
import net.osm.vault.DefaultVault;
import net.osm.vault.Vault;

/**
 * Simple servlet to validate that the servlet build process is in place.
 */

public final class GatewayServlet extends HttpServlet 
implements HomeAdapter
{
    //==============================================================
    // static
    //==============================================================

    public static final int DISCONNECTED = -1;
    public static final int CONNECTING = 0;
    public static final int CONNECTED = 1;

    private static final String DEFAULT_FORMAT =
        "%{time} [%7.7{priority}] (%{category}): %{message}\\n%{throwable}";

    //==============================================================
    // state
    //==============================================================

    private int mode = DISCONNECTED;
    private int interval = 900;
    private Logger m_logger;
    private Configuration m_config;
    private ConnectionMonitor m_monitor;
    private ORB m_orb;
    private HomeAdapter m_home;
    private Vault m_vault;
    private String m_address;

    //==============================================================
    // LogEnabled
    //==============================================================

   /**
    * Assignment of a logging channel by the container.
    * @param logger the logging channel.
    */
    public void setLogger( Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Returns the assigned logging channel.
    * @return Logger the logging channel.
    */
    protected Logger getLogger()
    {
        return m_logger;
    }

    //==============================================================
    // Servlet
    //==============================================================

   /**
    * The <code>init</code> method invoked by the servlet loader.
    */
    public synchronized void init( ServletConfig conf )
    throws ServletException 
    {

        super.init( conf );

        //
        // load the configuration
        //

        String path = getInitParameter( "configuration", "WEB-INF/config.xml" );
        System.out.println("Gateway initialization: " + path );

        try
        {
            Configuration defaults = loadConfiguration("net/osm/gateway/GatewayServlet.xml");
            Configuration current = null;
            try
            {
                InputStream stream = getServletContext().getResourceAsStream( path ); 
                DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder( ); 
                current = builder.build( stream );
            }
            catch( Throwable e )
            {
                System.out.println("CONFIG-WARNING: " + e.toString() );
                current = new DefaultConfiguration( "config", null );
            }
            m_config = new CascadingConfiguration( current, defaults );
        }
        catch( Throwable e )
        {
            throw new ServletException("Unable to establish gateway configuration", e ); 
        }

        //
        // create the logging channel
        //
        File target = null;
        try
        {
            Configuration logging = m_config.getChild("logging");
            Priority priority = Priority.getPriorityForName( logging.getAttribute( "priority", "DEBUG" ) );
            File appBase = new File( getServletContext().getRealPath( "gateway.log" ) ).getParentFile();
            File logBase = new File( appBase, "../../logs" );
            target = new File( logBase, logging.getAttribute("target", "gateway.log" ));
            m_logger = createLogger( target, priority );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to establish gateway logging channel on target: " + target
              + ".\nGateway initalization aborted.";
            System.err.println( error );
            throw new ServletException( error, e ); 
        }
        
        //
        // create the client ORB
        //

        try
        {
            Configuration orb_config = m_config.getChild("orb");
            Logger logger = m_logger.getChildLogger("orb");
            m_orb = new ORB();
            LifecycleHelper.pipeline( m_orb, logger, new DefaultContext(), orb_config, null );
        }
        catch( Throwable e )
        {
            final String error = "Unable to establish gateway client ORB.";
            m_logger.error( error, e );
            throw new ServletException( error, e ); 
        }
        
        //
        // login to the vault and declare the current principal
        //

        getLogger().debug("vault initalization");
        try
        {
            DefaultVault vault = new DefaultVault();
            vault.enableLogging( getLogger().getChildLogger("vault") );
            vault.configure( m_config.getChild("vault") );
            vault.initialize();
            m_vault = vault;
            m_vault.login();
	      PrincipalManagerBase manager = (PrincipalManagerBase)
			m_orb.resolve_initial_references("PRINCIPAL");
		manager.setLocalPrincipal( new StandardPrincipalBase( m_vault.getCertificatePath() ) );

        }
        catch (Throwable e)
        {
            String error = "principal establishment error";
            throw new ServletException( error, e );
        }

        //
        // launch a thread to establish and monitor the conection
        //

        try
        {
            String address = m_config.getChild("connection").getAttribute( 
              "url", "corbaloc::home.osm.net:2056/session" );
            final URL url = new URL( null, address, new Handler( m_orb ));
            m_monitor = new ConnectionMonitor( url );
            m_monitor.enableLogging( m_logger.getChildLogger("monitor") );
            m_monitor.initialize();
        }
        catch( Throwable e )
        {
            final String error = "Unable to establish gateway client ORB.";
            m_logger.error( error, e );
            throw new ServletException( error, e ); 
        }
        
        //
        // publish ourselves as an attribute in the servlet
        //

        conf.getServletContext().setAttribute("net.osm.session", this );

        System.out.println("Gateway ready." );
    }

    /**
     * Respond to a GET request for the content produced by
     * this servlet.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are producing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
/*
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
      throws IOException, ServletException {

	response.setContentType("text/html");
	PrintWriter writer = response.getWriter();

	writer.println("<html>");
	writer.println("<head>");
	writer.println("<title>Gateway Servlet Page</title>");
	writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"osm.css\" title=\"index\" />");
	writer.println("</head>");
	writer.println("<body bgcolor=#ffffff>");

	writer.println("<p class=\"logo\"><a href=\"index.html\"><img src=\"images/osm.gif\" border=0></a></p>");
	writer.println("<p class=\"title\">gateway servlet</p>");
	writer.println("<hr/>");

	writer.println("<table border=\"0\" width=\"100%\">");
	Enumeration names = request.getHeaderNames();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    writer.println("<tr>");
	    writer.println("  <th align=\"right\">" + name + ":</th>");
	    writer.println("  <td>" + request.getHeader(name) + "</td>");
	    writer.println("</tr>");
	}
	writer.println("</table>");

	writer.println("</body>");
	writer.println("</html>");

    }
*/

   /**
    * Invoked by the servlet container to dispose of this servlet.
    */
    public void destroy()
    {
        m_monitor.dispose();
    }

    //==============================================================
    // services
    //==============================================================

    /**
     * Returns a object resolved from the supplied path.
     * @param  path a string that identifies a path to an object
     * @return  Adapter an adapter backed by an object reference
     * @exception  InvalidPath thrown if the path is invalid
     * @exception  ObjectNotFound thrown if the path cannot be resolved
     */
     public Adapter resolve(String path) 
     throws InvalidPath, ObjectNotFound
     {
         System.out.println("gateway/resolve: " + path );
         return getHome().resolve( path );
     }

    /**
     * Returns a URL for this adapter.
     * @return String a url to this adapter
     */
     public String getURL()
     {
         System.out.println("gateway/getURL" );
         return "./gateway";
     }

     public String getBase()
     {
         System.out.println("gateway/getBase" );
         return "gateway";
     }

    /**
     * Returns a user relative to the undelying principal.
     * @param  policy TRUE if a new should be created if the principal is unknown
     * otherwise, the UnknownPrincipal exception will be thrown if the principal
     * cannot be resolved to a user reference
     * @return  UserAdapter an adapter wrapping a user object reference
     * @exception  UnknownPrincipal if the underlying principal does not
     * match a registered user.
     */
     public net.osm.session.user.PrincipalAdapter resolve_user(boolean policy)
         throws net.osm.session.UnknownPrincipal
     {
         System.out.println("gateway/resolve_user" );
         return getHome().resolve_user( policy );
     }

   /**
    * Returns the set of names supported by the chooser.
    * @return  String[] the set of names
    */
    public String[] getNames()
    {
        return getHome().getNames();
    }

   /**
    * Locates an Adapter to a object reference by name.
    * @param  name the service to lookup
    * @exception  UnknownName if the supplied name is not known by the chooser
    */
    public ServiceAdapter lookup(String name)
        throws net.osm.chooser.UnknownName
    {
        System.out.println("gateway/lookup" );
        return getHome().lookup( name );
    }

    public String[] getKey()
    {
        System.out.println("gateway/getKey" );
        return getHome().getKey();
    }

    /**
     * Returns an iterator of the available services.
     */
    public Iterator getServices()
    {
        System.out.println("gateway/getServices" );
        return getHome().getServices();
    }

    /**
     * Returns the gateway service name.
     */
    public String getName()
    {
        System.out.println("gateway/getName" );
        return getHome().getName( );
    }

    /**
     * Returns the service description.
     */
    public String getDescription()
    {
        System.out.println("gateway/getDescription" );
        return getHome().getDescription();
    }

    /**
     * Returns the primary object reference that the adapter is adapting.
     * @return  org.omg.CORBA.Object object reference
     */
    public org.omg.CORBA.Object getPrimary()
    {
        System.out.println("gateway/getPrimary" );
        return getHome().getPrimary();
    }

    /**
     * Returns the primary corbaloc URL.
     */
    public URL getCorbaloc()
    {
        System.out.println("gateway/getCorbaloc" );
        return getHome().getCorbaloc();
    }

    public String toString()
    {
        try
        {
            return getHome().toString();
        }
        catch( GatewayDisconnectException e )
        {
            return "Service unavailable.";
        }
        catch( Throwable e )
        {
            return "Service error: " + e.toString();
        }
    }

    public String [] _truncatable_ids()
    {
        return getHome()._truncatable_ids();
    }


    //==============================================================
    // utilities
    //==============================================================

    private String getInitParameter( final String name, final String defaultValue )
    {
        final String value = getInitParameter( name );
        if ( null == value )
        {
            return defaultValue;
        }
        else
        {
            return value;
        }
    }

    private Logger createLogger( final File file, Priority priority )
        throws Exception
    {
        final AvalonFormatter formatter = new AvalonFormatter( DEFAULT_FORMAT );
        final FileTarget target = new FileTarget( file, false, formatter );

        final Hierarchy hierarchy = new Hierarchy();
        final org.apache.log.Logger logger = hierarchy.getLoggerFor( "gateway" );
        logger.setLogTargets( new LogTarget[]{ target } );
        logger.setPriority( priority );
        logger.debug( "Logger started" );
        return new LogKitLogger( logger );
    }

   /**
    * Returns a configuration resource form a jar file. 
    * @param path the package path to the 
    * @exception ConfigurationException if there is a problem
    */
    private static Configuration loadConfiguration( String path ) 
    throws ConfigurationException 
    {
        try
        {
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder( );
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream( path );
            if( is != null ) return builder.build( is );
            throw new ConfigurationException( "Could not locate configuration from path: " + path );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected exception while attempting to load configuration from path: ";
            throw new ConfigurationException( error + path, e ); 
        } 
    }

    private HomeAdapter getHome() throws GatewayDisconnectException
    {
        if( !m_monitor.getConnectedState() ) throw new GatewayDisconnectException(
          "Connection unavailable.");
        if( m_home != null ) return m_home;

        Home home = HomeHelper.narrow( m_monitor.getObject() );
        if( home == null ) throw new GatewayDisconnectException(
          "Home connection unavailable." );
        m_home = (HomeAdapter) home.get_adapter();
        return m_home;
    }

    public void doTest( String path )
    {
        org.omg.CORBA.Object object = null;
        try
        {
            System.out.println("CORBALOC: " + path ); 
            object = m_orb.string_to_object( path );
            Home home = HomeHelper.narrow( object );
        }
        catch( Throwable e )
        {
            System.out.println("CORBALOC-ERROR:\n" + e.toString() );
        }
    }

}
