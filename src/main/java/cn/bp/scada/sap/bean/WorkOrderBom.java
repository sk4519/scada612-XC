package cn.bp.scada.sap.bean;
/**
 * 工单bom
 * @author Administrator
 *
 */
public class WorkOrderBom {
	 private String  matnr ; //物料编号
	 private String  maktx ; //物料描述
	 private String  menge ; //数量
	 private String  posnr ; //销售和分销凭证的项目号
	 private String  postp ; //项目类别（物料单） 
	 private String  idnrk ; //bom 组件
	 private String  idktx ; //物料描述
	 private String  idnge ; //以组件计量单位为准的已计算的组件数量
	 private String  meins ; //基本计量单位
	 private String  notes ; //                                                            
	 private String  pn    ; //wender pn
	 private String  id    ; //主键id
	 private String  in_dt ; //（插入时间）从erp读取资料的时间
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
	public String getMenge() {
		return menge;
	}
	public void setMenge(String menge) {
		this.menge = menge;
	}
	public String getPosnr() {
		return posnr;
	}
	public void setPosnr(String posnr) {
		this.posnr = posnr;
	}
	public String getPostp() {
		return postp;
	}
	public void setPostp(String postp) {
		this.postp = postp;
	}
	public String getIdnrk() {
		return idnrk;
	}
	public void setIdnrk(String idnrk) {
		this.idnrk = idnrk;
	}
	public String getIdktx() {
		return idktx;
	}
	public void setIdktx(String idktx) {
		this.idktx = idktx;
	}
	public String getIdnge() {
		return idnge;
	}
	public void setIdnge(String idnge) {
		this.idnge = idnge;
	}
	public String getMeins() {
		return meins;
	}
	public void setMeins(String meins) {
		this.meins = meins;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getPn() {
		return pn;
	}
	public void setPn(String pn) {
		this.pn = pn;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIn_dt() {
		return in_dt;
	}
	public void setIn_dt(String in_dt) {
		this.in_dt = in_dt;
	}
	 
	 
}                              