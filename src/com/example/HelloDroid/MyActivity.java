package com.example.HelloDroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    TextView tvOut;
    Button btnOk;
    Button btnCancel;
    Button startServer;
    Button stopServer;
    Button btnConnect;

    BluetoothDevice deviceForConnect;

    public static final String bluetoothDevicePahaStr = "BC:CF:CC:EA:4F:FF";
    public static final String bluetoothDeviceDjuStr = "50:FC:9F:DE:BD:18";

    BluetoothAdapter mBluetoothAdapter;

    public static final String NAME = "testBl";
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    //ArrayList< HashMap<String, String > findDev  //= new HashMap<String, String>();
    ArrayList< HashMap<String,String> > findDev = new ArrayList<HashMap<String, String>>();
    private static final int REQUEST_ENABLE_BT = 1;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                deviceForConnect = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //findDev.add("name", device.getName());
                HashMap<String,String> item = new HashMap<String, String>();
                item.put("name", deviceForConnect.getName());
                item.put("mac", deviceForConnect.getAddress());
                findDev.add(item);
                tvOut.setText(deviceForConnect.getName() + "\n" + deviceForConnect.getAddress() + "\n" + deviceForConnect.getUuids()[0]);
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    //private ArrayAdapter mArrayAdapter

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // найдем View-элементы
        tvOut = (TextView) findViewById(R.id.tvOut);
        btnOk = (Button) findViewById(R.id.btnOk);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        startServer = (Button) findViewById(R.id.startServer);
        stopServer = (Button) findViewById(R.id.stopServer);
        btnConnect = (Button) findViewById(R.id.btnConnect);

        startServer.setOnClickListener( oclBtnOk );
        stopServer.setOnClickListener( oclBtnOk  );
        btnConnect.setOnClickListener( oclBtnOk );
        btnCancel.setOnClickListener( oclBtnOk  );
        btnOk.setOnClickListener( oclBtnOk );

        //btnCancel

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            Log.d("BLOO", "Not supported");
        }
        else
        {
            Log.d("BLOO", "ALL good");
        }

        if (mBluetoothAdapter.isEnabled())
        {
            Log.d("BLOO", "Check Name");
            String mydeviceaddress = mBluetoothAdapter.getAddress();
            String mydevicename = mBluetoothAdapter.getName();

            String state =  Integer.toString( mBluetoothAdapter.getState() );

            String status = mydevicename + " : " + mydeviceaddress + " state " + state;
            tvOut.setText( status );
            //mBluetoothAdapter.setName("Sum Dju");
        }
        /*
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        */
    }

    View.OnClickListener oclBtnOk = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btnConnect:
                    new ConnectThread(deviceForConnect).start();
                    break;
                case R.id.btnOk:

                    tvOut.setText("Нажата кнопка ОК");
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                    mBluetoothAdapter.startDiscovery();

                    break;
                case R.id.startServer:
                    /*
                    tvOut.setText("Нажата кнопка ОК");
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                    mBluetoothAdapter.startDiscovery();
                    */
                    Log.d("BLOO", "Before connect");
                    new AcceptThread().start();
                    break;
                case R.id.stopServer:
                    /*
                    tvOut.setText("Нажата кнопка ОК");
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                    mBluetoothAdapter.startDiscovery();
                    */
                    new AcceptThread().cancel();
                    break;
                case R.id.btnCancel:
                    tvOut.setText("Нажата кнопка Cancel");
                    mBluetoothAdapter.cancelDiscovery();
                    break;
            }

        }
    };
    private class AcceptThread extends Thread {

        public void manageConnectedSocket( BluetoothSocket  socket )
        {


        }

        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    Log.d("BLOO", "Before accept");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null)
                {
                    // Do work to manage the connection (in a separate thread)
                    /*
                    manageConnectedSocket(socket);
                    mmServerSocket.close();
                    */
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            //mmDevice = device;
            mmDevice = mBluetoothAdapter.getRemoteDevice( "BC:CF:CC:EA:4F:FF" );
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Log.d("BLOO", "Before connect");
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    /*
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                            */
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    /*

    public class AcceptThread extends Thread
    {
        private final BluetoothServerSocket mmServerSocket;


        public AcceptThread() {
            // используем вспомогательную переменную, которую в дальнейшем
            // свяжем с mmServerSocket,
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID это UUID нашего приложения, это же значение
                // используется в клиентском приложении
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // ждем пока не произойдет ошибка или не
            // будет возвращен сокет
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // если соединение было подтверждено
                if (socket != null) {
                    // управлчем соединением (в отдельном потоке)
                    manageConnectedSocket(socket);
                    mmServerSocket.close();
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }


    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // используем вспомогательную переменную, которую в дальнейшем
            // свяжем с mmSocket,
            BluetoothSocket tmp = null;
            mmDevice = device;

            // получаем BluetoothSocket чтобы соединиться с  BluetoothDevice
            try {
                // MY_UUID это UUID, который используется и в сервере
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Отменяем сканирование, поскольку оно тормозит соединение
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Соединяемся с устройством через сокет.
                // Метод блокирует выполнение программы до
                // установки соединения или возникновения ошибки
                mmSocket.connect();
            } catch (IOException connectException) {
                // Невозможно соединиться. Закрываем сокет и выходим.
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // управлчем соединением (в отдельном потоке)
            manageConnectedSocket(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Получить входящий и исходящий потоки данных
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // буферный массив
            int bytes; // bytes returned from read()

            // Прослушиваем InputStream пока не произойдет исключение
            while (true) {
                try {
                    // читаем из InputStream
                    bytes = mmInStream.read(buffer);
                    // посылаем прочитанные байты главной деятельности
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

    }
    */

}
