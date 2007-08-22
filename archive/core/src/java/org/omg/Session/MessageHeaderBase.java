
package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

import net.osm.util.Incrementor;

/**
 */
public class MessageHeaderBase extends MessageHeader implements ValueFactory
{

    private Incrementor inc = Incrementor.create("messages"); 

    //
    // constructors
    //
   
   /**
    * Default constructor for stream internalization.
    */
    public MessageHeaderBase() 
    {
        this.priority = MessagePriority.NORMAL;
        this.classification = MessageClassification.INFORM;
        this.identifier = 0;
    }

   /**
    * Creation of a new SystemMessageBase based on a 
    * supplied header and body value.
    */

    public MessageHeaderBase( String subject, MessagePriority priority, MessageClassification classification, AbstractResource source ) 
    {
	  this.subject = subject;
	  this.priority = priority;
	  this.classification = classification;
	  this.source = source;
        this.identifier = 0;
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new MessageHeaderBase( ) );
    }

}

