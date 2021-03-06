/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright (c) 2013, MPL CodeInside http://codeinside.ru
 */

package ru.codeinside.gses.webui;

import com.google.common.collect.ImmutableSet;
import org.activiti.engine.ProcessEngine;
import ru.codeinside.adm.AdminService;
import ru.codeinside.adm.database.Role;
import ru.codeinside.gses.service.ActivitiService;
import ru.codeinside.gses.service.DeclarantService;
import ru.codeinside.gses.service.ExecutorService;

import javax.persistence.EntityManager;

public interface Flasher {
  String getLogin();

  String getRemoteAddr();

  String getUserAgent();

  ImmutableSet<Role> getRoles();

  ActivitiService getActivitiService();

  ProcessEngine getProcessEngine();

  DeclarantService getDeclarantService();

  ExecutorService getExecutorService();

  AdminService getAdminService();

  EntityManager getEm();

  EntityManager getLogEm();

  public interface Closable {
    void close(boolean success);
  }

}
