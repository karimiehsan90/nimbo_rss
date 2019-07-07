package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import in.nimbo.TestUtility;
import in.nimbo.dao.EntryDAO;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EntryDAO.class, Entry.class})
public class RSSServiceTest {
    private static EntryDAO entryDAO;
    private static RSSService rssService;

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
    }

    @Before
    public void beforeAnyTest() {
        entryDAO = PowerMockito.mock(EntryDAO.class);
        rssService = spy(new RSSService(entryDAO));
    }

    @Test
    public void addSiteEntries() {
        Entry entry = TestUtility.createEntry("channel", "title", "link", new Date(), "content", "description");
        Site site = new Site("site-name", "site-link");
        List<Entry> entries = new ArrayList<>();
        entries.add(entry);

        when(entryDAO.save(entry)).thenReturn(entry);
        doReturn("content").when(rssService).getContentOfRSSLink(entry.getLink());

        List<Entry> savedEntries = rssService.addSiteEntries(site, entries);
        assertEquals(savedEntries.size(), entries.size());
    }

    @Test
    public void getEntries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(TestUtility.createEntry("channel", "title 1", "link 1", new Date(), null, null));
        entries.add(TestUtility.createEntry("channel", "title 2", "link 2", new Date(), null, null));
        entries.add(TestUtility.createEntry("channel", "title 3", "link 3", new Date(), null, null));

        SyndFeed syndFeed = new SyndFeedImpl();
        syndFeed.setTitle("channel");
        List<SyndEntry> syndEntries = new ArrayList<>();
        syndFeed.setEntries(syndEntries);
        for (Entry entry : entries) {
            SyndEntry syndEntry = new SyndEntryImpl();
            syndEntry.setTitle(entry.getTitle());
            syndEntry.setLink(entry.getLink());
            syndEntry.setPublishedDate(entry.getPublicationDate());
            syndEntries.add(syndEntry);
        }

        assertEquals(entries, rssService.getEntries(syndFeed));
    }
}
