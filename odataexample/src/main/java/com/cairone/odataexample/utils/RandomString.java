package com.cairone.odataexample.utils;

import java.security.SecureRandom;

public class RandomString {

	public static final String generate( int len ){
		
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder( len );
		
		for( int i = 0; i < len; i++ ) sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		
		return sb.toString();
	}
}
