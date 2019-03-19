package message;

import util.string.StringUtils;

import java.util.ArrayDeque;

public class MessageQueue extends ArrayDeque<MessageUpdate> {
    public void addFirst(String message) {
        this.addFirst(new MessageUpdate(message));
    }

    public boolean add(String message) {
        return this.add(new MessageUpdate(message));
    }

    // Returns if there are no messages in the queue or if the next message is empty
    public boolean isEmptyMessage() {
        return this.isEmpty() || StringUtils.isNullOrEmpty(this.peek().getMessage());
    }
}
