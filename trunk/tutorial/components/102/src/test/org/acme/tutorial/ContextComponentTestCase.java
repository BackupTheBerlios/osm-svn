/*
 * Copyright 2005 Stephen J. McConnell.
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

package org.acme.tutorial;

import java.awt.Color;
import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.Provider;

import org.acme.tutorial.ContextTestComponent.Context;

/**
 * Local context validation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextComponentTestCase extends TestCase
{   
    // cached reference to the instance
    private ContextTestComponent m_value;
    
    // cached reference to the context object passed to the instance
    private Context m_context;
    
   /**
    * Testcase setup during which the part definition 'context.part'
    * is established as a file uri and the context test component is 
    * established.
    * @exception Exception if an unexpected error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "context.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        Component component = Controller.STANDARD.createComponent( uri );
        Provider instance = component.getProvider();
        m_value = (ContextTestComponent) instance.getValue( false );
        m_context = m_value.getContext();
    }
    
   /**
    * Test the initial component state using both the assigned colour value
    * the a default supplied value.
    * @exception Exception if an unexpected error occurs
    */
    public void testColor() throws Exception
    {
        assertEquals( "color/1", Color.RED, m_context.getColor() );
        assertEquals( "color/2", Color.YELLOW, m_context.getOptionalColor( Color.YELLOW ) );
    }
    
   /**
    * Test the passing of a null as a default argument (allowed).
    * @exception Exception if an unexpected error occurs
    */
    public void testNullColor() throws Exception
    {
        assertEquals( "color-null", null, m_context.getOptionalColor( null ) );
    }
    
   /**
    * Test access to an integer context value.
    * @exception Exception if an unexpected error occurs
    */
    public void testInteger() throws Exception
    {
        assertEquals( "int/1", 0, m_context.getInteger() );
        assertEquals( "int/2", 999, m_context.getOptionalInteger( 999 ) );
    }
    
   /**
    * Test access to an short context value.
    * @exception Exception if an unexpected error occurs
    */
    public void testShort() throws Exception
    {
        short s1 = 0;
        short s2 = 9;
        assertEquals( "short/1", s1, m_context.getShort() );
        assertEquals( "short/2", s2, m_context.getOptionalShort( s2 ) );
    }
    
   /**
    * Test access to an long context value.
    * @exception Exception if an unexpected error occurs
    */
    public void testLong() throws Exception
    {
        long v1 = 0;
        long v2 = 9;
        assertEquals( "long/1", v1, m_context.getLong() );
        assertEquals( "long/2", v2, m_context.getOptionalLong( v2 ) );
    }
    
   /**
    * Test access to an byte context value.
    * @exception Exception if an unexpected error occurs
    */
    public void testByte() throws Exception
    {
        byte v1 = 0;
        byte v2 = 9;
        assertEquals( "byte/1", v1, m_context.getByte() );
        assertEquals( "byte/2", v2, m_context.getOptionalByte( v2 ) );
    }
    
   /**
    * Test access to an double context value.
    * @exception Exception if an unexpected error occurs
    */
    public void testDouble() throws Exception
    {
        double v1 = 0;
        double v2 = 9;
        double delta = 0.001;
        
        assertEquals( "double/1", v1, m_context.getDouble(), delta );
        assertEquals( "double/2", v2, m_context.getOptionalDouble( v2 ), delta );
    }
    
   /**
    * Test access to an float context value.
    * @exception Exception if an unexpected error occurs
    */
    public void testFloat() throws Exception
    {
        float v1 = 0.5f;
        float v2 = 3.142f;
        double delta = 0.001;
        assertEquals( "float/1", v1, m_context.getFloat(), delta );
        assertEquals( "float/2", v2, m_context.getOptionalFloat( v2 ), delta );
    }
    
   /**
    * Test access to an char context value.
    * @exception Exception if an unexpected error occurs
    */
    public void testChar() throws Exception
    {
        char v1 = 'x';
        char v2 = '#';
        assertEquals( "char/1", v1, m_context.getChar() );
        assertEquals( "char/2", v2, m_context.getOptionalChar( v2 ) );
    }
    
   /**
    * Test access to an boolean context value.
    * @exception Exception if an unexpected error occurs
    */
    public void testBoolean() throws Exception
    {
        assertEquals( "boolean/1", true, m_context.getBoolean() );
        assertEquals( "boolean/2", false, m_context.getOptionalBoolean( false ) );
    }
    
   /**
    * Test access to an context value declared via a symbolic reference.
    * @exception Exception if an unexpected error occurs
    */
    public void testWorkSymbolicReference() throws Exception
    {
        final File test = new File( System.getProperty( "user.dir" ) );
        assertEquals( "file/1", test, m_context.getFile() );
        assertEquals( "file/2", test, m_context.getFile( new File( "abc" ) ) );
        final File somewhere = new File( "somewhere" );
        assertEquals( "file/3", somewhere, m_context.getOptionalFile( somewhere ) );
    }
    
   /**
    * Test access to a uri context value declared via a symbolic reference.
    * @exception Exception if an unexpected error occurs
    */
    public void testURISymbolicReference() throws Exception
    {
        URI foo = new URI( "foo:bar" );
        URI uri = new URI( "component:/context" );
        assertEquals( "uri", uri, m_context.getURI() );
        assertEquals( "uri", foo, m_context.getOptionalURI( foo ) );
    }
    
   /**
    * Test access to an system defined symbolic name.
    * @exception Exception if an unexpected error occurs
    */
    public void testNameSymbolicReference() throws Exception
    {
        assertEquals( "name", "context", m_context.getName() );
    }
    
   /**
    * Test access to an system defined symbolic value.
    * @exception Exception if an unexpected error occurs
    */
    public void testPathSymbolicReference() throws Exception
    {
        assertEquals( "path", "/context", m_context.getPath() );
    }
    
    static
    {
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }
}
