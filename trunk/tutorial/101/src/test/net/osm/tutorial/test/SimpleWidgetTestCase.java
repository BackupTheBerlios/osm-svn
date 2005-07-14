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

import junit.framework.TestCase;

import net.dpml.part.PartContentHandlerFactory;

import net.osm.tutorial.SimpleWidget;

/**
 * Test a simple component case.
 *
 * @author <a href="http://www.osm.net">Open Service Management</a>
 */
public class SimpleWidgetTestCase extends TestCase
{
    private static final String PATH = "test.part";
    private static File TEST_DIR = new File( System.getProperty( "project.test.dir" ) );

   /**
    * Test the construction of the widget implementation.
    */
    public void testWidgetDefaults() throws Exception
    {
        //
        // create the component
        //

        URL url = new File( TEST_DIR, PATH ).toURL();
        url.openConnection().setContentHandlerFactory( new PartContentHandlerFactory() );
        SimpleWidget widget = (SimpleWidget) url.getContent( new Class[]{ Object.class } );

        //
        // execute it just to confirm that it does not reaise any errors
        //

        widget.process( "red" );

        //
        // validate the the default message is what we are expecting
        //

        String message = widget.buildMessage( "red" );
        String user = System.getProperty( "user.name" );
        String expected = "Painting " + user + "'s house red.";
        assertEquals( "message content", expected, message );
    }
}
