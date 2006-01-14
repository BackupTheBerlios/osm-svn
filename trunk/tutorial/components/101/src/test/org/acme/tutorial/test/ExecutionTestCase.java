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

package org.acme.tutorial.test;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.part.Controller;
import net.dpml.part.Component;
import net.dpml.part.Provider;

import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.MutableContextModel;

import org.acme.tutorial.SimpleWidget;

/**
 * Test a widget component.
 *
 * @author <a href="http://www.osm.net">Open Service Management</a>
 */
public class ExecutionTestCase extends TestCase
{
    private static final String PATH = "test.part";
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
        SimpleWidget widget = (SimpleWidget) provider.getValue( false );
        widget.process( "blue" );
    }
}
