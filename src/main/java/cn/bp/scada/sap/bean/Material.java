package cn.bp.scada.sap.bean;
/**
 * 物料主表
 * @author Administrator
 *
 */
public class Material {
	private String  fct_code;//工厂编号
	private String  mt_matkl;//物料组
	private String  MT_WGBEZ;//物料组描述
	private String  IT_TP;//物料类型
	private String  MT_TNR;//物料编号
	private String  MT_DS;//物料描述
	private String  MT_MST;//物料状态
	private String  ct_date;//建档日期
	private String  MT_MST_DS;//物料状态描述
	private String  MEINS;//基本计量单位
	public String getFct_code() {
		return fct_code;
	}
	public void setFct_code(String fct_code) {
		this.fct_code = fct_code;
	}
	public String getMt_matkl() {
		return mt_matkl;
	}
	public void setMt_matkl(String mt_matkl) {
		this.mt_matkl = mt_matkl;
	}
	public String getMT_WGBEZ() {
		return MT_WGBEZ;
	}
	public void setMT_WGBEZ(String mT_WGBEZ) {
		MT_WGBEZ = mT_WGBEZ;
	}
	public String getIT_TP() {
		return IT_TP;
	}
	public void setIT_TP(String iT_TP) {
		IT_TP = iT_TP;
	}
	public String getMT_TNR() {
		return MT_TNR;
	}
	public void setMT_TNR(String mT_TNR) {
		MT_TNR = mT_TNR;
	}
	public String getMT_DS() {
		return MT_DS;
	}
	public void setMT_DS(String mT_DS) {
		MT_DS = mT_DS;
	}
	public String getMT_MST() {
		return MT_MST;
	}
	public void setMT_MST(String mT_MST) {
		MT_MST = mT_MST;
	}
	public String getCt_date() {
		return ct_date;
	}
	public void setCt_date(String ct_date) {
		this.ct_date = ct_date;
	}
	public String getMT_MST_DS() {
		return MT_MST_DS;
	}
	public void setMT_MST_DS(String mT_MST_DS) {
		MT_MST_DS = mT_MST_DS;
	}
	public String getMEINS() {
		return MEINS;
	}
	public void setMEINS(String mEINS) {
		MEINS = mEINS;
	}
          
	
}                