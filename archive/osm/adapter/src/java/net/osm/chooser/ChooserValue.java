
package net.osm.chooser;

import java.net.URL;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import net.osm.adapter.Adapter;
import net.osm.adapter.ServiceValue;
import net.osm.adapter.ServiceAdapter;
import net.osm.adapter.Adaptive;

/**
 * An adapter for an <code>Chooser</code> that provides 
 * state accessors that follow the EJB patterns.
 */
public class ChooserValue extends ServiceValue
implements ChooserAdapter
{
    //=============================================================
    // static
    //=============================================================
    
    /**
     * Return the truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/chooser/ChooserAdapter:1.0",
    };

    public static final String BASE_KEYWORD = "chooser";

    //=============================================================
    // state
    //=============================================================
    
    private Chooser m_chooser;

    private List m_list;

    /**
     * A set of keys of the services provided by the chooser.
     */
     protected String[] m_keys;

    //=============================================================
    // constructors
    //=============================================================
 
   /**
    * Default constructor.
    */
    public ChooserValue( ) 
    {
    }
   
   /**
    * Creation of a new DefaultFinderAdapter.
    * @param chooser the <code>Chooser</code> object reference
    * @param url a corbaloc URL
    * @param name the chooser presentation name
    * @param description short description of the chooser
    */
    public ChooserValue( Chooser chooser, URL url, String[] key, String name, 
      String description, String[] keys ) 
    {
        super( chooser, url, key, name, description );
        m_keys = keys;
    }

    //=============================================================
    // ChooserAdapter
    //=============================================================

    /**
     * Returs the primary chooser object reference.
     */
    public Chooser getChooser()
    {
        if( m_chooser != null ) return m_chooser;
        m_chooser = ChooserHelper.narrow( m_primary );
        return m_chooser;
    }

    /**
     * Returns the set of names supported by the chooser.
     * @return String[] the set of names
     */
    public String[] getNames()
    {
        return m_keys;
    }

    /**
     * Locates an Adapter to a object reference by name.
     */
    public ServiceAdapter lookup(String name) throws UnknownName
    {
        getLogger().debug("lookup: " + name );
        return (ServiceAdapter) getChooser().lookup( name ).get_adapter();
    }

    public Iterator getServices()
    {
        List list = new LinkedList();
        for( int i=0; i<m_keys.length; i++ )
        {
            try
            {
                list.add( lookup( m_keys[i] ));
            }
            catch( UnknownName e )
            {
                // this is possible if the names have changed
            }
        }
        return list.iterator();
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
        buffer.append( "\n" + lead + "keys: " );
        for( int i=0; i<m_keys.length; i++ )
        {
            if(( i > 0 ) && ( i < ( m_keys.length )))
            {
                buffer.append( ", " );
            }
            buffer.append( m_keys[i] );
        }
    }


    public String toString()
    {
        return toString( "chooser" );
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
            m_keys = KeySequenceHelper.read(is);
        }
        catch( Throwable _input_read_exception_ )
        {
            throw new RuntimeException(
               "Error reading input stream for '"
               + "net.osm.chooser.ChooserValue', due to: " 
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
            KeySequenceHelper.write(os,m_keys);
        }
        catch( Throwable _output_read_exception_ )
        {
            throw new RuntimeException(
               "Error writting to output stream for '"
               + "net.osm.chooser.ChooserValue' due to: " 
               + _output_read_exception_.toString() );
        }
    }

    /**
     * Return the value TypeCode
     */
     public org.omg.CORBA.TypeCode _type()
     {
        return ChooserValueHelper.type();
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
