package cn.edu.ouc.flying_spirit_controller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.conn.BleCharacterCallback;
import com.clj.fastble.conn.BleGattCallback;
import com.clj.fastble.data.ScanResult;
import com.clj.fastble.exception.BleException;
import com.clj.flying_spirit_controller.utils.HexUtil;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RockerView mRockerViewDir;
    private RockerView mRockerViewAccelerator;
    private SeekBar mSbAccelerator;
    private TextView mTvAccelerator;
    private Switch mSBle;
    private ImageView mIvLoading;
    private Animation loadingAnim;
    private ProgressDialog connectingDialog;

    private RockerState rockerDirState = new RockerState();
    private RockerState rockerAccState = new RockerState();
    private controlRunnable cRunnable = new controlRunnable();
    private receiveRunnable rRunnable = new receiveRunnable();
    private Thread controlThread = new Thread(cRunnable);
    private Thread receiveThread = new Thread(rRunnable);

    private String connectName = "flying_spirit";
    private String UUID_SERVICE = "00000000-0000-0000-8000-00805f9b0000";
    private String UUID_READ = "00000000-0000-0000-8000-00805f9b0000";
    private BleManager myBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region findViewById
        mRockerViewDir = (RockerView) findViewById(R.id.rocker_dir);
        mRockerViewAccelerator = (RockerView) findViewById(R.id.rocker_accelerator);
        mSbAccelerator = (SeekBar) findViewById(R.id.sb_accelerator);
        mTvAccelerator = (TextView) findViewById(R.id.tv_accelerator);
        mSBle = (Switch) findViewById(R.id.s_bluetooth);
        mIvLoading = (ImageView) findViewById(R.id.iv_loading);
        //endregion

        myBleManager = new BleManager(this);

        loadingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        loadingAnim.setInterpolator(new LinearInterpolator());

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
        mSBle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (myBleManager.isSupportBle()) {
                        if (!myBleManager.isBlueEnable())
                            myBleManager.enableBluetooth();
                        mIvLoading.startAnimation(loadingAnim);
                        myBleManager.scanNameAndConnect(connectName, 5000, true, new BleGattCallback() {
                            @Override
                            public void onNotFoundDevice() {
                                mIvLoading.clearAnimation();
                                Toast.makeText(MainActivity.this, "No Device Found", Toast.LENGTH_SHORT).show();
                                mSBle.setChecked(false);
                            }

                            @Override
                            public void onFoundDevice(ScanResult scanResult) {
                                mIvLoading.clearAnimation();
                                connectingDialog = ProgressDialog.show(MainActivity.this, "Connecting", "Connecting to" + connectName);
                            }

                            @Override
                            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                                connectingDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Connect Success", Toast.LENGTH_SHORT).show();
                                if(!receiveThread.isAlive()){
                                    receiveThread.start();
                                }
                            }

                            @Override
                            public void onConnectFailure(BleException exception) {
                                connectingDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Connect Failed", Toast.LENGTH_SHORT).show();
                                mSBle.setChecked(false);
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "Device Not Supported", Toast.LENGTH_SHORT).show();
                        mSBle.setChecked(false);
                    }
                } else {
                    if (myBleManager.isBlueEnable()) {
                        myBleManager.closeBluetoothGatt();
                        myBleManager.disableBluetooth();
                    }
                }
            }
        });
    }

    private class controlRunnable implements Runnable {

        @Override
        public void run() {
            while (rockerDirState.direction != RockerView.Direction.DIRECTION_CENTER ||
                    rockerAccState.direction != RockerView.Direction.DIRECTION_CENTER) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (rockerAccState.direction) {
                            case DIRECTION_UP:
                                mSbAccelerator.setProgress(mSbAccelerator.getProgress() + rockerAccState.distance);
                                break;
                            case DIRECTION_DOWN:
                                mSbAccelerator.setProgress(mSbAccelerator.getProgress() - rockerAccState.distance);
                                break;
                        }
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class receiveRunnable implements Runnable{
        @Override
        public void run() {
            myBleManager.readDevice(UUID_SERVICE, UUID_READ, new BleCharacterCallback() {
                @Override
                public void onSuccess(BluetoothGattCharacteristic characteristic) {
                    Log.d("receiveData",String.valueOf(HexUtil.encodeHex(characteristic.getValue())));
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
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


