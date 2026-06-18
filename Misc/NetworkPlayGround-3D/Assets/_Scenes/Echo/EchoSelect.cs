using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System;
using System.Net.Sockets;

public class EchoSelect : MonoBehaviour
{
    Socket socket;
    public InputField inputField;
    public Text text;

    private List<Socket> checkRead = new List<Socket>();
    private List<Socket> checkWrite = new List<Socket>();

    // Start is called before the first frame update
    void Start()
    {

    }

    public void Connection() {
        socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        socket.Connect("127.0.0.1", 8888);
    }

    public void Send() {
        Debug.Log("Socket Send Select");

        checkWrite.Clear();
        checkWrite.Add(socket);
        Socket.Select(null, checkWrite, null, 0);

        foreach(Socket s in checkWrite) {
            string sendStr = inputField.text;
            byte[] sendBytes = System.Text.Encoding.Default.GetBytes(sendStr);
            s.Send(sendBytes);
        }
    }

    // Update is called once per frame
    void Update()
    {
        if(socket == null) {
            return;
        }

        checkRead.Clear();
        checkRead.Add(socket);
        Socket.Select(checkRead, null, null, 0);

        foreach(Socket s in checkRead) {
            Debug.Log("Socket Receive Select");
            byte[] readBuff = new byte[1024];
            int count = s.Receive(readBuff);
            string recvStr = System.Text.Encoding.Default.GetString(readBuff, 0, count) + "\n" + text.text;
            text.text = recvStr;
        }
    }
}
