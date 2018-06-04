<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.*,java.util.*" %>
<%!String v;%>
<html>

<body background="background.jpg" style="font-family:Gloria Hallelujah, cursive;">
Selected keywords:<br>
<%if(request.getParameter("box")!=null)
{
	//out.println("You input "+request.getParameter("txtname"));
	v="";
	String[] arr = request.getParameterValues("box");
	for(int i=0;i<arr.length;i++)
	v+=arr[i]+" ";
	out.println(v);
}
else
{
	v=null;
	out.println("You input nothing");
}

%>

<br>
<form method="post" action="test.jsp">
<input type="text" value="<%=v%>" name="txtname">
<input type="submit" value="Submit"> 
</form>
</body>
</html>

