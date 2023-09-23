package com.thedirone.multiplayer_tic_tac_toe.features.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thedirone.multiplayer_tic_tac_toe.core.utils.returnCopiedIntArr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

// For now Server is for X
// X => 1
// O => 2
class ServerViewModel : ViewModel() {
    private val _receivedDataFromClient = MutableLiveData<Int>()
    val receivedDataFromClient: LiveData<Int> = _receivedDataFromClient

    private val _serverStatus = MutableLiveData<String>()
    val serverStatus: LiveData<String> = _serverStatus

    private val _gameArrayInfo = MutableLiveData<IntArray>()
    val gameArrayInfo: LiveData<IntArray> = _gameArrayInfo

    private val gameArr = IntArray(9)

    private var serverSocket: ServerSocket? = null
    private var socket: Socket? = null
    private var dataInputStream: DataInputStream? = null
    private var dataOutputStream: DataOutputStream? = null
    fun startServer() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(2345)
            } catch (e: IOException) {
                Log.d("ServerTesting", "failed to start server socket")
                withContext(Dispatchers.Main){
                    _serverStatus.value = "failed to start server socket"
                }
                e.printStackTrace()
            }

            // wait for a connection
            Log.d("ServerTesting", "waiting for connections...")
            withContext(Dispatchers.Main){
                _serverStatus.value = "waiting for connections..."
            }
            try {
                socket = serverSocket!!.accept()
            } catch (e: IOException) {
                withContext(Dispatchers.Main){
                    _serverStatus.value = "failed to accept"
                }
                Log.d("ServerTesting", "failed to accept")
                e.printStackTrace()
            }
            Log.d("ServerTesting", "client connected")
            withContext(Dispatchers.Main){
                _serverStatus.value = "client connected"
            }

                    // create input and output streams
            try {
                dataInputStream = DataInputStream(BufferedInputStream(socket!!.getInputStream()))
                dataOutputStream =
                    DataOutputStream(BufferedOutputStream(socket!!.getOutputStream()))
            } catch (e: IOException) {
                Log.d("ServerTesting", "failed to create streams")
                withContext(Dispatchers.Main){
                    _serverStatus.value = "failed to create streams"
                }
                e.printStackTrace()
            }

            try {
                while(true) {
                    val pos = dataInputStream!!.readInt()
                    val data = dataInputStream!!.readInt()
                    Log.d("ServerReceived", "Position is ${pos.toString()}")
                    Log.d("ServerReceived", "data is ${data.toString()}")
                   // gameArr[pos] = data
                    withContext(Dispatchers.Main) {
                        if(gameArr[pos] == 0){
                            gameArr[pos] = data
                            _gameArrayInfo.value = returnCopiedIntArr(gameArr)
                        }
                    }
//                    withContext(Dispatchers.Main){
//                        _receivedDataFromClient.value = test
//                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun sendDataWithPositionToClient(pos:Int, data:Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    if(gameArr[pos] == 0){
                         gameArr[pos] = data
                        _gameArrayInfo.value = returnCopiedIntArr(gameArr)
                    }
                }
                dataOutputStream!!.writeInt(pos)
                dataOutputStream!!.writeInt(data)
                dataOutputStream!!.flush()
            } catch (e: IOException) {
                Log.d("ServerTesting", "failed to send: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun closeServer() {
        viewModelScope.launch {

        }
    }
}