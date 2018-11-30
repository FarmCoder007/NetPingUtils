package com.ruoxia.reader.netpingutils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Layout;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


public class NetTestActivity extends FragmentActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private EditText editText;
    private Button button_start;
    private Button button_end;
    private Button button_clear;
    private Button button_copy;
    private TextView textView;
    //    private NetPing netPing;
    private boolean run;
    private int count;
    private LocationManager locationManager;
    private Location location;
    private String provider;
    private final int WHAT_LOCATION = 878;
    private RadioGroup radioGroup;
    private static final int TEST_PING = 0;
    private static final int TEST_DIG = 1;
    private int TEST_MODE = TEST_PING;
    private String result;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_net_test);
        textView = (TextView) findViewById(R.id.textView_ping_log);
        textView.setMovementMethod(new ScrollingMovementMethod());
        editText = (EditText) findViewById(R.id.editText_url);
        button_start = (Button) findViewById(R.id.button_start_ping);
        button_end = (Button) findViewById(R.id.button_end_ping);
        button_clear = (Button) findViewById(R.id.button_clear);
        button_copy = (Button) findViewById(R.id.button_copy);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup_type);

        button_start.setOnClickListener(this);
        button_end.setOnClickListener(this);
        button_clear.setOnClickListener(this);
        button_copy.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);
        editText.setText("www.baidu.com");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        run = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start_ping:
                final String url = editText.getText().toString().trim();
                if (TextUtils.isEmpty(url)) {
                    Toast.makeText(this, "请输入要测试的地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                run = true;
                count = 0;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (run && count < 5) {
                            if (TEST_MODE == TEST_PING) {
                                result = ping(url);
                            } else if (TEST_MODE == TEST_DIG) {
                                result = digTrace(url);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView.append(result + "\n");
                                    Layout layout = textView.getLayout();
                                    if (layout != null) {
                                        int scrollAmount = layout.getLineTop(textView.getLineCount()) - textView.getHeight();
                                        if (scrollAmount > 0) {
                                            textView.scrollTo(0, scrollAmount + textView.getCompoundPaddingBottom());
                                        } else {
                                            textView.scrollTo(0, 0);
                                        }
                                    }
                                }
                            });
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            count++;
                        }

                    }
                }.start();
                break;
            case R.id.button_end_ping:
                run = false;
                break;
            case R.id.button_clear:
                textView.setText("");
                break;
            case R.id.button_copy:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", textView.getText());
                cm.setPrimaryClip(mClipData);
                Toast.makeText(this, "已成功复制到粘贴板", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /**
     * ping测试
     *
     * @param str
     * @return
     */
    public String ping(String str) {
        String result = "";
        Process p;
        StringBuilder buffer = new StringBuilder();
        try {
            //ping -c 3 -w 100  中  ，-c 是指ping的次数 5是指ping 5次 ，-w 10  以秒为单位指定超时间隔，是指超时时间为10秒
            p = Runtime.getRuntime().exec("ping -c 1 -w 10 " + str);
            int status = p.waitFor();

            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));

            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            System.out.println("Return ============" + buffer.toString());

            if (status == 0) {
                result = "success";
            } else {
                result = "failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString() + " result:" + result + "\n\n";
    }

    /**
     * dig 测试
     *
     * @param str
     * @return
     */
    public String digTrace(String str) {
        String result = "";
        Process p;
        StringBuilder buffer = new StringBuilder();
        try {
            p = Runtime.getRuntime().exec("dig " + str + " a +trace");
            int status = p.waitFor();

            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));

            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            System.out.println("Return ============" + buffer.toString());

            if (status == 0) {
                result = "success";
            } else {
                result = "failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString() + " result:" + result + "\n\n";
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioButton_ping:
                TEST_MODE = TEST_PING;
                break;
            case R.id.radioButton_dig:
                TEST_MODE = TEST_DIG;
                break;
            default:
                break;
        }
    }

    private class NetPing extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return ping(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textView.append(s);
            Layout layout = textView.getLayout();
            if (layout != null) {
                int scrollAmount = layout.getLineTop(textView.getLineCount()) - textView.getHeight();
                if (scrollAmount > 0) {
                    textView.scrollTo(0, scrollAmount + textView.getCompoundPaddingBottom());
                } else {
                    textView.scrollTo(0, 0);
                }
            }
        }
    }


}
