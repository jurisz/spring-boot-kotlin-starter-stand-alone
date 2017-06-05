package com.twino.ls.base;

public interface ReloadableProperties {

	void reload();

	String getProperty(String name);

	String getProperty(String name, String defaultValue);
}
