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

import dpml.util.DefaultLogger;

import net.dpml.lang.Strategy;
import net.dpml.util.Logger;
import net.dpml.transit.Artifact;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import junit.framework.TestCase;


/**
 * Demonstration of application managed persistence.
 */
public class ComponentUnitTestCase extends TestCase
{
    public static final Logger LOGGER = new DefaultLogger( "test" );
    
    private EntityManagerFactory m_factory;
    
    public void setUp() throws Exception
    {
        URI uri = getPartURI();
        URL url = Artifact.toURL( uri );
        
        Strategy strategy = Strategy.load( uri );
        m_factory = strategy.getInstance( EntityManagerFactory.class );
        if( null == m_factory )
        {
            throw new NullPointerException( "factory" );
        }
    }
    
    public void testPersistence() throws Exception
    {
        LOGGER.info( "building content" );
        populate();
        LOGGER.info( "listing content" );
        list();
        LOGGER.info( "searching content" );
        search();
        LOGGER.info( "deleting content" );
    }
    
    private URI getPartURI() throws Exception
    {
        String test = System.getProperty( "project.test.dir" );
        File basedir = new File( test );
        File file = new File( basedir, "test.part" );
        return file.toURI();
    }

    private void populate()
    {
        EntityManager manager = m_factory.createEntityManager();
        manager.getTransaction().begin();
        CategoryStore tractor = new CategoryStore( "TRACTOR" );
        manager.persist( tractor );
        
        // create the CASE-IH manufacturer
        
        ManufacturerStore caseIH = new ManufacturerStore();
        caseIH.setLabel( "CASE" );
        caseIH.setName( "Case IH" );
        createCaseIH1896( caseIH, tractor );
        createCaseIH1056XL( caseIH, tractor );
        manager.persist( caseIH );
        
        // create the Mercedes Benz manufacturer
        
        ManufacturerStore mb = new ManufacturerStore();
        mb.setLabel( "MBENZ" );
        mb.setName( "MERCEDES BENZ" );
        createTrack1500( mb, tractor );
        manager.persist( mb );
        
        manager.getTransaction().commit();
    }
    
    private void createTrack1500( ManufacturerStore manufacturer, CategoryStore category )
    {
        ProductStore product = manufacturer.newProduct( category, "TRACK 1500" );
        product.newPart( "801.248.180", "THERMOSTAT" );
        product.newPart( "14P7052", "OIL FILTER CARTRIDGE" );
        product.newPart( "14PF834", "FUEL FILTER CARTRIDGE" );
        product.newPart( "14PA2405", "AIR FILTER" );
        product.newPart( "14PA2835", "INNER AIR FILTER" );
        product.newPart( "90230-7", "SPIGOT BEARING" );
    }
    
    private void createCaseIH1896( ManufacturerStore manufacturer, CategoryStore category )
    {
        ProductStore product = manufacturer.newProduct( category, "1896" );
        product.newPart( "90130-61", "WATER PUMP" );
        product.newPart( "27.8159K", "FAN DRIVE BELT" );
        product.newPart( "251.53216", "RADIATOR CAP" );
        product.newPart( "14BT339", "OIL FILTER SPIN-ON" );
        product.newPart( "14BF1280", "FUEL FILTER SPIN-ON", "PRIMARY" );
        product.newPart( "14BF788", "FUEL FILTER SPIN-ON", "SECONDARY" );
        product.newPart( "14PA2633FN", "AIR FILTER" );
        product.newPart( "14PA2634", "INNER AIR FILTER" );
        product.newPart( "14PT380KIT", "HYDRAULIC FILTER" );
    }
    
    private void createCaseIH1056XL( ManufacturerStore manufacturer, CategoryStore category )
    {
        ProductStore product = manufacturer.newProduct( category, "1056XL" );
        product.newPart( "849.6970", "ENGINE REBUILD KIT IN CHASSIS" );
        product.newPart( "849.9894", "ENGINE REBUILD KIT OUT OF CHASSIS" );
        product.newPart( "82PC2060", "CON-ROD BEARING STD" );
        product.newPart( "90028-14", "GUDGEON BUSH" );
        product.newPart( "90031-65", "LINER KIT [INCL PISTON RINGS ETC]", "STD PISTON " );
        product.newPart( "90035-63", "LINER ONLY " );
        product.newPart( "90042-23", "INLET VALVE " );
        product.newPart( "90041-16", "VALVE GUIDE" );
        product.newPart( "90045-15", "VALVE SPRING" );
        product.newPart( "90043-23", "EXHAUST VALVE" );
    }
    
    public void list()
    {
        EntityManager manager = m_factory.createEntityManager();
        Query query = manager.createQuery( "SELECT m FROM ManufacturerStore m" );
        List list = query.getResultList();
        int n = list.size();
        
        LOGGER.info( "  manufacturer count: " + n );
        for( Object o : list )
        {
            ManufacturerStore s = (ManufacturerStore) o;
            LOGGER.info( "    Manufacturer: " + s.getName() + " (" + s.getLabel() + ")" );
            Collection<ProductStore> products = s.getProducts();
            for( ProductStore product : products )
            {
                LOGGER.info( "      Product: " + product.getName() );
                Collection<PartStore> parts = product.getParts();
                for( PartStore part : parts )
                {
                    String description = part.getDescription();
                    String comments = part.getComments();
                    if( null == comments )
                    {
                        LOGGER.info( "        Part: " + description );
                    }
                    else
                    {
                        LOGGER.info( "        Part: " + description + ", " + comments );
                    }
                }
            }
        }
    }
    
    public void search()
    {
        search( "VALVE" );
        search( "FILTER" );
        search( "FUEL FILTER" );
        search( "SPRING" );
    }
    
    public synchronized void search( String value )
    {
        LOGGER.info( "  Product Query: \"" + value + "\"" );
        StringBuffer buffer = new StringBuffer( 
          "SELECT p FROM PartStore p " );
        String[] words = value.split( " " );
        for( int i=0; i<words.length; i++ )
        {
            String s = words[i];
            if( i==0 )
            {
                buffer.append( "WHERE p.description LIKE " + "'%" + s + "%'" );
            }
            else
            {
                buffer.append( " AND p.description LIKE " + "'%" + s + "%'" );
            }
        }
        buffer.append( " ORDER BY p.product.manufacturer.name, p.product.name, p.description" );
        
        EntityManager manager = m_factory.createEntityManager();
        String expression = buffer.toString();
        Query query = manager.createQuery( expression );
        List list = query.getResultList();
        int n = list.size();
        LOGGER.info( "  search count: " + n );
        
        ManufacturerStore m = null;
        ProductStore p = null;
        CategoryStore c = null;
        for( Object o : list )
        {
            PartStore part = (PartStore) o;
            if( c != part.getProduct().getCategory() )
            {
                c = part.getProduct().getCategory();
                LOGGER.info( "    " + c );
            }
            if( m != part.getProduct().getManufacturer() )
            {
                m = part.getProduct().getManufacturer();
                LOGGER.info( "      " + m );
            }
            if( p != part.getProduct() )
            {
                p = part.getProduct();
                LOGGER.info( "        " + p );
            }
            
            String id = part.getID();
            String description = part.getDescription();
            String comments = part.getComments();
            LOGGER.info( "          " + part + " (" + id + ")" );
        }
    }
}
