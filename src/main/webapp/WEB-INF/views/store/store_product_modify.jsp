<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<script src="https://cdn.ckeditor.com/ckeditor5/34.2.0/super-build/ckeditor.js"></script>
<script type="text/javascript" src="resources/js/store_option.js"></script>
<style type="text/css">
/* .item_input 영역의 스타일링 */
.item_input {
    max-width: 80vw; /* 최대 너비 설정 */
    margin: 1vw auto; /* 상하 마진 20px, 좌우 자동 중앙 정렬 */
    padding: 20px; /* 내부 여백 설정 */
    background-color: #ffffff; /* 배경색을 흰색으로 설정 */
    border-radius: 8px; /* 모서리를 둥글게 처리 */
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* 박스 그림자 추가 */
    font-size: 0.9vw; /* 폰트 크기를 뷰포트 너비의 1%로 설정 */
    overflow: hidden; /* 내용이 div를 넘어가면 숨김 */
    font-family:'Pretendard-Regular';
}

/* .item_input 내부의 목록 스타일링 */
.item_input ul {
    list-style-type: none; /* 목록 기호 제거 */
    padding: 0; /* 내부 여백 제거 */
}

/* .item_input 내부의 각 목록 항목 스타일링 */
.item_input li {
    margin-bottom: 10px; /* 각 항목 하단에 마진 추가 */
}

/* 텍스트, 숫자 입력 및 파일 선택 필드 스타일링 */
.item_input input[type="text"],
.item_input input[type="number"],
.item_input input[type="file"],
.item_input select,
.item_input textarea {
    width: 10vw; /* 너비를 부모 요소의 100%로 설정 */
    padding: 8px; /* 내부 패딩 설정 */
    margin-top: 2px; /* 상단 마진 설정 */
    border: 1px solid #ccc; /* 테두리 설정 */
    border-radius: 4px; /* 테두리 모서리 둥글게 처리 */
}

/* 라디오 버튼 스타일링 */
.item_input input[type="radio"] {
    margin-right: 5px; /* 오른쪽 마진 추가 */
}

/* 버튼 스타일링 */
.item_input input[type="button"] {
    background-color: #2a2f4f; /* 배경색 설정 */
    color: white; /* 글자색 흰색으로 설정 */
    border: none; /* 테두리 제거 */
    padding: 10px 20px; /* 패딩 설정 */
    text-align: center; /* 텍스트 가운데 정렬 */
    text-decoration: none; /* 텍스트 장식 제거 */
    display: inline-block; /* 인라인 블록으로 표시 */
    font-size: 16px; /* 글자 크기 설정 */
    margin: 4px 2px; /* 마진 설정 */
    cursor: pointer; /* 마우스 커서를 포인터로 설정 */
    border-radius: 4px; /* 테두리 모서리 둥글게 처리 */
}

/* 버튼 호버 시 스타일 변경 */
.item_input input[type="button"]:hover {
    background-color: #5a2f4f; /* 호버 시 배경색 변경 */
}
</style>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action="store_product_modify_save" id="form_input" method="post"	enctype="multipart/form-data">
		<div class="item_input">
			<ul>
				<li>기존 카테고리: ${pd_dto.pd_category}<input type="hidden" name="pd_number" value="${pd_dto.pd_number}"></li><br>
				<li>상품카테고리 선택</li><li><select name="pd_category" required="required"><c:forEach items="${category}" var="ca"><option value="${ca.prbt_big_cls}=>${ca.prdt_mid_cls}${ca.prdt_small_cls}">${ca.prbt_big_cls}=>${ca.prdt_mid_cls}${ca.prdt_small_cls}</option></c:forEach></select></li>
				<li>상품명</li><li><input type="text" name="pd_name" required="required" placeholder="최대 100자" maxlength="100" value="${pd_dto.pd_name}"></li>
				<li>상품가격</li><li><input type="number" name="pd_price" min="1" max="9999999999" required="required" placeholder="최소 1원" value="${pd_dto.pd_price}"></li>
				<li>상품대표 이미지</li>
				<li><label for="pd_photo">
				<img alt="" src="./resources/store/item_cover/${pd_dto.pd_photo}" id="pd_basic_photo" width="100px" style="max-height: 100px;">
				</label>
				<input type="file" id="pd_photo" name="pd_photo" onchange="previewImage(event)" style="display: none;" accept="image/png, image/jpeg, image/gif" >
				<input type="hidden" name="pd_org_photo" value="${pd_dto.pd_photo}"></li>
				<c:if test="${os_list.size()!=0}">
					<li>
						<c:forEach items="${os_list}" var="os">
							<input type="hidden" name="os_number" value="${os.os_number}">
							<li>
							<label for="os_photo_${os.os_number}">
							<c:if test="${not empty os.os_photo}"><img alt="" src="./resources/store/item_cover/${os.os_photo}" id="os_basic_photo_${os.os_number}" width="100px" style="max-height: 100px;"></c:if>
							</label>
							<input type="file" name="os_photo_${os.os_number}" id="os_photo_${os.os_number}" onchange="os_previewImage(event,${os.os_number})" value="${os.os_photo}" style="display: none;" accept="image/png, image/jpeg, image/gif">
							옵션 이름: <input type="text" name="os_1name_${os.os_number}" id="os_1name_${os.os_number}" value="${os.os_1name}">
							<c:if test="${not empty os.os_2name}"><input type="text" name="os_2name_${os.os_number}" id="os_2name_${os.os_number}" value="${os.os_2name}"></c:if>
							<c:if test="${not empty os.os_3name}"><input type="text" name="os_3name_${os.os_number}" id="os_3name_${os.os_number}" value="${os.os_3name}"></c:if>
							옵션 가격: <input type="number" name="os_price_${os.os_number}" id="os_price_${os.os_number}" value="${os.os_price}">
							옵션 재고: <input type="number" name="os_stock_${os.os_number}" id="os_stock_${os.os_number}" value="${os.os_stock}" onchange="tot_stock()" class="os_input_stock"></li>
						</c:forEach>
					</li>
				</c:if>
				<li>상품 모든 재고</li><li><input type="number" min="0" max="9999999999" id="pd_stock" name="pd_stock" required="required" placeholder="0개 입력시 품절" value="${pd_dto.pd_stock}"></li>
				<li>상세페이지</li>
				<li><textarea id="editor" name="pd_content" cols="20" rows="5">${pd_dto.pd_content}</textarea></li>
				<li><input type="button" value="수정" onclick="val_check()"></li>
			</ul>
		</div>
	</form>
</body>
<script type="text/javascript" src="resources/js/input_editor.js"></script>
<script type="text/javascript" src="resources/js/editor_check.js"></script>
<script type="text/javascript">
function previewImage(event) { //이미지 미리보기 함수
    const input = event.target;
    const preview = document.getElementById('pd_basic_photo');
    const file = input.files[0];
    const v_image = ['image/jpeg', 'image/png', 'image/gif'];//이미지 타입 체크
    const reader = new FileReader();
	if(file && v_image.includes(file.type)){
	    reader.onload = function(){
	        preview.src = reader.result;
	    };
	    reader.readAsDataURL(file);
	}else{
		alert("이미지 파일만 업로드 가능합니다.");
		this.value=""; //입력된 이미지 초기화
	}
}
function os_previewImage(event,os_number) { //옵션이미지 미리보기 함수
	const input = event.target;
	const preview = document.getElementById('os_basic_photo_'+os_number);
	const file = input.files[0];
    const v_image = ['image/jpeg', 'image/png', 'image/gif'];//이미지 타입 체크
	const reader = new FileReader();
	if(file && v_image.includes(file.type)){
	    reader.onload = function(){
	        preview.src = reader.result;
	    };
	    reader.readAsDataURL(file);
	}else{
		alert("이미지 파일만 업로드 가능합니다.");
		this.value=""; //입력된 이미지 초기화
	}
}
</script>
</html>