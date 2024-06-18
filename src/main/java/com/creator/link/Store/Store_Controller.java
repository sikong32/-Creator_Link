package com.creator.link.Store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.creator.link.Paging;
import com.creator.link.Member.Member_DTO;

@Controller
public class Store_Controller {
	@Autowired
	SqlSession sqlSession;

	@Autowired
	ServletContext servletContext;

	String imagePath;
	
	@PostConstruct
	public void init() {
        Properties properties = new Properties();
        try {
        	String Path = servletContext.getRealPath(".");
            properties.load(new FileInputStream(Path+"/META-INF/maven/com.creator/link/pom.properties"));
            String projectLocation = properties.getProperty("m2e.projectLocation");
            imagePath = projectLocation + "/src/main/webapp/resources/store/item_cover/";
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@RequestMapping(value = "store_main")
	public String store_main(HttpServletRequest request,Model model) {
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		String now_page = request.getParameter("now_page");
		if(now_page==null) now_page = "1";
		String view_per_page = request.getParameter("view_per_page");
		if(view_per_page == null) view_per_page = "12";
		int value_of_total = ss.pd_total();
		Paging page = new Paging(Integer.parseInt(now_page),Integer.parseInt(view_per_page),value_of_total);
//		System.out.println("start "+page.getStart_value()+"end "+page.getEnd_value());
		ArrayList<Store_DTO> list = ss.store_mainout(page);
		model.addAttribute("list",list);
		model.addAttribute("page",page);
		return "store_main";
	}
	@RequestMapping(value = "store_main_category")
	public String store_main_category(HttpServletRequest request,Model model) {
		String category = request.getParameter("category");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		ArrayList<Store_DTO> list = ss.store_main_category_out(category);
		model.addAttribute("list",list);
		return "store_main";
	}
	@RequestMapping(value = "order_list")
	public String order_list(HttpServletRequest request,Model model) {
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		if(member!=null) {
			String member_number = member.getMb_number();
			ArrayList<Order_DTO> od_list = ss.order_list(member_number);
			for (int i = 0; i < od_list.size(); i++) {
				String image = null;
				if(od_list.get(i).getOs_number()!=0) {
					Store_OS_DTO os_list = ss.select_os(od_list.get(i).getOs_number());
					String os_name = os_list.getOs_1name()+os_list.getOs_2name()+os_list.getOs_3name();
					image = os_list.getOs_photo();
					od_list.get(i).setOs_name(os_name.replace("null", ""));
					od_list.get(i).setImage(image);
				}else {
					Store_DTO pd_list = ss.select_pd(od_list.get(i).getOd_pd_number());
					image = pd_list.getPd_photo();
					od_list.get(i).setImage(image);
				}
			}
			model.addAttribute("order_list",od_list);
		}
		
		return "order_list";
	}
	@ResponseBody
	@RequestMapping(value = "shopping_cart_save",method = RequestMethod.POST)
	public String shopping_cart(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("text/html;charset=utf-8");
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		int buy_quantity = Integer.parseInt(request.getParameter("buy_quantity"));
		if(request.getParameter("os_number")!=null) { //옵션 선택 이 있을 경우
			String[] os_numbers = request.getParameterValues("os_number");
			for (int i = 0; i < os_numbers.length; i++) {
				ss.cart_save_os(pd_number,member.getMb_number(),os_numbers[i],buy_quantity);
			}
		}else{
			int pd_duplication = ss.cart_select_pd(pd_number,member.getMb_number());
			if(pd_duplication >0) {
				ss.cart_up_stock(pd_number,member.getMb_number(),buy_quantity);
			}else {
				ss.cart_save_noos(pd_number,member.getMb_number(),buy_quantity);
			}
		}
		return 	"저장성공";
	}
	@ResponseBody
	@RequestMapping(value = "cart_delete",method = RequestMethod.POST)
	public String cart_delete(HttpServletRequest request,HttpServletResponse response) {
		response.setContentType("text/html;charset=utf-8");
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		int products = Integer.parseInt(request.getParameter("cartQuantity"));
		ss.cart_delete(products,member.getMb_number());
		return 	"저장성공";
	}
	@RequestMapping(value = "shopping_cart_view")
	public String shopping_cart_view(HttpServletRequest request, Model mo) {
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		ArrayList<Store_Cart_DTO> cart_list = ss.cart_select(member.getMb_number());
		ArrayList<Store_OS_DTO> os_list = new ArrayList<Store_OS_DTO>(); // 옵션 정보 초기화
		ArrayList<Store_DTO> pd_list = new ArrayList<Store_DTO>(); // 상품 정보 초기화
		ArrayList<Store_DTO> pd_all_list = new ArrayList<Store_DTO>(); // 상품 정보 초기화

		for (int i = 0; i < cart_list.size(); i++) {
			if(cart_list.get(i).getOs_number()!=0) {
				Store_OS_DTO os_number = ss.select_os(cart_list.get(i).getOs_number());
				os_number.setCt_pd_qnt(cart_list.get(i).getCt_pd_qnt());
				os_list.add(os_number);
			}else {
				Store_DTO pd_number = ss.select_pd(cart_list.get(i).getPd_number());
				pd_number.setPd_buy_su(cart_list.get(i).getCt_stock());
				pd_number.setCt_pd_qnt(cart_list.get(i).getCt_pd_qnt());
				pd_list.add(pd_number);
			}
			Store_DTO pd_all_number = ss.select_pd(cart_list.get(i).getPd_number());
			pd_all_list.add(pd_all_number);
		}
		mo.addAttribute("cart_list",cart_list);
		mo.addAttribute("os_list",os_list);
		mo.addAttribute("pd_list",pd_list);
		mo.addAttribute("pd_all_list",pd_all_list);
		return "shopping_cart_view";
	}
	@ResponseBody
	@RequestMapping(value = "os_number_get",method = RequestMethod.GET)
	public String os_number_get(HttpServletRequest request) {
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		String[] os_names = request.getParameter("os_names").split(",");
		int os_number = 0;
		if(os_names.length == 1) {
			os_number = ss.select_os_3number(pd_number,os_names[0]);
		}else if(os_names.length == 2) {
			os_number = ss.select_os_2number(pd_number,os_names[0],os_names[1]);
		}else if(os_names.length == 3) {
			os_number = ss.select_os_1number(pd_number,os_names[0],os_names[1],os_names[2]);
		}
		return String.valueOf(os_number);
	}
	@RequestMapping(value = "store_detail")
	public String store_detail(HttpServletRequest request,Model model) {
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		Store_DTO dto = ss.store_detail(pd_number);
		ArrayList<Store_OS_DTO> list = ss.store_os_detail(pd_number);
		ArrayList<String> os_1name = ss.os_1name_search(pd_number);
		ArrayList<String> os_2name = ss.os_2name_search(pd_number);
		ArrayList<String> os_3name = ss.os_3name_search(pd_number);
		ArrayList<Review_DTO> re_list = ss.review_search(pd_number);
		if(member!=null) {
			String review = null;
			ArrayList<Order_DTO> od_list = ss.order_list(member.getMb_number());
			for (int i = 0; i < od_list.size(); i++) {
				if(od_list.get(i).getOd_pd_number()==pd_number) {
					review = "OK";
					model.addAttribute("review",review);
				}
				for (int j = 0; j < re_list.size(); j++) {
					if(od_list.get(i).getOd_pd_number()==pd_number && re_list.get(j).getMb_number()==Integer.parseInt(member.getMb_number())) {
						review = "modify";
						model.addAttribute("review",review);
					}
				}
			}
		}
		int star = 0;
		for (int i = 0; i < re_list.size(); i++) {
			star += re_list.get(i).getRe_star().length();
		}
		if(star!=0) {
			DecimalFormat df = new DecimalFormat("##0.00");
			double staravg = (double)star / re_list.size();
			model.addAttribute("staravg",df.format(staravg));
		}
		LocalDate today = LocalDate.now();
		model.addAttribute("re_list",re_list);
		model.addAttribute("dto",dto);
		model.addAttribute("list",list);
		model.addAttribute("os1name",os_1name);
		model.addAttribute("os2name",os_2name);
		model.addAttribute("os3name",os_3name);
		model.addAttribute("today",today.plusDays(3));
		return "store_detail";
	}
	@RequestMapping(value = "review_input")
	public String review_input(HttpServletRequest request,Model model) {
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		Store_DTO dto = ss.store_detail(pd_number);
		model.addAttribute("dto",dto);
		return "review_input";
	}
	@RequestMapping(value = "review_modify")
	public String review_modify(HttpServletRequest request,Model model) {
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		int re_number = Integer.parseInt(request.getParameter("re_number"));
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		Store_DTO dto = ss.store_detail(pd_number);
		Review_DTO re_dto = ss.review_modify(re_number);
		model.addAttribute("dto",dto);
		model.addAttribute("re_dto",re_dto);
		return "review_modify";
	}
	@ResponseBody
	@RequestMapping(value = "review_save")
	public String review_save(HttpServletRequest request,Model model) {
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		String review_text = request.getParameter("review_text");
		String re_star = request.getParameter("review_star");
		ss.review_save(pd_number,review_text,re_star,member.getMb_number());
		return "<script>window.close();window.opener.location.reload();</script>";
	}
	@ResponseBody
	@RequestMapping(value = "review_modify_save")
	public String review_modify_save(HttpServletRequest request,Model model) {
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		int re_number = Integer.parseInt(request.getParameter("re_number"));
		String review_text = request.getParameter("review_text");
		String re_star = request.getParameter("review_star");
		ss.review_update(re_number, review_text,re_star,member.getMb_number());
		return "<script>window.close();window.opener.location.reload();</script>";
	}
	@ResponseBody
	@RequestMapping(value = "review_delete", produces = "text/html; charset=UTF-8")
	public String review_delete(HttpServletRequest request,Model model) {
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		int re_number = Integer.parseInt(request.getParameter("re_number"));
		ss.review_delete(re_number, member.getMb_number());
		return "<script>alert('삭제 완료'); window.location.href = document.referrer;</script>";
	}
	@RequestMapping(value = "shoping_buy", method= RequestMethod.POST)
	public String shoping_buy(HttpServletRequest request,Model mo) {
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		String seller_mb_number = request.getParameter("mb_number");
		ArrayList<Store_DTO> pd_list = ss.select_pd_all(pd_number);
		ArrayList<Store_OS_DTO> os_list = null;
		if(request.getParameter("os_number")!=null) {
			String[] os_numbers = request.getParameterValues("os_number");
			for (int i = 0; i < os_numbers.length; i++) {
				os_list = ss.select_os_all(pd_number,Integer.parseInt(os_numbers[i]));
			}
			mo.addAttribute("pd_list",pd_list);
			mo.addAttribute("os_list",os_list);
		}
		int os_number = 0;
		if(request.getParameter("os_1")!=null&&request.getParameter("os_2")!=null&&request.getParameter("os_3")!=null) {
			String os_1 = request.getParameter("os_1");
			String os_2 = request.getParameter("os_2");
			String os_3 = request.getParameter("os_3");
			os_number = ss.select_os_1number(pd_number,os_1,os_2,os_3);
		}else if(request.getParameter("os_1")!=null&&request.getParameter("os_2")!=null) {
			String os_1 = request.getParameter("os_1");
			String os_2 = request.getParameter("os_2");
			os_number = ss.select_os_2number(pd_number,os_1,os_2);
		}else if(request.getParameter("os_1")!=null){
			String os_1 = request.getParameter("os_1");
			os_number = ss.select_os_3number(pd_number,os_1);
		}
		int buy_quantity = Integer.parseInt(request.getParameter("buy_quantity"));
		Store_DTO pddto = ss.store_detail(pd_number);
		if(os_number!=0) {
			Store_OS_DTO osdto = ss.select_os(os_number);
			String os_f_name = osdto.getOs_1name()+""+osdto.getOs_2name()+""+osdto.getOs_3name();
			String os_full_name = os_f_name.replace("null","");
			mo.addAttribute("osdto",osdto);
			mo.addAttribute("os_full_name",os_full_name);
		}
		mo.addAttribute("pddto",pddto);
		mo.addAttribute("buy_quantity",buy_quantity);
		mo.addAttribute("member",member);
		mo.addAttribute("seller_mb_number",seller_mb_number);
		return "shoping_buy";
	}
	@RequestMapping(value = "shoping_cart_buy", method= RequestMethod.POST)
	public String shoping_cart_buy(HttpServletRequest request,Model mo) {
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		ArrayList<Store_OS_DTO> os_list = new ArrayList<Store_OS_DTO>(); // 옵션 정보 초기화
		ArrayList<Store_DTO> pd_list = new ArrayList<Store_DTO>(); // 상품 정보 초기화
		if(request.getParameterValues("selectedPd")!=null) {
			String[] pd_numbers = request.getParameterValues("selectedPd");
			for (int i = 0; i < pd_numbers.length; i++) {
				Store_DTO pd_number = ss.select_pd(Integer.parseInt(pd_numbers[i]));
				pd_number.setPd_buy_su(Integer.parseInt(request.getParameter("buy_pd_stock_"+pd_numbers[i]))); //구매수량
				pd_list.add(pd_number);
			}
		}
		if(request.getParameterValues("selectedOs")!= null) {
			String[] os_numbers = request.getParameterValues("selectedOs");
			for (int i = 0; i < os_numbers.length; i++) {
				Store_OS_DTO os_number = ss.select_os(Integer.parseInt(os_numbers[i]));
				os_number.setOs_buy_su(Integer.parseInt(request.getParameter("buy_os_stock_"+os_numbers[i])));
				os_list.add(os_number);
				Store_DTO pd_all_number = ss.select_pd(os_list.get(i).getPd_number());
				if(os_list.get(i).getPd_number()==pd_all_number.getPd_number()) {
					os_list.get(i).setPd_name(pd_all_number.getPd_name());
				}
			}
		}
		mo.addAttribute("member",member);
		mo.addAttribute("os_list",os_list);
		mo.addAttribute("pd_list",pd_list);
		return "shoping_cart_buy";
	}
	@RequestMapping(value = "shoping_cart_buy_fix", method= RequestMethod.POST)
	public String shoping_cart_buy_fix(HttpServletRequest request,Model mo) {
//		구매번호:	배송주소:	결제금액:
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		//맨버 정보
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		String od_id = member.getMb_id();
		int mb_number = Integer.parseInt(member.getMb_number());
		//배송 주소 저장
		int zip_code = Integer.parseInt(request.getParameter("zip_code"));
		String dlvy_address = request.getParameter("dlvy_address");
		String dlvy_address_dong = request.getParameter("dlvy_address");
		String dlvy_detail = request.getParameter("dlvy_detail");
		String dlvy_comment = request.getParameter("dlvy_comment");
		// 제품 정보 자겨오기
		String od_cp_code = "0";
		if(request.getParameter("od_cp_code")!=null) {
			od_cp_code = request.getParameter("od_cp_code");
		}
		// 옵션 없는 제품
		String[] pd_numbers = null;
		int od_su = 0;
		if(request.getParameterValues("pd_number")!=null) {
			pd_numbers = (request.getParameterValues("pd_number"));
			for (int i = 0; i < pd_numbers.length; i++) {
				int pd_number = Integer.parseInt(pd_numbers[i]);
				int od_pd_qnt = Integer.parseInt(request.getParameterValues("pd_buy_quantity")[i]);
				int od_price = Integer.parseInt(request.getParameterValues("pdtot_price")[i]);
				String od_pd_name = request.getParameterValues("pd_name")[i];
				ss.od_insert(od_id,od_pd_name,od_pd_qnt,od_price,od_cp_code,mb_number,zip_code,dlvy_address,dlvy_address_dong,dlvy_detail,dlvy_comment,pd_number);
				ss.od_updata_pd(pd_number,od_pd_qnt);
			}
			od_su = pd_numbers.length;
		}
		// 옵션 있는 제품
		String[] os_numbers = null;
		if(request.getParameter("os_number")!=null) {
			os_numbers = (request.getParameterValues("os_number"));
			for (int i = 0; i < os_numbers.length; i++) {
				int os_number = Integer.parseInt(os_numbers[i]);
				int od_pd_qnt = Integer.parseInt(request.getParameterValues("os_buy_quantity")[i]);
				int od_price = Integer.parseInt(request.getParameterValues("ostot_price")[i]);
				int pd_number = Integer.parseInt(request.getParameterValues("os_pd_number")[i]);
				String od_pd_name = request.getParameterValues("os_pd_name")[i];
				//String od_os_name = request.getParameterValues("os_name")[i];
				ss.od_insert_os(od_id,od_pd_name,od_pd_qnt,od_price,od_cp_code,mb_number,os_number,zip_code,dlvy_address,dlvy_address_dong,dlvy_detail,dlvy_comment,pd_number);
				ss.od_updata_os(pd_number,od_pd_qnt,os_number);
			}
			od_su += os_numbers.length;
		}
		int od_price = Integer.parseInt(request.getParameter("all_tot_price"));
		mo.addAttribute("od_su",od_su);
		mo.addAttribute("od_address",dlvy_address+""+dlvy_detail+""+dlvy_comment);
		mo.addAttribute("od_price",od_price);
		return "shoping_cart_buy_fix";
	}
	@RequestMapping(value = "shoping_buy_fix")
	public String shoping_buy_fix(HttpServletRequest request,Model mo) {
//		구매번호:	배송주소:	결제금액:
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		//배송 주소 저장
		int zip_code = Integer.parseInt(request.getParameter("zip_code"));
		String dlvy_address = request.getParameter("dlvy_address");
		String dlvy_address_dong = request.getParameter("dlvy_address");
		String dlvy_detail = request.getParameter("dlvy_detail");
		String dlvy_comment = request.getParameter("dlvy_comment");
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		String od_id = member.getMb_id();
		String od_pd_name = request.getParameter("pd_name");
		int od_pd_qnt = Integer.parseInt(request.getParameter("buy_quantity"));
		int od_price = Integer.parseInt(request.getParameter("price"));
		String od_cp_code = "0";
		int mb_number = Integer.parseInt(member.getMb_number());
		if(request.getParameter("od_cp_code")!=null) {
			od_cp_code = request.getParameter("od_cp_code");
		}
		if(request.getParameter("os_number")!=null) {
			int os_number = Integer.parseInt(request.getParameter("os_number"));
			ss.od_insert_os(od_id,od_pd_name,od_pd_qnt,od_price,od_cp_code,mb_number,os_number,zip_code,dlvy_address,dlvy_address_dong,dlvy_detail,dlvy_comment,pd_number);
			ss.od_updata_os(pd_number,od_pd_qnt,os_number);
		}else {
			ss.od_insert(od_id,od_pd_name,od_pd_qnt,od_price,od_cp_code,mb_number,zip_code,dlvy_address,dlvy_address_dong,dlvy_detail,dlvy_comment,pd_number);
		}
		ss.od_updata_pd(pd_number,od_pd_qnt);
		int od_number = ss.od_select_number(od_id,od_pd_name,mb_number);
		mo.addAttribute("od_number",od_number);
		mo.addAttribute("od_address",dlvy_address+""+dlvy_detail+""+dlvy_comment);
		mo.addAttribute("od_price",od_price);
		return "shoping_buy_fix";
	}
	@RequestMapping(value = "editor", method = RequestMethod.POST)
	public String editor(HttpServletRequest request,Model model) {
		String editor = request.getParameter("editor");
		model.addAttribute("editor",editor);
		return "editorout";
	}
	@RequestMapping(value = "store_input")
	public String store_input(Model mo) {
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		ArrayList<Category_DTO> category = ss.category_all();
		mo.addAttribute("category",category);
		return "store_input";
	}
	@RequestMapping(value = "store_management")
	public String store_management(HttpServletRequest request,Model mo) {
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		// 회원 번호 가져오기
		HttpSession hs = request.getSession();
		Member_DTO dto = (Member_DTO)hs.getAttribute("member");
		int mb_number = Integer.parseInt(dto.getMb_number());
		ArrayList<Store_DTO> ma_list = ss.store_mana(mb_number);
		mo.addAttribute("ma_list",ma_list);
		return "store_management";
	}
	@RequestMapping(value = "store_product_modify")
	public String updateProduct(HttpServletRequest request,Model mo) {
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		//카테고리 가져오기
		ArrayList<Category_DTO> category = ss.category_all();
		mo.addAttribute("category",category);
		// 로그인 여부 확인
		HttpSession hs = request.getSession();
		if(hs.getAttribute("member")!=null) {
			int pd_number = Integer.parseInt(request.getParameter("pd_number"));
			Store_DTO pd_dto = ss.store_detail(pd_number);
			ArrayList<Store_OS_DTO> os_list = ss.store_os_detail(pd_number);
			mo.addAttribute("os_list",os_list);
			mo.addAttribute("pd_dto",pd_dto);
			return "store_product_modify";
		}else {
			return "login";
		}
	}
	@RequestMapping(value = "store_order_management")
	public String shopping_order_management(HttpServletRequest request,Model mo) {
		HttpSession hs = request.getSession();
		Member_DTO member = (Member_DTO)hs.getAttribute("member");
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		if(member!=null) {
			String member_number = member.getMb_number();
			ArrayList<Store_DTO> pd_list = ss.store_mana(Integer.parseInt(member_number));//내가 등록한 제품 가져오기
			ArrayList<Order_DTO> all_od_list = ss.order_list_all();//모든 구매 목록 가져오기
			ArrayList<Order_DTO> od_list = new ArrayList<Order_DTO>();//신규 구매목록
			//내가 등록한 상품이 있는지확인 후 있으면 모든 주문에서 내가 등록한 제품만 표시
			if(pd_list.size()!=0) {
				for (int i = 0; i < all_od_list.size(); i++) {
					for (int j = 0; j < pd_list.size(); j++) {
						if(pd_list.get(j).getPd_number() == all_od_list.get(i).getOd_pd_number()) {
							od_list.add(all_od_list.get(i));
						}
					}
				}
			//신규 구매 목록에 사진 넣기
			for(int i= 0; i< od_list.size(); i++) {
				String image = null;
				if(od_list.get(i).getOs_number()!=0) {
					Store_OS_DTO os_list = ss.select_os(od_list.get(i).getOs_number());
					String os_name = os_list.getOs_1name()+os_list.getOs_2name()+os_list.getOs_3name();
					image = os_list.getOs_photo();
					od_list.get(i).setOs_name(os_name.replace("null", ""));
					od_list.get(i).setImage(image);
				}else {
					Store_DTO pd_list1 = ss.select_pd(od_list.get(i).getOd_pd_number());
					image = pd_list1.getPd_photo();
					od_list.get(i).setImage(image);
				}
			}
				mo.addAttribute("order_list",od_list);
			}
		}
		return "store_order_management";

	}
	@RequestMapping(value = "store_product_modify_save", method = RequestMethod.POST)
	public String store_product_modify_save(MultipartHttpServletRequest mul,HttpServletRequest request) throws IllegalStateException, IOException {
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		// 회원 번호 가져오기
		HttpSession hs = request.getSession();
		if(hs.getAttribute("member")!=null) {
			Member_DTO dto = (Member_DTO)hs.getAttribute("member");
			int mb_number = Integer.parseInt(dto.getMb_number());
		
			// 대표 상품 정보 가져와서 저장
			int pd_number = Integer.parseInt(request.getParameter("pd_number"));
			String pd_category = mul.getParameter("pd_category");
			String pd_name = mul.getParameter("pd_name");
			int pd_price = Integer.parseInt(mul.getParameter("pd_price"));
			String pd_content = request.getParameter("pd_content");
			int pd_stock = Integer.parseInt(mul.getParameter("pd_stock"));
			
			
			MultipartFile mf = mul.getFile("pd_photo");
			String org_mf = request.getParameter("pd_org_photo");
			String pd_photo = null;
			if(mf == null || mf.isEmpty()) {
				pd_photo = org_mf;
			}else {
				pd_photo = filesave(mf.getOriginalFilename());
				mf.transferTo(new File(imagePath + pd_photo));
				
				// 기존 파일 삭제
				File delPhoto = new File(imagePath+org_mf);
				delPhoto.delete();
			}
			
			
			//옵션 정보 가져와서 저장
			if(request.getParameter("os_number")!=null) {
				for (int i = 0; i < request.getParameterValues("os_number").length; i++) {
					int os_number = Integer.parseInt(request.getParameterValues("os_number")[i]);
					int os_price = Integer.parseInt(request.getParameter("os_price_"+os_number));
					int os_stock = Integer.parseInt(request.getParameter("os_stock_"+os_number));
					String os_photo = pd_photo;
					MultipartFile mf1 = mul.getFile("os_photo_"+os_number);
					if(mf1 != null && !mf1.isEmpty()){
						os_photo = filesave(mf1.getOriginalFilename());
						mf1.transferTo(new File(imagePath + os_photo)); // 옵션사진파일저장
					}
					if(request.getParameter("os_1name"+os_number)!=null && request.getParameter("os_2name"+os_number)!=null && request.getParameter("os_3name"+os_number)!=null) {
						String os_1name = request.getParameter("os_1name"+os_number);
						String os_2name = request.getParameter("os_2name"+os_number);
						String os_3name = request.getParameter("os_3name"+os_number);
						ss.os3_up(os_number,os_1name,os_2name,os_3name,os_price,os_photo,os_stock);
					}else if(request.getParameter("os_1name"+os_number)!=null && request.getParameter("os_2name"+os_number)!=null){
						String os_1name = request.getParameter("os_1name"+os_number);
						String os_2name = request.getParameter("os_2name"+os_number);
						ss.os2_up(os_number,os_1name,os_2name,os_price,os_photo,os_stock);
					}else if(request.getParameter("os_1name"+os_number)!=null) {
						String os_1name = request.getParameter("os_1name"+os_number);
						ss.os1_up(os_number,os_1name,os_price,os_photo,os_stock);
					}
				}
				ss.store_pd_up(pd_number,pd_name, pd_price, pd_category, pd_content, pd_photo, pd_stock, mb_number);
			}else {
				ss.store_pd_up(pd_number,pd_name, pd_price, pd_category, pd_content, pd_photo, pd_stock, mb_number);
			}
		}else {
			return "login";
		}
		return "redirect:store_management";
	}
	@RequestMapping(value = "store_save", method = RequestMethod.POST)
	public String store_save2(MultipartHttpServletRequest mul,HttpServletRequest request) throws IllegalStateException, IOException {
		Store_Service ss = sqlSession.getMapper(Store_Service.class);
		// 회원 번호 가져오기
		HttpSession hs = request.getSession();
		Member_DTO dto = (Member_DTO)hs.getAttribute("member");
		int mb_number = Integer.parseInt(dto.getMb_number());
		// 대표 상품 정보 가져오기
		String pd_category = mul.getParameter("pd_category");
		String pd_name = mul.getParameter("pd_name");
		int pd_price = Integer.parseInt(mul.getParameter("pd_price"));
		String pd_content = request.getParameter("pd_content");
		int pd_stock = Integer.parseInt(mul.getParameter("pd_stock"));
		MultipartFile mf = mul.getFile("pd_photo");
		String pd_photo = filesave(mf.getOriginalFilename());
		mf.transferTo(new File(imagePath + pd_photo));
		//옵션
		if(request.getParameter("os_radio").equals("Y")&&request.getParameter("os_print_su")!=null) {
			int os_print_su = Integer.parseInt(request.getParameter("os_print_su"));
			ss.store_insert0(pd_name, pd_price, pd_category, pd_content, pd_photo, pd_stock, mb_number);
			int max_index = ss.store_max_index();
			for (int i = 1; i <= os_print_su; i++) {
				int os_price;
				if(request.getParameter("os_"+i+"price").equals("")){
					os_price = pd_price;
				}else {
					os_price = Integer.parseInt(request.getParameter("os_"+i+"price"));
				}
				int os_stock;
				if(request.getParameter("os_"+i+"stock").equals("")) {
					os_stock = 0;
				}else {
					os_stock = Integer.parseInt(request.getParameter("os_"+i+"stock"));
				}
				String os_photo = pd_photo;
				MultipartFile mf1 = mul.getFile("os_"+i+"photo");
				if(mf1 != null && !mf1.isEmpty()){
					os_photo = filesave(mf1.getOriginalFilename());
					mf1.transferTo(new File(imagePath + os_photo)); // 옵션사진파일저장
				}
				if(request.getParameter("os_1_1")!=null && request.getParameter("os_2_1")!=null && request.getParameter("os_3_1")!=null) {
					String os_1name = request.getParameter("os_1_"+i);
					String os_2name = request.getParameter("os_2_"+i);
					String os_3name = request.getParameter("os_3_"+i);
					ss.os3_insert(max_index,os_1name,os_2name,os_3name,os_price,os_photo,os_stock);
					}else if(request.getParameter("os_1_1")!=null && request.getParameter("os_2_1")!=null){
						String os_1name = request.getParameter("os_1_"+i);
						String os_2name = request.getParameter("os_2_"+i);
						ss.os2_insert(max_index,os_1name,os_2name,os_price,os_photo,os_stock);
					}else if(request.getParameter("os_1_1")!=null) {
						String os_1name = request.getParameter("os_1_"+i);
						ss.os1_insert(max_index,os_1name,os_price,os_photo,os_stock);
					}
			}
		}else {
			ss.store_insert0(pd_name, pd_price, pd_category, pd_content, pd_photo, pd_stock, mb_number);
		}
		return "redirect:store_main";
	}
	private String filesave(String fname) throws IOException {
		UUID uuid = UUID.randomUUID();
		String sname = uuid.toString()+"_"+fname;
		return sname;
	}
	

	@RequestMapping(value = "store_Delete", method = RequestMethod.GET)
	public String store_Delete(HttpServletRequest request) {
		Store_Service ss =sqlSession.getMapper(Store_Service.class);
		int pd_number = Integer.parseInt(request.getParameter("pd_number"));
		// 회원 번호 가져오기
		HttpSession hs = request.getSession();
		if(hs.getAttribute("member")!=null) {
			//Member_DTO dto = (Member_DTO)hs.getAttribute("member");
			//int mb_number = Integer.parseInt(dto.getMb_number());
			ss.pd_delete_up(pd_number);// 주문 내역 업데이트 맴버 넘버를 1로 수정하고 재고 0으로 설정
			ss.os_delete_up(pd_number);// 옵션 재고 0으로 설정
		}
//		Store_DTO dto =  ss.select_pd(pd_number);
//		String delete_Fname =  dto.getPd_photo();
//		ArrayList<Store_OS_DTO> osList = ss.store_os_detail(pd_number);
//		for(Store_OS_DTO Fname : osList) {
//			File delOsPhoto = new File(imagePath+Fname.getOs_photo());
//			delOsPhoto.delete();
//		}
//		File delPhoto = new File(imagePath+delete_Fname);
//		delPhoto.delete();	
//		ss.delete_pd(pd_number);
//		ss.delete_os(pd_number);
		return "redirect:/store_management";
	}
	//주문 제품 취소 사유
	@RequestMapping(value = "cancel", method = RequestMethod.GET)
	public String cancel(HttpServletRequest request) {
		Store_Service ss =sqlSession.getMapper(Store_Service.class);
		int od_number = Integer.parseInt(request.getParameter("od_number"));
		String cancel_text = request.getParameter("cancel_text");
		// 회원 번호 가져오기
		HttpSession hs = request.getSession();
		if(hs.getAttribute("member")!=null) {
			ss.od_cancel_up(od_number,cancel_text);// 
		}
		return "redirect:/store_order_management";
	}
	//주문 제품 송장 입력
		@RequestMapping(value = "dlvyAddress", method = RequestMethod.GET)
		public String dlvyAdd(HttpServletRequest request) {
			Store_Service ss =sqlSession.getMapper(Store_Service.class);
			String dlvyAdd = request.getParameter("dlvyAdd");
			int od_number = Integer.parseInt(request.getParameter("od_number"));
			// 회원 번호 가져오기
			HttpSession hs = request.getSession();
			if(hs.getAttribute("member")!=null) {
				ss.od_dlvyAdd_up(od_number,dlvyAdd);
			}
			return "redirect:/store_order_management";
		}
	
}
