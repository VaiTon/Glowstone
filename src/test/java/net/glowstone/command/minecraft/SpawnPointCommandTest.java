package net.glowstone.command.minecraft;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.eq;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import net.glowstone.command.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.hamcrest.MatcherAssert;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@PrepareForTest({Bukkit.class, CommandUtils.class})
public class SpawnPointCommandTest extends CommandTestWithFakePlayers<SpawnPointCommand> {

    private CommandSender opPlayer;

    public SpawnPointCommandTest() {
        super(SpawnPointCommand::new, "player1", "player2", "thePlayer3");
    }

    @Override
    @BeforeMethod
    public void before() {
        super.before();

        opPlayer = PowerMockito.mock(Player.class);
        Mockito.when(opSender.getServer()).thenReturn(server);
        Mockito.when(opPlayer.hasPermission(Mockito.anyString())).thenReturn(true);
        Mockito.when(opPlayer.getName()).thenReturn("ChuckNorris");
        Mockito.when(((Entity) opPlayer).getLocation()).thenReturn(location);
        Mockito.when(world.getMaxHeight()).thenReturn(50);
        PowerMockito.stub(PowerMockito.method(CommandUtils.class, "getWorld", CommandSender.class))
            .toReturn(world);
    }

    @Test
    public void testExecuteFailsWithoutPermission() {
        MatcherAssert.assertThat(command.execute(sender, "label", new String[0]), is(false));
        Mockito.verify(sender).sendMessage(eq(ChatColor.RED
            + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."));
    }

    @Test
    public void testExecuteFailsWithTwoParameters() {
        MatcherAssert.assertThat(command.execute(opSender, "label", new String[2]), is(false));
        Mockito.verify(opSender).sendMessage(eq(ChatColor.RED
            + "Usage: /spawnpoint OR /spawnpoint <player> OR /spawnpoint <player> <x> <y> <z>"));
    }

    @Test
    public void testExecuteFailsWithThreeParameters() {
        MatcherAssert.assertThat(command.execute(opSender, "label", new String[3]), is(false));
        Mockito.verify(opSender).sendMessage(eq(ChatColor.RED
            + "Usage: /spawnpoint OR /spawnpoint <player> OR /spawnpoint <player> <x> <y> <z>"));
    }

    @Test
    public void testExecuteFailsWithSenderNotPlayer() {
        MatcherAssert.assertThat(command.execute(opSender, "label", new String[0]), is(false));
        Mockito.verify(opSender).sendMessage(eq(ChatColor.RED
            + "You must specify which player you wish to perform this action on."));
    }

    @Test
    public void testExecuteFailsUnknownTarget() {
        MatcherAssert.assertThat(command.execute(opSender, "label", new String[]{"player"}), is(false));
        Mockito.verify(opSender).sendMessage(eq(ChatColor.RED + "Player 'player' cannot be found"));
    }

    @Test
    public void testExecuteFailsWithDefaultLocation() {
        MatcherAssert.assertThat(command.execute(opSender, "label", new String[]{"player1"}), is(false));
        Mockito.verify(opSender).sendMessage(
            eq(ChatColor.RED + "Default coordinates can not be used without a physical user."));
    }

    @Test
    public void testExecuteFailsWithRelativeLocation() {
        MatcherAssert.assertThat(command.execute(opSender, "label", new String[]{"player1", "~2", "3", "4"}), is(false));
        Mockito.verify(opSender).sendMessage(
            eq(ChatColor.RED + "Relative coordinates can not be used without a physical user."));
    }

    @Test
    public void testExecuteFailsWithYCoordinatesTooHigh() {
        MatcherAssert.assertThat(command.execute(opSender, "label", new String[]{"player1", "2", "10000", "4"}), is(false));
        Mockito.verify(opSender).sendMessage(
            eq(ChatColor.RED + "'10000.0' is too high for the current world. Max value is '50'."));
    }

    @Test
    public void testExecuteFailsWithYCoordinatesTooSmall() {
        MatcherAssert.assertThat(command.execute(opSender, "label", new String[]{"player1", "2", "-10000", "4"}), is(false));
        Mockito.verify(opSender).sendMessage(
            eq(ChatColor.RED + "The y coordinate (-10000.0) is too small, it must be at least 0."));
    }

    @Test
    public void testExecuteSucceedsWithCurrentLocation() {
        MatcherAssert.assertThat(command.execute(opPlayer, "label", new String[0]), is(true));
        Mockito.verify((Player) opPlayer)
            .setBedSpawnLocation(new Location(world, 10.5, 20.0, 30.5), true);
    }

    @Test
    public void testExecuteSucceedsOnAnotherPlayerWithCurrentLocation() {
        MatcherAssert.assertThat(command.execute(opPlayer, "label", new String[]{"player1"}), is(true));
        Mockito.verify(Bukkit.getPlayerExact("player1"))
            .setBedSpawnLocation(new Location(world, 10.5, 20.0, 30.5), true);
    }

    @Test
    public void testExecuteSucceedsOnAnotherPlayerWithSpecificLocation() {
        MatcherAssert.assertThat(command.execute(opPlayer, "label", new String[]{"player1", "30", "20", "10"}), is(true));
        Mockito.verify(Bukkit.getPlayerExact("player1"))
            .setBedSpawnLocation(new Location(world, 30.5, 20.0, 10.5), true);
    }

    @Test
    public void testExecuteSucceedsAllPlayersWithCurrentLocation() {
        MatcherAssert.assertThat(command.execute(opPlayer, "label", new String[]{"@a"}), is(true));
        Mockito.verify(Bukkit.getPlayerExact("player1"))
            .setBedSpawnLocation(new Location(world, 10.5, 20.0, 30.5), true);
        Mockito.verify(Bukkit.getPlayerExact("player2"))
            .setBedSpawnLocation(new Location(world, 10.5, 20.0, 30.5), true);
        Mockito.verify(Bukkit.getPlayerExact("thePlayer3"))
            .setBedSpawnLocation(new Location(world, 10.5, 20.0, 30.5), true);
    }

    @Test
    public void testExecuteSucceedsAllPlayersWithSpecificLocation() {
        MatcherAssert.assertThat(command.execute(opPlayer, "label", new String[]{"@a", "30", "20", "10"}), is(true));
        Mockito.verify(Bukkit.getPlayerExact("player1"))
            .setBedSpawnLocation(new Location(world, 30.5, 20.0, 10.5), true);
        Mockito.verify(Bukkit.getPlayerExact("player2"))
            .setBedSpawnLocation(new Location(world, 30.5, 20.0, 10.5), true);
        Mockito.verify(Bukkit.getPlayerExact("thePlayer3"))
            .setBedSpawnLocation(new Location(world, 30.5, 20.0, 10.5), true);
    }

    @Test
    public void testExecuteSucceedsAllPlayersWithRelativeLocation() {
        MatcherAssert.assertThat(command.execute(opPlayer, "label", new String[]{"@a", "30", "~20", "10"}), is(true));
        Mockito.verify(Bukkit.getPlayerExact("player1"))
            .setBedSpawnLocation(new Location(world, 30.5, 40.0, 10.5), true);
        Mockito.verify(Bukkit.getPlayerExact("player2"))
            .setBedSpawnLocation(new Location(world, 30.5, 40.0, 10.5), true);
        Mockito.verify(Bukkit.getPlayerExact("thePlayer3"))
            .setBedSpawnLocation(new Location(world, 30.5, 40.0, 10.5), true);
    }

    @Test
    public void testTabComplete() {
        MatcherAssert.assertThat(command.tabComplete(opSender, "alias", new String[0]), is(Collections.emptyList()));
        MatcherAssert.assertThat(command.tabComplete(opSender, "alias", new String[]{""}), is(ImmutableList.of("player1", "player2", "thePlayer3")));
        MatcherAssert.assertThat(command.tabComplete(opSender, "alias", new String[]{"player"}), is(ImmutableList.of("player1", "player2")));
        MatcherAssert.assertThat(command.tabComplete(opSender, "alias", new String[]{"th"}), is(ImmutableList.of("thePlayer3")));
        MatcherAssert.assertThat(command.tabComplete(opSender, "alias", new String[]{"12", "test"}), is(Collections.emptyList()));
        MatcherAssert.assertThat(command.tabComplete(opSender, "alias", new String[]{"player", "test"}), is(Collections.emptyList()));
    }
}
