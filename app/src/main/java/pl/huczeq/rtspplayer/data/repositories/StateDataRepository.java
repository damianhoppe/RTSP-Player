package pl.huczeq.rtspplayer.data.repositories;

import androidx.lifecycle.MutableLiveData;

import pl.huczeq.rtspplayer.data.utils.DataState;

public abstract class StateDataRepository {

    protected MutableLiveData<DataState> state;

    public StateDataRepository() {
        this.state = new MutableLiveData<>();
        this.state.setValue(DataState.NOT_LOADED);
    }

    public MutableLiveData<DataState> getState() {
        return state;
    }
}
