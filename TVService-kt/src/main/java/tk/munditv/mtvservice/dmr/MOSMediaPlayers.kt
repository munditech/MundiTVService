package tk.munditv.mtvservice.dmr

import android.content.Context
import android.util.Log
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes
import org.fourthline.cling.support.lastchange.LastChange
import org.fourthline.cling.support.model.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author offbye
 */
open class MOSMediaPlayers(numberOfPlayers: Int,
                           context: Context,
                           avTransportLastChange: LastChange,
                           renderingControlLastChange: LastChange,
                           tvControlLastChange: LastChange) : ConcurrentHashMap<UnsignedIntegerFourBytes?, MOSMediaPlayer?>(numberOfPlayers) {
    protected val avTransportLastChange: LastChange
    protected val renderingControlLastChange: LastChange
    protected val tvControlLastChange: LastChange
    private val mContext: Context
    protected open fun onPlay(player: MOSMediaPlayer) {
        Log.d(TAG, "Player is playing: " + player.instanceId)
    }

    protected open fun onStop(player: MOSMediaPlayer) {
        Log.d(TAG, "Player is stopping: " + player.instanceId)
    }

    companion object {
        private val TAG = MOSMediaPlayers::class.java.simpleName
    }

    init {
        Log.d(TAG, "MOSMediaPlayers()")
        mContext = context
        this.avTransportLastChange = avTransportLastChange
        this.renderingControlLastChange = renderingControlLastChange
        this.tvControlLastChange = tvControlLastChange
        for (i in 0 until numberOfPlayers) {
            val player: MOSMediaPlayer = object : MOSMediaPlayer(
                    UnsignedIntegerFourBytes(i*1L),
                    mContext,
                    avTransportLastChange,
                    renderingControlLastChange,
                    tvControlLastChange
            ) {
                override fun transportStateChanged(newState: TransportState) {
                    super.transportStateChanged(newState)
                    if (newState == TransportState.PLAYING) {
                        onPlay(this)
                    } else if (newState == TransportState.STOPPED) {
                        onStop(this)
                    }
                }
            }
            put(player.instanceId, player)
        }
    }

}