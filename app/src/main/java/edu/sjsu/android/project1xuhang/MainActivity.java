package edu.sjsu.android.project1xuhang;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText etPrincipal;
    private SeekBar sbRate;
    private TextView tvRateValue, tvResult;
    private RadioGroup rgYears;
    private CheckBox cbTaxes;
    private Button btnCalc, btnUninstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etPrincipal = findViewById(R.id.etPrincipal);
        sbRate = findViewById(R.id.sbRate);
        tvRateValue = findViewById(R.id.tvRateValue);
        rgYears = findViewById(R.id.rgYears);
        cbTaxes = findViewById(R.id.cbTaxes);
        btnCalc = findViewById(R.id.btnCalc);
        btnUninstall = findViewById(R.id.btnUninstall);
        tvResult      = findViewById(R.id.tvResult);

        sbRate.setMax(200);
        sbRate.setProgress(100);
        tvRateValue.setText("10.0%");

        sbRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double rate = progress / 10.0;
                tvRateValue.setText(String.format("%.1f%%", rate));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        btnCalc.setOnClickListener(v -> calculateMortgage());
        btnUninstall.setOnClickListener(v -> uninstallApp());
    }
    private void calculateMortgage() {
        String principalStr=etPrincipal.getText().toString().trim();
        if(principalStr.isEmpty()){
            Toast.makeText(this,"Please enter principal amount",Toast.LENGTH_SHORT).show();
            return;
        }
        double principal=Double.parseDouble(principalStr);
        double annualRate=sbRate.getProgress()/10.0;
        double monthlyRate=annualRate/100/12;

        int checkedId=rgYears.getCheckedRadioButtonId();
        if(checkedId==-1){
            Toast.makeText(this, "Please select loan term", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedButton=findViewById(checkedId);
        int years=Integer.parseInt(selectedButton.getText().toString().split(" ")[0]);
        int months=years*12;

        if(cbTaxes.isChecked()){
            principal*=1.001;
        }
        double monthlyPayment;
        if(monthlyRate>0) {
            monthlyPayment = principal * (monthlyRate * Math.pow(1 + monthlyRate, months)) / (Math.pow(1 + monthlyRate, months) - 1);
        }else{
            monthlyPayment=principal/months;
        }

        String result = String.format("Monthly Payment: $%.2f", monthlyPayment);
        tvResult.setText(result);


    }
    private void uninstallApp() {
        Uri pkg = Uri.parse("package:" + getPackageName());
        Intent settings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkg);
        startActivity(settings);
    }


}
