package com.attask.jenkins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import hudson.model.Queue;

import java.util.*;

/**
 * Prevents too many throttled items from being in the queue.
 * <p/>
 * Checks the parameters used to start a build and limits the number of builds in the queue that match user-specified values.
 * It will always keep the newest builds in the queue.
 * <p/>
 * User: Joel Johnson
 * Date: 2/4/13
 * Time: 7:37 PM
 */
@Extension
public class QueueKiller extends Queue.QueueDecisionHandler {
	public boolean shouldSchedule(Queue.Task toBeQueuedTask, List<Action> toBeQueuedActions) {
		QueueKillerProperty property = QueueItemUtils.getQueueKillerProperty(toBeQueuedTask);
		if (property == null) {
			return true;
		}

		// Originally I designed this to take multiple configurations per project, but I got lazy.
		//		So this will stay as a list to make it easy to make that change in the future.
		List<QueueKillerProperty> configurationList = Arrays.asList(property);

		QueueMap queueMap = QueueMap.getQueue();
		Map<String, StringParameterValue> toBeQueuedParameters = createParameterMap(toBeQueuedActions);

		EnvVars envVars = QueueItemUtils.createEnvVarsForProject(toBeQueuedTask);

		for (QueueKillerProperty configuration : configurationList) {
			Set<String> checkedValuesSet = configuration.createCheckedValuesSet();
			Set<String> valuesToCopySet = configuration.createValuesToCopySet();

			for (String checkedParameterName : checkedValuesSet) {
				StringParameterValue parameter = toBeQueuedParameters.get(checkedParameterName);
				if (parameter != null) {
					List<Queue.Item> queueItems = queueMap.getQueueItems(toBeQueuedTask.getName(), checkedParameterName, parameter.value);
					if (queueItems != null && queueItems.size() >= configuration.expandNumberAllowedInQueue(envVars)) {
						List<StringParameterValue> parametersToCopy = findParametersToCopy(toBeQueuedActions, valuesToCopySet);
						Queue.Item alreadyQueued = queueItems.get(0); //The list is sorted. Top of the list is the one to be removed
						copyParameters(alreadyQueued, parametersToCopy);

						return false;
					}
				}
			}
		}

		return true;
	}

	private void copyParameters(Actionable actionable, List<StringParameterValue> parametersToCopy) {
		ParametersAction action = actionable.getAction(ParametersAction.class);

		for (StringParameterValue parameterToCopy : parametersToCopy) {
			String newValue = parameterToCopy.value;

			ParameterValue parameterToHack = action.getParameter(parameterToCopy.getName());
			if(parameterToHack instanceof StringParameterValue) {
				ReflectionUtils.setValue((StringParameterValue) parameterToHack, newValue);
			}
		}
	}


	private List<StringParameterValue> findParametersToCopy(List<Action> actions, Set<String> valuesToCopySet) {
		ImmutableList.Builder<StringParameterValue> builder = ImmutableList.builder();
		ParametersAction action = ActionableUtils.filterAction(actions, ParametersAction.class);
		if (action != null) {
			for (String valueToCopy : valuesToCopySet) {
				ParameterValue parameter = action.getParameter(valueToCopy);
				if (parameter != null && parameter instanceof StringParameterValue) {
					builder.add((StringParameterValue) parameter);
				}
			}
		}
		return builder.build();
	}

	private static Map<String, StringParameterValue> createParameterMap(Iterable<? extends Action> actions) {
		ImmutableMap.Builder<String, StringParameterValue> builder = ImmutableMap.builder();

		if (actions != null) {
			for (Action action : actions) {
				if (action instanceof ParametersAction) {
					for (ParameterValue parameterValue : ((ParametersAction) action).getParameters()) {
						if (parameterValue instanceof StringParameterValue) {
							builder.put(parameterValue.getName(), (StringParameterValue) parameterValue);
						}
					}
				}
			}
		}

		return builder.build();
	}
}
