
package net.osm.session.message;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.Session.AbstractResource;

/**
 * Valuetype implementation for a mesage header.
 */
public class DefaultMessageHeader extends MessageHeader 
implements ValueFactory
{

    //==========================================================
    // constructors
    //==========================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public DefaultMessageHeader() 
    {
        super.priority = MessagePriority.NORMAL;
        super.classification = MessageClassification.INFORM;
        super.identifier = 0;
    }

   /**
    * Creation of a new <code>SystemMessage</code> based on a 
    * supplied header and body value.
    */
    public DefaultMessageHeader( String subject, MessagePriority priority, MessageClassification classification, AbstractResource source ) 
    {
	  super.subject = subject;
	  super.priority = priority;
	  super.classification = classification;
	  super.source = source;
        super.identifier = 0;
    }

    //==========================================================
    // ValueFactory
    //==========================================================
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new DefaultMessageHeader( ) );
    }

}

