package example.zerotier;
import zerotier.*;

import java.io.IOException;
import java.lang.Thread;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UdpProxyZerotier {

    public interface interfaceSendDate{
        DatagramPacket getdate();
    }

    static { System.loadLibrary("zt-shared"); } // load libzt.dylib or libzt.so

    final ZeroTier zt = new ZeroTier();

    String path = null;
    long nwid = 0x17d709436c911e4fL;

    // Test modes
    boolean blocking_start_call = true;
    boolean loop = true; // RX/TX multiple times
    boolean idle = false; // Idle loop after node comes online. For testing reachability
    boolean use_select = true;

    int err, r, w, lengthToRead = 0, flags = 0;
    String remoteAddrStr = "11.7.7.107";
    String localAddrStr = "0.0.0.0";
    int portNo = 4040;

    ZTSocketAddress remoteAddr, localAddr;
    ZTSocketAddress sockname = new ZTSocketAddress();
    ZTSocketAddress addr = new ZTSocketAddress();

    public void mainZerotier(String args) {
        path = args;

        new Thread(new Runnable() {
            public void run() {

                if ( zt.stack_running() )
                    return;

                System.out.println("Starting ZT service...");
                if (blocking_start_call) {
                    zt.startjoin(path, nwid);
                }

                System.out.println("Core started. Networks can be joined after this point");
                zt.join(nwid);

                checkZtruning();

                System.out.println("ZT service ready.");
                // Device/Node address info
                System.out.println("path=" + zt.get_path());

                long nodeId = zt.get_node_id();
                System.out.println("nodeId=" + Long.toHexString(nodeId));
                int numAddresses = zt.get_num_assigned_addresses(nwid);
                System.out.println("this node has (" + numAddresses + ") assigned addresses on network " + Long.toHexString(nwid));

                for (int i = 0;
                     i < numAddresses; i++) {
                    zt.get_address_at_index(nwid, i, sockname);
                    System.out.println("address[" + i + "] = " + sockname.toCIDR());
                }
                udpstartServerinit();
            }
        }).start();
    }

    private void checkZtruning() {
        // Wait for userspace stack to start, we trigger this by joining a network
        while (!zt.stack_running()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    DatagramSocket clientDs = null;
    InetSocketAddress clientAddress =null;

    public void udpstartServer(InetSocketAddress clientAddress) {
        this.clientAddress = clientAddress;
        try {
            this.clientDs = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void udpstartServerinit() {

        checkZtruning();
        int fd = -1;
        System.out.println("mode:udp");
        if ((fd = zt.socket(zt.AF_INET, zt.SOCK_DGRAM, 0)) < 0) {
            System.out.println("error creating socket");
            return;
        }

        System.out.println("mode:server");
        localAddr = new ZTSocketAddress(localAddrStr, portNo);
        System.out.println("binding to " + localAddr.toString());
        if ((err = zt.bind(fd, localAddr)) < 0) {
            System.out.println("error binding socket to virtual interface");
            return;
        }

        byte[] rxBuffer = new byte[1028*8];
        remoteAddr = new ZTSocketAddress("-1.-1.-1.-1", 0);

        if (!use_select) {
            while (true) {
                addr = new ZTSocketAddress();
                r = zt.recvfrom(fd, rxBuffer, rxBuffer.length, flags, remoteAddr);

                if ( r > 0 && this.clientDs != null && this.clientAddress != null )
                {
                    DatagramPacket dp = new DatagramPacket(rxBuffer, r, clientAddress);
                    try {
                        clientDs.send(dp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("read (" + r + ") bytes from " + remoteAddr.toString() + ", buffer = " + new String(rxBuffer));
            }
        }
        zt.close(fd);
    }

    public void udpstartClient(interfaceSendDate getDate) {
        checkZtruning();

        int fd = -1;
        System.out.println("mode:udp");
        if ((fd = zt.socket(zt.AF_INET, zt.SOCK_DGRAM, 0)) < 0) {
            System.out.println("error creating socket");
            return;
        }

        System.out.println("mode:client");
        localAddr = new ZTSocketAddress(localAddrStr, portNo);
        if ((err = zt.bind(fd, localAddr)) < 0) {
            System.out.println("error binding socket to virtual interface");
            return;
        }
        remoteAddr = new ZTSocketAddress(remoteAddrStr, portNo);
        System.out.println("sending message to: " + remoteAddr.toString());
        if (loop) {
            while (true) {
                DatagramPacket dp = getDate.getdate();
                byte[] txBuffersend = dp.getData();
                if ((w = zt.sendto(fd, txBuffersend, dp.getLength(), flags, remoteAddr)) < 0) {
                    System.out.println("error sending bytes");
                } else {
                    System.out.println("sendto()=" + w);
                }
            }
        } else {
            byte[] rxBuffer;
            byte[] txBuffer = "welcome to the machine".getBytes();
            if ((w = zt.sendto(fd, txBuffer, txBuffer.length, flags, remoteAddr)) < 0) {
                System.out.println("error sending bytes");
            } else {
                System.out.println("sendto()=" + w);
            }
        }
        zt.close(fd);
    }
}
