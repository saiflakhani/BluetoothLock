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
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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
import com.quicsolv.bluetoothlock.pojo.LockProperties;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    TextView status,lockDetails;
    boolean bluetoothPermissions = false, locationPermissions = false;

    String LOCK_MAC_ADDRESS = "FF:FF:30:04:C6:56";
    String LOCK_KEY = "41375902884721600622694758343588";
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        animationView = findViewById(R.id.animation_view);
        animationView.setVisibility(View.GONE);
        status = findViewById(R.id.connectedStatus);
        animationView.setOnClickListener(touchAnimationListener);
        lockDetails = findViewById(R.id.tVLockDetails);
        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter();
        getIntentIfAvailable();
        LOCK_KEY = convert_key_to_hex(LOCK_KEY);
        checkPermissions();

    }

    private void getIntentIfAvailable()
    {
        try {
            LockProperties currentLock = (LockProperties) getIntent().getSerializableExtra("LockProperties");
            if (currentLock != null) {
                LOCK_MAC_ADDRESS = currentLock.getMac();
                String lockKeyVariables[] = currentLock.getLockKey().split(",");
                String preProcessedKey = "";
                for (String s:lockKeyVariables)
                {
                    if(s.length()==1)s = "0"+s;
                    preProcessedKey+=s;
                }
                LOCK_KEY = preProcessedKey;
                Log.d("LOCK RETRIEVED", currentLock.getName()+": "+currentLock.getMac()+" ---> "+LOCK_KEY);
                lockDetails.setText("Lock Details: "+currentLock.getName()+": "+currentLock.getMac());
            }

        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }
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
            adapter.getBluetoothLeScanner().startScan(callback);

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




    ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice bluetoothDevice = result.getDevice();
            Log.d("LE SCAN", bluetoothDevice.getAddress() + " --->" + LOCK_MAC_ADDRESS);

            if(bluetoothDevice.getAddress().equals(LOCK_MAC_ADDRESS)){
                status.setText("Lock Discovered");
                ourLock = bluetoothDevice;
                //adapter.stopLeScan(mLeScanCallback);
                if(buttonPressed)
                    mBluetoothGatt = ourLock.connectGatt(MainActivity.this,false,mGattCallback);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("FAILED",String.valueOf(errorCode));
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
                byte[] payload = {(byte)0x05,(byte)0x01,(byte)0x06,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
                byte[] encrypted_payload = encrypt_data(payload);
                //byte encrypted[] = encrypt_data(new String(payload));
                characteristic.setValue(encrypted_payload);
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

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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
                        //ourLock = null;
                    }
                }, 3000);
            }else{
                Toast.makeText(MainActivity.this,"Please make sure the light on the lock is blinking",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private String convert_key_to_hex(String key)
    {
        String hexArray="";
        for(int i=0;i<key.length();i+=2)
        {
            String currentHex = key.substring(i,i+2);
            int cur = Integer.parseInt(currentHex);
            //String hex = Integer.toHexString(cur);
            String hex1 = String.format("%02X", cur);
            hexArray+=hex1;
        }
        System.out.println("Key in Hex --->"+hexArray);
        byte[] intermediate = hexStringToByteArray(hexArray);
        System.out.println("Key in Hex --->"+new String(intermediate));
        return new String(intermediate);
    }


    private byte[] encrypt_data(byte[] data)
    {
        byte[] cipherText={0x00};
        int addNum = 0;
        while(data.length<16) {
            addNum++;
        }
        byte[] newPayload = new byte[data.length+addNum];

        for(int i=0;i<data.length;i++)
        {
            newPayload[i]= data[i];
        }
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(LOCK_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            System.out.println("data -->" + newPayload);
            cipherText = cipher.doFinal(newPayload);
            //System.out.println("Base64 encoded: "+ Base64.encode(data.getBytes()).length);

            //byte[] original = cipher.doFinal(Base64.encode(data.getBytes()));
            System.out.println("Cipher Text ----> " + bytesToHex(cipherText));
            return cipherText;
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return cipherText;
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.getBluetoothLeScanner().stopScan(callback);
    }
}
