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
import net.dpml.part.component.Provider;

import net.dpml.composition.data.ValueDirective;

import net.osm.tutorial.SimpleWidget;

/**
 * Test a widget component.
 *
 * @author <a href="http://www.osm.net">Open Service Management</a>
 */
public class SimpleWidgetTestCase extends TestCase
{
    private static final String PATH = "test.part";
    private static File DIRECTORY = new File( System.getProperty( "project.test.dir" ) );
    private static final Controller CONTROLLER = Controller.STANDARD;
    
   /**
    * Test the cexecution of the widget.
    */
    public void testWidgetExecution() throws Exception
    {
        URI uri = new File( DIRECTORY, PATH ).toURI();
        Component component = CONTROLLER.createComponent( uri );
        Provider provider = component.getProvider();
        SimpleWidget widget = (SimpleWidget) provider.getValue( false );
        widget.process( "blue" );
    }
    
   /**
    * Test widget defaults.
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

   /**
    * Test widget management.
    */
    public void testWidgetManagement() throws Exception
    {
        //
        // get a reference to the widget Component and from this retrieve the 
        // definition of the component. 
        //
        
        URI uri = new File( DIRECTORY, PATH ).toURI();
        MutableComponentModel model = (MutableComponentModel) CONTROLLER.createModel( uri );
        MutableContextModel context = (MutableContextModel) model.getContextModel();
        ValueDirective value = new ValueDirective( "java.lang.String", "car" );
        context.setEntryDirective( "color", value );
        Component component = CONTROLLER.createComponent( model );
        Provider provider = component.getProvider();
        SimpleWidget widget = (SimpleWidget) provider.getValue( false );
        String message = widget.buildMessage( "blue" );
        String user = System.getProperty( "user.name" );
        String expected = "Painting " + user + "'s car blue.";
        assertEquals( "message content", expected, message );
    }
}
