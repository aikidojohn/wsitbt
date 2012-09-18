/**
 * WSIT Build Tools (http://wsitbt.codeplex.com)
 *
 * Copyright (c) 2011 Application Security, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Application Security, Inc.
 */
package com.appsec.maven.wsit.it;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Integration test of maven wsit plugin.
 *
 * @author Dan Rollo
 * Date: 6/14/11
 * Time: 9:32 PM
 */
public final class PluginITest {

    private static final class ITArtifactInfo {
        private final String org;
        private final String name;
        private final String ver;
        private final String ext;

        private final Verifier verifier;
        private final File artifactFile;
        private final boolean isPriorInstall;

        private final File logFile;

        private ITArtifactInfo(final File localRepo, final File moduleBasedir,
                final String groupId, final String artifactId, final String version, final String extension)
                throws VerificationException {

            org = groupId;
            name = artifactId;
            ver = version;
            ext = extension;

            verifier = new Verifier(moduleBasedir.getAbsolutePath());
            logFile = new File(moduleBasedir, verifier.getLogFileName());

            /*
            //@todo Use non-default local repo to avoid side effects?
            // use local repo in target dir (avoids messing with default local repo).
            verifier.setLocalRepo(localRepo.getAbsolutePath());
            //*/

            final String artifactPath = verifier.getArtifactPath(org, name, ver, ext);
            artifactFile = new File(artifactPath);
            isPriorInstall = artifactFile.exists();
        }

        private void doDelete() throws IOException {
           verifier.deleteArtifact(org, name, ver, ext);
        }

        private void depCleanup() throws IOException {
            if (!isPriorInstall) {
                // avoid side effects by deleting artifacts from local repo if they did not already exist
                //verifier.deleteArtifact(org, name, ver, ext);
                verifier.deleteArtifacts(org + "." + name);
            }

            if (logFile.exists()) {
                if (!logFile.delete()) {
                    throw new RuntimeException("Error deleting ITest log file: " + logFile.getAbsolutePath());
                }
            }
        }

        private void doInstall(final boolean isSkipClean) throws VerificationException {

            System.out.println("running 'install' with basedir: " + verifier.getBasedir() + ", artifactId: " + name);

            final List<String> cliOptions = new ArrayList<String>();
            cliOptions.add("-N");

            // show more debug info
            //cliOptions.add("-X");

            // skip running of license plugin during ITest
            cliOptions.add("-Dlicense.skip=true");

            if (isSkipClean) {
                // "clean" is failing on Windows due to something holding onto jar in target dir:
                // [ERROR] Failed to execute goal org.apache.maven.plugins:maven-clean-plugin:2.4.1:clean (default-cli) on project wsit-bt-core: Failed to clean project: Failed to delete C:\oss\java\hg\wsit-build-tools\wsitbt\wsit-bt-core\target\wsit-bt-core-1.0-SNAPSHOT.jar -> [Help 1]
                // and:
                // [ERROR] Failed to execute goal org.apache.maven.plugins:maven-clean-plugin:2.4.1:clean (default-cli) on project wsit-maven-plugin: Failed to clean project: Failed to delete C:\oss\java\hg\wsit-build-tools\wsitbt\wsit-maven-plugin\target\wsit-maven-plugin-1.0-SNAPSHOT.jar -> [Help 1]
                //cliOptions.add("-Dclean.skip=true");
                verifier.setAutoclean(!isSkipClean);
            }

            verifier.setCliOptions(cliOptions);

            execGoalAndVerifyNoError(verifier, "install");
        }
    }

    private static void execGoalAndVerifyNoError(final Verifier verifier, final String goal) throws VerificationException {

        verifier.executeGoal(goal);

        /*
         * This is the simplest way to check a build
         * succeeded. It is also the simplest way to create
         * an IT test: make the build pass when the test
         * should pass, and make the build fail when the
         * test should fail. There are other methods
         * supported by the verifier. They can be seen here:
         * http://maven.apache.org/shared/maven-verifier/apidocs/index.html
         */
        verifier.verifyErrorFreeLog();
        /*
         * Reset the streams before executing the verifier
         * again.
         */
        verifier.resetStreams();
        /*
         * The verifier also supports beanshell scripts for
         * verification of more complex scenarios. There are
         * plenty of examples in the core-it tests here:
         * http://svn.apache.org/repos/asf/maven/core-integration-testing/trunk
         */
    }

    @Test
    public void testPlugin() throws Exception {

        // The testdir is computed from the location of this file.
        final File testDir = ResourceExtractor.simpleExtractResources(getClass(), "mojotest");

        final Properties testVersionProps = new Properties();
        final InputStream isProp = getClass().getClassLoader().getResourceAsStream("test-version.properties");
        try {
            testVersionProps.load(isProp);
        } finally {
            isProp.close();
        }
        final String groupdId = testVersionProps.getProperty("it.groupId");
        final String version = testVersionProps.getProperty("it.version");


        final File parentBasedir = new File(getClass().getClassLoader().getResource("").getFile())
                .getParentFile().getParentFile().getParentFile();

        final File localRepo = new File(parentBasedir, "wsit-maven-plugin-itest/target/itest-local-repo");

        // @todo Find a way to run this test when 'mvn' is not on the path (and M*_HOME is not defined).
        // For now, we need the mvn script on our path, so skip this Integration Test if mvn is not on the path
        final Verifier checkPath = new Verifier(testDir.getAbsolutePath());
        try {
            checkPath.getMavenVersion();
        } catch (VerificationException e) {
            checkPath.resetStreams();

            System.err.println("WARNING: Skipping integration test: " + getClass().getName()
                    + ". Add 'mvn' to your path to run this test.");
            return;
        }

        /*
         * We must first make sure that any artifact created
         * by this test has been removed from the local
         * repository. Failing to do this could cause
         * unstable test results. Fortunately, the verifier
         * makes it easy to do this.
         */

        final ITArtifactInfo depProjectParent
                = new ITArtifactInfo(localRepo, parentBasedir, groupdId, "wsit-build-tools-project", version, "pom");

        final ITArtifactInfo depWSITMavenPlugin
                = new ITArtifactInfo(localRepo, new File(parentBasedir, "wsit-maven-plugin"),
                groupdId, "wsit-maven-plugin", version, "jar");

        final ITArtifactInfo depWSITCore
                = new ITArtifactInfo(localRepo, new File(parentBasedir, "wsit-bt-core"),
                groupdId, "wsit-bt-core", version, "jar");

        depProjectParent.doDelete();
        depWSITMavenPlugin.doDelete();
        depWSITCore.doDelete();
        try {
            //depProjectParent.doInstall(false);
            depProjectParent.doInstall(true);
            depWSITCore.doInstall(true);
            depWSITMavenPlugin.doInstall(true);

            final Verifier execPlugin = new Verifier( testDir.getAbsolutePath());

            /*
            //@todo Use non-default local repo to avoid side effects?
            execPlugin.setLocalRepo(localRepo.getAbsolutePath());
            //*/

            execGoalAndVerifyNoError(execPlugin, "package");
        } finally {
            // avoid side effects by deleting artifacts from local repo if they did not already exist
            depWSITMavenPlugin.depCleanup();
            depWSITCore.depCleanup();
            depProjectParent.depCleanup();
        }
    }
}
