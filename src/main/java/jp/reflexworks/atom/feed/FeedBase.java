package jp.reflexworks.atom.feed;

import java.io.Serializable;
import java.util.List;

import jp.reflexworks.atom.entry.EntryBase;

/**
 * Feedの親クラス.
 * <p>
 * ATOM形式のFeedです。<br>
 * このクラスのentry項目に、データであるエントリーが複数格納されます。<br>
 * 各プロジェクトでこのクラスを継承し、カスタマイズしたFeedクラスを生成してください。<br>
 * </p>
 */
public abstract class FeedBase implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	/**
	 * デフォルトの名前空間
	 */
	public String _$xmlns;
	public String _$xmlns$rx;
	public List<Author> _author;
	public List<Category> _category;
	public List<Contributor> _contributor;
	public Generator _generator;
	public String _icon;
	public String _id;
	/**
	 * 次ページカーソル.
	 * <p>
	 * 属性rel="next"の、href属性に設定された値が次ページ検索のためのカーソルです。
	 * </p>
	 */
	public List<Link> _link;
	public String _logo;
	public String _rights;
	public String _title;
	public String _title_$type;
	public String _subtitle;
	public String _subtitle_$type;
	public String _updated;
	/** エントリーリスト */
	public List<EntryBase> _entry;

	public String get$xmlns() {
		return _$xmlns;
	}

	public void set$xmlns(String _$xmlns) {
		this._$xmlns = _$xmlns;
	}

	public String get$xmlns$rx() {
		return _$xmlns$rx;
	}

	public void set$xmlns$rx(String _$xmlns$rx) {
		this._$xmlns$rx = _$xmlns$rx;
	}

	public List<Author> getAuthor() {
		return _author;
	}

	public void setAuthor(List<Author> author) {
		this._author = author;
	}

	public List<Category> getCategory() {
		return _category;
	}

	public void setCategory(List<Category> category) {
		this._category = category;
	}

	public List<Contributor> getContributor() {
		return _contributor;
	}

	public void setContributor(List<Contributor> contributor) {
		this._contributor = contributor;
	}

	public Generator getGenerator() {
		return _generator;
	}

	public void setGenerator(Generator generator) {
		this._generator = generator;
	}

	public String getIcon() {
		return _icon;
	}

	public void setIcon(String icon) {
		this._icon = icon;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public List<Link> getLink() {
		return _link;
	}

	public void setLink(List<Link> link) {
		this._link = link;
	}

	public String getLogo() {
		return _logo;
	}

	public void setLogo(String logo) {
		this._logo = logo;
	}

	public String getRights() {
		return _rights;
	}

	public void setRights(String rights) {
		this._rights = rights;
	}

	public String getTitle() {
		return _title;
	}

	public void setTitle(String title) {
		this._title = title;
	}

	public String getTitle_$type() {
		return _title_$type;
	}

	public void setTitle_$type(String title_$type) {
		this._title_$type = title_$type;
	}

	public String getSubtitle() {
		return _subtitle;
	}

	public void setSubtitle(String subtitle) {
		this._subtitle = subtitle;
	}

	public String getSubtitle_$type() {
		return _subtitle_$type;
	}

	public void setSubtitle_$type(String subtitle_$type) {
		this._subtitle_$type = subtitle_$type;
	}

	public String getUpdated() {
		return _updated;
	}

	public void setUpdated(String updated) {
		this._updated = updated;
	}

	public List<EntryBase> getEntry() {
		return _entry;
	}

	public void setEntry(List<EntryBase> entry) {
		this._entry = entry;
	}

	@Override
	public String toString() {
		return "Feed [entry=" + _entry + "]";
	}

	/**
	 * 項目チェック
	 */
	public abstract boolean validate(String ucode, List<String> groups)
			throws java.text.ParseException;

	public abstract void maskprop(String ucode, List<String> groups);

	/**
	 * キーにサービス名を付加します.
	 * @param svcname サービス名
	 */
	public void addSvcname(String svcname) {
		if (svcname == null) {
			return;
		}
		if (_entry != null) {
			for (EntryBase _e : _entry) {
				_e.addSvcname(svcname);
			}
		}
	}

	/**
	 * キーからサービス名を除去します.
	 * @param svcname サービス名
	 */
	public void cutSvcname(String svcname) {
		if (svcname == null) {
			return;
		}
		if (_entry != null) {
			for (EntryBase _e : _entry) {
				_e.cutSvcname(svcname);
			}
		}
	}

}
