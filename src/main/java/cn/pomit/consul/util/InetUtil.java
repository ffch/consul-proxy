package cn.pomit.consul.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InetUtil {
	private final static Log log = LogFactory.getLog(InetUtil.class);

	public static InetAddress findFirstNonLoopbackAddress(List<String> preferredAddress) {
		InetAddress result = null;
		try {
			int lowest = Integer.MAX_VALUE;
			for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics
					.hasMoreElements();) {
				NetworkInterface ifc = nics.nextElement();
				if (ifc.isUp()) {
					log.trace("Testing interface: " + ifc.getDisplayName());
					if (ifc.getIndex() < lowest || result == null) {
						lowest = ifc.getIndex();
					} else if (result != null) {
						continue;
					}

					for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements();) {
						InetAddress address = addrs.nextElement();
						if (address instanceof Inet4Address && !address.isLoopbackAddress()
								&& isPreferredAddress(address, preferredAddress)) {
							log.trace("Found non-loopback interface: " + ifc.getDisplayName());
							result = address;
						}
					}
				}
			}
		} catch (IOException ex) {
			log.error("Cannot get first non-loopback address", ex);
		}

		if (result != null) {
			return result;
		}

		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.warn("Unable to retrieve localhost");
		}

		return null;
	}

	static boolean isPreferredAddress(InetAddress address, List<String> preferredAddress) {
		if (preferredAddress == null)
			return true;
		for (String regex : preferredAddress) {
			final String hostAddress = address.getHostAddress();
			if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
				return true;
			}
		}
		log.trace("Ignoring address: " + address.getHostAddress());
		return false;
	}
}
