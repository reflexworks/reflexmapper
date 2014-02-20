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
	public List<Author> author;
	public List<Category> category;
	public List<Contributor> contributor;
	public Generator generator;
	public String icon;
	public String id;
	/**
	 * 次ページカーソル.
	 * <p>
	 * 属性rel="next"の、href属性に設定された値が次ページ検索のためのカーソルです。
	 * </p>
	 */
	public List<Link> link;
	public String logo;
	public String rights;
	public String title;
	public String title_$type;
	public String subtitle;
	public String subtitle_$type;
	public String updated;
	/** エントリーリスト */
	public List<EntryBase> entry;

	public String get_$xmlns() {
		return _$xmlns;
	}

	public void set_$xmlns(String _$xmlns) {
		this._$xmlns = _$xmlns;
	}

	public String get_$xmlns$rx() {
		return _$xmlns$rx;
	}

	public void set_$xmlns$rx(String _$xmlns$rx) {
		this._$xmlns$rx = _$xmlns$rx;
	}

	public List<Author> getAuthor() {
		return author;
	}

	public void setAuthor(List<Author> author) {
		this.author = author;
	}

	public List<Category> getCategory() {
		return category;
	}

	public void setCategory(List<Category> category) {
		this.category = category;
	}

	public List<Contributor> getContributor() {
		return contributor;
	}

	public void setContributor(List<Contributor> contributor) {
		this.contributor = contributor;
	}

	public Generator getGenerator() {
		return generator;
	}

	public void setGenerator(Generator generator) {
		this.generator = generator;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Link> getLink() {
		return link;
	}

	public void setLink(List<Link> link) {
		this.link = link;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle_$type() {
		return title_$type;
	}

	public void setTitle_$type(String title_$type) {
		this.title_$type = title_$type;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getSubtitle_$type() {
		return subtitle_$type;
	}

	public void setSubtitle_$type(String subtitle_$type) {
		this.subtitle_$type = subtitle_$type;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public List<EntryBase> getEntry() {
		return entry;
	}

	public void setEntry(List<EntryBase> entry) {
		this.entry = entry;
	}

	@Override
	public String toString() {
		return "Feed [entry=" + entry + "]";
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
		if (entry != null) {
			for (EntryBase _e : entry) {
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
		if (entry != null) {
			for (EntryBase _e : entry) {
				_e.cutSvcname(svcname);
			}
		}
	}

}
