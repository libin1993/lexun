package io.cordova.lexuncompany.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.cordova.lexuncompany.R;
import io.cordova.lexuncompany.bean.IDCardBean;
import io.cordova.lexuncompany.units.ViewUnits;


/**
 * Author：Li Bin on 2019/8/16 10:28
 * Description：
 */
public class ScanResultActivity extends AppCompatActivity {
    @BindView(R.id.view_status_bar)
    View viewStatusBar;
    @BindView(R.id.tv_title_name)
    TextView tvTitleName;
    @BindView(R.id.iv_title_back)
    ImageView ivTitleBack;
    @BindView(R.id.ll_title)
    LinearLayout llTitle;
    @BindView(R.id.iv_id_card)
    ImageView ivCard;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.ll_name)
    LinearLayout llName;
    @BindView(R.id.tv_gender)
    TextView tvGender;
    @BindView(R.id.ll_gender)
    LinearLayout llGender;
    @BindView(R.id.tv_nation)
    TextView tvNation;
    @BindView(R.id.ll_nation)
    LinearLayout llNation;
    @BindView(R.id.tv_number)
    TextView tvNumber;
    @BindView(R.id.ll_number)
    LinearLayout llNumber;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.ll_address)
    LinearLayout llAddress;
    @BindView(R.id.tv_police)
    TextView tvPolice;
    @BindView(R.id.ll_police)
    LinearLayout llPolice;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.ll_date)
    LinearLayout llDate;
    @BindView(R.id.btn_scan_cancel)
    Button btnScanCancel;
    @BindView(R.id.btn_scan_confirm)
    Button btnScanConfirm;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        ViewUnits.getInstance().setTitleHeight(viewStatusBar);
        llTitle.setBackgroundResource(R.color.blue_0d8);
        tvTitleName.setText("识别结果");
        IDCardBean idCardBean = (IDCardBean) getIntent().getSerializableExtra("id_card");


        ivCard.setImageURI(Uri.fromFile(new File(idCardBean.getPath())));
        if (idCardBean.getOrientation() == 1) {
            llName.setVisibility(View.VISIBLE);
            llGender.setVisibility(View.VISIBLE);
            llNation.setVisibility(View.VISIBLE);
            llNumber.setVisibility(View.VISIBLE);
            llAddress.setVisibility(View.VISIBLE);

            tvName.setText(idCardBean.getName());
            tvGender.setText(idCardBean.getGender());
            tvNation.setText(idCardBean.getNation());
            tvNumber.setText(idCardBean.getNumber());
            tvAddress.setText(idCardBean.getAddress());
        } else {
            llPolice.setVisibility(View.VISIBLE);
            llDate.setVisibility(View.VISIBLE);
            tvPolice.setText(idCardBean.getPolice());
            tvDate.setText(idCardBean.getDate());
        }

    }


    @OnClick({R.id.iv_title_back, R.id.btn_scan_cancel, R.id.btn_scan_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_title_back:
            case R.id.btn_scan_cancel:
                finish();
                break;
            case R.id.btn_scan_confirm:
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
        }
    }
}
