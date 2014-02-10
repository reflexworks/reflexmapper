package jp.reflexworks.atom.feed;

import java.io.Serializable;

public class Category implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public String _$xml$lang;
	public String _$xml$base;
	public String _$term;
	public String _$scheme;
	public String _$label;

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
