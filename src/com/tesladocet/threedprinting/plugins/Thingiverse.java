package com.tesladocet.threedprinting.plugins;

import android.net.Uri;

public class Thingiverse {
	
	private final static String BASE_URL = "https://api.thingiverse.com/";
	private final static String ACCESS_TOKEN = "4cb3de55ca86a3ad4818cf9e06455940";
	
	Uri.Builder uriBuilder;
	String uri;
	
	public Thingiverse() {
		uri = BASE_URL;
	}
	
	private String addToken() {
		return uri + "?access_token=" + ACCESS_TOKEN;
	}
}