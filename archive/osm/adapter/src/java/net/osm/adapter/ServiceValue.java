package net.osm.adapter;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.io.Serializable;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

/**
 * Valuetype containg a reference to object service together with 
 * supporting presentation name and description.
 */
public class ServiceValue extends AdapterValue
implements ServiceAdapter
{
    //=======================================================================
    // static
    //=======================================================================

    /**
     * truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/adapter/ServiceAdapter:1.0",
    };

    public static final String BASE_KEYWORD = "service";

    //=======================================================================
    // state
    //=======================================================================

    /**
     * Service identifier.
     */
     protected String[] m_key;

    /**
     * A string version of the identifier.
     */
     protected String m_path;

    /**
     * A short presentation name of the service.
     */
     protected String m_name;

    /**
     * Text describing the service.
     */
     protected String m_description;

    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public ServiceValue()
    {
    }

   /**
    * Creation of a new <code>ServiceAdapter</code> using a supplied
    * <code>Object</code> reference, name and description.
    * @param primary the primary object reference
    * @param name the name of the service
    * @param description the service description
    */
    public ServiceValue( 
      org.omg.CORBA.Object primary, URL url, String[] key, String name, 
      String description ) 
    {
	  super( primary, url );
        m_key = key;
        m_name = name;
        m_description = description;
    }

    //=============================================================
    // ServiceAdapter
    //=============================================================

    /**
     * Returns the service identifier.
     * @return String[] the service identifier
     */
    public String[] getKey()
    {
        return m_key;
    }


    /**
     * Returns the service name.
     * @return String the service name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Returns the service description.
     * @return String the service description
     */
    public String getDescription()
    {
        return m_description;
    }

    //=============================================================
    // Adapter
    //=============================================================

    /**
     * Suppliments the supplied <code>StringBuffer</code> with a short 
     * description of the adapted object.
     * @param  buffer the string buffer to append the description to
     * @param  lead a <code>String</code> that is prepended to content
     */
    public void report( StringBuffer buffer, String lead )
    {
        super.report( buffer, lead );
        buffer.append( "\n" + lead + "name: " + m_name );
        buffer.append( "\n" + lead + "description: " + m_description );
    }

   /**
    * Returns the adapter URL.
    */
    public String getURL()
    {
        if( m_path != null ) return m_path;
        StringBuffer buffer = new StringBuffer();
        for( int i=0; i<m_key.length; i++ )
        {
            buffer.append( m_key[i] );
            if( i < ( m_key.length - 1) ) buffer.append( "/" );
        }
        m_path = buffer.toString();
        return m_path;
    }

    public String toString()
    {
        return toString( "service" );
    }

   /**
    * Returns the static base keyword for the entity.
    */
    public String getBase()
    {
        return BASE_KEYWORD;
    }


    //=======================================================================
    // StreamableValue
    //=======================================================================

    /**
     * Unmarshal the value into an InputStream
     */
    public void _read( org.omg.CORBA.portable.InputStream is )
    {
        super._read( is );
        try
        {
            m_key = KeySequenceHelper.read(is);
            m_name = is.read_string();
            m_description = is.read_string();
        }
        catch( Throwable _input_read_exception_ )
        {
            throw new RuntimeException(
               "Error reading input stream for '"
               + "net.osm.adapter.ServiceValue', due to: " 
               + _input_read_exception_.toString() );
        }
    }

    /**
     * Marshal the value into an OutputStream
     */
     public void _write( org.omg.CORBA.portable.OutputStream os )
     {
        super._write( os );
        try
        {
            KeySequenceHelper.write(os,m_key);
            os.write_string(m_name);
            os.write_string(m_description);
        }
        catch( Throwable _output_read_exception_ )
        {
            throw new RuntimeException(
               "Error writting to output stream for '"
               + "net.osm.adapter.ServiceValue' due to: " 
               + _output_read_exception_.toString() );
        }
    }

    /**
     * Return the value TypeCode
     */
     public org.omg.CORBA.TypeCode _type()
     {
        return ServiceValueHelper.type();
     }

   /**
    * Returns the truncatable ids identifying this valuetype.
    * @return String[] truncatable ids
    */
    public String [] _truncatable_ids()
    {
        return _ids_list;
    }

}
