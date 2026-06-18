using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Net.Sockets;
using UnityEngine.UI;
using System;

// connect、receive、send，都改成 async
public class EchoAsync : MonoBehaviour
{
    Socket socket;

    public InputField inputField;
    public Text text;

    byte[] readBuff = new byte[1024];
    string recvStr = "";

    // Start is called before the first frame update
    void Start()
    {
        // Debug.Log("echo async enter");
    }

    public void Connection()
    {
        socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        // socket.Connect("127.0.0.1", 8888);
        socket.BeginConnect("127.0.0.1", 8888, ConnectCallback, socket);
    }

    public void ConnectCallback(IAsyncResult ar)
    {
        try {
            Socket socket = (Socket)ar.AsyncState;
            socket.EndConnect(ar); // 结束挂起的异步连接请求
            Debug.Log("Sockect Connect Succ");

            socket.BeginReceive(readBuff, 0, 1024, 0, ReceiveCallback, socket);
        } catch(SocketException ex) {
            Debug.Log("Sockect Connect Fail: " + ex.ToString());
        }
    }

    public void ReceiveCallback(IAsyncResult ar)
    {
        try {
            Socket socket = (Socket)ar.AsyncState;
            int count = socket.EndReceive(ar); // 结束挂起的异步receive请求
            string s = System.Text.Encoding.Default.GetString(readBuff, 0, count);
            recvStr = s + "\n" + recvStr;
            Debug.Log("Receive Str = " + recvStr);

            socket.BeginReceive(readBuff, 0, 1024, 0, ReceiveCallback, socket);
        } catch(SocketException ex) {
            Debug.Log("Socket Receive Fail: " + ex.ToString());
        }
    }

    public void Send()
    {
        Debug.Log("Send Stuff from Client");
        string sendStr = inputField.text;
        byte[] sendBytes = System.Text.Encoding.Default.GetBytes(sendStr);
        // socket.Send(sendBytes);
        socket.BeginSend(sendBytes, 0, sendBytes.Length, 0, SendCallback, socket);

        // byte[] readBuff = new byte[1024];
        // int count = socket.Receive(readBuff);
        // string recvStr = System.Text.Encoding.Default.GetString(readBuff, 0, count);
        // text.text = recvStr;
        // socket.Close();
    }

    public void SendCallback(IAsyncResult ar) {
        try {
            Socket socket  = (Socket)ar.AsyncState;
            int count = socket.EndSend(ar);
            Debug.Log("Socket Send Succ: " + count);
        } catch(SocketException ex) {
            Debug.Log("Socket Send Fail: " + ex.ToString());
        }
    }

    // Update is called once per frame
    void Update()
    {
        text.text = recvStr;
    }
}
