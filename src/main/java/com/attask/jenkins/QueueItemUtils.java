package com.attask.jenkins;

import hudson.EnvVars;
import hudson.model.*;
import hudson.tasks.BuildWrapper;
import hudson.util.DescribableList;

import java.util.List;

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

	public static EnvVars createEnvVarsForProject(AbstractProject abstractProject) {
		EnvVars envVars = new EnvVars();
		if(abstractProject == null) {
			return envVars;
		}

		ParametersDefinitionProperty property = (ParametersDefinitionProperty) abstractProject.getProperty(ParametersDefinitionProperty.class);
		if(property != null) {
			List<String> parameterDefinitionNames = property.getParameterDefinitionNames();
			if(parameterDefinitionNames != null) {
				for (String parameterDefinitionName : parameterDefinitionNames) {
					ParameterDefinition parameterDefinition = property.getParameterDefinition(parameterDefinitionName);
					if(parameterDefinition != null && parameterDefinition instanceof StringParameterDefinition) {
						String value = ((StringParameterDefinition) parameterDefinition).getDefaultValue();
						envVars.put(parameterDefinitionName, value);
					}
				}
			}
		}
		return envVars;
	}

	public static EnvVars createEnvVarsForProject(Queue.Task task) {
		if(task instanceof AbstractProject) {
			return createEnvVarsForProject((AbstractProject)task);
		} else {
			return new EnvVars();
		}
	}
}
