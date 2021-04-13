package com.carparking;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Handler;
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
import java.util.Vector;

public class DistanceLogic {
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothSocket mmSocket;
    private static BluetoothDevice mmDevice = null;
    private static OutputStream mmOutputStream;
    private static InputStream mmInputStream;
    private static Thread readThread;

    private static VectorMasterView distance_vector;
    private static ValueAnimator valueAnimator;

    private static volatile boolean stopReadWorker = false;

    private static int mode = 0;
    private static PathModel distance1;
    private static PathModel distance2;
    private static PathModel distance3;
    private static PathModel distance4;

    static void startBT(Handler handler, VectorMasterView distance_vector) throws Exception {
        DistanceLogic.distance_vector = distance_vector;
        distance1 = distance_vector.getPathModelByName("distance1");
        distance2 = distance_vector.getPathModelByName("distance2");
        distance3 = distance_vector.getPathModelByName("distance3");
        distance4 = distance_vector.getPathModelByName("distance4");

        mmDevice = null;
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
                if(deviceName.equals("ESP32test"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        if(mmDevice == null){
            throw new Exception("Kunne ikke finde den korrekte forbindelse til ESP32test!");
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        if(mmSocket.isConnected()){
            mmSocket.close();
        }
        try{
            mmSocket.connect();
        }
        catch(Exception e){
            throw new Exception("Kunne ikke forbinde til bluetooth enhed " + mmDevice.getName());
        }
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        listenForData(handler);
    }

    @SuppressLint("SetTextI18n")
    static void listenForData(Handler handler)
    {
        final byte delimiter = 13;

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
                        String data = new String(packetBytes, StandardCharsets.US_ASCII);

                        String formatted_data = data;

                        int colon = data.indexOf(":");
                        if(colon != -1){
                            formatted_data = data.substring(0, colon);
                        }

                        System.out.println(formatted_data);

                        try{
                            int ndata = Integer.parseInt(formatted_data);
                            if(ndata < 40){
                                mode = 4;
                                distanceVectorUpdater();
                            }else if(ndata < 80){
                                mode = 3;
                                distanceVectorUpdater();
                            }else if(ndata < 120){
                                mode = 2;
                                distanceVectorUpdater();
                            }else{
                                mode = 1;
                                distanceVectorUpdater();
                            }
                        }
                        catch (Exception e){

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


    static void distanceVectorUpdater(){
        distance4.setFillAlpha(1);
        distance3.setFillAlpha(1);
        distance2.setFillAlpha(1);
        distance1.setFillAlpha(1);
        distance1.setFillColor(Color.parseColor("#ff5500"));
        distance2.setFillColor(Color.parseColor("#d4ff00"));
        distance3.setFillColor(Color.parseColor("#91ff00"));
        distance4.setFillColor(Color.parseColor("#40ff00"));

        if(mode == 1){
            distance_vector.update();
        }
        else if(mode == 2){
            distance4.setFillAlpha(0);
        }
        else if(mode == 3){
            distance4.setFillAlpha(0);
            distance3.setFillAlpha(0);
        }
        else if(mode == 4){
            distance4.setFillAlpha(0);
            distance3.setFillAlpha(0);
            distance2.setFillAlpha(0);
        }
        distance_vector.update();
    }
}
