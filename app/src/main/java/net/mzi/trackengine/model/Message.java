package net.mzi.trackengine.model;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 75623478772657L;
    String message;
    String ticketId;
    String ticketNumber;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
