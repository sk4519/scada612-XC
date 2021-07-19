package cn.bp.scada.sap.bean;
/**
 * 原厂SN集中绑定PN
 * @author Administrator
 *
 */
public class YCSNOrPN {
	private String matnr; //物料编号
	private String charg; //批号,浪潮PN
	private String ycqn ; //原厂SN
	private String maktx; //物料描述
	private String erdate   ; // 创建日期
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
	public String getYcqn() {
		return ycqn;
	}
	public void setYcqn(String ycqn) {
		this.ycqn = ycqn;
	}
	public String getMaktx() {
		return maktx;
	}
	public void setMaktx(String maktx) {
		this.maktx = maktx;
	}
	public String getErdate() {
		return erdate;
	}
	public void setErdate(String erdate) {
		this.erdate = erdate;
	}
	
	
}
