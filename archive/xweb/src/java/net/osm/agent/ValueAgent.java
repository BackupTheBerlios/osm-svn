
package net.osm.agent;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.omg.CommunityFramework.Control;
import org.omg.CORBA.portable.ValueBase;


public class ValueAgent extends AbstractAgent implements Agent
{

   /**
    * The object reference to the valuetype that this agents 
    * represents.
    */
    protected ValueBase valuebase;

   /**
    * Cache reference to kind IDL string.
    */
    protected String kind;


    //=========================================================================
    // Constructor
    //=========================================================================

    public ValueAgent(  )
    {
    }

    public ValueAgent( Object object )
    {
	  if( object == null ) throw new RuntimeException(
		"LinkAgent - null reference supplied to agent constructor");
        try
        {
            this.valuebase = (ValueBase) object;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"LinkAgent/setReference - bad type.");
        }
    }

    //=========================================================================
    // Atrribute setters
    //=========================================================================

   /**
    * Set the valuetype that is to be presented.
    */
    public void setReference( Object value ) 
    {
	  if( value == null ) throw new RuntimeException(
		"ValueAgent/setReference - null value supplied.");
	  try
	  {
		this.valuebase = (ValueBase) value;
        }
	  catch( Throwable t )
        {
		throw new RuntimeException(
		"ValueAgent/setReference - bad type.");
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

   /**
    * Returns the resource kind.
    */

    public String getKind( )
    {
	  if( kind != null ) return kind;
	  try
        {
            kind = valuebase._truncatable_ids()[0];
		return kind;
	  }
	  catch( Throwable e )
        {
            throw new CascadingRuntimeException( "ValueAgent:getKind", e );
        }
    }
}
