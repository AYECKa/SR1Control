package com.root;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.commons.net.tftp.TFTPServer;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class DeviceInterface
{
	private Snmp				snmp			= null;
	private InetAddress			address			= null;
	private TransportMapping	transport		= new DefaultUdpTransportMapping();
	private double				lastOctets		= 0;
	private double[]			bitRates		= new double[10];
	private int					bitRateIndex	= 0;
	private String				readCommunity	= "public";
	private String				writeCommunity	= "private";
	private long				lastBitRatePoll	= 0;
	private int					bitRateSlots	= 0;
	private InetAddress			tftpServerIp	= null;
	private TFTPServer			server			= null;

	static class Settings
	{

		public static final String	Esno					= "esno1";
		public static final String	PowerLevel				= "powerLevel1";
		public static final String	LinkMargin				= "linkMargin1";
		public static final String	BitRate					= "bitRate";
		public static final String	SoftwareVersion			= "softwareVersion";
		public static final String	FpgaVersion				= "fpgaVersion";
		public static final String	HardwareVersion			= "hardwareVersion";
		public static final String	DeviceLock				= "demodulatorStatus1";

		public static final String	SoftwareImageFilename	= "softwareImageFilename";
		public static final String	StartSoftwareUpgrade	= "startSoftwareUpgrade";
		public static final String	SoftwareVersionValue	= "softwareVersionValue";
		public static final String	SoftwareVersionValid	= "softwareVersionValid";
		public static final String	SoftwareVersionActive	= "softwareVersionActive";

		public static final String	TftpServerIp			= "tftpServerIp";
		public static final String	FpgaImageFilename		= "fpgaImageFilename";
		public static final String	StartFpgaUpgrade		= "startFpgaUpgrade";
		public static final String	FpgaVersionValue		= "fpgaVersionValue";
		public static final String	FpgaVersionValid		= "fpgaVersionValid";
		public static final String	FpgaVersionActive		= "fpgaVersionActive";

		public static final String	ColdReset				= "coldReset";
	}

	private static DeviceInterface	instance	= null;

	public static DeviceInterface getInstance()
	{
		return instance;
	}

	public static DeviceInterface createInstance(InetAddress address, String readCommunity, String writeCommunity,
			TFTPServer server) throws Exception
	{
		instance = new DeviceInterface(address, readCommunity, writeCommunity, server);
		return instance;
	}

	private DeviceInterface(InetAddress address, String readCommunity, String writeCommunity, TFTPServer server)
			throws Exception
	{
		this.server = server;

		long b = 0;

		for (int i = 3; i >= 0; i--)
		{
			b <<= 8;
			b |= (address.getAddress()[i] & 0xFF);
		}

		Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();

		while (nics.hasMoreElements() && (tftpServerIp == null))
		{
			NetworkInterface nic = nics.nextElement();

			for (InterfaceAddress nicInterfaceAddress : nic.getInterfaceAddresses())
			{
				InetAddress nicAddress = nicInterfaceAddress.getAddress();

				if (nicAddress instanceof Inet4Address)
				{
					long a = 0;

					for (int i = 3; i >= 0; i--)
					{
						a <<= 8;
						a |= (nicAddress.getAddress()[i] & 0xFF);
					}

					long s = (1 << nicInterfaceAddress.getNetworkPrefixLength()) - 1;

					if ((a & s) == (b & s))
					{
						this.tftpServerIp = nicAddress;
						break;
					}
				}
			}
		}

		snmp = new Snmp(transport);

		transport.listen();

		this.address = address;
		this.readCommunity = readCommunity;
		this.writeCommunity = writeCommunity;

		for (int i = 0; i < bitRates.length; i++)
			bitRates[i] = 0;
	}

	private Target getTarget(String community)
	{
		String str = address.getHostAddress();
		Address targetAddress = GenericAddress.parse("udp:" + str + "/161");
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		target.setAddress(targetAddress);
		target.setRetries(new Integer(Param.getValue("SnmpRetries", "1")));
		target.setTimeout(new Integer(Param.getValue("SnmpTimeout", "1000")));
		target.setVersion(SnmpConstants.version2c);
		return target;
	}

	private synchronized ResponseEvent get(OID oids[]) throws IOException
	{
		PDU pdu = new PDU();
		for (OID oid : oids)
		{
			pdu.add(new VariableBinding(oid));
		}
		pdu.setType(PDU.GET);

		Target target = getTarget(readCommunity);
		ResponseEvent event = snmp.send(pdu, target, transport);
		if (event != null) { return event; }
		throw new RuntimeException("GET timed out");
	}

	private synchronized ResponseEvent set(OID oid, Variable value) throws IOException
	{
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(oid, value));
		pdu.setType(PDU.SET);

		Target target = getTarget(writeCommunity);
		ResponseEvent event = snmp.send(pdu, target, transport);
		if (event != null) { return event; }
		throw new RuntimeException("SET timed out");
	}

	public void coldReset() throws Exception
	{
		set(new OID(MibParser.getOid(Settings.ColdReset)), new Integer32(1));
	}

	public double getEsno()
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(MibParser.getOid(Settings.Esno)) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return Double.parseDouble(responsePdu.get(0).getVariable().toString()) / 10;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	public double getPowerLevel() throws Exception
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(MibParser.getOid(Settings.PowerLevel)) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return Double.parseDouble(responsePdu.get(0).getVariable().toString()) / 10;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	public double getLinkMargin() throws Exception
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(MibParser.getOid(Settings.LinkMargin)) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return Double.parseDouble(responsePdu.get(0).getVariable().toString()) / 10;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	public double getBitRate() throws Exception
	{
		double ret = 0;

		try
		{
			ResponseEvent event = get(new OID[] { new OID(".1.3.6.1.2.1.2.2.1.16.1") });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null)
			{
				double currentOctets = Double.parseDouble(responsePdu.get(0).getVariable().toString());

				long now = System.currentTimeMillis();

				if (lastBitRatePoll > 0)
				{
					double diff = (now - lastBitRatePoll) / 1000.0;
					double bitRate = (currentOctets - lastOctets) * 8 / diff / 1e6;
					bitRates[bitRateIndex++] = bitRate;
					if (bitRateIndex == bitRates.length) bitRateIndex = 0;

					double averageBitRate = 0;

					if (bitRateSlots < bitRates.length) bitRateSlots++;

					for (int i = 0; i < Math.min(bitRateSlots, bitRates.length); i++)
					{
						averageBitRate += bitRates[i];
					}

					ret = averageBitRate / bitRates.length;
				}

				lastOctets = currentOctets;
				lastBitRatePoll = now;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return ret;
	}

	public String getSoftwareVersion()
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(MibParser.getOid(Settings.SoftwareVersion)) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return responsePdu.get(0).getVariable().toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return "0.0";
	}

	public String getFpgaVersion()
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(MibParser.getOid(Settings.FpgaVersion)) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return responsePdu.get(0).getVariable().toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return "0.0";
	}

	public String getHardwareVersion()
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(MibParser.getOid(Settings.HardwareVersion)) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return responsePdu.get(0).getVariable().toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return "0.0";
	}

	public boolean isLocked()
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(MibParser.getOid(Settings.DeviceLock)) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return responsePdu.get(0).getVariable().toInt() == 1;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public void setAveragingLength(int averageLength)
	{
		bitRates = new double[averageLength];

		for (int i = 0; i < bitRates.length; i++)
			bitRates[i] = 0;
	}

	public String getStringValue(String oid)
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(oid) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return responsePdu.get(0).getVariable().toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return "";
	}

	public int getIntValue(String oid)
	{
		try
		{
			ResponseEvent event = get(new OID[] { new OID(oid) });
			PDU responsePdu = event.getResponse();

			if (responsePdu != null) return responsePdu.get(0).getVariable().toInt();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return -1;
	}

	private HashMap<String, String>	upgradeOids	= new HashMap<String, String>();

	static class Upgrade
	{
		public static final String	TftpServerIp	= "TftpServerIp";
		public static final String	ImageFilename	= "ImageFilename";
		public static final String	StartUpgrade	= "StartUpgrade";
		public static final String	VersionValue	= "VersionValue";
		public static final String	VersionValid	= "VersionValid";
		public static final String	VersionActive	= "VersionActive";
		public static final String	UpgradeVersion	= "UpgradeVersion";
	}

	public void startFirmwareUpgrade(String filename, SwingProgressBar bar) throws Exception
	{
		if (filename.length() == 0) throw new Exception("Invalid filename");

		upgradeOids.clear();
		upgradeOids.put(Upgrade.TftpServerIp, MibParser.getOid(Settings.TftpServerIp));
		upgradeOids.put(Upgrade.ImageFilename, MibParser.getOid(Settings.FpgaImageFilename));
		upgradeOids.put(Upgrade.StartUpgrade, MibParser.getOid(Settings.StartFpgaUpgrade));
		upgradeOids.put(Upgrade.VersionValue, MibParser.getOid(Settings.FpgaVersionValue));
		upgradeOids.put(Upgrade.VersionValid, MibParser.getOid(Settings.FpgaVersionValid));
		upgradeOids.put(Upgrade.VersionActive, MibParser.getOid(Settings.FpgaVersionActive));
		upgradeOids.put(Upgrade.UpgradeVersion, MibParser.getOid(Settings.FpgaVersion));
		upgrade(filename, bar);
	}

	public void startSoftwareUpgrade(String filename, SwingProgressBar bar) throws Exception
	{
		if (filename.length() == 0) throw new Exception("Invalid filename");

		upgradeOids.clear();
		upgradeOids.put(Upgrade.TftpServerIp, MibParser.getOid(Settings.TftpServerIp));
		upgradeOids.put(Upgrade.ImageFilename, MibParser.getOid(Settings.SoftwareImageFilename));
		upgradeOids.put(Upgrade.StartUpgrade, MibParser.getOid(Settings.StartSoftwareUpgrade));
		upgradeOids.put(Upgrade.VersionValue, MibParser.getOid(Settings.SoftwareVersionValue));
		upgradeOids.put(Upgrade.VersionValid, MibParser.getOid(Settings.SoftwareVersionValid));
		upgradeOids.put(Upgrade.VersionActive, MibParser.getOid(Settings.SoftwareVersionActive));
		upgradeOids.put(Upgrade.UpgradeVersion, MibParser.getOid(Settings.SoftwareVersion));
		upgrade(filename, bar);
	}

	private void upgrade(String filename, SwingProgressBar bar) throws Exception
	{
		if (tftpServerIp == null)
		{
			JOptionPane.showMessageDialog(null, "Unable to set TFTP Server IP", "Upgrade", JOptionPane.ERROR_MESSAGE);
			return;
		}

		UpgradeVersion fuv = new UpgradeVersion(new File("./TFTP/" + filename));
		System.out.println(fuv);

		bar.setText("Configuring TFTP Server as " + tftpServerIp);
		set(new OID(upgradeOids.get(Upgrade.TftpServerIp)), new IpAddress(tftpServerIp));
		bar.setText("Setting filename: " + filename);
		set(new OID(upgradeOids.get(Upgrade.ImageFilename)), new OctetString(filename));
		bar.setText("Starting upgrade: " + filename);
		set(new OID(upgradeOids.get(Upgrade.StartUpgrade)), new Integer32(1));

		long start = System.currentTimeMillis();
		int index = 0;

		int tftpTimeout = new Integer(Param.getValue("TftpTimeout", "60000"));
		int rebootTimeout = new Integer(Param.getValue("RebootTimeout", "90000"));
		boolean tftpStarted = false;

		while (true)
		{
			Thread.sleep(5000);

			String upgradeVersion = getStringValue(upgradeOids.get(Upgrade.VersionValue) + ".1");
			UpgradeVersion uv = new UpgradeVersion(upgradeVersion.trim());

			if (uv.equals(fuv))
			{
				index = 1;
				break;
			}

			// System.out.println("UpgradeVersion(1) = " + uv + ", Filename = "
			// + fuv);

			upgradeVersion = getStringValue(upgradeOids.get(Upgrade.VersionValue) + ".2");
			uv = new UpgradeVersion(upgradeVersion.trim());

			if (uv.equals(fuv))
			{
				index = 2;
				break;
			}

			// System.out.println("UpgradeVersion(2) = " + uv + ", Filename = "
			// + fuv);

			long now = System.currentTimeMillis();

			if (server == null)
			{
				bar.setText("Waiting for TFTP process to finish (" + ((now - start) / 1000.0) + " sec.)");
			}
			else
			{
				if ((server.tftpTransferOpened(address) == false) && !tftpStarted)
				{
					bar.setText("Waiting for TFTP process to start (" + ((now - start) / 1000.0) + " sec.)");
				}
				else
				{
					bar.setText("Waiting for TFTP process to finish (" + ((now - start) / 1000.0) + " sec.)");
					tftpStarted = true;
				}
			}

			if ((now - start) > tftpTimeout) throw new Exception("Upgrade Failed");
		}

		// Thread.sleep(10000);

		bar.setText("TFTP process done, file at slot " + index + ", checking if version is valid");

		int valid = getIntValue(upgradeOids.get(Upgrade.VersionValid) + "." + index);

		if (valid == 0) throw new Exception("Upgrade file not valid");

		bar.setText("Setting Active mode");

		set(new OID(upgradeOids.get(Upgrade.VersionActive) + "." + index), new Integer32(1));

		Thread.sleep(1000);

		bar.setText("Rebooting ...");

		set(new OID(MibParser.getOid(Settings.ColdReset)), new Integer32(1));

		start = System.currentTimeMillis();

		while (true)
		{
			Thread.sleep(5000);

			String upgradeVersion = getStringValue(upgradeOids.get(Upgrade.UpgradeVersion));
			UpgradeVersion uv = new UpgradeVersion(upgradeVersion.trim());

			if (uv.equals(fuv)) return;

			// System.out.println("UpgradeVersion = " + upgradeVersion +
			// ", Filename = " + filename);

			long now = System.currentTimeMillis();

			bar.setText("Waiting for reboot process to finish (" + ((now - start) / 1000.0) + " sec.)");

			if ((now - start) > rebootTimeout) throw new Exception("Upgrade Failed");
		}
	}
}

class UpgradeVersion
{
	public int	majorVersion	= 0;
	public int	minorVersion	= 0;
	public int	build			= 0;

	public UpgradeVersion(String version)
	{
		try
		{
			if (version.length() == 0) return;

			String[] split = version.split("\\.|b");

			if (split.length != 3) return;

			majorVersion = Integer.parseInt(split[0].trim());
			minorVersion = Integer.parseInt(split[1].trim());
			build = Integer.parseInt(split[2].trim());

			System.out.println("Converted " + version + " to " + this.toString() + "(" + majorVersion + ","
					+ minorVersion + "," + build + ")");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public UpgradeVersion(File file) throws Exception
	{
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		byte[] bytes = new byte[16];
		bis.read(bytes);
		bis.close();

		majorVersion = bytes[12];
		minorVersion = bytes[13];
		build = ((bytes[15] & 0xFF) << 8) | (bytes[14] & 0xFF);
	}

	@Override
	public String toString()
	{
		return majorVersion + "." + minorVersion + "b" + build;
	}

	@Override
	public boolean equals(Object obj)
	{
		UpgradeVersion other = (UpgradeVersion) obj;

		if (majorVersion != other.majorVersion) return false;

		if (minorVersion != other.minorVersion) return false;

		return build == other.build;
	}
}
