package com.lolson.encryptsms.ui.about

import androidx.lifecycle.ViewModel
import com.lolson.encryptsms.utility.LogMe

/**
 * The view model is used to retain data between rebuilds.
 */
class AboutViewModel : ViewModel()
{
//    fun consStream() = liveData(viewModelScope.coroutineContext + Dispatchers.IO){
////        Runtime.getRuntime().exec("logcat -c")
//        Runtime.getRuntime().exec("logcat")
//            .inputStream
//            .bufferedReader()
//            .useLines { lines -> lines.forEach { line -> emit(line) } }
//    }
//
//    val conFlow: Flow<String> = flow {
//        l.d("Flow runup")
//        Runtime.getRuntime().exec("logcat -c")
//        Runtime.getRuntime().exec("logcat")
//            .inputStream
//            .bufferedReader()
//            .useLines { lines -> lines.forEach { line -> emit(line) } }
//    }
//        .onStart {
//            l.d("Flow started")
//            emit("FLOW START") }
//        .flowOn(Dispatchers.Default)
//
//    private var _text = conFlow.asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)
////    private var _text = consStream()
//
//    val text: LiveData<String>
//    get() = _text

    val l = LogMe()
    val appInfo =
            "Encrypt SMS 2021" +
            " \nCreated for class project MSSE 692/696 " +
                    "\n\nCreated by: Lucas Olson"
//    init
//    {
//        viewModelScope.launch(Dispatchers.IO){
//        }
//    }
}