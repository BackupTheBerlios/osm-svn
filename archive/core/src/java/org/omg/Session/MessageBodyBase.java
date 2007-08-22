
package org.omg.Session;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

/**
 */
public class MessageBodyBase extends MessageBody implements ValueFactory
{

    //
    // constructors
    //
   
   /**
    * Default constructor for stream internalization.
    */
    public MessageBodyBase() 
    {
    }

   /**
    * Creation of a new SystemMessageBase based on a 
    * type identifier and message content.
    */
    public MessageBodyBase( String type, String message ) 
    {
	  this.type = type;
	  this.message = message;
    }

    //
    // implementation of ValueFactory
    //
    
    public Serializable read_value( InputStream is ) {
        return is.read_value( new MessageBodyBase( ) );
    }

}

