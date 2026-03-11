package repository;

import domain.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

public class MessageDbRepository implements Repository<Long, Message> {
    private String url;
    private String user;
    private String password;

    private Repository<Long, Persoana> persoanaRepo;
    private Repository<Long, Duck> duckRepo;

    public MessageDbRepository(String url, String user, String password,
                               Repository<Long, Persoana> persoanaRepo,
                               Repository<Long, Duck> duckRepo) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.persoanaRepo = persoanaRepo;
        this.duckRepo = duckRepo;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private User findUser(Long id, String type) {
        if ("PERSOANA".equals(type)) {
            return persoanaRepo.findOne(id);
        } else if ("RATA".equals(type)) {
            return duckRepo.findOne(id);
        }
        return null;
    }

    private List<User> getRecipientsForMessage(Long messageId) {
        List<User> recipients = new ArrayList<>();
        String sql = "select recipient_id, recipient_type from message_recipients where message_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, messageId);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                Long userId = rs.getLong("recipient_id");
                String type = rs.getString("recipient_type");
                User u = findUser(userId, type);
                if (u != null) {
                    recipients.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipients;
    }

    private Message extractMessage(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long fromId = rs.getLong("from_id");
        String fromType = rs.getString("from_type");
        String text = rs.getString("message_text");
        LocalDateTime data = rs.getTimestamp("data").toLocalDateTime();

        Long replyToId = rs.getObject("reply_to_id") != null ? rs.getLong("reply_to_id") : null;

        User sender = findUser(fromId, fromType);
        List<User> recipients = getRecipientsForMessage(id);

        if (replyToId != null) {
            return new ReplyMessage(id, sender, recipients, text, data, null);
        } else {
            return new Message(id, sender, recipients, text, data);
        }
    }

    @Override
    public Message save(Message entity) {
        String sqlMsg = "insert into messages (from_id, from_type, message_text, data, reply_to_id) values (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlMsg, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, entity.getFrom().getId());
                ps.setString(2, (entity.getFrom() instanceof Persoana) ? "PERSOANA" : "RATA");
                ps.setString(3, entity.getMessage());
                ps.setTimestamp(4, Timestamp.valueOf(entity.getDate()));

                if (entity instanceof ReplyMessage && ((ReplyMessage) entity).getOriginalMessage() != null) {
                    ps.setLong(5, ((ReplyMessage) entity).getOriginalMessage().getId());
                } else {
                    ps.setObject(5, null);
                }

                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                }
            }

            String sqlRec = "insert into message_recipients (message_id, recipient_id, recipient_type) values (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlRec)) {
                for(User dest : entity.getTo()) {
                    ps.setLong(1, entity.getId());
                    ps.setLong(2, dest.getId());
                    ps.setString(3, (dest instanceof Persoana) ? "PERSOANA" : "RATA");
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return entity;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Iterable<Message> findAll() {
        String sql = "select * from messages order by data";

        //Map pentru a accesa usor mesaje dupa id
        Map<Long, Message> messageMap = new HashMap<>();

        Map<Long, Long> replyRelations = new HashMap<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {
                Message m = extractMessage(rs);
                messageMap.put(m.getId(), m);

                Long replyToId = rs.getObject("reply_to_id") != null ? rs.getLong("reply_to_id") : null;
                if (replyToId != null) {
                    replyRelations.put(m.getId(), replyToId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Setam parintii
        for (Map.Entry<Long, Long> entry : replyRelations.entrySet()) {
            Long childId = entry.getKey();
            Long parentId = entry.getValue();

            Message child = messageMap.get(childId);
            Message parent = messageMap.get(parentId);

            if (child instanceof ReplyMessage && parent != null) {
                ((ReplyMessage) child).setOriginalMessage(parent);
            }
        }

        //Sortam lista cronologic
        List<Message> resultList = new ArrayList<>(messageMap.values());
        resultList.sort(Comparator.comparing(Message::getDate));

        return resultList;
    }

    @Override
    public Message findOne(Long id) {
        String sql = "select * from messages where id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return extractMessage(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Message delete(Long id) {
        return null;
    }
}