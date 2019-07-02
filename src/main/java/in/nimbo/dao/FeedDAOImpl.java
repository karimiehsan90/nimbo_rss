package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Content;
import in.nimbo.entity.Entry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FeedDAOImpl extends DAO implements FeedDAO {
    private ContentDAO contentDAO;

    public FeedDAOImpl(ContentDAO contentDAO) {
        this.contentDAO = contentDAO;
    }

    /**
     * create a list of entries from a ResultSet of JDBC
     * @param resultSet resultSet of database
     * @return list of entries
     */
    private List<Entry> createEntryFromResultSet(ResultSet resultSet) {
        List<Entry> result = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Entry entry = new Entry();
                SyndEntry syndEntry = new SyndEntryImpl();
                entry.setSyndEntry(syndEntry);

                // fetch id
                entry.setId(resultSet.getInt(1));

                // fetch channel
                entry.setChannel(resultSet.getString(2));

                // fetch title
                syndEntry.setTitle(resultSet.getString(3));

                List<Content> contents = contentDAO.getByFeedId(entry.getId());
                // fetch description
                Optional<Content> description = contents.stream()
                        .filter(content -> content.getRelation().equals("description"))
                        .findFirst();
                description.ifPresent(content -> syndEntry.setDescription(content.getSyndContent()));

                // fetch contents
                List<SyndContent> media = contents.stream()
                        .filter(content -> content.getRelation().equals("media"))
                        .map(Content::getSyndContent)
                        .collect(Collectors.toList());
                syndEntry.setContents(media);

                // fetch publication data
                syndEntry.setPublishedDate(resultSet.getDate(4));

                result.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * find entries which their title contain a "searchString" strings
     * @param searchString string want to be in title of entry
     * @return list of entries which their title contain "searchString"
     */
    @Override
    public List<Entry> filterFeeds(String searchString) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM feed WHERE title LIKE ?");
            preparedStatement.setString(1, "%" + searchString + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException("filterFeeds error", e);
        }
    }

    /**
     * fetch all of entries in database
     * @return a list of entries
     */
    @Override
    public List<Entry> getFeeds() {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM feed");
            ResultSet resultSet = preparedStatement.executeQuery();
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException("getFeeds error", e);
        }
    }

    /**
     * save an entry in database
     * contents of entry will be added to 'content' database
     *      description of entry will be added as a content of type 'description'
     *      contents of entry will be added as a content of type 'content'
     * @param entry entry
     * @return entry which it's ID will be set after adding to database
     */
    @Override
    public Entry save(Entry entry) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "INSERT INTO feed(channel, title, pub_date) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, entry.getChannel());
            preparedStatement.setString(2, entry.getSyndEntry().getTitle());
            preparedStatement.setDate(3, new Date(entry.getSyndEntry().getPublishedDate().getTime()));
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            entry.setId(newId);

            // add entry description
            if (entry.getSyndEntry().getDescription() != null) {
                Content content = new Content("description", entry.getSyndEntry().getDescription());
                content.setFeed_id(newId);
                contentDAO.save(content);
            }

            // add entry contents
            if (!entry.getSyndEntry().getContents().isEmpty()) {
                for (SyndContent syndContent : entry.getSyndEntry().getContents()) {
                    Content content = new Content("content", syndContent);
                    content.setFeed_id(newId);
                    contentDAO.save(content);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entry;
    }

    /**
     * check whether database contain a same entry
     * check based on entry.title and entry.channel
     * @param entry which is checked
     * @return true if database contain same entry as given entry
     */
    @Override
    public boolean contain(Entry entry) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM feed WHERE channel=? AND title=?");
            preparedStatement.setString(1, entry.getChannel());
            preparedStatement.setString(2, entry.getSyndEntry().getTitle());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("contain error", e);
        }
    }
}
