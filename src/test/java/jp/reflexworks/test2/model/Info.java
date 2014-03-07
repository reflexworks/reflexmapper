package jp.reflexworks.test2.model;

public class Info {
	
	public String _name;
	public String _category;
	public String _color;
	public String _size;

	public String getName() {
		return _name;
	}
	public void setName(String name) {
		this._name = name;
	}
	public String getCategory() {
		return _category;
	}
	public void setCategory(String category) {
		this._category = category;
	}
	public String getColor() {
		return _color;
	}
	public void setColor(String color) {
		this._color = color;
	}
	public String getSize() {
		return _size;
	}
	public void setSize(String size) {
		this._size = size;
	}

	@Override
	public String toString() {
		return "Info [name=" + _name + ", category=" + _category + ", color="
				+ _color + ", size=" + _size + "]";
	}

}
