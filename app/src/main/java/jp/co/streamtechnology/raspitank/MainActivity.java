package jp.co.streamtechnology.raspitank;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private BluetoothAdapter BtAdapter;
    private BluetoothDevice BtDev;
    private BluetoothSocket BtSock;
    private InputStream BtInStream;
    private OutputStream BtOutStream;
    Button ButtonStartBt, ButtonStopBt;
    TextView TextSpeed;
    RadioButton RadioButtonDigital,RadioButtonAnalog;
    ImageView STLogo;

    boolean IsBtConnect;
    boolean BtSending;

    float Velocity;
    ArrayList AccGraphData;

    HorizontalBarChart AccChart;

    private SensorManager SM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        double inchX = displayMetrics.widthPixels / displayMetrics.xdpi;
        double inchY = displayMetrics.heightPixels / displayMetrics.ydpi;
        double inch = Math.sqrt((inchX * inchX) + (inchY * inchY));
        Log.d("RasPiTank","Display size: Size:" + inch + " inch, Width: " + inchX + " inch, Height: " + inchY + " inch");

        // NEXUS7をベースにする。
        // Width: 6インチ, Height: 3.4インチ
        float ScaleFactor = (float) (inchX / 6.0);

        float DefaultLargeText = 15;
        float DefaultMidiumText = 12;
        float DefaultSmallText = 10;
        TextView ModeText = (TextView) findViewById(R.id.textView_mode);
        ModeText.setTextSize(DefaultLargeText*ScaleFactor);
        TextView SpeedTitleText = (TextView)findViewById(R.id.textView_speed_title);
        SpeedTitleText.setTextSize(DefaultLargeText*ScaleFactor);

        RadioButtonDigital = (RadioButton)findViewById(R.id.radioButton_digital);
        RadioButtonDigital.setTextSize(DefaultMidiumText*ScaleFactor);
        RadioButtonDigital.setScaleX(ScaleFactor);
        RadioButtonDigital.setScaleY(ScaleFactor);
        RadioButtonAnalog = (RadioButton)findViewById(R.id.radioButton_analog);
        RadioButtonAnalog.setTextSize(DefaultMidiumText*ScaleFactor);
        RadioButtonAnalog.setScaleX(ScaleFactor);
        RadioButtonAnalog.setScaleY(ScaleFactor);

        ButtonStartBt = (Button)findViewById(R.id.button_start_bt);
        ButtonStartBt.setTextSize(DefaultMidiumText*ScaleFactor);
        ButtonStartBt.setScaleX(ScaleFactor);
        ButtonStartBt.setScaleY(ScaleFactor);
        ButtonStopBt = (Button)findViewById(R.id.button_stop_bt);
        ButtonStopBt.setTextSize(DefaultMidiumText*ScaleFactor);
        ButtonStopBt.setScaleX(ScaleFactor);
        ButtonStopBt.setScaleY(ScaleFactor);

        ImageButton ButtonLeftForward, ButtonLeftStop, ButtonLeftBack;
        ImageButton ButtonRightForward, ButtonRightStop, ButtonRightBack;
        ButtonLeftForward = (ImageButton)findViewById(R.id.imageButton_left_forward);
        ButtonLeftForward.setScaleX(ScaleFactor);
        ButtonLeftForward.setScaleY(ScaleFactor);
        ButtonLeftStop = (ImageButton)findViewById(R.id.imageButton_left_stop);
        ButtonLeftStop.setScaleX(ScaleFactor);
        ButtonLeftStop.setScaleY(ScaleFactor);
        ButtonLeftBack = (ImageButton)findViewById(R.id.imageButton_left_back);
        ButtonLeftBack.setScaleX(ScaleFactor);
        ButtonLeftBack.setScaleY(ScaleFactor);
        ButtonRightForward = (ImageButton)findViewById(R.id.imageButton_right_forward);
        ButtonRightForward.setScaleX(ScaleFactor);
        ButtonRightForward.setScaleY(ScaleFactor);
        ButtonRightStop = (ImageButton)findViewById(R.id.imageButton_right_stop);
        ButtonRightStop.setScaleX(ScaleFactor);
        ButtonRightStop.setScaleY(ScaleFactor);
        ButtonRightBack = (ImageButton)findViewById(R.id.imageButton_right_back);
        ButtonRightBack.setScaleX(ScaleFactor);
        ButtonRightBack.setScaleY(ScaleFactor);

        TextSpeed = (TextView)findViewById(R.id.textView_speed);
        TextSpeed.setTextSize(DefaultMidiumText*ScaleFactor);

        TextView TextPoweredBy = (TextView)findViewById(R.id.textView_poweredby);
        TextPoweredBy.setTextSize(DefaultSmallText*ScaleFactor);
        STLogo = (ImageView)findViewById(R.id.imageView_stlogo);
        STLogo.setScaleX(ScaleFactor/2);
        STLogo.setScaleY(ScaleFactor/2);

        ButtonLeftForward.setOnClickListener(this);
        ButtonLeftStop.setOnClickListener(this);
        ButtonLeftBack.setOnClickListener(this);

        ButtonRightForward.setOnClickListener(this);
        ButtonRightStop.setOnClickListener(this);
        ButtonRightBack.setOnClickListener(this);

        RadioButtonDigital.setOnClickListener(this);
        RadioButtonAnalog.setOnClickListener(this);

        ButtonStartBt.setOnClickListener(this);
        ButtonStopBt.setOnClickListener(this);

        STLogo.setOnClickListener(this);

        AccChart = (HorizontalBarChart) findViewById(R.id.speed_graph);

        AccGraphData = new ArrayList<>();

        AccGraphData.add(new BarEntry(0,0f, "X"));
        AccGraphData.add(new BarEntry(1,0f, "Y"));
        AccGraphData.add(new BarEntry(2,0f, "Z"));

        BarDataSet dataset = new BarDataSet(AccGraphData,"戦車の加速度");

        dataset.setValueFormatter(new IValueFormatter(){
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return entry.getData().toString();
            }
        });

        ArrayList LabelArray = new ArrayList<String>();
        LabelArray.add("X");
        LabelArray.add("Y");
        LabelArray.add("Z");
        ;

        BarData data = new BarData(dataset);
        AccChart.setData(data);


        YAxis yaxis = AccChart.getAxisLeft();
        yaxis.setAxisMaximum(1.2f);
        yaxis.setAxisMinimum(-1.2f);

        yaxis = AccChart.getAxisRight();
        yaxis.setEnabled(false);

        XAxis xaxis = AccChart.getXAxis();
        xaxis.setEnabled(false);
        xaxis.setDrawLabels(true);

        Description desc = new Description();
        desc.setText("");
        AccChart.setDescription(desc);


        SM = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor SensorType = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SM.registerListener(this, SensorType, SensorManager.SENSOR_DELAY_UI);
    }

    private void ShowToast(CharSequence Message){

        Toast.makeText(this, Message, Toast.LENGTH_SHORT).show();

    }

    private String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    float AccX, AccY, AccZ;

    @Override
    public void onClick(View v) {



        String DigitalCommand = "";

        switch(v.getId()) {
            case R.id.button_start_bt:

                this.ButtonStopBt.setEnabled(false);
                this.ButtonStartBt.setEnabled(false);

                Set<BluetoothDevice> pairedDevices = null;
                try {
                    BtAdapter = BluetoothAdapter.getDefaultAdapter();
                    pairedDevices = BtAdapter.getBondedDevices();
                }
                catch (Exception ex){
                    ShowToast("Bluetoothが有効になっていません。:" + ex.getMessage());
                    this.ButtonStopBt.setEnabled(true);
                    this.ButtonStartBt.setEnabled(false);
                    return;
                }
                this.ShowToast("RasPiTankに接続しています。");
                this.BtDev = null;
                for (BluetoothDevice dev : pairedDevices) {
                    if (dev.getName().contains("RasPiTank")) {
                        this.BtDev = dev;
                        break;
                    }
                }

                if(this.BtDev == null){

                    ShowToast("エラー：　RasPiTankがペアリングされていません。");
                    this.ButtonStopBt.setEnabled(false);
                    this.ButtonStartBt.setEnabled(true);
                }

                try {
                    this.BtSock = this.BtDev.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                    this.BtSock.connect();
                    this.BtInStream = this.BtSock.getInputStream();
                    this.BtOutStream = this.BtSock.getOutputStream();
                    this.ButtonStartBt.setEnabled(false);
                    this.ButtonStopBt.setEnabled(true);
                    this.ShowToast("接続しました。");
                    this.IsBtConnect = true;


                    final Handler Updatehandle;

                    Updatehandle = new Handler(Looper.getMainLooper());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int msg_index = 0;
                            String SensorDataStr = "";
                            String [] SensorValStr;

                            Log.d("RasPiTanl", "start sensor thread");
                            while(IsBtConnect) {
                                int c = -1;

                                try {
                                    c = BtInStream.read();
                                }
                                catch(IOException e){
                                    e.printStackTrace();
                                    Updatehandle.post(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              ButtonStartBt.setEnabled(true);
                                                              ButtonStopBt.setEnabled(false);
                                                              ShowToast("RasPiTankから切断されました。");
                                                          }
                                                      });

                                    IsBtConnect = false;
                                    break;
                                }
                                if(c == -1) continue;


                                if((char)c == '$'){
                                    SensorDataStr = "";
    //                                Log.d("RasPiTank-2","$ comes.");
                                    continue;
                                }
                                if(c=='\n'){

//                                    Log.d("RasPiTank-2",SensorDataStr);
                                    int i;
                                    String checksum_recved_str;
                                    int checksum_recved, checksum = '$';
                                    for(i=0;i<SensorDataStr.length();i++){
                                        if(SensorDataStr.charAt(i)=='#') break;
                                        checksum += SensorDataStr.charAt(i);
                                    }
                                    if(i == SensorDataStr.length()) continue;
                                    if((i+2) >= SensorDataStr.length()) continue;
                                    checksum += SensorDataStr.charAt(i);
                                    i++;
                                    checksum_recved_str = SensorDataStr.substring(i, i+2);
                                    checksum_recved = Integer.parseInt(checksum_recved_str,16);
  //                                  Log.d("RasPiTank", "CheckSum:" + String.format("%2X",checksum) + ", RecvChecksum:" + checksum_recved_str);

                                    if(((checksum + checksum_recved)&0xFF) != 0) continue;

                                    SensorValStr = SensorDataStr.split(":");
                                    String Command = SensorValStr[0];
                                    SensorValStr = SensorValStr[1].split("#");
                                    SensorValStr = SensorValStr[0].split(",");

                                    AccX = Float.parseFloat(SensorValStr[0]);
                                    AccY = Float.parseFloat(SensorValStr[1]);
                                    AccZ = Float.parseFloat(SensorValStr[2]);

                                    Updatehandle.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            Velocity = (float)((int)(Velocity*10))/10;
                                            Velocity += AccY*9.80665*0.1;

                                            TextSpeed.setText(String.format("%.1f km/h", Math.abs((Velocity/1000)*60*60)));


                                            AccGraphData.clear();
                                            AccGraphData.add(new BarEntry(0, AccX, "X"));
                                            AccGraphData.add(new BarEntry(1, AccY, "Y"));
                                            AccGraphData.add(new BarEntry(2, AccZ, "Z"));

                                            BarDataSet dataset = new BarDataSet(AccGraphData,"戦車の加速度");

                                            dataset.setValueFormatter(new IValueFormatter(){
                                                @Override
                                                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                                                    return entry.getData().toString();
                                                }
                                            });

                                            BarData data = new BarData(dataset);
                                            AccChart.setData(data);
                                            AccChart.invalidate();

                                        }
                                    });
                                }
                                else{
                                    SensorDataStr += ((char)c);
//                                    Log.d("RasPiTank",SensorDataStr + "(" + SensorDataStr.length() + ")");
                                }
                            }
                        }
                    }).start();

                }
                catch(Exception ex) {

                    this.ButtonStartBt.setEnabled(true);
                    this.ButtonStopBt.setEnabled(false);
                    ShowToast("RasPiTankに接続できません。");

                }

                break;
            case R.id.button_stop_bt:
                try {
                    this.BtInStream.close();
                    this.BtOutStream.close();

                    this.BtSock.close();

                    this.ButtonStartBt.setEnabled(true);
                    this.ButtonStopBt.setEnabled(false);

                    this.ShowToast("RasPiTankから切断されました。");

                    this.IsBtConnect = false;
                } catch (Exception ex) {
                }
                finally {
                }
                break;
            case R.id.radioButton_analog:
                this.RadioButtonAnalog.setChecked(true);
                this.RadioButtonDigital.setChecked(false);
                break;
            case R.id.radioButton_digital:
                this.RadioButtonAnalog.setChecked(false);
                this.RadioButtonDigital.setChecked(true);
                break;
            case R.id.imageButton_left_forward:
                DigitalCommand = CreateDigitalCommand(LEFT,FORWARD,ACTIVE);
                break;
            case R.id.imageButton_left_stop:
                DigitalCommand = CreateDigitalCommand(LEFT,FORWARD,STOP);
                Velocity = 0;
                break;
            case R.id.imageButton_left_back:
                DigitalCommand = CreateDigitalCommand(LEFT,BACK,ACTIVE);
                break;
            case R.id.imageButton_right_forward:
                DigitalCommand = CreateDigitalCommand(RIGHT,FORWARD,ACTIVE);
                break;
            case R.id.imageButton_right_stop:
                DigitalCommand = CreateDigitalCommand(RIGHT,FORWARD,STOP);
                Velocity = 0;
                break;
            case R.id.imageButton_right_back:
                DigitalCommand = CreateDigitalCommand(RIGHT,BACK,ACTIVE);
                break;

            case R.id.imageView_stlogo:
                Uri uri = Uri.parse("http://www.streamtechnology.co.jp/");
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(i);
                break;
            default: break;
        }

        if(DigitalCommand != "" && IsBtConnect && !BtSending && !RadioButtonAnalog.isChecked()) {
            try {
                BtSending = true;
                BtOutStream.write(DigitalCommand.getBytes());
            } catch (Exception ex) {
            }
            BtSending = false;
        }
    }

    int FORWARD = 0;
    int BACK = 1;

    int ACTIVE = 0;
    int STOP = 1;

    int RIGHT = 0;
    int LEFT = 1;

    String CreateDigitalCommand(int right_left, int forward_back, int active_stop) {
        String command;

        command = "$DIGITAL:";
        if(right_left==RIGHT) command += "R,";
        if(right_left==LEFT) command += "L,";

        if(forward_back == FORWARD) command += "F,";
        if(forward_back == BACK) command += "B,";

        if(active_stop == ACTIVE) command += "A#";
        if(active_stop == STOP) command += "S#";

        int checksum = 0;
        int i;
        for(i=0;i<command.length();i++){
            checksum += (int)command.charAt(i);
        }
        checksum = (~checksum + 1) & 0xFF;
        command += String.format("%2X\n",checksum);

        Log.d("RasPiTank",command);

        return command;
    }

    float PrevXValf, PrevYValf, PrevZValf;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            String XVal, YVal, ZVal;
            float XValf, YValf, ZValf;

            if(IsBtConnect && !BtSending && RadioButtonAnalog.isChecked()) {

                BtSending = true;

                XValf = event.values[0];
                YValf = event.values[1];
                ZValf = event.values[2];

                if(Math.abs(PrevXValf-XValf) > 0.1 || Math.abs(PrevYValf-YValf) > 0.1 || Math.abs(PrevZValf-ZValf) > 0.1) {
                    XVal = String.format("%1.2f", event.values[0] / 9.80665);
                    YVal = String.format("%1.2f", event.values[1] / 9.80665);
                    ZVal = String.format("%1.2f", event.values[2] / 9.80665);

                    String command = "$ANALOG:" + XVal + "," + YVal + "," + ZVal + "#";

                    int checksum = 0;
                    int i;
                    for (i = 0; i < command.length(); i++) {
                        checksum += (int) command.charAt(i);
                    }
                    checksum = (~checksum + 1) & 0xFF;
                    command += String.format("%2X\n", checksum);

                    //Log.d("RasPiTank",command);
                    try {
                        BtOutStream.write(command.getBytes());
                    } catch (Exception ex) {
                    }

                    if(Float.valueOf(YVal) > -0.3 && Float.valueOf(YVal) < 0.3){
                        if(Float.valueOf(XVal) > 0.3 && Float.valueOf(XVal) < 0.7){
                            Velocity = 0.0f;
                        }
                    }
                }

                BtSending = false;

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            this.BtInStream.close();
            this.BtOutStream.close();

            this.BtSock.close();

        } catch (Exception ex) {
        }
        finally {
        }

    }
}
