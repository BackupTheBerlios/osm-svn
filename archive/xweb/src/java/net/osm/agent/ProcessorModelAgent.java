
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.CommunityFramework.Control;
import org.omg.CollaborationFramework.ProcessorModel;

public class ProcessorModelAgent extends ControlAgent
{

   /**
    * The object reference to the ProcessorModel that this agents 
    * represents.
    */
    protected ProcessorModel model;

    //=========================================================================
    // Constructor
    //=========================================================================

    public ProcessorModelAgent(  )
    {
    }

    public ProcessorModelAgent( Object value )
    {
	  super( value );
        try
        {
            this.model = (ProcessorModel) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ProcessorModelAgent/setReference - bad type.");
        }
    }


    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the resource that is to be presented.
    */
    public void setReference( Object value ) 
    {
        super.setReference( value );
        try
        {
            this.model = (ProcessorModel) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ProcessorModelAgent/setReference - bad type.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns an array of UsageDescriptorAgent instances.
    */

    public UsageDescriptorAgent[] getDescriptors()
    {
        UsageDescriptorAgent[] array = new UsageDescriptorAgent[ model.usage.length ];
        for( int i = 0; i<model.usage.length; i++ )
        {
            array[i] = new UsageDescriptorAgent( model.usage[i] );
        }
	  return array;
    }
}
