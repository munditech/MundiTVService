
package tk.munditv.mcontroller.dmr;

import android.content.Context;
import android.util.Log;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.TransportState;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author offbye
 */
public class MOSMediaPlayers extends ConcurrentHashMap<UnsignedIntegerFourBytes, MOSMediaPlayer> {

    private final static String TAG = MOSMediaPlayers.class.getSimpleName();

    final protected LastChange avTransportLastChange;
    final protected LastChange renderingControlLastChange;
    final protected LastChange tvControlLastChange;

    private Context mContext;


    public MOSMediaPlayers(int numberOfPlayers,
                           Context context,
                           LastChange avTransportLastChange,
                           LastChange renderingControlLastChange,
                           LastChange tvControlLastChange) {
        super(numberOfPlayers);
        Log.d(TAG, "MOSMediaPlayers()");

        this.mContext = context;
        this.avTransportLastChange = avTransportLastChange;
        this.renderingControlLastChange = renderingControlLastChange;
        this.tvControlLastChange = tvControlLastChange;

        for (int i = 0; i < numberOfPlayers; i++) {

            MOSMediaPlayer player =
                    new MOSMediaPlayer(
                            new UnsignedIntegerFourBytes(i),
                            mContext,
                            avTransportLastChange,
                            renderingControlLastChange,
                            tvControlLastChange
                    ) {
                        @Override
                        protected void transportStateChanged(TransportState newState) {
                            super.transportStateChanged(newState);
                            if (newState.equals(TransportState.PLAYING)) {
                                onPlay(this);
                            } else if (newState.equals(TransportState.STOPPED)) {
                                onStop(this);
                            }
                        }
                    };
            put(player.getInstanceId(), player);
        }
    }

    protected void onPlay(MOSMediaPlayer player) {
        Log.d(TAG, "Player is playing: " + player.getInstanceId());
    }

    protected void onStop(MOSMediaPlayer player) {
        Log.d(TAG, "Player is stopping: " + player.getInstanceId());
    }
}
