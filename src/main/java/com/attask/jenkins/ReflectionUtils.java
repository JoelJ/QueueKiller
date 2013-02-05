package com.attask.jenkins;

import com.google.common.collect.ImmutableList;
import hudson.model.*;

import java.lang.reflect.Field;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 2/4/13
 * Time: 9:13 PM
 */
public class ReflectionUtils {
	public static void setValue(StringParameterValue parameterToChange, String parameterToCopyValue) {
		try {
			//This is safe because it's a public final, so it won't ever change because it'll break the interface anyway.
			Field value = parameterToChange.getClass().getField("value");
			value.setAccessible(true);
			value.set(parameterToChange, parameterToCopyValue);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("This shouldn't happen since setAccessible is set to true", e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("This shouldn't happen because value is a public field", e);
		}
	}
}
