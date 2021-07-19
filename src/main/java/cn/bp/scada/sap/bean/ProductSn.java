package cn.bp.scada.sap.bean;
/**
 * 产品SN
 * @author Administrator
 *
 */
public class ProductSn {

	private String sernr;
	private String aufnr;
	public String getSernr() {
		return sernr;
	}
	public void setSernr(String sernr) {
		this.sernr = sernr;
	}
	public String getAufnr() {
		return aufnr;
	}
	public void setAufnr(String aufnr) {
		this.aufnr = aufnr;
	}
	@Override
	public String toString() {
		return "ProductSn [sernr=" + sernr + ", aufnr=" + aufnr + "]";
	}
	
	
	
}
