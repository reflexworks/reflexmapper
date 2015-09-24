package jp.reflexworks.atom.feed;

import java.io.Serializable;

import org.msgpack.annotation.Index;

public class Author implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	@Index(0)
	public String name;
	@Index(1)
	public String uri;
	@Index(2)
	public String email;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "Author [uri=" + uri + ", email=" + email + ", name=" + name + "]";
	}

}
