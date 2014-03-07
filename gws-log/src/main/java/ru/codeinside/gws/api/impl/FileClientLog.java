/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright (c) 2014, MPL CodeInside http://codeinside.ru
 */
package ru.codeinside.gws.api.impl;

import ru.codeinside.gws.api.ClientLog;
import ru.codeinside.gws.api.ClientRequest;
import ru.codeinside.gws.api.ClientResponse;
import ru.codeinside.gws.api.Packet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author xeodon
 * @since 1.0.2
 */
final class FileClientLog implements ClientLog {

  final String componentName;
  final String processInstanceId;
  final String timestamp;
  final Logger logger = LogServiceFileImpl.LOGGER;

  OutputStream httpOut;
  OutputStream httpIn;

  public FileClientLog(String componentName, String processInstanceId) {
    Date now = new Date();
    this.componentName = componentName;
    this.processInstanceId = processInstanceId;
    timestamp = new SimpleDateFormat("yyMMddHHmmssSSS").format(now);
    writeStringToSpool("Date", now.toString());
    writeStringToSpool("ProcessInstanceId", processInstanceId);
  }

  @Override
  public void log(Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    writeStringToSpool("Error", sw.toString());
  }

  @Override
  public OutputStream getHttpOutStream() {
    if (httpOut == null) {
      try {
        httpOut = new BufferedFileOutputStream(createSpoolFile("http-" + true + "-" + true));
      } catch (FileNotFoundException e) {
        logger.log(Level.WARNING, "create spool file fail", e);
        httpOut = new NullOutputStream();
      }
    }
    return httpOut;
  }

  @Override
  public OutputStream getHttpInStream() {
    if (httpIn == null) {
      try {
        httpIn = new BufferedFileOutputStream(createSpoolFile("http-" + false + "-" + true));
      } catch (FileNotFoundException e) {
        logger.log(Level.WARNING, "create spool file fail", e);
        httpIn = new NullOutputStream();
      }
    }
    return httpIn;
  }

  @Override
  public void logRequest(ClientRequest request) {
    if (request != null && request.packet != null) {
      savePacketToFile(request.packet, "ClientRequest");
    }
  }

  @Override
  public void logResponse(ClientResponse response) {
    if (response != null && response.packet != null) {
      savePacketToFile(response.packet, "ClientResponse");
    }
  }

  @Override
  public void close() {
    Streams.close(httpOut, httpIn);
    moveFromSpool();
  }

  private void moveFromSpool() {
    File source = Files.getAppTmpDir(LogSettings.getPath(true), marker());

    // Вринат разбиения по каталогам:
    //File target0 = Files.getAppTmpDir(LogSettings.getPath(false), componentName);
    //File target1 = new File(target0, processInstanceId);
    //if (!target1.exists() && !target1.mkdir()) {
    //  logger.log(Level.WARNING, "can't create " + target1);
    //  return;
    // }
    // File target = new File(target1, timestamp);

    File target = Files.getAppTmpDir(LogSettings.getPath(false), UUID.randomUUID().toString().replace("-", ""));
    if (target.exists()) {
      target.delete();
    }

    if (!source.renameTo(target)) {
      logger.log(Level.INFO, "move from spool (override?)");
      File[] files = source.listFiles();
      if (files != null) {
        for (File file : files) {
          if (!file.renameTo(new File(target, file.getName()))) {
            logger.log(Level.INFO, "can't move " + file);
          }
        }
      }
      if (!source.delete()) {
        logger.log(Level.INFO, "can't delete " + source);
      }
    }
  }

  public void savePacketToFile(Packet packet, String name) {
    writeStringToSpool(name, LogServiceFileImpl.createSoapPackage(packet));
  }

  File createSpoolFile(String name) {
    return Files.createCacheFileName(LogSettings.getPath(true), marker(), "log", name);
  }

  String marker() {
    return componentName + "-" + processInstanceId + "-" + timestamp;
  }

  void writeStringToSpool(String fileName, String content) {
    OutputStreamWriter writer = null;
    try {
      File spoolFile = createSpoolFile(fileName);
      writer = new OutputStreamWriter(new BufferedFileOutputStream(spoolFile), LogServiceFileImpl.UTF8);
      writer.write(content);
      writer.close();
    } catch (FileNotFoundException e) {
      logger.log(Level.WARNING, "io error", e);
    } catch (IOException e) {
      logger.log(Level.WARNING, "io error", e);
    } finally {
      Streams.close(writer);
    }
  }
}
