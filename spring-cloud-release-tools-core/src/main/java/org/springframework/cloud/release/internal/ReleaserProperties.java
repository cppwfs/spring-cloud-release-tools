/*
 *  Copyright 2013-2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.springframework.cloud.release.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.SerializationUtils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * Since, we are making a deep copy of this object, remember to have all the
 * nested classes to implement the Serializable interface.
 *
 * @author Marcin Grzejszczak
 */
@ConfigurationProperties("releaser")
public class ReleaserProperties implements Serializable {

	/**
	 * By default Releaser assumes running the program from the current working directory.
	 * If you want to change this behaviour - just change this value.
	 */
	private String workingDir;

	private Git git = new Git();

	private Pom pom = new Pom();

	private Maven maven = new Maven();

	private Gradle gradle = new Gradle();

	private Sagan sagan = new Sagan();

	private Template template = new Template();

	/**
	 * Project name to its version - overrides all versions
	 * retrieved from a repository like Spring Cloud Release
	 */
	private Map<String, String> fixedVersions = new LinkedHashMap<>();

	private MetaRelease metaRelease = new MetaRelease();

	public static class MetaRelease implements Serializable {
		/**
		 * Are we releasing the whole suite of apps or only one?
		 */
		private boolean enabled = false;

		/**
		 * Name of the release train project
		 */
		private String releaseTrainProjectName = "spring-cloud-release";

		/**
		 * The URL of the Git organization. We'll append each project's
		 * name to it
		 */
		private String gitOrgUrl = "https://github.com/spring-cloud";

		/**
		 * Names of projects to skip deployment for meta-release
		 */
		private List<String> projectsToSkip = new ArrayList<String>() {
			{
				this.add("spring-boot");
				this.add("spring-cloud-stream");
				this.add("spring-cloud-task");
			}
		};

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getGitOrgUrl() {
			return this.gitOrgUrl;
		}

		public void setGitOrgUrl(String gitOrgUrl) {
			this.gitOrgUrl = gitOrgUrl;
		}

		public String getReleaseTrainProjectName() {
			return this.releaseTrainProjectName;
		}

		public void setReleaseTrainProjectName(String releaseTrainProjectName) {
			this.releaseTrainProjectName = releaseTrainProjectName;
		}

		public List<String> getProjectsToSkip() {
			return this.projectsToSkip;
		}

		public void setProjectsToSkip(List<String> projectsToSkip) {
			this.projectsToSkip = projectsToSkip;
		}

		@Override
		public String toString() {
			return "MetaRelease{" +
					"enabled=" + this.enabled +
					", releaseTrainProjectName='" + this.releaseTrainProjectName + '\'' +
					", gitOrgUrl='" + this.gitOrgUrl + '\'' +
					", projectsToSkip=" + this.projectsToSkip +
					'}';
		}
	}

	public static class Git implements Serializable {

		/**
		 * URL to Spring Cloud Release Git repository
		 */
		private String releaseTrainBomUrl = "https://github.com/spring-cloud/spring-cloud-release";

		/**
		 * URL to the documentation Git repository
		 */
		private String documentationUrl = "https://github.com/spring-cloud/spring-cloud-static";

		/**
		 * Branch to check out for the documentation project
		 */
		private String documentationBranch = "gh-pages";

		/**
		 * If {@code false}, will not update the documentation repository.
		 */
		private boolean updateDocumentationRepo = true;

		/**
		 * Where should the Spring Cloud Release repo get cloned to. If {@code null} defaults to a temporary directory
		 */
		private String cloneDestinationDir;

		/**
		 * If {@code true} then should fill the map of versions from Git. If {@code false} then picks fixed versions
		 */
		private boolean fetchVersionsFromGit = true;

		/**
		 * GitHub OAuth token to be used to interact with GitHub repo
		 */
		private String oauthToken = "";

		/**
		 * Optional Git username. If not passed keys will be used for authentication
		 */
		private String username;

		/**
		 * Optional Git password. If not passed keys will be used for authentication
		 */
		private String password;

		/**
		 * In order not to iterate endlessly over milestones we introduce a threshold of milestones
		 * that we will go through to find the matching milestone
		 */
		private Integer numberOfCheckedMilestones = 10;

		/**
		 * If set to {@code false}, will not update Spring Guides for a release train.
		 */
		private boolean updateSpringGuides = true;

		public String getReleaseTrainBomUrl() {
			return this.releaseTrainBomUrl;
		}

		public void setReleaseTrainBomUrl(String releaseTrainBomUrl) {
			this.releaseTrainBomUrl = releaseTrainBomUrl;
		}

		public String getDocumentationUrl() {
			return this.documentationUrl;
		}

		public void setDocumentationUrl(String documentationUrl) {
			this.documentationUrl = documentationUrl;
		}

		public String getDocumentationBranch() {
			return this.documentationBranch;
		}

		public void setDocumentationBranch(String documentationBranch) {
			this.documentationBranch = documentationBranch;
		}

		public boolean isUpdateDocumentationRepo() {
			return this.updateDocumentationRepo;
		}

		public void setUpdateDocumentationRepo(boolean updateDocumentationRepo) {
			this.updateDocumentationRepo = updateDocumentationRepo;
		}

		public String getCloneDestinationDir() {
			return this.cloneDestinationDir;
		}

		public void setCloneDestinationDir(String cloneDestinationDir) {
			this.cloneDestinationDir = cloneDestinationDir;
		}

		public String getOauthToken() {
			return this.oauthToken;
		}

		public void setOauthToken(String oauthToken) {
			this.oauthToken = oauthToken;
		}

		public String getUsername() {
			return this.username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return this.password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Integer getNumberOfCheckedMilestones() {
			return this.numberOfCheckedMilestones;
		}

		public void setNumberOfCheckedMilestones(Integer numberOfCheckedMilestones) {
			this.numberOfCheckedMilestones = numberOfCheckedMilestones;
		}

		public boolean isFetchVersionsFromGit() {
			return this.fetchVersionsFromGit;
		}

		public void setFetchVersionsFromGit(boolean fetchVersionsFromGit) {
			this.fetchVersionsFromGit = fetchVersionsFromGit;
		}

		public boolean isUpdateSpringGuides() {
			return this.updateSpringGuides;
		}

		public void setUpdateSpringGuides(boolean updateSpringGuides) {
			this.updateSpringGuides = updateSpringGuides;
		}

		@Override
		public String toString() {
			return "Git{" +
					"releaseTrainBomUrl='" + this.releaseTrainBomUrl + '\'' +
					", documentationUrl='" + this.documentationUrl + '\'' +
					", documentationBranch='" + this.documentationBranch + '\'' +
					", updateDocumentationRepo=" + this.updateDocumentationRepo +
					", cloneDestinationDir='" + this.cloneDestinationDir + '\'' +
					", fetchVersionsFromGit=" + this.fetchVersionsFromGit +
					", oauthToken='" + this.oauthToken + '\'' +
					", numberOfCheckedMilestones=" + this.numberOfCheckedMilestones +
					", updateSpringGuides=" + this.updateSpringGuides +
					'}';
		}
	}

	public static class Pom implements Serializable {

		/**
		 * Which branch of release train BOM should be checked out. Defaults to {@code master}
		 */
		private String branch = "master";

		/**
		 * Subfolder of the pom that contains the {@code spring-boot-starer-parent} dependency
		 */
		private String pomWithBootStarterParent = "spring-cloud-starter-parent/pom.xml";

		/**
		 * Subfolder of the pom that contains the versions for the release train
		 */
		private String thisTrainBom = "spring-cloud-dependencies/pom.xml";

		/**
		 * The pattern to match a version property in a BOM
		 */
		private String bomVersionPattern = "^(spring-cloud-.*)\\.version$";

		/**
		 * List of regular expressions of ignored poms. Defaults to test projects and samples.
		 */
		@SuppressWarnings("unchecked")
		private List<String> ignoredPomRegex = Arrays.asList(
				"^.*\\.git/.*$",
				"^.*spring-cloud-contract-maven-plugin/src/test/projects/.*$",
				"^.*spring-cloud-contract-maven-plugin/target/.*$",
				"^.*samples/standalone/[a-z]+/.*$"
		);

		public String getBranch() {
			return this.branch;
		}

		public void setBranch(String branch) {
			this.branch = branch;
		}

		public List<String> getIgnoredPomRegex() {
			return this.ignoredPomRegex;
		}

		public void setIgnoredPomRegex(List<String> ignoredPomRegex) {
			this.ignoredPomRegex = ignoredPomRegex;
		}

		public String getPomWithBootStarterParent() {
			return this.pomWithBootStarterParent;
		}

		public void setPomWithBootStarterParent(String pomWithBootStarterParent) {
			this.pomWithBootStarterParent = pomWithBootStarterParent;
		}

		public String getThisTrainBom() {
			return this.thisTrainBom;
		}

		public void setThisTrainBom(String thisTrainBom) {
			this.thisTrainBom = thisTrainBom;
		}

		public String getBomVersionPattern() {
			return this.bomVersionPattern;
		}

		public void setBomVersionPattern(String bomVersionPattern) {
			this.bomVersionPattern = bomVersionPattern;
		}

		@Override
		public String toString() {
			return "Pom{" +
					"branch='" + this.branch + '\'' +
					", pomWithBootStarterParent='" + this.pomWithBootStarterParent + '\'' +
					", thisTrainBom='" + this.thisTrainBom + '\'' +
					", bomVersionPattern='" + this.bomVersionPattern + '\'' +
					", ignoredPomRegex=" + this.ignoredPomRegex +
					'}';
		}
	}

	public static class Maven implements Serializable {

		/**
		 * Command to be executed to build the project
		 */
		private String buildCommand = "./mvnw clean install -B -Pdocs {{systemProps}}";

		/**
		 * Command to be executed to deploy a built project
		 */
		private String deployCommand = "./mvnw deploy -DskipTests -B -Pfast,deploy {{systemProps}}";

		/**
		 * Command to be executed to publish documentation. If present "{{version}}" will be replaced by the
		 * provided version
		 */
		private String[] publishDocsCommands = {
				"mkdir -p target",
				"wget https://raw.githubusercontent.com/spring-cloud/spring-cloud-build/master/docs/src/main/asciidoc/ghpages.sh -O target/gh-pages.sh",
				"chmod +x target/gh-pages.sh",
				"./target/gh-pages.sh -v {{version}} -c"
		};

		public static final String SYSTEM_PROPS_PLACEHOLDER = "{{systemProps}}";

		/**
		 * Additional system properties that should be passed to the build / deploy commands.
		 * If present in other commands "{{systemProps}}" will be substituted with this property.
		 */
		private String systemProperties = "";

		/**
		 * Max wait time in minutes for the process to finish
		 */
		private long waitTimeInMinutes = 20;

		public String getBuildCommand() {
			return this.buildCommand;
		}

		public void setBuildCommand(String buildCommand) {
			this.buildCommand = buildCommand;
		}

		public long getWaitTimeInMinutes() {
			return this.waitTimeInMinutes;
		}

		public void setWaitTimeInMinutes(long waitTimeInMinutes) {
			this.waitTimeInMinutes = waitTimeInMinutes;
		}

		public String getDeployCommand() {
			return deployCommand;
		}

		public void setDeployCommand(String deployCommand) {
			this.deployCommand = deployCommand;
		}

		public String[] getPublishDocsCommands() {
			return this.publishDocsCommands;
		}

		public void setPublishDocsCommands(String[] publishDocsCommands) {
			this.publishDocsCommands = publishDocsCommands;
		}

		public String getSystemProperties() {
			return this.systemProperties;
		}

		public void setSystemProperties(String systemProperties) {
			this.systemProperties = systemProperties;
		}

		@Override public String toString() {
			return "Maven{" + "buildCommand='" + this.buildCommand + '\'' + ", deployCommand='"
					+ this.deployCommand + '\'' + ", publishDocsCommands=" + Arrays
					.toString(this.publishDocsCommands) + ", waitTimeInMinutes="
					+ this.waitTimeInMinutes + '}';
		}
	}

	public static class Gradle implements Serializable {

		/**
		 * A mapping that should be applied to {@code gradle.properties} in order
		 * to perform a substitution of properties. The mapping is from a property
		 * inside {@code gradle.properties} to the projects name. Example
		 *
		 * In {@code gradle.properties} you have {@code verifierVersion=1.0.0} . You
		 * want this property to get updated with the value of {@code spring-cloud-contract}
		 * version. Then it's enough to do the mapping like this for this Releaser's property:
		 * {@code verifierVersion=spring-cloud-contract}
		 */
		private Map<String, String> gradlePropsSubstitution = new HashMap<>();

		/**
		 * List of regular expressions of ignored gradle props.
		 * Defaults to test projects and samples.
		 */
		@SuppressWarnings("unchecked")
		private List<String> ignoredGradleRegex = Arrays.asList(
				"^.*spring-cloud-contract-maven-plugin/src/test/projects/.*$",
				"^.*spring-cloud-contract-maven-plugin/target/.*$",
				"^.*samples/standalone/[a-z]+/.*$"
		);

		public Map<String, String> getGradlePropsSubstitution() {
			return this.gradlePropsSubstitution;
		}

		public void setGradlePropsSubstitution(
				Map<String, String> gradlePropsSubstitution) {
			this.gradlePropsSubstitution = gradlePropsSubstitution;
		}

		public List<String> getIgnoredGradleRegex() {
			return this.ignoredGradleRegex;
		}

		public void setIgnoredGradleRegex(List<String> ignoredGradleRegex) {
			this.ignoredGradleRegex = ignoredGradleRegex;
		}

		@Override public String toString() {
			return "Gradle{" + "gradlePropsSubstitution=" + this.gradlePropsSubstitution
					+ ", ignoredGradleRegex=" + this.ignoredGradleRegex + '}';
		}
	}

	public static class Sagan implements Serializable {
		/**
		 * URL to the Sagan API
		 */
		private String baseUrl = "https://spring.io";

		public String getBaseUrl() {
			return this.baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		@Override public String toString() {
			return "Sagan{" + "baseUrl='" + this.baseUrl + '\'' + '}';
		}
	}

	public static class Template implements Serializable {
		/**
		 * Folder in which blog, email etc. templates are stored
		 */
		private String templateFolder = "cloud";

		public String getTemplateFolder() {
			return this.templateFolder;
		}

		public void setTemplateFolder(String templateFolder) {
			this.templateFolder = templateFolder;
		}

		@Override
		public String toString() {
			return "Template{" +
					"templateFolder='" + this.templateFolder + '\'' +
					'}';
		}
	}

	public String getWorkingDir() {
		return StringUtils.hasText(this.workingDir) ?
				this.workingDir : System.getProperty("user.dir");
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public Git getGit() {
		return this.git;
	}

	public void setGit(Git git) {
		this.git = git;
	}

	public Pom getPom() {
		return this.pom;
	}

	public void setPom(Pom pom) {
		this.pom = pom;
	}

	public Maven getMaven() {
		return this.maven;
	}

	public void setMaven(Maven maven) {
		this.maven = maven;
	}

	public Gradle getGradle() {
		return this.gradle;
	}

	public void setGradle(Gradle gradle) {
		this.gradle = gradle;
	}

	public Map<String, String> getFixedVersions() {
		return this.fixedVersions;
	}

	public void setFixedVersions(Map<String, String> fixedVersions) {
		this.fixedVersions = fixedVersions;
	}

	public MetaRelease getMetaRelease() {
		return this.metaRelease;
	}

	public void setMetaRelease(MetaRelease metaRelease) {
		this.metaRelease = metaRelease;
	}

	public Sagan getSagan() {
		return this.sagan;
	}

	public void setSagan(Sagan sagan) {
		this.sagan = sagan;
	}

	public Template getTemplate() {
		return this.template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	@Override public String toString() {
		return "ReleaserProperties{" + "workingDir='" + this.workingDir + '\'' + ", git=" + this.git
				+ ", pom=" + this.pom + ", maven=" + this.maven + ", gradle=" + this.gradle + ", sagan="
				+ this.sagan + ", fixedVersions=" + this.fixedVersions + ", metaRelease="
				+ this.metaRelease + ", template=" + this.template + '}';
	}

	public ReleaserProperties copy() {
		return (ReleaserProperties) SerializationUtils.clone(this);
	}
}
