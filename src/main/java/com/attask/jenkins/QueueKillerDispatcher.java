package com.attask.jenkins;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;

/**
 * User: Joel Johnson
 * Date: 2/5/13
 * Time: 8:36 AM
 */
@Extension
public class QueueKillerDispatcher extends QueueTaskDispatcher {
	@Override
	public CauseOfBlockage canTake(Node node, Queue.BuildableItem item) {
		//TODO: Get the QueueKillerProperty
		//TODO: Check how many builds are running that match the property
		//TODO: Block if there are too many

		//This method needs to be really fast. Return null if it's good to go, otherwise return CauseOfBlockage.BecauseLabelIsBusy
		return null;
	}
}
