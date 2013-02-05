package com.attask.jenkins;

import hudson.model.BuildableItemWithBuildWrappers;
import hudson.model.Descriptor;
import hudson.model.Queue;
import hudson.tasks.BuildWrapper;
import hudson.util.DescribableList;

/**
 * User: Joel Johnson
 * Date: 2/5/13
 * Time: 10:17 AM
 */
public class QueueItemUtils {
	public static QueueKillerProperty getQueueKillerProperty(Queue.Task task) {
		if(task != null && task instanceof BuildableItemWithBuildWrappers) {
			BuildableItemWithBuildWrappers toBeQueuedProject = (BuildableItemWithBuildWrappers)task;
			DescribableList<BuildWrapper,Descriptor<BuildWrapper>> buildWrappersList = toBeQueuedProject.getBuildWrappersList();

			for (BuildWrapper buildWrapper : buildWrappersList) {
				if(buildWrapper instanceof QueueKillerProperty) {
					return (QueueKillerProperty) buildWrapper;
				}
			}
		}
		return null;
	}
}
