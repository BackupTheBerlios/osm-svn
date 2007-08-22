
/*
* BasicRealm.java
* Needs to be cleaned up in terms of exception management, general 
* utility methods and inclusion of javadoc comments.  Management of the
* configuration should be seperated out to an independent class.  That 
* class should extend a generalized ORB node handler class.
*/

package net.osm.security.realm;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.Principal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.osm.service.realm.Authenticator;
import net.osm.service.realm.AuthenticatorHelper;
import net.osm.util.IOR;
import net.osm.util.ORBConfigurationHelper;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.ConnectorRegistry;
import org.omg.CosPersistentState.ConnectorRegistryHelper;
import org.omg.CosPersistentState.Connector;
import org.omg.CosPersistentState.Session;
import org.omg.CosPersistentState.Parameter;
import org.omg.CosPersistentState.READ_WRITE;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POAHelper;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.Container;
import org.apache.catalina.Realm;
import org.apache.catalina.util.MD5Encoder;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.realm.RealmBase;

/**
 * BasicRealm is the client authentication interface to the server side 
 * realm implementation.  BasicRealm communicates with the realm server 
 * using IIOP for the purpose of pricipal authentication and access to 
 * a pricipals role bindings.
 */

public class BasicRealm implements Lifecycle, Realm 
{

   /**
    * The default ORB class.
    */
    protected static final String DEFAULT_ORB_CLASS  = "org.openorb.CORBA.ORB";

   /**
    * The default ORB Singleton class.
    */
    protected static final String DEFAULT_ORB_SINGLETON = "org.openorb.CORBA.ORBSingleton";

   /**
    * Descriptive information about this Realm implementation.
    */
    protected static final String REALM_INFO = "net.osm.security.Realm/1.0";

   /**
    * The Container with which this Realm is associated.
    */
    protected Container container = null;

   /**
    * The property change support for this component.
    */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

   /**
    * The lifecycle event support for this component.
    */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

   /**
    * Has this component been started?
    */
    protected boolean started = false;

   /**
    * path to the realm configuration.
    */
    protected File path = new File("src/etc/realm.xml");

   /**
    * Root directory containing the realm configuration, used as a 
    * relative directory for any path declarations contained in the 
    * configuration file.
    */
    protected File root = new File("src/etc");

   /**
    * Configuration derived from the path.
    */
    protected Configuration config;

   /**
    * Properties to be passed to the ORB (established through configuration values).
    */
    protected Properties properties;

   /**
    * Static reference to the current ORB.
    */
    ORB orb;

   /**
    * Static reference to the Authenticator servant.
    */
    Authenticator authenticator;


    public BasicRealm( )
    {
    }

   /**
    * Sets the configuration file path.
    */

    public void setPath( String p )
    {
	  if( p != null )
	  {
		File working = new File( System.getProperty("user.dir") );
		this.path = new File( working, p );
		this.root = path.getParentFile();
        }
    }

   /**
    * Add a lifecycle event listener to this component.
    *
    * @param listener The listener to add
    */
    public void addLifecycleListener(LifecycleListener listener) 
    {
	lifecycle.addLifecycleListener(listener);
    }

   /**
    * Remove a lifecycle event listener from this component.
    *
    * @param listener The listener to remove
    */
    public void removeLifecycleListener(LifecycleListener listener) 
    {
	lifecycle.removeLifecycleListener(listener);
    }

   /**
    * Set the Container with which this Realm has been associated.
    *
    * @param container The associated Container
    */
    public void setContainer(Container container)
    {
	  Container oldContainer = this.container;
	  this.container = container;
	  support.firePropertyChange("container", oldContainer, this.container);
    }

   /**
    * Return the Container with which this Realm has been associated.
    */
    public Container getContainer()
    {
        return this.container;
    }

   /**
    * Return descriptive information about this Realm implementation and
    * the corresponding version number, in the format
    * <code>&lt;description&gt;/&lt;version&gt;</code>.
    */
    public String getInfo()
    {
        return REALM_INFO;
    }

   /**
    * Add a property change listener to this component.
    *
    * @param listener The listener to add
    */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
	support.addPropertyChangeListener(listener);
    }

   /**
    * Remove a property change listener from this component.
    *
    * @param listener The listener to remove
    */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
	  support.removePropertyChangeListener(listener);
    }

   /**
    * Prepare for the beginning of active use of the public methods of this
    * component.  This method should be called before any of the public
    * methods of this component are utilized.  It should also send a
    * LifecycleEvent of type START_EVENT to any registered listeners.
    *
    * @exception IllegalStateException if this component has already been
    *  started
    * @exception LifecycleException if this component detects a fatal error
    *  that prevents this component from being used
    */
    public synchronized void start() throws LifecycleException
    {

	  System.out.println("REALM: start");

	  // Validate and update our current component state
	  if (started) throw new IllegalStateException("BasicRealm already started");

	  if( path == null ) throw new LifecycleException("null configuration, cannot proceed");
	  if( !path.exists() ) throw new LifecycleException("configuration not found, " + path.toString() );

	  try
	  {
	      DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder( );
		config = builder.buildFromFile( path );
        }
	  catch(Exception e)
        {
		e.printStackTrace();
	  	throw new LifecycleException("failed to load realm configuration, " + path, e );
        }

	  try
	  {
		Configuration orbConfig = config.getChild("orb");
	      ORBConfigurationHelper helper = new ORBConfigurationHelper( orbConfig, root );
            properties = helper.getProperties();
        }
	  catch(Exception e)
        {
		//e.printStackTrace();
	  	throw new LifecycleException("failed to establish ORB properties, " + path, e );
        }

	  try
	  {
            orb = ORB.init( new String[0], properties );
	  }
	  catch( Throwable e )
        {
		e.printStackTrace();
            throw new LifecycleException("failed to instantiate the ORB", e);
        }

        //
        // get the reference to the authenticator 
        //

	  String ior = config.getAttribute("ior","file:./realm.ior");
	  try
	  {
            authenticator = AuthenticatorHelper.narrow( IOR.readIOR( orb, ior ) );
        }
        catch( Exception e )
	  {
		throw new LifecycleException( e.toString(), e );
        }
        
	  lifecycle.fireLifecycleEvent(START_EVENT, null);
	  started = true;
	  System.out.println( "Realm association complete." );

    }

   /**
    * Return the Principal associated with the specified username and
    * credentials, if there is one; otherwise return <code>null</code>.
    *
    * @param username Username of the Principal to look up
    * @param credentials Password or other credentials to use in
    *  authenticating this username
    */
    public Principal authenticate( String username, String credentials )
    {

	  System.out.println("authenticate, " + username + ", " + credentials );

	  try
	  {
		if( authenticator.authenticate( username, credentials ))
            {
	 	    return new BasicPrincipal( this, username );
		}
		else
		{
		    return null;
		}
        }
	  catch( Exception e )
        {
		return null;
        }
    }
  

   /**
    * Return the Principal associated with the specified username and
    * credentials, if there is one; otherwise return <code>null</code>.
    *
    * @param username Username of the Principal to look up
    * @param credentials Password or other credentials to use in
    *  authenticating this username
    */
    public Principal authenticate(String username, byte[] credentials)
    {
        return null;
    }

   /**
    * Return the Principal associated with the specified username, which
    * matches the digest calculated using the given parameters using the 
    * method described in RFC 2069; otherwise return <code>null</code>.
    * 
    * @param username Username of the Principal to look up
    * @param digest Digest which has been submitted by the client
    * @param nonce Unique (or supposedly unique) token which has been used
    * for this request
    * @param realm Realm name
    * @param md5a2 Second MD5 digest used to calculate the digest : 
    * MD5(Method + ":" + uri)
    */
    public Principal authenticate(
	String username, String digest, String nonce, String nc, String cnonce, String qop, String realm, String md5a2 )
    {
        return null;
    }

   /**
    * Return <code>true</code> if the specified Principal has the specified
    * security role, within the context of this Realm; otherwise return
    * <code>false</code>.
    *
    * @param principal Principal for whom the role is to be checked
    * @param role Security role to be checked
    */
    public boolean hasRole(Principal principal, String role)
    {
	  return authenticator.verify( principal.getName(), role );
    }

   /**
    * Terminates the real access.
    */

    public synchronized void stop() throws LifecycleException 
    {
	  if (!started) throw new IllegalStateException("BasicRealm already stopped");
	  lifecycle.fireLifecycleEvent(STOP_EVENT, null);

	  //
        // clean up state members established during the start phase
        //

	  config = null;
        properties = null;
        orb = null;
        authenticator = null;

	  started = false;
    }


    public static void main( String[] args )
    {
        BasicRealm realm = new BasicRealm();

	  try
        {
	      System.out.println("starting the realm");
	      realm.start();
	      System.out.println("started the realm");
        }
        catch( LifecycleException e )
        {
	      System.out.println("ERROR: failed to start the realm");
		e.printStackTrace();
		System.exit(0);
        }

	  try
        {
	      System.out.println("stopping the realm");
	      realm.stop();
	      System.out.println("stopped the realm");
        }
        catch( LifecycleException e )
        {
	      System.out.println("ERROR: failed to stop the realm");
		e.printStackTrace();
		System.exit(0);
        }
    }
}
