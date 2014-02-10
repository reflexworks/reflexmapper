package jp.reflexworks.atom.feed;

import java.io.Serializable;

public class Generator implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public String _$xml$lang;
	public String _$xml$base;
	public String _$uri;
	public String _$version;
	public String _$$text;

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

	public String get$uri() {
		return _$uri;
	}

	public void set$uri(String _$uri) {
		this._$uri = _$uri;
	}

	public String get$version() {
		return _$version;
	}

	public void set$version(String _$version) {
		this._$version = _$version;
	}

	public String get$$text() {
		return _$$text;
	}

	public void set$$text(String _$$text) {
		this._$$text = _$$text;
	}

	@Override
	public String toString() {
		return "Generator [" + _$$text + "]";
	}

}
