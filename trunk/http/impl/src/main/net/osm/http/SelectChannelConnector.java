/*
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
package net.osm.http;

/**
 * Thread pool implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SelectChannelConnector extends org.mortbay.jetty.nio.SelectChannelConnector
{
    private static final int HEADER_BUFFER_SIZE = 4*1024;
    private static final int REQUEST_BUFFER_SIZE = 8*1024;
    private static final int RESPONSE_BUFFER_SIZE = 32*1024;
    private static final int MAXIMUM_IDLE_TIME = 30000;
    private static final int ACCEPT_QUEUE_SIZE = 0;
    private static final int ACCEPTORS = 1;
    private static final int SO_LINGER_TIME = 1000;
    private static final int CONFIDENTIAL_PORT = 0;
    private static final int INTEGRAL_PORT = 0;
    private static final boolean ASSUME_SHORT_DISPATCH = false;
    
   /**
    * Select channel context definition.
    */
    public interface Context extends ConnectorContext
    {
       /**
        * Return the policy concerning short dispatch. 
        * @param flag implementation defined default value
        * @return the supplied policy unless overriden in the deployment configuration
        */
        boolean getAssumeShortDispatch( boolean flag );
    }

   /**
    * Creation of a new <tt>SelectChannelConnector</tt>.
    * @param context the component context
    * @exception Exception if a component configuration error occurs
    */
    public SelectChannelConnector( Context context ) throws Exception
    {
        super();
        
        String host = context.getHost( null );
        if( null != host )
        {
            setHost( host );
        }
    
        int port = context.getPort();
        setPort( port );
        
        int headerBufferSize = context.getHeaderBufferSize( HEADER_BUFFER_SIZE );
        setHeaderBufferSize( headerBufferSize );
        
        int requestBufferSize = context.getRequestBufferSize( REQUEST_BUFFER_SIZE );
        setRequestBufferSize( requestBufferSize );
        
        int responseBufferSize = context.getResponseBufferSize( RESPONSE_BUFFER_SIZE );
        setResponseBufferSize( responseBufferSize );
        
        int maxIdle = context.getMaxIdleTime( MAXIMUM_IDLE_TIME );
        setMaxIdleTime( maxIdle );
        
        int queueSize = context.getAcceptQueueSize( ACCEPT_QUEUE_SIZE );
        setAcceptQueueSize( queueSize );
        
        int acceptCount = context.getAcceptors( ACCEPTORS );
        setAcceptors( acceptCount );
        
        int linger = context.getSoLingerTime( SO_LINGER_TIME );
        setSoLingerTime( linger );
        
        int confidentialPort = context.getConfidentialPort( CONFIDENTIAL_PORT );
        setConfidentialPort( confidentialPort );
        
        Scheme confidentialScheme = Scheme.parse( context.getConfidentialScheme( "https" ) );
        setConfidentialScheme( confidentialScheme.getName() );
        
        int integralPort = context.getIntegralPort( INTEGRAL_PORT );
        setIntegralPort( integralPort );
        
        Scheme integralScheme = Scheme.parse( context.getIntegralScheme( "https" ) );
        setIntegralScheme( integralScheme.getName() );
        
        // SelectChannelConnector$Context
        
        boolean flag = context.getAssumeShortDispatch( ASSUME_SHORT_DISPATCH );
        setAssumeShortDispatch( flag );
    }
}
