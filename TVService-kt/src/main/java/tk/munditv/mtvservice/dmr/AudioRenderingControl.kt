package tk.munditv.mtvservice.dmr

import android.util.Log
import org.fourthline.cling.model.types.*
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.*
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl
import org.fourthline.cling.support.renderingcontrol.RenderingControlErrorCode
import org.fourthline.cling.support.renderingcontrol.RenderingControlException

/**
 * @author offbye
 */
class AudioRenderingControl(lastChange: LastChange?, players: Map<UnsignedIntegerFourBytes, MOSMediaPlayer>) : AbstractAudioRenderingControl(lastChange) {
    private val players: Map<UnsignedIntegerFourBytes, MOSMediaPlayer>
    protected fun getPlayers(): Map<UnsignedIntegerFourBytes, MOSMediaPlayer> {
        Log.d(TAG, "getPlayers()")
        return players
    }

    @Throws(RenderingControlException::class)
    protected fun getInstance(instanceId: UnsignedIntegerFourBytes): MOSMediaPlayer {
        Log.d(TAG, "getInstance()")
        return getPlayers()[instanceId]
                ?: throw RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID)
    }

    @Throws(RenderingControlException::class)
    protected fun checkChannel(channelName: String) {
        Log.d(TAG, "checkChannel()")
        if (getChannel(channelName) != Channel.Master) {
            throw RenderingControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: $channelName")
        }
    }

    @Throws(RenderingControlException::class)
    override fun getMute(instanceId: UnsignedIntegerFourBytes, channelName: String): Boolean {
        Log.d(TAG, "getMute()")
        checkChannel(channelName)
        return getInstance(instanceId).volume == 0.0
    }

    @Throws(RenderingControlException::class)
    override fun setMute(instanceId: UnsignedIntegerFourBytes, channelName: String, desiredMute: Boolean) {
        Log.d(TAG, "setMute() = $desiredMute")
        checkChannel(channelName)
        getInstance(instanceId).setMute(desiredMute)
    }

    @Throws(RenderingControlException::class)
    override fun getCommand(instanceId: UnsignedIntegerFourBytes, channelName: String): String {
        Log.d(TAG, "getCommand()")
        checkChannel(channelName)
        return getInstance(instanceId).command!!
    }

    @Throws(RenderingControlException::class)
    override fun getPackages(instanceId: UnsignedIntegerFourBytes, channelName: String): String {
        Log.d(TAG, "getPackages()")
        checkChannel(channelName)
        return getInstance(instanceId).packages
    }

    @Throws(RenderingControlException::class)
    override fun setCommand(instanceId: UnsignedIntegerFourBytes, channelName: String, desiredCommand: String) {
        Log.d(TAG, "setCommand() = $desiredCommand")
        checkChannel(channelName)
        getInstance(instanceId).command = desiredCommand
    }

    @Throws(RenderingControlException::class)
    override fun getVolume(instanceId: UnsignedIntegerFourBytes, channelName: String): UnsignedIntegerTwoBytes {
        Log.d(TAG, "getVolume()")
        checkChannel(channelName)
        val vol = (getInstance(instanceId).volume * 100) as Int
        return UnsignedIntegerTwoBytes(vol*1L)
    }

    @Throws(RenderingControlException::class)
    override fun setVolume(instanceId: UnsignedIntegerFourBytes, channelName: String, desiredVolume: UnsignedIntegerTwoBytes) {
        checkChannel(channelName)
        Log.d(TAG, "setVolume() = $desiredVolume")
        val vol = desiredVolume.value / 100.0
        getInstance(instanceId).volume = vol
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
        private val TAG = AudioRenderingControl::class.java.simpleName
    }

    init {
        Log.d(TAG, "AudioRenderingControl()")
        this.players = players
    }
}