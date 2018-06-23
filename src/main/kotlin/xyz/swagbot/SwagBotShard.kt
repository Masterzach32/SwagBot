package xyz.swagbot

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.masterzach32.commands4k.CommandListener
import net.masterzach32.commands4k.Permission
import org.slf4j.LoggerFactory
import sx.blah.discord.api.IShard
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import xyz.swagbot.commands.*
import xyz.swagbot.commands.admin.AutoAssignRoleCommand
import xyz.swagbot.commands.admin.ChangePrefixCommand
import xyz.swagbot.commands.admin.EditPermissionsCommand
import xyz.swagbot.commands.admin.GameSwitchCommand
import xyz.swagbot.commands.mod.BringCommand
import xyz.swagbot.commands.mod.MassAfkCommand
import xyz.swagbot.commands.mod.MigrateCommand
import xyz.swagbot.commands.mod.PruneCommand
import xyz.swagbot.commands.music.*
import xyz.swagbot.commands.normal.*
import xyz.swagbot.database.getBotDMPermission
import xyz.swagbot.database.getBotPermission
import xyz.swagbot.database.getCommandPrefix

class SwagBotShard(val shard: IShard) {

    val logger = LoggerFactory.getLogger("SwagBot Shard ${shard.info[0]}")

    val commandManager = CommandListener(::getCommandPrefix) { this.getUserPermission(it) }

    val audioManager: AudioPlayerManager = DefaultAudioPlayerManager()

    init {
        registerCommands()

        AudioSourceManagers.registerRemoteSources(audioPlayerManager)


    }

    private fun getCommandPrefix(guild: IGuild?): String = guild?.getCommandPrefix() ?: config.getString("defaults.command_prefix")

    private fun IUser.getUserPermission(guild: IGuild?): Permission {
        return if (guild == null)
            this.getBotDMPermission()
        else {
            val perm = this.getBotPermission(guild)
            if (guild.owner == this && perm != Permission.DEVELOPER)
                Permission.ADMIN
            else
                perm
        }
    }

    private fun registerCommands() {
        // basic
        cmds.add(
                DonateCommand,
                InfoCommand,
                InviteCommand,
                PingCommand,
                SupportCommand
        )
        // music
        cmds.add(AutoPlayCommand)
        cmds.add(ClearCommand)
        cmds.add(LeaverClearCommand)
        cmds.add(LoopCommand)
        cmds.add(MoveTrackCommand)
        cmds.add(NowPlayingCommand)
        cmds.add(PauseResumeCommand)
        cmds.add(PlayCommand)
        cmds.add(QueueCommand)
        cmds.add(RemoveDuplicatesCommand)
        cmds.add(RemoveTrackCommand)
        cmds.add(ReplayCommand)
        cmds.add(SearchCommand)
        cmds.add(SeekCommand)
        cmds.add(ShuffleCommand)
        cmds.add(SkipCommand)
        cmds.add(SkipToCommand)
        cmds.add(VoteSkipCommand)
        // normal
        cmds.add(LookupCRNCommand)
        cmds.add(CatCommand)
        cmds.add(DogCommand)
        cmds.add(BrawlCommand)
        cmds.add(IAmCommand)
        cmds.add(IAmNotCommand)
        cmds.add(JoinCommand)
        cmds.add(LmgtfyCommand)
        cmds.add(MassAfkCommand)
        cmds.add(R8BallCommand)
        cmds.add(RockPaperScissorsCommand)
        cmds.add(StrawpollCommand)
        cmds.add(UrbanDictionaryCommand)
        cmds.add(UrlShortenCommand)
        cmds.add(VoiceCommand)
        cmds.add(VolumeCommand)
        // mod
        cmds.add(BringCommand)
        cmds.add(MigrateCommand)
        cmds.add(PruneCommand)
        // admin
        cmds.add(AutoAssignRoleCommand)
        cmds.add(ChangePrefixCommand)
        //cmds.add(ChatOnlyCommand)
        cmds.add(EditPermissionsCommand)
        cmds.add(GameSwitchCommand)
        // dev
        cmds.add(
                ShutdownCommand,
                GarbageCollectionCommand,
                JvmStatsCommand,
                SetMotdCommand,
                StatsCommand,
                ShardStatusCommand
        )

        cmds.sortCommands()
    }
}