package com.example.nenguou.meizhiday.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.nenguou.meizhiday.Entity.Gank;
import com.example.nenguou.meizhiday.GetDatas.GankOkhttp;
import com.example.nenguou.meizhiday.UI.others.GithubPageActivity;
import com.example.nenguou.meizhiday.R;
import com.example.nenguou.meizhiday.adapter.Android_iOS_Adapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SwipeRefreshLayout qianduan_swipe_refresh_layout;
    private RecyclerView qianduan_recyclerview;
    private LinearLayoutManager linearLayoutManager;
    private Android_iOS_Adapter qianduan_adapter;
    private ArrayList<Gank> main_ganks = new ArrayList<>();
    private int lastVisibleItem;
    private int count = 1;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inidId();
        inidViews();
        FirstLoadDatas();
        setRefreshListener();
    }

    private void setRefreshListener() {
        qianduan_swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetQianDuanDatas("http://gank.io/api/data/%E5%89%8D%E7%AB%AF/10/1",1).execute();
            }
        });
        qianduan_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem +3 >= linearLayoutManager.getItemCount()){
                    new GetQianDuanDatas("http://gank.io/api/data/%E5%89%8D%E7%AB%AF/20/"+(++count),0).execute();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private void FirstLoadDatas() {
        new GetQianDuanDatas("http://gank.io/api/data/%E5%89%8D%E7%AB%AF/10/1",0).execute();
    }

    private void inidViews() {
        linearLayoutManager = new LinearLayoutManager(getContext());
        qianduan_recyclerview.setLayoutManager(linearLayoutManager);
        qianduan_recyclerview.hasFixedSize();
        qianduan_swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary, R.color.colorYello, R.color.colorAccent, R.color.colorTablayout);
        qianduan_adapter = new Android_iOS_Adapter(R.layout.card_layout_android_ios,main_ganks,getContext());
        qianduan_recyclerview.setAdapter(qianduan_adapter);
        qianduan_adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        qianduan_adapter.isFirstOnly(true);
    }

    private void inidId() {
      qianduan_swipe_refresh_layout = (SwipeRefreshLayout) getView().findViewById(R.id.qianduan_swipe_refresh_layout);
      qianduan_recyclerview = (RecyclerView) getView().findViewById(R.id.qianduan_recyclerview);
    }

    public class GetQianDuanDatas extends AsyncTask<String,Void,String>{

        private List<Gank> ganks;
        private String url;
        private String datas;
        private String JSONDatas;
        private int flag;

        public GetQianDuanDatas(String url,int flag){
            this.url = url;
            this.flag = flag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            qianduan_swipe_refresh_layout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                datas = GankOkhttp.getDatas(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return datas;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(flag == 0){
                Gson gson = new Gson();
                try {
                    JSONObject jsonObject = new JSONObject(datas);
                    JSONDatas = jsonObject.getString("results");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Type type = new TypeToken<ArrayList<Gank>>(){}.getType();
                ganks = gson.fromJson(JSONDatas,type);
                main_ganks.addAll(ganks);
                qianduan_adapter.notifyItemInserted(main_ganks.size());
                qianduan_swipe_refresh_layout.setRefreshing(false);
            }else if(flag == 1){
                Toast.makeText(getContext(),"No more datas",Toast.LENGTH_SHORT).show();
                qianduan_swipe_refresh_layout.setRefreshing(false);
            }

            qianduan_adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                    Intent intent = new Intent(getContext(), GithubPageActivity.class);
                    Bundle bundle = new Bundle();

                    bundle.putString("url",main_ganks.get(i).url);
                    bundle.putString("title",main_ganks.get(i).desc);
                    intent.putExtras(bundle);
                    ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(view,(int)view.getWidth()/2,(int)view.getHeight()/2,0,0);
                    ActivityCompat.startActivity(getContext(),intent,compat.toBundle());
                }
            });
        }
    }

    public VideoFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_front, container, false);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
