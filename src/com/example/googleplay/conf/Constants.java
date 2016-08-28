package com.example.googleplay.conf;

import com.example.googleplay.utils.LogUtils;

public class Constants
{
	// ALL显示所有的日志，OFF关闭日志的显示
	public static final int DEBUGLEVEL = LogUtils.LEVEL_ALL;
	public static final int PAGERSIZE = 20;
	public static final long PROTOCOLOUTTIME = 5 * 60 * 1000 * 1000000000;
	
	/**
	 * 请求地址：url
	 * 请求方式：get/post/put/delete
	 * 请求参数：json/key-value
	 * 返回参数
	 * 
	 * 
	 * 服务器地址——BASEURL
	 * http://127.0.0.1:8090/ 服务器在手机上
	 * http://192.168.1.3.8080/GooglePlayServer/ 服务器在电脑上，直接ip访问
	 * http://10.0.2.2:8080/GooglePlayServer/ 服务器在电脑上，android模拟器访问
	 * http://10.0.3.2:8080/GooglePlayServer/ 服务器在电脑上-，genymotion模拟器访问
	 * http://192.168.31.1:8080/GooglePlayServer/服务器在电脑上-，真机访问
	 * 
	 * 主页接口
	 * 1.请求方式：GET
	 * 2.URL:服务器地址 + home
	 * 3.请求参数：index（分页显示中的第几条，默认从0开始）
	 * 例子：http://localhost:8080/GooglePlayServer/home?index=0
	 * 
	 * 应用页面接口
	 * 1.请求方式：GET
	 * 2.URL：服务器地址 + app
	 * 3.请求参数：index（分页显示中第几条，默认从0开始）
	 * 例子：http://localhost:8080/GooglePlayServer/app?index=0
	 */
	public static final class URLS
	{
		// public static final String BASEURL = "http://10.0.2.2:8080/GooglePlayServer/";
		public static final String BASEURL = "http://192.168.31.92:8080/GooglePlayServer/";
		// http://localhost:8080/GooglePlayServer/image?name=
		public static final String IMAGEBASEURL = BASEURL + "image?name=";
		// http://localhost:8080/GooglePlayServer/download?name=app/com.itheima.www/com.itheima.www.apk&range=0
		public static final String DOWNLOADURL = BASEURL + "download";
	}

	public static final class PAY
	{
		
	}
	
	public static final class REQ
	{
		
	}
	
	public static final class RES
	{
		
	}
}

