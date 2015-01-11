package com.root;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class Param
{
	private static Properties		properties	= loadProperties();
	private static SimpleDateFormat	sdf			= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static String getValue(String key, String defaultValue)
	{
		synchronized (properties)
		{
			if (properties.containsKey(key) == false)
			{
				setValue(key, defaultValue);
			}

			return properties.getProperty(key);
		}
	}

	public static void setValue(String key, String value)
	{
		synchronized (properties)
		{
			properties.setProperty(key, value);

			try
			{
				properties.storeToXML(new FileOutputStream("settings.xml"),
						"Settings last update at " + sdf.format(Calendar.getInstance().getTime()), "UTF-8");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static Properties loadProperties()
	{
		Properties properties = new Properties();
		try
		{
			properties.loadFromXML(new FileInputStream("settings.xml"));
		}
		catch (Exception e)
		{
		}
		return properties;
	}
}
