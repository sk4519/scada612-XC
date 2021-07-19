package cn.bp.scada.sap.bean;

/**
 * 工单信息
 */
public class WorkOrder {
    private String wo_cd;  //主键id(工单号)
    private String o_no;  //订单号
    private String fct_code;  //工厂编号
    private String mt_tnr;  //物料编号
    private String mt_ds;  //物料描述
    private String m_pt_maktx;  //产品层次结构
    private String m_pt_matnr;  //订单的物料编号
    private String pc_md;  //产品型号
    private String o_num_sum;  //订单数量总计
    private String sp_code;  //商品编码
    private String ftrmi;  //实际下达日期
    private String ftrmi_tm;  //下达时间
    private String status;  //订单状态
    private String auart;  //订单类型

    private String v_pt_matnr;//订单的物料编号
    private String 	v_sernr  ;//	 序列号
    private String 	v_tdline ;//	 产品配置（产品层次）
    private String 	v_memo   ;//	 长文本
    private String 	rkrq     ;//	 入库日期
    private String 	rkrq_tm  ;//	 要求入库时间
    private String 	tech_inst;//	 技术指令号
    private String 	proj_name;//	 项目名称
    private String 	emergency;//	 是否加急
    private String 	dispo    ;//	 对订单的mrp控制者
    private String 	ernam    ;//	 输入者
    private String 	pzms     ;//	 配置描述
    private String 	wrkst    ;//	 基本物料
    private String 	bismt    ;//	 旧物料号
    private String 	zxghsj   ;//	 新希望供货时间
    private String 	vkbur    ;//	 销售办事处
    private String 	gstrs    ;//	 排产开始
    private String 	gsuzs    ;//	 计划开始（时间）
    private String 	gltrs    ;//	 计划完工
    private String 	gluzs    ;//	 计划完成时间
    private String 	arbpl    ;//	 工作中心
    private String 	aufnr_a  ;//	 阿里订单号
    private String 	cplb     ;//	 产品类别

    public String getV_pt_matnr() {
        return v_pt_matnr;
    }

    public void setV_pt_matnr(String v_pt_matnr) {
        this.v_pt_matnr = v_pt_matnr;
    }

    public String getV_sernr() {
        return v_sernr;
    }

    public void setV_sernr(String v_sernr) {
        this.v_sernr = v_sernr;
    }

    public String getV_tdline() {
        return v_tdline;
    }

    public void setV_tdline(String v_tdline) {
        this.v_tdline = v_tdline;
    }

    public String getV_memo() {
        return v_memo;
    }

    public void setV_memo(String v_memo) {
        this.v_memo = v_memo;
    }

    public String getRkrq() {
        return rkrq;
    }

    public void setRkrq(String rkrq) {
        this.rkrq = rkrq;
    }

    public String getRkrq_tm() {
        return rkrq_tm;
    }

    public void setRkrq_tm(String rkrq_tm) {
        this.rkrq_tm = rkrq_tm;
    }

    public String getTech_inst() {
        return tech_inst;
    }

    public void setTech_inst(String tech_inst) {
        this.tech_inst = tech_inst;
    }

    public String getProj_name() {
        return proj_name;
    }

    public void setProj_name(String proj_name) {
        this.proj_name = proj_name;
    }

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }

    public String getDispo() {
        return dispo;
    }

    public void setDispo(String dispo) {
        this.dispo = dispo;
    }

    public String getErnam() {
        return ernam;
    }

    public void setErnam(String ernam) {
        this.ernam = ernam;
    }

    public String getPzms() {
        return pzms;
    }

    public void setPzms(String pzms) {
        this.pzms = pzms;
    }

    public String getWrkst() {
        return wrkst;
    }

    public void setWrkst(String wrkst) {
        this.wrkst = wrkst;
    }

    public String getBismt() {
        return bismt;
    }

    public void setBismt(String bismt) {
        this.bismt = bismt;
    }

    public String getZxghsj() {
        return zxghsj;
    }

    public void setZxghsj(String zxghsj) {
        this.zxghsj = zxghsj;
    }

    public String getVkbur() {
        return vkbur;
    }

    public void setVkbur(String vkbur) {
        this.vkbur = vkbur;
    }

    public String getGstrs() {
        return gstrs;
    }

    public void setGstrs(String gstrs) {
        this.gstrs = gstrs;
    }

    public String getGsuzs() {
        return gsuzs;
    }

    public void setGsuzs(String gsuzs) {
        this.gsuzs = gsuzs;
    }

    public String getGltrs() {
        return gltrs;
    }

    public void setGltrs(String gltrs) {
        this.gltrs = gltrs;
    }

    public String getGluzs() {
        return gluzs;
    }

    public void setGluzs(String gluzs) {
        this.gluzs = gluzs;
    }

    public String getArbpl() {
        return arbpl;
    }

    public void setArbpl(String arbpl) {
        this.arbpl = arbpl;
    }

    public String getAufnr_a() {
        return aufnr_a;
    }

    public void setAufnr_a(String aufnr_a) {
        this.aufnr_a = aufnr_a;
    }

    public String getCplb() {
        return cplb;
    }

    public void setCplb(String cplb) {
        this.cplb = cplb;
    }

    public String getWo_cd() {
        return wo_cd;
    }

    public void setWo_cd(String wo_cd) {
        this.wo_cd = wo_cd;
    }

    public String getO_no() {
        return o_no;
    }

    public void setO_no(String o_no) {
        this.o_no = o_no;
    }

    public String getFct_code() {
        return fct_code;
    }

    public void setFct_code(String fct_code) {
        this.fct_code = fct_code;
    }

    public String getMt_tnr() {
        return mt_tnr;
    }

    public void setMt_tnr(String mt_tnr) {
        this.mt_tnr = mt_tnr;
    }

    public String getMt_ds() {
        return mt_ds;
    }

    public void setMt_ds(String mt_ds) {
        this.mt_ds = mt_ds;
    }

    public String getM_pt_maktx() {
        return m_pt_maktx;
    }

    public void setM_pt_maktx(String m_pt_maktx) {
        this.m_pt_maktx = m_pt_maktx;
    }

    public String getM_pt_matnr() {
        return m_pt_matnr;
    }

    public void setM_pt_matnr(String m_pt_matnr) {
        this.m_pt_matnr = m_pt_matnr;
    }

    public String getPc_md() {
        return pc_md;
    }

    public void setPc_md(String pc_md) {
        this.pc_md = pc_md;
    }

    public String getO_num_sum() {
        return o_num_sum;
    }

    public void setO_num_sum(String o_num_sum) {
        this.o_num_sum = o_num_sum;
    }

    public String getSp_code() {
        return sp_code;
    }

    public void setSp_code(String sp_code) {
        this.sp_code = sp_code;
    }

    public String getFtrmi() {
        return ftrmi;
    }

    public void setFtrmi(String ftrmi) {
        this.ftrmi = ftrmi;
    }

    public String getFtrmi_tm() {
        return ftrmi_tm;
    }

    public void setFtrmi_tm(String ftrmi_tm) {
        this.ftrmi_tm = ftrmi_tm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuart() {
        return auart;
    }

    public void setAuart(String auart) {
        this.auart = auart;
    }

	@Override
	public String toString() {
		return "WorkOrder [wo_cd=" + wo_cd + ", o_no=" + o_no + ", fct_code=" + fct_code + ", mt_tnr=" + mt_tnr
				+ ", mt_ds=" + mt_ds + ", m_pt_maktx=" + m_pt_maktx + ", m_pt_matnr=" + m_pt_matnr + ", pc_md=" + pc_md
				+ ", o_num_sum=" + o_num_sum + ", sp_code=" + sp_code + ", ftrmi=" + ftrmi + ", ftrmi_tm=" + ftrmi_tm
				+ ", status=" + status + ", auart=" + auart + ", v_pt_matnr=" + v_pt_matnr + ", v_sernr=" + v_sernr
				+ ", v_tdline=" + v_tdline + ", v_memo=" + v_memo + ", rkrq=" + rkrq + ", rkrq_tm=" + rkrq_tm
				+ ", tech_inst=" + tech_inst + ", proj_name=" + proj_name + ", emergency=" + emergency + ", dispo="
				+ dispo + ", ernam=" + ernam + ", pzms=" + pzms + ", wrkst=" + wrkst + ", bismt=" + bismt + ", zxghsj="
				+ zxghsj + ", vkbur=" + vkbur + ", gstrs=" + gstrs + ", gsuzs=" + gsuzs + ", gltrs=" + gltrs
				+ ", gluzs=" + gluzs + ", arbpl=" + arbpl + ", aufnr_a=" + aufnr_a + ", cplb=" + cplb + "]";
	}
    
    
}
