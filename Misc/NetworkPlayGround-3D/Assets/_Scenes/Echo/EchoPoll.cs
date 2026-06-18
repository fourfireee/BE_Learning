using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System;
using System.Net.Sockets;

public class EchoPoll : MonoBehaviour
{
    Socket socket;
    public InputField inputField;
    public Text text;

    // Start is called before the first frame update
    void Start()
    {

    }

    public void Connection() {
        socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        socket.Connect("127.0.0.1", 8888);
    }

    public void Send() {
        Debug.Log("Socket Send Poll");
        if(socket.Poll(0, SelectMode.SelectWrite)) {
            string sendStr = inputField.text;
            byte[] sendBytes = System.Text.Encoding.Default.GetBytes(sendStr);
            socket.Send(sendBytes);
        }
    }

    // Update is called once per frame
    void Update()
    {
        if(socket == null) {
            return;
        }

        if(socket.Poll(0, SelectMode.SelectRead)) {
            Debug.Log("Socket Receive Poll");
            byte[] readBuff = new byte[1024];
            int count = socket.Receive(readBuff);
            string recvStr = System.Text.Encoding.Default.GetString(readBuff, 0, count) + "\n" + text.text;
            text.text = recvStr;
        }
    }
}
