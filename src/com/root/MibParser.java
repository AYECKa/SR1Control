package com.root;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.MibLoaderLog.LogEntry;
import net.percederberg.mibble.value.ObjectIdentifierValue;

public class MibParser
{
	private static Object									lock	= new Object();

	private static HashMap<String, ObjectIdentifierValue>	mibMap	= loadMib();

	private static HashMap<String, ObjectIdentifierValue> loadMib()
	{
		try
		{
			MibLoader loader = new MibLoader();
			File mibFile = new File(Param.getValue("MIB", "SR1c.mib"));
			Mib mib = loader.load(mibFile);
			return extractOids(mib);
		}
		catch (MibLoaderException mle)
		{
			MibLoaderLog log = mle.getLog();
			Iterator<?> it = log.entries();

			while (it.hasNext())
			{
				LogEntry logEntry = (LogEntry) it.next();
				System.err.println(logEntry.getMessage() + " => " + logEntry.getFile() + " line "
						+ logEntry.getLineNumber());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private static HashMap<String, ObjectIdentifierValue> extractOids(Mib mib)
	{
		HashMap<String, ObjectIdentifierValue> map = new HashMap<String, ObjectIdentifierValue>();
		Iterator<?> iter = mib.getAllSymbols().iterator();
		MibSymbol symbol;
		ObjectIdentifierValue value;

		while (iter.hasNext())
		{
			symbol = (MibSymbol) iter.next();
			value = extractOid(symbol);
			if (value != null)
			{
				map.put(symbol.getName(), value);
			}
		}
		return map;
	}

	private static ObjectIdentifierValue extractOid(MibSymbol symbol)
	{
		MibValue value;

		if (symbol instanceof MibValueSymbol)
		{
			value = ((MibValueSymbol) symbol).getValue();
			if (value instanceof ObjectIdentifierValue) { return (ObjectIdentifierValue) value; }
		}
		return null;
	}

	public static String getOid(String name)
	{
		synchronized (lock)
		{
			ObjectIdentifierValue value = mibMap.get(name);

			if (value == null)
			{
				System.err.println("Cannot find OID for " + name);
			}

			return value.toString();
		}
	}

	public static void reload()
	{
		synchronized (lock)
		{
			mibMap = loadMib();
		}
	}
}
