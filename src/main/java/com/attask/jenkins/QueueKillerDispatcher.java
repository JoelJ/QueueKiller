package com.attask.jenkins;

import hudson.Extension;
import hudson.model.*;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.util.RunList;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Joel Johnson
 * Date: 2/5/13
 * Time: 8:36 AM
 */
@Extension
public class QueueKillerDispatcher extends QueueTaskDispatcher {
	private static Logger log = Logger.getLogger("QueueKiller");

	@Override
	public CauseOfBlockage canTake(Node node, Queue.BuildableItem queueItemToBeRun) {
		if(queueItemToBeRun == null || !(queueItemToBeRun.task instanceof AbstractProject)) {
			if(log.isLoggable(Level.FINEST)) {
				log.finest("not throttling. queueItemToBeRun is not an instance of AbstractProject. " + queueItemToBeRun);
			}
			return null;
		}

		Queue.Task taskToBeRun = queueItemToBeRun.task;
		QueueKillerProperty configuration = QueueItemUtils.getQueueKillerProperty(taskToBeRun);
		if(configuration == null) {
			return null; // Not configured to be throttled, don't throttle it. Null means "good to go". Awkward.
		}

		int numberAllowedToRun = configuration.getNumberAllowedToRun();
		if(numberAllowedToRun <= 0) {
			return null;
		}

		Set<String> checkedValuesSet = configuration.createCheckedValuesSet();
		if(checkedValuesSet.size() <= 0) {
			return null;
		}

		Map<String, StringParameterValue> toRunThrottledParameters = getThrottledParameters(queueItemToBeRun, checkedValuesSet);
		if(toRunThrottledParameters.size() <= 0) {
			return null;
		}

		AbstractProject projectToRun = (AbstractProject)queueItemToBeRun.task;
		List<Run> builds = (List<Run>)projectToRun.getBuilds();

		//The threshold is how many FINISHED builds we run into in a row before we assume there are no more running builds.
		final int maxThreshold = configuration.getPassThreshold();
		int threshold = 0;

		int matches = 0;
		for (Run build : builds) {
			if(build.isBuilding()) {
				threshold = 0;
				Map<String, StringParameterValue> alreadyBuiltThrottledParameters = getThrottledParameters(build, toRunThrottledParameters.keySet());
				if(alreadyBuiltThrottledParameters.size() > 0) {
					for (String key : alreadyBuiltThrottledParameters.keySet()) {
						StringParameterValue alreadyBuiltParameter = alreadyBuiltThrottledParameters.get(key);
						StringParameterValue toRunParameter = toRunThrottledParameters.get(key);
						if(toRunParameter != null && alreadyBuiltParameter != null) {
							if(toRunParameter.value == null) {
								if(alreadyBuiltParameter.value == null) {
									matches++;
									if(matches >= configuration.getNumberAllowedToRun()) {
										return new ThrottleCauseOfBlockage("Only allowed " + configuration.getNumberAllowedToRun() + " for:\n" + configuration.getCheckedValues());
									}
									break;
								}
							} else if(toRunParameter.value.equals(alreadyBuiltParameter.value)) {
								matches++;
								if(matches >= configuration.getNumberAllowedToRun()) {
									return new ThrottleCauseOfBlockage("Only allowed " + configuration.getNumberAllowedToRun() + " for:\n" + configuration.getCheckedValues());
								}
								break;
							}
						}
					}
				}
			} else {
				threshold++;
				if(threshold > maxThreshold) {
					break;
				}
			}
		}


		//This method needs to be really fast. Return null if it's good to go, otherwise return CauseOfBlockage.BecauseLabelIsBusy
		return null;
	}

	private Map<String, StringParameterValue> getThrottledParameters(Actionable item, Collection<String> configuration) {
		Map<String, StringParameterValue> result = new HashMap<String, StringParameterValue>(configuration.size());

		List<ParametersAction> actions = item.getActions(ParametersAction.class);
		for (ParametersAction action : actions) {
			for (String paramName : configuration) {
				ParameterValue parameter = action.getParameter(paramName);
				if(parameter != null && parameter instanceof StringParameterValue) {
					result.put(paramName, (StringParameterValue) parameter);
				}
			}
		}

		return result;
	}

	private static String findReasonToBlock(Run run, Set<String> configuration) {
		if(run == null || run.isBuilding()) {
			return null;
		}
		return "Some reason";
	}

	private static class ThrottleCauseOfBlockage extends CauseOfBlockage {
		private final String reasonToBlock;

		public ThrottleCauseOfBlockage(String reasonToBlock) {
			this.reasonToBlock = reasonToBlock;
		}

		@Override
		public String getShortDescription() {
			return reasonToBlock;
		}
	}
}
