package androdns.android.leetdreams.ch.androdns.ui.query

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QueryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Query Name"
    }
    val text: LiveData<String> = _text
}