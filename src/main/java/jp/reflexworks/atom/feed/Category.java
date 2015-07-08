package jp.reflexworks.atom.feed;

import java.io.Serializable;

import org.msgpack.annotation.Index;

public class Category implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	@Index(0)
	public String _$term;
	@Index(1)
	public String _$scheme;
	@Index(2)
	public String _$label;

	public String get_$term() {
		return _$term;
	}

	public void set_$term(String term) {
		this._$term = term;
	}

	public String get_$scheme() {
		return _$scheme;
	}

	public void set_$scheme(String scheme) {
		this._$scheme = scheme;
	}

	public String get_$label() {
		return _$label;
	}

	public void set_$label(String label) {
		this._$label = label;
	}

	@Override
	public String toString() {
		return "Category [_$term=" + _$term + ", _$scheme=" + _$scheme
				+ ", _$label=" + _$label + "]";
	}

}
