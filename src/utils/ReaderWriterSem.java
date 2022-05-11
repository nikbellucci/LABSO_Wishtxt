package utils;

public class ReaderWriterSem {
    // This class handles critical sections (reader-writer problem) within a single file. An object for each file is needed

    private int readerCount=0;
    private boolean dbReading=false, dbWriting=false;

    public int startRead() {
        synchronized(this) {
            while (dbWriting == true) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            readerCount++;
            if (readerCount == 1)
                dbReading = true;
            return readerCount;
        }
    }


    public int endRead() {
        synchronized(this) {
            --readerCount;
            if (readerCount == 0) {
                notifyAll();
                dbReading = false;
            }

            return readerCount;
        }
    }


    public void startWrite() {
        synchronized(this) {
            while (dbReading == true || dbWriting == true) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            dbWriting = true;
        }
    }


    public void endWrite() {
        synchronized (this) {
            dbWriting = false;
            notifyAll();
        }
    }

    
    //TODO: Remove this getter methods
    public boolean isDbReading() {
        return dbReading;
    }
    public boolean isDbWriting() {
        return dbWriting;
    }
    public int getReaderCount() {
        return readerCount;
    }
}
