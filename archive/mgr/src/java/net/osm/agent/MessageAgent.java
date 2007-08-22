
package net.osm.agent;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;

import org.omg.Session.AbstractResource;
import org.omg.Session.SystemMessage;
import org.omg.Session.MessagePriority;
import org.omg.Session.MessageClassification;

import net.osm.util.IconHelper;
import net.osm.agent.AbstractResourceAgent;
import net.osm.shell.EditorPanel;
import net.osm.shell.ScrollView;
import net.osm.shell.StaticFeature;
import net.osm.shell.MGR;


/**
 * The <code>MessageAgent</code> class provides support for the declaration of 
 * messages that can be queued against a user.  Typically, messages represent 
 * status notifications from things like tasks or processors.  PricipalAgent 
 * implementations are responsible for managing the de-quing of messages and  
 * presentation of messages to real users.
 */

public class MessageAgent extends ValueAgent
{

    //=========================================================================
    // Members
    //=========================================================================

    protected SystemMessage message;

    private static String path = "net/osm/agent/image/resource.gif";
    private static ImageIcon icon = IconHelper.loadIcon( path );

   /**
    * The list of property views.
    */
    private List list;

   /**
    * The list of message features.
    */
    private List features;

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
    public void setPrimary( Object value ) 
    {
	  super.setPrimary( value );
	  try
	  {
	      this.message = (SystemMessage) value;
        }
        catch( Throwable e )
        {
            throw new RuntimeException("MessageAgent/setPrimary", e );
        }
    }

   /**
    * The <code>getType</code> method returns a human-friendly name of the entity.
    */
    public String getType( )
    {
	  return "Message";
    }

    public String getSubject()
    {
        return message.header.subject;
    }

    public long getIdentifier()
    {
        return message.header.identifier;
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
	      return (AbstractResourceAgent) getResolver().resolve( r );
	  }
	  catch( Throwable e )
        {
		throw new RuntimeException("unable to resolve the issuing resoruce", e );
        }
    }

    public String getMessage()
    {
        return message.body.message;
    }

    public String getMessageAsHTML()
    {
	  if( getMessage().startsWith("<html>") || getMessage().startsWith("<HTML>") ) return getMessage();

        final String mheader = "<html><body><table>";
        final String mfooter = "</table></body></html>";
        final String mtype = "<tr bgcolor='#ccccff'><td valign='top' colspan='2'><h3>" 
		+ getClassification() + "</h3></td></tr>";
        final String mtitle = "<tr><td valign='top'>Subject</td><td>"
		+ getSubject() + "</td></tr>";
        final String mpriority = "<tr><td valign='top'>Priority</td><td>" 
		+ getPriority() + "</td></tr>";
        final String mcontent = "<tr><td valign='top'>Message</td><td valign='top'>" 
		+ getMessage() + "</td></tr>";
        return mheader + mtype + mtitle + mpriority + mcontent + mfooter;
    }

    public String getMessageType()
    {
        return message.body.type;
    }

    //=========================================================================
    // Entity implementation
    //=========================================================================

   /**
    * Returns the name of the <code>Entity</code> as a <code>String</code>.
    */
    public String getName()
    {
        return getSubject();
    }

   /**
    * The <code>getPropertyPanels</code> operation returns a sequence of panels 
    * representing different views of the content and/or associations maintained by
    * and agent.
    */
    public List getPropertyPanels()
    {
	  //
	  // get all of the supertype panels
	  // together with the local panels
        //

	  if( list == null )
        {
	      list = super.getPropertyPanels();
		EditorPanel editor = new EditorPanel( this, "Content" );
		editor.setContentType( getMessageType() );
		if( getMessageType().equals("text/html"))
		{
		    editor.setText( getMessageAsHTML() );
		}
		else
		{
		    editor.setText( getMessage() );
		}
            editor.setEditable( false );
		editor.setFont( MGR.font );
		ScrollView view = new ScrollView( editor );
		view.setPreferred( true );
		list.add( view );
	  }
        return list;
    }

   /**
    * Returns a list of <code>Features</code>.
    * @return List the list of <code>Feature</code>s
    */
    public List getFeatures()
    {
        if( features != null ) return features;

        List list = super.getFeatures();
	  try
	  {
	      list.add( new StaticFeature("content", "" + getMessageType() ));
	      list.add( new StaticFeature("length", "" + getMessage().length() ));
	      list.add( new StaticFeature("priority", "" + getPriority() ));
	      list.add( new StaticFeature("classification", "" + getClassification() ));
        }
	  catch( Exception e )
	  {
	      return list;
	  }
	  this.features = list;
	  return features;
    }

}
