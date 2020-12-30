<%@page contentType="application/octet-stream"%>
<%@page pageEncoding="ISO-8859-1"%>

<%
String tableData = "No records found";
String fileName = "dataTable";

if(request.getParameter("fileName")!= null || request.getParameter("fileName")!=""){
    fileName = request.getParameter("fileName");
}
if(request.getParameter("tableData")!= null || request.getParameter("tableData")!=""){
    tableData = request.getParameter("tableData");
}
response.setContentType("application/vnd.ms-excel");
response.setCharacterEncoding("UTF8");
response.setHeader("Content-Disposition", "attachment;filename=\""+fileName+".xls\"");
out.print(tableData);
%>


