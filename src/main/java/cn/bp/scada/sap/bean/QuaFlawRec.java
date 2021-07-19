package cn.bp.scada.sap.bean;
/**
 * 质量缺陷记录
 * @author Administrator
 *
 */
public class QuaFlawRec {
	 private String pzh  ; //凭证号 
	 private String hxm  ; //                                                            
	 private String jyly ; //检验来源 
	 private String qn   ; //QN号 
	 private String sn   ; //SN
	 private String werks; //工厂
	 private String prodh; //物料编号
	public String getPzh() {
		return pzh;
	}
	public void setPzh(String pzh) {
		this.pzh = pzh;
	}
	public String getHxm() {
		return hxm;
	}
	public void setHxm(String hxm) {
		this.hxm = hxm;
	}
	public String getJyly() {
		return jyly;
	}
	public void setJyly(String jyly) {
		this.jyly = jyly;
	}
	public String getQn() {
		return qn;
	}
	public void setQn(String qn) {
		this.qn = qn;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getWerks() {
		return werks;
	}
	public void setWerks(String werks) {
		this.werks = werks;
	}
	public String getProdh() {
		return prodh;
	}
	public void setProdh(String prodh) {
		this.prodh = prodh;
	}
	
	 
}                            
