
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.CommunityFramework.Control;

/**
 * The <code>ControlAgent</code> class is a base class for all
 * agents hosting valuetypes based on the <code>org.omg.CommunityFramework.Control</code>
 * specification.  A control exposes a label and a descrive note and is used extensively within 
 * the Community and Collaboration frameworks within policy defintions.
 */
public class ControlAgent extends ValueAgent implements Agent
{

   /**
    * The object reference to the Control that this agents 
    * represents.
    */
    protected Control control;

    //=========================================================================
    // Constructor
    //=========================================================================

   /**
    * Default constructor.
    */
    public ControlAgent(  )
    {
    }

   /**
    * Creation of a new ControlAgent based on a supplied Control instance.
    * @object instance of Control
    * @see org.omg.CommunityFramework.Control
    */
    public ControlAgent( Object object )
    {
	  super( object );
        try
        {
            this.control = (Control) object;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ControlAgent/setReference - bad type.");
        }
    }

    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the control instance this agent is wrapping.
    */
    public void setReference( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"ControlAgent/setReference - null value supplied.");
	  try
	  {
		this.control = (Control) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ControlAgent/setReference - bad type.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns the label of the control.
    * @see #getLabel
    */

    public String getName()
    {
	  return getLabel();
    }

   /**
    * Returns the label of the control.
    */
    public String getLabel()
    {
	  try
        {
            return control.label;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "ControlAgent:getLabel", e );
        }
    }

   /**
    * Returns the note of the control.
    */
    public String getNote()
    {
	  try
        {
            return control.note;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "ControlAgent:getNote", e );
        }
    }
}
