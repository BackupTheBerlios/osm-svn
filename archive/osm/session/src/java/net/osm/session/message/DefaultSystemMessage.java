
package net.osm.session.message;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

/**
 * Valuetype implementation of a system message.
 */
public class DefaultSystemMessage extends SystemMessage 
implements ValueFactory
{

    //=============================================================
    // constructors
    //=============================================================
   
   /**
    * Default constructor for stream internalization.
    */
    public DefaultSystemMessage() 
    {
    }

   /**
    * Creation of a new SystemMessageBase  based on a 
    * supplied header and body value.
    */
    public DefaultSystemMessage( MessageHeader header, MessageBody body ) {
	  super.header = header;
	  super.body = body;
    }

    //=============================================================
    // ValueFactory
    //=============================================================
    
    public Serializable read_value( InputStream is ) 
    {
        return is.read_value( new DefaultSystemMessage( ) );
    }

}

