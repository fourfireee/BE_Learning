using System;
using System.Net;
using System.Net.Sockets;
using System.Collections.Generic;

class ClientState {
    public Socket? socket;
    public byte[] readBuff = new byte[1024];
}

class MainClass {
    static Socket listenfd = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
    static Dictionary<Socket, ClientState> clients = new Dictionary<Socket, ClientState>();

    public static void Main(string[] args) {
        IPAddress ipAdr = IPAddress.Parse("127.0.0.1");
        IPEndPoint ipEp = new IPEndPoint(ipAdr, 8888);
        listenfd.Bind(ipEp);

        listenfd.Listen(0);
        Console.WriteLine("[服务器] 启动成功");

        List<Socket> checkRead = new List<Socket>();
        while(true) {
            checkRead.Clear();
            checkRead.Add(listenfd);
            foreach(ClientState s in clients.Values) {
                checkRead.Add(s.socket!);
            }

            Socket.Select(checkRead, null, null, 0);
            foreach(Socket s in checkRead) {
                if(s == listenfd) {
                    ReadListenfd(s);
                } else {
                    ReadClientfd(s);
                }
            }
        }
    }

    public static void ReadListenfd(Socket listenfd) {
        Console.WriteLine("Accept");
        Socket clientfd = listenfd.Accept();
        ClientState s = new ClientState();
        s.socket = clientfd;
        clients.Add(clientfd, s);
    }

    public static bool ReadClientfd(Socket clientfd) {
        ClientState state = clients[clientfd];
        int count = 0;
        try {
            count = clientfd.Receive(state.readBuff);
        } catch(SocketException ex) {
            clientfd.Close();
            clients.Remove(clientfd);
            Console.WriteLine("Socket Receive Exception: " + ex.ToString());
            return false;
        }
        if(count == 0) {
            clientfd.Close();
            clients.Remove(clientfd);
            Console.WriteLine("Socket Close");
            return false;
        }

        string recvStr = System.Text.Encoding.Default.GetString(state.readBuff, 0, count);
        Console.WriteLine("Receive : " + recvStr);
        string sendStr = clientfd.RemoteEndPoint!.ToString() + " : " + recvStr;
        byte[] sendBytes = System.Text.Encoding.Default.GetBytes(sendStr);
        foreach(ClientState cs in clients.Values) {
            cs.socket!.Send(sendBytes);
        }
        return true;
    }
}