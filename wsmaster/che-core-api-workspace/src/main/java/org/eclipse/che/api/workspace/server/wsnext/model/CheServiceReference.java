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
package org.eclipse.che.api.workspace.server.wsnext.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CheServiceReference {

  private String name = null;
  private String version = null;
  private List<CheServiceParameter> parameters = new ArrayList<CheServiceParameter>();

  /** */
  public CheServiceReference name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /** */
  public CheServiceReference version(String version) {
    this.version = version;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  /** */
  public CheServiceReference parameters(List<CheServiceParameter> parameters) {
    this.parameters = parameters;
    return this;
  }

  public List<CheServiceParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<CheServiceParameter> parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CheServiceReference cheServiceReference = (CheServiceReference) o;
    return Objects.equals(name, cheServiceReference.name)
        && Objects.equals(version, cheServiceReference.version)
        && Objects.equals(parameters, cheServiceReference.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, version, parameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CheServiceReference {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
