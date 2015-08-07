package com.lxz.exam.simpleone.activemq;

public class Mail {
	private String mailID;
	private String country;
	private String weight;

	public Mail(){
		
	}
	
	public Mail(String mailID,String country, String weight){
		this.mailID = mailID;
		this.country = country;
		this.weight = weight;
	}
	
	public String getMailID() {
		return mailID;
	}

	public void setMailID(String mailID) {
		this.mailID = mailID;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}
	
}
