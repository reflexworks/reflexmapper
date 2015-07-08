package jp.reflexworks.atom.feed;

import java.io.Serializable;

import org.msgpack.annotation.Index;

public class Link implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public static final String REL_NEXT = "next";

	@Index(0)
	public String _$href;
	@Index(1)
	public String _$rel;
	@Index(2)
	public String _$type;
	@Index(3)
	public String _$title;
	@Index(4)
	public String _$length;


	public String get$href() {
		return _$href;
	}

	public void set$href(String _$href) {
		this._$href = _$href;
	}

	public String get$rel() {
		return _$rel;
	}

	public void set$rel(String _$rel) {
		this._$rel = _$rel;
	}

	public String get$type() {
		return _$type;
	}

	public void set$type(String _$type) {
		this._$type = _$type;
	}

	public String get$title() {
		return _$title;
	}

	public void set$title(String _$title) {
		this._$title = _$title;
	}

	public String get$length() {
		return _$length;
	}

	public void set$length(String _$length) {
		this._$length = _$length;
	}

	@Override
	public String toString() {
		return "Link [_$href=" + _$href 
				+ ", _$length=" + _$length + ", _$rel=" + _$rel + ", _$title="
				+ _$title + ", _$type=" + _$type + "]";
	}

	
}
