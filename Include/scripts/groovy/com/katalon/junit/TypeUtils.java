package com.katalon.junit;

import java.util.Map;

import org.apache.poi.ss.formula.functions.T;

import com.google.common.collect.ImmutableMap;

public class TypeUtils {
	public static final Map<Class<?>, Class<?>> TYPES_MAP = new ImmutableMap.Builder<Class<?>, Class<?>>()
			.put(boolean.class, Boolean.class).put(Boolean.class, boolean.class)
			.put(byte.class, Byte.class).put(Byte.class, byte.class)
			.put(char.class, Character.class).put(Character.class, char.class)
			.put(double.class, Double.class).put(Double.class, double.class)
			.put(float.class, Float.class).put(Float.class, float.class)
			.put(int.class, Integer.class).put(Integer.class, int.class)
			.put(long.class, Long.class).put(Long.class, long.class)
			.put(short.class, Short.class).put(Short.class, short.class)
			.put(void.class, Void.class).put(Void.class, void.class)
			.build();

	@SuppressWarnings({ "unchecked", "hiding" })
	public static <T> Class<T> toBoxed(Class<T> clazz) {
		Class<T> targetClass = clazz.isPrimitive() ? (Class<T>) TYPES_MAP.get(clazz) : clazz;
		return targetClass != null ? targetClass : clazz;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	public static <T> Class<T> toUnboxed(Class<T> clazz) {
		Class<T> targetClass = clazz.isPrimitive() ? clazz : (Class<T>) TYPES_MAP.get(clazz);
		return targetClass != null ? targetClass : clazz;
	}

	@SuppressWarnings("hiding")
	public static <T> Class<T> toggleBoxed(Class<T> clazz) {
		return clazz.isPrimitive() ? toBoxed(clazz) : toUnboxed(clazz);
	}
}