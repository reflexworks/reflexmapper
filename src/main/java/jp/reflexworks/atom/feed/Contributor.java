package jp.reflexworks.atom.feed;

import java.io.Serializable;

import org.msgpack.annotation.Index;

public class Contributor implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	@Index(0)
	public String _$xml$lang;
	@Index(1)
	public String _$xml$base;
	@Index(2)
	public String name;
	@Index(3)
	public String uri;
	@Index(4)
	public String email;

	public String get$xml$lang() {
		return _$xml$lang;
	}

	public void set$xml$lang(String _$xml$lang) {
		this._$xml$lang = _$xml$lang;
	}

	public String get$xml$base() {
		return _$xml$base;
	}

	public void set$xml$base(String _$xml$base) {
		this._$xml$base = _$xml$base;
	}

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
		return "Contributor [uri=" + uri + ", email=" + email + ", name=" + name + "]";
	}

}
