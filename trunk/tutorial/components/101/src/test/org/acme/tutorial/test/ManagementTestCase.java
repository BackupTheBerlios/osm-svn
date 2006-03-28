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

import net.dpml.component.Controller;
import net.dpml.component.Component;
import net.dpml.component.Provider;

import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.ComponentModel;
import net.dpml.metro.ContextModelManager;

import org.acme.tutorial.SimpleWidget;

/**
 * Test a widget component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ManagementTestCase extends TestCase
{
    private static final String PATH = "test.part";
    private static final File DIRECTORY = new File( System.getProperty( "project.test.dir" ) );
    private static final Controller CONTROLLER = Controller.STANDARD;
    
   /**
    * Test widget management.
    * @exception Exception if an error occurs
    */
    public void testWidgetManagement() throws Exception
    {
        //
        // get a reference to the component model and from this retrieve the 
        // component's context model and change the definition  of the target
        // context entry
        //
        
        URI uri = new File( DIRECTORY, PATH ).toURI();
        ComponentModel model = (ComponentModel) CONTROLLER.createModel( uri );
        ContextModelManager context = (ContextModelManager) model.getContextModel();
        ValueDirective value = new ValueDirective( "java.lang.String", "car" );
        context.setEntryDirective( "target", value );
        
        //
        // instantiate the component using our custom model
        //
        
        Component component = CONTROLLER.createComponent( model );
        Provider provider = component.getProvider();
        SimpleWidget widget = (SimpleWidget) provider.getValue( false );
        
        //
        // test message produced by the widget
        //
        
        String message = widget.buildMessage( "blue" );
        String user = System.getProperty( "user.name" );
        String expected = "Painting " + user + "'s car blue.";
        assertEquals( "message content", expected, message );
        widget.process( "red" );
    }
}
