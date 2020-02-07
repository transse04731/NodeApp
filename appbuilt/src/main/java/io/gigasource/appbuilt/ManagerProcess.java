package io.gigasource.appbuilt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ManagerProcess {

    public ManagerProcess() {
    }

    private List<String[]> find(File binary) {
        final File root = new File(File.separator + "proc");
        final List<String[]> result = new ArrayList<>();
        if (root.isDirectory()) {
            final File[] list = root.listFiles();
            if (list != null) {
                for (File file : list) {
                    final String[] found = processPidDirectory(file, binary);
                    if (found != null) {
                        result.add(found);
                    }
                }
            }
        }
        return result;
    }

    private String[] processPidDirectory(File directory, File binary) {
        if (!directory.isDirectory()) {
            return null;
        }
        try {
            int pid = Integer.parseInt(directory.getName());
            File fileCommandLine = new File(directory, "cmdline");
            if (!fileCommandLine.isFile() || !fileCommandLine.canRead()) {
                return null;
            }
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(fileCommandLine))
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(binary.getAbsolutePath())) {
                        return new String[]{String.valueOf(pid), line};
                    }
                }
            } catch (IOException ignored) {
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ignored) {
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public int[] getPid(File command) {
        final List<String[]> commandLines = find(command);
        final int[] pids = new int[commandLines.size()];
        for (int i = 0; i < pids.length; i++) {
            pids[i] = Integer.parseInt(commandLines.get(i)[0]);
        }
        return pids;
    }

    public List<String[]> getCommandLine(File command) {
        final List<String[]> result = new ArrayList<>();
        final List<String[]> foundCommands = find(command);
        for (String[] foundCommand : foundCommands) {
            String part = "";
            final List<String> parts = new LinkedList<>();
            final int length = foundCommand[1].length();
            for (int i = 0; i < length; i++) {
                char letter = foundCommand[1].charAt(i);
                if (i == length - 1 || letter == 0) {
                    parts.add(part);
                    part = "";
                } else {
                    part += letter;
                }
            }
            result.add(parts.toArray(new String[parts.size()]));
        }
        return result;
    }

    public void kill(File command) {
        for (int i = 0; i < 10; i++) {
            final int[] pids = getPid(command);
            for (int pid : pids) {
                kill(pid);
            }
        }
    }

    public void kill(int pid) {
        android.os.Process.sendSignal(pid, 15);
    }

    public boolean isRunning(File binary) {
        return getPid(binary).length > 0;
    }

    public boolean isRunning(File binary, int pid) {
        final int[] pids = getPid(binary);
        for (int pidLocal : pids) {
            if (pidLocal == pid) {
                return true;
            }
        }
        return false;
    }

    public void stop(Process process, File binary, int signal, Integer mainPid) {
        if (mainPid != null) {
            stopInternal(process, binary, signal, mainPid);
        }
        for (; ; ) {
            final int[] pids = getPid(binary);
            if (pids == null || pids.length <= 0) {
                break;
            }
            stopInternal(process, binary, signal, pids[0]);
        }
    }

    private void stopInternal(Process process, File binary, int signal, int pid) {
        android.os.Process.sendSignal(pid, signal);
        for (int i = 0; i < 20; i++) {
            if (process != null) {
                try {
                    process.exitValue();
                    break;
                } catch (IllegalThreadStateException ignored) {
                }
            } else if (!isRunning(binary, pid)) {
                break;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }
        }
    }

}

