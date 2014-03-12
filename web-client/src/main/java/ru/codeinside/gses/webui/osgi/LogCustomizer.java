/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright (c) 2013, MPL CodeInside http://codeinside.ru
 */

package ru.codeinside.gses.webui.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import ru.codeinside.gws.api.ClientLog;
import ru.codeinside.gws.api.ClientRequest;
import ru.codeinside.gws.api.ClientResponse;
import ru.codeinside.gws.api.LogService;

import java.io.OutputStream;
import java.util.logging.Logger;

public class LogCustomizer implements ServiceTrackerCustomizer {

  static BundleContext BUNDLE;
  static volatile ServiceReference REF;

  final Logger logger = Logger.getLogger(getClass().getName());

  LogCustomizer(BundleContext bundleContext) {
    BUNDLE = bundleContext;
  }

  @Override
  public Object addingService(ServiceReference serviceReference) {
    if (REF == null) {
      REF = serviceReference;
    } else {
      logger.info("new logger: " + serviceReference.getBundle());
      REF = serviceReference;
    }
    return null;
  }

  @Override
  public void modifiedService(ServiceReference serviceReference, Object o) {
    // изменения атрибутов нам не выжны
  }

  @Override
  public void removedService(ServiceReference serviceReference, Object o) {
    if (REF != serviceReference) {
      logger.info("removed other logger: " + serviceReference.getBundle());
    }
    REF = null;
  }

  public static void setShouldWriteServerLog(boolean should) {
    ServiceReference ref = REF;
    if (ref != null) {
      LogService service = (LogService) BUNDLE.getService(ref);
      try {
        service.setShouldWriteServerLog(should);
      } finally {
        BUNDLE.ungetService(ref);
      }
    }
  }

  public static Boolean isServerLogEnabled(String componentName) {
    ServiceReference ref = REF;
    if (ref != null) {
      LogService service = (LogService) BUNDLE.getService(ref);
      try {
        return service.isServerLogEnabled(componentName);
      } finally {
        BUNDLE.ungetService(ref);
      }
    }
    return null;
  }

  public static void setServerLogEnabled(String componentName, boolean enabled) {
    ServiceReference ref = REF;
    if (ref != null) {
      LogService service = (LogService) BUNDLE.getService(ref);
      try {
        service.setServerLogEnabled(componentName, enabled);
      } finally {
        BUNDLE.ungetService(ref);
      }
    }
  }


  public static String getStoragePath() {
    ServiceReference ref = REF;
    if (ref != null) {
      LogService service = (LogService) BUNDLE.getService(ref);
      try {
        return service.getPathInfo();
      } finally {
        BUNDLE.ungetService(ref);
      }
    }
    return null;
  }

  public static ClientLog createClientLog(String componentName, String processInstanceId) {
    final ServiceReference ref = REF;
    if (ref != null) {
      LogService service = (LogService) BUNDLE.getService(ref);
      ClientLog clientLog = service.createClientLog(componentName, processInstanceId);
      return new ClientLogProxy(clientLog, ref);
    }
    return null;
  }

  final static class ClientLogProxy implements ClientLog {

    final ClientLog log;
    final ServiceReference ref;

    public ClientLogProxy(ClientLog log, ServiceReference ref) {
      this.log = log;
      this.ref = ref;
    }

    @Override
    public void log(Throwable e) {
      log.log(e);
    }

    @Override
    public OutputStream getHttpOutStream() {
      return log.getHttpOutStream();
    }

    @Override
    public OutputStream getHttpInStream() {
      return log.getHttpInStream();
    }

    @Override
    public void logRequest(ClientRequest request) {
      log.logRequest(request);
    }

    @Override
    public void logResponse(ClientResponse response) {
      log.logResponse(response);
    }

    @Override
    public void close() {
      log.close();
      BUNDLE.ungetService(ref);
    }
  }
}
