
package net.osm.agent;

import java.util.LinkedList;
import org.omg.CORBA.ORB;
import org.omg.Session.AbstractResource;
import org.omg.CollaborationFramework.Processor;
import org.omg.CollaborationFramework.ProcessorHelper;
import org.omg.CollaborationFramework.ProcessorModel;
import org.omg.CommunityFramework.Problem;

import net.osm.agent.AbstractResourceAgent;

/**
 * The <code>ProcessorAgent</code> class is an agent used to represent
 * a remote Processor.
 */

public class ProcessorAgent extends AbstractResourceAgent
{

    //=========================================================================
    // state
    //=========================================================================

    protected Processor processor;

    private LinkedList list;

    private ProcessorModelAgent modelAgent;

    //=========================================================================
    // Agent
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setPrimary( Object value )
    {
	  super.setPrimary( value );
        try
        {
	      this.processor = ProcessorHelper.narrow( (org.omg.CORBA.Object) value );
        }
        catch( Exception local )
        {
            throw new RuntimeException( 
		  "Bad primary object reference.", local );
        }
    }

    //=========================================================================
    // ProcessorAgent
    //=========================================================================

   /**
    * Return the model constraining the runtime behaviour of the processor.
    */
    public ProcessorModelAgent getModel( )
    {
	  if( modelAgent != null ) return modelAgent;
        try
        {
		modelAgent = (ProcessorModelAgent) getResolver().resolve( processor.model() );
		return modelAgent;
        }
        catch( Exception e )
        {
            throw new RuntimeException( 
		  "Unexpected exception while resolving the processor model.", e );
        }
    }

   /**
    * Returns the primary object reference in the form of a Processor.
    */
    public Processor getProcessor()
    {
        return this.processor;
    }

   /**
    * Verify that the processor/task configuration meets usage constraints.
    */
    public boolean verify()
    {
        try
	  {
            Problem[] problems = processor.verify();
		return ( problems.length == 0 );
	  }
	  catch( Exception e )
	  {
            throw new RuntimeException( 
		  "Unexpected exception while verifying processor.", e );
	  }
    }

   /**
    * The <code>getType</code> method returns a human-friendly name of the entity.
    */
    public String getType( )
    {
	  return "Processor";
    }
}

