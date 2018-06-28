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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.ImagePullSecretProvisioner.SECRET_NAME_SUFFIX;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Secret;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.infrastructure.docker.auth.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfig;
import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfigs;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link ImagePullSecretProvisioner}.
 *
 * @author David Festal
 */
@Listeners(MockitoTestNGListener.class)
public class ImagePullSecretProvisionerTest {

  private static final String WORKSPACE_ID = "workspace123";

  private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  @Mock private UserSpecificDockerRegistryCredentialsProvider credentialsProvider;

  @Mock private AuthConfigs authConfigs;

  @Mock private Pod pod;

  @Mock private PodSpec podSpec;

  private LocalObjectReference existingImagePullSecretRef =
      new LocalObjectReferenceBuilder().withName("existing").build();

  private LocalObjectReference newImagePullSecretRef =
      new LocalObjectReferenceBuilder().withName(WORKSPACE_ID + SECRET_NAME_SUFFIX).build();

  private ImagePullSecretProvisioner imagePullSecretProvisioner;

  @BeforeMethod
  public void setup() {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    k8sEnv = KubernetesEnvironment.builder().build();
    k8sEnv.getPods().put("wksp", pod);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getImagePullSecrets()).thenReturn(ImmutableList.of(existingImagePullSecretRef));

    when(credentialsProvider.getCredentials()).thenReturn(authConfigs);
    imagePullSecretProvisioner = new ImagePullSecretProvisioner(credentialsProvider);
  }

  @Test
  public void doNotDoAnythingIfNoPrivateRegistries() throws Exception {
    when(authConfigs.getConfigs()).thenReturn(Collections.emptyMap());

    imagePullSecretProvisioner.provision(k8sEnv, runtimeIdentity);

    assertTrue(k8sEnv.getSecrets().isEmpty());
    verifyZeroInteractions(podSpec);
  }

  @Test
  public void addSecretAndReferenceInPod() throws Exception {
    when(authConfigs.getConfigs())
        .thenReturn(
            ImmutableMap.of(
                "reg1",
                new TestAuthConfig().withUsername("username1").withPassword("password1"),
                "reg2",
                new TestAuthConfig().withUsername("username2").withPassword("password2")));

    imagePullSecretProvisioner.provision(k8sEnv, runtimeIdentity);

    verify(podSpec)
        .setImagePullSecrets(ImmutableList.of(newImagePullSecretRef, existingImagePullSecretRef));

    Secret secret = k8sEnv.getSecrets().get(WORKSPACE_ID + SECRET_NAME_SUFFIX);
    assertNotNull(secret);
    assertEquals(secret.getType(), "kubernetes.io/dockercfg");

    String dockerCfgData = secret.getData().get(".dockercfg");
    assertNotNull(dockerCfgData);

    Gson gson = new Gson();
    assertEquals(
        gson.toJson(
            gson.fromJson(new String(Base64.getDecoder().decode(dockerCfgData)), Map.class)),
        gson.toJson(
            gson.fromJson(
                ""
                    + "{ \"https://reg1\": { \"username\": \"username1\", \"password\": \"password1\", \"email\": \"email@email\", \"auth\": \""
                    + buildAuth("username1", "password1")
                    + "\" }"
                    + ", \"https://reg2\": { \"username\": \"username2\", \"password\": \"password2\", \"email\": \"email@email\", \"auth\": \""
                    + buildAuth("username2", "password2")
                    + "\" }"
                    + "}",
                Map.class)));
  }

  private static class TestAuthConfig implements AuthConfig {

    private String username;
    private String password;

    @Override
    public String getUsername() {
      return username;
    }

    @Override
    public void setUsername(String username) {
      this.username = username;
    }

    @Override
    public AuthConfig withUsername(String username) {
      setUsername(username);
      return this;
    }

    @Override
    public String getPassword() {
      return password;
    }

    @Override
    public void setPassword(String password) {
      this.password = password;
    }

    @Override
    public AuthConfig withPassword(String password) {
      setPassword(password);
      return this;
    }
  }

  private String buildAuth(String username, String password) {
    return Base64.getEncoder().encodeToString((username + ':' + password).getBytes());
  }
}
