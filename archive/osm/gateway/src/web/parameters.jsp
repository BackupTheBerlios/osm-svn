
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ page import="net.osm.adapter.Adapter" %>
<%@ page import="net.osm.adapter.ServiceAdapter" %>
<%@ page import="net.osm.factory.FactoryAdapter" %>
<%@ page import="net.osm.factory.FactoryRuntimeException" %>
<%@ page import="net.osm.factory.Parameter" %>
<%@ page import="net.osm.session.HomeAdapter" %>
<%@ page import="net.osm.session.user.PrincipalAdapter" %>
<%@ page import="net.osm.gateway.GatewayRuntimeException" %>
<%@ page import="net.osm.gateway.ActionContext" %>

<%! PrincipalAdapter m_principal; %>
<%! FactoryAdapter m_adapter; %>
<% 

    try
    {
        HomeAdapter gateway = 
          (HomeAdapter) application.getAttribute("net.osm.session");
        m_principal = gateway.resolve_user( true );
    }
    catch( Throwable e )
    {
        final String error = "Could not find principal adapter.";
        throw new GatewayRuntimeException( error, e );
    }

    m_adapter = (FactoryAdapter) pageContext.findAttribute("adapter");
    if( m_adapter == null )
    {
        final String error = "Page context factory adapter attribute is null.";
        throw new NullPointerException( error );
    }

    // create a session level activity context

    ActionContext context = new ActionContext();
    context.put( "factory", m_adapter );
    context.put( "principal", m_principal );
    String key = context.getId();
    session.setAttribute( key , context );

    // prepare form presentation

    StringBuffer buffer = new StringBuffer();
    Parameter[] params = m_adapter.getParameters();
    String content = "";
    if( params.length > 0 )
    {
        for( int i=0; i<params.length; i++ )
        {
            buffer.append( "<tr>" );
            buffer.append( "<td>" + params[i].getName() + "</td>" );
            buffer.append( "<td>" + params[i].getKey() + "</td>" );
            buffer.append( "<td>" + params[i].getRequired() + "</td>" );
            buffer.append( "</tr>" );
        }
        buffer.append( "<tr height=\"2\" bgcolor=\"lightsteelblue\">" );
        buffer.append( "<td colspan=\"2\" height=\"2\"></td>" );
        buffer.append( "</tr>" );
        content = buffer.toString();
    }

%>
    <html:form method="post" action="createTask.do">
      <input type=hidden name="key" value="<%= key %>">
      <table width="100%" cellpadding="3" border="0">
        <tr>
          <td>
            <p class="column">Task Name:</br>
            <html:text property="name" size="45"
              value="<%= m_adapter.getDefaultName() %>" /> 
            </p>
          </td>
        </tr>
        <%= buffer.toString() %>
        <tr>
          <td width="73%">
            <div align="left">
              <input type=image 
                src="/gateway/nav/images/buttons/OK.gif" 
                width="70" height="21" > 
            </div>
          </td>
        </tr> 
      </table>
    </html:form>

