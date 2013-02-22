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
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		for (Action a : getActions(actionable)) {
			if (type.isInstance(a)) {
				builder.add(type.cast(a));
			}
		}
		return builder.build();
	}

	public static <T extends Action> T getAction(Actionable actionable, Class<T> type) {
		List<T> actions = getActions(actionable, type);
		if(actions == null || actions.isEmpty()) {
			return null;
		}
		return actions.get(0);
	}
}
