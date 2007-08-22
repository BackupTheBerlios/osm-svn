/*
 */
package net.osm.hub.processor;

import java.io.File;
import net.osm.hub.gateway.ServantContext;
import org.apache.avalon.framework.configuration.Configuration;


/**
 * ProcessorContext is an object passed as an argument during the contextualize
 * phase of a component lifecycle.  ProcessorContext suppliments the resource 
 * context with the appliance repository path.
 *
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public class ProcessorContext extends ServantContext
{

    //============================================================
    // static
    //============================================================
 
    public static final String APPLIANCE_NAME = "appliance.name";
    public static final String APPLIANCE_CLASS = "appliance.class";
    public static final String APPLIANCE_POLICY = "appliance.policy";
    public static final String APPLIANCE_CONFIGURATION = "appliance.configuration";

    //============================================================
    // state
    //============================================================

   /**
    * The name of the appliance.
    */
    private String name;

   /**
    * The appliance policy configuration.
    */
    private Configuration policy;

   /**
    * The optional appliance configuration.
    */
    private Configuration configuration;

   /**
    * The appliance class
    */
    private Class implementation;


    //============================================================
    // constructor
    //============================================================

   /**
    * Creation of a new ProcessorContext
    */
    public ProcessorContext( ServantContext parent, String name, Class clazz, 
      Configuration policy, Configuration config )
    {
	  super( parent, parent.getStorageObject() );

  	  this.name = name;
  	  this.policy = policy;
  	  this.configuration = config;
  	  this.implementation = clazz;

        put( APPLIANCE_NAME, name );
        put( APPLIANCE_POLICY, policy );
        put( APPLIANCE_CONFIGURATION, config );
        put( APPLIANCE_CLASS, clazz );
    }

    //============================================================
    // implementation
    //============================================================

   /**
    * Returns the name of the appliance.
    */
    public String getApplianceName()
    {
        return name;
    }

   /**
    * Returns the appliance policy.
    */
    public Configuration getAppliancePolicy()
    {
        return policy;
    }

   /**
    * Returns the appliance configuration.
    */
    public Configuration getApplianceConfiguration()
    {
        return configuration;
    }

   /**
    * Returns the appliance implementation class.
    */
    public Class getApplianceClass()
    {
        return implementation;
    }
}



