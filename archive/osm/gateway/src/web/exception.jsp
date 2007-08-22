<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<!--
The exception.jsp page handles cascading exception reporting.
-->

<%@ page isErrorPage="true" %>
<%@ page import="javax.servlet.jsp.JspException" %>
<%@ page import="javax.servlet.ServletException" %>
<%@ page import="net.osm.gateway.tag.ExceptionUtil" %>
<%! Throwable m_cause; %>
<%! Throwable m_last; %>
<%! String m_path; %>
<%
    m_last = exception;
    if( exception instanceof JspException )
    {
        m_cause = ((JspException)exception).getRootCause();
    }
    else if( exception instanceof ServletException )
    {
        m_cause = ((ServletException)exception).getRootCause();
    }
    else
    {
        m_cause = exception.getCause();
    }

    String query = request.getQueryString();
    String url = "" + request.getRequestURL();
    if( query != null )
    {
        m_path = url + "?" + query;
    }
    else
    {
       m_path = url;
    }

    request.setAttribute("title", "Gateway Exception" );
    pageContext.include("/header.jsp");
%>
  <!--
  Gateway Exception.
  -->
  <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 
   <tr bgcolor="lightsteelblue">
     <td valign="top" colspan="2"><p class="banner">Description</p></td>
   </tr>
  </table>
  
  <p>An error occured while attempting to handle the request</br>
      <a href="<%= m_path %>"><%= m_path %></a>
  </p>

  </p>

  <table border="0" cellPadding="3" cellSpacing="0" width="100%"> 

    <tr bgcolor="lightsteelblue">
      <td valign="top"><p class="banner">Exception</p></td>
      <td><p class="banner">Message</p></td>
    </tr>
    <tr valign="top">
      <td>
        <p><%= exception.getClass().getName() %></p>
      </td>
      <td>
        <%
        if( exception.getMessage() != null )
        {
            %>
            <p>
              <%= exception.getMessage().replaceAll("\n","</br>") %>
            </p>
            <%
        }
        %>
      </td>
    </tr>

    <%
    while( m_cause != null )
    {
        m_last = m_cause;
        %>
        <tr valign="top">
          <td>
            <p class="property"><%= m_cause.getClass().getName() %></p>
          </td>
          <td>
            <%
            if( m_cause.getMessage() != null )
            {
               %>
               <p>
                 <%= m_cause.getMessage().replaceAll("\n","</br>") %>
               </p>
               <%
            }
            %>
          </td>
        </tr>
        <%
        if( m_cause instanceof JspException )
        {
            m_cause = ((JspException)m_cause).getRootCause();
        }
        else if( exception instanceof ServletException )
        {
            m_cause = ((ServletException)m_cause).getRootCause();
        }
        else
        {
            m_cause = m_cause.getCause();
        }
    }
    %>

  </table>

  </p>

  <table border="0" cellPadding="0" cellSpacing="0" width="100%"> 

    <tr>
      <td valign="top" colspan="2" bgcolor="lightgrey"><p class="banner">Stack Trace</p></td>
    </tr>

    <tr valign="top">
      <td><p>
      <%
      String[] list = ExceptionUtil.captureStackTrace( m_last );
      for( int i=1; i<list.length; i++ )
      {
          String line = list[i];
          %>
          <%= line %></br>
          <%
      }
      %>
      </p></td>
    </tr>
  </table>
  
  </p>
<%
    pageContext.include("footer.jsp");
%>
