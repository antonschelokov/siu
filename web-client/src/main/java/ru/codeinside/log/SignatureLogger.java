/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright (c) 2015, MPL CodeInside http://codeinside.ru
 */
package ru.codeinside.log;

import org.apache.commons.codec.binary.Base64;
import ru.codeinside.adm.AdminServiceProvider;
import ru.codeinside.gses.activiti.forms.Signatures;
import ru.codeinside.gses.webui.Flash;
import ru.codeinside.gses.webui.form.SignatureType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Логирует подписи СП и ОВ
 */
public class SignatureLogger {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SignatureLogger.class.getName());
    private static final String basePath = "logs" + File.separator + "bids";

    private final Long bidId;
    private final String taskId;

    public SignatureLogger(Long bidId, String taskId) {
        this.taskId = taskId;
        if (bidId == null) {
            this.bidId = getBidId();
        } else {
            this.bidId = bidId;
        }
    }

    public void log(String data, Signatures signatures, SignatureType signatureType) {
        File logFile;
        try {
            logFile = prepareToLog(signatureType);
        } catch (IOException e) {
            logger.throwing(SignatureLogger.class.getName(), "log", e);
            return;
        }

        if (logFile == null) {
            logger.warning("Log file already exist: bidId = " + bidId + " signatureType = " + signatureType.name()
                    + ". WHY!?");
            return;
        }

        try {
            writeToFile(logFile, data, signatures);
        } catch (IOException e) {
            logger.throwing(SignatureLogger.class.getName(), "log", e);
        }
    }

    private void writeToFile(File logFile, String data, Signatures signatures) throws IOException {
        Writer fw = null;
        try {
            fw = new FileWriter(logFile)
                    .append(String.format("Дата: %s%n%n", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date())))
                    .append(String.format("Логин: %s%n%n", Flash.login()))
                    .append(String.format("Данные: %s%n%n", data))
                    .append(String.format("Сертификат: %s%n%n", Base64.encodeBase64String(signatures.certificate)))
                    .append(String.format("Подпись: %s", Base64.encodeBase64String(signatures.signs[0])));
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    private File prepareToLog(SignatureType signatureType) throws IOException {
        File rootFile = new File(basePath);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }

        File bidFile = new File(rootFile.getAbsolutePath() + File.separator + String.valueOf(bidId));
        if (bidFile.exists()) {
            bidFile.mkdir();
        }

        String suffix = (taskId == null ? "" : "-" + taskId) + ".log";
        File signFile = new File(bidFile.getAbsolutePath() + File.separator + signatureType.name() + suffix);
        if (!signFile.createNewFile()) {
            return null;
        }
        return signFile;
    }

    private Long getBidId() {
        return AdminServiceProvider.get().getBidByTask(taskId).getId();
    }
}
