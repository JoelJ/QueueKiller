package com.attask.jenkins;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

/**
 * User: Joel Johnson
 * Date: 2/4/13
 * Time: 10:49 PM
 */
public class QueueKillerProperty extends BuildWrapper {
	private final int numberAllowedInQueue;
	private final int numberAllowedToRun;
	private final String checkedValues;
	private final String valuesToCopy;
	private final int passThreshold;

	private final int maxTotalRuns;

	@DataBoundConstructor
	public QueueKillerProperty(int numberAllowedInQueue, int numberAllowedToRun, String checkedValues, String valuesToCopy, int passThreshold, int maxTotalRuns) {
		this.numberAllowedInQueue = numberAllowedInQueue <= 0 ? 1 : numberAllowedInQueue;
		this.numberAllowedToRun = numberAllowedToRun < 0 ? 0 : numberAllowedToRun; //Zero means infinite
		this.checkedValues = fixNull(checkedValues);
		this.valuesToCopy = fixNull(valuesToCopy);
		this.passThreshold = passThreshold <= 0 ? 10 : passThreshold;
		this.maxTotalRuns = maxTotalRuns;
	}

	public int getNumberAllowedInQueue() {
		return numberAllowedInQueue;
	}

	public int getNumberAllowedToRun() {
		return numberAllowedToRun;
	}

	public String getCheckedValues() {
		return checkedValues;
	}

	public String getValuesToCopy() {
		return valuesToCopy;
	}

	public int getPassThreshold() {
		return passThreshold;
	}

	public int getMaxTotalRuns() {
		return maxTotalRuns;
	}

	public Set<String> createCheckedValuesSet() {
		return createListFromString(getCheckedValues());
	}

	public Set<String> createValuesToCopySet() {
		return createListFromString(getValuesToCopy());
	}

	private Set<String> createListFromString(String source) {
		ImmutableSet.Builder<String> builder = ImmutableSet.builder();
		Scanner scanner = new Scanner(source);
		try {
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				builder.add(line);
			}
		} finally {
			scanner.close();
		}
		return builder.build();
	}

	private static String fixNull(String value) {
		if(value == null) {
			return "";
		}
		return value;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				return true;
			}
		};
	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public String getDisplayName() {
			return "Queue Throttle";
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return item instanceof BuildableItemWithBuildWrappers;
		}
	}
}
