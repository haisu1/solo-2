package com.office.supplies.common;

import com.office.supplies.entity.User;

public class UserContext {
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        USER_HOLDER.set(user);
    }

    public static User getCurrentUser() {
        return USER_HOLDER.get();
    }

    public static Long getCurrentUserId() {
        User user = USER_HOLDER.get();
        return user != null ? user.getId() : null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
