/*
 * Copyright 2006 Stephen J. McConnell
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.osm.process.info;

import java.util.Arrays;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.lang.UnknownKeyException;
import net.dpml.lang.DuplicateKeyException;

/**
 * The WorkspaceDirective holds the collection of products defining a project layout.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class WorkspaceDirective extends DirectoryDirective
{
    private final ProductDirective[] m_products;
    
    private final transient Map m_map = new Hashtable();
    
   /**
    * Creation of a new workspace directive.
    * @param name the workspace name
    * @param info workspace info
    * @param path directory path
    * @param products array of products within the workspace
    * @exception DuplicateKeyException if one or more duplicate product keys are supplied
    */
    public WorkspaceDirective( 
      final String name, InfoDirective info, final String path, final ProductDirective[] products )
      throws DuplicateKeyException
    {
        super( name, info, path );
        if( null == products )
        {
            throw new NullPointerException( "products" );
        }
        m_products = products;
        for( int i=0; i<m_products.length; i++ )
        {
            ProductDirective product = m_products[i];
            String key = product.getName();
            if( m_map.containsKey( key ) )
            {
                throw new DuplicateKeyException( key );
            }
            else
            {
                m_map.put( key, product );
            }
        }
    }
    
   /**
    * Get the set of product directives.
    * @return the product directive array.
    */
    public ProductDirective[] getProductDirectives()
    {
        return m_products;
    }

   /**
    * Get a named product from the layout.
    * @param name the product name
    * @return the named product
    * @exception UnknownKeyException if the requested name is unknown
    */
    public ProductDirective getProductDirective( String name ) throws UnknownKeyException
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        int n = name.indexOf( "/" );
        if( n > -1 )
        {
            String item = name.substring( 0, n );
            String remainder = name.substring( n + 1 );
            WorkspaceDirective workspace = (WorkspaceDirective) getProductDirective( item );
            return workspace.getProductDirective( remainder );
        }
        else
        {
            if( m_map.containsKey( name ) )
            {
                return (ProductDirective) m_map.get( name );
            }
            else
            {
                throw new UnknownKeyException( name );
            }
        }
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof WorkspaceDirective ) )
        {
            WorkspaceDirective object = (WorkspaceDirective) other;
            return Arrays.equals( m_products, object.m_products );
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashArray( m_products );
        return hash;
    }
}
