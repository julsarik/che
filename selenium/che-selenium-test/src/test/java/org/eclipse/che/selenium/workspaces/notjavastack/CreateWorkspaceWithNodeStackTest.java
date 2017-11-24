/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.workspaces.notjavastack;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.Workspaces;
import org.eclipse.che.selenium.pageobject.dashboard.workspacedetails.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateWorkspaceWithNodeStackTest {
  private final String WORKSPACE = NameGenerator.generate("WsNode", 4);

  @Inject private TestUser defaultTestUser;
  @Inject private NavigationBar navigationBar;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private MachineTerminal terminal;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Workspaces workspaces;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void createWorkspaceWithNodeStackTest() {
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.clickOnNewWorkspaceBtn();

    createWorkspace.waitToolbar();
    createWorkspace.typeWorkspaceName(WORKSPACE);
    createWorkspace.selectStack(TestStacksConstants.NODE.getId());
    createWorkspace.setMachineRAM("2");
    createWorkspace.clickOnCreateWorkspaceButton();

    dashboard.waitNotificationIsClosed();
    seleniumWebDriver.switchFromDashboardIframeToIde();

    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    terminal.waitTerminalConsole(EXPECTED_MESS_IN_CONSOLE_SEC);
  }
}
