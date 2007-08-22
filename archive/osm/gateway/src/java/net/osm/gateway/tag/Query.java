
package net.osm.gateway.tag;

/**
 * Utility class supporting the parsing of HTTP queries.
 */
public class Query
{

    //=================================================================
    // state
    //=================================================================

    private String m_query;

    //=================================================================
    // constructor
    //=================================================================

    public Query( String query )
    {
        m_query = query;
    }

    //=================================================================
    // Query
    //=================================================================

   /**
    * Returns <code>TRUE</code> if the query contains the supplied 
    * name as a query parameter. 
    * @param the query parameter name
    * @return boolean TRUE if the supplied name matches a parameter name in the query
    *   otherwise <code>FALSE</code>
    */
    public boolean exists( String name )
    {
        if( m_query.startsWith( name + "=" ) ) return true;
        if( m_query.indexOf( "?" + name + "=" ) > 0 ) return true;
        return false;
    }

   /**
    * Returns the value of a query attribute given a supplied parameter name.
    * @param the query parameter name
    * @return String the parameter value or null if the parameter is not found.
    */
    public String lookup( String name )
    {
        if( !exists( name ) ) return null;

        String sub = "";
        if( m_query.startsWith( name + "=" ) )
        {
            sub = m_query.substring( m_query.indexOf( "=") + 1, m_query.length() );
        }
        else
        {
            sub = m_query.substring( m_query.indexOf("?" + name + "=") + 2 + name.length(), m_query.length() );
        }

        if( sub.indexOf("%20?") > 0 )
        {
            return sub.substring(0, sub.indexOf("%20?"));
        }
        else if( sub.indexOf("?") > 0 )
        {
            return sub.substring(0, sub.indexOf("?"));
        }
        else
        {
            return sub;
        }
    }
}
