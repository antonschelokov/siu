/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Copyright (c) 2014, MPL CodeInside http://codeinside.ru
 */

package ru.codeinside.gses.form.docx;

import ru.codeinside.gses.form.FormConverter;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

final public class RemoteService {

    static boolean testMode = false;

    final Logger logger = Logger.getLogger(getClass().getName());
    final Process process;
    final File jarFile;
    final Thread errorLogger;
    final static String JAVA;
    final static File JAVA_HOME;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        JAVA = os.contains("win") ? "java.exe" : "java";
        JAVA_HOME = new File(System.getProperty("java.home"));
    }

    BufferedReader reader;


    BufferedReader getIn() {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        }
        return reader;
    }

    OutputStream getOut() {
        return process.getOutputStream();
    }

    boolean isAlive() {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    void close() {
        if (isAlive()) {
            close(process.getInputStream());
            close(process.getOutputStream());
            close(process.getErrorStream());
            process.destroy();
        }
        errorLogger.interrupt();
        jarFile.delete();
    }

    RemoteService() {
        try {
            jarFile = File.createTempFile("docx_", ".jar");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        InputStream is = null;
        FileOutputStream fos = null;
        try {
            if (testMode) {
                is = new FileInputStream("target/srv/docx.jar");
            } else {
                is = getClass().getResourceAsStream("/srv/docx.jar");
                if (is == null) {
                    throw new IllegalStateException("service jar not found!");
                }
            }
            fos = new FileOutputStream(jarFile);
            byte[] buffer = new byte[8192];
            int count;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
        } catch (FileNotFoundException e) {
            jarFile.delete();
            throw new IllegalStateException(e);
        } catch (IOException e) {
            jarFile.delete();
            throw new IllegalStateException(e);
        } finally {
            close(fos);
            close(is);
        }

        String java = detectJava();
        ProcessBuilder builder = new ProcessBuilder(java,
                "-Djava.awt.headless=true", "-Xmx64m", "-Xms16m", "-Xss1m",
                "-jar", jarFile.getAbsolutePath()
        );
        try {
            process = builder.start();
        } catch (IOException e) {
            jarFile.delete();
            throw new IllegalStateException(e);
        }

        final InputStream error = process.getErrorStream();
        errorLogger = new Thread(Thread.currentThread().getThreadGroup(), new Runnable() {
            @Override
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(error));
                try {
                    while (!Thread.interrupted()) {
                        try {
                            String s = br.readLine();
                            if (s == null) {
                                break;
                            }
                            s = s.trim();
                            if (!s.isEmpty()) {
                                Logger.getLogger(FormConverter.class.getName()).info(s);
                            }
                        } catch (IOException e) {
                            break;
                        }
                    }
                } finally {
                    close(br);
                }
            }
        }, "docxErrorLogger", 64 * 1024);
        errorLogger.setDaemon(true);
        errorLogger.start();
    }

    private String detectJava() {
        String process = JAVA;
        if (!isJava(process)) {
            process = absoluteJavaPath(process);
            if (!isJava(process)) {
                throw new IllegalStateException("Java not found!");
            }
        }
        return process;
    }

    private boolean isJava(String processName) {
        Process process;
        try {
            process = new ProcessBuilder(processName, "-version").start();
        } catch (IOException e) {
            return false;
        }
        boolean ok = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String version = reader.readLine();
            if (version != null && version.startsWith("java version")) {
                ok = true;
            }
        } catch (IOException e) {
            logger.log(Level.INFO, "io error", e);
        } finally {
            process.destroy();
            close(reader);
        }
        return ok;
    }

    private String absoluteJavaPath(String shortName) {
        File process = new File(new File(JAVA_HOME, "bin"), shortName);
        if (process.exists()) {
            return process.getAbsolutePath();
        }
        throw new IllegalStateException("not found " + process);
    }

    void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.log(Level.INFO, "io error", e);
            }
        }
    }

}
