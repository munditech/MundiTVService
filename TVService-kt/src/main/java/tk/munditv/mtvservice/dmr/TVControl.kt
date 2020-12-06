package tk.munditv.mtvservice.dmr

import android.util.Log
import org.fourthline.cling.model.types.ErrorCode
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.Channel
import tk.munditv.libtvservice.support.tvcontrol.AbstractTVControl
import tk.munditv.libtvservice.support.tvcontrol.TVControlErrorCode
import tk.munditv.libtvservice.support.tvcontrol.TVControlException

class TVControl(lastChange: LastChange?, players: Map<UnsignedIntegerFourBytes, MOSMediaPlayer>) : AbstractTVControl(lastChange) {
    private val players: Map<UnsignedIntegerFourBytes, MOSMediaPlayer>
    @Throws(TVControlException::class)
    protected fun getInstance(instanceId: UnsignedIntegerFourBytes): MOSMediaPlayer {
        Log.d(TAG, "getInstance()")
        return getPlayers()[instanceId]
                ?: throw TVControlException(TVControlErrorCode.INVALID_INSTANCE_ID)
    }

    @Throws(TVControlException::class)
    protected fun checkChannel(channelName: String) {
        Log.d(TAG, "checkChannel()")
        if (getChannel(channelName) != Channel.Master) {
            throw TVControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: $channelName")
        }
    }

    protected fun getPlayers(): Map<UnsignedIntegerFourBytes, MOSMediaPlayer> {
        Log.d(TAG, "getPlayers()")
        return players
    }

    @Throws(TVControlException::class)
    override fun getCommand(instanceId: UnsignedIntegerFourBytes, channelName: String): String {
        Log.d(TAG, "getCommand()")
        checkChannel(channelName)
        return getInstance(instanceId).command!!
    }

    @Throws(TVControlException::class)
    override fun getPackages(instanceId: UnsignedIntegerFourBytes, channelName: String): String {
        Log.d(TAG, "getPackages()")
        checkChannel(channelName)
        return getInstance(instanceId).packages
    }

    @Throws(TVControlException::class)
    override fun setCommand(instanceId: UnsignedIntegerFourBytes, channelName: String, desiredCommand: String) {
        Log.d(TAG, "setCommand() = $desiredCommand")
        checkChannel(channelName)
        getInstance(instanceId).command = desiredCommand
    }

    override fun getCurrentChannels(): Array<Channel> {
        Log.d(TAG, "getCurrentChannels()")
        return arrayOf(
                Channel.Master
        )
    }

    override fun getCurrentInstanceIds(): Array<UnsignedIntegerFourBytes?> {
        Log.d(TAG, "getCurrentInstanceIds()")
        val ids = arrayOfNulls<UnsignedIntegerFourBytes>(getPlayers().size)
        var i = 0
        for (id in getPlayers().keys) {
            ids[i] = id
            i++
        }
        return ids
    }

    companion object {
        private val TAG = TVControl::class.java.simpleName
    }

    init {
        Log.d(TAG, "AudioRenderingControl()")
        this.players = players
    }
}