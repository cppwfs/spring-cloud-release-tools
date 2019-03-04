/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.release.internal;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.release.internal.docs.DocumentationUpdater;
import org.springframework.cloud.release.internal.git.ProjectGitHandler;
import org.springframework.cloud.release.internal.gradle.GradleUpdater;
import org.springframework.cloud.release.internal.pom.ProjectPomUpdater;
import org.springframework.cloud.release.internal.pom.ProjectVersion;
import org.springframework.cloud.release.internal.pom.Projects;
import org.springframework.cloud.release.internal.post.PostReleaseActions;
import org.springframework.cloud.release.internal.project.ProjectBuilder;
import org.springframework.cloud.release.internal.sagan.SaganUpdater;
import org.springframework.cloud.release.internal.tech.MakeBuildUnstableException;
import org.springframework.cloud.release.internal.template.TemplateGenerator;
import org.springframework.util.Assert;

/**
 * @author Marcin Grzejszczak
 */
public class Releaser {

	private static final Logger log = LoggerFactory.getLogger(Releaser.class);

	private static boolean ASSERT_SNAPSHOTS = true;

	private static boolean SKIP_SNAPSHOT_ASSERTION = false;

	private final ProjectPomUpdater projectPomUpdater;

	private final ProjectBuilder projectBuilder;

	private final ProjectGitHandler projectGitHandler;

	private final TemplateGenerator templateGenerator;

	private final GradleUpdater gradleUpdater;

	private final SaganUpdater saganUpdater;

	private final DocumentationUpdater documentationUpdater;

	private final PostReleaseActions postReleaseActions;

	public Releaser(ProjectPomUpdater projectPomUpdater, ProjectBuilder projectBuilder,
			ProjectGitHandler projectGitHandler, TemplateGenerator templateGenerator,
			GradleUpdater gradleUpdater, SaganUpdater saganUpdater,
			DocumentationUpdater documentationUpdater,
			PostReleaseActions postReleaseActions) {
		this.projectPomUpdater = projectPomUpdater;
		this.projectBuilder = projectBuilder;
		this.projectGitHandler = projectGitHandler;
		this.templateGenerator = templateGenerator;
		this.gradleUpdater = gradleUpdater;
		this.saganUpdater = saganUpdater;
		this.documentationUpdater = documentationUpdater;
		this.postReleaseActions = postReleaseActions;
	}

	public File clonedProjectFromOrg(String projectName) {
		return this.projectGitHandler.cloneProjectFromOrg(projectName);
	}

	public Projects retrieveVersionsFromSCRelease() {
		return this.projectPomUpdater.retrieveVersionsFromReleaseTrainBom();
	}

	public Projects fixedVersions() {
		return this.projectPomUpdater.fixedVersions();
	}

	public void updateProjectFromBom(File project, Projects versions,
			ProjectVersion versionFromScRelease) {
		updateProjectFromBom(project, versions, versionFromScRelease, ASSERT_SNAPSHOTS);
	}

	private void updateProjectFromBom(File project, Projects versions,
			ProjectVersion versionFromScRelease, boolean assertSnapshots) {
		this.projectPomUpdater.updateProjectFromReleaseTrain(project, versions,
				versionFromScRelease, assertSnapshots);
		this.gradleUpdater.updateProjectFromBom(project, versions, versionFromScRelease,
				assertSnapshots);
		ProjectVersion changedVersion = new ProjectVersion(project);
		log.info("\n\nProject was successfully updated to [{}]", changedVersion);
	}

	public void buildProject(ProjectVersion versionFromScRelease) {
		this.projectBuilder.build(versionFromScRelease);
		log.info("\nProject was successfully built");
	}

	public void commitAndPushTags(File project, ProjectVersion changedVersion) {
		this.projectGitHandler.commitAndTagIfApplicable(project, changedVersion);
		log.info("\nCommit was made and tag was pushed successfully");
	}

	public void deploy(ProjectVersion versionFromScRelease) {
		this.projectBuilder.deploy(versionFromScRelease);
		log.info("\nThe artifact was deployed successfully");
	}

	public void publishDocs(ProjectVersion changedVersion) {
		this.projectBuilder.publishDocs(changedVersion.version);
		log.info("\nThe docs were published successfully");
	}

	public void rollbackReleaseVersion(File project, Projects projects,
			ProjectVersion scReleaseVersion) {
		if (scReleaseVersion.isSnapshot()) {
			log.info("\nWon't rollback a snapshot version");
			return;
		}
		this.projectGitHandler.revertChangesIfApplicable(project, scReleaseVersion);
		ProjectVersion originalVersion = originalVersion(project);
		log.info("Original project version is [{}]", originalVersion);
		if ((scReleaseVersion.isRelease() || scReleaseVersion.isServiceRelease())
				&& originalVersion.isSnapshot()) {
			Projects newProjects = Projects.forRollback(projects, originalVersion);
			updateProjectFromBom(project, newProjects, originalVersion,
					SKIP_SNAPSHOT_ASSERTION);
			this.projectGitHandler.commitAfterBumpingVersions(project, originalVersion);
			log.info("\nSuccessfully reverted the commit and bumped snapshot versions");
		}
		else {
			log.info(
					"\nSuccessfully reverted the commit and came back to snapshot versions");
		}
	}

	ProjectVersion originalVersion(File project) {
		return new ProjectVersion(project);
	}

	public void pushCurrentBranch(File project) {
		this.projectGitHandler.pushCurrentBranch(project);
		log.info("\nSuccessfully pushed current branch");
	}

	public void closeMilestone(ProjectVersion releaseVersion) {
		if (releaseVersion.isSnapshot()) {
			log.info("\nWon't close a milestone for a SNAPSHOT version");
			return;
		}
		try {
			this.projectGitHandler.closeMilestone(releaseVersion);
			log.info("\nSuccessfully closed milestone");
		}
		catch (Exception ex) {
			throw new MakeBuildUnstableException("Failed to create an email template");
		}
	}

	public void createEmail(ProjectVersion releaseVersion, Projects projects) {
		Assert.notNull(releaseVersion,
				"You must provide a release version for your project");
		Assert.notNull(releaseVersion.version,
				"You must provide a release version for your project");
		if (releaseVersion.isSnapshot()) {
			log.info("\nWon't create email template for a SNAPSHOT version");
			return;
		}
		try {
			File email = this.templateGenerator.email(projects);
			if (email != null) {
				log.info("\nSuccessfully created email template at location [{}]", email);
			}
			else {
				throw new MakeBuildUnstableException(
						"Failed to create an email template");
			}
		}
		catch (Exception ex) {
			throw new MakeBuildUnstableException("Failed to create an email template",
					ex);
		}
	}

	public void createBlog(ProjectVersion releaseVersion, Projects projects) {
		if (releaseVersion.isSnapshot()) {
			log.info("\nWon't create blog template for a SNAPSHOT version");
			return;
		}
		try {
			File blog = this.templateGenerator.blog(projects);
			if (blog != null) {
				log.info("\nSuccessfully created blog template at location [{}]", blog);
			}
			else {
				throw new MakeBuildUnstableException("Failed to create a blog template");
			}
		}
		catch (Exception ex) {
			throw new MakeBuildUnstableException("Failed to create a blog template", ex);
		}
	}

	public void updateSpringGuides(ProjectVersion releaseVersion, Projects projects) {
		if (!(releaseVersion.isRelease() || releaseVersion.isServiceRelease())) {
			log.info(
					"\nWon't update Spring Guides for a non Release / Service Release version");
			return;
		}
		try {
			this.projectGitHandler.createIssueInSpringGuides(projects, releaseVersion);
		}
		catch (Exception ex) {
			throw new MakeBuildUnstableException(
					"Successfully updated Spring Guides issues", ex);
		}
	}

	public void createTweet(ProjectVersion releaseVersion, Projects projects) {
		if (releaseVersion.isSnapshot()) {
			log.info("\nWon't create tweet template for a SNAPSHOT version");
			return;
		}
		try {
			File tweet = this.templateGenerator.tweet(projects);
			if (tweet != null) {
				log.info("\nSuccessfully created tweet template at location [{}]", tweet);
			}
			else {
				throw new MakeBuildUnstableException("Failed to create a tweet template");
			}
		}
		catch (Exception ex) {
			throw new MakeBuildUnstableException("Failed to create a tweet template", ex);
		}
	}

	public void createReleaseNotes(ProjectVersion releaseVersion, Projects projects) {
		if (releaseVersion.isSnapshot()) {
			log.info("\nWon't create release notes for a SNAPSHOT version");
			return;
		}
		try {
			File output = this.templateGenerator.releaseNotes(projects);
			log.info("\nSuccessfully created release notes at location [{}]", output);
		}
		catch (Exception ex) {
			throw new MakeBuildUnstableException("Failed to create release notes", ex);
		}
	}

	public void updateSagan(File project, ProjectVersion releaseVersion) {
		String currentBranch = this.projectGitHandler.currentBranch(project);
		ProjectVersion originalVersion = new ProjectVersion(project);
		try {
			this.saganUpdater.updateSagan(project, currentBranch, originalVersion,
					releaseVersion);
			log.info("\nSuccessfully updated Sagan for branch [{}]", currentBranch);
		}
		catch (Exception ex) {
			throw new MakeBuildUnstableException(ex);
		}
	}

	public void updateDocumentationRepository(ReleaserProperties properties,
			ProjectVersion releaseVersion) {
		String releaseBranch = properties.getPom().getBranch();
		this.documentationUpdater.updateDocsRepo(releaseVersion, releaseBranch);
		log.info("\nSuccessfully updated documentation repository for branch [{}]",
				releaseBranch);
	}

	public void updateSpringProjectPage(Projects projects) {
		if (this.documentationUpdater.updateProjectRepo(projects) != null) {
			log.info("\nSuccessfully updated Spring project page");
		}
		else {
			throw new MakeBuildUnstableException("Failed to update Spring Project page");
		}
	}

	public void runUpdatedSamples(Projects projects) {
		this.postReleaseActions.runUpdatedTests(projects);
		log.info("\nSuccessfully updated and ran samples");
	}

	public void generateReleaseTrainDocumentation(Projects projects) {
		this.postReleaseActions.generateReleaseTrainDocumentation(projects);
		log.info("\nSuccessfully updated and generated release train documentation");
	}

	public void updateAllSamples(Projects projects) {
		this.postReleaseActions.updateAllTestSamples(projects);
		log.info("\nSuccessfully updated all samples");
	}

	public void updateReleaseTrainWiki(Projects projects) {
		this.documentationUpdater.updateReleaseTrainWiki(projects);
		log.info("\nSuccessfully updated project wiki");
	}

}
