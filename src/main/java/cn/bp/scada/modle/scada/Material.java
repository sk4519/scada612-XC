package cn.bp.scada.modle.scada;

public class Material {
	private String material_code;
	private String material_qty;
	private String material_sn;
	private String material_pn;


	public String getMaterial_sn() {
		return material_sn;
	}

	public void setMaterial_sn(String material_sn) {
		this.material_sn = material_sn;
	}

	public String getMaterial_pn() {
		return material_pn;
	}

	public void setMaterial_pn(String material_pn) {
		this.material_pn = material_pn;
	}

	public String getMaterial_code() {
		return material_code;
	}

	public void setMaterial_code(String material_code) {
		this.material_code = material_code;
	}

	public String getMaterial_qty() {
		return material_qty;
	}

	public void setMaterial_qty(String material_qty) {
		this.material_qty = material_qty;
	}

}
