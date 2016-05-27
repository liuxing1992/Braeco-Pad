package com.braeco.braecowaiter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.UIs.MyDownBounceAnimator;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dd.CircularProgressButton;
import com.hb.views.PinnedSectionListView;
import com.malinskiy.superrecyclerview.OnMoreListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Weiping on 2015/12/1.
 */
public class ServiceMenuFragment extends Fragment
        implements
        SwipeRefreshLayout.OnRefreshListener,
        OnMoreListener,
        MenuAdapter.OnOrderListener {

    public final static int START_SET = 8;

    private Activity activity;

    private SwipeRefreshLayout refresh;

    private PinnedSectionListView menus;
    private MenuAdapter menuAdapter;

    private ListView categories;
    private CategoryAdapter categoryAdapter;

    private int lastFirst = 0;

    private FrameLayout car_fy;

    private ImageView car;

    private TextView num;

    private CircularProgressButton reload;
    private FrameLayout emptyTip;

    private int lastSum = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            activity = (Activity)context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.d("BraecoWaiter", "ServiceMenuFragment onCreateView");
        View messageLayout = inflater.inflate(R.layout.fragment_service_menu, container, false);

        emptyTip = (FrameLayout)messageLayout.findViewById(R.id.empty_tip);
        reload = (CircularProgressButton)messageLayout.findViewById(R.id.reload);
        reload.setIndeterminateProgressMode(true);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BraecoWaiterApplication.LOADING_MENU) {
                    BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                    BraecoWaiterApplication.LOADED_MENU = false;
                    BraecoWaiterApplication.LOADING_MENU = true;
                    reload.setProgress(1);
                    ((OnGetMenu)activity).onGetMenu(true);
                }
            }
        });
        if (BraecoWaiterApplication.mButton == null || BraecoWaiterApplication.mMenu == null) {
            // the menu is loading or loading fail
            if (!BraecoWaiterApplication.LOADING_MENU) {
                if (BraecoWaiterApplication.LOAD_MENU_FAIL) {
                    // the menu is loading fail
                    reload.setIdleText("载入菜单失败，点击重新载入");
                    reload.setProgress(0);
                } else {
                    reload.setIdleText("点击载入菜单");
                    reload.setProgress(0);
                }
            } else {
                // the menu is loading
                reload.setProgress(1);
            }
        } else {
            // the menu is loaded completely
            reload.setVisibility(View.INVISIBLE);
            emptyTip.setVisibility(View.INVISIBLE);
        }
        refresh = (SwipeRefreshLayout)messageLayout.findViewById(R.id.refresh);
        refresh.setEnabled(false);
        menus = (PinnedSectionListView)messageLayout.findViewById(R.id.menu);
        menus.initShadow(false);
        categories = (ListView)messageLayout.findViewById(R.id.category);

        car_fy = (FrameLayout)messageLayout.findViewById(R.id.car_fy);
        car_fy.bringToFront();
        car = (ImageView) messageLayout.findViewById(R.id.car);
        car_fy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Bounce)
                        .duration(700)
                        .playOn(car_fy);
                boolean hasMeal = false;
//                for (int i = BraecoWaiterApplication.orderedMeals.size() - 1; i >= 0; i--) {
//                    if (BraecoWaiterApplication.orderedMeals.get(i).size() != 0) {
//                        hasMeal = true;
//                        break;
//                    }
//                }
                for (Pair<Map<String, Object>, Integer> p : BraecoWaiterApplication.orderedMealsPair) {
                    if (p.second > 0) {
                        hasMeal = true;
                        break;
                    }
                }
                if (hasMeal) {
                    Intent intent = new Intent(getActivity(), ServiceMenuFragmentCar.class);
                    getActivity().startActivity(intent);
                } else {
                    BraecoWaiterUtils.showToast(getActivity(), "请先点菜吧！");
                }
            }
        });

        num = (TextView)messageLayout.findViewById(R.id.num);
        num.bringToFront();
        setNum(true);

        if (BraecoWaiterApplication.mButton == null || BraecoWaiterApplication.mMenu == null) {
            return messageLayout;
        }

        emptyTip.setVisibility(View.INVISIBLE);
        reload.setVisibility(View.INVISIBLE);

        menuAdapter = new MenuAdapter(this);
        menus.setAdapter(menuAdapter);

        categoryAdapter = new CategoryAdapter();
        categories.setAdapter(categoryAdapter);

        refresh.setEnabled(true);

        setMenuListener();

        return messageLayout;
    }

    private void setMenuListener() {
        menus.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                refresh.setEnabled(false);

                if (firstVisibleItem != lastFirst) {
                    lastFirst = firstVisibleItem;
                    for (int i = 0; BraecoWaiterApplication.a[i] != -1; i++) {
                        if (lastFirst < BraecoWaiterApplication.a[i] + i) {
                            categoryAdapter.select(i - 1);
                            int firstCompletelyPosition = categories.getFirstVisiblePosition();
                            int lastCompletelyPosition = categories.getChildCount() - 1;
                            if (categories.getChildAt(0).getTop() < 0) {
                                firstCompletelyPosition++;
                            }
                            if (categories.getChildAt(lastCompletelyPosition).getBottom() > categories.getHeight()) {
                                lastCompletelyPosition--;
                            }
                            if (i - 1 < firstCompletelyPosition) {
                                categories.setSelectionFromTop(i - 1, 0);
                            } else if (i - 1 > lastCompletelyPosition) {
                                categories.setSelection(i - 1);
                            }
                            break;
                        }
                    }
                }

                if (firstVisibleItem == 0) {
                    // check if we reached the top or bottom of the list
                    View v = menus.getChildAt(0);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
//                        Log.d("BraecoWaiter", "To Top");
                        refresh.setEnabled(true);
                        return;
                    }
                } else if (totalItemCount - visibleItemCount == firstVisibleItem){
                    View v =  menus.getChildAt(totalItemCount-1);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
//                        Log.d("BraecoWaiter", "To Bottom");
                        return;
                    }
                }

            }
        });

        categories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                categoryAdapter.select(position);
                // Todo
                menus.setSelectionFromTop(BraecoWaiterApplication.a[position] + position, 0);
            }
        });



        refresh.setEnabled(true);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int sum = 0;
                for (int i = 0; i < BraecoWaiterApplication.orderedMeals.size(); i++) {
                    sum += BraecoWaiterApplication.orderedMeals.get(i).size();
                }
                if (sum > 0) {
                    new MaterialDialog.Builder(ServiceMenuFragment.this.getActivity())
                            .title("刷新")
                            .content("刷新菜单将会清空购物车，您需要重新点餐，确认刷新吗？")
                            .positiveText("确认")
                            .negativeText("取消")
                            .onAny(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    if (dialogAction == DialogAction.POSITIVE) {
                                        if (!BraecoWaiterApplication.LOADING_MENU) {
                                            BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                                            BraecoWaiterApplication.LOADED_MENU = false;
                                            BraecoWaiterApplication.LOADING_MENU = true;
                                            ((OnGetMenu)activity).onGetMenu(false);
                                            for (int i = BraecoWaiterApplication.orderedMeals.size() - 1; i >= 0; i--)
                                                BraecoWaiterApplication.orderedMeals.get(i).clear();
                                            BraecoWaiterApplication.orderedMealsPair.clear();
                                            setNum(false);
                                        }
                                    } else if (dialogAction == DialogAction.NEGATIVE) {
                                        refresh.setRefreshing(false);
                                    }
                                }
                            })
                            .show();
                } else {
                    if (!BraecoWaiterApplication.LOADING_MENU) {
                        BraecoWaiterApplication.LOAD_MENU_FAIL = false;
                        BraecoWaiterApplication.LOADED_MENU = false;
                        BraecoWaiterApplication.LOADING_MENU = true;
                        ((OnGetMenu)activity).onGetMenu(false);
                    }
                }
            }
        });
    }

    public void refreshMenu() {
        Log.d("BraecoWaiter", "refresh menu");
        if (menus == null || categories == null) {
            return;
        }
        if (menuAdapter == null) {
            menuAdapter = new MenuAdapter(this);
            categoryAdapter = new CategoryAdapter();
            menus.setAdapter(menuAdapter);
            categories.setAdapter(categoryAdapter);
        } else {
            menuAdapter.notifyDataSetChanged();
            categoryAdapter.notifyDataSetChanged();
        }
        refresh.setRefreshing(false);
        emptyTip.setVisibility(View.INVISIBLE);
        reload.setVisibility(View.INVISIBLE);
        setMenuListener();
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {

    }

    @Override
    public void OnOrderListen(boolean setNum) {
        categoryAdapter.notifyDataSetChanged();
        if (setNum) setNum(false);
    }

    @Override
    public void OnAnimationListen(View v, int[] startLocation) {
        setAnim(v, startLocation);
    }

    public void setEmptyTip(int progress, String tip, int vis) {
        if (vis == View.VISIBLE) {
            refresh.setRefreshing(false);
            refresh.setEnabled(false);
        }
        if (emptyTip != null && reload != null) {
            reload.setProgress(progress);
            reload.setIdleText(tip);
            reload.setVisibility(vis);
            emptyTip.setVisibility(vis);
        }
    }

    private void setNum(boolean fromResume) {
        int sum = 0;
        for (Pair<Map<String, Object>, Integer> p : BraecoWaiterApplication.orderedMealsPair) {
            sum += p.second;
        }
        // because the set isn't there
//        for (int i = 0; i < BraecoWaiterApplication.orderedMeals.size(); i++) {
//            sum += BraecoWaiterApplication.orderedMeals.get(i).size();
//        }
        if (car_fy != null) {
            if (sum < lastSum) {
                YoYo.with(Techniques.Tada)
                        .duration(700)
                        .playOn(car_fy);
            } else {
                if (!fromResume) {
                    YoYo.with(new MyDownBounceAnimator())
                            .duration(700)
                            .playOn(car_fy);
                }
            }
            lastSum = sum;
        }
        if (num != null) {
            if (sum == 0) {
                num.setVisibility(View.INVISIBLE);
            } else {
                num.setVisibility(View.VISIBLE);
                if (sum >= 100) {
                    num.setText("...");
                } else {
                    num.setText(sum + "");
                }

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menuAdapter != null) menuAdapter.notifyDataSetChanged();
        if (categoryAdapter != null) categoryAdapter.notifyDataSetChanged();
        setNum(true);
        if (BraecoWaiterApplication.LOAD_MENU_FAIL) {
            setEmptyTip(0, "菜单加载失败，点击重新加载", View.VISIBLE);
            refresh.setEnabled(false);
        } else {
            refresh.setEnabled(true);
            reload.setVisibility(View.INVISIBLE);
            if (BraecoWaiterApplication.LOADED_MENU)
                emptyTip.setVisibility(View.INVISIBLE);
            else {
                emptyTip.setVisibility(View.VISIBLE);
                reload.setVisibility(View.VISIBLE);
                reload.setProgress(1);
            }
        }
    }

    public interface OnGetMenu {
        void onGetMenu(boolean firstTime);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BraecoWaiter", "Goodbye ServiceMenuFragment");
    }

    /**
     * @Description: 创建动画层
     * @param
     * @return void
     * @throws
     */
    private ViewGroup anim_mask_layout;
    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) ServiceMenuFragment.this.getActivity().getWindow().getDecorView();
        LinearLayout animLayout = new LinearLayout(ServiceMenuFragment.this.getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setId(Integer.MAX_VALUE-1);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    private View addViewToAnimLayout(final ViewGroup parent, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    public void setAnim(final View v, int[] startLocation) {
        anim_mask_layout = null;
        anim_mask_layout = createAnimLayout();
        anim_mask_layout.addView(v);//把动画小球添加到动画层
        final View view = addViewToAnimLayout(anim_mask_layout, v,
                startLocation);
        int[] endLocation = new int[2];// 存储动画结束位置的X、Y坐标
        car.getLocationInWindow(endLocation);// shopCart是那个购物车
        endLocation[0] += car.getWidth() / 2;
        endLocation[1] += car.getHeight() / 2;

        // 计算位移
        int endX = 0 - startLocation[0] + 40;// 动画位移的X坐标
        int endY = endLocation[1] - startLocation[1];// 动画位移的y坐标
        TranslateAnimation translateAnimationX = new TranslateAnimation(0,
                endX, 0, 0);
        translateAnimationX.setInterpolator(new LinearInterpolator());
        translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(true);

        TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0,
                0, endY);
        translateAnimationY.setInterpolator(new AccelerateInterpolator());
        translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(true);

        AnimationSet set = new AnimationSet(false);

        v.setVisibility(View.VISIBLE);

        float[] sl = new float[2];
        float[] ml = new float[2];
        float[] el = new float[2];
        sl[0] = startLocation[0] * 1.0f;
        sl[1] = startLocation[1] * 1.0f;
        el[0] = endLocation[0] * 1.0f;
        el[1] = endLocation[1] * 1.0f;
        ml[0] = (sl[0] + el[0]) / 2;  // + (new Random().nextInt(200) - 100);
        ml[1] = sl[1] / 2;

        AnimatorSet animatorSetXY = new AnimatorSet();
        AnimatorSet animatorSetX = new AnimatorSet();
        AnimatorSet animatorSetY = new AnimatorSet();
        ArrayList<Animator> animatorsX = new ArrayList<>();
        ArrayList<Animator> animatorsY = new ArrayList<>();
        long duration = 1500;
        int count = 20;
        float step = 0;
        float per = 1f / count;
        float xx = sl[0] - el[0];
        float yy = sl[1] - el[1];
        for (int i = 0; i <= count; i++) {
            float x = sl[0] - i * per * xx;
            float x2 = sl[0] - (i + 1) * per * xx;
            ObjectAnimator xTrasnlationObjectAnimator = ObjectAnimator.ofFloat(v, "x", sl[0] - i * per * xx);
            float y = getY(sl, el, ml, x);
            float y2 = getY(sl, el, ml, x2);
            ObjectAnimator yTrasnlationObjectAnimator = ObjectAnimator.ofFloat(v, "y", y);
            xTrasnlationObjectAnimator.setDuration(1);
            yTrasnlationObjectAnimator.setDuration(1);
            animatorsX.add(xTrasnlationObjectAnimator);
            animatorsY.add(yTrasnlationObjectAnimator);
        }

        animatorSetX.playSequentially(animatorsX);
        animatorSetY.playSequentially(animatorsY);
        animatorSetXY.playTogether(animatorSetX, animatorSetY);
        animatorSetXY.start();
        animatorSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
                setNum(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        set.setFillAfter(false);
        set.addAnimation(translateAnimationY);
        set.addAnimation(translateAnimationX);
        set.setDuration(800);// 动画的执行时间
//        view.startAnimation(set);
        // 动画监听事件
        set.setAnimationListener(new Animation.AnimationListener() {
            // 动画的开始
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            // 动画的结束
            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);

            }
        });

    }

    private static float getY(float[] startPoint, float[] endPoint, float[] midPoint, float x) {
        float x1 = startPoint[0];
        float y1 = startPoint[1];
        float x2 = endPoint[0];
        float y2 = endPoint[1];
        float x3 = midPoint[0];
        float y3 = midPoint[1];
        float a, b, c;

        a = (y1 * (x2 - x3) + y2 * (x3 - x1) + y3 * (x1 - x2))
                / (x1 * x1 * (x2 - x3) + x2 * x2 * (x3 - x1) + x3 * x3 * (x1 - x2));
        b = (y1 - y2) / (x1 - x2) - a * (x1 + x2);
        c = y1 - (x1 * x1) * a - x1 * b;

        return a * x * x + b * x + c;
    }

}
