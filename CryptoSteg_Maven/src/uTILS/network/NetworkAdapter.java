package uTILS.network;

public class NetworkAdapter {
	
	public byte[] localIP;
	public byte[] remoteIP;
	public byte[] localMAC;
	public String locIP;
	public String remIP;
	public String locMAC;
	
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


}
