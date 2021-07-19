package cn.bp.scada.modle.scada;

/**
 * 统一设备请求主表
 */
public class ScadaRequest {

	private String op_flag;// 操作码(B05)
	private String device_sn;// 设备条码
	private String work_sn;// 工位SN
	private String emp_no;// 操作员工号
	private String pro_sn;// pcb板sn，赋值为空
	private String box_sn;// 赋值为空
	private String con_sn;// 赋值为空，载具SN
	private String mo_num;// 赋值为空
	private String mat_point; // 物料点位
	private String et_ip; // 设备IP
	private String flow_code;
	private String frame_stion; // 老化架所在位置
	private String material_stion; // 老化架产品所放位置
	private String result_message;
	private String status;



	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResult_message() {
		return result_message;
	}

	public void setResult_message(String result_message) {
		this.result_message = result_message;
	}

	public String getFrame_stion() {
		return frame_stion;
	}

	public void setFrame_stion(String frame_stion) {
		this.frame_stion = frame_stion;
	}

	public String getMaterial_stion() {
		return material_stion;
	}

	public void setMaterial_stion(String material_stion) {
		this.material_stion = material_stion;
	}

	public String getFlow_code() {
		return flow_code;
	}

	public void setFlow_code(String flow_code) {
		this.flow_code = flow_code;
	}

	public String getEt_ip() {
		return et_ip;
	}

	public void setEt_ip(String et_ip) {
		this.et_ip = et_ip;
	}

	public String getMat_point() {
		return mat_point;
	}

	public void setMat_point(String mat_point) {
		this.mat_point = mat_point;
	}

	public String getOp_flag() {
		return op_flag;
	}

	public void setOp_flag(String op_flag) {
		this.op_flag = op_flag;
	}

	public String getDevice_sn() {
		return device_sn;
	}

	public void setDevice_sn(String device_sn) {
		this.device_sn = device_sn;
	}

	public String getWork_sn() {
		return work_sn;
	}

	public void setWork_sn(String work_sn) {
		this.work_sn = work_sn;
	}

	public String getEmp_no() {
		return emp_no;
	}

	public void setEmp_no(String emp_no) {
		this.emp_no = emp_no;
	}

	public String getPro_sn() {
		return pro_sn;
	}

	public void setPro_sn(String pro_sn) {
		this.pro_sn = pro_sn;
	}

	public String getBox_sn() {
		return box_sn;
	}

	public void setBox_sn(String box_sn) {
		this.box_sn = box_sn;
	}

	public String getCon_sn() {
		return con_sn;
	}

	public void setCon_sn(String con_sn) {
		this.con_sn = con_sn;
	}

	public String getMo_num() {
		return mo_num;
	}

	public void setMo_num(String mo_num) {
		this.mo_num = mo_num;
	}

	@Override
	public String toString() {
		return "ScadaRequest [op_flag=" + op_flag + ", device_sn=" + device_sn + ", work_sn=" + work_sn + ", emp_no="
				+ emp_no + ", pro_sn=" + pro_sn + ", box_sn=" + box_sn + ", con_sn=" + con_sn + ", mo_num=" + mo_num
				+ ", mat_point=" + mat_point + ", et_ip=" + et_ip + ", flow_code=" + flow_code + ", frame_stion="
				+ frame_stion + ", material_stion=" + material_stion + ", result_message=" + result_message + "]";
	}

}
