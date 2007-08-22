package net.osm.adapter;

import java.io.Serializable;
import java.net.URL;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.orb.corbaloc.Handler;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA.portable.StreamableValue;

/**
 * <p>The <code>AdapterValue</code> is a valuetype that implements the 
 * <code>Adapter</code> interface enabling server implementations
 * to publish variations of adapter implementations that enable 
 * specialized client side functionality.</p> 
 * <p>Specialization of <code>AdapterValue</code> should include
 * the following in their implementation:</p>
 * <pre>
 *   //static truncatable ids declaration
 *   static final String[] _ids_list = 
 *     { "IDL:osm.net/session/workspace/WorkspaceAdapter:1.0", };
 * 
 *   public String [] _truncatable_ids()
 *   {
 *     return _ids_list;
 *   }
 * </pre>
 */
public class AdapterValue extends AdapterFactory 
implements StreamableValue, Adapter
{

    //=======================================================================
    // static
    //=======================================================================

    /**
     * truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/adapter/Adapter:1.0",
    };

    public static final String BASE_KEYWORD = "adapter";

    //=======================================================================
    // state
    //=======================================================================

    /**
     * Primary object reference.
     */
     protected Object m_primary;

    /**
     * Primary corbaloc address.
     */
     protected String m_corbaloc;

     private URL m_url;

     private ORB m_orb;

    //=============================================================
    // constructors
    //=============================================================
    
   /**
    * Default constructor for stream internalization.
    */
    public AdapterValue()
    {
    }

   /**
    * Creation of a new <code>AdapterValue</code> using a supplied
    * <code>Object</code> reference.
    * @param primary the primary object reference
    * @param url the primary object corbaloc address
    */
    public AdapterValue( Object primary, URL url ) 
    {
	  m_primary = primary;
        if( url != null )
        {
            m_corbaloc = url.toExternalForm();
        }
        else
        {
            m_corbaloc = "";
        }
    }

    //=============================================================
    // Serviceable
    //=============================================================

   /**
    * Service provisioning enabled by the container. This service
    * manager provides the valuetype with a reference to the current 
    * ORB.
    * @param manager a service manager
    */
    public void service( ServiceManager manager ) 
    throws ServiceException
    {
        super.service( manager );
        try
        {
            m_orb = (ORB) manager.lookup("orb");
        }
        catch( Throwable e )
        {
            final String error = "Unable to resolve ORB service.";
            throw new ServiceException( error, e );
        }
    }

    //=============================================================
    // Adapter
    //=============================================================

   /**
    * Returns the static base keyword for the entity.
    */
    public String getBase()
    {
        return BASE_KEYWORD;
    }

    /**
     * Returns the primary object reference that the adapter is adapting.
     * @return  org.omg.CORBA.Object object reference
     */
    public Object getPrimary()
    {
        if( m_primary == null ) throw new NullPointerException(
           "Primary state has not been initialized.");
        return m_primary;
    }

    /**
     * Returns the adapter URL.
     */
     public String getURL()
     {
         return "?base=adapter";
     }

    /**
     * Return the corbaloc address of the primary object reference.
     * @return URL the corbaloc URL of the primary object
     */
     public URL getCorbaloc()
     {
         if( m_url == null )
         {
             try
             {
                 m_url = new URL( null, m_corbaloc, new Handler( m_orb ) );
             }
             catch( Throwable e )
             {
                 throw new AdapterRuntimeException(
                   "Unexpected exception while creating corbaloc URL for path: " +
                   m_corbaloc, e );
             }
         }
         return m_url;
     }
 
    /**
     * Suppliments the supplied <code>StringBuffer</code> with a short 
     * description of the adapted object.
     * @param  buffer the string buffer to append the description to
     * @param  lead a <code>String</code> that is prepended to content
     */
    public void report( StringBuffer buffer, String lead )
    {
        buffer.append( "\n" + lead + "class: " + getClass().getName() );
        buffer.append( "\n" + lead + "instance: " + System.identityHashCode( this ));
        buffer.append( "\n" + lead + "url: " + getCorbaloc() );
    }

    public String toString()
    {
        return toString( "adapter" );
    }

    protected String toString( String enclosing )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( enclosing );
        report( buffer, "  " );
        return buffer.toString();
    }

    //=======================================================================
    // StreamableValue
    //=======================================================================

    public String [] _truncatable_ids()
    {
        return _ids_list;
    }

    /**
     * Unmarshal the value into an InputStream
     */
    public void _read( org.omg.CORBA.portable.InputStream is )
    {
        try
        {
            m_primary = is.read_Object();
            m_corbaloc = is.read_string();
        }
        catch( Throwable _input_read_exception_ )
        {
            throw new RuntimeException(
               "Error reading input stream for '"
               + "net.osm.adapter.AdapterValue', due to: " 
               + _input_read_exception_.toString() );
        }
    }

    /**
     * Marshal the value into an OutputStream
     */
     public void _write( org.omg.CORBA.portable.OutputStream os )
     {
        try
        {
            os.write_Object(m_primary);
            os.write_string(m_corbaloc);
        }
        catch( Throwable _output_read_exception_ )
        {
            throw new RuntimeException(
               "Error writting to output stream for '"
               + "net.osm.adapter.AdapterValue' due to: " 
               + _output_read_exception_.toString() );
        }
    }

    /**
     * Return the value TypeCode
     */
     public org.omg.CORBA.TypeCode _type()
     {
        return net.osm.adapter.AdapterValueHelper.type();
     }
}

