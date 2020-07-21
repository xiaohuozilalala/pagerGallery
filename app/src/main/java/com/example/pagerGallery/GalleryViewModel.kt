package com.example.pagerGallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val factory = PixabayDataSourceFactory(application)
    val pagedListLiveData = factory.toLiveData(1)
    //中间人观察，以一个观察另一个livedata
    //从datasource中获得该变量！！！
    val networkStatus = Transformations.switchMap(factory.pixabayDataSource) {it.networkStatus}
    fun resetQuery() {
        //告知dataSource已经失效，让其重新生成dataSource（见dataSource）
        pagedListLiveData.value?.dataSource?.invalidate()
    }
    fun retry() {
        factory.pixabayDataSource.value?.retry?.invoke()
    }
}