package com.xinyuangongxiang.shop.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 提交订单Bean
 * @author KingKong·HE
 * @Time 2014年2月14日 下午5:09:16
 * @E-mail hjgang@bizpoer.com
 */
public class BuyStep1 {
	public static class Attr{
		public static final String STORE_CART_LIST = "store_cart_list";
		public static final String FREIGHT_HASH = "freight_hash";
		public static final String ADDRESS_INFO = "address_info";
		public static final String IFSHOW_OFFPAY = "ifshow_offpay";
		public static final String VAT_HASH = "vat_hash";
		public static final String INV_INFO = "inv_info";
		public static final String AVAILABLE_PREDEPOSIT = "available_predeposit";
		public static final String AVAILABLE_RC_BALANCE = "available_rc_balance";
		public static final String MEMBER_AVAILABLE_HEALTHBEAN = "member_available_healthbean";
		public static final String RPT_LIST = "rpt_list";
		public static final String HEALTHBEAN_ALLOW = "healthbean_allow";

	}
	
	private String store_cart_list;
	private String freight_hash;
	private String address_info;
	private String ifshow_offpay;
	private String vat_hash;
	private String inv_info;
	private String available_predeposit;
	private String available_rc_balance;
	private String member_available_healthbean;
	private String rpt_list;
	private String healthbean_allow;

	public BuyStep1() {
	}

	public BuyStep1(String store_cart_list, String freight_hash,
			String address_info, String ifshow_offpay, String vat_hash,
			String inv_info, String available_predeposit,String available_rc_balance,String member_available_healthbean,String rpt_list,String healthbean_allow) {
		super();
		this.store_cart_list = store_cart_list;
		this.freight_hash = freight_hash;
		this.address_info = address_info;
		this.ifshow_offpay = ifshow_offpay;
		this.vat_hash = vat_hash;
		this.inv_info = inv_info;
		this.available_predeposit = available_predeposit;
		this.available_rc_balance = available_rc_balance;
		this.member_available_healthbean = member_available_healthbean;
		this.rpt_list = rpt_list;
		this.healthbean_allow = healthbean_allow;
	}

	public static BuyStep1 newInstanceList(String json){
		BuyStep1 bean = null;
		try {
			JSONObject obj = new JSONObject(json);
			if(obj.length()> 0){
				String store_cart_list = obj.optString(Attr.STORE_CART_LIST);
				String freight_hash = obj.optString(Attr.FREIGHT_HASH);
				String address_info = obj.optString(Attr.ADDRESS_INFO);
				String ifshow_offpay = obj.optString(Attr.IFSHOW_OFFPAY);
				String vat_hash = obj.optString(Attr.VAT_HASH);
				String inv_info = obj.optString(Attr.INV_INFO);
				String available_predeposit = obj.optString(Attr.AVAILABLE_PREDEPOSIT);
				String available_rc_balance = obj.optString(Attr.AVAILABLE_RC_BALANCE);
				String member_available_healthbean = obj.optString(Attr.MEMBER_AVAILABLE_HEALTHBEAN);
				String healthbean_allow = obj.optString(Attr.HEALTHBEAN_ALLOW);
				String rpt_list = obj.optString(Attr.RPT_LIST);
				 bean = new BuyStep1(store_cart_list, freight_hash, address_info, ifshow_offpay,
						 vat_hash, inv_info, available_predeposit,available_rc_balance,member_available_healthbean,rpt_list,healthbean_allow);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return bean;
	}

	public String getHealthbean_allow() {
		return healthbean_allow;
	}

	public void setHealthbean_allow(String healthbean_allow) {
		this.healthbean_allow = healthbean_allow;
	}

	public String getMember_available_healthbean() {
		return member_available_healthbean;
	}

	public void setMember_available_healthbean(String member_available_healthbean) {
		this.member_available_healthbean = member_available_healthbean;
	}

	public String getAvailable_rc_balance() {
		return available_rc_balance;
	}

	public void setAvailable_rc_balance(String available_rc_balance) {
		this.available_rc_balance = available_rc_balance;
	}

	public String getStore_cart_list() {
		return store_cart_list;
	}

	public void setStore_cart_list(String store_cart_list) {
		this.store_cart_list = store_cart_list;
	}

	public String getFreight_hash() {
		return freight_hash;
	}

	public void setFreight_hash(String freight_hash) {
		this.freight_hash = freight_hash;
	}

	public String getAddress_info() {
		return address_info;
	}

	public void setAddress_info(String address_info) {
		this.address_info = address_info;
	}

	public String getIfshow_offpay() {
		return ifshow_offpay;
	}

	public void setIfshow_offpay(String ifshow_offpay) {
		this.ifshow_offpay = ifshow_offpay;
	}

	public String getVat_hash() {
		return vat_hash;
	}

	public void setVat_hash(String vat_hash) {
		this.vat_hash = vat_hash;
	}

	public String getInv_info() {
		return inv_info;
	}

	public void setInv_info(String inv_info) {
		this.inv_info = inv_info;
	}

	public String getAvailable_predeposit() {
		return available_predeposit;
	}

	public void setAvailable_predeposit(String available_predeposit) {
		this.available_predeposit = available_predeposit;
	}

	public String getRpt_list() {
		return rpt_list;
	}

	public void setRpt_list(String rpt_list) {
		this.rpt_list = rpt_list;
	}

	@Override
	public String toString() {
		return "BuyStep1{" +
				"store_cart_list='" + store_cart_list + '\'' +
				", freight_hash='" + freight_hash + '\'' +
				", address_info='" + address_info + '\'' +
				", ifshow_offpay='" + ifshow_offpay + '\'' +
				", vat_hash='" + vat_hash + '\'' +
				", inv_info='" + inv_info + '\'' +
				", available_predeposit='" + available_predeposit + '\'' +
				", available_rc_balance='" + available_rc_balance + '\'' +
				", member_available_healthbean='" + member_available_healthbean + '\'' +
				", rpt_list='" + rpt_list + '\'' +
				'}';
	}
}
