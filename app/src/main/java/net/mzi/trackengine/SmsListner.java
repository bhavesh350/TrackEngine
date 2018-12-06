package net.mzi.trackengine;

import net.mzi.trackengine.model.TicketInfoClass;

public interface SmsListner{
    void messageReceived(TicketInfoClass messageText);
}
