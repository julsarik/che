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
package org.eclipse.che.multiuser.api.distributed.cache;

import java.util.List;
import java.util.Map;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedHashMap;

/**
 * <b>Purpose</b>: Provides an empty implementation of {@link ReplicatedHashMap.Notification}. Users
 * who do not require to be notified about all map changes can subclass this class and implement
 * only the methods required.
 *
 * @author Sergii Leshchenko
 */
public class ReplicatedMapNotificationAdapter implements ReplicatedHashMap.Notification {
  @Override
  public void entrySet(Object key, Object value) {}

  @Override
  public void entryRemoved(Object key) {}

  @Override
  public void contentsSet(Map new_entries) {}

  @Override
  public void contentsCleared() {}

  @Override
  public void viewChange(View view, List mbrs_joined, List mbrs_left) {}
}
