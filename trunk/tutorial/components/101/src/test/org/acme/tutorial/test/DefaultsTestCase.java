/*
 * Copyright 2005-2006 Stephen J. McConnell.
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

package org.acme.tutorial.test;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.Provider;

import org.acme.tutorial.SimpleWidget;

/**
 * Test a widget component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultsTestCase extends TestCase
{
    private static final String PATH = "test.part";
    private static final File DIRECTORY = new File( System.getProperty( "project.test.dir" ) );
    private static final Controller CONTROLLER = Controller.STANDARD;
    
   /**
    * Test widget defaults.
    * @exception Exception if an error occurs
    */
    public void testWidgetDefaults() throws Exception
    {
        //
        // create the component
        //
        
        URI uri = new File( DIRECTORY, PATH ).toURI();
        Component component = CONTROLLER.createComponent( uri );
        Provider provider = component.getProvider();
        SimpleWidget widget = (SimpleWidget) provider.getValue( false );

        //
        // validate that the default message is what we are expecting
        //

        String message = widget.buildMessage( "red" );
        String user = System.getProperty( "user.name" );
        String expected = "Painting " + user + "'s house red.";
        assertEquals( "message content", expected, message );
    }
}
