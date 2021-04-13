package org.samo_lego.taterzens.interfaces;

public interface TaterzenPlayer {
    /**
     * Gets the last time player has interacted with
     * {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     * Used to prevent double triggering of
     * right-click actions.
     *
     * @return last interaction time
     */
    long getLastInteractionTime();

    /**
     * Sets last interaction time
     * with {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     * @param time current player interaction time
     */
    void setLastInteraction(long time);

    /**
     * Gets how many ticks have passed
     * since player got last message.
     * @return ticks since last message
     */
    int ticksSinceLastMessage();

    /**
     * Resets ticks since player got the last message
     * from {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     */
    void resetMessageTicks();

    /**
     * Gets last message position in
     * {@link org.samo_lego.taterzens.npc.NPCData#messages array of messages}.
     * @return last message index
     */
    int getLastMsgPos();

    /**
     * Sets the index of last message in
     * {@link org.samo_lego.taterzens.npc.NPCData#messages array of messages}.
     * @param newPos new index for the last sent message.
     */
    void setLastMsgPos(int newPos);
}
