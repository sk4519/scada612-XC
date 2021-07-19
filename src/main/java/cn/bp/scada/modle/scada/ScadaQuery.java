package cn.bp.scada.modle.scada;

/**
 * 统一设备请求主表
 */
public class ScadaQuery {

  private String op_flag;//操作码(B05)
  private String device_sn;//设备条码
  private String work_sn;//工位SN
  private String emp_no;//操作员工号
  private String pro_sn;//pcb板sn，赋值为空
  private String box_sn;//赋值为空
  private String con_sn;//赋值为空，载具SN
  private String mo_num;//赋值为空
  private String result_flag;
  private String code;
	private String message;
	private String reqcode;
	private String frame_stion; //老化架所在位置
	private String material_stion; //老化架产品所放位置
	private String flow_code; //流程码


  public String getFlow_code() {
		return flow_code;
	}

	public void setFlow_code(String flow_code) {
		this.flow_code = flow_code;
	}

public String getResult_flag() {
		return result_flag;
	}

	public void setResult_flag(String result_flag) {
		this.result_flag = result_flag;
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

public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getReqcode() {
		return reqcode;
	}

	public void setReqcode(String reqcode) {
		this.reqcode = reqcode;
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
	return "ScadaQuery [op_flag=" + op_flag + ", device_sn=" + device_sn + ", work_sn=" + work_sn + ", emp_no=" + emp_no
			+ ", pro_sn=" + pro_sn + ", box_sn=" + box_sn + ", con_sn=" + con_sn + ", mo_num=" + mo_num
			+ ", result_flag=" + result_flag + ", code=" + code + ", message=" + message + ", reqcode=" + reqcode
			+ ", frame_stion=" + frame_stion + ", material_stion=" + material_stion + ", getResult_flag()="
			+ getResult_flag() + ", getFrame_stion()=" + getFrame_stion() + ", getMaterial_stion()="
			+ getMaterial_stion() + ", getCode()=" + getCode() + ", getMessage()=" + getMessage() + ", getReqcode()="
			+ getReqcode() + ", getOp_flag()=" + getOp_flag() + ", getDevice_sn()=" + getDevice_sn() + ", getWork_sn()="
			+ getWork_sn() + ", getEmp_no()=" + getEmp_no() + ", getPro_sn()=" + getPro_sn() + ", getBox_sn()="
			+ getBox_sn() + ", getCon_sn()=" + getCon_sn() + ", getMo_num()=" + getMo_num() + ", getClass()="
			+ getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
}


}
