package edu.thesis.web.service.rest;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
public class ImageEntity {
	
	@Id @GeneratedValue 
	private int id;
	@Basic private String location;
	@Basic private String time;
	@Basic private String path;
	@ManyToMany(targetEntity=User.class)
	private Set<User> users;
	
	@OneToMany(targetEntity=Face.class)
	private Set<Face> faces;
	
	public ImageEntity(String location, String time, String path){
		this.location = location;
		this.time = time;
		this.path = path;
		this.faces = new HashSet<Face>();
		users = new HashSet<User>();
	}
	
	public Set<Face> getFaces() {
		return faces;
	}

	public void setFaces(Set<Face> faces) {
		this.faces = faces;
	}

	public ImageEntity(){
		
	}
	
	public void addUser(User user){
		users.add(user);
	}
	
	public int getId(){
		return this.id;
	}
	
	public String getLocation(){
		return this.location;
	}
	
	public String getTime(){
		return this.time;
	}
	
	
	public void setLocation(String location){
		this.location = location;
	}
	
	public void setTime(String time){
		this.time = time;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String setPath(String path){
		return this.path = path;
	}
	
	
}
