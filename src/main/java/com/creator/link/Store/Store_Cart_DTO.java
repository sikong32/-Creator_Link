package com.creator.link.Store;

public class Store_Cart_DTO {
	int ct_pd_qnt;
	int pd_number;
	int mb_number;
	int os_number;
	int ct_stock;
	String ct_qnt_day;
	public Store_Cart_DTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Store_Cart_DTO(int ct_pd_qnt, int pd_number, int mb_number, int os_number, int ct_stock, String ct_qnt_day) {
		super();
		this.ct_pd_qnt = ct_pd_qnt;
		this.pd_number = pd_number;
		this.mb_number = mb_number;
		this.os_number = os_number;
		this.ct_stock = ct_stock;
		this.ct_qnt_day = ct_qnt_day;
	}
	public int getCt_pd_qnt() {
		return ct_pd_qnt;
	}
	public void setCt_pd_qnt(int ct_pd_qnt) {
		this.ct_pd_qnt = ct_pd_qnt;
	}
	public int getPd_number() {
		return pd_number;
	}
	public void setPd_number(int pd_number) {
		this.pd_number = pd_number;
	}
	public int getMb_number() {
		return mb_number;
	}
	public void setMb_number(int mb_number) {
		this.mb_number = mb_number;
	}
	public int getOs_number() {
		return os_number;
	}
	public void setOs_number(int os_number) {
		this.os_number = os_number;
	}
	public int getCt_stock() {
		return ct_stock;
	}
	public void setCt_stock(int ct_stock) {
		this.ct_stock = ct_stock;
	}
	public String getCt_qnt_day() {
		return ct_qnt_day;
	}
	public void setCt_qnt_day(String ct_qnt_day) {
		this.ct_qnt_day = ct_qnt_day;
	}
	
}
