package com.tesladocet.threedprinting.printers;

abstract public class Link {
	public static final int WIFI = 0;
	public static final int USB = 1;
	public static final int SERIAL = 2;
	public static final int BLUETOOTH = 3;
	
	abstract void send(byte[] gCode);
}
