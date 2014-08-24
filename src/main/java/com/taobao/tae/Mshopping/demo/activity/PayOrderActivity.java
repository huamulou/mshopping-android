package com.taobao.tae.Mshopping.demo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import com.taobao.tae.Mshopping.demo.R;
import com.taobao.tae.Mshopping.demo.model.CreateOrderResp;
import com.taobao.tae.Mshopping.demo.task.GetPayOrderUrlTask;
import com.taobao.tae.Mshopping.demo.util.NetWorkStateUtil;

public class PayOrderActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pay_order_activity);

        if (!NetWorkStateUtil.isConnected(this)) {
            finish();
            toast("请检查网络连接");
            return;
        }

        CreateOrderResp createOrderResp = (CreateOrderResp) getIntent().getSerializableExtra("createOrderResp");
        if (createOrderResp == null) {
            finish();
        }
        final String itemId = getIntent().getStringExtra("itemId");
        WebView webView = (WebView) findViewById(R.id.pay_order_wv);
        webView.requestFocus();
        WebSettings wSet = webView.getSettings();
        wSet.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("WebView", "onPageStarted");
                Bundle bundle = new Bundle();
                bundle.putString("itemId", itemId);
                if (url != null && url.contains("http://m.taobao.com")) {
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.putExtra("ACTIVITY_NAME_KEY", R.string.title_activity_pay_order);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setClass(PayOrderActivity.this, ItemDetailActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                if (url != null && url.contains("m.alipay.com")) {
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.putExtra("ACTIVITY_NAME_KEY", R.string.title_activity_pay_order);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setClass(PayOrderActivity.this, ItemDetailActivity.class);
                    startActivity(intent);
                    finish();
                    toast("服务端异常，请稍后再试");
                }
            }
        });

        GetPayOrderUrlTask getPayOrderUrlTask = new GetPayOrderUrlTask(this, createOrderResp, webView);
        getPayOrderUrlTask.execute();
        RelativeLayout backBtnLayout = (RelativeLayout) findViewById(R.id.pay_order_top_back_btn);
        backBtnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


}
