/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;

import various.common.light.network.utils.IPaddressConverter;
import various.common.light.utility.bytes.BitWorker;

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
		try {
			if(networkInterface != null && inetAddr != null) {
				for (InterfaceAddress addressIface : networkInterface.getInterfaceAddresses()) {
				    if (addressIface.getAddress().equals(inetAddr)) {
						return BitWorker.maskLengthToBytes(addressIface.getNetworkPrefixLength());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return "- Address Undefined -";
	}

	public String getBroadCastFromInetAddr() {
		try {
			if(networkInterface != null && inetAddr != null) {
				for (InterfaceAddress addressIface : networkInterface.getInterfaceAddresses()) {
					if (addressIface.getAddress().equals(inetAddr)) {
						return addressIface.getBroadcast().getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return "- Address Undefined -";
	}

	/** Name fragments of adapters created by VMs, VPNs, containers and the OS itself: real hardware never has them */
	private static final String[] VIRTUAL_NAME_HINTS = {"virtual", "vmware", "vmnet", "vbox", "virtualbox", "host-only", "hyper-v", "vethernet", "wsl", "docker", "veth", "br-", "tap-", "tap0", "tun", "tunnel", "teredo", "isatap", "6to4", "pseudo", "miniport", "npcap", "loopback", "bluetooth", "vpn", "wan ", "ppp", "tailscale", "zerotier", "hamachi", "wireguard", "kernel debug", "wi-fi direct", "virbr", "utun", "awdl", "llw", "bridge"};

	/**
	 * Heuristic: true for adapters that are not physical network cards (VM, VPN, container, tunnel, Bluetooth...).
	 * Java has no reliable flag for it, so it combines the interface flags, a missing MAC and the usual name fragments.
	 */
	public boolean isVirtual() {
		if (networkInterface == null) {
			return true;
		}
		try {
			if (networkInterface.isVirtual() || networkInterface.isPointToPoint() || networkInterface.isLoopback()) {
				return true;
			}
		} catch (java.net.SocketException e) {
			// unreadable flags: judged by name and MAC
		}
		if (localMAC == null || localMAC.length == 0) {
			return true;
		}
		String lower = ((name == null ? "" : name) + " " + networkInterface.getName()).toLowerCase(java.util.Locale.ROOT);
		for (String hint : VIRTUAL_NAME_HINTS) {
			if (lower.contains(hint)) {
				return true;
			}
		}
		return false;
	}

	/** True for addresses that are noise for a normal user: IPv6 and link-local (169.254.x.x, fe80::) */
	public boolean isSecondaryAddress() {
		return inetAddr == null || inetAddr instanceof java.net.Inet6Address || inetAddr.isLinkLocalAddress();
	}

	@Override
	public String toString() {
		return name +" - "+ locMAC + " - "+ locIP;
	}
}
