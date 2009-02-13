package com.googlecode.hibernate.audit.extension.converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DefaultPropertyValueConverter implements PropertyValueConverter {
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'.'SSSZ");

	public String toString(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Calendar) {
			return DATE_FORMAT.format(((Calendar) obj).getTime());
		}

		if (obj instanceof Date) {
			return DATE_FORMAT.format((Date) obj);
		}

		return obj.toString();
	}

	public Object valueOf(Class type, String str) {
		if (Integer.class.equals(type)) {
			return Integer.valueOf(str);
		} else if (Long.class.equals(type)) {
			return Long.valueOf(str);
		} else if (Double.class.equals(type)) {
			return Double.valueOf(str);
		} else if (Float.class.equals(type)) {
			return Float.valueOf(str);
		} else if (Byte.class.equals(type)) {
			return Byte.valueOf(str);
		} else if (Short.class.equals(type)) {
			return Short.valueOf(str);
		} else if (Boolean.class.equals(type)) {
			return Boolean.valueOf(str);
		} else if (Calendar.class.equals(type)) {
			try {
				DateFormat calendarFormat = (DateFormat) DATE_FORMAT.clone();
				calendarFormat.setLenient(false);
				calendarFormat.parse(str);
				return calendarFormat.getCalendar();
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		} else if (Date.class.equals(type)) {
			try {
				return (Date) DATE_FORMAT.parse(str);
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		}

		try {
			Constructor constructor = type
					.getConstructor(new Class[] { String.class });
			if (constructor != null) {
				return constructor.newInstance(str);
			}
		} catch (SecurityException e1) {
		} catch (IllegalArgumentException e1) {
		} catch (NoSuchMethodException e1) {
		} catch (InstantiationException e1) {
		} catch (IllegalAccessException e1) {
		} catch (InvocationTargetException e1) {
		}

		try {
			Method valueOf = type.getMethod("valueOf", String.class);
			if (valueOf != null) {
				return valueOf.invoke(null, str);
			}
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

		throw new IllegalArgumentException(
				"Unable to construct new instance of " + type + " from string:"
						+ str);
	}
}
