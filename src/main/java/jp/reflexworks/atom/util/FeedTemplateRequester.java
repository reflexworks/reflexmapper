package jp.reflexworks.atom.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import org.json.JSONException;

import jp.sourceforge.reflex.util.DeflateUtil;
import jp.sourceforge.reflex.util.Requester;
import jp.reflexworks.servlet.ReflexServletConst;
import jp.reflexworks.atom.feed.FeedBase;
import jp.reflexworks.atom.mapper.FeedTemplateMapper;

public class FeedTemplateRequester extends Requester implements ReflexServletConst {
	
	private static int FORMAT_DEFAULT = FORMAT_JSON;
	
	public FeedTemplateRequester() {}
	public FeedTemplateRequester(int format) {
		FORMAT_DEFAULT = format;
	}
	
	public FeedTemplateURLConnection requestFeed(String urlStr, String method, 
			FeedBase feed, Map<String, String> property,
			FeedTemplateMapper mapper, DeflateUtil deflateUtil) 
	throws IOException, JSONException, ClassNotFoundException, DataFormatException {
		return requestFeed(urlStr, method, feed, property, -1, mapper, deflateUtil);
	}

	public FeedTemplateURLConnection requestFeed(String urlStr, String method, 
			FeedBase feed, Map<String, String> property, int timeoutMillis,
			FeedTemplateMapper mapper, DeflateUtil deflateUtil) 
	throws IOException, JSONException, ClassNotFoundException, DataFormatException {
		return requestFeed(urlStr, method, feed, property, timeoutMillis, mapper,
				deflateUtil, -1);
	}

	public FeedTemplateURLConnection requestFeed(String urlStr, String method, 
			FeedBase feed, Map<String, String> property, int timeoutMillis,
			FeedTemplateMapper mapper, DeflateUtil deflateUtil,
			int format) 
	throws IOException, JSONException, ClassNotFoundException, DataFormatException {
		HttpURLConnection http = prepare(urlStr, method, feed, property, 
				timeoutMillis, mapper, deflateUtil, format);
		http.getResponseCode();	// ここでサーバに接続
		
		// 戻り値をFeedBaseに変換
		FeedBase retFeed = convertFeed(http, mapper, deflateUtil);
		
		return new FeedTemplateURLConnection(http, retFeed);
	}

	/**
	 * HTTPリクエスト送信準備
	 * @param urlStr URL
	 * @param method method
	 * @param feed リクエストデータ
	 * @param property リクエストヘッダ
	 * @param timeoutMillis タイムアウト時間(ミリ秒)。0(無制限)は無効とし、デフォルト設定になります。
	 * @return HttpURLConnection
	 */
	public HttpURLConnection prepare(String urlStr, String method, 
			FeedBase feed, Map<String, String> property, int timeoutMillis,
			FeedTemplateMapper mapper, DeflateUtil deflateUtil,
			int format) 
	throws IOException {
		// feedをinputstreamに設定
		byte[] inputData = null;
		if (feed != null && 
				(POST.equalsIgnoreCase(method) || PUT.equalsIgnoreCase(method))) {
			if (property == null) {
				property = new HashMap<String, String>();
			}
			inputData = convertSendingData(feed, property, mapper, deflateUtil, format);
		}

		return super.prepare(urlStr, method, inputData, property, 
				timeoutMillis);
	}
	
	public FeedBase convertFeed(HttpURLConnection http, FeedTemplateMapper mapper, 
			DeflateUtil deflateUtil) 
	throws IOException, DataFormatException, ClassNotFoundException,
			JSONException {
		int format = FORMAT_DEFAULT;
		String contentType = http.getHeaderField(HEADER_CONTENT_TYPE);
		if (contentType == null) {
			contentType = http.getHeaderField(HEADER_CONTENT_TYPE.toLowerCase());
		}
		if (contentType != null) {
			if (contentType.indexOf(CONTENT_TYPE_MESSAGEPACK) > -1) {
				format = FORMAT_MESSAGEPACK;
			} else if (contentType.indexOf(CONTENT_TYPE_JSON) > -1) {
				format = FORMAT_JSON;
			} else if (contentType.indexOf(CONTENT_TYPE_XML) > -1) {
				format = FORMAT_XML;
			//} else if (contentType.indexOf(CONTENT_TYPE_PLAIN) > -1) {
			//	format = FORMAT_TEXT;
			}
		}
		byte[] respData = null;
		InputStream in = null;
		if (http.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
			in = http.getInputStream();
		} else {
			in = http.getErrorStream();
		}
		if (in != null) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			write(in, bout);
			respData = bout.toByteArray();
		}
		FeedBase respFeed = null;
		if (respData != null && respData.length > 0) {
			if (format == FORMAT_MESSAGEPACK) {
				byte[] msgData = null;
				if (deflateUtil != null) {
					msgData = deflateUtil.inflate(respData);
				} else {
					msgData = respData;
				}
				respFeed = (FeedBase)mapper.fromMessagePack(msgData);
			} else if (format == FORMAT_JSON) {
				String json = new String(respData, ENCODING);
				respFeed = (FeedBase)mapper.fromJSON(json);
			} else {	// XML
				String xml = new String(respData, ENCODING);
				respFeed = (FeedBase)mapper.fromXML(xml);
			}
		}
		return respFeed;
	}
	
	public String convertText(HttpURLConnection http) 
	throws IOException {
		byte[] respData = null;
		if (http.getInputStream() != null) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			write(http.getInputStream(), bout);
			respData = bout.toByteArray();
			if (respData != null && respData.length > 0) {
				return new String(respData, ENCODING);
			}
		}
		return null;
	}

	/*
	 * リクエストデータのMessagePack変換・圧縮
	 */
	private byte[] convertSendingData(FeedBase feed, Map<String, String> property,
			FeedTemplateMapper mapper, DeflateUtil deflateUtil, int format) 
	throws IOException {
		if (feed == null || mapper == null) {
			return null;
		}

		byte[] data = null;
		String contentType = null;
		String contentEncoding = null;
		if (format != FORMAT_XML && format != FORMAT_JSON && 
				format != FORMAT_MESSAGEPACK) {
			format = FORMAT_DEFAULT;
		}
		if (format == FORMAT_MESSAGEPACK) {
			// deflateUtilがnullでない場合圧縮する。
			byte[] tmpData = mapper.toMessagePack(feed);
			if (deflateUtil != null) {
				data = deflateUtil.deflate(tmpData);
				contentEncoding = HEADER_CONTENT_ENCODING_DEFLATE;
			} else {
				data = tmpData;
			}
			contentType = CONTENT_TYPE_MESSAGEPACK;

		} else if (format == FORMAT_JSON) {
			String json = mapper.toJSON(feed);
			data = json.getBytes(ENCODING);
			contentType = CONTENT_TYPE_REFLEX_JSON;
			
		} else {	// XML
			String xml = mapper.toXML(feed);
			data = xml.getBytes(ENCODING);
			contentType = CONTENT_TYPE_REFLEX_XML;
		}

		if (property != null) {
			property.put(HEADER_CONTENT_TYPE, contentType);
			if (contentEncoding != null) {
				property.put(HEADER_CONTENT_ENCODING, contentEncoding);
			}
		}
		return data;
	}

}
