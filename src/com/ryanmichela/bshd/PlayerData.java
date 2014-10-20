package com.ryanmichela.bshd;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
	public Map<String, Boolean> options;
	
	public PlayerData() {
		options = new HashMap<String, Boolean>();
		options.put("bookin", false);
		options.put("bookout", false);
	}
}