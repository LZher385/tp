package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.friend.Friend;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyFriendsList {

    /**
     * Returns an unmodifiable view of the friends list.
     * This list will not contain any duplicate friends.
     */
    ObservableList<Friend> getFriendsList();

}
