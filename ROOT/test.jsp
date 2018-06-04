<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<body background="background.jpg" style="font-family:Gloria Hallelujah, cursive;">
<%@page language="java"%>
<%@page import="java.util.StringTokenizer"%>
<%@page import="jdbm.*"%>
<%@page import="java.util.Vector"%>
<%@page import="java.io.IOException"%>
<%@page import="phase1.*"%>
<%@page import="java.util.Map"%>
<%@page import="IRUtilities.*"%>

<%!RecordManager recman;%>
<%!Indexer indexer;%>
<%!SE se;%>
<%!int i;%>
<%!Vector<scoreMap> result;%>
<%!int pageID;%>
<%!Vector<fPair> w;%>
<%
if(request.getParameter("txtname")!=null)
{
	try{
	recman = RecordManagerFactory.createRecordManager("fetch");
	indexer = new Indexer(recman);	
//	se = new SE();
	//out.println("You input:<br>"+request.getParameter("txtname"));
//	Indexer indexer = new Indexer(recman);//create the indexer for retrieve informantion
//	SE se = new SE();
	StringTokenizer st = new StringTokenizer(request.getParameter("txtname"));
	Vector<String> key = new Vector<String>();
	while(st.hasMoreTokens())
		key.add(st.nextToken());
	
	result = SE.search(key);
	if(result != null&&result.size()!=0){
%>

<%	for(i=0;i<result.size()&&i<50;i++){%>
		<%!String url;%><%url = indexer.getURL(result.elementAt(i).getID());%>
		<%!String title;%><%title = indexer.getPageContent(result.elementAt(i).getID()).getTitle();%>
		
	<%--<%!Page p;%><%p = indexer.getPageContent(result.elementAt(i).getID());%>--%>
	<%--	<%pageID = result.elementAt(i).getID();%>--%>
		<%out.println(indexer.getPageContent(result.elementAt(i).getID()).getTitle()+"<br>");%>
		Score :<%out.print(result.elementAt(i).getScore()+"   ");%><br><a href="<%=url.toString()%>"><%=title%></a><br>
		<a href="<%=url.toString()%>"><%=url%></a><br>
		<%out.println(indexer.getPageContent(result.elementAt(i).getID()).getModifiedDate().toString()+"   "+indexer.getPageContent(result.elementAt(i).getID()).getPageSize());%><br>
		<%--output five most frequent words with freq--%>
		<%--!Vector<fPair> w;--%><%w = indexer.getTopFiveWord(result.elementAt(i).getID());%>
		<%if(w!=null) for(int j =0;j<w.size();j++){%>
			<%out.print("("+indexer.getWord(w.elementAt(j).getID())+", "+w.elementAt(j).getfreq()+") ");%>
		<%}%><br>
		<%!Vector<Integer> pl;%><%pl = indexer.getParentLink(result.elementAt(i).getID());%>
		<%!Vector<Integer> cl;%><%cl = indexer.getChildLink(result.elementAt(i).getID());%>
		Parent Links:<br>
		<%if (pl!=null) for(int j =0;j<pl.size();j++){%>
			<%=indexer.getURL(pl.elementAt(j).intValue())%><br>
		<%}%>
		Child Links:<br>
		<%if(cl!=null) for(int j = 0;j<cl.size();j++){%>
			<%=indexer.getURL(cl.elementAt(j).intValue())%><br>
		<%}%>
	
--------------------------------------------------------------------------------------------<br>
<%	}%>

	
	<%} else {out.println("No page found!"+"<br>");}%>
	<%
	indexer.finalize();
	}
	catch(Exception e){
		System.err.println(e.toString());
	}
	%>

<%}
else
{
	out.println("You input nothing");
}
	
%>
</body>
</html>
