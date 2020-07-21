package com.example.pagerGallery

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

//kotlin新引进，更便捷

data class Pixabay (
    val totalHits:Int,
    val hits:Array<PhotoItem>,
    val total:Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pixabay

        if (totalHits != other.totalHits) return false
        if (!hits.contentEquals(other.hits)) return false
        if (total != other.total) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalHits
        result = 31 * result + hits.contentHashCode()
        result = 31 * result + total
        return result
    }
}

//标注即可，不需再去实现Parcelable
@Parcelize data class PhotoItem (
    //序列化名称为对应数据名，变量名更合理
    @SerializedName("webformatURL") val previewURL:String,
    @SerializedName("id") val photoId:Int,
    @SerializedName("largeImageURL")val fullURL:String,
    @SerializedName("webformatHeight")val photoHeight:Int,
    @SerializedName("user") val photoUser:String,
    @SerializedName("likes") val photoLikes:Int,
    @SerializedName("favorites") val photoFavorites:Int
):Parcelable