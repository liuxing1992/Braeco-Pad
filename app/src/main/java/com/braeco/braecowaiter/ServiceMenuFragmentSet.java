package com.braeco.braecowaiter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.braeco.braecowaiter.UIs.ExpandableLayout;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import me.grantland.widget.AutofitTextView;

public class ServiceMenuFragmentSet extends BraecoAppCompatActivity
    implements
        ServiceMenuFragmentSetSubAdapter.OnPlusClickListener,
        ServiceMenuFragmentSetSubAdapter.OnMinusClickListener {

//    private ObservableScrollView observableScrollView;
    private LinearLayout back;
    private AutofitTextView title;
    private ListView listView;
    private ServiceMenuFragmentSetAdapter adapter;
    private TextView sum;
    private LinearLayout makeSure;

    private Integer setId = -1;
    public static Map<String, Object> set;
    public static ArrayList<HashMap<Integer, Stack<Map<String, Object>>>> orderedCombos;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_menu_fragment_set);

        mContext = this;

        setId = getIntent().getIntExtra("id", -1);
        for (Map<String, Object> s : BraecoWaiterApplication.mMenu) {
            if (s.get("id").equals(setId)) {
                set = s;
                break;
            }
        }

        orderedCombos = new ArrayList<>();
        int comboSetNum = ((ArrayList<Map<String, Object>>)set.get("combo")).size();
        for (int i = 0; i < comboSetNum; i++) {
            orderedCombos.add(new HashMap<Integer, Stack<Map<String, Object>>>());
            for (Integer id : (HashSet<Integer>)((ArrayList<Map<String, Object>>)set.get("combo")).get(i).get("content")) {
                orderedCombos.get(i).put(id, new Stack<Map<String, Object>>());
            }
        }

//        observableScrollView = (ObservableScrollView)findViewById(R.id.scrollView);

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        title = (AutofitTextView) findViewById(R.id.title);
        title.setText((String)set.get("name"));

        sum = (TextView)findViewById(R.id.sum);
        setSum();

        makeSure = (LinearLayout)findViewById(R.id.make_sure);
        makeSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                putInCar();
            }
        });

        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ServiceMenuFragmentSetAdapter(set, this, this);
        listView.setAdapter(adapter);

    }

    private void putInCar() {
        boolean finish = true;
        int size = orderedCombos.size();
        String unfinishedName = "";
        for (int p = 0; p < size; p++) {
            int maxNumber = (Integer)((ArrayList<Map<String, Object>>)set.get("combo")).get(p).get("require");
            if (maxNumber == 0) continue;
            int selectedNumber = 0;
            for (HashMap.Entry<Integer, Stack<Map<String, Object>>> mealStack : orderedCombos.get(p).entrySet()) {
                selectedNumber += mealStack.getValue().size();
            }
            if (selectedNumber != maxNumber) {
                unfinishedName = (String)((ArrayList<Map<String, Object>>)set.get("combo")).get(p).get("name");
                finish = false;
                break;
            }
        }
        if (!finish) {
            new MaterialDialog.Builder(mContext)
                    .title("无法添加套餐")
                    .content(unfinishedName + "尚未选择完成。")
                    .positiveText("确认")
                    .show();
        } else {
            // put this set to car
            Map<String, Object> newSet = new HashMap<>(set);
            newSet.put("fullPrice", fullPrice);  // the total price of this set
            // put the select
            ArrayList<ArrayList<Map<String, Object>>> combos = new ArrayList<>();
            int comboSize = orderedCombos.size();
            for (int p = 0; p < comboSize; p++) {
                // for every combo
                ArrayList<Map<String, Object>> mealsInACombo = new ArrayList<>();
                for (HashMap.Entry<Integer, Stack<Map<String, Object>>> mealStack : orderedCombos.get(p).entrySet()) {
                    // for every sub meals' stack
                    for (Map<String, Object> meal : mealStack.getValue()) {
                        // for every sub meal in the stack
                        Map<String, Object> selectMeal = new HashMap<>();
                        ArrayList<String> properties = new ArrayList<>();
                        int[] choices = (int[])meal.get("choices");
                        ArrayList<String>[] attributeItemName = (ArrayList<String>[]) meal.get("shuxing");
                        int attributeGroupNum = (Integer) meal.get("num_shuxing");
                        for (int j = 0; j < attributeGroupNum; j++) {
                            properties.add(attributeItemName[j].get(choices[j]));
                        }
                        selectMeal.put("id", meal.get("id"));
                        selectMeal.put("properties", properties);
                        mealsInACombo.add(selectMeal);
                    }
                }
                // sort the meals for comparing whether 2 sets are the same
                Collections.sort(mealsInACombo, new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
                        int lId = (Integer)lhs.get("id");
                        int rId = (Integer)rhs.get("id");
                        if (lId < rId) return -1;
                        else if (lId > rId) return 1;
                        else return 0;
                    }
                });
                combos.add(mealsInACombo);
            }
            newSet.put("properties", combos);
            newSet.put("id", newSet.get("id"));
            newSet.put("isSet", true);

//            we don't need to do the following, because the icon '-' and the number will never show in set
//            BraecoWaiterApplication.orderedMeals
//                    .get(BraecoWaiterApplication.index[position])
//                    .push(newMenu);

            BraecoWaiterUtils.getInstance().LogMap(newSet);

            boolean isExist = false;
            for (int i = BraecoWaiterApplication.orderedMealsPair.size() - 1; i >= 0; i--) {
                if (BraecoWaiterUtils.getInstance().isSameMeal(
                        BraecoWaiterApplication.orderedMealsPair.get(i).first,
                        newSet)) {
                    Integer newNum = BraecoWaiterApplication.orderedMealsPair.get(i).second + 1;
                    BraecoWaiterApplication.orderedMealsPair.set(i,
                            new Pair<>(BraecoWaiterApplication.orderedMealsPair.get(i).first, newNum));
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                BraecoWaiterApplication.orderedMealsPair.add(new Pair<>(newSet, 1));
            }
            finish();
        }
    }

    private double fullPrice = 0;
    private void setSum() {
        DecimalFormat df = new DecimalFormat("#.##");
        if ("combo_static".equals(set.get("dc_type"))) {
            fullPrice = Double.parseDouble(df.format(set.get("price")));
            sum.setText("套餐总额： ¥" + String.format("%.2f", fullPrice));
        } else if ("combo_sum".equals(set.get("dc_type"))) {
            fullPrice = 0;
            int p = 0;
            for (HashMap<Integer, Stack<Map<String, Object>>> orderCombo : orderedCombos) {
                for (Stack<Map<String, Object>> menus : orderCombo.values()) {
                    for (Map<String, Object> menu : menus) {
                        int discount = ((Integer)(((ArrayList<Map<String, Object>>)set.get("combo")).get(p).get("discount")));
                        fullPrice += Double.parseDouble(
                                df.format(
                                        (Double)menu.get("fullPrice")
                                                * discount / 100));
                    }
                }
                p++;
            }
            sum.setText("套餐总额： ¥" + String.format("%.2f", fullPrice));
        }
    }

    @Override
    public void onMinus(int id, int p) {
        View view = getViewByPosition(p, listView);
        final AutofitTextView selected = (AutofitTextView)view.findViewById(R.id.selected);
        int maxNumber = (Integer)((ArrayList<Map<String, Object>>)set.get("combo")).get(p).get("require");
        int selectedNumber = 0;
        for (HashMap.Entry<Integer, Stack<Map<String, Object>>> mealStack : orderedCombos.get(p).entrySet()) {
            selectedNumber += mealStack.getValue().size();
        }
        if (maxNumber == 0) {
            selected.setText("已选" + selectedNumber + "款");
        } else {
            if (maxNumber == selectedNumber) {
                selected.setText("已选好");
            } else {
                selected.setText(selectedNumber == 0 ? "尚未选择" : "已选" + selectedNumber + "款");
            }
        }
        selected.setText(selectedNumber == 0 ? "尚未选择" : "已选" + selectedNumber + "款");
        setSum();
    }

    @Override
    public void onPlus(int id, int p) {
        View view = getViewByPosition(p, listView);
        final AutofitTextView selected = (AutofitTextView)view.findViewById(R.id.selected);
        int maxNumber = (Integer)((ArrayList<Map<String, Object>>)set.get("combo")).get(p).get("require");
        int selectedNumber = 0;
        for (HashMap.Entry<Integer, Stack<Map<String, Object>>> mealStack : orderedCombos.get(p).entrySet()) {
            selectedNumber += mealStack.getValue().size();
        }
        if (maxNumber == 0) {
            selected.setText("已选" + selectedNumber + "款");
        } else {
            if (maxNumber == selectedNumber) {
                selected.setText("已选好");
                ((ExpandableLayout)view.findViewById(R.id.expandable_layout)).hide();
                listView.scrollTo(0, 0);
                adapter.setExpandState(p, false);
//                observableScrollView.fullScroll(ObservableScrollView.FOCUS_UP);
            } else {
                selected.setText(selectedNumber == 0 ? "尚未选择" : "已选" + selectedNumber + "款");
            }
        }
        setSum();
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
