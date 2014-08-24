package com.taobao.tae.Mshopping.demo.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.taobao.api.internal.util.WebUtils;
import com.taobao.tae.Mshopping.demo.R;
import com.taobao.tae.Mshopping.demo.constant.Constants;
import com.taobao.tae.Mshopping.demo.fegment.ItemsListFragment;
import com.taobao.tae.Mshopping.demo.model.TaobaoItemBasicInfo;
import com.taobao.tae.Mshopping.demo.util.Helper;
import com.taobao.tae.Mshopping.demo.util.SecurityKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xinyuan on 14/6/25.
 */
public class ItemContentTask extends AsyncTask<String, Integer, List<TaobaoItemBasicInfo>> {

    private Context context;
    private int type = Constants.PULL_REFRESH_ACTION;
    ItemsListFragment itemsListFragment;

    public ItemContentTask(Context context, int type, ItemsListFragment itemsListFragment) {
        super();
        this.context = context;
        this.type = type;
        this.itemsListFragment = itemsListFragment;
    }

    @Override
    protected List<TaobaoItemBasicInfo> doInBackground(String... params) {
        try {
            return parseItemsJSON(params[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<TaobaoItemBasicInfo> result) {
        if(result == null){
            toast("获取商品失败");
            return;
        }

        if (type == Constants.PULL_REFRESH_ACTION) {
            itemsListFragment.staggeredAdapter.addItemTop(result);
            itemsListFragment.staggeredAdapter.notifyDataSetChanged();
            itemsListFragment.refreshableListView.stopRefresh();

            if (result.size() > 0) {
                itemsListFragment.lastPushedItemsTime = result.get(0).getPushTime();//更新时间
            }
            if (itemsListFragment.lastPushedItemsTime != null) {
                String dateTime;
                if (itemsListFragment.lastPushedItemsTime.getDate() == new Date().getDate()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    dateTime = "今天 ".concat(dateFormat.format(itemsListFragment.lastPushedItemsTime));
                } else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dateTime = dateFormat.format(itemsListFragment.lastPushedItemsTime);
                }
                itemsListFragment.refreshableListView.setRefreshTime(dateTime);
            }
        } else if (type == Constants.VIEW_MORE_ITEMS_ACTION) {
            itemsListFragment.refreshableListView.stopLoadMore();
            itemsListFragment.staggeredAdapter.addItemLast(result);
            itemsListFragment.staggeredAdapter.notifyDataSetChanged();
        }else if(type == Constants.OTHER_ACTION){
        }

    }

    @Override
    protected void onPreExecute() {
    }

    public List<TaobaoItemBasicInfo> parseItemsJSON(String url) throws IOException {
        List<TaobaoItemBasicInfo> taobaoItemBasicInfos = new ArrayList<TaobaoItemBasicInfo>();
        String json = "";
        if (Helper.checkConnection(context)) {
            try {
                int timeout = 30000;
                Map param = new HashMap<String, String>();
                param.put("securityKey", SecurityKey.getKey());
                json = WebUtils.doPost(url, param, timeout, timeout);
            } catch (IOException e) {
                Log.e("IOException is : ", e.toString());
                e.printStackTrace();
                return null;
            }
        }
        try {
            if (null != json) {
                JSONArray jsonArray = new JSONArray(json);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonArrayJSONObject = jsonArray.getJSONObject(i);
                    TaobaoItemBasicInfo taobaoItemBasicInfo = new TaobaoItemBasicInfo();
                    taobaoItemBasicInfo.setItemId(jsonArrayJSONObject.isNull("itemId") ? 0 : jsonArrayJSONObject.getLong("itemId"));
                    taobaoItemBasicInfo.setPicUrl(jsonArrayJSONObject.isNull("picUrl") ? "" : jsonArrayJSONObject.getString("picUrl"));
                    taobaoItemBasicInfo.setTitle(jsonArrayJSONObject.isNull("title") ? "" : jsonArrayJSONObject.getString("title"));
                    taobaoItemBasicInfo.setPrice(jsonArrayJSONObject.isNull("price") ? "" : jsonArrayJSONObject.getString("price"));
                    taobaoItemBasicInfo.setPushTime(jsonArrayJSONObject.isNull("pushTime") ? null : new Date(jsonArrayJSONObject.getLong("pushTime")));
                    taobaoItemBasicInfos.add(taobaoItemBasicInfo);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return taobaoItemBasicInfos;
    }


    /**
     * 展示一个粉色的Toast
     *
     * @param message
     */
    public void toast(String message) {
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.toast, null);
        Toast toast = new Toast(context);
        toast.setView(toastRoot);
        TextView tv = (TextView) toastRoot.findViewById(R.id.pink_toast_notice);
        tv.setText(message);
        toast.show();
    }
}
