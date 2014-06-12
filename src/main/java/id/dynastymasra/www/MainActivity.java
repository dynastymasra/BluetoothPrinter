package id.dynastymasra.www;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.os.Handler;

/**
 * Author   : Dynastymasra
 * Name     : Dimas Ragil T
 * Email    : dynastymasra@gmail.com
 * LinkedIn : http://www.linkedin.com/in/dynastymasra
 * Blogspot : dynastymasra.wordpress.com | dynastymasra.blogspot.com
 */

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private EditText textPrint;
    private Button print, bluetoothOpen, blueToothClose;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread thread;
    private byte[] readBuffer;
    private int readBufferPos;
    private boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textPrint = (EditText) findViewById(R.id.input_print);
        bluetoothOpen = (Button) findViewById(R.id.open);
        blueToothClose = (Button) findViewById(R.id.close);
        print = (Button) findViewById(R.id.print);

        actionBarStatus(0);

        bluetoothOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findBluetooth();
                try {
                    openBluetooth();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        blueToothClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    closeBluetooth();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void actionBarStatus(Integer status) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("Bluetooth Printer");
        if (status == 0) {
            actionBar.setSubtitle("No Bluetooth Connection");
        } else if (status == 1) {
            actionBar.setSubtitle("Bluetooth Device Found!");
        } else if (status == 2) {
            actionBar.setSubtitle("Bluetooth Opened!");
        } else if (status == 3) {
            actionBar.setSubtitle("Data Sent!");
        } else if (status == 4) {
            actionBar.setSubtitle("Bluetooth Closed!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void findBluetooth() {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(getApplicationContext(), "No Bluetooth Adapter Available!", Toast.LENGTH_SHORT).show();
            }
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 0);
            }

            Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
            if (deviceSet.size() > 0) {
                for (BluetoothDevice device:deviceSet) {
                    if (device.getName().equals("P25_061146_01")) {
                        bluetoothDevice = device;
                        break;
                    }
                }
            }

            actionBarStatus(1);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Bluetooth Connection Failed!" + ex, Toast.LENGTH_SHORT).show();
        }
    }

    public void openBluetooth() throws IOException {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

//            listenFromData();
            actionBarStatus(2);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Open Bluetooth" + ex, Toast.LENGTH_SHORT).show();
        }
    }

    private void listenFromData() {
        try {
            final Handler handler = new Handler();
            final byte delimiter = 10;
            stopWorker = false;
            readBufferPos = 0;
            readBuffer = new byte[1024];

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = inputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPos];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPos = 0;

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Data" + data, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPos++] = b;
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            stopWorker = true;
                        }
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendData() throws IOException {
        PrinterCommandTranslator translator = new PrinterCommandTranslator();
        try {
            String msg = textPrint.getText().toString();
            print(translator.toNormalRepeatTillEnd('-'));
            print(translator.toNormalCenterAll("Example"));
            print(translator.toNormalRepeatTillEnd('-'));
            print(translator.toNormalLeft("Test : " + msg));
            print(translator.toNormalTwoColumn2(12345, "C2"));
            print(translator.toMiniLeft("TEST"));
            print(translator.toNormalTwoColumn("C1", 12345));

            actionBarStatus(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void closeBluetooth() throws IOException {
        try {
            stopWorker = true;
            outputStream.close();
            inputStream.close();
            bluetoothSocket.close();
            actionBarStatus(4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void print(byte[] cmd) {
        try {
            outputStream.write(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
