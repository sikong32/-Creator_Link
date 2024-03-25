<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>

<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<!-- CSS 경로  -->
	<link href="${pageContext.request.contextPath}/resources/css/top.css" rel="stylesheet" type="text/css">
	<!-- 적용 글꼴  -->
	<link rel="preconnect" href="https://fonts.googleapis.com">
	<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
	<link href="https://fonts.googleapis.com/css2?family=Madimi+One&display=swap" rel="stylesheet">
	<!-- 아이콘 적용 설정  -->
	<script src="https://kit.fontawesome.com/5c1c1e3491.js" crossorigin="anonymous"></script>
	<title>CreatorLink_</title>
</head>

<body>

	<!-- 네비게이션 바  -->
	<nav class="navbar">
		<!-- 로고 -->
		<div class="navbar_logo">
			<a href="index"><img src="./resources/cssimage/logo.PNG" width="120px" height="100px">Creator Link</a>
		</div>
		
		<!-- 기본 메뉴  <i class="fa-solid fa-store"></i>-->
		<ul class="navbar_menu">
			<li><a href="store_main">스토어</a></li>
			<li><a href="board_main">게시판</a></li>
		</ul>
		
		<!-- 검색창 -->
		<div class="navbar_search">
			<form class="search_box" action="#" method="get">
				<input class="search_txt" type="text" name="" placeholder="검색어를 입력하세요">
				<button class="search_btn" type="submit">
					<i class="fa-solid fa-magnifying-glass"></i>
				</button>
			</form>
		</div>
		
		<!-- 로그인 (로그인 전 글자) -->
		<ul class="navbar_login">
			<li><a href="login">로그인</a></li>
			<li><a href="regist">회원가입</a></li>
		</ul>	
		
		<!-- 로그인 (로그인 후 아이콘)
		<ul class="navbar_icon">
			<li><a href=""><i class="fa-regular fa-star"></i></a></li>
			<li><a href=""><i class="fa-regular fa-heart"></i></a></li>
			<li><a href=""><i class="fa-solid fa-cart-shopping"></i></a></li>
			<li><a href=""><i class="fa-regular fa-user"></i></a></li>
		</ul> -->
		
		<!-- 반응형 웹 기능 (토글 버튼) -->
		<a href="#" class="toogle_bnt"><i class="fa-solid fa-bars"></i></a>
	</nav>

</body>

</html>