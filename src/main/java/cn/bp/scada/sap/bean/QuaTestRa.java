package cn.bp.scada.sap.bean;
/**
 * 质检释放凭证
 * @author Administrator
 *
 */
public class QuaTestRa {
      private String  mblnr; //物料凭证编号
	  private Integer  zeile; //物料凭证中的项目
	  private Integer  mjahr; //物料凭证年度
	  private String  werks; //工厂
	  private String  ebeln; //采购凭证号 
	  private Integer  ebelp; //采购凭证的项目编号 
	  private String  charg; //批号
	  private String  meins; //基本计量单位
	  private Double  menge; //数量
	  private String  lifnr; //供应商或债权人的帐号
	  private String  bldat; //凭证中的凭证日期
	  private String  cputm; //输入时间
	  private String  matnr; //物料编号
	  private String  maktx; //物料描述
	  private String  usnam; //用户名 
	  private String  bktxt; //凭证抬头文本
	  private String  bwart; //移动类型(库存管理)
	  private String  shkzg; //借方/贷方标识
	  private String  insmk;//    库存类型
	  private String  name1  ;//  名称 1
	public String getMblnr() {
		return mblnr;
	}
	public void setMblnr(String mblnr) {
		this.mblnr = mblnr;
	}
	
	public String getWerks() {
		return werks;
	}
	public void setWerks(String werks) {
		this.werks = werks;
	}
	public String getEbeln() {
		return ebeln;
	}
	public void setEbeln(String ebeln) {
		this.ebeln = ebeln;
	}
	
	public String getCharg() {
		return charg;
	}
	public void setCharg(String charg) {
		this.charg = charg;
	}
	public String getMeins() {
		return meins;
	}
	public void setMeins(String meins) {
		this.meins = meins;
	}
	
	public Integer getZeile() {
		return zeile;
	}
	public void setZeile(Integer zeile) {
		this.zeile = zeile;
	}
	public Integer getMjahr() {
		return mjahr;
	}
	public void setMjahr(Integer mjahr) {
		this.mjahr = mjahr;
	}
	public Integer getEbelp() {
		return ebelp;
	}
	public void setEbelp(Integer ebelp) {
		this.ebelp = ebelp;
	}
	
	public Double getMenge() {
		return menge;
	}
	public void setMenge(Double menge) {
		this.menge = menge;
	}
	public String getLifnr() {
		return lifnr;
	}
	public void setLifnr(String lifnr) {
		this.lifnr = lifnr;
	}
	public String getBldat() {
		return bldat;
	}
	public void setBldat(String bldat) {
		this.bldat = bldat;
	}
	public String getCputm() {
		return cputm;
	}
	public void setCputm(String cputm) {
		this.cputm = cputm;
	}
	public String getMatnr() {
		return matnr;
	}
	public void setMatnr(String matnr) {
		this.matnr = matnr;
	}
	public String getMaktx() {
		return maktx;
	}
	public void setMaktx(String maktx) {
		this.maktx = maktx;
	}
	public String getUsnam() {
		return usnam;
	}
	public void setUsnam(String usnam) {
		this.usnam = usnam;
	}
	public String getBktxt() {
		return bktxt;
	}
	public void setBktxt(String bktxt) {
		this.bktxt = bktxt;
	}
	public String getBwart() {
		return bwart;
	}
	public void setBwart(String bwart) {
		this.bwart = bwart;
	}
	public String getShkzg() {
		return shkzg;
	}
	public void setShkzg(String shkzg) {
		this.shkzg = shkzg;
	}
	public String getInsmk() {
		return insmk;
	}
	public void setInsmk(String insmk) {
		this.insmk = insmk;
	}
	public String getName1() {
		return name1;
	}
	public void setName1(String name1) {
		this.name1 = name1;
	}
	  
}
                      