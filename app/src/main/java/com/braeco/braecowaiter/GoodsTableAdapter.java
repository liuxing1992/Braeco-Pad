package com.braeco.braecowaiter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.codecrafters.tableview.TableColumnModel;
import de.codecrafters.tableview.TableDataAdapter;

/**
 * Created by Weiping on 2015/12/13.
 */
public class GoodsTableAdapter extends TableDataAdapter<String[]> {

    private final int TEXT_SIZE = 14;

    public GoodsTableAdapter(Context context, String[][] data) {
        super(context, data);
    }

    public GoodsTableAdapter(Context context, List<String[]> data) {
        super(context, data);
    }

    protected GoodsTableAdapter(Context context, int columnCount, List<String[]> data) {
        super(context, columnCount, data);
    }

    protected GoodsTableAdapter(Context context, TableColumnModel columnModel, List<String[]> data) {
        super(context, columnModel, data);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        String[] rowData = getRowData(rowIndex);
        TextView textView = new TextView(getContext());
        switch (columnIndex) {
            case 0:
                textView.setText(rowData[columnIndex]);
                textView.setPadding(20, 10, 20, 10);
                textView.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
                textView.setTextSize(TEXT_SIZE);
                break;
            case 1:
                textView.setText(rowData[columnIndex]);
                textView.setPadding(20, 10, 20, 10);
                textView.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
                textView.setTextSize(TEXT_SIZE);
                break;
            case 2:
                textView.setText(rowData[columnIndex]);
                textView.setPadding(20, 10, 20, 10);
                textView.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.price_color));
                textView.setTextSize(TEXT_SIZE);
                break;
        }
        return textView;
    }
}
