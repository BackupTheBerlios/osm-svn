
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

import net.dpml.annotation.Context;

/**
* Connection handler deployment context.
*/
@Context
public interface ConnectionContext
{
   /**
    * Return the classname of the driver.  Default value is 
    * <tt>org.apache.derby.jdbc.EmbeddedDriver</tt>.
    * @return the driver classname
    */
    String getDriver();
    
   /**
    * Return the protocol identifying the connection source.
    * No default value is provided as such component assemblers 
    * must supply a value in a component directive or as a part
    * uri query.
    * @return the protocol value
    */
    String getProtocol();
   
   /**
    * Return the auto-create policy.  If true, the connection will be 
    * established with the attribute <tt>create=true</tt>.
    * The default policy is false.
    * @return the auto create policy
    */
    boolean getAutoCreatePolicy();
    
   /**
    * Return the auto-commit policy.  On establishment of a connection
    * the implementation will set the autocommit policy to the returned value.
    * The default policy is false.
    * @return the autocommit policy
    */
    boolean getAutoCommitPolicy();
    
   /**
    * Return the shutdown on close policy.  On disposal of a connection the 
    * implementation will shutdown the underlying database if thihs policy 
    * returns true. The default value is true.
    * @return the shutdown on close policy
    */
    boolean getShutdownOnClosePolicy();
}
    
