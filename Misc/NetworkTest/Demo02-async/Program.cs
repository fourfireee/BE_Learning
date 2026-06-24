using System;
using System.Net;
using System.Net.Sockets;
using System.Collections.Generic;

class ClientState {
    public Socket? socket; // 「?」表示将该字段声明为可以为null
    public byte[] readBuff = new byte[1024];
}

/*
→ Bind：同步
→ Listen：同步
→ Accept：异步
→ Receive：异步
→ Send：异步
*/
class MainClass {
    static Socket listenfd = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
    static Dictionary<Socket, ClientState> clients = new Dictionary<Socket, ClientState>();

    public static void Main(string[] args) {
        Console.WriteLine("hello");

        // listenfd = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        IPAddress ipAdr = IPAddress.Parse("127.0.0.1");
        IPEndPoint ipEpt = new IPEndPoint(ipAdr, 8888);
        listenfd.Bind(ipEpt);

        listenfd.Listen(0);
        Console.WriteLine("[服务器] 启动成功");

        //
        listenfd.BeginAccept(AcceptCallback, listenfd);

        Console.ReadLine();
    }

    public static void AcceptCallback(IAsyncResult ar) {
        try {
            Console.WriteLine("[服务器] Accept");
            Socket listenfd = (Socket)ar.AsyncState!;
            Socket clientfd = listenfd.EndAccept(ar);

            ClientState state = new ClientState();
            state.socket = clientfd;
            clients.Add(clientfd, state);
            clientfd.BeginReceive(state.readBuff, 0, 1024, 0, ReceiveCallback, state);
            listenfd.BeginAccept(AcceptCallback, listenfd);
        } catch(SocketException ex) {
            Console.WriteLine("Socket Accept Fail : " + ex.ToString());
        }
    }

    public static void ReceiveCallback(IAsyncResult ar) {
        try {
            Console.WriteLine("Receive Stuff");

            ClientState state = (ClientState)ar.AsyncState!; // 「!」表示这里一定不是空(null)。处理一个warning：https://blog.csdn.net/Surbowl/article/details/109024026
            Socket clientfd = state.socket!;
            int count = clientfd.EndReceive(ar);

            if(count == 0) {
                clientfd.Close();
                clients.Remove(clientfd);
                Console.WriteLine("Socket close");
                return;
            }

            string recvStr = System.Text.Encoding.Default.GetString(state.readBuff, 0, count);
            string sendStr = clientfd.RemoteEndPoint!.ToString() + " : " + recvStr;
            byte[] sendBytes = System.Text.Encoding.Default.GetBytes(sendStr);

            //clientfd.Send(sendBytes); // 减少代码量不做异步
            // clientfd.BeginSend(sendBytes, 0, sendBytes.Length, 0, SendCallback, clientfd);
            foreach(ClientState s in clients.Values) {
                s.socket!.BeginSend(sendBytes, 0, sendBytes.Length, 0, SendCallback, s.socket);
            }

            clientfd.BeginReceive(state.readBuff, 0, 1024, 0, ReceiveCallback, state);
        } catch(SocketException ex) {
            Console.WriteLine("Socket Receive Fail : " + ex.ToString());
        }
    }

    public static void SendCallback(IAsyncResult ar) {
        try {
            Socket socket = (Socket)ar.AsyncState!;
            int count = socket.EndSend(ar);
            Console.WriteLine("Socket Send Succ : " + count);
        } catch(SocketException ex) {
            Console.WriteLine("Socket Send Fail : " + ex.ToString());
        }
    }
}