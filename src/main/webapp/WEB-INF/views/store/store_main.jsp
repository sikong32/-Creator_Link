<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link href="${pageContext.request.contextPath}/resources/css/store_main.css" rel="stylesheet" type="text/css">
<title>Creator Link_Store Main</title>
</head>
<body>
	<form action="#">
		<table border="1" align="center">
			<tr>
				<th>굿즈 스토어</th>
			</tr>
		</table>
	</form>
	<form action="#">
		<table>
			<tr>
				<th>공지</th>
				<td>공지 내용</td>
			</tr>
		</table>
	</form>
	<form action="#">
		<table>
			<tr>
				<th>굿즈</th>
			</tr>
			<tr>
				<td><a href="gifthub"><img
						src="./resources/store/item_cover/gift.png" width="100"></a></td>
				<c:forEach items="${list}" var="s">
					<td><a href="store_detail?pd_number=${s.pd_number}"><img
							src="./resources/store/item_cover/${s.pd_photo}" width="100"><br>${s.pd_name}<br>${s.pd_price}</a></td>
				</c:forEach>
			</tr>
		</table>
	</form>
	<form action="#">
		<table>
			<tr>
				<th><input type="button" value="상품 등록" onclick="location.href='store_input'"></th>
			</tr>
		</table>
	</form>
</body>
</html>