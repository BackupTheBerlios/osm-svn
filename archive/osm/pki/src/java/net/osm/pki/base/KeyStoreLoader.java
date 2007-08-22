package net.osm.pki.base;

import java.io.*;
import java.security.*;
import java.security.cert.*;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.AvalonFormatter;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.log.output.io.FileTarget;
import org.apache.log.Hierarchy;

public class KeyStoreLoader implements LogEnabled, Contextualizable, Configurable, Initializable
{

    //=========================================================
    // static
    //=========================================================

    private final static String  DEFAULT_FORMAT =
        "%{time} [%7.7{priority}] (%{category}): %{message}\\n%{throwable}";

    //================================================================
    // state
    //================================================================

   /**
    * Internal reference to the log channel.
    */
    private Logger log;

   /**
    * Internal reference to the configuration.
    */
    private Configuration configuration;

   /**
    * Internal reference to the initalization state.
    */
    private boolean initialized;

   /**
    * Internal reference to the name of the root alias.
    */
    private String alias;

   /**
    * Internal reference to an in-memory keystore.
    */
    private KeyStore keyStore;

   /**
    * Internal reference to the supplied keystore password used to 
    * load and save the keystore.
    */
    private char[] password;

   /**
    * Application context
    */
    Context context;

   /**
    * The keystore file path used during load and save to construct the 
    * file input and output streams.
    */
    private File file;

    //================================================================
    // constructors
    //================================================================

   /**
    * Constructs a new keystore loader.
    */
    public KeyStoreLoader( )
    {
    }

    //================================================================
    // LogEnabled implementation
    //================================================================

   /**
    * Sets the logging channel.
    *
    * @param logger Logger to direct log entries to
    */ 
    public void enableLogging( final Logger logger )
    {
        log = logger;
    }

    //=================================================================
    // Contextualizable
    //=================================================================

    public void contextualize( Context context ) throws ContextException
    {
	  this.context = context;
    }

    //================================================================
    // Configurable implementation
    //================================================================
    
   /**
    * Configuration of the runtime environment based on a supplied 
    * Configuration arguments including the file path of the keystore 
    * and the keystore password.
    *
    * @param config keystore configuration block
    * @exception ConfigurationException if the supplied configuration is badly formed.
    */
    public void configure( final Configuration config )
    throws ConfigurationException
    {
        if( configuration != null ) throw new ConfigurationException(
		"Attempt to re-configure a static configuration.");
        if( context == null ) throw new ConfigurationException(
		"Attempt to configure KeyStoreLoader without contextualization.");

        try
	  {
            configuration = config;
		if( configuration.getAttribute("password") != null )
            {
                password = new String(configuration.getAttribute("password")).toCharArray();
		}
	      else
		{
		    throw new ConfigurationException(
			"Keystore password is unknown.");
		}
	      alias = configuration.getAttribute("alias");
		if( alias == null ) throw new ConfigurationException(
			"Keystore alias is unknown.");
            try
            {
		    File root = (File) context.get("app.home");
	          file = new File( root, configuration.getAttribute("file") );
            }
            catch( ContextException ce )
            {
		    File root = new File( System.getProperty("user.dir") );
	          file = new File( root, configuration.getAttribute("file") );
            }
        }
	  catch( Exception e )
        {
            throw new ConfigurationException("Failed to configure the keystore", e );
        }
    }

    //================================================================
    // Initializable
    //================================================================
    
   /**
    * Initialization is invoked by the framework following configuration, during which 
    * the keystore is loaded into memory based on the supplied configuration.
    */
    public void initialize()
    throws Exception
    {

        //
        // Load from the supplied location
        //

        FileInputStream input = null;

	  if( file.exists() )
	  {
		if( file.isDirectory() ) throw new IOException("Keystore filename " + file + " is a directory.");
	      if( !file.canRead() ) throw new IOException("Cannot read the keystore " + file );
		try
	      {
		    input = new FileInputStream( file );
		}
	      catch( Exception e )
	      {
		    throw new Exception("Failed to open the keystore on " + file, e );
	      }
        }
            
        try 
        {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load( input, password );
        }
        catch (Exception e) 
        {
            throw new Exception("Failed to load the keystore form " + file, e );
        }

        initialized = true;
    }

   /**
    * Accessor to the in-memory vault.
    *
    * @return KeyStore
    */
    public KeyStore getKeyStore() 
    {
        return keyStore;
    }

   /**
    * Store the keystore to the location supplied under the keystore 
    * configuration.
    *
    * @exception FileNotFoundException
    * @exception KeyStoreException
    * @exception IOException
    * @exception NoSuchAlgorithmException
    * @exception CertificateException
    */
    public void save( ) 
    throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException 
    {
        FileOutputStream fileOutputStream = new FileOutputStream( file );
        keyStore.store(fileOutputStream, password );
        fileOutputStream.close();
    }

   /**
    * The <code>boot</code> method enables configuration, initialization and startup
    * of the agent server when running outside of a server management framework.
    *
    * @param args sequence of string arguments
    */
    public static void main(String args[]) 
    {

        KeyStoreLoader loader = new KeyStoreLoader();

	  Logger log = createLogger( "vault.log", "VAULT" );
	  Configuration conf = getConfiguration( null, args[0] );
	  Configuration config = conf.getChild("authority").getChild("registration-authority").getChild("vault");

	  try
	  {
            loader.enableLogging( log );
            loader.configure( config );
            loader.initialize();
		loader.save();
        }
	  catch( Exception e )
	  {
		String error = "Lifecycle failure.";
            log.error( error , e );
		throw new RuntimeException( error, e );
        }

	  try
	  {
            KeyStore keyStore = loader.getKeyStore();
		String alias = config.getAttribute("alias");
            if (keyStore.isKeyEntry(alias)) 
            {
                System.out.println(alias + " is a key entry in the keystore");
		    String passwordString = config.getAttribute("password");
                char c[] = new char[ passwordString.length() ];
                passwordString.getChars(0, c.length, c, 0);

                System.out.println("The private key for " + alias + 
                            " is " + keyStore.getKey( alias, c ));
                java.security.cert.Certificate certs[] = keyStore.getCertificateChain( alias );
		    System.out.println("CERTS.LENGTH: " + certs.length );
                if (certs[0] instanceof X509Certificate) 
		    {
                    X509Certificate x509 = (X509Certificate) certs[0];
                    System.out.println(alias + " is really " +
                        x509.getSubjectDN());
                }
                if (certs[certs.length - 1] instanceof
                                     X509Certificate) {
                    X509Certificate x509 = (X509Certificate) 
                                        certs[certs.length - 1];
                    System.out.println(alias + " was verified by " +
                        x509.getIssuerDN());
                }
            }
            else if (keyStore.isCertificateEntry( alias )) 
            {
                System.out.println( alias +
                            " is a certificate entry in the keystore");
                java.security.cert.Certificate c = keyStore.getCertificate( alias );
                if (c instanceof X509Certificate) {
                    X509Certificate x509 = (X509Certificate) c;
                    System.out.println( alias + " is really " +
                        x509.getSubjectDN());
                    System.out.println( alias + " was verified by " +
                        x509.getIssuerDN());
                }
            }
            else 
            {
                System.out.println( alias +
                        " is unknown to this keystore");
            }

        }
	  catch( Exception e )
	  {
		String error = "Lifecycle failure.";
            log.error( error , e );
		throw new RuntimeException( error, e );
        }
    }

   /**
    * Returns the configuration resource.
    *
    * @param 
    */
    private static Configuration getConfiguration( ClassLoader classLoader, String path ) 
    {
        InputStream is = null;
	  DefaultConfigurationBuilder builder = null;
	  try
	  {
	      builder = new DefaultConfigurationBuilder( );
		if( classLoader != null )
		{
	          is = classLoader.getResourceAsStream( path );
		}
		else
		{
		    is = new FileInputStream( path );
		}
        }
	  catch(Exception e)
        {
	      throw new RuntimeException("Failed to read configuration from path " + path, e );
        }
	  if( is == null ) throw new RuntimeException(
		"Could not find the configuration resource \"" + path + "\"");
	  try
	  {
		Configuration c = builder.build( is );
		return c;
        }
	  catch(Exception e)
        {
	      throw new RuntimeException("Failed to build the configuration.", e );
        }
    }

   /**
    * Internal utility to create a logger when running a standalone test.
    *
    * @param filename the filename to use for the log file
    * @param catagory the catagory to assign to the log channel
    * @return Logger - log channel
    */
    private static Logger createLogger( String filename, String catagory )
    {
	  try
	  {
            final AvalonFormatter formatter = new AvalonFormatter( DEFAULT_FORMAT );
            final File file = new File( filename );
            final FileTarget logTarget = new FileTarget( file, false, formatter );

            Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
            hierarchy.setDefaultLogTarget( logTarget );
            org.apache.log.Logger logKitLoggger = hierarchy.getLoggerFor( catagory );
		return new LogKitLogger( logKitLoggger );
        }
        catch( Exception e)
	  {
		throw new RuntimeException("Failed to establish logger.",e);
	  }
    }
 
}
