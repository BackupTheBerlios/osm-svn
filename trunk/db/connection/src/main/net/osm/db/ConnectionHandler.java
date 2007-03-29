
package net.osm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

import java.util.Properties;

import net.dpml.util.Logger;

/**
 * Connection handler component. 
 */
public class ConnectionHandler 
{
    //------------------------------------------------------------------------------
    // state
    //------------------------------------------------------------------------------
    
    private final Connection m_connection;
    private final ConnectionContext m_context;
    private final Logger m_logger;
    
    //------------------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------------------
    
   /**
    * Creation of a new connection handler.  The handler is responsible for 
    * proper initialization of a connection and formal disposal on tetrmination.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @exception SQLException if an SQL error occurs
    */
    public ConnectionHandler( final Logger logger, final ConnectionContext context ) throws SQLException
    {
        m_logger = logger;
        m_context = context;
        
        String driver = m_context.getDriver();
        Properties properties = new Properties();
        String protocol = m_context.getProtocol();
        boolean policy = m_context.getAutoCommitPolicy();
        
        m_logger.info( "loading connection: " + protocol );
        if( m_context.getAutoCreatePolicy() )
        {
            protocol = protocol + ";create=true";
        }
        m_connection = DriverManager.getConnection( protocol, properties );
        m_connection.setAutoCommit( policy );
        m_logger.info( "connection established" );
    }
    
    //------------------------------------------------------------------------------
    // ConnectionHandler
    //------------------------------------------------------------------------------
    
   /**
    * Return the connection established by the handler.
    * @return the connection
    */
    public Connection getConnection()
    {
        return m_connection;
    }
    
    //------------------------------------------------------------------------------
    // lifecycle
    //------------------------------------------------------------------------------
    
   /**
    * Lifecycle termination handler that will handle the repective commit on 
    * close policy and shutdown on close policy.
    * @exception SQLException if an SQL error occurs
    */
    public void dispose() throws SQLException
    {
        try
        {
            m_logger.info( "initiating commit on close" );
            m_connection.commit();
            m_logger.info( "commit complete" );
        }
        catch( SQLException sqle )
        {
            m_logger.warn( "commit failed", sqle );
        }
        
        try
        {
            m_connection.close();
            m_logger.info( "connection closed" );
        }
        catch( SQLException e )
        {
            m_logger.warn( "connection close raised an error", e );
        }
        if( m_context.getShutdownOnClosePolicy() )
        {
            String protocol = m_context.getProtocol();
            try
            {
                DriverManager.getConnection( protocol + ";shutdown=true" );
            }
            catch( SQLException e )
            {
                // normal behaviour
            }
            finally
            {
                m_logger.info( "driver shutdown complete" );
            }
        }
    }
}
