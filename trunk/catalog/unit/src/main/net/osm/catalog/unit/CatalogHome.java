/*
 * Copyright 2007 Stephen J. McConnell
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

package net.osm.catalog.unit;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;


/**
 * Catlog home component.
 */
public class CatalogHome implements EntityManagerFactory
{
    private static final String PERSISTENCE_UNIT_NAME = "osm-catalog-unit";

    public interface Context
    {
        Map<String,String> getParameters();
    }
    
    private final EntityManagerFactory m_factory;
    
    public CatalogHome( Context context )
    {
        Map<String,String> params = context.getParameters();
        for( String s : params.keySet() )
        {
            System.out.println( "" + s + "=" + params.get( s ) );
        }
        m_factory = 
          Persistence.createEntityManagerFactory( 
            PERSISTENCE_UNIT_NAME, 
            params );
    }
    
    public void close()
    {
        m_factory.close();
    }
    
    public boolean isOpen()
    {
        return m_factory.isOpen();
    }
    
    public EntityManager createEntityManager()
    {
        return m_factory.createEntityManager();
    }
    
    public EntityManager createEntityManager( Map map )
    {
        return m_factory.createEntityManager( map );
    }
}
