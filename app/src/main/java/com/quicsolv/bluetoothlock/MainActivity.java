package com.quicsolv.bluetoothlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {
    LottieAnimationView animationView;
    BluetoothManager bluetoothManager;
    BluetoothAdapter adapter;
    BluetoothDevice ourLock=null;
    BluetoothGatt mBluetoothGatt;
    boolean buttonPressed = false;
    TextView status;
    boolean bluetoothPermissions = false, locationPermissions = false;

    String LOCK_MAC_ADDRESS = "FF:FF:30:04:C6:56";
    String LOCK_KEY = "41375902884721600622694758343588";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);
        status = findViewById(R.id.connectedStatus);
        animationView.setOnClickListener(touchAnimationListener);
        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter();
        checkPermissions();

    }

    private void checkPermissions()
    {
        if(bluetoothManager == null || !adapter.isEnabled())
        {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i, 0);
        }else{
            bluetoothPermissions = true;
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            locationPermissions = true;
        }
        if(locationPermissions && bluetoothPermissions){

            adapter.startLeScan(mLeScanCallback);
            animationView.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0){
            if (resultCode == RESULT_OK){
                checkPermissions();
            }
            if(resultCode == RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "Error occurred while enabling Bluetooth. Leaving the application.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }//onActivityResult

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            Log.d("LE SCAN", bluetoothDevice.getAddress());
            if(bluetoothDevice.getAddress().equals(LOCK_MAC_ADDRESS)){
                status.setText("Lock Discovered");
                ourLock = bluetoothDevice;
                //adapter.stopLeScan(mLeScanCallback);
                if(buttonPressed)
                    mBluetoothGatt = ourLock.connectGatt(MainActivity.this,false,mGattCallback);
            }
        }

    };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED)
            {
                Log.d("LE DEVICES", "Connected. Discovering Services");
                mBluetoothGatt.discoverServices();
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                ourLock = null;
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
                BluetoothGattService service = gatt.getService(UUID.fromString("0000fee7-0000-1000-8000-00805f9b34fb"));
                //Log.d("SERVICE :",blgs.getUuid().toString());
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("000036f5-0000-1000-8000-00805f9b34fb"));
                Log.d("DESCRIPTOR COUNT",String.valueOf(characteristic.getDescriptors().size()));
                //Log.d("CHARACTERISTIC EXISTS",String.valueOf(characteristic!=null));
                //
                //setCharacteristicNotification(gatt,characteristic,true);
                //characteristic.getDescriptors()
                byte payload[] = {(byte)0xfc,(byte)0xa4,(byte)0x06,(byte)0x6b,(byte)0x50,(byte)0xa8,(byte)0x68,(byte)0xa4,(byte)0x10,(byte)0xbb,(byte)0x15,(byte)0x96,(byte)0xae,(byte)0x3c,(byte)0x48,(byte)0x43};
                //descriptor.setValue(payload);
                //byte encrypted[] = encrypt_data(new String(payload));
                characteristic.setValue(payload);
                boolean check = gatt.writeCharacteristic(characteristic);
                if(check)
                {
                    Log.d("DEVICE","UNLOCKED!!");
                    gatt.disconnect();
                }
                Log.d("WRITE TO CHARACTERISTIC",String.valueOf(check));

        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            byte[] notification = characteristic.getValue();
            Log.d("GOT NOTIFICATION --->", new String(notification));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            byte[] notification = characteristic.getValue();
            Log.d("GOT NOTIFICATION --->", new String(notification));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] notification = characteristic.getValue();
            Log.d("GOT NOTIFICATION --->", new String(notification));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            byte[] notification = descriptor.getValue();
            Log.d("GOT  DESC READ", new String(notification));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            byte[] notification = descriptor.getValue();
            Log.d("GOT  DESC WRITE", new String(notification));
        }
    };

    private boolean setCharacteristicNotification(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean enable)
    {
        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic,enable);
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptors().get(0);
        //System.out.println("Descriptor data --->"+descriptor.getValue());
        //descriptor.setValue(descriptor.getValue()+1);
        byte payload[] = {(byte)0xfc,(byte)0xa4,(byte)0x06,(byte)0x6b,(byte)0x50,(byte)0xa8,(byte)0x68,(byte)0xa4,(byte)0x10,(byte)0xbb,(byte)0x15,(byte)0x96,(byte)0xae,(byte)0x3c,(byte)0x48,(byte)0x43};
        descriptor.setValue(payload);
        Log.d("WRITE TO DESCRIPTOR? ",bluetoothGattCharacteristic.getDescriptors().get(0).getUuid().toString()+" "+bluetoothGatt.writeDescriptor(descriptor));
        return bluetoothGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
    }


    private View.OnClickListener touchAnimationListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ourLock != null) {
                animationView.playAnimation();
                buttonPressed = true;
                if (buttonPressed) {
                    mBluetoothGatt = ourLock.connectGatt(MainActivity.this, false, mGattCallback);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationView.setProgress(0.0f);
                        buttonPressed = false;
                        ourLock = null;
                        //adapter.stopLeScan(mLeScanCallback);
                    }
                }, 3000);
            }else{
                Toast.makeText(MainActivity.this,"Please make sure the light on the lock is blinking",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private byte[] encrypt_data(String data)
    {
        byte[] cipherText = {0x00};
        while(data.length()<16) {
            data = data + "0";
        }
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(LOCK_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            System.out.println("data -->" + data);
            cipherText = cipher.doFinal(data.getBytes());
            //System.out.println("Base64 encoded: "+ Base64.encode(data.getBytes()).length);

            //byte[] original = cipher.doFinal(Base64.encode(data.getBytes()));
            System.out.println("Cipher Text ----> " + cipherText);
            return cipherText;
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return cipherText;
    }
}
