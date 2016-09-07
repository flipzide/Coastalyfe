package com.ondemandbay.taxianytime.event;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Event implements Serializable {
	int eventId, ownerId, eventMember;
	String eventName, eventAddrs, eventDateTime, eventPromo;
	double eventPayPerPerson, eventLat, eventLong;

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public int getEventMember() {
		return eventMember;
	}

	public void setEventMember(int eventMember) {
		this.eventMember = eventMember;
	}

	public String getEventAddrs() {
		return eventAddrs;
	}

	public void setEventAddrs(String eventAddrs) {
		this.eventAddrs = eventAddrs;
	}

	public String getEventDateTime() {
		return eventDateTime;
	}

	public void setEventDateTime(String eventDateTime) {
		this.eventDateTime = eventDateTime;
	}

	public String getEventPromo() {
		return eventPromo;
	}

	public void setEventPromo(String eventPromo) {
		this.eventPromo = eventPromo;
	}

	public double getEventLat() {
		return eventLat;
	}

	public void setEventLat(double eventLat) {
		this.eventLat = eventLat;
	}

	public double getEventLong() {
		return eventLong;
	}

	public void setEventLong(double eventLong) {
		this.eventLong = eventLong;
	}

	public double getEventPayPerPerson() {
		return eventPayPerPerson;
	}

	public void setEventPayPerPerson(double eventPayPerPerson) {
		this.eventPayPerPerson = eventPayPerPerson;
	}
}
