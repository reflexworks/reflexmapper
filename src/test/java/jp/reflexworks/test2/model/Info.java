package jp.reflexworks.test2.model;

public class Info {
	
	public String name;
	public String category;
	public String color;
	public String size;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "Info [name=" + name + ", category=" + category + ", color="
				+ color + ", size=" + size + "]";
	}

}
