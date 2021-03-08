package org.samo_lego.taterzens.interfaces;

public interface TaterzenPlayer {
    long getLastInteractionTime();
    void setLastInteraction(long time);

    int ticksSinceLastMessage();
    void resetMessageTicks();

    int getCurrentMsgPos();
    void setCurrentMsgPos(int newPos);
}
