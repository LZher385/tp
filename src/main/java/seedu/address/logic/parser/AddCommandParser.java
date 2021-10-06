package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FRIEND_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FRIEND_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GAME;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.friend.Friend;
import seedu.address.model.friend.FriendId;
import seedu.address.model.friend.FriendName;
import seedu.address.model.game.Game;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                // TODO: Update to match Add Friend command specification in User Guide.
                // currently friendId and friendName is required
                ArgumentTokenizer.tokenize(args, PREFIX_FRIEND_ID, PREFIX_FRIEND_NAME, PREFIX_GAME);

        if (!arePrefixesPresent(argMultimap, PREFIX_FRIEND_ID, PREFIX_FRIEND_NAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }


        FriendId friendId = ParserUtil.parseFriendId(argMultimap.getValue(PREFIX_FRIEND_ID).get());
        FriendName friendName = ParserUtil.parseFriendName(argMultimap.getValue(PREFIX_FRIEND_NAME).get());
        Set<Game> gameSet = ParserUtil.parseGames(argMultimap.getAllValues(PREFIX_GAME));

        Friend friend = new Friend(friendId, friendName, gameSet);

        return new AddCommand(friend);
    }

}
