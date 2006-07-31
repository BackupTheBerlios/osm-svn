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

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.dpml.util.DOM3DocumentBuilder;
import net.dpml.util.ElementHelper;
import net.dpml.util.DecodingException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

import junit.framework.TestCase;

/**
 * The ProcessXsdTestCase class validates a process XML schema instance.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ProcessXsdTestCase extends TestCase
{
    private static final String XSD_URI = "@PROJECT-XSD-URI@";
    
    private URI m_uri;
    private Map m_products;
    private Map m_processes;
    
   /**
    * Testcase setup.
    * @exception Exception if an error occursduring testcase setup
    */
    public void setUp() throws Exception
    {
        // get the sample xml file to parse
        
        File testdir = new File( System.getProperty( "project.test.dir" ) );
        File doc = new File( testdir, "sample.xml" );
        m_uri = doc.toURI();
        
        // define a system property declaring the location of the source xsd 
        
        File basedir = new File( System.getProperty( "project.basedir" ) );
        File target = new File( basedir, "target" );
        File xsd = new File( target, "process.xsd" );
        System.setProperty( XSD_URI, xsd.toURI().toString() );

        // parse the file
        
        DOM3DocumentBuilder builder = new DOM3DocumentBuilder();
        Document document = builder.parse( m_uri );
        Element element = document.getDocumentElement();
        
        // setup the product and process maps
        
        Element[] children = ElementHelper.getChildren( element );
        m_products = new Hashtable();
        m_processes = new Hashtable();
        
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            TypeInfo info = child.getSchemaTypeInfo();
            if( info.isDerivedFrom( XSD_URI, "ProductType", TypeInfo.DERIVATION_EXTENSION ) )
            {
                String name = ElementHelper.getAttribute( child, "name" );
                m_products.put( name, child );
            }
            else if( 
              XSD_URI.equals( info.getTypeNamespace() ) && "ProcessType".equals( info.getTypeName() ) 
              || info.isDerivedFrom( XSD_URI, "ProcessType", TypeInfo.DERIVATION_EXTENSION ) )
            {
                String name = ElementHelper.getAttribute( child, "name" );
                m_processes.put( name, child );
            }
            else
            {
                final String error = 
                  "Element is not derived from product or process."
                  + "\nNamespace: " 
                  + info.getTypeNamespace() 
                  + " (" 
                  + XSD_URI.equals( info.getTypeNamespace() ) 
                  + ")"
                  + "\nType: " 
                  + info.getTypeName()
                  + "\n" 
                  + child;
                fail( error );
            }
        }
    }
    
   /**
    * Test parsing of the sample file.
    * @exception Exception if an error occurs during test execution
    */
    public void testSampleParse() throws Exception
    {
        String[] keys = (String[]) m_processes.keySet().toArray( new String[0] );
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            Element elem = (Element) m_processes.get( key );
            evaluationProcess( elem );
        }
    }
    
   /**
    * Test the evaluation of implicit targets.
    * @exception Exception if an error occurs
    */
    public void testImplicitTargets() throws Exception
    {
        System.out.println( "PROCESS DRIVEN" );
        Element[] implicits = getImplicitProcesses();
        for( int i=0; i<implicits.length; i++ )
        {
            Element implicit = implicits[i];
            reportProcess( implicit );
        }
        System.out.println( "" );
    }
    
   /**
    * test explicit targets.
    * @exception Exception if an error occurs
    */
    public void testExplicitProducts() throws Exception
    {
        System.out.println( "PRODUCT DRIVEN" );
        Element jar = (Element) m_products.get( "target.deliverables.jar" );
        if( null == jar )
        {
            throw new NullPointerException( "target.deliverables.jar" );
        }
        ArrayList list = new ArrayList();
        getInputProcesses( list, jar );
        Element[] processors = (Element[]) list.toArray( new Element[0] );
        for( int i=0; i<processors.length; i++ )
        {
            Element process = processors[i];
            reportProcess( process );
        }
        System.out.println( "" );
    }

   /**
    * test explicit targets.
    * @exception Exception if an error occurs
    */
    public void testProcessTarget() throws Exception
    {
        System.out.println( "PROCESS" );
        ArrayList list = new ArrayList();
        Element rmic = (Element) m_processes.get( "rmic" );
        Element jar = (Element) m_processes.get( "jar" );
        getImpliedProcesses( list, rmic );
        getImpliedProcesses( list, jar );
        Element[] processors = (Element[]) list.toArray( new Element[0] );
        for( int i=0; i<processors.length; i++ )
        {
            Element process = processors[i];
            reportProcess( process );
        }
    }

    private Element[] getImplicitProcesses() throws DecodingException
    {
        ArrayList list = new ArrayList();
        String[] keys = (String[]) m_processes.keySet().toArray( new String[0] );
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            Element elem = (Element) m_processes.get( key );
            if( ElementHelper.getBooleanAttribute( elem, "implicit", false ) )
            {
                list.add( elem );
            }
        }
        Element[] implicits = (Element[]) list.toArray( new Element[0] );
        ArrayList expanded = new ArrayList();
        for( int i=0; i<implicits.length; i++ )
        {
            Element implicit = implicits[i];
            getImpliedProcesses( expanded, implicit );
        }
        return (Element[]) expanded.toArray( new Element[0] );
    }
    
    private void getImpliedProcesses( List list, Element process ) throws DecodingException
    {
        if( list.contains( process ) )
        {
            return;
        }
        getDepedentProcessors( list, process );
        Element[] inputs = getInputElements( process );
        for( int i=0; i<inputs.length; i++ )
        {
            Element e = inputs[i];
            String id = ElementHelper.getAttribute( e, "id" );
            Element product = (Element) m_products.get( id );
            getInputProcesses( list, product );
        }
        if( !list.contains( process ) )
        {
            list.add( process );
        }
        processValidators( list, process );
    }
    
    private void getDepedentProcessors( List list, Element process ) throws DecodingException
    {
        Element dependencies = ElementHelper.getChild( process, "dependencies" );
        if( null == dependencies )
        {
            return;
        }
        else
        {
            Element[] children = ElementHelper.getChildren( dependencies );
            String[] keys = new String[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String key = ElementHelper.getAttribute( child, "id" );
                Element proc = (Element) m_processes.get( key );
                if( null == proc )
                {
                    final String error = 
                      "Processor dependency ["
                      + key
                      + "] not recognized.";
                    throw new DecodingException( child, error );
                }
                else
                {
                    getImpliedProcesses( list, proc );
                }
            }
        }
    }
    
    private void processValidators( List list, Element process ) throws DecodingException
    {
        Element validators = ElementHelper.getChild( process, "validators" );
        if( null == validators )
        {
            return;
        }
        else
        {
            Element[] children = ElementHelper.getChildren( validators );
            String[] keys = new String[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String key = ElementHelper.getAttribute( child, "id" );
                Element proc = (Element) m_processes.get( key );
                if( null == proc )
                {
                    final String error = 
                      "Processor validation dependency ["
                      + key
                      + "] not recognized.";
                    throw new DecodingException( child, error );
                }
                else
                {
                    if( !list.contains( proc ) )
                    {
                        getImpliedProcesses( list, proc );
                    }
                }
            }
        }
    }
    
    private void getInputProcesses( List list, Element product ) throws DecodingException
    {
        if( null == product )
        {
            throw new NullPointerException( "product" );
        }
        String name = ElementHelper.getAttribute( product, "name" );
        getProducers( list, name );
    }
    
    private void getProducers( List list, String id ) throws DecodingException
    {
        
        String[] keys = (String[]) m_processes.keySet().toArray( new String[0] );
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            Element process = (Element) m_processes.get( key );
            String productionId = getProcessProductionID( process );
            if( id.equals( productionId ) )
            {
                if( !list.contains( process ) )
                {
                    getImpliedProcesses( list, process );
                    if( !list.contains( process ) )
                    {
                        list.add( process );
                    }
                }
            }
        }
    }
    
    private String getProcessProductionID( Element process )
    {
        return ElementHelper.getAttribute( process, "produces" );
    }
    
    private Element[] getInputElements( Element process )
    {
        Element consumes = ElementHelper.getChild( process, "consumes" );
        return ElementHelper.getChildren( consumes );
    }
    
    private void evaluationProcess( Element process )
    {
        Element[] inputs = getInputElements( process );
        for( int i=0; i<inputs.length; i++ )
        {
            Element input = inputs[i];
            String id = ElementHelper.getAttribute( input, "id" );
            Element product = (Element) m_products.get( id );
            if( null == product )
            {
                final String error =
                  "Input element:\n"
                  + DecodingException.list( input )
                  + "\n references an unknown product: "
                  + id;
                fail( error );
            }
        }
        String id = getProcessProductionID( process );
        if( null != id )
        {
            Element product = (Element) m_products.get( id );
            if( null == product )
            {
                final String error =
                  "Output production assertion:\n"
                  + DecodingException.list( process )
                  + "\n references an unknown product: "
                  + id;
                fail( error );
            }
        }
    }
    
    private void reportProcess( Element process )
    {
        String id = ElementHelper.getAttribute( process, "name" );
        System.out.println( "# process: " + id );
    }
}
