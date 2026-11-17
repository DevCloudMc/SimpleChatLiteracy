package org.devcloud.simplechatliteracy.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentIteratorType;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        Component message = event.message();
        List<Component> components = new ArrayList<>();
        StringBuilder content = new StringBuilder();
        int first = -1;
        int last = -1;

        for (Component component : message.iterable(ComponentIteratorType.DEPTH_FIRST)) {
            components.add(component);
            if (component instanceof TextComponent textComponent) {
                content.append(textComponent.content());
                if (textComponent.content().isEmpty()) {
                    continue;
                }
            }

            if (first == -1) {
                first = components.size() - 1;
            }
            last = components.size() - 1;
        }

        if (content.isEmpty()) {
            return;
        }

        String messageContent = content.toString();
        boolean canEditStart = components.get(first) instanceof TextComponent;
        boolean canEditEnd = components.get(last) instanceof TextComponent;

        if (canEditStart && messageContent.startsWith("./")) {
            TextComponent textComponent = (TextComponent) components.get(first);
            components.set(first, textComponent.content(textComponent.content().substring(1)));
            Iterator<Component> replacements = components.iterator();
            event.message(replacements.next().toBuilder().mapChildrenDeep(child -> replacements.next()).build());
            return;
        }

        boolean isSplit = messageContent.split(" ").length >= 2;
        boolean isLastCharacterSpecial = SPECIAL_CHARACTERS.contains(String.valueOf(messageContent.charAt(messageContent.length() - 1)));
        boolean isFirstCharacterSpecial = SPECIAL_CHARACTERS.contains(String.valueOf(messageContent.charAt(0)));

        if (canEditStart && canEditEnd && isSplit && !isLastCharacterSpecial && !isFirstCharacterSpecial) {
            TextComponent textComponent = (TextComponent) components.get(last);
            components.set(last, textComponent.content(textComponent.content() + "."));
        }

        if (canEditStart && Character.isLowerCase(messageContent.charAt(0))) {
            TextComponent textComponent = (TextComponent) components.get(first);
            String firstContent = textComponent.content();
            components.set(first, textComponent.content(Character.toUpperCase(firstContent.charAt(0)) + firstContent.substring(1)));
        }

        Iterator<Component> replacements = components.iterator();
        event.message(replacements.next().toBuilder().mapChildrenDeep(child -> replacements.next()).build());
    }
}
