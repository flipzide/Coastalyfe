package com.ondemandbay.taxianytime.models;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Booking implements Serializable{

	int id, requestId;
	String startTime, source_add, tag, dest_add, mapImage, vehicleType, driverStatus, ongoingTripTime;
	//public static boolean isCurrentRequest = false;
	//public static boolean isFutureRequest = false;
	
	public void setStartTime(String startTime){
		this.startTime = startTime;
	}
	
	public String getStartTime(){
		return startTime;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void setSource(String source_add){
		this.source_add = source_add;
	}
	
	public String getSource(){
		return source_add;
	}
		
	public void setTag(String tag){
		this.tag = tag;
	}
	
	public String getTag(){
		return tag;
	}
	
	public void setRequestId(int requestId){
		this.requestId = requestId;
	}
	
	public int getRequestId(){
		return requestId;
	}
	
	public void	setDest(String dest_add){
		this.dest_add = dest_add;
	}
	
	public String getDest(){
		return dest_add;
	}
	
	public void setMapImage(String mapImage){
		this.mapImage = mapImage;
	}
	
	public String getMapImage(){
		return mapImage;
	}
	
	public void setVehicleType(String vehicleType){
		this.vehicleType = vehicleType;
	}
	
	public String getVehicleType(){
		return vehicleType;
	}
	
	public void setDriverStatus(String driverStatus){
		this.driverStatus = driverStatus;
	}
	
	public String getDriverStatus(){
		return driverStatus;
	}
	
	public void setRequestCreatedTime(String ongoingTripTime){
		this.ongoingTripTime=ongoingTripTime;
	}
	
	public String getRequestCreatedTime(){ 
		return ongoingTripTime;
	}
}
