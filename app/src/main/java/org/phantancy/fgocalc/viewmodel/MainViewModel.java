package org.phantancy.fgocalc.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import org.phantancy.fgocalc.data.CalcRepository;
import org.phantancy.fgocalc.data.ServantAvatar;
import org.phantancy.fgocalc.entity.FilterEntity;
import org.phantancy.fgocalc.entity.ServantEntity;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    final String TAG = "MainViewModel";

    //数据源
    private CalcRepository calcRepository;
    private MutableLiveData<List<ServantEntity>> mServants = new MutableLiveData<>();

    public LiveData<List<ServantEntity>> getServants() {
        return mServants;
    }

    //搜索关键词
    private MutableLiveData<String> mKeyword = new MutableLiveData<>();

    public LiveData<String> getKeyword() {
        return mKeyword;
    }

    public String getCurrentKeyword() {
        return mKeyword.getValue();
    }

    public void setKeyword(String keyword) {
        mKeyword.setValue(keyword);
    }

    //清理搜索框
    private MutableLiveData<Boolean> clearSearch = new MutableLiveData<>();
    public LiveData<Boolean> getClearSearch() {
        return clearSearch;
    }

    //筛选列表
    public MutableLiveData<List<FilterEntity>> filters = new MutableLiveData<>();

    //当前页
    private MutableLiveData<Integer> mCurrentPage = new MutableLiveData<>();

    public LiveData<Integer> getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int page) {
        mCurrentPage.setValue(page);
    }

    public MainViewModel(Application app) {
        super(app);
        calcRepository = new CalcRepository(app);
//        getAllServants();
//        getFilters();
    }


    //获取所有从者
    public void getAllServants() {
        Log.d(TAG, "getAllServant");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<ServantEntity> svts = calcRepository.getAllServants();
                Log.d(TAG,"size:" + svts.size());
                mServants.postValue(ServantAvatar.setAvatars(svts));
            }
        }).start();

    }

    /**
     * 搜索
     */
    public void getServantsByKeyword(String keyword) {
      Log.d(TAG,"searchServant");

        new Thread(new Runnable() {
          @Override
          public void run() {
              List<ServantEntity> svts = calcRepository.getServantsByKeyWord(keyword);
              mServants.postValue(ServantAvatar.setAvatars(svts));
          }
      }).start();
    }

    //清空搜索栏
    public void clearKeyword() {
        clearSearch.setValue(true);
    }

    //获取筛选列表
    public void getFilters() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                filters.postValue(calcRepository.getFilters());
            }
        }).start();
    }

    public void getServantByFilter(List<FilterEntity> filters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sql = "SELECT * FROM servants WHERE 1=1";
                String classType = handleClassType(filters.get(0).getValue0());
                String star = handleStar(filters.get(1).getValue1());
                String attribute = handleAttribute(filters.get(2).getValue0());
                String traits = handleTraits(filters.get(4).getValue0());
                String npColor = handleNpColor(filters.get(5).getValue0());
                String npType = handleNpType(filters.get(6).getValue0());
                String cards = handleCards(filters.get(7).getValue0());
                String orderType = handleOrderType(filters.get(3).getValue0());

                Log.d(TAG,classType + " " + star + " " + attribute + " " + orderType + " " + traits + " "
                        +  npColor + " " + npType + " " + cards);

                sql += classType + star + attribute + traits + npColor + npType + cards + orderType;
                Log.d(TAG,"sql:" + sql);
                SimpleSQLiteQuery query = new SimpleSQLiteQuery(sql);
                List<ServantEntity> svts = calcRepository.getServantsByFilter(query);
                mServants.postValue(ServantAvatar.setAvatars(svts));
            }
        }).start();
    }

    //职阶类型
    private String handleClassType(String x) {
        return x.equals("any") ? "" : " AND class_type = '" + x + "'";
    }

    //星
    private String handleStar(int x) {
        return x == -1 ? "" : " AND star = " + x;
    }

    //阵营
    private String handleAttribute(String x) {
        return x.equals("any") ? "" : " AND attribute = '" + x + "'";
    }

    //特性
    private String handleTraits(String x) {
        return x.equals("any") ? "" : " AND traits LIKE '%" + x + "%'";
    }

    //宝具卡色
    private String handleNpColor(String x) {
        return x.equals("any") ? "" : " AND np_color = '" + x + "'";
    }

    //宝具类型
    private String handleNpType(String x) {
        return x.equals("any") ? "" : " AND np_type = '" + x + "'";
    }

    //卡序
    private String handleCards(String x) {
        return x.equals("any") ? "" : " AND cards = '" + x + "'";
    }

    //排序
    private String handleOrderType(String x) {
        return " ORDER BY " + x;
    }

}
