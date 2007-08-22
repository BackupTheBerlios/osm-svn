
package net.osm.factory;

import java.net.URL;
import java.util.List;

import net.osm.adapter.ServiceValue;
import net.osm.adapter.Adapter;

/**
 * An adapter for an <code>Factory</code> that provides 
 * state accessors that follow the EJB patterns.
 */
public class FactoryValue extends ServiceValue
implements FactoryAdapter
{
    //=============================================================
    // static
    //=============================================================
    
    /**
     * Return the truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/factory/FactoryAdapter:1.0",
    };

    public static final String BASE_KEYWORD = "factory";

    //=============================================================
    // state
    //=============================================================
    
   /**
    * Reference to the factory server.
    */
    private Factory m_factory;

    /**
     * Sequence of parameter descriptors.
     */
     protected Parameter[] m_params;

    /**
     * Default name to assign to new instances.
     */
     protected String m_default;

    //=============================================================
    // constructors
    //=============================================================
 
   /**
    * Default constructor.
    */
    public FactoryValue( ) 
    {
    }
   
   /**
    * Creation of a new DefaultFinderAdapter.
    * @param chooser the <code>Chooser</code> object reference
    * @param name the chooser presentation name
    * @param description short description of the chooser
    */
    public FactoryValue( Factory factory, URL url, String[] key, String name, String description, 
      Parameter[] params, String defaultName ) 
    {
        super( factory, url, key, name, description );
        if( params == null )
        {
            m_params = new Parameter[0];
        }
        else
        {
            m_params = params;
        }
        if( defaultName == null )
        {
            m_default = "Untitled Service";
        }
        else
        {
            m_default = defaultName;
        }
    }

    //=============================================================
    // FactoryAdapter
    //=============================================================

    /**
     * Returs the primary factory object reference.
     */
    public Factory getFactory()
    {
        if( m_factory != null ) return m_factory;
        m_factory = FactoryHelper.narrow( m_primary );
        return m_factory;
    }

    /**
     * Returns the default name to apply to new instances created by the 
     * factory.
     * @return String the default instance name
     */
     public String getDefaultName()
     {
         System.out.println("returning default name: " + m_default );
         return m_default;
     }

    /**
     * Creates a new adapted object via the underlying factory
     * @param  arguments an array of arguments to supply to the factory
     * @return  Adapter an adapter wrapping the created object
     * @exception  UnrecognizedCriteria if the arguments established by the
     * adapter implementation is unknown to the factory
     * @exception  InvalidCriteria if the arguments created by the 
     * implementation is recognized but rejected as invalid
     */
    public Adapter create( Argument[] arguments )
        throws UnrecognizedCriteria, InvalidCriteria, CreationException
    {
        return getFactory().create( arguments ).get_adapter();
    }

   /**
    * Returns the set of parameters for this factory.
    * @return Parameter[] an array of parameter descriptors
    */
    public Parameter[] getParameters()
    {
        return m_params;
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
        buffer.append( "\n" + lead + "params: " + m_params.length );
    }

    public String toString()
    {
        return toString( "factory" );
    }

    public String getURL()
    {
        return super.getURL();
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
            m_params = ParameterSequenceHelper.read(is);
	      m_default = is.read_string( );
        }
        catch( Throwable _input_read_exception_ )
        {
            throw new RuntimeException(
               "Error reading input stream for '"
               + "net.osm.factory.FactoryValue', due to: " 
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
            ParameterSequenceHelper.write(os,m_params);
	      os.write_string( m_default );
        }
        catch( Throwable _output_read_exception_ )
        {
            throw new RuntimeException(
               "Error writting to output stream for '"
               + "net.osm.factory.FactoryValue' due to: " 
               + _output_read_exception_.toString() );
        }
    }

   /**
    * Return the value TypeCode
    */
    public org.omg.CORBA.TypeCode _type()
    {
        return FactoryValueHelper.type();
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
