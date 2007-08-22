/* 
 * ORBConfigurationHelper.java
 */

package net.osm.orb;

import java.net.URL;
import java.io.File;
import java.util.Properties;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Utility class that provides supporting operations related to the ORB 
 * element encountered in configurationi files.  This class can be 
 * instantiated by passing a Configiration instance to the constructor, 
 * where the configuration instance is a node named 'ORB'.  The class 
 * provides a set of operations that simplfy the extraction of properties
 * and other ORB related information - enabling simplificatiuon of 
 * configuration code that is depenedent on the establishement of an ORB
 * runtime environment.
 *
 * @author Stephen McConnell
 */

public class ORBConfigurationHelper
{

    //====================================================================
    // state
    //====================================================================

    private static final boolean trace = false;

   /**
    * The default ORB class.
    */
    protected static final String DEFAULT_ORB_CLASS  = "org.openorb.CORBA.ORB";

   /**
    * The default ORB Singleton class.
    */
    protected static final String DEFAULT_ORB_SINGLETON = "org.openorb.CORBA.ORBSingleton";

   /**
    * The Configuration instance supplied to the constructor.
    */
    protected Configuration configuration;

   /**
    * Root directory from which file and URL relative reference shall be resolved.
    */
    protected File root;

    //====================================================================
    // Constructors
    //====================================================================

    public ORBConfigurationHelper( Configuration config )
    {
        this( config, new File( System.getProperty("user.dir") ) );
    }

    public ORBConfigurationHelper( Configuration config, File root )
    {
        final String nullError = "null configuration supplied to constructor";
        final String badName = "configuration element is not named 'orb'";
        if( config == null ) throw new RuntimeException( nullError );
        String name = config.getName();
        if( !name.equals("orb") ) throw new RuntimeException( badName );
        this.configuration = config;
        this.root = root;
    }

    //====================================================================
    // implementation
    //====================================================================

   /**
    * Return a Properties instance based on the ORB class and singleton delcarations 
    * together with containing property declarations.  The properties instance is 
    * provided in a form suitable for passing to an ORB.init() method.
    */

    public Properties getProperties( ) throws Exception 
    {
	  Properties p = new Properties();
	  String orbClass = configuration.getAttribute("org.omg.CORBA.ORBClass", DEFAULT_ORB_CLASS );
	  String orbSingleton = configuration.getAttribute("org.omg.CORBA.ORBSingletonClass", DEFAULT_ORB_SINGLETON );
	  p.setProperty("org.omg.CORBA.ORBClass", orbClass );
	  p.setProperty("org.omg.CORBA.ORBSingletonClass", orbSingleton );

	  //
	  // resolve any ORB specific properties
	  //

	  Configuration[] props = configuration.getChildren("property");
	  for( int i = 0; i< props.length; i++ )
        {
		Configuration child = props[i];

		//
		// every property must have a name
		//

		String name = "";
		try
		{
		    name = child.getAttribute("name");
		}
		catch( ConfigurationException noName )
	      {
		    final String error = "encountered a property without a name";
		    throw new Exception ( error, noName );
		}

		//
		// The value of a property is either declared directly under a value attribute, 
		// or indirectory under a 'file' attribute.  In the case of 'file' attributes
		// we need to resolve this relative to this file before setting the 
		// property value.
		//

		String value = "";
		try
		{
		    value = child.getAttribute("value");
		}
		catch( ConfigurationException noValueAttribute )
	      {
		    try
		    {
		        final String s = child.getAttribute("file");
			  File f = new File( root, s );
			  value = f.getAbsolutePath();
		    }
		    catch( ConfigurationException noFileAttribute )
		    {
			  String s = null;
			  try
			  {
		            s = child.getAttribute("url");
			  }
			  catch( Exception noURL )
			  {
				final String error = "Found a property without a 'value', 'file' or 'url' attribute";
		            throw new Exception( error, noURL );
			  }
			  if( s.startsWith("file:"))
                    {
			      try
			      {
				    URL base = root.toURL();
			          URL url = new URL( base, s );
			          value = url.toString();
			          if( trace ) System.out.println( "URL: " + value );
			      }
			      catch( Exception unknown )
			      {
			  	    final String error = "Unexpected exception while creating file:// URL value.";
		                throw new Exception( error, unknown );
				}
			  }
			  else
			  {
			      try
			      {
			          URL url = new URL( s );
			          if( trace ) System.out.println( "URL: " + url );
			          value = url.toString();
			          if( trace ) System.out.println( "URL/value: " + value );
			      }
			      catch( Exception unknown )
			      {
			  	    final String error = "Unexpected exception while creating URL value.";
		                throw new Exception( error 
					+ "\n" + "cause: " + unknown.getClass().getName() + ", " 
					+ "\n" + unknown.getMessage(), unknown );
				}
			  }
		    }
		}
		p.setProperty( name, value );
        }
        return p;
    }
}
