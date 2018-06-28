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
package org.eclipse.che.plugin.golang.shared;

/** @author Eugene Ivantsov */
public final class Constants {
  /** Language attribute name */
  public static String LANGUAGE = "language";
  /** Node JS Project Type ID */
  public static String GOLANG_PROJECT_TYPE_ID = "golang";

  /** Default extension for Go files */
  public static String GOLANG_FILE_EXT = "go";

  private Constants() {
    throw new UnsupportedOperationException("You can't create instance of Constants class");
  }
}
