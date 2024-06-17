package communications.protocol.messages;

import servers.handlers.guessingGames.ResultInfo;

import java.util.List;

public record GuessingGameResult(List<ResultInfo> gameResults) {}
