/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.executor;

import static java.lang.String.format;
import static java.lang.System.getProperty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.selenium.core.provider.OpenShiftWebConsoleUrlProvider;
import org.eclipse.che.selenium.core.utils.executor.CommandExecutor;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is aimed to call OpenShift CLI command.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class OpenShiftCliCommandExecutor implements CommandExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftCliCommandExecutor.class);

  private static final boolean IS_MAC_OS = getProperty("os.name").toLowerCase().startsWith("mac");
  private static final String DEFAULT_OPENSHIFT_USERNAME = "developer";
  private static final String DEFAULT_OPENSHIFT_PASSWORD = "any";

  private static final Path PATH_TO_OPENSHIFT_CLI_DIRECTORY =
      Paths.get(getProperty("java.io.tmpdir"));

  private static final Path PATH_TO_OPENSHIFT_CLI = PATH_TO_OPENSHIFT_CLI_DIRECTORY.resolve("oc");

  @Inject private ProcessAgent processAgent;

  @Inject(optional = true)
  @Named("env.openshift.username")
  private String openShiftUsername;

  @Inject(optional = true)
  @Named("env.openshift.password")
  private String openShiftPassword;

  @Inject(optional = true)
  @Named("env.openshift.token")
  private String openShiftToken;

  @Inject private OpenShiftWebConsoleUrlProvider openShiftWebConsoleUrlProvider;

  @Override
  public String execute(String command) throws IOException {
    if (!PATH_TO_OPENSHIFT_CLI.toFile().exists()) {
      downloadOpenShiftCli();
    }

    String openShiftCliCommand = format("%s %s", PATH_TO_OPENSHIFT_CLI, command);

    return processAgent.process(openShiftCliCommand);
  }

  /** Logs into OpensShift as a regular user */
  public void login() throws IOException {
    String loginToOpenShiftCliCommand;
    if (openShiftToken != null) {
      loginToOpenShiftCliCommand =
          format(
              "login --server=%s --token=%s --insecure-skip-tls-verify",
              openShiftWebConsoleUrlProvider.get(), openShiftToken);
    } else {
      loginToOpenShiftCliCommand =
          format(
              "login --server=%s -u=%s -p=%s --insecure-skip-tls-verify",
              openShiftWebConsoleUrlProvider.get(),
              openShiftUsername != null ? openShiftUsername : DEFAULT_OPENSHIFT_USERNAME,
              openShiftPassword != null ? openShiftPassword : DEFAULT_OPENSHIFT_PASSWORD);
    }

    execute(loginToOpenShiftCliCommand);
  }

  private void downloadOpenShiftCli() throws IOException {
    if (Files.notExists(PATH_TO_OPENSHIFT_CLI_DIRECTORY)) {
      Files.createDirectory(PATH_TO_OPENSHIFT_CLI_DIRECTORY);
    }

    URL url;
    File packagePath;
    String commandToUnpackOpenShiftCli;
    if (IS_MAC_OS) {
      url =
          new URL(
              "https://github.com/openshift/origin/releases/download/v3.9.0/openshift-origin-client-tools-v3.9.0-191fece-mac.zip");
      packagePath =
          PATH_TO_OPENSHIFT_CLI_DIRECTORY.resolve("openshift-origin-client-tools.zip").toFile();
      commandToUnpackOpenShiftCli =
          format("unzip -d %s %s", PATH_TO_OPENSHIFT_CLI_DIRECTORY, packagePath);
    } else {
      url =
          new URL(
              "https://github.com/openshift/origin/releases/download/v3.9.0/openshift-origin-client-tools-v3.9.0-191fece-linux-64bit.tar.gz");
      packagePath =
          PATH_TO_OPENSHIFT_CLI_DIRECTORY.resolve("openshift-origin-client-tools.tar.gz").toFile();
      commandToUnpackOpenShiftCli =
          format("tar --strip 1 -xzf %s -C %s", packagePath, PATH_TO_OPENSHIFT_CLI_DIRECTORY);
    }

    LOG.info("Downloading OpenShift CLI from {} ...", url);
    FileUtils.copyURLToFile(url, packagePath);
    LOG.info("OpenShift CLI has been downloaded.");

    processAgent.process(commandToUnpackOpenShiftCli);

    FileUtils.deleteQuietly(packagePath);
  }
}
