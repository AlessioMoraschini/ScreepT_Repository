package network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;

import utility.BitWorker;

public class NetworkAdapter {
	
	// FIELDS //
	
	public byte[] localIP;
	public byte[] remoteIP;
	public byte[] localMAC;
	public String locIP;
	public String remIP;
	public String locMAC;
	public InetAddress inetAddr;
	public NetworkInterface networkInterface;
	
	public int MTU;
	 
	public String name;
	public String hostName;
	
	// CONSTRUCTOR //
	
	public NetworkAdapter() {
		
	}

	// GETTERS AND SETTERS //
	
	public byte[] getLocalIP() {
		return localIP;
	}

	public byte[] getRemoteIP() {
		return remoteIP;
	}

	public byte[] getLocalMAC() {
		return localMAC;
	}

	public InetAddress getInetAddr() {
		return inetAddr;
	}

	public void setInetAddr(InetAddress inetAddr) {
		this.inetAddr = inetAddr;
	}

	public void setLocalIP(byte[] localIP) {
		this.localIP = localIP;
		this.locIP = IPaddressConverter.getIpAddress(localIP);
	}

	public void setRemoteIP(byte[] remoteIP) {
		this.remoteIP = remoteIP;
		this.remIP = IPaddressConverter.getIpAddress(remoteIP);
	}

	public void setLocalMAC(byte[] localMAC) {
		this.localMAC = localMAC;
		this.locMAC = IPaddressConverter.getMacFromBytes(localMAC);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getMTU() {
		return MTU;
	}

	public void setMTU(int mTU) {
		MTU = mTU;
	}

	public NetworkInterface getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
	}
	
	public String getSubnetMaskFromInetAddr() {
		if(networkInterface != null && inetAddr != null) {
			for (InterfaceAddress addressIface : networkInterface.getInterfaceAddresses()) {
			    if (addressIface.getAddress().equals(inetAddr)) {
					return BitWorker.maskLengthToBytes(addressIface.getNetworkPrefixLength());
				}
			}
		} 
		
		return "- Address Undefined -";
	}

	public String getBroadCastFromInetAddr() {
		if(networkInterface != null && inetAddr != null) {
			for (InterfaceAddress addressIface : networkInterface.getInterfaceAddresses()) {
				if (addressIface.getAddress().equals(inetAddr)) {
					return addressIface.getBroadcast().getHostAddress();
				}
			}
		} 
		
		return "- Address Undefined -";
	}

	@Override
	public String toString() {
		return name +" - "+ locMAC + " - "+ locIP;
	}
}
