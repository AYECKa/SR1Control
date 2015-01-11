package com.root;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.net.tftp.TFTPServer;

public class SupportDump
{
	private Properties	properties	= new Properties();

	public SupportDump()
	{
		try
		{
			properties.loadFromXML(new FileInputStream("support.xml"));
		}
		catch (Exception e)
		{
			properties.setProperty("SN", ".1.3.6.1.4.1.27928.101.3.3.0");
			properties.setProperty("production code", ".1.3.6.1.4.1.27928.101.3.4.0");
			properties.setProperty("permanentStorageDevice", ".1.3.6.1.4.1.27928.101.3.21.0");
			properties.setProperty("rxChipsetModel", ".1.3.6.1.4.1.27928.101.3.22.0");
			properties.setProperty("RX1 Freq", ".1.3.6.1.4.1.27928.101.1.1.1.1.1.0");
			properties.setProperty("RX1 Acq BW", ".1.3.6.1.4.1.27928.101.1.1.1.1.2.0");
			properties.setProperty("RX1 Standard", ".1.3.6.1.4.1.27928.101.1.1.1.2.1.0");
			properties.setProperty("RX1 SR", ".1.3.6.1.4.1.27928.101.1.1.1.2.2.0");
			properties.setProperty("RX1 LNB power", ".1.3.6.1.4.1.27928.101.1.1.1.3.1.0");
			properties.setProperty("RX1 22K", ".1.3.6.1.4.1.27928.101.1.1.1.3.3.0");
			properties.setProperty("Rx1 Tuner status", ".1.3.6.1.4.1.27928.101.1.1.4.1.0");
			properties.setProperty("RX1 FreqOffset", ".1.3.6.1.4.1.27928.101.1.1.4.2.0");
			properties.setProperty("RX1 Power Level", ".1.3.6.1.4.1.27928.101.1.1.4.3.0");
			properties.setProperty("RX1 Esno", ".1.3.6.1.4.1.27928.101.1.1.4.4.0");
			properties.setProperty("RX1 BER", ".1.3.6.1.4.1.27928.101.1.1.4.5.0");
			properties.setProperty("RX1 DemodStatus", ".1.3.6.1.4.1.27928.101.1.1.4.11.0");
			properties.setProperty("RX1 Transport status", ".1.3.6.1.4.1.27928.101.1.1.4.13.0");
			properties.setProperty("RX1 NumFilters", ".1.3.6.1.4.1.27928.101.1.1.5.0");
			properties.setProperty("Active RX", ".1.3.6.1.4.1.27928.101.1.3.1.0");
			try
			{
				properties.storeToXML(new FileOutputStream("support.xml"), "Support Dump Details", "UTF-8");
			}
			catch (FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	public void dump(String deviceAddress, String readCommunity, String writeCommunity, TFTPServer server)
	{
		BufferedWriter writer = null;

		try
		{
			DeviceInterface deviceInterface = DeviceInterface.getInstance();

			writer = new BufferedWriter(new FileWriter("SupportDump.txt"));

			writer.write("Software Version = " + deviceInterface.getSoftwareVersion() + "\n");
			writer.write("Hardware Version = " + deviceInterface.getHardwareVersion() + "\n");
			writer.write("FPGA Version = " + deviceInterface.getFpgaVersion() + "\n");

			for (Object obj : properties.keySet())
			{
				String key = (String) obj;
				String oid = properties.getProperty(key);
				if (oid != null)
				{
					String value = deviceInterface.getStringValue(oid);
					writer.write(key + " = " + value + "\n");
					writer.flush();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (writer != null) try
			{
				writer.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
