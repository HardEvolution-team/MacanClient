package com.ded.macanclient.features.modules.player;

import com.ded.macanclient.features.Category;
import com.ded.macanclient.features.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class FriendManager extends Module {
    private static final List<String> friends = new ArrayList<>();

    public FriendManager(String name, Category category) {
        super(name, category);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled) return;
        // Логика обновления списка друзей, если нужно
    }

    public static void addFriend(String username) {
        if (!friends.contains(username)) {
            friends.add(username);
            System.out.println("Added friend: " + username);
        }
    }

    public static void removeFriend(String username) {
        if (friends.remove(username)) {
            System.out.println("Removed friend: " + username);
        }
    }

    public static boolean isFriend(String username) {
        return friends.contains(username);
    }

    public static List<String> getFriends() {
        return new ArrayList<>(friends);
    }
}