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
import java.util.HashMap;
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

    private static volatile boolean expectOK = false;

    private static volatile String prev_data_read = "";
    private static volatile boolean stopReadWorker = false;
    private static volatile boolean stopWriteWorker = false;
    private static volatile boolean readyToSwitch = true;

    public static VectorMasterView vector;


    static HashMap<String, TextView> textViews = new HashMap<>();
    static void addTextview(String name, TextView textView){
        CanBusLogic.textViews.put(name, textView);
    }

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
                                data = data.replaceAll(">", "");
                                data = data.replaceAll("<", "");
                                readBufferPosition = 0;

                                final String finalData = data;
                                handler.post(() -> {
                                    if(finalData.equals(prev_data_read) && !finalData.equals("OK")) return;

                                    System.out.println(finalData);

                                    if(finalData.equals("OK")){
                                        CanBusLogic.expectOK = false;
                                    }
                                    if(stopWriteWorker) {
                                        int type = 0;
                                        try{
                                            type = Integer.parseInt(finalData.substring(0, 3));
                                        }
                                        catch(Exception e){
                                            System.out.println("Ingen type på -> " + finalData);
                                        }
                                        if (finalData.length() > 10 && type == 346) {
                                            int brakeValue = Integer.parseInt(finalData.substring(11, 12), 16);
                                            CanBusLogic.textViews.get("brake").setText(brakeValue == 2 ? "Ja" : "Nej");
                                        }
                                        if (finalData.length() > 10 && type == 374) {
                                            int battery = Integer.parseInt(finalData.substring(5, 7), 16);
                                            battery = Math.round((((float)battery)/210)*100);
                                            CanBusLogic.textViews.get("battery").setText(battery + "%");
                                            float trimPathEnd = (((battery*0.8f)/100)+0.1f);
                                            PathModel outline = vector.getPathModelByName("outline");
                                            outline.setTrimPathEnd(trimPathEnd);
                                            vector.update();
                                        }
                                        if (finalData.length() > 10 && type == 418) {
                                            int gear = Integer.parseInt(finalData.substring(3, 5), 16);
                                            switch(gear){
                                                case 68:
                                                    CanBusLogic.textViews.get("gear").setText("D");
                                                    break;
                                                case 78:
                                                    CanBusLogic.textViews.get("gear").setText("N");
                                                    break;
                                                case 80:
                                                    CanBusLogic.textViews.get("gear").setText("P");
                                                    break;
                                                case 82:
                                                    CanBusLogic.textViews.get("gear").setText("R");
                                                    break;
                                            }
                                        }
                                        readyToSwitch = true;
                                    }
                                    prev_data_read = finalData;
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

    static void switchCanBusData(int id) {
        if(writeThread != null) {
            writeThread.interrupt();
            writeThread = null;
        }
        CanBusLogic.stopWriteWorker = false;
        writeThread = new Thread(() -> {
            try {
                CanBusLogic.runCommand("atsp6");
                CanBusLogic.runCommand("ate0");
                CanBusLogic.runCommand("ath1"); //ath1
                CanBusLogic.runCommand("atcra " + id);
                CanBusLogic.runCommand("atcaf0");
                CanBusLogic.runCommand("atS0");
                CanBusLogic.sendData("atma");
                CanBusLogic.stopWriteWorker = true;
            }
            catch(Exception e){
                e.printStackTrace();
            }
        });
        writeThread.start();
    }

    static void runCommand(String command) throws IOException, InterruptedException {
        CanBusLogic.expectOK = true;
        CanBusLogic.sendData(command);

        //setTimeout(() -> CanBusLogic.expectOK = false, 1000);

        int time = 0;
        while(CanBusLogic.expectOK){
            if(time > 200){
                System.out.println("Forcer 'expectOK' til false!");
                CanBusLogic.expectOK = false;
            }
            Thread.sleep(5);
            time++;
        }
    }

    static void setReadyToSwitch(boolean readyToSwitch){
        CanBusLogic.readyToSwitch = readyToSwitch;
    }

    static boolean isReadyToSwitch(){
        return stopWriteWorker && readyToSwitch;
    }

    static void sendData(String msg) throws IOException
    {
        msg += "\r";
        mmOutputStream.write(msg.getBytes());
    }

    static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            }
            catch (Exception e){
                System.err.println(e);
            }
        }).start();
    }
}
