package uTILS.network;

import java.net.InetAddress;

public class NetworkAdapter {
	
	public byte[] localIP;
	public byte[] remoteIP;
	public byte[] localMAC;
	public String locIP;
	public String remIP;
	public String locMAC;
	public InetAddress AddressInfo;
	
	public String name;
	public String hostName;
	
	public NetworkAdapter() {
		
	}

	public byte[] getLocalIP() {
		return localIP;
	}

	public byte[] getRemoteIP() {
		return remoteIP;
	}

	public byte[] getLocalMAC() {
		return localMAC;
	}

	public InetAddress getAddressInfo() {
		return AddressInfo;
	}

	public void setAddressInfo(InetAddress addressInfo) {
		AddressInfo = addressInfo;
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

	@Override
	public String toString() {
		return name +" - "+ locMAC + " - "+ locIP;
	}


}
