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

package org.acme.tutorial;

import java.io.File;
import java.net.URI;
import java.awt.Color;

import junit.framework.TestCase;

import net.dpml.component.Controller;
import net.dpml.component.Component;
import net.dpml.component.Provider;

/**
 * Test a widget component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ExecutionTestCase extends TestCase
{
    private static final String PATH = "example.part";
    private static final File DIRECTORY = new File( System.getProperty( "project.test.dir" ) );
    private static final Controller CONTROLLER = Controller.STANDARD;
    
   /**
    * Test the cexecution of the widget.
    * @exception Exception if an error occurs
    */
    public void testWidgetExecution() throws Exception
    {
        URI uri = new File( DIRECTORY, PATH ).toURI();
        Component component = CONTROLLER.createComponent( uri );
        Provider provider = component.getProvider();
        ExampleComponent instance = (ExampleComponent) provider.getValue( false );
        Color color = instance.getColor();
        assertEquals( "color", new Color( 100, 200, 0 ), color );
    }
}
