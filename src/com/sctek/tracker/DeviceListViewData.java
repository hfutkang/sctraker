package com.sctek.tracker;

public class DeviceListViewData {
	
	public String name;
	public String deviceNum;
	public String masterNum;
	public String imagePath;
	public int power;
	public int id;
	public String last_position;
	public String pw;
	public String isMaster;
	public String deviceId;
	
	public void setName(String n) {
		name = n;
	}
	
	public void setDeviceNum(String d) {
		deviceNum = d;
	}
	
	public void setMasterNum(String m) {
		masterNum = m;
	}
	
	public void setPower(int p) {
		power = p;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDeviceNum() {
		return deviceNum;
	}
	
	public String getMasterNum() {
		return masterNum;
	}
	
	public int getPower() {
		return power;
	}
	
	public void clean() {
		name = "";
		deviceNum = "";
		masterNum = "";
		imagePath = "";
		power = -1;
		id = -1;
		last_position = "";
		pw = "";
		isMaster = "";
		deviceId = "";
	}

}
