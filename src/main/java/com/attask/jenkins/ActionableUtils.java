package com.attask.jenkins;

import com.google.common.collect.ImmutableList;
import hudson.model.Action;
import hudson.model.Actionable;

import java.util.Collections;
import java.util.List;

/**
 * The Actionable.getActions uses the synchronize a little too liberally. This is to circumvent that locking.
 * User: Joel Johnson
 * Date: 2/12/13
 * Time: 5:05 PM
 */
public class ActionableUtils {
	public static List<Action> getActions(Actionable actionable) {
		List<Action> actions = ReflectionUtils.getValue(Actionable.class, actionable, "actions");
		if(actions == null) {
			actions = Collections.emptyList();
		}
		return actions;
	}

	public static <T extends Action> List<T> getActions(Actionable actionable, Class<T> type) {
		List<Action> actions = getActions(actionable);
		return filterActions(actions, type);
	}

	public static <T extends Action> List<T> filterActions(List<Action> actions, Class<T> type) {
		ImmutableList.Builder<T> builder = ImmutableList.builder();

		for (Action a : actions) {
			if (type.isInstance(a)) {
				builder.add(type.cast(a));
			}
		}
		return builder.build();
	}

	public static <T extends Action> T filterAction(List<Action> actions, Class<T> type) {
		for (Action a : actions) {
			if (type.isInstance(a)) {
				return type.cast(a);
			}
		}

		return null;
	}
}
