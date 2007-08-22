/*
 */
package net.osm.hub.processor;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;

/**
 * ApplianceContext is an object passed as an argument during the 
 * contextualize phase of an appliance.  ApplianceContext suppliments 
 * the default context interface with accessors to the processor
 * callback.
 * @author Stephen McConnell <mcconnell@osm.net>
 */

public class ApplianceContext extends DefaultContext
{

    //============================================================
    // static
    //============================================================
 
    public static final String CALLBACK = "CALLBACK";
    private static final String nullArg = "null callback supplied to constructor";

    //============================================================
    // state
    //============================================================

    private final ProcessorCallback callback;

    //============================================================
    // constructor
    //============================================================

   /**
    * Creation of a new appliance context object using a supplied 
    * ProcessorCallback.
    * @param callback the host processor 
    */
    public ApplianceContext( ProcessorCallback callback )
    {
        this( null, callback );
    }    

   /**
    * Creation of a new appliance context object using a supplied 
    * hosting processor.
    * @param parent the parent context object 
    * @param callback the host processor 
    */
    public ApplianceContext( Context parent, ProcessorCallback callback )
    {
	  super( parent );
        if( callback == null ) throw new NullPointerException( nullArg );
        this.callback = callback;
        put( CALLBACK, callback );
    }

    //============================================================
    // implementation
    //============================================================

   /**
    * Returns the processor callback.
    * @return ProcessorCallback the hosting processor
    */
    public ProcessorCallback getProcessorCallback()
    {
        return this.callback;
    }

}



