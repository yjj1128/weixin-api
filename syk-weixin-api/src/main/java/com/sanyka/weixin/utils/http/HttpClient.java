/**
 * HttpClient.java - 2012-8-15
 *
 * Licensed Property to kayakwise Co., Ltd.
 * @author OF
 */
package com.sanyka.weixin.utils.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;

/**
 * Description: HTTP通信工具类
 *
 * (C) Copyright of kayakwise Co., Ltd. 2015.
 *
 * @author OF
 * @date 2015年3月8日
 */
public class HttpClient {
	/**
	 * 目标地址
	 */
	private URL		url;

	/**
	 * 通信连接超时时间
	 */
	private int		connectionTimeout;

	/**
	 * 通信读超时时间
	 */
	private int		readTimeOut;

	/**
	 * 通信结果
	 */
	private String	result;
	/**
	 * 发送数据类型（json、kay-val）
	 */
	private String	dataType;

	/**
	 * 构造函数
	 *
	 * @param url
	 *            目标地址
	 * @throws MalformedURLException
	 */
	public HttpClient(String url) throws MalformedURLException {
		this(url, 60 * 1000, 60 * 1000);
	}

	/**
	 * 构造函数
	 *
	 * @param url
	 *            目标地址
	 * @param connectionTimeout
	 *            HTTP连接超时时间
	 * @param readTimeOut
	 *            HTTP读写超时时间
	 * @throws MalformedURLException
	 */
	public HttpClient(String url, int connectionTimeout, int readTimeOut) throws MalformedURLException {
		this(url, connectionTimeout, readTimeOut, "kay-val");
	}

	/**
	 * 构造函数
	 *
	 * @param url
	 *            目标地址
	 * @param connectionTimeout
	 *            HTTP连接超时时间
	 * @param readTimeOut
	 *            HTTP读写超时时间
	 * @param dataType
	 *            需要发送的数据类型
	 * @throws MalformedURLException
	 */
	public HttpClient(String url, int connectionTimeout, int readTimeOut, String dataType)
			throws MalformedURLException {
		this.url = new URL(url);
		this.connectionTimeout = connectionTimeout;
		this.readTimeOut = readTimeOut;
		this.dataType = dataType;
	}

	/**
	 * 发送信息到服务端
	 *
	 * @param data
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public int send(Map<String, String> data, String encoding) throws Exception {
		try {
			HttpURLConnection httpURLConnection = createConnection(encoding);
			if (null == httpURLConnection) {
				throw new Exception("创建联接失败");
			}
			this.requestServer(httpURLConnection, this.getRequestParamString(data, encoding), encoding);
			this.result = this.response(httpURLConnection, encoding);
			return httpURLConnection.getResponseCode();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 发送信息到服务端
	 *
	 * @param data
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public int sendPost(String data, String encoding) throws Exception {
		try {
			HttpURLConnection httpURLConnection = createConnection(encoding);
			if (null == httpURLConnection) {
				throw new Exception("创建联接失败");
			}
			this.requestServer(httpURLConnection, data, encoding);
			this.result = this.response(httpURLConnection, encoding);
			return httpURLConnection.getResponseCode();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 发送信息到服务端
	 *
	 * @param data
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public int sendJson(Map<String, Object> data, String encoding) throws Exception {
		try {
			HttpURLConnection httpURLConnection = createConnection(encoding);
			if (null == httpURLConnection) {
				throw new Exception("创建联接失败");
			}
			this.requestServer(httpURLConnection, com.sanyka.weixin.utils.JsonUtil.objectToJson(data), encoding);
			this.result = this.response(httpURLConnection, encoding);
			return httpURLConnection.getResponseCode();
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * HTTP Post发送消息
	 *
	 * @param connection
	 * @param message
	 * @throws IOException
	 */
	private void requestServer(final URLConnection connection, String message, String encoder) throws Exception {
		PrintStream out = null;
		try {
			connection.connect();
			out = new PrintStream(connection.getOutputStream(), false, encoder);
			out.print(message);
			out.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != out) {
				out.close();
			}
		}
	}

	/**
	 * 显示Response消息
	 *
	 * @param connection
	 * @param CharsetName
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private String response(final HttpURLConnection connection, String encoding)
			throws URISyntaxException, IOException, Exception {
		InputStream in = null;
		StringBuilder sb = new StringBuilder(1024);
		BufferedReader br = null;
		String temp = null;
		try {
			if (200 == connection.getResponseCode()) {
				in = connection.getInputStream();
				br = new BufferedReader(new InputStreamReader(in, encoding));
				while (null != (temp = br.readLine())) {
					sb.append(temp);
				}
			} else {
				in = connection.getErrorStream();
				br = new BufferedReader(new InputStreamReader(in, encoding));
				while (null != (temp = br.readLine())) {
					sb.append(temp);
				}
			}
			return sb.toString();
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != br) {
				br.close();
			}
			if (null != in) {
				in.close();
			}
			if (null != connection) {
				connection.disconnect();
			}
		}
	}

	/**
	 * 创建连接
	 *
	 * @return
	 * @throws ProtocolException
	 */
	private HttpURLConnection createConnection(String encoding) throws ProtocolException {
		HttpURLConnection httpURLConnection = null;
		try {
			httpURLConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		httpURLConnection.setConnectTimeout(this.connectionTimeout);// 连接超时时间
		httpURLConnection.setReadTimeout(this.readTimeOut);// 读取结果超时时间
		httpURLConnection.setDoInput(true); // 可读
		httpURLConnection.setDoOutput(true); // 可写
		httpURLConnection.setUseCaches(false);// 取消缓存
		// 根据不同数据类型配置获取不同的配置
		httpURLConnection.setRequestProperty("Content-type", getRequestProperty(encoding));
		httpURLConnection.setRequestMethod("POST");
		if ("https".equalsIgnoreCase(url.getProtocol())) {
			HttpsURLConnection husn = (HttpsURLConnection) httpURLConnection;
			husn.setSSLSocketFactory(new BaseHttpSSLSocketFactory());
			husn.setHostnameVerifier(
					/**
					 * 解决由于服务器证书问题导致HTTPS无法访问的情况 <br>
					 */
					new HostnameVerifier() {
						public boolean verify(String hostname, SSLSession session) {
							// 直接返回true
							return true;
						}
					});// 解决由于服务器证书问题导致HTTPS无法访问的情况
			return husn;
		}
		return httpURLConnection;
	}

	/**
	 * 获取请求数据类型配置
	 *
	 * @param encoding
	 *            字符编码集
	 * @return
	 */
	public String getRequestProperty(String encoding) {
		if (StringUtils.isNoneBlank(dataType) && "json".toUpperCase().equals(dataType.toUpperCase())) {
			return "application/json;charset=" + encoding;
		} else {
			return "application/x-www-form-urlencoded;charset=" + encoding;
		}
	}

	/**
	 * 将Map存储的对象，转换为key=value&key=value的字符
	 *
	 * @param requestParam
	 * @param coder
	 * @return
	 */
	private String getRequestParamString(Map<String, String> requestParam, String coder) {
		if (null == coder || "".equals(coder)) {
			coder = "UTF-8";
		}
		StringBuffer sf = new StringBuffer("");
		String reqstr = "";
		if (null != requestParam && 0 != requestParam.size()) {
			for (Entry<String, String> en : requestParam.entrySet()) {
				try {
					sf.append(en.getKey() + "=" + (null == en.getValue() || "".equals(en.getValue()) ? ""
							: URLEncoder.encode(en.getValue(), coder)) + "&");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return "";
				}
			}
			reqstr = sf.substring(0, sf.length() - 1);
		}
		return reqstr;
	}

	/**
	 * 获取通信结果
	 *
	 * @return
	 */
	public String getResult() {
		return result;
	}

	/**
	 * 设置通信结果
	 *
	 * @param result
	 */
	public void setResult(String result) {
		this.result = result;
	}
}
