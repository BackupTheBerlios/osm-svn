/*
 * Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
 * Copyright 2006 Stephen McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.osm.http.test;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Test listeners.
 */
public class TestListener implements HttpSessionListener,  HttpSessionAttributeListener, HttpSessionActivationListener, ServletContextListener, ServletContextAttributeListener, ServletRequestListener, ServletRequestAttributeListener
{
   /**
    * Attribute added.
    * @param se binding event
    */
    public void attributeAdded( HttpSessionBindingEvent se )
    {
        // System.err.println("attributedAdded "+se);
    }

   /**
    * Attribute removed.
    * @param se binding event
    */
    public void attributeRemoved( HttpSessionBindingEvent se )
    {
        // System.err.println("attributeRemoved "+se);
    }

   /**
    * Attribute replaced.
    * @param se binding event
    */
    public void attributeReplaced( HttpSessionBindingEvent se )
    {
        // System.err.println("attributeReplaced "+se);
    }

   /**
    * Session passivate notification
    * @param se session event
    */
    public void sessionWillPassivate( HttpSessionEvent se )
    {
        // System.err.println("sessionWillPassivate "+se);
    }

   /**
    * Session did activate notification
    * @param se session event
    */
    public void sessionDidActivate( HttpSessionEvent se )
    {
        // System.err.println("sessionDidActivate "+se);
    }

   /**
    * Context initiated notification
    * @param sce servlet context event
    */
    public void contextInitialized( ServletContextEvent sce )
    {
        // System.err.println("contextInitialized "+sce);
    }

   /**
    * Context destroy notification
    * @param sce servlet context event
    */
    public void contextDestroyed( ServletContextEvent sce )
    {
        // System.err.println("contextDestroyed "+sce);
    }

   /**
    * Attribute added notification
    * @param scab servlet context attribute event
    */
    public void attributeAdded( ServletContextAttributeEvent scab )
    {
        // System.err.println("attributeAdded "+scab);
    }

   /**
    * Attribute removed notification
    * @param scab servlet context attribute event
    */
    public void attributeRemoved( ServletContextAttributeEvent scab )
    {
        // System.err.println("attributeRemoved "+scab);
    }

   /**
    * Attribute replaced notification
    * @param scab servlet context attribute event
    */
    public void attributeReplaced( ServletContextAttributeEvent scab )
    {
        // System.err.println("attributeReplaced "+scab);
    }

   /**
    * Request destroyed notification
    * @param sre servlet request event
    */
    public void requestDestroyed( ServletRequestEvent sre )
    {
        // System.err.println("requestDestroyed "+sre);
    }

   /**
    * Request initialized notification
    * @param sre servlet request event
    */
    public void requestInitialized( ServletRequestEvent sre )
    {
        // System.err.println("requestInitialized "+sre);
    }

   /**
    * Attribute added notification
    * @param srae servlet request attribute event
    */
    public void attributeAdded( ServletRequestAttributeEvent srae )
    {
        // System.err.println("attributeAdded "+srae);
    }

   /**
    * Attribute removed notification
    * @param srae servlet request attribute event
    */
    public void attributeRemoved( ServletRequestAttributeEvent srae )
    {
        // System.err.println("attributeRemoved "+srae);
    }

   /**
    * Attribute replaced notification
    * @param srae servlet request attribute event
    */
    public void attributeReplaced( ServletRequestAttributeEvent srae )
    {
        // System.err.println("attributeReplaced "+srae);
    }

   /**
    * Session created notification
    * @param se session event
    */
    public void sessionCreated( HttpSessionEvent se )
    {
        // System.err.println("sessionCreated "+se);
    }

   /**
    * Session destroyed notification
    * @param se session event
    */
    public void sessionDestroyed( HttpSessionEvent se )
    {
        // System.err.println("sessionDestroyed "+se);
    }

}
