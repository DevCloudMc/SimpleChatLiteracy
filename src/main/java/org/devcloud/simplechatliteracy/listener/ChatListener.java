package org.devcloud.simplechatliteracy.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private static final String SPECIAL_CHARACTERS = "*[.|?!-+,()><;:_^]";

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncChatEvent event) {
        if (event == null) {
            return;
        }

        spellCheck(event);
    }

    private void spellCheck(AsyncChatEvent event) {
        Component originalMessage = event.originalMessage();
        if (!(originalMessage instanceof TextComponent textComponent)) {
            return;
        }

        if (textComponent.content().isEmpty()) {
            return;
        }

        int length = textComponent.content().length() - 1;
        String messageContent = textComponent.content();

        if (messageContent.startsWith("./")) {
            messageContent = messageContent.substring(1);
            textComponent = textComponent.content(messageContent);
            event.message(textComponent);
            length -= 1;
        }

        boolean isSplit = messageContent.split(" ").length >= 2;
        boolean isLastCharacterSpecial = SPECIAL_CHARACTERS.contains(String.valueOf(messageContent.charAt(length)));
        boolean isFirstCharacterSpecial = SPECIAL_CHARACTERS.contains(String.valueOf(messageContent.charAt(0)));


        if (isSplit && !isLastCharacterSpecial && !isFirstCharacterSpecial) {
            messageContent += ".";
            textComponent = textComponent.content(messageContent);
            event.message(textComponent);
        }

        if (Character.isLowerCase(messageContent.charAt(0))) {
            messageContent = Character.toUpperCase(messageContent.charAt(0)) + messageContent.substring(1);
            textComponent = textComponent.content(messageContent);
            event.message(textComponent);
        }
    }
}
