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

import net.dpml.util.DOM3DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.TestCase;

/**
 * The ProcessXsdTestCase class validates a process XML schema instance.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class WorkspaceTestCase extends TestCase
{
    private static final String XSD_URI = "@PROJECT-XSD-URI@";
    
    private WorkspaceDirective m_workspace;
    
   /**
    * Testcase setup.
    * @exception Exception if an error occursduring testcase setup
    */
    public void setUp() throws Exception
    {
        // get the sample xml file to parse
        
        File testdir = new File( System.getProperty( "project.test.dir" ) );
        File doc = new File( testdir, "workspace.xml" );
        
        // define a system property declaring the location of the source xsd 
        
        File basedir = new File( System.getProperty( "project.basedir" ) );
        File target = new File( basedir, "target" );
        //File deliverables = new File( target, "deliverables" );
        //File xsds = new File( deliverables, "xsds" );
        //String version = System.getProperty( "project.version" );
        File xsd = new File( target, "process.xsd" );
        System.setProperty( XSD_URI, xsd.toURI().toString() );

        // parse the layout file
        
        DOM3DocumentBuilder builder = new DOM3DocumentBuilder();
        Document document = builder.parse( doc.toURI() );
        Element element = document.getDocumentElement();
        
        // setup the layout directive
        
        WorkspaceDecoder decoder = new WorkspaceDecoder();
        m_workspace = decoder.build( doc );
    }
    
   /**
    * Test worksace creation.
    * @exception Exception if an error occurs during test execution
    */
    public void testWorkspace() throws Exception
    {
        System.out.println( "# WORKSPACE " + m_workspace.getName() );
        ProductDirective[] products = m_workspace.getProductDirectives();
        for( int i=0; i<products.length; i++ )
        {
            ProductDirective product = products[i];
            System.out.print( "# " + product.getName() );
            if( product instanceof DirectoryDirective )
            {
                DirectoryDirective dir = (DirectoryDirective) product;
                System.out.println( "\t" + dir.getPath() );
            }
            else
            {
                System.out.println( "" );
            }
        }
        assertEquals( "count", 7, products.length );
    }
    
   /**
    * Test worksace lookup.
    * @exception Exception if an error occurs during test execution
    */
    public void testLookup() throws Exception
    {
        System.out.println( "# LOOKUP " + m_workspace.getName() );
        ProductDirective product = m_workspace.getProductDirective( "target/reports/test" );
        System.out.println( "# TEST " + product.getName() );
    }
}
