
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.omg.CORBA.ORB;
import org.omg.Session.AbstractResource;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.ProcessorModel;


public class ProcessorAgent extends AbstractResourceAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    protected Processor processor;

    //=========================================================================
    // Constructor
    //=========================================================================

    public ProcessorAgent( )
    {
	  super();
    }

    public ProcessorAgent( ORB orb, Processor reference )
    {
        super( orb, reference );
	  this.processor = reference;
    }

    //=========================================================================
    // Operations
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setReference( Object value )
    {
	  super.setReference( value );
        try
        {
	      this.processor = ProcessorHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new CascadingRuntimeException( "Bad primary object reference.", local );
        }
    }

   /**
    * Return the model constraining the runtime behaviour of the processor.
    */
    public ProcessorModelAgent getModel( )
    {
        try
        {
            return new ProcessorModelAgent( (ProcessorModel) processor.model());
        }
        catch( Exception e )
        {
            throw new CascadingRuntimeException( "Unexpected exception while resolving the processor model.", e );
        }
    }
}

