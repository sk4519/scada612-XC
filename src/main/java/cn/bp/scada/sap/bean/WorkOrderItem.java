package cn.bp.scada.sap.bean;
/**
 * 工单明细
 * @author Administrator
 *
 */
public class WorkOrderItem {
	 private String    aufnr       ; //订单号
	 private String    v_sj_matnr  ; //物料编号
	 private String    v_bdmng     ; //需求量
	 private String    v_sj_maktx  ; //物料描述
	 private String    dumps       ; //虚拟项目标识

	  private String  v_lgort      ;//存储位置
	  private String  	v_charg    ;//	批次编号
	  private String  	status     ;//	状态
	  private String  	proj_info  ;//	项目
	  private String  	matkl      ;//	物料组
	  private String  	wgbez      ;//	物料组描述
	  private String    pn         ;//	wender pn


	public String getV_lgort() {
		return v_lgort;
	}
	public void setV_lgort(String v_lgort) {
		this.v_lgort = v_lgort;
	}
	public String getV_charg() {
		return v_charg;
	}
	public void setV_charg(String v_charg) {
		this.v_charg = v_charg;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getProj_info() {
		return proj_info;
	}
	public void setProj_info(String proj_info) {
		this.proj_info = proj_info;
	}
	public String getMatkl() {
		return matkl;
	}
	public void setMatkl(String matkl) {
		this.matkl = matkl;
	}
	public String getWgbez() {
		return wgbez;
	}
	public void setWgbez(String wgbez) {
		this.wgbez = wgbez;
	}
	public String getPn() {
		return pn;
	}
	public void setPn(String pn) {
		this.pn = pn;
	}
	public String getAufnr() {
		return aufnr;
	}
	public void setAufnr(String aufnr) {
		this.aufnr = aufnr;
	}
	public String getV_sj_matnr() {
		return v_sj_matnr;
	}
	public void setV_sj_matnr(String v_sj_matnr) {
		this.v_sj_matnr = v_sj_matnr;
	}
	public String getV_bdmng() {
		return v_bdmng;
	}
	public void setV_bdmng(String v_bdmng) {
		this.v_bdmng = v_bdmng;
	}
	public String getV_sj_maktx() {
		return v_sj_maktx;
	}
	public void setV_sj_maktx(String v_sj_maktx) {
		this.v_sj_maktx = v_sj_maktx;
	}
	public String getDumps() {
		return dumps;
	}
	public void setDumps(String dumps) {
		this.dumps = dumps;
	}

	@Override
	public String toString() {
		return "WorkOrderItem{" +
				"aufnr='" + aufnr + '\'' +
				", v_sj_matnr='" + v_sj_matnr + '\'' +
				", v_bdmng='" + v_bdmng + '\'' +
				", v_sj_maktx='" + v_sj_maktx + '\'' +
				", dumps='" + dumps + '\'' +
				", v_lgort='" + v_lgort + '\'' +
				", v_charg='" + v_charg + '\'' +
				", status='" + status + '\'' +
				", proj_info='" + proj_info + '\'' +
				", matkl='" + matkl + '\'' +
				", wgbez='" + wgbez + '\'' +
				", pn='" + pn + '\'' +
				'}';
	}
}
