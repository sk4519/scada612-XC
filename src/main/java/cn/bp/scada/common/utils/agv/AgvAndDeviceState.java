package cn.bp.scada.common.utils.agv;

import org.springframework.stereotype.Component;

@Component
public class AgvAndDeviceState {
	private static final AgvAndDeviceState INSTANCE = new AgvAndDeviceState();
	public int tp;
	public int ku;
	public int fk=0;
	private AgvAndDeviceState() {

	}

	public int getKu() {
		return ku;
	}

	public void setKu(int ku) {
		this.ku = ku;
	}

	public static AgvAndDeviceState getInstance() {
		return INSTANCE;
	}

	public boolean isPilerUse = false;

	public boolean isAgvUse = false;

	public  int putNewProduct = 0;

	public int count = 0;

	public int productCount = 0; //记录成品机是否有成品

	public int getProductCount() {
		return productCount;
	}

	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPutNewProduct() {
		return putNewProduct;
	}

	public void setPutNewProduct(int putNewProduct) {
		this.putNewProduct = putNewProduct;
	}

	public synchronized boolean isAgvUse() {
		return isAgvUse;
	}

	public void setAgvUse(boolean isAgvUse) {
		this.isAgvUse = isAgvUse;
	}

	public boolean isPilerUse() {
		return isPilerUse;
	}

	public void setPilerUse(boolean isPilerUse) {
		this.isPilerUse = isPilerUse;
	}

}
