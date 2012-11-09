package edu.isi.karma.er.helper.entity;

import com.hp.hpl.jena.rdf.model.Resource;

public class InputStruct extends Ontology {

	private Resource resource;
	private String nameforBuilding;
	private double y;
	private double x;
	private String polygon;

	public InputStruct() {
	}

	public Resource getResource() {
		return resource;
	}

	public String getNameforBuilding() {
		return nameforBuilding;
	}

	public double getY() {
		return y;
	}

	public double getX() {
		return x;
	}

	public String getPolygon() {
		return polygon;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setNameforBuilding(String nameforBuilding) {
		this.nameforBuilding = nameforBuilding;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setPolygon(String polygon) {
		this.polygon = polygon;
	}
}
