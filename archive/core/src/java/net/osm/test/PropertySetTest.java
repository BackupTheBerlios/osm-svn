/*
 * @(#)PropertySetTest.java
 *
 * Copyright 2000 OSM SARL. All Rights Reserved.
 * 
 * This software is the proprietary information of OSM SARL.  
 * Use is subject to license terms.
 * 
 * @author  Stephen McConnell
 * @version 1.0 08/04/2001
 */

package net.osm.test;

import java.util.Date;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.DataInputStream;

import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.ProcessorModel;
import org.omg.CollaborationFramework.ProcessorCriteria;
import org.omg.CommunityFramework.Criteria;
import org.omg.CommunityFramework.GenericCriteria;
import org.omg.CommunityFramework.Problem;
import org.omg.CommunityFramework.ResourceFactoryProblem;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Any;
import org.omg.CORBA.StringHolder;
import org.omg.CosLifeCycle.FactoryFinder;
import org.omg.CosLifeCycle.FactoryFinderHelper;
import org.omg.CosLifeCycle.NVP;
import org.omg.CosNotifyComm.StructuredPushSupplier;
import org.omg.CosNotification.EventType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosPropertyService.Property;
import org.omg.CosPropertyService.PropertySet;
import org.omg.CosPropertyService.PropertiesHolder;
import org.omg.CosPropertyService.PropertyHolder;
import org.omg.CosPropertyService.PropertiesIteratorHolder;
import org.omg.CosPropertyService.MultipleExceptions;
import org.omg.CosPropertyService.PropertyNamesHolder;
import org.omg.CosPropertyService.PropertyNamesIteratorHolder;
import org.omg.CosPropertyService.PropertyModeType;
import org.omg.CosPropertyService.PropertyDef;
import org.omg.CosPropertyService.PropertyModesHolder;
import org.omg.CosPropertyService.PropertyMode;
import org.omg.CosPropertyService.PropertyTypesHolder;
import org.omg.CosPropertyService.PropertyDefsHolder;
import org.omg.NamingAuthority.RegistrationAuthority;
import org.omg.NamingAuthority.AuthorityId;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.Session.AbstractResource;
import org.omg.Session.AbstractResourceHelper;
import org.omg.Session.IdentifiableDomainConsumer;
import org.omg.Session.IdentifiableDomainConsumerHelper;
import org.omg.Session.TasksHolder;
import org.omg.Session.TaskIteratorHolder;
import org.omg.Session.WorkspacesHolder;
import org.omg.Session.WorkspaceIteratorHolder;
import org.omg.Session.LinkHelper;
import org.omg.Session.*;
import org.omg.TimeBase.UtcT;

import org.omg.CommunityFramework.UserCriteria;
import org.omg.CommunityFramework.MessageCriteria;
import org.omg.CollaborationFramework.CollaborationSingleton;
import net.osm.hub.home.ResourceFactory;
import net.osm.hub.home.ResourceFactoryHelper;
import net.osm.util.IOR;
import net.osm.time.TimeUtils;


/**
 * General test and validation client application for PropertySet methods exposed by User.
 */

public class PropertySetTest
{
    
    protected static Thread thread;
    protected static POA root;
    protected static ORB orb;
    
    public static void main( String args[] )
    {
        
        System.out.println("\nCopyright, January 2000 - OSM SARL.\n");
        
	  //
        // command line must contain two file paths, the first is the 
        // IOR for the factory finder and the second is the IOR for 
	  // an audit service
        //

        if( args.length < 3 ) throw new RuntimeException("bad argument set");

	  String FINDER_STRING = args[0];
	  String AUDIT_STRING = args[1];

        //
        // initialize client
        //
        
        orb = ORB.init( args,null );
        CollaborationSingleton.init( orb );
        FactoryFinder finder = null;
        ResourceFactory factory = null;
        
        // 
	  // locate the factory finder
        //

        try
        {
            System.out.println("\n\tLocating finder." );
            finder = FactoryFinderHelper.narrow( IOR.readIOR( orb, FINDER_STRING ) );
            NameComponent k = new NameComponent( AbstractResourceHelper.id(), "factory" );
            NameComponent[] key = new NameComponent[]{k};
            org.omg.CORBA.Object[] factories = finder.find_factories( key );
            factory = ResourceFactoryHelper.narrow( factories[0] );
        }
        catch( Exception e )
        {
            System.out.println ("Unexpected exception while resolving factory.");
            e.printStackTrace();
            System.exit(0);
        }
        
        //
        // commence testing
        //

	  PropertySetTest c = new PropertySetTest();

	  for( int i=0; i<1; i++ )
	  {
		try
		{
		    System.out.println("\ncycle: " + i + "\n");
	          c.test( finder, factory );
		}
		catch( Exception e )
		{
		    e.printStackTrace();
		}
	  }

        // exit the test

	  System.exit(0);

    }


    public void test( FactoryFinder finder, ResourceFactory factory )
    {
        
        User user = null;

	  try
	  {

		// create a user

            try
            {
                System.out.println("\n\tTEST: creating a new user" );
		    UserCriteria criteria = new UserCriteria();
                user = UserHelper.narrow( factory.create( "new untitled user", criteria ));
                System.out.println("\tRESULT: ok");
		}
            catch ( ResourceFactoryProblem e )
            {
                System.out.println("\tRESULT: user creation failed - " + e.problem.message );
                e.printStackTrace();
		    System.exit(0);
	      }

		//
		// Test of the operations under the 
		// PropertySet interface
		//

		// add some properties to the new user
		// "define_property"

            try
            {
                System.out.println("\n\tTEST: define_property" );
		    Any email = orb.create_any();
		    email.insert_string("info@osm.net");
                user.define_property( "email", email );
		    Any web = orb.create_any();
		    web.insert_string("http://home.osm.net");
                user.define_property( "web", web );
		    listProperties( user );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

		// get properties by name
		// "get_property_value"

            try
            {
                System.out.println("\n\tTEST: get_property_value" );
                Any email = user.get_property_value( "email" );
		    String address = email.extract_string();
                System.out.println("\temail: " + address );
                Any web = user.get_property_value( "web" );
		    String url = web.extract_string();
                System.out.println("\turl: " + url );
		    listProperties( user );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
		    e.printStackTrace();
		    System.exit(0);
	      }

		// add some properties to the new user
		// "define_properties"

            try
            {
                System.out.println("\n\tTEST: define_properties" );
		    Any name = orb.create_any();
		    name.insert_string( System.getProperty("user.name") );
		    Property p1 = new Property( "name", name );
		    Any company = orb.create_any();
		    company.insert_string("OSM");
		    Property p2 = new Property( "company", company );
                user.define_properties( new Property[]{ p1, p2 } );
		    listProperties( user );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: user property association failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

		// get properties by as a sequence
		// "get_all_properties", PropertiesHolder

            try
            {
                System.out.println("\n\tTEST: get_all_properties/sequence" );
		    PropertiesHolder holder = new PropertiesHolder();
		    PropertiesIteratorHolder iterator = new PropertiesIteratorHolder();
                user.get_all_properties( 4, holder, iterator );
		    for( int i=0; i<holder.value.length; i++ )
		    {
			  String name = holder.value[i].property_name;
			  String value = holder.value[i].property_value.extract_string();
			  System.out.println("\tproperty: " + name + ", " + value );
		    }
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
		    e.printStackTrace();
		    System.exit(0);
	      }

		// get all property names
		// "get_all_property_names"

            try
            {
                System.out.println("\n\tTEST: get_all_property_names" );
		    PropertyNamesHolder holder = new PropertyNamesHolder();
		    PropertyNamesIteratorHolder iterator = new PropertyNamesIteratorHolder();
                user.get_all_property_names( 0, holder, iterator );
		    StringHolder s = new StringHolder();
	  	    while( iterator.value.next_one( s ) )
	          {
		        System.out.println("\tproperty name: " + s.value );
		    }
	  	    iterator.value.destroy();
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
		    e.printStackTrace();
		    System.exit(0);
	      }

		// get the number of properties in the property set
		// "get_number_of_properties"

            try
            {
                System.out.println("\n\tTEST: get_number_of_properties" );
                int n = user.get_number_of_properties( );
                System.out.println("\tRESULT: " + n );
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
		    e.printStackTrace();
		    System.exit(0);
	      }

		// test if a property is defined
		// "is_property_defined"

            try
            {
                System.out.println("\n\tTEST: is_property_defined" );
                boolean a = user.is_property_defined( "email" );
                boolean b = user.is_property_defined( "xyz" );
                System.out.println("\tname: email, " + a );
                System.out.println("\tname: xyz, " + b );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
		    e.printStackTrace();
		    System.exit(0);
	      }

		// delete property by name
		// "delete_property"

            try
            {
                System.out.println("\n\tTEST: delete_property" );
                user.delete_property( "email" );
		    listProperties( user );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

		// delete properties by name
		// "delete_properties"

            try
            {
                System.out.println("\n\tTEST: delete_properties" );
                user.delete_properties( new String[]{"company","name"} );
		    listProperties( user );
                System.out.println("\tRESULT: ok");
		}
            catch ( MultipleExceptions e )
            {
                System.out.println("\tRESULT: failed with MultipleExceptions" );
		    System.exit(0);
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

		// delete all of the properties
		// "delete_all_properties", PropertiesIteratorHolder

            try
            {
                System.out.println("\n\tTEST: delete all properties" );
                user.delete_all_properties( );
		    listProperties( user );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
		    e.printStackTrace();
		    System.exit(0);
	      }

		// PropertySetDef operations

	      // define a property with a mode
		// "define_property_with_mode"

            try
            {
                System.out.println("\n\tTEST: define_property_with_mode" );
		    Any email = orb.create_any();
		    email.insert_string("info@osm.net");
                user.define_property_with_mode( "email", email, PropertyModeType.fixed_readonly );
		    Any web = orb.create_any();
		    web.insert_string("http://home.osm.net");
                user.define_property_with_mode( "web", web, PropertyModeType.normal );
		    listProperties( user );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

	      // get the mode of a single property
		// "get_property_mode"

            try
            {
                System.out.println("\n\tTEST: get_property_mode" );
                PropertyModeType m = user.get_property_mode( "web" );
                System.out.println("\tweb: " + m.value());
                PropertyModeType n = user.get_property_mode( "email" );
                System.out.println("\temail: " + n.value());
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }


	      // define a properties with a modes
		// "define_properties_with_modes"

            try
            {
                System.out.println("\n\tTEST: define_properties_with_modes" );
		    Any name = orb.create_any();
		    name.insert_string("steve");
		    PropertyDef d1 = new PropertyDef( "name", name, PropertyModeType.normal );
		    Any company = orb.create_any();
		    company.insert_string("OSM");
		    PropertyDef d2 = new PropertyDef( "company", company, PropertyModeType.undefined );
                user.define_properties_with_modes( new PropertyDef[]{ d1, d2 });
		    listProperties( user );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

		// get property modes
		// "get_property_modes", PropertyModesHolder

            try
            {
                System.out.println("\n\tTEST: get_property_modes" );
		    PropertyModesHolder holder = new PropertyModesHolder();
                user.get_property_modes( new String[]{"name", "company"}, holder );
		    for( int i=0; i<holder.value.length; i++ )
		    {
			  String name = holder.value[i].property_name;
			  int mode = holder.value[i].property_mode.value();
			  System.out.println("\tproperty: " + name + ", " + mode );
		    }
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
		    e.printStackTrace();
		    System.exit(0);
	      }

	      // set the mode of a existing property
		// "set_property_mode"

            try
            {
                System.out.println("\n\tTEST: set_property_mode" );
                user.set_property_mode( "email", PropertyModeType.undefined );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

	      // set the mode of a set of properties
		// "set_property_modes"

            try
            {
                System.out.println("\n\tTEST: set_property_modes" );
		    PropertyMode m1 = new PropertyMode("name", PropertyModeType.fixed_readonly );
		    PropertyMode m2 = new PropertyMode("email", PropertyModeType.fixed_readonly );
                user.set_property_modes( new PropertyMode[]{ m1, m2} );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

	      // get the allowed property types
		// "get_allowed_property_types"

            try
            {
                System.out.println("\n\tTEST: get_allowed_property_types" );
		    PropertyTypesHolder holder = new PropertyTypesHolder();
                user.get_allowed_property_types( holder );
		    System.out.println("\tlength: " + holder.value.length );
		    for( int i=0; i<holder.value.length; i++ )
		    {
			  System.out.println("\ttype: " + holder.value[i] );
		    }
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }


	      // get the allowed properties
		// "get_allowed_properties"

            try
            {
                System.out.println("\n\tTEST: get_allowed_properties" );
		    PropertyDefsHolder holder = new PropertyDefsHolder();
                user.get_allowed_properties( holder );
		    System.out.println("\tlength: " + holder.value.length );
                System.out.println("\tRESULT: ok");
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
                e.printStackTrace();
		    System.exit(0);
	      }

		// delete all of the properties
		// "delete_all_properties"

            try
            {
                System.out.println("\n\tTEST: delete all properties" );
                boolean result = user.delete_all_properties( );
		    listProperties( user );
                System.out.println("\tRESULT: " + result);
		}
            catch ( Exception e )
            {
                System.out.println("\tRESULT: failed" );
		    e.printStackTrace();
		    System.exit(0);
	      }


	  }
        finally
	  {

		// delete the user

		try
		{
	          if( user != null ) user.remove();
		    System.out.println("\nuser destroyed");
		}
		catch( Exception removalException )
		{
		    System.out.println("failed to remove the user");
		    removalException.printStackTrace();
		}
	  }

	  System.exit(0);
    }
    
    private void listProperties( PropertySet set )
    {
        PropertiesHolder holder = new PropertiesHolder();
	  PropertiesIteratorHolder iterator = new PropertiesIteratorHolder();
        set.get_all_properties( 0, holder, iterator );

	  PropertyHolder props = new PropertyHolder();
	  while( iterator.value.next_one( props ) )
	  {
	      String name = props.value.property_name;
		String value = props.value.property_value.extract_string();
		System.out.println("\tproperty: " + name + ", " + value );
	  }
	  iterator.value.destroy();
    }

}
