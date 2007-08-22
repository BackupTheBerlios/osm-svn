
package net.osm.agent;

import org.omg.Session.AbstractResource;
import org.omg.Session.SystemMessage;
import org.omg.Session.MessagePriority;
import org.omg.Session.MessageClassification;
import org.apache.avalon.framework.CascadingRuntimeException;

public class MessageAgent extends ValueAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    protected SystemMessage message;


    //=========================================================================
    // Constructor
    //=========================================================================

    public MessageAgent( )
    {
	  super();
    }

    public MessageAgent( SystemMessage message )
    {
        super( message );
	  this.message = message;
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
	      this.message = (SystemMessage) value;
        }
        catch( Throwable e )
        {
            throw new CascadingRuntimeException("MessageAgent/setReference", e );
        }
    }

    //=========================================================================
    // Getter methods
    //=========================================================================

    public String getSubject()
    {
        return message.header.subject;
    }

    public String getClassification()
    {
        int value = getMessageClassification().value();
	  switch ( value )
	  {
		case 0 :
			return "INFORM";
		case 1 :
			return "REQUEST";
		case 2 :
			return "WARNING";
		case 3 :
			return "ERROR";
		case 4 :
			return "FATAL";
	  }
	  return "UNKNOWN";
    }

    public MessageClassification getMessageClassification()
    {
        return message.header.classification;
    }

    public String getPriority()
    {
        int value = getMessagePriority().value();
	  switch ( value )
	  {
		case 0 :
			return "LOW";
		case 1 :
			return "NORMAL";
		case 2 :
			return "HIGH";
	  }
	  return "UNKNOWN";
    }

    public MessagePriority getMessagePriority()
    {
        return message.header.priority;
    }

    public AbstractResourceAgent getSource()
    {
	  try
        {
            AbstractResource r = message.header.source;
	      return (AbstractResourceAgent) AgentServer.getAgentService().resolve( r );
	  }
	  catch( Throwable e )
        {
		throw new CascadingRuntimeException("Web agent creation failure.", e );
        }
    }

    public String getMessage()
    {
        return message.body.message;
    }

    public String getMessageType()
    {
        return message.body.type;
    }
}
