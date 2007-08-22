
package net.osm.finder;

import java.net.URL;
import net.osm.adapter.Adapter;
import net.osm.adapter.ServiceValue;

/**
 * An adapter for an <code>Finder</code> that provides 
 * state accessors that follow the EJB patterns.
 */
public class FinderValue extends ServiceValue
implements FinderAdapter
{
    //=============================================================
    // static
    //=============================================================

    /**
     * Return the truncatable ids
     */
    static final String[] _ids_list =
    {
        "IDL:osm.net/finder/FinderAdapter:1.0",
    };
    
    public static final String BASE_KEYWORD = "finder";

    //=============================================================
    // state
    //=============================================================
    
    private Finder m_finder;

    //=============================================================
    // constructors
    //=============================================================
 
   /**
    * Default constructor.
    */
    public FinderValue( ) 
    {
    }
   
   /**
    * Creation of a new DefaultFinderAdapter.
    * @param finder the <code>Finder</code> object reference
    */
    public FinderValue( Finder finder, URL url, String[] key, String name, String description ) 
    {
	  super( finder, url, key, name, description );
    }

    //=============================================================
    // FinderValue
    //=============================================================

    /**
     * Returs the primary finder object reference.
     */
    public Finder getFinder()
    {
        if( m_finder != null ) return m_finder;
        m_finder = FinderHelper.narrow( m_primary );
        return m_finder;
    }

    //=============================================================
    // FinderAdapter
    //=============================================================

    /**
     * Returns a adapter based on a supplied query.
     * @param  query the query portion of a URL.
     * @exception InvalidPath if the query is invalid
     * @exception ObjectNotFound if the query is valid but the object cannot be resolved
     */
    public Adapter resolve( String path )
    throws InvalidPath, ObjectNotFound
    {
        return getFinder().resolve( path ).get_adapter();
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
    }

   /**
    * Returns the adapter URL.
    */
    public String getURL()
    {
        return "finder?key=" + getName();
    }

    public String toString()
    {
        return toString( "finder" );
    }

   /**
    * Returns the static base keyword for the entity.
    */
    public String getBase()
    {
        return BASE_KEYWORD;
    }


    //=============================================================
    // ValueBase
    //=============================================================

   /**
    * Returns the truncatable ids identifying this valuetype.
    * @return String[] truncatable ids
    */
    public String [] _truncatable_ids()
    {
        return _ids_list;
    }
}
