package cn.bp.scada.sap.bean;
/**
 * mes工单表
 * @author Administrator
 *
 */
public class MesMoT {
	private String fct_cd ;//工厂
	private String item_cd ;//物料编号
	private String item_nm ;//物料名称
	private String plan_strt_dt ;//计划开始时间
	private String plan_end_dt ; //计划结束时间
	private String o_no ;//订单号
	public String getFct_cd() {
		return fct_cd;
	}
	public void setFct_cd(String fct_cd) {
		this.fct_cd = fct_cd;
	}
	public String getItem_cd() {
		return item_cd;
	}
	public void setItem_cd(String item_cd) {
		this.item_cd = item_cd;
	}
	public String getItem_nm() {
		return item_nm;
	}
	public void setItem_nm(String item_nm) {
		this.item_nm = item_nm;
	}
	public String getPlan_strt_dt() {
		return plan_strt_dt;
	}
	public void setPlan_strt_dt(String plan_strt_dt) {
		this.plan_strt_dt = plan_strt_dt;
	}
	public String getPlan_end_dt() {
		return plan_end_dt;
	}
	public void setPlan_end_dt(String plan_end_dt) {
		this.plan_end_dt = plan_end_dt;
	}
	public String getO_no() {
		return o_no;
	}
	public void setO_no(String o_no) {
		this.o_no = o_no;
	}
	@Override
	public String toString() {
		return "MesMoT [fct_cd=" + fct_cd + ", item_cd=" + item_cd + ", item_nm=" + item_nm + ", plan_strt_dt="
				+ plan_strt_dt + ", plan_end_dt=" + plan_end_dt + ", o_no=" + o_no + "]";
	}
	
	
}
                   