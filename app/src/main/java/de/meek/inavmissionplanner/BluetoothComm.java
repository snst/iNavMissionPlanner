package de.meek.inavmissionplanner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothComm implements IComm {

    private Handler handler_ = null;
    private BluetoothAdapter btAdapter_ = null;
    private BluetoothSocket btSocket_ = null;
    private boolean connected_ = false;
    private boolean enabled_ = false;
    private int rx_ = 0;
    private int tx_ = 0;

    private OutputStream outStream_ = null;
    private InputStream inStream_ = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothComm() {
    }

    public void setHandler(Handler handler) {
        handler_ = handler;
    }

    @Override
    public int rx() {
        return rx_;
    }

    @Override
    public int tx() {
        return tx_;
    }

    public boolean isConnected() {
        return connected_;
    }

    public void showToast(final String toast) {
        Message m = new Message();
        m.what = 0;
        m.obj = toast;
        if (handler_ != null) {
            handler_.sendMessage(m);
        }
    }


    public void enable() {
        btAdapter_ = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter_ == null) {
            showToast("Bluetooth not available");
            return;
        }

        if (!btAdapter_.isEnabled()) {
            showToast("Enabling Bluetooth");
            btAdapter_.enable();
            return;
        }
        enabled_ = true;
    }

    public boolean connect(String address, int speed) {

        if (!enabled_) {
            enable();
        }

        showToast("Connecting");

        if (btAdapter_.isEnabled()) {
            try {
                getRemoteDevice(address);
                btSocket_.connect();
                connected_ = true;
                showToast("Connected");
            } catch (IOException e) {
                try {
                    btSocket_.close();
                    connected_ = false;
                    showToast("Failed to connect (1)");
                } catch (IOException e2) {
                    showToast("Failed to connect (2)");
                }
            }

            try {
                outStream_ = btSocket_.getOutputStream();
                inStream_ = btSocket_.getInputStream();
            } catch (IOException e) {
                showToast("Stream creation failed");
            }
        }
        return connected_;
    }

    public boolean dataAvailable() {
        boolean ret = false;

        try {
            if (connected_)
                ret = inStream_.available() > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public byte read() {
        byte ret = 0;
        try {
            ret = (byte) inStream_.read();
            rx_++;
        } catch (IOException e) {
            showToast("Read error");
        }
        return ret;
    }

    public void write(byte[] arr) {
        try {
                if (connected_) {
                    outStream_.write(arr);
                    outStream_.flush();
                    tx_ += arr.length;
                }
        } catch (IOException e) {
            close();
            showToast("Write error");
        }
    }

    public void disable() {
        try {
            btAdapter_.disable();
        } catch (Exception e) {
            showToast("Failed to disable BT");
        }
    }

    private void getRemoteDevice(String address) {
        BluetoothDevice device = btAdapter_.getRemoteDevice(address);
        try {
            btSocket_ = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            showToast("RFComm failed");
        }

        if (btAdapter_.isDiscovering()) {
            btAdapter_.cancelDiscovery();
        }
    }

    public void close() {
        if (outStream_ != null) {
            try {
                outStream_.flush();
            } catch (IOException e) {
                showToast("Unable to close socket");
            }
        }

        try {
            if (btSocket_ != null) {
                btSocket_.close();
            }
            showToast("disconnected");

        } catch (Exception e2) {
            showToast("Unable to close socket");
        }
        connected_ = false;
    }
}
