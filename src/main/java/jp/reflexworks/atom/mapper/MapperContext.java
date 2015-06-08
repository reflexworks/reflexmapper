package jp.reflexworks.atom.mapper;

import java.util.List;

public class MapperContext {

	
	public String parent;
	public String uid;
	public List groups;
	public String myself;
	public String id;
	public Object cipher;
	public String secretkey;
	
	public MapperContext(Object cipher,String id) {
		this.cipher = cipher;
		this.id = id;
	}
	
}
