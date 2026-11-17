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
    private static final String SPECIAL_CHARACTERS = "*[.|?!-+,()><;:_^]…。．｡！？؟۔‼⁇⁈⁉";

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
                if (textComponent.content().isBlank()) {
                    continue;
                }
            }

            if (first == -1) {
                first = components.size() - 1;
            }
            last = components.size() - 1;
        }

        String messageContent = content.toString();
        if (messageContent.isBlank()) {
            return;
        }

        boolean canEditStart = components.get(first) instanceof TextComponent;
        boolean canEditEnd = components.get(last) instanceof TextComponent;

        if (canEditStart && messageContent.startsWith("./")) {
            TextComponent textComponent = (TextComponent) components.get(first);
            components.set(first, textComponent.content(textComponent.content().substring(1)));
            rebuildMessage(event, components);
            return;
        }

        String strippedContent = messageContent.strip();
        boolean isSplit = strippedContent.split("\\p{javaWhitespace}+").length >= 2;
        boolean isLastCharacterSpecial = SPECIAL_CHARACTERS.indexOf(strippedContent.codePointBefore(strippedContent.length())) >= 0;
        boolean isFirstCharacterSpecial = SPECIAL_CHARACTERS.indexOf(strippedContent.codePointAt(0)) >= 0;

        if (canEditStart && canEditEnd && isSplit && !isLastCharacterSpecial && !isFirstCharacterSpecial) {
            TextComponent textComponent = (TextComponent) components.get(last);
            String lastContent = textComponent.content();
            int end = lastContent.stripTrailing().length();
            components.set(last, textComponent.content(lastContent.substring(0, end) + "." + lastContent.substring(end)));
        }

        if (canEditStart) {
            TextComponent textComponent = (TextComponent) components.get(first);
            String firstContent = textComponent.content();
            int start = firstContent.length() - firstContent.stripLeading().length();
            int firstCharacter = firstContent.codePointAt(start);
            if (Character.isLowerCase(firstCharacter)) {
                StringBuilder capitalizedContent = new StringBuilder(firstContent.substring(0, start));
                capitalizedContent.appendCodePoint(Character.toUpperCase(firstCharacter));
                capitalizedContent.append(firstContent.substring(start + Character.charCount(firstCharacter)));
                components.set(first, textComponent.content(capitalizedContent.toString()));
            }
        }

        rebuildMessage(event, components);
    }

    private void rebuildMessage(AsyncChatEvent event, List<Component> components) {
        Iterator<Component> replacements = components.iterator();
        event.message(replacements.next().toBuilder().mapChildrenDeep(child -> replacements.next()).build());
    }
}
