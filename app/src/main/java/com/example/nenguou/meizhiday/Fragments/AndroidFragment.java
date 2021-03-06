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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.nenguou.meizhiday.Entity.Gank;
import com.example.nenguou.meizhiday.GetDatas.GankOkhttp;
import com.example.nenguou.meizhiday.MainActivity;
import com.example.nenguou.meizhiday.UI.others.GithubPageActivity;
import com.example.nenguou.meizhiday.R;
import com.example.nenguou.meizhiday.adapter.Android_iOS_Adapter;
import com.example.nenguou.meizhiday.network.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AndroidFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AndroidFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AndroidFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SwipeRefreshLayout android_ios_swipe_refresh_layout;
    private RecyclerView android_ios_recyclerview;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Gank> main_ganks = new ArrayList<>();
    private Android_iOS_Adapter android_iOS_adapter;
    GetAndroidDates getAndroidDates = null;
    private boolean androidIsLoading = true;

    private int count = 1;
    private int lastVisibleItem;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initId();
        initViews();
        if (!Utils.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), "请检查网络", Toast.LENGTH_SHORT).show();
        } else {
            //firstLoad();
            FirstLoadDatas();
            setRefreshListener();
        }
        //FirstLoadDatas();
        //setRefreshListener();
    }

    private void setRefreshListener() {
        android_ios_swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAndroidDates = new GetAndroidDates("http://gank.io/api/data/Android/10/1", 1);
                getAndroidDates.execute();
            }
        });
        //android_iOS_adapter.setPreLoadNumber(main_ganks.size()+4);
        android_ios_recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && androidIsLoading &&
                        lastVisibleItem + 3 >= linearLayoutManager.getItemCount()) {
                    androidIsLoading = false;
                    getAndroidDates =
                    new GetAndroidDates("http://gank.io/api/data/Android/20/" + (++count), 0);
                    getAndroidDates.execute();

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private void initViews() {
        linearLayoutManager = new LinearLayoutManager(getContext());
        android_ios_recyclerview.setLayoutManager(linearLayoutManager);
        android_ios_recyclerview.setHasFixedSize(true);
        android_ios_swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary, R.color.colorYello, R.color.colorAccent, R.color.colorTablayout);
        android_iOS_adapter = new Android_iOS_Adapter(R.layout.card_layout_android_ios, main_ganks, getContext());
        android_ios_recyclerview.setAdapter(android_iOS_adapter);
        android_iOS_adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        android_iOS_adapter.isFirstOnly(true);
        android_iOS_adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                //Toast.makeText(getContext(), "第" + i + "个", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), GithubPageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", main_ganks.get(i).url);
                bundle.putString("title", main_ganks.get(i).desc);
                intent.putExtras(bundle);
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(view, (int) view.getWidth() / 2, (int) view.getHeight() / 2, 0, 0);
                ActivityCompat.startActivity(getContext(), intent, compat.toBundle());
            }
        });
    }

    private void FirstLoadDatas() {
        getAndroidDates = new GetAndroidDates("http://gank.io/api/data/Android/10/1", 0);
        getAndroidDates.execute();
    }

    private void initId() {
        android_ios_recyclerview = (RecyclerView) getView().findViewById(R.id.android_ios_recyclerview);
        android_ios_swipe_refresh_layout = (SwipeRefreshLayout) getView().findViewById(R.id.android_ios_swipe_refresh_layout);
    }


    public AndroidFragment() {
        // Required empty public constructor
    }

    public static AndroidFragment newInstance() {
        AndroidFragment fragment = new AndroidFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_android, container, false);
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

    @Override
    public void onPause() {
        super.onPause();
        getAndroidDates.cancel(true);
        Log.d("LifeCircle","onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("LifeCircle","onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("LifeCircle","onDestroyView");
    }

    public class GetAndroidDates extends AsyncTask<String, Void, String> {

        private List<Gank> ganks;
        private String url;
        private String datas;
        private String JSONDatas;
        private int falg;
        private boolean isLoading = true;

        public GetAndroidDates(String url, int falg) {
            this.url = url;
            this.falg = falg;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            android_ios_swipe_refresh_layout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (isLoading) {
                try {
                    datas = GankOkhttp.getDatas(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(),"当前网络状态不好，请稍后再试。",Toast.LENGTH_SHORT).show();
                    datas = "";
                }
                isLoading = false;

            }
            return datas;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (datas.toString() == ""){
                android_ios_swipe_refresh_layout.setRefreshing(false);
            }
            if (falg == 0 && datas.toString() != "") {
                Gson gson = new Gson();
                Log.d("isLoadingnow","isLoading");
                try {
                    JSONObject jsonObject = new JSONObject(datas);
                    JSONDatas = jsonObject.getString("results");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Type type = new TypeToken<ArrayList<Gank>>() {
                }.getType();
                ganks = gson.fromJson(JSONDatas, type);
                Log.i("tetet", ganks.get(0).desc);
                try{main_ganks.addAll(ganks);}catch (Exception e){}
                android_iOS_adapter.notifyItemInserted(main_ganks.size());
                android_ios_swipe_refresh_layout.setRefreshing(false);
                androidIsLoading = true;
                if (isLoading == false){
                    isLoading = true;
                }
            }
            if (falg == 1) {
                Toast.makeText(getContext(), "No more datas", Toast.LENGTH_SHORT).show();
                android_ios_swipe_refresh_layout.setRefreshing(false);
            }
//            android_iOS_adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
//                @Override
//                public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
//                    //Toast.makeText(getContext(), "第" + i + "个", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(getContext(), GithubPageActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("url", main_ganks.get(i).url);
//                    bundle.putString("title", main_ganks.get(i).desc);
//                    intent.putExtras(bundle);
//                    ActivityOptionsCompat compat = ActivityOptionsCompat.makeScaleUpAnimation(view, (int) view.getWidth() / 2, (int) view.getHeight() / 2, 0, 0);
//                    ActivityCompat.startActivity(getContext(), intent, compat.toBundle());
//                }
//            });


        }
    }
}
