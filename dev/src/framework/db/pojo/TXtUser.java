package framework.db.pojo;

import java.sql.Timestamp;

public class TXtUser  implements java.io.Serializable{
	private String UId;
	private String UPwd;
	private String UName;
	private String URemark;
	private String yxBj;
	private Timestamp lrSj;
	private Timestamp xgSj;
	private String order;

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getUId() {
		return this.UId;
	}

	public void setUId(String UId) {
		this.UId = UId;
	}

	public String getUPwd() {
		return this.UPwd;
	}

	public void setUPwd(String UPwd) {
		this.UPwd = UPwd;
	}

	public String getUName() {
		return this.UName;
	}

	public void setUName(String UName) {
		this.UName = UName;
	}

	public String getURemark() {
		return this.URemark;
	}

	public void setURemark(String URemark) {
		this.URemark = URemark;
	}

	public String getYxBj() {
		return this.yxBj;
	}

	public void setYxBj(String yxBj) {
		this.yxBj = yxBj;
	}

	public Timestamp getLrSj() {
		return this.lrSj;
	}

	public void setLrSj(Timestamp lrSj) {
		this.lrSj = lrSj;
	}

	public Timestamp getXgSj() {
		return this.xgSj;
	}

	public void setXgSj(Timestamp xgSj) {
		this.xgSj = xgSj;
	}
}