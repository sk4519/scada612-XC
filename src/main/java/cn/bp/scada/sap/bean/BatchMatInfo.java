package cn.bp.scada.sap.bean;
/**
 * 批次物料信息
 * @author Administrator
 *
 */
public class BatchMatInfo {
	 private String matnr  ; //物料编号
	 private String charg  ; //批号
	 private String maktx  ; //物料描述
	 private String ersda  ; //创建日期
	 private String lifnr  ; //供应商或债权人的帐号
	 private String name1  ; //名称 1
	public String getMatnr() {
		return matnr;
	}
	public void setMatnr(String matnr) {
		this.matnr = matnr;
	}
	public String getCharg() {
		return charg;
	}
	public void setCharg(String charg) {
		this.charg = charg;
	}
	public String getMaktx() {
		return maktx;
	}
	public void setMaktx(String maktx) {
		this.maktx = maktx;
	}
	public String getErsda() {
		return ersda;
	}
	public void setErsda(String ersda) {
		this.ersda = ersda;
	}
	public String getLifnr() {
		return lifnr;
	}
	public void setLifnr(String lifnr) {
		this.lifnr = lifnr;
	}
	public String getName1() {
		return name1;
	}
	public void setName1(String name1) {
		this.name1 = name1;
	}
	@Override
	public String toString() {
		return "BatchMatInfo [matnr=" + matnr + ", charg=" + charg + ", maktx=" + maktx + ", ersda=" + ersda
				+ ", lifnr=" + lifnr + ", name1=" + name1 + "]";
	}
	 
	 
}
