/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.osm.tutorial.test;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import net.dpml.part.Part;
import net.dpml.part.PartContentHandlerFactory;
import net.dpml.part.PartReference;
import net.dpml.part.component.Component;
import net.dpml.part.component.Consumer;

import net.dpml.composition.data.ValueDirective;

import net.osm.tutorial.SimpleWidget;

/**
 * Test a simple component case.
 *
 * @author <a href="http://www.osm.net">Open Service Management</a>
 */
public class SimpleWidgetTestCase extends TestCase
{
   /**
    * Test the construction of the widget implementation.
    */
    public void testWidgetDefaults() throws Exception
    {
        //
        // create the component
        //

        URL url = new File( TEST_DIR, PATH ).toURL();
        SimpleWidget widget = (SimpleWidget) url.getContent( new Class[]{ Object.class } );

        //
        // validate that the default message is what we are expecting
        //

        String message = widget.buildMessage( "red" );
        String user = System.getProperty( "user.name" );
        String expected = "Painting " + user + "'s house red.";
        assertEquals( "message content", expected, message );

        //
        // execute it just for fun
        //

        widget.process( "red" );
    }

   /**
    * Test the construction of the widget implementation.
    */
    public void testWidgetManagement() throws Exception
    {
        //
        // get a reference to the widget Component and from this retrieve the 
        // definition of the component. 
        //

        URL url = new File( TEST_DIR, PATH ).toURL();
        Component component = (Component) url.getContent( new Class[]{ Component.class } );
        if( component instanceof Consumer )
        {
            //
            // update the component's 'target' dependency with a new solution
            //

            Consumer consumer = (Consumer) component;
            ValueDirective value = new ValueDirective( "my-car-part", "car" );
            consumer.setProvider( "target", value );
        }
        else
        {
            // will not happen becaue this component is a Consumer
            final String error = 
              "It's not a consumer!";
            throw new IllegalStateException( error );
        }

        //
        // validate that the new directives refects our new solution 
        //

        SimpleWidget widget = (SimpleWidget) component.resolve( false );
        String message = widget.buildMessage( "blue" );
        String user = System.getProperty( "user.name" );
        String expected = "Painting " + user + "'s car blue.";
        assertEquals( "message content", expected, message );

        //
        // execute it just for fun
        //

        widget.process( "blue" );
    }

    static
    {
        URLConnection.setContentHandlerFactory( new PartContentHandlerFactory() );
    }

    private static final String PATH = "test.part";
    private static File TEST_DIR = new File( System.getProperty( "project.test.dir" ) );

}
