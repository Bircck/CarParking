package com.carparking;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class CanBusLogic {
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mmSocket;
    private static BluetoothDevice mmDevice;
    private static OutputStream mmOutputStream;
    private static InputStream mmInputStream;
    private static Thread readThread, writeThread;
    private static byte[] readBuffer;
    private static int readBufferPosition;

    private static volatile String prev_data_read = "";
    private static volatile boolean stopReadWorker = false;
    private static volatile boolean stopWriteWorker = false;

    private static int id = 0;
    private static TextView textView;
    private static VectorMasterView vector;

    static void startBT(Handler handler) throws Exception {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            throw new Exception("Ingen bluetooth enhed fundet!");
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                String deviceName = device.getName();
                if(deviceName.equals("OBDLink MX"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        listenForData(handler);
    }

    @SuppressLint("SetTextI18n")
    static void listenForData(Handler handler)
    {
        final byte delimiter = 13;

        readBufferPosition = 0;
        readBuffer = new byte[1024];
        readThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted() && !stopReadWorker)
            {
                try
                {
                    int bytesAvailable = mmInputStream.available();
                    if(bytesAvailable > 0)
                    {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mmInputStream.read(packetBytes);
                        for(int i=0;i<bytesAvailable;i++)
                        {
                            byte b = packetBytes[i];
                            if(b == delimiter)
                            {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                String data = new String(encodedBytes, StandardCharsets.US_ASCII);
                                int colon = data.indexOf(":");
                                if(colon != -1){
                                    data = data.substring(colon+1);
                                }
                                data = data.replaceAll(" ", "");
                                readBufferPosition = 0;

                                final String finalData = data;
                                handler.post(() -> {
                                    if(finalData.equals(prev_data_read)) return;
                                    prev_data_read = finalData;

                                    System.out.println(finalData);
                                    if(stopWriteWorker) {
                                        if (CanBusLogic.id == 346 && finalData.length() > 10) {
                                            try{
                                                int brakeValue = Integer.parseInt(finalData.substring(5, 7), 16);
                                                if (brakeValue == 2) {
                                                    CanBusLogic.textView.setText("Ja");
                                                } else {
                                                    CanBusLogic.textView.setText("Nej");
                                                }
                                            } catch (Exception e){
                                                System.out.println("Kunne ikke passes til int: " + finalData.substring(8, 9));
                                            }
                                        }
                                        if (CanBusLogic.id == 374 && finalData.length() > 10) {
                                            int battery = Integer.parseInt(finalData.substring(2, 4), 16) - 110;
                                            CanBusLogic.textView.setText(battery + "%");
                                            float trimPathEnd = (((battery*0.8f)/100)+0.1f);
                                            PathModel outline = vector.getPathModelByName("outline");
                                            outline.setTrimPathEnd(trimPathEnd);
                                            vector.update();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                }
                catch (IOException ex)
                {
                    stopReadWorker = true;
                }
            }
        });

        readThread.start();
    }

    static void initCanBusData() throws IOException {
        CanBusLogic.stopWriteWorker = false;
        writeThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted() && !stopWriteWorker)
            {
                try {
                    CanBusLogic.sendData("atsp6");
                    Thread.sleep(1000);
                    CanBusLogic.sendData("ate0");
                    Thread.sleep(1000);
                    CanBusLogic.sendData("atcaf0");
                    Thread.sleep(1000);
                    CanBusLogic.sendData("atS0");
                    stopWriteWorker = true;
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        writeThread.start();
    }

    static void switchCanBusData(int id, TextView textView, VectorMasterView vector) throws IOException {
        CanBusLogic.id = id;
        CanBusLogic.textView = textView;
        CanBusLogic.vector = vector;
        CanBusLogic.stopWriteWorker = false;
        if(writeThread != null) {
            writeThread.interrupt();
            writeThread = null;
        }
        writeThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted() && !CanBusLogic.stopWriteWorker)
            {
                try {
                    CanBusLogic.sendData("atcra " + id);
                    Thread.sleep(1000);
                    CanBusLogic.sendData("atma");
                    CanBusLogic.stopWriteWorker = true;
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        writeThread.start();
    }

    static boolean isSwitchReady(){
        return stopWriteWorker;
    }

    static void sendData(String msg) throws IOException
    {
        msg += "\r";
        mmOutputStream.write(msg.getBytes());
    }
}
