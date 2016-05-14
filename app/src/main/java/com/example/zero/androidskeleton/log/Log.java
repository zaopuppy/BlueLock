package com.example.zero.androidskeleton.log;

import android.os.Environment;
import com.example.zero.androidskeleton.utils.Counter;

import java.io.*;

/**
 * Created by zero on 5/14/16.
 */
public class Log {

    private static final String TAG = "bluelock|";

    private static final File backupLogFile =
        new File(Environment.getExternalStorageDirectory(), "bluelock_bak.data");

    private static final File logFile =
        new File(Environment.getExternalStorageDirectory(), "bluelock.data");

    private static OutputStreamWriter logWriter = null;

    private static final int MAX_LOG_FILE_LINE = 10*10000;

    private static final Counter counter = new Counter(MAX_LOG_FILE_LINE);

    private static synchronized void write(String msg) {
        counter.update(1);
        if (counter.check()) {
            backupLogFile.delete();
            logFile.renameTo(backupLogFile);
            //
            if (logWriter != null) {
                try {
                    logWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // ignore
                }
                // reopen log file
                logWriter = null;
            }
        }

        if (logWriter == null) {
            try {
                logWriter = new OutputStreamWriter(new FileOutputStream(logFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            logWriter.write(msg + '\n');
            logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void d(String tag, String msg) {
        android.util.Log.d(TAG + tag, msg);
        write("DEBG|" + TAG + tag + '|' + msg);
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(TAG + tag, msg);
        write("INFO|" + TAG + tag + '|' + msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(TAG + tag, msg);
        write("WARN|" + TAG + tag + '|' + msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(TAG + tag, msg);
        write("ERRO|" + TAG + tag + '|' + msg);
    }

}
