package cn.edu.ouc.flying_spirit_controller;

import android.bluetooth.BluetoothManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.clj.flying_spirit_controller.BleManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RockerView mRockerViewDir;
    private RockerView mRockerViewAccelerator;
    private SeekBar mSbAccelerator;
    private TextView mTvAccelerator;
    private Switch mSBle;
    private BleManager bleManager;
    private RockerState rockerDirState = new RockerState();
    private RockerState rockerAccState = new RockerState();
    private controlRunnable cRunnable = new controlRunnable();
    private Thread controlThread = new Thread(cRunnable);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRockerViewDir = (RockerView) findViewById(R.id.rocker_dir);
        mRockerViewAccelerator = (RockerView) findViewById(R.id.rocker_accelerator);
        mSbAccelerator = (SeekBar) findViewById(R.id.sb_accelerator);
        mTvAccelerator = (TextView) findViewById(R.id.tv_accelerator);
        mSBle=(Switch)findViewById(R.id.s_bluetooth);

        mRockerViewDir.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(RockerView.Direction direction) {
                rockerDirState.direction = direction;
                if (!controlThread.isAlive()) {
                    controlThread.start();
                }
            }

            @Override
            public void onFinish() {

            }
        });
        mRockerViewDir.setOnAngleChangeListener(new RockerView.OnAngleChangeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void angle(double angle) {
                rockerDirState.angle = angle;
            }

            @Override
            public void onFinish() {

            }
        });
        mRockerViewDir.setOnDistanceLevelListener(new RockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                rockerDirState.distance = level;
            }
        });
        mRockerViewAccelerator.setOnShakeListener(RockerView.DirectionMode.DIRECTION_2_VERTICAL, new RockerView.OnShakeListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void direction(RockerView.Direction direction) {
                rockerAccState.direction = direction;
                if (!controlThread.isAlive()) {
                    controlThread.start();
                }
            }

            @Override
            public void onFinish() {

            }
        });
        mRockerViewAccelerator.setOnDistanceLevelListener(new RockerView.OnDistanceLevelListener() {
            @Override
            public void onDistanceLevel(int level) {
                rockerAccState.distance = level;
            }
        });
        mSbAccelerator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mTvAccelerator.setText(String.format(Locale.US, "%d", i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        bleManager=new BleManager(this);
        mSBle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    bleManager.enableBluetooth();
                }else {
                    bleManager.disableBluetooth();
                }
            }
        });
    }

    private class controlRunnable implements Runnable {

        @Override
        public void run() {
            while (rockerDirState.direction != RockerView.Direction.DIRECTION_CENTER ||
                    rockerAccState.direction != RockerView.Direction.DIRECTION_CENTER) {
                switch (rockerAccState.direction) {
                    case DIRECTION_UP:
                        mSbAccelerator.setProgress(mSbAccelerator.getProgress() + rockerAccState.distance);
                        break;
                    case DIRECTION_DOWN:
                        mSbAccelerator.setProgress(mSbAccelerator.getProgress() - rockerAccState.distance);
                        break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class RockerState {
    RockerView.Direction direction;
    double angle;
    int distance;

    RockerState() {
        direction = RockerView.Direction.DIRECTION_CENTER;
        angle = 0;
        distance = 0;
    }
}

