
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORB;
import org.omg.CollaborationFramework.InputDescriptor;
import org.omg.CollaborationFramework.OutputDescriptor;
import org.omg.CollaborationFramework.UsageDescriptor;

public class UsageDescriptorAgent extends ValueAgent implements Agent
{

   /**
    * The UsageDescriptor that this agents represents.
    */
    protected UsageDescriptor usage;

    //=========================================================================
    // Constructor
    //=========================================================================

    public UsageDescriptorAgent(  )
    {
    }

    public UsageDescriptorAgent( Object object )
    {
	  super( object );
        try
        {
            this.usage = (UsageDescriptor) object;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"UsageDescriptorAgent/setReference - bad type.");
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
	  if( value == null ) throw new RuntimeException(
		"UsageDescriptorAgent/setReference - null value supplied.");
	  try
	  {
		this.usage = (UsageDescriptor) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"UsageDescriptorAgent/setReference - bad type.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns the name of the resource.
    */

    public String getTag()
    {
	  return usage.getTag();
    }

   /**
    * Returns the type code constraint from the usage descriptor.
    */

    public TypeCode getTypeCode()
    {
	  try
        {
            return usage.getType();
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "UsageDescriptorAgent:getTypeCode", e );
        }
    }

   /**
    * Returns the IDL identifier of the type code constraint from the usage descriptor.
    */

    public String getIdentifier()
    {
	  try
        {
            return usage.getID();
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "UsageDescriptorAgent:getTypeCode", e );
        }
    }

   /**
    * Returns the required status of the usage descriptor - implementation returns false
    * for any instance of OutputDescriptor and returns the value decalred on InputDescriptor
    * instances.
    */

    public boolean getRequired()
    {
	  try
        {
            if( usage instanceof InputDescriptor ) return ((InputDescriptor) usage).required;
		return false;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "UsageDescriptorAgent:getRequired", e );
        }
    }

   /**
    * Returns the implied status of the usage descriptor - implementation returns false
    * for any instance of OutputDescriptor and returns the value decalred on InputDescriptor
    * instances.
    */

    public boolean getImplied()
    {
	  try
        {
            if( usage instanceof InputDescriptor ) return ((InputDescriptor) usage).implied;
		return false;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "UsageDescriptorAgent:getImplied", e );
        }
    }
}
