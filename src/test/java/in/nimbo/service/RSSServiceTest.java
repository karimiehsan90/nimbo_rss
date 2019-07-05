package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import in.nimbo.TestUtility;
import in.nimbo.dao.EntryDAO;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EntryDAO.class, Entry.class})
public class RSSServiceTest {
    private static EntryDAO entryDAO;
    private static RSSService service;

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
        entryDAO = PowerMockito.mock(EntryDAO.class);
        service = spy(new RSSService(entryDAO));
    }

    @Test
    public void addSiteEntries() {
        SyndFeed feed = new SyndFeedImpl();

        Entry entry = TestUtility.createEntry("channel", "title", "link", new Date(), "content", "description");
        Site site = new Site("site-name", "site-link");

        List<SyndEntry> entries = new ArrayList<>();
        entries.add(entry.getSyndEntry());
        feed.setEntries(entries);

        when(entryDAO.save(entry)).thenReturn(entry);
        doReturn("content").when(service).getContentOfRSSLink(entry.getSyndEntry().getLink());

        List<Entry> savedEntries = service.addSiteEntries(site, feed);
        assertEquals(savedEntries.size(), feed.getEntries().size());
    }
}
