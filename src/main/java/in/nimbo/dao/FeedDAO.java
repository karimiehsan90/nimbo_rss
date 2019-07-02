package in.nimbo.dao;

import in.nimbo.entity.Entry;

import java.util.List;

public interface FeedDAO {
    List<Entry> getEntryByTitle(String title);

    List<Entry> getEntries();

    Entry save(Entry entry);

    /**
     * check whether an entry exists in database
     * @param entry which is checked
     * @return true if entry exists in database
     *          based on entry.title and entry.channel
     */
    boolean contain(Entry entry);
}
