package fr.oqom.ouquonmange.models;

public class MessageEvent {
    String message;

    public MessageEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "message='" + message + '\'' +
                '}';
    }
}
