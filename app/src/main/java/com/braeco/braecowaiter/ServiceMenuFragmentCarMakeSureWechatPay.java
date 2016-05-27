package com.braeco.braecowaiter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braeco.braecowaiter.Model.BraecoAppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ServiceMenuFragmentCarMakeSureWechatPay extends BraecoAppCompatActivity
        implements View.OnClickListener {

    private TextView sum;
    private LinearLayout back;
    private LinearLayout paySuccessfully;
    private LinearLayout payFailed;

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_menu_fragment_car_make_sure_wechat_pay);

        sum = (TextView)findViewById(R.id.sum);
        sum.setText(getIntent().getStringExtra("sum"));

        back = (LinearLayout)findViewById(R.id.back);
        back.setOnClickListener(this);

        paySuccessfully = (LinearLayout)findViewById(R.id.pay_successfully);
        paySuccessfully.setOnClickListener(this);

        payFailed = (LinearLayout)findViewById(R.id.pay_failed);
        payFailed.setOnClickListener(this);

        image = (ImageView)findViewById(R.id.image);
        image.setImageBitmap(generateQRCode(getIntent().getStringExtra("qrcode")));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.pay_successfully:
                BraecoWaiterUtils.decreaseLimit();

                for (int i = BraecoWaiterApplication.orderedMeals.size() - 1; i >= 0; i--)
                    BraecoWaiterApplication.orderedMeals.get(i).clear();
                BraecoWaiterApplication.orderedMealsPair.clear();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("ERROR", "NO_ERROR");
                setResult(RESULT_OK, resultIntent);
                BraecoWaiterApplication.FINISH_ORDER = true;
                BraecoWaiterApplication.JUST_GIVE_ORDER = true;
                finish();
                break;
            case R.id.pay_failed:
                finish();
                break;
        }
    }

    private Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] rawData = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = Color.WHITE;
                if (matrix.get(i, j)) {
                    color = Color.BLACK;
                }
                rawData[i + (j * w)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
        return bitmap;
    }

    private Bitmap generateQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 500, 500);
            return bitMatrix2Bitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
