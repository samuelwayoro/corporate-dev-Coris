<%@page contentType="text/html"%>
<%--<%@page pageEncoding="ISO-8859-1"%>--%>
<%@page pageEncoding="UTF8"%>
<%
String tableData = "No records found";
String fileName = "dataTable";

if(request.getParameter("fileName")!= null || request.getParameter("fileName")!=""){
    fileName = request.getParameter("fileName");
}
if(request.getParameter("tableData")!= null || request.getParameter("tableData")!=""){
    tableData = request.getParameter("tableData");
}
response.setContentType("application/csv");
response.setCharacterEncoding("UTF8");
response.setHeader("Content-Disposition", "attachment;filename=\""+fileName+".csv\"");
out.print(tableData);
%>


