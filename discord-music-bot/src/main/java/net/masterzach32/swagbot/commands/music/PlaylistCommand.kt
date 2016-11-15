/*
    SwagBot-java
    Copyright (C) 2016 Zach Kozar

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.masterzach32.swagbot.commands.music

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.getBotLockedMessage
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.music.LocalPlaylist
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class PlaylistCommand: Command("Playlist", "playlist", "plist") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val guild = App.guilds.getGuildSettings(channel.guild)
        if(guild.botLocked)
            return getBotLockedMessage(channel)
        if(args.isEmpty())
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        val perms = true // TODO get user perms
        val action = args[0]
        val manager = guild.playlistManager
        val builder = MetadataMessageBuilder(channel)
        // admin actions
        if (action == "load" && perms) {
            val msg = MetadataMessageBuilder(channel).withContent("Loading playlists from save...").build()
            manager.load()
            msg.edit("${user.mention()} Finished loading all playlists.")
        } else if (action == "save" && perms) {
            manager.save()
            builder.withContent("Saved all playlists.")
        } else if (action == "list") {
            builder.withContent("**Playlists**: $manager")
        }
        // not we need a playlist name
        if (args.size < 2)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        val name = args[1]
        if (action == "create") {
            manager.add(LocalPlaylist(name, false, false))
            builder.withContent("Created playlist **$name**")
        } else if (action == "delete" && perms) {
            if(manager.remove(name))
                builder.withContent("Removed playlist **$name**")
            else
                builder.withContent("Could not find playlist **$name**")
        } else if (action == "info") {
            manager[name]?.queue(user, guild)
        }
        if (action == "import") {
            builder.withContent("Importing playlists from youtube still needs to be re-implemented.")
        }
        return null
        // TODO finish
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<action> <playlist name> [link/param]", "Basic parameters.")
        usage.put("create <playlist name>", "Create a new playlist.")
        usage.put("import <playlist name> <youtube playlist link>", "Import a playlist from youtube.")
        usage.put("add <playlist name> <youtube/soundcloud link>", "Add a song to the playlist.")
        usage.put("remove <playlist name> <youtube/soundcloud link>", "Remove a song from the playlist.")
        usage.put("delete <playlist name>", "Delete a playlist.")
        usage.put("queue <playlist name>", "Add a playlist to the queue.")
        usage.put("list", "List all playlists.")
        usage.put("info <playlist name> [page number]", "List all songs in a playlist.")
    }
}