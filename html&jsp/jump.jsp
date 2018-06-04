<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.io.*,java.util.*" %>
<%@page import="java.util.StringTokenizer"%>
<%@page import="jdbm.*"%>
<%@page import="java.util.Vector"%>
<%@page import="java.io.IOException"%>
<%@page import="phase1.*"%>
<%@page import="java.util.Map"%>
<%@page import="IRUtilities.*"%>
<%!RecordManager recman;%>
<%!Indexer indexer;%>
<html>

<body background="background.jpg" style="font-family:Gloria Hallelujah, cursive;">
<%
try{
recman = RecordManagerFactory.createRecordManager("fetch");
indexer = new Indexer(recman);
}catch(Exception e){System.err.println(e.toString());}
%>
<form method="post" action="trans.jsp">
<%
try{
int size = (indexer.getWordIDSize()>50? 50:indexer.getWordIDSize());
for(int i=0;i<size;i++){%>

<%!String s;%><%s=indexer.getWord(i);%>
<%=s%>
<input type="checkbox" name="box" value="<%=s%>"><br>

<%
}
}catch(Exception e){System.err.println(e.toString());}
%>
<input type="submit" value="Select">
</form>

</body>
</html>

