package com.example.googleplay.manager;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.example.googleplay.bean.AppInfoBean;
import com.example.googleplay.bean.DownloadInfoBean;
import com.example.googleplay.conf.Constants.URLS;
import com.example.googleplay.factory.ThreadPoolFactory;
import com.example.googleplay.utils.apkUtils;
import com.example.googleplay.utils.FileUtils;
import com.example.googleplay.utils.UIUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

/**
 * 下载管理器，记录当前状态，暴露当前状态
 * 
 * @author haopi
 *
 */
public class DownloadManager
{
	public static final int STATE_UNDOWNLOAD = 0;// 未下载
	public static final int STATE_DOWNLOADING = 1;// 下载中
	public static final int STATE_PAUSEDDOWNLOAD = 2;// 暂停下载
	public static final int STATE_WAITINGDOWNLOAD = 3;// 等待下载
	public static final int STATE_DOWNLOADFAILED = 4;// 下载失败
	public static final int STATE_DOWNLOADED = 5;// 下载完成
	public static final int STATE_INSTALLED = 6;// 已安装

	private static DownloadManager instance;

	Map<String, DownloadInfoBean> downloadInfoBeans = new HashMap<String, DownloadInfoBean>();

	private DownloadManager() {

	}

	/** 获取单例 */
	public static DownloadManager getInstance() {
		if (instance == null) {
			synchronized (DownloadManager.class) {
				if (instance == null) {
					instance = new DownloadManager();
				}
			}
		}
		return instance;
	}

	/** 点击下载 */
	public void doDownload(DownloadInfoBean downloadInfoBean) {
		// 保存数据
		downloadInfoBeans.put(downloadInfoBean.packageName, downloadInfoBean);

		/** 下载状态：等待下载 */
		downloadInfoBean.state = STATE_WAITINGDOWNLOAD;
		// 通知观察者
		notifyObservers(downloadInfoBean);

		// 新建任务
		DownloadTask downloadTask = new DownloadTask(downloadInfoBean);
		// 保存任务
		downloadInfoBean.task = downloadTask;

		// 开启线程池下载
		ThreadPoolFactory.getmDownLoadPool().execute(downloadTask);
	}

	class DownloadTask implements Runnable
	{
		public void stop() {
			UIUtils.postTaskRemove(this);
		}

		DownloadInfoBean downloadInfoBean;

		public DownloadTask(DownloadInfoBean downloadInfoBean) {
			super();
			this.downloadInfoBean = downloadInfoBean;
		}

		@Override
		public void run() {
			try {
				// 从绝对路径获取APP文件已经下载的大小
				long range = getRangeFromAbsolutePath(downloadInfoBean);
				downloadInfoBean.curProgress = range;

				/** 下载状态：下载中 */
				downloadInfoBean.state = STATE_DOWNLOADING;
				// 通知观察者
				notifyObservers(downloadInfoBean);

				HttpUtils httpUtils = new HttpUtils();

				String url = URLS.DOWNLOADURL;
				RequestParams params = new RequestParams();
				params.addQueryStringParameter("name", downloadInfoBean.downloadUrl);
				params.addQueryStringParameter("range", range + "");

				ResponseStream responseStream = httpUtils.sendSync(HttpMethod.GET, url, params);

				if (responseStream.getStatusCode() == 200) {
					// 获取输入流
					InputStream is = responseStream.getBaseStream();
					// 追加写入文件
					File saveFile = new File(downloadInfoBean.savePathAbsolute);

					RandomAccessFile accessFile = new RandomAccessFile(saveFile, "rw");

					// FileOutputStream fos = new FileOutputStream(saveFile, true);
					accessFile.seek(downloadInfoBean.curProgress);

					byte[] buffer = new byte[1024];
					int length = 0;
					while (-1 != (length = is.read(buffer))) {
						if (downloadInfoBean.state == STATE_PAUSEDDOWNLOAD) {
							break;
						}

						accessFile.write(buffer, 0, length);
						// 进度条值
						downloadInfoBean.curProgress += length;

						/** 下载状态：下载中 */
						downloadInfoBean.state = STATE_DOWNLOADING;
						// 通知观察者
						notifyObservers(downloadInfoBean);

						if (downloadInfoBean.curProgress == downloadInfoBean.max) {
							/** 下载状态：下载完成 */
							downloadInfoBean.state = STATE_DOWNLOADED;
							// 通知观察者
							notifyObservers(downloadInfoBean);

							break;
						}
					}
					// if (downloadInfoBean.curProgress == downloadInfoBean.max)
					// {
					// /** 下载状态：下载完成 */
					// downloadInfoBean.state = STATE_DOWNLOADED;
					// // 通知观察者
					// notifyObservers(downloadInfoBean);
					// }
					accessFile.close();

				} else {
					/** 下载状态：下载失败 */
					downloadInfoBean.state = STATE_DOWNLOADFAILED;
					// 通知观察者
					notifyObservers(downloadInfoBean);
				}

			} catch (Exception e) {
				e.printStackTrace();

				/** 下载状态：下载失败 */
				downloadInfoBean.state = STATE_DOWNLOADFAILED;
				// 通知观察者
				notifyObservers(downloadInfoBean);
			}
		}

	}

	/** 在绝对路径中获取下载中的apk文件的大小 */
	private long getRangeFromAbsolutePath(DownloadInfoBean downloadInfoBean) {
		File file = new File(downloadInfoBean.savePathAbsolute);
		if (file.exists()) {
			return file.length();
		}
		return 0;
	}

	/** 暴露当前状态，返回downloadInfoBean，其中包含下载的APP的状态 */
	public DownloadInfoBean getDownloadInfo(AppInfoBean data) {
		DownloadInfoBean downloadInfo = getGenerateDownloadInfo(data);
		/*
		 * 未下载 下载中 暂停下载 等待下载 下载失败 下载完成 已安装
		 */
		// 已安装
		if (apkUtils.isInstalled(UIUtils.getContext(), data.packageName)) {
			downloadInfo.state = STATE_INSTALLED;
			return downloadInfo;
		}
		// 下载完成
		// 根据apk下载的路径获取apk下载的文件
		File downloadFile = new File(downloadInfo.savePathAbsolute);
		if (downloadFile.exists() && downloadFile.length() == data.size) {
			downloadInfo.state = STATE_DOWNLOADED;
			return downloadInfo;
		}
		// 下载中 暂停下载 等待下载 下载失败
		DownloadInfoBean downloadInfoBean = downloadInfoBeans.get(data.packageName);
		if (downloadInfoBean != null) {
			return downloadInfoBean;
		}

		// 剩余情况：未下载
		downloadInfo.state = STATE_UNDOWNLOAD;
		return downloadInfo;

	}

	/** 进行一些常规的赋值，把AppInfoBean转换成DownloadInfoBean，除了state */
	public DownloadInfoBean getGenerateDownloadInfo(AppInfoBean data) {
		// apk文件下载的绝对路径
		String dir = FileUtils.getDir("download");
		String name = data.packageName + ".apk";
		File downloadFile = new File(dir, name);
		String downloadFileAbsolutePath = downloadFile.getAbsolutePath();

		DownloadInfoBean downloadInfo = new DownloadInfoBean();

		downloadInfo.savePathAbsolute = downloadFileAbsolutePath;
		downloadInfo.downloadUrl = data.downloadUrl;
		downloadInfo.packageName = data.packageName;
		downloadInfo.max = data.size;
		downloadInfo.curProgress = 0;

		return downloadInfo;
	}

	/** 暂停下载 */
	public void pauseDownload(DownloadInfoBean downloadInfo) {
		downloadInfo.state = STATE_PAUSEDDOWNLOAD;
		notifyObservers(downloadInfo);

		DownloadTask downloadTask = (DownloadTask) downloadInfo.task;
		// 移除线程
		downloadTask.stop();
	}

	/** 取消下载，前提是还未开始下载 */
	public void cancelDownload(DownloadInfoBean downloadInfo) {
		Runnable task = downloadInfo.task;
		ThreadPoolFactory.getmDownLoadPool().remove(task);

		downloadInfo.state = STATE_UNDOWNLOAD;
		notifyObservers(downloadInfo);
	}

	/** 安装应用 */
	public void installApk(DownloadInfoBean downloadInfo) {
		File apkFile = new File(downloadInfo.savePathAbsolute);
		apkUtils.installApp(UIUtils.getContext(), apkFile);
	}

	/** 打开应用 */
	public void openApp(DownloadInfoBean downloadInfo) {
		apkUtils.openApp(UIUtils.getContext(), downloadInfo.packageName);
	}

	/** ================= 自定义设计模式-begin================= */
	/** 观察者接口 */
	public interface DownloadStateObserver
	{
		void onDownloadInfoChange(DownloadInfoBean downloadInfo);
	}

	/** 用于保存观察者 */
	public List<DownloadStateObserver> observers = new LinkedList<DownloadStateObserver>();

	/** 添加观察者 */
	public void addObserver(DownloadStateObserver observer) {
		if (observer == null) {
			throw new NullPointerException("observer == null");
		}
		synchronized (this) {
			if (!observers.contains(observer)) {
				observers.add(observer);
			}
		}
	}

	/** 删除观察者 */
	public synchronized void deleteObserver(DownloadStateObserver observer) {
		observers.remove(observer);
	}

	/** 通知观察者 */
	public void notifyObservers(DownloadInfoBean downloadInfo) {
		for (DownloadStateObserver observer : observers) {
			observer.onDownloadInfoChange(downloadInfo);
		}
	}
	/** ================= 自定义设计模式-end================= */

}
