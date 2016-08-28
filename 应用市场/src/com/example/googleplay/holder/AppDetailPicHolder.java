package com.example.googleplay.holder;

import java.util.List;

import com.example.googleplay.R;
import com.example.googleplay.base.BaseHolder;
import com.example.googleplay.bean.AppInfoBean;
import com.example.googleplay.conf.Constants.URLS;
import com.example.googleplay.utils.BitmapHelper;
import com.example.googleplay.utils.UIUtils;
import com.example.googleplay.views.SmartImageView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class AppDetailPicHolder extends BaseHolder<AppInfoBean>
{

	@ViewInject(R.id.app_detail_pic_iv_container)
	LinearLayout mPicIvContainer;

	@Override
	public View initHolderView() {
		View view = View.inflate(UIUtils.getContext(), R.layout.app_detail_pic, null);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void refreshHolderView(AppInfoBean data) {
		List<String> screen = data.screen;
		for (int i = 0; i < screen.size(); i++) {
			String uri = URLS.IMAGEBASEURL + screen.get(i);

			SmartImageView iv = new SmartImageView(UIUtils.getContext());
			BitmapHelper.display(iv, uri);

			// 获取屏幕宽度
			int widthPixels = UIUtils.getResouce().getDisplayMetrics().widthPixels;
			// 减去内边距
			widthPixels = widthPixels - mPicIvContainer.getPaddingLeft() - mPicIvContainer.getPaddingRight();
			// widthPixels/3
			int picWidth = widthPixels / 3;

			// 某View被LinearLayout包含，则该View的setLayoutParams参数类型必须是LinearLayout.LayoutParams
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(picWidth, LayoutParams.WRAP_CONTENT);
			// 从第二个图片开始设置左外边距
			if (i != 0) {
				layoutParams.leftMargin = UIUtils.dip2px(5);
			}

			// 将比例容器加入容器
			mPicIvContainer.addView(iv, layoutParams);

			// 按比例设置图片
			iv.setRatio((float) 0.6);
		}

	}

}
