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
package org.eclipse.che.selenium.hotupdate.recreate;

import static java.lang.Integer.*;
import static org.testng.Assert.*;

import com.google.inject.Inject;
import java.io.IOException;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.CheTestSystemClient;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.requestfactory.CheTestAdminHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.OPENSHIFT, TestGroup.MULTIUSER})
public class RecreateUpdateStrategyTest {
  private static final String COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE =
      "get dc | grep che | awk '{print $2}'";

  @Inject CheTestAdminHttpJsonRequestFactory testUserHttpJsonRequestFactory;
  @Inject CheTestSystemClient cheTestSystemClient;
  @Inject ProjectExplorer projectExplorer;
  @Inject OpenShiftCliCommandExecutor openShiftCliCommandExecutor;
  @Inject private ProcessAgent processAgent;
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private CheTerminal terminal;
  @Inject private Menu menu;

  private int cheDeploymentBeforeRollout;

  @BeforeClass
  public void setUp() throws IOException {
    cheDeploymentBeforeRollout =
        parseInt(openShiftCliCommandExecutor.execute(COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE));
  }

  @Test
  public void checkRecreateUpdateStrategy() throws Exception {
    String ocClientRolloutCommand = "rollout latest che";

    int requestAttempts = 100;
    int requestTimeoutInSec = 6;

    // open a user workspace and send request for preparing to shutdown
    ide.open(workspace);

    cheTestSystemClient.stop();

    // reopen the workspace and make sure that this one is not available after suspending system
    ide.open(workspace);
    projectExplorer.waitProjectExplorerDisappearance(requestTimeoutInSec);
    terminal.waitTerminalIsNotPresent(requestTimeoutInSec);

    // performs rollout
    openShiftCliCommandExecutor.execute(ocClientRolloutCommand);
    cheTestSystemClient.waitWorkspaceMasterStatus(
        requestAttempts, requestTimeoutInSec, SystemStatus.RUNNING);

    // get current version of deployment after rollout
    int cheDeploymentAfterRollout =
        parseInt(openShiftCliCommandExecutor.execute(COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE));

    // After rollout updating - deployment should be increased on 1. Previews version +1 should be
    // equal current
    assertEquals(cheDeploymentAfterRollout, cheDeploymentBeforeRollout + 1);

    // make sure that CHE ide is available after updating again
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
  }
}
