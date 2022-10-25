package utils;

public class ReaderWriterSem {
    // This class handles critical sections (reader-writer problem) within a single
    // file. An object for each file is needed

    private int readerCount = 0;
    private boolean dbReading = false, dbWriting = false;

    /**
     * This function returns the number of readers currently reading from the
     * database.
     * 
     * @return The number of readers currently reading the book.
     */
    public int getReaderCount() {
        return this.readerCount;
    }

    /**
     * Returns true if the database is currently being read from.
     * 
     * @return The value of the dbReading variable.
     */
    public boolean isDbReading() {
        return this.dbReading;
    }

    /**
     * > Returns true if the database is currently being written to
     * 
     * @return The value of the dbWriting variable.
     */
    public boolean isDbWriting() {
        return this.dbWriting;
    }

    /**
     * If the database is being written to, wait until it's not being written to,
     * then increment the
     * number of readers and if it's the first reader, set the database to be read
     * 
     * @return The number of readers currently reading the database.
     */
    public int startRead() {
        synchronized (this) {
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

    /**
     * The function decrements the reader count, and if the reader count is 0, it
     * notifies all waiting
     * threads and sets the dbReading flag to false
     * 
     * @return The number of readers currently reading the database.
     */
    public int endRead() {
        synchronized (this) {
            --readerCount;
            if (readerCount == 0) {
                notifyAll();
                dbReading = false;
            }

            return readerCount;
        }
    }

    /**
     * If the database is being read or written to, wait until it is not being read
     * or written to,
     * then set the database to be written to
     */
    public void startWrite() {
        synchronized (this) {
            while (dbReading == true || dbWriting == true) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            dbWriting = true;
        }
    }

    /**
     * If the database is being written to, wait until it's done, then set the
     * database to not being
     * written to, and notify all threads that are waiting for the database to be
     * done being written
     * to.
     */
    public void endWrite() {
        synchronized (this) {
            dbWriting = false;
            notifyAll();
        }
    }

}
