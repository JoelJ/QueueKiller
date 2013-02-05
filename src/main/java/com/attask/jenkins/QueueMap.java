package com.attask.jenkins;

import hudson.model.*;
import hudson.model.Queue;

import java.util.*;

/**
 * Complicated map structure so that we don't have to iterate over the queue, config, and parameters every time.
 * It essentially trades memory for time.
 * User: Joel Johnson
 * Date: 2/4/13
 * Time: 9:13 PM
 */
public class QueueMap {
	//          Proj        ParamName    ParamValue
	private Map<String, Map<String, Map<String, List<Queue.Item>>>> map;

	public QueueMap(List<Queue.Item> queue) {
		map = new HashMap<String, Map<String, Map<String, List<Queue.Item>>>>();
		populateMap(queue);
	}

	public void putQueueItem(String projectName, String parameterName, String parameterValue, Queue.Item item) {
		Map<String, Map<String, List<Queue.Item>>> parameterMap = map.get(projectName);
		if(parameterMap == null) {
			parameterMap = new HashMap<String, Map<String, List<Queue.Item>>>();
			map.put(projectName, parameterMap);
		}

		Map<String, List<Queue.Item>> parameterValueMap = parameterMap.get(parameterName);
		if(parameterValueMap == null) {
			parameterValueMap = new HashMap<String, List<Queue.Item>>();
			parameterMap.put(parameterName, parameterValueMap);
		}

		List<Queue.Item> queueItems = parameterValueMap.get(parameterValue);
		if(queueItems == null) {
			queueItems = new ArrayList<Queue.Item>();
			parameterValueMap.put(parameterValue, queueItems);
		}
		queueItems.add(item);
	}

	/**
	 * @param projectName The name of the project
	 * @param parameterName The name of the parameter
	 * @param parameterValue The value of the parameter
	 * @return Gets the queue items for a specific project/parameter/parameterValue combination.
	 * 			The result is sorted by the time they have been in the queue.
	 */
	public List<Queue.Item> getQueueItems(String projectName, String parameterName, String parameterValue) {
		Map<String, Map<String, List<Queue.Item>>> parameterMap = map.get(projectName);
		if(parameterMap == null) {
			return null;
		}
		Map<String, List<Queue.Item>> parameterValueMap = parameterMap.get(parameterName);
		if(parameterValueMap == null) {
			return null;
		}

		List<Queue.Item> items = parameterValueMap.get(parameterValue);
		if(items != null) {
			Collections.sort(items, new Comparator<Queue.Item>() {
				public int compare(Queue.Item i1, Queue.Item i2) {
					return ((Long)i1.getInQueueSince()).compareTo(i2.getInQueueSince());
				}
			});
		}
		return items;
	}

	private void populateMap(List<Queue.Item> queue) {
		for (Queue.Item queueItem : queue) {
			Queue.Task task = queueItem.task;
			String projectName = task.getName();
			if(task instanceof Actionable) {
				List<ParametersAction> actions = queueItem.getActions(ParametersAction.class);
				for (ParametersAction action : actions) {
					List<ParameterValue> parameters = action.getParameters();
					for (ParameterValue parameter : parameters) {
						if(parameter instanceof StringParameterValue) {
							String paramName = parameter.getName();
							String paramValue = ((StringParameterValue) parameter).value;
							putQueueItem(projectName, paramName, paramValue, queueItem);
						}
					}
				}
			}
		}
	}
}
