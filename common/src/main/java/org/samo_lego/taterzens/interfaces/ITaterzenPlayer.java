package org.samo_lego.taterzens.interfaces;

import java.util.UUID;

/**
 * Used for player interactions with Taterzens.
 */
public interface ITaterzenPlayer {
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
     * @param taterzenUuid uuid of taterzen to get msg pos for
     * @return ticks since last message
     */
    int ticksSinceLastMessage(UUID taterzenUuid);

    /**
     * Resets ticks since player got the last message
     * from {@link org.samo_lego.taterzens.npc.TaterzenNPC}.
     * @param taterzenUuid uuid of taterzen to get msg pos for
     */
    void resetMessageTicks(UUID taterzenUuid);

    /**
     * Gets last message position in
     * {@link org.samo_lego.taterzens.npc.NPCData#messages array of messages}.
     * @param taterzenUuid uuid of taterzen to get msg pos for
     * @return last message index
     */
    int getLastMsgPos(UUID taterzenUuid);

    /**
     * Sets the index of last message in
     * {@link org.samo_lego.taterzens.npc.NPCData#messages array of messages}.
     * @param taterzenUuid uuid of taterzen to get msg pos for
     * @param newPos new index for the last sent message.
     */
    void setLastMsgPos(UUID taterzenUuid, int newPos);
}
