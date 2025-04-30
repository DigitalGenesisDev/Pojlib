package org.digitalgenesis.utils;

import org.digitalgenesis.API;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;

/** Singleton class made to log on one file
 * The singleton part can be removed but will require more implementation from the end-dev
 */
public class Logger {

    /* Instance variables */
    private final File mLogFile;
    private PrintStream mLogStream;
    private WeakReference<eventLogListener> mLogListenerWeakReference = null;

    /* No public construction */
    private Logger(){
        this("latestlog.txt");
    }

    private Logger(String fileName){
        mLogFile = new File(API.logPath, fileName);
        // Make a new instance of the log file
        mLogFile.delete();
        try {
            mLogFile.createNewFile();
            mLogStream = new PrintStream(mLogFile.getAbsolutePath());
        }catch (IOException e){e.printStackTrace();}

    }

    private static final class SLoggerSingletonHolder {
        static final Logger sLoggerSingleton = new Logger();
    }

    /** Get the instance of the logger
     * @return The instance of the logger
     */
    public static Logger getInstance(){
        return SLoggerSingletonHolder.sLoggerSingleton;
    }

    /**
     * Print the text to the log file if not censored
     * @param text The text to append
     */
    public void appendToLog(String text){
        if(shouldCensorLog(text)) return;
        appendToLogUnchecked(text);
    }

    /**
     * Print the text to the log file, no china censoring there
     * @param text The text to append
     * */
    public void appendToLogUnchecked(String text){
        mLogStream.println(text);
        notifyLogListener(text);
    }

    /** Reset the log file, effectively erasing any previous logs */
    public void reset(){
        try{
            mLogFile.delete();
            mLogFile.createNewFile();
            mLogStream = new PrintStream(mLogFile.getAbsolutePath());
        }catch (IOException e){ e.printStackTrace();}
    }

    /** Disables the printing */
    public void shutdown(){
        mLogStream.close();
    }

    /**
     * Perform various checks to see if the log is safe to print
     * Subclasses may want to override this behavior
     * @param text The text to check
     * @return Whether the log should be censored
     */
    private static boolean shouldCensorLog(String text){
        return text.contains("Session ID is");
    }

    /** Small listener for anything listening to the log */
    public interface eventLogListener {
        void onEventLogged(String text);
    }

    /** Link a log listener to the logger
     * @param logListener The listener to link
     */
    public void setLogListener(eventLogListener logListener){
        this.mLogListenerWeakReference = new WeakReference<>(logListener);
    }

    /**
     * Notifies the event listener, if it exists
     * @param text The text to notify
     */
    private void notifyLogListener(String text){
        if(mLogListenerWeakReference == null) return;
        eventLogListener logListener = mLogListenerWeakReference.get();
        if(logListener == null){
            mLogListenerWeakReference = null;
            return;
        }
        logListener.onEventLogged(text);
    }
}
