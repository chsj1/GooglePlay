package com.example.googleplay.activity;

import com.example.googleplay.R;
import com.example.googleplay.base.BaseActivity;
import com.example.googleplay.base.LoadingPager;
import com.example.googleplay.base.LoadingPager.LoadedResult;
import com.example.googleplay.bean.AppInfoBean;
import com.example.googleplay.holder.AppDetailBottomHolder;
import com.example.googleplay.holder.AppDetailDesHolder;
import com.example.googleplay.holder.AppDetailInfoHolder;
import com.example.googleplay.holder.AppDetailPicHolder;
import com.example.googleplay.holder.AppDetailSafeHolder;
import com.example.googleplay.manager.DownloadManager;
import com.example.googleplay.protocol.DetailProtocol;
import com.example.googleplay.utils.UIUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 
 * @author haopi
 * @创建时间 2016年8月26日 下午6:42:55
 * @描述 APP详情页面
 *　
 * @修改提交者 $Author$
 * @提交时间 $Date$
 * @当前版本 $Rev$
 *
 */
public class DetailActivity extends BaseActivity
{
	private String mPackageName;
	private DetailProtocol mDetailProtocol;
	private AppInfoBean mLoadData;
	private LoadingPager mLoadingPager;
	private AppDetailBottomHolder mAppDetailBottomHolder;

	@Override
	public void init() {
		// 获取传过来的包名
		Intent intent = getIntent();
		// Bundle bundle = intent.getExtras();
		mPackageName = intent.getStringExtra("packageName");
	}

	public void initView() {
		mLoadingPager = new LoadingPager(UIUtils.getContext()) {

			@Override
			public LoadedResult initData() {
				return onInitData();
			}

			@Override
			public View initSuccessView() {
				return onInitSuccessView();
			}

		};

		// 设置视图
		setContentView(mLoadingPager);
	}

	@Override
	public void initActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("应用市场");
		// 显示返回按钮
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void initData() {
		// 触发加载数据
		mLoadingPager.loadData();
	}

	// 数据加载
	private LoadedResult onInitData() {
		mDetailProtocol = new DetailProtocol(mPackageName);
		try {
			mLoadData = mDetailProtocol.loadData(0);
			if (mLoadData == null) {
				return LoadedResult.EMPTY;
			}
			return LoadedResult.SUCCESS;

		} catch (Exception e) {
			e.printStackTrace();
			return LoadedResult.ERROR;
		}

	}

	@ViewInject(R.id.app_detail_bottom)
	FrameLayout appDetailBottom;
	@ViewInject(R.id.app_detail_info)
	FrameLayout appDetailInfo;
	@ViewInject(R.id.app_detail_safe)
	FrameLayout appDetailSafe;
	@ViewInject(R.id.app_detail_pic)
	FrameLayout appDetailPic;
	@ViewInject(R.id.app_detail_des)
	FrameLayout appDetailDes;

	// 视图显示
	private View onInitSuccessView() {
		View view = View.inflate(UIUtils.getContext(), R.layout.activity_detail, null);
		ViewUtils.inject(this, view);

		// 信息部分
		AppDetailInfoHolder appDetailInfoHolder = new AppDetailInfoHolder();
		appDetailInfo.addView(appDetailInfoHolder.getHolderView());
		// 设置数据刷新视图
		appDetailInfoHolder.setDataAndRefreshHolderView(mLoadData);

		// 安全部分
		AppDetailSafeHolder appDetailSafeHolder = new AppDetailSafeHolder();
		appDetailSafe.addView(appDetailSafeHolder.getHolderView());
		// 设置数据刷新视图
		appDetailSafeHolder.setDataAndRefreshHolderView(mLoadData);

		// 截图部分
		AppDetailPicHolder appDetailPicHolder = new AppDetailPicHolder();
		appDetailPic.addView(appDetailPicHolder.getHolderView());
		// 设置数据刷新视图
		appDetailPicHolder.setDataAndRefreshHolderView(mLoadData);

		// 简介部分
		AppDetailDesHolder appDetailDesHolder = new AppDetailDesHolder();
		appDetailDes.addView(appDetailDesHolder.getHolderView());
		// 设置数据刷新视图
		appDetailDesHolder.setDataAndRefreshHolderView(mLoadData);

		// 底部下载部分
		mAppDetailBottomHolder = new AppDetailBottomHolder();
		appDetailBottom.addView(mAppDetailBottomHolder.getHolderView());
		// 设置数据刷新视图
		mAppDetailBottomHolder.setDataAndRefreshHolderView(mLoadData);

		// 添加观察者
		DownloadManager.getInstance().addObserver(mAppDetailBottomHolder);

		return view;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 界面不可见需要移除观察者
	@Override
	protected void onPause() {
		if (mAppDetailBottomHolder != null) {
			// 移除观察者
			DownloadManager.getInstance().deleteObserver(mAppDetailBottomHolder);
		}
		super.onPause();
	}

	// 界面可见需要添加观察者
	@Override
	protected void onResume() {
		if (mAppDetailBottomHolder != null) {
			// 开启监听的时候，手动的去获取最新的状态
			mAppDetailBottomHolder.addObserverAndRefreshBtn();
		}
		super.onResume();
	}
}
