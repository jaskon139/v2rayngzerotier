package example.zerotier;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import example.zerotier.UdpProxyZerotier;

public class Udpproxy {

    final static UdpProxyZerotier zt = new UdpProxyZerotier();

    static InetAddress localaddress = null;
    static int localport = -1;

    public static void mainudpproxy(String args) {
        zt.mainZerotier(null);
        Udpproxy proxy = new Udpproxy();
        proxy.send();
        proxy.receiver();
    }

    public static void send() {
        new Thread() {
            public void run() {
                try {

                    InetSocketAddress address = null;

                    /*
                     * IP地址和端口，注意端口不要重复
                     */
                    while(localaddress == null || localport == -1 )
                    {
                        sleep(300);
                    }

                    address = new InetSocketAddress(localaddress,localport);
                    // 定义一个UDP的Socket来发送数据


                    // 定义一个UDP的数据发送包来发送数据，inetSocketAddress表示要接收的地址
                    zt.udpstartServer( address );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void receiver() {
        new Thread() {
            @Override
            public void run() {
                // UDP接收端
                /*
                 * 这里只需要设置端口，无需IP地址。其端口与发送端一致。
                 */
                final DatagramSocket ds;
                try {
                    ds = new DatagramSocket(3000);

                    // 定义将UDP的数据包接收到什么地方

                    byte[] buf = new byte[1024 * 8];
                    // 定义UDP的数据接收包
                    final DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    zt.udpstartClient(new UdpProxyZerotier.interfaceSendDate() {
                        public DatagramPacket getdate() {
                            try {
                                ds.receive(dp);
                                localaddress = dp.getAddress();
                                localport = dp.getPort();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return dp;
                        }
                    });
                    ds.close();

                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}